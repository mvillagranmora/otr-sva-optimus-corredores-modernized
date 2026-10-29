angular.module('ModuloFFM').controller('MantenedorValorCuotaCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.datosCarga = [];
	$scope.habilitaBotonProcesar = false;
	$scope.habilitaBotonEditarParidad = false;
	var conf = bcsConfiguracionFFM.getConsultaValorCuota();
	$scope.defaultOptions = conf.options;
	$scope.filasSelec = {};
	$scope.isAllFunds = false;
	const monedaSistema = $scope.$root.SesionIdMonedaSistema;

	const formulaVariacionValorCuota = `<div style="display:flex; justify-content:center;">
	<p style=" margin:0;">( Valor cuota fecha carga &nbsp;</p> * <p style=" margin:0;" >&nbsp; Paridad ) &nbsp;</p> - <p style=" margin:0;" >&nbsp; Valor cuota del dia anterior fecha carga </p></div>
	<div>_____________________________________</div>
 	<p style=" margin:0;" > Valor cuota del dia anterior fecha carga</p>`

	$scope.winID = bcsComunes.ventanaWinId($scope);

	$scope.historicoCuotasFMM = function () {
		bcsComunes.ventanaAbrir("ConsultaValorCuotaHistorico", {});
	}

	$scope.toolTipOptions = {
	filter: "th:last-child",
	position: "top",
	content: function(e) {
		  return formulaVariacionValorCuota;
	  
	},
	show: function(e) {
	  this.popup.element[0].style.height = "50px";
	}
  }

	// $scope.defaultOptions.gridCreated = function (grid) {
	// 	bcsComunes.ventanaTamanoAutomatico($scope.winID, 40);
	// }
	bcsComunes.ventanaAutoHeight($scope.winID);		

	var cargarValorCuota = function (simular, allfunds) {

		bcsComunes.ventanaOverlay($scope.winID, true);
		const parametrosSP = {};
		parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;
		parametrosSP.p_IdModulo = 'FMM';
		parametrosSP.p_UsaReglasNegocio = 'N';
		parametrosSP.p_SimularOperacion = simular;
		parametrosSP.p_AllFunds = allfunds;
		parametrosSP.p_ValorCuota = $scope.datosCarga;


		if ($scope.datosCarga.length) {
			
				bcsServicioFondosMutuos.CargarValorCuota(parametrosSP).$promise.then(function (respuesta) {
				const {P_OUT_RESULTADO, P_OUT_ERRORESCONTADOR} = respuesta;
				P_OUT_RESULTADO.map((fila) => fila.VariacionValorCuota = `${fila.VariacionValorCuota}%`)

				cargarDatosEnGrilla(P_OUT_RESULTADO);
				validarDatosCarga(P_OUT_RESULTADO,P_OUT_ERRORESCONTADOR,respuesta);
				
			},
			
			function (respuesta) {
				mostrarErrorCargaDatos(respuesta)
			});
		}

		function cargarDatosEnGrilla(datos) {
			$scope.defaultOptions.dataSource.data(datos);
		}

		function mostrarErrorCargaDatos(respuesta) {
			bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR_INGRESAR_VALOR_CUOTA"), conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
		}

		function validarDatosCarga(datosCarga, erroresContador, respuesta){
			if (datosCarga[0] !== undefined && erroresContador === 0) {
				bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_INGRESAR_VALOR_CUOTA"), conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
				$scope.habilitaBotonProcesar = simular === 'S'
				$scope.habilitaBotonEditarParidad = allfunds === 'S'
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}

			mostrarErrorCargaDatos(respuesta)
		}
	}

	$scope.cargarArchivo = function () {
		bcsComunes.ventanaOverlay($scope.winID, true);
		var datosArchivo = { codigoArchivo: 'ARCHIVO_VALOR_CUOTA', formatoArchivoDefecto: '.txt' };
		var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);
		var promesa = bcsComunes.ventanaPromesa(idVentanaCargaArchivo);
		promesa.then(function (data) {
			if (!data || !data.fileData) {
				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}

			$scope.isAllFunds = false;
			$scope.datosCarga = data.fileData;

			const datos = $scope.datosCarga

			datos.forEach(fila=>fila.OBSERVACION = '')

			console.log($scope.datosCarga);
			cargarValorCuota('S', 'N');
		});
	}

	$scope.cargarArchivoAF = function () {
		bcsComunes.ventanaOverlay($scope.winID, true);
		var datosArchivo = { codigoArchivo: 'ARCHIVO_VALOR_CUOTA_ALL_FUNDS', formatoArchivoDefecto: '.txt' };
		var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);
		var promesa = bcsComunes.ventanaPromesa(idVentanaCargaArchivo);
		promesa.then(function (data) {
			if (!data || !data.fileData) {

				bcsComunes.ventanaOverlay($scope.winID, false);
				return;
			}


			$scope.isAllFunds = true;
			$scope.datosCarga = data.fileData;

			const datos = $scope.datosCarga

			datos.forEach(fila=>fila.OBSERVACION = '')

			cargarValorCuota('S', 'S');
			
		});
	}

	$scope.procesarCuotasFFMM = function () {
	
	
			const valoresGrilla = $scope.defaultOptions.dataSource.data();
		const valoresSinCambios = [];

		if($scope.isAllFunds){

				if (valoresGrilla.length > 0) {
					const validarCambioParidad = valoresGrilla.filter(fila => (fila.IdMoneda !==  monedaSistema && fila.ParidadValorCuota === 1))
		
					if (validarCambioParidad.length > 0) {
						valoresGrilla.map(fila => {
							if (fila.IdMoneda !== monedaSistema && fila.ParidadValorCuota === 1) {
								const objCarga = {
									IdInterno: fila.IdSerieFondo,
									tipo: 'ADVERTENCIA',
									codigo: 23,
									descripcion: "No se ha editado paridad del valor cuota"
								};
								valoresSinCambios.push(objCarga)
							}
						});
		
						const ventanaAdvertencia = bcsComunes.ventanaAbrir("AdvertenciaParidad", { usaNuevosMensajes: true, campoSecuencia: "IdInterno", nombreSecuencia: 'UI_SECUENCIA', datos: valoresSinCambios });
						bcsComunes.ventanaPromesa(ventanaAdvertencia)
						.then((data) => {if (data.aceptar) {cargarValorCuota('N', 'S')}})
						return;
					}
						cargarValorCuota('N', 'S');
				}
				
			}

		else{
			cargarValorCuota('N', 'N');
		}
		
	}





	$scope.editarParidadFFMM = function () {


		function isObjEmpty(obj) {
			for (var prop in obj) {
				if (obj.hasOwnProperty(prop)) return false;
			}
			return true;
		}

		function validarMonedaInstrumentos(filasSeleccionadas) {
			return filasSeleccionadas.every(fila => fila.IdMoneda !==  monedaSistema)
		}

		if (isObjEmpty($scope.filasSelec)) {
			bcsComunes.ventanaMsgErr($translate.instant("UI_ERROR_SIN_SELECCION"), $scope.winID);
			return;
		}

		if (validarMonedaInstrumentos($scope.filasSelec)) {

			const { filasSelec, datosCarga } = $scope;
			const gridData = $scope.defaultOptions.dataSource.data();
			const ISINSeleccionados = new Set(filasSelec.map(fila=>fila.ISIN));
			const allFundsArr = gridData.filter((fila) => ISINSeleccionados.has(fila.ISIN))
			

			const ventanaEditarParidad = bcsComunes.ventanaAbrir('EditarParidadValorCuota', { filasSeleccionadas: allFundsArr}, false, false, $scope.winID)
			bcsComunes.ventanaPromesa(ventanaEditarParidad)
				.then(() => {
					const gridMap = new Map();
					gridData.forEach(fila=>gridMap.set(fila.ISIN,fila.ValorCuotaCalculado))
					datosCarga.forEach(fila=>{fila.VALORCUOTA = Number(gridMap.get(fila.IDSERIEFONDO))})
					console.log(datosCarga);
				})
		}
		else {
			bcsComunes.ventanaMsgErr($translate.instant("UI_ERROR_VALIDAR_MONEDA_SISTEMA"), $scope.winID);
			return;
		}	
		
	}

	
	

	
})