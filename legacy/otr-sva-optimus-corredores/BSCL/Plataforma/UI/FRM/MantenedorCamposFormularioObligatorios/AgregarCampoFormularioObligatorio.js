angular.module('ModuloPlataforma').controller('AgregarCampoFormularioObligatorioCtrl',function($scope,$translate,bcsComunes,bcsServicioPlataforma){
	var agregarCampoFormularioObligatorio = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	
	agregarCampoFormularioObligatorio.formData = {};
	agregarCampoFormularioObligatorio.formData.filtrosCanal = {
    	logic: "or",
    	AGE: { field: "IdTipoCanalIngreso", operator: "eq", value: 'AGE'},
		EJE: { field: "IdTipoCanalIngreso", operator: "eq", value: 'EJE'},
		WBS: { field: "IdTipoCanalIngreso", operator: "eq", value: 'WBS'},
		CLI: { field: "IdTipoCanalIngreso", operator: "eq", value: 'CLI'}
	}
	
	agregarCampoFormularioObligatorio.crearCampoFormulario = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var parametros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_SimularOperacion : 'N',
			p_UsaReglasNegocio : 'S',
			p_CodigoFormulario : agregarCampoFormularioObligatorio.datos.CodigoFormulario,
			p_CodigoCampo : agregarCampoFormularioObligatorio.datos.CodigoCampo,
			p_IdTipoCanal : agregarCampoFormularioObligatorio.datos.IdTipoCanal === 0 ? '' : agregarCampoFormularioObligatorio.datos.IdTipoCanal,
			p_CantidadMinima : agregarCampoFormularioObligatorio.datos.CantidadMinima || 0,
			p_Requerido : agregarCampoFormularioObligatorio.datos.Requerido ? 'S' : 'N'
		}
		bcsServicioPlataforma.Servicios().CrearCampoFormulario(parametros).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREAR_CAMPO_FORMULARIO_CORRECTAMENTE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			} else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_CAMPO_FORMULARIO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
	  	},function(respuesta){
	  		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_CAMPO_FORMULARIO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
	  	})
	}
});