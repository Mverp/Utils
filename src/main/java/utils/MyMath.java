package utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import data.Coordinates;
import geometry.Line2D;
import ij.IJ;

public final class MyMath
{
	public static double DOUBLE_DOUBT = 0.001; // The amount of wriggle room allowed in a double comparison


	/**
	 * Calculate the centre coordinate of a group of points (represented by their Coordinates) based on equal weight for each point.
	 *
	 * @param aData
	 *            The list of points (as Coordinates)
	 *
	 * @return The central Coordinates
	 */
	public static Coordinates calculateCentre(final List<Coordinates> aData)
	{
		double xSum = 0;
		double ySum = 0;
		double zSum = 0;
		for (final Coordinates point : aData)
		{
			xSum += point.getXcoordinate();
			ySum += point.getYcoordinate();
			zSum += point.getZcoordinate();
		}

		final int numPoint = aData.size();
		return new Coordinates(xSum / numPoint, ySum / numPoint, zSum / numPoint);
	}


	/**
	 * Calculate the centre of a circle based on three points in the same z-plane.
	 *
	 * @param aFirstPoint
	 *            The first point as Coordinates
	 * @param aSecondPoint
	 *            The second point as Coordinates
	 * @param aThirdPoint
	 *            The third point as Coordinates
	 * @return The Coordinates of the centre of the circle as spanned by those points
	 */
	public static Coordinates circleCentre(final Coordinates aFirstPoint, final Coordinates aSecondPoint, final Coordinates aThirdPoint)
	{
		double x1 = aFirstPoint.getXcoordinate();
		double x2 = aSecondPoint.getXcoordinate();
		double x3 = aThirdPoint.getXcoordinate();
		double y1 = aFirstPoint.getYcoordinate();
		double y2 = aSecondPoint.getYcoordinate();
		double y3 = aThirdPoint.getYcoordinate();

		if ((x1 == x2 && x2 == x3) || (y1 == y2 && y2 == y3))
		{
			// All in one line -> no circle
			return null;
		}
		else if (x1 == x2 && x2 != x3)
		{
			double temp = x2;
			x2 = x3;
			x3 = temp;
			temp = y2;
			y2 = y3;
			y3 = temp;
		}
		else if (x1 != x2 && x2 == x3)
		{
			double temp = x2;
			x2 = x1;
			x1 = temp;
			temp = y2;
			y2 = y1;
			y1 = temp;
		}

		if (y1 == y2)
		{
			double temp = x3;
			x3 = x1;
			x1 = temp;
			temp = y3;
			y3 = y1;
			y1 = temp;
		}

		final double yDelta_a = y2 - y1;
		final double xDelta_a = x2 - x1;
		final double yDelta_b = y3 - y2;
		final double xDelta_b = x3 - x2;
		final Coordinates centre = new Coordinates(0, 0);

		final double aSlope = yDelta_a / xDelta_a;
		final double bSlope = yDelta_b / xDelta_b;
		centre.setXcoordinate((aSlope * bSlope * (y1 - y3) + bSlope * (x1 + x2) - aSlope * (x2 + x3)) / (2 * (bSlope - aSlope)));
		centre.setYcoordinate(-1 * (centre.getXcoordinate() - (x1 + x2) / 2) / aSlope + (y1 + y2) / 2);

		return centre;
	}


	public static double[] convertFloatsToDoubles(final float[] input)
	{
		if (input == null)
		{
			return null;
		}
		final double[] output = new double[input.length];
		for (int i = 0; i < input.length; i++)
		{
			output[i] = input[i];
		}
		return output;
	}


	/**
	 * The cross product between two vectors.
	 *
	 * @param aC0
	 *            The coordinates describing the first vector.
	 * @param aC1
	 *            The coordinates describing the second vector.
	 *
	 * @return The cross product of the two vectors as given by a set of Coordinates (i.e., a new vector that is perpendicular to the two initial vectors and which has a size that relates to the sizes of both initial vectors).
	 */
	public static Coordinates cross(final Coordinates aC0, final Coordinates aC1)
	{
		final double x = (aC0.getYcoordinate() * aC1.getZcoordinate()) - (aC0.getZcoordinate() * aC1.getYcoordinate());
		final double y = (aC0.getZcoordinate() * aC1.getXcoordinate()) - (aC0.getXcoordinate() * aC1.getZcoordinate());
		final double z = (aC0.getXcoordinate() * aC1.getYcoordinate()) - (aC0.getYcoordinate() * aC1.getXcoordinate());
		return new Coordinates(x, y, z);
	}


