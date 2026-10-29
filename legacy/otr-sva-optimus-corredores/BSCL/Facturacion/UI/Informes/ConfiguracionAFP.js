angular.module('ModuloFacturacion').controller('ConfiguracionAFPCtrl',
		function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
				$timeout,$translate,$q, $rootScope,$resource,
				bcsConfiguracionAFPGrid){
	
	$scope.data = {};
	obj = {};
	var filters = new Array();
	$scope.data.IdEmpresa = $scope.SesionIdEmpresa;
	$scope.data.IdUsuario = $scope.SesionIdUsuario;
	$scope.data.IdModulo = 'FAC';
//	$scope.data.FechaSistema = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");  // dd-mm-YYYY
	$scope.data.FechaSistema = $scope.SesionFechaSistema;										// YYYY-mm-dd
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
	
	$scope.confBasicaConfiguracioAfp = { 
		dataSource			: bcsConfiguracionAFPGrid.dataSourceConfiguracionAFP,
		columns				: bcsConfiguracionAFPGrid.ColumnasConfiguracionAFP(),
		sortable			: true,
		pageable			: true,
//		filterable			: true,
		userSettings        : true,
//		autoBind			: true,
		height				: 400,
		filterable			: true,
		excel 				: { allPages : true },
		selectable 			: "row",
		navigatable         : true
	};
	
	bcsConfiguracionAFPGrid.dataSourceConfiguracionAFP.filter(filters);
	
	$scope.data.oConfiguracionAfp = {
		tipo : "combobox-afp",
		configuracion : {},
		filtros : {
			read : false
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oClientePortafolio = {
		tipo : "combobox-cliente-portafolio",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		filtros : {
			IdEstado : {
				"field" : "IdEmpresa",
				"operator" : "eq",
				"value" : $scope.SesionIdEmpresa
			},
			EsCliente : {
				"field" : "EsCliente",
				"operator" : "eq",
				"value" : 'UI_SI'
			}
		},
	};
	
	$scope.data.oComboboxMultifondos = {
		tipo : "combobox-multifondo",
		configuracion : {},
		filtros : {
			read : false
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	// ***********************************************
	// Botones de Agregar y Quitar registro
	// ***********************************************
	
	$scope.buscar = function(){
		bcsConfiguracionAFPGrid.dataSourceConfiguracionAFP.filter(filters);
	};
	
	$scope.limpiarFiltros = function(){	
		$scope.filtros.IdAFP = false;
		$scope.filtros.IdMultifondo = false;	
		$scope.filtros.IdPortafolio = false;
		bcsConfiguracionAFPGrid.dataSourceConfiguracionAFP.removeAll();	
		$scope.seleccionConfiguracionAfp = undefined;
	};
	
	$scope.data.agregar = function(){
		if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio === undefined || 
				$scope.data.oConfiguracionAfp.seleccionado.IdAFP === undefined ||
				$scope.data.oComboboxMultifondos.seleccionado.IdMultifondo === undefined) {
			$scope.data.btnActivoQuitar = true; 
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_INGRESAR_CAMPOS_OBLIGATORIOS'));
			return;
		} else {
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", 
					$translate.instant("UI_CONTINUAR"), idVentana);
			t.then(function(result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					// Logica para Quitar Registro
					$scope.data.btnActivoAgregar = false;
					bcsComunes.ventanaOverlay(idVentana, true);
					var envio = {};
			        var datosArray = [];
					datosArray.push({
			        	IdEmpresa : $scope.data.IdEmpresa,
			        	IdPortafolio : $scope.data.oClientePortafolio.seleccionado.IdPortafolio,
			        	IdCliente : $scope.data.oClientePortafolio.seleccionado.IdCliente,	
			        	IdAFP : $scope.data.oConfiguracionAfp.seleccionado.IdAFP,
			        	IdMultifondo : $scope.data.oComboboxMultifondos.seleccionado.IdMultifondo,
			        	FechaIngreso : $scope.data.FechaSistema,
			        }); 
			    	envio.tipo = "ADD"; 
			    	envio.p_EnvioAdd = datosArray;			        
			        var r = $resource("",{} , {
						'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290606"}
					});
					r.Consultar(envio).$promise.then(function(result){						
				    	if(result.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.msg("info", "info", 'Configuracion AFP agregada para cliente ' + $scope.data.oClientePortafolio.seleccionado.IdCliente);
							$scope.limpiarFiltros();
							$scope.buscar();
				    	}else{
							bcsComunes.msg("warning", "warning", result.P_OUT_MENSAJES[0].descripcion);
							$scope.limpiarFiltros();
							$scope.buscar();
				    	}
						
					},function(result){
						bcsComunes.msg("warning", "warning", 'Error de conexion');
						$scope.limpiarFiltros();
						$scope.buscar();
					});
					$scope.data.btnActivoAgregar = true; 
					bcsComunes.ventanaOverlay(idVentana, false);
				} else {
					bcsComunes.msg("info", "info", $translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + 
							$translate.instant("UI_CANCELAR"));
				}
			});
		}
	};
	
	$scope.data.quitar = function() {
		if ($scope.seleccionConfiguracionAfp === undefined) {
			$scope.data.btnActivoQuitar = true; 
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		} else {
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", 
					$translate.instant("UI_MENSAJE_QUITAR_REGISTRO"), idVentana);
			t.then(function(result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					// Logica para Quitar Registro
					$scope.data.btnActivoQuitar = false;
					bcsComunes.ventanaOverlay(idVentana, true);
					var envio = {};
			        var datosArray = [];
			        datosArray.push({
			        	IdConfiguracionAFP : $scope.seleccionConfiguracionAfp.IdConfiguracionAFP,
			        }); 	
			        envio.tipo = "DELETE"; 
			        envio.p_EnvioDelete = datosArray;
			        bcsComunes.msg("info", "info", JSON.stringify(envio));
			        var r = $resource("",{} , {
						'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290606"}
					});
					r.Consultar(envio).$promise.then(function(result){
						if(result.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.msg("info", "info", 'Configuracion AFP numero ' + $scope.seleccionConfiguracionAfp.IdConfiguracionAFP + ' quitada correctamente');
							$scope.limpiarFiltros();
							$scope.buscar();
							$scope.seleccionConfiguracionAfp = undefined;
				    	}else{
							bcsComunes.msg("warning", "warning", result.P_OUT_MENSAJES[0].descripcion);
							$scope.limpiarFiltros();
							$scope.buscar();
							$scope.seleccionConfiguracionAfp = undefined;
				    	}
					},function(result){
						bcsComunes.msg("warning", "warning", 'Error de conexion');
						$scope.limpiarFiltros();
						$scope.buscar();
						$scope.seleccionConfiguracionAfp = undefined;
					});
					$scope.data.btnActivoQuitar = true;
					bcsComunes.ventanaOverlay(idVentana, false);
				} else {
					bcsComunes.msg("info", "info", $translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + 
							$translate.instant("UI_CANCELAR"));
				}
			});
		}
	};	
	
	$scope.$on('$viewContentLoaded', function(){
		$scope.buscar();
	});
});