angular.module('ModuloOrdenes').controller('ConsultarOrdenesReporteCtrl',function($scope,$translate,bcsComunes,bcsServicioPlataforma,bcsConfiguracionOrdenesV2,bcsServicioOrden,$timeout){
	
	$scope.formData = {};
	$scope.filtros ={IdCliente: { "field": "IdCliente","operator": "eq", "value"   :  undefined}};
	$scope.filtros.IdClasificacionTipoInstrumento = 'RE';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.idTipoOperacion='COMRRV';
	$scope.datos = {};
	
	var conf = {
			buscar : function(apiGrid,filtros) {
				apiGrid.dataSource.filter(bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.filtros,'REPORTES',''));			
			},
			options : {
				userSettings:true,
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: true,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: false,
	      		scrollable: true,
	      		height:400,
	      		rowCellColor : function(row) {
	      			switch(row["AccionOrden"].toUpperCase()) {
					case 'VENTA':
						return {column: "AccionOrden", color: "red"};
						break;
					case 'COMPRA':
						return {column: "AccionOrden", color: "green"};
						break;
				}
				
				},
				rowColorBackground : function(row) {
					console.log(row["IdEstadoOrden"].toUpperCase());
					switch(row["IdEstadoOrden"].toUpperCase()) {
						case "ANU":
							return "red";
							break;
						case "RZD":
							return "red";
							break;	
						case "VCD":
							return "green";
							break;
						case "PDA":
							return "yellow";
							break;
						case "ANP":
							return "yellow";
							break;
							
					}
				},
				dataSource: bcsConfiguracionOrdenesV2.getDataSourceOrdenesReporte(),

				columns: [
					bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("AccionOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("ReportanteOReportado",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoSubyacente",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento", {groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tasa",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("Plazo",{groupable:true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("SaldoRep",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrden",{ groupable: true})
				          ,bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsConfirmadaUI",{ groupable: true})
				          
				          
				         
				],
			
			
			}
	};
	
		
	$scope.buscarOrdenesReportes = conf.buscar;
		
	$scope.oSocioNegocio={
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.oPortafolio={
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  undefined }
		 },
	 };
	
	$scope.oConfiguracionTipoOperacion={
			filtros : {
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RE' }
			
			}
	};
	
	$scope.oConfiguracionOperacion={
			filtros:{
				IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": 'RE'}
			}
	}
	
	$scope.oConfiguracionCondicionLiquidacion = {
 			filtros:{
 				AccionTipoOperacion : {"field": "AccionTipoOperacion","operator": "eq","value": undefined},   
 				IdTipoOperacion		: {"field": "IdTipoOperacion","operator":"eq","value": undefined}
 			}
 	}

	$scope.formData.confGridOrdenesReportes = conf.options;
	
	
	$scope.crearOrdenReportado=function (){
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		var id = bcsComunes.ventanaAbrir("IngresoOperacionesReporte",{},false); 
	    var promesa=bcsComunes.ventanaPromesa(id);                               
	    $.when(promesa).then(function(data){  
	    	$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
	        $scope.datos.kNgModel = undefined;
	        bcsComunes.ventanaOverlay($scope.winID,false);
	    }); 
		
		
	}
	$scope.crearOrdenReportante=function (){
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		var id = bcsComunes.ventanaAbrir("IngresoReportantes",{},false); 
	    var promesa=bcsComunes.ventanaPromesa(id);                               
	    $.when(promesa).then(function(data){  
	    	$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
	        $scope.datos.kNgModel = undefined;
	        bcsComunes.ventanaOverlay($scope.winID,false);
	    }); 
		
		
	}
	$scope.editarOrden=function (orden){
		if(!orden || orden.length == 0 || orden[0].AccionOrden != 'VENTA') return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var id = bcsComunes.ventanaAbrir("IngresoOperacionesReporte",{param:"EDITAR",orden:orden},false); 
	    var promesa=bcsComunes.ventanaPromesa(id);                               
	    $.when(promesa).then(function(data){  
	    	$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
	        $scope.datos.kNgModel = undefined;
	        bcsComunes.ventanaOverlay($scope.winID,false);
	    }); 
	}
	
	$scope.verReportantes = function(orden){
		if(!orden || orden.length == 0) return;
		bcsComunes.ventanaAbrir("VerReportantes",{reportado:orden[0]},false); 
	}
		
	$scope.confirmarDesconfirmarOrden = function(orden,esConfirmada){
		if(!orden || orden.length == 0) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa   : $scope.SesionIdEmpresa,
				p_IdUsuario   : $scope.SesionIdUsuario,
				p_IdModulo 	  : 'ORD',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				p_Ordenes	: []
		}
		var estadoAsoc = true;
		$.each(orden,function(key,ord){
			if (esConfirmada === 'S' && ord.MontoDisponibleAsociar > 0){
				estadoAsoc = false;
			}
  			datos.p_Ordenes.push({IdInterno: ord.IdOrden, IdOrden: ord.IdOrden, EsConfirmada: esConfirmada});
  		});
		if (!estadoAsoc) {
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_ORDEN_SIN_ASOCIAR_REPORTANTE'));
			bcsComunes.ventanaOverlay($scope.winID,false);
			return;
		}
		var errorTexto = esConfirmada === 'S' ? "UI_ERROR_CONFIRMAR_ORDEN_REPORTES" : "UI_ERROR_DESCONFIRMAR_ORDEN_REPORTES";
		var texto = esConfirmada === 'S' ? "UI_EXITO_CONFIRMAR_ORDEN_REPORTES" : "UI_EXITO_DESCONFIRMAR_ORDEN_REPORTES";
		
		bcsServicioOrden.ConfirmarDesconfirmarOrdenes(datos).$promise.then(function(respuesta){
			var idv;
			if(respuesta.P_OUT_ERRORESCONTADOR == 0){
				idv=bcsComunes.ventanaMsg({mensaje:$translate.instant(texto),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				
			}else{
				idv=bcsComunes.ventanaMsg({mensaje:$translate.instant(errorTexto),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			
			$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
			$scope.datos.kNgModel = undefined;

			var promesa=bcsComunes.ventanaPromesa(idv); 
            $.when(promesa).then(function(){
            	
               bcsComunes.ventanaOverlay($scope.winID,false);
             });
			
			
			
		},function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant(errorTexto),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
			$scope.datos.kNgModel = undefined;
			bcsComunes.ventanaOverlay($scope.winID,false);
		});
	}
	
	$scope.anularOrden=function (ordenesTemp){
		if (!ordenesTemp || ordenesTemp.length == 0) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var id = bcsComunes.ventanaAbrir("IngresarMotivoAnularOrdenes", {} ,false,false,$scope.winID);
		var promesa = bcsComunes.ventanaPromesa(id);
		promesa.then(function(motivo) {
			if (motivo && angular.isString(motivo)) {
				var datos = {
	    			p_IdEmpresa : $scope.SesionIdEmpresa,
		  			p_IdUsuario : $scope.SesionIdUsuario,
		  			p_IdModulo : 'ORD',
		  			p_ordenes : [],
	    		};
		    	$.each(ordenesTemp,function(key,ord){
		  			datos.p_ordenes.push({IdOrden : ord.IdOrden,MotivoAnulacion : motivo});
		  		});
		    	//bcsComunes.ventanaOverlay($scope.idVentana,true);
		    	bcsServicioOrden.AnularOrdenMasiva(datos).$promise.then(function(result){
		    		bcsComunes.ventanaOverlay($scope.winID,false);
					var id = bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana)

					$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
		    		
		    	},function(result){
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.buscarOrdenesReportes($scope.apiGridOrdenesReportes,$scope.filtros);
		    		
		    	});
			}
		});
	}
	$scope.anticiparOrden=function (orden){
		
	}
	
	$scope.abrirDetalle = function (items) {
		
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden},false,false);
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdSocioNegocio=false;
		$scope.filtros.IdPortafolio = 0;
		$scope.filtros.instrumentoSubyacente=undefined;
		$scope.filtros.buscarInstrumento="-1";
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.tipoOperacion = undefined;
		$scope.filtros.OrdenesPorAsociar = true;
		$scope.filtros.IdCondicionDeLiquidacion = undefined;
		$scope.filtros.AccionOrden = 'VENTA';
	};
	
	$scope.limpiarFiltros();
		
});