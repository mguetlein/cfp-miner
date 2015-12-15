package org.kramerlab.cfpminer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.kramerlab.cfpminer.cdk.CDKUtil;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.CountedSet;
import org.mg.javalib.util.DoubleArraySummary;
import org.mg.javalib.util.FileUtil;
import org.mg.javalib.util.HashUtil;
import org.mg.javalib.util.SetUtil;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.BitSetFingerprint;
import org.openscience.cdk.fingerprint.CircularFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;

public class CFPMiner implements Serializable
{
	private static final long serialVersionUID = 5L;

	protected int numCompounds = 0;
	protected List<String> endpoints;
	protected List<String> trainingDataSmiles;
	protected HashMap<String, String> trainingInchisToEndpoint;
	protected LinkedHashMap<CFPFragment, LinkedHashSet<Integer>> fragmentToCompound = new LinkedHashMap<CFPFragment, LinkedHashSet<Integer>>();
	protected int numUnfoldedConflicts = 0;

	public enum CFPType
	{
		ecfp6, ecfp4, ecfp2, ecfp0, fcfp6, fcfp4, fcfp2, fcfp0;

		int getClassType()
		{
			if (this == ecfp6)
				return CircularFingerprinter.CLASS_ECFP6;
			else if (this == fcfp6)
				return CircularFingerprinter.CLASS_FCFP6;
			else if (this == ecfp4)
				return CircularFingerprinter.CLASS_ECFP4;
			else if (this == fcfp4)
				return CircularFingerprinter.CLASS_FCFP4;
			else if (this == ecfp2)
				return CircularFingerprinter.CLASS_ECFP2;
			else if (this == fcfp2)
				return CircularFingerprinter.CLASS_FCFP2;
			else if (this == ecfp0)
				return CircularFingerprinter.CLASS_ECFP0;
			else if (this == fcfp0)
				return CircularFingerprinter.CLASS_FCFP0;
			else
				throw new IllegalStateException("wtf");
		}

		public int getDiameter()
		{
			if (this == ecfp6)
				return 6;
			else if (this == fcfp6)
				return 6;
			else if (this == ecfp4)
				return 4;
			else if (this == fcfp4)
				return 4;
			else if (this == ecfp2)
				return 2;
			else if (this == fcfp2)
				return 2;
			else if (this == ecfp0)
				return 0;
			else if (this == fcfp0)
				return 0;
			else
				throw new IllegalStateException("wtf");
		}

		public boolean isECFP()
		{
			return (this == ecfp0 || this == ecfp2 || this == ecfp4 || this == ecfp6);
		}

		public String toNiceString()
		{
			return this.toString();//.toUpperCase();
		}
	}

	public enum FeatureSelection
	{
		filt, fold, none;

		public String toNiceString()
		{
			switch (this)
			{
				case filt:
					return "Filtering";
				case fold:
					return "Folding";
				case none:
					return "Unprocessed";
			}
			throw new IllegalStateException();
		}

		public String attribute()
		{
			switch (this)
			{
				case filt:
					return "Filtered";
				case fold:
					return "Folded";
				case none:
					return "Unprocessed";
			}
			throw new IllegalStateException();
		}

		public String toNiceShortString()
		{
			switch (this)
			{
				case filt:
					return "Filt.";
				case fold:
					return "Fold.";
				case none:
					return "Unproc.";
			}
			throw new IllegalStateException();
		}
	}

	protected CFPType type;
	protected FeatureSelection featureSelection;
	protected int hashfoldsize;
	protected int absMinFreq = 2;

	transient LinkedHashMap<CFPFragment, LinkedHashSet<Integer>> fragmentToCompound_unfiltered;
	transient CircularFingerprinter fp;
	transient CFPFragment[] fragmentList;
	transient HashMap<Integer, LinkedHashSet<CFPFragment>> compoundToFragment;
	transient HashMap<CFPFragment, Integer> fragmentToIteration = new HashMap<CFPFragment, Integer>();
	transient HashMap<CFPFragment, Integer> fragmentToNumAtoms = new HashMap<CFPFragment, Integer>();
	transient HashMap<IAtomContainer, LinkedHashSet<CFPFragment>> testMoleculeToFragment;
	transient String[] classValues;
	transient Integer activeIdx;
	transient private HashMap<Integer, Set<Integer>> collisionMap;

