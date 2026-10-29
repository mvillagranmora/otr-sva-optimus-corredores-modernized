angular.module('ModuloContabilidad').controller('EventosAsientoCtrl',function($rootScope,$scope,$resource,$http,bcsComunes,bcsMantenedorEventosGrid,$translate,bcsAlerta,$timeout,OPCIONES_BCSALERTA){
	
	$scope.centralizaciones = {};
	$scope.grid = {};
	$scope.inputs = {};
	$scope.data = {};
	$scope.eventos = {};
	var filtersEventsDisp = {};
	var filtersCent = {};
	var	filterEventAsoc = {};
	var Accion = ''
		
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objAsiento = $.extend({},parametrosVentana.Objeto);
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	/*********** Seteo Inicial ********/
	$scope.vIdDefinicionAsiento = objAsiento.IdDefinicionAsiento;
	filterEventAsoc.p_IdDefinicionAsiento 		= objAsiento.IdDefinicionAsiento;
	$scope.inputs.suma 							= true;
	filtersEventsDisp.p_IdTipoCentralizacion   	= objAsiento.IdTipoContabilidad;
	filtersEventsDisp.p_Estado					= 'V';
	$scope.data.tipoCentralizacion				= objAsiento.IdTipoContabilidad;
	
	$scope.data.filtroTipoContabilidad = { 
		IdTipoContabilidad : {field: "IdTipoContabilidad", operator: "eq", value: objAsiento.IdTipoContabilidad}
	};

	/************ Fin Seteo inicial *********/
	
	/************ Grillas Glosas ***********/
	
	var dsEventosDisponibles = new bcs.DataSource({
		type : "call",
		definition : {
			callParameters      : filtersEventsDisp,
			callTransaction 	: "12016",
			callResultSet 		: "p_out_TabEventos",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosDisponibles(),
		}
	});
	
	$scope.eventos.confEventosDisponibles = {
			dataSource			: dsEventosDisponibles,
			columns				: bcsMantenedorEventosGrid.columnasEventosDisponibles(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 340,
		    selectable 			: "row",
		    scrollable			: false
		};
	
	var dsEventosAsociados = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterEventAsoc,
			callTransaction 	: "12011",
			callResultSet 		: "p_out_TabEventosAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsiento(),
		}
	});


	
	$scope.eventos.confEventosAsociados = {
			dataSource			: dsEventosAsociados,
			columns				: bcsMantenedorEventosGrid.columnasEventosAsientos(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 340,
		    selectable 			: "row",
		    pageable			: false
		};

	/*************** Fin grillas Glosas *****************/
	
	/*************** Botonera Inferior ******************************/
	
	$scope.btnAgregar = function(seleccionadoTiposComprobante){	
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var params = {};		
			params.p_IdEmpresa				=	$scope.SesionIdEmpresa;         		
			params.p_IdUsuario          	=	$scope.SesionIdUsuario ;	
			params.p_IdModulo           	=	"CON"; 	
		    params.p_IdTipoEvento			=	$scope.grid.seleccionadoEventoAsoc.IdTipoEvento;	
		    params.p_IdDefinicionAsiento	=	objAsiento.IdDefinicionAsiento;	
		    	
			$scope.call("12035", params,success_fn);

		};
	};	
	
	$scope.btnEliminar = function(seleccionadoEventoAsoc){
		if(seleccionadoEventoAsoc === undefined)
		{
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
		return;
		}else{			
			var params = {};		
			params.p_IdEmpresa				=	$scope.SesionIdEmpresa;         		
			params.p_IdUsuario          	=	$scope.SesionIdUsuario ;	
			params.p_IdModulo           	=	"CON"; 	
		    params.p_IdTipoEvento			=	$scope.grid.seleccionadoEventoAsoc.IdTipoEvento;	
		    params.p_IdDefinicionAsiento	=	objAsiento.IdDefinicionAsiento;	
		    	
			$scope.call("12039", params,success_fn);
		}
		};
		 
			
	
	
	$scope.btnGrabar = function(seleccionadoEventoDisp){	
		if(seleccionadoEventoDisp !== undefined)
		{
			
			var params = {};		
			params.p_IdEmpresa				=	$scope.SesionIdEmpresa;         		
			params.p_IdUsuario          	=	$scope.SesionIdUsuario ;	
			params.p_IdModulo           	=	"CON"; 	
			params.p_UsaReglasNegocio		=	"N";
		    params.p_IdTipoEvento			=	$scope.grid.seleccionadoEventoDisp.IdTipoEvento;	
		    params.p_IdDefinicionAsiento	=	objAsiento.IdDefinicionAsiento;	
//		    params.p_Operador				=	"+";//$scope.inputs.suma ? "+" : "-";
		    params.p_Operador				=	$scope.inputs.suma ? "+" : "-";
		    params.p_Estado					=	objAsiento.Estado;
			
			$scope.call("12035", params,success_fn);
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_GRABAR"));
	};
	
	$scope.$watch('inputs.suma',function(newval,oldval){
		switch (newval){
			case true:  	$scope.inputs.resta 		= false; break;
			case false:		$scope.inputs.resta 		= true; break;
		}			
	});
	
	$scope.$watch('inputs.resta',function(newval,oldval){
		switch (newval){
			case true:  	$scope.inputs.suma 		= false; break;
			case false:		$scope.inputs.suma 		= true; break;	
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
	
	$scope.call = function(callTransaction,parameters,success) {
		var resultSet = [];
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		var post = r.Filtros(parameters);
		post.$promise.then(success) 
	};
	
	var success_fn = function(respuesta)
	{
		filterEventAsoc = {};
		
		filterEventAsoc.p_IdDefinicionAsiento = $scope.vIdDefinicionAsiento;		
		dsEventosAsociados.read();
		
		bcsComunes.ventanaMsg({				 
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);
	};	
	var error = function(respuesta)
	{
		bcsComunes.ventanaMsg({				 
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);
	};


	
	/*************** 	Fin Botonera Inferior ******************************/
	
});
