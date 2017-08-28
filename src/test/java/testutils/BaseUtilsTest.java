package testutils;

import static org.junit.Assert.*;

import java.util.Random;

public class BaseUtilsTest
{

	protected static final double DOUBLE_DELTA = 0.00001;
	protected static final double DOUBLE_FLOAT = 0.00001;


	protected void doAssertEquals(final double aD1, final double aD2)
	{
		assertEquals(aD1, aD2, DOUBLE_DELTA);
	}


	protected void doAssertEquals(final float aF1, final float aF2)
	{
		assertEquals(aF1, aF2, DOUBLE_FLOAT);
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


	protected float[] randomFloats(final int aSize)
	{
		final float[] result = new float[aSize];
		final Random rand = new Random();
		for (int i = 0; i < aSize; i++)
		{
			result[i] = rand.nextFloat();
		}

		return result;
	}
}
