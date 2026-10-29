angular.module('ModuloPlataforma').factory('bcsOperacionesComunes',function($translate) {
	var obj = {};

	//Almacenar los registros seleccionados de una grilla en un objeto para luego
	//recuperarlos para su uso.
	var data = new Object();
	obj.AlmacenarRegistrosGrilla = function(sd){	
		//limpiar popó
		var newobj=[];
		
		if (angular.isArray(data))
		{
			for (var key in sd) {
				var obj={};
				var el=sd[key];
				for (var okey in el) {
					if (el.hasOwnProperty(okey)==true) 
						obj[okey]=el[okey];
				}
				newobj.push(obj);
			}
		}
		else
			newobj.push(sd);
			
		data = newobj;
	};
		
	obj.RecuperarRegistrosAlmacenados = function(){
		var t;
		if (angular.isArray(data))
		{
			t = [];
			var ar = [];
			for( var x in data[0] ) {
				var ob = {};
				if( !_.isFunction(data[0][x]) && i !== 'parent' && x !== 'dirty' && x !== '_events' && x !== 'uid' && x !== '_handlers' && x !== '__metadata' && x !== 'fields' && x !== 'defaults' ) {
					for (var i in data[0][x]) {
						if( !_.isFunction(data[0][x][i]) && i !== 'parent' && i !== 'dirty' && i !== '_events' && i !== 'uid' && i !== '_handlers' && i !== '__metadata' && i !== 'fields' && i !== 'defaults' ) {
							ob[i] = data[0][x][i];
						}
					}
					ar[x] = ob;
				}
				else{
					ar[0] = data[0];
					break;
				}
				
			}
			t[0] = ar;
			/*$.each(data, function(key,value) {
				t.push($.extend({},{},value));
			});*/
		}
		else
			t = $.extend({},{},data);
		data = undefined;
		return t;
	};
	//FIN
	
	return obj;
});
