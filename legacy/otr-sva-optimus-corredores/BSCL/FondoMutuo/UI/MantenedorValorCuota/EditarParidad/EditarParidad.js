angular.module('ModuloFFM').controller('EditarParidadCtrl',function($scope,$translate,bcsComunes,bcsServicioFondosMutuos,$timeout){

	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.params = bcsComunes.ventanaParams($scope.winID);

	$scope.valorParidad = 1;

	$scope.aceptar = function(){

		const { filasSeleccionadas } = $scope.params;
		const { valorParidad } = $scope;

		if(valorParidad <= 0){
			bcsComunes.ventanaMsgErr("VALOR PARIDAD DEBE SER MAYOR QUE CERO",$scope.winID);
			return ;
		}
		
		if(typeof valorParidad == 'string'){
			$scope.valorParidad = 1;
		}
		
		filasSeleccionadas.forEach( fila => {
			fila.ParidadValorCuota = Number($scope.valorParidad);
			fila.ValorCuotaCalculado = Number((fila.ValorCuota * $scope.valorParidad)).toFixed(2);
			fila.VariacionValorCuota = fila.ValorCuotaFechaAnterior <= 0 ? Number(($scope.valorParidad-1)*100).toFixed(2)+'%' : Number(Math.abs(((((fila.ValorCuota * $scope.valorParidad)-fila.ValorCuotaFechaAnterior)/fila.ValorCuotaFechaAnterior)*100))).toFixed(1)+'%';
		})

		console.log(filasSeleccionadas)

		bcsComunes.ventanaMsg({									
			mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
		},$scope.winID);

		bcsComunes.ventanaCerrar($scope.winID);
		

	};
	
	$scope.cancelar = function(){
	
		bcsComunes.ventanaCerrar($scope.winID);
	};
	

	
})