(function() {
	angular.module('ModuloNormativa').controller('EstadisticaOperacionClienteCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var cEst = this;
		cEst.idVentana = bcsComunes.ventanaWinId($scope); 
		cEst.params = bcsComunes.ventanaParams(cEst.idVentana);
		cEst.domVentana = $("[ventanaid='"+cEst.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	

     	// FOD
     	cEst.fnTabOperaciones = function () {
            console.log('PanelOperaciones click');
            if (cEst.TabOperaciones !== true) {
            	cEst.TabOperaciones = true;
            	cEst.domVentana.find('#PanelOperaciones').append($compile('<div ng-include="\'../../Normativa/UI/EstadisticaOperacionCliente/PanelOperaciones.html\'" ></div>')($scope));
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