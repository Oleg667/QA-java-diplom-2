package config;

public class Config {
    // Базовые URL
    public static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    public static final String USER_API = "api/auth/register"; //POST создание пользователя
    public static final String USER_LOGIN_API = "api/auth/login"; //POST авторизация пользователя
    public static final String NEW_ORDER_API = "/api/orders"; //POST создание заказа
    public static final String ORDER_API = "/api/orders/all"; //GET получение всех заказов
    public static final String USER_ORDER_API = "/api/orders"; //GET получение заказов пользователя
    public static final String USER_DEL_API = "/api/auth/user"; //DEL удаление пользователя
    public static final String USER_PATCH_API = "/api/auth/user"; //DEL удаление пользователя
    public static final String INGRADIENT_API = "/api/ingredients"; //GET получение списка инградиентов



    // Тестовые данные
    public static final String DEFAULT_USER_LOGIN_PREFIX = "auto_test_user_";
    public static final String DEFAULT_PASSWORD = "667667";
    public static final String DEFAULT_FIRST_NAME = "Oleg";

    // Статус-коды
    public static final int STATUS_CODE_CREATED = 201;
    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_CODE_CLIENT_ERROR = 400;
    public static final int STATUS_CODE_UNAUTHORIZED = 401;
    public static final int STATUS_CODE_NOT_FOUND = 404;
    public static final int STATUS_CODE_ACCEPTED = 202;
    public static final int STATUS_CODE_FORBIDDEN = 403;

    // Таймауты
    public static final int CONNECTION_TIMEOUT_MS = 5000;
    public static final int SOCKET_TIMEOUT_MS = 5000;

}