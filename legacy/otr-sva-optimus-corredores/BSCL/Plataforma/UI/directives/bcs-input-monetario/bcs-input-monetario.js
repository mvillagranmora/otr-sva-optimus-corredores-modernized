angular.module('ModuloPlataforma').directive('bcsInputMonetario', function($timeout,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			changeEvent			: '&?',
			configuracion: '=?kOptions',
			ngModel : '=?',
			kNgModel : '=?',
			kendoWidget : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			simbolo     : '=?',
			decimales   : '=?',
			ngBlur		: '&?',
		},		
		templateUrl: 'directives/bcs-input-monetario/bcs-input-monetario.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},		
		link: function(scope, element, attrs, fn) {
			$timeout(function(){
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
				scope.simboloTemp = undefined;
				if (scope.simbolo   === undefined) { 
					scope.simboloTemp  = $rootScope.SesionIdMonedaSistema;//kendo.culture().numberFormat.currency.symbol;
				}
				else {
					scope.simboloTemp = scope.simbolo;
				}
				if (scope.decimales === undefined) {
					scope.decimales=kendo.culture().numberFormat.currency.decimals;
				}
				scope.culture= $.extend(true, {}, kendo.culture());
				scope.culture.numberFormat.currency.symbol = scope.simboloTemp;
				scope.culture.numberFormat.currency.decimals=scope.decimales;
				scope.culture.name= scope.culture.numberFormat.currency.symbol;

				var defaultOptions = { step:0,format: 'c' , min: 0 ,  max: 999999999999999999999, spinners: false, culture: scope.culture };		
				scope.kOptions = $.extend( {}, defaultOptions, scope.configuracion || {});	

				scope.$watchGroup(['decimales','simbolo','kNgModel'],function(newval,oldval){
					if (newval[0] !== oldval[0] || newval[1] !== oldval[1]) {
						scope.kOptions.culture = scope.culture;	
						scope.kOptions.culture.numberFormat.currency.symbol = scope.simbolo || '';	
						scope.kOptions.culture.numberFormat.currency.decimals=scope.decimales;
						if (scope.kendoWidget) {
							//var oldv=scope.kNgModel;
							//scope.kNgModel='';
							scope.kendoWidget.setOptions(scope.kOptions);
							scope.kendoWidget.value(scope.kNgModel);
							//setTimeout(function () { scope.kNgModel=oldv; },0);			
						}
					}
					if (newval[2] === oldval[2]) 
						return; 
					scope.ngModel = newval[2]; 
					
				});
				
				scope.fnBlur = function(e) {
					if (scope.ngBlur) scope.ngBlur();
				};
				
				scope.$watchCollection("configuracion",function(newval,oldval){
					if (scope.kendoWidget && scope.configuracion)  {
						scope.kOptions = $.extend( {}, scope.kOptions, newval || {});
						//scope.kendoWidget.setOptions(scope.configuracion);
					}
				});
				
			});
		}
	};
});