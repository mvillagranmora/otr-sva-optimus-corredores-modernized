angular.module('ModuloPlataforma').controller('ActualizarEventos',function($scope,bcsMantenedorEventosGrid,bcsServicioPlataforma,bcsComunes,$translate,bcsAlerta,$timeout){

	console.log("ActualizarEventos");
		$scope.conf = {};
	
		$scope.configuracionEventoOriginal = {};
		var idVentana = bcsComunes.ventanaWinId($scope); 
		var parametros = bcsComunes.ventanaParams(idVentana);
 		$scope.conf.RequiereConfirmacion = {	value1 : true };
		$scope.conf.Personalizable = { value1 : true };  
		$scope.conf.Agrupa = { value1 : true }; 
		$scope.conf.RequiereRecordar = { value1 : true }; 
		$scope.conf.ControlProceso = { value1 : true }; 
		$scope.ExcluirDestinatario = {value1 : false};
		$scope.EnviarUsuariosRol = {value1 :false};
		$scope.EnviarRemitente = {value1 : false};
		$scope.datosConfiguracion = {};
		$scope.configuracion = parametros.ConfiguracionEvento;
		$scope.configuracionevento = {};
		$scope.AniadirCopias = {};
		$scope.data = {};
		/*$scope.configuracionevento.oFormaDespacho={
				filtros : {
					FormaDespacho 			: {"field"	: "FormaDespacho"			,"operator"	:	"eq", "value"	: ""}
				},
			};
		$scope.configuracionevento.oTiempoDespacho={
				filtros : {
					TiempoDespacho 			: {"field"	: "TiempoDespacho"			,"operator"	:	"eq", "value"	: ""}
				},
			};
		$scope.configuracionevento.oPlantillaRecordatorio={
				filtros : {
					IdPlantillaRecordatorio 			: {"field"	: "IdPlantillaRecordatorio"			,"operator"	:	"eq", "value"	: 0}
				},
			};
		$scope.configuracionevento.oPlantilla={
				filtros : {
					IdPlantillaRecordatorio 			: {"field"	: "IdPlantilla"			,"operator"	:	"eq", "value"	: 0}
				},
			};
			*/
		$scope.conf.oRol={
			filtros : {
				idEmpresa 			: {"field"	: "IdEmpresa"			,"operator"	:	"eq", "value"	: $scope.SesionIdEmpresa}
			},
		};
		
		var dataSourceConfiguracion = new bcs.DataSource({
			
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdConfiguracionEvento", dir: "desc" },
				pageSize : 50,
			    transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1376",
						dataType: "json"
					}
				},
				schema :
				{
					model: {
						fields : {
							
							
							IdConfiguracionEvento 		: {type: "number"}
						 	,Descripcion				: {type: "string"}
		 					,IdUsuario					: {type: "number"} 
							,FechaCreacion				: {type: "date"}
							,IdEmpresa					: {type: "number"} 
							,IdEstado					: {type: "code"} 
		 					,PermitePersonalizar		: {type: "code"} 
							,FormaDespacho				: {type: "code"} 
							,TiempoDespacho				: {type: "code"} 
							,ConfirmacionDespacho		: {type: "code"} 
							,FechaVencimiento			: {type: "date"} 
							,FechaRecordatorio			: {type: "date"} 
							,IdPlantilla				: {type: "number"} 
							,IdPlantillaRecordatorio	: {type: "number"}
							,CodigoTipoEvento			: {type: "string"}
							,NotificaTodosUsuarios		: {type: "string"}
							,ExcluyeDestinatario		: {type: "string"}
							,CopiarRemitente 			: {type: "string"}
							,CopiasAdicionales			: {type: "string"}
							
						}
					} 
				},
				filter : {field : "IdConfiguracionEvento", operator :"eq", value : $scope.configuracion[0].IdConfiguracionEvento},
			}
		});
		
		
		
		var dataSourceCorreos = new bcs.DataSource({
			
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
  			    transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1387",
						dataType: "json"
					}
				},
				schema :
				{
					model: {
						fields : {
							
							
							IdConfiguracionEvento 		: {type: "number"}
						 	,Correo						: {type: "string"}
		 					,IdUsuario					: {type: "number"} 
							,FechaCreacion				: {type: "date"}
							 
							
						}
					} 
				},
				filter : {field : "IdConfiguracionEvento", operator :"eq", value : $scope.configuracion[0].IdConfiguracionEvento},
			},
			
		});
		$scope.confCorreos = {
				
				dataSource			: dataSourceCorreos,
				columns				: [
				       				   		{field: "Correo" ,title: "UI_CORREO"},					            
					                    ],	  		
				autoBind			: true,  //comentar
				height				: 120,
			    selectable 			: "multiple row"    
			};
		
	
		dataSourceConfiguracion.fetch().then(function(){
			if (dataSourceConfiguracion.data().length!=1) {
	 			return false;
			}
			else {
				$scope.$apply(function(){
					
					
					$scope.configuracionEventoOriginalIdActual = $.extend({},dataSourceConfiguracion.data()[0]);
					$scope.configuracionEventoOriginal = $.extend({},dataSourceConfiguracion.data()[0]);
				//	$scope.configuracionevento.oFormaDespacho.filtros.FormaDespacho = $scope.configuracionEventoOriginalIdActual.FormaDespacho;
				//	$scope.configuracionevento.oTiempoDespacho.filtros.TiempoDespacho = $scope.configuracionEventoOriginalIdActual.TiempoDespacho;
				//	$scope.configuracionevento.oPlantillaRecordatorio.filtros.IdPlantillaRecordatorio = $scope.configuracionEventoOriginalIdActual.IdPlantillaRecordatorio;
			 //		$scope.configuracionevento.oPlantilla.filtros.IdPlantilla = $scope.configuracionEventoOriginalIdActual.IdPlantilla;
					
 					if($scope.configuracionEventoOriginalIdActual.TiempoDespacho=='H'){
 						var d = new Date($scope.configuracionEventoOriginalIdActual.FechaDespacho);
 						var n = d.getHours();
 					    var l = d.getMinutes();
 					    var x = d.getSeconds();
 					    var time = "";
 					    if(x<10){
 					       time = n+":"+l+":0"+x;
 					    }else{
 					      time = n+":"+l+":"+x;
 					     }
 					   $scope.data = {hora : $scope.configuracionEventoOriginalIdActual.FechaDespacho};
 					}
					
					if($scope.configuracionEventoOriginalIdActual.ExcluyeDestinatario=='S'){
						$scope.ExcluirDestinatario.value1 = true;
					}else{
						$scope.ExcluirDestinatario.value1 = false;
					}
					if(!$scope.configuracionEventoOriginalIdActual.NotificaTodosUsuarios || $scope.configuracionEventoOriginalIdActual.NotificaTodosUsuarios=='N'){
						$scope.EnviarUsuariosRol.value1 = false;
						$scope.EnviarUsuariosRol.IdRol = -1;
					}else{
						$scope.EnviarUsuariosRol.value1 = true;
						$scope.EnviarUsuariosRol.IdRol = $scope.configuracionEventoOriginalIdActual.IdUsuarioRol;
					}

					if($scope.configuracionEventoOriginalIdActual.CopiarRemitente=='S'){
						$scope.EnviarRemitente.value1 = true;
					}else{
						$scope.EnviarRemitente.value1 = false;
					}
					if($scope.configuracionEventoOriginalIdActual.IdUsuarioRol==-1){
						$scope.EnviarUsuariosRol.value1 = false;
					}
					if($scope.configuracionEventoOriginalIdActual.Agrupa=='S'){
						
						$scope.conf.Agrupa.value1 = true;
					}else{
						$scope.conf.Agrupa.value1 = false;
					}
					if($scope.configuracionEventoOriginalIdActual.ConfirmacionDespacho=='S'){
						
						$scope.conf.RequiereConfirmacion.value1 = true;
						$scope.conf.kngFechaVencimiento = $scope.configuracionEventoOriginalIdActual.FechaVencimientoDias;

					}else{
						$scope.conf.RequiereConfirmacion.value1 = false;
						$scope.conf.kngFechaVencimiento = undefined;
					}
					if($scope.configuracionEventoOriginalIdActual.RecordarDespacho=='S'){
						
						$scope.conf.RequiereRecordar.value1 = true;
	 					$scope.conf.kngFechaRecordatorio =$scope.configuracionEventoOriginalIdActual.FechaRecordatorioDias;

					}else{
						$scope.conf.RequiereRecordar.value1 = false;
						$scope.conf.kngFechaRecordatorio = undefined;
					}
					if($scope.configuracionEventoOriginalIdActual.ControlProceso=='S'){
						
						$scope.conf.ControlProceso.value1 = true;

					}else{
						$scope.conf.ControlProceso.value1 = false;
					}
					if($scope.configuracionEventoOriginalIdActual.PermitePersonalizar=='Si'){
						
						$scope.conf.Personalizable.value1 = true;
					}else{
						$scope.conf.Personalizable.value1 = false;
					}
					if($scope.configuracionEventoOriginalIdActual.CopiasAdicionales=='S'){
						
						$scope.AniadirCopias.value1 = true;
					}else{
						$scope.AniadirCopias.value1 = false;
					}
					
					$scope.Descripcion = $scope.configuracionEventoOriginalIdActual.Descripcion;
					
					
 					
				});
				return true;
			}
		});

		$scope.ActualizarEventoConfiguracion = function(){
			var datosPlantilla = {}; 
		  	
	        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	        datosPlantilla.p_IdModulo = 'EVT'; 
	        datosPlantilla.p_IdConfiguracionEvento = $scope.configuracionEventoOriginalIdActual.IdConfiguracionEvento;
	        
	        if($scope.conf.Agrupa.value1==true) {
        		datosPlantilla.p_Agrupa ='S';
		  	}else{
        		datosPlantilla.p_Agrupa ='N';   
		  	}
		        datosPlantilla.p_FormaDespacho =$scope.conf.oFormaDespacho.CodigoFormaDespacho;
	        
	        if($scope.conf.oTiempoDespacho.IdTiempoDespacho==2){
	        	
	        	if($scope.data.hora===undefined){
	        		bcsComunes.msg("error","error",$translate.instant('Campo Hora no puede ir vacío'));
	        		return;

 	        	}else{
	        		datosPlantilla.p_FechaDespacho = $scope.data.hora; 	
	        	}
	        }else{
	        	datosPlantilla.p_FechaDespacho = ""; 	
	        }
	        
 		    datosPlantilla.p_TiempoDespacho =$scope.conf.oTiempoDespacho.CodigoTiempoDespacho;
	        
	        datosPlantilla.p_FechaVencimiento =$scope.conf.kngFechaVencimiento;
	        datosPlantilla.p_FechaRecordatorio =$scope.conf.kngFechaRecordatorio;
	        if($scope.conf.RequiereConfirmacion.value1==true) {
	        		datosPlantilla.p_RequiereConfirmacion ='S';
	        }else{
	        		datosPlantilla.p_RequiereConfirmacion ='N';   
	        		 datosPlantilla.p_FechaVencimiento =0;
	        }
	        
	        if($scope.conf.RequiereRecordar.value1==true) {
        		datosPlantilla.p_RequiereRecordar ='S';
        		datosPlantilla.p_PlantillaRecordatorio =$scope.conf.oPlantillaNotificacionRecordatorio.IdPlantilla;
	        }else{
	        		datosPlantilla.p_RequiereRecordar ='N';
	        		datosPlantilla.p_FechaRecordatorio = 0;
	        		datosPlantilla.p_PlantillaRecordatorio = 0;
	        }
	        
	        if($scope.conf.Personalizable.value1==true) {
	        		datosPlantilla.p_Personalizable ='S';
	        }
	        else{
	        		datosPlantilla.p_Personalizable ='N';
	        }
 	        datosPlantilla.p_Plantilla =$scope.conf.oPlantillaNotificacion.IdPlantilla;
	        datosPlantilla.p_PlantillaRecordatorio;
	        datosPlantilla.p_Correos = [];
	        $.each($scope.gridApiCorreos.dataSource.data(), function(key,codigos){
	        	datosPlantilla.p_Correos.push({Correo : codigos.Correo});
 	        });
	        
	        if($scope.ExcluirDestinatario.value1==true) {
        		datosPlantilla.p_ExcluyeDestinatario ='S';
	        }
	        else{
	        	datosPlantilla.p_ExcluyeDestinatario ='N';
	        }
	        if($scope.EnviarUsuariosRol.value1==true) {
        		datosPlantilla.p_IdRol =$scope.conf.Rol.IdRol;
	        }
	        else{
	        	datosPlantilla.p_IdRol =-1;
	        }
	        if($scope.EnviarRemitente.value1==true) {
        		datosPlantilla.p_EnviarRemitente ='S';
	        }
	        else{
	        	datosPlantilla.p_EnviarRemitente ='N';
	        }if($scope.AniadirCopias.value1==true){
        		datosPlantilla.p_CopiasAdicionales ='S';

	        }else{
        		datosPlantilla.p_CopiasAdicionales ='N';  
 
	        }

	        if($scope.conf.ControlProceso.value1==true) {
        		datosPlantilla.p_ControlProceso ='S';
		  	}else{
        		datosPlantilla.p_ControlProceso ='N';   
		  	}
	        
	        
	        if(parseInt($scope.conf.kngFechaRecordatorio)>=parseInt($scope.conf.kngFechaVencimiento)){
	        	bcsComunes.msg("error","error",$translate.instant("Los dias del recordatorio deben ser menos or igual a los dias de vencimiento"));
	        }else{ 
		        
		        bcsServicioPlataforma.ActualizarEventoConfiguracion(datosPlantilla,function(result)
			    {
			        	 console.log(JSON.stringify(result));
			        	
			        	
			        	 if (result.P_OUT_ERRORESCONTADOR == "0"){
								bcsComunes.msg("info","info",$translate.instant(result.P_SALIDA));	
								bcsComunes.ventanaCerrar(idVentana);
								
					        }   
					        else{
								bcsComunes.msg("error","error",$translate.instant(result.P_SALIDA));

					        }
			        	 
			        },
					function(result){ 
			        	console.log(result);
						bcsComunes.msg("error","error",$translate.instant('COMPLETE TODOS LOS CAMPOS'));
					});
	        }
		}
		
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
 		
 		
		$scope.agregarCorreo= function(){
			
			if(validateEmail($scope.data.Correo)){
				var data = {Correo : $scope.data.Correo};
				var mail = $scope.data.Correo;
				
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
					$scope.data.Correo = '';
				}
				if(contRep>0){
					bcsComunes.msg("error",'Error',$translate.instant("UI_CORREO_YA_AGREGADO"));
					$scope.data.Correo = '';
				}
			}else{
				bcsComunes.msg("error","error",$translate.instant("UI_CORREO_NO_VALIDO"));
				$scope.data.Correo = '';
			}
		};
		
		$scope.Limpiar = function(){
			
			$scope.conf.tiempodespacho = undefined;
			$scope.conf.formadespacho = undefined;
			$scope.conf.Agrupa.value1 = false;
			$scope.conf.RequiereConfirmacion.value1 = false;
			$scope.conf.Personalizable.value1 = false;
			$scope.conf.kngFechaVencimiento = undefined;
			$scope.conf.RequiereRecordar.value1 = false;
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
		
		
		$scope.eliminarCorreo= function(){
			var data = {Correo : $scope.conf.seleccionCorreo[0].Correo};
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

