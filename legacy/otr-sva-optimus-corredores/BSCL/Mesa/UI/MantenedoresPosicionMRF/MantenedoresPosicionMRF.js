(function() {
	angular.module('ModuloMesa').controller('MantenedoresPosicionMRFCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var mPMRF = this;
		mPMRF.idVentana	= bcsComunes.ventanaWinId($scope); 
		mPMRF.params	= bcsComunes.ventanaParams(mPMRF.idVentana);
		mPMRF.domVentana= $("[ventanaid='"+mPMRF.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
		mPMRF.fnTabMetodoPosicion = function () {
            console.log('PanelMetodoPosicion click');
            if (mPMRF.TabMetodoPosicion !== true) {
            	mPMRF.TabMetodoPosicion = true;
            	mPMRF.domVentana.find('#PanelMetodoPosicion').append($compile('<div ng-include="\'../../Mesa/UI/MantenedoresPosicionMRF/PanelMetodoPosicion.html\'" ></div>')($scope));
            }
     	};
     	
     	mPMRF.fnTabTipoOperacionPosicion = function () {
            console.log('PanelTipoOperacionPosicion click');
            if (mPMRF.TabTipoOperacionPosicion !== true) {
            	mPMRF.TabTipoOperacionPosicion = true;
            	mPMRF.domVentana.find('#PanelTipoOperacionPosicion').append($compile('<div ng-include="\'../../Mesa/UI/MantenedoresPosicionMRF/PanelTipoOperacionPosicion.html\'" ></div>')($scope));
            }
     	};
     	
     	mPMRF.fnTabEventoPosicion = function () {
            console.log('PanelEventoPosicion click');
            if (mPMRF.TabEventoPosicion !== true) {
            	mPMRF.TabEventoPosicion = true;
            	mPMRF.domVentana.find('#PanelEventoPosicion').append($compile('<div ng-include="\'../../Mesa/UI/MantenedoresPosicionMRF/PanelEventoPosicion.html\'" ></div>')($scope));
            }
     	};
     	
     	mPMRF.fnTabMetodoEventoPosicion = function () {
            console.log('PanelMetodoEventoPosicion click');
            if (mPMRF.TabMetodoEventoPosicion !== true) {
            	mPMRF.TabMetodoEventoPosicion = true;
            	mPMRF.domVentana.find('#PanelMetodoEventoPosicion').append($compile('<div ng-include="\'../../Mesa/UI/MantenedoresPosicionMRF/PanelMetodoEventoPosicion.html\'" ></div>')($scope));
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