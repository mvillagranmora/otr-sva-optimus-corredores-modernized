angular.module('ModuloOrdenes').controller('DetalleReportanteCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$timeout,$translate) {

		$scope.datos = {};
		$scope.formData={};
		var idVentana = bcsComunes.ventanaWinId($scope);
	
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
			console.log(JSON.stringify($scope.parametros));
		if (!$scope.parametros.IdOrden)
			console.log("Orden no existe!");
		else {
			bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_INFORMACION_ORDEN") + ' ' + $scope.parametros.IdOrden);
			var ordenDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarOrden'),
							dataType: "json"
						}
					},
	    		    filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
	    		    schema: bcsConfiguracionOrdenesV2.getSchemaOrden()
	    		}});
		
			var asignacionDatasource = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering:true,
		    		transport: {
		    			read: {
		    				url: bcsConsultasOrdenes('ConsultarAsignacionesUI'),
		    				dataType: "json"
		    		    }
		    		},
		    		filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
					schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion()
				}
			});

			var cobrosOrdenDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
						transport: {
							read: {
								url: bcsConsultasOrdenes('ConsultarCobrosOrden'),
								dataType: "json"
								}
						},
						filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
						schema: bcsConfiguracionOrdenesV2.getSchemaCobrosOrden(),
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
						filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
						schema: bcsConfiguracionOrdenesV2.getSchemaMovFacturar(),
				}
			});
		
			asignacionDatasource.fetch().then(function(){
				$scope.datos.asignaciones = asignacionDatasource.data();
			});
			movFacDatasource.fetch().then(function(){
				$scope.datos.movimientos = movFacDatasource.data();
			});
			ordenDatasource.fetch().then(function(){
				//bcsComunes.ventanaOverlay(idVentana,false);
				if (ordenDatasource.data().length!=1) {
					console.log("Orden no existe!");
					return false;
				}
				else {
					//$scope.$apply(function() {
						$scope.orden = ordenDatasource.data()[0];
						console.log("Orden:"+JSON.stringify($scope.orden));
						$scope.esVentaReporte=$scope.orden.IdTipoOperacion=="VENRRV" || $scope.orden.IdTipoOperacion=="VENRRF"? true:false;
						$scope.orden.DecimalesCantidad = util.bcs_formato(0,'ORD','Cantidad',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.DecimalesMonto = util.bcs_formato(0,'ORD','Monto',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.DecimalesTasa = util.bcs_formato(0,'ORD','Tasa',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.DecimalesPrecio = util.bcs_formato(0,'ORD','Precio',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.EsOrdenEspecialTexto = ($scope.orden.EsOrdenEspecial=='S')?'SI':'NO';
						$timeout(function(){bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));},20);
					//	});
					return true;
				}

			},function(result){
				console.log(result);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
			
			
		}
		
		$scope.verBitacora = function()
		{
			bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));
			bcsComunes.ventanaAbrir("BitacoraOrden", {IdOrden : $scope.parametros.IdOrden},false,false );
		};

		$scope.verComprobante = function(){
			bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,$scope.orden.DocumentoIdentidad,$scope.orden.FechaOperacionOrden,$scope.orden.IdOrden,$scope.orden.MontoLiquidacion,idVentana);			
		};

		
		
					
});
