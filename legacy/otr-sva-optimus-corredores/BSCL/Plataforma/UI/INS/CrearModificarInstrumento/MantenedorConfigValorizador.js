angular.module('ModuloPlataforma').controller(
        'MantenedorConfigValorizadorCtrl',
        [ '$resource', '$scope', '$translate', 'bcsComunes', 'bcsAlerta',
                'OPCIONES_BCSALERTA', MantenedorConfigValorizadorCtrl ]);

function MantenedorConfigValorizadorCtrl($resource, $scope, $translate, bcsComunes, bcsAlerta,
         OPCIONES_BCSALERTA) {

    var windowParams = getWindowParams($scope);
    $scope.config = angular.copy(windowParams.configSeleccionada);
    $scope.Flag = $scope.config.Valor === 'S';

    $scope.guardar = function() {
        $scope.config.IdUsuario = $scope.SesionIdUsuario;

        if ($scope.config.Tipo === 'OPCION') {
            $scope.config.Valor = $scope.Flag ? 'S' : 'N';
        }

        grabarConfig($scope.config, function(reply) {
            if (windowParams.callBackFunction)
                windowParams.callBackFunction();
            bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
        }, function(error) {
            showError(error, 'Modificación de Configuración', bcsComunes
                    .ventanaWinId($scope));
        });
    };

    function grabarConfig(config, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = $scope.SesionIdEmpresa;
        params.p_IdUsuario = config.IdUsuario;
        params.p_IdModulo = 'VAL';
        params.p_IdConfig = config.IdConfig;
        params.p_Descripcion = config.Descripcion;
        params.p_Nombre = config.Nombre;
        params.p_Tipo = config.Tipo;
        params.p_Valor = config.Valor;

        callServiceById(4142, params, fnSuccess, fnError);
    }

    function callServiceById(id, params, success, error) {
        var url = getUrlServicioById(id, {});
        var service = $resource('', {}, { call: { method: 'POST', url: url } });
        var post = service.call(params);
        post.$promise.then(success).catch(error);
    }

    function getUrlServicioById(id, args) {
        var url = '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + id;
        if (args != null) {
            var str = '';
            for (var key in args) {
                str += '&' + key + '=' + encodeURIComponent(args[key]);
            }
            url += str;
        }

        return url;
    }

    function getWindowParams($scope) {
        var id = bcsComunes.ventanaWinId($scope);
        return bcsComunes.ventanaParams(id);
    }

    function showError(error, message, winid) {
        var msg = 'Error';
        if (typeof error === 'string') {
            msg = error;
        } else if (typeof error.message !== 'undefined') {
            msg = error.message;
        } else if (typeof error.statusText !== 'undefined') {
            msg = error.statusText;
        }

        bcsComunes.ventanaMsg({
            mensaje : message,
            conflictos : {
                advertencias : [],
                autorizaciones : [],
                errores : [ {
                    Codigo : 'COD',
                    Descripcion : msg
                } ],
                erroresFatales : [],
                informaciones : []
            }
        }, winid);
    }

}
