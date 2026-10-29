/*************************************************************
 * Purpose	: Modal Grupo Direccion Electronica
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalGrupoDireccionElectronicaCtrl',
	function($scope,$resource,bcsComunes, bcsServicioPlataforma) {
		var mgdie = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mgdie.data = {};
		mgdie.IdPersona = parametros.IdPersona;

		mgdie.CodigoFormulario = parametros.CodigoFormulario;
		mgdie.camposRequeridos = {};
		bcsServicioPlataforma.CamposRequeridosFormulario(mgdie.CodigoFormulario, function(respuesta) {
			mgdie.camposRequeridos = respuesta;
		});

		var crearGrupoDireccionesP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=239";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
	    };
		
	    mgdie.aceptar = function() {
			var parametrosSP = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario :$scope.SesionIdUsuario,
			p_IdModulo :'SOC',
			p_UsaReglasNegocio :'S',
			p_Grupo : mgdie.data.Grupo || '',
			p_Comentario: mgdie.data.Comentario || '',   
			p_IdPersona: mgdie.IdPersona,
			p_IdEstado: (mgdie.data.Bloqueado === true) ? 'BLO':'ACT',
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			crearGrupoDireccionesP(parametrosSP).then(
				function(result) {
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Grupo de Direcciones creado",
						conflictos : util.parseServerResponse(result)}, idVentana);
					
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);	
					});
					
				},
				function(){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	
		};
		
		mgdie.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------