package utils;

import org.junit.Test;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import testutils.BaseUtilsTest;

public class EuclideanDistanceMapTest extends BaseUtilsTest
{

	@Test
	public void testminDist2()
	{
		final byte[] pixels = new byte[25];
		pixels[7] = 1;
		pixels[11] = 1;
		pixels[12] = 1;
		pixels[13] = 1;
		pixels[17] = 1;
		final ImageProcessor ip = new ByteProcessor(5, 5, pixels);
		final FloatProcessor fp = EuclideanDistanceMap.makeFloatEDM(ip, 1, false);
		fp.getPixel(5, 5);
	}
}
