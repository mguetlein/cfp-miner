package org.kramerlab.cfpminer.experiments;

import java.util.Arrays;

import org.mg.cdklib.data.DataLoader;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.FileUtil;

public class DatasetSizeAndClasses extends CreatePlots
{

	public void printTables()
	{
		DataLoader d = new DataLoader("data");
		String datasets[] = d.allDatasets();
		Arrays.sort(datasets, 0, datasets.length, DataLoader.CFPDataComparator);

		{
			ResultSet r = d.getInfo(datasets);
			for (int idx = 0; idx < r.getNumResults(); idx++)
				r.setResultValue(idx, "name",
						r.getResultValue(idx, "name").toString().replaceAll("_", " "));
			r.removePropery("source");
			System.out.println(r.toNiceString());
			if (WRITE_FILES)
			{
				String dest = DEST_FOLDER + "datasets.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
			System.out.println();
		}

		{
			ResultSet r = d.getCategoryInfo(datasets);
			System.out.println(r.toNiceString());
			if (WRITE_FILES)
			{
				String dest = DEST_FOLDER + "datasets_overview.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
		}
	}

	public static void main(String[] args)
	{
		//System.out.println(new DataLoader("data").getDataset("MUV_733").getWarnings());
		new DatasetSizeAndClasses().printTables();

	}
}
