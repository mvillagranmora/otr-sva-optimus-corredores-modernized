(function() {
    angular.module('ModuloMesa').controller('InformeOrdenesConfirmadas', function($rootScope,$scope, $translate, $resource, $timeout, $http, bcsComunes, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA, bcsInformeOrdenesConfirmadasGrid) {
        var idVentana = bcsComunes.ventanaWinId($scope);
        $scope.data = {};
        var fechaMinima = util.dateTerminoDia($scope.SesionFechaSistema);
        $scope.seleccionInformeOrdenesConfirmadas = {};
        $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
        $scope.data.kngFechaOperacionConf = {
           // max: fechaMinima //min: iODP.filtro.Fecha
        };
        $scope.data.EstadoConfirmacion = {
        		filtros: {
        			NombreEntidad : { "field": "NombreEntidad","operator": "eq", "value"   :  "ORDEN.CONFIRMACION" },
        		},
        	};
        $scope.data.filtrosCondicionDeLiquidacion = {
            AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": ""}    
        };
    
        $scope.data.filtrosMonedaNegociacion = {
            IdMoneda : {"field": "IdMoneda","operator": "eq","value": ""}    
        };
        $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
        $scope.data.IdUsuario = $scope.SesionIdUsuario;

        $scope.confBasicaInformeOrdenesConfirmadas = {
            dataSource: bcsInformeOrdenesConfirmadasGrid.dataSourceInformeOrdenesConfirmadas,
            columns: bcsInformeOrdenesConfirmadasGrid.columnasInformeOrdenesConfirmadas(),
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

        $scope.parametrosInformeOrdenesConfirmadas = function(parametros) {

            var objParams = {};
            objParams.p_IdEmpresa = $scope.SesionIdEmpresa;
            objParams.p_IdUsuario = $scope.SesionIdUsuario;
            objParams.p_FechaConsulta = util.dateSPdate($scope.data.kngFechaOperacion);
            objParams.p_EstadoConfirmacion = $scope.data.EstadoConfirmacionSeleccionado.IdEstado?$scope.data.EstadoConfirmacionSeleccionado.IdEstado:"";
            objParams.p_IdTipoInstrumentoSeleccionado= $scope.data.IdTipoInstrumentoSeleccionado.IdTipoInstrumento?$scope.data.IdTipoInstrumentoSeleccionado.IdTipoInstrumento:"";
            objParams.p_Moneda = $scope.filtros.IdMoneda? $scope.filtros.IdMoneda:"";
            objParams.p_CondicionDeLiquidacion = $scope.filtros.IdCondicionDeLiquidacion?$scope.filtros.IdCondicionDeLiquidacion:"";
            return objParams;
        }

        $scope.buscar = function() {
            bcsInformeOrdenesConfirmadasGrid.dataSourceInformeOrdenesConfirmadas.setCallParameters($scope.parametrosInformeOrdenesConfirmadas($scope.data));
        };

        $scope.limpiarFiltros = function() {
            $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
            $scope.seleccionInformeOrdenesConfirmadas = {};
            bcsInformeOrdenesConfirmadasGrid.dataSourceInformeOrdenesConfirmadas.removeAll();
        };
    });
}());
