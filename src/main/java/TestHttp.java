import ua.goit.hw13http.HttpClientJsonPlaceHolder;

public class TestHttp {
    public static void main(String[] args) {
        HttpClientJsonPlaceHolder httpClientJsonPlaceHolder = HttpClientJsonPlaceHolder.getInstance();
        //httpClientJsonPlaceHolder.postNewObject();
        //httpClientJsonPlaceHolder.putUpdateObject();
        //httpClientJsonPlaceHolder.deleteObjects();
        //httpClientJsonPlaceHolder.getAllUsers();
        /*httpClientJsonPlaceHolder.getUsersById();
        System.out.println();
        System.out.println("TEST getUsersByUsername");
        httpClientJsonPlaceHolder.getUsersByUsername();*/
        httpClientJsonPlaceHolder.getCommentsByUserId();
    }
}
