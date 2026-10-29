angular.module('ModuloNormativa').controller('ComplementosMainRMOCtrl',function($scope, $compile, bcsComunes, Utils){
	
	const complementosMain = this;
	const windowParams = Utils.getWindowParams($scope);
	const idVentana = bcsComunes.ventanaWinId($scope);
	
	complementosMain.confGlobal = {};
	complementosMain.confGlobal.pensando = false;
	$scope.formData = {};
	complementosMain.domVentana = $("[ventanaid='"+idVentana+"']");
	complementosMain.formCargado = '';
	complementosMain.parametros = windowParams.datos;
	complementosMain.formReporteSaldoCuentasCambio = '<div ng-include="\'../../Normativa/UI/MantenedorReporteMensualOperaciones/Maestros/Complementos/ReporteSaldoCuentasCambio.html\'" ></div>';
	
	complementosMain.forms = {
		REPSALDOCUENTACAMBIO : complementosMain.formReporteSaldoCuentasCambio
	};
	
	$scope.$watch('tipoComplemento.seleccionado',function(newval) {
		
		if (!newval) {
			return;
		}
		
		if (!complementosMain.forms[newval.CodigoComplemento])
			complementosMain.domVentana.find('#loadMe').children().remove();
			
		if (complementosMain.formCargado === complementosMain.forms[newval.CodigoComplemento])
			return;
		
		complementosMain.confGlobal.esquema = complementosMain.parametros.esquema;
		complementosMain.confGlobal.version = complementosMain.parametros.version;
		complementosMain.domVentana.find('#loadMe').children().remove();
		complementosMain.domVentana.find('#loadMe').append($compile(complementosMain.forms[newval.CodigoComplemento])($scope));
		complementosMain.formCargado = complementosMain.forms[newval.CodigoComplemento];
			
	});

	$scope.formData.filtrosTipoComplemento = {
		Esquema : { field: "Esquema", operator: "eq", value: complementosMain.parametros.esquema }
	};
	
});