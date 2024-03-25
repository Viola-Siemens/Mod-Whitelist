package com.hexagram2021.mod_whitelist.server.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hexagram2021.mod_whitelist.ModWhitelist;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hexagram2021.mod_whitelist.ModWhitelist.MODID;

public class MWServerConfig {
	public interface IConfigValue<T extends Serializable> {
		List<IConfigValue<?>> configValues = Lists.newArrayList();
		
		String name();
		T value();
		void parseAsValue(JsonElement element);
		
		void checkValueRange() throws ConfigValueException;
	}

	public static abstract class ListConfigValue<T extends Serializable> implements IConfigValue<ArrayList<T>> {
		private final String name;
		private final ArrayList<T> value;

		@SafeVarargs
		public ListConfigValue(String name, T... defaultValues) {
			this(name, Arrays.stream(defaultValues).collect(Collectors.toCollection(Lists::newArrayList)));

			configValues.add(this);
		}

		public ListConfigValue(String name, ArrayList<T> value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public void checkValueRange() throws ConfigValueException {
			this.value.forEach(v -> {
				if(!this.isValid(v)) {
					throw new ConfigValueException(this.createExceptionDescription(v));
				}
			});
		}

		@Override
		public void parseAsValue(JsonElement element) {
			this.value.clear();
			element.getAsJsonArray().asList().forEach(e -> this.value.add(this.parseAsElementValue(e)));
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public ArrayList<T> value() {
			return this.value;
		}

		protected abstract boolean isValid(T element);
		protected abstract String createExceptionDescription(T element);
		protected abstract T parseAsElementValue(JsonElement element);
	}

	public static class ModIdListConfigValue extends ListConfigValue<String> {

		public ModIdListConfigValue(String name, String... defaultValues) {
			super(name, defaultValues);
		}

		@SuppressWarnings("unused")
		public ModIdListConfigValue(String name, ArrayList<String> value) {
			super(name, value);
		}

		@Override
		protected boolean isValid(String element) {
			return Pattern.matches("[a-z\\d\\-._]+", element);
		}

		@Override
		protected String createExceptionDescription(String element) {
			return "\"%s\" is not a valid modid!".formatted(element);
		}

		@Override
		protected String parseAsElementValue(JsonElement element) {
			return element.getAsString();
		}
	}

	public static class BoolConfigValue implements IConfigValue<Boolean> {
		private final String name;
		private boolean value;

		public BoolConfigValue(String name, boolean value) {
			this.name = name;
			this.value = value;

			configValues.add(this);
		}

		@Override
		public void checkValueRange() throws ConfigValueException {
		}

		@Override
		public void parseAsValue(JsonElement element) {
			this.value = element.getAsBoolean();
		}

		@Override
		public String name() {
			return this.name;
		}

		@Override
		public Boolean value() {
			return this.value;
		}
	}

	public static final File filePath = new File("./config/");
	private static final File configFile = new File(filePath + "/" + MODID + "-config.json");
	private static final File readmeFile = new File(filePath + "/" + MODID + "-config-readme.md");
	
	//WhiteLists
	public static final BoolConfigValue USE_WHITELIST_ONLY = new BoolConfigValue("USE_WHITELIST_ONLY", false);
	public static final ModIdListConfigValue CLIENT_MOD_NECESSARY = new ModIdListConfigValue("CLIENT_MOD_NECESSARY", MODID);
	public static final ModIdListConfigValue CLIENT_MOD_WHITELIST = new ModIdListConfigValue("CLIENT_MOD_WHITELIST", "forge", "minecraft", MODID);
	public static final ModIdListConfigValue CLIENT_MOD_BLACKLIST = new ModIdListConfigValue("CLIENT_MOD_BLACKLIST", "aristois", "bleachhack", "meteor-client", "wurst");

	public static List<Pair<String, MismatchType>> test(List<String> mods) {
		List<Pair<String, MismatchType>> ret = Lists.newArrayList();
		for(String mod: CLIENT_MOD_NECESSARY.value()) {
			if(!mods.contains(mod)) {
				ret.add(Pair.of(mod, MismatchType.UNINSTALLED_BUT_SHOULD_INSTALL));
			}
		}
		if(USE_WHITELIST_ONLY.value()) {
			for(String mod: mods) {
				if(!CLIENT_MOD_WHITELIST.value().contains(mod)) {
					ret.add(Pair.of(mod, MismatchType.INSTALLED_BUT_SHOULD_NOT_INSTALL));
				}
			}
		} else {
			for(String mod: mods) {
				if(CLIENT_MOD_BLACKLIST.value().contains(mod)) {
					ret.add(Pair.of(mod, MismatchType.INSTALLED_BUT_SHOULD_NOT_INSTALL));
				}
			}
		}
		return ret;
	}

	static {
		lazyInit();
	}
	
	private static void lazyInit() {
		try {
			if (!filePath.exists() && !filePath.mkdir()) {
				MWLogger.LOGGER.error("Could not mkdir " + filePath);
			} else {
				if (configFile.exists()) {
					try(Reader reader = new FileReader(configFile)) {
						JsonElement json = JsonParser.parseReader(reader);
						loadFromJson(json.getAsJsonObject());
					}
					checkValues();
					saveConfig();
				} else {
					if (configFile.createNewFile()) {
						saveConfig();
					} else {
						MWLogger.LOGGER.error("Could not create new file " + configFile);
					}
				}
				if(!readmeFile.exists()) {
					if (readmeFile.createNewFile()) {
						fillReadmeFile();
					} else {
						MWLogger.LOGGER.error("Could not create new file " + readmeFile);
					}
				}
			}
		} catch (IOException e) {
			MWLogger.LOGGER.error("Error during loading config.", e);
		}
	}
	
	private static void fillReadmeFile() throws IOException {
		try(Writer writer = new FileWriter(readmeFile)) {
			writer.write("# Abstract\n\n");
			writer.write("Thank you for choosing our Mod Whitelist mod to protect your server from client hacking mods. Let me introduce how it works and what you can do.\n\n");
			writer.write("This mod works on client and server separately:\n\n");
			writer.write("- On the client side, it gathers all identifier of mods (\"mod_id\"s), encrypted them and send to the server.\n");
			writer.write("- On the server side, it checks players who try to connect the server if they install hacking mods, or if they do not install any necessary mods to avoid problems.\n\n");
			writer.write("But both sides are required. If not:\n\n");
			writer.write("- Installed on the client side but not installed on the server side. The client player can still enter the server and play, but this mod can not protect your server from hacking.\n");
			writer.write("- Installed on the server side but not installed on the client side. The client player is not allowed to enter the server and sent message \"multiplayer.disconnect.mod_whitelist.packet_corruption\".\n\n");

			writer.write("# Adding a mod to whitelist and blacklist\n\n");
			writer.write("First, you should find the identifier of the mod (modid), a simple way is open the jar file with an archiver software (eg. WinZip, HaoZip, 7-Zip), open \"fabric.mod.json\" and see what the value of key \"id\" is. For example, the modid of Mod Whitelist mod is \"mod_whitelist\".\n\n");
			writer.write("Then, add it to `CLIENT_MOD_NECESSARY` field if you want client players install it. By default, it is blacklist mode, so you can add it to `CLIENT_MOD_BLACKLIST` field if you do not want client players install it. If you want to use whitelist mode instead, set `USE_WHITELIST_ONLY` to true and add all whitelist modids to `CLIENT_MOD_WHITELIST` field.\n\n");
			writer.write("In addition, if `USE_WHITELIST_ONLY` is true, `CLIENT_MOD_BLACKLIST` field is just ignored while running the server. And if `USE_WHITELIST_ONLY` is true, `CLIENT_MOD_WHITELIST` field is ignored instead.\n\n");
			writer.write("As you might see, if fabric-api is installed, the modlist will contains quite a lot of modids. You can run a client with this mod installed, and open \".minecraft/logs/latest.log\", and you will see the following format line to simplify gathering the modlist manually:\n\n");
			writer.write("```\n\nMod Whitelist vx.x.x from the client! Modlist: [\"fabric-api\", \"fabric-api-base\", ...]\n\n```\n\n");

			writer.write("# Issue tracker\n\n");
			writer.write("Visit https://github.com/Viola-Siemens/Mod-Whitelist/issues and post your issue and logs if you find any problems with this mod.\n");
		}
	}
	
	private static void loadFromJson(JsonObject jsonObject) {
		MWLogger.LOGGER.debug("Loading json config file.");
		IConfigValue.configValues.forEach(iConfigValue -> {
			if(jsonObject.has(iConfigValue.name())) {
				iConfigValue.parseAsValue(jsonObject.get(iConfigValue.name()));
			}
		});
	}
	
	private static void saveConfig() throws IOException {
		MWLogger.LOGGER.debug("Saving json config file.");
		try(Writer writer = new FileWriter(configFile)) {
			JsonObject configJson = new JsonObject();
			IConfigValue.configValues.forEach(iConfigValue -> {
				Serializable value = iConfigValue.value();
				if(value instanceof Number number) {
					configJson.addProperty(iConfigValue.name(), number);
				} else if(value instanceof Boolean bool) {
					configJson.addProperty(iConfigValue.name(), bool);
				} else if(value instanceof String str) {
					configJson.addProperty(iConfigValue.name(), str);
				} else if(value instanceof List<?> list) {
					configJson.add(iConfigValue.name(), buildList(list));
				} else {
					MWLogger.LOGGER.error("Unknown Config Value Type: " + value.getClass().getName());
				}
			});
			IConfigHelper.writeJsonToFile(writer, null, configJson, 0);
		}
	}

	private static JsonArray buildList(List<?> list) {
		JsonArray ret = new JsonArray();
		list.forEach(value -> {
			if(value instanceof Number number) {
				ret.add(number);
			} else if(value instanceof Boolean bool) {
				ret.add(bool);
			} else if(value instanceof String str) {
				ret.add(str);
			} else if(value instanceof List<?> list1) {
				ret.add(buildList(list1));
			} else {
				MWLogger.LOGGER.error("Unknown Element Type from List: " + value.getClass().getName());
			}
		});
		return ret;
	}
	
	public static void checkValues() {
		IConfigValue.configValues.forEach(IConfigValue::checkValueRange);
	}
	
	public static class ConfigValueException extends RuntimeException {
		public ConfigValueException(String message) {
			super(message);
		}
	}

	public static void hello() {
		MWLogger.LOGGER.info("%s v%s is protecting your server!".formatted(ModWhitelist.MOD_NAME, ModWhitelist.MOD_VERSION));
	}
}
