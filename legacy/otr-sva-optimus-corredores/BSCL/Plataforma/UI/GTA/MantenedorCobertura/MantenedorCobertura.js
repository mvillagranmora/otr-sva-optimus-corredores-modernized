angular.module('ModuloPlataforma').controller('MantenedorCoberturaCtrl',
		function($scope,$translate,$timeout,$state,$modal,bcsComunes,
				bcsConfiguracionGTA,bcsServiciosGta,bcsServicioPlataforma)
{
	
	$scope.data	= {};
	$scope.filtros = {};
	$scope.criteriosFiltros = {};
	$scope.data.showPlazoTope = false;  //Debe aparecer sólo cuando la cobertura sea de RF o IF.
	
	
	//CONFIGURACION GRILLA
	$scope.confBasicaCoberturaGarantia = {
		dataSource			: bcsConfiguracionGTA.dataSourceCobertura,
		columns				: bcsConfiguracionGTA.columnasCobertura(),
		sortable			: true,		
		filterable          : true,
		pageable			: true,
		//autoBind			: true,
		height				: 440,
		userSettings		: true,
		navigatable			: true,
		excel 				: {allPages: true,},
		selectable 			: "row"
	};
	

	
	//CONFIGURACION COMBOBOX
	$scope.filtros.oConfiguracionEntidadDestinoGarantia={
			tipo 			: "combobox-entidad-destino-garantia",
			configuracion 	: {},
			filtros 		: {IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" }},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
	

	$scope.filtros.oConfiguracionTipoInstrumentoExigeGarantia={
		tipo 			: "combobox-tipo-instrumento",
		configuracion 	: {},
		filtros 		: {
				IdEstado 				: { "field"   : "IdEstado",					"operator": "eq", "value"   : "VIG" },
				ExigeGarantia 			: { "field"   : "ExigeGarantia",			"operator": "eq", "value"   : "S" },
				IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },			
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};
	

	$scope.filtros.oConfiguracionInstrumentoExigeGarantia={
		tipo 			: "combobox-instrumento-bolsa",
		configuracion 	: {},
		filtros 		: {
			IdEstado 		  		: { "field"   : "IdEstado",         		"operator": "eq", "value"   : "ACT" },
			ExigeGarantia 	  		: { "field"   : "ExigeGarantia",    		"operator": "eq", "value"   : "S" },
			IdTipoInstrumento 		: { "field"   : "IdTipoInstrumento",		"operator": "eq", "value"   : "" },
			IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};
	
	
	// -----------------------------------------------
	// FUNCIONES
	// -----------------------------------------------	
	
	$scope.buscarCoberturaGarantia = function(){
		var filters = new Array();
		
		if ($scope.filtros.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia)
			filters.push( { "field": "IdEntidadDestinoGarantia","operator": "eq", "value" : $scope.filtros.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia});	
		
		if ($scope.filtros.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumentoExigeGarantia","operator": "eq", "value" : $scope.filtros.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento});
		
		if ($scope.filtros.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento)
			filters.push( { "field": "IdInstrumentoExigeGarantia","operator": "eq", "value" : $scope.filtros.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento});
		
		console.log("FILTROS:" + JSON.stringify(filters));
		bcsConfiguracionGTA.dataSourceCobertura.filter(filters);
	};
	
 
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.oConfiguracionEntidadDestinoGarantia.idActual = 0;
		$scope.filtros.oConfiguracionTipoInstrumentoExigeGarantia.idActual = 0;
		$scope.filtros.oConfiguracionInstrumentoExigeGarantia.idActual = 0;		
		bcsConfiguracionGTA.dataSourceCobertura.removeAll();
		
	};
	
	
	//Abre el formulario para crear una nueva cobertura de garantía
	$scope.crearCoberturaGarantia = function() 
	{	
			var objDatosExigeGarantia = {};
			objDatosExigeGarantia.Accion 						 = 'CREAR';
			objDatosExigeGarantia.IdEntidadDestinoGarantia 		 = $scope.filtros.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia || 0;			
			objDatosExigeGarantia.IdTipoInstrumentoExigeGarantia = $scope.filtros.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento || '';
			objDatosExigeGarantia.IdInstrumentoExigeGarantia 	 = $scope.filtros.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento || 0;
			
			var ventanaCrearCobertura = bcsComunes.ventanaAbrir("EditarCobertura",objDatosExigeGarantia,false,false);
			
			//Retorna una promesa, cuando la ventana se cierre
	        var promesa = bcsComunes.ventanaPromesa(ventanaCrearCobertura);                               
	        $.when(promesa).then(function(data){
	              bcsConfiguracionGTA.dataSourceCobertura.read();
	        });		
	};
	
	
	//Abre formulario para modificar una cobertura de garantía
	$scope.editarCobertura = function(seleccionCoberturaGarantia)
	{
			seleccionCoberturaGarantia.Accion 	= 'MODIFICAR';
			var ventanaEditarCobertura = bcsComunes.ventanaAbrir("EditarCobertura", seleccionCoberturaGarantia,false,false);
			
			//Retorna una promesa, cuando la ventana se cierre
	        var promesa = bcsComunes.ventanaPromesa(ventanaEditarCobertura);                               
	        $.when(promesa).then(function(data){
	              bcsConfiguracionGTA.dataSourceCobertura.read();
	        });
	}
	
	
});

