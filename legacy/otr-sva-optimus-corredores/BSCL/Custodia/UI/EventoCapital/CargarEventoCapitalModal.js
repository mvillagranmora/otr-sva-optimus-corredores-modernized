angular.module('ModuloCustodia').controller('CargarEventoCapitalCtrl',
		function ($scope,bcsServicioPlataforma,bcsBlotterEventoCapitalGrid, bcsComunes,$translate,$resource) {

		$scope.data = {};
		$scope.grillaVacia = true;
		
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var gridData;
		$scope.data.kngFechaLimite = util.dateInicioDia($scope.SesionFechaSistema);
		
		var cargaConfirmado = false;
		

	  	$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana, cargaConfirmado);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		
		
		
		$scope.cargarEventosDesdeBursatil = function() {
		    if ($scope.data.kngFechaLimite == undefined || $scope.data.kngFechaLimite == "" || $scope.data.kngFechaLimite == null) {
		        bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("DEBE_ESPECIFICAR_FECHA_LIMITE"));
		        return;
		    }
		
		    var entradaPrograma = {
		        p_IdEmpresa: $scope.SesionIdEmpresa,
		        p_IdUsuario: $scope.SesionIdUsuario,
		        p_IdModulo: 'CUS',
		        p_IdAgenda: 0,
		        p_IdTipoTarea: 'CARGAEVENTOCAPITALDESDEBURSATIL',
		        p_Segundos: 0,
		        p_CreaParam: 'S',
		        p_TablaParametros: [
		            { NombreParametro: 'IdEmpresa', Valor: $scope.SesionIdEmpresa },
		            { NombreParametro: 'IdUsuario', Valor: $scope.SesionIdUsuario },
		            { NombreParametro: 'IdModulo', Valor: 'CUS' },
		            { NombreParametro: 'FechaSistema', Valor: util.dateSPdate($scope.data.kngFechaLimite) }
		        ]
		    };
		
		    console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
		
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
		                    mensaje: $translate.instant("ERROR_AL_CARGAR_EVENTOS_CAPITAL")
		                }, idVentana);
		            }
		        },
		        function(respuesta) {
		            bcsComunes.ventanaMsg({
		                mensaje: $translate.instant("ERROR_AL_CARGAR_EVENTOS_CAPITAL"),
		                conflictos: util.parseServerResponse(respuesta)
		            }, idVentana);
		        }
		    );
		};
		

	
		$scope.confBasicaEventosFaltantes = {
				dataSource			: bcsBlotterEventoCapitalGrid.dataSourceEventosFaltantes(),
				columns				: bcsBlotterEventoCapitalGrid.columnasEventosFaltantes(),				
				sortable			: true,
				userSettings		: true,
				pageable			: true,
				///excel 				: { allPages : true },
				height				: 260,		
				selectable 			: "multiple row",
				filterable			: true

			};
		
		$scope.buscarEventosFaltantes = function() {
		    if ($scope.data.kngFechaLimite == undefined || $scope.data.kngFechaLimite == "" || $scope.data.kngFechaLimite == null) {
		        bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("DEBE_ESPECIFICAR_FECHA_LIMITE"));
		        return;
		    }
		
		    var datos = {
		        p_IdEmpresa: $scope.SesionIdEmpresa,
		        p_IdUsuario: $scope.SesionIdUsuario,
		        p_IdModulo: 'CUS',
		        p_FechaSistema: util.dateSPdate($scope.data.kngFechaLimite)
		    };
		    
			console.log("setCallParameters")
			console.log($scope.gridApi.dataSource.data())
		    // Actualiza la cuadrícula con nuevos datos
			$scope.gridApi.dataSource.setCallParameters(datos).done(() => {
				
                $scope.grillaVacia = $scope.gridApi.dataSource.data().length === 0 ? true : false;
			
			});

		};
		

				
});
