angular.module('ModuloPlataforma').controller('ingresarAccesoIClienteRelacionadoCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.searchData = {};
	$scope.recordData = {};
	let idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.data.oConfiguracionSocioNegocio = {
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				IdCliente : {field: "IdCliente", operator: "gte", value: 1 },
				EstadoCliente : {field: "EstadoCliente", operator: "eq", value: 'ACT' }
			},
	};
	
	//watch que permite asignar dinamicamente un filtro al combobox-socio-negocio
	$scope.$watchGroup(['data.Relacion.IdPersona','data.FirmaOptimusCliente'],function(nval,oval) {
		if ($scope.data.Relacion && $scope.data.Relacion.IdPersona) {				
			$scope.data.conf.oConfiguracionPortafolio.filtros = {
				IdCliente : { field : "IdCliente", operator: "eq", value : $scope.data.ClienteObj.IdCliente }
			};
					
			let filtrosRelacionados = {}; 
			if($scope.data.FirmaOptimusCliente){
				filtrosRelacionados = { filters:[{ field : "UsaFirmaDocumento", operator : "eq", value : "S" },
												 { field : "AccesoOptimusCliente", operator : "eq", value : "S" }]};				
			}else{
				filtrosRelacionados = { field : "AccesoOptimusCliente", operator : "eq", value : "S" };	
			}
									
			$scope.data.conf.oConfiguracionRelacionados.filtros = {
				complejo: true,
				logic: "and",
				filters: [
					{ field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
					{ field : "IdPersonaOrigen", operator : "eq", value : $scope.data.ClienteObj.IdPersona},
					{ field : "Administracion", operator : "eq", value : "RELACIONADOS"},
					{ field : "IdEstado", operator : "eq", value : "ACT"},
					filtrosRelacionados
				]			
			};
		}
	});

	$scope.data.oConfiguracionTipoPerfil = {
		tipo : "combobox-tipo-perfil-portafolio",
		configuracion : {},
		filtros : {	IdTipoRelacion : { field : "IdTipoRelacion", operator : "eq", value : "AUTICL" }},
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
			p_IdPersona: $scope.data.RelacionadoObj.IdPersonaDestino,
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
