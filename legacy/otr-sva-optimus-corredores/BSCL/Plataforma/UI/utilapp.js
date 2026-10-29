var util = {}; 
util.zonaLocal = function(){
	return "es-CL";
};
util.formateaNumero = function(numeroStr, decimales){
	if (numeroStr === undefined || numeroStr === null || numeroStr === '') return '';
	return Number(Number(numeroStr).toFixed(decimales)).toLocaleString(util.zonaLocal(), { minimumFractionDigits:decimales })
};
/*********************
 *  MANEJO DE FECHAS 
 ********************/
util.dateDS= function (obj,tipoDato,tipoOrigen) {
	if (tipoOrigen==='ODATA') {
		if (!(obj instanceof Date)) {
			throw new Error("dateDS: obj debe ser Date para ODATA");
		}
		if (tipoDato==='FechaSistema' || tipoDato==='Fecha') { //fecha local
			var p1=moment(obj).utc().format("YYYY-MM-DD");
			return util.dateDS(moment(p1).toDate(),'FIXS','FIXS');
		}
		else if (tipoDato==='FechaHoraSistema') { //fecha local  +  hora UTC
			var mdate=moment(obj).utc().format("YYYY-MM-DD");
			var mhour=moment.utc(mdate + ' ' + moment(obj).utc().format('HH:mm:ss')).local().format('HH:mm:ss');
			return util.dateDS(moment(mdate+' '+mhour,'YYYY-MM-DD HH:mm:ss').toDate(),'FIXS','FIXS');
		}
		else if (tipoDato==='HoraSistema') {	//fecha local  +  hora UTC
			var mdate=moment("0001-01-01").format("YYYY-MM-DD");			
			var mhour=moment.utc(moment().format('YYYY-MM-DD') + ' ' + moment(obj).utc().format('HH:mm:ss')).local().format('HH:mm:ss');		
			return util.dateDS(moment(mdate+' '+mhour,'0001-01-01 HH:mm:ss').toDate(),'FIXS','FIXS');
		}	
		else if (tipoDato==='FechaHora') {  //Fecha UTC			
			return util.dateDS(obj,'FIX','FIX');	
		}		
		else {
			throw new Error("dateDS: Tipo tipoDato inválido "+tipoDato);
		}		
	}
	else if (tipoOrigen==='CALL') {		
		if (obj instanceof Date) {
			throw new Error("dateDS: obj no puede ser Date para CALL");
		}
		if (tipoDato==='FechaSistema' || tipoDato==='Fecha' ) { //fecha local
			return util.dateDS(moment(obj.substring(0,10)).toDate(),'FIXS','FIXS');
		}
		else if (tipoDato==='FechaHoraSistema') {	//fecha local  +  hora UTC
			var mdate=moment(obj.substring(0,10)).format("YYYY-MM-DD");			
			var mhour=moment.utc(mdate + ' ' + obj.substring(11)).local().format('HH:mm:ss');		
			return util.dateDS(moment(mdate+' '+mhour,'YYYY-MM-DD HH:mm:ss').toDate(),'FIXS','FIXS');
		}
		else if (tipoDato==='HoraSistema') {	//fecha local  +  hora UTC
			var mdate=moment("0001-01-01").format("YYYY-MM-DD");			
			var mhour=moment.utc(moment().format('YYYY-MM-DD') + ' ' + obj.substring(0,10)).local().format('HH:mm:ss');		
			return util.dateDS(moment(mdate+' '+mhour,'0001-01-01 HH:mm:ss').toDate(),'FIXS','FIXS');
		}		
		else if (tipoDato==='FechaHora') { //fecha completa UTC
			return util.dateDS(moment.utc(obj.substring(0,10)+'T'+obj.substring(11)).toDate(),'FIX','FIX');
		}
		else {
			throw new Error("dateDS: Tipo tipoDato inválido "+tipoDato);
		}		
	}
	else if ((tipoOrigen==='FIX' || tipoOrigen==='FIXS') && obj instanceof Date) {		
		if (obj.getTime()>util.datecorte()) { //algunos programas como kendo no convierten correctamente
			return obj;   
		}else {
			if (tipoOrigen==='FIXS')  
				return util.datesysdefault(); 
			else 
				return util.datedefault();
		}		
	}
	else {
		throw new Error("dateDS: Tipo Origen inválido "+tipoOrigen);
	}
};
util.fixUtcDate = function(jsDate){
	return moment.parseZone(jsDate);
}
util.datefix = function (obj) {
	console.log("********************************************");
	console.log("datefix no se usa, usar por Datasource entregado por arquitectura");
	console.log("Este mensaje se transformara en error posterior 31/05");
	console.log("********************************************");
	if (BCS_AMBIENTE=='DES' && moment(new Date()).format('YYYYMMDD')>20160531) {
		throw new Error("datefix: funcion eliminada");
	}
	if (obj instanceof Date) {
		if (obj.getTime()>util.datecorte()) { //algunos programas como kendo no convierten correctamente
			var jsOFFSET = moment(new Date()).format("Z"); //-+03:00	
			var jsOFFSETMins = -1*(jsOFFSET.split(':')[0]*60+jsOFFSET.split(':')[1]*1);
			return moment.utc(obj).add(jsOFFSETMins,'minutes').local().toDate();   
		}else {
			return util.datedefault();
		}
	}else if (typeof obj === "string" && obj.length==10) {
		var jsOFFSET = moment(new Date()).format("ZZ");
		return moment(obj+' 00:00:00 '+jsOFFSET,'YYYY-MM-DD HH:mm:ss ZZ').local().toDate();		
	}else if (typeof obj === "string" && obj.length==19) {
		var jsOFFSET = moment(new Date()).format("ZZ");
		return moment(obj+' '+jsOFFSET,'YYYY-MM-DD HH:mm:ss ZZ').local().toDate();		
	}else if (typeof obj === "string" && obj.length==24 && obj.indexOf('T00:00:00.000Z')>0) {
		return util.datefix(new Date(obj));		
	}else if (typeof obj === "string" && obj.length==24 && obj.indexOf('T')>0 && obj.indexOf('Z')>0) {
		return moment(obj).local().toDate();		
	}else {
		throw new Error("datefix: Tipo parámetro inválido "+typeof obj);
	}
};
util.seconddatefix = function (obj) {
	console.log("********************************************");
	console.log("seconddatefix no se usa, usar por Datasource entregado por arquitectura");
	console.log("Este mensaje se transformara en error posterior 21/05");
	console.log("********************************************");
	if (BCS_AMBIENTE=='DES' && moment(new Date()).format('YYYYMMDD')>20160531) {
		throw new Error("seconddatefix: funcion eliminada");
	}	
	if (obj instanceof Date) {
		return moment.utc(obj).local().toDate();   
	}else 
	{
		throw new Error("seconddatefix: Tipo parámetro inválido "+typeof obj);
	}
};
util.dateOffSet = function() {
	var jsOFFSET = moment(new Date()).format("Z"); //-+03:00	
	var jsOFFSETMins = (jsOFFSET.split(':')[0]*60+jsOFFSET.split(':')[1]*1);
	return jsOFFSETMins;
}
util.dateInicioDia = function(obj) {
	if(obj !== null){	
		if (obj instanceof Date) {
			return moment(moment(obj).format('YYYY-MM-DD') + 'T00:00:00').toDate();
		}else {
			return moment(obj.substring(0,10) + 'T00:00:00').toDate();
		}
	}
}
util.dateTerminoDia = function(obj) {
	if(obj !== null){	
		if (obj instanceof Date) {
			return moment(moment(obj).format('YYYY-MM-DD') + 'T00:00:00').add(1,'days').add(-1,'seconds').toDate();
		}else {
			return moment(obj.substring(0,10) + 'T00:00:00').add(1,'days').add(-1,'seconds').toDate();
		}
	}	
}

