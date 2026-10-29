angular.module('ModuloPlataforma').controller('ProcesosSistemaCtrl',
		function($scope,bcsComunes,$translate,bcsCierreModulosGrid, $resource){
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//Obtiene datos de la empresa
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1557",
	    				dataType: "json"
			    	}
				},
				filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			});
	
	        empresaDatasource.fetch().then(function(){
				 if (empresaDatasource.data().length != 1 ) 
				 {
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });		
	};
	
	//GRILLA
	var columnas = [];
	columnas.push({field: 'TipoRequisito', title:$translate.instant('UI_REQUISITO'), width: "40px" });
	columnas.push({field: 'Error', title:$translate.instant('UI_ERROR'), width: "50px" });
	
	
	var dSResultados = function(){
		return new bcs.DataSource({
			type: "array",
			definition:{
					data:[],
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					serverPaging 		: true, // Paginado de los datos en el servidor.
					serverSorting 		: true, // Ordenamiento de los datos en el servidor.
					sortable 			: true,
					pageSize 			: 50,
					schema : {
						model: {
							fields: {
								TipoRequisito	: { type: "string"},
								Error			: { type: "string"},
							}
						}
					}
				}
		})
	};
	
	$scope.configResultados = {
			dataSource: dSResultados(),
			columns: columnas,
			sortable: true,
			pageable: true,
			height: 380,
			//autoBind : true
		};
	
	
	
	
	$scope.buscar = function(){
		var filters = new Array();
		if ($scope.fechaConsulta !== null){
			filters.push({ field: "FechaProceso", operator: "gte", value: util.date($scope.fechaConsulta).hour(0).minute(0).second(0).toDate()});
			filters.push({ field: "FechaProceso", operator: "lte", value: util.date($scope.fechaConsulta).hour(23).minute(59).second(59).toDate()});
		}
		console.log("FILTROS: " + JSON.stringify(filters));
		$scope.grillas.gridLogProcesos.dataSource.filter(filters);
	};
	
	var servicioAperturaModulo= function(v_Dato){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=234";
		var r = $resource
		("", {}, {
					'urlConsulta': { method: "POST", url: urlConsulta}
				 }
		);
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	}
	
	$scope.entregaSistema = function() {
		$scope.gridApiResultados.dataSource.data([]);	
		var parametrosSP = {};
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'PLA';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'N';
		parametrosSP.p_TipoProgramaAdelantar	= 'CAMBIO_FECHA_SISTEMA';//nuevo!
		parametrosSP.p_SegundosDelayAdelantar		= 30;
						
		servicioEntregaSistema(parametrosSP).then(
			function(result) {
				var msg = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ADVERTENCIA');
				}else if (result.P_OUT_ERRORESCONTADOR) {
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR');
				}else {
					$scope.gridApiResultados.dataSource.data([]);
					for(var key in result.p_out_Validaciones){
						$scope.gridApiResultados.dataSource.add(result.p_out_Validaciones[key]);
					}
				}
				if(result.p_out_TipoProgramaAdelantado=='S'){
					$scope.gridApiResultados.dataSource.add({TipoRequisito:'PAUTA ADELANTADA: CAMBIO_FECHA_SISTEMA',Error:'OK'});
				}
				if(msg!="")
					bcsComunes.ventanaMsg({
						mensaje: msg,
						conflictos:util.parseServerResponse(result)
					},idVentana);
				
			},
			function(result){
				bcsComunes.msg("error", "Error", "La operacion tiene errores");
			});
	};
	
	var servicioEntregaSistema= function(v_Dato){
		//var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1726";
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20015";//java, incluye adelantar cambio fecha.
		var r = $resource("", {}, {'urlConsulta': { method: "POST", url: urlConsulta}});
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	};
	
	$scope.aperturaSistema = function() {
		
		var parametrosSP = {};
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'PLA';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'N';
						
		servicioAperturaSistema(parametrosSP).then(
			function(result) {
				var msg = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ADVERTENCIA');
				}else if (result.P_OUT_ERRORESCONTADOR) {
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR');
				}else {
					$scope.gridApiResultados.dataSource.data([]);
					for(var key in result.P_OUT_VALIDACIONES){
						$scope.gridApiResultados.dataSource.add(result.P_OUT_VALIDACIONES[key]);
					}
				}
				
				if(msg!="")
					bcsComunes.ventanaMsg({
						mensaje: msg,
						conflictos:util.parseServerResponse(result)
					},idVentana);
				
			},
			function(result){
				bcsComunes.msg("error", "Error", "La operacion tiene errores");
			});
	};
	
	var servicioAperturaSistema= function(v_Dato){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1730";
		var r = $resource
		("", {}, {
					'urlConsulta': { method: "POST", url: urlConsulta}
				 }
		);
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	}
	
});