	public CFPMiner(List<String> endpoints) throws CDKException
	{
		this.endpoints = endpoints;
	}

	public String[] getClassValues()
	{
		if (classValues == null)
			classValues = CFPDataLoader.getClassValues(endpoints);
		return classValues;
	}

	public int getActiveIdx()
	{
		if (activeIdx == null)
			activeIdx = CFPDataLoader.getActiveIdx(getClassValues());
		return activeIdx;
	}

	public List<String> getEndpoints()
	{
		return endpoints;
	}

	public List<String> getTrainingDataSmiles()
	{
		return trainingDataSmiles;
	}

	public int getHashfoldsize()
	{
		return hashfoldsize;
	}

	public int getNumFragments()
	{
		return fragmentToCompound.size();
	}

	public CFPType getCFPType()
	{
		return type;
	}

	public FeatureSelection getFeatureSelection()
	{
		return featureSelection;
	}

	public String getFeatureType()
	{
		return featureSelection.attribute() + " " + type.toNiceString();
	}

	public void setType(CFPType type)
	{
		this.type = type;
	}

	public void setFeatureSelection(FeatureSelection featureSelection)
	{
		this.featureSelection = featureSelection;
	}

	public void setHashfoldsize(int hashfoldsize)
	{
		this.hashfoldsize = hashfoldsize;
	}

	public boolean isFragmentIncludedInCompound(int compound, CFPFragment fragment)
	{
		if (fragmentToCompound.get(fragment) == null)
			throw new IllegalStateException("no compounds for fragment, should have been removed! " + fragment + " "
					+ fragmentToCompound.get(fragment));
		return (fragmentToCompound.get(fragment).contains(compound));
	}

	private CFPMiner()
	{
	}

	private void initCircularFingerprinter()
	{
		collisionMap = new HashMap<>();
		fp = new CircularFingerprinter(type.getClassType())
		{
			public IBitFingerprint getBitFingerprint(IAtomContainer mol) throws CDKException
			{
				calculate(mol);
				if (featureSelection != FeatureSelection.fold)
					return null;
				final BitSet bits = new BitSet(hashfoldsize);
				for (int n = 0; n < getFPCount(); n++)
				{
					int i = getFP(n).hashCode;
					long b = i >= 0 ? i : ((i & 0x7FFFFFFF) | (1L << 31));
					int bit = (int) (b % hashfoldsize);
					if (!collisionMap.containsKey(bit))
						collisionMap.put(bit, new HashSet<Integer>());
					collisionMap.get(bit).add(i);
					bits.set(bit);
				}
				return new BitSetFingerprint(bits);
			}
		};
	}

	public int[] getAtoms(String smiles, CFPFragment fragment) throws Exception
	{
		return getAtoms(CDKUtil.parseSmiles(smiles), fragment);
	}

	public int[] getAtoms(IAtomContainer mol, CFPFragment fragment) throws Exception
	{
		if (featureSelection == FeatureSelection.fold)
			throw new IllegalArgumentException();
		int atoms[] = null;
		initCircularFingerprinter();
		fp.getBitFingerprint(mol);
		for (int i = 0; i < fp.getFPCount(); i++)
		{
			if (fp.getFP(i).hashCode == fragment.getId())
			{
				atoms = fp.getFP(i).atoms;
				break;
			}
		}
		return atoms;
	}

	private static transient HashMap<Integer, Set<Integer>> atomsMultCache = new HashMap<>();

	/**
	 * fragment may occur multiple times
	 * 
	 * @param mol
	 * @param fragment
	 * @return
	 * @throws Exception
	 */
	public Set<Integer> getAtomsMultiple(IAtomContainer mol, CFPFragment fragment) throws Exception
	{
		if (featureSelection == FeatureSelection.fold)
			throw new IllegalArgumentException();

		Integer key = HashUtil.hashCode(type, mol, fragment);
		if (!atomsMultCache.containsKey(key))
		{
			Set<Integer> atoms = new HashSet<>();
			initCircularFingerprinter();
			fp.getBitFingerprint(mol);
			for (int i = 0; i < fp.getFPCount(); i++)
				if (fp.getFP(i).hashCode == fragment.getId())
					for (int a : fp.getFP(i).atoms)
						atoms.add(a);
			atomsMultCache.put(key, atoms);
		}
		return atomsMultCache.get(key);
	}

