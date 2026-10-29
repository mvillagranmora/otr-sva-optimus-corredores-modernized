angular.module('ModuloCustodia').controller('ConsultarCustodiaInstrPendientesCtrl',
function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma,bcsAlerta)
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
	  
	$scope.data.kngFecha				=  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.FechaConsultaOpt		= {/*max:util.dateInicioDia($scope.SesionFechaSistema)*/};
	$scope.data.chkFechaLiquidacion		= true;  //búsqueda por defecto por fecha de liquidación
	$scope.data.chkAgrupaPorRelacion	= true;

	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	try{
		if (parametros.IdCliente > 0 || parametros.IdInstrumento || parametros.Nemotecnico)
			conParametros = true;
	}catch (e){
		conParametros = false;
		parametros.IdSocioDeNegocio = 0; 
		parametros.IdCliente = 0;
		parametros.IdPortafolio = 0;
		parametros.IdClasificacionTipoInstrumento = "";
		parametros.IdTipoInstrumento = "";
		parametros.IdInstrumento = 0;
		parametros.p_IdProductoAgrupador = 0;
		//parametros.IdBoveda = "";
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
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.data.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : parametros.IdClasificacionTipoInstrumento },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoDerivados						: { "field" : "CodigoTipoInstrumento",              "operator": "neq", "value" : "DER" }
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
			pageSize		: 100,
			callTransaction	: "4290642",
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
						NombreCliente						: { type: "string"},
						CodigoPortafolio					: { type: "string"},
						IdPortafolio						: { type: "number"},
						Relacion							: { type: "string"},
						EsFungible							: { type: "string"},
						IdMonedaInstrReajuste				: { type: "string"},
						EsPagaderoEn						: { type: "string"},
						CantidadTransIng					: { type: "number"},
						CantidadTransEgr					: { type: "number"},
						Producto							: { type: "string"},
						TotalPosicion   					: { type: "number"},
	        			TotalPosicionPresunta 				: { type: "number"},
	        			CantidadCustodia					: { type: "number"},
	        			CantidadGarantia					: { type: "number"},
	        			CantidadPrestamo					: { type: "number"},
	        			CantidadEmpresti					: { type: "number"},
	        			CantidadPactosV						: { type: "number"},
	        			CantidadPactosC						: { type: "number"},
	        			CantidadTranomEgr					: { type: "number"},
						CantidadTranomIng					: { type: "number"},
						CantidadTransVenDesEgr				: { type: "number"},  // Venta Descubierta
						CantidadSimulCom					: { type: "number"},
						CantidadSimulVen					: { type: "number"},
						CantidadTransitoIng					: { type: "number"},
						CantidadTransitoEgr					: { type: "number"},
					}
				}
			}
		}
	});
	
	
	$scope.columnasCustPortafolio = function() {
		return  [
	         {field: "DocumentoIdentidad",				title: 'UI_CLIENTE',								width: "60px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "Producto",						title: 'UI_PRODUCTO',			                    width: "100px", groupable: true }
	        ,{field: "CodigoPortafolio",				title: 'UI_PORTAFOLIO',								width: "50px", 	groupable: true }
	        ,{field: "NombreCliente",					title: 'UI_NOMBRE',									width: "120px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
	        ,{field: "Relacion",						title: 'UI_RELACION',								width: "50px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "IdClasificacionTipoInstrumento",	title: 'UI_MERCADO',								width: "60px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO_ABR',					width: "60px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',							width: "100px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "ISIN", 							title: 'UI_ISIN', 									width: "100px",	groupable: true,	attributes:{style:"white-space:nowrap;"},	hidden: true }
			,{field: "IdEmisor",						title: 'UI_EMISOR',									width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "ClasificacionRiesgoInstrumento",	title: 'UI_LISTA',									width: "50px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
			,{field: "EsFungible",						title: 'UI_FUNGIBLE',								width: "50px" }
			,{field: "IdMonedaInstrReajuste",			title: 'UI_MONEDA_TIPO_REAJUSTE',					width: "60px", 	groupable: true}
			,{field: "CantidadTransitoIng",				title: 'UI_INGRESO',								width: "100px", attributes:{"class":"green-cell",style:"text-align:right;white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, filterable:false, template : " <label> #: util.bcs_formato(CantidadTransitoIng,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
			,{field: "CantidadTransitoEgr",				title: 'UI_RETIRO',									width: "100px", attributes:{"class":"red-cell",style:"text-align:right;white-space:nowrap"}, headerAttributes:{style:"text-align: left;"}, filterable:false, template : " <label> #: util.bcs_formato(CantidadTransitoEgr,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
			,{field: "TotalPosicion",					title: 'UI_TOTAL_CUSTODIA',							width: "100px"}
			,{field: "TotalPosicionPresunta",			title: 'UI_TOTAL_PRESUNTO',							width: "100px"}
			,{field: "CantidadCustodia",				title: 'UI_CUSTODIA_DISPONIBLE',					width: "100px"}
			,{field: "CantidadGarantia",				title: 'UI_GARANTIAS',								width: "100px"}
			,{field: "CantidadPrestamo",				title: 'UI_PRESTATARIO',							width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPrestamo,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadEmpresti",				title: 'UI_EMPRESTITO',								width: "100px", hidden: true,  	template : " <label> #:  util.bcs_formato(CantidadEmpresti,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadPactosV",					title: 'UI_VENTAS_CON_PACTO',						width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosV,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadPactosC",					title: 'UI_COMPRAS_CON_PACTO',						width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosC,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTranomEgr",				title: 'UI_COMPRA_NOMINAL',							width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTranomEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTranomIng",				title: 'UI_VENTA_NOMINAL',							width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTranomIng,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTransVenDesEgr",			title: 'UI_VENTA_DESCUBIERTA',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"},  template : " <label> #: util.bcs_formato(CantidadTransVenDesEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadSimulCom",				title: 'UI_COMPRA_A_PLAZO',							width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(CantidadSimulCom,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadSimulVen",				title: 'UI_VENTA_A_PLAZO',							width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(CantidadSimulVen,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			,{field: "CantidadTransIng",				title: 'UI_INGRESO_TRANSITO',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"}, filterable:false, template : " <label> #: util.bcs_formato(CantidadTransIng,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
			,{field: "CantidadTransEgr",				title: 'UI_EGRESOS_TRANSITO',						width: "100px", attributes:{style:"text-align:right;white-space:nowrap; background-color:Lightgray"}, headerAttributes:{style:"text-align: left; background-color:Lightgray"}, filterable:false, template : " <label> #: util.bcs_formato(CantidadTransEgr,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
			
			
		];
	};
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.confGrillaCustPendientes = {
		dataSource	: dsCustPortafolio,
		columns		: $scope.columnasCustPortafolio(),		
		sortable	: true,
		pageable	: { pageSize : 25 },   //pagina localmente
		filterable 	: true,
		selectable 	: "row",
		reorderable	: false,
		userSettings : true,
		groupable	: true,
		height		: 526,
		allowCopy	: { delimeter: "\t" },
		resizable	: true,
	    autoBind	: false,
	    gridCreated : function(grid) {

	    	grillaCustPortafolio = grid;	
 
	    	if($scope.MostrarProducto === true){
		    	grid.showColumn("NombreProductoAgrupador");
	    		dsCustPortafolio.read();
	    	}

	    	if ($scope.SesionLocale === 'PE'){
				grid.showColumn("ISIN");
				grid.showColumn("NumeroCuentaBoveda");
			}
	    },
	    scrollable	: true
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
		parametrosConsulta.p_IdProductoAgrupador = null;
		
		if ($scope.data.chkAgrupaPorRelacion)
			parametrosConsulta.p_AgrupaPorRelacion = "S";
		else
			parametrosConsulta.p_AgrupaPorRelacion = "N";

		if ($scope.data.chkFechaLiquidacion)
		{
			parametrosConsulta.p_TipoFechaConsulta = "LIQ";   // Filtrar por Fecha Liquidación
		}
		else if ($scope.data.chkFechaMovimiento)
		{
			parametrosConsulta.p_TipoFechaConsulta = "ADQ";   // Filtrar por Fecha Adquisición
		}

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
			if($scope.data.oConfiguracionClasificacion.seleccionado.Codigo !== undefined)
				parametrosConsulta.p_IdClasificacionTipoInstrumento = $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento;
		}catch(e){}
		
		try{
			if($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== 0)
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
		dsCustPortafolio.setCallParameters($scope.preparaParametros($scope.data));
		
		if($scope.data.chkAgrupaPorRelacion)
			grillaCustPortafolio.showColumn("Relacion");  //muestra columna "Relación"
		else
			grillaCustPortafolio.hideColumn("Relacion");  //oculta columna "Relación"
	};
	
	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionCliente.idActual = false;
		$scope.data.oConfiguracionCliente.seleccionado = false;
		
		$scope.data.oConfiguracionPortafolio.filtros = { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionPortafolio.seleccionado = false;
		
		$scope.data.oConfiguracionClasificacion.idPostFiltro = false;
		$scope.data.oConfiguracionClasificacion.idActual = false;
		$scope.data.oConfiguracionClasificacion.seleccionado = false;
		
    	$scope.data.oConfiguracionTipoInstrumento.idPostFiltro = false;
    	$scope.data.oConfiguracionTipoInstrumento.idActual = false;
    	$scope.data.oConfiguracionTipoInstrumento.seleccionado = false;
    	
    	$scope.data.oConfiguracionInstrumento.buscar = "-1";
    	
		$scope.oSocioNegocio.idActual						= false;
		$scope.oProducto.idActual							= false;
		$scope.oPortafolio.idActual							= false;
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
		//objParams.p_IdBoveda 			= "";
		
		if (parametros.FechaConsulta !== '' && parametros.FechaConsulta !== undefined)
			objParams.p_FechaConsulta = parametros.FechaConsulta;
		
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
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('oSocioNegocio.seleccionado',function(newval,oldval){
		if (!$scope.oSocioNegocio.seleccionado) return;
		try{
			$scope.socioGral.idActual = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			$scope.socioGral.idDefecto = $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
		}catch(e){}
	});
	
	
	$scope.$watch('data.chkFechaLiquidacion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.data.chkFechaMovimiento = false;  //desclikea check fecha adquisicion
				break;
			case false: 
				$scope.data.chkFechaMovimiento = true;				
				break;
		}				
	});
	
	$scope.$watch('data.chkFechaMovimiento',function(newval,oldval){
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