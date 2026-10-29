angular.module('ModuloNormativa').controller('PanelSolicitudInterfacesCtrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,$timeout){
	
	$scope.formData = {};
	$scope.data = {mensual:false,diario:false,PeriodoOptions:{format: 'MMMM yyyy', start: 'year',depth: 'year'}};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.formInfo = {
			fechaSolicitud : util.dateInicioDia($scope.SesionFechaSistema),
			usuario : $scope.SesionUsuario
	};
	
	var getSchemaConsultaArchivoInterfaces = function(){
		return {
			model : {
				fields : {
					IdInterfaz		: { type : "number"},
					Nombre			: { type : "string"},
					Descripcion		: {	type : "string"},
					JsonData 		: { type : "string"},
					IdTipoPrograma 	: { type : "string"},
					TipoInterfaz 	: { type : "string"},
					IdEstado		: { type : "string"}
				}
			}
		};
	}
	
	var getDataSourceConsultaArchivoInterfaces = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdInterfaz", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4191",
						dataType: "json"
					}
				},
				schema : getSchemaConsultaArchivoInterfaces()
			},
			
		});
	}
	
	var diarios = getDataSourceConsultaArchivoInterfaces();
	var mensual = getDataSourceConsultaArchivoInterfaces();
	
	$scope.configuracionFiltros = {};

	$scope.configuracionInterfaz = {};
	
	bcsComunes.ventanaOverlay($scope.winID,true);
	$scope.data.matrizDiario = {};
	$scope.data.matrizMensual = {};
	$scope.data.checkInterfaz = {};
	var filtrosDiarios = [];
	filtrosDiarios.push({field: "TipoInterfaz", operator: "eq", value: "DIARIO"});
	filtrosDiarios.push({field: "IdEstado", operator: "eq", value: 'ACT'});
	diarios.query({filter:filtrosDiarios}).then(function(){
		if(diarios.data().length > 0){
			var datos = diarios.data();
			$.each(datos,function(key,value){
				$scope.data.matrizDiario[value.IdInterfaz] = value;
				$scope.data.checkInterfaz[value.IdInterfaz] = false;
				bcsComunes.ventanaTamanoAutomatico($scope.winID,20);
			});
		}
		bcsComunes.ventanaOverlay($scope.winID,false);
	});
	var filtrosMensual = [];
	filtrosMensual.push({field: "TipoInterfaz", operator: "eq", value: "MENSUAL"});
	filtrosMensual.push({field: "IdEstado", operator: "eq", value: 'ACT'});
	mensual.query({filter:filtrosMensual}).then(function(){
		if(mensual.data().length > 0){
			var datos = mensual.data();
			$.each(datos,function(key,value){
				$scope.data.matrizMensual[value.IdInterfaz] = value;
				$scope.data.checkInterfaz[value.IdInterfaz] = false;
				bcsComunes.ventanaTamanoAutomatico($scope.winID,20);
			});
		}
		bcsComunes.ventanaOverlay($scope.winID,false);
	});
	
	$scope.oSocioNegocio = {};
	
	$scope.formInfo.limpiarFiltros = function(soloFiltros)
	{
		$scope.formInfo.fechaSolicitud = util.dateInicioDia($scope.SesionFechaSistema);
		if(!soloFiltros) $scope.formData.idInterfaz = false;
		$scope.formData.fecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.mes = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.idClienteActual = false;
		$scope.formData.esTodosCliente = true;
		$scope.formData.esTodosInstrumento = true;
		$scope.formData.setInstrumento = "-1";
		bcsComunes.ventanaTamanoAutomatico($scope.winID,20);
	};
	
	$scope.$watch('formData.Interfazseleccionado',function(newval,oldval){
		 if(newval && newval.IdInterfaz){
		 	$scope.formInfo.limpiarFiltros(true);
			var datos = JSON.parse(newval.JsonData);
			if(datos && datos.filtros){
				$scope.configuracionInterfaz = datos;
				if(datos.filtros.cliente){
					$scope.configuracionFiltros.activadoComboboxCliente = datos.filtros.cliente.habilitado;
					$scope.configuracionFiltros.disabledEsTodosCliente =  !datos.filtros.cliente.todos;
					$scope.formData.esTodosCliente = datos.filtros.cliente.default_todos;
				}
				if(datos.filtros.instrumento){
					$scope.configuracionFiltros.disabledInstrumento = !datos.filtros.instrumento.habilitado;
					$scope.configuracionFiltros.disabledEsTodosInstrumentos =  !datos.filtros.instrumento.todos;
					$scope.formData.esTodosInstrumento = datos.filtros.instrumento.default_todos;
				}
				if(datos.filtros.fecha_desde){
					$scope.configuracionFiltros.disabledFechaDesde = !datos.filtros.fecha_desde.habilitado;
				}
				if(datos.filtros.fecha_hasta){
					$scope.configuracionFiltros.disabledFechaHasta = !datos.filtros.fecha_hasta.habilitado;
				}
			}
		 }
	 });
	
	$scope.formInfo.ingresarSolicitarInterfaz = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosValidacion = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_FechaEjecucion : util.dateSPdate($scope.formData.fecha)
		}
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'NOR',
				p_TipoPrograma : [],
				p_Parametro : []
		}
		var i = 0;
		$.each($scope.data.checkInterfaz,function(key,value){
			if($scope.data.checkInterfaz[key]){
				var interfaz = $scope.data.matrizDiario[key] || $scope.data.matrizMensual[key]
				datos.p_TipoPrograma.push({
					IdInterno	:	i,
					IdTipoPrograma : interfaz.IdTipoPrograma,
					SegundosDesfase	:	0
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'IdModulo',
					Valor : 'NOR'
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'FechaDesde',
					Valor : interfaz.TipoInterfaz === 'DIARIO' ? util.dateSPseconddate($scope.formData.fecha) : util.dateSPseconddate($scope.formData.mes)
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'FechaHasta',
					Valor : interfaz.TipoInterfaz === 'DIARIO' ? util.dateSPseconddate(util.dateTerminoDia($scope.formData.fecha)) : util.dateSPseconddate(util.dateTerminoDia($scope.formData.mes))
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'IdCliente',
					Valor : $scope.formData.idClienteActual || 0
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'TodosClientes',
					Valor : $scope.formData.esTodosCliente ? 'S' : 'N'
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'IdInstrumento',
					Valor : $scope.formData.instrumento ? $scope.formData.instrumento.IdInstrumento : 0
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'TodosInstrumentos',
					Valor : $scope.formData.esTodosInstrumento ? 'S' : 'N'
				});
				datos.p_Parametro.push({
					IdInterno : i,
					NombreParametro : 'Ejercicio',
					Valor : interfaz.TipoInterfaz === 'MENSUAL' ? util.dateSPdate($scope.formData.mes).substring(5,7) + util.dateSPdate($scope.formData.mes).substring(0,4) : ""
				});
				i++;
			}
		});
		
		if($scope.SesionLocale === 'PE'){
			bcsServicioInterfaces.ValidaGenerarInterfaz(datosValidacion).$promise.then(
			function(resultado){
				if(resultado.P_OUT_ERRORESCONTADOR != 0){
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_CERRAR_MODULOS'),
										    conflictos : util.parseServerResponse(resultado)}, $scope.winID);
				}else{
					bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay($scope.winID.ventanaId,false);
						var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
						bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
							conflictos : util.parseServerResponse(result)}, $scope.winID);

					},
					function(result){
						bcsComunes.ventanaOverlay($scope.winID,false);
						bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
					});
				}
			});	
		}else{
			bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay($scope.winID.ventanaId,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);

			},
			function(result){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			});
		}		
	}
	
	$scope.formInfo.limpiarFiltros();
	
})