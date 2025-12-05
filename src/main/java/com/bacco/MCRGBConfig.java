package com.bacco;

import com.google.gson.Gson;

import static com.bacco.MCRGBClient.readJson;
import static com.bacco.MCRGBClient.writeJson;

public final class MCRGBConfig {

    public static MCRGBConfig instance = new MCRGBConfig();
    public boolean alwaysShowToolTips = false;
    public boolean sliderConstantUpdate = true;
    public boolean bypassOP = false;
    public String command = "give %p %i%c %q";

    public static void save(){
        Gson gson = new Gson();
        String blockColoursJson = gson.toJson(instance);
        writeJson(blockColoursJson, "./config/mcrgb_forge/", "config.json");
    }

    public static void load(){
        Gson gson = new Gson();
        instance = gson.fromJson(readJson("./config/mcrgb_forge/config.json"), MCRGBConfig.class);
        if(instance == null){
            instance = new MCRGBConfig();
            save();
        }

    }
}
