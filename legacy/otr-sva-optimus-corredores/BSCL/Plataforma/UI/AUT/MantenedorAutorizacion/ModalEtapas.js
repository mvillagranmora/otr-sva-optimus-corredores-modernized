angular.module('ModuloPlataforma').controller('PanelEtapasCtrl',function ($scope, bcsServiciosAUT, bcsComunes, $translate) {
	
	$scope.data = {};
	
	const ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams (ventanaId);
	
	bcsComunes.ventanaTitulo(ventanaId,$scope.parametros.Nombre);
	bcsComunes.ventanaAutoHeight(ventanaId);

	if($scope.parametros.Etapa)
	{
		$scope.codigo = $scope.parametros.Etapa.Codigo;
		$scope.nombre = $scope.parametros.Etapa.Nombre;
		$scope.orden = $scope.parametros.Etapa.Orden;
		$scope.RadioTipoAutdef = $scope.parametros.Etapa.TipoAutorizacion;
		$scope.minimo = $scope.parametros.Etapa.MinimoAutorizan;
		$scope.estadoEtapadef = $scope.parametros.Etapa.IdEstado;
		
		$scope.textoBotonActualizar = "UI_MODIFICAR"
	}else{
		$scope.minimo = 1;
		$scope.textoBotonActualizar = "UI_CREAR";
		$scope.RadioTipoAutdef = "M";
		$scope.estadoEtapadef = "VIG";
	}
		
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId);
	};
	
	$scope.data.oConfiguracionTipoEvento={
			tipo : "combobox-tipo-evento",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.data.oConfiguracionEstadoEtapa={
			tipo : "combobox-estado-evento",
			configuracion : {},
			filtros : {
				IdEstado : { field: "NombreEntidad", operator: "eq", value: "AUT.ETAPA" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};

	$scope.crearEtapa = function( ) {
		if($scope.parametros.Etapa) {
		    const datosModificar = {
			    p_IdEmpresa			: $scope.$root.SesionIdEmpresa, 
			    p_IdUsuario			: $scope.$root.SesionIdUsuario,
			    p_IdModulo			: 'AUT',
				p_EtapaAutorizacion : []
			};

			let etapaAutorizacion = {
				IdEtapa				: $scope.parametros.Etapa.IdEtapa,
			    Codigo				: $scope.codigo,
			    Nombre				: $scope.nombre,
			    Orden				: Number($scope.orden),
			    TipoAutorizacion	: $scope.RadioTipoAut,
			    MinimoAutorizan		: $scope.RadioTipoAut !== 'M' ? 0 : Number($scope.minimo),
			    IdEstado			: $scope.estadoEtapa,
				IdTipoEvento		: $scope.parametros.Etapa.IdTipoEvento
			};

			const tieneCambios = $scope.codigo !== $scope.parametros.Etapa.Codigo || 
								 $scope.nombre !== $scope.parametros.Etapa.Nombre ||
								 $scope.orden !== $scope.parametros.Etapa.Orden ||
								 $scope.RadioTipoAut !== $scope.parametros.Etapa.TipoAutorizacion ||
								 ($scope.RadioTipoAut === 'M' && $scope.minimo !== $scope.parametros.Etapa.MinimoAutorizan) ||
								 $scope.estadoEtapa !== $scope.parametros.Etapa.IdEstado;

			if (tieneCambios) {
				datosModificar.p_EtapaAutorizacion.push(etapaAutorizacion);
				bcsServiciosAUT.ModificarEtapa(datosModificar).$promise.then(
					function(result) {
						let mensajeId = bcsComunes.ventanaMsg({conflictos: util.parseServerResponse(result)}, ventanaId);
						bcsComunes.ventanaPromesa(mensajeId).then(function() {
							if(result.P_OUT_ERRORESCONTADOR === 0) {
								bcsComunes.ventanaCerrar(ventanaId);
							}
						});
					},
					function(result){   		
						bcsComunes.msg("error","ERR_INTERNO", $translate.instant("UI_ERROR"));
				});
			} else {
				bcsComunes.msg("info", "info", "UI_SIN_CAMBIOS");
			}
		}else{
    	   //creacion
	   	    const datosCrear = {
			   	p_IdEmpresa			: $scope.$root.SesionIdEmpresa, 
			   	p_IdUsuario			: $scope.$root.SesionIdUsuario,
			   	p_IdModulo			: 'AUT',
		   	    
				p_EtapaAutorizacion : [{
					IdTipoEvento		: $scope.parametros.IdTipoEvento,
					Codigo				: $scope.codigo,
					Nombre				: $scope.nombre,
					Orden				: Number($scope.orden),
					TipoAutorizacion	: $scope.RadioTipoAut,
					MinimoAutorizan		: $scope.RadioTipoAut !== 'M' ? 0:Number($scope.minimo),
					IdEstado			: $scope.estadoEtapa
				}]
	    	};
	    	
	    	bcsServiciosAUT.CrearEtapaAutorizacion(datosCrear).$promise.then(
				function(result) {
					let mensajeId = bcsComunes.ventanaMsg({conflictos: util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(mensajeId).then(function() {
						if(result.P_OUT_ERRORESCONTADOR === 0) {
							bcsComunes.ventanaCerrar(ventanaId);
						}
					});
				},
				function() {   		
					bcsComunes.msg("error","ERR_INTERNO", $translate.instant("UI_ERROR"));
			});
		}
	};
	
	$scope.$watch('RadioTipoAut',function(newval, oldval){
		if(oldval === 'T' && newval === 'M'){
			$scope.minimo = 1;
		}
	});
});