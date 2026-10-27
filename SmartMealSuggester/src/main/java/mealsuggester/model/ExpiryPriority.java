package mealsuggester.model;

/**
 * Represents how urgently an ingredient needs to be consumed.
 * Higher value = more urgent to use.
 */
public enum ExpiryPriority {
    HIGH(3),
    MEDIUM(2),
    LOW(1);

    private final int value;

    ExpiryPriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

