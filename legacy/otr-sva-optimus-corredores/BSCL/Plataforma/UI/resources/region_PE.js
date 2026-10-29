//TEST UTF-8=€Hí
//////////////////////////////////////// KENDO /////////////////////////////////////////////////
kendo.cultures["BSCL"] = {
	    name: "BSCL",
	    numberFormat: {
	        pattern: ["-n"],
	        decimals: 4,
	        ",": ",",  //ERROR KENDO
	        ".": ".",   //ERROR KENDO
	        groupSize: [3],
	        percent: {
	            pattern: ["-n %", "n %"],
	            decimals: 5,
	            ",": ",",  //ERROR KENDO
	            ".": ".",   //ERROR KENDO
	            groupSize: [3],
	            symbol: "%"
	        },
	        currency: {
	            pattern: ["$ -n", "$ n"],  
	            decimals: 2,
	            ",": ",",  //ERROR KENDO
	            ".": ".",   //ERROR KENDO
	            groupSize: [3],
	            symbol: "CLP"
	        }
	    },
	    calendars: {
	        standard: {
	            days: {
	                names: ["domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado"],
	                namesAbbr: ["dom", "lun", "mar", "mié", "jue", "vie", "sáb"],
	                namesShort: ["do", "lu", "ma", "mi", "ju", "vi", "sá"]
	            },
	            months: {
	                names: ["enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"],
	                namesAbbr: ["ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic"]
	            },
	            AM: ["am", "am", "AM"],
	            PM: ["pm", "pm", "PM"],
	            patterns: {
	                d: "dd-MM-yyyy",
	                D: "dddd, dd' de 'MMMM' de 'yyyy",
	                F: "dddd, dd' de 'MMMM' de 'yyyy H:mm:ss",
	                g: "dd-MM-yyyy H:mm",
	                G: "dd-MM-yyyy H:mm:ss",
	                m: "d' de 'MMM",
	                M: "d' de 'MMM",
	                s: "yyyy'-'MM'-'dd'T'HH':'mm':'ss",
	                t: "H:mm",
	                T: "H:mm:ss",
	                u: "yyyy'-'MM'-'dd HH':'mm':'ss'Z'",
	                y: "MMMM' de 'yyyy",
	                Y: "MMMM' de 'yyyy"
	            },
	            "/": "-",
	            ":": ":",
	            firstDay: 1  //Lunes
	        }
	    }
	};
kendo.culture("BSCL");    

