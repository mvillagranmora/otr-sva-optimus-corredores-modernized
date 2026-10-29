(function() {
	angular.module('ModuloMesa').controller('InformeArticulo',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		
		// --------------------------------------------------
		// TEMPLATES REPORTES
		// --------------------------------------------------
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
						"reportName": dataTemplate.NombreReporte,
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
					var nombreArchivo = dataTemplate.NombreReporte + '_' + $scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo + "." + dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
		};
		
		$scope.dataTemplate= {};
		
		$scope.generarInformeArticulo84 = function() {		
			var filterTemplate = [ { field: "ReportName" ,operator: "eq", value: 'INFORMEARTICULO84' } ];
				//[ { field: "Id"		,operator: "eq", value: 'E5FD5A581CCB7F13E1000000A1EE22E7' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
				
					var parametros = { 	"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
										"P_FECHADESDE"	: util.dateSPdate($scope.data.kngFechaDesde),
										"P_FECHAHASTA"	: util.dateSPdate($scope.data.kngFechaHasta),
										"P_TIPOPRODUCTO": ($scope.data.tipoProductoSeleccionado.Nombre === ' TODOS' ? '' : $scope.data.tipoProductoSeleccionado.IdTipoProducto),
										"P_TIPOLIMITE"	: ''
									  };
					$scope.dataTemplate.TipoReporte		= 'pdf';
					$scope.dataTemplate.Id				= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.NombreReporte	= ConsultaTemplate.view()[0].ReportName;
					//$scope.dataTemplate.ServicioReporte	= 556;
					
					$scope.dataTemplate.data = { "serviceId" : Number(556), "serviceData" : parametros };
				
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
		
		
		
	});
}());

