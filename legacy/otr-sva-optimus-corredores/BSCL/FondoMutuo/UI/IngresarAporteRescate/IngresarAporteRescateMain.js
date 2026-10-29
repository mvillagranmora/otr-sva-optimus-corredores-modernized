angular.module('ModuloFFM').controller('IngresarAporteRescateMainCtrl',function($scope,$compile,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	
	var aporteRescateMain = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	aporteRescateMain.idVentana = idVentana;
	aporteRescateMain.confGlobal = {};
	aporteRescateMain.confGlobal.pensando = false;
	aporteRescateMain.confGlobal.oOperacion = {};
	$scope.formData.focoOperacion = true;
	
	aporteRescateMain.confGlobal.JerarquiaPerfil = {};
	
	var confCategoria = angular.copy(bcsConfiguracionFFM.getDataSourcePerfilCategoriaFondoMutuo());
	
	$scope.oConfiguracionTipoOperacion = {
		filtros : {
			TieneIngresoManual : { "field": "TieneIngresoManual","operator": "eq", "value"   :  'S' }
		}
	}
	
	confCategoria.query({filter:{}}).then(function(){
		if(confCategoria.data().length > 0){
			var dataCategoria = confCategoria.data();
			for(var i = 0; i < dataCategoria.length; i++){
				aporteRescateMain.confGlobal.JerarquiaPerfil[dataCategoria[i].IdCategoriaInversionista] = dataCategoria[i].Jerarquia;
			}
		}
	});

	aporteRescateMain.domVentana = $("[ventanaid='"+aporteRescateMain.idVentana+"']");
	aporteRescateMain.formCargado = '';
	
	aporteRescateMain.formAporteNormal =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteNormal.html\'" ></div>';
	aporteRescateMain.formRescateNormal =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateNormal.html\'" ></div>';
	aporteRescateMain.formAporteOvernight =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteOvernight.html\'" ></div>';
	aporteRescateMain.formAporteRescateProgramado =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteConRescateProgramado.html\'" ></div>';
	aporteRescateMain.formRescateReinversion1N =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateReinversion1N.html\'" ></div>';
	aporteRescateMain.formRescateReinversion =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateReinversion.html\'" ></div>';
	aporteRescateMain.formAporteReinversion =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteReinversion.html\'" ></div>';
	aporteRescateMain.formRescateTraspaso =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTraspaso.html\'" ></div>';
	aporteRescateMain.formRescateTraspasoPortafolio = '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTraspasoPortafolio.html\'"></div>';
	aporteRescateMain.formRescateTransferencia =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateTransferencia.html\'" ></div>';
	aporteRescateMain.formRescateCambioSerie =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarRescateCambioSerie.html\'" ></div>';
	aporteRescateMain.formIngresoTitulo =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarTitulo.html\'" ></div>';
	aporteRescateMain.formRetiroTitulo =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/RetirarTitulo.html\'" ></div>';
	aporteRescateMain.formIngresoGarantia =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarGarantia.html\'" ></div>';
	aporteRescateMain.formRetiroGarantia =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/RetirarGarantia.html\'" ></div>';
	aporteRescateMain.formAporteDividendoCFI =  '<div ng-include="\'../../FondoMutuo/UI/IngresarAporteRescate/IngresarAporteDividendoCFI.html\'" ></div>';
	
	$scope.aporteRescateMain.parametros = bcsComunes.ventanaParams(aporteRescateMain.idVentana);
	aporteRescateMain.parametros = $scope.aporteRescateMain.parametros;
	
	aporteRescateMain.forms = {
		APONORM   : aporteRescateMain.formAporteNormal,
		RESNORM   : aporteRescateMain.formRescateNormal,
		APOOVER   : aporteRescateMain.formAporteOvernight,
		APOREINV  : aporteRescateMain.formAporteReinversion,
		APORESPRO : aporteRescateMain.formAporteRescateProgramado,
		//APOTRANSF : aporteRescateMain.formAporteNormal,
		//APOTRASP  : aporteRescateMain.formRescateNormal,
		//APODIV    : aporteRescateMain.formAporteNormal,
		//APOCANJE  : aporteRescateMain.formRescateNormal,
		//APOFUSION : aporteRescateMain.formAporteNormal,
		RESREINV1N  : aporteRescateMain.formRescateReinversion1N,
		RESREINV  : aporteRescateMain.formRescateReinversion,
		RESTRANSF : aporteRescateMain.formRescateTransferencia,
		RESTRASP  : aporteRescateMain.formRescateTraspaso,
		RESTRAPOR : aporteRescateMain.formRescateTraspasoPortafolio,
		RESCAM    : aporteRescateMain.formRescateCambioSerie,
		INGTIT	  : aporteRescateMain.formIngresoTitulo,
		RETTIT	  : aporteRescateMain.formRetiroTitulo,
		INGGTA	  : aporteRescateMain.formIngresoGarantia,
		RETGTA	  : aporteRescateMain.formRetiroGarantia,
		APODIVCFI : aporteRescateMain.formAporteDividendoCFI
		
		//RESCANJE  : aporteRescateMain.formRescateNormal,
		//RESFUSION : aporteRescateMain.formRescateNormal
	};
	
	$scope.$watch('oOperacion.seleccionado',function(newval,oldval) {
		
		if (!newval) {
			return;
		}
		
		if (!aporteRescateMain.forms[newval.IdTipoMovimientoFondoMutuo])
			aporteRescateMain.domVentana.find('#loadMe').children().remove();
			
		if (aporteRescateMain.formCargado === aporteRescateMain.forms[newval.IdTipoMovimientoFondoMutuo])
			return;
		
		aporteRescateMain.confGlobal.oOperacion.seleccionado = $scope.oOperacion.seleccionado;
		aporteRescateMain.confGlobal.IdSocioDeNegocio = aporteRescateMain.parametros.IdSocioDeNegocio;
		aporteRescateMain.confGlobal.IdCliente = aporteRescateMain.parametros.IdCliente;
		aporteRescateMain.confGlobal.IdPortafolio = aporteRescateMain.parametros.IdPortafolio;
		aporteRescateMain.confGlobal.IdSerieFondo = aporteRescateMain.parametros.IdSerieFondo;
		aporteRescateMain.domVentana.find('#loadMe').children().remove();
		aporteRescateMain.domVentana.find('#loadMe').append($compile(aporteRescateMain.forms[newval.IdTipoMovimientoFondoMutuo])($scope));
		aporteRescateMain.formCargado = aporteRescateMain.forms[newval.IdTipoMovimientoFondoMutuo];
	});

	if(aporteRescateMain.parametros){
		$scope.formData.idPostFiltro = aporteRescateMain.parametros.IdTipoMovimientoFondoMutuo;
	}
	
});