import config.Config;
import io.qameta.allure.*;
import io.restassured.response.Response;
import static org.junit.Assert.*;
import org.junit.Test;// импортируем Test
import static steps.UserSteps.*;  // или import steps.CounterSteps;
import io.qameta.allure.junit4.DisplayName; // импорт DisplayName
import org.junit.FixMethodOrder; //упорядочивние тестов в аллюр
import org.junit.runners.MethodSorters;


@Epic("API пользователя")
@Feature("создание уникального пользователя")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserTest {

    @Test
    @DisplayName("#1 - Успешная авторизация пользователя с полными данными")
    @Description("Создание пользователя с уникальными данными")

    public void test1_creating_User_Logged() {
        // Генерируем уникальное имя пользователя
        String uniqueName = generateUniqueLogin();

        // Формируем уникальный email на основе имени
        String email = uniqueName + "@mail.ru";

        // Отправляем запрос на создание пользователя (регистрацию)
        Response createResponse = createUser(uniqueName, email, Config.DEFAULT_PASSWORD);

        // Проверяем, что регистрация прошла успешно
        assertEquals("Неверный статус код при регистрации пользователя",
                Config.STATUS_CODE_OK,
                createResponse.getStatusCode());

        // Проверяем, что тело содержит поле success:true
        assertTrue("Регистрация неуспешна", createResponse.jsonPath().getBoolean("success"));

        // Авторизуемся теми же email и паролем
        Response loggedResponse = userLogged(email, Config.DEFAULT_PASSWORD);

        // Получаем статус код и тело ответа
        int statusCode = loggedResponse.getStatusCode();
        String responseBody = loggedResponse.getBody().asString();
        // Извлекаем токен
        String rawAccessToken = loggedResponse.jsonPath().getString("accessToken");

//        // Удаляем префикс "Bearer ", если он есть
//        String cleanToken = rawAccessToken != null && rawAccessToken.startsWith("Bearer ")
//                ? rawAccessToken.substring(7)
//                : rawAccessToken;
        try {
            // Проверяем, что тело ответа не пустое
            assertFalse("Тело ответа пустое", responseBody.isEmpty());

            // Проверяем, что авторизация прошла успешно
            assertTrue("Авторизация неуспешна", loggedResponse.jsonPath().getBoolean("success"));

            // Проверяем, что в ответе присутствует поле user.email
            assertEquals("Email в ответе не совпадает с ожидаемым",
                    email,
                    loggedResponse.jsonPath().getString("user.email"));

            // Проверяем, что в ответе присутствует accessToken
            String accessToken = loggedResponse.jsonPath().getString("accessToken");
            assertNotNull("accessToken отсутствует", accessToken);
            assertTrue("accessToken пустой", !accessToken.isEmpty());

            // Проверяем, что статус код 200 (успешная авторизация)
            assertEquals("Неверный статус код при авторизации пользователя",
                    Config.STATUS_CODE_OK,
                    statusCode);
        } finally {
            // Удаляем созданного пользователя (если предусмотрен метод удаления)
            cleanUp(rawAccessToken);
        }
    }


//    @Test
//    @DisplayName("#2 - Авторизация пользователя без логина → 400")
//    @Description("Проверка, что API возвращает статус 400, Ответ: \"Недостаточно данных для входа\" ")
//    public void test2_loggedUserWithoutLogin_ShouldReturn400() {
//        String json = "{"
//                + "\"password\": \"" + Config.DEFAULT_PASSWORD + "\""
//                + "}";
//        Response response = userLoggedPartial(json);
//        assertEquals(
//                "Ожидается статус 400 (Bad Request)",
//                Config.STATUS_CODE_CLIENT_ERROR,
//                response.getStatusCode());
//        assertEquals(
//                "Недостаточно данных для входа",
//                response.jsonPath().getString("message")
//        );
//    }
//    @Test
//    @DisplayName("#3 - Авторизация пользователя без пароля → 400")
//    @Description("Проверка, что API возвращает статус 400, Ответ: \"Недостаточно данных для входа\" ")
//    public void test3_loggedUserWithoutPassword_ShouldReturn400() {
//        String json = "{"
//                + "\"login\": \"" + Config.DEFAULT_USER_LOGIN_PREFIX + "\""
//                + "}";
//
//        Response response = userLoggedPartial(json);
//        assertEquals(
//                "Ожидается статус 400 (Bad Reqest)",
//                Config.STATUS_CODE_CLIENT_ERROR,
//                response.getStatusCode());
//        assertEquals(
//                "Недостаточно данных для входа",
//                response.jsonPath().getString("message")
//        );
//
//    }
//    @Test
//    @DisplayName("#4 - Авторизация пользователя с password = \"\" → 400")
//    @Description("Проверка, что API возвращает статус 400, Ответ: \"Недостаточно данных для входа\" ")
//    public void test4_loggedUserWithoutPasswordEmpty_Return400() {
//        String json = "{"
//                + "\"login\": \"" + Config.DEFAULT_USER_LOGIN_PREFIX + "\","
//                + "\"password\": \"\""
//                + "}";
//
//        Response response = userLoggedPartial(json);
//        assertEquals(
//                "Ожидается статус 400 (Bad Reqest)",
//                Config.STATUS_CODE_CLIENT_ERROR,
//                response.getStatusCode());
//        assertEquals(
//                "Недостаточно данных для входа",
//                response.jsonPath().getString("message")
//        );
//
//    }
//
//    @Test
//    @DisplayName("#5 - Авторизация пользователя с несуществующим логином")
//    @Description("Проверка, что API возвращает статус 404, Ответ: \"Учетная запись не найдена\" ")
//    public void test5_loggedUserNonexistentLogin_ShouldReturn404() {
//
//        String login = generateUniqueLogin(); // Генерация уникального логина
//        Response loggedResponse = userLogged(login, Config.DEFAULT_PASSWORD); // Отправляем запрос на авторизацию
//        try {
//            // Проверка тела ответа
//            assertFalse("Тело ответа пустое", loggedResponse.getBody().asString().isEmpty());
//
//            // Проверяем текст ответа
//            assertEquals("Учетная запись не найдена", loggedResponse.jsonPath().getString("message"));
//            // Ожидаем статус кода 404 (Not Found)
//            assertEquals("Статус код должен быть 404 (Not Found)",
//                    Config.STATUS_CODE_NOT_FOUND,
//                    loggedResponse.getStatusCode());
//        } finally {
//            // Очистка, если нужно
//        }
//    }
//    @Test
//    @DisplayName("#6 - Авторизация пользователя с неверным паролем")
//    @Description("Проверка, что API возвращает статус 404, Ответ: \"Учетная запись не найдена\" ")
//    public void test6_loggedUserNonexistentPassword_ShouldReturn404() {
//
//        String login = generateUniqueLogin(); // Генерация уникального логина
//        Response createResponse = createUser(login, Config.DEFAULT_PASSWORD, Config.DEFAULT_FIRST_NAME);// Отправляем запрос на создание курьера
//        Response loggedResponse = userLogged(login, Config.DEFAULT_PASSWORD+"xxx"); // Отправляем запрос на авторизацию
//
//        try {
//            // Проверка тела ответа
//            assertFalse("Тело ответа пустое", loggedResponse.getBody().asString().isEmpty());
//            // Проверяем текст ответа
//            assertEquals("Учетная запись не найдена", loggedResponse.jsonPath().getString("message"));
//            // Ожидаем статус кода 404 (Not Found)
//            assertEquals("Статус код должен быть 404 (Not Found)",
//                    Config.STATUS_CODE_NOT_FOUND,
//                    loggedResponse.getStatusCode());
//        } finally {
//            cleanUp(login, Config.DEFAULT_PASSWORD); // Удаляем созданного курьера, даже если упадет тест
//        }
//    }
//

}