angular.module('ModuloFFM').controller('NCG365TransaccionesFFMMCtrl', 
		function($scope,bcsComunes,$translate,$resource,$http,bcsServiciosNormativa){
	
	
	var pLCP = this;
	pLCP.TipoArchivo			= 'pdf';
	pLCP.IdTipoDocumentoCarta='NCG365INFO';
	pLCP.IdVersionEsquemaCarta 	= '';
	pLCP.modulo='CUS';
	$scope.dataTemplate	= {};	
	$scope.data = {};	
	$scope.config = {};

	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.fechahasta 	 = util.dateInicioDia($scope.SesionFechaSistema);

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
		
	
	
	$scope.limpiarPantalla = function() {
		//Formulario
		$scope.data.oSocioNegocio.idActual = 0;
	};
	
	pLCP.fnVersionEsquemaCarta = function() {
			if ($scope.data.fechahasta !=undefined)
				{
						var fd = util.dateSPdate($scope.data.fechahasta);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.IdTipoDocumentoCarta;
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

	
	
	pLCP.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": dataTemplate.parameters
				},
				"subReports": dataTemplate.subReports
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
	};
	
	
	
	
	
     		
	/*********************************************
	 * DATASOURCES
	 *********************************************/
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
	$scope.VerCarta = function() {
			pLCP.fnVersionEsquemaCarta();
			if($scope.data.oSocioNegocio.idActual>0)
			{
					
					
								// Obtener  version del tipo documento
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.IdTipoDocumentoCarta},
					                       { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pLCP.IdVersionEsquemaCarta },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.ConsultaArchivoNormativa.ds._data.length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.ConsultaArchivoNormativa.ds._data[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.ConsultaArchivoNormativa.ds._data[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.ConsultaArchivoNormativa.ds._data[0].NombreArchivo;
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_IdCliente"				: $scope.data.oSocioNegocio.seleccionado.IdCliente,
												"p_IdPortafolio"			: $scope.data.oPortafolio.seleccionado.IdPortafolio,
												"p_FormaSalida"			    : 'SERIES',
												"p_FechaConsulta"			: util.dateSPdate($scope.data.fechahasta),
												
					                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
					                           
  											  };
							
							//DATA 
							
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.ConsultaArchivoNormativa.ds._data[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							
							
							//SUB-REPORTE :RESCLIENTES
							var parametros1 =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												 "p_IdCliente"				: $scope.data.oSocioNegocio.seleccionado.IdCliente,
												 "p_IdPortafolio"			: $scope.data.oPortafolio.seleccionado.IdPortafolio,
												 "p_FormaSalida"			: 'REMUNERACION',
												 "p_FechaConsulta"			: util.dateSPdate($scope.data.fechahasta),
												 
					                             "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							
							
							
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							subrep.push({"reportName" : "REMUNERACION",
												                  "data" : {
													                  		"serviceId" : $scope.ConsultaArchivoNormativa.view()[0].ServicioArchivo,
													                  		"serviceData" :parametros1 
													                       }
												            });	
							
							//var subrep = new Array();
							//$scope.dataTemplate.subReports=subrep;
							
							$scope.dataTemplate.parameters= {};
							pLCP.getReport($scope.dataTemplate);
							
										
						}
					});
					
				
			};
				
		};
	
		$scope.$watch('data.fechahasta', function(newval,oldval){
			pLCP.fnVersionEsquemaCarta();
		},true);
				
});
