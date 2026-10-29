(function() {
	angular.module('ModuloSocioNegocio').controller('ModalCrsPaisResidenciaFiscalCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
		var mcprf = this;
		mcprf.idVentana = bcsComunes.ventanaWinId($scope); 
		mcprf.parametros = bcsComunes.ventanaParams(mcprf.idVentana);
		mcprf.domVentana = $("[ventanaid='"+mcprf.idVentana+"']");
		mcprf.win = {};
		mcprf.fn = {};
		mcprf.datag = {};
		mcprf.Habilitado = true;
		mcprf.datag.FechaInicioTributacionPais = "0001-01-01";

		mcprf.fn.aceptar = function() {
			if(!mcprf.fn.validaPais()) return;
			if(!mcprf.fn.validaFecha()) return;
			bcsComunes.ventanaOverlay(mcprf.idVentana, true);
			if(mcprf.parametros.Accion === "CREAR"){
				mcprf.fn.crearPaisResidenciaFiscal();
			}
			if(mcprf.parametros.Accion === "MODIFICAR"){
				mcprf.fn.modificarPaisResidenciaFiscal();
			}
		}

		mcprf.fn.crearPaisResidenciaFiscal= function(){
			var paisResidenciaFiscal = {};
			var listPaisesResidenciaFiscal = [];
			var params = {};

			paisResidenciaFiscal.IdPais 					= mcprf.datag.PaisResidenciaFiscal.idActual;
			paisResidenciaFiscal.FechaInicioTributacionPais = mcprf.datag.FechaInicioTributacionPais;
			paisResidenciaFiscal.IdPersona 					= mcprf.parametros.IdPersona;
			paisResidenciaFiscal.IdUsuarioCreador			= $scope.$root.SesionIdUsuario;
			paisResidenciaFiscal.IdUsuarioModificador		= $scope.$root.SesionIdUsuario;
			paisResidenciaFiscal.IdEstado					= "ACT";
			paisResidenciaFiscal.IdEmpresa					= $scope.$root.SesionIdEmpresa;
			listPaisesResidenciaFiscal.push(paisResidenciaFiscal);

			params.idEmpresa = $scope.$root.SesionIdEmpresa;
			params.paisResidenciaFiscal = listPaisesResidenciaFiscal;
			params.accion = mcprf.parametros.Accion;
			//llamar el servicio de ingreso o modificacion
			servicioSociodeNegocio.CrearModificarPaisResidenciaFiscal(params,function(respuesta){
				console.log(respuesta);
				
				var id = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},mcprf.idVentana);
				bcsComunes.ventanaPromesa(id).then(function(result){
					bcsComunes.ventanaOverlay(mcprf.idVentana, false);
					bcsComunes.ventanaCerrar(mcprf.idVentana);
				});
			},function(value){
				console.log(value);
				var msgModificado = "Error";
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(value),
					mensaje: msgModificado
				},mcprf.idVentana);
				bcsComunes.ventanaPromesa(id).then(function(result){
					bcsComunes.ventanaOverlay(mcprf.idVentana, false);
					bcsComunes.ventanaCerrar(mcprf.idVentana);
				});
			});
		}

		mcprf.fn.modificarPaisResidenciaFiscal = function(){
			var paisResidenciaFiscal = {};
			var listPaisesResidenciaFiscal = [];
			var params = {};

			paisResidenciaFiscal.IdPaisResidenciaFiscal 	= mcprf.parametros.dataGrilla.IdPaisResidenciaFiscal;
			paisResidenciaFiscal.IdPais 					= mcprf.parametros.dataGrilla.IdPais;
			paisResidenciaFiscal.FechaInicioTributacionPais = mcprf.datag.FechaInicioTributacionPais;
			paisResidenciaFiscal.IdUsuarioModificador		= $scope.$root.SesionIdUsuario;
			paisResidenciaFiscal.IdEstado					= "ACT";
			paisResidenciaFiscal.IdEmpresa					= $scope.$root.SesionIdEmpresa;
			listPaisesResidenciaFiscal.push(paisResidenciaFiscal);

			params.idEmpresa = $scope.$root.SesionIdEmpresa;
			params.paisResidenciaFiscal = listPaisesResidenciaFiscal;
			params.accion = mcprf.parametros.Accion;
			//llamar el servicio de ingreso o modificacion
			servicioSociodeNegocio.CrearModificarPaisResidenciaFiscal(params,function(respuesta){
				//console.log(respuesta);
				var id = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},mcprf.idVentana);
				bcsComunes.ventanaPromesa(id).then(function(result){
					bcsComunes.ventanaOverlay(mcprf.idVentana, false);
					bcsComunes.ventanaCerrar(mcprf.idVentana);
				});
			},function(value){
				console.log(respuesta);
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},mcprf.idVentana);
				bcsComunes.ventanaPromesa(id).then(function(result){
					bcsComunes.ventanaOverlay(mcprf.idVentana, false);
					bcsComunes.ventanaCerrar(mcprf.idVentana);
				});
			});
		}

		mcprf.fn.validaFecha = function(){
	    	if (mcprf.datag.FechaInicioTributacionPais === undefined || mcprf.datag.FechaInicioTributacionPais === '' || mcprf.datag.FechaInicioTributacionPais === '0001-01-01'){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_DATOS_INCOMPLETOS"));
				$scope.ModalCrsPaisResidenciaFiscal.$valid = undefined;
				return false;
			}
	    	return true;
	    }

		mcprf.fn.validaPais = function(){
	    	if (mcprf.datag.PaisResidenciaFiscal.idActual === undefined || mcprf.datag.PaisResidenciaFiscal.idActual === '' || mcprf.datag.PaisResidenciaFiscal.idActual === '0001-01-01'){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_DATOS_INCOMPLETOS"));
				$scope.ModalCrsPaisResidenciaFiscal.$valid = undefined;
				return false;
			}
	    	return true;
	    }

		mcprf.fn.cancelar = function() {
			bcsComunes.ventanaOverlay(mcprf.idVentana,false);
			bcsComunes.ventanaCerrar(mcprf.idVentana);
		};

		mcprf.fn.CargarPaisResidenciaFiscal = function(){
			if(mcprf.parametros.Accion === 'MODIFICAR'){
				mcprf.datag.FechaInicioTributacionPais 	= mcprf.parametros.dataGrilla.FechaInicioTributacionPais;
				mcprf.Habilitado = false;
			}
		};
		mcprf.fn.CargarPaisResidenciaFiscal();
	});
}());
//------------------------- FIN CONTROLLER --------------------------