(function() {
	angular.module('ModuloPlataforma').controller('ModalLimitesCuentaPropiaEmisorCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mlCPE 		= this;
		mlCPE.idVentana	= bcsComunes.ventanaWinId($scope);
		mlCPE.parametros= bcsComunes.ventanaParams(mlCPE.idVentana);
		mlCPE.domVentana= $("[ventanaid='"+mlCPE.idVentana+"']");
		
		var lineaDetalle= _.clone(mlCPE.parametros.FilaSel);
		
		mlCPE.win		= {};
		mlCPE.cmbbx		= {};
		mlCPE.respuesta	= {};
		mlCPE.respuesta.IdEstadoDef	= $scope.$root.SesionValoresDefectoSistema.IdEstadoEmisorLimiteCuentaPropia; 
		
		mlCPE.IdBotonAceptar = 'ModalLimitesCuentaPropiaEmisor_aceptar' + mlCPE.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mlCPE.idVentana, $translate.instant('UI_MODIFICAR_EMISOR'));
			mlCPE.respuesta.IdEmisor	= lineaDetalle.IdEmisor || 0;
			mlCPE.respuesta.IdEmisorDef	= lineaDetalle.IdEmisor || 0;
			mlCPE.respuesta.IdEstado	= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoEmisorLimiteCuentaPropia;
			mlCPE.respuesta.IdEstadoDef	= lineaDetalle.IdEstado;
			mlCPE.respuesta.RazonSocial	= lineaDetalle.RazonSocial;
			mlCPE.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mlCPE.idVentana, $translate.instant('UI_AGREGAR_EMISOR'));
			mlCPE.win.modoEditar = false;
		}
		
		mlCPE.cancelar = function () {
			bcsComunes.ventanaData(mlCPE.idVentana, mlCPE.respuesta);
			bcsComunes.ventanaCerrar(mlCPE.idVentana);
		};
		
		
		mlCPE.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdLimiteEmisor) { // EDITAR
				datosGuardar.p_IdLimiteEmisor	= lineaDetalle.IdLimiteEmisor;
				
				datosGuardar.p_RegistroCambio = [];
				if(mlCPE.respuesta.IdEstado !== lineaDetalle.IdEstado)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mlCPE.respuesta.IdEstado});
					
				if(datosGuardar.p_RegistroCambio.length === 0) return;
				
				bcsComunes.ventanaOverlay(mlCPE.idVentana,true);
				servicioLimites.modificarLimiteEmisor(datosGuardar,function (result){
					mlCPE.evaluaRespuestaSP(result, 'UI_EMISOR_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mlCPE.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdLimite	= mlCPE.parametros.LimiteSeleccionado.IdLimite;
				datosGuardar.p_IdEmisor	= mlCPE.respuesta.IdEmisor;
				datosGuardar.p_IdEstado	= mlCPE.respuesta.IdEstado;
				
				bcsComunes.ventanaOverlay(mlCPE.idVentana,true);
				servicioLimites.crearLimiteEmisor(datosGuardar,function (result){
					mlCPE.evaluaRespuestaSP(result, 'UI_EMISOR_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mlCPE.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mlCPE.evaluaRespuestaSP = function(result, msj){
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
			
			mlCPE.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mlCPE.idVentana,false);
			bcsComunes.ventanaData(mlCPE.idVentana, mlCPE.respuesta);
			bcsComunes.ventanaCerrar(mlCPE.idVentana);
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mlCPE.cmbbx.Emisor={
			filtros : {
				IdEstado : {field: "IdEstado", operator: "eq" , value: "ACT" }
			}
		};
		
		mlCPE.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			},
		};
	});
}());