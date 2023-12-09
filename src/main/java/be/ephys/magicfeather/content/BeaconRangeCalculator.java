package be.ephys.magicfeather.content;

import be.ephys.magicfeather.MagicFeather;
import be.ephys.magicfeather.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Optional;
import java.util.WeakHashMap;

public final class BeaconRangeCalculator {

  public enum BeaconVerticalRangeType {
    Java(0, 256),
    FullHeight(0, 0);

    private final int downRangeExtension;
    private final int upRangeExtension;

    BeaconVerticalRangeType(int downRangeExtension, int upRangeExtension) {
      this.downRangeExtension = downRangeExtension;
      this.upRangeExtension = upRangeExtension;
    }
  }

  @Config(name = "range_computation.vertical_range_type", description = "How the beacon range is calculated vertically. Java = Vanilla Java Behavior. Bedrock = Vanilla Bedrock behavior. FullHeight = expand vertical range to maximum")
  @Config.EnumDefault(value = "FullHeight", enumType = BeaconVerticalRangeType.class)
  public static ForgeConfigSpec.EnumValue<BeaconVerticalRangeType> verticalRangeType;

  @Config(name = "range_computation.base_range", description = "What is the beacon base range?")
  @Config.IntDefault(10)
  public static ForgeConfigSpec.IntValue baseRange;

  @Config(name = "range_computation.range_step", description = "How many blocks are added to the range per level?")
  @Config.IntDefault(10)
  public static ForgeConfigSpec.IntValue rangeStep;

  private static final WeakHashMap<Class<? extends BlockEntity>, BeaconTypeHandler> beaconHandlers = new WeakHashMap<>();

  public static void registerBeaconType(BeaconTypeHandler data) {
    beaconHandlers.put(data.getTargetClass(), data);
  }

  public static boolean isInBeaconRange(Entity entity) {
    ServerLevel world = (ServerLevel) entity.getLevel();
    Vec3 entityPos = entity.getEyePosition();

    BeaconVerticalRangeType verticalRangeType = BeaconRangeCalculator.verticalRangeType.get();

    PoiManager poiManager = world.getPoiManager();

    int maxRange = getRangeForLevel(6);

    Optional<BlockPos> foundBeaconPos = poiManager.find(
      MagicFeather.getBeaconPoi().getPredicate(),
      (pos) -> {
        BlockEntity blockEntityAtPos = world.getBlockEntity(pos);

        if (!(blockEntityAtPos instanceof BeaconBlockEntity)) {
          return false;
        }

        int radius = getBeaconRange(blockEntityAtPos);
        if (radius == 0) {
          return false;
        }

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (entityPos.x < (x - radius) || entityPos.x > (x + radius)) {
          return false;
        }

        if (entityPos.z < (z - radius) || entityPos.z > (z + radius)) {
          return false;
        }

        if (verticalRangeType != BeaconVerticalRangeType.FullHeight) {
          if (entityPos.y < (y - radius - verticalRangeType.downRangeExtension)
            || entityPos.y > (y + radius + verticalRangeType.upRangeExtension)) {
            return false;
          }
        }

        return true;
      },
      entity.blockPosition(),
      maxRange,
      PoiManager.Occupancy.ANY
    );

    return foundBeaconPos.isPresent();
  }

  private static int getRangeForLevel(int level) {
    return baseRange.get() + (level * rangeStep.get());
  }

  private static int getBeaconRange(BlockEntity te) {
    Class<?> classObj = te.getClass();
    BeaconTypeHandler handler = beaconHandlers.get(classObj);
    if (handler != null) {
      return handler.getFlightRangeAroundBeacon(te);
    }

    if (!(te instanceof BeaconBlockEntity)) {
      return 0;
    }

    BeaconBlockEntity beacon = (BeaconBlockEntity) te;
    // beacon is disabled
    if (beacon.beamSections.isEmpty()) {
      return 0;
    }

    int level = beacon.levels;
    if (level == 0) {
      return 0;
    }

    return getRangeForLevel(level);
  }
}
