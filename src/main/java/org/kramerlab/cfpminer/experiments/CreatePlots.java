package org.kramerlab.cfpminer.experiments;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.kramerlab.cfpminer.experiments.plots.CFPDiffChart;
import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.WekaResultSetUtil;
import org.mg.javalib.io.ExternalTool;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;
import org.mg.wekalib.eval2.persistance.DB;
import org.mg.wekalib.eval2.persistance.ResultProviderImpl;

public class CreatePlots
{
	//	public static final List<String> DATASETS = ListUtil.createList("CPDBAS_Mutagenicity",
	//			"DUD_vegfr2", "DUD_hivrt", "DUD_cdk2");
	public static final List<String> DATASETS = ArrayUtil
			.toList(new DataLoader("data").allDatasetsSorted());
	public static boolean SHOW_PLOTS = true;
	public static boolean WRITE_FILES = false;
	public static final String DEST_FOLDER = "/home/martin/documents/ecfps/latex/results/";
	public static final List<Integer> SIZES = ListUtil.createList(1024, 2048, 4096, 8192);

	public static final String RUNTIME = "Time";

	static
	{
		Locale.setDefault(Locale.US);
		ResultSet.T_TESTER = WekaResultSetUtil.T_TESTER;
	}

	protected static void toPDF(String svgFileWithoutExtension)
	{
		new ExternalTool(null).run("to-pdf", ("rsvg-convert -f pdf -o " + svgFileWithoutExtension
				+ ".pdf " + svgFileWithoutExtension + ".svg").split(" "));
	}

	public static void main(String[] args) throws Exception
	{
		DB.init(new ResultProviderImpl("jobs/store", "jobs/tmp"), null);
		SHOW_PLOTS = false;
		WRITE_FILES = true;
		//CFPLineChart.create();
		//new NumFragmentsAndCollisions().printTables();
		//new DatasetSizeAndClasses().printTables();
		new CFPDiffChart().create();
		//		new CFPWinLossChart().plotWinLossFeatureSelection();
		//		new CFPWinLossChart().plotWinLossECFPDiameter();
		//		new CFPWinLossChart().plotWinLossECFPvsFCFP();
		System.exit(0);
	}

	private static HashMap<Model, Integer> ALG_ORDERING = new HashMap<>();

	static
	{
		ALG_ORDERING.put(ModelProvider.RANDOM_FOREST, 0);
		ALG_ORDERING.put(ModelProvider.SVM, 1);
		ALG_ORDERING.put(ModelProvider.NAIVE_BAYES, 2);
	}

	public static Comparator<Object> ALG_COMPARATOR = new Comparator<Object>()
	{
		public int compare(Object o1, Object o2)
		{
			Model m1 = null;
			Model m2 = null;
			for (Model m : ModelProvider.ALL_MODELS_PARAM_DEFAULT)
			{
				if (o1.toString().equals(m.getAlgorithmShortName()))
					m1 = m;
				if (o2.toString().equals(m.getAlgorithmShortName()))
					m2 = m;
			}
			return ALG_ORDERING.get(m1) - ALG_ORDERING.get(m2);
		}
	};
}
