# Generates the FRESH family-B cases (TES-B-Fnn_*) in the scratch copy by transforming development cases (never edits them).
import json,glob,os,re,shutil,copy
C='X:/tf/modernized/otr-sva-optimus-corredores/BcsTesoreriaServicios/src/test/resources/tesoreria-b/cases'
def base(p): return glob.glob(C+'/'+p+'_*')[0]
def mk(name,basep,fn_case=None,fn_seed=None):
    b=base(basep); d=C+'/'+name
    if os.path.exists(d): shutil.rmtree(d)
    os.makedirs(d)
    c=json.load(open(b+'/case.json',encoding='utf-8')); c.pop('ref',None)
    if fn_case: fn_case(c)
    c['ref']='FRESH input (independent verifier), derived from '+os.path.basename(b)
    json.dump(c,open(d+'/case.json','w',encoding='utf-8'),indent=2,ensure_ascii=False)
    s=open(b+'/seed.sql',encoding='utf-8').read() if os.path.exists(b+'/seed.sql') else ''
    if fn_seed: s=fn_seed(s)
    if s.strip(): open(d+'/seed.sql','w',encoding='utf-8').write(s)

# FB01 ConsultaBCP: malformed / unusual client ids, unusual order, repeated service
def fb01(c):
    c['entrada']['IdSolicitudBcp']='RQ-C-F01'
    c['entrada']['ConsultasBcp']=[{'IdServicioBcp':'SRV09','IdClienteBcp':'0020101031340'},   # RUC padded with two extra zeros
        {'IdServicioBcp':'SRV01','IdClienteBcp':''},            # malformed: empty string
        {'IdServicioBcp':'SRV01','IdClienteBcp':'12AB5678'},    # malformed: letters in a numeric id
        {'IdServicioBcp':'SRV01','IdClienteBcp':'00012345678'}, # valid DNI
        {'IdServicioBcp':'SRV01','IdClienteBcp':'1234567890123456'}] # 16 digits
mk('TES-B-F01_bcp-consulta-ids-malformados-y-ruc-con-ceros','TES-B-89',fb01)
# FB02 RecaudacionBCP: sub-cent / huge amounts, padded RUC, and a blank client id
def fb02(c):
    e=c['entrada']; e['IdSolicitudBcp']='RQ-F02'; e['NumeroOperacionBcp']='9007199254740993'; e['FechaOperacionBcp']='2025-03-31 23:59:59'
    e['OperacionesBcp']=[{'IdClienteBcp':'000012345678','IdMoneda':'CLP','Monto':0.01},{'IdClienteBcp':'20101031340','IdMoneda':'USD','Monto':99999999999.99},{'IdClienteBcp':'        ','IdMoneda':'CLP','Monto':5}]
mk('TES-B-F02_bcp-recaudacion-0-01-99999999999-99-y-cliente-en-blanco','TES-B-67',fb02)
# FB03 RecaudacionBCP EXTORNO on a Saturday at midnight, the extorno number with leading zeros, several operations
def fb03(c):
    e=c['entrada']; e['IdSolicitudBcp']='RQ-F03'; e['FechaOperacionBcp']='2025-04-05 00:00:00'; e['NumeroOperacionExtornoBcp']='0000654321'
    e['OperacionesBcp']=[{'IdClienteBcp':'20101031340','IdMoneda':'CLP','Monto':-1500.75},{'IdClienteBcp':'00012345678','IdMoneda':'CLP','Monto':0}]
mk('TES-B-F03_bcp-extorno-sabado-medianoche-monto-negativo-y-cero','TES-B-66',fb03)
# FB04 ConsultarLibretaBanco: month end, negative simulated opening balance -0.01
def fb04(c):
    e=c['entrada']; e['p_FechaConsulta']='2025-03-31'; e['p_SaldoInicialSimulado']=-0.01
