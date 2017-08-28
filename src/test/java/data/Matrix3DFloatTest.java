package data;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import testutils.BaseUtilsTest;

public class Matrix3DFloatTest extends BaseUtilsTest
{

	private void doTestNeighbourhood(final Matrix3DFloat aMatrix, final Matrix3DInt aKernel, final int aNrPoints, final int aXRadius, final int aYRadius, final int aZRadius, final int aXPosition,
			final int aYPosition, final int aZPosition)
	{
		final float[] neighbourhood = aMatrix.getPointNeighbourhood(aKernel, aNrPoints, aXPosition, aYPosition, aZPosition, aXRadius, aYRadius, aZRadius);

		int pointsCounted = 0;
		for (int z = aZPosition - aZRadius; z <= aZPosition + aZRadius; z++)
		{
			if (z >= 0 && z < aMatrix.getDepth())
			{
				for (int y = aYPosition - aYRadius; y <= aYPosition + aYRadius; y++)
				{
					if (y >= 0 && y < aMatrix.getHeight())
					{
						for (int x = aXPosition - aXRadius; x <= aXPosition + aXRadius; x++)
						{
							if (x >= 0 && x < aMatrix.getWidth())
							{
								if (aKernel.get(x - aXPosition + aXRadius, y - aYPosition + aYRadius, z - aZPosition + aZRadius) > 0)
								{
									doAssertEquals(neighbourhood[pointsCounted], aMatrix.get(x, y, z));
									pointsCounted++;
								}
							}
						}
					}
				}
			}
		}

		assertEquals(neighbourhood.length, pointsCounted);
	}


	public Matrix3DFloat randomMatrix3D()
	{
		final Random rand = new Random();
		final int x = rand.nextInt(99) + 1; // Keep it sane at max 100
		final int y = rand.nextInt(99) + 1; // Keep it sane at max 100
		final int z = rand.nextInt(99) + 1; // Keep it sane at max 100
		final float[] elements = randomFloats(x * y * z);
		return new Matrix3DFloat(x, y, z, elements);
	}


	public Matrix3DFloat randomMatrix3D(final int aMinSize)
	{
		final Random rand = new Random();
		// Keep it sane at max 100
		final int x = rand.nextInt(99 - aMinSize) + 1 + aMinSize;
		final int y = rand.nextInt(99 - aMinSize) + 1 + aMinSize;
		final int z = rand.nextInt(99 - aMinSize) + 1 + aMinSize;
		final float[] elements = randomFloats(x * y * z);
		return new Matrix3DFloat(x, y, z, elements);
	}


	public Matrix3DFloat randomMatrix3D(final int aX, final int aY, final int aZ)
	{
		final float[] elements = randomFloats(aX * aY * aZ);
		return new Matrix3DFloat(aX, aY, aZ, elements);
	}


