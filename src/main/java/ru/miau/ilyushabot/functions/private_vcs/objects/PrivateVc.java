package ru.miau.ilyushabot.functions.private_vcs.objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import ru.miau.ilyushabot.functions.private_vcs.PrivateVcs;

import java.util.List;

import static ru.miau.ilyushabot.IlyushaBot.guild;
import static ru.miau.ilyushabot.IlyushaBot.jda;

public class PrivateVc {
    private final String channelId;
    private final String firstOwnerId;
    private String currentOwnerId;

    public boolean hasActiveOwner() {
        if (currentOwnerId != null) return true;
        else return false;
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
            currentOwnerId = null;
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
}
