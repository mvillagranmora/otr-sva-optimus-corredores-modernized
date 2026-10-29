(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioCuentaCustodioEnrolCtrl',
	function ($scope,bcsComunes, servicioSociodeNegocio,$translate) {
		const pPortCcust 			= this;
		pPortCcust.idVentana 		= bcsComunes.ventanaWinId($scope);
		pPortCcust.parametros 		= bcsComunes.ventanaParams(pPortCcust.idVentana);
		pPortCcust.CodigoFormulario = pPortCcust.parametros.Matriz['CUENTA_CUSTODIO'];

		pPortCcust.crearCuentaCustodio = function crearCuentaCustodio(){
			const datosEnviarModal = {
				IdEnrolamiento		 	: $scope.admSoc.dataSocioNegocio.IdEnrolamiento,
				CodigoFormulario		: pPortCcust.CodigoFormulario
            };
			const winModal = bcsComunes.ventanaAbrir("ModalPortafolioCrearCuentaCustodio", datosEnviarModal, false,false,pPortCcust.idVentana);                            
            bcsComunes.ventanaPromesa(winModal).then(function(){
            	dsCuentaCustodio.read();
            	pPortCcust.cuentaCustodioSeleccionado = undefined;
            	
            });
		};

		pPortCcust.modificarCuentaCustodio = function modificarCuentaCustodio(cuentaCustodio){
			const datosEnviarModal = {
				IdEnrolamiento		 	: $scope.admSoc.dataSocioNegocio.IdEnrolamiento,
				CuentaCustodioObj	 	: cuentaCustodio,
				CuentaCustodioTodosObj 	: undefined,
				CodigoFormulario		: pPortCcust.CodigoFormulario
            };
			const winModal = bcsComunes.ventanaAbrir("ModalPortafolioEditarCuentaCustodio", datosEnviarModal, false,false,pPortCcust.idVentana);                            
            bcsComunes.ventanaPromesa(winModal).then(function(){
            	dsCuentaCustodio.read();
            	pPortCcust.cuentaCustodioSeleccionado = undefined;
            	
            });
		};
		
		pPortCcust.eliminar = function eliminar() {
			bcsComunes.ventanaOverlay(pPortCcust.idVentana,true);
            const requestBody = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'SOC',
                p_UsaReglasNegocio: 'N',
                p_CodigoFormulario: '',
                p_IdTipoCanal: 'OPT',
                p_CuentasCustodioPortafolio: [
                    {
                        IdCuentaCustodioPortafolio: pPortCcust.cuentaCustodioSeleccionado.IdCuentaCustodioPortafolio,
						Codigo : pPortCcust.cuentaCustodioSeleccionado.Codigo,
                    }
                ]
            };

            servicioSociodeNegocio.eliminarBorradorCuentaCustodioPortafolio(requestBody).$promise.then(
                function (result) {
                   bcsComunes.ventanaOverlay(pPortCcust.idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error","error","Cuenta Custodio: La operación tiene errores");
						return;
					}
					
					bcsComunes.msg("info","info","Cuenta Custodio: Eliminación OK");
					
					bcsComunes.ventanaMsg({ mensaje : "Cuenta Custodio borrada",conflictos : util.parseServerResponse(result)}, pPortCcust.idVentana);
					dsCuentaCustodio.read();
            		pPortCcust.cuentaCustodioSeleccionado = undefined;
                },
                function () {
					bcsComunes.ventanaOverlay(pPortCcust.idVentana,false);
                    bcsComunes.msg("error", "Error", "La operacion tiene errores");
                }
            );
        };
		
		const dsCuentaCustodio = new bcs.DataSource({
			type : "odata",
			definition : {
				pageSize : 100,
				serverFiltering: true,
	        	serverPaging: true,
				transport : {
					read : {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700267",
						dataType : "json"
					}
				},
				filter : { field: "IdEnrolamiento", operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdEnrolamiento)},
				schema : {
					model : {
						fields : {
							IdCuentaCustodioPortafolio	: { type: "number"},
							IdEnrolamiento				: { type: "number"},
							IdBoveda					: { type: "string"},
							NombreBoveda				: { type: "string"},
							Codigo						: { type: "string"},

							NombreModeloCustodia			: { type: "string"},
							NombreTipoCustodia				: { type: "string"},
							NombreTipoCuentaCustodia		: { type: "string"},
							CodigoBancoCustodio				: { type: "string"},
							CodigoBancoBancoLiquidador		: { type: "string"},
							
							LiquidaVencimientosUI			: { type: "string"},
							LiquidaCuponesUI				: { type: "string"},
							LiquidaDividendosUI				: { type: "string"},
							LiquidaPrepagosUI				: { type: "string"},
							
							
						}
					}
				},
			}
		});
		
		pPortCcust.setGridCuentaCustodio = function setGridCuentaCustodio(){
			const conf = {
				dataSource : dsCuentaCustodio,
				columns: [
				    {field: "NombreBoveda", title: $translate.instant('UI_BOVEDA'),width: "200px"},
				    {field: "Codigo", title: $translate.instant('UI_CODIGO'), width: "90px"},
				],
				sortable			: true,
				pageable			: true,
				autoBind			: true,
				height				: 280,
			    selectable 			: "row"
			}
			
			if ($scope.SesionLocale === 'PE') {
				conf.columns.push({field: "NombreModeloCustodia",			title: $translate.instant('UI_MODELO_CUSTODIA'),width: "120px"});
				conf.columns.push({field: "NombreTipoCustodia", 			title: $translate.instant('UI_TIPO_CUSTODIA'),width: "120px"});
				conf.columns.push({field: "NombreTipoCuentaCustodia", 		title: $translate.instant('UI_TIPO_CUENTA'),width: "120px"});
				conf.columns.push({field: "CodigoBancoCustodio", 			title: $translate.instant('UI_BANCO_CUSTODIO'),width: "120px"});
				conf.columns.push({field: "CodigoBancoBancoLiquidador", 	title: $translate.instant('UI_BANCO_LIQUIDADOR'),width: "120px"});
				conf.columns.push({field: "LiquidaVencimientosUI", 			title: $translate.instant('UI_LIQUIDA_VENCIMIENTOS'),width: "120px"});
				conf.columns.push({field: "LiquidaCuponesUI", 				title: $translate.instant('UI_LIQUIDA_CUPONES'),width: "120px"});
				conf.columns.push({field: "LiquidaDividendosUI", 			title: $translate.instant('UI_LIQUIDA_DIVIDENDOS'),width: "120px"});
				conf.columns.push({field: "LiquidaPrepagosUI", 				title: $translate.instant('UI_LIQUIDA_PREPAGOS'),width: "120px"});
				
			}
			return conf;
		};
		pPortCcust.gridCuentaCustodio = pPortCcust.setGridCuentaCustodio();
	});
}());