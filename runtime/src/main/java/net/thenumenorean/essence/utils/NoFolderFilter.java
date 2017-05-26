package net.thenumenorean.essence.utils;

import java.io.File;
import java.io.FileFilter;

public class NoFolderFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		return pathname.isFile();
	}
	
}