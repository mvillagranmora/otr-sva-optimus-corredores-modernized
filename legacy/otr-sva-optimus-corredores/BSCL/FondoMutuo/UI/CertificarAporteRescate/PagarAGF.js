angular.module('ModuloFFM').controller('PagarAGFCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.formData.filtros = {
		TiposMovimientoLiquidacion : [{"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APONORM"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APOTRASP"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "RESNORM"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APOREINV"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "RESREINV"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APORESPRO"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APOOVER"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "RESTRAPOR"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "RESTRASP"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "APODIVCFI"},
									  {"field": "IdTipoMovimientoFondoMutuo","operator": "eq","value": "RESCNN"}
									  ],
		IdEstadoLiquidacion : 'ING',
		Switch : 'N'
	};
	var filtros = [];
	var conf = bcsConfiguracionFFM.getConsultaMovimientosFondosMutuos();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarSolicitudes = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	$scope.dataTemplate = {};
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.formData.fechaOptions = {
			max: util.dateTerminoDia($scope.SesionFechaSistema)
	}
	
	$scope.formData.pagarAGF = function()
    {
		var movimientos = [];
		$.each($scope.seleccionMovimientos, function(index,value) {
			movimientos.push({
				IdMovimiento : value.IdSolicitudAporteRescate,
			})
		});
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 console.log($scope.formData.filtros.FechaCertificacion);
		 var datosPagarAGF = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_SimularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 p_FechaCertificacion : $scope.SesionFechaSistema,
				 p_Movimientos : movimientos,
				 p_Filtros : [{
					    IdInterno		      	:	0,
					    IdEmisor 	          	:	(!$scope.formData.filtros.IdEmisor || $scope.formData.filtros.IdEmisor === ' TODOS' || $scope.formData.filtros.IdEmisor == '0') ? '' : $scope.formData.filtros.IdEmisor,
					    TipoSolicitud          	:	$scope.formData.filtros.TipoSolicitud,
					    IdEstado         		:	$scope.formData.filtros.IdEstado,
					   	FechaCertificacion      :	$scope.formData.filtros.FechaCertificacion == undefined ? '' : util.dateSPdate($scope.formData.filtros.FechaCertificacion),
					    FechaLiquidacion        :	$scope.formData.filtros.FechaLiquidacion == undefined ? '' : util.dateSPdate($scope.formData.filtros.FechaLiquidacion),
					    IdSerieFondo         	:	$scope.formData.filtros.FondoMutuo.IdSerieFondo ? $scope.formData.filtros.FondoMutuo.IdSerieFondo : ''
				 }]
		 }
		 
		 bcsServicioFondosMutuos.LiquidacionSolicitudAporteRescate(datosPagarAGF).$promise.then(function(respuesta){
		 	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
		 		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_PROCESO_LIQUIDACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		 		$scope.buscarSolicitudes($scope.apiGridSolicitud,$scope.formData.filtros);
		 	 }else{
		 		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_ERROR_LIQUIDACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		 	 }
		 },function(respuesta){
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_ERROR_LIQUIDACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		 });
    }
	
	$scope.formData.verCartas = function() {
		if(!$scope.formData.filtros.FechaCertificacion || !$scope.formData.filtros.FechaLiquidacion) bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_FILTROS'));
		var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'ArchLiq' },
		                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
		                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({filter: filterTemplate}).then(function(){
			if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
				
				var IdSerieFondo = ($scope.formData.filtros.FondoMutuo.IdSerieFondo) ? $scope.formData.filtros.FondoMutuo.IdSerieFondo : ''; 
				var IdEstado = ($scope.formData.filtros.IdEstado === 0) ? '' : $scope.formData.filtros.IdEstado;
				var TipoSolicitud = ($scope.formData.filtros.TipoSolicitud === 0) ? '' : $scope.formData.filtros.TipoSolicitud;
				var IdEmisor = (!$scope.formData.filtros.IdEmisor || $scope.formData.filtros.IdEmisor === ' TODOS' || $scope.formData.filtros.IdEmisor == '0') ? '' : $scope.formData.filtros.IdEmisor;
				/*$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+
																					  ",p_IdUsuario =>" + $scope.SesionIdUsuario + 
																					  ",p_IdModulo =>'FMM'"+ 
																					  ",p_IdEmisor =>'" + IdEmisor + 
																					  "',p_FechaMovimiento =>'" + util.dateSPdate($scope.formData.filtros.fechaMovimiento) + 
																					  "',p_FechaPago =>'" + util.dateSPdate($scope.formData.filtros.fechaPago) + 
																					  "',p_SerieFondo =>'" + IdSerieFondo +
																					  "',p_Estado =>'" + IdEstado +
																					  "',p_TipoSolicitud =>'" + TipoSolicitud + "')";
																					  */
				var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
									"p_IdUsuario"				: $scope.SesionIdUsuario,
									"p_IdModulo"		: 'FMM',
									"p_IdEmisor" : IdEmisor,
									"p_FechaMovimiento" : util.dateSPdate($scope.formData.filtros.FechaCertificacion),
									"p_FechaPago" : util.dateSPdate($scope.formData.filtros.FechaLiquidacion),
									"p_SerieFondo" : IdSerieFondo,
									"p_Estado" : IdEstado,
									"p_TipoSolicitud" : TipoSolicitud,
									
			                        "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									  };
				
				$scope.dataTemplate.parameters= [];
				
				$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				var subrep = new Array();
				$scope.dataTemplate.subReports=subrep;
				
				bcsConfiguracionFFM.getReport($scope.dataTemplate);
			}
		});

	};
	
