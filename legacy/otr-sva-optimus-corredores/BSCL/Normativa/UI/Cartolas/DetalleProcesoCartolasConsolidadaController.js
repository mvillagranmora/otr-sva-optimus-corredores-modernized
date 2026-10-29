angular.module('ModuloNormativa').controller(
		'DetalleProcesoCartolaConsolidadaController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Model', 'Utils',
				'bcsCartolas', DetalleProcesoCartolaConsolidadaController ]);

function DetalleProcesoCartolaConsolidadaController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Model, Utils, bcsCartolas) {

	var windowParams = Utils.getWindowParams($scope);
	var dataSourceNominas = bcsCartolas
			.getDataSourceNominas($scope.SesionIdEmpresa);
	var dataSourceParametros = bcsCartolas.getDataSourceParametros();
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.readOnly = false;
	$scope.nominaSeleccionada = null;
	$scope.Parametros = {};
	if (typeof windowParams.NominaSeleccionada !== 'undefined') {
		$scope.nominaSeleccionada = windowParams.NominaSeleccionada;
		if (typeof windowParams.ReadOnly !== 'undefined') {
			$scope.readOnly = windowParams.ReadOnly;
		}
	}

	$scope.programada = false;
	if (typeof windowParams.Programada !== 'undefined') {
		$scope.programada = windowParams.Programada;
	}

	$scope.textoGrabar = 'GUARDAR';
	if ($scope.programada) {
		inicializarProcesoProgramado();

		$scope.$watch('Nomina.OpcionProceso', function(newValue, oldValue) {
			if (newValue !== null && typeof newValue === 'string') {
				setOpcionProceso(newValue);
			}
		});
	} else if ($scope.nominaSeleccionada === null) {
		$scope.textoGrabar = 'NUEVO';
		inicializarProcesoNuevo();

		$scope.$watch('Nomina.OpcionProceso', function(newValue, oldValue) {
			if (newValue !== null && typeof newValue === 'string') {
				setOpcionProceso(newValue);
			}
		});
	} else {
		inicializarProcesoExistente();
	}

	function inicializarProcesoProgramado() {
		inicializarProcesoNuevo();

		$scope.Nomina.TipoProceso = 'PROGRAMADO';

		$scope.Parametros.DiaEjecucion = 1;
		$scope.Parametros.HoraEjecucion = '09:00';
		$scope.Parametros.Habilitado = true;
	}

	function inicializarProcesoNuevo() {
		$scope.AnoProceso = null;
		$scope.MesProceso = null;
		$scope.OpcionProcesoDisabled = false;
		$scope.FlagsDisabled = false;
		$scope.Nomina = {
			IdNomina : 0,
			IdEmpresa : $scope.SesionIdEmpresa,
			IdUsuario : $scope.SesionIdUsuario,
			Periodo : null,
			FechaDesde : null,
			FechaHasta : null,
			Comentario : null,
			TipoProceso : 'NORMAL',
			FechaProgramadaProcesoCartolaConsolidada : new Date(),
			CategoriaInversionista : null,
			ExcluirOperadoresDirectos : false,
			ExcluirPortafoliosIncluidosOtraNomina : false,
			IncluirCategoriaInversionista : false,
			IncluirPortafoliosConCustodia : false,
			IncluirPortafoliosConMovimientosCuentaInversion : false,
			IncluirPortafoliosConSaldo : false,
			IncluirPortafoliosExcluidos : false,
			IncluirTipoCliente : false,
			IncluirTipoPersona : false,
			OpcionProceso : 'NORMATIVO',
			TipoCliente : null,
			TipoPersona : null
		}
		
	}

	function inicializarProcesoExistente() {
		$scope.OpcionProcesoDisabled = true;
		$scope.FlagsDisabled = true;	
		$scope.Nomina = {
				IdNomina : $scope.nominaSeleccionada.IdNomina,
				IdEmpresa : $scope.SesionIdEmpresa,
				IdUsuario : $scope.SesionIdUsuario,
				Periodo : $scope.nominaSeleccionada.Periodo,
				Ano : $scope.nominaSeleccionada.Ano,
				Mes : $scope.nominaSeleccionada.Mes,
				FechaDesde : $scope.nominaSeleccionada.FechaDesde,
				FechaHasta : $scope.nominaSeleccionada.FechaHasta,
				Comentario : $scope.nominaSeleccionada.Comentario,
				Certificada : $scope.nominaSeleccionada.Certificada,
				FechaCertificacion : $scope.nominaSeleccionada.FechaCertificacion,
				IdUsuarioCertificador : $scope.nominaSeleccionada.IdUsuarioCertificador,
				NombreUsuarioCertificador : $scope.nominaSeleccionada.NombreUsuarioCertificador,
				NotaCertificacion : $scope.nominaSeleccionada.NotaCertificacion,
				TipoProceso : $scope.nominaSeleccionada.TipoProceso,
				EstadoProceso : $scope.nominaSeleccionada.EstadoProceso,
				FechaProgramadaProcesoCartolaConsolidada : $scope.nominaSeleccionada.FechaProgramadaProceso,
				FechaInicioProceso : $scope.nominaSeleccionada.FechaInicioProceso,
				FechaTerminoProceso : $scope.nominaSeleccionada.FechaTerminoProceso,
				MensajeProceso : $scope.nominaSeleccionada.MensajeProceso,
				CategoriaInversionista : $scope.nominaSeleccionada.CategoriaInversionista,
				ExcluirOperadoresDirectos : $scope.nominaSeleccionada.ExcluirOperadoresDirectos === 'S' ? true : false,
				ExcluirPortafoliosIncluidosOtraNomina : $scope.nominaSeleccionada.ExcluirPortafoliosIncluidosOtraNomina === 'S' ? true : false,
				IncluirCategoriaInversionista : $scope.nominaSeleccionada.IncluirCategoriaInversionista === 'S' ? true : false,
				IncluirPortafoliosConCustodia : $scope.nominaSeleccionada.IncluirPortafoliosConCustodia === 'S' ? true : false,
				IncluirPortafoliosConMovimientosCuentaInversion : $scope.nominaSeleccionada.IncluirPortafoliosConMovimientoCuentaInversion === 'S' ? true : false,
				IncluirPortafoliosConSaldo : $scope.nominaSeleccionada.IncluirPortafoliosConSaldo === 'S' ? true : false,
				IncluirPortafoliosExcluidos : $scope.nominaSeleccionada.IncluirPortafoliosExcluidos === 'S' ? true : false,
				IncluirTipoCliente : $scope.nominaSeleccionada.IncluirTipoCliente === 'S' ? true : false,
				IncluirTipoPersona : $scope.nominaSeleccionada.IncluirTipoPersona === 'S' ? true : false,
				OpcionProceso : $scope.nominaSeleccionada.OpcionProceso,
				TipoCliente : $scope.nominaSeleccionada.TipoCliente,
				TipoPersona : $scope.nominaSeleccionada.TipoPersona,
				IdEstado : $scope.nominaSeleccionada.EstadoProceso,
				MensajeProceso : $scope.nominaSeleccionada.MensajeProceso,
				NotaCertificacion: $scope.nominaSeleccionada.NotaCertificacion	
		}
		if ($scope.Nomina.Periodo !== null) {
			$scope.AnoProceso = $scope.Nomina.Ano;
			$scope.MesProceso = $scope.Nomina.Mes;
		}
	}

	function setPeriodo(newValue, oldValue) {
		$scope.Nomina.Periodo = null;
		if ($scope.AnoProceso === null || typeof $scope.AnoProceso !== 'string')
			return;
		if ($scope.MesProceso === null || typeof $scope.MesProceso !== 'string')
			return;
		$scope.Nomina.Periodo = $scope.AnoProceso + '-' + $scope.MesProceso;
	}

	$scope.$watch('AnoProceso', setPeriodo);
	$scope.$watch('MesProceso', setPeriodo);

	function setOpcionProceso(opcionProceso) {
		if (opcionProceso === 'NORMATIVO') {
			$scope.FlagsDisabled = true;
			$scope.Nomina.CategoriaInversionista = null;
			$scope.Nomina.ExcluirOperadoresDirectos = false;
			$scope.Nomina.ExcluirPortafoliosIncluidosOtraNomina = false;
			$scope.Nomina.IncluirCategoriaInversionista = false;
			$scope.Nomina.IncluirPortafoliosConCustodia = true;
			$scope.Nomina.IncluirPortafoliosConMovimientosCuentaInversion = true;
			$scope.Nomina.IncluirPortafoliosConSaldo = true;
			$scope.Nomina.IncluirPortafoliosExcluidos = false;
			$scope.Nomina.IncluirTipoCliente = false;
			$scope.Nomina.IncluirTipoPersona = false;
			$scope.Nomina.TipoCliente = null;
			$scope.Nomina.TipoPersona = null;
		} else if (opcionProceso === 'TODOS') {
			$scope.FlagsDisabled = true;
			$scope.Nomina.CategoriaInversionista = null;
			$scope.Nomina.ExcluirOperadoresDirectos = false;
			$scope.Nomina.ExcluirPortafoliosIncluidosOtraNomina = false;
			$scope.Nomina.IncluirCategoriaInversionista = false;
			$scope.Nomina.IncluirPortafoliosConCustodia = false;
			$scope.Nomina.IncluirPortafoliosConMovimientosCuentaInversion = false;
			$scope.Nomina.IncluirPortafoliosConSaldo = false;
			$scope.Nomina.IncluirPortafoliosExcluidos = false;
			$scope.Nomina.IncluirTipoCliente = false;
			$scope.Nomina.IncluirTipoPersona = false;
			$scope.Nomina.TipoCliente = null;
			$scope.Nomina.TipoPersona = null;
		} else {
			$scope.FlagsDisabled = false;
		}
	}

	$scope.grabarNomina = function() {
		var errors = [];		
		if ($scope.Nomina.TipoProceso === 'PROGRAMADO') {
			$scope.Nomina.AccionNomina = "PROGRAMADO";
			if ($scope.Parametros.DiaEjecucion < 1
					|| $scope.Parametros.DiaEjecucion > 10) {
				errors.push('Dia de Ejecucion deber ser entre 1 y 10');
			}

			if ($scope.Parametros.HoraEjecucion === null
					|| typeof $scope.Parametros.HoraEjecucion === 'undefined') {
				errors.push('Debe seleccionar Hora de Ejecucion');
			}
			$scope.Nomina.Habilitado = ($scope.Parametros.Habilitado!==undefined && $scope.Parametros.Habilitado) ? 'S' : 'N';
			$scope.Nomina.DiaEjecucion = $scope.Parametros.DiaEjecucion;
			$scope.Nomina.HoraEjecucion = $scope.Parametros.HoraEjecucion;
		} else {
			$scope.Nomina.AccionNomina = "INGRESAR";
			if ($scope.Nomina.Periodo === null) {
				errors.push('Debe seleccionar año/mes de proceso');
			}

			if ($scope.Nomina.FechaProgramadaProcesoCartolaConsolidada === null
					|| typeof $scope.Nomina.FechaProgramadaProcesoCartolaConsolidada === 'undefined') {
				errors
						.push('Debe seleccionar fecha/hora programada de proceso');
			} else {
				var y = $scope.Nomina.FechaProgramadaProcesoCartolaConsolidada.getFullYear();
				var m = $scope.Nomina.FechaProgramadaProcesoCartolaConsolidada.getMonth() + 1;
				if (m < 10)
					m = "0" + m;
				var s = y + "-" + m;
				if ($scope.Nomina.Periodo >= s) {
					errors
							.push('Fecha/hora de proceso debe ser posterior al periodo');
				}
			}
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Detalle de Proceso', bcsComunes
					.ventanaWinId($scope));
			return;
		}
		$scope.grabarNominaX($scope.Nomina);
	}

	$scope.grabarNominaX = function(nomina) {
		bcsComunes.ventanaOverlay(idVentana,true);
		nomina.CartolaConsolidada  = 's';
		nomina.IdEmpresa 	= $scope.SesionIdEmpresa;
		nomina.IdUsuario 	= $scope.SesionIdUsuario;		
		nomina.AccionNomina	= nomina.AccionNomina;
		nomina.SegundosEsperaProceso = 0;

		bcsCartolas.gestionarNominaConsolidada(nomina, 
			function(reply) {
				var id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(reply)
					},idVentana);
				
				nomina.IdNomina = reply.P_OUT_IDNOMINA;
				if(id){
					var promesa=bcsComunes.ventanaPromesa(id);                               
					$.when(promesa).then(function(data){
				    	 $timeout(function() {
				    		 bcsComunes.ventanaOverlay(idVentana,false);
				    		 bcsComunes.ventanaCerrar(idVentana);
				    	 },20);
				    });
				}else{
					bcsComunes.ventanaOverlay(idVentana,false);
		    		bcsComunes.ventanaCerrar(idVentana);
				}
			}, function(error) {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CREACION_NOMINA"),
					conflictos:util.parseServerResponse(error)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
		);
	}
	
	$scope.$watch('Nomina.IncluirPortafoliosExcluidos',function(newval,oldval){
		 if(newval){
			 $scope.Nomina.ExcluirPortafoliosIncluidosOtraNomina = false;
		 }
	 });
	
	$scope.$watch('Nomina.ExcluirPortafoliosIncluidosOtraNomina',function(newval,oldval){
		 if(newval){
			 $scope.Nomina.IncluirPortafoliosExcluidos = false;
		 }
	 });

}