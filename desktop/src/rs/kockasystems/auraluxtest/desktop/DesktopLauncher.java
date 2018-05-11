package rs.kockasystems.auraluxtest.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import rs.kockasystems.auraluxtest.AuraluxTest;

public class DesktopLauncher
{
	public static void main (String[] arg)
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = false;
		config.width = AuraluxTest.SCREEN_WIDTH;
		config.height = AuraluxTest.SCREEN_HEIGHT;
		new LwjglApplication(new AuraluxTest(null), config);
	}
}
