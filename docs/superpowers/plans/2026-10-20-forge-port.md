# Forge 1.20.1 port

Goal: preserve the existing biome RGB sampler, native weather shading, vanilla draw-time tint and optional Cirrus integration on Forge 47 / Java 17.

- [ ] Add ForgeGradle, Mixin remapping/refmap, client mod metadata, bundled MixinExtras, and an Actions jarJar artifact.
- [ ] Compile the unmodified source first to expose version and mapping differences.
- [ ] Port Minecraft mixin selectors from Fabric intermediary names to Mojang names with generated SRG refmap. Port Cirrus shader wrapping to Forge ShaderInstance registration. Preserve actual Gaussian sampler and palette.
- [ ] Run actual Minecraft sampler tests plus shader transformation tests. Check mixin targets against real Minecraft/Cirrus bytecode where available.
- [ ] Compile/reobfuscate and inspect the resulting JAR. Test Forge client launch when runtime/display available. Record modpack testing limits.

Existing Sodium 0.8 renderer targets are Fabric-specific; Forge 1.20.1 Embeddium uses vanilla cloud rendering. The standalone mod must load without Cirrus; optional mixins target only present third-party classes.
