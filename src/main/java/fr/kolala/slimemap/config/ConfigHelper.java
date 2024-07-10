package fr.kolala.slimemap.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.kolala.slimemap.SlimeMap;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public class ConfigHelper {
    private static File getConfigFile() {
        return new File(new File(MinecraftClient.getInstance().runDirectory, "config"), SlimeMap.MOD_ID + ".json");
    }

    public static boolean createConfigFileIfNotExisting() {
        // Create the file
        try {
            if (getConfigFile().createNewFile()) {
                SlimeMap.LOGGER.info("Created config file.");
            }
        }
        catch (IOException e) {
            SlimeMap.LOGGER.error("Couldn't create config file.");
            return true;
        }

        // Write default content into the file
        JsonObject defaultContent = new JsonObject();
        defaultContent.addProperty("coverBlocks", 1);
        return !write(defaultContent);
    }

    public static @Nullable JsonObject read() {
        File configFile = getConfigFile();

        if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
            SlimeMap.LOGGER.error("Config file is not readable, Creating a new one.");
            if (createConfigFileIfNotExisting()) {
                SlimeMap.LOGGER.error("Couldn't create config file.");
                return null;
            }
        }

        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(inputStreamReader).getAsJsonObject();
        }
        catch (Exception e) {
            SlimeMap.LOGGER.error("Failed to parse the JSON file '{}'", configFile.getAbsolutePath(), e);
            return null;
        }
    }

    public static boolean write(JsonObject json) {
        File configFile = getConfigFile();

        if (!configFile.isFile() || !configFile.canWrite() || !configFile.exists()) {
            SlimeMap.LOGGER.error("Config file is not writable, Creating a new one.");
            if (createConfigFileIfNotExisting()) {
                SlimeMap.LOGGER.error("Couldn't create config file.");
                return false;
            }
        }

        File fileTmp = new File(configFile.getParentFile(), configFile.getName() + ".tmp");

        if (fileTmp.exists())
        {
            fileTmp = new File(configFile.getParentFile(), UUID.randomUUID() + ".tmp");
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileTmp), StandardCharsets.UTF_8))
        {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
            writer.close();

            if (configFile.exists() && configFile.isFile() && !configFile.delete())
            {
                SlimeMap.LOGGER.warn("Failed to delete file '{}'", configFile.getAbsolutePath());
            }

            return fileTmp.renameTo(configFile);
        }
        catch (Exception e)
        {
            SlimeMap.LOGGER.warn("Failed to write JSON data to file '{}'", fileTmp.getAbsolutePath(), e);
        }

        return false;
    }


    private static JsonElement get(String name) {
        JsonObject json = read();
        if (json == null) {
            SlimeMap.LOGGER.error("Couldn't retrieve the value for {}!", name);
            return null;
        }
        return json.get(name);
    }

    public static int getInt(String name) {
        return Objects.requireNonNull(get(name)).getAsInt();
    }
}