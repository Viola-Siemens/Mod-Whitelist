package com.hexagram2021.mod_whitelist.common.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hexagram2021.mod_whitelist.common.utils.MWLogger;

import java.io.*;
import java.util.List;

import static com.hexagram2021.mod_whitelist.ModWhitelist.MODID;

public class MWCommonConfig {
	public interface IConfigValue<T extends Serializable> {
		List<IConfigValue<?>> configValues = Lists.newArrayList();
		
		String name();
		T value();
		void parseAsValue(JsonElement element);
		
		void checkValueRange() throws ConfigValueException;
	}
	
	public static class FloatConfigValue implements IConfigValue<Float> {
		private final String name;
		private float value;
		private final float min;
		private final float max;
		
		public FloatConfigValue(String name, float value, float min, float max) {
			this.name = name;
			this.value = value;
			this.min = min;
			this.max = max;
			
			configValues.add(this);
		}
		
		@Override
		public void checkValueRange() throws ConfigValueException {
			if(this.value > this.max || this.value < this.min) {
				throw new ConfigValueException(this.name + " is not in range [%f, %f]! Please check your config file.".formatted(this.min, this.max));
			}
		}
		
		@Override
		public void parseAsValue(JsonElement element) {
			this.value = element.getAsFloat();
		}
		
		@Override
		public String name() {
			return this.name;
		}
		
		@Override
		public Float value() {
			return this.value;
		}
	}
	
	public static class IntConfigValue implements IConfigValue<Integer> {
		private final String name;
		private int value;
		private final int min;
		private final int max;
		
		public IntConfigValue(String name, int value, int min, int max) {
			this.name = name;
			this.value = value;
			this.min = min;
			this.max = max;
			
			configValues.add(this);
		}
		
		@Override
		public void checkValueRange() throws ConfigValueException {
			if(this.value > this.max || this.value < this.min) {
				throw new ConfigValueException(this.name + " is not in range [%d, %d]! Please check your config file.".formatted(this.min, this.max));
			}
		}
		
		@Override
		public void parseAsValue(JsonElement element) {
			this.value = element.getAsInt();
		}
		
		@Override
		public String name() {
			return this.name;
		}
		
		@Override
		public Integer value() {
			return this.value;
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
			writer.write("\n\n");
			
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
				} else {
					MWLogger.LOGGER.error("Unknown Config Value Type: " + value.getClass().getName());
				}
			});
			IConfigHelper.writeJsonToFile(writer, null, configJson, 0);
		}
	}
	
	public static void checkValues() {
		IConfigValue.configValues.forEach(IConfigValue::checkValueRange);
	}
	
	public static class ConfigValueException extends RuntimeException {
		public ConfigValueException(String message) {
			super(message);
		}
	}
}
