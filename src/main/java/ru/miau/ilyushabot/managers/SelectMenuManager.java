package ru.miau.ilyushabot.managers;

import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import ru.miau.ilyushabot.annotations.SelectMenu;
import ru.miau.ilyushabot.functions.offers.interactionables.OffersSelectMenus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SelectMenuManager extends ListenerAdapter {
    private Class[] selectMenusClasses = new Class[] {
            OffersSelectMenus.class
    };

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        for (Class aClass : selectMenusClasses) {
            try {
                var classInstance = aClass.getConstructor().newInstance();
                for (Method method : aClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(SelectMenu.class) && event.getComponentId().equals(method.getName())) {
                        method.setAccessible(true);
                        method.invoke(classInstance, event);
                    }
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
