angular.module('ModuloFFM').controller('BlotterCargaOperacionFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaCargaOperacionesFondosMutuos();
	var apiGrid = conf.options.dataSource;
	$scope.filtros = {};
	$scope.buscarOperacionesCarga = conf.buscar;
	$scope.defaultOptions = conf.options;
	$scope.buscarSolicitudes = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.oConfiguracionTipoOperacion = {
		filtros : {
			TieneCarga : { "field": "TieneCarga","operator": "eq", "value"   :  'S' }
		}
	}
	
  	$scope.cargaOperacionesFFM = function()
	{
		 bcsComunes.ventanaOverlay($scope.winID,true);
  		 var datosArchivo = {codigoArchivo: 'ARCHIVO_CARGA_SOLICITUDES_FFMM', formatoArchivoDefecto: '.txt'};
		 var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
			
		 var promesa=bcsComunes.ventanaPromesa(idVentanaCargaArchivo);                               
		 promesa.then(function(data){
			 console.log(data);
			 if(!data || !data.fileData){
				 bcsComunes.ventanaOverlay($scope.winID,false);
				 return;
			 }
			 
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdModulo = 'FMM';
			parametrosSP.p_UsaReglasNegocio = 'N';
			parametrosSP.p_Solicitudes = data.fileData;
			
			if(data.fileData.length) {

				for (const operacionFfmm of data.fileData) {

					if ( operacionFfmm.IDMATRIZGRAUPADORAPV && operacionFfmm.IDMATRIZGRAUPADORAPV > 0 ) {
						operacionFfmm.IDPLAN = 0;
						operacionFfmm.IDPRODUCTO = 0;
						operacionFfmm.IDREGIMENPORTAFOLIO = 0;
					} else {
						operacionFfmm.IDMATRIZGRAUPADORAPV = 0;
					}

					if ( operacionFfmm.TIPOMOVIMIENTO !== 'RESNORM' ) 
						operacionFfmm.IDMEDIODEPAGO = "";
					
				}

				bcsServicioFondosMutuos.CargarSolicitudesAporteRescate(parametrosSP).$promise.then(function(respuesta){
				$scope.buscarSolicitudes($scope.apiGrid,$scope.filtros); 
				if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESAR_CARGAR_SOLICITUDES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_SOLICITUDES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
				}
				},function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CARGAR_SOLICITUDES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
				});
			}
		  });
	};
	
	$scope.resumenCargaOperacionesFFM = function (items) {
		var id = bcsComunes.ventanaAbrir("ResumenOperacionesFondosMutuos", {}, false, false, $scope.winID );
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
	    	$scope.buscarOperacionesCarga($scope.apiGrid,{});
	    });
		
  	};
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdTipoMovimientoCarga = 0;
		$scope.filtros.IdEmisor = 0;
		$scope.formData.setFondo = "-1";
	};
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.limpiarFiltros();
	
});