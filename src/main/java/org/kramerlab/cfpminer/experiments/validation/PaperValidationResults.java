package org.kramerlab.cfpminer.experiments.validation;

import org.kramerlab.cfpminer.experiments.PaperResults;

public class PaperValidationResults extends PaperResults
{
	public PaperValidationResults()
	{
		RunCV.initDB(false, null);
	}
}
