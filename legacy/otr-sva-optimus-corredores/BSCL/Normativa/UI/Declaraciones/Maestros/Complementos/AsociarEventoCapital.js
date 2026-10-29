angular.module('ModuloNormativa').controller('AsociarEventoCapitalCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	$scope.fechaDesde = null;
	$scope.fechaHasta = null;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultCalificacionesEventoCapital();
	$scope.defaultOptions = conf.options;
	var esquema = $scope.parametros.Esquema;
	var version = $scope.parametros.Version;
	$scope.filtros.buscarInstrumento = $scope.parametros.Nemotecnico;
	console.log($scope.filtros.buscarInstrumento);
	var dataSourceAsociarEventoCapital = conf.options.dataSource;

	$scope.configuracionGrillaAsociarEventoCapital = {
		dataSource : dataSourceAsociarEventoCapital,
		columns : [ 
			{field : 'Nemo',title : 'Nemo',width : '100px'},
			{field : 'SecuenciaEvento',title : 'Secuencia Evento',width : '100px'},
			{field : 'NumeroDividendo',title : 'Número Dividendo',width : '100px'},  
			{field : 'Descripcion',title : 'Descripción',width : '100px'},
			{field : 'FechaPago',title : 'Fecha Pago',width : '100px', filterable:{cell:{enabled:false}}}
			],
		allowCopy : false,
		autoBind : false,
		userSettings : true,
		excel : {
			allPages : true
		},
		filterable : {mode: "row"},
		height : 260,
		pageable : true,
		selectable: "row",
		reorderable : true,
		resizable : true,
		scrollable : true,
		sortable : true
	};



	$scope.buscarCalificaciones = function(apiGridEventoCapital,filtros) {
		//conf.buscar(apiGridEventoCapital, filtros);
		console.log(filtros);
		var year = version.substring(0,4);
		var month= 0;
		var startDate = new Date(year, month, 1);
		var endDate = new Date(year, month + 11, 31);
		$scope.fechaDesde = util.dateSPdate(startDate);
		$scope.fechaHasta = util.dateSPdate(endDate);
		var filters = new Array();
		if ($scope.fechaDesde != null) {
			filters.push({
				field : "FechaPago",
				operator : 'gte',
				value : $scope.fechaDesde
			});
		}
		if ($scope.fechaHasta != null) {
			filters.push({
				field : "FechaPago",
				operator : 'lte',
				value : $scope.fechaHasta
			});
		}
		filters.push({ field: "IdInstrumento", operator: "eq", value: $scope.filtros.instrumento.IdInstrumento });
		console.log(filters);
		$scope.apiGridEventoCapital.dataSource.filter(filters);
		
  	};

	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
  	
  	$scope.asociarEventoCapital = function(datos){
  		
  		if (datos == undefined){
  			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_EVENTO_ASOCIAR"));
  			return;
  		}
  		console.log(datos);
		console.log(datos[0]);
        var t = bcsAlerta.crear('PREGUNTA_SI_NO','�Seguro(a) desea asociar los datos seleccionados al Evento de Capital indicado?  Secuencia Evento: ' + $scope.parametros.SecuenciaEvento  + ' por: ' + datos.SecuenciaEvento + ' Fecha Pago: ' + util.dateSPdate($scope.parametros.FechaPago) + ' por: ' + util.dateSPdate(datos.FechaPago) + ' Nemotecnico: ' + $scope.parametros.Nemotecnico + ' por: ' + datos.Nemo);		
		 
  		t.result.then(function(result) {		

  		if (result === OPCIONES_BCSALERTA.SI) {
  			if(!datos || datos.length == 0) return;  			
			bcsComunes.ventanaOverlay($scope.winID,true);
			var parametrosSP = {};
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo = 'NOR';
	        parametrosSP.p_SimularOperacion = 'N';
	        parametrosSP.p_UsaReglasNegocio = 'S';
	        parametrosSP.p_CalificacionTributaria = [];
	   	    console.log(datos);
			parametrosSP.p_CalificacionTributaria.push({
	        		
				IdInterno: $scope.parametros.SecuenciaEvento,
				SecuenciaEventoDj:$scope.parametros.SecuenciaEvento,																										
				FechaPagoDj: $scope.parametros.FechaPago,
				NemoDj: $scope.parametros.Nemotecnico,
				SecuenciaEventoCalificacion: datos.SecuenciaEvento,																										
				FechaPagoCalificacion: datos.FechaPago,
				NemoCalificacion: datos.Nemo
			});
	        //$.each(datos,function(key,sol){
	        //	console.log(sol);
	  		//});
        
	        if (esquema === 'DJ1885'){
	    	bcsServiciosNormativa.AsociarEventoCapital(parametrosSP).$promise.then(function(respuesta){
	    		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     		conf.options.dataSource.read();
	   	     	}else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
		    },function(respuesta){
			  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		    });
	        }
	        
	        if (esquema === 'DJ1949'){
	        	bcsServiciosNormativa.AsociarEventoCapital1949(parametrosSP).$promise.then(function(respuesta){
	        		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	       	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	    	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	     		conf.options.dataSource.read();
	       	     	}else{
	    	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	     	 }
	    	    },function(respuesta){
	    		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	    });
	        }
	
	        if (esquema === 'DJ1922'){
	        	bcsServiciosNormativa.AsociarEventoCapital1922(parametrosSP).$promise.then(function(respuesta){
	        		$scope.defaultOptions.dataSource.data(respuesta.P_OUT_RESULTADO); 
	       	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
	    	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	     		conf.options.dataSource.read();
	       	     	}else{
	    	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	     	 }
	    	    },function(respuesta){
	    		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ASOCIAR_EVENTO_CAPITAL"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	    	    });
	        }  
		}
	});
     	
	}  	
  	
});