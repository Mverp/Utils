package data;

public class Matrix3DInt
{
	private final int[] elements;
	private final int width;
	private final int height;
	private final int depth;


	public Matrix3DInt(final int aWidth, final int aHeight, final int aDepth)
	{
		this.width = aWidth;
		this.height = aHeight;
		this.depth = aDepth;
		this.elements = new int[this.width * this.height * this.depth];
		reset();
	}


	public Matrix3DInt(final int aWidth, final int aHeight, final int aDepth, final int[] aElements)
	{
		assert aWidth * aHeight * aDepth == aElements.length : "Trying to create a Matrix3D with a set of ellements that doesn't match the matrix dimensions.";
		this.width = aWidth;
		this.height = aHeight;
		this.depth = aDepth;
		this.elements = aElements;
	}


	public Matrix3DInt addElements(final Matrix3DInt aOtherMatrix)
	{
		assert aOtherMatrix.getSize() == getSize() : "Only add matrices of equal size. This size = " + getSize() + " while the other size = " + aOtherMatrix.getSize();

		if (getSize() == aOtherMatrix.getSize())
		{
			final Matrix3DInt result = new Matrix3DInt(this.width, this.height, this.depth);
			for (int i = 0; i < this.elements.length; i++)
			{
				result.put(i, get(i) + aOtherMatrix.get(i));
			}

			return result;
		}
		else
		{
			return null;
		}
	}


	public Matrix3DInt copy()
	{
		return new Matrix3DInt(this.width, this.height, this.depth, this.elements.clone());
	}


	public void divide(final double aDouble)
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] /= aDouble;
		}
	}


	public void filter(final Matrix3DInt aFilter)
	{
		assert aFilter.getWidth() % 2 == 1 : "Only filters with an odd size are allowed";
		assert aFilter.getHeight() == aFilter.getWidth() : "Only filters with equal width and height are allowed";
		assert aFilter.getDepth() == aFilter.getHeight() : "Only filters with equal height and depth are allowed";
		final Matrix3DInt orig = this.copy();
		final Kernel3DInt kernel = new Kernel3DInt(0, 0, 0, orig, (aFilter.getWidth() - 1) / 2, true);

		do
		{
			final int[] position = kernel.getPosition();
			put(position[0], position[1], position[2], aFilter.multiplyElements(kernel).sumElements());
		} while (kernel.nextStep());
	}


	public int get(final int aIndex)
	{
		return this.elements[aIndex];
	}


	public int get(final int aX, final int aY, final int aZ)
	{
		return this.elements[aX + (this.width * aY) + (this.width * this.height * aZ)];
	}


	public int[] getAsArray()
	{
		return this.elements.clone();
	}


	public int getDepth()
	{
		return this.depth;
	}


	public int getHeight()
	{
		return this.height;
	}


	public int getSize()
	{
		return this.elements.length;
	}


	public int getWidth()
	{
		return this.width;
	}


	public int getWithMirror(final int aX, final int aY, final int aZ)
	{
		int x = aX >= 0 ? aX : -aX - 1;
		x = x < this.width ? x : ((2 * this.width) - x - 1);
		int y = aY >= 0 ? aY : -aY - 1;
		y = y < this.height ? y : ((2 * this.height) - y - 1);
		int z = aZ >= 0 ? aZ : -aZ - 1;
		z = z < this.depth ? z : ((2 * this.depth) - z - 1);
		return get(x, y, z);
	}


	public void multiply(final double aDouble)
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] *= aDouble;
		}
	}


	public Matrix3DInt multiplyElements(final Matrix3DInt aOtherMatrix)
	{
		// Check the sizes are the same. Note that this is only a limited check
		// as a 3x2x4 matrix will match a 4x3x2 matrix while it really
		// should'nt.
		if (getSize() == aOtherMatrix.getSize())
		{
			final Matrix3DInt result = new Matrix3DInt(this.width, this.height, this.depth);
			for (int i = 0; i < this.elements.length; i++)
			{
				result.put(i, get(i) * aOtherMatrix.get(i));
			}

			return result;
		}
		else
		{
			return null;
		}
	}


	public void put(final int aPos, final int aValue)
	{
		this.elements[aPos] = aValue;
	}


	public void put(final int aX, final int aY, final int aZ, final int aValue)
	{
		this.elements[aX + (this.width * aY) + (this.width * this.height * aZ)] = aValue;
	}


	public void reset()
	{
		for (int i = 0; i < this.elements.length; i++)
		{
			this.elements[i] = 0;
		}
	}


	public void setSlice(final int aZ, final int[] aSlice)
	{
		assert aSlice.length == this.width * this.height : "The size of a slice to set must be equal to the width * height of the matrix.";
		System.arraycopy(aSlice, 0, this.elements, aZ * this.width * this.height, aSlice.length);
	}


	public int sumElements()
	{
		int result = 0;
		for (final int element : this.elements)
		{
			result += element;
		}

		return result;
	}
}
