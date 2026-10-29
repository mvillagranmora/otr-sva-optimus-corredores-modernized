angular.module('ModuloOrdenes').controller('DesbloquearOrdenesCtrl',function($scope,bcsComunes,$translate,bcsConfiguracionOrdenesV2,bcsServicioOrden){

	$scope.datos = {};
	$scope.filtros = {};
	$scope.filtros.SessionIdEmpresa = $scope.SesionIdEmpresa;

	var conf = bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenesBloqueadas();
	$scope.defaultOptionsNew = conf.options;
	//$scope.buscarOrdenes = conf.buscar;
	$scope.buscarOrdenes = function(grid, filters){
		var resp = util.validarRangoFecha(filters.fechaDesde, filters.fechaHasta);
		if(!resp.estado){
			conf.buscar(grid,filters);	
		}else{
			bcsComunes.msg("warning", "INFO", resp.mensaje);
		}
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.IdTipoOperacion = 0;
		$scope.filtros.IdTipoInstrumento = 0;
		$scope.filtros.IdClasificacionTipoInstrumento = 'RV';
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		
	};
	
	$scope.desbloquearOrdenesSistema = function(ordenesBloqueadas) {
		console.log(ordenesBloqueadas);
		if (ordenesBloqueadas && ordenesBloqueadas.length>0) {
			ejecutarDesbloqueoOrdenes(ordenesBloqueadas);
		}
		else {
			bcsComunes.msg("error",$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_ORDENES"));
		}
	};
	
	var ejecutarDesbloqueoOrdenes = function(ordenesBloqueadas){
		var ordenes = [];
		$.each(ordenesBloqueadas,function(key,value) { 
			ordenes.push({IdOrden: value.IdOrden});
		});	
		
		var datos = {
			p_IdEmpresa		:$scope.SesionIdEmpresa,
		    p_IdUsuario		:$scope.SesionIdUsuario,
		    p_IdModulo		:'ORD',
		    p_Accion		:'D',
			p_Motivo		:'',	
			p_ordenes	:ordenes,
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.BloqueoDesbloqueoOrden(datos).$promise.then(function(result){
		
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var operaciones = [];
			
			$.each(result.P_OUT_ORDENES,function(key,value) {
				operaciones.push({id: value.IdOrden , resultado: (value.EstadoBloqueo==="N")?"OK":"NO OK", mensaje: value.Comentario});
			});
			
			var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_ORDENES_DESBLOQUEADAS"), 
				nombreEntidad : $translate.instant("UI_ORDEN"), 
				operaciones : operaciones   
			}, false, true);
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.buscarOrdenes($scope.apiGridOrdenesBloqueadas,$scope.filtros);
			});
					
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			console.log("ERROR" + JSON.stringify(result));
		});
		
	};
	

	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden} );
	};
   
	$scope.datos.oConfiguracionClasificacionTipoInstrumento={
		filtros : { 	
					
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};	
  
	$scope.datos.oConfiguracionTipoInstrumento={
		filtros : { 	
			IdClasificacionTipoInstrumento : {
				"field"   : "IdClasificacionTipoInstrumento",
		        "operator": "eq",
		        "value"   : ""
			}				
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.datos.oConfiguracionTipoOperacion={
		filtros : { 	EsOrden :  {"field"   : "EsOrden","operator": "eq","value"   : "S"},
						IdTipoInstrumento :	{"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""}	},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
  
  	$scope.limpiarFiltros();
  	
});
