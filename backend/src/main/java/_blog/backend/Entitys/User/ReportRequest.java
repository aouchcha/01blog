package _blog.backend.Entitys.User;

public class ReportRequest {

    private String reportted_username;

    private String discription;

    private String type;

    private Long post_id;

    public String getReportted_username() {
        return reportted_username;
    }

    public String getDiscription() {
        return discription;
    }

    public String getType() {
        return type;
    }

    public Long getPost_id() {
        return post_id;
    }

}