	/**
	 * Takes a matrix (two dimensional array), returns determinant.
	 *
	 * @param aMatrix
	 * @return The determinant of the matrix;
	 */
	public static double determinant(final double[][] aMatrix)
	{
		int sum = 0;
		int s;
		if (aMatrix.length == 1)
		{ // bottom case of recursion. size 1 matrix determinant is itself.
			return (aMatrix[0][0]);
		}
		for (int i = 0; i < aMatrix.length; i++)
		{ // finds determinant using row-by-row expansion
			// creates smaller matrix-values not in same row or column
			final double[][] smaller = new double[aMatrix.length - 1][aMatrix.length - 1];
			for (int a = 1; a < aMatrix.length; a++)
			{
				for (int b = 0; b < aMatrix.length; b++)
				{
					if (b < i)
					{
						smaller[a - 1][b] = aMatrix[a][b];
					}
					else if (b > i)
					{
						smaller[a - 1][b - 1] = aMatrix[a][b];
					}
				}
			}
			if (i % 2 == 0)
			{ // sign changes based on i
				s = 1;
			}
			else
			{
				s = -1;
			}
			// recursive step: determinant of larger determined by smaller.
			sum += s * aMatrix[0][i] * (determinant(smaller));
		}
		return (sum); // returns determinant value. Once stack is finished,
						// returns the final determinant.
	}


	/**
	 * The dot product between two vectors.
	 *
	 * @param aC0
	 *            The coordinates describing the first vector.
	 * @param aC1
	 *            The coordinates describing the second vector.
	 *
	 * @return The (scalar) dot product between the two vectors as a double. The dot product relates to both the size of the vectors and the angle between them (by means of it's cosine).
	 */
	public static double dot(final Coordinates aC0, final Coordinates aC1)
	{
		return (aC0.getXcoordinate() * aC1.getXcoordinate() + //
				aC0.getYcoordinate() * aC1.getYcoordinate() + //
				aC0.getZcoordinate() * aC1.getZcoordinate());
	}


	/**
	 * Get the line information given two points, where a line is of the form y = ax + b. In this case 'a' is called the slope and 'b' the offset.
	 *
	 * @param aPoint1
	 *            The first line point Coordinates
	 * @param aPoint2
	 *            The second line point Coordinates
	 * @return The line that crosses the two points.
	 */
	public static Line2D get2DLineFromTwoPoints(final Coordinates aPoint1, final Coordinates aPoint2)
	{
		final double x1 = aPoint1.getXcoordinate();
		final double y1 = aPoint1.getYcoordinate();
		final double x2 = aPoint2.getXcoordinate();
		final double y2 = aPoint2.getYcoordinate();

		if (x1 != x2)
		{
			final double slope = (y2 - y1) / (x2 - x1);
			final double offset = (-x1 * slope) + y1;
			return new Line2D(slope, offset);
		}
		else
		{
			//
			return null;
		}
	}


	/**
	 * From a list of Coordinates, get the independently lowest and the highest X-, Y- and Z-coordinates.
	 * 
	 * @param aCoordinates
	 *            The list of Coordinates
	 * @return A tuple of Coordinates, with the first one containing the lowest X-, Y-, and Z-coordinates and the second one containing the highest coordinates.
	 */
	public static Coordinates[] getBoundingBox(final List<Coordinates> aCoordinates)
	{
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		double maxZ = Double.NEGATIVE_INFINITY;

		for (final Coordinates coord : aCoordinates)
		{
			double coordinate = coord.getXcoordinate();
			minX = Math.min(coordinate, minX);
			maxX = Math.max(coordinate, maxX);

			coordinate = coord.getYcoordinate();
			minY = Math.min(coordinate, minY);
			maxY = Math.max(coordinate, maxY);

			coordinate = coord.getZcoordinate();
			minZ = Math.min(coordinate, minZ);
			maxZ = Math.max(coordinate, maxZ);
		}

		final Coordinates[] result = { new Coordinates(minX, minY, minZ), new Coordinates(maxX, maxY, maxZ) };
		return result;
	}


