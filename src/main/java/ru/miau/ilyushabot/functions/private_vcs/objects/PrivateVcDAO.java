package ru.miau.ilyushabot.functions.private_vcs.objects;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateVcDAO {
    private List<PrivateVc> privates;

    public void add(PrivateVc privateVC) {
        privates.add(privateVC);
        update();
    }
    public PrivateVc get(String channelId) {
        return privates.stream().filter(privateVC -> privateVC.getChannelId().equals(channelId))
                .findAny().orElse(null);
    }
    public void remove(PrivateVc privateVC) {
        privates.remove(privateVC);
        update();
    }

    public List<PrivateVc> getPrivates() {
        return this.privates;
    }
    public void update() {
        try {
            Yaml yaml = new Yaml();

            List<Map> ymlPrivates = privates.stream()
                    .map(privateVC -> {
                        Map map = new HashMap();
                        map.put("vcId", privateVC.getChannelId());
                        map.put("firstOwnerId", privateVC.getFirstOwnerId());
                        map.put("currentOwnerId", privateVC.getCurrentOwnerId());
                        return map;
                    }).toList();
            FileWriter writer = new FileWriter("privateVcs.yml");
            writer.write(yaml.dump(ymlPrivates));
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PrivateVcDAO() {
        try {
            Yaml yaml = new Yaml();
            File file = new File("privateVcs.yml");
            if (!file.exists()) file.createNewFile();
            FileReader reader = new FileReader(file);
            List<Map<String, String>> ymlPrivates = yaml.load(reader);
            reader.close();
            this.privates = new ArrayList<>();
            if (ymlPrivates != null) {
                List<PrivateVc> privatesList = ymlPrivates.stream()
                        .map(stringStringMap -> new PrivateVc(
                                stringStringMap.get("vcId"),
                                stringStringMap.get("firstOwnerId"),
                                stringStringMap.get("currentOwnerId")
                        )).toList();
                this.privates.addAll(privatesList);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
