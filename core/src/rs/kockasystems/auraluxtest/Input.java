package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.input.GestureDetector;

/**
 * A class for handling the input for the whole game.
 */
public class Input extends GestureDetector
{
	
	public Input() { super(30, 0.4f, 1.1f, 0.15f, new GDetector()); }
	
	// Unused methods
	@Override
	public boolean keyDown(final int keycode) {  return false; }
	@Override
	public boolean keyUp(final int keycode) { return false; }
	@Override
	public boolean keyTyped(final char character) { return false; }
	@Override
	public boolean scrolled(final int amount) { return false; }
	@Override
	public boolean mouseMoved(final int screenX, final int screenY) { return false; }
	
	/**
	 * The default method for handling the mouse click or touch event.
	 */
	@Override
	public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button)
	{
		super.touchDown(screenX, screenY, pointer, button);
		AuraluxTest.scene.touchDown(screenX, screenY, button);
		return true;
	}
	
	
	
	/**
	 * The default method for handling the mouse un-click or un-touch event.
	 * @author Akimil
	 */
	@Override
	public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button)
	{
		super.touchUp(screenX, screenY, pointer, button);
		AuraluxTest.scene.touchUp(screenX, screenY, button);
		return true;
	}
	
	/**
	 * The default method for handling the mouse drag or drag event.
	 */
	
	@Override
	public boolean touchDragged(final int screenX, final int screenY, final int pointer)
	{
		super.touchDragged(screenX, screenY, pointer);
		AuraluxTest.scene.touchDragged(screenX, screenY);
		return true;
	}
}