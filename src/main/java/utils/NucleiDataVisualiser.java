package utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import data.BaseNucleus;
import data.Coordinates;
import data.NuclearLink;
import data.NucleiSet2D;
import data.NucleiSet3D;
import data.Nucleus;
import data.NucleusEvent;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;
import ij.plugin.ChannelSplitter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.LUT;
import interfaces.IChartMouseEventCallback;
import ui.HistogramListener;

/**
 * Helper class to display nuclei, other image effects and data.
 *
 * @author Merijn van Erp
 *
 */
public class NucleiDataVisualiser
{
	/**
	 * Generate a bluish to greenish to reddish LUT for the display of histograms.
	 *
	 * @param ncol
	 *            the number of colours in the LUT
	 * @return the LUT
	 */
	protected static final LookupPaintScale createLUT(final int ncol)
	{
		final float[][] colors = new float[][] { { 0, 75 / 255f, 150 / 255f }, { 0.1f, 0.8f, 0.1f }, { 150 / 255f, 75 / 255f, 0 } };
		final float[] limits = new float[] { 0, 0.5f, 1 };
		final LookupPaintScale lut = new LookupPaintScale(0, 1, Color.BLACK);
		float val;
		float r, g, b;
		for (int j = 0; j < ncol; j++)
		{
			val = j / (ncol - 0.99f);
			int i = 0;
			for (i = 0; i < limits.length; i++)
			{
				if (val < limits[i])
				{
					break;
				}
			}
			i = i - 1;
			r = colors[i][0] + (val - limits[i]) / (limits[i + 1] - limits[i]) * (colors[i + 1][0] - colors[i][0]);
			g = colors[i][1] + (val - limits[i]) / (limits[i + 1] - limits[i]) * (colors[i + 1][1] - colors[i][1]);
			b = colors[i][2] + (val - limits[i]) / (limits[i + 1] - limits[i]) * (colors[i + 1][2] - colors[i][2]);
			lut.add(val, new Color(r, g, b));
		}
		return lut;
	}


	/**
	 * Draw a 2D line in the ImageProcessor the along the trajectory of the 3D NuclearLink starting in the first Nucleus of the link. This is only a partial trajectory as it is a 2D representation of a 3D line. The colour of the line will be green if
	 * the NuclearLink would have gone up and blue if it goes down. The line width can be adjusted. Since this is a 2D line, the z-coordinate is determined by the processor given.
	 *
	 * @param aLink
	 *            The NuclearLink that will be represented by the line drawn
	 * @param aOrigin
	 *            The nucleus that originates the link
	 * @param aWidth
	 *            The width that the line will have (in pixels)
	 * @param aProcessor
	 *            The ImageProcessor on which the line will be drawn. Be careful to select the correct z-slice processor!
	 */
	public static void draw3DNuclearLink2D(final NuclearLink<Nucleus> aLink, final Nucleus aOrigin, final int aWidth, final ImageProcessor aProcessor)
	{
		final int lineWidth = aProcessor.getLineWidth();
		final Color oldColour = Toolbar.getForegroundColor();
		aProcessor.setLineWidth(aWidth);
		final Coordinates startCoordinates = aOrigin.getCoordinates();
		final Coordinates endCoordinates = aLink.getTheOtherEnd(aOrigin).getCoordinates();
		Color lineColor = Color.GREEN;
		if (startCoordinates.getZcoordinate() > endCoordinates.getZcoordinate())
		{
			lineColor = Color.BLUE;
		}
		if (startCoordinates.getZcoordinate() == endCoordinates.getZcoordinate())
		{
			aProcessor.setColor(lineColor);
			final int startX = (int) startCoordinates.getXcoordinate();
			final int startY = (int) startCoordinates.getYcoordinate();
			final int midX = (int) (startX + (endCoordinates.getXcoordinate() - startX) / 2);
			final int midY = (int) (startY + (endCoordinates.getYcoordinate() - startY) / 2);
			aProcessor.drawLine(startX, startY, midX, midY);
			return;
		}

		// Restore original value
		aProcessor.setLineWidth(lineWidth);
		aProcessor.setColor(oldColour);
	}


