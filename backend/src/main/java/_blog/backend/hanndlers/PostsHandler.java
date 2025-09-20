package _blog.backend.hanndlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.Entitys.Post.PostRequst;
import _blog.backend.service.CreatePostService;
import _blog.backend.service.PostsService;

@RestController
@RequestMapping("/post")
public class PostsHandler {
    @Autowired
    private PostsService postsService;

    @GetMapping
    public ResponseEntity<?> getPosts(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        return postsService.getPosts(token);
    }

    @GetMapping("/{post_id}")
    public ResponseEntity<?> getSinglePost(
            @PathVariable Long post_id,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        return ResponseEntity.ok(null);
    }

    @Autowired
    private CreatePostService createPostService;

    @PostMapping
    public ResponseEntity<?> CreatePost(@ModelAttribute PostRequst postRequst,
            @RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer", "");
        return createPostService.create(postRequst, token);
    }
}
