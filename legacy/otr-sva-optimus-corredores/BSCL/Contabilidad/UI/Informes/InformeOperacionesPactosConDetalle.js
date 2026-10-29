angular.module('ModuloContabilidad').controller('InformeOperacionesPactosConDetalleCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsAlerta,OPCIONES_BCSALERTA, bcsServicioPlataforma)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.data.informes = {};
    $scope.data.informes.fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.AllPortafolio= true;
	$scope.data.esTasaMercado= true;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);

	$scope.filtros = {};
	$scope.filtros.kngFechaConsulta 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngMesa ;
	
	var datos = {
		p_fechas  : [
						{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 1, pais : $scope.SesionIdPaisSistema }
					],
		p_diaHabil 			: 'S',
		p_soloCaeDiaHabil 	: 'S',
		p_idPais 			: $scope.SesionIdPaisSistema
	};
	
	// --------------------------------------------------
	// TEMPLATES REPORTES
	// --------------------------------------------------
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
	
	$scope.data.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	// --------------------------------------------------
	// INFORMACION PARA REPORTES
	// --------------------------------------------------
	
	$scope.data.generarInforme = function() {


		bcsComunes.ventanaOverlay($scope.winID, true);

		var filterTemplate = [
			{ field: "NombreReporte", operator: "eq", value: 'INFORME_OPERACIONES_PACTO_DETALLE' },
			{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			{ field: "IdEstado", operator: "eq", value: 'ACT' }
		];

		var ServiceDataReporte = {
			"p_IdEmpresa"			: $scope.SesionIdEmpresa
			,"p_IdUsuario"			: $scope.SesionIdUsuario
			,"p_IdModulo"			: 'CNT'
			,"p_Fecha"				: util.dateSPdate($scope.filtros.kngFechaConsulta)
			,"p_IdMesa"				: $scope.filtros.kngMesa.IdMesa		
			,"p_FechaSistema"		: util.dateSPdate($scope.filtros.kngFechaSistema)
			,"$schema": [{ "field": "LogoCorredora", "type": "blob" }]
		};

		ConsultaTemplate.query({ filter: filterTemplate }).then(function() {
			var data = ConsultaTemplate.data();

			if (data.length === 0) {
				bcsComunes.msg("error", "Error", "No se encontró template para el informe solicitado");
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}

			var template = {
				TipoReporte: data[0].FormatoSalida,
				IdReporte: data[0].IdReporte,
				NombreReporte: $translate.instant(data[0].NombreReporte),
				data: { 
					"serviceId": data[0].IdServicio,
					"serviceData": ServiceDataReporte 
				}
			};

			$scope.getReport(template);
		});
	};


	
$scope.getReport = function(dataTemplate) {
		var parameters = {
			"exportFormat": dataTemplate.TipoReporte,
			"reportId": dataTemplate.IdReporte,
			"parameters": {},
			"configuration": {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data": dataTemplate.data,
					"parameters": {}
				},
				"subReports": []
			}
		};

		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
			.success(function(response) {
				var file = new Blob([response], { type: 'application/pdf' });
				var nombreArchivo = dataTemplate.NombreReporte + '_' +util.dateSPdate($scope.filtros.kngFechaConsulta).replace(/-/g, '');
				saveAs(file, nombreArchivo + '.' + dataTemplate.TipoReporte);

				// Al generar PDF, llama al Excel
				excelReport();
				bcsComunes.ventanaOverlay($scope.winID, false);

			})
			.error(function() {
				bcsComunes.msg("error", 'Error', $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID, false);
			});

			var excelReport = function(){
				/** Excel */
				var parameters = {
						"exportFormat"	: 'excel',
						"reportId"		: dataTemplate.IdReporte,
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
							var file = new Blob([response], {type: 'application/xls'});
                            var nombreArchivo = dataTemplate.NombreReporte+'_'+util.dateSPdate($scope.filtros.kngFechaConsulta).replace(/-/g,'')
						   
							saveAs(file, nombreArchivo+"."+'xls');
						//	bcsComunes.ventanaOverlay(idVentana,false);
						})
						.error(function (response) {				
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
								conflictos:util.parseServerResponse(response)
							},idVentana);
						});
	
			}
	};

});
