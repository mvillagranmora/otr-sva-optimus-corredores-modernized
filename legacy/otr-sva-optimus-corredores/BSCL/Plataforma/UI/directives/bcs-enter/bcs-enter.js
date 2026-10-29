angular.module('ModuloPlataforma').directive('bcsEnter', function () {
    return function (scope, element, attrs) {
        element.on("keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.bcsEnter);
                    document.activeElement.blur();
                });
                event.preventDefault();
            }
        });
    };
});

angular.module('ModuloPlataforma').directive('bcsTab', function () {
    return function (scope, element, attrs) {
        element.on("keydown", function (event) {
            if(event.which === 9) {
                scope.$apply(function (){
                    scope.$eval(attrs.bcsTab);
                });
                //event.preventDefault();
            }
        });
    };
});
angular.module('ModuloPlataforma').directive('bcsEscape', function () {
    return function (scope, element, attrs) {
      element.on("keydown", function (event) {
      event.stopPropagation();
            if(event.which === 27) {
                scope.$apply(function (){
                    scope.$eval(attrs.bcsEscape);                    
                });
              
            }
        });
    };
});
angular.module('ModuloPlataforma').directive('bcsEnters', function () {
    return function (scope, element, attrs) {
        element.on("keydown", function (event) {
            if(event.which === 13) {
                scope.$apply(function (){
                    scope.$eval(attrs.bcsEnter);
                });
                event.preventDefault();
            }
        });
    };
});
