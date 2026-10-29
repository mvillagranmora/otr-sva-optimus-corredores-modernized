angular.module('ModuloPlataforma').controller('RolesUsuarioModalCtrl',
		function ($scope,bcsServicioOrden,bcsComunes,$translate,$resource) {

		var overlay = bcsComunes.overlay;
			
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var algoConfirmado = false;
			
		var limpiarRolesUsuario = function(rolesUsuario)
		{
			$.each(rolesUsuario,function(key,value){
				value.resultado = 'UI_NO_ENVIADO';
				value.Mensajes = [];
			});
		};

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,algoConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.confirmarOperacionRoles = function()
	  	{
			$.each($scope.rolesData,function(key,value){
				var idCall = ($scope.operacion==="ADDROL")?"120":"121";
				value = $scope.callOperacionRolesUsuario(value, idCall);
			}); 
			algoConfirmado = true;
	  	};
	  	
		$scope.callOperacionRolesUsuario = function(rolData, idCall){
			
			var urlRoles = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+idCall;
			
			var r = $resource("", {},
		    {
				'mantenerRoles': { method: "POST", url: urlRoles}
			});
			
			var datosOperacionRol= {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA',
				//Datos
				p_IdUsuarioRol: rolData.IdUsuario,
				p_IdRol: rolData.IdRol,
				p_IdEmpresaRol: rolData.IdEmpresa,
				p_CodigoUsuario: rolData.codigoUsuario,
				p_FechaSistema: $scope.SesionFechaSistema
			};
			
			var post = r.mantenerRoles(datosOperacionRol);
			
			post.$promise.then(function(result){
				
				if(result.P_OUT_ADVERTENCIASCONTADOR > 0){
					for(var i in result.P_OUT_ADVERTENCIASDETALLE){
						//Agrega las advertencias a un arreglo para desplegarlas con estilo en la UI
						rolData.Mensajes.push({"text": $translate.instant(result.P_OUT_ADVERTENCIASDETALLE[i].Codigo), cssClass:'alert alert-info'});	
					}		
				}
				if(result.P_OUT_ERRORESCONTADOR > 0){
					for(var i in result.P_OUT_ERRORESDETALLE){
						//Agrega los errores a un arreglo para desplegarlas con estilo en la UI
						rolData.Mensajes.push({"text": $translate.instant(result.P_OUT_ERRORESDETALLE[i].Codigo), cssClass:'alert alert-danger'});
					}
					rolData.resultado = "UI_ERROR";
				}else{
					//Si no hay errores
					rolData.resultado = "UI_EXITO";
				}
				return rolData;
			});
		};
		
		$scope.rolesData = parametros.rolesData;
	  	$scope.operacion = parametros.operacion;
	  	
		$scope.confirmarHabilitado = true;
		limpiarRolesUsuario($scope.rolesData);
});