	/**
	 * From a list of double values, calculate the kurtosis. This can be used to see the chance of outliers based on the predicted distribution of the double values given.
	 * <p>
	 * We use the following (unbiased) formula to define kurtosis (same as Apache Commons Math):
	 * </p>
	 * <p>
	 * kurtosis = { [n(n+1) / (n -1)(n - 2)(n-3)] sum[(x_i - mean)^4] / std^4 } - [3(n-1)^2 / (n-2)(n-3)]
	 * </p>
	 * <p>
	 * where n is the number of values, mean is the {@link Mean} and std is the {@link StandardDeviation}
	 * </p>
	 *
	 * @param aDoubles
	 *            The values with which the distribution and hence the kurtosis is calculated
	 * @param aMeanValue
	 *            The mean value of the distribution
	 * @param aSD
	 *            The standard deviation for te list of values
	 *
	 * @return The double kurtosis value. A higher values suggests a more 'tailed' distribution (i.e. more chance of outliers), while a low kurtosis suggests less of a chance of outliers.
	 */
	public static double getKurtosis(final List<Double> aDoubles, final double aMeanValue, final double aSD)
	{
		final double voxelCount = aDoubles.size();

		if (voxelCount > 3)
		{
			double kurtosis = 0;
			for (final Double value : aDoubles)
			{
				final double sumTemp = value - aMeanValue;
				kurtosis += sumTemp * sumTemp * sumTemp;
			}
			kurtosis /= Math.pow(aSD, 4);
			kurtosis *= (voxelCount * (voxelCount + 1)) / ((voxelCount - 1) * (voxelCount - 2) * (voxelCount - 3));
			kurtosis -= (3 * (voxelCount + 1) * (voxelCount + 1)) / ((voxelCount - 2) * (voxelCount - 3));
			return kurtosis;
		}

		return 0;
	}


	/**
	 * Get the maximum value out of an array of floats
	 *
	 * @param aArray
	 *            The array of floats (cannot be empty)
	 * @return The maximum float value in the array
	 */
	public static float getMaximum(final float[] aArray)
	{
		float max = aArray[0];
		for (final float f : aArray)
		{
			if (f > max)
			{
				max = f;
			}
		}
		return max;
	}


	/**
	 * Get the maximum value of a list of double values.
	 *
	 * @param aDoubles
	 *            The list of Double values
	 *
	 * @return The double maximum of the list
	 */
	public static double getMaximum(final List<Double> aDoubles)
	{
		double max = Double.MIN_VALUE;
		for (final double voxel : aDoubles)
		{
			if (voxel > max)
			{
				max = voxel;
			}
		}
		return max;
	}


	/**
	 * Get the mean value of a list of double values.
	 *
	 * @param aDoubles
	 *            The list of Double values
	 *
	 * @return The double mean of the list
	 */
	public static double getMean(final List<Double> aDoubles)
	{
		double mean = 0;
		for (final double voxel : aDoubles)
		{
			mean += voxel;
		}
		mean /= aDoubles.size();
		return mean;
	}


	/**
	 * Get the median value of a list of double values.
	 *
	 * @param aDoubles
	 *            The list of Double values
	 *
	 * @return The double median of the list
	 */
	public static double getMedian(final List<Double> aDoubles)
	{
		Collections.sort(aDoubles);
		final double median = aDoubles.get(aDoubles.size() / 2);
		return median;
	}


	/**
	 * Get the minimum value of a list of double values.
	 *
	 * @param aDoubles
	 *            The list of Double values
	 *
	 * @return The double minimum of the list
	 */
	public static double getMinimum(final List<Double> aDoubles)
	{
		double min = Double.MAX_VALUE;
		for (final double voxel : aDoubles)
		{
			if (voxel < min)
			{
				min = voxel;
			}
		}
		return min;
	}


