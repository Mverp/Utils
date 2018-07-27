package utils;

import java.util.ArrayList;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.CurveFitter;
import ij.process.ImageProcessor;

public class AttenuationAdjuster
{
	public static List<Double> adjustAttenuation(final ImagePlus aImage, final int aChannelNr, final double aPercentile, final int aStandardSlice, final int aStartIntesity, final int aStartSlice,
			final int aEndIntensity, final int aFitMethod, final List<Double> aAttenuationFactors)
	{
		final List<Double> adjustments = aAttenuationFactors != null ? aAttenuationFactors
				: getAttenuationAdjustments(aImage, aChannelNr, aPercentile, aStandardSlice, aStartIntesity, aStartSlice, aEndIntensity, aFitMethod);

		final double standardNumber = adjustments.get(aStandardSlice);

		for (int sliceNr = 0; sliceNr < aImage.getNSlices(); sliceNr++)
		{
			final double conversionFactor = standardNumber / adjustments.get(sliceNr);
			final ImageProcessor proc = aImage.getStack().getProcessor(sliceNr * aImage.getNChannels() + aChannelNr);
			for (int x = 0; x < proc.getWidth(); x++)
			{
				for (int y = 0; y < proc.getHeight(); y++)
				{
					final int value = proc.getPixel(x, y);
					proc.set(x, y, Math.min((int) Math.round(value * conversionFactor), 255));
				}
			}
		}

		aImage.updateAndDraw();

		return adjustments;
	}


	public static List<Double> getAttenuationAdjustments(final ImagePlus aImage, final int aChannelNr, final double aPercentile, final int aStandardSlice, final int aStartIntesity,
			final int aStartSlice, final int aEndIntensity, final int aFitMethod)
	{
		final List<Double> adjustments = new ArrayList<>();

		final double[] xValues = new double[aImage.getNSlices()];
		for (int i = 0; i < xValues.length; i++)
		{
			xValues[i] = i;
		}

		final double[] cutOffs = getPercentileCutoffs(aImage, aChannelNr, aPercentile, aStandardSlice, aStartIntesity, aEndIntensity);

		final CurveFitter fitter = new CurveFitter(xValues, cutOffs);
		final int method = aFitMethod;
		fitter.doFit(method);
		final double[] params = fitter.getParams();

		final double startAdjustment = fitter.f(params, aStandardSlice);
		for (int sliceNr = 0; sliceNr < aImage.getNSlices(); sliceNr++)
		{
			if (sliceNr < aStartSlice)
			{
				adjustments.add(startAdjustment);
			}
			adjustments.add(fitter.f(params, sliceNr));
		}

		return adjustments;
	}


	public static double[] getPercentileCutoffs(final ImagePlus aImage, final int aChannelNr, final double aPercentile, final int aStandardSlice, final int aStartIntesity, final int aEndIntensity)
	{
		final double[] sliceCutoffs = new double[aImage.getNSlices()];
		final ImageStack stack = aImage.getStack();

		for (int sliceNr = 1; sliceNr <= aImage.getNSlices(); sliceNr++)
		{
			final ImageProcessor proc = stack.getProcessor((sliceNr - 1) * aImage.getNChannels() + aChannelNr);
			final int[] histogram = new int[256];
			for (final byte pixValue : ((byte[]) proc.getPixels()))
			{
				histogram[pixValue & 0xFF]++;
			}

			int totPix = 0;
			for (int bucketNr = aStartIntesity; bucketNr < aEndIntensity; bucketNr++)
			{
				totPix += histogram[bucketNr];
			}

			final double pixelThreshold = (totPix * aPercentile) / 100;

			int sum = 0;
			for (int bucketNr = aStartIntesity; bucketNr < aEndIntensity; bucketNr++)
			{
				sum += histogram[bucketNr];
				if (sum >= pixelThreshold)
				{
					sliceCutoffs[sliceNr - 1] = bucketNr;
					break;
				}
			}
		}

		return sliceCutoffs;
	}

}
