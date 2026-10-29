angular.module('ModuloPlataforma').directive('bcsRequerido', function() {
	return {
		restrict: 'A',
		require: ['^?form', '?bcsComboboxMultiseleccion',
                  '?bcsComboboxBase','?bcsInputNumero','?bcsInputDatetime','?bcsInputMonetario',
                  '?bcsInputTexto','?bcsInputDate','?bcsInputTime','?bcsInputPorcentaje','?bcsInputPassword','?bcsInputCheckgroup',                  
                  '?bcsComboboxClientePortafolio','?bcsComboboxInstrumentos','?bcsInputInstrumentos','?bcsInputInstrumentosDv','?bcsInputPlazo','?bcsInputCondicionLiquidacion','?bcsInputCondicionLiquidacionIntegracion'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					if ((newval === null || newval === undefined || newval === '' || newval === false  || util.esFechaAnteriorCorte(newval)) && binding)
					{
						element.addClass('invalid-bcs-required');
						formCtrl.$setDirty();
						formCtrl.$setValidity('bcs-required', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-required');
						formCtrl.$setValidity('bcs-required', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsMayorQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsComboboxBase','?bcsInputNumero','?bcsInputDatetime','?bcsInputMonetario',
                  '?bcsInputTexto','?bcsInputDate','?bcsInputTime','?bcsInputPorcentaje',                  
                  '?bcsComboboxClientePortafolio','?bcsComboboxInstrumentos','?bcsInputPlazo'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsMayorQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					var requerido = newvalArr[2] !==undefined? newvalArr[2]: true;
					if (binding === "false") binding = false;
					if (requerido && (newval === null || newval === undefined || newval === '' || newval === false || newval <= Number(binding)))
					{
						element.addClass('invalid-bcs-mayor-que');
						formCtrl.$setValidity('bcs-mayor-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-mayor-que');
						formCtrl.$setValidity('bcs-mayor-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsMenorQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsComboboxBase','?bcsInputNumero','?bcsInputDatetime','?bcsInputMonetario',
                  '?bcsInputTexto','?bcsInputDate','?bcsInputTime','?bcsInputPorcentaje',                  
                  '?bcsComboboxClientePortafolio','?bcsComboboxInstrumentos','?bcsInputPlazo'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsMenorQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || newval >= Number(binding)))
					{
						element.addClass('invalid-bcs-menor-que');
						formCtrl.$setValidity('bcs-menor-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-menor-que');
						formCtrl.$setValidity('bcs-menor-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsMenorIgualQue', function(bcsComunes, $translate) {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsComboboxBase','?bcsInputNumero','?bcsInputDatetime','?bcsInputMonetario',
                  '?bcsInputTexto','?bcsInputDate','?bcsInputTime','?bcsInputPorcentaje',                  
                  '?bcsComboboxClientePortafolio','?bcsComboboxInstrumentos','?bcsInputPlazo'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsMenorIgualQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || newval > Number(binding)))
					{
						element.addClass('invalid-bcs-menor-igual-que');
						formCtrl.$setValidity('bcs-menor-igual-que', false,element);
						bcsComunes.msg("error", "Error", util.translate($translate, 'UI_REQUIERE_MENOR_IGUAL_QUE', { umbral: binding }));
					}
					else 
					{
						element.removeClass('invalid-bcs-menor-igual-que');
						formCtrl.$setValidity('bcs-menor-igual-que', true,element);
					}
				});

			}
        }
    }
});


angular.module('ModuloPlataforma').directive('bcsMayorIgualQue', function(bcsComunes, $translate) {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsComboboxBase','?bcsInputNumero','?bcsInputDatetime','?bcsInputMonetario',
                  '?bcsInputTexto','?bcsInputDate','?bcsInputTime','?bcsInputPorcentaje',                  
                  '?bcsComboboxClientePortafolio','?bcsComboboxInstrumentos','?bcsInputPlazo'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsMayorIgualQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || newval < Number(binding)))
					{
						element.addClass('invalid-bcs-mayor-igual-que');
						formCtrl.$setValidity('bcs-mayor-igual-que', false,element);
						bcsComunes.msg("error", "Error", util.translate($translate, 'UI_REQUIERE_MAYOR_IGUAL_QUE', { umbral: binding }));
					}
					else 
					{
						element.removeClass('invalid-bcs-mayor-igual-que');
						formCtrl.$setValidity('bcs-mayor-igual-que', true,element);
					}
				});

			}
        }
    }
});




