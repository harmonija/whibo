package rs.fon.whibo.GDT.component.possibleSplits;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import rs.fon.whibo.GDT.component.possibleSplits.tools.OC1tool;
import rs.fon.whibo.GDT.component.splitEvaluation.SplitEvaluation;
import rs.fon.whibo.GDT.dataset.SplittedExampleSet;
import rs.fon.whibo.problem.SubproblemParameter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;

public class OC1Split extends AbstractPossibleSplit {

	/**
	 * List of partitions that define all possible ways for splitting dataset in
	 * current node.
	 */
	LinkedList<Partition> allPartitionsForAttribute;

	/**
	 * List of values that defines all split points for splitting dataset in
	 * current node..
	 */
	LinkedList<Double> splitValueList;

	/** Current attribute for candidate split creation. */
	Attribute currentAttribute;

	Attribute u;
	double[] a;
	int m;// m atribut, krece od 0

	@Override
	public void init(ExampleSet exampleSet,
			List<Attribute> attributesForSplitting) {
		// super.init(exampleSet, attributesForSplitting);
		currentSplit = new SplittedExampleSet(exampleSet, new Partition(
				new int[exampleSet.size()], 2));
		allPartitionsForAttribute = new LinkedList<Partition>();
		splitValueList = new LinkedList<Double>();
		currentAttribute = null;

		u = AttributeFactory.createAttribute("U", Ontology.REAL);
		this.attributesForSplitting = attributesForSplitting; // /negde proveri
		// da li su
		// numericki
		this.attributesForSplitting.add(u);
		exampleSet.getAttributes().addRegular(u);
		iteratorAtt = this.attributesForSplitting.listIterator();

		// u perturb
		a = OC1tool.generateRandomHyperplane(attributesForSplitting.size() + 1);
		/*
		 * for (Example example : exampleSet) { double uj =
		 * computeUj(example,m); example.setValue(u, uj);
		 * 
		 * }
		 */
		/*
		 * Repeat until none of the coefficient values is modified in the For
		 * loop: For i=1 to d, Perturb(H,i)
		 */

		for (int i = 0; i < a.length; i++) {
			m = i;
			perturb(a, i, exampleSet);
		}

	}

	private double computeUj(Example example, int m) {

		double sumaixi = a[a.length - 1]; // Vj= sumaixi+ ad+1;
		for (int i = 0; i < a.length - 1; i++) { // a ima d+1
			double xi = example.getValue(attributesForSplitting.get(i)); // att for split ima d
			// normalnih
			double ai = a[i];
			double aixi = ai * xi;
			sumaixi += aixi;
		}

		double Uj = (a[m] * example.getValue(attributesForSplitting.get(m)) - sumaixi)
				/ example.getValue(attributesForSplitting.get(m));
		return Uj;

	}

