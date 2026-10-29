# WP-F specs, transcribed from the LEGACY classes (oracle).

N = 'CentralizaInteresReajusteCarteraTerminoRFIF'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_DocumentoIdentidadCorredora','76-81'),
         q(N+'_SelectEventos.sql','EVE','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_DocumentoIdentidadCorredora','85-89'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_FechaContable p_DocumentoIdentidadCorredora LocSistema','93-98')],
     "IdMovimiento:VALUEOF_LONG DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa FechaLiquidacion:DATE TipoPersona FechaOperacion:DATE IdConcepto DocumentoIdentidadCAVALI TipoEmisor Mesa FolioOperacion:VALUEOF_LONG", '101-133')

N = 'CentralizaInteresTomaDepositosAutodesembolso'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion','71-76'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion','80-84'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa','88-91')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa FechaLiquidacion:DATE FechaOperacion:DATE Concepto TipoPersona CostingCode<-CentroCostoAgente CostingCode2<-Producto CostingCode3<-Relacion CostingCode4<-Bolsa CostingCode5:CONST", '95-127')

N = 'CentralizaLiquidacionFacturas'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdTipoCentralizacion','79-81'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdTipoCentralizacion Sistema','73,86-89'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable','93-94')],
     "TipoDocumento Folio:VALUEOF_LONG FechaDocumento CompraVenta Operacion Producto ClaseInstrumento TipoInstrumento MonedaComprobante Bolsa DocumentoIdentidad NombreCliente Portafolio Concepto NombreConcepto Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente EstadoSUNAT DocumentoIdentidadCAVALI EsExento Moneda MonedaLiquidacion EsContraparte Relacion ClaseEmisor Emisor Nemotecnico CondicionLiquidacion CodigoIFRS", '98-129')

