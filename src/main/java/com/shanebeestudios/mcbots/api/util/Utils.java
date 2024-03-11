package com.shanebeestudios.mcbots.api.util;

import com.diogonunes.jcolor.Attribute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shanebeestudios.mcbots.api.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;

@SuppressWarnings("deprecation")
public class Utils {

    private static JsonObject lang;

    public static String getFullText(TextComponent message, boolean colored) {
        if (!message.children().isEmpty()) {
            StringBuilder text = new StringBuilder();
            text.append(parseColor(message, colored));
            for (Component child : message.children()) {
                if (child instanceof TextComponent textComponent) {
                    text.append(getFullText(textComponent, colored));
                } else if (child instanceof TranslatableComponent translatableComponent) {
                    text.append(translate(translatableComponent));
                }
            }
            return text.toString();
        } else {
            return parseColor(message, colored);
        }
    }

    /**
     * A static function that gets full text from text components.
     * From 1.19.1 the protocol does not include sender's information in the message.
     * Thus, we need to merge two TextComponent (sender and message) and generate a text that consists of sent message.
     * This means using old getFullText will just send the message. Which does NOT contain username who sent this message
     *
     * @param sender  The TextComponent for sender.
     * @param message The TextComponent for message.
     * @param colored Whether if this output was colored or not.
     * @return Returns String in "Username : Message" format.
     */
    public static String getFullText(@Nullable TextComponent sender, TextComponent message, boolean colored) {
        if (colored) {
            Style messageStyle = message.style();
            StringBuilder outString = new StringBuilder();

            if (sender != null) {
                Style senderStyle = sender.style();
                TextColor color = senderStyle.color();
                if (color != null) { // Generate sender text with color.
                    outString.append(colorizeText(sender.content(), color));
                } else {
                    outString.append(sender.content());
                }
                outString.append(" : "); // Add delimiter as : between sender and message.
            }

            TextColor color = messageStyle.color();
            if (color != null && sender != null) { // Generate message text with color.
                outString.append(colorizeText(sender.content(), color));
            } else {
                outString.append(message.content());
            }

            return outString.toString();
        } else {
            if (sender == null)
                return message.content();
            else
                return sender.content() + " : " + message.content();
        }
    }

    private static String parseColor(TextComponent message, boolean colored) {
        String out = message.content();
        if (colored) {
            TextColor color = message.style().color();
            if (color != null) {
                List<Attribute> formats = new ArrayList<>(getFormats(message.style().decorations()));
                formats.add(Attribute.TEXT_COLOR(color.red(), color.green(), color.blue()));
                out = colorize(out, formats.toArray(new Attribute[0]));
            }
        }
        return out;
    }

    private static String colorizeText(String text, TextColor color) {
        return colorize(text, Attribute.TEXT_COLOR(color.red(), color.green(), color.blue()));
    }

    public static List<Attribute> getFormats(Map<TextDecoration, TextDecoration.State> decorations) {
        List<Attribute> list = new ArrayList<>();
        decorations.forEach((format, state) -> {
            if (state.equals(TextDecoration.State.TRUE)) {
                switch (format) {
                    case ITALIC:
                        list.add(Attribute.ITALIC());
                        break;
                    case UNDERLINED:
                        list.add(Attribute.UNDERLINE());
                        break;
                    case STRIKETHROUGH:
                        list.add(Attribute.STRIKETHROUGH());
                        break;
                }
            }
        });
        return list;
    }

    public static String translate(TranslatableComponent message) {
        if (lang == null) {
            try {
                InputStream resource = Utils.class.getResourceAsStream("/files/lang.json");
                if (resource == null) return null;
                lang = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
            } catch (Exception e) {
                Logger.critical(e);
            }
        }

        String key = message.key();
        String base = key;

        JsonElement value = lang.get(key);
        if (value != null) {
            base = value.getAsString();
        }

        ArrayList<String> placeholders = new ArrayList<>();

        if (!message.arguments().isEmpty()) {
            for (TranslationArgument arg : message.arguments()) {
                if (arg.asComponent() instanceof TranslatableComponent translatableComponent) {
                    placeholders.add(translate(translatableComponent));
                } else if (arg.asComponent() instanceof TextComponent textComponent) {
                    placeholders.add(getFullText(textComponent, false));
                }
            }
            return String.format(base, placeholders.toArray());
        } else {
            return base;
        }
    }
}
