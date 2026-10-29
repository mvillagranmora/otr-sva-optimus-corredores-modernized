(function() {
	angular.module('ModuloContabilidad').controller('ControlFacturacionCtrl',
  function ($rootScope, $scope, $window, $resource, $http, $timeout, bcsComunes, bcsAlerta, $translate, OPCIONES_BCSALERTA) {		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.LimiteFecha = {};
		
		const fechaSistema = util.dateDS($rootScope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.kngFechaConsulta = fechaSistema;	
		

	      // ---------------------------------------------------------
        // COMBOBOX
        // ---------------------------------------------------------
        $scope.data.oConfiguracionClasificacion={
          tipo : "combobox-clasificacion-tipo-instrumento",
          configuracion : {},
          filtros : {
            IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
          },
          idActual : false,
          seleccionado : false
        };
      
        $scope.data.oConfiguracionTipoInstrumento={
            tipo : "combobox-tipo-instrumento",
            configuracion : {},
            filtros : {
              IdEstado 						: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
              IdClasificacionTipoInstrumento 	: { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
              IdTipoInstrumentoExcluir 		: { "field"   : "IdTipoInstrumento","operator": "ne", "value"   : "ORF" }
            },
            idActual : false,
            seleccionado : false,
            idDefecto : ''
        };

		$scope.data.oConfiguracionBolsa={
			filtros:{
				logic : "or",
				BCS : {"field": "IdBolsa","operator": "eq","value": "BCS"},
				BEC : {"field": "IdBolsa","operator": "eq","value": "BEC"}
			  }
		};
		
		var ConsultaTemplate = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010155",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdTemplateContabilidad: {type: "string"},
							IdReporte  : { type: "string"},
							NombreReporte : { type: "string"},
							IdServicio  : { type: "number"},
							Descripcion  : { type: "string"},
							IdEmpresa  : { type: "number"},
							IdEstado  : { type: "string"},
							ReportName  : { type: "string"},
							MasterReportId : { type: "string"},
							ReportFile  : { type: "string"},
							ReportVersion : { type: "number"},
							FormatoSalida : { type: "string"},
						}
					}
				}
			}
		});

		$scope.data.generarReporte = function () {
			bcsComunes.ventanaOverlay($scope.IdVentana, true);
			var subReportsPDF = [];
			var templatePDF = {};
			var templateExcel = {};
			var subReportsExcel = [];

			var pendientes = 2; 
			var baseParams = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_FechaOperacion: String(util.dateSPdate($scope.data.kngFechaConsulta)),
				p_AgruparPorBolsa: String($scope.data.oConfiguracionTipoControl.idActual),
				p_Bolsa: String($scope.data.oConfiguracionBolsa.idActual),
				p_Mercado: String($scope.data.oConfiguracionClasificacion.idActual),
				p_TipoInstrumento: String($scope.data.oConfiguracionTipoInstrumento.idActual),
				p_Moneda: String($scope.data.oConfiguracionMoneda.idActual),
				"$schema": [{ "field": "LogoCorredora", "type": "blob" }]
			};

			function intentarEjecutarReporte() {
			if (pendientes === 0) {
				$scope.getReport(templatePDF, templateExcel);
			}
			}

			var filterTemplatePDF = [
			{ field: "NombreReporte", operator: "eq", value: 'INFORME_CONTROL_FACTURACION_OPERACIONES' },
			{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
			{ field: "IdEstado", operator: "eq", value: 'ACT' }
			];

			var filterTemplateExcel = [
			{ field: "NombreReporte", operator: "eq", value: 'INFORME_CONTROL_FACTURACION_OPERACIONES_RESUMEN' },
			{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
			{ field: "IdEstado", operator: "eq", value: 'ACT' }
			];

			// Consulta PDF
			ConsultaTemplate.query({ filter: filterTemplatePDF }).then(function () {
			var dataPDF = ConsultaTemplate.data();

			for (let i = 0; i < dataPDF.length; i++) {
				if (dataPDF[i].IdTemplateContabilidad === 'CoFaOp') {
				templatePDF = {
					TipoReporte: dataPDF[i].FormatoSalida,
					IdReporte: dataPDF[i].IdReporte,
					IdServicio: dataPDF[i].IdServicio,
					NombreReporte: $translate.instant(dataPDF[i].NombreReporte),
					data: {
					serviceId: dataPDF[i].IdServicio,
					serviceData: baseParams
					},
					idReporte: dataPDF[i].IdReporte
				};
				} else {
				subReportsPDF.push({
					reportName: $translate.instant(dataPDF[i].NombreSubReporte),
					data: {
					serviceId: dataPDF[i].IdServicio,
					serviceData: { ...baseParams, p_Modo: 'SUB', p_TipoBloque: $translate.instant(dataPDF[i].NombreSubReporte) }
					},
					parameters: {}
				});
				}
			}

			templatePDF.subReports = subReportsPDF;
			pendientes--;
			intentarEjecutarReporte(); 
			});

			//  Consulta Excel
			ConsultaTemplate.query({ filter: filterTemplateExcel }).then(function () {
			var dataExcel = ConsultaTemplate.data();

			for (let i = 0; i < dataExcel.length; i++) {
				if (dataExcel[i].IdTemplateContabilidad === 'CoFaOR') {
				templateExcel = {
					TipoReporte: dataExcel[i].FormatoSalida,
					IdReporte: dataExcel[i].IdReporte,
					IdServicio: dataExcel[i].IdServicio,
					NombreReporte: $translate.instant(dataExcel[i].NombreReporte),
					data: {
					serviceId: dataExcel[i].IdServicio,
					serviceData: { ...baseParams, p_Modo: 'xls', p_TipoBloque: 'CUADRO_PRODUCTO' }
					},
					idReporte: dataExcel[i].IdReporte
				};
				} else {
				subReportsExcel.push({
					reportName: $translate.instant(dataExcel[i].NombreSubReporte),
					data: {
					serviceId: dataExcel[i].IdServicio,
					serviceData: { ...baseParams, p_Modo: 'xls', p_TipoBloque: $translate.instant(dataExcel[i].NombreSubReporte) }
					},
					parameters: {}
				});
				}
			}

			templateExcel.subReports = subReportsExcel;
			pendientes--;
			intentarEjecutarReporte(); 
			});
		  };
	  
		  $scope.getReport = function(dataTemplatePDF, dataTemplateXls) {
			var parametersPDF = {
			  "exportFormat": dataTemplatePDF.TipoReporte,
			  "reportId": dataTemplatePDF.idReporte,
			  "parameters": {},
			  "configuration": {
				"mainReport": {
				  "reportName": dataTemplatePDF.NombreReporte,
				  "data": dataTemplatePDF.data,
				  "parameters": {}
				},
				"subReports": dataTemplatePDF.subReports || []
			  }
			};
			var parametersXls = {
				"exportFormat": dataTemplateXls.TipoReporte,
				"reportId": dataTemplateXls.idReporte,
				"parameters": {},
				"configuration": {
				  "mainReport": {
					"reportName": dataTemplateXls.NombreReporte,
					"data": dataTemplateXls.data,
					"parameters": {}
				  },
				  "subReports": dataTemplateXls.subReports || []
				}
			  };
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parametersPDF, { "responseType": "arraybuffer" })
			  .success(function(response) {
				var file = new Blob([response], { type: 'application/pdf' });
				var nombreArchivo = dataTemplatePDF.NombreReporte + '_' + String(util.dateSPdate($scope.data.kngFechaConsulta)).replace(/-/g, '');
				saveAs(file, nombreArchivo + '.' + dataTemplatePDF.TipoReporte);
		
				// Al generar PDF, llama al Excel
				excelReport(parametersPDF,parametersXls);
				bcsComunes.ventanaOverlay($scope.winID, false);
		
			  })
			  .error(function() {
				bcsComunes.msg("error", 'Error', $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID, false);
			  });
		
			  var excelReport = function(parametersProm1, parametersProm2) {
				/** Primer Excel (PROM1) */
				parametersProm1.exportFormat = 'excel';
				$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parametersProm1, { responseType: 'arraybuffer' })
				  .success(function (response1) {
					var file1 = new Blob([response1], { type: 'application/vnd.ms-excel' });
					var nombreArchivo1 = 'INFORME_FACTURACION_DETALLE-' + String(util.dateSPdate($scope.data.kngFechaConsulta)).replace(/-/g, '');
					saveAs(file1, nombreArchivo1 + '.xls');
				  })
				  .error(function (response) {
					bcsComunes.ventanaMsg({
					  mensaje: $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					  conflictos: util.parseServerResponse(response)
					}, idVentana);
				  });
			  
				/** Segundo Excel (PROM2) */
				parametersProm2.exportFormat = 'excel';
				$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parametersProm2, { responseType: 'arraybuffer' })
				  .success(function (response2) {
					var file2 = new Blob([response2], { type: 'application/vnd.ms-excel' });
					var nombreArchivo2 = 'INFORME_FACTURACION_RESUMEN-' + String(util.dateSPdate($scope.data.kngFechaConsulta)).replace(/-/g, '');
					saveAs(file2, nombreArchivo2 + '.xls');
					bcsComunes.ventanaOverlay(idVentana, false);
				  })
				  .error(function (response) {
					bcsComunes.ventanaMsg({
					  mensaje: $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					  conflictos: util.parseServerResponse(response)
					}, idVentana);
				  });
			  };
			  
		  };
		
		
		// deberia invocar aqui el log 
		var RegistrarLog = function ()
		{	
			var datos = new Object()	
			var datosLog = [{
                IdModulo: "MRF",
				IdUsuario: $scope.data.SesionIdEmpresa,
				TipoMensaje: "INFORMACION",
				Glosa : "Informe Control Facturación Operaciones",
                Valor1: util.dateSPdate($scope.data.kngFechaHasta),
                Valor2: "InformeControlFacturacionOperaciones"                
            }];
			datos.RegistroLog = datosLog;

			bcsServicioPlataforma.RegistrarLog(datos,function(resp){
				console.log(resp);
			})
			
		}
		
	});
}());


