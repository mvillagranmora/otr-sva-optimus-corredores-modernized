/*************************************************************
 * Purpose	: Panel Portafolio Plantilla Limites.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPlantillaLimitesCtrl',
	function($scope, $translate, servicioSociodeNegocio, $stateParams, bcsComunes, servicioPortafolio, bcsServicioPlataforma){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var pPortLim 			= this;
		pPortLim.winBase		= {};
		pPortLim.winExtra		= {};
		pPortLim.cmbbx 			= {};
		pPortLim.ds 			= {};
		pPortLim.fn 			= {};		
		pPortLim.winBase.modoLectura 	= true;
		pPortLim.winExtra.modoLectura 	= true;
		pPortLim.datosAntes		= _.clone($scope.admPort.datos);
		pPortLim.datosDespues	= _.clone($scope.admPort.datos);
		pPortLim.idVentana		= bcsComunes.ventanaWinId($scope);
		pPortLim.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_LIMITES'];
		pPortLim.camposRequeridos = {};

		// Configuración de campos requeridos para formularios
		bcsServicioPlataforma.CamposRequeridosFormulario(pPortLim.CodigoFormulario, function (respuesta) {
			pPortLim.camposRequeridos = respuesta;
		});

		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/


		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/

		//Base
		pPortLim.winBase.editar = function(){
			pPortLim.winBase.modoLectura = false;
		};
		
		pPortLim.winBase.cancelar = function(){
			pPortLim.datosDespues.IdPlantillaLimite		= pPortLim.datosAntes.IdPerfilLimites;
			pPortLim.datosDespues.IdPlantillaLimiteDef	= pPortLim.datosAntes.IdPerfilLimites;
			pPortLim.winBase.modoLectura = true;
			$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPlantillaLimites' });
		};

		pPortLim.winBase.guardar = function (idPlantillaExtra) {
			var dataPortafolio = pPortLim.fn.datosGuardar(idPlantillaExtra);
			bcsComunes.ventanaOverlay(pPortLim.idVentana, true);
			servicioPortafolio.updatePortafolio(dataPortafolio, function (result) {
				var titulo = "";
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0) {
					titulo = 'Portafolio modificado';
					pPortLim.winBase.modoLectura = true;
					pPortLim.datosAntes = _.clone(pPortLim.datosDespues);
					$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPlantillaLimites' });
					$scope.admPort.iniciar();
				} else if (result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0) {
					titulo = "MODIFICAR PORTAFOLIO" + result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
					pPortLim.winBase.modoLectura = true;
					pPortLim.datosDespues.IdPlantillaLimite = pPortLim.datosAntes.IdPerfilLimites;
				} else if (result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0) {
					titulo = "MODIFICAR PORTAFOLIO" + result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion;
				} else {
					titulo = $translate.instant("UI_REVISE_LOG");
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos: util.parseServerResponse(result)
				}, pPortLim.idVentana);
				bcsComunes.ventanaOverlay(pPortLim.idVentana, false);
			}, function () {
				bcsComunes.ventanaOverlay(pPortLim.idVentana, false);
			});
		};

		//Extra, gatillan accion en directiva.
		pPortLim.winExtra.editar = function(){
			pPortLim.winExtra.modoLectura = false;
			pPortLim.accionExtra = 'E';
		};
		
		pPortLim.winExtra.cancelar = function(){
			pPortLim.winExtra.modoLectura = true;
			pPortLim.accionExtra = 'C';
		};
		
		pPortLim.winExtra.guardar = function() {
			pPortLim.winExtra.modoLectura = true;
			pPortLim.accionExtra = 'G';
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/


		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pPortLim.cmbbx.PlantillaLimitesBase={
			tipo : "combobox-plantilla-limites",
			filtros : {
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa: {field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa},
				IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'}
			},
		};
		/*********************************************
		 * GRILLAS
		 *********************************************/
		 
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pPortLim.fn.datosGuardar = function(idPlantillaExtra){
			var datos = {};
			datos.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			datos.p_IdUsuario		= $scope.$root.SesionIdUsuario;
			datos.p_IdModulo		= 'POR';
			datos.p_UsaReglasNegocio= 'N';
			
			datos.p_IdPortafolio	= pPortLim.datosDespues.IdPortafolio;
			datos.p_RegistroCambio	= [];
			// Se comenta para que solo se genere Evento Autorizacion por modificacion de Plantilla Base
			// y no por los limites de la Plantilla Extra
			/*if(idPlantillaExtra){ 
				datos.p_RegistroCambio.push({CampoModificado:'IdPerfilLimitesExtra',ValorNuevo: idPlantillaExtra});
			}
			else{
				datos.p_RegistroCambio.push({CampoModificado:'IdPerfilLimites',ValorNuevo: pPortLim.datosDespues.IdPlantillaLimite || '0'});
			}*/
			datos.p_RegistroCambio.push({CampoModificado:'IdPerfilLimites',ValorNuevo: pPortLim.datosDespues.IdPlantillaLimite || '0'});
			
			return datos;
		};
		
		pPortLim.fn.iniciar = function(){
			pPortLim.datosAntes		= _.clone($scope.admPort.datos);
			pPortLim.datosDespues	= _.clone($scope.admPort.datos);
			pPortLim.datosDespues.IdPlantillaLimite		= pPortLim.datosAntes.IdPerfilLimites;
			pPortLim.datosDespues.IdPlantillaLimiteDef	= pPortLim.datosAntes.IdPerfilLimites;
			pPortLim.datosDespues.IdPlantillaLimiteExtra= pPortLim.datosAntes.IdPerfilLimitesExtra;
		};
		pPortLim.avisarCambioPanel = function () {

			const sonIguales = _.isEqual(pPortLim.datosAntes.IdPerfilLimites, pPortLim.datosDespues.IdPlantillaLimite);
			console.log(sonIguales)
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPlantillaLimites' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPlantillaLimites' });
			}
		};
		pPortLim.obtenerDatosComparables = function (data) {
			return {
				IdPerfilLimites: data.IdPerfilLimites || '',
			};
		}

		
			

		/*********************************************
		* FUNCIONES WATCH
		*********************************************/
		// Se comenta para que solo se genere Evento Autorizacion por modificacion de Plantilla Base
		// y no por los limites de la Plantilla Extra
		/*$scope.$watch('pPortLim.ok',function(newval,oldval){
			if(newval === true && pPortLim.datosDespues.IdPlantillaLimiteExtra !== '0'){
				pPortLim.winBase.guardar(pPortLim.datosDespues.IdPlantillaLimiteExtra);
			}
		});*/


		
		$scope.$watchCollection('pPortLim.datosDespues', function () {
			const modoEdicion = !pPortLim.winBase.modoLectura;
			if (modoEdicion) {
				pPortLim.avisarCambioPanel();
			}
		});
		
		$scope.$watch('pPortLim.finalizarOk',function(newval,oldval){
			if(newval){
				pPortLim.winExtra.modoLectura = true;
			}
		});

		$scope.$watch('admPort.datos',function(newVal){
			if(newVal && newVal.IdPortafolio) {
				pPortLim.fn.iniciar();
			}
		});
		
		

		/*********************************************
		 * GO!!!
		 *********************************************/
		pPortLim.fn.iniciar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------