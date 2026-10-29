angular.module('ModuloCustodia').controller('ConsultarCartolaMovimientosCtrl', 
function($scope,$timeout,$modal,$state,bcsComunes, $rootScope, bcsServicioPlataforma, $translate)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtro = {};
	$scope.data = {};
	var parametrosConsulta = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.filtro.fecInicio = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTermino = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
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
	
	$scope.data.oConfiguracionConceptoCustodia = {
		tipo : "combobox-concepto-custodia",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionBoveda = {
		tipo : "combobox-boveda",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoOperacionCustodia = {
		tipo : "combobox-tipo-operacion-custodia",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
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
			NoDerivados						: { "field" : "IdClasificacionTipoInstrumento", "operator": "neq", "value" : "DV" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
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
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   : undefined }
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
	
	
	var dsGrillaCartola = new bcs.DataSource({
		type		: "call",
		definition 	: {
			callTransaction	: "1724",
			callResultSet	: "P_OUT_TABLA",
			sort			: { field: "FolioCartera", dir: "desc" },
			pageSize        : 200,
			schema			: {
				model : {
					fields : {                                                                                                                                                             
	        			 IdCartera 						: { type: "number"}
						,FechaRegistro 					: { type: "sysdate"}
						,INGFechaOperacion				: { type: "sysdate"}
						,FechaOperacion 				: { type: "sysdate"}
						,TipoOperacion 					: { type: "string"}
						,IdCliente 						: { type: "number"}
						,IdPortafolio 					: { type: "number"}
						,CodigoPortafolio 				: { type: "string"}
						,NombreCliente 					: { type: "string"}
						,DocumentoIdentidad 			: { type: "string"}
						,IdConcepto 					: { type: "string"}
						,NombreConcepto 				: { type: "string"}
						,IdClasificacionTipoInstrumento : { type: "string"}
						,IdTipoInstrumento 				: { type: "string"}
						,IdInstrumento 					: { type: "number"}
						,ISIN							: { type: "string"}
						,Nemotecnico 					: { type: "string"}
						,IdEmisor 						: { type: "string"}
						,FechaLiquidacion 				: { type: "sysdate"}
						,FechaVencimientoInstr			: { type: "sysdate"}
						,IdTipoReajuste 				: { type: "string"}
						,NemoSVS 						: { type: "string"}
						,IdMonedaInstr 					: { type: "string"}
						,IdBoveda 						: { type: "string"}
						,Cantidad 						: { type: "number"}
						,Precio	 						: { type: "number"}
						,Tasa		 					: { type: "number"}
						,Monto	 						: { type: "number"}
						,ValorRescate					: { type: "number"}
						,Saldo 							: { type: "number"}
						,FolioCartera					: { type: "number"}
						,IdTipoComprobante 				: { type: "string"}
						,FolioComprobante				: { type: "number"}
						,FechaComprobante				: { type: "sysdate"}
						,NumeroCuentaBoveda				: { type: "string"}
						,NombreTipoCustodia				: { type: "string"}
						,Producto						: { type: "string"}
						,Spread							: { type: "number"}
						,SpreadMercado					: { type: "number"}
						,Accion							: { type: "string"}
					}
				}
			},
			sort: [ { field: "FechaRegistro", dir: "desc" } ]               
		}
	});
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.confGridCartola = {
		dataSource			: dsGrillaCartola,
		columns 			: [
			 {field: "FolioCartera", 					title: 'UI_FOLIO_CARTERA_ABR', 		width: "65px",	groupable: true }
		 	,{field: "FechaOperacion", 					title: 'UI_FECHA_OPERACION',  		width: "70px"}
    		,{field: "Accion",							title: 'UI_OPERACION',				width: "65px",	groupable: true }
			,{field: "TipoOperacion",	 				title: 'UI_TIPO_OPERACION', 		width: "100px", groupable: true, 	attributes: {style:"white-space:nowrap"} }
    		,{field: "NombreConcepto",	 				title: 'UI_CONCEPTO', 				width: "100px", groupable: true, 	attributes: {style:"white-space:nowrap"} }
    		,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_MERCADO', 				width: "50px",	groupable: true }
    		,{field: "IdTipoInstrumento", 				title: 'UI_TIPO_INSTRUMENTO',		width: "50px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "Nemotecnico", 					title: 'UI_INSTRUMENTO', 			width: "100px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "ISIN", 							title: 'UI_ISIN', 					width: "100px",	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "IdEmisor", 						title: 'UI_EMISOR', 				width: "80px",  groupable: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "FechaVencimientoInstr", 			title: 'UI_FECHA_VENCIMIENTO_ABR', 	width: "80px"}
    		,{field: "Cantidad", 						title: 'UI_CANTIDAD',  				width: "80px", 	template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaInstr,'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
    		,{field: "Saldo", 							title: 'UI_SALDO_CARTERA', 			width: "80px",	template : " <label> #: util.bcs_formato(Saldo,'GEN','Cantidad',IdMonedaInstr,'','').valor #</label>"}
    		,{field: "IdMonedaInstr", 					title: 'UI_MONEDA_TIPO_REAJUSTE', 	width: "63px"}
    		,{field: "IdBoveda", 						title: 'UI_BOVEDA_ABR', 			width: "57px",	groupable: true,}
    		,{field: "NumeroCuentaBoveda", 				title: 'UI_NUMERO_CUENTA', 			width: "55px", 	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "Precio", 							title: 'UI_PRECIO', 				width: "80px",	template : " <label> #: (util.bcs_formato(Precio,'GEN','Precio',IdMonedaInstr,IdTipoInstrumento,'')).valor #</label>" }
    		,{field: "Tasa",	 						title: 'UI_TASA', 					width: "60px",	template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMonedaInstr,IdTipoInstrumento,'')).valor #</label>" }
    		,{field: "Monto", 							title: 'UI_MONTO', 			 		width: "90px", 	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaInstr,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
    		,{field: "ValorRescate", 					title: 'UI_VALOR_RESCATE', 	 		width: "90px", 	template : " <label> #: (util.bcs_formato(ValorRescate,'GEN','Monto',IdMonedaInstr,'','')).valor #</label>" }
    		,{field: "Spread", 							title: 'UI_SPREAD', 				width: "80px",	template : " <label> #: (util.bcs_formato(Spread,'GEN','Monto',IdMonedaInstr,'','')).valor #</label>" }
    		,{field: "SpreadMercado",					title: 'UI_SPREAD_MERCADO', 		width: "80px",	template : " <label> #: (util.bcs_formato(SpreadMercado,'GEN','Monto',IdMonedaInstr,'','')).valor #</label>" }
			,{field: "IdTipoComprobante", 				title: 'UI_TIPO_COMPROBANTE', 		width: "60px", 	groupable: true }
    		,{field: "FolioComprobante", 				title: 'UI_FOLIO_COMPROBANTE', 		width: "50px", 	groupable: true }
    		,{field: "FechaComprobante", 				title: 'UI_FECHA_COMPROBANTE', 		width: "80px", 	groupable: true }
    		,{field: "NombreCliente", 					title: 'UI_CLIENTE', 				width: "120px", groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
    		,{field: "DocumentoIdentidad", 				title: 'UI_RUT', 					width: "70px",  groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
    		,{field: "CodigoPortafolio", 				title: 'UI_PORTAFOLIO', 			width: "50px",  groupable: true, }
			,{field: "NombreTipoCustodia", 				title: 'UI_TIPO_CUSTODIA', 			width: "50px",  groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}
    		,{field: "Producto", 						title: 'UI_PRODUCTO',		        width: "80px",  hidden: true}
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
		gridCreated 		: function(grid) {		

	    	if($scope.MostrarProducto === true){
	    		grid.showColumn("NombreProductoAgrupador");
	    	}

	    	if ($scope.SesionLocale === 'PE'){
	    		grid.showColumn("ISIN");
	    		grid.showColumn("NumeroCuentaBoveda");
	    		grid.showColumn("NombreTipoCustodia");
	    	}
	    }
	};

/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.preparaParametros = function(){
		parametrosSP = {};
		
		parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosSP.p_IdClasificacionTipoInstrumento = "";
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;
		parametrosSP.p_Nemotecnico = "";
		parametrosSP.p_IdBoveda = "";
		parametrosSP.p_TipoOperacion = "";
		parametrosSP.p_FolioCartera = 0;
		parametrosSP.p_IdConcepto = "";
		
		parametrosSP.p_IdCliente = 0;
		parametrosSP.p_IdPortafolio = 0;
		parametrosSP.p_IdProductoAgrupador = null;
		
		var seleccOk = false;
		var auxDate1;
    	var auxDate2;
		
		try{
			if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null){
    			auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
    			parametrosSP.p_FechaInicio =  auxDate1;
			} else {
				seleccOk = false;
			}
		}catch(e){
			seleccOk = false;
		}
		
		try{
			if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null){
    			auxDate2 =  util.dateOdata(util.date($scope.filtro.fecTermino).hours(0).minutes(0).seconds(0).toDate());
    			parametrosSP.p_FechaTermino = auxDate2;
			} else {
				seleccOk = false;
			}
		}catch(e){
			seleccOk = false;
		}
		
		if(auxDate2 > auxDate1)
			seleccOk = true;
		
		var timeDiff = Math.abs(auxDate2 - auxDate1);
		var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24));
		
		if (diffDays > 31)
		{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_PERIODO_SELECCIONADO_SUPERA_31_DIAS"));
    	 	return;
		}
		
		try{
			if($scope.MostrarProducto)
			{
				if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
				{
					parametrosSP.p_IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
					seleccOK = true;
				}
				
				if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					parametrosSP.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
				}
				else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
				{
					parametrosSP.p_IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
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
				if ($scope.data.oConfiguracionCliente.seleccionado.IdCliente > 0)
				{
					parametrosSP.p_IdCliente = $scope.data.oConfiguracionCliente.seleccionado.IdCliente;
					seleccOk = true;
				}
				
				if ($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio > 0)
					parametrosSP.p_IdPortafolio = $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
			}
		}catch(e){}
		
		try{
			if ($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento !== undefined && $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento != "") {
				parametrosSP.p_IdClasificacionTipoInstrumento = $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;
			}			
		}catch(e){}
		
		try{
			if ($scope.filtro.accion.Accion == "INGRESO" || $scope.filtro.accion.Accion == "RETIRO") {
				parametrosSP.p_TipoOperacion = $scope.filtro.accion.Accion;
			}else {
				parametrosSP.p_TipoOperacion = "";
			}
		}catch(e){
			parametrosSP.p_TipoOperacion = "";
		}
		
		try{
			if ($scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined && $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento != "0") {
				parametrosSP.p_IdTipoInstrumento = $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
			}
		}catch (e){}
		
		try {
			if ($scope.instrumentoDatos !== undefined){ 
				if ($scope.instrumentoDatos.IdInstrumento > 0 && ($scope.instrumentoDatos.ConteoNemotecnico === undefined || $scope.instrumentoDatos.ConteoNemotecnico === 1) ) {
					parametrosSP.p_IdInstrumento = Number($scope.instrumentoDatos.IdInstrumento);
				}
					
				if ($scope.instrumentoDatos.Nemotecnico != "" && $scope.instrumentoDatos.ConteoNemotecnico > 1) {
					parametrosSP.p_Nemotecnico = $scope.instrumentoDatos.Nemotecnico;
				}
			}
		}catch(e){}
		
		try{
			if ($scope.filtro.concepto !== undefined && $scope.filtro.concepto !== null && $scope.filtro.concepto.IdConcepto.length > 0){
				parametrosSP.p_IdConcepto = $scope.filtro.concepto.IdConcepto;
			}
		}catch(e){}
		
		try{
			if ($scope.filtro.boveda !== undefined && $scope.filtro.boveda !== null && $scope.filtro.boveda.IdBoveda.length > 0){
				parametrosSP.p_IdBoveda = $scope.filtro.boveda.IdBoveda;
			}
		}catch(e){}
		
		try {
			if ($scope.filtro.folioCartera !== undefined && $scope.filtro.folioCartera !=='' && $scope.filtro.folioCartera !==0) {
				parametrosSP.p_FolioCartera = Number($scope.filtro.folioCartera);
				seleccOk = true
			}
		}catch(e){}
		
		
		var filtros = [{
			"IdCliente"							: parametrosSP.p_IdCliente,
			"IdPortafolio"						: parametrosSP.p_IdPortafolio,
			"IdClasificacionTipoInstrumento"	: parametrosSP.p_IdClasificacionTipoInstrumento,
			"IdTipoInstrumento"					: parametrosSP.p_IdTipoInstrumento,
			"IdInstrumento"						: parametrosSP.p_IdInstrumento,
			"Nemotecnico"						: parametrosSP.p_Nemotecnico,
			"FolioCartera"						: parametrosSP.p_FolioCartera
		}];
		
		parametrosConsulta = {};
		parametrosConsulta.p_IdEmpresa			= $scope.SesionIdEmpresa;
		parametrosConsulta.p_IdUsuario			= $scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo			= "CUS";
		parametrosConsulta.p_FechaInicio		= util.dateSPdate($scope.filtro.fecInicio);
		parametrosConsulta.p_FechaTermino		= util.dateSPdate($scope.filtro.fecTermino);
		parametrosConsulta.p_VariablesProceso	= filtros;
		parametrosConsulta.p_IdProductoAgrupador= parametrosSP.p_IdProductoAgrupador;
		parametrosConsulta.p_TipoOperacion		= parametrosSP.p_TipoOperacion;
		parametrosConsulta.p_IdConcepto			= parametrosSP.p_IdConcepto;
		parametrosConsulta.p_IdBoveda			= parametrosSP.p_IdBoveda;
		parametrosConsulta.p_UIConsulta			= "UI_MOV_CAR";
		
		return parametrosConsulta;
	};
	
	
	$scope.buscarMovimientos = function(){
		dsGrillaCartola.setCallParameters($scope.preparaParametros());
	};
	
	
	$scope.limpiarFiltros = function(){
		$scope.filtro.Portafolio							= "";
		$scope.filtro.cliente								= undefined;
		$scope.filtro.PortafolioObjeto						= undefined;
		$scope.filtro.clasificacionTipoInstrumento			= "";
		$scope.filtro.tipoInstrumento						= "";
		$scope.filtro.accion								= undefined;
		$scope.filtro.concepto								= undefined;
		$scope.filtro.buscarinstrumento						= "-1";
		$scope.data.oConfiguracionCliente.idActual			= false;
		$scope.data.oConfiguracionPortafolio.filtros		= { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
		$scope.data.oConfiguracionPortafolio.idActual		= false;
		$scope.data.oConfiguracionPortafolio.seleccionado	= false;
		$scope.filtro.fecInicio								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTermino							= util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.oSocioNegocio.idActual						= false;
		$scope.oProducto.idActual									= 0;
		$scope.oPortafolio.idActual							= false;
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('data.oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.data.oConfiguracionCliente.seleccionado) return;
		
		try{
			$scope.data.socioGral.idActual = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			$scope.data.socioGral.idDefecto = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
		}catch(e){}
	});	
	
	
	$scope.$watch('oSocioNegocio.seleccionado',function(newval,oldval){
		if (!$scope.oSocioNegocio.seleccionado) return;
		
		try{
			$scope.data.socioGral.idActual = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			$scope.data.socioGral.idDefecto = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){}
	});	
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});