	/**
	 * Draw a (filled) circle with the colour provided in the ImageProcessor on the coordinates of the given Nucleus. The circle will be brightest at the centre and slowly fade to black.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the circle
	 * @param aImageProcessor
	 *            The ImageProcessor in which the circle will be drawn
	 * @param aRadius
	 *            The radius of the circle
	 * @param aColour
	 *            The RGB colour value to be used
	 * @param aIsOpen
	 *            Should the circle be filled (false) or open (true)
	 */
	public static void drawCirle(final BaseNucleus aNucleus, final ImageProcessor aImageProcessor, final int aRadius, final int aColour, final boolean aIsOpen)
	{
		final double xCoord = aNucleus.getXcoordinate();
		final double yCoord = aNucleus.getYcoordinate();
		final int rad = aRadius * aRadius;
		final int radLow = (aRadius - 2) * (aRadius - 2);
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
			{
				final int dist = x * x + y * y;
				if ((dist <= rad && !aIsOpen) || (dist < rad && dist >= radLow && aIsOpen))
				{
					if (xCoord + x >= 0 && yCoord + y >= 0 //
							&& xCoord + x < aImageProcessor.getWidth() && yCoord + y < aImageProcessor.getHeight())
					{
						final int colour = aIsOpen ? aColour : lightenColour(aColour, ((aRadius - Math.sqrt(x * x + y * y) + 1) / (aRadius + 1)));
						aImageProcessor.setf((int) xCoord + x, (int) yCoord + y, colour);
					}
				}
			}
	}


	/**
	 * Draw a cube, with the colour provided, in the ImageStack, on the coordinates of the given Nucleus. Alternatively, the cube will be a on open structure. The choice of this is given with a parameter.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the cube
	 * @param aImageStack
	 *            The ImageStack in which the cube will be drawn
	 * @param aColour
	 *            The RGB colour value to be used
	 * @param aIsOpen
	 *            If true, the cube will be open, false it will be filled.
	 */
	public static void drawCube(final BaseNucleus aNucleus, final ImageStack aImageStack, final int aRadius, final int aColour, final boolean aIsOpen)
	{
		final double xCoord = aNucleus.getXcoordinate();
		final double yCoord = aNucleus.getYcoordinate();
		final double zCoord = aNucleus.getZcoordinate();
		final int lowerBound = aIsOpen ? aRadius - 1 : 0;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
			// for (int z = -aRadius; z <= aRadius; z++)
			{
				final int absX = Math.abs(x);
				final int absY = Math.abs(y);
				// final int absZ = Math.abs(z);
				if (!aIsOpen || ((absX <= aRadius && absX >= lowerBound) || (absY <= aRadius && absY >= lowerBound)))// || (absZ <= aRadius && absZ >= lowerBound)))
				{
					if (xCoord + x >= 0 && yCoord + y >= 0 && zCoord >= 0 //
							&& xCoord + x < aImageStack.getWidth() && yCoord + y < aImageStack.getHeight() && zCoord < aImageStack.getSize())
					{
						final int colour = aIsOpen ? aColour : lightenColour(aColour, ((double) aRadius - Math.max(x, y)) / aRadius);
						aImageStack.setVoxel((int) xCoord + x, (int) yCoord + y, (int) zCoord, colour);
					}
				}
			}
	}


