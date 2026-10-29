angular.module('ModuloTesoreria').controller('DetalleCargaMasivaOperacionSpotCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate, bcsBlotterCargaMasivaOperacionSpotGrid)
{	
	$scope.oCargaMasiva = {};
	
	$scope.IdVentana  = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.IdVentana);

	$scope.oCargaMasiva = $scope.parametros.oCargaMasiva;
	

	//------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//------------------------------------------------
	var dsDefCargaMasivaDetalle = bcsBlotterCargaMasivaOperacionSpotGrid.dsDefCargaMasivaDetalleOperacionSpot;
	dsDefCargaMasivaDetalle.definition.filter = { field: "IdCargaMasiva", operator: "eq", value: $scope.oCargaMasiva.IdCargaMasiva }	
	var dsCargaMasivaDetalle = new bcs.DataSource(dsDefCargaMasivaDetalle);
	
	$scope.confBasicaCargaMasivaDetalleOperacionSpot = {
			dataSource			: dsCargaMasivaDetalle,		
			columns				: bcsBlotterCargaMasivaOperacionSpotGrid.columnasCargaMasivaDetalle(),		
			sortable			: true,
			pageable			: false,		
			height				: 350,
			autoBind			: true,
			selectable 			: "multiple row",
			rowColorBackground	: function(row) { 
									 var color = "";
									 if (row.IdEstadoCargaMasivaDetalle == 'INV')
										color = "red";
									 return color;
								  },
			
	};
	
	
});

