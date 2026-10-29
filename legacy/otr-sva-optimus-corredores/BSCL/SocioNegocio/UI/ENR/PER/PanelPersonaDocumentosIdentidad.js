(function () {
    angular.module('ModuloSocioNegocio').controller(
        'PanelEnrolamientoPersonaDocumentosIdentidadEnrolCtrl',
        function ($scope, $translate, bcsComunes, servicioSociodeNegocio) {
            /******************************************************
             * INICIO DE VARIABLES
             * ***************************************************/
            var ppdi = this;
            ppdi.idVentana = bcsComunes.ventanaWinId($scope);

			ppdi.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DOCUMENTO_IDENTIDAD']
            ppdi.abrirModal = function (IdEnrolamiento, DocumentoIdentidad) {
                const id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoDocumentoIdentidad", {
                    DocumentoIdentidad: DocumentoIdentidad,
                    IdEnrolamiento: IdEnrolamiento,
                    CodigoTipoPersona: $scope.admSoc.dataSocioNegocio.CodigoTipoPersona,
					CodigoFormulario: ppdi.CodigoFormulario
                }, false, false, ppdi.idVentana);

                bcsComunes.ventanaPromesa(id1).then(function (data) {
                    if (data === true) {
                        ppdi.documentoIdentidadSeleccionado = undefined;
                        ppdi.documentoIdentidadOptions.dataSource.read();
                    }
                });
            };

            ppdi.agregar = function () {
                ppdi.abrirModal(ppdi.IdEnrolamiento, undefined);
            };

            ppdi.eliminar = function () {
                const requestBody = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'N',
                    p_CodigoFormulario: '',
                    p_IdTipoCanal: 'OPT',
                    p_DocumentosIdentidades: [
                        {
                            IdDocumentoIdentidad: ppdi.documentoIdentidadSeleccionado.IdDocumentoIdentidad
                        }
                    ]
                };

                servicioSociodeNegocio.eliminarBorradorDocumentoIdentidad(requestBody).$promise.then(
                    function (result) {
                        bcsComunes.ventanaMsg({
                            mensaje: "Documento Identidad Borrado",
                            conflictos: util.parseServerResponse(result)
                        }, ppdi.idVentana);

                        ppdi.documentoIdentidadOptions.dataSource.read();
                    },
                    function (result) {
                        bcsComunes.msg("error", "Error", "La operacion tiene errores");
                    }
                );
            };

            ppdi.modificar = function (doc) {
                ppdi.abrirModal(ppdi.IdEnrolamiento, doc);
            };

            $scope.admSoc.socioNegocioPromise.then(function () {
                ppdi.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
                ppdi.documentoIdentidadOptions = {
                    dataSource: new bcs.DataSource({
                        type: "odata",
                        definition: {
                            serverFiltering: true,
                            transport: {
                                read: {
                                    url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700207",
                                    dataType: "json"
                                },
                            },
                            filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppdi.IdEnrolamiento) },
                            schema: {
                                model: {
                                    fields: {
                                        IdDocumentoIdentidad: { type: "number" },
                                        IdEnrolamiento: { type: "number" },
                                        IdTipoDocumentoIdentidad: { type: "string" },
                                        DocumentoIdentidad: { type: "string" },
                                        NumeroSerie: { type: "string" },
                                        FechaEmision: { type: "date" },
                                        FechaVencimiento: { type: "date" },
                                        Predeterminado: { type: "string" },
                                        IdTipoDocumentoIdentidadAsociado: { type: "string" },
                                        TipoPersona: { type: "date" },
                                        DocumentoIdentidadAsociado: { type: "date" },
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
                            field: "DocumentoIdentidad",
                            title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),
                            attributes: { style: "text-align:left;" },
                            width: "200px"
                        }, {
                            field: "NumeroSerie",
                            title: $translate.instant('UI_NUMERO_SERIE'),
                            attributes: { style: "text-align:left;" },
                            width: "200px"
                        }, {
                            field: "FechaEmision",
                            title: $translate.instant('UI_FECHA_EMISION'),
                            attributes: { style: "text-align:left;" },
                            format: "{0:d}",
                            width: "100px"
                        }, {
                            field: "FechaVencimiento",
                            title: $translate.instant('UI_FECHA_VENCIMIENTO'),
                            attributes: { style: "text-align:left;" },
                            format: "{0:d}",
                            template: kendo.template("#if (kendo.toString(FechaVencimiento, 'd-MM-yyyy')=='31-12-0000') {##} else {##=kendo.toString(FechaVencimiento, 'dd-MM-yyyy')##}#"),
                            width: "100px"
                        }, {
                            field: "Predeterminado",
                            title: $translate.instant('UI_PREDETERMINADO'),
                            template: "<span translate>#: Predeterminado # </span>",
                            attributes: { style: "text-align:left;" },
                        }
                    ]
                };
            });

        }
    );
}());
//------------------------- FIN CONTROLLER --------------------------