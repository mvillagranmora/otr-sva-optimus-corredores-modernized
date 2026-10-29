angular.module('ModuloPlataforma').directive('bcsInputDatetime', function($timeout,$rootScope) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			kOptions: '=?',
			ngModel : '=?',
			kNgModel : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			idPais      : '=?', //ID de Pais para habiles: Default $rootScope.SesionIdPaisSistema
			todoDias   : '=?',  //Todo los días. Default false			
		},
		template: '<input kendo-date-time-picker placeholder="{{placeholder}}" k-ng-delay="kOptions" k-ng-readonly="kNgReadonly" k-ng-disabled="kNgDisabled" k-options="kOptions" ng-model="ngModel" k-ng-model="kNgModel" ng-blur="validacion($event)"/>',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},
		link: function(scope, element, attrs, fn) {
			$timeout(function() {
				scope.kOptions = scope.kOptions || {};						
				if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
				if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;	
				if (scope.idPais == undefined) scope.idPais=$rootScope.SesionIdPaisSistema;
				if (scope.todoDias == undefined) scope.todoDias=false;
				scope.placeholder = $rootScope.SesionValoresDefectoSistema.PLACEHOLDER_FECHA || '';
				var defaultOptions = { format : "g" };		
				defaultOptions.dates = bcs_feriados;
				scope.validacion = function(e) {
					if (!scope.kNgModel || isNaN( scope.kNgModel.getTime())) {
						scope.ngModel = '';
				        return false;
				      }
				      return true;
				};
				defaultOptions.month ={								
	             content: 	 '# if ('+scope.todoDias+' == false  && util.esFechaFeriado('+scope.idPais+',data.date)==true )  { #' +
	                             '<div class="dia-nohabil dia-feriado">#= data.value #</div>' +
	            	 		 '# }else if ('+scope.todoDias+' == false  && util.esFechaHabil('+scope.idPais+',data.date)==false )  { #' +
	                             '<div class="dia-nohabil">#= data.value #</div>' +
	                         '# } else { #' +
	                         '#= data.value #' +
	                         '# } #' 
	             };
	        	defaultOptions.open = function(e){
	              $(".dia-nohabil").parent().removeClass("k-link");
	              $(".dia-nohabil").parent().removeAttr("href");
	            };				
				scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions || {});	
			});
		}
	};
});
