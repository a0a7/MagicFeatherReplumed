package be.ephys.magicfeather;

import be.ephys.magicfeather.config.ConfigSynchronizer;
import be.ephys.magicfeather.content.BeaconRangeCalculator;
import be.ephys.magicfeather.content.BeaconTypeHandler;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(MagicFeather.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MagicFeather.MODID)
public class MagicFeather {
  public static final String MODID = "magicfeather";
  private static final Logger logger = LogManager.getLogger(MODID);
  @ObjectHolder("alexsmobs:am_beacon")
  public static PoiType AM_BEACON_POI;

  public static PoiType MF_BEACON_POI;

  public MagicFeather() {
    ConfigSynchronizer.synchronizeConfig();

    FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(PoiType.class, this::registerPoiTypes);
  }

  public static Logger getLogger() {
    return logger;
  }

  public static PoiType getBeaconPoi() {
    if (AM_BEACON_POI != null) {
      return AM_BEACON_POI;
    }

    return MF_BEACON_POI;
  }

  public void registerPoiTypes(RegistryEvent.Register<PoiType> event) {
    if (ModList.get().isLoaded("alexsmobs")) {
      return;
    }

    MF_BEACON_POI = new PoiType(
      MagicFeather.MODID + ":beacon",
      PoiType.getBlockStates(Blocks.BEACON),
      0,
      1
    );

    MF_BEACON_POI.setRegistryName(MagicFeather.MODID, "beacon");

    event.getRegistry().register(MF_BEACON_POI);

  }

  @SubscribeEvent
  public static void processInterComms(InterModProcessEvent event) {
    event.getIMCStream(method -> method.equals("add-beacon-handler")).forEach(msg -> {
      Object data = msg.messageSupplier().get();

      if (data instanceof BeaconTypeHandler) {
        BeaconRangeCalculator.registerBeaconType((BeaconTypeHandler) data);
      }
    });
  }
}
