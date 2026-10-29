angular.module('ModuloPlataforma').controller('ConsultaUsoGlosaTipoInstrumentoCtrl',function($scope,bcsComunes,$timeout,$compile,bcsServicioPlataforma,$translate){
	var consultaUsoGlosaTipoInstrumento = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	consultaUsoGlosaTipoInstrumento.filtros = {};
	
	consultaUsoGlosaTipoInstrumento.HabilitaBaja = false;
	consultaUsoGlosaTipoInstrumento.HabilitaSube = false;
	
	consultaUsoGlosaTipoInstrumento.defaultOptionsAll = {
			allowCopy: true,
			autoBind: true,
			userSettings : false,
			navigatable : true,
			excel : {
				allPages: true,
			},
	      	filterable: false,
	      	height:350,
	      	pageable : false,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			columns : [
			           {field: "Nombre",								title : "UI_TERMINO", width: "75px"},
			           {field: "Descripcion",							title : "UI_DEFINICION", width: "75px"}
			           ],

			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: [
						{ field: "IdGlosaTipoInstrumento", dir: "asc" }
					],
					pageSize:1000,
					//filter : [ { field : 'IdGlosaTipoInstrumento', operator : 'eq', value : 'USD' } ],
					schema: {
						model : {
							fields : {
								IdGlosaTipoInstrumento  		: { type: "string"},
								Nombre  						: { type: "string"},
								Descripcion  					: { type: "string"},
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=101010268",
							dataType: "json"
						}
					}
				}
	      	}),
	};
	
	consultaUsoGlosaTipoInstrumento.defaultOptionsAct = {
			allowCopy: true,
			autoBind: false,
			userSettings : false,
			navigatable : true,
			excel : {
				allPages: true,
			},
	      	filterable: false,
	      	height:350,
	      	pageable : false,
	      	reorderable: false,
			resizable: true,
			scrollable: true,
	      	selectable: "row",
			sortable: false,
			columns : [
			           
			           {field: "IdTipoInstrumento",								title : "UI_TIPO_INSTRUMENTO", width: "75px"},
			           {field: "Nombre",										title : "UI_TERMINO", width: "75px"},
			           {field: "Prioridad",										title : "UI_PRIORIDAD", width: "75px"}
			           ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: [ 
						{ field: "Prioridad", dir: "asc" }
					],
					pageSize:1000,
					schema: {
						model : {
							fields : {
								IdGlosa  							: { type: "string"},
								IdTipoInstrumento  					: { type: "string"},
								Prioridad  							: { type: "number"},
								Nombre  							: { type: "string"},
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=101010269",
							dataType: "json"
						}
					}
				}
	      	}),
	};

	$scope.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
			filtros : {
				complejo:'true',
						logic:'and',
						filters:[
							{ "field" : "IdEstado", "operator": "eq", "value" : "VIG" },
							{logic: "or",
							filters:[
								{ "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "RF" },
								{ "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "IF" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "SPT" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "MON" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "ACN" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "ETX" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "ETN" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "CFI" },
								{ "field" : "IdTipoInstrumento",                       "operator": "eq", "value" : "CFX" },
							]
						},


						]
			},
			idActual : consultaUsoGlosaTipoInstrumento.filtros.uso,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
	};
	
	consultaUsoGlosaTipoInstrumento.buscar = function(){
		if(!consultaUsoGlosaTipoInstrumento.filtros.uso) return;
		var filters = [];
		filters.push({ field : 'IdTipoInstrumento', operator : 'eq', value : consultaUsoGlosaTipoInstrumento.filtros.uso });
		
		consultaUsoGlosaTipoInstrumento.apiGridAct.dataSource.filter(filters);
		
	}
	
	$scope.$watch('consultaUsoGlosaTipoInstrumento.filtros.uso',function(newVal,oldVal){
		if(newVal){
			consultaUsoGlosaTipoInstrumento.buscar();
		}
	});
	
	consultaUsoGlosaTipoInstrumento.agregar = function(){
		if(!consultaUsoGlosaTipoInstrumento.filtros.uso || !consultaUsoGlosaTipoInstrumento.registroSeleccionadoAll || consultaUsoGlosaTipoInstrumento.registroSeleccionadoAll.length == 0) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_InGlosasTipoInstrumento: []
		}
		
		$.each(consultaUsoGlosaTipoInstrumento.registroSeleccionadoAll,function(key,value){
			datos.p_InGlosasTipoInstrumento.push({IdTipoInstrumento : consultaUsoGlosaTipoInstrumento.filtros.uso, IdGlosa: value.IdGlosaTipoInstrumento});
		});
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().AgregarUsoGlosaTIpoInstrumento(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			
			consultaUsoGlosaTipoInstrumento.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_GLOSA_TIPO_INSTRUMENTO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoGlosaTipoInstrumento.buscar();
		})
	}
	
	consultaUsoGlosaTipoInstrumento.quitar = function(){
		if(!consultaUsoGlosaTipoInstrumento.filtros.uso || !consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_InGlosasTipoInstrumento: [{
				IdTipoInstrumento : consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.IdTipoInstrumento,
				IdGlosa: consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.IdGlosa
			}]
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().QuitarUsoGlosaTIpoInstrumento(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct = undefined;
			consultaUsoGlosaTipoInstrumento.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_GLOSA_TIPO_INSTRUMENTO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct = undefined;
			consultaUsoGlosaTipoInstrumento.buscar();
		})
	}
	
	consultaUsoGlosaTipoInstrumento.cambiar = function(subeBaja){
		if(!consultaUsoGlosaTipoInstrumento.filtros.uso || !consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_IdTipoInstrumento : consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.IdTipoInstrumento,
			p_IdGlosa : consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.IdGlosa,
			p_PrioridadNueva : subeBaja === "BAJA" ? consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.Prioridad + 1 : consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.Prioridad - 1,
			p_PrioridadAnterior : consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct.Prioridad
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().CambiarPrioridadUsoGlosaTIpoInstrumento(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct = undefined;
			consultaUsoGlosaTipoInstrumento.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_GLOSA_TIPO_INSTRUMENTO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct = undefined;
			consultaUsoGlosaTipoInstrumento.buscar();
		})
	}
	
	$scope.$watch('consultaUsoGlosaTipoInstrumento.registroSeleccionadoAct',function(newVal,oldVal){
		if(newVal && consultaUsoGlosaTipoInstrumento.filtros.uso){
			if(newVal.Prioridad != 1){
				consultaUsoGlosaTipoInstrumento.HabilitaSube = true;
			}else{
				consultaUsoGlosaTipoInstrumento.HabilitaSube = false;
			}
			if(consultaUsoGlosaTipoInstrumento.apiGridAct.dataSource.data().length != newVal.Prioridad){
				consultaUsoGlosaTipoInstrumento.HabilitaBaja = true;
			} else{
				consultaUsoGlosaTipoInstrumento.HabilitaBaja = false;
			}
		}else{
			consultaUsoGlosaTipoInstrumento.HabilitaBaja = false;
			consultaUsoGlosaTipoInstrumento.HabilitaSube = false;
		}
	});

});