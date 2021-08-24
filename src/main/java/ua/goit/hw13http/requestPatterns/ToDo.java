package ua.goit.hw13http.requestPatterns;

public class ToDo {
    private int userId;
    private int id;
    private String title;
    private boolean completed;

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public boolean isCompleted() {
        return completed;
    }
}
