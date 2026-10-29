/********************************************************************
 * Purpose	: Controlador para generar un contrato seleciconando apoderados
 * Author	: RJARA - BCS
 * Date		: 11/04/2022
 *******************************************************************/
 (function () {
    angular.module('ModuloMesa').controller('ModalGenerarContratoCtrl',
        function ($scope, $translate, bcsComunes, $http) {
            /*********************************************
             * VARIABLES Y CONTEXTOS
             ********************************************/
            var gContrato = this;
            gContrato.idVentana = bcsComunes.ventanaWinId($scope);
            gContrato.parametros = bcsComunes.ventanaParams(gContrato.idVentana);
            var ParametroGENERAANEXO5UFCLP = '';
            var ParametroMonedaPrincipalOperacion = '';
            var ParametroMonedaSecundariaOperacion = '';
            var ParametroCantidadMinimaFirmantesCorredora = '';
            var ParametroCantidadMinimaFirmantesCliente = '';
            gContrato.ds = {};
            gContrato.cmbbx = {};
            gContrato.fn = {};
            $scope.data = {};
            $scope.dataTemplate = {};
            $scope.data.activarReporte = false;
            

            /*********************************************
             * EVALUACION INICIAL
             ********************************************/
            if (!gContrato.parametros.P_FOLIOOPERACION) {
                bcsComunes.ventanaCerrar(gContrato.idVentana);
            }

            /*********************************************
             * DATASOURCES
             *********************************************/
            gContrato.ds.ConsultaTemplate = new bcs.newDataSource({
                type: "odata",
                definition: {
                    serverFiltering: true,
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
                            dataType: "json",
                        }
                    },
                    schema: {
                        model: {
                            fields: {
                                CompanyId: { type: "string" },
                                Id: { type: "string" },
                                ReportName: { type: "string" }
                            }
                        }
                    }
                },
            });
            /*parametro GENERA ANEXO 5 PARA OPERACION FORWARD UFCLP */
			var dataSourceGENERAANEXO5UFCLP = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering	: true, 
					serverPaging 	: true, 
					serverSorting	: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType: "json"
						}		
					},
					schema: {
				     	model: {
				     		fields: { Valor : { type: "string"}	}
				     	}
					},
					filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'GENERAANEXO5UFCLP' },
					         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
				}
			});			
				
			dataSourceGENERAANEXO5UFCLP.fetch(function(){
				dsDefConsultaMovimientosFWD.fetch(function(){
    				if(dataSourceGENERAANEXO5UFCLP.data().length > 0){
	                    ParametroMonedaPrincipalOperacion = dsDefConsultaMovimientosFWD.data()[0].IdMonedaPrincipal;
	                    ParametroMonedaSecundariaOperacion= dsDefConsultaMovimientosFWD.data()[0].IdMonedaSecundaria;
	                    ParametroGENERAANEXO5UFCLP = dataSourceGENERAANEXO5UFCLP.data()[0].Valor
                    }
                });
		 	});
		 	
             /*parametro Cantidad minima firmantes corredora */
			var dataSourceCANTIDADAPODERADOSCORREDORA = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering	: true, 
					serverPaging 	: true, 
					serverSorting	: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType: "json"
						}		
					},
					schema: {
				     	model: {
				     		fields: { Valor : { type: "string"}	}
				     	}
					},
					filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'CANTIDADAPODERADOSCORREDORA' },
					         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
				}
			});			
				
			dataSourceCANTIDADAPODERADOSCORREDORA.fetch(function(){
				if(dataSourceCANTIDADAPODERADOSCORREDORA.data().length > 0){
                    ParametroCantidadMinimaFirmantesCorredora = dataSourceCANTIDADAPODERADOSCORREDORA.data()[0].Valor
				}
		 	});
             /*parametro Cantidad minima firmantes cliente */
			var dataSourceCANTIDADAPODERADOSCLIENTE = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering	: true, 
					serverPaging 	: true, 
					serverSorting	: true,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType: "json"
						}		
					},
					schema: {
				     	model: {
				     		fields: { Valor : { type: "string"}	}
				     	}
					},
					filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'CANTIDADAPODERADOSCLIENTE' },
					         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
				}
			});			
				
			dataSourceCANTIDADAPODERADOSCLIENTE.fetch(function(){
				if(dataSourceCANTIDADAPODERADOSCLIENTE.data().length > 0){
                    ParametroCantidadMinimaFirmantesCliente = dataSourceCANTIDADAPODERADOSCLIENTE.data()[0].Valor
				}
		 	});
			//tab Cartera
			var dsDefConsultaMovimientosFWD = new bcs.DataSource({
				type : "odata",
				definition: {
	                serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                transport: {
	                	read: {
	                		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=515",
	                		dataType:"json"
	                	}
	                },
	                schema: {
	                	model: {
	                		fields: {
								IdMonedaPrincipal		: {type: "string"},
								IdMonedaSecundaria		: {type: "string"}							}
	                	}
	                },
                        filter: [{ field: "IdCartera"	, operator: "eq", value: gContrato.parametros.P_FOLIOOPERACION },
                                { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
                        }
				})
                
            /*********************************************
             * COMBOBOX
             *********************************************/
                gContrato.cmbbx.oConfApoderadoCliente = {
                    tipo: 'combobox-apoderado-cliente',
                    filtros: {
                        documentoIdentidad: {
                            "field": "DocumentoIdentidad", "operator": "eq", "value": gContrato.parametros.P_RUTCLIENTE
                        },
                        idTipoRelacion: {
                            "field": "IdTipoRelacion", "operator": "eq", "value": "APO"
                        },
                        idEstado: {
                            "field": "IdEstado", "operator": "eq", "value": "ACT"
                        }
                    },
                };

            gContrato.cmbbx.oConfApoderadoCorredora = {
                tipo: 'combobox-apoderado-corredora',
                filtros: {
                    idEmpresa: {
                        "field": "IdEmpresa", "operator": "eq", "value": gContrato.parametros.P_IDEMPRESA
                    }
                },
            };

            /*********************************************
             * FUNCIONES
             *********************************************/

            gContrato.fn.validarCombobox = function () {
                let cantidadCorredoraSeleccionada =0;
                let cantidadClienteSeleccionada = 0;
                if (gContrato.apoderadoCorredoraPrimero.DocumentoIdentidad){
                    cantidadCorredoraSeleccionada++;
                }
                if (gContrato.apoderadoCorredoraSegundo.DocumentoIdentidad){
                    cantidadCorredoraSeleccionada++;
                }
                if (gContrato.apoderadoClientePrimero.DocumentoIdentidad){
                    cantidadClienteSeleccionada++;
                }
                if (gContrato.apoderadoClienteSegundo.DocumentoIdentidad){
                    cantidadClienteSeleccionada++;
                }
                if (gContrato.apoderadoCorredoraSegundo.DocumentoIdentidad === gContrato.apoderadoCorredoraPrimero.DocumentoIdentidad && gContrato.apoderadoCorredoraPrimero.DocumentoIdentidad != undefined) {
                    bcsComunes.msg("error", "Error Generar Contrato", 'Apoderados Corredora no pueden ser iguales');
                    return false;
                } else if ( (gContrato.apoderadoClientePrimero.DocumentoIdentidadDestino != undefined && gContrato.apoderadoClientePrimero.DocumentoIdentidadDestino != undefined) && (gContrato.apoderadoClienteSegundo.DocumentoIdentidadDestino === gContrato.apoderadoClientePrimero.DocumentoIdentidadDestino)) {
                    bcsComunes.msg("error", "Error Generar Contrato", 'Apoderados Cliente no pueden ser iguales');
                    return false;
                }
                if(cantidadCorredoraSeleccionada>=ParametroCantidadMinimaFirmantesCorredora){
                    if(cantidadClienteSeleccionada>= ParametroCantidadMinimaFirmantesCliente){
                        return true
                    }else {
                        bcsComunes.msg("error", "Error Generar Contrato", 'Error al seleccionar Apoderados cliente');
                        return false;
                    }
                } else {
                    bcsComunes.msg("error", "Error Generar Contrato", 'Error al seleccionar Apoderados corredora');
                    return false;
                }
            }

            gContrato.fn.generarContrato = function () {
                if (gContrato.fn.validarCombobox()) {
                    if(ParametroGENERAANEXO5UFCLP === 'S' && ParametroMonedaPrincipalOperacion === 'UF' && ParametroMonedaSecundariaOperacion === 'CLP' ){
                        gContrato.parametros.template = 'CONTRATOFWD_A5'
                    }
                    let apoderadoCorredoraSegundo = '334-4';
                    let apoderadoCorredoraPrimero = '334-4';
                    if(gContrato.apoderadoCorredoraSegundo.DocumentoIdentidad) {apoderadoCorredoraSegundo = gContrato.apoderadoCorredoraSegundo.DocumentoIdentidad}
                    if(gContrato.apoderadoCorredoraPrimero.DocumentoIdentidad) {apoderadoCorredoraPrimero = gContrato.apoderadoCorredoraPrimero.DocumentoIdentidad}

                    var filterTemplate = [{ field: "ReportName", operator: "eq", value: gContrato.parametros.template }];
                    let parametrosSP = {
                        $schema: gContrato.parametros.$schema,
                        P_FOLIOOPERACION: gContrato.parametros.P_FOLIOOPERACION,
                        P_IDEMPRESA: 1,
                        P_RUT_APODERADOCLIENTEPRIMERO: gContrato.apoderadoClientePrimero.DocumentoIdentidadDestino,
                        P_RUT_APODERADOCORREDORAPRIMERO: apoderadoCorredoraPrimero,
                        P_RUT_APODERADOCLIENTESEGUNDO: gContrato.apoderadoClienteSegundo.DocumentoIdentidadDestino,
                        P_RUT_APODERADOCORREDORASEGUNDO: apoderadoCorredoraSegundo
                    }
                    gContrato.ds.ConsultaTemplate.query({ filter: filterTemplate }).then(function () {
                        if (gContrato.ds.ConsultaTemplate.view().length > 0) {
                            $scope.dataTemplate.TipoReporte = 'pdf';
                            $scope.dataTemplate.Id = gContrato.ds.ConsultaTemplate.view()[0].Id;
                            $scope.dataTemplate.ReportName = gContrato.ds.ConsultaTemplate.view()[0].ReportName;
                            $scope.dataTemplate.data = { "serviceId": gContrato.parametros.serviceId, "serviceData": parametrosSP };
                            $scope.dataTemplate.subReports = new Array();
                            $scope.getReport($scope.dataTemplate);
                        }
                    });
                }
            }

            $scope.getReport = function (dataTemplate) {
                var parameters = {
                    "exportFormat": dataTemplate.TipoReporte,
                    "reportId": dataTemplate.Id,
                    "parameters": "",
                    "configuration": {
                        "mainReport": {
                            "reportName": dataTemplate.ReportName,
                            "data": dataTemplate.data,
                            "parameters": {}
                        },
                        "subReports": dataTemplate.subReports
                    }
                };

                $http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
                    .success(function (response) {
                        var file = new Blob([response], { type: 'application/pdf' });
                        var nombreArchivo = dataTemplate.ReportName + '_' + $scope.SesionFechaSistema.replace(/-/g, '');
                        saveAs(file, nombreArchivo + "." + dataTemplate.TipoReporte);
                        bcsComunes.ventanaOverlay(gContrato.idVentana, false);
                        $scope.data.activarReporte = false;
                    })
                    .error(function (response) {
                        bcsComunes.ventanaMsg({
                            mensaje: $translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
                            conflictos: { errores: [{ Codigo: 'COD', Descripcion: 'Un error inesperado ha ocurrido' }] }
                        }, gContrato.idVentana);
                    });
            };

        });
}());