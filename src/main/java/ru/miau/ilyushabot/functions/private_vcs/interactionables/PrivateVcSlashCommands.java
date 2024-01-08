package ru.miau.ilyushabot.functions.private_vcs.interactionables;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ru.miau.ilyushabot.annotations.Command;
import ru.miau.ilyushabot.functions.private_vcs.PrivateVcs;
import ru.miau.ilyushabot.functions.private_vcs.objects.PrivateVc;

public class PrivateVcSlashCommands {
    @Command(description = "Makes you an owner of private vc if there is no owner here")
    void claim(SlashCommandInteraction interaction) {
        Member member = interaction.getMember();
        member.getVoiceState().getChannel();
        AudioChannelUnion channel = member.getVoiceState().getChannel();
        if (isClaimInteractionSuccess(interaction, channel)) {
            PrivateVc privateVc = PrivateVcs.privateVcDAO.get(channel.getId());
            privateVc.setOwnerPermissions(member.getIdLong());
            interaction.reply("Вы теперь владелец этого канала!")
                    .setEphemeral(true)
                    .queue();
            privateVc.getChannel().sendMessageFormat("**%s теперь владелец этого канала**", member.getAsMention())
                    .queue();
        }
    }

    private boolean isClaimInteractionSuccess(SlashCommandInteraction interaction, AudioChannelUnion channel) {
        if (channel == null) {
            interaction.reply("Вы сейчас не находитесь в каком-либо канале")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        PrivateVc privateVc = PrivateVcs.privateVcDAO.get(channel.getId());
        if (privateVc == null) {
            interaction.reply("Вы сейчас не находитесь в приватном канале")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        Member member = interaction.getMember();
        if (privateVc.hasActiveOwner()) {
            Member currentOwner = privateVc.getCurrentOwner();
            if (!member.getId().equals(privateVc.getCurrentOwnerId()) && !member.getId().equals(privateVc.getFirstOwnerId())) {
                interaction.replyFormat("У канала уже есть активный владелец %s", currentOwner.getAsMention())
                        .setEphemeral(true)
                        .queue();
                return false;
            }
        }
        if (member.getId().equals(privateVc.getCurrentOwnerId())) {
            interaction.reply("Вы уже являетесь владельцем этого канала")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }
}
