package com.shanebeestudios.mcbots.api.generator;

import com.shanebeestudios.mcbots.api.util.logging.Logger;
import org.jetbrains.annotations.Nullable;
import org.jline.utils.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
public class NickGenerator {

    private static final int DEFAULT_NICK_LENGTH = 16; // Minecraft names can only be this long
    private final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();
    private int linesSize;
    private List<String> lines;
    private int nickLength = DEFAULT_NICK_LENGTH;
    private boolean real;
    private String prefix = "";

    public NickGenerator(@Nullable String path, String prefix, boolean useRealNicknames) {
        if (path != null) {
            Logger.info("Loading nicknames from specified file");
            int nicksCount = loadFromFile(path);

            if (nicksCount == 0) {
                Logger.error("No valid nicknames loaded");
                System.exit(1);
            } else {
                Logger.info("Loaded %s nicknames", this.linesSize);
            }
        } else {
            if (useRealNicknames && this.lines == null) {
                loadLines();
                Logger.info("Loaded %s nicknames", this.linesSize);
            }
            this.real = useRealNicknames;
        }
        this.nickLength = DEFAULT_NICK_LENGTH - prefix.length();
        this.prefix = prefix;
    }

    private int loadFromFile(String filePath) {
        this.lines = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filePath));
            while (scanner.hasNextLine()) {
                try {
                    String line = scanner.nextLine().trim();

                    //add only valid nicknames
                    if (line.matches("^[a-zA-Z0-9_]{3,16}$")) {
                        this.lines.add(line);
                    }
                } catch (Exception ignored) {
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            Logger.error("Invalid nicknames list file path");
            System.exit(1);
        }

        this.linesSize = this.lines.size();
        Logger.info("Loaded " + this.linesSize + " valid nicknames");
        this.real = true;

        return this.linesSize;
    }

    private void loadLines() {
        try (InputStream resource = getClass().getResourceAsStream("/files/nicks.txt")) {
            assert resource != null;
            lines = new BufferedReader(
                new InputStreamReader(resource, StandardCharsets.UTF_8)).lines().collect(Collectors.toList()
            );
            linesSize = lines.size();
        } catch (Exception e) {
            Logger.error(e);
            System.exit(1);
        }
    }

    private String generateRandom(int len) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return result.toString();
    }

    public String nextReal() {
        String nick = this.prefix + this.lines.get(this.random.nextInt(this.linesSize));
        return nick.length() <= 16 ? nick : nick.substring(0, 15);
    }

    /**
     * Generate next nick
     *
     * @return generated nick
     */
    public String nextNick() {
        return this.real ? nextReal() : (this.prefix + generateRandom(this.nickLength));
    }

}
