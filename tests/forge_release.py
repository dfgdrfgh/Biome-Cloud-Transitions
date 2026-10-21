"""Check the installable Forge archive, not the slim development JAR."""
from pathlib import Path
import json, zipfile
jar=Path(__file__).resolve().parents[1]/'build/libs/biome-cloud-transitions-forge-1.20.1-1.0.1-forge.1.jar'
with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    assert 'META-INF/mods.toml' in names
    assert 'fabric.mod.json' not in names
    assert 'dev/biomecloudtransitions/BiomeCloudTransitions.class' in names
    assert any(n.startswith('META-INF/jarjar/') and n.endswith('.jar') for n in names)
    config=json.loads(z.read('biome-cloud-transitions.mixins.json'))
    assert config['compatibilityLevel']=='JAVA_17'
    refmap=json.loads(z.read(config['refmap']))
    assert refmap['mappings'], 'Minecraft mixin targets must have production SRG mappings'
    for name in names:
        if name.endswith('.class'):
            assert int.from_bytes(z.read(name)[6:8], 'big')<=61, name
    palette=json.loads(z.read('data/biome_cloud_transitions/biome_attributes.json'))
    assert palette['minecraft:pale_garden']['attributes']['minecraft:visual/cloud_color']=='#66828282'
print('FORGE_RELEASE_PASS: Java17, Forge metadata, production refmap, bundled MixinExtras and biome palette')
