package org.kramerlab.cfpminer.weka;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;

import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.StringUtil;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.RandomForest;
import weka.core.AdditionalMeasureProducer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.converters.ArffSaver;
import weka.experiment.ClassifierSplitEvaluator;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.Experiment;
import weka.experiment.InstancesResultListener;
import weka.experiment.PropertyNode;
import weka.experiment.RegressionSplitEvaluator;
import weka.experiment.SplitEvaluator;

public class AttributeCrossvalidator
{
	public static boolean RUNTIME_DEBUG = false;
	public static Boolean FORCE_SPARSE = null;

	public static interface AttributeProvider extends Serializable
	{
		public String getName();

		public int getNumAttributes();

		public String getAttributeName(int a);

		public List<String> getAttributeDomain(int a);

		public double getAttributeValue(int i, int a);

		public void applyFilter(Set<Integer> filterSubset);
	}

	private static int maxBuildCount = -1;
	private static HashMap<Thread, String> currentDatasetName = new HashMap<Thread, String>();
	private static HashMap<Thread, Integer> currentRun = new HashMap<Thread, Integer>();
	private static HashMap<Thread, Integer> currentBuildCount = new HashMap<Thread, Integer>();

	public static void tick(String info)
	{
		currentBuildCount.put(Thread.currentThread(), currentBuildCount.get(Thread.currentThread()) + 1);
		System.err.println(new SimpleDateFormat("HH:mm").format(new Date()) + " "
				+ StringUtil.concatWhitespace(currentBuildCount.get(Thread.currentThread()) + "", 3, false) + "/"
				+ StringUtil.concatWhitespace(maxBuildCount + "", 3, false) + " "
				+ currentRun.get(Thread.currentThread()) + " " + currentDatasetName.get(Thread.currentThread()) + " "
				+ info);
	}

	private static class MyClassifier extends AbstractClassifier implements Serializable, AdditionalMeasureProducer
	{
		Classifier classifier;
		AttributeProvider attributeProvider;
		boolean sparse = false;

		private final static long serialVersionUID = 4L;

		@Override
		public String[] getOptions()
		{
			return new String[] { classifier.getClass().getSimpleName(), attributeProvider.getName() };
		}

		public MyClassifier(Classifier classifier, AttributeProvider attributeProvider)
		{
			this.classifier = classifier;
			if (FORCE_SPARSE != null)
				sparse = FORCE_SPARSE;
			else if (classifier instanceof SMO || classifier instanceof Vote)
				sparse = true;
			this.attributeProvider = attributeProvider;
		}

		Instances data;

		private Instances getData(Attribute classAttribute)
		{
			if (data == null)
			{
				ArrayList<Attribute> a = new ArrayList<Attribute>();
				for (int i = 0; i < attributeProvider.getNumAttributes(); i++)
					a.add(new Attribute(attributeProvider.getAttributeName(i), attributeProvider.getAttributeDomain(i)));
				a.add(classAttribute);
				data = new Instances("pred", a, 1);
				data.setClassIndex(a.size() - 1);
			}
			return new Instances(data);
		}

