package me.creepermaxcz.mcbots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NickGenerator {

    private static final int NICK_LEN = 16;
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    private final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private final int charsLen = 62;

    private int linesSize;
    private Set<String> lines;

    private int nickLen = NICK_LEN;

    private boolean real = false;

    private String prefix = "";

    public int loadFromFile(String filePath) {
        lines = new HashSet<>();
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                try {
                    String line = scanner.nextLine().trim();

                    //add only valid nicknames
                    switch (line.length()) {
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 12:
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                            lines.add(line);
                            break;
                    }
                } catch (Exception ignored) { }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            Log.error("Invalid nicknames list file path");
            System.exit(1);
        }

        linesSize = lines.size();
        Log.info("Loaded " + linesSize + " valid nicknames");
        real = true;

        return linesSize;
    }

    private void loadLines() {
        try (InputStream resource = getClass().getResourceAsStream("/files/nicks.txt")) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
                lines = reader.lines().collect(Collectors.toSet());
                linesSize = lines.size();
            }
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }

    public String generateRandom(int len) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(CHARS[random.nextInt(charsLen)]);
        }
        return result.toString();
    }

    public String nextRandom() {
        return prefix + generateRandom(nickLen);
    }

    public String nextReal() {
        String nick = prefix + lines.toArray(new String[0])[random.nextInt(linesSize)];
        return nick.length() <= 16 ? nick : nick.substring(0, 15);
    }

    /**
     * Generate next nick
     * @return generated nick
     */
    public String nextNick() {
        return real ? nextReal() : nextRandom();
    }

    /**
     * Set prefix for generated nicks
     * @param prefix the prefix
     */
    public void setPrefix(String prefix) {
        nickLen = NICK_LEN - prefix.length();
        this.prefix = prefix;
    }

    /**
     * Set nick is real from nick list
     *
     * @param real true to generate real nicknames
     */
    public void setReal(boolean real) {
        if (real && lines == null) loadLines();
        this.real = real;
    }
}
