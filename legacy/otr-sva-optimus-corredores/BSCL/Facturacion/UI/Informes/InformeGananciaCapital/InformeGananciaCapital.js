angular.module('ModuloFacturacion').controller('InformeGananciaCapitalCtrl',
	function ($scope, bcsComunes, $translate, $resource, $http,bcsServicioInterfaces,  bcsServiciosFacturacion,bcsAlerta,OPCIONES_BCSALERTA,$timeout,$modal,Utils,Model,$compile) {
	var pse = this;
	pse.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
		$scope.data = {};
		$scope.config = {};
		$scope.dataTemplate	= {};
/*
		$scope.config.agnoComercial = {
			max: new Date(),
			start: "decade",
			depth: "decade",
			format: "mmyyyy"
		};
		*/
		$scope.config.agnoComercial = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		$scope.data.agnoComercial 	 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oSocioNegocio = {
			tipo: "combobox-socio-negocio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				EstadoCliente: { field: "EstadoCliente", operator: "eq", value: 'ACT' }
			},
		};

		$scope.data.oPortafolio = {
			tipo: "combobox-portafolio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdCliente: { "field": "IdCliente", "operator": "eq", "value": 0 }
			},
		};

		$scope.generarInformeGananciaCapitalMasivo = function generarInformeGananciaCapitalMasivo() {
			bcsComunes.ventanaOverlay(pse.idVentana,true);
			if ($scope.data.agnoComercial)
				{	
			 
	    	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + "? :"  + $translate.instant("UI_SOLICITAR_INFORMES_POR_PERIODO" ) +" ",pse.idVentana);
			t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	 
								bcsComunes.ventanaOverlay(pse.idVentana, true);
								if($scope.data.agnoComercial && $scope.data.agnoComercial.getFullYear().toString() !=''){
									const datos = {
										p_IdEmpresa: $scope.SesionIdEmpresa,
										p_IdUsuario: $scope.SesionIdUsuario,
										p_IdModulo: 'FAC',
										p_TipoPrograma: [{
											IdInterno: 0,
											IdTipoPrograma: "INFORME_IGK_MASIVO",
											SegundosDesfase: 0
										}],
										p_Parametro: [{
											IdInterno: 0,
											NombreParametro: 'IdEmpresa',
											Valor: $scope.SesionIdEmpresa
										}, {
											IdInterno: 0,
											NombreParametro: 'IdUsuario',
											Valor: $scope.SesionIdUsuario
										}, {
											IdInterno: 0,
											NombreParametro: 'IdModulo',
											Valor: 'FMM'
										}, {
											IdInterno: 0,
											NombreParametro: 'FechaConsulta',
											Valor: util.dateSPdate($scope.data.agnoComercial) 
														
										}]
									}
									bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
										function (result) {
											bcsComunes.ventanaOverlay(pse.idVentana, false);
												if (result.P_OUT_ERRORESCONTADOR==0)
													{
													var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
													bcsComunes.ventanaMsg({
														mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
														conflictos: util.parseServerResponse(result)
													},pse.idVentana);
													
													}else{
														
														bcsComunes.ventanaMsg({
															mensaje:  $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO' ) + ""+  result.P_OUT_MENSAJES[0].codigo,
															conflictos: util.parseServerResponse(result)
														},pse.idVentana);
													}
						
										},
										function (result) {
											bcsComunes.ventanaOverlay(pse.idVentana, false);
											bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
									});
								}else {
									bcsComunes.ventanaOverlay(pse.idVentana, false);
									bcsComunes.msg("error", "Error", $translate.instant('UI_GENERAR_REPORTE_MASIVO_ERROR'));
								}
				       };
					});	
				}else{
					bcsComunes.ventanaOverlay(pse.idVentana, false);
					bcsComunes.msg("error","error",$translate.instant('Seleccione filtro: Periodo'));
					
				};
				
		};

		$scope.ConsultaArchivoFacturacion = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				serverSorting : true, 
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4175",
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
							NombreReporte			: {type: "string"},
							FormatoSalida			: {type: "string"},
							IdServicio				: {type: "string"},
							IdReporte				: {type: "string"},
							IdEmpresa				: {type: "number"},
							ReportVersion			: {type: "number"},
							IdEstado				: {type: "string"}		
							
						}
					}
				}
			},
		});	
		
		$scope.generarInformeGananciaCapitalUnitario = function generarInformeGananciaCapitalUnitario() {
			
		if ($scope.data.oSocioNegocio.seleccionado && $scope.data.agnoComercial)
			{
				// Obtener  version del tipo documento
				var filterTemplate = [ { field: "NombreReporte",operator: "eq", value: 'REPORTE_IGK'},
				                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
				var sortTemplate = [ {field: "ReportVersion", dir: "desc" } ];
				var vFolio=0;
				if ($scope.data.Oficio)
				{
					vFolio=$scope.data.Oficio;
				}
				var Datacliente = [ { IdCliente: $scope.data.oSocioNegocio.seleccionado.IdCliente ,Folio: vFolio}];
				
				$scope.ConsultaArchivoFacturacion.query({ filter: filterTemplate ,sort: sortTemplate } ).then(function(){
					if($scope.ConsultaArchivoFacturacion.ds._data.length > 0) {
						$scope.dataTemplate.TipoReporte		= $scope.ConsultaArchivoFacturacion.ds._data[0].FormatoSalida;
						$scope.dataTemplate.IdReporte		= $scope.ConsultaArchivoFacturacion.ds._data[0].IdReporte;
						$scope.dataTemplate.NombreReporte	= $scope.ConsultaArchivoFacturacion.ds._data[0].NombreReporte;
						//PARAMETROS
						var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
											"p_IdUsuario"				: $scope.SesionIdUsuario,
											"p_IdModulo"				: 'CUS',
											"p_FechaConsulta"			: util.dateSPdate($scope.data.agnoComercial),
											"p_Datos"					: Datacliente,
				                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
				                           
											  };
						//'CIRRESUMEN.rpt'
						//DATA 
						$scope.dataTemplate.data= {  "serviceId"   :	$scope.ConsultaArchivoFacturacion.ds._data[0].IdServicio
													   , "serviceData" :	parametros
												  };
										
						var subrep = new Array();
						$scope.dataTemplate.subReports=subrep;
						$scope.getReport($scope.dataTemplate);
						
									
					}
				});
			
			}else{
				bcsComunes.msg("error","error",$translate.instant('Seleccione filtros: Cliente/Periodo'));
				
			};
			
		};
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.IdReporte,
				"parameters"	: [],
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"data"		: dataTemplate.data,
						"parameters":  {}
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
	     		
		
		
		
		
	});
