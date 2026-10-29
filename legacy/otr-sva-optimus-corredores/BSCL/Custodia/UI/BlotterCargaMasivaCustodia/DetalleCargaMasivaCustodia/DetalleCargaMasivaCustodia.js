angular.module('ModuloTesoreria').controller('DetalleCargaMasivaCustodiaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate, bcsBlotterCargaMasivaCustodiaGrid)
{	
	$scope.oCargaMasiva = {};
	
	$scope.IdVentana  = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.IdVentana);

	$scope.oCargaMasiva = $scope.parametros.oCargaMasiva;
	

	//------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//------------------------------------------------
	var dsDefCargaMasivaDetalle = bcsBlotterCargaMasivaCustodiaGrid.dsDefCargaMasivaDetalleCustodia;
	dsDefCargaMasivaDetalle.definition.filter = { field: "IdCargaMasiva", operator: "eq", value: $scope.oCargaMasiva.IdCargaMasiva }	
	var dsCargaMasivaDetalle = new bcs.DataSource(dsDefCargaMasivaDetalle);
	
	$scope.confBasicaCargaMasivaDetalleCustodia = {
			dataSource			: dsCargaMasivaDetalle,		
			columns				: bcsBlotterCargaMasivaCustodiaGrid.columnasCargaMasivaDetalle(),		
			sortable			: true,
			pageable			: true,		
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

