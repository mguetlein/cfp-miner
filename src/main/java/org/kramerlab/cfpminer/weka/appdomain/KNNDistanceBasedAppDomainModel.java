package org.kramerlab.cfpminer.weka.appdomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.HashUtil;
import org.mg.javalib.util.SortedList;

import weka.core.Instance;
import weka.core.Instances;

public abstract class KNNDistanceBasedAppDomainModel implements AppDomainModel
{
	// params

	int k = 5;

	public enum AvgMethod
	{
		mean, median;
	}

	AvgMethod avgMethod = AvgMethod.mean;

	double max = 1;

	// member variables

	private double avgTrainingDist;

	private Instances trainingData;

	public abstract double computeDistance(Instance i1, Instance i2);

	public abstract void buildInternal(Instances trainingData);

	private HashMap<Integer, Double> distances = new HashMap<>();

	public double getDistance(Instance i1, Instance i2)
	{
		Integer key = HashUtil.hashCode(i1, i2);
		if (!distances.containsKey(key))
		{
			double d = computeDistance(i1, i2);
			distances.put(key, d);
			distances.put(HashUtil.hashCode(i2, i1), d);
		}
		return distances.get(key);
	}

	@Override
	public void build(Instances trainingData)
	{
		buildInternal(trainingData);
		this.trainingData = trainingData;
		List<Double> knnTrainingDistances = new ArrayList<>();
		for (Instance instance : trainingData)
			knnTrainingDistances.add(computeKnnDist(instance));
		if (avgMethod == AvgMethod.mean)
			avgTrainingDist = DoubleArraySummary.create(knnTrainingDistances).getMean();
		else
			avgTrainingDist = DoubleArraySummary.create(knnTrainingDistances).getMedian();
	}

	private double computeKnnDist(Instance instance)
	{
		SortedList<Double> dists = new SortedList<>();
		for (Instance instance2 : trainingData)
		{
			if (instance == instance2)
				continue;
			double d = getDistance(instance, instance2);
			if (dists.size() < k)
				dists.add(d);
			else if (d < dists.get(k - 1))
			{
				dists.add(d);
				dists.remove(k);
			}
		}
		double dist;
		if (avgMethod == AvgMethod.mean)
			dist = DoubleArraySummary.create(dists).getMean();
		else
			dist = DoubleArraySummary.create(dists).getMedian();
		return dist;
	}

	@Override
	public boolean isInsideAppdomain(Instance testInstance)
	{
		double dist = computeKnnDist(testInstance);
		return dist <= avgTrainingDist * max;
	}
}
