/**
 * 
 */
package db.migration.mysql;

import java.io.BufferedReader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * As we've renamed {@link SpectralPowerDistribution}.radiance to .luminance, we
 * should ensure that any references to those fields are updated.
 * 
 * @author snowjak88
 *
 */
public class V0_4_3_1__RenameSPDRadianceAsLuminance extends BaseJavaMigration {
	
	@Override
	public void migrate(Context context) throws Exception {
		
		final Pattern radiancePattern = Pattern.compile("(\"radiance\"):\\s*(\\d+)");
		
		try (Statement selectScene = context.getConnection().createStatement()) {
			try (PreparedStatement updateJson = context.getConnection()
					.prepareStatement("UPDATE scene SET json = ? WHERE id = ?")) {
				try (ResultSet scenes = selectScene
						.executeQuery("SELECT id, json FROM scene WHERE json LIKE '%radiance%'")) {
					while (scenes.next()) {
						
						final int sceneID = scenes.getInt("id");
						
						final String json;
						final StringBuffer jsonBuffer = new StringBuffer();
						try (BufferedReader jsonReader = new BufferedReader(
								scenes.getClob("json").getCharacterStream())) {
							String nextLine = jsonReader.readLine();
							while (nextLine != null) {
								jsonBuffer.append(nextLine);
								nextLine = jsonReader.readLine();
							}
						}
						json = jsonBuffer.toString();
						
						final Map<String, String> replacements = new HashMap<>();
						
						final Matcher radianceMatcher = radiancePattern.matcher(json);
						radianceMatcher.results().forEach(mr -> {
							final var wholeRadiance = mr.group(0);
							
							replacements.put(wholeRadiance, wholeRadiance.replace("radiance", "luminance"));
						});
						
						replacements.forEach((k, v) -> json.replaceAll(k, v));
						
						updateJson.setClob(1, new StringReader(json));
						updateJson.setInt(2, sceneID);
						updateJson.execute();
					}
				}
			}
		}
	}
}
