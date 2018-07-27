package data;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import utils.MyMath;

/**
 * A set of measurements on an image segment. Based in part on the MorpholibJ and 3D plugins of Fiji.
 *
 * @author Merijn van Erp
 *
 */
public class SegmentMeasurements
{
	// Intensity measurements (no specific plugin)
	public static final String BACKGROUND_INTENSITY = "background intensity";
	public static final String INTENSITY_KURTOSIS = "kurtosis of intensity";
	public static final String INTENSITY_SKEWNESS = "skewness of intensity";
	public static final String LOG_VALUE = "Laplacian of Gaussian of marker";
	public static final String MAX_INTENSITY = "maximum intensity";
	public static final String MEDIAN_INTENSITY = "median intensity";
	public static final String MEAN_INTENSITY = "mean intensity";
	public static final String MIN_INTENSITY = "minimum intensity";
	public static final String MODE_INTENSITY = "mode intensity";
	public static final String STANDARD_DEVIATION = "standard deviation of intensity";
	public static final String[] STANDARD_GROUP = { MEAN_INTENSITY, MIN_INTENSITY, MAX_INTENSITY, MEDIAN_INTENSITY, STANDARD_DEVIATION, BACKGROUND_INTENSITY, MODE_INTENSITY, INTENSITY_KURTOSIS,
			INTENSITY_SKEWNESS };
	public static final String[] STANDARD_GROUP_NUCLEUS = { MEAN_INTENSITY, MIN_INTENSITY, MAX_INTENSITY, MEDIAN_INTENSITY, STANDARD_DEVIATION, BACKGROUND_INTENSITY, MODE_INTENSITY,
			INTENSITY_KURTOSIS, INTENSITY_SKEWNESS, LOG_VALUE };

	// Shape measurements MorpholibJ
	public static final String EULER_NUMBER = "Euler number";
	public static final String ELLIPSOID_CENTER_X = "ellipsiod center X";
	public static final String ELLIPSOID_CENTER_Y = "ellipsiod center Y";
	public static final String ELLIPSOID_CENTER_Z = "ellipsiod center Z";
	public static final String ELLIPSOID_RADIUS_1 = "ellipsiod radius 1";
	public static final String ELLIPSOID_RADIUS_2 = "ellipsiod radius 2";
	public static final String ELLIPSOID_RADIUS_3 = "ellipsiod radius 3";
	public static final String ELLIPSOID_RADIUS_AZIMUT = "ellipsiod radius azimut";
	public static final String ELLIPSOID_RADIUS_ELEVATION = "ellipsiod radius elevation";
	public static final String ELLIPSOID_RADIUS_ROLL = "ellipsiod radius roll";
	public static final String ELLOGATION_R1_R2 = "ellongation R1/R2";
	public static final String ELLOGATION_R1_R3 = "ellongation R1/R3";
	public static final String ELLOGATION_R2_R3 = "ellongation R2/R3";
	public static final String INSCRIBED_SPHERE_CENTER_X = "inscribed sphere center X";
	public static final String INSCRIBED_SPHERE_CENTER_Y = "inscribed sphere center Y";
	public static final String INSCRIBED_SPHERE_CENTER_Z = "inscribed sphere center Z";
	public static final String INSCRIBED_SPHERE_RADIUS = "inscribed sphere radius";
	public static final String SURFACE_AREA = "estimated surface area";
	public static final String SPHERICITY_MORPHOLIBJ = "sphericity (MorpholibJ)";
	public static final String[] MORPHOLIBJ_GROUP = { SURFACE_AREA, SPHERICITY_MORPHOLIBJ, EULER_NUMBER, ELLIPSOID_CENTER_X, ELLIPSOID_CENTER_Y, ELLIPSOID_CENTER_Z, ELLIPSOID_RADIUS_1,
			ELLIPSOID_RADIUS_2, ELLIPSOID_RADIUS_3, ELLIPSOID_RADIUS_AZIMUT, ELLIPSOID_RADIUS_ELEVATION, ELLIPSOID_RADIUS_ROLL, ELLOGATION_R1_R2, ELLOGATION_R1_R3, ELLOGATION_R2_R3,
			INSCRIBED_SPHERE_CENTER_X, INSCRIBED_SPHERE_CENTER_Y, INSCRIBED_SPHERE_CENTER_Z, INSCRIBED_SPHERE_RADIUS };

	// 3D
	public static final String INTEGRATED_DENSITY = "integrated density";

	// 3D Geometric Measure
	public static final String VOLUME_IN_PIXELS = "volume in voxels";
	public static final String VOLUME_IN_UNITS = "volume in units";
	public static final String SURFACE_IN_PIXELS = "surface area in pixels";
	public static final String SURFACE_IN_UNITS = "surface area in units";

