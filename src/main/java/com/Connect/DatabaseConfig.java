package com.Connect;


public final class DatabaseConfig {

    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String PARAMS = "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";

    public static final String ENV_PROD = "production";
    public static final String ENV_TEST = "test";
    public static final String DB_PROD = "flowershop";
    public static final String DB_TEST = "flowershop_test";

    private DatabaseConfig() {}

    public static String getEnv() {
        return System.getProperty("db.env", ENV_PROD);
    }

    public static String getUrl() {
        String db = ENV_TEST.equals(getEnv()) ? DB_TEST : DB_PROD;
        return BASE_URL + db + PARAMS;
    }

    public static String getUser() {
        return DB_USER;
    }

    public static String getPassword() {
        return DB_PASSWORD;
    }
}
