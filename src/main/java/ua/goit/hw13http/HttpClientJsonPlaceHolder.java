package ua.goit.hw13http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import ua.goit.hw13http.requestPatterns.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpClientJsonPlaceHolder {

    private static final String HOST = "https://jsonplaceholder.typicode.com";
    private static final String URL_USERS = "/users";
    private static final String URL_POSTS = "/posts";
    private static final String URL_COMMENTS = "/comments";

    private static HttpClientJsonPlaceHolder instance;
    private final HttpClient httpClient;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private enum Filter {
        NO_FILTER, FILTER_BY_ID, FILTER_BY_USERNAME
    }

    private HttpClientJsonPlaceHolder() {
        httpClient = HttpClient.newBuilder().build();
    }

    public static HttpClientJsonPlaceHolder getInstance() {
        if (instance == null) {
            instance = new HttpClientJsonPlaceHolder();
        }
        return instance;
    }

    private Users readInputJsonFile(String filename) {
        BufferedReader bufferedReader;
        Users users = null;
        try {
            bufferedReader = Files.newBufferedReader(Paths.get(".\\src\\main\\resources\\" + filename));
        } catch (IOException e) {
            e.printStackTrace();
            return users;
        }

        Gson gson = new Gson();
        try (bufferedReader) {
            users = gson.fromJson(bufferedReader, Users.class);
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return users;
    }

    private List<String> sendPostRequest(Users users) {
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
                })
                .collect(Collectors.toList());
    }

    private List<String> sendPutRequest(Users users) {
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
                })
                .collect(Collectors.toList());
    }

    private List<String> sendDeleteRequest(Users users) {
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
                })
                .collect(Collectors.toList());
    }

    private Stream<User> sendGetRequestToStream(Users users, Filter filter) {
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

    public void postNewObject() {
        Users users = readInputJsonFile("inputUsers.json");
        System.out.println(sendPostRequest(users));
    }

    public void putUpdateObject() {
        Users users = readInputJsonFile("updateUsers.json");
        System.out.println(sendPutRequest(users));
    }

    public void deleteObjects() {
        Users users = readInputJsonFile("deleteUsers.json");
        System.out.println(sendDeleteRequest(users));
    }

    public void getAllUsers() {
        Users users = new Users();
        users.add(new User());

        System.out.println(sendGetRequestToStream(users, Filter.NO_FILTER)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getUsersById() {
        Users users = readInputJsonFile("getUsers.json");

        System.out.println(sendGetRequestToStream(users, Filter.FILTER_BY_ID)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getUsersByUsername() {
        Users users = readInputJsonFile("getUsers.json");

        System.out.println(sendGetRequestToStream(users, Filter.FILTER_BY_USERNAME)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getCommentsByUserId() {
        Users users = readInputJsonFile("getUsers.json");
        System.out.println(gson.toJson(
                sendGetRequestToStream(users, Filter.FILTER_BY_ID)
                        .map(item -> {
                            int maxPostId = -1;
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
                                        .orElse(new Post())
                                        .getId();
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            return new AbstractMap.SimpleEntry<>(item, maxPostId);
                        })
                        .map(item -> {
                            Comments comments = null;
                            try {
                                comments = gson.fromJson(httpClient
                                        .send(HttpRequest
                                                        .newBuilder()
                                                        .header("Content-type", "application/json; charset=UTF-8")
                                                        .uri(URI.create(HOST + URL_POSTS + "/" + item.getValue().toString() + URL_COMMENTS))
                                                        .GET()
                                                        .build(),
                                                HttpResponse.BodyHandlers.ofString())
                                        .body(), Comments.class);
                            } catch (IOException | InterruptedException e) {
                                e.printStackTrace();
                            }
                            return new AbstractMap.SimpleEntry<>(item.getKey().getId(), comments);
                        })
                        .collect(Collectors.toConcurrentMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue))));
    }
}
