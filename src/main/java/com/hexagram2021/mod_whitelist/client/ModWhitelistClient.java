package com.hexagram2021.mod_whitelist.client;

import com.google.common.collect.Lists;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;

public class ModWhitelistClient implements ClientModInitializer {
	public static final List<String> mods = Lists.newArrayList();

	@Override
	public void onInitializeClient() {
		mods.clear();
		FabricLoader.getInstance().getAllMods().forEach(mod -> mods.add(mod.getMetadata().getId()));
	}
}
