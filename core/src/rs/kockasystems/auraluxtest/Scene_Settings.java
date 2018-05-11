package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Scene_Settings extends Scene 
{
	// Class variables
	private float EXIT_X, EXIT_Y;
	private float exitOffset;
	private float bx, by;
	private float bOffset;
	private float XP, YP;
	private float xp, yp;
	private boolean exitChange;
	private boolean bChange;
	private Rectangle exitCollider;
	private Rectangle backCollider;
	private TextureRegion line;
	private Sprite slider;
	private Sprite window, background, logo;
	private Checkbox enableParticlesCb, showSplashScreenCb;
	private Slider soundVolumeSl;
	private TextBounds bounds;
	
	// Constants
	private static final Color LINE_COLOR = Color.valueOf("531412");
	private static final Color LINE_COLOR_2 = Color.valueOf("380D0C");
	private static final String EXIT_TEXT = "Exit";
	private static final String BACK_TEXT = "Back";
	private static final float EXIT_SCALE = 1.2f;
	private static final float EXIT_Y_2 = 10.0f;
	private static final float OFFSET = 5.0f;
	private static final float SCALE_FACTOR = 0.75f;
	private static final float LINE_OFFSET = 12.0f;
	
	public void start()
	{
		super.start();
		createBackground();
		createLogo();
		createExit();
		createWindow();
		createBack();
		createColliders();
		createOther();
		createComponents();
	}
	
	private void createBackground()
	{
		background = new Sprite();
		background.setRegion(ImageManager.background);
		background.setBounds(0, 0, AuraluxTest.gameWidth, AuraluxTest.gameHeight);
	}
	
	private void createLogo()
	{
		logo = new Sprite();
		logo.setRegion(ImageManager.logo);
	}
	
	private void createExit()
	{
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		bounds = font.getBounds(EXIT_TEXT);
		
		EXIT_X = 10.0f;
		EXIT_Y = AuraluxTest.gameHeight - 10.0f;
		
		exitOffset = 0.0f;
		exitChange = false;
	}
	
	private void createWindow()
	{
		window = new Sprite();
		window.setRegion(ImageManager.settings);
		window.setPosition(MathUtils.floor((AuraluxTest.gameWidth - window.getRegionWidth()) / 2.0f), MathUtils.floor(2.0f * (AuraluxTest.gameHeight - window.getRegionHeight()) / 5.0f));
		window.setSize(window.getRegionWidth(), window.getRegionHeight());
	}
	
	private void createColliders()
	{
		exitCollider = new Rectangle(EXIT_X - OFFSET, EXIT_Y_2 - OFFSET, bounds.width + 2.0f * OFFSET, bounds.height + 2.0f * OFFSET);
		backCollider = new Rectangle(bx - 5.0f, AuraluxTest.gameHeight - by - 5.0f, bounds.width + 2 * 5.0f, bounds.height + 2 * 5.0f);
	}
	
	private void createBack()
	{
		font.setScale(1.5f, 1.5f);
		
		bounds = font.getBounds(BACK_TEXT);
		
		bx = MathUtils.floor(window.getX() + (window.getRegionWidth() - bounds.width) / 2.0f);
		by = MathUtils.floor(window.getY() + 19.0f + bounds.height);
		
		bChange = false;
		bOffset = 0.0f;
	}
	
	private void createComponents()
	{
		createEnableParticles();
		createShowSplash();
		createSoundVolume();
	}
	
	private void createEnableParticles()
	{
		enableParticlesCb = new Checkbox(2, Settings.areParticlesEnabled())
		{
			@Override
			public void onChanged(boolean isClicked)
			{
				// FIXME there is a bug with sound when particles are disabled
				Settings.setParticlesEnabled(isClicked);
			}
			
		};
	}
	
	private void createShowSplash()
	{
		showSplashScreenCb = new Checkbox(3, Settings.shouldShowSplashScreen())
		{
			@Override
			public void onChanged(boolean isClicked) { Settings.setShowSplashScreen(isClicked); }	
		};
	}
	
	private void createSoundVolume()
	{
		soundVolumeSl = new Slider(1, Settings.getSoundVolume())
		{
			@Override
			public void onChanged(float val) { Settings.setSoundVolume(val); }
		};
	}
	
	private void createOther()
	{
		slider = ImageManager.getSprite("slider");
		line = ImageManager.get("line");
		
		XP = MathUtils.floor(window.getX() + 21.0f);
		YP = MathUtils.floor(window.getY() + background.getRegionHeight() - 150f);
	}
	
	public void dispose()
	{
		super.dispose();
	}
	
	public void render()
	{
		super.render();
		updateTouch();
		updateComponents();
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
			drawBackground();
			drawLogo();
			drawExit();
			drawWindow();
			drawText();
			drawSlider();
			drawComponents();
		batch.end();
	}
	
	private void updateTouch()
	{
		if (bChange)
		{
			buttonPressSfx.play();
			Settings.save();
			AuraluxTest.resumeScene();
			bChange = false;
		}
		if (exitChange)
		{
			Gdx.app.exit();
			exitChange = false;
		}
	}
	
	private void updateComponents()
	{
		enableParticlesCb.update();
		showSplashScreenCb.update();
	}
	
	private void drawBackground() { background.draw(batch); }
	
	private void drawLogo()
	{
		final float newWidth = MathUtils.floor(ImageManager.logo.getWidth() * SCALE_FACTOR);
		final float newHeight = MathUtils.floor(ImageManager.logo.getHeight() * SCALE_FACTOR);
		final float posX = MathUtils.floor((AuraluxTest.gameWidth - newWidth) / 2.0f);
		final float posY = MathUtils.floor(AuraluxTest.gameHeight - newHeight);
		logo.setBounds(posX, posY, newWidth, newHeight);
		logo.draw(batch);
	}
	
	private void drawExit()
	{
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		font.draw(batch, EXIT_TEXT, EXIT_X, EXIT_Y - exitOffset);
	}
	
	private void drawWindow() { window.draw(batch); }
	
	private void drawText()
	{
		font.setScale(1.5f, 1.5f);
		
		xp = XP;
		yp = YP;
		font.draw(batch, "Sfx volume", xp, yp);
		endl();
		font.draw(batch, "Enable particles", xp, yp);
		endl();
		font.draw(batch, "Show splash screen", xp, yp);
		font.draw(batch, "Back", bx, by + bOffset);
	}
	
	private void drawSlider()
	{
		final float lineWidth = MathUtils.floor(window.getRegionWidth() - 2 * LINE_OFFSET);
		final float lineX = MathUtils.floor(window.getX() + LINE_OFFSET);
		final float lineY = MathUtils.floor(window.getY() + 48.0f);
		
		batch.setColor(LINE_COLOR);
		batch.draw(line, lineX, lineY, lineWidth, line.getRegionHeight());
		batch.setColor(Color.WHITE);
	}
	
	private void drawComponents()
	{
		enableParticlesCb.draw(batch);
		showSplashScreenCb.draw(batch);
		soundVolumeSl.draw(batch);	
	}
	
	private void endl() { yp -= 2 * 18.0f; }
	
	public void touchDown(int screenX, int screenY, int button)
	{
		super.touchDown(screenX, screenY, button);
		if (backCollider.contains(coords.x, coords.y)) bOffset = -2.0f;
		if (exitCollider.contains(coords.x, coords.y)) exitOffset = 2.0f;
		
		enableParticlesCb.processTouchDown();
		showSplashScreenCb.processTouchDown();
		soundVolumeSl.processTouchDown();
	}
	
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		bOffset = 0.0f;
		exitOffset = 0.0f;
		
		if (backCollider.contains(coords.x, coords.y)) bChange = true;
		if (exitCollider.contains(coords.x, coords.y)) exitChange = true;
		
		enableParticlesCb.processTouchUp();
		showSplashScreenCb.processTouchUp();
		soundVolumeSl.processTouchUp();
	}
	
	public void touchDragged(int screenX, int screenY)
	{
		super.touchDragged(screenX, screenY);
		soundVolumeSl.processTouchDragged();
	}
	
	private abstract class Checkbox
	{
		private static final int SIZE = 25;
		
		private float x, y, offset;
		private TextureRegion tr, trClicked;
		private Rectangle collider;
		private boolean pressed, clicked;
		
		public Checkbox(float x, float y, boolean isClicked)
		{
			this.x = MathUtils.floor(x);
			this.y = MathUtils.floor(y);
			
			tr = ImageManager.get("checkbox-off");
			trClicked = ImageManager.get("checkbox-on");
			
			collider = new Rectangle(x, AuraluxTest.gameHeight - y - SIZE, SIZE, SIZE);
			
			offset = 0.0f;
			pressed = false;
			
			clicked = isClicked;
		}
		
		public Checkbox(int line, boolean isClicked) { this(window.getX() + window.getWidth() - 3.0f - 18.0f - SIZE, YP - (2 * line - 1) * 18.0f - (SIZE - 18.0f) / 2.0f, isClicked); }
		
		public void update()
		{
			if (pressed)
			{
				buttonPressSfx.play();
				clicked = !clicked;
				onChanged(clicked);	
				pressed = false;
			}
		}
		
		public void draw(SpriteBatch batch) { batch.draw(clicked ? trClicked : tr, x, y + offset); }
		
		public void processTouchDown() { if (collider.contains(coords.x, coords.y)) offset = -2.0f; }
		
		public void processTouchUp()
		{
			offset = 0.0f;
			if (collider.contains(coords.x, coords.y)) pressed = true;
		}
		
		public abstract void onChanged(boolean isClicked);
		
	}
	
	private abstract class Slider
	{
		private static final float WIDTH = 150.0f;
		private float x, y, trX, trY;
		private float MIN_X, MAX_X;
		private boolean moving;
		
		public Slider(float x, float y, float percentage)
		{
			this.x = MathUtils.floor(x);
			this.y = MathUtils.floor(y);
			
			trX = MathUtils.floor(this.x + percentage * WIDTH);
			trY = MathUtils.floor(this.y + line.getRegionHeight() / 2.0f - slider.getHeight() / 2.0f);
			
			moving = false;
			
			MIN_X = this.x;
			MAX_X = MIN_X + WIDTH;
			
		}
		
		public Slider(int l, float percentage) { this(window.getX() + window.getWidth() - 21.0f - WIDTH, YP - 18 + (18.0f - line.getRegionHeight()) / 2.0f, percentage); }
		
		public void draw(SpriteBatch batch)
		{
			float width1 = MathUtils.floor(trX - slider.getRegionWidth() / 2.0f - x);
			
			if (width1 > 0)
			{
				batch.setColor(LINE_COLOR_2);
				batch.draw(line, x, y, width1, line.getRegionHeight());
			}
			
			float x2 = MathUtils.floor(trX + slider.getRegionWidth() / 2.0f);
			float width2 = MathUtils.floor(x + WIDTH - x2);
			
			if (width2 > 0)
			{
				batch.setColor(LINE_COLOR);
				batch.draw(line, x2, y, width2, line.getRegionHeight());
			}
			
			slider.setPosition(MathUtils.floor(trX - slider.getRegionWidth() / 2.0f), trY);
			batch.setColor(Color.WHITE);
			slider.draw(batch);
		}
		
		public void processTouchDown() { if (slider.getBoundingRectangle().contains(coords.x, AuraluxTest.gameHeight - coords.y)) moving = true; }
		
		public void processTouchUp()
		{
			if (moving)
			{
				buttonPressSfx.play();
				onChanged((trX - MIN_X) / WIDTH);
				moving = false;
			}
		}
		
		public void processTouchDragged()
		{
			if (moving)
			{
				trX = MathUtils.floor(coords.x);
				trX = MathUtils.clamp(trX, MIN_X, MAX_X);
			}
		}
		
		public abstract void onChanged(float val);
	}
	
}
