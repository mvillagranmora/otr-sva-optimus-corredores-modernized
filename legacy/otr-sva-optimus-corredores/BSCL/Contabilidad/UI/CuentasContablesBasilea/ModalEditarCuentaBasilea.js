(function () {
    angular.module('ModuloContabilidad').controller('ModalEditarCuentaBasileaCtrl',
        function ($scope, $translate, $resource, bcsComunes, $http, bcsServicioContabilidad) {

            var editarCuentaBasilea = this;
            editarCuentaBasilea.idVentana = bcsComunes.ventanaWinId($scope);
            editarCuentaBasilea.parametros = bcsComunes.ventanaParams(editarCuentaBasilea.idVentana);
            $scope.win = {};
            $scope.data = {};
            $scope.selecionadoCuentas = {};

            $scope.data.numeroCuentas = editarCuentaBasilea.parametros.CuentaContable;
            $scope.data.numeroCuenta = editarCuentaBasilea.parametros.CuentaContable;

            $scope.guardar = function () {
                var datos = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'CON',
                    p_Accion: 'GUARDAR',
                    p_Concepto: editarCuentaBasilea.parametros.Concepto,
                    p_TipoCuentaContable: editarCuentaBasilea.parametros.TipoCuentaContable,
                    p_CuentaContable: $scope.selecionadoCuentas.Cuenta,
                    p_NombreCuentaContable: $scope.selecionadoCuentas.NombreCuenta,
                }
                bcsServicioContabilidad.EditarCuentaBasilea(datos).$promise.then(
                    function (result) {
                        if (result) {
                            if (result.P_OUT_ERRORESCONTADOR === 0) {
                                bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_MODIFICACION_INGRESO_EXITO"), conflictos: util.parseServerResponse(result) }, editarCuentaBasilea.idVentana);
                                bcsComunes.ventanaOverlay(editarCuentaBasilea.idVentana, false);
                                editarCuentaBasilea.parametros.refrescar();
                                $scope.cancelar();
                            } else {
                                bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR_EDITAR_CUENTA_CONTABLE_BASILEA"), conflictos: util.parseServerResponse(result) }, editarCuentaBasilea.idVentana);
                            }
                        }
                    }, function (result) {
                        bcsComunes.ventanaOverlay(editarCuentaBasilea.idVentana, false);
                        bcsComunes.msg("error", "Error", $translate.instant('UI_ERROR'));
                    }
                );
            };

            $scope.cancelar = function () {
                bcsComunes.ventanaOverlay(editarCuentaBasilea.idVentana, false);
                bcsComunes.ventanaCerrar(editarCuentaBasilea.idVentana);
            }

            $scope.$watch('data.numeroCuentas', function (newval, oldval) {
                var tipodato = typeof newval;
                if (tipodato === "string") {
                    editarCuentaBasilea.parametros.NombreCuentaContable = $scope.selecionadoCuentas.NombreCuenta;
                }
            });

        });
}());