package data;

/**
 * A small cubic subset of a larger Matrix3D that can be used to traverse its
 * parent. The kernel is centred around a set of coordinates within its parent
 * Matrix3D and this centre can be moved. If the kernel is moved, it will be
 * updated with data from its parent depending on its new centre location.
 *
 * @author Merijn van Erp
 *
 */
public class Kernel3DInt extends Matrix3DInt
{
	final private Matrix3DInt parent;
	final private int size;
	private int xPos;
	private int yPos;
	private int zPos;
	boolean[] directions;


	public Kernel3DInt(final int aX, final int aY, final int aZ, final Matrix3DInt aParent, final int aSize, final boolean aDirection)
	{
		super(aSize * 2 + 1, aSize * 2 + 1, aSize * 2 + 1);
		this.size = aSize;
		this.xPos = aX;
		this.yPos = aY;
		this.zPos = aZ;
		this.parent = aParent;
		this.directions = new boolean[3];
		this.directions[0] = aDirection;
		this.directions[1] = aDirection;
		this.directions[2] = aDirection;

		for (int x = -aSize; x <= aSize; x++)
		{
			for (int y = -aSize; y <= aSize; y++)
			{
				for (int z = -aSize; z <= aSize; z++)
				{
					put(aSize + x, aSize + y, aSize + z, this.parent.getWithMirror(aX + x, aY + y, aZ + z));
				}
			}
		}
	}


	public int[] getPosition()
	{
		final int[] result = { this.xPos, this.yPos, this.zPos };
		return result;
	}


	public boolean nextStep()
	{
		final boolean xContinue = this.directions[0] ? this.xPos < this.parent.getWidth() - 1 : this.xPos > 0;
		if (xContinue)
		{
			shiftX(this.directions[0]);
		}
		else
		{
			this.directions[0] = !this.directions[0]; // Flip x direction
			final boolean yContinue = this.directions[1] ? this.yPos < this.parent.getHeight() - 1 : this.yPos > 0;
			if (yContinue)
			{
				shiftY(this.directions[1]);
			}
			else
			{
				this.directions[1] = !this.directions[1]; // Flip y direction
				final boolean zContinue = this.directions[2] ? this.zPos < this.parent.getDepth() - 1 : this.zPos > 0;
				if (zContinue)
				{
					shiftZ(this.directions[2]);
				}
				else
				{
					return false;
				}
			}
		}

		return true;
	}


	public void shiftX(final boolean aToTheRight)
	{
		final int direction = aToTheRight ? 1 : -1;
		this.xPos += direction; // Move the kernel in the correct x-direction
		int widthPos = -this.size;
		int heightPos = -this.size;
		int depthPos = -this.size;
		final Matrix3DInt orig = this.copy();
		for (int x = 0; x < getSize(); x++)
		{
			// Which is the "regular" x-coordinate that just requires the shift
			// of the existing kernel values
			final boolean widthRegular = aToTheRight ? widthPos < this.size : widthPos > -this.size;
			if (widthRegular)
			{
				put(x, orig.get(x + direction));
			}
			else
			{
				// Value not in the kernel yet, get it from its parent
				put(x, this.parent.getWithMirror(this.xPos + widthPos, this.yPos + heightPos, this.zPos + depthPos));
			}

			// Update the current position
			widthPos++;
			if (widthPos > this.size)
			{
				heightPos++;
				widthPos = -this.size;
				if (heightPos > this.size)
				{
					heightPos = -this.size;
					depthPos++;
				}
			}
		}

	}


	public void shiftY(final boolean aLower)
	{
		final int direction = aLower ? 1 : -1;
		this.yPos += direction; // Move the kernel in the correct y-direction
		final int widthStep = (direction * (this.size * 2)) + direction;
		int widthPos = -this.size;
		int heightPos = -this.size;
		int depthPos = -this.size;
		final Matrix3DInt orig = this.copy();
		for (int y = 0; y < getSize(); y++)
		{
			// Which is the "regular" y-coordinate that just requires the shift
			// of the existing kernel values
			final boolean heightRegular = aLower ? heightPos < this.size : heightPos > -this.size;
			if (heightRegular)
			{
				put(y, orig.get(y + widthStep));
			}
			else
			{
				put(y, this.parent.getWithMirror(this.xPos + widthPos, this.yPos + heightPos, this.zPos + depthPos));
			}

			// Update counters
			widthPos++;
			if (widthPos > this.size)
			{
				heightPos++;
				widthPos = -this.size;
				if (heightPos > this.size)
				{
					heightPos = -this.size;
					depthPos++;
				}
			}
		}
	}


	public void shiftZ(final boolean aDown)
	{
		final int direction = aDown ? 1 : -1;
		this.zPos += direction; // Move the kernel in the correct y-direction
		final int heightStep = (int) Math.pow(this.size * 2 + 1, 2) * direction;
		int widthPos = -this.size;
		int heightPos = -this.size;
		int depthPos = -this.size;
		final Matrix3DInt orig = this.copy();
		for (int z = 0; z < getSize(); z++)
		{
			// Which is the "regular" z-coordinate that just requires the shift
			// of the existing kernel values
			final boolean depthRegular = aDown ? depthPos < this.size : depthPos > -this.size;
			if (depthRegular)
			{
				put(z, orig.get(z + heightStep));
			}
			else
			{
				put(z, this.parent.getWithMirror(this.xPos + widthPos, this.yPos + heightPos, this.zPos + depthPos));
			}

			// Update counters
			widthPos++;
			if (widthPos > this.size)
			{
				heightPos++;
				widthPos = -this.size;
				if (heightPos > this.size)
				{
					heightPos = -this.size;
					depthPos++;
				}
			}
		}
	}
}
