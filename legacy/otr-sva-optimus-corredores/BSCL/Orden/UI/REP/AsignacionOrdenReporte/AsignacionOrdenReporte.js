angular.module('ModuloOrdenes').controller('AsignacionOrdenReporteCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,$timeout){
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.data = {};
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	console.log(JSON.stringify($scope.parametros.transacciones[0]));
	if(!$scope.parametros.transacciones[0]){
		bcsComunes.ventanaCerrar($scope.winID);
		return false;
	}
	var filtros={};
			
			if($scope.parametros.transacciones[0].IdTipoOperacion==='VENRRV' || $scope.parametros.transacciones[0].IdTipoOperacion==='VENRRF'){
					filtros = [{field:"IdTipoOperacion", operator: "eq", value: $scope.parametros.transacciones[0].IdTipoOperacion},
	                          		{field:"IdEstadoOrden", operator: "eq", value: "OFE"},
	                          		{field:"OrdenVigente", operator: "eq", value:"S"},
	                          		{field:"EsConfirmada", operator: "eq", value:"S"},
	                          		{field:"CantidadDisponibleOrden", operator:"gt", value:0},
	                          		{field:"IdMonedaOrden", operator:"eq", value:$scope.parametros.transacciones[0].IdMonedaInstrumento},
	                          		//{field:"FechaOperacionOrden",operator:"lt",value:util.dateDS($scope.parametros.transacciones[0].FechaOperacionTransaccion,'FechaHoraSistema','ODATA')},
	                          		{field:"IdInstrumentoSubyacente", operator:"eq", value:$scope.parametros.transacciones[0].IdInstrumentoSubyacente},
	                          		{field:"TasaPromedioReporte", operator:"eq", value:$scope.parametros.transacciones[0].Tasa},
	                          		{field:"IdBolsa", operator:"eq", value:$scope.parametros.transacciones[0].IdBolsa},
	                          		{field:"IdCondicionDeLiquidacion", operator:"eq", value:$scope.parametros.transacciones[0].IdCondicionDeLiquidacion},
	                          		{field:"PlazoReporte", operator:"eq", value:$scope.parametros.transacciones[0].Plazo}
	                          		];
			}
			else
			{	
				filtros = [{field:"IdTipoOperacion", operator: "eq", value: $scope.parametros.transacciones[0].IdTipoOperacion},
                    		{field:"IdEstadoOrden", operator: "eq", value: "OFE"},
                    		{field:"OrdenVigente", operator: "eq", value:"S"},
                    		{field:"CantidadDisponibleOrden", operator:"gt", value:0},//
                    		{field:"IdMonedaOrden", operator:"eq", value:$scope.parametros.transacciones[0].IdMonedaInstrumento},
                    		//{field:"FechaOperacionOrden",operator:"lt",value:util.dateDS($scope.parametros.transacciones[0].FechaOperacionTransaccion,'FechaHoraSistema','ODATA')},
                    		{field:"TasaPromedioReporte", operator:"eq", value:$scope.parametros.transacciones[0].Tasa},//
                    		{field:"IdBolsa", operator:"eq", value:$scope.parametros.transacciones[0].IdBolsa},
                    		{field:"IdCondicionDeLiquidacion", operator:"eq", value:$scope.parametros.transacciones[0].IdCondicionDeLiquidacion},
                    		{field:"PlazoReporte", operator:"eq", value:$scope.parametros.transacciones[0].Plazo}
                    		];
				
				
			}
			console.log(JSON.stringify(filtros));	
			var configuracionColumnas;
			if($scope.parametros.transacciones[0].IdTipoOperacion=='VENRRV' || $scope.parametros.transacciones[0].IdTipoOperacion=='VENRRF'  )	
					configuracionColumnas = function() {
					return { 
			
						NombreCliente	  			:{field: "NombreCompleto",				title: $translate.instant('UI_CLIENTE'),			width: "100px",	align: "left"},
						DescripcionPortafolio	  	:{field: "CodigoPortafolio",			title: $translate.instant('UI_PORTAFOLIO'),			width: "100px",	align: "left"},
						CantidadDisponibleOrden			:{field: "CantidadDisponibleOrden",			title: $translate.instant('UI_CANTIDAD'),			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(CantidadDisponibleOrden,'ORD','Cantidad',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
						CantidadAsignadaOrden		:{field: "CantidadAsignadaOrden",		title: $translate.instant('UI_CANTIDAD_ASIGNADA'),	width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(CantidadAsignadaOrden,'ORD','Cantidad',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
						MontoDisponibleOrden	  			:{field: "MontoDisponibleOrden",				title: $translate.instant('UI_MONTO'),		width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoDisponibleOrden,'ORD','Monto',IdMonedaOrden, '','')).valor #</label>"},
						Moneda	  					:{field: "IdMonedaOrden",				title: $translate.instant('UI_MONEDA'),				width: "100px",	align: "left"},
						Tasa	  					:{field: "Tasa",						title: $translate.instant('UI_TASA'),				width: "100px",	align: "left"},
					}     
				};
			else
				configuracionColumnas = function() {
				return { 
		
					NombreCliente	  			:{field: "NombreCompleto",				title: $translate.instant('UI_CLIENTE'),			width: "100px",	align: "left"},
					DescripcionPortafolio	  	:{field: "CodigoPortafolio",			title: $translate.instant('UI_PORTAFOLIO'),			width: "100px",	align: "left"},
					CantidadAsociada   			:{field: "CantidadAsociada",			title: $translate.instant('UI_CANTIDAD'),			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(CantidadAsociada,'ORD','Cantidad',IdMonedaOrden,IdTipoInstrumento,'')).valor #</label>"},
					
					MontoAsociado	  			:{field: "MontoAsociado",					title: $translate.instant('UI_MONTO'),		width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoAsociado,'ORD','Monto',IdMonedaOrden, '','')).valor #</label>"},
					Moneda	  					:{field: "IdMonedaOrden",				title: $translate.instant('UI_MONEDA'),				width: "100px",	align: "left"},
					Tasa	  					:{field: "Tasa",						title: $translate.instant('UI_TASA'),				width: "100px",	align: "left"},
				}     
			};
				
			
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	vards = bcsConfiguracionOrdenesV2.getDataSourceOrdenesReportePorAsignar();
	vards.definition.filter = filtros;
	
	var conf = {
			options : {
				userSettings:true,
				sortable: true,
	      		reorderable: true,
	      		filterable: false,
				groupable: false,
				pageable: true,
				selectable: "single, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:250,
		
				dataSource: vards,
			
			}
	};
	
	if($scope.parametros.transacciones[0].IdTipoOperacion=='VENRRV' || $scope.parametros.transacciones[0].IdTipoOperacion=='VENRRF'  )
		conf.options.columns=[
			getConfiguracionColumna("NombreCliente")
			,getConfiguracionColumna("DescripcionPortafolio")
			,getConfiguracionColumna("CantidadDisponibleOrden")//bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden")//"CantidadDisponibleOrden"
			,getConfiguracionColumna("CantidadAsignadaOrden")
			,getConfiguracionColumna("MontoDisponibleOrden")
			,getConfiguracionColumna("Moneda")
			,getConfiguracionColumna("Tasa")
		
			]
	else
		conf.options.columns=[
			getConfiguracionColumna("NombreCliente")
			,getConfiguracionColumna("DescripcionPortafolio")
			//,getConfiguracionColumna("CantidadDisponibleOrden")//getConfiguracionColumna("CantidadDisponibleAsociar")//"CantidadDisponibleAsociar"
			,getConfiguracionColumna("MontoAsociado")
			,getConfiguracionColumna("Moneda")
			,getConfiguracionColumna("Tasa")
		
			]
		
		
	
	$scope.data.configGridOrdenesReporte = conf.options;
	
	$scope.data.configGridOrdenesReporte.gridCreated = function(grid){
		bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	}
	
	$scope.asignarOrdenReporte = function(dato){
	
		var asignacionesOrdenes=[];
		console.log("ASIGNANDO");
		console.log(JSON.stringify(dato));
		if (dato){
		
			//$.each(dato,function(key,value) {
					
			asignacionesOrdenes.push( {
				IdInterno		: dato.IdOrden,
				IdOrden			: dato.IdOrden,
				IdTransaccion	: $scope.parametros.transacciones[0].IdTransaccion, 
				Cantidad		: dato.CantidadDisponibleOrden,//dato.CantidadAsociada,//CantidadDisponibleAsociar,//CantidadOrden,
				ValorRescate 	: dato.ValorRescate,
				FolioCartera 	: 0
			});
					
			//});
				
			
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ORD',
					p_Asignaciones : asignacionesOrdenes
			};
			
			bcsComunes.ventanaOverlay($scope.winID,true);
			promise = bcsServicioOrden.CrearAsignaciones(datos).$promise;
			promise.then(function(result) {
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
	                conflictos:util.parseServerResponse(result)
				},$scope.winID);
				
				/*
				if (result.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADO_CREAR_ASIGNACIONES', campoSecuencia : "id" ,nombreSecuencia : 'UI_SECUENCIA',datos : result.P_OUT_MENSAJES } ,false,false,$scope.winID);
				}
				else {
						$.each(result.P_OUT_RESULTADO,function(key,value) {
								operaciones.push({id: value.IdAsignacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_ASIGNACION_CREADA')});
							});
						bcsComunes.msg("info","UI_INFORMACION","UI_ASIGNACION_CREADAS")
					}*/
								
		
			},function(result){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:'UI_ERROR_INTERNO_SISTEMA',
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
	
				
			}); 
		}
		else {
			bcsComunes.msg("warning",$translate.instant('UI_WARNING'),$translate.instant("UI_SELECCIONAR_ORDEN"));
		}
			
	}
	
	
	
	
	
	
	
});