MMV = "DocumentoIdentidad Portafolio EsCuentaPropia:VALUEOF_STR EsRelacionado:VALUEOF_STR TipoCartera FechaOperacion:DATE FechaVencimientoOperacion:TOSTRING_DATE FechaLiquidacion:DATE FolioOperacion:VALUEOF_LONG ClaseInstrumento TipoInstrumento Nemotecnico<-Nemo Boveda Emisor TipoEmisor IdTipoReajuste<-TipoReajuste FechaVencimientoInstrumento:TOSTRING_DATE CodigoIFRS Moneda ClaseEmisor:TOSTRING_STR"
N = 'CentralizaMayorMenorValorRFIF'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_IdInforme','62-67'),
         q(N+'_SelectEventos.sql','EVE','p_IdEmpresa p_IdTipoCentralizacion p_IdInforme','71-74'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_DocumentoIdentidad p_IdInforme','78-81')], MMV, '85-104')
N = 'CentralizaMayorMenorValorRV'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_IdInforme','64-69'),
         q(N+'_SelectEventos.sql','EVE','p_IdEmpresa p_IdTipoCentralizacion p_IdInforme','73-76'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_DocumentoIdentidad p_IdInforme','80-83')], MMV, '87-106')

N = 'CentralizaOrdenDirecta'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion','65-69'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion MonedaSistema','62,73-77'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa','81-83')],
     "TipoDocumento FolioTransaccion CompraVenta CodigoBolsa TipoInstrumento EsCuentaPropia FamiliaInstrumento FechaLiquidacion:DATE Nemo Moneda Producto Estado", '88-99')

N = 'CentralizaPrestamoInicio'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','IdEmpresa FechaContable IdTipoCentralizacion IdClasificacionTipoInstrumento','72-83'),
         q(N+'_SelectEventos.sql','EVE','IdEmpresa FechaContable IdTipoCentralizacion IdClasificacionTipoInstrumento','90-95'),
         q(N+'_SelectDatosMovimiento.sql','DAT','IdEmpresa FechaContable LocSistema IdClasificacionTipoInstrumento','102-108')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda EsRelacionado EsCuentaPropia Producto Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente TipoPrestamo TipoPrestatario CondicionLiquidacion FechaLiquidacion:TOSTRING_STR FechaOperacion:TOSTRING_STR", '116-138',
     specials='''
    // Legacy CentralizaPrestamoInicio.java:70,82-86,94-98,107-111: when FechaContable != PLA.FechaSistema the *Historicos SQL run instead.
    @Test
    @DisplayName("{id}: FechaContable distinta de PLA.FechaSistema usa las consultas historicas (legacy :70,82-111)")
    void {mid}_fechaHistoricaUsaConsultasHistoricas() {
        Escenario e = new Escenario();
        e.fechaSistema = "2025-03-11";
        e.ejecutar(TIPO, "S");
        Map<String, Object> pMov = Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE,
                "IdTipoCentralizacion", ID_TIPO, "IdClasificacionTipoInstrumento", "PRE");
        Map<String, Object> pDat = Spec.params("IdEmpresa", ID_EMPRESA, "FechaContable", FECHA_CONTABLE,
                "LocSistema", LOC_SISTEMA, "IdClasificacionTipoInstrumento", "PRE");
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectMovimientosHistoricos.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pMov);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectEventosHistoricos.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pMov);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectDatosMovimientoHistorico.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(pDat);
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectMovimientos.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectEventos.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaPrestamoInicio_SelectDatosMovimiento.sql")).isEmpty();
    }

    // Legacy CentralizaPrestamoInicio.java:21-27 loads the clasificacion SQL and the three historic SQL too.
    @Test
    @DisplayName("{id}: SQL de clasificacion e historicos presentes en el classpath (legacy :21-27)")
    void {mid}_recursosSqlHistoricos() {
        ClassLoader cl = getClass().getClassLoader();
        for (String f : List.of("CentralizaPrestamoInicio_SelectClasificacionTipoInstrumento.sql",
                "CentralizaPrestamoInicio_SelectMovimientosHistoricos.sql",
                "CentralizaPrestamoInicio_SelectEventosHistoricos.sql",
                "CentralizaPrestamoInicio_SelectDatosMovimientoHistorico.sql")) {
            assertThat(cl.getResource("sql/" + f) != null || cl.getResource("cl/bcs/contabilidad/sql/" + f) != null)
                    .as(f).isTrue();
        }
    }
''')

N = 'CentralizaPrestamosEmprestitos'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','IdEmpresa FechaContable IdTipoCentralizacion','94-99'),
         q(N+'_SelectEventos.sql','EVE','IdEmpresa FechaContable FechaAnterior IdTipoCentralizacion','78-91,103-108'),
         q(N+'_SelectDatosMovimiento.sql','DAT','IdEmpresa FechaContable LocSistema','112-116')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda EsCuentaPropia EsVentaCorta Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa FechaLiquidacion:DATE FechaOperacion:DATE IdConcepto ClienteBancario", '121-149')

RES = "DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera:VALUEOF_STR TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente Relacion Bolsa FechaOperacion:DATE CostingCode<-CentroCostoAgente CostingCode2<-Producto CostingCode3<-Relacion CostingCode4<-Bolsa CostingCode5:CONST MontoMonedaSistema:DECIMAL TipoOperacionFFMM IdFondo Emisor"
N = 'CentralizaRescateFondoMutuoCuentaPropia'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdTipoCentralizacion IdEmpresa DocumentoIdentidadCorredora','76-81'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdTipoCentralizacion IdMonedaSistema IdEmpresa DocumentoIdentidadCorredora','85-90'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa DocumentoIdentidadCorredora','94-97')],
     RES + " ClienteBancario", '101-135')
N = 'CentralizaRescateFondoMutuoCuentaTercero'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdTipoCentralizacion IdEmpresa DocumentoIdentidadCorredora','77-82'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdTipoCentralizacion IdMonedaSistema IdEmpresa DocumentoIdentidadCorredora','86-91'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa DocumentoIdentidadCorredora','95-98')],
     RES + " FormaPago ClienteBancario", '102-136')

