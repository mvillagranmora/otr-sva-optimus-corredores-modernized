angular
	.module('ModuloPlataforma')
	.controller(
		'ConfiguracionFlujoCajaCtrl',
		function($scope, bcsComunes, $compile, $translate, $resource,
			bcsServiciosConfigFlujoCaja, bcsServicioPlataforma) {

		    // Funcion que filtra un arreglo hacia un arreglo con
		    // elementos unicos (elimina duplicaciones de un mismo elemento).
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
			var b = Math.round((tab.length * 5.4));
			b = b + '' + 'px';
			return b;
		    }

		    // Funcion que verifica si el "datasource" ha sido modificado
		    // de su forma original (recibida).
		    function hasBeenChanged(kalista) {
			for (j = 0; j < kalista.gridFlujoCaja.dataSource.data().length; j++) {
			    for (i = 1; i < kalista.gridFlujoCaja.columns.length; i++) {

				if (kalista.gridFlujoCaja.dataSource.data()[j][kalista.gridFlujoCaja.columns[i].field] != kalista.gridFlujoCaja.dataSource.ds._pristineData[j][kalista.gridFlujoCaja.columns[i].field]) {
				    return true
				}
			    }
			}
			return false
		    }

		    // Funcion que devuelve una lista de 3-tupla ("IdUsoFlujo","ItemCalculo" y "FuncionOperador")
		    // dado una ConfiguracionFlujoCaja; el objeto de esta clase.
		    function tripletaIdItemOperacion(sol) {
			var algo = [];
			for (j = 0; j < sol.gridFlujoCaja.dataSource.data().length; j++) {
			    for (i = 1; i < sol.gridFlujoCaja.columns.length; i++) {
				algo
					.push({
					    Item : sol.gridFlujoCaja.dataSource
						    .data()[j].Item,
					    IdUsoFlujoCaja : tkm[sol.gridFlujoCaja.columns[i].field],
					    FuncionOperador : sol.gridFlujoCaja.dataSource
						    .data()[j][sol.gridFlujoCaja.columns[i].field]
					});
			    }
			}
			return algo;
		    }

		    var tkm = {};
		    var overlay = bcsComunes.overlay;
		    var cFlujoCaja = this;
		    var idVentana = bcsComunes.ventanaWinId($scope);
		    var dataSourceGrid = {
			type : "array",
			definition : {
			    serverSorting : false,
			    data : [],
			    schema : {
				model : {
				    fields : {
					A : {
					    type : "string"
					}
				    }

				}
			    }
			}
		    };

		    cFlujoCaja.gridFlujoCaja = {
			dataSource : new bcs.DataSource(dataSourceGrid),
			gridCreated : function(grid) {
			    $scope.gridApi = grid;

			},
			columns : [ {
			    field : "A",
			    title : "A",
			    width : "100px"
			} ],
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

		    cFlujoCaja.idVentana = bcsComunes.ventanaWinId($scope);
		    cFlujoCaja.parametros = bcsComunes
			    .ventanaParams($scope.idVentana);

		    var dsFlujoCaja = {
			type : "odata",
			definition : {
			    serverFiltering : true,
			    serverPaging : true,
			    serverSorting : true,
			    transport : {
				read : {
				    url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=70123",
				    dataType : "json"
				}
			    },
			    schema : {
				model : {
				    fields : {
					IdUsoFlujoCaja : {
					    type : "number"
					},
					NombreUso : {
					    type : "string"
					},
					IdEmpresa : {
					    type : "number"
					},
					IdEstadoUso : {
					    type : "string"
					},
					ItemCalculo : {
					    type : "string"
					},
					ItemOrigen : {
					    type : "string"
					},
					DescripcionConfiguracion : {
					    type : "string"
					},
					FuncionOperador : {
					    type : "string"
					},
					IdEstadoConfiguracion : {
					    type : "string"
					},
					NombreEstadoConfiguracion : {
					    type : "string"
					},
					DesplegarEnUi : {
					    type : "string"
					}
				    }
				}
			    }
			}
		    };

		    dsFlujoCaja = new bcs.DataSource(dsFlujoCaja);

			var llenarGrilla = function() {
				dsFlujoCaja
					.fetch()
					.then(
						function() {
						var data = dsFlujoCaja.data();
						var columns = [];
						var FIELDSXX = {};
						var datos = [];
						var items = [];

						// Extraccion de nombres de columnas y
						// de la columna entera "Item"
						// Y asociacion del "IdUsoFlujoCaja" con 
						// cada "NombreUso".
						for (i = 0; i < data.length; i++) {
							tkm[data[i]["NombreUso"]] = data[i]["IdUsoFlujoCaja"];
							columns.push(data[i]["NombreUso"]);
							items.push(data[i]["ItemCalculo"]);
						}

						// Unicidad de valores, columnas y la
						// columna "Item" con sus valores.
						columns = uniqueArray(columns);
						items = uniqueArray(items);

						// La primera columna es siempre "Item".
						FIELDSXX['Item'] = {
							type : 'string'
						};

						// Se añaden las columnas a "columns".
						for (i = 0; i < columns.length; i++) {
							columns[i] = ({
							field : columns[i],
							title : columns[i],
							width : largoTab(columns[i]),
							editable : true
							});
							FIELDSXX[columns[i].field] = {
							type : 'string',
							editable : true
							};
						}

						// La primera columna es siempre "Item".
						columns.unshift({
							field : "Item",
							title : "Item",
							width : "200px"
						});

						// Datos de la columna "Item" ("Item
						// Calculo").
						for (i = 0; i < items.length; i++) {
							datos[i] = {
							Item : items[i]
							};
						}

						// Aqui se extraen los datos de
						// "UsoFlujoCaja"
						// para luego pivotearlos
						for (i = 1; i < columns.length; i++) {
							for (j = 0; j < items.length; j++) {
							for (k = 0; k < data.length; k++) {
								if (columns[i].field == data[k].NombreUso
									&& items[j] == data[k].ItemCalculo) {
								datos[j][columns[i].field] = data[k].FuncionOperador;
								} else if (!(datos[j]
									.hasOwnProperty(columns[i].field))) {
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

						cFlujoCaja.gridFlujoCaja = {
							dataSource : new bcs.DataSource(
								dataSourceGrid),
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

						$scope.gridApi
							.setOptions(cFlujoCaja.gridFlujoCaja);

						},
						function() {
						bcsComunes.msg("error", "error",
							"ERROR_EN_ALGO??????");
						bcsComunes.ventanaOverlay(
							$scope.idVentana, false);

						})
			}

			llenarGrilla();

		    // '+', '-' o '0' son los posibles valores en la grilla
		    // ingresar otro valor no es posible.
		    $scope.cFlujoCaja.gridFlujoCaja.save = function(e) {
			// console.log(e);
			let a = Object.keys(e.values)[0];
			if (!([ '+', '-', '0' ].includes(e.values[a]))) {
			    bcsComunes.msg("error", "UI_ERROR",
				    "UI_VALOR_NO_ESPERADO");
			    e.preventDefault();
			} else {
			    console.log(cFlujoCaja);
			}
		    };

		    // Graba los cambios de configuracion flujo caja
		    $scope.GrabarConfigurafionFlujoCaja = function() {
			// Ve si hubieron cambios en la grilla a la inicial
			if (hasBeenChanged(cFlujoCaja)) {
			    var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'N',
				p_ConfiguracionFlujoCaja : tripletaIdItemOperacion(cFlujoCaja)
			    };

			    // Promesa
			    bcsServiciosConfigFlujoCaja
				    .ActualizarConfiguracionFlujoCaja(
					    datos,
					    function(result) {
						console.log(JSON
							.stringify(result));
						console
							.log("Error contador."
								+ result.P_OUT_ERRORESCONTADOR);
						var msg = "";
						if (result.P_OUT_ERRORESCONTADOR == "0") {
						    msg = $translate
							    .instant('UI_ACT_CONFIG_FLUJO_CAJA_EXITO');
								llenarGrilla();

						} else {
						    msg = $translate
							    .instant('UI_ERR_FLUJO_CAJA');
						}

						bcsComunes
							.ventanaMsg(
								{
								    mensaje : msg,
								    conflictos : util
									    .parseServerResponse(result)
								}, idVentana);

					    },
					    function(result) {
						bcsComunes
							.ventanaMsg(
								{
								    mensaje : $translate
									    .instant('UI_ERR_FLUJO_CAJA'),
								    conflictos : util
									    .parseServerResponse(result)
								}, idVentana);
					    });
			} else {
			    bcsComunes.msg("warn", "warn", "UI_SIN_CAMBIOS");
			}
		    }

		});

angular
	.module('ModuloPlataforma')
	.factory(
		'bcsServiciosConfigFlujoCaja',
		[
			'$resource',
			function($resource) {
			    var urlActualizarConfiguracionFlujoCaja = "/BSCL/Plataforma/GWJAVA/PLA/call?t=65432";
			    return $resource("", {}, {
				'ActualizarConfiguracionFlujoCaja' : {
				    method : "POST",
				    url : urlActualizarConfiguracionFlujoCaja
				}
			    });
			} ]);