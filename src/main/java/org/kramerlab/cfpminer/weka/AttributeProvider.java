package org.kramerlab.cfpminer.weka;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public interface AttributeProvider extends Serializable
{
	public String getName();

	public int getNumAttributes();

	public String getAttributeName(int a);

	public List<String> getAttributeDomain(int a);

	public double getAttributeValue(int i, int a);

	public void applyFilter(Set<Integer> filterSubset);

	// for app-domain methods

	public double getTanimotoSimilarity(int i, int j);
}