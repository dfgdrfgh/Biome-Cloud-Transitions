# Biome Cloud Transitions — Forge 1.20.1 port

Cloud RGB blending with vanilla, Embeddium and Cirrus support. Java 17 and Forge 47.4.10 or newer for Minecraft 1.20.1.

This branch ports the Fabric 1.21.1 implementation. Spatial Gaussian sampling, encoded biome palettes, native weather/time shading and draw-time RGB tint are preserved. It is independent of WWOO. Optional Cirrus hooks match the Forge Cirrus port; its shaders keep their native lighting and Fancy opacity, with the Fast opacity fix applied only to Cirrus cloud shader resources.

Forge 1.20.1 Embeddium uses vanilla cloud rendering; the incompatible Fabric Sodium 0.8 renderer mixin is excluded. The source Fabric branch is unchanged.

Build with `bash gradlew test jarJar`, then run `python3 tests/forge_release.py`. Install the JAR without the `-slim` suffix from `build/libs`. MixinExtras is bundled. GitHub Actions uploads this installable archive.

Validation status is recorded in the pull request. The JUnit tests compare the sampler against Minecraft 1.20.1's actual CubicSampler across palette pairs, movement/reversal, alpha independence and boundaries, and check shader patch behavior. Compilation and these tests do not establish in-game renderer or complete-modpack compatibility. The supplied Modded Omelet ZIP is a resource pack; a complete playable Forge pack was not supplied.

See NOTICE.txt for palette attribution and terms.