N = 'CentralizaRFIFVencimientos'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdTipoCentralizacion','106-111'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdTipoCentralizacion','116-119'),
         q(N+'_SelectDatosMovimiento.sql','DAT','IdEmpresa FechaContable DocumentoIdentidad LocSistema','123-127')],
     "DocumentoIdentidad Portafolio CentroCosto CodigoAgente CodigoBolsa CodigoIFRS ClaseInstrumento TipoInstrumento CodigoSucursal CodigoTipoOperacion CodSecuencia!:CONST DocumentoIdentidadCAVALI DiasLiquidacion:CONST DiasVencido!:CONST DiasVencimiento!:CONST EmisorInstrumento EstadoMovimiento FamiliaInstrumento FechaEmisionDocumento:DATE FechaLiquidacionDocumento:DATE IndicadorLiquidacion IndicadorSimultanea Nemo ProductoMovimiento RelacionadoEmpresa RelacionCorredora FamiliaTipoInstrumento Relacion ClaseEmisor TipoCartera TipoFamilia TipoReajuste IdCartera!:CONST TipoDocumento VentaCorta TipoCmbteCont FolioCmbteCont Concepto Glosa FechaDeVencimiento:DATE FechaAnterior:DATE TasaMercadoActual!:CONST PrecioMercadoActual!:CONST TasaMercadoAnterior!:CONST PrecioMercadoAnterior!:CONST TasaCompra:DECIMAL PrecioCompra:DECIMAL Moneda TasaCambio:DECIMAL FechaLiquidacion:DATE TipoPersona FechaOperacion:DATE NombreCliente OpCentros TipoEmisor Mesa ClienteBancario", '132-191')

N = 'CentralizaSimultaneaAnticiposVencimientos'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion','70-74'),
         q(N+'_SelectEventos.sql','EVE','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion','78-81'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_FechaContable','85-88')],
     "IdMovimiento:VALUEOF_LONG DocumentoIdentidad NombreCliente PortafolioCliente<-Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera IdEmisor<-Emisor IdTipoReajuste<-TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa FechaLiquidacion:DATE TipoPersona FechaOperacion:DATE Concepto<-IdConcepto CostingCode<-CentroCostoAgente CostingCode2<-Producto CostingCode3<-Relacion CostingCode4<-Bolsa CostingCode5:CONST TipoOperacion ClienteBancario", '92-125')

N = 'CentralizaSimultaneasCuentaPropia'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_IdInforme','69-74'),
         q(N+'_SelectEventos.sql','EVE','p_IdEmpresa p_IdTipoCentralizacion p_IdInforme','78-81'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_DocumentoIdentidad p_IdInforme','85-88')],
     "IdMovimiento:VALUEOF_LONG DocumentoIdentidad NombreCliente PortafolioCliente<-Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera IdEmisor<-Emisor CodigoIFRS TipoCartera Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion Bolsa FechaLiquidacion:DATE TipoPersona FechaOperacion:DATE Concepto<-IdConcepto CostingCode<-CentroCostoAgente CostingCode2<-Producto CostingCode3<-Relacion CostingCode4<-Bolsa CostingCode5:CONST ClienteBancario", '92-122')

N = 'CentralizaSpotIngreso'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion','65-69'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion','73-76'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa','80-82')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera!:CONST Emisor TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente Canal Relacion Bolsa CentroCostoAgente TipoComprobante CompraVenta TipoPersona FechaOperacion ClienteBancario OperacionNoFacturable", '87-113')

