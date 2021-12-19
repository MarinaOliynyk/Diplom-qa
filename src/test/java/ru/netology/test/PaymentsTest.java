package ru.netology.test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;

import ru.netology.data.DataGenerator;
import ru.netology.page.PaymentsPage;
import ru.netology.sql.SqlHelperPayment;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.*;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentsTest {
    PaymentsPage paymentsPage = new PaymentsPage();
    DataGenerator dataGenerator = new DataGenerator();

    @BeforeAll
    static void SetUpAll() {
        SelenideLogger.addListener("allure", new AllureSelenide());
    }

    @AfterAll
    static void tearDownAll() {
        SelenideLogger.removeListener("allure");
    }

    @BeforeEach
    void setup() {
        open("http://localhost:8080");
        paymentsPage.paymentButton.click();
    }

    @AfterEach
    public void cleanBase() {
        SqlHelperPayment.cleanDefaultData();
    }

    @Test
    @DisplayName("Успешное отображение формы по нажатию на кнопку \"Купить\"")
    public void shouldPaymentForm() {
        paymentsPage.form.should(appear);
        paymentsPage.paymentHeader.should(appear);
    }

    @Test
    @DisplayName("Успешная покупка по клику на кнопку \"Купить\"")
    public void successfulPay() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.succeedNotification.should(appear, Duration.ofSeconds(15));
        assertEquals("APPROVED", SqlHelperPayment.getCardStatusApproved());
    }

    @Test
    @DisplayName("Отклоненная банком оплата по карте")
    public void invalidPay() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getDeclinedCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.failedNotification.should(appear, Duration.ofSeconds(15));
        assertEquals("DECLINED", SqlHelperPayment.getCardStatusDeclined());
    }

    @Test
    @DisplayName("Отклоненная банком оплата по несуществующей карте стороннего банка")
    public void invalidCardNumber() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getInValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Номер карты")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Отправка формы с незаполненными полями")
    public void emptyFields() {
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Номер карты")).shouldHave(text("Неверный формат")).shouldBe(visible);
        paymentsPage.form.shouldHave(text("Месяц")).shouldHave(text("Неверный формат")).shouldBe(visible);
        paymentsPage.form.shouldHave(text("Год")).shouldHave(text("Неверный формат")).shouldBe(visible);
        paymentsPage.form.shouldHave(text("Владелец")).shouldHave(text("Поле обязательно для заполнения")).shouldBe(visible);
        paymentsPage.form.shouldHave(text("CVC/CVV")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка поля \"Номер карты\" (15 цифр)")
    public void shortCardNumber() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getShortCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.failedNotification.should(appear, Duration.ofSeconds(15));
    }

    @Test
    @DisplayName("Проверка поля \"Месяц\" (негативный сценарий)")
    public void invalidMonth() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue("21");
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверно указан срок действия карты")).should(appear);
    }

    @Test
    @DisplayName("Проверка поля \"Год\" (негативный сценарий с прошедшим годом)")
    public void invalidYearExpired() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue("20");
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Истёк срок действия карты")).should(appear);
    }

    @Test
    @DisplayName("Проверка поля \"Год\" (негативный сценарий с годом превышающим 5 лет)")
    public void invalidYearAbove() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue("27");
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверно указан срок действия карты")).should(appear);
    }

    @Test
    @DisplayName("Проверка невалидности данных (при истекшем месяце текущего года)")
    public void invalidYearExpire() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue("06");
        paymentsPage.fieldYear.setValue("21");
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверно указан срок действия карты")).should(appear);
    }

    @Test
    @DisplayName("Проверка поля \"Владелец\" (имя кириллицей)")
    public void invalidNameCyrillic() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue("Марина Олийнык");
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверный формат имени")).should(appear);
    }

    @Test
    @DisplayName("Проверка поля \"CVC/CVV\" (негативный сценарий)")
    public void invalidCVV() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue("000");
        paymentsPage.proceedButton.click();
        $(withText("Неверный формат")).should(appear);
    }

    @Test
    @DisplayName("Проверка пустого поля \"Номер карты\"")
    public void numberFieldIsEmpty() {
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Номер карты")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка пустого поля \"Месяц\"")
    public void monthFieldIsEmpty() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Месяц")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка ввода 00 в поле \"Месяц\"")
    public void invalidMonthDoubleZero() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue("00");
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверный формат месяца")).should(appear);
    }

    @Test
    @DisplayName("Проверка пустого поля \"Год\"")
    public void yearFieldIsEmpty() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Год")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка пустого поля \"Владелец\"")
    public void nameFieldIsEmpty() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("Владелец")).shouldHave(text("Поле обязательно для заполнения")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка поля \"Владелец\" на ввод символов")
    public void invalidNameNumber() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue("123 456");
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        $(withText("Неверный формат имени")).should(appear);
    }

    @Test
    @DisplayName("Проверка пустого поля \"CVC/CVV\"")
    public void cvvFieldIsEmpty() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.proceedButton.click();
        paymentsPage.form.shouldHave(text("CVC/CVV")).shouldHave(text("Неверный формат")).shouldBe(visible);
    }

    @Test
    @DisplayName("Проверка нажатия по кнопке \"Закрыть\" на успешном уведомлении")
    public void closeSuccessfulNotification() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.succeedNotification.should(appear, Duration.ofSeconds(20));
        paymentsPage.closeSucceedNotification.click();
        paymentsPage.succeedNotification.should(disappear);
    }

    @Test
    @DisplayName("Проверка нажатия по кнопке \"Закрыть\" на неуспешном уведомлении")
    public void closeFailedNotification() {
        paymentsPage.fieldNumber.setValue(dataGenerator.getInValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.failedNotification.should(appear, Duration.ofSeconds(15));
        paymentsPage.closeFailedNotification.click();
        paymentsPage.failedNotification.should(disappear);
    }

    @Test
    @DisplayName("Проверка создания записи в таблице")
    void shouldCreateItem() {
        SqlHelperPayment.cleanDefaultData();
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.succeedNotification.should(appear, Duration.ofSeconds(15));
        assertEquals(SqlHelperPayment.getCardIdOrder(), SqlHelperPayment.getCardIdPayment());
    }

    @Test
    @DisplayName("Проверка статуса записи в таблице(Approved карта)")
    void shouldCheckStatusApproved() {
        SqlHelperPayment.cleanDefaultData();
        paymentsPage.fieldNumber.setValue(dataGenerator.getValidCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.succeedNotification.should(appear, Duration.ofSeconds(15));
        assertEquals("APPROVED", SqlHelperPayment.getCardStatusApproved());
    }

    @Test
    @DisplayName("Проверка статуса записи в таблице(Declined карта)")
    void shouldCheckStatusDeclined() {
        SqlHelperPayment.cleanDefaultData();
        paymentsPage.fieldNumber.setValue(dataGenerator.getDeclinedCardNumber());
        paymentsPage.fieldMonth.setValue(dataGenerator.getRandomMonth());
        paymentsPage.fieldYear.setValue(dataGenerator.getRandomYear());
        paymentsPage.fieldCardOwner.setValue(dataGenerator.generateCardOwnerName());
        paymentsPage.fieldCardCode.setValue(dataGenerator.getCVV());
        paymentsPage.proceedButton.click();
        paymentsPage.succeedNotification.should(appear, Duration.ofSeconds(15));
        assertEquals("DECLINED", SqlHelperPayment.getCardStatusDeclined());
    }
}