package org.kramerlab.cfpminer.weka.appdomain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.experiment.CrossValidationResultProducer;
import weka.experiment.OutputZipper;

public abstract class AppDomainCrossValidationResultProducer extends CrossValidationResultProducer
{
	public abstract AppDomainModel getAppDomainModel(int run);

	@Override
	public void doRun(int run) throws Exception
	{
		if (getRawOutput())
		{
			if (m_ZipDest == null)
			{
				m_ZipDest = new OutputZipper(m_OutputFile);
			}
		}

		if (m_Instances == null)
		{
			throw new Exception("No Instances set");
		}
		// Randomize on a copy of the original dataset
		Instances runInstances = new Instances(m_Instances);
		Random random = new Random(run);
		runInstances.randomize(random);
		if (runInstances.classAttribute().isNominal())
		{
			runInstances.stratify(m_NumFolds);
		}
		for (int fold = 0; fold < m_NumFolds; fold++)
		{
			// Add in some fields to the key like run and fold number, dataset name
			Object[] seKey = m_SplitEvaluator.getKey();
			Object[] key = new Object[seKey.length + 3];
			key[0] = Utils.backQuoteChars(m_Instances.relationName());
			key[1] = "" + run;
			key[2] = "" + (fold + 1);
			System.arraycopy(seKey, 0, key, 3, seKey.length);
			if (m_ResultListener.isResultRequired(this, key))
			{
				Instances train = runInstances.trainCV(m_NumFolds, fold, random);
				Instances test = runInstances.testCV(m_NumFolds, fold);

				AppDomainModel ad = getAppDomainModel(run);
				ad.build(train);
				List<Instance> outside = new ArrayList<>();
				for (Instance inst : test)
					if (!ad.isInsideAppdomain(inst))
						outside.add(inst);
				System.out.println(outside.size() + "/" + test.numInstances()
						+ " test instances are outside of the app domain");
				for (Instance out : outside)
					test.remove(out);

				try
				{
					Object[] seResults = m_SplitEvaluator.getResult(train, test);
					Object[] results = new Object[seResults.length + 1];
					results[0] = getTimestamp();
					System.arraycopy(seResults, 0, results, 1, seResults.length);
					if (m_debugOutput)
					{
						String resultName = ("" + run + "." + (fold + 1) + "."
								+ Utils.backQuoteChars(runInstances.relationName()) + "." + m_SplitEvaluator.toString())
								.replace(' ', '_');
						resultName = Utils.removeSubstring(resultName, "weka.classifiers.");
						resultName = Utils.removeSubstring(resultName, "weka.filters.");
						resultName = Utils.removeSubstring(resultName, "weka.attributeSelection.");
						m_ZipDest.zipit(m_SplitEvaluator.getRawResultOutput(), resultName);
					}
					m_ResultListener.acceptResult(this, key, results);
				}
				catch (Exception ex)
				{
					// Save the train and test datasets for debugging purposes?
					throw ex;
				}
			}
		}
	}

}