	/**
	 * Calculates the 'mode' of all intensities; i.e., it gets the most common intensity value. If several values are as common, it will get the value that reaches its occurrence value first.
	 *
	 * @param aDoubles
	 *            The list of intensity values
	 *
	 * @return The double mode of the list
	 */
	public static double getMode(final List<Double> aDoubles)
	{
		final HashMap<Double, Integer> hm = new HashMap<>();
		int max = 1;
		double temp = aDoubles.get(0);

		for (final double val : aDoubles)
		{
			if (hm.get(val) != null)
			{
				int count = hm.get(val);
				count++;
				hm.put(val, count);
				if (count > max)
				{
					max = count;
					temp = val;
				}
			}
			else
			{
				hm.put(val, 1);
			}
		}
		final double mode = temp;
		return mode;
	}


	/**
	 * Get the line perpendicular to the given line crossing at the given point. The lines are given in the form y = ax + b, where 'a' is the slope and 'b' is the offset.
	 *
	 * @param aLine
	 *            The original line
	 * @param aCrossingPoint
	 *            The Coordinates of the crossing point
	 * @return The perpendicular line.
	 */
	public static Line2D getPerpendicularLine(final Line2D aLine, final Coordinates aCrossingPoint)
	{
		if (aLine.getSlope() == 0)
		{
			// result is perpendicular to x-axis and cannot be described as a Line2D
			return null;
		}

		final double slope = -1 / aLine.getSlope(); // This is the negative
													// reciprocal
		// of the original line's slope
		final double offset = (-aCrossingPoint.getXcoordinate() * slope) + aCrossingPoint.getYcoordinate();

		return new Line2D(slope, offset);
	}


	/**
	 * Gets the determinant of the following matrix. Note: if the result == 0, the four points are in the same plane.
	 *
	 * <p>
	 * |C0.x C0.y C0.z 1| <br/>
	 * |C1.x C1.y C1.z 1| <br/>
	 * |C2.x C2.y C2.z 1| <br/>
	 * |C3.x C3.y C3.z 1| <br/>
	 * </p>
	 *
	 * @param aC0
	 * @param aC1
	 * @param aC2
	 * @param aC3
	 * @return The determinant as described above
	 */
	public static double getPlanarDeterminant(final Coordinates aC0, final Coordinates aC1, final Coordinates aC2, final Coordinates aC3)
	{
		final double[][] mat = { { aC0.getXcoordinate(), aC0.getYcoordinate(), aC0.getZcoordinate(), 1 }, //
				{ aC1.getXcoordinate(), aC1.getYcoordinate(), aC1.getZcoordinate(), 1 }, //
				{ aC2.getXcoordinate(), aC2.getYcoordinate(), aC2.getZcoordinate(), 1 }, //
				{ aC3.getXcoordinate(), aC3.getYcoordinate(), aC3.getZcoordinate(), 1 } };

		return determinant(mat);
	}


	/**
	 * Get the skewness of a list of double values. Skewness is a measure of the asymmetry of the probability distribution of a real-valued random variable about its mean. A negative skew means a longer or fatter 'tail' on the left of the
	 * distribution, while a positive skew means it is larger on the right.
	 * <p>
	 * We use the following (unbiased) formula to define skewness (same as Apache Commons Math):
	 * <p>
	 * skewness = [n / (n -1) (n - 2)] sum[(x_i - mean)^3] / std^3
	 * </p>
	 * where n is the number of values, mean is the {@link getMean} and std is the {@link getStandardDeviation}
	 * </p>
	 *
	 * @param aDoubles
	 *            The list of sample values
	 * @param aMeanValue
	 *            The mean of the list
	 * @param aSD
	 *            The standard deviation of the list
	 *
	 * @return The skewness of the sample list or 0 if the list contains 2 or less values
	 */
	public static double getSkewness(final List<Double> aDoubles, final double aMeanValue, final double aSD)
	{
		final double voxelCount = aDoubles.size();

		if (voxelCount > 2)
		{
			final double sdTrip = aSD * aSD * aSD;
			double skewness = 0;
			for (final Double value : aDoubles)
			{
				final double sumTemp = value - aMeanValue;
				skewness += sumTemp * sumTemp * sumTemp;
			}
			skewness /= sdTrip;
			skewness *= voxelCount / ((voxelCount - 1) * (voxelCount - 2));
			return skewness;
		}

		return 0;
	}