	/**
	 * Draw a (2D) line in the ImageProcessor between the two end points of a NuclearLink. Both the colour and line width can be adjusted. Since this is a 2D line, the correct z-coordinate is determined by the processor given.
	 *
	 * @param aLink
	 *            The NuclearLink that will be represented by the line drawn
	 * @param aWidth
	 *            The width that the line will have (in pixels)
	 * @param aColour
	 *            The Colour that the line will be drawn in.
	 * @param aProcessor
	 *            The ImageProcessor on which the line will be drawn. Be careful to select the correct z-slice processor!
	 */
	public static void drawNuclearLink2D(final NuclearLink<NucleusEvent> aLink, final int aWidth, final Color aColour, final ImageProcessor aProcessor)
	{
		final int lineWidth = aProcessor.getLineWidth();
		final Color oldColour = Toolbar.getForegroundColor();
		aProcessor.setLineWidth(aWidth);
		aProcessor.setColor(aColour);
		final double[] coordinates = aLink.get2DCoordinates();
		aProcessor.drawLine((int) coordinates[0], (int) coordinates[1], (int) coordinates[2], (int) coordinates[3]);

		// Restore original value
		aProcessor.setLineWidth(lineWidth);
		aProcessor.setColor(oldColour);
	}


	/**
	 * Add the set of 2D nuclear links to an image. The image will contain one red line with the given width per nuclear link. Note that the nuclear links are expected to be in the 2D realm, so they will be drawn on a slice depending on the NucleiSet
	 * they are part of.
	 *
	 * @param aNuclei
	 *            An array of NucleiSets. One per slice of the image is expected.
	 * @param aRadius
	 *            The size of the dot that is drawn per nucleus
	 * @param aResultImage
	 *            The image which will show all the results based on the original image and the links of the NucleiSets. This image should be of the same dimensions (hxwxd) as the aOriginalImage. Any data in this image will be overwritten, but will
	 *            keep showing it's current slice and zoom.
	 * @param aOriginalImage
	 *            The image which is taken as the base for adding the links to. The image is not changed, just copied.
	 *
	 * @return The result image, a copy of the original image, with the nuclear links added onto it.
	 */
	public static void drawNuclearLinkData2D(final NucleiSet2D[] aNuclei, final int aWidth, final ImagePlus aResultImage, final ImagePlus aOriginalImage)
	{
		final ImagePlus intermediateImage = NucleiDataVisualiser.getChannelRGBDuplicate(aOriginalImage, 0);

		for (int sliceNr = 1; sliceNr <= intermediateImage.getNSlices(); sliceNr++)
		{
			intermediateImage.setSlice(sliceNr);
			final ImageProcessor ip = intermediateImage.getProcessor();
			final Set<NuclearLink<NucleusEvent>> links = aNuclei[sliceNr - 1].getNuclearLinks(1);

			for (final NuclearLink<NucleusEvent> link : links)
			{
				drawNuclearLink2D(link, aWidth, Color.RED, ip);
			}

			aResultImage.getImageStack().setPixels(ip.getPixels(), sliceNr);
		}
		intermediateImage.changes = false;
		intermediateImage.close();

		if (!aResultImage.isVisible())
		{
			aResultImage.setTitle("Result image");
			aResultImage.show();
		}

		aResultImage.updateAndRepaintWindow();
	}


