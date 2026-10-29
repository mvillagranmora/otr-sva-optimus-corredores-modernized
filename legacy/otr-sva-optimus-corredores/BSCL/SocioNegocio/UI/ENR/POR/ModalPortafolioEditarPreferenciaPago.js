(function () {
    angular.module('ModuloSocioNegocio').controller('ModalEnrolamientoPortafolioEditarPreferenciaPagoEnrolCtrl', function ($scope, $translate, bcsComunes, servicioSociodeNegocio, bcsServicioPlataforma) {
        const meeppp = this;

        meeppp.idVentana = bcsComunes.ventanaWinId($scope);
        meeppp.parametros = bcsComunes.ventanaParams(meeppp.idVentana);
		meeppp.CodigoFormulario = meeppp.parametros.CodigoFormulario;
        meeppp.domVentana = $("[ventanaid='" + meeppp.idVentana + "']");
        meeppp.win = {};
        meeppp.datos = {};
        meeppp.cmbbx = {};
        meeppp.ds = {};
        meeppp.ktv = {};
        meeppp.fn = {};
        meeppp.datosAntes = {};
        meeppp.datosDespues = {};

        meeppp.isOperacionMesaPaga = false;
        meeppp.isOperacionMesaRecibe = true;

        (function validarParametrosIniciales() {
            const faltaIdEnrolamiento = !meeppp.parametros.IdEnrolamiento;
            const faltaRecibePagaPortafolio = !meeppp.parametros.RecibePagaPortafolio;

            if (faltaIdEnrolamiento || faltaRecibePagaPortafolio) {
                bcsComunes.msg("error", "Abrir Ventana", 'No se reciben parámetros iniciales');
                bcsComunes.ventanaCerrar(meeppp.idVentana);
            }
        })();

        bcsServicioPlataforma.CamposRequeridosFormulario(meeppp.CodigoFormulario, function (respuesta) {
            meeppp.camposRequeridos = respuesta;
        });

        meeppp.win.guardar = function guardar() {
            const dataPreferenciaPago = meeppp.fn.datosEditar();
			bcsComunes.ventanaOverlay(meeppp.idVentana, true);

            servicioSociodeNegocio.modificarBorradorPreferenciaPagoPortafolio(dataPreferenciaPago, function (result) {
				bcsComunes.ventanaOverlay(meeppp.idVentana, false);
                const idModal = bcsComunes.ventanaMsg({
                    mensaje: "Editar Preferencias Pago",
                    conflictos: util.parseServerResponse(result)
                }, meeppp.idVentana);

                bcsComunes.ventanaPromesa(idModal).then(function () {
                    meeppp.win.cancelar();
                });

            },function(result){
				console.log(result);
				bcsComunes.msg("error","error","Error al editar preferencia pago");
				bcsComunes.ventanaOverlay(meeppp.idVentana,false);
			});
        };

        meeppp.win.cancelar = function cancelar() {
            bcsComunes.ventanaCerrar(meeppp.idVentana);
        };

        meeppp.cmbbx.prefPagoTipoOperacion = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": meeppp.parametros.RecibePagaPortafolio
                }
            },
        };

        meeppp.cmbbx.personaOperacionPortafolioCliente = {
            filtros: {},
        };


        meeppp.cmbbx.prefPagoMedioPago = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": meeppp.parametros.RecibePagaPortafolio
                },
            },
            read: false,
        };

        meeppp.cmbbx.prefPagoMoneda = {
            filtros: {
                RecibePagaPortafolio: {
                    "field": "RecibePagaPortafolio",
                    "operator": "eq",
                    "value": meeppp.parametros.RecibePagaPortafolio
                },
                IdMedioDePago: {
                    "field": "IdMedioDePago",
                    "operator": "eq",
                    "value": "-1"
                }
            },
            read: false,
        };


        meeppp.cmbbx.personaCuentasBancarias = {
            filtros: {
                IdPersona: {
                    "field": "IdEnrolamiento",
                    "operator": "eq",
                    "value": meeppp.parametros.IdEnrolamiento
                },
                IdMoneda: {
                    "field": "IdMoneda",
                    "operator": "eq",
                    "value": "-1"
                },
            },
        };

        meeppp.cmbbx.personaCuentasBancariasCorredora = {
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

        meeppp.cmbbx.personaTarjetasBancarias = {
            filtros: {
                IdPersona: {
                    "field": "IdEnrolamiento",
                    "operator": "eq",
                    "value": meeppp.parametros.IdEnrolamiento
                },
            },
        };

        meeppp.fn.datosEditar = function datosEditar() {
			const idCuentaBancaria = meeppp.parametros.RecibePagaPortafolio === 'R' ? meeppp.datosDespues.IdCuentaBancaria : meeppp.datosDespues.IdCuentaBancariaCuenta;
            const requestBody = {
                p_IdEmpresa: $scope.$root.SesionIdEmpresa,
                p_IdUsuario: $scope.$root.SesionIdUsuario,
                p_IdModulo: 'SOC',
				p_CodigoFormulario : meeppp.parametros.CodigoFormulario,
				p_IdTipoCanal : 'OPT',
                p_UsaReglasNegocio: 'S',
                p_PreferenciasPagoPortafolio: [{
					IdInterno : 1,
					IdPreferenciaPagoPortafolio : meeppp.datosDespues.IdPreferenciaPagoPortafolio,
					IdEnrolamiento : meeppp.parametros.IdEnrolamiento,
                    IdMedioPago : meeppp.datosDespues.IdMedioDePago || '',
                    IdMoneda : meeppp.datosDespues.IdMoneda || '',
					IdPreferenciaPago : meeppp.datosDespues.IdPreferenciaPago || '',
					IdCuentaBancaria : 	idCuentaBancaria || 0,
					IdEmpresa : $scope.$root.SesionIdEmpresa,
					IdTarjetaBancaria : meeppp.datosDespues.IdTarjetaBancaria || 0,
					PorDefecto : meeppp.datosDespues.PorDefecto ? 'S' : 'N'
				}],
            };

            return requestBody;
        };


        meeppp.fn.cargaListaOperaciones = function cargaListaOperaciones() {
            meeppp.cmbbx.personaOperacionPortafolioCliente = {
                filtros: {
                    RecibePagaPortafolio: {
                        field: "RecibePagaPortafolio",
                        operator: "eq",
                        value: meeppp.parametros.RecibePagaPortafolio
                    },
                    IdMedioDePago: {
                        field: "IdMedioDePago",
                        operator: "eq",
                        value: meeppp.datosDespues.IdMedioDePago
                    },
                    IdMoneda: { field: "IdMoneda", operator: "eq", value: meeppp.datosDespues.IdMoneda },
                },
            };

            meeppp.listaOperaciones = undefined;
            bcsComunes.ventanaOverlay(meeppp.idVentana, false);
        };

        meeppp.fn.iniciar = function iniciar() {
            // Título ventana:
            // está modelado desde el punto de vista del cliente
            // pero los títulos se muestran desde el punto de vista de la corredora
            if (meeppp.parametros.RecibePagaPortafolio === 'P') {
                bcsComunes.ventanaTitulo(meeppp.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_CLIENTE_PAGA'));
            }

            if (meeppp.parametros.RecibePagaPortafolio === 'R') {
                bcsComunes.ventanaTitulo(meeppp.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_CLIENTE_RECIBE'));
            }

            meeppp.datosAntes.PorDefecto = false;
            meeppp.datosDespues.PorDefecto = false;

            // Filtros generales

            // Carga datos modo Edición
            if (meeppp.parametros.PreferenciaPagoObj) {
                meeppp.datosAntes = _.clone(meeppp.parametros.PreferenciaPagoObj);
                meeppp.datosDespues = _.clone(meeppp.parametros.PreferenciaPagoObj);
                meeppp.datosDespues.PorDefecto = meeppp.datosDespues.PorDefecto === "S";
                if (meeppp.parametros.RecibePagaPortafolio === 'P') {
                    meeppp.datosAntes.IdCuentaBancariaCuenta = meeppp.datosAntes.IdCuentaBancaria
                    meeppp.datosDespues.IdCuentaBancariaCuenta = meeppp.datosDespues.IdCuentaBancaria
                }
            }

            meeppp.datosDespues.operacion = {};
            meeppp.datosAntes.operacion = {};
        }

        $scope.$watch('meeppp.datosDespues.IdMoneda', function (newval) {
            if (newval) {
                meeppp.fn.cargaListaOperaciones();
            } else {
                meeppp.listaOperaciones = undefined;
            }
        });

        $scope.$watch("meeppp.datosDespues.IdMedioDePago", function (nval) {
            if (meeppp.parametros.RecibePagaPortafolio === 'R') {
                meeppp.isOperacionMesaRecibe = (nval && (nval === "CHE" || nval === "VVI" || nval === "VVE")) === false;
            }

        });

        $scope.$watch('meeppp.PersonaOperacionPortafolioClienteObj', function (newVal) {
            meeppp.isOperacionMesaPaga = newVal && (newVal.IdTipoProducto === "MEME" || newVal.IdTipoProducto === "MERF" || newVal.IdTipoProducto === "MEFW");
        });

        meeppp.fn.iniciar();
    });
}());