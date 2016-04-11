package org.kramerlab.cfpminer.appdomain;

public enum ADPrediction
{
	Inside, PossiblyOutside, Outside;

	public String toNiceString()
	{
		switch (this)
		{
			case Inside:
				return "inside";
			case Outside:
				return "outside";
			case PossiblyOutside:
				return "possibly outside";
		}
		throw new IllegalStateException();
	}
}
