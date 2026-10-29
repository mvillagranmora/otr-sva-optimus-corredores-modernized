angular.module('ModuloPlataforma').controller('ConsultarVectorPrecioCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.configData = {};
	$scope.filtros = {};
	$scope.recordData = {};
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		
	$scope.configResultados = {
		dataSource			: new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				groupable 			: true, 
				sort 				: { field: "Fecha", dir: "asc" }, // Criterios de Ordenamiento.
				pageSize 			: 25, // Número de registros por página.
				transport 			: {
										read : {
											url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=100043",
											dataType : "json"
										}
									},
				schema 				: {
					model: {
						fields 		: {
								        Fecha : {type: "string"},
								        Nemotecnico : {type: "string"},
								        Entidad : {type: "string"},
								        TipoRegistro : {type: "string"},
								        ValorRegistro : {type: "number"},
						}
					}
				},
			}
		}),
		columns				: [
								{field: "Fecha",title: "UI_FECHA_INGRESO", format: "{0:n5}",width: "150px"},
								{field: "Nemotecnico",title: "UI_NEMOTECNICO", format: "{0:n5}",width: "150px"},
								{field: "Entidad",title: "UI_ENTIDAD", format: "{0:n5}",width: "150px"},
								{field: "TipoRegistro",title: "UI_TIPO_REGISTRO", format: "{0:n5}",width: "150px"},
								{field: "ValorRegistro",title: "UI_VALOR", format: "{0:n5}",width: "150px"},
							],
		gridCreated: function(grid) {
			$scope.mainGrid = grid;
		},
		sortable			: true,
		pageable			: true,
		filterable			: true,
		groupable           : true,
		//autoBind			: true,
		height				: 300,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable 			: "row"
	};
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	$scope.buscar = function(){
		var objfilters = { };
		var niv1filters = new Array();
		objfilters.logic = 'and';
		objfilters.filters = niv1filters;

		if ($scope.filtros.fechaDesde != undefined) {
	    		if(!!$scope.filtros.fechaHasta){
	    			var resp = util.validarRangoFecha($scope.filtros.fechaDesde, $scope.filtros.fechaHasta,366);
					if(resp.estado){
						bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
						return;
					}	
	    		}
	    		niv1filters.push({ field: "Fecha", operator: "gte", value: util.dateSPdate($scope.filtros.fechaDesde) });	
	    					
				
			}
			if ($scope.filtros.fechaHasta != undefined) {
				niv1filters.push({ field: "Fecha", operator: "lte", value: util.dateSPdate(util.dateTerminoDia($scope.filtros.fechaHasta)) });
			}
		
		if(!!$scope.filtros.instrumento){
			niv1filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtros.instrumento.Nemotecnico });
		}

		$scope.mainGrid.dataSource.filter(objfilters);
	}
	
});
