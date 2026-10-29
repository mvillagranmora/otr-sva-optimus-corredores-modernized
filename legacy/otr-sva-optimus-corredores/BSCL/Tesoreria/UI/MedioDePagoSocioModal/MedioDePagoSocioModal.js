angular.module('ModuloTesoreria').controller('MedioDePagoSocioModalCtrl',
		function($scope,$state,$translate,bcsComunes,bcsServiciosTesoreria)
{

	$scope.frm = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);

	//SETEO INICIAL
	$scope.frm.medioDePago = parametros[0].NombreMedioDePago;
	$scope.frm.socioNegocio = parametros[0].NombrePersona;
	$scope.frm.documentoIdentidad = parametros[0].DocumentoIdentidad;
	$scope.frm.cuentaOrigenDCV = parametros[0].CuentaDCVOrigen;
	$scope.frm.cuentaDestinoDCV = parametros[0].CuentaDCVDestino;
	$scope.frm.claveDCV = parametros[0].ClaveDCV;
	$scope.frm.idMedioDePago = parametros[0].IdMedioDePago;
	console.log("Medio de pago",parametros[0].IdMedioDePago)

	//COMBOBOX
	$scope.frm.oConfiguracionModalidadPago={
			tipo : "combobox-modalidad-pago",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
				IdMedioDePago : { "field"   : "IdMedioDePago",   "operator": "eq", "value"   : parametros[0].IdMedioDePago }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : parametros[0].IdModalidadDePago
	};
	$scope.frm.oConfiguracionCuentaBancoSocioNegocio={
		tipo : "combobox-persona-cuentas-bancarias", 
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdPersona : { "field"   : "IdPersona","operator": "eq", "value"   : Number(parametros[0].IdPersona) } 
		},
		idActual : false,
		seleccionado : false,
		idDefecto : parametros[0].IdCuentaBancariaSocio
	};	
	$scope.frm.oConfiguracionCaja={
			tipo : "combobox-caja",
			configuracion : {},
			filtros : {
				IdEstadoCaja : { "field"   : "IdEstadoCaja", "operator": "eq", "value"   : "ACT" },
				IdMoneda : {"field"   : "IdMoneda", "operator": "eq", "value"   : parametros[0].IdMonedaSocio }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : Number(parametros[0].IdCaja)
	};
	
	//WATCH
	$scope.$watch('frm.oConfiguracionModalidadPago.seleccionado',function(newval,oldval){
		if (typeof $scope.frm.oConfiguracionModalidadPago.seleccionado !== 'undefined' && $scope.frm.oConfiguracionModalidadPago.seleccionado !== false)
		{
			$scope.showCuentasBancarias = $scope.frm.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago == 'MODDEP' ? true : false;
			$scope.showCajas = $scope.frm.oConfiguracionModalidadPago.seleccionado.IdModalidadDePago == 'MODCAJ' ? true : false;

			//podría cambiar al ser llamado de la pantalla de giro
			$scope.frm.oConfiguracionModalidadPago.activado = false;
		}		
	});	
	$scope.$watch('frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){
		if (typeof $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== 'undefined' && $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== false)
		{
			$scope.frm.tipoCuentaBancaria = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria;
			$scope.frm.banco = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
			$scope.frm.numeroCuenta = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
			$scope.frm.moneda = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreMoneda;
		}
	});
	$scope.$watch('frm.oConfiguracionCaja.seleccionado',function(newval,oldval){
		if (typeof $scope.frm.oConfiguracionCaja.seleccionado !== 'undefined' && $scope.frm.oConfiguracionCaja.seleccionado !== false)
		{
			$scope.frm.CodigoCaja = $scope.frm.oConfiguracionCaja.seleccionado.Codigo;
			$scope.frm.MonedaCaja = $scope.frm.oConfiguracionCaja.seleccionado.NombreLargoCajaMoneda;

			//podría cambiar al ser llamado de la pantalla de giro
			$scope.frm.oConfiguracionCaja.activado = false;
		}
	});

	
	//OPCIONES
	$scope.seleccionar = function(seleccion){
		var resultSelecion = {};
		resultSelecion.IdNominaTransferenciaDetalle = parametros[0].IdNominaTransferenciaDetalle || 0;
		resultSelecion.IdComprobanteFormaDePago = parametros[0].IdComprobanteFormaDePago || 0;
		resultSelecion.IdCuentaBancaria 		= 0;
		resultSelecion.NombreCuentaBancaria		= '';
		resultSelecion.IdTarjetaBancariaSocio 	= 0;
		resultSelecion.IdTipoCuentaBancaria     = 0;
		resultSelecion.NombreTipoCuentaBancaria = '';		
		resultSelecion.NumeroCuentaBancaria     = '';
		resultSelecion.IdMoneda                 = '';
		resultSelecion.NumeroChequeSocio		= '';
		resultSelecion.FechaVctoChequeSocio		= '0001-01-01';
		resultSelecion.IdCaja 					= 0;
		resultSelecion.IdCajaMoneda 			= 0;
		resultSelecion.CuentaDCVOrigen 			= '';
		resultSelecion.CuentaDCVDestino			= '';
		resultSelecion.ClaveDCV		 			= '';
		
		
		switch (seleccion.oConfiguracionModalidadPago.idActual)
		{
			case "MODDEP":
				resultSelecion.IdCuentaBancaria 		= $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBancaria;				
				resultSelecion.IdBanco 		            = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco;
				resultSelecion.NombreCuentaBancaria		= $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreCuentaBancariaFull;
				resultSelecion.IdTipoCuentaBancaria     = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdTipoCuentaBancaria;
				resultSelecion.NombreTipoCuentaBancaria = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreTipoCuentaBancaria;		
				resultSelecion.NumeroCuentaBancaria     = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
				resultSelecion.IdMoneda                 = $scope.frm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdMoneda;
				resultSelecion.CuentaDCVOrigen 			= $scope.frm.cuentaOrigenDCV;
				resultSelecion.CuentaDCVDestino			= $scope.frm.cuentaDestinoDCV;
				resultSelecion.ClaveDCV		 			= $scope.frm.claveDCV;			
				break;
				
			case "MODCAJ":
				resultSelecion.IdCaja = seleccion.oConfiguracionCaja.seleccionado.IdCaja;
				resultSelecion.IdCajaMoneda = seleccion.oConfiguracionCaja.seleccionado.IdCajaMoneda;
				break;
		}

		bcsComunes.ventanaData(idVentana,resultSelecion);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function(){
		bcsComunes.ventanaData(idVentana);
		bcsComunes.ventanaCerrar(idVentana);
	};

	var firstWindow = $('#Win_MedioDePagoSocioModal');
			firstWindow.css("width", "550px");
			
});