package me.creepermaxcz.mcbots;

import com.diogonunes.jcolor.Attribute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Utils {

    private static JsonObject lang;

    public static String getFullText(TextComponent message, boolean colored) {
        if (message.children().size() > 0) {
            StringBuilder text = new StringBuilder();
            text.append(parseColor(message, colored));
            for (Component child : message.children()) {
                if (child instanceof TextComponent) {
                    text.append(getFullText((TextComponent) child, colored));
                } else if (child instanceof TranslatableComponent) {
                    text.append(translate((TranslatableComponent) child));
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
                if (senderStyle.color() != null) { // Generate sender text with color.
                    outString.append(colorizeText(sender.content(), senderStyle.color()));
                } else {
                    outString.append(sender.content());
                }
                outString.append(" : "); // Add delimiter as : between sender and message.
            }

            if (messageStyle.color() != null && sender != null) { // Generate message text with color.
                outString.append(colorizeText(sender.content(), messageStyle.color()));
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
            if (message.style().color() != null) {
                List<Attribute> formats = new ArrayList<>(getFormats(message.style().decorations()));
                TextColor color = message.style().color();
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
        try {
            if (lang == null) {
                try {
                    InputStream resource = Utils.class.getResourceAsStream("/files/lang.json");
                    if (resource == null) {
                        throw new Exception("Failed to load lang resource.");
                    }
                    lang = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
                } catch (Exception e) {
                    Log.crit(e);
                }
            }
            //Log.info(message.toString());

            String key = message.key();
            String base = key;

            JsonElement value = lang.get(key);
            if (value != null) {
                base = value.getAsString();
            }

            ArrayList<String> placeholders = new ArrayList<>();

            if (message.arguments().size() > 0) {
                for (TranslationArgument arg : message.arguments()) {
                    Component component = arg.asComponent();
                    if (component instanceof TranslatableComponent) {
                        placeholders.add(translate((TranslatableComponent) component));
                    } else if (arg instanceof TextComponent) {
                        placeholders.add(getFullText((TextComponent) component, false));
                    }
                }
                //Log.info(Arrays.toString(placeholders.toArray()));
                try {
                    return String.format(base, placeholders.toArray());
                } catch (Exception e) {
                    Log.error("Error formatting '"+base+"' with placeholders " + Arrays.toString(placeholders.toArray()));
                }
            } else {
                return base;
            }
        } catch (Exception e) {
            Log.error(e);
        }

        return "";
    }
}
