(function() {
	angular.module('ModuloMesa').controller('ReporteCalceOperaciones',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.LimiteFecha = {};
		//$scope.data.LimiteFecha.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
		
		$scope.data.oPortafolio={
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};
		
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" },
				logic : "and",
		           IdMonedaPrincipal : { "field"   : "IdMonedaPrincipal","operator": "eq", "value" : "" },
		           IdMonedaSecundaria : { "field"   : "IdMonedaSecundaria","operator": "eq", "value" : "" }				
			}
		};
		
		//Moneda Principal
		$scope.data.oConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		//Moneda Secundaria
		$scope.data.oConfiguracionMonedaSecundaria={
			filtros : {
				IdTipoInstrumento 	: { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "FWD" },
				IdEstado 			: { "field"   : "IdEstado","operator": "eq", "value" : "ACT" }
			}
		};
		
		/*Valores por Defecto para el par de Monedas*/
		
		$scope.data.MonedaPrincipalDefecto = 'USD';
		$scope.data.MonedaSecundariaDefecto = 'CLP';
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		
		
		$scope.$watchGroup(['data.oConfiguracionMonedaPrincipal.seleccionado','data.oConfiguracionMonedaSecundaria.seleccionado'],function(newval,oldval) {   
            if($scope.data.oConfiguracionMonedaPrincipal.seleccionado === undefined)
            	return;
            
            if($scope.data.oConfiguracionMonedaSecundaria.seleccionado === undefined)
            	return;
            
			if (newval === oldval) 
				return;
            
			var IdMonedaPrincipalSel = ($scope.data.MonedaPrincipalIdActual === false ? '' : $scope.data.MonedaPrincipalIdActual);
			var IdMonedaSecundariaSel = ($scope.data.MonedaSecundariaIdActual === false ? '' : $scope.data.MonedaSecundariaIdActual);
			
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaPrincipal.value = IdMonedaPrincipalSel;
			$scope.data.oConfiguracionTipoInstrumento.filtros.IdMonedaSecundaria.value = IdMonedaSecundariaSel;
			
			if($scope.data.MonedaSecundariaIdActual === 'UF')
			{
				$scope.data.DecimalesPorInstrumento = 16
			}
			else
			{
				$scope.data.DecimalesPorInstrumento = $scope.data.oConfiguracionMonedaPrincipal.seleccionado.MonedaPrincipalDecimales;
			}
		});
		
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
							IdEstado		: {type: "string"}
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
					"subReports": []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					//var fileURL = URL.createObjectURL(file);
					var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')+'_'+$scope.data.seleccionOperacionesMesaRF[0].IdOperacion;
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
		};
		
		$scope.dataTemplate= {};
		
		$scope.generarReporteCalce = function() {
			$scope.dataTemplate.IdTemplateMesa = 'MFWINFORMECALCEOPERACIONES';
		
			var filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: $scope.dataTemplate.IdTemplateMesa },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= ConsultaTemplate.view()[0].TipoReporte;
					$scope.dataTemplate.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
					$scope.dataTemplate.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte;
					$scope.dataTemplate.ServicioReporte	= ConsultaTemplate.view()[0].ServicioReporte;
					$scope.dataTemplate.Descripcion		= ConsultaTemplate.view()[0].Descripcion;
				
					var parametros = { 	"p_FechaCartera"		: $scope.data.kngFecha,
										"p_IdCliente"			: $scope.data.oSocioNegocio.seleccionado.IdCliente,
										"p_IdPortafolio"		: $scope.data.oPortafolio.seleccionado,
										"p_IdTipoDeInstrumento"	: $scope.data.tipoInstrumentoSeleccionado.IdTipoInstrumento,
										"p_IdMonedaPrincipal"	: $scope.data.oConfiguracionMonedaPrincipal.seleccionado,
										"p_IdMonedaSecundaria"	: $scope.data.oConfiguracionMonedaSecundaria.seleccionado,
					};
				
					$scope.dataTemplate.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros };
				
					$scope.getReport($scope.dataTemplate);
				}
			});
		}
	});
}());

