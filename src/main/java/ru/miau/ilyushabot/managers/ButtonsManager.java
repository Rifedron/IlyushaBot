package ru.miau.ilyushabot.managers;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.miau.ilyushabot.annotations.Button;
import ru.miau.ilyushabot.functions.offers.interactionables.OffersButtons;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ButtonsManager extends ListenerAdapter {
    private Class[] buttonsClasses = new Class[] {
            OffersButtons.class
    };
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonId = event.getButton().getId();
        for (Class buttonsClass : buttonsClasses) {
            try {
                var buttonsInstance = buttonsClass.getDeclaredConstructor().newInstance();

                for (Method method : buttonsClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Button.class)) {
                        if (method.getName().equals(buttonId)) {
                            try {
                                method.setAccessible(true);
                                method.invoke(buttonsInstance, event);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {throw new RuntimeException(e);}
        }
    }
}
