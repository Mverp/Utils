package data;

import java.util.ArrayList;
import java.util.Arrays;

import ij.IJ;
import ij.ImagePlus;
import utils.MyMath;
import utils.ThreadUtils;

public class Matrix3DFloat
{
	private final float[] elements;
	private final int width;
	private final int height;
	private final int depth;


	public Matrix3DFloat(final ImagePlus aImage)
	{
		this.width = aImage.getWidth();
		this.height = aImage.getHeight();
		this.depth = aImage.getNSlices();
		this.elements = aImage.getStack().getVoxels(0, 0, 0, this.width, this.height, this.depth, null);
	}


	public Matrix3DFloat(final int aWidth, final int aHeight, final int aDepth)
	{
		this.width = aWidth;
		this.height = aHeight;
		this.depth = aDepth;
		this.elements = new float[this.width * this.height * this.depth];
		reset();
	}


	public Matrix3DFloat(final int aWidth, final int aHeight, final int aDepth, final float[] aElements)
	{
		assert aWidth * aHeight * aDepth == aElements.length : "Trying to create a Matrix3D with a set of ellements that doesn't match the matrix dimensions.";
		this.width = aWidth;
		this.height = aHeight;
		this.depth = aDepth;
		this.elements = aElements;
	}


	public Matrix3DFloat(final int aWidthStart, final int aWidthEnd, final int aHeightStart, final int aHeightEnd, final int aDepthStart, final int aDepthEnd, final ImagePlus aImage,
			final int[] aBorders)
	{
		final int widthStart = aWidthStart >= 0 ? aWidthStart : 0;
		final int heightStart = aHeightStart >= 0 ? aHeightStart : 0;
		final int depthStart = aDepthStart >= 0 ? aDepthStart : 0;
		final int imageWidth = aImage.getWidth();
		final int widthEnd = aWidthEnd <= imageWidth ? aWidthEnd : imageWidth;
		final int heightEnd = aHeightEnd <= aImage.getHeight() ? aHeightEnd : aImage.getHeight();
		final int depthEnd = aDepthEnd <= aImage.getNSlices() ? aDepthEnd : aImage.getNSlices();
		this.width = widthEnd - widthStart;
		this.height = heightEnd - heightStart;
		this.depth = depthEnd - depthStart;
		this.elements = new float[this.width * this.height * this.depth];
		for (int z = depthStart; z < depthEnd; z++)
		{
			final float[] pixels = (float[]) aImage.getImageStack().getPixels(z + 1);
			for (int x = widthStart; x < widthEnd; x++)
			{
				for (int y = heightStart; y < heightEnd; y++)
				{
					this.elements[x - widthStart + (this.width * (y - heightStart)) + (this.height * this.width * (z - depthStart))] = pixels[x + (imageWidth * y)];
				}
			}
		}

		aBorders[0] = widthStart - aWidthStart;
		aBorders[1] = aWidthEnd - widthEnd;
		aBorders[2] = heightStart - aHeightStart;
		aBorders[3] = aHeightEnd - heightEnd;
		aBorders[4] = depthStart - aDepthStart;
		aBorders[5] = aDepthEnd - depthEnd;
	}


	public Matrix3DFloat addElements(final Matrix3DFloat aOtherMatrix)
	{
		assert aOtherMatrix.getSize() == getSize() : "Only add matrices of equal size. This size = " + getSize() + " while the other size = " + aOtherMatrix.getSize();

		if (getSize() == aOtherMatrix.getSize())
		{
			final Matrix3DFloat result = new Matrix3DFloat(this.width, this.height, this.depth);
			for (int i = 0; i < this.elements.length; i++)
			{
				result.put(i, get(i) + aOtherMatrix.get(i));
			}

			return result;
		}
		else
		{
			return null;
		}
	}


