package rs.fon.whibo.GDT.component.possibleSplits.tools;

public class OC1tool {

	private static final double MAX_COEFFICIENT= 1.0;

	public static double[] generateRandomHyperplane(int lenght){
		double[] coefficients= new double[lenght];
		for (int i=0;i<coefficients.length;i++)
			coefficients[i] = myrandom(-MAX_COEFFICIENT, MAX_COEFFICIENT);
		return coefficients;
	}

	private static double myrandom( double above, double below){
		return ((above + Math.random() * (below - above)));
	}



}
