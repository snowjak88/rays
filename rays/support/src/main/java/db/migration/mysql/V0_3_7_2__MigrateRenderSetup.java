/**
 * 
 */
package db.migration.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.snowjak.rays.Settings;
import org.snowjak.rays.sampler.Sampler;

/**
 * Following the creation of the new render_setup table, we need to migrate over
 * such fields from render as belong there.
 * 
 * @author snowjak88
 *
 */
public class V0_3_7_2__MigrateRenderSetup extends BaseJavaMigration {
	
	@Override
	public void migrate(Context context) throws Exception {
		
		System.out.println();
		System.out.println("Migrating Render configurations to new RenderSetup instances ...");
		
		//
		// First, we process all the parents.
		//
		try (Statement selectRender = context.getConnection().createStatement()) {
			try (ResultSet renders = selectRender.executeQuery(
					"SELECT uuid, created, scene_id, film_json, renderer_json, sampler_json, camera_json FROM render WHERE parent_uuid IS NULL")) {
				try (PreparedStatement insertRenderSetup = context.getConnection().prepareStatement(
						"INSERT INTO render_setup ( version, created, scene_id, film_json, renderer_json, sampler_json, camera_json ) "
								+ "values ( 1, ?, ?, ?, ?, ?, ? )",
						Statement.RETURN_GENERATED_KEYS)) {
					try (PreparedStatement updateRender = context.getConnection()
							.prepareStatement("UPDATE render SET temp_render_setup_id = ? WHERE uuid = ?")) {
						while (renders.next()) {
							
							final var uuid = renders.getString("uuid");
							final var created = renders.getTimestamp("created");
							final var sceneID = renders.getInt("scene_id");
							final var filmJson = renders.getString("film_json");
							final var rendererJson = renders.getString("renderer_json");
							final var samplerJson = renders.getString("sampler_json");
							final var cameraJson = renders.getString("camera_json");
							
							System.out.println("Migrating Render (UUID = " + uuid + ")");
							
							//
							// Insert a new RenderSetup corresponding to the Render we just read.
							insertRenderSetup.setTimestamp(1, created);
							insertRenderSetup.setInt(2, sceneID);
							insertRenderSetup.setString(3, filmJson);
							insertRenderSetup.setString(4, rendererJson);
							insertRenderSetup.setString(5, samplerJson);
							insertRenderSetup.setString(6, cameraJson);
							insertRenderSetup.execute();
							
							//
							// Get the ID of the RenderSetup we just inserted.
							final long renderSetupID;
							
							try (ResultSet renderSetupKey = insertRenderSetup.getGeneratedKeys()) {
								
								if (renderSetupKey.next()) {
									renderSetupID = renderSetupKey.getLong(1);
									
									System.out.println("Created a new RenderSetup (ID=" + renderSetupID + ").");
									
									//
									// Write back that new ID to the Render we just read.
									updateRender.setLong(1, renderSetupID);
									updateRender.setString(2, uuid);
									updateRender.execute();
									
									//
									// Now we can start migrating the children of this Render (if any exist).
									migrateRenderChildren(context, uuid, renderSetupID);
								}
							}
							
							System.out.println("Finished migrating Render (UUID = " + uuid + ")");
						}
					}
				}
			}
		}
		
		System.out.println("Finished migrating all Render configurations to RenderSetup instances!");
		
	}
	
	private void migrateRenderChildren(Context context, String parentUuid, long renderSetupID) throws SQLException {
		
		System.out.println("Migrating children of UUID=" + parentUuid);
		
		try (PreparedStatement updateChild = context.getConnection().prepareStatement(
				"UPDATE render SET temp_render_setup_id = ?, offsetx = ?, offsety = ?, width = ?, height = ? WHERE uuid = ?")) {
			try (ResultSet childRenders = context.getConnection().createStatement().executeQuery(
					"SELECT uuid, sampler_json FROM render WHERE IFNULL( parent_uuid, '' ) = '" + parentUuid + "'")) {
				while (childRenders.next()) {
					
					//
					// For each child render, we want to set the following:
					// render_setup_id
					// width
					// height
					// offsetX
					// offsetY
					//
					final var childUuid = childRenders.getString("uuid");
					
					System.out.println("Migrating child of " + parentUuid + " -- UUID=" + childUuid);
					
					final var samplerJson = childRenders.getString("sampler_json");
					final var sampler = Settings.getInstance().getGson().fromJson(samplerJson, Sampler.class);
					
					final var offsetX = sampler.getXStart();
					final var offsetY = sampler.getYStart();
					final var width = sampler.getXEnd() - sampler.getXStart() + 1;
					final var height = sampler.getYEnd() - sampler.getYStart() + 1;
					
					System.out.println("Child dimensions (width x height) = (" + width + "x" + height + ") -- offsets ("
							+ offsetX + "," + offsetY + ")");
					
					updateChild.setLong(1, renderSetupID);
					updateChild.setInt(2, offsetX);
					updateChild.setInt(3, offsetY);
					updateChild.setInt(4, width);
					updateChild.setInt(5, height);
					updateChild.setString(6, childUuid);
					updateChild.execute();
					
					//
					// See if there are any children of this Render to process.
					migrateRenderChildren(context, childUuid, renderSetupID);
					
					System.out.println("Finished migrating child of " + parentUuid + " -- UUID=" + childUuid);
				}
			}
		}
		
		System.out.println("Finished migrating children of UUID=" + parentUuid);
	}
	
}
