angular.module('ModuloNormativa').controller('ReporteSaldoCuentasCambioCtrl',
function($scope,$translate,bcsComunes,$http,bcsConfiguracionMaestroRMO,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout,Model,ModelService){
	Model = ModelService.getModel();
	const idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	$scope.chkFormatoTXT = true;
	$scope.generarHabilitado = false;
	
    let conf = bcsConfiguracionMaestroRMO.getConsultaReporteSaldoCuentasCambio();
	
	$scope.defaultOptions = conf.options;
	$scope.buscarReporteSaldoCuentasCambio = conf.buscar;
 
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
		$scope.gridReporteSaldos = grid;
	}
	
	$scope.defaultOptions.dataBound = function(e) {
		var dataItems = e.sender.dataSource.data();
		var recordCount = dataItems.length;

		$scope.generarHabilitado = recordCount > 0 ? true: false;
	}
  	
  	$scope.ajustaVentana = function(){
		 bcsComunes.ventanaTamanoAutomatico(idVentana,40);
    }
    
	$scope.procesarPeriodo = function() {
		let t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_PROCESAR_REPORTE_SALDOS"), idVentana);
		t.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				let parametrosSP = {};

				parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
				parametrosSP.p_IdUsuario = $scope.SesionIdUsuario;
				parametrosSP.p_Periodo = versionSAL.value;

				bcsComunes.ventanaOverlay(idVentana, true);

				bcsServiciosNormativa.ProcesarReporteSaldoCuentasCambio(parametrosSP).$promise.then(function(respuesta) {
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));

					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.msg("info", "Informacion", "Reporte de saldos procesado correctamente.");
					}
					else {
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant("Existen errores al procesar reporte de saldos."),
							conflictos: util.parseServerResponse(respuesta)
						}, idVentana);
					}
					
					bcsComunes.ventanaOverlay(idVentana, false);
					$scope.buscarReporteSaldoCuentasCambio($scope.apiGridReporteSaldoCuentasCambio,$scope.filtros);

				}, function(respuesta) {
					$scope.buscarReporteSaldoCuentasCambio($scope.apiGridReporteSaldoCuentasCambio,$scope.filtros);
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.msg("error", "Error", "Existen errores al procesar reporte de saldos.");
				});

			};
		});
	}
	
	$scope.generarArchivo = function() {
		if($scope.gridReporteSaldos.dataSource.data().length === 0){
			bcsComunes.msg('error','Generar Reporte' ,'No hay datos para generar');
            bcsComunes.ventanaOverlay(idVentana, false);
		} else {
			let formatoArchivo = $scope.chkFormatoTXT ? 'TXT' : 'CSV';

			bcsComunes.ventanaOverlay(idVentana, true);
			let params = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: Model.getModulo(),

				p_IdAgenda: 0,
				p_IdTipoTarea: 'GENERAR_ARCHIVO_RSC',
				p_Segundos: 0,
				p_CreaParam: 'S',

				p_TablaParametros: [{
					NombreParametro: 'IdEmpresa',
					Valor: $scope.SesionIdEmpresa
				}, {
					NombreParametro: 'IdUsuario',
					Valor: $scope.SesionIdUsuario
				}, {
					NombreParametro: 'IdModulo',
					Valor: Model.getModulo()
				}, {
					NombreParametro: 'Periodo',
					Valor: versionSAL.value
				}, {
					NombreParametro: 'FormatoArchivo',
					Valor: formatoArchivo
				}]
			}

			bcsComunes.msg('info', 'Generacion', 'En proceso...');
			Model.agregarPrograma(params, function() {
				bcsComunes.msg('info', 'Agregar Programa', 'OK');
				let ruta = "SPT/";
				let periodo = versionSAL.value.replace("/", "");
				let nombre = 'RSC' + periodo + "." + formatoArchivo;

				$timeout(function() {
					let url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
					let data = { file: ruta + nombre };
					console.log(data);
					$http({
						method: 'POST',
						url: url,
						params: data
					}).success(function(data) {
						let file = new File([data], nombre);
						saveAs(file);
						bcsComunes.ventanaOverlay(idVentana, false);
					}).error(function(err) {
						bcsComunes.msg('error', 'Agregar Programa', err.message);
						bcsComunes.ventanaOverlay(idVentana, false);
					});
				}, 2000);

				bcsComunes.ventanaOverlay(idVentana, false);
			}, function(error) {
				bcsComunes.msg('error', 'Agregar Programa', error.message);
				bcsComunes.ventanaOverlay(idVentana, false);
			});
		}
	};
    
	let dataSourceEsquema = Model.getDataSourceEsquema();
	
	dataSourceEsquema.fetch().then(function() {
		let versiones = dataSourceEsquema.data();
		
		if (versiones.length < 1) {
			bcsComunes.msg('error', $scope.TITLE, 'No hay esquemas definidos');
			return;
		}

		datosEsquemas = [{Tipo: "RSC", Esquema: "DJ999"}];

		$('#esquemaSAL').kendoDropDownList({
			dataTextField : 'Tipo',
			dataValueField : 'Esquema',
			dataSource : datosEsquemas,
			autobind : true,
			index : 0
		});
		
		listEsquemas = $('#esquemaSAL').data('kendoDropDownList');

		$('#versionSAL').kendoDropDownList({
			dataTextField : 'Version',
			dataValueField : 'Version',
			dataSource : versiones,
			autobind : false,
			cascadeFrom : 'esquemaSAL',
			index : 0,
			change: function(e) {
    			$scope.filtros.periodo = $('#versionSAL').data('kendoDropDownList').value();
    			$scope.buscarReporteSaldoCuentasCambio($scope.apiGridReporteSaldoCuentasCambio,$scope.filtros);
  			}
		});
		listVersiones = $('#versionSAL').data('kendoDropDownList');
	});

  	/*********************************************
	* WATCHES
	*********************************************/

	$scope.$watch('chkFormatoTXT',function(newval){
		switch (newval){
			case true:  
				$scope.chkFormatoCSV = false;
				break;
			case false: 
				$scope.chkFormatoCSV = true;				
				break;
		}					
	});
		
	$scope.$watch('chkFormatoCSV',function(newval){
		switch (newval){
			case true:  
				$scope.chkFormatoTXT = false;
				break;
			case false: 
				$scope.chkFormatoTXT = true;				
				break;
		}		
	});
	
});