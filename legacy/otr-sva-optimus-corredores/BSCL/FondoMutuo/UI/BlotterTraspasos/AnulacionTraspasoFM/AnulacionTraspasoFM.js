/********************************************************************
 * Purpose	: Controlador para administrar 
 * Author	: CFLORES - BCS
 * Date		: 22/08/2018
 *******************************************************************/

(function() {
	angular.module('ModuloFFM').controller('AnulacionTraspasoFMCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var solCir = this;
		solCir.idVentana = bcsComunes.ventanaWinId($scope); 
		solCir.params = bcsComunes.ventanaParams(solCir.idVentana);
		solCir.domVentana = $("[ventanaid='"+solCir.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(solCir.idVentana,false,3000);
		solCir.fechaInicioSol= util.dateInicioDia($scope.SesionFechaSistema);
		solCir.cmbbx		= {};
		
			
	
		if(solCir.params.Prevfecha !==undefined)
		{
			solCir.Prevfecha 	 = solCir.params.Prevfecha;
		
		};
	
	
		bcsComunes.ventanaTitulo(solCir.idVentana, $translate.instant('UI_ANULACION'));
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/

     	solCir.fnTabNomina = function () {
            console.log('PanelNomina click');
            if (solCir.TabCartasCargado !== true) {
            	solCir.TabCartasCargado = true;
            	solCir.domVentana.find('#PanelNomina').append($compile('<div ng-include="\'../../FondoMutuo/UI/BlotterTraspasos/AnulacionTraspasoFM/Paneles/PanelPrincipal.html\'" ></div>')($scope));
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