angular.module('ModuloPlataforma').controller('ConfiguracionFlujoCustodiaCtrl',
function($scope, bcsComunes, $compile, $translate, $resource,bcsServiciosConfigFlujoCustodia, bcsServicioPlataforma) {
	
	// Funcion que filtra un arreglo hacia un arreglo con elementos unicos (elimina duplicaciones de un mismo elemento).
    function uniqueArray(ar) {
		var j = {};
	
		ar.forEach(function(v) {
		    j[v + '::' + typeof v] = v;
		});
	
		return Object.keys(j).map(function(v) {
		    return j[v];
		});
    }

    // Funcion que determina el largo de una pestaña tab.
    function largoTab(tab) {
		var b = Math.round((tab.length * 7.4));
		b = b + '' + 'px';
		return b;
    }

    // Funcion que verifica si el "datasource" ha sido modificado de su forma original (recibida).
    function hasBeenChanged(cFC) {
		for (j = 0; j < cFC.gridFlujoCustodia.dataSource.data().length; j++) {
		    for (i = 1; i < cFC.gridFlujoCustodia.columns.length; i++) {
				if (cFC.gridFlujoCustodia.dataSource.data()[j][cFC.gridFlujoCustodia.columns[i].field] != cFC.gridFlujoCustodia.dataSource.ds._pristineData[j][cFC.gridFlujoCustodia.columns[i].field]) {
				    return true;
				}
		    }
		}
		return false;
    }

    // Funcion que devuelve una lista de 3-tupla ("IdUsoFlujoCustodia","ItemCalculo" y "FuncionOperador") dado una ConfiguracionFlujoCustodia; el objeto de esta clase.
    function tripletaIdItemOperacion(sol) {
		var algo = [];
		for (j = 0; j < sol.gridFlujoCustodia.dataSource.data().length; j++) {
		    for (i = 1; i < sol.gridFlujoCustodia.columns.length; i++) {
		    	algo.push({
				    Item : sol.gridFlujoCustodia.dataSource.data()[j].Item,
				    IdUsoFlujoCustodia : tkm[sol.gridFlujoCustodia.columns[i].field],
				    FuncionOperador : sol.gridFlujoCustodia.dataSource.data()[j][sol.gridFlujoCustodia.columns[i].field]
				});
		    }
		}
		return algo;
    }

    var tkm = {};
    var overlay = bcsComunes.overlay;
    var idVentana = bcsComunes.ventanaWinId($scope);
    
    var cFlujoCustodia = this;
    cFlujoCustodia.idVentana = bcsComunes.ventanaWinId($scope);
    cFlujoCustodia.parametros = bcsComunes.ventanaParams($scope.idVentana);
    
    var dataSourceGrid = {
		type : "array",
		definition : {
		    serverSorting : false,
		    data : [],
		    schema : {
		    	model : {
		    		fields : {
		    			A: {type : "string"}
		    		}
		    	}
		    }
		}
    };

    cFlujoCustodia.gridFlujoCustodia = {
		dataSource : new bcs.DataSource(dataSourceGrid),
		columns : [
			{field : "A", title : "A", width : "100px"}
		],
		gridCreated : function(grid) {
		    $scope.gridApi = grid;
		},
		sortable : true,
		height : 200,
		userSettings : true,
		navigatable : true,
		excel : {
		    allPages : true,
		},
		selectable : "multiple row",
		autoBind : true
    };
    
    var dsFlujoCustodia = {
		type : "odata",
		definition : {
		    serverFiltering : true,
		    serverPaging : false,
		    pageSize : 200,
		    serverSorting : true,
		    transport : {
				read : {
				    url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=70124",
				    dataType : "json"
				}
		    },
		    schema : {
		    	model : {
		    		fields : {
		    			IdUsoFlujoCustodia : {type : "number"},
		    			IdEmpresa : {type : "number"},
		    			UsoTipoOperacion : {type : "string"},
		    			IdEstadoUso : {type : "string"},
		    			ItemCalculo : {type : "string"},
		    			ItemOrigen : {type : "string"},
		    			Descripcion : {type : "string"},
		    			FuncionOperador : {type : "string"},
		    			IdEstadoItem : {type : "string"},
		    			EstadoUso : {type : "string"},
		    			EstadoItem : {type : "string"},
		    			NombreTipoOperacion : {type : "string"}
		    		}
		    	}
		    }
		}
    };
    
	dsFlujoCustodia = new bcs.DataSource(dsFlujoCustodia);
	dsFlujoCustodia.fetch().then(function() {
		var data = dsFlujoCustodia.data();
		var columns = [];
		var FIELDSXX = {};
		var datos = [];
		var items = [];

		// Extraccion de nombres de columnas y de la columna entera "Item". Y asociacion del "IdUsoFlujoCustodia" con cada "NombreTipoOperacion".
		for (i = 0; i < data.length; i++) {
		    tkm[data[i]["NombreTipoOperacion"]] = data[i]["IdUsoFlujoCustodia"];
		    columns.push(data[i]["NombreTipoOperacion"]);
		    items.push(data[i]["ItemCalculo"]);
		}

		// Unicidad de valores, columnas y la columna "Item" con sus valores.
		columns = uniqueArray(columns);
		items = uniqueArray(items);

		// La primera columna es siempre "Item".
		FIELDSXX['Item'] = {type : 'string'};

		// Se añaden las columnas a "columns".
		for (i = 0; i < columns.length; i++) {
		    columns[i] = ({field : columns[i], title : columns[i], width : largoTab(columns[i]), editable : true});
		    FIELDSXX[columns[i].field] = {type : 'string', editable : true};
		}

		// La primera columna es siempre "Item".
		columns.unshift({field : "Item", title : "Item", width : "200px", locked: true});

		// Datos de la columna "Item" ("ItemCalculo").
		for (i = 0; i < items.length; i++) {
		    datos[i] = {Item : items[i]};
		}

		// Aqui se extraen los datos de "UsoFlujoCustodia" para luego pivotearlos
		for (i = 1; i < columns.length; i++) {
		    for (j = 0; j < items.length; j++) {
				for (k = 0; k < data.length; k++) {
				    if (columns[i].field == data[k].NombreTipoOperacion && items[j] == data[k].ItemCalculo) {
				    	datos[j][columns[i].field] = data[k].FuncionOperador;
				    } else if (!(datos[j].hasOwnProperty(columns[i].field))) {
				    	datos[j][columns[i].field] = '0';
				    }
				}
		    }
		}

		dataSourceGrid = {
		    type : "array",
		    definition : {
				serverSorting : false,
				data : datos,
				schema : {
				    model : {
				    	fields : FIELDSXX
				    }
				}
		    }
		};
		
		const ordenColumnas = {
			'Item': 1,
			'VENDES_': 2,
			'VENACN_VENTA_ACCIONES_NACIONALES': 3,
			'VENACX_VENTA_ACCIONES_EXTRANJERAS': 4,
			'RESCFM_RESCATE_FONDO_MUTUO': 5,
			'RETGTA_RETIRO_DE_GARANTIA': 6,
			'VENPAC_VENTA_CON_PACTO': 7,
			'FLJORD_': 8,
			'CARDIS_': 9
		};
		
		columns.sort((a, b) => {
		  const valorA = a.field;
		  const valorB = b.field;
		  
		  const ordenA = ordenColumnas[valorA] || 99;
		  const ordenB = ordenColumnas[valorB] || 99;
		
		  return ordenA - ordenB;
		});
	
		cFlujoCustodia.gridFlujoCustodia = {
		    dataSource : new bcs.DataSource(dataSourceGrid),
		    gridCreated : function(grid) {
		    	$scope.gridApi = grid;
		    },
		    columns : columns,
		    sortable : true,
		    height : 600,
		    userSettings : true,
		    navigatable : true,
		    excel : {
		    	allPages : true,
		    },
		    selectable : "multiple row",
		    autoBind : true
		};

		$scope.gridApi.setOptions(cFlujoCustodia.gridFlujoCustodia);

	},function() {
		bcsComunes.msg("error", "error","UI_ERROR_ACTUALIZACION_FLUJO_CUSTODIA");
		bcsComunes.ventanaOverlay($scope.idVentana, false);
	});

    // '+', '-' o '0' son los posibles valores en la grilla ingresar otro valor no es posible.
    $scope.cFlujoCustodia.gridFlujoCustodia.save = function(e) {
		// console.log(e);
		let a = Object.keys(e.values)[0];
		if (!([ '+', '-', '0' ].includes(e.values[a]))) {
		    bcsComunes.msg("error", "UI_ERROR","UI_VALOR_NO_ESPERADO");
		    e.preventDefault();
		} else {
		    console.log(cFlujoCustodia);
		}
    };

    // Graba los cambios de configuracion flujo custodia
    $scope.GrabarConfigurafionFlujoCustodia = function() {
    	// Verifica si hubo cambios en la grilla respecto a la grilla inicial
    	if (hasBeenChanged(cFlujoCustodia)) {
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'N',
				p_ConfiguracionFlujoCustodia : tripletaIdItemOperacion(cFlujoCustodia)
			};

		    // Promesa
		    bcsServiciosConfigFlujoCustodia.ActualizarConfiguracionFlujoCustodia(datos,function(result) {
				console.log(JSON.stringify(result));
				console.log("Error contador."+ result.P_OUT_ERRORESCONTADOR);
				
				var msg = "";
				if (result.P_OUT_ERRORESCONTADOR == "0") {
				    msg = $translate.instant('UI_ACT_CONFIG_FLUJO_CUSTODIA_EXITO');
				} else {
				    msg = $translate.instant('UI_ERR_FLUJO_CUSTODIA');
				}
	
				bcsComunes.ventanaMsg({mensaje : msg, conflictos : util.parseServerResponse(result)}, idVentana);
		    },
		    function(result) {
		    	bcsComunes.ventanaMsg({mensaje : $translate.instant('UI_ERR_FLUJO_CUSTODIA'), conflictos : util.parseServerResponse(result)}, idVentana);
		    });
		} else {
		    bcsComunes.msg("warn", "warn", "UI_SIN_CAMBIOS");
		}
	}
});

angular.module('ModuloPlataforma').factory('bcsServiciosConfigFlujoCustodia',['$resource',function($resource) {
	var urlActualizarConfiguracionFlujoCustodia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=65433";
	return $resource("", {}, {
		'ActualizarConfiguracionFlujoCustodia' : {
			method : "POST",
			url : urlActualizarConfiguracionFlujoCustodia
		}
	});
}]);