util.dateTerminoDiaLocal = function(obj) {
	if(obj !== null){	
		if (obj instanceof Date) {
			var asd = util.dateOffSet;
			return moment(moment(obj).format('YYYY-MM-DD') + 'T00:00:00').add(-1,'seconds').toDate();
		}else {
			return moment(obj.substring(0,10) + 'T00:00:00').add(-1,'seconds').toDate();
		}
	}	
}


util.dateOdata = function (obj) {
	if (obj instanceof Date) {
		//CORRECCION LLAMADA ODATA
		return new Date(obj.valueOf() + obj.getTimezoneOffset() * 60000);
	}else {
		throw new Error("dateOdata: Tipo parámetro inválido "+typeof obj);
	}
};
util.dateSPdate= function (obj) {
	if (obj instanceof Date) {
		if (obj.getTime()<util.datecorte())  return "0001-01-01";
		return moment(obj).local().format("YYYY-MM-DD");  	
	}else {
		throw new Error("dateSPdate: Tipo parámetro inválido "+typeof obj);
	}
};
util.dateSPseconddate= function (obj) {
	if (obj instanceof Date) {
		if (obj.getTime()<util.datecorte())  return "0001-01-01 00:00:00";
		return moment(obj).utc().format("YYYY-MM-DD HH:mm:ss");  	
	}else {
		throw new Error("dateSPseconddate: Tipo parámetro inválido "+typeof obj);
	}
};

