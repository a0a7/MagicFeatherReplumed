package be.ephys.magicfeather;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.List;
import java.util.WeakHashMap;

public class ItemMagicFeather extends Item {
  // TODO this should be a capability
  private static final WeakHashMap<Player, MagicFeatherData> GLOBAL_PLAYER_DATA = new WeakHashMap<>();

   public enum FallStyle {
    SLOW_FALL,
    NEGATE_FALL_DAMAGE
  }

  public ItemMagicFeather(Properties properties) {
    super(properties);
  }

  public int getEntityLifespan(ItemStack itemStack, Level world) {
    return Integer.MAX_VALUE;
  }

  private static void setMayFly(Player player, boolean mayFly) {

    if (player.getAbilities().mayfly == mayFly) {
      return;
    }

    player.getAbilities().mayfly = mayFly;
    player.onUpdateAbilities();
  }

  public boolean hasCustomEntity(ItemStack stack) {
    return true;
  }

  private static boolean requiresCurios() {
    return isCuriosInstalled() && ModConfigFile.looseRequiresCurios.get();
  }

  private static boolean isCuriosInstalled() {
    return ModList.get().isLoaded("curios");
  }

  private static boolean isCuriosEquipped(Player player, Item item) {
    return CuriosApi.getCuriosHelper().findFirstCurio(player, item).isPresent();
  }

  private static boolean hasItem(Player player, Item item) {
    if (isCuriosInstalled()) {
      if (isCuriosEquipped(player, item)) {
        return true;
      }

      // if requireCurios is false, we'll check the main inventory
      if (ModConfigFile.looseRequiresCurios.get()) {
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

  @Override
  @OnlyIn(Dist.CLIENT)
  public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
    super.appendHoverText(stack, worldIn, tooltip, flagIn);

    Player player = Minecraft.getInstance().player;
    if (player != null) {
      if (requiresCurios() && !isCuriosEquipped(player, ModItems.MAGIC_FEATHER.get())) {
        tooltip.add(
          Component.translatable(getDescriptionId(stack) + ".tooltip.requires_curios")
            .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
        );
      }

      tooltip.add(
              Component.translatable(getDescriptionId(stack) + ".tooltip.description")
          .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
      );
    }
  }

  public Entity createEntity(Level world, Entity entity, ItemStack itemstack) {
    entity.setInvulnerable(true);

    return null;
  }

  public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    if (event.side != LogicalSide.SERVER) {
      return;
    }

    Player player = event.player;

    MagicFeatherData data = ItemMagicFeather.GLOBAL_PLAYER_DATA.get(player);
    // if the player instance changes, we have to rebuild this.
    if (data == null || data.player != player) {
      data = new MagicFeatherData(player);
      ItemMagicFeather.GLOBAL_PLAYER_DATA.put(player, data);
    }

    data.onTick();
  }

  private static class MagicFeatherData {
    private final Player player;
    private boolean isSoftLanding = false;
    private boolean wasGrantedFlight = false;
    private boolean isSlowFalling = false;

    private int checkTick = 0;
    private boolean beaconInRangeCache;

    public MagicFeatherData(Player player) {
      this.player = player;
      this.beaconInRangeCache = player.getAbilities().mayfly;
    }

    public void onTick() {
      if (player.isSpectator()) {
        return;
      }

      boolean hasItem = hasItem(player, ModItems.MAGIC_FEATHER.get());
      boolean mayFly = player.isCreative() || (hasItem && checkBeaconInRange(player));

      if (mayFly) {
        setMayFly(player, true);
        isSoftLanding = false;
      } else {
        // we only remove the fly ability if we are the one who granted it.
        if (wasGrantedFlight) {
          isSoftLanding = true;
        }
      }

      if (isSoftLanding) {
        if (this.softLand()) {
          isSoftLanding = false;
        }
      }

      wasGrantedFlight = mayFly;
    }

    private boolean softLand() {
      if (ModConfigFile.fallStyle.get() == FallStyle.SLOW_FALL) {
        return this.slowFall();
      } else {
        return this.negateFallDamage();
      }
    }

    private boolean slowFall() {
      // SOFT LANDING:
      // on item removal, we disable flying until the player hits the ground
      // and only then do we remove the creative flight ability

      Abilities abilities = player.getAbilities();
      if (abilities.flying) {
        this.isSlowFalling = true;
        abilities.flying = false;
      }

      abilities.mayfly = false;
      player.onUpdateAbilities();

      boolean isPlayerOnGround = player.isOnGround() && player.fallDistance < 1F;
      if (isPlayerOnGround) {
        this.isSlowFalling = false;
      } else if (this.isSlowFalling) {
        if (checkTick++ % 5 != 0) {
          player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 0, false, false));
        }
      }

      return isPlayerOnGround;
    }

    private boolean negateFallDamage() {
      boolean isPlayerOnGround = player.isOnGround() && player.fallDistance < 1F;

      if (isPlayerOnGround) {
        setMayFly(player, false);

        // softland complete
        return true;
      } else {
        if (player.getAbilities().flying) {
          player.getAbilities().flying = false;
          player.onUpdateAbilities();
        }

        // softland in progress
        return false;
      }
    }

    private boolean checkBeaconInRange(Player player) {

      if (checkTick++ % 20 != 0) {
        return beaconInRangeCache;
      }

      beaconInRangeCache = BeaconRangeCalculator.isInBeaconRange(player);

      return beaconInRangeCache;
    }
  }
}
