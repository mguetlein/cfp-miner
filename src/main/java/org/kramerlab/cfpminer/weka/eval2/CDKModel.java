package org.kramerlab.cfpminer.weka.eval2;

import java.io.File;

import org.mg.wekalib.eval2.data.DataSet;
import org.mg.wekalib.eval2.data.FeatureProvidedDataSet;
import org.mg.wekalib.eval2.data.FoldDataSet;
import org.mg.wekalib.eval2.data.SplitDataSet;
import org.mg.wekalib.eval2.job.DefaultJobOwner;
import org.mg.wekalib.eval2.model.Model;
import org.mg.wekalib.evaluation.Predictions;

public abstract class CDKModel extends DefaultJobOwner<Predictions> implements Model
{
	protected DataSet train;
	protected DataSet test;

	@Override
	public Model cloneJob()
	{
		try
		{
			Model m = this.getClass().newInstance();
			cloneParams(m);
			m.setTestDataset(test);
			m.setTrainingDataset(train);
			return m;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getKeyPrefix()
	{
		String prefix = "";
		if (train != null)
			prefix += train.getKeyPrefix() + File.separator;
		prefix += getName();
		return prefix;
	}

	@Override
	public String getKeyContent()
	{
		return getKeyContent(getParamKey(), train, test);
	}

	@Override
	public Runnable nextJob() throws Exception
	{
		return blockedJob("Model: building " + getName() + " on " + train.getName(), new Runnable()
		{
			public void run()
			{
				validateModel();
			};
		});
	}

	private CDKDataSet getCDKDataSet(boolean train)
	{
		DataSet data = train ? this.train : this.test;
		while (!(data instanceof CDKDataSet))
		{
			if (data instanceof SplitDataSet)
				data = ((SplitDataSet) data).getSelf();
			else if (data instanceof FoldDataSet)
				data = ((FoldDataSet) data).getSelf();
			else if (data instanceof FeatureProvidedDataSet)
			{
				if (train)
					data = ((FeatureProvidedDataSet) data).getFeatureProvider()
							.getTrainingDataset();
				else
					data = ((FeatureProvidedDataSet) data).getFeatureProvider().getTestDataset();
			}
			else
				throw new IllegalStateException("what to do with " + data.getClass() + " " + data);
		}
		return (CDKDataSet) data;
	}

	private void validateModel()
	{
		try
		{
			build(getCDKDataSet(true));
			Predictions p = eval(getCDKDataSet(false));
			if (test instanceof SplitDataSet)
				for (int i = 0; i < p.origIndex.length; i++)
					p.origIndex[i] = ((SplitDataSet) test).getOrigIndex(i);
			setResult(p);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	protected abstract Predictions eval(CDKDataSet test);

	public abstract String getName();

	public abstract void build(CDKDataSet train);

	public abstract boolean isFast();

	protected abstract String getParamKey();

	protected abstract void cloneParams(Model clonedModel);

	@Override
	public void setTrainingDataset(DataSet train)
	{
		this.train = train;
	}

	@Override
	public void setTestDataset(DataSet test)
	{
		this.test = test;
	}

	@Override
	public DataSet getTrainingDataset()
	{
		return train;
	}

	@Override
	public boolean isValid(DataSet dataSet)
	{
		return true;
	}

}
