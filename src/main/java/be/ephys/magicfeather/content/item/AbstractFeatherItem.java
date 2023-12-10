package be.ephys.magicfeather.content.item;

import be.ephys.magicfeather.MFConfig;
import be.ephys.magicfeather.content.MFItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.WeakHashMap;

public class AbstractFeatherItem extends Item {
  // TODO this should be a capability

   public enum FallStyle {
    SLOW_FALL,
    NEGATE_FALL_DAMAGE
  }

  public AbstractFeatherItem(Properties properties) {
    super(properties);
  }

  public int getEntityLifespan(ItemStack itemStack, Level world) {
    return Integer.MAX_VALUE;
  }

  public static void setMayFly(Player player, boolean mayFly) {

    if (player.getAbilities().mayfly == mayFly) {
      return;
    }

    player.getAbilities().mayfly = mayFly;
    player.onUpdateAbilities();
  }

  public boolean hasCustomEntity(ItemStack stack) {
    return true;
  }

  public static boolean requiresCurios() {
    return isCuriosInstalled() && MFConfig.looseRequiresCurios.get();
  }

  public static boolean isCuriosInstalled() {
    return ModList.get().isLoaded("curios");
  }

  public static boolean isCuriosEquipped(Player player, Item item) {
    return CuriosApi.getCuriosHelper().findFirstCurio(player, item).isPresent();
  }

  public static boolean hasItem(Player player, Item item) {
    if (isCuriosInstalled()) {
      if (isCuriosEquipped(player, item)) {
        return true;
      }

      // if requireCurios is false, we'll check the main inventory
      if (MFConfig.looseRequiresCurios.get()) {
        return false;
      }
    }

    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
      ItemStack stack = player.getInventory().getItem(i);
      if (item.equals(stack.getItem())) {
        return true;
      }
    }

    return false;
  }

  public Entity createEntity(Level world, Entity entity, ItemStack itemstack) {
    entity.setInvulnerable(true);

    return null;
  }

}
