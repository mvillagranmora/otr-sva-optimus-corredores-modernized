angular.module('ModuloNormativa').controller('ConsultarCertificadosCtrl', 
		function($scope,bcsComunes,$translate,$resource,$http,bcsConfiguracionNormativa,bcsServiciosNormativa){
	
	var pLCP = this;
	$scope.data = {};	
	$scope.config = {};
	$scope.config.certificados = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	pLCP.TipoArchivo			= 'pdf';
	pLCP.IdTipoDocumentoCarta='CIRCCarta';
	pLCP.IdTipoDocumentoCartaConsolidada='CIRCCartaC';
	pLCP.ConfiguracionCircularizacionCustodia = 'P';
	pLCP.IdVersionEsquemaCarta = '';
	$scope.data.fechahasta 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.configuracionVerCarta = true;
	$scope.data.configVerCarta = {P: false, UP: false, CP: false};
	
	pLCP.fnVersionEsquemaCarta = function() {
		if ($scope.data.fechahasta !=undefined){
			var fd = util.dateSPdate($scope.data.fechahasta);
			var paramServicio = {};
			paramServicio.p_TipoDocumento = pLCP.ConfiguracionCircularizacionCustodia == 'CP' ? pLCP.IdTipoDocumentoCartaConsolidada : pLCP.IdTipoDocumentoCarta;
			if (fd !== null || fd !== undefined) {
				paramServicio.p_Fecha = fd;
			}
			else 
			{
				paramServicio.p_Fecha = $scope.SesionFechaSistema;
			}
			
			bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
				if (respuesta.P_OUT_VERSIONESQUEMA)
				{
					pLCP.IdVersionEsquemaCarta=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
				}
				else
				{	
					bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
				}		
				
			}, function(error){
				bcsComunes.msg('error', $scope.TITLE, error.message);
			});	
		};	
	};
	
	bcsConfiguracionNormativa.CONFIGURACION.promise.then(function(result){
		if (bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'] !== undefined){
			$scope.data.ConfiguracionCircularizacionCustodia = bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'];
			$scope.data.configVerCarta[$scope.data.ConfiguracionCircularizacionCustodia] = true;
			pLCP.ConfiguracionCircularizacionCustodia = $scope.data.ConfiguracionCircularizacionCustodia;
			pLCP.fnVersionEsquemaCarta();
		}
		else
			$scope.data.ConfiguracionCircularizacionCustodia = 'P';
	});
	
	$scope.config.periodo= {
		 // defines the start view
		start: "decade",
		
		// defines when the calendar should return date
		depth: "decade",
		
		// display month and year in the input
		format: "yyyy"	
	};
	
	$scope.data.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			EstadoCliente : {field: "EstadoCliente", operator: "eq", value: 'ACT' }
		},
	};
	
		
	$scope.data.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
	};
		
	$scope.data.oCertificadoClienteXXX={
		tipo : "combobox-certificados-cliente",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			
		},
	};
	
	$scope.data.oCertificadoClienteYYY={
			tipo : "combobox-certificados-cliente",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				
			},
		};
	
	
	
	$scope.limpiarPantalla = function() {
		//Formulario
		$scope.config.certificados = false;
		$scope.data.oSocioNegocio.idActual = 0;
		$scope.data.periodo = undefined;
		//Certificados
		$scope.data.oCertificadoClienteXXX.idActual = 0;
		$scope.data.oCertificadoClienteYYY.idActual = 0;
		
		$scope.data.MasivoXXX=false;
		$scope.data.MasivoYYY=false;
	};
	
	$scope.buscarCliente = function(){
		$scope.config.certificados = true;
		$scope.data.oCertificadoClienteXXX.filtros.IdEmpresa = {"field": "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa };
		$scope.data.oCertificadoClienteXXX.filtros.Esquema = {"field": "Esquema","operator": "eq", "value"   : "CIRCCarta" };
		$scope.data.oCertificadoClienteXXX.filtros.IdCliente = {"field": "IdCliente","operator": "eq", "value"   : $scope.data.oSocioNegocio.seleccionado.IdCliente };
		$scope.data.oCertificadoClienteXXX.filtros.Anio = {"field": "Anio","operator": "eq", "value"   : $scope.data.periodo.getFullYear().toString() };
		
		$scope.data.oCertificadoClienteYYY.filtros.IdEmpresa = {"field": "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa };
		$scope.data.oCertificadoClienteYYY.filtros.Esquema = {"field": "Esquema","operator": "eq", "value"   : "CERTCLI" };
		$scope.data.oCertificadoClienteYYY.filtros.IdCliente = {"field": "IdCliente","operator": "eq", "value"   : $scope.data.oSocioNegocio.idActual };
		$scope.data.oCertificadoClienteYYY.filtros.Anio = {"field": "Anio","operator": "eq", "value"   : $scope.data.periodo.getFullYear().toString() };
	};
		
	$scope.generarMasivo = function(){
		var jsonMasivo = [];
		
		console.log($scope.data.MasivoXXX);
		if($scope.data.MasivoXXX && $scope.data.oCertificadoClienteXXX.seleccionado.IdCertificadoCliente)
			jsonMasivo.push({"IdCertificado": $scope.data.oCertificadoClienteXXX.seleccionado.IdCertificadoCliente});
		if($scope.data.MasivoYYY && $scope.data.oCertificadoClienteYYY.seleccionado.IdCertificadoCliente)
			jsonMasivo.push({"IdCertificado": $scope.data.oCertificadoClienteYYY.seleccionado.IdCertificadoCliente});
		
		console.log(jsonMasivo);
		if(jsonMasivo.length > 0){
			bcsComunes.ventanaOverlay(idVentana,true);
			var urlGenerarMasivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3033";
			var r = $resource("", {},
		    {
				'GenerarCertificadosMasivo': { method: "POST", url: urlGenerarMasivo}
			});
			
			var datosMasivos= {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA',
				p_DatosMasivos: jsonMasivo,
			};
			
			var post = r.GenerarCertificadosMasivo(datosMasivos);
			
			post.$promise.then(function(result){
				if(result.P_OUT_RESULT && result.P_OUT_RESULT==="OK"){
					bcsComunes.msg("info","INFO","UI_SOLICITUD_CERTIFICADO_MASIVO_CLIENTE")
				}else{
					bcsComunes.ventanaMsg({
					mensaje: $translate.instant("UI_ERROR_CERTIFICADO_MASIVO_CLIENTE"),
					conflictos:util.parseServerResponse(result)
					},idVentana);	
				}
				bcsComunes.ventanaOverlay(idVentana,false);
			},function(error){
				bcsComunes.msg("error","ERROR","UI_ERROR_AL_GENERAR_REPORTE")
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		}else{
			$scope.data.MasivoXXX=false;
			$scope.data.MasivoYYY=false;
		}
	};
	
	$scope.generarInputReportesMasivos = function(registro){
		var parameters = {"IdCertificado": registro.IdCertificadoCliente}
		return parameters;
	};

	$scope.envioFecha = function(){

			var fecha = '';
			fecha=	 $scope.data.oCertificadoClienteXXX.seleccionado.Periodo;
			fecha=fecha.substring(0,4) + '-' + fecha.substring(4,7)  + '-01';
						
			var dateFin = new Date();
			dateFin 	 =  util.dateInicioDia(fecha);
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			$scope.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		
	$scope.ConsultaArchivoNormativa = new bcs.DataSource({
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
	
	$scope.descargar = function(certificado){
		
		var data = ($scope.data["oCertificadoCliente"+certificado])? $scope.data["oCertificadoCliente"+certificado].seleccionado : undefined;
		if(data){
			$scope.envioFecha();
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.ConfiguracionCircularizacionCustodia == 'CP' ? pLCP.IdTipoDocumentoCartaConsolidada : pLCP.IdTipoDocumentoCarta},
                { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
                { field: "Version",  	  operator: "eq", value: pLCP.IdVersionEsquemaCarta },
                { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
                { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			$scope.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
				if($scope.ConsultaArchivoNormativa.ds._data.length > 0) {
					var subrep = new Array();
					bcsComunes.ventanaOverlay(idVentana,true);
					
					//PARAMETROS
					var parameters = {
						"reportId":$scope.ConsultaArchivoNormativa.ds._data[0].IdArchivo,
						"parameters": [],
						"exportFormat": $scope.ConsultaArchivoNormativa.ds._data[0].TipoArchivo,
						"debug" : true,
						"configuration": {
							"mainReport": {
								"reportName": $scope.ConsultaArchivoNormativa.ds._data[0].NombreArchivo,
								"parameters": [],
								"data": {
									"serviceId" : $scope.ConsultaArchivoNormativa.ds._data[0].ServicioArchivo,
									"serviceData" : {
										"p_IdEmpresa"				: Number($scope.SesionIdEmpresa),
										"p_IdCliente"				: $scope.data.oCertificadoClienteXXX.filtros.IdCliente.value,
										"p_IdPortafolio"			: $scope.data.oPortafolio.seleccionado.IdPortafolio,//512357,
										"p_FechaConsulta"			: util.dateSPdate($scope.PeriodoEnvFinal),//"2016-10-31",
										"p_FormaSalida"				: pLCP.ConfiguracionCircularizacionCustodia === 'P' ?'CERTIFICADO':'CARTA',
										"p_UnificarPortafolios"		: pLCP.ConfiguracionCircularizacionCustodia === 'UP' ?'S':'N',
										"p_IdUsuario"				: $scope.SesionIdUsuario,
			                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									},
									"serviceResponse":"resultSet"//"json"
								}						
							},
							"subReports": subrep
						}
					};
					
					$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"}).success(function (response) {
						   var file = new Blob([response], {type: 'application/pdf'});
						   saveAs(file, data.NombreArchivo+".pdf");
						   bcsComunes.ventanaOverlay(idVentana,false);
					}).error(function (response) {
						bcsComunes.ventanaOverlay(idVentana,false);
						response.status = 500;
						response.statusText = "INTERNAL ERROR";
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant("UI_PROBLEMA_DESCARGAR_ARCHIVO"),
							conflictos:util.parseServerResponse(response)
						},idVentana);
					});
				}
			});
		}
	};
	
});
