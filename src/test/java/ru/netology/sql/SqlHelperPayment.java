package ru.netology.sql;

import lombok.SneakyThrows;
import lombok.val;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlHelperPayment {

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/app", "app", "pass"
        );
    }

    @SneakyThrows
    public static void cleanDefaultData() {
        val deletePay = "DELETE FROM payment_entity ";
        val deleteOrder = "DELETE FROM order_entity";
        try (
                val conn = connect();
                val dataStmt = conn.createStatement();
        ) {
            dataStmt.executeUpdate(deletePay);
            dataStmt.executeUpdate(deleteOrder);
        }
    }

    @SneakyThrows
    public static String getCardIdPayment() {
        val cardIdPay = "SELECT transaction_id FROM payment_entity WHERE status = 'APPROVED'";
        Thread.sleep(500);
        try (
                val con = connect();
                val dataStmt = con.createStatement();
        ) {
            try (val rs = dataStmt.executeQuery(cardIdPay)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "Error";
    }

    @SneakyThrows
    public static String getCardIdOrder() {
        val cardIdOrder = "SELECT payment_id FROM order_entity";
        try (
                val con = connect();
                val dataStmt = con.createStatement();
                val cardsStmt = con.prepareStatement(cardIdOrder);
        ) {
            try (val rs = dataStmt.executeQuery(cardIdOrder)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "Error";
    }

    @SneakyThrows
    public static String getCardStatusApproved() {
        val cardStatusApproved = "SELECT status FROM payment_entity";
        try (
                val con = connect();
                val dataStmt = con.createStatement();
                val cardsStmt = con.prepareStatement(cardStatusApproved);
        ) {
            try (val rs = dataStmt.executeQuery(cardStatusApproved)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "Error";
    }

    @SneakyThrows
    public static String getCardStatusDeclined() {
        val cardStatusDeclined = "SELECT status FROM payment_entity";
        try (
                val con = connect();
                val dataStmt = con.createStatement();
                val cardsStmt = con.prepareStatement(cardStatusDeclined);
        ) {
            try (val rs = dataStmt.executeQuery(cardStatusDeclined)) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return "Error";
    }
}