$scope.formData.verInformeLiquidacion = function() {
	if(!$scope.formData.filtros.FechaCertificacion || !$scope.formData.filtros.FechaLiquidacion) bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_FILTROS'));
		var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'InfoLiq' },
		                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
		                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({filter: filterTemplate}).then(function(){
			if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
				
				var IdSerieFondo = ($scope.formData.filtros.FondoMutuo.IdSerieFondo) ? $scope.formData.filtros.FondoMutuo.IdSerieFondo : ''; 
				var IdEstado = ($scope.formData.filtros.IdEstado === 0) ? '' : $scope.formData.filtros.IdEstado;
				var TipoSolicitud = ($scope.formData.filtros.TipoSolicitud === 0) ? '' : $scope.formData.filtros.TipoSolicitud;
				var IdEmisor = (!$scope.formData.filtros.IdEmisor || $scope.formData.filtros.IdEmisor === ' TODOS' || $scope.formData.filtros.IdEmisor == '0') ? '' : $scope.formData.filtros.IdEmisor;
				
				var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
									"p_IdUsuario"				: $scope.SesionIdUsuario,
									"p_IdModulo"		: 'FMM',
									"p_IdEmisor" : IdEmisor,
									"p_FechaMovimiento" : util.dateSPdate($scope.formData.filtros.FechaCertificacion),
									"p_FechaPago" : util.dateSPdate($scope.formData.filtros.FechaLiquidacion),
									"p_SerieFondo" : IdSerieFondo,
									"p_Estado" : IdEstado,
									"p_TipoSolicitud" : TipoSolicitud,
									
			                        "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									  };
				
				$scope.dataTemplate.parameters= [];
				
				$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				var subrep = new Array();
				$scope.dataTemplate.subReports=subrep;
				
				bcsConfiguracionFFM.getReport($scope.dataTemplate);
			}
		});

	};
	
	$scope.inicializarFiltros = function()
	{
		 $scope.formData.filtros.FechaCertificacion = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.formData.filtros.FechaLiquidacion = util.dateTerminoDia($scope.SesionFechaSistema);
		 $scope.formData.filtros.FechaCurse = util.dateTerminoDia($scope.SesionFechaSistema);
	};
	
	//$scope.inicializarFiltros();
	
	$scope.ajustaVentana = function()
	{
		bcsComunes.ventanaAutoHeight($scope.winID);
	}
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.buscar = function(filtros)
	{
		$scope.buscarSolicitudes($scope.apiGridSolicitud,filtros);
	}
});