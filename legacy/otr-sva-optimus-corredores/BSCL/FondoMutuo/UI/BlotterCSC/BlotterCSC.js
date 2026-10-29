angular.module('ModuloFFM').controller('BlotterCSCCtrl', function ($scope,$translate,bcsComunes,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionFFM)
{																

	
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaCSC();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscar = conf.buscar;
	var winID = bcsComunes.ventanaWinId($scope);
	
	
	
	
	// var conf
	$scope.winID=bcsComunes.ventanaWinId($scope);
	//$scope.formData.filtrosCliente = {
	$scope.dataTemplate = {};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fecha  = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.cliente = false;
			
	};
	
	//$scope.limpiarFiltros();
	
	
	$scope.AnulacionContratoCSC = function(vOpcion)
	{
		if(vOpcion === "CER")
		{
			bcsComunes.ventanaOverlay(winID,true);
			{
						
				var datosIngresoCSC = {
						 p_IdEmpresa : $scope.SesionIdEmpresa, 
						 p_IdUsuario : $scope.SesionIdUsuario, 
						 p_IdModulo : 'FMM',
						 p_Contrato : [{
							 			IdContrato		      		:	$scope.datos.kNgModel[0].IdFolioCSC,    	
						 }]
				 }
				
				
				 bcsServicioFondosMutuos.AnulacionContratoCSC(datosIngresoCSC).$promise.then(function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONTRATO_ANULADO_OK"),conflictos:util.parseServerResponse(respuesta)}, winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
					conf.options.dataSource.read();
				 },function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
				 });
						
				
			}
		}
		
		
	}
	
	$scope.RevocacionContratoCSC = function(vOpcion)
	{
		if(vOpcion === "REV")
		{
			bcsComunes.ventanaOverlay(winID,true);
			{
						
				var datosIngresoCSC = {
						 p_IdEmpresa : $scope.SesionIdEmpresa, 
						 p_IdUsuario : $scope.SesionIdUsuario, 
						 p_IdModulo : 'FMM',
						 p_Contrato : [{
							 			IdContrato		      		:	$scope.datos.kNgModel[0].IdFolioCSC,    	
						 }]
				 }
				
				
				 bcsServicioFondosMutuos.RevocacionContratoCSC(datosIngresoCSC).$promise.then(function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONTARO_REVOCADO_OK"),conflictos:util.parseServerResponse(respuesta)}, winID);
				    bcsComunes.ventanaOverlay($scope.winID,false);
				    conf.options.dataSource.read();
				 },function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
					bcsComunes.ventanaOverlay($scope.winID,false);
				 });
						
				
			}
		}
		
		
	}
	
	$scope.verComprobante = function() {
		
		bcsComunes.ventanaOverlay(winID,true);
	   
		var filterTemplate = [ 	{ field: "IdTipoDocumento", 	operator:"eq", 		value: 	'ComCSC' },
								{ field: "IdEmpresa",			operator: "eq", 	value: 	$scope.SesionIdEmpresa } ];
		
		bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
			if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
			
				var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
									"p_IdContratoCSC"			: $scope.datos.kNgModel[0].IdFolioCSC,
			                        "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
								};
			
				$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				
				$scope.dataTemplate.parameters= [];
				var subrep = new Array();
				$scope.dataTemplate.subReports=subrep;
				
				bcsConfiguracionFFM.getReport($scope.dataTemplate, winID);
				
			}
		});
		
	
			 
	}
	
	
	
	
	
	$scope.ingresarCSC = function(Solicitud)
	{
		if (Solicitud == 'EDITAR')
		{
			if ($scope.datos){
				var id = bcsComunes.ventanaAbrir("IngresarCSC", {IdTipoSolicitud: 'Editar',  ObjetoCSC: $scope.datos.kNgModel[0]},false,false,$scope.winID); 
		        var promesa=bcsComunes.ventanaPromesa(id);                               
		        $.when(promesa).then(function(data){
		        	conf.options.dataSource.read();
		        }); 
			}else{
				
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_REGISTRO_NO_SELECCIONADO"));
				bcsComunes.ventanaOverlay($scope.winID,false);
				return;
			}
			
		}
		bcsComunes.ventanaAbrir("IngresarCSC", {});
		
	
	};
	
	$scope.testRevocarCSC = function()
	{
		
		bcsComunes.ventanaAbrir("RevocacionContratoCSC", {});
		
	
	};
	
	$scope.confBasicaDetalleCSC = function(item) {
		var dsDef = bcsConfiguracionFFM.getDataSourceDetalleCSC;
		dsDef.definition.filter = 
		[
			 { field: "IdFolioCSC", operator: "eq",  value: item.IdFolioCSC}
		] 
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: bcsConfiguracionFFM.columnasDetalleCSC(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row",
			userSettings		: true
		};
	};
	
	
	
	
	
	
});