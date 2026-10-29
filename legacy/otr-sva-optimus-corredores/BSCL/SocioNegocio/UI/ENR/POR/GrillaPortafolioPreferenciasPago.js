(function () {
	angular.module('ModuloSocioNegocio').directive('bcsEnrolamientoPortafolioPreferenciasPago', function ($rootScope, $translate, bcsComunes, servicioSociodeNegocio) {
		return {
			restrict: 'E',
			replace: false,
			scope: {
				recibePagaPortafolio: '=?',
				idEnrolamiento: '=?',
				codigoFormulario: '=?'
			},
			templateUrl: '../../SocioNegocio/UI/ENR/POR/GrillaPortafolioPreferenciasPago.html',
			link: function (scope) {
				console.log('directiva bcsEnrolamientoPortafolioPreferenciasPago', scope.recibePagaPortafolio, scope.idEnrolamiento, scope.codigoFormulario);

				scope.cmbbx = scope.cmbbx || {};
				scope.fn = scope.fn || {};
				scope.win = scope.win || {};
				scope.grid = scope.grid || {};
				scope.idVentana = bcsComunes.ventanaWinId(scope);

				scope.win.abrirPopUp = function abrirPopUp(preferenciaPago) {
					let nombreModal = 'modalEnrolamientoPortafolioCrearPreferenciaPago';

					const parametrosModal = {
						IdEnrolamiento: scope.idEnrolamiento,
						RecibePagaPortafolio: scope.recibePagaPortafolio,
						CodigoFormulario : scope.codigoFormulario,
						PreferenciaPagoTodosObj: scope.grid.gridPrefPago.dataSource.data()
					};

					if (preferenciaPago) {
						nombreModal = 'modalEnrolamientoPortafolioEditarPreferenciaPago';
						parametrosModal.PreferenciaPagoObj = preferenciaPago;
					}

					bcsComunes.ventanaOverlay(scope.idVentana, true);

					const winPreferencia = bcsComunes.ventanaAbrir(nombreModal, parametrosModal, false, false);

					bcsComunes.ventanaPromesa(winPreferencia).then(function () {
						scope.grid.gridPrefPago.dataSource.read();
						bcsComunes.ventanaOverlay(scope.idVentana, false);
					});
				};
				
				scope.win.eliminar = function eliminar(preferenciaPago){
					bcsComunes.ventanaOverlay(scope.idVentana, true);
					const requestBody = {
		                p_IdEmpresa: $rootScope.SesionIdEmpresa,
		                p_IdUsuario: $rootScope.SesionIdUsuario,
		                p_IdModulo: 'SOC',
		                p_UsaReglasNegocio: 'N',
		                p_CodigoFormulario: '',
		                p_IdTipoCanal: 'OPT',
		                p_PreferenciasPagoPortafolio: [
		                    {
		                        IdPreferenciaPagoPortafolio: preferenciaPago.IdPreferenciaPagoPortafolio
		                    }
		                ]
		            };
		
		            servicioSociodeNegocio.eliminarBorradorPreferenciaPagoPortafolio(requestBody).$promise.then(
		                function (result) {
		                   bcsComunes.ventanaOverlay(scope.idVentana, false);
							if (result.P_OUT_ERRORESCONTADOR > 0){
								bcsComunes.msg("error","error","Preferencia de pago: La operación tiene errores");
								return;
							}
							
							bcsComunes.msg("info","info","Preferencia de pago: Eliminación OK");
							
							bcsComunes.ventanaMsg({ mensaje : "Preferencia de pago borrada",conflictos : util.parseServerResponse(result)}, scope.idVentana);
							scope.grid.gridPrefPago.dataSource.read();
							
		            		scope.grid.prefPagoSeleccionado = undefined;
		                },
		                function () {
							bcsComunes.ventanaOverlay(scope.idVentana, false);
		                    bcsComunes.msg("error", "Error", "La operacion tiene errores");
		                }
		            );
				}

				scope.setGridPrefPago = function setGridPrefPago() {
					return {
						dataSource: new bcs.DataSource({
							type: 'odata',
							definition: {
								serverFiltering: true,
								serverPaging: true,
								serverSorting: true,
								sortable: true,
								pageSize: 50,
								transport: {
									read: {
										url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700266',
										dataType: 'json'
									}
								},
								filter: [
									{ field: 'IdEnrolamiento', operator: 'eq', value: scope.idEnrolamiento },
									{ field: 'RecibePagaPortafolio', operator: 'eq', value: scope.recibePagaPortafolio }
								],
								schema: {
									model: {
										fields: {
											IdPreferenciaPagoPortafolio: { type: 'number' },
											IdPortafolio: { type: 'number' },
											IdTipoOperacion: { type: 'string' },
											TipoOperacion: { type: 'string' },
											IdMedioDePago: { type: 'string' },
											MedioDePago: { type: 'string' },
											IdMoneda: { type: 'string' },
											UsaCuentaBancaria: { type: 'string' },
											TextoUsaCuentaBancaria: { type: 'string' },
											UsaTarjetaBancaria: { type: 'string' },
											TextoUsaTarjetaBancaria: { type: 'string' },
											IdBanco: { type: 'number' },
											NombreBancoCuentaBancaria: { type: 'string' },
											NumeroCuentaBancaria: { type: 'string' },
											NombreBancoTarjetaBancaria: { type: 'string' },
											TextoPorDefecto: { type: 'string' },
											NumeroTarjetaBancaria: { type: 'string' }
										}
									}
								}
							}
						}),
						columns: [
							{
								field: 'MedioDePago',
								title: $translate.instant('UI_MEDIO_PAGO'),
								attributes: { style: 'text-align:left;' },
								width: '150px'
							},
							{
								field: 'IdMoneda',
								title: $translate.instant('UI_MONEDA'),
								attributes: { style: 'text-align:left;' },
								width: '50px'
							},
							{
								field: 'TipoOperacion',
								title: $translate.instant('UI_TIPO_OPERACION'),
								attributes: { style: 'text-align:center;' },
								width: '200px'
							},
							{
								field: 'TextoUsaCuentaBancaria',
								title: $translate.instant('UI_USA_CUENTA_BANCARIA'),
								attributes: { style: 'text-align:left;' },
								width: '100px'
							},
							{
								field: 'NombreBancoCuentaBancaria',
								title: $translate.instant('UI_BANCO'),
								attributes: { style: 'text-align:left;' },
								width: '150px'
							},
							{
								field: 'NumeroCuentaBancaria',
								title: $translate.instant('UI_NUMERO_CUENTA'),
								attributes: { style: 'text-align:left;' },
								width: '100px'
							},
							{
								field: 'TextoUsaTarjetaBancaria',
								title: $translate.instant('UI_USA_TARJETA_BANCARIA'),
								attributes: { style: 'text-align:left;' },
								width: '100px'
							},
							{
								field: 'NombreBancoTarjetaBancaria',
								title: $translate.instant('UI_BANCO'),
								attributes: { style: 'text-align:left;' },
								width: '150px'
							},
							{
								field: 'NumeroTarjetaBancaria',
								title: $translate.instant('UI_NUMERO_TARJETA'),
								attributes: { style: 'text-align:left;' },
								width: '100px'
							},
							{
								field: 'TextoPorDefecto',
								title: $translate.instant('UI_POR_DEFECTO'),
								attributes: { style: 'text-align:left;' },
								width: '100px'
							},
						],
						sortable: true,
						pageable: true,
						autoBind: true,
						height: 240,
						selectable: 'row'
					};
				};

				scope.$watch('idEnrolamiento', function (newval) {
					if (newval) {
						scope.grid.gridPrefPago = scope.setGridPrefPago();
						scope.grillaCargada = true;
					}
				});
			}
		}
	});
})();
