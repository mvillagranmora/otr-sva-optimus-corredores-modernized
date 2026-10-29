(function() {
	angular.module('ModuloPlataforma').controller('ConsultarLimitesCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var cLim = this;
		cLim.idVentana = bcsComunes.ventanaWinId($scope); 
		cLim.params = bcsComunes.ventanaParams(cLim.idVentana);
		cLim.domVentana = $("[ventanaid='"+cLim.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
     	// Limites Cliente
		cLim.fnTabLimitesCliente = function () {
            console.log('PanelLimitesCliente click');
            if (cLim.TabLimitesCliente !== true) {
            	cLim.TabLimitesCliente = true;
            	cLim.domVentana.find('#PanelLimitesCliente').append($compile('<div ng-include="\'../../Plataforma/UI/LIM/ConsultarLimites/PanelLimitesCliente.html\'" ></div>')($scope));
            }
     	};
     	
     	// Limites Cuenta Propia
     	cLim.fnTabLimitesCuentaPropia = function () {
            console.log('PanelLimitesCuentaPropia click');
            if (cLim.TabLimitesCuentaPropia !== true) {
            	cLim.TabLimitesCuentaPropia = true;
            	cLim.domVentana.find('#PanelLimitesCuentaPropia').append($compile('<div ng-include="\'../../Plataforma/UI/LIM/ConsultarLimites/PanelLimitesCuentaPropia.html\'" ></div>')($scope));
            }
     	};
     	
     	// Evaluacion
     	cLim.fnTabEvaluacionLimites = function () {
            console.log('PanelEvaluacionLimites click');
            if (cLim.TabEvaluacionLimites !== true) {
            	cLim.TabEvaluacionLimites = true;
            	cLim.domVentana.find('#PanelEvaluacionLimites').append($compile('<div ng-include="\'../../Plataforma/UI/LIM/ConsultarLimites/PanelEvaluacionLimites.html\'" ></div>')($scope));
            }
     	};
     	
     // Limites Agente
     	cLim.fnTabLimitesAgente = function () {
            console.log('PanelLimitesAgente click');
            if (cLim.TabLimitesAgente !== true) {
            	cLim.TabLimitesAgente = true;
            	cLim.domVentana.find('#PanelLimitesAgente').append($compile('<div ng-include="\'../../Plataforma/UI/LIM/ConsultarLimites/PanelLimitesAgente.html\'" ></div>')($scope));
            }
     	};
     	
/*********************************************
 * DATASOURCES
 *********************************************/
		
/*********************************************
 * COMBOBOX
 *********************************************/
		     	
/*********************************************
 * FUNCIONES
 ********************************************/
 
	});
}());