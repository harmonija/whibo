package rs.fon.whibo.GDT.component.possibleSplits;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import rs.fon.whibo.GDT.component.possibleSplits.tools.DiscriminantModel;
import rs.fon.whibo.GDT.component.possibleSplits.tools.QuadraticDiscriminantAnalysis;
import rs.fon.whibo.GDT.dataset.SplittedExampleSet;
import rs.fon.whibo.GDT.tools.PruningTools;
import rs.fon.whibo.problem.SubproblemParameter;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

public class QuadraticDiscriminantSpllit extends AbstractPossibleSplit {

	QuadraticDiscriminantAnalysis quadraticDiscriminantAnalysis;
	DiscriminantModel model;
	String[] labels;
	private static int newQuadraticAttributeIndex = 1;
	List<Attribute> newQuadraticAttributeList ;


	/** List of partitions that define all possible ways for splitting dataset in current node. */
	LinkedList <Partition> allPartitionsForAttribute;

	/** List of values that defines all split points for splitting dataset in current node.. */
	LinkedList<Double> splitValueList;

	/** Current attribute for candidate split creation. */
	Attribute currentAttribute;
	/** The iterator att. */
	ListIterator<Attribute> iteratorAtt;
	/** List of parameters for Possible Split components. */
	protected List<SubproblemParameter> parameters;

	/** Current split for evaluation . */
	protected SplittedExampleSet currentSplit;

	/** List of attributes that are awailable for possible split creation. */
	protected List<Attribute> attributesForSplitting;


	@Override
	public void init(ExampleSet exampleSet,	List<Attribute> attributesForSplitting) {

		ArrayList<Attribute> oldAttributes = new ArrayList<Attribute>();
		for (Attribute a: exampleSet.getAttributes())
			oldAttributes.add(a);

		newQuadraticAttributeList  = new ArrayList<Attribute>();
		currentSplit = new SplittedExampleSet(exampleSet, new Partition(new int[exampleSet.size()],2));

		allPartitionsForAttribute = new LinkedList<Partition>();
		splitValueList = new LinkedList<Double>();
		currentAttribute = null;




		try {
			quadraticDiscriminantAnalysis = new QuadraticDiscriminantAnalysis(OperatorService.getOperatorDescription(OperatorService.getOperatorKeys().toArray()[0].toString()));

			ExampleSet esForLearning = PruningTools.deepCopy(exampleSet);
			for (Iterator<Attribute> ia = esForLearning.getAttributes().iterator(); ia.hasNext(); ){
				Attribute a = ia.next();
				if (a.getName().contains("Quadratic") || !attributesForSplitting.contains(a)){
					ia.remove();
					esForLearning.getExampleTable().removeAttribute(a);
				}

			}

			model = (DiscriminantModel) quadraticDiscriminantAnalysis.learn(esForLearning);
			labels = model.getlabels();


			for (String label : labels) {
				Attribute newAttribute = AttributeFactory.createAttribute("newQuadraticAttribute_" + newQuadraticAttributeIndex++, Ontology.NUMERICAL);
				exampleSet.getExampleTable().addAttribute(newAttribute);
				exampleSet.getAttributes().addRegular(newAttribute);
				newQuadraticAttributeList.add(newAttribute);
			}


			for (int i=0; i<exampleSet.size(); i++){
				double[] newQuadraticAttributesValues = model.newLinearAttributes(esForLearning.getExample(i));

				for (int j = 0; j < newQuadraticAttributeList.size(); j++) {
					exampleSet.getExample(i).setValue(newQuadraticAttributeList.get(j), newQuadraticAttributesValues[j]);
				}
			}


			this.attributesForSplitting=newQuadraticAttributeList;
			iteratorAtt = this.attributesForSplitting.listIterator();

		} catch (OperatorException e) {
			//e.printStackTrace();
			System.out.println("Ne interesuje me sto nemas opis");
		}
	}


	public QuadraticDiscriminantSpllit(List<SubproblemParameter> parameters) {
		super(parameters);
	}

	@Override
	public SplittedExampleSet nextSplit() throws Exception {
		if ( !hasNextSplit()){
			return null;
		}

		if (allPartitionsForAttribute.isEmpty()){

			currentAttribute = iteratorAtt.next();
			while (!currentAttribute.isNumerical()){
				currentAttribute = iteratorAtt.next();
			}
			getPartitionsForAttribute(currentSplit, currentAttribute);
			Split(currentSplit, currentAttribute);

		}else
			Split(currentSplit, currentAttribute);

		return currentSplit;
	}

	@Override
	public boolean hasNextSplit() {
		// if there are more possible splits in generated partitions for an attribute
		if (!allPartitionsForAttribute.isEmpty())
			return true;

		// if there are more numerical attributes
		ListIterator<Attribute> i = attributesForSplitting.listIterator(iteratorAtt.nextIndex());

		while (i.hasNext()){
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
		return new String[] {}; // proveri ovo
	}

	@Override
	public String[] getExclusiveClassNames() {
		return new String[] {};// proveri ovo
	}



	/**
	 * Creates binary numerical splitting candidate for evaluation.
	 * 
	 * @param currentSplit - dataset in current node that will be splitted
	 * @param attribute - attribute for splitting
	 */
	private void Split(SplittedExampleSet currentSplit, Attribute attribute)
	{  
		currentSplit.setPartition(allPartitionsForAttribute.removeFirst());
		currentSplit.setAttribute(attribute);
		currentSplit.setSplitValue(splitValueList.removeFirst().doubleValue());
	}

	/**
	 * Creates all possible partitions for current numerical attribute
	 * 
	 * @param inputSet  - dataset in current node
	 * @param attribute - the attribute for splitting
	 * 
	 */
	private  void getPartitionsForAttribute(ExampleSet inputSet, Attribute attribute) {
		SortedExampleSet exampleSet = new SortedExampleSet((ExampleSet)inputSet, attribute, SortedExampleSet.INCREASING);
		Attribute labelAttribute = exampleSet.getAttributes().getLabel();
		double oldLabel = Double.NaN;
		double lastValue = Double.NaN;;
		int counter = -1;


		for (Example e : exampleSet) {
			counter++;
			double currentValue = e.getValue(attribute);
			double label = e.getValue(labelAttribute);          
			if(Double.isNaN(lastValue))
				lastValue=currentValue;
			if ((Double.isNaN(oldLabel)) || ((oldLabel != label) && (lastValue != currentValue))) {
				double splitValue = (lastValue + currentValue) / 2.0d;
				Partition partition = splitByAttribute(inputSet, attribute, splitValue);
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
	 * @param exampleSet - example set in current node
	 * @param attribute - current numerical attribute for candidate split creation
	 * @param value - split point value
	 * 
	 * @return Partition - one partition for attribute for a given split point
	 */
	private Partition splitByAttribute(ExampleSet exampleSet, Attribute attribute, double value) {
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