	/**
	 * Get the standard deviation of a list of doubles. The mean of the list is a separate parameter.
	 *
	 * @param aDoubles
	 *            The list of which to get the SD
	 * @param aMeanValue
	 *            The mean value of the list
	 *
	 * @return The standard deviation of the list as a double.
	 */
	public static double getStandardDeviation(final List<Double> aDoubles, final double aMeanValue)
	{
		double sd = 0;
		for (final double voxel : aDoubles)
		{
			sd += (voxel - aMeanValue) * (voxel - aMeanValue);
		}
		sd /= aDoubles.size();
		sd = Math.sqrt(sd);
		return sd;
	}


	/**
	 * Tests if a double is near zero (0.001 is the range here).
	 *
	 * @param aDouble
	 *            The double to be tested
	 * @return True if the double is within the -0.001 to 0.001 range, false otherwise.
	 */
	static public boolean isAboutZero(final double aDouble)
	{
		return aDouble >= -DOUBLE_DOUBT && aDouble <= DOUBLE_DOUBT;
	}


	/**
	 * Tests if the given value is (approximately) zero.
	 *
	 * @param aValue
	 *            The value to be tested
	 * @param aThreshold
	 *            The approximation to zero that is allowed (positive !)
	 * @return True if the value is close enough to zero, false otherwise.
	 */
	public static boolean isZero(final double value, final double threshold)
	{
		return value >= -threshold && value <= threshold;
	}


	/**
	 * Factor linear equations Ax = b using LU decomposition A = LU where L is lower triangular matrix and U is upper triangular matrix.
	 *
	 * @param aMatrix
	 *            A square matrix of doubles
	 * @param aIndexes
	 *            An integer array of pivot indices index (same size as the matrix).
	 *
	 * @return The factorisation LU is in matrix A. If an error is found, the method returns false, true otherwise.
	 */
	private static boolean luFactorLinearSystem(final double[][] aMatrix, final int[] aIndexes)
	{
		final double[] scale = new double[aMatrix.length];

		int i, j, k;
		int maxI = 0;
		double largest, temp1, temp2, sum;

		//
		// Loop over rows to get implicit scaling information
		//
		for (i = 0; i < aMatrix.length; i++)
		{
			for (largest = 0.0, j = 0; j < aMatrix.length; j++)
			{
				if ((temp2 = Math.abs(aMatrix[i][j])) > largest)
				{
					largest = temp2;
				}
			}

			if (largest == 0.0)
			{
				showMathError("Unable to factor linear system");
				return false;
			}
			scale[i] = 1.0 / largest;
		}
		//
		// Loop over all columns using Crout's method
		//
		for (j = 0; j < aMatrix.length; j++)
		{
			for (i = 0; i < j; i++)
			{
				sum = aMatrix[i][j];
				for (k = 0; k < i; k++)
				{
					sum -= aMatrix[i][k] * aMatrix[k][j];
				}
				aMatrix[i][j] = sum;
			}
			//
			// Begin search for largest pivot element
			//
			for (largest = 0.0, i = j; i < aMatrix.length; i++)
			{
				sum = aMatrix[i][j];
				for (k = 0; k < j; k++)
				{
					sum -= aMatrix[i][k] * aMatrix[k][j];
				}
				aMatrix[i][j] = sum;

				if ((temp1 = scale[i] * Math.abs(sum)) >= largest)
				{
					largest = temp1;
					maxI = i;
				}
			}
			//
			// Check for row interchange
			//
			if (j != maxI)
			{
				for (k = 0; k < aMatrix.length; k++)
				{
					temp1 = aMatrix[maxI][k];
					aMatrix[maxI][k] = aMatrix[j][k];
					aMatrix[j][k] = temp1;
				}
				scale[maxI] = scale[j];
			}
			//
			// Divide by pivot element and perform elimination
			//
			aIndexes[j] = maxI;

			if (Math.abs(aMatrix[j][j]) <= Double.MIN_NORMAL)
			{
				showMathError("Unable to factor linear system");
				return false;
			}

			if (j != (aMatrix.length - 1))
			{
				temp1 = 1.0 / aMatrix[j][j];
				for (i = j + 1; i < aMatrix.length; i++)
				{
					aMatrix[i][j] *= temp1;
				}
			}
		}

		return true;
	}


