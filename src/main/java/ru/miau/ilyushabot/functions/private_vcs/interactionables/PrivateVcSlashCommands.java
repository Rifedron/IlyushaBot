package ru.miau.ilyushabot.functions.private_vcs.interactionables;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import ru.miau.ilyushabot.annotations.Command;
import ru.miau.ilyushabot.annotations.HasOptions;
import ru.miau.ilyushabot.annotations.Option;
import ru.miau.ilyushabot.functions.private_vcs.objects.PrivateVc;

import static ru.miau.ilyushabot.functions.private_vcs.PrivateVcs.privateVcDAO;

public class PrivateVcSlashCommands {
    @Command(
            name = "claim",
            description = "Делает вас владельцем канала, если у него нет активного владельца"
    )
    void claim(SlashCommandInteraction interaction) {
        Member member = interaction.getMember();
        member.getVoiceState().getChannel();
        AudioChannelUnion channel = member.getVoiceState().getChannel();
        if (isClaimInteractionSuccess(interaction, channel)) {
            PrivateVc privateVc = privateVcDAO.get(channel.getId());
            privateVc.setOwnerPermissions(member.getIdLong());
            interaction.reply("Вы теперь владелец этого канала!")
                    .setEphemeral(true)
                    .queue();
            privateVc.getChannel().sendMessageFormat("**%s теперь владелец этого канала**", member.getAsMention())
                    .queue();
        }
    }

    private boolean isClaimInteractionSuccess(SlashCommandInteraction interaction, AudioChannelUnion channel) {
        if (!isPrivateVcInteractionValid(interaction, channel)) return false;

        PrivateVc privateVc = privateVcDAO.get(channel.getId());
        Member member = interaction.getMember();
        Member currentOwner = privateVc.getCurrentOwner();
        if (channel.getMembers().contains(currentOwner)) {
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

    @Command(
            name = "set-owner",
            description = "Передаёт права на канал указанному пользователю"
    )
    @HasOptions({
            @Option(
                    optionType = OptionType.USER,
                    name = "user",
                    description = "Пользователь, который получит права на канал"
            )}
    )
    void setOwner(SlashCommandInteraction interaction) {
        Member member = interaction.getMember();
        Member newOwner = interaction.getOption("user").getAsMember();
        AudioChannelUnion channel = member.getVoiceState().getChannel();
        if (isSetOwnerInteractionValid(interaction, channel)) {
            privateVcDAO.get(channel.getId())
                    .setOwnerPermissions(newOwner.getIdLong());
            interaction.reply("Вы передали права на канал "+newOwner.getAsMention())
                    .setEphemeral(true)
                    .queue();
            channel.asVoiceChannel().sendMessageFormat("**%s получил права на канал от %s**",
                            newOwner.getAsMention(), member.getAsMention())
                    .queue();
        }
    }
    private boolean isSetOwnerInteractionValid(SlashCommandInteraction interaction, AudioChannelUnion channel) {
        if (!isPrivateVcInteractionValid(interaction, channel)) return false;

        Member member = interaction.getMember();
        PrivateVc privateVc = privateVcDAO.get(channel.getId());
        if (!member.getId().equals(privateVc.getCurrentOwnerId())) {
            interaction.reply("Вы не являетесь владельцем этого канала")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }

    private boolean isPrivateVcInteractionValid(SlashCommandInteraction interaction, AudioChannelUnion channel) {
        if (channel == null) {
            interaction.reply("Вы сейчас не находитесь в каком-либо канале")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        PrivateVc privateVc = privateVcDAO.get(channel.getId());
        if (privateVc == null) {
            interaction.reply("Вы сейчас не находитесь в приватном канале")
                    .setEphemeral(true)
                    .queue();
            return false;
        }
        return true;
    }

}
