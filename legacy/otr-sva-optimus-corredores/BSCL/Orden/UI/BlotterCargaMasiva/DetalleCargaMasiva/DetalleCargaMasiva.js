angular.module('ModuloOrdenes').controller('DetalleCargaMasivaCtrl',
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
	
	/*
	if ($scope.oCargaMasiva.CodigoTipoCargaMasiva == 'CONCILIACION_RECAUDACION_BCP')
	{	
		gridCargaMasivaDetalle.showColumn("BCP_CON_CodigoCuenta");
		gridCargaMasivaDetalle.showColumn("BCP_CON_CodigoMoneda");
		gridCargaMasivaDetalle.showColumn("BCP_CON_NumeroCuenta");
		gridCargaMasivaDetalle.showColumn("BCP_CON_IdentificacionDepositante");
		gridCargaMasivaDetalle.showColumn("BCP_CON_FechaTransaccion");
		gridCargaMasivaDetalle.showColumn("BCP_CON_NumeroOperacion");
		gridCargaMasivaDetalle.showColumn("BCP_CON_ReferenciaOperacion");
		gridCargaMasivaDetalle.showColumn("BCP_CON_IndicadorExtorno");
		gridCargaMasivaDetalle.showColumn("BCP_CON_NumeroOperacionBD");
	}*/
				
	$scope.confBasicaCargaMasivaDetalle = {
			dataSource			: dsCargaMasivaDetalle,		
			columns				: $scope.oCargaMasiva.CodigoTipoCargaMasiva == 'CARGA_INGRESO_ORDEN_MASIVO' ? 
									bcsBlotterCargaMasivaGrid.columnasCargaMasivaDetalle() : bcsBlotterCargaMasivaGrid.columnasCargaMasivaInstruccionDetalle(),		
			sortable			: true,
			pageable			: true,	
			height				: 325,
			autoBind			: true,
			userSettings 		: true,
			selectable 			: "multiple row",
			rowColorBackground	: function(row) { 
									 var color = "";
									 if (row.IdEstadoCargaMasivaDetalle == 'ING')
										color = "yellow";
									 if (row.IdEstadoCargaMasivaDetalle == 'INV')
										color = "red";
									 if (row.IdEstadoCargaMasivaDetalle == 'PRO')
										color = "green";
									 return color;
								  },								  
			gridCreated: function(g)
			{
				var gridCargaMasivaDetalle = g;				
			}					  
			
	};

});
