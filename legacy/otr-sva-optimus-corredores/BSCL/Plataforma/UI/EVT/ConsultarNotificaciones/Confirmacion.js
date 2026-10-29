angular.module('ModuloPlataforma',["kendo.directives","ngSanitize"]).controller('ConfirmacionCtrl',function($rootScope,$scope,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$timeout){

 	$scope.conf = {};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana); 
 	$scope.childmethod = function() {
      $rootScope.$emit("CallParentNotifySend", {});
    };
	
		
		$scope.Confirmar = function(){
			var notificaciones = [];
			var datos = {	
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'EVT',
		  			p_IdNotificacion : notificaciones ,
		  			p_Comentario :  parametros.observacion,
		  			p_Accion : 1
	    				};  
			$.each(parametros,function(key,codigos){
 				if(codigos.IdNotificacion)
					notificaciones.push({"IdNotificacion" : codigos.IdNotificacion});    
			});
			if(parametros.observacion===undefined || parametros.observacion.trim()===''){
				bcsComunes.msg("error","error",$translate.instant('El campo observación no puede ser vacío'));

			}else{
			bcsServicioPlataforma.ConfirmarNotificacion(datos,function(result){
		       	
				if (result.P_SALIDA===1){
	    	
			    	bcsComunes.msg('info', 'Confirmar', result.P_MENSAJESALIDA);
					$scope.childmethod();
					bcsComunes.ventanaCerrar(idVentana);
	
				
	    	}else{
	    			console.log(result);
					bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
					bcsComunes.ventanaCerrar(idVentana);
	    	}
	
			},
			function(result){
				bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
				}); 
			};
			
		}
		
		
		$scope.Rechazar = function(){
			
			var notificaciones = [];
			var datos = {	
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'EVT',
		  			p_IdNotificacion : notificaciones ,
		  			p_Comentario :  parametros.observacion,
		  			p_Accion : 2
	    				};  
			$.each(parametros,function(key,codigos){
 				if(codigos.IdNotificacion)
					notificaciones.push({"IdNotificacion" : codigos.IdNotificacion});
			});
			if(parametros.observacion===undefined || parametros.observacion.trim()===''){
				bcsComunes.msg("error","error",$translate.instant('El campo observación no puede ser vacío'));

			}else{
			bcsServicioPlataforma.ConfirmarNotificacion(datos,function(result){
		       	
				if (result.P_SALIDA===1){
	    	
			    	bcsComunes.msg('info', 'Confirmar', result.P_MENSAJESALIDA);
					$scope.childmethod();
					bcsComunes.ventanaCerrar(idVentana);
	
				
	    	}else{
	    			console.log(result);
					bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
					bcsComunes.ventanaCerrar(idVentana);
	    	}
	
			},
			function(result){
				bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
				}); 
			};
			
		}
	
	
 
		
 
});
