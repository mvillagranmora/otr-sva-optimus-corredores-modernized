angular.module('ModuloCustodia').controller('ConsultarCustodiaPortafolioCtrl',
		function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma,bcsAlerta,bcsServiciosCustodia)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtros = {};
	$scope.data ={};
	$scope.data.btnHabilitado = false;
	 
	var parametrosConsulta = {};
	
	var conParametros = false;
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	
	(parametros.esVentana != undefined) ? ($scope.esVentana = parametros.esVentana) : ($scope.esVentana = false);

	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	$scope.data.kngFecha				=  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.FechaConsultaOpt		= {/*max:util.dateInicioDia($scope.SesionFechaSistema)*/};
	$scope.data.chkFechaAdquisicion		= true;  //búsqueda por defecto por fecha de liquidación
	$scope.data.requiereClasifica		= true;
	$scope.data.chkAgrupaPorBoveda		= true;
	$scope.data.chkAgrupaPorRelacion	= true;
	
	try{
		if (parametros.IdCliente > 0 || parametros.IdInstrumento || parametros.Nemotecnico){
			conParametros = true;	
		}
		
	}catch (e){
		conParametros = false;
		parametros.IdSocioDeNegocio = 0; 
		parametros.IdCliente = 0;
		parametros.IdPortafolio = 0;
		parametros.IdClasificacionTipoInstrumento = "";
		parametros.IdTipoInstrumento = "";
		parametros.IdInstrumento = 0;
		parametros.IdBoveda = "";
	}
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oConfiguracionCliente = {
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
	
	$scope.data.oConfiguracionPortafolio = {
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.data.oConfiguracionClasificacion = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		idActual : false,
		seleccionado : false
	};
	
	$scope.data.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : parametros.IdClasificacionTipoInstrumento },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.data.oConfiguracionInstrumento = {
		buscar : "-1"
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		//idActual : false,
		//seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  undefined }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
   	
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	/*
	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			if(dataSourceConfiguracion.data()[0].Valor === 'S'){
				$scope.MostrarProducto = true;
			}
		}
 	});
 	*/
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/	
	
	var dsCustPortafolio = new bcs.DataSource({
		type		: "call",
		definition	: {
			//serverFiltering 	: true, // Filtrado de datos en el servidor.
			//serverPaging 		: true, // Paginado de los datos en el servidor.
			//serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			
			pageSize		: 200,
			callTransaction	: "22",
			callResultSet	: "P_OUT_TBL",
			schema : {
				model : {
					fields : {
	        			IdClasificacionTipoInstrumento 		: { type: "string"},
	        			NombreClasificacionTipoInstrumento 	: { type: "string"},
	        			IdTipoInstrumento					: { type: "string"},
	        			NombreTipoInstrumento				: { type: "string"},
	        			ISIN								: { type: "string"},
	        			IdInstrumento						: { type: "number"},
	        			ClasificacionRiesgoInstrumento		: { type: "string"},
	        			Nemotecnico							: { type: "string"},
	        			IdEmisor							: { type: "string"},
	        			IdCliente							: { type: "number"},
	        			DocumentoIdentidad					: { type: "string"},
	        			DocumentoIdentidadRutCavali         : { type: "string"},
	        			NombreCliente						: { type: "string"},
						NombreEjecutivo						: { type: "string"},
	        			CodigoPortafolio					: { type: "string"},
						CodigoPortafolioWS					: { type:"string"},
	        			IdPortafolio						: { type: "number"},
	        			Relacion							: { type: "string"},
	        			IdBoveda							: { type: "string"},
	        			NumeroCuentaBoveda					: { type: "string"},
	        			EsFungible							: { type: "string"},
	        			CantidadCustodia					: { type: "number"},
	        			PPPCustodia							: { type: "number"},
	        			MontoPPPCustodia					: { type: "number"},
	        			ValorPPPCustodia					: { type: "number"},
	        			PrecioMercadoCustodia				: { type: "number"},
	        			TasaMercado 						: { type: "number"},
						TasaMercadoCustodia 				: { type: "number"},
	        			PrecioCompraCustodia				: { type: "number"},
	        			Rentabilidad						: { type: "number"},
	        			ValorMercadoCustodia				: { type: "number"},
	        			MontoMercadoCustodia				: { type: "number"},
	        			DiferenciaMercadoCustodia			: { type: "number"},
	        			IdMonedaInstrReajuste				: { type: "string"},
						EsPagaderoEn						: { type: "string"},
	        			CantidadSimulCom					: { type: "number"},
	        			PPPSimulCom							: { type: "number"},
	        			ValorPPPSimulCom					: { type: "number"},
	        			PrecioMercadoSimulCom				: { type: "number"},
	        			ValorMercadoSimulCom				: { type: "number"},
	        			DiferenciaMercadoSimulCom			: { type: "number"},
	        			CantidadSimulVen					: { type: "number"},
	        			PPPSimulVen							: { type: "number"},
	        			ValorPPPSimulVen					: { type: "number"},
	        			PrecioMercadoSimulVen				: { type: "number"},
	        			ValorMercadoSimulVen				: { type: "number"},
	        			DiferenciaMercadoSimulVen			: { type: "number"},
	        			CantidadPrestamo					: { type: "number"},
	        			PPPPrestamo							: { type: "number"},
	        			ValorPPPPrestamo					: { type: "number"},
	        			PrecioMercadoPrestamo				: { type: "number"},
	        			ValorMercadoPrestamo				: { type: "number"},
	        			DiferenciaMercadoPrestamo			: { type: "number"},
	        			CantidadEmpresti					: { type: "number"},
	        			PPPEmpresti							: { type: "number"},
	        			ValorPPPEmpresti					: { type: "number"},
	        			PrecioMercadoEmpresti				: { type: "number"},
	        			ValorMercadoEmpresti				: { type: "number"},
	        			DiferenciaMercadoEmpresti			: { type: "number"},
	        			CantidadGarantia					: { type: "number"},
	        			PPPGarantia							: { type: "number"},
	        			ValorPPPGarantia					: { type: "number"},
	        			PrecioMercadoGarantia				: { type: "number"},
	        			ValorMercadoGarantia				: { type: "number"},
	        			DiferenciaMercadoGarantia			: { type: "number"},
	        			CantidadPactosV						: { type: "number"},
	        			PPPPactosV							: { type: "number"},
	        			ValorPPPPactosV						: { type: "number"},
	        			PrecioMercadoPactosV				: { type: "number"},
	        			ValorMercadoPactosV					: { type: "number"},
	        			DiferenciaMercadoPactosV			: { type: "number"},
	        			CantidadPactosC						: { type: "number"},
	        			PPPPactosC							: { type: "number"},
	        			ValorPPPPactosC						: { type: "number"},
	        			PrecioMercadoPactosC				: { type: "number"},
	        			ValorMercadoPactosC					: { type: "number"},
	        			DiferenciaMercadoPactosC			: { type: "number"},													        																        		
	        			CantidadReporteVenta				: { type: "number"},
	        			PPPReporteVenta						: { type: "number"},
	        			ValorPPPReporteVenta				: { type: "number"},
	        			PrecioMercadoReporteVenta			: { type: "number"},
	        			ValorMercadoReporteVenta			: { type: "number"},
	        			DiferenciaMercadoReporteVenta		: { type: "number"},													        			
	        			CantidadReporteCompra				: { type: "number"},
	        			PPPReporteCompra					: { type: "number"},
	        			ValorPPPReporteCompra				: { type: "number"},
	        			PrecioMercadoReporteCompra			: { type: "number"},
	        			ValorMercadoReporteCompra			: { type: "number"},
	        			DiferenciaMercadoReporteCompra		: { type: "number"},													        																        			
						CantidadTranomEgr					: { type: "number"},
						CantidadTranomIng					: { type: "number"},
						DividendoEnDinero					: { type: "number"},
	        			CantidadTransIng					: { type: "number"},
	        			CantidadTransEgr					: { type: "number"},
	        			CantidadTransVenDesEgr				: { type: "number"},  // Venta Descubierta
	        			TotalPosicion   					: { type: "number"},
	        			TotalPosicionPresunta 				: { type: "number"},
	        			Producto							: { type: "string"},
	        			RegimenTributario					: { type: "string"},
	        			ValorMercadoCustodiaReajuste		: { type: "number"},
						MontoMercadoTotalCustodia			: { type: "number"}
					}
				}
			}
		}
	});
	
	$scope.columnasCustPortafolio = function() {
		return  [
	         {field: "DocumentoIdentidad",				title: 'UI_CLIENTE',								width: "60px", 	groupable: true	}
	        ,{field: "Producto",						title: 'UI_PRODUCTO',			                    width: "100px", groupable: true	}
	        ,{field: "CodigoPortafolio",				title: 'UI_PORTAFOLIO',								width: "90px", 	groupable: true }
	        ,{field: "DocumentoIdentidadRutCavali",		title: 'UI_RUT_CAVALI', 							width: "100px", groupable: true , 	hidden: true }
	        ,{field: "NombreCliente",					title: 'UI_NOMBRE_CLIENTE',							width: "200px", groupable: true	}
			,{field: "NombreEjecutivo",					title: 'UI_NOMBRE_EJECUTIVO',						width: "200px", groupable: true }
	        ,{field: "Relacion",						title: 'UI_RELACION',								width: "50px", 	groupable: true	}
			,{field: "IdClasificacionTipoInstrumento",	title: 'UI_MERCADO',								width: "60px", 	groupable: true	}
			,{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO_ABR',					width: "60px", 	groupable: true	}
			,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',							width: "100px", groupable: true	}
			,{field: "ISIN", 							title: 'UI_ISIN', 									width: "100px",	groupable: true	,	hidden: true }
			,{field: "IdEmisor",						title: 'UI_EMISOR',									width: "80px", 	groupable: true	}
			,{field: "ClasificacionRiesgoInstrumento",	title: 'UI_LISTA',									width: "50px", 	groupable: true	}
			,{field: "EsFungible",						title: 'UI_FUNGIBLE',								width: "50px" }
			,{field: "IdMonedaInstrReajuste",			title: 'UI_TIPO_REAJUSTE',							width: "60px", 	groupable: true}
			,{field: "EsPagaderoEn",					title: 'UI_MONEDA_PAGO',							width: "60px", 	groupable: true}
			,{field: "IdBoveda",						title: 'UI_BOVEDA',									width: "60px", 	groupable: true}
			,{field: "NumeroCuentaBoveda", 				title: 'UI_NUMERO_CUENTA', 							width: "55px", 	groupable: true}
			,{field: "TotalPosicion",					title: 'UI_TOTAL_CUSTODIA',							width: "100px",		template : " <label> #: util.bcs_formato(TotalPosicion,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "TotalPosicionPresunta",			title: 'UI_TOTAL_PRESUNTO',							width: "100px",	 	template : " <label> #: util.bcs_formato(TotalPosicionPresunta,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PPPCustodia",						title: 'UI_PPP_CUSTODIA',							width: "100px",  	template : " <label> #: util.bcs_formato(PPPCustodia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}			
			,{field: "ValorPPPCustodia",				title: 'UI_VALOR_PPP_CUSTODIA',						width: "100px", 	template : " <label> #: util.bcs_formato(ValorPPPCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "MontoPPPCustodia",				title: 'UI_MONTO_PPP_CUSTODIA',						width: "100px", 	template : " <label> #: util.bcs_formato(MontoPPPCustodia,'GEN','Monto','"+$scope.data.IdMonedaSistema+"','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}	    		
			,{field: "Rentabilidad",					title: 'UI_RENTABILIDAD',							width: "100px",    	template : " <label> #: util.formateaNumero(Rentabilidad,2) #</label>"}
			
			,{field: "CantidadCustodia",				title: 'UI_CUSTODIA_DISPONIBLE',					width: "100px",  	template : " <label> #: util.bcs_formato(CantidadCustodia,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoCustodia",			title: 'UI_PRECIO_MERCADO_CUSTODIA',				width: "100px",  	template : " <label> #: util.bcs_formato(PrecioMercadoCustodia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "TasaMercadoCustodia",				title: 'UI_TASA_MERCADO',							width: "70px",  	template : " <label> #: util.bcs_formato(TasaMercadoCustodia,'GEN','Tasa',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>"}
			,{field: "ValorMercadoCustodia",			title: 'UI_VALOR_MERCADO_CUSTODIA_DISPONIBLE',		width: "100px",  	template : " <label> #: util.bcs_formato(ValorMercadoCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "MontoMercadoCustodia",			title: 'UI_MONTO_MERCADO_CUSTODIA_DISPONIBLE',		width: "100px",  	template : " <label> #: util.bcs_formato(MontoMercadoCustodia,'GEN','Monto','"+$scope.data.IdMonedaSistema+"',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoCustodia",		title: 'UI_DIFERENCIA_MERCADO_TOTAL_CUSTODIA',		width: "100px",  	template : " <label> #: util.bcs_formato(DiferenciaMercadoCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			
			,{field: "DividendoEnDinero",				title: 'UI_DIVIDENDO_EN_DINERO',					width: "100px",		template : " <label> #: util.bcs_formato(DividendoEnDinero,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			
			,{field: "CantidadGarantia",				title: 'UI_GARANTIAS',								width: "100px",  	template : " <label> #: util.bcs_formato(CantidadGarantia,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoGarantia",			title: 'UI_PRECIO_MERCADO_GARANTIAS',				width: "100px",  	template : " <label> #: util.bcs_formato(PrecioMercadoGarantia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoGarantia",			title: 'UI_MONTO_MERCADO_GARANTIAS',				width: "100px",  	template : " <label> #: util.bcs_formato(ValorMercadoGarantia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoGarantia",		title: 'UI_DIFERENCIA_MERCADO_GARANTIAS',			width: "100px",  	template : " <label> #: util.bcs_formato(DiferenciaMercadoGarantia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			
			,{field: "CantidadSimulCom",				title: 'UI_COMPRA_A_PLAZO',						width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(CantidadSimulCom,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoSimulCom",			title: 'UI_PRECIO_MERCADO_COMPRA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulCom,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoSimulCom",			title: 'UI_MONTO_MERCADO_COMPRA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoSimulCom,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoSimulCom",		title: 'UI_DIFERENCIA_MERCADO_COMPRA_A_PLAZO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulCom,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
			,{field: "CantidadSimulVen",				title: 'UI_VENTA_A_PLAZO',						width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(CantidadSimulVen,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoSimulVen",			title: 'UI_PRECIO_MERCADO_VENTA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulVen,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoSimulVen",			title: 'UI_MONTO_MERCADO_VENTA_A_PLAZO',		width: "100px", hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoSimulVen,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoSimulVen",		title: 'UI_DIFERENCIA_MERCADO_VENTA_A_PLAZO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulVen,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
			,{field: "CantidadPrestamo",				title: 'UI_PRESTATARIO',							width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPrestamo,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoPrestamo",			title: 'UI_PRECIO_MERCADO_PRESTATARIO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPrestamo,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoPrestamo",			title: 'UI_MONTO_MERCADO_PRESTATARIO',				width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(ValorMercadoPrestamo,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoPrestamo",		title: 'UI_DIFERENCIA_MERCADO_PRESTATARIO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPrestamo,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
			,{field: "CantidadEmpresti",				title: 'UI_EMPRESTITO',								width: "100px", hidden: true,  	template : " <label> #:  util.bcs_formato(CantidadEmpresti,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoEmpresti",			title: 'UI_PRECIO_MERCADO_EMPRESTITO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoEmpresti,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoEmpresti",			title: 'UI_MONTO_MERCADO_EMPRESTITO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoEmpresti,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoEmpresti",		title: 'UI_DIFERENCIA_MERCADO_EMPRESTITO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoEmpresti,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			
			,{field: "CantidadPactosV",					title: 'UI_VENTAS_CON_PACTO',						width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosV,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoPactosV",			title: 'UI_PRECIO_MERCADO_VENTAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosV,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoPactosV",				title: 'UI_MONTO_MERCADO_VENTAS_CON_PACTO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosV,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoPactosV",		title: 'UI_DIFERENCIA_MERCADO_VENTAS_CON_PACTO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosV,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			
			,{field: "CantidadPactosC",					title: 'UI_COMPRAS_CON_PACTO',						width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosC,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoPactosC",			title: 'UI_PRECIO_MERCADO_COMPRAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosC,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoPactosC",				title: 'UI_MONTO_MERCADO_COMPRAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosC,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoPactosC",		title: 'UI_DIFERENCIA_MERCADO_COMPRAS_CON_PACTO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosC,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
			,{field: "CantidadReporteCompra",			title: 'UI_REPORTE_COMPRA',							width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(CantidadReporteCompra,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoReporteCompra",		title: 'UI_PRECIO_MERCADO_REPORTE_COMPRA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteCompra,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoReporteCompra",		title: 'UI_MONTO_MERCADO_REPORTE_COMPRA',			width: "100px", hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoReporteCompra,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoReporteCompra",	title: 'UI_DIFERENCIA_MERCADO_REPORTE_COMPRA',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteCompra,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}	    		
			
			,{field: "CantidadReporteVenta",			title: 'UI_REPORTE_VENTA',							width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(CantidadReporteVenta,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "PrecioMercadoReporteVenta",		title: 'UI_PRECIO_MERCADO_REPORTE_VENTA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteVenta,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "ValorMercadoReporteVenta",		title: 'UI_MONTO_MERCADO_REPORTE_VENTA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoReporteVenta,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "DiferenciaMercadoReporteVenta",	title: 'UI_DIFERENCIA_MERCADO_REPORTE_VENTA',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteVenta,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}

			,{field: "CantidadTranomEgr",				title: 'UI_COMPRA_NOMINAL',							width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTranomEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTranomIng",				title: 'UI_VENTA_NOMINAL',							width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTranomIng,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
			,{field: "CantidadTransIng",				title: 'UI_INGRESO_TRANSITO',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTransIng,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTransEgr",				title: 'UI_EGRESOS_TRANSITO',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTransEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTransVenDesEgr",			title: 'UI_VENTA_DESCUBIERTA',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTransVenDesEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "RegimenTributario",				title: 'UI_REGIMEN_TRIBUTARIO',						width: "100px", groupable: true	}
			,{field: "CodigoPortafolioWS",				title: 'UI_PORTAFOLIOWS',							width: "60px" }
			,{field: "ValorMercadoCustodiaReajuste",	title: 'UI_VALOR_MERCADO_CUSTODIA_REAJUSTE',		width: "60px",  	template : " <label> #: util.bcs_formato(ValorMercadoCustodiaReajuste,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "MontoMercadoTotalCustodia",		title: 'UI_MONTO_MERCADO_TOTAL_CUSTODIA',			width: "100px",  	template: kendo.template("# if (IdClasificacionTipoInstrumento === 'RV') { #" + "#= util.bcs_formato(MontoMercadoTotalCustodia, 'GEN', 'Monto', '" + $scope.data.IdMonedaSistema + "', '', '').valor #" + "# } else { #" + "#= '-' #" + "# } #" ), groupFooterTemplate: "#= util.formateaNumero(sum,2) #"}
		];
	};
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
   	//GRILLA PRINCIPAL
	var grillaCustPortafolio;
	
	$scope.confGrillaCustPortafolio = {
		dataSource	: dsCustPortafolio,
		columns		: $scope.columnasCustPortafolio(),
		sortable	: true,
		pageable	: true,   //pagina localmente
		filterable 	: true,
		selectable 	: "row",
		reorderable	: true,
		userSettings : true,
		groupable	: true,
		height		: 526,
		allowCopy	: { delimeter: "\t" },
		resizable	: true,
	    autoBind	: false,
	    excel : {
	        allPages: true,
	      },
	    gridCreated : function(grid) {
	    	grillaCustPortafolio = grid;
	    	if ($scope.SesionLocale === 'PE'){
				grid.showColumn("ISIN");
//				grid.showColumn("NumeroCuentaBoveda");
				
				grid.showColumn("CantidadReporteCompra");
	    		grid.showColumn("PrecioMercadoReporteCompra");
	    		grid.showColumn("ValorMercadoReporteCompra");
	    		grid.showColumn("DiferenciaMercadoReporteCompra");	    		
	    		//-----
	    		grid.showColumn("CantidadReporteVenta");
	    		grid.showColumn("PrecioMercadoReporteVenta");
	    		grid.showColumn("ValorMercadoReporteVenta");
	    		grid.showColumn("DiferenciaMercadoReporteVenta");
	    		grid.showColumn("DocumentoIdentidadRutCavali");
			}
	    	if ( $scope.SesionLocale === 'CL' ){
	    		grid.showColumn("CantidadSimulCom");
	    		grid.showColumn("PrecioMercadoSimulCom");
	    		grid.showColumn("ValorMercadoSimulCom");
	    		grid.showColumn("DiferenciaMercadoSimulCom");
				//-----
	    		grid.showColumn("CantidadSimulVen");
	    		grid.showColumn("PrecioMercadoSimulVen");
	    		grid.showColumn("ValorMercadoSimulVen");
	    		grid.showColumn("DiferenciaMercadoSimulVen");
				//-----
	    		grid.showColumn("CantidadPrestamo");
	    		grid.showColumn("PrecioMercadoPrestamo");
	    		grid.showColumn("ValorMercadoPrestamo");
	    		grid.showColumn("DiferenciaMercadoPrestamo");
				//-----
	    		grid.showColumn("CantidadEmpresti");
	    		grid.showColumn("PrecioMercadoEmpresti");
	    		grid.showColumn("ValorMercadoEmpresti");
	    		grid.showColumn("DiferenciaMercadoEmpresti");
	    		//-----
	    		grid.showColumn("CantidadPactosV");
	    		grid.showColumn("PrecioMercadoPactosV");
	    		grid.showColumn("ValorMercadoPactosV");
	    		grid.showColumn("DiferenciaMercadoPactosV");
	    		//-----
	    		grid.showColumn("CantidadPactosC");
	    		grid.showColumn("PrecioMercadoPactosC");
	    		grid.showColumn("ValorMercadoPactosC");
	    		grid.showColumn("DiferenciaMercadoPactosC");
	    		grid.showColumn("RegimenTributario");
	    		grid.hideColumn("DocumentoIdentidadRutCavali");
	    	}
	    },
	    scrollable	: true,
		rowColorBackground: function(row) {
			var color = "black";
			if (row.EsNegativa === 'S') {
				color = "red";
			}
			return color;
		}
	};
   	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.preparaParametros = function(filtros){
		parametrosConsulta = {};
		
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo = "CUS";
		parametrosConsulta.p_IdCliente = null;
		parametrosConsulta.p_IdPortafolio = null;
		parametrosConsulta.p_IdClasificacionTipoInstrumento = "";
		parametrosConsulta.p_IdTipoInstrumento = "";
		parametrosConsulta.p_IdInstrumento = 0;
		parametrosConsulta.p_Nemotecnico = "";
		parametrosConsulta.p_IdBoveda = "";
		parametrosConsulta.p_AgrupaPorBoveda = "N";
		parametrosConsulta.p_IdProductoAgrupador = null;
		parametrosConsulta.p_SolicitaDividendo = "S";
		
		if ($scope.data.chkAgrupaPorBoveda)
			parametrosConsulta.p_AgrupaPorBoveda = "S";
		else
			parametrosConsulta.p_AgrupaPorBoveda = "N";
		
		if ($scope.data.chkAgrupaPorRelacion)
			parametrosConsulta.p_AgrupaPorRelacion = "S";
		else
			parametrosConsulta.p_AgrupaPorRelacion = "N";
		
		if ($scope.data.chkSaldosNegativos)
			parametrosConsulta.p_SaldosNegativos = "S";
		else
			parametrosConsulta.p_SaldosNegativos = "N";
		
		if ($scope.data.chkSaldosNegativosPresuntos)
			parametrosConsulta.p_SaldosNegativosPresuntos = "S";
		else
			parametrosConsulta.p_SaldosNegativosPresuntos = "N";

		if ($scope.data.chkFechaLiquidacion)
			parametrosConsulta.p_TipoFechaConsulta = "LIQ";   // Filtrar por Fecha Liquidación
		else if ($scope.data.chkFechaAdquisicion)
			parametrosConsulta.p_TipoFechaConsulta = "ADQ";   // Filtrar por Fecha Adquisición
		
		
		try{
			if($scope.MostrarProducto)
			{
				if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
					parametrosConsulta.p_IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
				
				if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					parametrosConsulta.p_IdPortafolio  = Number($scope.oPortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					parametrosConsulta.p_IdPortafolio  = Number($scope.oPortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio === undefined)
				{
					if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
					{
						if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
						{
							parametrosConsulta.p_IdProductoAgrupador = 0;
						}
						else
						{
							parametrosConsulta.p_IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
						}
					}
				}
			}
			else
			{
				if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0 && $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio > 0)
				{
					parametrosConsulta.p_IdCliente = $scope.data.oConfiguracionCliente.seleccionado.IdCliente;
					parametrosConsulta.p_IdPortafolio = $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
				}
				else if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0)
				{
					parametrosConsulta.p_IdCliente = $scope.data.oConfiguracionCliente.seleccionado.IdCliente;
					parametrosConsulta.p_IdPortafolio = 0;
				}
				else
				{
					parametrosConsulta.p_IdCliente = 0;
					parametrosConsulta.p_IdPortafolio = 0;
				}
			}
		}catch(e){
			parametrosConsulta.p_IdCliente = 0;
			parametrosConsulta.p_IdPortafolio = 0;
		}
		
		try{
			if ($scope.data.oConfiguracionClasificacion.seleccionado.Codigo !== undefined)
				parametrosConsulta.p_IdClasificacionTipoInstrumento = $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento;
		}catch(e){}
		
		try{
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== 0)
				parametrosConsulta.p_IdTipoInstrumento = $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
		}catch (e){}
		
		try {
			if ($scope.data.instrumentoDatos.IdInstrumento > 0 && ($scope.data.instrumentoDatos.ConteoNemotecnico === undefined || $scope.data.instrumentoDatos.ConteoNemotecnico === 1) )
				parametrosConsulta.p_IdInstrumento = $scope.data.instrumentoDatos.IdInstrumento;
			else
				parametrosConsulta.p_IdInstrumento = 0;
				
			if ($scope.data.instrumentoDatos.Nemotecnico != "" && $scope.data.instrumentoDatos.ConteoNemotecnico > 1)
				parametrosConsulta.p_Nemotecnico = $scope.data.instrumentoDatos.Nemotecnico;
			else
				parametrosConsulta.p_Nemotecnico = "";
		}catch(e){}
		
		parametrosConsulta.p_FechaConsulta = util.dateSPdate($scope.data.kngFecha);
		
		return parametrosConsulta;
	};
	
	$scope.buscar = function (){
		var params = {};
		params.p_IdEmpresa	= $scope.SesionIdEmpresa; 
		params.p_IdUsuario	= $scope.SesionIdUsuario;
		params.p_IdModulo	= "AGE";
		params.p_ListaParametros = [{moduloParametro: "PLATAFORMA", nombreParametro : "PLA.FechaGoLive" }];
		
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		
		bcsServiciosCustodia.ConsultarFechaCorteHistorica(params,
			function(response){		
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				{
					$scope.buscarActual();
				}	
			},
			function(response){
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
				$scope.buscarActual();
			}
		);
		
	};
	
	
	
	$scope.buscarActual = function (){
		dsCustPortafolio.setCallParameters($scope.preparaParametros($scope.data));
		if ($scope.data.chkAgrupaPorBoveda){
			grillaCustPortafolio.showColumn("IdBoveda");
//				if ($scope.SesionLocale === 'PE')
//					grillaCustPortafolio.showColumn("NumeroCuentaBoveda");
		} else {
			grillaCustPortafolio.hideColumn("IdBoveda");
//				if ($scope.SesionLocale === 'PE')
//					grillaCustPortafolio.hideColumn("NumeroCuentaBoveda");
		}
		
		if ($scope.data.chkAgrupaPorRelacion)
		{
			grillaCustPortafolio.showColumn("Relacion");  //muestra columna "Relación"
		}
		else
		{
			grillaCustPortafolio.hideColumn("Relacion");  //oculta columna "Relación"
		}
		
		$scope.seleccionCustPortafolio = false;
	};
	
	$scope.limpiarFiltros = function() {
		//CLIENTE
		$scope.data.socioGral.idActual = false;
		$scope.data.oConfiguracionCliente.seleccionado = false;
		//PORTAFOLIO
		$scope.data.oConfiguracionPortafolio.filtros = { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionPortafolio.seleccionado = false;
		//SEPARA POR BOVEDA
		$scope.data.chkAgrupaPorBoveda = true;
		//SEPARA POR RELACION
		$scope.data.chkAgrupaPorRelacion = true;
		//MERCADO
		$scope.data.oConfiguracionClasificacion.idActual = 0;
		$scope.data.oConfiguracionClasificacion.seleccionado = 0;
		//TIPO INSTRUMENTO
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.seleccionado = 0;
		//INSTRUMENTO
		$scope.data.oConfiguracionInstrumento.buscar = "-1";
		//FECHA CONSULTA
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		//FECHA LIQUIDACION
		$scope.data.chkFechaLiquidacion = false;
		//FECHA ADQUISICION
		$scope.data.chkFechaAdquisicion = true;
		//SALDOS NEGATIVOS
		$scope.data.chkSaldosNegativos = false;
		//SALDOS NEGATIVOS PRESUNTOS
		$scope.data.chkSaldosNegativosPresuntos = false;
		//OTROS
		$scope.seleccionCustPortafolio = false;
		$scope.oProducto.idActual	= 0;

		dsCustPortafolio.removeAll();
	}
		
	//------------------------------------------------------------
	// FUNCIONES PARA CONSULTA DE CUSTODIA DESDE OTRAS UI
	//------------------------------------------------------------
	//Setea algunos filtro de acuerdo a los parametros enviados	
	$scope.setearFiltros = function(parametros)
	{
		$scope.data.oConfiguracionCliente.idActual = parametros.IdSocioDeNegocio;
		$scope.data.oConfiguracionClasificacion.idPostFiltro = parametros.IdClasificacionTipoInstrumento;
		$scope.data.oConfiguracionTipoInstrumento.idPostFiltro = parametros.IdTipoInstrumento;
		if (parametros.Nemotecnico !== '' && parametros.Nemotecnico !== undefined)
			$scope.data.oConfiguracionInstrumento.buscar		   = parametros.Nemotecnico;    		
		if (parametros.FechaConsulta !== '' && parametros.FechaConsulta !== undefined)
		    $scope.data.kngFecha 	=  util.dateInicioDia(parametros.FechaConsulta);
		if (parametros.TipoFechaConsulta)
		    $scope.data.chkFechaLiquidacion =  parametros.TipoFechaConsulta === 'LIQ' ? true : false;
		
	};
	
	
	//Busca custodia portafolio según parametros enviados desdo otra UI
	$scope.buscarSegunParametros = function(parametros)
	{
		var objParams = {};
		objParams.p_IdEmpresa 			= $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		objParams.p_IdUsuario 			= $scope.SesionIdUsuario;
		objParams.p_IdModulo 			= "CUS";
		objParams.p_IdCliente 			= parametros.IdCliente;
		objParams.p_IdPortafolio 		= parametros.IdPortafolio;
		objParams.p_IdClasificacionTipoInstrumento = parametros.IdClasificacionTipoInstrumento;
		objParams.p_IdTipoInstrumento 	= parametros.IdTipoInstrumento;
		objParams.p_IdInstrumento		= Number(parametros.IdInstrumento);
		objParams.p_IdBoveda 			= parametros.IdBoveda ? parametros.IdBoveda : "";
		objParams.p_AgrupaPorBoveda 	= parametros.agrupaPorBoveda ? parametros.agrupaPorBoveda : "";
		
		if (parametros.FechaConsulta !== '' && parametros.FechaConsulta !== undefined)
			objParams.p_FechaConsulta = parametros.FechaConsulta;
			
		if (parametros.TipoFechaConsulta)
			objParams.p_TipoFechaConsulta = parametros.TipoFechaConsulta;
			
		dsCustPortafolio.setCallParameters(objParams);
		dsCustPortafolio.read();
	};
	

	//Funcion de controlador padre(para traspasar datos entre controladores hijos)
	if ($scope.blotterPadre !== undefined)
	{	
		$scope.blotterPadre.data.buscarCustodiaPortafolio = function(parametros) {		
			setTimeout(function(){ 
				$scope.setearFiltros(parametros);
	    	}, 80);
			
			setTimeout(function(){
				$scope.buscarSegunParametros(parametros)
			}, 400);
		};
	};
	
	if (conParametros){
		setTimeout(function(){ 
			$scope.setearFiltros(parametros);
    	}, 80);
		
		setTimeout(function(){			
			$scope.buscarSegunParametros(parametros)
		}, 400);
    }	
	
	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		grillaCustPortafolio.select(grillaCustPortafolio.tbody.find(">tr"));
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		grillaCustPortafolio.clearSelection();
	};
	
	
	//***********************************************
	// Despliega ventana Detalle Flujo de Instrumento buscando de acuerdo a los datos de la operación seleccionada en la grilla
	//***********************************************
	$scope.verCustodiaFlujoInstrumento = function(seleccionCustPortafolio) {
		var objParametros = {};

		if(seleccionCustPortafolio.IdClasificacionTipoInstrumento ==="IF" && seleccionCustPortafolio.IdTipoInstrumento ==="PAG"){
		            if(seleccionCustPortafolio.IdMonedaInstrReajuste ==="CLP"){
		            	objParametros.Nemotecnico					= "PAGARE NR";
		            }else{
		            	objParametros.Nemotecnico					= "PAGARE R";
		            }
		}else{
			//nemo tal cual
			objParametros.Nemotecnico				= seleccionCustPortafolio.Nemotecnico;

		}
		    objParametros.IdCliente									= seleccionCustPortafolio.IdCliente;
			objParametros.IdPortafolio								= seleccionCustPortafolio.IdPortafolio;
			objParametros.IdClasificacionTipoInstrumento			= seleccionCustPortafolio.IdClasificacionTipoInstrumento;
			objParametros.IdTipoInstrumento							= seleccionCustPortafolio.IdTipoInstrumento;
			objParametros.IdInstrumento								= seleccionCustPortafolio.IdInstrumento;
			objParametros.FechaConsulta								= util.dateSPdate($scope.data.kngFecha);
			objParametros.IdEmisor									= seleccionCustPortafolio.IdEmisor;
			objParametros.TipoReajuste								= seleccionCustPortafolio.IdMonedaInstrReajuste;
			objParametros.FechaVencimiento							= seleccionCustPortafolio.FechaVencimiento;
			objParametros.IdBoveda									= seleccionCustPortafolio.IdBoveda;	
			bcsComunes.ventanaAbrir("ConsultarFlujoInstrumento", objParametros, false);
	};
	
	//***********************************************
	// Despliega ventana Detalle Precio Promedio buscando de acuerdo a los datos de la operación seleccionada en la grilla
	//***********************************************
	$scope.verCustodiaPrecioPromedio = function(seleccionCustPortafolio) {
		var objParametros = {};
		objParametros.IdPortafolio					= seleccionCustPortafolio.IdPortafolio;
		objParametros.IdClasificacionTipoInstrumento= seleccionCustPortafolio.IdClasificacionTipoInstrumento;
		objParametros.IdInstrumento					= seleccionCustPortafolio.IdInstrumento;
		objParametros.IdBoveda						= seleccionCustPortafolio.IdBoveda;
		objParametros.IdTipoInstrumento				= seleccionCustPortafolio.IdTipoInstrumento;
		objParametros.IdMonedaInstrumento			= seleccionCustPortafolio.IdMonedaInstrReajuste !='' ? seleccionCustPortafolio.IdMonedaInstrReajuste : $rootScope.SesionIdMonedaSistema;
		objParametros.FechaConsulta					= util.dateSPdate($scope.data.kngFecha);
		objParametros.Nemotecnico					= seleccionCustPortafolio.Nemotecnico;
		
		bcsComunes.ventanaAbrir("ConsultarDetallePrecioPromedio", objParametros, false);
	};
	
/******************************************************/
/***  WATCH											***/
/******************************************************/

	$scope.$watch('data.oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.data.oConfiguracionCliente.seleccionado) return;
		
		try {
			if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0 ) {
				$scope.data.requiereClasifica = false;
				$scope.data.socioGral.idActual = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
				$scope.data.socioGral.idDefecto = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			} else {
				$scope.data.requiereClasifica = true;
				$scope.data.socioGral.idActual = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
				$scope.data.socioGral.idDefecto = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			}
			
		} catch (e){
			$scope.data.requiereClasifica = true;;
		}

	});
	
	$scope.$watch('oSocioNegocio.seleccionado',function(newval,oldval){
		if (!$scope.oSocioNegocio.seleccionado) return;
		
		try{
			$scope.data.socioGral.idActual = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			$scope.data.socioGral.idDefecto = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){}
		
		//$scope.data.btnHabilitado = true;
	});	

	$scope.$watch('data.chkFechaLiquidacion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.data.chkFechaAdquisicion = false;  //desclikea check fecha adquisicion
				break;
			case false: 
				$scope.data.chkFechaAdquisicion = true;				
				break;
		}				
	});
	
	$scope.$watch('data.chkFechaAdquisicion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.data.chkFechaLiquidacion = false; //desclikea check fecha liquidacion				
				break;
				
			case false: 
				$scope.data.chkFechaLiquidacion = true;
				break;  
		}
	});
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});
