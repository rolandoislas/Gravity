package com.rolandoislas.gravity.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Rolando.
 */
public class FileUtil {

    /*
        Extracts all the contents of a zip archive.
        @param zipPath path to zip
        @param destpath Destination directory
     */
    public static void unZip(File zipPath, File destPath) throws IOException {
        if(!destPath.exists()) {
            destPath.mkdirs();
        }
        ZipInputStream in = new ZipInputStream(new FileInputStream(zipPath));
        ZipEntry entry = in.getNextEntry();
        while(entry != null) {
            String filePath = destPath + File.separator + entry.getName();
            if(!entry.isDirectory()) {
                extract(in, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            in.closeEntry();
            entry = in.getNextEntry();
        }
        in.close();
    }

    private static void extract(ZipInputStream in, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytes = new byte[4096];
        int read;
        while((read = in.read(bytes)) != -1) {
            bos.write(bytes, 0, read);
        }
        bos.close();
    }

    public static void export(InputStream in, File out, String fileName) throws IOException {
        if(!out.exists()) {
            out.mkdirs();
        }
        File filePath = new File(out, fileName);
        if(filePath.exists()) {
            return;
        }
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytes = new byte[4096];
        int read;
        while((read = in.read(bytes)) != -1) {
            bos.write(bytes, 0, read);
        }
        bos.close();
    }

    /*
        Deletes a file or recursively delete a directory.
        @param file The path/file that will be deleted.
     */
    public static void delete(File file) {
        if(file.isDirectory()) {
            if(file.list().length == 0) {
                file.delete();
            } else {
                String files[] = file.list();
                for(String path: files) {
                    File deleteFile = new File(file, path);
                    delete(deleteFile);
                }
                if(file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

	public static void rename(File oldFile, File newFile) throws IOException {
		rename(oldFile, newFile, false);
	}

	public static void rename(File oldFile, File newFile, boolean ignoreExisting) throws IOException {
		if(newFile.exists()) {
			if (ignoreExisting)
				return;
			throw new IOException("A file with the name " + newFile.getName() + " already exists");
		}
		boolean success = oldFile.renameTo(newFile);
		if(!success)
			throw new IOException("Could note rename file: " + oldFile.getName() + " to " + newFile.getName() +
					" in path " + oldFile.getParent());
	}

}
