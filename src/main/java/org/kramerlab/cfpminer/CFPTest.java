package org.kramerlab.cfpminer;

import java.util.List;
import java.util.Random;

import org.kramerlab.cfpminer.weka.CFPValidate;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.attribute_evaluation.AttributeCrossvalidator;

public class CFPTest
{

	public static void amesRuntimeTest() throws Exception
	{
		AttributeCrossvalidator.RUNTIME_DEBUG = true;

		String datasetName = "AMES";
		CFPType type = CFPType.ecfp6;
		FeatureSelection featureSelection = FeatureSelection.filt;
		int hashfoldsize = 1024;

		CDKDataset dataset = new DataLoader("data").getDataset(datasetName);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		cfps.setHashfoldsize(hashfoldsize);
		cfps.mine(list);

		String classifier = "SMO";
		for (int i : new int[] { 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192 })
		{
			cfps.setHashfoldsize(i);
			CFPValidate.validate(datasetName, 1, "/dev/null", new String[] { classifier },
					endpointValues, new CFPMiner[] { cfps });
		}

		System.exit(0);
	}

	public static void demo() throws Exception
	{
		String datasetName = "CPDBAS_MultiCellCall";
		CFPType type = CFPType.fcfp4;
		FeatureSelection featureSelection = FeatureSelection.filt;
		//		int hashfoldsize = 1024;

		CDKDataset dataset = new DataLoader("data").getDataset(datasetName);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		//		cfps.hashfoldsize = hashfoldsize;
		cfps.mine(list);

		for (String classifier : new String[] { "SMO", "RaF" })
		{
			for (int i : new int[] { classifier.equals("RaF") ? 1024 : 1024 })
			{
				Boolean b = null;
				//				for (Boolean b : new boolean[] { true, false })
				//				{
				//					AttributeCrossvalidator.FORCE_SPARSE = b;
				cfps.setHashfoldsize(i);
				CFPValidate.validate(datasetName, 1, "/tmp/" + classifier + "_" + b + "_mult.arff",
						new String[] { classifier }, endpointValues, new CFPMiner[] { cfps });
				//				}
			}
		}

		System.exit(0);
	}

	public static void main(String[] args) throws Exception
	{
		demo();

		//		//		//demo();
		//		String datasetName = "AMES";
		//		CFPType type = CFPType.ecfp4;
		//		FeatureSelection featureSelection = FeatureSelection.none;
		//		int hashfoldsize = 1024;
		//		String classifier = "RaF";
		//		//
		//		estimateRuntime(datasetName, type, featureSelection, hashfoldsize, classifier);
	}
}
