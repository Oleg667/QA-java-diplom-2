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
@Feature("создание, авторизация пользователя")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserTest {

    @Test
    @DisplayName("#1 - Создание уникального пользователя с полными данными")
    @Description("Регистрация под созданным пользователем с полными данными")

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
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }
    }


    @Test
    @DisplayName("#2 - Авторизация пользователя без логина → 401")
    @Description("Проверка, что API возвращает статус 401, Ответ: \"email or password are incorrect\" ")
    public void test2_loggedUserWithoutLogin_ShouldReturn401() {
        String json = "{"
                + "\"password\": \"" + Config.DEFAULT_PASSWORD + "\""
                + "}";
        Response response = userLoggedPartial(json);
        assertFalse("success должен быть false", response.jsonPath().getBoolean("success"));
        assertEquals(
                "Ожидается статус 401 (Bad Request)",
                Config.STATUS_CODE_UNAUTHORIZED,
                response.getStatusCode());
        assertEquals(
                "email or password are incorrect",
                response.jsonPath().getString("message")
        );
    }
    @Test
    @DisplayName("#3 - Авторизация пользователя без пароля → 401")
    @Description("Проверка, что API возвращает статус 401, Ответ: \"email or password are incorrect\" ")
    public void test3_loggedUserWithoutPassword_ShouldReturn401() {
        String json = "{"
                + "\"login\": \"" + Config.DEFAULT_USER_LOGIN_PREFIX + "\""
                + "}";

        Response response = userLoggedPartial(json);
        assertFalse("success должен быть false", response.jsonPath().getBoolean("success"));
        assertEquals(
                "Ожидается статус 400 (Bad Reqest)",
                Config.STATUS_CODE_UNAUTHORIZED,
                response.getStatusCode());
        assertEquals(
                "email or password are incorrect",
                response.jsonPath().getString("message")
        );

    }
    @Test
    @DisplayName("#4 - Создание пользователя который уже зарегистрирован → 403")
    @Description("Проверка, что API возвращает статус 403, Ответ: \"User already exists\" ")
    public void test4_testRegistrationWithExistingLogin_Return403() {
        // Генерируем уникальное имя пользователя
        String uniqueName = generateUniqueLogin();

        // Формируем уникальный email на основе имени
        String email = uniqueName + "@mail.ru";

        // Отправляем запрос на создание пользователя (регистрацию)
        Response createResponse = createUser(uniqueName, email, Config.DEFAULT_PASSWORD);

        // Извлекаем токен
        String rawAccessToken = createResponse.jsonPath().getString("accessToken");
        // Отправляем повторно запрос на создание
        Response createResponseRet = createUser(uniqueName, email, Config.DEFAULT_PASSWORD);
        try {
            // Проверка тела ответа
            assertFalse("Тело ответа пустое", createResponseRet.getBody().asString().isEmpty());
            assertFalse("success должен быть false", createResponseRet.jsonPath().getBoolean("success"));
            // Проверяем текст ответа
            assertEquals("User already exists", createResponseRet.jsonPath().getString("message"));
            // Ожидаем статус кода 403 (Not Found)
            assertEquals("Статус код должен быть 403",
                    Config.STATUS_CODE_FORBIDDEN,
                    createResponseRet.getStatusCode());
        } finally {
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }
    }

    @Test
    @DisplayName("#5 - Авторизация пользователя с несуществующим логином")
    @Description("Проверка, что API возвращает статус 401, Ответ: \"email or password are incorrect\" ")
    public void test5_loggedUserNonexistentLogin_ShouldReturn403() {

        String login = generateUniqueLogin(); // Генерация уникального логина
        Response loggedResponse = userLogged(login, Config.DEFAULT_PASSWORD); // Отправляем запрос на авторизацию
        try {
            // Проверка тела ответа
            assertFalse("Тело ответа пустое", loggedResponse.getBody().asString().isEmpty());
            assertFalse("success должен быть false", loggedResponse.jsonPath().getBoolean("success"));
            // Проверяем текст ответа
            assertEquals("email or password are incorrect", loggedResponse.jsonPath().getString("message"));
            // Ожидаем статус кода 401 (Not Found)
            assertEquals("Статус код должен быть 401 (Not Found)",
                    Config.STATUS_CODE_UNAUTHORIZED,
                    loggedResponse.getStatusCode());
        } finally {
            // Очистка, если нужно
        }
    }
    @Test
    @DisplayName("#6 - Авторизация пользователя с неверным паролем")
    @Description("Проверка, что API возвращает статус 401, Ответ: \"email or password are incorrect\" ")
    public void test6_loggedUserNonexistentPassword_ShouldReturn401() {

        // Генерируем уникальное имя пользователя
        String uniqueName = generateUniqueLogin();

        // Формируем уникальный email на основе имени
        String email = uniqueName + "@mail.ru";

        // Отправляем запрос на создание пользователя (регистрацию)
        Response createResponse = createUser(uniqueName, email, Config.DEFAULT_PASSWORD);

        // Извлекаем токен
        String rawAccessToken = createResponse.jsonPath().getString("accessToken");
        // Отправляем запрос на авторизацию
        Response loggedResponse = userLogged(email, Config.DEFAULT_PASSWORD+"xxx");

        try {
            assertFalse("success должен быть false", loggedResponse.jsonPath().getBoolean("success"));
            // Проверка тела ответа
            assertFalse("Тело ответа пустое", loggedResponse.getBody().asString().isEmpty());
            // Проверяем текст ответа
            assertEquals("email or password are incorrect", loggedResponse.jsonPath().getString("message"));
            // Ожидаем статус кода 401 (Not Found)
            assertEquals("Статус код должен быть 401",
                    Config.STATUS_CODE_UNAUTHORIZED,
                    loggedResponse.getStatusCode());
        } finally {
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }
    }

}