	public void mine(List<String> smiles) throws Exception
	{
		this.trainingDataSmiles = smiles;

		trainingInchisToEndpoint = new HashMap<>();
		int idx = 0;
		for (String smi : smiles)
		{
			String inchi = CDKUtil.toInchi(smi);
			if (trainingInchisToEndpoint.containsKey(inchi))
				throw new IllegalStateException("no duplicates allowed!");
			trainingInchisToEndpoint.put(inchi, endpoints.get(idx));
			idx++;
		}

		initCircularFingerprinter();
		for (String smi : smiles)
		{
			IBitFingerprint finger = fp.getBitFingerprint(CDKUtil.parseSmiles(smi));
			if (featureSelection == FeatureSelection.fold)
				for (int i : finger.getSetbits())
					insert(fragmentToCompound, new CFPFragment(i), numCompounds);
			else
				for (int i = 0; i < fp.getFPCount(); i++)
				{
					//					if (fp.getFP(i).hashCode == 1039376976)
					//					{
					//						System.err.println(fp.getFP(i).atoms.length + " " + ArrayUtil.toString(fp.getFP(i).atoms));
					//						try
					//						{
					//							drawFP(null, mol, fp.getFP(i).atoms);
					//						}
					//						catch (Exception e)
					//						{
					//							e.printStackTrace();
					//						}
					//					}
					CFPFragment frag = new CFPFragment(fp.getFP(i).hashCode);
					insert(fragmentToCompound, frag, numCompounds);
					boolean conflict = check(fragmentToIteration, frag, fp.getFP(i).iteration);
					conflict |= check(fragmentToNumAtoms, frag, fp.getFP(i).atoms.length);
					if (conflict)
					{
						//						System.err.println(fp.getFP(i).hashCode);
						//						if (numUnfoldedConflicts > 5)
						//							System.exit(1);
						numUnfoldedConflicts++;
					}
				}
			numCompounds++;
		}
	}

	private static <T1, T2> void insert(HashMap<T1, LinkedHashSet<T2>> map, T1 key, T2 val)
	{
		if (!map.containsKey(key))
			map.put(key, new LinkedHashSet<T2>());
		map.get(key).add(val);
	}

	private static <T> boolean check(HashMap<T, Integer> map, T key, int val)
	{
		if (map.containsKey(key) && map.get(key) != val)
			return true; //System.err.println("conflict " + key + " val1: " + val + " val2:" + map.get(key));
		map.put(key, val);
		return false;
	}

	public LinkedHashSet<Integer> getCompoundsForFragment(CFPFragment fragment)
	{
		return fragmentToCompound.get(fragment);
	}

	public LinkedHashSet<CFPFragment> getFragmentsForCompound(Integer compound)
	{
		if (compoundToFragment == null)
		{
			compoundToFragment = new HashMap<Integer, LinkedHashSet<CFPFragment>>();
			for (CFPFragment f : fragmentToCompound.keySet())
				for (Integer c : fragmentToCompound.get(f))
					insert(compoundToFragment, c, f);
		}
		if (compoundToFragment.containsKey(compound))
			return compoundToFragment.get(compound);
		else
			return new LinkedHashSet<CFPFragment>();
	}

	public double getTanimotoSimilarity(int i, int j)
	{
		HashSet<CFPFragment> h1 = getFragmentsForCompound(i);
		HashSet<CFPFragment> h2 = getFragmentsForCompound(j);
		int and = SetUtil.intersectSize(h1, h2);
		int or = h1.size() + h2.size() - and;
		return and / (double) or;
	}

	public String getTrainingActivity(String smiles) throws CDKException
	{
		return trainingInchisToEndpoint.get(CDKUtil.toInchi(smiles));
	}

	public CFPFragment getFragmentViaIdx(int fragmentIdx)
	{
		if (fragmentList == null)
		{
			fragmentList = new CFPFragment[fragmentToCompound.size()];
			int idx = 0;
			for (CFPFragment h : fragmentToCompound.keySet())
				fragmentList[idx++] = h;
		}
		return fragmentList[fragmentIdx];
	}

