angular.module('ModuloPlataforma').directive('bcsGridSimple', function($rootScope) {
	return {
		restrict: 'E',
		replace: false,
		transclude: true,
		scope: {
			configuracion : '=?',
			fnDobleClick : '&?',
			fnExportarExcel : '=?',
			seleccionado : '=?',
			kendoWidget : '=?',
		},
		templateUrl: 'directives/bcs-grid-base/bcs-grid-simple.html',
    link: function(scope, elem, attrs) {
			
				scope.$on("kendoWidgetCreated", function (event, widget) {
					if (widget.$angular_scope === scope) {
						scope.kendoWidget = widget;
						scope.fnExportarExcel = function()
						{
							scope.kendoWidget.saveAsExcel();
						}
					}
				});					
				
				scope.handleChange = function(event,data,dataItem,columns)
			  	{
					scope.seleccionado = ($.isArray(data))? _.uniq(data): data;
			  	};
				
				scope.$watch("configuracion",function(newval,oldval){
					scope.kOptions = scope.configuracion;
					if (scope.elemento) {
						scope.elemento.setOptions(scope.kOptions);						
					}
				});
				
    }
  };
});

angular.module('ModuloPlataforma').directive('bcsGridBase', function($translate) {
	return {
		restrict: 'E',
		replace: false,
		scope: false,
		transclude: true,
		templateUrl: 'directives/bcs-grid-base/bcs-grid-base.html',
    link: function(scope, elem, attrs) {

		scope.$on("kendoWidgetCreated", function (event, widget) {
			if (widget.$angular_scope === scope)
				scope.kendoWidget = widget;
		});
		/*scope.hola = function(){
			console.log("hola");
		}*/
    	var defaultOptions = {
    			sortable: true,
                reorderable: true,
                filterable: true,
    			groupable: true,
    			pageable: true,
    			selectable: "multiple, row",
    			columnMenu: false,
    			reorderable: true,
    			resizable: true,
                autoBind: false,
                scrollable: true,
                height:600,
    			//toolbar: ["excel"],
                //toolbar: [{template: kendo.template("<label style='padding-right:10px'>Titulo Grilla</label>")},"excel","pdf",{template: kendo.template("<input ng-click='hola()' type='button' value='un boton'</input>")}],
    	};
    	elem.on("dblclick", function (event) {
    		if (event !== undefined && $(event.target).prop('nodeName') == "TD")
    		{
    			scope.onDoubleClick({data: scope.kendoWidget.dataItem($(event.target).parent())});
    		}

            });
    	scope.handleChange = function(event,data,dataItem,columns)
    	{
    		scope.kNgModel = data;
    	};

		scope.kOptions = scope.kOptions || {};
		scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions );
    }
  };
});

angular.module('ModuloPlataforma').directive('bcsGridBaseV2', function(bcsConfiguracion,$timeout,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		transclude: true,
		scope: {
			nombre : '=?',
			configuracion : '=?',
			fnDobleClick : '&?',
			fnExportarExcel : '=?',
			seleccionado : '=?',
			elemento : '=?',
		},
		transclude: true,
		templateUrl: 'directives/bcs-grid-base/bcs-grid-base.html',
    link: function(scope, elem, attrs) {
			$timeout(function(){
				scope.$on("kendoWidgetCreated", function (event, widget) {
					if (widget.$angular_scope === scope) {
						scope.kendoWidget = widget;					
					}
				});					
				$rootScope.$watch("PaginaCargada",function(newval,oldval){
					if (newval==true && scope.kendoWidget) {
						 scope.kendoWidget.refresh();
					};
				});
				//bcsConfiguracion.configurarGrid(scope,elem.find("[kendo-grid]"),scope.configuracion,scope.botones);
				//scope.kOptions = scope.configuracion;
				scope.$watch("configuracion",function(newval,oldval){
					bcsConfiguracion.configurarGrid(scope,elem.find("[kendo-grid]"),scope.configuracion,scope.botones);
					scope.kOptions = scope.configuracion;
					if (scope.elemento) {
						scope.elemento.setOptions(scope.kOptions);						
					}
				});
				
			});
    }
  };
});
