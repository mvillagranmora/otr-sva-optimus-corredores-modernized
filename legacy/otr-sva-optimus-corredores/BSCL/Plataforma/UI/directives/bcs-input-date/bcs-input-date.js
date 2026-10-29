angular.module('ModuloPlataforma').directive('bcsInputDate', function($timeout,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			configuracion: '=?kOptions',
			ngModel : '=?',
			kNgModel : '=?',
			kendoWidget : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			idPais      : '=?', //ID de Pais para habiles: Default $rootScope.SesionIdPaisSistema
			todoDias   : '=?',  //Todo los días. Default false
			ngBlur		: '&?',
			kNgChange: '&?'
		},		
		template: '<span><input kendo-date-picker="datepicker" placeholder="{{placeholder}}"  k-ng-delay="kOptions" k-ng-disabled="kNgDisabled" k-scope-field="datePicker" k-options="kOptions" ng-model="ngModel" k-ng-model="kNgModel" ng-blur="fnBlur()" ng-change="kNgChange()"  /></span>',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},
    link: function(scope, element, attrs, fn) {    
    		$timeout(function() {
				if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
				if (scope.idPais == undefined) scope.idPais=$rootScope.SesionIdPaisSistema;
				if (scope.todoDias == undefined) scope.todoDias=false;
				scope.placeholder = $rootScope.SesionValoresDefectoSistema.PLACEHOLDER_FECHA || '';
				var defaultOptions = { format : "d" };
				defaultOptions.dates = bcs_feriados;
				scope.$watch('kNgModel',function(newval,oldval){
					if (angular.isDate(scope.kNgModel) && scope.kNgModel.getTime())
						return true;
					if (angular.isString(scope.kNgModel) && util.dateInicioDia(scope.kNgModel).getTime()) {
						return true;
					}
					
					scope.ngModel = '';
					return false;
				});
					
				defaultOptions.month ={								
	             content: 	 '# if ('+scope.todoDias+' == false  && util.esFechaFeriado('+scope.idPais+',data.date)==true )  { #' +
	                             '<div class="dia-nohabil dia-feriado">#= data.value #</div>' +
	            	 		 '# }else if ('+scope.todoDias+' == false  && util.esFechaHabil('+scope.idPais+',data.date)==false )  { #' +
	                             '<div class="dia-nohabil">#= data.value #</div>' +
	                         '# } else { #' +
	                         '#= data.value #' +
	                         '# } #' 
	             };
				scope.first=true;
				scope.navigate = function() {
					$(".dia-nohabil").parent().removeClass("k-link");
					$(".dia-nohabil").parent().removeAttr("href");
				};				
	        	defaultOptions.open = function(e){
	        	  scope.navigate();
	              if (scope.first) {
	            	  $(".k-nav-prev").click(scope.navigate);
	            	  $(".k-nav-next").click(scope.navigate);
	            	  scope.first=false;
	              }	              
	            };
	            scope.oldValue = scope.$root.SesionFechaSistema;
	            defaultOptions.change = function(e){
	            	if(!scope.todoDias){
						var dt = e.sender;
						var oldValue = dt._old;
						var value = dt.value();
						if(util.esFechaFeriado(scope.idPais, value) || !util.esFechaHabil(scope.idPais, value)){
							dt.value(scope.oldValue);
						}else{
							scope.oldValue = value;
						}
					}
	            }
				scope.kOptions = $.extend( {}, defaultOptions, scope.configuracion || {} );
				
				
				scope.$watch("configuracion",function(newval,oldval){
					if (scope.datepicker && newval) scope.datepicker.setOptions(scope.configuracion);
				});
				
				scope.fnBlur = function(e) {
					if(scope.configuracion && scope.configuracion.min){
						if(!scope.kNgModel ||  moment(scope.kNgModel).isBefore(scope.configuracion.min)){
							scope.ngModel = scope.kNgModel;
						}
					}
					if(!scope.kNgModel){
						scope.ngModel = '';
					}
					if (scope.ngBlur) scope.ngBlur();
				};
				
				
				scope.$on("kendoWidgetCreated", function (event, widget) {
					if (widget.$angular_scope === scope) {
						scope.kendoWidget = widget;
						
					}
				});
				
    		});
    }
  };
});