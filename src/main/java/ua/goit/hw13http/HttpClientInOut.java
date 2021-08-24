package ua.goit.hw13http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import ua.goit.hw13http.requestPatterns.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClientInOut {

    private final HttpClient httpClient;
    private final Gson gson;

    public HttpClientInOut(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .setPrettyPrinting()
                .create();
    }

    private Users readInputJsonFile(String filename) {
        BufferedReader bufferedReader;
        Users users = new Users();
        try {
            bufferedReader = Files.newBufferedReader(Paths.get(".\\src\\main\\resources\\" + filename));
        } catch (IOException e) {
            e.printStackTrace();
            return users;
        }

        try (bufferedReader) {
            users = gson.fromJson(bufferedReader, Users.class);
        } catch (IOException | JsonIOException | JsonSyntaxException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void postNewObject() {
        Users users = readInputJsonFile("inputUsers.json");
        System.out.println(httpClient
                .sendPostRequest(users)
                .collect(Collectors.toList()));
    }

    public void putUpdateObject() {
        Users users = readInputJsonFile("updateUsers.json");
        System.out.println(httpClient
                .sendPutRequest(users)
                .collect(Collectors.toList()));
    }

    public void deleteObjects() {
        Users users = readInputJsonFile("deleteUsers.json");
        System.out.println(httpClient
                .sendDeleteRequest(users)
                .collect(Collectors.toList()));
    }

    public void getAllUsers() {
        Users users = new Users();
        users.add(new User());

        System.out.println(httpClient
                .sendGetRequest(users, HttpClient.Filter.NO_FILTER)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getUsersById() {
        Users users = readInputJsonFile("getUsers.json");

        System.out.println(httpClient
                .sendGetRequest(users, HttpClient.Filter.FILTER_BY_ID)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getUsersByUsername() {
        Users users = readInputJsonFile("getUsers.json");

        System.out.println(httpClient
                .sendGetRequest(users, HttpClient.Filter.FILTER_BY_USERNAME)
                .map(gson::toJson)
                .collect(Collectors.toList()));
    }

    public void getComments() {
        Users users = readInputJsonFile("getUsers.json");
        System.out.println(gson.toJson(
                httpClient
                        .getCommentsByUserId(users)
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
                        })
                        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

    public void getTasks() {
        Users users = readInputJsonFile("getUsers.json");
        System.out.println(gson.toJson(
                httpClient
                        .getTasksByUserId(users)
                        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

}
