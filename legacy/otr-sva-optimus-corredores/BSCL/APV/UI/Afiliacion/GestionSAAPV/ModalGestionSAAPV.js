/*************************************************************
 * Purpose	: Modal Gestion SAAPV
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 14/03/2018
 **************************************************************/
(function() {
	angular.module('ModuloAPV').controller('ModalGestionSAAPVCtrl', 
	function($scope,bcsComunes,bcsServiciosAPV,$translate,$resource)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		var idVentana = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams(idVentana);
		var mgsaapv = this;

		var IdEntidad = parametros.IdEntidad;
		var ContratosSAAPV = parametros.ContratosSAAPV;
		
		mgsaapv.TipoDeGestion = parametros.Entidad;
		mgsaapv.Fecha = new Date();
		
		mgsaapv.AceptarHabilitado = true;
		mgsaapv.CancelarHabilitado = true;
		
		switch (IdEntidad)
		{
			case 'REV':
				//$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=108";
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_REVOCAR_CONTRATO_SAAPV'));
			break;
			
			case 'CER':
				//$scope.Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=178";
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_CERRAR_CONTRATO_SAAPV'));
			break;
			
			case 'ANU':
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_ANULAR_CONTRATO_SAAPV'));
			break;
			
		
			case 'VIG':
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_DEJAR_VIGENTE_CONTRATO_SAAPV'));
			break;
			
		}
		
/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
		mgsaapv.GlosaFiltros =
		{
			filtros : 
			{
				Codigo 		: {field: "Codigo", 	 operator: "eq", value: IdEntidad},
				EstadoGlosa : {field: "EstadoGlosa", operator: "eq", value: 'VIG'}
			}
		};

/******************************************************/
/*** FUNCIONES										***/
/******************************************************/

		mgsaapv.Cancelar = function () 
		{
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		mgsaapv.Aceptar = function () 
		{
			if(mgsaapv.Comentario !== undefined && mgsaapv.Comentario !== null && mgsaapv.Comentario !== '')
			{
				if(mgsaapv.GlosaIdActual !== undefined && mgsaapv.GlosaIdActual !== false)
				{
					var parametros = 
					{
						p_IdEmpresa			: $scope.SesionIdEmpresa,
						p_IdUsuario			: $scope.SesionIdUsuario,
						p_IdModulo			: 'APV',
						p_Opcion			: IdEntidad,
						p_ContratosSAAPV	: 
						[{
							IdPersona					: ContratosSAAPV[0].IdPersona,
							IdCliente					: ContratosSAAPV[0].IdCliente,
							IdEmpleador					: ContratosSAAPV[0].IdEmpleador,
							IdFolioContratoSAAPV		: ContratosSAAPV[0].IdFolioContratoSAAPV,
							FechaRevocacion				: ContratosSAAPV[0].FechaRevocacion,
							IdEstadoEnvioNotificacion	: ContratosSAAPV[0].IdEstadoEnvioNotificacion,
							IdEstadoSolicitud			: ContratosSAAPV[0].IdEstadoSolicitud,
							IdEstado					: ContratosSAAPV[0].IdEstado,
						    IdGlosa						: mgsaapv.GlosaIdActual,
						    DescripcionGlosa			: mgsaapv.Comentario
						}],
					};
				
					bcsComunes.ventanaOverlay(idVentana,true);
					
					bcsServiciosAPV.CambiarEstadoContratosSAAPV(parametros).$promise.then(function(respuesta)
					{
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						
						if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
						{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
							
						}else if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_ADVERTENCIASCONTADOR != 0)
						{
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
						}
						else
						{
							var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
							var mensajeTotal = new Array();
							
							$.each(ErrorDetalle,function(key,value)
							{
								var objMensaje = {
                                    Id : value.id,
                                    tipo : value.tipo,
                                    codigo : value.codigo,
                                    descripcion : $translate.instant(value.descripcion)
                             };
							 	mensajeTotal.push(objMensaje);
								
							});
							respuesta.P_OUT_MENSAJES = mensajeTotal;
                            bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						}
					},function(respuesta)
					{
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_EXISTEN_PROBLEMAS'),conflictos:util.parseServerResponse(respuesta)},idVentana);
					});
				}
				else
				{
					bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBES_SELECCIONAR_UNA_GLOSA'));
					return;
				}
			}
			else
			{
				bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBES_INGRESAR_UN_COMENTARIO'));
				return;
			}
		}
		
		/*
		var cambioEstadoSP = function(v_Entidad, v_Dato)
	    {
		    var r = $resource ("", {}, { 'url': { method: "POST", url: $scope.Url} } );
		    var post = r.url(v_Dato);
		    return post.$promise;
	    };
	    
	    parametrosSP = function() {
	    	var par = {};
	    	
	    	par.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	    	par.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
	    	
	    	par.p_UsaReglasNegocio 	= "N";
	    	
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
	    	    	par.p_Fecha 		= $scope.data.Fecha;
	    			par.p_IdPortafolio 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
	    		break;
	    		case 'CLIENTE':
	    			par.p_IdModulo		= 'SOC';
	    			par.p_IdTipo 		= $scope.data.IdTipo;
	    	    	par.p_Fecha 		= $scope.data.Fecha;
	    			par.p_IdCliente 	= parametros.IdEntidad;
	    			par.p_FechaHasta	= ($scope.data.TipoBloqueoObj&&$scope.data.TipoBloqueoObj.CodigoTipoBloqueo.endsWith('TEMPO'))?$scope.data.FechaHasta:undefined;
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
					bcsComunes.ventanaOverlay(ventanaId,false);
					if (result.P_OUT_ERRORESCONTADOR){
						
						bcsComunes.ventanaMsg({ mensaje : "La operacion tiene errores",
							conflictos : util.parseServerResponse(result)}, ventanaId);
						
					}else{
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $scope.tituloModal,
							conflictos : util.parseServerResponse(result)}, ventanaId);
							
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(ventanaId,{res:'OK', result: result});
							bcsComunes.ventanaCerrar(ventanaId);
						});
					}
				},
				function(result){
					bcsComunes.ventanaOverlay(ventanaId,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
			);
		};
		*/
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());	