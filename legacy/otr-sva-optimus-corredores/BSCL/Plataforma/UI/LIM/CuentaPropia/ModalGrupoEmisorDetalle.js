(function() {
	angular.module('ModuloPlataforma').controller('ModalGrupoEmisorDetalleCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mGED 		= this;
		mGED.idVentana	= bcsComunes.ventanaWinId($scope);
		mGED.parametros	= bcsComunes.ventanaParams(mGED.idVentana);
		mGED.domVentana	= $("[ventanaid='"+mGED.idVentana+"']");
		
		var lineaDetalle= _.clone(mGED.parametros.EmisorSel);
		
		mGED.win		= {};
		mGED.cmbbx		= {};
		mGED.respuesta	= {};
		mGED.respuesta.IdEstadoDef = $scope.$root.SesionValoresDefectoSistema.IdEstadoEmisorLimiteCuentaPropia; 
		
		mGED.IdBotonAceptar = 'ModalGrupoEmisorDetalle_aceptar' + mGED.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mGED.idVentana, $translate.instant('UI_MODIFICAR_EMISOR'));
			mGED.respuesta.IdEmisor		= lineaDetalle.IdEmisor || 0;
			mGED.respuesta.IdEmisorDef	= lineaDetalle.IdEmisor || 0;
			mGED.respuesta.IdEstado		= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoEmisorLimiteCuentaPropia;
			mGED.respuesta.IdEstadoDef	= lineaDetalle.IdEstado;
			mGED.respuesta.RazonSocial	= lineaDetalle.RazonSocial;
			mGED.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mGED.idVentana, $translate.instant('UI_AGREGAR_EMISOR'));
			mGED.win.modoEditar = false;
		}
		
		mGED.cancelar = function () {
			bcsComunes.ventanaData(mGED.idVentana, mGED.respuesta);
			bcsComunes.ventanaCerrar(mGED.idVentana);
		};
		
		
		mGED.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdGrupoEmisorDetalle) { // EDITAR
				datosGuardar.p_IdGrupoEmisorDetalle = lineaDetalle.IdGrupoEmisorDetalle;
				
				datosGuardar.p_RegistroCambio = [];
				if(mGED.respuesta.IdEstado !== lineaDetalle.IdEstado)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mGED.respuesta.IdEstado});
					
				if(datosGuardar.p_RegistroCambio.length === 0) return;
				
				bcsComunes.ventanaOverlay(mGED.idVentana,true);
				servicioLimites.modificarGrupoEmisorDetalle(datosGuardar,function (result){
					mGED.evaluaRespuestaSP(result, 'UI_EMISOR_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mGED.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdGrupoEmisor = mGED.parametros.GrupoEmisorSel.IdGrupoEmisor;
				datosGuardar.p_IdEmisor	= mGED.respuesta.IdEmisor;
				datosGuardar.p_IdEstado	= mGED.respuesta.IdEstado;
				
				bcsComunes.ventanaOverlay(mGED.idVentana,true);
				servicioLimites.crearGrupoEmisorDetalle(datosGuardar,function (result){
					mGED.evaluaRespuestaSP(result, 'UI_EMISOR_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mGED.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mGED.evaluaRespuestaSP = function(result, msj){
			console.log(result);
			
			bcsComunes.ventanaMsg({ mensaje : msj,
				conflictos : util.parseServerResponse(result)}, mGED.idVentana);
			
			mGED.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mGED.idVentana,false);
			bcsComunes.ventanaData(mGED.idVentana, mGED.respuesta);
			bcsComunes.ventanaCerrar(mGED.idVentana);
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mGED.cmbbx.Emisor={
			filtros : {
				IdEstado : {field: "IdEstado", operator: "eq" , value: "ACT" }
			}
		};
		
		mGED.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			},
		};
	});
}());