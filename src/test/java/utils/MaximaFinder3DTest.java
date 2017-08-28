package utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import data.Coordinates;
import data.PointValue;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;

public class MaximaFinder3DTest
{

	@Test
	public void testGetListPeaksRepression()
	{
		final ImagePlus image = IJ.createImage("Test image", 100, 100, 100, 32);
		final ImageStack stack = image.getStack();
		for (int i = 10; i < 100; i += 10)
		{
			for (int j = 10; j < 100; j += 10)
			{
				for (int k = 10; k < 100; k += 10)
				{
					stack.setVoxel(i, j, k, 100);
					for (int m = 1; m < 10; m++)
					{
						stack.setVoxel(i, j, m + k, 99);
					}
				}
			}
		}

		stack.setVoxel(50, 50, 50, 101);

		final MaximaFinder3D finder = new MaximaFinder3D(image, 1, 1);
		finder.setRadii(5, 5, 5);

		final List<PointValue> maxima = finder.getListPeaks();
		assertEquals(83, maxima.size()); // Two extra due to the break on point 50 50 50

		assertTrue(maxima.contains(new Coordinates(10, 10, 10)));
		assertTrue(maxima.contains(new Coordinates(80, 40, 10)));
		assertTrue(maxima.contains(new Coordinates(50, 50, 50))); // True max in image
		assertTrue(maxima.contains(new Coordinates(50, 50, 10))); // Extra max due to 50 50 50
		assertTrue(maxima.contains(new Coordinates(50, 50, 60))); // Extra max due to 50 50 50
	}


	@Test
	public void testGetListPeaksSinglePeaks()
	{
		final ImagePlus image = IJ.createImage("Test image", 100, 100, 100, 32);
		final ImageStack stack = image.getStack();
		for (int i = 10; i < 100; i += 10)
		{
			for (int j = 10; j < 100; j += 10)
			{
				for (int k = 10; k < 100; k += 10)
				{
					stack.setVoxel(i, j, k, 100);
				}
			}
		}

		stack.setVoxel(50, 50, 50, 101);

		final MaximaFinder3D finder = new MaximaFinder3D(image, 1, 1);
		finder.setRadii(5, 5, 5);

		final List<PointValue> maxima = finder.getListPeaks();
		assertEquals(729, maxima.size());
	}
}
