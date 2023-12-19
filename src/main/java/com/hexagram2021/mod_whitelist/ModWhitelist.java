package com.hexagram2021.mod_whitelist;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod(ModWhitelist.MODID)
public class ModWhitelist {
	public static final String MODID = "mod_whitelist";
	public static final String MOD_NAME = "Mod Whitelist";
	public static final String MOD_VERSION = ModList.get().getModFileById(MODID).versionString();
	
	public ModWhitelist() {
		MinecraftForge.EVENT_BUS.register(this);
	}
}
