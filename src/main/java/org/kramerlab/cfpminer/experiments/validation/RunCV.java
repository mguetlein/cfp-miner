package org.kramerlab.cfpminer.experiments.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.kramerlab.cfpminer.experiments.PaperResults;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.javalib.util.SwingUtil;
import org.mg.wekalib.eval2.job.Printer;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.persistance.BlockerImpl;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;
import org.mg.wekalib.evaluation.PredictionUtil.ClassificationMeasure;

public class RunCV
{
	static void initDB(boolean blockEnabled, boolean outdirEnabled, String outfilePrefix)
	{
		DB.init(new ResultProviderImpl(PaperResults.RESULTS + "/jobs/store",
				PaperResults.RESULTS + "/jobs/tmp"),
				blockEnabled ? new BlockerImpl(PaperResults.RESULTS + "/jobs/block") : null);
		if (outdirEnabled)
			Printer.setOutfile(PaperResults.RESULTS + "/jobs/out", outfilePrefix);
	}

	public static void main(String[] args) throws Exception
	{
		boolean debug = false;
		if (args.length == 0)
		{
			debug = true;
			//			//			String dataset = "-d CPDBAS_SingleCellCall,MUV_858,MUV_692,NCTRER,ChEMBL_126,ChEMBL_19905,ChEMBL_15,DUD_hivrt ";
			//			//			String size = "-s 1024,2048,4096,8192 ";
			//			String dataset = "";//"-d ChEMBL_126 ";
			//			String size = "";//"-s 1024,2048,4096 ";
			//			args = (size + dataset + "-o ridgeEval -f filt").split(" ");
			//			DB.init(new ResultProviderImpl("jobs/store", "jobs/tmp"), null);

			//			args = ("-s 1024 -d NCTRER -o compareCFPs -f filt,none -t ecfp4 -r 1 -c 0").split(" ");

			args = ("-s 8192 -d MUV_733 -o paramOptimize -v -f filt -t ecfp4 -r 1 -c 0,1")
					.split(" ");
			DB.init(new ResultProviderImpl("/tmp/jobs/store", "/tmp/jobs/tmp"), null);
		}
		else
		{
			initDB(true, true, ArrayUtil.toString(args, "_", "", "", "").replaceAll(",", "#"));
		}
		Options opt = new Options();
		opt.addOption("s", true, "sizes, comma seperated");
		opt.addOption("d", true, "datasets, comma seperated");
		opt.addOption("c", true, "classifier index 0-12(optimize) 0-2(default), comma seperated");
		opt.addOption("v", false,
				"v given: validate (requires o:paramOptimize), v not given: select");
		opt.addOption("f", true, "features selection methods, comma seperated");
		opt.addOption("o", true, "o: paramOptimize|compareCFPs|ridgeEval, default: compareCFPs");
		opt.addOption("t", true, "type, comma seperated");
		opt.addOption("r", true, "num-repetitions, default is 3");

		CommandLine cmd = new DefaultParser().parse(opt, args);
		Integer sizes[] = null;
		String datasets[] = null;
		Integer classifiers[] = null;
		boolean validate = false;
		String feats[] = null;
		String option = null;
		String types[] = null;
		Integer repetitions = null;
		if (cmd.hasOption("s"))
			sizes = ArrayUtil.parseIntegers(cmd.getOptionValue("s").split(","));
		if (cmd.hasOption("d"))
			datasets = cmd.getOptionValue("d").split(",");
		if (cmd.hasOption("c"))
			classifiers = ArrayUtil.parseIntegers(cmd.getOptionValue("c").split(","));
		if (cmd.hasOption("v"))
			validate = true;
		if (cmd.hasOption("f"))
			feats = cmd.getOptionValue("f").split(",");
		if (cmd.hasOption("o"))
			option = cmd.getOptionValue("o");
		if (cmd.hasOption("t"))
			types = cmd.getOptionValue("t").split(",");
		if (cmd.hasOption("r"))
			repetitions = Integer.parseInt(cmd.getOptionValue("r"));

		CFPCrossValidation cv;
		if (option == null || option.equals("compareCFPs"))
			cv = CFPCrossValidation.compareCFPs();
		else if (option.equals("paramOptimize"))
			cv = CFPCrossValidation.paramOptimize();
		else if (option.equals("ridgeEval"))
			cv = CFPCrossValidation.ridgeEval();
		else
			throw new IllegalArgumentException(
					"invalid o: '" + option + "', not within paramOptimize|compareCFPs|ridgeEval");

		if (sizes != null)
			cv.sizes = ArrayUtil.toList(sizes);
		if (datasets != null)
			cv.datasets = ArrayUtil.toList(datasets);
		if (classifiers != null)
		{
			List<Model> selectedClassifiers = new ArrayList<>();
			for (Integer c : classifiers)
				selectedClassifiers.add(cv.classifiers.get(c));
			cv.classifiers = selectedClassifiers;
		}
		if (feats != null)
		{
			cv.featureSelections.clear();
			for (String f : feats)
				cv.featureSelections.add(FeatureSelection.valueOf(f));
		}
		if (types != null)
		{
			cv.types.clear();
			for (String t : types)
				cv.types.add(CFPType.valueOf(t));
		}
		if (repetitions != null)
			cv.numCVRepetitions = repetitions;

		if (validate)
		{
			if (option == null || !option.equals("paramOptimize"))
				throw new IllegalArgumentException();
			cv.jobValidateModel().runSequentially();
		}
		else
			cv.jobSelectModel().runSequentially();

		if (debug)
		{
			//			ResultSet rs = cv.selectModelResultsPerRepetition(ClassificationMeasure.values(), false,
			//					true);

			for (String dataset : new ArrayList<>(cv.datasets))
			{
				cv.datasets = ListUtil.createList(dataset);
				ResultSet rs = cv.selectModelResults(ClassificationMeasure.values(), false);
				System.out.println(rs.toNiceString());
				ResultSetBoxPlot bx = new ResultSetBoxPlot(rs, dataset, "performance", "AlgParams",
						ListUtil.createList("AUC", "AUPRC"));
				bx.setHideMean(true);
				bx.setPrintMeanAndStdev(true);
				//			ResultSetBoxPlot bx = new ResultSetBoxPlot(rs, "title", "performance", "AlgParams",
				//					"HashfoldSize", "AUC");
				SwingUtil.showInDialog(bx.getChart());
			}
		}

		System.exit(0);
	}
}
