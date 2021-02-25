package me.creepermaxcz.mcbots;

import com.diogonunes.jcolor.Attribute;
import com.github.steveice10.mc.protocol.data.message.Message;
import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.data.message.TranslationMessage;
import com.github.steveice10.mc.protocol.data.message.style.ChatColor;
import com.github.steveice10.mc.protocol.data.message.style.ChatFormat;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Utils {

    private static JsonObject lang;

    public static String getFullText(TextMessage message) {
        if (message.getExtra().size() > 0) {
            StringBuilder text = new StringBuilder();
            for (Message extra : message.getExtra()) {
                text.append(getFullText((TextMessage) extra));
            }
            return text.toString();
        } else {
            return ((TextMessage)message).getText();
        }
    }

    public static String getFormattedFullText(Message message) {
        if (message.getExtra().size() > 0) {
            StringBuilder text = new StringBuilder();
            for (Message extra : message.getExtra()) {
                text.append(getFormattedFullText(extra));
            }
            return text.toString();
        } else {

            String out = ((TextMessage) message).getText();

            List<Attribute> formats = new ArrayList<>(getFormat(message.getStyle().getFormats()));
            formats.add(getColor(message.getStyle().getColor()));

            out = colorize(out, formats.toArray(new Attribute[0]));

            return out;
        }
    }

    public static Attribute getColor(String name) {
        switch (name) {
            case ChatColor.RED:
                return Attribute.BRIGHT_RED_TEXT();
            case ChatColor.DARK_RED:
                return Attribute.RED_TEXT();

            case ChatColor.BLUE:
                return Attribute.BRIGHT_BLUE_TEXT();
            case ChatColor.DARK_BLUE:
                return Attribute.BLUE_TEXT();

            case ChatColor.WHITE:
                return Attribute.BRIGHT_WHITE_TEXT();
            case ChatColor.BLACK:
                return Attribute.BLACK_TEXT();
            case ChatColor.GRAY:
                return Attribute.WHITE_TEXT();
            case ChatColor.DARK_GRAY:
                return Attribute.BRIGHT_BLACK_TEXT();

            case ChatColor.AQUA:
                return Attribute.BRIGHT_CYAN_TEXT();
            case ChatColor.DARK_AQUA:
                return Attribute.CYAN_TEXT();

            case ChatColor.YELLOW:
                return Attribute.BRIGHT_YELLOW_TEXT();
            case ChatColor.GOLD:
                return Attribute.YELLOW_TEXT();

            case ChatColor.GREEN:
                return Attribute.BRIGHT_GREEN_TEXT();
            case ChatColor.DARK_GREEN:
                return Attribute.GREEN_TEXT();

            case ChatColor.LIGHT_PURPLE:
                return Attribute.BRIGHT_MAGENTA_TEXT();
            case ChatColor.DARK_PURPLE:
                return Attribute.MAGENTA_TEXT();
        }
        return Attribute.NONE();
    }

    public static List<Attribute> getFormat(List<ChatFormat> formats) {
        List<Attribute> list = new ArrayList<>();
        formats.forEach(format -> {
            switch (format) {
                case BOLD:
                    list.add(Attribute.BOLD());
                    break;
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
        });
        return list;
    }

    public static String translate(TranslationMessage message) {
        if (lang == null) {
            try {
                InputStream resource = Utils.class.getClass().getResourceAsStream("/files/lang.json");
                lang = new JsonParser().parse(new InputStreamReader(resource)).getAsJsonObject();
            } catch (Exception e) {
                Log.error(e);
            }
        }
        Log.info(message.toString());

        String key = message.getKey();
        String base = key;

        JsonElement value = lang.get(key);
        if (value != null) {
            base = value.getAsString();
        }

        ArrayList<String> placeholders = new ArrayList<>();

        if (message.getWith().size() > 0) {
            for (Message with : message.getWith()) {
                if (with instanceof TranslationMessage) {
                    placeholders.add(translate((TranslationMessage) with));
                }
                else if (with instanceof TextMessage) {
                    placeholders.add(((TextMessage) with).getText());
                }
            }
            Log.info(Arrays.toString(placeholders.toArray()));
            return String.format(base, placeholders.toArray());
        } else {
            return base;
        }
    }
}
