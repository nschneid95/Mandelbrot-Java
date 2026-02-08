import javax.swing.event.*;
import java.awt.event.*;

public class MouseHandler extends MouseInputAdapter{
	
	@Override
	public void mousePressed(MouseEvent event) {
		Mandelbrot.mousePressed();
	}
	
	@Override
	public void mouseReleased(MouseEvent event) {
		Mandelbrot.mouseReleased();
	}
	
	@Override
	public void mouseDragged(MouseEvent event) {
		Mandelbrot.mouseDragged();
	}
	
	@Override
	public void mouseExited(MouseEvent event) {
		Mandelbrot.mouseExited();
	}
}