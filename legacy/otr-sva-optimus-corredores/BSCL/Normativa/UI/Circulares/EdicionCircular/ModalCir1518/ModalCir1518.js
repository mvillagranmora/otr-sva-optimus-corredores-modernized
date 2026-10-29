/********************************************************************
 * Purpose	: Controlador para administrar 
 * Author	: CFLORES - BCS
 * Date		: 10/04/2017
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('ModalCir1518Ctrl',
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
		
		if(solCir.params.PrevVentana !==undefined)
		{
			solCir.PrevVentana 	 = solCir.params.PrevVentana;
		};
		
	
		bcsComunes.ventanaTitulo(solCir.idVentana, $translate.instant('UI_CIR1518_CUADRATURA_SEMANAL_DE_CUSTODIA'));
		//solCir.panelTitulo=$translate.instant('UI_PARAMETROS');
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
		// CARTAS
     	solCir.fnTabEdicion = function () {
            console.log('PanelEdicion click');
            if (solCir.TabEdicion !== true) {
            	solCir.TabEdicion = true;
            	solCir.domVentana.find('#PanelEdicion').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/EdicionCircular/ModalCir1518/Paneles/PanelEdicion.html\'" ></div>')($scope));
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