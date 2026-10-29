/********************************************************************
 * Purpose	: Controlador para solicitar Circulares
 * Author	: CFLORES - BCS
 * Date		: 10/09/2016
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('CertificadosTributariosCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var solCir = this;
		solCir.idVentana = bcsComunes.ventanaWinId($scope); 
		solCir.params = bcsComunes.ventanaParams(solCir.idVentana);
		solCir.domVentana = $("[ventanaid='"+solCir.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(solCir.idVentana,false,3000);
		
		solCir.AnchoVentana=bcsComunes.getAncho(solCir.idVentana);
		
		
		solCir.fechaInicioSol= util.dateInicioDia($scope.SesionFechaSistema);
		solCir.cmbbx		= {};
		

		solCir.modulo = 'NOR';
		solCir.IdEstadoDocumento='PEN';
		solCir.esquema=solCir.params.esquema;
		solCir.version=solCir.params.version;
		
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
	
    	solCir.fnTabINFTRABCS = function() {

     			solCir.domVentana.find('#PanelINFTRABCS').append($compile('<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Certificados/Paneles/PanelCertificados.html\'" ></div>')($scope));

     	};
   	     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
	// --------------------------------------------------
	// IMPRIMIR REPORTE
	// --------------------------------------------------
	solCir.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
						VersionArchivo			: {type: "string"},
						Esquema					: {type: "string"},
						Version 				: {type: "string"},
						IdTipoDocumento 		: {type: "string"},
						Descripcion				: {type: "string"},
						NombreArchivo			: {type: "string"},
						IdArchivo				: {type: "string"},
						TipoArchivo				: {type: "string"},
						ServicioArchivo			: {type: "string"},
						IdEmpresa				: {type: "number"},
						IdEstado				: {type: "string"}						
					}
				}
			}
		},
	});		
/*********************************************
 * COMBOBOX
 *********************************************/
	solCir.cmbbx.TipoDocumento={
			filtros : {
				Esquema : { field: "Esquema", operator: "eq", value: solCir.params.esquema },
				UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'N' }
			}
		};		
	solCir.cmbbx.Normativa={
			filtros : {
				IdNormativa :  { field: "IdNormativa", operator: "eq", value: 'TRIBUTARIO' },
				UI_Emision  : { field: "PermiteSolicitar", operator: "eq", value: 'N' }
			}
		};	     	
	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	

		

	
	solCir.fnAjustaPantalla = function () {
		bcsComunes.ventanaCambiarTamano(solCir.idVentana,550,280);
		//ancho, alto
	};
	
	
	

	
	solCir.fnNormativa = function (newval, oldval) {
		solCir.IdTipoDocumento=solCir.cmbbx.IdTipoDocumento;
		solCir.fnAjustaPantalla();
		
	};
	
	
	
	
	$scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',solCir.fnNormativa);
	
	
	
	
	
 	});
	


}());