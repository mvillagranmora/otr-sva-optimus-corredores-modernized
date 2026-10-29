angular.module('ModuloPlataforma').controller('ConfigurarEventos',function($rootScope, $scope,bcsMantenedorEventosGrid,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta){

	
 		$scope.conf = {};
 		var idVentana = bcsComunes.ventanaWinId($scope);
 		$scope.tiempodespacho = {};
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
		$scope.boton = {value1 : true};
		$scope.view = [];
		 
		
		$scope.childMethodEventos = function(){
			$rootScope.$emit("CallParentEventos",{});
		};
		
		var dataSourceConfiguracion = new bcs.DataSource({
			
			type : "odata",
			definition : {
  				serverSorting : true, 
				sort: { field: "IdConfiguracionEvento", dir: "desc" },
				pageSize : 50,
			    transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t="+1376,
						dataType: "json"
					}
				},
				schema : {
					model: {
						fields : {
							IdConfiguracionEvento 	: {type: "number"},
						 	Descripcion				: {type: "string"}
						
		 				}
					} 
				}
			}
		});
		
		dataSourceConfiguracion.fetch().then(function(){
			
			$scope.view = dataSourceConfiguracion.data();
		});
		 
		
		$scope.AgregarEventoConfiguracion = function()    
		{	
				$scope.boton.value1 = false;
				var validator = 0;
				if($scope.data.descripcion===undefined || $scope.data.descripcion.trim()===''){
		        	bcsComunes.msg("error","error",$translate.instant("El nombre de la notificacion no debe ser vacío"));
		        	validator++;
		        	 $scope.boton.value1 = true;
		        	return;
				}
				
				$.each($scope.view, function(key,codigos){
					
					if(codigos.Descripcion===$scope.data.descripcion){
			        	bcsComunes.msg("error","error",$translate.instant("El nombre de la notificacion ya existe"));
			        	validator++;
 					}
						
					
				});
				if($scope.RequiereRecordar.value1 && $scope.conf.kngFechaRecordatorio===undefined){
		        	bcsComunes.msg("error","error",$translate.instant("Debe ingresar los dias para el recordatorio"));
					validator++;
 				}
				if($scope.RequiereConfirmacion.value1&&$scope.conf.kngFechaVencimiento===undefined){
 			        	bcsComunes.msg("error","error",$translate.instant("Debe ingresar los dias para el vencimiento"));
						validator++;
						
 
				}
				if($scope.conf.tiempodespacho.IdTiempoDespacho==2 && $scope.data.hora===undefined){
					bcsComunes.msg("error","error",$translate.instant("Debe ingresar la hora de envío de la notificación"));
					validator++;
					
				}
				
				if(validator===0){  
			    	var datosPlantilla = {}; 
			  	
		        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		        datosPlantilla.p_IdModulo = 'NOT'; 
		        if($scope.Agrupa.value1==true) {
	        		datosPlantilla.p_Agrupa ='S';
			  	}else{
	        		datosPlantilla.p_Agrupa ='N';   
			  	} 
 		        datosPlantilla.p_FormaDespacho =$scope.conf.formadespacho.CodigoFormaDespacho;
		        
		        if($scope.conf.tiempodespacho.IdTiempoDespacho==2){
		        	
		        	if($scope.data.hora===undefined || $scope.data.hora === null){
		        		bcsComunes.msg("error","error",$translate.instant('Campo Hora no puede ir vacío'));
		        		$scope.boton.value1 = true;

		        		return;

	 	        	}else{
		        		datosPlantilla.p_FechaDespacho = $scope.data.hora; 	
		        	}	
		        }else{
		        	datosPlantilla.p_FechaDespacho = ""; 	
		        }
		        
		        datosPlantilla.p_TiempoDespacho =$scope.conf.tiempodespacho.CodigoTiempoDespacho;
		        datosPlantilla.p_FechaVencimiento =$scope.conf.kngFechaVencimiento;
		        datosPlantilla.p_FechaRecordatorio =$scope.conf.kngFechaRecordatorio;
		        if($scope.RequiereConfirmacion.value1==true) {
		        		datosPlantilla.p_RequiereConfirmacion ='S';
		        }else{
		        		datosPlantilla.p_RequiereConfirmacion ='N'; 
		        		datosPlantilla.p_FechaVencimiento = 0;
		        }
		        
		        if($scope.RequiereRecordar.value1==true) {
	        		datosPlantilla.p_RequiereRecordar ='S';
	        		datosPlantilla.p_PlantillaRecordatorio =$scope.conf.oPlantillaNotificacionRecordatorio.IdPlantilla;
		        }else{
		        		datosPlantilla.p_RequiereRecordar ='N';
		        		datosPlantilla.p_FechaRecordatorio = 0;
		        		datosPlantilla.p_PlantillaRecordatorio = 0;
		        }
		        
		        if($scope.Personalizable.value1==true) {
		        		datosPlantilla.p_Personalizable ='S';
		        }
		        else{
		        		datosPlantilla.p_Personalizable ='N';
		        }
		        datosPlantilla.p_Descripcion = $scope.data.descripcion;
		        datosPlantilla.p_Plantilla =$scope.conf.oPlantillaNotificacion.IdPlantilla;
		        datosPlantilla.p_PlantillaRecordatorio;
		        datosPlantilla.p_Correos = [];
		        $.each($scope.gridApiCorreos.dataSource.data(), function(key,codigos){
		        	datosPlantilla.p_Correos.push({Correo : codigos.Correo});
		        	console.log(codigos.Correo);
		        });
		        if($scope.ExcluirDestinatario.value1==true) {
	        		datosPlantilla.p_ExcluyeDestinatario ='S';
		        }
		        else{
		        	datosPlantilla.p_ExcluyeDestinatario ='N';
		        }
		        if($scope.EnviarUsuariosRol.value1==true) {
	        		datosPlantilla.p_IdRol =$scope.conf.oRol.IdRol;
		        }
		        else{
		        	datosPlantilla.p_IdRol =-1;
		        }
		        if($scope.EnviarRemitente.value1==true) {
	        		datosPlantilla.p_EnviarRemitente ='S';
		        }
		        else{
		        	datosPlantilla.p_EnviarRemitente ='N';
		        }
		        if($scope.AniadirCopias==true){
	        		datosPlantilla.p_CopiasAdicionales ='S';

		        }else{
	        		datosPlantilla.p_CopiasAdicionales ='N';

		        }
		        
 		        
		        if($scope.conf.kngFechaRecordatorio>=$scope.conf.kngFechaVencimiento){
		        	bcsComunes.msg("error","error",$translate.instant("Dias de recordatorio debe ser menor a los dias del vencimiento"));
		        }else{ 
		        
		        	bcsServicioPlataforma.AgregarEventoConfiguracion(datosPlantilla,function(result)
				    {
		        		
 				        	 if (result.P_OUT_ERRORESCONTADOR == "0"){
									bcsComunes.msg("info","info",$translate.instant(result.P_SALIDA));	
									 $scope.Limpiar();
									 $scope.childMethodEventos();
									 bcsComunes.ventanaCerrar(idVentana);
									 
						        }   
						        else{
						        	bcsComunes.setConflictos(util.parseServerResponse(result));
									bcsComunes.msg("error","error",$translate.instant(result.P_SALIDA));
						        }
				        },
						function(result){
				        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
						});
		        	 $scope.boton.value1 = true;
		        }
				}else{  
		        $scope.boton.value1 = true;
 				}
				$scope.boton.value1 = true;
		}
		
		$scope.RequiereConfirmacion.value1=false;
		$scope.Personalizable.value1 = false;
		$scope.Agrupa.value1 = false; 
		$scope.RequiereRecordar.value1 = false;
		$scope.AniadirCopias.value1 = false;
		$scope.EnviarUsuariosRol.value1 = false;
		$scope.boton.value1 = true;
		
		
        
		$scope.$watch("RequiereRecordar.value1", function(newValue,oldValue){
			if (newValue===oldValue) {
			      return;
			    }
	 		if(!$scope.RequiereRecordar.value1){
	 			 $scope.conf.kngFechaRecordatorio = undefined;
	 		}
			return;

		});
		
		
		
		$scope.$watch("RequiereConfirmacion.value1", function(newValue,oldValue){
			if (newValue===oldValue) {
			      return;
			    }
	 		if(!$scope.RequiereRecordar.value1){
	 			 $scope.conf.kngFechaVencimiento = undefined;
	 		}
			return;

		});
		
		$scope.$watch("conf.tiempodespacho.IdTiempoDespacho", function(newValue,oldValue){
			if (newValue===oldValue) {
			      return;
			    }
	 		if($scope.conf.tiempodespacho.IdTiempoDespacho!=2){
	 			$scope.data.hora = '';
	 			$scope.data.hora = undefined;
	 			$scope.data.hora = null;
	 		}
			return;

		});
		
		var dataSourceCorreos = new bcs.DataSource({
			
			type : "array",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
  			    schema : {
  			    	model : {
  			    		fields : {
  			    			Correo : { type : "string"}
  			    		}
  			    	}
  			    }
				 
			}   
			
		});
		
		
		$scope.confCorreos = {
				dataSource:  dataSourceCorreos,
				columns: [
							{field: "Correo" ,title: "UI_CORREO"},
 						],
				height: 120,
 				selectable : "row", 
 				autoBind : true
		};
		
		
		
		$scope.Limpiar = function(){
			
			$scope.data.descripcion = '';
			$scope.Agrupa.value1 = false;
			$scope.RequiereConfirmacion.value1 = false;
			$scope.Personalizable.value1 = false;
			$scope.conf.kngFechaVencimiento = undefined;
			$scope.RequiereRecordar.value1 = false;
			$scope.conf.kngFechaRecordatorio = undefined;
			$scope.ExcluirDestinatario.value1 = false;
			$scope.EnviarUsuariosRol.value1 = false;
			$scope.EnviarRemitente.value1 = false;
			$scope.AniadirCopias.value1 = false;
			$scope.conf.Correo = '';
			$scope.conf.oTiempoDespacho.filtros.TiempoDespacho = 'I';
			$scope.conf.oFormaDespacho.filtros.FormaDespacho = 'T';
			$scope.gridApiCorreos.dataSource.data([]);
			
		};
 		
		
		$scope.agregarCorreo= function(){
			if(validateEmail($scope.data.Correo)){
			var data = {Correo : $scope.data.Correo};
			var count = $scope.gridApiCorreos.dataSource.data().length;
			var correo = {};
			var contRep = 0;
			if(count>0){ 
			for(var i = 0; i < count; i++){
				correo = $scope.gridApiCorreos.dataSource.data()[i];
				if((correo.Correo).toUpperCase()==mail.toUpperCase()){
					contRep++;
				}
			 }
			}else{
				$scope.gridApiCorreos.dataSource.add(data);
 			}
			if(contRep===0 && count > 0 ){
				$scope.gridApiCorreos.dataSource.add(data);
 			}
			if(contRep>0){
				bcsComunes.msg("error",'Error',$translate.instant("UI_CORREO_YA_AGREGADO"));

			}
			  
			$scope.data.Correo = '';
			}else{
				bcsComunes.msg("error",'Error',$translate.instant("UI_CORREO_NO_VALIDO"));
			}
		};
		$scope.eliminarCorreo= function(){
			var data = {Correo : $scope.conf.seleccionCorreo.Correo};
			var count = $scope.gridApiCorreos.dataSource.data().length;
			var correo = {};
			for(var i = 0; i < count; i++){
				correo = $scope.gridApiCorreos.dataSource.data()[i];
				if(correo.Correo==data.Correo){
					$scope.gridApiCorreos.dataSource.remove(correo);
				}
			 }
		};
		function validateEmail(email) {
			  var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
			  return re.test(email);
		}
		 

	 
	});

