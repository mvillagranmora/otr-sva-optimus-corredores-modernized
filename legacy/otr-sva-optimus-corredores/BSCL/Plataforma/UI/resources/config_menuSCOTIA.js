//---------------------------------------------
/*
 /$$$$$$$$ /$$   /$$ /$$       /$$             /$$      /$$ /$$$$$$$$ /$$   /$$ /$$   /$$
| $$_____/| $$  | $$| $$      | $$            | $$$    /$$$| $$_____/| $$$ | $$| $$  | $$
| $$      | $$  | $$| $$      | $$            | $$$$  /$$$$| $$      | $$$$| $$| $$  | $$
| $$$$$   | $$  | $$| $$      | $$            | $$ $$/$$ $$| $$$$$   | $$ $$ $$| $$  | $$
| $$__/   | $$  | $$| $$      | $$            | $$  $$$| $$| $$__/   | $$  $$$$| $$  | $$
| $$      | $$  | $$| $$      | $$            | $$\  $ | $$| $$      | $$\  $$$| $$  | $$
| $$      |  $$$$$$/| $$$$$$$$| $$$$$$$$      | $$ \/  | $$| $$$$$$$$| $$ \  $$|  $$$$$$/
|__/       \______/ |________/|________/      |__/     |__/|________/|__/  \__/ \______/ 
*/
//---------------------------------------------
var BCS_FULLMENU = [
    //SOCIO NEGOCIO
    {
    	WIN: BCS_VENTANA.MODULO_SOCIO_NEGOCIO , TITULO: 'UI_SOCIO_NEGOCIO',
    	SUBMENU:  [
    	      { SEPARATE: true , TITULO: 'UI_INFORMACION' },
    	      { WIN: BCS_VENTANA.AgenteConsultarResumemEjecutivo , TITULO: 'UI_RESUMEN_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.ConsultarSocioNegocio , TITULO: 'UI_GESTION_SOCIO_NEGOCIO' },   
    	      { WIN: BCS_VENTANA.AgenteConsultaAvanzadaClientes , TITULO: 'UI_CONSULTA_AVANZADA' }, 
    	      { WIN: BCS_VENTANA.ResumenInversionCliente , TITULO: 'UI_RESUMEN_INVERSION_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.ImprimirArchivosContratos , TITULO: 'UI_IMPRIMIR_ARCHIVOS_CONTRATOS' },
    	      { SEPARATE: true , TITULO: 'UI_INGRESOS' },
    	      { WIN: BCS_VENTANA.CrearCliente , TITULO: 'UI_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.CrearPersona , TITULO: 'UI_PERSONA' }, 
    	      { WIN: BCS_VENTANA.CrearEmpleado , TITULO: 'UI_EMPLEADO' }, 
    	      { WIN: BCS_VENTANA.CrearContraparte , TITULO: 'UI_CONTRAPARTE' }, 
    	      { WIN: BCS_VENTANA.CrearProveedor , TITULO: 'UI_PROVEEDOR' },
    	      { WIN: BCS_VENTANA.CrearClienteNomina , TITULO: 'UI_CLIENTE_NOMINA' },
      	      { WIN: BCS_VENTANA.Movimientos, TITULO: 'UI_CLIENTES_MOVIMIENTOS' },
			  { WIN: BCS_VENTANA.BlotterAdministrarMatrizNOAPV , TITULO: 'UI_MANTENEDOR_MATRIZ_NO_APV' },
    	      { SEPARATE: true , TITULO: 'UI_UTILITARIOS' },
    	      { WIN: BCS_VENTANA.ConsultarSupervisor , TITULO: 'UI_SUPERVISORES' },
    	      { WIN: BCS_VENTANA.ConsultarEjecutivo , TITULO: 'UI_EJECUTIVOS' },
    	      { WIN: BCS_VENTANA.CarteraEjecutivo , TITULO: 'UI_CARTERAS_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.BloqueoMasivoClientes , TITULO: 'UI_BLOQUEO_DESBLOQUEO_MASIVO' },
    	      { WIN: BCS_VENTANA.ActivacionMasivaClientes , TITULO: 'UI_ACTIVACION_MASIVA' },
    	      { WIN: BCS_VENTANA.BloqueoAutomaticoClientes , TITULO: 'UI_BLOQUEO_AUTOMATICO' },
    	      { WIN: BCS_VENTANA.ConsultarNotificacionCliente , TITULO: 'UI_CONSULTAR_NOTIFICACION_CLIENTE' },
    	      { WIN: BCS_VENTANA.GestionEnrolamiento , TITULO: 'UI_GESTION_ENROLAMIENTO' },
    	      { WIN: BCS_VENTANA.MantenedorPortafolio, TITULO: 'UI_MANTENEDOR_PORTAFOLIO' },
    	      { WIN: BCS_VENTANA.CargaMasivaSocioNegocio , TITULO: 'UI_CARGA_MASIVA_SOCIO_NEGOCIO' },
    	      { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.ReporteCajaAgente 					,TITULO: 'UI_REPORTE_CAJA_POR_AGENTE' },
    	      { WIN: BCS_VENTANA.InformacionCliente 				,TITULO: 'UI_REPORTE_INFORMACION_CLIENTE' },
    	      { WIN: BCS_VENTANA.ReporteFlujoAportesRescates 		,TITULO: 'UI_REPORTE_FLUJO_APORTE_RESCATE' },
    	      { WIN: BCS_VENTANA.ForwardVencer 						,TITULO: 'UI_REPORTE_FWD_VENCER' },
    	      { WIN: BCS_VENTANA.ReporteGestionClienteInstrumento 	,TITULO: 'UI_REPORTE_GESTION_CLIENTE_INSTRUMENTO' },

			  { SEPARATE: true , TITULO: 'UI_CONFIGURACION' },
    	      { WIN: BCS_VENTANA.ConsultarGruposClientes, TITULO: 'UI_GRUPOS_DE_CLIENTES' }
			  
    	]    		
    },
    {
    	WIN: BCS_VENTANA.MODULO_MERCADO , TITULO: 'UI_INFORMACION_MERCADO',
    	SUBMENU:  [
    		{ WIN: BCS_VENTANA.ConsultarPreciosInstrumentos , TITULO: 'UI_PRECIOS_DE_INSTRUMENTOS' },
    		{ WIN: BCS_VENTANA.ConsultarPreciosSistema , TITULO: 'UI_CONSULTAR_PRECIOS_SISTEMA' }
    		]
    },
    //ORDENES
    {    	
    	WIN: BCS_VENTANA.MODULO_ORDEN , TITULO: 'UI_OPERACIONES_EN_RUEDA',
    	SUBMENU:  [
    		{ WIN: BCS_VENTANA.BlotterOrdenes , TITULO: 'UI_GESTION_ORDENES' },
    		{ WIN: BCS_VENTANA.ResumenOperacionesDiarias , TITULO: 'UI_RESUMEN_OPERACIONES_DIARIAS' },
    		{ WIN: BCS_VENTANA.ConsultarPosicion , TITULO: 'UI_CONSULTA_DE_POSICION' },
			{ WIN: BCS_VENTANA.ConsultarHistorialTransacciones, TITULO: 'UI_CONSULTA_HISTORIAL_TRANSACCIONES'},
		{ WIN: BCS_VENTANA.InformeOrdenesConfirmadas , TITULO: 'UI_INFORME_ORDENES_CONFIRMADAS' },
    	      { SEPARATE: true , TITULO: 'UI_INGRESOS' },
    	      { WIN: BCS_VENTANA.IngresarOrdenMain , TITULO: 'UI_INGRESAR_ORDEN' },
			  { WIN: BCS_VENTANA.IngresarOrdenRF , TITULO: 'UI_INGRESAR_ORDEN_RF', QA: true  },
    	      { WIN: BCS_VENTANA.CrearOrdenSimultanea , TITULO: 'UI_INGRESAR_ORDEN_SIMULTANEA' }, 
			{ WIN: BCS_VENTANA.IngresarOrdenOP , TITULO: 'UI_INGRESAR_ORDEN_OP' },			  
	      		{ WIN: BCS_VENTANA.IngresarOrdenFU , TITULO: 'UI_INGRESAR_ORDEN_FU' },    
    	      { WIN: BCS_VENTANA.CrearTransaccion , TITULO: 'UI_INGRESAR_TRANSACCION' }, 
    	      { SEPARATE: true , TITULO: 'UI_OPERACIONES' },
    	      { WIN: BCS_VENTANA.InterfazAsignacion , TITULO: 'UI_INTERFAZ_ASIGNACION' },
    	      { WIN: BCS_VENTANA.InterfazRefacturacion , TITULO: 'UI_INTERFAZ_REFACTURACION' },
    	      { WIN: BCS_VENTANA.AsignacionPropCrono , TITULO: 'UI_ASIGNACION_AUTOMATICA' }, 
    	      { WIN: BCS_VENTANA.EjecutarDesasignacion , TITULO: 'UI_EJECUTAR_DESASIGNACION' }, 
    	      { WIN: BCS_VENTANA.BlotterOrdenesNegociacion , TITULO: 'UI_NEGOCIACION_ORDENES' }, 
    	      { WIN: BCS_VENTANA.EnvioFacturarMasivo , TITULO: 'UI_ENVIO_FACTURACION' }, 
    	      { WIN: BCS_VENTANA.CargaArchivosOrdenes , TITULO: 'UI_CARGA_ARCHIVOS_OPERACIONES_RUEDA' }, 
			  { WIN: BCS_VENTANA.VencimientoOrdenes , TITULO: 'UI_VENCIMIENTO_ORDENES' },
			  { WIN: BCS_VENTANA.OficializacionOrdenes , TITULO: 'UI_OFICIALIZAR_ORDENES' },
    	      //{ WIN: BCS_VENTANA.GenerarInformeOperacionesRueda , TITULO: 'UI_GENERAR_INFORMES' }, 
    	      { WIN: BCS_VENTANA.AsignacionMasivaPrecioPromedio , TITULO: 'UI_ASIGNACION_MASIVA_PRECIO_PROMEDIO' },
    	      { SEPARATE: true , TITULO: 'UI_APERTURA_CIERRE' },
    	      { WIN: BCS_VENTANA.AperturaModuloOrdenes , TITULO: 'UI_APERTURA_MODULO' },
    	      { WIN: BCS_VENTANA.CuadraturaDiariaOperaciones , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_CONFIGURACION' },
    	      { WIN: BCS_VENTANA.AdministrarServTransacciones , TITULO: 'UI_OPERADORES_RUEDA' },
    	      { WIN: BCS_VENTANA.ConsultarCorredorCorresponsal , TITULO: 'UI_CORREDORES_CORRESPONSALES' },
    	      { WIN: BCS_VENTANA.ConsultarGruposPortafolio , TITULO: 'UI_GRUPOS_DE_PORTAFOLIOS' },
    	      { WIN: BCS_VENTANA.MaestroLibroOrdenes , TITULO: 'UI_LIBROS_DE_ORDENES' },
    	      { WIN: BCS_VENTANA.DesbloquearTransaccionesSistema , TITULO: 'UI_DESBLOQUEAR_OPERACIONES' },
    	      { WIN: BCS_VENTANA.GestionFix , TITULO: 'UI_GESTION_FIX' , QA: true },
			  { WIN: BCS_VENTANA.ConfiguracionAsignacionDirecta , TITULO: 'UI_ASIGNACION_DIRECTA' },
			  { WIN: BCS_VENTANA.ShortName , TITULO: 'UI_SHORTNAME' },
    	]    	    	
    },
    //FFMM
	/*
    {    	
    	WIN: BCS_VENTANA.MODULO_FFM , TITULO: 'UI_FFM',
    	SUBMENU:  [
    		  { WIN: BCS_VENTANA.MantenedorInstrumentoFMM , TITULO: 'UI_MANTENEDOR_INSTRUMENTO_FFM' },
  	      	  { WIN: BCS_VENTANA.MantenedorAperturaCierreFFM , TITULO: 'UI_MANTENEDOR_APERTURA_CIERRE_FFM' },   
  	      	  { WIN: BCS_VENTANA.BlotterFFM , TITULO: 'UI_BLOTTER_FFM' },
		  { WIN: BCS_VENTANA.BlotterTraspasosFFM , TITULO: 'UI_GESTION_DE_TRASPASOS' },
  	      	  { WIN: BCS_VENTANA.PreCertificacionOmnibus , TITULO: 'UI_PRECERTIFICACION_OMNIBUS' },
  	      	  { WIN: BCS_VENTANA.CargaOperacionesFondosMutuos , TITULO: 'UI_CARGA_OPERACIONES_FONDOS_MUTUOS' }, 
  	      	  { WIN: BCS_VENTANA.ConsultaCustodiaFondosMutuos , TITULO: 'UI_CONSULTA_CUSTODIA_FONDOS_MUTUOS' },     	     
  	      	  { WIN: BCS_VENTANA.CargaOperacionesAgfFondosMutuos , TITULO: 'UI_CARGA_INTERFAZ_AGF_OPERACIONES_FONDOS_MUTUOS' }, 
  	      	  //{ WIN: BCS_VENTANA.GenerarArchivoAGF , TITULO: 'UI_GENERAR_ARCHIVO_OPERACIONES_AGF' }, 
  	       	  { WIN: BCS_VENTANA.MantenedorRegimenFondoMutuo , TITULO: 'UI_MANTENEDOR_REGIMEN_FONDO_MUTUO' },
  	      	  { WIN: BCS_VENTANA.CuadraturaPagoDividendosAGF , TITULO: 'UI_CUADRATURA_PAGO_DIVIDENDOS_AGF' }, 
  	      	  { WIN: BCS_VENTANA.CuadraturaAGF, TITULO: 'UI_CUADRATURA_AGF' },
			  { WIN: BCS_VENTANA.ConsultaValorCuotaVigente , TITULO: 'UI_CONSULTA_VALOR_CUOTA' },
			  { WIN: BCS_VENTANA.MantenedorFondosIndustria , TITULO: 'UI_MANTENEDOR_FONDOS_INDUSTRIA' },
			  { WIN: BCS_VENTANA.ConsultaComisionesNN , TITULO: 'UI_COMISIONES_NN' },
			  { SEPARATE: true , TITULO: 'UI_CSC' },
			  { WIN: BCS_VENTANA.BlotterCSC , TITULO: 'UI_BLOTTER_CSC' },
    	      { WIN: BCS_VENTANA.IngresarCSC , TITULO: 'UI_INGRESAR_CSC' },
  	      	  { WIN: BCS_VENTANA.CuadraturaCierreDiarioFFMM , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
       	      { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.NCG365TransaccionesFFMM , TITULO: 'UI_NCG365_INFORME_DE_TRANSACCIONES' },
    	]    	    	
    },
	*/
    //CUENTA_INVERSION
    {    	
    	WIN: BCS_VENTANA.MODULO_CUENTA_INVERSION , TITULO: 'UI_CUENTA_INVERSION',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.CrearCargoAbono , TITULO: 'UI_INGRESAR_CARGO_ABONO_MANUAL' },
    	      //CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
    	      { WIN: BCS_VENTANA.CargaMasivaCuentaInversion , TITULO: 'UI_INGRESO_MASIVO_CARGO_ABONO' },
    	      { WIN: BCS_VENTANA.CuentaInversionCliente , TITULO: 'UI_CUENTA_INVERSION_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.LiquidarSaldo , TITULO: 'UI_LIQUIDACION_DE_SALDOS' },
    	      // CELHALCONN-821 - se solicita quitar cuadratura cta inversion { WIN: BCS_VENTANA.CuadraturaCuentaInversion, TITULO: 'UI_CUADRATURA_CUENTA_INVERSION' },
	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioCTA, TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_MANTENEDORES' },
    	      { WIN: BCS_VENTANA.MantenedorCuentaInversion , TITULO: 'UI_MANTENEDOR_CUENTA_INVERSION' },
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.CartolaDeMovimientos , TITULO: 'UI_CARTOLA_MOVIMIENTOS' },
    	      { WIN: BCS_VENTANA.CuentaInversionEjecutivo , TITULO: 'UI_CONSULTA_SALDO_MOVIMIENTOS' }, 
    	      { WIN: BCS_VENTANA.CuentasPorPagarYCobrar , TITULO: 'UI_CUENTAS_COBRAR_CUENTAS_PAGAR' }, 
    	      { WIN: BCS_VENTANA.CuadraturaSaldosCuentaInversion , TITULO: 'UI_CONSULTA_CUADRATURA_SALDOS_CUENTAS_INVERSION' },
    	      { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.ReporteCuentasPorCobrarAnexo16 , TITULO: 'UI_ANEXO16_LIBRO_DIARIO_DE_CUENTAS_POR_COBRAR' },
    	      { WIN: BCS_VENTANA.AtributtionAnalisys , TITULO: 'UI_ATRIBUTION_ANALISIS' },
    	      { WIN: BCS_VENTANA.AtributionAnalisysConsultarInforme , TITULO: ' UI_ATRIBUTION_ANALISYS' },
			  { WIN: BCS_VENTANA.ReporteAntiguedadDeuda , TITULO: 'UI_REPORTE_ANTIGUEDAD_DEUDA' },  
    	     
		]    	    	
    },
    //CUSTODIA
    {    	
    	WIN: BCS_VENTANA.MODULO_CUSTODIA , TITULO: 'UI_CUSTODIA',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.IngresarTitulo , TITULO: 'UI_MENU_CUST_INGRESAR_RETIRAR_TITULO' },
    	      { WIN: BCS_VENTANA.IngresarTituloTest , TITULO: 'TEST INGRESO RETIRO TITULOS',QA: true  },  
    	      { WIN: BCS_VENTANA.BlotterGarantia , TITULO: 'UI_MENU_CUST_GARANTIAS' }, 
    	      { WIN: BCS_VENTANA.BlotterEventoCapital , TITULO: 'UI_TITULO_EVENTO_CAPITAL' },
			  { WIN: BCS_VENTANA.BlotterSimultanea , TITULO: 'UI_SIMULTANEAS' }, 
			  { WIN: BCS_VENTANA.BlotterPrestamos , TITULO: 'UI_MENU_CUST_PRESTAMOS_Y_EMPRESTITOS' }, 
			  { WIN: BCS_VENTANA.BlotterCarteraDerivados , TITULO: 'UI_MENU_CUST_GESTION_OPCIONES' }, 			  
			  { WIN: BCS_VENTANA.PrestamosPendientesPorCargar , TITULO: 'UI_PRESTAMOS_PENDIENTES_POR_CARGAR' },
			  { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.MovimientoEnTransito , TITULO: 'UI_MENU_CUST_MOVIMIENTOS_EN_TRANSITO' }, 
    	      { WIN: BCS_VENTANA.ConsultarCarteras , TITULO: 'UI_MENU_CUST_CONSULTAR_CARTERAS' },
    	      { WIN: BCS_VENTANA.ConsultarCustodia , TITULO: 'UI_MENU_CUST_CONSULTAR_CUSTODIA' },     	     
    	      { WIN: BCS_VENTANA.ConsultarFlujoInstrumento , TITULO: 'UI_MENU_CUST_DETALLE_FLUJO_INSTRUMENTO' }, 
    	      { WIN: BCS_VENTANA.RevisionCoberturasGarantias , TITULO: 'UI_COBERTURA_GARANTIAS' }, 
			  { WIN: BCS_VENTANA.BlotterPactosCorteSorteo , TITULO: 'UI_MENU_CUST_PACTOS_CORTE_SORTEO' }, 
			  { WIN: BCS_VENTANA.ConsultarJuntaAccionistas , TITULO: 'UI_CONSULTAR_JUNTA_ACCIONISTAS' }, 
			  { WIN: BCS_VENTANA.BlotterPagoCorteCupon , TITULO: 'UI_PAGO_CORTE_CUPON' },
			  { WIN: BCS_VENTANA.ConsultarPatrimonio , TITULO: 'UI_CONSULTAR_PATRIMONIO' },
    	      { SEPARATE: true , TITULO: 'UI_PROCESOS' },
    	      { WIN: BCS_VENTANA.BlotterCuadraturaCustodia , TITULO: 'UI_CUADRATURA_CUSTODIA' }, 
	      { WIN: BCS_VENTANA.BlotterCuadraturaCircular1518 , TITULO: 'UI_CUADRATURA_CIRCULAR1518' },
			  { WIN: BCS_VENTANA.BlotterOperacionesPendientes , TITULO: 'UI_TITULO_OPERACIONES_PENDIENTES' }, 
			  { WIN: BCS_VENTANA.VencimientoSimultanea , TITULO: 'UI_TITULO_VENCIMIENTO_SIMULTANEA' }, 
			  { WIN: BCS_VENTANA.VencerPrestamos , TITULO: 'UI_MENU_CUST_VENCER_PRESTAMOS' }, 
			  { WIN: BCS_VENTANA.VencimientoPactos , TITULO: 'UI_TITULO_VENCIMIENTO_PACTOS' },
			  { WIN: BCS_VENTANA.ValorizacionCustodia , TITULO: 'UI_MENU_CUST_VALORIZACION_CUSTODIA' }, 
			  { WIN: BCS_VENTANA.BlotterCargaMasivaCustodia , TITULO: 'UI_CARGA_MASIVA_CUSTODIA' }, 
    	      { WIN: BCS_VENTANA.BlotterTraspasoDCV , TITULO: 'UI_TITULO_TRANSFERENCIA_DE_VALORES_AL_DEPOSITO' },    	      
    	      { WIN: BCS_VENTANA.TarjetaExistencia , TITULO: 'UI_TARJETA_EXISTENCIA' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioCUS , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
              { WIN: BCS_VENTANA.ConsultarFusionCartera , TITULO: 'UI_FUSION_CARTERA' },
    	      { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.CertificadoCustodia , TITULO: 'UI_CERTIFICADO_CUSTODIA' },
    	      { WIN: BCS_VENTANA.ReporteDiarioPrestamosAnexo14 , TITULO: 'UI_ANEXO14_REGISTRO_DE_PRESTAMOS_DE_VALORES' },
    	      { WIN: BCS_VENTANA.ConsultarCertificadoPrestamo , TITULO: "UI_CERTIFICADOS_PRESTAMOS" },
    	      
		]    	    	
    },
    //MODULO_FACTURACION
    {    	
    	WIN: BCS_VENTANA.MODULO_FACTURACION , TITULO: 'UI_FACTURACION',
    	SUBMENU:  [
    	      //{ WIN: BCS_VENTANA.IngresarMovimiento , TITULO: 'UI_INGRESAR_MOVIMIENTO' },
    	      { WIN: BCS_VENTANA.BlotterFacturacion , TITULO: 'UI_GESTION_FACTURACION' },
              { WIN: BCS_VENTANA.EmisionFacturacionMasiva , TITULO: 'UI_EMISION_FACTURACION_MASIVA' },			  
    	      { WIN: BCS_VENTANA.BlotterLibroDte , TITULO: 'UI_LIBRO_DTE' },
    	      { WIN: BCS_VENTANA.ContratoCobroPeriodico , TITULO: 'UI_FACTURACION_COBROS_MENSUAL' },    	      
    	      { WIN: BCS_VENTANA.CargaMasivaFacturacion , TITULO: 'UI_CARGA_MASIVA' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioFAC , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { WIN: BCS_VENTANA.ConsultaFacturacionDte , TITULO: 'UI_CONSULTA_FACTURACION_DTE' },
			  { WIN: BCS_VENTANA.ConsultaTareasDte , TITULO: 'UI_CONSULTA_TAREAS_DTE' },
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.CuadraturaTransaccionFacturacion , TITULO: 'UI_CUADRATURA_TRANSACCION_FACTURACION' },     	     
    	      { WIN: BCS_VENTANA.ConsultaGeneralFacturacion , TITULO: 'UI_CONSULTA_GENERAL_FACTURACION' }, 
    	      { WIN: BCS_VENTANA.BlotterDeudoresAcreedores , TITULO: 'UI_DEUDORES_ACREEDORES_POR_INTERMEDIACION' },
    	      { WIN: BCS_VENTANA.ContratoCobroPeriodico , TITULO: 'UI_CONTRATOS_COBROS_PERIODICOS' }, 
    	      { WIN: BCS_VENTANA.ConsultaCobrosClientes , TITULO: 'UI_CONSULTA_COBROS_CLIENTE' }, 
			  { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.GenerarInterfazAfp , TITULO: 'UI_INTERFAZ_AFP' }, 
			  { WIN: BCS_VENTANA.GenerarInformeGananciaCapital , TITULO: 'UI_GANANCIA_DE_CAPITAL' },
		]    	    	
    },
    //TESORERIA
    {    	
    	WIN: BCS_VENTANA.MODULO_TESORERIA , TITULO: 'UI_TESORERIA',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.CrearIngresoEgreso , TITULO: 'UI_CREAR_INGRESO_EGRESO' },
    	      { WIN: BCS_VENTANA.BlotterComprobantesTesoreria , TITULO: 'UI_GESTION_TESORERIA' },   
    	      { WIN: BCS_VENTANA.ClearingTesoreria , TITULO: 'UI_CLEARING' }, 
    	      { WIN: BCS_VENTANA.GestionContrapartes , TITULO: 'UI_GESTION_CONTRAPARTES' },
    	      { WIN: BCS_VENTANA.BlotterNominasTransferencias , TITULO: 'UI_GESTION_NOMINA_TRANSFERENCIA' },     	     
    	      { WIN: BCS_VENTANA.BlotterDepositos , TITULO: 'UI_GESTION_DEPOSITOS' }, 
    	      { WIN: BCS_VENTANA.CargaMasivaTesoreria , TITULO: 'UI_CARGA_MASIVA' }, 
    	      //{ WIN: BCS_VENTANA.RevisionCierreDiario , TITULO: 'UI_REVISION_CIERRE_DIARIO' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioTES , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_MANTENEDORES' },
    	      { WIN: BCS_VENTANA.CajasTesoreria , TITULO: 'UI_MANTENEDOR_CAJA' }, 
    	      { WIN: BCS_VENTANA.MonedasTesoreria , TITULO: 'UI_MANTENEDOR_MONEDAS_TESORERIA' }, 
    	      { WIN: BCS_VENTANA.BancosTesoreria , TITULO: 'UI_MANTENEDOR_CUENTA_BANCARIA' }, 
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.ConsultaSaldoBanco , TITULO: 'UI_CONSULTA_SALDO_BANCO' }, 
    	      { WIN: BCS_VENTANA.CuadraturaComprobantesCuentaInversion , TITULO: 'UI_CUADRATURA_COMPROBANTES_CUENTAINVERSION' }, 
    	      { WIN: BCS_VENTANA.LibretaBanco , TITULO: 'UI_LIBRETA_BANCO' }, 			 
            { WIN: BCS_VENTANA.BlotterPAC , TITULO: 'UI_BLOTTER_PAC' },
		]    	    	
    } ,
    //MESARF
    {    	
    	WIN: BCS_VENTANA.MODULO_MESARF , TITULO: 'UI_RENTA_FIJA_E_IIF',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.MesaRentaFija , TITULO: 'UI_GESTION_MESA_RENTA_FIJA' },
    	      { WIN: BCS_VENTANA.ConsultaCarteraMesaRentaFija , TITULO: 'UI_CARTERA_RENTA_FIJA' },   
    	      { WIN: BCS_VENTANA.CarteraReservada , TITULO: 'UI_CARTERA_RESERVADA' }, 
    	      { WIN: BCS_VENTANA.CarteraConsolidada , TITULO: 'UI_CARTERA_CONSOLIDADA' }, 
    	      { WIN: BCS_VENTANA.PosicionMRF , TITULO: 'UI_POSICION_MRF' },     	     
    	      { WIN: BCS_VENTANA.CargaMasivaDepositos , TITULO: 'UI_CARGA_MASIVA_DE_DEPOSITOS' }, 
    	      { WIN: BCS_VENTANA.CargaMasivaOperaciones , TITULO: 'UI_CARGA_MASIVA_DE_OPERACIONES' },
    	      { WIN: BCS_VENTANA.CargaMasivaMesa , TITULO: 'UI_CARGA_MASIVA' }, 			  
    	      { WIN: BCS_VENTANA.CierreMesa , TITULO: 'UI_CIERRE_Y_APERTURA_DE_MESA' }, 
    	      { WIN: BCS_VENTANA.MantenedorMesas , TITULO: 'UI_MANTENEDOR_MESAS' }, 
    	      { WIN: BCS_VENTANA.ConsultaPlazosFechas , TITULO: 'UI_CONSULTA_PLAZOS_FECHAS' }, 
    	      { WIN: BCS_VENTANA.MantenedoresPosicionMRF , TITULO: 'UI_MANTENEDORES_POSICION_MRF' }, 
    	      { WIN: BCS_VENTANA.MantenedorItemsManualesPosicionMRF , TITULO: 'UI_MANTENEDOR_ITEMS_MANUALES_POSICION_MRF' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioMRF , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { WIN: BCS_VENTANA.ConfirmarMRF , TITULO: 'UI_CONFIRMAR_OPERACIONES_MRF' }, //CELZORRO-654
            { WIN: BCS_VENTANA.ReasignacionMesaRF , TITULO: 'UI_REASIGNACION_MESA_RF' },
    	      { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.InformeOperacionesDiariasPactos , TITULO: 'UI_INFORME_OPERACIONES_DIARIAS_PACTOS' },
	      { WIN: BCS_VENTANA.InformeMovimientosDiarios , TITULO: 'UI_INFORME_MOVIMIENTOS_DIARIOS' },
    	      { WIN: BCS_VENTANA.QuincenalDePactosMRF , TITULO: 'UI_QUINCENAL_DE_PACTOS' },
	       { WIN: BCS_VENTANA.ContratoCondicionesGenerales , TITULO: 'UI_MRF_CCG_CONTRATO_CONDICIONES_GENERALES' },
		]    	    	
    },
    //ILS
    {    	
    	WIN: BCS_VENTANA.MODULO_ILS , TITULO: 'UI_ILS',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.ILSMantenedor , TITULO: 'UI_ILS_MANTENEDOR_ILS' },
    	      { WIN: BCS_VENTANA.ILSMantenedorConcepto , TITULO: 'UI_ILS_MANTENEDOR_CONCEPTO' },   
    	      { WIN: BCS_VENTANA.ILSMantenedorIndice , TITULO: 'UI_ILS_MANTENEDOR_INDICE' }, 
    	      { WIN: BCS_VENTANA.ILSMantenedorInforme , TITULO: 'UI_ILS_MANTENEDOR_INFORME' }, 
    	      { WIN: BCS_VENTANA.ILSMantenedorItem , TITULO: 'UI_ILS_MANTENEDOR_ITEM' },     	     
		]    	    	
    }, 
  //PLATAFORMA
    {    	
    	WIN: BCS_VENTANA.MODULO_PLATAFORMA , TITULO: 'UI_PLATAFORMA',
    	SUBMENU:  [
			  { SEPARATE: true , TITULO: 'UI_AUTORIZACION' },	
    	      { WIN: BCS_VENTANA.Autorizaciones , TITULO: 'UI_AUTORIZACIONES' },
    	      { WIN: BCS_VENTANA.MantenedorAccesosIcliente , TITULO: 'UI_MANTENEDOR_ACCESOS_ICLIENTE' },
    	      { WIN: BCS_VENTANA.ConsultaUsuarios , TITULO: 'UI_CONSULTA_USUARIOS' },
    	      { WIN: BCS_VENTANA.ConsultarRoles , TITULO: 'UI_CONSULTAR_ROLES' },
    	      { WIN: BCS_VENTANA.ConsultarPerfiles , TITULO: 'UI_CONSULTAR_PERFILES' },
    	      { WIN: BCS_VENTANA.ConsultarAccesosUsuarios , TITULO: 'UI_CONSULTAR_ACCESOS_USUARIO' }, 
    	      { SEPARATE: true , TITULO: 'UI_INSTRUMENTOS' },
    	      { WIN: BCS_VENTANA.Clasificaciondeactivos , TITULO: 'UI_CLASIFICACION_DE_ACTIVOS' },
    	      { WIN: BCS_VENTANA.ConsultarInstrumentos , TITULO: 'UI_CONSULTAR_INSTRUMENTOS' }, 
			  { WIN: BCS_VENTANA.CarteraInversion , TITULO: 'UI_CARTERAS_INVERSION' },
			  { SEPARATE: true , TITULO: 'UI_COMERCIAL' },				  
    	      { WIN: BCS_VENTANA.administrarModelosComerciales , TITULO: 'UI_MODELOS_COMERCIALES' }, 
    	      { WIN: BCS_VENTANA.AdministrarModelosTipoCliente , TITULO: 'UI_MODELOS_TIPO_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.administrarRequisitosOperativos , TITULO: 'UI_REQUISITOS_OPERATIVOS' }, 
    	      { WIN: BCS_VENTANA.administrarPlantillasEjecutivos , TITULO: 'UI_PLANTILLAS_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.MantenedorIndicadores , TITULO: 'UI_MANTENEDOR_INDICADORES' },
    	      { WIN: BCS_VENTANA.MantenedorContratosComisiones , TITULO: 'UI_MANTENEDOR_COBRO_COMISIONES_MENSUALES' },
			  { WIN: BCS_VENTANA.ConfiguracionEncuesta , TITULO: 'UI_CONFIGURACION_ENCUESTA' },
    	      { WIN: BCS_VENTANA.MantenedorTipoRelaciones , TITULO: 'UI_MANTENEDOR_TIPO_RELACIONES' },
    	      { WIN: BCS_VENTANA.MantenedorDocumentos , TITULO: 'UI_MANTENEDOR_DOCUMENTOS' },
    	      { WIN: BCS_VENTANA.AdministrarPreferenciasPago , TITULO: 'UI_ADMINISTRAR_PREFERENCIAS_PAGO' },
			  { SEPARATE: true , TITULO: 'UI_NEGOCIO' },
    	      { WIN: BCS_VENTANA.ConfiguracionFlujoCaja , TITULO: 'UI_CONFIGURACION_FLUJO_CAJA' },
			  { WIN: BCS_VENTANA.ConfiguracionFlujoCustodia , TITULO: 'UI_CONFIGURACION_FLUJO_CUSTODIA' },
			  { WIN: BCS_VENTANA.ConfiguracionPatrimonioConsolidado , TITULO: 'UI_CONFIGURACION_PATRIMONIO_CONSOLIDADO' },
    	      { WIN: BCS_VENTANA.MantenedorCobertura , TITULO: 'UI_MANTENEDOR_COBERTURA' },
    	      { WIN: BCS_VENTANA.MantenedorLimites , TITULO: 'UI_MANTENEDOR_LIMITES'},
			  { WIN: BCS_VENTANA.ConsultarLimites , TITULO: 'UI_CONSULTAR_LIMITES' },
    	      { WIN: BCS_VENTANA.MantenedorExigibilidad , TITULO: 'UI_MANTENEDOR_EXIGIBILIDAD' },
    	      { SEPARATE: true , TITULO: 'UI_CONTROL_SISTEMA' },
			  { WIN: BCS_VENTANA.AperturaCierreModulos , TITULO: 'UI_MODULOS' },
    	      { WIN: BCS_VENTANA.MantenedorPauta , TITULO: 'UI_PAUTA'},
			  { SEPARATE: true , TITULO: 'UI_CONTROL' },
			  { WIN: BCS_VENTANA.ConsultaLog , TITULO: 'UI_CONSULTA_LOGS' },
    	      { WIN: BCS_VENTANA.MantenedorAgenda , TITULO: 'UI_CONSULTAR_AGENDA' },
    	      { WIN: BCS_VENTANA.MantenedorPrograma , TITULO: 'UI_CONSULTAR_PROGRAMAS' },
    	      { WIN: BCS_VENTANA.ConsultaGeneralRegistrosSistema , TITULO: 'UI_CONSULTA_REGISTRO_SISTEMA'},
			  { SEPARATE: true , TITULO: 'UI_ADMINISTRAR_NOTIFICACIONES' },
			  { WIN: BCS_VENTANA.MantenedorEventos , TITULO: 'Maestro Notificaciones' },
    	      { WIN: BCS_VENTANA.MantenedorComentenos , TITULO: 'Comentenos (Dummy)' },
    	      { WIN: BCS_VENTANA.MantenedorNotificaciones , TITULO: 'Consultar Notificaciones'},
    	      { WIN: BCS_VENTANA.MantenedorMensajes , TITULO: 'Mantenedor Mensajes'},
			  { SEPARATE: true , TITULO: 'PlataformaSeparadorTasas' },
			  { WIN: BCS_VENTANA.SolicitudTasas , TITULO: 'UI_TASAS' },
    	      { WIN: BCS_VENTANA.CargaMantenedorTasas , TITULO: 'UI_CARGA_Y_MANTENEDOR_DE_TASAS' },
    	      { WIN: BCS_VENTANA.CriterioSeleccionTasas , TITULO: 'UI_CRITERIO_DE_SELECCION_DE_TASAS'},
			  { WIN: BCS_VENTANA.MargenesTasas , TITULO: 'UI_MANTENEDOR_MARGENES_TASAS' },
			  { SEPARATE: true , TITULO: 'UI_MERCADO' },
    	      { WIN: BCS_VENTANA.ParidadYTipoCambio , TITULO: 'UI_PARIDAD_Y_TIPOCAMBIO'},
    	      { WIN: BCS_VENTANA.MantenedorEmisores , TITULO: 'UI_EMISORES'},
    	      { WIN: BCS_VENTANA.CargaVectoresPrecio, TITULO: 'UI_CARGA_VECTOR_PRECIOS'},
			  { WIN: BCS_VENTANA.ConsultarVectorPrecio , TITULO: 'UI_CONSULTAR_VECTOR_PRECIO' },
    	      { WIN: BCS_VENTANA.MaestroParametrosFinanciero , TITULO: 'UI_TIPOS_CAMBIO_E_INDICES'},
    	      { WIN: BCS_VENTANA.ConsultaUsoParametroFinanciero , TITULO: 'UI_CONSULTA_USO_PARAMETRO_FINANCIERO'},
			  { WIN: BCS_VENTANA.MantenedorDeterioro , TITULO: 'UI_MANTENEDOR_DETERIORO_EMISOR'},
			  { SEPARATE: true , TITULO: 'UI_OTROS' },
			  { WIN: BCS_VENTANA.DashboardPowerBI , TITULO: 'UI_DASHBOARD_POWER_BI' },
			  { WIN: BCS_VENTANA.InterfazLiquidezIntradia , TITULO: 'UI_INTERFAZ_LIQUIDEZ_INTRADIA' },
    	      { WIN: BCS_VENTANA.AjusteHorario , TITULO: 'UI_AJUSTE_HORARIO' },
    	      { WIN: BCS_VENTANA.AjusteFechaSistema , TITULO: 'UI_AJUSTE_FECHA_SISTEMA' },
    	      //{ WIN: BCS_VENTANA.ConsultarParidades , TITULO: 'UI_PARIDADES_Y_TIPO_CAMBIO'},
			  { WIN: BCS_VENTANA.MantenedorGeneral , TITULO: 'Mantenedor General' },
			  { WIN: BCS_VENTANA.MantenedorParametros , TITULO: 'Mantenedor Parametros' },
    	      { WIN: BCS_VENTANA.AdministrarBoveda , TITULO: 'UI_BOVEDAS' },
    	      { WIN: BCS_VENTANA.AdministrarTipoOrigenFondo , TITULO: 'UI_TIPO_ORIGEN_FONDO'},
			  { WIN: BCS_VENTANA.RelacionInstrumento , TITULO: 'UI_RELACION_INSTRUMENTO' },
    	      { WIN: BCS_VENTANA.MantenedorConceptosManuales , TITULO: 'UI_MANTENEDOR_CONCEPTOS_MANUALES' },
    	      { WIN: BCS_VENTANA.MantenedorArchivosContratos , TITULO: 'UI_MANTENEDOR_ARCHIVOS_CONTRATOS'},
			  { WIN: BCS_VENTANA.MantenedorDatosContratos , TITULO: 'UI_MANTENEDOR_DATOS_CONTRATOS' },
    	      { WIN: BCS_VENTANA.MantenedorImagenes , TITULO: 'UI_MANTENEDOR_IMAGENES' },
    	      { WIN: BCS_VENTANA.MantenedorTemplates , TITULO: 'UI_MANTENEDOR_TEMPLATES'},
			  { WIN: BCS_VENTANA.MantenedorBoletines , TITULO: 'UI_MANTENEDOR_BOLETINES'},
			  { WIN: BCS_VENTANA.AdmDiccionario , TITULO: 'Diccionario ' },
    	      { WIN: BCS_VENTANA.CambiarClaveUsuarioReplica , TITULO: 'UI_CAMBIAR_CLAVE_USUARIO_REPLICA' },
			  { WIN: BCS_VENTANA.MantenedorDiasFeriados , TITULO: 'UI_MANTENEDOR_DIAS_FERIADOS' },
			  { WIN: BCS_VENTANA.PreferenciaCalceLibroInventario , TITULO: 'UI_PREFERENCIA_CALCE_LIBRO_INVENTARIO' },
			  { WIN: BCS_VENTANA.ConsultaConciliacion , TITULO: 'UI_CONSULTA_CONCILIACION' },
			  { WIN: BCS_VENTANA.MantenedorCamposFormularioObligatorios , TITULO: 'UI_MANTENEDOR_CAMPOS_FORMULARIO' },
			  { SEPARATE: true , TITULO: 'UI_VALORIZADOR' },
    	      { WIN: BCS_VENTANA.ConsultaCupones, TITULO: 'UI_CONSULTA_CUPONES'},
    	      { WIN: BCS_VENTANA.ValorizadorInstrumento, TITULO: 'UI_VALORIZADOR_INSTRUMENTO'},
	      { WIN: BCS_VENTANA.ValorizadorPagare, TITULO: 'UI_VALORIZADOR_PAGARE'},
    	      { WIN: BCS_VENTANA.ValorizadorCartera, TITULO: 'UI_VALORIZADOR_CARTERA'},
    	      { WIN: BCS_VENTANA.ConsultaConfigValorizador, TITULO: 'UI_CONSULTA_CONFIG_VALORIZADOR'},
    	]    	    	
    },
    //NORMATIVA
    {    	
    	WIN: BCS_VENTANA.MODULO_NORMATIVO , TITULO: 'UI_NORMATIVA',
    	SUBMENU:  [
    	      { SEPARATE: true , TITULO: 'UI_NORMATIVA' },     
	  	      //{ WIN: BCS_VENTANA.EmisionCirculares , TITULO: 'UI_EMISION_DE_CIRCULARES' },
	  	      { WIN: BCS_VENTANA.Circulares , TITULO: 'UI_CIRCULARES' },
		      //{ WIN: BCS_VENTANA.NORMantenedorDocumentos , TITULO: 'UI_MANTENEDOR_DE_DOCUMENTOS' },   
		      { WIN: BCS_VENTANA.LibroOperaciones , TITULO: 'UI_LIBRO_DE_OPERACIONES' }, 
		      { WIN: BCS_VENTANA.Circularizacion , TITULO: 'UI_CIRCULARIZACION' },
		      { WIN: BCS_VENTANA.ConsultarLibroVentas , TITULO: 'UI_LIBRO_DE_VENTAS' },
		      { WIN: BCS_VENTANA.Cumplimiento , TITULO: 'UI_CUMPLIMIENTO' },
		      { WIN: BCS_VENTANA.NotificacionOrdenes , TITULO: 'UI_NOTIFICACION_DE_ORDENES' },
		      { WIN: BCS_VENTANA.ConsultarEjecucionOrdenes , TITULO: 'UI_CONSULTAR_EJECUCION_DE_ORDENES' },
			  { WIN: BCS_VENTANA.NotificacionCircular1985 , TITULO: 'UI_NOTIFICACION_CIRCULAR_1985' },
		      { WIN: BCS_VENTANA.ConsultarCertificados , TITULO: 'UI_CONSULTAR_CERTIFICADOS' },
		      { WIN: BCS_VENTANA.ConsultarIndicadoresNormativa , TITULO: 'UI_CONSULTAR_INDICADORES' },
		      { WIN: BCS_VENTANA.ConsultarSiiF29 , TITULO: 'UI_CONSULTAR_SIIF29' },
		      { WIN: BCS_VENTANA.ConsultarCalculoPolizaGarantiaAPV , TITULO: 'UI_CALCULO_POLIZA_GARANTIA_ANUAL_APV' },
			  { WIN: BCS_VENTANA.ConsultarInversionistasRV13419 , TITULO: 'UI_CONSULTAR_INVERSIONISTAS_RV_13419' },
			  { WIN: BCS_VENTANA.ManetenedorEntidadesAuditoras , TITULO: 'UI_MANTENEDOR_ENTIDADES_AUDITORAS' },
		      { SEPARATE: true , TITULO: 'UI_CARTOLAS' },
		      { WIN: BCS_VENTANA.ProcesosCartolas , TITULO: 'UI_PROCESOS_CARTOLAS' }, 
		      { WIN: BCS_VENTANA.ConsultaCartolas , TITULO: 'UI_CONSULTA_CARTOLAS' }, 
		      //{ WIN: BCS_VENTANA.PreferenciasCartolas , TITULO: 'UI_PREFERENCIAS_CARTOLAS' },  		      
		      //{ SEPARATE: true , TITULO: 'UI_DOCUMENTOS' },
		      { SEPARATE: true , TITULO: 'UI_TRIBUTARIO' },
		      { WIN: BCS_VENTANA.VisorDeclaraciones , TITULO: 'UI_MENTENEDOR_DE_DECLARACIONES' },
		      { WIN: BCS_VENTANA.MantenedorEsquemaDeclaracion , TITULO: 'UI_CONFIGURACION_ESQUEMA_PERIODO' }, 
		      { WIN: BCS_VENTANA.ControlProcesoTributario , TITULO: 'UI_CONTROL_PROCESO' },
		      { WIN: BCS_VENTANA.CalificacionesTributarias, TITULO: 'UI_CALIFICACIONES_TRIBUTARIAS'},		      
		      { SEPARATE: true , TITULO: 'UI_BANCOCENTRAL' },
		      { WIN: BCS_VENTANA.VisorDeclaracionesBancoCentral , TITULO: 'UI_MANTENEDOR_DE_DECLARACIONES_BCC' },		      		      
		      { WIN: BCS_VENTANA.CertificadosTributariasSIGACRM, TITULO: 'UI_CERTIFICADO_TRIBUTARIOS_SIGACRM'},
			  { SEPARATE: true , TITULO: 'UI_SISTEMA' },
			  { WIN: BCS_VENTANA.BlotterMLT , TITULO: 'UI_MANTENEDOR_COSTO_18QUATER_y_RENTABILIDAD_APV' }
    	]    	    	
    },
    //MESA FORWARD
    {    	
    	WIN: BCS_VENTANA.MODULO_MESAFORWARD , TITULO: 'UI_FORWARD',
    	SUBMENU:  [
		  	{ WIN: BCS_VENTANA.MesaForward , TITULO: 'UI_INGRESO_FORWARD' },
		  	{ WIN: BCS_VENTANA.MesaForwardModal , TITULO: 'UI_INGRESO_FORWARD' },
		  	{ WIN: BCS_VENTANA.CalceOperaciones , TITULO: 'UI_CALCE_OPERACIONES' },
		  	{ WIN: BCS_VENTANA.VencimientoForward , TITULO: 'UI_VENCIMIENTO_FORWARD' },
		  	{ WIN: BCS_VENTANA.DevengoForward , TITULO: 'UI_DEVENGO_FORWARD' },
		  	{ WIN: BCS_VENTANA.ValorizacionForward , TITULO: 'UI_VALORIZACION_FORWARD' },
		  	{ WIN: BCS_VENTANA.MesaMonedaPorProductoForward , TITULO: 'UI_MONEDAS_POR_PRODUCTO' },
		  	{ WIN: BCS_VENTANA.CierreMesaForward , TITULO: 'UI_CIERRE_Y_APERTURA_DE_MESA' },
		  	{ WIN: BCS_VENTANA.ConsultaOperacionesFdw , TITULO: 'UI_CONSULTA_OPERACIONES' },
		  	{ WIN: BCS_VENTANA.MantenedorCanastaFactores , TITULO: 'UI_CANASTA_FACTORES' },
		  	{ WIN: BCS_VENTANA.MantenedorCanastaModal , TITULO: 'UI_CANASTA_MODAL' },
		  	{ WIN: BCS_VENTANA.MantenedorFactoresModal , TITULO: 'UI_FACTOR_MODAL' },
		  	{ WIN: BCS_VENTANA.MantenedorMatrizRiesgo , TITULO: 'UI_MATRIZ_RIESGO' },
			{ WIN: BCS_VENTANA.MantenedorMesaForward , TITULO: 'UI_MANTENEDOR_MESAS' },
		  	{ WIN: BCS_VENTANA.AnexoCapituloIX , TITULO: 'UI_CAPITULO_ANEXO_IX' },
		  	{ WIN: BCS_VENTANA.EmisionDevengoFWD , TITULO: 'UI_EMISION_DEVENGO_FWD' },
		  	{ WIN: BCS_VENTANA.CuadraturaCierreDiarioFWD , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
			{ WIN: BCS_VENTANA.ConfirmacionForward , TITULO: 'UI_CONFIRMACION_FORWARD' },
		  	{ SEPARATE: true , TITULO: 'UI_INFORMES' },
		  	{ WIN: BCS_VENTANA.ReporteCalceOperaciones , TITULO: 'UI_REPORTE_CALCE_OPERACIONES' },
		  	{ WIN: BCS_VENTANA.InformeArticulo , TITULO: 'UI_ARTICULO_84' },
		  	{ WIN: BCS_VENTANA.ReporteMovimientosDiarioFWDMonedas , TITULO: 'UI_MOVIMIENTOS_DIARIO_MONEDA' },
		  	{ WIN: BCS_VENTANA.ReporteCarteraFWDBasilea , TITULO: 'UI_CARTERA_FWD_BASILEA' },
		  	{ WIN: BCS_VENTANA.ReporteResumenDescalceDiarioCliente , TITULO: 'UI_DESCALCE_DIARIO_CLIENTE' },
		  	{ WIN: BCS_VENTANA.InformePosicionClientesFWD , TITULO: 'UI_POSICION_CLIENTE_FWD' },
		  	{ WIN: BCS_VENTANA.ReporteOperacionesVigentesAnexo15 , TITULO: 'UI_ANEXO15_OPERACIONES_FORWARD_VIGENTES' },
			{ WIN: BCS_VENTANA.ContratoCondicionesGeneralesFWD , TITULO: 'UI_FWD_CCG_CONTRATO_CONDICIONES_GENERALES' },
			{ WIN: BCS_VENTANA.InformeLiquidacionFWD , TITULO: 'UI_FWD_INFORME_LIQUIDACION' },
			{ WIN: BCS_VENTANA.ForwardVencer , TITULO: 'UI_REPORTE_FWD_VENCER' }

    	]    	    	
    },
    //MESA SPOT
    {    	
    	WIN: BCS_VENTANA.MODULO_MESASPOT , TITULO: 'UI_SPOT',
    	SUBMENU:  [
		  	{ WIN: BCS_VENTANA.MesaSpot , TITULO: 'UI_INGRESO_SPOT' },
		  	{ WIN: BCS_VENTANA.BlotterCargaMasivaOperacionSpot , TITULO: 'UI_CARGA_MASIVA_OPERACIONES_SPOT' },
		  	{ WIN: BCS_VENTANA.PuntaSpot , TITULO: 'UI_MANTENEDOR_PUNTA' },
		  	{ WIN: BCS_VENTANA.MesaMonedaPorProductoSpot , TITULO: 'UI_MONEDAS_POR_PRODUCTO' },
		  	{ WIN: BCS_VENTANA.CierreMesaSpot , TITULO: 'UI_CIERRE_Y_APERTURA_DE_MESA' },
		  	{ WIN: BCS_VENTANA.ConsultaOperaciones , TITULO: 'UI_CONSULTA_OPERACIONES' },
		  	{ WIN: BCS_VENTANA.RUTA_DATATEC , TITULO: 'UI_RUTA_DATATEC' },
		  	{ WIN: BCS_VENTANA.RESULTADODEOPERACIONES , TITULO: 'UI_RESULTADO_OPERACIONES' },
			{ WIN: BCS_VENTANA.MantenedorMesaSpot , TITULO: 'UI_MANTENEDOR_MESAS' },
		  	{ WIN: BCS_VENTANA.POSICIONINICIALMESASPOT , TITULO: 'UI_POSICION_INICIAL_MESA_SPOT' },
		  	{ WIN: BCS_VENTANA.POSICIONSPOT , TITULO: 'UI_POSICION' },
		  	{ WIN: BCS_VENTANA.POSICIONPORMONEDA , TITULO: 'UI_POSICION_POR_MONEDA' },
		  	
			{ WIN: BCS_VENTANA.DATATEC , TITULO: 'UI_DATATEC_ONLINE' },
			{ WIN: BCS_VENTANA.REPROCESARDATATEC , TITULO: 'UI_REPROCESAR_DATATEC' },	
		  	{ WIN: BCS_VENTANA.INTERFAZOMA , TITULO: 'UI_INTERFAZ_OMA' },
		  	{ WIN: BCS_VENTANA.PlanillaBancoCentral , TITULO: 'UI_MANTENEDOR_PLANILLA_BANCO_CENTRAL' },
		  	{ WIN: BCS_VENTANA.ReporteOperacionesFicticias , TITULO: 'UI_REPORTE_OPERACIONES_FICTICIAS' },
		  	{ WIN: BCS_VENTANA.ReporteOperacionesSobre500000 , TITULO: 'UI_REPORTE_OPERACIONES_SOBRE_500000' },
		  	{ WIN: BCS_VENTANA.ReporteResultadoContableSpot , TITULO: 'UI_REPORTE_RESULTADO_CONTABLE_SPOT' },
		  	{ WIN: BCS_VENTANA.ParidadYTipoCambio , TITULO: 'UI_PARIDAD_Y_TIPOCAMBIO' },
			{ WIN: BCS_VENTANA.LiquidarSpot , TITULO: 'UI_LIQUIDAR_SPOT' },
			{ WIN: BCS_VENTANA.CuadraturaCierreDiarioSPT , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
			{ WIN: BCS_VENTANA.CalculadoraSpot , TITULO: 'UI_CALCULADORA_SPOT' },
			{ WIN: BCS_VENTANA.ReporteMensualOperaciones , TITULO: 'UI_REPORTE_MENSUAL_OPERACIONES' },
			{ WIN: BCS_VENTANA.InformeSaldosCuentaCambio , TITULO: 'UI_INFORME_SALDOS_CUENTA_CAMBIO' },
			{ WIN: BCS_VENTANA.LimitesSpotCorredora , TITULO: 'UI_INFORME_LIMITES_CORREDORA' },

		]    	    	
    },
    //CONTABILIDAD
    {    	
    	WIN: BCS_VENTANA.MODULO_CONTABILIDAD , TITULO: 'UI_INTERFAZ_CONTABLE',
    	SUBMENU:  [
			{ WIN: BCS_VENTANA.InterfazContable , TITULO: 'UI_GESTION_INTERFAZ_CONTABLE' },
			{ WIN: BCS_VENTANA.ConfiguracionComprobantes , TITULO: 'UI_CONFIGURACION_COMPROBANTES' },
			{ WIN: BCS_VENTANA.MantenedorMetadataContabilidad , TITULO: 'UI_CONFIGURACION_METADATA' },
			{ WIN: BCS_VENTANA.CuadraturaCierreDiarioCON , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
			{ WIN: BCS_VENTANA.CuentascontablesBasilea, TITULO: 'UI_CUENTAS_CONTABLES_BASILEA'},
			{ SEPARATE: true , TITULO: 'UI_INFORMES' },
			{ WIN: BCS_VENTANA.InformeControlFacturacionOperaciones , TITULO: 'UI_INFORME_CONTROL_FACTURACION_OPERACIONES' },
			{ WIN: BCS_VENTANA.InformeMayorYMenorValorCuentaPropia , TITULO: 'UI_INFORME_MAYOR_MENOR_VALOR' },
			{ WIN: BCS_VENTANA.InformeCarteraPactosValorizadaCuentaPropia , TITULO: 'UI_INFORME_CARTERA_PACTOS_VALORIZADA' },
			{ WIN: BCS_VENTANA.InformeCarteraPropia , TITULO: 'UI_INFORME_CARTERA_PROPIA' },
			{ WIN: BCS_VENTANA.CoberturaPatrimonial , TITULO: 'UI_COBERTURA_PATRIMONIAL' },
			{ WIN: BCS_VENTANA.InformeSimultaneaCuentaPropia , TITULO: 'UI_INFORME_SIMULTANEA_CUENTA_PROPIA' },
			{ WIN: BCS_VENTANA.InformeOperacionesPactosConDetalle , TITULO: 'UI_INFORME_PACTO_CON_DETALLE' },
			{ WIN: BCS_VENTANA.InformeCarteraLeasing , TITULO: 'UI_INFORME_CARTERA_LEASING' },
			{ WIN: BCS_VENTANA.InformeComprasVentasTermino, TITULO: 'UI_INFORME_COMPRAS_VENTAS_TERMINO' },
			{ WIN: BCS_VENTANA.InformeVencimientosAnticiposPactos , TITULO: 'UI_INFORME_VENCIMIENTOS_ANTICIPOS_PACTOS' },
			{ WIN: BCS_VENTANA.InformeClientesRelacionados , TITULO: 'UI_INFORME_CLIENTES_RELACIONADOS' },
    	]    	    	
    },
    	 //INTERFACES
    {
    	WIN: BCS_VENTANA.MODULO_INTERFACES, TITULO: 'UI_INTERFACES',
    	SUBMENU: [
	          { WIN: BCS_VENTANA.MantenedorFilialesP40 , TITULO: 'UI_MANTENEDOR_DE_FILIALES_P40' },
    	          { WIN: BCS_VENTANA.GeneracionInterfaces , TITULO: 'UI_GENERACION_INTERFACES' }
    	]
    	
    },
    //DESARROLLO
    {    	
    	WIN: BCS_VENTANA.MODULO_DESARROLLO , TITULO: 'Desarrollo', QA: true,
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.MonitorTools , TITULO: 'MONITOR' },
    	      { WIN: BCS_VENTANA.DebugTools , TITULO: 'DEBUG' },
			  { WIN: BCS_VENTANA.UIPlayground , TITULO: 'Playground' },
    	      { WIN: BCS_VENTANA.ConsultarServicios , TITULO: 'UI_CONSULTAR_SERVICIOS' },  
			  { WIN: BCS_VENTANA.ResumenOperacionesMasivas , TITULO: 'NO_TITLE' },
    	      { WIN: BCS_VENTANA.gFormulario , TITULO: 'gFormulario' },  
			  { WIN: BCS_VENTANA.gBlotter , TITULO: 'gBlotter' },
    	      { WIN: BCS_VENTANA.PocBlotterOrdenes , TITULO: 'POC Blotter' },  
			  { WIN: BCS_VENTANA.GeneraRut , TITULO: 'Genera Rut' },
    	      { WIN: BCS_VENTANA.PocG , TITULO: 'POC G' },   
    	]    	    	
    },
    //HOMOLOGACION
    {    	
    	WIN: BCS_VENTANA.MODULO_HOMOLOGACION , TITULO: 'UI_HOMOLOGACION', QA: true,
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.MantenedorCargasMasivas , TITULO: 'UI_MANTENEDOR_CARGAS_MASIVAS' },
    	]    	    	
    },
    //APV
    {    	
    	WIN: BCS_VENTANA.MODULO_APV , TITULO: 'UI_APV',
    	SUBMENU:  [
    	           { SEPARATE: true , TITULO: 'UI_AFILIACION' },
    	           //{ WIN: BCS_VENTANA.AfiliacionMasiva , TITULO: 'UI_AFILIACION_MASIVA' },
    	           { WIN: BCS_VENTANA.GestionSAAPV , TITULO: 'UI_GESTION_SAAPV' },
    	           { WIN: BCS_VENTANA.ContratoSAAPV , TITULO: 'UI_CONTRATO_SAAPV' },
    	           //{ WIN: BCS_VENTANA.ContratoTraspasoSAAPV , TITULO: 'UI_CONTRATO_TRASPASO_SAAPV' },
    	           //{ WIN: BCS_VENTANA.ResumenInversionClienteAPV , TITULO: 'UI_RESUMEN_INVERSION_CLIENTE_APV' },
    	           //{ WIN: BCS_VENTANA.DistribuirInversionSAAPV , TITULO: 'UI_DISTRIBUCION_SAAPV' },
    	           //{ WIN: BCS_VENTANA.ConsultaContratoSAAPV , TITULO: 'UI_CONSULTA_CONTRATO_SAAPV' },
    	           //{ WIN: BCS_VENTANA.ConsultaDistribucionInversionSAAPV , TITULO: 'UI_CONSULTA_DISTRIBUCION_SAAPV' },
				   { WIN: BCS_VENTANA.ClienteContratoSAAPV , TITULO: 'UI_CAMBIAR_A_PLAN_ACTIVO' },
           { WIN: BCS_VENTANA.AsociaPerfilAPV , TITULO: 'UI_ASOCIAR_PERFILES_APV' },
    	           { SEPARATE: true , TITULO: 'UI_RECAUDACION' },
    	           { WIN: BCS_VENTANA.GestionDeRecaudacion , TITULO: 'UI_GESTION_DE_RECAUDACION' },
    	           { WIN: BCS_VENTANA.IngresoRecaudacionDeposito , TITULO: 'UI_INGRESO_DEPOSITO' },
    	           { WIN: BCS_VENTANA.IngresoRecaudacionDetalle , TITULO: 'UI_INGRESO_DETALLE' },
    	           { WIN: BCS_VENTANA.GestionDeInterfaces , TITULO: 'UI_GESTION_DE_INTERFACES' },
    	           
    	           { WIN: BCS_VENTANA.GestionDeRezagos , TITULO: 'UI_GESTION_REZAGOS' },
   				   { WIN: BCS_VENTANA.ConfiguracionRecaudacion , TITULO: 'UI_CONFIGURACION' },
				   { WIN: BCS_VENTANA.MantenedorBancoPrevired , TITULO: 'UI_MANTENEDOR_BANCO_PREVIRED' },
    	           { SEPARATE: true , TITULO: 'UI_INVERSION_DESINVERSION' },
    	           { WIN: BCS_VENTANA.RegistroTraspasoAPV , TITULO: 'UI_REGISTRO_TRASPASO_APV' },
    	           { WIN: BCS_VENTANA.GestionIngresoEgresoAPV , TITULO: 'UI_GESTION_INGRESO_EGRESO_APV' },
    	           { WIN: BCS_VENTANA.DesinversionAPV , TITULO: 'UI_DESINVERSION_APV' },
    	           { WIN: BCS_VENTANA.InversionAPV , TITULO: 'UI_INVERSION_APV' },
    	           { WIN: BCS_VENTANA.GestionBonificacionAPV , TITULO: 'UI_GESTION_BONIFICACION_APV' },
    	        
    	]    	    	
    },
    //INFORMES GENERALES CORREDORAS
    {    	
    	WIN: BCS_VENTANA.MODULO_INFORMES , TITULO: 'UI_INFORMES',
    	SUBMENU:  [
  	      { WIN: BCS_VENTANA.GenerarInformeEstadisticas , TITULO: 'UI_GENERAR_INFORME_ESTADISTICAS' },
  	      { WIN: BCS_VENTANA.GenerarInformeCostoFondo , TITULO: 'UI_GENERAR_INFORME_COSTO_FONDO' },
  	      { WIN: BCS_VENTANA.GenerarInformeRiesgoFinanciero, TITULO: 'UI_GENERAR_INFORME_RIESGO_FINANCIERO'},
  	      { WIN: BCS_VENTANA.GenerarInformeVencimientosGenerales, TITULO: 'UI_GENERAR_INFORME_VENCIMIENTOS_GENERALES'},
  	      { WIN: BCS_VENTANA.GenerarInformeCobroComisionesMensuales, TITULO: 'UI_GENERAR_INFORME_COBRO_COMISIONES_MENSUALES'}
  		]  
    },
];