	public LinkedHashSet<CFPFragment> getFragmentsForTestCompound(String smiles) throws Exception
	{
		return getFragmentsForTestCompound(CDKUtil.parseSmiles(smiles));
	}

	public LinkedHashSet<CFPFragment> getFragmentsForTestCompound(IAtomContainer testMol) throws CDKException
	{
		if (testMoleculeToFragment == null)
			testMoleculeToFragment = new HashMap<IAtomContainer, LinkedHashSet<CFPFragment>>();
		if (!testMoleculeToFragment.containsKey(testMol))
		{
			if (fp == null)
				initCircularFingerprinter();
			LinkedHashSet<CFPFragment> fragments = new LinkedHashSet<CFPFragment>();
			IBitFingerprint finger = fp.getBitFingerprint(testMol);
			if (featureSelection == FeatureSelection.fold)
				for (int i : finger.getSetbits())
					fragments.add(new CFPFragment(i));
			else
				for (int i = 0; i < fp.getFPCount(); i++)
					fragments.add(new CFPFragment(fp.getFP(i).hashCode));
			testMoleculeToFragment.put(testMol, fragments);
		}
		return testMoleculeToFragment.get(testMol);
	}

	@SuppressWarnings("unchecked")
	public CFPMiner clone()
	{
		CFPMiner f = new CFPMiner();
		f.type = type;
		f.hashfoldsize = hashfoldsize;
		f.featureSelection = featureSelection;
		f.absMinFreq = absMinFreq;
		f.numCompounds = numCompounds;
		for (CFPFragment frag : fragmentToCompound.keySet())
			f.fragmentToCompound.put(frag, (LinkedHashSet<Integer>) fragmentToCompound.get(frag).clone());
		return f;
	}

	public int getNumCompounds()
	{
		return numCompounds;
	}

	private void applyMinFreq(Set<Integer> compoundSubset, int minFreq)
	{
		// remove with min-freq 0, would not have been mined on subset
		List<CFPFragment> fragmentToDelete = new ArrayList<CFPFragment>();
		for (CFPFragment f : fragmentToCompound.keySet())
			if (SetUtil.intersectSize(fragmentToCompound.get(f), compoundSubset) == 0)
				fragmentToDelete.add(f);
		removeFragments(fragmentToDelete);

		// remove only if larger than hashfoldize
		if (fragmentToCompound.size() <= hashfoldsize)
			return;

		// collect tuples of hash code and min-freq
		List<int[]> hf = new ArrayList<int[]>();
		for (CFPFragment f : fragmentToCompound.keySet())
			hf.add(new int[] { f.getId(), SetUtil.intersectSize(fragmentToCompound.get(f), compoundSubset) });

		// sort according to min freq, in decreasing order
		Collections.sort(hf, new Comparator<int[]>()
		{
			@Override
			public int compare(int[] o1, int[] o2)
			{
				// swap params to sort in descending order
				return Integer.valueOf(o2[1]).compareTo(Integer.valueOf(o1[1]));
			}
		});

		Set<CFPFragment> toRemove = new HashSet<>();
		// apply min-freq while size > hashfoldsize
		for (int i = hashfoldsize; i < hf.size(); i++)
		{
			if (hf.get(i)[1] < absMinFreq)
				toRemove.add(new CFPFragment(hf.get(i)[0]));
		}
		removeFragments(toRemove);

		System.err.println("applied min freq filter: " + fragmentToCompound.size());

	}

	protected static <T> long[] nominalCounts(List<T> domain, List<T> values)
	{
		CountedSet<T> countedValues = CountedSet.create(values);
		long[] counts = new long[domain.size()];
		int i = 0;
		for (T v : domain)
			counts[i++] = countedValues.getCount(v);
		return counts;
	}

