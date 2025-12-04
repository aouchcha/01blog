package _blog.backend.helpers;

import java.util.UUID;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import _blog.backend.Entitys.Post.Post;
import _blog.backend.Entitys.Post.PostRequst;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class HandleMedia {

    @PersistenceContext
    private EntityManager entityManager;

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

    public boolean save(Post p, PostRequst postRequst) {
        // System.out.println("HaNNI F SAVE");
        if (postRequst.getMedia() != null) {
            p.setMedia(Rename(postRequst.getMedia().getOriginalFilename()));
            File uploads = createFolder("../backend/uploads");
            Path dest = Paths.get(uploads.getAbsolutePath(), p.getMedia());
            try {
                postRequst.getMedia().transferTo(dest);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

}
