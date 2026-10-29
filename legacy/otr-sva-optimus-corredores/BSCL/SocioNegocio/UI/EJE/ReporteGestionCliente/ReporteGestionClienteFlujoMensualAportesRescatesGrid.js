angular.module('ModuloSocioNegocio').factory('bcsReporteGestionClienteFlujoMensualAportesRescatesGrid',
		function(bcsComunes, $rootScope, $translate)  
{
	
	var obj = {};
	
	
	modeloReporteGestionCliente = function() {
		return {
			model: {
				fields : {
					NombreAgente 	    : { type: "string"},
					NombreCliente	    : { type: "string"},
					Monto_01	 		: { type: "number"},
					Monto_02	        : { type: "number"},
					Monto_03	     	: { type: "number"},
					Monto_04			: { type: "number"},
					Monto_05			: { type: "number"},
					Monto_06		    : { type: "number"},
					Monto_07			: { type: "number"},
					Monto_08			: { type: "number"},
					Monto_09	  	    : { type: "number"},
					Monto_10	 		: { type: "number"},
					Monto_11 			: { type: "number"},
					Monto_12		    : { type: "number"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceReporteGestionCliente = new bcs.newDataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
		definition 	: {
						serverFiltering : true, 	// Filtrado de datos en el servidor.
						serverPaging 	: true, 	// Paginado de los datos en el servidor.
						pageSize		: 200,
						callTransaction	: "700021",	// Numero de servicio
						callResultSet	: "P_OUT_TBL",
			            schema 			: modeloReporteGestionCliente()
		              }  
	});
	
	// Formato de columnas de los totales por mes
	modeloFormatNumerico = function(mes, tituloMes) {
		let columna = "Monto_" + mes;
		let templateNumerico = "<label> #: (util.bcs_formato(" + columna +",'GEN','Cantidad','CLP','','')).valor #</label>";
		let objFormato = {field : columna,	title: tituloMes, width : "100px", sortable : false, template : templateNumerico, headerAttributes: {"class": "HeaderCenter"}, attributes: {"class": "columnaNumero"}};
		return objFormato;
	}
	
	
	obj.columnasReporteGestionCliente = function(agno) {
		let d = new Date();					
		let diaActual 	= d.getDate();		// dia actual
		let mesActual 	= d.getMonth() + 1;	// mes actual getMonth() -> 0=Enero, 1=Febrero...
		let agnoActual 	= d.getFullYear();	// agno acutal
		
		let objColumnas = [
			 {field : "NombreAgente", 	title:	$translate.instant("UI_NOMBRE_AGENTE"),  width : "200px", align: "left" , sortable : true, editable: false, attributes: {"class": "columnaTexto"} }			 			 
			,{field : "NombreCliente", 	title:	$translate.instant("UI_NOMBRE_CLIENTE"), width : "200px", align: "left" , sortable : true, editable: false, attributes: {"class": "columnaTexto"} }
		];
		
		let ultimoDiaMes, tituloTotalMes, m = 0;
		const listaMeses = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
		for (let i = 0; i < listaMeses.length; i++) {
			m = i + 1;
			ultimoDiaMes = new Date(agno, m, 0).getDate();						// Busca el ultimo dia del mes
			
			if (agno === agnoActual && (mesActual === m)) {
				tituloTotalMes = agno + '-' + listaMeses[i] + '-' + diaActual;	// Muestra el Titulo del mes con la fecha actual y sale del ciclo para no mostrar los siguientes meses
				objColumnas.push(modeloFormatNumerico(listaMeses[i], tituloTotalMes));
				break;
			}
			tituloTotalMes = agno + '-' + listaMeses[i] + '-' + ultimoDiaMes;	// Titulo de la columna
			objColumnas.push(modeloFormatNumerico(listaMeses[i], tituloTotalMes));
		}
		return objColumnas;
	};
		
	return obj;
});	


