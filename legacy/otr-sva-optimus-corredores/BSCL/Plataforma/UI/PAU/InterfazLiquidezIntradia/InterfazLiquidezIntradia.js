angular.module('ModuloPlataforma')
	.controller('InterfazLiquidezIntradiaCtrl', function ($scope, bcsComunes, $translate, bcsServicioPlataforma) {
		$scope.data = {};
		var idVentana = bcsComunes.ventanaWinId($scope);

		$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);

		var optionEnabled = function () {
			$scope.data.procesarDisabled = true;
			$scope.data.cancelDisabled = true;
		}

		optionEnabled();

		$scope.data.procesar = function () {
			bcsComunes.ventanaOverlay(idVentana, true);
			
			let entradaPrograma = {
		        p_IdEmpresa: $scope.SesionIdEmpresa,
		        p_IdUsuario: $scope.SesionIdUsuario,
		        p_IdModulo: 'CUS',
		        p_IdAgenda: 0,
		        p_IdTipoTarea: 'ENVIAR_INTERFAZ_LIQUIDEZ_INTRADIA',
		        p_Segundos: 0,
		        p_CreaParam: 'S',
		        p_TablaParametros: [
		            { NombreParametro: 'IdEmpresa', Valor: $scope.SesionIdEmpresa },
		            { NombreParametro: 'IdUsuario', Valor: $scope.SesionIdUsuario },
		            { NombreParametro: 'IdModulo', Valor: 'PLA' },
		            { NombreParametro: 'EsManual', Valor: 'S' },
		            { NombreParametro: 'FechaProceso', Valor: util.dateSPdate($scope.data.kngFechaSistema) },
		            { NombreParametro: 'EnviarArchivoACasilla', Valor: $scope.data.EnviarArchivoACasilla ? "S" : "N" }
		        ]
		    };
		    
		    bcsComunes.log().info(JSON.stringify(entradaPrograma));
			
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
		        function(respuesta) {
		            if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
		                Programa = respuesta.P_OUT_IDPROGRAMA;
		                if (Programa.length > 0)
		                    idPrograma = Programa[0].IdPrograma;
		
		                bcsComunes.ventanaMsg({
		                    mensaje: "Programa solicitado correctamente (#" + idPrograma + "). Vea la salida en Consultar Programas"
		                }, idVentana);
		            } else {
		                bcsComunes.ventanaMsg({
		                    mensaje: $translate.instant("UI_ERROR_EJECUCION_INTERFAZ_LIQUIDEZ_INTRADIA")
		                }, idVentana);
		            }
		        },
		        function(respuesta) {
		            bcsComunes.ventanaMsg({
		                mensaje: $translate.instant("UI_ERROR_EJECUCION_INTERFAZ_LIQUIDEZ_INTRADIA"),
		                conflictos: util.parseServerResponse(respuesta)
		            }, idVentana);
		        }
		    );
		};
	});

