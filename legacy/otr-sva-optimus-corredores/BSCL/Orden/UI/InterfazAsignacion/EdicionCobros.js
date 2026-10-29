angular.module('ModuloOrdenes').controller('EdicionCobrosCtrl',function ($scope,bcsComunes) {

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.idVentana = idVentana;
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.cobros = _.filter(parametros.cobros, function(cob) { if (cob.PermiteCambiarValor === 'S') return cob; });
	$scope.esVentaCorta = parametros.esVentaCorta ?? false;
	$scope.fechaInicial = parametros.fechaInicial;
	$scope.idTipoOperacion = parametros.idTipoOperacion;
	$scope.plazoVentaCorta = 0;
	$scope.tasaVentaCorta = 0;
	$scope.tipoCambio = 0;
	$scope.idCondicionDeLiquidacion = parametros.idCondicionDeLiquidacion;
	$scope.idMonedaInstrumento = parametros.idMonedaInstrumento;
	$scope.idTipoInstrumento = parametros.idTipoInstrumento;
	
	$scope.cambiaBoveda = false;
	$scope.cambiaFormaCustodia = false;
	$scope.cambiaPlazoVentaCorta = false;
	$scope.cambiaTasaVentaCorta = false;
	$scope.cambiaTipoCambio = false;
	$scope.cambiaMonedaLiquidacionOrden = false;
	
	$scope.filtrosBoveda = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
	}
		
	$scope.filtrosMonedaLiquidacion = {
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : $scope.idMonedaInstrumento },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : $scope.idTipoInstrumento },
	}
	
	$scope.aceptar = function() {
		var cobros = _.filter($scope.cobros,function(cob) {
			if (cob.Seleccionado === true && cob.ValorNuevo > 0) {
				return cob;
			}
		});
		var datos = {
				cobros : cobros,
				idBoveda : $scope.cambiaBoveda? $scope.bovedaSeleccionada.IdBoveda:undefined,
				idFormaCustodia : $scope.cambiaFormaCustodia? $scope.formaCustodiaSeleccionada.IdFormaCustodiaOrden:undefined,
				plazoVentaCorta : $scope.cambiaPlazoVentaCorta? $scope.plazoVentaCorta:undefined,
				tasaVentaCorta : $scope.cambiaTasaVentaCorta? $scope.tasaVentaCorta:undefined,
				tipoCambio :	$scope.cambiaTipoCambio? $scope.tipoCambio : undefined,
				idMonedaLiquidacionOrden : $scope.cambiaMonedaLiquidacionOrden?$scope.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion : undefined,
		}
		bcsComunes.ventanaData(idVentana, datos);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		
		bcsComunes.ventanaData(idVentana, []);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	
		
	$.each($scope.cobros,function(key,val) {
		val.Decimales = util.bcs_formato(0,'ORD','Monto',val.IdMoneda,'','').cantidadDecimales;
		val.ValorNuevo = val.Minimo;
	});

});