	/**
	 * Add the set of 3D nuclear links to an image. The image will contain one blue/green line with the given width per nuclear link. Note that the nuclear links are in the 3D realm, so they cannot be fully represented in the 2D slices of ImageJ.
	 * Instead, each link will be drawn starting from each nucleus and going in the x/y direction for a short bit. The colour will determine in the link goes up (green) or down (blue).
	 *
	 * @param aNuclei
	 *            An array of NucleiSets. One per slice of the image is expected.
	 * @param aRadius
	 *            The size of the dot that is drawn per nucleus
	 * @param aResultImage
	 *            The image which will show all the results based on the original image and the links of the NucleiSets. This image should be of the same dimensions (hxwxd) as the aOriginalImage. Any data in this image will be overwritten, but will
	 *            keep showing it's current slice and zoom.
	 * @param aOriginalImage
	 *            The image which is taken as the base for adding the links to. The image is not changed, just copied.
	 *
	 * @return The result image, a copy of the original image, with the nuclear links added onto it.
	 */
	public static void drawNuclearLinkData3D(final NucleiSet3D aNuclei, final int aWidth, final ImagePlus aResultImage, final ImagePlus aOriginalImage)
	{
		final ImagePlus intermediateImage = NucleiDataVisualiser.getChannelRGBDuplicate(aOriginalImage, 0);

		// NulceiSet3D is sorted in z-value, so go through each z slice at a
		// time
		int sliceNr = -1;
		ImageProcessor ip = intermediateImage.getProcessor();
		for (final Nucleus nucleus : aNuclei)
		{
			final int nucleusSlice = (int) nucleus.getZcoordinate() + 1;
			if (nucleusSlice != sliceNr)
			{
				// Change of z slice detected. Process the previous one and set
				// up the new slice.
				if (sliceNr > 0)
				{
					aResultImage.getImageStack().setPixels(ip.getPixels(), sliceNr);
				}
				sliceNr = nucleusSlice;
				intermediateImage.setSlice(sliceNr);
				ip = intermediateImage.getProcessor();
			}

			final List<NuclearLink<Nucleus>> links = nucleus.getNeighbours();
			if (links != null)
			{
				for (final NuclearLink<Nucleus> link : links)
				{
					draw3DNuclearLink2D(link, nucleus, aWidth, ip);
				}
			}
		}
		aResultImage.getImageStack().setPixels(ip.getPixels(), sliceNr);

		intermediateImage.changes = false;
		intermediateImage.close();

		if (!aResultImage.isVisible())
		{
			aResultImage.setTitle("Result image");
			aResultImage.show();
		}

		aResultImage.updateAndRepaintWindow();
	}


	/**
	 * Redraw an image based on a set of nuclei. The image will contain one colour-coded dot of a given radius per nucleus found depending on the features of the nucleus.
	 *
	 * @param aNuclei
	 *            An array of NucleiSets. One per slice of the image is expected.
	 * @param aCollectives
	 *            The list of all collectives sets per slice. Can be null (no collective data will be drawn).
	 * @param aRadius
	 *            The size of the dot that is drawn per nucleus
	 * @param aImage
	 *            The image with all the nucleus dots in it. Note that this method may change the image.
	 *
	 * @return The result image, either newly created or updated with the nuclei info.
	 */
	public static void drawNucleiData2D(final NucleiSet2D[] aNuclei, final List<List<NucleiSet2D>> aCollectives, final int aRadius, final ImagePlus aImage)
	{
		final ImagePlus intermediateImage = NucleiDataVisualiser.getChannelRGBDuplicate(aImage, 0);

		for (int sliceNr = 1; sliceNr <= intermediateImage.getNSlices(); sliceNr++)
		{
			intermediateImage.setSlice(sliceNr);
			final ImageProcessor ip = intermediateImage.getProcessor();
			for (final NucleusEvent nucleus : aNuclei[sliceNr - 1])
			{
				if (nucleus.isTrueNucleus())
				{
					drawCirle(nucleus, ip, aRadius, Color.GREEN.getRGB(), false);
				}
				else
				{
					drawCirle(nucleus, ip, aRadius, Color.RED.getRGB(), false);
				}

				if (nucleus.isSingleCell())
				{
					drawCirle(nucleus, ip, aRadius, Color.YELLOW.getRGB(), true);
				}
			}

			if (aCollectives != null)
			{
				final List<NucleiSet2D> sliceCollectives = aCollectives.get(sliceNr - 1);
				final int nrOfCollectives = sliceCollectives.size();
				final int[] colours = getColourDivision(nrOfCollectives);

				for (int collectiveNr = 0; collectiveNr < nrOfCollectives; collectiveNr++)
				{
					final int colour = colours[collectiveNr];
					final NucleiSet2D collective = sliceCollectives.get(collectiveNr);
					for (final NucleusEvent nucleus : collective)
					{
						drawSquare(nucleus, ip, aRadius, colour, true);
					}
				}
			}
			aImage.getImageStack().setPixels(ip.getPixels(), sliceNr);
		}
		intermediateImage.changes = false;
		intermediateImage.close();

		if (!aImage.isVisible())
		{
			aImage.setTitle("Result image");
			aImage.show();
		}

		aImage.updateAndRepaintWindow();
	}


