package com.hexagram2021.mod_whitelist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class ModWhitelist implements ModInitializer {
	public static final String MODID = "mod_whitelist";
	public static final String MOD_NAME = "Mod Whitelist";
	public static final String MOD_VERSION = FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata().getVersion().getFriendlyString();
	
	@Override
	public void onInitialize() {
	}
}
