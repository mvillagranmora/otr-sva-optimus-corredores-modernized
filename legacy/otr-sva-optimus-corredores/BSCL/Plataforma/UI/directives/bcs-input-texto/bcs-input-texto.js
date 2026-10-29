angular.module('ModuloPlataforma').directive('bcsInputTexto', function($parse) {
	return {
		restrict: 'E',
	    require: 'ngModel',
		replace: false,
		scope: {
			ngModel 		: '=?',
			ngDisabled 		: '=?kNgDisabled',
			tipoCampo 		: '=?', //TEXTAREA o INPUT
			soloMayusculas	: '@?', //DEFAULT EN S
			atributos		: '@?',
			foco			: '@?', // JG:Para dar foco en el input. 
			placeholder		: '=?',
			ngBlur			: '&?',
			ngFocus			: '&?',
			keyEnter		: '&?', // JG:Asocia funcion al presionar Enter.
			maxlength		: '@?'
		},
		templateUrl: 'directives/bcs-input-texto/bcs-input-texto.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.ngModel;
			}
		},
		link: function(scope, element, attrs, controller, transcludeFn) {
			if (scope.maxlength) { 
				element.find("input").attr('maxlength',scope.maxlength);
				element.find("textarea").attr('maxlength',scope.maxlength);
			}
			if (scope.soloMayusculas === undefined) scope.usaMayusculas='S';
			else  scope.usaMayusculas = scope.soloMayusculas;
				
			if (scope.placeholder === undefined) scope.placeholder = '';
			if (scope.foco === undefined) scope.foco = false;  
			if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
			if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;

			scope.$watch(scope.foco, function(newValue){
                if (newValue == true) element.find('input').focus();
            });
						
			scope.fnBlur = function(e) {
				if (scope.ngBlur) scope.ngBlur();
			};
			
			scope.fnFocus = function(e) {
				if (scope.ngFocus) scope.ngFocus();
			};
			
			scope.keyPress = function(e) {
				if (scope.keyEnter && e.keyCode === 13) scope.keyEnter();
				//if (scope.keyXXX && e.keyCode === XX) scope.keyXXX(); //Otra tecla
			};
	
		}
	};
});
