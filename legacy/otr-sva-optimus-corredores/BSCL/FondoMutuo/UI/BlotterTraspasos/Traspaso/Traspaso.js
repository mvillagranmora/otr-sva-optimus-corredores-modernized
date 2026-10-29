/********************************************************************
 * Purpose	: Controlador para Traspasos
 * Author	: CFLORES - BCS
 * Date		: 20/07/2018
 *******************************************************************/

(function() {
	angular.module('ModuloFFM').controller('TraspasoCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var solTra = this;
		solTra.idVentana = bcsComunes.ventanaWinId($scope); 
		solTra.params = bcsComunes.ventanaParams(solTra.idVentana);
		solTra.domVentana = $("[ventanaid='"+solTra.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(solTra.idVentana,false,3000);
		
		solTra.AnchoVentana=bcsComunes.getAncho(solTra.idVentana);
		
		solTra.modoPantalla=
		solTra.fechaInicioSol= util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		solTra.cmbbx		= {};
		
		solTra.separadorListas = ';';
		solTra.simboloDecimal = ',';
		solTra.simboloFecha = 'dd/MM/yyyy';
		solTra.fuenteDatos = 'INICIAL';
		solTra.reemplazar = 'false';
		solTra.grabar = 'false';
		solTra.aceptarFechasNull = 'true';
		solTra.modulo = 'NOR';
		solTra.IdEstadoDocumento='PEN';
		solTra.modoPantalla=solTra.params.IdTipoMovimiento;
		
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
		bcsComunes.ventanaAutoHeight(solTra.idVentana);
     	
     	// ** EGRESOS
		// INGRESOS
     	solTra.fnTabPanelIngreso = function () {
            console.log('PanelIngreso click');
            if (solTra.TabPanelIngresoCargado !== true) {
            	solTra.TabPanelIngresoCargado = true;
            	solTra.domVentana.find('#PanelIngreso').append($compile('<div ng-include="\'../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelIngreso.html\'" ></div>')($scope));
    			switch (solTra.params.Modo){
    			case "DETALLE":
    				bcsComunes.ventanaCambiarTamano(solTra.idVentana,solTra.AnchoVentana,600);
    				break;
    			default:
    				bcsComunes.ventanaCambiarTamano(solTra.idVentana,solTra.AnchoVentana,350);
    				break;
    			}
            }
     	};
   	     	
     	
     	solTra.fnTabPanelEgreso = function () {
            console.log('PanelEgreso click');
            if (solTra.TabPanelEgresoCargado !== true) {
            	solTra.TabPanelEgresoCargado = true;
            	solTra.domVentana.find('#PanelEgreso').append($compile('<div ng-include="\'../../FondoMutuo/UI/BlotterTraspasos/Traspaso/Paneles/PanelEgreso.html\'" ></div>')($scope));
            	bcsComunes.ventanaCambiarTamano(solTra.idVentana,solTra.AnchoVentana,500);
				bcsComunes.ventanaAutoHeight(solTra.idVentana);
				
            	
            }
            
     	};

     	
/*********************************************
 * DATASOURCES
 *********************************************/
	// --------------------------------------------------
	// IMPRIMIR REPORTE
	// --------------------------------------------------
	
/*********************************************
 * COMBOBOX
 *********************************************/
     
	solTra.cmbbx.TipoMovimiento={
		filtros : {
			IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
		}
	};		
	
	solTra.cmbbx.DivisionPais={
			filtros : {
				IdPais : { field: "IdPais", operator: "eq", value: $scope.SesionIdPaisSistema }
			}
		};		
			
	
	     	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	solTra.fnPantallaPrev = function () {
		if (solTra.params.IdTipoMovimiento==undefined ||solTra.params.IdTipoMovimiento=='0')
		{
			
		    solTra.cmbbx.TipoMovimiento.filtros.value='-1';
		    solTra.cmbbx.IdTipoMovimientoPost='TRAING';
		    solTra.cmbbx.IdTipoMovimientoActivo='true';
		}else{
			solTra.cmbbx.IdTipoMovimientoPost=solTra.params.IdTipoMovimiento;
			solTra.cmbbx.IdTipoMovimientoActivo='false';
		};
		
	};

	
	$scope.solTra.fnPantallaPrev();
	

	
	
	/*********************************************
	 * WATCH
	 ********************************************/

 	});



}());