package utils;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import data.Coordinates;
import ij.ImageStack;

public class MyMathTest // extends BaseUtilsTest
{

	/**
	 * Draw a black spot on an image. For debugging purposes only.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the circle
	 * @param aImageStack
	 *            The ImageStack in which the circle will be drawn
	 * @param aRadius
	 *            The radius of the circle
	 */
	public static void drawDarkCircle(final Coordinates aCoord, final ImageStack aImageStack, final int aRadius)
	{
		final double xCoord = aCoord.getXcoordinate();
		final double yCoord = aCoord.getYcoordinate();
		final double zCoord = aCoord.getZcoordinate();
		final int rad = aRadius * aRadius;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
			{
				final int dist = x * x + y * y;
				if (dist <= rad)
				{
					if (xCoord + x >= 0 && yCoord + y >= 0 //
							&& xCoord + x < aImageStack.getWidth() && yCoord + y < aImageStack.getHeight())
					{
						aImageStack.setVoxel((int) xCoord + x, (int) yCoord + y, (int) zCoord, 0);
					}
				}
			}
	}


	protected double[] randomDoubles(final int aSize)
	{
		final double[] result = new double[aSize];
		final Random rand = new Random();
		for (int i = 0; i < aSize; i++)
		{
			result[i] = rand.nextDouble();
		}

		return result;
	}


