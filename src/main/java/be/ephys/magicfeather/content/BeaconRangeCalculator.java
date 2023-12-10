package be.ephys.magicfeather;

import be.ephys.magicfeather.mixin.BeaconBlockEntityAccessor;
import be.ephys.magicfeather.mixin.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public final class BeaconRangeCalculator {

    public enum BeaconVerticalRangeType {
        JAVA(0, 256),
        FULL_HEIGHT(0, 0);

        private final int downRangeExtension;
        private final int upRangeExtension;

        BeaconVerticalRangeType(int downRangeExtension, int upRangeExtension) {
            this.downRangeExtension = downRangeExtension;
            this.upRangeExtension = upRangeExtension;
        }
    }

    private static final WeakHashMap<Class<? extends BlockEntity>, BeaconTypeHandler> GLOBAL_BEACON_HANDLERS = new WeakHashMap<>();

    public static void registerBeaconType(BeaconTypeHandler data) {
        GLOBAL_BEACON_HANDLERS.put(data.getTargetClass(), data);
    }

    public static boolean isInBeaconRange(Entity entity) {
        Level world = entity.level;
        Vec3 entityPos = entity.position();

        BeaconVerticalRangeType verticalRangeType = ModConfigFile.verticalRangeType.get();

        // vanilla creates a new block entity if one isn't found, this will be problematic here, so use the forge method that doesn't do that and just returns null
        List<BlockEntity> tickingBlockEntities = ((LevelAccessor) world).getBlockEntityTickers().stream()
                .filter(blockEntity -> !blockEntity.isRemoved() && blockEntity.getPos() != null)
                .map(blockEntity -> world.getExistingBlockEntity(blockEntity.getPos()))
                .filter(Objects::nonNull)
                .toList();

        for (BlockEntity t : tickingBlockEntities) {
            int radius = getBeaconRange(t);

            if (radius == 0) {
                continue;
            }

            BlockPos pos = t.getBlockPos();
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            if (entityPos.x < (x - radius) || entityPos.x > (x + radius)) {
                continue;
            }

            if (entityPos.z < (z - radius) || entityPos.z > (z + radius)) {
                continue;
            }

            if (verticalRangeType != BeaconVerticalRangeType.FULL_HEIGHT) {
                if (entityPos.y < (y - radius - verticalRangeType.downRangeExtension)
                        || entityPos.y > (y + radius + verticalRangeType.upRangeExtension)) {
                    continue;
                }
            }

            return true;
        }

        return false;
    }

    private static int getBeaconRange(BlockEntity te) {
        Class<?> classObj = te.getClass();
        BeaconTypeHandler handler = GLOBAL_BEACON_HANDLERS.get(classObj);
        if (handler != null) {
            return handler.getFlightRangeAroundBeacon(te);
        }

        if (!(te instanceof BeaconBlockEntity beacon)) {
            return 0;
        }

        int rangeStep = ModConfigFile.rangeStep.get();
        int baseRange = ModConfigFile.baseRange.get();

        int level = ((BeaconBlockEntityAccessor) beacon).getLevels();

        return (level * rangeStep + baseRange);
    }
}