angular.module('ModuloPlataforma').directive('bcsFechaMayorIgualQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputDate','?bcsInputDateTime'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsFechaMayorIgualQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					
					var a = moment(newval);
					var b = moment(binding);

					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || a.diff(b) < 0))
					{
						element.addClass('invalid-bcs-fecha-mayor-igual-que');
						formCtrl.$setValidity('bcs-fecha-mayor-igual-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-fecha-mayor-igual-que');
						formCtrl.$setValidity('bcs-fecha-mayor-igual-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsFechaMayorQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputDate','?bcsInputDateTime'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsFechaMayorQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					
					var a = moment(newval);
					var b = moment(binding);

					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || a.diff(b) <= 0))
					{
						element.addClass('invalid-bcs-fecha-mayor-que');
						formCtrl.$setValidity('bcs-fecha-mayor-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-fecha-mayor-que');
						formCtrl.$setValidity('bcs-fecha-mayor-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsFechaMenorIgualQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputDate','?bcsInputDateTime'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsFechaMenorIgualQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					
					var a = moment(newval);
					var b = moment(binding);

					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || a.diff(b) > 0))
					{
						element.addClass('invalid-bcs-fecha-menor-igual-que');
						formCtrl.$setValidity('bcs-fecha-menor-igual-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-fecha-menor-igual-que');
						formCtrl.$setValidity('bcs-fecha-menor-igual-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsFechaMenorQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputDate','?bcsInputDateTime'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;

				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsFechaMenorQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					
					var a = moment(newval);
					var b = moment(binding);

					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || a.diff(b) >= 0))
					{
						element.addClass('invalid-bcs-fecha-menor-que');
						formCtrl.$setValidity('bcs-fecha-menor-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-fecha-menor-que');
						formCtrl.$setValidity('bcs-fecha-menor-que', true,element);
					}
				});

			}
        }
    }
});


angular.module('ModuloPlataforma').directive('bcsFechaIgualQue', function() {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputDate','?bcsInputDateTime'],

        link: function(scope, element, attrs, ctrls) {
			var formCtrl = ctrls[0];
			var elemCtrl = {};
			
			for (var i = 1; i<ctrls.length; i++)
			{
				if (ctrls[i]) 
				{
					elemCtrl = ctrls[i];
					break;
				}
			}
			
			if (formCtrl && elemCtrl)
			{
				if (attrs.bcsRequerido === undefined) attrs.bcsRequerido = false;
				
				scope.$watchGroup([elemCtrl.getSeleccionado,attrs.bcsFechaIgualQue,attrs.bcsRequerido],function(newvalArr,oldvalArr){
					var newval = newvalArr[0];
					var binding = newvalArr[1] !==undefined? newvalArr[1]: true;
					if (binding === "false") binding = false;
					
					var a = moment(newval);
					var b = moment(binding);

					if (newvalArr[2] && (newval === null || newval === undefined || newval === '' || newval === false || a.diff(b) !== 0))
					{
						element.addClass('invalid-bcs-fecha-igual-que');
						formCtrl.$setValidity('bcs-fecha-igual-que', false,element);
					}
					else 
					{
						element.removeClass('invalid-bcs-fecha-igual-que');
						formCtrl.$setValidity('bcs-fecha-igual-que', true,element);
					}
				});

			}
        }
    }
});

angular.module('ModuloPlataforma').directive('bcsValidarRut', function($translate,bcsComunes) {
	return {
		restrict: 'A',
		require: ['^?form',
                  '?bcsInputTexto'],
		//falta agregar metodo para formatear el ngModel en el padre (11.111.111-1)
		//falta evaluar constantemente si cambia el uso del bind (por si cambia el tipo de documento)
		//
        link: function(scope, element, attrs, ctrls) {

        	scope.obtieneDigitoVerificador = function dv(T) {
				var M=0,S=1;for(;T;T=Math.floor(T/10))
					S=(S+T%10*(9-M++%6))%11;return S?S-1:'K';
			};
			scope.formatearRut = function(doc, dv){
				doc = (doc+".").replace(/(\S)(?=(\S{3})+\.)/g, '$1.');
				return doc.substring(0, doc.lastIndexOf("."))+"-"+dv;	
			};
			scope.validarFormato = function(rut){
				if(rut.match(new RegExp("[a-z|A-Z]"))!==null || rut.indexOf("-") < 0 || rut.indexOf("-") != rut.lastIndexOf("-")){
					return "";
				}else{
					return rut;
				}
			}
			scope.limpiarRut = function(doc){
				doc = scope.validarFormato(doc || "");
				return doc.replace(new RegExp("\\.", "g"), "").replace(new RegExp("-", "g"), "");//obtener solo rut sin puntos ni guiones
			};
			scope.validarRut = function(newval){
				var d = newval;
				var doc = scope.limpiarRut(d);
				var rut = doc.substring(0, doc.length-1);
				var dv = doc.substring(doc.length-1);

				if( doc.length>=2 && rut.length < 10 && dv == scope.obtieneDigitoVerificador(rut)){
					element.removeClass('invalid-bcs-validar-rut');
					formCtrl.$setValidity('bcs-validar-rut', true,element);
					return scope.formatearRut(rut, dv);
				}else{
					element.addClass('invalid-bcs-validar-rut');
					formCtrl.$setDirty();
					formCtrl.$setValidity('bcs-validar-rut', false,element);
					if(newval && newval!="")
						bcsComunes.msg("error",'Error',$translate.instant('UI_RUT_INVALIDO'));
				}
			};
			
			

			var formCtrl = ctrls[0];
			var elemCtrl = (ctrls[1])?ctrls[1] : null;
			var newval = elemCtrl.getSeleccionado();
			var binding = (attrs.bcsValidarRut !==undefined || attrs.bcsValidarRut===true)? true: false;

			if(binding && formCtrl && elemCtrl){
				if(scope.binded==false){
					element.find("input").bind("blur", function (e) {
						scope.validarRut(elemCtrl.getSeleccionado());
					});
					scope.binded=  true;
				}else{
					element.removeClass('invalid-bcs-validar-rut');
					formCtrl.$setValidity('bcs-validar-rut', true,element);
				}
        	};
        }
    }
});