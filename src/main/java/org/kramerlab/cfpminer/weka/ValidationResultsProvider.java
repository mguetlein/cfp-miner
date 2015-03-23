package org.kramerlab.cfpminer.weka;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetBoxPlot;
import org.mg.javalib.util.SwingUtil;
import org.mg.javalib.weka.MergeArffFiles;

import weka.core.Instances;

public class ValidationResultsProvider
{
	ResultSet allResults;
	ResultSet results;

	HashMap<String, String> wekaAttributes = null;

	public HashMap<String, String> getWekaAttributes()
	{
		if (wekaAttributes == null)
		{
			//default
			wekaAttributes = new LinkedHashMap<>();
			wekaAttributes.put("Area_under_ROC", "AUC");
			wekaAttributes.put("Percent_correct", "Accuracy");
			wekaAttributes.put("True_positive_rate", "Sensitivity");
			wekaAttributes.put("IR_precision", "Selectivity");
			wekaAttributes.put("True_negative_rate", "Specificity");
		}
		return wekaAttributes;
	}

	public ResultSet getResults()
	{
		if (results == null)
		{
			//			for (String p : allResults.getProperties())
			//				System.err.println(p);
			results = allResults.copy(getWekaAttributes().keySet());
			if (getWekaAttributes().containsKey("Percent_correct"))
				for (int i = 0; i < results.getNumResults(); i++)
				{
					results.setResultValue(i, "Percent_correct",
							((Double) results.getResultValue(i, "Percent_correct")) * 0.01);
				}
			results.sortProperties(new ArrayList<String>(getWekaAttributes().keySet()));
			for (String p : getWekaAttributes().keySet())
				results.setNicePropery(p, getWekaAttributes().get(p));

		}
		return results;
	}

	public ResultSet getJoinedResults()
	{
		return getResults().join(null);
	}

	public void plot(String valPng)
	{
		ResultSetBoxPlot plot = new ResultSetBoxPlot(getResults(), "", "", (String) null, new ArrayList<String>(
				getWekaAttributes().keySet()));
		plot.setHideMean(true);
		plot.printResultsPerPlot(false);
		if (valPng != null)
			plot.ToPNGFile(valPng, new Dimension(500, 150));
		else
			SwingUtil.showInDialog(plot.getChart());
	}

	public ValidationResultsProvider(String... arffResultFiles) throws FileNotFoundException, IOException
	{
		File arff;
		if (arffResultFiles.length > 1)
		{
			arff = File.createTempFile("result", "arff");
			arff.deleteOnExit();
			MergeArffFiles.merge(arffResultFiles, arff.getAbsolutePath());
		}
		else
			arff = new File(arffResultFiles[0]);
		allResults = ResultSet.fromWekaDataset(new Instances(new FileReader(arff)));
	}

	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		ValidationResultsProvider res = new ValidationResultsProvider(
				"/home/martin/workspace/CFPMiner/results/r3_ecfp_fold_8192_RaF_ChEMBL_87.arff");
		System.out.println(res.getResults().toNiceString());
		System.out.println(res.getJoinedResults().toNiceString());
		res.plot(null);
		System.exit(0);
	}

}
