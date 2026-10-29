angular.module('ModuloOrdenes').controller('DetalleTransaccionCtrl',
		function ($scope,bcsComunes,$translate,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes) {

	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
	
	var ConsultarTransacciones = $scope.parametros.IdClasificacionTipoInstrumento === "RE"?'ConsultarTransaccionesReporteUI':'ConsultarTransaccionesUI';
	var ConsultarAsignaciones =	$scope.parametros.IdClasificacionTipoInstrumento === "RE"?'ConsultarAsignacionesReporteUI':'ConsultarAsignacionesUI';
	if (!$scope.parametros.IdTransaccion)
		console.log("Transaccion no existe!");
	else {
		var transaccionDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: bcsConsultasOrdenes(ConsultarTransacciones),
	    				dataType: "json"
	    		    }
	    		},
					filter: { field: "IdTransaccion", operator: "eq", value: Number($scope.parametros.IdTransaccion) },
					schema: bcsConfiguracionOrdenesV2.getSchemaTransaccion(),
			}
		});

		var asignacionDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes(ConsultarAsignaciones),
							dataType: "json"
							}
					},
					filter: { field: "IdTransaccion", operator: "eq", value: Number($scope.parametros.IdTransaccion) },
					schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion(),
			}
		});

		var movFacDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarMovimientosFacturar'),
							dataType: "json"
							}
					},
					filter: { field: "IdTransaccion", operator: "eq", value: Number($scope.parametros.IdTransaccion) },
					schema: bcsConfiguracionOrdenesV2.getSchemaMovFacturar(),
			}
		});

		transaccionDatasource.fetch().then(function(){
			if (transaccionDatasource.data().length!=1) {
				console.log("Transaccion no existe!");
				return false;
			}
			else {
				$scope.$apply(function() {
					$scope.transaccion = transaccionDatasource.data()[0];
					$scope.transaccion.FechaLiquidacion = util.dateDS($scope.transaccion.FechaLiquidacion,"FechaSistema","ODATA");
					
					
					$scope.transaccion.DecimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',$scope.transaccion.IdMonedaTransaccion,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					$scope.transaccion.DecimalesMonto = util.bcs_formato(0,'GEN','Monto',$scope.transaccion.IdMonedaTransaccion,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					$scope.transaccion.DecimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.transaccion.IdMonedaTransaccion,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					$scope.transaccion.DecimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.transaccion.IdMonedaTransaccion,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					
					
					bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_INFORMACION_TRANSACCION") + ' ' + $scope.transaccion.InstrumentoUI + ' - ' + $scope.transaccion.FolioTransaccion);
					
				});
				return true;
			}
		}).then(function(){
			asignacionDatasource.query().then(function(){
				$scope.$apply(function() {$scope.asignaciones = asignacionDatasource.data();});
				return true;
			})
		}).then(function(){
			movFacDatasource.query().then(function(){
				$scope.$apply(function() {$scope.movimientos = movFacDatasource.data();});
				return true;
			})
		});
	}
	
	$scope.verMapaRelaciones = function()
	{
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "TRANSACCION",IdEntidad :$scope.parametros.IdTransaccion},false,false );
	};
});
