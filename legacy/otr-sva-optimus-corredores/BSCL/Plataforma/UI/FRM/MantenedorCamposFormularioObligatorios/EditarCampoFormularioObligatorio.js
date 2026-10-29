angular.module('ModuloPlataforma').controller('EditarCampoFormularioObligatorioCtrl',function($scope,$translate,bcsComunes,bcsServicioPlataforma){
	var editarCampoFormularioObligatorio = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	editarCampoFormularioObligatorio.datosAntes = bcsComunes.ventanaParams(idVentana);
	editarCampoFormularioObligatorio.datosAntes.RequeridoCheck = editarCampoFormularioObligatorio.datosAntes.Requerido === 'S' ? true : false;
	editarCampoFormularioObligatorio.datosAntes.RegeneraContratos = editarCampoFormularioObligatorio.datosAntes.RegeneraContratos === 'S' ? true : false;
	editarCampoFormularioObligatorio.datosDespues = angular.copy(editarCampoFormularioObligatorio.datosAntes);
	
	let debeMostrarValorPorDefecto = function debeMostrarValorPorDefecto(){
		let noEsTipoDatoGrid = editarCampoFormularioObligatorio.datosAntes.TipoDato !== 'GRID';
		let noEsRequerido = !editarCampoFormularioObligatorio.datosDespues.RequeridoCheck;
		let tipoCampoFrontendValido = editarCampoFormularioObligatorio.datosAntes.TipoCampoFrontend !== '';
		return noEsTipoDatoGrid && noEsRequerido && tipoCampoFrontendValido;
	}
	
	editarCampoFormularioObligatorio.mostrarValorPorDefecto = debeMostrarValorPorDefecto();
	
	if(editarCampoFormularioObligatorio.datosDespues.ConfiguracionFrontend !== ""){
		editarCampoFormularioObligatorio.datosDespues.ConfiguracionFrontend = JSON.parse(editarCampoFormularioObligatorio.datosDespues.ConfiguracionFrontend);
	}
	
	const esInputDate = function esInputDate() {
		return editarCampoFormularioObligatorio.datosAntes.TipoCampoFrontend === 'bcs-input-date';
	}
	
	const convertirFechaPorDefecto = function convertirFechaPorDefecto() {
	
		if(!esInputDate()) return;
		
		if(!editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario) return;
		
		const FechaPorDefectoConvertida = util.date(editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario).toDate();
		editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario = FechaPorDefectoConvertida;
		
	}
	
	convertirFechaPorDefecto();

	
	editarCampoFormularioObligatorio.editarCampoFormulario = function editarCampoFormulario(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var parametros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_SimularOperacion : 'N',
			p_UsaReglasNegocio : 'S',
			p_CodigoFormulario : editarCampoFormularioObligatorio.datosDespues.CodigoFormulario,
			p_CodigoCampo : editarCampoFormularioObligatorio.datosDespues.CodigoCampo,
			p_IdTipoCanal : editarCampoFormularioObligatorio.datosDespues.IdTipoCanal === 0 ? '' : editarCampoFormularioObligatorio.datosDespues.IdTipoCanal,
			p_CantidadMinima : editarCampoFormularioObligatorio.datosDespues.CantidadMinima || 0,
			p_Requerido : editarCampoFormularioObligatorio.datosDespues.RequeridoCheck ? 'S' : 'N',
			p_RegeneraContratos : editarCampoFormularioObligatorio.datosDespues.RegeneraContratos ? 'S' : 'N',
			p_ValorDefecto : editarCampoFormularioObligatorio.mostrarValorPorDefecto && editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario ? editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario : '',
			p_ValorAntes : editarCampoFormularioObligatorio.datosAntes.NombreValorDefectoCampoFormulario,
			p_TipoCampoFrontend : editarCampoFormularioObligatorio.datosDespues.TipoCampoFrontend
		}
		
		if(esInputDate()){
			if(editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario){
				parametros.p_ValorDefecto = util.dateSPdate(editarCampoFormularioObligatorio.datosDespues.ValorDefectoCampoFormulario);
			}else{
				parametros.p_ValorDefecto = '';
			}
		}
		
		bcsServicioPlataforma.Servicios().EditarCampoFormulario(parametros).$promise.then(function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_EDITAR_CAMPO_FORMULARIO_CORRECTAMENTE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			} else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_EDITAR_CAMPO_FORMULARIO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
	  	},function(respuesta){
	  		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_EDITAR_CAMPO_FORMULARIO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
	  	})
	}
	
	$scope.$watch('editarCampoFormularioObligatorio.datosDespues.RequeridoCheck',function(){
		editarCampoFormularioObligatorio.mostrarValorPorDefecto = debeMostrarValorPorDefecto();
	});
	$scope.$watch('editarCampoFormularioObligatorio.datosDespues.RegeneraContratos',function(){
		editarCampoFormularioObligatorio.mostrarValorPorDefecto = debeMostrarValorPorDefecto();
	});
});