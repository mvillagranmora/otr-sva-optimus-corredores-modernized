angular.module('ModuloOrdenes').controller('IngresarOrdenMainCtrl', function($scope,$compile,bcsComunes,$timeout){
	
	var ordenMain = this;
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	ordenMain.idVentana = idVentana;
	ordenMain.confGlobal = {};
	ordenMain.confGlobal.pensando = false;
	
	ordenMain.domVentana = $("[ventanaid='"+ordenMain.idVentana+"']");
	ordenMain.formCargado = '';
	ordenMain.filtrosTipoInstrumento = {
		IdEstado 	: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
		EsOrden 	: { "field"   : "EsOrden","operator": "eq", "value"   : "S" },
	};
	
	ordenMain.formRV =  '<div ng-include="\'../../Orden/UI/IngresarOrden/IngresarOrdenRV.html\'" ></div>';
	ordenMain.formRF = 	'<div ng-include="\'../../Orden/UI/IngresarOrden/IngresarOrdenRF.html\'" ></div>';
	//ordenMain.formFU =  '<div ng-include="\'../../Orden/UI/IngresarOrden/IngresarOrdenFU.html\'" ></div>';
	
	ordenMain.forms = {
		ACN :ordenMain.formRV,
		ACX :ordenMain.formRV,
		CFI :ordenMain.formRV,
		CFX :ordenMain.formRV,
		CFM :ordenMain.formRV,
		ETN :ordenMain.formRV,
		ETX :ordenMain.formRV,
		MIL :ordenMain.formRV,
		MON :ordenMain.formRV,
		OSA :ordenMain.formRV,
		
		FUF :ordenMain.formRV, //FUTURO UF
		FIP :ordenMain.formRV, //FUTURO IPSA
		//FBC :ordenMain.formRV, //FUTURO BCU
		FIC :ordenMain.formRV, //FUTURO ICP
		FDO :ordenMain.formRV, //FUTURO DOLAR

		BEM :ordenMain.formRF,
		LHT :ordenMain.formRF
	};
	
	$scope.$watch('ordenMain.confGlobal.tipoInstrumentoSeleccionado',function(newval,oldval) {
		
		if (!newval) {
			return;
		}
		
		if (!ordenMain.forms[newval.IdTipoInstrumento])
			ordenMain.domVentana.find('#loadMe').children().remove();
			
		if (ordenMain.formCargado === ordenMain.forms[newval.IdTipoInstrumento])
			return;
		
		ordenMain.domVentana.find('#loadMe').children().remove();
		ordenMain.domVentana.find('#loadMe').append($compile(ordenMain.forms[newval.IdTipoInstrumento])($scope));
		ordenMain.formCargado = ordenMain.forms[newval.IdTipoInstrumento];
			
		
		
	});
	
    
});
