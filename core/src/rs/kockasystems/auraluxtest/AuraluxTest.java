/**
 * @name AuraluxTest
 * @package rs.kockasystems.vitaminko
 * @date 20/11/15
 * @author Akimil
 */

package rs.kockasystems.auraluxtest;

// Library imports
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

/**
* The main class of the application.
*/

public class AuraluxTest extends ApplicationAdapter
{
	
	// The game base resolution
	public static final int WIDTH				= 800;
	public static final int HEIGHT 				= 480;
	
	// The actual screen resolution (please note in DesktopLauncher it's now SCREEN_WIDTH and SCREEN_HEIGHT, not WIDTH and HEIGHT)
	public static final int SCREEN_WIDTH 		= 800; 
	public static final int SCREEN_HEIGHT 		= 480;
	
	// Variables
	public static float gameWidth, gameHeight;	// The game "actual" resolution
	public static BluetoothConnection btc;		// Bluetooth connection class
	public static Scene scene, overScene;		// Current Scene
	public static boolean overScened;			// Is main scene overscened?
	
	public AuraluxTest(BluetoothConnection btc) { AuraluxTest.btc = btc; }
	
	/** 
	* The default method that is called when window is opened
	*/
	
	@Override
	public void create ()
	{
		setGameScale();
		
		// Initializing settings and images.
		Settings.load();
		if(!ImageManager.init())System.out.println("Error initializing images.");
		
		// Setting the Input processor
		Gdx.input.setInputProcessor(new Input());
		
		scene = firstScene();
		scene.start();
		
	}
	
	private void setGameScale()
	{
		final float scaleX = (float) SCREEN_WIDTH / (float) WIDTH;
		final float scaleY = (float) SCREEN_HEIGHT / (float) HEIGHT;
		final float scale = Math.min(scaleX, scaleY);
		
		gameWidth = SCREEN_WIDTH / scale;
		gameHeight = SCREEN_HEIGHT / scale;
	}
	
	public Scene firstScene(){ return Settings.shouldShowSplashScreen() ? new Scene_Menu() : new Scene_Splash(); }
	
	public static void changeScene(Scene newScene)
	{
		scene.dispose();
		scene = newScene;
		scene.start();
	}
	
	public static void overScene(Scene newOverScene)
	{
		overScene = scene;
		scene = newOverScene;
		scene.start();
		overScened = true;
	}
	
	public static void resumeScene()
	{
		// TODO : Da li da ovde pozovem scene.dispose()? Meni je crashovalo...
		scene = overScene;
		overScene = null;
		overScened = false;
	}
	
	/**
	 * The default method that is called when window is closed.
	 */
	@Override
	public void dispose()
	{
		scene.dispose();
		if(overScened)overScene.dispose();
		ImageManager.dispose();
	}
	
	
	/**
	 * Called on Android when home button is pressed / a call is received, and, more importantly,
	 * right before calling {@link #dispose()}, when the app is closed.
	 * Used to save the settings.
	 */
	@Override
	public void pause() { Settings.save(); }
	
	
	/**
	 * The default method called on every 60th part of a second.
	 */
	@Override
	public void render ()
	{
		// Clearing
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		scene.render();
	}

}