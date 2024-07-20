package com.pro4d.quickmc.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

public class PlayerFetcher {

    private final String url = "https://playerdb.co/api/player/minecraft/";

    @Setter private String name;
    @Setter private UUID uuid;

    public PlayerFetcher(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    public PlayerFetcher(String name) {
        this(name, null);
    }
    public PlayerFetcher(UUID uuid) {
        this(null, uuid);
    }

    @SneakyThrows
    public JsonObject getPlayerJson() {
        if(name == null && uuid == null) return null;
        URL url = new URL(this.url + (name == null ?
                uuid : name));

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();

        return JsonParser.parseString(stringBuilder.toString()).getAsJsonObject()
                .getAsJsonObject("data")
                .getAsJsonObject("player");
    }

    public UUID getUUID() {
        return UUID.fromString(getPlayerJson().get("id").getAsString());
    }

    public String getName(){
        return getPlayerJson().get("username").getAsString();
    }

}
