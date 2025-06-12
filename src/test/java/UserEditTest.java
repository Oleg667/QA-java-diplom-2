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
@Feature("Редактирование данных пользователя")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserEditTest {

    @Test
    @DisplayName("#1 - Редактирование данных пользователя с авторизацией")
    @Description("Изменение Имени Почты Пароля")


    public void test_Edit_User_Data() {
        String uniqueName = generateUniqueLogin();// Генерируем уникальное имя пользователя
        String email = uniqueName + "@mail.ru";                                             // Формируем уникальный email на основе имени
        createUser(uniqueName, email, Config.DEFAULT_PASSWORD);                             // Отправляем запрос на создание пользователя (регистрацию)
        Response loggedResponse = userLogged(email, Config.DEFAULT_PASSWORD);               // Авторизуемся теми же email и паролем
        String rawAccessToken = loggedResponse.jsonPath().getString("accessToken");     // Извлекаем токен
        // Подготовка изменённых данных
        String editedName = uniqueName + "_edit";
        String editedEmail = uniqueName + "_edit" + "@mail.ru";
        Response editUserResponse = editUser(editedName, editedEmail, rawAccessToken); //отправляем запрос на редактирование данных пользователя
        String responseBody = editUserResponse.getBody().asString();
        int statusCode = editUserResponse.getStatusCode(); // Получаем статус код и тело ответа

        try {
            // Проверяем, что тело ответа не пустое
            assertFalse("Тело ответа пустое", responseBody.isEmpty());

            // Проверка успешного редактирования
            assertEquals("Неверный статус код при редактировании пользователя",
                    Config.STATUS_CODE_OK,
                    editUserResponse.getStatusCode());
            assertTrue("success должен быть true", editUserResponse.jsonPath().getBoolean("success"));
            assertEquals("Email после редактирования не совпадает", editedEmail,
                    editUserResponse.jsonPath().getString("user.email"));
            assertEquals("Имя после редактирования не совпадает", editedName,
                    editUserResponse.jsonPath().getString("user.name"));

        } finally {
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }
    }

    @Test
    @DisplayName("#2 - Попытка редактирования без авторизации")
    @Description("Проверка, что редактирование пользователя без accessToken запрещено (401 Unauthorized)")
    public void test_EditUser_WithoutAuthorization() {
        String uniqueName = generateUniqueLogin();
        String email = uniqueName + "@mail.ru";                                             // Формируем уникальный email на основе имени
        createUser(uniqueName, email, Config.DEFAULT_PASSWORD);                             // Отправляем запрос на создание пользователя (регистрацию)
        Response loggedResponse = userLogged(email, Config.DEFAULT_PASSWORD);               // Авторизуемся теми же email и паролем
        String rawAccessToken = loggedResponse.jsonPath().getString("accessToken");     // Извлекаем токен
        // Подготовка изменённых данных
        String editedName = uniqueName + "_edit";
        String editedEmail = uniqueName + "_edit" + "@mail.ru";
        Response editUserResponse = editUserWithoutToken(editedName, editedEmail); //отправляем запрос на редактирование без токена
//        String responseBody = editUserResponse.getBody().asString();
//        int statusCode = editUserResponse.getStatusCode(); // Получаем статус код и тело ответа

        try {
            // Проверяем статус 401
            assertEquals("Ожидался статус 401 Unauthorized", Config.STATUS_CODE_UNAUTHORIZED, editUserResponse.getStatusCode());

            // Проверяем, что success = false
            assertFalse("success должен быть false", editUserResponse.jsonPath().getBoolean("success"));

            // Проверяем сообщение
            assertEquals("Сообщение об ошибке не совпадает",
                    "You should be authorised",
                    editUserResponse.jsonPath().getString("message"));

        } finally {
            // Удаляем созданного пользователя
            cleanUp(rawAccessToken);
        }
    }
}

