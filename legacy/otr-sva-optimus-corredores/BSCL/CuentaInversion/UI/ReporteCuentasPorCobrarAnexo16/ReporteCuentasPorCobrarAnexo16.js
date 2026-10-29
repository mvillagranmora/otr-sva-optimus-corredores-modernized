(function() {
	angular.module('ModuloCuentaInversion').controller('ReporteCuentasPorCobrarAnexo16Ctrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma,$http)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.data.LimiteFecha = {};
		
		
		$scope.data.LimiteFecha.max = new Date(util.date($scope.SesionFechaSistema).toDate());
		$scope.data.kngFechaMovimiento = util.dateInicioDia($scope.SesionFechaSistema); 

		$scope.data.generarReporte = function()
	    { 
			var entradaPrograma 				= {};					
			entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
			entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
			entradaPrograma.p_IdModulo    		= 'CTA';
			entradaPrograma.p_IdAgenda    		= 0;    
			entradaPrograma.p_IdTipoTarea   	= 'GENERAARCHIVOANEXO16';
			entradaPrograma.p_Segundos      	= 0;			
			entradaPrograma.p_CreaParam     	= 'S';
			
			entradaPrograma.p_TablaParametros =
			[
			   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
			   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
			   {NombreParametro	: 'IdModulo', 	  		Valor: 'CTA'},			   
			   {NombreParametro	: 'FechaHasta', 	  	Valor: util.dateSPdate($scope.data.kngFechaMovimiento) || '0001-01-01'}
			];
			
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
						Programa = respuesta.P_OUT_IDPROGRAMA;					
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;								
								
						bcsComunes.ventanaMsg({
							mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
						},$scope.winID);						
					}
					else
					{						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
						},$scope.winID);
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				}
			);			
	    }
		
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
						conflictos:util.parseServerResponse(response)
					},idVentana);
				});
		};

		firstHeight = $("#Win_ReporteCuentasPorCobrarAnexo16")
		firstHeight.css("height","134px");
	});
}());

