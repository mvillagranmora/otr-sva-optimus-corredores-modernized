(function() {
	angular.module('ModuloCustodia').controller('ReporteDiarioPrestamosAnexo14Ctrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.LimiteFecha = {};
		
		
		$scope.data.LimiteFecha.max = new Date(util.date($scope.SesionFechaSistema).toDate());
		$scope.data.kngFechaMovimiento = util.dateInicioDia($scope.SesionFechaSistema); 
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
		
		
		$scope.dataTemplate= {};
		$scope.data.generarReporte = function()
	    { 
			//--------------------
			var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'ANEXO14CUSPRESTAMOS' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte	= 'pdf';
					$scope.dataTemplate.Id			= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
				
					var parametros = { 	"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
										"P_FECHAOPERACION"	: util.dateSPdate($scope.data.kngFechaMovimiento),
										"P_IDUSUARIO"		: $scope.SesionIdUsuario,
										"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
									};
				
					$scope.dataTemplate.data = { "serviceId" : Number(3332), "serviceData" : parametros };
				
					$scope.getReport($scope.dataTemplate);
				}
			});
	    }
		
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
						conflictos:util.parseServerResponse(response)
					},idVentana);
				});
		};
	});
}());

