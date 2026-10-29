angular.module('ModuloNormativa').controller('QuadControllerRMO',function QuadControllerRMO($scope, $translate, bcsComunes, Model, Utils) {
	
	const windowParams = Utils.getWindowParams($scope);
	const datos = windowParams.datos;
    const idVentana = bcsComunes.ventanaWinId($scope);
          
    Utils.setWindowTitle($scope, $translate.instant('UI_VALIDAR') + ' RMO');

    $scope.fechaDesde = new Date(windowParams.fechaDesde);
    $scope.fechaHasta = new Date(windowParams.fechaHasta);
    
    $scope.botonValidarHabilitado = true;
    
    $scope.validarRMO = function() {
    	
    	$scope.botonValidarHabilitado = false;
        
        try {
           submitirValidacion(); 
        } catch (err) {
            bcsComunes.msg('error', 'Validación de Datos', err);
        }
        
        $scope.botonValidarHabilitado = true;
    }

    function submitirValidacion() {
        let params = {
            p_IdEmpresa : $scope.SesionIdEmpresa,
            p_IdUsuario : $scope.SesionIdUsuario,
            p_IdModulo : datos.modulo,

            p_IdAgenda : 0,
            p_IdTipoTarea : 'ValidacionDJ',
            p_Segundos : 0,
            p_CreaParam : 'S',

            p_TablaParametros : [ {
                NombreParametro : 'idDocumento',
                Valor : datos.idDocumento
            }, {
                NombreParametro : 'idUsuario',
                Valor : $scope.SesionIdUsuario
            }, {
                NombreParametro : 'schema',
                Valor : datos.esquemaDatos
            } ]
        };

        let fechaDesde = Utils.formatDate($scope.fechaDesde);
        if (fechaDesde != null) {
            params.p_TablaParametros.push({
                NombreParametro : 'fechaDesde',
                Valor : fechaDesde
            });
        }

        let fechaHasta = Utils.formatDate($scope.fechaHasta);
        if (fechaHasta != null) {
            params.p_TablaParametros.push({
                NombreParametro : 'fechaHasta',
                Valor : fechaHasta
            });
        }

        bcsComunes.msg('info', 'Validacion', 'En proceso...');
        Model.agregarPrograma(params, function(reply) {
            bcsComunes.msg('info', 'Agregar Programa', 'OK');
            bcsComunes.ventanaCerrar(idVentana);
        }, function(error) {
            bcsComunes.msg('error', 'Agregar Programa', error.message);
        });
    }
});
