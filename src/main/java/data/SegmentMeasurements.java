package data;

import java.util.List;

import utils.MyMath;

/**
 * A set of measurements on an image segment. Based on the MorpholibJ and 3D plugins of Fiji.
 *
 * @author Merijn van Erp
 *
 */
public class SegmentMeasurements
{
	// Intensity measurements Morpholibj
	private Double meanIntensity;
	private Double standardDeviation;
	private Double maxIntensity;
	private Double minIntensity;
	private Double medianIntensity;
	private Double modeIntensity;
	private Double skewness;
	private Double kurtosis;

	// Shape measurements Morpholibj
	private double surfaceArea;
	private double sphericities;
	private double eulerNumber;
	private double mexicanHatValue;
	private double[] ellipsoid = new double[9];
	private double[] elongations = new double[3];
	private double[] inscribedSphere = new double[4];

	// 3D
	private double meanIntensity3D = 0;
	private double standardDeviation3D = 0;
	private double minimum3D = 0;
	private double maximum3D = 0;
	private double integratedDensity3D = 0;

	// 3D Geometric Measure
	private double volumePixels = 0;
	private double volumeUnit = 0;
	private double areaPixels = 0;
	private double areaUnit = 0;

	// 3D Shape measure
	private double compactness = 0;
	private double sphericity = 0;
	private double elongatio = 0;
	private double flatness = 0;
	private double spareness = 0;


	public SegmentMeasurements(final List<Double> aIntensityValues)
	{
		this.meanIntensity = MyMath.getMean(aIntensityValues);
		this.medianIntensity = MyMath.getMedian(aIntensityValues);
		this.maxIntensity = MyMath.getMaximum(aIntensityValues);
		this.minIntensity = MyMath.getMinimum(aIntensityValues);
		this.modeIntensity = MyMath.getMode(aIntensityValues);
		this.standardDeviation = MyMath.getStandardDeviation(aIntensityValues, getMeanIntensity());
		this.skewness = MyMath.getSkewness(aIntensityValues, getMeanIntensity(), getStandardDeviation());
		this.kurtosis = MyMath.getKurtosis(aIntensityValues, getMeanIntensity(), getStandardDeviation());
	}


	/**
	 * @return the areaPixels
	 */
	public double getAreaPixels()
	{
		return this.areaPixels;
	}


	/**
	 * @return the areaUnit
	 */
	public double getAreaUnit()
	{
		return this.areaUnit;
	}


	/**
	 * @return the compactness
	 */
	public double getCompactness()
	{
		return this.compactness;
	}


	/**
	 * @return the ellipsoid
	 */
	public double[] getEllipsoid()
	{
		return this.ellipsoid;
	}


	/**
	 * @return the elongatio
	 */
	public double getElongatio()
	{
		return this.elongatio;
	}


	/**
	 * @return the elongations
	 */
	public double[] getElongations()
	{
		return this.elongations;
	}


	/**
	 * @return the eulerNumber
	 */
	public double getEulerNumber()
	{
		return this.eulerNumber;
	}


	/**
	 * @return the flatness
	 */
	public double getFlatness()
	{
		return this.flatness;
	}


	/**
	 * @return the inscribedSphere
	 */
	public double[] getInscribedSphere()
	{
		return this.inscribedSphere;
	}


	/**
	 * @return the integratedDensity3D
	 */
	public double getIntegratedDensity3D()
	{
		return this.integratedDensity3D;
	}


	public double getKurtosis()
	{
		return this.kurtosis;
	}


	/**
	 * @return the maximum3D
	 */
	public double getMaximum3D()
	{
		return this.maximum3D;
	}


	public double getMaxIntensity()
	{

		return this.maxIntensity;
	}


	/**
	 * Returns the mean intensity value of DAPI signal
	 *
	 * @return double meanIntensity
	 */
	public double getMeanIntensity()
	{
		return this.meanIntensity;
	}


	/**
	 * @return the meanIntensity3D
	 */
	public double getMeanIntensity3D()
	{
		return this.meanIntensity3D;
	}


	public double getMedianIntensity()
	{
		return this.medianIntensity;
	}


	/**
	 * @return the mexicanHatValue
	 */
	public double getMexicanHatValue()
	{
		return this.mexicanHatValue;
	}


	/**
	 * @return the minimum3D
	 */
	public double getMinimum3D()
	{
		return this.minimum3D;
	}


	public double getMinIntensity()
	{
		return this.minIntensity;
	}


	public double getModeIntensity()
	{
		return this.modeIntensity;
	}


	public double getSkewness()
	{
		return this.skewness;
	}


	/**
	 * @return the spareness
	 */
	public double getSpareness()
	{
		return this.spareness;
	}


	/**
	 * @return the sphericities
	 */
	public double getSphericities()
	{
		return this.sphericities;
	}


