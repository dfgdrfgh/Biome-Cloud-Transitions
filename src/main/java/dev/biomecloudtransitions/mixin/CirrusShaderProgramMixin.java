package dev.biomecloudtransitions.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import dev.biomecloudtransitions.CloudOpacity;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Forge supplies namespaced ShaderInstance construction directly. */
@Mixin(ShaderInstance.class)
public abstract class CirrusShaderProgramMixin {
    @ModifyVariable(method="<init>(Lnet/minecraft/server/packs/resources/ResourceProvider;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/VertexFormat;)V",
        at=@At("HEAD"), argsOnly=true, ordinal=0, remap=false)
    private static ResourceProvider biomeclouds$shaderResources(ResourceProvider provider,
            ResourceProvider original, ResourceLocation name, VertexFormat format) {
        return name.getNamespace().equals("cirrus") && name.getPath().equals("cirrus_clouds")
            ? CloudOpacity.resources(provider) : provider;
    }
}
