angular.module('ModuloTesoreria').controller('GenerarCartaBancoCtrl',
		function($scope,$state,$translate,bcsComunes,bcsServiciosTesoreria,$http)		
{
	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var bancosConfigurados = [];
	var codigoDoc= $scope.parametros.TemplateCartaInstruccion;
	
	//---------------------------------
	// CONFIGURACION COMBOBOX
	//---------------------------------
	$scope.oConfiguracionEmpleadoFirma1={
			tipo          : "combobox-empleado",
			configuracion : {},
			filtros       : { IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};		

	$scope.oConfiguracionEmpleadoFirma2={
			tipo          : "combobox-empleado",
			configuracion : {},
			filtros       : { IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};	

	
		
	
	//----------------------------------------------------------------------
	// OBTENER APODERADOS DE LA CUENTA BANCO (quienes deben firman la carta) 
	//----------------------------------------------------------------------	
	var dsCuentaBanco = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {			
			serverFiltering 	: true,
			transport 			: {
									read : {
										url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1017",  // BSCL.Tesoreria.BD.Vistas::ConsultarCuentasBancarias
										dataType : "json",
									}
								},
								
			filter				: [{ field: "IdCuentaBanco", operator: "eq", value: $scope.parametros.IdCuentaBanco }],		
			schema 				: {
									model: {
										fields : {
													IdEmpleadoFirma1	: {type: "number"},
													MontoSegundaFirma	: {type: "number"},
													IdEmpleadoFirma2	: {type: "number"},											
										         }
									        }
								  }
								
		},
	});
 	
	dsCuentaBanco.fetch().then(function(){
		 if (dsCuentaBanco.data().length > 0 ) 
		 {
			 $scope.$apply(function() {				 
				 $.each(dsCuentaBanco.data(), function(key, registro){		
					$scope.oConfiguracionEmpleadoFirma1.idPostFiltro = registro.IdEmpleadoFirma1;
					$scope.oConfiguracionEmpleadoFirma2.idPostFiltro = registro.IdEmpleadoFirma2;						
					$scope.MontoSegundaFirma 						 = registro.MontoSegundaFirma;
				 });
				return true;
			 });			 
		 }
		 else
		 {
			 return false;
		 }
	});
	
	
	$scope.cancelar = function() 
	{
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
	
	//-----------------------------------------------------------
	// OBTENER CONFIGURACION DE REPORTE (Carta Banco) 
	//-----------------------------------------------------------
	var dsTemplateBanco = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {			
			serverFiltering 	: true,
			transport 			: {
									read : {
										url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",   //BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria 
										dataType : "json",
									}
								},								
			filter				: [
					          		{ field: "CodigoTemplateTesoreria", operator: "eq", value: codigoDoc  }, //Carta Instruccion Pago Banco
					          		{ field: "IdEstado",  operator: "eq", value: 'ACT' },
					          		{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
					            ],		
			schema 				: {
				model: {
					fields : {
						IdBanco       : {type: "string"},
					}
				}
			}							
		},
	});

	function fetchTemplateBanco(callback) {
		dsTemplateBanco.fetch(function() {
			if (dsTemplateBanco.data().length > 0) {
				$scope.$apply(function() {
					$.each(dsTemplateBanco.data(), function(key, registro) {
						bancosConfigurados.push(registro.IdBanco);
					});
					callback(null);
				});
			} else {
				callback(null);
			}
		}, function(error) {
			callback(error);
		});
	}

	function fetchTemplateTesoreria(filtroBanco, callback) {
		var dsTemplateTesoreria = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",
						dataType: "json",
					}
				},
				filter: [
					{ field: "CodigoTemplateTesoreria", operator: "eq", value: codigoDoc },
					{ field: "IdBanco", operator: "eq", value: filtroBanco },
					{ field: "IdEstado", operator: "eq", value: 'ACT' },
					{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				],
				schema: {
					model: {
						fields: {
							IdReporte: { type: "string" },
							IdServicio: { type: "number" },
							NombreReporte: { type: "string" },
							FormatoSalida: { type: "string" },
						}
					}
				}
			},
		});

		dsTemplateTesoreria.fetch(function() {
			if (dsTemplateTesoreria.data().length > 0) {
				$scope.$apply(function() {
					$.each(dsTemplateTesoreria.data(), function(key, registro) {
						$scope.ReporteCarta = {
							IdReporte: registro.IdReporte,
							NombreReporte: registro.NombreReporte,
							IdServicio: registro.IdServicio,
							FormatoSalida: registro.FormatoSalida
						};
					});
					callback(null);
				});
			} else {
				callback(null);
			}
		}, function(error) {
			callback(error);
		});
	}

	fetchTemplateBanco(function(error) {
		if (error) {
			console.error("Error en fetchTemplateBanco:", error);
		} else {
			var filtroBanco;
			if (bancosConfigurados.indexOf($scope.parametros.IdBanco.toString()) !== -1) {
				filtroBanco = $scope.parametros.IdBanco;
			} else {
				filtroBanco = 0;
			}
			fetchTemplateTesoreria(filtroBanco, function(error) {
				if (error) {
					console.error("Error en fetchTemplateTesoreria:", error);
				}
			});
		}
	});

	
	
	
	//-----------------------------------------------------------
	// GENERAR REPORTE (Carta de transferencias bancarias) 
	//-----------------------------------------------------------		

	$scope.generar = function()	{
		
		if ($scope.ReporteCarta == undefined)
		{						
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_REPORTE_SIN_CONFIGURACION_TESORERIA")},$scope.winID);
			return false;
		}
		
		if ($scope.ReporteCarta.IdReporte == '' || $scope.ReporteCarta.IdReporte == undefined || $scope.ReporteCarta.NombreReporte  == '' || $scope.ReporteCarta.NombreReporte  == undefined || $scope.ReporteCarta.IdServicio == '' || $scope.ReporteCarta.IdServicio == undefined || $scope.ReporteCarta.IdServicio == 0 || $scope.ReporteCarta.FormatoSalida == '' || $scope.ReporteCarta.FormatoSalida == undefined)
		{						
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_REPORTE_SIN_CONFIGURACION_TESORERIA")				
			},$scope.winID);
			return false;
		}		
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		var idNominaTransferencia  = $scope.parametros.IdNominaTransferencia || 0;
		var fecha = new Date();
				
		//Nombre de carta para descarga local
		var nombreCartaDescarga = $scope.ReporteCarta.NombreReporte + "_"+ String(fecha.getFullYear()) + String(fecha.getMonth()+1) + String(fecha.getDate()) + "_" + String(fecha.getHours()) + String(fecha.getMinutes()) + String(fecha.getSeconds()) + ".pdf"
		  
		var parameters = {
						"reportId": $scope.ReporteCarta.IdReporte,						
						"parameters": [],
						"exportFormat": $scope.ReporteCarta.FormatoSalida,
						//"debug" : true,
						
						"configuration": {
							"mainReport": {								
								"reportName": $scope.ReporteCarta.NombreReporte,
								"parameters": [],
								 "data" : {
					                  "serviceId" : $scope.ReporteCarta.IdServicio,
					                  "serviceData" : {								                      
					          			                 "P_IDEMPRESA"		        : $scope.SesionIdEmpresa,
					                                     "P_IDNOMINATRANSFERENCIA"	: idNominaTransferencia,					                                     
					                                     "P_IDEMPLEADOFIRMA1"		: $scope.oConfiguracionEmpleadoFirma1.seleccionado.IdEmpleado || 0,
					                                     "P_IDEMPLEADOFIRMA2"		: $scope.oConfiguracionEmpleadoFirma2.seleccionado.IdEmpleado || 0,
					                                     "P_MONTOSEGUNDAFIRMA"		: $scope.MontoSegundaFirma || 0,					                                     
					                                 	 "$schema"                  : [ {
																							"field" : "LogoCorredora",
																							"type" : "blob"
					                                 	 							  } ]
					                                  }
					              },
								"parameters": {},
							},
							"subReports": []
						},
					};

		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
		  .success(function (response) {
			   var file = new Blob([response], {type: 'application/pdf'});
			   saveAs(file, nombreCartaDescarga);
			   bcsComunes.ventanaOverlay($scope.winID,false);
		}).error(function (response) {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_AL_GENERAR_CARTA")
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
		});

		return;
	};
	
});

