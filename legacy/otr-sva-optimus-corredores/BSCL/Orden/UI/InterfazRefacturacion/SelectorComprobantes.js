angular.module('ModuloOrdenes').controller('SelectorComprobantesCtrl',
		function($scope,bcsServicioOrden,bcsComunes,bcsConfiguracionOrdenesV2,$timeout,$translate){
	
	$scope.fechaComprobante = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.aceptar = function() {
		bcsComunes.ventanaData(idVentana, $scope.seleccionado);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.buscar = function() {
		
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_FechaConsulta : util.dateSPdate($scope.fechaComprobante)
				
				
			};
						
			bcsComunes.ventanaOverlay(idVentana,true);
			var p = bcsServicioOrden.ConsultarComprobantesRefacturacion(datos).$promise;
			p.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_COMPROBANTESFACTURACION.length === 0) {
					bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_RESULTADOS");
				}
				
				var comprobantes = [];
				$.each(result.P_OUT_COMPROBANTESFACTURACION,function(key,value){
					comprobantes.push(value);
					$.each(result.P_OUT_MOVIMIENTOS,function(key2,value2){
						if (!value.movimientos) {
							value.movimientos = [];
						}
						if (value.IdComprobante === value2.IdComprobante) {
							value.movimientos.push(value2);
						}
					});
					
				})
				
				$scope.apiGrid.dataSource.data(comprobantes);
				
			},function(result){
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(result)
				});
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		
	}
	
	$scope.configuracionDetalle = function(dataItem) {
		return {
			allowCopy: true,
			autoBind: true,
      		filterable: false,
			groupable: false,
			height:100,
			navigatable: true,
			reorderable: true,
			resizable: true,
      		selectable: "multiple,row",
			sortable: true,
			userSettings:true,
			
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : dataItem.movimientos,
					schema : {
						model : {
							fields : {
								FolioTransaccion 		:	{ type : "string"},
								FechaTransaccion 		:	{ type : "sysdate"},
								NombreCompleto 			:	{ type : "string"},
								CodigoPortafolio 		:	{ type : "string"},
								Instrumento				:	{ type : "string"},
								Cantidad				:	{ type : "string"},
								IdMoneda				:	{ type : "string"},
								DocumentoIdentidad		:	{ type : "string"}
							}
						}
					}
				
	  			}
			}),
	  		columns: [
	  			bcsConfiguracionOrdenesV2.getConfiguracionColumna("FolioTransaccion"),
	  			bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaTransaccion"),
	  			bcsConfiguracionOrdenesV2.getConfiguracionColumna("Instrumento"),
	  			bcsConfiguracionOrdenesV2.getConfiguracionColumna("Cantidad")
	  			
					
			]
		}
	}
	
	var schemaComprobantes = bcsConfiguracionOrdenesV2.getSchemaComprobante();
	schemaComprobantes.model.fields.movimientos = {type : "array"};
	
	
	$scope.configuracion = {
			allowCopy: true,
			autoBind: false,
      		filterable: false,
			groupable: false,
			height:220,
			navigatable: true,
			reorderable: true,
			resizable: true,
      		selectable: "multiple,row",
			sortable: true,
			userSettings:true,
			gridCreated : function(grid) {
				$scope.buscar();
			},
			dataSource: new bcs.DataSource({
	  			type : "array",
	  			definition : {
					data : [],
					schema : schemaComprobantes
				
	  			}
			}),
	  		columns: [
	  			bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdComprobante")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoComprobante")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEstadoComprobante")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto")
					,bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio")
					
			]
	}
	

});
