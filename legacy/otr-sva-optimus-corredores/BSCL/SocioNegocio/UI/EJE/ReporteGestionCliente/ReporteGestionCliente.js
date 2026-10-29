(function() {
    angular.module('ModuloMesa').controller('ReporteGestionCliente', function($scope, $translate, $resource, $timeout, $http, bcsConsultasMesa, bcsServiciosMesa, bcsComunes, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA, bcsReporteGestionClienteGrid) {
        var idVentana = bcsComunes.ventanaWinId($scope);
        $scope.data = {};
        var fechaMinima = util.dateTerminoDia($scope.SesionFechaSistema);
        $scope.seleccionReporteGestionCliente = {};
        $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
        $scope.data.kngFechaOperacionConf = {
            max: fechaMinima //min: iODP.filtro.Fecha
        };
        $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
        $scope.data.IdUsuario = $scope.SesionIdUsuario;

        $scope.confBasicaReporteGestionCliente = {
            dataSource: bcsReporteGestionClienteGrid.dataSourceReporteGestionCliente,
            columns: bcsReporteGestionClienteGrid.columnasReporteGestionCliente(),
            sortable: true,
            pageable: true,
            filterable: true,
            userSettings: true,
            excel: {
                allPages: true
            },
            height: 450,
            selectable: "multiple row",
            navigatable: true,
            allowCopy: true,
            groupable: true
        };

        $scope.parametrosReporteGestionCliente = function(parametros) {

            var objParams = {};
            objParams.p_IdEmpresa = $scope.SesionIdEmpresa;
            objParams.p_IdUsuario = $scope.SesionIdUsuario;
            objParams.p_IdInstrumento = parametros.instrumentoDatos.IdInstrumento === null ? '' : parametros.instrumentoDatos.IdInstrumento;
            objParams.p_FechaConsulta = util.dateSPdate($scope.data.kngFechaOperacion);

            return objParams;
        }

        $scope.buscar = function() {
            bcsReporteGestionClienteGrid.dataSourceReporteGestionCliente.setCallParameters($scope.parametrosReporteGestionCliente($scope.data));
        };

        $scope.limpiarFiltros = function() {
            $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
            $scope.seleccionReporteGestionCliente = {};
            $scope.gridApiReporteGestionCliente.dataSource.removeAll();
        };
    });
}());
