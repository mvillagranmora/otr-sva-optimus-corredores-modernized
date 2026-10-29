angular.module('ModuloFFM').controller('ConsultaComisionesNNFFMCtrl',
	function($scope, $translate, bcsComunes, bcsConfiguracionFFM, bcsServicioInterfaces, bcsServicioFondosMutuos, bcsAlerta, OPCIONES_BCSALERTA, bcsServicioReportes, $timeout) {
		/******************************************************/
		/*** INICIO DE VARIABLES Y PRECARGA					***/
		/******************************************************/

		$scope.formData = {};
		$scope.filtros = {};
		var conf = bcsConfiguracionFFM.getConsultaComisionesNN();
		var apiGridCustodiaFondosMutuos = conf.options.dataSource;
		$scope.defaultOptions = conf.options;
		$scope.buscarCustodia = conf.buscar;
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.winID = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var datos = {};
		datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
		datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
		datos.p_IdModulo = 'FMM';
		datos.p_UsaReglasNegocio = 'S';
		$scope.seleccionarTodoNN = true;
		$scope.habilitadoBotonBuscar = false;

		$scope.data = {};
		$scope.data.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaValor = util.dateInicioDia($scope.SesionFechaSistema);
		

		$scope.data.oClientePortafolio = {
			tipo: "combobox-cliente-portafolio",
			configuracion: {},
			filtros: {
				IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
				EsCliente: { "field": "EsCliente", "operator": "eq", "value": 'UI_SI' }
			},
			idActual: false,
			seleccionado: false,
			idDefecto: '',
		};

		$scope.data.oComboTrimestres = {
			tipo: "combobox-trimestres",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			idDefecto: '',
		};

		$scope.data.oComboAnios = {
			tipo: "combobox-anios-comisiones-ffmm",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			idDefecto: '',
		};

		$scope.$watch('data.oComboTrimestres.seleccionado', function(newval, oldval) {
			if (newval !== oldval && newval !== false) {
				if (newval.Codigo == 1) {
					$scope.data.kngFechaDesde = $scope.data.oComboAnios.seleccionado.Codigo + "-01-01";
					$scope.data.kngFechaHasta = $scope.data.oComboAnios.seleccionado.Codigo + "-03-31";
				} else if (newval.Codigo == 2) {
					$scope.data.kngFechaDesde = $scope.data.oComboAnios.seleccionado.Codigo + "-04-01";
					$scope.data.kngFechaHasta = $scope.data.oComboAnios.seleccionado.Codigo + "-06-30";
				} else if (newval.Codigo == 3) {
					$scope.data.kngFechaDesde = $scope.data.oComboAnios.seleccionado.Codigo + "-07-01";
					$scope.data.kngFechaHasta = $scope.data.oComboAnios.seleccionado.Codigo + "-09-30";
				} else if (newval.Codigo == 4) {
					$scope.data.kngFechaDesde = $scope.data.oComboAnios.seleccionado.Codigo + "-10-01";
					$scope.data.kngFechaHasta = $scope.data.oComboAnios.seleccionado.Codigo + "-12-31";
				}
				if($scope.data.oComboAnios.seleccionado !== false){
					$scope.habilitadoBotonBuscar = true;				
				}else{
					$scope.habilitadoBotonBuscar = false;
				}
			}
			if(newval == false){
				$scope.habilitadoBotonBuscar = false;
				$scope.resetearFechas();
			}
				
		});

		$scope.limpiarFiltros = function() {
			$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.filtros.IdPortafolio = false;

		};

		$scope.ajustaVentana = function() {
			bcsComunes.ventanaTamanoAutomatico($scope.winID, 40);
		};

		$scope.defaultOptions.gridCreated = function(grid) {
			$scope.ajustaVentana();
		};

		$scope.buscar = function() {
			if($scope.data.oComboTrimestres.idActual !== false && $scope.data.oComboTrimestres.idActual > 0 && $scope.data.oComboAnios.idActual !== false){
				apiGridCustodiaFondosMutuos.setCallParameters($scope.preparaParametros($scope.data));
				apiGridCustodiaFondosMutuos.read();
			}else{
				bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("UI_FILTROS_ANIO_Y_TRIMESTRE_REQUERIDOS"));
			}
		};

		$scope.formData.procesar = function(solicitudesTemp) {
			if (!$scope.seleccionarTodoNN && (!solicitudesTemp || solicitudesTemp.length == 0))
			{
				bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR_GENERA_RESCATES_COMISIONES")}, $scope.winID);
			}
			
			else
			{

				bcsComunes.ventanaOverlay($scope.winID, true);
				var datosComisiones = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'FMM',
				p_FechaConsultaDesde: $scope.data.kngFechaDesde,
				p_FechaConsultaHasta: $scope.data.kngFechaHasta,
				p_Solicitudes : []
				}
			
				if(!$scope.seleccionarTodoNN && solicitudesTemp && solicitudesTemp.length > 0) // Check no encendido
				{
					$.each(solicitudesTemp,function(key,sol){
	  					datosComisiones.p_Solicitudes.push({IdCartera: sol.IdCartera});
	  				});
			
					bcsServicioFondosMutuos.ProcesarComisionesNN(datosComisiones).$promise.then(function(respuesta) {
						if(respuesta.P_OUT_MENSAJES.length > 0){
							bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'RESULTADOS', campoSecuencia : "id" ,nombreSecuencia : 'UI_FOLIO_CARTERA',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.winID);							
						}
						
						if(respuesta.P_OUT_RESULTADO.length > 0){
							var id = bcsComunes.ventanaAbrir("ConfirmarProcesarNNFFM", { datosComisiones: datosComisiones, respuesta: respuesta, masivo: false }, false, false, $scope.winID);
						}
						
						$solicitudesTemp = undefined;
						buscarLuegoDeCerrarVentana(id);
					});
					
				}else{
					$.each($scope.apiGridCustodiaFondosMutuos.dataSource.data(),function(key,sol){
	  					datosComisiones.p_Solicitudes.push({IdCartera: sol.IdCartera});
	  				});
					
					let listaComisiones = JSON.stringify(datosComisiones.p_Solicitudes.map(car => car.IdCartera).join(','));
					const datos = {
						p_IdEmpresa: $scope.SesionIdEmpresa,
						p_IdUsuario: $scope.SesionIdUsuario,
						p_IdModulo: 'FFM',
						p_TipoPrograma: [{
							IdInterno: 0,
							IdTipoPrograma: "ComisionesNN",
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
							},{
								IdInterno: 0,
								NombreParametro: 'EsMasivo',
								Valor: 'S'
							},{
								IdInterno: 0,
								NombreParametro: 'FechaDesde',
								Valor: datosComisiones.p_FechaConsultaDesde
							},{
								IdInterno: 0,
								NombreParametro: 'FechaHasta',
								Valor: datosComisiones.p_FechaConsultaHasta
							},{
								IdInterno: 0,
								NombreParametro: 'Comisiones',
								ValorBlob: listaComisiones.replace('\"', '').replace('\"','')
							}]
						}
						
					bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
					function (result) {
						bcsComunes.ventanaOverlay(idVentana, false);
						var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
							conflictos: util.parseServerResponse(result)
						}, $scope.winID);
	
					},
					function(result){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);					
					});
				
						
				}	
			}
				
			
		};

		var buscarLuegoDeCerrarVentana = function(id) {
			var promesa = bcsComunes.ventanaPromesa(id);
			$.when(promesa).then(function(data) {
				$scope.buscar();
			});
		}

		$scope.$watch('apiGridCustodiaFondosMutuos.dataSource.ds._data', function(newval, oldval) {
			if (newval !== undefined && newval.length > 0 && (newval !== oldval)) {
				$scope.data.kngFechaValor = newval[0].FechaValorCuota;
			}

			if (newval !== undefined && oldval !== undefined) {
				if ((apiGridCustodiaFondosMutuos.data().length) === 0) {
					bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("UI_NO_EXISTE_INFORMACION"));
					return;
				};
			}

		});

		$scope.preparaParametros = function(filtros) {
			parametrosConsulta = {};
			parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosConsulta.p_IdUsuario = $scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo = "FMM";
			parametrosConsulta.p_AgrupaPorBoveda = "S";
			parametrosConsulta.p_FechaConsultaDesde = $scope.data.kngFechaDesde;
			parametrosConsulta.p_FechaConsultaHasta = $scope.data.kngFechaHasta;

			parametrosConsulta.p_IdCliente = ($scope.data.oClientePortafolio.seleccionado) ? $scope.data.oClientePortafolio.seleccionado.IdCliente : 0;
			parametrosConsulta.p_IdPortafolio = ($scope.data.oClientePortafolio.seleccionado) ? $scope.data.oClientePortafolio.seleccionado.IdPortafolio : 0;

			return parametrosConsulta;
		};

		
		$scope.limpiarFiltros();

		$scope.$watch('data.oComboAnios.seleccionado', function(newval, oldval){
			if (newval !== oldval) {
				$scope.data.oComboTrimestres.idActual = false;
				$scope.resetearFechas();
			}
			if(newval == false){
				$scope.habilitadoBotonBuscar = false;
			}
		});
		
		$scope.resetearFechas = function (){
			$scope.data.kngFechaDesde = util.dateInicioDia('0001-01-01');
			$scope.data.kngFechaHasta = util.dateInicioDia('0001-01-01');
		};
		
	});