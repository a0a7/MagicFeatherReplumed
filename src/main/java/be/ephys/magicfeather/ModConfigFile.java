package be.ephys.magicfeather;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfigFile {
    public static ForgeConfigSpec.EnumValue<BeaconRangeCalculator.BeaconVerticalRangeType> verticalRangeType;
    public static ForgeConfigSpec.IntValue baseRange;
    public static ForgeConfigSpec.IntValue rangeStep;
    public static ForgeConfigSpec.BooleanValue looseRequiresCurios;
    public static ForgeConfigSpec.EnumValue<ItemMagicFeather.FallStyle> fallStyle;

    public static ForgeConfigSpec buildSpec() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        verticalRangeType = builder.comment("How the beacon range is calculated vertically. JAVA = Vanilla Java Behavior. FULL_HEIGHT = expand vertical range to maximum")
                .defineEnum("range_computation.vertical_range_type", BeaconRangeCalculator.BeaconVerticalRangeType.FULL_HEIGHT);
        baseRange = builder.comment("What is the beacon base range?").defineInRange("range_computation.base_range", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
        rangeStep = builder.comment("How many blocks are added to the range per level?").defineInRange("range_computation.range_step", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
        looseRequiresCurios = builder.comment("If curios is installed, the magic feather will need to be installed in its charm slot to function.").define("item.requires_curios", true);
        fallStyle = builder.comment("When losing the ability to fly, should the player receive a slow fall effect or simply negate the fall damage?")
                .defineEnum("too_close_to_the_sun_behavior", ItemMagicFeather.FallStyle.NEGATE_FALL_DAMAGE);
        return builder.build();
    }
}
