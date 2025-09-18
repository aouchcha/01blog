package _blog.backend.helpers;

import java.util.UUID;
import java.io.*;

import org.springframework.stereotype.Component;

@Component
public class HandleMedia {
    public String Rename(String Origin) {
        String extension = "";

        int i = Origin.lastIndexOf('.');
        if (i > 0) {
            extension = Origin.substring(i);
        }

        String uniqueFilename = UUID.randomUUID().toString() + extension;
        return uniqueFilename;
    }

    public File createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) { // check if folder exists
            folder.mkdirs(); // creates parent directories too
        }
        return folder;
    }
}
