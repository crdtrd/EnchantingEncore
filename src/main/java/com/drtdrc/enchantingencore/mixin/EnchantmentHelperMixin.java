package com.drtdrc.enchantingencore.mixin;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EnchantableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
    @Inject(
            method = "calculateRequiredExperienceLevel",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private static void onCalculateExperienceLevel(Random random, int slotIndex, int bookshelfCount, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        EnchantableComponent enchantableComponent = stack.get(DataComponentTypes.ENCHANTABLE);
        if (enchantableComponent == null) {
            cir.setReturnValue(0);
        } else {
            if (bookshelfCount > 50) {
                bookshelfCount = 50;
            }

            int i = random.nextInt(4) + 1 + (bookshelfCount >> 1);
            if (slotIndex == 0) {
                cir.setReturnValue( Math.max(i / 3, 1) );
            } else {
                cir.setReturnValue( slotIndex == 1 ? i * 2 / 3 + 1 : bookshelfCount );
            }
        }
    }
}