	public OC1Split(List<SubproblemParameter> parameters) {
		super(parameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SplittedExampleSet nextSplit() throws Exception {
		if (!hasNextSplit()) {
			return null;
		}

		if (allPartitionsForAttribute.isEmpty()) {

			currentAttribute = iteratorAtt.next();
			while (!currentAttribute.isNumerical()) {
				currentAttribute = iteratorAtt.next();
			}
			getPartitionsForAttribute(currentSplit, currentAttribute);
			Split(currentSplit, currentAttribute);

		} else
			Split(currentSplit, currentAttribute);

		return currentSplit;
	}

	@Override
	public boolean hasNextSplit() { // trebalo bi da resi prvo sve obiène pa
		// onda Uj
		// if there are more possible splits in generated partitions for an
		// attribute
		if (!allPartitionsForAttribute.isEmpty())
			return true;

		// if there are more numerical attributes
		ListIterator<Attribute> i = attributesForSplitting
				.listIterator(iteratorAtt.nextIndex());

		while (i.hasNext()) {
			if (i.next().isNumerical())
				return true;
		}

		return false;
	}

	@Override
	public boolean isCategoricalSplit() {
		return false;
	}

	@Override
	public boolean isNumericalSplit() {
		return true;
	}

	@Override
	public String[] getNotCompatibleClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getExclusiveClassNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean perturb(double[] hyperplane, int currentm,
			ExampleSet exampleSet) {

		// a= OC1tool.generateRandomHyperplane(attributesForSplitting.size()+1);

		for (Example example : exampleSet) {
			double uj = computeUj(example, m);
			example.setValue(u, uj);
		}

		SortedExampleSet sortedexampleSet = new SortedExampleSet(exampleSet, u,
				SortedExampleSet.INCREASING);
		getPartitionsForAttribute(sortedexampleSet, u);
		Split(currentSplit, u);
		// SplittedExampleSet bestSplit = evaluate();
		// splitValueList

		////SVE ISPOCETKA

		double am = 1.0;// best Ui
		double[] h1 = new double[a.length];
		for (int i = 0; i < h1.length; i++) {
			h1[i] = a[i];
		}
		h1[m] = am;

		if (getImpuriti(h1) < getImpuriti(a)) {
			a[m] = h1[m];

			return true;
		} else if (getImpuriti(h1) == getImpuriti(a)) {

			return true;
		}

		return false;

	}

	private double getImpuriti(double[] h) {
		return 0.5;
	}

	private SplittedExampleSet evaluate(SplitEvaluation componentEval) {

		/*
		 * String className=String.valueOf(InformationGain.class); Constructor c
		 * = Class.forName(className).getConstructor(); componentEval
		 * =((SplitEvaluation)c.newInstance(new
		 * Object[]{steps.getSubproblemData().getListOfParameters()}));
		 */

		double bestSplitEvaluation = 0.0;
		SplittedExampleSet bestPossibleSplit = currentSplit;// koji je najbolji?

		while (hasNextSplitInter()) {
			SplittedExampleSet possibleSplit;
			try {
				possibleSplit = nextSplit();

				double splitEvaluation = componentEval.evaluate(possibleSplit);

				boolean splitIsBetter = componentEval.betterThan(
						splitEvaluation, bestSplitEvaluation);
				// subTreeLog.add(LogPossibleSplit.logEvaluation(splitEvaluation,
				// bestSplitEvaluation, splitIsBetter));
				if (splitIsBetter) {
					bestPossibleSplit = (SplittedExampleSet) possibleSplit
							.clone(); // OK/ //TODO: READ PROOF! da li uzima
					// memoriju
					bestSplitEvaluation = splitEvaluation;
				}
			} catch (Exception e) {
				e.printStackTrace();// ne baca nista
			}
		}

		return bestPossibleSplit;
		// return getImpuriti();
	}

	private boolean hasNextSplitInter() {

		if (!allPartitionsForAttribute.isEmpty())
			return true;

		return false;

	}

	/**
	 * Creates binary numerical splitting candidate for evaluation.
	 * 
	 * @param currentSplit
	 *            - dataset in current node that will be splitted
	 * @param attribute
	 *            - attribute for splitting
	 */
	private void Split(SplittedExampleSet currentSplit, Attribute attribute) {
		currentSplit.setPartition(allPartitionsForAttribute.removeFirst());
		currentSplit.setAttribute(attribute);
		currentSplit.setSplitValue(splitValueList.removeFirst().doubleValue());
	}

	/**
	 * Creates all possible partitions for current numerical attribute
	 * 
	 * @param inputSet
	 *            - dataset in current node
	 * @param attribute
	 *            - the attribute for splitting
	 * 
	 */
	private void getPartitionsForAttribute(ExampleSet inputSet,
			Attribute attribute) {
		SortedExampleSet exampleSet = new SortedExampleSet(
				(ExampleSet) inputSet, attribute, SortedExampleSet.INCREASING);
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		double oldLabel = Double.NaN;
		double lastValue = Double.NaN;
		;
		int counter = -1;

		for (Example e : exampleSet) {
			counter++;
			double currentValue = e.getValue(attribute);
			double label = e.getValue(labelAttribute);
			if (Double.isNaN(lastValue))
				lastValue = currentValue;
			if ((Double.isNaN(oldLabel))
					|| ((oldLabel != label) && (lastValue != currentValue))) {
				double splitValue = (lastValue + currentValue) / 2.0d;
				Partition partition = splitByAttribute(inputSet, attribute,
						splitValue);
				splitValueList.add(splitValue);
				allPartitionsForAttribute.add(partition);
			}
			lastValue = currentValue;
			oldLabel = label;
		}

	}

	/**
	 * Creates one partition for current attribute and split point.
	 * 
	 * @param exampleSet
	 *            - example set in current node
	 * @param attribute
	 *            - current numerical attribute for candidate split creation
	 * @param value
	 *            - split point value
	 * 
	 * @return Partition - one partition for attribute for a given split point
	 */
	private Partition splitByAttribute(ExampleSet exampleSet,
			Attribute attribute, double value) {
		int[] elements = new int[exampleSet.size()];
		Iterator<Example> reader = exampleSet.iterator();
		int i = 0;

		while (reader.hasNext()) {
			Example example = reader.next();
			double currentValue = example.getValue(attribute);
			if (currentValue <= value)
				elements[i++] = 0;
			else
				elements[i++] = 1;
		}
		return new Partition(elements, 2);

	}

}