util.dateSPtime= function (obj) {
	if (obj instanceof Date) {
		if (obj.getTime()<util.datecorte())  return "00:00:00";
		return moment(obj).utc().format("HH:mm:ss");  	
	}else {
		throw new Error("dateSPseconddate: Tipo parámetro inválido "+typeof obj);
	}
};
util.formatoDate = function (jsDate) {
	return moment(jsDate).format('L');
};
util.formatoTime = function (jsDate) {
	return moment(jsDate).format('HH:mm:ss');
};
util.formatoSecondate = function (jsDate) {
	return moment(jsDate).format('LL');
};
util.formatoTimeStamp = function (jsDate) {
	return moment(jsDate).format('LLL');
};
util.date= function(obj) {
	return moment(obj);
};
util.duration = function(obj) {
	return moment.duration(obj);
}
util.datediff = function (obj1,obj2) {
	if (!(obj1 instanceof Date)) throw new Error("datediff: Tipo parámetro1  inválido "+typeof obj1);
	if (!(obj2 instanceof Date)) throw new Error("datediff: Tipo parámetro2  inválido "+typeof obj2);		
	var ms = moment(obj1).diff(moment(obj2));
	var obj = moment.duration(ms);
	obj.format = function () {		
		return Math.floor(obj.asHours()) + moment.utc(ms).format(":mm:ss");
	}
	return obj;
}
util.dateisdefault = function (obj) {
	return (moment(obj).utc().format("YYYY-MM-DD") == "0001-01-01");
};
util.datedefault = function() {
	return moment.utc("0001-01-01 00:00:00 000",'YYYY-MM-DD HH:mm:ss ZZ').toDate();
};
util.datesysdefault = function() {
	return moment("0001-01-01 00:00:00 000",'YYYY-MM-DD HH:mm:ss ZZ').toDate();
};
util.datecorte = function() {
	return new Date(1900,0,0).getTime();
};
util.dateToFormat= function(date,format) {
	return moment(date).format(format);
};
util.dateSubMonths = function (obj,months) {
	return moment(obj).subtract(months,'months').format('YYYY-MM-DD');
}
util.dateAddMonths = function (obj,months) {
	return moment(obj).add(months,'months').format('YYYY-MM-DD');
}
util.dateSubDays = function (obj,days) {
	if(obj !== null){	
		if (obj instanceof Date) {
			return moment(moment(obj).format('YYYY-MM-DD')).add(days,'days').toDate();
		}else {
			return moment(obj.substring(0,10)).add(days,'days').toDate();
		}
	}
}
util.dateAddDays = function (obj,days) {
	if(obj !== null){	
		if (obj instanceof Date) {
			return moment(moment(obj).format('YYYY-MM-DD')).add(days,'days').toDate();
		}else {
			return moment(obj.substring(0,10)).add(days,'days').toDate();
		}
	}
}
util.fechaHoraSistema = function (obj, format){
	return moment.utc(obj).local().format(format);
}
/*********************
 *  TOKENS           *
 ********************/
util.ObtenerToken = function() {
	var surl=window.location.href;
	if (surl.indexOf("X-SESSION-TOKEN=")>0) {
		surl=surl.substring(surl.indexOf("X-SESSION-TOKEN=")+16)
		return surl.substring(0,92);
	}
	return (sessionStorage.Token||'').trim();
};
util.ObtenerModo = function() {
	var surl=window.location.href;
	if (surl.indexOf("sapb1=true")>0) {
		return "sapb1";
	}
	return "browser";
};

util.getPropertyCaseInsensitive = function(obj,propName) {
	var pName = propName.toLowerCase();
	
	for(var name in obj) {
	   if (obj.hasOwnProperty(name)) {
		   if (pName === name.toLowerCase()) {
			   return name;
		   }
	   }
	}
	return undefined;
	
}

util.parseServerResponse = function(response)
{
	var result = {
			erroresFatales 			:[],
			errores 				:[], 
			advertencias			:[],
			autorizaciones			:[],
			informaciones			:[],
			erroresFatalesContador	: 0,
			erroresContador			: 0,
			autorizacionesContador	: 0,
			advertenciasContador	: 0,
			informacionesContador	: 0
		};
	
	try {
		if (response.status!=undefined && response.status != 200) {
			result.erroresFatales.push({Codigo : "FATAL", Descripcion : "UI_INTERNAL_SERVER_ERROR" });
			result.erroresFatalesContador = 1;
		}
		
		var advName = util.getPropertyCaseInsensitive(response,"P_OUT_ADVERTENCIASDETALLE");
		if (advName) {
			$.each(response[advName],function(index,value){;
				if (value.RequiereAutorizacion === 'S') {
				    result.autorizaciones.push(value);
				    result.autorizacionesContador++;
				}
				else {   
				    result.advertencias.push(value);
				    result.advertenciasContador++;
				}
			});
		}
		
		var erroresName = util.getPropertyCaseInsensitive(response,"P_OUT_ERRORESDETALLE");
		if (erroresName) {
			$.each(response[erroresName],function(index,value){;
				result.errores.push(value);
				result.erroresContador++;
			});
		}
		
		var infoName = util.getPropertyCaseInsensitive(response,"P_OUT_INFORMACIONDETALLE");
		if (infoName) {
			$.each(response[infoName],function(index,value){;
				result.informaciones.push(value);
				result.informacionesContador++;
			});
		}
		
		var mensajesName = util.getPropertyCaseInsensitive(response,"P_OUT_MENSAJES");
		if (mensajesName) {
			$.each(response[mensajesName],function(index,value){;
				value.Descripcion = value.descripcion;
				if (value.tipo === "ERROR") {
					result.errores.push(value);
					result.erroresContador++;
				}
				if (value.tipo === "AUTORIZACION") {
					result.autorizaciones.push(value);
					result.autorizacionesContador++;
				}
				if (value.tipo === "ADVERTENCIA") {
					result.advertencias.push(value);
					result.advertenciasContador++;
				}
				if (value.tipo === "INFORMACION" || value.tipo === "OK") {
					result.informaciones.push(value);
					result.informacionesContador++;
				}
			});
		}
		
		if (response.P_OUT_ERRORESCONTADOR > 0) {
			result.erroresContador = Number(response.P_OUT_ERRORESCONTADOR);
		}

	}catch(e) {
		console.error(e);
		if (typeof response !== 'undefined' &&  typeof response.data !== 'undefined' && typeof response.data.CODIGO !== 'undefined') {
			result.erroresFatales.push({ Codigo : response.data.CODIGO  ,Descripcion: "Error interno en la aplicación #" +  response.data.CODIGO   } );
		}else {
			result.erroresFatales.push({ Codigo : "FATAL" ,Descripcion: "Error fatal en el sistema."  } );
		}
	}
	return result;
};

