/**
 * 
 */
package db.migration.mysql;

import java.io.BufferedReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.camera.Camera;

/**
 * Following the creation of a camera_json field on Render, we should extract
 * the "Camera" portion from any existing Scene records and store them in their
 * corresponding Render entities.
 * 
 * @author snowjak88
 *
 */
public class V0_3_5_2__MigrateCameraJson extends BaseJavaMigration {
	
	@Override
	public void migrate(Context context) throws Exception {
		
		try (Statement selectScene = context.getConnection().createStatement()) {
			try (ResultSet resultSet = selectScene
					.executeQuery("SELECT s.json, r.uuid FROM scene s INNER JOIN render r ON r.scene_id = s.id")) {
				try (PreparedStatement updateRender = context.getConnection()
						.prepareStatement("UPDATE render SET temp_camera_json = ? WHERE uuid = ?")) {
					while (resultSet.next()) {
						
						final String sceneJson;
						final StringBuffer sceneJsonBuffer = new StringBuffer();
						try (BufferedReader sceneReader = new BufferedReader(
								resultSet.getClob("json").getCharacterStream())) {
							String nextLine = sceneReader.readLine();
							while (nextLine != null) {
								sceneJsonBuffer.append(nextLine);
								nextLine = sceneReader.readLine();
							}
						}
						sceneJson = sceneJsonBuffer.toString();
						
						final UUID renderUUID = UUID.fromString(resultSet.getString("uuid"));
						
						final Scene scene = Settings.getInstance().getGson().fromJson(sceneJson, Scene.class);
						final Camera camera = scene.getCamera();
						final String cameraJson = Settings.getInstance().getGson().toJson(camera);
						
						updateRender.setString(1, cameraJson);
						updateRender.setString(2, renderUUID.toString());
						updateRender.execute();
					}
				}
			}
		}
	}
	
}
