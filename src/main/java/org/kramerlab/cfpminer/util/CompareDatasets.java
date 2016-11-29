package org.kramerlab.cfpminer.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.kramerlab.cfpminer.appdomain.ADModel;
import org.kramerlab.cfpminer.appdomain.ADPrediction;
import org.kramerlab.cfpminer.appdomain.KNNTanimotoCFPAppDomainModel;
import org.mg.cdklib.cfp.BasicCFPMiner;
import org.mg.cdklib.cfp.CFPType;
import org.mg.cdklib.cfp.FeatureSelection;
import org.mg.cdklib.data.CDKDataset;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.SetUtil;
import org.mg.javalib.util.StringUtil;

public class CompareDatasets
{
	CDKDataset data[];

	public CompareDatasets(CDKDataset... data)
	{
		this.data = data;
	}

	public void singleAnalysis() throws Exception
	{
		ResultSet rs = new ResultSet();
		for (CDKDataset d : data)
		{
			System.out.println(d.getDatasetName());

			int idx = rs.addResult();
			rs.setResultValue(idx, "Dataset", d.getDatasetName());
			rs.setResultValue(idx, "size", d.getSmiles().size());

			DescriptiveStatistics stats = new DescriptiveStatistics();
			for (int i = 0; i < d.getSmiles().size() - 1; i++)
			{
				if (i % 500 == 0)
					System.out.println("compute overall dist: " + i);
				for (int j = i + 1; j < d.getSmiles().size(); j++)
				{
					stats.addValue(1.0 - ((KNNTanimotoCFPAppDomainModel) getModel(d)).getCFPMiner()
							.getTanimotoSimilarity(i, j));
				}
			}
			String v = StringUtil.formatDouble(stats.getMean()) + " \u00B1 "
					+ StringUtil.formatDouble(stats.getStandardDeviation());
			rs.setResultValue(idx, "Tanimoto distance", v);

			stats = new DescriptiveStatistics();
			for (String smiles : d.getSmiles())
				stats.addValue(getModel(d).getDistanceWithoutIdenticalSmiles(smiles));
			v = StringUtil.formatDouble(stats.getMean()) + " \u00B1 "
					+ StringUtil.formatDouble(stats.getStandardDeviation());
			rs.setResultValue(idx, "Per compound: mean 3-knn distance", v);

		}
		System.out.println(rs.toNiceString());
	}

	public void overlaps()
	{
		Map<CDKDataset, Set<String>> compoundsAsSet = new HashMap<>();
		for (CDKDataset d : data)
		{
			compoundsAsSet.put(d, new HashSet<>(d.getSmiles()));
			if (compoundsAsSet.get(d).size() != d.getSmiles().size())
				throw new RuntimeException("duplicates found!");
		}

		ResultSet rs = new ResultSet();
		for (CDKDataset d : data)
		{
			int idx = rs.addResult();
			rs.setResultValue(idx, "Dataset", d.getDatasetName());

			for (CDKDataset d2 : data)
			{
				int intersect = SetUtil.intersectSize(compoundsAsSet.get(d),
						compoundsAsSet.get(d2));
				double ratio = intersect / (double) d.getSmiles().size();

				rs.setResultValue(idx, d2.getDatasetName(), ratio);
			}
		}
		System.out.println("<ratio> of dataset <row> is included in dataset <col>");
		System.out.println(rs.toNiceString());
	}

	Map<CDKDataset, ADModel> ads;

	private ADModel getModel(CDKDataset dataset) throws Exception
	{
		if (ads == null)
		{
			ads = new HashMap<>();
			for (CDKDataset d : data)
			{
				System.out.println("building ad for " + d.getDatasetName());
				ADModel ad = new KNNTanimotoCFPAppDomainModel(3, true);
				System.out.println("mining features");
				BasicCFPMiner cfp = new BasicCFPMiner();
				cfp.setFeatureSelection(FeatureSelection.fold);
				cfp.setHashfoldsize(2048);
				cfp.setType(CFPType.ecfp4);
				cfp.mine(d.getSmiles());
				ad.setCFPMiner(cfp);
				System.out.println("building ad");
				ad.build();
				ads.put(d, ad);
			}
		}
		return ads.get(dataset);
	}

	public void meanDistance() throws Exception
	{
		ResultSet rs = new ResultSet();
		for (CDKDataset d : data)
		{
			int idx = rs.addResult();
			rs.setResultValue(idx, "Dataset", d.getDatasetName());

			for (CDKDataset d2 : data)
			{
				DescriptiveStatistics stats = new DescriptiveStatistics();
				for (String smiles : d.getSmiles())
					stats.addValue(getModel(d2).getDistanceWithoutIdenticalSmiles(smiles));
				//				double val = stats.getMean();

				String v = StringUtil.formatDouble(stats.getMean()) + " \u00B1 "
						+ StringUtil.formatDouble(stats.getStandardDeviation());

				rs.setResultValue(idx, d2.getDatasetName(), v);
			}
		}
		System.out.println(
				"<mean 3-knn distance> of compounds in dataset <row> to compounds in dataset <col>");
		System.out.println(rs.toNiceString());
	}

	public void insideAppDomain() throws Exception
	{
		ResultSet rs = new ResultSet();
		for (CDKDataset d : data)
		{
			int idx = rs.addResult();
			rs.setResultValue(idx, "Dataset", d.getDatasetName());

			for (CDKDataset d2 : data)
			{
				int inside = 0;
				for (String smiles : d.getSmiles())
					if (getModel(d2).isInsideAppdomain(smiles) == ADPrediction.Inside)
						inside++;
				double val = inside / (double) d.getSmiles().size();

				rs.setResultValue(idx, d2.getDatasetName(), val);
			}
		}
		System.out.println("<ratio> of dataset <row> is included in APP-DOMAIN of dataset <col>");
		System.out.println(rs.toNiceString());
	}

	public static void main(String[] args) throws Exception
	{
		//		System.setOut(new PrintStream(new BufferedOutputStream(
		//				new FileOutputStream("/home/martin/documents/envipath/meeting_2016_05/res.out"))));

		CompareDatasets comp = new CompareDatasets(
				DataLoader.getDatasetFromSMILES("UM-BDD_July_2007",
						"/home/martin/data/envipath/mastermatrix.ob.smi"),
				DataLoader.getDatasetFromSMILES("UM-BDD current",
						"/home/martin/data/envipath/all_compounds.c.ob.smi"),
				DataLoader.getDatasetFromSMILES("KEGG_matrix_100316",
						"/home/martin/data/envipath/KEGG_matrix_100316.ob.smi"),
				DataLoader.getDatasetFromSMILES("Biodeg-Relevance",
						"/home/martin/data/envipath/ALL.ob.smi"));
		//		CompareDatasets comp = new CompareDatasets(DataLoader.INSTANCE.getDataset("CPDBAS_Mouse"),
		//				//DataLoader.INSTANCE.getDataset("CPDBAS_Rat"),
		//				DataLoader.INSTANCE.getDataset("NCTRER")
		//		//DataLoader.INSTANCE.getDataset("DUD_hivrt")
		//		);
		comp.singleAnalysis();
		comp.overlaps();
		comp.insideAppDomain();
		comp.meanDistance();

	}
}
