/********************************************************************
 * Purpose	: Controlador Circularizacion
 * Author	: CFLORES - BCS
 * Date		: 10/05/2016
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('SolicitarCircularizacionCtrl',
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
		
		solCir.separadorListas = ';';
		solCir.simboloDecimal = ',';
		solCir.simboloFecha = 'dd/MM/yyyy';
		solCir.fuenteDatos = 'INICIAL';
		solCir.reemplazar = 'false';
		solCir.grabar = 'false';
		solCir.aceptarFechasNull = 'true';
		solCir.modulo = 'NOR';
		solCir.IdEstadoDocumento='PEN';
		solCir.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
	
		if(solCir.params.PrevPeriodo !==undefined)
		{
			solCir.Periodo 	 = solCir.params.PrevPeriodo;
		}else{
			solCir.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		};
		if(solCir.params.PrevIdVersionEsquema !==undefined)
		{
			solCir.IdVersionEsquema 	 = solCir.params.PrevIdVersionEsquema;
		}else{
			solCir.IdVersionEsquema 	 =  '';
		
		};
		 
		
		bcsComunes.ventanaTitulo(solCir.idVentana, $translate.instant('UI_CIRCULARIZACION'));
		
		if (solCir.params.PrevUIModo==='CIRCULARIZACION')
		{solCir.panelTitulo=$translate.instant('UI_CIRCULARIZACION');}
		else {
			solCir.panelTitulo=$translate.instant('UI_PARAMETROS');
		};
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
		// CARTAS
     	solCir.fnTabCartas = function () {
            console.log('PanelCartas click');
            if (solCir.TabCartasCargado !== true) {
            	solCir.TabCartasCargado = true;
            	solCir.domVentana.find('#PanelCartas').append($compile('<div ng-include="\'../../Normativa/UI/Circularizacion/SolicitarCircularizacion/Paneles/PanelCartas.html\'" ></div>')($scope));
            }
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
			IdNormativa : { field: "IdNormativa", operator: "eq", value: '-1' }
		}
	};		
	
	solCir.cmbbx.Normativa={
		filtros : {
			IdNormativa : { field: "IdEstado", operator: "eq", value: 'ACT' }
		}
	};	     	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	solCir.fnPantallaPrev = function () {
		if (solCir.params.PrevNormativa==undefined ||solCir.params.PrevNormativa.idactual==0)
		{
			
		    solCir.cmbbx.TipoDocumento.filtros.value='-1';
		};
	};
		
	solCir.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.dataSource,
					"parameters": {}
				},
				"subReports": []
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
	};
	
	$scope.solCir.fnPantallaPrev();
	

	solCir.fnNormativa = function (newval, oldval) {
		if( solCir.cmbbx.IdNormativa =='NCG380')
		{
			solCir.cmbbx.TipoDocumento={
				filtros : {
					IdNormativa : { field: "IdNormativa", operator: "eq", value: '-1' }
				}
			};
			
		}else{
			solCir.cmbbx.TipoDocumento={
				filtros : {
					IdNormativa : { field: "IdNormativa", operator: "eq", value: solCir.cmbbx.IdNormativa }
				}
			};
			
		};	
	};
	
	
	
	//$scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',solCir.fnTipoDocumento);
	$scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',solCir.fnNormativa);
	
 	});
	

	
	
}());