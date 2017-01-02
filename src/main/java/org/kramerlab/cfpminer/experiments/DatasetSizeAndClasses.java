package org.kramerlab.cfpminer.experiments;

import org.mg.cdklib.data.DataProvider;
import org.mg.javalib.datamining.ResultSet;
import org.mg.javalib.util.FileUtil;

public class DatasetSizeAndClasses extends PaperResults
{
	public void printTables()
	{
		{
			ResultSet r = DataProvider.getInfo(false, DATASETS);
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
			ResultSet r = DataProvider.getCategoryInfo(true, DATASETS);
			System.out.println(r.toNiceString());
			if (WRITE_FILES)
			{
				String dest = DEST_FOLDER + "datasets_overview.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
		}

		{
			ResultSet r = DataProvider.getCategoryInfo(false, DATASETS);
			System.out.println(r.toNiceString());
			if (WRITE_FILES)
			{
				String dest = DEST_FOLDER + "datasets_links.tex";
				System.out.println("write table to " + dest);
				FileUtil.writeStringToFile(dest, r.toLatexTable());
			}
		}
	}

	public static void main(String[] args)
	{
		WRITE_FILES = false;
		//System.out.println(new DataLoader("data").getDataset("MUV_733").getWarnings());
		new DatasetSizeAndClasses().printTables();

	}
}
