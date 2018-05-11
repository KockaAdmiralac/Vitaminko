package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Scene_Description extends Scene
{
	private static final String EXIT_TEXT = "Exit";
	private float EXIT_X, EXIT_Y;
	private static final float EXIT_SCALE = 1.2f;
	private TextBounds bounds;
	private Color LINE_COLOR = Color.valueOf("531412");
	private static final float SCALE_FACTOR = 0.75f;
	private float exitOffset;
	private boolean exitChange;
	private Sprite window, background, logo;
	private TextureRegion line;
	private Rectangle exitCollider;
	private Rectangle backCollider;
	private float bx, by;
	private boolean bChange;
	private float bOffset;
	private LevelInfo level;
	
	private static final float EXIT_Y_2 = 10.0f;
	private static final float OFFSET = 5.0f;
	private static final String BACK_TEXT = "Back";
	private static final float LINE_OFFSET = 12.0f;
	
	public Scene_Description(final LevelInfo level) { this.level = level; }
	
	public void start()
	{
		super.start();
		createBackground();
		createLogo();
		createWindow();
		createBack();
		createExit();
		createColliders();
		createLine();
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
		by = MathUtils.floor(window.getY() + 3.0f + 16.0f + bounds.height);
		
		bChange = false;
		bOffset = 0.0f;
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
		window.setRegion(ImageManager.file);
		window.setPosition(MathUtils.floor((AuraluxTest.gameWidth - window.getRegionWidth()) / 2.0f),MathUtils.floor((AuraluxTest.gameHeight - window.getRegionHeight()) / 2.0f));
		window.setSize(window.getRegionWidth(), window.getRegionHeight());
	}
	
	private void createLine() { line = ImageManager.get("line"); }
	
	@Override
	public void dispose()
	{
		
	}
	
	@Override
	public void render()
	{
		updateTouch();
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
			drawBackground();
			drawLogo();
			drawExit();
			drawWindow();
			drawLine();
			drawBack();
			drawComponents();
		batch.end();
	}
	
	private void updateTouch()
	{
		if (bChange)
		{
			buttonPressSfx.play();
			AuraluxTest.resumeScene();
			bChange = false;
		}
		
		if (exitChange)
		{
			Gdx.app.exit();
			exitChange = false;
		}
	}
	
	private void drawBackground() { background.draw(batch); }
	
	private void drawLogo()
	{
		final float newWidth = MathUtils.floor(logo.getRegionWidth() * SCALE_FACTOR);
		final float newHeight = MathUtils.floor(logo.getRegionHeight() * SCALE_FACTOR);
		logo.setBounds(MathUtils.floor((AuraluxTest.gameWidth - newWidth) / 2.0f), MathUtils.floor(AuraluxTest.gameHeight - newHeight), newWidth, newHeight);
		logo.draw(batch);
	}
	
	private void drawExit()
	{
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		font.draw(batch, EXIT_TEXT, EXIT_X, EXIT_Y - exitOffset);
	}
	
	private void drawLine()
	{
		float lineWidth = MathUtils.floor(window.getRegionWidth() - 2 * LINE_OFFSET);			
		float lineX = MathUtils.floor(window.getX() + LINE_OFFSET);
		float lineY = MathUtils.floor(by + 16.0f - 2.0f);
		
		batch.setColor(LINE_COLOR);
		batch.draw(line, lineX, lineY, lineWidth, line.getRegionHeight());
		batch.setColor(Color.WHITE);
	}
	
	private void drawBack()
	{
		font.setScale(1.5f, 1.5f);
		font.draw(batch, "Back", bx, by + bOffset);
	}
	
	private void drawComponents()
	{
		final float titleX = window.getX() + 30.0f;
		final float titleY = window.getY() + window.getRegionHeight() - 20.0f;
		
		String title = level.title;
		String text = level.description;
		
		font.setScale(1.3f, 1.3f);
		font.draw(batch, title, titleX, titleY);
		
		font.setScale(1.0f, 1.0f);
		font.drawMultiLine(batch, text, titleX, titleY - 2.2f * (1.3f * 12.0f));
	}
	
	private void drawWindow() { window.draw(batch); }
	
	@Override
	public void touchDown(int screenX, int screenY, int button)
	{
		super.touchDown(screenX, screenY, button);
		if (backCollider.contains(coords.x, coords.y)) bOffset = -2.0f;
		if (exitCollider.contains(coords.x, coords.y)) exitOffset = 2.0f;
	}

	@Override
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		bOffset = 0.0f;
		exitOffset = 0.0f;
		if (backCollider.contains(coords.x, coords.y)) bChange = true;
		if (exitCollider.contains(coords.x, coords.y)) exitChange = true;
	}
}
