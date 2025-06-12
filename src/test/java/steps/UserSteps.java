package steps;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import io.restassured.config.SSLConfig;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.ContentType;
import config.Config;



public class UserSteps {

    static {
        RestAssured.baseURI = Config.BASE_URI; //Устанавливаем базовый URL один раз при загрузке класса
        RestAssured.config = RestAssured.config()
                .sslConfig(new SSLConfig().relaxedHTTPSValidation())
                .httpClient(HttpClientConfig.httpClientConfig() //Получить конфигурацию для HttpClient
                        .setParam("http.connection.timeout", Config.CONNECTION_TIMEOUT_MS) //сколько максимум ждать подключения к серверу
                        .setParam("http.socket.timeout", Config.SOCKET_TIMEOUT_MS) //сколько ждать ответа после подключения
                );
    }

    // Метод генерации уникального логина
    @Step("Генерация уникального логина для пользователя")
    public static String generateUniqueLogin() {
        String login = config.Config.DEFAULT_USER_LOGIN_PREFIX + System.currentTimeMillis();
        System.out.println("Сгенерирован уникальный логин: " + login);
        return login;
    }

    // Метод создания пользователя
    @Step("Создание пользователя (логин: {name})")
    public static Response createUser(String name,String email, String password) {
        String requestBody = String.format(
                "{ \"name\": \"%s\", \"email\": \"%s\", \"password\": \"%s\" }",
                name, email, password
        );

        System.out.println("Отправка запроса на создание пользователя...");

        return given()
                //.log().all()  // ← Логирует ВЕСЬ запрос (URL, headers, body)
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                //.post("/api/v1/user");
                .post(Config.USER_API);
    }
    @Step("создание пользователя с неполными данными {jsonRequestBody}")
    // Метод для тестирования неполных данных при создании пользователя
    public static Response createUserPartial(String jsonRequestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(jsonRequestBody)
                .post(Config.USER_API);
    }
    // Метод авторизации пользователя, возвращающий полный Response
    @Step("Авторизация пользователя по логину : {name} и паролю")
    public static Response userLogged(String email, String password) {
        String requestBody = String.format(
                "{ \"email\": \"%s\", \"password\": \"%s\" }",
                email, password
        );

        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post(Config.USER_LOGIN_API);
    }
    @Step("авторизация пользователя с неполными данными {jsonRequestBody}")
    // Метод для тестирования неполных данных при авторизации курьера
    public static Response userLoggedPartial(String jsonRequestBody) {
        return given()
                .contentType(ContentType.JSON)
                .body(jsonRequestBody)
                .post(Config.USER_LOGIN_API);
    }

    // Метод редактирования данных пользователя через accessToken
    @Step("Редактирование пользователя через accessToken")
    public static Response editUser(String name,String email,String accessToken) {
        String requestBody = String.format(
                "{ \"name\": \"%s\", \"email\": \"%s\" }",
                name, email
        );

        System.out.println("Редактирование пользователя: " + accessToken);

        return given()
                .header("Authorization", accessToken)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .patch(Config.USER_PATCH_API);
      }
    @Step("Редактирование пользователя без токена")
    public static Response editUserWithoutToken(String name, String email) {
        String requestBody = String.format(
                "{ \"name\": \"%s\", \"email\": \"%s\" }",
                name, email
        );

        return given()
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .patch(Config.USER_PATCH_API);
    }
    // Метод удаления пользователя через accessToken
    @Step("Удаление пользователя через accessToken")
    public static void deleteUser(String accessToken) {
        System.out.println("Удаление пользователя с токеном: " + accessToken);

        given()
                .header("Authorization", accessToken)
                .when()
                .delete(Config.USER_DEL_API)
                .then()
                .statusCode(Config.STATUS_CODE_ACCEPTED); // не совсем по семантике HTTP но так настроено

        System.out.println("Пользователь успешно удалён");
    }

    // Метод очистки тестовых данных
    @Step("Очистка тестовых данных: удаление пользователя через accessToken")
    public static void cleanUp(String accessToken) {
        try {
            if (accessToken != null && !accessToken.isEmpty()) {
                deleteUser(accessToken);
            } else {
                System.err.println("Токен доступа пустой, удаление невозможно");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
        }
    }
}