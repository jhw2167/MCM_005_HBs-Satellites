package com.holybuckets.satellite;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CommonProxy {

	default void openScreen(Object screenData) {}
}