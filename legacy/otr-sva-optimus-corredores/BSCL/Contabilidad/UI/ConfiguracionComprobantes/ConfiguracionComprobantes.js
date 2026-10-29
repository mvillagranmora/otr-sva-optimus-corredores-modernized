angular.module('ModuloContabilidad').controller('ConfiguracionComprobantesCtrl',function($rootScope,$scope,$resource,$http,bcsComunes,bcsMantenedorEventosGrid,$translate,bcsAlerta,$timeout,OPCIONES_BCSALERTA){
	
	$scope.planDeCuentas = {};
	$scope.tipoComprobantes = {};
	$scope.asientos = {};
	$scope.grid = {};
	$scope.filtros = {};
	$scope.inputs = {};
	var filtersTipComp = {};
	var filtersAsComp = {};
	var filtersCent = {};
	var filterCondComp = {};
	var filterAsDatos = {};
	var filterAsientos = {};
	var	filterEventAsiento = {};
	var filterCondAs = {};
	var filtersValoresFiltros = {};
	var Accion = '';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	/*********** Seteo Inicial ********/
	
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
					$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
					return true;
				 });
			 }
	    });		
	};
	
	filtersTipComp.p_IdEmpresa 					= $scope.SesionIdEmpresa;
	filterEventAsiento.p_IdDefinicionAsiento 	= -1;
	$scope.select = function(grid){
		$scope.pestaña = grid;
		util.refreshGridSize(grid,0);
		util.refreshGridSize("asientosGrid",0);
		util.refreshGridSize("datosAsientosGrid",0);
		util.refreshGridSize("datosAsientosGridB",0);
		util.refreshGridSize("gridCondicionesAsientos",0);
		util.refreshGridSize("gridEventosAsientos",0)
		util.refreshGridSize("gridCondAsiento",0)
	}
	

	$scope.inputs.comboDisabled = true;
	$scope.inputs.inputDisabled = true;
	$scope.inputs.comboBDisabled = true;
	$scope.inputs.inputBDisabled = true;
	
	/************ Fin Seteo inicial *********/
	
	var dataSourcePlanDeCuentas = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: false, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601112",
					dataType: "json"
				}		
			},
			schema: {
				model: {
					fields: {
						Cuenta		 	: { type: "string"},
						NombreCuenta	: { type: "string"}
					}
		     	}
			}
		}
	});
	
	$scope.planDeCuentas.configuracionGrid = {
			dataSource			: dataSourcePlanDeCuentas,
			sortable			: true,
			height				: 560,
			selectable 			: "none",
			autoBind            : true,
			filterable			: true,
			pageable			: true,
			allowCopy			: true,
			userSettings		: true,
			reorderable			: true,
			columns				: [
									{field: "Cuenta",				title: "UI_NUMERO_CUENTA",	width: "150px"},
									{field: "NombreCuenta",			title: "UI_NOMBRE"}
			 ],
			gridCreated			: function(grid){
				$scope.gridPlanCuentas = grid;
			}
	}
	
	/************ Grillas Tipo Comprobantes ***********/
	
	var dsTiposComprobante = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filtersTipComp,
			callTransaction 	: "12009",
			callResultSet 		: "p_out_TabContabilidadTipo",
			schema : bcsMantenedorEventosGrid.ModeloConsultarListaComprobantes(),
		}
	});
	
	$scope.tipoComprobantes.confTiposComprobantes = {
			dataSource			: dsTiposComprobante,
			columns				: bcsMantenedorEventosGrid.columnasListaComprobantes(),		
 			filterable			: true,
 			autoBind			: true,  //comentar
			height				: 560,
		    selectable 			: "row",
		    pageable			: true,
		    scrollable			: false
		}; 
	
	var dsAsientosComprobante = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filtersAsComp,
			callTransaction 	: "12013",
			callResultSet 		: "p_out_TabContabilidadDefinicionAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarAsientosComprobantes(),
		}
	});
	
	$scope.tipoComprobantes.confAsientosComprobante = {
			dataSource			: dsAsientosComprobante,
			columns				: bcsMantenedorEventosGrid.columnasAsientosComprobantes(),		
 			filterable			: false,
 			//autoBind			: false,  //comentar
			height				: 300,
		    selectable 			: "row",
		    scrollable			: false,
		    pageable			: true
		}; 
	
	var dsCentralizacionesRequeridas = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filtersCent,
			callTransaction 	: "12021",
			callResultSet 		: "p_out_TabTipoCentralizacionTipoContabilidad",
			schema : bcsMantenedorEventosGrid.ModeloConsultarCentralizacionesRequeridas(),
		}
	});
	
	$scope.tipoComprobantes.confCentralizacionesRequeridas = {
			dataSource			: dsCentralizacionesRequeridas,
			columns				: bcsMantenedorEventosGrid.columnasCentralizacionesRequeridas(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 230,
		    selectable 			: "row",
		    scrollable			: false
		    //pageable			: true
		}; 
	
	/************** Fin grillas Tipo Comprobantes ***********/
	
	/************** Grillas Configurar Asientos *******************/
	
	var dsCondicionesComprobante = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterCondComp,
			callTransaction 	: "12018",
			callResultSet 		: "p_out_TabCondiciones",
			schema : bcsMantenedorEventosGrid.ModeloConsultarConfiguracionComprobante(),
		}
	});
	
	$scope.asientos.condComprobanteConfiguracion = {
			dataSource			: dsCondicionesComprobante,
			columns				: bcsMantenedorEventosGrid.columnasConfiguracionComprobante(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "multiple row",
		    pageable			: true
		};
	
	var dsAsientosDatos = new bcs.DataSource({
		type : "call",
		definition : {						
			callParameters      : filterAsDatos,
			callTransaction 	: "12005",
			callResultSet 		: "p_out_TabFiltrosTipoDatos",
			editable            : true,
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltros(),
		}
	});
	
	$scope.asientos.confAsientosDatos = {
			dataSource			: dsAsientosDatos,
			columns				: bcsMantenedorEventosGrid.columnasDatosAsientos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "row",
		    pageable			: true
		    
		};
	
	var dsAsientosDatosB = new bcs.DataSource({
		type : "call",
		definition : {						
			callParameters      : filterAsDatos,
			callTransaction 	: "12005",
			callResultSet 		: "p_out_TabFiltrosTipoDatos",
			editable            : true,
			schema : bcsMantenedorEventosGrid.ModeloConsultarMovimientosFiltros(),
		}
	});
	
	$scope.asientos.confAsientosDatosB = {
			dataSource			: dsAsientosDatosB,
			columns				: bcsMantenedorEventosGrid.columnasDatosAsientos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "row",
		    pageable			: true
		};
	
	var dsAsientos = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filtersAsComp,
			callTransaction 	: "12013",
			callResultSet 		: "p_out_TabContabilidadDefinicionAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarAsientosComprobantes(),
		}
	});
	
	$scope.asientos.confAsientos = {
			dataSource			: dsAsientos,
			columns				: bcsMantenedorEventosGrid.columnasAsientosConf(),		
 			filterable			: true,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "row",
		    pageable			: true,
		    scrollable			: false,
		    sortable			: true
		};
	
	var dsEventosAsiento = new bcs.DataSource({
		type : "call",
		definition : {								
			callParameters      : filterEventAsiento,
			callTransaction 	: "12011",
			callResultSet 		: "p_out_TabEventosAsiento",
			schema : bcsMantenedorEventosGrid.ModeloConsultarEventosAsiento(),
		}
	});
	
	$scope.asientos.confEventosAsientos = {
			dataSource			: dsEventosAsiento,
			columns				: bcsMantenedorEventosGrid.columnasEventosAsientos(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "row",
		    pageable			: true,
		    scrollable			: false
		};
	
	var dsCondicionesAsiento = new bcs.DataSource({
		type : "call",
		definition : {							
			callParameters      : filterCondAs,
			callTransaction 	: "12018",
			callResultSet 		: "p_out_TabCondiciones",
			schema : bcsMantenedorEventosGrid.ModeloConsultarConfiguracionComprobante(),
		}
	});
	
	$scope.asientos.confCondicionesAsiento = {
			dataSource			: dsCondicionesAsiento,
			columns				: bcsMantenedorEventosGrid.columnasConfiguracionComprobante(),		
 			filterable			: false,
 			autoBind			: false,  //comentar
			height				: 160,
		    selectable 			: "multiple row",
		    pageable			: {
				pageSize		: true,
				buttonCount		: 5
			},
		};


	/*************** Fin grillas Configurar Asientos *****************/
	
	/*************** Botonera Inferior Asientos******************************/
	
	$scope.btnIngresarAsiento = function(seleccionadoAsiento){	
		Accion = 'Ingresar' 
		$scope.abrirModal(seleccionadoAsiento,"MantenedorAsiento");
	};
	
	$scope.btnAgregarMetadata = function(seleccionadoTiposComprobante){	
		Accion = 'Ingresar' 
		$scope.abrirModal(seleccionadoTiposComprobante,"AgregarAsientoMetadata");
	};
	
	$scope.btnModificarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			Accion = 'Modificar' 
			$scope.abrirModal(seleccionadoAsiento,"MantenedorAsiento");
		}
	};
	$scope.btnReplicarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			Accion = 'Replicar' 
			$scope.abrirModal(seleccionadoAsiento,"MantenedorAsiento");
		}
	};
	
	$scope.btnAprobarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_HABILITAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Aprobar";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	$scope.btnBloquearAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_BLOQUEAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Bloquear";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	}; 
	
	$scope.btnEliminarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_ELIMINAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Eliminar";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	/*************** 	Fin Botonera Inferior Asientos******************************/
	
