package com.yourname.expensetracker;

import java.io.FileWriter;
import java.util.List;

public class Utils {

    public static void exportCSV(List<Expense> list) {
        try (FileWriter w = new FileWriter("expenses.csv")) {
            w.write("Category,Amount,Date\n");
            for (Expense e : list) {
                w.write(e.getCategory() + "," +
                        e.getAmount() + "," +
                        e.getDate() + "\n");
            }
        } catch (Exception e) { }
    }
}
