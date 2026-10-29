angular.module('ModuloTesoreria').controller('AgregarCajaUsuarioCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria){
	
	$scope.data = {}
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.CajaUsuario =  $.extend({}, parametros.CajaUsuario)
	
	$scope.oConfiguracionComboBoxUsuario = {
		tipo : "combobox-usuario",
		configuracion : {},
		filtros : { 
			TipoEntidad : {"field": "TipoEntidad","operator": "eq", "value"   :  'EMP'}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.oConfiguracionEstadoTesoreria={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   :  'TES.CajaUsuario'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	if (parametros.CajaUsuario){ //MODIFICACION
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_CAJAUSUARIO'));
		$scope.data.codigoCaja = parametros.CajaUsuario.CodigoCaja;
		$scope.data.nombreCaja = parametros.CajaUsuario.NombreCaja;
		$scope.oConfiguracionComboBoxUsuario.idDefecto = parametros.CajaUsuario.IdUsuario;
		$scope.data.sobregiro = (parametros.CajaUsuario.Sobregiro === 'S')? true : false
		$scope.data.fechaInicio = util.dateInicioDia(parametros.CajaUsuario.FechaInicio);
		$scope.data.fechaTermino =util.dateInicioDia(parametros.CajaUsuario.FechaTermino);
		$scope.oConfiguracionEstadoTesoreria.idDefecto = parametros.CajaUsuario.IdEstado;
	} 
	else {
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CAJAUSUARIO'));
		$scope.data.codigoCaja = parametros.CodigoCaja;
		$scope.data.nombreCaja = parametros.NombreCaja;
	}
	
	// -----------------------------------------------
	// ------------ Crear Caja - Usuario -------------
	// -----------------------------------------------
	
	var successCrearCajaUsuario = function (respuesta){
			//AGREGAR
			if($scope.CajaUsuario && respuesta.P_OUT_IDCAJAUSUARIO !== undefined && respuesta.P_OUT_IDCAJAUSUARIO > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_OPERACION_INGRESO_EXITO"),
					conflictos : util.parseServerResponse(respuesta)}
				,idVentana);
				
				$scope.limpiarFormulario();
				bcsComunes.ventanaCerrar(idVentana);
			}
			//MODIFICAR
			else if ($scope.CajaUsuario && respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{	
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_OPERACION_INGRESO_EXITO"),
					conflictos : util.parseServerResponse(respuesta)}
				,idVentana);
				
				bcsComunes.ventanaCerrar(idVentana);
			}
			
		//ERROR
		else
		{	
			bcsComunes.ventanaMsg({
						mensaje : $translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos : util.parseServerResponse(respuesta)
					}, idVentana);
		}
		bcsComunes.ventanaOverlay(idVentana, false);
		
	};
	
	var errorCrearCajaUsuario = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje : $translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos : util.parseServerResponse(respuesta)
		}, idVentana);
		
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.AgregarCajaUsuario = function() {
		bcsComunes.ventanaOverlay(idVentana, true);
		
		if (parametros.CajaUsuario){ //MODIFICAR
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'TES',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Mantenedor : 'CAJAUSUARIO',
					p_FechaModificacion : util.dateSPdate(new Date()),
					p_Dato :
						[{NumFila: 1, Campo: 'IdCajaUsuario', 	Valor			: parametros.CajaUsuario.IdCajaUsuario,
																ValorAnterior	: parametros.CajaUsuario.IdCajaUsuario},
						 {NumFila: 2, Campo: 'IdUsuario', 		Valor			: $scope.oConfiguracionComboBoxUsuario.idActual,
																ValorAnterior	: parametros.CajaUsuario.IdUsuario},
						 {NumFila: 3, Campo: 'Sobregiro', 		Valor			: ($scope.data.sobregiro)? 'S': 'N',
								 								ValorAnterior	: parametros.CajaUsuario.Sobregiro},
						 {NumFila: 4, Campo: 'FechaInicio', 	Valor			: util.dateSPdate($scope.data.fechaInicio),
								 								ValorAnterior	: util.dateSPdate(parametros.CajaUsuario.FechaInicio)},
						 {NumFila: 5, Campo: 'FechaTermino', 	Valor			: util.dateSPdate($scope.data.fechaTermino),
								 								ValorAnterior	: util.dateSPdate(parametros.CajaUsuario.FechaTermino)},
						 {NumFila: 6, Campo: 'IdEstado', 		Valor			: $scope.oConfiguracionEstadoTesoreria.idActual,
								 								ValorAnterior	: parametros.CajaUsuario.IdEstado}]
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarMantenedorCaja(parametrosSP, successCrearCajaUsuario, errorCrearCajaUsuario);
		}
		else { //AGREGAR
			var datos ={
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo			: 'TES',
					p_SimularOperacion 	: 'N',
					p_UsaReglasNegocio 	: 'S',
					p_IdCaja			: parametros.IdCaja,
					p_IdUsuarioCaja		: $scope.oConfiguracionComboBoxUsuario.idActual,
					p_Sobregiro			: ($scope.data.sobregiro)? 'S': 'N',	
					p_FechaInicio	 	: util.dateSPdate($scope.data.fechaInicio),
					p_FechaTermino		: util.dateSPdate($scope.data.fechaTermino),
					p_IdEstado			: $scope.oConfiguracionEstadoTesoreria.idActual
			};
			bcsComunes.log().info(JSON.stringify(datos));
			bcsServiciosTesoreria.CrearCajaUsuario(datos, successCrearCajaUsuario, errorCrearCajaUsuario);
		}
	};
	// -----------------------------------------------
	// ------------ Fin Crear Caja - Usuario ---------
	// -----------------------------------------------
	
	$scope.limpiarFormulario = function() {
		$scope.data.usuario = 0;
		$scope.data.fechaInicio = '';
		$scope.data.fechaTermino = '';
		$scope.oConfiguracionEstadoTesoreria.idActual = 0;
	};
	
	$scope.cancelar = function() {
		//bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};
});