!function(e,define){define([],e)}(function(){return function(e){kendo.ui.FilterCell&&(kendo.ui.FilterCell.prototype.options.operators=e.extend(!0,kendo.ui.FilterCell.prototype.options.operators,{date:{eq:"Es igual a",gte:"Es posterior o igual a",gt:"Es posterior",lte:"Es anterior o igual a",lt:"Es anterior",neq:"No es igual a"},number:{eq:"Es igual a",gte:"Es mayor o igual que",gt:"Es mayor que",lte:"Es menor o igual que",lt:"Es menor que",neq:"No es igual a"},string:{endswith:"Termina en",eq:"Es igual a",neq:"No es igual a",startswith:"Comienza con",contains:"Contiene",doesnotcontain:"No contiene"},enums:{eq:"Es igual a",neq:"No es igual a"}})),kendo.ui.FilterMenu&&(kendo.ui.FilterMenu.prototype.options.operators=e.extend(!0,kendo.ui.FilterMenu.prototype.options.operators,{date:{eq:"Es igual a",gte:"Es posterior o igual a",gt:"Es posterior",lte:"Es anterior o igual a",lt:"Es anterior",neq:"No es igual a"},number:{eq:"Es igual a",gte:"Es mayor o igual que",gt:"Es mayor que",lte:"Es menor o igual que",lt:"Es menor que",neq:"No es igual a"},string:{endswith:"Termina en",eq:"Es igual a",neq:"No es igual a",startswith:"Comienza con",contains:"Contiene",doesnotcontain:"No contiene"},enums:{eq:"Es igual a",neq:"No es igual a"}})),kendo.ui.ColumnMenu&&(kendo.ui.ColumnMenu.prototype.options.messages=e.extend(!0,kendo.ui.ColumnMenu.prototype.options.messages,{columns:"Columnas",sortAscending:"Orden ascendente",sortDescending:"Orden descendente",settings:"Configuración de columnas",done:"Hecho",lock:"Bloquear",unlock:"Desbloquear"})),kendo.ui.RecurrenceEditor&&(kendo.ui.RecurrenceEditor.prototype.options.messages=e.extend(!0,kendo.ui.RecurrenceEditor.prototype.options.messages,{daily:{interval:"día(s)",repeatEvery:"Repetir cada:"},end:{after:"Después",occurrence:"ocurrencia(s)",label:"Fin:",never:"Nunca",on:"En",mobileLabel:"Ends"},frequencies:{daily:"Diariamente",monthly:"Mensualmente",never:"Nunca",weekly:"Semanalmente",yearly:"Anualmente"},monthly:{day:"Día",interval:"mes(es)",repeatEvery:"Repetir cada:",repeatOn:"Repetir en:"},offsetPositions:{first:"Primero",fourth:"Cuarto",last:"Último",second:"Segundo",third:"Tercero"},weekly:{repeatEvery:"Repetir cada:",repeatOn:"Repetir en:",interval:"semana(s)"},yearly:{of:"de",repeatEvery:"Repetir cada:",repeatOn:"Repetir en:",interval:"año(s)"},weekdays:{day:"día",weekday:"día de semana",weekend:"día de fin de semana"}})),kendo.ui.Grid&&(kendo.ui.Grid.prototype.options.messages=e.extend(!0,kendo.ui.Grid.prototype.options.messages,{commands:{create:"Agregar",excel: "Exportar a Excel",pdf:"Exportar a PDF",destroy:"Eliminar",canceledit:"Cancelar",update:"Actualizar",edit:"Editar",select:"Seleccionar",cancel:"Cancelar Cambios",save:"Guardar Cambios"},editable:{confirmation:"¿Confirma la eliminación de este registro?",cancelDelete:"Cancelar",confirmDelete:"Eliminar"}})),kendo.ui.Pager&&(kendo.ui.Pager.prototype.options.messages=e.extend(!0,kendo.ui.Pager.prototype.options.messages,{page:"Página",display:"Elementos mostrados  {0} - {1} de {2}",of:"de {0}",empty:"No hay registros.",refresh:"Actualizar",first:"Ir a la primera página",itemsPerPage:"ítems por página",last:"Ir a la última página",next:"Ir a la página siguiente",previous:"Ir a la página anterior",morePages:"Más paginas"})),kendo.ui.FilterCell&&(kendo.ui.FilterCell.prototype.options.messages=e.extend(!0,kendo.ui.FilterCell.prototype.options.messages,{filter:"Filtrar",clear:"Limpiar filtro",isFalse:"No",isTrue:"Sí",operator:"Operador"})),kendo.ui.FilterMenu&&(kendo.ui.FilterMenu.prototype.options.messages=e.extend(!0,kendo.ui.FilterMenu.prototype.options.messages,{filter:"Filtrar",and:"Y",clear:"Limpiar filtro",info:"Mostrar filas con valor que",isFalse:"No",isTrue:"Si",or:"Or",cancel:"Cancelar",operator:"Operador",value:"Valor",selectValue:"-Seleccionar valor-"})),kendo.ui.Groupable&&(kendo.ui.Groupable.prototype.options.messages=e.extend(!0,kendo.ui.Groupable.prototype.options.messages,{empty:"Arrastre un encabezado de columna y suéltelo aquí para agrupar por dicho criterio"})),kendo.ui.Editor&&(kendo.ui.Editor.prototype.options.messages=e.extend(!0,kendo.ui.Editor.prototype.options.messages,{dialogButtonSeparator:"or",dialogCancel:"Cancel",dialogInsert:"Insert",imageAltText:"Alternate text",imageWebAddress:"Web address",linkOpenInNewWindow:"Open link in new window",linkText:"Text",linkToolTip:"ToolTip",linkWebAddress:"Web address",search:"Search",createTable:"Crear una tabla",addColumnLeft:"Add column on the left",addColumnRight:"Add column on the right",addRowAbove:"Add row above",addRowBelow:"Add row below",deleteColumn:"Delete column",deleteRow:"Delete row",backColor:"Background color",bold:"Bold",createLink:"Insert hyperlink",deleteFile:'Are you sure you want to delete "{0}"?',directoryNotFound:"A directory with this name was not found.",dropFilesHere:"drop files here to upload",emptyFolder:"Empty Folder",fontName:"Select font family",fontNameInherit:"(inherited font)",fontSize:"Select font size",fontSizeInherit:"(inherited size)",foreColor:"Color",formatBlock:"Format",indent:"Indent",insertHtml:"Insert HTML",insertImage:"Insert image",insertOrderedList:"Insert ordered list",insertUnorderedList:"Insert unordered list",invalidFileType:'The selected file "{0}" is not valid. Supported file types are {1}.',italic:"Italic",justifyCenter:"Center text",justifyFull:"Justify",justifyLeft:"Align text left",justifyRight:"Align text right",linkOpenInNewWindow1:"Open link in new window",linkText1:"Text",linkToolTip1:"ToolTip",linkWebAddress1:"Web address",orderBy:"Arrange by:",orderByName:"Name",orderBySize:"Size",outdent:"Outdent",overwriteFile:'\'A file with name "{0}" already exists in the current directory. Do you want to overwrite it?',search1:"Search",strikethrough:"Strikethrough",styles:"Styles",subscript:"Subscript",superscript:"Superscript",underline:"Underline",unlink:"Remove hyperlink",uploadFile:"Upload",formatting:"Format",viewHtml:"View HTML",dialogUpdate:"Update",insertFile:"Insert file"})),kendo.ui.Scheduler&&(kendo.ui.Scheduler.prototype.options.messages=e.extend(!0,kendo.ui.Scheduler.prototype.options.messages,{allDay:"todo el día",cancel:"Cancelar",editable:{confirmation:"¿Está seguro que quiere eliminar este evento?"},date:"Fecha",destroy:"Eliminar",editor:{allDayEvent:"Todo el día",description:"Descripción",editorTitle:"Evento",end:"Fin",endTimezone:"Zona horaria de fin",repeat:"Repetir",separateTimezones:"Usar zonas horarias separadas para el inicio y el fin",start:"Inicio",startTimezone:"Zona horaria de inicio",timezone:" ",timezoneEditorButton:"Zona horaria",timezoneEditorTitle:"Zonas horarias",title:"Título",noTimezone:"Sin zona horaria"},event:"Evento",recurrenceMessages:{deleteRecurring:"¿Quiere eliminar esta ocurrencia del evento o la serie completa?",deleteWindowOccurrence:"Eliminar ocurrencia actual",deleteWindowSeries:"Eliminar la serie",deleteWindowTitle:"Eliminar elemento recurrente",editRecurring:"¿Quiere editar esta ocurrencia del evento o la serie completa?",editWindowOccurrence:"Editar ocurrencia actual",editWindowSeries:"Editar la serie",editWindowTitle:"Editar elemento recurrente"},save:"Guardar",time:"Hora",today:"Hoy",views:{agenda:"Agenda",day:"Día",month:"Mes",week:"Semana",workWeek:"Semana laboral"},deleteWindowTitle:"Eliminar evento",showFullDay:"Mostrar día completo",showWorkDay:"Mostrar horas laborables"})),kendo.ui.Upload&&(kendo.ui.Upload.prototype.options.localization=e.extend(!0,kendo.ui.Upload.prototype.options.localization,{cancel:"Cancelar",dropFilesHere:"Arrastre los archivos aquí para subirlos",headerStatusUploaded:"Completado",headerStatusUploading:"Subiendo...",remove:"Quitar",retry:"Reintentar",select:"Seleccione...",statusFailed:"Error",statusUploaded:"Completado",statusUploading:"subiendo",uploadSelectedFiles:"Subir archivos"}))}(window.kendo.jQuery),window.kendo},"function"==typeof define&&define.amd?define:function(e,t){t()});

