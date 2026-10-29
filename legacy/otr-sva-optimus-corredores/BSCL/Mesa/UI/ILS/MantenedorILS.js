(function() {
	angular.module('ModuloMesa').controller('MantenedorILSCtrl',
	function ($state,
			  $q,
			  $scope,
			  $compile,
			  bcsComunes,
			  $translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		
		var mantenedorILS = this;
		mantenedorILS.idVentana = bcsComunes.ventanaWinId($scope);
		mantenedorILS.params	= bcsComunes.ventanaParams(mantenedorILS.idVentana);
		mantenedorILS.domVentana	= $("[ventanaid='"+mantenedorILS.idVentana+"']");
		
		/*********************************************
		 * FUNCIONES DE CARGA DE TABS
		 ********************************************/
     	
     	// Resultado
		mantenedorILS.fnTabResultado = function () {
            if (mantenedorILS.TabResultado !== true) {
            	mantenedorILS.TabResultado = true;
            	mantenedorILS.domVentana.find('#PanelResultado').append($compile('<div ng-include="\'../../Mesa/UI/ILS/PanelResultado.html\'" ></div>')($scope));
            }
     	};
     	// Conceptos
		mantenedorILS.fnTabConceptos = function () {
            if (mantenedorILS.TabConceptos !== true) {
            	mantenedorILS.TabConceptos = true;
            	mantenedorILS.domVentana.find('#PanelConceptos').append($compile('<div ng-include="\'../../Mesa/UI/ILS/PanelConceptos.html\'" ></div>')($scope));
            }
     	}; 
     	// Items
     	mantenedorILS.fnTabItems = function () {
            if (mantenedorILS.TabItems !== true) {
            	mantenedorILS.TabItems = true;
            	mantenedorILS.domVentana.find('#PanelItems').append($compile('<div ng-include="\'../../Mesa/UI/ILS/PanelItems.html\'" ></div>')($scope));
            }
     	};  
     	// Detalle Item
 		mantenedorILS.fnTabDetalleItem = function () {
	        if (mantenedorILS.TabDetalleItem !== true) {
	        	mantenedorILS.TabDetalleItem = true;
	        	mantenedorILS.domVentana.find('#PanelDetalleItem').append($compile('<div ng-include="\'../../Mesa/UI/ILS/PanelDetalleItem.html\'" ></div>')($scope));
	        }
 		}; 
     	// Detalle Cobertura
 		mantenedorILS.fnTabDetalleCobertura = function () {
	        if (mantenedorILS.TabDetalleCobertura !== true) {
	        	mantenedorILS.TabDetalleCobertura = true;
	        	mantenedorILS.domVentana.find('#PanelDetalleCobertura').append($compile('<div ng-include="\'../../Mesa/UI/ILS/PanelDetalleCobertura.html\'" ></div>')($scope));
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