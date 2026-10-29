angular.module('ModuloSocioNegocio').controller('DetalleCargaMasivaSocioNegocioCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsCargaMasivaGrid)
{	
	$scope.oCargaMasiva = {};
	
	$scope.IdVentana  = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.IdVentana);

	$scope.oCargaMasiva = $scope.parametros;
	

	//------------------------------------------------
	// CONFIGURACION DE GRILLAS
	//------------------------------------------------
	var dsDefCargaMasivaDetalle = bcsCargaMasivaGrid.dsDefCargaMasivaDetalle;
	dsDefCargaMasivaDetalle.definition.filter = { field: "IdCargaMasiva", operator: "eq", value: $scope.oCargaMasiva.IdCargaMasiva }	
	var dsCargaMasivaDetalle = new bcs.DataSource(dsDefCargaMasivaDetalle);
	
	$scope.confBasicaCargaMasivaDetalleSocioNegocio = {
		dataSource			: dsCargaMasivaDetalle,		
		columns				: bcsCargaMasivaGrid.columnasCargaMasivaDetalle(),		
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
