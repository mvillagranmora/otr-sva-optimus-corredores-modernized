angular.module('ModuloPlataforma').controller('RequisitosAperturaCierreCtrl',
		function($scope,bcsComunes,$translate,bcsCierreModulosGrid, $resource, bcsServicioPlataforma){
	
	var reap = this;
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	reap.formulario = false;
	
	reap.oConfiguracionUsuario={
			tipo          : "combobox-usuario",
			configuracion : {},
			filtros       : {
				IdEmpresa    : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''			
	};
	
	reap.oConfiguracionPerfil={
			tipo          : "combobox-perfil",
			configuracion : {},
			filtros       : {
				IdEmpresa    : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''			
	};
	
	reap.oConfiguracionModulo={
			tipo          : "combobox-modulos-cierre",
			configuracion : {},
			filtros       : {
				IdEmpresa    : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''	,
			idPostFiltro  :	false
	};
	
	reap.oConfiguracionModuloFuncionProceso={
			tipo          : "combobox-funcion-proceso",
			configuracion : {},
			filtros       : {
				IdCREModuloEmpresa : {"field" : "IdCREModuloEmpresa", "operator" : "eq", "value" : "" }
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};
	
	//GRILLA MODULOS
	reap.confBasicaRequisitos = {
		dataSource			: bcsCierreModulosGrid.dataSourceRequisitos(),
		columns				: bcsCierreModulosGrid.columnasRequisitos(),
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 200,
		selectable			: "row"
	};
	
	
	
	//LLAMADA A SERVICIO
	var agregarRequisitoAperturaCierreModulo = function(v_Dato){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=228";
		var r = $resource
		("", {}, {
					'urlConsulta': { method: "POST", url: urlConsulta}
				 }
		);
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	}
	
	reap.grabarRequisitoAperturaCierreModulo = function (IdTipoRequisito){
		
		var parametrosSP = {};
		//overlay.encendido = true;
		
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'PLA';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'S';
		
		parametrosSP.p_IdCREModuloEmpresa		= $scope.rcrr.oConfiguracionModulos.seleccionado.IdCREModuloEmpresa;
		parametrosSP.p_IdTipoRequisito			= IdTipoRequisito;
		
		if (IdTipoRequisito == 'HOR'){
			parametrosSP.p_Tipo					= reap.tipoHorario;
			parametrosSP.p_HoraMinApertura		= (reap.tipoHorario=='A')?reap.horaMinima:null;
			parametrosSP.p_HoraMinCierre		= (reap.tipoHorario=='C')?reap.horaMinima:null;
			parametrosSP.p_Nombre				= (reap.tipoHorario=='A')?'Hora minima apertura':((reap.tipoHorario=='C')?'Hora minima Cierre':null);
		}
		else if (IdTipoRequisito == 'USU') {
			parametrosSP.p_IdUsuarioRequisito 	= reap.oConfiguracionUsuario.seleccionado.IdUsuario;
			parametrosSP.p_Tipo					= reap.tipoUsuario;
			parametrosSP.p_Nombre				= (reap.tipoUsuario=='A')?'Usuario Responsable Apertura':((reap.tipoUsuario=='C')?'Usuario Responsable Cierre':null);
		}
		else if (IdTipoRequisito == 'PRF') {
			parametrosSP.p_IdPerfil				= reap.oConfiguracionPerfil.seleccionado.IdPerfil;
			parametrosSP.p_Tipo					= reap.tipoPerfil;
			parametrosSP.p_Nombre				= (reap.tipoPerfil=='A')?'Perfil Responsable Apertura':((reap.tipoPerfil=='C')?'Perfil Responsable Cierre':null);
			
		}
		else if (IdTipoRequisito == 'PRO') {
			parametrosSP.p_IdModuloFuncionProceso = reap.oConfiguracionModuloFuncionProceso.seleccionado.IdModuloFuncionProceso;
			parametrosSP.p_Tipo					= reap.tipoProcesos;
			parametrosSP.p_Nombre				= reap.oConfiguracionModuloFuncionProceso.seleccionado.NombreFuncionProceso;
		}
		else return;
		
		parametrosSP.p_AbreModulo			= $scope.rcrr.oConfiguracionModulos.seleccionado.TipoApertura;
		parametrosSP.p_CierraModulo			= $scope.rcrr.oConfiguracionModulos.seleccionado.TipoCierre;
		parametrosSP.p_IdEstado 			= 'ACT'
		
		console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
		
		/*if (parametrosVentana.Chequera){
			//	Modificar
			parametrosSP.p_IdChequera		= parametrosVentana.Chequera.IdChequera;
			parametrosSP.p_Dato 			= parametrosModificados();
			parametrosSP.p_FechaModificacion =  util.dateSPdate(new Date())
			
			console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarChequera(parametrosSP, successGrabarChequera, errorGrabarChequera);
		}
		else {
			// Crear
			parametrosSP.p_Remanente			= $scope.Remanente;
			parametrosSP.p_NumeroChequeActual 	= $scope.NroChequeActual;
			bcsServiciosTesoreria.CrearChequera(parametrosSP, successGrabarChequera, errorGrabarChequera);
		}*/
		agregarRequisitoAperturaCierreModulo(parametrosSP).then(
				function(result) {
					var msg = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ADVERTENCIA');
					}else if (result.P_OUT_ERRORESCONTADOR) {
						msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR');
					}else {
						bcsComunes.msg("info","Informacion",$translate.instant('DATOS_INGRESO_EXITO'));	
						reap.gridApiRequisitos.dataSource.read();
						reap.seleccionRequisitos = false;
					}
					
					if(msg!="")
						bcsComunes.ventanaMsg({
							mensaje: msg,
							conflictos:util.parseServerResponse(result)
						},idVentana);
					
				},
				function(result){
					bcsComunes.msg("error", "Error", "La operacion tiene errores");
				});
		reap.limpiarFormulario();
	};
	
	reap.limpiarFormulario = function (){
		reap.horaMinima 									= '';
		reap.tipoHorario 									= null;
		reap.oConfiguracionUsuario.idActual 				= false;
		reap.oConfiguracionUsuario.idDefecto				= '';
		reap.tipoUsuario 									= null;
		reap.oConfiguracionPerfil.idActual 					= false;
		reap.oConfiguracionPerfil.idDefecto 				= '';
		reap.tipoPerfil 									= null;
		reap.oConfiguracionModulo.idActual 					= false;
		reap.oConfiguracionModulo.idDefecto 				= '';
		reap.oConfiguracionModuloFuncionProceso.idActual 	= false;
		reap.oConfiguracionModuloFuncionProceso.idDefecto 	= '';
		reap.tipoProcesos 									= null;
	};
	
	$scope.$watch('rcrr.RequisitosBuscar', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			var filters = new Array();
			if($scope.rcrr.oConfiguracionModulos.idActual !== null && $scope.rcrr.oConfiguracionModulos.idActual !== false){
				filters.push({field: "IdCREModuloEmpresa", operator: "eq", value: $scope.rcrr.oConfiguracionModulos.idActual});
				console.log("FILTROS: " +JSON.stringify(filters));
				reap.gridApiRequisitos.dataSource.filters(filters);
			}
			reap.formulario = true;
			$scope.rcrr.RequisitosBuscar = false;
		}
	});
	
});