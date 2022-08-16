package me.creepermaxcz.mcbots;

import com.diogonunes.jcolor.Attribute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Utils {

    private static JsonObject lang;

    public static String getFullText(TextComponent message, boolean colored) {
        if (message.children().size() > 0) {
            StringBuilder text = new StringBuilder();
            for (Component child : message.children()) {
                if (child instanceof TextComponent) {
                    text.append(getFullText((TextComponent) child, colored));
                } else if (child instanceof TranslatableComponent) {
                    text.append(translate((TranslatableComponent) child));
                }

            }
            return text.toString();
        } else {
            String out = message.content();
            if (colored) {
                if (message.style().color() != null) {
                    List<Attribute> formats = new ArrayList<>(getFormats(message.style().decorations()));
                    formats.add(getColor((NamedTextColor) message.style().color()));
                    out = colorize(out, formats.toArray(new Attribute[0]));
                }
            }
            return out;
        }
    }

    /**
     * A static function that gets full text from text components.
     * From 1.19.1 the protocol does not include sender's information in the message.
     * Thus, we need to merge two TextComponent (sender and message) and generate a text that consists of sent message.
     * This means using old getFullText will just send the message. Which does NOT contain username who sent this message
     * @param sender The TextComponent for sender.
     * @param message The TextComponent for message.
     * @param colored Whether if this output was colored or not.
     * @return Returns String in "Username : Message" format.
     */
    public static String getFullText(@Nullable TextComponent sender, TextComponent message, boolean colored) {
        if (colored) { // Check if we are using colors.
            Style messageStyle = message.style();
            StringBuilder outString = new StringBuilder();

            if (sender != null) {
                Style senderStyle = sender.style();
                if (senderStyle.color() != null) { // Generate sender text with color.
                    NamedTextColor color = (NamedTextColor) senderStyle.color();
                    outString.append(colorize(sender.content(), getColor(color)));
                } else {
                    outString.append(sender.content());
                }
                outString.append(" : "); // Add delimiter as : between sender and message.
            }

            if (messageStyle.color() != null) { // Generate message text with color.
                NamedTextColor color = (NamedTextColor) messageStyle.color();
                outString.append(colorize(message.content(), getColor(color)));
            } else {
                outString.append(message.content());
            }

            return outString.toString();
        } else { // When color formatting was disabled.
            if (sender == null)
                return message.content();
            else
                return sender.content() + " : " + message.content();
        }
    }

    /**
     * A static function that gets full text from text components.
     * From 1.19.1 the protocol does not include sender's information in the message.
     * Sometimes the message as component is null. When this happens, this method will show message as String.
     * Since this is just a String being represented, this will not have formatting on String.
     * @param sender The TextComponent for sender.
     * @param message The TextComponent for message.
     * @param colored Whether if this output was colored or not.
     * @return Returns String in "Username : Message" format.
     */
    public static String getFullText(@Nullable TextComponent sender, String message, boolean colored) {
        if (colored) { // Check if we are using colors.
            StringBuilder outString = new StringBuilder();

            if (sender != null) {
                Style senderStyle = sender.style();
                if (senderStyle.color() != null) { // Generate sender text with color.
                    NamedTextColor color = (NamedTextColor) senderStyle.color();
                    outString.append(colorize(sender.content(), getColor(color)));
                } else {
                    outString.append(sender.content());
                }
                outString.append(" : "); // Add delimiter as : between sender and message.
            }

            outString.append(message);
            return outString.toString();
        } else { // When color formatting was disabled.
            if (sender == null)
                return message;
            else
                return sender.content() + " : " + message;
        }
    }

    public static Attribute getColor(NamedTextColor color) {
        switch (color.toString()) {
            case "red":
                return Attribute.BRIGHT_RED_TEXT();
            case "dark_red":
                return Attribute.RED_TEXT();

            case "blue":
                return Attribute.BRIGHT_BLUE_TEXT();
            case "dark_blue":
                return Attribute.BLUE_TEXT();

            case "white":
                return Attribute.BRIGHT_WHITE_TEXT();
            case "black":
                return Attribute.BLACK_TEXT();
            case "gray":
                return Attribute.WHITE_TEXT();
            case "dark_gray":
                return Attribute.BRIGHT_BLACK_TEXT();

            case "aqua":
                return Attribute.BRIGHT_CYAN_TEXT();
            case "dark_aqua":
                return Attribute.CYAN_TEXT();

            case "yellow":
                return Attribute.BRIGHT_YELLOW_TEXT();
            case "gold":
                return Attribute.YELLOW_TEXT();

            case "green":
                return Attribute.BRIGHT_GREEN_TEXT();
            case "dark_green":
                return Attribute.GREEN_TEXT();

            case "purple":
                return Attribute.BRIGHT_MAGENTA_TEXT();
            case "dark_purple":
                return Attribute.MAGENTA_TEXT();
        }
        return Attribute.NONE();
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

        if (message.args().size() > 0) {
            for (Component component : message.args()) {
                if (component instanceof TranslatableComponent) {
                    placeholders.add(translate((TranslatableComponent) component));
                }
                else if (component instanceof TextComponent) {
                    placeholders.add(getFullText((TextComponent) component, false));
                }
            }
            //Log.info(Arrays.toString(placeholders.toArray()));
            return String.format(base, placeholders.toArray());
        } else {
            return base;
        }
    }

}
