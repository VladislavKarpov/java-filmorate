package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MpaRating {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17");

    private final String label;

    MpaRating(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static MpaRating fromString(String value) {
        for (MpaRating mpaRating : values()) {
            if (mpaRating.label.equalsIgnoreCase(value) || mpaRating.name().equalsIgnoreCase(value)) {
                return mpaRating;
            }
        }
        throw new IllegalArgumentException("Unknown MpaRating: " + value);
    }
}