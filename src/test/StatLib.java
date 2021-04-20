package test;

import java.lang.Math;

public class StatLib {

	private static float sum(float[] x) {
		float sum=0;
		for (float f : x) {
			sum += f;
		}
		return sum;
	}
	
	private static float[] product(float[] x, float[] y) {
		float[] result = new float[x.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = x[i] * y[i];
		}

		return result;
	}

	// simple average
	public static float avg(float[] x){
		return (sum(x)/x.length);
	}

	// returns the variance of X and Y
	public static float var(float[] x){
		float[] xSquer = product(x,x);
		return avg(xSquer) - (float)Math.pow(avg(x),2);
	}

	// returns the covariance of X and Y
	public static float cov(float[] x, float[] y){
		float[] product_arr = product(x,y);
		return avg(product_arr) - avg(x)*avg(y);
	}


	// returns the Pearson correlation coefficient of X and Y
	public static float pearson(float[] x, float[] y){
		return cov(x,y) / (float)Math.sqrt(var(x)*var(y));
	}

	// performs a linear regression and returns the line equation
	public static Line linear_reg(Point[] points){
		float a,b;
		float[] x = new float[points.length];
		float[] y = new float[points.length];
		for (int i = 0; i < points.length; i++) {
			x[i]=points[i].x;
			y[i]=points[i].y;
		}
		a = cov(x,y)/var(x);
		b = avg(y)-a*avg(x);
		return new Line(a,b);
	}

	// returns the deviation between point p and the line equation of the points
	public static float dev(Point p,Point[] points){
		Line main = linear_reg(points);
		return dev(p,main);
	}

	// returns the deviation between point p and the line
	public static float dev(Point p,Line l){
		return Math.abs(l.a*p.x-p.y+l.b);
	}
	
	public static Point[] makePoint(float[] x, float[] y) {
		Point[] result = new Point[x.length];
		for (int i = 0; i < x.length; i++) {
			result[i] = new Point(x[i],y[i]);
		}
		return result;
	}
}
