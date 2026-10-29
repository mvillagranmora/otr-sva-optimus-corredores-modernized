(function() {
	angular.module('ModuloAPV').controller('ReporteNominasCtrl',	function($scope,
			$translate,
			$resource,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$http,
			$q,
			bcsConsultasAPV,
			bcsServiciosAPV,
			bcsAlerta,
			OPCIONES_BCSALERTA){
		
		$scope.data = {};	
		//Recupera parametros desde la ventana recibida
		var IdVentana    = bcsComunes.ventanaWinId ($scope);	
		$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
		bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

		var repNom = this;
		repNom.datosForm = {};
		repNom.fn = {};
		repNom.datasource = {};
		repNom.ConfiguracionCmbo = {};
		repNom.consulta = {};
		repNom.fechaDesde = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		repNom.fechaHasta = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		
		repNom.ConfiguracionCmbo.Estado = {
				filtros: {					
					IdEstado : {"field": "IdEstado","operator": "eq", "value" : 'ACT'}
				}		
			};
		
		repNom.ConfiguracionCmbo.Rut = {
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				DocumentoEntidad : {"field": "DocumentoEntidad","operator": "eq", "value"   :  0}
			}		
		};
		
		//COMBOBOX
		//$scope.data.oConfiguracionClasificacionTipoInstrumento={
		$scope.repNom.datosForm.documentoEntidadConf = {
				tipo : "combobox-entidad-recaudadora",
				configuracion : {},
				filtros : { 	
					IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		
		//*****************************************************************************************************************/
		//*********************************************** DataSource ******************************************************/
		//*****************************************************************************************************************/
		
		//* LOGICA PARA GENERAR EL REPORTE *//	
		var ConsultarEntidadDestinoTraspaso = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=6112", //*LLAMADA CONSULTA TEMPLATES
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
							DocumentoEntidad: {type: "string"},
						}
					}
				}
			},
		});
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015", //*LLAMADA CONSULTA TEMPLATES
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
		
		//* LOGICA PARA GENERAR LA NOMINA *//	
		repNom.fn.GeneraNomina = function(i,consultaTemplate,consultaEntidadDestino,consultar){ 
			var filtrosNomina = {
					p_IdEmpresa			 	:	$scope.SesionIdEmpresa,
					p_IdModulo			 	:	'APV',
					p_DocumentoEntidad 	 	:	repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false ? null : repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad,
					p_DocumentoIdentidad 	:	repNom.datosForm.RutActual === false ? null : repNom.datosForm.RutActual,
					p_FechaDesde		 	:	repNom.fechaDesde.toISOString().slice(0,10),
					p_FechaHasta			:	repNom.fechaHasta.toISOString().slice(0,10)
			};
			
			if(consultar){
				var Resultado = bcsServiciosAPV.NotificacionNominaTraspasoEgreso(filtrosNomina).$promise;
				Resultado.then(function(result) {
					if(result.ResultSet_0.length === 1 && result.ResultSet_0[0].RutEntidadDestino === ''){
						bcsComunes.ventanaMsgErr($translate.instant("UI_NO_SE_ENCONTRARON_DATOS"),IdVentana);
						return;
					};
					
					var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'NOTIFICACION_NOMINA_TRASPASO_EGRESO' } ];
					ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
						var filter = {};
						//if(repNom.datosForm.documentoEntidadIdActual === false) {
						if(repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false) {
							ConsultarEntidadDestinoTraspaso.query({filter: filter}).then(function(){
								if(ConsultarEntidadDestinoTraspaso.view().length > 0){
									repNom.fn.GeneraNomina(0,ConsultaTemplate.view(),ConsultarEntidadDestinoTraspaso.view(), false);
									return;
								}else{
									bcsComunes.msg("warning","warning",$translate.instant("UI_CLIENTE_SIN_EMPLEADOR"));
								}
							});
						}else{
							repNom.fn.GeneraNomina(0,ConsultaTemplate.view(),[], false); return;
						}
					});
				});
			}
			
			//if(consultaEntidadDestino.length === 0 && repNom.datosForm.documentoEntidadIdActual === false)return;
			if(consultaEntidadDestino.length === 0 && repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false)return;
			
			if(consultaTemplate.length > 0) {
				
				repNom.dataTemplate= {};
				var parametros = {
						"p_IdEmpresa"			: $scope.SesionIdEmpresa,
						"p_IdModulo"			: 'APV',
						//"p_DocumentoEntidad"	: repNom.datosForm.documentoEntidadIdActual === false ? consultaEntidadDestino[i].DocumentoEntidad : repNom.datosForm.documentoEntidadIdActual,
						"p_DocumentoEntidad"	: repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false ? consultaEntidadDestino[i].DocumentoEntidad : repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad,
						//"p_DocumentoIdentidad" 	: repNom.datosForm.documentoEntidadIdActual === false ? null : (repNom.datosForm.RutActual !== false ? repNom.datosForm.RutActual : null),
						"p_DocumentoIdentidad" 	: repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false ? null : (repNom.datosForm.RutActual !== false ? repNom.datosForm.RutActual : null),		
						"p_FechaDesde"			: repNom.fechaDesde.toISOString().slice(0,10),
						"p_FechaHasta"			: repNom.fechaHasta.toISOString().slice(0,10),
						"$schema" 				: [{ "field" : "LogoCorredora", "type" : "blob" }]
				};
				
				repNom.dataTemplate.TipoReporte	= 'pdf';
				repNom.dataTemplate.Id			= consultaTemplate[0].Id;
				//repNom.dataTemplate.ReportName	= ((repNom.datosForm.documentoEntidadIdActual === false ? consultaEntidadDestino[i].DocumentoEntidad : repNom.datosForm.documentoEntidadIdActual).replace('.','').replace('-','').padStart(10,0) + '_' + ($scope.SesionFechaSistema.slice(0,10)).replace(/-/g,'')) + '_0';
				repNom.dataTemplate.ReportName	= ((repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false ? consultaEntidadDestino[i].DocumentoEntidad : repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad).replace('.','').replace('-','').padStart(10,0) + '_' + ($scope.SesionFechaSistema.slice(0,10)).replace(/-/g,'')) + '_0';
				repNom.dataTemplate.data = { "serviceId": '6111', "serviceData": parametros };
				repNom.dataTemplate.subReports = new Array();

				var parameters = {
					"exportFormat"	: repNom.dataTemplate.TipoReporte,
					"reportId"		: repNom.dataTemplate.Id,
					"parameters"	: "",
					"configuration"	: {
						"mainReport": {
							"reportName": repNom.dataTemplate.ReportName,
							"data"		: repNom.dataTemplate.data,
							"parameters": {}
						},
						"subReports": repNom.dataTemplate.subReports
					}
				};
					
				$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
					.success(function (response) {
						var file = new Blob([response], {type: 'application/pdf'});
						var nombreArchivo = repNom.dataTemplate.ReportName;
						saveAs(file, nombreArchivo+"."+repNom.dataTemplate.TipoReporte);
						bcsComunes.ventanaOverlay($scope.winID,false);
						if(i < (consultaEntidadDestino.length - 1)){
							var fila = i + 1;
							//if(repNom.datosForm.documentoEntidadIdActual === false) {
							if(repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false) {
								repNom.fn.GeneraNomina(fila,consultaTemplate,consultaEntidadDestino,false);
							}
						}
					})
					.error(function (response) {				
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
							conflictos:util.parseServerResponse(response)
						},IdVentana);
						if(i < (consultaEntidadDestino.length - 1)){
							var fila = i + 1;
							//if(repNom.datosForm.documentoEntidadIdActual === false) {
							if(repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad === false) {
								repNom.fn.GeneraNomina(fila,consultaTemplate,consultaEntidadDestino,false);
							}
						}
					});
			}
		};
		
		
			
		//*FIN LOGICA REPORTE*//

		//*****************************************************************************************************************/
		//*********************************************** Funciones *******************************************************/
		//*****************************************************************************************************************/
		
		repNom.fn.Limpiar = function(){
			repNom.fechaDesde = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			repNom.fechaHasta = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			//repNom.datosForm.documentoEntidadIdActual = false;
			repNom.datosForm.documentoEntidadSeleccionado.DocumentoIdentidad = false;
			repNom.datosForm.documentoEntidadSeleccionado = false;
			repNom.datosForm.rutSeleccionado = false;
			repNom.datosForm.RutActual = false;
		}
	});

}());