mk('TES-B-F04_libreta-fin-de-mes-saldo-inicial-simulado-negativo','TES-B-09',fb04,lambda s:s.replace('2025-04-01','2025-03-31'))
# FB05 ConsultarSaldoCuentaBanco: window across month end (4 days), plus a second account without data and a currency not seen (USD)
def fb05(c):
    e=c['entrada']; e['p_FechaDesde']='2025-03-31'; e['p_FechaHasta']='2025-04-03'
    e['p_CuentasAConsultar']=[{'IdCuentaBanco':999,'IdBanco':17,'IdMoneda':'USD'}]+e['p_CuentasAConsultar']
mk('TES-B-F05_saldo-ventana-de-fin-de-mes-y-cuenta-usd-sin-datos','TES-B-01',fb05,lambda s:s.replace("'2025-04-01'","'2025-03-31'"))
# FB06 ConsultarClearingFinanciero: day 2025-04-02 (not used by the head cases), chambers in reverse order
def fb06(c):
    e=c['entrada']; e['p_FechaLiquidacion']='2025-04-02'; e['p_Camaras']=[{'Camara':'DV'},{'Camara':'RV'},{'Camara':'HS'}]
mk('TES-B-F06_clearing-consulta-02-abril-dv-rv-hs','TES-B-23',fb06)
# FB07 ProcesarClearingFinanciero: day 2025-04-02 with the two transactions of that day (RV 218, DV 219)
def s07(s):
    out=s
    for l in s.splitlines(True):
        if re.search(r'ORD\.Transaccion"? .*VALUES \(201,',l) or ('ORD.Transaccion' in l and 'VALUES (201,' in l):
            out+=l.replace('(201,','(218,')
            out+=l.replace('(201,','(219,').replace("'RV'","'DV'")
    return out
def fb07(c):
    e=c['entrada']; e['p_FechaLiquidacion']='2025-04-02'; e['p_Camaras']=[{'Camara':'DV'},{'Camara':'RV'}]
mk('TES-B-F07_clearing-procesa-02-abril-dv-y-rv','TES-B-47',fb07,s07)
# FB08 ModificarPAC: SAAPV mandate 7004, leap-day date, new account 4002, DiaDescuento 31, long CuentaCorriente
def fb08(c):
    m=c['entrada']['p_IngresoPAC'][0]
    m.update(FolioCSC='0',FolioSAAPV='SAAPV-300',CuentaCorriente='00099988877766',Fecha='2024-02-29T00:00:00Z',IdCuentaBancaria=4002,DiaDescuento=31,IdMandatoPACBase=7004,NumeroMandato=2,Receptor='CAJA ñ',Firma='')
mk('TES-B-F08_modificar-pac-saapv-29-feb-dia-31-firma-vacia','TES-B-120',fb08)
# FB09 GestionDeDepositos: EDP of two deposits in reverse order, one already EDP, month end
def fb09(c):
    e=c['entrada']; e['p_FechaMovimiento']='2025-03-31'
    e['p_Registros']=[{'Id':206,'Entidad':'TES.Deposito','IdConcepto':'','Descripcion':''},{'Id':201,'Entidad':'TES.Deposito','IdConcepto':'','Descripcion':''}]
mk('TES-B-F09_depositos-edp-dos-depositos-en-orden-inverso','TES-B-243',fb09)
# FB10 CargaMasivaIngresoEgresoDAPFFMM: amounts 0.01 and 99999999999.99
def s10(s): return s.replace(';1000.50;',';0.01;').replace(';1000.50;',';0.01;').replace(';250;',';99999999999.99;')
mk('TES-B-F10_carga-masiva-montos-0-01-y-99999999999-99','TES-B-178',None,s10)
# FB11 ConsultaBCP: leading blank in a numeric client id (the legacy throws NumberFormatException for the whole request)
def fb11(c):
    c['entrada']['IdSolicitudBcp']='RQ-C-F11'
    c['entrada']['ConsultasBcp']=[{'IdServicioBcp':'SRV01','IdClienteBcp':'00012345678'},{'IdServicioBcp':'SRV02','IdClienteBcp':' 12345678'}]
mk('TES-B-F11_bcp-consulta-id-con-blanco-inicial-lanza','TES-B-89',fb11)
print('ok')
