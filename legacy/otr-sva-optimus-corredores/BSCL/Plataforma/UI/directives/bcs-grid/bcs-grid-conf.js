angular.module('ModuloPlataforma').controller('bcsGridConfCtrl',function ($scope,bcsComunes) {
		/****************************************
		 *
		 * Declaraciones
		 *
		 * **************************************/
		//Obtiene los parametros pasado a la ventana
		$scope.IdVentana = bcsComunes.ventanaWinId ($scope);   
		$scope.dato = bcsComunes.ventanaParams($scope.IdVentana);
		var gridId = null;
		if ( typeof $scope.dato !== 'undefined' && 
			 typeof $scope.dato.confOptions !== 'undefined' &&
			 $scope.dato.confOptions.getGridId() !== '') {
			gridId = $scope.dato.confOptions.getGridId();
		}
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
			
		};/*
		$scope.aceptar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN ACEPTAR");
			bcsComunes.ventanaCerrar($scope.IdVentana);			
		};*/

		$scope.guardar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN GUARDAR");
			
			var tmpData = $scope.dato.confOptions.dataSource.data();
			var newConf = { columns: bcs.cleanData(dataSource.data())};
			sesion.setPreferencias("GRID_" + $scope.dato.confOptions.getGridId(), newConf);
			$scope.dato.confOptions.setConfiguration(newConf);
			var msg = "Preferencias guardadas.";
			if (tmpData.length > 0) {
				$scope.dato.confOptions.dataSource.fetch(function(){
					$scope.dato.confOptions.dataSource.data(tmpData);
				});
				msg += " Actualizando datos...";
			}

			dataSource.fetch(function(){
				var conf = $scope.dato.confOptions.getConfiguration();
				dataSource.data(conf.columns);
			});

			bcsComunes.msg("info","info", msg);
			//bcsComunes.ventanaCerrar($scope.IdVentana);
			
		};

		$scope.aplicar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN APLICAR");

			var tmpData = $scope.dato.confOptions.dataSource.data();
			$scope.gridConfOptions;
			$scope.dato.confOptions.setConfiguration({  
				columns: bcs.cleanData(dataSource.data())
			});
			var msg = "Preferencias aplicadas.";
			if (tmpData.length > 0) {
				$scope.dato.confOptions.dataSource.fetch(function(){
					$scope.dato.confOptions.dataSource.data(tmpData);
				});
				//$scope.dato.confOptions.dataSource.read();
				msg += " Actualizando datos...";
			}

			dataSource.fetch(function(){
				var conf = $scope.dato.confOptions.getConfiguration();
				dataSource.data(conf.columns);
				$scope.expandedRows = [];
				/*var detailRows = $scope.bcsgridConf.tbody.find('td.k-hierarchy-cell .k-icon').closest("tr");
				for(var i = 0; i < detailRows.length; i++) {
					$(detailRows[i]).data("uid")
					if(!!$scope.expandedRows[$(detailRows[i]).data("uid")]) {
						$scope.bcsgridConf.expandRow($(detailRows[i]));
					}
				}*/
			});

			bcsComunes.msg("info","info", msg);
			
		};

		$scope.restaurar = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN RESTAURAR");

			var tmpData = $scope.dato.confOptions.dataSource.data();
			$scope.dato.confOptions.setConfiguration();
			var conf = $scope.dato.confOptions.getConfiguration();
			var msg = "Preferencias restauradas.";
			if (tmpData.length > 0) {
				$scope.dato.confOptions.dataSource.fetch(function(){
					$scope.dato.confOptions.dataSource.data(tmpData);
				});
				msg += " Actualizando datos...";
			}
			dataSource.fetch(function(){
				dataSource.data(conf.columns);
			});

			bcsComunes.msg("info","info", msg);
			
		};

		$scope.guardarPredeterminado = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN GUARDAR PREDETERMINADO");

			if(gridId != null) {
				var tmpData = $scope.dato.confOptions.dataSource.data();

				var val = { columns: bcs.cleanData(dataSource.data())};
				var key = BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId;
				saveDefaultOptions([{"Llave" : key+"", "Valor": JSON.stringify(val)}]); // Guarda en BD.
				$scope.dato.confOptions.setConfiguration(val);

				var defaultOptions = sesion.getPreferencias("DEFAULT-GRID");
				if(typeof defaultOptions !== "undefined") {
					defaultOptions[key] = JSON.stringify(val);
					sesion.setPreferencias("DEFAULT-GRID", defaultOptions);
				}
				
				var conf = $scope.dato.confOptions.getConfiguration();
				var msg = "Preferencias predeterminadas guardadas.";
				if (tmpData.length > 0) {
					$scope.dato.confOptions.dataSource.fetch(function(){
						$scope.dato.confOptions.dataSource.data(tmpData);
					});
					msg += " Actualizando datos...";
				}
				dataSource.fetch(function(){
					dataSource.data(conf.columns);
				});

				bcsComunes.msg("info","info", msg);
			}
			
		};
		
		$scope.restaurarPredeterminado = function() {
			bcsComunes.ventanaData($scope.IdVentana,"CLICK BTN RESTAURAR PREDETERMINADO");

			if(gridId != null) {
				var key = BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId;
				var defaultOptions = sesion.getPreferencias("DEFAULT-GRID");

				if(!!defaultOptions && !!defaultOptions[key]) {
					var tmpData = $scope.dato.confOptions.dataSource.data();
					$scope.dato.confOptions.setConfiguration(JSON.parse(defaultOptions[key]));
					var msg = "Preferencias predeterminadas restauradas.";
					if (tmpData.length > 0) {
						$scope.dato.confOptions.dataSource.fetch(function(){
							$scope.dato.confOptions.dataSource.data(tmpData);
						});
						msg += " Actualizando datos...";
					}
					dataSource.fetch(function(){
						var conf = $scope.dato.confOptions.getConfiguration();
						dataSource.data(conf.columns);
					});

					bcsComunes.msg("info","info", msg);
				}
			}

			/*if(gridId != null) {
				var key = BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId;

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
				ds.fetch().then(function() {
					var defaultOptions = null;
					var data = ds.data();
					if(data.length === 1) {
						defaultOptions = JSON.parse(data[0].Valor);

						$scope.dato.confOptions.setConfiguration(defaultOptions);

						var tmpData = $scope.dato.confOptions.dataSource.data();
						sesion.setPreferencias("GRID_" + gridId, defaultOptions);
						var msg = "Preferencias restauradas.";
						if (tmpData.length > 0) {
							$scope.dato.confOptions.dataSource.fetch(function(){
								$scope.dato.confOptions.dataSource.data(tmpData);
							});
							msg += " Actualizando datos...";
						}

						dataSource.fetch(function(){
							var conf = $scope.dato.confOptions.getConfiguration();
							dataSource.data(conf.columns);
						});

						bcsComunes.msg("info","info", msg);
					}
					bcsComunes.ventanaOverlay($scope.IdVentana,false);
				});

			}*/
			
		};



		/* ****************************
		 *  Logica
		 * ****************************/

		if ($scope.dato == undefined || $scope.dato=="") {
			$scope.dato="SIN PARAMETRO";
		}
		var detailTemplate = $scope.dato.confOptions.getOptions().detailTemplate;
		var hideLockColumn = _.isNull(detailTemplate) ? false : true;

		var confOptions = $scope.dato.confOptions.getConfiguration();
		var confOptionsCols = confOptions.columns.slice();
		dataSource = new kendo.data.DataSource({
				schema: {
				  model: {
					fields: {
					  title:	{ type: "string", editable: false },
					  width:	{ type: "number" },
					  visible:	{ type: "boolean", editable: false },
					  locked:	{ type: "boolean", editable: false }
					}
				  }
				},
      			data: confOptionsCols
			});

		$scope.gridConfOptions = {
			dataSource: dataSource,
			height:290,
			editable:true,
			//groupable: true,
			detailTemplate_: "<div>Level: #: level #</div><div>Columna: #: title #</div>",
			detailTemplate: '<div class="detail-grid"></div>',
			detailExpand: function(e) {
				$scope.expandedRows[e.masterRow.data("uid")] = e.masterRow;
			},
			detailCollapse: function(e) {
				if(!!$scope.expandedRows[e.masterRow.data("uid")]) {
					$scope.expandedRows = $scope.expandedRows.splice(e.masterRow.data("uid"),1);
				}
			},
			detailInit: function(e) {
				console.log();
				e.detailRow.find(".detail-grid").parent().addClass("grid-detail-container");
				var detailGrid = e.detailRow.find(".detail-grid").kendoGrid({
					editable:true,
					scrollable: false,
					width:300,
					columns: [{
							template: '<div class="sapUiIcon sort detail"></div>',
							attributes: {
								style: "cursor: move !important;"
							},
							width: 16
						}, /*{
							field: "level",
							title: "Nivel",
							width: 20
						},*/ {
							field: "title",
							title: "Columnas",
							attributes: {
							  style: "text-align: left; height:20px !important;cursor: move !important;"
							}//,
							//width_: 200//"308px"
						}, {
							field: "width",
							title: "Ancho",
							attributes: {
							  style: "text-align: right;"
							},
							editor : function (container, options) {
								detailGrid;
								//options.model[options.field] //
								$('<input style="text-align: right;" data-bind="value:' + options.field + '"/>')
									.appendTo(container)
									.kendoNumericTextBox({
										spinners: false,
										format:"n0"
									})
									.keypress(function(e) {
										if(detailGrid && e.which === 13) {
											e.stopPropagation();
											detailGrid.closeCell(true);
										}
									})
									.keyup(function(e) {
										if (detailGrid && e.keyCode === 27) {
											e.stopPropagation();
											detailGrid.closeCell(true);
										}
									});
							},
							width: 90
						},{
							field: "visible",
							title: "Visible",
							//attributes: { "class": "editableCell" },
							template: '<div><kendo-button class="btnTransparent sapUiIcon #= visible ? \'show\' : \'iconHide\' #" ng-click="dataItem.visible = !dataItem.visible" ng-class="{\'show\': dataItem.visible, \'iconHide\': !dataItem.visible}"></kendo-button></div>',
							width: 80
						}/*,{
							field: "locked",
							template: '<div><kendo-button class="btnTransparent sapUiIcon action-settings #= locked ? \'locked\' : \'unlocked\' #" ng-click="dataItem.locked = !dataItem.locked" ng-class="{\'locked\': dataItem.locked, \'unlocked\': !dataItem.locked}"></kendo-button></div>',
							title: "Fija",
							hidden: hideLockColumn,
							width: "80px"
						}*/],
					dataSource: new kendo.data.DataSource({
						schema: {
							model: {
								fields: {
									title:	{ type: "string", editable: false },
									width:	{ type: "number" },
									visible:	{ type: "boolean", editable: false },
									locked:	{ type: "boolean", editable: false }
								}
							}
						},
      				//	data: bcs.cleanData(e.data.columns)
      					data: e.data.columns
					}),
					dataBound : function(e) {
						var  grid = this.wrapper.data("kendoGrid");
						if (!!grid) {
							var rows = grid.tbody.find("tr");
							for(var i=0; i < rows.length; i++) {
								var row = $(rows[i]);
								$injector = row.injector();
								$injector.invoke(function($compile){
									var newScope = row.scope().$new(false);
									var dataItem = grid.dataItem(row);
									newScope.dataItem = dataItem;
									$compile(row)(newScope);
								});
							}
						}
					}
				}).data("kendoGrid");

				$(detailGrid.table.children()[1]).hide();

				detailGrid.table.kendoSortable({
					filter: ">tbody >tr",
					cursor: "move",
					autoScroll: true,
					hint:$.noop,
					placeholder: function(element) {
						return element.clone().addClass("k-state-hover").css({"opacity": 0.65, "background-color": "#b5d5fd"});
					},
					ignore: "span,input,button",
					change: function(e) {
						var newIndex = e.newIndex;
						var data = detailGrid.dataSource.data();
						var dataItem = detailGrid.dataSource.getByUid(e.item.data("uid"));
						detailGrid.dataSource.remove(dataItem);
						detailGrid.dataSource.insert(newIndex, dataItem);
					}
				});

			},
			//navigatable:true,
			dataBound: function(e) {
			//	var count = 0;
				var eventData = e.sender;
				var rows = eventData.dataSource.data();
				if(rows.length > 0) {
					var htmlRows = eventData.tbody.find('tr');
					var hashRows = [];
					for (var i = 0; i < htmlRows.length; i++) {
						if(typeof $(htmlRows[i]).attr('data-uid') !== "undefined") {
							hashRows[$(htmlRows[i]).attr('data-uid')] = $(htmlRows[i]);
						}
					}
					for (var i = 0; i < rows.length; i++) {
						if(!rows[i].columns) {
							hashRows[rows[i].uid].find('td.k-hierarchy-cell .k-icon').removeClass();
						}
						if(!!$scope.expandedRows[rows[i].uid]) {
							e.sender.expandRow(hashRows[rows[i].uid]);
						}
					}
				}
			},
			columns: [
				{
					template: '<div class="sapUiIcon sort"></div>',
					attributes: {
					  style: "cursor: move !important;"
					},
					width: "16px"
				},
				/*{
					field: "level",
					title: "Nivel",
					width: 20
				},*/
				{
					field: "title",
					title: "Columnas",
					template: '<div>#= title#</div>',
					attributes: {
					  style: "text-align: left; height:20px !important;cursor: move !important;"
					},
					width_: 200//"308px"
				},{
					field: "width",
					title: "Ancho",
					attributes: {
					  style: "text-align: right;"
					},
					editor : function (container, options) {
						//options.model[options.field] //
						if(!!options.model[options.field]) {
							$('<input style="text-align: right;" data-bind="value:' + options.field + '"/>')
								.appendTo(container)
								.kendoNumericTextBox({
									spinners: false,
									format:"n0"
								})
								.keypress(function(e) {
									if($scope.bcsgridConf && e.which === 13) {
										e.stopPropagation();
										$scope.bcsgridConf.closeCell();
									}
								})
								.keyup(function(e) {
									if ($scope.bcsgridConf && e.keyCode === 27) {
										e.stopPropagation();
										$scope.bcsgridConf.closeCell(true);
									}
								});
						}
					},
					width: "90px"
				},{
					field: "visible",
					title: "Visible",
					//attributes: { "class": "editableCell" },
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
					//	widget.collapseRow(element);
						if(!element.hasClass("k-detail-row")) {
							widget.collapseRow(element.parent().children());
							/*if(element.next().hasClass("k-detail-row"))
							//	return [element.clone().addClass("k-state-hover").css({"opacity": 0.65, "background-color": "#b5d5fd"})[0], element.next()[0]];
								return element.clone().addClass("k-state-hover").css({"opacity": 0.65, "background-color": "#b5d5fd"});*/
							return element.clone().addClass("k-state-hover").css({"opacity": 0.65, "background-color": "#b5d5fd"});
						}
						return element;
					},
				//	ignore: "td,span,input,button,div.sapUiIcon.sorting-ranking.detail-grid",
					ignore: "td,span,input,button,div.detail",
					change: function(e) {
						var newIndex = e.newIndex;
						var data = widget.dataSource.data();
						var dataItem = widget.dataSource.getByUid(e.item.data("uid"));

						widget.dataSource.remove(dataItem);
						widget.dataSource.insert(newIndex, dataItem);
					//	widget.expandRow(e.item);
						widget.expandRow(widget.tbody.find('[data-uid="'+e.item.data("uid")+'"]'));
					}
				});
			}
		});


		/* ****************************
		 *  Utilitarios
		 * ****************************/

		saveDefaultOptions = function(conf) {
			var options = {
				p_IdEmpresa : BCS_SESION.IdEmpresa,
			//	p_IdUsuario : BCS_SESION.IdUsuario,
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
			//
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
					value: key // BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + gridId
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
					/*
					console.log(confPredeterminada);
					$scope.dato.confOptions.setConfiguration(confPredeterminada);
					var conf = $scope.dato.confOptions.getConfiguration();
					dataSource.data(conf.columns);
					dataSource.read();*/
					//$scope.bcsgridConf.refresh();
				}
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
				return defaultOptions;
			});
			return a;
		}

});
