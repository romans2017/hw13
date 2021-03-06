package ua.goit.hw13http.requestPatterns;

public class Post {
    private final int userId;
    private final int id;
    private String title;
    private String body;

    public Post() {
        this.userId = -1;
        this.id = -1;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }
}
