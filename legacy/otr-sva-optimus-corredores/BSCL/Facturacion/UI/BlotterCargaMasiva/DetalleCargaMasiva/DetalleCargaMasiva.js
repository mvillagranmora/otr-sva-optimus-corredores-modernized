angular.module('ModuloFacturacion').controller('DetalleCargaMasivaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsBlotterCargaMasivaGrid)
{	
	
	$scope.parametros 	= {};	
	$scope.IdVentana  	= bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	$scope.oCargaMasiva = $scope.parametros;
 
	//------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//------------------------------------------------
	var dsDefCargaMasivaDetalle = bcsBlotterCargaMasivaGrid.dsDefCargaMasivaDetalle;
	dsDefCargaMasivaDetalle.definition.filter = { field: "IdCargaMasiva", operator: "eq", value: $scope.oCargaMasiva.IdCargaMasiva }	
	var dsCargaMasivaDetalle = new bcs.DataSource(dsDefCargaMasivaDetalle);
		
		
	$scope.confBasicaCargaMasivaDetalle = {
			dataSource			: dsCargaMasivaDetalle,		
			columns				: bcsBlotterCargaMasivaGrid.columnasCargaMasivaDetalle(),		
			sortable			: true,
			pageable			: false,		
			height				: 350,
			autoBind			: true,
			selectable 			: "multiple row",
			userSettings        : true, 
			rowColorBackground	: function(row) { 
									 var color = "";
									 if (row.IdEstadoCargaMasivaDetalle == 'INV')
										color = "red";
									 return color;
								  },								  
			gridCreated			: function(g) {
										var gridCargaMasivaDetalle = g;
								  }
			
	};
	
	
	
	
	
});
