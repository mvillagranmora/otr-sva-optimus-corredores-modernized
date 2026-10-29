angular.module('ModuloPlataforma').controller('gBlotterVentana4Ctrl', function($scope, bcsComunes, $uibModal, $state, $timeout, $element) {

	//$scope.tabs = new bcs.test($timeout,1000,$scope,this);
	//$scope.tabs = new bcs.uibTab($timeout,1000);

	$scope.ventana4 = {};

	var ds0 = new bcs.DataSource({
		type : "odata",
		definition : {
			transport : {
				read : {
					url : "/BSCL/Orden/XSODATA/Ordenes.xsodata/Asignaciones",
					dataType : "json"
				}
			},
			pageSize : 100,
			serverPaging : true,
			serverSorting : true,
			schema : {
				model : {
					fields : {
						IdAsignacion :									{ type : "string" , editable : false },
						IdTransaccion :									{ type : "number" },
						IdMonedaInstrumento	:							{ type : "string" },
						IdOrden :										{ type : "number" },
						FechaAsignacion :								{ type : "sysdatetime" },
						FechaOperacionTransaccion :						{ type : "datetime" },
						FechaOperacionOrden :							{ type : "datetime" },
						PrecioAsignacion :								{ type : "number" },
						FechaLiquidacion :								{ type : "date" },
						FechaVencimiento :								{ type : "date" },
						CantidadAsignada :								{ type : "number" },
						MontoAsignado :									{ type : "number" },
						MontoAsignadoPlazo :							{ type : "number" },
						ValorRescateAsignado :							{ type : "number" },
						Instrumento :									{ type : "string" }/*,
						InstrumentoSubyacente :							{ type : "string" },
						TipoOperacionAccion :							{ type : "string" },
						TipoOperacion :									{ type : "string" },
						NombreTipoInstrumento :							{ type : "string" },
						NombreTipoInstrumentoSubyacente :				{ type : "string" },
						NombreClasificacionTipoInstrumento :			{ type : "string" },
						NombreClasificacionTipoInstrumentoSubyacente :	{ type : "string" },
						EstadoAsignacion :								{ type : "string" },
						EstadoFacturacion :								{ type : "string" },
						IdCliente :										{ type : "number" },
						RazonSocial :									{ type : "string" },
						IdPortafolioCliente :							{ type : "number" },
						EsAPlazo :										{ type : "string" },
						IdAsignacionRef :								{ type : "number" }*/
					}
				}
			}
		}
	});


	ds0.setOptions({
		type : "odata",
		definition : {
			transport : {
				read : {
					url : "/BSCL/Orden/XSODATA/Ordenes.xsodata/Asignaciones",
					dataType : "json"
				}
			},
			itemChange : function (e) {
				console.log(e);
			},
			pageSize : 100,
			serverPaging : true,
			serverSorting : true,
			schema : {
				model : {
					fields : {
						IdAsignacion :									{ type : "string" , editable : false },
						IdTransaccion :									{ type : "number" },
						IdMonedaInstrumento	:							{ type : "string" },
						IdOrden :										{ type : "number" },
						FechaAsignacion :								{ type : "sysdatetime" },
						FechaOperacionTransaccion :						{ type : "datetime" },
						FechaOperacionOrden :							{ type : "datetime" },
						PrecioAsignacion :								{ type : "number" },
						FechaLiquidacion :								{ type : "date" },
						FechaVencimiento :								{ type : "date" , editable : false  },
						CantidadAsignada :								{ type : "number" },
						MontoAsignado :									{ type : "number" },
						MontoAsignadoPlazo :							{ type : "number" },
						ValorRescateAsignado :							{ type : "number" },
						Instrumento :									{ type : "string" },
						InstrumentoSubyacente :							{ type : "string" },
						TipoOperacionAccion :							{ type : "string" },
						TipoOperacion :									{ type : "string" },
						NombreTipoInstrumento :							{ type : "string" },
						NombreTipoInstrumentoSubyacente :				{ type : "string" },
						NombreClasificacionTipoInstrumento :			{ type : "string" },
						NombreClasificacionTipoInstrumentoSubyacente :	{ type : "string" },
						EstadoAsignacion :								{ type : "string" },
						EstadoFacturacion :								{ type : "string" },
						IdCliente :										{ type : "number" },
						RazonSocial :									{ type : "string" },
						IdPortafolioCliente :							{ type : "number" },
						EsAPlazo :										{ type : "string" },
						IdAsignacionRef :								{ type : "number" }
					}
				}
			}
		}
	});
	ds0.read().then(function() {
		var view = ds0.view();
	//	console.log(view[0].IdTransaccion);
	});

	console.log(ds0);

	var mainGrid = {
		dataSource : ds0,
		scrollable : {
			virtual : true
		},
		sortable : true,
		pageSize : 20,
		pageable : true,
		height : 300,
		columns : [ {
			field : "IdAsignacion",
			title : "IdAsignacion",
			width : "120px"
		}, {
			field : "IdTransaccion",
			title : "IdTransaccion",
			width : "120px"
		}, {
			field : "MontoAsignadoPlazo",
			title : "MontoAsignadoPlazo"
		} ]
	};

	$scope.mainGridOptions = mainGrid;

	// $("#mainGrid").kendoGrid(mainGrid);

	// Configuración Básica grilla de órdenes

	var datosArray = [
		{ IdAsignacion: 163368463, IdTransaccion: 49656 },
		{ IdAsignacion: 163368464, IdTransaccion: 49639 },
		{ IdAsignacion: 163368465, IdTransaccion: 49640 },
		{ IdAsignacion: 163368466, IdTransaccion: 49650 },
		{ IdAsignacion: 163368467, IdTransaccion: 49649 },
		{ IdAsignacion: 163368468, IdTransaccion: 49641 },
		{ IdAsignacion: 163368469, IdTransaccion: 49642 },
		{ IdAsignacion: 163368470, IdTransaccion: 49643 },
		{ IdAsignacion: 163368471, IdTransaccion: 49644 },
		{ IdAsignacion: 163368472, IdTransaccion: 49645 },
		{ IdAsignacion: 163368473, IdTransaccion: 49646 },
		{ IdAsignacion: 163368474, IdTransaccion: 49647 },
		{ IdAsignacion: 163368475, IdTransaccion: 49648 },
		{ IdAsignacion: 163368476, IdTransaccion: 49651 },
		{ IdAsignacion: 163368477, IdTransaccion: 49651 },
		{ IdAsignacion: 163368504, IdTransaccion: 49647 },
		{ IdAsignacion: 163368505, IdTransaccion: 49648 },
		{ IdAsignacion: 163368506, IdTransaccion: 49651 },
		{ IdAsignacion: 163368507, IdTransaccion: 49651 },
		{ IdAsignacion: 163368508, IdTransaccion: 49659 },
		{ IdAsignacion: 163368509, IdTransaccion: 49659 },
		{ IdAsignacion: 163368510, IdTransaccion: 49659 },
		{ IdAsignacion: 163368511, IdTransaccion: 1 },
		{ IdAsignacion: 163428474, IdTransaccion: 4966277 }
	];

	var dsGrid = function() {
		return {
			type : "array",
			definition : {
				data : datosArray,
				schema : {
					model : {
						fields : {
							IdAsignacion :	{ type : "number" },
							IdTransaccion :	{ type : "number" }
						}
					}
				}
	  		}
		};
	};

	var ds1 = new bcs.DataSource(dsGrid());
	// var testData = ds1.data();
	//var ds2 = new bcs.DataSource({type : "call"});
 
	/*console.log(ds1);
	ds1.fetch(function() {
		var data = ds1.data();
		console.log(data);
	})*/
	var gridVentana4 = {};
	var gridInit = function(grid,ds0) {
		gridVentana4 = grid;
		grid.hideColumn(2);
		//grid.hideColumn(3);
		//grid.dataSource.read();
	}

	var grilla = $scope.confGridVentana4 = {
	//	dataSource : dsGrid(),
		dataSource : ds0,

		columns : [ {
			field : "IdAsignacion",
			title : "IdAsignacion",
			width : "70px"
		}, {
			field : "IdTransaccion",
			title : "IdTransaccion",
			width : "75px"
			,editable : true
		}, {
			field : "MontoAsignadoPlazo",
			title : "MontoAsignadoPlazo",
			width : "105px"
		}, {
			field : "FechaVencimiento",
			title : "FechaVencimiento (date)",
			width : "100px"
			//,template : "<label> #: util.formatoDate(FechaVencimiento) #</label>"
		}, {
			field : "FechaAsignacion",
			title : "FechaAsignacion (timestamp)"
			,width : "155px"
			,filterablesss : {
				//ui: "datetimepicker",
				cell: {
					//showOperators: false,
					//operator: "contains",
					//suggestionOperator: "contains",
					template: function (args) {
						args.element.kendoDateTimePicker({
						//format: "{0:dd MMM yyyy}",
						//change: function() {
						//var selection = moment(this.value()).format("DD MMM YYYY");
						// How do I filter the grid with the selection?
						//}
						});
					},
					//showOperators: false
				}
			}
			//,template : "<label> #: util.formatoSecondate(FechaAsignacion) #</label>"
		}, {
			field : "FechaOperacionTransaccion",
			title : "FechaOperacionTransaccion (secondate)",
			width : "155px"
			/*,filterables : {
				cell: {
					//showOperators: false,
					//operator: "contains",
					//suggestionOperator: "contains",
					template: function (args) {
						args.element.kendoDateTimePicker({
							format : "dd-MM-yyyy HH:mm:ss"
						});
					}
				}
			}*/
		}, {
			field : "EstadoAsignacion",
			title : "EstadoAsignacions",
			width : "135px"
		//	,editable : true
		}],
		contextMenu : true,
		filterable: {
			mode: "row"
		},
		sortable : true,
		pageable : true,
		height : 400,
		selectable : "multiple row",
		autoBind : false,
		navigatable: true,
		gridCreated : gridInit
	};

	$scope.ventana4.test = function() {
		ds0.read();
		//console.log($scope);
		//$scope.apiGridVentana4.dataSource.read();
		//alert(":P " + kendo.culture().name);

		//$scope.apiGridVentana2.select($scope.apiGridVentana2.tbody.find(">tr"));
	}

	$scope.ventana4.test2 = function() {
		//console.log($scope);
		//console.log(ds0);
		//console.log(gridVentana4);

		$scope.confGridVentana4.columns = [ {
			field : "IdAsignacion",
			title : "IdAsignacion nueva",
			width : "170px"
		}, {
			field : "IdTransaccion",
			title : "IdTransaccion nueva"
		//	,width : "175px"
		//	,editable : true
		}];
		//$scope.apiGridVentana4.dataSource.read();
		//alert(":P " + kendo.culture().name);

		//$scope.apiGridVentana2.select($scope.apiGridVentana2.tbody.find(">tr"));
	}

});
