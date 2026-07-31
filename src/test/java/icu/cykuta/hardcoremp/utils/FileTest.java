package icu.cykuta.hardcoremp.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;

class FileTest {

    @TempDir
    java.io.File root;

    @Test
    @DisplayName("zips the whole world folder keeping the tree")
    void zipsTheWorldFolder() throws Exception {
        java.io.File world = worldFolder("hcmp_game");
        java.io.File backups = new java.io.File(root, "old_worlds");

        java.io.File zip = File.createWorldZip(backups, "hcmp_game", world);

        assertTrue(zip.isFile());
        List<String> entries = entriesOf(zip);
        assertTrue(entries.contains("hcmp_game/level.dat"), entries.toString());
        assertTrue(entries.contains("hcmp_game/region/r.0.0.mca"), entries.toString());
    }

    @Test
    @DisplayName("the three dimensions of a reset produce three different zips")
    void oneZipPerWorld() throws Exception {
        java.io.File backups = new java.io.File(root, "old_worlds");

        // Same second for the three of them: the timestamp alone was not unique
        // and two of the three backups used to overwrite the first one.
        java.io.File overworld = File.createWorldZip(backups, "hcmp_game", worldFolder("hcmp_game"));
        java.io.File nether = File.createWorldZip(backups, "hcmp_game_nether", worldFolder("hcmp_game_nether"));
        java.io.File end = File.createWorldZip(backups, "hcmp_game_the_end", worldFolder("hcmp_game_the_end"));

        assertNotEquals(overworld.getName(), nether.getName());
        assertNotEquals(nether.getName(), end.getName());
        assertEquals(3, backups.listFiles().length);
    }

    @Test
    @DisplayName("the backup folder is created when it does not exist")
    void createsBackupFolder() throws Exception {
        java.io.File backups = new java.io.File(root, "nested/old_worlds");

        File.createWorldZip(backups, "hcmp_game", worldFolder("hcmp_game"));

        assertTrue(backups.isDirectory());
    }

    @Test
    @DisplayName("a missing world folder is reported instead of writing an empty zip")
    void missingWorldFolderFails() {
        java.io.File backups = new java.io.File(root, "old_worlds");
        java.io.File missing = new java.io.File(root, "does_not_exist");

        assertThrows(IOException.class, () -> File.createWorldZip(backups, "hcmp_game", missing));
    }

    private java.io.File worldFolder(String name) throws IOException {
        java.io.File world = new java.io.File(root, name);
        java.io.File region = new java.io.File(world, "region");
        assertTrue(region.mkdirs());

        Files.write(new java.io.File(world, "level.dat").toPath(), "level".getBytes(StandardCharsets.UTF_8));
        Files.write(new java.io.File(region, "r.0.0.mca").toPath(), "region".getBytes(StandardCharsets.UTF_8));
        return world;
    }

    private List<String> entriesOf(java.io.File zip) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(zip)) {
            java.util.Enumeration<? extends ZipEntry> it = zipFile.entries();
            while (it.hasMoreElements()) names.add(it.nextElement().getName());
        }
        return names;
    }
}
