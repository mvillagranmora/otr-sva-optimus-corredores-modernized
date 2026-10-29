(function() {
angular.module('ModuloPlataforma').controller('modificarAccesoIClienteCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	var macc = this;
	$scope.data = {oConfiguracionSocioNegocio: {filtros:{}}};	
	let idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.recordData = {};
	
	macc.IdTipoAutorizado = parametros.IdTipoAutorizado;
	macc.FirmaOptimusCliente = parametros.FirmaOptimusCliente === 'S';
	$scope.recordData = {IdCliente : parametros.IdCliente, 
			NombreCompletoClienteYRut : parametros.DocumentoIdentidadCLI + ' ' + parametros.AutorizadoPor,
			CodigoPropositoPortafolio : parametros.CodigoPropositoPortafolio,
			IdTipoAutorizado : parametros.IdTipoAutorizado,
			FirmaOptimusCliente : parametros.FirmaOptimusCliente=='S'? true: false,
			NombreCompletoPersonaAut: parametros.DocumentoIdentidad + ' ' + parametros.NombreCompleto,
			IdTipoAutorizadoNew : parametros.IdTipoAutorizado,
			IdAutorizado : parametros.IdAutorizado
			};

	$scope.data.oConfiguracionTipoPerfil={
			tipo : "combobox-tipo-perfil-portafolio",
			configuracion : {},
			filtros : {
				IdTipoRelacion : { field : "IdTipoRelacion", operator : "eq", value : "AUTICL" }
			},
			seleccionado : true
		};
		
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	
	$scope.limpiarPantalla = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.modificarAcceso = function(){
	
		let r = $resource("", {},{
			'ModificarAutorizado': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601221"}
		});
		
		let datosCreacion = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_ListaAutorizados : [{IdAutorizado : $scope.recordData.IdAutorizado, IdTipoAutorizado :  $scope.recordData.IdTipoAutorizadoNew.IdTipoAutorizado, FirmaOptimusCliente : $scope.recordData.FirmaOptimusCliente}]
		};
		
		let post = r.ModificarAutorizado(datosCreacion);
		
		bcsComunes.ventanaOverlay(idVentana, true);
		post.$promise.then(function(result){
			
			var idMensaje = bcsComunes.ventanaMsg({
				mensaje:	(result.P_OUT_ERRORESCONTADOR == 0)?$translate.instant("CREAR_AUTORIZADO_ORDEN_EXITO") : $translate.instant("UI_ERROR"),
				conflictos:	util.parseServerResponse(result)
			}, idVentana);
			
			bcsComunes.ventanaPromesa(idMensaje).then(function(){
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaCerrar(idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);	
			});
			
		},function(error){
			bcsComunes.msg("error",'ERROR',$translate.instant("UI_INTERNAL_ERROR"));	
			bcsComunes.ventanaOverlay(idVentana, false);
		});
	};
	
});
}());
//------------------------- FIN CONTROLLER --------------------------
