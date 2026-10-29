angular.module('ModuloPlataforma').controller('MantenedorEvento',function($rootScope,$scope,bcsMantenedorEventosGrid,bcsComunes,bcsServicioPlataforma,bcsServicioOrden,$translate,bcsAlerta,$timeout){

	$scope.conf = {};
	var obj = {};
	var objAsociados = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$rootScope.$on("CallParentEventos",function(){
		$scope.leerEventos();
	});
	
	$rootScope.$on("CallParentVerDetalle",function(event, data){
	 	$scope.verDetalle(data.IdConfiguracionEvento);
 	});

 	//watch
 	$scope.$watch("conf.seleccionadoevento", function(oval, nval){
 		$scope.verDetalle();
 	});
 
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
						,IdEstado					: {type: "string"}
						,IdEstadoUI					: {type: "code"}
						,NombreFormaDespachoUI		: {type: "code"}
						,NombreTiempoDespachoUI		: {type: "code"}
	 					,PermitePersonalizar		: {type: "code"} 
						,FormaDespacho				: {type: "code"} 
						,TiempoDespacho				: {type: "string"} 
						,ConfirmacionDespacho		: {type: "string"} 
						,ConfirmacionDespachoUI		: {type: "code"} 
						,FechaVencimiento	 		: {type: "date"} 
						,FechaRecordatorio			: {type: "date"} 
						,IdPlantilla				: {type: "number"} 
						,IdPlantillaRecordatorio	: {type: "number"}
						,CodigoTipoEvento			: {type: "string"}
						,ControlProceso		    	: {type: "string"}
						
					}
				} 
			}
		}
	});
	
	var dataSourceAsociados = new bcs.DataSource({
		
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "IdConfiguracionEvento", dir: "desc" },
			pageSize : 50,
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t="+1384,
					dataType: "json"
				}
			},
			schema :
			{
				model: {
					fields : {

							 CodigoTipoEvento					: {type: "string"}
							,DireccionEvento 					: {type: "string"}
							,DescripcionTipoEvento 				: {type: "string"}
							,IdConfiguracionEvento				: {type: "number"}
							,IdEstadoDetalle 					: {type: "code"}
						
					}
				}
			},
		}
	});
	
	
 
	 
	 
	$scope.configuracionevento = {
			dataSource			: dataSourceConfiguracion,
			columns				: bcsMantenedorEventosGrid.columnasConfiguracionEvento(),		
			sortable			: true,
			filterable			: true,
			autoBind			: true,  //comentar
			height				: 220,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
			gridCreated: function(grid) {
				$scope.mainGrid = grid;
			},
		    selectable 			: "multiple row"    
		};

	$scope.asociadosAConfiguracion = {
			
			dataSource			: dataSourceAsociados,
			columns				: [
				                    { field: "DescripcionTipoEvento"		,title: 'UI_NOMBRE_EVENTO'				, width: "321px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: center;"} },
				                    { field: "DireccionEvento"				,title: 'UI_DIRECCION_EVENTO'			, width: "321px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"} },
 				                    { field: "IdEstadoDetalle"			    	,title: 'UI_ESTADO'				, attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: center;"} },
				            
				                    ],	    	
			sortable			: true,
			filterable			: true,
			pageable			: true,
			autoBind			: false,  //comentar
			height				: 220,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "multiple row"
			
			
	}
	 
	$scope.verDetalle = function(IdConfiguracionEvento){
		
		var filters = new Array();

		if (!!$scope.conf.seleccionadoevento && $scope.conf.seleccionadoevento[0].IdConfiguracionEvento)
			filters.push({ field: "IdConfiguracionEvento", operator: "eq", value: $scope.conf.seleccionadoevento[0].IdConfiguracionEvento }); 
		dataSourceAsociados.filter(filters);
		
	};
	
	$scope.administrarPlantilla = function(){
		
		var id1 = bcsComunes.ventanaAbrir("MantenedorPlantilla" ,false,true);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			$scope.configuracionevento.dataSource.read();
		});
	};
	
	$scope.ActivarConfiguracion = function(configEvt){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_IdConfiguracionEvento : [],
	   			p_Accion : 1
 					};
				
	        $.each(configEvt,function(key,codigos){
 	  			datos.p_IdConfiguracionEvento.push({IdConfiguracionDetalle : codigos.IdConfiguracionEvento});
 	  		});
	        bcsServicioPlataforma.ActivarConfiguracionEvento(datos,function(result)
		    {
				var titulo = "";
				if (result.P_OUT_ERRORESCONTADOR == "0"){	
					$scope.gridApiAsociadosAConfiguracion.dataSource.read();
					titulo ="NOTIFICACION ACTIVADA CORRECTAMENTE";
				}else{
					titulo ="OCURRIO UN ERROR AL ACTIVAR LA NOTIFICACION";
				}
				$scope.mainGrid.dataSource.read();

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
		        	 
			},
			function(result){
				datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
			});
	};

	$scope.DesactivarConfiguracion = function(configEvt){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,    
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_IdConfiguracionEvento : [],
	   			p_Accion : 2   
 					};
				
	        $.each(configEvt,function(key,codigos){
 	  			datos.p_IdConfiguracionEvento.push({IdConfiguracionDetalle : codigos.IdConfiguracionEvento});
  	  		});
	        bcsServicioPlataforma.ActivarConfiguracionEvento(datos,function(result)
		    {
				var titulo = "";
				if (result.P_OUT_ERRORESCONTADOR == "0"){	
					$scope.gridApiAsociadosAConfiguracion.dataSource.read();
					titulo ="NOTIFICACION DESACTIVADA CORRECTAMENTE";
				}else{
					titulo ="OCURRIO UN ERROR AL DESACTIVAR LA NOTIFICACION";
				}
				$scope.mainGrid.dataSource.read();
				
				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
		        	 
			},
			function(result){
				datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
			});
	};
	$scope.ActivarConfiguracionDetalle = function(configEvt){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_IdConfiguracionEventoDetalle : [],
	   			p_Accion : 1
 					};
				
	        $.each(configEvt,function(key,codigos){
 	  			datos.p_IdConfiguracionEventoDetalle.push({IdConfiguracionEventoDetalle : codigos.IdConfiguracionEventoDetalle});
	  		});
	        bcsServicioPlataforma.ActivarConfiguracionEventoDetalle(datos,function(result)
		    {
				var titulo = "";
				if (result.P_OUT_ERRORESCONTADOR == "0"){	
					$scope.gridApiAsociadosAConfiguracion.dataSource.read();
					titulo ="EVENTO ACTIVADO CORRECTAMENTE";
				}else{
					titulo ="OCURRIO UN ERROR AL ACTIVAR EL EVENTO";
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
		        	 
			},function(result){   
				datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
			});
	};
	
	$scope.DesactivarConfiguracionDetalle = function(configEvt){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_IdConfiguracionEventoDetalle : [],
	   			p_Accion : 2
 					};
				
	        $.each(configEvt,function(key,codigos){
	        	 
 	  			datos.p_IdConfiguracionEventoDetalle.push({IdConfiguracionEventoDetalle : codigos.IdConfiguracionEventoDetalle});
	  		});
	        bcsServicioPlataforma.ActivarConfiguracionEventoDetalle(datos,function(result)
		    {
				var titulo = "";
				if (result.P_OUT_ERRORESCONTADOR == "0"){	
					$scope.gridApiAsociadosAConfiguracion.dataSource.read();
					titulo ="EVENTO DESACTIVADO CORRECTAMENTE";
				}else{
					titulo ="OCURRIO UN ERROR AL DESACTIVAR EL EVENTO";
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
		        	 
			},
			function(result){
				datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
			});
	};
	
	$scope.quitarEvento = function(configEvt){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_CodigoTipoEvento : [],
  					};    
				
	        $.each(configEvt,function(key,codigos){
	        	 
 	  			datos.p_CodigoTipoEvento.push({CodigoTipoEvento : codigos.CodigoTipoEvento, IdConfiguracionEvento : codigos.IdConfiguracionEvento});
 
 	  		});     
	        bcsServicioPlataforma.QuitarEvento(datos,function(result)
		    {
				var titulo = "";
				if (result.P_OUT_ERRORESCONTADOR == "0"){	
					$scope.gridApiAsociadosAConfiguracion.dataSource.read();
					titulo ="EL EVENTO FUE QUITADO CORRECTAMENTE";
				}else{
					titulo ="OCURRIO UN ERROR AL QUITAR EL EVENTO";
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
		        	 
			},
			function(result){
				datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
			}); 
	};

	 
	$scope.leerEventos = function(){
		 
		$scope.gridApiConfEvento.dataSource.read();
	};
	
 
	$scope.limpiarGrillas = function ()
	{
		dataSourceConfiguracion.removeAll();
		dataSourceAsociados.removeAll();
  	 
	};
	
	$scope.abrirConfigurarEvento = function()
	{
		var id1 = bcsComunes.ventanaAbrir("ConfigurarEventos" ,{},false,false,idVentana);  
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			$scope.configuracionevento.dataSource.read();
		});
 	}
	$scope.ActualizarConfEventos = function()
	{
		var total = $scope.conf.seleccionadoevento.length;
		console.log(total+"**********TOTAL");
		if(total>1){
			 bcsComunes.msg("error","error",$translate.instant('No se puede editar más de una notificación simultaneamente'));
		}else{
			$timeout(function(){
				var id1 = bcsComunes.ventanaAbrir("ActualizarConfEventos" ,{ConfiguracionEvento : $scope.conf.seleccionadoevento},false,false,idVentana);
				var promesa=bcsComunes.ventanaPromesa(id1);
				promesa.then(function(data){ 
					$scope.configuracionevento.dataSource.read();
				});
			},500); 
		}
		
	}
	$scope.abrirDetalleEvento = function()
	{	 
		
		var id1 = bcsComunes.ventanaAbrir("AsociarTiposDeEventos",{ idconf : $scope.conf.seleccionadoevento[0].IdConfiguracionEvento},false,false,idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			$scope.configuracionevento.dataSource.read();
		});
	}

	
 
});
