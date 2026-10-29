angular.module('ModuloContabilidad').controller('InterfazContableCtrl',
	function($rootScope,$scope,$window,$resource,bcsComunes,bcsMantenedorEventosGrid,$translate,bcsAlerta,OPCIONES_BCSALERTA,$timeout,bcsServicioPlataforma){
		
	$scope.centralizaciones = {};
	$scope.movimientos = {};
	$scope.eventos = {};
	$scope.comprobantes = {};
	$scope.asientos = {};
	$scope.detallesAsientos = {};
	$scope.grid = {};
	$scope.filtrosDetAsientos = {};
	$scope.inputs = {};
	$scope.data = {};
	var filtersCent = {};
	var filtersListCent = {};
	var filterMovCent = {};
	var filterMovDatos = {};
	var filterMovFiltros = {};
	var filterEventos = {};
	var filterEventosFiltros = {};
	var filterEventosDatos = {};
	var filterCompFec = {};
	var filterListComp = {};
	var filterAsientos = {};
	var filterEventAsientos = {};
	var filterConfComp = {};
	var filterDetAsientos = {};
	var filterDetAsientosDatos = {};
	var filterDetAsientosFiltros = {};
	var filterCentralizacion = {};
	var filterMovimientos = {};
	$scope.filterCentralizacionCabeceraCrear = {};
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	bcsComunes.ventanaAutoHeight(idVentana)
	$scope.TipoCentralizacion={filtros : {}};	
	$scope.TipoCentralizacion.filtros = {
		Estado : {"field": "Estado","operator": "eq","value": "V"}							
	};
	
	/*********** Seteo Inicial ********/	
	$scope.select = function(grid)
	{
		$scope.pestana = grid;
		$timeout(function() {			
			var t = $("."+grid).find(".k-grid");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},100);
	};
	
	if ($scope.SesionIdEmpresa)
	{
		var empresaDatasource = new kendo.data.DataSource({
			type: "odata",
			serverFiltering:true,
			transport  : {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",dataType : "json"}},
			filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
		});

        empresaDatasource.fetch().then(function(){
			 if (empresaDatasource.data().length != 1 ) 
			 {
				 console.log("Empresa no existe!");
				 return false;
			 }
			 else 
			 {
				 $scope.$apply(function() {						
					 //$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
					 $scope.Empresa = $scope.SesionEmpresa;
					return true;
				 });
			 }
	    });		
	};
	
	$scope.obtenerPeriodoContable = function(fechaContable){
		var periodo = util.date(fechaContable).format("MMMM - YYYY")
		return periodo;
	};
	
	$scope.obtenerFecha = function(fechaContable){
		var periodo = util.date(fechaContable).format("DD-MM-YYYY")
		return periodo;
	};
	
	$scope.obtenerHora = function(fechaContable){
		var periodo = util.date(fechaContable).format("HH:mm:ss")
		return periodo;
	};
	

	
	
	$scope.filtros = {};	
	$scope.filtros.kngFechaSistema 		= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.FechaSistema			= $scope.obtenerFecha($scope.SesionFechaSistema);
	$scope.filtros.kngFechaContable 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.periodoContable 		= $scope.obtenerPeriodoContable($scope.filtros.kngFechaContable);
	//$scope.filtros.periodoContable 		= $scope.obtenerFecha($scope.SesionFechaSistema);
	
	filtersCent.p_IdEmpresa 				= $scope.SesionIdEmpresa;
	filtersCent.p_Fecha						= $scope.filtros.FechaSistema;
	filtersCent.p_FechaContableInicio		= util.dateSPdate($scope.filtros.kngFechaContable);
	filtersCent.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
	
	filtersListCent.p_IdEmpresa       		= $scope.SesionIdEmpresa;
	filtersListCent.p_CodigoUsuario			= $scope.SesionIdUsuario;
	
	filterCompFec.p_IdEmpresa 				= $scope.SesionIdEmpresa;
	filterCompFec.p_FechaContableInicial	= util.dateSPdate($scope.filtros.kngFechaContable);
	filterCompFec.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
	/************ Fin Seteo inicial *********/
	




	/************ Grillas Centralizaciones ***********/
	
	var dsCentralizacionesProcesadas = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filtersCent,
			callTransaction 	: "12001",
			callResultSet 		: "p_out_TabCentralizaciones",
			schema : bcsMantenedorEventosGrid.ModeloConsultarCentralizaciones(),
		}
	});
	
	var dsListaCentralizaciones = new bcs.DataSource({
		type : "call",
		definition : {
			callParameters      : filtersListCent,
			callTransaction 	: "12002",
			callResultSet 		: "p_out_TabCentralizacionesTipo",
			schema : bcsMantenedorEventosGrid.ModeloConsultarCentralizacionesTipo(),
		}
	});
	
	$scope.centralizaciones.confCentralizacionesProcesadas = {
			dataSource			: dsCentralizacionesProcesadas,
			columns				: bcsMantenedorEventosGrid.columnasCentralizacionesProcesadas(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 530,
		    selectable 			: "row",
		    scrollable			: false,
		    sortable			: true
		}; 
	
	$scope.centralizaciones.confListaCentralizaciones = {
			dataSource			: dsListaCentralizaciones,
			columns				: bcsMantenedorEventosGrid.columnasListaCentralizaciones(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 530,
		    selectable 			: "multiple row",
		    scrollable			: true
		};
	/************** Fin grillas Centralizaciones ***********/
	
	/************** Grillas Movimientos *******************/
	
	var dsMovimientosCentralizacion = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterMovCent,
			callTransaction 	: "12003",
			callResultSet 		: "p_out_TabMovimientos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosCentralizacion(),
		}
	});
	
	var dsMovimientosDatos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterMovDatos,
			callTransaction 	: "12004",
			callResultSet 		: "p_out_TabMovimientosDatos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosDatos(),
		}
	});
	
	var dsMovimientosFiltros = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterMovFiltros,
			callTransaction 	: "12005",
			callResultSet 		: "p_out_TabFiltrosTipoDatos",
			editable            : true,
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltros(),
		}
	});
	
	var dsMovimientosFiltrosTabla = new bcs.DataSource({
		type : "call",
		definition : {
			schema : bcsMantenedorEventosGrid.ModeloConsultarTablaMovimientosFiltros(),
		}
	});
	
	$scope.movimientos.confMovimientosCentralizacion = {
			dataSource			: dsMovimientosCentralizacion,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosCentralizacion(),		
 			filterable			: true,
 			autoBind			: false,  
			height				: 511,
		    selectable 			: "multiple row",
		    pageable			: true, // genera paginado 
		    scrollable			: false,
			pageSize        : 200,  //cantidad de lineas por pagina 
			excel 			: { allPages: true }, // exporta de todas las paginas
			dataBound			: function(e) {},
			gridCreated 		: function(grid) {
		    	$scope.grillaAll = grid;
		    	
		    	if ($scope.SesionLocale === 'PE')
		    	{
		    		grid.showColumn("CompraVenta");
		    		grid.showColumn("Concepto");
		    		grid.showColumn("CondicionLiquidacion");
					grid.showColumn("MonedaLiquidacion");
					grid.showColumn("Operacion");
					grid.showColumn("Producto");
					grid.showColumn("Relacion");
		    	}
		    }
		};
	
	$scope.movimientos.confMovimientosDatos = {
			dataSource			: dsMovimientosDatos,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosDatos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 230,
		    selectable 			: "multiple row",
		};
	
	$scope.movimientos.confMovimientosFiltros = {
			dataSource			: dsMovimientosFiltros,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosFiltros(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 230,
		    selectable 			: "row",
		};
	/*************** Fin grillas Movimientos *****************/
	
	/*************** Grillas Eventos *********************/
	
	var dsEventos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterEventos,
			callTransaction 	: "12007",
			callResultSet 		: "p_out_TabEventos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventos(),
		}
	});
	
	var dsEventosFiltros = new bcs.DataSource({
		type : "call",
		definition : {						
			callParameters      : filterEventosFiltros,
			callTransaction 	: "12005",
			callResultSet 		: "p_out_TabFiltrosTipoDatos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltros(),
		}
	});
	var dsEventosDatos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterEventosDatos,
			callTransaction 	: "12004",
			callResultSet 		: "p_out_TabMovimientosDatos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosDatos(),
		}
	});
	
	$scope.eventos.confEventos = {
			dataSource			: dsEventos,
			columns				: bcsMantenedorEventosGrid.columnasEventos(),		
 			filterable			: true,
 			autoBind			: false,  //comentar
			height				: 511,
		    selectable 			: "multiple row",
		    pageable			: true,
		    scrollable			: false,
		    dataBound			: function(e) {
				//console.log("eventos dataBound length: " + e.sender.dataSource.data().length);
		    }
		};
	
	$scope.eventos.confEventosDatos = {
			dataSource			: dsEventosDatos,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosDatos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 230,
		    selectable 			: "multiple row",
		};
	
	$scope.eventos.confEventosFiltros = {
			dataSource			: dsEventosFiltros,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosFiltros(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 230,
		    selectable 			: "row"
		};
		
	/*************** Fin grillas Eventos ********************/
	
	/*************** Grillas Pre Comprobantes *********************/
	
	var dsComprobantesFecha = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterCompFec,
			callTransaction 	: "12008",
			callResultSet 		: "p_out_TabPreComprobantes",
			schema : bcsMantenedorEventosGrid.ModeloConsultarComprobantesFecha(),
		}
	});
		
	var dsListaComprobantes = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterListComp,
			callTransaction 	: "12009",
			callResultSet 		: "p_out_TabContabilidadTipo",
			schema : bcsMantenedorEventosGrid.ModeloConsultarListaComprobantes(),
		}
	});
	
	//GRILLA PRINCIPAL
	let grillaPrecomprobantes;
	
	$scope.comprobantes.confComprobantesFecha = {
		dataSource			: dsComprobantesFecha,
		columns				: bcsMantenedorEventosGrid.columnasComprobantesFecha(),		
		filterable			: true,
		autoBind			: false,  //comentar
		height				: 530,
		selectable 			: "multiple row",
		pageable			: true,
		scrollable			: false,
		pageSize            : 200,
		rowColorBackground : function(row) {
			if (row.Estado == "C"){
				return "light-green-font";
			}else if (row.Estado == "A"){
				return "red-font";
			}else if (row.Estado == "E"){
				return "brown-font";
			}else if (row.Estado == "D"){
				return "orange-font";
			}
			return "";
		},
		boldRow: function() {
			console.log($scope);
			return "bold";
		},

		sortable			: true,
		gridCreated : function(grid) {
	    	grillaPrecomprobantes = grid;
	    	bcsComunes.tootipGlosa('toolConsultarPreComprobanteEnvioContabilidad', array)
	    },
		dataBound: function() {           
	    	$("a.verTareaProgramada").on("click",function(e){
	            if (grillaPrecomprobantes) {
					grillaPrecomprobantes.clearSelection();
	                let fila = $(e.target).closest("tr");
	                grillaPrecomprobantes.select(fila);
	                let data = $("#gridComprobantesFecha").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent());
					let idPrograma = data.IdPrograma;
					
					bcsComunes.ventanaOverlay(idVentana,true);
					
					let ventanaConsultaProgramas = $window["Win_MantenedorPrograma"];
					if (ventanaConsultaProgramas && !ventanaConsultaProgramas.closed) {
						ventanaConsultaProgramas.focus();
						$rootScope.$broadcast('eventoVentanaEstaAbierta', idPrograma);
					}
					
					let ventanaTareas = bcsComunes.ventanaAbrir("MantenedorPrograma", {IdPrograma: idPrograma});
					
					let promesa = bcsComunes.ventanaPromesa(ventanaTareas);   
            
           			promesa.then(function(){
                		bcsComunes.ventanaOverlay(idVentana,false);
            		}); 
	            }
	        });
    	}
	};


	$scope.comprobantes.confListaComprobantes = {
			dataSource			: dsListaComprobantes,
			columns				: bcsMantenedorEventosGrid.columnasListaComprobantes(),		
 			filterable			: true,
 			autoBind			: false,  //comentar
			height				: 530,
		    selectable 			: "multiple row",
		    pageable			: false,
		    scrollable			: false
		};
		
	/*************** Fin grillas Pre Comprobantes ********************/
	
	/*************** Grillas Asientos *********************/
	
	var dsAsientos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterAsientos,
			callTransaction 	: "12010",
			callResultSet 		: "p_out_TabPreComprobanteAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarAsientos(), //CAMBIO AQUI Asientos por Eventos
		}
	});
	
	var dsEventosAsientos = new bcs.DataSource({
		type : "call",
		definition : {						
			callParameters      : filterEventAsientos,
			callTransaction 	: "12063",
			callResultSet 		: "p_out_TabEventoAsientoAsociado",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsientos(),
		}
	});
	
	var dsConfiguracionComprobantes = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterConfComp,
			callTransaction 	: "12018",
			callResultSet 		: "p_out_TabCondiciones",
			schema : bcsMantenedorEventosGrid.ModeloConsultarConfiguracionComprobante(),
		}
	});
	
	$scope.asientos.confAsientos = {
			dataSource			: dsAsientos,
			columns				: bcsMantenedorEventosGrid.columnasAsientos(),		
 			filterable			: true,
 			autoBind			: false,  //comentar
			height				: 507,
		    selectable 			: "multiple row",
		    pageable			: true,
		    scrollable			: true,
		    sortable			: true,
			pageSize            : 200
		};
	
	$scope.asientos.confEventosAsientos = {
			dataSource			: dsEventosAsientos,
			columns				: bcsMantenedorEventosGrid.columnasEventosAsientos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 240,
		    selectable 			: "row",
		    pageable			: true,
			pageSize            : 200
		};
	
	$scope.asientos.confComprobanteConfiguracion = {
			dataSource			: dsConfiguracionComprobantes,
			columns				: bcsMantenedorEventosGrid.columnasConfiguracionComprobante(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 240,
		    selectable 			: "multiple row",
		    pageable			: {
				pageSize		: true,
				buttonCount		: 5
			},
			pageSize            : 200
		};
	
	/*************** Fin grillas Asientos ********************/
	
	/*************** Grillas Detalles Asientos *********************/
	
	var dsDetallesAsientos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterDetAsientos,
			callTransaction 	: "12014",
			callResultSet 		: "p_out_TabDetallePreComprobanteAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarDetallesAsientos(),
		}
	});
	
	var dsDetallesAsientosFiltros = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterDetAsientosFiltros,
			callTransaction 	: "12005",
			callResultSet 		: "p_out_TabFiltrosTipoDatos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltros(),
		}
	});
	
	var dsDetallesAsientosDatos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterDetAsientosDatos,
			callTransaction 	: "12004",
			callResultSet 		: "p_out_TabMovimientosDatos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosDatos(),
		}
	});
	
	$scope.detallesAsientos.confDetallesAsientos = {
			dataSource			: dsDetallesAsientos,
			columns				: bcsMantenedorEventosGrid.columnasDetallesAsientos(),		
 			filterable			: true,
 			autoBind			: false,  //comentar
			height				: 480,
		    selectable 			: "multiple row",
		    pageable			: true,
		    scrollable			: false,
			pageSize            : 200
		};
	
	$scope.detallesAsientos.confDetallesAsientosDatos = {
			dataSource			: dsDetallesAsientosDatos,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosDatos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 215,
		    selectable 			: "row",
		    pageable			: true,
			pageSize            : 200
		};
	
	$scope.detallesAsientos.confDetallesAsientosFiltros = {
			dataSource			: dsDetallesAsientosFiltros,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosFiltros(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 215,
		    selectable 			: "row",
		    pageable			: true,
			pageSize            : 200
		};
		
	/*************** Fin grillas Detalles Asientos ********************/
	
	/********** Botonera cabecera interfaz ***********/
	
	$scope.btnBuscar = function() {
		
		$scope.filtros.periodoContable 			= $scope.obtenerPeriodoContable($scope.filtros.kngFechaContable);
		dsCentralizacionesProcesadas.read();
		dsListaCentralizaciones.read();
		dsComprobantesFecha.read();
		dsListaComprobantes.read();
		filtersCent.p_IdEmpresa 				= $scope.SesionIdEmpresa;	
		filtersCent.p_Fecha						= util.dateSPdate($scope.filtros.kngFechaSistema);
		filtersCent.p_FechaContableInicio		= util.dateSPdate($scope.filtros.kngFechaContable);
		filtersCent.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
		filtersCent.p_Estado					= '';
		filtersCent.p_IdTipoCentralizacion		= 0;
		dsCentralizacionesProcesadas.setCallParameters(filtersCent);
		
		filtersListCent.p_IdEmpresa       		= $scope.SesionIdEmpresa;
		filtersListCent.p_CodigoUsuario			= $scope.SesionIdUsuario;
		filtersListCent.p_Estado				= 'V';
		dsListaCentralizaciones.setCallParameters(filtersListCent);
		
		filterCompFec.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filterCompFec.p_FechaContableInicial	= util.dateSPdate($scope.filtros.kngFechaContable);
		filterCompFec.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
		dsComprobantesFecha.setCallParameters(filterCompFec);
		
		filterListComp.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filterListComp.p_Estado					= 'V';
		dsListaComprobantes.setCallParameters(filterListComp);
		$scope.limpiarSeleccionGrillas();
		$scope.limpiarGrillasSecundarias();
	};
	
	
	$scope.btnLimpiarFiltros = function() {
		
		$scope.filtros.FechaSistema					= $scope.obtenerFecha($scope.SesionFechaSistema);
		$scope.filtros.kngFechaContable 			= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.periodoContable 				= $scope.obtenerPeriodoContable($scope.filtros.kngFechaContable);
		
		$scope.filtros.tipoMovimiento 				= '';
		$scope.filtros.fechaContable 				= '';
		$scope.filtros.horaProceso 					= '';
		$scope.filtros.nombreEstadoCentralizacion   = '';
		
		$scope.filtros.comprobante 					= '';
		$scope.filtros.debe 						= '';
		$scope.filtros.haber 						= '';
		$scope.filtros.estado						= '';
		$scope.filtros.nombreEstado					= '';
		
		$scope.filtrosDetAsientos.comprobante		= '';
		$scope.filtrosDetAsientos.nombreCuenta		= '';
		$scope.filtrosDetAsientos.linea				= '';
		$scope.filtrosDetAsientos.debe				= '';
		$scope.filtrosDetAsientos.cuentaContable	= '';
		$scope.filtrosDetAsientos.haber				= '';
		$scope.filtrosDetAsientos.estado			= '';
		$scope.filtrosDetAsientos.nombreEstado		= '';
		
		dsCentralizacionesProcesadas.removeAll();
		dsListaCentralizaciones.removeAll();
		$scope.limpiarGrillasSecundarias();
		dsListaComprobantes.removeAll();
		dsComprobantesFecha.removeAll();
		
		$scope.movFiltrosInput			= false;
		$scope.movFiltrosCombo			= false;
		$scope.eventosFiltrosInput		= false;
		$scope.eventosFiltrosCombo		= false;
		$scope.detAsientosFiltrosInput	= false;
		$scope.detAsientosFiltrosCombo	= false;
		$scope.limpiarSeleccionGrillas();
		
		filtersCent.p_FechaContableInicio		= undefined;
		filtersCent.p_FechaContableFinal		= undefined;
		$scope.btnLimpiarFiltrosProcesados();
	};
	
	$scope.btnLimpiarFiltrosProcesados = function() {
		$scope.filtros.IdEstadoCent					= 0;
		$scope.filtros.IdTipoCentralizacion			= 0;
	}

	$scope.limpiarSeleccionGrillas = function() {
		$scope.grid.seleccionCentralizacion = undefined;
		$scope.grid.seleccionMovimientos	= undefined;
		$scope.grid.seleccionEventos		= undefined;
		$scope.grid.seleccionadoComprobante	= undefined;
		$scope.grid.seleccionadoAsientos	= undefined;
		$scope.grid.seleccionadoDetAsiento	= undefined;
		$scope.grid.seleccionListaCentralizacion = undefined;
		$scope.grid.seleccionadoListaComprobante = undefined;
	}

	$scope.limpiarGrillasSecundarias = function() {
		dsMovimientosCentralizacion.removeAll();
		dsMovimientosDatos.removeAll();
		dsMovimientosFiltros.removeAll();
		dsEventos.removeAll();
		dsEventosFiltros.removeAll();
		dsEventosDatos.removeAll();					
		dsEventosAsientos.removeAll();	
		dsConfiguracionComprobantes.removeAll();	
		dsAsientos.removeAll();
		dsDetallesAsientos.removeAll();		
		dsDetallesAsientosDatos.removeAll();
		dsDetallesAsientosFiltros.removeAll();
	}
	
	$scope.btnAnularCentralizacion = function() {
		
		if ($scope.grid.seleccionCentralizacion === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		filterCentralizacion.p_IdEmpresa        = 	$scope.SesionIdEmpresa;
		filterCentralizacion.p_IdUsuario     	=	$scope.SesionIdUsuario;
		filterCentralizacion.p_IdModulo       	= 	"CON"; 
		filterCentralizacion.p_IdCentralizacion = 	$scope.grid.seleccionCentralizacion.IdCentralizacion;	
		filterCentralizacion.p_Ejercicio		=	util.date($scope.filtros.kngFechaContable).format("YYYY");
		
		$scope.call("12043",filterCentralizacion);	
		}
	}
	
	$scope.btnConsultarCentralizacion = function() {
		var datos = {};
		
		if ($scope.grid.seleccionCentralizacion === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
			datos.IdEmpresa        = 	$scope.SesionIdEmpresa;
			datos.IdModulo       	= 	"CON"; 
			datos.IdCentralizacion = 	$scope.grid.seleccionCentralizacion.IdCentralizacion;
			
		var id1 = bcsComunes.ventanaAbrir("ConsultarCentralizacionModal",datos,false, false, idVentana); 			
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){
        }); 
		}
	}
	
	
	$scope.btnConsultarPreComprobantes
	= function() {
		var datos = {};
		
		if ($scope.grid.seleccionadoComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
			datos.IdEmpresa        = 	$scope.SesionIdEmpresa;
			datos.IdModulo       	= 	"CON"; 
			datos.IdPreComprobante = 	$scope.grid.seleccionadoComprobante[0].IdPreComprobante;
			
		var id1 = bcsComunes.ventanaAbrir("ConsultarPreComprobanteModal",datos,false, false, idVentana); 			
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){
        }); 
		}
	}
	
	$scope.btnConsultarPreComprobanteEnvioContabilidad
	= function() {
		var datos = {};
		
		if ($scope.grid.seleccionadoComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
			datos.IdEmpresa          = 	$scope.SesionIdEmpresa;
			datos.IdModulo       	 = 	"CON"; 
			datos.IdPreComprobante 	 = 	$scope.grid.seleccionadoComprobante[0].IdPreComprobante;
			datos.Estado			 = 	$scope.grid.seleccionadoComprobante[0].Estado;
			
		var id1 = bcsComunes.ventanaAbrir("ConsultarPreComprobanteEnvioContabilidad",datos,false, false, idVentana); 			
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){
        }); 
		}
	}
	
	$scope.btnProcesarCentralizacion = function() {
		filterCentralizacion = {};
		p_modal = {};
		if ($scope.grid.seleccionListaCentralizacion === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{		
		 
			if ($scope.grid.seleccionListaCentralizacion.length > 0)
			{				
				filterCentralizacion.p_IdEmpresa        	= 	$scope.SesionIdEmpresa;
				filterCentralizacion.p_IdUsuario     		=	$scope.SesionIdUsuario;
				filterCentralizacion.p_IdModulo       		= 	"CON";
				filterCentralizacion.p_FechaContable		=	util.dateSPdate($scope.filtros.kngFechaContable);				
				filterCentralizacion.p_Ejercicio  			=  	util.date($scope.filtros.kngFechaContable).format("YYYY");
				filterCentralizacion.p_SimulaOperacion		= 	'N';
				filterCentralizacion.p_in_TiposCentralizacion = [];			 
				$.each($scope.grid.seleccionListaCentralizacion,function(key,value){

					filterCentralizacion.p_in_TiposCentralizacion.push
					({     
						IdTipoCentralizacion 				:  value.IdCentralizacionTipo,
				    });
				});						
				if($scope.verificarRequiereFechaConsulta($scope.grid.seleccionListaCentralizacion)){
					p_Modal = {p_FechaContable : filterCentralizacion.p_FechaContable};
					var id1 = bcsComunes.ventanaAbrir("ProcesarFechaModal",p_Modal,false, false, idVentana); 			
					var promesa=bcsComunes.ventanaPromesa(id1);
					promesa.then(function(data){
						if(Object.keys(data).length > 0){
							if ($scope.filtros.kngFechaContable < util.dateDS(data,"FechaSistema","CALL")) {
								bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_FECHA_CONTABLE_LIMITE'))
								return;
							}
							filterCentralizacion.p_FechaConsulta = data;
							$scope.call("12065",filterCentralizacion);
						}
					});
				}else{
					$scope.call("12065",filterCentralizacion);
				}
			
			}
		}
	}

	$scope.verificarRequiereFechaConsulta = function(data) {
		return data.some(function(item) {
			return item.RequiereFechaConsulta === 'S';
		});
	};
  
   $scope.btnProcesarCentralizacionTodos	= function() {			
		
		var entradaPrograma 		= {};
		entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
		entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
		entradaPrograma.p_IdModulo    		= 'CON';
		entradaPrograma.p_IdAgenda    		= 0;
		entradaPrograma.p_IdTipoTarea   	= 'GENERAR_CENTRALIZACION_MASIVA';
		entradaPrograma.p_Segundos      	= 0;
		entradaPrograma.p_CreaParam     	= 'S';

		var p_in_TiposCentralizacion 		= [];
		var idsTipoCentralizacion           = [];
		var data = $scope.grid.Apic.dataSource.data();
		
		for (var i = 0;i<data.length;i++) {
			idsTipoCentralizacion.push(data[i].IdCentralizacionTipo);
		}
		
		p_in_TiposCentralizacion = idsTipoCentralizacion.join(";");
	
		entradaPrograma.p_TablaParametros =
		[
		   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
		   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
		   {NombreParametro	: 'IdModulo', 	  		Valor: 'CON'},			   			   
		   {NombreParametro	: 'FechaContable', 	  	Valor: util.dateSPdate($scope.filtros.kngFechaContable)},
		   {NombreParametro	: 'Ejercicio', 	  		Valor: util.date($scope.filtros.kngFechaContable).format("YYYY")},
		   {NombreParametro	: 'IdsTipoCentralizacion', 	  	Valor: p_in_TiposCentralizacion}    
		];
					
		if($scope.verificarRequiereFechaConsulta(data)){
			p_Modal = {p_FechaContable : util.dateSPdate($scope.filtros.kngFechaContable)};
			var id1 = bcsComunes.ventanaAbrir("ProcesarFechaModal",p_Modal,false, false, idVentana); 			
			var promesa=bcsComunes.ventanaPromesa(id1);
			promesa.then(function(data){
				if ($scope.filtros.kngFechaContable < util.dateDS(data,"FechaSistema","CALL")) {
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_FECHA_CONTABLE_LIMITE'))
					return;
				}
				if(Object.keys(data).length > 0){
					
					entradaPrograma.p_TablaParametros.push({
    					NombreParametro: 'fechaConsulta', Valor: data
					});
					
					console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
								
					bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
						function(respuesta){
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
								Programa = respuesta.P_OUT_IDPROGRAMA;					
								if (Programa.length > 0)
								    idPrograma = Programa[0].IdPrograma;								
										
								bcsComunes.ventanaMsg({
									mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
								},$scope.winID);						
							}
							else
							{						
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
								},$scope.winID);
							}
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);					
						}
					);							
				}
			});
		}else{
			
			console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
						
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						Programa = respuesta.P_OUT_IDPROGRAMA;
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;
								
						bcsComunes.ventanaMsg({
							mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"
						},$scope.winID);
					}
					else
					{						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")				
						},$scope.winID);
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
			);			
		}	}

	$scope.btnAnularMovimiento = function() {
		
		if ($scope.grid.seleccionMovimientos === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		filterMovimientos.p_IdEmpresa      	    = 	$scope.SesionIdEmpresa;
		filterMovimientos.p_IdUsuario     		=	$scope.SesionIdUsuario;
		filterMovimientos.p_IdModulo      	 	= 	"CON"; 
		filterMovimientos.p_IdMovimiento		= 	$scope.grid.seleccionMovimientos[0].IdMovimiento;	
 		
		$scope.call("12044",filterMovimientos);	
		
		}
	}
	
	$scope.btnAnularEventos = function() {
	
		if ($scope.grid.seleccionEventos === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		filterCentralizacion.p_IdEmpresa        = 	$scope.SesionIdEmpresa;
		filterCentralizacion.p_IdUsuario     	=	$scope.SesionIdUsuario;
		filterCentralizacion.p_IdModulo       	= 	"CON"; 
		filterCentralizacion.p_IdEvento			= 	$scope.grid.seleccionEventos[0].IdEvento;	
		
		$scope.call("12045",filterCentralizacion);	
		}
	}
	
	$scope.btnProcesarPreComprobantes	= function() {			
			
		var filterComprobantes = {};
		
		if ($scope.grid.seleccionadoListaComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		var filterComprobantes = {};
		filterComprobantes.p_IdEmpresa       	 		= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario     				=	$scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo       	 		= 	"CON"; 
		//filterComprobantes.p_IdTipoContabilidad 		=	$scope.grid.seleccionadoListaComprobante.IdTipoContabilidad;
		
		filterComprobantes.p_TipoContabilidad 		= [];			 
		$.each($scope.grid.seleccionadoListaComprobante,function(key,value){
			filterComprobantes.p_TipoContabilidad.push
			({     
				IdTipoContabilidad  					:  value.IdTipoContabilidad,
		    });
		});		
		
		filterComprobantes.p_FechaContable 				=	util.dateSPdate($scope.filtros.kngFechaContable);
		filterComprobantes.p_Ejercicio					=	util.date($scope.filtros.kngFechaContable).format("YYYY");	 		
		
		$scope.call("12061",filterComprobantes);	 
		
		}
	}

	$scope.btnProcesarPreComprobantesTodos	= function() {			
		var filterComprobantes = {};
		filterComprobantes.p_IdEmpresa       	 		= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario     				=	$scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo       	 		= 	"CON"; 

		filterComprobantes.p_TipoContabilidad 		= [];
		var data = $scope.grid.Api.dataSource.data();
		
		for (var i = 0;i<data.length;i++) {
			var dat = data[i];
			filterComprobantes.p_TipoContabilidad.push
			({     
				IdTipoContabilidad  					:  dat.IdTipoContabilidad,
		    });
		}

		filterComprobantes.p_FechaContable 				=	util.dateSPdate($scope.filtros.kngFechaContable);
		filterComprobantes.p_Ejercicio					=	util.date($scope.filtros.kngFechaContable).format("YYYY");	 		
		
		$scope.call("12061",filterComprobantes);
	}
		
	$scope.btnReversarPreComprobantes	= function() {		
		
		var filterComprobantes = {};		
		
		if ($scope.grid.seleccionadoComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		filterComprobantes.p_IdEmpresa				= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdPreComprobanteAnt	=	$scope.grid.seleccionadoComprobante[0].IdPreComprobante;
		filterComprobantes.p_FechaContable			=	util.dateSPdate($scope.grid.seleccionadoComprobante[0].FechaContable);
		filterComprobantes.p_CodigoUsuario			=	$scope.grid.seleccionadoComprobante[0].CodigoUsuario;
		filterComprobantes.p_IdPreComprobante		=	0;
	
		$scope.call("12062",filterComprobantes);	
		}
	}
	$scope.btnEnviarPreComprobantesTodos	= function() {
		
		var precomprobantes = $scope.grid.ApiP.dataSource.data();
		var contadorD = 0;
		var precmbteEnviar = "";
		
		for (var i = 0;i<precomprobantes.length;i++) {
		var dat = precomprobantes[i];	
			if(precomprobantes[i].Estado === 'V' || precomprobantes[i].Estado === 'S' || precomprobantes[i].Estado === 'D' || precomprobantes[i].Estado === 'E'){
				precmbteEnviar+= precomprobantes[i].IdPreComprobante+ ";";
				if (precomprobantes[i].Estado === 'D'){
					contadorD++;
				}
			}
		}
		
		if (precomprobantes.length === 0) {
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('UI_NO_SE_SELECCIONARON_REGISTROS_VALIDOS_PARA_CONTABILIZAR'));
		}
		
		var filterComprobantes = {};
			
		filterComprobantes.p_IdEmpresa				= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario				=   $scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo				=   'CON';
		filterComprobantes.p_UsaReglasNegocio		=   'N';
		filterComprobantes.p_SecCmbte				=	precmbteEnviar;
		filterComprobantes.p_Destino				=	'B';
		
		

		if (contadorD === 0){
			enviarPrecomprobante(filterComprobantes);
		}
		
		if (contadorD === 1 && precomprobantes.length === 1){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_SELECCION_COMPROBANTE_DESCUADRADO'), idVentana);
				t.then(function(result){
    				if (result === OPCIONES_BCSALERTA.SI) {
						enviarPrecomprobante(filterComprobantes);
   			 }
		});
		};

		if (contadorD >= 1 && precomprobantes.length > 1){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_SELECCION_COMPROBANTES_DESCUADRADOS'), idVentana);
				t.then(function(result){
    				if (result === OPCIONES_BCSALERTA.SI) {
						enviarPrecomprobante(filterComprobantes);
   			 }
		});
		}
		
	}	
	
	$scope.btnEnviarPreComprobantes	= function() {
		
		var precomprobantes = $scope.grid.seleccionadoComprobante;
		var precmbteEnviar = "";
		var contadorD = 0;
		
		var j = 0;
		
		for (var i = 0;i<precomprobantes.length;i++) {
			
			if(precomprobantes[i].Estado === 'V' || precomprobantes[i].Estado === 'S' || precomprobantes[i].Estado === 'D' || precomprobantes[i].Estado === 'E'){
				precmbteEnviar+= precomprobantes[i].IdPreComprobante+ ";";
				j = ++j;
				if(precomprobantes[i].Estado === 'D'){
					contadorD++;
				}
			}
		}
	//	if (precomprobantes.length === 0) {

		if (j == 0) {
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('UI_NO_SE_SELECCIONARON_REGISTROS_VALIDOS_PARA_CONTABILIZAR'));
			return;
		}else{
				if (i != j ) {
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('UI_EXISTEN REGISTROS_NO_VALIDOS_QUE_NO_SERAN_CONTABILIZADOS'));
				}
		};
		
		var filterComprobantes = {};
			
		filterComprobantes.p_IdEmpresa				= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario				=   $scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo				=   'CON';
		filterComprobantes.p_UsaReglasNegocio		=   'N';
		filterComprobantes.p_SecCmbte				=	precmbteEnviar;
		filterComprobantes.p_Destino				=	'B';

		if (contadorD === 0){
			enviarPrecomprobante(filterComprobantes);
		}
		
		if (contadorD === 1 && precomprobantes.length === 1){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_SELECCION_COMPROBANTE_DESCUADRADO'), idVentana);
				t.then(function(result){
    				if (result === OPCIONES_BCSALERTA.SI) {
						enviarPrecomprobante(filterComprobantes);
   			 }
		});
		};

		if (contadorD >= 1 && precomprobantes.length > 1){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant('UI_SELECCION_COMPROBANTES_DESCUADRADOS'), idVentana);
				t.then(function(result){
    				if (result === OPCIONES_BCSALERTA.SI) {
						enviarPrecomprobante(filterComprobantes);
   			 }
		});
		}


		
		
		
	}
	
	var enviarPrecomprobante = function(parameters) {
		
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4145";
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		
		var post = r.Filtros(parameters);
		
		bcsComunes.ventanaOverlay(idVentana, true);
		post.$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({	
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
					
		}).catch(function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({	
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
    	});
		
	}
	
	$scope.btnAnularPreComprobantes	= function() {	
		
		var filterComprobantes = {};
		
		if ($scope.grid.seleccionadoComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
		
		filterComprobantes.p_IdEmpresa       	 		= 	$scope.SesionIdEmpresa;
		filterComprobantes.p_IdUsuario     				=	$scope.SesionIdUsuario;
		filterComprobantes.p_IdModulo       	 		= 	"CON"; 
		filterComprobantes.p_UsaReglasNegocio			=	"N";
		filterComprobantes.p_IdPreComprobante			=	$scope.grid.seleccionadoComprobante[0].IdPreComprobante;
		filterComprobantes.p_IdPreComprobanteAsiento	=	0;
		filterComprobantes.p_IdEvento			 		=	0;
		filterComprobantes.p_Origen				 		=	"PR";	
		
		filterComprobantes.p_PreComprobantes 		= [];			 
		$.each($scope.grid.seleccionadoComprobante,function(key,value){
			filterComprobantes.p_PreComprobantes.push
			({     
				IdPreComprobante  					:  value.IdPreComprobante,
		    });
		});		
		
		$scope.call("12056",filterComprobantes);	 
		
		}
	}
 
	/********** Fin botonera ***************/
	 
	/********** Eventos grillas ************/
	
	$scope.cargarMovimientosEventos = function() {
		filterMovCent.p_IdEmpresa          		= $scope.SesionIdEmpresa;
		filterMovCent.p_IdCentralizacion 		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
		filterMovCent.p_DocumentoIdentidad 		= '        0-0';
		filterMovCent.p_FechaMovInicio 			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterMovCent.p_FechaMovFinal			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterMovCent.p_DatosFiltro				= [];
		
		dsMovimientosCentralizacion.setCallParameters(filterMovCent).then(function() { 
			if(dsMovimientosCentralizacion.data().length === 0)
			{
				bcsComunes.ventanaMsg({
	                mensaje:$translate.instant("UI_NO_MOVIMIENTOS_CENTRALIZACION"),
	                conflictos:null
	              }, idVentana); 
			}
		});

		
		filterEventos.p_IdEmpresa          		= $scope.SesionIdEmpresa;
		filterEventos.p_FechaContableInicial 	= util.dateSPdate($scope.filtros.kngFechaContable);
		filterEventos.p_FechaContableFinal 		= util.dateSPdate($scope.filtros.kngFechaContable);
		filterEventos.p_IdCentralizacion		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
		//filterEventos.p_RutMovimiento 			= '        0-0';
		filterEventos.p_DatosFiltro				= [];
		
		dsEventos.setCallParameters(filterEventos);
		
		filterMovFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterMovFiltros.p_IdTipoMovimiento		=	'';
		filterMovFiltros.p_Estado				=	'V';
		dsMovimientosFiltros.setCallParameters(filterMovFiltros);
		
		filterEventosFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterEventosFiltros.p_IdTipoMovimiento		=	'';
		filterEventosFiltros.p_Estado				=   'V';
		dsEventosFiltros.setCallParameters(filterEventosFiltros);
		
		$scope.filtros.tipoMovimiento 			= $scope.grid.seleccionCentralizacion.NombreTipoContralizacion;		
		$scope.filtros.fechaContable 			= $scope.obtenerFecha($scope.grid.seleccionCentralizacion.FechaContable);		
		$scope.filtros.horaProceso 				= $scope.obtenerHora($scope.grid.seleccionCentralizacion.HoraProceso);
		$scope.filtros.nombreEstadoCentralizacion =  $scope.grid.seleccionCentralizacion.NombreEstado;
	};
	
	$scope.cargarMovimientosDatosFiltros = function() {
		
		filterMovDatos.p_IdMovimiento 			= $scope.grid.seleccionMovimientos[0].IdMovimiento;
		dsMovimientosDatos.setCallParameters(filterMovDatos);
		
		filterMovFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterMovFiltros.p_IdTipoMovimiento		=	$scope.grid.seleccionMovimientos[0].IdTipoMovimiento.replace(/ /g, '');
		filterMovFiltros.p_Estado				=	'V';
		dsMovimientosFiltros.setCallParameters(filterMovFiltros);
		
		
	};
	
	$scope.cargarEventosDatosFiltros = function() {
		
		filterEventosDatos.p_IdMovimiento 			= $scope.grid.seleccionEventos[0].IdMovimiento;
		filterEventosDatos.p_IdEvento					=$scope.grid.seleccionEventos[0].IdEvento;
		dsEventosDatos.setCallParameters(filterEventosDatos);
		
		filterEventosFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterEventosFiltros.p_IdTipoMovimiento		=	$scope.grid.seleccionEventos[0].IdTipoMovimiento.replace(/ /g, '');
		filterEventosFiltros.p_Estado				=   'V';
		dsEventosFiltros.setCallParameters(filterEventosFiltros);
	};
	
	
	$scope.cargarAsientos = function() {
		if($scope.grid.seleccionadoComprobante !== undefined){
		var idPreCompr=$scope.grid.seleccionadoComprobante[0].IdPreComprobante;
		var dsComprobantesFechaData=new bcs.DataSource({
			type : "call",
			definition : {								
				callParameters      : {p_IdEmpresa:$scope.SesionIdEmpresa,p_IdPreComprobante:idPreCompr},
				callTransaction 	: "12008",
				callResultSet 		: "p_out_TabPreComprobantes",
				schema : bcsMantenedorEventosGrid.ModeloConsultarComprobantesFecha(),
			}
		});
		var filter = {field: "IdPreComprobante", operator: "eq", value: idPreCompr};

		dsComprobantesFechaData.query({}).then(function(){
			var aux=dsComprobantesFechaData.data();
			var IdMonedaAsiento = '';
			
			if (dsComprobantesFechaData.data().length<1)
			{
				dsComprobantesFecha.read();
			
			}
			/*else
			{
				$scope.dataComprobanteSeleccionado= dsComprobantesFechaData.data();
				if($scope.dataComprobanteSeleccionado[0].Estado=='C'){
					dsComprobantesFecha.read();
					bcsComunes.ventanaMsg({
		                mensaje:$translate.instant("PRECOMPROBANTE_YA_ESTA_CONTABILIZADO"),
		                conflictos:null
		              }, idVentana);
					
				}
								
			}*/
			
		})
		
		$scope.filtros.comprobante 			= $scope.grid.seleccionadoComprobante[0].NombreTipoContabilidad;
		IdMonedaAsiento = $scope.grid.seleccionadoComprobante[0].IdMoneda;
//OPTIMUSPE-830		
//		$scope.filtros.debe 				= (util.bcs_formato($scope.grid.seleccionadoComprobante[0].Debe,'GEN','MONTO','CLP','','')).valor;
//		$scope.filtros.haber 				= (util.bcs_formato($scope.grid.seleccionadoComprobante[0].Haber,'GEN','MONTO','CLP','','')).valor;
		$scope.filtros.debe 				= (util.bcs_formato($scope.grid.seleccionadoComprobante[0].Debe,'GEN','MONTO',IdMonedaAsiento,'','')).valor;
		$scope.filtros.haber 				= (util.bcs_formato($scope.grid.seleccionadoComprobante[0].Haber,'GEN','MONTO',IdMonedaAsiento,'','')).valor;

		
		$scope.filtros.estado				= $scope.grid.seleccionadoComprobante[0].Estado;
		$scope.filtros.nombreEstado			= $scope.grid.seleccionadoComprobante[0].NombreEstado;

//OPTIMUSPE-830		
		$scope.filtros.debeKOptions			= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO',IdMonedaAsiento,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.filtros.haberKOptions		= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO',IdMonedaAsiento,'','')).cantidadDecimales,min:0,spinners: false};
