package rs.fon.whibo.GDT.component.possibleSplits.tools;

import java.util.Iterator;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public class DiscriminantModel extends com.rapidminer.operator.learner.SimplePredictionModel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2790159597809590939L;
	/**
	 * 
	 */

	private double alpha;
	private String[] labels;

	private Matrix[] meanVectors;
	private Matrix[] inverseCovariances;
	private double[] aprioriProbabilities;

	private double[] constClassValues;

	public DiscriminantModel(ExampleSet exampleSet, String[] labels, Matrix[] meanVectors, Matrix[] inverseCovariances,double[] aprioriProbabilities, double alpha) {
		super(exampleSet);
		this.alpha = alpha;
		this.labels = labels;
		this.meanVectors = meanVectors;
		this.inverseCovariances = inverseCovariances;
		this.aprioriProbabilities = aprioriProbabilities;

		this.constClassValues = new double[labels.length];
		/*		for (int i = 0; i < labels.length; i++) {
			constClassValues[i] = - 0.5d * meanVectors[i].times(inverseCovariances[i]).times(meanVectors[i].transpose()).get(0, 0)
									+ Math.log(aprioriProbabilities[i]); 
		}
		 */	}

	public double[] newLinearAttributes(Example example) throws OperatorException {
		int numberOfAttributes = meanVectors[0].getColumnDimension();
		double[] vector = new double[numberOfAttributes];
		int i = 0;
		Attribute attribute;

		Iterator<Attribute> iteratorAtt=example.getAttributes().iterator();
		for (int j = 0; j < example.getAttributes().size(); j++) {
			attribute=iteratorAtt.next();
			if (attribute.isNumerical()) {
				vector[i] = example.getValue(attribute);

				i++;
			}
		}

		//TODO: JECO UBICU TE :) :))))
		//		Iterator<Attribute> iteratorAtt=example.getAttributes().iterator();
		//		//for (Attribute attribute: example.getAttributes()) {
		//		for (int j = 0; j < example.getAttributes().size()-labels.length; j++) {
		//			attribute=iteratorAtt.next();
		//	//	}
		//			if (attribute.isNumerical()) {
		//				vector[i] = example.getValue(attribute);
		//				
		//				i++;
		//			}
		//		}
		Matrix xVector = new Matrix(vector, 1);

		double[] labelFunction = new double[labels.length];
		for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
			labelFunction[labelIndex] = 
					xVector.times(inverseCovariances[labelIndex]).times(meanVectors[labelIndex].transpose()).get(0,0) + constClassValues[labelIndex]; 

		}

		return labelFunction;
	}

	public String[] getlabels(){
		return labels;
	}

	public Matrix[] getMeanVectors(){
		return meanVectors;
	}

	@Override
	public double predict(Example example) throws OperatorException {
		// TODO Auto-generated method stub
		return 0;
	}
}
