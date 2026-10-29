import os,re,json,glob,shutil,hashlib
A='C:/Users/m.villagran.mora/Downloads/develop/nuam-modernizado/analysis/otr-sva-optimus-corredores/equivalence/BcsCuentaInversionServicios'
dev=set()
for side in ('legacy','new'):
    for fam in ('reportes','transaccional'):
        for f in glob.glob(A+'/out/%s/%s/*.txt'%(side,fam)):
            dev.add(hashlib.sha1(open(f,'rb').read()).hexdigest())
print('dev texts',len(dev))
def fid(name):
    n=int(re.match(r'CTA-F-(\d+)_',name).group(1)); return 'F%02d'%n
rows=[]; skipped=[]
def handle(fam,out):
    for f in sorted(glob.glob(out+'/legacy/'+fam+'/*.txt')):
        base=os.path.basename(f)[:-4]
        if fam=='transaccional':
            case,part=base.split('__',1)
            if part.startswith('tabla_'): part='tablas_'+part[6:]
            case=case
        else:
            m=re.match(r'(CTA-F-\d+_.*?)_(salida|tablas_modificadas|llamadasColaboradores|archivos_generados|pdfTextos)__canon$',base)
            case,part=m.group(1),{'salida':'salida','tablas_modificadas':'tablas','llamadasColaboradores':'colaboradores','archivos_generados':'archivos','pdfTextos':'pdfTextos'}[m.group(2)]
        n=f.replace('legacy','new',1) if False else out+'/new/'+fam+'/'+os.path.basename(f)
        lt=open(f,'rb').read(); nt=open(n,'rb').read()
        rows.append((fam,case,part,lt,nt))
handle('transaccional','outT5'); handle('reportes','outR5')
EXCL_INPUTS={'F03':'legacy throws DateTimeParseException (exception class only, no output)','F20':'legacy returns only the status error UI_REGISTRO_DISTINTO_VIG, no table change (status-only)','F26':'legacy throws ArithmeticException (/ by zero), no output'}
cases=[]; used={}
for fam,case,part,lt,nt in rows:
    i=fid(case)
    if i in EXCL_INPUTS: continue
    txt=lt.decode('utf-8').strip()
    if txt in ('[]','{}','null','','{ }'): skipped.append((i,part,'trivial: '+txt)); continue
    h=hashlib.sha1(lt).hexdigest()
    if h in dev: skipped.append((i,part,'identical to a development output')); continue
    for side,data in (('legacy',lt),('new',nt)):
        p='%s/fresh/%s/%s/%s.txt'%(A,side,i,part)
        
        if side=='legacy':
            assert open(p,'rb').read()==data, 'LEGACY CHANGED '+p
        else:
            open(p,'wb').write(data)
    cases.append({'id':'%s-%s'%(i,part),'input':i,'title':case[len('CTA-F-NN_'):] +' ['+part+']','legacy':'fresh/legacy/%s/%s.txt'%(i,part),'new':'fresh/new/%s/%s.txt'%(i,part),'mask':[]})
    used[i]=used.get(i,0)+1
print(len(cases),'cases;',len(used),'inputs; skipped',skipped)
print({k:v for k,v in EXCL_INPUTS.items()})
doc={'system':'otr-sva-optimus-corredores','module':'BcsCuentaInversionServicios',
 'legacy':{'label':'unmodified legacy cl.bcs.cuentaInversion classes executed through the existing oracle (record.sh/OracleRunner, shims, H2 MODE=LEGACY) on FRESH inputs CTA-F-NN (verifier); canonical text of expected.json parts','command':'record.sh run CTA-F (transaccional: JVM zone UTC; reportes: America/Santiago) in a scratch copy of the module'},
 'new':{'label':'modernized BcsCuentaInversionServicios src/main, JDK 25, same H2 seeds and fakes, driven by FreshTransaccionalTest / FreshReportesTest (scratch copy)','command':'mvn -o -pl :bcs-cuenta-inversion-servicios test-compile surefire:test@default-test -Dtest=FreshTransaccionalTest -Dequivalence.out=<dir>; surefire:test@reportes-zona-santiago -Dtest=FreshReportesTest -Dequivalence.out=<dir>'},
 'note':'Fresh (non-development) inputs. No byte masks are declared here: the only masking is the one the harness applies to both sides inside the canonical text (transaccional: yyyyMMdd_HHmmss -> <TS>; reportes: random IdTrabajo, today timestamps -> <NOW>/<HOY>/<AHORA>, output path -> ${OUT}).',
 'cases':cases}
old=json.load(open(A+'/fresh-cases.json',encoding='utf-8')); assert old==doc,'cases json would change'
