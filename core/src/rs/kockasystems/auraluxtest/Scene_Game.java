package rs.kockasystems.auraluxtest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Scene_Game extends Scene
{
	// Class variables
	private final Random rnd = new Random();					// Random generator
	private Vector2 touched, dragged = new Vector2();			// Circle coordinates
	private ShapeRenderer renderer;								// Renderer for circle
	private Circle selectedCircle;								// The selected circle
	private boolean selecting, forcedRadius;					// Is circle currently being selected, and is radius too small?
	private ArrayList<Soldier> soldiers;	 					// The list of soldiers
	private ArrayList<Barrack> barracks;						// The list of barracks
	private ArrayList<Integer> xlist;							// Will be important for speeding up the collision detection
	private List<Effect> effects;								// List of currently playing effects.
	private ArrayList<MParticle> mparticles;					// List of MParticles
	private TextureRegion mParticleTexture, lightTexture;
	private TextureRegion[] powerUpTextures = new TextureRegion[5];
	private final Color[] lightColors = new Color[5];			// Light colors
	private float[] target_potential;							// Moved this here
	private float Z, W;
	private int mode = MODE_SELECT;
	private int nBarracks;
	private PowerUp currentPowerUp;
	private PowerUp[] teamPowerUp = new PowerUp[5];
	private LevelInfo levelInfo;
	private Vector2 tempTouch = new Vector2();
	private byte gameOver = -1;
	private byte yourTeam, enemyTeam;
	private Texture gameOverGood, gameOverBad;
	private Sprite gameOverSprite;
	private float gameOverAlpha;
	private boolean multiplayer, host;
	private String current;
	
	private TextureRegion pauseBtnTex;
	private Rectangle pauseBtnCollider;
	private boolean pauseChanged;
	private boolean wasPausePressed;
	
	private TextureRegion modeSelectBtnTex, modeMoveBtnTex;
	private Rectangle modeBtnCollider;
	private boolean modeChanged;
	private boolean wasModePressed;
	
	private TextureRegion filesBtnTex; 
	private Rectangle filesBtnCollider;
	private boolean filesChanged;
	private boolean wasFilesPressed;
	
	private static final int MODE_SELECT = 0;
	private static final int MODE_MOVE_AND_ZOOM = 1;
	
	// Texture arrays - allow accessing textures with indexes, not with if-s. Implemented by MatejaS
	private final TextureRegion[] barrackTextures = new TextureRegion[5];
	private final TextureRegion[] soldierTextures = new TextureRegion[5];
		
	// Constants
	private static final byte SUN_STEP 		   			= 60;			// The number of frames for sun update (max = 255, 60 = 1 second)
	private static final float COLLISION_DISTANCE 		= 5;			// If two soldiers are closer than this, they collide and disapear
	private static final float SUN_ATTACK				= 5;			// When soldier is near to an enemy sun, he reduces its health
	private static final float SUN_DEFEND		   		= 50;			// The distance at which the sun detect that it's attacked
	private static final int SUN_MAGNET		   			= 30;			// When soldier has went near the sun, he auto repairs/attacks the sun
	private static final float SOLDIER_SPEED 	  		= 1f;			// The speed of soldiers, ie. the number of pixels that soldier passes in 1 second
	private static final float ACCELERATION	   			= 0.05f;		// Maximal acceleration that soldier can attain
	private static final float FRICTION_DECELERATION	= 0.01f;		// The decceleration due to the friction
	private static final int MAX_HEALTH 		   		= 100;			// The health of the normal (unattacked) sun
	private static final int ATTACK_DAMAGE		   		= 1;			// The damage made to an enemy sun.
	private static final int HEAL_REGENERATION    		= 1;			// The health regeneration when sun is healed.
	private static final float NULL_VALUE 				= -1000000.0f; 	// The NULL_VALUE for touched vector
	private static final int POWER_UP_DURATION 			= 1000;
	private static final int DRAGGED_BUGFIX 			= 20;
	private static final float GAME_OVER_STEP			= 0.005f;
	
	// Light-related constants
	private static final float LIGHT_STEP 				= 2.0f;
	private static final float LIGHT_MAX 				= 16.0f;
	private static final float LIGHT_MAX_EXPLOSION 		= 50.0f;
	
	// Team constants
	private static final byte TEAM_NEUTRAL 				= 0;
	private static final byte TEAM_BLUE 				= 1;
	private static final byte TEAM_RED 					= 2;
	private static final byte TEAM_GREEN				= 3;
	private static final byte TEAM_YELLOW 				= 4;
	
	private Color PAUSE_COLOR = Color.valueOf("D3D3D3");
	private Color PAUSE_COLOR_2 = Color.valueOf("EFEFEF");
	
	private byte getTeam(String teamStr)
	{
		switch (teamStr)
		{
			case "neutral": return TEAM_NEUTRAL;
			case "blue": return TEAM_BLUE;
			case "red": return TEAM_RED;
			case "green": return TEAM_GREEN;
			case "yellow": return TEAM_YELLOW;
			default: throw new RuntimeException("Error parsing JSON file: invalid team name: " + teamStr);
		}
	}
	
	public Scene_Game(LevelData levelData, LevelInfo levelInfo, final boolean multiplayer, final boolean host)
	{
		int _index = 0;
		barracks = new ArrayList<Barrack>();
		for (BarrackData bData : levelData.barracks)
		{
			barracks.add(new Barrack(bData.x, bData.y, getTeam(bData.team), _index));
			_index++;
		}
		nBarracks = levelData.barracks.size;
		target_potential = new float [nBarracks];
		this.levelInfo = levelInfo;
		this.multiplayer = multiplayer;
		this.host = host;
	}
	
	public void start()
	{
		super.start();
		createTextures();
		createVariables();
		createPowerUp();
		createCamera();
		createLightColors();
		createResolution();
		createButtons();
		determineTeam();
	}
	
	private void createTextures()
	{
		soldierTextures[TEAM_BLUE]		= ImageManager.get("FriendSoldier");
		barrackTextures[TEAM_BLUE] 		= ImageManager.get("RedBloodCell");
		soldierTextures[TEAM_RED] 		= ImageManager.get("EnemySoldier");
		barrackTextures[TEAM_RED] 		= ImageManager.get("EnemyBarrack");
		soldierTextures[TEAM_GREEN]		= ImageManager.get("Team3Soldier");
		barrackTextures[TEAM_GREEN]		= ImageManager.get("GreenBarrack");
		soldierTextures[TEAM_YELLOW]	= ImageManager.get("Team4Soldier");
		barrackTextures[TEAM_YELLOW]	= ImageManager.get("YellowBarrack");
		barrackTextures[TEAM_NEUTRAL]	= ImageManager.get("neutral_test");
		mParticleTexture 				= ImageManager.get("MParticle");
		lightTexture 					= ImageManager.get("light_effect");
		
		gameOverGood 					= new Texture("res/win.png");
		gameOverBad 					= new Texture("res/lose.png");
		
		for(int i = 0; i < 5; ++i){powerUpTextures[i] = ImageManager.get("PowerUp" + i); System.out.println(powerUpTextures[i]);}
	}
	
	private void createVariables()
	{
		selectedCircle					= new Circle();
		soldiers 						= new ArrayList<Soldier>();
		xlist 							= new ArrayList<Integer>();
		font							= new BitmapFont();
		effects							= new ArrayList<Effect>();
		mparticles						= new ArrayList<MParticle>();
		renderer 						= new ShapeRenderer();
		touched 						= new Vector2(NULL_VALUE, NULL_VALUE);
	}
	
	private void createPowerUp()
	{
		currentPowerUp = new PowerUp();
		for(int i = 0; i < 5; ++i)teamPowerUp[i] = new PowerUp();
	}
	
	private void createCamera()
	{
		camera = new OrthographicCamera();
		camera.translate(-Z, -W, 0.0f);
		camera.update();
		camera.setToOrtho(false, AuraluxTest.gameWidth, AuraluxTest.gameHeight);
	}
	
	private void createLightColors()
	{
		lightColors[TEAM_NEUTRAL] = Color.valueOf("FFE566");
		lightColors[TEAM_BLUE] = Color.valueOf("FF3333");
		lightColors[TEAM_RED] = Color.valueOf("66FF66");
		lightColors[TEAM_GREEN] = Color.valueOf("FF3333");
		lightColors[TEAM_YELLOW] = Color.valueOf("66FF66");
	}
	
	private void createResolution()
	{
		Z = MathUtils.floor((AuraluxTest.gameWidth - (float) AuraluxTest.WIDTH) / 2.0f);
		W = MathUtils.floor((AuraluxTest.gameHeight - (float) AuraluxTest.HEIGHT) / 2.0f);
	}
	
	private void createButtons()
	{
		createPauseButton();
		createModeButton();
		createFileButton();
	}
	
	private void createPauseButton()
	{
		pauseBtnTex = ImageManager.get("pause");
		pauseBtnCollider = new Rectangle(10 - 5, 10 - 5, 25 + 2 * 5, 25 + 2 * 5);
		pauseChanged = false;
		wasPausePressed = false;
	}
	
	private void createModeButton()
	{
		modeSelectBtnTex = ImageManager.get("mode-select");
		modeMoveBtnTex = ImageManager.get("mode-move");
		modeBtnCollider = new Rectangle(MathUtils.floor(AuraluxTest.gameWidth - 10 - 25), 10, 25, 25);
		modeChanged = false;
		wasModePressed = false;
	}
	
	private void createFileButton()
	{
		filesBtnTex = ImageManager.get("info");
		filesChanged = false;
		wasFilesPressed = false;
		filesBtnCollider = new Rectangle(40, 5, 35, 35);
	}
	
	private void determineTeam()
	{
		yourTeam = !multiplayer || host ? TEAM_BLUE : TEAM_RED;
		enemyTeam = !multiplayer || host ? TEAM_RED : TEAM_BLUE;
	}
	
	public void dispose()
	{
		super.dispose();
		renderer.dispose();
		for(Effect effect : effects)effect.stop();
	}
	
	public void render()
	{
		super.render();
		if(gameOver != -1)renderGameOver();
		else updateTouch();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
			
			// Draw barracks' sprites
			for(int i=0; i<barracks.size(); i++)
			{
				barracks.get(i).draw(batch);
				barracks.get(i).update();
			}
			
			// Draw soldier sprites
			for(int i=0; i<soldiers.size(); i++)
			{
				soldiers.get(i).draw(batch);
				soldiers.get(i).update();
			}
			
			font.draw(batch, String.valueOf(currentPowerUp.health), 50, 50);
			font.draw(batch, String.valueOf(currentPowerUp.time), 50, 30);
			
			// Update Power-ups
			teamPowerUp[0].update();
			teamPowerUp[1].update();
			teamPowerUp[2].update();
			teamPowerUp[3].update();
			
			currentPowerUp.draw(batch);
			
			if(currentPowerUp.type >= 0)currentPowerUp.update();
			else
			{
				if(rnd.nextFloat()<0.005)
				{	currentPowerUp.team = (byte)0;
					currentPowerUp.health = 0;
					currentPowerUp.type = 0;
					currentPowerUp.time = 15000;
					currentPowerUp.x = (AuraluxTest.gameWidth - 20) * rnd.nextFloat() + 10;
					currentPowerUp.y = (AuraluxTest.gameHeight - 20) * rnd.nextFloat() + 10;
				}
			}
			
			// Draw particles
			if((Settings.areParticlesEnabled() || Settings.getSoundVolume() > 0) && effects.size() > 0)for(int i=0; i<effects.size(); ++i)
			{
				if(effects.get(i).started())effects.get(i).update(batch, Gdx.graphics.getDeltaTime());
				else if(effects.get(i).stopped())
				{
					effects.get(i).stop();
					effects.remove(i);
				}
			}
		batch.end();
		
		if(gameOver != -1)return;
		
		// Rendering the select circle
		if(touched.x != NULL_VALUE && touched.y != NULL_VALUE)
		{
			renderer.setProjectionMatrix(camera.combined);
			renderer.begin(ShapeType.Line);
			selectedCircle.x = (touched.x + dragged.x) / 2;
			selectedCircle.y = (touched.y + dragged.y) / 2;
			selectedCircle.radius = forcedRadius ? 100f : pitagora(Math.abs(touched.x - dragged.x), Math.abs(touched.y - dragged.y)) / 2;
			renderer.circle(selectedCircle.x, AuraluxTest.gameHeight - selectedCircle.y, selectedCircle.radius);
			renderer.end();
		}
		
		// Start rendering MParticles
		batch.begin();
		
		for (int i = mparticles.size() - 1; i >= 0; i--)
		{
			MParticle mp = mparticles.get(i);
			if (mp.isDead())
			{
				mparticles.remove(i);
				continue;
			}				
			mp.time--;	
			mp.draw(batch);
		}
						
		// End rendering MParticles
		batch.end();
					
		// draw gui
		batch.setProjectionMatrix(guiCamera.combined);
		batch.begin();
			batch.setColor(wasPausePressed ? PAUSE_COLOR_2 : PAUSE_COLOR);
			batch.draw(pauseBtnTex, 10, MathUtils.floor(AuraluxTest.gameHeight - 10 - pauseBtnTex.getRegionHeight()));
			batch.setColor(wasModePressed ? PAUSE_COLOR_2 : PAUSE_COLOR);			
			batch.draw(mode == MODE_SELECT ? modeMoveBtnTex : modeSelectBtnTex,
				MathUtils.floor(AuraluxTest.gameWidth - 10 - modeSelectBtnTex.getRegionWidth()),
				MathUtils.floor(AuraluxTest.gameHeight - 10 - modeSelectBtnTex.getRegionHeight()));
			batch.setColor(wasFilesPressed ? PAUSE_COLOR_2 : PAUSE_COLOR);
			batch.draw(filesBtnTex, 10 + 25 + 10, MathUtils.floor(AuraluxTest.gameHeight - 10 - modeSelectBtnTex.getRegionHeight()));
			batch.setColor(Color.WHITE);
			
		batch.end();
		
		updateBluetooth();
	}
	
	public void renderGameOver()
	{
		gameOverAlpha += GAME_OVER_STEP;
		if(1f - gameOverAlpha < GAME_OVER_STEP)AuraluxTest.changeScene(new Scene_Menu());
		gameOverSprite.setAlpha(gameOverAlpha);
		batch.begin();
		gameOverSprite.draw(batch);	
		batch.end();
	}
	
	public void updateTouch()
	{
		if (pauseChanged)
		{
			buttonPressSfx.play();
			AuraluxTest.overScene(new Scene_Level(multiplayer, host));
			Scene_Level.onShouldResume();
			pauseChanged = false;
		}
		
		if (modeChanged)
		{
			buttonPressSfx.play();
			resetTouchData();
			mode = (mode + 1) % 2;
			modeChanged = false;
		}
		
		if (filesChanged)
		{
			buttonPressSfx.play();
			AuraluxTest.overScene(new Scene_Description(levelInfo));
			filesChanged = false;
		}
	}
	
	private void updateBluetooth()
	{
		if(multiplayer)
		{
			// Read from Bluetooth connection
			current = AuraluxTest.btc.read();
			if(current != "" && current != null)
			{
				Data d = new Data(current.split("\\|"));
				moveSoldiersInCircle(d.x, d.y, d.radius, d.x1, d.y1, enemyTeam);
			}
		}
	}
	
	public void touchDown(int screenX, int screenY, int button)
	{
		super.touchDown(screenX, screenY, button);
		boolean skip = false;
		
		float lastX = coords.x, lastY = coords.y;
		
		// Handle GUI input here
		if (pauseBtnCollider.contains(coords.x, coords.y))
		{
			wasPausePressed = !wasPausePressed;
			skip = !skip;
		}
		
		if (modeBtnCollider.contains(coords.x, coords.y))
		{
			wasModePressed = !wasModePressed;
			skip = !skip;
		}
		
		if (filesBtnCollider.contains(coords.x, coords.y))
		{
			wasFilesPressed = true;
			skip = true;
		}
		
		coords.x = lastX;
		coords.y = lastY;
		
		if (!skip && mode == MODE_SELECT)if(button == Buttons.LEFT && !selecting)
		{
			float offsetX =  camera.position.x - (AuraluxTest.gameWidth / 2), offsetY = (AuraluxTest.gameHeight / 2) - camera.position.y;
			touched.set(offsetX + coords.x, offsetY + coords.y); /* !!! */
			dragged.x = offsetX + coords.x;
			dragged.y = offsetY + coords.y;
		}
	}
	
	public void touchUp(int screenX, int screenY, int button)
	{
		super.touchUp(screenX, screenY, button);
		boolean skip = false;
		
		float lastX = coords.x, lastY = coords.y;
		
		if (wasPausePressed)
		{
			if (pauseBtnCollider.contains(coords.x, coords.y)) pauseChanged = true;
			resetTouchData();
			skip = true;	
		}
		
		wasPausePressed = false;
		
		if (wasModePressed)
		{
			if (modeBtnCollider.contains(coords.x, coords.y))modeChanged = true;
			resetTouchData();
			skip = true;
		}
		
		if (wasFilesPressed)
		{
			if (filesBtnCollider.contains(coords.x, coords.y)) filesChanged = true;
			resetTouchData();
			skip = true;
		}
		
		wasModePressed = false;
		
		coords.x = lastX;
		coords.y = lastY;
		
		if (!skip && mode == MODE_SELECT)
		{	
			if(selecting)
			{
				moveSoldiersInCircle(selectedCircle.x, selectedCircle.y, selectedCircle.radius, coords.x, coords.y, yourTeam);
				if(multiplayer)AuraluxTest.btc.write(new Data((selectedCircle.x + "|" +selectedCircle.y + "|" + selectedCircle.radius + "|" + coords.x + "|" + coords.y).split("\\|")));
				touched.set(NULL_VALUE, NULL_VALUE);
				forcedRadius = false;
			}
			else if(selectedCircle.radius < 1f) forcedRadius = true;
			if(button == Buttons.LEFT) selecting = !selecting;
		}
		
	}
	
	public void touchDragged(int screenX, int screenY)
	{
		super.touchDragged(screenX, screenY);
		if (!wasPausePressed && !wasModePressed && mode == MODE_SELECT)if(touched.x != NULL_VALUE && touched.y != NULL_VALUE)
		{
			float offsetX = camera.position.x - (AuraluxTest.gameWidth / 2), offsetY = (AuraluxTest.gameHeight / 2) - camera.position.y;
			tempTouch.x = coords.x;
			tempTouch.y = coords.y;
			if (tempTouch.dst2(touched) > DRAGGED_BUGFIX * DRAGGED_BUGFIX)
			{
				dragged.x = offsetX + coords.x;
				dragged.y = offsetY + coords.y;
				if(selecting)
				{
					selecting = false;
					forcedRadius = false;
					touched.x = offsetX + coords.x;
					touched.y = offsetY + coords.y;
				}
			}
		}
	}
	
	public void longPress(float screenX, float screenY)
	{
		super.longPress(screenX, screenY);
		camera.zoom += 0.1f;
		camera.update();
	}
	
	public void pan(float x, float y, float deltaX, float deltaY)
	{
		super.pan(x, y, deltaX, deltaY);
		if (mode == MODE_MOVE_AND_ZOOM && !wasPausePressed && !wasModePressed)
		{
			camera.translate(-deltaX, deltaY, 0.0f);
			camera.update();
		}
	}
	
	public void zoom(float initialDist, float dist)
	{
		super.zoom(initialDist, dist);
		if (mode == MODE_MOVE_AND_ZOOM && !wasPausePressed && !wasModePressed)
		{
			final float MAX_ZOOM = 0.3f;
			final float MIN_ZOOM = 2.0f;
			
			camera.zoom *= initialDist / dist;
			camera.zoom = MathUtils.clamp(camera.zoom, MAX_ZOOM, MIN_ZOOM);
			camera.update();
		}
	}
	
	private void checkGameOver()
	{
		byte initialTeam = barracks.get(0).team;
		for(Barrack a : barracks)if(initialTeam != a.team && a.team != TEAM_NEUTRAL)return;
		gameOver = initialTeam;
		gameOverSprite = new Sprite();
		gameOverSprite.setRegion((gameOver == yourTeam) ? gameOverGood : gameOverBad);
		gameOverSprite.setBounds(AuraluxTest.gameWidth - (gameOverSprite.getRegionWidth() * 3 / 2), AuraluxTest.gameHeight - (gameOverSprite.getRegionHeight() * 3 / 2), gameOverSprite.getRegionWidth(), gameOverSprite.getRegionHeight());
		gameOverSprite.setAlpha(0);
		gameOverAlpha = 0;
	}
	
	/**
	 * The data class for barracks (suns).
	 * index	 - The index of the barrack in the barracks list
	 * x         - The X position of the barrack
	 * y         - The Y position of the barrack
	 * level     - The level of the barrack
	 * team	     - The team of the barrack//Team 0-neutral, Team 1, Team 2...
	 * functional- Determines if the sun is producing soldiers or not
	 * gauge     - The health gauge
	 * gaugeTeam - What team is posessing the health gauge
	 * step      - Current step, by default when it reaches 60 new soldier is made.
	 * attacked  - True if the sun is currently attacked by enemy forces
	 */
	
	private class Barrack
	{
		
		// Variable definition
		private int index;
		public float x, y;
		private byte level;
		protected byte team;
		private int step;
		protected int health = MAX_HEALTH;
		private boolean functional = false;
		public int attacked = 0, attack = -1;
		public ArrayList<Integer> attacked_by = new ArrayList<Integer>();
		
		private float lightRadiusPlus;
		private boolean inc;
		private float lightMax;
		
		private Color clr = null;
		
		/**
		 * The class constructor.
		 * @param x
		 * @param y
		 * @param team
		 */
		public Barrack(){ }
		
		public Barrack(final float x, final float y, final byte team, final int index)
		{
			this.x = x;
			this.y = y;
			this.team = team;
			this.index = index;
			if(team == TEAM_NEUTRAL)
			{
				functional = false;
				health = 0;
			}
			else functional = true;
			
			lightRadiusPlus = 0.0f;
			inc = false;
			lightMax = -1.0f;
		}
		
		/**
		 * The method called on frame update.
		 */
		public void update()
		{
			if(health == MAX_HEALTH) functional = true;
			++step;
			if(step == SUN_STEP)
			{
				step = 0;
				if(functional)
				{
					// I use variable step here as some other variable : don't be confused, this should normally work as long as step is int
					while(step < soldiers.size() && soldiers.get(step).exists) ++step;
					
					final float theta = (float) rnd.nextInt(360);
					
					final float radius = barrackTextures[team].getRegionWidth() / 2.0f + 1 + (float) rnd.nextInt(4);
					
					final float cos = MathUtils.cosDeg(theta);
					final float sin = MathUtils.sinDeg(theta);
					
					final float posX = x + radius * cos;
					final float posY = y + radius * sin;
					
					if(step == soldiers.size()) soldiers.add(new Soldier(posX, posY, rnd.nextFloat(), rnd.nextFloat(), team, index));
					else soldiers.get(step).init(posX, posY, rnd.nextFloat(), rnd.nextFloat(), team, index);
		
					inc = true;
					if (lightMax == -1.0f) lightMax = LIGHT_MAX;
				}
				step = 0;
			}
			
			if (inc)
			{
				lightRadiusPlus += LIGHT_STEP;
				if (lightRadiusPlus >= lightMax)
				{
					lightRadiusPlus = lightMax;
					inc = false;
					lightMax = -1.0f;
				}
			}
			else if (lightRadiusPlus > 0.0f)
			{
				lightRadiusPlus -= LIGHT_STEP;
				if (lightRadiusPlus <= 0.0f)
				{
					lightRadiusPlus = 0.0f;	
					clr = null;
				}
			}
			
			// Important for independent particle AI
			attacked = 0;
			attacked_by.clear();
			for(int i = 0; i < soldiers.size(); ++i) if(soldiers.get(i).exists && soldiers.get(i).team != team && pitagora(soldiers.get(i).x - x, soldiers.get(i).y - y) < SUN_DEFEND)
			{
				attacked++;
				attacked_by.add(i);
			}
			
			if(team <= 1 || !functional)return;	// Don't govern player's activities
			
			// Sun's AI
			target_potential = new float[nBarracks];
			for(int i=0; i < barracks.size(); ++i)
			{
				Barrack barrack = barracks.get(i);
				if(barrack.team == team && barrack.functional)continue;
				target_potential[i] = 1 / SUN_DEFEND;
				byte t = barrack.team;
				if(t != team) for(Soldier soldier : soldiers) if(soldier.exists && soldier.team == t)
				{
					float temp = pitagora(barrack.x - soldier.x, barrack.y - soldier.y);
					target_potential[i] += (temp < SUN_DEFEND) ? 1 / SUN_DEFEND : soldier.health / temp;		
				}
				
				target_potential[i] *= (barrack.team == 0 || !barrack.functional) ? 100 : (t != team) ? (barrack.health + 1) : (100 - barrack.health);
				
				// Finalize
				target_potential[i] *= pitagora(barrack.x - x, barrack.y - y);
			}
			// Finalize			
			float min_potential = 1000000000;
			int min_index = -1;
			for(int i = 0; i < barracks.size(); ++i)
			{
				if(barracks.get(i).team == team && barracks.get(i).functional)continue;
				if(target_potential[i] < min_potential)
				{
					min_potential = target_potential[i];
					min_index = i;
				}
			}
			
			if(min_index == -1)
			{
				attack =- 1;
				return;
			}
			
			attack = min_index;
			if(attack >= 0)
			{
				int s = 0;
				for(int i = 0; i < soldiers.size(); ++i) if(soldiers.get(i).exists && pitagora(x - soldiers.get(i).x, y - soldiers.get(i).y) < SUN_DEFEND && soldiers.get(i).team == this.team) s += soldiers.get(i).health;
				if(attack >= 0 && !barracks.get(attack).functional) if(s<0.5*(100-(barracks.get(attack).team==this.team?1:-1)*barracks.get(attack).health))	attack =- 1;
				if(attack >= 0 && barracks.get(attack).functional)
				{
					int t = 0;
					for(int i = 0; i<soldiers.size(); ++i)if(soldiers.get(i).team == barracks.get(attack).team && pitagora(x - soldiers.get(i).x, y - soldiers.get(i).y) < 1.5 * SUN_DEFEND) t += soldiers.get(i).health;
					if(s < 0.1 * (100 + barracks.get(attack).health + t))attack = -1;
				}
			}
			
			if(attack == -1) for(int i=0; i<barracks.size(); ++i) if(barracks.get(i).team!=team)for(int j = 0; j<barracks.size(); ++j) if(barracks.get(j).team == team)
			{
				float x1, x2, x3, y1, y2, y3;
				x1 = x;
				y1 = y;
				x2 = barracks.get(i).x;
				y2 = barracks.get(i).y;
				x3 = barracks.get(j).x;
				y3 = barracks.get(j).y;
				float dist1 = (x1 - x2) * (x1 - x2);
				float dist2 = (x1 - x3) * (x1 - x3);
				float dist3 = (x2 - x3) * (x2 - x3);
				// We check if the triangle has an angle greater than 120
				if(dist1 <= dist2 + dist3 + Math.sqrt(dist3 * dist2))break;
				attack = j;
			}
		}
		
		
		
		/**
		 * The method called when drawing the sun.
		 * @param batch
		 */
		public void draw(final SpriteBatch batch)
		{
			final float lightRadius = lightTexture.getRegionWidth() / 2.0f + lightRadiusPlus;
			
			final float lightX = x - lightRadius;
			final float lightY = y - lightRadius;
			
			batch.setColor(clr != null ? clr : lightColors[team]);
			batch.draw(lightTexture, lightX, lightY, 2 * lightRadius, 2 * lightRadius);
			batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
			
			if((team == 0 ? (functional == false) : (true))) batch.draw(barrackTextures[team], x - (barrackTextures[team].getRegionWidth() / 2), y - (barrackTextures[team].getRegionHeight() / 2));
			
			String healthStr = String.valueOf(health);
			
			font.setScale(1.0f, 1.0f);
			
			final float planetX = x - barrackTextures[team].getRegionWidth() / 2.0f;
			final float planetY = y - barrackTextures[team].getRegionHeight() / 2.0f;
			
			final float textWidth = font.getBounds(healthStr).width;
			final float planetWidth = barrackTextures[team].getRegionWidth();
			
			final float textX = planetX + (planetWidth - textWidth) / 2.0f;
			final float textY = planetY;
			
			font.draw(batch, healthStr, textX, textY);
		}
		
		
		
		/**
		 * The method called when barracks change team.
		 * @param team Team that it'll be changed to
		 * @author Akimil
		 */
		public void changeTeam(final byte team)
		{
			inc = true;
			lightMax = LIGHT_MAX_EXPLOSION;
			if (clr == null) clr = lightColors[this.team];
			effects.add(new Effect("destroy", this.x, this.y));
			this.team = team;
			this.health = 0;
			checkGameOver();
		}
		
		
		
		/**
		 * The method called when an enemy soldier attacked your barrack.
		 */
		public void attack()
		{
			health -= ATTACK_DAMAGE;
		}
		
		
		
		/**
		 * The method called when your soldier healed your barrack.
		 */
		public void heal()
		{
			health += HEAL_REGENERATION;
		}
		
		
	}
	
	/**
	 * Class used for displaying... something?
	 * 
	 * That "something" are the particles that are emitted when two soldiers or
	 * a soldier and a Sun collide.
	 *
	 */
	
	public class MParticle
	{
		private float x, y, radius;
		private int lifeSpan;
		
		public int time;
		
		public MParticle(final float x, final float y, final float radius, final int lifeSpan)
		{
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.lifeSpan = lifeSpan;
			time = lifeSpan;
		}
		
		public void draw(SpriteBatch batch) {
			// texture size is 2*radius
			
			final float posX = x - radius;
			final float posY = y - radius;
			
			batch.setColor(1.0f, 1.0f, 1.0f, 0.8f);
			batch.draw(mParticleTexture, posX, posY);
			batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		}
		

		public boolean isDead() { return time <= 0; }
		
	}
	/**
	 * A data class for sun soldiers.
	 * x  	- The X position of the soldier.
	 * y  	- The Y position of the soldier.
	 * px 	- The X position of the destination.
	 * py 	- The Y position of the destination.
	 * team - The team in which soldier is.
	 * exists-Determines if the soldier exists at all
	 * original_barrack-The barrack the soldier is supposed to defend;
	 * defending - Show us if the soldier is actually defending some barrack
	 */
	private class Soldier
	{
		
		// Variable definition
		public float x, y, px, py, vx, vy, ax, ay;
		private byte team;
		private boolean exists = true;
		public boolean free = true;
		public int original_barrack = 0, health;
		private int following;
		
		/**
		 * The class constructor.
		 * @param x
		 * @param y
		 * @param vx
		 * @param vy
		 * @author Akimil
		 */
		public Soldier(final float x, final float y, final float vx, final float vy, final byte team, final int origin) { init(x, y, vx, vy, team, origin); }
		
		public void init(final float x, final float y, final float vx, final float vy, final byte team, final int origin)
		{
			this.x 					= x;
			this.px 				= x;
			this.y 					= y;
			this.py 				= y;
			this.vx					= vx;
			this.vy 				= vy;
			this.team 				= team;
			this.original_barrack 	= origin;
			this.following			= -1;
			this.health				= 1;
			this.exists 			= true;
		}
		
		/**
		 * The method called on frame update.
		 * @author Akimil
		 */
		public void update()
		{
			
			if(!exists)return;
			//Merging-very important for optimization, if we have a great army, we merge some of it's soldiers, and consequently  optimize the collision detection
			int s=soldiers.size(), l=0;
			int i = 0;
			int indexfirst=0;
			for(; i<s; ++i)	// Yes, this is n^2, but as we keep n as small as possible by this, this is not more than collision detector
			{
				if(dist(this, soldiers.get(i))<COLLISION_DISTANCE)
				{
					if(soldiers.get(i).team==this.team)
					{
						if(l==0) indexfirst=i;
						++l;
					}
					else
					{
						// There is 50% chance that both of them die, 25% that first wins, 25% that second wins
						while(exists&&soldiers.get(i).exists)
						{	
							float tmp = rnd.nextFloat();
							if(tmp >= 0 && tmp < 0.75) {
								die(false, false);
								if(!exists)return;						// First dies
							}
							else if(tmp >= 0.25 && tmp < 1) soldiers.get(i).die(false, false);  // Second dies
						}
					}
				}
			}
			
			if(l > 15)
			{
				health += soldiers.get(indexfirst).health;
				//font.draw(batch, "Merged", soldiers.get(indexfirst).x, soldiers.get(indexfirst).y);
				soldiers.get(indexfirst).exists=false;
				soldiers.get(indexfirst).health=0;
			}
			i = 0;
			// Determining the original_barrack
			for(int j = 0; j < barracks.size(); ++j)
			{
				if(pitagora(barracks.get(j).x - x, barracks.get(j).y - y) < SUN_DEFEND && barracks.get(j).team == team && barracks.get(j).functional)
				{
					original_barrack = j;
					break;
				}
			}
			
			if(free && original_barrack >= 0)
			{
				// Hear the orders of the original_barrack
				if(barracks.get(original_barrack).attack >= 0)
				{
					i = barracks.get(original_barrack).attack;
					px = barracks.get(i).x;
					py = barracks.get(i).y;
					i = 0;
				}
				
				// Independent particle AI
				if(barracks.get(original_barrack).attacked > 0) following = barracks.get(original_barrack).attacked_by.get(rnd.nextInt(barracks.get(original_barrack).attacked_by.size()));
				
				// With team AI this should be changed
				if(barracks.get(original_barrack).health < MAX_HEALTH)
				{
					px = barracks.get(original_barrack).x;
					py = barracks.get(original_barrack).y;
				}
				
				if(following >= 0)
				{
					if(!soldiers.get(following).exists) following = -1;
					else
					{
						if(pitagora(soldiers.get(following).x - barracks.get(original_barrack).x, soldiers.get(following).y - barracks.get(original_barrack).y) > SUN_DEFEND)
						{
							px = barracks.get(original_barrack).x + 40 * rnd.nextFloat() - 20f;
							py = barracks.get(original_barrack).y + 40 * rnd.nextFloat() - 20f;
						}
						else
						{
							px = soldiers.get(following).x;
							py = soldiers.get(following).y;
						}
					}
				}
			}
			
			if(Math.abs(x - px) < 5f && Math.abs(y - py) < 5f) free = true;
			
			// Motion
			ax = ay = 0;
			if(x != px) ax = (float)((float)ACCELERATION * (px - x) / Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)));
			if(y != py) ay = (float)((float)ACCELERATION * (py - y) / Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)));
			
			// Standard acceleration
			this.vx += this.ax;
			this.vy += this.ay;
			
			// Friction deceleration
			if(vx != 0) this.vx *= (1 - FRICTION_DECELERATION);
			if(vy != 0) this.vy *= (1 - FRICTION_DECELERATION);
			x += vx;
			y += vy;
			
			
			if(this.vx * this.vx + this.vy * this.vy > (teamPowerUp[team - 1].type == 0 ? 2 * SOLDIER_SPEED * SOLDIER_SPEED : SOLDIER_SPEED * SOLDIER_SPEED))
			{
				this.vx-=ax;
				this.vy-=ay;
			}
			
			// Sun attack detector
			for(i = 0; i < barracks.size(); i++)
			{
				// Attack
				if( barracks.get(i).team != this.team && barracks.get(i).team != 0 && pitagora(this.x - barracks.get(i).x, this.y - barracks.get(i).y) < SUN_ATTACK && barracks.get(i).x == this.px && barracks.get(i).y == this.py)
				{
					while(exists)
					{
						barracks.get(i).attack();	 												// Attack the barrack
						if(rnd.nextFloat() > 0.5f) die(true, false);								// 50% chance of surviving
						if(barracks.get(i).health <= 0)
						{
							barracks.get(i).changeTeam((byte)0);									//Change team if lost health
							break;				
						}
					}
					return;
				}
				
				// Recover
				else if((barracks.get(i).team == this.team || barracks.get(i).team == 0) && pitagora(this.x - barracks.get(i).x, this.y - barracks.get(i).y) < SUN_ATTACK && barracks.get(i).health != MAX_HEALTH && barracks.get(i).x == this.px && barracks.get(i).y == this.py)
				{
					if(barracks.get(i).team == 0)
					{
						if(rnd.nextFloat() > 0.5f)die(true, true);	// 50% chance of surviving
						barracks.get(i).heal();
						barracks.get(i).changeTeam((byte)team);
					}
					while(exists && barracks.get(i).health < MAX_HEALTH)
					{
						if(rnd.nextFloat() > 0.5f)die(true, true);	// 50% chance of surviving
						barracks.get(i).heal();			 			// We could implement randomization here also
						if(barracks.get(i).health == MAX_HEALTH)
						{
							barracks.get(i).team = this.team;
							barracks.get(i).functional = true;
							break;
						}
					}
					return;
				}
			}
			
			if(currentPowerUp.health < 100 && pitagora(currentPowerUp.x - x, currentPowerUp.y - y) < SUN_ATTACK)
			{
					if(currentPowerUp.team == 0)
					{
						font.draw(batch, "Case 1", 50, 50);
						currentPowerUp.team=team;
						currentPowerUp.health++;
						while(health > 0)
						{
							if(rnd.nextFloat()>0.5) die(true, false);
							currentPowerUp.health ++;
							if(currentPowerUp.health >= 100) break;
						}
					}
					else
					{
						if(currentPowerUp.team == team)
						{
							while(exists && currentPowerUp.health < MAX_HEALTH)
							{
								if(rnd.nextFloat() > 0.5f)die(true, false);	// 50% chance of surviving
								currentPowerUp.heal();				 		// We could implement randomization here also
								if(currentPowerUp.health == 100)
								{
									currentPowerUp.team=this.team;
									break;
								}
							}
							
						}
						else
						{
							font.draw(batch, "Case 3", 50, 50);
							while(exists)
							{
								currentPowerUp.attack();	 												// Attack the barrack
								if(rnd.nextFloat() > 0.5f)die(true, false);										// 50% chance of surviving
								if(currentPowerUp.health <= 0)
								{
									currentPowerUp.changeTeam((byte)0);									//Change team if lost health
									break;				
								}
							}
						}
					}
					if(currentPowerUp.health>=100)
					{
						teamPowerUp[currentPowerUp.team-1]=currentPowerUp;
						currentPowerUp.health=0;
						currentPowerUp.type=-1;
						
					}
			}
		}	
		
		public void goTo(final float x1, final float y1)
		{
			px = camera.position.x - (AuraluxTest.gameWidth / 2) + x1;
			py = (AuraluxTest.gameHeight / 2) + camera.position.y - y1;
			System.out.println(px + " " + py);
			following = -1;
			
			// If this is near to some sun, order to the soldier to attack/repare the sun
			for(int i = 0; i < barracks.size(); ++i) if(Math.abs(px - barracks.get(i).x) < SUN_MAGNET && Math.abs(py - barracks.get(i).y) < SUN_MAGNET)
			{
				px = barracks.get(i).x;
				py = barracks.get(i).y;
				break;
			}

			free = false;

			// Computing soldier acceleration
			if(px != x) ax = (float)((ACCELERATION * px - x) / Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)));
			if(py != y) ay = (float)((ACCELERATION * py - y) / Math.sqrt((px - x) * (px - x) + (py - y) * (py - y)));

		}
		
		/**
		 * The method called when drawing the soldier.
		 * @param b
		 */	
		public void draw(final SpriteBatch b)
		{
			if(exists) b.draw(soldierTextures[team], x, y);
		}
		
		/**
		 * The method called when soldier dies.
		 */
		public void die(final boolean b, final boolean heal)
		{
			health --;
			if(health > 0)return;
			exists = false;	
			
			final float w = (float) soldierTextures[team].getRegionWidth() / 2.0f;
			final float h = (float) soldierTextures[team].getRegionHeight() / 2.0f;
			
			if (!heal && rnd.nextInt(3) > 0) if (Settings.areParticlesEnabled()) mparticles.add(new MParticle(x + w, y + h, 4.0f + (float) rnd.nextInt(2), 5 + rnd.nextInt(3)));
			
		}
		
		
		
	}
	
	private class PowerUp extends Barrack
	{
		public int type, time;
		public PowerUp()
		{
			this.time = 1000;
			this.type = -1;
			this.health = 0;
		}
		
		public PowerUp(byte type, float x, float y, int time)
		{
			this.type = type;
			this.team = 0;
			this.time = time;
			this.x = x;
			this.y = y;
			this.health = 0;
		}
		@Override 
		public void update()
		{
			time--;
			if(type == -1)
				return;
			if(time < 0)this.type = -1;
			if(health >= 100 && teamPowerUp[team - 1].time - 1 != this.time)
			{
				time = POWER_UP_DURATION;
				teamPowerUp[team - 1] = this;	
			}
		}
		public void draw(SpriteBatch batch)
		{
			if(type >= 0 && health < 100) batch.draw(powerUpTextures[type + 1], x, y);
		}
	}
	
	/**
	 * =======================================================================================================================================================================
	 * 																		HELPER METHODS
	 * =======================================================================================================================================================================
	 */
	
	/**
	 * The Pytagorian theorem method.
	 * @param num1
	 * @param num2
	 * @return float
	 */
	private float pitagora(final float num1, final float num2){ return (float)Math.sqrt((double)(num1*num1 + num2*num2)); }
	
	/**
	 * The distance formula, using pitagora method
	 * @param sola
	 * @param solb
	 * @return float
	 * @author Akimil
	*/
	private float dist(final Soldier sola, final Soldier solb){return (float)pitagora(sola.x - solb.x, sola.y - solb.y);}
	
	private void resetTouchData()
	{
		touched.set(NULL_VALUE, NULL_VALUE);
		dragged.set(0.0f, 0.0f); // !!!
		selectedCircle.set(0.0f, 0.0f, 0.0f); // !!!
		
		selecting = false;
		forcedRadius = false; // !!! <up>
	}
	
	/**
	 * Moves all soldiers in the selected circle to a specified point,
	 * if they are in a specific team.
	 * @param circle
	 * @param team
	 */
	public void moveSoldiersInCircle(final float p1, final float p2, final float radius, final float x1, final float y1, final byte team)
	{
		Circle c = new Circle();
		c.x = p1;
		c.y = p2;
		c.radius = radius;
		for(Soldier s : soldiers)if(s.team == team && s.x <= c.x + c.radius && s.x >= c.x - c.radius && Gdx.graphics.getHeight() - s.y <= c.y + c.radius && Gdx.graphics.getHeight() - s.y >= c.y - c.radius)if(c.contains(s.x, Gdx.graphics.getHeight() - s.y) && c.contains(s.x, Gdx.graphics.getHeight() - s.y))s.goTo(x1, y1);
	}
	
}
