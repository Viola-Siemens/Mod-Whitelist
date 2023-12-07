package com.hexagram2021.mod_whitelist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ModWhitelist implements ModInitializer {
	public static String MODID = "mod_whitelist";
	public static String MOD_NAME = "Mod Whitelist";
	public static String MOD_VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
	
	@Override
	public void onInitialize() {

	}
}