	public void addElementsAt(final Matrix3DFloat aOtherMatrix, final int aX, final int aY, final int aZ)
	{
		final int width = aOtherMatrix.getWidth();
		final int height = aOtherMatrix.getHeight();
		final int depth = aOtherMatrix.getDepth();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < depth; z++)
				{
					put(x + aX, y + aY, z + aZ, get(x + aX, y + aY, z + aZ) + aOtherMatrix.get(x, y, z));
				}
			}
		}
	}


	public int coordsToIndex(final int aX, final int aY, final int aZ)
	{
		return aX + (this.width * aY) + (this.width * this.height * aZ);
	}


	public Matrix3DFloat copy()
	{
		return new Matrix3DFloat(this.width, this.height, this.depth, this.elements.clone());
	}


	public void divide(final double aDouble)
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] /= aDouble;
		}
	}


	public void filter(final Matrix3DFloat aFilter)
	{
		assert aFilter.getWidth() % 2 == 1 : "Only filters with an odd size are allowed";
		assert aFilter.getHeight() == aFilter.getWidth() : "Only filters with equal width and height are allowed";
		assert aFilter.getDepth() == aFilter.getHeight() : "Only filters with equal height and depth are allowed";
		final Matrix3DFloat orig = this.copy();

		final int nrCPUs = Math.max(ThreadUtils.getNbCpus() - 1, 1);
		final Thread[] threads = ThreadUtils.createThreadArray(nrCPUs);
		final int threadPart = (this.elements.length / nrCPUs) + 1;
		for (int ithread = 0; ithread < threads.length; ithread++)
		{
			final int kernelStart = ithread * threadPart;
			final Kernel3DFloat kernel = new Kernel3DFloat(kernelStart, orig, (aFilter.getWidth() - 1) / 2);
			threads[ithread] = new Thread()
			{
				@Override
				public void run()
				{
					// Do initial step
					int[] position = kernel.getPosition();
					put(position[0], position[1], position[2], aFilter.multiplyElements(kernel).sumElements());

					int stepsTaken = 1;

					// Any other steps
					Object[] kernelStep = kernel.nextStep();
					while (kernelStep != null && stepsTaken < threadPart)
					{
						position = (int[]) kernelStep[1];
						put(position[0], position[1], position[2], aFilter.multiplyElements((Matrix3DFloat) kernelStep[0]).sumElements());
						kernelStep = kernel.nextStep();
						stepsTaken++;
					}

					System.out.println("Done filter from " + kernelStart + " to " + (kernelStart + stepsTaken));
				}
			};
		}
		ThreadUtils.startAndJoin(threads);
	}


	public float get(final Coordinates aCoordinates)
	{
		return get((int) Math.round(aCoordinates.getXcoordinate()), (int) Math.round(aCoordinates.getYcoordinate()), (int) Math.round(aCoordinates.getZcoordinate()));
	}


	public float get(final int aIndex)
	{
		return this.elements[aIndex];
	}


	public float get(final int aX, final int aY, final int aZ)
	{
		return this.elements[aX + (this.width * aY) + (this.width * this.height * aZ)];
	}


	public float[] getAsArray()
	{
		return this.elements.clone();
	}


	public int getDepth()
	{
		return this.depth;
	}


	public int getHeight()
	{
		return this.height;
	}


	/**
	 * Get the maxima out of this matrix. A given set of radii determines the area in which a point needs to have the maximum value in order to be considered a proper maximum for this method.
	 *
	 * @param aXRadius
	 *            The X radius of the local maximum area
	 * @param aYRadius
	 *            The Y radius of the local maximum area
	 * @param aZRadius
	 *            The Z radius of the local maximum area
	 * @param aMinimalZ
	 *            The minimum z layer to process (used for parallel computation)
	 * @param aMaximumZ
	 *            The maximum z layer to process (used for parallel computation)
	 * @return A list of MaximumPoints that contain both the Coordinates and the value of each maximum.
	 */
	public ArrayList<PointValue> getMaxima(final float aXRadius, final float aYRadius, final float aZRadius, int aMinimalZ, int aMaximumZ, final float aMinimum)
	{
		final ArrayList<PointValue> result = new ArrayList<>();
		final Matrix3DInt kernel = Filters.createKernelEllipsoid(aXRadius, aYRadius, aZRadius);
		final int nPix = Filters.getNPixelsFromKernel(kernel);
		if (aMinimalZ < 0)
		{
			aMinimalZ = 0;
		}
		if (aMaximumZ > getDepth())
		{
			aMaximumZ = getDepth();
		}
		float value;
		for (int k = aMinimalZ; k < aMaximumZ; k++)
		{
			IJ.showStatus("3D filter : " + (k + 1) + "/" + aMaximumZ);
			for (int j = 0; j < getHeight(); j++)
			{
				for (int i = 0; i < getWidth(); i++)
				{
					final float[] neighbourhood = getPointNeighbourhood(kernel, nPix, i, j, k, aXRadius, aYRadius, aZRadius);
					value = get(i, j, k);
					if (value >= aMinimum && value == MyMath.getMaximum(neighbourhood))
					{
						if (i == 455 && j == 674 && k == 2)
						{
							IJ.log("Binog");
						}
						result.add(new PointValue(i, j, k, value));
					}
				}
			}
		}

		return result;
	}


	/**
	 * Gets the neighbourhood values of the matrix position with a customized kernel.
	 *
	 * @param aKernelMask
	 *            The kernel mask which defines the shape of the neighbourhood that gets returned (>0 means part of the kernel)
	 * @param aNonZero
	 *            The number of non-zero values
	 * @param aX
	 *            Coordinate x of the pixel
	 * @param aY
	 *            Coordinate y of the pixel
	 * @param aZ
	 *            Coordinate z of the pixel
	 * @param aXRadius
	 *            Radius x of the neighboring
	 * @param aZRadius
	 *            Radius y of the neighboring
	 * @param aYRadius
	 *            Radius z of the neighboring
	 * @return The values of the neighbour pixels inside the kernel as an array
	 */
	public float[] getPointNeighbourhood(final Matrix3DInt aKernelMask, final int aNonZero, final int aX, final int aY, final int aZ, final float aXRadius, final float aYRadius, final float aZRadius)
	{
		final float[] pix = new float[aNonZero];
		final int vx = (int) Math.ceil(aXRadius);
		final int vy = (int) Math.ceil(aYRadius);
		final int vz = (int) Math.ceil(aZRadius);
		int resultPos = 0;
		int kernelPos = 0;
		for (int k = aZ - vz; k <= aZ + vz; k++)
		{
			for (int j = aY - vy; j <= aY + vy; j++)
			{
				for (int i = aX - vx; i <= aX + vx; i++)
				{
					if (aKernelMask.get(kernelPos) > 0)
					{
						if ((i >= 0) && (j >= 0) && (k >= 0) && (i < getWidth()) && (j < getHeight()) && (k < getDepth()))
						{
							pix[resultPos] = get(i, j, k);
							resultPos++;
						}
					}
					kernelPos++;
				}
			}
		}

		final float[] result = new float[resultPos];
		System.arraycopy(pix, 0, result, 0, resultPos);
		return result;
	}


	public int getSize()
	{
		return this.elements.length;
	}


	public float[] getSlice(final int aZ)
	{
		if (aZ < this.depth)
		{
			final int sliceSize = this.width * this.height;
			return Arrays.copyOfRange(this.elements, aZ * sliceSize, (aZ * sliceSize) + sliceSize);
		}

		return null;
	}


	public int getWidth()
	{
		return this.width;
	}


	public float getWithMirror(final int aX, final int aY, final int aZ)
	{
		int x = aX >= 0 ? aX : -aX - 1;
		x = x < this.width ? x : ((2 * this.width) - x - 1);
		int y = aY >= 0 ? aY : -aY - 1;
		y = y < this.height ? y : ((2 * this.height) - y - 1);
		int z = aZ;
		while (z < 0 || z >= this.depth)
		{
			z = z >= 0 ? z : -z - 1;
			z = z < this.depth ? z : ((2 * this.depth) - z - 1);
		}
		return get(x, y, z);
	}


	/**
	 * Given an index number of the elements array, what x, y, and z coordinates go with that?
	 *
	 * @param aIndex
	 *            The elements index number
	 * @return An array with the corresponding x, y, and z coordinates respectively.
	 */
	public int[] indexToCoords(final int aIndex)
	{
		int index = aIndex;
		final int z = index / (this.width * this.height);
		index %= (this.width * this.height);
		final int y = index / this.width;
		final int x = index % this.width;

		final int[] result = { x, y, z };
		return result;
	}


	public boolean isLocalMax(final int aX, final int aY, final int aZ, final int aRadius)
	{
		final float localMax = get(aX, aY, aZ);
		for (int x = -aRadius; x <= aRadius; x++)
		{

			for (int y = -aRadius; y <= aRadius; y++)
			{

				for (int z = -aRadius; z <= aRadius; z++)
				{
					if (!(x == 0 && y == 0 && z == 0))
					{
						final float value = get(aX + x, aY + y, aZ + z);
						if (value >= localMax)
						{
							return false;
						}
					}
				}
			}
		}

		return true;
	}


	public void multiply(final double aDouble)
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] *= aDouble;
		}
	}


	public Matrix3DFloat multiplyElements(final Matrix3DFloat aOtherMatrix)
	{
		// Check the sizes are the same. Note that this is only a limited check
		// as a 3x2x4 matrix will match a 4x3x2 matrix while it really
		// should'nt.
		if (getSize() == aOtherMatrix.getSize())
		{
			final Matrix3DFloat result = new Matrix3DFloat(this.width, this.height, this.depth);
			for (int i = 0; i < this.elements.length; i++)
			{
				result.put(i, get(i) * aOtherMatrix.get(i));
			}

			return result;
		}
		else
		{
			return null;
		}
	}


	public void put(final Coordinates aCoordinates, final float aValue)
	{
		put((int) Math.round(aCoordinates.getXcoordinate()), (int) Math.round(aCoordinates.getYcoordinate()), (int) Math.round(aCoordinates.getZcoordinate()), aValue);
	}


	public void put(final int aPos, final float aValue)
	{
		this.elements[aPos] = aValue;
	}


	public void put(final int aX, final int aY, final int aZ, final float aValue)
	{
		this.elements[aX + (this.width * aY) + (this.width * this.height * aZ)] = aValue;
	}


	public void reset()
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] = 0;
		}
	}


	public void setSlice(final int aZ, final float[] aSlice)
	{
		assert aSlice.length == this.width * this.height : "The size of a slice to set must be equal to the width * height of the matrix.";
		System.arraycopy(aSlice, 0, this.elements, aZ * this.width * this.height, aSlice.length);
	}


	public float sumElements()
	{
		float result = 0;
		for (final float element : this.elements)
		{
			result += element;
		}

		return result;
	}
}
