angular.module('ModuloPlataforma').controller('ConsultaUsoParametroFinancieroCtrl',function($scope,bcsComunes,$timeout,$compile,bcsServicioPlataforma,$translate){
	var consultaUsoParametroFinanciero = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	consultaUsoParametroFinanciero.filtros = {};
	
	consultaUsoParametroFinanciero.HabilitaBaja = false;
	consultaUsoParametroFinanciero.HabilitaSube = false;
	
	consultaUsoParametroFinanciero.defaultOptionsAll = {
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
			           {field: "IdParametroFinanciero",				title : "UI_PARAMETRO_FINANCIERO", width: "75px"},
			           {field: "Nombre",							title : "UI_NOMBRE", width: "75px"}
			           ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: [
						{ field: "IdParametroFinanciero", dir: "asc" }
					],
					pageSize:1000,
					filter : [ { field : 'IdMonedaParametro', operator : 'eq', value : 'USD' } ],
					schema: {
						model : {
							fields : {
								IdParametroFinanciero  			: { type: "string"},
								Nombre  						: { type: "string"},
								Descripcion  					: { type: "string"},
								Periodo  						: { type: "string"},
								IdMonedaParametro  				: { type: "string"},
								IdMonedaValor  					: { type: "string"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600070",
							dataType: "json"
						}
					}
				}
	      	}),
	};
	
	consultaUsoParametroFinanciero.defaultOptionsAct = {
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
			           
			           {field: "IdUso",								title : "UI_USO", width: "75px"},
			           {field: "Nombre",							title : "UI_PARAMETRO_FINANCIERO", width: "75px"},
			           {field: "Prioridad",							title : "UI_PRIORIDAD", width: "75px"}
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
								IdUso  							: { type: "string"},
								IdParametroFinanciero  			: { type: "string"},
								Prioridad  						: { type: "number"},
								Nombre  						: { type: "string"},
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600069",
							dataType: "json"
						}
					}
				}
	      	}),
	};
	
	consultaUsoParametroFinanciero.buscar = function(){
		if(!consultaUsoParametroFinanciero.filtros.uso) return;
		var filters = [];
		filters.push({ field : 'IdUso', operator : 'eq', value : consultaUsoParametroFinanciero.filtros.uso });
		
		consultaUsoParametroFinanciero.apiGridAct.dataSource.filter(filters);
		
	}
	
	$scope.$watch('consultaUsoParametroFinanciero.filtros.uso',function(newVal,oldVal){
		if(newVal){
			consultaUsoParametroFinanciero.buscar();
		}
	});
	
	consultaUsoParametroFinanciero.agregar = function(){
		if(!consultaUsoParametroFinanciero.filtros.uso || !consultaUsoParametroFinanciero.registroSeleccionadoAll || consultaUsoParametroFinanciero.registroSeleccionadoAll.length == 0) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_UsosParidadParametroFinanciero: []
		}
		
		$.each(consultaUsoParametroFinanciero.registroSeleccionadoAll,function(key,value){
			datos.p_UsosParidadParametroFinanciero.push({IdUso : consultaUsoParametroFinanciero.filtros.uso, IdParametroFinanciero: value.IdParametroFinanciero});
		});
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().AgregarUsoParidadParametroFinanciero(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			
			consultaUsoParametroFinanciero.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_PARAMETRO_FINANCIERO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoParametroFinanciero.buscar();
		})
	}
	
	consultaUsoParametroFinanciero.quitar = function(){
		if(!consultaUsoParametroFinanciero.filtros.uso || !consultaUsoParametroFinanciero.registroSeleccionadoAct) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_UsosParidadParametroFinanciero: [{
				IdUso : consultaUsoParametroFinanciero.registroSeleccionadoAct.IdUso,
				IdParametroFinanciero: consultaUsoParametroFinanciero.registroSeleccionadoAct.IdParametroFinanciero
			}]
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().QuitarUsoParidadParametroFinanciero(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			consultaUsoParametroFinanciero.registroSeleccionadoAct = undefined;
			consultaUsoParametroFinanciero.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_PARAMETRO_FINANCIERO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoParametroFinanciero.registroSeleccionadoAct = undefined;
			consultaUsoParametroFinanciero.buscar();
		})
	}
	
	consultaUsoParametroFinanciero.cambiar = function(subeBaja){
		if(!consultaUsoParametroFinanciero.filtros.uso || !consultaUsoParametroFinanciero.registroSeleccionadoAct) return;
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: "PLA",
			p_IdUso : consultaUsoParametroFinanciero.registroSeleccionadoAct.IdUso,
			p_IdParametroFinanciero : consultaUsoParametroFinanciero.registroSeleccionadoAct.IdParametroFinanciero,
			p_PrioridadNueva : subeBaja === "BAJA" ? consultaUsoParametroFinanciero.registroSeleccionadoAct.Prioridad + 1 : consultaUsoParametroFinanciero.registroSeleccionadoAct.Prioridad - 1,
			p_PrioridadAnterior : consultaUsoParametroFinanciero.registroSeleccionadoAct.Prioridad
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().CambiarPrioridadUsoParidadParametroFinanciero(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR > 0){
				var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}
			consultaUsoParametroFinanciero.registroSeleccionadoAct = undefined;
			consultaUsoParametroFinanciero.buscar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTUALIZAR_USO_PARAMETRO_FINANCIERO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultaUsoParametroFinanciero.registroSeleccionadoAct = undefined;
			consultaUsoParametroFinanciero.buscar();
		})
	}
	
	$scope.$watch('consultaUsoParametroFinanciero.registroSeleccionadoAct',function(newVal,oldVal){
		if(newVal && consultaUsoParametroFinanciero.filtros.uso){
			if(newVal.Prioridad != 1){
				consultaUsoParametroFinanciero.HabilitaSube = true;
			}else{
				consultaUsoParametroFinanciero.HabilitaSube = false;
			}
			if(consultaUsoParametroFinanciero.apiGridAct.dataSource.data().length != newVal.Prioridad){
				consultaUsoParametroFinanciero.HabilitaBaja = true;
			} else{
				consultaUsoParametroFinanciero.HabilitaBaja = false;
			}
		}else{
			consultaUsoParametroFinanciero.HabilitaBaja = false;
			consultaUsoParametroFinanciero.HabilitaSube = false;
		}
	});

});