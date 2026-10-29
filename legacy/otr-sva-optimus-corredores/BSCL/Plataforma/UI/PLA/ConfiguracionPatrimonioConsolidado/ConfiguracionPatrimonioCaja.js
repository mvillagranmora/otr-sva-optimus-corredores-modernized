angular.module('ModuloPlataforma').controller('ConfiguracionPatrimonioCajaCtrl',function($scope,bcsComunes,$timeout,$compile,bcsServicioPlataforma,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	var configuracionPatrimonioCaja = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var datosGuardar = [];
	configuracionPatrimonioCaja.datosPorGuardar = false;
	
	configuracionPatrimonioCaja.defaultOptions = {
		contextMenu : true,
		filterable	: false,
		sortable 	: false,
		pageable 	: false,
		height 		: 345,
		autoBind 	: true,
		selectable 	: "multiple row",
		excel 		: { allPages: true },
		columns : [
			{field : "DescripcionItem",	title:$translate.instant('UI_ITEM'), width: "150px"},
			{field : "FuncionOperador",	title:$translate.instant('UI_OPERADOR'), width: "150px", editable: true}
		],
		save :function(e) {
			datosGuardar.push({CodigoItem: e.model.CodigoItem, FuncionOperador: e.values.FuncionOperador});
			configuracionPatrimonioCaja.datosPorGuardar = true;
		},
		dataSource : new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true,
				sort: { field: "CodigoItem", dir: "asc" },
				filter: [{ field : 'IdEstado', operator : 'eq', value : 'ACT' }],
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601212",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							CodigoItem						:	{ type : "string"},
							DescripcionItem					:	{ type : "string"},
							FuncionOperador					:	{ type : "string"},
							IdEstado						:	{ type : "string"}
						}
					}
				}			
			}
		}),
      	
      	gridCreated : function(grid) {
      		configuracionPatrimonioCaja.grid = grid;
      		configuracionPatrimonioCaja.grid.refresh();
		},
		
		dataBound : function(e) {
			bcsComunes.ventanaOverlay($scope.winID, false);
	    }
	}
	
	configuracionPatrimonioCaja.guardar = function(){
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
	    		p_IdUsuario 		: $scope.SesionIdUsuario,
	    		p_IdModulo 			: 'PLA',
	    		p_SimularOperacion 	: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_Items : datosGuardar
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().AdministraConfiguracionPatrimonioCaja(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			var id = bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result)
			},idVentana);
			limpiar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_GUARDAR_CONFIGURACION_PATRIMONIO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			limpiar();
		})
		
	}
	
	var limpiar = function(){
		datosGuardar = [];
		configuracionPatrimonioCaja.datosPorGuardar = false;
		configuracionPatrimonioCaja.grid.dataSource.read();
	}
	
});