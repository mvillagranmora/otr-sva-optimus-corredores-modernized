/*************************************************************
 * Purpose	: Panel Portafolio Plantilla Productos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPlantillaProductosCtrl',
	function($scope, $translate, servicioSociodeNegocio, $stateParams, bcsComunes, servicioPortafolio, bcsServicioPlataforma){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var pPortProd 			= this;
		pPortProd.winBase		= {};
		pPortProd.winExtra		= {};
		pPortProd.cmbbx 		= {};
		pPortProd.ds 			= {};
		pPortProd.fn 			= {};		
		pPortProd.winBase.modoLectura 	= true;
		pPortProd.winExtra.modoLectura 	= true;
		pPortProd.datosAntes			= _.clone($scope.admPort.datos);
		pPortProd.datosDespues			= _.clone($scope.admPort.datos);
		pPortProd.idVentana = bcsComunes.ventanaWinId($scope);
		pPortProd.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_PERFIL_PRODUCTOS'];
		pPortProd.camposRequeridos = {};

		// Configuración de campos requeridos
		bcsServicioPlataforma.CamposRequeridosFormulario(pPortProd.CodigoFormulario, function (respuesta) {
			pPortProd.camposRequeridos = respuesta;
		});
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/


		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/

		//Base
		pPortProd.winBase.editar = function(){
			pPortProd.winBase.modoLectura = false;
		};
		
		pPortProd.winBase.cancelar = function(){
			pPortProd.datosDespues.IdPerfilProductos = pPortProd.datosAntes.IdPerfilProductos;
			pPortProd.datosDespues.IdPerfilProductosDef = pPortProd.datosAntes.IdPerfilProductos;
			pPortProd.winBase.modoLectura = true;
			$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilProductos' });
		};

		pPortProd.winBase.guardar = function (idPerfilExtra) {
			var dataPortafolio = pPortProd.fn.datosGuardar(idPerfilExtra);
			bcsComunes.ventanaOverlay(pPortProd.idVentana, true);
			servicioPortafolio.updatePortafolio(dataPortafolio, function (result) {
				var titulo = "";
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
					titulo = "Portafolio modificado";
					pPortProd.winBase.modoLectura = true;
					pPortProd.datosAntes = _.clone(pPortProd.datosDespues);
					$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilProductos' });
					$scope.admPort.iniciar();

				} else if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
					titulo = "Operación con advertencias";
					pPortProd.winBase.modoLectura = true;
					pPortProd.datosDespues.IdPerfilProductos = pPortProd.datosAntes.IdPerfilProductos;
				} else {
					titulo = "Error al guardar";
				}

				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos: util.parseServerResponse(result)
				}, pPortProd.idVentana);
				bcsComunes.ventanaOverlay(pPortProd.idVentana, false);
			});
		};

		//Extra, gatillan accion en directiva.
		pPortProd.winExtra.editar = function(){
			pPortProd.winExtra.modoLectura = false;
			pPortProd.accionExtra = 'E';
		};
		
		pPortProd.winExtra.cancelar = function(){
			pPortProd.winExtra.modoLectura = true;
			pPortProd.accionExtra = 'C';
		};
		
		pPortProd.winExtra.guardar = function() {
			pPortProd.winExtra.modoLectura = true;
			pPortProd.accionExtra = 'G';
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/


		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pPortProd.cmbbx.PerfilProductosBase={
			tipo : "combobox-perfil-productos",
			filtros : {
				IdCategoriaInversionista : {
						"field"   : "IdCategoriaInversionista",
						"operator": "eq",
						"value"   : pPortProd.datosAntes.IdCategoriaInversionista,
				},
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				EsPerfilPersonalizado : {
					"field"   : "EsPerfilPersonalizado",
					"operator": "eq",
					"value"   : 'N',
				}
			},
		};
		/*********************************************
		 * GRILLAS
		 *********************************************/
		 
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pPortProd.fn.datosGuardar = function(idPerfilExtra){
			var datos = {};
			datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.$root.SesionIdUsuario;
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'N';
			datos.p_IdPortafolio = pPortProd.datosDespues.IdPortafolio;
			datos.p_RegistroCambio = [];
			if(idPerfilExtra) datos.p_RegistroCambio.push({CampoModificado:'IdPerfilProductosExtra',ValorNuevo: idPerfilExtra});
			else datos.p_RegistroCambio.push({CampoModificado:'IdPerfilProductos',ValorNuevo: pPortProd.datosDespues.IdPerfilProductos || '0'});
			return datos;
		};
		
		pPortProd.fn.iniciar = function(){
			pPortProd.datosAntes		= _.clone($scope.admPort.datos);
			pPortProd.datosDespues	= _.clone($scope.admPort.datos);
			pPortProd.datosDespues.IdPerfilProductos = pPortProd.datosAntes.IdPerfilProductos;
			pPortProd.datosDespues.IdPerfilProductosDef = pPortProd.datosAntes.IdPerfilProductos;
		};

		pPortProd.avisarCambioPanel = function () {
			const sonIguales = _.isEqual(pPortProd.datosAntes.IdPerfilProductos, pPortProd.datosDespues.IdPerfilProductos);
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilProductos' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilProductos' });
			}
		};

		/*********************************************
		* FUNCIONES WATCH
		*********************************************/
		
		$scope.$watchCollection('pPortProd.datosDespues', function () {
			const modoEdicion = !pPortProd.winBase.modoLectura
			if (modoEdicion) {
				pPortProd.avisarCambioPanel();
			}
		});
		$scope.$watch('pPortProd.ok',function(newval,oldval){
			if(newval === true && pPortProd.datosDespues.IdPerfilProductosExtra !== '0'){
				pPortProd.winBase.guardar(pPortProd.datosDespues.IdPerfilProductosExtra);
			}
		});
		
		$scope.$watch('pPortProd.finalizarOk',function(newval,oldval){
			if(newval){
				pPortProd.winExtra.modoLectura = true;
			}
		});

		$scope.$watch('admPort.datos',function(newVal){
			if(newVal && newVal.IdPortafolio) {
				pPortProd.fn.iniciar();
			}
		});

		$scope.$watch('pPortProd.AllPlantillasProductos',function(newVal){
			if(newVal==true){
				$scope.pPortProd.cmbbx.PerfilProductosBase.filtros.IdCategoriaInversionista.value = "";
			}else{
				$scope.pPortProd.cmbbx.PerfilProductosBase.filtros.IdCategoriaInversionista.value = $scope.pPortProd.datosAntes.IdCategoriaInversionista;
			}
		});

		/*********************************************
		 * GO!!!
		 *********************************************/
		pPortProd.fn.iniciar();
	});
}());
//------------------------- FIN CONTROLLER --------------------------