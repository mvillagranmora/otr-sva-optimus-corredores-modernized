/*************************************************************
 * Purpose	: Administrar Portafolio Autorizaciones (Modal)
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('AdministrarPortafolioAutorizacionesModalCrearCtrl',
	function($scope, servicioPortafolio, bcsComunes, $translate, bcsServicioPlataforma) {
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/
		
		var apaCreMod = this;
		apaCreMod.ds = {};
		var idVentana 					= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros 					= bcsComunes.ventanaParams(idVentana);
		apaCreMod.IdCliente				= parametros.IdCliente;
		apaCreMod.IdPortafolio			= parametros.IdPortafolio;
		apaCreMod.IdPersona				= parametros.IdPersona;
		apaCreMod.Autorizado			= $.extend({},parametros.IdAutorizado);
		apaCreMod.AutorizadoOriginal	= $.extend({},parametros.IdAutorizado);
		apaCreMod.CodigoFormulario		= parametros.CodigoFormulario;

		apaCreMod.camposRequeridos = {};
		bcsServicioPlataforma.CamposRequeridosFormulario(apaCreMod.CodigoFormulario, function (respuesta) {
			apaCreMod.camposRequeridos = respuesta;
		});
		
		if ($scope.SesionLocale =="PE") {
			apaCreMod.NumeroRegistroPublico = parametros.NumeroRegistroPublico;
			apaCreMod.NumeroAsiento		= parametros.NumeroAsiento;
			apaCreMod.FichaPartida		= parametros.FichaPartida;
			apaCreMod.TipoFirma 		= parametros.TipoFirma;
			apaCreMod.EtiquetasTipoFirma	= {INDIVIDUAL: "UI_INDIVIDUAL", CONJUNTA : "UI_CONJUNTA"};
			apaCreMod.ValoresTipoFirma		= {INDIVIDUAL: true, CONJUNTA : false};
			
		}
		
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/
		if(parametros.IdAutorizado){
			//apaCreMod.titulo = 'UI_MODIFICAR_AUTORIZACION';  //modificacion
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_AUTORIZACION'));
		}
		else{
			//apaCreMod.titulo = 'UI_AGREGAR_AUTORIZACION';
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_AUTORIZACION'));
			apaCreMod.Autorizado = {};
			apaCreMod.Autorizado.IdEstado = 'PEN';  // Pendiente hasta que se autorice
		}
		
		apaCreMod.aceptar = function() {
		var datos = {
			p_IdEmpresa			: $scope.SesionIdEmpresa,
			p_IdUsuario			: $scope.SesionIdUsuario,
			p_IdModulo			: 'SOC',
			p_UsaReglasNegocio	: 'S',
			p_IdPortafolio		: apaCreMod.IdPortafolio,
			p_IdPersona			: apaCreMod.RelacionadoObj.IdPersonaDestino,
			p_IdEstado			: apaCreMod.Autorizado.IdEstado,
			p_IdTipoAutorizado 	: apaCreMod.TipoAutObj.IdTipoAutorizado,
		};
		
		if ($scope.SesionLocale =="PE") {
			datos.p_NumeroRegistroPublico = apaCreMod.NumeroRegistroPublico;
			datos.p_Asiento = apaCreMod.NumeroAsiento;
			datos.p_FichaPartida = apaCreMod.FichaPartida;
			datos.p_TipoFirma = apaCreMod.ValoresTipoFirma.INDIVIDUAL?"INDIVIDUAL":"CONJUNTA";
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		servicioPortafolio.crearAutorizadoOrden(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				var titulo = ""; 
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					titulo = "La operacion tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR){
					titulo = "Error al agregar Autorizado";
				}
				else
					titulo = "Autorizado agregado";
				
				var idd = bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
				
				bcsComunes.ventanaPromesa(idd).then(function(result2){
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
					}
				})
				
			},
			function(result){
				bcsComunes.ventanaMsg({
					mensaje: "Error al agregar Autorizado",
					conflictos:util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
	};
		
		apaCreMod.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
	/*********************************************
	 * DATASOURCES
	 *********************************************/
		
		
	/*********************************************
	 * COMBOBOX
	 *********************************************/
		apaCreMod.conf = {};
//		apaCreMod.conf.oConfiguracionSocioNegocio={
//			filtros: {
//				IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
//				EsCliente : {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
//			},
//		};
		
		apaCreMod.conf.oConfiguracionRelacionados={
				filtros: {
					IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
					IdPersonaOrigen : {"field"   : "IdPersonaOrigen","operator": "eq", "value" : apaCreMod.IdPersona},
					Administracion : {"field"   : "Administracion","operator": "eq", "value" : "RELACIONADOS"},
					IdEstado : {"field" : "IdEstado", "operator": "eq", "value": "ACT"}
				},
			};
		
		
		apaCreMod.conf.oConfiguracionTipoAutorizado={};
		
	/*********************************************
	 * GRILLAS
	 *********************************************/
		
	/*********************************************
	 * FUNCIONES
	 ********************************************/

	});
}());
//------------------------- FIN CONTROLLER --------------------------