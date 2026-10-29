angular.module('ModuloPlataforma').controller('MantenedorBancoCustodioCtrl',function($scope,$translate,bcsComunes){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
		
	$scope.crearBancoCustodio = function() {
		var id = bcsComunes.ventanaAbrir("ModalBancoCustodio", {},false,false,$scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id);                               
        promesa.then(function(res){
        	   $scope.apiGrillaBancoCustodio.dataSource.read();
        	   $scope.seleccionado = undefined;
        });
		
	}
		
	$scope.modificarBancoCustodio = function() {
		var id = bcsComunes.ventanaAbrir("ModalBancoCustodio", { bancoCustodio : $scope.seleccionado},false,false,$scope.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id);                               
        promesa.then(function(res){
        	   $scope.apiGrillaBancoCustodio.dataSource.read();
        	   $scope.seleccionado = undefined;
        });
	}
	

	$scope.grillaBancoCustodio = {
			columns				: [
				{field: "Codigo",						width: "100px",title: $translate.instant("UI_CODIGO")},
				{field: "DocumentoIdentidad",			width: "100px",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD")},
				{field: "NombreCompleto",				width: "100px",title: $translate.instant("UI_NOMBRE_COMPLETO")},
				{field: "EsBancoLiquidadorUI",			width: "100px",title: $translate.instant("UI_BANCO_LIQUIDADOR")},
				{field: "EsBancoCustodioUI",			width: "100px",title: $translate.instant("UI_BANCO_CUSTODIO")},
				{field: "NombreEstado",					width: "100px",title: $translate.instant("UI_ESTADO")},
				
			],
			gridCreated: function(grid) {
				$scope.apiGrillaBancoCustodio = grid;
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
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1813",
							dataType : "json"
						}
					},
					schema 			: {
						model : {
							fields : {
								Codigo 				: { type : "string" },
								DocumentoIdentidad 	: { type : "string" },
								NombreCompleto 		: { type : "string" },
								NombreEstado		: { type : "string" },
								EsBancoLiquidadorUI		: { type : "string" },
								EsBancoCustodioUI		: { type : "string" },
								
								
							}
						}
					},
					
				}
			}),
			
	}
	
	
});

angular.module('ModuloPlataforma').factory('servicioBancoCustodio',function($resource) {
	var urlCrear = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3514";
	var urlActualizar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3512";
	return $resource("", {},
	{
		'crearBancoCustodio' 		: {method: "POST",url:urlCrear},
		'actualizarBancoCustodio' 	: {method: "POST",url:urlActualizar},
	});
});