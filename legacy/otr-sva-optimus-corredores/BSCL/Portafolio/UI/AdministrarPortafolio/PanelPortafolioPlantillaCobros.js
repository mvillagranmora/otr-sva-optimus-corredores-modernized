/*************************************************************
 * Purpose	: Panel Portafolio Plantilla Cobros.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPlantillaCobrosCtrl',
	function($scope, $translate, servicioSociodeNegocio, $stateParams, bcsComunes, servicioPortafolio, bcsServicioPlataforma){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var pPortCob 			= this;
		pPortCob.winBase		= {};
		pPortCob.winExtra		= {};
		pPortCob.cmbbx 			= {};
		pPortCob.ds 			= {};
		pPortCob.fn 			= {};		
		pPortCob.winBase.modoLectura 	= true;
		pPortCob.winExtra.modoLectura 	= true;
		pPortCob.datosAntes		= _.clone($scope.admPort.datos);
		pPortCob.datosDespues	= _.clone($scope.admPort.datos);
		pPortCob.idVentana = bcsComunes.ventanaWinId($scope);
		pPortCob.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_COBROS'];
		pPortCob.camposRequeridos = {};

		// Configuración de campos requeridos para formularios
		bcsServicioPlataforma.CamposRequeridosFormulario(pPortCob.CodigoFormulario, function (respuesta) {
			pPortCob.camposRequeridos = respuesta;
		});
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		//Base
		pPortCob.winBase.editar = function(){
			pPortCob.winBase.modoLectura = false;
		};
		
		pPortCob.winBase.cancelar = function(){
			pPortCob.datosDespues.IdPerfilCobros = pPortCob.datosAntes.IdPerfilCobros;
			pPortCob.datosDespues.IdPerfilCobrosDef = pPortCob.datosAntes.IdPerfilCobros;
			pPortCob.winBase.modoLectura = true;
			$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilCobros' });
		};

		pPortCob.winBase.guardar = function (idPerfilExtra) {
			var dataPortafolio = pPortCob.fn.datosGuardar(idPerfilExtra);
			bcsComunes.ventanaOverlay(pPortCob.idVentana, true);
			servicioPortafolio.updatePortafolio(dataPortafolio, function (result) {
				titulo = "";
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
					titulo = 'Portafolio modificado';
					pPortCob.winBase.modoLectura = true;
					pPortCob.datosAntes = _.clone(pPortCob.datosDespues);
					$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilCobros' });
					$scope.admPort.iniciar();
				} else if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
					titulo = "Operación con advertencias";
					pPortCob.winBase.modoLectura = true;
					pPortCob.datosDespues.IdPerfilCobros = pPortCob.datosAntes.IdPerfilCobros;
				} else {
					titulo = "Error al guardar";
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos: util.parseServerResponse(result)
				}, pPortCob.idVentana);
				bcsComunes.ventanaOverlay(pPortCob.idVentana, false);
			});
		};

		//Extra, gatillan accion en directiva.
		pPortCob.winExtra.editar = function(){
			pPortCob.winExtra.modoLectura = false;
			pPortCob.accionExtra = 'E';
		};
		
		pPortCob.winExtra.cancelar = function(){
			pPortCob.winExtra.modoLectura = true;
			pPortCob.accionExtra = 'C';
		};
		
		pPortCob.winExtra.guardar = function() {
			pPortCob.winExtra.modoLectura = true;
			pPortCob.accionExtra = 'G';
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/


		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pPortCob.cmbbx.PerfilCobrosBase={
			tipo : "combobox-perfil-cobros",
			filtros : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				EsPerfilPersonalizado : {
					"field"   : "EsPerfilPersonalizado",
					"operator": "eq",
					"value"   : 'N',
			},
			EstadoPerfilCobros : {
				"field"   : "EstadoPerfilCobros",
				"operator": "eq",
				"value"   : 'ACT',
			}
			},
		};
		/*********************************************
		 * GRILLAS
		 *********************************************/
		 
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pPortCob.fn.datosGuardar = function(idPerfilExtra){
			var datos = {};
			datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.$root.SesionIdUsuario;
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'N';
			datos.p_IdPortafolio = pPortCob.datosDespues.IdPortafolio;
			datos.p_RegistroCambio = [];
			if(idPerfilExtra) datos.p_RegistroCambio.push({CampoModificado:'IdPerfilCobrosExtra',ValorNuevo: idPerfilExtra});
			else datos.p_RegistroCambio.push({CampoModificado:'IdPerfilCobros',ValorNuevo: pPortCob.datosDespues.IdPerfilCobros || '0'});
			return datos;
		};
		
		pPortCob.fn.iniciar = function(){
			pPortCob.datosAntes		= _.clone($scope.admPort.datos);
			pPortCob.datosDespues	= _.clone($scope.admPort.datos);
			pPortCob.datosDespues.IdPerfilCobros = pPortCob.datosAntes.IdPerfilCobros;
			pPortCob.datosDespues.IdPerfilCobrosDef = pPortCob.datosAntes.IdPerfilCobros;
		};

		pPortCob.avisarCambioPanel = function () {
			const sonIguales = _.isEqual(pPortCob.datosAntes.IdPerfilCobros, pPortCob.datosDespues.IdPerfilCobros);
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilCobros' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilCobros' });
			}
		};

		/*********************************************
		* FUNCIONES WATCH
		*********************************************/
		
		$scope.$watchCollection('pPortCob.datosDespues', function () {
			const modoEdicion = !pPortCob.winBase.modoLectura
			if (modoEdicion) {
				pPortCob.avisarCambioPanel();
			}
		});
		$scope.$watch('pPortCob.ok',function(newval,oldval){
			if(newval === true && pPortCob.datosDespues.IdPerfilCobrosExtra !== '0'){
				pPortCob.winBase.guardar(pPortCob.datosDespues.IdPerfilCobrosExtra);
			}
		});
		
		$scope.$watch('pPortCob.finalizarOk',function(newval,oldval){
			if(newval){
				pPortCob.winExtra.modoLectura = true;
			}
		});

		$scope.$watch('admPort.datos',function(newVal){
			if(newVal && newVal.IdPortafolio) {
				pPortCob.fn.iniciar();
			}
		});

		/*********************************************
		 * GO!!!
		 *********************************************/
		pPortCob.fn.iniciar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------
