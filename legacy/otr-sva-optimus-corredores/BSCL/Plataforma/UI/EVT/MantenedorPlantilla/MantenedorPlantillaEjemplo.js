angular.module('ModuloPlataforma',["kendo.directives","ngSanitize"]).controller('MantenedorPlantillaEjemplo',function($rootScope,$scope,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$timeout){

 	$scope.conf = {};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana); 
	
 	var plantilla = JSON.parse(parametros.Plantilla);
	
	$scope.childmethod = function(){  
		$rootScope.$emit("CallParentMethod", {});
	};
	console.log(JSON.parse(parametros.Plantilla));
	
	if(parametros.IdNotificacion){
		 var datos = {	
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'EVT',
		  			p_in_IdNotificacion : [],
		  			p_Accion : 6
	    				};
			 
	  	datos.p_in_IdNotificacion.push({IdNotificacion : parametros.IdNotificacion});
	  	
		bcsServicioPlataforma.EnviarNotificaciones(datos,function(result){
 	       	
	    if (result.P_OUT_ERRORESCONTADOR == "0"){
	    	
 				$scope.childmethod();
 				var OpenWindow = window.open("/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html", "TITULO", '');
 				$timeout(function(){OpenWindow.init(plantilla)},1000);
 		    	bcsComunes.msg('info', 'Procesar', 'OK');

				
	    	}else{
 				bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
	    	}

			},
			function(result){
				bcsComunes.msg("error","error",$translate.instant('Error al intentar actualizar notificación'));
 			}); 
		
		
			
    	 
	    	
	   
	}else{
		var OpenWindow = window.open("/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html", "TITULO", '');
		$timeout(function(){OpenWindow.init(plantilla)},1000);
	}
   
 
});
