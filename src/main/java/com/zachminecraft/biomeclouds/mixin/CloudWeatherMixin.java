package com.zachminecraft.biomeclouds.mixin;

import com.zachminecraft.biomeclouds.CloudWeather;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Capture the native result before ordinary return hooks can replace it with a stepwise biome tint. */
@Mixin(value=ClientLevel.class,priority=2000)
public abstract class CloudWeatherMixin {
    @Inject(method="method_23785",at=@At("RETURN"),remap=false)
    private void biomeclouds$rememberWeather(float partialTick,CallbackInfoReturnable<Vec3> cir) {
        CloudWeather.remember((ClientLevel)(Object)this,cir.getReturnValue());
    }
}
