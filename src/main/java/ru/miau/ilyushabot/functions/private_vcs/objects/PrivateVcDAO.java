package ru.miau.ilyushabot.functions.private_vcs.objects;

import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.data_storing.AbstractDAO;

import java.util.Map;

public class PrivateVcDAO extends AbstractDAO<PrivateVc> {
    public PrivateVcDAO() {
        super("privateVcs.yml");
    }

    @Override
    public PrivateVc fromMap(Map map) {
        return new PrivateVc(
                (String) map.get(YamlKeys.PRIVATE_VC_CHANNEL_ID),
                (String) map.get(YamlKeys.PRIVATE_VC_FIRST_CREATOR_ID),
                (String) map.get(YamlKeys.PRIVATE_VC_CURRENT_OWNER_ID)
        );
    }
}
