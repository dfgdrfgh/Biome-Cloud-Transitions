"""Validate release palettes; native sky-blend logic is checked in the Fabric client renderer test."""
from pathlib import Path
import zipfile,json
ROOT=Path(__file__).resolve().parent.parent
JAR=ROOT/'biome-cloud-transitions-fabric-1.21.1-1.0.0.jar'
with zipfile.ZipFile(JAR) as z:
    assert 'com/zachminecraft/biomeclouds/CloudBlend.class' in z.namelist()
    assert not any('CloudBlendCheck' in n for n in z.namelist())
    # Preserve encoded biome data; rendering uses RGB without animating its opacity.
    attributes=json.loads(z.read('data/biome_cloud_transitions/biome_attributes.json'))
    assert attributes['minecraft:pale_garden']['attributes']['minecraft:visual/cloud_color']=='#66828282'
    assert attributes['minecraft:desert']['attributes']['minecraft:visual/cloud_color']=='#11ffffff'
print('CLOUD_ATTRIBUTES_PASS: encoded palettes retained; native spatial checks run in tests/cloud_rendering.py')
