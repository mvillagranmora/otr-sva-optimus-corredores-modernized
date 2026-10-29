angular.module('ModuloMesa').controller('ResumenMRFCtrl',
		function($scope,bcsComunes,$translate,bcsServiciosMesa)
{	

	$scope.data = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data = bcsComunes.ventanaParams($scope.winID);

	$scope.NombreArchivo = "Posicion MRF "+ $scope.SesionFechaSistema;


	var p_grillaAll;



	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
				    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};

	var DataSourceMRF = new bcs.DataSource({
		type : "array",
		definition : {
			data : [],
			schema : {
				model : {
					fields : {
								IdTipoReajuste : {type : "string"},
								PosicionInicial : {type : "number"},
								PosicionFinal : {type : "number"}
					}
				}
			}
		}
	});	 

	$scope.data.gridConfiguracionMRF = {	
		dataSource			: DataSourceMRF,
		columns				: 
			[

				{field: "IdTipoReajuste", 	title: 'UI_MONEDA', 			width: "60px" }
				,{field : "PosicionInicial", title: $translate.instant('UI_POSICION_INICIAL'), width: "90px",template : " <label> #: (util.bcs_formato(PosicionInicial,'GEN','Monto','CLP','','')).valor #</label>"}
				,{field : "PosicionFinal", title: $translate.instant('UI_POSICION_FINAL'), width: "90px",template : " <label> #: (util.bcs_formato(PosicionFinal,'GEN','Monto','CLP','','')).valor #</label>"}
			],
			
		userSettings 		: false,
		pageable	        : true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		groupable 			: false,
		filterable 			: false,
		height				: 145,
		selectable 			: true,
		autoBind            : true,
		dataBound: function(e) {
                    var gridElement = e.sender.element[0];
                    var cells = gridElement.querySelectorAll('tbody tr td');
                    cells.forEach(function(cell) {
                        cell.addEventListener('dblclick', function() {
                            var cellIndex = cell.cellIndex;
                            
                            if(cellIndex === 0) {
								//columna reajuste	                            
							} else if(cellIndex === 1) {
                                bcsComunes.ventanaAbrir("PosicionInicialMRF",{},false,false,$scope.winID);
                                
                            } else if(cellIndex === 2) {
                                bcsComunes.ventanaAbrir("PosicionMRF",{},false,false,$scope.winID);                                
                            }
                        });
                    });
                }

	};

	$scope.data.ConfMetodo={
		tipo : "combobox-metodo-posicion-mrf",
		configuracion : {},
		filtros : { 
					IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
					IdTipoPosicion : { "field"   : "IdTipoPosicion", "operator": "ne", "value" : "INICIAL" }
				  },
		idActual : false,
		seleccionado : false,
		autoCargar: true 
};	

	$scope.show = true;

	$scope.posicionFinal = {};
	$scope.posicionInicial = {};
	$scope.valorPosicionFinal = 0;
	$scope.valorPosicionInicial = 0;

	$scope.ObtenerPosicion = function() {
		return bcsServiciosMesa.ConsultarPosicionResumenMRF().$promise.then(function(respuesta) {
			for (var i = 0; i < respuesta.d.results.length; i++) {

				if (($scope.data.oConfiguracionMesas.idActual === respuesta.d.results[i].IdMesa)){
					if (respuesta.d.results[i].IdTipoPosicion === "INICIAL" ) {
					 	$scope.posicionInicial = {
							NombreMetodo: respuesta.d.results[i].NombreMetodo,
							IdMetodo: respuesta.d.results[i].IdMetodo,
							IdTipoPosicion: respuesta.d.results[i].IdTipoPosicion
						};
					} else {
						$scope.posicionFinal = {
							NombreMetodo: respuesta.d.results[i].NombreMetodo,
							IdMetodo: respuesta.d.results[i].IdMetodo,
							IdTipoPosicion: respuesta.d.results[i].IdTipoPosicion
						};
					};					
				}
			}
			console.log($scope.posicionInicial);

		}).catch(function(error) {
			console.error("Error fetching data:", error);
			throw error; // Re-throwing the error for proper error handling
		});
	};
	
	$scope.buscarPosicionFinalMRF = function() {
		return $scope.ObtenerPosicion().then(function() {
			var datosPosicionMRF = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'MRF',
				p_IdPais: $scope.SesionIdPaisSistema,
				p_FechaPosicion: $scope.SesionFechaSistema,
				p_IdMetodo: $scope.posicionFinal.IdMetodo,
				p_NombreMetodo:$scope.posicionFinal.NombreMetodo,
				p_TipoPosicion: $scope.posicionFinal.IdTipoPosicion
			};
		
			var p1 = bcsServiciosMesa.ConsultarPosicionMRF2(datosPosicionMRF).$promise;
			bcsComunes.ventanaOverlay($scope.winID, true);
			return p1.then(function(result) {
				var PosicionMRF = [];
				for (var posIndex in result.P_OUT_POSICION) {
					var CampoPosicion = {};
					CampoPosicion.IdMetodo = result.P_OUT_POSICION[posIndex].IdMetodo;
					CampoPosicion.NombreMetodo = result.P_OUT_POSICION[posIndex].NombreMetodo;
					CampoPosicion.IdTipoReajuste = result.P_OUT_POSICION[posIndex].IdTipoReajuste;
					CampoPosicion.IdMesa = result.P_OUT_POSICION[posIndex].IdMesa;
					CampoPosicion.NomMesa = result.P_OUT_POSICION[posIndex].NomMesa;
					CampoPosicion.IdEvento = result.P_OUT_POSICION[posIndex].IdEvento;
					CampoPosicion.IdTipoProducto = result.P_OUT_POSICION[posIndex].IdTipoProducto;
					CampoPosicion.IdTipoOperacion = result.P_OUT_POSICION[posIndex].IdTipoOperacion;
					CampoPosicion.IdTipoEvento = result.P_OUT_POSICION[posIndex].IdTipoEvento;
					CampoPosicion.NombreEvento = result.P_OUT_POSICION[posIndex].NombreEvento;
					CampoPosicion.NombreAgrupacion = result.P_OUT_POSICION[posIndex].NombreAgrupacion;
					CampoPosicion.IndAccion = result.P_OUT_POSICION[posIndex].IndAccion;
					CampoPosicion.Debe = result.P_OUT_POSICION[posIndex].Debe;
					CampoPosicion.Haber = result.P_OUT_POSICION[posIndex].Haber;
	
					PosicionMRF.push(CampoPosicion);
				}
	
				//$scope.data.frmMesaPosicion = result.P_OUT_NOMBREMESA;
				//$scope.data.oConfiguracionMesas.idActual = result.P_OUT_IDMESA;
				$scope.data.frmPosicionFinal = Number(result.P_OUT_POSICIONFINAL);
				bcsComunes.ventanaOverlay($scope.winID, false);
	
				return $scope.data.frmPosicionFinal;
			});
		}).catch(function(error) {
			console.error("Error fetching data:", error);
			// Handle error
			throw error; // Propagate the error
		});
	};
	
	$scope.buscarPosicionInicialMRF = function() {
		return $scope.ObtenerPosicion().then(function() {
			var datosPosicionMRF = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'MRF',
				p_IdPais: $scope.SesionIdPaisSistema,
				p_FechaPosicion: $scope.SesionFechaSistema,
				p_IdMetodo: $scope.posicionInicial.IdMetodo,
				p_NombreMetodo: $scope.posicionInicial.NombreMetodo,
				p_TipoPosicion: $scope.posicionInicial.IdTipoPosicion
			};
	
			console.log(datosPosicionMRF);
	
			var p1 = bcsServiciosMesa.ConsultarPosicionMRF2(datosPosicionMRF).$promise;
			bcsComunes.ventanaOverlay($scope.winID, true);
			return p1.then(function(result) {
				var PosicionMRF = [];
				for (var posIndex in result.P_OUT_POSICION) {
					var CampoPosicion = {};
					CampoPosicion.IdMetodo = result.P_OUT_POSICION[posIndex].IdMetodo;
					CampoPosicion.NombreMetodo = result.P_OUT_POSICION[posIndex].NombreMetodo;
					CampoPosicion.IdTipoReajuste = result.P_OUT_POSICION[posIndex].IdTipoReajuste;
					CampoPosicion.IdMesa = result.P_OUT_POSICION[posIndex].IdMesa;
					CampoPosicion.NomMesa = result.P_OUT_POSICION[posIndex].NomMesa;
					CampoPosicion.IdEvento = result.P_OUT_POSICION[posIndex].IdEvento;
					CampoPosicion.IdTipoProducto = result.P_OUT_POSICION[posIndex].IdTipoProducto;
					CampoPosicion.IdTipoOperacion = result.P_OUT_POSICION[posIndex].IdTipoOperacion;
					CampoPosicion.IdTipoEvento = result.P_OUT_POSICION[posIndex].IdTipoEvento;
					CampoPosicion.NombreEvento = result.P_OUT_POSICION[posIndex].NombreEvento;
					CampoPosicion.NombreAgrupacion = result.P_OUT_POSICION[posIndex].NombreAgrupacion;
					CampoPosicion.IndAccion = result.P_OUT_POSICION[posIndex].IndAccion;
					CampoPosicion.Debe = result.P_OUT_POSICION[posIndex].Debe;
					CampoPosicion.Haber = result.P_OUT_POSICION[posIndex].Haber;
	
					PosicionMRF.push(CampoPosicion);
				}
	
				//$scope.data.frmMesaPosicion = result.P_OUT_NOMBREMESA;
				//$scope.data.oConfiguracionMesas.idActual = result.P_OUT_IDMESA;
				$scope.data.frmPosicionInicial = Number(result.P_OUT_POSICIONFINAL);
				console.log($scope.data.frmPosicionInicial);
				bcsComunes.ventanaOverlay($scope.winID, false);
	
				return $scope.data.frmPosicionInicial;
	
			});
		}).catch(function(error) {
			console.error("Error fetching data:", error);
			// Handle error
			throw error; // Propagate the error
		});
	};
    

	$scope.buscarPosicionMRFFinal = function() {
		// Ejecutar buscarPosicionFinalMRF
		var posicionFinalPromise = $scope.buscarPosicionFinalMRF();
	
		// Luego, esperar a que la promesa se resuelva
		posicionFinalPromise.then(function(posicionFinal) {
			if (!$scope.data.oConfiguracionMesas.seleccionado) {
				bcsComunes.msg("warning", "info", $translate.instant('UI_DEBE_INDICAR_MESA'));
			} else {
				bcsComunes.ventanaOverlay($scope.winID, true);
				var datosPosicionMRF = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdPais: $scope.SesionIdPaisSistema,
					p_IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa
				};
				var p1 = bcsServiciosMesa.ConsultarResumenPosicionMRF(datosPosicionMRF).$promise;
				p1.then(function(result) {
					var PosicionMRF = [];
					for (var posIndex in result.P_OUT_RESUMENPOSICION) {
						var CampoPosicion = {};
						CampoPosicion.IdTipoReajuste = result.P_OUT_RESUMENPOSICION[posIndex].IdTipoReajuste;
						CampoPosicion.PosicionFinal = posicionFinal;
						$scope.valorPosicionFinal = posicionFinal;
						CampoPosicion.PosicionInicial = $scope.valorPosicionInicial;
						PosicionMRF.push(CampoPosicion);
					}
	
					$scope.data.gridConfiguracionMRF.dataSource.data(PosicionMRF);
	
					bcsComunes.ventanaOverlay($scope.winID, false);
				}).catch(function(error) {
					console.error("Error al consultar el resumen de posición MRF:", error);
					bcsComunes.msg("error", "info", "Error al consultar el resumen de posición MRF");
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
			}
		}).catch(function(error) {
			console.error("Error al buscar la posición final MRF:", error);
			bcsComunes.msg("error", "info", "Error al buscar la posición final MRF");
		});
	};

	$scope.buscarPosicionMRFInicial = function() {
		// Ejecutar buscarPosicionFinalMRF
		$scope.buscarPosicionInicialMRF().then(function(posicionInicial) {
			console.log("ASDASD" + posicionInicial);
			if (!$scope.data.oConfiguracionMesas.seleccionado) {
				bcsComunes.msg("warning", "info", $translate.instant('UI_DEBE_INDICAR_MESA'));
			} else {
				bcsComunes.ventanaOverlay($scope.winID, true);
				var datosPosicionMRF = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdPais: $scope.SesionIdPaisSistema,
					p_IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa
				};
				var p1 = bcsServiciosMesa.ConsultarResumenPosicionMRF(datosPosicionMRF).$promise;
				p1.then(function(result) {
					var PosicionMRF = [];
					for (var posIndex in result.P_OUT_RESUMENPOSICION) {
						var CampoPosicion = {};
						CampoPosicion.IdTipoReajuste = result.P_OUT_RESUMENPOSICION[posIndex].IdTipoReajuste;
						CampoPosicion.PosicionInicial = posicionInicial;
						CampoPosicion.PosicionFinal = $scope.valorPosicionFinal;
						$scope.valorPosicionInicial = posicionInicial;
						PosicionMRF.push(CampoPosicion);
					}
	
					$scope.data.gridConfiguracionMRF.dataSource.data(PosicionMRF);
	
					bcsComunes.ventanaOverlay($scope.winID, false);
				}).catch(function(error) {
					console.error("Error al consultar el resumen de posición MRF:", error);
					bcsComunes.msg("error", "info", "Error al consultar el resumen de posición MRF");
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
			}
		}).catch(function(error) {
			console.error("Error al buscar la posición final MRF:", error);
			bcsComunes.msg("error", "info", "Error al buscar la posición final MRF");
		});
	};
	
	

	
	// funcion WebSocket
	$scope.msgWebsocket = function(message) {    	
		if(message.Evento==='ACTMRF' || message.Evento==='ANUMRF' || message.Evento==='MODMRF' || message.Evento==='SIMFACT' || message.Evento==='ACTPOSMRF' || message.Evento==='ANUFACT' || message.Evento==='GENFACT' || message.Evento==='ANTMRF'){
			$scope.buscarPosicionMRFFinal();
			$scope.buscarPosicionMRFInicial();
		}
	};

	$scope.data.WinIngOperacion.WinResumenMRF = $scope;
	
	$scope.$watch('data.oConfiguracionMesas.seleccionado', function(newval,oldval ){
		if ($scope.data.oConfiguracionMesas.seleccionado && $scope.data.gridConfiguracionMRF != undefined) {
			$scope.buscarPosicionMRFFinal();
			$scope.buscarPosicionMRFInicial();

		}
		$scope.data.oConfiguracionMesas.seleccionado = newval;

	});

});
