'use strict';
angular
  .module('ngBcsComponents')
  .directive('wfonts', function () {
      var controller = function ($scope,bcsComunes) {
    	  $scope.ventana=bcsComunes.ventana;
		  $scope.bodysize = $("body").css('font-size');
		  $scope.WinX = -1;
		  $scope.WinY = -1;		  
		  $scope.WinBodyX = -1;
		  $scope.WinBodyY = -1;
		  $scope.WinFormX = -1;
		  $scope.WinFormY = -1;		
		  $scope.INWinFormX = -1;
		  $scope.INWinFormY = -1;				  
		  displayWindowSize();		  
          window.onresize = displayWindowSize;
          window.onload = displayWindowSize;
		  //------------- ************ ------------------          
          function displayWindowSize() {
              var myWidth = 0, myHeight = 0;              
        	  //General
              if( typeof( window.innerWidth ) == 'number' ) {
                  myWidth = window.innerWidth; myHeight = window.innerHeight;
              } else if( document.documentElement && ( document.documentElement.clientWidth ||document.documentElement.clientHeight ) ) {
                  myWidth = document.documentElement.clientWidth; myHeight = document.documentElement.clientHeight;
              } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
                  myWidth = document.body.clientWidth; myHeight = document.body.clientHeight;
                }
              $scope.WinX=myWidth;
              $scope.WinY=myHeight;  
              //Area Efectiva           
              $scope.WinFormX= $( "#Formulario" ).width();
              $scope.WinFormY= $( "#Formulario" ).height();  
              $scope.WinBodyX= $( "#VentanaBody").width();
              $scope.WinBodyY= $( "#VentanaBody" ).height(); 
		      $scope.INWinFormX = $scope.WinFormX;
		      $scope.INWinFormY = $scope.WinFormY;				  
          };
          
          $scope.CambiarFormWindow = function() {
        	  $( "#Formulario" ).width($scope.INWinFormX);
        	  $( "#Formulario" ).height($scope.INWinFormY);
              $scope.WinFormX= $( "#Formulario" ).width();
              $scope.WinFormY= $( "#Formulario" ).height(); 
          };
		  
          $scope.MuestraWidgets= function() {
        	  $scope.ventana.form.MuestraWidgets = ! $scope.ventana.form.MuestraWidgets;
          };
          
          $scope.CambiarLineas= function() {
        	  $scope.ventana.form.debug = ! $scope.ventana.form.debug;
          };		  
          
		  $scope.AumentarFuente = function() {
			  var size = parseInt($("body").css('font-size'));
			  console.log("Tamaño Fuente:",size);
			  if (size<20) {
				  $("body").css('font-size',size+1);
			  }
		  };
		  $scope.DisminuirFuente = function() {
			  var size = parseInt($("body").css('font-size'));
			  console.log("Tamaño Fuente:",size);
			  if (size>5) {
				  $("body").css('font-size',size-1);
			  }
		  };     
		  $scope.NormalFuente = function() {
			  console.log("Tamaño Fuente:",$scope.bodysize);
			  $("body").css('font-size',$scope.bodysize);
		  };    		  
      };
      return {
          restrict: 'EA',
          scope: true,
          controller: controller,
          templateUrl: 'widgets/wfonts.html'
      };
});
