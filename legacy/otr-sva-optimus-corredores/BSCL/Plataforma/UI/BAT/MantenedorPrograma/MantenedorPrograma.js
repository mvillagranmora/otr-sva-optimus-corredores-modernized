angular.module('ModuloPlataforma').controller('MantenedorTareasCtrl',function($scope,bcsMantenedorTareasGrid,bcsComunes,$translate,bcsAlerta,$http){

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	console.log("PARAMETROS",parametros);
	let idPrograma = parametros.IdPrograma ? parametros.IdPrograma : 0;
	
	$scope.conf = {};
		
	$scope.conf.oConfiguracionTipoProcesos={
			idActual : false,
			seleccionado : false
	};

	$scope.empresa = { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa };
	$scope.usuario = { field: "IdUsuario", operator: "eq", value: $scope.SesionIdUsuario };
	$scope.esEmpleado = { field: "TipoEntidad", operator: "eq", value: "EMP" };
	$scope.construirFiltros = function(){
		var filtroComplejo = [$scope.empresa, $scope.usuario, $scope.esEmpleado];
		var filtroBasico = [$scope.empresa, $scope.esEmpleado];

		return BCS_ACCESOS["ADMIN"] ? filtroBasico : filtroComplejo ;
	}

	$scope.conf.oConfiguracionUsuario ={
			idActual : false,
			seleccionado : false,
			idDefecto : $scope.SesionIdUsuario,
			filtros : $scope.construirFiltros()
	};

	

	$scope.conf.oConfiguracionTipoSalida ={
			idActual : false,
			seleccionado : false,
			idDefecto : parametros.IdTipoSalida
	}
	
	let gridTareasProgramadas;
	
	$scope.tareasprogramdas = {
		dataSource			: bcsMantenedorTareasGrid.dataSourceResumenOperacion(),
		columns				: bcsMantenedorTareasGrid.columnasDetalleOperaciones(),		
		sortable			: true,
		filterable			: true,
		pageable			: true,
		autoBind			: false,
		height				: 450,
		selectable 			: "row",
		dataBound: function () {
            if (gridTareasProgramadas && idPrograma !== 0) {
                $.each(gridTareasProgramadas.dataSource.view(), function (keyTran, data) {
                    if (data.IdPrograma === idPrograma) {
                        gridTareasProgramadas.select("tr:eq(" + keyTran + ")");
                        return false;
                    }
                });
                idPrograma = 0;
            }
        },
		gridCreated : function(grid) {
			gridTareasProgramadas = grid;
			if(idPrograma !== 0){
				$scope.BuscarTareas();
			}
		}
	};	
	
	$scope.BuscarTareas = function(){

		var filters = new Array();

		if ($scope.conf.oConfiguracionTipoProcesos.seleccionado.IdTipoPrograma)
			filters.push({ field: "IdTipoPrograma", operator: "eq", value: $scope.conf.oConfiguracionTipoProcesos.seleccionado.IdTipoPrograma });
		
		if ($scope.conf.IdEstadoPrograma)
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.conf.IdEstadoPrograma });

		if ($scope.conf.oConfiguracionTipoSalida.idActual)
			filters.push({field:"TipoSalida", operator: "eq" , value:$scope.conf.oConfiguracionTipoSalida.idActual});

		if ($scope.conf.oConfiguracionUsuario.idActual){
			filters.push({field:"IdUsuario", operator: "eq" , value:$scope.conf.oConfiguracionUsuario.idActual});
		}else{
			//es el filtro todos o vacio, traer solo los del usuariosi no es ADMIN
			if(!BCS_ACCESOS["ADMIN"]){
				filters.push($scope.usuario);
			}
		}
		
		if(idPrograma !== 0){
			filters.push({ field: "IdPrograma", operator: "eq", value: idPrograma });
		}else{
			if ($scope.conf.kngFechaDesde){
				var resp = util.validarRangoFecha($scope.conf.kngFechaDesde, $scope.conf.kngFechaHasta);
				if(resp.estado){
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
				filters.push({ field: "fechaCreacion", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.conf.kngFechaDesde)) });
			}
			
			if ($scope.conf.kngFechaHasta)
				filters.push({ field: "fechaCreacion", operator: "lte", value: util.dateOdata(util.dateTerminoDia($scope.conf.kngFechaHasta)) });
		}

		gridTareasProgramadas.dataSource.filter(filters);
	};
	 
	$scope.limpiarFiltros = function ()
	{
		$scope.conf.oConfiguracionTipoProcesos.idActual = 0;
		$scope.conf.IdEstadoPrograma = 0;
		$scope.conf.kngFechaDesde =  util.dateInicioDia(new Date());
		$scope.conf.kngFechaHasta = util.dateTerminoDia(new Date()); 
		$scope.conf.oConfiguracionUsuario.idActual= $scope.SesionIdUsuario;
		$scope.conf.oConfiguracionTipoSalida.idActual= 0;
	}
 	
	$scope.limpiarFiltros();
	
	$scope.DescargarReporte = function (dato){
		var datos = {file:dato.RutaSalida}
		bcsComunes.ventanaOverlay(idVentana,true);
		var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
		$http({

			method: 'POST',
			url: url,
			params: datos,
			responseType:'arraybuffer'
			
		}).success(function(datos){
				var file = new File([datos],dato.RutaSalida.split('/')[dato.RutaSalida.split('/').length - 1]);
				saveAs(file);
				
				bcsComunes.msg("info",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant("UI_DESCARGA_REPORTE_EXITOSA"));
				bcsComunes.ventanaOverlay(idVentana,false);
				
		})
		.error(function (err){
				bcsComunes.msg("error",$translate.instant("UI_DESCARGA_REPORTE"),$translate.instant('UI_DESCARGA_REPORTE_ERROR'));
				bcsComunes.ventanaOverlay(idVentana,false);
		});

	}
	
	$scope.DescargarLog = function (dato){
		var datos = {file:dato.RutaLog}
		bcsComunes.ventanaOverlay(idVentana,true);
		var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
		
		$http({

			method: 'POST',
			url: url,
			params: datos
			
		}).success(function(datos){
				var file = new File([datos],dato.RutaLog.split('/')[dato.RutaLog.split('/').length - 1]);
				saveAs(file);
				bcsComunes.msg("info",$translate.instant("UI_DESCARGA_LOG"),$translate.instant("UI_DESCARGA_LOG_EXITOSA"));
				bcsComunes.ventanaOverlay(idVentana,false);
				
		})
		.error(function (err){
				bcsComunes.msg("error",$translate.instant("UI_DESCARGA_LOG"),$translate.instant('UI_DESCARGA_LOG_ERROR'));
				bcsComunes.ventanaOverlay(idVentana,false);
		});

	}
	
	$scope.$on('eventoVentanaEstaAbierta', function(event, parametroRecibido) {
	    	// Manejar el parametro recibido
	    	idPrograma = parametroRecibido;
	    	$scope.BuscarTareas();
  	});
  	
});