	/**
	 * Solve linear equations Ax = b using LU decompostion A = LU where L is lower triangular matrix and U is upper triangular matrix. The solution vector is written directly over input load vector.
	 *
	 * <p>
	 * Note that A=LU and index[] should be generated from method LUFactorLinearSystem.
	 * </p>
	 *
	 * @param aMatrix
	 *            The factored matrix A=LU
	 * @param aIndex
	 *            The integer array of pivot indices index (size is identical to the matrix)
	 * @param aLoadVector
	 *            The "load" vector
	 */
	private static void luSolveLinearSystem(final double[][] aMatrix, final int[] aIndex, final double[] aLoadVector)
	{
		//
		// Proceed with forward and backsubstitution for L and U
		// matrices. First, forward substitution.
		//
		for (int ii = -1, i = 0; i < aMatrix.length; i++)
		{
			final int idx = aIndex[i];
			double sum = aLoadVector[idx];
			aLoadVector[idx] = aLoadVector[i];

			if (ii >= 0)
			{
				for (int j = ii; j <= (i - 1); j++)
				{
					sum -= aMatrix[i][j] * aLoadVector[j];
				}
			}
			else if (sum != 0.0)
			{
				ii = i;
			}

			aLoadVector[i] = sum;
		}
		//
		// Now, back substitution
		//
		for (int i = aMatrix.length - 1; i >= 0; i--)
		{
			double sum = aLoadVector[i];
			for (int j = i + 1; j < aMatrix.length; j++)
			{
				sum -= aMatrix[i][j] * aLoadVector[j];
			}
			aLoadVector[i] = sum / aMatrix[i][i];
		}
	}


	/**
	 * Determines if all numbers in an array are either all equal and larger than zero or all equal and smaller than zero.
	 *
	 * @param aNumbers
	 *            The list of numbers
	 *
	 * @return True if the numbers are all either >= 0 or <= 0.
	 */
	public static boolean sameSign(final double[] aNumbers)
	{
		boolean noneSmallerZero = true;
		boolean noneLargerZero = true;
		for (final double number : aNumbers)
		{
			if (number > 0)
			{
				noneLargerZero = false;
			}
			else if (number < 0)
			{
				noneSmallerZero = false;
			}
		}

		return noneSmallerZero || noneLargerZero;
	}


	private static void showMathError(final String aMessage)
	{
		IJ.showMessage("Math error!", aMessage);

	}


	/**
	 * Solve linear equations Ax = b using Crout's method.
	 *
	 * and load vector. S. The dimension of the matrix is specified in size. If error is found, method returns a 0.
	 *
	 * @param aMatrix
	 *            A square matrix of doubles. Note that this matrix will be overwritten during calculations!
	 * @param aLoadVector
	 *            A "load" vector
	 *
	 * @return The solution is written over the load vector.
	 */
	public static boolean solveLinearSystem(final double[][] aMatrix, final double[] aLoadVector)
	{
		// if we solving something simple, just solve it
		//
		if (aMatrix.length == 2)
		{
			final double det;
			final double[] y = new double[2];

			final double[][] detDouble = { { aMatrix[0][0], aMatrix[0][1] }, { aMatrix[1][0], aMatrix[1][1] } };
			det = determinant(detDouble);

			if (det == 0.0)
			{
				// Unable to solve linear system
				return false;
			}

			y[0] = (aMatrix[1][1] * aLoadVector[0] - aMatrix[0][1] * aLoadVector[1]) / det;
			y[1] = (-aMatrix[1][0] * aLoadVector[0] + aMatrix[0][0] * aLoadVector[1]) / det;

			aLoadVector[0] = y[0];
			aLoadVector[1] = y[1];
			return true;
		}
		else if (aMatrix.length == 1)
		{
			if (aMatrix[0][0] == 0.0)
			{
				// Unable to solve linear system
				return false;
			}

			aLoadVector[0] /= aMatrix[0][0];
			return true;
		}

		//
		// System of equations is not trivial, use Crout's method
		//

		// Check on allocation of working vectors
		//
		final int[] index = new int[aMatrix.length];

		//
		// Factor and solve matrix
		//
		if (!luFactorLinearSystem(aMatrix, index))
		{
			return false;
		}
		luSolveLinearSystem(aMatrix, index, aLoadVector);

		return true;
	}


