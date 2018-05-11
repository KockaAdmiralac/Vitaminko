package rs.kockasystems.auraluxtest;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class GDetector implements GestureDetector.GestureListener
{
	// Unused methods
	@Override
	public boolean fling(final float arg0, final float arg1, final int arg2) { return false; }
	@Override
	public boolean panStop(float arg0, float arg1, int arg2, int arg3) { return false; }
	@Override
	public boolean pinch(Vector2 arg0, Vector2 arg1, Vector2 arg2, Vector2 arg3) { return false; }
	@Override
	public boolean tap(float x, float y, int count, int button) { return false; }
	@Override
	public boolean touchDown(float arg0, float arg1, int arg2, int arg3) { return false; }
	
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY)
	{
		AuraluxTest.scene.pan(x, y, deltaX, deltaY);
		return true;
	}
	
	@Override
	public boolean zoom(float initialDist, float dist)
	{
		AuraluxTest.scene.zoom(initialDist, dist);
		return true;
	}
	
	@Override
	public boolean longPress(float arg0, float arg1)
	{
		AuraluxTest.scene.longPress(arg0, arg1);
		return true;
	}
}