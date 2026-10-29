(function() {
	angular.module('ModuloMesa').controller('ReporteOperacionesFicticias',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.LimiteFecha = {};
		
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "SPT" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
			}
		};
		
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data['$schema'] = [{ "field" : "LogoCorredora", "type" : "blob" }];
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
							CompanyId	: {type: "string"},
							Id			: {type: "string"},
							ReportName	: {type: "string"}					
						}
					}
				}
			},
		});
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.Id,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					//var fileURL = URL.createObjectURL(file);
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {				
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos:{
							errores : [ {
			    				Codigo : 'COD',
			    				Descripcion : 'Un error inesperado ha ocurrido'
			    			} ],
						}
					},idVentana);
				});
		};
		
		$scope.dataTemplate= {};
		
		$scope.generarReporteOperacionesFicticias = function() {
				var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'MSP_INFORME_OPERACIONES_NO_FACTURABLES' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= 'pdf';
					$scope.dataTemplate.Id				= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
				
					var parametros = { 	
						"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
						"P_FECHAREPORTE"	: $scope.data.kngFecha,
						"P_NEMOTECNICO"		: $scope.data.tipoInstrumento.seleccionado.Nemotecnico === undefined ? $scope.data.tipoInstrumento.seleccionado.Instrumento : $scope.data.tipoInstrumento.seleccionado.Nemotecnico,
						"P_IDUSUARIO"		: $scope.SesionIdUsuario,
						"$schema" 			: [{ "field" : "LogoCorredora", "type" : "blob" }]
					};
				
					$scope.dataTemplate.data = { "serviceId" : Number(554), "serviceData" : parametros };
				
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
	});
}());

