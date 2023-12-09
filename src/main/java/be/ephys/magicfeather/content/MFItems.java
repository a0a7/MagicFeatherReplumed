package be.ephys.magicfeather.content;

import be.ephys.magicfeather.MagicFeather;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MagicFeather.MODID)
public class MFItems {
  public static MagicFeatherItem magicFeather;

  @SubscribeEvent
  public static void registerItems(RegistryEvent.Register<Item> event) {
    magicFeather = new MagicFeatherItem();

    event.getRegistry().register(magicFeather);
    MinecraftForge.EVENT_BUS.addListener(MFItems.magicFeather::onPlayerTick);
  }

  @SubscribeEvent
  public static void sendInterComms(InterModEnqueueEvent event) {
    InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
  }
}
