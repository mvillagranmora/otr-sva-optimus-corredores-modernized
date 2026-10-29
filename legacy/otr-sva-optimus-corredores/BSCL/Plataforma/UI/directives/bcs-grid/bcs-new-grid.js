bcs.newDataSource = function(properties) {

	var setCallTransport = function(ds, callTransaction, callResultSet) {
		ds.transport = {};
		ds.schema.data  = function(response) {
			if (ds.serverPaging === true) {
				return response.data;
			}
			else {
				return response[callResultSet];	
			}
			
		};
		ds.schema.parse  = function(response) {
			return response;
		};
		ds.schema.total = function(response) {
			if (ds.serverPaging === true) {
				return response.total;
			}
			else {
				return response[callResultSet].length;	
			}
		};
		ds.transport.parameterMap = function(data,type) {
			return kendo.stringify(that.lastCallParameters);			
		}
		ds.transport.read = {
			url: (function(options) {
				var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet;
				var skip = options.skip !== undefined? "&skip=" + options.skip : "";
				var take = options.take !== undefined? "&take=" + options.take : "";

                var sort = "";
				if(ds.serverSorting){
					if(Array.isArray(options.sort) && options.sort.length){
					    sort = "&sort=" + options.sort.map( c => {return c.field+"::"+c.dir}).pop();
					}else{
						sort = "&sort=" + ds.sort.field+"::"+ds.sort.dir;
					}
				}

				return "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet+skip+take+sort;
			}),
			dataType : "json",
			type : "POST",
			contentType:"application/json; charset=utf-8",
			data: function(options) {
				return {};
			}
		};
		/*if (ds.schema) {
			ds.schema.data  = function(response) {
				return response;
			};
			ds.schema.parse  = function(response) {
				return response;
			};
			ds.schema.total = function(response) {
				return response.length;
			}
		}*/
		

		/*ds.total = function(response) {
			return ds.data().length;
		};
		ds.transport = {
			read: function(options) {
				$.ajax({
					url: (function() {
						var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet;
						var skip = options.data.skip !== undefined? "&skip=" + options.data.skip : "";
						var take = options.data.take !== undefined? "&take=" + options.data.take : "";
						return "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet+skip+take;
						})(),
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
		};*/
	};

	
	var fixNumber = function(def) {
		$.each( def.schema.model.fields, function(key, value) {
			if( value.type === "number") {
				value.parse = function(v) {
					 return kendo.parseFloat(v, "en-US");
				};
			}
		});
	}
	
	var fixCode = function(def) {
		$.each( def.schema.model.fields, function(key, value) {
			if( value.type === "code") {
				value.type = "string";
				value.parse = function(v) {
					return translate.instant(v);
				};
			}
		});
	}
	
	var setDateByType = function (date, dateType, dsType) {
		var dt;
		switch (dateType) {
			case 'date':
				dt = util.dateDS(date, 'Fecha', dsType);
			break;
			case 'sysdate':
				dt = util.dateDS(date, 'FechaSistema', dsType);
			break;
			case 'datetime':
				dt = util.dateDS(date, 'FechaHora', dsType);
			break;
			case 'sysdatetime':
				dt = util.dateDS(date, 'FechaHoraSistema', dsType);
			break;
			case 'systime':
				dt = util.dateDS(date, 'HoraSistema', dsType);
			break;
		}
		return dt;
	}
	
	var fixDate = function(def,dsType) {
		$.each( def.schema.model.fields, function(key, value) {
			if( value.type === 'date' || value.type === 'datetime' || 
				value.type === 'sysdate' || value.type === 'sysdatetime' || value.type === 'systime') {
				if ( _.isUndefined(value.bcsType) ) {
					value.bcsType = value.type;
				}
				value.type = 'date';
				value.parse = function(v) {
					if ( v == undefined || v == null || _.isDate(v) ) return v;
					var d = v;
					switch ( dsType ) {
						case 'ODATA':
							if (typeof v !== "string") return v;
							var parts = v.match(/\/Date\((-?\d+)([+-]\d{2})?(\d{2})?.*/);
							if ( parts == undefined || parts == null ) return parts;
							var parseDate = parts[1] == undefined ? 0 : parseInt(parts[1]);
							var date = new Date(parseDate);
							d = setDateByType(date, value.bcsType, 'ODATA');
						break;
						case 'CALL':
							d = setDateByType(v, value.bcsType, 'CALL');
						break;
					}
					return d;
				};
			}
		});    
	};
	
	//aqui empieza
	var translate = angular.element(document.body).injector().get('$translate');
	var rootScope = angular.element(document.body).injector().get('$rootScope');
	var realDefinition = $.extend(true, {}, properties.definition);
	
	this.definition = $.extend(true, {}, properties.definition);
	this.type = properties.type;
	this.lastCallParameters = undefined;
	var that = this;
	realDefinition.schema = realDefinition.schema || {};
	realDefinition.schema.model = realDefinition.schema.model || {};
	realDefinition.schema.model.fields = realDefinition.schema.model.fields || {};
	
	switch (properties.type) {
	case "odata"	:	realDefinition.type = "odata";
						fixNumber(realDefinition);
						fixCode(realDefinition);
						fixDate(realDefinition,"ODATA");
						realDefinition.transport = realDefinition.transport || {} ;
						realDefinition.transport.parameterMap = function(data,type) {
							if (realDefinition.handler && realDefinition.handler.length > 0) {
								data['$handler'] = realDefinition.handler;
							}
							var obj = kendo.data.transports.odata.parameterMap(data);
							that.lastCallParameters = obj;
							return obj;
						}
						break;
	case "call"		: 	fixNumber(realDefinition);
						fixCode(realDefinition);
						fixDate(realDefinition,"CALL");
						
						if (realDefinition.callTransaction && realDefinition.callResultSet) {
							setCallTransport(realDefinition,
								realDefinition.callTransaction,
								realDefinition.callResultSet);
						}
						
						break;
	case "object"	:	break;
	case "array"	:	realDefinition.type = "array";
						fixDate(realDefinition,"");
						break;
					
	}
	
	
	if (realDefinition.pageSize && rootScope.SesionValoresDefectoSistema.CantidadRegistrosDatasource && angular.isNumber(Number(rootScope.SesionValoresDefectoSistema.CantidadRegistrosDatasource))) {
		realDefinition.pageSize = Number(rootScope.SesionValoresDefectoSistema.CantidadRegistrosDatasource);
	}
	
	
	this.ds = new kendo.data.DataSource(realDefinition);
	
	this.add = function(model) {
		return this.ds.add(model);
	}
	
	this.aggregate = function(data) {
		return this.ds.aggregate(data);
	}

	this.aggregates = function() {
		return this.ds.aggregates();
	}

	this.at = function(index) {
		return this.ds.at(index);
	}

	this.cancelChanges = function(item) {
		ds.cancelChanges(item);
	}

	this.data = function(array, isObject) {
		if ( isObject ) {
			return this.ds.data(Object.keys(array).map(function(k) {
				return array[k]
			}));
		} else if(!_.isUndefined(array) && !array)  {
			var newArray = bcs.cleanData(this.ds.data());
			return newArray;
		} else {
			var fieldsArray = Object.keys(this.ds.options.schema.model.fields);

			if (!!fieldsArray && !!array) {
				var newArray = [];

				for (var i=0; i < array.length; i++) {
					var obj = array[i];
					var newObj = {};

					for ( var j=0; j < fieldsArray.length; j++) {
						newObj[fieldsArray[j]] = obj[fieldsArray[j]];
					}

					newArray.push(newObj);
				}
				return this.ds.data(newArray);
			}
			return this.ds.data(array);
		}
	}
	
	
	this.fetch = function(callback) {
		return this.ds.fetch(callback);
	}
	
	this.filter = function(filters) {
		return this.ds.filter(filters);
	}

	this.get = function(id) {
		return this.ds.getByUid(id);
	}

	this.getByUid = function(uid) {
		return this.ds.getByUid(uid);
	}

	this.group = function(value) {
		return this.ds.group(value);
	}

	this.hasChanges = function() {
		return this.ds.hasChanges();
	}

	this.indexOf = function(item) {
		return this.ds.indexOf(item);
	}

	this.insert = function(index, item) {
		ds.insert(index, item);
	}

	this.online = function(value) {
		return this.ds.online(value);
	}

	this.offlineData = function(value) {
		return this.ds.offlineData(value);
	}

	this.page = function(value) {
		return this.ds.page(value);
	}

	this.pageSize = function(value) {
		return this.ds.pageSize(value);
	}

	this.pushCreate = function(items) {
		this.ds.pushCreate(items);
	}

	this.pushDestroy = function(items) {
		this.ds.pushDestroy(items);
	}

	this.pushInsert = function(items) {
		this.ds.pushInsert(items);
	}

	this.pushUpdate = function(items) {
		this.ds.pushUpdate(items);
	}

	this.query = function(options) {
		return this.ds.query(options);
	}

	this.read = function(data) {
		return this.ds.read(data);
	}

	this.remove = function(item) {
		var ds = this.ds;
		if ($.isArray(item)) {
			$.each(item, function(index, value) {
				ds.remove(value);
			});
		}
		else {
			ds.remove(item);
		}
	}

	this.removeAll = function() {
		this.ds.data([]);
	}
	
	this.skip = function() {
		return this.ds.skip();
	}

	this.sort = function(value) {
		return this.ds.sort(value);
	}

	this.sync = function() {
		return this.ds.sync();
	}

	this.total = function() {
		return this.ds.total();
	}

	this.totalPages = function() {
		return this.ds.totalPages();
	}

	this.view = function() {
		return this.ds.view();
	}
	
	this.exportToExcel = function(data,title,nombreArchivo) {
		var rows = [];
		var columns = _.map(data,function(element1,index1){ 
			return element1.field; 
		});
		var columnsWidth = _.map(data,function(element1,index1){ 
			return {autoWidth: true}; 
		});
		var titles = _.map(data,function(element2,index2){ 
			return { "value" : element2.title };
		});
		var formats = {};
		for (var i in data){
			formats[data[i].field] = data[i].format;
		}
		rows.push( { "cells" : titles  } );
		$.each(this.data(),function(keyyy,value) {
			var props = _.pick(value,columns);
			var fila = _.map(props,function(val,key){
				if(formats[key]){
					//return { value: kendo.toString(val, formats[key]) };
					return { value: val,format : formats[key] };
				}else{
					return { value: (val instanceof Date)?(util.dateisdefault(val)?"":util.formatoSecondate(val)):val };
				}
			})
			rows.push({ cells: fila});
		})
		var workbook = new kendo.ooxml.Workbook({
          names : [],
          sheets: [
            {
              columns: columnsWidth,
              title: "Hoja1",
              rows: rows
            }
          ]
        });
		
		//LIBRO-YYYY-MM-DD-NOMBRELIBRO
		nombreArchivo = nombreArchivo || "File";
		kendo.saveAs({dataURI: workbook.toDataURL(), fileName: nombreArchivo +".xlsx"});
	};

	this.setCallParameters = function(parameters) {
		if (parameters) {
			this.lastCallParameters = parameters;
			//this.ds.data([]);
			this.ds.filter(parameters);
		}
	}


}

angular.module('ModuloPlataforma').directive('newBcsGrid', function($translate,bcsComunes,bcsServicioPlataforma,$compile) {
	return {
		replace: false,
		transclude: true,
		scope: {
			options : '=?configuracion',
			bcsApi: '=?api',
			seleccionado : '=?',
			fnClick : '&',
			fnDobleClick : '&'
		},
		templateUrl: 'directives/bcs-grid/bcs-new-grid.html',
		link: {
			pre: function preLink(scope, elem, attrs, ctrl,transclude) {
				
				var startTime = new Date().getTime();
				
				function crearApi(grid,apigrid) {
					var api = {};
					api.dataSource = {};
					
					api.getGridId = function() {
						return scope.gridId;
					}
					
					api.isNestedGrid = function() {
						console.error("no implementado");
					}
					
					api.refresh = function() {
						scope.kendoGridApi.refresh();
					}
					
					api.hideColumn = function(value) {
						scope.kendoGridApi.hideColumn(value);
					}
					
					api.showColumn = function(value) {
						scope.kendoGridApi.showColumn(value);
					}
					
					api.select = function(value) {
						scope.kendoGridApi.select(value);
					}
					
					api.tbody = scope.kendoGridApi.tbody;
					
					api.getFilteredDataSource = function() {
						console.error("no implementado");
					}
					
					api.getConfiguration = function() {
						console.error("no implementado");
					}
					
					api.setOptions = function(options) {
						if (options.dataSource.ds) {
							scope._bcsDataSource = options.dataSource;
							options.dataSource = options.dataSource.ds;
						}
						scope.kendoGridApi.setOptions(options);
					}
					
					api.getOptions = function() {
						console.error("no implementado");
					}

					api.clearSelection = function() {
						scope.kendoGridApi.clearSelection();
					}
					
					api.dataItem = function(row) {
						scope.kendoGridApi.dataItem(row);
					}
					
					api.dataSource = scope._bcsDataSource;
					
					return api;
				}
				
				function fnSetColumnProperties(col) {
					var field = scope.options.dataSource.options.schema.model.fields[col.field];
					var fieldType = field.bcsType? field.bcsType : field.type;
					col.title = $translate.instant(col.title);
					
					if (col.groupable) {
						groupable = true;
					}
					
					if (col.format && _.isFunction(col.format)) {
						col.bcsFormatTemplate = col.format;
						delete col.format;
						col.template = function(i) {
							scope;
							var v = i[col.field];
							var f = col.bcsFormatTemplate(v, i);
							if (!i["__bcsFormat__"])
								i["__bcsFormat__"] = {};
							i["__bcsFormat__"][col.field] = f;
							return '<span bcs-bind="dataItem.' + col.field + '">' + f.valor + '</span>';
						}
					}
					
					
					if (!col.filterable) {
						col.filterable = { cell : {} };
					}
					if ( (fieldType === "date" || fieldType === "sysdate") && _.isUndefined(col.template) ) {
						var tpl = "#if (util.dateisdefault(" + col.field + "))  {##} " +
							"else {##=util.formatoDate(" + col.field + ")##}#";
						col.template = kendo.template(tpl);
					}
					
					if ( fieldType === "systime" && _.isUndefined(col.template) ) {
						var tpl = "#if (util.dateisdefault(" + col.field + "))  {##} " +
							"else {##=util.formatoTime(" + col.field + ")##}#";
						col.template = kendo.template(tpl);
					}
					
					if ( fieldType === "systime") {
						col.filterable = {
							cell: {
								template: function (args) {
									args.element.kendoDateTimePicker({
										format : "HH:mm:ss" // TODO: Mapear con kendo culture.
									});
								}
							}
						};
					}
					
					
					
					if ( (fieldType === "datetime" || fieldType === "sysdatetime") && _.isUndefined(col.template) ) {
						var tpl = "#if (util.dateisdefault(" + col.field + "))  {##} " +
							"else {##=util.formatoSecondate(" + col.field + ")##}#";
						col.template = kendo.template(tpl);
						col.filterable = {
							cell: {
								template: function (args) {
									args.element.kendoDateTimePicker({
										format : "dd-MM-yyyy HH:mm" // TODO: Mapear con kendo culture.
									});
								}
							}
						};
					}
					
					if ( fieldType === "number" && Object.keys(col.filterable.cell).length === 0) {
						col.filterable = {
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
					
					col.attributes = col.attributes || {};
					col.attributes.style = col.attributes.style || "";
					col.locked = col.locked !== undefined ? col.locked : false;

					switch (fieldType) {
						case "number":
							col.attributes.style += ";text-align:right;";
							break;
	
						case "string":
							col.attributes.style += ";text-align:left;";
							break;
					}
				}
				
				function gridDataBound(e) {
					
					var eventData = e.sender;

					var htmlRows = e.sender.tbody.find("tr");
					var htmlColumns = e.sender.thead.find("tr:first").find("th");
					var hashRows = {};
					var hashCells = {};

					for (var i = 0; i < htmlRows.length; i++) {
						if(typeof $(htmlRows[i]).attr('data-uid') !== "undefined") {
							hashRows[$(htmlRows[i]).attr('data-uid')] = $(htmlRows[i]);
							hashCells[$(htmlRows[i]).attr('data-uid')] = {};
							
							var cells = hashRows[$(htmlRows[i]).attr('data-uid')].find("td");

							for (var j = 0; j < htmlColumns.length ; j ++) {
								hashCells[$(htmlRows[i]).attr('data-uid')][$(htmlColumns[j]).attr("data-field")] = $(cells[j]);
							}
						}
					}

					var groupingElement = eventData.element.find('.k-grouping-header');
					if(groupingElement.find('.k-group-indicator').length > 0) {
						var rows = eventData.dataSource.data();
					}
					else {
						var rows = eventData.dataSource.view();
					}
						
					for (var i = 0; i < rows.length; i++) {
						
						// rowColorBackground
						if (scope.options.rowColorBackground) {
							var color = scope.options.rowColorBackground(rows[i]);
							if ( color !== undefined || color !== "" ) {
								if(typeof hashRows[rows[i].uid] !== "undefined") {
									hashRows[rows[i].uid].addClass(color);
								}
							}
						}


					// boldRow
					if (scope.options.boldRow) {
						var bold = scope.options.boldRow(rows[i]);
						if ( bold ) {
							if(typeof hashRows[rows[i].uid] !== "undefined") {
								hashRows[rows[i].uid].css("font-weight","bold");
								hashRows[rows[i].uid][0].style.setProperty("font-weight", "bold");
								hashRows[rows[i].uid].addClass("boldRow");
							}
						}
					}
						// rowCellColor
						if (scope.options.rowCellColor) {
							var colorObj = scope.options.rowCellColor(rows[i]);
							if ( !!colorObj && colorObj.column !== "" && colorObj.color !== "" && !angular.isArray(colorObj)) {
								if(typeof hashCells[rows[i].uid] !== "undefined") {
									hashCells[rows[i].uid][colorObj.column].addClass(colorObj.color + "-cell");
								}
							}
							else if ( !!colorObj && angular.isArray(colorObj) && colorObj.length > 0) {
								if(typeof hashCells[rows[i].uid] !== "undefined") {
									for (var xx = 0 ; xx < colorObj.length ; xx++) {
										hashCells[rows[i].uid][colorObj[xx].column].addClass(colorObj[xx].color + "-cell");
									}
								}
							}
						}
					}
				}
				
				/* INIT */
				scope.elem = elem;
				scope.gridId = elem.attr("id") || "";
				scope.idVentana = bcsComunes.ventanaWinId(scope);
				scope._bcsDataSource = scope.options.dataSource;
				scope.options.dataSource = scope._bcsDataSource.ds;
				
				//defaults opciones
				if (scope.options.autoBind === undefined) scope.options.autoBind = false;
				if (scope.options.allowCopy === undefined) scope.options.allowCopy = true;
				if (scope.options.filterable) {
					scope.options.filterable = {
						cell : {
							minlength : 50
						},
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
				
				
				
				scope.options.scrollable = true;
				if (scope.options.resizable === undefined) scope.options.resizable = true;
				if (scope.options.reorderable === undefined) scope.options.reorderable = true;
				if (scope.options.scrollable === undefined) scope.options.scrollable = true;
				if (scope.options.dataBound !== undefined && _.isFunction(scope.options.dataBound)) {
					var valueDataBound = scope.options.dataBound;
					var fnDataBound = function(e) {
						gridDataBound(e);
						valueDataBound(e);
					}
					
					scope.options.dataBound = fnDataBound;
				}
				else {
					scope.options.dataBound = gridDataBound;
				}
				
				
				
				//defaults columnas
				var groupable;
				for ( var i = 0; i < scope.options.columns.length ; i++) {
					fnSetColumnProperties(scope.options.columns[i]);
				}
				if (groupable) {
					scope.options.groupable = groupable;
				}
				
				//guarda configuracion base
				scope.options.gridStartingSettings = {
					columns : {}
				};
				for (var i = 0;i < scope.options.columns.length;i++) {
					var data = {
						title : scope.options.columns[i].title,
						width : Number(scope.options.columns[i].width.replace("px",'')),
						visible : !scope.options.columns[i].hidden,
						locked : scope.options.columns[i].locked,
						columnIndex : i
					};
					scope.options.gridStartingSettings.columns[scope.options.columns[i].field] = data;
				}
				
				//settings columnas
				var gridDefaultSettings = undefined;
				if(scope.gridId.trim()!="") {
					var gridUserSettings = sesion.getPreferencias("GRID_" + scope.gridId);
					if(sesion.getPreferencias("DEFAULT-GRID") !== undefined)
						gridDefaultSettings = sesion.getPreferencias("DEFAULT-GRID")[BCS_SESION.IdEmpresa + '::DEFAULT-GRID_' +  scope.gridId];
				}
				
				var settings = undefined;
				if (gridUserSettings == undefined) {
					if (gridDefaultSettings == undefined) {
						settings = scope.options.gridStartingSettings;
					}
					else {
						settings = gridDefaultSettings;
					}
				}
				else{
					settings = gridUserSettings;
				}
				
				try {
					settings = JSON.parse(settings)
					if (settings.columns === undefined) {
						sesion.setPreferencias("GRID_" + gridId, {columns : {}});
					}
				}
				catch (error) {
					
				}

				
				var columnsSorted = Array(scope.options.columns.length);
				var newColumns = Array();
				for ( var i = 0; i < scope.options.columns.length ; i++) {
					
					var col = scope.options.columns[i];
					var colConf = settings.columns[col.field];
					
					if (colConf !== undefined) {
						
						if (colConf.visible !== undefined) {
							col.hidden = !colConf.visible;	
						}
						if (colConf.width !== undefined) {
							col.width = colConf.width;
						}
						if (colConf.locked !== undefined) {
							col.locked = colConf.locked;
						}
						if (colConf.columnIndex !== undefined) {
							columnsSorted[colConf.columnIndex] = col;
						}
						else {
							newColumns.push(col);
						}
					}
				}
				
				scope.options.columns = [];
				for (var jj = 0;jj < columnsSorted.length ; jj++) {
					if (columnsSorted[jj] === undefined) {
						continue;
					}

					scope.options.columns.push(columnsSorted[jj]);
					columnsSorted[jj].columnIndex = scope.options.columns.indexOf(columnsSorted[jj]) ;
				}
				
				for (var jj = 0;jj < newColumns.length ; jj++) {
					scope.options.columns.push(newColumns[jj]);
					newColumns[jj].columnIndex = scope.options.columns.indexOf(newColumns[jj]) ;
				}
				
				scope.options.change = function(e) {

					var multiple = scope.options.selectable.indexOf('multiple') === 0;
					if (multiple) {
						scope.seleccionado = [];
						$.each(scope.kendoGridApi.select(),function(key,value){
							if ($.inArray(scope.seleccionado,value)) {
								scope.seleccionado.push(scope.kendoGridApi.dataItem(value));	
							}
						})
					}
					else {
						scope.seleccionado = scope.kendoGridApi.dataItem(scope.kendoGridApi.select());
					}

				}
				
				scope.options.applyColumnConfiguration = function(conf) {
					for ( var i = 0; i < scope.kendoGridApi.columns.length ; i++) {
						var col = scope.kendoGridApi.columns[i];
						var colConf = conf.columns[col.field];
						if (!conf.columns) {
							return;
						}

						if (colConf !== undefined && (colConf.visible == true || colConf.visible === undefined)) {
							scope.kendoGridApi.showColumn(scope.kendoGridApi.columns[i].field);
						}
						else if (colConf !== undefined && colConf.visible == false) {
							scope.kendoGridApi.hideColumn(scope.kendoGridApi.columns[i].field);
						}

						if (colConf !== undefined && (colConf.locked == true)) {
							scope.kendoGridApi.lockColumn(scope.kendoGridApi.columns[i].field);
						}
						else if (colConf !== undefined && !colConf.locked) {
							scope.kendoGridApi.unlockColumn(scope.kendoGridApi.columns[i].field);
						}

						if (colConf !== undefined && (colConf.width) && scope.kendoGridApi.columns[i].width !== colConf.width) {
							scope.kendoGridApi.resizeColumn(scope.kendoGridApi.columns[i],colConf.width);
						}
						if (colConf !== undefined && (colConf.columnIndex !== undefined) && colConf.columnIndex < scope.kendoGridApi.columns.length) {
							scope.kendoGridApi.reorderColumn(colConf.columnIndex,scope.kendoGridApi.columns[i]);
						}
					}
					scope.kendoGridApi.refresh();
				} 
				
				
				scope.options.excel = scope.options.excel || {};
				scope.options.excel.allPages = false;

				/*scope.options.detailInit = function(a,b,c,d,e) {
					console.log("init");
				}*/
				
				
				//scope.options.detailInit = 
				
				/*transclude(scope, function(clone, scopeB) {
					var detailContent = "";
					for( var i = 0; i < clone.length; i++) {
						detailContent += clone[i].outerHTML || '';
					}
					var tag = 'div[ng-non-bindable][bcs-grid-detail]';
					if ($('<div>'+detailContent+'</div>').find("div[ng-non-bindable][bcs-grid-detail]").html() !== undefined) {
						var det = $(detailContent);
						det = det.removeAttr("ng-non-bindable");
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
						scope.options.detailTemplate = kendo.template(det.html());
						
						
					}
				});*/
				
				/*scope.options.columns.unshift({
					field : "blank",width : "0px",locked:true
				});*/

				scope.gridHtmlElement = $(elem).find("div").kendoGrid(scope.options);
				scope.kendoGridApi = $(elem).find("div").data("kendoGrid");
				scope.bcsApi = crearApi(scope.gridHtmlElement,scope.kendoGridApi);
				if (scope.options.gridCreated) {
					scope.options.gridCreated(scope.bcsApi,scope.kendoGridApi);
				}
				
				/* END INIT */
				
				/* HIDE GROUPING HEADER */
				if(!!scope.options.hideGroupingHeader) {
					scope.gridHtmlElement.children(".k-grouping-header").hide();
				}
				/* END HIDE GROUPING HEADER /*
				
				/* MENU CONTEXTUAL */
				if (!elem[0].id) {
					console.warn("grilla sin id definido")
				}
				else {
					scope.idHtml = "#" + elem[0].id;
					
					var dsMenu = [{
			                    text: "Exportar Pagina Actual"
			                },{
			                    text: "Exportar Todo"
			                },{
			                    text: "Configuración"
			                }]
					if (scope.options.selectable === "multiple row" || scope.options.selectable === "multiple, row") {
						dsMenu.splice(0,0,{text: "Deseleccionar Todo"});
						dsMenu.splice(0,0,{text: "Seleccionar Todo"});
					}


					var menu = $(elem[0]).find("span:first").find("ul:last").kendoContextMenu({
						target : scope.idHtml,
						orientation: "vertical",
						select : function(e) {
							if (e.item.textContent === 'Exportar Todo') {
								scope.fnExportarExcel(undefined, true);
							}
							else if (e.item.textContent === 'Exportar Pagina Actual') {
								scope.fnExportarExcel(undefined, false);
							}
							else if (e.item.textContent === 'Seleccionar Todo') {
								scope.kendoGridApi.select(scope.kendoGridApi.tbody.find(">tr"));
							}
							else if (e.item.textContent === 'Deseleccionar Todo') {
								scope.kendoGridApi.clearSelection();
							}
							else if (e.item.textContent === 'Configuración') {
								scope.bcsgridconf();
							}
						},
						dataSource: dsMenu
			                
					});
				}
				/* FIN MENU CONTEXTUAL*/
				
				/* SETTINGS */
				scope.bcsgridconf = function() {
					var id1 = bcsComunes.ventanaAbrir("bcsGridNewConf",{ api: scope.kendoGridApi,confOptions : scope.options,gridId : scope.gridId },false,false,scope.idVentana);
					var promesa = bcsComunes.ventanaPromesa(id1);				
				};
				/* FIN SETTINGS */
				
				/* DOBLE CLICK */
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
				/* FIN DOBLE CLICK */
				
				/*EXPORTAR A EXCEL*/
				scope.fnExportarExcel = function(fileName, allPages) {
					if(!fileName){
						var ventana = $translate.instant(BCS_VENTANA[scope.elem.closest("[winKey]").attr("winKey")].TITLE);
						var fecha = util.formatoDate(new Date());
						var hora = util.formatoTime(new Date()).replace(/:/g,"-");
						var tab = $(scope.elem.closest("[winkey]")).find("[heading].active").attr("heading");
						fileName = (ventana+"_")+( (tab)?tab+"_" : "" )+(fecha)+"_"+(hora);	
					}
					
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
					(scope.kendoGridApi.getOptions().columns!==undefined ? scope.kendoGridApi.getOptions().columns : [] ).forEach( element => {
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
					var filasMaximas = scope.options.dataSource.pageSize();
					var cantidadColumnas = (columnas!==undefined) ? columnas.length : -1;
					var cantidadFilas = (scope.kendoGridApi.dataSource.data().length > filasMaximas ) ? filasMaximas : scope.kendoGridApi.dataSource.data().length;
					var cantidadFilasTotal = scope.kendoGridApi.dataSource.total();
					
					if(scope.kendoGridApi.dataSource.data().length>0){
						//si la grilla es pequeña o es de tipo array o pide exportar solo la hoja actual
						// primer caso, imprimir online si se solicita imprimir la hoja actual y el resultado es pequenio
						// segundo caso, imprimir online si se solicita imprimir todo y el resultado es pequenio
						// tercer caso, imprimir de forma batch todas las demas posibilidades
						if( ((cantidadColumnas * cantidadFilas) < 15000  && !allPages) || scope.kendoGridApi.dataSource.options.type ==="array" || ((cantidadColumnas * cantidadFilasTotal)<15000 && allPages)){
							
							scope.kendoGridApi.options.excel.fileName = fileName;
							scope.kendoGridApi.options.excel.allPages = allPages;
							scope.kendoGridApi.saveAsExcel();
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
									parametros.columns[k].type = scope.options.dataSource.options.schema.model.fields[parametros.columns[k].field].type;
									parametros.columns[k].bcstype = scope.options.dataSource.options.schema.model.fields[parametros.columns[k].field].bcsType;
							}

							//parametros url
							if(scope._bcsDataSource._type === "call"){
								//es call o java
								if(!!scope.kendoGridApi.dataSource.definition){
									parametros.t = scope.kendoGridApi.dataSource.definition.callTransaction;
									parametros.r = scope.kendoGridApi.dataSource.definition.callResultSet;
									parametros.callParameters = scope.kendoGridApi.dataSource.lastCallParameters;
									parametros.gridFilters = scope.options.dataSource.filter() ? scope.options.dataSource.filter().filters : undefined;
									parametros.gridSort = scope.options.dataSource.sort() && scope.options.dataSource.sort().length > 0 ? scope.options.dataSource.sort() : undefined;

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
								}else{
									parametros.t = scope._bcsDataSource.definition.callTransaction;
									parametros.r = scope._bcsDataSource.definition.callResultSet;
									parametros.callParameters = scope._bcsDataSource.lastCallParameters;
									parametros.odataParameters = scope.kendoGridApi.dataSource._sort;
									parametros.gridFilters = scope.options.dataSource.filter() ? scope.options.dataSource.filter().filters : undefined;
									parametros.gridSort = scope.options.dataSource.sort() && scope.options.dataSource.sort().length > 0 ? scope.options.dataSource.sort() : undefined;

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
								}
								
							}else{
								//es vista
								//obtener idServicio
								_.first(scope._bcsDataSource.definition.transport.read.url.split("?")
								.filter( (path) => path.indexOf("t=")>=0 ))
								.split("&")
								.forEach( (params) => {
												splitParam = params.split("=");
												parametros[splitParam[0]] = splitParam[1];
											});						
								//obtener los ultimos filtros
								parametros.odataParameters = scope._bcsDataSource.lastCallParameters;
								if(allPages){
									if(parametros.odataParameters!==undefined){
										delete parametros.odataParameters.$top;	
									}else{
										console.warn("ATENCION: La nueva grilla necesita el bcs.newDatasource!");
									}
								}
							}
							
							parametros.callParameters = parametros.callParameters || {};
							parametros.odataParameters = parametros.odataParameters || {};
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
									bcsComunes.ventanaOverlay(scope.idVentana,false);
									var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
									bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
										conflictos : util.parseServerResponse(result)}, scope.idVentana);

								},
								function(result){
									bcsComunes.ventanaOverlay(scope.idVentana,false);
									bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
								});

							}else{
								bcsComunes.msg("info","info", $translate.instant('UI_EXPORTACION_BATCH'));		
							}
						}
					}else{
						bcsComunes.msg("info","info", $translate.instant('UI_NO_HAY_INFORMACION_PARA_EXPORTAR'));	
					}
					
				}
				let aux = bcsComunes.obtenerIdVentanaGrid(elem[0].id);

					if(elem[0].id){ // si existe id, se agrega elemento para hace ajuste desde grilla
				
						let grid = elem.find('.k-grid.k-widget.k-display-block.k-reorderable')
						let resizeDiv = $('<div class="gridResize"></div>');
						grid.after(resizeDiv);
					}
		
					aux.find('.k-resize-handle.k-resize-s').on("mousedown", aux, function(event) {
						aux.css("height", "auto")
						let tab = aux.find('.tab-pane.ng-scope.active').not('tr.k-detail-row .tab-pane.ng-scope.active');
						if (tab.length === 0){ // caso en el cual la grilla se encuentra en una ventana sin tabs
							if (event.clientY >= (elem.offset().top + elem.height() - 10)) {
								let grid = aux.find(".k-grid.k-widget.k-display-block.k-reorderable").not('tr.k-detail-row.ng-scope .k-grid.k-widget.k-display-block.k-reorderable');
								if(grid.length > 1){
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
							
								  if (newHeight > 100 && newHeightContent > 100 && dif > 0) {
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
							
						}
						else{ // caso cuando el grid se encuentra dentro de un tab
							if (event.clientY >= (elem.offset().top + elem.height() - 10)) {
								//let gridElement = aux.find(".tab-pane.ng-scope.active [kendo-grid]:not(tr.k-detail-row.ng-scope [kendo-grid])");
								let gridElement =  aux.find(".tab-pane.ng-scope.active .k-grid.k-widget.k-display-block.k-reorderable").not('tr.k-detail-row.ng-scope [kendo-grid]');
								if(gridElement.length > 1){
									return
								}
								
								let lockCell = aux.find('.tab-pane.ng-scope.active .k-grid.k-widget.k-display-block.k-reorderable .k-grid-content-locked');
								
								//let gridContentElement = aux.find(".tab-pane.ng-scope.active [kendo-grid] .k-grid-content.k-auto-scrollable");
								let gridContentElement = aux.find(".tab-pane.ng-scope.active .k-grid.k-widget.k-display-block.k-reorderable .k-grid-content.k-auto-scrollable").not('tr.k-detail-row.ng-scope [kendo-grid] .k-grid-content.k-auto-scrollable');
		
								if (gridElement.length == 0 && gridContentElement.length == 0){ // caso en el que una grilla se encuentre en un tab, pero la tabla es fuera del formulario
									gridElement = elem.find(".k-grid.k-widget.k-display-block.k-reorderable")
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
				
								if (newHeight > 100 && newContentHeight > 100 && dif > 0) {
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
									let grid = elem.find(".k-grid.k-widget.k-display-block.k-reorderable").not('tr.k-detail-row.ng-scope .k-grid.k-widget.k-display-block.k-reorderable');
									console.log(grid);
									if(grid.length > 1){
										return
									}
									
									let contentGrid = elem.find(".k-grid-content.k-auto-scrollable").not("tr.k-detail-row .k-grid.k-widget.k-display-block.k-reorderable");
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
								
									  if (newHeight > 100 && newHeightContent > 100 && dif > 0) {
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
				/* FIN EXPORTAL EXCEL */
				var endTime = new Date().getTime();
				console.log("Grid " + scope.gridId + "build time" + (endTime - startTime));
			}
		}
	}
	
});