	/**
	 * Redraw an image based on a set of nuclei. The image will contain one colour-coded dot of a given radius per nucleus found depending on the features of the nucleus.
	 *
	 * @param aNuclei
	 *            An array of NucleiSets. One per slice of the image is expected.
	 * @param aCollectives
	 *            The list of all collectives sets per slice. Can be null (no collective data will be drawn).
	 * @param aRadius
	 *            The size of the dot that is drawn per nucleus
	 * @param aImage
	 *            The image with all the nucleus dots in it. Note that this method may change the image.
	 *
	 * @return The result image, either newly created or updated with the nuclei info.
	 */
	public static void drawNucleiData3D(final NucleiSet3D aNucleiSet, final int aRadius, final ImagePlus aImage)
	{
		final ImagePlus intermediateImage = NucleiDataVisualiser.getChannelRGBDuplicate(aImage, 0);
		final ImageStack stack = intermediateImage.getStack();

		for (final Nucleus nucleus : aNucleiSet)
		{
			drawSphere(nucleus, stack, aRadius, Color.GREEN.getRGB(), false);

			if (nucleus.isSingleCell())
			{
				drawSphere(nucleus, stack, aRadius, Color.YELLOW.getRGB(), true);
			}
		}

		final List<NucleiSet3D> collectives = aNucleiSet.getIdentifiedCollectives();
		if (collectives != null)
		{
			final int nrOfCollectives = collectives.size();
			final int[] colours = getColourDivision(nrOfCollectives);

			for (int collectiveNr = 0; collectiveNr < nrOfCollectives; collectiveNr++)
			{
				final int colour = colours[collectiveNr];
				final NucleiSet3D collective = collectives.get(collectiveNr);
				for (final Nucleus nucleus : collective)
				{
					drawCube(nucleus, stack, aRadius, colour, true);
				}
			}
		}

		final float[] voxels = stack.getVoxels(0, 0, 0, aImage.getWidth(), aImage.getHeight(), aImage.getNSlices(), null);
		aImage.getStack().setVoxels(0, 0, 0, aImage.getWidth(), aImage.getHeight(), aImage.getNSlices(), voxels);

		intermediateImage.changes = false;
		intermediateImage.close();

		if (!aImage.isVisible())
		{
			aImage.setTitle("Result image");
			aImage.show();
		}

		aImage.updateAndRepaintWindow();
	}


	/**
	 * Draw a (filled) sphere with the colour provided in the ImageProcessor on the coordinates of the given Nucleus. The sphere will be brightest at the centre and slowly fade to black.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the sphere
	 * @param aImageStack
	 *            The ImageStack in which the sphere will be drawn
	 * @param aRadius
	 *            The radius of the sphere
	 * @param aColour
	 *            The RGB colour value to be used
	 * @param aIsOpen
	 *            Should the circle be filled (false) or open (true)
	 */
	public static void drawSphere(final BaseNucleus aNucleus, final ImageStack aImageStack, final int aRadius, final int aColour, final boolean aIsOpen)
	{
		final double xCoord = aNucleus.getXcoordinate();
		final double yCoord = aNucleus.getYcoordinate();
		final double zCoord = aNucleus.getZcoordinate();
		final int rad = aRadius * aRadius;
		final int radLow = (aRadius - 2) * (aRadius - 2);
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
			// for (int z = -aRadius; z <= aRadius; z++)
			// {
			{
				final int dist = x * x + y * y;
				if ((dist <= rad && !aIsOpen) || (dist < rad && dist >= radLow && aIsOpen))
				{
					if (xCoord + x >= 0 && yCoord + y >= 0 && zCoord >= 0 //
							&& xCoord + x < aImageStack.getWidth() && yCoord + y < aImageStack.getHeight() && zCoord < aImageStack.getSize())
					{
						final int colour = aIsOpen ? aColour : lightenColour(aColour, ((aRadius - Math.sqrt(x * x + y * y) + 1) / (aRadius + 1)));
						aImageStack.setVoxel((int) xCoord + x, (int) yCoord + y, (int) zCoord, colour);
					}
					// }
				}
			}
	}


