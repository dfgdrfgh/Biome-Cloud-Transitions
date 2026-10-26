"""Reproduce old WWOO popping, then traverse both real sets of client hooks with the release cloud JAR."""
from pathlib import Path
import os,shutil,subprocess,json,zipfile
ROOT=Path(__file__).resolve().parent.parent
JAVA=Path(os.environ['CLOUD_JAVA_HOME'])/'bin' if 'CLOUD_JAVA_HOME' in os.environ else Path(shutil.which('javac')).resolve().parent
JAR=ROOT/'biome-cloud-transitions-fabric-1.21.1-1.0.2.jar'
folder=ROOT/'test-legacy-wwoo';shutil.rmtree(folder,ignore_errors=True);(folder/'mods').mkdir(parents=True)
# The renderer suite has downloaded the exact platform client libraries from Mojang.
client_libraries=[str(p.resolve()) for p in (ROOT/'test-runtime/libraries').rglob('*.jar')]
cp=os.pathsep.join([str(ROOT/'minecraft-client-named.jar'),(ROOT/'test-runtime/launch-classpath.txt').read_text(),*client_libraries])
classes=folder/'classes';classes.mkdir()
sources=[str(p) for p in (ROOT/'tests/fixtures/wwoo6').rglob('*.java')]
sources.append(str(ROOT/'tests/dev/biomecloudtransitions/LegacyWwooCloudProbe.java'))
subprocess.run([str(JAVA/'javac'),'-proc:none','--release','21','-cp',cp,'-d',str(classes),*sources],check=True)
raw=folder/'fixture-named.jar'
with zipfile.ZipFile(raw,'w',zipfile.ZIP_DEFLATED) as z:
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes))
mcp=(ROOT/'mapping-classpath.txt').read_text()+os.pathsep+str(ROOT)
official=folder/'fixture-official.jar';inter=folder/'fixture-intermediary.jar'
for a,b,m,src,dst,mc in [
    (raw,official,'client-mojmap.tiny','named','official',ROOT/'minecraft-client-named.jar'),
    (official,inter,'intermediary.tiny','official','intermediary',ROOT/'toolchain/client-1.21.1.jar')]:
    subprocess.run([str(JAVA/'java'),'-cp',mcp,'MappingTool','remap',str(a),str(b),str(ROOT/m),src,dst,str(mc)],check=True)
with zipfile.ZipFile(inter) as z:
    entries={n:z.read(n) for n in z.namelist() if n.endswith('.class')}
with zipfile.ZipFile(JAR) as z:attributes=z.read('data/biome_cloud_transitions/biome_attributes.json')
with zipfile.ZipFile(folder/'mods/legacy-wwoo.jar','w',zipfile.ZIP_DEFLATED) as z:
    for n,v in entries.items():
        if n.startswith('dev/biomecloudtransitions/testing/legacy/'):z.writestr(n,v)
    z.writestr('data/wwoocontent/biome_attributes.json',attributes)
    z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'wwoo','version':'3.0.1+backport.1.21.1.6',
        'environment':'client','mixins':['legacy-wwoo.mixins.json']}))
    z.writestr('legacy-wwoo.mixins.json',json.dumps({'required':True,'package':'dev.biomecloudtransitions.testing.legacy.mixin',
        'compatibilityLevel':'JAVA_21','client':['CloudColorMixin','CloudRendererAccess','CloudRendererMixin'],'injectors':{'defaultRequire':1}}))
with zipfile.ZipFile(folder/'mods/legacy-probe.jar','w',zipfile.ZIP_DEFLATED) as z:
    for n,v in entries.items():
        if n.startswith('dev/biomecloudtransitions/LegacyWwooCloudProbe'):z.writestr(n,v)
    z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'legacy_cloud_probe','version':'1','environment':'client',
        'entrypoints':{'preLaunch':['dev.biomecloudtransitions.LegacyWwooCloudProbe']}}))
launch_cp=os.pathsep.join([(ROOT/'test-runtime/launch-classpath.txt').read_text(),str(ROOT/'toolchain/lib/intermediary.jar'),*client_libraries])
command=[str(JAVA/'java'),'-Xmx2G','-Dmixin.debug.export=true','-Dfabric.gameJarPath='+str(ROOT/'toolchain/client-1.21.1.jar'),
    '-cp',launch_cp,'net.fabricmc.loader.impl.launch.knot.KnotClient']
for standalone,legacy,marker in [(False,True,'LEGACY_WWOO_POP_CONTROL_PASS'),(True,True,'LEGACY_WWOO_TRANSITION_PASS'),(True,False,'STANDALONE_TRANSITION_PASS')]:
    if standalone:shutil.copyfile(JAR,folder/'mods'/JAR.name)
    if not legacy:shutil.move(folder/'mods/legacy-wwoo.jar',folder/'legacy-wwoo-disabled.jar')
    result=subprocess.run(command,cwd=folder,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True,timeout=180)
    (folder/('standalone.log' if not legacy else 'combined.log' if standalone else 'control.log')).write_text(result.stdout)
    print(result.stdout[-18000:]);assert result.returncode==0 and marker in result.stdout
print('STANDALONE_CLOUD_PASS: core native-weather/vertex-alpha fix prevents source color/opacity jumps; complete JAR verified with and without external biome hooks')
