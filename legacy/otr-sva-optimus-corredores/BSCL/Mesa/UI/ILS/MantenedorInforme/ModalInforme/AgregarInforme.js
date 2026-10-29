angular.module('ModuloMesa').controller('AgregarInformeCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria){
	
	$scope.data = {}
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.Caja = $.extend({}, parametros.Caja);
	
	$scope.oConfiguracionEstadoTesoreria={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   :  'TES.Caja'},
				IdEstado : {"field": "IdEstado","operator": "eq", "value"   :  (parametros.Caja ? '':'CER') } //Caja se crea cerrada inicialmente
			},
			idActual : false,
			seleccionado : false,
			idDefecto : (parametros.Caja ? '':'CER')			
	};
	
	if (parametros.Caja) //modificacion
	{	
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_ILS_MODIFICAR_INFORME'));
		 
		$scope.data.codigoCaja = parametros.Caja.CodigoCaja;
		$scope.data.nombreCaja = parametros.Caja.NombreCaja;
		$scope.oConfiguracionEstadoTesoreria.idDefecto = parametros.Caja.IdEstadoCaja;
	}
	else //AGREGAR
	{	
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_ILS_AGREGAR_INFORME'));
	}
	
	// -----------------------------------------------
	// ----------------- Crear Caja ------------------
	// -----------------------------------------------
	var successCrearCaja = function (respuesta){
		//AGREGAR
		if($scope.Caja && respuesta.P_OUT_IDCAJA !== undefined && respuesta.P_OUT_IDCAJA > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO') +' '+  respuesta.P_OUT_IDCAJA);
			
			$scope.limpiarFormulario();
			bcsComunes.ventanaCerrar(idVentana);
		}
		//MODIFICAR
		else if ($scope.Caja && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.ventanaCerrar(idVentana);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	var errorCrearCaja = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.AgregarCajas = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		
		if(parametros.Caja){
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'TES',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Mantenedor : 'CAJA',
					p_FechaModificacion : util.dateSPdate(new Date()),
					p_Dato :
						[{NumFila: 1, Campo: 'IdCaja', Valor: parametros.Caja.IdCaja, ValorAnterior: parametros.Caja.IdCaja},
						 {NumFila: 2, Campo: 'Codigo', Valor: $scope.data.codigoCaja, ValorAnterior: parametros.Caja.CodigoCaja},
						 {NumFila: 3, Campo: 'Nombre', Valor: $scope.data.nombreCaja, ValorAnterior: parametros.Caja.NombreCaja},
						 {NumFila: 4, Campo: 'IdEstado', Valor: $scope.oConfiguracionEstadoTesoreria.idActual, ValorAnterior: parametros.Caja.IdEstadoCaja}]
					
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarMantenedorCaja(parametrosSP, successCrearCaja, errorCrearCaja);
		}
		else {
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_Codigo : $scope.data.codigoCaja,
					p_Nombre : $scope.data.nombreCaja,
					p_IdEstado : $scope.oConfiguracionEstadoTesoreria.idActual
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				bcsServiciosTesoreria.CrearCaja(parametrosSP, successCrearCaja, errorCrearCaja);
		}
		
	};
	// -----------------------------------------------
	// --------------- Fin Crear Caja ----------------
	// -----------------------------------------------
	
	$scope.limpiarFormulario = function(){
		$scope.data.codigoCaja = '';
		$scope.data.nombreCaja = '';
		$scope.oConfiguracionEstadoTesoreria.idActual = 0;
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};
	
});