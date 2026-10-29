(function() {
	angular.module('ModuloMesa').controller('ModalEditarEstado',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
				
		var camDig = this;
		
		camDig.datosAntes = {};
		camDig.datosDespues = {};
		camDig.datosAntes = angular.copy(camDig.datosDespues);
		
		//Configurar al principio
		
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		camDig.data = {};
		
		camDig.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		
		//if(typeof camDig.datosEntrada.items === 'undefined'){
			//return;
		//} 
		if(camDig.datosEntrada.items != undefined){
			$scope.data.instrumento = camDig.datosEntrada.items.Instrumento;
			$scope.data.monedaPrincipal = camDig.datosEntrada.items.IdMonedaPrincipal;
			$scope.data.monedaSecundaria = camDig.datosEntrada.items.IdMonedaSecundaria;
			$scope.data.NombreProducto = camDig.datosEntrada.items.NombreProducto;
		
			$scope.data.estadoDefecto = camDig.datosEntrada.items.IdEstado;
		}
	});
}());