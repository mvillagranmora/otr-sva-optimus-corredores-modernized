angular.module('ModuloPlataforma').factory('bcsConfiguracion',function($translate,bcsConfiguracionPlataforma) {

	//public
	var obj = {};

	//Configuracion base para grilla
	obj.configurarGrid = function(scope,elem,options,toolbarConfig)
	{
		options = options || {};
		//referencia al widget
		scope.$on("kendoWidgetCreated", function (event, widget) {
			if (widget.$angular_scope === scope)
				scope.elemento = widget;
				scope.kendoWidget = widget;
		});


		//Opciones normles
	if (typeof options.columnMenu === 'undefined') options.columnMenu = false;
    if (typeof options.groupable === 'undefined') options.groupable = true;
    if (typeof options.filterable === 'undefined') options.filterable =  true;
    if (typeof options.sortable === 'undefined') options.sortable = true;
    if (typeof options.allowCopy === 'undefined') options.allowCopy = true;
	if (typeof options.reorderable === 'undefined')	options.reorderable = true;
    if (typeof options.resizable === 'undefined') options.resizable = true;
    if (typeof options.pageable === 'undefined') options.pageable = true;

		//exportar a excel
		scope.fnExportarExcel = function()
		{
			scope.kendoWidget.saveAsExcel();
		}
		//pintar el toolbar
		var toolbarObj = [];
		if (toolbarConfig !== undefined)
		{
			if (toolbarConfig.title !== undefined && toolbarConfig.title.length > 0)
				toolbarObj.push({template : kendo.template("<label style='padding-right:10px' translate>" + toolbarConfig.title +"</label>")});
			if (toolbarConfig.buttons !== undefined)
			{
				$.each(toolbarConfig.buttons,function(key,value){
					if (value.name === "excel")
						toolbarObj.push({template : kendo.template("<bcs-button class='col-xs-2' bcs-texto=\"'UI_EXPORTAR_EXCEL' | translate \"  bcs-clic='saveAsExcel()'  bcs-habilitado='true' bcs-auto-desactivar='false'  ></bcs-button>")});
					else
					{
						toolbarObj.push({template : kendo.template("<bcs-button class='col-xs-2' bcs-texto=\"'"+value.name+"' | translate \"  bcs-clic='" + value.functionName + "()' bcs-habilitado='true' bcs-auto-desactivar='false' ></bcs-button>")});
						scope[value.functionName] = value.functionCall;
					}
				});
			}
			options.toolbar = null;
		}

		//doble click
		if (!scope.dblclickRegistrado) {
			scope.dblclickRegistrado = true;
			elem.on("dblclick", function (event) {
				var tt;
				if (event !== undefined)
				{
					if ($(event.target).prop('nodeName') == "TD")
						tt = $(event.target);
					if ($(event.target).parent().prop('nodeName') == "TD")
						tt = $(event.target).parent();
				}
	
				if (tt)
				{
					scope.fnDobleClick();
				}
			
	    	});
		}
		//seleccionado
		scope.handleChange = function(event,data,dataItem,columns)
  	{
		scope.seleccionado = ($.isArray(data))? _.uniq(data): data;
  	};
	};

	return obj;

});
