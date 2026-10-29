angular.module('ModuloPlataforma').controller('AsociarTipoEvento',function($rootScope, $scope,bcsMantenedorEventosGrid,bcsComunes,bcsServicioPlataforma,$translate,bcsAlerta){

	$scope.conf = {};
	
	
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.configuracion = parametros.ConfiguracionEvento;
	var idconf = parametros.idconf;
 	var filters = [];
	
 	$scope.childMethodEventos = function(idEvt){
		$rootScope.$emit("CallParentVerDetalle",{IdConfiguracionEvento : idEvt});
	};
 
	 var dataSourceYaAsociados = new bcs.DataSource({
			
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "CodigoTipoEvento", dir: "desc" },
							pageSize 			: 50,
							transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1384",
										dataType: "json"
									}
							},
							schema 				: {
													model: {
														fields : {
				 		 
																 CodigoTipoEvento					: {type: "string"},
 
														}
													}
												},
 	 
							
						  }
						      
					
		}); 
	 
	 dataSourceYaAsociados.fetch().then(function(){
		 
		 $.each(dataSourceYaAsociados.data(),function(key,codigos){
			 	console.log(codigos.CodigoTipoEvento);
			 	console.log(idconf);
			 	filters.push({field : "CodigoTipoEvento",operator:"neq",value : codigos.CodigoTipoEvento});
			});  
		 
		  $scope.asociartipoevento.dataSource.filters(filters);	
 	 });
	filters.push({field:"CodigoTipoEvento",operator:"neq",value:'TagGenerales'});
 	
	 var dataSourceAsociados = new bcs.DataSource({
			
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "CodigoTipoEvento", dir: "desc" },
							pageSize 			: 50,
							transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1382",
										dataType: "json"
									}
							},
							schema 				: {
													model: {
														fields : {
						
																 CodigoTipoEvento					: {type: "string"}
																,DireccionEvento 					: {type: "string"}
																,Descripcion						: {type: "string"}
														}
													}
												},
							filter				: filters
						  }
						
					
		});
	 
	 $scope.asociartipoevento = {
				dataSource			: dataSourceAsociados,
				columns				: bcsMantenedorEventosGrid.columnasEventoAsociados(),		
				autoBind			: false,  //comentar
				height				: 220,
			    selectable 			: "multiple row"
			};
	
	
	$scope.AsocicarTipoEventoConConfiguracion = function(codigosTipoEventos)
	{
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
	   			p_IdUsuario : $scope.SesionIdUsuario,
	   			p_IdModulo : 'EVT',
	   			p_IdConfiguracionEvento : idconf,
	   			p_CodigoTipoEvento : [],
					};
	        $.each(codigosTipoEventos,function(key,codigos){ 
	  			datos.p_CodigoTipoEvento.push({CodigoTipoEvento : codigos.CodigoTipoEvento});
 	  		});
 	  		
 	  		
	        bcsServicioPlataforma.AsocicarTipoEventoConConfiguracion(datos,function(result)
		    {
		        	 console.log(JSON.stringify(result));
		        	 var msg = "";
		        	 if (result.P_OUT_ERRORESCONTADOR == "0"){
							msg = $translate.instant("Evento asociado correctamente");		
							$scope.childMethodEventos(idconf);
				     }else{
							msg = $translate.instant(result.P_OUT_ERRORESDETALLE);
				     }
		        	 
		        	 bcsComunes.ventanaMsg({
		        			mensaje: msg,
		        			conflictos:util.parseServerResponse(result)
		        		},idVentana);
		        	 bcsComunes.ventanaCerrar(idVentana);
		        },
				function(result){
		        	datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
	}


	
 
});