//////////////////////////////////////// ANGULAR /////////////////////////////////////////////////		
angular.module("ngLocale", [], ["$provide", function($provide) {
var PLURAL_CATEGORY = {ZERO: "zero", ONE: "one", TWO: "two", FEW: "few", MANY: "many", OTHER: "other"};
$provide.value("$locale", {
  "DATETIME_FORMATS": {
    "AMPMS": [
      "a.\u00a0m.",
      "p.\u00a0m."
    ],
    "DAY": [
      "domingo",
      "lunes",
      "martes",
      "mi\u00e9rcoles",
      "jueves",
      "viernes",
      "s\u00e1bado"
    ],
    "ERANAMES": [
      "antes de Cristo",
      "despu\u00e9s de Cristo"
    ],
    "ERAS": [
      "a. C.",
      "d. C."
    ],
    "MONTH": [
      "enero",
      "febrero",
      "marzo",
      "abril",
      "mayo",
      "junio",
      "julio",
      "agosto",
      "setiembre",
      "octubre",
      "noviembre",
      "diciembre"
    ],
    "SHORTDAY": [
      "dom.",
      "lun.",
      "mar.",
      "mi\u00e9.",
      "jue.",
      "vie.",
      "s\u00e1b."
    ],
    "SHORTMONTH": [
      "ene.",
      "feb.",
      "mar.",
      "abr.",
      "may.",
      "jun.",
      "jul.",
      "ago.",
      "set.",
      "oct.",
      "nov.",
      "dic."
    ],
    "fullDate": "EEEE, d 'de' MMMM 'de' y",
    "longDate": "d 'de' MMMM 'de' y",
    "medium": "dd-MM-y h:mm:ss a",
    "mediumDate": "dd-MM-y",
    "mediumTime": "h:mm:ss a",
    "short": "dd-MM-yy h:mm a",
    "shortDate": "dd-MM-yy",
    "shortTime": "h:mm a"
  },
  "NUMBER_FORMATS": {
    "CURRENCY_SYM": "$",
    "DECIMAL_SEP": ",",
    "GROUP_SEP": ".",
    "PATTERNS": [
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 3,
        "minFrac": 0,
        "minInt": 1,
        "negPre": "-",
        "negSuf": "",
        "posPre": "",
        "posSuf": ""
      },
      {
        "gSize": 3,
        "lgSize": 3,
        "maxFrac": 2,
        "minFrac": 2,
        "minInt": 1,
        "negPre": "\u00a4-",
        "negSuf": "",
        "posPre": "\u00a4",
        "posSuf": ""
      }
    ]
  },
  "id": "es-cl",
  "pluralCat": function(n, opt_precision) {  if (n == 1) {    return PLURAL_CATEGORY.ONE;  }  return PLURAL_CATEGORY.OTHER;}
});
}]);