util.parseHandler = function(obj) {
	var handler = '';
	for (var i = 0; i < Object.keys(obj).length ; i++) {
		if (typeof obj[Object.keys(obj)[i]] === 'string') {
			obj[Object.keys(obj)[i]] = "'" + obj[Object.keys(obj)[i]] + "'";
		}

		handler += 'PLACEHOLDER."$$'+Object.keys(obj)[i].toLowerCase()+'$$"=>'+obj[Object.keys(obj)[i]];
		if (i != Object.keys(obj).length -1)
			handler += ',';
	}
	return handler = '('+handler+')';
}

util.formatRut = function (rut)
{
	//compatibilidad con metodo legacy
	return util.validarRut(rut).rutFormat;
}

util.validarRut = function(newval){
	var obtieneDigitoVerificador = function dv(T) {
		var M=0,S=1;for(;T;T=Math.floor(T/10))
		S=(S+T%10*(9-M++%6))%11;return S?S-1:'K';
	};
	var formatearRut = function(doc, dv){
		doc = (doc+".").replace(/(\S)(?=(\S{3})+\.)/g, '$1.');
		return doc.substring(0, doc.lastIndexOf("."))+"-"+dv;	
	};
	var validarFormato = function(doc){
		var rut = doc.substring(0, doc.lastIndexOf("-"));
		var dv = doc.substring(doc.length-1);
		if(rut.match(new RegExp("[a-z|A-Z]"))!==null || doc.indexOf("-") < 0 || doc.indexOf("-") != doc.lastIndexOf("-")){
			return "";
		}else{
			return doc;
		}
	}
	var limpiarRut = function(doc){
		doc = validarFormato(doc || "");
		return doc.replace(new RegExp("\\.", "g"), "").replace(new RegExp("-", "g"), "");//obtener solo rut sin puntos ni guiones
	};

	var d = newval;
	var doc = limpiarRut(d);
	var rut = doc.substring(0, doc.length-1);
	var dv = doc.substring(doc.length-1);
	var respuesta = {};
	if( doc.length>=2 && rut.length < 10 && dv == obtieneDigitoVerificador(rut)){
		respuesta = {valid:true, rut:d, rutFormat: formatearRut(rut, dv)};
	}else{
		respuesta = {valid:false,rut:d, rutFormat: formatearRut(rut, dv)};
	}
	return respuesta;
};


util.fnFormatHighlight = function (itemText, text) {
	if (itemText===undefined||itemText.length==0) return "";
	var textMatcher = new RegExp(text, "ig");
	return itemText.replace(textMatcher, function(match) {
		return "<strong>" + match + "</strong>";
	});
};
util.limpiaDataSource = function (dataSource){
	while (dataSource._total > 0){
		var dataItem = dataSource.at(dataSource._total-1);
		dataSource.remove(dataItem);
	}
	dataSource.sync();
};



// --------------------------------------------------------------------
// --- Formatos para precios instrumentos, montos, catidades, etc. ---
// --------------------------------------------------------------------
 
/*
 * Objetivo: Trunca un Numero a los decimales ingresado
 * */
util.truncarNumero = function(numeroStr, decimales){
	if (numeroStr === undefined || numeroStr === '' || decimales === undefined || decimales === '')  return '';	
	var vbig = new Big(numeroStr);
	var trun =vbig.round(decimales,0);
	return Number(trun.toString());
};

/**
 * Objetivo: Agregar separador de decimales a numeros que vengan sin el 
 */
util.incluirDecimales = function(numeroStr, decimales){
	if((numeroStr.length) > decimales){
		return parseFloat(numeroStr.substr(0, (numeroStr.length) - decimales) + "." + numeroStr.substr((numeroStr.length) - decimales)); 
	}else{
		throw new Error("Exception, el numero no es del largo suficiente para agregar el separador de decimales.");
	}	
};

util.formatoPA = function(numeroStr){
	if((numeroStr.length) != 0){
		return parseFloat(numeroStr.substring(0,3) + '.' + numeroStr.substring(3, 5)); 
	}else{
		throw new Error("Exception, el numero no cumple con los requisitos.");
	}	
};

/*
 * Objetivo: Retorna True si un valor corresponde a un número válido
 * */
util.isNumeric = function(valor)
{
	return !isNaN(parseFloat(valor)) && isFinite(valor);
}

/*
 * Objetivo: Fun. interna que valida si una rama existe en la matriz de formatos. 
 *           Retorna un objeto con el estado y mensaje de la validación. 
 *           
 * */
