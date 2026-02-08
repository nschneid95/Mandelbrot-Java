import java.awt.image.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.math.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Mandelbrot {
	static BufferedImage bimage;
	static ImageIcon image;
	static JLabel label;
	static JFrame frame;
	static int width = 512;
	static int height = 500;
	static Point startZoom;
	static Point endZoom;
	static MouseHandler mouseHandler;
	static boolean rendering;
	
	// Arrays for box drawing
	static int[] top;
	static int[] bottom;
	static int[] left;
	static int[] right;
	
	// Data for math
	static Complex[][] values;
	static Complex topLeft;
	static Complex bottomRight;
	static int previousIterations;
	static int minIterationsUsed;
	static int[][] colors;
	
	static ExecutorService threadPool;
	static int loadingColor = 0xFFFFFFFF;
	
	public static void main(String[] args) {
		threadPool = Executors.newFixedThreadPool(3);
		values = new Complex[width][height];
		colors = new int[width][height];
		rendering = true;
		startZoom = null;
		endZoom = null;
		bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		bottomRight = new Complex(new BigDecimal(2), new BigDecimal(-2));
		topLeft = new Complex(new BigDecimal(-2), new BigDecimal(2));
		previousIterations = 100;
		image = new ImageIcon(bimage);
		label = new JLabel(image);
		label.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		mouseHandler = new MouseHandler();
		label.addMouseMotionListener(mouseHandler);
		label.addMouseListener(mouseHandler);
		frame = new JFrame();
		frame.getContentPane().add(label);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE	);
		frame.setVisible(true);
		render();
		rendering = false;
	}
	
	public static void mousePressed() {
		if (rendering)
			return;
		startZoom = subtract(MouseInfo.getPointerInfo().getLocation(), label.getLocationOnScreen());
	}
	
	public static void mouseReleased() {
		if (startZoom == null || rendering || endZoom == null)
			return;
		top = null;
		left = null;
		bottom = null;
		right = null;
		rendering = true;
		int minY = startZoom.y > endZoom.y ? endZoom.y : startZoom.y;
		int minX = startZoom.x > endZoom.x ? endZoom.x : startZoom.x;
		int maxX = startZoom.x > endZoom.x ? startZoom.x : endZoom.x;
		int maxY = startZoom.y > endZoom.y ? startZoom.y : endZoom.y;
		topLeft = values[minX][minY];
		bottomRight = values[maxX][maxY];
		render();
		startZoom = null;
		endZoom = null;
		rendering = false;
	}
	
	public static void mouseDragged() {
		if (startZoom == null || rendering)
			return;
		// Overwrite old box with proper pixels
		if (top != null && bottom != null && left != null && right != null)
			clearBox();
		// Calculate the new box parameters
		endZoom = subtract(MouseInfo.getPointerInfo().getLocation(), label.getLocationOnScreen());
		// Make zoom a box
		if (Math.abs(endZoom.x - startZoom.x) > Math.abs(endZoom.y - startZoom.y)) {
			if (endZoom.y >= startZoom.y)
				endZoom.y = startZoom.y + Math.abs(endZoom.x - startZoom.x);
			else
				endZoom.y = startZoom.y - Math.abs(endZoom.x - startZoom.x);
		}
		else {
			if (endZoom.x >= startZoom.x)
				endZoom.x = startZoom.x + Math.abs(endZoom.y - startZoom.y);
			else
				endZoom.x = startZoom.x - Math.abs(endZoom.y - startZoom.y);
		}
		if (endZoom.x >= width || endZoom.y >= height) {
			endZoom.x = Math.max(Math.min(endZoom.x, width - 1), 0);
			endZoom.y = Math.max(Math.min(endZoom.y, height - 1), 0);
			mouseExited();
			return;
		}
		// Draw the new box
		drawBox();
	}
	
	public static void mouseExited() {
		if (rendering)
			return;
		if (startZoom != null && endZoom != null)
			clearBox();
		startZoom = null;
		endZoom = null;
		top = null;
		bottom = null;
		left = null;
		right = null;
	}
	
	/*
	 * Renders the screen and paints it by dynamically choosing colors.
	 */
	private static void paintScreen() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (colors[x][y] == -1)
				{
					bimage.setRGB(x, y, 0xFF000000);
				}
				else if (minIterationsUsed < 0 || colors[x][y] < minIterationsUsed)
				{
					bimage.setRGB(x, y, loadingColor);
				}
				else
				{
//					double numerator = (double)(previousIterations - colors[x][y]);
//					double denominator = (double)(previousIterations - minIterationsUsed);
//					double colorValue = numerator / denominator * 0xFFFFFF;
					bimage.setRGB(x, y, ColorMap.mapColor(colors[x][y]));
				}
			}
		}
	}
	
	/*
	 * Initializes a new Renderer to render the given portion of the screen.
	 */
	private static Renderer initRenderer(int minX, int maxX, int minY, int maxY) {
		Renderer r = new Renderer();
		r.values = refreshValues(minX, maxX, minY, maxY);
		r.baseValues = values;
		r.previousIterations = previousIterations;
		r.colors = colors;
		return r;
	}
	
	/*
	 * Renders the entire screen.
	 */
	private static void render(){
		System.out.println("Rendering...");
		long startTime = System.currentTimeMillis();
		int numberOfXDivisions = 15;
		int numberOfYDivisions = 15;
		label.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Renderer[][] renderers = new Renderer[numberOfXDivisions][numberOfYDivisions];
		for (int i  = 0; i < numberOfXDivisions; i++) {
			for (int j = 0; j < numberOfYDivisions; j++) {
				renderers[i][j] = initRenderer(j * width / numberOfXDivisions, (j + 1) * width / numberOfXDivisions, i * height / numberOfYDivisions, (i + 1) * height / numberOfYDivisions);
			}
		}
		for (Renderer[] array : renderers) {
			for (Renderer r : array) {
				threadPool.execute(r);
			}
		}
		
		previousIterations = 0;
		minIterationsUsed = -1;
		
		for (Renderer[] array : renderers) {
			for (Renderer r : array) {
				while (!r.isComplete) {
					try {
						TimeUnit.MILLISECONDS.sleep(500);
					} catch (InterruptedException e) {}
					for (Renderer[] array2 : renderers) {
						for (Renderer r2 : array2) {
							previousIterations = Math.max(previousIterations, r2.previousIterations);
							if (r2.minIterationsUsed >= 0)
							{
								if (minIterationsUsed >= 0)
									minIterationsUsed = Math.min(minIterationsUsed, r2.minIterationsUsed);
								else
									minIterationsUsed = r2.minIterationsUsed;
							}
						}
					}
					paintScreen();
					label.paintImmediately(0, 0, width, height);
				}
			}
		}
		previousIterations = 0;
		for (Renderer[] array : renderers) {
			for (Renderer r : array) {
				previousIterations = Math.max(previousIterations, r.previousIterations);
			}
		}
		paintScreen();
		label.repaint();
		System.out.println("Rendering is complete");
		System.out.println((System.currentTimeMillis() - startTime) / 1000 + " seconds");
		label.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	/*
	 * Refreshes the data for the given section of the screen, including the values, colors, and starting c values.
	 */
	private static HashMap<Point, Complex> refreshValues(int minWidth, int maxWidth, int minHeight, int maxHeight) {
		minIterationsUsed = 0;
		HashMap<Point, Complex> startValues = new HashMap<Point, Complex>();
		for (int x = minWidth; x < maxWidth; x++) {
			for (int y = minHeight; y < maxHeight; y++) {
				BigDecimal real = bottomRight.getRealPart().subtract(topLeft.getRealPart());
				real = real.multiply(new BigDecimal(x)).divide(new BigDecimal(width)).add(topLeft.getRealPart());
				BigDecimal imag = bottomRight.getImaginaryPart().subtract(topLeft.getImaginaryPart());
				imag = imag.multiply(new BigDecimal(y)).divide(new BigDecimal(height)).add(topLeft.getImaginaryPart());
				values[x][y] = new Complex(real, imag);
				startValues.put(new Point(x, y), new Complex(new BigDecimal(0), new BigDecimal(0)));
				colors[x][y] = 0;
			}
		}
		return startValues;
	}
	
	private static void clearBox() {
		int minY = startZoom.y > endZoom.y ? endZoom.y : startZoom.y;
		int minX = startZoom.x > endZoom.x ? endZoom.x : startZoom.x;
		int maxX = startZoom.x > endZoom.x ? startZoom.x : endZoom.x;
		int maxY = startZoom.y > endZoom.y ? startZoom.y : endZoom.y;
		for (int i = 0; i < top.length; i++)
			bimage.setRGB(i + minX, minY, top[i]);
		for (int i = 0; i < bottom.length; i++)
			bimage.setRGB(i + minX, maxY, bottom[i]);
		for (int i = 0; i < left.length; i++)
			bimage.setRGB(minX, i + minY + 1, left[i]);
		for (int i = 0; i < right.length; i++)
			bimage.setRGB(maxX, i + minY + 1, right[i]);
		label.repaint();
	}
	
	private static void drawBox() {
		int minY = startZoom.y > endZoom.y ? endZoom.y : startZoom.y;
		int minX = startZoom.x > endZoom.x ? endZoom.x : startZoom.x;
		int maxX = startZoom.x > endZoom.x ? startZoom.x : endZoom.x;
		int maxY = startZoom.y > endZoom.y ? startZoom.y : endZoom.y;
		top = new int[maxX - minX + 1];
		bottom = new int[maxX - minX + 1];
		// Store the pixels and draw the new box
		for (int x = minX; x <= maxX; x++) {
			top[x - minX] = bimage.getRGB(x, minY);
			bottom[x - minX] = bimage.getRGB(x, maxY);
			bimage.setRGB(x, minY, ColorMap.invertColor(bimage.getRGB(x, minY)));
			bimage.setRGB(x, maxY, ColorMap.invertColor(bimage.getRGB(x, maxY)));
		}
		if (maxY != minY) {
			left = new int[maxY - minY - 1];
			right = new int[maxY - minY - 1];
			for (int y = minY + 1; y < maxY; y++) {
				left[y - minY - 1] = bimage.getRGB(minX, y);
				right[y - minY - 1] = bimage.getRGB(maxX, y);
				bimage.setRGB(minX, y, ColorMap.invertColor(bimage.getRGB(minX, y)));
				bimage.setRGB(maxX, y, ColorMap.invertColor(bimage.getRGB(maxX, y)));
			}
		}
		else {
			left = new int[0];
			right = new int[0];
		}
		label.repaint();
	}
	
	private static Point subtract(Point p1, Point p2) {
		return new Point(p1.x - p2.x, p1.y - p2.y);
	}
}