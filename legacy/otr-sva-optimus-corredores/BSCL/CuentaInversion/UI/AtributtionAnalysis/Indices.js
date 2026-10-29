angular.module('ModuloCuentaInversion').controller('IndicesCtrl',
		function($scope,bcsComunes,$timeout,$compile,bcsServicioPlataforma,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	var indices = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var datosGuardar = {};
	var datosPorGuardar = false;
	indices.filtros = {
			periodo : util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01')
	};
	
	indices.grillaCargada = false;
	
	indices.defaultOptions = {
		contextMenu : true,
		filterable	: false,
		sortable 	: false,
		pageable 	: false,
		height 		: 345,
		autoBind 	: true,
		selectable 	: "multiple row",
		excel 		: { allPages: true },
		rowColorBackground : function(row) {
			if(row["TipoFecha"]){
				switch(row["TipoFecha"].toUpperCase()) {
				case 'INHABIL':
					return "red";
					break;
				case 'FERIADO':
					return "red";
					break;
				case 'HABIL':
					return "green";
					break;	
				}
			}
		},
		columns : [],
		save :function(e) {
			$.each(e.model,function(key,value){
				if(key.includes("_ID")){
					var valor = e.values[e.model[key] + '_VALOR']
					var idIndiceFinanciero = e.model[key]
					if(valor !== undefined){	
						if (!datosGuardar[e.model.Fecha +"-"+idIndiceFinanciero]) datosGuardar[e.model.Fecha +"-"+idIndiceFinanciero] = {};
						datosGuardar[e.model.Fecha +"-"+idIndiceFinanciero] = {
							Fecha : e.model.Fecha,
							IdIndiceFinanciero : idIndiceFinanciero,
							Valor :valor,
						}
						datosPorGuardar = true
					}
				}
			})
		},
		dataSource : new bcs.DataSource({
      		type : 'array',
      		definition : {
				schema: {
					model : {
						fields : {}
					}
				}
			}
      	}),
      	
      	gridCreated : function(grid) {
      		indices.grid = grid;
      		indices.grid.refresh();
		},
		
		dataBound : function(e) {
			bcsComunes.ventanaOverlay($scope.winID, false);
	    }
	}
	
	var consultar = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo 	: 'PLA',
				p_Fecha 	: indices.filtros.tipoIndice == 'DI' ? util.dateSPdate(indices.filtros.periodo).substring(0,7) : util.dateSPdate(indices.filtros.periodo).substring(0,4),
				p_TipoPeriodo : indices.filtros.tipoIndice,
				p_TipoParametro : 'Indice'
		}
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().ConsultaIndicesCorredora(datos).$promise;
		p.then(function(result){
			datosGuardar = {};
			datosPorGuardar = false;
			indices.grillaCargada = false;
			var columnasIndice = [];
			var camposIndice = {};
			var fechaDatos = {};
			var datosIndice = [];
			if(indices.filtros.tipoIndice == 'DI') {
				columnasIndice.push({field : "Dia",	title:"UI_DIA",  width: "30px"});
				camposIndice["Dia"] = {type : "string"};
			}
			
			if(indices.filtros.tipoIndice == 'ME') {
				columnasIndice.push({field : "AgnoMes",	title:"UI_PERIODO",  width: "30px"});
				camposIndice["AgnoMes"] = {type : "string"};
			}
			
			if(indices.filtros.tipoIndice == 'AN') {
				columnasIndice.push({field : "Agno",	title:"UI_ANIO",  width: "30px"});
				camposIndice["Agno"] = {type : "string"};
			}
			//Aca debo modificar para que la grilla no sea editable  editable: true}
			camposIndice["Fecha"] = {type : "string"};
			camposIndice["TipoFecha"] = {type : "string"};
			if(result.P_PARAMETROS_FINANCIEROS && result.P_VALORES_PARAMETROS_FINANCIEROS){
				$.each(result.P_PARAMETROS_FINANCIEROS,function(key,value){
					var nombreValor = value.IdIndiceFinanciero + '_VALOR'
					var nombreID = value.IdIndiceFinanciero + '_ID'
					if (value.TipoIndice=="USU")
					{
						columnasIndice.push({field : nombreValor,	title:value.IdIndiceFinanciero,  width: "75px", editable: true});
					}
					else
					{
						columnasIndice.push({field : nombreValor,	title:value.IdIndiceFinanciero,  width: "75px", editable: false});
					}	
					camposIndice[nombreValor] = {type : "number"};
					camposIndice[nombreID] = {type : "string"};
				});
				
				$.each(result.P_VALORES_PARAMETROS_FINANCIEROS,function(key,value){
					var nombreValor = value.IdIndiceFinanciero + '_VALOR'
					var nombreID = value.IdIndiceFinanciero + '_ID'
					var fechaValor = fechaDatos[value.Fecha] || {Dia: value.Dia, AgnoMes : value.AgnoMes, Agno : value.Agno, Fecha: value.Fecha, TipoFecha: value.TipoFecha};
					fechaValor[nombreValor] = value.Valor;
					fechaValor[nombreID] = value.IdIndiceFinanciero;
					fechaDatos[value.Fecha] = fechaValor;
				});
				
				$.each(fechaDatos,function(key,value){
					datosIndice.push(value);
				});

				var dataSource = {
					type : "array",
					definition : {
						data		: datosIndice,
						schema  	: {
							model 	: { fields : camposIndice }
						}
					}
				}
				
				indices.grid.setOptions({
					dataSource : new bcs.DataSource(dataSource),
					columns : columnasIndice
				});
				indices.grillaCargada = true;
				
			}
			bcsComunes.ventanaOverlay(idVentana,false);
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_BUSCAR_PARAMETRO_FINANCIERO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
		}
		);
	}
	
	indices.buscar = function(){
		if(!indices.filtros.periodo) return;
		
		if(datosPorGuardar){
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_PARAMETROS_FINANCIEROS_SIN_GUARDAR'));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					indices.guardar();
				}else{
					consultar();
				}
				
			});
		}else{
			consultar();
		}
	}
	
	
	indices.guardar = function(){
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
	    		p_IdUsuario 		: $scope.SesionIdUsuario,
	    		p_IdModulo 			: 'PLA',
	    		p_SimularOperacion 	: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_ParametrosFinancieros : []
		}
		
		$.each(datosGuardar,function(key,value){
			datos.p_ParametrosFinancieros.push({Fecha : value.Fecha, IdIndiceFinanciero: value.IdIndiceFinanciero, Valor: value.Valor});
		});
		
		
		
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().AdministrarIndices(datos).$promise;
		
		p.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			var id = bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(result)
			},idVentana);
			consultar();
		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_GUARDAR_PARAMETRO_FINANCIERO"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			consultar();
		})
		
	}
	
	//// ABRE VENTANA
	indices.AbreVentana= function(){
		var id = bcsComunes.ventanaAbrir("MantenedorIndicesCorr", {IdTipoMovimiento: 'TRAEGR'} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
		$.when(promesa).then(function(data){        	
        	consultar();
        });
		
	};


    ////MODIFICAR
	indices.modificar= function(){
		var id = bcsComunes.ventanaAbrir("ModificarIndice", {IdTipoMovimiento: 'TRAEGR'} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
		$.when(promesa).then(function(data){        	
        	consultar();
        });
		
		
	};
	
	
	//// ELIMINAR 
	indices.eliminar= function(){
		var id = bcsComunes.ventanaAbrir("EliminarIndice", {IdTipoMovimiento: 'TRAEGR'} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
		$.when(promesa).then(function(data){        	
        	consultar();
        });
	};
	
	
	
	
	$scope.$watch('indices.filtros.tipoIndice',function(newVal,oldVal){
		if(newVal && indices.filtros.periodo){
			if(newVal==='DI') {
				indices.filtros.periodo = util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01');
				indices.configuracionPeriodo = {
					start: "year",
					depth: "year",
					format: "MM-yyyy"
				}
			} else {
				indices.configuracionPeriodo = {
					start: "decade",
					depth: "decade",
					format: "yyyy"
				}
			}
			indices.buscar();
		}
	});
	
	$scope.$watch('indices.filtros.periodo',function(newVal,oldVal){
		if(newVal && indices.filtros.tipoIndice){
			indices.buscar();
		}
	});
});