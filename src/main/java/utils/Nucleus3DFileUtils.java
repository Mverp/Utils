package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.Coordinates;
import ij.IJ;

public abstract class Nucleus3DFileUtils
{
	// The file column numbers for the various nucleus features
	public final static int NUCLEUS_ID = 2;
	public final static int X_COORDINATE = 3;
	public final static int Y_COORDINATE = 4;
	public final static int Z_COORDINATE = 5;
	public final static int TRUE_NUCLEUS = 7;
	public final static int IS_PART_OFF = 8;
	// Total number of expected columns in the file
	public final static int NUMBER_OF_COLUMNS = 9;

	private final static String DATA_SEPARATOR = "\t";
	private final static String TRUE_POINT = "true";
	private final static String FALSE_POINT = "false";


	/**
	 * Read the just central coordinates of a nucleus from a Nucleus3D file
	 *
	 * @param aDirectory,
	 *            Where the Nucleus_3D file is located
	 * @param aFileName,
	 *            The file name of the Nucleus_3D file
	 * @return A list of all the central coordinates
	 */
	static public List<Coordinates> readNucleusSeeds(final File aDirectory, final String aFileName) throws IOException
	{
		final List<Coordinates> result = new ArrayList<>();

		String currentNucleusID = "";
		int nrOfSeedsPerID = 0;
		try
		{
			final FileReader fileReader = new FileReader(aDirectory.getParentFile() + File.separator + aFileName);
			final BufferedReader reader = new BufferedReader(fileReader);
			String line;
			while ((line = reader.readLine()) != null)
			{
				final String[] columns = line.split(DATA_SEPARATOR);
				// Check if each pointGroup have one 'true' point (central point)
				if (!currentNucleusID.contains(columns[NUCLEUS_ID]))
				{
					// If there is a new pointGroup reset the countTrue and namePointGroup
					currentNucleusID = columns[NUCLEUS_ID];
					nrOfSeedsPerID = 0;
				}

				if (columns[TRUE_NUCLEUS].equals(TRUE_POINT))
				{
					nrOfSeedsPerID = nrOfSeedsPerID + 1;
					if (nrOfSeedsPerID == 1)
					{
						// Get the XYZ coordinates of the central point
						final double x = Double.valueOf(columns[X_COORDINATE]);
						final double y = Double.valueOf(columns[Y_COORDINATE]);
						final double z = Double.valueOf(columns[Z_COORDINATE]);

						result.add(new Coordinates(x, y, z));
					}
					else
					{
						// Log error but continue anyway.
						IJ.log("Error: group " + columns[NUCLEUS_ID] + " contains at least two central points");
					}
				}
			}
			reader.close();
		}
		catch (final FileNotFoundException e)
		{
			IJ.error("FileNotFoundException: " + e.getMessage());
			e.printStackTrace();
		}

		return result;
	}
}
