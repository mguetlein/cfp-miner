package org.kramerlab.cfpminer.weka;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.kramerlab.cfpminer.CFPtoArff;
import org.mg.cdklib.cfp.CFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.attribute_evaluation.AttributeCrossvalidator;

public class CFPValidate
{
	public static void main(String[] args) throws Exception
	{
		Locale.setDefault(Locale.US);

		//		printCollisions();

		Options options = new Options();
		options.addOption("d", "datasetName", true, "");
		options.addOption("r", "run", true, "cv run");
		options.addOption("t", "type", true, "ecfp|fcfp");

		options.addOption("c", "classifier", true, "RaF|SMO|NBy|Ens|RnF");
		options.addOption("f", "featureSelection", true, "fold|filt|none");
		options.addOption("s", "hashfoldsize", true, "default:1024, requires folded=true");

		options.addOption(
				"x",
				"no-decoy-resample",
				false,
				"if param is given, resampling decoys for ChEMBL and MUV datasets will be disabled (and the same sample will be used for runs>1 as for run 1)");

		if (args.length == 0)
		{
			System.err.println(options);
			System.exit(1);
		}
		boolean validate = true;
		if (args.length > 0 && args[0].equals("debug"))
		{
			//			for (String name : names)
			//			{
			//				main(("--datasetName " + name + " --classifier RandomForest --featureSelection filter").split(" "));
			//			}

			// ChEMBL_10188
			// ChEMBL_259
			// DUD_vegfr2
			// CPDBAS_Mutagenicity
			// AMES
			args = "--datasetName ChEMBL_259 --run 1 --classifier RnF --type ecfp4 --featureSelection filt --hashfoldsize 1024"
					.split(" ");
			validate = true;
			//
			//			args = "--datasetName DUD_vegfr2 --classifier RandomForest --folded false --pValueThreshold 0.05,0.1"
			//					.split(" ");
			//			args = "--datasetName ChEMBL_10188 --classifier RandomForest --featureSelection filter".split(" ");
			//			args = ("--datasetName CPDBAS_Dog_Primates --runs 3 --classifier RandomForest,SMO "
			//					+ "--type ecfp,fcfp --folded true,false --hashfoldsize 1024,4096 "
			//					+ "--pValueThreshold 0.05,0.1 --relMinFreq 0.02,0.01").split(" ");
		}

		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		String datasetName = cmd.getOptionValue("d");
		int run = 1;
		if (cmd.hasOption("r"))
			run = Integer.parseInt(cmd.getOptionValue("r"));
		CFPType type = CFPType.ecfp6;
		if (cmd.hasOption("t"))
			type = CFPType.valueOf(cmd.getOptionValue("t"));
		FeatureSelection featureSelection = FeatureSelection.filt;
		if (cmd.hasOption("f"))
			featureSelection = FeatureSelection.valueOf(cmd.getOptionValue("f"));
		int hashfoldsize = 0;
		if (cmd.hasOption("s"))
			hashfoldsize = Integer.parseInt(cmd.getOptionValue("s"));
		String classifier = "RnF";
		if (cmd.hasOption("c"))
			classifier = cmd.getOptionValue("c");

		if (hashfoldsize < 512 && featureSelection != FeatureSelection.none)
			throw new IllegalArgumentException("please set hashfoldsize");

		DataLoader loader = new DataLoader("data");
		if (cmd.hasOption("x"))
			loader.setResampleDecoys(false);
		CDKDataset dataset = loader.getDataset(datasetName, run);
		List<String> list = dataset.getSmiles();
		List<String> endpointValues = dataset.getEndpoints();
		ListUtil.scramble(new Random(1), list, endpointValues);

		CFPMiner cfps = new CFPMiner(ListUtil.cast(String.class, endpointValues));
		cfps.setType(type);
		cfps.setFeatureSelection(featureSelection);
		cfps.setHashfoldsize(hashfoldsize);
		cfps.mine(list);
		System.out.println(cfps);

		if (validate)
		{
			String outfile = "results/"
					+ resultFileName(run, type, featureSelection, hashfoldsize, classifier, datasetName);
			validate(datasetName, run, outfile, new String[] { classifier }, endpointValues, new CFPMiner[] { cfps });
		}
		else if (featureSelection == FeatureSelection.filt)
		{
			cfps.applyFilter();
			System.out.println(cfps);
			CFPtoArff.writeTrainingDataset("/tmp/test.arff", cfps, datasetName);
		}
	}

	public static void validate(String datasetName, int run, String outfile, String classifiers[], List<?> endpoints,
			CFPMiner... ecfps) throws Exception
	{
		CFPMinerAttributes[] apCFP = new CFPMinerAttributes[ecfps.length];
		for (int i = 0; i < apCFP.length; i++)
			apCFP[i] = new CFPMinerAttributes(ecfps[i]);
		AttributeCrossvalidator cv = new AttributeCrossvalidator(datasetName, ListUtil.cast(String.class, endpoints),
				apCFP);
		cv.setOutfile(outfile);
		cv.setRun(run);
		cv.setClassifiers(classifiers);
		cv.run();
	}

	public static String resultFileName(int run, CFPType type, FeatureSelection featureSelection, int hashfoldsize,
			String classifier, String datasetName)
	{
		return "r" + String.format("%02d", run) + "_"
				+ resultFileName(type, featureSelection, hashfoldsize, classifier, datasetName);
	}

	public static String resultFileName(CFPType type, FeatureSelection featureSelection, int hashfoldsize,
			String classifier, String datasetName)
	{
		return type + "_" + featureSelection + "_" + hashfoldsize + "_" + classifier + "_" + datasetName + ".arff";
	}

}
