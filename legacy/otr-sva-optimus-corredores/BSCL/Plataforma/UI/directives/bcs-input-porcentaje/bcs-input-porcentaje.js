angular.module('ModuloPlataforma').directive('bcsInputPorcentaje', function($timeout) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			changeEvent			: '&?',
			kOptions			: '=?',
			ngModel 			: '=?',
			kNgModel 			: '=?',
			kendoWidget 		: '=?',
			kNgDisabled 		: '=?',
			kNgReadonly 		: '=?',
			decimales   		: '=?',
			formato				: '=?',
			foco				: '@?', // JG: Para dar foco en el input. 
			permiteNegativo		: '=?', // JG: input permite porcentajes negativos
			limitarMaximo		: '=?', // SG: input limita maximo a 100%
			redondea 			: '=?' // SG: Redondea o no el decimal
		},
		templateUrl: 'directives/bcs-input-porcentaje/bcs-input-porcentaje.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},
		link: function(scope, element, attrs, fn) {
			$timeout(function(){
				
				if (scope.foco === undefined) scope.foco = false;  
				if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
				if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;					
				scope.$on("kendoWidgetCreated", function (event, widget) {
					if (widget.$angular_scope === scope) {
						scope.kendoWidget = widget;
						scope.kendoWidget.element.on('focus', function () {
							var input = $(this);
							setTimeout(function () { input.select(); });
						});						
						scope.kendoWidget.element.on('keypress', function(e) {
							if (e.keyCode === 13) {
              					e.preventDefault();
            				}							
						});
					}
				});
				
				if (scope.redondea === undefined) scope.redondea = true;
				if (scope.decimales === undefined) scope.decimales=kendo.culture().numberFormat.percent.decimals;
				scope.culture= $.extend(true, {}, kendo.culture());
				scope.culture.numberFormat.percent.decimals=scope.decimales;
				scope.culture.numberFormat.decimals=scope.decimales;
				
				var defaultOptions = { 
					step: 0, 
					round: scope.redondea,
					format: '0.' + (new Array(scope.decimales + 1).join("0")) + ' \\' + scope.culture.numberFormat.percent.symbol, 
					min: (scope.permiteNegativo === true) ? undefined : 0, 
					max: (scope.limitarMaximo === true) ? 100 : undefined, 
					spinners: false, culture: scope.culture 
				};
				
				scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions || {});
				if (scope.formato) scope.kOptions.format = scope.formato;
				
				scope.$watch('decimales',function(newval,oldval){
					if (newval == oldval) return;
					scope.kOptions.culture = scope.culture;	
					scope.kOptions.culture.numberFormat.decimals=scope.decimales;
					scope.kOptions.format = '0.'+(new Array(scope.decimales+1).join( "0" ))+' \\' + scope.culture.numberFormat.percent.symbol;
					//scope.kOptions.culture.numberFormat.percent.decimals=scope.decimales;
					if (scope.kendoWidget) {
						var oldv=scope.kNgModel;
						scope.kNgModel='';
						scope.kendoWidget.setOptions(scope.kOptions);	
						setTimeout(function () { scope.kNgModel=oldv; });			
					}
				});
				
				scope.$watchCollection("kOptions",function(newval,oldval){
					if (angular.equals(newval, oldval)) return;
					if (scope.kendoWidget) {
						scope.kendoWidget.setOptions(scope.kOptions);
					}
				});
				
				scope.$watch("kNgModel",function(newval,oldval){ 
					if (newval == oldval) return;
					scope.ngModel = newval; 
				});
				
				scope.$watch(scope.foco, function(newValue){
	                if (newValue == true) element.find('input').focus();
	            });
				
				scope.$watch("permiteNegativo", function(newval,oldval){
					if (newval == oldval) return;
	                if (newval == true)scope.kOptions.min=undefined;
	                else scope.kOptions.min=0;
	            });
				
			});
		}
	};
});
