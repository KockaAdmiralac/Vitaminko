package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class LevelLoader
{
	private Json json;
	
	public LevelLoader()
	{
		json = new Json();
		json.setTypeName(null);
		json.setUsePrototypes(false);
		json.setIgnoreUnknownFields(true);
		json.setOutputType(OutputType.json);
	}
	
	public LevelsInfo loadLevelsInfo() { return json.fromJson(LevelsInfo.class, Gdx.files.internal("levels/info")); }
	
	public LevelData loadLevel(int levelId)
	{
		final String fileName = "levels/" + (levelId + 1);
		return json.fromJson(LevelData.class, Gdx.files.internal(fileName));
	}
}
