angular.module('ModuloTesoreria').controller('ConsultarEnvioDocumentosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
				 $translate,bcsServiciosTesoreria, bcsBlotterComprobantesGrid)
{	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo = true;

	$scope.winID = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);

	//SETEO INICIAL
	$scope.kngFechaEnvioDocumentosDesde	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaEnvioDocumentosHasta	= util.dateTerminoDia($scope.SesionFechaSistema);

	
	//--------------------------------------
	// CONFIGURACION DE COMBOBOX
	//--------------------------------------
	
	$scope.oConfiguracionMedioDePagoTesoreria={
		tipo 			: "combobox-medio-pago-tesoreria",
		configuracion 	: {},
		filtros 		: {
					IdEstado 					: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
					RequiereRevisionDocumento 	: { "field"   : "RequiereRevisionDocumento","operator": "eq", "value"   : "S" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};
	
	$scope.oConfiguracionEstadoDocumentoTesoreria={
		tipo 			: "combobox-estado-tesoreria",
		configuracion 	: {},
		filtros 		: {
					NombreEntidad 	: {"field": "NombreEntidad", "operator": "eq",  "value": 'TES.EnvioDocumento'},
					IdEstado 		: {"field": "IdEstado",      "operator": "neq", "value": "ANU" }
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''			
	};
	
	$scope.oConfiguracionCaja={
		tipo 			: "combobox-caja",
		configuracion 	: {},
		filtros 		: {
				//IdEstadoCaja : { "field"   : "IdEstadoCaja","operator": "eq", "value"   : "ABT" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''			
	};
	
	$scope.oConfiguracionCajaMoneda={
		tipo 			: "combobox-caja-moneda",
		configuracion 	: {},
		filtros 		: {
					IdCaja 		: { "field": "IdCaja",	 "operator": "eq",   "value"   : "" },
					IdMoneda 	: { "field": "IdMoneda", "operator": "neq",  "value"   : "null" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''			
	};




	//--------------------------------------
	// CONFIGURACION DE GRILLA
	//--------------------------------------
	
	var dsEnvioDocumentos = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefEnvioDocumentos);	
	$scope.confBasicaEnvioDocumentos = {
			dataSource			: dsEnvioDocumentos,
			columns				: bcsBlotterComprobantesGrid.columnasEnvioDocumentos(),
			sortable			: true,
			pageable			: false,
			autoBind			: false,
			filterable			: true,
			height				: 488,
			selectable 			: "row",
			userSettings		: true,
			navigatable         : true,
			gridCreated 		: function(g) { 
									$scope.ApiComprobantes = g;
			}
	};
	
	
	
	//--------------------------------------
	// FUNCIONES
	//--------------------------------------	
	$scope.buscarEnvioDocumentos = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		
		filters.push({ field: "FechaEnvio", operator: "gte", value: util.dateInicioDia($scope.kngFechaEnvioDocumentosDesde) });
		filters.push({ field: "FechaEnvio", operator: "lte", value: util.dateTerminoDia($scope.kngFechaEnvioDocumentosHasta) });		
		
		filters.push({ field: "IdCaja", operator: "eq", value: $scope.oConfiguracionCaja.seleccionado.IdCaja || -1 });		
		filters.push({ field: "IdMedioDePago", operator: "eq", value: $scope.oConfiguracionMedioDePagoTesoreria.seleccionado.IdMedioDePago || -1 });
		filters.push({ field: "IdMoneda", operator: "eq", value: $scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda || -1 });
		filters.push({ field: "IdEstado", operator: "eq", value: $scope.oConfiguracionEstadoDocumentoTesoreria.seleccionado.IdEstado || -1 });

		
		let filtrosGrid = []
		if($scope.ApiComprobantes.dataSource.ds._filter != undefined  
		&& $scope.ApiComprobantes.dataSource.ds._filter.filters != undefined  ) {
			filtrosGrid = $scope.ApiComprobantes.dataSource.ds._filter.filters;
		}
	
		//array filters son los filtros de busqueda
		
		var hashmap = new Map();
		if(!$scope.oConfiguracionEstadoDocumentoTesoreria.seleccionado.IdEstado == 0){
			hashmap.clear();
			//pusheamos los valores blotter 
			filters.forEach(element =>{
				hashmap.set(element.field+"**"+element.operator, element)
			});
		
			filtrosGrid.forEach(element =>{
				//preguntar si ya existe valor de grilla, se respeta valor de boton buscar
				let aux = hashmap.get(element.field+"**"+element.operator);
				if(!aux){
					//pusheamos los valores grilla
					hashmap.set(element.field+"**"+element.operator, element)
				}
			});
			
			
		}else{
			hashmap.clear();
			//pusheamos los valores blotter 
			filters.forEach(element =>{
				hashmap.set(element.field+"**"+element.operator, element)
			});
		
			filtrosGrid.forEach(element =>{
				//preguntar si ya existe valor de grilla, se respeta valor de boton buscar
				let aux = hashmap.get(element.field+"**"+element.operator);
				if(!aux && element.field != "IdEstado"){
					//pusheamos los valores grilla
					hashmap.set(element.field+"**"+element.operator, element)
				}
			});
		}
		
		//limpiamos filters
		filters = new Array();
		
		//llenamos filters desde el hash
		hashmap.forEach( (value, key, map) => {
			if (value.value != -1){
				filters.push(value);
			}
		});
		
		dsEnvioDocumentos.filters(filters);	
	};
	

	
	$scope.limpiarFiltros = function() 
	{		
		$scope.oConfiguracionMedioDePagoTesoreria.idActual 	= 0;
		$scope.oConfiguracionCaja.idActual 			= 0;
		$scope.oConfiguracionCajaMoneda.idActual 	= 0;		
		$scope.oConfiguracionEstadoDocumentoTesoreria.idActual = 0;
		$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
		dsEnvioDocumentos.removeAll();
		$scope.seleccionEnvioDocumentos = [];
	};


	//Abre modal con el detalle de los envios de documentos 
	$scope.envioDocumentosDetalle = function(row)
	{
		var ventana = bcsComunes.ventanaAbrir("EnvioDocumentosDetalle", row, true, false, $scope.winID );

		var promesa = bcsComunes.ventanaPromesa(ventana);
		promesa.then(function(data){			
			$scope.buscarEnvioDocumentos();
		});
	};	
	
	$scope.$on('$viewContentLoaded', function() {
		$timeout(function() {
		  var iconoConfiguracion = angular.element('bcs-grid#grid-envio-documentos > span> div.bcsgridconf');
		  iconoConfiguracion.css('display', 'block');
		});
	  });
	
});