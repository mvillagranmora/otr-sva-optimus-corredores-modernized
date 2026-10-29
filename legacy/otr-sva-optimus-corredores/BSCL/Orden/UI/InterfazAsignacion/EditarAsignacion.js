angular.module('ModuloOrdenes').controller('EditarAsignacionCtrl',function ($scope,bcsComunes,$timeout,$q,bcsServicioPlataforma) {

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.idVentana = idVentana;
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.transaccion = $.extend({},parametros.transaccion);
	$scope.asignacion = $.extend({},parametros.asignacion);
	$scope.datosForm = {};
	
	$scope.pagoCobroCuentaInversion = $scope.asignacion.PagoCobroCuentaInversion === "S";
	$scope.esVentaCorta = ($scope.transaccion.EsVentaCorta === 'S' && $scope.asignacion.IdOrden === 0);
	$scope.tipoCambio = $scope.asignacion.TipoCambio;
	$scope.codigoIFRS = $scope.asignacion.CodigoIFRS;
	$scope.idBoveda = $scope.asignacion.IdBoveda;
	$scope.idFormaCustodia = ($scope.esVentaCorta ? 'NOM' : $scope.asignacion.IdFormaCustodia);
	$scope.glosa = $scope.asignacion.Glosa;
	$scope.tasaVentaCorta = $scope.asignacion.TasaVentaCorta;
	$scope.plazoVentaCorta = $scope.asignacion.PlazoVentaCorta;
	$scope.idMonedaLiquidacionOrden = $scope.asignacion.IdMonedaLiquidacionOrden;
	$scope.idPreferenciaPagoDefecto = $scope.asignacion.IdPreferenciaPagoPortafolio;
	$scope.idCuentaCustodioDefecto = $scope.asignacion.IdCuentaCustodioPortafolio;
	$scope.EsCuentaPropia = false;
	
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
			.then(
				function (parametroUsaFechaLiquidacion) {
					const fechaBase = $scope.esVentaCorta && parametroUsaFechaLiquidacion.Valor === 'S'
							? $scope.transaccion.FechaLiquidacion
							: $scope.transaccion.FechaOperacionTransaccion;
					$scope.fechaInicial = new Date(fechaBase);
				},
				function () {
					$scope.fechaInicial = new Date($scope.transaccion.FechaOperacionTransaccion);
				},
			);
	
	$scope.datosOperacion = {};
	$scope.datosOperacion.idInstrumento = $scope.transaccion.IdInstrumento;
	$scope.datosOperacion.cantidad = $scope.asignacion.CantidadAsignadaSimulacion;
	$scope.datosOperacion.precio = $scope.asignacion.PrecioPromedio;//$scope.asignacion.PrecioSimulacion;
	$scope.datosOperacion.idMonedaLiquidacion = $scope.asignacion.IdMonedaLiquidacionOrden;
	$scope.datosOperacion.idPortafolio = $scope.asignacion.IdPortafolio;
	$scope.datosOperacion.idTipoProducto = $scope.transaccion.IdTipoProducto;
	$scope.datosOperacion.idTipoOperacion = $scope.transaccion.IdTipoOperacion;
	$scope.datosOperacion.idMonedaOrden = $scope.transaccion.IdMonedaInstrumento;
	$scope.datosOperacion.montoOrden = $scope.asignacion.MontoAsignacion;
	$scope.datosOperacion.idBolsa = $scope.asignacion.IdBolsa;
	$scope.datosOperacion.idTipoCanalIngreso = $scope.asignacion.IdTipoCanalIngreso;
	$scope.datosOperacion.idCorredorCorresponsal = $scope.asignacion.IdCorredorCorresponsal;
	
	var operacion = [{
		IdInstrumento  				: $scope.datosOperacion.idInstrumento,
		FechaOperacion 				: $scope.SesionFechaSistema,
		CantidadOrden   			: $scope.datosOperacion.cantidad,
		PrecioOrden   				: $scope.datosOperacion.precio,
		MontoTotal					: 0,
		MontoOrden					: $scope.datosOperacion.montoOrden,
		IdMonedaLiquidacionOrden	: $scope.datosOperacion.idMonedaLiquidacion,
		IdPortafolio 				: $scope.datosOperacion.idPortafolio,
		IdTipoProducto 				: $scope.datosOperacion.idTipoProducto,
		IdMonedaOrden				: $scope.datosOperacion.idMonedaOrden,
		IdTipoOperacion 			: $scope.datosOperacion.idTipoOperacion,
		IdCanalIngreso 				: $scope.datosOperacion.idTipoCanalIngreso,
		IdBolsa 					: $scope.datosOperacion.idBolsa,
		IdCorredorCorresponsal		: $scope.datosOperacion.idCorredorCorresponsal,
		TipoCambio 					: $scope.tipoCambio
	}];
	
	$scope.datosForm.deferredCobros = $q.defer();
	$scope.datosForm.cobrosModificados = {};
	$scope.datosForm.apiCobros = {};
	$scope.datosForm.deferredCobros.promise.then(function(result){
		$scope.datosForm.apiCobros = result;
		$scope.datosForm.apiCobros.calcularCobros(operacion,false);
	});
	
	$scope.filtrosPreferenciaPago = {
			IdMedioDePago 	: { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
			IdPortafolio 	: { "field"   : "IdPortafolio","operator": "eq", "value"   :  $scope.asignacion.IdPortafolio },
			IdMoneda 		: { "field"   : "IdMoneda","operator": "eq", "value"   :  $scope.idMonedaLiquidacionOrden },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : $scope.transaccion.IdTipoOperacion }
	}
	
	$scope.filtrosBoveda = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
			IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   : "" }
	}
	
	$scope.filtrosCuentaCustodio = {
			IdPortafolio 			: { "field"   : "IdPortafolio","operator": "eq", "value"   : $scope.asignacion.IdPortafolio },
			IdEstadoAutorizacion 	: { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
			IdEstado 				: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			
	}
	
	$scope.filtrosMonedaLiquidacion = {
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : $scope.asignacion.IdMonedaInstrumento },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : $scope.asignacion.IdTipoInstrumento },
	}
	
	$.each(parametros.asignacion.cobrosAsignacion, function(key,cob) {
		if (cob.ValorCobroAplicado !== undefined) {
			$scope.datosForm.cobrosModificados[cob.IdCobro] = cob.ValorCobroAplicado;	
		}
	});
	
	$scope.aceptar = function() {

		var cobros = [];

		cobros = $scope.datosForm.cobrosModificados;
		
		
		bcsComunes.ventanaData(idVentana, { 
			cobros : cobros, 
			glosa : $scope.glosa, 
			tasaVentaCorta				: $scope.esVentaCorta? $scope.tasaVentaCorta : 0, 
			plazoVentaCorta 			: $scope.esVentaCorta? $scope.plazoVentaCorta : 0, 
			idFormaCustodia 			: $scope.formaCustodiaSeleccionada.IdFormaCustodiaOrden, 
			idBoveda 					: $scope.bovedaSeleccionada.IdBoveda,
			tipoCambio 					: $scope.tipoCambio,
			idMonedaLiquidacionOrden 	: $scope.idMonedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
			pagoCobroCuentaInversion	: $scope.pagoCobroCuentaInversion?"S":"N",
			idPreferenciaPagoPortafolio	: !$scope.formaDePagoSeleccionada?null:$scope.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
			idCuentaCustodioPortafolio	: $scope.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio,
			codigoIFRS					: $scope.codigoIFRSSeleccionado.Codigo
			
		});
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		
		bcsComunes.ventanaData(idVentana, undefined);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.$watch('pagoCobroCuentaInversion',function(newval,oldval){
		if (newval)
			$scope.idpreferenciapago = false;
	})

	$scope.$watch('tipoCambio',function(newval,oldval){
		if (newval !== oldval){
			var operacion = [{
				IdInstrumento  				: $scope.datosOperacion.idInstrumento,
				FechaOperacion 				: $scope.SesionFechaSistema,
				CantidadOrden   			: $scope.datosOperacion.cantidad,
				PrecioOrden   				: $scope.datosOperacion.precio,
				MontoTotal					: 0,
				IdMonedaLiquidacionOrden	: $scope.datosOperacion.idMonedaLiquidacion,
				IdPortafolio 				: $scope.datosOperacion.idPortafolio,
				IdTipoProducto 				: $scope.datosOperacion.idTipoProducto,
				IdTipoOperacion 			: $scope.datosOperacion.idTipoOperacion,
				IdCanalIngreso 				: $scope.datosOperacion.idTipoCanalIngreso,
				IdBolsa 					: $scope.datosOperacion.idBolsa,
				IdCorredorCorresponsal		: $scope.datosOperacion.idCorredorCorresponsal,
				TipoCambio 					: newval
			}];
			$scope.datosForm.apiCobros.calcularCobros(operacion,false);
		}
	})
});
