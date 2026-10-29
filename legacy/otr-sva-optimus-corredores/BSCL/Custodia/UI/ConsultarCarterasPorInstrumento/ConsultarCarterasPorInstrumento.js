angular.module('ModuloCustodia').controller('ConsultarCarterasPorInstrumentoCtrl',
		function($scope,$timeout,$modal,$state,$rootScope, bcsComunes, $translate)
{	
	
	//*** configuracion de objetos ***//
	
	$scope.oConfiguracionEstado = {
		tipo : "combobox-estado",
		configuracion : {},
		filtros : {
			nACT : { "field"   : "IdEstado","operator": "neq", "value"   :  "ACT" },
			nANU : { "field"   : "IdEstado","operator": "neq", "value"   :  "ANU" },
			nANT : { "field"   : "IdEstado","operator": "neq", "value"   :  "ANT" },
			nCER : { "field"   : "IdEstado","operator": "neq", "value"   :  "CER" },
			nCRT : { "field"   : "IdEstado","operator": "neq", "value"   :  "CRT" },
			nCOM : { "field"   : "IdEstado","operator": "neq", "value"   :  "COM" },
			nENV : { "field"   : "IdEstado","operator": "neq", "value"   :  "ENV" },
			nINA : { "field"   : "IdEstado","operator": "neq", "value"   :  "INA" },
			nING : { "field"   : "IdEstado","operator": "neq", "value"   :  "ING" },
			nINV : { "field"   : "IdEstado","operator": "neq", "value"   :  "INV" },
			nMAN : { "field"   : "IdEstado","operator": "neq", "value"   :  "MAN" },
			nMOD : { "field"   : "IdEstado","operator": "neq", "value"   :  "MOD" },
			nNAS : { "field"   : "IdEstado","operator": "neq", "value"   :  "NAS" },
			nNOT_: { "field"   : "IdEstado","operator": "neq", "value"   :  "NOT" },
			nPEN : { "field"   : "IdEstado","operator": "neq", "value"   :  "PEN" },
			nPLQ : { "field"   : "IdEstado","operator": "neq", "value"   :  "PLQ" },
			nPRO : { "field"   : "IdEstado","operator": "neq", "value"   :  "PRO" },
			nREC : { "field"   : "IdEstado","operator": "neq", "value"   :  "REC" },
			nRES : { "field"   : "IdEstado","operator": "neq", "value"   :  "RES" },
			nRPA : { "field"   : "IdEstado","operator": "neq", "value"   :  "RPA" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'VIG'
		
	};
	
	
	modeloCarterasI = function() {
		return {
			data: "data",
			total: "total",
    		model: {
    			fields: {
        			IdCartera: 						{ type: "number"},
        			FolioCartera: 					{ type: "number"},
        			FechaLiquidacion: 				{ type: "sysdate"},
        			FechaOperacion:					{ type: "sysdate"},
        			INGFechaOperacion:				{ type: "sysdate"}, 
        			NombreEstado: 					{ type: "string"},
        			DocumentoIdentidad: 			{ type: "string"},
        			IdCliente: 						{ type: "number"},
        			CodigoPortafolio: 				{ type: "string"},
        			Nemotecnico:					{ type: "string"},
        			FechaVencimientoInstrumento:	{ type: "sysdate"},
        			IdBoveda: 						{ type: "string"},
        			Cantidad: 						{ type: "number"},
        			INGCantidad: 					{ type: "number"},
        			IdMonedaInstrumento:			{ type: "string"},
        			IdMonedaSistema: 				{ type: "string"},
        			IdTipoReajuste: 				{ type: "string"},
        			PrecioCompra:					{ type: "number"},
        			MontoCompra: 					{ type: "number"},
        			INGMontoATasaCompraActual:  	{ type: "number"},
        			TipoInstrumento: 				{ type: "string"},
        			EsFungible: 					{ type: "string"},
        			TasaCompra: 					{ type: "number"},
        			INGTasaMercadoActual: 			{ type: "number"},
        			NemoSVS: 						{ type: "string"},
        			IdTipoComprobante:				{ type: "string"},
        			FolioComprobante:				{ type: "number"},
        			FechaComprobante:				{ type: "sysdate"},
        			INGPrecioPromedioCompra: 		{ type: "number"},
        			INGMontoPrecioPromedioCompra: 	{ type: "number"},
        			INGPrecioMercadoActual:			{ type: "number"},
        			INGMontoATasaMercadoActual: 	{ type: "number"},
        			INGFechaPagoCorteCupon:			{ type: "sysdate"},
        			INGMontoCorteCupon: 			{ type: "number"},
        			INGMontoCapitalCorteCupon:		{ type: "number"},
        			INGMontoInteresCorteCupon:		{ type: "number"},
        			INGValorRescate:				{ type: "number"},
        			DiferenciaInversion: 					{ type: "number"},
        			ISIN:									{ type: "string"},
        			IdClasificacionTipoInstrumento			: { type: "string"},
        			IdEmisor								: { type: "string"},
        			INGPrecioCompra							: { type: "number"},
        			INGFuenteTasaPrecioMercadoActual		: { type: "string"},
        			IdConcepto								: { type: "string"},
        			NombreConcepto							: { type: "string"},
        			CodigoIFRS								: { type: "string"},
        			NumeroCuentaBoveda						: { type: "string"},
					IdTipoCustodia							: { type: "string"},
					NombreTipoCustodia						: { type: "string"},
					IdEstado								: { type: "string"},
					NombreEstado							: { type: "string"},
					RazonSocial 							: { type: "string"},
					ClasificacionTipoInstrumento 			: { type: "string"},
					IdMonedaInstrReajuste 					: { type: "string"},
					IdTipoInstrumento						: { type: "string"}
        		} 
        	}
		};
	};
	
	
	$scope.data ={};
	
	$scope.kngFechaConsulta		 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.chkFechaAdquisicion 	 = true;  //búsqueda por defecto por fecha de liquidación
	
	
	$scope.verGrillaIAll = true;
	$scope.verGrillaIRV = false;
	$scope.verGrillaIRF = false;
	$scope.verGrillaIIF = false;
	$scope.verGrillaIDV = false;
	$scope.verGrillaIFM = false;
	

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);

	
	$scope.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
	};
	
   	$scope.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
				IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
				NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
				NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
				NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
				NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
				NoReportesRV					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRV" },
				NoReportesRF					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRF" },
				NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" },
				NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
	};
   	
   	$scope.oConfiguracionInstrumento={
			buscar          : "-1"    
	};	
   	
	var dsGrillaCarterasIAll = new bcs.DataSource({
	               type : "call",
	               definition : {
	                               sort 			: { field: "FolioCartera", dir: "desc" },
	                               pageSize         : 200,
	                               serverPaging		: true,
	                               serverSorting	: true,
	                               callTransaction	: "100012",
	                   			   callResultSet	: "P_OUT_TABLA",
	                   			   transport : {
										read: function(options) {
											util.callTransport(options, "100012", "P_OUT_TABLA", $scope.grillaIAll.dataSource.lastCallParameters);
										}
									},
	                               schema :modeloCarterasI()
	               }
	});
	
	$scope.confBasicaCarterasIAll = {
		dataSource			: dsGrillaCarterasIAll,
		columns				: 
			[
	    		 {field: "FolioCartera", 							title: 'UI_FOLIO_CARTERA_ABR', 			width: "70px" }
	    		,{field: "FechaOperacion", 							title: 'UI_FECHA_INGRESO_ABR', 			width: "80px" }
		    	,{field: "FechaLiquidacion", 						title: 'UI_FECHA_LIQUIDACION_ABR',		width: "80px" }
	    		,{field: "DocumentoIdentidad", 						title: 'UI_CLIENTE', 					width: "85px", 	groupable: true, 	template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>",	attributes:{style:"white-space:nowrap;"}} 
	    		,{field: "CodigoPortafolio", 						title: 'UI_PORTAFOLIO_ABR', 			width: "50px", 	groupable: true }
	    		,{field: "NombreTipoCustodia", 						title: 'UI_TIPO_CUSTODIA', 				width: "50px",  groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
				,{field: "IdClasificacionTipoInstrumento",			title: 'UI_MERCADO', 					width: "50px", 	groupable: true, 	template: "<span class='flags' title='#=ClasificacionTipoInstrumento#'>#=IdClasificacionTipoInstrumento#</span>",	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "TipoInstrumento", 						title: 'UI_TIPO_INSTRUMENTO', 			width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "Nemotecnico", 							title: 'UI_INSTRUMENTO', 				width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "ISIN", 									title: 'UI_ISIN', 						width: "100px",	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "IdEmisor", 								title: 'UI_EMISOR', 					width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
				,{field: "IdTipoReajuste", 					        title: 'UI_TIPO_REAJUSTE', 			    width: "55px", 	groupable: true,	hidden:true,    attributes:{style:"white-space:nowrap;"}}
				,{field: "FechaVencimientoInstrumento", 			title: 'UI_VENCIMIENTO', 				width: "80px"  }
	    		,{field: "IdBoveda", 								title: 'UI_BOVEDA_ABR', 				width: "55px", 	groupable: true }
	    		,{field: "NumeroCuentaBoveda", 						title: 'UI_NUMERO_CUENTA', 				width: "55px", 	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "INGValorRescate", 				        title: 'UI_VALOR_RESCATE', 			    width: "80px", 	hidden:true,        template : " <label> #:  (util.formateaNumero(INGValorRescate,4)) #</label>", 	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
	    		,{field: "INGCantidad", 							title: 'UI_CANTIDAD', 					width: "80px",	template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaInstrReajuste,'','')).valor #</label>", 	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }	    		
	    		,{field: "INGPrecioCompra", 						title: 'UI_PRECIO_COMPRA_ABR', 			width: "90px", 	template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>"}
				,{field: "TasaCompra", 								title: 'UI_TASA_COMPRA', 				width: "60px", 	template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
				,{field: "INGMontoATasaCompraActual", 				title: 'UI_MONTO_COMPRA', 				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    		
	    		,{field: "INGPrecioMercadoActual", 					title: 'UI_PRECIO_MERCADO', 			width: "90px", 	template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", 					title: 'UI_TASA_MERCADO', 				width: "60px", 	template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
	    		,{field: "INGMontoATasaMercadoActual", 				title: 'UI_MONTO_MERCADO', 				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGFuenteTasaPrecioMercadoActual", 		title: 'UI_FUENTE_MERCADO', 			width: "150px", groupable: true,	attributes:{style:"white-space:nowrap;"}}	    		
	    		,{field: "INGPrecioPromedioCompra", 				title: 'UI_PRECIO_PROMEDIO',	 		width: "90px", 	template : " <label> #:  (util.bcs_formato(INGPrecioPromedioCompra,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
	    		,{field: "INGMontoPrecioPromedioCompra", 			title: 'UI_MONTO_PROMEDIO', 			width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoPrecioPromedioCompra,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    		
	    		,{field: "DiferenciaInversion", 					title: 'UI_DIFERENCIA_MERCADO', 		width: "105px", template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    		
	    		,{field: "IdMonedaInstrumento", 					title: 'UI_MONEDA_ABR', 				width: "55px", 	groupable: true }
	    		,{field: "EsFungible", 								title: 'UI_FUNGIBLE', 					width: "50px" }
	    		,{field: "IdConcepto", 								title: 'UI_CONCEPTO_ABR', 				width: "55px", 	groupable: true, 	template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>"  }
	    		,{field: "NombreConcepto", 							title: 'UI_CONCEPTO', 					width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "CodigoIFRS", 								title: 'UI_CATEGORIA_IFRS_ABR', 		width: "80px" }
	    		,{field: "NemoSVS", 								title: 'UI_NEMO_SVS', 					width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}	    		
	    		,{field: "IdTipoComprobante", 						title: 'UI_TIPO_COMPROBANTE', 			width: "60px" }
	    		,{field: "FolioComprobante", 						title: 'UI_FOLIO_COMPROBANTE', 			width: "50px", 	format: "{0:n0}" }
	    		,{field: "FechaComprobante", 						title: 'UI_FECHA_COMPROBANTE', 			width: "80px" }	    		
	    		,{field: "INGFechaPagoCorteCupon",		 				title: 'UI_FECHA_CORTE_CUPON', 			width: "80px" }
	    		,{field: "INGMontoCorteCupon", 						title: 'UI_MONTO_CORTE_CUPON', 			width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCorteCupon,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>" }
	    		,{field: "INGMontoCapitalCorteCupon", 				title: 'UI_CAPITAL_CORTE_CUPON', 		width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCapitalCorteCupon,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGMontoInteresCorteCupon", 				title: 'UI_INTERES_CORTE_CUPON', 		width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoInteresCorteCupon,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
				,{field: "NombreEstado", 							title: 'UI_ESTADO', 					width: "80px", groupable: true }
			],
			userSettings 		: true,
			pageable	        : true,
			allowCopy			: { delimeter: "\t" },
			sortable			: true,
			groupable 			: true,
			filterable 			: true,
			height				: 480,
			excel 				: {allPages: true,},
			selectable 			: "row",
		gridCreated 		: function(grid) {
	    	$scope.grillaIAll = grid;
	    	if ($scope.SesionLocale === 'PE'){
	    		grid.showColumn("ISIN");
	    		grid.showColumn("NombreTipoCustodia");
	    		grid.showColumn("NumeroCuentaBoveda");
	    	}
	    }
	};
	
	
   	//********* fin configuracion de objetos *********//
	
	
	$scope.buscarCarteras = function(){
		
		var seleccOK = false;
		parametrosSP = {};
		
		var filters = new Array();
		
		parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo = 'CUS';
		parametrosSP.p_tipoFecha = ($scope.chkFechaLiquidacion ? 'LIQ' : 'ADQ');
		parametrosSP.p_FechaConsulta = util.dateSPdate($scope.kngFechaConsulta);
		
		parametrosSP.p_IdClasificacionTipoInstrumento = "";
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;
		parametrosSP.p_Nemotecnico = "";
		parametrosSP.p_FolioCartera = 0;
		parametrosSP.p_IdMoneda = "";
		parametrosSP.p_IdEstado = "";
		
		parametrosSP.p_IdProductoAgrupador = null;

		if ($scope.instrumentoDatos !== undefined && $scope.instrumentoDatos != ""){    	
	    	if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined && ($scope.instrumentoDatos.ConteoNemotecnico === undefined || $scope.instrumentoDatos.ConteoNemotecnico === 1))	{		
    			parametrosSP.p_IdInstrumento = Number($scope.instrumentoDatos.IdInstrumento)
				seleccOK = true;
			}
	    		
			if ($scope.instrumentoDatos.Nemotecnico != "" && $scope.instrumentoDatos.Nemotecnico !== undefined && $scope.instrumentoDatos.ConteoNemotecnico > 1) {
				parametrosSP.p_Nemotecnico = $scope.instrumentoDatos.Nemotecnico;
				seleccOK = true;				
			}
    	}
		
		if ($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento != "" && $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento !== undefined){
			parametrosSP.p_IdClasificacionTipoInstrumento = $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;
			seleccOK = true;
		}
    		
    	if ($scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento != "" && $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined)  {
    		parametrosSP.p_IdTipoInstrumento = $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
    		seleccOK = true;
    	}  		
		if ($scope.oConfiguracionEstado.seleccionado != "" && $scope.oConfiguracionEstado.seleccionado !== undefined && $scope.oConfiguracionEstado.seleccionado.IdEstado != 0)
    		parametrosSP.p_IdEstado = $scope.oConfiguracionEstado.seleccionado.IdEstado;    	
		
		dsGrillaCarterasIAll.removeAll();

		switch ($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento){
    		case "RV":
    		    $scope.grillaIAll.hideColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.hideColumn("IdEmisor");
    		    $scope.grillaIAll.hideColumn("IdTipoReajuste");
    		    $scope.grillaIAll.hideColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.hideColumn("INGValorRescate");
    		    $scope.grillaIAll.showColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.hideColumn("EsFungible");
    		    $scope.grillaIAll.hideColumn("CodigoIFRS");
    		    $scope.grillaIAll.hideColumn("NemoSVS");
    		    $scope.grillaIAll.hideColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoInteresCorteCupon");
    			break;
    		case "RF":
    		    $scope.grillaIAll.hideColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.hideColumn("IdEmisor");
    		    $scope.grillaIAll.hideColumn("IdTipoReajuste");
    		    $scope.grillaIAll.showColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.hideColumn("INGValorRescate");
    		    $scope.grillaIAll.hideColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.hideColumn("EsFungible");
    		    $scope.grillaIAll.showColumn("CodigoIFRS");
    		    $scope.grillaIAll.hideColumn("NemoSVS");
    		    $scope.grillaIAll.showColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoInteresCorteCupon");
    			break;
    		case "IF":
    		    $scope.grillaIAll.hideColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.showColumn("IdEmisor");
    		    $scope.grillaIAll.showColumn("IdTipoReajuste");
    		    $scope.grillaIAll.showColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.showColumn("INGValorRescate");
    		    $scope.grillaIAll.showColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.showColumn("EsFungible");
    		    $scope.grillaIAll.showColumn("CodigoIFRS");
    		    $scope.grillaIAll.showColumn("NemoSVS");
    		    $scope.grillaIAll.hideColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoInteresCorteCupon");
    			break;
    		case "DV":
    		    $scope.grillaIAll.hideColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.hideColumn("IdEmisor");
    		    $scope.grillaIAll.hideColumn("IdTipoReajuste");
    		    $scope.grillaIAll.hideColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.hideColumn("INGValorRescate");
    		    $scope.grillaIAll.showColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.hideColumn("EsFungible");
    		    $scope.grillaIAll.showColumn("CodigoIFRS");
    		    $scope.grillaIAll.hideColumn("NemoSVS");
    		    $scope.grillaIAll.hideColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoInteresCorteCupon");
    			break;
    		case "FM":
    		    $scope.grillaIAll.hideColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.hideColumn("IdEmisor");
    		    $scope.grillaIAll.hideColumn("IdTipoReajuste");
    		    $scope.grillaIAll.hideColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.hideColumn("INGValorRescate");
    		    $scope.grillaIAll.showColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.hideColumn("EsFungible");
    		    $scope.grillaIAll.showColumn("CodigoIFRS");
    		    $scope.grillaIAll.hideColumn("NemoSVS");
    		    $scope.grillaIAll.hideColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.hideColumn("INGMontoInteresCorteCupon");
    			break;
    		default:
    		    $scope.grillaIAll.showColumn("IdClasificacionTipoInstrumento");
    		    $scope.grillaIAll.showColumn("IdEmisor");
    		    $scope.grillaIAll.hideColumn("IdTipoReajuste");
    		    $scope.grillaIAll.showColumn("FechaVencimientoInstrumento");
    		    $scope.grillaIAll.hideColumn("INGValorRescate");
    		    $scope.grillaIAll.showColumn("IdMonedaInstrumento");
    		    $scope.grillaIAll.showColumn("EsFungible");
    		    $scope.grillaIAll.showColumn("CodigoIFRS");
    		    $scope.grillaIAll.showColumn("NemoSVS");
    		    $scope.grillaIAll.showColumn("INGFechaCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoCapitalCorteCupon");
    		    $scope.grillaIAll.showColumn("INGMontoInteresCorteCupon");
    			break;
		}
    	dsGrillaCarterasIAll.setCallParameters(parametrosSP);		
	};
	
	$scope.limpiarFiltros = function() {
		$scope.oConfiguracionClasificacionTipoInstrumento.idActual = "";
		$scope.oConfiguracionClasificacionTipoInstrumento.idDefecto = "";
		//$scope.filtros.instrumento = "";
		//$scope.filtros.instrumentoObj = undefined;
		$scope.oConfiguracionTipoInstrumento.idActual = "";
		$scope.oConfiguracionTipoInstrumento.idDefecto = "";
		$scope.kngFechaConsulta		 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.chkFechaLiquidacion 	 = true;  //búsqueda por defecto por fecha de liquidación 
	};
   
    var fnCambiaInstrumento = function (newValue, oldValue){
    	try{
	    	if ($scope.filtros.instrumentoDatos.IdInstrumento > 0){
	    		$scope.oConfiguracionTipoInstrumento.idActual = $scope.filtros.instrumentoDatos.IdTipoInstrumento;
	    		$scope.oConfiguracionTipoInstrumento.idDefecto = $scope.filtros.instrumentoDatos.IdTipoInstrumento;

	    		$scope.oConfiguracionClasificacionTipoInstrumento.idActual = $scope.filtros.instrumentoDatos.IdClasificacionTipoInstrumento;
	    		$scope.oConfiguracionClasificacionTipoInstrumento.idDefecto = $scope.filtros.instrumentoDatos.IdClasificacionTipoInstrumento;
	    	}
    	}catch(e){
    	}
    };
    
    $scope.$watch('filtros.instrumentoDatos', fnCambiaInstrumento );
   
	$scope.$watch('chkFechaLiquidacion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.chkFechaAdquisicion = false;  //desclikea check fecha adquisicion
				break;
			case false: 
				$scope.chkFechaAdquisicion = true;				
				break;
		}
	});
	
	$scope.$watch('chkFechaAdquisicion',function(newval,oldval){
		switch (newval){
			case true:
				$scope.chkFechaLiquidacion = false; //desclikea check fecha liquidacion				
				break;

			case false:
				$scope.chkFechaLiquidacion = true;
				break;
		}
	});
});