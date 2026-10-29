angular.module('ModuloPlataforma').controller('ModalRechazarAutorizacionCtrl',
		function($scope
				,bcsComunes 
				,bcsServicioPlataforma
				) {


	$scope.data = {};
	$scope.grid = {};
	$scope.filtros = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);

    $scope.Parametros = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.ComentarioRechazo = "";
	$scope.data.requiereComentario = false;
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('AUT.RequiereComentarioRechazo', $scope.SesionIdEmpresa, 'PLATAFORMA')
	.then(function (parametroRequiereComentarioRechazo) {
		//parametroRequiereComentarioRechazo = parametroRequiereComentarioRechazo.Valor;
		if(parametroRequiereComentarioRechazo.Valor ==='S'){
			$scope.data.requiereComentario = true;
		}
	});
	$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.btnCancelar = function()
	{
		$scope.Data = {};
		$scope.Data.Accion = '';
		bcsComunes.ventanaData($scope.IdVentana, $scope.Data);
		bcsComunes.ventanaCerrar($scope.IdVentana); 
		
	};
	
	//Cierra la ventana dado el ID
	$scope.btnGrabar = function()
	{
		$scope.Data = {};
		$scope.Data.Accion = 'RZD';
		$scope.Data.ComentarioRechazo = $scope.ComentarioRechazo
		bcsComunes.ventanaData($scope.IdVentana, $scope.Data);
		bcsComunes.ventanaCerrar($scope.IdVentana); 
	}

});


