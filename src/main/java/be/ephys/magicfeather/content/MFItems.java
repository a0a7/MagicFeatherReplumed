package be.ephys.magicfeather;

import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = MagicFeatherMod.MODID)
public class ModItems {
  private static final DeferredRegister<Item> ITEM_DEFERRED_REGISTER = DeferredRegister.create(Registry.ITEM_REGISTRY, MagicFeatherMod.MODID);
  public static final RegistryObject<Item> MAGIC_FEATHER = ITEM_DEFERRED_REGISTER.register("magicfeather", () -> new ItemMagicFeather(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));

  @SubscribeEvent
  public static void onConstructMod(final FMLConstructModEvent evt) {
    ITEM_DEFERRED_REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
    MinecraftForge.EVENT_BUS.addListener(ItemMagicFeather::onPlayerTick);
  }

  @SubscribeEvent
  public static void sendInterComms(InterModEnqueueEvent event) {
    InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
  }
}
