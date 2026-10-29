angular.module('ModuloPlataforma').factory('bcsServicioReportes',function($resource,bcsComunes,$http,$translate) {
	var servicios = {
			RSCA:{id:"ReporteSolicitudCompraAcciones"},
			RN:{id:"ReporteNuevo"}
	};
	
	function downloadReporte(serv, data, winId){
		var urlReporte = "/BSCL/Plataforma/GWJAVA/PLA/report?file";
		bcsComunes.ventanaOverlay(winId,true);
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', data, {"responseType": "arraybuffer"}).success(function (response) {
				   var file = new Blob([response], {type: 'application/pdf'});
				   saveAs(file, data.configuration.mainReport.reportName+".pdf");
				   bcsComunes.ventanaOverlay(winId,false);
			}).error(function (response) {
				bcsComunes.ventanaOverlay(winId,false);
				response.status = 500;
				response.statusText = "INTERNAL ERROR";
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant("UI_PROBLEMA_DESCARGAR_ARCHIVO"),
					conflictos:util.parseServerResponse(response)
				},winId);
			});
	};

	var obj = new Object();

	obj.abrirReporteSolicitudCompraOrdenes = function(data, winId) {
		var urlReporte = servicios.RSCA.id;
		downloadReporte(urlReporte, data, winId);
	};

	obj.descargarReportePdf417 = function(idTemplate, idServicio,nombre, pdf417,formato, servicedata, winId, debug) {
		var parameters = {
			"reportId": idTemplate,
			"parameters": [],
			"exportFormat": formato,
			"debug" : debug,
			"configuration": {
				"mainReport": {
					"reportName": nombre,
					"parameters": [
					{
					"type" : "pdf417",
					"name" : "pdf417",
					"data" :  pdf417
					}],
					"data": {
						"serviceId" : idServicio,
						"serviceData" : servicedata,
						"serviceResponse":"resultSet"
					}						
				},
			"subReports": []
			}
		};	
		downloadReporte("",parameters, winId);
	};
	
	return obj;
});
