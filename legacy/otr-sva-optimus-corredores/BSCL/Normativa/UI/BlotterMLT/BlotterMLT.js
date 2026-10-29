(function() {
	angular.module('ModuloNormativa').controller('BlotterMLTCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var bTra = this;
		bTra.idVentana = bcsComunes.ventanaWinId($scope); 
		bTra.params = bcsComunes.ventanaParams(bTra.idVentana);
		bTra.domVentana = $("[ventanaid='"+bTra.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	

    	
     	// ** EGRESOS
		/*
		bTra.fnTabPanelEgresos = function () {
            console.log('PanelEgresos click');
            if (bTra.TabPanelEgresoCargado !== true) {
            	bTra.TabPanelEgresoCargado = true;
            	bTra.domVentana.find('#PanelEgresos').append($compile('<div ng-include="\'../../Normativa/UI/BlotterMLT/PanelEgresos.html\'" ></div>')($scope));
            	
            }
            
     	};
     	*/
		// INGRESOS
     	bTra.fnTabPanelIngresos = function () {
            console.log('PanelIngresos click');
            if (bTra.TabPanelIngresoCargado !== true) {
            	bTra.TabPanelIngresoCargado = true;
            	bTra.domVentana.find('#PanelIngresos').append($compile('<div ng-include="\'../../Normativa/UI/BlotterMLT/PanelIngresos.html\'" ></div>')($scope));
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