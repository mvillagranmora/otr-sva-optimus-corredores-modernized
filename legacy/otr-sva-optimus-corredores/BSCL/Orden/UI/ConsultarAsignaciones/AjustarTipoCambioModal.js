angular.module('ModuloOrdenes').controller('AjustarTipoCambioModalCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes, bcsServicioOrden,$translate,bcsAlerta, OPCIONES_BCSALERTA) {

    var idVentana 				 = bcsComunes.ventanaWinId($scope);
    $scope.parametros			 = bcsComunes.ventanaParams(idVentana);
    $scope.data 				 = {};
	$scope.data.AjustaTipoCambio = false;

	if ($scope.parametros.AjustaTipoCambio) {
		$scope.data.AjustaTipoCambio = $scope.parametros.AjustaTipoCambio
	}

    if (!$scope.parametros.IdAsignacion)
		console.log("Asignacion no existe!");
	else {
		var asignacionDatasource = new bcs.DataSource({
			type : "odata",
  			definition : {
				serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarAsignacionesUI'),
							dataType: "json"
							}
					},
					filter: { field: "IdAsignacion", operator: "eq", value: Number($scope.parametros.IdAsignacion) },
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
					schema: bcsConfiguracionOrdenesV2.getSchemaMovFacturar(),
			}
		});

		asignacionDatasource.fetch().then(function(){
			if (asignacionDatasource.data().length!=1)
			{
				console.log("Asignacion no existe!");
				return false;
			}
			else
			{
				$scope.$apply(function() {$scope.asignacion = asignacionDatasource.data()[0];});
				return true;
			}
		}).then(function(){
			
			var filterMovs = [{ field: "IdAsignacion", operator: "eq", value: asignacionDatasource.data()[0]===undefined? Number($scope.parametros.IdAsignacion):asignacionDatasource.data()[0].IdAsignacion }];
			movFacDatasource.query({filter : filterMovs}).then(function(){
				$scope.$apply(function() {$scope.movimientos = movFacDatasource.data();});
			});

			if ($scope.data.AjustaTipoCambio) {
				$scope.asignacion.NuevoTipoCambio = $scope.asignacion.TipoCambio
				$scope.asignacion.NuevoMontoFinal = $scope.asignacion.MontoAsignadoLocal
			}
		});
	}
			
    $scope.calcularMontoFinal = function() {
		$scope.asignacion.NuevoMontoFinal = Math.round(
			($scope.asignacion.NuevoTipoCambio * $scope.asignacion.CantidadAsignada) 
			* $scope.asignacion.PrecioAsignacion
		);
    }

	$scope.construirBodyAjusteCambio = function(asignacion) {

		const {
			IdAsignacion,
			IdTransaccion,
			IdOrden,
			MontoAsignado,
			IdEstadoAsignacion,
			IdMonedaOperacion,
			IdMonedaLiquidacionOrden,
			IdClasificacionTipoInstrumento,
			FechaLiquidacion,
			FolioTransaccion,
			IdCliente,
			IdPortafolio,
			TipoCambio,
			NuevoTipoCambio
		} = asignacion;
		
		return {
			IdAsignacion,
			IdTransaccion,
			IdOrden,
			MontoAsignado,
			IdEstadoAsignacion,
			IdMonedaOperacion,
			IdMonedaLiquidacionOrden,			
			IdClasificacionTipoInstrumento,
			FechaLiquidacion,
			FolioTransaccion,
			IdCliente,
			IdPortafolio,
			TipoCambio,
			NuevoTipoCambio
		};
	}

	$scope.ajustarTipoCambio = function() {
		
		if ($scope.asignacion.NuevoTipoCambio <= 0) {
			bcsComunes.msg("error", $translate.instant('UI_ERROR'), $translate.instant("UI_ERROR_TIPO_CAMBIO_MAYOR_A_CERO"));
			return
		}

		if ($scope.asignacion.NuevoTipoCambio === $scope.asignacion.TipoCambio) {
			bcsComunes.msg("error", $translate.instant('UI_ERROR'), $translate.instant("UI_ERROR_TIPO_CAMBIOS_IGUALES"));
			return
		}

		let modalConfirmacion = bcsAlerta.crear("PREGUNTA_SI_NO",
								$translate.instant('UI_CONFIRMAR_AJUSTE_TIPO_CAMBIO'),
								idVentana);
		modalConfirmacion.then(function(result){
			if(result === OPCIONES_BCSALERTA.SI) {

				let datosAjusteTipoCambio =  {
					p_IdEmpresa 	: $scope.SesionIdEmpresa,
					p_IdUsuario 	: $scope.SesionIdUsuario,
					p_IdModulo		:'ORD',
					p_Asignacion	: [$scope.construirBodyAjusteCambio($scope.asignacion)]
				}

				bcsServicioOrden.AjustarTipoCambioAsignacion(datosAjusteTipoCambio).$promise
				.then(function(response) {						
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(response)
					},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				})
				.catch(function(error) {

					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(error)
					},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);

				});

			} 
		})
	}

	$scope.cerrar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}

});
