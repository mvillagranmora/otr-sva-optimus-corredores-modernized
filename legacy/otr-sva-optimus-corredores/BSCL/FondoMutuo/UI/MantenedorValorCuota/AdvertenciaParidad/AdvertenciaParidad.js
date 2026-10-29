angular.module('ModuloFFM').controller('AdvertenciaParidadCtrl',function($scope,$translate,bcsComunes,bcsServicioFondosMutuos,$timeout){

	$scope.winID = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);

	var operacionesArray = [];
	var operacionesMap = {};
	
	var mensajesGrid = [];
	if (parametros.usaNuevosMensajes) {
		$.each(parametros.datos,function(key,val){
			var msje = {};
			msje.IdInterno = val[parametros.campoSecuencia];
			msje.Codigo = val.codigo;
			msje.Descripcion = val.descripcion;
			if (val.tipo === "ERROR")
				msje.Resultado = "UI_ERROR";
			if (val.tipo === "AUTORIZACION")
				msje.Resultado = "UI_AUTORIZACION";
			if (val.tipo === "ADVERTENCIA")
				msje.Resultado = "UI_ADVERTENCIA";
			if (val.tipo === "INFORMACION")
				msje.Resultado = "UI_INFORMACION";
			mensajesGrid.push(msje);
		})
	}
	
	$.each(mensajesGrid,function(key,val) {
		var operacion = operacionesMap[val.IdInterno];
		if (!operacion) {
			operacion = {
				IdInterno	: val.IdInterno, 
				Resultado 	: val.Resultado,
				Informacion : '',
				UI_ERROR		: 0,
				UI_AUTORIZACION	: 0,
				UI_ADVERTENCIA	: 0,
				UI_INFORMACION: 0,
				Mensajes	: []
			};
			operacionesMap[val.IdInterno] = operacion;
			operacionesArray.push(operacion);
		}
		var err = {
				IdInterno	: val.IdInterno, 
				IdOperacion : val.IdOperacion, 
				Resultado 	: val.Resultado,
				Codigo 		: val.Codigo, 
				Descripcion : val.Descripcion
				};
		operacion[err.Resultado]++;
		operacion.Mensajes.push(err);
		
	});	
	
	$.each(operacionesArray,function(key,operacion){
		if (operacion.Mensajes.length === 1) {
			operacion.Informacion = operacion.Mensajes[0].Descripcion;
		} else {
			var texts = [];
			if (operacion.UI_ERROR > 0)
				texts.push(operacion.UI_ERROR + ' ' + 'error(es)');
			if (operacion.UI_AUTORIZACION > 0)
				texts.push(operacion.UI_AUTORIZACION + ' ' + 'autorizacion(es)');
			if (operacion.UI_ADVERTENCIA > 0)
				texts.push(operacion.UI_ADVERTENCIA + ' ' + 'advertencia(s)');
			if (operacion.UI_INFORMACION > 0)
				texts.push(operacion.UI_INFORMACION + ' ' + 'informacion(es)');
			
			for (var ii = 0;ii<texts.length;ii++) {
				if (ii === texts.length-1)
					operacion.Informacion+=texts[ii];
				else
					operacion.Informacion+=texts[ii] + ', ';
			}

		}

	});
	
	
	
	/*
	 * ID INTERNO: 1, RESULTADO: SIN ERRORES, INFORMACION: MOVIMIENTO A FACTURAR CREADO NRO 1111  
	 * ID INTERNO: 2, RESULTADO: SIN ERRORES, INFORMACION: MOVIMIENTO A FACTURAR CREADO NRO 1112  
	 * ID INTERNO: 3, RESULTADO: ERROR, INFORMACION: 1 ERROR(ES), 1 AUTORIZACION(ES), 2 ADVERTENCIA(S)  
	 * ID INTERNO: 4, RESULTADO: ERROR, INFORMACION: 1 ERROR(ES) 
	 */
	
	var datos = operacionesArray;
	
	$scope.confGrid = {
		gridCreated : function(e) {
			apiRef = e;
		},
		autoBind: true,
		height : 150,
		sortable : true,
		pageable : true,
		filterable	: true,
		rowColorBackground : function(row) {
			switch(row.get("Resultado")) {
				case 'UI_INFORMACION':
					return "green";
					break;
				case 'UI_ADVERTENCIA':
					return "yellow";
					break;
				case 'UI_AUTORIZACION':
					return "yellow";
					break;
				case 'UI_ERROR':
					return "red";
					break;
			}
		},
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				data : datos,
				serverSorting : false,
				sort: { field: "IdInterno", dir: "asc" },
				schema : {
					model : {
						fields : {	
							IdInterno		: {type : "string"},
							IdOperacion		: {type : "number"},
							Resultado		: {type : "string"},
							Informacion		: {type : "string"}
						}
					}
				}
			}
			
		}),
		columns : [
		           { field: "IdInterno",	title: 'UI_INSTRUMENTO',	width: "125px"},
		           { field: "Informacion",	title: 'UI_INFORMACION'},

		]
	};

	$scope.confDetalle = function(dataItem) {
		return {
			autoBind: true,
			height : 100,
			rowColorBackground : function(row) {
				switch(row.get("Resultado")) {
					case 'UI_INFORMACION':
						return "green";
						break;
					case 'UI_ADVERTENCIA':
						return "yellow";
						break;
					case 'UI_AUTORIZACION':
						return "yellow";
						break;
					case 'UI_ERROR':
						return "red";
						break;
				}
			},
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					data : dataItem.Mensajes,
					schema : {
						model : {
							fields : {	
								IdInterno		: {type : "number"},
								IdOperacion		: {type : "number"},
								Resultado		: {type : "string"},
								Codigo			: {type : "string"},
								Descripcion		: {type : "string"}
							}
						}
					}
				}
				
			}),
			columns : [
			           { field: "Resultado",	title: 'UI_RESULTADO',			width: "100px",template : "<span translate>#: Resultado # </span>"},
			           //{ field: "Codigo",		title: 'UI_CODIGO',				width: "75px"},
			           { field: "Descripcion",	title: 'UI_MENSAJE'},
	
			]
		};
	}

	$scope.aceptar = function(){
		bcsComunes.ventanaData($scope.winID,{aceptar:true})
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar($scope.winID);
	};
	

	
})