	// 3D Shape measure
	public static final String COMPACTNESS = "compactness";
	public static final String ELONGATIO = "elongatio";
	public static final String FLATNESS = "flatness";
	public static final String SPARENESS = "spareness";
	public static final String SPHERICITY_MCIB3D = "sphericity (MCIB3D)";
	public static final String[] MCIB3D_GROUP = { INTEGRATED_DENSITY, COMPACTNESS, ELONGATIO, FLATNESS, SPARENESS, SPHERICITY_MCIB3D, VOLUME_IN_PIXELS, VOLUME_IN_UNITS, SURFACE_IN_PIXELS,
			SURFACE_IN_UNITS };

	// Groups
	public static final String STANDARD_MEASUREMENTS = "Standard measurements";
	public static final String MORPHOLIBJ_MEASUREMENTS = "MorphoLibJ measurements";
	public static final String MCIB3D_MEASUREMENTS = "3D tools measurements";

	// Map with the actual measurements
	private final HashMap<String, Double> measurements;


	/**
	 * Initially store the intensity value measurements and background signal for this segment.
	 *
	 * @param aIntensityValues
	 *            The list of intensity values (one per pixel in the segment). Can be null.
	 * @param aBackground
	 *            The background measure of the same signal as the intensity values. Can be null.
	 */
	public SegmentMeasurements(final List<Double> aIntensityValues, final Double aBackground)
	{
		this.measurements = new HashMap<>();

		if (aIntensityValues != null && !aIntensityValues.isEmpty())
		{
			setMeasurement(MEAN_INTENSITY, MyMath.getMean(aIntensityValues));
			setMeasurement(MEDIAN_INTENSITY, MyMath.getMedian(aIntensityValues));
			setMeasurement(MAX_INTENSITY, MyMath.getMaximum(aIntensityValues));
			setMeasurement(MIN_INTENSITY, MyMath.getMinimum(aIntensityValues));
			setMeasurement(MODE_INTENSITY, MyMath.getMode(aIntensityValues));
			setMeasurement(STANDARD_DEVIATION, MyMath.getStandardDeviation(aIntensityValues, getMeasurement(MEAN_INTENSITY)));
			setMeasurement(INTENSITY_SKEWNESS, MyMath.getSkewness(aIntensityValues, getMeasurement(MEAN_INTENSITY), getMeasurement(STANDARD_DEVIATION)));
			setMeasurement(INTENSITY_KURTOSIS, MyMath.getKurtosis(aIntensityValues, getMeasurement(MEAN_INTENSITY), getMeasurement(STANDARD_DEVIATION)));
		}
		else
		{
			setMeasurement(MEAN_INTENSITY, 0.0);
			setMeasurement(MEDIAN_INTENSITY, 0.0);
			setMeasurement(MAX_INTENSITY, 0.0);
			setMeasurement(MIN_INTENSITY, 0.0);
			setMeasurement(MODE_INTENSITY, 0.0);
			setMeasurement(STANDARD_DEVIATION, 0.0);
			setMeasurement(INTENSITY_SKEWNESS, 0.0);
			setMeasurement(INTENSITY_KURTOSIS, 0.0);
		}

		setMeasurement(BACKGROUND_INTENSITY, aBackground);
	}


	/**
	 * Gets a named measurement value.
	 *
	 * @param aMeasureName
	 *            The name under which the value has been stored
	 * @return The value as stored or null if no value has been stored under such a name (note that null may also be the actually stored value!)
	 */
	public Double getMeasurement(final String aMeasureName)
	{
		return this.measurements.get(aMeasureName);
	}


	/**
	 * Returns all the names of measurements that have been given a value.
	 *
	 * @return The Set of measurement names
	 */
	public Set<String> getMeasurementNames()
	{
		return this.measurements.keySet();
	}


	/**
	 * Does this SegmentMeasurements contain a value for the measurement name given?
	 *
	 * @param aMeasureName
	 *            The measurement name under which a value may have been stored
	 * @return True if such a measured value exists or false if not
	 */
	public boolean hasMeasurement(final String aMeasureName)
	{
		return this.measurements.containsKey(aMeasureName);
	}


	/**
	 * Removes any vale for the named measurement from this SegmentedMeasurements. This can be used if the value is really not a valid measurement instead of just storing null (which would be a valid measurement without a value).
	 *
	 * @param aMeasureName
	 *            The measurement name
	 * @return The stored value if there was one or null if not (or if that was the stored value of course).
	 */
	public Double removeMeasurement(final String aMeasureName)
	{
		return this.measurements.remove(aMeasureName);
	}


	/**
	 * A generic way to set any measurement in the list of measured values. If there already is such a measurement value, it is replaced and the old value will be returned.
	 *
	 * @param aMeasureName
	 *            The name under which the measurement will be saved.
	 * @param aMeasurement
	 *            The measured value
	 * @return The old value of the measurement (if any). Null if no old value existed.
	 */
	public Double setMeasurement(final String aMeasureName, final Double aMeasurement)
	{
		return this.measurements.put(aMeasureName, aMeasurement);
	}

}