util.validarRamaMatrizFormatos = function(bcs_formato_validar,uso,idMoneda,idTipoInstrumento,idTipoOperacion)
{	
	var validacion = {};
	validacion.estado = 0;   //0:éxito    1:error
	validacion.mensaje = "Validación Ok";
 
	try
	{	
    	if (bcs_formato_validar[uso] === undefined || bcs_formato_validar[uso] === '')
    	    throw new Error("Uso="+iuso+" no existe en matriz de formatos");    	
    	if (bcs_formato_validar[uso][idMoneda] === undefined || bcs_formato_validar[uso][idMoneda] === '')
    	    throw new Error("La rama ["+uso+"]["+idMoneda+"] no existe en matriz de formatos");    	
    	if (bcs_formato_validar[uso][idMoneda][idTipoInstrumento] === undefined || bcs_formato_validar[uso][idMoneda][idTipoInstrumento] == '')    	    		
    		throw new Error("La rama ["+uso+"]["+ idMoneda +"]["+idTipoInstrumento+"] no existe en matriz de formatos");    	
    	if (idTipoOperacion !== '' && idTipoOperacion !== undefined)  //El parámetro idTipoOperacion podría ser vacío para algunas ramas 
    	{
        	if (bcs_formato_validar[uso][idMoneda][idTipoInstrumento][idTipoOperacion] === undefined || bcs_formato_validar[uso][idMoneda][idTipoInstrumento][idTipoOperacion] === '')
        		throw new Error("La rama ["+uso+"]["+ idMoneda +"]["+idTipoInstrumento+"]["+idTipoOperacion+"] no existe en matriz de formatos");
    	}
	}
	catch(err)
	{
		validacion.estado = 1;
		validacion.mensaje = err.message; //err.name //err.message;;
	}
	return validacion;
	
};    	    	

/*
 * Objetivo: Formatea un valor de acuerdo a un uso(cantidad, precio, etc.) y una matriz de formatos.
 * Llamada:  util.bcs_formato("95.569385695", "cantidad", "CLP","ACN","COMACN");
 * */