	/**
	 * Draw a square with the colour provided in the ImageProcessor on the coordinates of the given Nucleus. The square will be brightest at the centre and slowly fade to black. Alternatively, the square will be a ring of equal colour throughout. The
	 * choice of this is given with a parameter.
	 *
	 * @param aNucleus
	 *            The Nucleus which provides the Coordinates for the cube
	 * @param aImageProcessor
	 *            The ImageProcessor in which the square will be drawn
	 * @param aColour
	 *            The RGB colour value to be used
	 * @param aIsOpen
	 *            If true, the square will be a ring, false it will be filled.
	 */
	public static void drawSquare(final BaseNucleus aNucleus, final ImageProcessor aImageProcessor, final int aRadius, final int aColour, final boolean aIsOpen)
	{
		final double xCoord = aNucleus.getXcoordinate();
		final double yCoord = aNucleus.getYcoordinate();
		final int lowerBound = aIsOpen ? aRadius - 1 : 0;
		for (int x = -aRadius; x <= aRadius; x++)
			for (int y = -aRadius; y <= aRadius; y++)
			{
				final int absX = Math.abs(x);
				final int absY = Math.abs(y);
				if (!aIsOpen || ((absX <= aRadius && absX >= lowerBound) || (absY <= aRadius && absY >= lowerBound)))
				{
					if (xCoord + x >= 0 && yCoord + y >= 0 //
							&& xCoord + x < aImageProcessor.getWidth() && yCoord + y < aImageProcessor.getHeight())
					{
						final int colour = aIsOpen ? aColour : lightenColour(aColour, ((double) aRadius - Math.max(x, y)) / aRadius);
						aImageProcessor.setf((int) xCoord + x, (int) yCoord + y, colour);
					}
				}
			}
	}


	/**
	 * Creates a duplicate RGB image from the channel of the original image. If the original isn't an RGB image (single channel), the image will get a gray value LUT first.
	 *
	 * @param aOriginalImage
	 *            The original image. Can be composite (multi-channel) or simple (1-channel)
	 * @param aChannel
	 *            The channel of the original image that will be duplicated. Ignored if the image is not a composite.
	 * @return An RGB copy of the original image or the specified channel of the original composite image.
	 */
	public static ImagePlus getChannelRGBDuplicate(final ImagePlus aOriginalImage, final int aChannel)
	{
		final ImagePlus result;
		if (aOriginalImage.isComposite())
		{
			final ImagePlus dup = aOriginalImage.duplicate();
			final ImagePlus[] dupChannels = ChannelSplitter.split(dup);
			result = dupChannels[aChannel];
			for (int i = 0; i < dupChannels.length; i++)
			{
				if (i != aChannel)
				{
					dupChannels[i].close();
				}
			}

			if (dup != null)
			{
				dup.close();
			}
		}
		else
		{
			result = aOriginalImage.duplicate();
		}
		if (result.getType() != ImagePlus.COLOR_RGB)
		{
			result.setLut(LUT.createLutFromColor(Color.LIGHT_GRAY));
			(new ImageConverter(result)).convertToRGB();
		}

		return result;
	}


	/**
	 * Get a number of colours spread over RGB colour array. The numbers are based on the hue in the HSB colour space that ranges from 0 to 1. The colours are evenly divided over that range and with a maximum saturation and brightness are converted
	 * to their RGB equivalent.
	 *
	 * @param aNumberOfColours
	 *            The number of colours desired
	 * @return An array of colour numbers in the RGB colour space. The number of colours matches the parameter.
	 */
	private static int[] getColourDivision(final int aNumberOfColours)
	{
		final int[] result = new int[aNumberOfColours];
		for (int i = 0; i < aNumberOfColours; i++)
		{
			result[i] = Color.HSBtoRGB(((float) i) / ((float) aNumberOfColours), 1, 1);
		}
		return result;
	}


