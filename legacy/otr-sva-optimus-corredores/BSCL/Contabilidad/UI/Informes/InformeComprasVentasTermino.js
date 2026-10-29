angular.module('ModuloContabilidad').controller('InformeComprasVentasTerminoCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var fechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.NombreArchivo = "Consulta"+$scope.SesionFechaSistema;
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.AllPortafolio= false;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.kngFechaProceso = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.data.FechaOperacionOpt = {
			max:fechaOperacion
	};
	$scope.data.kngFechaOperacion = fechaOperacion;
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionClientePortafolio={
		tipo : "combobox-cliente-mesas",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa",operator: "eq", value   :  $scope.SesionIdEmpresa },
			IdTipoProducto: { field: "IdTipoProducto",	operator: "eq", value : 'MERF'}
		},
		idDefecto : false,
	};
	// --------------------------------------------------
	// TEMPLATES REPORTES
	// --------------------------------------------------
	var ConsultaTemplate = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {	read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010155",dataType : "json",	}},
			schema 				: {
				model: {
					fields : {
						IdTemplateContabilidad: {type: "string"},
						IdReporte		: { type: "string"},
						NombreReporte	: { type: "string"},
						IdServicio		: { type: "number"},
						Descripcion		: { type: "string"},
						IdEmpresa		: { type: "number"},
						IdEstado		: { type: "string"},
						ReportName		: { type: "string"},
						MasterReportId	: { type: "string"},
						ReportFile		: { type: "string"},
						ReportVersion	: { type: "number"},
						FormatoSalida	: { type: "string"},	
					}
				}
			}
		},
	});

	$scope.data.Generar = function(){
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		var subReports = [];
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'INFORME_COMPRA_VENTAS_TERMINO' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		var ServiceDataReporte= {
	       	 	 "p_IdEmpresa"			: Number( $scope.SesionIdEmpresa)
				,"p_IdPortafolio"       : Number($scope.data.oConfiguracionClientePortafolio.seleccionado.IdPortafolio )
				,"p_IdMesaOrigen"       : Number($scope.data.oConfiguracionClientePortafolio.seleccionado.IdMesa )
				,"p_FechaBusqueda"		: util.dateSPdate($scope.data.kngFechaOperacion)
				,"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
		       };
				
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			if (ConsultaTemplate.view().length === 0) {
				bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_SE_ENCONTRARON_DATOS");
				return;
			}
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateContabilidad === 'CoVeTe'){				
						template = {
									TipoReporte		: data[i].FormatoSalida//'pdf' 
									,IdReporte		: data[i].IdReporte
									,NombreReporte	: $translate.instant(data[i].NombreReporte)//("INFORME_CARTERA_PROPIA_MRF")
									,data			: { "serviceId" 	: data[i].IdServicio,
														"serviceData" 	: ServiceDataReporte
														}
														,idReporte		: '67760130E3EB92771900DF00E2780260'
												};
				}
				else {
					  subReports.push({ "reportName" 	: data[i].NombreSubReporte//'InformeCarteraPropiaGarantiaMesaRF_Detalle'
							             ,"parameters" 	: {} 
							             ,"data" 		: {
												                  "serviceId" : data[i].ServicioReporte,
												                  "serviceData" :ServiceDataSubReporte
												              }
										});
				}	
			}			
		template.subReports = subReports;
		console.log('template',template);
		$scope.getReport(template);
		});
	};
	
	$scope.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte	,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports || []
			}
		};

		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = dataTemplate.NombreReporte+'_'+util.dateSPdate($scope.data.kngFechaOperacion).replace(/-/g,'')
				nombreArchivo = !dataTemplate.idReporte ? nombreArchivo+'_'+$scope.data.seleccionOperacionesMesaRF[0].IdOperacion:nombreArchivo;
			    saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				excelReport();
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
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
                            var nombreArchivo = dataTemplate.NombreReporte+'_'+util.dateSPdate($scope.data.kngFechaOperacion).replace(/-/g,'')
							nombreArchivo = !dataTemplate.idReporte ? nombreArchivo+'_'+$scope.data.seleccionOperacionesMesaRF[0].IdOperacion:nombreArchivo;
						   
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

	$scope.data.oConfiguracionClientePortafolio.idActual	= $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0;
	$scope.data.oConfiguracionClientePortafolio.idDefecto	= $scope.data.oConfiguracionClientePortafolio.seleccionado.IdSocioDeNegocio || 0;
});

