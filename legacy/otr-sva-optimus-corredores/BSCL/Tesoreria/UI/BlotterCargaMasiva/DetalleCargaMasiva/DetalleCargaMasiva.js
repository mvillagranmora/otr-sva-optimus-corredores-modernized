angular.module('ModuloTesoreria').controller('DetalleCargaMasivaCtrl',
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
				
				//Se activan columnas de 'CONCILIACION_RECAUDACION_BCP'
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
				}
				
				
				//Se activan columnas de 'EGRESOS_POR_ABONOS_BCP_PAC'
				if ($scope.oCargaMasiva.CodigoTipoCargaMasiva == 'EGRESOS_POR_ABONOS_BCP_PAC')
				{
					gridCargaMasivaDetalle.showColumn("Ori_TipoCuenta");
					gridCargaMasivaDetalle.showColumn("Ori_NumeroCuenta");
					gridCargaMasivaDetalle.showColumn("Ori_Moneda");
					gridCargaMasivaDetalle.showColumn("Ori_TipoDocumentoIdentidad");
					gridCargaMasivaDetalle.showColumn("Ori_DocumentoIdentidad");
				}
				
				
			}
								  
			
	};
	
	
	
	
	
});
