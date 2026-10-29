/*************************************************************
 * Purpose	: Linea Sobregiro (Modal)
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('ModalPortafolioLineaSobregiroCtrl',function ($scope,bcsComunes,servicioPlantillaLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var mpLS 		= this;
		mpLS.idVentana 	= bcsComunes.ventanaWinId($scope);
		mpLS.parametros = bcsComunes.ventanaParams(mpLS.idVentana);
		mpLS.domVentana	= $("[ventanaid='"+mpLS.idVentana+"']");
		
		var lineaDetalle		= _.clone(mpLS.parametros.LinSobGirSel);
		var IdPlantillaLimite	= mpLS.parametros.DataDirectiva.IdPlantillaLimite;
		
		mpLS.win		= {};
		mpLS.cmbbx		= {};
		mpLS.respuesta	= {};
		mpLS.respuesta.IdMonedaDef	= $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteLineaSobregiro;
		mpLS.respuesta.EstadoDef	= $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteLineaSobregiro;
		
		mpLS.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',$scope.$root.SesionIdMonedaSistema,'','')).cantidadDecimales;
		
		mpLS.IdBotonAceptar = 'ModalPortafolioLineaSobregiro_aceptar' + mpLS.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mpLS.idVentana, $translate.instant('UI_MODIFICAR_LIMITE_LINEA_SOBREGIRO'));
			mpLS.respuesta.Nombre		= lineaDetalle.Nombre || '';
			mpLS.respuesta.IdMoneda		= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteLineaSobregiro;
			mpLS.respuesta.IdMonedaDef	= lineaDetalle.IdMoneda;
			mpLS.respuesta.MontoTotal	= lineaDetalle.MontoTotal || 0;
			mpLS.respuesta.Estado		= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteLineaSobregiro;
			mpLS.respuesta.EstadoDef	= lineaDetalle.IdEstado;
		}
		else{
			bcsComunes.ventanaTitulo(mpLS.idVentana, $translate.instant('UI_AGREGAR_LIMITE_LINEA_SOBREGIRO'));
		}
		
		
		mpLS.cancelar = function () {
			bcsComunes.ventanaData(mpLS.idVentana, mpLS.respuesta);
			bcsComunes.ventanaCerrar(mpLS.idVentana);
		};
		
		
		mpLS.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "LIM";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			datosGuardar.p_IdTipoEntidad	= 'PORTAFOLIO';
			datosGuardar.p_IdEntidadOrigen	= mpLS.parametros.DataDirectiva.IdPortafolio;
			
			if(lineaDetalle && lineaDetalle.IdLimite) { // EDITAR
				datosGuardar.p_IdLimite	= lineaDetalle.IdLimite;
				
				datosGuardar.p_RegistroCambio = [];
				if(mpLS.respuesta.Nombre	!== lineaDetalle.Nombre)	datosGuardar.p_RegistroCambio.push({CampoModificado:'Nombre',ValorNuevo:mpLS.respuesta.Nombre});
				if(mpLS.respuesta.IdMoneda	!== lineaDetalle.IdMoneda)	{
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMoneda',ValorNuevo:mpLS.respuesta.IdMoneda});
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMonedaCalculo',ValorNuevo:mpLS.respuesta.IdMoneda});
				}
				if(mpLS.respuesta.MontoTotal!== lineaDetalle.MontoTotal)datosGuardar.p_RegistroCambio.push({CampoModificado:'MontoTotal',ValorNuevo:mpLS.respuesta.MontoTotal});
				if(mpLS.respuesta.Estado	!== lineaDetalle.IdEstado)	datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mpLS.respuesta.Estado});
					
				if(datosGuardar.p_RegistroCambio.length === 0) return;
				
				bcsComunes.ventanaOverlay(mpLS.idVentana,true);
				servicioPlantillaLimites.modificarLimite(datosGuardar,function (result){
					mpLS.evaluaRespuestaSP(result, 'UI_LIMITE_LINEA_SOBREGIRO_MODIFICADO');
				}, function(resError){
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_REVISE_LOG"),
						conflictos:util.parseServerResponse(resError)
					},mpLS.idVentana);
					console.log(result);
					bcsComunes.ventanaOverlay(mpLS.idVentana,false);
				});
			}
			else { // CREAR
				/*CELCONDOR-58 - Configurar una linea por Moneda*/
				var boolLineaExiste = false;
				$.each(mpLS.parametros.LinSobGirSel,function(key,value){
					if(value.IdMoneda === mpLS.respuesta.IdMoneda){
						boolLineaExiste = true;
						return false;
					}
				});
				if(boolLineaExiste){
					bcsComunes.ventanaMsgErr($translate.instant('UI_MONEDA_LINEA_SOBREGIRO_EXISTE'),mpLS.idVentana);
					return;
				}
				/*CELCONDOR-58 - Configurar una linea por Moneda*/
				datosGuardar.p_IdPlantillaLimite= mpLS.parametros.DataDirectiva.IdPlantillaLimite;
				datosGuardar.p_IdTipoLimite		= mpLS.parametros.DataDirectiva.Modo === 'LS' ? 'LIMPORTSOBREGIRO' : '';
				datosGuardar.p_Nombre			= mpLS.respuesta.Nombre;
				datosGuardar.p_IdMoneda			= mpLS.respuesta.IdMoneda;
				datosGuardar.p_IdMonedaCalculo  = mpLS.respuesta.IdMoneda;
				datosGuardar.p_MontoTotal		= Number(mpLS.respuesta.MontoTotal);
				datosGuardar.p_IdEstado			= mpLS.respuesta.Estado;
				datosGuardar.p_UI_Limite		= mpLS.parametros.DataDirectiva.UILimite;
				
				bcsComunes.ventanaOverlay(mpLS.idVentana,true);
				servicioPlantillaLimites.crearLimite(datosGuardar,function (result){
					mpLS.evaluaRespuestaSP(result, 'UI_LIMITE_LINEA_SOBREGIRO_CREADO');
				}, function(resError){
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant("UI_REVISE_LOG"),
						conflictos:util.parseServerResponse(resError)
					},mpLS.idVentana);
					console.log(result);
					bcsComunes.ventanaOverlay(mpLS.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mpLS.cmbbx.Moneda={
			filtros : {
				IdEstadoMonedaTesoreria : { field: "IdEstado", operator: "eq", value: "ACT" }
			},
		};
		
		/*CELCONDOR-58 - Configurar una linea por Moneda*/
		/*
		if(mpLS.parametros.LinSobGirSel !== undefined){
			$scope.filtroMoneda = new Array();
			$scope.IdEstadoMonedaTesoreria = { field: "IdEstado", operator: "eq", value: "ACT" };
			$.each(mpLS.parametros.LinSobGirSel,function(key,value){
				$scope.filtroMoneda.push({field : "IdMoneda" , operator : "neq" , value : value.IdMoneda});
			});

			mpLS.cmbbx.Moneda = {
				filtros : {
					complejo: [$scope.IdEstadoMonedaTesoreria,$scope.filtroMoneda],
					logic: "and",
					filters: [{
						logic: "or",
						filters: $scope.filtroMoneda
					}]
				}
			};
		}
		*/
		/*CELCONDOR-58 - Configurar una linea por Moneda*/
		
		mpLS.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			},
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mpLS.evaluaRespuestaSP = function(result, msj){
			
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0){
				msj = $translate.instant(msj);
			}
			else{
				msj = $translate.instant("UI_REVISE_LOG");
			}
			console.log(result);
			
			bcsComunes.ventanaMsg({
				mensaje: msj,
				conflictos:util.parseServerResponse(result)
			},mpLS.idVentana);
			
			mpLS.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mpLS.idVentana,false);
			bcsComunes.ventanaData(mpLS.idVentana, mpLS.respuesta);
			bcsComunes.ventanaCerrar(mpLS.idVentana);
		};
		
		$scope.$watch('mpLS.respuesta.IdMoneda', function (newValue, oldValue){
			if(newValue !== oldValue)
				mpLS.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',mpLS.respuesta.IdMoneda,'','')).cantidadDecimales;
		});
		
	});
}());