/*

*/
kendo.support.scrollbar = function () { return 10; };

var bcs = {};

function _uibTab(t,d) {
	var refresh = [];

	this.select = function(grid) {
		if ( !refresh[grid] ) {
			t( function() {
			//	var innerGrid = $("#" + grid).find("[kendo-grid]").data("kendo-grid") || $("#" + grid).data("kendo-grid");
				var innerGrid = $("#" + grid).find("[kendo-grid]").data("kendoGrid");
				if ( innerGrid ) {
					//innerGrid.dataSource.read();
					innerGrid.refresh();
				}
			},
			d );
			refresh[grid] = true;
		}
	}
};

bcs.uibTab1 = _uibTab;

function getScope(id) {
var elem;
$('.ng-scope').each(function(){
    var s = angular.element(this).scope(),
        sid = s.$id;

    if(sid == id) {
        elem = this;
        return false; // stop looking at the rest
    }
});
return elem;
}

bcs.uibTab =  function(t,d,scope,th) {
	var refresh = [];
	var myScope = scope;
	var myThis = th;

	this.select = function(grid) {
	//	console.log(myScope);
	//	console.log(myThis);
		//if ( !refresh[grid] ) {
			t( function() {
			//	var innerGrid = $("#" + grid).find("[kendo-grid]").data("kendo-grid") || $("#" + grid).data("kendo-grid");
			//	var innerGrid = $("#" + grid).find("[kendo-grid]").data("kendoGrid");
				var innerGrid = $("#" + grid).find("bcs-grid").find("[kendo-grid]").data("kendoGrid");
				if ( innerGrid ) {
					//innerGrid.dataSource.read();
					innerGrid.refresh();
				}
			},
			d );
			refresh[grid] = true;
		//}
	}
};

/**
* Crea y retorna una copia completa del objeto 'source'.
*/
bcs.clone = function(source) {
	return $.extend(true, {}, source);
};

bcs.clone2 = function(source) {
	return source;
};


bcs.cleanData = function(array) {
	var newArray = [];
	for (var i=0; i < array.length; i++) {
		var obj = array[i];
		var objKeys = Object.keys(obj);
		var newObj = {};

		for ( var j=0; j < objKeys.length; j++) {
			if ( objKeys[j] !== 'uid' && objKeys[j] !== 'parent' &&
				 objKeys[j] !== 'dirty' && objKeys[j] !== '_events' &&
				 objKeys[j] !== '_handlers') {
				newObj[objKeys[j]] = obj[objKeys[j]];
			}
		}
		newArray.push(newObj);
	}
	return newArray;
};

function getCaretPosition(ctrl) {
	var CaretPos = 0;
	// IE Support
	if (document.selection) {
		ctrl.focus();
		var Sel = document.selection.createRange();
		Sel.moveStart('character', -ctrl.value.length);
		CaretPos = Sel.text.length;
	}
	// Firefox support
	else if (ctrl.selectionStart || ctrl.selectionStart == '0') {
		CaretPos = ctrl.selectionStart;
	}

	return CaretPos;
}

function setCaretPosition(ctrl, pos) {
	if (ctrl.setSelectionRange) {
		ctrl.focus();
		ctrl.setSelectionRange(pos,pos);
	}
	else if (ctrl.createTextRange) {
		var range = ctrl.createTextRange();
		range.collapse(true);
		range.moveEnd('character', pos);
		range.moveStart('character', pos);
		range.select();
	}
}

bcs.MaskedTextBox = function (args,options) {
	var textBox = null;
	if (typeof options === "undefined")
		textBox = args.element.kendoMaskedTextBox();
	else
		textBox = args.element.kendoMaskedTextBox(options);

	textBox
	.keyup(function(e) {
		var caretPosition = getCaretPosition(this);
		this.value = this.value.toLocaleUpperCase();
		setCaretPosition(this, caretPosition);

		if (e.which === 13 ) {
			//console.log("enter " + this.value + " " + textBox.data("kendoMaskedTextBox").value());
			this.blur();
			this.focus();
		}
	})
	.attr("style","text-transform:uppercase;width:calc(100% - 22px);float:left;")
	;
};

