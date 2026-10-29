angular.module('ModuloPlataforma').directive('bcsLink', function(bcsComunes) {
	return {
		restrict: 'E',
		template : '<i class="fa fa-arrow-right" aria-hidden="true"></i>',
        link: function(scope, element, attrs, ctrls) {
			
        	element.bind('click', function(e) {
        		var ventana = scope.$eval(attrs.link);
        		var params = scope.$eval(attrs.params);
        		if (ventana && params)
        			bcsComunes.ventanaAbrir(ventana,params,false,false);
        		
        	});
        	
        	
        	
        	
        	
        	
        	
        }
    }
});
