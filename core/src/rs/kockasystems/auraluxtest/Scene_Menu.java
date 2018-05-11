package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Scene_Menu extends Scene
{
	private static final String EXIT_TEXT = "Exit";
	private static final float EXIT_SCALE = 1.2f;
	private static final float EXIT_Y_2 = 10.0f;
	private static final float OFFSET = 5.0f;
	
	private float EXIT_X, EXIT_Y;
	private Rectangle exitCollider;
	private float exitOffset;
	private boolean exitChange;
	private Sprite singleplayer, multiplayer;
	
	public void start()
	{
		super.start();
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		
		TextBounds bounds = font.getBounds(EXIT_TEXT);
		
		EXIT_X = 10.0f;
		EXIT_Y = AuraluxTest.gameHeight - 10.0f;
		
		exitCollider = new Rectangle(EXIT_X - OFFSET, EXIT_Y_2 - OFFSET, bounds.width + 2.0f * OFFSET, bounds.height + 2.0f * OFFSET);
		
		exitOffset = 0.0f;
		exitChange = false;
		
		createSingleplayerButton();
		createMultiplayerButton();
		
	}
	
	private void createSingleplayerButton()
	{
		singleplayer = ImageManager.getSprite("singleplayer");
		singleplayer.setPosition(AuraluxTest.gameWidth / 4 - (singleplayer.getWidth() / 2), AuraluxTest.gameHeight / 4 - (singleplayer.getHeight() / 2));
	}
	
	private void createMultiplayerButton()
	{
		multiplayer = ImageManager.getSprite("multiplayer");
		multiplayer.setPosition(AuraluxTest.gameWidth * 3 / 4 - (multiplayer.getWidth() / 2), AuraluxTest.gameHeight / 4 - (multiplayer.getHeight() / 2));
	}
	
	public void render()
	{
		if (exitChange)
		{
			Gdx.app.exit();
			exitChange = false;
		}
		
		batch.begin();
			batch.draw(ImageManager.background, 0, 0, AuraluxTest.gameWidth, AuraluxTest.gameHeight);
			batch.draw(ImageManager.logo, MathUtils.floor((AuraluxTest.gameWidth - ImageManager.logo.getWidth()) / 2.0f), MathUtils.floor(3.0f * (AuraluxTest.gameHeight - ImageManager.logo.getHeight()) / 5.0f)); 
			singleplayer.draw(batch);
			multiplayer.draw(batch);
			font.setScale(EXIT_SCALE, EXIT_SCALE);	
			font.draw(batch, EXIT_TEXT, EXIT_X, EXIT_Y - exitOffset);
		batch.end();
	}
	
	public void touchDown(int screenX, int screenY, int button)
	{
		super.touchDown(screenX, screenY, button);
		if (singleplayer.getBoundingRectangle().contains(coords.x, AuraluxTest.gameHeight - coords.y))
		{
			buttonPressSfx.play();
			AuraluxTest.changeScene(new Scene_Level(false, false));
		}
		
		else if(multiplayer.getBoundingRectangle().contains(coords.x, AuraluxTest.gameHeight - coords.y))
		{
			buttonPressSfx.play();
			AuraluxTest.changeScene(new Scene_Multiplayer());
		}
	}
	
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		exitOffset = 0.0f;
		
		if (exitCollider.contains(coords.x, coords.y)) exitChange = true;
	}
}
