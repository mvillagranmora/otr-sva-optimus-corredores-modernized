(function() {
	angular.module('ModuloMesa').controller('MantenedorMesasCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var mMesas = this;
		mMesas.idVentana	= bcsComunes.ventanaWinId($scope); 
		mMesas.params		= bcsComunes.ventanaParams(mMesas.idVentana);
		mMesas.domVentana	= $("[ventanaid='"+mMesas.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
		mMesas.fnTabMesas = function () {
            console.log('PanelMesas click');
            if (mMesas.TabMesas !== true) {
            	mMesas.TabMesas = true;
            	mMesas.domVentana.find('#PanelMesas').append($compile('<div ng-include="\'../../Mesa/UI/MantenedorMesas/PanelMesas.html\'" ></div>')($scope));
            }
     	};
     	
     	mMesas.fnTabOperadoresMesa = function () {
            console.log('PanelOperadoresMesa click');
            if (mMesas.TabOperadoresMesa !== true) {
            	mMesas.TabOperadoresMesa = true;
            	mMesas.domVentana.find('#PanelOperadoresMesa').append($compile('<div ng-include="\'../../Mesa/UI/MantenedorMesas/PanelOperadoresMesa.html\'" ></div>')($scope));
            }
     	};
     	
     	mMesas.fnTabFormaPagoMesa = function () {
            console.log('PanelFormaPagoMesa click');
            if (mMesas.TabFormaPagoMesa !== true) {
            	mMesas.TabFormaPagoMesa = true;
            	mMesas.domVentana.find('#PanelFormaPagoMesa').append($compile('<div ng-include="\'../../Mesa/UI/MantenedorMesas/PanelFormaPagoMesa.html\'" ></div>')($scope));
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