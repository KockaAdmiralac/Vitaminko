package rs.kockasystems.auraluxtest;

public class Data
{
	public float x, y, radius, x1, y1;

	public Data(final String[] format)
	{
		x = Float.valueOf(format[0]);
		y = Float.valueOf(format[1]);
		radius = Float.valueOf(format[2]);
		x1 = Float.valueOf(format[3]);
		y1 = Float.valueOf(format[4]);
	}
	
	public String toString() { return x + "|" + y + "|" + radius + "|" + x1 + "|" + y1; }
	
}
