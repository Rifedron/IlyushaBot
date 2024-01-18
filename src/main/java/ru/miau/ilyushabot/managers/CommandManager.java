package ru.miau.ilyushabot.managers;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import ru.miau.ilyushabot.annotations.Command;
import ru.miau.ilyushabot.annotations.Context;
import ru.miau.ilyushabot.annotations.HasOptions;
import ru.miau.ilyushabot.annotations.Option;
import ru.miau.ilyushabot.functions.offers.interactionables.OffersContextCommands;
import ru.miau.ilyushabot.functions.private_vcs.interactionables.PrivateVcSlashCommands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {
    private Class[] slashCommandClasses = new Class[] {
            PrivateVcSlashCommands.class
    };
    private Class[] contextClasses = new Class[] {
            OffersContextCommands.class
    };

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        for (Class commandClass : slashCommandClasses) {
            try {
                var commandClassInstance = commandClass.getConstructor().newInstance();
                for (Method method : commandClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Command.class)) {
                        if (commandName.equals(method.getAnnotation(Command.class).name())) {
                            method.setAccessible(true);
                            method.invoke(commandClassInstance, event);
                            return;
                        }
                    }
                }
                event.getInteraction().reply("Команда не найдена")
                        .queue();
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        for (Class contextClass : contextClasses) {
            try {
                var contextClassInstance = contextClass.getDeclaredConstructor().newInstance();
                for (Method method : OffersContextCommands.class.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Context.class)) {
                        Context context = method.getAnnotation(Context.class);
                        if (context.type() == net.dv8tion.jda.api.interactions.commands.Command.Type.MESSAGE && event.getName().equals(context.name())) {
                            try {
                                method.setAccessible(true);
                                method.invoke(contextClassInstance, event);
                                return;
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        event.reply("Команда не найдена")
                .queue();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        event.getGuild().updateCommands()
                .addCommands(slashCommands())
                .addCommands(contextCommands())
                .queue(System.out::println);
    }

    private List<CommandData> slashCommands() {
        List<CommandData> commandData = new ArrayList<>();
        for (Class commandClass : slashCommandClasses) {
            for (Method method : commandClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Command.class)) {
                    Command command = method.getAnnotation(Command.class);
                    SlashCommandData data = Commands.slash(command.name(), command.description());
                    if (method.isAnnotationPresent(HasOptions.class)) {
                        Option[] options = method.getAnnotation(HasOptions.class).value();
                        for (Option option : options) {
                            data.addOption(option.optionType(),
                                    option.name(), option.description(), option.isRequired());
                        }
                    }
                    commandData.add(data);
                }
            }
        }
        return commandData;
    }
    private List<CommandData> contextCommands() {
        List<CommandData> commandData = new ArrayList<>();
        for (Class contextClass : contextClasses) {
            for (Method method : contextClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Context.class)) {
                    Context context = method.getAnnotation(Context.class);
                    commandData.add(Commands.context(context.type(), context.name()));
                }
            }
        }
        return commandData;
    }
}