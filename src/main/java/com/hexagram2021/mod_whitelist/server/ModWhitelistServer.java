package com.hexagram2021.mod_whitelist.server;

import com.hexagram2021.mod_whitelist.server.config.MWServerConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

import static com.hexagram2021.mod_whitelist.ModWhitelist.MODID;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModWhitelistServer {
	@SubscribeEvent
	public void onInitializeServer(FMLDedicatedServerSetupEvent event) {
		MWServerConfig.hello();
	}
}
