angular.module('ModuloCustodia').controller('ConsultarCertificadoPrestamo',
		function($scope,$timeout,$modal,$state,bcsComunes, $rootScope, $translate, $http)
{	
	$scope.dataTemplate = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.certPres = {
			kngFechaDesde: util.dateInicioDia($scope.SesionFechaSistema),
			kngFechaHasta: util.dateTerminoDia($scope.SesionFechaSistema)
	};
		
	$scope.oConfiguracionCliente={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : undefined,
		seleccionado : -1,
		idDefecto : -1
	};
	
	$scope.oConfiguracionCertificados={
			tipo : "combobox-certificados-prestamos",
			configuracion :{},		
			idActual : undefined,
			seleccionado : -1,
			idDefecto : -1
	};
	
	
	$scope.Generar = function(){
		$scope.dataTemplate.TipoReporte	= $scope.oConfiguracionCertificados.seleccionado.TipoArchivo;
		$scope.dataTemplate.Id			= $scope.oConfiguracionCertificados.seleccionado.IdReporte;
		$scope.dataTemplate.ReportName	= $scope.oConfiguracionCertificados.seleccionado.ReportName;
		var parametros = 
		{ 	"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
			"P_FECHADESDE"		: util.dateSPdate($scope.certPres.kngFechaDesde),
			"P_FECHAHASTA"		: util.dateSPdate($scope.certPres.kngFechaHasta),
			"P_IDCLIENTE"		: $scope.oConfiguracionCliente.seleccionado.IdCliente,
			"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]
		};
		$scope.dataTemplate.data = { "serviceId" : Number($scope.oConfiguracionCertificados.seleccionado.ServicioArchivo), "serviceData" : parametros };
		
		$scope.getReport($scope.dataTemplate);
	};
	
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
				var nombreArchivo = $scope.dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+$scope.dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay(idVentana,false);
			})
			.error(function (response) {				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					conflictos:util.parseServerResponse(response)
				},idVentana);
			});
	};
	
	
	
});