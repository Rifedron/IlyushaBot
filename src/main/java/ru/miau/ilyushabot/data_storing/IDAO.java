package ru.miau.ilyushabot.data_storing;

public interface IDAO<V extends SavableObject> {
    void add(V value);

    V get(String key);

    void remove(V value);
    V remove(String key);
    boolean has(String key);
    void update();

    void load();
}
