package _blog.backend.hanndlers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import _blog.backend.Entitys.Comment.CommentRequest;
import _blog.backend.service.CommentsService;

@RestController
@CrossOrigin(origins = { "http://localhost:4200" })
@RequestMapping("/api/comment")

public class CommentController {
    private final CommentsService commentsService;

    public CommentController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    @PostMapping
    public ResponseEntity<?> CreateComments(@RequestBody CommentRequest commentRequest) {
        return commentsService.create(commentRequest);
    }

    @DeleteMapping("/{comment_id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long comment_id
        ) {
        return commentsService.delete(comment_id);
    }
}
