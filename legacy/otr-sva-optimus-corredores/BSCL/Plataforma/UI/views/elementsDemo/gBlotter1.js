angular.module('ModuloPlataforma').controller('gBlotterVentana1Ctrl', function($scope, bcsComunes, $uibModal, $state, $timeout, $element) {

	//$scope.tabs = new bcs.test($timeout,1000,$scope,this);
	//$scope.tabs = new bcs.uibTab($timeout,1000);
	var mainGridOptions101;
	$scope.ventana1 = {};
	$scope.name = 'YO';
	//$scope.datos1 = {};
	//$scope.detail101 = {};
	//$scope.detail102 = {};
	//$scope.detail103 = {};

	var ds0_ = new bcs.DataSource({
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


	ds0_.setOptions({
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
			serverFiltering : true,
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
						,scopeId :								{ type : "number" }
					}
				}
			}
		}
	});

	var ds0 = new bcs.DataSource({
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
			serverFiltering : true,
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
						,scopeId :								{ type : "number" }
					}
				}
			}
		}
	});
	/*ds0.read().then(function() {
		var view = ds0.view();
	//	console.log(view[0].IdTransaccion);
	});*/

	var ds1def = {
		type : "odata",
		definition : {
			transport : {
				read : {
					url : "/BSCL/Orden/XSODATA/Ordenes.xsodata/CobrosAsignacion",
					dataType : "json"
				}
			},
			pageSize : 20,
			serverPaging : true,
			serverSorting : true,
			schema : {
				model : {
					fields : {
						IdCobroAsignacion :					{ type : "string" },
						IdAsignacion :						{ type : "number" },
						IdCobro 	:						{ type : "string" },
						FechaIngresoCobro  :				{ type : "date" },
						FechaIngresoRegistro  :				{ type : "sysdatetime" },
						IdPerfilCobro  :					{ type : "string" },
						FormaCobro  :						{ type : "string" },
						NombreFormaCobro  :					{ type : "string" },
						ValorCobroPerfil  :					{ type : "number" },
						ValorCobroAplicado  :				{ type : "number" },
						MontoCobroCalculado  :				{ type : "number" },
						MontoCobroCalculadoLocal  :			{ type : "number" },
						MontoCobroCalculadoLiquidacion :	{ type : "number" },
						CodigoCobroClasificacion  :			{ type : "string" },
						IdMonedaLiquidacionCobros :			{ type : "string" }
					}
				}
			}
		}
	};

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
	var gridVentana1 = {};
	var gridInit = function(grid,ds0) {
		gridVentana1 = grid;
		grid.hideColumn(2);
		//grid.hideColumn(3);
		//grid.dataSource.read();
	}

	$scope.confGridVentana1 = {
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


	$scope.innerGrid1Ventana1 = {
		//dataSource : new bcs.DataSource(ds1def),
		columns : [ {
			field : "IdCobroAsignacion",
			title : "IdCobroAsignacion",
			width : "70px"
		}, {
			field : "IdAsignacion",
			title : "IdAsignacion",
			width : "75px"
			,editable : true
		}, {
			field : "MontoCobroCalculado",
			title : "MontoCobroCalculado"
		}],
		sortable : true,
		pageable : true,
		height : 100,
		selectable : "multiple row",
		autoBind : false
	//	navigatable: true,
	//	gridCreated : gridInit
	};


	var detailGridOptions = {
		dataSource: new kendo.data.DataSource({
			type: "odata",
			transport: {
				read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
			},
			serverPaging: true,
			serverSorting: true,
			serverFiltering: true,
			pageSize: 5
			//  ,filter: { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID }
		}),
		scrollable: false,
		sortable: true,
		pageable: true,
		columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "106px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
		]
	};

	$scope.detailGridOptions = detailGridOptions;





	//$scope.apiGridVentana1.hideColumn(2);

	//$scope.apiGridVentana1.refresh();
	//ds1.read();

	$scope.ventana1.test = function() {
		var filters = [];						
		filters.push({ field: 'Country', operator: 'eq', value: 'UK' });
		filters.push({ field: 'City', operator: 'eq', value: 'London' });
	//	ds101.filters(filters);
		ds101.read();
		
	//	console.log(mainGridOptions101);
		//mainGridOptions101.dataSource.read();
		//console.log($scope);
		//$scope.apiGridVentana1.dataSource.read();
		//alert(":P " + kendo.culture().name);

		//$scope.apiGridVentana2.select($scope.apiGridVentana2.tbody.find(">tr"));
	}

	$scope.ventana1.test2 = function() {
		//mainGridOptions101;
		//console.log($scope);
		//console.log(ds0);
		//console.log(gridVentana1);
	//	console.log(mainGridOptions101.getConfiguration());
	//	console.log(JSON.stringify(mainGridOptions101.getConfiguration()));
		/*ds101Kendo.page(2);
		ds101Kendo.read().then(function() {
			
			console.log(ds101Kendo.data());
			//console.log(ds101Kendo.view());
			//fds.page(2);
		});*/

		var fds = mainGridOptions101.getFilteredDataSource(5);
		
		fds.fetchPage(1).then(function() {
			console.log(fds.totalPages());
			console.log(fds.data());
			console.log(fds.view());
		});

		fds.fetchPage(2).then(function() {
			console.log(fds.totalPages());
			console.log(fds.data());
			console.log(fds.view());
		});


/*
		fds.fetch().then(function() {
			fds.page(2);
			console.log(fds.view());
		});*/


		/*fds.query({
			filter: fds.filter(),
			pageSize:1,
			//page:2
		}).then(function(e) {
			//fds.page(2);
			console.log(fds.data());
		});*/


		/*mainGridOptions101.setConfiguration({  
			columns:[  
				{ field:"City", visible:true, locked:false, width:"120px" },
				{ field:"EmployeeID", visible:true, locked:false, width:"80px" },
				{ field:"LastName", visible:true, locked:false, width:"120px" },
				{ field:"Country", visible:true, locked:false, width:"120px" },
				{ field:"Title", visible:true, locked:true, width:198 },
				{ field:"FirstName", visible:false, locked:false, width:"120px" }
			],
			filter:{
				filters:[ { operator:"eq", value:"USA", field:"Country" } ],
				logic:"and"
			},
			sort:[ { field:"LastName", dir:"asc" } ]
		});

		ds101.read();*/

		/*$scope.confGridVentana1.columns = [ {
			field : "IdAsignacion",
			title : "IdAsignacion nueva",
			width : "170px"
		}, {
			field : "IdTransaccion",
			title : "IdTransaccion nueva"
		//	,width : "175px"
		//	,editable : true
		}];*/
		//$scope.apiGridVentana1.dataSource.read();
		//alert(":P " + kendo.culture().name);

		//$scope.apiGridVentana2.select($scope.apiGridVentana2.tbody.find(">tr"));
	}


	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	$scope.mainGridOptions100 = {
		dataSource: {
			type: "odata",
			transport: {
				read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
			},
			pageSize: 5,
			serverPaging: true,
			serverSorting: true
		},
		sortable: true,
		pageable: true,
		dataBound: function() {
			this.expandRow(this.tbody.find("tr.k-master-row").first());
		},
		columns: [{
			field: "FirstName",
			title: "First Name",
			width: "120px"
			},{
			field: "LastName",
			title: "Last Name",
			width: "120px"
			},{
			field: "Country",
			width: "120px"
			},{
			field: "City",
			width: "120px"
			},{
			field: "Title"
		}]
	};
	//$scope.grillaInterna = "una grilla Interna";
	//$scope.inner1 = {};
	//$scope.hola = "www";

	$scope.detailGridOptions100 = function(dataItem) {

	//	dataItem.shareScope = $scope.hola;



		return {
			dataSource: {
				type: "odata",
				transport: {
					read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
				},
				serverPaging: true,
				serverSorting: true,
				serverFiltering: true,
				pageSize: 5,
				filter: { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID }
			},
			scrollable: false,
			sortable: true,
			pageable: true,
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
			]
			,grillaInterna : "una grilla Interna"
		};
	};
	
	$scope.test100 = function(dataItem) {
		return {
			/*dataSource: {
				type: "odata",
				transport: {
					read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
				},
				serverPaging: true,
				serverSorting: true,
				serverFiltering: true,
				pageSize: 5
				//,filter: { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID }
			},*/
			scrollable: false,
			sortable: true,
			pageable: true,
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
			]
			,grillaInterna : "una grilla Interna"
		};
	};


	var ds101 = new bcs.DataSource({
		type: "odata",
		definition : {
			transport: {
				read: "http://demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
			},
			pageSize: 5,
			serverPaging: true,
			serverSorting: true,
			serverFiltering: true,
			schema : {
				model : {
					fields : {
						EmployeeID :		{ type : "number" },
						LastName :			{ type : "string" },
						FirstName :			{ type : "string" },
						Title :				{ type : "string" },
						TitleOfCourtesy :	{ type : "string" },
						BirthDate :			{ type : "date" },
						HireDate :			{ type : "date" },
						Address :			{ type : "string" },
						City :				{ type : "string" },
						Region 	:			{ type : "string" },
						PostalCode 	:		{ type : "string" },
						Country 	:		{ type : "string" },
						HomePhone 	:		{ type : "string" },
						Extension 	:		{ type : "string" },
						Notes 	:			{ type : "string" }
					}
				}
			},
			aggregate: [
				{ field: "EmployeeID", aggregate: "min" },
				{ field: "EmployeeID", aggregate: "max" }
			]
		}
	});

	var ds101Kendo = new kendo.data.DataSource({
		type: "odata",
		transport: {
			read: "http://demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
		},
		pageSize: 5,
		serverPaging: true,
		serverSorting: true,
		serverFiltering: true,
		schema : {
			model : {
				fields : {
					EmployeeID :		{ type : "number" },
					LastName :			{ type : "string" },
					FirstName :			{ type : "string" },
					Title :				{ type : "string" },
					TitleOfCourtesy :	{ type : "string" },
					BirthDate :			{ type : "date" },
					HireDate :			{ type : "date" },
					Address :			{ type : "string" },
					City :				{ type : "string" },
					Region 	:			{ type : "string" },
					PostalCode 	:		{ type : "string" },
					Country 	:		{ type : "string" },
					HomePhone 	:		{ type : "string" },
					Extension 	:		{ type : "string" },
					Notes 	:			{ type : "string" }
				}
			}
		},
		aggregate: [
			{ field: "EmployeeID", aggregate: "min" },
			{ field: "EmployeeID", aggregate: "max" }
		]
	});

	$scope.mainGridOptions101 = {
		dataSource: ds101,
		navigatable : true,
		contextMenu : true,
		height : 400,
		sortable: true,
		pageable: true,
		/*dataBound: function() {
			this.expandRow(this.tbody.find("tr.k-master-row").first());
		},*/
		selectable : "multiple cell",
		columns: [{
			field: "EmployeeID",
			template1: "<strong>#: EmployeeID # </strong>",
			template: function(dataItem) {
				console.log($scope.mainGridOptions101);
				return "<strong>" + kendo.htmlEncode(dataItem.EmployeeID) + "</strong>";
			},
			title: "EmployeeID",
			width: "80px",
			footerTemplate: "Min: #: min # Max: #: max #",
			footerAttributes: {
				"class": "table-footer-cell",
				style: "text-align: right; font-size: 10px"
			}
			},{
			field: "FirstName",
			title: "First Name",
			hidden: true,
			width: "120px"
			},{
			field: "LastName",
			title: "Last Name",
			editable: true,
			width: "120px"
			},{
			field: "Country",
			filterable1 : {
				cell: {
					template: function (args) {
					//	create a DropDownList of unique values (colors)
						args.element.kendoDropDownList({
							dataSource: args.dataSource,
							dataTextField: "Country",
							dataValueField: "Country",
							valuePrimitive: true
						});
					},
					showOperators: false
				}
			},
			width: "120px"
			},{
			field: "City",
			editable: true,
			width: "120px"
			},{
			field: "Title"
		}],
		filterable : true,
		gridCreated: function(grid) {
			mainGridOptions101 = grid;
			$scope.gridOptions101 = grid;
			return grid;
		},
		userSettings: true,
		groupable: true,
		notEditableColumns : function(row) {
			if ( row.City === 'Tacoma' ) {
				return ['City']; 
			}
			else if ( row.FirstName === 'Steven') {
				return ['LastName','City']; 
			}
		},
		hideDetailButton: function(row) {
			var hide = false
			if(row.Country === 'UK') 
				hide = true;
			return hide;
		},
		rowColorBackground: function(row) {
			var color = "";
			switch(row.get("City")) {
				case 'Seattle':
					color = "yellow";
					break;
				case 'London':
					color = "green";
					break;
				case 'Tacoma':
					color = "red";
					break;
			}
			return color;
		},
		/*dataBound : function(e) {
			console.log('dataBound');
			console.log(e);
		},*/
		detailInit : function(e) {
			console.log(e);
			//new dsJorgito(e.data);
		}
	};

	var detailDSOptions = function() {
		return {
			type: "odata",
			definition: {
				transport: {
					read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
				},
				serverPaging: true,
				//serverSorting: true,
				serverFiltering: true,
				pageSize: 5,
				schema : {
					model : {
						fields : {
							OrderID			:	{ type : "number" },
							CustomerID		:	{ type : "number" },
							EmployeeID		:	{ type : "number" },
							OrderDate		:	{ type : "date" },
							RequiredDate	:	{ type : "date" },
							ShippedDate		:	{ type : "date" },
							ShipVia			:	{ type : "number" },
							Freight			:	{ type : "number" },
							ShipName		:	{ type : "string" },
							ShipAddress		:	{ type : "string" },
							ShipCity		:	{ type : "string" },
							ShipRegion		:	{ type : "string" },
							ShipPostalCode	:	{ type : "string" },
							ShipCountry		:	{ type : "string" }
						}
					}
				},
				aggregate: [
					{ field: "EmployeeID", aggregate: "min" },
					{ field: "EmployeeID", aggregate: "max" }
				]
			//,filter: { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID }
			}
		};
	};

	$scope.eliminarVentana1Inner1 = function(item) {
		console.log(item);
		console.log(item.seleccionVentana1Inner1);
		console.log(item.seleccionVentana1Inner1Inner);
		console.log($scope);
	}

	$scope.detailGridOptions101 = function(dataItem) {
		var dsDef = detailDSOptions();
		dsDef.definition.filter = { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID };
		dsDef.definition.pageSize = 2000;
		return {
			dataSource: new bcs.DataSource(dsDef),
			height:200,
			autoBind:true,
			scrollable: false,
			sortable: true,
			pageable: true,
			selectable : "multiple row",
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address", width: "200px" },
			{ field: "ShipName", title: "Ship Name", width: "190px" },
			{ field: "Freight", title:"Freight", editable:true }
			]
		};
	};

	$scope.detailGridOptions102 = function(dataItem) {
		var dsDef = detailDSOptions();
		dsDef.definition.filter = [
			{ field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID },
			{ field: "ShipCountry", operator: "eq", value: dataItem.Country }
			];
		return {
			dataSource: new bcs.DataSource(dsDef),
			height:200,
			selectable : "multiple row",
			autoBind:true,
			scrollable: false,
			sortable: true,
			pageable: true,
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
			]
		};
	};

	$scope.data = {};
	$scope.data.data1 = {};
	$scope.data.data1.detailGridOptions103 = function(dataItem) {
		var dsDef = detailDSOptions();
		dsDef.definition.filter = [
			{ field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID },
			{ field: "ShipCountry", operator: "eq", value: dataItem.ShipCountry }
			];
		return {
			dataSource: new bcs.DataSource(dsDef),
			height:200,
			selectable : "multiple row",
			autoBind:true,
			scrollable: false,
			sortable: true,
			pageable: true,
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
			]
		};
	};
	
	$scope.test101 = function(dataItem) {
		return {
			/*dataSource: {
				type: "odata",
				transport: {
					read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
				},
				serverPaging: true,
				serverSorting: true,
				serverFiltering: true,
				pageSize: 5
				//,filter: { field: "EmployeeID", operator: "eq", value: dataItem.EmployeeID }
			},*/
			scrollable: false,
			sortable: true,
			pageable: true,
			columns: [
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address" },
			{ field: "ShipName", title: "Ship Name", width: "190px" }
			]
		};
	};

	$scope.onClick1 = function() {
		console.log('onClick1');
		//id de ventana -> scope.$parent.$parent.ventanaid;
		//var a = $scope.mainGridOptions101.gridCreated(grid);
		var id1 = bcsComunes.ventanaAbrir("bcsGridConf",{ confOptions: $scope.gridOptions101 },false,false,$scope.winId);

		bcsComunes.ventanaTitulo(id1,"Preferencias (grilla seleccionada)");
		bcsComunes.ventanaCambiarTamano(id1, bcsComunes.getAncho(id1), bcsComunes.getAlto(id1)+90);
		var promesa = bcsComunes.ventanaPromesa(id1);					
   		$.when(promesa).then(function(data) {
   			bcsComunes.msg("info","info", '' + data /*$translate.instant("UI_MENSAJE_DETALLE") + ' :'+ data*/);
      	});
      	
	}

});
