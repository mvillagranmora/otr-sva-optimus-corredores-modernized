(function() {
	angular.module('ModuloMesa').controller('ReporteCalceOperaciones',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.LimiteFecha = {};
		//$scope.data.LimiteFecha.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
		
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
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
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
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.Id,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
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
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.ventanaMsg({
					      mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					      conflictos:{
					       errores : [ {
					           Codigo : 'COD',
					           Descripcion : 'Un error inesperado ha ocurrido'
					          } ],
					      }
					     },idVentana);
				});
		}; 
		
		$scope.dataTemplate= {};
		
		
		$scope.generarReporteCalce = function() {
				var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'MFW_INFORME_CALCE_OPERACIONES' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= 'pdf';
					$scope.dataTemplate.Id				= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
					
					var parametros = { 	"p_IdEmpresa"			: $scope.SesionIdEmpresa,
										"p_FechaCartera"		: $scope.data.kngFecha,
										"p_IdCliente"			: $scope.data.oClientePortafolio.seleccionado.IdCliente,
										"p_IdPortafolio"		: $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
										"p_IdTipoDeInstrumento"	: $scope.data.tipoInstrumentoSeleccionado.IdTipoInstrumento,
										"p_IdMonedaPrincipal"	: $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMonedaPrincipal,
										"p_IdMonedaSecundaria"	: $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMonedaSecundaria,
										"p_Usuario"				: $scope.SesionIdUsuario,
										"$schema"		: [{ "field" : "Logo", "type" : "blob" }]
					}; 
				
					$scope.dataTemplate.data = { "serviceId" : Number(552), "serviceData" : parametros };
				
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
	});
}());

