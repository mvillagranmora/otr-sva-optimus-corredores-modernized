angular.module('ModuloTesoreria').controller('BlotterComprobantesCtrl', function ($scope, bcsComunes, $timeout, $compile) {
    var estadoPestanas = {
        'gridMovimientos': { seleccionada: false, contenido: '<div ng-include="\'../../Tesoreria/UI/BlotterComprobantes/ConsultarIngresoEgreso/ConsultarIngresoEgreso.html\'" ></div>' },
        'gridComprobantes': { seleccionada: false, contenido: '<div ng-include="\'../../Tesoreria/UI/BlotterComprobantes/ConsultarComprobantes/ConsultarComprobantes.html\'" ></div>' },
        'gridFormasDePago': { seleccionada: false, contenido: '<div ng-include="\'../../Tesoreria/UI/BlotterComprobantes/ConsultarDocumentos/ConsultarDocumentos.html\'" ></div>' },
        'gridRecepcionDocumentos': { seleccionada: false, contenido: '<div ng-include="\'../../Tesoreria/UI/BlotterComprobantes/ConsultarEnvioDocumentos/ConsultarEnvioDocumentos.html\'" ></div>' }
    }; 

    var idVentana = bcsComunes.ventanaWinId($scope);
    var domVentana = $("[ventanaid='" + idVentana + "']");
    bcsComunes.ventanaAutoHeight(idVentana);

    $scope.select = function (grid) {
        var estadoPestana = estadoPestanas[grid];

        if (!estadoPestana.seleccionada) {
            domVentana.find('#' + grid).append($compile(estadoPestana.contenido)($scope));
            estadoPestana.seleccionada = true;
        }
    };

    $timeout(function () {
        if ($("#gridMovimientos").find("[kendo-grid]").data("kendo-grid"))
            $("#gridMovimientos").find("[kendo-grid]").data("kendo-grid").refresh()
    }, 50);
});