bcs.DataSource = function(properties) {
	var ds;
	var translate = angular.element(document.body).injector().get('$translate');
//	var definition = $.extend(true, {}, properties.definition);
	var definition = bcs.clone(properties.definition);
	var type = properties.type;
	var dataSourceChanged = [];
	var dataSourceChangedMap = [];

	var setCallTransport = function(ds, callTransaction, callResultSet, callParameters) {
		const dsConfig = ds.options;
		ds.transport = {
			read: function(options) {
				
				var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet;
				
				if(dsConfig && dsConfig.schema && dsConfig.schema.total && dsConfig.schema.data && dsConfig.serverPaging 
						&& dsConfig.serverSorting && options.data) {
					if (options.data.skip !== "undefined" && typeof options.data.take !== "undefined") {
						url += "&skip=" + options.data.skip + "&take=" + options.data.take;
					};
					
					if (options.data.sort && options.data.sort[0] && options.data.sort[0].field && options.data.sort[0].dir) {
						var sort = `${options.data.sort[0].field}::${options.data.sort[0].dir}`;
						url += "&sort=" + sort;
					};
				};
				$.ajax({
					url: url,
					dataType: "json",
					type: "POST",
					contentType:"application/json; charset=utf-8",
					data: JSON.stringify(callParameters),
					success: function(result) {
						var resultSet = [];
						var outResultSet = result[callResultSet] ? result[callResultSet] : result;
						var totalResults = result["total"] ? result["total"] : undefined;
						outResultSet = result["data"] ? result["data"] : outResultSet
						for ( var key in outResultSet) {
							resultSet.push(outResultSet[key]);
						}
						result.P_OUT_TBL = resultSet;

						if (dsConfig && dsConfig.schema && dsConfig.schema.total && dsConfig.schema.data && dsConfig.serverPaging 
								&& dsConfig.serverSorting && totalResults && typeof totalResults == 'number') {
							options.success({data: result.P_OUT_TBL, total: totalResults});
						} else {
							options.success(result.P_OUT_TBL);
						};
					},
					error: function(result) {
						options.error(result);
					}
				});
			}
		};
	};

	var setDefinition = function(def, t) {

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
		
		// Corrige problemas de formatos numericos
		var fixNumber = function() {
			$.each( def.schema.model.fields, function(key, value) {
				if( value.type === "number") {
					value.parse = function(v) {
						 return kendo.parseFloat(v, "en-US");
					};
				}
			});
		}

		var fixCode = function() {
			$.each( def.schema.model.fields, function(key, value) {
				if( value.type === "code") {
					value.bcsType = value.type;
					value.type = "string";
					value.parse = function(v) {
						return translate.instant(v);
					};
				}
			});
		}

		var fixDate = function(dsType) {
			$.each( def.schema.model.fields, function(key, value) {
				if( value.type === 'date' || value.type === 'datetime' || 
					value.type === 'sysdate' || value.type === 'sysdatetime' || value.type === 'systime') {
					if ( _.isUndefined(value.bcsType) ) {
						value.bcsType = value.type;
					}
					value.type = 'date';
				//	value._dsType = dsType;
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

		switch (t) {
		case "array":
			def.type = "array";
			def.schema = def.schema || {};
			def.schema.model = def.schema.model || {};
			def.schema.model.fields = def.schema.model.fields || {};
			fixDate("");		
			break;

		case "object":
			break;

		case "odata":
			def.type = "odata";
			fixNumber();
			fixCode();
			fixDate("ODATA");
			if (def.transport) {
				def.transport.parameterMap = function(data,type) {
					//console.log(def);

					if (def.handler && def.handler.length > 0) {
						data['$handler'] = def.handler;
					}
					var obj = kendo.data.transports.odata.parameterMap(data);
					def.lastCallParameters = obj;
					return obj;
				}
			}
			
			break;

		case "call":
			//def.type = "json";
			fixNumber();
			fixCode();
			fixDate("CALL");
			if (def.callTransaction && def.callResultSet) {
				// if ( _.isObject( def.callParameters ) )
				// /*_.isFunction(object) */ { }
				if (def.callParameters) {
					setCallTransport(def,
						def.callTransaction,
						def.callResultSet,
						def.callParameters);
				}
			}
			break;
		}

		if ( def.filters ) {
		//	def.filter = $.extend(true, {}, def.filters);
			def.filter = bcs.clone(def.filters);
			// delete def.filters;
		}

		/*if ( def.filtersFn && scope.grid && scope.grid.dataItem ) {
			def.filter = ( def.filter ) ? def.filter : dataSource.def.filtersFn(scope.grid.dataItem);
			delete def.filtersFn;
		}*/

		var dsDefRequestEnd = function(e) {
			if(e.type === "read") {
				dataSourceChanged = [];
				dataSourceChangedMap = [];
			//	scope.dataSourceMap = [];
			}
		}

		var valueDsDefRequestEnd;

		if ( def.requestEnd ) {
			valueDsDefRequestEnd = def.requestEnd;
			var fnRequestEnd = function(e) {
				dsDefRequestEnd(e);
				valueDsDefRequestEnd(e);
			}
			def.requestEnd = fnRequestEnd;
		}
		else {
			def.requestEnd = dsDefRequestEnd;
		}

		var itemChange = def.itemChange;
		var dataChange = def.dataChange;

		def.change = function(e) {
			if (_.isUndefined(e.action) && e.items.length > 0) {
			//	console.log("bcs.DataSource data bounded");
			}
			else if (e.action === "itemchange") {
				if ( _.isUndefined(dataSourceChangedMap[e.items[0].uid]) ) {
					dataSourceChanged.push(e.items[0]);
					dataSourceChangedMap[e.items[0].uid] = dataSourceChanged.length - 1;
				}
				else {
					dataSourceChanged[dataSourceChangedMap[e.items[0].uid]] = e.items[0];
				}

				if (!_.isUndefined(itemChange)) {
					itemChange(e);
				}

			}
			
			if (!_.isUndefined(dataChange)) {
				e.preventDefault();
				dataChange(e);
				return;
			}
		};

	}
	
	setDefinition(definition, type);
	ds = new kendo.data.DataSource(definition);

	this.ds = ds;
	this.definition = definition;
	this._type = type;
	this.firstLoad = false;

	/* ********************* */
	/*   Methods             */
	/* ********************* */
	this.add = function(model) {
		return this.ds.add(model);
	}

	this.setHandler = function(handler) {
		this.definition.handler = util.parseHandler(handler);
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
			var fieldsArray = !this.definition.schema || !this.definition.schema.model || !this.definition.schema.model.fields ? 
				false : Object.keys(this.definition.schema.model.fields);

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

	/**
	* dataColumns : Retorna un subconjunto del dataSource de acuerdo a
	* las columnas espeficicadas en el arreglo 'arrayCols'.
	*/
	this.dataColumns = function(arrayCols, ds) {
		// console.warn('Esta función será eliminada en 2016-06-01.');
		var dataCols = [];
		if (ds) {
			$.each(ds, function(index, value) {
				dataCols[index] = _.pick(value, arrayCols);
			});
		} else {
			$.each(this.ds.data(), function(index, value) {
				dataCols[index] = _.pick(value, arrayCols);
			});
		}

		return (dataCols.length > 0) ? dataCols : undefined;
	}

	this.fetch = function(callback) {
		this.firstLoad = true;
		return this.ds.fetch(callback);
	}
	
	/**
	* fetchPage : Retorna una promesa que Recupera la página indicada
	* en el parámetro 'page'.
	*/
	this.fetchPage = function(page) {
		var options = {
			filter		: this.filter(),
			pageSize	: this.pageSize(),
			page		: page,
			sort		: this.sort(),
			aggregate	: this.aggregate(),
			group		: this.group()
		};
		return this.ds.query(options);
	}

	/**
	* filters : Reemplaza la función original 'filter'.
	*/
	this.filters = function(filters) {
		this.firstLoad = true;
		console.error("El método 'filters' será eliminado, en su luga debe usar 'filter'");
		return this.ds.filter(filters);
	}

	this.filter = function(filters) {
		this.firstLoad = true;
		return this.ds.filter(filters);
	}

	/**
	* formattedData : 
	*/
	this.formattedData = function(ds, fieldFormat) {
		var dataCols = [];
		for (var i = 0; i < ds.length; i++) {
		//	dataCols[i] = $.extend(true, {}, ds[i]);
			dataCols[i] = bcs.clone(ds[i]);
			for (var j = 0; j < fieldFormat.length; j++) {
				if (_.isFunction(fieldFormat[j].format)) {
					var fieldValue = dataCols[i][fieldFormat[j].field];
					var formattedValue = fieldFormat[j].format(fieldValue);
					dataCols[i][fieldFormat[j].field] = formattedValue;
				}
			}
		}
		return dataCols;
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

	/**
	* itemsEdited : Retorna los items del dataSource que han
	* sido modificados. 
	*/
	this.itemsEdited = function(array) {
		return dataSourceChanged;
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

	this.pushUpdate = function(items) {
		this.ds.pushUpdate(items);
	}

	this.query = function(options) {
		this.firstLoad = true;
		return this.ds.query(options);
	}

	this.read = function(data) {
		this.firstLoad = true;
		return this.ds.read(data);
	}

	/**
	* remove : Elimina un ítem específico o un conjunto de 
	* items sí el parametro de entrada es un arreglo de items.
	*/
	this.remove = function(item) {
		var ds = this.ds;
		if ($.isArray(item)) {
			$.each(item, function(index, value) {
				try {
					ds.remove(value);
				}
				catch(error) {
					console.error(error);
				}
			});
		}
		else {
			try {
				ds.remove(item);
			}
			catch(error) {
				console.error(error);
			}
		}
	}

	/**
	* removeAll : Elimina todos los items del dataSource.
	*/
	this.removeAll = function() {
		this.ds.data([]);
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
	
	/**
	* setCallParameters : Configuración de los parámetros usados en
	* el dataSource tipo 'call'.
	*/
	this.setCallParameters = function(parameters) {
		this.lastCallParameters = parameters;
	//	if (this.definition.callParameters) {
		if (parameters) {
			setCallTransport(this.ds, this.definition.callTransaction,
				this.definition.callResultSet, parameters);
			return this.ds.read();
		}
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

	this.setOptions = function(options) {
		this._type = options.type;
		setDefinition(options.definition, options.type);
		this.definition = options.definition;
		var newds = new kendo.data.DataSource(options.definition);
		options = null;
		this.ds = newds;

	}

	this.getOptions = function() {
		return { type : this._type, definition: this.definition};
	}

	/* ******************** */
	/*   Bind               */
	/* ******************** */
	
	/**
	* Usado para suscribir eventos 
	* después de la inicialización.
	*/
	this.bind = function(event, callback) {
		return this.ds.bind(event, callback);
	}
	
}
