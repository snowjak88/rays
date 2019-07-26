/**
 * 
 */
package db.migration.hsqldb;

import java.io.StringReader;
import java.sql.PreparedStatement;
import java.util.Arrays;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.filter.MitchellFilter;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.light.DiffuseLight;
import org.snowjak.rays.material.LambertianMaterial;
import org.snowjak.rays.material.PerfectMirrorMaterial;
import org.snowjak.rays.renderer.PathTracingRenderer;
import org.snowjak.rays.sampler.StratifiedSampler;
import org.snowjak.rays.shape.PlaneShape;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class V0_0_1__SetupSampleSceneAndRender extends BaseJavaMigration {
	
	@Override
	public void migrate(Context context) throws Exception {
		
		final Scene scene = new Scene(
				Arrays.asList(
						new Primitive(
								new PlaneShape(new TranslationTransform(0, 0, 4),
										new RotationTransform(Vector3D.I, 90)),
								new LambertianMaterial(new ConstantTexture(RGB.WHITE))),
						new Primitive(
								new PlaneShape(new TranslationTransform(-3, 0, 0),
										new RotationTransform(Vector3D.K, 90)),
								new LambertianMaterial(new ConstantTexture(RGB.RED))),
						new Primitive(
								new PlaneShape(new TranslationTransform(+3, 0, 0),
										new RotationTransform(Vector3D.K, -90)),
								new LambertianMaterial(new ConstantTexture(RGB.BLUE))),
						new Primitive(new PlaneShape(new TranslationTransform(0, -1, 0)),
								new LambertianMaterial(new ConstantTexture(RGB.WHITE))),
						new Primitive(new PlaneShape(new TranslationTransform(0, +5, 0)),
								new LambertianMaterial(new ConstantTexture(RGB.WHITE))),
						new Primitive(new SphereShape(1.0, new TranslationTransform(-1.5, 0, 0)),
								new LambertianMaterial(new ConstantTexture(RGB.WHITE))),
						new Primitive(new SphereShape(1.0, new TranslationTransform(+1.5, 0, 0)),
								new PerfectMirrorMaterial(new ConstantTexture(RGB.WHITE)))),
				Arrays.asList(new DiffuseLight(new SphereShape(0.5, new TranslationTransform(0, 3, 0)),
						SpectralPowerDistribution.fromBlackbody(2800, 250))));
		
		final RenderTask rt = new RenderTask(new StratifiedSampler(0, 0, 399, 299, 4, 9, 9),
				new PathTracingRenderer(4, 4),
				new Film(400, 300, 16.0, 0.04, 100, 12.4, new MitchellFilter(1, 0.33333, 0.33333)), scene, null);
		
		try (PreparedStatement insertSampleScene = context.getConnection()
				.prepareStatement("insert into scene ( version, nickname, json ) values ( 1, 'Sample Scene', ? )")) {
			
			final String sceneJson = Settings.getInstance().getGson().toJson(scene);
			insertSampleScene.setClob(1, new StringReader(sceneJson));
			
			insertSampleScene.execute();
		}
		
		try (PreparedStatement insertRender = context.getConnection().prepareStatement(
				"insert into render ( uuid, version, scene_id, decomposed, width, height, spp, offsetx, offsety, sampler_json, renderer_json, film_json ) "
						+ "select ?, s.id, ?, ?, ?, ?, ?, 0, 0, ?, ?, ? "
						+ "from scene s where s.nickname = 'Sample Scene'")) {
			
			final String samplerJson = Settings.getInstance().getGson().toJson(rt.getSampler()),
					rendererJson = Settings.getInstance().getGson().toJson(rt.getRenderer()),
					filmJson = Settings.getInstance().getGson().toJson(rt.getFilm());
			
			insertRender.setString(1, rt.getUuid().toString());
			insertRender.setInt(2, 1);
			insertRender.setBoolean(3, false);
			insertRender.setInt(4, rt.getFilm().getWidth());
			insertRender.setInt(5, rt.getFilm().getHeight());
			insertRender.setInt(6, rt.getSampler().getSamplesPerPixel());
			insertRender.setString(7, samplerJson);
			insertRender.setString(8, rendererJson);
			insertRender.setString(9, filmJson);
			
			insertRender.execute();
		}
		
	}
	
}
