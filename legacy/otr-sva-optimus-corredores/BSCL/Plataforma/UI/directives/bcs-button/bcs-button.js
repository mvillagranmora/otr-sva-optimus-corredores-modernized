angular.module('ModuloPlataforma').directive('bcsButton', function(bcsComunes,$translate) {
	return {
		restrict: 'E',
		transclude: false,
		replace: false,
		scope: {
			bcsTipo : '=?',    			 	/* tipo de boton principal,opcion,default */
			bcsTexto : '=?',				/* texto en el boton  */
			bcsIcon	 : '=?',    			/* agregar un icono al boton  */
			bcsHabilitado : '=?', 			/* activa/desactiva  el boton  */
			bcsMostrar : '=?', 			/* activa/desactiva  el boton  */
		    bcsClic  : '&?',  				/* click en el boton  */
			bcsAutoDesactivar  : '@?',      /* luego del clic desactiva boton */
			bcsBlur : '&?',
		},
		template: '<kendo-button ng-class="{\'sapUiIcon decline\' : bcsIcon == \'icon-decline\' , \'sapUiIcon edit\' : bcsIcon == \'icon-edit\' , \'sapUiIcon search\' : bcsIcon == \'icon-search\' , \'k-button-opcion\' : bcsTipo == \'opcion\' , \'k-primary\' : bcsTipo == \'principal\' , \'k-buttonPrimarySearch\' : bcsTipo == \'bcs-button-search\' , \'k-buttonPrimary\' : bcsTipo == \'bcs-button-primary\' , \'k-buttonSecondary\' : bcsTipo == \'bcs-button-secondary\' ,  \'k-buttonEdit\' : bcsTipo == \'bcs-button-edit\'  ,  \'k-buttonDecline\' : bcsTipo == \'bcs-button-anular\' , \'k-buttonInputForm\' : bcsTipo == \'bcs-button-input-form\'}" ' +
				' ng-disabled="!bcsHabilitado" ng-show="bcsMostrar" ng-blur="blur()" ng-click="btnClic()" >' +
				'{{bcsTexto|FormatoMayusculas}}' +
				'</kendo-button>',
		link: function(scope, elem, attrs) {
			if (typeof scope.bcsPrincipal === 'undefined') scope.bcsPrincipal=false;
			if (typeof scope.bcsAutoDesactivar === 'undefined') scope.bcsAutoDesactivar=true;
			//if (typeof scope.bcsHabilitado === 'undefined') scope.bcsHabilitado=false;
			if (typeof scope.bcsMostrar === 'undefined') scope.bcsMostrar=true;
			scope.blur = function(e) {
				if (scope.bcsBlur) scope.bcsBlur();
			};
			if (scope.bcsClic === undefined) {
				console.warn("funcion bcs-clic no definida");
			}
			scope.btnClic = function (e) {
				if(sesion.getAccess($(elem).attr("id"))){
					if (scope.bcsClic && (scope.bcsAutoDesactivar == "true" || scope.bcsAutoDesactivar === true)) scope.bcsHabilitado=false;
					if (scope.bcsClic) scope.bcsClic();
				}else{
					bcsComunes.msg("error",'Error',$translate.instant("UI_NO_TIENE_ACCESO"));
				}
			};
		}
  };
});
