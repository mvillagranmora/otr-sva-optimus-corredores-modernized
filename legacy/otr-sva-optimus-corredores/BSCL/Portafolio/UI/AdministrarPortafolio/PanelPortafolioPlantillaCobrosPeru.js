/*************************************************************
 * Purpose	: Panel Portafolio Plantilla Cobros.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPlantillaCobrosPeruCtrl',
	function($scope,$translate,servicioSociodeNegocio,$stateParams,bcsComunes,servicioPortafolio){

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

		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		pPortCob.refreshGrids = function() {
			if ($("#PanelPortafolioPlantillaCobrosPeru").find("[kendo-grid]").length) 
			$.each($("#PanelPortafolioPlantillaCobrosPeru").find("[kendo-grid]"),function(key,val){
				if (val.data) val.data("kendo-grid").refresh();
			})
		}
		
		//Base
		pPortCob.winBase.editar = function(){
			pPortCob.winBase.modoLectura = false;
		};
		
		pPortCob.winBase.cancelar = function(){
			pPortCob.datosDespues.IdPerfilCobrosPeru 	= pPortCob.datosAntes.IdPerfilCobrosPeru;
			pPortCob.datosDespues.IdPerfilCobrosPeruDef = pPortCob.datosAntes.IdPerfilCobrosPeru;
			pPortCob.winBase.modoLectura 		 	= true;
		};

		pPortCob.winBase.guardar = function(idPerfilExtra){
			if (pPortCob.datosDespues.IdPerfilCobrosPeru) {
				var dataPortafolio = pPortCob.fn.datosGuardar(idPerfilExtra);
				bcsComunes.ventanaOverlay(pPortCob.idVentana,true);
				servicioPortafolio.updatePortafolio(dataPortafolio,function(result){
					bcsComunes.ventanaOverlay(pPortCob.idVentana,false);
					titulo = "";
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0){
						titulo = 'Portafolio modificado';
						pPortCob.winBase.modoLectura = true;
						pPortCob.datosAntes = _.clone(pPortCob.datosDespues);
						
						$scope.admPort.iniciar();
					}
					else if(result.P_OUT_ADVERTENCIASCONTADOR > 0 ) {
						titulo = "Operación con advertencias";
						pPortCob.winBase.modoLectura = true;
						pPortCob.datosDespues.IdPerfilCobrosPeru = pPortCob.datosAntes.IdPerfilCobrosPeru;
					}
					else {
						titulo = "Error al guardar";
					}
										
					bcsComunes.ventanaMsg({
						mensaje: titulo,
						conflictos:util.parseServerResponse(result)
					},pPortCob.idVentana);
				});
			}
			else {
				bcsComunes.msg("error","MODIFICAR PORTAFOLIO",'No ha seleccionado Plantilla');
			}
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
			if(idPerfilExtra) datos.p_RegistroCambio.push({CampoModificado:'IdPerfilCobrosPeruExtra',ValorNuevo: idPerfilExtra});
			else datos.p_RegistroCambio.push({CampoModificado:'IdPerfilCobrosPeru',ValorNuevo: pPortCob.datosDespues.IdPerfilCobrosPeru || '0'});
			return datos;
		};
		
		pPortCob.fn.iniciar = function(){
			pPortCob.datosAntes		= _.clone($scope.admPort.datos);
			pPortCob.datosDespues	= _.clone($scope.admPort.datos);
			pPortCob.datosDespues.IdPerfilCobrosPeru = pPortCob.datosAntes.IdPerfilCobrosPeru;
			pPortCob.datosDespues.IdPerfilCobrosPeruDef = pPortCob.datosAntes.IdPerfilCobrosPeru;
		};

		/*********************************************
		* FUNCIONES WATCH
		*********************************************/	
		$scope.$watch('pPortCob.ok',function(newval,oldval){
			if(newval === true && pPortCob.datosDespues.IdPerfilCobrosPeruExtra !== '0'){
				pPortCob.winBase.guardar(pPortCob.datosDespues.IdPerfilCobrosPeruExtra);
			}
		});
		
		$scope.$watch('pPortCob.finalizarOk',function(newval,oldval){
			if(newval){
				console.log("clic finalizar en directiva cobros");
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
