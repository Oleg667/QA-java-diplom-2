
import config.Config;
import io.qameta.allure.*;
import io.restassured.response.Response;
import static org.junit.Assert.*;
import org.junit.Test;// импортируем Test
import static steps.UserSteps.*;  // или import steps.CounterSteps;
import static steps.OrderSteps.*;
import io.qameta.allure.junit4.DisplayName; // импорт DisplayName
import org.junit.FixMethodOrder; //упорядочивние тестов в аллюр
import org.junit.runners.MethodSorters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



@Epic("API пользователя")
@Feature("Создание и получение заказов")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class OrderTest {

@Test
@DisplayName("#1 - Создание заказа без авторизации")
@Description("Только ингредиенты ")
public void test_CreateOrder_WithoutAuthorization() {
    int ingredientsCount = 4; //Количество ингредиентов

    // Шаг 1: Получаем все ингредиенты
    Response ingredientsResponse = getAllIngredients();

    // Шаг 2: Формируем тело с  случайными ингредиентами
    Map<String, List<String>> requestBody = getRandomIngredientsPayload(ingredientsResponse, ingredientsCount);

    // Шаг 3: Создаём заказ без авторизации
    Response orderResponse = createOrder(requestBody);

    // Проверки
    assertEquals("Ожидался статус 200 OK", Config.STATUS_CODE_OK, orderResponse.getStatusCode());
    assertTrue("Ожидалось, что success=true", orderResponse.jsonPath().getBoolean("success"));
    assertTrue("success должен быть true", orderResponse.jsonPath().getBoolean("success"));

    // Проверка, что поле order не null
    assertNotNull("Поле 'order' должно быть", orderResponse.jsonPath().getMap("order"));

    // Проверка, что в order есть number и он не null
    Integer orderNumber = orderResponse.jsonPath().getInt("order.number");
    assertNotNull("Номер заказа не должен быть null", orderNumber);
    assertTrue("Номер заказа должен быть положительным", orderNumber > 0);
}
    @Test
    @DisplayName("#2 - Создание заказа с авторизации")
    @Description("Только ингредиенты ")
    public void test_CreateOrder_Authorization() {
        int ingredientsCount = 4; //количество ингредиентов
        // создание пользователя
        String uniqueName = generateUniqueLogin();
        String email = uniqueName + "@mail.ru";
        createUser(uniqueName, email, Config.DEFAULT_PASSWORD);
        Response loggedResponse = userLogged(email, Config.DEFAULT_PASSWORD);
        String rawAccessToken = loggedResponse.jsonPath().getString("accessToken");

        // Шаг 1: Получаем все ингредиенты
        Response ingredientsResponse = getAllIngredients();

        // Шаг 2: Формируем тело с случайными ингредиентами
        Map<String, List<String>> requestBody = getRandomIngredientsPayload(ingredientsResponse, ingredientsCount);

        // Шаг 3: Создаём заказ c авторизацией
        Response orderResponse = createOrderAuthor(requestBody,rawAccessToken);
        try {
        // Проверки
        assertEquals("Ожидался статус 200 OK", Config.STATUS_CODE_OK, orderResponse.getStatusCode());
        assertTrue("Ожидалось, что success=true", orderResponse.jsonPath().getBoolean("success"));
        assertTrue("success должен быть true", orderResponse.jsonPath().getBoolean("success"));

        // Проверка, что поле order не null
        assertNotNull("Поле 'order' должно быть", orderResponse.jsonPath().getMap("order"));

        // Проверка, что в order есть number и он не null
        Integer orderNumber = orderResponse.jsonPath().getInt("order.number");
        assertNotNull("Номер заказа не должен быть null", orderNumber);
        assertTrue("Номер заказа должен быть положительным", orderNumber > 0);

        // Проверка совпадения имени и email владельца
        Map<String, Object> owner = orderResponse.jsonPath().getMap("order.owner");
        assertNotNull("Поле 'owner' должно быть", owner);
        assertEquals("Имя владельца не совпадает", uniqueName, owner.get("name"));
        assertEquals("Email владельца не совпадает", email, owner.get("email"));
        } finally {
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }

    }

    @Test
    @DisplayName("#3 - Создание заказа без ингредиентов -> 400" )
    @Description("Без авторизации ")
    public void test_CreateOrder_WithoutIngredients() {
        int ingredientsCount = 0; //без ингредиентов

        // Шаг 1: Получаем все ингредиенты
        Response ingredientsResponse = getAllIngredients();

        // Шаг 2: Формируем тело
        Map<String, List<String>> requestBody = getRandomIngredientsPayload(ingredientsResponse, ingredientsCount);

        // Шаг 3: Создаём заказ без авторизации
        Response orderResponse = createOrder(requestBody);

        // Проверки
        assertEquals("Ожидался статус 400 Bad Request",Config.STATUS_CODE_CLIENT_ERROR, orderResponse.getStatusCode());
        assertFalse("Ожидалось success=false", orderResponse.jsonPath().getBoolean("success"));

        // Проверка сообщения об ошибке
        String expectedMessage = "Ingredient ids must be provided";
        String actualMessage = orderResponse.jsonPath().getString("message");
        assertEquals("Неверное сообщение об ошибке", expectedMessage, actualMessage);
    }

}