	public static Coordinates sphereCentre(final Coordinates aFirstPoint, final Coordinates aSecondPoint, final Coordinates aThirdPoint, final Coordinates aZedPoint)
	{
		final Coordinates circleCentre = circleCentre(aFirstPoint, aSecondPoint, aThirdPoint);
		final double circleRadius = circleCentre.distanceFromPoint(aFirstPoint);
		Coordinates fourthVector = new Coordinates(aZedPoint.getXcoordinate() - circleCentre.getXcoordinate(), aZedPoint.getYcoordinate() - circleCentre.getYcoordinate(), 0);
		double fourthX = fourthVector.getXcoordinate();
		double fourthY = fourthVector.getYcoordinate();
		double fourthLength = Math.sqrt(fourthX * fourthX + fourthY * fourthY);
		if (fourthLength <= 0.001) // zedPoint is above the circle centre
		{
			fourthVector = new Coordinates(aFirstPoint.getXcoordinate() - circleCentre.getXcoordinate(), aFirstPoint.getYcoordinate() - circleCentre.getYcoordinate(), 0);
			fourthX = fourthVector.getXcoordinate();
			fourthY = fourthVector.getYcoordinate();
			fourthLength = circleRadius;
		}
		final Coordinates zedOnCircle = new Coordinates((fourthX * circleRadius / fourthLength) + circleCentre.getXcoordinate(),
				(fourthY * circleRadius / fourthLength) + circleCentre.getYcoordinate(), aFirstPoint.getZcoordinate());
		final Coordinates reverseZedOnCircle = new Coordinates(-(fourthX * circleRadius / fourthLength) + circleCentre.getXcoordinate(),
				-(fourthY * circleRadius / fourthLength) + circleCentre.getYcoordinate(), aFirstPoint.getZcoordinate());
		final Coordinates zedCircle = circleCentre(aZedPoint, zedOnCircle, reverseZedOnCircle);

		return new Coordinates(circleCentre.getXcoordinate(), circleCentre.getYcoordinate(), zedCircle.getZcoordinate());
	}


	/**
	 * Checks if the three points given have a pairwise
	 *
	 * @param pt1
	 * @param pt2
	 * @param pt3
	 * @return
	 */
	private boolean isPerpendicular(final Coordinates pt1, final Coordinates pt2, final Coordinates pt3)
	// Check the given point are perpendicular to x or y axis
	{
		final double yDelta_a = Math.abs(pt2.getYcoordinate() - pt1.getYcoordinate());
		final double xDelta_a = Math.abs(pt2.getXcoordinate() - pt1.getXcoordinate());
		final double yDelta_b = Math.abs(pt3.getYcoordinate() - pt2.getYcoordinate());
		final double xDelta_b = Math.abs(pt3.getXcoordinate() - pt2.getXcoordinate());

		// checking whether the line of the two pts are vertical
		if (xDelta_a <= 0.000000001 && yDelta_b <= 0.000000001)
		{
			// TRACE("The points are perpendicular and parallel to x-y axis\n");
			return false;
		}

		if (yDelta_a <= 0.0000001)
		{
			// TRACE(" A line of two point are perpendicular to x-axis 1\n");
			return true;
		}
		else if (yDelta_b <= 0.0000001)
		{
			// TRACE(" A line of two point are perpendicular to x-axis 2\n");
			return true;
		}
		else if (xDelta_a <= 0.000000001)
		{
			// TRACE(" A line of two point are perpendicular to y-axis 1\n");
			return true;
		}
		else if (xDelta_b <= 0.000000001)
		{
			// TRACE(" A line of two point are perpendicular to y-axis 2\n");
			return true;
		}
		else
			return false;
	}

}
