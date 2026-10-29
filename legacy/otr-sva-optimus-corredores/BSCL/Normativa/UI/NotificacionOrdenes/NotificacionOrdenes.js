(function() {
	angular.module('ModuloNormativa').controller('NotificacionOrdenesCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var cLim = this;
		cLim.idVentana = bcsComunes.ventanaWinId($scope); 
		cLim.params = bcsComunes.ventanaParams(cLim.idVentana);
		cLim.domVentana = $("[ventanaid='"+cLim.idVentana+"']");
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	

     	// Nomina de Clientes
     	cLim.fnTabNomina = function () {
            console.log('PanelNomina click');
            if (cLim.TabNomina !== true) {
            	cLim.TabNomina = true;
            	cLim.domVentana.find('#PanelNomina').append($compile('<div ng-include="\'../../Normativa/UI/NotificacionOrdenes/PanelNomina.html\'" ></div>')($scope));
            }
     	};
     	// Resumen de NotificacionOrdenes
     	/*
     	cLim.fnTabResumen = function () {
            console.log('PanelResumen click');
            if (cLim.TabResumen !== true) {
            	cLim.TabResumen = true;
            	cLim.domVentana.find('#PanelResumen').append($compile('<div ng-include="\'../../Normativa/UI/NotificacionOrdenes/PanelResumen.html\'" ></div>')($scope));
            }
     	};
*/
     	
	cLim.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
	};
     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
	cLim.ConsultaArchivoNormativa = new bcs.DataSource({
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
		     	
/*********************************************
 * FUNCIONES
 ********************************************/
 
	});
}());