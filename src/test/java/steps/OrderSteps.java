package steps;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import io.restassured.config.SSLConfig;
import io.restassured.config.HttpClientConfig;
import config.Config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderSteps {

    static {
        RestAssured.baseURI = Config.BASE_URI; //Устанавливаем базовый URL один раз при загрузке класса
        RestAssured.config = RestAssured.config()
                .sslConfig(new SSLConfig().relaxedHTTPSValidation())
                .httpClient(HttpClientConfig.httpClientConfig() //Получить конфигурацию для HttpClient
                        .setParam("http.connection.timeout", Config.CONNECTION_TIMEOUT_MS) //сколько максимум ждать подключения к серверу
                        .setParam("http.socket.timeout", Config.SOCKET_TIMEOUT_MS) //сколько ждать ответа после подключения
                );
    }
    @Step("Получить ответ со всеми ингредиентами")
    public static Response getAllIngredients() {
        return given()
                .when()
                .get(Config.INGRADIENT_API)
                .then()
                .statusCode(200)
                .extract()
                .response();
    }

    @Step("Выбрать {n} случайных ингредиентов для заказа")
    public static Map<String, List<String>> getRandomIngredientsPayload(Response ingredientsResponse, int n) {
        List<String> allIds = ingredientsResponse.jsonPath().getList("data._id");

        if (allIds.size() < n) {
            throw new IllegalArgumentException("Недостаточно ингредиентов: требуется " + n + ", а доступно " + allIds.size());
        }

        Collections.shuffle(allIds); //обеспечиват выбор случайных элементов
        List<String> selected = allIds.subList(0, n);

        Map<String, List<String>> payload = new HashMap<>();
        payload.put("ingredients", selected);
        return payload;
    }

    @Step("Создать заказ с ингредиентами без авторизации")
    public static Response createOrder(Map<String, List<String>> orderBody) {
        return given()
                .header("Content-Type", "application/json")
                .body(orderBody)
                .when()
                .post(Config.NEW_ORDER_API)
                .then()
                .extract()
                .response();
    }
    @Step("Создать заказ с ингредиентами с авторизацией")
    public static Response createOrderAuthor(Map<String, List<String>> orderBody, String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .header("Content-Type", "application/json")
                .body(orderBody)
                .when()
                .post(Config.NEW_ORDER_API)
                .then()
                .extract()
                .response();
    }
    @Step("Список заказов пользователя")
    public static Response OrderUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .get(Config.USER_ORDER_API)
                .then()
                .extract()
                .response();
    }
    @Step("Список Всех заказов")
    public static Response OrderAll() {

        return given()
                //.log().all() // логирует весь запрос
                .when()
                .get(Config.ORDER_API)
                .then()
                //.log().all() // логирует весь ответ
                .extract()
                .response();
    }

}