/*************** Botonera Inferior Comprobantes******************************/
	
	$scope.btnIngresar = function(seleccionadoTiposComprobante){	
		Accion = 'Ingresar' 
		$scope.abrirModal(seleccionadoTiposComprobante,"ConfiguracionTipoContabilidad");
	};
	
	$scope.btnAgregar = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			$scope.abrirModal(seleccionadoTiposComprobante,"CentralizacionesRequeridas");
		}
	};

	$scope.btnModificar = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			Accion = 'Modificar' 
			$scope.abrirModal(seleccionadoTiposComprobante,"ConfiguracionTipoContabilidad");
		}
	};
	$scope.btnReplicar = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			Accion = 'Replicar' 
			$scope.abrirModal(seleccionadoTiposComprobante,"ConfiguracionTipoContabilidad");
		}
	};
	
	$scope.btnAprobar = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_HABILITAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Aprobar";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	$scope.btnBloquear = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_BLOQUEAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Bloquear";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	}; 
	
	$scope.btnEliminar = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_TIPO_COMPROBANTE_ELIMINAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Eliminar";
					var params = $scope.obtenerParametros(seleccionadoTiposComprobante,Accion);
					$scope.call("12022", params, success_fn);
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	$scope.btnCargar = function(){
		$scope.abrirModal(undefined,"CargaConfiguracionContable");
	}
	
	/*************** 	Fin Botonera Inferior Comprobantes******************************/
	
	/**************		Funciones Varias ***************************************/
	
	$scope.obtenerParametros = function(seleccionadoTiposComprobante,Accion){
		var params = {};
		params.p_IdEmpresa      		=	$scope.SesionIdEmpresa;    		
		params.p_IdUsuario      		=	$scope.SesionIdUsuario ;  		
		params.p_IdModulo       		= 	"CON";    		
		params.p_IdTipoContabilidad		= 	seleccionadoTiposComprobante.IdTipoContabilidad;
		switch(Accion)
		{
			case "Eliminar"	: params.p_Estado	= 	"E";break;
			case "Bloquear"	: params.p_Estado	= 	"B";break;
			case "Aprobar"	: params.p_Estado	= 	"V";break;
		}			
		return params;
	}
	
	$scope.obtenerParametrosAsiento = function(seleccionadoAsiento,Accion){
		var params = {};
		params.p_IdEmpresa      		=	$scope.SesionIdEmpresa;    		
		params.p_IdUsuario      		=	$scope.SesionIdUsuario ;  		
		params.p_IdModulo       		= 	"CON";    		
		params.p_IdTipoContabilidad		= 	$scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;
		params.p_IdDefinicionAsiento	=	seleccionadoAsiento.IdDefinicionAsiento;
		switch(Accion)
		{
			case "Eliminar"	: params.p_Estado	= 	"E";break;
			case "Bloquear"	: params.p_Estado	= 	"B";break;
			case "Aprobar"	: params.p_Estado	= 	"V";break;
		}			
		return params;
	}
	
	$scope.call = function(callTransaction,parameters,success) {
		var resultSet = [];
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		var post = r.Filtros(parameters);
		post.$promise.then(success);
	};
	
	var success_fn = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var refrescar = false;
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
			dsTiposComprobante.read();
		}
		else
			bcsComunes.ventanaMsg({				 
			      // mensaje:$translate.instant("UI_OPERACION_INGRESADA")+": " + respuesta.P_OUT_ERRORESDETALLE[0].IdInterno,
			conflictos:util.parseServerResponse(respuesta)
			},idVentana);
	};
	
	var success_fn_asiento = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var refrescar = false;
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
			dsAsientos.read();
		}
		else
			bcsComunes.ventanaMsg({				 
			      // mensaje:$translate.instant("UI_OPERACION_INGRESADA")+": " + respuesta.P_OUT_ERRORESDETALLE[0].IdInterno,
			conflictos:util.parseServerResponse(respuesta)
			},idVentana);
	};
	
	var success_fnCombo = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var resultSet = [];
			for ( var key in respuesta.P_OUT_TABFILTROSVALOR) {
				resultSet.push(respuesta.P_OUT_TABFILTROSVALOR[key]);
			}
			$scope.inputs.comboboxEventos = resultSet;
			$scope.inputs.valorCombo = $scope.inputs.comboboxEventos[0];
			$scope.inputs.inputDisabled = true;
			$scope.inputs.comboDisabled = false;
			$scope.inputs.valorDato = "";
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var success_fnComboB = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var resultSet = [];
			for ( var key in respuesta.P_OUT_TABFILTROSVALOR) {
				resultSet.push(respuesta.P_OUT_TABFILTROSVALOR[key]);
			}
			$scope.inputs.comboboxEventosB = resultSet;
			$scope.inputs.valorComboB = $scope.inputs.comboboxEventosB[0];
			$scope.inputs.inputBDisabled = true;
			$scope.inputs.comboBDisabled = false;
			$scope.inputs.valorDatoB = "";
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var success_condicion = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
			dsCondicionesComprobante.read();
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var success_condicionB = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_EXITO"));
			dsCondicionesAsiento.read();
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	$scope.abrirModal = function(seleccionado, ventana)
	{	
		var id1 = bcsComunes.ventanaAbrir(ventana,{Objeto : seleccionado, Accion: Accion, data: $scope.filtros.tipoContabilizacion, comprobante: $scope.grid.seleccionadoTiposComprobante },false,false,$scope.idVentana);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	//if (data === true)
			//{	         
        		$scope.asientos.asientosGridApi.dataSource.read();
        		$scope.asientos.asientosEventosGridApi.dataSource.read();
        		$scope.asientos.centralizacionesRequeridasGridApi.dataSource.read();
        		$scope.tipoComprobantes.tipoComprobantesApi.dataSource.read();
			//}
    	});  
        
	};
	
	/**************	Fin	Funciones Varias ***************************************/
	
	/***************Botones Configuracion Asientos ******************/
	
	$scope.btnConfigurarGlosa = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			seleccionadoAsiento.TipoContabilidad = $scope.grid.seleccionadoTiposComprobante.Nombre;
			$scope.abrirModal(seleccionadoAsiento,"ConfiguracionGlosa");
		}
	};
	
	$scope.btnAgregarEventos = function(seleccionadoTiposComprobante){
		if (seleccionadoTiposComprobante === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			$scope.abrirModal(seleccionadoTiposComprobante,"EventosAsiento");
		}
	};
	
	$scope.btnAprobarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ASIENTO_HABILITAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Aprobar";
					var params = $scope.obtenerParametrosAsiento(seleccionadoAsiento,Accion);
					$scope.call("12033", params, success_fn_asiento);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	$scope.btnEliminarAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ASIENTO_ELIMINAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Eliminar";
					var params = $scope.obtenerParametrosAsiento(seleccionadoAsiento,Accion);
					$scope.call("12033", params, success_fn_asiento);
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	};
	
	$scope.btnBloquearAsiento = function(seleccionadoAsiento){
		if (seleccionadoAsiento === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ASIENTO_BLOQUEAR"));
			  t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					Accion = "Bloquear";
					var params = $scope.obtenerParametrosAsiento(seleccionadoAsiento,Accion);
					$scope.call("12033", params, success_fn_asiento);	
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   

			};
	}; 
	
	$scope.$watch('inputs.igual',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igual 				= true; 
				$scope.inputs.distinto 				= false;
				$scope.inputs.mayorEstricto 		= false;
				$scope.inputs.menorEstricto 		= false;
				$scope.inputs.valorCondicion 		= "=";
				$scope.inputs.igualDisabled	 		= true;	  	
				$scope.inputs.distintoDisabled		= false;
				$scope.inputs.mayorEstrictoDisabled	= false;
				$scope.inputs.menorEstrictoDisabled	= false;
				break;
		}			
	});
	$scope.$watch('inputs.distinto',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igual 				= false; 
				$scope.inputs.distinto 				= true;
				$scope.inputs.mayorEstricto 		= false;
				$scope.inputs.menorEstricto 		= false;
				$scope.inputs.valorCondicion 		= "<>";
				$scope.inputs.igualDisabled	 		= false;	  	
				$scope.inputs.distintoDisabled		= true;
				$scope.inputs.mayorEstrictoDisabled	= false;
				$scope.inputs.menorEstrictoDisabled	= false;
				break;
		}			
	});
	$scope.$watch('inputs.mayorEstricto',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igual 				= false; 
				$scope.inputs.distinto 				= false;
				$scope.inputs.mayorEstricto 		= true;
				$scope.inputs.menorEstricto 		= false;
				$scope.inputs.valorCondicion 		= ">";
				$scope.inputs.igualDisabled	 		= false;	  	
				$scope.inputs.distintoDisabled		= false;
				$scope.inputs.mayorEstrictoDisabled	= true;
				$scope.inputs.menorEstrictoDisabled	= false;
				break;
		}			
	});
	$scope.$watch('inputs.menorEstricto',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igual 				= false; 
				$scope.inputs.distinto 				= false;
				$scope.inputs.mayorEstricto 		= false;
				$scope.inputs.menorEstricto 		= true;
				$scope.inputs.valorCondicion 		= "<";
				$scope.inputs.igualDisabled	 		= false;	  	
				$scope.inputs.distintoDisabled		= false;
				$scope.inputs.mayorEstrictoDisabled	= false;
				$scope.inputs.menorEstrictoDisabled	= true;
				break; 
		}			
	});
	
	$scope.$watch('inputs.igualB',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igualB 					= true; 
				$scope.inputs.distintoB 				= false;
				$scope.inputs.mayorEstrictoB 			= false;
				$scope.inputs.menorEstrictoB 			= false;
				$scope.inputs.valorCondicionB 			= "=";
				$scope.inputs.igualDisabledB	 		= true;	  	
				$scope.inputs.distintoDisabledB			= false;
				$scope.inputs.mayorEstrictoDisabledB	= false;
				$scope.inputs.menorEstrictoDisabledB	= false;
				break;
		}			
	});
	$scope.$watch('inputs.distintoB',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igualB 					= false; 
				$scope.inputs.distintoB 				= true;
				$scope.inputs.mayorEstrictoB 			= false;
				$scope.inputs.menorEstrictoB 			= false;
				$scope.inputs.valorCondicionB 			= "<>";
				$scope.inputs.igualDisabledB	 		= false;	  	
				$scope.inputs.distintoDisabledB			= true;
				$scope.inputs.mayorEstrictoDisabledB	= false;
				$scope.inputs.menorEstrictoDisabledB	= false;
				break;
		}			
	});
	$scope.$watch('inputs.mayorEstrictoB',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igualB 					= false; 
				$scope.inputs.distintoB 				= false;
				$scope.inputs.mayorEstrictoB		 	= true;
				$scope.inputs.menorEstrictoB 			= false;
				$scope.inputs.valorCondicionB 			= ">";
				$scope.inputs.igualDisabledB	 		= false;	  	
				$scope.inputs.distintoDisabledB			= false;
				$scope.inputs.mayorEstrictoDisabledB	= true;
				$scope.inputs.menorEstrictoDisabledB	= false;
				break;
		}			
	});
	$scope.$watch('inputs.menorEstrictoB',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.inputs.igualB 					= false; 
				$scope.inputs.distintoB 				= false;
				$scope.inputs.mayorEstrictoB 			= false;
				$scope.inputs.menorEstrictoB 			= true;
				$scope.inputs.valorCondicionB 			= "<";
				$scope.inputs.igualDisabledB	 		= false;	  	
				$scope.inputs.distintoDisabledB			= false;
				$scope.inputs.mayorEstrictoDisabledB	= false;
				$scope.inputs.menorEstrictoDisabledB	= true;
				break; 
		}			
	});
	
	$scope.cargarFiltros	= function() {
		
		datosPredefinidos = $scope.grid.datosAsientoSeleccionado.DatosPreDefinidos;
		filtersValoresFiltros.p_IdTipoDatoEvento = $scope.grid.datosAsientoSeleccionado.IdTipoDatoEvento;
		if( datosPredefinidos === "S")
		{			
			$scope.call("12006",filtersValoresFiltros, success_fnCombo);
		}
		else
		{
			$scope.inputs.valorDato = "";
			$scope.inputs.valorCombo = "";
			$scope.comboboxEventos = [];
			$scope.inputs.comboDisabled = true;			
			$scope.inputs.inputDisabled = false;
		}
			
	}
	
	$scope.cargarFiltrosB	= function() {
		
		datosPredefinidos = $scope.grid.datosBAsientoSeleccionado.DatosPreDefinidos;
		filtersValoresFiltros.p_IdTipoDatoEvento = $scope.grid.datosBAsientoSeleccionado.IdTipoDatoEvento;
		if( datosPredefinidos === "S")
		{
			$scope.call("12006",filtersValoresFiltros, success_fnComboB);	
		}
		else
		{
			$scope.inputs.valorDatoB = "";
			$scope.inputs.valorComboB = "";
			$scope.comboboxEventosB = [];
			$scope.inputs.comboBDisabled = true;
			$scope.inputs.inputBDisabled = false;
		}
			
	}
	
	$scope.agregarCondicionComprobante	=	function(){
		if($scope.inputs.valorCondicion !== undefined)
		{
			if($scope.inputs.valorCombo !== undefined && $scope.inputs.valorCombo !== "")
			{
				var params = $scope.obtenerParametrosCondicionComprobante($scope.inputs.valorCombo.Valor,0);
				$scope.call("12034",params,success_condicion);
			}
			else
				if($scope.inputs.valorDato !== undefined && $scope.inputs.valorDato !== "")
				{
					var params = $scope.obtenerParametrosCondicionComprobante($scope.inputs.valorDato,0);
					$scope.call("12034",params,success_condicion);
				}
				else
					bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_AGREGAR_UN_VALOR_A_LA_CONDICION'));
		}
		else
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UNA_CONDICION'));
	}
	
	$scope.eliminarCondicionComprobante	=	function(seleccionadoCondComprobante){
		 
			if(seleccionadoCondComprobante !== undefined)
			{	
				//var params = $scope.obtenerParametrosCondicionComprobante("",0, "E");							
				//var params = $scope.obtenerParametrosCondicionComprobante("",0, "E");
				
				if (seleccionadoCondComprobante.length > 0)
				{		
					$.each(seleccionadoCondComprobante,function(key,value){											
						
						var params = {};	
						
						params.p_IdEmpresa				=	$scope.SesionIdEmpresa;       		
						params.p_IdUsuario          	=	$scope.SesionIdUsuario;	
						params.p_IdModulo           	=	"CON";						
						params.p_UsaReglasNegocio		= 	"N";
					    params.p_IdCondicion			=	value.IdCondicion;									
						params.p_TipoConsulta			=   value.TipoCondicion;
						
						$scope.call("12055",params,success_condicion);	
					
					});					
				}
				}
			else
				bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_CONDICION'));
	}
	
	$scope.eliminarCondicionComprobanteB	=	function(seleccionadoCondAsiento){
		 
		if(seleccionadoCondAsiento !== undefined)
		{	
			//var params = $scope.obtenerParametrosCondicionComprobante("",0, "E");							
			//var params = $scope.obtenerParametrosCondicionComprobante("",0, "E");
			
			if (seleccionadoCondAsiento.length > 0)
			{		
				$.each(seleccionadoCondAsiento,function(key,value){											
					
					var params = {};	
					
					params.p_IdEmpresa				=	$scope.SesionIdEmpresa;       		
					params.p_IdUsuario          	=	$scope.SesionIdUsuario;	
					params.p_IdModulo           	=	"CON";						
					params.p_UsaReglasNegocio		= 	"N";
				    params.p_IdCondicion			=	value.IdCondicion;									
					params.p_TipoConsulta			=   value.TipoCondicion;
					
					$scope.call("12055",params,success_condicionB);	
				
				});					
			}
			}
		else
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_CONDICION'));
}
	
	$scope.obtenerParametrosCondicionComprobante = function(valorVarchar, valorDecimal){
		var params = {};
		params.p_IdEmpresa				=	$scope.SesionIdEmpresa;       		
		params.p_IdUsuario          	=	$scope.SesionIdUsuario;	
		params.p_IdModulo           	=	"CON";
		params.p_UsaReglasNegocio		=	"N";
		params.p_IdDefinicionAsiento	=	0;
		params.p_IdTipoContabilidad		=	$scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;
		params.p_IdTipoDatoEvento		=	$scope.grid.datosAsientoSeleccionado.IdTipoDatoEvento ? $scope.grid.datosAsientoSeleccionado.IdTipoDatoEvento : "" ;
		params.p_ValorVarchar			=	valorVarchar;
		params.p_ValorDecimal			=	valorDecimal;
		params.p_Condicion				=	$scope.inputs.valorCondicion;
		params.p_Conector				=	"AND";
		params.p_TipoCondicion			=	"C";
		params.p_Estado					=	$scope.grid.seleccionadoTiposComprobante.Estado;//"V";
		params.p_OmiteCondicionCmbte	=	"";
		return params;
	}
		
	$scope.agregarCondicionAsiento	=	function(){
		if($scope.inputs.valorCondicionB !== undefined)
		{
			if($scope.grid.seleccionadoAsiento !== undefined)
			{
				if($scope.inputs.valorComboB !== undefined && $scope.inputs.valorComboB !== "")
				{
					var params = $scope.obtenerParametrosCondicionAsiento($scope.inputs.valorComboB.Valor,0);
					$scope.call("12034",params,success_condicionB);
				}
				else
					if($scope.inputs.valorDatoB !== undefined && $scope.inputs.valorDatoB !== "")
					{
						var params = $scope.obtenerParametrosCondicionAsiento($scope.inputs.valorDatoB,0);
						$scope.call("12034",params,success_condicionB);
					}
					else
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_AGREGAR_UN_VALOR_A_LA_CONDICION'));
			}
			else
				bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_ASIENTO'));
		}
		else
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UNA_CONDICION'));
	}
	
	$scope.obtenerParametrosCondicionAsiento = function(valorVarchar, valorDecimal, estado){
		var params = {};
		params.p_IdEmpresa				=	$scope.SesionIdEmpresa;       		
		params.p_IdUsuario          	=	$scope.SesionIdUsuario;	
		params.p_IdModulo           	=	"CON";
		params.p_UsaReglasNegocio		=	"N";
		params.p_IdDefinicionAsiento	=	$scope.grid.seleccionadoAsiento.IdDefinicionAsiento;
		params.p_IdTipoContabilidad		=	0;
		params.p_IdTipoDatoEvento		=	$scope.grid.datosBAsientoSeleccionado.IdTipoDatoEvento;
		params.p_ValorVarchar			=	valorVarchar;
		params.p_ValorDecimal			=	valorDecimal;
		params.p_Condicion				=	$scope.inputs.valorCondicionB;
		params.p_Conector				=	"AND";
		params.p_TipoCondicion			=	"A";
		params.p_Estado					=	"V";
		params.p_OmiteCondicionCmbte	=	"";
		return params;
	}
		
	/*************** Fin Botonera Configurar Asientos ***********************/
	
	/********** Eventos grillas ************/
	
	$scope.cargarAsientosCentralizaciones = function() {
		
		$scope.grid.seleccionadoAsiento			= undefined;
		
		filtersAsComp.p_IdTipoContabilidad      = $scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;	
		dsAsientosComprobante.setCallParameters(filtersAsComp);	
		
		filtersCent.p_IdEmpresa          		= $scope.SesionIdEmpresa;
		filtersCent.p_IdTipoContabilidad      	= $scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;	
		filtersCent.p_CodigoUsuario				= $scope.SesionIdUsuario;
		dsCentralizacionesRequeridas.setCallParameters(filtersCent);
		
		$scope.filtros.tipoContabilizacion		= $scope.grid.seleccionadoTiposComprobante.Nombre;
		$scope.filtros.tipo 					= $scope.grid.seleccionadoTiposComprobante.Tipo;
		$scope.filtros.tipoNombre 				= $scope.grid.seleccionadoTiposComprobante.Tipo === 'T' ? $translate.instant('UI_TRASPASO').toUpperCase() : '';
		$scope.filtros.estado					= $scope.grid.seleccionadoTiposComprobante.NombreEstado;
		
		filterCondComp.p_IdTipoContabilidad		= $scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;
		filterCondComp.p_Estado					= $scope.grid.seleccionadoTiposComprobante.Estado;
		
		dsCondicionesComprobante.setCallParameters(filterCondComp);
		
		filterAsDatos.p_TipoConsulta			=	"CONTABILIZACION";
		filterAsDatos.p_IdTipoContabilidad		=   $scope.grid.seleccionadoTiposComprobante.IdTipoContabilidad;
		dsAsientosDatos.setCallParameters(filterAsDatos);
		dsAsientosDatosB.setCallParameters(filterAsDatos);
		
		dsAsientos.setCallParameters(filtersAsComp);
		
		filterCondAs.p_IdDefinicionAsiento		= null;
		
		//dsCondicionesAsiento.setCallParameters(filterCondAs);
		
	};
	
	$scope.cargarAsientosCondicionesEventos  = function() {
		
		filterCondAs.p_IdDefinicionAsiento		= $scope.grid.seleccionadoAsiento.IdDefinicionAsiento;		
		dsCondicionesAsiento.setCallParameters(filterCondAs);		
		filterEventAsiento.p_IdDefinicionAsiento = $scope.grid.seleccionadoAsiento.IdDefinicionAsiento;	
		dsEventosAsiento.setCallParameters(filterEventAsiento);
	};
	
	/********** Fin Eventos grillas ************/
	
	$scope.refrescarPlanCuentas = function(){
		$scope.gridPlanCuentas.dataSource.read();
		$scope.gridPlanCuentas.refresh();
	}
	
	$scope.actualizarPlanCuentas = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdModulo  : "CON",
			p_IdUsuario : $scope.SesionIdUsuario
		};
		$scope.call("601111", params, function(respuesta){
			bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	}
	
});
