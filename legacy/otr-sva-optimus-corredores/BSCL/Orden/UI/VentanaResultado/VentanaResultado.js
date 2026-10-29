angular.module('ModuloOrdenes').controller('VentanaResultadoCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2) {

	$scope.apiGrid;	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana); //nombreSecuencia, tipo, datos, debe seguir el estandar de salida de sp
	bcsComunes.ventanaTitulo(idVentana,parametros.titulo)
	
	//ID OPERACION // RESULTADO // OBSERVACIONES -> SI ES UN MENSAJE, SE MUESTRA ESE MENSAJE, SI SON VARIOS, SE MUESTRA CUANTOS Y DE QUE TIPO: EJ 4 ERRORES, 2 ADVERTENCIAS
	/*var informacionDetalle = [
	                    { IdInterno : 1 , IdOperacion : 1 ,Codigo: 'CODIGO_1',Descripcion: 'movimiento facturar creado 111'},
	                    { IdInterno : 2 , IdOperacion : 2 ,Codigo: 'CODIGO_1',Descripcion: 'movimiento facturar creado 222'},
	                    ];
	
	var advertenciasDetalle = [
							{ IdInterno : 2 , IdOperacion : 2 ,Codigo: 'CODIGO_23',Descripcion: 'falta algo pero pasa igual'},
	  	                    { IdInterno : 3 , IdOperacion : 3 ,Codigo: 'CODIGO_2',RequiereAutorizacion : 'N', Descripcion: 'falta algo'},
	  	                    { IdInterno : 3 , IdOperacion : 3 ,Codigo: 'CODIGO_2',RequiereAutorizacion : 'N',Descripcion: 'falta algo mas'},
	  	                    { IdInterno : 3 , IdOperacion : 3 ,Codigo: 'CODIGO_3',RequiereAutorizacion : 'S',Descripcion: 'falta algo que requiere autorizacion'},
	  	                    ];
	
	var erroresDetalle = [
	  	                    { IdInterno : 3 , IdOperacion : 3 ,Codigo: 'CODIGO_4',Descripcion: 'error fatal 1'},
	  	                    { IdInterno : 4 , IdOperacion : 4 ,Codigo: 'CODIGO_4',Descripcion: 'error fatal 2'},
	  	                    ];
	
	var resultado = {
			P_OUT_INFORMACIONDETALLE : informacionDetalle,
			P_OUT_ADVERTENCIASDETALLE : advertenciasDetalle,
			P_OUT_ERRORESDETALLE : erroresDetalle,
	};*/
	
	var operacionesArray = [];
	var operacionesMap = {};
	
	var mmmm = [];
	if (parametros.usaNuevosMensajes) {
		$.each(parametros.datos,function(key,val){
			var msje = {};
			msje.IdInterno = val[parametros.campoSecuencia];
			msje.Codigo = val.codigo;
			msje.Descripcion = val.descripcion;
			if (val.tipo === "ERROR")
				msje.Resultado = "UI_ERROR";
			if (val.tipo === "OK")
				msje.Resultado = "UI_OK";
			if (val.tipo === "AUTORIZACION")
				msje.Resultado = "UI_AUTORIZACION";
			if (val.tipo === "ADVERTENCIA")
				msje.Resultado = "UI_ADVERTENCIA";
			if (val.tipo === "INFORMACION")
				msje.Resultado = "UI_INFORMACION";
			mmmm.push(msje);
		})
	}
	//modelo antiguo
	else {
		$.each(parametros.datos.P_OUT_ERRORESDETALLE,function(key,val){
			mmmm.push({
				Resultado : 'UI_ERROR',
				IdInterno : val[parametros.campoSecuencia] , 
				IdOperacion : val.IdOperacion ,
				Codigo: val.Codigo,
				Descripcion: val.Descripcion
			});
		})
		$.each(parametros.datos.P_OUT_ADVERTENCIASDETALLE,function(key,val){
			mmmm.push({
				Resultado : val.RequiereAutorizacion === 'S'?'UI_AUTORIZACION':'UI_ADVERTENCIA' ,
				IdInterno : val[parametros.campoSecuencia] ,
				IdOperacion : val.IdOperacion ,
				Codigo: val.Codigo,
				RequiereAutorizacion : val.RequiereAutorizacion,
				Descripcion: val.Descripcion
			});
		})
		$.each(parametros.datos.P_OUT_INFORMACIONDETALLE,function(key,val){
			mmmm.push({
				Resultado : 'UI_INFORMACION',
				IdInterno : val[parametros.campoSecuencia] , 
				IdOperacion : val.IdOperacion ,
				Codigo: val.Codigo,
				Descripcion: val.Descripcion
			});
		})
	}
	
	
	
	$.each(mmmm,function(key,val) {
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
								IdInterno		: {type : parametros.usaCampoSecuenciaTipoString ? "string" : "number"},
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
	
	$scope.confGrid = {
		gridCreated : function(e) {
			apiRef = e;
		},
		autoBind: true,
		height : 300,
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
							IdInterno		: {type : parametros.usaCampoSecuenciaTipoString ? "string" : "number"},
							IdOperacion		: {type : "number"},
							Resultado		: {type : "string"},
							Informacion		: {type : "string"}
						}
					}
				}
			}
			
		}),
		columns : [
		           { field: "IdInterno",	title: parametros.nombreSecuencia,	width: "125px"},
		           { field: "Resultado",	title: 'UI_RESULTADO',				width: "100px",template : "<span translate>#: Resultado # </span>"},
		           { field: "Informacion",	title: 'UI_INFORMACION'},

		]
	};
	
	$scope.aceptar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};		
});