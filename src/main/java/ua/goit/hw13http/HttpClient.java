package ua.goit.hw13http;

import com.google.gson.*;
import ua.goit.hw13http.requestPatterns.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class HttpClient {

    private static final String HOST = "https://jsonplaceholder.typicode.com";
    private static final String URL_USERS = "/users";
    private static final String URL_POSTS = "/posts";
    private static final String URL_COMMENTS = "/comments";
    private static final String URL_TODOS = "/todos";

    private static HttpClient instance;
    private final java.net.http.HttpClient httpClient;
    private final Gson gson;

    enum Filter {
        NO_FILTER, FILTER_BY_ID, FILTER_BY_USERNAME
    }

    private HttpClient() {
        httpClient = java.net.http.HttpClient.newBuilder().build();
        gson = new Gson();
    }

    public static HttpClient getInstance() {
        if (instance == null) {
            instance = new HttpClient();
        }
        return instance;
    }

    Stream<String> sendPostRequest(Users users) {
        return users
                .parallelStream()
                .map(item -> {
                    try {
                        return httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .uri(URI.create(HOST + URL_USERS))
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(item)))
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    Stream<String> sendPutRequest(Users users) {
        return users
                .parallelStream()
                .map(item -> {
                    try {
                        return httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(HOST + URL_USERS + "/" + item.getId()))
                                                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(item)))
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    Stream<String> sendDeleteRequest(Users users) {
        return users
                .parallelStream()
                .map(item -> {
                    try {
                        return httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(HOST + URL_USERS + "/" + item.getId()))
                                                .DELETE()
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).map(item -> {
                    if (item == null) {
                        return "500 {}";
                    } else {
                        return item.statusCode() + " " + item.body();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    Stream<User> sendGetRequest(Users users, Filter filter) {
        return users
                .parallelStream()
                .map(item -> {
                    Class classFromJson = Users.class;
                    String url = HOST + URL_USERS;
                    if (filter.equals(Filter.FILTER_BY_USERNAME)) {
                        url += "?username=" + item.getUsername();
                    } else if (filter.equals(Filter.FILTER_BY_ID)) {
                        url += "/" + item.getId();
                        classFromJson = User.class;
                    }
                    try {
                        return gson.fromJson(httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(url))
                                                .GET()
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body(), classFromJson);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .flatMap(item -> {
                    if (item instanceof Users) {
                        return ((Users) item).stream();
                    } else {
                        return Stream.of((User) item);
                    }
                });
    }

    public Stream<Map.Entry<Post, Comments>> getCommentsByUserId(Users users) {
        return sendGetRequest(users, Filter.FILTER_BY_ID)
                .map(item -> {
                    Post maxPostId = new Post();
                    try {
                        maxPostId = gson.fromJson(httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(HOST + URL_USERS + "/" + item.getId() + URL_POSTS))
                                                .GET()
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body(), Posts.class)
                                .parallelStream()
                                .max(Comparator.comparingInt(Post::getId))
                                .orElse(new Post());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Map.entry(item, maxPostId);
                })
                .map(item -> {
                    Comments comments = null;
                    try {
                        comments = gson.fromJson(httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(HOST + URL_POSTS + "/" + item.getValue().getId() + URL_COMMENTS))
                                                .GET()
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body(), Comments.class);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Map.entry(item.getValue(), comments);
                })
                .peek(item -> {
                    String outPath = ".\\src\\main\\resources\\out\\";
                    if (!Files.exists(Paths.get(outPath))) {
                        try {
                            Files.createDirectory(Paths.get(outPath));
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    try (PrintStream printStream = new PrintStream(outPath + "user-" + item.getKey().getUserId() + "-post-" + item.getKey().getId() + "-comments.json")) {
                        printStream.print(gson.toJson(item.getValue()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    public Stream<Map.Entry<User, ToDos>> getTasksByUserId(Users users) {
        return sendGetRequest(users, Filter.FILTER_BY_ID)
                .map(item -> {
                    ToDos toDos = new ToDos();
                    try {
                        toDos = gson.fromJson(httpClient
                                .send(HttpRequest
                                                .newBuilder()
                                                .header("Content-type", "application/json; charset=UTF-8")
                                                .uri(URI.create(HOST + URL_USERS + "/" + item.getId() + URL_TODOS))
                                                .GET()
                                                .build(),
                                        HttpResponse.BodyHandlers.ofString())
                                .body(), ToDos.class)
                                .parallelStream()
                                .filter(itemTodo -> !itemTodo.isCompleted())
                                .collect(ToDos::new, ToDos::add, ToDos::addAll);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Map.entry(item, toDos);
                });
    }
}
