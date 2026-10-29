(function () {
    angular.module('ModuloSocioNegocio').controller('ventanaGrillaRequisitosEnrolamientoFullCtrl',
        function ($scope, bcsComunes, $translate) {
            const vg = this;
            const ventanaId = bcsComunes.ventanaWinId($scope);
            const parametros = bcsComunes.ventanaParams(ventanaId);

            vg.validacionActual = {};

            (function init() {
                const dataSource = new bcs.DataSource({
                    type: "array",
                    definition: {
                        schema: {
                            model: {
                                fields: {
                                    IdTipoEntidad: { type: 'string' },
                                    Descripcion: { type: 'string' },
                                    Ubicacion: { type: 'string' },
                                }
                            }
                        }
                    }
                });


                vg.grillaRequisitosOptions = {
                    gridCreated: function () {
                        vg.grillaRequisitosOptions.dataSource.data(parametros.validaciones);
                    },
                    dataSource: dataSource,
                    reorderable: true,
                    filterable: false,
                    groupable: false,
                    pageable: false,
                    selectable: "row",
                    columnMenu: false,
                    resizable: true,
                    autoBind: true,
                    scrollable: true,
                    height: 300,
                    columns: [
                        {
                            field: "Ubicacion",
                            title: $translate.instant('UI_UBICACION'),
                            attributes: { style: "text-align:left;" },
                            width: "250px"
                        }, {
                            field: "Descripcion",
                            title: $translate.instant('UI_DESCRIPCION'),
                            attributes: { style: "text-align:left;" },
                            width: "300px"
                        }
                    ]
                };
            })();

            vg.cerrar = function cerrar() {
                bcsComunes.ventanaCerrar(ventanaId);
            };
        });
}());
