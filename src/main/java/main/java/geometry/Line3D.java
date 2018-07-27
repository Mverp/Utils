package geometry;

import data.Coordinates;
import utils.MyMath;

public class Line3D
{
	private final double[] slopes;
	private final double[] offsets;


	/**
	 * Get the 3D line information given two points, where a line is of the form
	 * z = ax + by + c.
	 *
	 * @param aPoint1
	 *            The first line point Coordinates
	 * @param aPoint2
	 *            The second line point Coordinates
	 * @return The line that crosses the two points.
	 */
	public Line3D(final Coordinates aPoint1, final Coordinates aPoint2)
	{
		final double x1 = aPoint1.getXcoordinate();
		final double y1 = aPoint1.getYcoordinate();
		final double z1 = aPoint1.getZcoordinate();
		final double x2 = aPoint2.getXcoordinate();
		final double y2 = aPoint2.getYcoordinate();
		final double z2 = aPoint2.getZcoordinate();

		this.slopes = new double[3];
		this.offsets = new double[3];
		this.slopes[0] = x2 - x1;
		this.slopes[1] = y2 - y1;
		this.slopes[2] = z2 - z1;
		this.offsets[0] = x1;
		this.offsets[1] = y1;
		this.offsets[2] = z1;
	}


	public Line3D(final double[] aSlopes, final double[] aOffsets)
	{
		this.slopes = aSlopes;
		this.offsets = aOffsets;
	}


	public double getXOffset()
	{
		return this.offsets[0];
	}


	public double getXSlope()
	{
		return this.slopes[0];
	}


	/**
	 * Get the x and y coordinates matching the z-coordinate given.
	 *
	 * @param aZ
	 *            The z-coordinate
	 * @return A double array of the x- and y-coordinate on this line
	 *         corresponding to the z-coordinate or null if the line is strictly
	 *         in the XY direction (i.e. no z-component).
	 */
	public double[] getXY(final double aZ)
	{
		if (!MyMath.isAboutZero(getZSlope()))
		{
			final double t = (aZ - getZOffset()) / getZSlope();
			final double[] result = new double[2];
			result[0] = (getXSlope() * t + getXOffset());
			result[1] = (getYSlope() * t + getYOffset());
			return result;
		}
		return null;
	}


	/**
	 * Get the x- and z- coordinates matching the y-coordinate given.
	 *
	 * @param aY
	 *            The y-coordinate
	 * @return A double array of the x- and z-coordinate on this line
	 *         corresponding to the y-coordinate or null if the line is strictly
	 *         in the XZ direction (i.e. no y-component).
	 */
	public double[] getXZ(final double aY)
	{
		if (MyMath.isAboutZero(getYSlope()))
		{
			final double t = (aY - getYOffset()) / getYSlope();
			final double[] result = new double[2];
			result[0] = (getXSlope() * t + getXOffset());
			result[1] = (getZSlope() * t + getZOffset());
			return result;
		}
		return null;
	}


	public double getYOffset()
	{
		return this.offsets[1];
	}


	public double getYSlope()
	{
		return this.slopes[1];
	}


	/**
	 * Get the y and z coordinates matching the x-coordinate given.
	 *
	 * @param aX
	 *            The x-coordinate
	 * @return A double array of the y- and z-coordinate on this line
	 *         corresponding to the x-coordinate or null if the line is strictly
	 *         in the YZ direction (i.e. no x-component).
	 */
	public double[] getYZ(final double aX)
	{
		if (MyMath.isAboutZero(getXSlope()))
		{
			final double t = (aX - getXOffset()) / getXSlope();
			final double[] result = new double[2];
			result[0] = (getYSlope() * t + getYOffset());
			result[1] = (getZSlope() * t + getZOffset());
			return result;
		}
		return null;
	}


	public double getZOffset()
	{
		return this.offsets[2];
	}


	public double getZSlope()
	{
		return this.slopes[2];
	}
}
