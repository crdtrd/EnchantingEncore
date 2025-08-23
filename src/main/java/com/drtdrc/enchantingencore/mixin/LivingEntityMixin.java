package com.drtdrc.enchantingencore.mixin;

import com.drtdrc.enchantingencore.EnchantingEncore;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Unique
    private static final Identifier WATER_DS_BOOST_ID = Identifier.of(EnchantingEncore.MOD_ID, "water_ds_boost");

    @Inject(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"))
    private void applyWaterSpeedBoost(Vec3d input, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.getWorld().isClient()) return; // server-side only


        var inst = self.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (inst == null) return;

        // Remove prior tick’s modifier to avoid stacking
        inst.removeModifier(WATER_DS_BOOST_ID);

        if (self.isTouchingWater()) {
            // How much above vanilla are we? (vanilla max h = 1)
            double hRaw = self.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
            double bonus = Math.max(0.0, hRaw - 1.0); // 0 for DS<=3, >0 for DS4+

            if (bonus > 0.0) {
                double k = 0.70;
                inst.addTemporaryModifier(new net.minecraft.entity.attribute.EntityAttributeModifier(
                        WATER_DS_BOOST_ID,
                        k * bonus, // e.g., DS5 with hRaw≈1.67 → ~0.5 * k
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ));
            }
        }
    }
}
