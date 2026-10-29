(function () {
    angular.module('ModuloSocioNegocio').controller(
        'ModalEnrolamientoGrupoDireccionElectronicaDetalleEnrolCtrl',
        function ($scope, $resource, $translate, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma) {
            const mgdied = this;
            let idVentana = bcsComunes.ventanaWinId($scope);
            let parametros = bcsComunes.ventanaParams(idVentana);

            mgdied.data = parametros.Grupo;
           
            mgdied.IdEnrolamiento = parametros.IdEnrolamiento;
            mgdied.comboDireccion = {};
            mgdied.comboDireccion.filtros = {
                IdEnrolamiento: { field: "IdEnrolamiento", operator: "eq", value: Number(parametros.IdEnrolamiento) }
            };

            mgdied.CodigoFormulario = parametros.CodigoFormularioDetalle;

            bcsServicioPlataforma.CamposRequeridosFormulario(mgdied.CodigoFormulario, function (respuesta) {
                mgdied.camposRequeridos = respuesta;
                mgdied.asignarValoresPorDefecto();
                mgdied.dataOriginal = $.extend({},  mgdied.data);
            });

            mgdied.direccionElectronicaOptions = {
                dataSource: new bcs.DataSource({
                    type: "odata",
                    definition: {
                        serverFiltering: true,
                        transport: {
                            read: {
                                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700214",
                                dataType: "json"
                            }
                        },
                        filter: [
                            {
                                field: "IdGrupoDireccion",
                                operator: "eq",
                                value: Number(parametros.Grupo.IdGrupoDireccion)
                            }
                        ],
                        schema: {
                            model: {
                                fields: {
                                    IdGrupoDireccionDetalle: { type: "number" },
                                    IdDireccionElectronica: { type: "number" },
                                    IdTipoDestinatario: { type: "string" },
                                    IdGrupoDireccion: { type: "number" },
                                    Direccion: { type: "string" },
                                    NombreTipoDestinatario: { type: "string" }
                                }
                            }
                        }
                    }
                }),
                reorderable: true,
                filterable: false,
                groupable: false,
                pageable: false,
                selectable: "row",
                columnMenu: false,
                resizable: true,
                autoBind: true,
                scrollable: true,
                height: 200,
                columns: [
                    {
                        field: "Direccion",
                        title: $translate.instant('UI_DIRECCION'),
                        attributes: { style: "text-align:left;" },
                        width: "200px"
                    },
                    {
                        field: "NombreTipoDestinatario",
                        title: $translate.instant('UI_TIPO_DESTINATARIO'),
                        attributes: { style: "text-align:left;" },
                        width: "100px"
                    }
                ]
            };

            mgdied.agregar = function () {
                const entradaServicio = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'N',
                    p_CodigoFormulario: '',
                    p_IdTipoCanal: 'OPT',
                    p_GrupoDireccionDetalles: [
                        {
                            IdDireccionElectronica: mgdied.data.IdDireccionElectronica || 0,
                            IdTipoDestinatario: mgdied.data.IdTipoDestinatario || '',
                            IdGrupoDireccion: parametros.Grupo.IdGrupoDireccion || 0
                        }
                    ]
                };

                servicioSociodeNegocio.crearBorradorGrupoDireccionDetalle(entradaServicio).$promise.then(
                    function (result) {
                        bcsComunes.ventanaMsg({
                            mensaje: "Grupo de Direcciones creado",
                            conflictos: util.parseServerResponse(result)
                        }, idVentana);
                        mgdied.direccionElectronicaOptions.dataSource.read();

                    },
                    function (result) {
                        bcsComunes.msg("error", "Error", "La operacion tiene errores");
                    }
                );
            };

            mgdied.aceptar = function () {
                const requestBody = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'S',
                    p_CodigoFormulario: mgdied.CodigoFormulario,
                    p_IdTipoCanal: 'OPT',
                    p_GruposDirecciones: [
                        {
                            IdEnrolamiento: mgdied.IdEnrolamiento,
                            IdGrupoDireccion: parametros.Grupo.IdGrupoDireccion,
                            Grupo: mgdied.data.Grupo,
                            Comentario: mgdied.data.Comentario ? mgdied.data.Comentario : '',
                        }
                    ]
                };

                servicioSociodeNegocio.modificarBorradorGrupoDireccion(requestBody).$promise.then(
                    function (result) {
                        const idMensaje = bcsComunes.ventanaMsg({
                            mensaje: "Grupo de Direcciones modificado",
                            conflictos: util.parseServerResponse(result)
                        }, idVentana);

                        bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaOverlay(idVentana, false);
                            bcsComunes.ventanaCerrar(idVentana);
                        });
                    },
                    function (result) {
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.msg("error", "Error", "La operacion tiene errores");
                    }
                );
            };

            mgdied.eliminar = function () {
                const requestBody = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'N',
                    p_CodigoFormulario: '',
                    p_IdTipoCanal: 'OPT',
                    p_GrupoDireccionDetalles: [
                        {
                            IdGrupoDireccionDetalle: mgdied.direccionElectronicaSeleccionada.IdGrupoDireccionDetalle
                        }
                    ],
                };

                bcsComunes.ventanaOverlay(idVentana, true);

                servicioSociodeNegocio.eliminarBorradorGrupoDireccionDetalle(requestBody).$promise.then(
                    function (result) {
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.ventanaMsg({
                            mensaje: 'Dirección Eliminada',
                            conflictos: util.parseServerResponse(result)
                        });

                        mgdied.direccionElectronicaOptions.dataSource.read();
                    },
                    function (result) {
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.msg('error', 'Error', 'La operación tiene errores');
                    }
                );
            };
            
            mgdied.asignarValoresPorDefecto = function (){
				$.each(mgdied.camposRequeridos ,function(key,value){
					let noEsRequerido = value.Requerido === 'N';
					let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					let esCampoEnBlanco = !mgdied.data[value.CodigoValorFrontend];
					if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
						mgdied.data[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
					}
				});
			}

            mgdied.cancelar = function () {
                bcsComunes.ventanaData(idVentana, false);
                bcsComunes.ventanaCerrar(idVentana);
            };

            // No operation
            mgdied.noop = function () {
            }
        }
    );
}());
//------------------------- FIN CONTROLLER --------------------------