N = 'CentralizaSpotUtilidadPerdida'
tipo(N, [q(N+'_ConsultaTablaBloqueo.sql','OTRA','IdBloqueoCentralizacion','88-98'),
         q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion MonedaSistema','125-133'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion MonedaSistema','137-141'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa MonedaSistema','145-149')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera<-Boveda Emisor TipoReajuste CodigoIFRS Agente NombreAgente SucursalAgente NombreSucursalAgente Canal Relacion Bolsa CentroCostoAgente TipoComprobante CompraVenta TipoPersona FechaOperacion ClienteBancario OperacionNoFacturable", '155-181',
     specials='''
    // Legacy CentralizaSpotUtilidadPerdida.java:98-108: an existing lock row aborts with 1 error and UI_CENTRALIZACION_CONTABILIZADA_ANULAR
    // (valor Centralizacion = Nombre from CentralizaGenerico_SelectTipoCentralizacion.sql) and no movement SQL runs.
    @Test
    @DisplayName("{id}: bloqueo existente -> 1 error UI_CENTRALIZACION_CONTABILIZADA_ANULAR y no centraliza (legacy :98-108)")
    void {mid}_bloqueoExistenteAbortaConError() {
        Escenario e = new Escenario();
        e.filas("CentralizaSpotUtilidadPerdida_ConsultaTablaBloqueo.sql",
                List.of(Map.of("IdBloqueoCentralizacion", "470_2025-03-10")));
        e.filas("CentralizaGenerico_SelectTipoCentralizacion.sql",
                List.of(Map.of("IdTipoCentralizacion", ID_TIPO, "Nombre", "SPOT UTILIDAD PERDIDA", "TipoEnvio", "A")));
        e.filas("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql",
                List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).singleElement().asString()
                .startsWith("UI_CENTRALIZACION_CONTABILIZADA_ANULAR").contains("SPOT UTILIDAD PERDIDA");
        assertThat(e.llamadasA("CentralizaGenerico_SelectTipoCentralizacion.sql")).singleElement()
                .extracting(Escenario.SqlCall::params).isEqualTo(Spec.params("IdTipoCentralizacion", ID_TIPO));
        assertThat(e.llamadasA("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql")).isEmpty();
        assertThat(s.tabMovimientos()).isEmpty();
        assertThat(s.resultados()).isEmpty();
    }

    // Legacy CentralizaSpotUtilidadPerdida.java:187-189: any exception inside the try is logged and swallowed;
    // tables filled so far are kept, P_OUT_RESULTADO stays empty, P_OUT_ERRORESCONTADOR stays 0.
    @Test
    @DisplayName("{id}: excepcion en datos movimiento se traga: no propaga, 0 errores, resultados vacios (legacy :187-189)")
    void {mid}_excepcionSeTragaSinErrores() {
        Escenario e = new Escenario();
        e.filas("CentralizaSpotUtilidadPerdida_SelectMovimientos.sql",
                List.of(Filas.movimiento(7001L, EJERCICIO, "CLP", "10.00")));
        e.filas("CentralizaSpotUtilidadPerdida_SelectDatosMovimiento.sql",
                p -> { throw new IllegalStateException("fallo simulado de la consulta de datos"); });
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isZero();
        assertThat(s.resultados()).isEmpty();
        assertThat(s.tabDatosMovimiento()).isEmpty();
        assertThat(s.tabMovimientos()).extracting(m -> m.idMovimiento()).containsExactly(7001L);
    }
''')

N = 'CentralizaTarjetaExistencia'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','IdEmpresa FechaContable IdTipoCentralizacion','62-66'),
         q(N+'_SelectEventos.sql','EVE','IdEmpresa FechaContable IdTipoCentralizacion','70-73'),
         q(N+'_SelectDatosMovimiento.sql','DAT','IdEmpresa FechaContable','77-79')],
     "IdMovimiento:VALUEOF_LONG DocumentoIdentidad NombreCliente PortafolioCliente Moneda Relacion ClaseInstrumento TipoInstrumento Nemo IdCartera!:CONST IdEmisor TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente", '83-105')

