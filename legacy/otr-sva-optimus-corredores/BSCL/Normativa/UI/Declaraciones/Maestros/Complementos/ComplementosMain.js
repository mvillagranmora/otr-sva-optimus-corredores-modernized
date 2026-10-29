angular.module('ModuloNormativa').controller('ComplementosMainCtrl',function($scope,$compile,$translate,bcsComunes,$timeout){
	
	var complementosMain = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	complementosMain.idVentana = idVentana;
	complementosMain.confGlobal = {};
	complementosMain.confGlobal.pensando = false;
	//complementosMain.confGlobal.esquema = {};
	$scope.formData.focoOperacion = true;
	$scope.parametros = bcsComunes.ventanaParams(complementosMain.idVentana);
	complementosMain.domVentana = $("[ventanaid='"+complementosMain.idVentana+"']");
	complementosMain.formCargado = '';
	
	complementosMain.formFondoAccionario =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/FondoAccionario.html\'" ></div>';
	complementosMain.formCargaIPC =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/CargaIPC.html\'" ></div>';
	complementosMain.formEventoCapital =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/EventoCapital.html\'" ></div>';
	complementosMain.formActualizarMontos =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/ActualizarMontos.html\'" ></div>';
	complementosMain.formAjustarRescatesDelPeriodo =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/AjustarRescatesDelPeriodo.html\'" ></div>';
	complementosMain.formCalculo4Porciento =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/Calculo4Porciento.html\'" ></div>';
	complementosMain.formCalificacionPendiente = '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/CalificacionPendiente.html\'" ></div>';
	complementosMain.formValorizarDeclaracion =  '<div ng-include="\'../../Normativa/UI/Declaraciones/Maestros/Complementos/ValorizarDeclaracion.html\'" ></div>';
	
	$scope.complementosMain.parametros = bcsComunes.ventanaParams(complementosMain.idVentana);
	complementosMain.parametros = $scope.complementosMain.parametros;
	console.log(complementosMain.parametros );
	complementosMain.forms = {
		FONDOACCIONARIO		: complementosMain.formFondoAccionario,
		CARGAIPC			: complementosMain.formCargaIPC,
		EVENTOCAPITAL		: complementosMain.formEventoCapital,
		ACTUALIZARMONTOS	: complementosMain.formActualizarMontos,
		AJURESDELPER		: complementosMain.formAjustarRescatesDelPeriodo,
		CALCULO4PORCIENTO	: complementosMain.formCalculo4Porciento,
		VALORIZARDECLARACION : complementosMain.formValorizarDeclaracion,
		CALIFICACIONPENDIENT : complementosMain.formCalificacionPendiente
	};
	
	$scope.$watch('tipoComplemento.seleccionado',function(newval,oldval) {
		
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
		Esquema : { field: "Esquema", operator: "eq", value:complementosMain.parametros.esquema }
	};
	
});