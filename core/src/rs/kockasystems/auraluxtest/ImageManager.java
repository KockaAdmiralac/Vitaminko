package rs.kockasystems.auraluxtest;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ImageManager
{
	private static TextureAtlas atlas;
	public static Texture logo, background, levels, card, settings, splash, file;
	private static TextureRegion temp;
	
	private static Map<String, TextureRegion> _cache;
	
	public static boolean init()
	{
		try
		{
			_cache = new HashMap<String, TextureRegion>();
			atlas = new TextureAtlas("res/ui.atlas");
			createTextures();
			return true;
		}
		catch(Exception e) { return false; }
		
	}
	
	private static void createTextures()
	{
		logo 		= loadTexture("logo");
		background 	= loadTexture("background");
		levels 		= loadTexture("levels");
		card		= loadTexture("card");
		settings	= loadTexture("settings");
		splash		= loadTexture("splash");
		file		= loadTexture("file");
	}
	
	private static Texture loadTexture(final String name){ return new Texture("res/" + name + ".png"); }
	
	private static void disposeTextures()
	{
		logo.dispose();
		background.dispose();
		levels.dispose();
		card.dispose();
		settings.dispose();
	}
	
	public static TextureRegion get(final String name) { return (_cache.get(name) == null) ? atlas.findRegion(name) : _cache.get(name); }
	
	public static Sprite getSprite(final String name)
	{
		Sprite sprite = new Sprite();
		temp = get(name);
		sprite.setRegion(temp);
		sprite.setSize(temp.getRegionWidth(), temp.getRegionHeight());
		return sprite;
	}
	
	public static void dispose()
	{
		atlas.dispose();
		disposeTextures();
	}
	
}
