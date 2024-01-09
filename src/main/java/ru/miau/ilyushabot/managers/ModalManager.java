package ru.miau.ilyushabot.managers;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalInteraction;
import ru.miau.ilyushabot.annotations.Modal;
import ru.miau.ilyushabot.functions.offers.interactionables.OffersModals;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ModalManager extends ListenerAdapter {
    private Class[] modalClasses = new Class[] {
            OffersModals.class
    };

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        ModalInteraction interaction = event.getInteraction();
        for (Class modalClass : modalClasses) {
            try {
                var modalClassInstance = modalClass.getConstructor().newInstance();
            for (Method method : OffersModals.class.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Modal.class) && interaction.getModalId().startsWith(method.getName())) {
                    method.setAccessible(true);
                    method.invoke(modalClassInstance, event);
                }
            }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