util.bcs_formato = function(valor, uso, tipoFormato, idMoneda, idTipoInstrumento, idTipoOperacion)
{	
	var cantidadDecimales = 0;	
	var trucarRedondear  = "";
	
	var formatoFinal = {};    //Objeto de retorno  
	formatoFinal.valor = 0;   //Valor formateado
	formatoFinal.valorNumerico = 0;   //Valor como numero
	formatoFinal.cantidadDecimales = 0;
	formatoFinal.trucaRedondea = '';
	formatoFinal.error = "";  //Mensaje en caso de error
	formatoFinal.excelFormat = "";  //Mensaje en caso de error
	
    try
    {
    	//Valida ingreso de datos
        //if (valor === undefined || valor === '') throw new Error("El parámetro Valor está vacío");  //Ej. 956.2369
    	if (valor === undefined || valor === '' || valor === null) valor = 0;  //Ej. 956.2369
        if (uso === undefined || uso === '') throw new Error("El parámetro Uso está vacío");   //Ej.: GEN / CUS....
        if (tipoFormato === undefined || tipoFormato === '') throw new Error("El parámetro TipoFormato está vacío");  //Ej.: Cantidad, Monto...
        if (idMoneda === undefined || idMoneda === '') throw new Error("El parámetro IdMoneda está vacío"); //Ej: CLP
        if (idTipoInstrumento === undefined || idTipoInstrumento === null) idTipoInstrumento = '';
        //if (uso.toUpperCase() !== 'CUS')  //para algunos usos el tipo operación no es necesario
        //{
        //	if (idTipoOperacion === undefined || idTipoOperacion === '') throw new Error("El parámetro IdTipoOperación está vacío");  //COMACN
        //}
        
        //Valida que el valor sea número legal
        if (!util.isNumeric(valor)) throw new Error("El parámetro Valor no es un número válido");
        
        
        //Trasforma a mayuscula para homologar a la matriz.
        uso = uso.toUpperCase();
        idMoneda = idMoneda.toUpperCase(); 
        idTipoInstrumento = idTipoInstrumento?idTipoInstrumento.toUpperCase():''; 
        idTipoOperacion = idTipoOperacion?idTipoOperacion.toUpperCase():'';
                                     
    	//Obtiene los formatos en la matriz de acuerdo al uso.
    	var formatos = undefined;
    	var nombreCantidadDecimales = undefined;
    	var nombreTruncarRedondear = undefined;
		switch(tipoFormato.toUpperCase()) {
    	    case "CANTIDAD":
    	    	formatos = bcs_formato_cantidad;
    	    	nombreCantidadDecimales = "CantidadDecimales";
    	    	nombreTruncarRedondear = "CantidadTrucaRedondea";
    	    	break;
    	            	        
    	    case "PRECIO":
    	    	formatos = bcs_formato_precio;
    	    	nombreCantidadDecimales = "PrecioDecimales";
    	    	nombreTruncarRedondear = "PrecioTrucaRedondea";
    	    	break;
    	        
    	    case "TASA":
    	    	formatos = bcs_formato_tasa;
    	    	nombreCantidadDecimales = "TasaDecimales";
    	    	nombreTruncarRedondear = "TasaTrucaRedondea";
    	    	break;
    	         
    	    case "MONTO":
    	   		formatos = bcs_formato_monto;
    	   		nombreCantidadDecimales = "MontoDecimales";
    	    	nombreTruncarRedondear = "MontoTrucaRedondea";
    	    	break;   
    	        
    	    case "TIPOCAMBIO":
    	    	formatos = bcs_formato_tipoCambio;
    	    	nombreCantidadDecimales = "TipoCambioDecimales";
    	    	nombreTruncarRedondear = "TipoCambioTrucaRedondea";
    	    	break;        
    	        
    	    case "PARIDAD":
    	    	formatos = bcs_formato_paridad;
    	    	nombreCantidadDecimales = "ParidadDecimales";
    	    	nombreTruncarRedondear = "ParidadTrucaRedondea";
    	    	break;
    	        
    	    case "PUNTOS":
    	    	formatos = bcs_formato_puntos;
    	    	nombreCantidadDecimales = "PuntosDecimales";
    	    	nombreTruncarRedondear = "PuntosTrucaRedondea";
    	    	break;    
    	        
    	    default:    	    	
    	    	throw new Error("El parámetro Uso no es válido");
    	}

    	//intentamos obtener el formato
    	var validacion = util.validarRamaMatrizFormatos(formatos,uso,idMoneda,idTipoInstrumento,idTipoOperacion);  
    	if (validacion.estado == 1) {
    		//buscamos configuracion base
    		var validacionBase = util.validarRamaMatrizFormatos(formatos,uso,idMoneda,"",""); 
    		idTipoInstrumento = "";
    		idTipoOperacion = ""; 
			if (validacionBase.estado == 1) {
				throw new Error(validacion.mensaje);
			}
    	}
    	
    	cantidadDecimales = formatos[uso][idMoneda][idTipoInstrumento][idTipoOperacion][nombreCantidadDecimales];
    	trucarRedondear  = formatos[uso][idMoneda][idTipoInstrumento][idTipoOperacion][nombreTruncarRedondear];   
    	
    	       	
		//Valida que exista formato
    	if (cantidadDecimales == undefined || cantidadDecimales === '') {
    		throw new Error("Matriz sin formato para Cantidad Decimales");
    	}
    	if (trucarRedondear == undefined || trucarRedondear === '') {
    		throw new Error("Matriz sin formato en TruncaRedondea");
    	}
    	
    	//Valida que exista formato
    	if (cantidadDecimales == undefined || cantidadDecimales === '') throw new Error("Matriz sin formato para Cantidad Decimales");
    	if (trucarRedondear == undefined || trucarRedondear === '') throw new Error("Matriz sin formato en TruncaRedondea");
     
    	//Aplica formato
    	switch(trucarRedondear.toUpperCase()) 
    	{
		    case "T":  //Truncar
		    	formatoFinal.valorNumerico = Number(util.truncarNumero(valor, cantidadDecimales));	    	
		    	formatoFinal.valor = kendo.toString(formatoFinal.valorNumerico, "n"+cantidadDecimales);
		    	formatoFinal.cantidadDecimales = cantidadDecimales;
		    	formatoFinal.trucaRedondea = trucarRedondear;
		    	formatoFinal.excelFormat = "#,##0".concat(cantidadDecimales > 0? ".".concat("0".repeat(cantidadDecimales)):"");
		        break;
		        
		    case "R":  //Redondear
		    	formatoFinal.valorNumerico = Number(new Big(valor).toFixed(cantidadDecimales));
		    	formatoFinal.valor = kendo.toString(formatoFinal.valorNumerico, "n"+cantidadDecimales);
		    	formatoFinal.cantidadDecimales = cantidadDecimales;
		    	formatoFinal.trucaRedondea = trucarRedondear;
		    	formatoFinal.excelFormat = "#,##0".concat(cantidadDecimales > 0? ".".concat("0".repeat(cantidadDecimales)):"");
		        break;
		        
		    default:    	    	
		    	throw new Error("Formato de TruncaRedondea de la matriz de formatos no es válido");
    	}
    	
    }
    catch(err)
    {	   	    	
    	//No se guarda log, ya que es sólo una consulta no transaccional.
		formatoFinal.valor = err.name;
		formatoFinal.error = err.message;		
		formatoFinal.cantidadDecimales = 0;
    	formatoFinal.trucaRedondea = '';
    	formatoFinal.excelFormat = '';
    }	
		
	return formatoFinal;
};

/*
 * Objetivo: Dada una fecha determina si el dia es feriado retorna true / false
 * Llamada:  util.esFechaFeriado(IdPais,Date);
 * */
util.esFechaFeriado = function  (idPais,fecha) {
	if (bcs_feriados[idPais] !== undefined && bcs_feriados[idPais][moment(fecha).format("YYYY-MM-DD")] !== undefined){	
		return true;	
	}else{
		return false;	
	}
};

/*
 * Objetivo: Dado un string de fecha y un formato determina si es una fecha valida
 * Llamada:  util.esFechaHabil(IdPais,Date);
 * */
util.esFechaValida = function  (fechaStr,formato) {
	if (moment(fechaStr,formato,true).isValid())
		return true;
	return false;	
};

/*
 * Objetivo: Dada una fecha determina si el dia es hábil retorna true / false
 * Llamada:  util.esFechaHabil(IdPais,Date);
 * */
