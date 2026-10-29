(function() {
	angular.module('ModuloNormativa').controller('PanelNGC368PARTCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig, bcsServicioPlataforma){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var p368PART = this;
		p368PART.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		p368PART.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		p368PART.TipoArchivo = 'pdf';
		p368PART.IdVersionEsquema = '';
	
		p368PART.IdTipoDocumento = 'NGC368PART';
		
		$scope.filtros = {};
		$scope.dataTemplate = {};
		$scope.oFondo = {};
		
		$scope.oFondo.filtros = {
			IdFondo : { field: "IdFondo", operator: "eq", value:undefined },
		};
		
		$scope.filtros.FechaRegistros = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		$scope.p368PART.clear = function(){
		    $scope.filtros.FechaRegistros = util.dateInicioDia($scope.SesionFechaSistema);
		    $scope.filtros.IdFondo = '';
		    $scope.filtros.IdCliente = false;
		};

		p368PART.solicitar = function(){
		    var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'REPORTE_PARTICIPES_GRAVAMENES_FFMM',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'registroDate',
					Valor : util.dateSPdate($scope.filtros.FechaRegistros)
				},{
					IdInterno : 0,
					NombreParametro : 'idFondo',
					Valor : $scope.filtros.IdFondo || ''//$scope.oFondo.filtros.IdFondo.value
				},{
					IdInterno : 0,
					NombreParametro : 'idSerie',
					Valor : ($scope.filtros.IdSerie) ? $scope.filtros.IdSerie : ''
				},{
					IdInterno : 0,
					NombreParametro : 'idCliente',
					Valor : ($scope.filtros.seleccionadoCliente.IdCliente)? $scope.filtros.seleccionadoCliente.IdCliente : 0
				},{
					IdInterno : 0,
					NombreParametro : 'reportType',
					Valor : p368PART.IdTipoDocumento
				}]
			}
		    
		    bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
			    function(result) {
				bcsComunes.ventanaOverlay(p368PART.idVentana,false);
				if(result.P_OUT_ERRORESCONTADOR === 0){
					var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
				} else{
				    bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				}
			    },
			    function(result){
				bcsComunes.ventanaOverlay(p368PART.idVentana,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			    }
		    );
		};
	});
}());