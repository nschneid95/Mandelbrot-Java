import java.math.*;

public class Complex {
	private BigDecimal real;
	private BigDecimal imag;
	static final MathContext context = MathContext.DECIMAL64;
	
	public Complex(BigDecimal realPart, BigDecimal imaginaryPart) {
		real = realPart.round(context);
		imag = imaginaryPart.round(context);
	}
	
	public Complex(Complex c) {
		real = c.getRealPart();
		imag = c.getImaginaryPart();
	}
	
	public BigDecimal getRealPart() {
		return real;
	}
	
	public BigDecimal getImaginaryPart() {
		return imag;
	}
	
	public Complex round() {
		return new Complex(real.round(context), imag.round(context));
	}
	
	public Complex add(Complex c) {
		return new Complex(real.add(c.getRealPart()), imag.add(c.getImaginaryPart()));
	}
	
	public Complex subtract(Complex c) {
		return new Complex(real.subtract(c.getRealPart()), imag.subtract(c.getImaginaryPart()));
	}
	
	public Complex multiply(Complex c) {
		return new Complex(real.multiply(c.getRealPart()).subtract(imag.multiply(c.getImaginaryPart())), real.multiply(c.getImaginaryPart()).add(imag.multiply(c.getRealPart())));
	}
	
	public Complex invert() {
		BigDecimal scalar = real.multiply(real).subtract(imag.multiply(imag));
		return new Complex(real.divide(scalar), imag.divide(scalar)).negate();
	}
	
	public Complex divide(Complex c) {
		return this.multiply(c.invert());
	}
	
	public Complex negate() {
		return new Complex(real.negate(), imag.negate());
	}
	
	public Complex conjugate() {
		return new Complex(real, imag.negate());
	}
	
	public BigDecimal abs() {
		return real.multiply(real).add(imag.multiply(imag));
	}
	
	@Override
	public String toString() {
		return real.toString() + " + " + imag.toString() + "i";
	}
}