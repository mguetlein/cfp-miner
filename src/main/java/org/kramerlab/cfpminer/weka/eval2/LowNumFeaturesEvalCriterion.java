package org.kramerlab.cfpminer.weka.eval2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mg.javalib.datamining.Result;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.datamining.ResultSetFilter;
import org.mg.javalib.util.ArrayUtil;
import org.mg.javalib.util.ListUtil;
import org.mg.wekalib.eval2.CV;
import org.mg.wekalib.eval2.Validation;
import org.mg.wekalib.eval2.ValidationEval.EvalCriterion;
import org.mg.wekalib.eval2.model.AbstractModel;
import org.mg.wekalib.eval2.model.FeatureModel;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.Predictions;

public class LowNumFeaturesEvalCriterion implements EvalCriterion
{
	PredictionUtil.ClassificationMeasure measure;

	public LowNumFeaturesEvalCriterion(PredictionUtil.ClassificationMeasure measure)
	{
		this.measure = measure;
	}

	private static int version()
	{
		return 3;
	}

	@Override
	public String getKeyPrefix()
	{
		return measure + "-LF" + version();
	}

	@Override
	public String getKeyContent()
	{
		return measure + "-LF" + version();
	}

	/**
	 * instead of selecting best model, we select
	 * "one of the best models with low amount of features"
	 *  
	 * - reduce to models with performance > max-performance - 0.005
	 * - further reduce models if the remaining difference is significant with with relaxed significance
	 *   (sig-level 0.2 instead of 0.05 or 0.01, no correction-term)
	 * - sort according to
	 * -- num-features (i.e., prefer low number)
	 * --- tie-breaking: fast (i.e., prefer random-forest + nb to svm)
	 * ---- tie-breaking: better performance
	 */
	@Override
	public String selectBestModel(List<Validation> cvs)
	{
		// collect results
		ResultSet rs = new ResultSet();
		for (Validation cv : cvs)
		{
			if (!cv.isDone())
				throw new IllegalArgumentException("cv no yet done!");
			else
			{
				List<Predictions> ps;
				if (cv instanceof CV)
					ps = PredictionUtil.perFold(cv.getResult());
				else
					ps = ListUtil.createList(cv.getResult());

				for (Predictions p : ps)
				{
					int idx = rs.addResult();
					rs.setResultValue(idx, "ModelKey", cv.getModel().getKey());
					rs.setResultValue(idx, "ModelName", cv.getModel().getName());
					rs.setResultValue(idx, "Features",
							((CFPFeatureProvider) ((FeatureModel) cv.getModel())
									.getFeatureProvider()).getHashfoldSize());
					Model m = ((FeatureModel) cv.getModel()).getModel();
					boolean fast = false;
					if (m instanceof AbstractModel)
						fast = ((AbstractModel) m).isFast();
					rs.setResultValue(idx, "Fast", fast ? 1 : 0);
					rs.setResultValue(idx, "Seed", cv.getRandomSeed());
					if (cv instanceof CV)
						rs.setResultValue(idx, "Fold", p.fold[0]);
					rs.setResultValue(idx, measure.toString(),
							PredictionUtil.getClassificationMeasure(p, measure,
									cv.getDataSet().getPositiveClass()));
				}
			}
		}

		// reduce to models with performance > max-performance - 0.005
		double MAX_DEGREDATION = 0.005;
		String ommit[] = new String[] { "Seed" };
		if (cvs.get(0) instanceof CV)
			ommit = ArrayUtil.push(ommit, "Fold");
		ResultSet joined = rs.join(new String[] { "ModelKey", "ModelName", "Features", "Fast" },
				ommit, null);
		joined.sortResults(measure.toString(), false, true, -1);
		System.err.println(joined.toNiceString());
		double max = (Double) joined.getResultValue(0, measure.toString());
		final Set<String> accept = new HashSet<>();
		for (int i = 0; i < joined.getNumResults(); i++)
			if (max - (Double) joined.getResultValue(i, measure.toString()) < MAX_DEGREDATION)
				accept.add(joined.getResultValue(i, "ModelKey").toString());
		rs = rs.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				return accept.contains(result.getValue("ModelKey").toString());
			}
		});

		// further reduce models if the remaining difference is significant with with relaxed significance
		// (sig-level 0.2 instead of (0.05 or 0.01), no correction-term)
		double SIG_LEVEL_RELAXED = 0.2;
		Double TEST_CORRECTION_RELAXED = null;
		final ResultSet test = rs.pairedTTest_All("ModelKey", ArrayUtil.toList(ommit),
				measure.toString(), SIG_LEVEL_RELAXED, TEST_CORRECTION_RELAXED);
		rs = rs.join(new String[] { "ModelKey", "ModelName", "Features", "Fast" }, ommit, null);
		//		System.err.println("\n#" + rs.getNumResults() + " dist to best < " + MAX_DEGREDATION + ":");
		//		System.err.println(rs.toNiceString());
		rs = rs.filter(new ResultSetFilter()
		{
			@Override
			public boolean accept(Result result)
			{
				Boolean w = ResultSet.isWinOrLoss(test, "ModelKey",
						result.getValueToString("ModelKey"), measure.toString());
				// accept if no-loss (i.e., if no sig-difference or win)
				return (w == null) || w;
			}
		});
		//		System.err.println("\n#" + rs.getNumResults() + " not significantly worse:\n");
		//		System.err.println(rs.toNiceString());

		// sort according to
		// - num-features (i.e., prefer low number)
		// -- tie-breaking: fast (i.e., prefer random-forest + nb to svm)
		// --- tie-breaking: better performance (at this point delta < 0.005 and insignificant)
		rs.sortResults(measure.toString(), false, true, -1);
		rs.sortResults("Fast", false, true, -1);
		rs.sortResults("Features", true, true, -1);
		//		System.err.println("\nsorted rs according to #Features, Fast, " + measure + "\n");
		//		System.err.println(rs.toNiceString());

		return rs.getResultValue(0, "ModelKey").toString();
	}

}
