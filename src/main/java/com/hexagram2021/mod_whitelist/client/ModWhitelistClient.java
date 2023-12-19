package com.hexagram2021.mod_whitelist.client;

import com.google.common.collect.Lists;
import com.hexagram2021.mod_whitelist.ModWhitelist;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

import static com.hexagram2021.mod_whitelist.ModWhitelist.MODID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModWhitelistClient {
	public static final List<String> mods = Lists.newArrayList();

	@SubscribeEvent
	public void onInitializeClient(FMLClientSetupEvent event) {
		mods.clear();
		ModList.get().getMods().forEach(mod -> mods.add(mod.getModId()));
		mods.sort(String::compareTo);

		hello();
	}

	public static void hello() {
		StringBuilder modlist = new StringBuilder();
		mods.forEach(mod -> modlist.append('"').append(mod).append("\", "));
		MWLogger.LOGGER.info("%s v%s from the client! Modlist: [%s]".formatted(ModWhitelist.MOD_NAME, ModWhitelist.MOD_VERSION, modlist.toString()));
	}
}
