package ru.netology.data;

import com.github.javafaker.Faker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class DataGenerator {

    private String validCardNumber = "4444 4444 4444 4441";
    private String declinedCardNumber = "4444 4444 4444 4442";
    private String inValidCardNumber = "4272 3000 0925 5304";
    private String shortCardNumber = "4444 4444 4444 444";

    Faker faker = new Faker();

    private String[] months = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    private Random rand = new Random();

    public String getRandomMonth() {
        return months[rand.nextInt(months.length)];
    }

    public String getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return (new SimpleDateFormat("MM").format(cal.getTime()));
    }

    private String[] years = new String[]{"22", "23", "24", "25", "26"};
    private Random random = new Random();

    public String getRandomYear() {
        return years[random.nextInt(years.length)];
    }

    public String generateCardOwnerName() {
        return faker.name().firstName() + " " + faker.name().lastName();
    }

    private String[] cvv = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"};
    private Random randomCVV = new Random();

    public String getCVV() {
        return cvv[randomCVV.nextInt(cvv.length)] + cvv[randomCVV.nextInt(cvv.length)] + cvv[randomCVV.nextInt(cvv.length)];
    }

    public String getValidCardNumber() {
        return validCardNumber;
    }

    public String getDeclinedCardNumber() {
        return declinedCardNumber;
    }

    public String getInValidCardNumber() {
        return inValidCardNumber;
    }

    public String getShortCardNumber() {
        return shortCardNumber;
    }
}