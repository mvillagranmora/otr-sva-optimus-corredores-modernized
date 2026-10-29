angular.module('ModuloPlataforma',["kendo.directives","ngSanitize"]).controller('ConfirmarNotificacionCtrl',function($rootScope,$scope,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$timeout){

 	$scope.conf = {};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana); 
 
 	$scope.notificacion = parametros.Notificacion;
 	$scope.notif = {};
 	
 	$scope.notif.fechaEnviado = util.formatoSecondate($scope.notificacion.FechaDespachoOriginal);
	$scope.data = {};
	
	$scope.readNotification = function(){  
		$rootScope.$emit("CallParentNotifySend", {});
	};
		
	$scope.Confirmar = function(){
			
			      
			if($scope.data.observacion===undefined || $scope.data.observacion.trim()===''){
				bcsComunes.msg("error","error",$translate.instant('El campo observación no puede ser vacío'));
				
			}else{

	    		
 	    		
	    		var notificaciones = []; 
				var datos = {	
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'EVT',
			  			p_IdNotificacion : notificaciones ,
			  			p_Comentario :  $scope.data.observacion,
			  			p_Accion : 1
		    				};  
				$.each($scope.notificacion,function(key,codigos){
						if(key=='IdNotificacion')
 						notificaciones.push({"IdNotificacion" : codigos});
				});
				bcsServicioPlataforma.ConfirmarNotificacion(datos,function(result){
			       	
					if (result.P_SALIDA===1){
		    	
				    	bcsComunes.msg('info', 'Confirmar', result.P_MENSAJESALIDA);
						$scope.readNotification();
						bcsComunes.ventanaCerrar(idVentana);
		
					
					}else{
						bcsComunes.ventanaMsg({
							mensaje: "Error",
							conflictos:util.parseServerResponse(result)
						},idVentana);
						bcsComunes.ventanaCerrar(idVentana);
					}
		
				},
				function(result){
					bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
				}); 
				
	
	
			}
			
		}
		      
		
		$scope.Rechazar = function(){
			
			if($scope.data.observacion===undefined || $scope.data.observacion.trim()===''){
				bcsComunes.msg("error","error",$translate.instant('El campo observación no puede ser vacío'));
				
			}else{
				var notificaciones = [];
				var datos = {	
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'EVT',
			  			p_IdNotificacion : notificaciones ,
			  			p_Comentario :  $scope.data.observacion,
			  			p_Accion : 2
		    				};  
				$.each($scope.notificacion,function(key,codigos){
						if(key=='IdNotificacion')
 						notificaciones.push({"IdNotificacion" : codigos});
 				});
				bcsServicioPlataforma.ConfirmarNotificacion(datos,function(result){
			       	
					if (result.P_SALIDA===1){
		    	
				    	bcsComunes.msg('info', 'Confirmar', result.P_MENSAJESALIDA);
						$scope.readNotification();
						bcsComunes.ventanaCerrar(idVentana);
		
					
					}else{
						bcsComunes.ventanaMsg({
							mensaje: "Error",
							conflictos:util.parseServerResponse(result)
						},idVentana);
						bcsComunes.ventanaCerrar(idVentana);
					}
		
				},
				function(result){
					bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
							}); 
	
			}
			
		}
	
	
 
		
 
});
