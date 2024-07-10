package fr.kolala.slimemap;

import fr.kolala.slimemap.item.ModItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlimeMap implements ModInitializer {
	public static final String MOD_ID = "slimemap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing {}.", MOD_ID);
		ModItems.initialize();
	}
}