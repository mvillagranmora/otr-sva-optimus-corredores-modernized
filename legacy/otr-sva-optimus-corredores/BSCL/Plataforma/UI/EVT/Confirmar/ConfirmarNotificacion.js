angular.module('ModuloPlataforma').controller('ComentenosCtrl',function($scope,bcsMantenedorEventosGrid,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta){

	console.log("ConsultarNotificaciones");
		$scope.conf = {};
 		 
		
		$scope.RequiereConfirmacion = {	value1 : true }; 
		$scope.Personalizable = { value1 : true };  
		$scope.Agrupa = { value1 : true }; 
		$scope.RequiereRecordar = {value1 : false};
		$scope.ExcluirDestinatario = {value1 : false};
		$scope.EnviarUsuariosRol = {value1 :false};
		$scope.EnviarRemitente = {value1 : false};
		$scope.AniadirCopias = {value1:false};
		$scope.data = {Correo : ''};
		$scope.p_Correos = [];
	 
		$scope.reEnviarNotificacion = function (notificacionesTemp)
		{
			var contadorEnviado = 0;
			var datos = {
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'EVT',
		  			p_in_IdNotificacion : parametros.notificacion.IdNotificacion,
		  			p_Mail : $scope.data.Correo
 	    	};
			
			bcsServicioPlataforma.confirmarNotificacion(datos,function(result)
				    {
	       	 console.log(JSON.stringify(result));
	       	
	       	 	var msg = ";"
	       		if (result.P_OUT_ERRORESCONTADOR == "0"){
	       				 
						msg = $translate.instant(result.P_SALIDA);								
			        }   
			    else{
						msg = $translate.instant(result.P_OUT_ERRORESDETALLE);
			        }
	       		bcsComunes.ventanaMsg({
	       			mensaje: msg,
	       			conflictos:util.parseServerResponse(result)
	       		},idVentana);
	       		
	       	 
	       	 
	       },
			function(result){
	    	   datos.message = 'UI_ACTUALIZACION_INCORRECTA';
			});
		};

		 

	 
	});

