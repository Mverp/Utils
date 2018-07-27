package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import data.Coordinates;
import data.Matrix3DFloat;
import data.PointValue;
import ij.IJ;
import ij.ImagePlus;

/**
 * Method MyMaximaFinder is originated from mcib3d. This method is modified
 *
 * @author thomasb/Esther
 */
public class MaximaFinder3D
{
	// The raw image to analyze
	private Matrix3DFloat imageMat;

	// The noise tolerance around each peak
	private float noiseTolerance;

	private final float minimumValue;

	// The list of peaks
	private ArrayList<PointValue> maxima;

	// The radius XY and Z to find local maxima
	private float radX = 1.5f;
	private float radY = 1.5f;
	private float radZ = 1.5f;
	// The number of cpus to use, 0 = all
	private int nCPUs = 0;


	/**
	 * Constructor with default values for radii
	 *
	 * @param ima
	 *            The raw image
	 * @param noiseTolerance
	 *            The noise tolerance
	 */
	public MaximaFinder3D(final ImagePlus aImage, final float aNoiseTolerance, final float aMinimumValue)
	{
		this.imageMat = new Matrix3DFloat(aImage);
		this.noiseTolerance = aNoiseTolerance;
		this.minimumValue = aMinimumValue;
	}


	private void computePeaks()
	{
		IJ.log("Start maxima Finder");
		final ArrayList<PointValue> maximaTmp = getListMaxima(this.minimumValue);
		Collections.sort(maximaTmp, Collections.reverseOrder());

		IJ.log("  Number of peaks found: " + maximaTmp.size());
		IJ.log("  Removing peaks below noise");

		this.maxima = new ArrayList<>();

		int c = 1;
		final int nb = maximaTmp.size();
		for (final PointValue maximum : maximaTmp)
		{
			// Check if the point hasn't already been suppressed by now
			if (this.imageMat.get(maximum) >= 0) // && maximum.getZcoordinate() != 0 && maximum.getZcoordinate() != this.imageMat.getDepth() - 1)
			{
				// This point is an actual maximum
				this.maxima.add(maximum);
				IJ.showStatus("Processing peak " + c + "/" + nb + " " + maximum);
				IJ.log("Processing peak " + c + "/" + nb + " " + maximum + " ; " + maximum.getValue());
				c++;

				// Suppress all connected points that lie within the noise limit
				repressCloseValueNeighbours(maximum, Math.max(1, maximum.getValue() - this.noiseTolerance), -1);
			}
		}
		IJ.log("  Total peaks found: " + this.maxima.size());
		IJ.log("MaximaFinder3D finished.");
	}


	/**
	 * Get a list of all maximum points in the image. This method spreads the work over several CPUs for parallel processing.
	 *
	 * @param aMinimum
	 *
	 * @param aXRadius
	 *            The x-radius of the area in which a pont must be the local maximum
	 * @param aYRadius
	 *            The y-radius of the area in which a pont must be the local maximum
	 * @param aZRadius
	 *            The z-radius of the area in which a pont must be the local maximum
	 * @param aNCPUs
	 *            The number of CPUs to calculate on (0 = max possible on machine)
	 * @return The list of PointValue local maxima.
	 */
	private ArrayList<PointValue> getListMaxima(final float aMinimum)
	{
		// PARALLEL
		final AtomicInteger ai = new AtomicInteger(0);
		final int nrCPUs = this.nCPUs == 0 ? ThreadUtils.getNbCpus() : this.nCPUs;
		final int partition = (int) Math.ceil((double) this.imageMat.getDepth() / (double) nrCPUs);
		final Thread[] threads = ThreadUtils.createThreadArray(nrCPUs);
		@SuppressWarnings("unchecked")
		final ArrayList<PointValue>[] partialResults = new ArrayList[nrCPUs];
		for (int ithread = 0; ithread < threads.length; ithread++)
		{
			threads[ithread] = new Thread()
			{
				@Override
				public void run()
				{
					for (int k = ai.getAndIncrement(); k < nrCPUs; k = ai.getAndIncrement())
					{
						partialResults[k] = MaximaFinder3D.this.imageMat.getMaxima(MaximaFinder3D.this.radX, MaximaFinder3D.this.radY, MaximaFinder3D.this.radZ, partition * k, partition * (k + 1),
								aMinimum);
					}
				}
			};
		}
		ThreadUtils.startAndJoin(threads);

		final ArrayList<PointValue> result = new ArrayList<>();
		for (final ArrayList<PointValue> partialList : partialResults)
		{
			result.addAll(partialList);
		}

		return result;
	}


