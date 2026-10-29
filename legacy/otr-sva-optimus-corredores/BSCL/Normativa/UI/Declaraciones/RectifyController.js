angular.module('ModuloNormativa').controller(
        'RectifyController',
        [ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils',
                RectifyController ]);

function RectifyController($scope, $translate, bcsComunes, Model, Utils) {

    var windowParams = Utils.getWindowParams($scope);
    
    if(windowParams.Model){
		Model = windowParams.Model;
	}
    
    $scope.opcRectificacion = 'S';

 	$scope.rectificar = function() {
		var params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : Model.getModulo(),
			p_UsaReglasNegocio	: 'S',
			p_FechaProceso	: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
			p_Rectifica : $scope.opcRectificacion,
			p_List : []
		};
		
		for (var i = 0; i < windowParams.marcados.length; i++) {
			params.p_List.push({
				ID : windowParams.marcados[i]
			});
		}
		
		Model.callServiceById(4290705, params, function() {
			bcsComunes.msg('info', 'Rectificar Registros', 'OK');
			var p = windowParams.gridData.dataSource.page();
			windowParams.buscarDatos();
			windowParams.gridData.dataSource.page(p);
            var winid = bcsComunes.ventanaWinId($scope);
            bcsComunes.ventanaCerrar(winid);
		}, function(error) {
			bcsComunes.msg('error', 'Rectificar Registros', error.message);
		});
    }
}
