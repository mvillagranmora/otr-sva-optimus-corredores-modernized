angular.module('ModuloCustodia').controller('CertificadoCustodiaCtrl', 
		function($scope,bcsComunes,$translate,$resource,$http,bcsServiciosNormativa,bcsConfiguracionNormativa,$compile){
	
	
	var pLCP = this;
	pLCP.TipoArchivo			= 'pdf';
	pLCP.IdTipoDocumentoCarta='CIRCCarta';
	pLCP.IdTipoDocumentoCartaConsolidada='CIRCCartaC';
	pLCP.IdVersionEsquemaCarta 	= '';
	pLCP.modulo='CUS';
	pLCP.ConfiguracionCircularizacionCustodia = 'P';
	$scope.dataTemplate	= {};	
	$scope.data = {};	
	$scope.config = {};

	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data.fechahasta 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.configuracionVerCarta = true;
	$scope.data.cartaConfirmacionCliente = false;
	$scope.data.configVerCarta = {P: false, UP: false, CP: false};

	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			EstadoCliente : {field: "EstadoCliente", operator: "eq", value: 'ACT' }
		},
	};

	var cerCif = this;
	cerCif.idVentana = bcsComunes.ventanaWinId($scope); 
	cerCif.params = bcsComunes.ventanaParams(cerCif.idVentana);
	cerCif.domVentana = $("[ventanaid='"+cerCif.idVentana+"']");
	bcsComunes.ventanaAutoOverlay(cerCif.idVentana,false,3000);
	cerCif.panelTitulo=$translate.instant('UI_PARAMETROS');
	
	cerCif.separadorListas = ';';
	cerCif.simboloDecimal = ',';
	cerCif.simboloFecha = 'dd/MM/yyyy';
	cerCif.reemplazar = 'false';
	cerCif.grabar = 'false';
	cerCif.aceptarFechasNull = 'true';
	cerCif.modulo = 'NOR';
	
	$scope.limpiarPantalla = function() {
		//Formulario
		$scope.filtros.IdPortafolio = false;
	};
	
	bcsConfiguracionNormativa.CONFIGURACION.promise.then(function(result){
		if (bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'] !== undefined){
			$scope.data.ConfiguracionCircularizacionCustodia = bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'];
			$scope.data.configVerCarta[$scope.data.ConfiguracionCircularizacionCustodia] = true;
			pLCP.ConfiguracionCircularizacionCustodia = $scope.data.ConfiguracionCircularizacionCustodia;
		}
		else
			$scope.data.ConfiguracionCircularizacionCustodia = 'P';
	});
	
	pLCP.fnVersionEsquemaCarta = function() {
			if ($scope.data.fechahasta !=undefined)
				{
						var fd = util.dateSPdate($scope.data.fechahasta);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.ConfiguracionCircularizacionCustodia != 'CP' ? pLCP.IdTipoDocumentoCarta : pLCP.IdTipoDocumentoCartaConsolidada;
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
			"parameters"	: [],
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
	 * FUNCIONES DE CARGA DE TABS
	 ********************************************/
     	
		// CARTAS
		cerCif.fnTabCartas = function () {
            if (cerCif.TabCartasCargado !== true) {
            	cerCif.TabCartasCargado = true;
            	cerCif.domVentana.find('#PanelCartasCertif').append($compile('<div ng-include="\'../../Custodia/UI/CertificadoCustodia/Paneles/PanelCartasCertif.html\'" ></div>')($scope));
            }
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
			if($scope.data.oClientePortafolio)
			{
					
					
								// Obtener  version del tipo documento
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.ConfiguracionCircularizacionCustodia == 'CP' ? pLCP.IdTipoDocumentoCartaConsolidada : pLCP.IdTipoDocumentoCarta},
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
												"p_IdCliente"				: $scope.data.oClientePortafolio.seleccionado.IdCliente,
												"p_IdPortafolio"			: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
												"p_FechaConsulta"			: util.dateSPdate($scope.data.fechahasta),
												"p_FormaSalida"				: "CARTA",
												"p_UnificarPortafolios"		: pLCP.ConfiguracionCircularizacionCustodia === 'UP' ?'S':'N',
												"p_CartaConfirmacionCliente": $scope.data.cartaConfirmacionCliente ? 'S' : 'N',
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }, { "field" : "FirmaCorredora", "type" : "blob" }]
  											  };
							//'CIRRESUMEN.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.ConsultaArchivoNormativa.ds._data[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							//PARAMETERS
							$scope.dataTemplate.parameters= [{  "type"   :	"pdf417"
  													 		 , "name"   :	"pdf417"
  													 		 , "data"   :	$scope.data.oClientePortafolio.seleccionado.NombreCompleto.normalize("NFD").replace(/[^a-zA-Z0-9-\s]/g, '')
															}];
							
							
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							pLCP.getReport($scope.dataTemplate);
							
										
						}
					});
					
				
			};
				
		};
	
		$scope.$watch('data.fechahasta', function(newval,oldval){
			pLCP.fnVersionEsquemaCarta();
		},true);
				
});
