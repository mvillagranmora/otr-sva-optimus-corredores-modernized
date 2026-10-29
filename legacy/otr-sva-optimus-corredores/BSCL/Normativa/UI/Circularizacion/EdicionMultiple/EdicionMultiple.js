/********************************************************************
 * Purpose	: Controlador para administrar 
 * Author	: CFLORES - BCS
 * Date		: 10/10/2016
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('EdicionMultipleCtrl',
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
		
	
		solCir.modulo = 'NOR';
		
	
		if(solCir.params.PrevSeleccion !==undefined)
		{
			solCir.Seleccion 	 = solCir.params.PrevSeleccion;
		
		};
	
	
		bcsComunes.ventanaTitulo(solCir.idVentana, $translate.instant('UI_EDICION_MULTIPLE'));
		//solCir.panelTitulo=$translate.instant('UI_PARAMETROS');
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
		// CARTAS
     	solCir.fnTabNomina = function () {
            console.log('PanelNomina click');
            if (solCir.TabCartasCargado !== true) {
            	solCir.TabCartasCargado = true;
            	solCir.domVentana.find('#PanelNomina').append($compile('<div ng-include="\'../../Normativa/UI/Circularizacion/EdicionMultiple/Paneles/PanelNomina.html\'" ></div>')($scope));
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