	public void applyClosedSetFilter(Set<Integer> compoundSubset)
	{
		if (fragmentToCompound.size() <= hashfoldsize)
			return;
		int maxNumRemove = fragmentToCompound.size() - hashfoldsize;

		LinkedHashMap<CFPFragment, Set<Integer>> fragmentToCompoundSubset = new LinkedHashMap<>();
		LinkedHashMap<Integer, List<CFPFragment>> fragmentWithSimilarSubsets = new LinkedHashMap<>();
		for (CFPFragment f : fragmentToCompound.keySet())
		{
			@SuppressWarnings("unchecked")
			Set<Integer> hCompounds = (Set<Integer>) fragmentToCompound.get(f).clone();
			hCompounds.retainAll(compoundSubset);
			fragmentToCompoundSubset.put(f, hCompounds);

			int eq = hCompounds.hashCode();
			if (!fragmentWithSimilarSubsets.containsKey(eq))
				fragmentWithSimilarSubsets.put(eq, new ArrayList<CFPFragment>());
			fragmentWithSimilarSubsets.get(eq).add(f);
		}

		if (fragmentList == null)
			getFragmentViaIdx(0);
		Set<CFPFragment> fragmentsToRemove = new HashSet<>();

		for (List<CFPFragment> fragmentSet : fragmentWithSimilarSubsets.values())
		{
			for (int i = 0; i < fragmentSet.size() - 1; i++)
			{
				CFPFragment f1 = fragmentSet.get(i);
				if (fragmentsToRemove.contains(f1))
					continue;

				for (int j = i + 1; j < fragmentSet.size(); j++)
				{
					CFPFragment f2 = fragmentSet.get(j);
					if (fragmentsToRemove.contains(f2))
						continue;

					if (fragmentToCompoundSubset.get(f1).equals(fragmentToCompoundSubset.get(f2)))
					{
						CFPFragment obsolete = getNonClosed(f1, f2, fragmentToCompoundSubset.get(f1));
						if (obsolete != null)
						{
							fragmentsToRemove.add(obsolete);
							if (fragmentsToRemove.size() >= maxNumRemove || obsolete == f1)
								break;
						}
					}
				}
				if (fragmentsToRemove.size() >= maxNumRemove)
					break;
			}
			if (fragmentsToRemove.size() >= maxNumRemove)
				break;
		}

		removeFragments(fragmentsToRemove);

		System.err.println("applied closed fragment filter: " + fragmentToCompound.size());
	}

