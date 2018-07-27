package data.spheroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.Coordinates;
import ij.IJ;
import ij.io.FileInfo;
import utils.MyMath;

public class SphereIO
{
	public static final String SPHERE_TXT = "_sphere.txt";


	/**
	 * Read the spheroid location from file and calculate it.
	 *
	 * @param aSpheroidFile
	 *            The name of the spheroid file
	 * @param aZFactor
	 *            The factor with which the Z-coordinates need to be adjusted when measuring distances
	 *
	 * @return The Spheroid if any found or null if no complete Spheroid has been found.
	 */
	public static Spheroid readSpheroidFile(final File aSpheroidFile, final double aZFactor)
	{
		final Map<Integer, List<Coordinates>> sphereMap = new HashMap<>();
		if (aSpheroidFile != null && aSpheroidFile.exists())
		{
			try
			{
				final FileReader fileReader = new FileReader(aSpheroidFile);
				final BufferedReader br = new BufferedReader(fileReader);
				String line;
				try
				{
					// Read lines until they run out
					while ((line = br.readLine()) != null)
					{
						final Coordinates readCoordinates = Coordinates.parseCoordinates(line);
						final int zValue = (int) readCoordinates.getZcoordinate();

						List<Coordinates> listOfPoints;
						if (!sphereMap.containsKey(zValue))
						{
							listOfPoints = new ArrayList<>();
							sphereMap.put(zValue, listOfPoints);
						}
						else
						{
							listOfPoints = sphereMap.get(zValue);
						}

						listOfPoints.add(readCoordinates);
					}

					br.close();
				}
				catch (final IOException ioe)
				{
					IJ.handleException(ioe);
				}
			}
			catch (final FileNotFoundException fnfe)
			{
				IJ.handleException(fnfe);
			}

			if (sphereMap.size() == 2)
			{
				List<Coordinates> threePoints = null;
				List<Coordinates> onePoint = null;
				final Iterator<Map.Entry<Integer, List<Coordinates>>> iterator = sphereMap.entrySet().iterator();
				while (iterator.hasNext())
				{
					final Map.Entry<Integer, List<Coordinates>> entry = iterator.next();
					final List<Coordinates> coords = entry.getValue();
					if (coords.size() == 3)
					{
						threePoints = coords;
					}
					else if (coords.size() == 1)
					{
						onePoint = coords;
					}
				}

				if (threePoints != null && onePoint != null)
				{
					final Coordinates center = MyMath.sphereCentre(threePoints.get(0), threePoints.get(1), threePoints.get(2), onePoint.get(0), aZFactor);
					final double radius = center.correctedDistanceFromPoint(threePoints.get(0), aZFactor);
					return new Spheroid(center, radius, aZFactor);
				}
			}
		}

		return null;
	}


	public static void writeSpheroidPointsToFile(final FileInfo aImageInfo, final Map<Integer, List<Coordinates>> aSpheroid)
	{
		String filename = aImageInfo.fileName;
		filename = filename.contains(".") ? filename.substring(0, filename.lastIndexOf('.')) : filename;
		filename += SPHERE_TXT;
		try
		{
			final PrintWriter resultsFile = new PrintWriter(aImageInfo.directory + File.separator + filename);

			final Iterator<Map.Entry<Integer, List<Coordinates>>> iterator = aSpheroid.entrySet().iterator();
			while (iterator.hasNext())
			{
				final List<Coordinates> coords = iterator.next().getValue();
				for (final Coordinates coord : coords)
				{
					// Write the coordinates to file
					final StringBuilder sb = new StringBuilder();
					sb.append(coord.getXcoordinate() + "\t");
					sb.append(coord.getYcoordinate() + "\t");
					sb.append(coord.getZcoordinate());
					resultsFile.println(sb.toString());
				}
			}

			resultsFile.close();
		}
		catch (final FileNotFoundException fnfEx)
		{
			IJ.handleException(fnfEx);
		}
	}
}
