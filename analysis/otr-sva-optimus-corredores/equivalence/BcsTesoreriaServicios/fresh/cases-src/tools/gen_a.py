# Generates the FRESH family-A cases (TES-A-Fnn_*) in the scratch copy by transforming development cases (never edits them).
import json,glob,os,re,shutil,copy
C='X:/tf/modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/test/resources/tesoreria-a/cases'
def base(p): return glob.glob(C+'/'+p+'_*')[0]
def mk(name,basep,fn_case=None,seed=None,keep_seed=False):
    b=base(basep); d=C+'/'+name
    if os.path.exists(d): shutil.rmtree(d)
    os.makedirs(d)
    c=json.load(open(b+'/case.json',encoding='utf-8')); c.pop('ref',None)
    if fn_case: fn_case(c)
    c['ref']='FRESH input (independent verifier), derived from '+os.path.basename(b)
    json.dump(c,open(d+'/case.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)
    s=''
    if keep_seed and os.path.exists(b+'/seed.sql'): s=open(b+'/seed.sql',encoding='utf-8').read()
    if callable(seed): s=seed(open(b+'/seed.sql',encoding='utf-8').read() if os.path.exists(b+'/seed.sql') else '')
    elif seed: s=seed
    if s.strip(): open(d+'/seed.sql','w',encoding='utf-8').write(s)

# ---------- F01 ValidarTransferenciaDVP: RCP, blank (spaces) clave, ANU (break), a 4th nomina never reached
NT='"BSCL.Tesoreria.BD::TES.NominaTransferencia" ("IdNominaTransferencia","IdEstado","Monto") VALUES (%d,\'%s\',%s);\n'
ND='"BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" ("IdNominaTransferencia","IdComprobanteFormaDePago","IdEstado") VALUES (%d,%d,\'%s\');\n'
FP='"BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" ("IdComprobanteFormaDePago","IdComprobante","ClaveDCV","IdMedioDePago") VALUES (%d,%d,\'%s\',\'TDP\');\n'
CD='"BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobante","IdTipoOperacion") VALUES (%d,\'VTA\');\n'
CO='"BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante","IdPersona") VALUES (%d,%d);\n'
def nomina(i,est,clave):
    return ''.join('INSERT INTO '+x for x in (NT%(i,est,'0.01'),ND%(i,i*10,est),FP%(i*10,i*100,clave),CD%(i*100),CO%(i*100,i*100)))+\
      'INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (%d,900);\n'%(i*100)
def f01(c):
    c['entrada']['p_NominaTransferencia']=[{'IdNominaTransferencia':i,'IdMedioDePago':'TDP','Monto':-0.01} for i in (21,22,23,24)]
mk('TES-A-F01_dvp-RCP-clave-en-blanco-ANU-corta-y-cuarta-nomina','TES-A-09',f01,
   seed=nomina(21,'RCP','CLAVE-X')+nomina(22,'ING','   ')+nomina(23,'ANU','CLAVE-Y')+nomina(24,'ING','CLAVE-Z'))
# ---------- F02 CrearIngresoEgreso: three movements in unusual order, extremes, leap-day liquidation
def mov(ref,tipo,con,monto,fl,**kw):
    d={"NumeroReferencia":ref,"IdTipoMovimiento":tipo,"IdTipoPersona":"CLI","IdPersona":101,"IdPortafolio":8001,"IdMoneda":"CLP","Monto":monto,"FechaLiquidacion":fl,"IdConcepto":con,
       "IdTipoPersonaTitularPago":"CLI","IdPersonaTitularPago":101,"IdPortafolioTitularPago":8001,"IdCaja":0,"Glosa":"\u00d1and\u00fa 'q' ; --","FechaOperacion":"2025-03-31","TitularPago":"S","MueveCuentaInversion":"S"}
    d.update(kw); return d
def fp(ref,n,monto,**kw):
    d={"NumeroReferencia":ref,"NumeroReferenciaFormaDePago":n,"IdMedioDePago":"TRA","IdModalidadDePago":"MODDEP","IdMoneda":"CLP","Monto":monto,"IdCuentaBancariaCorredora":301,"IdCuentaBancariaSocio":401,"IdTarjetaBancariaCorredora":0,"IdTarjetaBancariaSocio":0}
    d.update(kw); return d
def f02(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_IngresoEgresoGenerar']=[mov(7,'MOVEGR','VTARV0',0.01,'2025-03-31'),mov(3,'MOVING','FCOT00',1234567890.12,'2024-02-29'),mov(5,'MOVEGR','VTARV0',99999999999.99,'2025-04-05')]
    e['p_FormaDePago']=[fp(5,1,99999999999.99),fp(3,2,634567890.06),fp(7,3,0.01),fp(3,4,600000000.06)]
mk('TES-A-F02_cie-tres-movimientos-desordenados-extremos-y-29-feb','TES-A-27',f02)
# ---------- F03 CrearIngresoEgreso malformed: blank (spaces) currency / empty concept / blank movement type
def f03(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_IngresoEgresoGenerar']=[mov(1,'MOVING','FCOT00',10,'2025-03-31',IdMoneda='  '),mov(2,'MOVEGR','',10,'2025-03-31'),mov(3,' ','FCOT00',10,'2025-03-31')]
    e['p_FormaDePago']=[fp(1,1,10),fp(2,2,10),fp(3,3,10)]
mk('TES-A-F03_cie-malformado-moneda-blanca-concepto-vacio-tipo-blanco','TES-A-27',f03)
# ---------- F04 CrearTraspaso: pairs out of order, 0.01 and 12-digit
def tr(ref,tipo,per,port,con,monto):
    return mov(ref,tipo,con,monto,'2025-03-31',IdPersona=per,IdPortafolio=port,IdPersonaTitularPago=per,IdPortafolioTitularPago=port)
def f04(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_IngresoEgresoGenerar']=[tr(-3,'MOVEGR',101,8001,'VTARV0',0.01),tr(2,'MOVING',106,8006,'FCOT00',123456789012.34),tr(3,'MOVING',106,8006,'FCOT00',0.01),tr(-2,'MOVEGR',101,8001,'VTARV0',123456789012.34)]
    e['p_FormaDePago']=[fp(-2,1,123456789012.34),fp(3,2,0.01),fp(2,3,123456789012.34),fp(-3,4,0.01)]
mk('TES-A-F04_ctr-dos-pares-desordenados-0-01-y-12-cifras','TES-A-747',f04)
# ---------- F05 SaldoPendienteDeLiquidar: month end, negative amount, sub-cent, window crossing Feb->Mar
def f05(c):
    e=c['entrada']; e['p_FechaDesde']='2025-02-27'; e['p_FechaHasta']='2025-03-01'
def s05(s):
    return s.replace("'CLP',500,'2025-04-02'","'CLP',-250.5,'2025-02-28'").replace("'CLP',900,'2025-04-02'","'CLP',0.004,'2025-03-01'")
mk('TES-A-F05_spl-fin-de-febrero-monto-negativo-y-sub-centavo','TES-A-713',f05,seed=s05)
# ---------- F06 ConsultarComprobantesWS: filters USD/MOVEGR/document, range starting on the boundary day, third comprobante of 0.01
def s06(s):
    out=s
    for l in s.splitlines(True):
        if '6102' in l or '61021' in l or '61022' in l:
            out+=l.replace('61021','61031').replace('61022','61032').replace('6102','6103').replace("'2025-04-15'","'2025-04-10'").replace(',10000,',',0.01,')
    return out
def f06(c):
    e=c['entrada']; e.update(FechaInLiquidacion='2025-04-10',FechaTerLiquidacion='2025-04-20',TipoMovimiento='MOVEGR',IdMoneda='USD',DocumentoIdentidad='66666666-6')
mk('TES-A-F06_ccw-filtros-usd-egreso-documento-limite-inferior-del-rango','TES-A-610',f06,seed=s06)
# ---------- F07 AnularRegistros: batch [already annulled, ING with forma, id -1], month end
def s07(s):
    s2=s.replace("5301","7101").replace("6301","7201")
    s3=s.replace("5301","7102").replace("6301","7202").replace("'ING','OPT',0)","'ANU','OPT',0)").replace("10000,'ING')","10000,'ANU')")
    return s2+s3
def f07(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_RegistrosAProcesar']=[{'Id':7102,'Entidad':'TES.IngresoEgreso'},{'Id':7101,'Entidad':'TES.IngresoEgreso'},{'Id':-1,'Entidad':'TES.IngresoEgreso'}]
mk('TES-A-F07_anr-lote-anulado-ingresado-y-id-negativo','TES-A-300',f07,seed=s07)
# ---------- F08 PreLiquidarComprobante: USD 99999999.99 split in two formas + an annulled comprobante
def s08(s):
    a=s.replace('1090','1190').replace("'CLP'","'USD'").replace('10000','99999999.99')
    out=[]
    for x in a.splitlines(True):
        if 'ComprobanteFormaDePago' in x:
            out.append(x.replace('99999999.99','49999999.99'))
            out.append(x.replace('99999999.99','50000000.00').replace('11901','11902'))
        else: out.append(x)
    b=s.replace('10900','11910').replace('10901','11911').replace('1090','1191').replace("'ING'","'ANU'")
    return ''.join(out)+b
def f08(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_ComprobanteLiquidar']=[{'IdComprobante':1191,'IdTipoMovimiento':'MOVING','AfectaCuentaInversion':'CLI'},{'IdComprobante':1190,'IdTipoMovimiento':'MOVING','AfectaCuentaInversion':'CLI'}]
mk('TES-A-F08_pre-usd-99999999-99-dos-formas-y-comprobante-anulado','TES-A-290',f08,seed=s08)
# ---------- F09 LiquidarComprobante: forma 9999.99 vs 10000 (one cent short) and an exact USD 0.01 comprobante
def s09(s):
    a=s.replace('10010','12010').replace('10011','12011').replace('1001','1201')
    a=a.replace("'CLP',10000,'ING',0,0,0","'CLP',9999.99,'ING',0,0,0")
    b=s.replace('10010','12020').replace('10011','12021').replace('1001','1202').replace("'CLP'","'USD'").replace('10000','0.01')
    return a+b
def f09(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_ComprobanteLiquidar']=[{'IdComprobante':1202,'IdTipoMovimiento':'MOVING','AfectaCuentaInversion':'CLI'},{'IdComprobante':1201,'IdTipoMovimiento':'MOVING','AfectaCuentaInversion':'CLI'}]
mk('TES-A-F09_liq-forma-un-centavo-menor-y-comprobante-usd-0-01','TES-A-100',f09,seed=s09)
print('ok')
