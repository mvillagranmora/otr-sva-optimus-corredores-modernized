angular.module('ModuloTesoreria').controller('AgregarComprobanteGiroEgresoCtrl', function($scope, $timeout, $modal, $state, bcsComunes,
						bcsAlerta, OPCIONES_BCSALERTA, $translate,
						bcsServiciosTesoreria, bcsBlotterComprobantesGrid,
						$http) {
					$scope.winID = bcsComunes.ventanaWinId($scope);
					$scope.configGrilla = bcsComunes.ventanaParams($scope.winID);
					bcsComunes.ventanaAutoOverlay($scope.winID, false, 3000);
					$scope.btnActivo = false;		
					// GRILLA PRINCIPAL
					var dsComprobantes =bcsBlotterComprobantesGrid.dsDefComprobantes;
					dsComprobantes.definition.filter = Object.values($scope.configGrilla[0]);
					var ds = new bcs.DataSource(dsComprobantes);
					$scope.confBasicaComprobantes = {
						dataSource : ds,
						columns : bcsBlotterComprobantesGrid.columnasComprobantes(),
						sortable : true,
						pageable : true,
						sort 				: { field: "IdComprobante", dir: "desc" }, 		
						// autoBind : true, //comentar
						filterable : true,
						height : 470,
						selectable : "multiple row",
						navigatable : true,
						gridCreated : function(g) {
							$scope.ApiComprobantes = g;
							ds.pageSize("400");
						},
						rowColorBackground : function(row) {
							var color = "";

							if (row.IdEstadoAutorizacion == 'VIG')
								color = "yellow";
							if (row.IdEstadoAutorizacion == 'AUT')
								color = "green";
							if (row.IdEstado === 'ANU')
								color = "red";
							return color;
						},
						userSettings : true
					};

					$scope.confBasicaComprobanteDetalle = function(item) {
						var dsDef = bcsBlotterComprobantesGrid.dsDefComprobanteDetalle;
						dsDef.definition.filter = {
							field : "IdComprobante",
							operator : "eq",
							value : item.IdComprobante
						};
						return {
							dataSource : new bcs.DataSource(dsDef),
							columns : bcsBlotterComprobantesGrid
									.columnasComprobanteDetalle(),
							sortable : true,
							pageable : true,
							autoBind : true,
							height : 150,
							selectable : "multiple row",
							navigatable : true,
							userSettings : true
						};
					};
					$scope.confBasicaComprobanteFormaPago = function(item) {
						var dsDef = bcsBlotterComprobantesGrid.dsDefComprobanteFormaDePago;
						dsDef.definition.filter = [ {
							field : "IdComprobante",
							operator : "eq",
							value : item.IdComprobante
						}];
						return {
							dataSource : new bcs.DataSource(dsDef),
							columns : bcsBlotterComprobantesGrid
									.columnasComprobanteFormaDePago(),
							sortable : true,
							pageable : true,
							autoBind : true,
							height : 150,
							navigatable : true,
							selectable : "multiple row",
							userSettings : true
						};
					};
					// OPCIONES
					$scope.validarGirarComprobantesEgreso = function(datos, comprobantesAGirar) {
							bcsServiciosTesoreria.ValidarComprobantesEgreso( datos, function(respuesta) {
								if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
									var parametrosVentana = [];
									// Si las validaciones resultaron exitosas, agrega comprobantes a ventana de Giro de Egresos con los comprobantes seleccionados más los anteriores.
									parametrosVentana
									.push({
										IdConcepto : respuesta.p_out_IdConcepto,
										NombreConcepto : respuesta.p_out_NombreConcepto,
										IdMoneda : respuesta.p_out_IdMoneda,
										Monto : 0, // CAMBIAR
										NombreMoneda : respuesta.p_out_NombreMoneda,
										FechaLiquidacion : util.dateInicioDia(respuesta.p_out_FechaLiquidacion),
										IdCaja : respuesta.p_out_IdCaja,
										NombreCaja : respuesta.p_out_NombreCaja,
										comprobantesAGirar : comprobantesAGirar,
										mediosDePagoAGirar : respuesta.p_out_MediosDePagoAGirar,
										IdTipoMovimiento : 'MOVEGR',
										TipoMovimiento : 'PAREGR' 

									});
									bcsComunes.setConflictos(util.parseServerResponse(respuesta));
									bcsComunes.ventanaOverlay($scope.winID, false);
									//$scope.configGrilla[0] = parametrosVentana;
			
									bcsComunes.ventanaParamsEdit("GiroEgresos",$scope.configGrilla[1], parametrosVentana);
									bcsComunes.ventanaCerrar($scope.winID);
									/* var ventanaGiroEgresos = bcsComunes.ventanaAbrir("GiroEgresos",parametrosVentana,false, false);
									var promesa = bcsComunes.ventanaPromesa(ventanaGiroEgresos);
									promesa.then(function(data) {
										//$scope.buscarComprobantes();
									}); */

								} else {
									bcsComunes.ventanaMsg(
													{mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
														conflictos : util.parseServerResponse(respuesta)
													}, $scope.winID);
								}
								bcsComunes.ventanaOverlay(
										$scope.winID, false);
							}, function(respuesta) {
								bcsComunes.ventanaMsg({
													mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
													conflictos : util.parseServerResponse(respuesta)
												}, $scope.winID);
								bcsComunes.ventanaOverlay(
										$scope.winID, false);
							});
						
					};


					$scope.agregarTodos = function() {

						bcsComunes.ventanaOverlay($scope.winID, true);
						let listaDeEgresosIngresos = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering: true,
								sort: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.sort,
								transport: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.transport,
								filter: $scope.ApiComprobantes.dataSource.ds._filter.filters,
								schema: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.schema
							}
						});
						
						listaDeEgresosIngresos.query().then(function (result) {
							if(listaDeEgresosIngresos.data().length > 0){
								let comprobantesAGirar = [...new Set(listaDeEgresosIngresos.data().map( obj => ({"IdComprobante": obj.IdComprobante}) ))];
								var datosActuales = bcsComunes.ventanaParams($scope.configGrilla[1]);
								if (datosActuales && datosActuales[0] && datosActuales[0].comprobantesAGirar && datosActuales[0].comprobantesAGirar.length > 0) {
									$.each(
											datosActuales[0].comprobantesAGirar,
											function(key, value) {
													comprobantesAGirar.push({IdComprobante : datosActuales[0].comprobantesAGirar[key].IdComprobante});
											});
								}
								if (comprobantesAGirar.length === 0) {
									bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")}, $scope.winID);
									return;
								};
								var datos = {};
								datos.p_IdEmpresa = $scope.SesionIdEmpresa;
								datos.p_IdUsuario = $scope.SesionIdUsuario;
								datos.p_IdModulo  = 'TES';
								datos.p_simularOperacion = 'S'; // Para este caso debe ir 'S' para que sólo valide
								datos.p_UsaReglasNegocio = 'S';
								datos.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
								datos.p_ComprobantesAGirar = comprobantesAGirar;
								// Estos parámetros se envían vacios, ya que en esta instancia este servicio se usa solo para validar las condiciones de los comprobantes seleccionados para girar.						
								datos.p_GeneraNomina = '';  
								datos.p_GeneraTransferencia = '';
								datos.p_IdMedioDePago = '';
								datos.p_IdCuentaBancariaCorredora = 0;
								datos.p_Monto = 0;
								// datos.p_IdCaja = 0 ;
								datos.p_FormasDePago = [];
								datos.p_usaTalonarioCheque = '';
								datos.p_IdModalidadDePago = '';

								$scope.validarGirarComprobantesEgreso( datos,comprobantesAGirar );
										
							}

						});
					};

					$scope.agregarSeleccionados = function() {
						var comprobantesAGirar = [];
						var datosActuales = bcsComunes.ventanaParams($scope.configGrilla[1]);
						if (datosActuales && datosActuales[0] && datosActuales[0].comprobantesAGirar && datosActuales[0].comprobantesAGirar.length > 0) {
							$.each(
									datosActuales[0].comprobantesAGirar,
									function(key, value) {
											comprobantesAGirar.push({IdComprobante : datosActuales[0].comprobantesAGirar[key].IdComprobante});
									});
						}
						$.each(
									$scope.seleccionComprobantes,
									function(key, value) {
											comprobantesAGirar.push({IdComprobante : $scope.seleccionComprobantes[key].IdComprobante});
									});
						comprobantesAGirar = [...new Set(comprobantesAGirar)];
						if (comprobantesAGirar.length === 0) {
							bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")}, $scope.winID);
							return;
						};
						bcsComunes.ventanaOverlay($scope.winID, true);
						var datos = {};
						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdModulo  = 'TES';
						datos.p_simularOperacion = 'S'; // Para este caso debe ir 'S' para que sólo valide
						datos.p_UsaReglasNegocio = 'S';
						datos.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_ComprobantesAGirar = comprobantesAGirar;
						// Estos parámetros se envían vacios, ya que en esta instancia este servicio se usa solo para validar las condiciones de los comprobantes seleccionados para girar.						
						datos.p_GeneraNomina = '';  
						datos.p_GeneraTransferencia = '';
						datos.p_IdMedioDePago = '';
						datos.p_IdCuentaBancariaCorredora = 0;
						datos.p_Monto = 0;
						datos.p_FormasDePago = [];
						datos.p_usaTalonarioCheque = '';
						datos.p_IdModalidadDePago = '';

						$scope.validarGirarComprobantesEgreso( datos,comprobantesAGirar );

					};
					$scope.cerrarVentana = function(){
						bcsComunes.ventanaCerrar($scope.winID);
					}

});
