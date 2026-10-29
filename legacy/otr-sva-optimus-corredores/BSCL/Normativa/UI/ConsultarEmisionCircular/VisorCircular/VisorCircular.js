angular.module('ModuloNormativa').controller('VisorCircularesCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsVisorCircularConfig){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	var params = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID, false, 3000);
	
	if(params.IdTipoDocumento === 'ART171Car1') {
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ART171_CARTA1_BCS'));
	}
	else if (params.IdTipoDocumento === 'ART171Car2') {
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ART171_CARTA2_SVS'));
	}
	else if (params.IdTipoDocumento === 'CI63Nomina') {
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_CIR63_NOMINA_DE_PERSONAS'));
	}
	else if (params.IdTipoDocumento === 'CI63Transa') {
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_CIR63_TRANSACCIONES_DE_RELACIONADOS'));
	}
	else if (params.IdTipoDocumento === 'CIR1518') {
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_CIR1518_CUADRATURA_SEMANAL_DE_CUSTODIA'));
	}
	
	
	var dataSource = bcsVisorCircularConfig.getDataSource2(params.tablaParametros);
	var filters= {};
	filters.p_TablaParametros= params.tablaParametros;
	dataSource.lastCallParameters = filters;

	// GRILLA PRINCIPAL
	$scope.confBasicaGridVisorCirculares = {
			dataSource 	: dataSource ,
			columns 	: bcsVisorCircularConfig.getColumns(),
			sortable 	: true,
			pageable 	: true,
			height 		: 450,
			selectable 	: "row",
			filterable 	: true,
			autoBind	: true
		};

	$scope.cerrarVentana = function(){
		bcsComunes.ventanaCerrar($scope.winID);
	};
});