	/**
	 * Get an RGB colour and lighten it on all channels with the given factor.
	 *
	 * @param aColour
	 *            The RGB colour as an int
	 * @param aLightenFactor
	 *            The lighten factor where 1 is no change, 0.5 is half as bright while 3.0 is three times brighter.
	 * @return The adjusted RGB colour. If any channel is too light (for 8-bits), it will get adjusted down to the max 8-bit brightness.
	 */
	private static int lightenColour(final int aColour, final double aLightenFactor)
	{
		int red = (aColour >> 16) & 0xFF;
		int green = (aColour >> 8) & 0xFF;
		int blue = (aColour >> 0) & 0xFF;
		red *= aLightenFactor;
		green *= aLightenFactor;
		blue *= aLightenFactor;
		red = Math.min(red, 255);
		green = Math.min(green, 255);
		blue = Math.min(blue, 255);
		return ((red << 16) + (green << 8) + blue);
	}


	/**
	 * Avoids the ChartPanel input and output (and necessary part of the import where used) if not needed.
	 *
	 * @param aData
	 * @param aTitle
	 * @param aXTitle
	 * @param aYTitle
	 * @return
	 */
	public static void plotData(final float[][] aData, final Paint[] aColours, final String aTitle, final String aXTitle, final String aYTitle)
	{
		plotData(aData, aColours, aTitle, aXTitle, aYTitle, null);
	}


	/**
	 * Plots an x/y set of data points. The plot can be configured to have individual colours for any data point, allowing several data sets to be shown simultaneously.
	 *
	 * @param aData
	 *            The data set, consisting of 2 float arrays. The first array (0) contains the x values of the points while the second array (1) contains the y values
	 * @param aColours
	 *            An array of colours. One colour can be assigned per data point. Less is possible (any extra points will get the first colour) as is null (all black)
	 * @param aTitle
	 *            The title of the graph
	 * @param aXTitle
	 *            The title of the x-axis
	 * @param aYTitle
	 *            The title of the y-axis
	 * @param aChartPanel
	 *            The ChartPanel in which the plot needs to be displayed. May be null, in which case a new panel is created for this purpose
	 *
	 * @return The ChartPanel (or a new one) with the plot displayed.
	 */
	public static ChartPanel plotData(final float[][] aData, final Paint[] aColours, final String aTitle, final String aXTitle, final String aYTitle, final ChartPanel aChartPanel)
	{
		final NumberAxis domainAxis = new NumberAxis(aXTitle);
		domainAxis.setAutoRangeIncludesZero(false);
		final NumberAxis rangeAxis = new NumberAxis(aYTitle);
		rangeAxis.setAutoRangeIncludesZero(false);
		final FastScatterPlot plot = new ExtendedFastScatterPlot(aData, domainAxis, rangeAxis, 5, aColours, 1);
		final JFreeChart chart = new JFreeChart(aTitle, plot);
		// chart.setLegend(null);

		// force aliasing of the rendered content..
		chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		ChartPanel panel;
		if (aChartPanel == null)
		{
			panel = new ChartPanel(chart, true);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			final Dimension screenFitDim = new Dimension(((int) screenSize.getWidth() / 2), ((int) screenSize.getHeight() / 2));
			panel.setPreferredSize(screenFitDim);
			panel.setMinimumDrawHeight(10);
			panel.setMaximumDrawHeight(2000);
			panel.setMinimumDrawWidth(20);
			panel.setMaximumDrawWidth(2000);

			final JFrame window = new JFrame(aTitle + "plot");
			window.add(panel);
			window.validate();
			window.setSize(screenFitDim);
			window.setVisible(true);
		}
		else
		{
			panel = aChartPanel;
			panel.setChart(chart);
			// panel.updateUI();
		}

		return panel;
	}


