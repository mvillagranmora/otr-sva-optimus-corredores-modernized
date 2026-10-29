/********************************************************************
 * Purpose	: Controlador para administrar Limites
 * Author	: MMACHMAR - BCS
 * Date		: 10/05/2016
 *******************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('AdministrarLimitesCtrl',
	function($state, $q, $scope, $compile, bcsComunes, $translate) {
		const self = this;
		self.idVentana = bcsComunes.ventanaWinId($scope); 
		self.params = bcsComunes.ventanaParams(self.idVentana);
		self.domVentana = $("[ventanaid='" + self.idVentana + "']");
		
		
		self.fnTabSelect = function fnTabSelect(producto, destino) {
			console.log({ producto, destino });
			const tabProducto = self.tabPorProducto[producto];
			const propDestino = destino === null || destino === undefined ? tabProducto.porDefecto : destino;
			const tabDestino = tabProducto.tabs[propDestino];
			if (tabDestino.requiereCargar) {
				const contenedor = self.domVentana.find('#' + tabDestino.id);
				const contenido = $compile(tabDestino.template)($scope);
				contenedor.append(contenido);
				tabDestino.requiereCargar = false;
			}
			tabDestino.activo = true;
		}

		self.tabPorProducto = {
			rf: {
				porDefecto: 'cuentaPropiaRFPlazo',
				tabs: {
					cuentaPropiaRFPlazo: { id: 'PanelLimitesRFCuentaPropiaPlazo', requiereCargar: true, activo: true, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaRFPlazo.html\'" ></div>' },
					cuentaPropiaRFEmisor: { id: 'PanelLimitesRFCuentaPropiaEmisor', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaRFEmisor.html\'" ></div>' },
					cuentaPropiaRFClasificacionRiesgo: { id: 'PanelLimitesRFCuentaPropiaClasificacionRiesgo', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaRFClasifRiesgo.html\'" ></div>' },
					cuentaPropiaRFMargenTasas: { id: 'PanelLimitesRFCuentaPropiaMargenTasas', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaRFMargenTasas.html\'" ></div>' },
					cuentaPropiaRFMoneda: { id: 'PanelLimitesCuentaPropiaRFMoneda', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaRFMoneda.html\'" ></div>' },
				}
			},
			if: {
				porDefecto: 'cuentaPropiaIFPlazo',
				tabs: {
					cuentaPropiaIFPlazo: { id: 'PanelLimitesIFCuentaPropiaPlazo', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaIFPlazo.html\'" ></div>' },
					cuentaPropiaIFEmisor: { id: 'PanelLimitesIFCuentaPropiaEmisor', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaIFEmisor.html\'" ></div>' },
					cuentaPropiaIFClasifRiesgo: { id: 'PanelLimitesIFCuentaPropiaClasificacionRiesgo', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaIFClasifRiesgo.html\'" ></div>' },
					cuentaPropiaIFMargenTasas: { id: 'PanelLimitesIFCuentaPropiaMargenTasas', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaIFMargenTasas.html\'" ></div>' },
					cuentaPropiaIFMoneda: { id: 'PanelLimitesIFCuentaPropiaMoneda', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaIFMoneda.html\'" ></div>' },
				}
			},
			spot: {
				porDefecto: 'cuentaPropia',
				tabs: {
					cuentaPropia: { id: 'PanelLimitesSpotCuentaPropia', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaSpot.html\'" ></div>' }
				},
			},
			forward: {
				porDefecto: 'cuentaPropia',
				tabs: {
					cuentaPropia: { id: 'PanelLimitesForwardCuentaPropia', requiereCargar: true, activo: false, template: '<div ng-include="\'../../Plataforma/UI/LIM/CuentaPropia/PanelLimitesCuentaPropiaForward.html\'" ></div>' }
				},
			},
		};
     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
		
/*********************************************
 * COMBOBOX
 *********************************************/
		     	
/*********************************************
 * FUNCIONES
 ********************************************/
 
	});
}());