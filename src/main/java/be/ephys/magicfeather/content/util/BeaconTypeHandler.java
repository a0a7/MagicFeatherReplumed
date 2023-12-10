package be.ephys.magicfeather.content.util;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BeaconTypeHandler {
  Class<? extends BlockEntity> getTargetClass();

  int getFlightRangeAroundBeacon(BlockEntity beacon);
}
