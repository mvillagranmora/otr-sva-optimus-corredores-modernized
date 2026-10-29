(function() {
	angular.module('ModuloPlataforma').controller('AdministrarMargenesTasasCtrl',
	function($scope,$translate,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		
		var aMT = this;
		
		aMT.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		aMT.parametros 	= bcsComunes.ventanaParams(aMT.idVentana);
		aMT.domVentana 	= $("[ventanaid='"+aMT.idVentana+"']");
		
		aMT.cmbbx	= {};
		aMT.fn		= {};
		
		
		aMT.cmbbx.Moneda={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_MONEDA'),
			},
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		aMT.fn.tabMargenesTasasAgente = function () {
            console.log('PanelMargenesTasasAgente click');
            //aMT.IdMonedaSel = undefined;
            if (aMT.TabMargenesTasasAgente !== true) {
            	aMT.TabMargenesTasasAgente = true;
            	aMT.domVentana.find('#PanelMargenesTasasAgente').append($compile('<div ng-include="\'../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasAgente.html\'" ></div>')($scope));
            }
     	};
     	
     	aMT.fn.tabMargenesTasasCliente = function () {
            console.log('PanelMargenesTasasCliente click');
            //aMT.IdMonedaSel = undefined;
            if (aMT.TabMargenesTasasCliente !== true) {
            	aMT.TabMargenesTasasCliente = true;
            	aMT.domVentana.find('#PanelMargenesTasasCliente').append($compile('<div ng-include="\'../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasCliente.html\'" ></div>')($scope));
            }
     	};

     	aMT.fn.tabMargenesTasasCustodia = function () {
            console.log('PanelMargenesTasasCustodia click');
            //aMT.IdMonedaSel = undefined;
            if (aMT.TabMargenesTasasCustodia !== true) {
            	aMT.TabMargenesTasasCustodia = true;
            	aMT.domVentana.find('#PanelMargenesTasasCustodia').append($compile('<div ng-include="\'../../Plataforma/UI/TAS/MargenesTasas/PanelMargenesTasasCustodia.html\'" ></div>')($scope));
            }
     	};
		
     	/*
     	aMT.fn.buscar = function(){
     		aMT.IdMonedaSel = aMT.IdTipoReajuste;
     	};
     	*/
	});
}());