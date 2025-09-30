package _blog.backend.Entitys.User;

public class UserStatsDTO {
    private Long id;
    private String username;
    private String email;
    private Long postCount;
    private Long reportCount;

    public UserStatsDTO(Long id, String username, String email, Long postCount, Long reportCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.postCount = postCount;
        this.reportCount = reportCount;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Long getPostCount() { return postCount; }
    public Long getReportCount() { return reportCount; }
}