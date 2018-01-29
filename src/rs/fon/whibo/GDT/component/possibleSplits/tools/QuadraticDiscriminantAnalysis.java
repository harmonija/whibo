package rs.fon.whibo.GDT.component.possibleSplits.tools;

import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.math.matrix.CovarianceMatrix;

public class QuadraticDiscriminantAnalysis extends LinearDiscriminantAnalysis {

	public QuadraticDiscriminantAnalysis(OperatorDescription description) {
		super(description);
	}

	@Override
	protected DiscriminantModel getModel(ExampleSet exampleSet, String[] labels, Matrix[] meanVectors, Matrix[] inverseCovariances, double[] aprioriProbabilities) {
		return new DiscriminantModel(exampleSet, labels, meanVectors, inverseCovariances, aprioriProbabilities, 1d);		
	}

	@Override
	protected Matrix[] getInverseCovarianceMatrices(ExampleSet exampleSet, String[] labels) throws UndefinedParameterError {
		Matrix[] classInverseCovariances = new Matrix[labels.length];
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
			// calculate inverse matrix
			Matrix inverse = CovarianceMatrix.getCovarianceMatrix(labelSet).inverse();
			classInverseCovariances[labelIndex] = inverse;
			labelIndex++;
		}
		return classInverseCovariances;
	}


}
