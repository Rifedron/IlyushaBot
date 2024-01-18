package ru.miau.ilyushabot.data_storing;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDAO<V extends SavableObject> implements IDAO<V> {

    private List<V> values;
    private String fileName;

    public AbstractDAO(String fileName) {
        this.fileName = fileName;
        load();
    }


    @Override
    public void add(V value) {
        values.add(value);
        update();
    }
    @Override
    public V get(String key) {
        return values.stream().filter(v -> v.getKey().equals(key))
                .findAny().orElse(null);
    }
    @Override
    public void remove(V value) {
        values.remove(value);
        update();
    }

    @Override
    public V remove(String key) {
        for (V value : values) {
            if (value.getKey().equals(key)) {
                V toRemove = value;
                remove(toRemove);
                return toRemove;
            }
        }
        return null;
    }

    @Override
    public boolean has(String key) {
        return get(key) != null;
    }

    @Override
    public void update() {
        new Thread(() -> {
            List<Map<String, Object>> mapList = new ArrayList<>();
            if (!values.isEmpty()) {
                values.forEach(v -> mapList.add(v.toSavableMap()));
            }
            try {
                File file = new File(fileName);
                if (!file.exists()) file.createNewFile();
                FileWriter writer = new FileWriter(file);
                Yaml yaml = new Yaml();
                writer.write(yaml.dump(mapList));
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public void load() {
        this.values = new ArrayList<>();
        File file = new File(fileName);
        try {

        if (!file.exists()) {
            file.createNewFile();
        }
        FileReader reader = new FileReader(file);
        Yaml yaml = new Yaml();
        List<Map<String, Object>> ymlMaps = yaml.load(reader);
        ymlMaps.forEach(stringObjectMap ->
                values.add(fromMap(stringObjectMap))
        );

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
    public abstract V fromMap(Map<String, Object> map);

    public List<V> getValues() {
        return this.values;
    }
}