		private void copyInstance(Instances newData, Instance oldInstance)
		{
			int idx = (int) oldInstance.value(0);
			double endpoint = oldInstance.value(1);
			double vals[] = new double[newData.numAttributes()];
			for (int i = 0; i < attributeProvider.getNumAttributes(); i++)
				vals[i] = attributeProvider.getAttributeValue(idx, i);
			vals[newData.numAttributes() - 1] = endpoint;
			Instance inst;
			if (sparse)
				inst = new SparseInstance(1.0, vals);
			else
				inst = new DenseInstance(1.0, vals);
			inst.setDataset(newData);
			newData.add(inst);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Enumeration enumerateMeasures()
		{
			Vector newVector = new Vector();
			newVector.addElement("measureNumAttributesSelected");
			if (classifier instanceof AdditionalMeasureProducer)
			{
				Enumeration en = ((AdditionalMeasureProducer) classifier).enumerateMeasures();
				while (en.hasMoreElements())
					newVector.addElement((String) en.nextElement());
			}
			return newVector.elements();
		}

		public double getMeasure(String additionalMeasureName)
		{
			if (additionalMeasureName.compareToIgnoreCase("measureNumAttributesSelected") == 0)
				return attributeProvider.getNumAttributes();
			else if (classifier instanceof AdditionalMeasureProducer)
				return ((AdditionalMeasureProducer) classifier).getMeasure(additionalMeasureName);
			else
				throw new IllegalArgumentException(additionalMeasureName + " not supported (MyClassifier)");
		}

		@Override
		public void buildClassifier(Instances oldData) throws Exception
		{
			if (RUNTIME_DEBUG && currentBuildCount.get(Thread.currentThread()) > 0)
				return;

			//			System.out.println(oldData);
			// apply filter based on list of indices in the training dataset
			HashSet<Integer> filterSubset = new HashSet<Integer>();
			for (Instance oldInstance : oldData)
				filterSubset.add((int) oldInstance.value(0));
			attributeProvider.applyFilter(filterSubset);

			AttributeCrossvalidator.tick(attributeProvider.getName() + " #" + attributeProvider.getNumAttributes()
					+ " " + classifier.getClass().getSimpleName());

			// add feature values to instance data
			Instances newData = getData(oldData.attribute(1));
			for (Instance oldInstance : oldData)
				copyInstance(newData, oldInstance);

			long start = System.currentTimeMillis();

			//			{
			//				ArffSaver saver = new ArffSaver();
			//				saver.setInstances(newData);
			//				saver.setFile(new File("/tmp/test.arff"));
			//				saver.writeBatch();
			//			}

			classifier.buildClassifier(newData);
			if (RUNTIME_DEBUG)
				System.err.println("build time: " + (System.currentTimeMillis() - start) / 1000.0);
		}

		@Override
		public double[] distributionForInstance(Instance oldInstance) throws Exception
		{
			if (RUNTIME_DEBUG && currentBuildCount.get(Thread.currentThread()) > 0)
				return new double[] { 0.0, 0.0 };

			// add feature values to instance data
			Instances newData = getData(null);
			copyInstance(newData, oldInstance);
			return classifier.distributionForInstance(newData.firstInstance());
		}
	}

	String datasetName;
	String outfile;
	List<String> endpointValues;
	Classifier classifiers[] = new Classifier[] { new RandomForest(), new SMO() };
	AttributeProvider[] provider;
	int run = 1;

	public AttributeCrossvalidator(String datasetName, List<String> endpointValues, AttributeProvider... provider)
	{
		this.datasetName = datasetName;
		this.endpointValues = endpointValues;
		this.provider = provider;

		currentDatasetName.put(Thread.currentThread(), datasetName);
		currentBuildCount.put(Thread.currentThread(), 0);
	}

	public void setOutfile(String outfile)
	{
		this.outfile = outfile;
	}

	public void setRun(int run)
	{
		this.run = run;
		currentRun.put(Thread.currentThread(), run);
	}

