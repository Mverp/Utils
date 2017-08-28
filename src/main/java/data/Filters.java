package data;

public abstract class Filters
{
	/**
	 * Create an ellipsoid kernel mask where each part of the kernel gets a value of 1 and each non-part gets 0.
	 *
	 * @param aXRadius
	 *            Radius x of the ellipsoid
	 * @param aYRadius
	 *            Radius y of the ellipsoid
	 * @param aZRadius
	 *            Radius z of the ellipsoid
	 * @return The kernel mask as a Matrix3DInt
	 */
	public static Matrix3DInt createKernelEllipsoid(final float aXRadius, final float aYRadius, final float aZRadius)
	{
		final int vx = (int) Math.ceil(aXRadius);
		final int vy = (int) Math.ceil(aYRadius);
		final int vz = (int) Math.ceil(aZRadius);
		final Matrix3DInt ker = new Matrix3DInt(2 * vx + 1, 2 * vy + 1, 2 * vz + 1);
		double dist;

		final double rx2 = aXRadius != 0 ? 1.0 / (aXRadius * aXRadius) : 0;
		final double ry2 = aYRadius != 0 ? 1.0 / (aYRadius * aYRadius) : 0;
		final double rz2 = aZRadius != 0 ? 1.0 / (aZRadius * aZRadius) : 0;

		for (int k = -vz; k <= vz; k++)
		{
			for (int j = -vy; j <= vy; j++)
			{
				for (int i = -vx; i <= vx; i++)
				{
					dist = (i * i) * rx2 + (j * j) * ry2 + (k * k) * rz2;
					ker.put(i + vx, j + vy, k + vz, dist <= 1.0 ? 1 : 0);
				}
			}
		}

		return ker;
	}


	/**
	 * Get the Gaussian 3D kernel for the giiven sigma.
	 *
	 * @param aSigma
	 *            The standard deviation value for this Gaussian
	 * @return The kernel as a Matrix3DFloat
	 */
	public static Matrix3DFloat getGaussian3D(final double aSigma)
	{
		int size = 0;
		if (aSigma != 0)
		{
			size = (int) (6 * aSigma + 1);
			if (size % 2 == 0)
				size++;
		}

		final int halfwidth = size / 2;
		final Matrix3DFloat gaussianFilter = new Matrix3DFloat(size, size, size);
		final double sigSq = 2 * Math.pow(aSigma, 2);
		final double sigTr = (Math.pow(2 * Math.PI, 1.5) * Math.pow(aSigma, 3));
		for (int x = -halfwidth; x < halfwidth; x++)
		{
			for (int y = -halfwidth; y < halfwidth; y++)
			{
				for (int z = -halfwidth; z < halfwidth; z++)
				{
					final float gaussian = (float) (1.0 / sigTr * Math.exp(-(x * x + y * y + z * z) / sigSq));
					gaussianFilter.put(x + halfwidth, y + halfwidth, z + halfwidth, gaussian);
				}
			}
		}

		return gaussianFilter;
	}


	/**
	 * Get the number of mask pixels from the kernel.
	 *
	 * @param aKernel
	 *            A kernel given as a mask.
	 * @return The number of active pixels in the kernel
	 */
	public static int getNPixelsFromKernel(final Matrix3DInt aKernel)
	{
		int nb = 0;
		for (final int ker : aKernel.getAsArray())
		{
			if (ker > 0)
			{
				nb++;
			}
		}
		return nb;
	}


	/**
	 * Get the 3D Sobel filter kernel of size 3 in the x-direction.
	 *
	 * @return The kernel as a 3x3x3 Matrix3DFloat
	 */
	public static Matrix3DFloat getSobelX3D()
	{
		final float[] sobelNumbers = { -1, 0, 1, -2, 0, 2, -1, 0, 1, -2, 0, 2, -4, 0, 4, -2, 0, 2, -1, 0, 1, -2, 0, 2, -1, 0, 1 };
		return new Matrix3DFloat(3, 3, 3, sobelNumbers);
	}


	/**
	 * Get the 3D Sobel filter kernel of size 3 in the y-direction.
	 *
	 * @return The kernel as a 3x3x3 Matrix3DFloat
	 */
	public static Matrix3DFloat getSobelY3D()
	{
		final float[] sobelNumbers = { -1, -2, -1, 0, 0, 0, 1, 2, 1, -2, -4, -2, 0, 0, 0, 2, 4, 2, -1, -2, -1, 0, 0, 0, 1, 2, 1 };
		return new Matrix3DFloat(3, 3, 3, sobelNumbers);
	}


	/**
	 * Get the 3D Sobel filter kernel of size 3 in the y-direction.
	 *
	 * @return The kernel as a 3x3x3 Matrix3DFloat
	 */
	public static Matrix3DFloat getSobelZ3D()
	{
		final float[] sobelNumbers = { -1, -2, -1, -2, -4, -2, -1, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 1, 2, 4, 2, 1, 2, 1 };
		return new Matrix3DFloat(3, 3, 3, sobelNumbers);
	}
}
