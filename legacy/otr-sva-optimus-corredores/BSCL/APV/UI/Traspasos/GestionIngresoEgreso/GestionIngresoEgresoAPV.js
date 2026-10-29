angular.module('ModuloAPV').controller('GestionAPVIngresoEgresoCtrl',
			function($compile,$scope,$translate,$timeout,$rootScope,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
{
	
	$scope.data = {};	
	//Recupera parametros desde la ventana recibida
	var IdVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

	var gesIngEgr = this;
	gesIngEgr.datosForm = {};
	gesIngEgr.fn = {};
	gesIngEgr.datasource = {};
	gesIngEgr.ConfiguracionCmbo = {};
	gesIngEgr.consulta = {};
	gesIngEgr.HabilitoBotonesTraspaso = false;
	gesIngEgr.datosForm.fechaTraspaso = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	gesIngEgr.idVentana = bcsComunes.ventanaWinId($scope);
	gesIngEgr.domVentana = $("[ventanaid='"+gesIngEgr.idVentana+"']");

	gesIngEgr.tabs = { 	
			PanelIngresos: { id : 'PanelIngresos',	url : '<div ng-include="\'../../APV/UI/Traspasos/GestionIngresoEgreso/PanelIngresos.html\'" ></div>',		  active : true,  cargado: false, datosCambiados: false },
			PanelEgresos: { id : 'PanelEgresos',	url : '<div ng-include="\'../../APV/UI/Traspasos/GestionIngresoEgreso/PanelEgresos.html\'" ></div>',		  active : false,  cargado: false, datosCambiados: false },
	};

	gesIngEgr.fnTabSelect = function(ambito,nombreTab) {
		if (gesIngEgr.tabs[nombreTab].cargado !== true) {
			gesIngEgr.tabs[nombreTab].cargado = true;
			gesIngEgr.domVentana.find('#'+gesIngEgr.tabs[nombreTab].id).append($compile(gesIngEgr.tabs[nombreTab].url)($scope));
		}
	};

	
});
