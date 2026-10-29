angular.module('ModuloTesoreria').controller('AgregarCajasMonedaCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria){
	
	$scope.data = {}
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.CajaMoneda = $.extend({}, parametros.CajaMoneda);
	
	$scope.oConfiguracionComboBoxMoneda = {
			tipo : "combobox-moneda-tesoreria",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	$scope.oConfiguracionTipoMovimiento={
			tipo : "combobox-tipo-movimiento",
			configuracion : {},
			filtros : {
				IdModulo      : { "field"   : "IdModulo","operator": "eq", "value"   : "TES" },				
				IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.oConfiguracionEstadoTesoreria={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   :  'TES.CajaMoneda'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	if (parametros.CajaMoneda){
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_CAJAMONEDA'));
		$scope.data.codigoCaja = parametros.CajaMoneda.Codigo;
		$scope.data.nombreCaja = parametros.CajaMoneda.NombreCaja;
		//$scope.data.kngSaldo = parametros.CajaMoneda.Saldo;
		$scope.oConfiguracionComboBoxMoneda.idDefecto = parametros.CajaMoneda.IdMoneda;
		$scope.oConfiguracionTipoMovimiento.idDefecto = (parametros.CajaMoneda.IdTipoMovimiento === 'TODOS')? '': parametros.CajaMoneda.IdTipoMovimiento;		
		$scope.oConfiguracionEstadoTesoreria.idDefecto = parametros.CajaMoneda.IdEstadoCajaMoneda;
	}
	else {
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CAJAMONEDA'));
		$scope.data.codigoCaja = parametros.CodigoCaja;
		$scope.data.nombreCaja = parametros.NombreCaja;
	}
	
	// -----------------------------------------------
	// ------------ Crear Caja - Moneda --------------
	// -----------------------------------------------
	var successCrearCajaMoneda = function (respuesta){
		//AGREGAR
		if($scope.CajaMoneda && respuesta.P_OUT_IDCAJAMONEDA !== undefined && respuesta.P_OUT_IDCAJAMONEDA > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO') +' '+  respuesta.P_OUT_IDCAJAMONEDA);
			
			$scope.limpiarFormulario();
			bcsComunes.ventanaCerrar(idVentana);
		}
		//MODIFICAR
		else if ($scope.CajaMoneda && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.ventanaCerrar(idVentana);
		}
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
	
	var errorCrearCajaMoneda = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.AgregarCajaMoneda = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		
		if (parametros.CajaMoneda) {
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'TES',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Mantenedor : 'CAJAMONEDA',
					p_FechaModificacion : util.dateSPdate(new Date()),
					p_Dato :
						[{NumFila: 1, Campo: 'IdCajaMoneda', 	 Valor			: parametros.CajaMoneda.IdCajaMoneda, 
																 ValorAnterior	: parametros.CajaMoneda.IdCajaMoneda},
						 {NumFila: 2, Campo: 'IdMoneda', 		 Valor			: $scope.oConfiguracionComboBoxMoneda.idActual, 
																 ValorAnterior	: parametros.CajaMoneda.IdMoneda},
						 {NumFila: 3, Campo: 'Saldo', 			 Valor			: 0, //$scope.data.kngSaldo, 
																 ValorAnterior	: 0}, //parametros.CajaMoneda.Saldo},
						 {NumFila: 4, Campo: 'IdTipoMovimiento', Valor			: ($scope.oConfiguracionTipoMovimiento.idActual == 0)? 'TODOS':$scope.oConfiguracionTipoMovimiento.idActual, 
								 								 ValorAnterior	: parametros.CajaMoneda.IdTipoMovimiento},						 
						 {NumFila: 5, Campo: 'IdEstado', 		 Valor			: $scope.oConfiguracionEstadoTesoreria.idActual, 
								 								 ValorAnterior	: parametros.CajaMoneda.IdEstadoCajaMoneda},
						 {NumFila: 6, Campo: 'IdCaja', 		     Valor			: parametros.CajaMoneda.IdCaja, 
							                                     ValorAnterior	: parametros.CajaMoneda.IdCaja}]
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarMantenedorCaja(parametrosSP, successCrearCajaMoneda, errorCrearCajaMoneda);
		}
		else {
			var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo			: 'TES',
				p_SimularOperacion 	: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_IdCaja			: parametros.IdCaja,
				p_IdMoneda			: $scope.oConfiguracionComboBoxMoneda.idActual,
				p_Saldo				: 0, //$scope.data.kngSaldo,
				p_IdTipoMovimiento 	: ($scope.oConfiguracionTipoMovimiento.idActual == 0)? 'TODOS':$scope.oConfiguracionTipoMovimiento.idActual,				
				p_IdEstado			: $scope.oConfiguracionEstadoTesoreria.idActual
			};
			bcsComunes.log().info(JSON.stringify(datos));
			bcsServiciosTesoreria.CrearCajaMoneda(datos, successCrearCajaMoneda, errorCrearCajaMoneda);
			};
	};
	// -----------------------------------------------
	// ------------ Fin Crear Caja - Moneda ----------
	// -----------------------------------------------
	
//	$scope.$watch('oConfiguracionComboBoxMoneda.idActual',function(newval,oldval){
//		if (typeof $scope.oConfiguracionComboBoxMoneda.seleccionado !== 'undefined' &&
//			$scope.oConfiguracionComboBoxMoneda.idActual !== 'undefined' && $scope.oConfiguracionComboBoxMoneda.idActual !== '')
//		{
//			
//		};
//	},true);
	
	$scope.limpiarFormulario = function(){
		$scope.data.moneda = 0;
		$scope.oConfiguracionTipoMovimiento.idActual = 0;
		$scope.oConfiguracionEstadoTesoreria.idActual = 0;
	};
	
	$scope.cancelar = function() {
		//bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};
});