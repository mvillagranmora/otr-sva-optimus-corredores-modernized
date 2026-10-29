angular.module('ModuloPlataforma').controller('TipoCambioCtrl',function($scope,bcsComunes,$timeout,$compile,bcsServicioPlataforma,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	var tipoCambio = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var datosGuardar = {};
	var datosPorGuardar = false
	tipoCambio.filtros = {
			periodo : util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01')
	};
	
	tipoCambio.grillaCargada = false;
	
	tipoCambio.configuracionPeriodo = {
		start: "year",
		depth: "year",
		format: "MM-yyyy"
	}
	
	tipoCambio.defaultOptions = {
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
					return "grey";
					break;
				case 'FERIADO':
					return "red";
					break;
				}
			}
		},
		columns : [],
		save :function(e) {
			$.each(e.model,function(key,value){
				if(key.includes("_ID")){
					var valor = e.values[e.model[key] + '_VALOR']
					var idParametroFinanciero = e.model[key]
					if(valor){
						if (!datosGuardar[e.model.Fecha +"-"+idParametroFinanciero]) datosGuardar[e.model.Fecha +"-"+idParametroFinanciero] = {};
						datosGuardar[e.model.Fecha +"-"+idParametroFinanciero] = {
							Fecha : e.model.Fecha,
							IdParametroFinanciero : idParametroFinanciero,
							Valor : valor,
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
      		tipoCambio.grid = grid;
      		tipoCambio.grid.refresh();
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
				p_Fecha 	: tipoCambio.filtros.tipoPeriodo == 'DI' ? util.dateSPdate(tipoCambio.filtros.periodo).substring(0,7) : util.dateSPdate(tipoCambio.filtros.periodo).substring(0,4),
				p_TipoPeriodo : tipoCambio.filtros.tipoPeriodo,
				p_TipoParametro : 'TipoCambio'
		}
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().ConsultarParametroFinanciero(datos).$promise;
		p.then(function(result){
			datosGuardar = {};
			datosPorGuardar = false;
			tipoCambio.grillaCargada = false;
			var columnasTipoCambio = [];
			var camposTipoCambio = {};
			var fechaDatos = {};
			var datosTipoCambio = []
			
			if(tipoCambio.filtros.tipoPeriodo == 'DI') {
				columnasTipoCambio.push({field : "Dia",	title:"UI_DIA",  width: "30px"});
				camposTipoCambio["Dia"] = {type : "string"};
			}
			
			if(tipoCambio.filtros.tipoPeriodo == 'ME') {
				columnasTipoCambio.push({field : "AgnoMes",	title:"UI_PERIODO",  width: "30px"});
				camposTipoCambio["AgnoMes"] = {type : "string"};
			}
			
			camposTipoCambio["Fecha"] = {type : "string"};
			camposTipoCambio["TipoFecha"] = {type : "string"};
			if(result.P_PARAMETROS_FINANCIEROS && result.P_VALORES_PARAMETROS_FINANCIEROS){
				$.each(result.P_PARAMETROS_FINANCIEROS,function(key,value){
					var nombreValor = value.IdParametroFinanciero + '_VALOR'
					var nombreID = value.IdParametroFinanciero + '_ID'
					columnasTipoCambio.push({field : nombreValor,	title:value.Nombre + ' (' + value.IdMonedaValor + ')',  width: "150px", editable: true});
					camposTipoCambio[nombreID] = {type : "string"};
					camposTipoCambio[nombreValor] = {type : "number"};
				});
				
				$.each(result.P_VALORES_PARAMETROS_FINANCIEROS,function(key,value){
					var nombreValor = value.IdParametroFinanciero + '_VALOR'
					var nombreID = value.IdParametroFinanciero + '_ID'
					var fechaValor = fechaDatos[value.Fecha] || {Dia: value.Dia, AgnoMes : value.AgnoMes, Fecha: value.Fecha, TipoFecha: value.TipoFecha};
					fechaValor[nombreValor] = value.Valor;
					fechaValor[nombreID] = value.IdParametroFinanciero;
					fechaDatos[value.Fecha] = fechaValor;
				});
				
				$.each(fechaDatos,function(key,value){
					datosTipoCambio.push(value);
				});
				
				var dataSource = {
					type : "array",
					definition : {
						data		: datosTipoCambio,
						schema  	: {
							model 	: { fields : camposTipoCambio }
						}
					}
				}
				
				tipoCambio.grid.setOptions({
					dataSource : new bcs.DataSource(dataSource),
					columns : columnasTipoCambio
				});
				tipoCambio.grillaCargada = true;
				
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
	
	tipoCambio.buscar = function(){
		if(!tipoCambio.filtros.periodo) return;
		if(datosPorGuardar){
			var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_PARAMETROS_FINANCIEROS_SIN_GUARDAR'));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					tipoCambio.guardar();
				}else{
					consultar();
				}
				
			});
		}else{
			consultar();
		}
	}
	
	tipoCambio.guardar = function(){
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
	    		p_IdUsuario 		: $scope.SesionIdUsuario,
	    		p_IdModulo 			: 'PLA',
	    		p_SimularOperacion 	: 'N',
				p_UsaReglasNegocio 	: 'S',
				p_ParametrosFinancieros : []
		}
		
		$.each(datosGuardar,function(key,value){
			datos.p_ParametrosFinancieros.push({Fecha : value.Fecha, IdParametroFinanciero: value.IdParametroFinanciero, Valor: value.Valor});
		});
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var p = bcsServicioPlataforma.Servicios().AdministrarParametroFinanciero(datos).$promise;
		
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
	
	$scope.$watch('tipoCambio.filtros.tipoPeriodo',function(newVal,oldVal){
		if(newVal && tipoCambio.filtros.periodo){
			if(newVal==='DI') {
				tipoCambio.filtros.periodo = util.dateInicioDia($scope.SesionFechaSistema.substring(0,7) + '-01');
				tipoCambio.configuracionPeriodo = {
					start: "year",
					depth: "year",
					format: "MM-yyyy"
				}
			} else {
				tipoCambio.configuracionPeriodo = {
					start: "decade",
					depth: "decade",
					format: "yyyy"
				}
			}
			tipoCambio.buscar();
		}
	});
	
	$scope.$watch('tipoCambio.filtros.periodo',function(newVal,oldVal){
		if(newVal){
			tipoCambio.buscar();
		}
	});	
	
});