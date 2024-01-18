package ru.miau.ilyushabot.functions.private_vcs.objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.data_storing.SavableObject;
import ru.miau.ilyushabot.functions.private_vcs.PrivateVcs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.miau.ilyushabot.IlyushaBot.guild;
import static ru.miau.ilyushabot.IlyushaBot.jda;

public class PrivateVc implements SavableObject {
    private final String channelId;
    private final String firstOwnerId;
    private String currentOwnerId;

    public boolean hasActiveOwner() {
        return currentOwnerId != null;
    }
    public String getChannelId() {
        return channelId;
    }

    public VoiceChannel getChannel() {
        return jda.getVoiceChannelById(channelId);
    }
    public String getFirstOwnerId() {
        return firstOwnerId;
    }

    public String getCurrentOwnerId() {
        return currentOwnerId;
    }

    public Member getCurrentOwner() {
        return guild.getMemberById(currentOwnerId);
    }
    public void setOwnerPermissions(Long newOwnerId) {
        VoiceChannel voiceChannel = getChannel();
        if (voiceChannel != null) {
            removeOwnerPermissions();
            currentOwnerId = newOwnerId.toString();
            voiceChannel.upsertPermissionOverride(getCurrentOwner())
                    .setPermissions(List.of(Permission.MANAGE_CHANNEL, Permission.VOICE_MOVE_OTHERS), null)
                    .complete();
            if (PrivateVcs.privateVcDAO != null) PrivateVcs.privateVcDAO.update();
        }
    }
    public void removeOwnerPermissions() {
        if (hasActiveOwner()) {
            VoiceChannel voiceChannel = getChannel();
            voiceChannel.getMemberPermissionOverrides().forEach(permissionOverride -> permissionOverride.delete()
                    .complete());
            if ( PrivateVcs.privateVcDAO != null) PrivateVcs.privateVcDAO.update();
        }
    }

    public PrivateVc(String channelId, String ownerId) {
        this(channelId, ownerId, ownerId);
    }
    public PrivateVc(String channelId, String firstOwnerId, String currentOwnerId) {
        this.channelId = channelId;
        this.firstOwnerId = firstOwnerId;
        this.currentOwnerId = currentOwnerId;
        if (currentOwnerId != null) {
            setOwnerPermissions(Long.valueOf(currentOwnerId));
        }
    }

    @Override
    public String getKey() {
        return channelId;
    }

    @Override
    public Map<String, Object> toSavableMap() {
        return new HashMap<>() {{
            put(YamlKeys.PRIVATE_VC_CHANNEL_ID, channelId);
            put(YamlKeys.PRIVATE_VC_FIRST_CREATOR_ID, firstOwnerId);
            put(YamlKeys.PRIVATE_VC_CURRENT_OWNER_ID, currentOwnerId);
        }};
    }
    public static PrivateVc fromMap(Map<String, Object> map) {
        return new PrivateVc(
                (String) map.get(YamlKeys.PRIVATE_VC_CHANNEL_ID),
                (String) map.get(YamlKeys.PRIVATE_VC_FIRST_CREATOR_ID),
                (String) map.get(YamlKeys.PRIVATE_VC_CURRENT_OWNER_ID)
        );
    }

}
