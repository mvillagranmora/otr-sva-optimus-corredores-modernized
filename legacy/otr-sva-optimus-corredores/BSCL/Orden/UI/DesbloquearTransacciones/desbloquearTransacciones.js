angular.module('ModuloOrdenes').controller('DesbloquearTransaccionesCtrl',function($scope,bcsComunes,bcsConfiguracionOrdenesV2,bcsServicioOrdenV2,bcsServicioPlataforma,bcsServicioOrden,$translate){
	
	var conf = bcsConfiguracionOrdenesV2.getConfiguracionConsultarTransaccionesBloqueadas();
	$scope.defaultOptionsDesbloqueo = conf.options;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var buscarTransacciones = conf.buscar;
	
	$scope.buscarTransaccionesBloqueadas = function() {
		buscarTransacciones($scope.apiGridTransaccionesBloqueadas,$scope.filtrosTransaccionesBloqueadas);
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtrosTransaccionesBloqueadas.SessionIdEmpresa = $scope.SesionIdEmpresa;
		$scope.filtrosTransaccionesBloqueadas.buscarInstrumento = "-1";
		$scope.filtrosTransaccionesBloqueadas.IdTipoOperacion = 0;
		$scope.filtrosTransaccionesBloqueadas.IdTipoInstrumento = 0;
		$scope.filtrosTransaccionesBloqueadas.IdClasificacionTipoInstrumento = 'RV';
		$scope.filtrosTransaccionesBloqueadas.fechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
	};
	
	$scope.desbloquearTransaccionSistema = function(transaccionesBloqueadas) {
		console.log(transaccionesBloqueadas);
		if (transaccionesBloqueadas && transaccionesBloqueadas.length>0) {
			ejecutarDesbloqueoTransacciones(transaccionesBloqueadas);
		}
		else {
			bcsComunes.msg("error",$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TRANSACCIONES"));
		}
	};
	
	var ejecutarDesbloqueoTransacciones = function(transaccionesBloqueadas){
		var transacciones = [];
		$.each(transaccionesBloqueadas,function(key,value) { 
			transacciones.push({IdTransaccion: value.IdTransaccion});
		});	
		
		var datos = {
			p_IdEmpresa		:$scope.SesionIdEmpresa,
		    p_IdUsuario		:$scope.SesionIdUsuario,
		    p_IdModulo		:'ORD',
		    p_Accion		:'D',
			p_Motivo		:'',	
			p_transacciones	:transacciones,
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServicioOrden.BloqueoDesbloqueoTrasnsaccion(datos).$promise.then(function(result){
		
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var operaciones = [];
			
			$.each(result.P_OUT_TRANSACCIONES,function(key,value) {
				operaciones.push({id: value.IdTransaccion , resultado: (value.EstadoBloqueo==="N")?"OK":"NO OK", mensaje: value.Comentario});
			});
			
			var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_TRANSACCIONES_DESBLOQUEADAS"), 
				nombreEntidad : $translate.instant("UI_TRANSACCION"), 
				operaciones : operaciones   
			}, false, true);
			bcsComunes.ventanaPromesa(id).then(function() {
				$scope.buscarTransaccionesBloqueadas($scope.apiGridTransaccionesBloqueadas,$scope.filtrosTransaccionesBloqueadas);
			});
					
			
		
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			console.log("ERROR" + JSON.stringify(result));
		});
		
	};
	
	//GO
	$scope.filtrosTransaccionesBloqueadas = {};  
  	$scope.data = {};
 	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
  	$scope.data.oConfiguracionTipoInstrumento={
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
  	$scope.data.oConfiguracionTipoOperacion={
			filtros : { 	IdClasificacionTipoInstrumento :  {"field"   : "IdClasificacionTipoInstrumento","operator": "eq","value"   : ""},
							IdTipoInstrumento :	{"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""}	},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
  	
  	$scope.limpiarFiltros();
  	

});
