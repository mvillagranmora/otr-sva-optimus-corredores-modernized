angular.module('ModuloAPV').controller('EditarBancoPreviredCtrl',function($scope,$translate,bcsComunes,bcsServiciosAPV){
	var editarBancoPrevired = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	editarBancoPrevired.datosAntes = bcsComunes.ventanaParams(idVentana);
	editarBancoPrevired.datosDespues = angular.copy(editarBancoPrevired.datosAntes);
	editarBancoPrevired.modificarBancoPrevired = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var parametros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'APV',
			p_SimularOperacion : 'N',
			p_UsaReglasNegocio : 'S',
			p_BancosPrevired : [{
				IdInterno : 0,
				IdCuentaBanco : editarBancoPrevired.datosDespues.IdCuentaBanco,
				CodigoBancoPrevired : editarBancoPrevired.datosDespues.CodigoBancoPrevired
			}]
		}
		bcsServiciosAPV.ModificarBancoPrevired(parametros).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CODIGO_BANCO_PREVIRED_EDITADO_CORRECTAMENTE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			} else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_EDITAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
	  	},function(respuesta){
	  		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_EDITAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
	  	})
	}
});