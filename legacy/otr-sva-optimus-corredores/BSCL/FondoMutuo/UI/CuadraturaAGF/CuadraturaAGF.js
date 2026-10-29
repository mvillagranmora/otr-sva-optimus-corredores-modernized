angular.module('ModuloFFM').controller('CuadraturaAGFCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos){
	$scope.formData = {};
	$scope.filtros = {};
	var idVentana=bcsComunes.ventanaWinId($scope);
	var conf = bcsConfiguracionFFM.getCuadraturaAGF();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscar = conf.buscar;
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdEmisor = 0;
		$scope.formData.setFondo = "-1";
		$scope.filtros.fechaCuadratura = util.dateInicioDia($scope.SesionFechaSistema);
	};
		
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight(idVentana);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.cargarCuadraturaAGF = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FMM',
			p_FechaCuadratura : $scope.filtros.fechaCuadratura ? util.dateSPdate($scope.filtros.fechaCuadratura) : util.dateInicioDia($scope.SesionFechaSistema)
		}
		
		bcsServicioFondosMutuos.CargarCustodiaFondoMutuo(datos).$promise.then(function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_CARGA_REALIZADA"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}else{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_CARGA_ERRONEA"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
			$scope.buscar($scope.apiGridCuadratura,$scope.filtros);
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
   			bcsComunes.ventanaOverlay(idVentana,false);
   		 })
	}
	
	$scope.cargarArchivo = function(archivo){
		if(archivo === 'NACIONAL'){
			bcsComunes.ventanaOverlay(idVentana,true);
			var datosArchivo = {codigoArchivo: 'CARGA_INVENTARIO_FFMM_NACIONAL', titulo: 'Carga Inventario Nacional'};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
			bcsComunes.ventanaPromesa(id1).then(function(result){
				if(!result || !result.fileData){
					 bcsComunes.ventanaOverlay(idVentana,false);
					 return;
				}
				bcsComunes.ventanaOverlay(idVentana,true);
				var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo  : 'FMM',
						p_FechaCuadratura : $scope.filtros.fechaCuadratura ? util.dateSPdate($scope.filtros.fechaCuadratura) : util.dateInicioDia($scope.SesionFechaSistema),
						p_Cuadratura: result.fileData
				}
				bcsServicioFondosMutuos.CargarArchivoCuadraturaNacionalUI(datos).$promise.then(function(respuesta){
					
					if(respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_CARGA_REALIZADA"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
					}else{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_CARGA_ERRONEA"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
					}
					$scope.buscar($scope.apiGridCuadratura,$scope.filtros);
					bcsComunes.ventanaOverlay(idVentana,false);
				},function(respuesta){
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   			bcsComunes.ventanaOverlay(idVentana,false);
		   		 })
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				console.log(result);
			})
		}
		if(archivo === 'INTERNACIONAL'){
			bcsComunes.ventanaOverlay(idVentana,true);
			var datosArchivo = {codigoArchivo: 'CARGA_INVENTARIO_FFMM_INTERNACIONAL', titulo: 'Carga Inventario Internacional'};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
			bcsComunes.ventanaPromesa(id1).then(function(result){
				if(!result || !result.fileData){
					 bcsComunes.ventanaOverlay(idVentana,false);
					 return;
				}
				bcsComunes.ventanaOverlay(idVentana,true);
				var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo  : 'FMM',
						p_FechaCuadratura : $scope.filtros.fechaCuadratura ? util.dateSPdate($scope.filtros.fechaCuadratura) : util.dateInicioDia($scope.SesionFechaSistema),
						p_Cuadratura: result.fileData
				}
				bcsServicioFondosMutuos.CargarArchivoCuadraturaInternacionalUI(datos).$promise.then(function(respuesta){
					
					if(respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_CARGA_REALIZADA"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
					}else{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_CARGA_ERRONEA"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
					}
					$scope.buscar($scope.apiGridCuadratura,$scope.filtros);
					bcsComunes.ventanaOverlay(idVentana,false);
				},function(respuesta){
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   			bcsComunes.ventanaOverlay(idVentana,false);
		   		 })
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				console.log(result);
			})
		}
	}
	
	$scope.limpiarFiltros();
});