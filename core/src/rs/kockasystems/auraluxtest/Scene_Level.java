package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Scene_Level extends Scene
{
	// Creating variables
	private Rectangle exitCollider, cardCollider, resumeCollider, settingsCollider;
	private float exitOffset, offsetCard, settingsOffset, resumeOffset;
	private float SETTINGS_X, SETTINGS_Y;
	private float RESUME_X, RESUME_Y;
	private float ix, iy, lOffset;
	private float cardX, cardY;
	private float FILES_X, FILES_Y;
	private boolean exitChange, toChange, toChange2;
	private boolean leftActive, rightActive;
	private boolean resumeChanged;
	private boolean filesChanged;
	private static boolean toResume;
	private Texture levelImages;
	private Texture card;
	private Sprite rightArrow, leftArrow, rightArrowGray, leftArrowGray;
	private int cardIndex, deltaIndex;
	private LevelLoader levelLoader;
	private LevelsInfo levelsInfo;
	private LevelInfo currentLevel;
	private TextBounds bounds;
	private Rectangle filesCollider;
	private float EXIT_X, EXIT_Y;
	private boolean multiplayer, host;
	
	// Constants
	private static final String FILES_TEXT = "Info";
	private static final float EXIT_SCALE = 1.2f;
	private static final String EXIT_TEXT = "Exit";
	private static final String SETTINGS_TEXT = "Settings";
	private static final float SETTINGS_SCALE = 1.2f;
	private static final String RESUME_TEXT = "Resume";
	private static final float RESUME_SCALE = 1.6f;
	private static final float EXIT_Y_2 = 10.0f;
	private static final float SETTINGS_Y_2 = 10.0f;
	private static final float OFFSET = 5.0f;
	private static final int LEVEL_CARD = 160;
	private static final int LEVEL_WIDTH = 3;
	private static final float SCALE_FACTOR = 0.75f;
	
	public static void onShouldResume(){ toResume = true; }
	
	public Scene_Level(final boolean multiplayer, final boolean host)
	{
		this.multiplayer = multiplayer;
		this.host = host;
		
	}
	
	@Override
	public void start()
	{
		super.start();
		createLevels();
		createCard();
		createArrows();
		createSettings();
		createExit();
		createResume();
		createInfo();
		createColliders();
		createOther();
		updateLevel();
	}
	
	private void createLevels()
	{
		levelLoader = new LevelLoader();
		levelsInfo = levelLoader.loadLevelsInfo();
		
		levelImages = ImageManager.levels;
		levelImages.setFilter(TextureFilter.Linear, TextureFilter.Linear);
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
	
	private void createSettings()
	{
		font.setScale(SETTINGS_SCALE, SETTINGS_SCALE);
		bounds = font.getBounds(SETTINGS_TEXT);
		
		SETTINGS_X = AuraluxTest.gameWidth - bounds.width - 10.0f;
		SETTINGS_Y = AuraluxTest.gameHeight - 10.0f;
		
		settingsOffset = 0.0f;
	}
	
	private void createCard()
	{
		card = ImageManager.card;
		cardIndex = 0;
		cardX = MathUtils.floor((AuraluxTest.gameWidth - card.getWidth()) / 2.0f);
		cardY = MathUtils.floor((AuraluxTest.gameHeight - card.getHeight()) / 2.0f);
	}
	
	private void createInfo()
	{
		float lOffset = (card.getWidth() - 160.0f) / 2.0f;
		
		ix = MathUtils.floor(cardX + lOffset);
		iy = MathUtils.floor(cardY + card.getHeight() - 160 - lOffset);
		
		float ty = MathUtils.floor(iy - 20.0f);
		
		font.setScale(1.2f, 1.2f);
		bounds = font.getBounds(FILES_TEXT);
		
		FILES_X = MathUtils.floor((AuraluxTest.gameWidth - bounds.width) / 2.0f);
		FILES_Y = MathUtils.floor(cardY + (ty - cardY) / 2.0f);
		
		filesChanged = false;
	}
	
	private void createResume()
	{
		font.setScale(RESUME_SCALE, RESUME_SCALE);
		
		bounds = font.getBounds(RESUME_TEXT);
		
		RESUME_X = MathUtils.floor((AuraluxTest.gameWidth - bounds.width) / 2.0f);
		RESUME_Y = MathUtils.floor(cardY / 2.0f + bounds.height / 2.0f);
		
		toResume = false;
		resumeChanged = false;
		resumeOffset = 0.0f;
	}
	
	private void createArrows()
	{
		createOnArrows();
		createOffArrows();
	}
	
	private void createOnArrows()
	{
		rightArrow = ImageManager.getSprite("arrow-on");
		iy = MathUtils.floor(cardY + (card.getHeight() - rightArrow.getRegionHeight()) / 2.0f);
		ix = cardX + card.getWidth() + 20.0f;
		rightArrow.setPosition(ix, iy);
		leftArrow = ImageManager.getSprite("arrow-on");
		ix = cardX - rightArrow.getRegionWidth() - 20.0f;
		leftArrow.setPosition(ix, iy);
		leftArrow.setFlip(true, false);
	}
	
	private void createOffArrows()
	{
		rightArrowGray = ImageManager.getSprite("arrow-off");
		ix = cardX + card.getWidth() + 20.0f;
		rightArrowGray.setPosition(ix, iy);
		leftArrowGray = ImageManager.getSprite("arrow-off");
		ix = cardX - rightArrowGray.getRegionWidth() - 20.0f;
		leftArrowGray.setPosition(ix, iy);
		leftArrowGray.setFlip(true, false);
	}
	
	private void createColliders()
	{
		exitCollider 		= new Rectangle(EXIT_X - OFFSET, EXIT_Y_2 - OFFSET, bounds.width + 2.0f * OFFSET, bounds.height + 2.0f * OFFSET);
		settingsCollider 	= new Rectangle(SETTINGS_X - OFFSET, SETTINGS_Y_2 - OFFSET, bounds.width + 2.0f * OFFSET, bounds.height + 2.0f * OFFSET);
		resumeCollider 		= new Rectangle(RESUME_X - 5.0f, (AuraluxTest.gameHeight - RESUME_Y) - 5.0f, bounds.width + 2 * 5.0f, bounds.height + 2 * 5.0f);
		cardCollider 		= new Rectangle(ix, AuraluxTest.gameHeight - iy - 160.0f, 160.0f, 160.0f);
		filesCollider 		= new Rectangle(FILES_X - 5.0f, AuraluxTest.gameHeight - FILES_Y - 5.0f, bounds.width + 2 * 5.0f, bounds.height + 2 * 5.0f);
	}
	
	private void createOther()
	{
		offsetCard = 0.0f;
		deltaIndex = 0;
		leftActive = false;
		rightActive = true;
		toChange = toChange2 = false;
	}
	
	public void render()
	{
		super.render();
		updateTouch();
		updateLevel();
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
			drawMain();
			drawArrows();
			drawLevel();
			drawFiles();
			drawExit();
			drawSettings();
			if (toResume)drawResume();
		batch.end();
	}
	
	private void updateTouch()
	{
		if (toChange)
		{
			buttonPressSfx.play();
			if(AuraluxTest.overScened)AuraluxTest.resumeScene();
			AuraluxTest.changeScene(new Scene_Game(levelLoader.loadLevel(cardIndex), currentLevel, multiplayer, host));
			toChange = false;
		}
		
		if (toChange2)
		{
			if(multiplayer)return;
			buttonPressSfx.play();
			AuraluxTest.overScene(new Scene_Settings());
			toChange2 = false;
		}
		
		if (exitChange)
		{
			Gdx.app.exit();
			exitChange = false;
		}
		
		if (resumeChanged)
		{
			buttonPressSfx.play();
			AuraluxTest.resumeScene();
			resumeChanged = false;
		}
		
		if (deltaIndex != 0)
		{
			buttonPressSfx.play();
			cardIndex += deltaIndex;
			deltaIndex = 0;
			leftActive = !(cardIndex < 1);
			rightActive = !(cardIndex > levelsInfo.nLevels - 2);
		}
		
		if (filesChanged)
		{
			buttonPressSfx.play();
			AuraluxTest.overScene(new Scene_Description(currentLevel));
			filesChanged = false;
		}
	}
	
	private void updateLevel(){ currentLevel = levelsInfo.levels.get(cardIndex); }
	
	private void drawMain()
	{
		ix = MathUtils.floor(ImageManager.logo.getWidth() * SCALE_FACTOR);
		iy = MathUtils.floor(ImageManager.logo.getHeight() * SCALE_FACTOR);
		batch.draw(ImageManager.background, 0, 0, AuraluxTest.gameWidth, AuraluxTest.gameHeight);
		batch.draw(ImageManager.logo, MathUtils.floor((AuraluxTest.gameWidth - ix) / 2.0f), MathUtils.floor(AuraluxTest.gameHeight - iy), ix, iy);
		batch.draw(card, cardX, cardY - offsetCard);
	}
	
	private void drawArrows()
	{
		if(leftActive)leftArrow.draw(batch);
		else leftArrowGray.draw(batch);
		if(rightActive) rightArrow.draw(batch);
		else rightArrowGray.draw(batch);
	}
	
	private void drawLevel()
	{
		drawLevelImage();
		drawLevelName();
	}
	
	private void drawLevelImage()
	{
		lOffset = (card.getWidth() - LEVEL_CARD) / 2.0f;
		ix = MathUtils.floor(cardX + lOffset);
		iy = MathUtils.floor(cardY + card.getHeight() - LEVEL_CARD - lOffset);
		
		batch.draw(levelImages, ix, iy - offsetCard, LEVEL_CARD, LEVEL_CARD, (cardIndex % LEVEL_WIDTH) * LEVEL_CARD, (cardIndex / LEVEL_WIDTH) * LEVEL_CARD, LEVEL_CARD, LEVEL_CARD, false, false);
	}
	
	private void drawLevelName()
	{
		String str = currentLevel.name;
		
		font.setScale(2.0f, 1.6f);
		font.setColor(Color.WHITE);
		
		bounds = font.getBounds(str);
		
		ix = MathUtils.floor(cardX + (card.getWidth() - bounds.width) / 2.0f);
		iy = MathUtils.floor(iy - 20.0f);
		
		font.draw(batch, str, ix, iy - offsetCard);
	}
	
	private void drawFiles()
	{
		font.setScale(1.2f, 1.2f);
		font.draw(batch, FILES_TEXT, FILES_X, FILES_Y - offsetCard);
	}
	
	private void drawExit()
	{
		font.setScale(EXIT_SCALE, EXIT_SCALE);
		font.draw(batch, EXIT_TEXT, EXIT_X, EXIT_Y - exitOffset);
	}
	
	private void drawResume()
	{
		font.setScale(RESUME_SCALE, RESUME_SCALE);
		font.draw(batch, RESUME_TEXT, RESUME_X, RESUME_Y - resumeOffset);
	}
	
	private void drawSettings()
	{
		if(multiplayer)return;
		font.setScale(SETTINGS_SCALE, SETTINGS_SCALE);
		font.draw(batch, SETTINGS_TEXT, SETTINGS_X, SETTINGS_Y - settingsOffset);
	}
	
	public void touchDown(int screenX, int screenY, int button)
	{
		super.touchDown(screenX, screenY, button);
		if (exitCollider.contains(coords.x, coords.y)) exitOffset = 2.0f;
		if (cardCollider.contains(coords.x, coords.y)) offsetCard = 2.0f;
		if (settingsCollider.contains(coords.x, coords.y)) settingsOffset = 2.0f;
		if (toResume && resumeCollider.contains(coords.x, coords.y)) resumeOffset = 2.0f;
		if (filesCollider.contains(coords.x, coords.y)) offsetCard = 2.0f;
	}
	
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		
		exitOffset = settingsOffset = offsetCard = resumeOffset = 0.0f;
		
		if (exitCollider.contains(coords.x, coords.y)) exitChange = true;
		if (cardCollider.contains(coords.x, coords.y)) toChange = true;
		if (settingsCollider.contains(coords.x, coords.y)) toChange2 = true;
		if (toResume && resumeCollider.contains(coords.x, coords.y)) resumeChanged = true;
		if (filesCollider.contains(coords.x, coords.y)) filesChanged = true;
		if (leftActive && leftArrow.getBoundingRectangle().contains(coords.x, coords.y)) deltaIndex = -1;
		else if (rightActive && rightArrow.getBoundingRectangle().contains(coords.x, coords.y)) deltaIndex = 1;
	}
	
}
