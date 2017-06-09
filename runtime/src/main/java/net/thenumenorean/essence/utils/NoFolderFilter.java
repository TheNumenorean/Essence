package net.thenumenorean.essence.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * This FileFilter accepts only files.
 * @author Francesco Macagno
 *
 */
public class NoFolderFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		return pathname.isFile();
	}
	
}