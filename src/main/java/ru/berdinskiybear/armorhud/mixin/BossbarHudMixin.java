package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import java.util.ArrayList;
import java.util.List;

@Mixin(BossBarHud.class)
public class BossbarHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private final List<ItemStack> armorItems = new ArrayList<>(4);

    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    public int calculateOffset(int offset) {
        ArmorHudConfig config = this.getArmorHudConfig();
        if (config.isEnabled() && config.isPushBossbars()) {
            int add = 0;
            if (config.getAnchor() == ArmorHudConfig.Anchor.Top_Center) {
                int amount = 0;
                PlayerEntity playerEntity = this.getCameraPlayer();
                if (playerEntity != null) {
                    this.armorItems.clear();
                    for (ItemStack itemStack : playerEntity.getInventory().armor) {
                        if (!itemStack.isEmpty())
                            amount++;
                        if (!itemStack.isEmpty() || config.getSlotsShown() != ArmorHudConfig.SlotsShown.Show_Equipped)
                            this.armorItems.add(itemStack);
                    }

                    if (!(amount == 0 && config.getSlotsShown() != ArmorHudConfig.SlotsShown.Always_Show)) {
                        if (config.getOrientation() == ArmorHudConfig.Orientation.Vertical)
                            add += 20 * (amount - 1);
                        add += 22 + config.getOffsetY();
                    }
                }
            }
            return offset + Math.max(add, 0);
        } else
            return offset;
    }

    /**
     * This function determines which config is supposed to be current. Usually the loaded config is considered current
     * but if config screen is open then the preview config is used as current.
     *
     * @return Current config
     */
    @Unique
    private ArmorHudConfig getArmorHudConfig() {
        return this.client.currentScreen != null && this.client.currentScreen.getTitle() == ArmorHudMod.CONFIG_SCREEN_NAME ? ArmorHudMod.previewConfig : ArmorHudMod.getConfig();
    }

    @Unique
    private PlayerEntity getCameraPlayer() {
        return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
    }
}
