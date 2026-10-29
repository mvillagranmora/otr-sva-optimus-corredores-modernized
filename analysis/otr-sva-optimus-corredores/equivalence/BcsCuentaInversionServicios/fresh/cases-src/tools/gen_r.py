import json,glob,os,re,shutil,copy
C='ws/modernized/otr-sva-optimus-corredores/BcsCuentaInversionServicios/src/test/resources/reportes/cases'
def base(p): return glob.glob(C+'/'+p+'*')[0]
def mk(name,basep,fn_case=None,seed=None,seed_extra=None):
    b=base(basep); d=C+'/'+name
    if os.path.exists(d): shutil.rmtree(d)
    os.makedirs(d)
    c=json.load(open(b+'/case.json',encoding='utf-8'))
    c['id']=name.split('_')[0]
    if fn_case: fn_case(c)
    json.dump(c,open(d+'/case.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)
    s=open(b+'/seed.sql',encoding='utf-8').read() if os.path.exists(b+'/seed.sql') else ''
    if seed is not None: s=seed
    if seed_extra: s+=seed_extra
    if s.strip(): open(d+'/seed.sql','w',encoding='utf-8').write(s)
IDX='INSERT INTO "BSCL.Plataforma.BD::MER.IndiceFinanciero" VALUES (\'%s\', DATE \'%s\', %s);\n'
# F14 IndiceRentabilidad: rounding boundary 201/200 = 1.005 (HALF_UP -> 1.01), negative -20 %, DST day inside the range, unusual order
def f14(c):
    c['nota']='FRESH'
    c['entrada']={'p_FechaDesde':'2025-04-04','p_FechaHasta':'2025-04-07','p_clasificacion':[{'IdArbolClasificacionActivo':1046},{'IdArbolClasificacionActivo':1045}]}
mk('CTA-F-14_indice-limite-redondeo-1005-y-caida-20-pct-dia-DST','CTA-B-175_',f14,
   seed_extra=IDX%('IPSA','2025-04-04','200')+IDX%('IPSA','2025-04-07','201')+IDX%('IGPA','2025-04-04','100')+IDX%('IGPA','2025-04-07','80'))
# F15 rango invertido (desde > hasta), id de clasificacion inexistente, clasificacion sin indice
def f15(c):
    c['entrada']={'p_FechaDesde':'2025-03-14','p_FechaHasta':'2025-03-10','p_clasificacion':[{'IdArbolClasificacionActivo':9999},{'IdArbolClasificacionActivo':1047},{'IdArbolClasificacionActivo':1045}]}
mk('CTA-F-15_indice-rango-invertido-e-id-inexistente','CTA-B-175_',f15)
# F16 Procesar masivo: un dia, extremo inferior; fila VIG con monto 0 en el borde
def f16(c):
    c['entrada']['p_Filtros'].update(FechaDesde='2025-02-10',FechaHasta='2025-02-11')
mk('CTA-F-16_procesar-masivo-rango-10-11-feb','CTA-B-109_',f16)
PC='INSERT INTO "BSCL.Custodia.BD::CUS.PatrimonioCustodia" ("IdPatrimonioCustodia","IdCliente","IdPortafolio","IdInstrumento","FechaPatrimonio","TipoFechaConsulta","MontoComision","IdEstadoComision","MontoPatrimonio") VALUES (%d, %d, %d, 3001, DATE \'%s\', \'LIQ\', %s, \'VIG\', %s);\n'
def f17(c):
    c['entrada']['p_Filtros'].update(FechaDesde='2025-02-28',FechaHasta='2025-02-28')
    c['entrada']['p_PortafoliosSeleccionados']=[{'IdPortafolio':2002},{'IdPortafolio':2001}]
    c['entrada']['p_EsProcesoMasivo']='N'
mk('CTA-F-17_procesar-seleccion-invertida-fin-de-mes-comision-negativa-y-centavo','CTA-B-110_',f17,
   seed_extra=PC%(8101,1001,2001,'2025-02-28','-10.50','100')+PC%(8102,1002,2002,'2025-02-28','0.01','1'))
# F18..F20 EditarCobroComisionRegistros
def f18(c): c['entrada'].update(p_IdPortafolio=2001,p_MontoComision=-0.01,p_Periodo=2,p_EstadoComision='VIG')
mk('CTA-F-18_editar-comision-negativa-un-centavo-periodo-2-crea-editado','CTA-B-32_',f18)
def f19(c): c['entrada'].update(p_IdPortafolio=2002,p_MontoComision=99999999999999.99,p_Periodo=2,p_EstadoComision='VIG')
mk('CTA-F-19_editar-existente-monto-maximo-estado-VIG','CTA-B-32_',f19)
def f20(c): c['entrada'].update(p_IdPortafolio=2001,p_MontoComision=0,p_Periodo=2,p_EstadoComision='vig')   # malformed: lower-case status code, zero amount
mk('CTA-F-20_editar-malformado-estado-en-minusculas-monto-cero','CTA-B-32_',f20)
# F21 Antiguedad: tramo boundaries 2,3,10,11,30,31,32 days before 2025-03-14, decimals, separa S
S='INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" VALUES (DATE \'%s\', 1001, 2001, \'CLP\', \'%s\', %s);\n'
rows=[('2025-03-12','G','-10.5'),('2025-03-12','I','-4.25'),('2025-03-11','G','-12'),('2025-03-11','I','-4.25'),
      ('2025-03-04','G','-20.75'),('2025-03-04','I','-5'),('2025-03-03','G','-33.5'),('2025-03-03','I','-6'),
      ('2025-02-12','G','-45.125'),('2025-02-12','I','-10'),('2025-02-11','G','-60'),('2025-02-11','I','-15'),
      ('2025-02-10','G','-99'),('2025-02-10','I','-1'),('2025-03-14','G','-50.5'),('2025-03-14','I','-9.75')]
def f21(c):
    c['entrada'].update(p_SeparaIntermediacion='S',p_AgrupaPortafolios='N',p_Consolidar='N')
    c['colaboradores']['consultarSaldo']['P_OUT_SALDOPORFECHAEFECTIVA'][0]['SaldoDisponible']=-60.25
mk('CTA-F-21_antiguedad-limites-de-tramo-2-3-10-11-30-31-32-dias-separa-S','CTA-B-93_',f21,seed=''.join(S%r for r in rows))
# F22 / F23 Bolsa: deuda exacta = suma de cargos / un centavo mas
CA='INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" VALUES (%d, 4001, 1, \'MOVCAR\', \'CRT\', \'%s\', %s, \'CNC001\', %s, DATE \'%s\', DATE \'%s\', TIMESTAMP \'%s 09:00:00\');\n'
seedB=(CA%(8101,'CTA','NULL','12.5','2025-03-13','2025-03-13','2025-03-13')+CA%(8102,'CTA','NULL','0.01','2025-03-12','2025-03-12','2025-03-12')
       +CA%(8103,'FAC','6001','100.34','2025-02-28','2025-02-28','2025-02-28')+CA%(8104,'FAC','6001','147.15','2025-01-31','2025-01-31','2025-01-31'))
def fb(monto,sep):
    def f(c):
        c['entrada'].update(p_SeparaIntermediacion=sep)
        c['colaboradores']['consultarSaldo']['P_OUT_SALDOPORFECHAEFECTIVA'][0]['SaldoDisponible']=monto
    return f
mk('CTA-F-22_bolsa-deuda-exacta-igual-a-la-suma-de-cargos-260','CTA-B-69_',fb(-260,'S'),seed=seedB)
mk('CTA-F-23_bolsa-deuda-un-centavo-sobre-la-suma-de-cargos-separa-S','CTA-B-69_',fb(-260.01,'S'),seed=seedB)
# F24 GenerarArchivoSaldoCuentas: valores de redondeo, nombres con comillas/acentos, fecha con DST, moneda USD
def f24(c):
    rows=c['colaboradores']['consultarSaldo']['P_OUT_SALDOPORFECHAEFECTIVA']
    vals=[(0.005,"Ñandú \"x\" Ltda",'2025-04-05'),(-1234567.895,'Plain','0001-01-01'),(999999999.999,'','2025-03-31')]
    for r,(v,n,f) in zip(rows,vals):
        r.update(SaldoDisponible=v,NombrePortafolio=n,FechaUltimoMovimiento=f,TotalLiquidez=v,SaldoRetenido=-0.5)
    c['entrada'].update(p_IdMoneda='USD',p_FechaDesde='2025-04-05',p_FechaHasta='2025-04-05')
mk('CTA-F-24_saldo-cuentas-redondeo-usd-nombre-con-comillas-y-vacio','CTA-B-131_',f24)
# F25 Acreedores: consolidar N, saldo positivo minimo
def f25(c):
    c['entrada'].update(p_Consolidar='N',p_FechaConsulta='2025-03-14')
    c['colaboradores']['consultarSaldo']['P_OUT_SALDOPORFECHAEFECTIVA'][0]['SaldoDisponible']=160
    c['colaboradores']['consultarSaldo']['P_OUT_SALDOPORFECHAEFECTIVA'][1]['SaldoDisponible']=0.01
mk('CTA-F-25_acreedores-sin-consolidar-saldo-igual-a-la-suma-de-abonos-160-y-centavo','CTA-B-83_',f25)

# F26 probes: division by zero in the index (first value 0)
def f26(c): c['entrada']={'p_FechaDesde':'2025-05-01','p_FechaHasta':'2025-05-02','p_clasificacion':[{'IdArbolClasificacionActivo':1045}]}
mk('CTA-F-26_indice-primer-valor-cero','CTA-B-175_',f26,seed_extra=IDX%('IPSA','2025-05-01','0')+IDX%('IPSA','2025-05-02','5'))
# F27 Editar sobre registro existente en periodo cerrado? (period 1, existing none) estado VIG monto 0.005
def f27(c): c['entrada'].update(p_IdPortafolio=2002,p_MontoComision=0.005,p_Periodo=2,p_EstadoComision='VIG')
mk('CTA-F-27_editar-existente-monto-medio-centavo','CTA-B-32_',f27)
