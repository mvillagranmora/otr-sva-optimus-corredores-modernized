# Per-type specs transcribed from the legacy classes (line numbers refer to the legacy .java files).
SPECS = []

def std(cls, mov, ev, datos, fields, maplines, passLines, extra='', **kw):
    """mov/ev/datos: (file, P(...), lines)."""
    q = [('movimientos',) + mov] if mov else []
    if ev:
        q += [('eventos',) + ev]
    q += [('datosMovimiento',) + datos]
    sp = dict(cls=cls, variants=[dict(queries=q, datos=datos[0], fields=fields, maplines=maplines)],
              passLines=passLines, extra=extra)
    sp.update(kw)
    SPECS.append(sp)
    return sp

# ---------------------------------------------------------------- AjustePreciosVentaCorta
std('CentralizaAjustePreciosVentaCorta',
    ('CentralizaAjustePreciosVentaCorta_SelectMovimientos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T'), '76-81'),
    ('CentralizaAjustePreciosVentaCorta_SelectEventos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T'), '85-89'),
    ('CentralizaAjustePreciosVentaCorta_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T LocSistema:L'), '93-98'),
    'DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor '
    'TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'FechaAnterior=dsql FechaActual=dsql TipoCambio=lit:"" PrecioMercadoAnterior=lit:"" PrecioMercadoActual=lit:"" TipoPersona '
    'FechaOperacion=dsql Relacion', '101-134', '81,89,136')

# ---------------------------------------------------------------- AnticipoVencimiento* (clasificacion pre-query)
ANT_FIELDS = ('DocumentoIdentidad NombreCliente Portafolio Moneda EsRelacionado EsCuentaPropia Producto Nemo Boveda IdCartera Emisor '
              'TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente TipoPrestamo '
              'TipoPrestatario CondicionLiquidacion FechaLiquidacion=tostrS FechaOperacion=tostrS')
for cls, o in (('CentralizaAnticipoVencimientoEmprestito', 0), ('CentralizaAnticipoVencimientoPrestamo', 1)):
    def L(a, b, o=o):
        return '%d-%d' % (a + o, b + o)
    cf = cls + '_SelectClasificacionTipoInstrumento.sql'
    sp = std(cls,
             (cls + '_SelectMovimientos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T ClasificacionTipoInstrumento:C'), L(70, 75)),
             (cls + '_SelectEventos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T ClasificacionTipoInstrumento:C'), L(79, 83)),
             (cls + '_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F LocSistema:L ClasificacionTipoInstrumento:C'), L(87, 91)),
             ANT_FIELDS, L(94, 119), L(75, 83),
             setup=['rows("%s", List.of(row("ClasificacionTipoInstrumento", CLASIF)));' % cf])
    sp['variants'][0]['queries'].insert(0, ('clasificacionTipoInstrumento', cf, P('IdTipoCentralizacion:T'), L(64, 67)))

std('CentralizaAnticipoVencimientoPrestamoEmprestito',
    ('CentralizaAnticipoVencimientoPrestamoEmprestito_SelectMovimientos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T'), '69-73'),
    ('CentralizaAnticipoVencimientoPrestamoEmprestito_SelectEventos.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T'), '77-80'),
    ('CentralizaAnticipoVencimientoPrestamoEmprestito_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F LocSistema:L'), '84-87'),
    'ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente '
    'SucursalAgente NombreSucursalAgente CentroCostoAgente FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql IdConcepto Relacion '
    'EsCuentaPropia ClienteBancario', '90-115', '73,80,117')

# ---------------------------------------------------------------- AporteFondoMutuo*
FFMM = ('DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera=svS '
        'TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente Relacion Bolsa FechaOperacion=dsql '
        'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:"" '
        'MontoMonedaSistema=dec TipoOperacionFFMM IdFondo Emisor ClienteBancario')
for cls, end in (('CentralizaAporteFondoMutuoCuentaPropia', '101-137'), ('CentralizaAporteFondoMutuoCuentaTercero', '101-138')):
    std(cls,
        (cls + '_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T IdEmpresa:E DocumentoIdentidadCorredora:D'), '77-82'),
        (cls + '_SelectEventos.sql', P('FechaContable:F IdTipoCentralizacion:T IdMonedaSistema:M IdEmpresa:E DocumentoIdentidadCorredora:D'), '86-91'),
        (cls + '_SelectDatosMovimiento.sql', P('FechaContable:F IdEmpresa:E DocumentoIdentidadCorredora:D'), '95-98'),
        FFMM, end, '82,91,139')

# ---------------------------------------------------------------- CarterasForward family
def vr_event(idc, code, name, amount):
    return 'new TabEventoRef(ID_TIPO, %dL, "%s", "%s", new BigDecimal("%s"), MONEDA_SISTEMA)' % (idc, code, name, amount)

def monto_row(idc, act, pas, neto, extra=''):
    return ('row("IdCartera", %dL, "IdMonedaPrincipal", "USD", "RazonableActivo", new BigDecimal("%s"), '
            '"RazonablePasivo", new BigDecimal("%s"), "RazonableNeto", new BigDecimal("%s")%s)' % (idc, act, pas, neto, extra))

def forward_events_test(cls, lines):
    ev = [ 'EV',
           vr_event(11, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '100.50'),
           vr_event(11, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '160.50'),
           vr_event(11, 'VALOR_RAZONABLE_PERDIDA', 'Valor a Razonable Perdida', '60.00'),
           vr_event(12, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '50.10'),
           vr_event(12, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '25.00'),
           vr_event(12, 'VALOR_RAZONABLE_UTILIDAD', 'Valor a Razonable Utilidad', '25.10'),
           vr_event(13, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '10.00'),
           vr_event(13, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '10.00'),
           vr_event(21, 'VALOR_RAZONABLE_ACTIVO_ANTERIOR', 'Valor a Razonable Activo Anterior', '1.00'),
           vr_event(21, 'VALOR_RAZONABLE_PASIVO_ANTERIOR', 'Valor a Razonable Pasivo Anterior', '6.50'),
           vr_event(21, 'VALOR_RAZONABLE_PERDIDA_ANTERIOR', 'Valor a Razonable Perdida Anterior', '5.50'),
           vr_event(22, 'VALOR_RAZONABLE_ACTIVO_ANTERIOR', 'Valor a Razonable Activo Anterior', '9.00'),
           vr_event(22, 'VALOR_RAZONABLE_PASIVO_ANTERIOR', 'Valor a Razonable Pasivo Anterior', '2.00'),
           vr_event(22, 'VALOR_RAZONABLE_UTILIDAD_ANTERIOR', 'Valor a Razonable Utilidad Anterior', '7.00')]
    return '''    @Test
    @DisplayName("CONT-E-@ID@: VALOR_RAZONABLE_* events appended after the eventos rows: neto<0 PERDIDA |neto|, neto>0 UTILIDAD |neto|, neto=0 neither; *_ANTERIOR from the His rows; moneda = MonedaSistema, IdMovimiento = IdCartera")
    void contE@ID@_valorRazonableEventsFromMontoRows() {
        // Legacy: %s.java:%s
        rows("%s_SelectMontoEventos.sql", List.of(
                %s,
                %s,
                %s));
        rows("%s_SelectMontoEventosHis.sql", List.of(
                %s,
                %s));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                %s);
    }
''' % (cls, lines, cls, monto_row(11, '100.50', '160.50', '-60.00'), monto_row(12, '50.10', '25.00', '25.10'),
       monto_row(13, '10.00', '10.00', '0.00'), cls, monto_row(21, '1.00', '6.50', '-5.50'), monto_row(22, '9.00', '2.00', '7.00'),
       ',\n                '.join(ev))

def dia_anterior_test(cls, lines):
    return '''    @Test
    @DisplayName("CONT-E-@ID@: FechaAnteriorSistema = ObtenerDiaAnterior(idPais of the company, fechaContable, 1 business day)")
    void contE@ID@_fechaAnteriorFromCompanyCountry() {
        // Legacy: %s.java:%s (inObtenerDiaAnterior.idPais = empresa.IdPais, numeroDias = 1, diaHabil = "S")
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, 51L, DOC_CORREDORA, "CORREDORA PE"));
        when(plataforma.obtenerDiaAnterior(eq(51L), eq(FECHA))).thenReturn("2025-03-06");
        service.centralizar(entrada("S"));
        verify(plataforma).obtenerDiaAnterior(51L, FECHA);
        assertThat(paramsOf("%s")).containsEntry("FechaAnteriorSistema", "2025-03-06");
    }
''' % (cls, lines, '%s')