TES = "NombreCuentaCorriente TipoDocumento Folio:VALUEOF_LONG FechaDocumento NumeroCuentaCorriente MedioDePago NumeroDocumentoPago FechaLiquidacion:DATE FechaDocumentoPago:TS_O_DEFECTO DocumentoIdentidadCAVALI SerieDocumento Concepto NombreConcepto DocumentoIdentidad NombreCliente Portafolio Moneda CodigoCaja EmitidoA EsBilateral TipoPersona Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente FechaProceso Proceso TipoComprobante Valuta PartidaLiquidacion NumeroComprobante:VALUEOF_LONG AliasBancoCaja Monto:DECIMAL TipoComprobanteAdj FolioComprobanteAdj:VALUEOF_LONG FolioOperacion:VALUEOF_LONG CodigoProducto Nemotecnico Emisor Relacion Valor:DECIMAL TipoCambio:DECIMAL EsContraparte Bolsa ComprobanteTipo FechaMovimiento:DATE MontoMonedaSistema:DECIMAL Referencia3:VALUEOF_LONG EsProveedor:VALUEOF_STR ClienteBancario CondicionLiquidacion FolioDTE:VALUEOF_LONG"

def tes_special(nombre):
    return '''
    // Legacy {N}.java:136-142 reads module "CONTABILIDAD", parameter "CalculaParidadCLPCentralizacionTesoreria" (default "N" on error),
    // and :154-226, 244-260, 287-304, 371-484 convert every non-CLP amount to CLP: factor = TipoCambioMercado of "DO" for USD,
    // of the currency itself otherwise; MontoDestino = MontoOrigen * factor (DECIMAL128) then setScale(0, HALF_UP).
    @Test
    @DisplayName("{id}: CalculaParidadCLPCentralizacionTesoreria=S convierte montos no CLP a CLP con HALF_UP escala 0 (legacy :136-304,371-484)")
    void {mid}_paridadConvierteMontosNoClp() {
        Escenario e = escenarioParidad();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        // 25 USD * 0.5 = 12.5 -> 13 ; 1000 CLP unchanged ; 10 EUR * 1.05 = 10.50 -> 11 (HALF_EVEN would give 10)
        assertThat(s.tabMovimientos()).extracting(m -> m.monto().toPlainString()).containsExactly("13", "1000.00", "11");
        assertThat(s.tabEventos()).extracting(ev -> ev.idEvento() + "=" + ev.montoEvento().toPlainString())
                .containsExactly("SOLICITUD_MONTO_INGRESO=13", "OTRO_EVENTO=25.00", "DOCUMENTO_MONTO_EGRESO=11",
                        "SOLICITUD_MONTO_INGRESO=1000.00");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.idMovimiento() == 8001L
                        && (d.tipoDato().equals("Monto") || d.tipoDato().equals("Valor")))
                .extracting(d -> d.tipoDato() + "=" + d.valorDecimal().toPlainString())
                .containsExactly("Monto=13", "Valor=13");
        assertThat(s.erroresContador()).isZero();
    }

    // Same rows with the flag absent: amounts untouched and no paridad lookup (legacy :154 guard).
    @Test
    @DisplayName("{id}: sin CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria=S los montos no se convierten (legacy :154)")
    void {mid}_sinParidadMontosOriginales() {
        Escenario e = escenarioParidad();
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabMovimientos()).extracting(m -> m.monto().toPlainString()).containsExactly("25.00", "1000.00", "10.00");
        assertThat(e.llamadasA("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql")).isEmpty();
    }

    // Legacy :379-417 first paridad lookups for the first non-CLP movement: IdParametroFinanciero "DO", then the currency;
    // FechaMinima = Fecha - PLA.DiasFechaMinimaCalculoParidad (default 1095) days, FechaMaxima = Fecha.
    @Test
    @DisplayName("{id}: consulta de paridad DO y moneda con ventana de 1095 dias (legacy :379-417)")
    void {mid}_consultaParidadParametros() {
        Escenario e = escenarioParidad();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        e.ejecutar(TIPO, "S");
        List<Escenario.SqlCall> calls = e.llamadasA("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql");
        assertThat(calls).hasSizeGreaterThanOrEqualTo(2);
        assertThat(calls.get(0).params().get("IdParametroFinanciero")).isEqualTo("DO");
        assertThat(String.valueOf(calls.get(0).params().get("FechaMinima"))).isEqualTo("2022-03-11");
        assertThat(String.valueOf(calls.get(0).params().get("FechaMaxima"))).isEqualTo("2025-03-10");
        assertThat(calls.get(1).params().get("IdParametroFinanciero")).isEqualTo("USD");
    }

    // Legacy :189-215, 230-233: a currency without TipoCambioMercado is RECHAZO -> P_OUT_ERRORESCONTADOR = 1,
    // one UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA message (Moneda = EUR), and no movements / events / datos at all.
    @Test
    @DisplayName("{id}: moneda sin tipo de cambio -> 1 error UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA y salida vacia (legacy :189-233)")
    void {mid}_paridadRechazadaAbortaConError() {
        Escenario e = new Escenario();
        e.config.put("CONTABILIDAD|CalculaParidadCLPCentralizacionTesoreria", "S");
        e.filas(MOV, List.of(Filas.movimiento(8003L, EJERCICIO, "EUR", "10.00")));
        e.filas("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql", p -> "DO".equals(p.get("IdParametroFinanciero"))
                ? List.of(paridad("DO", "0.5")) : List.of());
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.erroresContador()).isEqualTo(1L);
        assertThat(s.mensajes()).singleElement().asString()
                .startsWith("UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA").contains("EUR");
        assertThat(s.tabMovimientos()).isEmpty();
        assertThat(s.tabEventos()).isEmpty();
        assertThat(s.tabDatosMovimiento()).isEmpty();
        assertThat(s.resultados()).isEmpty();
        assertThat(e.llamadasA(EVE)).isEmpty();
    }

    // Legacy :315 FechaDocumentoPago: Fecha.toStringTimeStamp (yyyy-MM-dd HH:mm:ss.SSS), null -> "0001-01-01 00:00:00".
    @Test
    @DisplayName("{id}: FechaDocumentoPago con milisegundos y por defecto 0001-01-01 00:00:00 (legacy :315)")
    void {mid}_fechaDocumentoPagoFormato() {
        Escenario e = new Escenario();
        List<Map<String, Object>> rows = Filas.datos(TIPO, 0, false);
        rows.get(1).put("FechaDocumentoPago", null);
        e.filas(DAT, rows);
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.tipoDato().equals("FechaDocumentoPago"))
                .extracting(d -> d.valorVarChar()).containsExactly("2025-03-10 14:05:09.123", "0001-01-01 00:00:00");
    }

    // Legacy :42-45 loads the paridad SQL (and the comprobantes-asociados insert).
    @Test
    @DisplayName("{id}: SQL de paridad presente en el classpath (legacy :45)")
    void {mid}_recursoSqlParidad() {
        String f = "CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql";
        ClassLoader cl = getClass().getClassLoader();
        assertThat(cl.getResource("sql/" + f) != null || cl.getResource("cl/bcs/contabilidad/sql/" + f) != null).as(f).isTrue();
    }

    private static Map<String, Object> paridad(String idMoneda, String tipoCambio) {
        Map<String, Object> r = new HashMap<>();
        r.put("IdMoneda", idMoneda);
        r.put("Fecha", java.sql.Timestamp.valueOf("2025-03-10 00:00:00"));
        r.put("ParidadMercado", BigDecimal.ONE);
        r.put("TipoCambioMercado", new BigDecimal(tipoCambio));
        return r;
    }

    private static Escenario escenarioParidad() {
        Escenario e = new Escenario();
        e.filas(MOV, List.of(
                Filas.movimiento(8001L, EJERCICIO, "USD", "25.00"),
                Filas.movimiento(8002L, EJERCICIO, "CLP", "1000.00"),
                Filas.movimiento(8003L, EJERCICIO, "EUR", "10.00")));
        e.filas(EVE, List.of(
                Filas.evento(8001L, "SOLICITUD_MONTO_INGRESO", "25.00", "USD"),
                Filas.evento(8001L, "OTRO_EVENTO", "25.00", "USD"),
                Filas.evento(8003L, "DOCUMENTO_MONTO_EGRESO", "10.00", "EUR"),
                Filas.evento(8002L, "SOLICITUD_MONTO_INGRESO", "1000.00", "CLP")));
        List<Map<String, Object>> datos = Filas.datos(TIPO, 0, false);
        datos.get(0).put("IdMovimiento", 8001L);
        datos.get(0).put("Moneda", "USD");
        datos.get(0).put("Monto", new BigDecimal("25.00"));
        datos.get(0).put("Valor", new BigDecimal("25.00"));
        datos.get(1).put("Moneda", "CLP");
        e.filas(DAT, datos);
        e.filas("CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql", p -> switch (String.valueOf(p.get("IdParametroFinanciero"))) {
            case "DO" -> List.of(paridad("DO", "0.5"));
            case "EUR" -> List.of(paridad("EUR", "1.05"));
            default -> List.<Map<String, Object>>of();
        });
        return e;
    }
'''.replace('{N}', nombre)

