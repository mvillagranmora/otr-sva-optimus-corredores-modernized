angular.module('ModuloPlataforma').controller('ConsultarConfirmarRechazarNotificaciones',function($rootScope,$scope,bcsServicioPlataforma,bcsMantenedorEventosGrid,bcsComunes,$translate){
	
	$scope.conf = {}; 
	$scope.datos = {};
	
 	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
 	
	/************************
	 ***DECLARACION GRIDS****
	 *************************/
	var dataSourceNotificaciones = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "FechaIngreso", dir: "desc" }, 
			pageSize : 20,   
		    transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290621",
					dataType: "json"
				}
			}, 
			schema : 
				{
					model: {
						fields : {
							 IdEmpresa						: {type: "number"}
							,CodigoTipoEvento	 			: {type: "string"}
							,FechaIngreso					: {type: "date"}
							,IdEstado						: {type: "string"}
							,CantidadEventos				: {type: "number"}
							,IdOrigenEvento  				: {type: "number"}
							,DescripcionOrigen				: {type: "string"} 
							,SecuenciaOrigen				: {type: "string"} 
							,TipoEventoOrigen  				: {type: "string"}
							
						}
					}
			}
			//filter : {field : "IdUsuarioEnviado", operator: "eq", value : $scope.SesionIdUsuario},
		}
	});
	
	$scope.notificaciones = {
		dataSource			: dataSourceNotificaciones,
		columns				: bcsMantenedorEventosGrid.columnasConsultarConfirmarRechazarNotificaciones(),		
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
	
	/**
	 * FUNCIONES BUSQUEDA GRIDS
	 */
	$scope.BuscarNotificaciones = function(){     
		
		var filters = new Array();
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
		filters.push({ field: "IdEstado", operator: "eq", value: "VIG"});
		
 		$scope.notificaciones.dataSource.filter(filters);		
	};
	
	$scope.verDetalle = function(items)
	{
		if (!items) return;
		bcsComunes.ventanaAbrir("ConsultarDetalleEventos", {IdOrigenEvento : items[0].IdOrigenEvento} );
  	};

  	$scope.callCambiarEstadoNotificacion = function(datos, estado){
  		bcsComunes.ventanaOverlay($scope.ventanaId,true);
  					
			var datosConsulta = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA',
				p_Estado: estado,
				p_OrigenesEvento: datos.map( d => { return {"IdOrigenEvento" : d.IdOrigenEvento}}),
				p_descripcionOrigen: datos.map( d => { return {"DescripcionOrigen" : d.DescripcionOrigen}}),
				p_secuenciaOrigen:datos.map( d => { return {"SecuenciaOrigen" : d.SecuenciaOrigen}}), 
				p_tipoEventoOrigen:datos.map( d => { return {"TipoEventoOrigen" : d.TipoEventoOrigen}})
			};
			bcsServicioPlataforma.Servicios().CambiarEstadoNotificacion(datosConsulta).$promise.then(function(result){
                $scope.BuscarNotificaciones();
                bcsComunes.ventanaOverlay($scope.ventanaId,false);
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_BORRADOR'),$scope.ventanaId);
			});
  	}
		
	$scope.aprobarNotificacion = function (notificacionesTemp)
	{
		 //TODO
		 console.log("APRUEBO");
		 console.log(notificacionesTemp);
		 $scope.callCambiarEstadoNotificacion(notificacionesTemp, "A");
	};
	
	$scope.rechazarNotificacion = function (notificacionesTemp)
	{
		//TODO 
		console.log("RECHAZO");
		console.log(notificacionesTemp);
		$scope.callCambiarEstadoNotificacion(notificacionesTemp, "R");
	};
	
});
