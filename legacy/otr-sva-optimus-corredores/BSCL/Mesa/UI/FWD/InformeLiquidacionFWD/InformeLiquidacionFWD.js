angular.module('ModuloMesa').controller('InformeLiquidacionFWDCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsAlerta,OPCIONES_BCSALERTA)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.dataTemplate2= {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var fechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	//$scope.NombreArchivo = "ConsultaOperacionesMesaRF_"+$scope.SesionFechaSistema;
	//$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.SesionIdUsuario = $scope.SesionIdUsuario;
	//$scope.data.AllPortafolio= false;
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	
	
	$scope.data.FechaOperacionOpt = {
			max:fechaOperacion
	};
	$scope.data.kngFechaOperacion = fechaOperacion;
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------

	
	
	// --------------------------------------------------
	// TEMPLATES REPORTES
	// --------------------------------------------------
	var ConsultaTemplate = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdTemplateMesa	: {type: "string"},
						IdReporte		: {type: "string"},
						NombreReporte	: {type: "string"},
						TipoReporte		: {type: "string"},
						ServicioReporte	: {type: "string"},
						IdEmpresa		: {type: "number"},
						IdEstado		: {type: "string"},
						
						
					}
				}
			}
		},
	});

	$scope.getReport = function(dataTemplate){
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
					"subReports": dataTemplate.subReports || []
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
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					conflictos:util.parseServerResponse(response)
				},$scope.winID);
			});
	};
	

$scope.data.InformeLiquidacionFWD = function(){
		
		var subReports = [];
		
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'FWDINFORMELIQUIDACION' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		
		

		var ServiceDataReporte= {
				"p_IdEmpresa"	: $scope.SesionIdEmpresa,
				"p_IdModulo"	: 'FWD',
				"p_FechaConsulta"		: $scope.data.kngFechaOperacion,	
				"p_IdUsuario"	: $scope.data.SesionIdUsuario,
				"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]

				
	       };	
		
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateMesa === 'FWDINFORMELIQUIDACION'){
					
						template = {
									TipoReporte		: data[i].TipoReporte//'pdf' 
									,IdReporte		: data[i].IdReporte
									,NombreReporte	: $translate.instant(data[i].NombreReporte)
									,data			: { "serviceId" 	: data[i].ServicioReporte,
														"serviceData" 	:ServiceDataReporte
														}
														,idReporte		: 'CAR_PRO'
												};
					
				}
				else {
					//
					  subReports.push({ "reportName" 	: data[i].IdTemplateMesaSubreporte//'InformeCarteraPropiaGarantiaMesaRF_Detalle'
							              		,"parameters" 	: {} 
							              		,"data" 		: {
												                  "serviceId" : data[i].ServicioReporte,//'1759',
												                  "serviceData" :ServiceDataReporte
												              }
										});
				}			
			}
			
			
		template.subReports = subReports;
		console.log(template);
		$scope.getReport(template);
		
		
			
		});
	};	

});