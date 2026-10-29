angular.module('ModuloOrdenes').controller('gBlotterVentana3Ctrl',function($scope,bcsComunes,$uibModal,$state,$timeout,$element){
	
	$scope.ventana3 = {};

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
				pageSize: 500,
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
				}
			}
		};
	};

	var ds301_ = new bcs.DataSource(detailDSOptions());

	$scope.mainGridOptions301_ = {
		dataSource: ds301,
		height:420,
		autoBind:false,
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
			{ 
				field: "Freight",
			//	editable: true,
				title: "Freight"
			}
		]
	};

	var dsOptions301 = function(){
		return {
			type: "odata",
			transport: {
				read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Orders"
			},
			serverPaging: true,
			//serverSorting: true,
			serverFiltering: true,
			pageSize: 500,
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
			}
		}
	};

	var ds301 = new kendo.data.DataSource(dsOptions301());


	$scope.mainGridOptions301 = {
	//	dataSource: ds301,
		dataSource: dsOptions301(),
		height:420,
		autoBind:false,
		//scrollable: false,
		sortable: true,
		pageable: true,
		selectable : "multiple row",
		columns: [
			{ field: "OrderID", title: "Order ID", width: 60 },
			{ field: "CustomerID", title: "Customer ID", width: 90},
			{ field: "ShipName", title:"Ship Name", width: 220 },
			{ field: "ShipAddress", title:"Ship Address", width: 280 },
			{ field: "ShipCity", title:"Ship City", width: 110 },
			{ field: "ShipCountry", title:"Ship Country", width: 110 },
			{ field: "CustomerID", title: "Customer ID", width: 90},
			{ field: "ShipName", title:"Ship Name", width: 220 },
			{ field: "ShipAddress", title:"Ship Address", width: 280 },
			{ field: "ShipCity", title:"Ship City", width: 110 },
			{ field: "ShipCountry", title:"Ship Country", width: 110 } ,     
			{ field: "CustomerID", title: "Customer ID", width: 90},
			{ field: "ShipName", title:"Ship Name", width: 220 },
			{ field: "ShipAddress", title:"Ship Address", width: 280 },
			{ field: "ShipCity", title:"Ship City", width: 110 },
			{ field: "ShipCountry", title:"Ship Country", width: 110 },
			{ field: "CustomerID", title: "Customer ID", width: 90},
			{ field: "ShipName", title:"Ship Name", width: 220 },
			{ field: "ShipAddress", title:"Ship Address", width: 280 },
			{ field: "ShipCity", title:"Ship City", width: 110 },
			{ field: "ShipCountry", title:"Ship Country", width: 110 }
			/* ,
			{ field: "EmployeeID", title:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px" },
			{ field: "ShipAddress", title:"Ship Address", width: "200px" },
			{ field: "ShipName", title: "Ship Name", width: "190px" },
			{ 
				field: "Freight",
			//	editable: true,
				title: "Freight"
			}*/
		]
	};

	$scope.$on("kendoWidgetCreated", function (event, widget) {

			if ( widget.options.name.toLowerCase() === "grid" ) {
				widget.refresh();
				//apiGrid(scope, widget.element);
			}
	});

	$scope.ventana3.cargar = function() {
		$scope.grid3.dataSource.read();
	}

	$scope.ventana3.exportarExcel = function(filename) {
		$scope.grid3.saveAsExcel();
	}






