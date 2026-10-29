(function() {
	angular.module('ModuloNormativa').controller('PanelEdicionCircularCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var cLim = this;
		cLim.idVentana = bcsComunes.ventanaWinId($scope); 
		cLim.params = bcsComunes.ventanaParams(cLim.idVentana);
		cLim.domVentana = $("[ventanaid='"+cLim.idVentana+"']");
		cLim.cmbbx		= {};
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/

     	cLim.fnTabCir1518 = function () {
            console.log('PanelCir1518 click');
            if (cLim.TabCir1518 !== true) {
            	cLim.TabCir1518 = true;
            	cLim.domVentana.find('#PanelCir1518').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/EdicionCircular/PanelCir1518.html\'" ></div>')($scope));
            }
     	}; 
		 cLim.fnTabCirNCG499 = function () {
            console.log('PanelCirNCG499 click');
            if (cLim.TabCirNCG499 !== true) {
            	cLim.TabCirNCG499 = true;
            	cLim.domVentana.find('#PanelCirNCG499').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/EdicionCircular/PanelCirNCG499.html\'" ></div>')($scope));
            }
     	};

       	cLim.fnTabCir1720 = function () {
            console.log('PanelCir1720 click');
            if (cLim.TabCir1720 !== true) {
            	cLim.TabCir1720 = true;
            	cLim.domVentana.find('#PanelCir1720').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/EdicionCircular/PanelCir1720.html\'" ></div>')($scope));
            }
     	};  	
	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
		
/*********************************************
 * COMBOBOX
 *********************************************/
	cLim.oCircularConfiguracion = {
		tipo : "combobox-tipo-normativa-UI",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		filtros : {
						IdNormativa : { field: "IdEstado", operator: "eq", value: 'ACT' },
						UI  : { field: "UI", operator: "eq", value: 'EDICION' }
					}
	};  	

	cLim.cmbbx.TipoDocumento={
		filtros : {
			IdNormativa : { field: "IdNormativa", operator: "eq", value: '0' },
						UI  : { field: "UI", operator: "eq", value: 'EDICION' }
		}
	};		
		
					
/*********************************************
 * FUNCIONES
 ********************************************/
/*	
 $scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',cLim.fnNormativa);
 	});
	*/
	
	
	});
}());