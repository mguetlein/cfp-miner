package org.kramerlab.cfpminer.weka.eval2;

import org.kramerlab.cfpminer.util.PubChemDiscriminationClassifier;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.evaluation.PredictionUtil;
import org.mg.wekalib.evaluation.Predictions;

public class PubChemADModel extends CDKModel
{
	@Override
	public String getAlgorithmShortName()
	{
		return "PubChemAD";
	}

	@Override
	public boolean isFast()
	{
		return true;
	}

	int n = 3000;

	@Override
	protected String getParamKey()
	{
		return "n=" + n;
	}

	@Override
	protected void cloneParams(Model clonedModel)
	{
	}

	PubChemDiscriminationClassifier disc;

	@Override
	public void build(CDKDataSet train)
	{
		disc = new PubChemDiscriminationClassifier(n);
		try
		{
			disc.build(train.getSmiles());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Predictions eval(CDKDataSet test)
	{
		try
		{
			System.out.println("predicting " + test.getSize() + " test compounds");
			Predictions p = new Predictions();
			for (int i = 0; i < test.getSize(); i++)
			{
				double prob = disc.predict(test.getSmiles().get(i));
				PredictionUtil.add(p, Double.NaN, prob >= 0.5 ? 1.0 : 0.0, prob, -1, -1);
			}
			System.out.println("done " + p.actual.length);
			return p;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getName()
	{
		return getAlgorithmShortName();
	}

	@Override
	public String getAlgorithmParamsNice()
	{
		return "n: " + n;
	}
}
