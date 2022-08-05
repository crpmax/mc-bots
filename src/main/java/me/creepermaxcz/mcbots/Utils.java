package me.creepermaxcz.mcbots;

import com.diogonunes.jcolor.Attribute;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

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