//		$scope.filtros.debeKOptions			= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO','CLP','','')).cantidadDecimales,min:0,spinners: false};
//		$scope.filtros.haberKOptions		= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO','CLP','','')).cantidadDecimales,min:0,spinners: false};

		
		
		


		filterAsientos.p_IdPreComprobante 		= $scope.grid.seleccionadoComprobante[0].IdPreComprobante;
		filterAsientos.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filterAsientos.p_Origen					= 'M';		
		dsAsientos.setCallParameters(filterAsientos);
		
		if ($scope.grid.seleccionadoAsientos !== undefined){		
			dsConfiguracionComprobantes.read();			
			dsEventosAsientos.read();
			dsDetallesAsientos.read();	
			dsDetallesAsientosDatos.read();	
			dsDetallesAsientosFiltros.read();
			
		
		}
		}
		
	};
	
	$scope.cargarDetallesAsientos = function() {
		
		var IdMonedaDetAsiento = '';

		dsDetallesAsientosDatos.read();	
		dsDetallesAsientosFiltros.read();
		filterDetAsientos.p_IdPreComprobante 		= $scope.grid.seleccionadoAsientos[0].IdPreComprobante;
		filterDetAsientos.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filterDetAsientos.p_IdCuentaContable		= $scope.grid.seleccionadoAsientos[0].IdCuentaContable;
		filterDetAsientos.p_FechaContableInicial 	= util.dateSPdate($scope.filtros.kngFechaContable);
		filterDetAsientos.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
		filterDetAsientos.p_RutMovimiento			= '        0-0';
		filterDetAsientos.p_DatosFiltro				= [];
		
		dsDetallesAsientos.setCallParameters(filterDetAsientos);
		
		filterEventAsientos.p_IdPreComprobanteAsiento = $scope.grid.seleccionadoAsientos[0].IdPreComprobanteAsiento;
		IdMonedaDetAsiento = $scope.grid.seleccionadoAsientos[0].IdMoneda;
		
		dsEventosAsientos.setCallParameters(filterEventAsientos);
		
		filterConfComp.p_IdDefinicionAsiento		= $scope.grid.seleccionadoAsientos[0].IdDefinicionAsiento;
		filterConfComp.p_IdTipoContabilidad			= $scope.grid.seleccionadoAsientos[0].IdTipoContabilidad;
		filterConfComp.p_Estado						= $scope.grid.seleccionadoAsientos[0].Estado;
		
		dsConfiguracionComprobantes.setCallParameters(filterConfComp);
		
		$scope.filtrosDetAsientos.comprobante		= $scope.grid.seleccionadoAsientos[0].Glosa;
		$scope.filtrosDetAsientos.nombreCuenta		= $scope.grid.seleccionadoAsientos[0].NombreCuenta;
		$scope.filtrosDetAsientos.linea				= $scope.grid.seleccionadoAsientos[0].Linea;
		$scope.filtrosDetAsientos.debe				= $scope.grid.seleccionadoAsientos[0].MontoDebe;
		$scope.filtrosDetAsientos.cuentaContable	= $scope.grid.seleccionadoAsientos[0].IdCuentaContable;
		$scope.filtrosDetAsientos.haber				= $scope.grid.seleccionadoAsientos[0].MontoHaber;
		$scope.filtrosDetAsientos.estado			= $scope.grid.seleccionadoAsientos[0].Estado;
		$scope.filtrosDetAsientos.nombreEstado		= $scope.grid.seleccionadoAsientos[0].NombreEstado;

//OPTIMUSPE-830
//		$scope.filtros.debeDetKOptions				= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO','CLP','','')).cantidadDecimales,min:0,spinners: false};
//		$scope.filtros.haberDetKOptions				= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO','CLP','','')).cantidadDecimales,min:0,spinners: false};
		$scope.filtros.debeDetKOptions				= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO',IdMonedaDetAsiento,'','')).cantidadDecimales,min:0,spinners: false};
		$scope.filtros.haberDetKOptions				= {format: 'n'+(util.bcs_formato(0,'GEN','MONTO',IdMonedaDetAsiento,'','')).cantidadDecimales,min:0,spinners: false};

		
	};
	
	$scope.cargarAsientosDatosFiltros = function() {
		
		filterDetAsientosDatos.p_IdMovimiento 			= $scope.grid.seleccionadoDetAsiento[0].IdMovimiento;
		dsDetallesAsientosDatos.setCallParameters(filterDetAsientosDatos);
		
		filterDetAsientosFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterDetAsientosFiltros.p_IdTipoMovimiento		=	$scope.grid.seleccionadoDetAsiento[0].IdTipoMovimiento.replace(/ /g, '');
		dsDetallesAsientosFiltros.setCallParameters(filterDetAsientosFiltros);
	};
	
	$scope.resultado = [];
	
	$scope.movFiltrosInput			= false;
	$scope.movFiltrosCombo			= false;
	$scope.eventosFiltrosInput		= false;
	$scope.eventosFiltrosCombo		= false;
	$scope.detAsientosFiltrosInput	= false;
	$scope.detAsientosFiltrosCombo	= false;
	
	$scope.filtrosGrillas = function() {
		
		var filtersValoresFiltros = {};
		var datosPredefinidos;
		switch($scope.pestana)
		{
			case "Movimientos":
				
				datosPredefinidos = $scope.grid.seleccionadoFiltrosMovimientos.DatosPreDefinidos;
				filtersValoresFiltros.p_IdTipoDatoEvento = $scope.grid.seleccionadoFiltrosMovimientos.IdTipoDatoEvento;
				if( datosPredefinidos === "S")
				{
					
					
					$scope.movFiltrosInput		= false;
					$scope.movFiltrosCombo		= true;
					$scope.call("12006",filtersValoresFiltros);	
				}
				else
				{
					$scope.inputs.MovInput = "";
					$scope.inputs.comboboxMovimientos 	= '';
					$scope.movFiltrosCombo		= false;
					$scope.movFiltrosInput		= true;
					
					
				}
				break;
				
			case "Eventos":
				
				datosPredefinidos = $scope.grid.seleccionadoFiltrosEventos.DatosPreDefinidos;
				filtersValoresFiltros.p_IdTipoDatoEvento = $scope.grid.seleccionadoFiltrosEventos.IdTipoDatoEvento;
				if( datosPredefinidos === "S")
				{
					$scope.eventosFiltrosInput	= false;
					$scope.eventosFiltrosCombo	= true;
					$scope.call("12006",filtersValoresFiltros);	
				}
				else
				{
					$scope.inputs.EventInput = "";
					$scope.inputs.comboboxEventos = '';
					$scope.eventosFiltrosInput	= true;
					$scope.eventosFiltrosCombo	= false;
				}
					
				break;
				
			case "DetalleAsientos":
				
				datosPredefinidos = $scope.grid.selecccionadoFiltrosDetalleAsientos.DatosPreDefinidos;
				filtersValoresFiltros.p_IdTipoDatoEvento = $scope.grid.selecccionadoFiltrosDetalleAsientos.IdTipoDatoEvento;
				if( datosPredefinidos === "S")
				{
					$scope.detAsientosFiltrosInput	= false;
					$scope.detAsientosFiltrosCombo	= true;
					$scope.call("12006",filtersValoresFiltros);
				}
				else
				{
					$scope.detAsientosFiltrosInput			= true;
					$scope.detAsientosFiltrosCombo			= false;
					$scope.inputs.DetAsiento				= "";
					$scope.inputs.comboboxDetalleAsientos 	= '';
				}	
				break;
		}	
	};
	
	$scope.actualizarGrillaFiltrosCombo = function(item)
	{	
		switch($scope.pestana)
		{
			case "Movimientos":
				$scope.grid.seleccionadoFiltrosMovimientos.Valor = item.Valor;
				break;
				
			case "Eventos":
				$scope.grid.seleccionadoFiltrosEventos.Valor = item.Valor;
				break;
				
			case "DetalleAsientos":
				$scope.grid.selecccionadoFiltrosDetalleAsientos.Valor = item.Valor;
				break;
		}	
	}
	
	$scope.actualizarGrillaFiltrosInput = function(item)
	{	
		switch($scope.pestana)
		{
			case "Movimientos":
				$scope.grid.seleccionadoFiltrosMovimientos.Valor = item;
				//$scope.inputMov = null;
				break;
				
			case "Eventos":
				$scope.grid.seleccionadoFiltrosEventos.Valor = item;
				break;
				
			case "DetalleAsientos":
				$scope.grid.selecccionadoFiltrosDetalleAsientos.Valor = item;
				break;
		}	
	}
	
	/*var success_fn = function(respuesta)
	{
		
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0) {
			//respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0
			var resultSet = [];
			for ( var key in respuesta.P_OUT_TABFILTROSVALOR) {
				resultSet.push(respuesta.P_OUT_TABFILTROSVALOR[key]);
			}
			
			switch($scope.pestana)
			{
				case "Movimientos":
					$scope.inputs.comboboxMovimientos = resultSet;
					$scope.inputs.movSel = $scope.inputs.comboboxMovimientos[0];
					break;
					
				case "Eventos":
					$scope.inputs.comboboxEventos = resultSet;
					$scope.inputs.evenSel = $scope.inputs.comboboxEventos[0];
					dsEventos.read();
					break;
					
				case "DetalleAsientos":
					$scope.inputs.comboboxDetalleAsientos = resultSet;
					$scope.inputs.detAsSel = $scope.inputs.comboboxDetalleAsientos[0];
					break;
					
				case "Centralizaciones":
					bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
					dsCentralizacionesProcesadas.read();
					break;	
					
				case "PreComprobantes":
					bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
					dsComprobantesFecha.read();
					break;	
					
			}
						
		}else{
			bcsComunes.ventanaMsg({		
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
			
		} 
	};*/
	
	$scope.call = function(callTransaction,parameters) {
		bcsComunes.ventanaOverlay(idVentana, true);
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		
		var error = function(respuesta)
    	{	
    		console.log("Error:" + JSON.stringify(respuesta));
			if (callTransaction === "12065") {
				$scope.centralizacionCabeceraCrear();
			} else {
				bcsComunes.ventanaOverlay(idVentana, false);
    			bcsComunes.ventanaMsg({	
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
    	};
		
		var post = r.Filtros(parameters);
		
		post.$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);
			
			
			if(callTransaction === "12043")
			{
				bcsComunes.ventanaMsg({	
					mensaje: $translate.instant("UI_CENTRALIZACION_ANULADA"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
			else if(callTransaction === "4145"){
				dsConfiguracionComprobantes.read();	
				$scope.grid.seleccionadoComprobante = undefined;
				bcsComunes.ventanaMsg({	
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
			}
			else if(callTransaction === "12065"){
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADO'), campoSecuencia : "id" ,nombreSecuencia : 'UI_CENTRALIZACION',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
			}
			
			else if(callTransaction === "12061"){
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADO'), campoSecuencia : "id" ,nombreSecuencia : 'UI_PRECOMPROBANTE',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
			}
			else if(callTransaction !== "12006")
			{
				bcsComunes.ventanaMsg({	
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
			}
			


			var resultSet = [];
			for ( var key in respuesta.P_OUT_TABFILTROSVALOR) {
				resultSet.push(respuesta.P_OUT_TABFILTROSVALOR[key]);
			}
			switch($scope.pestana)
			{
				case "Centralizaciones":					
					dsCentralizacionesProcesadas.read();
					break;
				case "Movimientos":
					$scope.inputs.comboboxMovimientos = resultSet;
					$scope.inputs.movSel = $scope.inputs.comboboxMovimientos[0];
					dsMovimientosCentralizacion.read();
					break;					
				case "Eventos":
					$scope.inputs.comboboxEventos = resultSet;
					$scope.inputs.evenSel = $scope.inputs.comboboxEventos[0];
					dsEventos.read();
					break;
				
				case "PreComprobantes":					
					dsComprobantesFecha.read();
					break;

				case "DetalleAsientos":
					$scope.inputs.comboboxDetalleAsientos = resultSet;
					$scope.inputs.detAsSel = $scope.inputs.comboboxDetalleAsientos[0];
					break;
			};			
		}).catch(error);
	};
	
	$scope.aplicarFiltros = function() {
		
		var dataList = [];
		switch($scope.pestana)
		{
			case "Centralizaciones":
				if (filtersCent.p_FechaContableInicio!=undefined && filtersCent.p_FechaContableFinal!=undefined){
					filtersCent.p_IdEmpresa 				= $scope.SesionIdEmpresa;	
					filtersCent.p_Fecha						= util.dateSPdate($scope.filtros.kngFechaSistema);
					filtersCent.p_Estado					= $scope.filtros.EstadoCent && $scope.filtros.EstadoCent.IdEstadoContabilidad!=0 ? $scope.filtros.EstadoCent.IdEstadoContabilidad : '';
					filtersCent.p_IdTipoCentralizacion		= $scope.filtros.TipoCentralizacion ? $scope.filtros.TipoCentralizacion.IdTipoCentralizacion : 0;
					dsCentralizacionesProcesadas.setCallParameters(filtersCent);
				}
			break;
			case "Movimientos":
					$.each(dsMovimientosFiltros.data(), function(index, value){
						if(value.Valor !== ""){
							var data = {};
							data.IdTipoDatoEvento = value.IdTipoDatoEvento;
							data.TipoDato = value.TipoDato;
							if(value.TipoDato === 'V'){
								data.ValorVarchar = value.Valor;
								data.ValorDecimal = 0;
							}	
							else{
								data.ValorDecimal = value.Valor;
								data.ValorVarchar = '';
							}	
							dataList.push(data);
						}
					});
					
					filterMovCent.p_IdEmpresa          		= $scope.SesionIdEmpresa;
					filterMovCent.p_IdCentralizacion 		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
					filterMovCent.p_Rut 					= '        0-0';
					filterMovCent.p_FechaMovInicio 			= util.dateSPdate($scope.filtros.kngFechaContable);
					filterMovCent.p_FechaMovFinal			= util.dateSPdate($scope.filtros.kngFechaContable);
					filterMovCent.p_DatosFiltro				= dataList;
					$scope.movFiltrosInput		= false;
					$scope.movFiltrosCombo		= false;
					dsMovimientosCentralizacion.setCallParameters(filterMovCent);
			break;
			
			case "Eventos":

				$.each(dsEventosFiltros.data(), function(index, value){
					if(value.Valor !== ""){
						var data = {};
						data.IdTipoDatoEvento = value.IdTipoDatoEvento;
						data.TipoDato = value.TipoDato;
						if(value.TipoDato === 'V'){
							data.ValorVarchar = value.Valor;
							data.ValorDecimal = 0;
						}	
						else{
							data.ValorDecimal = value.Valor;
							data.ValorVarchar = '';
						}	
						dataList.push(data);
					}
				});
				
				filterEventos.p_IdEmpresa          		= $scope.SesionIdEmpresa;
				filterEventos.p_IdCentralizacion 		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
				filterEventos.p_Rut 					= '        0-0';
				filterEventos.p_FechaMovInicio 			= util.dateSPdate($scope.filtros.kngFechaContable);
				filterEventos.p_FechaMovFinal			= util.dateSPdate($scope.filtros.kngFechaContable);
				filterEventos.p_DatosFiltro				= dataList;
				$scope.eventosFiltrosInput	= false;
				$scope.eventosFiltrosCombo	= false;
				dsEventos.setCallParameters(filterEventos);
			break;
			
			case "DetalleAsientos":

				if (!$scope.grid.seleccionadoAsientos || $scope.grid.seleccionadoAsientos.length === 0) {
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_ASIENTO'));
					break;
				}
				$.each(dsDetallesAsientosFiltros.data(), function(index, value){
					if(value.Valor !== ""){
						var data = {};
						data.IdTipoDatoEvento = value.IdTipoDatoEvento;
						data.TipoDato = value.TipoDato;
						if(value.TipoDato === 'V'){
							data.ValorVarchar = value.Valor;
							data.ValorDecimal = 0;
						}	
						else{
							data.ValorDecimal = value.Valor;
							data.ValorVarchar = '';
						}	
						dataList.push(data);
					}
				});
				
				filterDetAsientos.p_IdPreComprobante 		= $scope.grid.seleccionadoAsientos[0].IdPreComprobante;
				filterDetAsientos.p_IdEmpresa 				= $scope.SesionIdEmpresa;
				filterDetAsientos.p_IdCuentaContable		= $scope.grid.seleccionadoAsientos[0].IdCuentaContable;
				filterDetAsientos.p_FechaContableInicial 	= util.dateSPdate($scope.filtros.kngFechaContable);
				filterDetAsientos.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
				filterDetAsientos.p_RutMovimiento			= '        0-0';
				filterDetAsientos.p_DatosFiltro				= dataList;
				$scope.detAsientosFiltrosInput			= false;
				$scope.detAsientosFiltrosCombo			= false;			
				dsDetallesAsientos.setCallParameters(filterDetAsientos);
			break;
		}			
	};
	
	$scope.limpiarMovimientosFiltros = function() {
		
		$scope.movFiltrosCombo					= false;
		$scope.movFiltrosInput					= false;
		filterMovFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterMovFiltros.p_IdTipoMovimiento		= '';
		dsMovimientosFiltros.setCallParameters(filterMovFiltros);
		filterMovCent.p_IdEmpresa          		= $scope.SesionIdEmpresa;
		filterMovCent.p_IdCentralizacion 		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
		filterMovCent.p_Rut 					= '        0-0';
		filterMovCent.p_FechaMovInicio 			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterMovCent.p_FechaMovFinal			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterMovCent.p_DatosFiltro				= [];
		dsMovimientosCentralizacion.setCallParameters(filterMovCent);
		filterMovDatos.p_IdMovimiento 			= 0;
		dsMovimientosDatos.setCallParameters(filterMovDatos);
		
		
	};
	
	$scope.limpiarEventosFiltros = function() {
		
		$scope.eventosFiltrosInput		= false;
		$scope.eventosFiltrosCombo		= false;
		
		filterEventosFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterEventosFiltros.p_IdTipoMovimiento		=	'';
		dsEventosFiltros.setCallParameters(filterEventosFiltros);
		filterEventos.p_IdEmpresa          		= $scope.SesionIdEmpresa;
		filterEventos.p_IdCentralizacion 		= $scope.grid.seleccionCentralizacion.IdCentralizacion;
		filterEventos.p_Rut 					= '        0-0';
		filterEventos.p_FechaMovInicio 			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterEventos.p_FechaMovFinal			= util.dateSPdate($scope.filtros.kngFechaContable);
		filterEventos.p_DatosFiltro				= [];
		dsEventos.setCallParameters(filterEventos);
		filterEventosDatos.p_IdMovimiento = 0;
		filterEventosDatos.p_IdEvento = 0;
		dsEventosDatos.setCallParameters(filterEventosDatos);
	};
	
	$scope.limpiarDetalleAsientosFiltros = function() {
			
		$scope.detAsientosFiltrosInput	= false;
		$scope.detAsientosFiltrosCombo	= false;
		
		filterDetAsientosFiltros.p_TipoConsulta			=	"MOVIMIENTO";
		filterDetAsientosFiltros.p_IdTipoMovimiento		=	'';
		dsDetallesAsientosFiltros.setCallParameters(filterDetAsientosFiltros);
		filterDetAsientos.p_IdPreComprobante 		= $scope.grid.seleccionadoAsientos[0].IdPreComprobante;
		filterDetAsientos.p_IdEmpresa 				= $scope.SesionIdEmpresa;
		filterDetAsientos.p_IdCuentaContable		= $scope.grid.seleccionadoAsientos[0].IdCuentaContable;
		filterDetAsientos.p_FechaContableInicial 	= util.dateSPdate($scope.filtros.kngFechaContable);
		filterDetAsientos.p_FechaContableFinal		= util.dateSPdate($scope.filtros.kngFechaContable);
		filterDetAsientos.p_RutMovimiento			= '        0-0';
		filterDetAsientos.p_DatosFiltro				= [];
		dsDetallesAsientos.setCallParameters(filterDetAsientos);
		filterDetAsientosDatos.p_IdMovimiento = 0;
		dsDetallesAsientosDatos.setCallParameters(filterDetAsientosDatos);
		
	};

	$scope.centralizacionCabeceraCrear = function() {
		$scope.filterCentralizacionCabeceraCrear.p_IdEmpresa = $scope.SesionIdEmpresa;
		$scope.filterCentralizacionCabeceraCrear.p_Ejercicio = util.date($scope.filtros.kngFechaContable).format("YYYY");
		$scope.filterCentralizacionCabeceraCrear.p_FechaContable = util.dateSPdate($scope.filtros.kngFechaContable);	
		$scope.filterCentralizacionCabeceraCrear.p_CodigoUsuario = $scope.SesionIdUsuario;
		$scope.filterCentralizacionCabeceraCrear.p_TipoCentralizacion = [];			 
		$.each(filterCentralizacion.p_in_TiposCentralizacion, function(key,value) {
			$scope.filterCentralizacionCabeceraCrear.p_TipoCentralizacion.push({ IdTipoCentralizacion: value.IdTipoCentralizacion });
		});
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010113";
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		var error = function(respuesta) {	
    		console.log("Error:" + JSON.stringify(respuesta));	    		
    		bcsComunes.ventanaOverlay(idVentana, false);
    		bcsComunes.ventanaMsg({	
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
    	};
		var success = function(respuesta) {
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADO'), campoSecuencia : "id" ,nombreSecuencia : 'UI_CENTRALIZACION',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
			dsCentralizacionesProcesadas.read();
		}
		var post = r.Filtros($scope.filterCentralizacionCabeceraCrear);
		post.$promise.then(success).catch(error);
	}
	/********** Fin Eventos grillas ************/
	
	$scope.$watch('grid.seleccionMovimientos',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaMontoMovimientos = 0;
		var moneda = '';
		$.each(newval,function(key,value){
			$scope.sumaMontoMovimientos = $scope.sumaMontoMovimientos + value.Monto;
			moneda = value.IdMoneda;
		})
		$scope.sumaMontoMovimientos = util.bcs_formato($scope.sumaMontoMovimientos,'GEN','Monto',moneda,'','').valor;
	});
	
	$scope.$watch('grid.seleccionEventos',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaMontoEventos = 0;
		var moneda = '';
		$.each(newval,function(key,value){
			$scope.sumaMontoEventos = $scope.sumaMontoEventos + value.Monto;
			moneda = value.IdMoneda;
		})
		$scope.sumaMontoEventos = util.bcs_formato($scope.sumaMontoEventos,'GEN','Monto',moneda,'','').valor;
	});
	
	$scope.$watch('grid.seleccionadoComprobante',function(newval,oldval) {
		if (!newval) return;
		var moneda = '';
		$scope.sumaMontoDebeComprobante = 0;
		$scope.sumaMontoHaberComprobante = 0;
		$.each(newval,function(key,value){
			$scope.sumaMontoDebeComprobante = $scope.sumaMontoDebeComprobante + value.Debe;
			$scope.sumaMontoHaberComprobante = $scope.sumaMontoHaberComprobante + value.Haber;
			moneda = value.IdMoneda;
		})
		$scope.sumaMontoDebeComprobante = util.bcs_formato($scope.sumaMontoDebeComprobante,'GEN','Monto',moneda,'','').valor;
		$scope.sumaMontoHaberComprobante = util.bcs_formato($scope.sumaMontoHaberComprobante,'GEN','Monto',moneda,'','').valor;
		
	});
	
	$scope.$watch('grid.seleccionadoAsientos',function(newval,oldval) {
		if (!newval) return;
		var moneda = '';
		$scope.sumaMontoDebeAsiento = 0;
		$scope.sumaMontoHaberAsiento = 0;
		$.each(newval,function(key,value){
			$scope.sumaMontoDebeAsiento = $scope.sumaMontoDebeAsiento + value.MontoDebe;
			$scope.sumaMontoHaberAsiento = $scope.sumaMontoHaberAsiento + value.MontoHaber;
			moneda = value.IdMoneda;
		})
		$scope.sumaMontoDebeAsiento = util.bcs_formato($scope.sumaMontoDebeAsiento,'GEN','Monto',moneda,'','').valor;
		$scope.sumaMontoHaberAsiento = util.bcs_formato($scope.sumaMontoHaberAsiento,'GEN','Monto',moneda,'','').valor;

		
	});
	
	$scope.$watch('grid.seleccionadoDetAsiento',function(newval,oldval) {
		if (!newval) return;
		var moneda = '';
		$scope.sumaMontoDebeDetalleAsiento = 0;
		$scope.sumaMontoHaberDetalleAsiento = 0;
		$.each(newval,function(key,value){
			$scope.sumaMontoHaberDetalleAsiento = $scope.sumaMontoHaberDetalleAsiento + value.MontoHaber;
			$scope.sumaMontoDebeDetalleAsiento = $scope.sumaMontoDebeDetalleAsiento + value.MontoDebe;
			moneda = value.IdMoneda;
		})
		$scope.sumaMontoHaberDetalleAsiento = util.bcs_formato($scope.sumaMontoHaberDetalleAsiento,'GEN','Monto',moneda,'','').valor;
		$scope.sumaMontoDebeDetalleAsiento = util.bcs_formato($scope.sumaMontoDebeDetalleAsiento,'GEN','Monto',moneda,'','').valor;
	});

	var array = [
		{ Estado: 'Vigente', Color: 'black' },		
		{ Estado: 'Anulado', Color: 'red' },	    
		{ Estado: 'Contabilizado', Color: 'green' },
		{ Estado: 'Error', Color: 'brown' },
		{ Estado: 'Descuadrado', Color: 'orange' },
	];
});
