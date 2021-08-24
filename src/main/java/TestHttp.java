import ua.goit.hw13http.HttpClient;
import ua.goit.hw13http.HttpClientInOut;

public class TestHttp {
    public static void main(String[] args) {
        HttpClientInOut inOut = new HttpClientInOut(HttpClient.getInstance());

        System.out.println("Post new users");
        inOut.postNewObject();

        System.out.println("*******************************************************");
        System.out.println("Update users");
        inOut.putUpdateObject();

        System.out.println("*******************************************************");
        System.out.println("Delete users");
        inOut.deleteObjects();

        System.out.println("*******************************************************");
        System.out.println("Get all users");
        inOut.getAllUsers();

        System.out.println("*******************************************************");
        System.out.println("Get users by ID");
        inOut.getUsersById();

        System.out.println("*******************************************************");
        System.out.println("Get users by Username");
        inOut.getUsersByUsername();

        System.out.println("*******************************************************");
        System.out.println("Get comments of users' last post");
        inOut.getComments();

        System.out.println("*******************************************************");
        System.out.println("Get users' uncompleted tasks");
        inOut.getTasks();
    }
}
