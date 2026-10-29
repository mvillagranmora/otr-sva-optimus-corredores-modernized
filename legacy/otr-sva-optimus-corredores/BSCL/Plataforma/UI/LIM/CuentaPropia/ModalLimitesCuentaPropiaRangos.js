(function() {
	angular.module('ModuloPlataforma').controller('ModalLimitesCuentaPropiaRangosCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mlCPR 		= this;
		mlCPR.idVentana	= bcsComunes.ventanaWinId($scope);
		mlCPR.parametros= bcsComunes.ventanaParams(mlCPR.idVentana);
		mlCPR.domVentana= $("[ventanaid='"+mlCPR.idVentana+"']");
		
		var lineaDetalle= _.clone(mlCPR.parametros.FilaSel);
		
		mlCPR.win		= {};
		mlCPR.cmbbx		= {};
		mlCPR.respuesta	= {};
		mlCPR.respuesta.IdEstadoDef	= $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
		mlCPR.respuesta.IdMoneda	= mlCPR.parametros.LimiteSeleccionado.IdMoneda;
		mlCPR.respuesta.Moneda		= mlCPR.parametros.LimiteSeleccionado.Moneda;
		
		mlCPR.win.MontoDecimal	= (util.bcs_formato(0,'ORD','MONTO',mlCPR.respuesta.IdMoneda,'','')).cantidadDecimales;
		mlCPR.win.TasaDecimal	= (util.bcs_formato(0,'ORD','TASA', mlCPR.respuesta.IdMoneda,'','')).cantidadDecimales;
		
		mlCPR.IdBotonAceptar = 'ModalLimitesCuentaPropiaRangos_aceptar' + mlCPR.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mlCPR.idVentana, $translate.instant('UI_MODIFICAR_RANGO_MONTO_PLAZO'));
			mlCPR.respuesta.MontoDesde	= lineaDetalle.MontoDesde || 0;
			mlCPR.respuesta.MontoHasta	= lineaDetalle.MontoHasta || 0;
			mlCPR.respuesta.PlazoDesde	= lineaDetalle.PlazoDesde || 0;
			mlCPR.respuesta.PlazoHasta	= lineaDetalle.PlazoHasta || 0;
			mlCPR.respuesta.Tasa		= lineaDetalle.Tasa || 0;
			
			mlCPR.respuesta.PuntosTasaMenos	= lineaDetalle.PuntosTasaMenos || 0;
			mlCPR.respuesta.PuntosTasaMas	= lineaDetalle.PuntosTasaMas || 0;
			mlCPR.respuesta.TasaFinalMenos	= lineaDetalle.TasaFinalMenos || 0;
			mlCPR.respuesta.TasaFinalMas	= lineaDetalle.TasaFinalMas || 0;
			
			mlCPR.respuesta.PuntosTasaMenosAgente	= lineaDetalle.PuntosTasaMenosAgente || 0;
			mlCPR.respuesta.PuntosTasaMasAgente		= lineaDetalle.PuntosTasaMasAgente || 0;
			mlCPR.respuesta.TasaFinalMenosAgente	= lineaDetalle.TasaFinalMenosAgente || 0;
			mlCPR.respuesta.TasaFinalMasAgente		= lineaDetalle.TasaFinalMasAgente || 0;
			
			mlCPR.respuesta.IdEstado	= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
			mlCPR.respuesta.IdEstadoDef	= lineaDetalle.IdEstado;
			mlCPR.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mlCPR.idVentana, $translate.instant('UI_AGREGAR_RANGO_MONTO_PLAZO'));
			
			mlCPR.respuesta.MontoDesde = mlCPR.respuesta.MontoHasta = 0;
			mlCPR.respuesta.PlazoDesde = mlCPR.respuesta.PlazoHasta = 0;
			mlCPR.respuesta.Tasa = 0; 
			mlCPR.respuesta.PuntosTasaMenos	= mlCPR.respuesta.PuntosTasaMas = 0;
			mlCPR.respuesta.TasaFinalMenos	= mlCPR.respuesta.TasaFinalMas = 0;
			mlCPR.respuesta.PuntosTasaMenosAgente= mlCPR.respuesta.PuntosTasaMasAgente = 0;
			mlCPR.respuesta.TasaFinalMenosAgente = mlCPR.respuesta.TasaFinalMasAgente = 0;
			mlCPR.win.modoEditar = false;
		}
		
		mlCPR.cancelar = function () {
			bcsComunes.ventanaData(mlCPR.idVentana, mlCPR.respuesta);
			bcsComunes.ventanaCerrar(mlCPR.idVentana);
		};
		
		
		mlCPR.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdRangosMontoPlazo) { // EDITAR
				datosGuardar.p_IdRangosMontoPlazo = lineaDetalle.IdRangosMontoPlazo;
				
				datosGuardar.p_RegistroCambio = [];
				var tasa = typeof mlCPR.respuesta.Tasa === 'string' ? Number(mlCPR.respuesta.Tasa.replace(',','.')) : mlCPR.respuesta.Tasa; 
				if(tasa !== lineaDetalle.Tasa)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'Tasa',ValorNuevo:tasa});
				if(mlCPR.respuesta.PuntosTasaMenos !== lineaDetalle.PuntosTasaMenos)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'PuntosTasaMenos',ValorNuevo:mlCPR.respuesta.PuntosTasaMenos});
				if(mlCPR.respuesta.PuntosTasaMas !== lineaDetalle.PuntosTasaMas)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'PuntosTasaMas',ValorNuevo:mlCPR.respuesta.PuntosTasaMas});
				if(mlCPR.respuesta.PuntosTasaMenosAgente !== lineaDetalle.PuntosTasaMenosAgente)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'PuntosTasaMenosAgente',ValorNuevo:mlCPR.respuesta.PuntosTasaMenosAgente});
				if(mlCPR.respuesta.PuntosTasaMasAgente !== lineaDetalle.PuntosTasaMasAgente)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'PuntosTasaMasAgente',ValorNuevo:mlCPR.respuesta.PuntosTasaMasAgente});
				if(mlCPR.respuesta.IdEstado !== lineaDetalle.IdEstado)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mlCPR.respuesta.IdEstado});
					
				if(datosGuardar.p_RegistroCambio.length === 0) return;
				
				bcsComunes.ventanaOverlay(mlCPR.idVentana,true);
				servicioLimites.modificarLimiteRangos(datosGuardar,function (result){
					mlCPR.evaluaRespuestaSP(result, 'UI_RANGO_MONTO_PLAZO_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mlCPR.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdLimite		= mlCPR.parametros.LimiteSeleccionado.IdLimite;
				datosGuardar.p_MontoDesde	= mlCPR.respuesta.MontoDesde;
				datosGuardar.p_MontoHasta	= mlCPR.respuesta.MontoHasta;
				datosGuardar.p_PlazoDesde	= mlCPR.respuesta.PlazoDesde;
				datosGuardar.p_PlazoHasta	= mlCPR.respuesta.PlazoHasta;
				datosGuardar.p_Tasa			= mlCPR.respuesta.Tasa;
				datosGuardar.p_PuntosTasaMenos		= mlCPR.respuesta.PuntosTasaMenos;
				datosGuardar.p_PuntosTasaMas		= mlCPR.respuesta.PuntosTasaMas;
				datosGuardar.p_PuntosTasaMenosAgente= mlCPR.respuesta.PuntosTasaMenosAgente;
				datosGuardar.p_PuntosTasaMasAgente	= mlCPR.respuesta.PuntosTasaMasAgente;
				datosGuardar.p_IdEstado		= mlCPR.respuesta.IdEstado;
				
				bcsComunes.ventanaOverlay(mlCPR.idVentana,true);
				servicioLimites.crearLimiteRangos(datosGuardar,function (result){
					mlCPR.evaluaRespuestaSP(result, 'UI_RANGO_MONTO_PLAZO_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mlCPR.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mlCPR.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","LIMITES CUENTA PROPIA",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","LIMITES CUENTA PROPIA",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","LIMITES CUENTA PROPIA",result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			}
			else bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			mlCPR.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mlCPR.idVentana,false);
			bcsComunes.ventanaData(mlCPR.idVentana, mlCPR.respuesta);
			bcsComunes.ventanaCerrar(mlCPR.idVentana);
		};
		
		
		$scope.$watch('mlCPR.respuesta.Tasa', function (newValue, oldValue){
			if(newValue !== oldValue){
				if(newValue >= mlCPR.respuesta.PuntosTasaMenos){
					mlCPR.respuesta.TasaFinalMenos = newValue - mlCPR.respuesta.PuntosTasaMenos;
				}
				else{
					mlCPR.respuesta.TasaFinalMenos = 0;
					mlCPR.respuesta.PuntosTasaMenos= 0;
				}
				
				if(mlCPR.respuesta.Tasa >= mlCPR.respuesta.PuntosTasaMenosAgente){
					mlCPR.respuesta.TasaFinalMenosAgente = mlCPR.respuesta.Tasa - mlCPR.respuesta.PuntosTasaMenosAgente;
				}
				else{
					mlCPR.respuesta.TasaFinalMenosAgente = 0;
					mlCPR.respuesta.PuntosTasaMenosAgente= 0;
				}
				mlCPR.respuesta.TasaFinalMas		= mlCPR.respuesta.Tasa + mlCPR.respuesta.PuntosTasaMas;
				mlCPR.respuesta.TasaFinalMasAgente	= mlCPR.respuesta.Tasa + mlCPR.respuesta.PuntosTasaMasAgente;
			}
		});
		$scope.$watch('mlCPR.respuesta.PuntosTasaMenos', function (newValue, oldValue){
			if(newValue !== oldValue){
				if(mlCPR.respuesta.Tasa >= mlCPR.respuesta.PuntosTasaMenos){
					mlCPR.respuesta.TasaFinalMenos = mlCPR.respuesta.Tasa - mlCPR.respuesta.PuntosTasaMenos;
				}
				else{
					mlCPR.respuesta.TasaFinalMenos = 0;
					mlCPR.respuesta.PuntosTasaMenos= 0;
				}
			}
		});
		$scope.$watch('mlCPR.respuesta.PuntosTasaMas', function (newValue, oldValue){
			if(newValue !== oldValue){
				mlCPR.respuesta.TasaFinalMas = mlCPR.respuesta.Tasa + mlCPR.respuesta.PuntosTasaMas;
			}
		});
		$scope.$watch('mlCPR.respuesta.PuntosTasaMenosAgente', function (newValue, oldValue){
			if(newValue !== oldValue){
				if(mlCPR.respuesta.Tasa >= mlCPR.respuesta.PuntosTasaMenosAgente){
					mlCPR.respuesta.TasaFinalMenosAgente = mlCPR.respuesta.Tasa - mlCPR.respuesta.PuntosTasaMenosAgente;
				}
				else{
					mlCPR.respuesta.TasaFinalMenosAgente = 0;
					mlCPR.respuesta.PuntosTasaMenosAgente= 0;
				}
			}
		});
		$scope.$watch('mlCPR.respuesta.PuntosTasaMasAgente', function (newValue, oldValue){
			if(newValue !== oldValue){
				mlCPR.respuesta.TasaFinalMasAgente = mlCPR.respuesta.Tasa + mlCPR.respuesta.PuntosTasaMasAgente;
			}
		});
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mlCPR.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			},
		};
	});
}());