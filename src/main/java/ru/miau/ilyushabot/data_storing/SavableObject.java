package ru.miau.ilyushabot.data_storing;

import java.util.Map;

public interface SavableObject {
    String getKey();
    Map<String, Object> toSavableMap();
}
