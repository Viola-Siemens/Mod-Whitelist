package com.hexagram2021.mod_whitelist.server;

import com.hexagram2021.mod_whitelist.server.config.MWServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;

public class ModWhitelistServer implements DedicatedServerModInitializer {
	@Override
	public void onInitializeServer() {
		MWServerConfig.hello();
	}
}
