angular.module('ModuloAPV').controller('IngresoBancoPreviredCtrl',function($scope,$translate,bcsComunes,bcsServiciosAPV){
	var ingresoBancoPrevired = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	ingresoBancoPrevired.crearBancoPrevired = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var parametros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'APV',
			p_SimularOperacion : 'N',
			p_UsaReglasNegocio : 'S',
			p_BancosPrevired : [{
				IdInterno : 0,
				IdCuentaBanco : ingresoBancoPrevired.IdCuentaBanco,
				CodigoBancoPrevired : ingresoBancoPrevired.CodigoBancoPrevired
			}]
		}
		bcsServiciosAPV.CrearBancoPrevired(parametros).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CODIGO_BANCO_PREVIRED_CREADO_CORRECTAMENTE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			} else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
	  	},function(respuesta){
	  		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
	  	})
	}
});