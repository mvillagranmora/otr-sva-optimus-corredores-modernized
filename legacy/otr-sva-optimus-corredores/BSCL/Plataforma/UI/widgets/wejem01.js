'use strict';
angular
  .module('ngBcsComponents')
  .directive('wejem01', function () {
      var controller = function ($scope, $interval) {

        $scope.contador=0;
        $scope.timer = $interval(function(){ $scope.contador++;  }, 1000);

        //Destruye el contador
        $scope.$on('$destroy', function() {
          $interval.cancel($scope.timer);
        });

        //Pone en 0 el contador
        $scope.reinicia = function () {
          $scope.contador=0;
        }

      };
      return {
          restrict: 'EA',
          scope: true,
          controller: controller,
          templateUrl: 'widgets/wejem01.html'
      };
});
