// src/main/java/com/drtdrc/enchantingencore/mixin/EnchantmentScreenHandlerMixin.java
package com.drtdrc.enchantingencore.mixin;

import com.drtdrc.enchantingencore.BiasContext;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.*;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    @Shadow @Final private ScreenHandlerContext context;

    /**
     * Compute bias *before* offers get (re)generated.
     * Mapping note: the point you inject at can be any stable spot just before
     * the handler recomputes offers. If your Yarn differs, redirect just ahead
     * of whatever updates the offers.
     */
    @Inject(method = "onContentChanged", at = @At("HEAD"))
    private void prepareBias(Inventory inventory, CallbackInfo ci) {
        context.run(BiasContext::compute);
    }

    /** Clear it right after we’re done. */
    @Inject(method = "onContentChanged", at = @At("TAIL"))
    private void clearBias(Inventory inventory, CallbackInfo ci) {
        BiasContext.deactivate();
    }

    @Inject( method = "onButtonClick", at = @At("HEAD") )
    private void prepareBiasOnApply(net.minecraft.entity.player.PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        context.run(BiasContext::compute);
    }

    @Inject( method = "onButtonClick", at = @At("RETURN") )
    private void clearBiasOnApply(net.minecraft.entity.player.PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        BiasContext.deactivate();
    }
}
