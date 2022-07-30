package me.creepermaxcz.mcbots;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class NickGenerator {

    private static final int NICK_LEN = 16;
    private final SecureRandom random = new SecureRandom();

    private final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final int charsLen = 62;

    private int linesSize;
    private List<String> lines;

    private int nickLen = NICK_LEN;

    private boolean real = false;

    private String prefix = "";

    public int loadFromFile(String filePath) {
        lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                try {
                    String line = scanner.nextLine().trim();

                    //add only valid nicknames
                    if (line.matches("^[a-zA-Z0-9_]{3,16}$")) {
                        lines.add(line);
                    }
                }
                catch (Exception ignored) { }
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
            lines = new BufferedReader(
                    new InputStreamReader(resource, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()
            );
            linesSize = lines.size();
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }

    public String generateRandom(int len) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(CHARS.charAt(random.nextInt(charsLen)));
        }
        return result.toString();
    }
    
    public String nextRandom() {
        return prefix + generateRandom(nickLen);
    }

    public String nextReal() {
        String nick = prefix + lines.get(random.nextInt(linesSize));
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
