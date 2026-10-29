angular.module('ModuloAPV').controller('MantenedorBancoPreviredCtrl',function($scope,$translate,bcsComunes,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA){
	var mantenedorBancoPrevired = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	
	mantenedorBancoPrevired.filtros = {};
	mantenedorBancoPrevired.datos = {};
	
	mantenedorBancoPrevired.filtrosComboboxCuentaBanco = {
			IdBanco : { "field": "IdBanco","operator": "eq", "value":  undefined}
	}
	
	
	mantenedorBancoPrevired.gridBancoRecaudacion = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 15,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700181",
							dataType	: "json"
					    }
	                },
					schema: {
						model: {
							fields: {
								CodigoBancoPrevired	: { type: "string"},
								IdBanco				: { type: "number"},
								NombreBanco			: { type: "string"},
								CodigoBanco			: { type: "string"},
								NombreTipoBanco    	: { type: "string"},
								IdCuentaBanco		: { type: "number"},
								NombreCuentaBancaria: { type: "string"},
								NumeroCuentaBancaria: { type: "string"},
								IdMoneda			: { type: "string"},
								IdTipoCuentaBancaria: { type: "string"},
								NombreTipoCuentaBancaria: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "CodigoBancoPrevired"	,title: $translate.instant('UI_CODIGO_BANCO_PREVIRED'),  width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "NombreCuentaBancaria"	,title: $translate.instant('UI_NOMBRE_CUENTA_BANCARIA'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "NumeroCuentaBancaria"	,title: $translate.instant('UI_NOMBRE_CUENTA_BANCARIA'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "NombreTipoCuentaBancaria",title: $translate.instant('UI_TIPO_CUENTA_BANCARIA'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "NombreBanco"			,title: $translate.instant('UI_BANCO'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "CodigoBanco"			,title: $translate.instant('UI_CODIGO_BANCO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "NombreTipoBanco"		,title: $translate.instant('UI_TIPO_BANCO'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "IdMoneda"				,title: $translate.instant('UI_MONEDA'), width: "80px", align: "left" , sortable : true, editable: false }
			],
			gridCreated : function(grid){
				mantenedorBancoPrevired.ajustaVentana();
			},
			sortable			: false,
			pageable			: true,
			autoBind			: true,
	    	filterable			: true,
			height				: 350,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true
	};
	
	mantenedorBancoPrevired.buscarBancoPrevired = function() {
		
		var filtroBusqueda = new Array();
		
		if(mantenedorBancoPrevired.filtros.IdBanco){
			filtroBusqueda.push({ field: "IdBanco", operator: "eq", value: mantenedorBancoPrevired.filtros.IdBanco});
		}
		if(mantenedorBancoPrevired.filtros.IdCuentaBanco){
			filtroBusqueda.push({ field: "IdCuentaBanco", operator: "eq", value: mantenedorBancoPrevired.filtros.IdCuentaBanco});
		}
		
		mantenedorBancoPrevired.gridBancoRecaudacion.dataSource.filter(filtroBusqueda);
	};
	
	
	mantenedorBancoPrevired.crearBancoPrevired = function () {
		var id = bcsComunes.ventanaAbrir("IngresarBancoPrevired",false,false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	mantenedorBancoPrevired.gridBancoRecaudacion.dataSource.read();
        	mantenedorBancoPrevired.datos.kNgModel = undefined;
        }); 
  	};
	
	
  	mantenedorBancoPrevired.modificarBancoPrevired = function (datos) {
		if(!datos || datos.length == 0) return;
		var id = bcsComunes.ventanaAbrir("EditarBancoPrevired",datos[0],false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	mantenedorBancoPrevired.gridBancoRecaudacion.dataSource.read();
        	mantenedorBancoPrevired.datos.kNgModel = undefined;
        }); 
  	};
  	
  	mantenedorBancoPrevired.elimminarBancoPrevired = function (datos) {
		if(!datos || datos.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ELIMINAR_BANCO_PREVIRED'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var parametros = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'APV',
					p_SimularOperacion : 'N',
					p_UsaReglasNegocio : 'S',
					p_BancosPrevired : []	
				};
				
		    	$.each(datos,function(index,banco){
		    		parametros.p_BancosPrevired.push({IdInterno: index, CodigoBancoPrevired: banco.CodigoBancoPrevired});
		  		});
		    	
		    	bcsComunes.ventanaOverlay(idVentana,true);
		    	bcsServiciosAPV.EliminarBancoPrevired(parametros).$promise.then(function(respuesta){
		    		if(respuesta.P_OUT_ERRORESCONTADOR == 0){
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CODIGO_BANCO_PREVIRED_ELIMINADO_CORRECTAMENTE"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		    		}else{
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ELIMINAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		    		}
		    		bcsComunes.ventanaOverlay(idVentana,false);
		    		mantenedorBancoPrevired.gridBancoRecaudacion.dataSource.read();
		        	mantenedorBancoPrevired.datos.kNgModel = undefined;
		    	},function(respuesta){
		    		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ELIMINAR_BANCO_PREVIRED"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		    	});
			}
		});
  	};
  	
  	mantenedorBancoPrevired.ajustaVentana = function() {
		 bcsComunes.ventanaTamanoAutomatico(idVentana,40);
    }
  	
  	mantenedorBancoPrevired.limpiarFiltros = function() {
  		mantenedorBancoPrevired.filtros.IdBanco = 0;
	};
		
	mantenedorBancoPrevired.limpiarFiltros();
});