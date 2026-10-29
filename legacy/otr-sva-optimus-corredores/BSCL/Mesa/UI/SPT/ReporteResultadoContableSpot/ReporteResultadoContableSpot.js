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
					"subReports": dataTemplate.subReports || []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
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
				
			var parameters = {
				"exportFormat"	: 'excel',
				"reportId"		: dataTemplate.Id,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": dataTemplate.subReports || []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+'xls');
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {				
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos:util.parseServerResponse(response)
					},idVentana);
				});
		};
		
		
		$scope.generarReporteResultadoOperaciones = function(){
			if(!$scope.data.kngFecha || $scope.data.kngFecha === ''){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),"Debe ingresar valor en filtro Fecha.");
				return;
			}
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario	= $scope.$root.SesionIdUsuario;
			parametrosSP.p_Fecha		= util.dateSPdate($scope.data.kngFecha);
			parametrosSP.p_Nemotecnico	= $scope.data.tipoInstrumento.seleccionado.Instrumento.trim().toUpperCase() === 'TODOS' ? '' : $scope.data.tipoInstrumento.seleccionado.Nemotecnico;
			
			bcsServiciosMesa.ResultadoContableSpot(parametrosSP,function (result){
				if(result && result.ResultSet_0[0] && result.ResultSet_0[0].MensajeError !== ''){
					var respuesta = {};
					respuesta.P_OUT_ERRORESCONTADOR = result.ResultSet_0[0].ErroresContador;
					respuesta.P_OUT_ERRORESDETALLE = [];
					if(result.ResultSet_0[0].MensajeErrorOp) respuesta.P_OUT_ERRORESDETALLE.push({ "Descripcion": result.ResultSet_0[0].MensajeErrorOp });
					if(result.ResultSet_0[0].MensajeErrorDO) respuesta.P_OUT_ERRORESDETALLE.push({ "Descripcion": result.ResultSet_0[0].MensajeErrorDO });
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_OBTENER_REPORTE"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
				}
				else{
					$scope.dataTemplate= {};
					
					var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'MSP_INFORME_RESULTADO_OPERACIONES' } ];
					ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
						if(ConsultaTemplate.view().length > 0) {
							$scope.dataTemplate.TipoReporte	= 'pdf';
							$scope.dataTemplate.Id			= ConsultaTemplate.view()[0].Id;
							$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;

							var parametros = {
								"p_IdEmpresa"	: $scope.SesionIdEmpresa,
								"p_IdUsuario"   : $scope.SesionIdUsuario,
								"p_Fecha"		: util.dateSPdate($scope.data.kngFecha),
								"p_Nemotecnico"	: $scope.data.tipoInstrumento.seleccionado.Instrumento.trim().toUpperCase() === 'TODOS' ? '' : $scope.data.tipoInstrumento.seleccionado.Nemotecnico,
								"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
							};

							$scope.dataTemplate.data = { "serviceId" : 4193, "serviceData" : parametros };
							$scope.dataTemplate.subReports = [{
								"reportName" 	: 'Detalle'
								,"parameters" 	: {}
								,"data" : {
									 "serviceId"	: 4194
									,"serviceData"	: {
										"p_IdEmpresa"	: $scope.SesionIdEmpresa,
										"p_IdUsuario"   : $scope.SesionIdUsuario,
										"p_Fecha"		: util.dateSPdate($scope.data.kngFecha),
										"p_Nemotecnico"	: $scope.data.tipoInstrumento.seleccionado.Instrumento.trim().toUpperCase() === 'TODOS' ? '' : $scope.data.tipoInstrumento.seleccionado.Nemotecnico,
									}
								}
							}];

							$scope.getReport($scope.dataTemplate);
						}
					});
				}
				
			}, function(resError){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(idVentana,false);
			});
			
		};
	});
}());

