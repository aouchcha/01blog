package _blog.backend.Entitys.Post;

import org.springframework.web.multipart.MultipartFile;

public class PostRequst {
    private String description;
    private MultipartFile media;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile getMedia() {
        return media;
    }

    public void setMedia(MultipartFile media) {
        this.media = media;
    }

}