util.esFechaHabil = function  (idPais,fecha) {
	if (moment(fecha).format("e") == "5"  || moment(fecha).format("e") == "6"){	
		return false;
	}else if (util.esFechaFeriado(idPais,fecha)){
		return false;
	} else{
		return true;		
	}
};

/*
 * Objetivo: Abrir una ventana
 * Llamada:  util.VentanaAbrir(winkey,param,unique);
 * */
util.VentanaAbrir = function (winkey,params,unique) {
	var obj=angular.element(document.body).injector().get('bcsComunes');
	obj.ventanaAbrir(winkey,params);
};

/*
 * Objetivo: Obtener nombre acceso
 * */
util.getNombreAccesoPadre= function(idPadre){
	var nombreAcceso = '';
	$.each(BCS_ACCESOS,function(key,value){
		if(value.IdAcceso!=undefined && parseInt(value.IdAcceso)===parseInt(idPadre)){
			nombreAcceso = value.NombreAcceso;
			return false;
		}
	});
	return nombreAcceso;
};
/*
 * Objetivo: Formato de String   Xxxxxxx Yyyyyyyy Zzzzzzzz
 * Llamada:  util.FormatoTitulo('xxxxx yyyyyyy Hhhhhh');
 * */

util.FormatoTitulo = function (str)
{
    return str.replace(/[\p{L}\p{M}]+/gu, function(txt){
    		//Si el las 2 primeras letras son mayusculas respetamos la palabra
    		if (txt.length>=2)
    		  if (txt.charAt(0)==txt.charAt(0).toUpperCase())
    			  if (txt.charAt(1)==txt.charAt(1).toUpperCase()) 
    				  return txt;
    		//Si la palabra es es corta probablmente sea una proposicion
    		if (txt.length<=3) 
    			return txt.toLowerCase();    		
    		//Capital
    		return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    	}
    );
}
/*
 * Objetivo: Formato de String   Xxxxxxx yyyyyyyy zzzzzzzz
 * Llamada:  util.FormatoCapital('xxxxx yyyyyyy Hhhhhh');
 * */

util.FormatoCapital = function (str)
{
   return str.charAt(0).toUpperCase() + str.substr(1).toLowerCase();
}

/*
 * Objetivo: Validar que el rango de una fecha desde y una hasta no excedan el rango definido de dias
 * Observacion:  Deben ser del tipo date, la directiva kendo datepicker ya tiene las fechas en el formato correcto.
 * Llamada:  util.validarRangoFecha(new Date('2016-08-01'), new Date('2016-09-01'));
 * Opcional: util.validarRangoFecha(new Date('2016-08-01'), new Date('2016-09-01'), 60); 
 * */
util.validarRangoFecha = function(desde, hasta, rango){
	var resp = {};
	try{
		if(desde instanceof Date && hasta instanceof Date){
			rango = (rango===undefined)?sesion.getPreferencias("FiltroRangoDias") : rango;
			if(desde > hasta){
				resp.estado = "NOK";
				resp.mensaje = "FECHA_RANGO_CONSULTA_INVALIDA";
				return resp;
			}else{
				var days = Math.ceil((hasta-desde)/ (1000*60*60*24));
				if(days>rango){
					resp.estado = "NOK";
					resp.mensaje = "Rango de fecha busqueda no puede ser superior a "+rango+" dia"+((rango!=1)?"s":"")+".";
					return resp;
				}else{
					return true;		
				}
			}	
		}else{
			resp.estado = "NOK";
			resp.mensaje = "Alguna de las fechas ingresadas no tiene el formato correcto.";
			return resp;
		}
	}catch(e){
		resp.estado = "NOK";
		resp.mensaje = "Ocurrio un error al comparar las fechas.";
		return resp;
	}
 };
 
 /**
  * Objetivo: Actualizar el tamaño de una grilla acorde al height configurado.
  * Observacion: Es util cuando la grilla toma un tamaño que sobrepasa el de la pantalla.
  * Llamada: util.refreshGridSize("IdHtmlGrid");
  * Opcional: util.refreshGridSize("IdHtmlGrid", 100); 
  */
 util.refreshGridSize = function(grid, time){
	 time = (time)?time : 50;
	 var idTimeout = window.setTimeout(function(){
		if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
			$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh();
			window.clearTimeout(idTimeout);
	 },time) 
 };
 
 /*
  * Objetivo: PadLeft
  * Llamada:  util.PadLeft(dato,caracter,largo);
  * */
 util.PadLeft = function(dato,caracter,largo) {
	if(typeof(dato)!='string'||typeof(caracter)!='string'||typeof(largo)!='number')return dato;
	if(caracter.length>1||largo-dato.length<1)return dato;
	var pad = Array(largo + 1).join(caracter);
 	return pad.substring(0,pad.length-dato.length)+dato;
 };
 
 /*
  * Objetivo: PadRight
  * Llamada:  util.PadRight(dato,caracter,largo);
  * */
 util.PadRight = function(dato,caracter,largo) {
	if(typeof(dato)!='string'||typeof(caracter)!='string'||typeof(largo)!='number')return dato;
	if(caracter.length>1||largo-dato.length<1)return dato;
	var pad = Array(largo + 1).join(caracter);
 	return dato+pad.substring(0,pad.length-dato.length);
 };
 
 /*
  * Objetivo: Validaciones para IUP
  * Llamada:  util.validarIUP(dcmnto);
  * */
 util.validarIUP = function(dcmnto){
	dcmnto=''+dcmnto;
	if(dcmnto.length!==14)return{escorrecta:false,mensaje:'UI_NO_CUMPLE_LARGO_PERMITIDO'};
	var tsep={aTipDoc:dcmnto.substring(0,2),bNroDoc:dcmnto.substring(2,12),cNroExt:dcmnto.substring(12,14)};
	var tipDocPosibles=['02','20','03','22','30'];
	var tipDocMatch=false;
	for(i in tipDocPosibles)if(tsep.aTipDoc==tipDocPosibles[i])tipDocMatch=true;
	if(!tipDocMatch)return{escorrecta:false,mensaje:'UI_NO_CUMPLE_TIPO_DOCUMENTO_PERMITIDO'};
	if(isNaN(parseInt(tsep.cNroExt)))return{escorrecta:false,mensaje:'UI_NO_CUMPLE_DIGITOS_SECUENCIA_PERMITIDO'};
	return {escorrecta:true,mensaje:'UI_VALIDACION_OK',detalle:tsep};
};

