public final class ColorMap {
	
	private ColorMap() {}
	
	public static int mapColor(int value) {
		int color = 0xFF000000;
		color += (int)(0xFF * Math.pow(Math.sin(Math.PI * value * 1. / 0xFFFFFF / 2), 2)) << 16;
		color += (int)(0xFF * Math.pow(Math.sin(Math.PI * value * 3. / 0xFFFFFF / 2), 2)) << 8;
		color += (int)(0xFF * Math.pow(Math.sin(Math.PI * value * 5. / 0xFFFFFF / 2), 2)) << 0;
		return color;
	}
	
	public static int mapColor(long value) {
		return mapColor((int)value);
	}
	
	public static int invertColor(int color) {
		int alpha = color & 0xFF000000;
		int R = color & 0x00FF0000;
		int G = color & 0x0000FF00;
		int B = color & 0x000000FF;
		return alpha + (0x00FF0000 - R) + (0x0000FF00 - G) + (0x000000FF - B); 
	}
}