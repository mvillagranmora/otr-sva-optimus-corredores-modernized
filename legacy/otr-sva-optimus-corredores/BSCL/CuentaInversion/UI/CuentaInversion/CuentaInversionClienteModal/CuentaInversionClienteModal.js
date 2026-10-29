angular.module('ModuloCuentaInversion').controller('CuentaInversionClienteModalCtrl',
		function($scope,$uibModal,$translate,bcsComunes)
{
		
	var bSaldo = this;

	//Recupera parametros desde la ventana recibida
    $scope.winID 		= bcsComunes.ventanaWinId($scope);
    $scope.parametros 	= bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	
	bSaldo.InfoCliente = $scope.parametros.InfoCliente;
	bSaldo.InfoSaldos = $scope.parametros.InfoSaldos;

	
	$scope.cartolaDeMovimiento = function(){
		var info = {};
		info.IdCliente 				= bSaldo.InfoCliente[0].IdCliente;
		info.NombreCliente			= bSaldo.InfoCliente[0].NombreCliente;
		info.IdPortafolio 			= bSaldo.InfoCliente[0].IdPortafolio;
		info.NombrePortafolio		= bSaldo.InfoCliente[0].NombrePortafolio;
		info.FechaConsulta			= bSaldo.InfoCliente[0].FechaEfectivaMovimiento;
		info.IdMoneda				= bSaldo.InfoSaldos ? bSaldo.InfoSaldos[0].IdMoneda : '';
		info.NombreMoneda			= bSaldo.InfoSaldos ? bSaldo.InfoSaldos[0].NombreMoneda : '';
		info.IdCuentaInversion  	= bSaldo.InfoSaldos ? bSaldo.InfoSaldos[0].IdCuentaInversion : 0;		
		info.autoBind				= bSaldo.InfoSaldos[0].IdMoneda ? true : false;

		bcsComunes.ventanaAbrir("CartolaDeMovimientos", info, false, false, $scope.winID);
	};
	$scope.flujoCaja = function(){
		var objInfoCLiente = bSaldo.InfoSaldos[0];
		if (objInfoCLiente.IdMoneda !== '')
			bcsComunes.ventanaAbrir("FlujoCaja", objInfoCLiente,false,false,$scope.winID);
		else
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_CUENTA_INVERSION"));
		
	};
	let firstHeight = $('#Win_CuentaInversionClienteModal');
		firstHeight.css('height', '384px');
});