angular.module('ModuloCuentaInversion').controller('ParametrosCobroComisionesCtrl',
		function($compile,$scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope, bcsServiciosCuentaInversion ){

	var pcc = this;
	pcc.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(pcc.idVentana);
	pcc.domVentana = $("[ventanaid='"+pcc.idVentana+"']");
	pcc.data = {};
	
	//SETEO INICIAL
	pcc.tabs = {
		PanelCobroComisionOpcionesCobros	: { id : 'PanelCobroComisionOpcionesCobros',	url : '<div ng-include="\'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/PanelCobroComisionOpcionesCobros.html\'" ></div>',		  active : true,  cargado: false, datosCambiados: false },
		PanelCobroComisionConceptos			: { id : 'PanelCobroComisionConceptos',	url : '<div ng-include="\'../../CuentaInversion/UI/BlotterCobroComisiones/ParametrosCobroComisiones/PanelCobroComisionConceptos.html\'" ></div>',		  active : false,  cargado: false, datosCambiados: false },
	};
	
	pcc.fnTabSelect = function (ambito,nombreTab) {
		if (pcc.tabs[nombreTab].cargado !== true) {
			pcc.tabs[nombreTab].cargado = true;
			pcc.domVentana.find('#'+pcc.tabs[nombreTab].id).append($compile(pcc.tabs[nombreTab].url)($scope));
		}
	};
	
});
