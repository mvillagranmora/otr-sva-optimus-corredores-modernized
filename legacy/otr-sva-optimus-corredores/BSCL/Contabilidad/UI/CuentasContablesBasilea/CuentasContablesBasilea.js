(function () {
    angular.module('ModuloContabilidad').controller('CuentasContablesBasileaCtrl',
        function ($scope, $translate, $resource, bcsComunes, $http, bcsServicioContabilidad) {

            var obj = this;
            var idVentana = bcsComunes.ventanaWinId($scope);
            obj.filter = [];

            $scope.data = {};
            $scope.grid = {};


            obj.dsDefCuentasContablesBasilea = {
                type: "odata",
                definition: {
                    filter: obj.filter,
                    serverFiltering: true,
                    serverPaging: true,
                    serverSorting: false,
                    pageSize: 20,
                    sort: [
                        { field: "TipoCuentaContable", dir: "asc" }
                    ],
                    transport: { read: { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=701154", dataType: "json" } },
                    schema: {
                        model: {
                            fields: {
                                Concepto: { type: "string" },
                                TipoCuentaContable: { type: "string" },
                                CuentaContable: { type: "string" },
                                NombreCuentaContable: { type: "string" },
                                CodigoUsuario: { type: "string" },
                                FechaModificacionRegistro: { type: "sysdate" },
                                Estado: { type: "string" },
                                Producto: { type: "string" }
                            }
                        }
                    }
                }
            };

            var dsCuentasBasilea = new bcs.DataSource(obj.dsDefCuentasContablesBasilea);
            $scope.grid.confCuentasContablesBasilea = {
                dataSource: dsCuentasBasilea,
                columns: [
                    { field: "Concepto", title: "UI_CONCEPTO", width: "125px" },
                    { field: "TipoCuentaContable", title: "UI_TIPO_CUENTA_CONTABLE", width: "285px" },
                    { field: "CuentaContable", title: "UI_CUENTA_CONTABLE", width: "90px" },
                    { field: "NombreCuentaContable", title: "UI_NOMBRE_CUENTA_CONTABLE", width: "100px" },
                    { field: "CodigoUsuario", title: "UI_USUARIO", width: "60px" },
                    { field: "FechaModificacionRegistro", title: "UI_FECHA_MODIFICACION", width: "80px" },
                    { field: "Estado", title: "UI_ESTADO", width: "60px" },
                    { field: "Producto", title: "UI_PRODUCTO", width: "60px" }
                ],
                filterable: true,
                sortable: true,
                pageable: true,
                height: 500,
                selectable: "single row",
                autoBind: false,
            }

            $scope.buscarCuentasBasilea = function () {
                if (obj.seleccionado.Producto && obj.seleccionado.Producto !== " TODOS") {
                    obj.filter = [{ field: "Producto", operator: "eq", value: obj.seleccionado.Producto }];
                } else {
                    obj.filter = [];
                }

                dsCuentasBasilea.filter(obj.filter);
            };

            $scope.limpiarFiltro = function () {
                obj.filter = [];
                obj.idActual = " TODOS";
                obj.seleccionado = false;
                $scope.buscarCuentasBasilea();
            };

            $scope.EditarCuenta = function (data) {
                var parametros = {};
                parametros.refrescar = $scope.buscarCuentasBasilea;
                parametros.Concepto = data.Concepto;
                parametros.TipoCuentaContable = data.TipoCuentaContable;
                parametros.CuentaContable = data.CuentaContable;
                parametros.NombreCuentaContable = data.NombreCuentaContable;
                bcsComunes.ventanaAbrir("EditarCuentascontablesBasilea", parametros, false, false, idVentana);
            };

            $scope.cancelar = function () {
                bcsComunes.ventanaCerrar(idVentana);
            }

        });
}());