for N, extra in (('CentralizaTesoreria', ' OperacionNoFacturable:VALUEOF_STR'), ('CentralizaTesoreriaFormasDePago', '')):
    tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion LocSistema','145-151'),
             q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion LocSistema','236-242'),
             q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa FechaSistema LocSistema','133,265-270'),
             q('CentralizaTesoreria_SelectDatosMovimiento_Documentos_Partidas.sql','DAT','FechaContable IdEmpresa FechaSistema LocSistema','281'),
             q('CentralizaTesoreria_SelectDatosMovimiento_Solicitudes_Partidas.sql','DAT','FechaContable IdEmpresa FechaSistema LocSistema','282')],
         TES + extra, '307-360' if extra else '307-361', specials=tes_special(N))

N = 'CentralizaTomaDepositosAutodesembolso'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','FechaContable IdEmpresa IdTipoCentralizacion','96-100'),
         q(N+'_SelectEventos.sql','EVE','FechaContable IdEmpresa IdTipoCentralizacion','104-107'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa','111-114')],
     "DocumentoIdentidad Portafolio CentroCosto CodigoAgente CodigoBolsa CodigoIFRS TipoInstrumento CodigoSucursal CodigoTipoOperacion CodSecuencia!:CONST DiasLiquidacion DiasVencido DiasVencimiento!:CONST EmisorInstrumento EstadoMovimiento FamiliaInstrumento FechaEmisionDocumento:DATE FechaLiquidacionDocumento:DATE IndicadorLiquidacion IndicadorSimultanea Nemo ProductoMovimiento RelacionadoEmpresa RelacionCorredora TipoEmisor FamiliaTipoInstrumento Relacion ClaseEmisor TipoCartera TipoFamilia TipoReajuste IdCartera!:CONST TipoDocumento Concepto Glosa FechaDeVencimiento:DATE FechaAnterior:DATE FechaActual:DATETIME Moneda FechaLiquidacion:DATE TipoPersona FechaOperacion:DATE NombreCliente DocumentoIdentidadCAVALI TasaMercadoActual:DECIMAL PrecioMercadoActual:DECIMAL TasaMercadoAnterior:DECIMAL PrecioMercadoAnterior:DECIMAL TasaEmision TasaCompra:DECIMAL PrecioCompra:DECIMAL TasaCambio:DECIMAL", '118-173',
     specials='''
    // Legacy CentralizaTomaDepositosAutodesembolso.java:159 FechaActual = Fecha.toStringDatetime(Timestamp) -> yyyy-MM-dd HH:mm:ss (no millis).
    @Test
    @DisplayName("{id}: FechaActual se formatea yyyy-MM-dd HH:mm:ss sin milisegundos (legacy :159)")
    void {mid}_fechaActualSinMilisegundos() {
        Escenario e = new Escenario();
        e.filas(DAT, Filas.datos(TIPO, 0, false));
        SalidaCentralizacion s = e.ejecutar(TIPO, "S");
        assertThat(s.tabDatosMovimiento()).filteredOn(d -> d.tipoDato().equals("FechaActual"))
                .extracting(d -> d.valorVarChar()).containsExactly("2025-03-10 14:05:09", "2025-03-10 15:05:09");
    }
''')