	/**
	 * Plots a histogram of the x and y data and adds a callback listener to the histogram to react to clicks and mouse moves.
	 *
	 * @param aXData
	 *            The double[] containing the x values per tick
	 * @param aYData
	 *            The double[] having the matching y values to the aXData
	 * @param aTitle
	 *            The title of the histogram
	 * @param aXAxis
	 *            The name of the x-axis
	 * @param aYAxis
	 *            The name of the y-axis
	 * @param aCallback
	 *            The callback object that will be used to handle any mouse clicks and moves. Can be null.
	 */
	public static void plotDistanceHistogram(final double[] aXData, final double[] aYData, final String aTitle, final String aXAxis, final String aYAxis, final IChartMouseEventCallback aCallback)
	{
		final XYSeriesCollection histogram_plots = new XYSeriesCollection();
		final LookupPaintScale lut = createLUT(1);
		XYSeries series;

		// Fill the XYSeries with the data and get the proper x-axis range, to
		// get the right zoom-level
		int firstNotNull = -1;
		int lastNotNull = -1;
		series = new XYSeries(aYAxis);
		for (int j = 0; j < aYData.length; j++)
		{
			final double xValue = aXData != null ? aXData[j] : j;
			series.add(xValue, aYData[j]);
			if (aYData[j] != 0)
			{
				lastNotNull = j;
				if (firstNotNull < 0)
				{
					firstNotNull = j;
				}
			}
		}
		histogram_plots.addSeries(series);

		if (firstNotNull >= 0)
		{
			// Create chart with histograms
			final JFreeChart chart = ChartFactory.createHistogram(aTitle, aXAxis, aYAxis, histogram_plots, PlotOrientation.VERTICAL, true, true, false);

			// Set the look of histograms
			final XYPlot plot = (XYPlot) chart.getPlot();
			plot.setDomainCrosshairVisible(true);
			plot.setDomainCrosshairLockedOnData(true);
			plot.setRangeCrosshairVisible(true);
			plot.setRangeCrosshairLockedOnData(true);
			ClusteredXYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter()); // No gradiant colours
			final ClusteredXYBarRenderer renderer = new ClusteredXYBarRenderer(0.3, false);
			renderer.setSeriesPaint(0, lut.getPaint(1));
			renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator("{1}", new DecimalFormat("0.00"), new DecimalFormat("0.00"))); // Put x-value on top of bar
			renderer.setSeriesItemLabelsVisible(0, true); // Makes x-value on top of bar visible
			renderer.setShadowVisible(false); // No shadow. Looks bad when zoomed out.
			plot.setRenderer(0, renderer);

			if (aXData != null)
			{
				final double rangeMargin = 0.05 * (aXData[lastNotNull] - aXData[firstNotNull]);
				double lowerRange = aXData[firstNotNull] - rangeMargin;
				double upperRange = aXData[lastNotNull] + rangeMargin;
				if (lowerRange == upperRange)
				{
					lowerRange--;
					upperRange++;
				}
				plot.getDomainAxis().setRange(lowerRange, upperRange);
			}
			else
			{
				plot.getDomainAxis().setRange(firstNotNull, lastNotNull);
			}

			// Get the Panel to contain the histogram. Adjust to screen size.
			final ChartPanel chartPanel = new ChartPanel(chart);
			final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			final Dimension screenFitDim = new Dimension(((int) screenSize.getWidth() / 2), ((int) screenSize.getHeight() / 2));
			chartPanel.setPreferredSize(screenFitDim);

			// If need be, add a mouse listener
			if (aCallback != null)
			{
				final HistogramListener cml = new HistogramListener(chartPanel, aCallback);
				chartPanel.addChartMouseListener(cml);
			}

			// Create the window for displaying. Also adjusted to screen size.
			final JFrame window = new JFrame(aTitle + " histogram");
			window.add(chartPanel);
			window.validate();
			window.setSize(screenFitDim);
			window.setVisible(true);
		}
	}

}