// moment.js language configuration
// language : spanish (es)
// author : Julio Napurí : https://github.com/julionc

(function (factory) {
    if (typeof define === 'function' && define.amd) {
        define(['moment'], factory); // AMD
    } else if (typeof exports === 'object') {
        module.exports = factory(require('../moment')); // Node
    } else {
        factory(window.moment); // Browser global
    }
}(function (moment) {
    return moment.locale('es', {
        months : "enero_febrero_marzo_abril_mayo_junio_julio_agosto_septiembre_octubre_noviembre_diciembre".split("_"),
        monthsShort : "ene._feb._mar._abr._may._jun._jul._ago._sep._oct._nov._dic.".split("_"),
        weekdays : "domingo_lunes_martes_miércoles_jueves_viernes_sábado".split("_"),
        weekdaysShort : "dom._lun._mar._mié._jue._vie._sáb.".split("_"),
        weekdaysMin : "Do_Lu_Ma_Mi_Ju_Vi_Sá".split("_"),
        longDateFormat : {
            LT : "H:mm",
            L : "DD-MM-YYYY",
            LL : "DD-MM-YYYY HH:mm:ss",
            LLL : "DD-MM-YYYY HH:mm:ss.SSS",
            LLLL : "dddd, D [de] MMMM [de] YYYY LT"          
        },
        calendar : {
            sameDay : function () {
                return '[hoy a la' + ((this.hours() !== 1) ? 's' : '') + '] LT';
            },
            nextDay : function () {
                return '[mañana a la' + ((this.hours() !== 1) ? 's' : '') + '] LT';
            },
            nextWeek : function () {
                return 'dddd [a la' + ((this.hours() !== 1) ? 's' : '') + '] LT';
            },
            lastDay : function () {
                return '[ayer a la' + ((this.hours() !== 1) ? 's' : '') + '] LT';
            },
            lastWeek : function () {
                return '[el] dddd [pasado a la' + ((this.hours() !== 1) ? 's' : '') + '] LT';
            },
            sameElse : 'L'
        },
        relativeTime : {
            future : "en %s",
            past : "hace %s",
            s : "unos segundos",
            m : "un minuto",
            mm : "%d minutos",
            h : "una hora",
            hh : "%d horas",
            d : "un día",
            dd : "%d días",
            M : "un mes",
            MM : "%d meses",
            y : "un año",
            yy : "%d años"
        },
        ordinal : '%dº',
        week : {
            dow : 1, // Monday is the first day of the week.
            doy : 4  // The week that contains Jan 4th is the first week of the year.
        }
    });
}));


