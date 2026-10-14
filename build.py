"""Compile the standalone client mod with JDK 21 and remap to Fabric intermediary names."""
from pathlib import Path
import os, shutil, subprocess, zipfile

ROOT = Path(__file__).resolve().parent
JAVA = Path(os.environ['CLOUD_JAVA_HOME'])/'bin' if 'CLOUD_JAVA_HOME' in os.environ else Path(shutil.which('javac') or '/missing/javac').resolve().parent
BUILD = ROOT/'build'
shutil.rmtree(BUILD, ignore_errors=True)
BUILD.mkdir()
cp = (ROOT/'compile-classpath.txt').read_text().replace(str(ROOT/'minecraft-named.jar'), str(ROOT/'minecraft-client-named.jar'))
classes = BUILD/'classes'
subprocess.run([str(JAVA/'javac'), '-proc:none', '--release', '21', '-cp', cp, '-d', str(classes),
    *[str(p) for p in sorted((ROOT/'src/main/java').rglob('*.java'))]], check=True, cwd=ROOT)
named = BUILD/'cloud-named.jar'
with zipfile.ZipFile(named, 'w', zipfile.ZIP_DEFLATED) as z:
    for p in sorted(classes.rglob('*.class')):
        z.write(p, p.relative_to(classes))
mcp = (ROOT/'mapping-classpath.txt').read_text()+os.pathsep+str(ROOT)
official = BUILD/'cloud-official.jar'
intermediary = BUILD/'cloud-intermediary.jar'
for a,b,m,src,dst,mc in [
    (named,official,'client-mojmap.tiny','named','official',ROOT/'minecraft-client-named.jar'),
    (official,intermediary,'intermediary.tiny','official','intermediary',ROOT/'toolchain/client-1.21.1.jar')]:
    subprocess.run([str(JAVA/'java'), '-cp', mcp, 'MappingTool', 'remap', str(a), str(b), str(ROOT/m), src, dst, str(mc)], check=True, cwd=ROOT)
with zipfile.ZipFile(intermediary) as z:
    entries = {n:z.read(n) for n in z.namelist() if n.endswith('.class')}
for p in (ROOT/'src/main/resources').rglob('*'):
    if p.is_file(): entries[p.relative_to(ROOT/'src/main/resources').as_posix()] = p.read_bytes()
entries['NOTICE.txt'] = (ROOT/'NOTICE.txt').read_bytes()
out = ROOT/'biome-cloud-transitions-fabric-1.21.1-1.0.0.jar'
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
    for n,data in sorted(entries.items()):
        info=zipfile.ZipInfo(n,(1980,1,1,0,0,0));info.create_system=3;info.external_attr=0o100644<<16;info.compress_type=zipfile.ZIP_DEFLATED
        z.writestr(info,data,compresslevel=9)
print('Built '+str(out))
