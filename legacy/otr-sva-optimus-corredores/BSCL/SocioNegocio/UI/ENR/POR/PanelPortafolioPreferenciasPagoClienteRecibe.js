(function () {
    angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPortafolioPreferenciasPagoClienteRecibeEnrolCtrl', function ($scope) {
        const pepppcr = this;

        $scope.admSoc.socioNegocioPromise.then(function () {
            if ($scope.admSoc && $scope.admSoc.dataSocioNegocio) {
                pepppcr.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
                pepppcr.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['PREFERENCIAS_PAGO'];
            }
        });
    });
}());
