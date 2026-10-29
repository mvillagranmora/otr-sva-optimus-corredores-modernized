angular.module('ModuloNormativa').controller('CargaArchivoMLTCtrl',function($scope,$translate,bcsComunes,bcsServiciosNormativa,bcsConfGrillaNormativa,bcsAlerta,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.datosCarga = []

	
	$scope.habilitaBotonProcesar = false;
		
	var conf = bcsConfGrillaNormativa.getMLT(); 
	$scope.defaultOptions = conf.options;

	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.defaultOptions.gridCreated = function(grid){
		bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	}
	
	var cargarArchivoMLT = function(simular){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_UsaReglasNegocio = 'N';
        parametrosSP.p_SimularOperacion = simular;
        parametrosSP.p_MLT = $scope.datosCarga;
   	    
        if($scope.datosCarga.length)
    	{ 
        	
        	bcsServiciosNormativa.CargarMLT(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	   	     		if (simular !='S')
	   	     		{
	   	     			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESADO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);		
	   	     		}
		     		
		     		$scope.habilitaBotonProcesar = simular === 'S' ? true : false;
		     		bcsComunes.ventanaOverlay($scope.winID,false);
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_MLT"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		bcsComunes.ventanaOverlay($scope.winID,false);
	   	     	}
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESAR_MLT"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			  bcsComunes.ventanaOverlay($scope.winID,false);
		    });
        	
       	}
	}
	
	$scope.cargarArchivo = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosArchivo = {codigoArchivo: 'ARCHIVO_CARGA_MLT', formatoArchivoDefecto: '.txt'};
		 var idVentanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
		 var promesa=bcsComunes.ventanaPromesa(idVentanaCargaArchivo);                               
		 promesa.then(function(data){
			 if(!data || !data.fileData){
				 bcsComunes.ventanaOverlay($scope.winID,false);
				 return;
			 }
			 $scope.datosCarga = data.fileData;
			 cargarArchivoMLT('S');
		 });
	}
	
	$scope.procesarArchivo = function(){
		 cargarArchivoMLT('N');
	}
	
})