	@Test
	public void testCircleCenter()
	{
		// Try different combinations of equal x or y coordinates
		Coordinates center = MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(0, 1), new Coordinates(1, 0));
		assertEquals(0.5, center.getXcoordinate(), 0);
		assertEquals(0.5, center.getYcoordinate(), 0);
		center = MyMath.circleCentre(new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 0));
		assertEquals(0.5, center.getXcoordinate(), 0);
		assertEquals(0.5, center.getYcoordinate(), 0);
		center = MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(0, 0), new Coordinates(1, 0));
		assertEquals(0.5, center.getXcoordinate(), 0);
		assertEquals(0.5, center.getYcoordinate(), 0);
		center = MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(0, 1), new Coordinates(0, 0));
		assertEquals(0.5, center.getXcoordinate(), 0);
		assertEquals(0.5, center.getYcoordinate(), 0);

		// Make sure that in line -> null
		assertNull(MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(2, 1), new Coordinates(3, 1)));
		assertNull(MyMath.circleCentre(new Coordinates(1, 2), new Coordinates(1, 6), new Coordinates(1, 3.5)));

		// Double negative check
		center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2400) - 20), new Coordinates(Math.sqrt(2100) - 10, 0), new Coordinates(20, 20));
		assertEquals(-10, center.getXcoordinate(), 0.0001);
		assertEquals(-20, center.getYcoordinate(), 0.0001);

		// Two points at the other side ( (0,58.xx) and (50,50) for example are smaller in y then (20,70))
		center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2400) + 20), new Coordinates(Math.sqrt(2100) + 10, 0), new Coordinates(50, 50));
		assertEquals(10, center.getXcoordinate(), 0.0001);
		assertEquals(20, center.getYcoordinate(), 0.0001);

		// With a switched X and Y input should work as well.
		center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2100) + 10), new Coordinates(Math.sqrt(2400) + 20, 0), new Coordinates(50, 50));
		assertEquals(20, center.getXcoordinate(), 0.0001);
		assertEquals(10, center.getYcoordinate(), 0.0001);
	}


	@Test
	public void testCircleSphere()
	{
		// Try different combinations of equal x or y coordinates
		final Coordinates center = MyMath.sphereCentre(new Coordinates(100, 100, 25), new Coordinates(0, 100, 25), new Coordinates(100, 0, 25), new Coordinates(50, 50, 25 + 0.5 * Math.sqrt(5000)), 2);
		assertEquals(50, center.getXcoordinate(), 0);
		assertEquals(50, center.getYcoordinate(), 0);
		assertEquals(25, center.getZcoordinate(), 0);
		// center = MyMath.circleCentre(new Coordinates(0, 0), new Coordinates(0, 1), new Coordinates(1, 0));
		// assertEquals(0.5, center.getXcoordinate(), 0);
		// assertEquals(0.5, center.getYcoordinate(), 0);
		// center = MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(0, 0), new Coordinates(1, 0));
		// assertEquals(0.5, center.getXcoordinate(), 0);
		// assertEquals(0.5, center.getYcoordinate(), 0);
		// center = MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(0, 1), new Coordinates(0, 0));
		// assertEquals(0.5, center.getXcoordinate(), 0);
		// assertEquals(0.5, center.getYcoordinate(), 0);
		//
		// // Make sure that in line -> null
		// assertNull(MyMath.circleCentre(new Coordinates(1, 1), new Coordinates(2, 1), new Coordinates(3, 1)));
		// assertNull(MyMath.circleCentre(new Coordinates(1, 2), new Coordinates(1, 6), new Coordinates(1, 3.5)));
		//
		// // Double negative check
		// center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2400) - 20), new Coordinates(Math.sqrt(2100) - 10, 0), new Coordinates(20, 20));
		// assertEquals(-10, center.getXcoordinate(), 0.0001);
		// assertEquals(-20, center.getYcoordinate(), 0.0001);
		//
		// // Two points at the other side ( (0,58.xx) and (50,50) for example are smaller in y then (20,70))
		// center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2400) + 20), new Coordinates(Math.sqrt(2100) + 10, 0), new Coordinates(50, 50));
		// assertEquals(10, center.getXcoordinate(), 0.0001);
		// assertEquals(20, center.getYcoordinate(), 0.0001);
		//
		// // With a switched X and Y input should work as well.
		// center = MyMath.circleCentre(new Coordinates(0, Math.sqrt(2100) + 10), new Coordinates(Math.sqrt(2400) + 20, 0), new Coordinates(50, 50));
		// assertEquals(20, center.getXcoordinate(), 0.0001);
		// assertEquals(10, center.getYcoordinate(), 0.0001);
	}


	//
	// @Test
	// public void testConvertFloatsToDoubles()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testCross()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testDeterminant()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testDot()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testGet2DLineFromTwoPoints()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testGetMaximum()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testGetPerpendicularLine()
	// {
	// fail("Not yet implemented");
	// }
	//
	//
	// @Test
	// public void testGetPlanarDeterminant()
	// {
	// fail("Not yet implemented");
	// }
	//

	@Test
	public void testDoCircleCenter()
	{
		Coordinates centerTot = new Coordinates(0, 0, 0);
		Coordinates center = MyMath.circleCentre(new Coordinates(371, 1.2), new Coordinates(292, 35.8), new Coordinates(65, 22.8));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(379, 309), new Coordinates(487, 69), new Coordinates(617, 284));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		final double radius = Math.sqrt(Math.pow(centerTot.getXcoordinate() - 379.0, 2) + Math.pow(centerTot.getYcoordinate() - 309.0, 2) + Math.pow(centerTot.getZcoordinate() - 21.0, 2));
		System.out.println("" + center);
		System.out.println("" + centerTot);
		System.out.println("" + radius + "\n");
		// drawDarkCircle(centerTot, IJ.getImage().getImageStack(), (int) radius); // Show circle for debug purposes

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(546, 1), new Coordinates(412, 36), new Coordinates(46, 15.6));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(210, 472), new Coordinates(421, 25), new Coordinates(18, 196));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(499, 1.4), new Coordinates(406, 44.4), new Coordinates(33, 51));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(481, 125), new Coordinates(121, 73), new Coordinates(208, 468));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(590, 0.4), new Coordinates(329, 29.4), new Coordinates(199, 13.4));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(519, 276), new Coordinates(96, 302), new Coordinates(312, 626));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(474, 2.6), new Coordinates(454, 17.2), new Coordinates(212, 5.4));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(124, 204), new Coordinates(175, 426), new Coordinates(455, 394));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(486, 0.4), new Coordinates(238, 9.4), new Coordinates(340, 16.2));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(149, 241), new Coordinates(283, 495), new Coordinates(404, 314));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(284, 27.2), new Coordinates(80, 3.8), new Coordinates(489, 15));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(407, 432), new Coordinates(359, 130), new Coordinates(163, 390));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);

		centerTot = new Coordinates(0, 0, 0);
		center = MyMath.circleCentre(new Coordinates(568, 3.4), new Coordinates(392, 33.8), new Coordinates(62, 13));
		centerTot.setYcoordinate(center.getXcoordinate());
		centerTot.setZcoordinate(center.getYcoordinate());
		System.out.println("" + center);
		center = MyMath.circleCentre(new Coordinates(436, 139), new Coordinates(61, 295), new Coordinates(437, 456));
		centerTot.setXcoordinate(center.getXcoordinate());
		centerTot.setYcoordinate((center.getYcoordinate() + centerTot.getYcoordinate()) / 2);
		System.out.println("" + center);
		System.out.println("" + centerTot);
	}


	@Test
	public void testIsAboutZero()
	{
		assertTrue(MyMath.isAboutZero(MyMath.DOUBLE_DOUBT));
		assertTrue(MyMath.isAboutZero(-MyMath.DOUBLE_DOUBT));
		assertFalse(MyMath.isAboutZero(MyMath.DOUBLE_DOUBT + 0.00000000001));
		assertFalse(MyMath.isAboutZero((-MyMath.DOUBLE_DOUBT) - 0.00000000001));
	}


	@Test
	public void testIsZero()
	{
		final Random rand = new Random();
		final double dub = rand.nextDouble();
		assertTrue(MyMath.isZero(dub, dub));
		assertTrue(MyMath.isZero(-dub, dub));
		assertFalse(MyMath.isZero(dub, dub - 0.00000000001));
		assertFalse(MyMath.isZero((-dub), dub - 0.00000000001));
		assertFalse(MyMath.isZero(dub + 0.00000000001, dub));
		assertFalse(MyMath.isZero((-dub) - 0.00000000001, dub));
	}


	@Test
	public void testSameSign()
	{
		final double[] dubs = randomDoubles(10); // always all positive or zero.
		assertTrue(MyMath.sameSign(dubs));

		dubs[0] = -dubs[0];
		assertFalse(MyMath.sameSign(dubs));
		dubs[0] = -dubs[0];
		dubs[5] = -dubs[5];
		assertFalse(MyMath.sameSign(dubs));
		dubs[5] = -dubs[5];
		dubs[9] = -dubs[9];
		assertFalse(MyMath.sameSign(dubs));
		dubs[9] = -dubs[9];

		for (int i = 0; i < 10; i++)
		{
			dubs[i] = -dubs[i];
		}
		assertTrue(MyMath.sameSign(dubs)); // all negative

		dubs[0] = -dubs[0];
		assertFalse(MyMath.sameSign(dubs));
		dubs[0] = -dubs[0];
		dubs[5] = -dubs[5];
		assertFalse(MyMath.sameSign(dubs));
		dubs[5] = -dubs[5];
		dubs[9] = -dubs[9];
		assertFalse(MyMath.sameSign(dubs));
		dubs[9] = -dubs[9];
	}

	// @Test
	// public void testSolveLinearSystem()
	// {
	// fail("Not yet implemented");
	// }

}
