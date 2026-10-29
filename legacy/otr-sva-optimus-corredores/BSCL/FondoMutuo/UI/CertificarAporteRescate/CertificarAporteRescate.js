angular.module('ModuloFFM').controller('CertificarAporteRescateCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.formData.filtros = {};
	$scope.filtros = {};
	
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.filtros.fechaCertificacion = util.dateInicioDia($scope.SesionFechaSistema);

	$scope.formData.filtrosTipoOperacion = {
			IndicadorAporteRescate : { "field"   : "IndicadorAporteRescate","operator": "eq", "value"   :  undefined},
			TieneCertificacion : {"field": "TieneCertificacion","operator": "eq", "value"   :  'S'}
	};
	
	var filtros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,		
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FMM',
			p_FechaCertificacion : $scope.SesionFechaSistema,
			
	};
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	
	var ds = new bcs.DataSource({ 
		type : "call",
		definition : {
			callTransaction	: "700186",
			serverFiltering : false, 
			serverPaging : false, 
			serverSorting : false,
			callParameters  : filtros,
			callResultSet   : "P_OUT_RESULTADO",
			sort: { field: "IdSerieFondo", dir: "asc" },
			pageSize : 200,
			schema : {
				model : {
					fields : {
						IdTipoMovimientoFondoMutuo				: { type : "string"},
						DescripcionTipoMovimiento				: { type : "string"},
						IdSerieFondo				            : { type : "string"},
						FechaCertificacion						: { type : "date"},
						ValorCuota							    : {	type : "number"},
						FechaCierre 							: { type : "date"},
						IdMonedaFondo 					        : { type : "string"},
						CantidadRegistros						: { type : "number"},
						Monto               					: { type : "number"},
						IdEmpresa								: { type : "number"},
						DecimalesValorCuota						: { type : "number"},
						IdEmisor								: { type : "string"},
						EsFondoNacional							: { type : "string"},
					}
				}
			}
		},			
	});
	
	$scope.defaultOptions = {
		      		
      		selectable : "row",
	  		filterable : {
	  			mode : "row"
	  		},	
	  		autoBind:true,
			height:450,
	  		dataSource: ds,
		
			columns: [
			           bcsConfiguracionFFM.getConfiguracionColumna("DescripcionTipoMovimiento")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondo")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("IdMonedaFondo")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("FechaCertificacion")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("CantidadRegistros")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("TotalMonto")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("FechaValorCuota")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("ValorCuota")
			],
			gridCreated : function(grid) {
				ds.setCallParameters(filtros);
			}
	};
	
	$scope.buscarAportesRescates = function(){
		filtros.p_IdEmisor =  $scope.filtros.IdEmisor || '';
		filtros.p_IdMonedaFondo = $scope.filtros.IdMoneda || '';
		filtros.p_IdSerieFondo = $scope.filtros.FondoMutuo ? $scope.filtros.FondoMutuo.IdSerieFondo : '';
		filtros.p_EsNacional = $scope.filtros.EsNacional ? 'S' : '';
		filtros.p_TipoSolicitud = $scope.filtros.tipoSolicitud != 0 ? $scope.filtros.tipoSolicitud : '';
		filtros.p_IdTipoMovimientoFondoMutuo = $scope.filtros.tipoOperacion != 0 ? $scope.filtros.tipoOperacion : '';
		filtros.p_MoneyMarket = $scope.filtros.certificaMoneyMarket ? 'S' : '';
		filtros.p_FechaCertificacion = util.dateSPdate($scope.filtros.fechaCertificacion) || '0001-01-01';
		
		ds.setCallParameters(filtros);
	};
	
	 $scope.formData.certificarAportesRescates = function()
     {
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var series = [];
		 $.each($scope.defaultOptions.dataSource.data(),function(key,value) {
			 	var serie = {IdInterno : 0, IdSerieFondo: value.IdSerieFondo};
			 	series.push(serie);
		 	});
		 var datosCertificarAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_SimularOperacion : 'S',
				 p_UsaReglasNegocio : 'S',
				 p_FechaCertificacion : util.dateSPdate($scope.filtros.fechaCertificacion),
				 p_Solicitudes : [],
				 p_Filtros : [{
					    IdInterno		      		 :	0,
					    IdEmisor                	 :  ($scope.filtros.IdEmisor != 0) ? $scope.filtros.IdEmisor : '',
					    IdMoneda                	 :  ($scope.filtros.IdMoneda != 0) ? $scope.filtros.IdMoneda : '',
					    IdSerieFondo            	 :  ($scope.filtros.FondoMutuo && $scope.filtros.FondoMutuo.IdSerieFondo) ? $scope.filtros.FondoMutuo.IdSerieFondo : '',
					    Nacional                	 :  ($scope.filtros.EsNacional) ? 'S' : 'N',
					    TipoSolicitud    			 :	($scope.filtros.tipoSolicitud != 0) ? $scope.filtros.tipoSolicitud : '',
					    IdTipoMovimientoFondoMutuo   :	($scope.filtros.tipoOperacion != 0) ? $scope.filtros.tipoOperacion : '',
					    MoneyMarket                  :  ($scope.filtros.certificaMoneyMarket) ? 'S' : 'N',
					    IdCarga						 : 	0
				 }]
		 }
	     bcsServicioFondosMutuos.CertificarPorFiltrosSolicitudAporteRescate(datosCertificarAporteRescate).$promise.then(function(respuesta){
	     	 var id = bcsComunes.ventanaAbrir("ConfirmarCertificarFFM", {datosCertificarAporteRescate: datosCertificarAporteRescate, respuesta: respuesta, masivo: true}, false, false, $scope.winID );
	     	 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $scope.buscarAportesRescates();
		     });
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
     }
	 
	$scope.formData.consultarLogCertificacionAportesRescates = function()
	{
		bcsComunes.ventanaAbrir("LogCertificarFFM", {} );
	};
	
	$scope.limpiarFiltros = function(){
		$scope.filtros.IdEmisor = undefined;
		$scope.filtros.IdMoneda = undefined;
		$scope.formData.setFondo = "-1";
		$scope.filtros.EsNacional = false;
		$scope.filtros.tipoSolicitud = undefined;
		$scope.filtros.tipoOperacion = undefined;
		$scope.filtros.certificaMoneyMarket = false;
		$scope.filtros.fechaCertificacion = util.dateInicioDia($scope.SesionFechaSistema);
	};
	
	$scope.formData.test = function()
	{
		bcsComunes.ventanaAbrir("ResumenCartaLiquidacion", {} );
	};
});