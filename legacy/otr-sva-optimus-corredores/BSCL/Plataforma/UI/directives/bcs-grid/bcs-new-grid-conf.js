angular.module('ModuloPlataforma').controller('bcsGridNewConfCtrl',function ($scope,bcsComunes) {
		/****************************************
		 *
		 * Declaraciones
		 *
		 * **************************************/
		//Obtiene los parametros pasado a la ventana
		$scope.IdVentana = bcsComunes.ventanaWinId ($scope);   
		$scope.dato = bcsComunes.ventanaParams($scope.IdVentana);
		var gridId = $scope.dato.gridId;
		var dataSource = null;
		$scope.expandedRows = [];
		/****************************************
		 *
		 * Eventos
		 *
		 * **************************************/
		$scope.cerrar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN CERRAR");
			bcsComunes.ventanaCerrar($scope.IdVentana);
			
		};

		function generar() {
			var conf = dataSource.data();
			var saveData = {};
			saveData.columns = {};
			for (var i = 0;i<conf.length;i++) {
				var colConf = {
					
				};

				if (conf[i].title !== undefined) {
					colConf.title = conf[i].title;
				}
				if (conf[i].width !== undefined) {
					colConf.width = conf[i].width;
				}
				if (conf[i].visible !== undefined) {
					colConf.visible = conf[i].visible;
				}
				else {
					colConf.visible = true;
				}
				if (conf[i].locked !== undefined) {
					colConf.locked = conf[i].locked;
				}

				colConf.columnIndex = i;

				saveData.columns[conf[i].field] = colConf;
			}
			return saveData;
		}

		$scope.guardar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN GUARDAR");
			var saveData = generar();
			$scope.dato.confOptions.applyColumnConfiguration(saveData);
			sesion.setPreferencias("GRID_" + gridId, saveData);
			bcsComunes.ventanaCerrar($scope.IdVentana);
			
		};
		
		$scope.cancelar = function() {
			bcsComunes.ventanaCerrar($scope.IdVentana);
		}

		$scope.restaurar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN RESTAURAR");
			$scope.dato.confOptions.applyColumnConfiguration($scope.dato.confOptions.gridStartingSettings);
			sesion.setPreferencias("GRID_" + gridId, $scope.dato.confOptions.gridStartingSettings);
			var msg = "Preferencias restauradas.";
			bcsComunes.msg("info","info", msg);
			bcsComunes.ventanaCerrar($scope.IdVentana);
			
		};

		$scope.guardarPredeterminado = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN GUARDAR PREDETERMINADO");
			var saveData = generar();
			var key = BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId;
			saveDefaultOptions([{"Llave" : key+"", "Valor": JSON.stringify(saveData)}]); // Guarda en BD.
			
			var defaultOptions = sesion.getPreferencias("DEFAULT-GRID");
			if(typeof defaultOptions !== "undefined") {
				defaultOptions[key] = saveData;
				sesion.setPreferencias("DEFAULT-GRID", defaultOptions);
			}
			
			var msg = "Preferencias predeterminadas guardadas.";
			bcsComunes.msg("info","info", msg);
			
		};
		
		$scope.restaurarPredeterminado = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN RESTAURAR PREDETERMINADO");
			var defaultOptions = sesion.getPreferencias("DEFAULT-GRID");
			var key = BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId;
			var pref = defaultOptions[key];
			$scope.dato.confOptions.applyColumnConfiguration(pref);
			sesion.setPreferencias("GRID_" + gridId, pref);
			
			var msg = "Preferencias predeterminadas restauradas.";
			bcsComunes.msg("info","info", msg);
			bcsComunes.ventanaCerrar($scope.IdVentana);

		};
		
		
		saveDefaultOptions = function(conf) {
			var options = {
				p_IdEmpresa : BCS_SESION.IdEmpresa,
				p_IdModulo: 'PLA',
				p_Configuracion: conf
			};
			$.ajax({
			method: "POST",
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13022",
				data: JSON.stringify(options),
				contentType: 'application/json',
				dataType: 'json'
			}).fail(function(err) {
				console.log(err);
			});

			var defaultOptions = sesion.getPreferencias("DEFAULT-GRID");
			if(typeof defaultOptions !== "undefined" && conf.length > 0) {
				defaultOptions[conf[0].Llave] = conf[0].Valor;
				sesion.setPreferencias("DEFAULT-GRID", defaultOptions);
			}
		}

		getDefaultOptions = function(key) {
			var ds = new kendo.data.DataSource({
				type: "odata",
				serverFiltering: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13021",
						dataType: "json"
					}
				},
				filter: {
					field: "Llave",
					operator: "eq",
					value: key
				},
				schema: {
					model: {
						fields : {
							IdEmpresa:	{type: "number"},
							IdUsuario:	{type: "number"},
							Llave:		{type: "string"},
							Valor:		{type: "string"},
						}
					}
				}
			});

			bcsComunes.ventanaOverlay($scope.IdVentana,true);
			var a = ds.fetch().then(function() {
				bcsComunes.ventanaOverlay($scope.IdVentana,true);
				var defaultOptions = null;
				var data = ds.data();
				if(data.length === 1) {
					defaultOptions = JSON.parse(data[0].Valor);
				}
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				return defaultOptions;
			});
			return a;
		}

		/******** START ********/
		
		
		var hideLockColumn =  true;
		var countLocked = 0;
		var confOptionsCols = $scope.dato.api.columns.slice();
		var data = [];
		for (var c in confOptionsCols) {
			if (confOptionsCols[c].field == "blank") {
				continue;
			}
			if (confOptionsCols[c].visible === undefined) {
				c.visible = true;
			}
			data.push({
				field : confOptionsCols[c].field,
				title : confOptionsCols[c].title,
				width : confOptionsCols[c].width,
				visible : !confOptionsCols[c].hidden,
				locked : confOptionsCols[c].locked,
			});
			
			if (confOptionsCols[c].locked === true) {
				countLocked++;
			}
		}
		
		if (countLocked > 0) {
			hideLockColumn = false;
		}
		
		dataSource = new kendo.data.DataSource({
			schema: {
				model: {
					fields: {
						field : { type: "string"},
					  	title:	{ type: "string", editable: false },
					  	width:	{ type: "number" },
					  	visible:	{ type: "boolean", editable: false },
					  	locked:	{ type: "boolean", editable: false }
					}
				}
			},
      		data: data
		});

		$scope.gridConfOptions = {
			dataSource: dataSource,
			height:220,
			editable:true,
			columns: [
				{
					template: '<div class="sapUiIcon sort"></div>',
					attributes: {
					  style: "cursor: move !important;"
					},
					width: "16px"
				},
				{
					field: "title",
					title: "Columnas",
					template: '<div>#= title#</div>',
					attributes: {
					  style: "text-align: left; height:20px !important;cursor: move !important;"
					},
					width_: 200
				},{
					field: "width",
					title: "Ancho",
					attributes: {
					  style: "text-align: right;"
					},
					width: "90px"
				},{
					field: "visible",
					title: "Visible",
					template: '<div><kendo-button class="btnTransparent sapUiIcon #= visible ? \'show\' : \'iconHide\' #" ng-click="dataItem.visible = !dataItem.visible" ng-class="{\'show\': dataItem.visible, \'iconHide\': !dataItem.visible}"></kendo-button></div>',
					width: "80px"
				},{
					field: "locked",
					template: '<div><kendo-button class="btnTransparent sapUiIcon action-settings #= locked ? \'locked\' : \'unlocked\' #" ng-click="dataItem.locked = !dataItem.locked" ng-class="{\'locked\': dataItem.locked, \'unlocked\': !dataItem.locked}"></kendo-button></div>',
					title: "Fija",
					hidden: hideLockColumn,
					width: "80px"
				}
			]
		};

		$scope.$on("kendoWidgetCreated", function(event, widget){
			if (widget === $scope.bcsgridConf) {

				widget.table.kendoSortable({
					filter: ">tbody >tr",
					autoScroll: true,
					cursor: "move",
					hint:$.noop,
					placeholder: function(element) {
						return element.clone().addClass("k-state-hover").css({"opacity": 0.65, "background-color": "#b5d5fd"});
					},
					ignore: "td,span,input,button,div.detail",
					change: function(e) {
						var newIndex = e.newIndex;
						var data = widget.dataSource.data();
						var dataItem = widget.dataSource.getByUid(e.item.data("uid"));

						widget.dataSource.remove(dataItem);
						widget.dataSource.insert(newIndex, dataItem);
					}
				});
			}
		});
		
		var firstHeight = $('#Win_bcsGridNewConf');
		firstHeight.css("height", "337px");

});
