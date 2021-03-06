/*
 * Copyright 2019 Vulcalien
 * This code is licensed under MIT License by Vulcalien (see LICENSE)
 */
package vulc.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;

/**
 * FileUtils allows to perform some basic File operations:
 * <ul>
 * <li>copy</li>
 * <li>move</li>
 * <li>delete</li>
 * <li>download</li>
 * </ul>
 * @author Vulcalien
 */
public final class FileUtils {

	private FileUtils() {
	}

	private static void transferData(InputStream in, OutputStream out) {
		try {
			byte[] buffer = new byte[1024];
			int lengthRead;
			while((lengthRead = in.read(buffer)) >= 0) {
				out.write(buffer, 0, lengthRead);
				out.flush();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// --------
	// - copy -
	// --------
	/**
	 * Copies a file or a directory.
	 * In this case, the destination is a file if the source is a file or it is a directory if the source is a directory.
	 * @param source the file to copy
	 * @param destination the path of the destination file
	 *
	 * @throws FileNotFoundException if the source does not exist
	 * @throws IOException if the source file and the destination file are the same
	 */
	public static void copy(File source, File destination) {
		try {
			source = source.getCanonicalFile();
			destination = destination.getCanonicalFile();

			if(!source.exists()) throw new FileNotFoundException(source.toString());
			if(source.equals(destination))
			    throw new IOException("Source file: " + source + " equals destination file: " + destination);
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			destination.getParentFile().mkdirs();
		} catch(NullPointerException e) {
			// in this case the parent's directory is a root
		}

		if(source.isDirectory()) {
			copyDirectory(source, destination);
		} else if(source.isFile()) {
			copyFile(source, destination);
		}
	}

	/**
	 * Copies a file or a directory.
	 * In this case, the destination is a file if the source is a file or it is a directory if the source is a directory.
	 * @param source the path of the file to copy
	 * @param destination the path of the destination file
	 *
	 * @throws FileNotFoundException if the source does not exist
	 * @throws IOException if the source file and the destination file are the same
	 */
	public static void copy(String source, String destination) {
		copy(new File(source), new File(destination));
	}

	/**
	 * Copies a file or a directory.
	 * In this case, the destination always is a directory and the source file or directory will be copied inside the destination directory.
	 * @param source the file to copy
	 * @param destination the destination directory
	 *
	 * @throws FileNotFoundException if the source does not exist
	 * @throws IOException if the destination directory is the parent of the source file
	 */
	public static void copyToDirectory(File source, File destination) {
		try {
			source = source.getCanonicalFile();
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		copy(source, new File(destination + File.separator + source.getName()));
	}

	/**
	 * Copies a file or a directory.
	 * In this case, the destination always is a directory and the source file or directory will be copied inside the destination directory.
	 * @param source the path of the file to copy
	 * @param destination the path of the destination directory
	 *
	 * @throws FileNotFoundException if the source does not exist
	 * @throws IOException if the destination directory is the parent of the source file
	 */
	public static void copyToDirectory(String source, String destination) {
		copyToDirectory(new File(source), new File(destination));
	}

	private static void copyDirectory(File source, File destination) {
		File[] files = source.listFiles();
		destination.mkdir();
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			File newDestination = new File(destination + File.separator + file.getName());
			if(file.isDirectory()) {
				copyDirectory(file, newDestination);
			} else if(file.isFile()) {
				copyFile(file, newDestination);
			}
		}
	}

	private static void copyFile(File source, File destination) {
		try {
			destination.createNewFile();

			InputStream in = new FileInputStream(source);
			OutputStream out = new FileOutputStream(destination);

			transferData(in, out);

			in.close();
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// --------
	// - move -
	// --------
	/**
	 * Moves a file or a directory.<br>
	 * In this case, the destination is a file if the source is a file or it is a directory if the source is a directory.
	 * @param source the file to move
	 * @param destination the destination file
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws FileAlreadyExistsException if the destination file already exists
	 * @throws IOException if the source file and the destination file are the same
	 * @throws IOException if the destination is a subdirectory of the source
	 */
	public static void move(File source, File destination) {
		try {
			source = source.getCanonicalFile();
			destination = destination.getCanonicalFile();

			if(!source.exists()) throw new FileNotFoundException(source.toString());
			if(source.equals(destination))
			    throw new IOException("Source file: " + source + " equals destination file: " + destination);

			if(source.isDirectory()) {
				if(destination.exists()) throw new FileAlreadyExistsException(destination.toString());
				if(destination.getPath().startsWith(source.getPath() + File.separator))
				    throw new IOException("Cannot not move directory: " + source +
				                          " to a subdirectory: " + destination);
				moveDirectory(source, destination);
			} else if(source.isFile()) {
				moveFile(source, destination);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Moves a file or a directory.<br>
	 * In this case, the destination is a file if the source is a file or it is a directory if the source is a directory.
	 * @param source the path of the file to move
	 * @param destination the path of the destination file
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws FileAlreadyExistsException if the destination file is a directory and it already exists
	 * @throws IOException if the source file and the destination file are the same
	 * @throws IOException if the destination is a subdirectory of the source
	 */
	public static void move(String source, String destination) {
		move(new File(source), new File(destination));
	}

	/**
	 * Moves a file or a directory.<br>
	 * In this case, the destination always is a directory and the source file or directory will be moved inside the destination directory.
	 * @param source the file to move
	 * @param destination the destination directory
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws FileAlreadyExistsException if a file with the same name of the source already exists in the destination directory
	 * @throws IOException if the destination directory is the parent of the source file
	 * @throws IOException if the source and the destination are the same or the destination is a subdirectory of the source
	 */
	public static void moveToDirectory(File source, File destination) {
		try {
			source = source.getCanonicalFile();
		} catch(IOException e) {
			e.printStackTrace();
			return;
		}
		move(source, new File(destination + File.separator + source.getName()));
	}

	/**
	 * Moves a file or a directory.<br>
	 * In this case, the destination always is a directory and the source file or directory will be moved inside the destination directory.
	 * @param source the path of the file to move
	 * @param destination the path of the destination directory
	 *
	 * @throws FileNotFoundException if the source file does not exist
	 * @throws FileAlreadyExistsException if a file with the same name of the source already exists in the destination directory
	 * @throws IOException if the destination directory is the parent of the source file
	 * @throws IOException if the source and the destination are the same or the destination is a subdirectory of the source
	 */
	public static void moveToDirectory(String source, String destination) {
		moveToDirectory(new File(source), new File(destination));
	}

	private static void moveDirectory(File source, File destination) {
		copy(source, destination);
		delete(source);
	}

	private static void moveFile(File source, File destination) {
		copy(source, destination);
		delete(source);
	}

	// ----------
	// - delete -
	// ----------
	/**
	 * Deletes a file or a directory.
	 * @param file the file to delete
	 *
	 * @throws RuntimeException if the file still exists
	 */
	public static void delete(File file) {
		if(!file.exists()) return;
		if(file.isDirectory()) {
			deleteDirectory(file);
		} else if(file.isFile()) {
			deleteFile(file);
		}
	}

	/**
	 * Deletes a file or a directory.
	 * @param file the path of the file to delete
	 *
	 * @throws RuntimeException if the file still exists
	 */
	public static void delete(String file) {
		delete(new File(file));
	}

	private static void deleteDirectory(File file) {
		File[] files = file.listFiles();
		for(int i = 0; i < files.length; i++) {
			File selected = files[i];
			if(selected.isDirectory()) {
				deleteDirectory(selected);
			} else if(selected.isFile()) {
				deleteFile(selected);
			}
		}
		file.delete();
		if(file.exists()) throw new RuntimeException("Could not delete directory: " + file);
	}

	private static void deleteFile(File file) {
		file.delete();
		if(file.exists()) throw new RuntimeException("Could not delete file: " + file);
	}

	// ------------
	// - download -
	// ------------
	/**
	 * Downloads a file from a url to the destination file.
	 * @param url the URL referring to the data to download
	 * @param destination the file that will store the downloaded data
	 *
	 * @throws IOException
	 */
	public static void download(URL url, File destination) {
		try {
			destination.createNewFile();

			InputStream in = url.openStream();
			OutputStream out = new FileOutputStream(destination);

			transferData(in, out);

			in.close();
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Downloads a file from a url to the destination file.
	 * @param url the URL referring to the data to download
	 * @param destination the path of the destination file that will store the downloaded data
	 *
	 * @throws IOException
	 */
	public static void download(URL url, String destination) {
		download(url, new File(destination));
	}

	/**
	 * Downloads a file from a url to the destination file.
	 * @param url the URL referring to the data to download
	 * @param destination the file that will store the downloaded data
	 *
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void download(String url, File destination) {
		try {
			download(new URL(url), destination);
		} catch(MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Downloads a file from a url to the destination file.
	 * @param url the URL referring to the data to download
	 * @param destination the path of the destination file that will store the downloaded data
	 *
	 * @throws IOException
	 */
	public static void download(String url, String destination) {
		download(url, new File(destination));
	}

}