/*
 * Validar formato numero telefono fijo o movil
 * Formato: +CodPais CodArea Nro
 * Ejemplo: +56 9 1111 2222 / +56 2 2111 2222 / +56 63 111 2222 / +1 222 333 4444
 * El caracter separador puede ser '-' (guion) o ' ' (espacio)
 * La Expresión Regular utilizada es:
 * 		/^\+?\d{1,3}?[- ]?((\d[- ]?\d{4}[- ]?)|(\d{2}[- ]?\d{3}[- ]?)|(\d{3}[- ]?\d{3}[- ]?))\d{4}$/g
 */
var region = {};
region.ValidaFormatoNumeroTelefono=function(valor) {
	if(valor.length = 15){
		var expReg = /^\+?\d{1,3}?[- ]?((\d[- ]?\d{4}[- ]?)|(\d{2}[- ]?\d{3}[- ]?)|(\d{3}[- ]?\d{3}[- ]?))\d{4}$/g;
		return expReg.test(valor);
	}
	return false;
};


/*
 * Validar formato numero tarjeta de credito
 * Formato: xxxxxxxxxxxxxx, xxxxxxxxxxxxxxxx
 * La Expresión Regular utilizada es:
 * 		/^\d{14,16}$/g
 */
region.ValidaFormatoNumeroTarjetaCredito=function(tipoTarjeta, largo, valor) {
	//valor = valor.replace(/\s/g,'');
	if(valor.length <= largo){
		var expReg = /^\d{14,16}$/g;
		return expReg.test(valor);
	}
	return false;
};

/*
 * Objetivo: Validar que fecha de nacimiento no exceda la cantidad de años del parametro con la fecha sistema
 * 
 * 
 * */
region.validarFechaNacimiento = function(fechaNac,fechaSistema, cantidadAnios){
	var resp = {};
	try{
		if(fechaNac instanceof Date && cantidadAnios != undefined){
			
			var timeDiff = Math.abs(fechaSistema.getTime() - fechaNac.getTime());
			var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24 * 365)); 
			if(diffDays > cantidadAnios){
				return false
			}else{
				return true;		 			
			}	
		}else{
			return false;
		}
	}catch(e){
		return false;
	}
 };


/*
 * Validar formato codigo verificacion
 * Formato: xxx, xxxx (valor numerico)
 * La Expresión Regular utilizada es:
 * 		/^\d{3,4}?$/g
 */
region.ValidaFormatoCodigoVerificacion=function(tipoTarjeta, largo, valor) {
	if(valor.toString().length <= largo){
		var expReg = /^\d{3,4}?$/g;
		return expReg.test(valor);
	}
	return false;
};


/*
 * Validar formato email
 * Formato: a@a.cl
 * La Expresión Regular utilizada es:
 * 		/^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*(\.[a-zA-Z]{2,15})$/g
 */
region.ValidaFormatoEmail=function(valor) {
	if(valor.length > 5){
		var expReg = /^[_a-zA-Z0-9-]+(\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*(\.[a-zA-Z]{2,15})$/g;
		return expReg.test(valor);
	}
	return false;
};

/*
 * Validar formato cuenta bancaria
 * Formato: 0001 | 000-1 | 0-0-0-1
 * La Expresión Regular utilizada es:
 * 		/^([0-9]|[-](?=[0-9]))+$/
 */
region.ValidaFormatoCuentaBancaria=function(valor) {
	if(valor && valor.length > 5){
		var expReg = /^([0-9]|[-](?=[0-9]))+$/;
		return expReg.test(valor);
	}
	return false;
};

