angular.module('ModuloPlataforma', ["kendo.directives","ngSanitize"]).controller('ComentenosCtrl',function($scope,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta,$http){

	$scope.conf = {};
	$scope.filtros = {};
	$scope.mail = {};
 	 


    
	var dataSourceCorreos = new bcs.DataSource({
		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
 			pageSize : 50,
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1547",
					dataType: "json"
				}
			},
			schema : 
			{
				model: {
					fields : {
						
								IdUsuario				 	: {type: "number"}
					 			,Direccion					: {type: "string"} 
					 			,IdRol						: {type: "number"}
 						 
					}
				} 
			},
			filter : {field : "IdUsuario",operator:"eq",value : $scope.SesionIdUsuario}
		}
	});
	
	dataSourceCorreos.fetch(function(){
		
		$scope.mail = $.extend({},dataSourceCorreos.data()[0]);

		
	});
	
	 $scope.enviarComentario = function(){
		  var datosComentenos = {}; 
		  datosComentenos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	      datosComentenos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	      datosComentenos.p_IdModulo = 'EVT'; 
	      datosComentenos.p_IdRolEnviar = $scope.conf.seleccionado.IdRol;
	      datosComentenos.p_Mensaje = $scope.data.texto;
 		  
	      
	      
	      bcsServicioPlataforma.EnviarComentenos(datosComentenos,function(result)
				    {
				        	 console.log(JSON.stringify(result));
				        	
				        	 	
				        	 bcsComunes.ventanaMsg({
				        		 mensaje: $translate.instant(result.P_OUT_SALIDA),
				        		 conflictos:util.parseServerResponse(result)
				        		 },idVentana);
				        	 
				        },
						function(result){
				        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
						});
		 
	 };
	
 
 
});