//---------------------------------------------
/*
 /$$      /$$ /$$$$$$$$ /$$   /$$ /$$   /$$        /$$$$$$   /$$$$$$  /$$$$$$$$ /$$   /$$ /$$$$$$$$ /$$$$$$$$
| $$$    /$$$| $$_____/| $$$ | $$| $$  | $$       /$$__  $$ /$$__  $$| $$_____/| $$$ | $$|__  $$__/| $$_____/
| $$$$  /$$$$| $$      | $$$$| $$| $$  | $$      | $$  \ $$| $$  \__/| $$      | $$$$| $$   | $$   | $$      
| $$ $$/$$ $$| $$$$$   | $$ $$ $$| $$  | $$      | $$$$$$$$| $$ /$$$$| $$$$$   | $$ $$ $$   | $$   | $$$$$   
| $$  $$$| $$| $$__/   | $$  $$$$| $$  | $$      | $$__  $$| $$|_  $$| $$__/   | $$  $$$$   | $$   | $$__/   
| $$\  $ | $$| $$      | $$\  $$$| $$  | $$      | $$  | $$| $$  \ $$| $$      | $$\  $$$   | $$   | $$      
| $$ \/  | $$| $$$$$$$$| $$ \  $$|  $$$$$$/      | $$  | $$|  $$$$$$/| $$$$$$$$| $$ \  $$   | $$   | $$$$$$$$
|__/     |__/|________/|__/  \__/ \______/       |__/  |__/ \______/ |________/|__/  \__/   |__/   |________/
*/
//---------------------------------------------
var BCS_MENU_AGENTE = [
   //AGENTE
   {    	
	   	WIN: BCS_VENTANA.MODULO_AGENTE , TITULO: 'UI_AGENTE',
	   	SUBMENU:  [
	   	    { SEPARATE: true , TITULO: 'UI_AGENTE' }, 
			{ WIN: BCS_VENTANA.ConsultarAsuntosPendientes , TITULO: 'UI_CONSULTAR_ASUNTOS_PENDIENTES' },
			{ WIN: BCS_VENTANA.MantenedorNotificaciones , TITULO: 'Consultar Notificaciones'},
			{ WIN: BCS_VENTANA.Autorizaciones , TITULO: 'UI_AUTORIZACIONES' },
			{ SEPARATE: true , TITULO: 'UI_ACCIONES' },
			{ WIN: BCS_VENTANA.AgenteConsultarResumemEjecutivo , TITULO: 'UI_RESUMEN_EJECUTIVO' },
	   	    { WIN: BCS_VENTANA.AgenteSeparadorInformacionCliente , TITULO: 'UI_CLIENTE' },
	   	    { WIN: BCS_VENTANA.ResumenInversionCliente , TITULO: 'UI_RESUMEN_INVERSION_CLIENTE' },
	   	    { WIN: BCS_VENTANA.CrearCliente , TITULO: 'UI_INGRESAR_CLIENTE' },
	   	    { WIN: BCS_VENTANA.AgenteConsultaAvanzadaClientes , TITULO: 'UI_CONSULTA_AVANZADA_CLIENTES' },
	   	    { WIN: BCS_VENTANA.ImprimirArchivosContratos , TITULO: 'UI_IMPRIMIR_ARCHIVOS_CONTRATOS' },
			
	   	]    	    	
   },
   {    	
	   	WIN: BCS_VENTANA.MODULO_AGENTE , TITULO: 'UI_CLIENTE',
	   	SUBMENU:  [
	   		{ SEPARATE: true , TITULO: 'UI_CARTOLAS' },
	   		{ WIN: BCS_VENTANA.ConsultaCartolas , TITULO: 'UI_CONSULTA_CARTOLAS' },
	   		{ SEPARATE: true , TITULO: 'UI_CERTIFICADOS_IMPRESION' },
	   		{ WIN: BCS_VENTANA.ConsultarCertificados , TITULO: 'UI_CONSULTAR_CERTIFICADOS' },
			{ WIN: BCS_VENTANA.LibroOperaciones , TITULO: 'UI_LIBRO_DE_OPERACIONES' }, 
	   		{ SEPARATE: true , TITULO: 'UI_HISTORIAL_MOVIMIENTOS' },
			{ WIN: BCS_VENTANA.CartolaDeMovimientos , TITULO: 'UI_CARTOLA_MOVIMIENTOS' },
	   		{ WIN: BCS_VENTANA.CuentaInversionEjecutivo , TITULO: 'UI_CONSULTA_SALDO_MOVIMIENTOS' },
	   		{ WIN: BCS_VENTANA.ConsultarCustodia , TITULO: 'UI_MENU_CUST_CONSULTAR_CUSTODIA' },
	   	]    	    	
   },
   {    	
	   	WIN: BCS_VENTANA.MODULO_AGENTE , TITULO: 'UI_INVERSIONES',
	   	SUBMENU:  [

	   	    { SEPARATE: true , TITULO: 'UI_INGRESO_ORDENES' },
	   	    { WIN: BCS_VENTANA.AgenteIngresarOrdenMain , TITULO: 'UI_INGRESAR_ORDEN' },
	   	    { WIN: BCS_VENTANA.AgenteBlotterOrdenes , TITULO: 'UI_GESTION_ORDENES' },
	   	]    	    	
  },
  {    	
	   	WIN: BCS_VENTANA.MODULO_AGENTE , TITULO: 'UI_MOVIMIENTOS_TESORERIA',
	   	SUBMENU:  [
	   		{ WIN: BCS_VENTANA.CrearCargoAbono , TITULO: 'UI_INGRESAR_CARGO_ABONO_MANUAL' },
	   		{ WIN: BCS_VENTANA.AgenteSeparadorOperacionFFMM , TITULO: 'UI_FONDOS_MUTUOS' },
	   	]    	    	
  },
  {
	   	WIN: BCS_VENTANA.MODULO_AGENTE , TITULO: 'UI_CUSTODIA',
	   	SUBMENU:  [
	   		{ WIN: BCS_VENTANA.CertificadoCustodia , TITULO: 'UI_CERTIFICADO_CUSTODIA' },
	   		{ WIN: BCS_VENTANA.ConsultarCustodia , TITULO: 'UI_MENU_CUST_CONSULTAR_CUSTODIA' },
	   		{ WIN: BCS_VENTANA.BlotterGarantia , TITULO: 'UI_MENU_CUST_GARANTIAS' },
	   	]    	    	
  },
  {
		WIN: BCS_VENTANA.MODULO_MERCADO , TITULO: 'UI_INFORMACION_MERCADO',
    	SUBMENU:  [
    		{ WIN: BCS_VENTANA.ConsultarPreciosInstrumentos , TITULO: 'UI_PRECIOS_DE_INSTRUMENTOS' },
    		{ WIN: BCS_VENTANA.ConsultarPreciosSistema , TITULO: 'UI_CONSULTAR_PRECIOS_SISTEMA' }
		]
  }
];

