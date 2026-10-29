/*************************************************************
 * Purpose	: Socio Negocio Bloqueo.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalSocioNegocioBloqueoCtrl', 
	function($scope,bcsComunes,$translate,$resource){
		
		$scope.data = {};
		var ventanaId = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams (ventanaId);
		//debe recibir entre parametros 
		//parametros.Entidad --> Tipo Entidad 'PERSONA' 'CLIENTE'
		//parametros.IdEntidad --> IdPersona, IdCliente
		//parametros.IdEstado --> 'ACT','BLO' : corresponde al estado nuevo.
		$scope.IdBoton = 'ModalSocioNegocioBloqueo_' + parametros.Entidad + '_Aceptar' + parametros.IdEstado;
		$scope.tituloModal = "Cambio de Estado Realizado";

		//seteo inicial.
		$scope.data.Fecha = new Date();
		$scope.Url = '';
		switch (parametros.Entidad){
		case 'PERSONA':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=108";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_SOCIO_DE_NEGOCIO'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_SOCIO_DE_NEGOCIO'));
		break;
		case 'PORTAFOLIO':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=178";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_PORTAFOLIO'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_PORTAFOLIO'));
		break;
		case 'CLIENTE':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=96";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_CLIENTE'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_CLIENTE'));
		break;
		case 'EMPLEADO':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=103";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_EMPLEADO'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_EMPLEADO'));
		break;
		case 'PROVEEDOR':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=119";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_PROVEEDOR'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_PROVEEDOR'));
		break;
		case 'CONTRAPARTE':
			$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=111";
			if(parametros.IdEstado==='BLO') bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_BLOQUEAR_CONTRAPARTE'));
			else bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_ACTIVAR_CONTRAPARTE'));
		break;
		}	
			
		$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};
		
		var cambioEstadoSP = function(v_Entidad, v_Dato)
	    {
		    var r = $resource ("", {}, { 'url': { method: "POST", url: $scope.Url} } );
		    var post = r.url(v_Dato);
		    return post.$promise;
	    };
		
	    
	    $scope.data.oConfiguracionTipoBloqueo = {
				tipo : 'combobox-tipo-bloqueo-socio-negocio',
				configuracion : {},
				filtros : [ 
					{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa},
					{ field: "IdTipoEntidad", operator: "eq", value: parametros.Entidad  },
					{ field: "IdAccion", operator: "eq", value: parametros.IdEstado },
					{ field: "IdTipoBloqueo", operator: "ne", value: 'BAUTOM' },
					{ field: "IdTipoBloqueo", operator: "ne", value: 'AAUTOM' },
					{ field: "IdTipoBloqueo", operator: "ne", value: 'BLOAUTOM' },
        			{ field: "IdTipoBloqueo", operator: "ne", value: 'DESBAUTOM' },
					{ field: "IdEstado", operator: "eq", value: 'ACT' }]
				,
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
	    
	    parametrosSP = function() {
	    	var par = {};
	    	
	    	par.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	    	par.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
	    	
	    	par.p_UsaReglasNegocio 	= "S";
	    	
	    	par.p_IdEstado 			= parametros.IdEstado;
	    	par.p_Comentario 		= $scope.data.Comentario;
	        
	    	switch (parametros.Entidad){
	    		case 'PERSONA':
	    			par.p_IdModulo 		= 'SOC';
	    	    	par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha  		= $scope.data.Fecha;
	    			par.p_IdPersona		= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
	    		break;
	    		case 'PORTAFOLIO':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= util.dateSPseconddate($scope.data.Fecha); //$scope.data.Fecha;
	    			par.p_IdPortafolio 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&($scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO')||$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPORAL')))?util.dateSPseconddate($scope.data.FechaHasta):undefined;//?$scope.data.FechaHasta:undefined;
	    		break;
	    		case 'CLIENTE':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= util.dateSPseconddate($scope.data.Fecha);
	    			par.p_IdCliente 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?util.dateSPseconddate($scope.data.FechaHasta):undefined;
	    		break;
	    		case 'EMPLEADO':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= $scope.data.Fecha;
	    			par.p_IdEmpleado 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
	    		break;
	    		case 'PROVEEDOR':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= $scope.data.Fecha;
	    			par.p_IdProveedor 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
	    		break;
	    		case 'CONTRAPARTE':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= $scope.data.Fecha;
	    			par.p_IdContraparte = parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
	    		break;
	    	};
	    	return par;
	    }
	    
	    $scope.CambioEstado = function() {
	    	bcsComunes.ventanaOverlay(ventanaId,true);
	    	
	    	cambioEstadoSP(parametros.Entidad,parametrosSP()).then(
				function(result) {
					var erroresContador = Number(result.P_OUT_ERRORESCONTADOR || 0);
					var srv = util.parseServerResponse(result);
					var autorizacionesContador = Number((srv && srv.autorizacionesContador) || result.P_OUT_AUTORIZACIONESCONTADOR || 0);
					var pendienteAutorizar = (erroresContador === 0 && autorizacionesContador > 0);
					if (erroresContador > 0){
						bcsComunes.ventanaOverlay(ventanaId,false);
						var descripcionError = "";
						if (srv && srv.errores && srv.errores.length > 0){
							descripcionError = (srv.errores[0].Descripcion || srv.errores[0].descripcion || "");
						}
						var descripcionUpper = descripcionError.toUpperCase();
						var eventoDuplicado = descripcionUpper.indexOf("YA TIENE UN EVENTO") >= 0
							|| descripcionUpper.indexOf("EVENTO INGRESADO DEL MISMO TIPO") >= 0;

						if (eventoDuplicado){
							bcsComunes.ventanaData(ventanaId,{res:'NOK', result: result});
							bcsComunes.ventanaCerrar(ventanaId);
							var idMensajeErrorDuplicado = bcsComunes.ventanaAbrir("VentanaMensajeResultado", {
								mensaje : "La operacion tiene errores",
								conflictos : srv
							}, false, true);
							bcsComunes.ventanaPromesa(idMensajeErrorDuplicado);
						}else{
							var idMensajeError = bcsComunes.ventanaAbrir("VentanaMensajeResultado", {
								mensaje : "La operacion tiene errores",
								conflictos : srv
							}, false, true);
							bcsComunes.ventanaPromesa(idMensajeError);
						}
						
					}else{
						bcsComunes.ventanaOverlay(ventanaId,false);
						if (pendienteAutorizar){
							bcsComunes.ventanaData(ventanaId,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(ventanaId);
							var idMensajePendiente = bcsComunes.ventanaAbrir("VentanaMensajeResultado", {
								mensaje : $scope.tituloModal,
								conflictos : srv
							}, false, true);
							bcsComunes.ventanaPromesa(idMensajePendiente);
						}else{
							bcsComunes.ventanaData(ventanaId,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(ventanaId);
							var idMensaje = bcsComunes.ventanaAbrir("VentanaMensajeResultado", {
								mensaje : $scope.tituloModal,
								conflictos : srv
							}, false, true);
							bcsComunes.ventanaPromesa(idMensaje);
						}
					}
				},
				function(result){
					bcsComunes.ventanaOverlay(ventanaId,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
			);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------	