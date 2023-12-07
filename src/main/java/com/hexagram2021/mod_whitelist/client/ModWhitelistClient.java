package com.hexagram2021.mod_whitelist.client;

import com.google.common.collect.Lists;
import com.hexagram2021.mod_whitelist.ModWhitelist;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class ModWhitelistClient implements ClientModInitializer {
	public static final List<String> mods = Lists.newArrayList();

	@Override
	public void onInitializeClient() {
		mods.clear();
		FabricLoader.getInstance().getAllMods().forEach(mod -> mods.add(mod.getMetadata().getId()));

		hello();
	}

	public static void hello() {
		StringBuilder modlist = new StringBuilder();
		mods.forEach(mod -> modlist.append(mod).append(", "));
		MWLogger.LOGGER.info("%s v%s from the client! Modlist: [%s]".formatted(ModWhitelist.MOD_NAME, ModWhitelist.MOD_VERSION, modlist.toString()));
	}
}
