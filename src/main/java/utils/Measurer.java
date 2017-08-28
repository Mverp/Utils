package utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import data.Coordinates;
import data.NuclearLink;
import data.Nucleus;
import data.NucleusEvent;
import geometry.Line2D;
import geometry.Line3D;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class Measurer
{
	public static Coordinates findProgressiveCentreOfMass(final List<Coordinates> aOriginalCoordinates, final double aPurgeFactor, final Integer aMinimumSize)
	{
		final Coordinates centreOfMass = MyMath.calculateCentre(aOriginalCoordinates);
		Coordinates result = centreOfMass;

		IJ.log("CoM = " + result);
		final int minimumSize = aMinimumSize != null ? aMinimumSize : aOriginalCoordinates.size() / 10; // 10% as default minimum size

		if (aOriginalCoordinates.size() > minimumSize)
		{
			final List<Coordinates> purgedList = new ArrayList(aOriginalCoordinates);

			purgedList.sort(new Comparator<Coordinates>()
			{
				@Override
				public int compare(final Coordinates aCoordinate1, final Coordinates aCoordinate2)
				{
					final Double c1Distance = aCoordinate1.distanceFromPoint(centreOfMass);
					final Double c2Distance = aCoordinate2.distanceFromPoint(centreOfMass);
					return c1Distance.compareTo(c2Distance);
				}
			});

			result = findProgressiveCentreOfMass(purgedList.subList(0, (int) (purgedList.size() * aPurgeFactor)), aPurgeFactor, minimumSize);
		}

		return result;
	}


	/**
	 * Get the average of the intensity values surrounding the given coordinates.
	 *
	 * @param aCoordinates
	 *            The coordinates that are the centre of the intensity check.
	 * @param aRadius
	 *            The radius of the circle around the coordinates in which the intensity is measured
	 * @param aImageProcessor
	 *            The image processor on which to measure the intensity values
	 *
	 * @return The average intensity within the user-defined minimal radius.
	 */
	public static double getAverageIntensity(final Coordinates aCoordinates, final int aRadius, final ImageProcessor aImageProcessor)
	{
		final int xCoordinate = (int) aCoordinates.getXcoordinate();
		final int yCoordinate = (int) aCoordinates.getYcoordinate();
		double intensitySum = 0;
		double validPoints = 0;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
				if (x * x + y * y <= aRadius * aRadius)
				{
					if (xCoordinate + x >= 0 && yCoordinate + y >= 0 //
							&& xCoordinate + x < aImageProcessor.getWidth() && yCoordinate + y < aImageProcessor.getHeight())
					{
						intensitySum += aImageProcessor.getPixelValue(xCoordinate + x, yCoordinate + y);
						validPoints++;
					}
				}

		return (intensitySum / validPoints);
	}


	/**
	 * Gets the average intensity in a sphere around a set of coordinates in a given channel of the image.
	 *
	 * @param aCoordinates
	 *            The centre coordinates of the sphere
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aZFactor
	 *            A ratio factor to resize the z-dimension to get a realistic representation of a sphere with a large x/y vs z resolution difference
	 * @param aImage
	 *            The ImagePlus image
	 *
	 * @return The average intensity within the given sphere
	 */
	public static double getAverageIntensity3D(final Coordinates aCoordinates, final int aRadius, final double aZFactor, final ImagePlus aImage)
	{
		final int xCoordinate = (int) aCoordinates.getXcoordinate();
		final int yCoordinate = (int) aCoordinates.getYcoordinate();
		final int zCoordinate = (int) aCoordinates.getZcoordinate();
		double intensitySum = 0;
		double validPoints = 0;
		final ImageStack stack = aImage.getStack();
		final double zRadius = aRadius / aZFactor;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
				for (int z = (int) -zRadius; z <= zRadius; z++) // Cast to int explicitely
					if ((x * x + y * y) / aRadius + (z * z) / zRadius <= 1)
					{
						if (xCoordinate + x >= 0 && yCoordinate + y >= 0 && zCoordinate + z >= 0 //
								&& xCoordinate + x < aImage.getWidth() && yCoordinate + y < aImage.getHeight() && zCoordinate + z < aImage.getNSlices())
						{
							intensitySum += stack.getVoxel(xCoordinate + x, yCoordinate + y, zCoordinate + z);
							validPoints++;
						}
					}

		return (intensitySum / validPoints);
	}


	/**
	 * Gets the average intensity in a sphere around a set of coordinates in a given channel of the image.
	 *
	 * @param aCoordinates
	 *            The centre coordinates of the sphere
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aZFactor
	 *            A ratio factor to resize the z-dimension to get a realistic representation of a sphere with a large x/y vs z resolution difference
	 * @param aChannel
	 *            The channel of the image to measure the intensity on
	 * @param aImage
	 *            The ImagePlus image
	 *
	 * @return The average intensity within the given sphere
	 */
	public static double getAverageIntensity3D(final Coordinates aCoordinates, final int aRadius, final double aZFactor, final int aChannel, final ImagePlus aImage)
	{
		final int curChannel = aImage.getC();
		final boolean switchChannels = aChannel != curChannel;
		if (switchChannels)
		{
			aImage.setC(aChannel);
		}
		final double result = getAverageIntensity3D(aCoordinates, aRadius, aZFactor, aImage);
		if (switchChannels)
		{
			aImage.setC(curChannel);
		}

		return result;
	}


	/**
	 * Get the list of intensity values in a sphere around a given image coordinate.
	 *
	 * @param aCoordinates
	 *            The centre coordinates of the sphere
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aZFactor
	 *            A ratio factor to resize the z-dimension to get a realistic representation of a sphere with a large x/y vs z resolution difference
	 * @param aImage
	 *            The ImagePlus image
	 *
	 * @return The ArrayList of Double intensity values for all coordinates falling within the sphere
	 */
	public static List<Double> getIntensity3D(final Coordinates aCoordinates, final int aRadius, final double aZFactor, final ImagePlus aImage)
	{
		final List<Double> intensities = new ArrayList<>();
		final int xCoordinate = (int) aCoordinates.getXcoordinate();
		final int yCoordinate = (int) aCoordinates.getYcoordinate();
		final int zCoordinate = (int) aCoordinates.getZcoordinate();
		final ImageStack stack = aImage.getStack();
		final double zRadius = aRadius / aZFactor;
		final double zPow = zRadius + zRadius;
		final double radPower = aRadius * aRadius;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
				for (int z = (int) -zRadius; z <= zRadius; z++) // Cast to int explicitely
					if ((x * x + y * y) / radPower + (z * z) / zPow <= 1)
					{
						if (xCoordinate + x >= 0 && yCoordinate + y >= 0 && zCoordinate + z >= 0 //
								&& xCoordinate + x < aImage.getWidth() && yCoordinate + y < aImage.getHeight() && zCoordinate + z < aImage.getNSlices())
						{
							intensities.add(stack.getVoxel(xCoordinate + x, yCoordinate + y, zCoordinate + z));
						}
					}

		return intensities;
	}


	/**
	 * Get the list of intensity values in a sphere around a coordinate in a given channel of the image.
	 *
	 * @param aCoordinates
	 *            The centre coordinates of the sphere
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aZFactor
	 *            A ratio factor to resize the z-dimension to get a realistic representation of a sphere with a large x/y vs z resolution difference
	 * @param aChannel
	 *            The channel of the image to measure the intensity on
	 * @param aImage
	 *            The ImagePlus image
	 *
	 * @return The ArrayList of Double intensity values for all coordinates falling within the sphere
	 */
	public static List<Double> getIntensity3D(final Coordinates aCoordinates, final int aRadius, final double aZFactor, final int aChannel, final ImagePlus aImage)
	{
		final int curChannel = aImage.getC();
		final boolean switchChannels = aChannel != curChannel;
		if (switchChannels)
		{
			aImage.setC(aChannel);
		}
		final List<Double> intensities = getIntensity3D(aCoordinates, aRadius, aZFactor, aImage);
		if (switchChannels)
		{
			aImage.setC(curChannel);
		}

		return intensities;
	}


	/**
	 * Get the intensity values of a list of Coordinates on a given image.
	 *
	 * @param aCoordinates
	 *            The list of Coordinates to fetch the intensitie values from
	 * @param aImage
	 *            The ImagePlus image. The current channel and time frame will be used.
	 * @return The list of Double intensity values for all the Coordinates
	 */
	public static List<Double> getIntensityCell3D(final List<Coordinates> aCoordinates, final ImagePlus aImage)
	{
		final List<Double> intensities = new ArrayList<>();
		final ImageStack stack = aImage.getStack();
		for (final Coordinates point : aCoordinates)
		{
			intensities.add(stack.getVoxel((int) point.getXcoordinate(), (int) point.getYcoordinate(), (int) point.getZcoordinate()));
		}

		return intensities;
	}


	public static double[] getLinePlotProfile2D(final NuclearLink<NucleusEvent> aLink, final ImagePlus aImage, final double aStrokeWidth)
	{
		final Coordinates start = aLink.getStartCoordinates();
		final Coordinates end = aLink.getEndCoordinates();
		final Roi oldRoi = aImage.getRoi();

		final double startX = start.getXcoordinate();
		final double startY = start.getYcoordinate();
		final double dx = end.getXcoordinate() - startX;
		final double dy = end.getYcoordinate() - startY;
		final int lineLength = (int) Math.round(Math.sqrt(dx * dx + dy * dy));
		final double xinc = dx / lineLength;
		final double yinc = dy / lineLength;

		final Line2D profileLine = MyMath.get2DLineFromTwoPoints(start, end);
		final double[] result = new double[lineLength];

		for (int step = 0; step < lineLength; step++)
		{
			// Get the coordinates for the perpendicular Roi
			final Line2D line = MyMath.getPerpendicularLine(profileLine, new Coordinates(startX + (step * xinc), startY + (step * yinc)));
			final double oneYstep = line.getY(startX + 1) - line.getY(startX);
			// The distance travelled along the line in one X-step
			final double oneLineStep = Math.sqrt(1 + (oneYstep * oneYstep));
			final double xStepsNeeded = aStrokeWidth / oneLineStep;
			final double x1 = startX + (step * xinc) - xStepsNeeded;
			final double y1 = line.getY(x1);
			final double x2 = startX + (step * xinc) + xStepsNeeded;
			final double y2 = line.getY(x2);

			final Line roi = new Line(x1, y1, x2, y2);
			roi.setStrokeWidth(1);

			aImage.setRoi(roi, false);
			result[step] = new ProfilePlot(aImage).getMax();
		}

		if (oldRoi != null)
		{
			aImage.setRoi(oldRoi);
		}
		else
		{
			aImage.deleteRoi();
		}

		return result;
	}


	/**
	 * Get a max-intensity line profile on a 3D line. Note that with the z-stack often not being contiguous, a proper line profile cannot be given. As a solution, this method will give a max intensity readout for every z-slice that the given
	 * NucleusLink touches or crosses. The readout is done in a circle of aStrokeWidth wide around the point that the NuclearLink intersects the z-slice.
	 *
	 * In case the NuclearLink is wholly within one z-slice, the getLinePlotProfile2D result is returned instead.
	 *
	 * @param aLink
	 *            The NuclearLink for which the profile will be constructed.
	 * @param aImage
	 *            The image. Note that the proper channel must have been set beforehand.
	 * @param aStrokeWidth
	 *            The width of the circle to sample the intensity values from
	 * @return A double array containing the subsequent max intensities for each z-slice around the point the NuclearLink intersects the z-slice or the 2D profile if there is no z-component to the NuclearLink.
	 */
	public static double[] getLinePlotProfile3D(final NuclearLink<Nucleus> aLink, final ImagePlus aImage, final int aStrokeWidth)
	{
		final Coordinates start = aLink.getStartCoordinates();
		final Coordinates end = aLink.getEndCoordinates();
		final double startZ = start.getZcoordinate();
		final double endZ = end.getZcoordinate();
		final double zDiff = startZ - endZ;

		if (!MyMath.isAboutZero(zDiff))
		{
			final Line3D line = new Line3D(start, end);
			final int dif = (int) Math.abs(zDiff);
			final double[] result = new double[dif + 1];
			final int direction = startZ > endZ ? -1 : 1;
			for (int zStep = 0; zStep <= dif; zStep++)
			{
				final int zCoord = (int) (startZ + (zStep * direction));
				final double[] xyCoord = line.getXY(zCoord);
				result[zStep] = getMaxIntensity(new Coordinates(xyCoord[0], xyCoord[1], zCoord), aStrokeWidth, aImage.getImageStack().getProcessor(zCoord + 1));
			}

			return result;
		}
		else
		{
			// The line is in just one level, so in practice just 2D -> return
			// 2D profile.
			return getLinePlotProfile2D(new NuclearLink<>(new NucleusEvent(aLink.getStartCoordinates()), new NucleusEvent(aLink.getEndCoordinates())), aImage, aStrokeWidth);
		}
	}


	/**
	 * Get the maximum of the intensity values surrounding the given coordinates. The all intensities used are those of the points within the minimal radius as given for the FRST algorithm by the user.
	 *
	 * @param aCoordinates
	 *            The coordinates that are the centre of the intensity check.
	 *
	 * @return The average intensity within the user-defined minimal radius.
	 */
	public static double getMaxIntensity(final Coordinates aCoordinates, final int aRadius, final ImageProcessor aImageProcessor)
	{
		final int xCoordinate = (int) aCoordinates.getXcoordinate();
		final int yCoordinate = (int) aCoordinates.getYcoordinate();
		double maxIntensity = 0;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
				if (x * x + y * y <= aRadius * aRadius)
				{
					if (xCoordinate + x >= 0 && yCoordinate + y >= 0 //
							&& xCoordinate + x < aImageProcessor.getWidth() && yCoordinate + y < aImageProcessor.getHeight())
					{
						final double curIntensity = aImageProcessor.getPixelValue(xCoordinate + x, yCoordinate + y);
						if (curIntensity > maxIntensity)
						{
							maxIntensity = curIntensity;
						}
					}
				}

		return maxIntensity;
	}

}
