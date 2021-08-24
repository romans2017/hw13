package ua.goit.hw13http.requestPatterns;

public class User {

    private int id;
    private String name;
    private String username;
    private String email;
    private Address address;
    private String phone;
    private String website;
    private Company company;

    private static class Address {
        private String street;
        private String suite;
        private String city;
        private String zipcode;
        private Geo geo;

        private static class Geo {
            private String lat;
            private String lng;
        }
    }

    private static class Company {
        private String name;
        private String catchPhrase;
        private String bs;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}
