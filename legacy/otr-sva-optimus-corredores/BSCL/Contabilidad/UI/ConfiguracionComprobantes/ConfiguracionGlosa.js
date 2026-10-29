angular.module('ModuloContabilidad').controller('ConfiguracionGlosaCtrl',function($rootScope,$scope,$resource,$http,bcsComunes,bcsMantenedorEventosGrid,$translate,bcsAlerta,$timeout,OPCIONES_BCSALERTA){
	

	$scope.centralizaciones = {};
	$scope.grid = {};
	$scope.inputs = {};
	$scope.eventos = {};
	var filtersEventsDisp = {};
	var filtersCent = {};
	var	filterEventAsoc = {};
	var Accion = ''
	var filterGlosaAsociado = {};
		
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objAsiento = $.extend({},parametrosVentana.Objeto);
	var objTipoComprobante = $.extend({},parametrosVentana.Objeto);
	
	filterGlosaAsociado.p_IdEmpresa  =	$scope.SesionIdEmpresa;  
	filterGlosaAsociado.p_IdUsuario  =	$scope.SesionIdUsuario ;	
	filterGlosaAsociado.p_IdModulo  =	"CON";
	filterGlosaAsociado.p_UsaReglasNegocio  =	"N";
	filterGlosaAsociado.p_IdTipoContabilidad  = objTipoComprobante.IdTipoContabilidad;
	filterGlosaAsociado.p_IdDefinicionAsiento  = objTipoComprobante.IdDefinicionAsiento;
	filterGlosaAsociado.p_TipoConsulta = "A"; 
	
	/*********** Seteo Inicial ********/
	
	 	
	$scope.inputs.numeroContabilidad = objTipoComprobante.IdTipoContabilidad;
	$scope.inputs.nombreTipoContabilidad = objTipoComprobante.TipoContabilidad;

	/************ Fin Seteo inicial *********/
	
	/************ Grillas Glosas ***********/
	
	var dsGlosaDisponibles = new bcs.DataSource({
		type : "call",
		definition : {
			callParameters      : filterGlosaAsociado,
			callTransaction 	: "12040",
			callResultSet 		: "p_out_TabValoresGlosaDisponibles",
			schema : bcsMantenedorEventosGrid.ModeloConsultarGlosa(),
		}
	});
	
	$scope.eventos.confGlosasDisponibles = {
			dataSource			: dsGlosaDisponibles,
			columns				: bcsMantenedorEventosGrid.columnasGlosa(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 350,
		    selectable 			: "row",
		    scrollable			: false
		};
	/*****CAMBIOS GRILLAS *************/
	
	var dsEventosAsociados = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterEventAsoc,
			callTransaction 	: "12011",
			callResultSet 		: "p_out_TabEventosAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsiento(),
		}
	});
	
	var dsGlosaAsociados = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterGlosaAsociado,
			callTransaction 	: "12041",
			callResultSet 		: "p_out_TabValoresGlosaAsociados",
			schema : bcsMantenedorEventosGrid.ModeloConsultarGlosa(),
		}
	});
	
	
	$scope.eventos.confGlosaAsociados = {
			dataSource			: dsGlosaAsociados,
			columns				: bcsMantenedorEventosGrid.columnasGlosaAsociadas(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 350,
		    selectable 			: "row",
		    pageable			: false
		};
	
	/******************/

	/*************** Fin grillas Glosas *****************/
	
	/*************** Botonera Inferior ******************************/
	
	$scope.btnAgregar = function(seleccionadoTiposComprobante){	
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			 

		};
	};	
	
	$scope.btnEliminar = function(seleccionadoEventoAsoc){
		if(seleccionadoEventoAsoc !== undefined)
		{
			var params = {};		
			params.p_IdEmpresa				=	$scope.SesionIdEmpresa;         		
			params.p_IdUsuario          	=	$scope.SesionIdUsuario ;	
			params.p_IdModulo           	=	"CON"; 	
			params.p_UsaReglasNegocio		=   "N";			
		    params.p_IdTipoContabilidad     = 	$scope.grid.seleccionadoEventoAsoc.IdTipoContabilidad;
			params.p_IdDefinicionAsiento	= 	objTipoComprobante.IdDefinicionAsiento; 
		    params.p_IdTipoDatoEvento 		=	$scope.grid.seleccionadoEventoAsoc.NombreCampo;	
		    params.p_TipoConsulta			=	$scope.inputs.todosAsientos? 'C':'A';
		    
		    	
			$scope.call("12037", params,success_fn);
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_GRABAR"));
	}; 
	
	$scope.btnGrabar = function(seleccionadoGlosaDisp){	
				
		if(seleccionadoGlosaDisp !== undefined && $scope.inputs.numeroOrden !== undefined )
		{
			var params = {};		
			params.p_IdEmpresa				=	$scope.SesionIdEmpresa;         		
			params.p_IdUsuario          	=	$scope.SesionIdUsuario ;	
			params.p_IdModulo           	=	"CON"; 	
			params.p_UsaReglasNegocio		=	"N";
		 
			params.p_IdTipoContabilidad		=	seleccionadoGlosaDisp.IdTipoContabilidad;	//$scope.grid.seleccionadoEventoDisp.IdTipoEvento;	
		    params.p_IdDefinicionAsiento	=	objTipoComprobante.IdDefinicionAsiento;		
		    params.p_Origen					=	seleccionadoGlosaDisp.Origen;	 
		    params.p_Orden					=	$scope.inputs.numeroOrden;
			   
		    params.p_GlosaCampo				=	seleccionadoGlosaDisp.GlosaCampo;
		    params.p_NombreCampoMovimiento	=	""; 
			params.p_IdTipoDatoEvento		=   seleccionadoGlosaDisp.NombreCampo; 		   	   		    	 
		    params.p_Estado					=	"V";
		    params.p_TodosAsientos			=   $scope.inputs.todosAsientos? 'S':'N';
		    
			$scope.call("12038", params,success_fn);
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_GRABAR"));
	};
	
	 
	
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
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {

			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_GRABAR_EXITO"));
			dsGlosaAsociados.read();
			dsGlosaDisponibles.read();
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var error = function(respuesta)
	{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_GRABAR"));
	};
	/*************** 	Fin Botonera Inferior ******************************/
	
});
