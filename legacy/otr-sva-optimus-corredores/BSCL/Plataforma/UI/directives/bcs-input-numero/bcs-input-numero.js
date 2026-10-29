angular.module('ModuloPlataforma').directive('bcsInputNumero', function($timeout) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			changeEvent			: '&?',
			configuracion		: '=?kOptions',
			ngModel 			: '=?',
			kNgModel 			: '=?',
			kendoWidget 		: '=?',
			kNgDisabled 		: '=?',
			kNgReadonly 		: '=?',
			decimales   		: '=?',
			formato				: '=?',
			foco				: '=?', // JG:Para dar foco en el input. 
			ngBlur				: '&?',
			sinRedondeo   		: '=?'
		},
		templateUrl: 'directives/bcs-input-numero/bcs-input-numero.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},
		link: function(scope, element, attrs, fn) {
			$timeout(function(){
				if (scope.foco === undefined) scope.foco = false;  
				if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
				if (scope.kNgReadonly == undefined) scope.kNgReadonly = false;			
				if (scope.sinRedondeo === undefined) scope.sinRedondeo = false;
				
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
				
				let defaultOptions = {
					step 		: 0,
					min 		: 0,
					max 		: 999999999999999,
					spinners 	: false,
					round		: !scope.sinRedondeo
				};

				if (!scope.sinRedondeo) {
					if (scope.decimales === undefined) scope.decimales = kendo.culture().numberFormat.decimals;
					scope.culture = $.extend(true, {}, kendo.culture());
					scope.culture.numberFormat.decimals = scope.decimales;
					defaultOptions.format = 'n';
					defaultOptions.culture = scope.culture;
					
					scope.kOptions = $.extend({}, defaultOptions, scope.configuracion || {});
					if (scope.formato) scope.kOptions.format = scope.formato;
			

				}else{
					let numDecimales = scope.decimales !== undefined ? scope.decimales : 14;
					defaultOptions.decimals = numDecimales;
					defaultOptions.format = numDecimales > 0 ? "#." + "#".repeat(numDecimales) : "#";
					defaultOptions.culture = scope.culture;
					scope.kOptions = $.extend({}, defaultOptions, scope.configuracion || {});
				}

		
				scope.$watch('decimales', function (newval, oldval) {
					if (newval == oldval || scope.sinRedondeo) return;
					scope.kOptions.culture = scope.culture;
					scope.kOptions.culture.numberFormat.decimals = scope.decimales;
					if (scope.kendoWidget) {
						scope.kendoWidget.setOptions(scope.kOptions);
						scope.kendoWidget.value(scope.kNgModel);
					}
				});

				scope.fnBlur = function(e) {
					if (scope.ngBlur) scope.ngBlur();
				};

				scope.$watch("configuracion",function(newval,oldval){
					if (scope.kendoWidget && scope.configuracion && JSON.stringify(newval) !== JSON.stringify(oldval)) {
						if (!scope.sinRedondeo) {
							scope.kendoWidget.setOptions(scope.configuracion);
						}else{
							scope.kendoWidget.setOptions($.extend({}, defaultOptions, newval || {}));
						}
					}
				});
				
				scope.$watch("kNgModel",function(newval,oldval){ 
					if (newval == oldval) return;
					scope.ngModel = newval; 
				});
				
				scope.$watch('foco', function(newValue){
	                if (newValue == true) element.find('input').focus();
	            });
				
			});
		}
	};
});
