package icu.cykuta.hardcoremp.utils;

import icu.cykuta.hardcoremp.HardcoreMP;
import org.bukkit.World;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class File {
    public static void createWorldsZip(World[] worlds) throws IOException {
        java.io.File pluginFolder = HardcoreMP.getPlugin().getDataFolder();
        java.io.File oldWorldsFolder = new java.io.File(pluginFolder, "old_worlds");
        if (!oldWorldsFolder.exists()) {
            oldWorldsFolder.mkdirs();
        }

        String timestamp = formatMillis(System.currentTimeMillis());
        java.io.File zipFile = new java.io.File(
                oldWorldsFolder, timestamp + ".zip"
        );

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (World world : worlds) {
                if (world == null) continue;

                java.io.File worldFolder = world.getWorldFolder();
                if (worldFolder == null || !worldFolder.exists()) continue;

                zipFolder(zos, worldFolder, worldFolder.getName() + "/");
            }
        }
    }

    private static void zipFolder(ZipOutputStream zos, java.io.File folder, String parentPath) throws IOException {
        for (java.io.File file : Objects.requireNonNull(folder.listFiles())) {
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

    private static String formatMillis(long millis) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MM-dd-yyyy-HH-mm-ss");

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(millis),
                ZoneId.systemDefault()
        ).format(formatter);
    }
}