	public void setClassifiers(String[] c)
	{
		this.classifiers = new Classifier[c.length];
		int idx = 0;
		for (String cl : c)
		{
			if (cl.equals("RaF"))
				throw new IllegalArgumentException("random forest v3.7.12 now called RnF");
			else if (cl.equals("RnF"))
				this.classifiers[idx++] = new RandomForest();
			else if (cl.equals("SMO"))
				this.classifiers[idx++] = new SMO();
			else if (cl.equals("NBy"))
				this.classifiers[idx++] = new NaiveBayes();
			else if (cl.equals("Ens"))
			{
				Vote ens = new Vote();
				ens.setClassifiers(new Classifier[] { new RandomForest(), new SMO(), new NaiveBayes() });
				this.classifiers[idx++] = ens;
			}
			//			else if (cl.equals("LibSVM"))
			//				this.classifiers[idx++] = new LibSVM();
			else
				throw new IllegalArgumentException(cl);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() throws Exception
	{
		String instancesFile = "/tmp/instances." + datasetName + ".arff";
		if (!new File(instancesFile).exists())
		{
			ArrayList<Attribute> a = new ArrayList<Attribute>();
			a.add(new Attribute("index"));
			a.add(new Attribute("endpoints", new ArrayList<String>(new HashSet<String>(endpointValues))));
			Instances data = new Instances(datasetName, a, endpointValues.size());
			int idx = 0;
			for (String e : endpointValues)
			{
				DenseInstance inst = new DenseInstance(2);
				inst.setDataset(data);
				inst.setValue(0, idx++);
				inst.setValue(1, e);
				data.add(inst);
			}
			//			System.out.println(data);
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(instancesFile));
			saver.writeBatch();
			if (!new File(instancesFile).exists())
				throw new IllegalStateException();
			System.err.println("created " + instancesFile);
		}

		//		String runStr = "_r" + minRunIndex + "-" + inclusiveMaxRunIndex;
		boolean classification = true;

		Experiment exp = new Experiment();
		exp.setPropertyArray(new Classifier[0]);
		exp.setUsePropertyIterator(true);

		SplitEvaluator se = null;
		Classifier sec = null;

		if (classification)
		{
			se = new ClassifierSplitEvaluator();
			sec = ((ClassifierSplitEvaluator) se).getClassifier();
		}
		else
		{
			se = new RegressionSplitEvaluator();
			sec = ((RegressionSplitEvaluator) se).getClassifier();
		}

		int numFolds = 10;
		CrossValidationResultProducer cvrp = new CrossValidationResultProducer();
		cvrp.setNumFolds(numFolds);
		cvrp.setSplitEvaluator(se);

		PropertyNode[] propertyPath = new PropertyNode[2];
		try
		{
			propertyPath[0] = new PropertyNode(se, new PropertyDescriptor("splitEvaluator",
					CrossValidationResultProducer.class), CrossValidationResultProducer.class);
			propertyPath[1] = new PropertyNode(sec, new PropertyDescriptor("classifier", se.getClass()), se.getClass());
		}
		catch (IntrospectionException e)
		{
			e.printStackTrace();
		}

		exp.setResultProducer(cvrp);
		exp.setPropertyPath(propertyPath);

		exp.setRunLower(run);
		exp.setRunUpper(run);

		maxBuildCount = numFolds * classifiers.length * provider.length;

		Classifier classi[] = new Classifier[classifiers.length * provider.length];
		int idx = 0;
		for (int i = 0; i < classifiers.length; i++)
		{
			for (int j = 0; j < provider.length; j++)
			{
				classi[idx++] = new MyClassifier(classifiers[i], provider[j]);
			}
		}
		exp.setPropertyArray(classi);

		DefaultListModel model = new DefaultListModel();
		model.addElement(new File(instancesFile));
		exp.setDatasets(model);

		InstancesResultListener irl = new InstancesResultListener();
		File resultsFile;
		if (outfile == null)
			resultsFile = File.createTempFile("results", "arff");
		else
			resultsFile = new File(outfile);
		irl.setOutputFile(resultsFile);
		exp.setResultListener(irl);

		// 2. run experiment
		System.out.println("Initializing...");
		exp.initialize();
		System.out.println("Running...");
		exp.runExperiment();
		System.out.println("Finishing...");
		exp.postProcess();

		System.out.println("results printed to " + resultsFile);
	}

	public static void main(String[] args) throws Exception
	{
		final Random r = new Random();
		new AttributeCrossvalidator("dummy", ArrayUtil.toList("a,a,a,a,a,b,b,b,b,b".split(",")),
				new AttributeProvider()
				{
					@Override
					public String getName()
					{
						return "dummy";
					}

					@Override
					public int getNumAttributes()
					{
						return 2;
					}

					@Override
					public String getAttributeName(int a)
					{
						return new String[] { "attribute1", "attribute2" }[a];
					}

					@Override
					public List<String> getAttributeDomain(int a)
					{
						return ArrayUtil.toList(new String[] { "ene", "mene" });
					}

					@Override
					public double getAttributeValue(int i, int a)
					{
						return r.nextBoolean() ? 0.0 : 1.0;
					}

					@Override
					public void applyFilter(Set<Integer> filterSubset)
					{
					}
				});
	}

}
