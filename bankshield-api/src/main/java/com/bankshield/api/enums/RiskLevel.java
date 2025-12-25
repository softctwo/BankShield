package com.bankshield.api.enums;

/**
 * 风险级别枚举
 * @author BankShield
 */
public enum RiskLevel {
    CRITICAL("严重", "#FF0000", 5),
    HIGH("高危", "#FF6600", 4),
    MEDIUM("中危", "#FF9900", 3),
    LOW("低危", "#FFCC00", 2),
    INFO("信息", "#0099FF", 1);

    private final String description;
    private final String color;
    private final int score;

    RiskLevel(String description, String color, int score) {
        this.description = description;
        this.color = color;
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public String getColor() {
        return color;
    }

    public int getScore() {
        return score;
    }
}