	public CFPFragment getNonClosed(CFPFragment f1, CFPFragment f2, Set<Integer> compounds)
	{

		//		System.out.println("check if " + h1 + " is superset of " + h2 + ", compounds: " + compounds);
		try
		{
			boolean f1SupersetCandiate = true;
			boolean f2SupersetCandiate = true;
			for (Integer c : compounds)
			{
				IAtomContainer mol = CDKUtil.parseSmiles(trainingDataSmiles.get(c));
				Set<Integer> atoms1 = getAtomsMultiple(mol, f1);
				Set<Integer> atoms2 = getAtomsMultiple(mol, f2);
				//				System.out.println("mol " + c + " atoms h1: " + atoms1 + " atoms h2: " + atoms2);
				if (f1SupersetCandiate)
					if (!SetUtil.isSubSet(atoms1, atoms2))
						f1SupersetCandiate = false;
				if (f2SupersetCandiate)
					if (!SetUtil.isSubSet(atoms2, atoms1))
						f2SupersetCandiate = false;
				if (!f1SupersetCandiate && !f2SupersetCandiate)
					return null;
			}
			if (f1SupersetCandiate)
				return f2;
			else if (f2SupersetCandiate)
				return f1;
			else
				throw new IllegalStateException();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public void applyChiSquareFilter(Set<Integer> compoundSubset)
	{
		// chi square is apply to a counts array for each class
		// e.g. it compares 45 x active 41 x inactive in the compoundSubset
		// to feature-x with 31 x active, 5 x inactive for all compounds with feature-value(feature-x, compound-y)= active
		if (endpoints.size() != numCompounds)
			throw new IllegalArgumentException(endpoints.size() + " != " + numCompounds);
		if (fragmentToCompound.size() <= hashfoldsize)
			return;
		List<String> domain = new ArrayList<String>(new HashSet<String>(endpoints));
		List<String> subsetEndpoints = new ArrayList<String>();
		for (Integer c : compoundSubset)
			subsetEndpoints.add(endpoints.get(c));
		long[] all = nominalCounts(domain, subsetEndpoints);
		//		System.out.println(domain);
		//		System.out.println(ArrayUtil.toString(all));

		// create tuples of hash code and p-value
		List<double[]> hp = new ArrayList<double[]>();
		for (CFPFragment f : fragmentToCompound.keySet())
		{
			List<String> values = new ArrayList<String>();
			for (Integer c : fragmentToCompound.get(f))
				if (compoundSubset.contains(c))
					values.add(endpoints.get(c));
			//			System.out.println(values);
			//			System.out.println(ArrayUtil.toString(nominalCounts(domain, values)));

			double p = Double.MAX_VALUE;
			if (values.size() > 0)
			{
				long sel[] = nominalCounts(domain, values);
				//System.err.println(ArrayUtil.toString(all));
				//				System.err.println(values.size());
				//System.err.println(ArrayUtil.toString(sel));
				p = TestUtils.chiSquareTestDataSetsComparison(sel, all);
				//				System.err.println("p value of hash code " + h + " is " + p);
				//				if (p > pValueThreshold)
				//					hashCodeToDelete.add(h);
			}
			hp.add(new double[] { f.getId(), p });
		}

		//sort tuples according to p-values
		Collections.sort(hp, new Comparator<double[]>()
		{
			@Override
			public int compare(double[] o1, double[] o2)
			{
				return Double.valueOf(o1[1]).compareTo(Double.valueOf(o2[1]));
			}
		});

		//sort compounds higher than hash
		Set<CFPFragment> toRemove = new HashSet<>();
		for (int i = hashfoldsize; i < hp.size(); i++)
		{
			//			System.err.println("remove fragment with p-value " + hp.get(i)[1]);
			toRemove.add(new CFPFragment((int) hp.get(i)[0]));
		}
		removeFragments(toRemove);
		if (fragmentToCompound.size() != hashfoldsize)
			throw new IllegalStateException();

		System.err.println("applied chi square filter: " + fragmentToCompound.size());
	}

	private void removeFragments(Collection<CFPFragment> fragmentToDelete)
	{
		for (CFPFragment f : fragmentToDelete)
			fragmentToCompound.remove(f);
		compoundToFragment = null;
		fragmentList = null;
	}

	public ResultSet getSummary(boolean nice)
	{
		ResultSet set = new ResultSet();
		int idx = set.addResult();
		//set.setResultValue(idx, "name", getName());
		if (!nice)
			set.setResultValue(idx, "Endpoints", CountedSet.create(endpoints));
		set.setResultValue(idx, "Num fragments", fragmentToCompound.size());
		if (!nice)
			set.setResultValue(idx, "Num compounds", numCompounds);
		set.setResultValue(idx, "Fragment type", nice ? type.toNiceString() : type);
		set.setResultValue(idx, "Feature selection", nice ? featureSelection.toNiceString() : featureSelection);
		//		if (featureSelection == FeatureSelection.filter)
		//		{
		//			set.setResultValue(idx, "Min frequency (relative/absolute)",
		//					relMinFreq + " / " + (int) Math.round(relMinFreq * numCompounds));
		//			set.setResultValue(idx, Character.toString((char) 967) + Character.toString((char) 178) + " max p-value",
		//					pValueThreshold);
		//		}
		if (!nice)
			set.setResultValue(idx, "Fingerprint size", hashfoldsize);
		if (!nice)
		{
			set.setResultValue(idx, "num unfolded conflicts", numUnfoldedConflicts);
			int n = 0;
			for (int c = 0; c < numCompounds; c++)
				if (getFragmentsForCompound(c).isEmpty())
					n++;
			set.setResultValue(idx, "compounds w/o hash code", n);
			List<Integer> cmps = new ArrayList<>();
			List<String> cmpsStr = new ArrayList<>();
			for (CFPFragment fragment : fragmentToCompound.keySet())
			{
				cmps.add(fragmentToCompound.get(fragment).size());
				cmpsStr.add(fragmentToCompound.get(fragment).size() + "");
			}
			set.setResultValue(idx, "mean compounds per fragment", DoubleArraySummary.create(cmps));
			CountedSet<String> setStr = CountedSet.create(cmpsStr);
			for (Integer f : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
				set.setResultValue(idx, "fragment with freq " + f + ": ", setStr.getCount(f + ""));

			List<Integer> numFragments = new ArrayList<>();
			for (int c = 0; c < numCompounds; c++)
				numFragments.add(getFragmentsForCompound(c).size());
			set.setResultValue(idx, "mean fragments per compound", DoubleArraySummary.create(numFragments));

			if (featureSelection == FeatureSelection.fold)
			{
				estimateCollisions(set, idx, "");
			}
		}
		return set;
	}

	public void estimateCollisions(ResultSet set, int idx, String prefix)
	{
		List<Integer> counts = new ArrayList<Integer>();
		List<String> countsStr = new ArrayList<String>();
		int numCollisions = 0;
		int numBits = 0;
		for (int i = 0; i < hashfoldsize; i++)
			if (collisionMap.containsKey(i))
			{
				if (collisionMap.get(i).size() > 1)
					numCollisions++;
				numBits++;
				counts.add(collisionMap.get(i).size());
				countsStr.add(collisionMap.get(i).size() + "");
			}
			else
				countsStr.add("0");

		//		set.setResultValue(idx, prefix + "collisions", numCollisions + "/" + numBits);
		set.setResultValue(idx, prefix + "collisions", (double) numCollisions / numBits);

		//		set.setResultValue(idx, prefix + "collision ratio", numCollisions / (double) numBits);
		DoubleArraySummary occu = DoubleArraySummary.create(counts);
		set.setResultValue(idx, prefix + "bit-load", occu.getMean());

		//		CountedSet<String> occuStr = CountedSet.create(countsStr);
		//		for (Integer f : new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 })
		//			set.setResultValue(idx, prefix + "bits with #" + f + " fragments", occuStr.getCount(f + ""));
	}

	public String toString()
	{
		return getSummary(false).translate().toNiceString();
	}

	//	LinkedHashMap<Pair, Set<Integer>> pairAdjacent = new LinkedHashMap<>();

	public void toCSVFile(String path, List<String> smiles, List<?> endpoints)
	{
		if (smiles.size() != numCompounds)
			throw new IllegalArgumentException();

		StringBuffer s = new StringBuffer();
		s.append("SMILES,endpoint");
		for (CFPFragment f : fragmentToCompound.keySet())
			s.append("," + f);
		s.append("\n");
		for (int c = 0; c < numCompounds; c++)
		{
			s.append(smiles.get(c) + "," + endpoints.get(c));
			for (CFPFragment f : fragmentToCompound.keySet())
				s.append("," + (getFragmentsForCompound(c).contains(f) ? "1" : "0"));
			s.append("\n");
		}
		FileUtil.writeStringToFile(path, s.toString());
	}

	protected void minePairs(Set<Integer> compoundSubset)
	{
	}

	public void applyFilter()
	{
		HashSet<Integer> allCompounds = new HashSet<Integer>();
		for (int i = 0; i < numCompounds; i++)
			allCompounds.add(i);
		applyFilter(allCompounds);
	}

	@SuppressWarnings("unchecked")
	public void applyFilter(Set<Integer> filterSubset)
	{
		if (featureSelection != FeatureSelection.filt)
			return;

		// undo old filter
		if (fragmentToCompound_unfiltered != null)
		{
			fragmentToCompound = (LinkedHashMap<CFPFragment, LinkedHashSet<Integer>>) fragmentToCompound_unfiltered
					.clone();
			compoundToFragment = null;
			fragmentList = null;
		}
		else
			fragmentToCompound_unfiltered = (LinkedHashMap<CFPFragment, LinkedHashSet<Integer>>) fragmentToCompound
					.clone();

		System.err.println("apply filtering: " + fragmentToCompound.size());

		// apply new filter
		applyMinFreq(filterSubset, absMinFreq);

		minePairs(filterSubset);
		applyClosedSetFilter(filterSubset);

		applyChiSquareFilter(filterSubset);

		//		System.out.println("filtered to: " + this);
	}

	public String getName()
	{
		String suffix = "";
		if (featureSelection == FeatureSelection.fold)
			suffix = "_" + hashfoldsize;
		else if (featureSelection == FeatureSelection.filt)
			suffix = "_" + hashfoldsize;
		return type + "_" + featureSelection + suffix;
	}

}
