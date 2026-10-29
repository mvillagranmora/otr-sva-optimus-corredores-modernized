(function () {
    angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPersonaGrupoDireccionesElectronicasEnrolCtrl', function (
        $scope,
        $translate,
        bcsComunes,
        bcsAlerta,
        servicioSociodeNegocio
    ) {
        /******************************************************
         * INICIO DE VARIABLES
         * ***************************************************/
        const ppde = this;
        ppde.idVentana = bcsComunes.ventanaWinId($scope);

        ppde.abrirModal = function abrirModal(idEnrolamiento) {
            const id1 = bcsComunes.ventanaAbrir('agregarEnrolamientoGrupoDireccionElectronica', {
                IdEnrolamiento: idEnrolamiento,
                CodigoFormulario: ppde.CodigoFormulario,
                CodigoFormularioDetalle: ppde.CodigoFormularioDetalle,
            }, false, false, ppde.idVentana);

            const promesa = bcsComunes.ventanaPromesa(id1);
            promesa.then(function () {
                ppde.direccionElectronicaOptions.dataSource.read();
            });
        };

        ppde.agregar = function agregar() {
            ppde.abrirModal(ppde.IdEnrolamiento);
        };

        ppde.eliminar = function(grupoSeleccionado) {
            const requestBody = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'SOC',
                p_UsaReglasNegocio: 'N',
                p_CodigoFormulario: '',
                p_IdTipoCanal: 'OPT',
                p_GruposDirecciones: [
                    {
                        IdGrupoDireccion: grupoSeleccionado.IdGrupoDireccion
                    }
                ],
            };

            bcsComunes.ventanaOverlay(ppde.idVentana, true);

            servicioSociodeNegocio.eliminarBorradorGrupoDireccion(requestBody).$promise.then(
                function (result) {
                    if (result.p_OUT_ERRORESCONTADOR > 0) {
                        bcsComunes.msg('error', 'Error', 'La operacion tiene errores');
                        bcsComunes.ventanaOverlay(ppde.idVentana, false);
                        return;
                    }

                    bcsComunes.ventanaMsg(
                        {
                            mensaje: 'Grupo Direccion Borrado',
                            conflictos: util.parseServerResponse(result)
                        },
                        ppde.idVentana
                    );

                    ppde.direccionElectronicaOptions.dataSource.read();
                    bcsComunes.ventanaOverlay(ppde.idVentana, false);
                },
                function () {
                    bcsComunes.msg('error', 'Error', 'La operacion tiene errores');
                    bcsComunes.ventanaOverlay(ppde.idVentana, false);
                }
            );
        };

        ppde.modificar = function modificar(grupoSeleccionado) {
            const id1 = bcsComunes.ventanaAbrir(
                'agregarEnrolamientoGrupoDireccionElectronicaDetalle',
                {
                    IdEnrolamiento: ppde.IdEnrolamiento,
                    Grupo: grupoSeleccionado,
                    CodigoFormulario: ppde.CodigoFormulario,
                    CodigoFormularioDetalle: ppde.CodigoFormularioDetalle,
                },
                false,
                false,
                ppde.idVentana
            );

            const promesa = bcsComunes.ventanaPromesa(id1);
            promesa.then(function () {
                ppde.direccionElectronicaOptions.dataSource.read();
            });
        };

        $scope.admSoc.socioNegocioPromise.then(function () {
            ppde.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
            ppde.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['GRUPO_DIRECCION_ELECTRONICA'];
            ppde.CodigoFormularioDetalle = $scope.admSoc.dataSocioNegocio.Matriz['GRUPO_DIRECCION_ELECTRONICA_DETALLE'];
            ppde.direccionElectronicaOptions = {
                dataSource: new bcs.DataSource(
                    {
                        type: 'odata',
                        definition: {
                            serverFiltering: true,
                            transport: {
                                read: {
                                    url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700213',
                                    dataType: 'json'
                                }
                            },
                            filter: { field: 'IdEnrolamiento', operator: 'eq', value: Number(ppde.IdEnrolamiento) },
                            schema: {
                                model: {
                                    fields: {
                                        IdGrupoDireccion: { type: 'number' },
                                        IdEnrolamiento: { type: 'number' },
                                        Grupo: { type: 'string' },
                                        Comentario: { type: 'string' }
                                    }
                                }
                            }
                        }
                    }),
                reorderable: true,
                filterable: false,
                groupable: false,
                pageable: false,
                selectable: 'row',
                columnMenu: false,
                resizable: true,
                autoBind: true,
                scrollable: true,
                height: 200,
                columns: [
                    {
                        field: 'Grupo',
                        title: $translate.instant('UI_GRUPO'),
                        attributes: { style: 'text-align:left;' },
                        width: '370px'
                    },
                    {
                        field: 'Comentario',
                        title: $translate.instant('UI_COMENTARIO'),
                        attributes: { style: 'text-align:left;' },
                        width: '500px'
                    }
                ]
            };
        });
    });
}());
//------------------------- FIN CONTROLLER --------------------------