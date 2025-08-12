package com.drtdrc.enchantingencore.mixin;

import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
    // Need to change POWER_PROVIDER_OFFSETS to new offset values
    @Shadow @Final @Mutable
    public static List<BlockPos> POWER_PROVIDER_OFFSETS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void tweakPowerProviderOffsets(CallbackInfo ci) {
        POWER_PROVIDER_OFFSETS = BlockPos.stream(-4, -4, -4, 4, 4, 4)
                .filter(pos -> Math.abs(pos.getX()) == 4 || Math.abs(pos.getZ()) == 4)
                .map(BlockPos::toImmutable)
                .toList();
    }

//    // UNsure if this is needed
//    @Inject(
//            method = "canAccessPowerProvider",
//            at = @At(value = "HEAD"),
//            cancellable = true
//    )
//    public static void canAccessPowerProviderModified(World world, BlockPos tablePos, BlockPos providerOffset, CallbackInfoReturnable<Boolean> cir) {
//        boolean r = world.getBlockState(tablePos.add(providerOffset)).isIn(BlockTags.ENCHANTMENT_POWER_PROVIDER)
//                && world.getBlockState(tablePos.add(providerOffset.getX() / 2, providerOffset.getY(), providerOffset.getZ() / 2))
//                .isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
//    }
}
