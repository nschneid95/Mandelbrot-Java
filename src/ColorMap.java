public final class ColorMap {
	
	private ColorMap() {}
	
	public static int mapColor(long value) {
		final double SCALE = 1.0;
		final int R = 0xFFFF0000;
		final int G = 0xFF00FF00;
		final int B = 0xFF0000FF;
		value = (long)(value * SCALE) % (256 * 6);
		// Which max saturation line on the color cube are we
		int line = (int)(value / 256);
		// Beginning of line is 0, end is 256
		int fractionOfLine = (int)(value % 256);
		// Convert each line to a color
		switch (line) {
		case 0:
			// Red to yellow line
			return R + (fractionOfLine << 8);
		case 1:
			// Yellow to green line
			return G + ((255 - fractionOfLine) << 16);
		case 2:
			// Green to cyan line
			return G + fractionOfLine;
		case 3:
			// Cyan to blue line
			return B + ((255 - fractionOfLine) << 8);
		case 4:
			// Blue to purple line
			return B + (fractionOfLine << 16);
		case 5:
			// Purple to red line
			return R + (255 - fractionOfLine);
		default:
			// This never happens
			return -1;
		}
	}
	
	public static int invertColor(int color) {
		int alpha = color & 0xFF000000;
		int R = color & 0x00FF0000;
		int G = color & 0x0000FF00;
		int B = color & 0x000000FF;
		return alpha + (0x00FF0000 - R) + (0x0000FF00 - G) + (0x000000FF - B); 
	}
}