CF = 'CentralizaCarterasForward'
sp = std(CF,
    (CF + '_SelectMovimientos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '102-107'),
    (CF + '_SelectEventos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '110-113'),
    (CF + '_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F'), '127-129'),
    'DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio Moneda=s:MonedaPrincipal Modalidad=svS:ModalidaPago Producto '
    'ClaseInstrumento TipoInstrumento TipoOperacion Nemo Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa TasaCambio=plain TipoPersona Concepto FechaLiquidacion=dstr FechaOperacion=dstr CompraVenta '
    'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:"" ClienteBancario',
    '155-188', '107,113,189')
q = sp['variants'][0]['queries']
q.insert(2, ('montoEventos', CF + '_SelectMontoEventos.sql', P('IdEmpresa:E FechaContable:F'), '116-118'))
q.insert(3, ('montoEventosHis', CF + '_SelectMontoEventosHis.sql', P('IdEmpresa:E FechaContable:F FechaAnteriorSistema:A'), '89-100,121-124'))
sp['extra'] = forward_events_test(CF, '131-153') + dia_anterior_test(CF, '86-100,123') % (CF + '_SelectMontoEventosHis.sql')

CFI = 'CentralizaCarterasForwardIngreso'
std(CFI,
    (CFI + '_SelectMovimientos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '65-70'),
    ('CentralizaCarterasForwardIngresos_SelectEventos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '22,72-76'),
    (CFI + '_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F'), '78-81'),
    'DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio MonedaPrincipal MonedaSecundaria Modalidad=svS:ModalidaPago Producto '
    'ClaseInstrumento TipoInstrumento TipoOperacion Nemo Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa TasaCambio=plain TipoPersona Concepto FechaLiquidacion=dstr FechaOperacion=dstr CompraVenta '
    'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:""',
    '83-116', '70,76,117')

CFV = 'CentralizaCarterasForwardValorizacion'
VAL_FIELDS = ('DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio MonedaPrincipal MonedaSecundaria Modalidad=svS:ModalidaPago Producto '
    'ClaseInstrumento TipoInstrumento TipoOperacion Nemo Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa TasaCambio=plain TipoPersona Concepto CompraVenta FechaLiquidacion=dstr FechaOperacion=dstr '
    'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:""')
val_ev = ['EV',
          vr_event(11, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '100.50'),
          vr_event(11, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '160.50'),
          vr_event(11, 'VALOR_RAZONABLE_PERDIDA', 'Valor a Razonable Perdida', '60.00'),
          vr_event(11, 'VALOR_RAZONABLE_ACTIVO_ANTERIOR', 'Valor a Razonable Activo Anterior', '90.00'),
          vr_event(11, 'VALOR_RAZONABLE_PASIVO_ANTERIOR', 'Valor a Razonable Pasivo Anterior', '60.00'),
          vr_event(11, 'VALOR_RAZONABLE_UTILIDAD_ANTERIOR', 'Valor a Razonable Utilidad Anterior', '30.00'),
          vr_event(12, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '5.00'),
          vr_event(12, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '5.00'),
          vr_event(12, 'VALOR_RAZONABLE_ACTIVO_ANTERIOR', 'Valor a Razonable Activo Anterior', '3.00'),
          vr_event(12, 'VALOR_RAZONABLE_PASIVO_ANTERIOR', 'Valor a Razonable Pasivo Anterior', '3.00'),
          vr_event(13, 'VALOR_RAZONABLE_ACTIVO', 'Valor a Razonable Activo', '8.00'),
          vr_event(13, 'VALOR_RAZONABLE_PASIVO', 'Valor a Razonable Pasivo', '3.00'),
          vr_event(13, 'VALOR_RAZONABLE_UTILIDAD', 'Valor a Razonable Utilidad', '5.00'),
          vr_event(13, 'VALOR_RAZONABLE_ACTIVO_ANTERIOR', 'Valor a Razonable Activo Anterior', '1.00'),
          vr_event(13, 'VALOR_RAZONABLE_PASIVO_ANTERIOR', 'Valor a Razonable Pasivo Anterior', '3.00'),
          vr_event(13, 'VALOR_RAZONABLE_PERDIDA_ANTERIOR', 'Valor a Razonable Perdida Anterior', '2.00')]
def vrow(idc, a, p, n, aa, pa, na):
    return monto_row(idc, a, p, n, ', "RazonableActivoAnterior", new BigDecimal("%s"), "RazonablePasivoAnterior", new BigDecimal("%s"), '
                     '"RazonableNetoAnterior", new BigDecimal("%s")' % (aa, pa, na))
SPECS.append(dict(
    cls=CFV, passLines='119,130,208',
    variants=[
        dict(label='fechaContable = FechaSistema', queries=[
            ('movimientos', CFV + '_SelectMovimientos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '113-119'),
            ('eventos', CFV + '_SelectEventos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F IdMonedaSistema:M'), '125-130'),
            ('montoEventos', CFV + '_SelectMontoEventos.sql', P('IdEmpresa:E FechaContable:F FechaAnteriorSistema:A'), '97-111,136-140'),
            ('datosMovimiento', CFV + '_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F'), '146-149')],
            datos=CFV + '_SelectDatosMovimiento.sql', fields=VAL_FIELDS, maplines='175-207'),
        dict(label='fechaContable != FechaSistema', suffix='Historico', setup=['fechaSistema(FECHA_HIST);'], queries=[
            ('movimientos', CFV + '_SelectMovimientosHis.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '118-122'),
            ('eventos', CFV + '_SelectEventosHis.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F IdMonedaSistema:M'), '129-133'),
            ('montoEventos', CFV + '_SelectMontoEventosHis.sql', P('IdEmpresa:E FechaContable:F FechaAnteriorSistema:A'), '139-143'),
            ('datosMovimiento', CFV + '_SelectDatosMovimientoHis.sql', P('IdEmpresa:E FechaContable:F'), '148-152')],
            notIssued=[CFV + '_SelectMovimientos.sql', CFV + '_SelectEventos.sql', CFV + '_SelectMontoEventos.sql', CFV + '_SelectDatosMovimiento.sql'],
            datos=CFV + '_SelectDatosMovimientoHis.sql', fields=VAL_FIELDS, maplines='150-152,175-207')],
    extra='''    @Test
    @DisplayName("CONT-E-@ID@: one monto row yields ACTIVO, PASIVO, PERDIDA/UTILIDAD(|Neto|) then *_ANTERIOR from the Anterior columns; zero neto adds neither")
    void contE@ID@_valorRazonableEventsCurrentAndAnteriorPerRow() {
        // Legacy: CentralizaCarterasForwardValorizacion.java:154-173
        rows("CentralizaCarterasForwardValorizacion_SelectMontoEventos.sql", List.of(
                %s,
                %s,
                %s));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                %s);
    }
''' % (vrow(11, '100.50', '160.50', '-60.00', '90.00', '60.00', '30.00'), vrow(12, '5.00', '5.00', '0.00', '3.00', '3.00', '0.00'),
       vrow(13, '8.00', '3.00', '5.00', '1.00', '3.00', '-2.00'), ',\n                '.join(val_ev)) + '''    @Test
    @DisplayName("CONT-E-@ID@: FechaAnteriorSistema = ObtenerDiaAnterior(idPais of the company, fechaContable)")
    void contE@ID@_fechaAnteriorFromCompanyCountry() {
        // Legacy: CentralizaCarterasForwardValorizacion.java:94-111,138
        when(plataforma.consultarEmpresa(any())).thenReturn(new EmpresaInfo(ID_EMPRESA, 51L, DOC_CORREDORA, "CORREDORA PE"));
        when(plataforma.obtenerDiaAnterior(eq(51L), eq(FECHA))).thenReturn("2025-03-06");
        service.centralizar(entrada("S"));
        verify(plataforma).obtenerDiaAnterior(51L, FECHA);
        assertThat(paramsOf("CentralizaCarterasForwardValorizacion_SelectMontoEventos.sql")).containsEntry("FechaAnteriorSistema", "2025-03-06");
    }
'''))

CFX = 'CentralizaCarterasForwardVencimiento'
sp = std(CFX,
    (CFX + '_SelectMovimientos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '102-107'),
    (CFX + '_SelectEventos.sql', P('IdEmpresa:E IdTipoCentralizacion:T FechaContable:F'), '110-113'),
    (CFX + '_SelectDatosMovimiento.sql', P('IdEmpresa:E FechaContable:F'), '127-129'),
    'DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio MonedaPrincipal MonedaSecundaria Modalidad=svS:ModalidaPago Producto '
    'ClaseInstrumento TipoInstrumento TipoOperacion Nemo Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa TasaCambio=plain TipoPersona Concepto FechaLiquidacion=dstr FechaOperacion=dstr CompraVenta '
    'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:""',
    '153-185', '107,113,186')
q = sp['variants'][0]['queries']
q.insert(2, ('montoEventos', CFX + '_SelectMontoEventos.sql', P('IdEmpresa:E FechaContable:F'), '116-118'))
q.insert(3, ('montoEventosHis', CFX + '_SelectMontoEventosHis.sql', P('IdEmpresa:E FechaContable:F FechaAnteriorSistema:A'), '89-100,121-124'))
sp['extra'] = forward_events_test(CFX, '131-151') + dia_anterior_test(CFX, '86-100,123') % (CFX + '_SelectMontoEventosHis.sql')

# ---------------------------------------------------------------- Carteras GarCtaProp / RFCompraTermino / RVTermino
std('CentralizaCarterasGarCtaProp',
    ('CentralizaCarteras_SelectMovimientosGarCtaProp.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '24,85-90'),
    ('CentralizaCarteras_SelectEventosGarCtaProp.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '25,94-98'),
    ('CentralizaCarteras_SelectDatosMovimientoGarCtaProp.sql', P('FechaContable:F IdEmpresa:E DocumentoIdentidad:D LocSistema:L'), '26,102-106'),
    'DocumentoIdentidad NombreCliente Portafolio Folio=svL DocumentoIdentidadCAVALI Producto ClaseInstrumento TipoInstrumento '
    'IdTipoInstrumentoGarantizado IdOrigenGarantia=svS Nemo Boveda IdGarantia=svL Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor '
    'Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Concepto NombreConcepto TipoMovimiento Moneda '
    'FechaActual=dsql TasaCambio=dec Relacion=svS Bolsa FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql',
    '109-148', '90,98,150')

std('CentralizaCarterasRFCompraTermino',
    ('CentralizaCarteras_SelectMovimientosRFCompraTermino.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '23,78-83'),
    ('CentralizaCarteras_SelectEventosRFCompraTermino.sql', P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '24,87-91'),
    ('CentralizaCarteras_SelectDatosMovimientoRFCompraTermino.sql', P('FechaContable:F IdEmpresa:E DocumentoIdentidad:D'), '25,95-98'),
    'DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera=svL Emisor '
    'TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion=svS Bolsa FechaLiquidacion=dsql FechaOperacion=dsql Concepto TipoPersona TipoEmisor Mesa FolioOperacion=svL ClienteBancario',
    '101-134', '83,91,136')

RV = 'CentralizaCarterasRVTerminoYMayorMenorValor'
RV_FIELDS = ('IdMovimiento=svL DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda '
    'IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente '
    'CentroCostoAgente PrecioMercadoAnterior=dec PrecioMercadoActual=dec TipoCambio=dec FechaAnterior=dsql FechaActual=dsql Relacion '
    'Bolsa FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql Concepto=s:IdConcepto DocumentoIdentidadCAVALI '
    'CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:""')
RVP = P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T p_DocumentoIdentidadCorredora:D')
RVD = P('p_IdEmpresa:E p_FechaContable:F p_DocumentoIdentidadCorredora:D')
SPECS.append(dict(
    cls=RV, passLines='87,100,161',
    variants=[
        dict(label='fechaContable = FechaSistema', queries=[
            ('movimientos', RV + '_SelectMovimientos.sql', RVP, '81-87'),
            ('eventos', RV + '_SelectEventos.sql', RVP, '95-100'),
            ('datosMovimiento', RV + '_SelectDatosMovimiento.sql', RVD, '108-114')],
            datos=RV + '_SelectDatosMovimiento.sql', fields=RV_FIELDS, maplines='119-159', pojo='MovimientoCarterasRVTerminoYMayorMenorValor'),
        dict(label='fechaContable != FechaSistema', suffix='Historico', setup=['fechaSistema(FECHA_HIST);'], queries=[
            ('movimientos', RV + '_SelectMovimientosHistorico.sql', RVP, '86-90'),
            ('eventos', RV + '_SelectEventosHistorico.sql', RVP, '99-103'),
            ('datosMovimiento', RV + '_SelectDatosMovimientoHistorico.sql', RVD, '113-117')],
            notIssued=[RV + '_SelectMovimientos.sql', RV + '_SelectEventos.sql', RV + '_SelectDatosMovimiento.sql'])]))

# ---------------------------------------------------------------- ClearingFinanciero (no SQL; ConsultarClearingFinanciero)
SPECS.append(dict(
    cls='CentralizaClearingFinanciero', standardPass=False, stubMov=False,
    persistPrep=['when(plataforma.consultarClearingFinanciero(any(), any(), any(), any()))',
                 '        .thenReturn(List.of(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO)));'],
    helpers='''    static ClearingFinancieroDetalle det(Long idTransaccion, Long idComprobante, String folio, BigDecimal venta, BigDecimal compra) {
        return new ClearingFinancieroDetalle(idTransaccion, idComprobante, folio, venta, compra, "USD", "22222222-2",
                "CONTRAPARTE SA", "C", "RF", "CAMARA-PM", "PH", "BCS", FECHA);
    }

    void clearing(ClearingFinancieroDetalle... d) {
        when(plataforma.consultarClearingFinanciero(any(), any(), any(), any())).thenReturn(List.of(d));
    }
''',
    imports='import cl.bcs.contabilidad.domain.port.out.PlataformaPort.ClearingFinancieroDetalle;\n',
    extra='''    @Test
    @DisplayName("CONT-E-@ID@: data source is ConsultarClearingFinanciero(idEmpresa, idUsuario, idModulo, fechaLiquidacion = fechaContable); no SQL is run")
    void contE@ID@_sourceIsConsultarClearingFinanciero() {
        // Legacy: CentralizaClearingFinanciero.java:58-68 (also p_UsaReglasNegocio = "S", camaras PM,RV,SM,PH,HS - not expressible in the port)
        clearing();
        service.centralizar(entrada("S"));
        verify(plataforma).consultarClearingFinanciero(ID_EMPRESA, ID_USUARIO, ID_MODULO, FECHA);
        assertThat(issuedSql()).isEmpty();
        verify(repository, never()).queryMovimientos(any(), any(), any());
        verify(repository, never()).queryEventosOperacion(any(), any(), any());
    }

    @Test
    @DisplayName("CONT-E-@ID@: only details with IdComprobanteTesoreria > 0 are centralized")
    void contE@ID@_onlyPositiveComprobante() {
        // Legacy: CentralizaClearingFinanciero.java:71,92,114 (filter detalle.IdComprobanteTesoreria > 0)
        clearing(det(1L, 0L, "1", new BigDecimal("5.00"), BigDecimal.ZERO), det(2L, 7L, "2", new BigDecimal("6.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::idMovimiento).containsExactly(2L);
        assertThat(salida.tabEventos()).extracting(TabEventoRef::idMovimiento).containsExactly(2L, 2L);
        assertThat(salida.tabDatosMovimiento()).extracting(TabDatoMovimientoRef::idMovimiento).containsOnly(2L).hasSize(10);
    }

    @Test
    @DisplayName("CONT-E-@ID@: movement row: CLEARING, Numero = folio, Linea 0, IdPortafolio 0, Ejercicio = year of fechaContable (not entrada), Periodo MM-yy, Estado empty")
    void contE@ID@_movementBuiltFromDetail() {
        // Legacy: CentralizaClearingFinanciero.java:72-87
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), new BigDecimal("3.00")));
        EntradaCentralizacion in = new EntradaCentralizacion(ID_EMPRESA, ID_USUARIO, ID_MODULO, ID_TIPO, FECHA, null, 1999L, "S");
        SalidaCentralizacion salida = service.centralizar(in);
        assertThat(salida.tabMovimientos()).containsExactly(new TabMovimientoRef(ID_TIPO, 9001L, "CLEARING", ID_EMPRESA, 4455L, 0L,
                FECHA, "22222222-2", 0L, 2025L, "03-25", new BigDecimal("1000.00"), "USD", ""));
        assertThat(salida.resultados()).singleElement().satisfies(r -> {
            assertThat(r.ejercicio()).isEqualTo("2025");
            assertThat(r.periodo()).isEqualTo("03-25");
        });
    }

    @Test
    @DisplayName("CONT-E-@ID@: Monto = TotalVenta when > 0, otherwise TotalCompra; null folio -> Numero 0")
    void contE@ID@_montoVentaElseCompraAndNullFolio() {
        // Legacy: CentralizaClearingFinanciero.java:77 (Optional.ofNullable(folio).orElse("0")), :85
        clearing(det(1L, 5L, null, BigDecimal.ZERO, new BigDecimal("77.70")), det(2L, 5L, "8", new BigDecimal("-1.00"), new BigDecimal("4.00")));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::numero, TabMovimientoRef::monto)
                .containsExactly(tuple(0L, new BigDecimal("77.70")), tuple(8L, new BigDecimal("4.00")));
    }

    @Test
    @DisplayName("CONT-E-@ID@: events TOTAL_VENTA then TOTAL_COMPRA per detail, null total -> 0, moneda of the transaction")
    void contE@ID@_totalVentaCompraEvents() {
        // Legacy: CentralizaClearingFinanciero.java:92-110
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), null));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                new TabEventoRef(ID_TIPO, 9001L, "TOTAL_VENTA", "Total Venta", new BigDecimal("1000.00"), "USD"),
                new TabEventoRef(ID_TIPO, 9001L, "TOTAL_COMPRA", "Total Compra", BigDecimal.ZERO, "USD"));
    }

    @Test
    @DisplayName("CONT-E-@ID@: null TotalVenta aborts (TotalVenta.compareTo on null)")
    void contE@ID@_nullTotalVentaAborts() {
        // Legacy: CentralizaClearingFinanciero.java:85 detalle.TotalVenta.compareTo(...) -> NPE before the events are built
        clearing(det(9001L, 5L, "4455", null, new BigDecimal("3.00")));
        assertThatThrownBy(() -> service.centralizar(entrada("S"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-@ID@: 10 datos-movimiento in order; FechaDocumento = fechaContable; EsContraparte = SI")
    void contE@ID@_datosMovimientoFields() {
        // Legacy: CentralizaClearingFinanciero.java:113-194
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("Folio", "4455", BigDecimal.ZERO),
                        tuple("FechaDocumento", FECHA, BigDecimal.ZERO),
                        tuple("CompraVenta", "C", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "RF", BigDecimal.ZERO),
                        tuple("Camara", "CAMARA-PM", BigDecimal.ZERO),
                        tuple("CondicionLiquidacion", "PH", BigDecimal.ZERO),
                        tuple("Bolsa", "BCS", BigDecimal.ZERO),
                        tuple("DocumentoIdentidad", "22222222-2", BigDecimal.ZERO),
                        tuple("NombreCliente", "CONTRAPARTE SA", BigDecimal.ZERO),
                        tuple("EsContraparte", "SI", BigDecimal.ZERO));
        assertIds(salida.tabDatosMovimiento(), 9001L);
    }

    @Test
    @DisplayName("CONT-E-@ID@: null detail strings -> empty, except Folio which stays null (assigned directly, no ctor coalescing)")
    void contE@ID@_datosMovimientoNulls() {
        // Legacy: CentralizaClearingFinanciero.java:119 (mov.ValorVarChar = detalle.FolioTransaccion, no-arg ctor),
        // :135-183 Optional.ofNullable(..).orElse("")
        clearing(new ClearingFinancieroDetalle(9001L, 5L, null, new BigDecimal("1.00"), BigDecimal.ZERO, null, null, null, null, null, null, null, null, null));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar)
                .containsExactly(
                        tuple("Folio", null), tuple("FechaDocumento", FECHA), tuple("CompraVenta", ""), tuple("ClaseInstrumento", ""),
                        tuple("Camara", ""), tuple("CondicionLiquidacion", ""), tuple("Bolsa", ""), tuple("DocumentoIdentidad", ""),
                        tuple("NombreCliente", ""), tuple("EsContraparte", "SI"));
    }

    @Test
    @DisplayName("CONT-E-@ID@: 0 errors, no messages, no datos-evento")
    void contE@ID@_noErrors() {
        // Legacy: BcsCentralizacion.java:25-39; CentralizaClearingFinanciero.java never touches P_OUT_* counters
        clearing(det(9001L, 5L, "4455", new BigDecimal("1000.00"), BigDecimal.ZERO));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }
'''))

# ---------------------------------------------------------------- CompraVentaPactos (config MESA.TASARECOMPRAVENTACONPACTO)
CVP = 'CentralizaCompraVentaPactos'
sp = std(CVP,
    (CVP + '_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T'), '74-77'),
    (CVP + '_SelectEventosOperacion.sql', P('FechaContable:F IdTipoCentralizacion:T'), '81-83'),
    (CVP + '_SelectDatosMovimiento.sql', P('FechaContable:F'), '94-95'),
    'DocumentoIdentidad NombreCliente CompraVenta Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera=svL Emisor '
    'TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion=svS Bolsa TipoPersona Concepto FechaLiquidacion=dsql FechaOperacion=dsql TipoEmisor Mesa FolioOperacion=svL',
    '98-130', '77,83,90,132', sub='EV_SUB', eventsExpected='List.of(EV, EV_SUB)')
sp['variants'][0]['queries'].insert(2, ('eventosSubyacente', CVP + '_SelectEventosSubyacente.sql',
                                       P('FechaContable:F IdTipoCentralizacion:T IncluirSpreadVentaPacto:"N"'), '86-90,138-148'))
sp['extra'] = '''    @Test
    @DisplayName("CONT-E-@ID@: MESA.TASARECOMPRAVENTACONPACTO = S is passed as IncluirSpreadVentaPacto to the subyacente events query")
    void contE@ID@_tasaRecompraFlagChangesSubyacenteQuery() {
        // Legacy: CentralizaCompraVentaPactos.java:86-90,142 (ConfiguracionSistema MESA / TASARECOMPRAVENTACONPACTO)
        config("MESA", "TASARECOMPRAVENTACONPACTO", "S");
        service.centralizar(entrada("S"));
        assertThat(paramsOf("CentralizaCompraVentaPactos_SelectEventosSubyacente.sql")).containsEntry("IncluirSpreadVentaPacto", "S");
    }

    @Test
    @DisplayName("CONT-E-@ID@: TASARECOMPRAVENTACONPACTO absent -> default N, not an error")
    void contE@ID@_tasaRecompraDefaultsToN() {
        // Legacy: CentralizaCompraVentaPactos.java:140-146 (exception caught, value stays "N")
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        verify(plataforma).obtenerParametroConfiguracion(eq(ID_EMPRESA), eq("MESA"), eq("TASARECOMPRAVENTACONPACTO"), eq("N"));
        assertThat(salida.erroresContador()).isZero();
    }
'''

# ---------------------------------------------------------------- CorteCuponRF / CtaInversion / CuentaCaja
std('CentralizaCorteCuponRF',
    ('CentralizaCorteCuponRF_SelectMovimientos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '85-89'),
    ('CentralizaCorteCuponRF_SelectEventos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '93-96'),
    ('CentralizaCorteCuponRF_SelectDatosMovimiento.sql', P('p_IdEmpresa:E p_FechaContable:F LocSistema:L'), '100-104'),
    'IdMovimiento=svL NombreCuentaCorriente TipoDocumento Folio=svL FechaDocumento NumeroCuentaCorriente MedioDePago NumeroDocumentoPago '
    'FechaLiquidacion=dsql SerieDocumento Concepto DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio Moneda CodigoCaja=svL '
    'EmitidoA EsBilateral TipoPersona Producto Relacion Bolsa CentroCostoAgente ComprobanteTipo Nemotecnico CostingCode=s:CentroCostoAgente '
    'CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=s:OpCentros TipoEmisor Mesa TipoCartera Emisor '
    'EsCuentaPropia ClienteBancario IdTipoReajuste ClaseEmisor EsCuentaTercero', '107-149', '89,96,151')

std('CentralizaCtaInversionCaAbManual',
    ('CentralizaCtaInversionCaAbManual_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T'), '69-72'),
    ('CentralizaCtaInversionCaAbManual_SelectEventos.sql', P('FechaContable:F IdTipoCentralizacion:T'), '76-78'),
    ('CentralizaCtaInversionCaAbManual_SelectDatosMovimiento.sql', P('FechaContable:F LocSistema:L'), '82-84'),
    'IdEmpresa=bdL Fecha DocumentoIdentidad Portafolio Concepto NombreConcepto DocumentoIdentidadCAVALI NombreCliente Moneda Producto '
    'Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente EsContraparte Relacion Bolsa TipoPersona Nemo TipoDocumento '
    'TipoComprobante=s:IdTipoComprobante CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion '
    'CostingCode4=s:Bolsa CostingCode5=s:OpCentros ClienteBancario OperacionNoFacturable', '87-117', '72,78,119')

std('CentralizaCuentaCaja',
    ('CentralizaCuentaCaja_SelectMovimientos.sql', P('FechaContable:F IdEmpresa:E IdTipoCentralizacion:T'), '66-71'),
    ('CentralizaCuentaCaja_SelectEventos.sql', P('FechaContable:F IdTipoCentralizacion:T'), '75-77'),
    ('CentralizaCuentaCaja_SelectDatosMovimiento.sql', P('FechaContable:F IdEmpresa:E LocSistema:L'), '81-84'),
    'TipoDocumento Folio=svL FechaDocumento IdEmpresa=svL Fecha DocumentoIdentidad Portafolio Concepto NombreConcepto NombreCliente Moneda '
    'Agente NombreAgente SucursalAgente=svL NombreSucursalAgente CentroCostoAgente Producto Relacion Bolsa TipoPersona Nemo '
    'DocumentoIdentidadCAVALI Dias=svL TipoCmbte ClienteBancario', '87-116', '71,77,118')

# ---------------------------------------------------------------- Deterioro*
std('CentralizaDeterioroMayorMenorValorRF',
    ('CentralizaDeterioroMayorMenorValorRF_SelectMovimientos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T p_DocumentoIdentidadCorredora:D'), '73-78'),
    ('CentralizaDeterioroMayorMenorValorRF_SelectEventos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T p_DocumentoIdentidadCorredora:D'), '82-86'),
    ('CentralizaDeterioroMayorMenorValorRF_SelectDatosMovimiento.sql', P('p_IdEmpresa:E p_FechaContable:F p_DocumentoIdentidadCorredora:D'), '90-94'),
    'IdMovimiento=svL DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera '
    'Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql IdConcepto DocumentoIdentidadCAVALI TipoEmisor Mesa Fase=svI',
    '97-130', '78,86,133')

std('CentralizaDeterioroPactos',
    ('CentralizaDeterioroPactos_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T'), '71-74'),
    ('CentralizaDeterioroPactos_SelectEventosOperacion.sql', P('FechaContable:F IdTipoCentralizacion:T'), '71-76'),
    ('CentralizaDeterioroPactos_SelectDatosMovimiento.sql', P('FechaContable:F'), '79-80'),
    'IdMovimiento=svL DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio CompraVenta Moneda Producto ClaseInstrumento '
    'TipoInstrumento Nemo Boveda IdCartera=svL Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente '
    'NombreSucursalAgente CentroCostoAgente Relacion=svS Bolsa TipoPersona Concepto FechaLiquidacion=dsql FechaOperacion=dsql TipoEmisor '
    'Mesa FolioOperacion=svL Fase=svI', '83-117', '74,76,119')

std('CentralizaDeterioroSimultaneas',
    ('CentralizaDeterioroSimultaneas_SelectMovimientos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '65-70'),
    ('CentralizaDeterioroSimultaneas_SelectEventos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '65-72'),
    ('CentralizaDeterioroSimultaneas_SelectDatosMovimiento.sql', P('p_IdEmpresa:E p_FechaContable:F'), '73-75'),
    'IdMovimiento=svL DocumentoIdentidad NombreCliente PortafolioCliente=s:Portafolio Moneda Producto ClaseInstrumento TipoInstrumento '
    'Nemo Boveda IdCartera Emisor TipoCartera Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa '
    'FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql Concepto=s:IdConcepto CostingCode=s:CentroCostoAgente CostingCode2=s:Producto '
    'CostingCode3=s:Relacion CostingCode4=s:Bolsa CostingCode5=lit:"" Fase=svI', '78-109', '70,72,111')

# ---------------------------------------------------------------- Devengo Emprestito / Prestamo (movements built in Java)
def devengo(cls, rv, rf, instRv, instRf, LN):
    mov = cls + '_SelectMovimiento.sql'
    his = cls + '_SelectMovimientoHistorico.sql'
    tip = cls + '_SelectMovimientoTipoCentralizacionTipo.sql'
    j = '''    @Test
    @DisplayName("CONT-E-@ID@: tipo de movimiento query runs %(tip)s with params {IdTipoCentralizacion}")
    void contE@ID@_tipoCentralizacionQuery() {
        // Legacy: %(cls)s.java:%(l107)s
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("%(tip)s")).isEqualTo(Map.of("IdTipoCentralizacion", ID_TIPO));
    }

    @Test
    @DisplayName("CONT-E-@ID@: fechaContable = PLA.FechaSistema -> %(mov)s with {FechaContable, IdTipoInstrumento=%(instRv)s for %(rv)s, IdEmpresa}")
    void contE@ID@_movimientoQueryCurrent() {
        // Legacy: %(cls)s.java:%(l102)s (fechaSistema from ConfiguracionSistema PLATAFORMA/PLA.FechaSistema), %(l111)s
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("%(mov)s")).isEqualTo(Map.of("FechaContable", FECHA, "IdTipoInstrumento", "%(instRv)s", "IdEmpresa", ID_EMPRESA));
        assertNotIssued("%(his)s");
    }

    @Test
    @DisplayName("CONT-E-@ID@: fechaContable != PLA.FechaSistema -> %(his)s; IdTipoMovimiento other than %(rv)s -> IdTipoInstrumento %(instRf)s")
    void contE@ID@_movimientoQueryHistoricoAndRf() {
        // Legacy: %(cls)s.java:%(l112)s,%(l118)s
        fechaSistema(FECHA_HIST);
        rows("%(tip)s", List.of(row("IdTipoMovimiento", "%(rf)s")));
        try { service.centralizar(entrada("S")); } catch (RuntimeException ignored) { }
        assertThat(paramsOf("%(his)s")).isEqualTo(Map.of("FechaContable", FECHA, "IdTipoInstrumento", "%(instRf)s", "IdEmpresa", ID_EMPRESA));
        assertNotIssued("%(mov)s");
    }

    @Test
    @DisplayName("CONT-E-@ID@: row with EsCuentaPropia NO -> one movement: IdMovimiento/Numero/Linea = FolioOperacion, Periodo yyyy-MM, Monto = MontoCompraActual, moneda sistema")
    void contE@ID@_movementBuiltFromRow() {
        // Legacy: %(cls)s.java:%(l137)s
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).containsExactly(new TabMovimientoRef(ID_TIPO, 501L, "%(rv)s", ID_EMPRESA, 501L, 501L,
                FECHA, "33333333-3", 300L, EJERCICIO, "2025-03", new BigDecimal("99.40"), "CLP", "V"));
        assertThat(salida.resultados()).singleElement().satisfies(r -> assertThat(r.ejercicio()).isEqualTo("2025"));
    }

    @Test
    @DisplayName("CONT-E-@ID@: IdTipoMovimiento of the movement is %(rf)s whenever the configured type is not %(rv)s")
    void contE@ID@_movementTypeRf() {
        // Legacy: %(cls)s.java:%(l142)s
        rows("%(tip)s", List.of(row("IdTipoMovimiento", "OTRO")));
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).extracting(TabMovimientoRef::idTipoMovimiento).containsExactly("%(rf)s");
    }

    @Test
    @DisplayName("CONT-E-@ID@: rows whose EsCuentaPropia is not NO produce nothing")
    void contE@ID@_cuentaPropiaSkipped() {
        // Legacy: %(cls)s.java:%(l137)s if ("NO".equals(data.EsCuentaPropia))
        rows("%(mov)s", List.of(devRow("SI", 501L), devRow(null, 502L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabMovimientos()).isEmpty();
        assertThat(salida.tabEventos()).isEmpty();
        assertThat(salida.tabDatosMovimiento()).isEmpty();
        assertThat(salida.resultados()).isEmpty();
    }

    @Test
    @DisplayName("CONT-E-@ID@: 16 datos-movimiento per movement in legacy order (Premio carries TasaCompra as decimal)")
    void contE@ID@_datosMovimientoFields() {
        // Legacy: %(cls)s.java:%(l158)s
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabDatosMovimiento())
                .extracting(TabDatoMovimientoRef::tipoDato, TabDatoMovimientoRef::valorVarChar, TabDatoMovimientoRef::valorDecimal)
                .containsExactly(
                        tuple("DocumentoIdentidad", "33333333-3", BigDecimal.ZERO),
                        tuple("Portafolio", "PORT-1", BigDecimal.ZERO),
                        tuple("EsCuentaPropia", "NO", BigDecimal.ZERO),
                        tuple("EsRelacionado", "null", BigDecimal.ZERO),
                        tuple("FechaOperacion", "2025-03-03", BigDecimal.ZERO),
                        tuple("FechaLiquidacion", "2025-03-05", BigDecimal.ZERO),
                        tuple("FolioOperacion", "501", BigDecimal.ZERO),
                        tuple("ClaseInstrumento", "RV", BigDecimal.ZERO),
                        tuple("TipoInstrumento", "ACC", BigDecimal.ZERO),
                        tuple("Nemotecnico", "NEMO1", BigDecimal.ZERO),
                        tuple("Boveda", "DCV", BigDecimal.ZERO),
                        tuple("Emisor", "EMI1", BigDecimal.ZERO),
                        tuple("TipoReajuste", "$", BigDecimal.ZERO),
                        tuple("CodigoIFRS", "", BigDecimal.ZERO),
                        tuple("Moneda", "USD", BigDecimal.ZERO),
                        tuple("Premio", "", new BigDecimal("0.0450")));
        assertIds(salida.tabDatosMovimiento(), 501L);
    }

    @Test
    @DisplayName("CONT-E-@ID@: 7 events per movement in legacy order, moneda = IdMonedaSistema of the row, null monto kept null")
    void contE@ID@_events() {
        // Legacy: %(cls)s.java:%(l177)s ; TabEventos ctor does not coalesce MontoEvento (pojo/TabEventos.java:17-24)
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.tabEventos()).containsExactly(
                new TabEventoRef(ID_TIPO, 501L, "MONTO_MERCADO_ANTERIOR", "Monto a Tasa Mercado Anterior", null, "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_MERCADO_ACTUAL", "Monto a Tasa Mercado Actual", new BigDecimal("101.20"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_ANTERIOR", "Monto a Tasa Compra Anterior", new BigDecimal("99.30"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_ACTUAL", "Monto a Tasa Compra Actual", new BigDecimal("99.40"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "MONTO_COMPRA_PROXIMO", "Monto a Tasa Compra Proximo", new BigDecimal("99.50"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "INTERES_COMPRA_ANTERIOR", "Interes Compra Anterior", new BigDecimal("0.60"), "CLP"),
                new TabEventoRef(ID_TIPO, 501L, "INTERES_COMPRA_PROXIMO", "Interes Compra Proximo", new BigDecimal("0.70"), "CLP"));
    }

    @Test
    @DisplayName("CONT-E-@ID@: null FechaOperacion aborts (data.FechaOperacion.toString())")
    void contE@ID@_nullFechaOperacionAborts() {
        // Legacy: %(cls)s.java:%(l162)s
        Map<String, Object> r = devRow("NO", 501L);
        r.put("FechaOperacion", null);
        rows("%(mov)s", List.of(r));
        assertThatThrownBy(() -> service.centralizar(entrada("S"))).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("CONT-E-@ID@: CUS.UsaFechaLiquidacionEnVencimiento is read but does not change the output")
    void contE@ID@_usaFechaLiquidacionHasNoEffect() {
        // Legacy: %(cls)s.java:%(l124)s stores it in conf.usaFechaLiquidacion; dataCentralizacion (:%(l136)s-%(l186)s) never reads it
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        config("PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento", "S");
        SalidaCentralizacion conS = service.centralizar(entrada("S"));
        config("PLATAFORMA", "CUS.UsaFechaLiquidacionEnVencimiento", "N");
        SalidaCentralizacion conN = service.centralizar(entrada("S"));
        assertThat(conS).isEqualTo(conN);
        assertThat(conS.tabMovimientos()).hasSize(1);
    }

    @Test
    @DisplayName("CONT-E-@ID@: 0 errors, no messages, no datos-evento")
    void contE@ID@_noErrors() {
        // Legacy: BcsCentralizacion.java:25-39
        rows("%(mov)s", List.of(devRow("NO", 501L)));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(salida.erroresContador()).isZero();
        assertThat(salida.mensajes()).isEmpty();
        assertThat(salida.tabDatosEvento()).isEmpty();
    }
''' % dict(cls=cls, mov=mov, his=his, tip=tip, rv=rv, rf=rf, instRv=instRv, instRf=instRf,
           **LN)
    helpers = '''    /** Row of %(mov)s (legacy %(cls)s.Movimientos). */
    static Map<String, Object> devRow(String esCuentaPropia, Long folio) {
        return row("DocumentoIdentidad", "33333333-3", "IdPortafolio", 300L, "CodigoPortafolio", "PORT-1",
                "EsCuentaPropia", esCuentaPropia, "EsRelacionado", null,
                "FechaOperacion", java.sql.Date.valueOf("2025-03-03"), "FechaVencimientoOperacion", java.sql.Date.valueOf("2025-04-03"),
                "FechaLiquidacion", java.sql.Date.valueOf("2025-03-05"), "IdOperacion", 88L, "FolioOperacion", folio,
                "IdClasificacionTipoInstrumento", "RV", "IdTipoInstrumento", "ACC", "IdInstrumento", 9L, "Nemotecnico", "NEMO1",
                "IdEmisor", "EMI1", "IdTipoReajuste", "$", "IdBoveda", "DCV", "CodigoIFRS", null,
                "IdMonedaSistema", "CLP", "IdMonedaInstrumento", "USD", "TasaCompra", new BigDecimal("0.0450"),
                "MontoMercadoAnterior", null, "MontoMercadoActual", new BigDecimal("101.20"),
                "MontoCompraAnterior", new BigDecimal("99.30"), "MontoCompraActual", new BigDecimal("99.40"),
                "MontoCompraProximo", new BigDecimal("99.50"), "InteresMontoCompraAnterior", new BigDecimal("0.60"),
                "InteresMontoCompraProximo", new BigDecimal("0.70"), "IdEmpresa", ID_EMPRESA, "IdEstado", "V");
    }
''' % dict(mov=mov, cls=cls)
    SPECS.append(dict(cls=cls, standardPass=False, stubMov=False,
                      setup=['rows("%s", List.of(row("IdTipoMovimiento", "%s")));' % (tip, rv)],
                      persistPrep=['rows("%s", List.of(devRow("NO", 501L)));' % mov],
                      extra=j, helpers=helpers))

devengo('CentralizaDevengoEmprestito', 'DEVEMPRV', 'DEVEMPRF', 'EMP', 'ERF', dict(l107='107-110', l102='102-104', l111='112-122', l112='114', l118='118-121', l137='140-157', l142='145', l158='161-176', l177='180-186', l162='165', l124='127', l136='139', l186='189'))
devengo('CentralizaDevengoPrestamo', 'DEVPRERV', 'DEVPRERF', 'PRT', 'PRF', dict(l107='105-108', l102='100-102', l111='110-119', l112='112', l118='115-118', l137='137-154', l142='142', l158='158-173', l177='177-183', l162='162', l124='124', l136='136', l186='186'))

# ---------------------------------------------------------------- EmprestitoInicio
EI = 'CentralizaEmprestitoInicio'
EIP = P('IdEmpresa:E FechaContable:F IdTipoCentralizacion:T IdClasificacionTipoInstrumento:C')
EID = P('IdEmpresa:E FechaContable:F LocSistema:L IdClasificacionTipoInstrumento:C')
SPECS.append(dict(
    cls=EI, passLines='83,95,141',
    setup=['rows("%s_SelectClasificacionTipoInstrumento.sql", List.of(row("ClasificacionTipoInstrumento", CLASIF)));' % EI],
    variants=[
        dict(label='fechaContable = PLA.FechaSistema', queries=[
            ('clasificacionTipoInstrumento', EI + '_SelectClasificacionTipoInstrumento.sql', P('IdTipoCentralizacion:T'), '72-75'),
            ('movimientos', EI + '_SelectMovimientos.sql', EIP, '70,77-83'),
            ('eventos', EI + '_SelectEventos.sql', EIP, '89-95'),
            ('datosMovimiento', EI + '_SelectDatosMovimiento.sql', EID, '101-108')],
            datos=EI + '_SelectDatosMovimiento.sql', maplines='114-139',
            fields='DocumentoIdentidad NombreCliente Portafolio Moneda EsRelacionado EsCuentaPropia Producto Nemo Boveda IdCartera Emisor '
                   'TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente TipoPrestamo '
                   'TipoPrestatario CondicionLiquidacion FechaLiquidacion FechaOperacion'),
        dict(label='fechaContable != PLA.FechaSistema', suffix='Historico', setup=['fechaSistema(FECHA_HIST);'], queries=[
            ('movimientos', EI + '_SelectMovimientosHistoricos.sql', EIP, '84-86'),
            ('eventos', EI + '_SelectEventosHistoricos.sql', EIP, '96-98'),
            ('datosMovimiento', EI + '_SelectDatosMovimientoHistorico.sql', EID, '109-111')],
            notIssued=[EI + '_SelectMovimientos.sql', EI + '_SelectEventos.sql', EI + '_SelectDatosMovimiento.sql'])]))

# ---------------------------------------------------------------- EventosDeCapital
std('CentralizaEventosDeCapital',
    ('CentralizaEventosDeCapital_SelectMovimientos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '72-76'),
    ('CentralizaEventosDeCapital_SelectEventos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T'), '80-83'),
    ('CentralizaEventosDeCapital_SelectDatosMovimiento.sql', P('p_IdEmpresa:E p_FechaContable:F p_DocumentoIdentidadCorredora:D LocSistema:L'), '87-91'),
    'IdMovimiento=svL TipoEvento FechaLiquidacion DocumentoIdentidad NombreCliente Portafolio Moneda EsCuentaMandante EsCuentaDepositante '
    'TipoPersona Producto ClaseInstrumento TipoInstrumento Nemo TipoCartera Boveda Agente NombreAgente EsContraparte Relacion Bolsa '
    'SucursalAgente NombreSucursalAgente CentroCostoAgente Concepto OpCentros DocumentoIdentidadCAVALI TipoEmisor TipoDocumento ClienteBancario',
    '94-125', '76,83,127')

# ---------------------------------------------------------------- Facturas family
FAC_COMMON = ('TipoDocumento FechaDocumento CompraVenta Operacion Producto ClaseInstrumento TipoInstrumento CondicionLiquidacion '
              'MonedaComprobante Bolsa DocumentoIdentidad NombreCliente Portafolio Concepto NombreConcepto Agente NombreAgente SucursalAgente '
              'NombreSucursalAgente CentroCostoAgente FolioDTE=svL Folio=svL EsExento Moneda MonedaLiquidacion MonedaNegociacion EsContraparte '
              'Relacion Emisor Nemotecnico TipoPersona CodigoTipoComprobante FechaLiquidacion=dsql FechaComprobante=ts EstadoComprobante '
              'EsOtrosConceptos EsAPlazo CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion CostingCode4=s:Bolsa '
              'EsVentaCorta EsArbitraje DocumentoIdentidadContraparte=cfgDoc NombreContraparte=cfgNom ')
CFG_SETUP = ['config("PLATAFORMA", "CON.DocumentoIdentidadContraparte", CFG_DOC);',
             'config("PLATAFORMA", "CON.NombreContraparte", CFG_NOM);']

def cfg_tests(cls, lines, datos):
    return '''    @Test
    @DisplayName("CONT-E-@ID@: CON.DocumentoIdentidadContraparte / CON.NombreContraparte absent -> both datos values empty, not an error")
    void contE@ID@_contraparteConfigDefaultsToEmpty() {
        // Legacy: %s.java:%s (getParametroConfiguracionString in try/catch, both start as "")
        config("PLATAFORMA", "CON.DocumentoIdentidadContraparte", "");
        config("PLATAFORMA", "CON.NombreContraparte", "");
        rows("%s", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        verify(plataforma, org.mockito.Mockito.atLeastOnce()).obtenerParametroConfiguracion(eq(ID_EMPRESA), eq("PLATAFORMA"), eq("CON.DocumentoIdentidadContraparte"), eq(""));
        verify(plataforma, org.mockito.Mockito.atLeastOnce()).obtenerParametroConfiguracion(eq(ID_EMPRESA), eq("PLATAFORMA"), eq("CON.NombreContraparte"), eq(""));
        assertThat(salida.tabDatosMovimiento())
                .filteredOn(d -> d.tipoDato().equals("DocumentoIdentidadContraparte") || d.tipoDato().equals("NombreContraparte"))
                .extracting(TabDatoMovimientoRef::valorVarChar).containsExactly("", "");
        assertThat(salida.erroresContador()).isZero();
    }
''' % (cls, lines, datos)

FA = 'CentralizaFacturas'
SPECS.append(dict(
    cls=FA, passLines='126,133,238', setup=CFG_SETUP,
    variants=[
        dict(label='LocalizacionSistema CL', queries=[
            ('movimientos', FA + '_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '119-127'),
            ('eventos', FA + '_SelectEventosChile.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '130-134'),
            ('datosMovimiento', FA + '_SelectDatosMovimientoChile.sql', P('FechaContable:F IdEmpresa:E IdTipoCentralizacion:T DocumentoIdentidad:D'), '136-140')],
            notIssued=[FA + '_SelectEventosPeru.sql', FA + '_SelectDatosMovimientoPeru.sql'],
            datos=FA + '_SelectDatosMovimientoChile.sql', maplines='110-113,167-212,231-235',
            fields=FAC_COMMON + 'EstadoSII ClienteBancario AreaCliente'),
        dict(label='LocalizacionSistema PE', suffix='Peru', setup=['localizacion("PE");'], queries=[
            ('movimientos', FA + '_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:""'), '143-151'),
            ('eventos', FA + '_SelectEventosPeru.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:""'), '154-158'),
            ('datosMovimiento', FA + '_SelectDatosMovimientoPeru.sql', P('FechaContable:F IdEmpresa:E IdTipoCentralizacion:T DocumentoIdentidad:""'), '160-164')],
            notIssued=[FA + '_SelectEventosChile.sql', FA + '_SelectDatosMovimientoChile.sql'],
            datos=FA + '_SelectDatosMovimientoPeru.sql', maplines='167-212,215-230',
            fields=FAC_COMMON + 'EstadoSUNAT TipoBonoIRF CodigoIFRS OrigenInstrumento IndicadorRelacionadoGrupo Plaza PactoAndino FormadorMercado '
                   'PresenciaBursatil TipoInstrumentoVenta TipoInstrumentoCompra DocumentoIdentidadCAVALI OpCentros CostingCode5=s:OpCentros')],
    extra=cfg_tests(FA, '107-117,211-212', FA + '_SelectDatosMovimientoChile.sql') + '''    @Test
    @DisplayName("CONT-E-@ID@: LocalizacionSistema other than CL/PE -> no query at all and empty output")
    void contE@ID@_otherLocalizacionProducesNothing() {
        // Legacy: CentralizaFacturas.java:119,143 (both branches skipped; P_TAB* keep the empty lists of the ctor)
        localizacion("CO");
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(issuedSql()).isEmpty();
        verify(repository, never()).queryMovimientos(any(), any(), any());
        assertThat(salida.tabMovimientos()).isEmpty();
        assertThat(salida.tabEventos()).isEmpty();
        assertThat(salida.tabDatosMovimiento()).isEmpty();
        assertThat(salida.resultados()).isEmpty();
    }
''',
    persistPrep=['rows("%s_SelectDatosMovimientoChile.sql", List.of(fullRow()));' % FA]))

FCP = 'CentralizaFacturasCtaProp'
std(FCP,
    ('CentralizaFacturas_SelectMovimientosCtaProp.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '25,97-102'),
    ('CentralizaFacturas_SelectEventosCtaProp.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '26,107-110'),
    ('CentralizaFacturas_SelectDatosMovimientoCtaProp.sql', P('FechaContable:F IdEmpresa:E DocumentoIdentidad:D'), '27,114-117'),
    'TipoDocumento CompraVenta Operacion Producto ClaseInstrumento TipoInstrumento CondicionLiquidacion MonedaComprobante Bolsa '
    'DocumentoIdentidad NombreCliente Portafolio Concepto NombreConcepto Agente Folio=svL FolioDTE=svL NombreAgente SucursalAgente '
    'NombreSucursalAgente CentroCostoAgente EstadoSII EsExento Moneda MonedaLiquidacion ClaseEmisor Emisor TipoEmisor Nemotecnico Relacion '
    'TipoPersona CodigoIFRS CodigoTipoComprobante FechaLiquidacion=dsql TipoComprobante FechaComprobante=ts '
    'DocumentoIdentidadContraparte=cfgDoc NombreContraparte=cfgNom IndicadorOrdenDirecta TipoReajuste AreaCliente FolioDTE=svL',
    '88-91,120-163', '102,110,165', setup=CFG_SETUP,
    extra=cfg_tests(FCP, '85-95,157-158', 'CentralizaFacturas_SelectDatosMovimientoCtaProp.sql'))

FD = 'CentralizaFacturasDetallada'
SPECS.append(dict(
    cls=FD, passLines='110,117,209',
    variants=[dict(label='LocalizacionSistema CL', queries=[
        ('movimientos', FD + '_SelectMovimientos.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '106-110'),
        ('eventos', FD + '_SelectEventosChile.sql', P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D'), '113-117'),
        ('datosMovimiento', FD + '_SelectDatosMovimientoChile.sql', P('FechaContable:F IdEmpresa:E IdTipoCentralizacion:T DocumentoIdentidad:D'), '119-123')],
        datos=FD + '_SelectDatosMovimientoChile.sql', maplines='139-206',
        fields='TipoDocumento FechaDocumento CompraVenta Operacion Producto ClaseInstrumento TipoInstrumento CondicionLiquidacion '
               'MonedaComprobante Bolsa DocumentoIdentidad NombreCliente Portafolio Concepto NombreConcepto Agente NombreAgente SucursalAgente '
               'NombreSucursalAgente CentroCostoAgente FolioPoliza=svL Folio=svL EsExento Moneda MonedaLiquidacion EsContraparte Relacion '
               'Emisor Nemotecnico TipoPersona CodigoTipoComprobante FechaLiquidacion=dsql TipoComprobante FechaComprobante=ts '
               'EstadoComprobante EsOtrosConceptos EsAPlazo CostingCode=s:CentroCostoAgente CostingCode2=s:Producto CostingCode3=s:Relacion '
               'CostingCode4=s:Bolsa EsVentaCorta EsArbitraje CorredoraContraparte NombreCorredoraContraparte EstadoSII')],
    extra='''    @Test
    @DisplayName("CONT-E-@ID@: LocalizacionSistema not CL -> only the movimientos query runs; no eventos, no datos-movimiento")
    void contE@ID@_nonChileOnlyMovimientos() {
        // Legacy: CentralizaFacturasDetallada.java:106-110 (always), :113-124 (CL only; PE branch commented out at :126-137)
        localizacion("PE");
        rows("CentralizaFacturasDetallada_SelectDatosMovimientoChile.sql", List.of(fullRow()));
        SalidaCentralizacion salida = service.centralizar(entrada("S"));
        assertThat(paramsOf("CentralizaFacturasDetallada_SelectMovimientos.sql"))
                .isEqualTo(Map.of("FechaContable", FECHA, "IdTipoCentralizacion", ID_TIPO, "DocumentoIdentidad", DOC_CORREDORA));
        assertNotIssued("CentralizaFacturasDetallada_SelectEventosChile.sql");
        assertNotIssued("CentralizaFacturasDetallada_SelectDatosMovimientoChile.sql");
        assertThat(salida.tabEventos()).isEmpty();
        assertThat(salida.tabDatosMovimiento()).isEmpty();
    }

    @Test
    @DisplayName("CONT-E-@ID@: no configuration parameter is read (contraparte comes from the row, not from CON.*)")
    void contE@ID@_noConfigRead() {
        // Legacy: CentralizaFacturasDetallada.java:96-213 has no ConfiguracionSistema call
        service.centralizar(entrada("S"));
        verify(plataforma, never()).obtenerParametroConfiguracion(any(), any(), eq("CON.DocumentoIdentidadContraparte"), any());
        verify(plataforma, never()).obtenerParametroConfiguracion(any(), any(), eq("CON.NombreContraparte"), any());
    }
'''))

FVC = 'CentralizaFacturasVentaCorta'
FVP = P('FechaContable:F IdTipoCentralizacion:T DocumentoIdentidad:D IdClasificacionTipoInstrumento:C')
sp = std(FVC,
    ('CentralizaFacturaVentaCorta_SelectMovimientos.sql', FVP, '26,131-136'),
    ('CentralizaFacturaVentaCorta_SelectEventos.sql', FVP, '27,131-137'),
    ('CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql', P('FechaContable:F DocumentoIdentidad:D IdClasificacionTipoInstrumento:C IdEmpresa:E'), '28,138-140'),
    'TipoDocumento FechaDocumento CompraVenta Operacion Producto ClaseInstrumento TipoInstrumento CondicionLiquidacion MonedaComprobante '
    'Bolsa DocumentoIdentidad NombreCliente Portafolio Concepto NombreConcepto Agente NombreAgente SucursalAgente NombreSucursalAgente '
    'FolioPoliza=svL Folio=svL EsExento Moneda MonedaLiquidacion EsContraparte Relacion Emisor Nemotecnico TipoPersona CodigoTipoComprobante '
    'FechaLiquidacion=dsql FechaComprobante=ts EstadoComprobante EsOtrosConceptos EsAPlazo EsVentaCorta EsArbitraje '
    'DocumentoIdentidadContraparte=cfgDoc NombreContraparte=cfgNom EstadoSII', '117-120,142-183', '136,137,185',
    setup=CFG_SETUP + ['rows("CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql", List.of(row("ClasificacionTipoInstrumento", CLASIF)));'],
    extra=cfg_tests(FVC, '114-124,180-181', 'CentralizaFacturaVentaCorta_SelectMovimientosDatos.sql'))
sp['variants'][0]['queries'].insert(0, ('clasificacionTipoInstrumento', 'CentralizaFacturaVentaCorta_SelectMercadoCentralizacion.sql', P('IdTipoCentralizacion:T'), '29,126-128'))

# ---------------------------------------------------------------- FusionCarterasRFIIF
std('CentralizaFusionCarterasRFIIF',
    ('CentralizaFusionCarterasRFIIF_SelectMovimientos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T p_DocumentoIdentidadCorredora:D'), '72-77'),
    ('CentralizaFusionCarterasRFIIF_SelectEventos.sql', P('p_IdEmpresa:E p_FechaContable:F p_IdTipoCentralizacion:T p_DocumentoIdentidadCorredora:D'), '81-85'),
    ('CentralizaFusionCarterasRFIIF_SelectDatosMovimiento.sql', P('p_IdEmpresa:E p_FechaContable:F p_DocumentoIdentidadCorredora:D'), '89-93'),
    'IdMovimiento=svL DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera '
    'Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente '
    'Relacion Bolsa FechaLiquidacion=dsql TipoPersona FechaOperacion=dsql IdConcepto DocumentoIdentidadCAVALI TipoEmisor Mesa',
    '96-128', '77,85,130')
