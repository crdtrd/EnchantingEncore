package com.drtdrc.enchantingencore.mixin;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(EntityAttributes.class)
public class EntityAttributesMixin {

    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/attribute/EntityAttributes;register(Ljava/lang/String;Lnet/minecraft/entity/attribute/EntityAttribute;)Lnet/minecraft/registry/entry/RegistryEntry;"
            ),
            index = 1,
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=water_movement_efficiency")
            )
    )
    private static EntityAttribute replaceWaterMovementAttr(EntityAttribute original) {
        return new ClampedEntityAttribute(
                "attribute.name.water_movement_efficiency",
                0.0, 0.0, 2.0
        ).setTracked(true);
    }
}

