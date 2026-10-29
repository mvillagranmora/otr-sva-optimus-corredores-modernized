angular.module('ModuloPlataforma').controller('PanelAutorizadorCtrl',function ($scope, bcsServiciosAUT, bcsComunes, $translate) {
	$scope.data = {};
	$scope.filtros = {};
	$scope.data.EsAutorizacionPorRol = false;
	$scope.filtros.reemplazoIndefinido = false;
	
	const idVentana = bcsComunes.ventanaWinId($scope);
	const parametros = bcsComunes.ventanaParams (idVentana);
	bcsComunes.ventanaTitulo(idVentana, parametros.Nombre);
	bcsComunes.ventanaAutoHeight(idVentana);
	
	$scope.data.oConfiguracionEstadoAutorizador={
		tipo : "combobox-estado-evento",
		configuracion : {},
		filtros : {
			IdEstado : { field: "NombreEntidad", operator: "eq", value: "AUT.AUTORIZADOETAPA" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'VIG'
	};
	
	$scope.getConfiguracionUsuario = function() {
		return {
			tipo : "combobox-usuario",
			configuracion : {},
			filtros : {
				IdEstado  	: { field: "IdEstado",    operator: "eq", value: "ACT" },
				IdEmpresa 	: { field: "IdEmpresa",   operator: "eq", value: $scope.$root.SesionIdEmpresa },
				TipoEntidad : { field: "TipoEntidad", operator: "eq", value: "EMP" }, //No pueden ser CLI para excluir los de iCliente
			},
			idActual : false,
			seleccionado : false,
			idDefecto : undefined
		}
	};
	
	$scope.data.oConfiguracionUsuario = $scope.getConfiguracionUsuario();
	$scope.data.oConfiguracionUsuarioReemplaza = $scope.getConfiguracionUsuario();

	$scope.data.oConfiguracionRol={
		tipo : "combobox-rol",
		configuracion : {},
		filtros : {
			IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" },
			IdEmpresa : { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	if(parametros.Autorizador){
		$scope.data.EsAutorizacionPorRol = parametros.Autorizador.EsAutorizacionPorRol === 'S';
	   	$scope.data.oConfiguracionUsuario.idDefecto = parametros.Autorizador.IdUsuario;	   	
	   	$scope.data.oConfiguracionRol.idDefecto = parametros.Autorizador.IdRol;
	   	$scope.data.oConfiguracionUsuarioReemplaza.idDefecto = parametros.Autorizador.IdUsuarioReemplaza;
	   	
	   	let reemplazaDesde = util.dateSPdate(parametros.Autorizador.ReemplazaDesde) || '0001-01-01';
	   	let reemplazaHasta = util.dateSPdate(parametros.Autorizador.ReemplazaHasta) || '0001-01-01';
	   	
	   	if(reemplazaDesde === "0001-01-01" && reemplazaHasta === "0001-01-01" && parametros.Autorizador.IdUsuarioReemplaza !== 0){
	   		$scope.filtros.reemplazoIndefinido = true;
	   	}else{
			$scope.data.fechaInicioReemplazo = new Date(parametros.Autorizador.ReemplazaDesde); 
			$scope.data.fechaTerminoReemplazo = new Date(parametros.Autorizador.ReemplazaHasta); 	
	   	}
	   	$scope.data.oConfiguracionEstadoAutorizador.idDefecto = parametros.Autorizador.IdEstado;
	   	
	   	$scope.textoBotonActualizar = "UI_MODIFICAR"
	   	$scope.originalData = angular.copy($scope.data);
	}else{
		$scope.textoBotonActualizar = "UI_CREAR"
	}
	
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.crearAutorizador = function() {	
		if(parametros.Autorizador){
	        const datosModificar = {
			   	p_IdEmpresa				: $scope.$root.SesionIdEmpresa, 
			   	p_IdUsuario				: $scope.$root.SesionIdUsuario,
			   	p_IdModulo				: 'PLA',
		   	    p_AutorizadoEtapa		:[]
		   	};
		   	
		   	let autorizadoEtapa = { 
			 	IdAutorizadoEtapa 		: parametros.Autorizador.IdAutorizadoEtapa, 
				IdEtapa					: parametros.IdEtapa,
			   	IdUsuario				: $scope.data.oConfiguracionUsuario.idActual || 0,
			   	IdRol					: $scope.data.oConfiguracionRol.idActual || 0,
			   	IdUsuarioReemplaza		: $scope.data.oConfiguracionUsuarioReemplaza.idActual || 0,
			   	EsAutorizacionPorRol 	: $scope.data.EsAutorizacionPorRol? 'S' : 'N',
			   	IdEstado				: $scope.data.oConfiguracionEstadoAutorizador.idActual,
			   	ReemplazaDesde 			: !$scope.filtros.reemplazoIndefinido && $scope.data.fechaInicioReemplazo ? util.dateSPdate($scope.data.fechaInicioReemplazo) : '0001-01-01',
	        	ReemplazaHasta 			: !$scope.filtros.reemplazoIndefinido && $scope.data.fechaTerminoReemplazo ? util.dateSPdate($scope.data.fechaTerminoReemplazo) : '0001-01-01'
			};
	        
	       const reemplazaDesde = util.dateSPdate(parametros.Autorizador.ReemplazaDesde);
		   const reemplazaHasta = util.dateSPdate(parametros.Autorizador.ReemplazaHasta);
		   const esAutorizacionPorRol = parametros.Autorizador.EsAutorizacionPorRol === 'S';	
	       
	       const tieneCambios = 
				   $scope.data.EsAutorizacionPorRol !== esAutorizacionPorRol ||
				   autorizadoEtapa.ReemplazaDesde !== reemplazaDesde ||
				   autorizadoEtapa.ReemplazaHasta !== reemplazaHasta ||
				   autorizadoEtapa.IdEstado !== parametros.Autorizador.IdEstado ||
				   autorizadoEtapa.IdRol !== parametros.Autorizador.IdRol ||
				   autorizadoEtapa.IdUsuario !== parametros.Autorizador.IdUsuario ||
				   autorizadoEtapa.IdUsuarioReemplaza !== parametros.Autorizador.IdUsuarioReemplaza;
				    
			if(tieneCambios){	    
	        	datosModificar.p_AutorizadoEtapa.push(autorizadoEtapa);

		        bcsServiciosAUT.ModificarAutorizadorEtapa(datosModificar).$promise.then(
					function(result) {
						let mensajeId = bcsComunes.ventanaMsg({ conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(mensajeId).then(function(){
							if(result.P_OUT_ERRORESCONTADOR === 0){
								bcsComunes.ventanaCerrar(idVentana);
							}
						});
					},
					function(){
						bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
				});
			}else{
				bcsComunes.msg("info", "info", "Sin cambios");
			}
		} 
		else 
		{
	   	    const datosCrear = {
			   	p_IdEmpresa				: $scope.$root.SesionIdEmpresa, 
			   	p_IdUsuario				: $scope.$root.SesionIdUsuario,
			   	p_IdModulo				: 'PLA',
		   	    p_AutorizadoEtapa		:[{
					IdEtapa					: parametros.IdEtapa,
				   	IdUsuario				: $scope.data.oConfiguracionUsuario.idActual || 0,
				   	IdRol					: $scope.data.oConfiguracionRol.idActual || 0,
				   	IdUsuarioReemplaza		: $scope.data.oConfiguracionUsuarioReemplaza.idActual || 0,
				   	EsAutorizacionPorRol 	: $scope.data.EsAutorizacionPorRol? 'S' : 'N',
				   	IdEstado				: $scope.data.oConfiguracionEstadoAutorizador.idActual,
				   	ReemplazaDesde 			: !$scope.filtros.reemplazoIndefinido && $scope.data.fechaInicioReemplazo ? util.dateSPdate($scope.data.fechaInicioReemplazo) : '0001-01-01',
		        	ReemplazaHasta 			: !$scope.filtros.reemplazoIndefinido && $scope.data.fechaTerminoReemplazo ? util.dateSPdate($scope.data.fechaTerminoReemplazo) : '0001-01-01'
				}]
		   	};
	        
	        bcsServiciosAUT.CrearAutorizadorEtapa(datosCrear).$promise.then(
				function(result) {
					let mensajeId = bcsComunes.ventanaMsg({ conflictos : util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaPromesa(mensajeId).then(function(){
						if(result.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaCerrar(idVentana);
						}
					});
				},
				function(){
					bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
			});	       
		}	
	}; 
	
	$scope.$watch('data.EsAutorizacionPorRol',function(newval){
		if(newval){
			$scope.data.oConfiguracionUsuario.idActual = false;
			$scope.data.oConfiguracionUsuarioReemplaza.idActual = false;
			$scope.filtros.reemplazoIndefinido = false;
		}else{
			$scope.data.oConfiguracionRol.idActual = false;
		}
	});
	
	$scope.$watch('filtros.reemplazoIndefinido',function(newval){
		if(newval){
			$scope.data.fechaInicioReemplazo = null;
			$scope.data.fechaTerminoReemplazo = null;
		}
	});
	
	$scope.$watch('data.oConfiguracionUsuarioReemplaza.seleccionado',function(newval){
		if(newval && parametros.Autorizador){
			$scope.data.fechaInicioReemplazo = util.dateSPdate(parametros.Autorizador.ReemplazaDesde) !== '0001-01-01'? new Date(parametros.Autorizador.ReemplazaDesde): '0001-01-01';
			$scope.data.fechaTerminoReemplazo = util.dateSPdate(parametros.Autorizador.ReemplazaHasta) !== '0001-01-01'? new Date(parametros.Autorizador.ReemplazaHasta): '0001-01-01';
			$scope.filtros.reemplazoIndefinido = $scope.data.fechaInicioReemplazo === '0001-01-01' && $scope.data.fechaTerminoReemplazo === '0001-01-01';
		}else{
			$scope.data.fechaInicioReemplazo = null;
			$scope.data.fechaTerminoReemplazo = null;
			$scope.filtros.reemplazoIndefinido = false;
		}
	})

});