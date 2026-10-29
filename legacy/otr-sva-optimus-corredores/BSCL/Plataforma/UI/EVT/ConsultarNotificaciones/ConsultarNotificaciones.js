angular.module('ModuloPlataforma').controller('ConsultarNotificaciones',function($rootScope,$scope,$http,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate,bcsAlerta,$timeout){
	
	$scope.conf = {}; 
	$scope.datos = {};
	$scope.view = [];
	var objTagDefault = {};  
	var objFather = {};
	$scope.camposAsociados = {};
 	var idVentana = bcsComunes.ventanaWinId($scope);
 	var filtrarUsuarioRemitente = "S";
 	$scope.BuscarNotificaciones = false;
 	
	$scope.conf.kngFechaDespachoDesde = util.dateInicioDia(new Date());
	$scope.conf.kngFechaDespachoHasta = util.dateTerminoDia(new Date());
	$scope.conf.kngFechaDespachoDesdeRecibidas = util.dateInicioDia(new Date());
	$scope.conf.kngFechaDespachoHastaRecibidas = util.dateTerminoDia(new Date());
	
	$rootScope.$on("CallParentNotifySend", function(){
		console.log("CallParentNotifySend");
		$scope.notificacionesenviadas.dataSource.read();
		$scope.notificacionesrecibidas.dataSource.read();
	});
	
	/************************
	 ***DECLARACION GRIDS****
	 *************************/
	
	var dataSourceConfiguracionConsultarNotificaciones = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'PLA.FiltrarNotificacionesEnviadasUsuario' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracionConsultarNotificaciones.fetch(function(){
		if(dataSourceConfiguracionConsultarNotificaciones.data().length > 0){
			filtrarUsuarioRemitente = dataSourceConfiguracionConsultarNotificaciones.data()[0].Valor;	
		}
 	});
	
	var dataSourceNotificacionesEnviadas = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 	
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdNotificacion", dir: "desc" },
			pageSize : 20,   
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1377",
					dataType: "json"
				}
			}, 
			schema : 
				{
					model: {
						fields : {
							 IdEmpresa						: {type: "number"}
							,IdNotificacion 				: {type: "number"}
							,IdConfiguracionEvento			: {type: "number"}
							,FechaCreacion 					: {type: "sysdatetime"}
							,FormaDespacho					: {type: "code"}
							,TiempoDespacho					: {type: "code"}
							,Asunto							: {type: "string"}
							,FechaDespachoOrginal			: {type: "sysdatetime"}
							,FechaDespachoRecordatorio		: {type: "sysdatetime"}
							,Enviado						: {type: "code"}
							,FechaEnviado					: {type: "sysdatetime"}
							,IdUsuarioEnviado				: {type: "number"}
							,ReEnviar						: {type: "code"}
							,ReEnviado						: {type: "code"}
							,FechaReEnviado					: {type: "sysdatetime"}
							,IdUsuarioReEnviado				: {type: "number"}
							,ConfirmacionDespacho			: {type: "code"}
							,TokenConfirmacion				: {type: "string"}
							,Confirmado						: {type: "code"}
							,FechaConfirmado				: {type: "sysdatetime"}
							,AccionConfirmada				: {type: "code"}
							,IdUsuarioConfimado				: {type: "number"}
							,FechaVencimiento				: {type: "date"}
							,FechaRecordatorio				: {type: "date"}
							,Observacion					: {type: "code"}
							,Descartado						: {type: "code"}
							,IdUsuarioDescarte				: {type: "number"}
							,FechaDescarte					: {type: "sysdatetime"}
							,Enviar							: {type: "code"}
							,DireccionEvento				: {type: "string"}
							,Leida							: {type: "string"}
							,IdUsuarioEnviado				: {type: "number"}
							,IdRemitente					: {type: "string"}
							,NombreRemitente				: {type: "string"}
							,IdDestinatario					: {type: "string"}
							,NombreDestinatario				: {type: "string"}
							,Direccion						: {type: "string"}
							,UsuarioConfirmado				: {type: "string"}
							,Nemo							: {type: "string"}	
							,DescripcionConfiguracion		: {type: "string"}
							,UsuarioEnviado				    : {type: "string"}
							,UsuarioReEnviado				: {type: "string"}
							,UsuarioConfirma				: {type: "string"}
							,UsuarioDescartado				: {type: "string"}
							,Secuencia						: {type: "string"}
							,NombreAdjunto					: {type: "string"}
							,LogEnvio						: {type: "string"}
						}
					}
			},
			filter : [],
		}
	});

	var dataSourceNotificacionesRecibidas = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdNotificacion", dir: "desc" },
			pageSize : 20,   
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t="+1381,
					dataType: "json"
				}
			},
			filter : {field : "IdUsuarioRecibe", operator: "eq", value : $scope.SesionIdUsuario},
			schema : 
			{
				model: {
					fields : {
						 IdEmpresa						: {type: "number"}
						,IdNotificacion 				: {type: "number"}
						,IdConfiguracionEvento			: {type: "number"}
						,FechaCreacion 					: {type: "date"}
						,FormaDespacho					: {type: "code"}
						,TiempoDespacho					: {type: "code"}
						,Asunto							: {type: "string"}
						,FechaDespachoOrginal			: {type: "sysdatetime"}
						,FechaDespachoRecordatorio		: {type: "sysdatetime"}
						,Enviado						: {type: "code"}
						,FechaEnviado					: {type: "sysdatetime"}
						,IdUsuarioEnviado				: {type: "number"}
						,ReEnviar						: {type: "code"}
						,ReEnviado						: {type: "code"}
						,FechaReEnviado					: {type: "sysdatetime"}
						,IdUsuarioReEnviado				: {type: "number"}
						,ConfirmacionDespacho			: {type: "code"}
						,TokenConfirmacion				: {type: "string"}
						,Confirmado						: {type: "code"}
						,FechaConfirmado				: {type: "date"}
						,AccionConfirmada				: {type: "code"}
						,IdUsuarioConfimado				: {type: "number"}
						,FechaVencimiento				: {type: "date"}
						,FechaRecordatorio				: {type: "date"}
						,Observacion					: {type: "code"}
						,Descartado						: {type: "code"}
						,IdUsuarioDescarte				: {type: "number"}
						,FechaDescarte					: {type: "sysdatetime"} 
						,Enviar							: {type: "code"}
						,DireccionEvento				: {type: "string"}
						,Leida							: {type: "string"}
						,IdUsuarioEnviado				: {type: "number"}
						,IdRemitente					: {type: "string"}
						,NombreRemitente				: {type: "string"}
						,IdDestinatario					: {type: "string"}
						,NombreDestinatario				: {type: "string"}
						,Direccion						: {type: "string"}
						,UsuarioConfirmado				: {type: "string"}

						,DescripcionConfiguracion		: {type: "string"}
						,UsuarioEnviado				: {type: "string"}
						,UsuarioReEnviado				: {type: "string"}
					}
				}
			}			
		}
	});
	$scope.notificacionesenviadas = {
		dataSource			: dataSourceNotificacionesEnviadas,
		columns				: bcsMantenedorEventosGrid.columnasConsultarNotificaciones(),		
		filterable			: true,
		autoBind			: false,  //comentar     
		height				: 400,
		pageable			: true,
	    selectable 			: "multiple row",
	    gridCreated			: function(grid) {
									mainGridOptions101 = grid;
									$scope.gridOptions101 = grid;
									return grid;
								},
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		}
	}; 
	$scope.notificacionesrecibidas = {
		dataSource			: dataSourceNotificacionesRecibidas,
		columns				: bcsMantenedorEventosGrid.columnasConsultarNotificacionesRecibidas(),			
		filterable			: true,    
		autoBind			: false,  //comentar
		selectable 			: "multiple",
		height				: 400,
		pageable			: true,
	    gridCreated			: function(grid) {
								mainGridOptions101 = grid;
								$scope.gridOptions101 = grid;
								return grid;
							},
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		}
	};
	/**
	 * FUNCIONES BUSQUEDA GRIDS
	 */
	$scope.BuscarNotificacionesEnviadas = function(){     
		
		var filters = new Array();
		
		if ($scope.conf.oTiempoDespacho.CodigoTiempoDespacho)
			filters.push({ field: "TiempoDespachoSrv", operator: "eq", value: $scope.conf.oTiempoDespacho.CodigoTiempoDespacho});
		
		if ($scope.conf.oTiempoDespacho.CodigoTiempoDespacho==='M'){
			filters.push({ field: "TiempoDespachoSrv", operator: "eq", value: 'M'});	
		}else{  
			if ($scope.conf.kngFechaDespachoDesde){
				var resp = util.validarRangoFecha($scope.conf.kngFechaDespachoDesde, $scope.conf.kngFechaDespachoHasta);
				if(resp.estado){
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
			}
			filters.push({ field: "FechaDespachoOrginal", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.conf.kngFechaDespachoDesde))});
			if ($scope.conf.kngFechaDespachoHasta)
				filters.push({ field: "FechaDespachoOrginal", operator: "lte", value: util.dateOdata(util.dateTerminoDia($scope.conf.kngFechaDespachoHasta))});
		}

		if ($scope.conf.oNotificacionEnviada.Enviado && $scope.conf.oNotificacionEnviada.Enviado!= 'T')
			filters.push({ field: "EnviadoSrv", operator: "eq", value: $scope.conf.oNotificacionEnviada.Enviado});

		if ($scope.conf.oEstadoConfirmacion.CodigoConfirmacion && $scope.conf.oEstadoConfirmacion.CodigoConfirmacion!= 'T')
			filters.push({ field: "EstadoConfirmacion", operator: "eq", value: $scope.conf.oEstadoConfirmacion.CodigoConfirmacion});
		
		$scope.BuscarNotificaciones = true;
 		$scope.notificacionesenviadas.dataSource.filters(filters);		
	};
	
	$scope.$watch(function() {return $scope.notificacionesenviadas.dataSource.ds._data}, function(newval, oldval) {
	    if(newval !== undefined && oldval !== undefined && $scope.BuscarNotificaciones){
	    	if (($scope.notificacionesenviadas.dataSource.data().length) === 0)  {
			   bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_SIN_INFORMACION"));
			   return;
		    };
	    }
	});
	
	$scope.BuscarNotificacionesRecibidas = function(){    
		
		var filters = new Array();
		
		var resp = util.validarRangoFecha($scope.conf.kngFechaDespachoDesde, $scope.conf.kngFechaDespachoHasta);
		if(resp.estado){
			bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
			return;
		}

		if ($scope.conf.oTiempoDespacho.CodigoTiempoDespacho)
			filters.push({ field: "TiempoDespachoSrv", operator: "eq", value: $scope.conf.oTiempoDespachoRecibida.CodigoTiempoDespacho});

 		if ($scope.conf.kngFechaDespachoDesde)
			filters.push({ field: "FechaDespachoOrginal", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.conf.kngFechaDespachoDesdeRecibidas))});
		
		if ($scope.conf.kngFechaDespachoHasta)
			filters.push({ field: "FechaDespachoOrginal", operator: "lte", value: util.dateOdata(util.dateTerminoDia($scope.conf.kngFechaDespachoHastaRecibidas))});
		
		if ($scope.conf.oNotificacionEnviada.Enviado && $scope.conf.oNotificacionEnviada.Enviado!= 'T')
			filters.push({ field: "EnviadoSrv", operator: "eq", value: $scope.conf.oNotificacionRecibida.Enviado});

		if ($scope.conf.oEstadoConfirmacion.EstadoConfirmacion && $scope.conf.oEstadoConfirmacion.CodigoConfirmacion!= 'T')
			filters.push({ field: "EstadoConfirmacion", operator: "eq", value: $scope.conf.oEstadoConfirmacionRecibida.CodigoConfirmacion});
			
		$scope.notificacionesrecibidas.dataSource.filters(filters);		
	};
	
	function agregarPredeterminados(ds){
   		obj = {};
 		var i = 0;
 		var z = 0;
		var total = ds.length;
 		var codigoevento = '';
 		var name = '';
 		
		$.each(ds, function(key,codigos){
			obj[codigos.Llave] = codigos.ValorDefault;
			i++;
			if(i===total){
				objTagDefault[codigos.CodigoTipoEvento] = obj;
				obj = {};
			}
        });
		
		objFather["data"] = objTagDefault;
  		$scope.camposAsociados = JSON.stringify(objFather);
		camposAsociados = $scope.camposAsociados;  
        return Promise.resolve(objFather);
	};
	
	//obtener los tags generales (header, footer, confirmacion, codigo desbloqueo archivo)
	var tagsDefaultDataSource_ = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: { 
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1393",
				dataType: "json"
		    }
		},
		filter : {	field: "IdPlantilla", operator: "eq", value:  0}
	});
	tagsDefaultDataSource_.fetch(function(){
		$scope.view = tagsDefaultDataSource_.view();
	});
	
	$scope.childmethod = function(){  
		$rootScope.$emit("CallParentMethod", {});
	};	
	 
	$scope.enviarNotificacion = function (notificacionesTemp)
	{
		var contadorNoManual = 0;
		var contadorTiempo = 0;
		var contadorEnviado = 0;
		var descartado = 0;
		var enviando = 0;


		var datos = {
    			p_IdEmpresa : $scope.SesionIdEmpresa,
	  			p_IdUsuario : $scope.SesionIdUsuario,
	  			p_IdModulo : 'EVT',
	  			p_in_IdNotificacion : [],
	  			p_Accion : 1
    	};
		$.each(notificacionesTemp,function(key,notif){
  			datos.p_in_IdNotificacion.push({IdNotificacion : notif.IdNotificacion});
  			if(notif.TiempoDespachoSrv != 'M')
  			{
  				contadorTiempo++;
  				contadorNoManual++;
  			}
  			if( notif.EnviadoSrv == 'S'){
  				contadorNoManual++;
  				contadorEnviado++;
  			}
  			if(notif.Descartado=='Si'){
					contadorNoManual++;
				descartado++;
			}
			if(notif.EnviarSrv=='S'){
				contadorNoManual++;
				enviando++;
			}
  			
  		});
		bcsServicioPlataforma.EnviarNotificaciones(datos,function(result){
       	
       	 if(contadorNoManual > 0){ 
       	 		
       	 	  if(contadorTiempo>0){
       	 	  	  bcsComunes.msg("error","error",$translate.instant('UI_ERROR_ENVIO_NOMANUAL'));
       	 	  }
       	 	  if(contadorEnviado>0){
       	 	  	  bcsComunes.msg("error","error",$translate.instant('UI_ENVIO_ENVIADA'));
       	 	  }
       	 	  if(descartado>0){
       	 	  	 bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESCARTADA'));

       	 	  }
       	 	  if(enviando>0){
       	 	  	 bcsComunes.msg("error","error",$translate.instant('UI_ENVIANDO'));   
       	 	  }

     	 }else{
       		if (result.P_OUT_ERRORESCONTADOR == "0" && contadorNoManual == 0){
       			   
					bcsComunes.msg("info","info",$translate.instant('UI_ENVIANDO'));
					$scope.notificacionesenviadas.dataSource.read();								
		        }   
		    else{
		        	bcsComunes.setConflictos(util.parseServerResponse(result));
					bcsComunes.msg("error","error",$translate.instant(result.P_OUT_ERRORESDETALLE));
		        }
     	 }
       },
		function(result){
    	   datos.message = 'UI_ACTUALIZACION_INCORRECTA';
		});
	};
	
	$scope.reEnviarNotificacion = function (notificacionesTemp)
	{	
 		var datos = {
				p_in_IdNotificacion : [],
			};
		var contadorEnviado = 0;
		var contadorReenviado = 0;
		var descartada = 0;
		var env = 0;  
		$.each(notificacionesTemp,function(key,notif){
			
	  			datos.p_in_IdNotificacion.push({IdNotificacion : notif.IdNotificacion});
	  			if(notif.EnviadoSrv != 'S' || notif.Descartado != 'No'){
 	  				contadorEnviado++;
	  			}
	  			if(notif.ReEnviarSrv == 'S'){
	  				contadorReenviado++;
	  			}
	  			if(notif.Descartado != 'No'){
					descartada++;	
	  			}
	  			if(notif.EnviadoSrv != 'S'){
	  				env++;
	  			}
  			});
  		 if(contadorEnviado > 0 || contadorReenviado > 0){
			  if(env > 0){
 				  bcsComunes.msg("error","error",$translate.instant('UI_NO_ENVIO_ENVIADA'));  			  
 				  }
			   if(descartada > 0){
 				  bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESCARTADA'));
			  }
			  if(contadorReenviado > 0 ){
				  bcsComunes.msg("error","error",$translate.instant('UI_PROCESO_REENVIO'));
			  }
		 }else{
			 
     		var id1 = bcsComunes.ventanaAbrir("ReEnviarNotificacion" ,{IdNotificacion : notificacionesTemp[0].IdNotificacion},false,false,idVentana);   
    		var promesa=bcsComunes.ventanaPromesa(id1);
    		promesa.then(function(data){ 
				bcsComunes.ventanaOverlay(false, idVentana);
    		});
     	 }
		
	};
	
	$scope.confirmarNotificacion = function (notificacionesTemp)
	{
			var contadorEnviado = 0;
			var confirmaDespacho = 0;
			var confirmado = 0;
			var env = 0; 
			var procesoreenvio = 0;
			var procesoenvio = 0;
			var desc = 0;
			$.each(notificacionesTemp,function(key,notif){
  			if((notif.Enviado != 'Si' && notif.ConfirmacionDespacho == 'No') || notif.Confirmado=='Si' || notif.ReEnviarSrv =='S'|| notif.ConfirmacionDespacho=='No'||notif.Descartado != 'No') 			{
  				contadorEnviado++;
  			}
  			if(notif.ConfirmacionDespacho == 'No'){
				confirmaDespacho++;
  			}
  			if(notif.Confirmado=='Si'){
					confirmado++;
  			}
  			if(notif.Enviado != 'Si'){
				env++;
  			}
  			if(notif.Descartado != 'No'){
  				desc++;
  			}
  			
  		});
		if(contadorEnviado > 0){

			  
			  if(env>0){
				bcsComunes.msg("error","error",$translate.instant('UI_ENVIO_ERROR'));
			  }
			  if(confirmaDespacho>0){
	    		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACION_ERROR_REQ'));
				  }
				  if(confirmado>0){
	    		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACIONRECHAZO_OK'));

				  }
				  
			  if(desc > 0){
 				  bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESCARTADA'));
			  }
		}else{
			
			var id1 = bcsComunes.ventanaAbrir("ConfirmarNotificacion" ,{Notificacion : notificacionesTemp[0], IdVentanaPrincipal : idVentana},false,false,idVentana);
    		var promesa=bcsComunes.ventanaPromesa(id1);
    		promesa.then(function(data){ });
			
		}
	};
	
	$scope.confirmarNotificacionRecibida = function (notificacionesTemp)
	{
			var contadorEnviado = 0;
			var confirmaDespacho = 0;
			var confirmado = 0;
			var env = 0;
			var procesoreenvio = 0;
			var procesoenvio = 0;
			$.each(notificacionesTemp,function(key,notif){
				
				if(notif.ConfirmacionDespacho == 'No') 			{
	  				contadorEnviado++;
	  			}
				if(notif.Confirmado == 'Si') 			{
					confirmaDespacho++;
	  			}
  			 
  			
			});
			
			
		if(contadorEnviado > 0 || confirmaDespacho > 0 ){
				
				if(contadorEnviado >0)
     		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACION_ERROR_REQ'));
				if(confirmaDespacho>0)
	     		bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACIONRECHAZO_OK'));

 			 
		}else{
			
			var id1 = bcsComunes.ventanaAbrir("ConfirmarNotificacion" ,{Notificacion : notificacionesTemp},false,false,idVentana);
    		var promesa=bcsComunes.ventanaPromesa(id1);
    		promesa.then(function(data){ });
			
		}
	};
	
	$scope.rechazarNotificacion = function (notificacionesTemp)
	{
		var contadorEnviado = 0;
			var confirmaDespacho = 0;
			var confirmado = 0;
			var env = 0;
			var reenv = 0;
			var enviar= 0;
			var desc = 0;
			$.each(notificacionesTemp,function(key,notif){
  			if((notif.Enviado != 'Si' && notif.ConfirmacionDespacho == 'No')|| notif.ConfirmacionDespacho == 'No' || notif.ReEnviarSrv == 'S'|| notif.EnviarSrv== 'S' || notif.Confirmado=='Si' ||notif.Descartado != 'No') 
  			{
  				contadorEnviado++;
  			}
  			if(notif.ConfirmacionDespacho == 'No'){
				confirmaDespacho++;
  			}
  			if(notif.Confirmado=='Si'){
					confirmado++;
  			}
  			if(notif.Enviado != 'Si'){
				env++;
  			}
  			if(notif.ReEnviarSrv == 'S'){
				reenv++;
  			}
  			if(notif.EnviarSrv != 'N'){
				enviar++;
  			}
  			if(notif.Descartado != 'No'){
  				desc++;
  			}
  			
  		});
		if(contadorEnviado > 0){

			  
			  if(env>0){
				bcsComunes.msg("error","error",$translate.instant('UI_ENVIO_ERROR'));

			  }
			   if(reenv>0){
				bcsComunes.msg("error","error",$translate.instant('UI_PROCESO_REENVIO'));

			  }
			   if(enviar>0){
				bcsComunes.msg("error","error",$translate.instant('UI_PROCESO_ENVIO'));
			  } 
			   if(confirmaDespacho>0){
	    		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACION_ERROR_REQ'));
				  }
				  if(confirmado>0){
	    		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACIONRECHAZO_OK'));

				  }
			   if(desc > 0){
 				  bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESCARTADA'));
			  }
		}else{
			
			var id1 = bcsComunes.ventanaAbrir("RechazarNotificacion" ,{Notificacion : notificacionesTemp[0], IdVentanaPrincipal : idVentana},false,false,idVentana);
    		var promesa=bcsComunes.ventanaPromesa(id1);
    		promesa.then(function(data){ });
			 
		}    
	};

	$scope.rechazarNotificacionRecibida = function (notificacionesTemp)
	{
		var contadorEnviado = 0;
			var confirmaDespacho = 0;
			var confirmado = 0;
			var env = 0;
			var reenv = 0;
			var enviar= 0;
			$.each(notificacionesTemp,function(key,notif){
				
				if(notif.ConfirmacionDespacho == 'No') 			{
	  				contadorEnviado++;
	  			}
				if(notif.Confirmado == 'Si') 			{
					confirmaDespacho++;
	  			}
  			 
  			
			});	
			if(contadorEnviado > 0 || confirmaDespacho > 0 ){
				
				if(contadorEnviado >0)
     		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACION_ERROR_REQ'));
				if(confirmaDespacho>0)
	     		bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACIONRECHAZO_OK'));

 			 
		}else{
			
			var id1 = bcsComunes.ventanaAbrir("RechazarNotificacion" ,{Notificacion : notificacionesTemp},false,false,idVentana);
    		var promesa=bcsComunes.ventanaPromesa(id1);
    		promesa.then(function(data){ });
			 
		}   
	};

	$scope.descartarNotificacion = function (notificacionesTemp)
	{
		$scope.notificacionesenviadas.dataSource.read();
		var contadorEnviado = 0;
		var tiempoDespacho = 0;
		var descartado = 0;
		var env = 0;
		var datos = {
    			p_IdEmpresa : $scope.SesionIdEmpresa,
	  			p_IdUsuario : $scope.SesionIdUsuario,
	  			p_IdModulo : 'EVT',
	  			p_in_IdNotificacion : [],
	  			p_Accion : 5
    	};
		$.each(notificacionesTemp,function(key,notif){
  			datos.p_in_IdNotificacion.push({IdNotificacion : notif.IdNotificacion});
  			if(notif.Enviado == 'Si' || notif.TiempoDespachoSrv != 'M' || notif.Descartado == 'Si')   
  			{
  				contadorEnviado++;
  			}
  			if(notif.TiempoDespachoSrv != 'M')   
  			{
  				tiempoDespacho++;
  			}
  			if(notif.Enviado == 'Si')   
  			{
  				env++;
  			}
  			if(notif.Descartado == 'Si')   
  			{
  				descartado++;
  			}
  		});
  		if(contadorEnviado>0){
 			  if(tiempoDespacho>0){
    		    bcsComunes.msg("error","error",$translate.instant('UI_CONFIRMACION_ERROR_REQ'));
			  }
			  if(env>0){
    		    bcsComunes.msg("error","error",$translate.instant('UI_ENVIO_ENVIADA')); 
			  }
			  if(descartado>0){
				 bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESCARTADA'));
			  }
			 
  		}else{
		bcsServicioPlataforma.EnviarNotificaciones(datos,function(result)
			    {
       	 console.log(JSON.stringify(result));
       		if (result.P_OUT_ERRORESCONTADOR == "0"){
       				
					bcsComunes.msg("info","info",$translate.instant('UI_DESCARTA_OK'));
					$scope.notificacionesenviadas.dataSource.read();
		        }   
		    else{
		        	bcsComunes.setConflictos(util.parseServerResponse(result));
					bcsComunes.msg("error","error",$translate.instant('UI_DESCARTAR_ERROR'));
		        }
      	 },
			function(result){
				 bcsComunes.msg("error","error",$translate.instant('UI_DESCARTAR_ERROR'));
			});
  		};
	};
	$scope.limpiarFiltros = function ()
	{
		$scope.BuscarNotificaciones = false;
		$scope.conf.kngFechaDesde = util.dateInicioDia(new Date());
		$scope.conf.kngFechaHasta = util.dateTerminoDia(new Date()); 
		$scope.notificacionesenviadas.dataSource.removeAll();
		$scope.notificacionesrecibidas.dataSource.removeAll();   
	}
	$scope.mostarMantenedorEvento = function()
	{
		var id1 = bcsComunes.ventanaAbrir("MantenedorEventos" ,{},false,false,idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ });
	}
	
	$scope.callServletTemplates = function(objFather, datos){
		var contexto = JSON.stringify(objFather);
		var json = JSON.parse(contexto);
		var temp = htmlDecode(htmlEncode($scope.datos.Template));

		console.log($scope.datos.UrlConfirmacion + "URL CONFIRMACION ");
		var template =  json.data.TagGenerales.Confirmar.replace('::URL::',$scope.datos.UrlConfirmacion)+temp.replace(/<!--@(.*?)-->/g, '</@$1>');
		var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
		var dats = {
			template: template,
			context: contexto
		};   	

		$http({ 
			method: 'POST',
			url: url,
			headers: {
				'Content-Type': 'application/x-www-form-urlencoded'
			},
			transformRequest: function(obj) {
			var str = [];
			for(var p in obj)
			str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
			return str.join("&");
			},
			data: dats,
		}).success(function (dataTpt) {
			console.log(htmlEncode(dataTpt));

			bcsServicioPlataforma.EnviarNotificaciones(datos,function(result){
				if (result.P_OUT_ERRORESCONTADOR == "0"){ 
					var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
					$timeout(function(){OpenWindow.init(dataTpt)},500);
					bcsComunes.msg('info', 'Procesar', 'OK');

				}else{
					bcsComunes.ventanaMsg({
						mensaje: "ERROR",
						conflictos:util.parseServerResponse(result)
					},idVentana);
				}
			},function(result){
				bcsComunes.ventanaMsg({
						mensaje: "Error al intentar actualizar notificación",
						conflictos:util.parseServerResponse(result)
					},idVentana);
			}); 

		}).error(function (err){
			bcsComunes.msg('error', 'Procesar', err);
		});
	};

	$scope.verDetalleNotificacion = function (notificacionesTemp) {
		var notif = notificacionesTemp[0].IdNotificacion;
		var confEvt = notificacionesTemp[0].IdConfiguracionEvento;
		var datos = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'EVT',
			p_in_IdNotificacion: [],
			p_Accion: 6
		};
		datos.p_in_IdNotificacion.push({
			IdNotificacion: notificacionesTemp[0].IdNotificacion
		});
		var dataSourceTemplate = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				serverPaging: true,
				serverSorting: true,
				sort: {
					field: "IdPlantilla",
					dir: "desc"
				},
				pageSize: 50,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1405",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPlantilla: {type: "number"},
							Template: {type: "string"},
							IdUsuario: {type: "number"},
							Agrupa: {type: "string"},
							IdConfiguracionEvento: {type: "number"}
						}
					}
				},
				filter: [
					{field: "IdNotificacion", operator: "eq", value: notif},
					{field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
				]
			}
		});
		dataSourceTemplate.fetch(function () {
			$scope.datos = $.extend({}, dataSourceTemplate.data()[0]);
			var dataSourceCamposNoAgrupados = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: {
						field: "IdEventoDetalle",
						dir: "desc"
					},
					pageSize: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1406",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								Llave: {type: "string"},
								Valor: {type: "string"},
								CodigoTipoEvento: {type: "string"},
								IdEvento: {type: "number"},
								IdConfiguracionEvento: {type: "number"},
								Agrupa: {type: "string"}
							}
						}
					},
					filter: {
						logic: "and",
						filters: [
							{field: "IdConfiguracionEvento", operator: "eq", value: confEvt}, 
							{field: "Agrupa",operator: "eq", value: 'N'}, 
							{field: "IdNotificacion", operator: "eq",value: notificacionesTemp[0].IdNotificacion}
						]
					}
				}
			});
			var dataSourceCamposAgrupados = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: {
						field: "IdEventoDetalle",
						dir: "desc"
					},
					pageSize: 50,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1407",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								Llave: {type: "string"},
								Valor: {type: "string"},
								CodigoTipoEvento: {type: "string"},
								IdEvento: {type: "number"},
								IdConfiguracionEvento: {type: "number"},
								Agrupa: {type: "string"},
								IdUsuario: {type: "number"}
							}
						}
					},
					filter: {
						logic: "and",
						filters: [
							{field: "Agrupa", operator: "eq", value: 'S'},
							{field: "IdConfiguracionEvento", operator: "eq", value: confEvt },
							{field: "IdNotificacion", operator: "eq", value: notificacionesTemp[0].IdNotificacion}
						]
					}
				}
			});
			keyValue = [];
			if ($scope.datos.Agrupa === 'S') {
				dataSourceCamposAgrupados.fetch(function () {
					var obj = {};
					var i = 0;
					var total = dataSourceCamposAgrupados.total();
					var codigoevento = '';
					var llave = '';
					$.each(dataSourceCamposAgrupados.data(), function (key, codigos) {
						if (parseInt(codigos.UsuarioEnvia) === $scope.SesionIdUsuario || parseInt(codigos.UsuarioRecibe) === $scope.SesionIdUsuario) {
							if (codigos.Agrupa == 'S') {
								if (i === 0) {
									codigoevento = codigos.CodigoTipoEvento;
									llave = codigos.Llave;
								}
								if (codigoevento === codigos.CodigoTipoEvento) {
									if (i > 0) {
										if (llave === codigos.Llave) {
											keyValue.push(obj);
											objTagDefault[codigoevento] = keyValue;
											obj = {};
											obj[codigos.Llave] = codigos.Valor;
										} else {
											obj[codigos.Llave] = codigos.Valor;
										}
									} else {
										obj[codigos.Llave] = codigos.Valor;
									}
								} else {
									keyValue.push(obj);
									objTagDefault[codigoevento] = keyValue;
									obj = {};
									keyValue = [];
									codigoevento = codigos.CodigoTipoEvento;
									obj[codigos.Llave] = codigos.Valor;
								}
								i++;
								if (i === total) {
									keyValue.push(obj);
									objTagDefault[codigoevento] = keyValue;
									obj = {};
								}
							}
						}
					});
					agregarPredeterminados($scope.view).then((result)=>{
                        if (total > 0) {
                            var contexto = JSON.stringify(result);
                            var json = JSON.parse(contexto);
                            var temp = htmlDecode(htmlEncode($scope.datos.Template));
                            var template = json.data.TagGenerales.Confirmar.replace('::URL::', $scope.datos.UrlConfirmacion) + temp.replace(/<!--@(.*?)-->/g, '</@$1>');
                            var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
                            var dats = {
                                template: template,
                                context: contexto
                            };
                            $http({
                                method: 'POST',
                                url: url,
                                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                                transformRequest: function (obj) {
                                    var str = [];
                                    for (var p in obj) str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                                    return str.join("&");
                                },
                                data: dats
                            }).success(function (dataTpt) {
                                bcsComunes.msg('info', 'Procesar', 'OK');
                                bcsServicioPlataforma.EnviarNotificaciones(datos, function (result) {
                                    if (result.P_OUT_ERRORESCONTADOR == "0") {
                                        var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
                                        $timeout(function () {
                                            OpenWindow.init(dataTpt)
                                        }, 500);
                                        bcsComunes.msg('info', 'Procesar', 'OK');
                                    } else {
                                        bcsComunes.msg("error", "error", $translate.instant(result.P_OUT_ERRORESDETALLE));
                                    }
                                }, function (result) {
                                    console.log(result);
                                    bcsComunes.msg("error", "error", $translate.instant('Error al intentar actualizar notificación'));
                                });
                            }).error(function (err) {
                                bcsComunes.msg('error', 'Procesar', err);
                            });
                        }
                    });
					
				});
			}
			if ($scope.datos.Agrupa === 'N') {
				dataSourceCamposNoAgrupados.fetch(function () {
					var z = 0;
					var obj = {};
					var i = 0;
					var total = dataSourceCamposNoAgrupados.total();
					var c = dataSourceCamposNoAgrupados.data();
					
                    $.each(c, function (key, codigos) {
						if (codigos.Agrupa == 'N') {
							i++;
							if (z === 0) {
								codigoevento = codigos.CodigoTipoEvento;
								obj[codigos.Llave] = codigos.Valor;
							}
							if (z > 0) {
								if (codigoevento === codigos.CodigoTipoEvento) {
									obj[codigos.Llave] = codigos.Valor;
								} else {
									objTagDefault[codigoevento] = obj;
									obj = {};
									obj[codigos.Llave] = codigos.Valor;
									codigoevento = codigos.CodigoTipoEvento;
								}
							}
							z++;
							if (z === total) {
                				objTagDefault[codigoevento] = obj;                
								obj = {};
							}
						}
					});
                    
					agregarPredeterminados($scope.view).then((result)=>{
                        var contexto = JSON.stringify(result);
					    var json = JSON.parse(contexto);
					    var temp = htmlDecode(htmlEncode($scope.datos.Template));
					    var template = json.data.TagGenerales.Confirmar.replace('::URL::', $scope.datos.UrlConfirmacion) + temp.replace(/<!--@(.*?)-->/g, '</@$1>');
					    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
					    var dats = {
    						template: template,
						    context: contexto
					    };

                        $http({
                            method: 'POST',
                            url: url,
                            headers: {
                                'Authorization': 'Basic dGVzdDp0ZXN0',
                                'Content-Type': 'application/x-www-form-urlencoded'
                            },
                            transformRequest: function (obj) {
                                var str = [];
                                for (var p in obj) str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
                                return str.join("&");
                            },
                            data: dats,
                        }).success(function (dataTpt) {
                            console.log(htmlEncode(dataTpt));
                            bcsServicioPlataforma.EnviarNotificaciones(datos, function (result) {
                                if (result.P_OUT_ERRORESCONTADOR == "0") {
    
                                    var OpenWindow = window.open('', 'popup', 'width=900,height=1200');
                                    
                                    if(OpenWindow === null)
                                    {
                                        OpenWindow = window.open('', 'popup', 'width=900,height=1200');
                                        console.log("error en linea 1024");
                                    }	
                                    if((OpenWindow === null) || (OpenWindow === "undefined"))
                                    {
                                        console.log("error en linea 1029");
                                        return;
                                    }	
                                        
                                    OpenWindow.location.href='/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html';
                                    $(OpenWindow.document).ready(function() {
                                        (function init(dataFromParent) {
                                            OpenWindow.document.open();
                                            OpenWindow.document.write(dataFromParent);
                                            OpenWindow.document.close();
                                        })(dataTpt);
                                    });					
                                    bcsComunes.msg('info', 'Procesar', 'OK');
                                } else {
                                    bcsComunes.msg("error", "error", $translate.instant(result.P_OUT_ERRORESDETALLE));
                                }
                            }, function (result) {
                                console.log(result);
                                bcsComunes.msg("error", "error", $translate.instant('Error al intentar actualizar notificación'));
                            });
                        }).error(function (err) {
                            bcsComunes.msg('error', 'Procesar', err);
                        });
                    });
					console.log("fuera 1012");
					
				});
			}
		});
	};

	function htmlDecode(input){
		var e = document.createElement('div');
		e.innerHTML = input;
		return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
	}; 
	var entityMap = { "<": "&lt;", ">": "&gt;" };

	function htmlEncode(string) {
		return String(string).replace(/[<>]/g, function (s) {
			return entityMap[s];
		});
	};

});
