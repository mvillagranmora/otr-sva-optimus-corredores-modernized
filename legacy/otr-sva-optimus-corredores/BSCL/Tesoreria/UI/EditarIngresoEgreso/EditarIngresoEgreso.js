angular.module('ModuloTesoreria').controller('EditarIngresoEgresoCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsServiciosTesoreria,bcsBlotterComprobantesGrid)
{
	
	$scope.data = {};
	$scope.gridApi = {};
	
	//Recupera parametros desde la ventana recibida
	$scope.IdVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	$scope.objIngresoEgreso = $.extend({},$scope.parametros.oIngresoEgreso);	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

	$scope.forms = {};
	$scope.objFormaDePago = [];    //Para enviar datos a directiva bcsFormasDePago
	$scope.objFormaDePago[0] = {};
	$scope.objFormaDePagoTraspaso = [];
	$scope.objFormaDePagoTraspaso[0] = {};
	

	//INFORMACION DEL TIPO DE EMPLEADO
	var TipoEmpleadoDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport	: {read:  {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1752",dataType: "json"}},
			schema 		: {model: {fields : {TipoEmpleado		: { type: "string"}}}},		
			filter		: [{ "field": "IdUsuario",	"operator": "eq", "value" : $rootScope.SesionIdUsuario}]
		}
	});
	TipoEmpleadoDatasource.query().then(function(){
		if (typeof TipoEmpleadoDatasource.data()[0] !== 'undefined'){
			$scope.TipoEmpleado = TipoEmpleadoDatasource.data()[0].TipoEmpleado;
			//BACKOFFICE (empleados que no son ejecutivos ni supervisores) //Los ejecutivos no tienen una caja asociada
			$scope.data.showGenerarComprobante =  $scope.TipoEmpleado === 'BACKOFFICE' ? true : false;
		}
	});
	
	//SETEO INICIAL DE PAGINA
	if ($scope.objIngresoEgreso.IdIngresoEgreso === undefined)
	{
		$scope.accion = 'CREAR';
		$scope.data.showIngresar = true;
		$scope.data.showModificar = false;
	    $scope.data.agregarFormaPagoAutomatica = 'S';  // S: propone preferencias de pago pre-configuradas
	}
	else
	{
		$scope.accion = 'MODIFICAR';
		$scope.data.showIngresar = false;
		$scope.data.showModificar = true;

		$scope.data.showGenerarComprobante =  false;
		$scope.data.agregarFormaPagoAutomatica = 'N';  // N: No propone preferencias de pago pre-configuradas
	}
	
	
	var IdIngresoEgresoFormaDePago       	= 0;  //Id referencial(solo para edición)
	var IdIngresoEgresoFormaDePagoEditada 	= 0;  //Id referencial(solo para edición)
	var IdIngresoEgresoFormaDePagoTraspaso       	= 0;  //Id referencial(solo para edición)
	var IdIngresoEgresoFormaDePagoEditadaTraspaso 	= 0;  //Id referencial(solo para edición)
	var sumPagos 							= 0;
	
	$scope.data.agruparEnComprobante 		= false;  	// Por defecto queda inactivo el checkBox "Agrupar en Comprobante"
	$scope.data.titularPago 				= false;   	// Por defecto queda activo el checkBox	
	$scope.data.EnableComprobante 			= true;	// por defecto se deja el checkbox desactivado 
	$scope.data.titularPagoTraspaso			= false;
	$scope.data.esEdicion      				= false;
	
	$scope.data.showBtnAgregar 				= true;
	$scope.data.showBtnEditar 				= false;	
	$scope.data.showBtnCancelar				= false;	
	$scope.data.titularPagoDisabled 		= true;
	$scope.data.titularPagoTraspasoDisabled = true;
	$scope.data.showDatosSocio 				= true;
	$scope.data.showDatosOperacion 			= true;
	$scope.data.showDatosSocioTraspaso 		= true;
	$scope.data.showDatosOperacionTraspaso 	= true;
	$scope.objFormaDePago[0].Monto  		= 0;
	$scope.objFormaDePagoTraspaso[0].Monto  = 0;
	$scope.data.showPortafolio				= true;
	$scope.data.showPortafolioRelacionado	= false;
	$scope.data.showPortafolioRelacionadoTraspaso = false;
		
	
	$scope.data.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaOperacion 	= util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.data.tabActivo = {
		tabDatosBasicos       :  true,
		tabFormaDePago        :  false,
		tabDatosReferenciales :  false
	};	
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	IdProductoAgrupador = 9999;
	
	//------------------------------------------------
	// CONFIGURACION GRILLA FORMA DE PAGO
	//------------------------------------------------
	 
	var dsDefFormaDePago = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgresoFormaDePago;
	if ($scope.accion == 'MODIFICAR')
	{
		dsDefFormaDePago.definition.filter = [
		                                        { field: "IdIngresoEgreso", operator: "eq", value: $scope.objIngresoEgreso.IdIngresoEgreso },
		                                        { field: "IdEstado", operator: "neq", value: "MOD" }
											 ];
	}  
	
	
	var dsFormaDePago = new bcs.DataSource(dsDefFormaDePago);	
	var dsFormaDePagoTraspaso = new bcs.DataSource(dsDefFormaDePago);
	
	$scope.confBasicaIngresoEgresoFormaDePago = {
			dataSource			: dsFormaDePago,
			columns			    : bcsBlotterComprobantesGrid.columnasIngresoEgresoFormaDePago(),		
			sortable			: true,
			pageable			: false,
			autoBind			: $scope.accion == 'MODIFICAR' ? true : false,
			height				: 124,
			selectable 			: "row",
			userSettings		: true,
		    gridCreated 		: function(g) { $scope.gridApi = g; ObtenerSaldoPorAsignar();}
	};
	
	$scope.confBasicaIngresoEgresoFormaDePagoTraspaso = {
			dataSource			: dsFormaDePagoTraspaso,
			columns			    : bcsBlotterComprobantesGrid.columnasIngresoEgresoFormaDePago(),		
			sortable			: true,
			pageable			: false,
			autoBind			: false,
			height				: 124,
			selectable 			: "row",
			userSettings		: true,
		    gridCreated 		: function(g) { $scope.gridApiTraspaso = g; ObtenerSaldoPorAsignar();}
	};
	
	
	//-----------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-----------------------------------------------------

	$scope.data.oConfiguracionTipoMovimiento={
		tipo 			: "combobox-tipo-movimiento",
		configuracion 	: {},
		filtros 		: {
							IdModulo      : { "field"   : "IdModulo","operator": "eq", "value"   : "TES" },				
							IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ($scope.objIngresoEgreso.IdTipoMovimiento || 'MOVING'),
		idPostFiltro  	: false	
	};
	$scope.data.oConfiguracionTipoSocioNegocio={
		tipo 			: "combobox-tipo-socio-negocio",
		configuracion 	: {},
		filtros 		: {},
		idActual 		: false,
		seleccionado 	: '',
		idDefecto 		: ($scope.objIngresoEgreso.IdTipoPersona || 'CLI'),
		idPostFiltro  	: 'CLI',
		idActualTraspaso: false,
		seleccionadoTraspaso:false,
		idPostFiltroTraspaso:false
	};
	

	//Filtros para combo Concepto Tesoreria
	$scope.FiltroNombreEntidad		   		= { "field" : "NombreEntidad",        "operator": "eq", "value" : "TES.IngresoEgreso" };
	$scope.FiltroIdEstado			   		= { "field" : "IdEstado",             "operator": "eq", "value" : "VIG" };
	$scope.FiltroDesplegarEnComboBox   		= { "field" : "DesplegarEnComboBox",  "operator": "eq", "value" : "S" };
	$scope.FiltroDisponibleIngresoManual   	= { "field" : "DisponibleIngresoManual",  "operator": "eq", "value" : "S" };
	$scope.FiltroTipoConceptoTesoreria 		= { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //IN|EG
	$scope.FiltroTipoConceptoTesoreriaVacio = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //vacio es para IN y EG 
	$scope.FiltroTipoConceptoTesoreriaAmbos = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "AM" }; //
	$scope.FiltroVisiblePorEjecutivoA  		= { "field" : "VisiblePorEjecutivo",  "operator": "eq", "value" : "" };  
	$scope.FiltroVisiblePorEjecutivoB  		= { "field" : "VisiblePorEjecutivo",  "operator": "eq", "value" : "" };  //BACKOFFICE ve todos los conceptos
	$scope.VisibleOptimus   	            = { "field" : "VisibleOptimus",  "operator": "eq", "value" : "S" };
	$scope.FiltroTipoConceptoTesoreriaTraspaso 		= { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //IN|EG
	$scope.FiltroTipoConceptoTesoreriaVacioTraspaso = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //vacio es para IN y EG 
	$scope.FiltroTipoConceptoTesoreriaAmbosTraspaso = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "AM" }; //
	$scope.data.oConfiguracionConcepto={
			tipo 			: "combobox-concepto-tesoreria",
			configuracion 	: {},			
			filtros : {
				complejo : [$scope.FiltroNombreEntidad,$scope.FiltroIdEstado,$scope.FiltroDesplegarEnComboBox,
				            $scope.FiltroDisponibleIngresoManual,$scope.FiltroTipoConceptoTesoreria,$scope.FiltroTipoConceptoTesoreriaVacio,$scope.FiltroTipoConceptoTesoreriaAmbos,
				            $scope.FiltroVisiblePorEjecutivoA,$scope.FiltroVisiblePorEjecutivoB],				
			  	logic: "and",
			  	filters: [
				{
				    logic: "or",
				    filters: [$scope.FiltroTipoConceptoTesoreria,$scope.FiltroTipoConceptoTesoreriaVacio,$scope.FiltroTipoConceptoTesoreriaAmbos]
				},
				$scope.FiltroNombreEntidad,
				$scope.FiltroIdEstado,
				$scope.FiltroDesplegarEnComboBox,
				$scope.FiltroDisponibleIngresoManual,
				$scope.VisibleOptimus,
				{
				    logic: "or",
				    filters: [$scope.FiltroVisiblePorEjecutivoA,$scope.FiltroVisiblePorEjecutivoB]
				}
			  ]},
			  filtrosTraspaso : {
					complejo : [$scope.FiltroNombreEntidad,$scope.FiltroIdEstado,$scope.FiltroDesplegarEnComboBox,
					            $scope.FiltroDisponibleIngresoManual,$scope.FiltroTipoConceptoTesoreriaTraspaso,$scope.FiltroTipoConceptoTesoreriaVacioTraspaso,$scope.FiltroTipoConceptoTesoreriaAmbosTraspaso,
					            $scope.FiltroVisiblePorEjecutivoA,$scope.FiltroVisiblePorEjecutivoB],				
				  	logic: "and",
				  	filters: [
					{
					    logic: "or",
					    filters: [$scope.FiltroTipoConceptoTesoreriaTraspaso,$scope.FiltroTipoConceptoTesoreriaVacioTraspaso,$scope.FiltroTipoConceptoTesoreriaAmbosTraspaso]
					},
					$scope.FiltroNombreEntidad,
					$scope.FiltroIdEstado,
					$scope.FiltroDesplegarEnComboBox,
					$scope.FiltroDisponibleIngresoManual,
					$scope.VisibleOptimus,
					{
					    logic: "or",
					    filters: [$scope.FiltroVisiblePorEjecutivoA,$scope.FiltroVisiblePorEjecutivoB]
					}
				  ]},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro  	: false,
			idActualTraspaso 		: false,
			seleccionadoTraspaso 	: false,
			idDefectoTraspaso 		: '',
			idPostFiltroTraspaso  	: false
	};	

	
	
	$scope.data.oConfiguracionCaja={
		tipo 			: "combobox-caja-moneda-usuario",
		configuracion 	: {},
		filtros 		: {
							 IdEmpresa 			 : { "field": "IdEmpresa",       	"operator": "eq", "value" : $scope.SesionIdEmpresa },
							 IdUsuario 			 : { "field": "IdUsuario",       	"operator": "eq", "value" : $scope.SesionIdUsuario },
							 IdMoneda     		 : { "field": "IdMoneda",     		"operator": "eq", "value" : "" },
							 IdEstadoCaja 		 : { "field": "IdEstadoCaja", 		"operator": "eq", "value" : "ABT" },						
							 IdEstadoCajaMoneda  : { "field": "IdEstadoCajaMoneda", 	"operator": "eq", "value" : "ACT" },
							 IdEstadoCajaUsuario : { "field": "IdEstadoCajaUsuario", "operator": "eq", "value" : "ACT" },				
							 FechaInicio    	 : { "field": "FechaInicio",     	"operator": "lte", "value" : $scope.SesionFechaSistema },
							 FechaTermino   	 : { "field": "FechaTermino",     	"operator": "gte", "value" : $scope.SesionFechaSistema },		

						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ($scope.objIngresoEgreso.IdCaja || 0),
		idPostFiltro  	: false				
	};
	$scope.data.oConfiguracionMoneda={
		tipo 			: "combobox-moneda-tesoreria",
		configuracion 	: {},
		filtros 		: {
							IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ($scope.objIngresoEgreso.IdMoneda || 0),
		idPostFiltro  	: false				
	};	
	$scope.data.oConfiguracionSocioNegocio={
		tipo 			: "combobox-socio-negocio",
		configuracion 	: {},
		filtros			: {
							IdEmpresa     		: { "field"   : "IdEmpresa",         "operator": "eq", "value"   : $scope.SesionIdEmpresa },
							EstadoPersona 		: { "field"   : "EstadoPersona",     "operator": "eq", "value"   : "ACT"},
							EsCliente     		: { "field"   : "EsCliente",         "operator": "eq", "value"   : "" },
							EstadoCliente 		: { "field"   : "EstadoCliente",     "operator": "eq", "value"   : ""},			
							EsProveedor   		: { "field"   : "EsProveedor",       "operator": "eq", "value"   : "" },
							EstadoProveedor 	: { "field"   : "EstadoProveedor",   "operator": "eq", "value"   : ""},
							EsEmpleado    		: { "field"   : "EsEmpleado",        "operator": "eq", "value"   : "" },
							EstadoEmpleado 		: { "field"   : "EstadoEmpleado",    "operator": "eq", "value"   : ""},
							EsContraparte 		: { "field"   : "EsContraparte",     "operator": "eq", "value"   : "" },
							EstadoContraparte 	: { "field"   : "EstadoContraparte", "operator": "eq", "value"   : ""},
							IdPersona 	        : { "field"   : "IdPersona",         "operator": "eq", "value"   : 0 },
						  },
	  filtrosTraspaso	: {
			IdEmpresa     		: { "field"   : "IdEmpresa",         "operator": "eq", "value"   : $scope.SesionIdEmpresa },
			EstadoPersona 		: { "field"   : "EstadoPersona",     "operator": "eq", "value"   : "ACT"},
			EsCliente     		: { "field"   : "EsCliente",         "operator": "eq", "value"   : "" },
			EstadoCliente 		: { "field"   : "EstadoCliente",     "operator": "eq", "value"   : ""},			
			EsProveedor   		: { "field"   : "EsProveedor",       "operator": "eq", "value"   : "" },
			EstadoProveedor 	: { "field"   : "EstadoProveedor",   "operator": "eq", "value"   : ""},
			EsEmpleado    		: { "field"   : "EsEmpleado",        "operator": "eq", "value"   : "" },
			EstadoEmpleado 		: { "field"   : "EstadoEmpleado",    "operator": "eq", "value"   : ""},
			EsContraparte 		: { "field"   : "EsContraparte",     "operator": "eq", "value"   : "" },
			EstadoContraparte 	: { "field"   : "EstadoContraparte", "operator": "eq", "value"   : ""},
			IdPersona 	        : { "field"   : "IdPersona",         "operator": "eq", "value"   : 0 },
		  },
		idActual 		: false,
		seleccionado 	: false,		
		idDefecto 		: ($scope.objIngresoEgreso.IdPersona || 0),
		idPostFiltro  	: false,	
		itemPostFiltro	: false,
		idActualTraspaso: false,
		seleccionadoTraspaso: false,		
		idDefectoTraspaso: false,
		idPostFiltroTraspaso: false,	
		itemPostFiltroTraspaso: false
	};
	$scope.data.oProducto = {
		filtros: {
			IdProductoAgrupador : {"field": "IdProductoAgrupador","operator": "neq", "value"   :  0 }
		}
	};
	$scope.data.oConfiguracionPortafolio={
		tipo 			: "combobox-portafolio",
		configuracion 	: {},
		filtros			: {
							IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
							IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'}
						  },
		filtrosAPV		: {
							IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
							IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'},
							IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
						  },				  
	    filtrosTraspaso	: {
			IdCliente 		: { "field"	: "IdCliente","operator": "eq", "value"   	:  0 },
			IdEstado 		: { "field"	: "IdEstado","operator": "eq", "value"   	:  'ACT'}
	    				},
		idActual 		: false,
		seleccionado 	: false,
		autoCargar		: ($scope.objIngresoEgreso.IdPortafolio ? true : false),
		idActualTraspaso: false,
		seleccionadoTraspaso: false,
		autoCargarTraspaso: false,
		idDefectoTraspaso: 0,
		idPostFiltroTraspaso: false	
	};
	$scope.data.oConfiguracionSocioNegocioRelacionado={
		tipo 			: "combobox-relacionado",
		configuracion 	: {},
		filtros			: {
							IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
							IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
							IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
						  },
		filtrosTraspaso	: {
				IdEmpresa		: { "field"   : "IdEmpresa",		"operator": "eq", "value"   : $scope.SesionIdEmpresa },
				IdEstado 		: { "field"   : "IdEstado",			"operator": "eq", "value"   : "ACT"},
				IdPersonaOrigen	: { "field"   : "IdPersonaOrigen",	"operator": "eq", "value"   : 0},
			  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ($scope.objIngresoEgreso.IdPersonaTitularPago || 0),
		idPostFiltro  	: false,	
		itemPostFiltro	: false,
		idActualTraspaso: false,
		seleccionadoTraspaso: false,
		idDefectoTraspaso : 0,
		idPostFiltroTraspaso : false,	
		itemPostFiltroTraspaso: false
	};
	$scope.data.oConfiguracionPortafolioRelacionado={
		tipo 			: "combobox-portafolio",
		configuracion 	: {},
		filtros			: {
							IdCliente 	: { "field"	: "IdCliente","operator": "eq", "value"   :  0 },
							IdEstado 	: { "field"	: "IdEstado","operator": "eq", "value"   :  'ACT'}
						  },
	    filtrosTraspaso	: {
			IdCliente 	: { "field"	: "IdCliente","operator": "eq", "value"   :  0 },
			IdEstado 	: { "field"	: "IdEstado","operator": "eq", "value"   :  'ACT'}
		  },
		idActual 		: false,
		seleccionado 	: false,
		autoCargar		: false, //($scope.objIngresoEgreso.IdPortafolioTitularPago && $scope.objIngresoEgreso.TitularPago === 'S' ? true : false),
		idDefecto 		: 0, //($scope.objIngresoEgreso.IdPortafolioTitularPago && $scope.objIngresoEgreso.TitularPago === 'S' ? $scope.objIngresoEgreso.IdPortafolioTitularPago : 0),
		idPostFiltro  	: 0,
		idActualTraspaso: false,
		seleccionadoTraspaso: false,
		autoCargarTraspaso: false, //($scope.objIngresoEgreso.IdPortafolioTitularPago && $scope.objIngresoEgreso.TitularPago === 'S' ? true : false),
		idDefectoTraspaso: false, //($scope.objIngresoEgreso.IdPortafolioTitularPago && $scope.objIngresoEgreso.TitularPago === 'S' ? $scope.objIngresoEgreso.IdPortafolioTitularPago : 0),
		idPostFiltroTraspaso: false
	};	
	$scope.data.oConfiguracionTipoProducto={
		tipo 			: "combobox-tipo-producto",
		configuracion 	: {},
		filtros 		: {
							IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
							IdTipoProducto : { "field"   : "IdTipoProducto","operator": "neq", "value"   : "CUST" },
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro  	: false				
	};
	$scope.data.oConfiguracionTipoOperacion={
		tipo 			: "combobox-tipo-operacion",
		configuracion 	: {},
		filtros 		: {
							EstadoTipoOperacionTipoInstrumento : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value"   : "ACT" },
							IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value"   : "" },
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro  	: false				
	};
	$scope.data.oConfiguracionCondicionLiquidacion={
		tipo : "combobox-condicion-liquidacion",
		configuracion 	: {},
		filtros 		: {
							IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
							EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro  	: false				
	};
	
	var cargarDatosParaEdicion = function(oIngresoEgreso)
	{	
		
		$scope.data.NombresocioDeNegocio								= oIngresoEgreso.NombrePersona;
		$scope.data.NombePortafolio										= oIngresoEgreso.NombrePortafolio;
		$scope.data.NombreConcepto										= oIngresoEgreso.NombreConcepto;
		$scope.data.oConfiguracionTipoMovimiento.idPostFiltro 			= oIngresoEgreso.IdTipoMovimiento;		
		$scope.data.oConfiguracionTipoSocioNegocio.idPostFiltro 		= oIngresoEgreso.IdTipoPersona;
	    $scope.data.oConfiguracionMoneda.idPostFiltro 					= oIngresoEgreso.IdMoneda;
	    $scope.data.oConfiguracionCaja.idPostFiltro 					= oIngresoEgreso.IdCaja;	    
	    $scope.data.oConfiguracionConcepto.idPostFiltro 				= oIngresoEgreso.IdConcepto;
		$scope.data.kngMonto 											= oIngresoEgreso.Monto;
		$scope.data.kngFechaLiquidacion 								= util.dateInicioDia(oIngresoEgreso.FechaLiquidacion);
		$scope.data.ngSecuenciaAgrupacion 								= oIngresoEgreso.SecuenciaAgrupacion;
		$scope.data.descripcion 										= oIngresoEgreso.Glosa;
		$scope.data.agruparEnComprobante                        		= oIngresoEgreso.AgrupaComprobante === 'SI' ? true : false;
		$scope.data.titularPago							       	 		= oIngresoEgreso.TitularPago === 'S' ? true : false;
		
		
		if (oIngresoEgreso.IdTipoMovimiento === 'MOVEGR')
		{	//MOVEGR
		
			$scope.data.titularPagoDisabled 								= true;  //check desabilitado	
			if (oIngresoEgreso.TitularPago === 'S'){
				$scope.data.oConfiguracionSocioNegocioRelacionado.idPostFiltro	= oIngresoEgreso.IdPersonaTitularPago;
				$scope.data.oConfiguracionPortafolioRelacionado.idPostFiltro	= oIngresoEgreso.IdPortafolioTitularPago;
				$scope.data.oConfiguracionPortafolioRelacionado.idDefecto 		= oIngresoEgreso.IdPortafolioTitularPago;
				$scope.data.NombeSocioRelacionado								= oIngresoEgreso.NombrePersonaTitularPago;
				$scope.data.NombePortafolioRelacionado							= oIngresoEgreso.NombrePortafolioTitularPago;
			}
		}
		else
		{
			$scope.data.titularPagoDisabled 								= true;  //check desabilitado	
		}
		
		//Datos Referenciales
		$scope.data.oConfiguracionTipoProducto.idPostFiltro     		= oIngresoEgreso.IdTipoProducto;
		$scope.data.oConfiguracionTipoOperacion.idPostFiltro    		= oIngresoEgreso.IdTipoOperacion;
		$scope.data.ngFolioOperacion                            		= oIngresoEgreso.IdOperacion;
		$scope.data.kngFechaOperacion 						    		= util.dateInicioDia(oIngresoEgreso.FechaOperacion);		
		$scope.data.oConfiguracionCondicionLiquidacion.idPostFiltro 	= oIngresoEgreso.IdCondicionLiquidacion;
		$scope.data.indicadorBilateral                          		= oIngresoEgreso.EsBilateral == 'S' ? true : false; 

		
		$scope.data.oConfiguracionSocioNegocio.idDefecto	            = oIngresoEgreso.IdSocioDeNegocio;		
		$scope.data.oConfiguracionSocioNegocio.filtros.IdPersona.value  = oIngresoEgreso.IdPersona;
		$scope.data.oConfiguracionSocioNegocio.idPostFiltro 			= oIngresoEgreso.IdPersona;
		$scope.data.oConfiguracionSocioNegocio.filtros.read 			= true;	
	    $scope.data.oConfiguracionPortafolio.idPostFiltro 				= oIngresoEgreso.IdPortafolio;
	    
		//Bloquea datos no editables
		$scope.data.oConfiguracionConcepto.activado						= false;
		$scope.data.oConfiguracionTipoMovimiento.activado 				= false;		
		$scope.data.oConfiguracionTipoSocioNegocio.activado 			= false;
	    $scope.data.oConfiguracionSocioNegocio.activado 				= false;
	    $scope.data.oConfiguracionPortafolio.activado 					= false;
	    
	    //Estos datos no se editan(para la edición se modifican tal cual)
	    $scope.data.IdTipoComprobanteOrigen 							= oIngresoEgreso.IdTipoComprobanteOrigen;
	    $scope.data.IdComprobanteOrigen 								= oIngresoEgreso.IdComprobanteOrigen;
	    $scope.data.FechaComprobanteOrigen 								= oIngresoEgreso.FechaComprobanteOrigen;
	    $scope.data.FechaLiquidacionComprobanteOrigen 					= oIngresoEgreso.FechaLiquidacionComprobanteOrigen;	
	};


    // ----------------------------------------------
	// BLOQUE WATCH (INICIO)
	// ----------------------------------------------    
	//Al cambiar "Tipo Socio Negocio" actualiza listado de socios de negocio
	$scope.$watch('data.oConfiguracionTipoSocioNegocio.seleccionado',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== 'undefined' && 
			$scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== false &&
			$scope.accion !== 'MODIFICAR')
		{	
			$scope.setarSocioNegocio();
			$scope.data.oConfiguracionSocioNegocio.idActual = false;
			$scope.data.oConfiguracionPortafolio.idActual 	= false;

			$scope.data.oConfiguracionPortafolio.activado = false;
			/////$scope.data.generarComprobante = true;

			var IdTipoSocioDeNegocio = $scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio
			switch (IdTipoSocioDeNegocio)
			{				
				case "CLI":   //Cliente
					$scope.data.oConfiguracionSocioNegocio.filtros.EsCliente.value      = 'UI_SI';
					$scope.data.oConfiguracionSocioNegocio.filtros.EstadoCliente.value  = 'ACT';
					$scope.data.oConfiguracionPortafolio.activado = true;
					$scope.data.showPortafolio=true;
					/////$scope.data.generarComprobante = false;
					break;

				case "EMP":   //Empleado
					$scope.data.oConfiguracionSocioNegocio.filtros.EsEmpleado.value     = 'UI_SI';
					$scope.data.oConfiguracionSocioNegocio.filtros.EstadoEmpleado.value = 'ACT';
					$scope.data.showPortafolio= false;
					$scope.data.oConfiguracionPortafolio.activado = false;
					
					break;					

				case "PRO":   //Proveedor					
					$scope.data.oConfiguracionSocioNegocio.filtros.EsProveedor.value     = 'UI_SI';
					$scope.data.oConfiguracionSocioNegocio.filtros.EstadoProveedor.value = 'ACT';
					$scope.data.showPortafolio= false;			
					break;

				case "CNT":   //Contraparte									
					$scope.data.oConfiguracionSocioNegocio.filtros.EsContraparte.value     = 'UI_SI';
					$scope.data.oConfiguracionSocioNegocio.filtros.EstadoContraparte.value = 'ACT';
					$scope.data.showPortafolio= false;
					$scope.data.oConfiguracionPortafolio.activado = false;
					break;
			}
		}
	});
	
	$scope.$watch('data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso !== 'undefined' && 
				$scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso !== false &&
				$scope.accion !== 'MODIFICAR')
		{	
				$scope.setarSocioNegocioTraspaso();
				$scope.data.oConfiguracionSocioNegocio.idActualTraspaso = false;
				$scope.data.oConfiguracionPortafolio.idActualTraspaso 	= false;

				$scope.data.oConfiguracionPortafolio.activadoTraspaso = false;
				var IdTipoSocioDeNegocioTraspaso = $scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso.IdTipoSocioDeNegocio
				switch (IdTipoSocioDeNegocioTraspaso)
				{				
					case "CLI":   //Cliente
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsCliente.value      = 'UI_SI';
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoCliente.value  = 'ACT';
						$scope.data.oConfiguracionPortafolio.activadoTraspaso = true;
						/////$scope.data.generarComprobante = false;
						break;
		
					case "EMP":   //Empleado
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsEmpleado.value     = 'UI_SI';
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoEmpleado.value = 'ACT';
						break;					
		
					case "PRO":   //Proveedor					
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsProveedor.value     = 'UI_SI';
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoProveedor.value = 'ACT';					
						break;
		
					case "CNT":   //Contraparte									
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsContraparte.value     = 'UI_SI';
						$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoContraparte.value = 'ACT';
						break;
				}
		}
	});

	$scope.$watch('data.oConfiguracionSocioNegocio.seleccionado',function(newval,oldval){
		if ($scope.accion === 'MODIFICAR'){
			return;
		}

		if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionado !== false &&
		typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoSocioNegocio.seleccionado !== false)
		{
			if ($scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio === 'CLI' && 
				$scope.data.oConfiguracionSocioNegocio.seleccionado.EsCliente === 'UI_SI'){
				
				$scope.data.oConfiguracionPortafolio.filtros.IdCliente.value = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
				$scope.data.oConfiguracionPortafolio.filtrosAPV.IdCliente.value = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;	
				if($scope.MostrarProducto){
					$scope.data.oConfiguracionPortafolio.filtrosAPV.IdProductoAgrupador.value = Number(IdProductoAgrupador);
				}									
			}
			
			
		
						
			if ($scope.data.oConfiguracionSocioNegocio.seleccionado.BloqueadoPorCumplimiento === 'S' && $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVING')
			{			
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO")},$scope.IdVentana);				
			}
		
			
		}
		else
		{
			$scope.data.oConfiguracionPortafolio.filtros.IdCliente.value = -1; //limpia portafolio
			$scope.data.oConfiguracionPortafolio.filtrosAPV.IdCliente.value = -1;
		}
			
		
		
	});
	
	$scope.$watch('data.oConfiguracionSocioNegocio.seleccionadoTraspaso',function(newval,oldval){
		if ($scope.accion === 'MODIFICAR'){
			return;
		}

		if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso !== false &&
		typeof $scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso !== false)
		{
			if ($scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso.IdTipoSocioDeNegocio === 'CLI' && 
				$scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.EsCliente === 'UI_SI'){
				
				$scope.data.oConfiguracionPortafolio.filtrosTraspaso.IdCliente.value = $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.IdCliente;									
			}
		}
		else
			$scope.data.oConfiguracionPortafolio.filtrosTraspaso.IdCliente.value = -1; //limpia portafolio
		
		
	});

	$scope.$watch('data.oConfiguracionSocioNegocioRelacionado.seleccionado',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado !== false)
		{
			if (typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado.IdCliente !== 'undefined')
				$scope.data.oConfiguracionPortafolioRelacionado.filtros.IdCliente.value = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado.IdCliente;									
		}
		else	
			$scope.data.oConfiguracionPortafolioRelacionado.filtros.IdCliente.value = -1; //limpia portafolio	
	});
	
	$scope.$watch('data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso !== false)
		{
			if (typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso.IdCliente !== 'undefined')
				$scope.data.oConfiguracionPortafolioRelacionado.filtrosTraspaso.IdCliente.value = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso.IdCliente;									
		}
		else	
			$scope.data.oConfiguracionPortafolioRelacionado.filtrosTraspaso.IdCliente.value = -1; //limpia portafolio	
	});
	
	$scope.$watch('data.agruparEnComprobante',function(newval,oldval){
		$scope.data.ngSecuenciaAgrupacion = '';
	});

	$scope.$watch('data.titularPago',function(newval,oldval){
		$scope.data.oConfiguracionSocioNegocioRelacionado.idActualTraspaso = 0;
		$scope.data.oConfiguracionPortafolioRelacionado.idActualTraspaso = 0;
	});	
	$scope.$watch('data.oProducto.seleccionado',function(newval,oldval){
		if(typeof $scope.data.oProducto.seleccionado !== 'undefined' && $scope.data.oProducto.seleccionado !== false){
			IdProductoAgrupador = Number($scope.data.oProducto.seleccionado.IdProductoAgrupador);
				if($scope.data.oConfiguracionPortafolio.filtrosAPV.IdCliente.value != -1){
					$scope.data.oConfiguracionPortafolio.filtrosAPV.IdProductoAgrupador.value = Number(IdProductoAgrupador);
				}
		}
	});	

	
	
	$scope.$watch('data.oConfiguracionTipoMovimiento.seleccionado',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionTipoMovimiento.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoMovimiento.seleccionado !== false)
		{
			
			//Se actualizan filtros para conceptos
			$scope.FiltroTipoConceptoTesoreria.value = $scope.data.oConfiguracionTipoMovimiento.seleccionado.Tipo;  // IN | EG 
			$scope.FiltroTipoConceptoTesoreriaVacio.value = "";  //vacio es para IN y EG 
			$scope.FiltroTipoConceptoTesoreriaAmbos.value = "AM"; //
			
			$scope.FiltroTipoConceptoTesoreriaTraspaso.value = $scope.data.oConfiguracionTipoMovimiento.seleccionado.Tipo === 'IN' ? 'EN' : 'IN';  // IN | EG 
			$scope.FiltroTipoConceptoTesoreriaVacioTraspaso.value = "";  //vacio es para IN y EG 
			$scope.FiltroTipoConceptoTesoreriaAmbosTraspaso.value = "AM"; //
			
			if ($scope.TipoEmpleado === 'BACKOFFICE')   //Ve todos los conceptos
			{
				$scope.FiltroVisiblePorEjecutivoA.value = 'S';
				$scope.FiltroVisiblePorEjecutivoB.value = 'N';				
			}
			else  //Ve solo conceptos habilitados para su perfil
			{
				$scope.FiltroVisiblePorEjecutivoA.value = ($scope.TipoEmpleado === 'FRONTOFFICE' || $scope.TipoEmpleado === 'SUPERVISOR' ? 'S' : '');
				$scope.FiltroVisiblePorEjecutivoB.value = ($scope.TipoEmpleado === 'FRONTOFFICE' || $scope.TipoEmpleado === 'SUPERVISOR' ? 'S' : '');
			}
		
			
			if ($scope.accion === 'CREAR')
			{

				var idTipoMovimiento =  $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento;  // MOVING  MOVEGR
				if (idTipoMovimiento === "MOVING")
				{	
					$scope.data.titularPagoDisabled = true;
					$scope.data.titularPagoTraspasoDisabled = false;
					//Limpiar campos de titular pago(ya que solo de egreso)
					$scope.data.oConfiguracionSocioNegocioRelacionado.idActual = 0;
					$scope.data.oConfiguracionPortafolioRelacionado.idActual   = 0;					
					$scope.data.titularPago = false;
				}				
				else   //MOVEGR: Egreso
				{
					$scope.data.titularPagoDisabled = false;
					$scope.data.titularPagoTraspasoDisabled = true;
				}
			}
		}
	});
	
	$scope.$watch('data.oConfiguracionMoneda.seleccionado',function(newval,oldval){
		if (typeof $scope.data.oConfiguracionMoneda.seleccionado !== 'undefined' && $scope.data.oConfiguracionMoneda.seleccionado !== false)
		{	
			$scope.data.kngMontoFormaPago = 0;
			if (!$scope.validarMonedaTesoreria())
			{                   
				bcsComunes.msg("warning","warning",$translate.instant('DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO'));
			}
		}
	});

	//Al cambiar datos básicos, actualiza datos del tab "Ficha de Pago"
	$scope.$watchGroup(['data.oConfiguracionTipoMovimiento.seleccionado',
	                    'data.oConfiguracionSocioNegocio.seleccionado',
	                    'data.oConfiguracionPortafolio.seleccionado',
	                    'data.oConfiguracionPortafolioRelacionado.seleccionado',	                    
	                    'data.oConfiguracionConcepto.seleccionado',
	                    'data.oConfiguracionCaja.seleccionado',
	                    'data.oConfiguracionMoneda.seleccionado',
	                    'data.kngFechaLiquidacion',
	                    'data.kngMonto',
	                    'data.titularPago',
	                    'data.oConfiguracionSocioNegocio.seleccionadoTraspaso',
	                    'data.oConfiguracionPortafolio.seleccionadoTraspaso',
	                    'data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso',	                    
	                    'data.oConfiguracionConcepto.seleccionadoTraspaso',
	                    'data.titularPagoTraspaso',
	                    'data.traspaso'], function(newval,oldval){
		
		$scope.objFormaDePago[0] = {};

		
		$scope.objFormaDePago[0].FechaLiquidacion 	= $scope.data.kngFechaLiquidacion;
		$scope.objFormaDePago[0].TipoEmpleado		= $scope.TipoEmpleado;

		if (typeof $scope.data.kngMonto !== 'undefined')
		{
			$scope.objFormaDePago[0].Monto = $scope.data.kngMonto;
			ObtenerSaldoPorAsignar();
		}

		if (typeof $scope.data.oConfiguracionTipoMovimiento.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoMovimiento.seleccionado !== false)
		{
			$scope.objFormaDePago[0].NombreTipoMovimiento = $scope.data.oConfiguracionTipoMovimiento.seleccionado.Nombre;
			$scope.objFormaDePago[0].IdTipoMovimiento = $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento;
			$scope.objFormaDePago[0].IdComprobante = 0; ///// FALTA QUITAR//////			
			$scope.objFormaDePago[0].TipoMovimiento = '';				
		}

		if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionado !== false)
		{	
			$scope.objFormaDePago[0].IdPersona          = $scope.data.oConfiguracionSocioNegocio.seleccionado.IdPersona;
			$scope.objFormaDePago[0].NombrePersona      = $scope.data.oConfiguracionSocioNegocio.seleccionado.NombreCompleto;	
			$scope.objFormaDePago[0].DocumentoIdentidad = $scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto;
		}

		if (typeof $scope.data.oConfiguracionPortafolio.seleccionado !== 'undefined' && $scope.data.oConfiguracionPortafolio.seleccionado !== false){
			$scope.objFormaDePago[0].IdPortafolio		= $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
		}
		
	
		if ($scope.data.titularPago && typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado !== 'undefined' && $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado !== false){
			
			$scope.objFormaDePago[0].IdPersona          = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado.IdPersonaDestino;
			$scope.objFormaDePago[0].NombrePersona      = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado.NombreCompleto;
			
			$scope.objFormaDePago[0].DocumentoIdentidad = '';
		}

	
		if ($scope.data.titularPago && typeof $scope.data.oConfiguracionPortafolioRelacionado.seleccionado !== 'undefined' && $scope.data.oConfiguracionPortafolioRelacionado.seleccionado !== false){			
				$scope.objFormaDePago[0].IdPortafolio		= $scope.data.oConfiguracionPortafolioRelacionado.seleccionado.IdPortafolio;				
		}		
		
		if (typeof $scope.data.oConfiguracionConcepto.seleccionado !== 'undefined' && $scope.data.oConfiguracionConcepto.seleccionado !== false)
		{			
			$scope.objFormaDePago[0].IdConcepto       = $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto;
			$scope.objFormaDePago[0].NombreConcepto   = $scope.data.oConfiguracionConcepto.seleccionado.NombreConcepto;
			$scope.data.generarComprobante = $scope.data.oConfiguracionConcepto.seleccionado.GeneraComprobanteAutomaticamente === 'S' ? true : false;
		}

		if (typeof $scope.data.oConfiguracionCaja.seleccionado !== 'undefined' && $scope.data.oConfiguracionCaja.seleccionado !== false)
		{	
			$scope.objFormaDePago[0].IdCaja       = $scope.data.oConfiguracionCaja.seleccionado.IdCaja;			
			$scope.objFormaDePago[0].CodigoCaja   = $scope.data.oConfiguracionCaja.seleccionado.CodigoCaja;			
			$scope.objFormaDePago[0].NombreCaja   = $scope.data.oConfiguracionCaja.seleccionado.NombreLargoCaja;
		}			
		
		if (typeof $scope.data.oConfiguracionMoneda.seleccionado !== 'undefined' && $scope.data.oConfiguracionMoneda.seleccionado !== false)
		{			
			$scope.objFormaDePago[0].IdMoneda   = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
		}

		if ($scope.accion === 'MODIFICAR'){
			$scope.objFormaDePago[0].IdPersona          = $scope.objIngresoEgreso.IdPersona;
			$scope.objFormaDePago[0].NombrePersona      = $scope.objIngresoEgreso.NombrePersona;
			$scope.objFormaDePago[0].DocumentoIdentidad = $scope.objIngresoEgreso.DocumentoIdentidadDefecto;
			$scope.objFormaDePago[0].IdConcepto       	= $scope.objIngresoEgreso.IdConcepto;
			$scope.objFormaDePago[0].NombreConcepto		= $scope.objIngresoEgreso.NombreConcepto;
			$scope.objFormaDePago[0].IdPortafolio       = $scope.objIngresoEgreso.IdPortafolio;
			$scope.objFormaDePago[0].IdMoneda       	= $scope.objIngresoEgreso.IdMoneda;
			$scope.objFormaDePago[0].IdTipoMovimiento   = $scope.objIngresoEgreso.IdTipoMovimiento;
			
		
			if ($scope.objIngresoEgreso.TitularPago === 'S')
			{
				$scope.objFormaDePago[0].IdPersona          = $scope.objIngresoEgreso.IdPersonaTitularPago;
				$scope.objFormaDePago[0].NombrePersona      = $scope.objIngresoEgreso.NombrePersonaTitularPago;
				$scope.objFormaDePago[0].DocumentoIdentidad = '';				
				$scope.objFormaDePago[0].IdPortafolio       = $scope.objIngresoEgreso.IdPortafolioTitularPago;			
			}
			
		}
		
		if($scope.data.traspaso){
			
			$scope.objFormaDePagoTraspaso[0] = {};

			
			$scope.objFormaDePagoTraspaso[0].FechaLiquidacion 	= $scope.data.kngFechaLiquidacion;
			$scope.objFormaDePagoTraspaso[0].TipoEmpleado		= $scope.TipoEmpleado;

			if (typeof $scope.data.kngMonto !== 'undefined')
			{
				$scope.objFormaDePagoTraspaso[0].Monto = $scope.data.kngMonto;
				ObtenerSaldoPorAsignar();
			}

			if (typeof $scope.data.oConfiguracionTipoMovimiento.seleccionado !== 'undefined' && $scope.data.oConfiguracionTipoMovimiento.seleccionado !== false)
			{
				$scope.objFormaDePagoTraspaso[0].NombreTipoMovimiento = $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVING' ? 'EGRESO' : 'INGRESO';
				$scope.objFormaDePagoTraspaso[0].IdTipoMovimiento = $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVING' ? 'MOVEGR' : 'MOVING';
				$scope.objFormaDePagoTraspaso[0].IdComprobante = 0; ///// FALTA QUITAR//////			
				$scope.objFormaDePagoTraspaso[0].TipoMovimiento = '';				
			}

			if (typeof $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso !== false)
			{	
				$scope.objFormaDePagoTraspaso[0].IdPersona          = $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.IdPersona;
				$scope.objFormaDePagoTraspaso[0].NombrePersona      = $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.NombreCompleto;	
				$scope.objFormaDePagoTraspaso[0].DocumentoIdentidad = $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.DocumentoIdentidadDefecto;
			}

			if (typeof $scope.data.oConfiguracionPortafolio.seleccionado !== 'undefined' && $scope.data.oConfiguracionPortafolio.seleccionado !== false){
				$scope.objFormaDePagoTraspaso[0].IdPortafolio		= $scope.data.oConfiguracionPortafolio.seleccionadoTraspaso.IdPortafolio;
			}

			if ($scope.data.titularPagoTraspaso && 
				typeof $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso !== false &&
				typeof $scope.data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso !== false){
				
				$scope.objFormaDePagoTraspaso[0].IdPersona          = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso.IdPersona;
				$scope.objFormaDePagoTraspaso[0].NombrePersona      = $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso.NombreCompleto;
				$scope.objFormaDePagoTraspaso[0].IdPortafolio		= $scope.data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso.IdPortafolio;
				$scope.objFormaDePagoTraspaso[0].DocumentoIdentidad = '';
			}

			if (typeof $scope.data.oConfiguracionConcepto.seleccionadoTraspaso !== 'undefined' && $scope.data.oConfiguracionConcepto.seleccionadoTraspaso !== false)
			{			
				$scope.objFormaDePagoTraspaso[0].IdConcepto       = $scope.data.oConfiguracionConcepto.seleccionadoTraspaso.IdConcepto;
				$scope.objFormaDePagoTraspaso[0].NombreConcepto   = $scope.data.oConfiguracionConcepto.seleccionadoTraspaso.NombreConcepto;
			}

			if (typeof $scope.data.oConfiguracionCaja.seleccionado !== 'undefined' && $scope.data.oConfiguracionCaja.seleccionado !== false)
			{	
				$scope.objFormaDePagoTraspaso[0].IdCaja       = $scope.data.oConfiguracionCaja.seleccionado.IdCaja;			
				$scope.objFormaDePagoTraspaso[0].CodigoCaja   = $scope.data.oConfiguracionCaja.seleccionado.CodigoCaja;			
				$scope.objFormaDePagoTraspaso[0].NombreCaja   = $scope.data.oConfiguracionCaja.seleccionado.NombreLargoCaja;
			}			
			
			if (typeof $scope.data.oConfiguracionMoneda.seleccionado !== 'undefined' && $scope.data.oConfiguracionMoneda.seleccionado !== false)
			{			
				$scope.objFormaDePagoTraspaso[0].IdMoneda   = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
			}
		}
		
	});
	
	// ----------------------------------------------
	// BLOQUE WATCH (FIN)
	// ----------------------------------------------  

	

	
    // ----------------------------------------------
	// FUNCIONES (INICIO)
	// ----------------------------------------------	
	/*$scope.select = function()
	{
		//Advierte de ingreso de "Tipo Movimiento"
		if ($scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === undefined){
		    bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_TIPO_MOVIMIENTO'));
		    return;
		}
		
		if ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda === undefined){
			bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_SELECCIONAR_MONEDA'));
			return;
		}

		if ($scope.data.kngMonto === undefined || $scope.data.kngMonto === 0 || $scope.data.kngMonto === ''|| $scope.data.kngMonto === null){			
			bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_INGRESAR_MONTO'));
			return;
		}
	};*/

	$scope.select = function(grid)
	{
		$timeout(function() {			
			var t = $("."+grid).find("[kendo-grid]");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},1);
	};

	$scope.validarMonedaTesoreria = function()
	{
		var resultadoValidacion = true
		if (dsFormaDePago !== undefined )
		{
			var arrDataFormaPago = [];
			arrDataFormaPago = dsFormaDePago.dataColumns(['IdMoneda']);
			
			if (arrDataFormaPago !== undefined)
			{
				$.each(arrDataFormaPago, function(key,value){
					if (value.IdMoneda !== $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda)
					{
						resultadoValidacion = false;
					}
				});	
			}
		}			
		return(resultadoValidacion);
	};
	
	$scope.setarSocioNegocio = function(){
		$scope.data.oConfiguracionSocioNegocio.filtros.EsCliente.value     		= '';		
		$scope.data.oConfiguracionSocioNegocio.filtros.EsProveedor.value   		= '';
		$scope.data.oConfiguracionSocioNegocio.filtros.EsEmpleado.value    		= '';
		$scope.data.oConfiguracionSocioNegocio.filtros.EsContraparte.value 		= '';
		$scope.data.oConfiguracionSocioNegocio.filtros.EstadoCliente.value     	= '';		
		$scope.data.oConfiguracionSocioNegocio.filtros.EstadoProveedor.value   	= '';
		$scope.data.oConfiguracionSocioNegocio.filtros.EstadoEmpleado.value    	= '';
		$scope.data.oConfiguracionSocioNegocio.filtros.EstadoContraparte.value 	= '';
	};
	
	$scope.setarSocioNegocioTraspaso = function(){
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsCliente.value     		= '';		
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsProveedor.value   		= '';
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsEmpleado.value    		= '';
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EsContraparte.value 		= '';
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoCliente.value     	= '';		
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoProveedor.value   	= '';
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoEmpleado.value    	= '';
		$scope.data.oConfiguracionSocioNegocio.filtrosTraspaso.EstadoContraparte.value 	= '';
	};

	

	$scope.data.limpiarFormulario = function()
	{	
		//Datos Básicos
		$scope.data.oConfiguracionTipoSocioNegocio.idActual = 'CLI';
		$scope.data.oConfiguracionSocioNegocio.idActual = false;
		$scope.data.oConfiguracionPortafolio.idActual = 0;
		$scope.data.oConfiguracionSocioNegocioRelacionado.idActual = 0;
		$scope.data.oConfiguracionPortafolioRelacionado.idActual = 0;
		
		$scope.data.oConfiguracionTipoMovimiento.idActual = 'MOVING';	
	    $scope.data.oConfiguracionMoneda.idActual = 0;
		$scope.data.oConfiguracionCaja.idActual = 0;
		$scope.data.kngMonto = '';
		$scope.data.monto = 0;
		$scope.data.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionConcepto.idActual = 0;		
		$scope.data.ngSecuenciaAgrupacion = '';
		$scope.data.descripcion = '';
		$scope.data.traspaso = false;
		$scope.data.oConfiguracionConcepto.seleccionado = undefined;
		$scope.data.generarComprobante = false;
		
		//Datos Forma de Pago
		$scope.data.limpiarFormaPago();		//esta en la directiva
		dsFormaDePago.removeAll();
		
		//Datos Forma de Pago
		$scope.data.limpiarFormaPagoTraspaso();		//esta en la directiva
		dsFormaDePagoTraspaso.removeAll();

		//Datos Referenciales
		$scope.data.oConfiguracionTipoProducto.idActual  = 0;
		$scope.data.oConfiguracionTipoOperacion.idActual = 0;
		$scope.data.ngFolioOperacion = '';
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionCondicionLiquidacion.idActual = 0;
		$scope.data.indicadorBilateral = false;	
		
		//Traspaso
		$scope.data.oConfiguracionTipoSocioNegocio.idActualTraspaso = 0;
		$scope.data.oConfiguracionSocioNegocio.idActualTraspaso = false;
		$scope.data.oConfiguracionPortafolio.idActualTraspaso = 0;
		$scope.data.oConfiguracionSocioNegocioRelacionado.idActualTraspaso = 0;
		$scope.data.oConfiguracionPortafolioRelacionado.idActualTraspaso = 0;
				
		$scope.data.oConfiguracionConcepto.idActualTraspaso = 0;		
	};


	//Carga la forma de pago editada en la grilla
	$scope.cargarFormaDePagoEditada = function() {
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPago(); // recupera la configuración desde la directiva

		if (typeof objFormaPago !== 'undefined')
		{	
			//Valida que el monto de la nueva forma de pago no exeda el monto total del movimiento
			if (!validarMontoFormaDePago(objFormaPago.Monto,'EDITAR'))
			{
				$scope.data.formaDePagoAEditar	= {};	
				$scope.cancelarEdicionFormaDePago();
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO")},$scope.IdVentana);
				return;
			}
			
			var formaDePago      = dsFormaDePago.data();
			for (i= 0 ; i < formaDePago.length ; i++)
			{	
				if (Number(IdIngresoEgresoFormaDePagoEditada) === Number(formaDePago[i].IdIngresoEgresoFormaDePago) )
				{
					formaDePago[i].IdIngresoEgresoFormaDePago			= objFormaPago.IdIngresoEgresoFormaDePago;
					formaDePago[i].IdCaja								= objFormaPago.IdCaja;
					formaDePago[i].CodigoCaja							= objFormaPago.CodigoCaja;
					formaDePago[i].NombreCaja							= objFormaPago.NombreCaja;

					formaDePago[i].IdMedioDePago						= objFormaPago.IdMedioDePago;
					formaDePago[i].NombreMedioDePago					= objFormaPago.NombreMedioDePago;
					formaDePago[i].TipoMedioDePago  					= objFormaPago.TipoMedioDePago;
					formaDePago[i].IdModalidadDePago					= objFormaPago.IdModalidadDePago;
					formaDePago[i].NombreModalidadDePago				= objFormaPago.NombreModalidadDePago;
					formaDePago[i].IdMoneda								= objFormaPago.IdMoneda;
					formaDePago[i].Monto								= objFormaPago.Monto;
					formaDePago[i].IdComprobante						= objFormaPago.IdComprobante;
					formaDePago[i].IdTipoMovimiento						= objFormaPago.IdTipoMovimiento;
					formaDePago[i].IdComprobantePartida              	= objFormaPago.IdComprobantePartida;
					//

					//CORREDORA
					formaDePago[i].IdCuentaBancariaCorredora 			= objFormaPago.IdCuentaBancariaCorredora;
					formaDePago[i].IdTipoCuentaBancariaCorredora 		= objFormaPago.IdTipoCuentaBancariaCorredora;
					formaDePago[i].NombreTipoCuentaBancariaCorredora	= objFormaPago.NombreTipoCuentaBancariaCorredora;
					formaDePago[i].NombreBancoCorredora					= objFormaPago.NombreBancoCorredora;
					formaDePago[i].NombreCuentaBancariaCorredora		= objFormaPago.NombreCuentaBancariaCorredora;
					formaDePago[i].NumeroCuentaBancariaCorredora		= objFormaPago.NumeroCuentaBancariaCorredora;
					formaDePago[i].IdChequera 			            	= objFormaPago.IdChequera;
					formaDePago[i].NumeroDocumentoCorredora				= objFormaPago.NumeroDocumentoCorredora;
					formaDePago[i].NumeroSerieCorredora					= objFormaPago.NumeroSerieCorredora;
					formaDePago[i].IdTarjetaBancariaCorredora 			= objFormaPago.IdTarjetaBancariaCorredora;
					formaDePago[i].FechaDocumentoPagoCorredora 			= objFormaPago.FechaDocumentoPagoCorredora;

					//SOCIO NEGOCIO
					formaDePago[i].NumeroCuentaBancariaSocio			= objFormaPago.NumeroCuentaBancariaSocio;
					formaDePago[i].IdCuentaBancariaSocio				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdBancoSocio			    			= objFormaPago.IdBancoSocio;
					formaDePago[i].NombreBancoSocio 					= objFormaPago.NombreBancoSocio;
					formaDePago[i].FechaDocumentoPagoSocio 				= objFormaPago.FechaDocumentoPagoSocio;
					formaDePago[i].NumeroDocumentoSocio 				= objFormaPago.NumeroDocumentoSocio;
					formaDePago[i].NumeroSerieSocio 					= objFormaPago.NumeroSerieSocio;
					formaDePago[i].IdCuentaBancariaSocio 				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdTipoCuentaBancariaSocio 			= objFormaPago.IdTipoCuentaBancariaSocio;
					formaDePago[i].NombreTipoCuentaBancariaSocio 		= objFormaPago.NombreTipoCuentaBancariaSocio;
					formaDePago[i].NombreCuentaBancariaSocio			= objFormaPago.NombreCuentaBancariaSocio;
					formaDePago[i].IdTarjetaBancariaSocio 		    	= objFormaPago.IdTarjetaBancariaSocio;
					formaDePago[i].IdTipoTarjetaBancariaSocio 			= objFormaPago.IdTipoTarjetaBancariaSocio;
					formaDePago[i].NombreTipoTarjetaBancariaSocio 		= objFormaPago.NombreTipoTarjetaBancariaSocio;
					formaDePago[i].NumeroTarjetaBancariaSocio 			= objFormaPago.NumeroTarjetaBancariaSocio;
				}
			};
		};
 
		//refresca la grilla con los datos editados		
		$scope.gridApi.refresh();
		
		ObtenerSaldoPorAsignar();
		$scope.data.showBtnAgregar 	= true;
		$scope.data.showBtnEditar  	= false;
		$scope.data.showBtnCancelar = false;
		$scope.data.formaDePagoAEditar	= {};
		$scope.cancelarEdicionFormaDePago();
	};
	
	$scope.cargarFormaDePagoEditadaTraspaso = function() {
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPagoTraspaso(); // recupera la configuración desde la directiva

		if (typeof objFormaPago !== 'undefined')
		{	
			//Valida que el monto de la nueva forma de pago no exeda el monto total del movimiento
			if (!validarMontoFormaDePagoTraspaso(objFormaPago.Monto,'EDITAR'))
			{
				$scope.data.formaDePagoAEditarTraspaso	= {};	
				$scope.cancelarEdicionFormaDePagoTraspaso();
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO")},$scope.IdVentana);
				return;
			}
			
			var formaDePago      = dsFormaDePagoTraspaso.data();
			for (i= 0 ; i < formaDePago.length ; i++)
			{	
				if (Number(IdIngresoEgresoFormaDePagoEditadaTraspaso) === Number(formaDePago[i].IdIngresoEgresoFormaDePago) )
				{
					formaDePago[i].IdIngresoEgresoFormaDePago			= objFormaPago.IdIngresoEgresoFormaDePago;
					formaDePago[i].IdCaja								= objFormaPago.IdCaja;
					formaDePago[i].CodigoCaja							= objFormaPago.CodigoCaja;
					formaDePago[i].NombreCaja							= objFormaPago.NombreCaja;

					formaDePago[i].IdMedioDePago						= objFormaPago.IdMedioDePago;
					formaDePago[i].NombreMedioDePago					= objFormaPago.NombreMedioDePago;
					formaDePago[i].TipoMedioDePago  					= objFormaPago.TipoMedioDePago;
					formaDePago[i].IdModalidadDePago					= objFormaPago.IdModalidadDePago;
					formaDePago[i].NombreModalidadDePago				= objFormaPago.NombreModalidadDePago;
					formaDePago[i].IdMoneda								= objFormaPago.IdMoneda;
					formaDePago[i].Monto								= objFormaPago.Monto;
					formaDePago[i].IdComprobante						= objFormaPago.IdComprobante;
					formaDePago[i].IdTipoMovimiento						= objFormaPago.IdTipoMovimiento;
					formaDePago[i].IdComprobantePartida              	= objFormaPago.IdComprobantePartida;
					//

					//CORREDORA
					formaDePago[i].IdCuentaBancariaCorredora 			= objFormaPago.IdCuentaBancariaCorredora;
					formaDePago[i].IdTipoCuentaBancariaCorredora 		= objFormaPago.IdTipoCuentaBancariaCorredora;
					formaDePago[i].NombreTipoCuentaBancariaCorredora	= objFormaPago.NombreTipoCuentaBancariaCorredora;
					formaDePago[i].NombreBancoCorredora					= objFormaPago.NombreBancoCorredora;
					formaDePago[i].NombreCuentaBancariaCorredora		= objFormaPago.NombreCuentaBancariaCorredora;
					formaDePago[i].NumeroCuentaBancariaCorredora		= objFormaPago.NumeroCuentaBancariaCorredora;
					formaDePago[i].IdChequera 			            	= objFormaPago.IdChequera;
					formaDePago[i].NumeroDocumentoCorredora				= objFormaPago.NumeroDocumentoCorredora;
					formaDePago[i].NumeroSerieCorredora					= objFormaPago.NumeroSerieCorredora;
					formaDePago[i].IdTarjetaBancariaCorredora 			= objFormaPago.IdTarjetaBancariaCorredora;
					formaDePago[i].FechaDocumentoPagoCorredora 			= objFormaPago.FechaDocumentoPagoCorredora;

					//SOCIO NEGOCIO
					formaDePago[i].NumeroCuentaBancariaSocio			= objFormaPago.NumeroCuentaBancariaSocio;
					formaDePago[i].IdCuentaBancariaSocio				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdBancoSocio			    			= objFormaPago.IdBancoSocio;
					formaDePago[i].NombreBancoSocio 					= objFormaPago.NombreBancoSocio;
					formaDePago[i].FechaDocumentoPagoSocio 				= objFormaPago.FechaDocumentoPagoSocio;
					formaDePago[i].NumeroDocumentoSocio 				= objFormaPago.NumeroDocumentoSocio;
					formaDePago[i].NumeroSerieSocio 					= objFormaPago.NumeroSerieSocio;
					formaDePago[i].IdCuentaBancariaSocio 				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdTipoCuentaBancariaSocio 			= objFormaPago.IdTipoCuentaBancariaSocio;
					formaDePago[i].NombreTipoCuentaBancariaSocio 		= objFormaPago.NombreTipoCuentaBancariaSocio;
					formaDePago[i].NombreCuentaBancariaSocio			= objFormaPago.NombreCuentaBancariaSocio;
					formaDePago[i].IdTarjetaBancariaSocio 		    	= objFormaPago.IdTarjetaBancariaSocio;
					formaDePago[i].IdTipoTarjetaBancariaSocio 			= objFormaPago.IdTipoTarjetaBancariaSocio;
					formaDePago[i].NombreTipoTarjetaBancariaSocio 		= objFormaPago.NombreTipoTarjetaBancariaSocio;
					formaDePago[i].NumeroTarjetaBancariaSocio 			= objFormaPago.NumeroTarjetaBancariaSocio;
				}
			};
		};
 
		//refresca la grilla con los datos editados		
		$scope.gridApiTraspaso.refresh();
		
		ObtenerSaldoPorAsignar();
		$scope.data.showBtnAgregarTraspaso 	= true;
		$scope.data.showBtnEditarTraspaso  	= false;
		$scope.data.showBtnCancelarTraspaso = false;
		$scope.data.formaDePagoAEditarTraspaso	= {};
		$scope.cancelarEdicionFormaDePagoTraspaso();
	};
	
	
	
	$scope.EditarFormaDePago = function(datos) 
	{	
		IdIngresoEgresoFormaDePagoEditada = datos.IdIngresoEgresoFormaDePago;  //Id referencial de FP editada
		$scope.data.showBtnAgregar 	 	= false;
		$scope.data.showBtnEditar 	 	= true;
		$scope.data.showBtnCancelar	 	= true;

		$scope.data.formaDePagoAEditar	= datos;   //Datos a enviar a directiva
	};
	
	$scope.EditarFormaDePagoTraspaso = function(datos) 
	{	
		IdIngresoEgresoFormaDePagoEditadaTraspaso = datos.IdIngresoEgresoFormaDePago;  //Id referencial de FP editada
		$scope.data.showBtnAgregar 	 	= false;
		$scope.data.showBtnEditar 	 	= true;
		$scope.data.showBtnCancelar	 	= true;

		$scope.data.formaDePagoAEditar	= datos;   //Datos a enviar a directiva
	};

	
	$scope.cancelarEdicionFormaDePago = function(){
		$scope.data.showBtnAgregar 			= true;
		$scope.data.showBtnEditar 			= false;
		$scope.data.showBtnCancelar			= false;
		$scope.data.limpiarFormaPago();		//esta en la directiva
	};
	
	$scope.cancelarEdicionFormaDePagoTraspaso = function(){
		$scope.data.showBtnAgregarTraspaso 			= true;
		$scope.data.showBtnEditarTraspaso 			= false;
		$scope.data.showBtnCancelarTraspaso			= false;
		$scope.data.limpiarFormaPagoTraspaso();		//esta en la directiva
	};
	
	
	//Quita una forma de pago de la grilla(solo visualmente) 
	$scope.eliminarFormaPago = function(seleccion){	         
		if (typeof seleccion === 'undefined'){	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_FORMA_DE_PAGO'));		
		}
		else {				
			dsFormaDePago.remove(seleccion);
			var formaDePago      = dsFormaDePago.data();
			if (seleccion.IdTipoMovimiento === 'MOVEGR' && 
				seleccion.IdMedioDePago === 'CHE'){

				
				for (i= 0 ; i < formaDePago.length ; i++){
					if (formaDePago[i].NumeroSerieCorredora === seleccion.NumeroSerieCorredora &&
						formaDePago[i].NumeroDocumentoCorredora > seleccion.NumeroDocumentoCorredora){
						formaDePago[i].NumeroDocumentoCorredora = formaDePago[i].NumeroDocumentoCorredora - 1;
						
						
						$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: formaDePago[i].IdCuentaBancariaCorredora,
														 NumeroSerieCorredora 		: formaDePago[i].NumeroSerieCorredora,
														 NumeroDocumentoCorredora 	: formaDePago[i].NumeroDocumentoCorredora};

					}
				}
				if (formaDePago.length === 0){
					$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: seleccion.IdCuentaBancariaCorredora,
													 NumeroSerieCorredora 		: seleccion.NumeroSerieCorredora,
													 NumeroDocumentoCorredora 	: seleccion.NumeroDocumentoCorredora -1
													 };
				}				
			}			
			$scope.gridApi.refresh();
			seleccion = undefined;
			if (formaDePago.length === 0 ){
				$scope.data.EnableComprobante = true;
			}
			ObtenerSaldoPorAsignar();
		}
	}
	
	//Quita una forma de pago de la grilla(solo visualmente) 
	$scope.eliminarFormaPagoTraspaso = function(seleccion){	         
		if (typeof seleccion === 'undefined'){	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_FORMA_DE_PAGO'));		
		}
		else {				
			dsFormaDePagoTraspaso.remove(seleccion);
			var formaDePago      = dsFormaDePagoTraspaso.data();
			if (seleccion.IdTipoMovimiento === 'MOVEGR' && 
				seleccion.IdMedioDePago === 'CHE'){

				
				for (i= 0 ; i < formaDePago.length ; i++){
					if (formaDePago[i].NumeroSerieCorredora === seleccion.NumeroSerieCorredora &&
						formaDePago[i].NumeroDocumentoCorredora > seleccion.NumeroDocumentoCorredora){
						formaDePago[i].NumeroDocumentoCorredora = formaDePago[i].NumeroDocumentoCorredora - 1;
						
						
						$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: formaDePago[i].IdCuentaBancariaCorredora,
														 NumeroSerieCorredora 		: formaDePago[i].NumeroSerieCorredora,
														 NumeroDocumentoCorredora 	: formaDePago[i].NumeroDocumentoCorredora};

					}
				}
				if (formaDePago.length === 0){
					$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: seleccion.IdCuentaBancariaCorredora,
													 NumeroSerieCorredora 		: seleccion.NumeroSerieCorredora,
													 NumeroDocumentoCorredora 	: seleccion.NumeroDocumentoCorredora -1
													 };
				}				
			}			
			$scope.gridApiTraspaso.refresh();
			seleccion = undefined;
			if (formaDePago.length === 0 ){
				$scope.data.EnableComprobante = true;
			}
			ObtenerSaldoPorAsignar();
		}
	}
	
	
	//Agrega una nueva forma de pago(un nuevo row a la grilla) (solo visualmente)
	$scope.agregarFormaPago = function()
	{	
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPago(); // recupera la configuración desde la directiva
		
		if (typeof objFormaPago !== 'undefined')
		{
			//Valida que el monto de la nueva forma de pago no exeda el monto total del movimiento
			if (!validarMontoFormaDePago(objFormaPago.Monto,'AGREGAR'))
			{	
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO")},$scope.IdVentana);
				return;
			}
			
			IdIngresoEgresoFormaDePago  			 = IdIngresoEgresoFormaDePago  + 1;  //Id referencial(sólo para edición) 
			objFormaPago.IdIngresoEgresoFormaDePago  = IdIngresoEgresoFormaDePago;       //Id referencial(para edición)
			
			dsFormaDePago.add(objFormaPago);
			ObtenerSaldoPorAsignar();
			var formaDePago      = dsFormaDePago.data();
			if (formaDePago.length > 0 ){
				$scope.data.EnableComprobante = false;
			}
			
			$scope.data.limpiarFormaPago();		//esta en la directiva
		}
	}
	
	//Agrega una nueva forma de pago(un nuevo row a la grilla) (solo visualmente)
	$scope.agregarFormaPagoTraspaso = function()
	{	
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPagoTraspaso(); // recupera la configuración desde la directiva
		
		if (typeof objFormaPago !== 'undefined')
		{
			//Valida que el monto de la nueva forma de pago no exeda el monto total del movimiento
			if (!validarMontoFormaDePagoTraspaso(objFormaPago.Monto,'AGREGAR'))
			{	
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_MONTO_FORMA_PAGO_EXCEDE_MONTO_MOVIMIENTO")},$scope.IdVentana);
				return;
			}
			
			IdIngresoEgresoFormaDePagoTraspaso  	 = IdIngresoEgresoFormaDePagoTraspaso  + 1;  //Id referencial(sólo para edición) 
			objFormaPago.IdIngresoEgresoFormaDePago  = IdIngresoEgresoFormaDePago;       //Id referencial(para edición)
			
			dsFormaDePagoTraspaso.add(objFormaPago);
			ObtenerSaldoPorAsignar();
			var formaDePago      = dsFormaDePagoTraspaso.data();
			if (dsFormaDePago.size > 0 ){
				$scope.data.EnableComprobante = false;
			}

			$scope.data.limpiarFormaPagoTraspaso();		//esta en la directiva
		}
	}
	
	
	
	//Calcula Saldo por asignar.
	function ObtenerSaldoPorAsignar()
	{
		var saldoTotal = 0;
		if (dsFormaDePago !== undefined ){
			//saldoTotal = $scope.objFormaDePago[0].Monto - dsFormaDePago.aggregates().Monto.sum;

			var arrDataFormaPago = [];
			arrDataFormaPago = dsFormaDePago.dataColumns(['Monto']);
			
			for (i=0;i<dsFormaDePago.data().length;i++){
				saldoTotal = saldoTotal + arrDataFormaPago[i].Monto;
			}
					
			$scope.data.saldoPorAsignar = $scope.objFormaDePago[0].Monto - saldoTotal;
		}
		
		if (dsFormaDePagoTraspaso !== undefined && $scope.data.traspaso){
			//saldoTotal = $scope.objFormaDePago[0].Monto - dsFormaDePago.aggregates().Monto.sum;
			var saldoTotalTraspaso = 0;
			var arrDataFormaPago = [];
			arrDataFormaPago = dsFormaDePagoTraspaso.dataColumns(['Monto']);
			
			for (i=0;i<dsFormaDePagoTraspaso.data().length;i++){
				saldoTotalTraspaso = saldoTotalTraspaso + arrDataFormaPago[i].Monto;
			}
					
			$scope.data.saldoPorAsignarTraspaso = $scope.objFormaDePagoTraspaso[0].Monto - saldoTotalTraspaso;
		}
	};
	

	//Valida que el Monto de la forma de pago no exeda el monto total del movimiento
	function validarMontoFormaDePago(newMonto, accion)
	{	
		var montoFormaDePago  = 0;
		montoFormaDePago      = montoFormaDePago + newMonto;  
		
		if (dsFormaDePago !== undefined )
		{	
			var arrDataFormaPago = [];
			arrDataFormaPago = dsFormaDePago.dataColumns(['Monto','IdIngresoEgresoFormaDePago']);
			
			for (i=0;i<dsFormaDePago.data().length;i++)
			{	
				if (accion === 'EDITAR' && (Number(IdIngresoEgresoFormaDePagoEditada) !== Number(arrDataFormaPago[i].IdIngresoEgresoFormaDePago)))
				{
					montoFormaDePago = montoFormaDePago + arrDataFormaPago[i].Monto; //Monto total actual de la formas de pago(sin incluir la editada)
				}
					
				if (accion === 'AGREGAR')
				{
					montoFormaDePago = montoFormaDePago + arrDataFormaPago[i].Monto; //Monto total actual de la formas de pago
				}
			}
			
			if (montoFormaDePago > $scope.objFormaDePago[0].Monto)
			{
				return false;
			}

		}
		
		return true;
	};	

	//Valida que el Monto de la forma de pago no exeda el monto total del movimiento
	function validarMontoFormaDePagoTraspaso(newMonto, accion)
	{	
		var montoFormaDePago  = 0;
		montoFormaDePago      = montoFormaDePago + newMonto;  
		
		if (dsFormaDePagoTraspaso !== undefined )
		{	
			var arrDataFormaPago = [];
			arrDataFormaPago = dsFormaDePagoTraspaso.dataColumns(['Monto','IdIngresoEgresoFormaDePago']);
			
			for (i=0;i<dsFormaDePagoTraspaso.data().length;i++)
			{	
				if (accion === 'EDITAR' && (Number(IdIngresoEgresoFormaDePagoEditadaTraspaso) !== Number(arrDataFormaPago[i].IdIngresoEgresoFormaDePago)))
				{
					montoFormaDePago = montoFormaDePago + arrDataFormaPago[i].Monto; //Monto total actual de la formas de pago(sin incluir la editada)
				}
					
				if (accion === 'AGREGAR')
				{
					montoFormaDePago = montoFormaDePago + arrDataFormaPago[i].Monto; //Monto total actual de la formas de pago
				}
			}
			
			if (montoFormaDePago > $scope.objFormaDePago[0].Monto)
			{
				return false;
			}

		}
		
		return true;
	};
	
	//------------------------------------------------------------
	// FUNCIONES (FIN)  
	//-------------------------------------------------------------
	
	
	
	// ------------------------------------------------------
	// SERVICIO CREAR/MODIFICAR MOVIMIENTO DE INGRESO/EGRESO
	// ------------------------------------------------------
	$scope.editarIngresoEgreso = function(){		
			if (!$scope.validarMonedaTesoreria()){                   
			bcsComunes.msg("error","error",$translate.instant('DIFERENTE_MONEDA_ENTRE_MOVIMIENTO_Y_FORMA_PAGO'));
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.IdVentana, true);

		
		var IdPersona, IdPortafolio, IdPersonaTitularPago, IdPortafolioTitularPago = 0;
		var IdConcepto = '';
		if ($scope.accion === 'MODIFICAR'){
			IdPersona				=  $scope.objIngresoEgreso.IdPersona;
			IdPortafolio			=  $scope.objIngresoEgreso.IdPortafolio;
			IdPersonaTitularPago	=  $scope.objIngresoEgreso.IdPersonaTitularPago;
			IdPortafolioTitularPago	=  $scope.objIngresoEgreso.IdPortafolioTitularPago;
			IdConcepto				=  $scope.objIngresoEgreso.IdConcepto;
		}
		else {
			IdPersona				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdPersona;
			IdPortafolio			= $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
			IdPersonaTitularPago	= $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionado.IdPersonaDestino;
			IdPortafolioTitularPago	= $scope.data.oConfiguracionPortafolioRelacionado.seleccionado.IdPortafolio;
			IdConcepto				= $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto;
		}

		
		//Movimiento a Ingresar
		var arrDataMovimiento = {};
		
	    arrDataMovimiento.IdTipoMovimiento							= $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento || '';
	    arrDataMovimiento.IdTipoPersona								= $scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio || '';
	    arrDataMovimiento.IdPersona									= IdPersona || 0;
		arrDataMovimiento.IdPortafolio								= IdPortafolio || 0;
	    
	    arrDataMovimiento.IdMoneda									= $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda || '';
		arrDataMovimiento.Monto										= $scope.data.kngMonto || 0;
		arrDataMovimiento.FechaLiquidacion      					= util.dateSPdate($scope.data.kngFechaLiquidacion);
	    arrDataMovimiento.IdConcepto								= IdConcepto || '';
	    
		arrDataMovimiento.IdTipoPersonaTitularPago  				= $scope.data.titularPago ? (typeof $scope.data.oConfiguracionPortafolioRelacionado.seleccionado.IdPortafolio !== 'undefined' ? 'CLI' : ( $scope.objIngresoEgreso.IdTipoPersonaTitularPago ?? '' ) ) : ($scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio);
		arrDataMovimiento.IdPersonaTitularPago      				= $scope.data.titularPago ? (IdPersonaTitularPago || 0) : (IdPersona || 0);
		arrDataMovimiento.IdPortafolioTitularPago  					= $scope.data.titularPago ? (IdPortafolioTitularPago || 0) : (IdPortafolio|| 0) ;
		arrDataMovimiento.IdCaja									= $scope.data.oConfiguracionCaja.seleccionado.IdCaja || 0;
	    arrDataMovimiento.SecuenciaAgrupacion						= $scope.data.ngSecuenciaAgrupacion || '';
	    arrDataMovimiento.SecuenciaRelacionada						= null;
	    arrDataMovimiento.Glosa										= $scope.data.descripcion || '';
	    arrDataMovimiento.IdEmpleadoDigitador						= $scope.objIngresoEgreso.IdEmpleadoDigitador || 0;
	    arrDataMovimiento.IdSucursalEmpleadoDigitador				= 0;
		
	    //Datos del comprobante origen(puede provenir de facturación)
		if ($scope.accion == 'CREAR')
		{   // Al crear datos del origen no exiten
		    arrDataMovimiento.IdTipoComprobante							= '';
			arrDataMovimiento.IdComprobante								= 0;
			arrDataMovimiento.FechaComprobante							= '0001-01-01';						
		}
		else if ($scope.accion == 'MODIFICAR')
		{	//Al editar se traspasan tal cual los datos del origen sin ser modificados
		    arrDataMovimiento.IdTipoComprobante							= $scope.data.IdTipoComprobanteOrigen;
			arrDataMovimiento.IdComprobante								= $scope.data.IdComprobanteOrigen;
			arrDataMovimiento.FechaComprobante							= util.dateSPdate($scope.data.FechaComprobanteOrigen);						
		}
				
		
		arrDataMovimiento.FechaLiquidacionComprobante				= $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.FechaLiquidacionComprobanteOrigen : '0001-01-01';		
		arrDataMovimiento.FolioCartera								= 0;
		arrDataMovimiento.IdInstrumento								= $scope.objIngresoEgreso.IdInstrumento || 0;
		arrDataMovimiento.IdTipoInstrumento							= $scope.objIngresoEgreso.IdTipoInstrumento || '';
		arrDataMovimiento.IdClasificacionTipoInstrumento			= $scope.objIngresoEgreso.IdClasificacionTipoInstrumento || '';
		arrDataMovimiento.IdTipoProducto							= $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProducto || '';
		arrDataMovimiento.IdTipoOperacion							= $scope.objIngresoEgreso.IdTipoOperacion || '';
		arrDataMovimiento.IdOperacion								= $scope.data.ngFolioOperacion || 0;
		arrDataMovimiento.FechaOperacion							= util.dateSPdate($scope.data.kngFechaOperacion);
		arrDataMovimiento.IdCondicionLiquidacion					= $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || '';
		arrDataMovimiento.IdBolsa									= '';
		arrDataMovimiento.IdMonedaOperacion							= $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.IdMonedaOperacion : '';
		arrDataMovimiento.MontoOperacion							= $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.MontoOperacion : 0;
		arrDataMovimiento.IdTipoReajuste							= $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.IdTipoReajuste : '';
		arrDataMovimiento.FechaParidad								= '0001-01-01';
		arrDataMovimiento.ValorParidad								= 0;	
		arrDataMovimiento.Bilateral									= $scope.data.indicadorBilateral ? 'S' : 'N';
		arrDataMovimiento.GeneraComprobante							= $scope.data.generarComprobante ? 'S' : 'N';		
		arrDataMovimiento.AgruparEnComprobante						= $scope.data.agruparEnComprobante ? 'S' : 'N';  
		arrDataMovimiento.MueveCuentaInversion						= $scope.accion == 'MODIFICAR' ? ( $scope.objIngresoEgreso.MueveCuentaInversion == 'SI' ? 'S' : 'N' ) : ( $scope.data.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio == 'CLI' ? 'S' : 'N' );
		arrDataMovimiento.TitularPago								= $scope.data.titularPago ? 'S' : 'N';		
		arrDataMovimiento.NumeroReferencia							= $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.IdIngresoEgreso : 1;


		//Rescata los valores de la grilla Formas de Pago para insertarlos al parámetro
		var arrDataFormaPago = new Array();			
		arrDataFormaPago = dsFormaDePago.dataColumns([
		                                   'IdMedioDePago',
		                                   'IdModalidadDePago',
		                                   'IdMoneda',
		                                   'Monto',
		                                   'IdCuentaBancariaCorredora',
		                                   'IdTarjetaBancariaCorredora',
		                                   'IdChequera',
		                                   'NumeroDocumentoCorredora',
										   'FechaDocumentoPagoCorredora',
										   'IdCuentaBancariaSocio',
										   'IdBancoSocio',
										   'NombreCuentaBancariaSocio',
										   'NumeroCuentaBancariaSocio',
										   'IdTipoCuentaBancariaSocio',
										   'NombreTipoCuentaBancariaSocio',
										   'IdTarjetaBancariaSocio',
										   'IdTipoTarjetaBancariaSocio',
										   'NombreTipoTarjetaBancariaSocio',
										   'NumeroTarjetaBancariaSocio',
										   'NumeroSerieSocio',
										   'NumeroDocumentoSocio',
										   'FechaDocumentoPagoSocio',
										   ]);
		
		//Ajusta algunos parámetros de forma de pago
		if (arrDataFormaPago !== undefined)
		{
			var i = 1;
			$.each(arrDataFormaPago, function(key,value){
				arrDataFormaPago[key].NumeroReferencia = $scope.accion == 'MODIFICAR' ? $scope.objIngresoEgreso.IdIngresoEgreso : 1;
				arrDataFormaPago[key].NumeroReferenciaFormaDePago = i;
				arrDataFormaPago[key].FechaDocumentoPagoCorredora = arrDataFormaPago[key].FechaDocumentoPagoCorredora ? util.dateSPdate(util.dateInicioDia(arrDataFormaPago[key].FechaDocumentoPagoCorredora)) : '0001-01-01';
				arrDataFormaPago[key].FechaDocumentoPagoSocio = arrDataFormaPago[key].FechaDocumentoPagoSocio ? util.dateSPdate(util.dateInicioDia(arrDataFormaPago[key].FechaDocumentoPagoSocio)) : '0001-01-01';
				i++;
			});			
		}
		
		
		var parametros = {};		
		parametros.p_IdEmpresa              	= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario              	= $scope.SesionIdUsuario;
		parametros.p_IdModulo               	= 'TES';
		parametros.p_simularOperacion       	= 'N';
		parametros.p_UsaReglasNegocio      		= 'S';
		parametros.p_FormaDePago				= arrDataFormaPago || [];

		if($scope.accion == 'CREAR' && $scope.data.traspaso){
			
			var IdPersonaTraspaso, IdPortafolioTraspaso, IdPersonaTitularPagoTraspaso, IdPortafolioTitularPagoTraspaso = 0;
			IdPersonaTraspaso				= $scope.data.oConfiguracionSocioNegocio.seleccionadoTraspaso.IdPersona;
			IdPortafolioTraspaso			= $scope.data.oConfiguracionPortafolio.seleccionadoTraspaso.IdPortafolio;
			IdPersonaTitularPagoTraspaso	= $scope.data.oConfiguracionSocioNegocioRelacionado.seleccionadoTraspaso.IdPersonaDestino;
			IdPortafolioTitularPagoTraspaso	= $scope.data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso.IdPortafolio;
			var IdConcepto = $scope.data.oConfiguracionConcepto.seleccionadoTraspaso.IdConcepto;
			var arrDataMovimientoTraspaso = {};
			
			arrDataMovimientoTraspaso.IdTipoMovimiento							= $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVING' ? 'MOVEGR' : 'MOVING';
			arrDataMovimientoTraspaso.IdTipoPersona								= $scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso.IdTipoSocioDeNegocio || '';
			arrDataMovimientoTraspaso.IdPersona									= IdPersonaTraspaso || 0;
			arrDataMovimientoTraspaso.IdPortafolio								= IdPortafolioTraspaso || 0;
		    
			arrDataMovimientoTraspaso.IdMoneda									= $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda || '';
			arrDataMovimientoTraspaso.Monto										= $scope.data.kngMonto || 0;
			arrDataMovimientoTraspaso.FechaLiquidacion      					= util.dateSPdate($scope.data.kngFechaLiquidacion);
			arrDataMovimientoTraspaso.IdConcepto								= IdConcepto || '';
		    
			arrDataMovimientoTraspaso.IdTipoPersonaTitularPago  				= $scope.data.titularPagoTraspaso ? (typeof $scope.data.oConfiguracionPortafolioRelacionado.seleccionadoTraspaso.IdPortafolio !== 'undefined' ? 'CLI' : '') : ($scope.data.oConfiguracionTipoSocioNegocio.seleccionadoTraspaso.IdTipoSocioDeNegocio);
			arrDataMovimientoTraspaso.IdPersonaTitularPago      				= $scope.data.titularPagoTraspaso ? (IdPersonaTitularPagoTraspaso || 0) : (IdPersonaTraspaso || 0);
			arrDataMovimientoTraspaso.IdPortafolioTitularPago  					= $scope.data.titularPagoTraspaso ? (IdPortafolioTitularPagoTraspaso || 0) : (IdPortafolioTraspaso|| 0) ;
			arrDataMovimientoTraspaso.IdCaja									= $scope.data.oConfiguracionCaja.seleccionado.IdCaja || 0;
			arrDataMovimientoTraspaso.SecuenciaAgrupacion						= $scope.data.ngSecuenciaAgrupacion || '';
			arrDataMovimientoTraspaso.SecuenciaRelacionada						= null;
			arrDataMovimientoTraspaso.Glosa										= $scope.data.descripcion || '';
			arrDataMovimientoTraspaso.IdEmpleadoDigitador						= $scope.SesionIdEmpleado;
			arrDataMovimientoTraspaso.IdSucursalEmpleadoDigitador				= 0;
			arrDataMovimientoTraspaso.IdTipoComprobante							= '';
			arrDataMovimientoTraspaso.IdComprobante								= 0;
			arrDataMovimientoTraspaso.FechaComprobante							= '0001-01-01';
			arrDataMovimientoTraspaso.FechaLiquidacionComprobante				= '0001-01-01';		
			arrDataMovimientoTraspaso.FolioCartera								= 0;
			arrDataMovimientoTraspaso.IdInstrumento								= 0;
			arrDataMovimientoTraspaso.IdTipoInstrumento							= '';
			arrDataMovimientoTraspaso.IdClasificacionTipoInstrumento			= '';
			arrDataMovimientoTraspaso.IdTipoProducto							= $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProducto || '';
			arrDataMovimientoTraspaso.IdTipoOperacion							= $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion || '';
			arrDataMovimientoTraspaso.IdOperacion								= $scope.data.ngFolioOperacion || 0;
			arrDataMovimientoTraspaso.FechaOperacion							= util.dateSPdate($scope.data.kngFechaOperacion);
			arrDataMovimientoTraspaso.IdCondicionLiquidacion					= $scope.data.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || '';
			arrDataMovimientoTraspaso.IdBolsa									= '';
			arrDataMovimientoTraspaso.IdMonedaOperacion							= '';
			arrDataMovimientoTraspaso.MontoOperacion							= 0;
			arrDataMovimientoTraspaso.IdTipoReajuste							= '';
			arrDataMovimientoTraspaso.FechaParidad								= '0001-01-01';
			arrDataMovimientoTraspaso.ValorParidad								= 0;	
			arrDataMovimientoTraspaso.Bilateral									= $scope.data.indicadorBilateral ? 'S' : 'N';
			arrDataMovimientoTraspaso.GeneraComprobante							= $scope.data.generarComprobante ? 'S' : 'N';		
			arrDataMovimientoTraspaso.AgruparEnComprobante						= $scope.data.agruparEnComprobante ? 'S' : 'N';  
			arrDataMovimientoTraspaso.MueveCuentaInversion						= 'S';
			arrDataMovimientoTraspaso.TitularPago								= $scope.data.titularPagoTraspaso ? 'S' : 'N';		
			arrDataMovimientoTraspaso.NumeroReferencia							= -1;


			//Rescata los valores de la grilla Formas de Pago para insertarlos al parámetro
			var arrDataFormaPagoTraspaso = new Array();			
			arrDataFormaPagoTraspaso = dsFormaDePagoTraspaso.dataColumns([
			                                   'IdMedioDePago',
			                                   'IdModalidadDePago',
			                                   'IdMoneda',
			                                   'Monto',
			                                   'IdCuentaBancariaCorredora',
			                                   'IdTarjetaBancariaCorredora',
			                                   'IdChequera',
			                                   'NumeroDocumentoCorredora',
											   'FechaDocumentoPagoCorredora',
											   'IdCuentaBancariaSocio',
											   'IdBancoSocio',
											   'NombreCuentaBancariaSocio',
											   'NumeroCuentaBancariaSocio',
											   'IdTipoCuentaBancariaSocio',
											   'NombreTipoCuentaBancariaSocio',
											   'IdTarjetaBancariaSocio',
											   'IdTipoTarjetaBancariaSocio',
											   'NombreTipoTarjetaBancariaSocio',
											   'NumeroTarjetaBancariaSocio',
											   'NumeroSerieSocio',
											   'NumeroDocumentoSocio',
											   'FechaDocumentoPagoSocio',
											   ]);
			
			//Ajusta algunos parámetros de forma de pago
			if (arrDataFormaPagoTraspaso !== undefined)
			{
				var i = 1;
				$.each(arrDataFormaPagoTraspaso, function(key,value){
					arrDataFormaPagoTraspaso[key].NumeroReferencia = -1;
					arrDataFormaPagoTraspaso[key].NumeroReferenciaFormaDePago = i * -1;
					arrDataFormaPagoTraspaso[key].FechaDocumentoPagoCorredora = arrDataFormaPagoTraspaso[key].FechaDocumentoPagoCorredora ? util.dateSPdate(util.dateInicioDia(arrDataFormaPagoTraspaso[key].FechaDocumentoPagoCorredora)) : '0001-01-01';
					arrDataFormaPagoTraspaso[key].FechaDocumentoPagoSocio = arrDataFormaPagoTraspaso[key].FechaDocumentoPagoSocio ? util.dateSPdate(util.dateInicioDia(arrDataFormaPagoTraspaso[key].FechaDocumentoPagoSocio)) : '0001-01-01';
					i++;
				});	
					
				parametros.p_FormaDePago = parametros.p_FormaDePago.concat(arrDataFormaPagoTraspaso);	
			}
			
			
			parametros.p_IngresoEgresoGenerar = [arrDataMovimiento,arrDataMovimientoTraspaso] || [];
			parametros.p_EsIngresoAutomatico		= 'N'; //N: Es ingreso Manual
			parametros.p_EsIngresoUI = 'S';
			bcsServiciosTesoreria.CrearTraspaso(
				parametros,
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
						  		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
						dsFormaDePago.removeAll();          //Limpia grilla	
						dsFormaDePagoTraspaso.removeAll();          //Limpia grilla	
						$scope.data.limpiarFormaPago();		//esta en la directiva
						$scope.data.limpiarFormaPagoTraspaso();		//esta en la directiva
						$scope.data.limpiarFormulario();	
						$scope.data.tabActivo.tabDatosBasicos       = true;  //Activa primer tab
						$scope.data.tabActivo.tabFormaDePago        = false;
						$scope.data.tabActivo.tabDatosReferenciales = false;
					}
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_CREAR_MOVIMIENTO_TESORERIA"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				}
			);
		}	
		else if ($scope.accion == 'CREAR')
		{
			parametros.p_IngresoEgresoGenerar		= [arrDataMovimiento] || [];
		    parametros.p_EsIngresoAutomatico		= 'N'; //N: Es ingreso Manual
			parametros.p_EsIngresoUI = 'S';
			bcsServiciosTesoreria.CrearIngresoEgreso(
				parametros,
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
						  		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
						dsFormaDePago.removeAll();          //Limpia grilla	
						dsFormaDePagoTraspaso.removeAll();          //Limpia grilla	
						$scope.data.limpiarFormaPago();		//esta en la directiva
						$scope.data.limpiarFormaPagoTraspaso();		//esta en la directiva
						$scope.data.limpiarFormulario();	
						$scope.data.tabActivo.tabDatosBasicos       = true;  //Activa primer tab
						$scope.data.tabActivo.tabFormaDePago        = false;
						$scope.data.tabActivo.tabDatosReferenciales = false;
					}
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_CREAR_MOVIMIENTO_TESORERIA"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				}
			);
		}
		else if ($scope.accion == 'MODIFICAR')
		{
			parametros.p_IngresoEgresoModificar		= [arrDataMovimiento] || [];
			bcsServiciosTesoreria.ModificarIngresoEgreso(
				parametros, 
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
					  		
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaData($scope.IdVentana, true);  //Asocia data a ventana, la cual será enviada como respuesta de la promesa		
						bcsComunes.ventanaCerrar($scope.IdVentana);
					};
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_MODIFICAR_MOVIMIENTO_TESORERIA"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				}
			);
		}

    };
	
	
	
	$timeout(function(){
		if ($scope.accion === 'MODIFICAR') 
		{			
			cargarDatosParaEdicion($scope.objIngresoEgreso);
		}
	},1000);
	
	// Para inicializar la altura de la ventana con el formato optimo sin alterar el responsive

	if($scope.accion === "CREAR"){
		var firstHeightCrear = $('#Win_CrearIngresoEgreso');
		firstHeightCrear.css('height', '630px');
	}
	else{
		var firstHeightEditar = $('#Win_EditarIngresoEgreso');
		firstHeightEditar.css('height', '620px');
	}
});
