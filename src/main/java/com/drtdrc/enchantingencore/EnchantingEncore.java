package com.drtdrc.enchantingencore;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantingEncore implements ModInitializer {

    public static final String MOD_ID = "enchanting-encore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("EnchantingEncore Init");
    }
}
