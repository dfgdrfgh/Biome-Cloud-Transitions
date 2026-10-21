package dev.biomecloudtransitions.mixin;

import dev.biomecloudtransitions.CloudAttributes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.biomecloudtransitions.CloudBlend;
import dev.biomecloudtransitions.CloudWeather;
import net.minecraft.world.phys.Vec3;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=LevelRenderer.class,priority=2000)
public abstract class CloudRendererMixin {
    @Unique private CloudBlend.Color wwoo$frameColor;
    @Inject(method="renderClouds", at=@At("HEAD"))
    private void wwoo$sampleCloudColor(CallbackInfo ci) {
        // Both fancy-cloud passes use the same instant in the transition.
        CloudWeather.beginFrame();
        wwoo$frameColor = CloudAttributes.color();
    }

    @WrapOperation(method="renderClouds", at=@At(value="INVOKE",
        target="Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
        remap=false))
    private void wwoo$tintCloudDraw(VertexBuffer buffer, Matrix4f modelView, Matrix4f projection,
                                    ShaderInstance shader, Operation<Void> original) {
        // Snapshot the shared shader-color array into locals before changing it.
        // Tint the original operation directly, without a helper or capturing callback.
        float[] state = RenderSystem.getShaderColor();
        float red = state[0], green = state[1], blue = state[2], alpha = state[3];
        var color = wwoo$frameColor;
        RenderSystem.setShaderColor(red * (float)color.red(), green * (float)color.green(),
            blue * (float)color.blue(), alpha);
        try {
            original.call(buffer, modelView, projection, shader);
        } finally {
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
    }

    @ModifyExpressionValue(method="renderClouds",at=@At(value="INVOKE",
        target="Lnet/minecraft/client/multiplayer/ClientLevel;getCloudColor(F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 biomeclouds$nativeWeatherColor(Vec3 original) {
        return CloudWeather.original(original);
    }

    @ModifyArg(method="buildClouds",at=@At(value="INVOKE",
        target="Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),index=3)
    private float biomeclouds$nativeCloudAlpha(float original) {
        // Vanilla cloud geometry uses 0.8. Biome alpha must not be baked into rebuilt cloud buffers.
        return .8f;
    }
}


