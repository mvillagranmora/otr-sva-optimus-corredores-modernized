(function () {
    angular.module('ModuloSocioNegocio').controller('ModalEnrolamientoPortafolioCrearPreferenciaPagoEnrolCtrl', function ($scope, $translate, bcsComunes, servicioSociodeNegocio, bcsServicioPlataforma) {
        const mecppp = this;

        mecppp.idVentana = bcsComunes.ventanaWinId($scope);
        mecppp.parametros = bcsComunes.ventanaParams(mecppp.idVentana);
		mecppp.CodigoFormulario = mecppp.parametros.CodigoFormulario;
        mecppp.domVentana = $("[ventanaid='" + mecppp.idVentana + "']");
        mecppp.win = {};
        mecppp.datos = {};
        mecppp.cmbbx = {};
        mecppp.ds = {};
        mecppp.ktv = {};
        mecppp.fn = {};
        mecppp.datosAntes = {};
        mecppp.datosDespues = {};

        mecppp.isOperacionMesaPaga = false;
        mecppp.isOperacionMesaRecibe = true;

        (function validarParametrosIniciales() {
            const faltaIdEnrolamiento = !mecppp.parametros.IdEnrolamiento;
            const faltaRecibePagaPortafolio = !mecppp.parametros.RecibePagaPortafolio;

            if (faltaIdEnrolamiento || faltaRecibePagaPortafolio) {
                bcsComunes.msg("error", "Abrir Ventana", 'No se reciben parámetros iniciales');
                bcsComunes.ventanaCerrar(mecppp.idVentana);
            }
        })();

        bcsServicioPlataforma.CamposRequeridosFormulario(mecppp.CodigoFormulario, function (respuesta) {
            mecppp.camposRequeridos = respuesta;
        });

        mecppp.win.guardar = function guardar() {
            const dataPreferenciaPago = mecppp.fn.datosCrear();
            bcsComunes.ventanaOverlay(mecppp.idVentana, true);

            servicioSociodeNegocio.crearBorradorPreferenciaPagoPortafolio(dataPreferenciaPago, function (result) {
				bcsComunes.ventanaOverlay(mecppp.idVentana, false);
                const idModal = bcsComunes.ventanaMsg({
                    mensaje: "Crear Preferencias Pago",
                    conflictos: util.parseServerResponse(result)
                }, mecppp.idVentana);

                bcsComunes.ventanaPromesa(idModal).then(function () {
                    mecppp.win.cancelar();
                });

            },function(result){
				console.log(result);
				bcsComunes.msg("error","error","Error al crear preferencia pago");
				bcsComunes.ventanaOverlay(mecppp.idVentana,false);
			});
        };

        mecppp.win.cancelar = function cancelar() {
            bcsComunes.ventanaCerrar(mecppp.idVentana);
        };

        mecppp.cmbbx.prefPagoTipoOperacion = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": mecppp.parametros.RecibePagaPortafolio
                }
            },
        };

        mecppp.cmbbx.personaOperacionPortafolioCliente = {
            filtros: {},
        };


        mecppp.cmbbx.prefPagoMedioPago = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": mecppp.parametros.RecibePagaPortafolio
                },
            },
            read: false,
        };

        mecppp.cmbbx.prefPagoMoneda = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": mecppp.parametros.RecibePagaPortafolio
                },
                IdMedioDePago: {
                    "field": "IdMedioDePago",
                    "operator": "eq",
                    "value": "-1"
                }
            },
            read: false,
        };


        mecppp.cmbbx.personaCuentasBancarias = {
            filtros: {
                IdPersona: {
                    "field": "IdEnrolamiento",
                    "operator": "eq",
                    "value": mecppp.parametros.IdEnrolamiento
                },
                IdMoneda: {
                    "field": "IdMoneda",
                    "operator": "eq",
                    "value": "-1"
                },
            },
        };

        mecppp.cmbbx.personaCuentasBancariasCorredora = {
            filtros: {
                IdPersona: {
                    "field": "IdEmpresa",
                    "operator": "eq",
                    "value": $scope.$root.SesionIdEmpresa
                },
                IdMoneda: {
                    "field": "IdMoneda",
                    "operator": "eq",
                    "value": "-1"
                },
            },
        };

        mecppp.cmbbx.personaTarjetasBancarias = {
            filtros: {
                IdPersona: {
                    "field": "IdEnrolamiento",
                    "operator": "eq",
                    "value": mecppp.parametros.IdEnrolamiento
                },
            },
        };

        mecppp.fn.datosCrear = function datosCrear() {
			const idCuentaBancaria = mecppp.parametros.RecibePagaPortafolio === 'R' ? mecppp.datosDespues.IdCuentaBancaria : mecppp.datosDespues.IdCuentaBancariaCuenta;
            const requestBody = {
                p_IdEmpresa: $scope.$root.SesionIdEmpresa,
                p_IdUsuario: $scope.$root.SesionIdUsuario,
                p_IdModulo: 'SOC',
				p_CodigoFormulario : mecppp.parametros.CodigoFormulario,
				p_IdTipoCanal : 'OPT',
                p_UsaReglasNegocio: 'S',
                p_IdPreferenciaPagoPortafolio: mecppp.datosDespues.IdPreferenciaPagoPortafolio,
                p_PreferenciasPagoPortafolio: [{
					IdInterno : 1,
					IdEnrolamiento : mecppp.parametros.IdEnrolamiento,
                    IdMedioPago : mecppp.datosDespues.IdMedioDePago || '',
                    IdMoneda : mecppp.datosDespues.IdMoneda || '',
					IdPreferenciaPago : mecppp.datosDespues.IdPreferenciaPago || '',
					IdCuentaBancaria : 	idCuentaBancaria || 0,
					IdEmpresa : $scope.$root.SesionIdEmpresa,
					IdTarjetaBancaria : mecppp.datosDespues.IdTarjetaBancaria || 0,
					PorDefecto : mecppp.datosDespues.PorDefecto ? 'S' : 'N'
				}],
            };

            return requestBody;
        };


        mecppp.fn.cargaListaOperaciones = function cargaListaOperaciones() {
            mecppp.cmbbx.personaOperacionPortafolioCliente = {
                filtros: {
                    RecibePagaPortafolio: {
                        field: "RecibePagaPortafolio",
                        operator: "eq",
                        value: mecppp.parametros.RecibePagaPortafolio
                    },
                    IdMedioDePago: {
                        field: "IdMedioDePago",
                        operator: "eq",
                        value: mecppp.datosDespues.IdMedioDePago
                    },
                    IdMoneda: { field: "IdMoneda", operator: "eq", value: mecppp.datosDespues.IdMoneda },
                },
            };

            bcsComunes.ventanaOverlay(mecppp.idVentana, false);
        };

        mecppp.fn.iniciar = function iniciar() {
            if (mecppp.parametros.RecibePagaPortafolio === 'P') {
                bcsComunes.ventanaTitulo(mecppp.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_CLIENTE_PAGA'));
            }

            if (mecppp.parametros.RecibePagaPortafolio === 'R') {
                bcsComunes.ventanaTitulo(mecppp.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_CLIENTE_RECIBE'));
            }

            mecppp.datosAntes.PorDefecto = false;
            mecppp.datosDespues.PorDefecto = false;

            mecppp.datosDespues.operacion = {};
            mecppp.datosAntes.operacion = {};
        }

        $scope.$watch('mecppp.datosDespues.IdMoneda', function (newval) {
            if (newval) {
                mecppp.fn.cargaListaOperaciones();
            }
        });

        $scope.$watch("mecppp.datosDespues.IdMedioDePago", function (nval) {
            if (mecppp.parametros.RecibePagaPortafolio === 'R') {
                mecppp.isOperacionMesaRecibe = (nval && (nval === "CHE" || nval === "VVI" || nval === "VVE")) === false;
            }

        });

        $scope.$watch('mecppp.PersonaOperacionPortafolioClienteObj', function (newVal) {
            mecppp.isOperacionMesaPaga = newVal && (newVal.IdTipoProducto === "MEME" || newVal.IdTipoProducto === "MERF" || newVal.IdTipoProducto === "MEFW");
        });

        mecppp.fn.iniciar();
    });
}());