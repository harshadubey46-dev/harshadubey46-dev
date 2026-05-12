package com.yourname.expensetracker;

import java.time.LocalDate;
import java.util.*;

public class AIAnalyzer {

    public static String generateInsights(List<Expense> list) {

        if (list.isEmpty()) return "No data available.";

        double total = 0;
        Map<String, Double> categoryMap = new HashMap<>();

        for (Expense e : list) {
            total += e.getAmount();
            categoryMap.put(e.getCategory(),
                    categoryMap.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
        }

        // Highest spending category
        String maxCat = Collections.max(categoryMap.entrySet(),
                Map.Entry.comparingByValue()).getKey();

        double maxVal = categoryMap.get(maxCat);

        StringBuilder insight = new StringBuilder();

        insight.append("Total Spending: ₹ ").append(total).append("\n");
        insight.append("Top Category: ").append(maxCat).append(" (₹ ").append(maxVal).append(")\n");

        // Smart suggestion
        if (maxVal > total * 0.4) {
            insight.append("⚠ You are overspending on ").append(maxCat).append("\n");
        }

        if (total > 10000) {
            insight.append("💡 Try reducing expenses this month.\n");
        } else {
            insight.append("✅ Your spending is under control.\n");
        }

        return insight.toString();
    }

    public static double getMonthlyTotal(List<Expense> list) {
        int currentMonth = LocalDate.now().getMonthValue();

        return list.stream()
                .filter(e -> e.getDate().getMonthValue() == currentMonth)
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}
