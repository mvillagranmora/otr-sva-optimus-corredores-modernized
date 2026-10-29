angular.module('ModuloNormativa').controller('DetalleSolicitudCircularCtrl',
		function($scope, $translate, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA,
				bcsBlotterEmisionCircular)
{
	$scope.data = {};
	
	$scope.windID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID, false, 3000);
	$scope.btnActivo = true;
	
	$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	
	/**
	 * 	CONFIGURACION DE COMBOS
	 */
	// configuracion combo Normativa
	
	$scope.data.oConfiguracionCircular = {
			tipo : "combobox-tipo-normativa",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	/**
	 * GRILLA PRINCIPAL
	 */
	var dsDetalleSolicitudCircular = bcsBlotterEmisionCircular.dataSourceSolicitudCircular();
	$scope.confBasicaDetalleSolicitudCircular = {
			dataSource			: dsDetalleSolicitudCircular,
			columns				: bcsBlotterEmisionCircular.columnasDetalleSolicitudCircular(),		
			sortable			: true,
			pageable			: true,
			height				: 400, 
			selectable 			: "row",
			autoBind			: false,
			filterable			: true
	};
	
	/**
	 * GRILLA PARAMETROS SOLICITUD CIRCULAR
	 */
	
	$scope.confBasicaParametrosSolicitudCircular = function(dataItem){
		var dsParametrosSolicitudCircular = bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular();
		dsParametrosSolicitudCircular.definition.filter = {field: "IdSolicitudCircular", operator: "eq", value: Number(dataItem.IdSolicitudCircular)};
		return {
			dataSource			: dsParametrosSolicitudCircular,
			columns				: bcsBlotterEmisionCircular.columnasParametrosSolicitudCircular(),		
			sortable			: true,
			pageable			: true,
			height				: 300, 
			selectable 			: "row",
			autoBind			: true,
			filterable			: true
		}
	};
	$scope.buscarDetalleSolicitudCircular = function() {
		var filters = [];
		//FILTROS
		if($scope.data.oConfiguracionCircular.seleccionado && $scope.data.oConfiguracionCircular.seleccionado.IdTipoCircular !== 0 && $scope.data.oConfiguracionCircular.seleccionado !== undefined && $scope.data.oConfiguracionCircular.seleccionado.IdTipoCircular !== '' && $scope.data.oConfiguracionCircular.seleccionado !== null)
			filters.push({field: "IdTipoCircular", operator: "eq", value: $scope.data.oConfiguracionCircular.seleccionado.IdTipoCircular});
		if($scope.data.kngFechaDesde)
			filters.push({field: "FechaIngresoSolicitud", operator: "gte", value: util.dateInicioDia($scope.data.kngFechaDesde)});
		if($scope.data.kngFechaHasta)
			filters.push({field: "FechaIngresoSolicitud", operator: "lte", value: util.dateTerminoDia($scope.data.kngFechaHasta)});
		
		dsDetalleSolicitudCircular.filters(filters);
		
	}
	
});