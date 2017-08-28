package utils;

import ij.ImagePlus;

/**
 * A class of generic utilities that are collected here until a better class can be constructed.
 *
 * @author Merijn van Erp
 *
 */
public class GenUtils
{

	/**
	 * Get the title of the image and remove any extension if present. Note: an extension in this case is any 3 letter combination after the last period.
	 *
	 * @param aImage
	 *            The image with title
	 * @return The title of the image, with any three letter final extension removed
	 */
	public static String getTitleNoExtension(final ImagePlus aImage)
	{
		final String title = aImage.getTitle();
		final int index = title.lastIndexOf(".");
		if (index < 0)
		{
			return title;
		}
		if (index == title.length() - 4)
		{
			return title.substring(0, title.lastIndexOf("."));
		}

		return title.substring(0, title.lastIndexOf("."));
	}
}
