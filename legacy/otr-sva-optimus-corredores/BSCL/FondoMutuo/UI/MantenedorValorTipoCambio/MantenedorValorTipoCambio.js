angular.module('ModuloFFM').controller('MantenedorValorTipoCambioCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.datosCarga = [];
	$scope.habilitaBotonProcesar = false;
	var conf = bcsConfiguracionFFM.getConsultaValorTipoCambio(); 
	$scope.defaultOptions = conf.options;

	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.historicoTipoCambioFMM = function(){
		bcsComunes.ventanaAbrir("ConsultaValorTipoCambioHistorico",{});
	}
	
	// $scope.defaultOptions.gridCreated = function(grid){
	// 	bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	// }
	bcsComunes.ventanaAutoHeight($scope.winID);
	var cargarTipoCambio = function(simular){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'FMM';
        parametrosSP.p_UsaReglasNegocio = 'N';
        parametrosSP.p_SimularOperacion = simular;
        parametrosSP.p_TipoCambio = $scope.datosCarga;
   	    
        if($scope.datosCarga.length)
        { 
    	   bcsServicioFondosMutuos.CargarValorTipoCambio(parametrosSP).$promise.then(function(respuesta){
    		  $scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
   	     	  if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0)
   	     	  {
	     		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESAR_TIPO_CAMBIO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		  $scope.habilitaBotonProcesar = simular === 'S' ? true : false;
	     		 bcsComunes.ventanaOverlay($scope.winID,false);
   	     	  }else
   	     	  {
	     		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_TIPO_CAMBIO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		 bcsComunes.ventanaOverlay($scope.winID,false);
   	     	  }
		   },function(respuesta){
			 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_TIPO_CAMBIO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			 bcsComunes.ventanaOverlay($scope.winID,false);
		   });
       	 }
	}
	
	$scope.cargarArchivo = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosArchivo = {codigoArchivo: 'ARCHIVO_VALOR_TIPO_CAMBIO', formatoArchivoDefecto: '.txt'};
		var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
			
		var promesa=bcsComunes.ventanaPromesa(idVentanaCargaArchivo);                               
		promesa.then(function(data){
		  if(!data || !data.fileData){
			  bcsComunes.ventanaOverlay($scope.winID,false);
			  return;
		  }
		  $scope.datosCarga = data.fileData;
		  cargarTipoCambio('S');
	    });
	}
	
	$scope.procesarTipoCambioFFMM = function(){
		cargarTipoCambio('N');
	}
})