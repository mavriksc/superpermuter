package org.mavriksc.superpermuter.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileWriter {
    public static void write(String path,String fileName, List<String> content){
        Path p = Paths.get(path);
        try {
            if (!Files.exists(p)) {
                try {
                    Files.createDirectories(p);
                } catch (IOException e) {
                    //fail to create directory
                    e.printStackTrace();
                }
            }
            File f = new File(path + "/" + fileName);

            Files.write(f.toPath(), content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