//##############################################################################################
//##############################################################################################
/*
  __  __                            ____                            _____           _   _ 
 |  \/  |   ___   _ __    _   _    |  _ \    ___   _ __   _   _    |  ___|  _   _  | | | |
 | |\/| |  / _ \ | '_ \  | | | |   | |_) |  / _ \ | '__| | | | |   | |_    | | | | | | | |
 | |  | | |  __/ | | | | | |_| |   |  __/  |  __/ | |    | |_| |   |  _|   | |_| | | | | |
 |_|  |_|  \___| |_| |_|  \__,_|   |_|      \___| |_|     \__,_|   |_|      \__,_| |_| |_|
                                                                                          
*/
//##############################################################################################
//##############################################################################################
var BCS_FULLMENU_PERU = [
    //SOCIO NEGOCIO
    {
    	WIN: BCS_VENTANA.MODULO_SOCIO_NEGOCIO , TITULO: 'UI_SOCIO_NEGOCIO',
    	SUBMENU:  [
    	      { SEPARATE: true , TITULO: 'UI_INFORMACION' },
    	      //{ WIN: BCS_VENTANA.AgenteConsultarResumemEjecutivo , TITULO: 'UI_RESUMEN_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.ConsultarSocioNegocio , TITULO: 'UI_GESTION_SOCIO_NEGOCIO' },   
    	      { WIN: BCS_VENTANA.AgenteConsultaAvanzadaClientes , TITULO: 'UI_CONSULTA_AVANZADA' }, 
    	      { WIN: BCS_VENTANA.ResumenInversionCliente , TITULO: 'UI_RESUMEN_INVERSION_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.ImprimirArchivosContratos , TITULO: 'UI_IMPRIMIR_ARCHIVOS_CONTRATOS' }, 
    	      { SEPARATE: true , TITULO: 'UI_INGRESOS' },
    	      { WIN: BCS_VENTANA.CrearCliente , TITULO: 'UI_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.CrearPersona , TITULO: 'UI_PERSONA' }, 
    	      { WIN: BCS_VENTANA.CrearEmpleado , TITULO: 'UI_EMPLEADO' }, 
    	      { WIN: BCS_VENTANA.CrearContraparte , TITULO: 'UI_CONTRAPARTE' }, 
    	      { WIN: BCS_VENTANA.CrearProveedor , TITULO: 'UI_PROVEEDOR' },
		{ WIN: BCS_VENTANA.CrearClienteFinal , TITULO: 'UI_CLIENTE_FINAL' },
    	      { WIN: BCS_VENTANA.CrearClienteNomina , TITULO: 'UI_CLIENTE_NOMINA' },
    	      { SEPARATE: true , TITULO: 'UI_UTILITARIOS' },
    	      { WIN: BCS_VENTANA.ConsultarSupervisor , TITULO: 'UI_SUPERVISORES' },
    	      { WIN: BCS_VENTANA.ConsultarEjecutivo , TITULO: 'UI_EJECUTIVOS' },
    	      { WIN: BCS_VENTANA.CarteraEjecutivo , TITULO: 'UI_CARTERAS_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.BloqueoMasivoClientes , TITULO: 'UI_BLOQUEO_MASIVO' },
    	      { WIN: BCS_VENTANA.ActivacionMasivaClientes , TITULO: 'UI_ACTIVACION_MASIVA' },
    	      { WIN: BCS_VENTANA.BloqueoAutomaticoClientes , TITULO: 'UI_BLOQUEO_AUTOMATICO' },
    	]    		
    },
    //ORDENES
    {    	
    	WIN: BCS_VENTANA.MODULO_ORDEN , TITULO: 'UI_OPERACIONES_EN_RUEDA',
    	SUBMENU:  [
    		{ WIN: BCS_VENTANA.BlotterOrdenes , TITULO: 'UI_GESTION_ORDENES' },
    		{ WIN: BCS_VENTANA.ConsultarPosicion , TITULO: 'UI_CONSULTA_DE_POSICION' },
    		{ WIN: BCS_VENTANA.ConsultarPosicionAsignacion , TITULO: 'UI_CONSULTA_DE_POSICION_ASIGNACION' },//CGO
    		{ WIN: BCS_VENTANA.ConsultarPreciosInstrumentos , TITULO: 'UI_INFORMACION_MERCADO' },
		{ WIN: BCS_VENTANA.ConsultarTransaccionAnuladasModificadas , TITULO: 'UI_CONSULTA_TRANSACCIONES_ANULADAS_MODIFICADAS' },
    	      { SEPARATE: true , TITULO: 'UI_INGRESOS' },
    	      { WIN: BCS_VENTANA.IngresarOrdenMain , TITULO: 'UI_ORDEN_RV' },
    	      { WIN: BCS_VENTANA.IngresarOrdenRF , TITULO: 'UI_ORDEN_RF' },    	      
    	      { WIN: BCS_VENTANA.CrearTransaccion , TITULO: 'UI_TRANSACCION' }, 
    	      { WIN: BCS_VENTANA.IngresarOrdenOpcionesPE , TITULO: 'UI_INGRESAR_ORDEN_OPCION' },
    	      { SEPARATE: true , TITULO: 'UI_OPERACIONES' },
    	      { WIN: BCS_VENTANA.InterfazAsignacion , TITULO: 'UI_INTERFAZ_ASIGNACION' },
    	      { WIN: BCS_VENTANA.AsignacionPropCrono , TITULO: 'UI_ASIGNACION_AUTOMATICA' }, 
    	      { WIN: BCS_VENTANA.EjecutarDesasignacion , TITULO: 'UI_EJECUTAR_DESASIGNACION' }, 
    	      { WIN: BCS_VENTANA.BlotterOrdenesNegociacion , TITULO: 'UI_NEGOCIACION_ORDENES' }, 
    	      { WIN: BCS_VENTANA.EnvioFacturarMasivo , TITULO: 'UI_ENVIO_FACTURACION' }, 
    	      
    	      
    	      
    	      { WIN: BCS_VENTANA.CargaMasivaOperacionesEnRueda , TITULO: 'UI_CARGA_MASIVA_OPERACIONES_EN_RUEDA' }, 
    	      
    	      
            
    	      { WIN: BCS_VENTANA.CargaArchivosOrdenes , TITULO: 'UI_CARGA_ARCHIVOS_OPERACIONES_RUEDA' }, 
    	      { WIN: BCS_VENTANA.GenerarInformeOperacionesRueda , TITULO: 'UI_GENERAR_INFORMES' }, 
	      { WIN: BCS_VENTANA.VencimientoOrdenes , TITULO: 'UI_VENCIMIENTO_ORDENES' },
    	      { SEPARATE: true , TITULO: 'UI_APERTURA_CIERRE' },
    	      { WIN: BCS_VENTANA.AperturaModuloOrdenes , TITULO: 'UI_APERTURA_MODULO' },

    	    //{ WIN: BCS_VENTANA.CuadraturaCierreDiario , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { WIN: BCS_VENTANA.CuadraturaDiariaOperaciones , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_CONFIGURACION' },
    	      { WIN: BCS_VENTANA.AdministrarServTransacciones , TITULO: 'UI_OPERADORES_RUEDA' },
    	      { WIN: BCS_VENTANA.ConsultarCorredorCorresponsal , TITULO: 'UI_CORREDORES_CORRESPONSALES' },
    	      { WIN: BCS_VENTANA.ConsultarGruposPortafolio , TITULO: 'UI_GRUPOS_DE_PORTAFOLIOS' },
    	      { WIN: BCS_VENTANA.MaestroLibroOrdenes , TITULO: 'UI_LIBROS_DE_ORDENES' },
    	      { WIN: BCS_VENTANA.DesbloquearTransaccionesSistema , TITULO: 'UI_DESBLOQUEAR_OPERACIONES' },
    	]    	    	
    },
    //REPORTE
    {    	
    	WIN: BCS_VENTANA.MODULO_REPORTE, TITULO: 'UI_REPORTE',
    	SUBMENU:  [
    	          { WIN: BCS_VENTANA.BlotterOperacionesReportes, TITULO: 'UI_GESTION_ORDENES'},
    	          { SEPARATE: true , TITULO: 'UI_INGRESOS' },
    	          { WIN: BCS_VENTANA.IngresoReportantes, TITULO: 'UI_REPORTANTES'},
    	          { WIN: BCS_VENTANA.IngresoOperacionesReporte, TITULO: 'UI_REPORTADO'},
    	          { SEPARATE: true , TITULO: 'UI_OPERACIONES' },
    	          { WIN: BCS_VENTANA.BlotterOrdenesReporteNegociacion, TITULO: 'UI_NEGOCIACION'},
    	          { WIN: BCS_VENTANA.BlotterCustodiaReportes, TITULO: 'UI_CUSTODIA'}
    	]    	    	
    },    
    //CUENTA_INVERSION
    {    	
    	WIN: BCS_VENTANA.MODULO_CUENTA_INVERSION , TITULO: 'UI_CUENTA_INVERSION',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.CrearCargoAbono , TITULO: 'UI_INGRESAR_CARGO_ABONO_MANUAL' },
    	    //CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
    	      { WIN: BCS_VENTANA.CargaMasivaCuentaInversion , TITULO: 'UI_INGRESO_MASIVO_CARGO_ABONO' },
    	      { WIN: BCS_VENTANA.CuentaInversionCliente , TITULO: 'UI_CUENTA_INVERSION_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.LiquidarSaldo , TITULO: 'UI_LIQUIDACION_DE_SALDOS' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioCTA, TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_MANTENEDORES' },
    	      { WIN: BCS_VENTANA.MantenedorCuentaInversion , TITULO: 'UI_MANTENEDOR_CUENTA_INVERSION' },
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.CartolaDeMovimientos , TITULO: 'UI_CARTOLA_MOVIMIENTOS' },
    	      { WIN: BCS_VENTANA.CuentaInversionEjecutivo , TITULO: 'UI_CONSULTA_SALDO_MOVIMIENTOS' }, 
    	      { WIN: BCS_VENTANA.CuentasPorPagarYCobrar , TITULO: 'UI_CUENTAS_COBRAR_CUENTAS_PAGAR' }, 
    	      { WIN: BCS_VENTANA.CuadraturaSaldosCuentaInversion , TITULO: 'UI_CONSULTA_CUADRATURA_SALDOS_CUENTAS_INVERSION' }, 
		]    	    	
    },
    //CUSTODIA
    {    	
    	WIN: BCS_VENTANA.MODULO_CUSTODIA , TITULO: 'UI_CUSTODIA',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.IngresarTitulo , TITULO: 'UI_MENU_CUST_INGRESAR_RETIRAR_TITULO' },
    	      //{ WIN: BCS_VENTANA.IngresarTituloTest , TITULO: 'TEST INGRESO RETIRO TITULOS' },  
    	      { WIN: BCS_VENTANA.BlotterGarantia , TITULO: 'UI_MENU_CUST_GARANTIAS' }, 
    	      { WIN: BCS_VENTANA.BlotterEventoCapital , TITULO: 'UI_TITULO_EVENTO_CAPITAL' },
			  //{ WIN: BCS_VENTANA.BlotterSimultanea , TITULO: 'UI_SIMULTANEAS' }, 
			  //{ WIN: BCS_VENTANA.BlotterPrestamos , TITULO: 'UI_MENU_CUST_PRESTAMOS_Y_EMPRESTITOS' }, 
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioCUS , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
			  { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.MovimientoEnTransito , TITULO: 'UI_MENU_CUST_MOVIMIENTOS_EN_TRANSITO' }, 
    	      { WIN: BCS_VENTANA.ConsultarCarteras , TITULO: 'UI_MENU_CUST_CONSULTAR_CARTERAS' },
    	      { WIN: BCS_VENTANA.ConsultarCustodia , TITULO: 'UI_MENU_CUST_CONSULTAR_CUSTODIA' },     	     
    	      { WIN: BCS_VENTANA.ConsultarFlujoInstrumento , TITULO: 'UI_MENU_CUST_DETALLE_FLUJO_INSTRUMENTO' }, 
    	      { WIN: BCS_VENTANA.RevisionCoberturasGarantias , TITULO: 'UI_COBERTURA_GARANTIAS' }, 
			  //{ WIN: BCS_VENTANA.BlotterPactosCorteSorteo , TITULO: 'UI_MENU_CUST_PACTOS_CORTE_SORTEO' }, 
			  //{ WIN: BCS_VENTANA.ConsultarJuntaAccionistas , TITULO: 'UI_CONSULTAR_JUNTA_ACCIONISTAS' },
			  //{ WIN: BCS_VENTANA.BlotterPagoCorteCupon , TITULO: 'UI_PAGO_CORTE_CUPON' },
    	      { SEPARATE: true , TITULO: 'UI_PROCESOS' },
    	      { WIN: BCS_VENTANA.BlotterCuadraturaCustodia , TITULO: 'UI_CUADRATURA_CUSTODIA' }, 
			  { WIN: BCS_VENTANA.BlotterOperacionesPendientes , TITULO: 'UI_TITULO_OPERACIONES_PENDIENTES' }, 
			  { WIN: BCS_VENTANA.BlotterCargaMasivaCustodia , TITULO: 'UI_CARGA_MASIVA_CUSTODIA' },
			  //{ WIN: BCS_VENTANA.VencimientoSimultanea , TITULO: 'UI_TITULO_VENCIMIENTO_SIMULTANEA' }, 
			  //{ WIN: BCS_VENTANA.VencerPrestamos , TITULO: 'UI_MENU_CUST_VENCER_PRESTAMOS' },
			  //{ WIN: BCS_VENTANA.VencimientoPactos , TITULO: 'UI_TITULO_VENCIMIENTO_PACTOS' },
			  { WIN: BCS_VENTANA.ValorizacionCustodia , TITULO: 'UI_MENU_CUST_VALORIZACION_CUSTODIA' }, 
			  //{ WIN: BCS_VENTANA.BlotterCargaMasivaCustodia , TITULO: 'UI_CARGA_MASIVA_CUSTODIA' }, 
    	      //{ WIN: BCS_VENTANA.BlotterTraspasoDCV , TITULO: 'UI_TITULO_TRANSFERENCIA_DE_VALORES_AL_DEPOSITO' },
			  { WIN: BCS_VENTANA.TarjetaExistencia , TITULO: 'UI_TARJETA_EXISTENCIA' }, 
		]    	    	
    },
    //FACTURACION
    {    	
    	WIN: BCS_VENTANA.MODULO_FACTURACION , TITULO: 'UI_FACTURACION',
    	SUBMENU:  [
    	      //{ WIN: BCS_VENTANA.IngresarMovimiento , TITULO: 'UI_INGRESAR_MOVIMIENTO' },
    	      { WIN: BCS_VENTANA.BlotterFacturacion , TITULO: 'UI_GESTION_FACTURACION' },   
    	      { WIN: BCS_VENTANA.BlotterLibroDte , TITULO: 'UI_LIBRO_DTE' },
			  { WIN: BCS_VENTANA.EmisionFacturacionMasiva , TITULO: 'UI_EMISION_FACTURACION_MASIVA' },	
			  { WIN: BCS_VENTANA.CargaMasivaFacturacion , TITULO: 'UI_CARGA_MASIVA' },
			  { WIN: BCS_VENTANA.PeriodoCierre , TITULO: 'UI_PERIODO_CIERRE' },
			  { WIN: BCS_VENTANA.CuadraturaCierreDiarioFAC , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.CuadraturaTransaccionFacturacion , TITULO: 'UI_CUADRATURA_TRANSACCION_FACTURACION' }, 
    	      { WIN: BCS_VENTANA.ConsultaGeneralFacturacion , TITULO: 'UI_CONSULTA_GENERAL_FACTURACION' }, 
    	      { WIN: BCS_VENTANA.ContratoCobroPeriodico , TITULO: 'UI_CONTRATOS_COBROS_PERIODICOS' }, 
    	      { WIN: BCS_VENTANA.ConsultaCobrosClientes , TITULO: 'UI_CONSULTA_COBROS_CLIENTE' }, 
			  { SEPARATE: true , TITULO: 'UI_INFORMES' },
    	      { WIN: BCS_VENTANA.GenerarInterfazAfp , TITULO: 'UI_INTERFAZ_AFP' }, 

		]    	    	
    },
    //TESORERIA
    {    	
    	WIN: BCS_VENTANA.MODULO_TESORERIA , TITULO: 'UI_TESORERIA',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.CrearIngresoEgreso , TITULO: 'UI_CREAR_INGRESO_EGRESO' },
    	      { WIN: BCS_VENTANA.BlotterComprobantesTesoreria , TITULO: 'UI_GESTION_TESORERIA' },   
    	      { WIN: BCS_VENTANA.ClearingTesoreria , TITULO: 'UI_CLEARING' }, 
    	      { WIN: BCS_VENTANA.GestionContrapartes , TITULO: 'UI_GESTION_CONTRAPARTES' },
    	      { WIN: BCS_VENTANA.BlotterNominasTransferencias , TITULO: 'UI_GESTION_NOMINA_TRANSFERENCIA' },     	     
    	      { WIN: BCS_VENTANA.BlotterDepositos , TITULO: 'UI_GESTION_DEPOSITOS' }, 
    	      { WIN: BCS_VENTANA.CargaMasivaTesoreria , TITULO: 'UI_CARGA_MASIVA' },
    	    //{ WIN: BCS_VENTANA.RevisionCierreDiario , TITULO: 'UI_REVISION_CIERRE_DIARIO' },
    	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioTES , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	      { SEPARATE: true , TITULO: 'UI_MANTENEDORES' },
    	      { WIN: BCS_VENTANA.CajasTesoreria , TITULO: 'UI_MANTENEDOR_CAJA' }, 
    	      { WIN: BCS_VENTANA.MonedasTesoreria , TITULO: 'UI_MANTENEDOR_MONEDAS_TESORERIA' }, 
    	      { WIN: BCS_VENTANA.BancosTesoreria , TITULO: 'UI_MANTENEDOR_CUENTA_BANCARIA' }, 
    	      { SEPARATE: true , TITULO: 'UI_CONSULTAS' },
    	      { WIN: BCS_VENTANA.ConsultaSaldoBanco , TITULO: 'UI_CONSULTA_SALDO_BANCO' }, 
    	      { WIN: BCS_VENTANA.CuadraturaComprobantesCuentaInversion , TITULO: 'UI_CUADRATURA_COMPROBANTES_CUENTAINVERSION' }, 
    	      { WIN: BCS_VENTANA.LibretaBanco , TITULO: 'UI_LIBRETA_BANCO' }, 			 
		]    	    	
    } ,
   //MESARF
    {    	
    	WIN: BCS_VENTANA.MODULO_MESARF , TITULO: 'UI_RENTA_FIJA_E_IIF',
    	SUBMENU:  [
    		{ WIN: BCS_VENTANA.MesaRentaFija , TITULO: 'UI_GESTION_MESA_RENTA_FIJA' },
    		{ WIN: BCS_VENTANA.MantenedorMesas , TITULO: 'UI_MANTENEDOR_MESAS' },
    		{ WIN: BCS_VENTANA.ConsultaCarteraMesaRentaFija , TITULO: 'UI_CARTERA_RENTA_FIJA' },
    		{ WIN: BCS_VENTANA.CuadraturaCierreDiarioMRF , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' },
    	    //{ WIN: BCS_VENTANA.IngresoDepositosDesembolsosMesaRF , TITULO: 'UI_INGRESAR_OPERACION' },
		]    	    	
    },
  //PLATAFORMA
    {    	
    	WIN: BCS_VENTANA.MODULO_PLATAFORMA , TITULO: 'UI_PLATAFORMA',
    	SUBMENU:  [
			  { SEPARATE: true , TITULO: 'UI_AUTORIZACION' },	
    	      { WIN: BCS_VENTANA.Autorizaciones , TITULO: 'UI_AUTORIZACIONES' },
    	      { WIN: BCS_VENTANA.ConsultaUsuarios , TITULO: 'UI_CONSULTA_USUARIOS' },
    	      { WIN: BCS_VENTANA.ConsultarRoles , TITULO: 'UI_CONSULTAR_ROLES' },
    	      { WIN: BCS_VENTANA.ConsultarPerfiles , TITULO: 'UI_CONSULTAR_PERFILES' },
    	      { WIN: BCS_VENTANA.ConsultarAccesosUsuarios , TITULO: 'UI_CONSULTAR_ACCESOS_USUARIO' }, 
    	      { SEPARATE: true , TITULO: 'UI_INSTRUMENTOS' }, 
    	      { WIN: BCS_VENTANA.Clasificaciondeactivos , TITULO: 'UI_CLASIFICACION_DE_ACTIVOS' },
    	      { WIN: BCS_VENTANA.ConsultarInstrumentos , TITULO: 'UI_CONSULTAR_INSTRUMENTOS' },
    	      { WIN: BCS_VENTANA.CarteraInversion , TITULO: 'UI_CARTERAS_INVERSION' },
    	      { WIN: BCS_VENTANA.CargaEntidadVectorPrecio , TITULO: 'UI_CARGA_VECTOR_PRECIO' },
			  { WIN: BCS_VENTANA.ConsultarVectorPrecio , TITULO: 'UI_CONSULTAR_VECTOR_PRECIO' },
			  { SEPARATE: true , TITULO: 'UI_COMERCIAL' },				  
    	      { WIN: BCS_VENTANA.administrarModelosComerciales , TITULO: 'UI_MODELOS_COMERCIALES' }, 
    	      { WIN: BCS_VENTANA.ConfiguracionCobros , TITULO: 'UI_CONFIGURACION_COBROS' }, 
    	      { WIN: BCS_VENTANA.AdministrarModelosTipoCliente , TITULO: 'UI_MODELOS_TIPO_CLIENTE' }, 
    	      { WIN: BCS_VENTANA.administrarRequisitosOperativos , TITULO: 'UI_REQUISITOS_OPERATIVOS' }, 
    	      { WIN: BCS_VENTANA.administrarPlantillasEjecutivos , TITULO: 'UI_PLANTILLAS_EJECUTIVO' },
    	      { WIN: BCS_VENTANA.MantenedorIndicadores , TITULO: 'UI_MANTENEDOR_INDICADORES' },
			  { WIN: BCS_VENTANA.ConfiguracionEncuesta , TITULO: 'UI_CONFIGURACION_ENCUESTA' },
    	      { WIN: BCS_VENTANA.MantenedorTipoRelaciones , TITULO: 'UI_MANTENEDOR_TIPO_RELACIONES' },
    	      { WIN: BCS_VENTANA.MantenedorDocumentos , TITULO: 'UI_MANTENEDOR_DOCUMENTOS' },
    	      { WIN: BCS_VENTANA.AdministrarPreferenciasPago , TITULO: 'UI_ADMINISTRAR_PREFERENCIAS_PAGO' },
			  { SEPARATE: true , TITULO: 'UI_NEGOCIO' },
    	      { WIN: BCS_VENTANA.CierreModulos , TITULO: 'UI_CIERRE_MODULOS' },
    	      { WIN: BCS_VENTANA.ConfiguracionFlujoCaja , TITULO: 'UI_CONFIGURACION_FLUJO_CAJA' },
			  { WIN: BCS_VENTANA.ConfiguracionFlujoCustodia , TITULO: 'UI_CONFIGURACION_FLUJO_CUSTODIA' },
    	      { WIN: BCS_VENTANA.MantenedorCobertura , TITULO: 'UI_MANTENEDOR_COBERTURA' },
    	      { WIN: BCS_VENTANA.MantenedorLimites , TITULO: 'UI_MANTENEDOR_LIMITES'},
			  { WIN: BCS_VENTANA.ConsultarLimites , TITULO: 'UI_CONSULTAR_LIMITES' },
    	      { WIN: BCS_VENTANA.MantenedorExigibilidad , TITULO: 'UI_MANTENEDOR_EXIGIBILIDAD' },
			  { SEPARATE: true , TITULO: 'UI_CONTROL_SISTEMA' },
			  { WIN: BCS_VENTANA.AperturaCierreModulos , TITULO: 'UI_MODULOS' },
    	      { WIN: BCS_VENTANA.MantenedorPauta , TITULO: 'UI_PAUTA'},
			  { SEPARATE: true , TITULO: 'UI_CONTROL' },
			  { WIN: BCS_VENTANA.ConsultaLog , TITULO: 'UI_CONSULTA_LOGS' },
    	      { WIN: BCS_VENTANA.MantenedorAgenda , TITULO: 'UI_CONSULTAR_AGENDA' },
    	      { WIN: BCS_VENTANA.MantenedorPrograma , TITULO: 'UI_CONSULTAR_PROGRAMAS' },
    	      { WIN: BCS_VENTANA.ConsultaGeneralRegistrosSistema , TITULO: 'UI_CONSULTA_REGISTRO_SISTEMA'},
			  { SEPARATE: true , TITULO: 'UI_ADMINISTRAR_NOTIFICACIONES' },
			  { WIN: BCS_VENTANA.MantenedorEventos , TITULO: 'Maestro Notificaciones' },
    	      { WIN: BCS_VENTANA.MantenedorComentenos , TITULO: 'Comentenos (Dummy)' },
    	      { WIN: BCS_VENTANA.MantenedorNotificaciones , TITULO: 'Consultar Notificaciones'},
			  { SEPARATE: true , TITULO: 'PlataformaSeparadorTasas' },
			  { WIN: BCS_VENTANA.SolicitudTasas , TITULO: 'UI_TASAS' },
    	      { WIN: BCS_VENTANA.CargaMantenedorTasas , TITULO: 'UI_CARGA_Y_MANTENEDOR_DE_TASAS' },
    	      { WIN: BCS_VENTANA.CriterioSeleccionTasas , TITULO: 'UI_CRITERIO_DE_SELECCION_DE_TASAS'},
			  { WIN: BCS_VENTANA.MargenesTasas , TITULO: 'UI_MANTENEDOR_MARGENES_TASAS' },
			  { SEPARATE: true , TITULO: 'UI_OTROS' },
    	      { WIN: BCS_VENTANA.AjusteHorario , TITULO: 'UI_AJUSTE_HORARIO' },
    	      { WIN: BCS_VENTANA.AjusteFechaSistema , TITULO: 'UI_AJUSTE_FECHA_SISTEMA' },
    	      //{ WIN: BCS_VENTANA.ConsultarParidades , TITULO: 'UI_PARIDADES_Y_TIPO_CAMBIO'},
    	      { WIN: BCS_VENTANA.MantenedorParametros , TITULO: 'Mantenedor Parametros' },
			  { WIN: BCS_VENTANA.MantenedorGeneral , TITULO: 'Mantenedor General' },
			  { WIN: BCS_VENTANA.AdministrarBoveda , TITULO: 'UI_BOVEDAS' },
			  { WIN: BCS_VENTANA.MantenedorBrokerExtranjero , TITULO: 'UI_BROKERS' },
			  { WIN: BCS_VENTANA.MantenedorBancoCustodio , TITULO: 'UI_BANCOS_CUSTODIOS' },
    	      { WIN: BCS_VENTANA.AdministrarTipoOrigenFondo , TITULO: 'UI_TIPO_ORIGEN_FONDO'},
			  { WIN: BCS_VENTANA.RelacionInstrumento , TITULO: 'UI_RELACION_INSTRUMENTO' },
    	      { WIN: BCS_VENTANA.MantenedorConceptosManuales , TITULO: 'UI_MANTENEDOR_CONCEPTOS_MANUALES' },
    	      { WIN: BCS_VENTANA.MantenedorArchivosContratos , TITULO: 'UI_MANTENEDOR_ARCHIVOS_CONTRATOS'},
			  { WIN: BCS_VENTANA.MantenedorDatosContratos , TITULO: 'UI_MANTENEDOR_DATOS_CONTRATOS' },
    	      { WIN: BCS_VENTANA.MantenedorImagenes , TITULO: 'UI_MANTENEDOR_IMAGENES' },
    	      { WIN: BCS_VENTANA.MantenedorTemplates , TITULO: 'UI_MANTENEDOR_TEMPLATES'},
			  { WIN: BCS_VENTANA.MantenedorBoletines , TITULO: 'UI_MANTENEDOR_BOLETINES'},
			  { WIN: BCS_VENTANA.AdmDiccionario , TITULO: 'Diccionario ' },
    	      
			  { WIN: BCS_VENTANA.MantenedorDiasFeriados , TITULO: 'UI_MANTENEDOR_DIAS_FERIADOS' },
			  { WIN: BCS_VENTANA.PreferenciaCalceLibroInventario , TITULO: 'UI_PREFERENCIA_CALCE_LIBRO_INVENTARIO' },
			  { WIN: BCS_VENTANA.BlotterCargaMasivaPlataforma , TITULO: 'UI_CARGA_MASIVA_PLATAFORMA' },
			  { SEPARATE: true , TITULO: 'UI_MERCADO' },
    	      { WIN: BCS_VENTANA.ParidadYTipoCambio , TITULO: 'UI_PARIDAD_Y_TIPOCAMBIO'},
    	      { WIN: BCS_VENTANA.MantenedorEmisores , TITULO: 'UI_EMISORES'},
			  { WIN: BCS_VENTANA.MaestroParametrosFinanciero , TITULO: 'UI_TIPOS_CAMBIO_E_INDICES'},
    	      { WIN: BCS_VENTANA.ConsultaUsoParametroFinanciero , TITULO: 'UI_CONSULTA_USO_PARAMETRO_FINANCIERO'},
			  { SEPARATE: true , TITULO: 'UI_VALORIZADOR' },
    	      { WIN: BCS_VENTANA.ConsultaCupones, TITULO: 'UI_CONSULTA_CUPONES'},
    	      { WIN: BCS_VENTANA.ValorizadorInstrumento, TITULO: 'UI_VALORIZADOR_INSTRUMENTO'},
	      { WIN: BCS_VENTANA.ValorizadorBono, TITULO: 'UI_VALORIZADOR_BONO'},
    	      { WIN: BCS_VENTANA.ValorizadorPagare, TITULO: 'UI_VALORIZADOR_PAGARE'},
    	      { WIN: BCS_VENTANA.ValorizadorCartera, TITULO: 'UI_VALORIZADOR_CARTERA'},
    	      { WIN: BCS_VENTANA.ConsultaConfigValorizador, TITULO: 'UI_CONSULTA_CONFIG_VALORIZADOR'},
    	]    	    	
    },
    //NORMATIVA
    {    	
    	WIN: BCS_VENTANA.MODULO_NORMATIVO , TITULO: 'UI_NORMATIVA',
    	SUBMENU:  [
    	      { SEPARATE: true , TITULO: 'UI_NORMATIVA' },
    	      { WIN: BCS_VENTANA.Circulares , TITULO: 'UI_CIRCULARES' }, 
			  { WIN: BCS_VENTANA.ConsultarLibroVentas , TITULO: 'UI_LIBRO_DE_VENTAS' },		//	  
		      { SEPARATE: true , TITULO: 'UI_CARTOLAS' },
              { WIN: BCS_VENTANA.ProcesosCartolasConsolidada , TITULO: 'UI_PROCESOS_CARTOLAS_CONSOLIDADA' }, 
		      { WIN: BCS_VENTANA.ProcesosCartolas , TITULO: 'UI_PROCESOS_CARTOLAS' }, 
		      { WIN: BCS_VENTANA.ConsultaCartolas , TITULO: 'UI_CONSULTA_CARTOLAS' }, 
		      { WIN: BCS_VENTANA.ConsultaCartolasConsolidada , TITULO: 'UI_CONSULTA_CARTOLAS_CONSOLIDADA' }, 
		      //{ WIN: BCS_VENTANA.PreferenciasCartolas , TITULO: 'UI_PREFERENCIAS_CARTOLAS' },  		      
		      
		      
    	]    	    	
    },
    //CONTABILIDAD
    {    	
    	WIN: BCS_VENTANA.MODULO_CONTABILIDAD , TITULO: 'UI_INTERFAZ_CONTABLE',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.InterfazContable , TITULO: 'UI_GESTION_INTERFAZ_CONTABLE' },
    	      { WIN: BCS_VENTANA.ConfiguracionComprobantes , TITULO: 'UI_CONFIGURACION_COMPROBANTES' }, 
	          { WIN: BCS_VENTANA.MantenedorMetadataContabilidad , TITULO: 'UI_CONFIGURACION_METADATA' },
    	      { WIN: BCS_VENTANA.AdministrarClaseEmisor , TITULO: 'UI_CLASE_EMISOR' },
    	      { WIN: BCS_VENTANA.AdministrarCentroCosto , TITULO: 'UI_CENTRO_COSTO' },
	      { WIN: BCS_VENTANA.CuadraturaCierreDiarioCON , TITULO: 'UI_CUADRATURA_CIERRE_DIARIO' }
			  
    	]    	    	
    },
	//INTERFACES
    {
    	WIN: BCS_VENTANA.MODULO_INTERFACES, TITULO: 'UI_INTERFACES',
    	SUBMENU: [
    	          { WIN: BCS_VENTANA.PanelSolicitudInterfaces, TITULO: 'UI_SOLICITAR_INTERFACES'},
    	          { WIN: BCS_VENTANA.MantenedorSolicitudesInterfaces , TITULO: 'UI_CONSULTAR_SOLICITUDES_INTERFACES' },
    	          { WIN: BCS_VENTANA.CargaDatosInterfaces , TITULO: 'UI_CARGAR_ARCHIVOS_INTERFACES' },
    	          { WIN: BCS_VENTANA.EnvioArchivoInterfaces , TITULO: 'UI_ENVIO_ARCHIVO_INTERFACES' },
    	          { WIN: BCS_VENTANA.CargaArchivosTransaccionesOrdenes , TITULO: 'UI_CARGA_ARCHIVOS_TRANSACCIONES_ORDENES' },
		  { WIN: BCS_VENTANA.EnvioArchivoCM , TITULO: 'UI_ENVIO_ARCHIVO_CM' }
    	]
    	
    },
	//MANTENCION
    {
    	WIN: BCS_VENTANA.MODULO_INTERFACES, TITULO: 'UI_MANTENCION', qa : true,
    	SUBMENU: [
    		{ WIN: BCS_VENTANA.ConsultarServicios , TITULO: 'UI_CONSULTAR_SERVICIOS' }
    	]
    	
    },
    //ILS
    {    	
    	WIN: BCS_VENTANA.MODULO_ILS , TITULO: 'UI_ILS',
    	SUBMENU:  [
    	      { WIN: BCS_VENTANA.ILSMantenedor , TITULO: 'UI_ILS_MANTENEDOR_ILS' },
    	      { WIN: BCS_VENTANA.ILSMantenedorConcepto , TITULO: 'UI_ILS_MANTENEDOR_CONCEPTO' },   
    	      { WIN: BCS_VENTANA.ILSMantenedorIndice , TITULO: 'UI_ILS_MANTENEDOR_INDICE' }, 
    	      { WIN: BCS_VENTANA.ILSMantenedorInforme , TITULO: 'UI_ILS_MANTENEDOR_INFORME' }, 
    	      { WIN: BCS_VENTANA.ILSMantenedorItem , TITULO: 'UI_ILS_MANTENEDOR_ITEM' },     	     
		]    	    	
    }
    
    
];
