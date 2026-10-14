package com.zachminecraft.biomeclouds.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Let the standalone renderer own biome tint/opacity when older WWOO builds are installed. */
@Pseudo
@Mixin(targets="com.zachminecraft.wwoocontent.CloudAttributes",remap=false)
public abstract class LegacyWwooCloudAttributesMixin {
    @Inject(method="color()I",at=@At("HEAD"),cancellable=true,require=0,remap=false)
    private static void biomeclouds$neutralLegacyCloudColor(CallbackInfoReturnable<Integer> cir) {
        // The old helper feeds both the native cloud RGB and baked vertex alpha.
        // White with vanilla's 0.8 alpha removes both biome jumps while retaining weather/time shading.
        // The standalone CloudAttributes class has a separate namespace and keeps its full palette.
        cir.setReturnValue(0xccffffff);
    }
}
