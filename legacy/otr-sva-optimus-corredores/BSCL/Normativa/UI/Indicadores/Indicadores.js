(function() {
	angular.module('ModuloNormativa').controller('IndicadoresCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
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
     	

     	// Indicador de Clientes
     	cLim.fnTabCliente = function () {
            console.log('PanelCliente click');
            if (cLim.TabCliente !== true) {
            	cLim.TabCliente = true;
            	cLim.domVentana.find('#PanelCliente').append($compile('<div ng-include="\'../../Normativa/UI/Indicadores/PanelCliente.html\'" ></div>')($scope));
            }
     	};
     	
     	// Indicador de Plataforma
 
     	cLim.fnTabPortafolio = function () {
            console.log('PanelPlataforma click');
            if (cLim.TabPortafolio !== true) {
            	cLim.TabPortafolio = true;
            	cLim.domVentana.find('#PanelPortafolio').append($compile('<div ng-include="\'../../Normativa/UI/Indicadores/PanelPortafolio.html\'" ></div>')($scope));
            }
     	};
     	
     	// Indicador de Persona
		
		cLim.fnTabPersona= function () {
            console.log('PanelPlataforma click');
            if (cLim.TabPersona !== true) {
            	cLim.TabPersona = true;
            	cLim.domVentana.find('#PanelPersona').append($compile('<div ng-include="\'../../Normativa/UI/Indicadores/PanelPersona.html\'" ></div>')($scope));
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