package rs.fon.whibo.GDT.component.possibleSplits.tools;

import java.util.List;

import rs.fon.whibo.GDT.tools.Tools;
import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCapability;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.matrix.CovarianceMatrix;

public class LinearDiscriminantAnalysis extends com.rapidminer.operator.learner.AbstractLearner{

	public LinearDiscriminantAnalysis(OperatorDescription description) {
		super(description);
	}
	public Model learn(ExampleSet exampleSet) throws OperatorException {
		int numberOfNumericalAttributes = 0;
		for (Attribute attribute: exampleSet.getAttributes()) {
			if (attribute.isNumerical()) {
				numberOfNumericalAttributes++;	
			}
		}

		List<String> presentLabels = Tools.getAllCategories(exampleSet, exampleSet.getAttributes().getLabel());
		String[] labelValues = (String[]) presentLabels.toArray(new String[0]);

		Matrix[] meanVectors = getMeanVectors(exampleSet, numberOfNumericalAttributes, labelValues);
		Matrix[] inverseCovariance = getInverseCovarianceMatrices(exampleSet, labelValues);

		return getModel(exampleSet, labelValues, meanVectors, inverseCovariance, getAprioriProbabilities(exampleSet, labelValues));
	}


	protected DiscriminantModel getModel(ExampleSet exampleSet, String[] labels, Matrix[] meanVectors, Matrix[] inverseCovariances, double[] aprioriProbabilities) throws UndefinedParameterError {
		return new DiscriminantModel(exampleSet, labels, meanVectors, inverseCovariances, aprioriProbabilities, 0d);		
	}

	private double[] getAprioriProbabilities(ExampleSet exampleSet, String[] labels) {
		double[] aprioriProbabilites = new double[labels.length];
		double totalSize = exampleSet.size();
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		SplittedExampleSet labelSet = SplittedExampleSet.splitByAttribute(exampleSet, exampleSet.getAttributes().getLabel());
		int labelIndex = 0;
		for (String label: labels) {
			// select apropriate subset
			for (int i = 0; i < labels.length; i++) {
				labelSet.selectSingleSubset(i);
				if (labelSet.getExample(0).getNominalValue(labelAttribute).equals(label))
					break;
			}
			// calculate apriori Prob
			aprioriProbabilites[labelIndex] = labelSet.size() / totalSize;
			labelIndex++;
		}
		return aprioriProbabilites;
	}

	protected Matrix[] getInverseCovarianceMatrices(ExampleSet exampleSet, String[] labels) throws UndefinedParameterError {
		Matrix[] classInverseCovariances = new Matrix[labels.length];
		Matrix inverse = invertMatrix(CovarianceMatrix.getCovarianceMatrix(exampleSet));
		for (int i = 0; i < labels.length; i++)
			classInverseCovariances[i] = inverse;
		return classInverseCovariances;
	}


	protected Matrix[] getMeanVectors(ExampleSet exampleSet, int numberOfAttributes, String[] labels) throws UserError {
		Matrix[] classMeanVectors = new Matrix[labels.length];
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		SplittedExampleSet labelSet = SplittedExampleSet.splitByAttribute(exampleSet, exampleSet.getAttributes().getLabel());
		if (labelSet.getNumberOfSubsets() != labels.length)
			throw new UserError(this, 118, labelAttribute, labelSet.getNumberOfSubsets(), 2);
		int labelIndex = 0;
		for (String label: labels) {
			// select apropriate subset
			for (int i = 0; i < labels.length; i++) {
				labelSet.selectSingleSubset(i);
				if (labelSet.getExample(0).getNominalValue(labelAttribute).equals(label))
					break;
			}
			// calculate mean
			labelSet.recalculateAllAttributeStatistics();
			double[] meanValues = new double[numberOfAttributes];
			int i = 0;
			for (Attribute attribute: labelSet.getAttributes()) {
				if (attribute.isNumerical()) {
					meanValues[i] = labelSet.getStatistics(attribute, Statistics.AVERAGE);
				}
				i++;
			}
			classMeanVectors[labelIndex] = new Matrix(meanValues, 1);
			labelIndex++;
		}
		return classMeanVectors;
	}


	private Matrix invertMatrix(Matrix m) {
		double startFactor = 0.1d;

		while (true) {
			try {
				Matrix inverse = m.inverse();
				//Matrix inverse = (new QRDecomposition(m)).solve(Matrix.identity(m.getRowDimension(), m.getRowDimension()));

				//				SingularValueDecomposition svd = m.svd();
				//				Matrix pseudoInverse = svd.getV().times(svd.getS().inverse()).times(svd.getU());

				return inverse;
			} catch (Exception e) {
				for (int x = 0; x < m.getColumnDimension(); x++) {
					for (int y = 0; y < m.getRowDimension(); y++) {
						m.set(x, y, m.get(x, y) + startFactor);
					}
				}
				startFactor *= 10d;
			}
		}
	}
	@Override
	public boolean supportsCapability(OperatorCapability capability) {
		// TODO Auto-generated method stub
		return false;
	}


}