/*
	$scope.innerTabs = new bcs.uibTab($timeout,1000,$scope,$element);
	$scope.test = function ($event) {
		$event.preventDefault();
	};

	var innerFilters = [];

  	var fnColorBk = function(row) {
        var color = "";

  		switch(row.get("Country")) {
            case 'UK':
                color = "red";
                break;
            case 'CL':
                color = "green";
                break;
            case 'USA':
                color = "yellow";
                break;
        }

        return color;
  	};

    var detailInit1 = function(e) {
		return  [
			{
				widget : "kendoTabStrip",
				htmlElement : "#tabstrip-grid3",
				attributes : {
					animation: {
						open: { effects: "fadeIn" }
					}
				}
			}
		]
	};


  	// Definición del modelo
  	// Los tipos de datos definidos son: string, date, number.
    var modeloEmpleado = function() {
  		return {
  			model: {
  				fields : {
            FirstName : { type : "string", editable : false },
            LastName : { type : "string" },
            Country : { type : "string" },
            City : { type : "string" },
            Title : { type : "string" },
            Test :  { type : "string" }
  				}
  			}
  		};
  	};

  	// Configuración del transporte del dataSource
  	var transporteEmpleado = function() {
  		return {
  			read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
  		};
  	};
  	// Definción del tipo OData para el dataSource
		var definicionOData = function() {
			return {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging : true, // Paginado de los datos en el servidor.
				pageSize : 10, // Número de registros por página.
				transport: transporteEmpleado(),
				schema : modeloEmpleado(),
				requestEnd : function(e) {
					console.log("requestEnd> ");
					console.log(e);
				},
				filtersFn : function (row) {
					return { field: "Country", operator: "eq", value: row.Country };
				}
			};
		};

  	// Definición del dataSource
  	var dataSourceEmpleado = function() {
  		return {
  			type : "odata", //Los tipos posibles son: odata, call, array y object
  			definition : definicionOData()
  		};
  	};
  	  	// Definición del dataSource
  	var dataSourceEmpleado0 = function() {
  		return {
  			type : "odata", //Los tipos posibles son: odata, call, array y object
  			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging : true, // Paginado de los datos en el servidor.
				pageSize : 10, // Número de registros por página.
				transport: transporteEmpleado(),
				schema : modeloEmpleado(),
				requestEnd : function(e) {
					console.log("requestEnd> ");
					console.log(e);
				}
			}
  		};
  	};

		var columnasEmpleado = function() {
			return  [{
				field: "FirstName",
				title: "First Name",
				width: "120px",
				editable : false
			},{
				field: "LastName",
				title: "Last Name",
				width: "120px",
				editable : true
			},{
				field: "Country",
				width: "120px",
				groupable : true
			},{
				field: "City",
				width: "120px"
			},{
				field: "Title"
			},{
				field: "Test",
				editable : true
			}];
		};

		var modeloOrdenCall = function() {
			return {
				model: {
					fields : {
						IdOrden : { type : "number" },
						IdInstrumento : { type : "number" },
						EstadoOrdenAsignacion : { type : "string" }
					}
				}
			};
		};

		var transporteOrdenCall = function() {
			return {
				read: {
					//url :	"/BSCL/Orden/XSODATA/Ordenes.xsodata/Ordenes",
					url :	"/BSCL/Plataforma/XSREST/callOData.xsjs?t=18",
					dataType : "json"
				}
			};
		};

		var definicionODataCall = function() {
			return {
				
			serverFiltering : true, // Filtrado de datos en el servidor.
			serverPaging : true, // Paginado de los datos en el servidor.
			//serverSorting : true, // Ordenamiento de los datos en el servidor.
			//sort : { field: "IdOrden", dir: "desc" }, // Criterios de Ordenamiento.
			pageSize : 10, // Número de registros por página.
			transport: transporteOrdenCall(),
			schema : modeloOrdenCall()
		};
	};
	$scope.fnTestClick = function(a) {
		console.log(a);
	}

    var dataSourceOrdenCall = function() {
  		return {
  			type : "odata", //Los tipos posibles son: odata, call, array y object
  			definition : definicionODataCall()
  		};
  	};

    var columnasOrdenCall = function() {
  		return  [{
				field: "IdOrden",
				title: "Id orden",
				width: "120px"
			},{
				field: "IdInstrumento",
				title: "Id Instrumento",
				width: "120px"
			},{
				field: "EstadoOrdenAsignacion",
        title : "Orden Asignacion"
			}];
    };

    //var test = $scope.apiGrid.details().data.City;
    var detailFilters = function (row) {
      return { field: "City", operator: "eq", value: row.City };
    };

    filters1 = {};

    var detailFilters1 = function (filters1, row) {
      return { field: "City", operator: "eq", value: row.City };
    };



  	// Configuración Básica grilla de órdenes
  	var grilla = $scope.confBasica = {
  		dataSource: dataSourceEmpleado(),
  		columns: columnasEmpleado(),
  		rowColorBackground : fnColorBk,
  		sortable: true,
  		pageable: false,
  		height: 600,
  		selectable : "multiple row",
  		//filterable : true,
  		filterable : {
  			mode : "menu, row",
			extra: false, //do not show extra filters
			operators: { // redefine the string operators
				string: {
					contains: "Contains",
					startswith: "Starts With",
					eq: "Is Equal To"
				}
			}
		},
  		//editable : true,
  		autoBind : true,

      //detailFilters : detailFilters1

      //detailTemplate: kendo.template($("#template-grid3").html()),
      //detailTemplate: $("#template-grid33"),
		//detailInit : detailInit1
  	};

  	$scope.detailGridOptions0 = {
  		dataSource: new bcs.DataSource(dataSourceEmpleado0()),
  		columns: columnasEmpleado(),
  		sortable: true,
  		pageable: false,
  		height: 200,
  		selectable : "multiple row",
  		//autoBind : true,
  		gridCreated : function(grid,grid2) {
  			var f = { field: 'Country', operator: 'eq', value: 'UK' };
  			console.log(f);
  			
  			grid2.dataSource.filter(f);
  			console.log(grid2.dataSource.filter());
  		}
  	};

  	$scope.testDetail = function(a) {
  		innerFilters = [{ field: "Country", operator: "eq", value: a.data.Country }];
  		console.log(a);
  		console.log($scope.confGrid3Interna);
  		//$scope.confGrid3Interna.columns= [];
  		console.log($scope.confGrid3Interna);
  		//grilla.colums = {};
  	};


		var grilla2 = $scope.confGrid3Interna = {
			dataSource: {
				type : "call", //Los tipos posibles son: odata, call, array y object
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging : true, // Paginado de los datos en el servidor.
					pageSize : 10, // Número de registros por página.
					callParameters : {
						p_FechaEfectivaMovimientoDesde: "2016-01-05",
						p_FechaEfectivaMovimientoHasta: "2016-01-13",
						p_IdCliente: null,
						p_IdCuentaInversion: "42565",
						p_IdEmpresa: 1,
						p_IdEstado: null,
						p_IdModulo: "CTA",
						p_IdMoneda: null,
						p_IdPortafolio: null,
						p_IdUsuario: 3,
						p_UsaReglasNegocio: "N"
					},
					callTransaction : "BSCL.CuentaInversion.BD.Servicios%3A%3AConsultarCartolaDeMovimiento",
					callResultSet : "P_OUT_CARTOLADEMOVIMIENTO",
					
					schema : {
						model: {
							fields : {
								IdTipoProducto : {type : "string"},
								IdTipoOperacion : {type : "string"},
								FechaOperacion : {type : "date"},
								IdOperacion : {type : "number"}
							}
						}
					},
					
				}
			},
			columns:  [{
				field: "IdTipoProducto",
				title: "IdTipoProducto",
				width: "120px",
				editable : false
			},{
				field: "IdTipoOperacion",
				title: "IdTipoOperacion",
				width: "120px",
				editable : true
			},{
				field: "FechaOperacion",
				width: "120px",
				groupable : true
			},{
				field: "FechaOperacion"
			}],
			sortable: true,
			pageable: true,
			height: 300,
			selectable : "multiple row",
			autoBind : true
		};

    var grilla3Call = $scope.confGrid3InternaCall = {
  		dataSource: dataSourceOrdenCall(),
  		columns: columnasOrdenCall(),
  		sortable: true,
  		pageable: true,
  		height: 300,
  		selectable : "multiple row",
  		autoBind : true
  	};

    var grilla3 = $scope.confGrid3Interna2 = {
      dataSource: dataSourceEmpleado(),
      columns: columnasEmpleado(),
      rowColorBackground : fnColorBk,
      sortable: true,
      pageable: true,
      height: 300,
      selectable : "multiple row",
      autoBind : true
      //detailTemplate: kendo.template($("#template-grid3").html()),
      //detailTemplate: $("#template-grid33"),
    //detailInit : detailInit1
    };

    var grilla4 = $scope.confGrid3Interna3 = {
      dataSource: dataSourceEmpleado(),
      columns: columnasEmpleado(),
      rowColorBackground : fnColorBk,
      sortable: true,
      pageable: true,
      height: 300,
      selectable : "multiple row",
      autoBind : true
      //detailTemplate: kendo.template($("#template-grid3").html()),
      //detailTemplate: $("#template-grid33"),
    //detailInit : detailInit1
    };



	var datosArray = [
				{ IdAsignacion: 163368463, IdTransaccion: 49656 },
				{ IdAsignacion: 163368464, IdTransaccion: 49639 },
				{ IdAsignacion: 163368465, IdTransaccion: 49640 },
				{ IdAsignacion: 163368466, IdTransaccion: 49650 },
				{ IdAsignacion: 163368467, IdTransaccion: 49649 },
				{ IdAsignacion: 163368468, IdTransaccion: 49641 },
				{ IdAsignacion: 163368469, IdTransaccion: 49642 },
				{ IdAsignacion: 163368496, IdTransaccion: 49650 },
				{ IdAsignacion: 163368497, IdTransaccion: 49649 },
				{ IdAsignacion: 163368498, IdTransaccion: 49641 },
				{ IdAsignacion: 163368499, IdTransaccion: 49642 },
				{ IdAsignacion: 163368500, IdTransaccion: 49643 },
				{ IdAsignacion: 163368501, IdTransaccion: 49644 },
				{ IdAsignacion: 163368502, IdTransaccion: 49645 },
				{ IdAsignacion: 163368503, IdTransaccion: 49646 },
				{ IdAsignacion: 163368504, IdTransaccion: 49647 },
				{ IdAsignacion: 163368505, IdTransaccion: 49648 },
				{ IdAsignacion: 163368506, IdTransaccion: 49651 },
				{ IdAsignacion: 163368507, IdTransaccion: 49651 },
				{ IdAsignacion: 163368508, IdTransaccion: 49659 },
				{ IdAsignacion: 163368509, IdTransaccion: 49659 },
				{ IdAsignacion: 163368510, IdTransaccion: 49659 },
				{ IdAsignacion: 163368511, IdTransaccion: 1 },
				{ IdAsignacion: 163428474, IdTransaccion: 4966277 }];

$scope.detailGridOptions2 = {
	dataSource: {
		type: "array",
		definition : {
			data: datosArray,
			schema : {
				model: {
					fields : {
						IdAsignacion : {type : "number"},
						IdTransaccion : {type : "number"}
					}
				}
			}
		}
	},
	columns: [{
		field: "IdAsignacion",
		title: "IdAsignacion",
		width: "120px"
	},{
		field: "IdTransaccion",
		title: "IdTransaccion",
		editable: true,
		editableFn: function(model) {
			var editable = false;
			if (model.IdTransaccion !== 1 ) {
				editable = true;
			}
			return editable;

		},
		editor: function(container, options) {
			if (options.model.IdTransaccion === 1 ) {
				$('<input class="k-textbox" data-bind="value:' + options.field + '"/>').appendTo(container);
			} 
			else {
				$('<label>' + options.model.IdTransaccion + '</label>').appendTo(container);
			}
		}
	},{
		title: "test",
		columns: [{
			field: "Instrumento"
		},{ field: "TipoOperacionAccion"
		}]
	}],
	notEditableColumns_off: function(row) {
		console.log(row.get("IdTransaccion"));
		if (row.get("IdTransaccion") !== 1 ) {
			return ["IdAsignacion","IdTransaccion"];
		}
		
	},
	contextMenu: true,
	sortable: true,
	pageable: false,
	height: 400,
	selectable : "multiple row",
	autoBind : true,
};



    $scope.user = {
       title:     "Technical Program Manager",
       email:     "ipsum@lorem.com",
       firstName: "Naomi",
       lastName:  "" ,
       company:   "Google" ,
       address:   "1600 Amphitheatre Pkwy" ,
       city:      "Mountain View" ,
       state:     "CA" ,
       country:   "USA" ,
       postalCode : "94043"
    };

    $scope.notified = true;

    $scope.buscarTest = function(filtros)
  	{
  		console.log($scope);
  		//$scope.apiGrid3InternaCall.dataSource.read();
  		//$scope.apiGrid3Interna.dataSource.read();
  		var ds = $scope.apiGrid.dataSource.data()
		var fieldFormat = [{field: "BirthDate", format: function(v) { return moment(v).utc().format('YYYY-MM-DD')}},{field: "Country", format: ""}];
		var dsf = $scope.apiGrid.dataSource.formattedData(ds,fieldFormat);
		//$scope.confBasica.columns= [];
		$scope.confBasica.pageable = true;
		$scope.apiGrid.dataSource.read();
		//$scope.apiGrid.refresh();
  	};


    $scope.gridDataSource = {
      type: "odata",
      transport: {
        read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
      },
      pageSize: 5,
      serverPaging: true,
      serverSorting: true
    };

    var mainGrid = $scope.mainGridOptions = {
      sortable: true,
      pageable: true,
      columns: [{
        field: "FirstName",
        title: "First Name",
        width: "120px"
      },{
        field: "LastName",
        title: "Last Name",
        width: "120px"
      }]
    };

    $scope.detailGridOptions = {
      scrollable: false,
      sortable: true,
      dataSource: $scope.gridDataSource,
      pageable: true,
      columns: [
        { field: "City", title:"City", width: "56px" },
        { field: "Address", title:"Address", width: "110px" }
      ]
    };

*/
});
