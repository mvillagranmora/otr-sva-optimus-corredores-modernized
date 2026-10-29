angular.module('ModuloPlataforma').directive('bcsGrid', function($timeout,$compile,$translate,$rootScope,bcsComunes,bcsServicioPlataforma) {

	/*
	<div kendo-grid="bcsgrid" k-options="KOptions" k-on-change="handleChange(kendoEvent,data, dataItem, columns)"></div>

	<div kendo-grid="bcsgrid" k-options="kOptions" k-ng-delay="kOptions" k-on-change="handleChange(kendoEvent,data, dataItem, columns)" k-ng-model="kNgModel">
	<ng-transclude></ng-transclude>
	*/

	function userSettings(scope, element) {

	}

	function showGroupButtons(grid) {
		if(!grid)
			return;
		
		var widget = grid;
		var rowHeaderOpt = widget.element.find("thead>tr[role='row']").children(".k-group-cell.k-header");

		if(rowHeaderOpt.length > 0) {
			var groupingHeader = widget.element.find(".k-grouping-header").children();
			var group = bcs.clone(widget.dataSource.group());
			var expandFunctions = [];
			for(var i = 0; i < rowHeaderOpt.length; i++) {
				$(rowHeaderOpt[i]).addClass("bcs-group-header");

				if(!group[i])
					continue;

				if($(groupingHeader[i]).attr("data-field") !== group[i].field)
					continue;

				group[i].title = $(groupingHeader[i]).attr("data-title");
				var fld = $(groupingHeader[i]).attr("data-title");
				var divHtml = '<div class="bcs-group-buttons"><a class="k-icon k-i-expand" href="#" tabindex="-1" field="'+ fld +'"></a><a class="k-icon k-i-collapse" href="#" tabindex="-1" field="'+ fld +'"></a></div>';
				var e = $(rowHeaderOpt[i]).html(divHtml);
				var expandBtn = e.children().children(".k-i-expand");
				var collapseBtn = e.children().children(".k-i-collapse");
				var expandFn = function(e) {
					console.log(e);
				}
				expandFunctions.push(expandFn);
				var ef = expandFunctions;
				var auxI = i;

				expandBtn.kendoButton({
					click: function(e) {
						console.log(ef.length, auxI, group);
						var f = e.sender.element.attr("field");
						for(var i = 0; i < Object.keys(group).length; i++) {
							var groups = widget.element.find(".k-grouping-row:contains(" + group[i].title + ")");
							for (var j = 0; j < groups.length; j++) {
								widget.expandGroup($(groups[j]));
							}
							if(group[i].title === f) {
								break; //Fin de la función
							}
						}
					}
				});

				collapseBtn.kendoButton({
					click: function(e) {
						var f = e.sender.element.attr("field");
						var groups = widget.element.find(".k-grouping-row:contains(" + f + ")");
						for (var i = 0; i < groups.length; i++) {
							widget.collapseGroup($(groups[i]));
						}
					}
				});

				expandBtn.removeClass("k-button");
				collapseBtn.removeClass("k-button");

			}
		}
	}

	function apiGrid(scope, element) {
		scope.bcsApi = {};
		scope.bcsApi.dataSource = {};

		if (scope.bcsgrid !== undefined) {

			scope.bcsApi.getGridId = function() {
				var gridId = element.parent().parent().attr("id") || "";
				return gridId;
			}

			scope.bcsApi.isNestedGrid = function() {
				var isNestedGrid = true;
				var gridDetailConf = element.parent().parent().attr('detalle-configuracion');
				if (!gridDetailConf) isNestedGrid = false;
				return isNestedGrid;
			}

			scope.bcsApi.refresh = function() {
				var HtmlCell = scope.bcsgrid.element.find(".k-state-focused").closest("td");
				HtmlCell = HtmlCell.length === 0 ? scope.bcsgrid.element.find(".k-edit-cell").closest("td") : HtmlCell;
				var HtmlRow = HtmlCell.closest("tr");
				var cellIndex = scope.bcsgrid.cellIndex(HtmlCell);
				var dataUid = HtmlRow.attr("data-uid");
				scope.bcsgrid.refresh();
				var row = scope.bcsgrid.table.find("[data-uid=" + dataUid + "]");
				scope.bcsgrid.select(row);
				scope.bcsgrid.current(row.find("td:eq(" + cellIndex + ")"));
				scope.bcsgrid.editCell(row.find("td:eq(" + cellIndex + ")"));
			}

			scope.bcsApi.hideColumn = function(value) {
				return scope.bcsgrid.hideColumn(value);
			}

			scope.bcsApi.showColumn = function(value) {
				return scope.bcsgrid.showColumn(value);
			}

			scope.bcsApi.select = function(value) {
				return scope.bcsgrid.select(value);
			}

			scope.bcsApi.tbody = scope.bcsgrid.tbody;

			scope.bcsApi.getFilteredDataSource = function(pageSize) {
				var filters = scope.bcsgrid.getOptions().dataSource.filter;
				var dsConf = null;
				if ( typeof filters !== 'undefined' ) {
					dsConf = scope.bcsConf.dataSource.getOptions();
					dsConf.definition.filters = filters;
					delete dsConf.definition.pageSize;
					if (typeof pageSize !== 'undefined') {
						dsConf.definition.pageSize = pageSize;
					}
					return new bcs.DataSource(dsConf);
				}
				return null;

			}

			scope.bcsApi.getConfiguration = function() {

				var setColumnConfiguration = function(level, array, column) {
					var c = {};
					c.level = level;
					c.title = typeof column.title === 'undefined' ? column.field : column.title;
					c.field = column.field;
					c.visible = !column.hidden;
					c.locked = column.locked || false;
					c.width = column.width;
					if (!c.width) {
						var columnHeader = element.find('th[role="columnheader"][data-field="' + c.field + '"]');
						c.width = columnHeader.outerWidth();
					}
					else if (typeof c.width === "string") {
						c.width = Number(c.width.replace('px','').trim());
						c.width = isNaN(c.width) ? -1 : c.width;
					}

					array.push(c);

					if(!!column.columns) {
						var cc = column.columns;
						var l = level + 1;
						var arr = [];
						for ( var i = 0; i < cc.length; i++) {
						//	arguments.callee(l, array, cc[i]);
							arguments.callee(l, arr, cc[i]);
						}
						c.columns = arr;
					}
				};

				var conf = {};
				conf.columns = [];
				var options = scope.bcsgrid.getOptions();
				var columns = options.columns;
				for ( var i = 0; i < columns.length; i++) {
					setColumnConfiguration(0, conf.columns, columns[i]);
				}
				conf.filter = options.dataSource.filter;
				conf.sort = options.sortable ? options.dataSource.sort : undefined;
				return conf;
			}

			scope.bcsApi.setConfiguration = function(options) {
				var newKOptions = bcs.clone(scope.KOptions);/*
				if (options.columns.length !== newKOptions.columns.length) {
					return;
				}*/
				if ( options ) {/*
					//scope.bcsgrid.setOptions( scope.KOptions );
				}
				else {*/
					var columnsPosition = [];
					var optionsCols = [];
					var newKOptionsCols = [];
					//newKOptions = bcs.clone(scope.KOptions);
					for ( var i = 0; i < newKOptions.columns.length; i++) {
						var column = newKOptions.columns[i];
						if ( !!column ) {// && column.field ) {
							if(!column.field) {
								columnsPosition[column.title] = i;
							}
							else {
								columnsPosition[column.field] = i;
							}
							newKOptionsCols.push(column.field);
						}
						column = options.columns[i];
						if ( !!column ) {//&& column.field ) {
							optionsCols.push(column.field);
						}
					}
					if(newKOptions.columns.length > options.columns.length) {
						// La configuración guardada tiene menos columnas de las disponibles.
						// obtener columnas nuevas.
						var diffCols = _.difference(newKOptionsCols,optionsCols);
						// agregar a options.columns
						for(var i=0; i<diffCols.length; i++) {
							if (diffCols[i] === undefined) {
								continue;
							}
							var auxCol = newKOptions.columns[columnsPosition[diffCols[i]]];
							var newCol = {
								field: auxCol.field, //"EmployeeID",
								locked: false,
								title: auxCol.title, //"EmpID",
								visible: true,
								width: auxCol.width //100
							};
							options.columns.push(newCol);
						}
					}

					var newColumns = [];
					for ( var i = 0; i < newKOptions.columns.length; i++) {//options.columns.length; i++) {
						var newColumn = options.columns[i];
						//newColumns[i] = newKOptions.columns[columnsPosition[newColumn.field]];
						
						if (newColumn === undefined) {
							continue;
						}
						
						var key = !newColumn.field ? newColumn.title : newColumn.field;
						var tmp = newKOptions.columns[columnsPosition[key]];
						//if ( !newColumn.visible ) newColumns[i].hidden = true;
						if(!tmp) {
							continue;
						}
						newColumns[i] = tmp;
						if( typeof newColumns[i].hidden !== "undefined" && newColumn.visible) 
							newColumns[i].hidden = false;
						else if (!newColumn.visible)
							newColumns[i].hidden = true;
						newColumns[i].width = newColumn.width;
						/*if ( newColumn.visible ) newColumns[i].width = newColumn.width;
						else newColumns[i].width = 0;*/
						if ( scope.detailContent.length <= 0 && newColumn.locked ) {
							newColumns[i].locked = true;
						}
						else {
							newColumns[i].locked = newColumn.locked;
						}

						if (!!newColumn.columns && !!newColumns[i].columns) {
							console.log();
						    for (var j = 0; j < newColumn.columns.length; j++) {
						        var savedChild = newColumn.columns[j];
						        var originalIndex = -1;

						        for (var k = 0; k < newColumns[i].columns.length; k++) {
						            if (newColumns[i].columns[k].field === savedChild.field) {
						                originalIndex = k;
						                break;
						            }
						        }

						        if (originalIndex < 0) {
						            continue;
						        }

						        var originalChild = newColumns[i].columns[originalIndex];
						        newColumns[i].columns[originalIndex] = bcs.clone(originalChild);
						        newColumns[i].columns[originalIndex].width = savedChild.width;

						        if (typeof savedChild.visible !== "undefined") {
						            newColumns[i].columns[originalIndex].hidden = !savedChild.visible;
						        }

						        if (typeof savedChild.locked !== "undefined") {
						            newColumns[i].columns[originalIndex].locked = savedChild.locked;
						        }
						    }
						}
					}


					newColumns = _.compact(newColumns);

					newKOptions.columns = newColumns;
				}

				var dsOptions = scope.dataSource.getOptions();
				try {
					dsOptions.definition.filters = scope.bcsgrid.dataSource.filter();//options.filter;
					dsOptions.definition.sort = scope.bcsgrid.dataSource.sort();//options.sort;
				}
				catch(error) {
					console.log(error);
				}

				scope.dataSource.setOptions( dsOptions );
				newKOptions.dataSource = scope.dataSource.ds;

				scope.bcsgrid.setOptions(newKOptions);

				//scope.bcsApi.dataSource.read();
				//scope.bcsApi.refresh();
				console.log();
			}
			
			scope.bcsApi.setOptions = function (options) {
				$.each(scope.bcsConf, function(index, value){
					if(typeof options[index] !== 'undefined')
						scope.bcsConf[index] = options[index];
				});
				gridConfig(scope, scope.element);
				scope.bcsgrid.setOptions(scope.KOptions);
			}
			
			scope.bcsApi.getOptions = function () {
				return scope.bcsgrid.getOptions();
			}

			scope.bcsApi.clearSelection = function () {
				scope.bcsgrid.clearSelection();
			}
			
			scope.bcsApi.dataItem = function (row) {
				return scope.bcsgrid.dataItem(row);
			}

			/*if ( !_.isUndefined(scope.bcsgrid.dataSource) ) {
				scope.bcsApi.dataSource = scope.bcsgrid.dataSource;
			}*/

			scope.bcsApi.dataSource.add = function(model) {
				return scope.bcsgrid.dataSource.add(model);
			}

			if ( !_.isUndefined(scope.dataSource) && _.isUndefined(scope.dataSource.type) ) {

				scope.bcsApi.dataSource = scope.dataSource;
			}
			else {

				scope.bcsApi.dataSource.data = function(array, isObject) {
					if (isObject===true) {
						return scope.bcsgrid.dataSource.data(Object.keys(array).map(function(k) { return array[k] }));
					}else{
						return scope.bcsgrid.dataSource.data(array);	
					}
				}

				scope.bcsApi.dataSource.dataColumns = function(arrayCols, ds) {
					var dataCols = [];
					if ( ds ) {
						$.each(ds, function(index, value){
							dataCols[index] = _.pick(value,arrayCols);
						});
					} else {
						$.each(scope.bcsgrid.dataSource.data(), function(index, value){
							dataCols[index] = _.pick(value,arrayCols);
						});
					}

					return (dataCols.length > 0) ? dataCols : undefined;
				}

				scope.bcsApi.dataSource.formattedData = function(ds, fieldFormat) {
					var dataCols = [];
					for ( var i=0; i < ds.length; i++ ) {
					//	dataCols[i] = $.extend(true, {}, ds[i]);
						dataCols[i] = bcs.clone(ds[i]);
						for ( var j = 0; j < fieldFormat.length; j++ ) {
							if ( _.isFunction(fieldFormat[j].format) ) {
								var fieldValue = dataCols[i][fieldFormat[j].field];
								var formattedValue = fieldFormat[j].format(fieldValue);
								dataCols[i][fieldFormat[j].field] = formattedValue;
							}
						}
					}
					return dataCols;
				}

				scope.bcsApi.dataSource.filters = function(filters) {
					scope.bcsgrid.dataSource.filter(filters);
				}

				scope.bcsApi.dataSource.itemsEdited = function(array) {
					return scope.dataSourceChanged;
				}

				scope.bcsApi.dataSource.removeAll = function() {
					scope.bcsgrid.dataSource.data([]);
				}

				scope.bcsApi.dataSource.remove = function(item) {
					if ( $.isArray(item) ) {
						$.each(item, function(index, value){
							scope.bcsgrid.dataSource.remove(value);
						});
					}
				}

				scope.bcsApi.dataSource.read = function(data) {
					scope.bcsgrid.dataSource.read(data);
				}

				scope.bcsApi.dataSource.setCallParameters = function(parameters) {

					setCallTransport(
						scope.KOptions['dataSource'],
						scope.bcsConf.dataSource.definition.callTransaction,
						scope.bcsConf.dataSource.definition.callResultSet,
						parameters);

					scope.bcsgrid.setOptions({dataSource : scope.KOptions['dataSource']});
				}

				scope.bcsApi.dataSource.sync = function() {
					scope.bcsgrid.dataSource.sync();
				}

				scope.bcsApi.dataSource.getByUid = function(uid) {
					scope.bcsgrid.dataSource.getByUid(uid);
				}

				scope.bcsApi.dataSource.aggregate = function(data) {
					scope.bcsgrid.dataSource.aggregate(data);
				}

				scope.bcsApi.dataSource.aggregates = function() {
					return scope.bcsgrid.dataSource.aggregates();
				}

			}

		}
	}

	function setCallTransport(ds, callTransaction, callResultSet, callParameters) {
		ds.transport = {
			read: function(options) {
				$.ajax({
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet,
					dataType: "json",
					type: "POST",
					contentType:"application/json; charset=utf-8",
					data: JSON.stringify(callParameters),
					success: function(result) {
						var resultSet = [];
						var outResultSet = result[callResultSet] ? result[callResultSet] : result;
						for ( var key in outResultSet) {
							resultSet.push(outResultSet[key]);
						}
						result.P_OUT_TBL = resultSet;
						options.success(result.P_OUT_TBL);
					},
					error: function(result) {
						options.error(result);
					}
				});
			}
		};
	}

	function gridConfig(scope,elem) {
		scope.firstRender = true;
		scope.KOptions = {};
		scope.KOptions['autoBind'] = false;
		scope.KOptions['resizable'] = true;
		scope.KOptions['reorderable'] = true;
		scope.KOptions['filterable'] = false;

		scope.allSelected = {}; // virtual
		scope.virtualScrollable = false; // virtual

	//	scope.KOptions['toolbar'] = ['excel']; // Para exportar a excel.
	//	scope.KOptions['excel'] = { allPages : true }; // Para exportar a excel.
		scope.KOptions['excelExport'] = function(e) { // TODO: Evaluar rendimiento.
			if ( !_.isUndefined(scope.excelFileName) ) {
				e.workbook.fileName = scope.excelFileName + ".xlsx";
			}
			var rows = e.workbook.sheets[0].rows;
			var cellFormats = [];
			var columnsField = [];
			var columnsFormat = [];
			var flds = scope.bcsgrid.dataSource.options.schema.model.fields;
			var cols = scope.bcsgrid.columns;
			for (var i = 0; i < cols.length; i++) {
				if(!cols.columns && !!cols[i].field) {
					columnsField[cols[i].title] = cols[i].field;
				}
				else {
					for (var j = 0; j < cols[i].columns.length; j++) {
						columnsField["[" + i +"]##" +cols[i].columns[j].title] = cols[i].columns[j].field;
					}
				}
			}/*
			for (var i = 0; i < scope.bcsgrid.columns.length;i++ ) {

				columnsField[scope.bcsgrid.columns[i].title] = scope.bcsgrid.columns[i].field;
			}*/
			var n = 0;
			var prevHeader = [];
			var prevHeaderRows = 0;
			var nHeaders = 0;
			for ( var i = 0; i < rows.length; i++) {
				if( rows[i].type === "header" ) {
					nHeaders++;
					var cells = rows[i].cells;
					for ( var j = 0; j < cells.length; j++) {
						if (cells[j].colSpan > 1) {
							prevHeader.push({title: "[" + j + "]##", rows: cells[j].colSpan});
							continue;
						}
						var fld = columnsField[cells[j].value];
						if(!fld && prevHeader.length > 0) {
							fld = columnsField[prevHeader[n].title + cells[j].value];
							prevHeaderRows++;
							if(prevHeader[n].rows === prevHeaderRows) {
								prevHeaderRows = 0;
								n++;
							}
						}
						if(!!flds[fld] && !!flds[fld].excelFormat) {
							columnsFormat.push({format: flds[fld].excelFormat, field: fld});
						}
						else if(!!flds[fld] && (flds[fld].bcsType === "datetime" || flds[fld].bcsType === "sysdatetime")) {
							columnsFormat.push({format: "dd-mm-yyyy hh:mm:ss", field: fld});
						}
						else if(!!flds[fld] && flds[fld].bcsType === "systime") {
							columnsFormat.push({format: "hh:mm:ss", field: fld});
						}
						else {
							columnsFormat.push({format: null, field: fld});
						}
						if(j === cells.length-1) {
							prevHeader = [];
						}
					}
				}
				if( rows[i].type === "data" ) {
					var cells = rows[i].cells;
					for ( var j = 0; j < cells.length; j++) {
						if ( _.isDate(cells[j].value) && cells[j].value <= (new Date(0)) ) {
							cells[j].value = "";
						}
						if (columnsFormat[j]!= undefined  && columnsFormat[j].format != null) {
							if(_.isFunction(columnsFormat[j].format)) {
								var item = scope.bcsgrid.dataSource.data()[i-nHeaders];
								columnsFormat[j].field;
								if(!!item && cells[j].value === item[columnsFormat[j].field]) {
									cells[j].format = columnsFormat[j].format(item);
								}
								else {
									cells[j].format = null;
								}
							}
							else
								cells[j].format = columnsFormat[j].format;
						}
						// FORMATOS EN:
						// https://support.office.com/en-us/article/Create-or-delete-a-custom-number-format-78f2a361-936b-4c03-8772-09fab54be7f4
						// "#,##0.00";	
					}
				}
			}
		};

		scope.KOptions['pdfExport'] = function(e) {
			 var gridPdf =e.sender
			 if ( !_.isUndefined(scope.pdfFileName) ) {
			      gridPdf.options.pdf.fileName = scope.pdfFileName + ".pdf";
				}
			// para que el pdf tenga apariencia de xls
			//esconde los filtros
			var filterRow = gridPdf.thead.find(".k-filter-row");
			filterRow.hide();
			// ocultar el primer comentario
			var groupingHeader = gridPdf.wrapper.find(".k-grouping-header");
		    groupingHeader.hide();
			// cambiamos los estilos
			var style = document.createElement("style");
			style.innerHTML = `
				.k-grid td { background:#ffffff !important; }
				.k-grid table { border-collapse: collapse !important;}
				.k-grid th, 
				.k-grid td { background-clip: padding-box !important; border: 1.5px solid #666 !important;}
				.k-grid thead th,
				.k-grid thead th *,
				.k-grid .k-header,
				.k-grid .k-group-cell { color: white !important;fill: white !important; }
				.k-grid thead th {      background:#8d8d8d !important;} `;
			document.head.appendChild(style);
			    // formato punto para separador de miles y coma para decimales
				e.promise.progress(function(ev) {
					var page = ev.page;
					var rows = page.children[0].children;	
					for (var i = 0; i < rows.length; i++) {			
						var row = rows[i];
						if (row.children) {
							for (var j = 0; j < row.children.length; j++) {
								var cell = row.children[j];
								if (cell.text && !isNaN(cell.text.replace(",", "."))) {
									var number = parseFloat(cell.text.replace(",", "."));
									if (!isNaN(number)) {
										cell.text = kendo.toString(number, "n2");
									}
								}
							}
						}
					}
				});
			
			e.promise.then(function() {
				filterRow.show();
				groupingHeader.show();
				document.head.removeChild(style);
			});
   
		}


		//scope.KOptions['scrollable'] = { virtual: true };

		var dataSource;
		var filters;
		var gridDataBound;

		if ( scope.detailContent.length > 0 ) {
			scope.KOptions['detailTemplate'] = kendo.template(scope.detailContent) ;
			// TODO Eliminar scope.grid
			scope.grid = scope.$parent;
			var parent = scope.$parent;
			var parentGridId = parent.detailGrid;
		//	while ( parent == null && dataItem == null)
			if ( parentGridId ) {
				while (parent != null && parent.$id != parentGridId) {
					parent = parent.$parent;
				}
				for (var i = 0; i < parent.parentGridScopeKeys.length; i++) {
					scope[parent.parentGridScopeKeys[i]] = parent[parent.parentGridScopeKeys[i]];
				}
			}
			else {
				var scopeKeys = Object.keys(scope);
				var parentScopeKeys = Object.keys(parent.__proto__);
				var keys = [];

				var parentKeys = Object.keys(parent);
				var parentProtoKeys = Object.keys(parent.__proto__);
				var scopeParentKeys = _.difference(parentKeys, parentProtoKeys);

				/*for (var i = 0; i < parentScopeKeys.length; i++) {
					if( parentScopeKeys[i].search(/(\$\$)|(\$)/i) === -1 && !scope[parentScopeKeys[i]]) {	
						scope[parentScopeKeys[i]] = parent[parentScopeKeys[i]];
						keys.push(parentScopeKeys[i]);
					}
				}*/

				for (var i = 0; i < scopeParentKeys.length; i++) {
					if( scopeParentKeys[i].search(/(\$\$)|(\$)/i) === -1 && !scope[scopeParentKeys[i]]) {	
						scope[scopeParentKeys[i]] = parent[scopeParentKeys[i]];
						keys.push(scopeParentKeys[i]);
					}
				}
				
				if ( keys.length > 0 ) {
					scope.detailGrid = scope.$id;
					scope.parentGridScopeKeys = keys;
				}
			}
			console.log();
			// TODO Migrar
		}

		if ( scope.bcsConf && (dataSource = scope.bcsConf.dataSource ) ) {
			/*dataSource = {};
			scope.bcsConf.dataSource = {};
			scope.KOptions.dataSource = {};*/


			if ( dataSource.type ) {
				console.warn("********************************************");
				console.warn("Debe migrar definición estática de dataSource por");
				console.warn("una instancia de la clase bcs.DataSource.");
				console.warn("Este mensaje se transformará en error posterior 30/06");
				console.warn("********************************************");
				if (BCS_AMBIENTE=='DES' && moment(new Date()).format('YYYYMMDD')>20160630) {
					throw new Error("dataSource: definición estática eliminada!");
				}
				var dsDef = bcs.clone(dataSource.definition);

				switch ( dataSource.type ){
					case "array":
					dsDef.type = "array";
					if ( !dsDef.schema || !dsDef.schema.model || !dsDef.schema.model.fields ) {
						dsDef.schema = {};
						dsDef.schema.model = {};
						dsDef.schema.model.fields = {};
					}
					break;

					case "object":
					break;

					case "odata":
					dsDef.type = "odata";
					break;

					case "call":
					dsDef.type = "json";
					if ( dataSource.definition.callTransaction && dataSource.definition.callResultSet ) {
						if ( dataSource.definition.callParameters ) {
							setCallTransport(dsDef, dataSource.definition.callTransaction, dataSource.definition.callResultSet, dataSource.definition.callParameters );
						}
					}
					break;
				}

				if ( dataSource.definition.filters ) {
					dsDef.filter = dataSource.definition.filters;
				}

				if ( dataSource.definition.filtersFn && scope.grid && scope.grid.dataItem ) {
					dsDef.filter = ( dsDef.filter ) ? dsDef.filter : dataSource.definition.filtersFn(scope.grid.dataItem);
					delete dsDef.filtersFn;
				}

				var dsDefRequestEnd = function(e) {
					if(e.type === "read") {
						scope.dataSourceChanged = [];
						scope.dataSourceChangedMap = [];
						scope.dataSourceMap = [];
					}
				}

				var valueDsDefRequestEnd;

				if ( dsDef.requestEnd ) {
					valueDsDefRequestEnd = dsDef.requestEnd;
					var fnRequestEnd = function(e) {
						dsDefRequestEnd(e);
						valueDsDefRequestEnd(e);
					}
					dsDef.requestEnd = fnRequestEnd;
				}
				else {
					dsDef.requestEnd = dsDefRequestEnd;
				}

				dsDef.change = function(e) {
					if(e.action === "itemchange") {
						if ( scope.dataSourceChangedMap[e.items[0].uid] >= 0 ) {
							scope.dataSourceChanged[scope.dataSourceChangedMap[e.items[0].uid]] = e.items[0];
						}
						else {
							scope.dataSourceChanged.push(e.items[0]);
							scope.dataSourceChangedMap[e.items[0].uid] = scope.dataSourceChanged.length - 1;
						}
					}
					/*if(!e.sender.dataSource) {
						e.sender.dataSource ={};
						e.sender.dataSource.data = function() {
							return e.sender._data;
						}
						scope.KOptions["dataBound"](e);
					}*/

					//virtual start
					var grid = this;
                    var gridView = grid.dataSource.view();
                    var selection = this.select();

                    for (var i = 0; i < gridView.length; i++) {
                        var id = gridView[i].uid;
                        if (allSelected[id] == true) {
                            var isSelectedNow = false;
                            for (var j = 0; j < selection.length; j++) {
                                var row = grid.dataItem(selection[j]);
                                if (row.uid == id) {
                                    isSelectedNow = true;
                                    break;
                                }
                            }
                            if (!isSelectedNow) {
                                allSelected[id] = false;
                            }
                        }
                    }

                    for (var i = 0; i < selection.length; i++) {
                        allSelected[grid.dataItem(selection[i]).uid] = true;
                    }
					//virtual end
				};
			//	scope.KOptions['dataSource'] = $.extend(true, {}, dsDef);
				scope.KOptions.dataSource = bcs.clone(dsDef);
			}
			else {
				scope.dataSource = dataSource;
				scope.KOptions.dataSource = dataSource.ds;
			}
		}

		var columns;
		if ( (columns = scope.bcsConf['columns']) !== undefined ) {
			var cols = [];
			angular.copy(columns, cols);
			scope._editable = false;
			var groupable = false;
			var dsc = _.isUndefined(scope.dataSource) ? scope.KOptions.dataSource : scope.dataSource.definition;

			if(!dsc.schema || !dsc.schema.model || !dsc.schema.model.fields) {
				console.error("ERROR", "bcs-grid requiere de la definición de schema");
				return;
			}

			var validateFieldColumn = function(f, c) {
				if(!c.field && !!c.columns) {
					var cc = c.columns;
					for(var i = 0; i < cc.length; i++)
						arguments.callee(f,cc[i]);
				}
				else if(!f[c.field]){
					console.error("ERROR", "El campo '" + c.field + "' no está definido en el schema de bcs.DataSource");
					return true;
				}
				return false;
			};

			var fields = dsc.schema.model.fields;
			var gridError = 0;
			for(var i = 0; i < columns.length; i++) {
				var c = columns[i];
				if(validateFieldColumn(fields, c))
					gridError++;
			}

			if(gridError > 0) {
				console.error("**********************************************************************");
				console.error("Inconsistencias entre la definición de bcs.DataSource");
				console.error("Si estos errores persisten luego del 01/04, la bcs-grid no será creada");
				console.error("**********************************************************************");
				if (BCS_AMBIENTE=='DES' && moment(new Date()).format('YYYYMMDD') >= 20170401) {
					throw new Error("bccGrid, bcs.DataSource: Definición con inconsistencias.");
					return;
				}
			}

			if ( _.isUndefined(dsc) && !_.isUndefined($translate) ) {
				for ( var i = 0; i < cols.length ; i++) {
					cols[i].title = $translate.instant(cols[i].title);
				}
			}
			else {
				for ( var i = 0; i < cols.length ; i++) {

					fnSetColumnProperties(dsc, cols[i]);

					if (cols[i] && cols[i].columns && cols[i].columns.length > 0){
						for ( var j = 0; j < cols[i].columns.length ; j++) {
							fnSetColumnProperties(dsc, cols[i].columns[j]);
						}
					}

				}

			}
			scope.KOptions['columns'] = cols;
			if ( !_.isUndefined(scope.dataSource) ) {
				scope.dataSource.setOptions( { type : dsc.type, definition : dsc} );
				scope.KOptions.dataSource = scope.dataSource.ds;
			}

			if ( scope._editable ) {
				scope.KOptions['editable'] = true;
			}
			if ( groupable ) {
			//	scope.KOptions['groupable'] = $.extend(true, {}, groupable);
				scope.KOptions['groupable'] = bcs.clone2(groupable);
			}
		}

		if ( (valueDT = scope.KOptions['detailTemplate']) !== undefined ) {

			/*var detailExpand = function(e) {
				var gridHeader = e.detailRow.find(".k-grid-header");
				var gridHeaderStyle = gridHeader.attr("style");
				if (e.detailRow.find(".k-grid-header") !== undefined && gridHeader.length > 0 && gridHeaderStyle !== undefined && gridHeaderStyle.length > 0) {
					gridHeader.removeAttr( "style" );
					scope.bcsgrid.clearSelection();
					this.select(e.masterRow);
				}
			}

			scope.KOptions['detailExpand'] = detailExpand;*/

			var detail = function(e) {
				//var detInit = valueDI(e);
				scope.detailDataEvent = e;
			};

			var st = scope.detailContentValue;
			var dFn;
			if ( st && st.length > 0 && (dFn = scope[$.trim(st.substring(0,st.indexOf("(")))]) ) {
				scope.KOptions['detailInit'] = dFn;
			}
			var detailInit = scope.bcsConf['detailInit'];
			scope.KOptions['detailInit'] = function(e) {
				if(detailInit) {
					detailInit(e);
				}
				if ( e.sender.$angular_scope && e.sender.$angular_scope.$parent) {
					var parentScope = e.sender.$angular_scope.$parent;
					
				//	e.data.detailGrid = parentScope.detailGrid; // Versión corta.

				}
				//e.data.parentScope.ventana1innerFn = parentScope.ventana1innerFn;
			}
		}

		function fnSetColumnProperties(ds, columns) {
			var cols = columns;

			if ( !_.isUndefined($translate) ) {
				cols.title = $translate.instant(columns.title);
			}

			if( cols && cols.groupable ) {
				cols.groupable = true;
				groupable = true;
			}
			else {
				cols.groupable = false;
			}
			var tFields = null;
			if ( _.isUndefined(ds.options) ) {
				if ( !_.isUndefined(ds.schema) ) {
					tFields = ds.schema.model.fields[columns.field];
				}
			}
			else {
				tFields = ds.options.schema.model.fields[columns.field];
			}

			if ( tFields != null && tFields.type !== undefined ) {
				tFields.editable = false;
				if( cols && cols.editable ) {
					tFields.editable = true;
					cols.attributes = {};
					cols.attributes.class = "editableCell";
					cols.attributes["bcs-bind"] = "dataItem." + cols.field;
					scope._editable = true;
				}

				if( !!cols && !!cols.format && _.isFunction(cols.format)) {
					cols.bcsFormatTemplate = cols.format;
					delete cols.format;
					cols.template = function(i) {
						var v = i[cols.field];
						var f = cols.bcsFormatTemplate(v, i);
						if (!i["__bcsFormat__"])
							i["__bcsFormat__"] = {};
						i["__bcsFormat__"][cols.field] = f;
						return '<span bcs-bind="dataItem.' + cols.field + '">' + f.valor + '</span>';
					};
				}

				if( cols && cols.validation ) {
					tFields.validation = cols.validation;
				}
				var tType = tFields.type.toLowerCase();
				var tBcsType = _.isUndefined(tFields.bcsType) ? tFields.bcsType : tFields.bcsType.toLowerCase();

				if ( (tBcsType === "date" || tBcsType === "sysdate") && _.isUndefined(columns.template) ) {
					var tpl = "#if (util.dateisdefault(" + columns.field + "))  {##} " +
						"else {##=util.formatoDate(" + columns.field + ")##}#";
					cols.template = kendo.template(tpl);
				}

				if ( (tBcsType === "date" || tBcsType === "systime") && _.isUndefined(columns.template) ) {
					var tpl = "#if (util.dateisdefault(" + columns.field + "))  {##} " +
						"else {##=util.formatoTime(" + columns.field + ")##}#";
					cols.template = kendo.template(tpl);
				}

				if (!cols.filterable) cols.filterable = { cell : {} };
			//	cols.filterable.cell.delay = 1440000;
			//	cols.filterable.cell.minLength = 50;
			//	cols.filterable.ignoreCase = true;

				if ( (tBcsType === "datetime" || tBcsType === "sysdatetime") && _.isUndefined(columns.template) ) {
					var tpl = "#if (util.dateisdefault(" + columns.field + "))  {##} " +
						"else {##=util.formatoSecondate(" + columns.field + ")##}#";
					cols.template = kendo.template(tpl);
					cols.filterable = {
						cell: {
							template: function (args) {
								args.element.kendoDateTimePicker({
									format : "dd-MM-yyyy HH:mm" // TODO: Mapear con kendo culture.
								});
							}
						}
					};
				}
				else if(tType==="systime"){
					var tpl = "#if (util.dateisdefault(" + columns.field + "))  {##} " +
						"else {##=util.formatoTime(" + columns.field + ")##}#";
					cols.template = kendo.template(tpl);
					cols.filterable = {
						cell: {
							template: function (args) {
								args.element.kendoDateTimePicker({
									format : "HH:mm:ss" // TODO: Mapear con kendo culture.
								});
							}
						}
					};
				}
				else if (tFields.type === "string" && Object.keys(cols.filterable.cell).length === 0) {
					cols.filterable = {
						cell: {
							operator: "contains",
							template: function (args) {
								bcs.MaskedTextBox(args);
							}
						}
					};
				}
				else if (tFields.type === "number" && Object.keys(cols.filterable.cell).length === 0) {
					cols.filterable = {
						cell: {
							operator: "eq",
							template: function (args) {
								args.element.kendoNumericTextBox({
									spinners: false
								});
							}
						}
					};
				}

				switch (tType) {
					case "number":
					if ( columns.attributes !== undefined ) {
						if ( columns.attributes.style !== undefined )
						cols.attributes.style = columns.attributes.style + ";text-align:right;";
						else
						cols.attributes.style = "text-align:right;";
					}
					else {
						cols.attributes = {};
						cols.attributes.style = "text-align:right;";
					}
					
					break;

					case "string":
					if ( columns.attributes ) {
						if ( columns.attributes.style ) {
							cols.attributes.style += cols.align ? ";text-align:"+cols.align+";" : ";text-align:left;";
						}
						else {
							cols.attributes.style = cols.align ? ";text-align:"+cols.align+";" : "text-align:left;";
						}
					}
					else {
						cols.attributes = {};
						cols.attributes.style = cols.align ? ";text-align:"+cols.align+";" : "text-align:left;";
					}
					break;
				}
			}
		}

		var fnDataSourceMap = function(e) {
			var ds = e.sender.dataSource.data();
			scope.dataSourceMap = [];
			for ( var i = 0; i < ds.length; i++ ) {
				scope.dataSourceMap[ds[i].uid] = i;
			}
		}

		var editorColumns = [];
		var fnRowColorBackground = scope.bcsConf['rowColorBackground'];
		var fnRowCellColor = scope.bcsConf['rowCellColor'];
		var fnHideDetailButton = scope.bcsConf['hideDetailButton'];
		var fnNotEditableColumns = scope.bcsConf['notEditableColumns'];
		var fnBoldRow = scope.bcsConf['boldRow'];

		var gridDataBound = function(e) {
			fnDataSourceMap(e);

			showGroupButtons(scope.bcsgrid);
			
			
			//virtual
			if(!!scope.bcsgrid && !!scope.virtualScroll){//if(rows.length > 0) {
				var grid = scope.bcsgrid;
				//scope.bcsgrid.selectable.userEvents._events.move = null;
				if(scope.dataSource.firstLoad) { //scope.virtualScroll.firstLoad) {
					scope.bcsgrid.selectable.userEvents._events.move = null;
					kendo.ui.progress(grid.element, true);

					//scope.virtualScroll.firstLoad = false;

					scope.virtualScroll.load = scope.dataSource.totalPages();
					scope.virtualScroll.pages = scope.dataSource.totalPages();


					scope.dataSource.firstLoad = false;
				}

				if(scope.virtualScroll.load > 0) {
					setTimeout(function() {
						console.log("pagina inicial a cache", scope.virtualScroll.load);
						var pageSize = scope.dataSource.pageSize();                                     
						scope.bcsgrid.dataSource.range((scope.virtualScroll.load-1)*pageSize, pageSize, $.noop); 
						scope.virtualScroll.load--;
						if (scope.virtualScroll.load == 0) {
							scope.virtualScroll.mapRanges = [];
							scope.virtualScroll.mapRangesValues = [];
							var r = scope.bcsgrid.dataSource._ranges;
							var n = 0;
							for (var i = 0; i < r.length; i++)
								for (var j = 0; j < r[i].data.length; j++) {
									scope.virtualScroll.mapRanges[r[i].data[j].uid] = n++;
									scope.virtualScroll.mapRangesValues[r[i].data[j].uid] = r[i].data[j];
								}
							kendo.ui.progress(scope.bcsgrid.element, false);
						}
					},0);
				}


				/*if (scope.virtualScrollableFirstLoad) {
					var pageSize = grid.dataSource.pageSize();
					console.log(pageSize);
					scope.virtualScrollableFirstLoad = false;
				}*/
				
				var pageData = grid.dataSource.view();
				var newSelection = [];
				for (var i = 0; i < pageData.length; i++) {
					if (scope.allSelected[pageData[i].uid]) {
						newSelection.push("tr[data-uid='" + pageData[i].uid + "']");
					}
				}
				grid.select(newSelection.join());
				console.log('newSelection: ' + newSelection.join(), newSelection);
			}
			//virtual

			var eventData = e.sender;
			var groupingElement = eventData.element.find('.k-grouping-header');
			if(groupingElement.find('.k-group-indicator').length > 0) {
				var rows = eventData.dataSource.data();
			}
			else {
				var rows = eventData.dataSource.view();
			}
			if(eventData.dataSource.data().length) {
				var htmlRows = eventData.tbody.find('tr'); //('tr.k-master-row');
				
				if (eventData.lockedContent != null) {
					var htmlRowsLocked = eventData.lockedContent.find('tr');
				}				
				
				var hashRows = [];
				var hashRowsLocked = [];
				for (var i = 0; i < htmlRows.length; i++) {
					if(typeof $(htmlRows[i]).attr('data-uid') !== "undefined") {
						hashRows[$(htmlRows[i]).attr('data-uid')] = $(htmlRows[i]);
					}

					if(eventData.lockedContent != null && typeof $(htmlRowsLocked[i]).attr('data-uid') !== "undefined") {
							hashRowsLocked[$(htmlRowsLocked[i]).attr('data-uid')] = $(htmlRowsLocked[i]);
					}
					
				}
				for (var i = 0; i < rows.length; i++) {
					// Callbacks

					// rowColorBackground
					if (fnRowColorBackground) {
						var color = fnRowColorBackground(rows[i]);
						if ( color !== undefined || color !== "" ) {
							if(typeof hashRows[rows[i].uid] !== "undefined") {
								hashRows[rows[i].uid].addClass(color);
							}
							if(eventData.lockedContent != null && typeof hashRowsLocked[rows[i].uid] !== "undefined") {
								hashRowsLocked[rows[i].uid].addClass(color);
							}							
						}
					}

					// boldRow
					if (fnBoldRow) {
						var bold = fnBoldRow(rows[i]);
						if ( bold ) {
							if(typeof hashRows[rows[i].uid] !== "undefined") {
								hashRows[rows[i].uid].css("font-weight","bold");
								hashRows[rows[i].uid][0].style.setProperty("font-weight", "bold");
								hashRows[rows[i].uid].addClass("boldRow");
							}
							
							if(eventData.lockedContent != null && typeof hashRowsLocked[rows[i].uid] !== "undefined") {
								hashRowsLocked[rows[i].uid].css("font-weight","bold");
								hashRowsLocked[rows[i].uid][0].style.setProperty("font-weight", "bold");
								hashRowsLocked[rows[i].uid].addClass("boldRow");
							}
							
						}
					}
					


					// rowCellColor
					if (fnRowCellColor) {
						var colorObj = fnRowCellColor(rows[i]);
						if ( !!colorObj && colorObj.column !== "" && colorObj.color !== "" && !angular.isArray(colorObj)) {
							if(typeof hashRows[rows[i].uid] !== "undefined") {
								//hashRows[rows[i].uid].addClass(color);
								hashRows[rows[i].uid].find('[ng-bind="dataItem.' + colorObj.column + '"]').parent().addClass(colorObj.color + "-cell");
								hashRows[rows[i].uid].find('[bcs-bind="dataItem.' + colorObj.column + '"]').parent().addClass(colorObj.color + "-cell");
							}
						}
						else if ( !!colorObj && angular.isArray(colorObj) && colorObj.length > 0) {
							if(typeof hashRows[rows[i].uid] !== "undefined") {
								for (var xx = 0 ; xx < colorObj.length ; xx++) {
									hashRows[rows[i].uid].find('[ng-bind="dataItem.' + colorObj[xx].column + '"]').parent().addClass(colorObj[xx].color + "-cell");
									hashRows[rows[i].uid].find('[bcs-bind="dataItem.' + colorObj[xx].column + '"]').parent().addClass(colorObj[xx].color + "-cell");
								}
							}
						}
					}

					// hideDetailButton
					if(!_.isUndefined(fnHideDetailButton) && fnHideDetailButton(rows[i])) {
						hashRows[rows[i].uid].find('td.k-hierarchy-cell .k-icon').removeClass();
					}

					// notEditableColumns
					if (fnNotEditableColumns) {
						var notEditableColumnsRow = fnNotEditableColumns(rows[i])
						if ( notEditableColumnsRow) {
							for (var j = 0; j < notEditableColumnsRow.length; j++) {
								if ( !editorColumns[notEditableColumnsRow[j]] ) {
									editorColumns[notEditableColumnsRow[j]] = true;
								}
								hashRows[rows[i].uid].find('[ng-bind="dataItem.' + notEditableColumnsRow[j] + '"]').parent().removeClass('editableCell');
								hashRows[rows[i].uid].find('[bcs-bind="dataItem.' + notEditableColumnsRow[j] + '"]').parent().removeClass('editableCell');
								hashRows[rows[i].uid].find('[bcs-bind="dataItem.' + notEditableColumnsRow[j] + '"]').removeClass('editableCell');
							}
						}
					}

					// FIN Callbacks
				}

				// Definición de editor de columnas (notEditableColumns)
				if (scope.bcsgrid) {
					var fields = scope.bcsgrid.dataSource.options.schema.model.fields;

					var cellEditor = function(f,c,o) {
						if(f[o.field].type === "number") {
							var decimals = kendo.culture().numberFormat.decimals;
							var format = "n" + decimals;
							var round = false;	
							if(!!o.model.__bcsFormat__ && !!o.model.__bcsFormat__[o.field]) {
								var bcsFormat = o.model.__bcsFormat__[o.field];
								format = "n" + bcsFormat.cantidadDecimales;
								decimals = bcsFormat.cantidadDecimales;
								round = bcsFormat.trucaRedondea === "T" ? false : true;
							}
							$('<input data-bind="value:' + o.field + '"/>')
								.appendTo(c)
								.kendoNumericTextBox({
									step: 0,
									format: format,
									spinners: false,
									decimals: decimals,
									restrictDecimals: true,
									round: round,
									value: o.model[o.field]
								});
						}
						else if(f[o.field].type === "date") {
							$('<input data-bind="value:' + o.field + '"/>')
								.appendTo(c)
								.kendoDatePicker();
						}
						else {
							$('<input class="k-textbox" data-bind="value:' + o.field + '"/>')
								.appendTo(c);
						}

						c.find("input").bind("focus", function () {
							var input = $(this);
							clearTimeout(input.data("selectTimeId"));

							var selectTimeId = setTimeout(function()  {
								input.select();
							});

							input.data("selectTimeId", selectTimeId);
						}).blur(function(e) {
							clearTimeout($(this).data("selectTimeId"));
						});

					};

					for (var i = 0; i < scope.bcsgrid.columns.length; i++) {
						if(editorColumns[scope.bcsgrid.columns[i].field]) {
							scope.bcsgrid.columns[i].editor = function(c, o) { //c -> container, o -> options
								if (!fnNotEditableColumns(o.model) || fnNotEditableColumns(o.model).indexOf(o.field) < 0) {
									cellEditor(fields,c,o);
								}
								else {
									if(!!o.model.__bcsFormat__ && !!o.model.__bcsFormat__[o.field] && o.model.__bcsFormat__[o.field].valor ) {
										$('<span>' + o.model.__bcsFormat__[o.field].valor + '</span>').appendTo(c);
									}
									else {
										$('<span>' + o.model[o.field] + '</span>').appendTo(c);
									}
								}
							}
						} else if (scope.bcsgrid.columns[i].editable) {
							var col = scope.bcsgrid.columns[i];
							var template = col.template;
						//	col.format = "{0:n3}";
							col.editor = function(c, o) { //c -> container, o -> options
								cellEditor(fields,c,o);
							}
						}
					}
				}

			}
			/*if (typeof scope.bcsgrid !== "undefined") {
				scope.bcsgrid.table.focus();
			}*/
			//console.log();
		}

		// dataBound
		var valueDataBound;
		if ( (valueDataBound = scope.bcsConf['dataBound']) !== undefined ) {
			var fnDataBound = function(e) {
				if (gridDataBound)
					gridDataBound(e);
				valueDataBound(e);
			}
			scope.KOptions['dataBound'] = fnDataBound;
		}
		else {
			if (gridDataBound)
			scope.KOptions['dataBound'] = gridDataBound;
		}

		// sortable
		if ( (valueSortable = scope.bcsConf['sortable']) !== undefined ) {
			scope.KOptions['sortable'] = valueSortable;
		}

		if ( (valuePageable = scope.bcsConf['pageable']) !== undefined ) {
			scope.KOptions['pageable'] = valuePageable;
		}

		if ( (valueHeight = scope.bcsConf['height']) !== undefined ) {
			scope.KOptions['height'] = valueHeight;
		}

		if( (valueSelectable = scope.bcsConf['selectable']) !== undefined ) {
			scope.KOptions['selectable'] = valueSelectable;
		}

		if( (valueAutoBind = scope.bcsConf['autoBind']) !== undefined ) {
			scope.KOptions['autoBind'] = valueAutoBind;
		}
		
		if( (valueExcel = scope.bcsConf['excel']) !== undefined ) {
			scope.KOptions['excel'] = valueExcel;
		}
		
		if( (valueAllowCopy = scope.bcsConf['allowCopy']) !== undefined ) {
			scope.KOptions['allowCopy'] = valueAllowCopy;
		}

		if( valueFilterable = scope.bcsConf['filterable'] ) {
			if(valueFilterable) {
				scope.KOptions['filterable'] = {
					mode : "row",
					operators: {
						string: {
							eq: "Es igual a",
							neq: "No es igual a",
							startswith: "Comienza con",
							contains: "Contiene",
							doesnotcontain: "No contiene",
							endswith: "Termina en"
						},
						number: {
							eq: "Es igual a",
							neq: "No es igual a",
							gte: "Es mayor o igual que",
							gt: "Es mayor que",
							lte: "Es menor o igual que",
							lt: "Es menor que"
						}
					}
				};
			}
			else {
				scope.KOptions['filterable'] = valueFilterable;
			}

			if (!scope.KOptions['filterable'].cell) 
				scope.KOptions['filterable'].cell = {};
			scope.KOptions['filterable'].cell.minLength = 50;
		}

		if( valueScrollable = scope.bcsConf['scrollable'] ) {
			scope.KOptions['scrollable'] = valueScrollable;
			scope.virtualScrollable = !!valueScrollable && !!valueScrollable.virtual;
			if(scope.virtualScrollable) {
				scope.virtualScroll = {
					//firstLoad : false,
					load : 0,
					pages : 0,
					allSelected : {}
				}
				scope.KOptions.pageable = {
					previousNext: false,
					numeric: false,
					messages: { display: "Elementos mostrados {2}" }
				};
				scope.KOptions.autoPageLoad = 100;
			}
		}

		if( valueReorderable = scope.bcsConf['reorderable'] ) {
			scope.KOptions['reorderable'] = valueReorderable;
		}

		if( valueNavigatable = scope.bcsConf['navigatable'] ) {
			scope.KOptions['navigatable'] = valueNavigatable;
		}

		if( valueSave = scope.bcsConf['save'] ) {
			scope.KOptions['save'] = valueSave;
		}

		if( valueDetailExpand = scope.bcsConf['detailExpand'] ) {
			scope.KOptions['detailExpand'] = valueDetailExpand;
		}

		/*scope.KOptions['edit'] = function(e) {
			var numeric = e.container.find("input[name=IdTransaccion]").data("kendoNumericTextBox");
			console.log(numeric);

			//e.container.find("input[name=IdTransaccion]").attr("readonly", true);
			e.container.find("input[name=IdTransaccion]").attr("enable", false);
			//numeric.enable(false);
		}*/
	}

	function preLink(scope, elem, attrs, ctrl,transclude) {

		Object.defineProperty(scope,'dataSource', {enumerable: false, writable: true});

		scope.idHtml = "";
		
		if (!elem[0].id) {
			console.warn("grilla sin id definido")
		}
		else {
			scope.idHtml = "#" + elem[0].id;
			
			var menu = $(scope.idHtml+" #menu").kendoContextMenu({
				target : scope.idHtml,
				orientation: "vertical",
				select : function(e) {
					if (e.item.textContent === 'Exportar Todo') {
						scope.fnExportarExcel(undefined, true);
					}
					else if (e.item.textContent === 'Exportar Pagina Actual') {
						scope.fnExportarExcel(undefined, false);
					}
				},
				dataSource:
	                [{
	                    text: "Exportar Pagina Actual"
	                },{
	                    text: "Exportar Todo"
	                }]
			});
		}
		
		
		$timeout(function() {

			var dataItem, conf;
			if ( (dataItem = scope.$parent.dataItem) && (conf = attrs.detalleConfiguracion) ) {
				var confFn = conf.indexOf("(");
				var conf = confFn === -1 ? conf : $.trim(conf.substring(0,confFn));
				var arrayConf = conf.split('.');
				var parent = scope.$parent;
				while (parent != null && parent[arrayConf[0]] == undefined) {
					parent = parent.$parent;
				}
				if ( parent != null ) {
					var objConf = parent;
					for (var i = 0; i < arrayConf.length; i++) {
						objConf = objConf[arrayConf[i]];
					}
				//	scope.bcsConf = parent[conf](dataItem);
					scope.bcsConf = objConf(dataItem);
				}
			}

			if(!scope.bcsConf) {
				scope.bcsConf = {};
			}
			scope.bcsConf._dataSource = {};
			angular.copy(scope.bcsConf.dataSource, scope.bcsConf._dataSource);
			scope.detailContent = "";
			transclude(scope, function(clone, scope) {
				var detailContent = "";
				for( var i = 0; i < clone.length; i++) {
					detailContent += clone[i].outerHTML || '';
				}
				var tag = 'div[ng-non-bindable][bcs-grid-detail]';
				if ($('<div>'+detailContent+'</div>').find("div[ng-non-bindable][bcs-grid-detail]").html() !== undefined) {
					var det = $(detailContent);
					var innerGrid = det.find('bcs-grid');
					for ( var i = 0; i < innerGrid.length; i++ ) {
						var attr;
						// TODO Eliminar attr.indexOf('grid.')
						if ( (attr = $(innerGrid[i]).attr('configuracion')) != undefined 
							&& attr.indexOf('grid.') === -1 ) {
							$(innerGrid[i]).attr({'detalle-configuracion': attr});
							$(innerGrid[i]).removeAttr('configuracion');
						}
					}
					scope.detailContent = det.html();
					scope.detailContentValue = $(detailContent).attr("bcs-grid-detail");
				}
			});

			gridConfig(scope, elem);
			scope.element = elem;
		});

		scope.$on("kendoWidgetCreated", function (event, widget) {

			if ( widget.options.name.toLowerCase() === "grid" ) {
				apiGrid(scope, widget.element);
				//kendo.support.scrollbar = function () { return 9; };
				//scope.bcsgrid.refresh();
				
				/*if ( widget.element.find(".k-grid-header").length > 0 ) {
					widget.element.find(".k-grid-header").removeAttr( "style" );
				}*/

				if ( widget.element.find(".k-grid-footer").length > 0 ) {
					widget.element.find(".k-grid-footer").attr( 'style', 'display:none;' );
				}

				if( scope.bcsConf['contextMenu'] && !_.isUndefined(widget.element.attr("kendo-context-menu")) ) {
					scope.contextMenu = widget.element.data("kendoContextMenu");
					scope.contextMenu.append(
						[{
							text: "Preferencias"//,
							// cssClass: "border-bottom: 1px white solid;" // Separador
						}/*
						Cortar
						Copiar
						Copiar tabla
						Pegar*/
						]
					);
				}
				if(scope.firstRender === true) {
					/*if ( scope.bcsConf['gridCreated'] !== undefined ) {
						var grid = {};
						grid.dataSource = {};
						$.extend(grid, scope.bcsApi);
						var d = {};
						scope.bcsConf['gridCreated'](grid,scope.bcsgrid);
					}*/

					showGroupButtons(widget);
					
					if(!!scope.bcsConf.hideGroupingHeader) {
						$(widget.element).children(".k-grouping-header").remove();
						//$(widget.element).children(".k-grouping-header").find(".k-button.k-button-icon.k-button-bare").remove();
					}
					
					if(!scope.bcsConf.autoBind) {
						scope.bcsgrid.refresh(); //JIRA http://jira.bcs.cl/browse/SAP-5479
					}

					if(!!scope.virtualScroll && !!scope.bcsConf.autoBind) {
						//scope.bcsgrid.selectable.userEvents._events.move = null;
						//scope.bcsgrid.dataSource.pageSize(50);
						//scope.dataSource.pageSize(50);
						scope.dataSource.firstLoad = true;
					}/*
					if(!!scope.virtualScroll) {
						var dsOptions = scope.dataSource.getOptions();
						dsOptions.definition.pageSize = 50;
						scope.dataSource.setOptions(dsOptions);
						scope.dataSource.firstLoad = true;
					}

					if(!scope.virtualScrollableFirstLoad && scope.bcsgrid.element.is(':visible')) {
						scope.virtualScrollableFirstLoad = true;
					}*/

					var tabPaneHtml = $(widget.element.closest(".tab-pane"));
					var tabPaneScope = angular.element(tabPaneHtml).scope();

					if(!!tabPaneScope) {
						var tabContentScope = tabPaneScope.$parent;
						var tabScope = tabContentScope.$parent;
/*
						tabPaneScope.tab.index = tabPaneScope.$index;
						if(!tabPaneScope.tab.bcsgrids) {
							tabPaneScope.tab.bcsgrids = [];
						}
						if(tabPaneScope.tab.active) {
							tabPaneScope.tab.gridsRefreshed = true;
						}
						tabPaneScope.tab.bcsgrids.push(widget);*/
						
						var tab = tabPaneScope.tab;

						var navtabs = $(tabPaneHtml.parent().parent().children()[0]).children();
						for(var i = 0; i < navtabs.length; i++) {
							if(_.isEqual(tab,tabContentScope.tabs[i]) && !tab.bcsgrids){
								var navtab = $(navtabs[i]);
								//console.log(navtab.attr("heading"), tab.heading);
								if(navtab.attr('select')) {
									console.log("Tab: [", navtab.attr("heading"),',', tab.heading,"] con evento select");
									continue;
								}
								tab.index = tabPaneScope.$index;
								if(!tab.bcsgrids) {
									tab.bcsgrids = [];
								}
								if(tab.active) {
									tab.gridsRefreshed = true;
								}
								tab.bcsgrids.push(widget);
								
								tab.select = function() {
									(tab.disabled||(tab.active=!0));
									
									console.log(navtab.attr("heading"), tab.heading);
									if(tab.active && !tab.gridsRefreshed && !!tab.bcsgrids) {
										console.log(tab.index, tab.heading, tab.bcsgrids);
										var _tab = tab;

										var gridsRefresh = function() {
											console.log(_tab.index, _tab.heading, _tab.bcsgrids);
											var bcsgrids = _tab.bcsgrids;
											for(var j = 0; j < bcsgrids.length; j++) {
												bcsgrids[j].refresh();
											}
											_tab.gridsRefreshed = true;
										};

										$timeout(gridsRefresh,100);
									}
									
								}
							}
							/*
							var navtab = $(navtabs[i]);
							if(!navtab.attr('select')) {
								navtab.attr('select','selectGrid("")');
								$injector = navtab.injector();
								$injector.invoke(function($compile){
									$compile(navtab)(navtab.scope())
								});
							}*/
						}/*
					
						tabPaneScope.tab.index = tabPaneScope.$index;
						if(!tabPaneScope.tab.bcsgrids) {
							tabPaneScope.tab.bcsgrids = [];
						}
						if(tabPaneScope.tab.active) {
							tabPaneScope.tab.gridsRefreshed = true;
						}
						tabPaneScope.tab.bcsgrids.push(widget);

						var selectFn = function(tabScopeId) {
							var t = tabContentScope.tabs;
							for(var i = 0; i < t.length; i++) {
								var tab = t[i];
								if(!tab.active) continue;
								if(tab.active && !tab.gridsRefreshed && !!tab.bcsgrids) {
									console.log(tab.index, tab.heading, tab.bcsgrids);
									var _tab = tab;

									var gridsRefresh = function() {
										console.log(_tab.index, _tab.heading, _tab.bcsgrids);
										var bcsgrids = _tab.bcsgrids;
										for(var j = 0; j < bcsgrids.length; j++) {
											bcsgrids[j].refresh();
										}
										_tab.gridsRefreshed = true;
									};
									
									$timeout(gridsRefresh,1);
								}
							}
						}
						
						tabScope.select = selectFn;*/

					}

					if ( scope.bcsConf['gridCreated'] !== undefined ) {
						var grid = {};
						grid.dataSource = {};
						$.extend(grid, scope.bcsApi);
						var d = {};
						scope.bcsConf['gridCreated'](grid,scope.bcsgrid);
					}

					scope.firstRender = false;
				}
				delete scope.bcsConf._dataSource;
				//userSettings(scope, widget.element);
				if ( scope.bcsConf['userSettings'] !== undefined && scope.bcsConf['userSettings'] && !scope.bcsApi.isNestedGrid()) {
					var bcsgridconf = widget.element.parent().find(".bcsgridconf");
					if ( widget.element.find(".k-grouping-header").length > 0 ) {
						bcsgridconf.attr('style','display:block;top:29px;border-top:solid 1px #c9c9c9;');
					}
					else {
						bcsgridconf.attr('style','display:block; padding: 0px !important');
					}

					var gridUserSettings = null; //(scope.bcsApi.getGridId() && scope.bcsApi.getGridId().trim()!="") ? sesion.getPreferencias("GRID_" + scope.bcsApi.getGridId()) : undefined;
					var gridDefaultSettings = null; //(scope.bcsApi.getGridId() && scope.bcsApi.getGridId().trim()!="") ? sesion.getPreferencias("DEFAULT-GRID_" + scope.bcsApi.getGridId()) : undefined;

					if((scope.bcsApi.getGridId() && scope.bcsApi.getGridId().trim()!="")) {
						gridUserSettings = sesion.getPreferencias("GRID_" + scope.bcsApi.getGridId());
						if(typeof sesion.getPreferencias("DEFAULT-GRID") !== "undefined")
							gridDefaultSettings = sesion.getPreferencias("DEFAULT-GRID")[BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' + scope.bcsApi.getGridId()];
					}

					if(!!gridUserSettings) { //if(typeof gridUserSettings !== 'undefined') {

						if(_.isString(gridUserSettings)) {
							try{
								gridUserSettings = JSON.parse(gridUserSettings);
							}
							catch(error) {
								gridUserSettings = { columns : [] };
								console.log(error);
							}
						}
						scope.bcsApi.setConfiguration(gridUserSettings);
						apiGrid(scope, widget.element);
						if ( scope.bcsConf['gridCreated'] !== undefined ) {
							var grid = {};
							grid.dataSource = {};
							$.extend(grid, scope.bcsApi);
							var d = {};
							scope.bcsConf['gridCreated'](grid,scope.bcsgrid);
						}
						scope.bcsgrid.refresh();
					}
					else if(!!gridDefaultSettings) {

						if(_.isString(gridDefaultSettings)) {
							try{
								gridDefaultSettings = JSON.parse(gridDefaultSettings);
							}
							catch(error) {
								gridDefaultSettings = { columns : [] };
								console.log(error);
							}
						}
						scope.bcsApi.setConfiguration(gridDefaultSettings);
						apiGrid(scope, widget.element);
						if ( scope.bcsConf['gridCreated'] !== undefined ) {
							var grid = {};
							grid.dataSource = {};
							$.extend(grid, scope.bcsApi);
							var d = {};
							scope.bcsConf['gridCreated'](grid,scope.bcsgrid);
						}
						scope.bcsgrid.refresh();
					}
					
					scope.bcsgridconf = function() {
						console.log('bcsgridconf');
						var grid = {};
						grid.dataSource = {};
						$.extend(grid, scope.bcsApi);
						var parent = scope.$parent;
						var winId = null;
						while (parent != null && winId == null) {
							winId = parent.ventanaid;
							parent = parent.$parent;
						}
						scope.$parent.$parent.ventanaid;
						bcsComunes.ventanaWinId(scope);
						var id1 = bcsComunes.ventanaAbrir("bcsGridConf",{ confOptions: grid },false,false,winId);
						bcsComunes.ventanaTitulo(id1,"Preferencias (grilla seleccionada)");
						bcsComunes.ventanaCambiarTamano(id1, bcsComunes.getAncho(id1), bcsComunes.getAlto(id1)+90);
						var promesa = bcsComunes.ventanaPromesa(id1);/*					
						$.when(promesa).then(function(data) { // JIRA SAP-5247
							bcsComunes.msg("info","info", '' + data );
						});*/
					};
				}
			}

			if ( widget.options.name.toLowerCase() === "contextMenu" ) {
				console.log('grid contextMenu')
			}
		});

	}

	function postLink(scope, elem, attrs, ctrl,transclude) {

		if(scope.bcsConf!==undefined){
			//cantidad de filas estandar para todas las grillas del sistema.
			scope.bcsConf.dataSource.definition.pageSize = (scope.$root.SesionValoresDefectoSistema.CantidadRegistrosDatasource!==undefined) ? scope.$root.SesionValoresDefectoSistema.CantidadRegistrosDatasource : scope.bcsConf.dataSource.definition.pageSize;
		}

		$timeout(function() {
			elem.find("div[bcs-grid-detail]").remove();
		//	elem.find(".k-grid-toolbar").remove(); //Quita el toolbar para exportar excel.
			scope.elemPosition =elem;
			var idVentana = bcsComunes.ventanaWinId(scope);

			//exportar a excel
			scope.fnExportarExcel = function(fileName, allPages) {
				if(!fileName){
					var ventana = $translate.instant(BCS_VENTANA[scope.elemPosition.closest("[winKey]").attr("winKey")].TITLE);
					var fecha = util.formatoDate(new Date());
					var tab = $(scope.elemPosition.closest("[winkey]")).find("[heading].active").attr("heading");
					fileName = (ventana+"_")+( (tab)?tab+"_" : "" )+(fecha);	
				}
				
				//restaurar propiedad original (fallback para funciones excel del menu opciones)
				scope.bcsgrid.options.excel.allPages = scope.fallbackConfiguracionExcel;

				allPages = (allPages!==undefined) ? allPages : (scope.bcsgrid.options.excel.allPages!==undefined) ? scope.bcsgrid.options.excel.allPages : false;
												
				//obtener cantidad de columnas de la grilla
				function getColumnasExcel(obj){
					var column = {};
					column.field = obj.field;
					column.title = obj.title;
					column.groupTitle = obj.groupTitle;
					return column;
				}
				//aplanar columnas (en caso de que exista columnas agrupadas)
				var columnasAplanadas = [];
				(scope.bcsApi.getOptions().columns!==undefined ? scope.bcsApi.getOptions().columns : [] ).forEach( element => {
					if(element.columns!==undefined){
						element.columns.forEach( subElement => {
							subElement.groupTitle = element.title;
							columnasAplanadas.push(subElement);
						});
					}else{
						columnasAplanadas.push(element);
					}
				});
				var columnas = columnasAplanadas.filter((column) => column.hidden===undefined || column.hidden===false ).map( (column) => getColumnasExcel(column));
				var filasMaximas = scope.bcsConf.dataSource.definition.pageSize;
				var cantidadColumnas = (columnas!==undefined) ? columnas.length : -1;
				var cantidadFilas = (scope.bcsApi.dataSource.data().length > filasMaximas ) ? filasMaximas : scope.bcsApi.dataSource.data().length;
				var cantidadFilasTotal = scope.bcsgrid.dataSource._total;
				
				if(scope.bcsApi.dataSource.data().length>0){
					//si la grilla es pequeña o es de tipo array o pide exportar solo la hoja actual
					// primer caso, imprimir online si se solicita imprimir la hoja actual y el resultado es pequenio
					// segundo caso, imprimir online si se solicita imprimir todo y el resultado es pequenio
					// tercer caso, imprimir de forma batch todas las demas posibilidades
					scope.bcsApi.dataSource.definition.type= (scope.bcsApi.dataSource.definition.type===undefined) ? "" : scope.bcsApi.dataSource.definition.type;
					if( ((cantidadColumnas * cantidadFilas) < 15000  && !allPages) || scope.bcsApi.dataSource.definition.type.toLowerCase()==="array" || ((cantidadColumnas * cantidadFilasTotal)<15000 && allPages)){
						
						scope.bcsgrid.options.excel.allPages = allPages;

						scope.excelFileName = fileName;
						scope.bcsgrid.saveAsExcel();
						bcsComunes.msg("info","info", $translate.instant('UI_EXPORTACION_DATOS_GENERADA'));	
					}else{

						//Se va a generar un excel muy grande, por lo tanto, generar el excel en modo batch
						//obtener parametros del servicio get
						var parametros = {};
						//obtener columnas
						parametros.columns = columnas;
						//agregar el tipo de dato a las columnas
						for( var k in parametros.columns){
							if(parametros.columns[k].field!==undefined)
								parametros.columns[k].type = scope.bcsApi.dataSource.definition.schema.model.fields[parametros.columns[k].field].type;
								parametros.columns[k].bcstype = scope.bcsApi.dataSource.definition.schema.model.fields[parametros.columns[k].field].bcsType;
						}

						//parametros url
						if(scope.bcsApi.dataSource.definition.callTransaction!==undefined){
							//es call o java
							parametros.t = scope.bcsApi.dataSource.definition.callTransaction;
							parametros.r = scope.bcsApi.dataSource.definition.callResultSet;
							parametros.callParameters = scope.bcsApi.dataSource.lastCallParameters;
							parametros.gridFilters = scope.bcsConf.dataSource.filter() ? scope.bcsConf.dataSource.filter().filters : undefined;
							parametros.gridSort = scope.bcsConf.dataSource.sort() && scope.bcsConf.dataSource.sort().length > 0 ? scope.bcsConf.dataSource.sort() : undefined;

							if(parametros.gridFilters && parametros.callParameters){
								parametros.gridFilters.forEach(gridFilter => {
								    gridFilter.type = gridFilter.value ? typeof gridFilter.value : undefined;
								    if(gridFilter.type && gridFilter.type === 'object'){
										try{
											gridFilter.type = "date";
											bcsType = parametros.columns ? parametros.columns.find(column => column.field === gridFilter.field) : undefined;
											gridFilter.bcsType = bcsType.bcstype;
										} catch (error) {
											console.log(error);
										}
									}
								});
							}
							
							if(scope.bcsApi.dataSource.definition.serverPaging && scope.bcsApi.dataSource.definition.serverSorting && scope.bcsApi.dataSource.definition.schema
									&& scope.bcsApi.dataSource.definition.schema.data && scope.bcsApi.dataSource.definition.schema.total && scope.bcsApi.dataSource.definition.sort) {
								if (angular.isArray(scope.bcsApi.dataSource.definition.sort)) {
									parametros.gridSort = scope.bcsApi.dataSource.definition.sort;
								} else {
									parametros.gridSort = [scope.bcsApi.dataSource.definition.sort];
								}
							}
						}else{
							//es vista
							//obtener idServicio
							_.first(scope.bcsApi.dataSource.definition.transport.read.url.split("?")
							.filter( (path) => path.indexOf("t=")>=0 ))
							.split("&")
							.forEach( (params) => {
											splitParam = params.split("=");
											parametros[splitParam[0]] = splitParam[1];
										});
							
							if(scope.bcsApi.dataSource.definition.sort){
								if(scope.bcsApi.dataSource.definition.sort.field){
									var Orderby = scope.bcsApi.dataSource.definition.sort.field.trim();
									if(scope.bcsApi.dataSource.definition.sort.dir){
										if(scope.bcsApi.dataSource.definition.sort.dir.trim().length > 0){
											Orderby = Orderby + " " + scope.bcsApi.dataSource.definition.sort.dir.trim();
										}
									}
									if(scope.bcsApi.dataSource.definition.lastCallParameters){
										if(scope.bcsApi.dataSource.definition.lastCallParameters.$orderby){
											scope.bcsApi.dataSource.definition.lastCallParameters.$orderby = Orderby;
										}
									}
								}
								
							}
							//obtener los ultimos filtros
							parametros.odataParameters = scope.bcsApi.dataSource.definition.lastCallParameters;
							if(allPages){
								delete parametros.odataParameters.$top;
							}
							//parametros.odataParameters = kendo.data.transports.odata.parameterMap(scope.bcsApi.dataSource.filter());//sirve para exportar todas las hojas
						}
						
						console.log(parametros);
						//fix entrada al servicio
						parametros.callParameters = (parametros.callParameters!==undefined) ? parametros.callParameters : {};
						parametros.odataParameters = (parametros.odataParameters!==undefined) ? parametros.odataParameters : {};
						parametros.t = (parametros.t!==undefined) ? parametros.t : undefined;
						parametros.gridFilters = (parametros.gridFilters) ? parametros.gridFilters : {};
						parametros.gridSort = (parametros.gridSort) ? parametros.gridSort : {};

						if(!!parametros.callParameters!==undefined || !!parametros.odataParameters || !!parametros.t){

							var batchParameters = {
								p_IdEmpresa : scope.$root.SesionIdEmpresa,
								p_IdUsuario: scope.$root.SesionIdUsuario,
								p_IdModulo  : 'PLA',
								p_TipoPrograma : [{
									IdInterno : 0,
									IdTipoPrograma : 'EXPORTAR_GRILLA_CSV',
									SegundosDesfase : 0,
									Comentario: "Archivo Exportado: " + fileName
								}],
								p_Parametro : [{
									IdInterno : 0,
									NombreParametro: "t",
									Valor: parametros.t
								},{
									IdInterno : 0,
									NombreParametro: "r",
									Valor: (parametros.r!==undefined) ? parametros.r : ""
								},{
									IdInterno : 0,
									NombreParametro: "callParameters",
									Valor: JSON.stringify(parametros.callParameters)
								},{
									IdInterno : 0,
									NombreParametro: "odataParameters",
									Valor: JSON.stringify(parametros.odataParameters)
								},{
									IdInterno: 0,
									NombreParametro: "token",
									Valor: util.ObtenerToken()
								},{
									IdInterno: 0,
									NombreParametro: "columns",
									ValorBlob: window.btoa(unescape(encodeURIComponent( JSON.stringify(parametros.columns) )))

								},{
									IdInterno: 0,
									NombreParametro: "fileName",
									Valor: fileName
								},{
									IdInterno: 0,
									NombreParametro: "gridFilters",
									Valor: JSON.stringify(parametros.gridFilters)
								},{
									IdInterno: 0,
									NombreParametro: "gridSort",
									Valor: JSON.stringify(parametros.gridSort)
								}]
							}
							
							
							bcsServicioPlataforma.Servicios().InscribirProcesoBatch(batchParameters).$promise.then(
							function(result) {
								bcsComunes.ventanaOverlay(idVentana,false);
								var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
								bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
									conflictos : util.parseServerResponse(result)}, idVentana);

							},
							function(result){
								bcsComunes.ventanaOverlay(idVentana,false);
								bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
							});

							console.log(batchParameters);

//							bcsComunes.msg("info","info", $translate.instant('UI_ERROR_AL_EXPORTAR_EXCEL'));		
						}else{
							bcsComunes.msg("info","info", $translate.instant('UI_EXPORTACION_BATCH'));		
						}
					}
				}else{
					bcsComunes.msg("info","info", $translate.instant('UI_NO_HAY_INFORMACION_PARA_EXPORTAR'));	

				
				}
				
				//	scope.dataSource.exportToExcel(scope.dataSource.definition.schema); // TODO las hojas
			}

			scope.fnExportarExcelTodo = function(fileName) {
				
			}
			
          // exportar a pdf

	         scope.fnExportarPdf = function(fileName) {

             if(!fileName){
             	var ventana = $translate.instant(BCS_VENTANA[scope.elemPosition.closest("[winKey]").attr("winKey")].TITLE);
                var fecha = util.formatoDate(new Date());
                var tab = $(scope.elemPosition.closest("[winkey]")).find("[heading].active").attr("heading");
                fileName = (ventana+"_")+( (tab)?tab+"_" : "" )+(fecha);
                }

         	 scope.pdfFileName = fileName;
       	     scope.bcsgrid.clearSelection();
       	     scope.bcsgrid.saveAsPDF();
             bcsComunes.msg("info","info", $translate.instant('UI_EXPORTACION_DATOS_GENERADA'));

            }

			//click
			elem.on("click", function (e) {
				if (typeof $(e.target).attr("kendo-button") !== "undefined") {
					$(e.currentTarget).find("[kendo-grid]").data("kendoGrid").table.blur();
				}
				else if (($(e.target).is('span') && $(e.target).hasClass("ng-binding") && $(e.target).parent().length > 0) || $(e.target).hasClass("k-grouping-header")) {
					var kendoGrid = $(e.currentTarget).find("[kendo-grid]");
					if (kendoGrid.length > 0) {
						kendoGrid.data("kendoGrid").table.focus();
					}
				}
				var tg;
				if (e !== undefined)
				{
					if ($(e.target).prop('nodeName') === "TD")
					tg = $(e.target);
					if ($(e.target).parent().prop('nodeName') === "TD")
					tg = $(e.target).parent();
				}
				if (tg)
				{
					scope.fnClick();
					e.stopPropagation();
				}

				console.log("clicked", e.ctrlKey, e.altKey, e.shiftKey);
				scope.shiftKey = e.shiftKey;
				if(!e.ctrlKey && !e.shiftKey && !!scope.seleccionado) {

					scope.allSelected = {};
					for(var i=0; i < scope.seleccionado.length; i++) {
						scope.allSelected[scope.seleccionado[i].uid] = true;
					}
					console.log('limpia scope.allSelected');
				}
				if(e.shiftKey && !!scope.seleccionado && !!scope.virtualScroll) {
					var s1 = Object.keys(scope.allSelected);
					var s1Min = scope.virtualScroll.mapRanges[s1[0]];
					var s1Max = scope.virtualScroll.mapRanges[s1[0]];
					for(var i = 1; i < s1.length; i++) {
						if(scope.virtualScroll.mapRanges[s1[i]] <= s1Min)
							s1Min = scope.virtualScroll.mapRanges[s1[i]];
						if(scope.virtualScroll.mapRanges[s1[i]] >= s1Max)
							s1Max = scope.virtualScroll.mapRanges[s1[i]];
					}

					var s2 = scope.seleccionado;
					var s2Min = scope.virtualScroll.mapRanges[s2[0].uid];
					var s2Max = scope.virtualScroll.mapRanges[s2[0].uid];
					for(var i = 1; i < s2.length; i++) {
						if(scope.virtualScroll.mapRanges[s2[i].uid] <= s2Min)
							s2Min = scope.virtualScroll.mapRanges[s2[i].uid];
						if(scope.virtualScroll.mapRanges[s2[i].uid] >= s2Max)
							s2Max = scope.virtualScroll.mapRanges[s2[i].uid];
					}
					console.log("shift y seleccionado", s1Min, s1Max, s2Min, s2Max);

					scope.allSelected = {};
					var m = Object.keys(scope.virtualScroll.mapRanges);

					if(s1Max === s2Max) {	
						console.log(":)",  s1Min, s2Max);
						for(var i = s1Min; i <= s2Max; i++) {
							scope.allSelected[m[i]] = true;
							scope.seleccionado[i] = scope.virtualScroll.mapRangesValues[m[i]];
						}
					}
					else{
						console.log(":)", s2Max, s1Max);
						for(var i = s2Max; i <= s1Max; i++) {
							scope.allSelected[m[i]] = true;
							scope.seleccionado[i] = scope.virtualScroll.mapRangesValues[m[i]];
						}
					}

				}
				console.log('scope.allSelected: ',scope.allSelected);
				//scope.bcsgrid.refresh();
			});

			// Resize grid desde ventana 
				
			let aux = bcsComunes.obtenerIdVentanaGrid(elem[0].id);
			if(elem[0].id){
				
				elem.find('.gridResize').css("display", "block")
			}


			aux.find('.k-resize-handle.k-resize-s').on("mousedown", aux, function(event) {
				aux.css("height", "auto")
				// let form = aux.find('.k-window-content.k-content');
				// form.css("overflow", "hidden")
				let tab = aux.find('.tab-pane.ng-scope.active').not('tr.k-detail-row .tab-pane.ng-scope.active');
				if (tab.length === 0){ // caso en el cual la grilla se encuentra en una ventana sin tabs
					if (event.clientY >= (elem.offset().top + elem.height() - 10)) {
						let grid = aux.find("[kendo-grid]").not('tr.k-detail-row.ng-scope [kendo-grid]');
						if(grid.length > 1){
							// form.css("overflow","auto");
							return
						}
						
						let contentGrid = elem.find(".k-grid-content.k-auto-scrollable").not("tr.k-detail-row .k-grid-content.k-auto-scrollable");
						let gridHeight = elem.height();
						let alturaContentGrid = contentGrid.height();
						let originalY = event.pageY;
						let dif = gridHeight - alturaContentGrid;
						
						
						
						let lockCell = elem.find('.k-grid-content-locked')

						if(lockCell.length != 0){
							heightLock = lockCell.height()
						}
						
					
						angular.element(document).on("mousemove", function(e) {
						  let newHeight = gridHeight + (e.pageY - originalY);
						  let newHeightContent = newHeight - dif;
						  let newLockHeight = null;
						  if(lockCell.length > 0){
							newLockHeight = heightLock + (e.pageY - originalY);
						}
					
						  if (newHeight > 80 && newHeightContent > 80 && dif > 0) {
							grid.css("height", "auto");
							contentGrid.css("height", newHeightContent);
							if(newLockHeight){
								lockCell.css("height", newLockHeight);
							}
						  }
						}).one("mouseup", function() {
						  angular.element(this).off("mousemove");
						  aux.css("height", "auto")
						//   form.css("overflow","auto");
						});
					  }
					
				}
				else{ // caso cuando el grid se encuentra dentro de un tab
					if (event.clientY >= (elem.offset().top + elem.height() - 10)) {
						//let gridElement = aux.find(".tab-pane.ng-scope.active [kendo-grid]:not(tr.k-detail-row.ng-scope [kendo-grid])");
						let gridElement = aux.find('.tab-pane.ng-scope.active [kendo-grid]').not('tr.k-detail-row.ng-scope [kendo-grid]');
						if(gridElement.length > 1){
							// form.css("overflow","auto");
							return
						}
						
						let lockCell = aux.find('.tab-pane.ng-scope.active [kendo-grid] .k-grid-content-locked');
						
						let gridContentElement = aux.find('.tab-pane.ng-scope.active [kendo-grid] .k-grid-content.k-auto-scrollable').not('tr.k-detail-row.ng-scope [kendo-grid] .k-grid-content.k-auto-scrollable');

						if (gridElement.length == 0 && gridContentElement.length == 0){ // caso en el que una grilla se encuentre en un tab, pero la tabla es fuera del formulario
							gridElement = elem.find("[kendo-grid]")
							gridContentElement = elem.find(".k-grid-content.k-auto-scrollable");
							if(lockCell.length == 0){ // en el caso de no encontrar columnas bloqueadas
								lockCell = elem.find('.k-grid-content-locked')
							}
						}


						if(lockCell.length != 0){
							heightLock = lockCell.height()
						}

	
						let gridHeight = gridElement.height();
						let alturaContentGrid = gridContentElement.height();
						let originalY = event.pageY;
						let dif = gridHeight - alturaContentGrid;

	
						angular.element(document).on("mousemove", function(e) {
						let newHeight = gridHeight + (e.pageY - originalY);
						let newContentHeight = newHeight - dif;
						let newLockHeight = null;
						
						if(lockCell.length > 0){
							newLockHeight = heightLock + (e.pageY - originalY);
						}
		
						if (newHeight > 80 && newContentHeight > 80 && dif > 0) {
								gridElement.css("height", "auto");
								gridContentElement.css("height", newContentHeight);
								if(newLockHeight){
									lockCell.css("height", newLockHeight);
								}
						}
						}).one("mouseup", function() {
						angular.element(this).off("mousemove");
						aux.css("height", "auto");
						});
					}
				}
				});

				elem.find('.gridResize').on("mousedown", aux, function(event) { //resize desde la grilla
					aux.css("height", "auto")
						if (event.clientY >= (elem.offset().top + elem.height() - 10)) {
							let grid = elem.find("[kendo-grid]").not('tr.k-detail-row.ng-scope [kendo-grid]');
							if(grid.length > 1){
								return
							}
							
							//let contentGrid = elem.find(".k-grid-content.k-auto-scrollable").not("tr.k-detail-row .tab-pane.ng-scope.active .k-grid-content.k-auto-scrollable");
							let contentGrid = elem.find(".k-grid-content.k-auto-scrollable").not("tr.k-detail-row .k-grid-content.k-auto-scrollable");
							let gridHeight = elem.height();
							let alturaContentGrid = contentGrid.height();
							let originalY = event.pageY;
							let dif = gridHeight - alturaContentGrid;
							
							
							
							let lockCell = elem.find('.k-grid-content-locked')
	
							if(lockCell.length != 0){
								heightLock = lockCell.height()
							}
							
						
							angular.element(document).on("mousemove", function(e) {
							  let newHeight = gridHeight + (e.pageY - originalY);
							  let newHeightContent = newHeight - dif;
							  let newLockHeight = null;

							  if(lockCell.length > 0){
								newLockHeight = heightLock + (e.pageY - originalY);
							}
						
							  if (newHeight > 80 && newHeightContent > 80 && dif > 0) {
								grid.css("height", "auto");
								contentGrid.css("height", newHeightContent);
								if(newLockHeight){
									lockCell.css("height", newLockHeight);
								}
							  }
							}).one("mouseup", function() {
							  angular.element(this).off("mousemove");
							  aux.css("height", "auto")
							
							});
						  }
					});

	


			//doble click
			elem.on("dblclick", function (e) {
				var tg;
				if (e !== undefined)
				{
					if ($(e.target).prop('nodeName') === "TD")
					tg = $(e.target);
					if ($(e.target).parent().prop('nodeName') === "TD")
					tg = $(e.target).parent();
				}
				if (tg)
				{
					scope.fnDobleClick();
					e.stopPropagation();
				}
			});

			// click derecho
			if ( scope.bcsConf && scope.bcsConf['contextMenu']) {
				scope.contextMenuSelect = function(e) {
					//scope.selected = $(e.item).children(".k-link").text();
				//	console.log($(e.item).children(".k-link").text());
				};

				scope.contextMenuOpen = function(e) {
					//scope.selected = $(e.item).children(".k-link").text();
					//var contextMenu = $(e.item).data("kendoContextMenu");
					//contextMenu.destroy();
					/*contextMenu.append(
						[{
							id: "p1",
							text: "Preferencias",
							cssStyle: "border-bottom: 1px white solid;"
						},
						{
							text: "Item 1",
							cssClass: "myClass",                         // Add custom CSS class to the item, optional, added 2012 Q3 SP1.
							url: "http://www.telerik.com"                // Link URL if navigation is needed, optional.
						}]
					);*/
				//	console.log($(e.item).children(".k-link").text());
					// add menu
				};

				elem.on("mousedown", "tr[role='row']", function (e,data,dataItem,columns) {
					if (e.which === 3) {
						if ( $(this).find('.k-header').length > 0 ) {

						}
						else {
							$(this).siblings().removeClass("k-state-selected");
							$(this).addClass("k-state-selected");
							e.stopPropagation();
							//abrir menu contextual.
						}
					}
				});
			}

			//seleccionado
			scope.handleChange = function(event,data,dataItem,columns) {
				scope.seleccionado = ($.isArray(data))? _.uniq(data): data;
				if(!$(document.activeElement).is('input')) {
					event.sender.table.focus();
				}
				console.log("handleChange", data);

				$rootScope.PrimerValNull = true
				$rootScope.ItemSeleccionado = true

				//Ejecuta las funciones de Keydown y Keyup, Previene que se dispare la funcion en todo el DOM
				function addEvent(element, eventName, callback) {
					if (element.addEventListener) {
						element.addEventListener(eventName, callback, false);
					} else if (element.attachEvent) {
						element.attachEvent("on" + eventName, callback);
					} else {
						element["on" + eventName] = callback;
					}
				}	

				//virtual start
				var grid = this.bcsgrid;
				var gridView = grid.dataSource.view();
				var selection = this.bcsgrid.select();

				for (var i = 0; i < gridView.length; i++) {
					var id = gridView[i].uid;
					if (scope.allSelected[id] == true) {
						var isSelectedNow = false;
						for (var j = 0; j < selection.length; j++) {
							var row = grid.dataItem(selection[j]);
							if (row.uid == id) {
								isSelectedNow = true;
								break;
							}
						}
						if (!isSelectedNow) {
							scope.allSelected[id] = false;
						}
					}
				}

				for (var i = 0; i < selection.length; i++) {
					scope.allSelected[grid.dataItem(selection[i]).uid] = true;
				}
				//virtual end
			};
			scope.fallbackConfiguracionExcel = (scope.bcsgrid!==undefined && scope.bcsgrid.options!==undefined && scope.bcsgrid.options.excel!==undefined) ? scope.bcsgrid.options.excel.allPages : false;
			
			//scope.gridScope = scope;

		});

		/*scope.$watch("bcsConf", function(newval, oldval){
			if (newval != undefined && oldval != undefined &&
				!_.isUndefined(oldval._dataSource) &&
				!_.isUndefined(newval._dataSource) &&
				!_.isEqual(newval, oldval ) ) {
				var tmpnewval = bcs.clone(newval); // {};
				var tmpoldval = bcs.clone(oldval); // {};
				delete tmpnewval.dataSource;
				delete tmpoldval.dataSource;
				if ( !_.isEqual(tmpnewval, tmpoldval) ) {
					gridConfig(scope, elem);
					scope.bcsgrid.setOptions(scope.KOptions);
				}
			}
		},true);*/

		_.defer(function(){
			scope.$apply();
		});

	}

	return {
		//restrict: 'E',
		replace: false,
		transclude: true,
		scope: {
		//	gridScope : '@',
			bcsConf : '=?configuracion',
			bcsApi : '=?api',
			seleccionado : '=?',
			fnClick : '&',
			fnDobleClick : '&',
			fnExportarExcel : '=?',
			fnExportarPdf : '=?' // funcion de sacar foto a grilla y exportar a pdf
		},
		templateUrl: 'directives/bcs-grid/bcs-grid.html',
		link: {
			pre: preLink,
			post: postLink
		}
	};
	
});