	@Test
	public void testAddElements()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		final Matrix3DFloat dup = randomMatrix3D(matrix.getWidth(), matrix.getHeight(), matrix.getDepth());
		final Matrix3DFloat sum = matrix.addElements(dup);
		for (int i = 0; i < matrix.getSize(); i++)
		{
			doAssertEquals(sum.get(i), matrix.get(i) + dup.get(i));
		}
	}


	@Test
	public void testCopy()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		final Matrix3DFloat dup = matrix.copy();
		for (int i = 0; i < matrix.getSize(); i++)
		{
			doAssertEquals(matrix.get(i), dup.get(i));
			doAssertEquals(matrix.getWidth(), dup.getWidth());
			doAssertEquals(matrix.getHeight(), dup.getHeight());
			doAssertEquals(matrix.getDepth(), dup.getDepth());
		}
	}


	@Test
	public void testDivide()
	{
		final Random rand = new Random();
		final int size = rand.nextInt(99) + 1;
		final float[] elements = randomFloats((int) Math.pow(size, 3));
		final Matrix3DFloat matrix = new Matrix3DFloat(size, size, size, elements.clone());
		final double div = rand.nextDouble() * size;
		matrix.divide(div);
		for (int i = 0; i < elements.length; i++)
		{
			doAssertEquals(elements[i] / div, matrix.get(i));
		}
	}


	@Test
	public void testFilter()
	{
		final Random rand = new Random();
		final int width = rand.nextInt(80) + 20;
		final int height = rand.nextInt(80) + 20;
		final int depth = rand.nextInt(80) + 20;
		final Matrix3DFloat matrix = randomMatrix3D(width, height, depth);
		final Matrix3DFloat origElements = matrix.copy();
		final int halfSize = rand.nextInt(5);
		final int filtSize = (halfSize * 2) + 1;
		final Matrix3DFloat filter = randomMatrix3D(filtSize, filtSize, filtSize);
		matrix.filter(filter);
		for (int z = 0; z < depth; z++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					float sumFilter = 0;

					for (int fz = 0; fz < filtSize; fz++)
					{
						for (int fy = 0; fy < filtSize; fy++)
						{
							for (int fx = 0; fx < filtSize; fx++)
							{
								final float elemProd = filter.get(fx, fy, fz) * origElements.getWithMirror(x - halfSize + fx, y - halfSize + fy, z - halfSize + fz);
								sumFilter += elemProd;
							}
						}
					}

					doAssertEquals(sumFilter, matrix.get(x, y, z));
				}
			}
		}
	}


	@Test
	public void testGetIndex()
	{
		final int size = new Random().nextInt(99) + 1;
		final float[] elements = randomFloats((int) Math.pow(size, 3));
		final Matrix3DFloat matrix = new Matrix3DFloat(size, size, size, elements.clone());
		for (int i = 0; i < elements.length; i++)
		{
			doAssertEquals(elements[i], matrix.get(i));
		}
	}


	@Test
	public void testGetMaximaBorder()
	{
		final Matrix3DFloat matrix = new Matrix3DFloat(10, 10, 10);

		matrix.put(5, 5, 5, 101);
		matrix.put(0, 0, 0, 101);

		final List<PointValue> maxima = matrix.getMaxima(6, 6, 6, 0, 9, 1);
		assertEquals(2, maxima.size());
	}


	@Test
	public void testGetMaximaSimple()
	{
		final Matrix3DFloat matrix = new Matrix3DFloat(100, 100, 100);
		for (int i = 10; i < 100; i += 10)
		{
			for (int j = 10; j < 100; j += 10)
			{
				for (int k = 10; k < 100; k += 10)
				{
					matrix.put(i, j, k, 100);
				}
			}
		}

		matrix.put(50, 50, 50, 101);

		List<PointValue> maxima = matrix.getMaxima(5, 5, 5, 0, 99, 1);
		assertEquals(729, maxima.size());

		// Check that the minimum works
		maxima = matrix.getMaxima(5, 5, 5, 0, 99, 101);
		assertEquals(1, maxima.size());
	}


	@Test
	public void testGetMaximaSubPoints()
	{
		final Matrix3DFloat matrix = new Matrix3DFloat(100, 100, 100);
		for (int i = 10; i < 100; i += 10)
		{
			for (int j = 10; j < 100; j += 10)
			{
				for (int k = 10; k < 100; k += 10)
				{
					// Add points at the radius range of the getMaxima method, it should ignore them.
					matrix.put(i, j, k, 100);
					matrix.put(i + 5, j, k, 99);
					matrix.put(i, j + 5, k, 99);
					matrix.put(i, j, k + 5, 99);
				}
			}
		}

		final List<PointValue> maxima = matrix.getMaxima(5, 5, 5, 0, 99, 1);
		assertEquals(729, maxima.size());
	}


	@Test
	public void testGetPointNeighbourhood()
	{
		final int radX = 5;
		final int radY = 4;
		final int radZ = 3;
		final Matrix3DFloat matrix = randomMatrix3D(50);
		final Matrix3DInt kernel = Filters.createKernelEllipsoid(radX, radY, radZ);
		final int nrPoints = Filters.getNPixelsFromKernel(kernel);

		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 0, 0, 0);
		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 49, 49, 49);
		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 20, 20, 20);
		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 20, 20, 0);
		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 20, 0, 20);
		doTestNeighbourhood(matrix, kernel, nrPoints, radX, radY, radZ, 0, 20, 20);
	}


	@Test
	public void testGetWithMirror()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		final int width = matrix.getWidth();
		final int height = matrix.getHeight();
		final int depth = matrix.getDepth();
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < depth; z++)
				{
					final double realValue = matrix.get(x, y, z);
					doAssertEquals(matrix.getWithMirror(-(x + 1), y, z), realValue);
					doAssertEquals(matrix.getWithMirror(x, -(y + 1), z), realValue);
					doAssertEquals(matrix.getWithMirror(x, y, -(z + 1)), realValue);
					doAssertEquals(matrix.getWithMirror(-(x + 1), -(y + 1), -(z + 1)), realValue);
					doAssertEquals(matrix.getWithMirror(2 * width - x - 1, y, z), realValue);
					doAssertEquals(matrix.getWithMirror(x, 2 * height - y - 1, z), realValue);
					doAssertEquals(matrix.getWithMirror(x, y, 2 * depth - z - 1), realValue);
					doAssertEquals(matrix.getWithMirror(2 * width - x - 1, 2 * height - y - 1, 2 * depth - z - 1), realValue);
					doAssertEquals(matrix.getWithMirror(-(x + 1), y, 2 * depth - z - 1), realValue);
				}
			}
		}
	}


	@Test
	public void testGetXYZ()
	{
		final int size = new Random().nextInt(99) + 1;
		final int sizeSq = size * size;
		final float[] elements = randomFloats((int) Math.pow(size, 3));

		final Matrix3DFloat matrix = new Matrix3DFloat(size, size, size, elements.clone());
		for (int x = 0; x < size; x++)
		{
			for (int y = 0; y < size; y++)
			{
				for (int z = 0; z < size; z++)
				{
					doAssertEquals(elements[x + size * y + sizeSq * z], matrix.get(x, y, z));
				}
			}
		}
	}


	@Test
	public void testMultiply()
	{
		final Random rand = new Random();
		final int size = rand.nextInt(99) + 1;
		final float[] elements = randomFloats((int) Math.pow(size, 3));
		final Matrix3DFloat matrix = new Matrix3DFloat(size, size, size, elements.clone());
		final double mult = rand.nextDouble() * size;
		matrix.multiply(mult);
		for (int i = 0; i < elements.length; i++)
		{
			doAssertEquals(mult * elements[i], matrix.get(i));
		}
	}


	@Test
	public void testMultiplyElements()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		final Matrix3DFloat dup = randomMatrix3D(matrix.getWidth(), matrix.getHeight(), matrix.getDepth());
		final Matrix3DFloat product = matrix.multiplyElements(dup);
		for (int i = 0; i < matrix.getSize(); i++)
		{
			doAssertEquals(product.get(i), matrix.get(i) * dup.get(i));
		}
	}


	@Test
	public void testPutDoubleIndex()
	{
		final Random rand = new Random();
		final Matrix3DFloat matrix = randomMatrix3D();
		final int index = rand.nextInt(matrix.getSize());
		final float input = rand.nextFloat() * index;
		matrix.put(index, input);
		doAssertEquals(input, matrix.get(index));
	}


	@Test
	public void testPutDoubleXYZ()
	{
		final Random rand = new Random();
		final Matrix3DFloat matrix = randomMatrix3D();
		final int indexX = rand.nextInt(matrix.getWidth());
		final int indexY = rand.nextInt(matrix.getHeight());
		final int indexZ = rand.nextInt(matrix.getDepth());
		final float input = rand.nextFloat() * indexX;
		matrix.put(indexX, indexY, indexZ, input);
		doAssertEquals(input, matrix.get(indexX, indexY, indexZ));
	}


	@Test
	public void testReset()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		matrix.reset();
		for (int i = 0; i < matrix.getSize(); i++)
		{
			doAssertEquals(matrix.get(i), 0.0);
		}
	}


	@Test
	public void testSetSlice()
	{
		final Matrix3DFloat matrix = randomMatrix3D();
		final int width = matrix.getWidth();
		final int height = matrix.getHeight();
		final float[] slice = randomFloats(width * height);
		final int sliceNr = new Random().nextInt(matrix.getDepth());
		matrix.setSlice(sliceNr, slice);
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				doAssertEquals(slice[x + (width * y)], matrix.get(x, y, sliceNr));
			}
		}

	}


	@Test
	public void testSumElements()
	{
		final Random rand = new Random();
		final int size = rand.nextInt(99) + 1;
		final float[] elements = randomFloats((int) Math.pow(size, 3));
		final Matrix3DFloat matrix = new Matrix3DFloat(size, size, size, elements.clone());
		float sum = 0;
		for (int i = 0; i < elements.length; i++)
		{
			sum += elements[i];
		}
		doAssertEquals(sum, matrix.sumElements());
	}

}