N = 'CentralizaVencimientoPactos'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion','86-90'),
         q(N+'_SelectEventosSubyacente.sql','EVE','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion p_FechaAnterior','69-83,103-111'),
         q(N+'_SelectEventosOperacion.sql','EVE','p_IdEmpresa p_FechaContable p_IdTipoCentralizacion','113-114'),
         q(N+'_SelectDatosMovimiento.sql','DAT','p_IdEmpresa p_FechaContable','118-120')],
     "IdMovimiento:VALUEOF_LONG DocumentoIdentidad NombreCliente PortafolioCliente<-Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor IdTipoReajuste<-TipoReajuste TipoEmisor CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion CompraVenta Mesa", '124-148',
     specials='''
    // Legacy CentralizaVencimientoPactos.java:94-112: CONTABILIDAD|DetalleEventoCentralizacionVP equalsIgnoreCase "S"
    // switches the subyacente SQL to *_SelectEventosSubyacenteDetalle.sql (same params, incl. p_FechaAnterior).
    @Test
    @DisplayName("{id}: DetalleEventoCentralizacionVP=s usa SelectEventosSubyacenteDetalle (legacy :94-112)")
    void {mid}_detalleEventoUsaConsultaDetalle() {
        Escenario e = new Escenario();
        e.config.put("CONTABILIDAD|DetalleEventoCentralizacionVP", "s");
        e.ejecutar(TIPO, "S");
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosSubyacenteDetalle.sql")).singleElement()
                .extracting(Escenario.SqlCall::params)
                .isEqualTo(Spec.params("p_IdEmpresa", ID_EMPRESA, "p_FechaContable", FECHA_CONTABLE,
                        "p_IdTipoCentralizacion", ID_TIPO, "p_FechaAnterior", FECHA_ANTERIOR));
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosSubyacente.sql")).isEmpty();
        assertThat(e.llamadasA("CentralizaVencimientoPactos_SelectEventosOperacion.sql")).hasSize(1);
    }
''')

N = 'CentralizaVentaTerminoSpread'
tipo(N, [q(N+'_SelectMovimientos.sql','MOV','IdEmpresa FechaContable IdTipoCentralizacion','77-81'),
         q(N+'_SelectEventos.sql','EVE','IdEmpresa FechaContable IdTipoCentralizacion DocumentoIdentidad','85-89'),
         q(N+'_SelectDatosMovimiento.sql','DAT','FechaContable IdEmpresa DocumentoIdentidad LocSistema','93-97')],
     "DocumentoIdentidad NombreCliente Portafolio Moneda Producto ClaseInstrumento TipoInstrumento Nemo Boveda IdCartera Emisor TipoReajuste CodigoIFRS TipoCartera ClaseEmisor Agente NombreAgente SucursalAgente NombreSucursalAgente CentroCostoAgente Relacion:VALUEOF_STR Bolsa FechaLiquidacion:DATE FechaOperacion:DATE TipoPersona TipoEmisor Mesa FolioOperacion:VALUEOF_LONG ClienteBancario", '101-131')
