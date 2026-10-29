angular.module('ModuloOrdenes').controller('InformacionAsignacionPropCronoCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2) {

	$scope.apiTran;
	$scope.apiOrd;
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	var filtros = bcsComunes.ventanaParams(idVentana);
	
	$scope.confTran = {
			gridCreated : function(e) {
				$scope.apiTran = e;
			},
			autoBind: true,
			height : 220,
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					data : [],
					schema : bcsConfiguracionOrdenesV2.getSchemaTransaccion()
				}
				
			}),
			columns : [
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTransaccion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Instrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleTransaccion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioTransaccion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionTransaccion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdCondicionDeLiquidacion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa")
			]
	};
	
	$scope.confOrd = {
			gridCreated : function(e) {
				$scope.apiOrd = e;
			},
			autoBind: true,
			height : 220,
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					data : [],
					schema : bcsConfiguracionOrdenesV2.getSchemaOrden()
				}
				
			}),
			columns : [
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Instrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoPrecio"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdCondicionDeLiquidacion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa")
			]
	}


	var datos = {
		p_IdEmpresa 						: $scope.SesionIdEmpresa,
		p_IdUsuario							: $scope.SesionIdUsuario,
		p_IdModulo							: 'ORD',
		p_HoraHasta 						: filtros.HoraHasta,
		p_IdLibroOrdenes					: filtros.IdLibroOrdenes,
		p_IdClasificacionTipoInstrumento	: filtros.IdClasificacionTipoInstrumento,
		p_IdCondicionDeLiquidacion			: filtros.IdCondicionDeLiquidacion,
		p_IdTipoInstrumento					: filtros.IdTipoInstrumento,
		p_IdAccionOrden						: filtros.AccionOrden,
		p_IdBolsa							: filtros.IdBolsa,
		p_IdInstrumento						: filtros.IdInstrumento,
		p_IdMonedaNegociacion				: filtros.IdMonedaNegociacion
	};

	var p = bcsServicioOrden.ConsultarInformacionEjecutarAsignacionPorHora(datos).$promise;
	p.then(function(result){
		$.each(result.P_OUT_TRANSACCIONES,function(p,k){
			k.FechaOperacionTransaccion = util.dateDS(k.FechaOperacionTransaccion,"FechaHoraSistema","CALL")
		});
		$.each(result.P_OUT_ORDENES,function(p,k){
			k.FechaOperacionOrden = util.dateDS(k.FechaOperacionOrden,"FechaHoraSistema","CALL")
		});
		$scope.apiTran.dataSource.data(result.P_OUT_TRANSACCIONES);
		$scope.apiOrd.dataSource.data(result.P_OUT_ORDENES);
	});
	
	
	
		
});
