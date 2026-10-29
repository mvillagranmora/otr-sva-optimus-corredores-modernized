(function () {
    angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoClienteDatosGeneralesEnrolCtrl', function (
        $scope,
        $translate,
        servicioSociodeNegocio,
        bcsComunes,
        $timeout,
        bcsConfiguracionSociodeNegocio,
        bcsServicioPlataforma
    ) {
        // /******************************************************
        //  * INICIO DE VARIABLES
        //  * ***************************************************/
        const pcdg = this;
        pcdg.ventanaId = bcsComunes.ventanaWinId($scope);
        pcdg.dataCliente = {};
        pcdg.dataSolicitudCambio = {};
        pcdg.dataAutorizacionEvento = {};
        pcdg.dataAutorizacionEvento.EventosVencidos = false;
        pcdg.dataSolicitudCambio.CambiosPendientes = false;
        pcdg.dataSolicitudCambio.CambioEstadoPendiente = false;
        pcdg.datosCargados = false;

        const parametros = bcsComunes.ventanaParams(pcdg.ventanaId);
        pcdg.IdEnrolamiento = parametros.IdEnrolamiento;

        bcsComunes.ventanaAutoOverlay(pcdg.ventanaId, 'id_tipoCliente input', 5000);

        pcdg.camposRequeridos = {};

		const asignarValoresPorDefecto = function asignarValoresPorDefecto() {
			$.each(pcdg.camposRequeridos, function (key, value) {
				const noEsRequerido = value.Requerido === 'N';
				const esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				const tipoCampo = value.TipoCampoFrontend;
				const esCampoEnBlanco = tipoCampo === 'bcs-input-texto' ? !pcdg.dataCliente[value.CodigoValorFrontend] : !pcdg.dataClienteOriginal[value.CodigoValorFrontend]

				if (noEsRequerido && esValorDefectoValido && esCampoEnBlanco) {
					let typedValue = value.ValorDefectoCampoFormulario;

					if (value.TipoDato === 'NUMBER') typedValue = parseInt(typedValue, 10);

					if (tipoCampo === 'bcs-input-texto'){
						pcdg.dataCliente['CLI_'+value.CodigoValorFrontend] = typedValue;
					} else {
						pcdg.dataClienteOriginal['CLI_'+value.CodigoValorFrontend] = typedValue;
					}
				}
			});
		};

        const cargarDatosMaestropcdg = function cargarDatosMaestropcdg() {
            $scope.admSoc.socioNegocioPromise.then(function () {
                pcdg.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['DATOS_GENERALES_CLIENTE'];
                bcsServicioPlataforma.CamposRequeridosFormulario(pcdg.CodigoFormulario, function (respuesta) {
                    pcdg.camposRequeridos = respuesta;
                });

                pcdg.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente
                                 ? $scope.admSoc.dataSocioNegocio.IdCliente
                                 : 0;


                const clienteDatosGeneralesDatasource = new bcs.DataSource({
                    type: "odata",
                    definition: {
                        serverFiltering: true,
                        transport: {
                            read: {
                                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700228",
                                dataType: "json"
                            }
                        },
                        filter: { field: "IdEnrolamiento", operator: "eq", value: Number(pcdg.IdEnrolamiento) },
                        schema: bcsConfiguracionSociodeNegocio.getSchemaBorradorCliente()
                    }
                });


                clienteDatosGeneralesDatasource.query().then(function () {
                    if (clienteDatosGeneralesDatasource.data().length !== 1) {
                        bcsComunes.ventanaOverlay(pcdg.ventanaId, false);
                    } else {
                        pcdg.dataCliente = $.extend({}, clienteDatosGeneralesDatasource.data()[0]);
                        pcdg.dataClienteOriginal = $.extend({}, pcdg.dataCliente);

                        const oCLI_EsClienteFinal = pcdg.dataClienteOriginal.CLI_EsClienteFinal === 'S';
                        pcdg.dataCliente.CLI_EsClienteFinal = oCLI_EsClienteFinal;
                        pcdg.dataClienteOriginal.CLI_EsClienteFinal = oCLI_EsClienteFinal;
                        asignarValoresPorDefecto();
                        pcdg.datosCargados = true;
                    }
                });
            });
        };

        pcdg.pantalla = {};
        pcdg.pantalla.deshabilitado = false;

        pcdg.oConfiguracionEjecutivo = {
            filtros: {
                IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
                IdEstado: { field: "IdEstado", operator: "eq", value: 'ACT' }
            }
        };

        pcdg.oConfiguracionSucursalPreferencia = {
            filtros: {
                IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }
            }
        };

        pcdg.oConfiguracionTipoCliente = {
            filtros: {
                IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
                IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" }
            }
        };

        /******************************************************
         * Funciones
         * ***************************************************/

        pcdg.guardarpcdg = function () {

            const datos = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'SOC',
                p_UsaReglasNegocio: 'S',
                p_CodigoFormulario: pcdg.CodigoFormulario,
                p_IdTipoCanal: 'OPT',
                p_ClientesDatosGenerales: [
                    {
                        IdEnrolamiento: pcdg.IdEnrolamiento,
                        IdTipoCliente: pcdg.dataCliente.CLI_IdTipoCliente || '',
                        IdCategoriaInversionista: pcdg.dataCliente.CLI_IdCategoriaInversionista || '',
                        IdSegmentoCliente: pcdg.dataCliente.CLI_IdSegmentoCliente || 0,
                        IdEmpleadoEjecutivo: pcdg.dataCliente.CLI_IdEmpleadoEjecutivo || pcdg.dataClienteOriginal.idEmpleadoEjecutivoDefecto || 0,
                        CodigoEjecutivoExterno: pcdg.dataCliente.CLI_CodigoEjecutivoExterno || '',
                        IdSucursalPreferencia: pcdg.dataCliente.CLI_IdSucursalPreferencia || 0,
                        EsClienteFinal: pcdg.dataCliente.CLI_EsClienteFinal ? 'S' : 'N',
						CodigoArea: pcdg.dataCliente.CLI_AreaCliente || '',
                    }
                ]
            };

            bcsComunes.ventanaOverlay(pcdg.ventanaId, true);
            servicioSociodeNegocio.modificarBorradorClienteDatosGenerales(datos).$promise.then(
                function (result) {
                    bcsComunes.ventanaOverlay(pcdg.ventanaId, false);
                    bcsComunes.ventanaMsg({
                        mensaje: $translate.instant('UI_DATOS_MODIFICADOS'),
                        conflictos: util.parseServerResponse(result)
                    }, pcdg.ventanaId);

                    cargarDatosMaestropcdg();

                },
                function () {
                    bcsComunes.ventanaOverlay(pcdg.ventanaId, false);
                    cargarDatosMaestropcdg();
                    bcsComunes.msg("error", "Error", $translate.instant('UI_OPERACION_TIENE_ERRORES'));
                }
            );
        };

        pcdg.cancelarpcdg = function () {
            pcdg.IdSocNegPersonaEjecutivo = false;
            $timeout(function () {
                pcdg.dataCliente = $.extend({}, pcdg.dataClienteOriginal);
                pcdg.IdSocNegPersonaEjecutivo = Number(pcdg.dataClienteOriginal.IdPersonaEjecutivo);
            });
        };

        pcdg.revisarencuesta = function () {

            if (pcdg.RegistroCategoriaInversionistaSeleccionado && pcdg.RegistroCategoriaInversionistaSeleccionado.TipoIngreso === 'Encuesta') {
                bcsComunes.ventanaAbrir("VerDetalleEncuesta", { IdRegistroEncuesta: pcdg.RegistroCategoriaInversionistaSeleccionado.IdRegistroEncuesta }, false, false, pcdg.ventanaId);
            }

        }

        pcdg.obtenerDatosComparables = function (data) {
            return {
                IdTipoCliente: data.CLI_IdTipoCliente,
                IdCategoriaInversionista: data.CLI_IdCategoriaInversionista || '',
                IdSegmentoCliente: data.CLI_IdSegmentoCliente || 0,
                IdEmpleadoEjecutivo: data.CLI_IdEmpleadoEjecutivo || 0,
                CodigoEjecutivoExterno: data.CLI_CodigoEjecutivoExterno,
                IdSucursalPreferencia: data.CLI_IdSucursalPreferencia || 0,
                EsClienteFinal: data.CLI_EsClienteFinal,
                CodigoArea: data.CLI_AreaCliente || '',
            }
        }

        pcdg.datosPorGuardar = function() {
			const dataOriginal = pcdg.obtenerDatosComparables(pcdg.dataClienteOriginal);
			const data = pcdg.obtenerDatosComparables(pcdg.dataCliente);
			const sonIguales = _.isEqual(data, dataOriginal);
			
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteDatosGenerales' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClienteDatosGenerales' });
			}		
		}

        var successFecha = function (result) {
            if (result.P_OUT_ERRORESCONTADOR === 0) {
                pcdg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
            } else {
                pcdg.kngFechaSistema = '0001-01-01';
                console.log("Error." + result.P_OUT_ERRORESCONTADOR);
                var i = 0;
                for (i; i < result.P_OUT_ERRORESCONTADOR; i++) {
                    console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
                    console.log("Error2.");
                }
            }
        };
        var errorFecha = function (result) {
            console.log(JSON.stringify(result));
            console.log("revisar");
        };
        var seteoFechaSistema = function () {
            var datos = {};
            datos.p_IdEmpresa = $scope.SesionIdEmpresa;
            datos.p_IdUsuario = $scope.SesionIdUsuario;
            datos.p_IdModulo = 'SOC';
            datos.p_UsaReglasNegocio = 'S';
            bcsServicioPlataforma.FechaSistema(datos, successFecha, errorFecha);
        };

        seteoFechaSistema();

        /******************************************************
         * WATCHERS
         * ***************************************************/
         $scope.$watchCollection('pcdg.dataCliente', function() {
			if(pcdg.datosCargados){
				pcdg.datosPorGuardar();
			}
		});
        /******************************************************
         * GO
         * ***************************************************/

        cargarDatosMaestropcdg();
    });
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
