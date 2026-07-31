package icu.cykuta.hardcoremp.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class File {

    /**
     * Zip a world folder into the backup folder.
     * <p>
     * The name of the world is part of the zip name: the three dimensions of a
     * reset are archived within the same second, and a timestamp alone made them
     * overwrite each other.
     *
     * @param backupFolder where the zip is written
     * @param worldName    name used for the zip file
     * @param worldFolder  folder to compress
     * @return the created zip file
     */
    public static java.io.File createWorldZip(java.io.File backupFolder, String worldName, java.io.File worldFolder)
            throws IOException {
        if (worldFolder == null || !worldFolder.isDirectory()) {
            throw new IOException("World folder does not exist: " + worldFolder);
        }
        if (!backupFolder.exists() && !backupFolder.mkdirs()) {
            throw new IOException("Could not create the backup folder: " + backupFolder);
        }

        java.io.File zipFile = new java.io.File(
                backupFolder, worldName + "-" + formatMillis(System.currentTimeMillis()) + ".zip"
        );

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            zipFolder(zos, worldFolder, worldName + "/");
        }

        return zipFile;
    }

    private static void zipFolder(ZipOutputStream zos, java.io.File folder, String parentPath) throws IOException {
        java.io.File[] children = folder.listFiles();
        // listFiles() returns null when the folder cannot be read
        if (children == null) return;

        for (java.io.File file : children) {
            String zipEntryName = parentPath + file.getName();

            if (file.isDirectory()) {
                zipFolder(zos, file, zipEntryName + "/");
            } else {
                zos.putNextEntry(new ZipEntry(zipEntryName));

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                }

                zos.closeEntry();
            }
        }
    }

    static String formatMillis(long millis) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MM-dd-yyyy-HH-mm-ss");

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(millis),
                ZoneId.systemDefault()
        ).format(formatter);
    }
}
