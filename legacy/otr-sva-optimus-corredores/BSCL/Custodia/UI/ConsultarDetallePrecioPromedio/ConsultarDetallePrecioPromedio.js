angular.module('ModuloCustodia').controller('ConsultarDetallePrecioPromedioCtrl',
function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosCustodia,bcsAlerta)
{	
	var winId = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(winId);
	
	$scope.confCarteraDisponible = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_CARTERA', 		width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridCarteraDisponible = grid;
	      		$scope.gridCarteraDisponible.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confCarterasEnTransito = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_CARTERA', 		width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridCarterasEnTransito = grid;
	      		$scope.gridCarterasEnTransito.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confComprasAPlazo = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_COMPRA_A_PLAZO', width: "60px"}
				 ,{field: "FolioCarteraOrigen", 		title: 'UI_FOLIO_CARTERA_ORIGEN', width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioAPlazo", 				title: 'UI_PRECIO_PLAZO',		width: "60px", template : " <label> #: util.bcs_formato(PrecioAPlazo,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								FolioCarteraOrigen 			: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								PrecioAPlazo				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridComprasAPlazo = grid;
	      		$scope.gridComprasAPlazo.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confVentasAPlazo = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_COMPRA_A_PLAZO', width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioAPlazo", 				title: 'UI_PRECIO_PLAZO',		width: "60px", template : " <label> #: util.bcs_formato(PrecioAPlazo,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								FolioCarteraOrigen 			: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								PrecioAPlazo				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridVentasAPlazo = grid;
	      		$scope.gridVentasAPlazo.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confEmprestitos = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_EMPRESTITO', width: "60px"}
				 ,{field: "FolioCarteraOrigen", 		title: 'UI_FOLIO_CARTERA_ORIGEN', width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioAPlazo", 				title: 'UI_PRECIO_PLAZO',		width: "60px", template : " <label> #: util.bcs_formato(PrecioAPlazo,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								FolioCarteraOrigen 			: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								PrecioAPlazo				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridEmprestitos = grid;
	      		$scope.gridEmprestitos.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confPrestamos = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioCartera", 				title: 'UI_FOLIO_EMPRESTITO', width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioAPlazo", 				title: 'UI_PRECIO_PLAZO',		width: "60px", template : " <label> #: util.bcs_formato(PrecioAPlazo,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioCartera 				: { type: "string"},
								FolioCarteraOrigen 			: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								PrecioAPlazo				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridPrestamos = grid;
	      		$scope.gridPrestamos.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	$scope.confGarantias = {
			contextMenu : true,
			filterable	: true,
			sortable 	: true,
			pageable 	: true,
			height 		: 150,
			autoBind 	: true,
			selectable 	: "none",
			excel 		: { allPages: true },
			columns : [
				 {field: "FolioGarantia", 				title: 'UI_FOLIO_GARANTIA', width: "60px"}
				 ,{field: "FolioCarteraOrigen", 		title: 'UI_FOLIO_CARTERA_ORIGEN', width: "60px"}
				 ,{field: "Cantidad", 					title: 'UI_CANTIDAD',			width: "60px", template : " <label> #: util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "PrecioCompra", 				title: 'UI_PRECIO_COMPRA',		width: "60px", template : " <label> #: util.bcs_formato(PrecioCompra,'GEN','Precio','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
				 ,{field: "MontoCarteraPrecioCompra", 	title: 'UI_MONTO',				width: "60px", template : " <label> #: util.bcs_formato(MontoCarteraPrecioCompra,'GEN','Monto','" + $scope.parametros.IdMonedaInstrumento + "','" + $scope.parametros.IdTipoInstrumento + "','').valor #</label>"}
			],
			dataSource : new bcs.DataSource({
	      		type : 'array',
	      		definition : {
					schema: {
						model : {
							fields : {
								FolioGarantia 				: { type: "string"},
								FolioCarteraOrigen 			: { type: "string"},
								Cantidad 					: { type: "number"},
								PrecioCompra 				: { type: "number"},
								MontoCarteraPrecioCompra 	: { type: "number"}
							}
						}
					}
				}
	      	}),
	      	
	      	gridCreated : function(grid) {
	      		$scope.gridGarantias = grid;
	      		$scope.gridGarantias.refresh();
	      		bcsComunes.ventanaTamanoAutomatico(winId);
			}
	}
	
	var consultarPrecioPromedio = function(){
		bcsComunes.ventanaOverlay(winId,true);
		
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'CUS',
				p_FechaBusqueda : $scope.parametros.FechaConsulta,
				p_Portafolios : [{
					IdPortafolio : $scope.parametros.IdPortafolio
				}],
				p_Instrumentos : [{
					IdInstrumento : $scope.parametros.IdInstrumento,
					IdBoveda : $scope.parametros.IdBoveda,
					IdClasificacionTipoInstrumento : $scope.parametros.IdClasificacionTipoInstrumento
				}]
		}
		
		bcsServiciosCustodia.ConsultarDetallePrecioPromedio(datos,
			function(respuesta){							
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					$scope.resultadoTotalizador = respuesta.P_OUT_TOTALIZADOR[0];
					$scope.resultadoPrecioPromedioActivo = respuesta.P_OUT_PRECIO_PROMEDIO_ACTIVO[0];
					$scope.confCarteraDisponible.dataSource.data(respuesta.P_OUT_CARTERAS);
					$scope.confCarterasEnTransito.dataSource.data(respuesta.P_OUT_CARTERAS_EN_TRANSITO);
					$scope.confComprasAPlazo.dataSource.data(respuesta.P_OUT_SIMULTANEAS_VENTA);
					$scope.confVentasAPlazo.dataSource.data(respuesta.P_OUT_SIMULTANEAS_COMPRA);
					$scope.confPrestamos.dataSource.data(respuesta.P_OUT_PRESTAMO);
					$scope.confEmprestitos.dataSource.data(respuesta.P_OUT_EMPRESTITO);
					$scope.confGarantias.dataSource.data(respuesta.P_OUT_GARANTIA);
				}
				bcsComunes.ventanaOverlay(winId,false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},winId);
				bcsComunes.ventanaOverlay(winId,false);		
			}
		);
	}
	
	consultarPrecioPromedio();
	
});