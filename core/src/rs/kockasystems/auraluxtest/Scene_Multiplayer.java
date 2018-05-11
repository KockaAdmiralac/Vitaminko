package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Scene_Multiplayer extends Scene
{
	private Sprite connectButton, acceptButton;
	private boolean connecting, failed;
	private float EXIT_X, EXIT_Y;
	private Rectangle exitCollider;
	private float exitOffset;
	private boolean exitChange, host, changeScene;
	
	private static final String MULTIPLAYER_TEXT = "Ovo je mod gde se igra protiv drugim korisnika Vitaminka!\nPovezivanje se obavlja preko Bluetooth konekcije, i onda nastupa bitka!";
	private static final String EXIT_TEXT = "Back";
	private static final float EXIT_SCALE = 1.2f;
	private static final float EXIT_Y_2 = 10.0f;
	private static final float OFFSET = 5.0f;
	
	public void start()
	{
		super.start();
		createConnectButton();
		createAcceptButton();
		createExit();
	}
	
	private void createConnectButton()
	{
		connectButton = ImageManager.getSprite("button");
		connectButton.setPosition(AuraluxTest.gameWidth / 4 - (connectButton.getRegionWidth() / 2), AuraluxTest.gameHeight / 4 - (connectButton.getRegionHeight() / 2));
	}
	
	private void createAcceptButton()
	{
		acceptButton = ImageManager.getSprite("button");
		acceptButton.setPosition(AuraluxTest.gameWidth * 3 / 4 - (connectButton.getRegionWidth() / 2), AuraluxTest.gameHeight / 4 - (connectButton.getRegionHeight() / 2));
	}
	
	private void createExit()
	{
		font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		
		TextBounds bounds = font.getBounds(EXIT_TEXT);
		
		EXIT_X = 10.0f;
		EXIT_Y = AuraluxTest.gameHeight - 10.0f;
		
		exitCollider = new Rectangle(EXIT_X - OFFSET, EXIT_Y_2 - OFFSET, bounds.width + 2.0f * OFFSET, bounds.height + 2.0f * OFFSET);
		
		exitOffset = 0.0f;
		exitChange = false;
	}
	
	public void render()
	{
		super.render();
		updateExit();
		updateChange();
		batch.begin();
			batch.draw(ImageManager.background, 0, 0);
			batch.draw(ImageManager.logo, MathUtils.floor((AuraluxTest.gameWidth - ImageManager.logo.getWidth()) / 2.0f), MathUtils.floor(3.0f * (AuraluxTest.gameHeight - ImageManager.logo.getHeight()) / 5.0f)); 
			connectButton.draw(batch);
			acceptButton.draw(batch);
			font.draw(batch, "Join", AuraluxTest.gameWidth / 4 - (connectButton.getRegionWidth() / 4), AuraluxTest.gameHeight / 4);
			font.draw(batch, "Host", AuraluxTest.gameWidth * 3 / 4 - (connectButton.getRegionWidth() / 4), AuraluxTest.gameHeight / 4);
			if(connecting)font.draw(batch, "Connecting...", AuraluxTest.gameWidth / 2, AuraluxTest.gameHeight / 4);
			else if(failed)font.draw(batch, "Failed to connect.", AuraluxTest.gameWidth / 2, AuraluxTest.gameHeight / 4);
			font.drawMultiLine(batch, MULTIPLAYER_TEXT, AuraluxTest.gameWidth / 4, AuraluxTest.gameHeight / 4 - 48);
			font.setScale(EXIT_SCALE, EXIT_SCALE);	
			font.draw(batch, EXIT_TEXT, EXIT_X, EXIT_Y - exitOffset);
		batch.end();
	}
	
	private void updateExit()
	{
		if (exitChange)
		{
			AuraluxTest.changeScene(new Scene_Menu());
			exitChange = false;
		}
	}
	
	private void updateChange()
	{ if(changeScene)AuraluxTest.changeScene(new Scene_Level(true, host)); }
	
	@Override
	public void touchDown(int screenX, int screenY, int button)
	{
		if(connecting)return;
		if(acceptButton.getBoundingRectangle().contains(screenX, screenY))
		{
			new Thread(){ public void run(){ accept(); } }.start();
			connecting = true;
		}
		else if(connectButton.getBoundingRectangle().contains(screenX, screenY))
		{
			new Thread(){ public void run(){ scan(); } }.start();
			connecting = true;
		}
	}
	
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		exitOffset = 0.0f;
		if (exitCollider.contains(coords.x, coords.y)) exitChange = true;
	}
	
	private void accept()
	{
		try
		{
			LevelLoader loader = new LevelLoader();
			host = true;
			AuraluxTest.btc.accept();
			while(!AuraluxTest.btc.isConnected());
			changeScene = true;
		}
		catch(Exception e)
		{
			connecting = false;
			failed = true;
		}
		
	}
	
	private void connect()
	{
		try
		{
			host = false;
			AuraluxTest.btc.connect(AuraluxTest.btc.getScannedDevice());
			while(!AuraluxTest.btc.isConnected());
			changeScene = true;
		}
		catch(Exception e)
		{
			connecting = false;
			failed = true;
		}
	}
	
	private void scan()
	{
		try
		{
			AuraluxTest.btc.scan();
			while(AuraluxTest.btc.getScannedDevice() == null);
			new Thread(){ public void run(){ connect(); } }.start();
		}
		catch(Exception e)
		{
			connecting = false;
			failed = true;
		}
	}
	
}
