angular.module('ModuloPlataforma').controller('ProcesosCierreCtrl',
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
	
	//GRILLA MODULOS
	$scope.confBasicaModulos = {
		dataSource			: bcsCierreModulosGrid.dataSourceModulos(),
		columns				: bcsCierreModulosGrid.columnasModulos(),
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 380,
		selectable			: "row"
	};
	
	//GRILLA LOG PROCESOS
	$scope.confBasicaLogProcesos = {
		dataSource			: bcsCierreModulosGrid.dataSourceLogProcesos(),
		columns				: bcsCierreModulosGrid.columnasLogProcesos(),
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 380,
		selectable			: "row"
	};
	
	$scope.fechaConsulta =  util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.buscar = function(){
		var filters = new Array();
		if ($scope.fechaConsulta !== null){
			filters.push({ field: "FechaProceso", operator: "gte", value: util.date($scope.fechaConsulta).hour(0).minute(0).second(0).toDate()});
			filters.push({ field: "FechaProceso", operator: "lte", value: util.date($scope.fechaConsulta).hour(23).minute(59).second(59).toDate()});
		}
		console.log("FILTROS: " + JSON.stringify(filters));
		$scope.grillas.gridLogProcesos.dataSource.filter(filters);
		
		filters = new Array();
		filters.push( {field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)} );
		$scope.grillas.gridModulos.dataSource.filter(filters);
		
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
	
	$scope.aperturaModulo = function() {
		if($scope.seleccionModulo === undefined){return}
		
		var parametrosSP = {};
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'PLA';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'N';
		
		parametrosSP.p_IdCREModuloEmpresa		= $scope.seleccionModulo.IdCREModuloEmpresa;
		parametrosSP.p_IdEstado					= $scope.seleccionModulo.IdEstado;
		parametrosSP.p_HoraApertura				= ($scope.seleccionModulo.HoraApertura === null)?'':$scope.seleccionModulo.HoraApertura;
		parametrosSP.p_FechaProceso 			= util.date($scope.SesionFechaSistema +" "+$scope.SesionHoraSistema).format("YYYY-MM-DD HH:mm:ss");
		parametrosSP.p_TipoEstado				= $scope.seleccionModulo.TipoEstado;
		
		console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
		
		servicioAperturaModulo(parametrosSP).then(
			function(result) {
				var msg = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ADVERTENCIA');
				}else if (result.P_OUT_ERRORESCONTADOR) {
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR');
				}else {
					bcsComunes.msg("info","Informacion",$translate.instant('DATOS_INGRESO_EXITO'));	
					$scope.grillas.gridModulos.dataSource.read();
					$scope.seleccionModulo = false;
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
	
	var servicioCierreModulo= function(v_Dato){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=240";
		var r = $resource
		("", {}, {
					'urlConsulta': { method: "POST", url: urlConsulta}
				 }
		);
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	}
	
	$scope.cierreModulo = function() {
		if($scope.seleccionModulo === undefined){return}
		
		var parametrosSP = {};
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'PLA';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'N';
		
		parametrosSP.p_IdCREModuloEmpresa		= $scope.seleccionModulo.IdCREModuloEmpresa;
		parametrosSP.p_IdEstado					= $scope.seleccionModulo.IdEstado;
		parametrosSP.p_HoraApertura				= ($scope.seleccionModulo.HoraApertura === null)?'':$scope.seleccionModulo.HoraApertura;
		parametrosSP.p_FechaProceso 			= util.date($scope.SesionFechaSistema +" "+$scope.SesionHoraSistema).format("YYYY-MM-DD HH:mm:ss");
		parametrosSP.p_TipoEstado				= $scope.seleccionModulo.TipoEstado;
		
		console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
		
		servicioCierreModulo(parametrosSP).then(
			function(result) {
				var msg = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ADVERTENCIA');
				}else if (result.P_OUT_ERRORESCONTADOR) {
					msg = $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR');
				}else {
					bcsComunes.msg("info","Informacion",$translate.instant('DATOS_INGRESO_EXITO'));	
					$scope.grillas.gridModulos.dataSource.read();
					$scope.seleccionModulo = false;
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
	
	
	
});