/*
 * Objetivo: Calcular Interes Acumulado a partir de un interes diario y la fecha del prestamo - tasa fija
 * Llamada:  util.calcularInteresAcumulado(interesDiario,new Date('2016-08-01'),new Date('2016-08-01'));
 * */
util.calcularInteresAcumulado = function (interesDiario, fechaDesde, fechaHasta) {
	if(typeof(interesDiario)!='number'||!fechaDesde instanceof Date||!fechaHasta instanceof Date)return interesDiario;
	try{
		if(fechaDesde > fechaHasta){
			return -1;
		}
		else{
			var days = Math.ceil((fechaHasta-fechaDesde)/ (1000*60*60*24));
			return Math.ceil(days * interesDiario);
		}
	}catch(e){
		return -1;
	}
};


util.b64toBlob = function(b64Data, contentType, sliceSize)
{
  contentType = contentType || '';
  sliceSize = sliceSize || 512;

  var byteCharacters = atob(b64Data);
  var byteArrays = [];

  for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
       var slice = byteCharacters.slice(offset, offset + sliceSize);

       var byteNumbers = new Array(slice.length);
       for (var i = 0; i < slice.length; i++) {
             byteNumbers[i] = slice.charCodeAt(i);
        }

       var byteArray = new Uint8Array(byteNumbers);

       byteArrays.push(byteArray);
   }
	
	var blob = new Blob(byteArrays, {type: contentType});
	return blob;
}

String.prototype.replaceAll = function(busqueda, reemplazo){
	var objetivo = this;
	if(objetivo!==undefined){
		return objetivo.replace(new RegExp(busqueda, 'g'), reemplazo);	
	}else{
		return this;	
	}
}

/**
 * Evalua si la fecha argumento de anterior al corte definido
 *
 * @param date instancia de Date a evaluar
 * @returns {boolean}
 */
util.esFechaAnteriorCorte = function esFechaAnteriorCorte(date) {
	return date instanceof Date && (isNaN(date.getTime()) || date.getTime() <= util.datecorte());
}

util.translate = function translate($translate, codigo, params = {}) {
	const separator = '::';
	const placeholders = Object.keys(params) || [];
	let translation = $translate.instant(codigo);
	
	for (const placeholder of placeholders) {
		const k = separator + placeholder + separator;
		const v = params[placeholder]; 
		translation = translation.replaceAll(k, v);
	}
	
	return translation;
}

/**
 *  Transport para los servicios call que tienen paginado por servidor (necesario para que kendo pueda exportar la pagina actual de la grilla)
 *
 * @param options configuracion de la grilla
 * @param callTransaction id del servicio
 * @param callResultSet nombre de la variable donde esta almacenados los datos
 * @param lastCallParameters parametros de la ultima ejecucion
 */
util.callTransport = function callTransport(options, callTransaction, callResultSet, lastCallParameters) {
	var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction+"&r="+callResultSet;
	
	if (options.data.skip !== "undefined" && typeof options.data.take !== "undefined") {
		url += "&skip=" + options.data.skip + "&take=" + options.data.take;
	};
	
	if (options.data.sort && options.data.sort[0] && options.data.sort[0].field && options.data.sort[0].dir) {
		var sort = `${options.data.sort[0].field}::${options.data.sort[0].dir}`;
		url += "&sort=" + sort;
	};
	
	$.ajax({
		url: url,
		dataType: "json",
		type: "POST",
		contentType:"application/json; charset=utf-8",
		data: JSON.stringify(lastCallParameters),
		success: function(result) {
			var resultSet = [];
			var outResultSet = result[callResultSet] ? result[callResultSet] : result;
			var totalResults = result["total"] ? result["total"] : undefined;
			outResultSet = result["data"] ? result["data"] : outResultSet
			for ( var key in outResultSet) {
				resultSet.push(outResultSet[key]);
			}
			result.P_OUT_TBL = resultSet;
			options.success({data: result.P_OUT_TBL, total: totalResults});

		},
		error: function(result) {
			options.error(result);
		}
	});
}