angular.module('ModuloPlataforma').controller('RolesModalCtrl',
		function ($scope,bcsServicioOrden,bcsComunes,$translate,$resource) {

		var overlay = bcsComunes.overlay;
			
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var algoConfirmado = false;
			
		var limpiarDataOperacion = function(data)
		{
			$.each(data,function(key,value){
				value.resultado = 'UI_NO_ENVIADO';
				value.Mensajes = [];
			});
		};

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,algoConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.confirmarOperacion = function()
	  	{
			$.each($scope.data,function(key,value){
				value = $scope.callOperacion(value, $scope.operacion);
			}); 
			algoConfirmado = true;
	  	};
	  	
		$scope.callOperacion = function(data, idCall){
			
			var urlRoles = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+idCall;
			
			var r = $resource("", {},
		    {
				'operarData': { method: "POST", url: urlRoles}
			});
						
			var post = r.operarData(data.dataOperacion);
			
			post.$promise.then(function(result){
				console.log(result);
				if(result.P_OUT_ADVERTENCIASCONTADOR > 0){
					for(var i in result.P_OUT_ADVERTENCIASDETALLE){
						//Agrega las advertencias a un arreglo para desplegarlas con estilo en la UI
						data.Mensajes.push({"text": $translate.instant(result.P_OUT_ADVERTENCIASDETALLE[i].Codigo), cssClass:'alert alert-info'});	
					}		
				}
				if(result.P_OUT_ERRORESCONTADOR > 0){
					for(var i in result.P_OUT_ERRORESDETALLE){
						//Agrega los errores a un arreglo para desplegarlas con estilo en la UI
						data.Mensajes.push({"text": $translate.instant(result.P_OUT_ERRORESDETALLE[i].Codigo), cssClass:'alert alert-danger'});
					}
					data.resultado = "UI_ERROR";
				}else{
					//Si no hay errores
					data.resultado = "UI_EXITO";
				}
				return data;
			});
		};
	
		$scope.data = parametros.data;
	  	$scope.operacion = parametros.operacion;
	  	
		$scope.confirmarHabilitado = true;
		limpiarDataOperacion($scope.data);
});
