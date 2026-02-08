import java.util.HashMap;
import java.awt.Point;
import java.lang.Thread;
import java.math.BigDecimal;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import java.lang.Runnable;
import java.util.Iterator;

public class Renderer implements Runnable {
	public HashMap<Point, Complex> values;
	public Complex[][] baseValues;
	public int previousIterations;
	public int minIterationsUsed;
	public int[][] colors;
	public boolean isComplete;
	
	public Renderer() {
		isComplete = false;
		minIterationsUsed = -1;
	}
	
	public void run()
	{
		if (values == null) {
			isComplete = true;
			return;
		}
		int numValues = values.keySet().size();
		int staleIterations = 0;
		int i = 0;
		while (staleIterations < 15 || i <= previousIterations) {
			if (values.keySet().size() == 0) {
				isComplete = true;
				return;
			}
			renderNext(values, baseValues);
			if (numValues == values.keySet().size())
				staleIterations++;
			else
				staleIterations = 0;
			numValues = values.keySet().size();
			i++;
		}
		// Rendering is now complete - set colors on all trapped values to -1
		for (Point p : values.keySet()) {
			colors[p.x][p.y] = -1;
		}
		previousIterations = i;
		isComplete = true;
	}
	
	private void renderNext(HashMap<Point, Complex> currentValues, Complex[][] c) {
		int startSize = currentValues.keySet().size();
		Iterator<Point> itr = currentValues.keySet().iterator();
		while (itr.hasNext()) {
			Point p = itr.next();
			Complex z = currentValues.get(p);
			z = fractalFunction(z, c[p.x][p.y]);
			// If z.Real > 2 or z.Imag > 2 then it escapes
			if (z.getRealPart().compareTo(new BigDecimal(2)) <= 0 && z.getImaginaryPart().compareTo(new BigDecimal(2)) <= 0) {
				colors[p.x][p.y]++;
				currentValues.put(p, z);
			}
			else {
				if (minIterationsUsed < 0)
					minIterationsUsed = colors[p.x][p.y];
				itr.remove();
			}
		}
	}
	
	private Complex fractalFunction(Complex z, Complex c) {
		return z.multiply(z).add(c);
	}
}