	/**
	 * @return the sphericity
	 */
	public double getSphericity()
	{
		return this.sphericity;
	}


	public double getStandardDeviation()
	{
		return this.standardDeviation;
	}


	/**
	 * @return the standard deviation 3D
	 */
	public double getStandardDeviation3D()
	{
		return this.standardDeviation3D;
	}


	/**
	 * @return the surfaceArea
	 */
	public double getSurfaceArea()
	{
		return this.surfaceArea;
	}


	/**
	 * @return the volumePixels
	 */
	public double getVolumePixels()
	{
		return this.volumePixels;
	}


	/**
	 * @return the volumeUnit
	 */
	public double getVolumeUnit()
	{
		return this.volumeUnit;
	}


	public void setAreaPixels(final double aArea)
	{
		this.areaPixels = aArea;
	}


	public void setAreaUnit(final double aAreaUnit)
	{
		this.areaUnit = aAreaUnit;
	}


	public void setCompactness(final double aCompactness)
	{
		this.compactness = aCompactness;
	}


	public void setEllipsoids(final double[] aEllipsoid)
	{
		this.ellipsoid = aEllipsoid;
	}


	public void setElongatio(final double aElongatio)
	{
		this.elongatio = aElongatio;
	}


	public void setElongations(final double[] aElongations)
	{
		this.elongations = aElongations;
	}


	/**
	 * @param aEulerNumber
	 *            the eulerNumber to set
	 */
	public void setEulerNumber(final double aEulerNumber)
	{
		this.eulerNumber = aEulerNumber;
	}


	public void setFlatness(final double aFlatness)
	{
		this.flatness = aFlatness;
	}


	public void setInscribedSphere(final double[] aInscribedSphere)
	{
		this.inscribedSphere = aInscribedSphere;
	}


	public void setIntegratedDensity3D(final double aIntegratedDensity3D)
	{
		this.integratedDensity3D = aIntegratedDensity3D;
	}


	public void setKurtosis(final double aKurtosis)
	{
		this.kurtosis = aKurtosis;
	}


	public void setMaximum3D(final double aMaximum3D)
	{
		this.maximum3D = aMaximum3D;
	}


	/**
	 * @param aMaxIntensity
	 *            the maxIntensityValue to set
	 */
	public void setMaxIntensity(final double aMaxIntensity)
	{
		this.maxIntensity = aMaxIntensity;
	}


	/**
	 * @param aMeanIntensity
	 *            the meanIntensity to set
	 */
	public void setMeanIntensity(final double aMeanIntensity)
	{
		this.meanIntensity = aMeanIntensity;
	}


	public void setMeanIntensity3D(final double aMeanIntensity3D)
	{
		this.meanIntensity3D = aMeanIntensity3D;
	}


	public void setMedianIntensity(final double aMedian)
	{
		this.medianIntensity = aMedian;
	}


	/**
	 * @param aMexicanHatValue
	 *            the mexicanHatValue to set
	 */
	public void setMexicanHatValue(final double aMexicanHatValue)
	{
		this.mexicanHatValue = aMexicanHatValue;
	}


	/**
	 * @param aMinimum3D
	 *            the aMinimum3D to set
	 */
	public void setMinimum3D(final double aMinimum3D)
	{
		this.minimum3D = aMinimum3D;
	}


	public void setMinIntensity(final double aMinIntensity)
	{
		this.minIntensity = aMinIntensity;
	}


	public void setModeIntensity(final double aMode)
	{
		this.modeIntensity = aMode;
	}


	public void setSkewness(final double aSkewness)
	{
		this.skewness = aSkewness;
	}


	public void setSpareness(final double aSpareness)
	{
		this.spareness = aSpareness;
	}


	/**
	 * @param aSphericities
	 *            the sphericities to set
	 */
	public void setSphericities(final double aSphericities)
	{
		this.sphericities = aSphericities;
	}


	public void setSphericity(final double aSphericity)
	{
		this.sphericity = aSphericity;
	}


	public void setStandardDeviation(final double aStandardDeviation)
	{
		this.standardDeviation = aStandardDeviation;
	}


	public void setStandardDeviation3D(final double aStandardDeviation3D)
	{
		this.standardDeviation3D = aStandardDeviation3D;
	}


	/**
	 * @param aSurfaceArea
	 *            the surfaceArea to set
	 */
	public void setSurfaceArea(final double aSurfaceArea)
	{
		this.surfaceArea = aSurfaceArea;
	}


	public void setVolumePixels(final double aVolumePixels)
	{
		this.volumePixels = aVolumePixels;
	}


	public void setVolumeUnit(final double aVolumeUnit)
	{
		this.volumeUnit = aVolumeUnit;
	}

}
