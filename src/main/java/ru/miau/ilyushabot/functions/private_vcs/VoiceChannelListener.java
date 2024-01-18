package ru.miau.ilyushabot.functions.private_vcs;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.miau.ilyushabot.IlyushaBot;
import ru.miau.ilyushabot.YamlKeys;
import ru.miau.ilyushabot.functions.private_vcs.objects.PrivateVc;

import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.miau.ilyushabot.functions.private_vcs.PrivateVcs.privateVcDAO;

public class VoiceChannelListener extends ListenerAdapter {
    private final Long voiceFabricId = (Long) IlyushaBot.config.get(YamlKeys.PRIVATE_VC_FABRIC_ID);

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion joinChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();

        if (joinChannel != null) new Thread(() -> onJoinVC(event)).start();
        if (leftChannel != null) new Thread(() -> onQuitVC(event)).start();
    }

    private void onJoinVC(GuildVoiceUpdateEvent e) {
        AudioChannelUnion channel = e.getChannelJoined();
        Long channelId = channel.getIdLong();
        Long memberId = e.getMember().getIdLong();

        if (channelId.equals(voiceFabricId)) {
            createPrivateVc(e);
            return;
        }
        PrivateVc privateVc = privateVcDAO.get(channel.getId());
        if (privateVc != null) {
            if (privateVc.getCurrentOwnerId().equals(memberId.toString()))
                privateVc.setOwnerPermissions(memberId);
        }
    }
    private void onQuitVC(GuildVoiceUpdateEvent e) {
        AudioChannelUnion channel = e.getChannelLeft();
        PrivateVc privateVC = privateVcDAO.get(channel.getId());
        if (privateVC != null) {
            if (channel.getMembers().isEmpty()) {
                try {
                    channel.delete().complete();
                } catch (Throwable ignore) {}
                privateVcDAO.remove(privateVC);
            } else if (e.getMember().getId().equals(privateVC.getCurrentOwnerId())) {
                privateVC.removeOwnerPermissions();
            }
        }
    }
    private void createPrivateVc(GuildVoiceUpdateEvent e) {
        Category category = e.getChannelJoined().getParentCategory();
        Member member = e.getMember();
        VoiceChannel voiceChannel = category.createVoiceChannel(member.getEffectiveName())
                .complete();
        voiceChannel.sendMessageEmbeds(privateChannelEmbed(member)).queue();
        AtomicBoolean success = new AtomicBoolean(false);
        try {
            voiceChannel.getGuild().moveVoiceMember(member, voiceChannel)
                    .onSuccess(unused -> {
                        PrivateVc privateVC = new PrivateVc(voiceChannel.getId(), member.getId());
                        privateVcDAO.add(privateVC);
                        success.set(true);
                    }).complete();
        } catch (Throwable ignored) {}
        if (!success.get()) {
            voiceChannel.delete().queue();
        } else try {
            Thread.sleep(750);
            if (voiceChannel.getMembers().isEmpty()) {
                privateVcDAO.remove(
                        privateVcDAO.get(e.getChannelJoined().getId())
                );
                voiceChannel.delete()
                        .complete();
            }
        } catch (Throwable ignored) {}
    }
    @Override
    public void onReady(ReadyEvent event) {
        new Thread(() -> {
            try {
                JDA jda = event.getJDA().awaitReady();
                List<PrivateVc> privateVcs = privateVcDAO.getValues().stream().toList();
                privateVcs.stream()
                        .filter(privateVc -> jda.getVoiceChannelById(privateVc.getChannelId()) == null)
                        .forEach(privateVc -> privateVcDAO.remove(privateVc));
                privateVcs.stream().forEach(privateVc -> {
                    VoiceChannel voiceChannel = jda.getVoiceChannelById(privateVc.getChannelId());
                    if (voiceChannel == null) privateVcDAO.remove(privateVc);
                    else if (voiceChannel.getMembers().isEmpty()) {
                        voiceChannel.delete().queue();
                        privateVcDAO.remove(privateVc);
                    }
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private MessageEmbed privateChannelEmbed(Member member) {
        return new EmbedBuilder()
                .setAuthor("Создатель канала: "+member.getEffectiveName(), null, member.getEffectiveAvatarUrl())
                .setTitle("Добро пожаловать в ваш приватный канал!")
                .setColor(Color.decode("#d8958f"))
                .setDescription("Если создатель выйдет, то вы всегда можете воспользоваться командой **/claim**," +
                        "чтобы присвоить этот канал себе, но по возвращению создателя он сможет вернуть свои права обратно")
                .setFooter("Приятного общения!", "https://cdn.discordapp.com/attachments/1190817984747405375/1193607671589372034/icons.png?ex=65ad54c5&is=659adfc5&hm=35488a51e3cb2a05ca4e9b0409ebde83c201e4fa8cfc7fce2f88915b76a25bd3&")
                .build();
    }
}
