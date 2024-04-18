package com.seamus.splatdata.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.splatcraft.forge.tileentities.container.PlayerInventoryContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class ArmorSlotMixin {

    @Shadow public abstract ItemStack getItem();

    @Inject(method = "mayPickup", cancellable = true, at = @At("HEAD"))
    public void mayPickUp(Player player, CallbackInfoReturnable<Boolean> ci) {
        if (player.level.isClientSide)
            return;

        if (getItem().getOrCreateTag().getBoolean("splatdata.forced") && ((ServerPlayer)player).gameMode.getGameModeForPlayer() != GameType.CREATIVE){
            ci.setReturnValue(false);
        }
    }
}
