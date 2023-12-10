package be.ephys.magicfeather;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

@Mod(MagicFeatherMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MagicFeatherMod.MODID)
public class MagicFeatherMod {
  public static final String MODID = "magicfeather";

  @SubscribeEvent
  public static void onConstructMod(final FMLConstructModEvent evt) {
    ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigFile.buildSpec());
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
