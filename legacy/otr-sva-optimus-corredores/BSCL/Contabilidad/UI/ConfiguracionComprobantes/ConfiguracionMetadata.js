angular.module('ModuloContabilidad').controller('ConfiguracionMetadataContabilidadCtrl',function($rootScope,$scope,$resource,$http,bcsComunes,bcsMantenedorEventosGrid,$translate,bcsAlerta,$timeout,bcsServicioContabilidad,OPCIONES_BCSALERTA){
	var idVentana = bcsComunes.ventanaWinId($scope);
	var filtersEventsDisp = {};
	$scope.data = {};
	$scope.inputs = {};
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	filtersEventsDisp.p_IdTipoCentralizacion  = parametrosVentana.comprobante.IdTipoContabilidad;
	filtersEventsDisp.p_Estado	= 'V';
	$scope.inputs.detallado = true;
	$scope.inputs.debe = true;
	$scope.inputs.suma = true;
	var condiciones = [];
	var eventos = [];
	$scope.cuentaContables = [];
	var condicionesCuenta = [];
	
	var dsEventosAsiento = new bcs.DataSource({
		type : "array",
		definition : {								
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsiento()
		}
	});
	
	$scope.data.filtroTipoContabilidad = {
			IdTipoContabilidad : { "field"   : "IdTipoContabilidad","operator": "eq", "value"   :  parametrosVentana.comprobante.IdTipoContabilidad},	
	};
	
	var dsCuentas = new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "CuentaOptimus", dir: "desc" },
							pageSize 			: 50,
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4239",
									dataType: "json"
								}
							},
							schema 				: bcsMantenedorEventosGrid.ModeloConsultarCuentasMetadata()	
						}
	});
	
	var dsEventosDisponibles = new bcs.DataSource({
		type : "call",
		definition : {
			callParameters      : filtersEventsDisp,
			callTransaction 	: "12016",
			callResultSet 		: "p_out_TabEventos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosDisponibles(),
		}
	});
	
	var dsEventosAsociados = new bcs.DataSource({
		type : "array",
		definition : {								
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsiento()
		}
	});
			
	$scope.tablesContabilidad = {
		confCuentas : {
			dataSource			: dsCuentas,
			columns				: bcsMantenedorEventosGrid.columnasCuentasMetadata(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "row",
		    pageable			: false,
		    scrollable			: false
		},
		confEventosDisponibles : {
			dataSource			: dsEventosDisponibles,
			columns				: bcsMantenedorEventosGrid.columnasEventosDisponibles(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 100,
		    selectable 			: "row",
		    pageable			: false,
		    scrollable			: false
		},
		confEventosAsociados : {
			dataSource			: dsEventosAsociados,
			columns				: bcsMantenedorEventosGrid.columnasEventosAsientos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 100,
		    selectable 			: "row",
		    pageable			: false,
		    scrollable			: false
		}
		
	}
	
	$scope.$watch('inputs.debe',function(newVal){
		if(newVal){
			$scope.inputs.haber = false;
		}
	});
	
	$scope.$watch('inputs.haber',function(newVal){
		if(newVal){
			$scope.inputs.debe = false;
		}
	});
	
	$scope.$watch('inputs.detallado',function(newVal){
		if(newVal){
			$scope.inputs.resumido = false;
		}
	});
	
	$scope.$watch('inputs.resumido',function(newVal){
		if(newVal){
			$scope.inputs.detallado = false;
		}
	});
	
	$scope.$watch('inputs.suma',function(newVal){
		if(newVal){
			$scope.inputs.resta = false;
		}
	});
	
	$scope.$watch('inputs.resta',function(newVal){
		if(newVal){
			$scope.inputs.suma = false;
		}
	});
	
	$scope.$watch('data.tipoCentralizacion',function(newval,oldval){
		var tipodato = typeof newval;		 				
		var params = {};
		if (tipodato === "number"  ){	 				
				filtersEventsDisp.p_IdTipoCentralizacion    = newval;
				filtersEventsDisp.p_Estado				    = 'V';			
				dsEventosDisponibles.setCallParameters(filtersEventsDisp);
				dsEventosDisponibles.read();
		}		
	});
	
	$scope.btnGrabarEvento = function(){	
		if ($scope.grid.seleccionadoEventoDisp === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			eventos.push({
				IdTipoEvento						: $scope.grid.seleccionadoEventoDisp.IdTipoEvento
				,IdDefinicionAsiento				: eventos.length
				,Operador							: $scope.inputs.suma ? "+" : "-"
				,Estado								: 'ACT'
				,Nombre								: $scope.grid.seleccionadoEventoDisp.Nombre
			});
			$scope.tablesContabilidad.confEventosAsociados.dataSource.data(eventos);
		};
	};
	
	$scope.btnEliminarEvento = function(){	
		if ($scope.grid.seleccionadoEventoAsoc === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			eventos = eventos.filter(function(item){
				item.IdDefinicionAsiento === $scope.grid.seleccionadoEventoAsoc.IdDefinicionAsiento
			})
			$scope.tablesContabilidad.confEventosAsociados.dataSource.data(eventos);
		};
	};
	
	$scope.btnBuscar = function(){
		if($scope.data.IdConfiguracionMetadata){
			var filtros = [];
			filtros.push({ field: "IdMetadataCuentaContable", operator: "eq", value:$scope.data.IdConfiguracionMetadata } );
			$scope.tablesContabilidad.confCuentas.dataSource.filter(filtros);
			$scope.tablesContabilidad.confCuentas.dataSource.read();
		}
	}
	
	$scope.btnGrabar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);

		var eventosInsert = eventos.map(function(item){
			return {
				IdTipoEvento: item.IdTipoEvento,
				Operador: item.Operador
			}
		});
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo  : 'CON',
			p_IdMetadataCuentaContable : $scope.data.IdConfiguracionMetadata,
			p_Eventos : eventosInsert,
			p_DebeHaber : $scope.inputs.haber ? 'H' : 'D',
			p_Ejercicio : $scope.inputs.ejercicio,
			p_Formato : $scope.inputs.resumido ? 'R' : 'D',
			p_IdTipoContabilidad :	parametrosVentana.comprobante.IdTipoContabilidad
		}
		bcsServicioContabilidad.AgregarCuentasPorMetadata(datos).$promise.then(
		function(result) {
			bcsComunes.ventanaOverlay(idVentana,false);
			if(result){
				var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
					conflictos : util.parseServerResponse(result)}, idVentana);
				if(result.P_OUT_ERRORESCONTADOR === 0) {
					if(id){
						var promesa=bcsComunes.ventanaPromesa(id);                               
					    $.when(promesa).then(function(data){
					    	 $timeout(function() {
					    		 bcsComunes.ventanaCerrar(idVentana);
					    	 },20);
					     }); 
					}else{
						 bcsComunes.ventanaCerrar(idVentana); 
					 }
				}
			}

		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
		});
	}
		
});