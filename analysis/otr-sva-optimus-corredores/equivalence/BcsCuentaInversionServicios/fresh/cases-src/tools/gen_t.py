import json,glob,os,re,shutil,copy,sys
C='ws/modernized/otr-sva-optimus-corredores/BcsCuentaInversionServicios/src/test/resources/transaccional/cases'
def base(p): return glob.glob(C+'/'+p+'*')[0]
def mk(name,basep,fn_case=None,fn_seed=None,seed_extra=''):
    b=base(basep); d=C+'/'+name
    if os.path.exists(d): shutil.rmtree(d)
    os.makedirs(d)
    c=json.load(open(b+'/case.json',encoding='utf-8'))
    if fn_case: fn_case(c)
    json.dump(c,open(d+'/case.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)
    s=open(b+'/seed.sql',encoding='utf-8').read() if os.path.exists(b+'/seed.sql') else ''
    if fn_seed: s=fn_seed(s)
    s+=seed_extra
    if s.strip(): open(d+'/seed.sql','w',encoding='utf-8').write(s)
CAM='BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento'
COLS='"IdCargoAbonoMovimiento","IdCuentaInversion","IdTipoMovimiento","IdEstado","IdEmpresa","Monto","Saldo","SaldoPorFechaMovimiento","FechaSistema","FechaEfectivaMovimiento","FechaCertificacion","IdConcepto","IdCondicionDeLiquidacion","EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso","IdUsuario"'
def ins(i,cu,t,est,monto,saldo,fs,fe,fc,conc='AACFIR'):
    return "INSERT INTO \"%s\" (%s) VALUES (%d,%d,'%s','%s',1,%s,%s,%s,'%s','%s','%s','%s','T0','N',0,97);\n"%(CAM,COLS,i,cu,t,est,monto,saldo,saldo,fs,fe,fc,conc)
# ---- F01 Crear batch of 3, unusual order, big/tiny/negative, DST day
def f01(c):
    e=c['entrada']; t=e['p_CargoAbonoAGenerar'][0]
    def one(n,port,mon,tipo,conc,monto,fecha):
        x=copy.deepcopy(t); x.update(NumeroCargoAbono=n,IdPortafolio=port,IdMoneda=mon,IdTipoMovimiento=tipo,IdConcepto=conc,Monto=monto,FechaCondicionDeLiquidacion=fecha); return x
    e['p_CargoAbonoAGenerar']=[one(30,8002,'CLP','MOVABN','AACFIR',-0.01,'2025-04-01'),
        one(10,8001,'USD','MOVCAR','CCACFI',999999999999.99,'2025-04-01'),
        one(20,8001,'CLP','MOVABN','AACFIR',0.01,'2025-04-05')]
mk('CTA-F-01_crear-lote-desordenado-extremos','CTA-A-48_',f01)
def f02(c):
    t=c['entrada']['p_CargoAbonoAGenerar'][0]
    t.update(Monto=0.0000000000000001,Descripcion="Ñandú 'x' ; -- \"q\"",IdConcepto='AACFIR')
    y=copy.deepcopy(t); y.update(NumeroCargoAbono=2,Monto=1234567.891234567,IdCondicionDeLiquidacion='T3',FechaCondicionDeLiquidacion='2025-04-04',IdPortafolio=8001,IdMoneda='USD')
    c['entrada']['p_CargoAbonoAGenerar'].append(y)
mk('CTA-F-02_crear-monto-1e-16-y-17-cifras-T3-viernes','CTA-A-48_',f02)
def f03(c):
    t=c['entrada']['p_CargoAbonoAGenerar'][0]
    t.update(IdMoneda='',Monto=10)   # malformed: empty currency where a code is expected
    y=copy.deepcopy(t); y.update(NumeroCargoAbono=2,IdMoneda='CLP',FechaCondicionDeLiquidacion='',Monto=5) # malformed: empty string where a date is expected
    c['entrada']['p_CargoAbonoAGenerar'].append(y)
mk('CTA-F-03_crear-malformado-moneda-vacia-y-fecha-vacia','CTA-A-48_',f03)
# ---- Certificar
def f04(c):
    c['entrada']['p_FechaMovimiento']='2025-03-31'
    c['entrada']['p_CargosAbonosACertificar']=[{'IdCargoAbonoMovimiento':4002},{'IdCargoAbonoMovimiento':4001}]
mk('CTA-F-04_certificar-dos-ids-descendentes-usd-extremos','CTA-A-13_',f04,lambda s: '',
 seed_extra=ins(4001,7002,'MOVCAR','PEN','99999999999.99',0,'2025-03-31','2025-02-28','2025-03-31 08:00:00')+ins(4002,7002,'MOVABN','PEN','0.01',0,'2025-03-31','2025-04-05','2025-03-31 08:00:00'))
def f05(c):
    c['entrada']['p_CargosAbonosACertificar']=[{'IdCargoAbonoMovimiento':2201}]
    c['colaboradores']['saldoFlujoCaja'][0]['Saldo']=999.99
mk('CTA-F-05_certificar-flujo-caja-un-centavo-bajo-el-cargo','CTA-A-22_',f05)
# ---- Anular
def f06(c):
    c['entrada']['p_CargoAbonoAAnular']=[{'IdCargoAbonoMovimiento':6002,'GlosaAnulacion':"Ñandú 'q' ; <b>&amp;"},{'IdCargoAbonoMovimiento':6001,'GlosaAnulacion':''}]
    c['entrada']['p_FechaMovimiento']='2025-03-31'
def ins2(i,cu,t,monto,saldo):
    return "INSERT INTO \"%s\" (%s,\"IdEvento\") VALUES (%d,%d,'%s','CRT',1,%s,%s,%s,'2025-03-31','2025-03-31','2025-03-31 09:00:00','AACFIR','T0','N',0,97,0);\n"%(CAM,COLS,i,cu,t,monto,saldo,saldo)
mk('CTA-F-06_anular-usd-abono-0-01-y-cargo-grande-glosa-unicode','CTA-A-33_',f06,lambda s:'',
 seed_extra=ins2(6001,7002,'MOVABN','0.01','0.01')+ins2(6002,7002,'MOVCAR','12345.6789','0'))
# ---- ConsultarSaldo
def f07(c):
    e=c['entrada']; e['p_FechaDesde']='2025-03-30'; e['p_FechaHasta']='2025-04-05'; e['p_IdPortafolio']=8001
rows=[(9701,7001,'MOVABN','100.005','2025-03-30'),(9702,7001,'MOVCAR','5.5','2025-03-31'),(9703,7002,'MOVABN','0.004','2025-04-05'),(9704,7002,'MOVCAR','-3.25','2025-04-02'),(9705,7001,'MOVABN','0.99','2025-04-06')]
mk('CTA-F-07_saldo-rango-30mar-5abr-centesimas-y-negativo','CTA-A-101_',f07,lambda s:'',
 seed_extra=''.join(ins(i,cu,t,'CRT',m,m,f,f,f+' 09:00:00') for i,cu,t,m,f in rows))
def f08(c):
    e=c['entrada']; e['p_FechaDesde']='2025-03-31'; e['p_FechaHasta']='2025-03-31'; e['p_IdPortafolio']=8002; e['p_IdCliente']=None
mk('CTA-F-08_saldo-un-dia-fin-de-mes-portafolio-propio-8002','CTA-A-101_',f08,lambda s:'',
 seed_extra=ins(9801,7003,'MOVABN','CRT','0.5','0.5','2025-03-31','2025-03-31','2025-03-31 09:00:00'))
# ---- Movimiento
def f09(c):
    k=c['entrada']['p_Consulta']; k.reverse()
    k[0].update(SaldoInicial=-0.5,FechaDesde='2025-03-31',FechaHasta='2025-03-31')
    k[1].update(IdMoneda='USD',SaldoInicial=0,FechaDesde='2025-03-31',FechaHasta='2025-04-01')
mk('CTA-F-09_movimiento-consultas-invertidas-saldo-inicial-negativo-fin-de-mes','CTA-A-166_',f09)
def f10(c):
    k=c['entrada']['p_Consulta'][0]; k.update(IdEstado='PEN',SaldoPorFechaEfectiva='S',FechaDesde='2025-03-29',FechaHasta='2025-04-02',SaldoInicial=-100.55,IdMoneda='CLP')
mk('CTA-F-10_movimiento-solo-pendientes-por-fecha-efectiva','CTA-A-159_',f10)
# ---- Cuadratura
def f11(c): c['entrada']['p_FechaInicio']='2025-04-02'
mk('CTA-F-11_cuadratura-proceso-fecha-solo-un-movimiento','CTA-A-147_',f11)
def s12(s):
    return s.replace("'[{\"IdCargoAbonoMovimiento\":1}]'","'[{\"IdCargoAbonoMovimiento\":99999999},{\"IdCargoAbonoMovimiento\":0,\"Nota\":\"Ñandú\"}]'").replace("'2025-03-31', 3, 1,","'2025-02-28', 0, 7,")
mk('CTA-F-12_cuadratura-consulta-detalle-json-unicode','CTA-A-153_',lambda c:c['entrada'].update(p_FechaInicio='2025-02-28'),s12)
# ---- WS crear
def f13(c):
    t=c['entrada']['p_CargoAbonoAGenerar'][0]; t.update(Monto=0.01,IdMoneda='USD',Descripcion='')
    c['entrada']['p_SimularOperacion']='N'
mk('CTA-F-13_ws-crear-real-N-usd-0-01','CTA-A-208_',f13)
