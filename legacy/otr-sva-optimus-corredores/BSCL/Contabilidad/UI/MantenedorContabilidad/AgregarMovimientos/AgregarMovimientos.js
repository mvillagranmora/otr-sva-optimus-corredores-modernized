angular.module('ModuloContabilidad').controller('AgregarMovimientosCtrl',function(
		$rootScope
		,$scope
		,$resource
		,$http
		,bcsComunes
		,bcsMantenedorEventosGrid
		,$translate
		,bcsAlerta
		,$timeout
		,OPCIONES_BCSALERTA
	){
	
	$scope.movimientos = {};
	$scope.grid = {};
	$scope.inputs = {};
	var filtersMov = {};
	var Accion = ''
	var params = {};
		
	/*var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objTipoComprobante = $.extend({},parametrosVentana.Objeto);*/
	
	/*********** Seteo Inicial ********/
	
	$scope.inputs.fechMovimiento	=	util.dateInicioDia($scope.SesionFechaSistema);
	$scope.inputs.fechaContable		=	util.dateInicioDia($scope.SesionFechaSistema);
	
	/*$scope.inputs.numeroContabilidad = objTipoComprobante.IdTipoContabilidad;
	$scope.inputs.nombreTipoContabilidad = objTipoComprobante.Nombre;
	
	filtersListCent.p_IdEmpresa       		= $scope.SesionIdEmpresa;
	filtersListCent.p_CodigoUsuario			= $scope.SesionIdUsuario;
	
	filtersCent.p_IdEmpresa          		= $scope.SesionIdEmpresa;
	filtersCent.p_IdTipoContabilidad      	= objTipoComprobante.IdTipoContabilidad;	
	filtersCent.p_CodigoUsuario				= $scope.SesionIdUsuario;*/
		   
	/************ Fin Seteo inicial *********/
	
	/************ Grillas Centralizaciones ***********/
	
	var dsMovimientosFiltrados = new bcs.DataSource({
		type : "call",
		definition : {
			callParameters      : filtersMov,
			callTransaction 	: "",
			callResultSet 		: "p_out_TabCentralizacionesTipo",
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltrados(),
		}
	});
	
	$scope.movimientos.confMovimientosFiltrados = {
			dataSource			: dsMovimientosFiltrados,
			columns				: bcsMantenedorEventosGrid.columnasMovimientosFiltrados(),		
 			filterable			: false,
 			autoBind			: true,  //comentar
			height				: 320,
		    selectable 			: "row",
		    scrollable			: false
		};
	
	
	/*************** Fin grillas Centralizaciones *****************/
	
	/*************** Botonera Inferior ******************************/
	
	/*$scope.btnAgregar = function(centralizacionDispSeleccionado){	
		if (centralizacionDispSeleccionado === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var dataList = [];
			var data = {};
			
			params.p_IdEmpresa      			=	$scope.SesionIdEmpresa;    		
			params.p_IdUsuario      			=	$scope.SesionIdUsuario ;  		
			params.p_IdModulo       			= 	"CON";    		
			params.p_UsaReglasNegocio 			=  	'N';	
				
			data.IdTipoCentralizacion 			=	centralizacionDispSeleccionado.IdCentralizacionTipo;		
			data.IdTipoContabilidad 			=	objTipoComprobante.IdTipoContabilidad;
			data.Estado 						= 	centralizacionDispSeleccionado.Estado;
			dataList.push(data);
			
			params.p_TabTipoCentralizacionTipo	=	dataList;
			$scope.call("12029", params, success, error_eliminar);
		};
	};	
	
	$scope.btnEliminar = function(centralizacionAsocSeleccionado){
		if (centralizacionAsocSeleccionado === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{		
			params.p_IdEmpresa      			=	$scope.SesionIdEmpresa;    		
			params.p_IdUsuario      			=	$scope.SesionIdUsuario ;  		
			params.p_IdModulo       			= 	"CON";    		
			params.p_IdTipoCentralizacion 		=  	centralizacionAsocSeleccionado.IdTipoCentralizacion;
			params.p_IdTipoContabilidad			=	objTipoComprobante.IdTipoContabilidad;
			$scope.call("12030", params, success, error_eliminar); 

		};
	}; 
	
	$scope.call = function(callTransaction,parameters,success, error) {
		var resultSet = [];
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros}
		});
		var post = r.Filtros(parameters);
		post.$promise.then(success).catch(error);
	};
	
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
			dsCentralizacionesRequeridas.read();
			dsListaCentralizaciones.read();
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var error_agregar = function(respuesta)
	{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_PROCESAR"));
	};
	
	var error_eliminar = function(respuesta)
	{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_PROCESAR"));
	};*/
	
	
	/*************** 	Fin Botonera Inferior ******************************/
	
});
