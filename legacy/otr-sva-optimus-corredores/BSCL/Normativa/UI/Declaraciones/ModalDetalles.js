(function() {
angular.module('ModuloNormativa').controller('ModalDetallesDjCtrl',
		function($scope, $compile, $translate, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mddj = this;
			mddj.idVentana = bcsComunes.ventanaWinId($scope);
			mddj.parametrosVentana = bcsComunes.ventanaParams(mddj.idVentana);
			mddj.tab = mddj.parametrosVentana.tab;
			mddj.keyCabecera = mddj.parametrosVentana.keyCabecera;
			mddj.version = mddj.parametrosVentana.version;
			mddj.domVentana = $("[ventanaid='"+mddj.idVentana+"']");

			/*********************************************
			 * FUNCIONES DE CARGA DE TABS
			 ********************************************/
			
			mddj.fnCambioDeTab=function(nombreTab) {
				mddj.tabs[nombreTab].active=true; 
				$timeout(function(e) {
					$.each(mddj.tabs,function(key,value) { value.active=false; });
					mddj.tabs[nombreTab].active=true; 
				},0);
			};
			
			mddj.tabs = { 
					PanelCalendarioPagos : { id : 'PanelCalendarioPagos', url : '<div ng-include="\'../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelCalendarioPagos.html\'" ></div>', active : true,  cargado: false, datosCambiados: false },
					PanelPagosPeriodo : { id : 'PanelPagosPeriodo', url : '<div ng-include="\'../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelPagosPeriodo.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelGarantias : { id : 'PanelGarantias', url : '<div ng-include="\'../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelGarantias.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelTipoActivoGarantia : { id : 'PanelTipoActivoGarantia', url : '<div ng-include="\'../../Normativa/UI/Declaraciones/PanelesModalDetalles/PanelTipoActivoGarantia.html\'" ></div>', active : false, cargado: false, datosCambiados: false }	
			};
		
			mddj.fnTabSelect = function(nombreTab) {
				if (mddj.tabs[nombreTab].cargado !== true) {
					mddj.tabs[nombreTab].cargado = true;
					mddj.tabs[nombreTab].active = true;
					mddj.domVentana.find('#'+mddj.tabs[nombreTab].id).append($compile(mddj.tabs[nombreTab].url)($scope));
				}
			};
			
			mddj.fnPreguntaCambioDeTab=function(nombreTab) {
				var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_PREGUNTA_DATOS_SIN_GUARDAR'));
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
						mddj.fnCambioDeTab(nombreTab);
					}
				});	
			};
			
			mddj.fnTabDeselect = function(nombreTab) {
				if (mddj.tabs[nombreTab].datosCambiados === true) mddj.fnPreguntaCambioDeTab(nombreTab);
			};	
			
		});			
		
}());
