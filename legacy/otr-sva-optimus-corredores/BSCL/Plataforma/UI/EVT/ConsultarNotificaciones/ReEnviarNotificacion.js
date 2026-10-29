angular.module('ModuloPlataforma').controller('ReEnviarCtrl',function($rootScope,$scope,bcsMantenedorEventosGrid,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta){

	console.log("ConsultarNotificaciones");
		$scope.conf = {};
 		 
		
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana); 
		$scope.RequiereConfirmacion = {	value1 : true }; 
		$scope.Personalizable = { value1 : true };  
		$scope.Agrupa = { value1 : true }; 
		$scope.RequiereRecordar = {value1 : false};
		$scope.ExcluirDestinatario = {value1 : false};  
		$scope.EnviarUsuariosRol = {value1 :false};
		$scope.EnviarRemitente = {value1 : false}; 
		$scope.AniadirCopias = {value1:false};
		$scope.data = {};
		
		$scope.p_Correos = [];
		var IdNotificacion = parametros.IdNotificacion;  
		
		console.log($scope.data.Correo);

		$scope.readNotification = function(){  
			$rootScope.$emit("CallParentNotifySend", {});
		};

		$scope.reEnviarNotificacion = function ()  
		{
			var mail = '';
			var contadorEnviado = 0;
			if($scope.AniadirCopias.value1===false){
				mail = $scope.data.Correo;
  			}
			var datos = {
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'EVT',
		  			p_IdNotificacion : IdNotificacion,
		  			p_Mail : mail
		  			
 	    	};
			
 			
			
			bcsServicioPlataforma.ReEnviarNotificaciones(datos,function(result){
	       
				var msg = "";
	       		if (result.P_OUT_ERRORESCONTADOR == "0"){
	       				 
						msg = $translate.instant('UI_REENVIANDO');	
						$scope.readNotification();						
			        }   
			    else{
						msg = $translate.instant('UI_REENVIADO_ERROR');
			        }
	       		
	       		var idMsg = bcsComunes.ventanaMsg({
	       			mensaje: msg,
	       			conflictos:util.parseServerResponse(result)
	       		},idVentana);
	       		bcsComunes.ventanaPromesa(idMsg).then(function(){
	       			bcsComunes.ventanaCerrar(idVentana);	
	       		});	       	 
	       },
			function(result){
	    	   datos.message = 'UI_ACTUALIZACION_INCORRECTA';
			});
		;}
		$scope.AniadirCopias.value1 = false;  

		 

	 
	});

