package org.kramerlab.cfpminer.experiments;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.WekaResultSetUtil;
import org.mg.javalib.io.ExternalTool;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.eval2.model.ModelProvider;

public class PaperResults
{
	public static final String RESULTS = System.getProperty("user.home") + "/results/cfpminer";

	protected static final String DEST_FOLDER = System.getProperty("user.home")
			+ "/documents/ecfps/latex/results/";
	protected static boolean SHOW_PLOTS = true;
	protected static boolean WRITE_FILES = false;
	protected static final List<String> DATASETS = ArrayUtil
			.toList(DataLoader.INSTANCE.allDatasetsSorted());
	protected static final List<Integer> SIZES = ListUtil.createList(1024, 2048, 4096, 8192);

	public static final String RUNTIME = "Time";

	static
	{
		Locale.setDefault(Locale.US);
		ResultSet.T_TESTER = WekaResultSetUtil.T_TESTER;
	}

	protected void toPDF(String svgFileWithoutExtension)
	{
		new ExternalTool(null).run("to-pdf", ("rsvg-convert -f pdf -o " + svgFileWithoutExtension
				+ ".pdf " + svgFileWithoutExtension + ".svg").split(" "));
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
