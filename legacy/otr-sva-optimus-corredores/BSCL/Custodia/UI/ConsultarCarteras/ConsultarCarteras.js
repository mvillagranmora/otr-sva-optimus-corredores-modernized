	angular.module('ModuloCustodia').controller('ConsultarCarterasCtrl',
function($scope,$timeout,$modal,$state,bcsComunes, $rootScope, $translate,bcsServiciosValorizacion)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.esVentana = false;
	$scope.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
	$scope.esVentana = ($scope.parametros.esVentana == 'S');
	$scope.IdClienteVentana = 0;
	$scope.IdPortafolioVentana = 0;
	
	$scope.verGrillaAll = true;
	
	$scope.kngFechaConsulta		 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.chkFechaAdquisicion 	 = true;  //búsqueda por defecto por fecha de liquidación 
	
	
	$scope.cargaFiltroParametros = function(){
		
		if ($scope.esVentana){
			$scope.filtros.clasificacionTipoInstrumento = $scope.parametros.IdClasificacionTipoInstrumento;
			$timeout(function(){
				$scope.filtros.tipoInstrumento = $scope.parametros.IdTipoInstrumento;	
			},1000);
			
			$scope.IdClienteVentana = $scope.parametros.IdCliente;
			$scope.IdPortafolioVentana = $scope.parametros.IdPortafolio;
			$scope.oConfiguracionCliente.seleccionado.IdCliente = $scope.parametros.IdCliente;
			$scope.oConfiguracionPortafolio.seleccionado.IdPortafolio = $scope.parametros.IdPortafolio;
			
			$timeout(function(){
				$scope.buscarCarteras();
			},1000);
		}
	}
	
	$timeout(function(){
    	$scope.cargaFiltroParametros();	
	},1000);
	
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	$scope.data = {};
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.oConfiguracionCliente = {
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : undefined,
		seleccionado : -1,
		idDefecto : -1
	};
	
	$scope.oConfiguracionPortafolio = {
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.oConfiguracionMoneda = {
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : { 	
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
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
	
	$scope.oConfiguracionClasificacionTipoInstrumento = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
   	$scope.oConfiguracionTipoInstrumento = {
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
   	
   	$scope.oConfiguracionInstrumento = {
		buscar : "-1"    
	};
   	
   	$scope.oConfiguracionInstrumento = {
   		buscar          : "-1"
	};
   	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  undefined }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	modeloCarteras = function() {
		return {
    		model: {
    			fields: {
				Cantidad								: { type: "string"},
    				Cantidad								: { type: "number"},
    				ClasificacionTipoInstrumento 			: { type: "string"},
    				CodigoIFRS								: { type: "string"},
    				CodigoPortafolio						: { type: "string"},
    				CorteMinimo								: { type: "number"},
    				DescripcionPortafolio 					: { type: "string"},
    				DiferenciaInversion						: { type: "number"},
    				DocumentoIdentidad						: { type: "string"},
    				EGRCantidad 							: { type: "number"},
    				EGRFechaComprobante 					: { type: "sysdate"},
    				EGRFechaLiquidacion 					: { type: "sysdate"},
    				EGRFechaOperacion 	 					: { type: "sysdate"},
    				EGRFolioComprobante 					: { type: "number"},
    				EGRIdConcepto 							: { type: "string"},
    				EGRIdTipoComprobante 					: { type: "string"},
    				EGRMonto 								: { type: "number"},
    				EGRMontoMonedaInstrumento 				: { type: "number"},
    				EGRNombreConcepto 						: { type: "string"},
    				EGRPrecio 								: { type: "number"},
    				EGRTasa 								: { type: "number"},
    				EGRValorRescate 						: { type: "number"},
    				EsFungible								: { type: "string"},
    				EsPagaderoEn							: { type: "string"},
    				FechaComprobante						: { type: "sysdate"},
    				FechaEmisionInstrumento					: { type: "sysdate"},
    				FechaLiquidacion						: { type: "sysdate"},
    				FechaOperacion							: { type: "sysdate"},
    				FechaVencimientoInstrumento				: { type: "sysdate"},
    				FolioCartera							: { type: "string"},
    				FolioComprobante						: { type: "number"},
    				IdAgente								: { type: "number"},
    				IdBolsa									: { type: "string"},
    				IdBoveda								: { type: "string"},
    				IdCartera								: { type: "number"},
    				IdClasificacionTipoInstrumento			: { type: "string"},
    				IdCliente								: { type: "number"},
    				IdConcepto								: { type: "string"},
    				IdCondicionLiquidacion 					: { type: "string"},
    				IdEmisor								: { type: "string"},
    				IdEmpresa 								: { type: "number"},
    				IdEstado								: { type: "string"},
    				IdInstrumento							: { type: "number"},
    				IdMesa									: { type: "number"},
    				IdMonedaInstrReajuste 					: { type: "string"},
    				IdMonedaInstrumento						: { type: "string"},
    				IdMonedaEmision							: { type: "string"}, // TODO Aqui pase
    				IdMonedaSistema							: { type: "string"},
    				IdPortafolio							: { type: "number"},
    				IdSucursal								: { type: "number"},
    				IdTipoComprobante						: { type: "string"},
    				IdTipoCustodia							: { type: "string"},
    				IdTipoInstrumento						: { type: "string"},
    				IdTipoOperacion 						: { type: "string"},
    				IdTipoReajuste							: { type: "string"},
    				INGCantidad								: { type: "number"},
    				INGFechaLiquidacion						: { type: "sysdate"},
    				INGFechaOperacion						: { type: "sysdate"},
    				INGFechaPagoCorteCupon					: { type: "sysdate"},
    				INGFuenteTasaPrecioMercadoActual		: { type: "string"},
    				INGFechaValorizacionCartera 			: { type: "sysdate"},
    				INGMontoATasaCompraActual				: { type: "number"},
    				INGMontoATasaMercadoActual				: { type: "number"},
    				INGMontoCapitalCorteCupon				: { type: "number"},
    				INGMontoCompraActualMonedaInstrumento	: { type: "number"},
    				INGMontoCorteCupon						: { type: "number"},
    				INGMontoInteresCompraActual				: { type: "number"},
    				InteresCorridoMonedaInstrumento  		: { type: "number"},    				
    				INGMontoInteresCorteCupon				: { type: "number"},
    				INGInteresesPagoCorteCupon				: { type: "number"},	
    				INGMontoMercadoActualMonedaInstrumento	: { type: "number"},
    				INGMontoPrecioPromedioCompra			: { type: "number"},
    				INGMontoReajusteCompraActual			: { type: "number"},
    				ReajusteCorridoMonedaInstrumento		: { type: "number"},
    				INGPrecioCompra							: { type: "number"},
    				INGPrecioMercadoActual					: { type: "number"},
    				INGPrecioPromedioCompra					: { type: "number"},
    				INGTasaCompra							: { type: "number"},
    				INGTasaMercadoActual					: { type: "number"},
    				INGValorRescate							: { type: "number"},
        			ISIN									: { type: "string"},    				
        			MonedaInstrumento						: { type: "string"},
        			MonedaSistema 							: { type: "string"},
        			MontoCompra								: { type: "number"},
        			MontoCompraInicial						: { type: "number"},
        			ValorCompraInicial						: { type: "number"},
        			NemoSVS									: { type: "string"},
        			Nemotecnico								: { type: "string"},
        			NombreBolsa								: { type: "string"},
        			NombreBoveda							: { type: "string"},
        			NombreConcepto							: { type: "string"},
        			NombreCondicionLiquidacion				: { type: "string"},
        			NombreEmpresa							: { type: "string"},
        			NombreEstado							: { type: "string"},
        			NombreMonedaInstrReajuste 				: { type: "string"},
        			NombreTipoCustodia						: { type: "string"},
        			NombreTipoReajuste						: { type: "string"},
        			NumeroCuentaBoveda						: { type: "string"},
        			PrecioCompra							: { type: "number"},
        			RazonSocial 							: { type: "string"},
        			RazonSocialInstrumento					: { type: "string"},
        			Rentabilidad							: { type: "number"},
        			RentabilidadReal						: { type: "number"},
        			CostoUF									: { type: "number"},
        			TasaCompra								: { type: "number"},
        			TipoInstrumento							: { type: "string"},
        			TipoOpcion								: { type: "string"},
					ValorRescate 							: { type: "number"},
					NombreProductoAgrupador		            : { type: "string"},
					RegimenTributario			            : { type: "string"},
					FechaOrigenCompra						: { type: "sysdate"},
					MontoCorteCuponAcumulado				: { type: "number"},
					MontoInteresCorteCuponAcumulado			: { type: "number"},
					MontoCapitalCorteCuponAcumulado			: { type: "number"}
        		} 
        	}
		};
	};
	
	
	var dsGrillaCarterasAll = new bcs.DataSource({
		type : "call",
		definition : {
			callTransaction	: "100012",
			callResultSet	: "P_OUT_TABLA",
			sort			: { field: "FolioCartera", dir: "desc" },
			pageSize		: 200,
			schema			: modeloCarteras()
		}
	});
	
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.confBasicaCarterasAll = {
		dataSource			: dsGrillaCarterasAll,
		columns				: 
			[
	    		 {field: "FolioCartera", 							title: 'UI_FOLIO_CARTERA_ABR', 			width: "70px" }
	    		,{field: "FechaOperacion", 							title: 'UI_FECHA_INGRESO_ABR', 			width: "80px" }
		    	,{field: "FechaLiquidacion", 						title: 'UI_FECHA_LIQUIDACION_ABR', 		width: "80px" }
	    		,{field: "DocumentoIdentidad", 						title: 'UI_CLIENTE', 					width: "85px", 	groupable: true,  	template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>"   } 
	    		,{field: "CodigoPortafolio", 						title: 'UI_PORTAFOLIO_ABR', 			width: "50px",  groupable: true }
	    		,{field: "NombreTipoCustodia", 						title: 'UI_TIPO_CUSTODIA', 				width: "50px",  groupable: true,	hidden: true  }
	    		,{field: "IdClasificacionTipoInstrumento", 			title: 'UI_MERCADO', 					width: "50px", 	groupable: true, 	template: "<span class='flags' title='#=ClasificacionTipoInstrumento#'>#=IdClasificacionTipoInstrumento#</span>" }
	    		,{field: "TipoInstrumento", 						title: 'UI_TIPO_INSTRUMENTO',	 		width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "Nemotecnico", 							title: 'UI_INSTRUMENTO', 				width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}	    		
	    		,{field: "ISIN", 									title: 'UI_ISIN', 						width: "100px", groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "IdEmisor", 								title: 'UI_EMISOR', 					width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "FechaVencimientoInstrumento", 			title: 'UI_VENCIMIENTO', 				width: "80px" }
	    		,{field: "IdBoveda", 							    title: 'UI_BOVEDA', 					width: "55px", 	groupable: true }
	    		,{field: "NumeroCuentaBoveda", 						title: 'UI_NUMERO_CUENTA', 				width: "55px", 	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "INGCantidad", 							title: 'UI_CANTIDAD', 					width: "80px", 	template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaInstrumento!=''?IdMonedaInstrumento:IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }	    		
	    		,{field: "INGValorRescate", 						title: 'UI_VALOR_RESCATE', 				width: "80px",  template : " <label> #:  (util.bcs_formato(INGValorRescate,'GEN','Cantidad',IdMonedaInstrReajuste,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
	    		,{field: "IdMonedaInstrumento", 					title: 'UI_MONEDA_PAGO', 				width: "55px", 	groupable: true }//Modificado
	    		,{field: "IdTipoReajuste", 							title: 'UI_TIPO_REAJUSTE', 				width: "55px", 	groupable: true}
	    		,{field: "INGPrecioCompra", 						title: 'UI_PRECIO_COMPRA_ABR', 			width: "90px", 	template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>"}
				,{field: "INGTasaCompra", 							title: 'UI_TASA_COMPRA', 				width: "60px", 	template : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }				
				,{field: "INGMontoCompraActualMonedaInstrumento", 	title: 'UI_VALOR_COMPRA',				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCompraActualMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }				
				,{field: "INGMontoATasaCompraActual", 				title: 'UI_MONTO_COMPRA', 				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    			    		
				,{field: "MontoCompraInicial",						title: 'UI_MONTO_COMPRA_INICIAL',		width: "105px", template : " <label> #:  (util.bcs_formato(MontoCompraInicial,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorCompraInicial", 						title: 'UI_VALOR_COMPRA_INICIAL',		width: "105px", template : " <label> #:  (util.bcs_formato(ValorCompraInicial,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "INGMontoInteresCompraActual",				title: 'UI_INTERES_DIARIO', 			width: "105px",	template : " <label> #:  (util.bcs_formato(INGMontoInteresCompraActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>"}
				,{field: "INGMontoReajusteCompraActual", 			title: 'UI_REAJUSTE_DIARIO', 			width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoReajusteCompraActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }								
				,{field: "INGPrecioMercadoActual", 					title: 'UI_PRECIO_MERCADO', 			width: "90px", 	template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", 					title: 'UI_TASA_MERCADO', 				width: "60px", 	template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
				,{field: "INGMontoMercadoActualMonedaInstrumento", 	title: 'UI_VALOR_MERCADO',				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoMercadoActualMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
				,{field: "Rentabilidad", 							title: 'UI_RENTABILIDAD', 				width: "60px", format:"{0:n2}"}
	    		,{field: "RentabilidadReal",						title: 'UI_RENTABILIDAD_REAL',			width: "60px", format:"{0:n2}"}
	    		,{field: "CostoUF",									title: 'UI_COSTO_UF',					width: "60px", format:"{0:n2}"}
				,{field: "INGMontoATasaMercadoActual", 				title: 'UI_MONTO_MERCADO', 				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGFuenteTasaPrecioMercadoActual", 		title: 'UI_FUENTE_PRECIO_MERCADO', 		width: "150px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "INGFechaValorizacionCartera", 			title: 'UI_FECHA_VALORIZACION', 		width: "80px" }
	    		,{field: "DiferenciaInversion", 					title: 'UI_DIFERENCIA_MERCADO', 		width: "105px", template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    		
	    		,{field: "IdMonedaInstrumento", 					title: 'UI_MONEDA_INSTRUMENTO', 		width: "55px", 	groupable: true }	    		
	    		,{field: "EsFungible", 								title: 'UI_FUNGIBLE', 					width: "50px" }
	    		,{field: "IdConcepto", 								title: 'UI_CONCEPTO_ABR', 				width: "55px", 	groupable: true, 	template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>"  }
	    		,{field: "NombreConcepto", 							title: 'UI_CONCEPTO', 					width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "CodigoIFRS", 								title: 'UI_CATEGORIA_IFRS_ABR', 		width: "80px" }
	    		,{field: "NemoSVS", 								title: 'UI_NEMO_SVS', 					width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}	    		
	    		,{field: "IdTipoComprobante", 						title: 'UI_TIPO_COMPROBANTE', 			width: "60px" }
	    		,{field: "TipoOpcion",		 						title: 'UI_TIPO_OPCION',	 			width: "60px" }
	    		,{field: "FolioComprobante", 						title: 'UI_FOLIO_COMPROBANTE', 			width: "50px", 	format: "{0:n0}" }
	    		,{field: "FechaComprobante", 						title: 'UI_FECHA_COMPROBANTE', 			width: "80px" }	    		
	    		,{field: "INGFechaPagoCorteCupon", 					title: 'UI_FECHA_CORTE_CUPON', 			width: "80px" }
	    		,{field: "INGMontoCorteCupon", 						title: 'UI_MONTO_CORTE_CUPON', 			width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCorteCupon,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoCapitalCorteCupon", 				title: 'UI_CAPITAL_CORTE_CUPON', 		width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCapitalCorteCupon,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGInteresesPagoCorteCupon", 				title: 'UI_INTERES_CORTE_CUPON', 		width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(INGInteresesPagoCorteCupon,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "InteresCorridoMonedaInstrumento",			title: 'UI_INTERESES_CORRIDOS', 		width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(InteresCorridoMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
				,{field: "ReajusteCorridoMonedaInstrumento", 		title: 'UI_REAJUSTES_VAC_CORRIDO', 		width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(ReajusteCorridoMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }																
	    		,{field: "NombreProductoAgrupador", 				title: 'UI_PRODUCTO',			        width: "80px", 	hidden: true}
	    		,{field: "RegimenTributario", 						title: 'UI_REGIMEN_TRIBUTARIO',	        width: "100px"}
	    		,{field: "FechaOrigenCompra", 						title: 'UI_FECHA_ORIGEN_COMPRA',	    width: "100px"}
	    		,{field: "NombreEstado", 							title: 'UI_ESTADO', 					width: "80px", groupable: true }
				,{field: "MontoCorteCuponAcumulado", 				title: 'UI_MONTO_CORTE_CUPON_ACUMULADO', 			width: "170px", hidden: true ,template : " <label> #:  (util.bcs_formato(MontoCorteCuponAcumulado,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "MontoCapitalCorteCuponAcumulado", 		title: 'UI_CAPITAL_CORTE_CUPON_ACUMULADO', 		width: "170px", hidden: true ,template : " <label> #:  (util.bcs_formato(MontoCapitalCorteCuponAcumulado,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "MontoInteresCorteCuponAcumulado", 		title: 'UI_INTERES_CORTE_CUPON_ACUMULADO', 		width: "170px", hidden: true,		template : " <label> #:  (util.bcs_formato(MontoInteresCorteCuponAcumulado,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
			],
		userSettings 		: true,
		pageable	        : true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		groupable 			: true,
		filterable 			: true,
		height				: 465,
		excel 				: {allPages: true,},
		selectable 			: "row",
		rowColorBackground: function(row) {
			var color = "black";
			if (row.EsNegativa === 'S') {
				color = "red";
			}
			return color;
		},
		gridCreated 		: function(grid) {
	    	$scope.grillaAll = grid;
	        	
	    	if($scope.MostrarProducto === true){
		    	grid.showColumn("NombreProductoAgrupador");
	    	}
	    	
	    	if ($scope.SesionLocale === 'PE'){
	    		grid.showColumn("ISIN");
	    		grid.showColumn("NumeroCuentaBoveda");
	    		grid.showColumn("NombreTipoCustodia");
				grid.showColumn("INGInteresesPagoCorteCupon");
				grid.showColumn("InteresCorridoMonedaInstrumento");
				grid.showColumn("ReajusteCorridoMonedaInstrumento");
	    	}
	    }
	};
	
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscarCarteras = function(){
		var seleccOK = false;
		parametrosSP = {};
		
		parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
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
		
		// se mete dentro de un try ya q cuando no se ingresa un valor al objeto, este no se lee
		try
		{
			if ($scope.filtros.folioCartera !== undefined && $scope.filtros.folioCartera !=='' && $scope.filtros.folioCartera !==0)
			{
	    		try
	    		{
		    		if (Number($scope.filtros.folioCartera))
		    		{
			    		var auxNum = Number($scope.filtros.folioCartera);
			    		seleccOK = true;
			    		parametrosSP.p_FolioCartera = auxNum;
		    		}
	    		}
	    		catch (e){}
        	}
        }
		catch(e) {}
		
    	
		try
		{
			if (!$scope.esVentana)
			{
				if($scope.MostrarProducto)
				{
					if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
					{
						parametrosSP.p_IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
						seleccOK = true;
					}
					
					if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
					{
						parametrosSP.p_IdPortafolio  = Number($scope.oPortafolio.seleccionado.IdPortafolio);
					}
					else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
					{
						parametrosSP.p_IdPortafolio  = Number($scope.oPortafolio.seleccionado.IdPortafolio);
					}
					else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio === undefined)
					{
						if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
						{
							if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
							{
								parametrosSP.p_IdProductoAgrupador = 0;
							}
							else
							{
								parametrosSP.p_IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
							}
						}
						
						seleccOK = true;
					}
				}
				else
				{
					if ($scope.oConfiguracionCliente.seleccionado.IdCliente > 0)
					{
						parametrosSP.p_IdCliente = $scope.oConfiguracionCliente.seleccionado.IdCliente;
			    		seleccOK = true;
			    		
			    		if ($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio > 0)
			    			parametrosSP.p_IdPortafolio = $scope.oConfiguracionPortafolio.seleccionado.IdPortafolio;
					}
				}
			}
			else
			{
				parametrosSP.p_IdCliente = $scope.IdClienteVentana;
				parametrosSP.p_IdPortafolio = $scope.IdPortafolioVentana;
	    		seleccOk = true;
			}
		}
		catch(e){}
		
		
		if (!seleccOK)
		{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Debe ingresar un cliente/portafolio o un folio de cartera para buscar.");
			return;
		}
		
    	if ($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento != "" && $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento !== undefined)
    		parametrosSP.p_IdClasificacionTipoInstrumento = $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;

    	if ($scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento != "" && $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined)    		
    		parametrosSP.p_IdTipoInstrumento = $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;

    	if ($scope.instrumentoDatos !== undefined){    		
	    	if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined && ($scope.instrumentoDatos.ConteoNemotecnico === undefined || $scope.instrumentoDatos.ConteoNemotecnico === 1))	    		
	    		parametrosSP.p_IdInstrumento = Number($scope.instrumentoDatos.IdInstrumento);
			if ($scope.instrumentoDatos.Nemotecnico != "" && $scope.instrumentoDatos.Nemotecnico !== undefined && $scope.instrumentoDatos.ConteoNemotecnico > 1)
				parametrosSP.p_Nemotecnico = $scope.instrumentoDatos.Nemotecnico;
    	}

    	if ($scope.filtros.moneda != "" && $scope.filtros.moneda !== undefined)
    		parametrosSP.p_IdMoneda = $scope.filtros.moneda;

    	if ($scope.oConfiguracionEstado.seleccionado != "" && $scope.oConfiguracionEstado.seleccionado !== undefined && $scope.oConfiguracionEstado.seleccionado.IdEstado != 0)
    		parametrosSP.p_IdEstado = $scope.oConfiguracionEstado.seleccionado.IdEstado;    	
    	
		dsGrillaCarterasAll.removeAll();
		dsGrillaCarterasAll.setCallParameters(parametrosSP);
    	
	};
	
	
	$scope.seleccionarRegistro = function (){
		var paramOut = {};
		var seleccxx = {};
	
    	seleccxx = $scope.seleccionCarterasAll;
    	
		if (seleccxx != undefined){
			paramOut.FolioCartera = seleccxx.FolioCartera;
			paramOut.IdInstrumento = seleccxx.IdInstrumento;
			paramOut.IdMoneda = seleccxx.IdMoneda;
			paramOut.Nemo = seleccxx.Nemo;
			paramOut.Precio = seleccxx.Precio;
			paramOut.IdBoveda = seleccxx.IdBoveda;
		}
		bcsComunes.ventanaData($scope.idVentana,paramOut);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	
	$scope.consultarCustodia = function (){
		var datos = {};
		var seleccxx = {};
		
    	
    	seleccxx = $scope.seleccionCarterasAll;
    	
		if (seleccxx != undefined){
			datos.IdEmpresa = $scope.SesionIdEmpresa;
			datos.IdSocioDeNegocio =$scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio; 
			datos.IdCliente = seleccxx.IdCliente;
			datos.IdPortafolio = seleccxx.IdPortafolio;
			datos.IdClasificacionTipoInstrumento = seleccxx.IdClasificacionTipoInstrumento;
			datos.IdTipoInstrumento = seleccxx.IdTipoInstrumento;
			datos.IdInstrumento = seleccxx.IdInstrumento;
			datos.IdBoveda = seleccxx.IdBoveda;
			datos.Nemotecnico = seleccxx.Nemotecnico;
			datos.cmbCliente = $scope.oConfiguracionCliente.seleccionado;
			datos.cmbPortafolio = $scope.oConfiguracionPortafolio.seleccionado;

				
      	    var id1 = bcsComunes.ventanaAbrir("ConsultarCustodiaPortafolio",datos,false,false,$scope.idVentana); 
        	var promesa=bcsComunes.ventanaPromesa(id1);                               
        	promesa.then(function(data){
	        	if (data && data.IdInstrumento){
	        		
	        		$scope.instrumentoCustodia = data;
	        		$scope.data.oConfiguracionBoveda.idActual = data.IdBoveda;
		        	$scope.data.kngFolioCartera = $scope.instrumentoCustodia.FolioCartera;
		        	$scope.data.kngCantidad = $scope.instrumentoCustodia.CantidadDisponible;
		        	$scope.data.checkFungible =($scope.instrumentoCustodia.EsFungible ==='S')? true : false;
		        	
		        	$scope.data.kngPrecio = Number($scope.instrumentoCustodia.INGPrecioCompraActual);
		        	$scope.data.kngTasa = Number($scope.instrumentoCustodia.INGTasaCompraActual);
		        	
		        	$scope.data.fechaVencimientoOptions = {min:$scope.instrumentoCustodia.FechaVencimiento};	
					$scope.data.fechaEmisionOptions = {max:$scope.instrumentoCustodia.FechaEmision};
	        	}
	    	});
		} else {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};	
	
	
	$scope.valorizar = function (){
		var elemento = {};
		if($scope.verGrillaAll) elemento = $scope.seleccionCarterasAll;
		if(!elemento){
			console.log("seleccione una cartera");
			return;
		}
		console.log("Desarrollador: Pendiente ventana confirmación");
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		$scope.datosValorizar = {};
		$scope.datosValorizar.p_IdEmpresa				= $scope.$root.SesionIdEmpresa;
		$scope.datosValorizar.p_IdUsuario				= $scope.$root.SesionIdUsuario;
		$scope.datosValorizar.p_IdModulo				= 'CUS';
		$scope.datosValorizar.p_IdPais					= $scope.$root.SesionIdPaisSistema;;
		$scope.datosValorizar.p_FechaAnterior			= null;
		$scope.datosValorizar.p_FechaActual				= $scope.$root.SesionFechaSistema;
		$scope.datosValorizar.p_FechaProximo			= null;
		$scope.datosValorizar.p_ValorizarCarteras		= 'S';
		$scope.datosValorizar.p_ValorizarGarantias		= 'N';
		$scope.datosValorizar.p_ValorizaATasaMercado	= 'S';
		$scope.datosValorizar.p_ValorizaTIF				= 'S';
		$scope.datosValorizar.p_SimularOperacion		= 'N';
		$scope.datosValorizar.p_TicketProcesamiento		= 0;
		$scope.datosValorizar.p_OrigenProcesamiento		= 'CONSULTA_CARTERAS';
		$scope.datosValorizar.p_OmiteValorizarAnteriorWS= 'N';
		$scope.datosValorizar.p_Instrumentos = [{
			"IdClasificacionTipoInstrumento":null,
			"IdTipoInstrumento"				:null,
			"IdInstrumento"					:null,
			"IdEmisor"						:null,
			"FechaVencimiento"				:null,
			"IdTipoReajuste"				:null,
			"EsFungible"					:null,
			"IdBoveda"						:null,
		}];
		$scope.datosValorizar.p_Carteras = [{
			"FolioCartera"					:(elemento.FolioCartera)?elemento.FolioCartera:null,
			"FolioGarantia"					:null,
			"FolioSubyacente"				:null,
			"IdCliente"						:elemento.IdCliente,
			"IdPortafolio"					:elemento.IdPortafolio,
		}];
		
		var folioCartera = String(elemento.FolioCartera);
		bcsServiciosValorizacion.ValorizarCustodiaMasiva($scope.datosValorizar,function(result){	
			console.log("registroValorizado",JSON.stringify(result));
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_VALORIZACION_CORRECTA_CARTERA_FOLIO') + ': ' + folioCartera,
				conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
			bcsComunes.setConflictos(util.parseServerResponse(result));
			$scope.seleccionCarteras = undefined;
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			$scope.buscarCarteras();
		},function(error){
			console.log("registroValorizado",JSON.stringify(error));
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_VALORIZAR_CARTERA_FOLIO') + ': ' + folioCartera,
				conflictos : util.parseServerResponse(error)}, $scope.ventanaId);
			bcsComunes.setConflictos(util.parseServerResponse(error));
			$scope.seleccionCarteras = undefined;
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
	};
	
	
	$scope.limpiarFiltros = function() {
		$scope.oConfiguracionCliente.idActual			= false;						//Limpia el combobox cliente
		$scope.socioGral.idActual                       = false;						//Limpia el combobox cliente
		$scope.oConfiguracionCliente.seleccionado		= -1;							//Limpia el combobox cliente
		$scope.oConfiguracionPortafolio.filtros			= { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };	//Limpia el combobox portafolio
		$scope.oConfiguracionPortafolio.idActual		= false;						//Limpia el combobox portafolio
		$scope.oConfiguracionPortafolio.seleccionado	= false;						//Limpia el combobox portafolio
			
		$scope.oConfiguracionClasificacionTipoInstrumento = "";							//Limpia el combobox mercado
		$scope.oConfiguracionTipoInstrumento.IdClasificacionTipoInstrumento = "";		//Limpia el combobx tipo instrumento
		$scope.oConfiguracionTipoInstrumento.idActual	= false;						//Limpia el combobx tipo instrumento
		$scope.oConfiguracionInstrumento 				= { buscar : "-1" };			//Limpia el combobx tipo instrumento
			
		$scope.filtros.folioCartera						= "";
		$scope.filtros.folioCarteraObj					= "";
		$scope.filtros.moneda							= "";
		$scope.filtros.clasificacionTipoInstrumento		= "";
		$scope.filtros.tipoInstrumento					= "";
		$scope.filtros.accion							= undefined;
		$scope.filtros.concepto							= undefined;
		$scope.filtros.instrumento						= "";
		$scope.filtros.instrumentoObj					= undefined;
		$scope.kngFechaConsulta							= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.chkFechaLiquidacion						= false;						//Limpia el check fecha liq - fecha adq
		
		$scope.oSocioNegocio.idActual					= false;
		$scope.oProducto.idActual						= 0;
		$scope.oPortafolio.idActual						= false;
	}
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
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

	
	$scope.$watch('oSocioNegocio.seleccionado',function(newval,oldval){
		if (!$scope.oSocioNegocio.seleccionado) return;
		try{
			$scope.socioGral.idActual = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			$scope.socioGral.idDefecto = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){
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
	
	$scope.$watch('oConfiguracionClasificacionTipoInstrumento.seleccionado',function(newval,oldval){
		$scope.verGrillaAll = true;
		if(!!$scope.grillaAll && !$scope.grillaAll.refreshed)
			$timeout(function() {
				$scope.grillaAll.refresh();
				$scope.grillaAll.refreshed = true;
			}, 100);
	});	
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});