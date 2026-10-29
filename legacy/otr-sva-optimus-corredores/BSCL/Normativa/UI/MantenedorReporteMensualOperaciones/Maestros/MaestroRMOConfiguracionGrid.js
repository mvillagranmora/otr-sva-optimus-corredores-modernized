angular.module('ModuloNormativa').factory('bcsConfiguracionMaestroRMO',function($translate,bcsComunes,$timeout) {
	const obj = {};
	
	obj.ingresarCerrarVentana = function(id, idPadre){
		 if(id){
			 let promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $timeout(function() {
		    		 bcsComunes.ventanaCerrar(idPadre);
		    	 },20);
		     }); 
		 }else{
			 bcsComunes.ventanaCerrar(idPadre); 
		 }
	 }
	
	obj.validarFiltros = function(filtros, modo)
	{
		let filters = new Array();
		if(modo === 'PERIODO'){
			if (filtros.periodo)
				filters.push({ field: "Periodo", operator: "eq", value: filtros.periodo});
		}
		return filters;
	}
	
	let configuracionColumnas = function() {
		return {
			Periodo: 				{ field: "Periodo", 			title: $translate.instant('UI_PERIODO'), 				width: "90px", 	align: "left" },
			RutInstitucion: 		{ field: "RutInstitucion", 		title: $translate.instant('UI_RUT_INSTITUCION'), 		width: "90px", 	align: "left" },
			FechaPosicion: 			{ field: "FechaPosicion", 		title: $translate.instant('UI_FECHA_POSICION'), 		width: "100px", align: "left" },
			Concepto: 				{ field: "Concepto", 			title: $translate.instant('UI_CONCEPTO'), 				width: "90px", 	align: "rigth"},
			Moneda: 				{ field: "Moneda", 				title: $translate.instant('UI_MONEDA'), 				width: "50px", 	align: "left" },
			Paridad: 				{ field: "Paridad", 			title: $translate.instant('UI_PARIDAD'), 				width: "100px", align: "rigth", format: function(value){ return util.bcs_formato(value,'GEN','Monto','USD','',''); }},
			MontoMonedaPrincipal: 	{ field: "MontoMonedaPrincipal",title: $translate.instant('UI_MONTO_MONEDA_PRINCIPAL'), width: "100px", align: "rigth", format: function(value){ return util.bcs_formato(value,'GEN','Monto','USD','',''); }},
			MontoDolar: 			{ field: "MontoDolar", 			title: $translate.instant('UI_MONTO_DOLAR'), 			width: "100px", align: "rigth", format: function(value){ return util.bcs_formato(value,'GEN','Monto','USD','',''); }}
		}
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	

	obj.getSchemaReporteSaldoCuentasCambio = function() {
		return { 
			model: {
				fields : {
					Periodo							 :  { type : "string"},
					RutInstitucion 		 	 		 : 	{ type : "string"},
					FechaPosicion 				 	 : 	{ type : "date"},
					Concepto 	  		     	 	 : 	{ type : "number"},
					Moneda 	 	 		             : 	{ type : "number"},
					Paridad   	     				 : 	{ type : "number"},
					MontoMonedaPrincipal   	     	 : 	{ type : "number"},
					MontoDolar						 : 	{ type : "number"}
				}
			}
		}
	};
	      
	obj.getDataSourceReporteSaldoCuentasCambio = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Concepto", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290725",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaReporteSaldoCuentasCambio()			
			}
		})
	};
	

	obj.getConsultaReporteSaldoCuentasCambio = function() {
		let config = {};
		config = {
				buscar : function(apiGrid, filtros){
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'PERIODO'));
				},
				options : {
					userSettings:false,
					sortable: false,
		      		reorderable: false,
		      		filterable: false,
					groupable: true,
					pageable: true,
					selectable: "row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
					dataSource: obj.getDataSourceReporteSaldoCuentasCambio(),
					columns: [
						 	   obj.getConfiguracionColumna("Periodo")
					          ,obj.getConfiguracionColumna("RutInstitucion")
					          ,obj.getConfiguracionColumna("FechaPosicion")
					          ,obj.getConfiguracionColumna("Concepto")
					          ,obj.getConfiguracionColumna("Moneda")
					          ,obj.getConfiguracionColumna("Paridad")
					          ,obj.getConfiguracionColumna("MontoMonedaPrincipal")
					          ,obj.getConfiguracionColumna("MontoDolar")
					]
				}
		};
		return config;
	};

	return obj;
});