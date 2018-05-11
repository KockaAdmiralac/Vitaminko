package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Settings
{
	private static Preferences prefs;
	private static final boolean CLEAR_ON_LOAD = false;
	private static float soundVolume;
	private static boolean enableParticles, hideSplashScreen;
		
	public static void load()
	{
		prefs = Gdx.app.getPreferences("VitaminkoPrefs");
		
		if (CLEAR_ON_LOAD)
		{
			prefs.clear();
			prefs.flush();
		}
		soundVolume = prefs.getFloat("sound_volume", 0.5f);
		enableParticles = prefs.getBoolean("enable_particles", true);
		hideSplashScreen = prefs.getBoolean("show_splashscreen", true);
	}
		
	public static float getSoundVolume() { return soundVolume; }
		
	public static void setSoundVolume(float value)
	{
		prefs.putFloat("sound_volume", value);
		soundVolume = value;
	}
		
	public static boolean areParticlesEnabled() { return enableParticles; }
	
	public static void setParticlesEnabled(boolean value)
	{
		prefs.putBoolean("enable_particles", value);
		enableParticles = value;
	}
	
	public static boolean shouldShowSplashScreen()
	{
		return !hideSplashScreen;
	}
	
	public static void setShowSplashScreen(boolean value)
	{
		prefs.putBoolean("show_splashscreen", value);
		hideSplashScreen = value;
	}
		
	public static void save() { prefs.flush(); }
}
