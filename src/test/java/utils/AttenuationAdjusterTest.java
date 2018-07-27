package utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;
import testutils.BaseUtilsTest;

public class AttenuationAdjusterTest extends BaseUtilsTest
{

	@Test
	public void testGetAttenuationAdjustments()
	{
		final int nChannels = 3;
		final int nSlices = 10;
		final int nFrames = 1;
		final int width = 25;
		final int height = 25;
		final ImagePlus image = IJ.createImage("Test image", "8-bit", width, height, nChannels, nSlices, nFrames);
		for (int c = 1; c <= nChannels; c++)
		{
			image.setPosition(c, 1, 1);
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					for (int k = 1; k <= nSlices; k++)
					{
						final ImageStack stack = image.getStack();
						final ImageProcessor proc = stack.getProcessor((k - 1) * nChannels + c);
						proc.set(i, j, Math.max(11 - k, c) * (Math.min(24 - i, 24 - j)));
					}
				}
			}
		}
		image.setC(1);

		final List<Double> adjustments = AttenuationAdjuster.getAttenuationAdjustments(image, 3, 90, 0, 255, 1, 1, CurveFitter.EXP_WITH_OFFSET);
		assertEquals(nSlices, adjustments.size());

		for (int k = 1; k <= nSlices; k++)
		{
			assertEquals(Math.max(k, nChannels) * 17, adjustments.get(k - 1), 0);
		}
	}


	@Test
	public void testGetPercentileCutoffs()
	{
		final int nChannels = 5;
		final int nSlices = 10;
		final int nFrames = 1;
		final int width = 25;
		final int height = 25;
		final ImagePlus image = IJ.createImage("Test image", "8-bit", width, height, nChannels, nSlices, nFrames);
		for (int c = 1; c <= nChannels; c++)
		{
			image.setPosition(c, 1, 1);
			for (int i = 0; i < width; i++)
			{
				for (int j = 0; j < height; j++)
				{
					for (int k = 1; k <= nSlices; k++)
					{
						final ImageStack stack = image.getStack();
						final ImageProcessor proc = stack.getProcessor((k - 1) * nChannels + c);
						proc.set(i, j, Math.max(k, c) * (Math.min(i, j)));
					}
				}
			}
		}
		image.setC(1);

		final double[] cutOffs = AttenuationAdjuster.getPercentileCutoffs(image, 3, 90.0, 0, 255, 1);
		assertEquals(nSlices, cutOffs.length);

		for (int k = 1; k <= nSlices; k++)
		{
			assertEquals(Math.max(k, 3) * 17, cutOffs[k - 1], 0);
		}
	}
}
