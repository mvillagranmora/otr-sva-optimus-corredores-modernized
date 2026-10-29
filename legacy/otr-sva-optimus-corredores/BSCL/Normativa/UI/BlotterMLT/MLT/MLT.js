/********************************************************************
 * Purpose	: Controlador para Traspasos
 * Author	: CFLORES - BCS
 * Date		: 20/07/2018
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('MLTCtrl',
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
		//solTra.fechaInicioSol= util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
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
		solTra.modoPantalla=solTra.params.Regimen;
		
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
     	// ** EGRESOS
		// INGRESOS
     	solTra.fnTabPanelIngreso = function () {
            console.log('PanelIngreso click');
            if (solTra.TabPanelIngresoCargado !== true) {
            	solTra.TabPanelIngresoCargado = true;
            	solTra.domVentana.find('#PanelIngreso').append($compile('<div ng-include="\'../../Normativa/UI/BlotterMLT/MLT/Paneles/PanelIngreso.html\'" ></div>')($scope));
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
   	     	
     
     

     	
/*********************************************
 * DATASOURCES
 *********************************************/
	// --------------------------------------------------
	// IMPRIMIR REPORTE
	// --------------------------------------------------
	
/*********************************************
 * COMBOBOX
 *********************************************/
     
	solTra.cmbbx.TipoSolicitud={
		filtros : {
			//IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
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
		if (solTra.params.Regimen==undefined ||solTra.params.Regimen=='0')
		{
			
		    //solTra.cmbbx.TipoSolicitud.filtros.value='-1';
		    solTra.cmbbx.Regimen='18Q';
		}else{
			solTra.cmbbx.Regimen=solTra.params.Regimen;
			solTra.fechavalorCuotaSol=solTra.params.selec.FechaValorCuota;
			solTra.Secuencia=solTra.params.selec.IdSolicitudAporteRescate;
			solTra.FolioContrato=solTra.params.selec.FolioContrato;
		};
		
	};

	
	$scope.solTra.fnPantallaPrev();
	

	
	
	/*********************************************
	 * WATCH
	 ********************************************/

 	});



}());