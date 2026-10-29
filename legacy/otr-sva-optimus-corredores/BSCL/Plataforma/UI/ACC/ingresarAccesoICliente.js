angular.module('ModuloPlataforma').controller('ingresarAccesoIClienteCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.searchData = {};
	$scope.recordData = {};
	let idVentana = bcsComunes.ventanaWinId($scope);
	
	//watch que permite asignar dinamicamente un filtro al combobox-socio-negocio
	$scope.$watch('data.Relacion.IdPersona',function(nval,oval) {
		if ($scope.data.Relacion && $scope.data.Relacion.IdPersona) {			

			$scope.data.conf.oConfiguracionPortafolio.filtros = {
					IdCliente : {"field"   : "IdCliente","operator": "eq", "value" : $scope.data.ClienteObj.IdCliente}
			};
		}
	});
	
	$scope.data.oConfiguracionTipoPerfil={
		tipo : "combobox-tipo-perfil-portafolio",
		configuracion : {},
		filtros : {
			IdTipoRelacion : { field : "IdTipoRelacion", operator : "eq", value : "AUTICL" }
		},
		seleccionado : false
	};
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	$scope.limpiarPantalla = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);

	};
	
	$scope.crearAcceso = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		let r = $resource("", {},{
			'CrearAutorizado': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=156"}
		});
		
		let datosCreacion = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_IdEstado: "ACT",
			p_UsaReglasNegocio: "S",
			p_IdPersona: $scope.data.ClienteObj.IdPersona,
			p_IdPortafolio: $scope.data.PortafolioObj.IdPortafolio,
			p_IdTipoAutorizado: $scope.data.PerfilObj.IdTipoAutorizado,
			p_FirmaOptimusCliente: $scope.data.FirmaOptimusCliente ? 'S' : 'N'
		};
		
		let post = r.CrearAutorizado(datosCreacion);
		
		post.$promise.then(function(result){
			bcsComunes.ventanaMsg({
				mensaje:	(result.P_OUT_ERRORESCONTADOR == 0)?$translate.instant("UI_ACCESO_CREADO_OK") : $translate.instant("UI_ERROR"),
				conflictos:	util.parseServerResponse(result)
			}, idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
			if (result.P_OUT_ERRORESCONTADOR == 0) {
				bcsComunes.ventanaCerrar(idVentana);
			}
		},function(error){
			bcsComunes.msg("error",'ERROR',$translate.instant("UI_INTERNAL_ERROR"));	
			bcsComunes.ventanaOverlay(idVentana, false);
		});
	};
	
});
