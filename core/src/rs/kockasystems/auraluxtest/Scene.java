package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

public class Scene
{
	public SpriteBatch batch;
	
	public OrthographicCamera guiCamera;					// Camera for GUI (does not scale / translate etc)
	protected OrthographicCamera camera; 					// The almighty camera!
	protected BitmapFont font; 								// The font for writing
	protected Sound buttonPressSfx;							// SFX played on button press
	public final Vector3 coords = new Vector3(0, 0, 0);		// Click coordinates
	
	public void start()
	{
		// Variable setup
		buttonPressSfx 	= Gdx.audio.newSound(Gdx.files.internal("audio/button.wav"));
		batch 			= new SpriteBatch();
		guiCamera 		= new OrthographicCamera();
		font 			= new BitmapFont();
		
		// Setting up the camera
		guiCamera.setToOrtho(false, AuraluxTest.gameWidth,  AuraluxTest.gameHeight);
	}
	
	public void dispose()
	{
		// Disposing everything
		batch.dispose();
		font.dispose();
		buttonPressSfx.dispose();
	}
	
	public void render() { }
	
	public void pan(float x, float y, float deltaX, float deltaY) { }
	
	public void zoom(float initialDist, float dist) { }
	
	public void touchDown(int screenX, int screenY, int button) { unproject(screenX, screenY, guiCamera); }
	
	public void touchUp(int screenX, int screenY, int button) { unproject(screenX, screenY, guiCamera); }
	
	public void touchDragged(int screenX, int screenY) { unproject(screenX, screenY, guiCamera); }
	
	public void longPress(float screenX, float screenY) { unproject(screenX, screenY, camera); }
	
	/**
	 * A utility function to convert screen coordinates to world coordinates
	 * It is here (and not with other util functions) for a reason, as we only use it in here :)
	 * @param screenX screen x coord
	 * @param screenY screen y coord
	 * @return void
	 * @author MatejaS
	 * @version 1.0
	 * @since 1.3
	 * @date 31/10/2015 (Halloween!)
	 */
	public void unproject(int screenX, int screenY) { unproject(screenX, screenY, camera); }
	
	public void unproject(float screenX, float screenY, OrthographicCamera cam)
	{
		coords.x = screenX;
		coords.y = screenY;
		cam.unproject(coords);
		coords.y = AuraluxTest.gameHeight - coords.y - 1;
	}
	
}
