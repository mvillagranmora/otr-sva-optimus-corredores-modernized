angular.module('ModuloPlataforma').controller('DetalleCargaMasivaPlataformaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate, bcsBlotterCargaMasivaPlataformaGrid)
{	
	$scope.oCargaMasiva = {};
	
	$scope.IdVentana  = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.IdVentana);

	$scope.oCargaMasiva = $scope.parametros.oCargaMasiva;
	

	//------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//------------------------------------------------
	var dsDefCargaMasivaDetalle = bcsBlotterCargaMasivaPlataformaGrid.dsDefCargaMasivaDetallePlataforma;
	dsDefCargaMasivaDetalle.definition.filter = { field: "IdCargaMasiva", operator: "eq", value: $scope.oCargaMasiva.IdCargaMasiva }	
	var dsCargaMasivaDetalle = new bcs.DataSource(dsDefCargaMasivaDetalle);
	
	$scope.confBasicaCargaMasivaDetallePlataforma = {
			dataSource			: dsCargaMasivaDetalle,		
			columns				: bcsBlotterCargaMasivaPlataformaGrid.columnasCargaMasivaDetalle(),		
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

