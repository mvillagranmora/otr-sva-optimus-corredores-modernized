import os,re,json,glob,hashlib
A='C:/Users/m.villagran.mora/Downloads/develop/nuam-modernizado/analysis/otr-sva-optimus-corredores/equivalence/BcsTesoreriaServicios'
dev=set()
for side in ('legacy','new'):
    for fam in ('familia_b','tesoreria-a'):
        for f in glob.glob(A+'/out/%s/%s/*.txt'%(side,fam)):
            dev.add(hashlib.sha1(open(f,'rb').read()).hexdigest())
print('dev texts',len(dev))
EXCL={}   # input id -> reason (whole input left out)
rows=[]; skipped=[]
def handle(fam,out):
    for f in sorted(glob.glob(out+'/legacy/'+fam+'/*.txt')):
        base=os.path.basename(f)[:-4]
        case,part=base.split('__',1)
        m=re.match(r'TES-([AB])-F(\d+)_(.*)',case)
        i='F'+m.group(1)+m.group(2); slug=m.group(3)
        if part.startswith('tabla_'):
            part='tablas_'+re.sub(r'^BSCL\.[A-Za-z]+\.BD(\.Vistas)?_','',part[6:])
        nf=out+'/new/'+fam+'/'+os.path.basename(f)
        rows.append((i,slug,part,open(f,'rb').read(),open(nf,'rb').read()))
handle('tesoreria-a','X:/dumpA'); handle('familia_b','X:/dumpB')
cases=[]; used={}
for i,slug,part,lt,nt in rows:
    if i in EXCL: continue
    txt=lt.decode('utf-8').strip()
    if txt in ('[]','{}','null','','{ }'): skipped.append((i,part,'trivial: '+txt)); continue
    h=hashlib.sha1(lt).hexdigest()
    if h in dev: skipped.append((i,part,'identical to a development output')); continue
    for side,data in (('legacy',lt),('new',nt)):
        p='%s/fresh/%s/%s/%s.txt'%(A,side,i,part)
        os.makedirs(os.path.dirname(p),exist_ok=True); open(p,'wb').write(data)
    cases.append({'id':'%s-%s'%(i,part),'input':i,'title':slug+' ['+part+']','legacy':'fresh/legacy/%s/%s.txt'%(i,part),'new':'fresh/new/%s/%s.txt'%(i,part),'mask':[]})
    used[i]=used.get(i,0)+1
print(len(cases),'cases;',len(used),'inputs; skipped',len(skipped))
for s in skipped: print('  skipped',s)
print(used)
doc={'system':'otr-sva-optimus-corredores','module':'BcsTesoreriaServicios',
 'legacy':{'label':'unmodified legacy cl.bcs.tesoreria classes executed through the existing oracles (tesoreria-a / tesoreria-b record.sh + OracleRunner, shims, H2 MODE=LEGACY, JVM zone UTC) on FRESH inputs TES-A-Fnn / TES-B-Fnn (verifier); canonical text of expected.json parts','command':'record.sh TES-A-F (family A) and record.sh TES-B-F (family B) in a scratch copy of the module (see fresh/cases-src/tools)'},
 'new':{'label':'modernized BcsTesoreriaServicios src/main, JDK 25, same H2 seeds and scripted collaborators, driven by FreshATest / FreshBTest (scratch copy) with the same wiring as the development tests; every list compared strictly (no unordered lists)','command':'mvn -o -f pom.xml -pl :tesoreria-service test -Dtest=FreshATest -Dequivalence.out=<dir>; same with -Dtest=FreshBTest (JVM -Duser.timezone=UTC from the module pom)'},
 'note':'Fresh (non-development) inputs. No byte masks are declared here: the only masking is what the harness applies to both sides inside the canonical text (file timestamps, ${RUTA_BASE}, identity hashes), none of which were needed to reach equality. Each case compares one output file; a comparison point the dev tests also compare (salida/tablas/archivos/colaboradores).',
 'cases':cases}
json.dump(doc,open(A+'/fresh-cases.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)
