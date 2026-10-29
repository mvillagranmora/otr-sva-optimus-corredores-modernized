angular.module('ModuloPlataforma').filter('kformat', function() {

  return function(text,format,simbolo,tipo,uso,muestraMoneda,tipoInstrumento) {
	//N C P E -> NUMBER
	//d, D, F, g, G, M/m, u, Y/y
	var formatType = format.charAt(0);
    if(formatType === 'x') //PARA EL CASO DE UN NUMERO DECIMAL EN STRING (0.05 = 0.05%)
    {
      return kendo.toString(Number(text)/100,'p');
    }
    
    if(formatType === 'R') //PARA EL caso de formateo de rut
    {    	
      return util.formatRut(text) ;
    }
     if( !_.isNaN(Number(text)) && (formatType === 'n' || formatType === 'c' || formatType === 'p' || formatType === 'e')) {
    	//NUMBER
    	if (formatType === 'c' && simbolo && tipo) {
    		var valor = util.bcs_formato(text,(uso || 'GEN'),tipo,simbolo,'','').valor;
    		if (muestraMoneda === 'N')
    			return valor;
    		return simbolo + " " +  valor;
       		
    	}
    	else if (formatType === 'n' && simbolo && tipo) {
    		var decimales = util.bcs_formato(0,(uso || 'GEN'),tipo,simbolo,tipoInstrumento,'').cantidadDecimales;
    		return kendo.toString(Number(text),"n"+decimales);
       		
    	}
    	else if (formatType === 'p' && simbolo && tipo) {
    		var v = util.bcs_formato(text,(uso || 'GEN'),tipo,simbolo,tipoInstrumento,'');
    		return v.valor + ' %';
    	}
    	else
   			return kendo.toString(Number(text),format);
    }
    else if (formatType === 'd' && text) {
    	return util.dateisdefault(text)? '':util.formatoDate(text);
    }
    else if (formatType === 'D' ||formatType === 'F' ||formatType === 'g' ||
    		formatType === 'G' ||formatType === 'M' ||formatType === 'm' || formatType === 'u' ||
    		formatType === 'Y' || formatType === 'y') {
        	return kendo.toString(text,format);          
        }
        else if (formatType === 'b' && (text==="S" || text==="N " ))
          return (text==="S")?"UI_SI":"UI_NO";
    else {
    	return text;
    }
  }

});

angular.module('ModuloPlataforma').filter('iif', function () {
   return function(input, trueValue, falseValue) {
        return input ? trueValue : falseValue;
   };
});

angular.module('ModuloPlataforma').filter('FormatoCapital', function() {
    return function(input) {    	
      return (!!input) ? util.FormatoCapital(input) : '';
    }
});

angular.module('ModuloPlataforma').filter('FormatoTitulo', function() {
    return function(input) {    	
      return (!!input) ? util.FormatoTitulo(input) : '';
    }
});

angular.module('ModuloPlataforma').filter('FormatoMayusculas', function() {
    return function(input) {    	
      return (!!input) ? input.toUpperCase() : '';
    }
});