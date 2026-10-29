angular.module('ModuloPlataforma').controller('MantenedorBrokerExtranjeroCtrl',function($scope,$translate,bcsComunes){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
		
	$scope.crearBrokerExtranjero = function() {
		var id = bcsComunes.ventanaAbrir("ModalBrokerExtranjero", {},false,false,$scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id);                               
        promesa.then(function(res){
        	   $scope.apiGrillaBrokerExtranjero.dataSource.read();
        	   $scope.seleccionado = undefined;
        });
		
	}
		
	$scope.modificarBrokerExtranjero = function() {
		var id = bcsComunes.ventanaAbrir("ModalBrokerExtranjero", { brokerExtranjero : $scope.seleccionado},false,false,$scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id);                               
        promesa.then(function(res){
        	   $scope.apiGrillaBrokerExtranjero.dataSource.read();
        	   $scope.seleccionado = undefined;
        });
	}
	

	$scope.grillaBrokersExtranjeros = {
			columns				: [
				{field: "Codigo",						width: "100px",title: $translate.instant("UI_CODIGO")},
				{field: "DocumentoIdentidad",			width: "100px",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD")},
				{field: "NombreCompleto",				width: "100px",title: $translate.instant("UI_NOMBRE_COMPLETO")},
				{field: "NombreEstado",					width: "100px",title: $translate.instant("UI_ESTADO")},
				
				
			],
			gridCreated: function(grid) {
				$scope.apiGrillaBrokerExtranjero = grid;
			},
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 280,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row",
			dataSource			: new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true, 
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3511",
							dataType : "json"
						}
					},
					schema 			: {
						model : {
							fields : {
								Codigo 				: { type : "string" },
								DocumentoIdentidad 	: { type : "string" },
								NombreCompleto 		: { type : "string" },
								NombreEstado		: { type : "string" }
								
							}
						}
					},
					
				}
			}),
			
	}
	
	
});

angular.module('ModuloPlataforma').factory('servicioBrokerExtranjero',function($resource) {
	var urlCrear = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3515";
	var urlActualizar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3513";
	return $resource("", {},
	{
		'crearBrokerExtranjero' 		: {method: "POST",url:urlCrear},
		'actualizarBrokerExtranjero' 	: {method: "POST",url:urlActualizar},
	});
});