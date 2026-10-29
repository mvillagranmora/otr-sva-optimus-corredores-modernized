angular.module('ModuloOrdenes').factory('bcsConfiguracionOrdenesV2',function($translate,$rootScope,bcsConsultasOrdenes) {
	var obj = {};
	
	var configuracionColumnas = function() {
		return { 
			
			AccionOrden							:{field: "AccionOrden"							,title: 'UI_OPERACION'						,width: "100px"},
			AccionTipoOperacion					:{field: "AccionTipoOperacion"					,title: 'UI_OPERACION'						,width: "100px"			,template : "<span translate>#: AccionTipoOperacion # </span>",align: "left"},
			AsignadoDia				   			:{field: "AsignadoDia"							,title: 'UI_ASIGNADO_DIA'					,width: "100px"			,template : " <label> #:  (util.bcs_formato(AsignadoDia,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>"},
			
			Cantidad		   					:{field: "Cantidad"								,title: 'UI_CANTIDAD'						,width: "75px"			,template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			CantidadAsignada		   			:{field: "CantidadAsignada"						,title: 'UI_CANTIDAD_ASIGNADA'				,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadAsignada,'GEN','Cantidad',IdMonedaOperacion,IdTipoInstrumento,'')).valor #</label>"},
			
			CantidadAsignadaSimulacion 			:{field: "CantidadAsignadaSimulacion"			,title: 'UI_CANTIDAD_ASIGNADA'				,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadAsignadaSimulacion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			CantidadTransaccionSimulacion 		:{field: "CantidadTransaccionSimulacion"		,title: 'UI_CANTIDAD'						,width: "75px"			,template : " <label> #:  (util.bcs_formato(CantidadTransaccionSimulacion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			ValorRescateMontoTransaccion		:{field: "ValorRescateMontoTransaccion"			,title: 'UI_CANTIDAD'						,width: "75px"			,template : " <label> #:  (util.bcs_formato(ValorRescateMontoTransaccion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			
			CantidadAsignadaOrden   			:{field: "CantidadAsignadaOrden"				,title: 'UI_CANTIDAD_ASIGNADA'				,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadAsignadaOrden,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>"},
			CantidadAsignadaTransaccion			:{field: "CantidadAsignadaTransaccion"			,title: 'UI_CANTIDAD_ASIGNADA'				,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadAsignadaTransaccion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			CantidadDisponibleAsociar			:{field: "CantidadDisponibleAsociar"			,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponibleAsociar,'GEN','Cantidad',IdMonedaOrden, '','')).valor #</label>"},
			CantidadDisponibleCartera			:{field: "CantidadDisponible"					,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponible,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			CantidadDisponibleGarantiaCustodia	:{field: "CantidadDisponibleGarantiaCustodia"	,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponibleGarantiaCustodia,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			CantidadDisponibleOrden				:{field: "CantidadDisponibleOrden"				,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponibleOrden,'GEN','Cantidad',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
			CantidadDisponibleSubyacente		:{field: "CantidadDisponibleSubyacente"			,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponibleSubyacente,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			CantidadDisponibleTransaccion		:{field: "CantidadDisponibleTransaccion"		,title: 'UI_SALDO'							,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadDisponibleTransaccion,'GEN','Cantidad',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			CantidadOrden						:{field: "CantidadOrden"						,title: $rootScope.SesionLocale === 'CL'?'UI_CANTIDAD':'UI_CANTIDAD_VALOR_NOMINAL',	width: "100px",template : " <label> #:  (util.bcs_formato(CantidadOrden,'GEN','Cantidad',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
			CantidadSimulacionGarantiaCustodia	:{field: "CantidadSimulacionGarantiaCustodia"	,title: "UI_CANTIDAD_SIMULACION"			,width: "100px"			,attributes: { style : "text-align:right;background-color: lemonchiffon" },template : " <label> #:  (util.bcs_formato(CantidadSimulacionGarantiaCustodia,'GEN','Cantidad',IdMonedaInstrumento, '','')).valor #</label>"},
			CantidadSimulacionSimultanea		:{field: "CantidadSimulacionSimultanea"			,title: "UI_CANTIDAD_SIMULACION"			,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadSimulacionSimultanea,'GEN','Cantidad',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoAVenderSimulado                :{field: "MontoAVenderSimulado"			        ,title: "MONTO_VENDER_SIMULADO"			    ,width: "100px"			,template : " <label> #:  (util.bcs_formato(MontoAVenderSimulado,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			CorteMinimo							:{field: "CorteMinimo"							,title: $translate.instant('UI_CORTE_MINIMO'),width: "100px" 		,template : " <label> #:  (util.bcs_formato(CorteMinimo,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			CantidadSimulacionCartera			:{field: "CantidadSimulacionCartera"			,title: 'UI_CANTIDAD_SIMULACION'			,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadSimulacionCartera,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			CantidadSimulacionCliente			:{field: "CantidadSimulacionCliente"			,title: 'UI_CANTIDAD_SIMULACION'			,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadSimulacionCliente,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"},
			CantidadSimulacionOrden				:{field: "CantidadSimulacionOrden"				,title: 'UI_CANTIDAD_SIMULACION'			,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadSimulacionOrden,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>"},
			CantidadSimulacionTransaccion		:{field: "CantidadSimulacionTransaccion"		,title: 'UI_CANTIDAD_SIMULACION'			,width: "100px"			,template : " <label> #:  (util.bcs_formato(CantidadSimulacionTransaccion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			CantidadTransaccion					:{field: "CantidadTransaccion"					,title: 'UI_CANTIDAD'						,width: "75px"			,template : " <label> #:  (util.bcs_formato(CantidadTransaccion,'GEN','Cantidad',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			InstrumentoNegociacion				:{field: "InstrumentoNegociacion"				,title: 'UI_INSTRUMENTO_NEGOCIACION'		,width: "100px"},
			IdMonedaNegociacion					:{field: "IdMonedaNegociacion"					,title: 'UI_MONEDA_NEGOCIACION'				,width: "100px"},
			CodigoClasificacionTipoInstrumento	:{field: "CodigoClasificacionTipoInstrumento"	,title: 'UI_CLASIFICACION'					,width: "110px"},
			CodigoProcedenciaOrden				:{field: "CodigoProcedenciaOrden"				,title: 'UI_PROCEDENCIA'					,width: "100px"},
			
			CodigoPortafolio					:{field: "CodigoPortafolio"						,title: 'UI_PORTAFOLIO'						,width: "75px"},
			CodigoBoveda						:{field: "CodigoBoveda"							,title: 'UI_CUENTA_BOVEDA'					,width: "75px"},
			CodigoCondicionDeLiquidacion		:{field: "CodigoCondicionDeLiquidacion"			,title: 'UI_CONDICION_LIQUIDACION_ABR'		,width: "50px"},
			CodigoEmpleadoPortafolio			:{field: "CodigoEmpleadoPortafolio"				,title: 'UI_CODIGO_EJECUTIVO_PORTAFOLIO'	,width: "70px"},
			CodigoEmpleadoDigitador				:{field: "CodigoEmpleadoDigitador"				,title: 'UI_CODIGO_EJECUTIVO_DIGITADOR'		,width: "100px"},
			CodigoEmpleadoReserva				:{field: "CodigoEmpleadoReserva"				,title: 'UI_RESERVADO_POR'					,width: "70px"},
			CodigoCorredorCorresponsal			:{field: "CodigoCorredorCorresponsal"			,title: 'UI_CODIGO_CORREDOR_CORRESPONSAL'	,width: "100px"},
			CodigoCorredorComprador				:{field: "CodigoCorredorComprador"				,title: 'UI_CORREDOR_COMPRADOR'				,width: "100px"},
			CodigoCorredorVendedor				:{field: "CodigoCorredorVendedor"				,title: 'UI_CORREDOR_VENDEDOR'				,width: "100px"},
			CodigoIFRS							:{field: "CodigoIFRS"							,title: 'UI_CODIGO_IFRS'					,width: "100px"},
			CodigoOperadorContraparte			:{field: "CodigoOperadorContraparte"			,title: 'UI_OPERADOR_CONTRAPARTE'			,width: "100px"},
			CodigoOperadorCorredor				:{field: "CodigoOperadorCorredor"				,title: 'UI_OPERADOR_CORREDOR'				,width: "100px"},
			CodigoOperadorComprador				:{field: "CodigoOperadorComprador"				,title: 'UI_OPERADOR_COMPRADOR'				,width: "100px"},
			CodigoOperadorVendedor				:{field: "CodigoOperadorVendedor"				,title: 'UI_OPERADOR_VENDEDOR'				,width: "100px"},
			CodigoOperador						:{field: "CodigoOperador"						,title: 'UI_OPERADOR'						,width: "100px"},
			EsConfirmadaUI						:{field: "EsConfirmadaUI"						,title: 'UI_CONFIRMADA'						,width: "100px"},
			CorteMinimo							:{field: "CorteMinimo"							,title: "UI_CORTE_MINIMO"					,width: "100px"			,template : " <label> #:  (util.bcs_formato(CorteMinimo,'GEN','Monto',IdMoneda,'','')).valor #</label>",attributes : {style : "text-align:right;"}},
			
			
			RutCavali				 			:{field: "DocumentoIdentidadAdicional"			,title: 'UI_RUT_CAVALI'						,width: "90px"},
			
			
			DocumentoIdentidad		 			:{field: "DocumentoIdentidad"					,title: 'UI_DOCUMENTO_IDENTIDAD'			,width: "90px"},
			
			EsCuentaPropia						:{field: "EsCuentaPropia"						,title: $translate.instant('UI_CUENTA_PROPIA'),width: "100px"},
			EsFungible							:{field: "EsFungible"							,title: $translate.instant('UI_FUNGIBLE')	,width: "75px"},
			EsFungibleUI						:{field: "EsFungibleUI"							,title: $translate.instant('UI_FUNGIBLE')	,width: "85px"},			
			EsBilateralUI						:{field: "EsBilateralUI"						,title: $translate.instant('UI_BILATERAL')	,width: "75px"},			
			EsAPlazo							:{field: "EsAPlazo"								,title: $translate.instant('UI_A_PLAZO')	,width: "75px"},
			EsAPlazoUI							:{field: "EsAPlazoUI"							,title: $translate.instant('UI_A_PLAZO')	,width: "75px"},
			EsVentaCorta						:{field: "EsVentaCorta"							,title: $translate.instant('UI_VENTA_CORTA'),width: "50px"},
			EsVentaCortaUI						:{field: "EsVentaCortaUI"						,title: $translate.instant('UI_VENTA_CORTA'),width: "50px"},
			EsPorMontoUI						:{field: "EsPorMontoUI"							,title: $translate.instant('UI_POR_MONTO')	,width: "75px"},
			
			EstadoAsignacion					:{field: "EstadoAsignacion"						,title: 'UI_ESTADO_ASIGNACION'				,width: "150px"			,template : "#: (EstadoAsignacion || '').toUpperCase()  #"},
			EstadoOrden							:{field: "EstadoOrden"							,title: 'UI_ESTADO'							,width: "125px"			,template : "#: (EstadoOrden || '').toUpperCase()  #"},
			EstadoOrdenAsignacion				:{field: "EstadoOrdenAsignacion"				,title: 'UI_ESTADO_ASIGNACION'				,width: "125px"			,template : "#: (EstadoOrdenAsignacion || '').toUpperCase()  #"},
			EstadoPortafolio					:{field: "EstadoPortafolio"						,title: 'UI_ESTADO'							,width: "100px"			,template : "#: (EstadoPortafolio || '').toUpperCase()  #"},
			EstadoTransaccion					:{field: "EstadoTransaccion"					,title: 'UI_ESTADO'							,width: "100px"			,template : "#: (EstadoTransaccion || '').toUpperCase()  #"},
			
			IdAsignacion 						:{field: "IdAsignacion"							,title: 'UI_SECUENCIA_ASIGNACION'			,width: "80px"},
			IdAsignacionRef						:{field: "IdAsignacionRef"						,title: 'UI_ASIGNACION_RELACIONADA'			,width: "80px"},
			IdBolsa								:{field: "IdBolsa"								,title: 'UI_BOLSA'							,width: "75px"},
			IdBoveda							:{field: "IdBoveda"								,title: 'UI_BOVEDA'							,width: "75px"},
			IdCorredorContraparte				:{field: "IdCorredorContraparte"				,title: 'UI_CORREDOR_CONTRAPARTE'			,width: "75px"},
			IdComprobante		 				:{field: "IdComprobante"						,title: 'UI_SECUENCIA_COMPROBANTE'			,width: "80px"},
			IdConceptoMovimiento				:{field: "IdConceptoMovimiento"					,title: 'UI_CONCEPTO'						,width: "50px"},
			IdEmisor							:{field: "IdEmisor"								,title: 'UI_EMISOR'							,width: "100px"},
			IdEstadoAsignacion          		:{field: "IdEstadoAsignacion"					,title: 'UI_ESTADO'							,width: "70px"}, 
			IdEstadoComprobante					:{field: "IdEstadoComprobante"					,title: 'UI_ESTADO'							,width: "70px"},
			IdFormaCustodia						:{field: "IdFormaCustodia"						,title: 'UI_FORMA_CUSTODIA'					,width: "75px"},
			IdMoneda							:{field: "IdMoneda"								,title: 'UI_MONEDA'							,width: "50px"},
			IdMonedaEmision						:{field: "IdMonedaEmision"						,title: 'UI_MONEDA_EMISION'					,width: "50px"},
			IdMonedaInstrumento					:{field: "IdMonedaInstrumento"					,title: 'UI_MONEDA_INSTRUMENTO'							,width: "50px"},
			IdMonedaTransaccion					:{field: "IdMonedaTransaccion"					,title: 'UI_MONEDA'							,width: "50px"},
			IdMonedaOperacion					:{field: "IdMonedaOperacion"					,title: 'UI_MONEDA'							,width: "50px"},
			IdMonedaOrden						:{field: "IdMonedaOrden"						,title: 'UI_MONEDA_ORDEN'					,width: "100px"},
			IdMonedaLiquidacion					:{field: "IdMonedaLiquidacion"					,title: 'UI_MONEDA_LIQUIDACION'				,width: "50px"},
			IdMonedaLiquidacionOrden			:{field: "IdMonedaLiquidacionOrden"				,title: 'UI_MONEDA_LIQUIDACION'				,width: "50px"},
			IndicadorOrdenDirecta				:{field: "IndicadorOrdenDirecta"				,title: 'UI_INDICADOR_ORDEN_DIRECTA'		,width: "75px"},
			IdOrden		 						:{field: "IdOrden"								,title: 'UI_SECUENCIA_ORDEN'				,width: "80px"},
			IdTransaccion		 				:{field: "IdTransaccion"						,title: 'UI_SECUENCIA_TRANSACCION'			,width: "80px"},
			IdTipoComprobante	 				:{field: "IdTipoComprobante"					,title: 'UI_TIPO_COMPROBANTE'				,width: "75px"},
			IdTipoInstrumento	 				:{field: "IdTipoInstrumento"					,title: 'UI_TIPO_INSTRUMENTO'				,width: "75px"},
			IdTipoReajuste		 				:{field: "IdTipoReajuste"						,title: 'UI_TIPO_REAJUSTE'					,width: "75px"},
			Instrumento		 					:{field: "Instrumento"							,title: 'UI_NEMOTECNICO'					,width: "150px"},
			InstrumentoUI		 				:{field: "InstrumentoUI"						,title: 'UI_NEMOTECNICO'					,width: "150px"},
			InstrumentoNegociacionTran		 	:{field: "InstrumentoNegociacionTran"			,title: 'UI_INSTRUMENTO_NEGOCIACION'		,width: "150px"},
			InstrumentoNegociacionOrden		 	:{field: "InstrumentoNegociacionOrden"			,title: 'UI_INSTRUMENTO_NEGOCIACION'		,width: "150px"},
			InstrumentoSubyacente		 		:{field: "InstrumentoSubyacente"				,title: 'UI_SUBYACENTE'						,width: "110px"},
			IdClasificacionTipoInstrumento		:{field: "IdClasificacionTipoInstrumento"		,title: 'UI_CLASIFICACION'					,width: "50px"},
			IdCondicionDeLiquidacion			:{field: "IdCondicionDeLiquidacion"				,title: 'UI_CONDICION_LIQUIDACION'			,width: "30px"},
			
			
			
			FechaAsignacion					:{field: "FechaAsignacion",				title: $translate.instant('UI_FECHA_ASIGNACION'),	width: "100px"},
			FechaEmision					:{field: "FechaEmision",				title: $translate.instant('UI_FECHA_EMISION'),		width: "100px"},
			FechaIngresoRegistro			:{field: "FechaIngresoRegistro",		title: $translate.instant('UI_FECHA_INGRESO'),		width: "100px"},
			FechaLiquidacion				:{field: "FechaLiquidacion",			title: $translate.instant('UI_FECHA_LIQUIDACION'),	width: "100px"},
			FechaModificacionOperacion		:{field: "FechaModificacionOperacion",	title: $translate.instant('UI_FECHA_MODIFICACION'),	width: "100px"},
			FechaOferta						:{field: "FechaOferta",					title: $translate.instant('UI_FECHA_OFERTA'),		width: "100px"},
			FechaLogCambio					:{field: "FechaLogCambio",				title: $translate.instant('UI_FECHA'),				width: "100px"},
			FechaOperacionOrden				:{field: "FechaOperacionOrden",			title: $translate.instant('UI_FECHA_ORDEN'),		width: "100px"},
			FechaOperacionTransaccion		:{field: "FechaOperacionTransaccion",	title: $translate.instant('UI_FECHA_TRANSACCION'),	width: "100px"},
			FechaTransaccion				:{field: "FechaTransaccion",			title: $translate.instant('UI_FECHA_TRANSACCION'),	width: "100px"},
			FechaRecepcion					:{field: "FechaRecepcion",				title: $translate.instant('UI_FECHA_RECEPCION'),	width: "100px"},
			FechaVencimiento				:{field: "FechaVencimiento",			title: $translate.instant('UI_FECHA_VENCIMIENTO'),	width: "100px"},
			FechaVigencia		 			:{field: "FechaVigencia",				title: $translate.instant('UI_FECHA_VIGENCIA'),		width: "100px"},
			FechaAceptacion		 			:{field: "FechaAceptacion",				title: $translate.instant('UI_FECHA_ACEPTACION'),	width: "100px"},
			
			FolioOrden						:{field: "FolioOrden",					title: $translate.instant('UI_FOLIO_ORDEN'),		width: "70px"},
			FolioTransaccion				:{field: "FolioTransaccion",			title: $translate.instant('UI_FOLIO_TRANSACCION'),	width: "70px"},
			FolioCartera					:{field: "FolioCartera",				title: $translate.instant('UI_FOLIO_CARTERA'),		width: "70px"},
			FolioCarteraAsignada			:{field: "FolioCarteraAsignada",		title: $translate.instant('UI_FOLIO_CARTERA'),		width: "70px"},
			
			GlosaOrden						:{field: "GlosaOrden",					title: $translate.instant('UI_GLOSA'),	width: "150px"},
			NombreAutorizadorOrden   		:{field: "NombreAutorizadorOrden",		title: $translate.instant('UI_AUTORIZADO_ORDEN'),		width: "140px"},
			
			Lista							:{field: "ValorClasificacionRiesgo",	title: $translate.instant('UI_LISTA'),	width: "50px"},
			
			MarcaNegociacion				:{field: "MarcaNegociacion",					title: $translate.instant('UI_MARCA_NEGOCIACION'),			width: "100px"},

			Monto		   					:{field: "Monto",								title: 'UI_MONTO',											width: "75px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"},

			MontoAsignado					:{field: "MontoAsignado",						title: $translate.instant('UI_MONTO_ASIGNADO'),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoAsignado,'GEN','Monto',IdMonedaOperacion, IdTipoInstrumento, '')).valor #</label>"},
			MontoAsignacion					:{field: "MontoAsignacion",						title: $translate.instant('UI_MONTO_ASIGNADO'),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoAsignacion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},

			
			
			MontoACubrir					:{field: "MontoACubrir",						title: $translate.instant("UI_MONTO_A_CUBRIR"),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoACubrir,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoAsignadoOrden				:{field: "MontoAsignadoOrden",					title: $translate.instant('UI_MONTO_ASIGNADO'),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoAsignadoOrden,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoAsignadoTransaccion		:{field: "MontoAsignadoTransaccion",			title: $translate.instant('UI_MONTO_ASIGNADO'),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoAsignadoTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoAsignadoPlazo				:{field: "MontoAsignadoPlazo",					title: $translate.instant('UI_MONTO_ASIGNADO_PLAZO'),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoAsignadoPlazo,'GEN','Monto',IdMonedaOperacion, '','')).valor #</label>"},
			MontoCoberturaCorredoraCustodia	:{field: "MontoCoberturaCorredora",				title: $translate.instant("UI_MONTO_COBERTURA_CORR_ABR"),	width: "100px", template : " <label> #:  (util.bcs_formato(MontoCoberturaCorredora,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoCoberturaCorredoraCtaInv	:{field: "MontoCoberturaCorredora",				title: $translate.instant("UI_MONTO_COBERTURA_CORR_ABR"),	width: "100px", template : " <label> #:  (util.bcs_formato(MontoCoberturaCorredora,'GEN','Monto',IdMoneda, '','')).valor #</label>"},
			MontoCoberturaEntidadCustodia	:{field: "MontoCoberturaEntidad",				title: $translate.instant("UI_MONTO_COBERTURA_ENT_ABR"),	width: "100px", template : " <label> #:  (util.bcs_formato(MontoCoberturaEntidad,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"}, 	
			MontoCoberturaEntidadCtaInv		:{field: "MontoCoberturaEntidad",				title: $translate.instant("UI_MONTO_COBERTURA_ENT_ABR"),	width: "100px", template : " <label> #:  (util.bcs_formato(MontoCoberturaEntidad,'GEN','Monto',IdMoneda, '','')).valor #</label>"},
			MontoCobroComprobante			:{field: "MontoCobroComprobante",				title: $translate.instant("UI_MONTO_COBRO"),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoCobroComprobante,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"},
			MontoComprobante				:{field: "MontoComprobante",					title: $translate.instant("UI_MONTO_COMPROBANTE"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoComprobante,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"},
			MontoDisponibleGarantiaCustodia	:{field: "MontoDisponibleGarantiaCustodia",		title: $translate.instant("UI_MONTO_DISPONIBLE"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoDisponibleGarantiaCustodia,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoDisponibleOrden			:{field: "MontoDisponibleOrden",				title: $translate.instant("UI_MONTO_DISPONIBLE_ABR"),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoDisponibleOrden,'GEN','Monto',IdMoneda, '','')).valor #</label>"},
			MontoDisponibleSubyacente		:{field: "MontoDisponibleSubyacente",			title: $translate.instant("UI_MONTO_DISPONIBLE_ABR"),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoDisponibleSubyacente,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoDisponibleTransaccion		:{field: "MontoDisponibleTransaccion",			title: $translate.instant("UI_MONTO_DISPONIBLE_ABR"),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoDisponibleTransaccion,'GEN','Monto',IdMonedaInstrumento, IdTipoInstrumento,'')).valor #</label>"},
			MontoExigibilidadCorr			:{field: "MontoExigibilidadCorr",				title: $translate.instant("UI_MONTO_EXIG_CORR_ABR"),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoExigibilidadCorr,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},	
			MontoExigibilidadEnt			:{field: "MontoExigibilidadEnt",				title: $translate.instant("UI_MONTO_EXIG_ENT_ABR"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoExigibilidadEnt,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},	
			MontoOrden						:{field: "MontoOrden",							title: $translate.instant('UI_MONTO_ORDEN'),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoOrden,'GEN','Monto',IdMonedaOrden, '','')).valor #</label>"},
			MontoLiquidacion				:{field: "MontoLiquidacion",					title: $translate.instant('UI_MONTO_LIQUIDACION'),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'GEN','Monto',IdMonedaLiquidacionOrden, '','')).valor #</label>"},
			MontoOrdenPlazo					:{field: "MontoOrdenPlazo",						title: $translate.instant('UI_MONTO_ORDEN_PLZ'),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoOrdenPlazo,'GEN','Monto',IdMonedaOrden, '','')).valor #</label>"},
			MontoPlazo						:{field: "MontoPlazo",							title: $translate.instant("UI_MONTO_PLAZO"),				width: "100px", template : " <label> #:  (util.bcs_formato(MontoPlazo,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoSimulacionSimultanea		:{field: "MontoSimulacionSimultanea",			title: $translate.instant("UI_MONTO_SIMULACION"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoSimulacionSimultanea,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoSimulacionTransaccion		:{field: "MontoSimulacionTransaccion",			title: $translate.instant("UI_MONTO_SIMULACION"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoSimulacionTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			MontoSimulacionGarantiaCustodia	:{field: "MontoSimulacionGarantiaCustodia",		title: $translate.instant("UI_MONTO_SIMULACION"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoSimulacionGarantiaCustodia,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},	
			MontoCoberturaSimular			:{field: "MontoCoberturaSimular",				title: $translate.instant("UI_MONTO_COBERTURA_SIMULACION"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoCoberturaSimular,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"}, //GUZ
			MontoSimulacionGarantiaCtaInv	:{field: "MontoSimulacionGarantiaCtaInv",		title: $translate.instant("UI_MONTO_SIMULACION"),			width: "100px", template : " <label> #:  (util.bcs_formato(MontoSimulacionGarantiaCtaInv,'GEN','Monto',IdMoneda, '','')).valor #</label>",attributes: { style : "text-align:right;background-color: lemonchiffon" }},	
			MontoTransaccion				:{field: "MontoTransaccion",					title: $translate.instant('UI_MONTO'),						width: "100px", template : " <label> #:  (util.bcs_formato(MontoTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			Motivo							:{field: "Motivo",								title: $translate.instant('UI_MOTIVO'),						width: "100px"},
			
			Nemotecnico						:{field: "Nemotecnico",													title: "UI_NEMOTECNICO",				width: "150px"},
			NemoInstrumento					:{field: "NemoInstrumento",												title: "UI_INSTRUMENTO_ABR",			width: "150px"},
			NemoSVS							:{field: "NemoSVS",														title: "UI_NEMOTECNICO_SVS",			width: "75px"},
			NemotecnicoSVS					:{field: "NemotecnicoSVS",												title: "UI_NEMOTECNICO_SVS",			width: "75px"},
			NombreBoveda					:{field: "NombreBoveda",												title: 'UI_BOVEDA',						width: "100px"},
			NombreCanal						:{field: "NombreCanal",													title: 'UI_CANAL_INGRESO',				width: "100px"},
			NombreCanalDistribucion			:{field: "NombreCanalDistribucion",										title: 'UI_CANAL_DISTRIBUCION',			width: "100px"},
			NombreClasificacionTipoInstrumento			:{field: "NombreClasificacionTipoInstrumento",				title: 'UI_CLASIFICACION',				width: "150px"},
			NombreClasificacionTipoInstrumentoSubyacente:{field: "NombreClasificacionTipoInstrumentoSubyacente",	title: 'UI_CLASIFICACION_SUBYACENTE',	width: "150px"},
			NombreClaseOrden				:{field: "NombreClaseOrden",											title: 'UI_MECANISMO_CAPTACION',		width: "100px"},
			NombreCondicionDeLiquidacion	:{field: "NombreCondicionDeLiquidacion",								title: 'UI_CONDICION_LIQUIDACION_ABR',	width: "100px"},
			NombreCompleto					:{field: "NombreCompleto",												title: 'UI_CLIENTE',					width: "200px"},
			NombreCorredorContraparte		:{field: "NombreCorredorContraparte",									title: 'UI_CORREDOR_CONTRAPARTE',		width: "220px"},
			NombreLargoCuentaCustodio		:{field: "NombreLargoCuentaCustodio",									title: 'UI_CUENTA_CUSTODIO',			width: "220px"},
			NombreEstadoComprobante			:{field: "NombreEstadoComprobante",										title: 'UI_ESTADO_COMPROBANTE',			width: "150px"},
			NombreEstadoFacturacion			:{field: "NombreEstadoFacturacion",										title: 'UI_ESTADO_FACTURACION',			width: "150px"},
			NombreEmpleadoDigitador			:{field: "NombreEmpleadoDigitador",										title: 'UI_EJECUTIVO_DIGITADOR',		width: "175px"},
			NombreEmpleadoPortafolio		:{field: "NombreEmpleadoPortafolio",									title: 'UI_EJECUTIVO_PORTAFOLIO',		width: "175px"},
			NombreFormaCustodia				:{field: "NombreFormaCustodia",											title: 'UI_FORMA_CUSTODIA',				width: "75px"},
			NombreFormaNegociacion			:{field: "NombreSistemaBursatil",										title: 'UI_FORMA_NEGOCIACION',			width: "150px"},
			NombreLibroOrdenes				:{field: "NombreLibroOrdenes",											title: 'UI_LIBRO_ORDENES',				width: "125px"},
//			NombreSistemaBursatil			:{field: "NombreSistemaBursatil",										title: 'UI_SISTEMA_BURSATIL',			width: "150px"},
			NombreMedioDePago				:{field: "NombreMedioDePago",											title: 'UI_MEDIO_DE_PAGO',				width: "150px"},
			
			NombreSistemaBursatil			:{field: "NombreSistemaBursatil",										title: 'UI_FORMA_NEGOCIACION',			width: "150px"},			
			NombreSucursalEmpleadoPortafolio:{field: "NombreSucursalEmpleadoPortafolio",							title: 'UI_SUCURSAL_EJECUTIVO_PORTAFOLIO',		width: "175px"},
			NombreSucursalEmpleadoDigitador	:{field: "NombreSucursalEmpleadoDigitador",								title: 'UI_SUCURSAL_EJECUTIVO_DIGITADOR',		width: "175px"},
			NombreTipoInstrumento			:{field: "NombreTipoInstrumento", 										title: 'UI_TIPO_INSTRUMENTO',			width: "215px"},
			NombreTipoInstrumentoSubyacente	:{field: "NombreTipoInstrumentoSubyacente", 							title: 'UI_TIPO_INSTRUMENTO_SUBYACENTE',width: "215px"},
			NombreTipoOperacion				:{field: "NombreTipoOperacion",											title: 'UI_TIPO_OPERACION',				width: "200px"},
			NombreTipoVigenciaOrden			:{field: "NombreTipoVigenciaOrden",										title: 'UI_TIPO_VIGENCIA',				width: "100px"},
			NombreTipoCanalIngreso			:{field: "NombreTipoCanalIngreso",										title: 'UI_CANAL_INGRESO',				width: "100px"},
						
			OrdenVigenteUI					:{field: "OrdenVigenteUI",								title: "UI_ORDEN_VIGENTE",	width: "50px"},
			OrdenEspecialUI					:{field: "OrdenEspecialUI",								title: "UI_ORDEN_ESPECIAL",	width: "50px"},
			Observaciones					:{field: "Observaciones",								title: 'UI_OBSERVACIONES',	width: "100px"},
			
			PagoCobroCuentaInversion		:{field: "PagoCobroCuentaInversion",	title: "UI_PAGO_COBRO_CON_CTA_INVERSION",	width: "100px"},
			Plazo							:{field: "Plazo",						title: "UI_PLAZO",							width: "50px"},
			PlazoVentaCorta					:{field: "PlazoVentaCorta",				title: "UI_PLAZO",							width: "50px"},
			PlazoReporte					:{field: "PlazoReporte",				title: "UI_PLAZO",							width: "50px"},
			Porcentaje						:{field: "Porcentaje",					title: "UI_PORCENTAJE",						width: "75px",template : " <label> #:  (util.bcs_formato(Porcentaje,'GEN','Tasa',IdMoneda,'','')).valor #</label>"},
			PorcentajeCoberturaCorredora	:{field: "PorcentajeCoberturaCorredora",title: "UI_PORC_COBERTURA_CORR_ABR",		width: "100px"},
			PorcentajeCoberturaEntidad		:{field: "PorcentajeCoberturaEntidad",	title: "UI_PORC_COBERTURA_ENT_ABR",			width: "100px"},
			PorcentajeSimulacion			:{field: "PorcentajeSimulacion",		title: "UI_PORCENTAJE",						width: "100px"},
			PorcExigibilidadCorr			:{field: "PorcExigibilidadCorr",		title: "UI_PORC_EXIG_CORR_ABR",				width: "100px"},
			PorcExigibilidadEnt				:{field: "PorcExigibilidadEnt",			title: "UI_PORC_EXIG_ENT_ABR",				 width: "100px"},		
			PorcentajeVisibilidad			:{field: "PorcentajeVisibilidad",		title: "UI_VISIBILIDAD",					width: "100px"},		

			
			PrecioAsignacion				:{field: "PrecioAsignacion",		title: 'UI_PRECIO',						width: "75px",	template : " <label> #:  (util.bcs_formato(PrecioAsignacion,'GEN','Precio',IdMonedaOperacion,IdTipoInstrumento,'')).valor #</label>"},
			PrecioCustodiaSimultanea		:{field: "Precio",					title: 'UI_PRECIO',						width: "75px",	template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},

			PrecioGarantiaCustodia			:{field: "PrecioGarantiaCustodia",	title: 'UI_PRECIO_MERCADO',				width: "95px",	template : " <label> #:  (util.bcs_formato(PrecioGarantiaCustodia,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},

			PrecioVaR						:{field: "PrecioVaR",				title: 'UI_PRECIO_VAR',					width: "75px",	template : " <label> #:  (util.bcs_formato(PrecioVaR,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			PorcentajeVaR					:{field: "PorcentajeVaR",			title: 'UI_PORCENTAJE_VAR',				width: "100px", template : " <label> #:  (util.bcs_formato(PorcentajeVaR,'GEN','Tasa',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},			

			PrecioPromedio					:{field: "PrecioPromedio",			title: 'UI_PRECIO_PROMEDIO_CLIENTE',					template : " <label> #:  (util.bcs_formato(PrecioPromedio,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			PrecioTransaccion				:{field: "PrecioTransaccion",		title: 'UI_PRECIO',						width: "75px",	template : " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			PrecioOrden 					:{field: "PrecioOrden",				title: 'UI_PRECIO',						width: "75px",	template : " <label> #:  (util.bcs_formato(PrecioOrden,'GEN','Precio',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
			PrecioStrike 					:{field: "PrecioStrike",			title: 'UI_PRECIO_STRIKE',				width: "75px",	template : " <label> #:  (util.bcs_formato(PrecioStrike,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			PrecioPromedioAsignacion		:{field: "PrecioPromedioAsignacion",title: 'UI_PRECIO_PROMEDIO_ASIGNACION',	width: "100px",	template : " <label> #:  (util.bcs_formato(PrecioPromedioAsignacion,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			PropositoPortafolio				:{field: "Proposito", 				title: 'UI_PROPOSITO',					width: "100px"  },
			
			RazonSocial		 				:{field: "RazonSocial",				title: $translate.instant('UI_RAZON_SOCIAL'),	width: "200px"},
			ReportanteOReportado			:{field: "ReportanteOReportado",	title: $translate.instant('UI_TIPO'),			width: "100px"},
			Reservado						:{field: "Reservado",				title: $translate.instant('UI_RESERVADO'),		width: "150px"},

			Saldo							:{field: "Saldo",					title: $translate.instant("UI_SALDO_DISPONIBLE_ABR"),	width: "100px",	template : " <label> #:  (util.bcs_formato(Saldo,'GEN','Monto',IdMoneda,'','')).valor #</label>",attributes : {style : "text-align:right;"}},
			SaldoRep						:{field: "SaldoRep",				title: $translate.instant("UI_SALDO_DISPONIBLE_ABR"),	width: "100px",	template : " <label> #:(util.bcs_formato(SaldoRep,'GEN','Monto',IdMonedaOrden,'','')).valor #</label>",attributes : {style : "text-align:right;"}},
			
			Tasa							:{field: "Tasa",					title: $translate.instant('UI_TASA'),					width: "85px", template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>"},
			TasaPromedioReporte				:{field: "TasaPromedioReporte",		title: $translate.instant('UI_TASA_PROMEDIO_REPORTE'),	width: "45px", template : " <label> #:  (util.bcs_formato(TasaPromedioReporte,'GEN','Tasa',IdMonedaOrden,'','')).valor #</label>"},
			TasaVentaCorta					:{field: "TasaVentaCorta",	title: $translate.instant('UI_TASA'),					width: "45px"},
			TasaTransaccion					:{field: "TasaTransaccion",	title: $translate.instant('UI_TASA'),					width: "45px", template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',IdMoneda,'','')).valor #</label>"},
			TasaCompra						:{field: "TasaCompra",		title: $translate.instant('UI_TASA_COMPRA'),			width: "45px", template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',IdMoneda,'','')).valor #</label>"},
			TasaMercado						:{field: "TasaMercado",		title: $translate.instant('UI_TASA_MERCADO'),			width: "45px", template : " <label> #:  (util.bcs_formato(TasaMercado,'GEN','Tasa',IdMoneda,'','')).valor #</label>"},
			TipoInstrumento					:{field: "TipoInstrumento",	title: $translate.instant("UI_TIPO_INSTRUMENTO_ABR"),	width: "75px"},
			TipoPrecio						:{field: "TipoPrecio",		title: $translate.instant('UI_TIPO_PRECIO'),			width: "75px"},
			TipoLogCambio					:{field: "TipoLogCambio",	title: $translate.instant('UI_MODIFICACION'),			width: "75px"},
			TipoCambio						:{field: "TipoCambio",      title: $translate.instant('UI_TIPO_CAMBIO'),			width: "75px"},
			
			ValorClasificacionRiesgo			:{field: "ValorClasificacionRiesgo",			title: $translate.instant('UI_CLASIFICACION_RIESGO'),		width: "100px"},
			ValorRescateTransaccion				:{field: "ValorRescateTransaccion",				title: $translate.instant("UI_VALOR_RESCATE_TRANSACCION"),	width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			ValorRescateAsignado				:{field: "ValorRescateAsignado",				title: $translate.instant("UI_VALOR_RESCATE_ASIGNADO"),		width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateAsignado,'GEN','Monto',IdMonedaOperacion, '','')).valor #</label>"},
			ValorRescateAsignadoTransaccion		:{field: "ValorRescateAsignadoTransaccion",		title: $translate.instant("UI_VALOR_RESCATE_ASIGNADO"),		width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateAsignadoTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			ValorRescateAsignadoSimulacion		:{field: "ValorRescateAsignadoSimulacion",		title: $translate.instant("UI_VALOR_RESCATE_ASIGNADO"),		width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateAsignadoSimulacion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			ValorRescateSimulacionCliente		:{field: "ValorRescateSimulacionCliente",		title: $translate.instant("UI_VALOR_RESCATE_ASIGNADO"),		width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateSimulacionCliente,'GEN','Monto',IdMoneda, '','')).valor #</label>"},
			ValorRescateDisponibleTransaccion	:{field: "ValorRescateDisponibleTransaccion",	title: $translate.instant("UI_SALDO_VALOR_RESCATE"),		width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateDisponibleTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			ValorRescateSimulacionTransaccion	:{field: "ValorRescateSimulacionTransaccion",	title: $translate.instant("UI_VALOR_RESCATE"),				width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescateSimulacionTransaccion,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			
			
			MercadoSMV						:{field: "MercadoSMV",					title: $translate.instant('UI_MERCADO_SMV'),					width: "75px"},
			ModalidadSMV					:{field: "ModalidadSMV",				title: $translate.instant('UI_MODALIDAD_SMV'),					width: "75px"},
			EsClienteFinal					:{field: "EsClienteFinal",				title: $translate.instant('UI_CLIENTE_FINAL'),					width: "75px"},
			IdPersonaBroker					:{field: "IdPersonaBroker",				title: $translate.instant('UI_ID_PERSONA_BROKER'),				width: "75px"},
			CodigoBrokerExtranjero			:{field: "CodigoBrokerExtranjero",		title: $translate.instant('UI_CODIGO_BROKER'),					width: "75px"},
			IdCuentaCustodioPortafolio		:{field: "IdCuentaCustodioPortafolio",	title: $translate.instant('UI_ID_CUENTA_CUSTODIO_PORTAFOLIO'),	width: "75px"},
			CuentaCustodioPortafolio		:{field: "CuentaCustodioPortafolio",	title: $translate.instant('UI_ID_CUENTA_CUSTODIO_PORTAFOLIO'),	width: "200px"},
			IndicadorBancoCustodio			:{field: "IndicadorBancoCustodio",		title: $translate.instant('UI_LIQUIDA_BANCO_CUSTODIO'),			width: "75px"},
			CodigoBancoCustodio				:{field: "CodigoBancoCustodio",			title: $translate.instant('UI_CODIGO_BANCO_CUSTODIO'),			width: "75px"},
			CodigoBancoLiquidador			:{field: "CodigoBancoLiquidador",		title: $translate.instant('UI_CODIGO_BANCO_LIQUIDADOR'),		width: "75px"},
			IdMedioDePago					:{field: "IdMedioDePago",				title: $translate.instant('UI_MEDIO_PAGO'),						width: "75px"},
			EsPutEsCall						:{field: "EsPutEsCall",					title: $translate.instant('UI_ES_PUT_ES_CALL'),					width: "75px"},
			
			ModalidadLiquidacion		:{field: "ModalidadLiquidacion",	title: $translate.instant('UI_MODALIDAD_LIQUIDACION_OPCIONES'),	width: "75px"},
			ModalidadEjercicio			:{field: "ModalidadEjercicio",		title: $translate.instant('UI_MODALIDAD_EJERCICIO_OPCIONES'),	width: "75px"},
			TipoOpcion					:{field: "TipoOpcion",				title: $translate.instant('UI_TIPO_OPCIONES'),					width: "75px"},
			EstiloOpcion				:{field: "EstiloOpcion",			title: $translate.instant('UI_ESTILO_OPCIONES'),				width: "75px"},
			Tamano						:{field: "Tamano",					title: $translate.instant('UI_LOTE_PADRON'),			width: "75px"},
			
			
			FolioGarantia			:{field: "FolioGarantia",					title: $translate.instant('UI_FOLIO'),			width: "75px"},
			FechaIngresoRegistro 	:{field: "FechaIngresoRegistro",			title: $translate.instant('UI_FECHA_INGRESO'),			width: "100px"},
			NombreEstado			:{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),			width: "100px"},
			NombreConcepto			:{field: "NombreConcepto",					title: $translate.instant('UI_CONCEPTO'),			width: "100px"},
			Nemotecnico				:{field: "Nemotecnico",					title: $translate.instant('UI_INSTRUMENTO'),			width: "200px"},
			CantidadGarantia		:{field: "Cantidad",					title: $translate.instant('UI_CANTIDAD'),			width: "100px", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
			MontoValorizado			:{field: "MontoValorizado",					title: $translate.instant('UI_MONTO'),			width: "200px", template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"},
			IdSistemaBursatil			:{field: "IdSistemaBursatil",		title: $translate.instant('UI_SISTEMA_BURSATIL'),				width: "75px"},
			IdTipoLibroOrden			:{field: "IdTipoLibroOrden",		title: $translate.instant('UI_TIPO_LIBRO_ORDEN'),				width: "75px"},
			Producto				:{field: "Producto",					title: $translate.instant('UI_PRODUCTO'),			width: "100px"},
			Interes					:{field: "Interes",					 		title: $translate.instant('UI_INTERES'),			width: "100px", template : "<label> # if(IdTipoInstrumento==='SIM'){# #: MontoOrdenPlazo-MontoOrden # #}else{# #: 0 # #} #</label>"},
			EnAnulacion				:{field: "EnAnulacion", title: $translate.instant("UI_EN_ANULACION"), width : "100px"},
			RazonSocialPortafolio	:{field: "RazonSocialPortafolio", title: $translate.instant("UI_RAZON_SOCIAL_PORTAFOLIO"), width : "175px"},
			TipoRegistroUI	:{field: "TipoRegistroUI", title: $translate.instant("UI_TIPO_REGISTRO"), width : "175px"},
			IdEstadoRegistroUI	:{field: "IdEstadoRegistroUI", title: $translate.instant("UI_ESTADO_PROCESO"), width : "175px"},
			FechaIngresoRegistroHistorico	:{field: "FechaIngresoRegistroHistorico", title: $translate.instant("UI_FECHA_INGRESO_REGISTRO"), width : "175px"},
			FechaEjecucionRegistro	:{field: "FechaEjecucionRegistro", title: $translate.instant("UI_FECHA_EJECUCION_REGISTRO"), width : "175px"},
			MontoCobro			:{field: "MontoCobro",				title: $translate.instant("UI_MONTO_COBRO"),		width: "100px", template : " <label> #:  (util.bcs_formato(MontoCobro,'GEN','Monto',IdMonedaInstrumento, '','')).valor #</label>"},
			IdComision			:{field: "IdComision", title: $translate.instant("UI_TIPO_COMISION"), width : "120px"}, 
			ValorComision		:{field: "ValorComision", title: $translate.instant("UI_VALOR_COMISION"), width : "120px"},
			MontoComision		:{field: "MontoComision", title: $translate.instant("UI_MONTO_COMISION"), width : "120px", template :" #:  (util.bcs_formato(MontoComision,'GEN','Monto',IdMonedaLiquidacionOrden, '','')).valor #"},
			FormaComisionGrilla	:{field: "FormaComisionGrilla", title: $translate.instant("UI_COMISION"), width : "75px"},
			TipoLiquidacion	:{field: "TipoLiquidacion", title: $translate.instant("UI_TIPO_LIQUIDACION_COMISION_FACTURA"), width : "90px"},
			ModoFacturacion	:{field: "ModoFacturacion", title: $translate.instant("UI_MODO_FACTURACION"), width : "100px"},
			

			CodigoUsuarioReserva	:{field: "CodigoUsuarioReserva",		title: $translate.instant('UI_USUARIO_RESERVA'),		width: "75px"},
			IdGrupoReserva			:{field: "IdGrupoReserva",				title: $translate.instant('UI_GRUPO_RESERVA'),			width: "320px"},
			
			NemotecnicoInstrumentoComprobante  : {field: "NemotecnicoInstrumentoComprobante",title: "UI_NEMOTECNICO", width: "200px",template:kendo.template("#if(SeparaPorInstrumento=='S'){# #: NemotecnicoInstrumentoComprobante # #}else{# #: ' ' # #}#")},
			CantidadTotal 			: {field: "CantidadTotal",title: "UI_CANTIDAD", width: "200px",template:kendo.template("#if(SeparaPorInstrumento=='S'&& NemotecnicoInstrumentoComprobante !=''){# #: CantidadTotal # #}else{# #: ' ' # #}#")},
			PPP						: {field:"PPP",title: "UI_PRECIO_PROMEDIO_PONDERADO", width: "200px",template:kendo.template("#if(SeparaPorInstrumento=='S' && NemotecnicoInstrumentoComprobante !=''){# #:  (util.bcs_formato(PPP,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor # #}else{# #: ' ' # #}#")},
			PrecioRaiz	:{field: "PrecioRaiz",	title: $translate.instant('UI_PRECIO_RAIZ'),	width: "95px",	template:kendo.template("#if(!MonedaRaiz==''){# #:   (util.bcs_formato(PrecioRaiz,'GEN','Monto',MonedaRaiz,'','')).valor # #}else{# #: ' ' # #}#")},
			MontoRaiz	:{field: "MontoRaiz",	title: $translate.instant("UI_MONTO_RAIZ"),		width: "100px", template:kendo.template("#if(!MonedaRaiz==''){# #:   (util.bcs_formato(MontoRaiz,'GEN','Monto',MonedaRaiz,'','')).valor # #}else{# #: ' ' # #}#")},			
			MonedaRaiz	:{field: "MonedaRaiz",	title: $translate.instant('UI_MONEDA_RAIZ'),	width: "75px"},
			MotivoReserva :{field: "MotivoReserva",	title: $translate.instant('UI_MOTIVO_RESERVA'),	width: "150px"},
			FolioSpot   :{field: "FolioSpot",		title: $translate.instant('UI_FOLIO_SPOT'),		width: "75px"},
			EstadoSpot   			:{field: "EstadoSpot",		title: $translate.instant('UI_ESTADO_SPOT'),		width: "75px"},
			InstrumentoNegociacion	:{field: "InstrumentoNegociacion",		title: $translate.instant('UI_INSTRUMENTO_NEGOCIACION'),	width: "100px"},			
			IdAsignacionMasiva :{field: "IdAsignacionMasiva", title: $translate.instant('UI_ID_ASIGNACION_MASIVA'), width: "125px"},
			NombreTipoAsignacion :{field: "NombreTipoAsignacion", title: $translate.instant('UI_TIPO_ASIGNACION'), width: "150px", hidden: true},
			MontoCLP	:{field: "MontoCLP", title: $translate.instant('UI_MONTO_CLP'), width: "75px", hidden: true, template :" #:  (util.bcs_formato(MontoCLP,'GEN','Monto','CLP', '','')).valor #"},
			AplicaComisionAsignacionMasiva	:{field: "AplicaComisionAsignacionMasiva", title: $translate.instant('UI_COBRO_COMISIONES'), width: "100px", hidden: true},
			MarcaAsignacion : {field: "MarcaAsignacion",title: $translate.instant('UI_MARCA_ASIGNACION'), width: "100px",}
		}
	};
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.error("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};

	obj.getConfiguracionColumna = getConfiguracionColumna;
	
	obj.getSchemaTransaccion = function(){
		return {
			model: {
				fields : {
					AccionOrden								: { type: "string"},
					CantidadDisponibleTransaccion			: { type: "number"},
					CantidadAsignadaTransaccion				: { type: "number"},
					CantidadTransaccion						: { type: "number"},
					CantidadTransaccionPorAsignar			: { type: "number"},
					CantidadSimulacionTransaccion			: { type: "number"},
					CodigoCorredorComprador					: { type: "string"},
					CodigoCorredorVendedor					: { type: "string"},
					CodigoClasificacionTipoInstrumento		: { type: "string"},
					CodigoCondicionDeLiquidacion			: { type: "string"},
					CodigoEmpleadoReserva					: { type: "string"},
					CodigoOperadorCorredor					: { type: "string"},
					CodigoOperadorContraparte				: { type: "string"},
					CodigoOperadorComprador					: { type: "string"},
					CodigoOperadorVendedor					: { type: "string"},
					CorteMinimo								: { type: "number"},
					EsCuentaPropia				: { type: "string"},
					EsAPlazo					: { type: "string"},
					EsAPlazoUI					: { type: "string"},
					EsFungible					: { type: "string"},
					EsFungibleUI				: { type: "string"},
					EsBilateral				    : { type: "string"},
					EsBilateralUI				: { type: "string"},
					EsVentaCorta				: { type: "string"},
					EsVentaCortaUI				: { type: "string"},
					EstadoTransaccion			: { type: "string"}, //
					FolioTransaccion 				: { type: "string"}, //
					FechaAceptacion					: { type: "sysdatetime"},
					FechaSistemaCreacion			: { type: "sysdate"}, //
					FechaOperacionTransaccion		: { type: "datetime"}, //
					FechaLiquidacion				: { type: "sysdate"}, //
					FechaLogCambio					: { type: "sysdatetime"}, //
					FechaModificacionOperacion		: { type: "sysdatetime"}, //
					FechaEmision					: { type: "sysdate"}, //
					FechaTransaccion				: { type: "sysdate"},
					FechaVencimiento				: { type: "sysdate"},
					HoraSistemaCreacion				: { type: "date"}, //
					IdBolsa							: { type: "string"}, //
					IdBoveda						: { type: "string"}, //
					IdModeloCustodia				: { type: "string"}, //
					IdCondicionDeLiquidacion		: { type: "string"},
					IdClasificacionTipoInstrumento	: { type: "string"},
					IdCorredorContraparte			: { type: "number"},
					IdEmisor 						: { type: "string"},
					IdEmpleadoReserva				: { type: "number"}, //
					IdInstrumento					: { type: "number"}, 
					IdInstrumentoSubyacente			: { type: "number"}, 
					IdInstrumentoNegociacion		: { type: "number"},
					IdLibroOrdenes					: { type: "number"}, 
					IdMovimiento					: { type: "number"}, 
					IdMonedaInstrumento 			: { type: "string"},
					IdMonedaTransaccion 			: { type: "string"},
					IdMonedaEmision					: { type: "string"},
					IdTipoInstrumento				: { type: "string"}, //
					IdTipoOperacion					: { type: "string"}, //
					IdTipoProducto					: { type: "string"}, //
					IdTipoReajuste					: { type: "string"}, //
					IdTransaccion					: { type: "number"}, //
					IdUsuarioReserva				: { type: "number"}, //
					IndicadorOrdenDirecta			: { type: "string"},
					Instrumento						: { type: "string"}, //
					InstrumentoUI					: { type: "string"}, //
					InstrumentoSubyacente			: { type: "string"},
					InstrumentoNegociacionTran  : { type: "string"},
					MarcaNegociacion				: { type: "string"}, //
					MontoTransaccion				: { type: "number",excelFormat : "#,##0.00"}, //
					MontoSimulacionTransaccion 		: { type: "number"}, //
					MontoDisponibleTransaccion		: { type: "number"}, //
					MontoAsignadoTransaccion		: { type: "number"}, //
					Motivo							: { type: "string"}, //
					Nemotecnico						: { type: "string"}, //
					NemotecnicoSVS					: { type: "string"}, //
					NombreTipoOperacion				: { type: "string"}, //
					NombreTipoInstrumento			: { type: "string"}, //
					NombreTipoInstrumentoSubyacente	: { type: "string"},
					NombreClasificacionTipoInstrumento				: { type: "string"}, //
					NombreClasificacionTipoInstrumentoSubyacente	: { type: "string"},
					NombreLibroOrdenes				: { type: "string"}, //
					NombreCondicionDeLiquidacion	: { type: "string"}, //
					NombreCorredorContraparte		: { type: "string"}, //
					NombreSistemaBursatil			: { type: "string"}, //
					Observaciones					: { type: "string"},
					Plazo							: { type: "number"},
					PrecioTransaccion				: { type: "number"},
					PrecioStrike					: { type: "number"},
					Reservado						: {type : "string"},
					Tasa							: { type: "number"}, //
					TasaTransaccion					: { type: "number"}, //
					TipoLogCambio					: { type: "string"}, //
					ValorRescateDisponibleTransaccion	: { type: "number"},
					ValorRescateTransaccion				: { type: "number"},
					ValorRescateSimulacionTransaccion 	: { type: "number"},
					ModalidadLiquidacion	: { type: "string"},
					ModalidadEjercicio		: { type: "string"},
					TipoOpcion				: { type: "string"},
					EstiloOpcion			: { type: "string"},
					Tamano					: { type: "number"},
					EnAnulacion				: {type: "string"},
					CodigoUsuarioReserva	: { type: "string"},
					IdGrupoReserva			: { type: "string"},
					IdComprobante			: { type: "number"},
					PrecioRaiz			: { type: "number"},
					MontoRaiz			: { type: "number"},	
					MonedaRaiz			: { type: "string"},	
					MotivoReserva 			: { type: "string"}
					
				}
			}
		};
	};
	
	obj.getSchemaTransaccionHistorico = function(){
		return {
			model: {
				fields : {
					
					IdTransaccionHistorico					: { type: "string"},
					TipoRegistro							: { type: "string"},
					TipoRegistroUI							: { type: "string"},
					IdEstadoRegistro						: { type: "string"},
					IdEstadoRegistroUI						: { type: "string"},
					FechaIngresoRegistroHistorico			: { type: "sysdatetime"},
					FechaEjecucionRegistro					: { type: "sysdatetime"},
					AccionOrden								: { type: "string"},
					CantidadDisponibleTransaccion			: { type: "number"},
					CantidadAsignadaTransaccion				: { type: "number"},
					CantidadTransaccion						: { type: "number"},
					CantidadSimulacionTransaccion			: { type: "number"},
					CodigoCorredorComprador					: { type: "string"},
					CodigoCorredorVendedor					: { type: "string"},
					CodigoClasificacionTipoInstrumento		: { type: "string"},
					CodigoCondicionDeLiquidacion			: { type: "string"},
					CodigoEmpleadoReserva					: { type: "string"},
					CodigoOperadorCorredor					: { type: "string"},
					CodigoOperadorContraparte				: { type: "string"},
					CodigoOperadorComprador					: { type: "string"},
					CodigoOperadorVendedor					: { type: "string"},
					CorteMinimo								: { type: "number"},
					
					EsCuentaPropia				: { type: "string"},
					EsAPlazo					: { type: "string"},
					EsAPlazoUI					: { type: "string"},
					EsFungible					: { type: "string"},
					EsFungibleUI				: { type: "string"},
					EsBilateral				    : { type: "string"},
					EsBilateralUI				: { type: "string"},
					EsVentaCorta				: { type: "string"},
					EsVentaCortaUI				: { type: "string"},
					EstadoTransaccion			: { type: "string"}, //
					
					FolioTransaccion 				: { type: "string"}, //
					FechaAceptacion					: { type: "sysdatetime"},
					FechaSistemaCreacion			: { type: "sysdate"}, //
					FechaOperacionTransaccion		: { type: "datetime"}, //
					FechaLiquidacion				: { type: "sysdate"}, //
					FechaLogCambio					: { type: "sysdatetime"}, //
					FechaModificacionOperacion		: { type: "sysdatetime"}, //
					FechaEmision					: { type: "sysdate"}, //
					FechaTransaccion				: { type: "sysdate"},
					FechaVencimiento				: { type: "sysdate"},
					
					HoraSistemaCreacion				: { type: "date"}, //
					
					IdBolsa							: { type: "string"}, //
					IdBoveda						: { type: "string"}, //
					IdModeloCustodia				: { type: "string"}, //
					IdCondicionDeLiquidacion		: { type: "string"},
					IdClasificacionTipoInstrumento	: { type: "string"},
					IdCorredorContraparte			: { type: "number"},
					IdEmisor 						: { type: "string"},
					IdEmpleadoReserva				: { type: "number"}, //
					IdInstrumento					: { type: "number"}, 
					IdLibroOrdenes					: { type: "number"}, 
					IdMovimiento					: { type: "number"}, 
					IdMonedaInstrumento 			: { type: "string"},
					IdMonedaTransaccion 			: { type: "string"},
					IdMonedaEmision					: { type: "string"},
					IdTipoInstrumento				: { type: "string"}, //
					IdTipoOperacion					: { type: "string"}, //
					IdTipoProducto					: { type: "string"}, //
					IdTipoReajuste					: { type: "string"}, //
					IdTransaccion					: { type: "number"}, //
					IdUsuarioReserva				: { type: "number"}, //
					IndicadorOrdenDirecta			: { type: "string"},
					Instrumento						: { type: "string"}, //
					InstrumentoUI					: { type: "string"}, //
					InstrumentoSubyacente			: { type: "string"},
					
					
					MarcaNegociacion				: { type: "string"}, //
					MontoTransaccion				: { type: "number",excelFormat : "#,##0.00"}, //
					MontoSimulacionTransaccion 		: { type: "number"}, //
					MontoDisponibleTransaccion		: { type: "number"}, //
					MontoAsignadoTransaccion		: { type: "number"}, //
					Motivo							: { type: "string"}, //
					
					Nemotecnico						: { type: "string"}, //
					NemotecnicoSVS					: { type: "string"}, //
					NombreTipoOperacion				: { type: "string"}, //
					NombreTipoInstrumento			: { type: "string"}, //
					NombreTipoInstrumentoSubyacente	: { type: "string"},
					NombreClasificacionTipoInstrumento				: { type: "string"}, //
					NombreClasificacionTipoInstrumentoSubyacente	: { type: "string"},
					NombreLibroOrdenes				: { type: "string"}, //
					NombreCondicionDeLiquidacion	: { type: "string"}, //
					NombreCorredorContraparte		: { type: "string"}, //
					NombreSistemaBursatil			: { type: "string"}, //
					
					Observaciones					: { type: "string"},
					Plazo							: { type: "number"},
					PrecioTransaccion				: { type: "number"},
					PrecioStrike					: { type: "number"},
					
					Reservado						: {type : "string"},
					
					Tasa							: { type: "number"}, //
					TasaTransaccion					: { type: "number"}, //
					TipoLogCambio					: { type: "string"}, //
					
					ValorRescateDisponibleTransaccion	: { type: "number"},
					ValorRescateTransaccion				: { type: "number"},
					ValorRescateSimulacionTransaccion 	: { type: "number"},
					
					ModalidadLiquidacion	: { type: "string"},
					ModalidadEjercicio		: { type: "string"},
					TipoOpcion				: { type: "string"},
					EstiloOpcion			: { type: "string"},
					Tamano					: { type: "number"},
					EnAnulacion				: {type: "string"}
					
				}
			}
		};
	};
	
	obj.getSchemaInstruccion = function () {
		return {
			model: {
                fields: {
                    IdInstruccion: { type: "string" },
					IdEstadoInstruccion: {type: "string" },
                    NombreEstado: { type: "string" },
                    FechaTrade: { type: "date" },
                    FechaLiquidacion: { type: "date" },
					IdClasificacionTipoInstrumento:	{type: "string"},
					IdClasificacionTipoInstrumentoInstruccion:	{type: "string"},
                    Mercado: { type: "string" },
					IdTipoInstrumentoInstruccion: { type : "string" },
					IdTipoInstrumento: { type : "string" },
                    Proposito: { type: "string" },
                    Tipo: { type: "string" },
					IdTipoOperacion: { type : "string" },
					NombreTipoOperacion: { type : "string" },
					NombreTipoOperacionInstruccion: { type : "string" },
					IdInstrumento: { type: "number" },
                    IdEmisor: { type: "string" },
                    Nemotecnico: { type: "string" },
                    NemotecnicoSVS: { type: "string" },					
					IdCliente: { type: "number" },
					IdPortafolioCliente: { type: "number" },
                    CodigoPropositoPortafolio: { type: "string" },
					IdCuentaCustodioPortafolio: { type: "number" },
                    DocumentoIdentidad: { type: "string" },
                    NombreCompleto: { type: "string" },
					IdBolsa: { type : "string" },
					IdBoveda: { type : "string" },
					NombreBoveda: { type : "string" },
					IdIntermediario: { type: "number" },
                    Intermediario: { type: "string" },
                    FolioFactura: { type: "string" },
                    FolioPoliza: { type: "string" },
                    IdMonedaLiquidacion: { type: "string" },
                    IdCondicionDeLiquidacion: { type: "string" },
                    Cantidad: { type: "number" },
                    PrecioInstruccion: { type: "number" },
                    MontoNeto: { type: "number" },
                    TotalCobros: { type: "number" },
                    MontoLiquidacion: { type: "number" },
                    TirTasa: { type: "number" },
					PorcentajeCobros: { type: "number" },
					ValorRescate: { type: "number" },
					ValorNominal: { type: "number" },
					IdTipoCanalIngreso: { type : "string" },
					NombreCanalIngreso: { type: "string" },
					IdTipoCanalDistribucion: { type: "number" },
					IdEmpresa: { type: "number" },
                    FechaIngreso: { type: "date" },
                    FechaUltimaModificacion: { type: "date" },
                    FechaVencimiento: { type: "date" },
                    PlazoVencimiento: { type: "number" },
                    IdTipoReajuste: { type: "string" },
					Glosa: { type: "string" },
					IdTransaccion: { type: "number" },				
					IdOrden: { type: "number" },
				    IdAsignacion: { type: "number" },
				   	IdMovimientoComprobante: { type: "number" },
				    IdComprobante: { type: "number" },
                    Motivo: { type: "string" }
					
                }
            }
		};
	};
	
	obj.getSchemaDocumentoInstruccion = function () {
		return {
			model: {
                fields: {
					IdDocumentoInstruccion: {type: "number" },
                    IdInstruccion: { type: "number" },
                    NombreDocumentoAdjunto: { type: "string" },
                    FechaActualizacion: { type: "date" },
					FechaActualizacionString: { type: "string" },
					Descripcion: { type: "string" },
					Borrado: { type: "string" }
                }
            }
		};
	};

	obj.getSchemaOrden = function(){
		return {
			model: {
				fields : {
					AsignadoDia					: { type: "number"},
					AccionOrden					: { type: "string"},
					AccionTipoOperacion			: { type: "string"},
					
					CantidadAsociada			: { type: "number"},
					CantidadOrden				: { type: "number"},
					CantidadDisponibleOrden		: { type: "number"},
					CantidadDisponibleAsociar	: { type: "number"},
					CantidadSimulacionOrden		: { type: "number"},
					CantidadAsignadaOrden		: { type: "number"},
					CodigoBrokerExtranjero		: { type: "string"},
					CodigoBancoCustodio			: { type: "string"},
					CodigoBancoLiquidador		: { type: "string"},
					CodigoBoveda				: { type: "string"},
					CodigoPortafolio			: { type: "string"},
					CodigoEmpleadoPortafolio	: { type: "string"},
					CodigoEmpleadoDigitador		: { type: "string"},
					CodigoProcedenciaOrden		: { type: "string"},
					CodigoCondicionDeLiquidacion: { type: "string"},
					CodigoCorredorCorresponsal	: { type: "string"},
					DocumentoIdentidad			: { type: "string"},
					
					EsConfirmadaUI				: { type: "string"},
					EsClienteFinal				: { type: "string"},
					EstadoOrden					: { type: "string"},	
					EstadoOrdenAsignacion		: { type: "string"},	
					EsPutEsCall					: { type: "string"},
					EsVentaCorta				: { type: "string"},
					EsVentaCortaUI				: { type: "string"},
					EsPorMontoUI				: { type: "string"},
					EstiloOpcion				: { type: "string"},
					FechaAceptacion				: { type: "datetime"},
					FechaRecepcion				: { type: "datetime"},
					FechaVigencia				: { type: "sysdate"},
					FechaVencimiento			: { type: "sysdate"},
					FechaOperacionOrden			: { type: "datetime"},
					FechaOferta					: { type: "sysdatetime"},
					FechaLiquidacion			: { type: "sysdate"},
					FolioOrden					: { type: "number"},
					
					GlosaOrden					: { type: "string"},
					
					IdBolsa						: { type: "string"},
					IdClasificacionTipoInstrumento : { type: "string"},
					IdCliente					: { type: "number"},
					IdCondicionDeLiquidacion 	: { type: "string"},
					IdCuentaCustodioPortafolio  : { type: "string"},
					IdEstadoAsignacion			: { type: "string"},
					IdGrupoCobro				: { type: "number"},
					IdLibroOrdenes				: { type: "number"},
					IdMedioDePago				: { type: "string"},
					IdMonedaOrden				: { type: "string"},					
					IdMonedaLiquidacionOrden	: { type: "string"},					
					IdMonedaInstrumento			: { type: "string"},
					IdOrden 					: { type: "number"},
					IdPersonaBroker				: { type: "number"},
					IdPortafolio				: { type: "number"},
					IdSistemaBursatil			: { type: "string"},
					IdTipoPrecio				: { type: "string"},
					IdTipoInstrumento			: { type: "string"},
					Instrumento					: { type: "string"},
					InstrumentoUI				: { type: "string"},
					InstrumentoSubyacente		: { type: "string"},
					IdInstrumentoNegociacion	: { type: "number"},
					IndicadorBancoCustodio		: { type: "string"},
					
					MercadoSMV					: { type: "string"},
					ModalidadSMV				: { type: "string"},
					ModoFacturacion				: {type: "string"},
					MontoAsociado				: { type: "number"},
					MontoOrden					: { type: "number"},
					MontoLiquidacion			: { type: "number"},
					MontoDisponibleAsociar		: { type: "number"},
					MontoDisponibleOrden		: { type: "number"},
					ModalidadLiquidacion		: { type: "string"},
					ModalidadEjercicio			: { type: "string"},
					MontoOrdenPlazo  			: { type: "number"},
					
					NombreBolsa					: { type: "string"},
					NombreBoveda				: { type: "string"},
					NombreCanal					: { type: "string"},
					NombreCanalDistribucion		: { type: "string"},
					NombreClaseOrden			: { type: "string" },
					NombreClasificacionTipoInstrumento				: { type: "string"},
					NombreClasificacionTipoInstrumentoSubyacente	: { type: "string"},
					NombreCompleto 				: { type: "string" },
					NombreCondicionDeLiquidacion: { type: "string"},
					NombreEmpleadoDigitador		: { type: "string"},
					NombreEmpleadoPortafolio	: { type: "string"},
					NombreFormaCustodia			: { type: "string"},
					NombreMedioDePago			: { type: "string"},
					NombreLibroOrdenes			: { type: "string"},
					NombreSistemaBursatil		: { type: "string"},
					NombreSucursalEmpleadoDigitador		: { type: "string"},
					NombreSucursalEmpleadoPortafolio	: { type: "string"},
					NombreTipoOperacion			: { type: "string"},
					NombreTipoInstrumento		: { type: "string"},
					NombreTipoInstrumentoSubyacente					: { type: "string"},
					NombreTipoVigenciaOrden		: { type: "string"},
					NombreAutorizadorOrden		: { type: "string"},
					
					OrdenVigenteUI				: { type: "string"},
					OrdenEspecialUI				: { type: "string"},
					
					PrecioOrden					: { type: "number"},
					PrecioStrike				: { type: "number"},
					PrecioPromedioAsignacion	: { type: "number"},
					Plazo						: { type: "number"},
					PlazoReporte				: { type: "number"},
					
					RazonSocial					: { type: "string"},
					ReportanteOReportado		: { type: "string"},
					Reservado					: { type : "string"},
					SaldoRep					: { type:"number"},
					Tamano						: { type: "number"},
					
					Tasa						: { type: "number"},
					TipoLiquidacion				: {type: "string"},
					TasaPromedioReporte			: { type: "number"},
					TipoPrecio					: { type: "string"},
					TipoOpcion					: { type: "string"},
					IdSistemaBursatil			: { type: "string"},
					IdTipoLibroOrden			: { type: "string"},
					Producto					: { type: "string"},
					Interes						: { type: "number"},
					EnAnulacion					: {type: "string"},
					RazonSocialPortafolio		: {type: "string"},
					PorcentajeVisibilidad		: {type: "string"},
					InstrumentoNegociacionOrden      : {type: "string"},
					InstrumentoNegociacion		: {type: "string"},
					IdMonedaNegociacion			: {type: "string"},

				}
			}
		};
	};
	
	obj.getSchemaGarantia = function(){
		return {
			model: {
				fields : {
					IdGarantia					: { type: "number"},
					FolioGarantia				: { type: "number"},	
					FolioCarteraOrigen			: { type: "number"},
					FolioCarteraGarantizada		: { type: "number"},
					EsEspecifica				: { type: "string"},
					IdInstrumentoCarteraGarantizada	: { type: "number"},
					//NemoGarantizado					: { type: "string"},
					//IdTipoInstrumentoGarantizado	
					//IdContraparteGarantia	
					//RazonSocialContraparte	
					//IdOrigenGarantia	
					//NombreOrigenGarantia	
					//IdEspecieGarantia	
					//DescripcionEspecie	
					FechaIngresoRegistro	: { type: "sysdate"},
					//FechaModificacionRegistro	
					//IdEmpresa	
					//IdUsuario	
					//IdModulo	
					IdCliente		: { type: "number"},
					IdPortafolio	: { type: "number"},
					NombreCliente	: { type: "string"},
					//DocumentoIdentidad	
					CodigoPortafolio	: { type: "number"},
					IdEstado			: { type: "string"},
					NombreEstado		: { type: "string"},
					//IdTipoProductoOperacionOrigen	
					//IdTipoCanalIngreso	
					//IdMonedaSistema	
					//NombreMonedaSistema	
					//CodigoIFRS	
					IdConcepto			: { type: "string"},
					NombreConcepto		: { type: "string"},
					//IdTipoOperacionOperacionOrigen	
					//FechaOperacion	
					//FechaVencimientoOperacion	
					//PlazoEnDiasOperacion	
					//FolioExternoReferencia	
					//IdMesaOperacionOrigen	
					//IdBolsaOperacionOrigen	
					//IdSucursal	
					//IdAgente	
					//IdCondicionLiquidacion	
					//FechaLiquidacion	
					//IdClasificacionTipoInstrumento	
					//ClasificacionTipoInstrumento	
					//IdTipoInstrumento	
					//NombreTipoInstrumento	
					IdInstrumento		: { type: "number"},
					Nemotecnico			: { type: "string"},
					//EsPagaderoEn	
					NombreInstrumento	: { type: "string"},
					//IdEmisor	
					//NemoSVS	
					//IdTipoReajuste	
					//FechaEmisionInstrumento	
					//FechaVencimientoInstrumento	
					//EsFungible	
					//IdBovedaOperacionOrigen	
					IdMonedaInstrumento		: { type: "string"},
					MontoGarantiaMonedaInstrumento		: { type: "number"},
					NombreMonedaInstrumento				: { type: "string"},
					//IdTipoComprobanteOperacionOrigen	
					//FolioComprobanteOperacionOrigen	
					//FechaComprobanteOperacionOrigen	
					Cantidad				: { type: "number"},
					PrecioGarantia			: { type: "number"},
					TasaGarantia			: { type: "number"},
					MontoGarantia			: { type: "number"},
					//CorteMinimo	
					//ValorRescate	
					//PrecioCompraOperacionOrigen	
					//TasaCompraOperacionOrigen	
					//PrecioPromedioCompraOperacionOrigen	
					//MontoPrecioPromedioCompraOperacionOrigen	
					//MontoPrecioPromedioMonedaInstrumentoOperacionOrigen	
					//ValorTipoCambioCierre	
					//INGFechaOperacion	
					//INGFechaLiquidacion	
					//INGCantidad	
					//CantidadGarantia	
					//INGPrecioGarantia	
					//INGTasaGarantia	
					//INGValorRescate	
					//INGMonto	
					//INGMontoATasaCompraActual	
					//INGMontoCompraActualMonedaInstrumento	
					//MontoValorizadoMonedaInstrumento	
					MontoValorizado			: { type: "number"},
					//INGMontoATasaCompraAnterior	
					//INGPrecioMercadoActual	
					//INGTasaMercadoActual	
					/*INGMontoATasaMercadoActual	
					INGMontoATasaMercadoAnterior	
					IdTipoInstrumentoAval	
					NombreTipoInstrumentoAval	
					IdClasificacionTipoInstrumentoAval	
					NombreClasificacionTipoInstrumentoAval	
					ValorClasificacionRiesgo	
					IdEntidadClasificadoraRiesgo	
					IdTipoClasificacionRiesgo	
					INGDurationCompraActual	
					INGPrecioMercadoProximo	
					INGTasaMercadoProximo	
					INGMontoATasaCompraProximo	
					INGMontoATasaMercadoProximo	
					INGMontoInteresCompraProximo	
					INGDurationCompraProximo	
					INGMontoReajusteCompraProximo	
					FuenteTasaPrecioMercado	
					FechaValorizacionGarantia	
					INGPrecioMercadoAnterior	
					EGRCantidad	
					EGRFechaLiquidacion	
					EGRFechaOperacion	
					ISIN	
					MontoInteresCorrido	
					MontoReajusteCorrido	
					MontoInteresCorridoMercado	
					MontoReajusteCorridoMercado	
					INGFechaValorizacionAnterior	
					INGFechaValorizacionActual	
					ValorNominal	
					EGRIdConcepto	
					IdCuentaCustodioPortafolio	
					IdEntidadDestinoGarantiaEmpresa
					*/
					
				}
			}
		};
	};

	obj.getSchemaAsignacion = function() {
		return {
			model: {
				fields : {
					AccionOrden						: { type: "string"},
					
					CantidadAsignadaSimulacion 		: { type: "number"},
					CantidadTransaccionSimulacion 		: { type: "number"},
					ValorRescateMontoTransaccion		: { type: "number"},
					
					CantidadTransaccion 			: { type: "number"},
					CodigoEmpleadoPortafolio		: { type: "string"},
					CodigoEmpleadoDigitador			: { type: "string"},
					CantidadAsignada			: { type: "number"},
					CodigoBoveda				: { type: "string"},
					CodigoIFRS				: { type: "string"},
					CodigoBancoCustodio			: { type: "string"},
					CodigoBancoLiquidador			: { type: "string"},
					CodigoPortafolio			: { type: "string"},
					CodigoTipoOperacionAccion		: { type: "string"},
					CodigoCondicionDeLiquidacion		: { type: "string"},
					CodigoOperador 			: { type: "string"},
					
					
					DocumentoIdentidadAdicional  		: { type: "string"},
					
					DocumentoIdentidad			: { type: "string"},
					
					EsExtranjera				: { type: "string"},
					EsFungible				: { type: "string"},
					EsAPlazo				: { type: "string"},
					EsAPlazoUI				: { type: "string"},
					EsVentaCorta				: { type: "string"},
					EsVentaCortaUI				: { type: "string"},
					EstadoAsignacion			: { type: "string"},
					EstiloOpcion				: { type: "string"},
					
					FechaAceptacion				: { type: "sysdatetime"},
					FechaAsignacion				: { type: "datetime"},
					FechaLiquidacion			: { type: "sysdate"},
					FechaOperacionOrden			: { type: "sysdatetime"},
					FechaOperacionTransaccion		: { type: "sysdatetime"},
					FechaTransaccion			: { type : "sysdate"},
					FechaVencimiento			: { type: "sysdate"},
					FolioCarteraAsignada 			: { type : "number"},
					FolioTransaccion			: { type: "string"},
					FolioOrden				: { type: "number"},
					
					Glosa					: { type : "string"},
					
					IdAsignacion 				: { type: "number"},
					IdAsignacionRef 			: { type: "number"},
					IdBolsa					: { type: "string"},
					IdBoveda				: { type : "string"},
					IdClasificacionTipoInstrumento		: { type: "string"},
					IdClasificacionTipoInstrumentoSubyacente: { type: "string"},
					IdCliente				: { type: "number"},
					IdCompFacturacionRef			: { type: "number"},
					IdCondicionDeLiquidacion		: { type: "string"},
					IdCorredorContraparte			: { type: "number"},
					IdCuentaCustodioPortafolio		: { type: "number"},
					CuentaCustodioPortafolio		: { type: "string"},
					IdEstadoAsignacion			: { type: "string"},
					IdEmisor				: { type: "string"},
					IdEstadoFacturacion			: { type: "string"},
					IdFormaCustodia				: { type : "string"},
					IdInstrumento				: { type: "number"},
					IdInstrumentoUI				: { type: "number"},
					IdInstrumentoSubyacente			: { type: "number"},
					IdInterno				: { type: "number"},
					IdLibroOrdenes				: { type: "number"},
					IdMedioDePago				: { type: "string"},
					IdMonedaInstrumento			: { type: "string"},
					IdMonedaLiquidacionOrden		: { type: "string"},
					IdMonedaOperacion			: { type: "string"},
					IdMovimiento				: { type: "number"},
					IndicadorBancoCustodio			: { type: "string"},
					IdOrden					: { type: "number"},
					IdPreferenciaPagoPortafolio		: { type: "number"},
					IdPortafolio				: { type: "number"},
					IdTipoCanalIngreso			: { type: "string"},
					IdTipoInstrumento			: { type: "string"},
					IdTipoInstrumentoSubyacente		: { type: "string"},
					IdTipoReajuste				: { type: "string"},
					IdTipoOperacion				: { type: "string"},
					IdTipoProducto				: { type: "string"},
					IdTransaccion				: { type: "number"},
					IndicadorOrdenDirecta			: { type: "string"},
					Instrumento				: { type: "string"},
					InstrumentoUI				: { type: "string"},
					InstrumentoSubyacente			: { type: "string"},
					InstrumentoNegociacionTran  : { type: "string"},

					MontoAsignado				: { type: "number"},
					MontoAsignadoLiquidacion		: { type: "number"},
					MontoAsignadoLocal			: { type: "number"},
					MontoAsignadoPlazo			: { type: "number"},
					ModalidadLiquidacion			: { type: "string"},
					ModalidadEjercicio			: { type: "string"},
					ModoFacturacion				: {type: "string"},
					MontoAsignacion				: { type: "number"}, // para simulacion de asignacion(calculado por regla 3)
					MontoTransaccion 			: { type: "number"},
					MontoCLP				: { type: "number"},
					
					Nemotecnico				: { type: "string"},
					NombreBolsa				: { type: "string"},
					NombreBoveda				: { type: "string"},
					NombreClasificacionTipoInstrumento	: { type: "string"},
					NombreClasificacionTipoInstrumentoSubyacente	: { type: "string"},
					NombreCompleto				: { type: "string"},
					NombreCondicionDeLiquidacion		: { type: "string"},
					NombreFormaCustodia			: { type: "string"},
					
					NombreLibroOrdenes			: { type: "string"},
					NombreMedioDePago			: { type: "string"},
					NombreTipoCanalIngreso			: { type: "string"},
					NombreTipoCanalDistribucion		: { type: "string"},
					NombreTipoInstrumento			: { type: "string"},
					NombreTipoInstrumentoSubyacente		: { type: "string"},
					NombreTipoOperacion			: { type: "string"},
					NombreEstadoFacturacion			: { type: "string"},
					
					Plazo					: { type: "number"},
					PlazoVentaCorta				: { type : "number"},
					PrecioAsignacion			: { type: "number"},
					PrecioPromedio 				: { type : "number"},
					PrecioSimulacion			: { type : "number"},
					PagoCobroCuentaInversion		: { type: "string"},
					PrecioStrike				: { type: "number"},
					
					RazonSocial				: { type: "string"},
					
					SecAsignacionMigracion			: { type: "number"},
					
					Tamano					: { type: "number"},
					Tasa					: { type: "string"},
					TasaVentaCorta				: { type : "number"},
					TipoLiquidacion			: {type: "string"},
					TipoOperacion				: { type: "string"},
					TipoCambio				: { type : "number"},
					TipoOpcion				: { type: "string"},
					
					ValorRescateAsignado			: { type: "string"},
					ValorRescateAsignadoSimulacion 		: { type : "number"},
					ValorClasificacionRiesgo		: { type: "string"},
					
					cobrosAsignacion			: { type : "array"},
					Producto				: { type: "string"},
					RazonSocialPortafolio	: { type: "string"},
					IdComision : { type: "string"},
					FormaComisionGrilla : { type: "string"},
					ValorComision : { type: "number"},
					MontoComision : { type: "number"},
					

					MontoAsignacionSimulacion : { type: "number"},
					TasaVentaCorta			: { type: "string"},
					FolioSpot   : { type: "number"},			
					EstadoSpot: { type: "string"},
					InstrumentoNegociacion : { type: "string"},
					IdAsignacionMasiva : { type: "number"},
					NombreTipoAsignacion : { type: "string"},
					AplicaComisionAsignacionMasiva	: { type: "string"},
					MarcaAsignacion : { type: "string"}				
				}
			}
		};
	};
	
	obj.getSchemaAsignacionResum = function() {
		return {
			model: {
				fields : { 
					IdAsignacion                    : { type: "number"},
					FechaAsignacion					: { type: "sysdatetime"},
					IdTransaccion                   : { type: "number"},
					CantidadAsignada				: { type: "number"},
					MontoAsignado  				    : { type: "number"},
					IdEstadoAsignacion				: { type: "string"},
					IdMonedaOperacion               : { type: "string"},
					IdTipoInstrumento               : { type: "string"},
				}
			}
		};
	};
	obj.getSchemaComprobante = function() {
		return {
			model: {
				fields : {
					DocumentoIdentidad				: { type: "string"},
					CodigoCondicionDeLiquidacion	: { type: "string"},
					CodigoPortafolio				: { type: "string"},
					FechaIngresoRegistro			: { type: "datetime"},
					FechaLiquidacion				: { type: "sysdate"},
					IdAgente						: { type: "number"},
					IdBolsa							: { type: "string"},
					IdCliente						: { type: "number"},
					IdComprobante					: { type: "number"},
					IdConceptoMovimiento			: { type: "string"},
					IdCondicionLiquidacion			: { type: "string"},
					IdEmpresa						: { type: "number"},
					IdEstadoComprobante				: { type: "string"},
					IdMonedaLiquidacion				: { type: "string"},
					IdMovimiento					: { type: "number"},
					IdPortafolio					: { type: "number"},
					IdSucursal						: { type: "number"},
					IdTipoComprobante				: { type: "string"},
					MontoCobroComprobante			: { type: "number"},
					MontoComprobante				: { type: "number"},
					MontoFinal						: { type: "number"},
					NombreCompleto					: { type: "string"},
					NombreEstadoComprobante			: { type: "string"},
					IdClasificacionTipoInstrumento  : { type: "string"},
					NombreLargoCliente				: { type: "string"},
					Origen							: { type: "string"},
					NemotecnicoInstrumentoComprobante:{type:"string"},
					CantidadTotal					: {type:"number"},
					PPP								: {type:"number"},
					SeparaPorInstrumento			: {type:"string"},
					IdMoneda						: {type:"string"},
					IdTipoInstrumento				: {type:"string"},

					
				}
			}
		};
	};
	obj.getSchemaMovFacturar = function() {
		return {
			model: {
				fields : {
					IdMovimientoComprobante : { type: "number"},
					IdConceptoMovimiento : { type: "string"},
					NombreConceptoMovimiento : { type: "string"},
					FolioComprobante : { type: "string"},
					Monto: { type: "number"},
					NombreEstadoFacturacion : { type: "string"},
				}
			}
		};
	};

	obj.getSchemaCobrosAsignacion = function() {
		return {
			model: {
				fields : {
					CodigoCobroClasificacion 		: { type: "string"},
					IdCobroAsignacion 				: { type: "number"},
					MontoCobroCalculadoLiquidacion 	: { type: "number"},
					NombreCobro 					: { type: "string"},
					IdMonedaLiquidacionOrden 		: { type: "string"},
					IdCobro 						: { type: "string"},
					FormaCobro  					: { type: "string"},
					NombreFormaCobro  				: { type: "string"},
				 	DescripcionCobroClasificacion 	: { type: "string"},
				 	ValorCobroAplicado				: { type: "string"},
				 	MontoCobroCalculado 			: { type: "string"}
				}
			}
		};
	};

	obj.getSchemaCobrosOrden = function() {
		return {
			model: {
				fields : {
					CodigoCobroClasificacion 		: { type: "string"},
					NombreCobro 					: { type: "string"},
					NombreFormaCobro 				: { type: "string"},
					MontoCobroCalculadoLiquidacion 	: { type: "number"},
					IdMonedaLiquidacionOrden 		: { type: "string"},
					IdCobroOrden 					: { type: "number"},
					IdCobro 						: { type: "string"},
					FormaCobro  					: { type: "string"},
				 	DescripcionCobroClasificacion	: { type: "string"},
				 	ValorCobroAplicado				: { type: "string"},
				 	MontoCobroCalculado				: { type: "string"}
				}
			}
		};
	};
	
	obj.generarFiltroOperadorCarteraInstrumento = function(filtros,callback){
		var filtrosInstrumento = {logic : 'or',filters : []}
		if (filtros.Operador && filtros.Operador.trim() != 'TODOS'){
			var lefiltro = [
				{ field: "IdEmpresa", operator: "eq", value: $rootScope.SesionIdEmpresa },
				{ field: "CodigoOperador", operator: "eq", value: filtros.Operador }
			];
			if(filtros.OperadorCarteraInversion!=undefined){
				if(filtros.OperadorCarteraInversion){
					if(filtros.OperadorCarteraInversion.trim()!='TODOS'){
						lefiltro.push({ field: "NombreCarteraInversion", operator: "eq", value: filtros.OperadorCarteraInversion });
					}
				}
			}
			var ClienteDatosGeneralesDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21003",
							dataType: "json"
					    }
					},
					filter: lefiltro,
					schema: {
						model: {
							fields : {
								CodigoOperador : { type: "string"},
								NombreCarteraInversion : { type: "string"},
								IdEmpresa : { type: "number"},
								IdInstrumento : { type: "number"}
							}
						}
					}
				}
			});
			ClienteDatosGeneralesDatasource.query().then(function(result){
				$.each(ClienteDatosGeneralesDatasource.data(),function(key,value){
					filtrosInstrumento.filters.push({ field: "IdInstrumento", operator: "eq", value: value.IdInstrumento });
				});
				if(filtrosInstrumento.filters.length>0)return callback(filtrosInstrumento);
				return callback({ field: "IdInstrumento", operator: "eq", value: -1 });
			});
		}else{
			return callback(filtrosInstrumento);
		}
	}
	
	obj.generarFiltroOperadorCarteraEjecutivo = function(filtros,callback){
		var filtrosEjecutivo = {logic : 'or',filters : []}
		if (filtros.Operador && filtros.Operador.trim() != 'TODOS'){
			var lefiltro = [
				{ field: "IdEmpresa", operator: "eq", value: $rootScope.SesionIdEmpresa },
				{ field: "CodigoOperador", operator: "eq", value: filtros.Operador }
			];
			if(filtros.OperadorCarteraEjecutivo != undefined && filtros.OperadorCarteraEjecutivo.trim()!='TODOS'){
				lefiltro.push({ field: "NombreCarteraEjecutivo", operator: "eq", value: filtros.OperadorCarteraEjecutivo });
			}
			var ClienteDatosGeneralesDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=21043",
							dataType: "json"
					    }
					},
					filter: lefiltro,
					schema: {
						model: {
							fields : {
								CodigoOperador 			: { type: "string"},
								NombreCarteraEjecutivo 	: { type: "string"},
								IdEmpresa 				: { type: "number"},
								IdEmpleadoEjecutivo 	: { type: "number"}
							}
						}
					}
				}
			});
			ClienteDatosGeneralesDatasource.query().then(function(result){
				$.each(ClienteDatosGeneralesDatasource.data(),function(key,value){
					filtrosEjecutivo.filters.push({ field: "IdEmpleadoEjecutivo", operator: "eq", value: value.IdEmpleadoEjecutivo });
				});
				if(filtrosEjecutivo.filters.length>0)return callback(filtrosEjecutivo);
				return callback({ field: "IdEmpleadoEjecutivo", operator: "eq", value: -1 });
			});
		}else{
			return callback(filtrosEjecutivo);
		}
	}
	
	obj.validarFiltrosV2 = function(filtros,modo,submodo)
	{
		var filters = new Array();
		filters.push({ field: "IdEmpresa", operator: "eq", value: $rootScope.SesionIdEmpresa});
	
		if (filtros.IdClasificacionTipoInstrumento)
			filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: filtros.IdClasificacionTipoInstrumento });
		if (filtros.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.IdTipoInstrumento });
		if (filtros.instrumento && filtros.instrumento.IdInstrumento && filtros.instrumento.IdTipoOperacion!=="COMSIM")
			filters.push({ field: "IdInstrumentoUI", operator: "eq", value: Number(filtros.instrumento.IdInstrumento) });
		if (filtros.IdTipoOperacion)
			filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.IdTipoOperacion });
		if (filtros.AccionOrden === 'COMPRA' || filtros.AccionOrden === 'VENTA')
			filters.push({ field: "AccionOrden", operator: "eq", value: filtros.AccionOrden });	
		if (filtros.IdCondicionDeLiquidacion)
			filters.push({ field: "IdCondicionDeLiquidacion", operator: "eq", value: filtros.IdCondicionDeLiquidacion });
		if (filtros.IdLibroOrdenes)
			filters.push({ field: "IdLibroOrdenes", operator: "eq", value: filtros.IdLibroOrdenes });
		if (filtros.cantidadDisponible)
			filters.push({ field: "CantidadDisponible", operator: "gt", value: 0});
		if (filtros.esAPlazo)
			filters.push({ field: "EsAPlazo", operator: "eq", value: filtros.esAPlazo});
		if(filtros.OrdenPorAsignar){
			filters.push({ field: "CantidadDisponibleOrden", operator: "gt", value: 0});
			filters.push({ field: "IdEstadoOrden", operator: "eq", value: "OFE"});
		}
		
		if (filtros.instrumentoSubyacente && filtros.instrumentoSubyacente.IdInstrumento){
			filters.push({ field: "IdInstrumentoSubyacente", operator:"eq", value: filtros.instrumentoSubyacente.IdInstrumento})
		}
		
		if (filtros.IdMoneda && filtros.IdMoneda.trim() != 'TODOS'){
			filters.push({ field: "IdMoneda", operator:"eq", value: filtros.IdMoneda})	
		} 
		if (modo === 'ORDEN') {
		
			if (filtros.IdTipoCanalIngreso) {
				filters.push({field : "IdTipoCanalIngreso", operator : "eq", value: filtros.IdTipoCanalIngreso});
			} 
			if (submodo === 'PORASIGNAR') {
				if (filtros.IdBolsa) {
					filters.push({
						logic : 'or',
						filters : [{ field: "IdBolsa", operator: "eq", value: filtros.IdBolsa },{ field: "IdBolsa", operator: "eq", value: "0" }]
					});
				}
				
				if (filtros.Instrumentos) {
					var filtrosInstrumento = {
						logic : 'or',
						filters : []
					}
					$.each(filtros.Instrumentos,function(key,value){
						filtrosInstrumento.filters.push({ field: "IdInstrumento", operator: "eq", value: value });
					})
					filters.push(filtrosInstrumento);
				}
				
				if (filtros.InstrumentosSubyacente) {
					var filtrosInstrumentoSubyacente = {
						logic : 'or',
						filters : []
					}
					$.each(filtros.InstrumentosSubyacente,function(key,value){
						filtrosInstrumentoSubyacente.filters.push({ field: "IdInstrumentoSubyacente", operator: "eq", value: value });
					})
					filters.push(filtrosInstrumentoSubyacente);
				}
				if (filtros.IdsMonedaTransaccion) {
					var filtrosIdsMonedaTransaccion = {
						logic : 'or',
						filters : []
					}
					$.each(filtros.IdsMonedaTransaccion,function(key,value){
						filtrosIdsMonedaTransaccion.filters.push({ field: "IdMonedaOrden", operator: "eq", value: value });
					})
					filters.push(filtrosIdsMonedaTransaccion);
				}
				if (filtros.InstrumentosNegociacion) {
					var filtrosInstrumentosNegociacion = {
						logic : 'or',
						filters : []
					}
					$.each(filtros.InstrumentosNegociacion,function(key,value){
						filtrosInstrumentosNegociacion.filters.push({ field: "IdInstrumentoNegociacion", operator: "eq", value: Number(value) });
					})
					filters.push(filtrosInstrumentosNegociacion);
				}
			}
			
			if (submodo==='PORNEGOCIAR'){
				if (filtros.Operador && filtros.Operador.trim() != 'TODOS'){
					this.generarFiltroOperadorCarteraInstrumento(filtros,function(filtroInstrumentos){
						filters.push(filtroInstrumentos);

					});
				}
				if (filtros.Operador && filtros.Operador.trim() != 'TODOS'){
					this.generarFiltroOperadorCarteraEjecutivo(filtros,function(filtroEjecutivos){
						filters.push(filtroEjecutivos);
					});
				}
			}
			
			if (filtros.IdBolsa && submodo !== 'PORASIGNAR')
				filters.push({ field: "IdBolsa", operator: "eq", value: filtros.IdBolsa });
			
			if (filtros.OrdenEspecial=== true)
				filters.push({ field: "OrdenEspecial", operator: "eq", value: 'S'});
			
			if (filtros.EsVentaCorta === true)
				filters.push({ field: "EsVentaCorta", operator: "eq", value: 'S'});
			
			//if (filtros.EsVentaCorta === false && submodo === 'PORASIGNAR')
				//filters.push({ field: "EsVentaCorta", operator: "eq", value: 'N'});
			
			
			if (filtros.EsVentaCorta === false && submodo === 'PORASIGNAR')
				filters.push({ field: "EsVentaCorta", operator: "eq", value: 'N'});
			
			if (filtros.IdClasificacionTipoInstrumento==='IF' && (filtros.EsFungible !== undefined))
				filters.push({ field: "EsFungible", operator: "eq", value: filtros.EsFungible?'S':'N'});
			
			if (filtros.fechaDesde)
				//filters.push({ field: "FechaOperacionOrden", operator: "gte", value: moment(moment(filtros.fechaDesde).format().split('T')[0] + ' 00:00:00').toDate()});
				filters.push({ field: "FechaOperacionOrden", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaDesde))});
			if (filtros.fechaHasta)
				//filters.push({ field: "FechaOperacionOrden", operator: "lte", value: moment(moment(filtros.fechaHasta).format().split('T')[0] + ' 23:59:59').toDate()});
				filters.push({ field: "FechaOperacionOrden", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaHasta))});
			if (filtros.fechaOperacion) {
				//filters.push({ field: "FechaOperacionOrden", operator: "gte", value: moment(moment(filtros.fechaOperacion).format().split('T')[0] + ' 00:00:00').toDate()});
				filters.push({ field: "FechaOperacionOrden", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaOperacion))});
				//filters.push({ field: "FechaOperacionOrden", operator: "lte", value: moment(moment(filtros.fechaOperacion).format().split('T')[0] + ' 23:59:59').toDate()});
				filters.push({ field: "FechaOperacionOrden", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaOperacion))});
			}
			if (filtros.FechaOperacion) {
				  //filters.push({ field: "FechaOperacionOrden", operator: "gte", value: moment(moment(filtros.FechaOperacion).format().split('T')[0] + ' 00:00:00').toDate()});
				  //filters.push({ field: "FechaOperacionOrden", operator: "lte", value: moment(moment(filtros.FechaOperacion).format().split('T')[0] + ' 23:59:59').toDate()});
				  filters.push({ field: "FechaOperacionOrden", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.FechaOperacion))});
				}
			if (filtros.fechaOperacionVigencia) {
				  //filters.push({ field: "FechaVigencia", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaOperacionVigencia))});
				  //filters.push({ field: "FechaVigencia", operator: "lte", value: util.dateOdata(util.dateSPdate(util.dateAddMonths(filtros.fechaOperacionVigencia,1)))});
				  filters.push({ field: "FechaVigencia", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaOperacionVigencia))});
				}
			if (filtros.PrecioOrden && filtros.AccionOrden) {
	        	if(filtros.AccionOrden === 'COMPRA'){
	        		filters.push({ field: "PrecioOrden", operator: "gte", value: filtros.PrecioOrden});
	        	}
	        	if(filtros.AccionOrden === 'VENTA'){
	        		filters.push({ field: "PrecioOrden", operator: "lte", value: filtros.PrecioOrden});
	        	}
	        }
			if (filtros.esVentaCorta)
				filters.push({ field: "EsVentaCorta", operator: "eq", value: "S"});
			if (filtros.nombreCompleto)
				filters.push({ field: "tolower(NombreCompleto)", operator: "contains", value: filtros.nombreCompleto.toLowerCase()});
			if (filtros.documentoIdentidad)
				filters.push({ field: "tolower(DocumentoIdentidad)", operator: "contains", value: filtros.documentoIdentidad.toLowerCase()});
			if (filtros.OrdenesVigentes === true){
				
				var nf = _.filter(filters,function(n) { 
					return n.field !== "FechaOperacionOrden" && n.field !== "OrdenVigente" 
				});
				nf.push({ field: "OrdenVigente", operator: "eq", value: "S"});
				nf.push({ field: "IdEstadoOrden", operator: "eq", value: "ANU"});
				nf.push({ field: "IdEstadoOrden", operator: "eq", value: "RZD"});
				nf.push({ field: "CantidadDisponibleOrden", operator: "gt", value: 0});
				var filtros1 = {
						logic: 'and',
						filters : nf
				}
				filters.push({ field: "IdEstadoOrden", operator: "neq", value: "ANU"});  // OrdenesVigentes excluye anuladas
				filters.push({ field: "IdEstadoOrden", operator: "neq", value: "RZD"});  // OrdenesVigentes excluye rechazadas
				filters.push({ field: "CantidadDisponibleOrden", operator: "gt", value: 0});  // OrdenesVigentes considera solo ordenes con saldo
				var filtros2 = {
						logic : 'and',
				        filters : filters
				};
				var filters = {
					logic : 'or',
					filters : [filtros1,filtros2]
				};
				
			}
			if(filtros.esConfirmada){
				filters.push({ field: "EsConfirmada", operator: "eq", value: "S"});
			}
			if(filtros.IdCliente)
				filters.push({ field: "IdCliente", operator: "eq", value: filtros.IdCliente});
			
			if(filtros.IdPortafolio)
				filters.push({ field: "IdPortafolio", operator: "eq", value: filtros.IdPortafolio});
			
			if(filtros.IdProductoAgrupador || filtros.IdProductoAgrupador === 0)
				filters.push({ field: "IdProductoAgrupador", operator: "eq", value: filtros.IdProductoAgrupador});
			
			if(filtros.IdCorredorCorresponsal)
				filters.push({ field: "IdCorredorCorresponsal", operator: "eq", value: filtros.IdCorredorCorresponsal});
		}
		
		if (modo === 'TRANSACCION') {
			
			if (filtros.TransaccionesConSaldo)
				filters.push({ field: "CantidadDisponibleTransaccion", operator: "gt", value: 0});
			if (filtros.IdBolsa)
				filters.push({ field: "IdBolsa", operator: "eq", value: filtros.IdBolsa });
			if (filtros.EsVentaCorta === true)
				filters.push({ field: "EsVentaCorta", operator: "eq", value: 'S'});
			if (filtros.IdClasificacionTipoInstrumento==='IF' && (filtros.EsFungible === true))
		           filters.push({ field: "EsFungible", operator: "eq", value: 'S'});
			if (filtros.fechaDesde)
	           filters.push({ field: "FechaOperacionTransaccion", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaDesde))});
			if (filtros.fechaHasta)
				filters.push({ field: "FechaOperacionTransaccion", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaHasta))});
			
			if (filtros.marcaNegociacion)
				filters.push({ field: "MarcaNegociacion", operator: "contains", value: filtros.marcaNegociacion});
	        if (filtros.fechaOperacion) {
	           filters.push({ field: "FechaOperacionTransaccion", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaOperacion))});
	           filters.push({ field: "FechaOperacionTransaccion", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaOperacion))});
			}
			if (filtros.codigoOperador){
				var filtrosOperador = {
					logic : 'or',
					filters : []
				}

				if (filtros.AccionOrden === 0 || filtros.AccionOrden === 'COMPRA') filtrosOperador.filters.push({ field: "CodigoOperadorComprador", operator: "eq", value: filtros.codigoOperador});
				if (filtros.AccionOrden === 0 || filtros.AccionOrden === 'VENTA') filtrosOperador.filters.push({ field: "CodigoOperadorVendedor", operator: "eq", value: filtros.codigoOperador});
				filters.push(filtrosOperador);
			}
			if (filtros.TransaccionesReservadas)
				filters.push({ field: "Reservado", operator: "eq", value: 'S'});
		}
		
		if (modo === 'ASIGNACION') {
			if (filtros.IdBolsa)
				filters.push({ field: "IdBolsa", operator: "eq", value: filtros.IdBolsa });
			if (filtros.EsVentaCorta !== undefined)
				filters.push({ field: "EsVentaCorta", operator: "eq", value: filtros.EsVentaCorta?'S':'N'});
			if (filtros.IdClasificacionTipoInstrumento==='IF' && (filtros.EsFungible !== undefined))
		           filters.push({ field: "EsFungible", operator: "eq", value: filtros.EsFungible?'S':'N'});
			if (filtros.fechaDesde)
				filters.push({ field: "FechaAsignacion", operator: "gte", value: util.dateOdata(filtros.fechaDesde)});
			if (filtros.fechaHasta)
				filters.push({ field: "FechaAsignacion", operator: "lte", value: util.dateOdata(filtros.fechaHasta)});
			if (filtros.IdEstadoAsignacion)
				filters.push({ field: "IdEstadoAsignacion", operator: "eq", value: filtros.IdEstadoAsignacion});
			if(filtros.IdCliente)
				filters.push({ field: "IdCliente", operator: "eq", value: filtros.IdCliente});
			
			if (filtros.IdPortafolio)
				filters.push({ field: "IdPortafolio", operator: "eq", value: filtros.IdPortafolio});
			
			if(filtros.IdProductoAgrupador || filtros.IdProductoAgrupador === 0)
				filters.push({ field: "IdProductoAgrupador", operator: "eq", value: filtros.IdProductoAgrupador});
			
			filters.push({ field: "IdEstadoAsignacion", operator: "neq", value: 'ANU'});
		}
		
		if (modo === 'REPORTES') {
			if (filtros.IdCliente && filtros.IdCliente.value)
				filters.push({ field: "IdCliente", operator: "eq", value: filtros.IdCliente.value });
			if (filtros.IdPortafolio)
				filters.push({ field: "IdPortafolio", operator: "eq", value: filtros.IdPortafolio});
			if (filtros.tipoOperacion)
				filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.tipoOperacion});
			if (filtros.fechaDesde)
				filters.push({ field: "FechaOperacionOrden", operator: "gte", value: util.dateOdata(filtros.fechaDesde)});
			if (filtros.fechaHasta)
				filters.push({ field: "FechaOperacionOrden", operator: "lte", value: util.dateOdata(filtros.fechaHasta)});
			if (filtros.instrumentoSubyacente && filtros.instrumentoSubyacente.IdInstrumento){
				filters.push({ field: "IdInstrumentoSubyacente", operator:"eq", value: filtros.instrumentoSubyacente.IdInstrumento})
			}
			if (filtros.OrdenesPorAsociar){
				if(filtros.AccionOrden === 'VENTA')
					filters.push({ field: "CantidadDisponibleAsociar", operator: "gt", value: 0});
				else
					filters.push({ field: "MontoDisponibleAsociar", operator: "gt", value: 0});
				
				filters.push({ field: "EsConfirmada", operator: "eq", value: 'N'});
			}
		}
		
		
		
		
		return filters;
	}
	
	/*------------------------------------------------------------------------------------------*/
	/*--------------------------- Consultas de ordenes------------------------------------------*/
	/*------------------------------------------------------------------------------------------*/
	obj.getConfiguracionInformacionEnvioFacturacionMasivo = function(apiRef,datos) {
		var a = {
			gridCreated : function(e) {
				apiRef = e;
				//apiRef.dataSource.data(datos);
			},
			autoBind: true,
			height : 300,
			pageable: true,
			reorderable: true,
			resizable: true,
			selectable: "multiple, row",
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					data : datos,
					schema : {
						model : {
							fields : {	
								IdTransaccion 						: {type : "number"},
								IdOrden								: {type : "number"},
								IdAsignacion 						: {type : "number"},
								FolioTransaccion 					: {type : "string"},
								CantidadAsignada 					: {type : "number"},
								ValorRescateAsignado 				: {type : "number"},
								IdClasificacionTipoInstrumento 		: {type : "string"},
								IdTipoInstrumento 					: {type : "string"},
								AccionOrden			 				: {type : "string"},
								IdBolsa 							: {type : "string"},
								IdMonedaInstrumento					: {type : "string"},
								IdMonedaOperacion					: {type : "string"},
								NombreLibroOrdenes					: {type : "string"},
								Instrumento							: {type : "string"},
								EsAPlazoUI							: {type : "string"},
								IdAsignacionMasiva                  : {type : "number"} 								
							}
						}
					}
				}
				
			}),
			columns : [
				getConfiguracionColumna("IdTransaccion"),
				getConfiguracionColumna("IdOrden"),
				getConfiguracionColumna("IdAsignacion"),
				getConfiguracionColumna("FolioTransaccion"),
				getConfiguracionColumna("CantidadAsignada"),
				getConfiguracionColumna("ValorRescateAsignado"),
				getConfiguracionColumna("IdClasificacionTipoInstrumento"),
				getConfiguracionColumna("IdTipoInstrumento"),
				getConfiguracionColumna("AccionOrden"),
				getConfiguracionColumna("IdBolsa"),
				
				getConfiguracionColumna("IdMonedaOperacion"),
				getConfiguracionColumna("NombreLibroOrdenes"),
				getConfiguracionColumna("Instrumento"),
				getConfiguracionColumna("EsAPlazoUI"),
				getConfiguracionColumna("IdAsignacionMasiva")
			]
		};
		return a;
		
	}
	
	obj.getDataSourceLibroPorDefecto = function() {
		return {
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarLibroPorDefecto'),
						dataType: "json"
			    	}
				},
				schema: {
					model: {
						fields: {
							IdLibroOrdenes	: { type: "number"},
							IdEmpresa		: { type: "number"}
						}
					}
				}
			}
		};
	}
	
	
	obj.getDataSourceConsultarOrdenesPorNegociar = function() {
		return {
			
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdOrden", dir: "desc" },
				pageSize : 200,
			    transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarOrdenesPorNegociar'),
						dataType: "json"
					}
				},
				schema : obj.getSchemaOrden()
			}
		};
	}
	
	obj.getDataSourceConsultarGarantias = function() {
		return {
			
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdOperacionOrigenGarantia"/*"IdGarantia"*/, dir: "desc" },
				pageSize : 200,
			    transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarGarantias'),
						dataType: "json"
					}
				},
				schema : obj.getSchemaGarantia()
			}
		};
	}
	
	obj.getConfiguracionConsultarOrdenesEnNegociacion = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltrosV2(filtros,'ORDEN'));
			},
			
			options : {
				allowCopy: true,
				autoBind: false,
	      		filterable: true,
				groupable: true,
				height:300,
				pageable: true,
				reorderable: true,
				resizable: true,
	      		rowColorBackground : function(row) {
			  		switch(row.get("AccionOrden")) {
			            case 'VENTA':
			                return "red";
			                break;
			            case 'COMPRA':
			                return "green";
			                break;
			        }
			  	},
			  	scrollable: true,
	      		selectable: "multiple, row",
				sortable: true,
				userSettings:true,
				navigatable: true,
				
				dataSource: new bcs.newDataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: { field: "IdOrden", dir: "desc" },
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasOrdenes('ConsultarOrdenesEnNegociacion'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaOrden()
					},
		  		}),
	  			columns: [
  			          getConfiguracionColumna("IdOrden")
					, getConfiguracionColumna("FechaOperacionOrden")
					, getConfiguracionColumna("PrecioOrden")
					, getConfiguracionColumna("CantidadOrden")
					, getConfiguracionColumna("CantidadDisponibleOrden")
					, getConfiguracionColumna("InstrumentoUI")
					, getConfiguracionColumna("EsVentaCortaUI")
					, getConfiguracionColumna("NombreTipoOperacion")
					
										
					, getConfiguracionColumna("CodigoCondicionDeLiquidacion")
					                          
					
					, getConfiguracionColumna("MontoOrden")
					, getConfiguracionColumna("IdMonedaOrden")
					, getConfiguracionColumna("DocumentoIdentidad")
					, getConfiguracionColumna("NombreCompleto")
					, getConfiguracionColumna("CodigoPortafolio")
					, getConfiguracionColumna("EstadoOrden")
					, getConfiguracionColumna("FechaVigencia")
					, getConfiguracionColumna("NombreFormaCustodia")
					, getConfiguracionColumna("CodigoEmpleadoPortafolio")
					, getConfiguracionColumna("CodigoEmpleadoDigitador")
					, getConfiguracionColumna("TipoPrecio")
					, getConfiguracionColumna("NombreCanal")
					, getConfiguracionColumna("IdBolsa")
					, getConfiguracionColumna("NombreLibroOrdenes")
			 		, getConfiguracionColumna("EstadoOrdenAsignacion")
					, getConfiguracionColumna("NombreTipoInstrumento")
					, getConfiguracionColumna("NombreClasificacionTipoInstrumento")
					, getConfiguracionColumna("CantidadAsignadaOrden")
					
					, getConfiguracionColumna("PrecioStrike")
					, getConfiguracionColumna("TipoOpcion")
					, getConfiguracionColumna("EstiloOpcion")
					, getConfiguracionColumna("ModalidadLiquidacion")
					, getConfiguracionColumna("ModalidadEjercicio")
					, getConfiguracionColumna("FechaVencimiento")
					, getConfiguracionColumna("Tamano")
					, getConfiguracionColumna("EnAnulacion")
					, getConfiguracionColumna("InstrumentoNegociacion")
					, getConfiguracionColumna("IdMonedaNegociacion")
					, getConfiguracionColumna("IdCondicionDeLiquidacion")
					
				],
			}
		};
		return config;
	};
	
	obj.getDataSourceConsultarLibroOrdenes = function() {
		return {
			type: 'odata',
  			definition : {
  				serverFiltering: true,
            	serverPaging: false,
            	filter : [
			        { field: "IdEmpresa",  		operator: "eq", value: $rootScope.SesionIdEmpresa}
			    ],
            	serverSorting: true,
            	sort : { field : "NombreLibroOrdenes",dir : "asc" },
           	 	transport: {
                	read: {
                		url:bcsConsultasOrdenes('ConsultarLibroOrdenes'),
                		dataType: "json"
                	}
            	},
            	schema: {
	        		model: {
    	        		fields: {
    	        			IdLibroOrdenes					: { type: "number"},
    	        			CodigoOperador					: { type: "string"},
    	        			NombreLibroOrdenes				: { type: "string"},
    	        			NombreTipoAsignacion			: { type: "code"},
    	        			NombreTipoLibroOrden			: { type: "string"},
    	        			IdTipoLibroOrden				: { type: "string"},
    	        			IdEstado						: { type: "string"},
    	        			NombreEstado					: { type: "string"},
    	        			AsignacionAutomatica			: { type: "string"},
    	        			AsignacionAutomaticaTranslate	: { type: "code"},
    	        			LibroPorDefectoTranslate		: { type: "code"},
    	        			IdTipoAsignacion				: { type: "string"},
    	        			Prioridad						: { type: "number"},
    	        			LibroPorDefecto					: { type: "string"},
    	        			
    	        			IdInstrumento					: { type: "number"},
    	        			Nemotecnico						: { type: "string"},
    	        			IdClienteOfertante				: { type: "number"},
    	        			NombreClienteOfertante			: { type: "string"},
    	        			Cantidad						: { type: "number"},
    	        			IdPlantillaCobros				: { type: "number"},
    	        			FechaApertura					: { type: "date"},
    	        			FechaCierre						: { type: "date"}
	        			}
	        		}
	        	}
  			}
		};
	}
	
	obj.getDataSourceConsultarOrdenesPorAsignar = function() {
		return {
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdOrden", dir: "desc" },
				pageSize : 200,
			    transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarOrdenesPorAsignar'),
						dataType: "json"
					}
				},
				schema : obj.getSchemaOrden()
			},
  		}
	}
	
	obj.getConfiguracionSimulacionOrdenes = function() {
		return	{
			sortable: true,
      		reorderable: true,
      		filterable: false,
			groupable: false,
			rowColorBackground : function(row) {
				if (row.get("IdTipoOperacion") === 'COMSIM' && row.get("CantidadSimulacionOrden") > 0) {
					return "green";
				}
		        if (row.get("CantidadSimulacionOrden") > row.get("CantidadDisponibleOrden")) {
		        	return "red";
		        }
		        if (row.get("CantidadSimulacionOrden") == row.get("CantidadDisponibleOrden")) {
		        	return "green";
		        }
		        if (row.get("CantidadSimulacionOrden") < row.get("CantidadDisponibleOrden")) {
		        	return "yellow";
		        }
		  	},
			pageable : {
				numeric: false,
			    previousNext: false,
			    messages: {
			    	display: "Total: {2}"
			    }
			},
			selectable: "multiple, row",
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: true,
      		height:220,
      		userSettings:true,
			navigatable: true,
		
			dataSource: new bcs.DataSource ({
	  			type : "array",
	  			definition : {
					data : [],
					schema : obj.getSchemaOrden()
				},
	  		}),
  			columns: [
  			          getConfiguracionColumna("IdOrden")
					, getConfiguracionColumna("FechaOperacionOrden")
					, getConfiguracionColumna("PrecioOrden")
					, getConfiguracionColumna("CantidadDisponibleOrden")
					, getConfiguracionColumna("CantidadSimulacionOrden")
					, getConfiguracionColumna("CantidadAsignadaOrden")
					, getConfiguracionColumna("CantidadOrden")
					, getConfiguracionColumna("InstrumentoUI")
					, getConfiguracionColumna("EsVentaCortaUI")
					, getConfiguracionColumna("NombreTipoOperacion")
					, getConfiguracionColumna("NombreCondicionDeLiquidacion")
					, getConfiguracionColumna("MontoOrden")
					, getConfiguracionColumna("IdMonedaOrden")
					, getConfiguracionColumna("DocumentoIdentidad")
					, getConfiguracionColumna("NombreCompleto")
					, getConfiguracionColumna("CodigoPortafolio")
					, getConfiguracionColumna("EstadoOrden")
					, getConfiguracionColumna("TipoPrecio")
					, getConfiguracionColumna("NombreCanal")
					, getConfiguracionColumna("IdBolsa")
					
					, getConfiguracionColumna("PrecioStrike")
					, getConfiguracionColumna("TipoOpcion")
					, getConfiguracionColumna("EstiloOpcion")
					, getConfiguracionColumna("ModalidadLiquidacion")
					, getConfiguracionColumna("ModalidadEjercicio")
					, getConfiguracionColumna("FechaVencimiento")
					, getConfiguracionColumna("Tamano")
					, getConfiguracionColumna("InstrumentoNegociacionOrden")

			],
			
		};
	};
	
	
	/*------------------------------------------------------------------------------------------*/
	/*--------------------------- Consultas de transacciones------------------------------------*/
	/*------------------------------------------------------------------------------------------*/
	obj.getDataSourceConsultarTransacciones = function() {
		return {
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true,
				pageSize: 200,
				sort: { field: "FechaOperacionTransaccion", dir: "desc" },
				transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarTransaccionesUI'),
						dataType: "json"
					}
				},
				schema : this.getSchemaTransaccion()
			},
  		}
	}
	
	obj.getDataSourceConsultarTransaccionesAnuMod = function() {
		return {
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true,
				pageSize: 200,
				sort: { field: "FechaOperacionTransaccion", dir: "desc" },
				transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarTransaccionesAnuModUI'),
						dataType: "json"
					}
				},
				schema : this.getSchemaTransaccion()
			},
  		}
	}
	
	obj.getDataSourceConsultarTransaccionesHistorico = function() {
		return {
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true,
				pageSize: 200,
				sort: [{field:"FechaOperacionTransaccion ", dir: "desc"},{ field: "IdTransaccionHistorico", dir: "desc" }],
				transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarTransaccionesHistorico'),
						dataType: "json"
					}
				},
				schema : this.getSchemaTransaccionHistorico()
			},
  		}
	}
	
	obj.getConfiguracionSelectorTransacciones = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltrosV2(filtros,'TRANSACCION'));
			},
			
			options : {
				allowCopy: true,
				autoBind: false,
	      		filterable: true,
				groupable: true,
				height:400,
				pageable: true,
	      		reorderable: true,
				resizable: true,
				scrollable: true,
	      		selectable: "multiple, row",
				sortable: true,
				userSettings:true,
				navigatable: true,
				
				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true,
						pageSize: 200,
						sort: { field: "IdTransaccion", dir: "desc" },
						transport: {
							read: {
								url:bcsConsultasOrdenes('ConsultarTransaccionesPorAsignar'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaTransaccion()
					},
		  		}),
	  			columns: [
	  			        getConfiguracionColumna("FolioTransaccion")
			    		,getConfiguracionColumna("FechaOperacionTransaccion")
			    		,getConfiguracionColumna("IdClasificacionTipoInstrumento")
			    		,getConfiguracionColumna("NombreTipoOperacion")
			    		,getConfiguracionColumna("InstrumentoUI")
			    		,getConfiguracionColumna("CantidadTransaccion")
			    		,getConfiguracionColumna("CantidadDisponibleTransaccion")
			    		,getConfiguracionColumna("PrecioTransaccion")
						,getConfiguracionColumna("IdMonedaTransaccion")
						,getConfiguracionColumna("Tasa")
						,getConfiguracionColumna("CodigoCondicionDeLiquidacion")
						,getConfiguracionColumna("IdEmisor")
						,getConfiguracionColumna("FechaVencimiento")
						,getConfiguracionColumna("IndicadorOrdenDirecta")
						,getConfiguracionColumna("IdTipoInstrumento")
						,getConfiguracionColumna("NemotecnicoSVS")
						,getConfiguracionColumna("EsFungibleUI")
			    		,getConfiguracionColumna("ValorRescateDisponibleTransaccion")
			    		,getConfiguracionColumna("ValorRescateTransaccion")
						,getConfiguracionColumna("IdBolsa")
						,getConfiguracionColumna("FechaLiquidacion")
						,getConfiguracionColumna("IdTipoReajuste")
						,getConfiguracionColumna("CodigoOperadorCorredor")
						,getConfiguracionColumna("CodigoOperadorContraparte")
			    		,getConfiguracionColumna("FechaEmision")
			    		,getConfiguracionColumna("MarcaNegociacion")
			    		,getConfiguracionColumna("EsCuentaPropia")
			    		,getConfiguracionColumna("EsAPlazoUI")
			    		,getConfiguracionColumna("EsVentaCortaUI")
			    		,getConfiguracionColumna("FechaModificacionOperacion")
			    		,getConfiguracionColumna("CantidadAsignadaTransaccion")
			    		,getConfiguracionColumna("MontoAsignadoTransaccion")
						,getConfiguracionColumna("MontoTransaccion")
						,getConfiguracionColumna("NombreLibroOrdenes")
						,getConfiguracionColumna("NombreTipoInstrumento")
			    		,getConfiguracionColumna("EstadoTransaccion")
			    		,getConfiguracionColumna("NombreCorredorContraparte")
						,getConfiguracionColumna("InstrumentoNegociacionTran")
				],
				
			}
		};
		return config;
	};

	obj.getConfiguracionSimulacionTransacciones = function() {
		return {
			sortable: true,
			userSettings:true,
			navigatable: true,
      		reorderable: true,
      		filterable: false,
			groupable: false,
			rowColorBackground : function(row) {
		        if (row.get("IdClasificacionTipoInstrumento") === 'IF') {
		        	 if (row.get("ValorRescateSimulacionTransaccion") > row.get("ValorRescateDisponibleTransaccion")) {
			        	return "red";
			        }
			        if (row.get("ValorRescateSimulacionTransaccion") == row.get("ValorRescateDisponibleTransaccion")) {
			        	return "green";
			        }
			        if (row.get("ValorRescateSimulacionTransaccion") < row.get("ValorRescateDisponibleTransaccion")) {
			        	return "yellow";
			        }		        	
		        }
		        else {
		        	if (row.get("CantidadSimulacionTransaccion") > row.get("CantidadDisponibleTransaccion")) {
			        	return "red";
			        }
			        if (row.get("CantidadSimulacionTransaccion") == row.get("CantidadDisponibleTransaccion")) {
			        	return "green";
			        }
			        if (row.get("CantidadSimulacionTransaccion") < row.get("CantidadDisponibleTransaccion")) {
			        	return "yellow";
			        }
		        }
		  	},
			selectable: "multiple,row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: true,
      		height:220,

			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : obj.getSchemaTransaccion()
					
	  			}
			}),
	  		columns: [
  			        getConfiguracionColumna("FolioTransaccion")
		    		,getConfiguracionColumna("FechaOperacionTransaccion")
		    		,getConfiguracionColumna("PrecioTransaccion")
					,getConfiguracionColumna("CantidadDisponibleTransaccion")
					,getConfiguracionColumna("CantidadSimulacionTransaccion")
					,getConfiguracionColumna("InstrumentoUI")
		    		,getConfiguracionColumna("NombreTipoOperacion")
					,getConfiguracionColumna("NombreCondicionDeLiquidacion")
					,getConfiguracionColumna("IdBolsa")
					,getConfiguracionColumna("IdMonedaTransaccion")
					,getConfiguracionColumna("CodigoOperadorCorredor")
					,getConfiguracionColumna("FechaVencimiento")
					,getConfiguracionColumna("MarcaNegociacion")
		    		,getConfiguracionColumna("IdEmisor")
		    		,getConfiguracionColumna("Tasa")
		    		,getConfiguracionColumna("IdTipoReajuste")
		    		,getConfiguracionColumna("NemotecnicoSVS")
		    		,getConfiguracionColumna("EsFungibleUI")
		    		,getConfiguracionColumna("IndicadorOrdenDirecta")
		    		,getConfiguracionColumna("EsVentaCortaUI")
		    		,getConfiguracionColumna("CantidadTransaccion")
		    		,getConfiguracionColumna("CantidadAsignadaTransaccion")
		    		,getConfiguracionColumna("MontoTransaccion")
		    		,getConfiguracionColumna("NombreTipoInstrumento")
		    		,getConfiguracionColumna("NombreClasificacionTipoInstrumento")
		    		//,getConfiguracionColumna("ValorRescateTransaccion")
		    		,getConfiguracionColumna("NombreCorredorContraparte")
		    		
		    		,getConfiguracionColumna("PrecioStrike")
					,getConfiguracionColumna("TipoOpcion")
					,getConfiguracionColumna("EstiloOpcion")
					,getConfiguracionColumna("ModalidadLiquidacion")
					,getConfiguracionColumna("ModalidadEjercicio")
					,getConfiguracionColumna("FechaVencimiento")
					,getConfiguracionColumna("Tamano")
					,getConfiguracionColumna("InstrumentoNegociacionTran")

			]
		};
	};
	
	obj.getConfiguracionSimulacionComprobantes = function() {
		return {
			sortable: true,
      		reorderable: true,
      		filterable: false,
			groupable: false,
			selectable: "multiple,row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: {
      			virtual: true
      		},
      		height:220,
      		userSettings:true,
			navigatable: true,

			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : obj.getSchemaComprobante()
				
	  			}
			}),
	  		columns: [
	  		        getConfiguracionColumna("FechaIngresoRegistro")
					,getConfiguracionColumna("FechaLiquidacion")
					,getConfiguracionColumna("IdComprobante")
					,getConfiguracionColumna("IdTipoComprobante")
					,getConfiguracionColumna("IdEstadoComprobante")
					,getConfiguracionColumna("CodigoCondicionDeLiquidacion")
					,getConfiguracionColumna("MontoCobroComprobante")
					,getConfiguracionColumna("MontoComprobante")
					,getConfiguracionColumna("IdMonedaLiquidacion")
					
			]
		};
	};
	
	/* GRILLA DE DATOS DE ENTRADA INTERFAZ DE ASIGNACION*/
	obj.getConfiguracionSimulacionAsignacion = function() {
		return {
			sortable: true,
      		reorderable: true,
      		filterable: false,
			groupable: false,
			
			pageable : {
				numeric: false,
			    previousNext: false,
			    messages: {
			    	display: "Total: {2}"
			    }
			},
			selectable: "multiple,row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: {
      			virtual: true
      		},
      		height:220,
      		userSettings:true,
			navigatable: true,

			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : obj.getSchemaAsignacion(),
					aggregate: [
			            { field: "CantidadAsignada", aggregate: "sum" },
			            { field: "MontoAsignado", aggregate: "sum" }
			            ]
	  			}
			}),
	  		columns: [
	  		        getConfiguracionColumna("FechaAsignacion")
	  		        ,getConfiguracionColumna("IdAsignacion")
					,getConfiguracionColumna("IdOrden")
					,getConfiguracionColumna("FolioTransaccion")
					,getConfiguracionColumna("CodigoPortafolio")
					,getConfiguracionColumna("FechaOperacionTransaccion")
					,getConfiguracionColumna("IdClasificacionTipoInstrumento")
					,getConfiguracionColumna("NombreTipoOperacion")
					,getConfiguracionColumna("InstrumentoUI")
					,getConfiguracionColumna("PrecioAsignacion")
				 	,getConfiguracionColumna("CantidadAsignada")
				 	,getConfiguracionColumna("MontoAsignado")
				 	,getConfiguracionColumna("Tasa")
				 	//,getConfiguracionColumna("Plazo")
				 	,getConfiguracionColumna("FechaVencimiento")
				 	//,getConfiguracionColumna("CodigoCondicionDeLiquidacion")
				 	//,getConfiguracionColumna("ValorRescateAsignado")
				 	,getConfiguracionColumna("EstadoAsignacion")
				 	//,getConfiguracionColumna("IdTipoReajuste")
				 	,getConfiguracionColumna("IdMonedaOperacion")				 	
				 	,getConfiguracionColumna("IdTipoInstrumento")
					,getConfiguracionColumna("NombreLibroOrdenes")
				 	,getConfiguracionColumna("MontoAsignadoPlazo")
					,getConfiguracionColumna("FechaOperacionOrden")
					,getConfiguracionColumna("InstrumentoNegociacionTran")

				 	//,getConfiguracionColumna("DocumentoIdentidad")
				 	//,getConfiguracionColumna("NombreCompleto")
				 	
					
			]
		};
	};
	
	
	/*------------------------------------------------------------------------------------------*/
	/*--------------------------- Consultas de asignaciones-------------------------------------*/
	/*------------------------------------------------------------------------------------------*/	
	/* ASIGNACIONES GENERADAS POR LA SIMULACION*/
	obj.getConfiguracionSimulacionAsignaciones = function() {
		return {
			sortable: true,
	  		reorderable: true,
	  		filterable: false,
			groupable: false,
			pageable : {
				numeric: false,
			    previousNext: false,
			    messages: {
			    	display: "Total: {2}"
			    }
			},
			selectable: "multiple,row",
			resizable: true,
	  		autoBind: true,
	  		scrollable: false,
	  		height:250,
	  		userSettings:true,
			navigatable: true,
			
			dataSource: new bcs.DataSource({ 
				type : "array",
				definition : {
					data : [],
					aggregate: [
			            { field: "CantidadAsignadaSimulacion", aggregate: "sum" },
					],
					schema : obj.getSchemaAsignacion()
				},
				
			}),
		
			columns: [
				{field:"IdInterno",title: $translate.instant("UI_SECUENCIA"),width : "75px" },
				getConfiguracionColumna("FolioTransaccion", {width: 'auto'}),
				getConfiguracionColumna("FechaTransaccion", {width: 'auto'}),
		        getConfiguracionColumna("Instrumento", {width: 'auto'}),
			    getConfiguracionColumna("DocumentoIdentidad", {width: 'auto'}),
			    getConfiguracionColumna("NombreCompleto", {width: 'auto'}),
			    getConfiguracionColumna("CodigoPortafolio"),
			    getConfiguracionColumna("IdOrden", {width: 'auto'}),
			    getConfiguracionColumna("FolioCarteraAsignada", {width: 'auto'}),
			    getConfiguracionColumna("CantidadAsignadaSimulacion",{width: 'auto',editable:true , footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #"}),
			    getConfiguracionColumna("MontoAsignacion", {width: 'auto'}), //Regla 3: ((Monto Palo * Cantidad Asignada) / Cantidad Palo)
				getConfiguracionColumna("PrecioPromedio",{width: 'auto'}),
				getConfiguracionColumna("PrecioStrike",{width: 'auto'}),
				getConfiguracionColumna("TipoOpcion",{width: 'auto'}),
				getConfiguracionColumna("EstiloOpcion",{width: 'auto'}),
				getConfiguracionColumna("ModalidadLiquidacion",{width: 'auto'}),
				getConfiguracionColumna("ModalidadEjercicio",{width: 'auto'}),
				getConfiguracionColumna("FechaVencimiento",{width: 'auto'}),
				getConfiguracionColumna("Tamano",{width: 'auto'}),
				getConfiguracionColumna("InstrumentoNegociacionTran",{width: 'auto'})

			]
		};
	};
	
	//para la grilla de clientes en la interfaz de asignacion
	obj.getConfiguracionSimulacionClientesAsignaciones = function() {
		return {
			allowCopy: true,
			autoBind: false,
			height:220,
			pageable : {
				numeric: false,
			    previousNext: false,
			    messages: {
			    	display: "Total: {2}"
			    }
			},
			reorderable: true,
			resizable: true,
	  		selectable: "multiple,row",
			sortable: true,
			userSettings:true,
			navigatable: true,
	  		
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : {
						model: {
							fields : {
								NombreCompleto 					: { type: "string"},
								DocumentoIdentidad 				: { type: "string"},
								CodigoPortafolio 				: { type: "string"},
								IdMoneda 						: { type: "string" },
								CantidadSimulacionCliente 		: { type: "number"},
								//ValorRescateSimulacionCliente	: { type: "number"},
								PorcentajeSimulacion			: { type: "number"},

								DescripcionPortafolio			: { type: "string" },
								IdCliente						: { type: "number"},
								IdEstado						: { type: "string" },
								IdEstadoPortafolio				: { type: "string" },
								IdLibroOrdenes					: { type: "string" },
								IdPersona						: { type: "number"},
								IdPortafolio					: { type: "number"},
								Proposito						: { type: "string" },
								RazonSocial						: { type: "string" },
								TipoDocumentoIdentidad			: { type: "string" },


							}
						}
					}
				},
	  		}),
			columns: [
			            getConfiguracionColumna("NombreCompleto", {width: 'auto'}) 
			          , getConfiguracionColumna("DocumentoIdentidad", {width: 'auto'})
			          , getConfiguracionColumna("CodigoPortafolio", {width: 'auto'}) 
			          , getConfiguracionColumna("CantidadSimulacionCliente", {width: 'auto'})
			          , getConfiguracionColumna("PorcentajeSimulacion", {editable: true,width: 'auto'})
			          //, getConfiguracionColumna("ValorRescateSimulacionCliente", {width: 'auto'})
			          
					]
		};
	};
	
	obj.getDataSourceIndicadoresPortafolio = function() {
		return {
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarIndicadoresPortafolioOrdenes'),
						dataType: "json"
						}
				},
				schema: {
					model : {
						fields : {
							IdPortafolio : { type : "number" },
							OperaCustodiaRV : { type : "string" },
							OperaCustodiaRFIF : { type : "string" },
							OperaCuentaInversion : { type : "string" }
						}
					}
				}
			}
		}
	};
	
	obj.getDataSourceInformacionPortafolio = function() {
		return {
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarInformacionPortafolioOrdenes'),
						dataType: "json"
						}
				},
				schema: {
					model : {
						fields : {
							IdPortafolio : { type : "number" },
							IdPersonaBrokerExtranjero : { type : "number" },
							EsClienteFinal : { type : "string" },
							OperaCustodiaRV : { type : "string" },
							OperaCustodiaRFIF : { type : "string" },
							OperaCuentaInversion : { type : "string" }
						}
					}
				}
			}
		}
	};
	
	obj.getDataSourceGruposPortafolios = function() {
		return {
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: bcsConsultasOrdenes('ConsultarGrupo'),
						dataType: "json"
						}
				},
				schema: {
					model : {
						fields : {
							IdGrupo : { type : "number" },
							Codigo	: { type : "string" }
						}
					}
				}
			}
		}
	};
	
	//para el selector de clientes en la interfaz de asignacion
	obj.getDataSourcePortafoliosGrupo = function() { 
		return {
			type : "odata",
  			definition : {
  				serverFiltering : true, 
				serverPaging : true, 
				pageSize : 200,
				serverSorting : true, 
				transport: {
	    			read: {
	    				url: bcsConsultasOrdenes ('ConsultarClientesLibroOrdenesGrupo'),
	    				dataType: "json"
	    		    }                    

	    		},
				schema : {
					model: {
						fields : {
							NombreCompleto 			: { type: "string"},
							DocumentoIdentidad 		: { type: "string"},
							CodigoPortafolio 		: { type: "string"},
							Proposito 				: { type: "string"},
							IdEstadoPortafolio 		: { type: "string"},
							NombreGrupo 			: { type: "string"},
							Porcentaje 				: { type: "number"},
							
						}
					}
				}
			},
  		}
	};
	
	//para el selector de clientes en la interfaz de asignacion
	obj.getDataSourceDefClientes = function() { 
		return {
			type : "odata",
  			definition : {
  				serverFiltering : true, 
				serverPaging : true, 
				pageSize : 200,
				serverSorting : true, 
				transport: {
	    			read: {
	    				url: bcsConsultasOrdenes ('ClientesLibroOrdenes'),
	    				dataType: "json"
	    		    }                    

	    		},
				schema : {
					model: {
						fields : {
							NombreCompleto 			: { type: "string"},
							DocumentoIdentidad 		: { type: "string"},
							CodigoPortafolio 		: { type: "string"},
							Proposito 				: { type: "string"},
							IdEstadoPortafolio 		: { type: "string"},
						}
					}
				}
			},
  		}
	};
	
	obj.getConfiguracionSelectorClientes = function() {
		return {
			selectable: "multiple, row",
			reorderable: true,
			pageable : true,
			autoBind: false,
	  		height:250,
	  		userSettings:true,
			navigatable: true,
	
			dataSource: new bcs.DataSource(obj.getDataSourceDefClientes()),
			columns: [
			          {field: "NombreCompleto",title: $translate.instant('UI_CLIENTE'),					width: "100px",align: "left"},
			          {field: "DocumentoIdentidad",title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),	width: "100px",align: "left"},
			          {field: "CodigoPortafolio",title: $translate.instant('UI_PORTAFOLIO'),			width: "100px",align: "left"},
			          {field: "Proposito",title: $translate.instant('UI_PROPOSITO'),					width: "100px",align: "left"},
					]
		};
				
	};
	
	var gridSimulacionCarteras = null;
	obj.getConfiguracionSimulacionCarteras = function() {
		return {
			columns :
				[
				 	getConfiguracionColumna("FolioCartera")
				 	,getConfiguracionColumna("DocumentoIdentidad")
				 	,getConfiguracionColumna("NombreCompleto")
				 	,getConfiguracionColumna("CodigoPortafolio")
				 	,getConfiguracionColumna("CantidadDisponibleCartera")
				 	,getConfiguracionColumna("CantidadSimulacionCartera",{editable:true})
				 	,getConfiguracionColumna("CorteMinimo")
				],
			sortable : true,
			userSettings:true,
			navigatable: true,
			pageable : {
				numeric: false,
			    previousNext: false,
			    messages: {
			    	display: "Total: {2}"
			    }
			},
			save:function(e) {
				if (gridSimulacionCarteras != null) {
					gridSimulacionCarteras.refresh();
				}
			},
			gridCreated:function(g) {
				gridSimulacionCarteras = g;
			},
			rowColorBackground : function(row) {
				if (row.CantidadSimulacionCartera === 0) {
					return null;
				}
				if (row.CantidadSimulacionCartera > row.CantidadDisponible) {
					return "red";
				}
				if (row.CantidadSimulacionCartera == row.CantidadDisponible) {
					return "green";
				}
				if (row.CantidadSimulacionCartera < row.CantidadDisponible) {
					return "yellow";
				}
			},
			filterable			: false,
			height				: 220,
			selectable 			: "multiple,row",
			autoBind			: false,
			editable: true,
		 	dataSource : new bcs.DataSource({
		 		type 		: "array",
				definition 	: {
					data : [],
					schema : {
						model: {
							fields : {
								DocumentoIdentidad 				: { type: "string"},
								NombreCompleto 					: { type: "string"},
								IdCliente						: { type: "number"},
								IdPortafolio					: { type: "number"},
								CodigoPortafolio 				: { type: "string"},
								IdInstrumento					: { type: "number"},
								FolioCartera					: { type: "number"},
								Instrumento 					: { type: "string"},
								IdTipoInstrumento 				: { type: "string"},
								IdClasificacionTipoInstrumento 	: { type: "string"},
								TasaCompra						: { type: "number"},
								TasaMercado						: { type: "number"},
								CantidadDisponible				: { type: "number"},
								CantidadSimulacionCartera		: { type: "number"},
								IdMoneda 						: { type: "string"},
								FechaLiquidacion 				: { type: "sysdate"},
								IdEmisor 						: { type: "string"},
								FechaVencimiento 				: { type: "sysdate"},
								NemoSVS 						: { type: "string"},
								IdTipoReajuste 					: { type: "string"},
								EsFungible 						: { type: "string"},
								CorteMinimo						: { type: "number"}
							}
						}	 
					},
				}
		 	}),
		};
	};
	
	/**/
	obj.getConfiguracionGridCustodiaSimultaneable = function() {
		return {
			columns :
				[
				 	 getConfiguracionColumna("Instrumento")
				 	,getConfiguracionColumna("IdTipoInstrumento")
				 	,getConfiguracionColumna("CantidadSimulacionSimultanea",{editable:true})
				 	,getConfiguracionColumna("CantidadDisponibleSubyacente")
				 	,getConfiguracionColumna("PrecioCustodiaSimultanea")
				 	,getConfiguracionColumna("PrecioVaR")
				 	,getConfiguracionColumna("PorcentajeVaR")
				 	,getConfiguracionColumna("MontoSimulacionSimultanea",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("MontoAVenderSimulado",{editable:true})
				 	,getConfiguracionColumna("MontoDisponibleSubyacente")
				 	,getConfiguracionColumna("MontoPlazo")
				 	,getConfiguracionColumna("MontoACubrir")
				 	,getConfiguracionColumna("Lista")
				 	,getConfiguracionColumna("MontoExigibilidadCorr",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("PorcExigibilidadCorr")
				 	,getConfiguracionColumna("MontoExigibilidadEnt",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("PorcExigibilidadEnt")
				 	,getConfiguracionColumna("MontoCobro")
				],
				editable: true,
				selectable: "row",
				autoBind: true,
				groupable : false,
		  		scrollable: true,
		 		height:300,
		 		userSettings:true,
				navigatable: true,
		 		 		
		 		dataSource : new bcs.DataSource({
		 			type : "array",
		 			definition : {
		 				data : [],
						schema: {
							model: {
			                    fields: {
			                    	IdInterno                           : { type: "number"},
		                       		Instrumento							: { type: "string" },
			                        IdTipoInstrumento					: { type: "string" },
			                        TipoInstrumento						: { type: "string" },
			                        CantidadSimulacionSimultanea		: { type: "number", validation : { required: true, min: 0 } },
			                        CantidadDisponibleSubyacente		: { type: "number"},
			                        MontoSimulacionSimultanea			: { type: "number"},
			                        MontoAVenderSimulado		        : { type: "number", validation : { required: true, min: 0 } },
			                        MontoDisponibleSubyacente			: { type: "number"},
			                        Precio								: { type: "number"},
			                        ValorClasificacionRiesgo			: { type: "string"},
			                        Tasa								: { type: "number"},
			                        Plazo								: { type: "number"},
			                        MontoPlazo							: { type: "number"},
			                        MontoExigibilidadCorr				: { type: "number"},
			                        PorcExigibilidadCorr				: { type: "number"},
			                        MontoExigibilidadEnt				: { type: "number"},
			                        PorcExigibilidadEnt					: { type: "number"},
			                     	CantidadOriginalSubyacente			: { type: "number"},
			                     	IdInstrumento						: { type: "number"},
			                     	IdMonedaInstrumento					: { type: "string"},
			                     	MontoCobro							: { type: "number"},			             
			                     	PrecioVaR 							: { type: "number"},
			                     	PorcentajeVaR						: { type: "number"},
			                     	MontoACubrir						: { type: "number"},
		                        }
		                   }
						},
						aggregate: [
				            { field: "MontoSimulacionSimultanea", aggregate: "sum" },
				            { field: "MontoExigibilidadCorr", aggregate: "sum" },
				            { field: "MontoExigibilidadEnt", aggregate: "sum" }
				            ]
				            
			 			}
		 		})
			};
	};
	
	obj.getConfiguracionGridCustodiaGarantizable = function() {
		return {
			columns :
				[
				 	getConfiguracionColumna("Instrumento")
				 	,getConfiguracionColumna("IdTipoInstrumento")
				 	,getConfiguracionColumna("IdMonedaInstrumento")
				 	,getConfiguracionColumna("CantidadSimulacionGarantiaCustodia",{editable:true})
				 	,getConfiguracionColumna("CantidadDisponibleGarantiaCustodia")				 	
				 	,getConfiguracionColumna("PrecioGarantiaCustodia")
				 	,getConfiguracionColumna("PrecioVaR")
				 	,getConfiguracionColumna("PorcentajeVaR")
				 	,getConfiguracionColumna("Lista")
				 	,getConfiguracionColumna("MontoCoberturaSimular",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #",editable:true}) //GUz
				 	,getConfiguracionColumna("MontoSimulacionGarantiaCustodia",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("MontoDisponibleGarantiaCustodia")
				 	,getConfiguracionColumna("MontoCoberturaEntidadCustodia",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("PorcentajeCoberturaEntidad")
				 	,getConfiguracionColumna("MontoCoberturaCorredoraCustodia",{footerTemplate:" #:  (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #"})
				 	,getConfiguracionColumna("PorcentajeCoberturaCorredora")
				],
				sortable: false,
		  		reorderable: false,
		  		allowCopy: true,
		  		filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				resizable: true,
		  		autoBind: true,
		  		scrollable: true,
		  		height:200,
		 		userSettings:true,
				navigatable: true,
				
		 		dataSource : new bcs.DataSource({
		 			type : "array",
		 			definition : {
		 				data : [],		 				
						schema: {
							model: {
			                    fields: {	                        
			                        IdInstrumento								: { type: "number" },
									Instrumento									: { type: "string" },
									IdTipoInstrumento							: { type: "string" },
									IdClasificacionTipoInstrumento				: { type: "string" },
									CantidadDisponibleGarantiaCustodia			: { type: "number" },
									MontoDisponibleGarantiaCustodia				: { type: "number" },
									PrecioGarantiaCustodia						: { type: "number" },
			                     	PrecioVaR 									: { type: "number" },
			                     	PorcentajeVaR								: { type: "number" },									
									MontoSimulacionGarantiaCustodia				: { type: "number" },
									CantidadSimulacionGarantiaCustodia			: { type: "number" , validation : { required: true, min: 0 } },
									IdMonedaInstrumento							: { type: "string" },
									FechaLiquidacion							: { type: "date"   },
									IdEmisor									: { type: "string" },
									ValorClasificacionRiesgo					: { type: "string" },
									FechaEmisionInstrumento						: { type: "date" },
				  					FechaVencimiento							: { type: "date" },
				  					NemoSVS										: { type: "string" },
				  					IdTipoReajuste								: { type: "string" },
				  					PlazoEnDias									: { type: "number" },
				  					EsFungible									: { type: "string" },
				  					ClasificacionRiesgo							: { type: "string" },				  					
				  					MontoCoberturaEntidad						: { type: "number" },
		   							MontoCoberturaCorredora						: { type: "number" },
		   							MontoCoberturaSimular						: { type: "number" },		                        
			                        CantidadOriginalGarantiaCustodia			: { type: "number" },
									PorcentajeCoberturaCorredora				: { type: "number" },
									PorcentajeCoberturaEntidad					: { type: "number" },
									IdMonedaSistema								: { type: "string" },

									
									

		                        }
	                   		}
						},
						aggregate: [
				            { field: "MontoSimulacionGarantiaCustodia", aggregate: "sum" },
				            { field: "MontoCoberturaEntidad", aggregate: "sum" },
				            { field: "MontoCoberturaCorredora", aggregate: "sum" },
				            { field: "MontoCoberturaSimular",aggregate: "sum"}, //Guz
				            ]
			 		}
		 			})
		 	};
	};
	
	obj.getConfiguracionGridCtaInvGarantizable = function() {
		return {
	
			columns :
				[
				 	getConfiguracionColumna("IdMoneda"),
				 	getConfiguracionColumna("Saldo"),
				 	getConfiguracionColumna("MontoSimulacionGarantiaCtaInv",{ editable : true}),
				 	getConfiguracionColumna("MontoCoberturaEntidadCtaInv"),
				 	getConfiguracionColumna("PorcentajeCoberturaEntidad"),
				 	getConfiguracionColumna("MontoCoberturaCorredoraCtaInv"),
				 	getConfiguracionColumna("PorcentajeCoberturaCorredora")
				],
				sortable: false,
		  		reorderable: false,
		  		allowCopy: true,
		  		filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				resizable: true,
		  		autoBind: true,
		  		scrollable: true,
		  		height:300,
		 		userSettings:true,
				navigatable: true,
				
		 		dataSource : new bcs.DataSource({
		 			type : "array",
		 			definition : {
		 				data : [],
		 				schema: {
							model: {
			                    fields: {
		                       		IdMoneda: { type: "string"},
			                        Saldo: { type: "number" },
			                        MontoSimulacionGarantiaCtaInv: { type: "number" },
			                        MontoCoberturaEntidad: { type: "number" },
			                        PorcentajeCoberturaEntidad: { type: "number" },
			                        MontoCoberturaCorredora: { type: "number" },
			                        PorcentajeCoberturaCorredora: { type: "number" },
		                        }
		                   }
						}
		 			}
			 	})
		 	};
	};
	
	//------------------------DESBLOQUEO TRANSACCIONES-----------------------------------
	obj.getConfiguracionConsultarTransaccionesBloqueadas = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltrosV2(filtros,'TRANSACCION'));
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		userSettings:true,
				navigatable: true,
				excel : {
					allPages: true,
				},				
	      		pageable: true,
	      		height:400,
	
				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true,
						pageSize: 50,
						sort: { field: "IdTransaccion", dir: "desc" },
						transport: {
							read: {
								url:bcsConsultasOrdenes('ConsultarTransaccionesBloqueadas'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaTransaccion()
					},
		  		}),
	  			columns: [
	  			        getConfiguracionColumna("FolioTransaccion")
			    		,getConfiguracionColumna("FechaOperacionTransaccion")
			    		,getConfiguracionColumna("PrecioTransaccion")
						,getConfiguracionColumna("CantidadDisponibleTransaccion")
						,getConfiguracionColumna("ValorRescateDisponibleTransaccion")
						,getConfiguracionColumna("InstrumentoUI")
			    		,getConfiguracionColumna("NombreTipoOperacion")
						,getConfiguracionColumna("NombreCondicionDeLiquidacion")
						,getConfiguracionColumna("IdBolsa")
						,getConfiguracionColumna("FechaLiquidacion")
						,getConfiguracionColumna("IdMonedaInstrumento")
						,getConfiguracionColumna("CodigoOperadorCorredor")
						,getConfiguracionColumna("CodigoOperadorContraparte")
			    		,getConfiguracionColumna("FechaEmision")
			    		,getConfiguracionColumna("FechaVencimiento")
			    		,getConfiguracionColumna("IndicadorOrdenDirecta")
			    		,getConfiguracionColumna("EsCuentaPropia")
			    		,getConfiguracionColumna("EsAPlazoUI")
			    		,getConfiguracionColumna("EsVentaCortaUI")
			    		//,getConfiguracionColumna("IdEmpleadoReserva")
			    		,getConfiguracionColumna("FechaModificacionOperacion")
			    		,getConfiguracionColumna("CantidadTransaccion")
			    		,getConfiguracionColumna("CantidadAsignadaTransaccion")
						,getConfiguracionColumna("MontoTransaccion")
			    		,getConfiguracionColumna("Tasa")
			    		,getConfiguracionColumna("NombreTipoInstrumento")
			    		,getConfiguracionColumna("NombreClasificacionTipoInstrumento")
			    		,getConfiguracionColumna("ValorRescateTransaccion")
			    		,getConfiguracionColumna("EstadoTransaccion")
			    		,getConfiguracionColumna("NombreCorredorContraparte")
				],
				
			}
		};
		return config;
	};
	
	obj.getConfiguracionConsultarOrdenesBloqueadas = function() {
		var config = {};
		config = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(obj.validarFiltrosV2(filtros,'ORDEN'));
			},
			
			options : {
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:400,
	      		userSettings:true,
				navigatable: true,
				excel : {
					allPages: true,
				},
				
				dataSource: new bcs.DataSource({
		  			type : "odata",
		  			definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true, 
						sort: { field: "IdOrden", dir: "desc" },
						pageSize : 200,
					    transport: {
							read: {
								url:bcsConsultasOrdenes('ConsultarOrdenesBloqueadas'),
								dataType: "json"
							}
						},
						schema : obj.getSchemaOrden()
					},
		  		}),
	  			columns: [
	  			          getConfiguracionColumna("IdOrden")
						, getConfiguracionColumna("FechaOperacionOrden")
						, getConfiguracionColumna("PrecioOrden")
						, getConfiguracionColumna("CantidadOrden")
						, getConfiguracionColumna("CantidadDisponibleOrden")
						, getConfiguracionColumna("Instrumento")
						, getConfiguracionColumna("InstrumentoSubyacente")
						, getConfiguracionColumna("EsVentaCortaUI")
						, getConfiguracionColumna("NombreTipoOperacion")
						, getConfiguracionColumna("NombreCondicionDeLiquidacion")
						, getConfiguracionColumna("MontoOrden")
						, getConfiguracionColumna("IdMonedaOrden")
						, getConfiguracionColumna("DocumentoIdentidad")
						, getConfiguracionColumna("NombreCompleto")
						, getConfiguracionColumna("CodigoPortafolio")
						, getConfiguracionColumna("EstadoOrden")
						, getConfiguracionColumna("FechaVigencia")
						, getConfiguracionColumna("OrdenVigenteUI")
						, getConfiguracionColumna("FechaRecepcion")
						, getConfiguracionColumna("CodigoEmpleadoPortafolio")
						, getConfiguracionColumna("CodigoEmpleadoDigitador")
						, getConfiguracionColumna("TipoPrecio")
						, getConfiguracionColumna("NombreCanal")
						, getConfiguracionColumna("IdBolsa")
						, getConfiguracionColumna("EstadoOrdenAsignacion")
						, getConfiguracionColumna("NombreTipoInstrumento")
						, getConfiguracionColumna("NombreClasificacionTipoInstrumento")
						, getConfiguracionColumna("CantidadAsignadaOrden")
				],
				
			}
		};
		return config;
	};
	
	obj.getConfiguracionConsultarTransaccionesAnuladasModificadas = function() {
		var config = {};
		config = {
			options : {
	      		autoBind: true,
	      		excel : {
					allPages: true,
				},
				
				dataSource: new bcs.DataSource({
		  			type : "array",
		  			definition : {
						data : [],
						schema : obj.getSchemaTransaccion()
					},
		  		}),
	  			columns: [
	  			          getConfiguracionColumna("IdTipoInstrumento")
						, getConfiguracionColumna("Nemotecnico")
						, getConfiguracionColumna("FechaLogCambio")
						, getConfiguracionColumna("IdBolsa")
						, getConfiguracionColumna("FechaTransaccion")
						, getConfiguracionColumna("FolioTransaccion")
						, getConfiguracionColumna("CodigoCorredorComprador")
						, getConfiguracionColumna("CodigoCorredorVendedor")
						, getConfiguracionColumna("TipoLogCambio")
						, getConfiguracionColumna("CantidadTransaccion")
						, getConfiguracionColumna("PrecioTransaccion")
						, getConfiguracionColumna("MontoTransaccion")
						, getConfiguracionColumna("TasaTransaccion")
						, getConfiguracionColumna("Motivo")
						, getConfiguracionColumna("Observaciones")
				],
				
			}
		};
		return config;
	};
	
	obj.getSchemaAsociaciones = function() {
		return { 
			model: {
				fields : {
					IdOrdenAsociada					: { type : "number"},
					IdOrdenReportado 	  		 	: { type : "number"},
					IdOrdenReportante 	  	  		: { type : "number"},
					MontoOrdenReportado 	  	  	: { type : "number"},
					MontoOrdenReportante 	  	  	: { type : "number"},
					NombreCompletoReportado			: { type : "string"},
					CodigoPortafolioReportado		: { type : "string"},
					IdMonedaOrdenReportado			: { type : "string"},
					NombreCompletoReportante		: { type : "string"},
					CodigoPortafolioReportante		: { type : "string"},
					IdMonedaOrdenReportante			: { type : "string"},
					TasaReportado					: { type : "number"},
					TasaReportante					: { type : "number"},
					FechaRegistro					: { type : "sysdate"},
					IdEstadoOrdenReportado			: { type : "string"},
					IdEstadoOrdenReportante			: { type : "string"},
					MontoAsociado					: { type : "number"},
					CantidadAsociada				: { type : "number"},
					IdTipoInstrumento				: { type : "string"},
					IdInstrumentoReportante			: { type : "number"},
					PlazoReportante					: { type : "number"},
					PrecioOrden						: { type : "number"},
					TasaPromedioReporte				: { type : "number"},
					IdMonedaLiquidacionOrdenReportante : { type : "string"},
					IdTipoOperacionReportante		: { type : "string"},
					IdTipoCanalIngresoReportante	: { type : "string"},
					IdBolsa							: { type : "string"},
					FechaOperacionReportante		: { type : "date"},
					IdTipoProducto					: { type : "string"},
					IdGrupoCobroReportante			: { type : "number"},
					IdGrupoCobroReportado			: { type : "number"},
					BaseReporteReportado			: { type : "number"},
					BaseReporteReportante			: { type : "number"},
					MontoOrdenReportante			: { type : "number"},
					MontoLiquidacion				: { type : "number"},
					MontoOrdenPlazo					: { type : "number"},
					MontoLiquidacionPlazo			: { type : "number"}
				}
			}
		}
	};
	
	obj.getDataSourceAsociaciones = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "IdOrdenAsociada", dir: "desc" }, //campo y criterio de ordenamiento
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4164",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaAsociaciones()			
		}
		})
	};
	
	obj.getDataSourceOrdenesReporte = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "IdOrden", dir: "desc" }, //campo y criterio de ordenamiento
			transport: {
	    		read: {
	    			url: bcsConsultasOrdenes('ConsultarOrdenesReportes'), //"/BSCL/Plataforma/GWJAVA/PLA/call?t=4149",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaOrden()			
		}
		})
	};
	
	obj.getDataSourceOrdenesReportePorAsignar = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "IdOrden", dir: "desc" }, //campo y criterio de ordenamiento
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4195",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaOrden()			
		}
		})
	};

	
	obj.getDataSourceConsultarOrdenesEnNegociacion = function() {
		return new bcs.DataSource({
  			type : "odata",
  			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdOrden", dir: "desc" },
				pageSize : 200,
			    transport: {
					read: {
						url:bcsConsultasOrdenes('ConsultarOrdenEnNegociacionReportes'),
						dataType: "json"
					}
				},
				schema : obj.getSchemaOrden()
			},
  		})
	};
	
	
	return obj;
});