package utils;

import data.Coordinates;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class EuclideanDistanceMap
{
	// Handle a line; two passes: left-to-right and right-to-left
	private static void edmLine(final byte[] aSourcePixels, final float[] aDistances, final Coordinates[][] aPointBuffers, final int aWidth, int aSourceOffset, final int aCurrentY,
			final int aBackgroundValue, final int aDistanceToYEdge)
	{
		Coordinates[] points = aPointBuffers[0]; // the first buffer: for the left-to-right pass
		Coordinates pPrev = null;
		Coordinates pDiag = null; // point at (-/+1, -/+1) to current one (-1,-1 in the first pass)
		Coordinates pNextDiag;
		final boolean edgesAreBackground = aDistanceToYEdge != Integer.MAX_VALUE;
		int distSqr = Integer.MAX_VALUE; // this value is used only if edges are not background
		for (int x = 0; x < aWidth; x++, aSourceOffset++)
		{
			pNextDiag = points[x]; // Note, this is the value at the same position on the previous line. Store in variable as this may be overwritten.
			if (aSourcePixels[aSourceOffset] == aBackgroundValue)
			{
				points[x] = new Coordinates(x, aCurrentY); // remember coordinates as a candidate for nearest background point
			}
			else
			{
				// foreground pixel found. Note that this does not overwrite the point[x] of the previous line!
				if (edgesAreBackground)
					distSqr = (x + 1 < aDistanceToYEdge) ? (x + 1) * (x + 1) : aDistanceToYEdge * aDistanceToYEdge; // shortest distance from edge (depends on pass)
				final float dist2 = minDist2(points, pPrev, pDiag, x, aCurrentY, distSqr);
				if (aDistances[aSourceOffset] > dist2)
					aDistances[aSourceOffset] = dist2; // Found a shorter path. Store it.
			}
			pPrev = points[x]; // The just handled position
			pDiag = pNextDiag; // Stored value of previous line
		}
		aSourceOffset--; // now points to the last pixel in the line
		points = aPointBuffers[1]; // the second buffer: for the right-to-left pass.
		pPrev = null;
		pDiag = null;
		for (int x = aWidth - 1; x >= 0; x--, aSourceOffset--)
		{
			pNextDiag = points[x];
			if (aSourcePixels[aSourceOffset] == aBackgroundValue)
			{
				points[x] = new Coordinates(x, aCurrentY); // remember coordinates as a candidate for nearest background point
			}
			else
			{ // foreground pixel:
				if (edgesAreBackground)
					distSqr = (aWidth - x < aDistanceToYEdge) ? (aWidth - x) * (aWidth - x) : aDistanceToYEdge * aDistanceToYEdge;
				final float dist2 = minDist2(points, pPrev, pDiag, x, aCurrentY, distSqr);
				if (aDistances[aSourceOffset] > dist2)
					aDistances[aSourceOffset] = dist2; // Found a shorter path. Store it.
			}
			pPrev = points[x];
			pDiag = pNextDiag;
		}
	} // private void edmLine


	/**
	 * Creates the Euclidian Distance Map of a (binary) byte image.
	 *
	 * @param aIP
	 *            The input image, not modified; must be a ByteProcessor.
	 * @param aBackgroundValue
	 *            Pixels in the input with this value are interpreted as background. Note: for pixel value 255, write either -1 or (byte)255.
	 * @param aEdgesAreBackground
	 *            Whether out-of-image pixels are considered background
	 * @return The EDM, containing the distances to the nearest background pixel. Returns null if the thread is interrupted.
	 */
	public static FloatProcessor makeFloatEDM(final ImageProcessor aIP, final int aBackgroundValue, final boolean aEdgesAreBackground)
	{
		final int width = aIP.getWidth();
		final int height = aIP.getHeight();
		final FloatProcessor resultFloatProc = new FloatProcessor(width, height);
		final byte[] sourcePixels = (byte[]) aIP.getPixels();
		final float[] resultPixels = (float[]) resultFloatProc.getPixels(); // Not a copy! This is the actual pixel array that resultFloatProc uses.

		// First set all non-background pixels to max float io. 0 (for background)
		for (int i = 0; i < width * height; i++)
		{
			if (sourcePixels[i] != aBackgroundValue)
			{
				resultPixels[i] = Float.MAX_VALUE;
			}
		}

		final Coordinates[][] pointBufs = new Coordinates[2][width]; // two buffers for two passes
		int yDist = Integer.MAX_VALUE; // this value is used only if edges are not background
		// pass 1: increasing y
		for (int x = 0; x < width; x++)
		{
			// Reset buffers
			pointBufs[0][x] = null;
			pointBufs[1][x] = null;
		}
		for (int y = 0; y < height; y++)
		{
			if (aEdgesAreBackground)
				yDist = y + 1; // distance to nearest background point (back to y=-1 at the first pass)
			edmLine(sourcePixels, resultPixels, pointBufs, width, y * width, y, aBackgroundValue, yDist);
		}
		// pass 2: decreasing y
		for (int x = 0; x < width; x++)
		{
			// Reset buffers
			pointBufs[0][x] = null;
			pointBufs[1][x] = null;
		}
		for (int y = height - 1; y >= 0; y--)
		{
			if (aEdgesAreBackground)
				yDist = height - y; // distance to nearest background point (back to height at the second pass)
			edmLine(sourcePixels, resultPixels, pointBufs, width, y * width, y, aBackgroundValue, yDist);
		}

		resultFloatProc.sqrt();
		return resultFloatProc;
	}


	/**
	 * Calculates minimum distance^2 of x,y from the following three points:
	 * <p>
	 * - points[x] (nearest point found for previous line, same x) <\p>
	 * <p>
	 * pPrev (nearest point found for same line, previous x), and <\p>
	 * <p>
	 * pDiag (nearest point found for diagonal, i.e., previous line, previous x)
	 * </p>
	 * Sets array element points[x] to the coordinates of the point having the minimum distance to x,y If the distSqr parameter is lower than the distance^2, then distSqr is used Returns to the minimum distance^2 obtained
	 */
	private static float minDist2(final Coordinates[] aPoints, final Coordinates aPrevPoint, final Coordinates aDiagonalPoint, final int aXCoord, final int aYCoord, int aDistanceSquared)
	{
		final Coordinates p0 = aPoints[aXCoord]; // the nearest background point for the same x in the previous line
		Coordinates nearestPoint = p0;
		if (p0 != null)
		{
			final int x0 = (int) p0.getXcoordinate();
			final int y0 = (int) p0.getYcoordinate();
			final int dist1Sqr = (aXCoord - x0) * (aXCoord - x0) + (aYCoord - y0) * (aYCoord - y0);
			if (dist1Sqr < aDistanceSquared)
				aDistanceSquared = dist1Sqr;
		}
		if (aDiagonalPoint != null && !aDiagonalPoint.equals(p0))
		{
			final int xDiag = (int) aDiagonalPoint.getXcoordinate();
			final int yDiag = (int) aDiagonalPoint.getYcoordinate();
			final int distDiag = (aXCoord - xDiag) * (aXCoord - xDiag) + (aYCoord - yDiag) * (aYCoord - yDiag);
			if (distDiag < aDistanceSquared)
			{
				nearestPoint = aDiagonalPoint;
				aDistanceSquared = distDiag;
			}
		}
		if (aPrevPoint != null && !aPrevPoint.equals(aDiagonalPoint))
		{
			final int xPrev = (int) aPrevPoint.getXcoordinate();
			final int yPrev = (int) aPrevPoint.getYcoordinate();
			final int distPrev = (aXCoord - xPrev) * (aXCoord - xPrev) + (aYCoord - yPrev) * (aYCoord - yPrev);
			if (distPrev < aDistanceSquared)
			{
				nearestPoint = aPrevPoint;
				aDistanceSquared = distPrev;
			}
		}
		aPoints[aXCoord] = nearestPoint;
		return aDistanceSquared;
	}

}
