angular.module('ModuloAPV').controller('GestionDeRecaudacionCtrl',
	function($compile,$scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {}; 
		
		var GestRec = this;
		//Configurar al principio
		GestRec.data = {};
		GestRec.idVentana = bcsComunes.ventanaWinId($scope);
		GestRec.domVentana = $("[ventanaid='"+GestRec.idVentana+"']");
		
		GestRec.tabs = { 	
				PanelDistribucion: { id : 'PanelDistribucion',	url : '<div ng-include="\'../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelDistribucion.html\'" ></div>',		  active : false,  cargado: false, datosCambiados: false },
				PanelRecaudacion: { id : 'PanelRecaudacion',	url : '<div ng-include="\'../../APV/UI/Recaudacion/GestionDeRecaudacion/PanelRecaudacion.html\'" ></div>',		  active : true,  cargado: false, datosCambiados: false },
		};

		GestRec.fnTabSelect = function(ambito,nombreTab) {
			if (GestRec.tabs[nombreTab].cargado !== true) {
				GestRec.tabs[nombreTab].cargado = true;
				GestRec.domVentana.find('#'+GestRec.tabs[nombreTab].id).append($compile(GestRec.tabs[nombreTab].url)($scope));
			}
		};
	});