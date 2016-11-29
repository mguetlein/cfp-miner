package org.kramerlab.cfpminer.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kramerlab.cfpminer.weka.eval2.CFPtoArff;
import org.mg.cdklib.CDKConverter;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.data.WekaInstancesDataSet;
import org.mg.wekalib.eval2.model.RandomForestModel;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.Predictions;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class PubChemDiscriminationClassifier
{
	String pubchemSample;

	int n;

	CFPMiner miner;

	Instances trainingData;

	Classifier classifier;

	public PubChemDiscriminationClassifier(int n)
	{
		this.n = n;
	}

	public void build(List<String> smiles) throws Exception
	{
		String pubchemSample = System.getProperty("user.home") + "/data/pubchem/pubchem_sample_" + n
				+ ".smi";

		CDKDataset pubchem = DataLoader.getDatasetFromSMILES("pubchem", pubchemSample);
		List<String> allSmiles = new ArrayList<>();
		List<String> endpoints = new ArrayList<>();

		Set<String> uniqSet = new HashSet<>();
		for (String smi : smiles)
			uniqSet.add(CDKConverter.toAbsoluteSmiles(smi));

		for (String smi : pubchem.getSmiles())
		{
			String uniq = CDKConverter.toAbsoluteSmiles(smi);
			if (!uniqSet.contains(uniq))
			{
				uniqSet.add(uniq);
				allSmiles.add(smi);
				endpoints.add("pubchem");
			}
			else
				System.err.println("skipping a pubchem compound " + smi);
		}
		for (String smi : smiles)
		{
			allSmiles.add(smi);
			endpoints.add("this-dataset");
		}

		System.out
				.println("building pubchem disc > mining (on " + allSmiles.size() + " compounds)");

		miner = new CFPMiner(endpoints);
		miner.setType(CFPType.ecfp4);
		miner.setHashfoldsize(2048);
		miner.setFeatureSelection(FeatureSelection.filt);
		miner.mine(allSmiles);
		miner.applyFilter();
		//		System.out.println(miner);

		System.out.println("building pubchem disc > converting to instances");

		trainingData = CFPtoArff.getTrainingDataset(miner, "discriminate-from-pubchem");

		System.out.println("building pubchem disc > building random forest (instances: "
				+ trainingData.numInstances() + ", features: " + trainingData.numAttributes()
				+ ")");

		classifier = new RandomForest();
		classifier.buildClassifier(trainingData);

		System.out.println("building pubchem disc > building done");
	}

	/**
	 * @param smiles
	 * @return probability (0-1) of belonging to this dataset
	 * @throws Exception 
	 */
	public double predict(String smiles) throws Exception
	{
		Instances test = CFPtoArff.getTestDataset(miner, "discriminate-from-pubchem",
				CDKConverter.parseSmiles(smiles));
		return classifier.distributionForInstance(test.firstInstance())[1];
	}

	void eval() throws Exception
	{
		CV cv = new CV();
		cv.setModel(new RandomForestModel());
		cv.setDataSet(new WekaInstancesDataSet(trainingData, 1));
		if (!cv.isDone())
			cv.runSequentially();
		Predictions p = cv.getResult();
		System.out.println(PredictionUtil.summaryClassification(p));
	}

	public static void main(String[] args) throws Exception
	{
		PubChemDiscriminationClassifier disc = new PubChemDiscriminationClassifier(1000);
		CDKDataset data = DataLoader.INSTANCE.getDataset("NCTRER");
		disc.build(data.getSmiles());
		System.out.println(disc.predict("c1cscc1"));
		System.out.println(disc.predict("CN(C)CCN(Cc1cccs1)c1ccccn1"));
		System.out.println(disc.predict("[Cl-].CCCCCCCCCCCCC[N+](C)(C)Cc1ccccc1"));
		System.out.println(disc.predict("C[Si](O[Si](Cc1ccccc1)(C)C)(Cc1ccccc1)C"));
		//FileUtil.writeStringToFile("/tmp/bla.arff", disc.trainingData.toString());
	}
}
