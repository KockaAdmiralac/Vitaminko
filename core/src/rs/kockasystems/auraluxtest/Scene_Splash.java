package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.graphics.g2d.Sprite;

public class Scene_Splash extends Scene
{
	// Variables
	private Sprite splash;
	private float alpha = 0.1f;
	private boolean stop;
	
	// Constants
	private static final float SPLASH_STEP = 0.01f;
	
	public void start()
	{
		super.start();
		createSplash();
	}
	
	private void createSplash()
	{
		splash = new Sprite();
		splash.setRegion(ImageManager.splash);
		splash.setBounds(0, 0, AuraluxTest.gameWidth, AuraluxTest.gameHeight);
	}
	
	public void render()
	{
		updateOpacity();
		batch.begin();
			splash.setAlpha(alpha);
			splash.draw(batch);
		batch.end();
	}
	
	private void updateOpacity()
	{
		if(alpha > 0)
		{
			if(alpha >= 1f) stop = true;
			alpha += (stop ? -1 : 1) * SPLASH_STEP;
		}
		else AuraluxTest.changeScene(new Scene_Menu());
	}
}
