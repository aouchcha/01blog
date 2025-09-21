package _blog.backend.helpers;

import java.util.List;
import java.util.UUID;
import java.io.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import _blog.backend.Entitys.Post.Post;
import _blog.backend.Repos.CommentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
public class HandleMedia {
    @Autowired
    private CommentRepository commentRepository;

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

    public List<Post> FixUrl(List<Post> posts) {
        for (Post p : posts) {
            entityManager.detach(p);
            p.setMedia("http://localhost:8080/uploads/" + p.getMedia());
            p.setCommentsCount(commentRepository.countByPost_id(p.getId()));
        }
        return posts;
    }
}
