package com.shanebeestudios.mcbots.api.generator;

import com.shanebeestudios.mcbots.api.util.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

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
                loadDefaultFile();
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
            this.lines = getListFromReader(new FileReader(filePath));
        } catch (FileNotFoundException ignore) {
            Logger.error("Invalid nicknames list file path, loading default file!");
            loadDefaultFile();
        }

        this.linesSize = this.lines.size();
        this.real = true;
        return this.linesSize;
    }

    private void loadDefaultFile() {
        try (InputStream resource = getClass().getResourceAsStream("/nicks.txt")) {
            assert resource != null;
            this.lines = getListFromReader(new InputStreamReader(resource, StandardCharsets.UTF_8));
            this.linesSize = this.lines.size();
        } catch (Exception e) {
            Logger.error(e);
            System.exit(1);
        }
    }

    private List<String> getListFromReader(Reader reader) {
        return new BufferedReader(reader)
            .lines()
            .filter(line -> line.matches("^[a-zA-Z0-9_]{3," + DEFAULT_NICK_LENGTH + "}$"))
            .toList();
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
