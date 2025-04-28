package net.laserdiamond.ultimatemanhunt.util.file;

import com.google.gson.*;
import net.laserdiamond.ultimatemanhunt.UltimateManhunt;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public abstract class JsonConfig {

    protected final String fileName;

    protected final File file;

    protected JsonObject jsonObject;

    public JsonConfig(String fileName)
    {
        this.fileName = fileName;
        this.file = new File(this.modFilePath() + fileName + ".json");

        this.createFile();

        try (FileReader fileReader = new FileReader(this.file))
        {
            this.jsonObject = this.createJsonNotNull(new Gson().fromJson(fileReader, JsonObject.class));
        } catch (IOException e)
        {
            UltimateManhunt.LOGGER.info("ERROR CREATING JSON OBJECT FOR FILE: " + fileName);
            e.printStackTrace();
        }
    }

    private String modFilePath()
    {
        return UltimateManhunt.MODID + File.separator + this.folderName() + File.separator;
    }

    protected abstract String folderName();

    public final boolean createFile()
    {
        if (this.file.exists())
        {
            return false;
        }
        this.file.getParentFile().mkdirs();

        try (FileWriter fileWriter = new FileWriter(this.file))
        {
            if (this.file.createNewFile())
            {
                UltimateManhunt.LOGGER.info("Created File: " + this.fileName);
                fileWriter.write(this.jsonObjectPrettyPrint(this.jsonObject));
                return true;
            } else
            {
                UltimateManhunt.LOGGER.info("Couldn't create file: " + this.fileName);
                return false;
            }
        } catch (IOException e)
        {
            UltimateManhunt.LOGGER.info("ERROR CREATING FILE: " + this.fileName + "!");
            e.printStackTrace();
        }
        return false;
    }

    public final boolean deleteFile()
    {
        return this.file.delete();
    }

    public boolean writeJsonToFile()
    {
        try (FileWriter fileWriter = new FileWriter(this.file))
        {
            fileWriter.write(this.jsonObjectPrettyPrint(this.jsonObject));
            return true;
        } catch (IOException e)
        {
            UltimateManhunt.LOGGER.info("ERROR WRITING JSON OBJECT: " + this.jsonObject.toString() + " TO FILE " + this.file + "!");
            e.printStackTrace();
        }
        return false;
    }

    public boolean isJsonNotNull(String key)
    {
        return this.jsonObject.get(key) != null;
    }

    public boolean toJsonNotNull(JsonObject jsonObject, String key, String value)
    {
        if (jsonObject.get(key) == null)
        {
            jsonObject.addProperty(key, value);
            return true;
        }
        return false;
    }

    public boolean toJsonNotNull(JsonObject jsonObject, String key, JsonElement jsonElement)
    {
        if (jsonObject.get(key) == null)
        {
            jsonObject.add(key, jsonElement);
            return true;
        }
        return false;
    }

    public boolean toJsonNotNull(JsonObject jsonObject, String key, Number value)
    {
        if (jsonObject.get(key) == null)
        {
            jsonObject.addProperty(key, value);
            return true;
        }
        return false;
    }

    public JsonObject createJsonNotNull(JsonObject jsonObject)
    {
        if (jsonObject == null)
        {
            jsonObject = new JsonObject();
        }
        return jsonObject;
    }

    protected final String jsonObjectPrettyPrint(JsonObject jsonObject)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement prettyJson = JsonParser.parseString(jsonObject.toString());
        return gson.toJson(prettyJson);
    }
}
