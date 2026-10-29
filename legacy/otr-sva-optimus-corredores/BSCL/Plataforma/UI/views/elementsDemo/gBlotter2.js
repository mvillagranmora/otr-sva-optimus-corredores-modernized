angular.module('ModuloOrdenes').controller('gBlotterVentana2Ctrl',function($scope,bcsComunes,$uibModal,$state){

	$scope.ventana2 = {};

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
							Freight			:	{ type : "number", excelFormat: "#,##0.000" },
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

	var ds201 = new bcs.DataSource(detailDSOptions());

	$scope.mainGridOptions201 = {
		filterable: true,
		dataSource: ds201,
		height:420,
		autoBind:false,
		scrollable: false,
		sortable: true,
		pageable: true,
		selectable : "multiple row",
		userSettings : true,
		navigatable : true,
		allowCopy: { delimiter : "\t" },
		columns: [
			{ field: "EmployeeID", title_:"EmpID", width: "100px" },
			{ field: "OrderID", title:"ID", width: "96px" },
			{ field: "ShipCountry", title:"Ship Country", width: "110px",
			groupable: true,
			filterable : {
							cell: {
								operator: "contains",
								template: function (args) {
									var options = {	mask: "00:00" };
									//bcs.MaskedTextBox(args, options);
									bcs.MaskedTextBox(args);
								}
							}
						} 
			},
			{ field: "ShipAddress", title:"Ship Address", width: "200px" },
			{ field: "ShipName", title: "Ship Name", width: "190px", editable: true },
			{ 
				field: "Freight",
			//	editable: true,
				title: "Freight"
			}
		],
		gridCreated: function(g) {
			console.log(g);
		}
	};

	$scope.ventana2.cargar = function() {
		ds201.read();
	}



	/*
	var mainGrid = {
		dataSource: {
			type: "odata",
			transport: {
				read: {
					url : "/BSCL/Orden/XSODATA/Ordenes.xsodata/Asignaciones",
					dataType: "json"
				}
			},
			pageSize: 100,
			serverPaging: true,
			serverSorting: true,
			schema: {
				model: {
					fields : {
						IdAsignacion : { type: "number"},
						IdTransaccion : { type: "number"},
						IdMonedaInstrumento : { type: "string"},
						IdOrden : { type: "number"},
						FechaAsignacion : { type: "date"},
						FechaOperacionTransaccion : { type: "date"},
						FechaOperacionOrden : { type: "date"},
						PrecioAsignacion : { type: "number"},
						FechaLiquidacion : { type: "date"},
						FechaVencimiento : { type: "date"},
						CantidadAsignada: { type: "number"},
						MontoAsignado: { type: "number"},
						MontoAsignadoPlazo: { type: "number"},
						ValorRescateAsignado: { type: "number"},
						Instrumento: { type: "string"},
						InstrumentoSubyacente: { type: "string"},
						TipoOperacionAccion : {type : "string"},
						TipoOperacion : {type : "string"},
						NombreTipoInstrumento: { type: "string"},
						NombreTipoInstrumentoSubyacente: { type: "string"},
						NombreClasificacionTipoInstrumento: { type: "string"},
						NombreClasificacionTipoInstrumentoSubyacente: { type: "string"},
						EstadoAsignacion: { type: "string"},
						EstadoFacturacion: { type: "string"},
						IdCliente: { type: "number"},
						RazonSocial: { type: "string"},
						IdPortafolioCliente: { type: "number"},
						EsAPlazo : { type: "string"},
						IdAsignacionRef: { type: "number"}
					}
				}
			}
		},
		scrollable: {
        	virtual: true
        },
		sortable: true,
		pageSize: 20,
		pageable: true,
		height: 300,
		columns: [{
			field: "IdAsignacion",
			title: "IdAsignacion",
			width: "120px"
		},{
			field: "IdTransaccion",
			title: "IdTransaccion"

			
		}]
	};

	$scope.mainGridOptions = mainGrid;

	//$("#mainGrid").kendoGrid(mainGrid);



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
				{ IdAsignacion: 163368478, IdTransaccion: 49656 },
				{ IdAsignacion: 163368479, IdTransaccion: 49639 },
				{ IdAsignacion: 163368480, IdTransaccion: 49640 },
				{ IdAsignacion: 163368481, IdTransaccion: 49650 },
				{ IdAsignacion: 163368482, IdTransaccion: 49649 },
				{ IdAsignacion: 163368483, IdTransaccion: 49641 },
				{ IdAsignacion: 163368484, IdTransaccion: 49642 },
				{ IdAsignacion: 163368485, IdTransaccion: 49643 },
				{ IdAsignacion: 163368486, IdTransaccion: 49644 },
				{ IdAsignacion: 163368487, IdTransaccion: 49645 },
				{ IdAsignacion: 163368488, IdTransaccion: 49646 },
				{ IdAsignacion: 163368489, IdTransaccion: 49647 },
				{ IdAsignacion: 163368490, IdTransaccion: 49648 },
				{ IdAsignacion: 163368491, IdTransaccion: 49651 },
				{ IdAsignacion: 163368492, IdTransaccion: 49651 },
				{ IdAsignacion: 163368493, IdTransaccion: 49656 },
				{ IdAsignacion: 163368494, IdTransaccion: 49639 },
				{ IdAsignacion: 163368495, IdTransaccion: 49640 },
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

var grilla = $scope.confGridVentana2 = {
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

//grilla.dataSource.definition.data = datosArray;

$scope.ventana2.test = function() {
	alert(":) " + kendo.culture().name);
	
	
			//$scope.apiGridVentana2.select($scope.apiGridVentana2.tbody.find(">tr"));
}
*/


});
