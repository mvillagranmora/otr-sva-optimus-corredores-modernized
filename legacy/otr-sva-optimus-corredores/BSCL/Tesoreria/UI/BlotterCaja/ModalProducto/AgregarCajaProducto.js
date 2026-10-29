angular.module('ModuloTesoreria').controller('AgregarCajaProductoCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosTesoreria){
	
	$scope.data = {}
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	
	$scope.data.codigoCaja = parametros.Codigo;
	$scope.data.nombreCaja = parametros.NombreCaja;

	
	$scope.CajaProducto = $.extend({}, parametros.CajaProducto)
	
	$scope.oConfiguracionProductoEmpresa={
			tipo : "combobox-producto-empresa",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	$scope.oConfiguracionEstadoTesoreria={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   :  'TES.CajaProducto'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	if (parametros.CajaProducto){
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_OPERACION_INGRESO_EXITO'));
		$scope.data.codigoCaja = parametros.CajaProducto.CodigoCaja;
		$scope.data.nombreCaja = parametros.CajaProducto.NombreCaja;
		$scope.oConfiguracionProductoEmpresa.idDefecto = parametros.CajaProducto.IdProductoEmpresa;
		$scope.oConfiguracionEstadoTesoreria.idDefecto = parametros.CajaProducto.IdEstado;
	}
	else {
		bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CAJAPRODUCTO'));
		$scope.data.codigoCaja = parametros.CodigoCaja;
		$scope.data.nombreCaja = parametros.NombreCaja;
	}
	
	// -----------------------------------------------
	// ------------ Crear Caja - ProductoEmpresa -----
	// -----------------------------------------------
	
	var successCrearCajaProducto = function (respuesta){
		if($scope.CajaProducto && respuesta.P_OUT_IDCAJAPRODUCTO !== undefined && respuesta.P_OUT_IDCAJAPRODUCTO > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO') +' '+  respuesta.P_OUT_IDCAJAPRODUCTO);
			
			$scope.limpiarFormulario();
			bcsComunes.ventanaCerrar(idVentana);
		}
		
		else if ($scope.CajaProducto && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			bcsComunes.ventanaCerrar(idVentana);
		}
		else{
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
	
	var errorCrearCajaProducto = function(respuesta){
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	$scope.AgregarCajaProducto = function() {
		bcsComunes.ventanaOverlay(idVentana, false);
		
		if (parametros.CajaProducto) { //modificar
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'TES',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_Mantenedor : 'CAJAPRODUCTO',
					p_FechaModificacion : util.dateSPdate(new Date()),
					p_Dato :
						[{NumFila: 1, Campo: 'IdCajaProductoEmpresa', 	Valor			: parametros.CajaProducto.IdCajaProductoEmpresa,
																		ValorAnterior	: parametros.CajaProducto.IdCajaProductoEmpresa},
						 {NumFila: 2, Campo: 'IdProductoEmpresa', 		Valor			: $scope.oConfiguracionProductoEmpresa.idActual,
																		ValorAnterior	: parametros.CajaProducto.IdProductoEmpresa	},
						 {NumFila: 3, Campo: 'IdEstado',				Valor			: $scope.oConfiguracionEstadoTesoreria.idActual,
																		ValorAnterior	: parametros.CajaProducto.IdEstado }]
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarMantenedorCaja(parametrosSP, successCrearCajaProducto, errorCrearCajaProducto);
		}
		else { // crear
			var datos ={
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo			: 'TES',
					p_SimularOperacion 	: 'N',
					p_UsaReglasNegocio 	: 'S',
					p_IdCaja			: parametros.IdCaja,
					p_IdProductoEmpresa	: $scope.oConfiguracionProductoEmpresa.idActual,
					p_IdEstado			: $scope.oConfiguracionEstadoTesoreria.idActual
			};
			bcsComunes.log().info(JSON.stringify(datos));
			bcsServiciosTesoreria.CrearCajaProducto(datos, successCrearCajaProducto, errorCrearCajaProducto);
		}
	};
	// -----------------------------------------------
	// ------------ Fin Crear Caja - Producto --------
	// -----------------------------------------------
	
	$scope.limpiarFormulario = function() {
		$scope.oConfiguracionProductoEmpresa.idActual = 0;
		$scope.oConfiguracionEstadoTesoreria.idActual = 0;
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	
});