	/**
	 * Get the list of maxima. Does not recalculate if already done.
	 *
	 * @return ArrayList with PointValues to give the coordinates and values of the maxima.
	 */
	public ArrayList<PointValue> getListPeaks()
	{
		if (this.maxima == null)
		{
			computePeaks();
		}
		return this.maxima;
	}


	/**
	 * Given the seed, check all neighbours in the image if they are above the limit and if so, repress them with the replacement value and recursively check their neighbours as well.
	 *
	 * @param aSeed
	 *            The starting coordinates
	 * @param aLowerLimit
	 *            The limit that must be exceeded iin order to be repressed
	 * @param aReplacementValue
	 *            The value used to repress any neigbour that exceeds the lower limit. Choose a value that can be used later.
	 */
	private void repressCloseValueNeighbours(final Coordinates aSeed, final float aLowerLimit, final int aReplacementValue)
	{
		assert aReplacementValue < aLowerLimit : "The replacement value " + aReplacementValue + " must be lower then the lower limt " + aLowerLimit;

		final int sizeX = this.imageMat.getWidth();
		final int sizeY = this.imageMat.getHeight();
		final int sizeZ = this.imageMat.getDepth();

		// Create the working queue and add the seed.
		final ArrayList<Coordinates> queue = new ArrayList<>();
		queue.add(aSeed);
		while (!queue.isEmpty())
		{
			// Still more points to process. Get the next point.
			final Coordinates curCoord = queue.remove(0);
			// IJ.log("Processing " + curCoord + " ; " + this.imageMat.get(curCoord));
			// Set the replacement value in the current point so it won't come up again
			this.imageMat.put(curCoord, aReplacementValue);
			// Go over all neighbouring pixels (inc. diagonals) to check if the are similar enough
			int curZ, curY, curX;
			for (int zz = -1; zz <= 1; zz++)
			{
				curZ = (int) (curCoord.getZcoordinate() + zz);
				if ((curZ >= 0) && (curZ <= (sizeZ - 1)))
				{
					for (int yy = -1; yy <= 1; yy++)
					{
						curY = (int) (curCoord.getYcoordinate() + yy);
						if ((curY >= 0) && (curY <= (sizeY - 1)))
						{
							for (int xx = -1; xx <= 1; xx++)
							{
								curX = (int) (curCoord.getXcoordinate() + xx);
								if ((curX >= 0) && (curX <= (sizeX - 1)))
								{
									// Check if the neighbour voxel has a high enough value
									if (this.imageMat.get(curX, curY, curZ) >= aLowerLimit)
									{
										// Check, continue with this one later
										final Coordinates newCandidate = new Coordinates(curX, curY, curZ);
										if (!queue.contains(newCandidate))
										{
											// No need to check on coordinates that have been done, as all 'done' coordinates have their value set to the replacement value
											queue.add(newCandidate);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * A new image to process. Also resets the maxima calculation.
	 *
	 * @param aImage
	 *            The image
	 */
	public void setImage(final ImagePlus aImage)
	{
		this.imageMat = new Matrix3DFloat(aImage);
		this.maxima = null;
	}


	/**
	 * Sets the number of CPUs that can be used for this calculation.
	 *
	 * @param aNCPUs
	 *            The number of CPUs
	 */
	public void setNCPUs(final int aNCPUs)
	{
		this.nCPUs = aNCPUs;
	}


	/**
	 * Set the noise tolerance. Also resets any maxima already calculated.
	 *
	 * @param aNoiseTolerance
	 *            The noise tolerance
	 */
	public void setNoiseTolerance(final float aNoiseTolerance)
	{
		this.noiseTolerance = aNoiseTolerance;
		this.maxima = null;
	}


	/**
	 * The radii to compute local maxima. The same radius is used in the X and Y directions, while the Z direction has its own setting. Also resets any maxima already calculated.
	 *
	 * @param aXRadius
	 *            The X radius to find local maxima
	 * @param aYRadius
	 *            The Y radius to find local maxima
	 * @param aZRadius
	 *            The Z radius to find local maxima
	 */
	public void setRadii(final float aXRadius, final float aYRadius, final float aZRadius)
	{
		this.radX = aXRadius;
		this.radY = aYRadius;
		this.radZ = aZRadius;
		this.maxima = null;
	}
}
