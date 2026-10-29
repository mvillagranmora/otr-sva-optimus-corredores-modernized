angular.module('ModuloOrdenes').controller('MaestroLibroOrdenesCtrl',
		function($scope,bcsComunes,$translate,$resource,bcsConsultasOrdenes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionOrdenesV2){

/******************************************************
 * INICIO DE VARIABLES
 * ***************************************************/
	var mlo = this;
	mlo.idVentana = bcsComunes.ventanaWinId($scope);
	mlo.formData = {};
	$scope.data = {};
	
	mlo.agregar = function() {
		
		var id1 = bcsComunes.ventanaAbrir("agregarModificarLibroOrdenes",{ TipoLibSel: undefined, AccionBoton : 'Agregar' },false,false,mlo.idVentana);
		
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){ 
        	if (data === true){
        		mlo.libroOrdenesSeleccionado = undefined;
        		mlo.buscar();
			}
    	});
		
	};
	mlo.modificar = function(tipoLibSel) {
		if(tipoLibSel != undefined){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarLibroOrdenes",{ TipoLibSel: tipoLibSel, AccionBoton : 'Modificar' },false,false,mlo.idVentana);
			
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(data){ 
	        	if (data === true){
	        		mlo.libroOrdenesSeleccionado = undefined;
	        		mlo.buscar();
				}
	    	});
		}
	};
	mlo.verOrdenes = function() {
		bcsComunes.ventanaAbrir("ConsultarOrdenes",{ IdLibroOrdenes : mlo.libroOrdenesSeleccionado.IdLibroOrdenes },false,false,mlo.idVentana);
	}
	mlo.cambioEstado = function(tipoLibSel, accion) {
		if(accion === 'ELI'){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_LIBRO_ORDENES"));
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					cambioEstadoLibroOrdenes(tipoLibSel, accion, 'Borrado');
				}
			});
			return;
		}
		else if(accion === 'INA' || accion === 'ACT'){
			cambioEstadoLibroOrdenes(tipoLibSel, accion, accion==='INA'?'Inactivo':'Activo');
		}
		else return;
	};
	/*  [CELCONDOR-687]  - Eliminar pestana operadores de la configuracion del libro de ordenes
	mlo.agregarOperador = function(tipoLibSel){
		mlo.abrirModalSubGrilla(tipoLibSel, 'OPE', 'agregarLibroOrdenesOperadores', 'Agregar');
	}
	 [CELCONDOR-687]  - Eliminar pestana operadores de la configuracion del libro de ordenes
*/
	mlo.agregarMarcasNegociacion = function(tipoLibSel){
		mlo.abrirModalSubGrilla(tipoLibSel, 'MAR', 'agregarLibroOrdenesMarcasNegociacion', 'Agregar');
	}
	
	mlo.eliminarElementoSubGrilla = function(tipoElemSel, elemento){
		var t;
		var elementoMsj = '';
		if(elemento === 'OPE'){
			t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_LIBRO_ORDENES_OPERADOR"));
			elementoMsj = 'Operador';
		}
		/*else if(elemento === 'SEG'){
			t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_LIBRO_ORDENES_SEGMENTO"));
			elementoMsj = 'Segmento';
		}*/
		else if(elemento === 'MAR'){
			t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_LIBRO_ORDENES_MARCAS_NEGOCIACION"));
			elementoMsj = 'Marca Negociación';
		}
		else return;
		
		t.result.then(function(result){
			if(result === OPCIONES_BCSALERTA.SI){
				cambioEstadoLibroOrdenesDetalle(tipoElemSel, elemento, elementoMsj);
			}
		});
		return;
	}
	
/*********************************************
 * DATASOURCES
 *********************************************/
	
	mlo.gridLibroOrdenes = {
  		selectable : "row",
  		dataSource : new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarLibroOrdenes()),
  		columns :
		[
		 	{field: "NombreLibroOrdenes",		title: $translate.instant('UI_NOMBRE'),							width: "100px"},
		 	{field: "NombreTipoLibroOrden",		title: $translate.instant('UI_TIPO_LIBRO'),						width: "100px"},
		 	{field: "AsignacionAutomaticaTranslate", title: $translate.instant('UI_ASIGNACION_AUTOMATICA'),		width: "80px"},
		 	{field: "NombreTipoAsignacion",		title: $translate.instant('UI_PROCESO_ASIGNACION_AUTOMATICA'),	width: "100px"},
		 	{field: "LibroPorDefectoTranslate",	title: $translate.instant('UI_LIBRO_POR_DEFECTO'),				width: "80px"},
		 	{field: "NombreEstado",				title: $translate.instant('UI_ESTADO'),							width: "70px"}
		],
		gridCreated: function(grid) {
			$scope.data.mainGrid = grid;
		},
		detailExpand : function(e) { 
			this.collapseRow(this.tbody.find(' > tr.k-master-row').not(e.masterRow));
			this.clearSelection();
			this.select(e.masterRow);
			
			mlo.operadorSeleccionado = undefined;
			mlo.segmentoSeleccionado = undefined;
			mlo.marcasNegociacionSeleccionado = undefined;
		},
		autoBind:true,
		height:400,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		}
	};
	/*  [CELCONDOR-687]  - Eliminar pestana operadores de la configuracion del libro de ordenes
	// TAB OPERADORES
	mlo.gridOperadores = function(dataItem){
		return {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					pageSize		: 50,
	            	transport: {
	                	read: {
	                		url:bcsConsultasOrdenes('ConsultarLibroOrdenesOperadores'),
	                		dataType: "json"
	                	}
	            	},
					schema: {
						model: {
							fields: {
								IdLibroOrdenesOperador	: { type: "number"},
								IdEmpresa				: { type: "number"},
								IdLibroOrdenes			: { type: "number"},
								CodigoOperador			: { type: "string"}
							}
						}
					},
					filter : [ 
		   			          	// Configuración de filtros sobre los datos en el detalle.
						        { field: "IdLibroOrdenes",		operator: "eq", value: Number(dataItem.IdLibroOrdenes)},
						        { field: "IdEmpresa",			operator: "eq", value: $scope.SesionIdEmpresa}
							]
				}
			}),
			columns: [
			    {field: "CodigoOperador", title: $translate.instant('UI_CODIGO_OPERADOR'), attributes: {style:"text-align:left;"}, width: "100px"}
			],	
			sortable	: false,
			pageable	: false,
			autoBind	: true,
			height		: 100,
		    selectable	: "row"
		}
	};
	 [CELCONDOR-687]  - Eliminar pestana operadores de la configuracion del libro de ordenes
	*/
	// TAB MARCAS NEGOCIACION
	mlo.gridMarcasNegociacion = function(dataItem) {
		return {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					pageSize		: 50,
	            	transport: {
	                	read: {
	                		url:bcsConsultasOrdenes('ConsultarLibroOrdenesMarcasNegociacion'),
	                		dataType: "json"
	                	}
	            	},
					schema: {
						model: {
							fields: {
								IdMarcaNegociacion	: { type: "number"},
								IdEmpresa			: { type: "number"},
								IdLibroOrdenes		: { type: "number"},
								MarcaNegociacion	: { type: "string"}
							}
						}
					},
					filter : [
						        { field: "IdLibroOrdenes",	operator: "eq", value: Number(dataItem.IdLibroOrdenes)},
						        { field: "IdEmpresa",  		operator: "eq", value: $scope.SesionIdEmpresa}
						]
				}
			}),
			columns: [
			    {field: "MarcaNegociacion", title: $translate.instant('UI_MARCA'), attributes: {style:"text-align:left;"}, width: "100px"}
			],	
			sortable	: false,
			pageable	: false,
			autoBind	: true,
			height		: 100,
		    selectable	: "row"
		}
	};
	
/*********************************************
 * COMBOBOX
 *********************************************/

	
/*********************************************
 * GRILLAS
 *********************************************/
	
	
/*********************************************
 * FUNCIONES
 ********************************************/

	mlo.buscar = function(){
		var objfilters = { };
		var niv1filters = new Array();
		objfilters.logic = 'and';
		objfilters.filters = niv1filters;
    	
		niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		if (mlo.formData.Nombre){
			niv1filters.push({ field: "tolower(NombreLibroOrdenes)", operator: "contains", value: mlo.formData.Nombre.toLowerCase() });
    	}
    	if (mlo.formData.TipoLibro){
    		niv1filters.push({ field: "tolower(IdTipoLibroOrden)", operator: "eq", value: mlo.formData.TipoLibro.toLowerCase() });
    	}
    	
    	if (niv1filters.length>0) mlo.apiGridLibroOrdenes.dataSource.filter(objfilters);
    	else mlo.apiGridLibroOrdenes.dataSource.filters(new Array());
    	
    	mlo.libroOrdenesSeleccionado = undefined;
	};
	
	mlo.limpiar = function(){
		mlo.formData.Nombre	= '';
		mlo.formData.TipoLibro = 0;
	}
	
	mlo.abrirLibroPorDefecto = function(){
		var id1 = bcsComunes.ventanaAbrir("libroPorDefecto",{},false,false,mlo.idVentana);
		
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){ 
        	if (data === true){
        		mlo.libroOrdenesSeleccionado = undefined;
        		mlo.buscar();
			}
    	});
	}
	
	// Se prepara llamada a SP para cambio de Estado de Libro de Ordenes.
	// Se procesa la respuesta del SP.
	function cambioEstadoLibroOrdenes(tipoLibSel, accion, accionMsj){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes	: tipoLibSel.IdLibroOrdenes,
			p_IdEstado			: accion,
			p_IdTipo			: '',
			p_Fecha				: new Date(),
			p_Comentario		: 'Libro Ordenes ' + accionMsj
		};
	    
		bcsComunes.ventanaOverlay(mlo.idVentana,true);
	    cambioEstadoLibroOrdenesSP(datos).then(
	    	function(result){
	        	if(result.P_OUT_ADVERTENCIASCONTADOR){
	        		bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        	}else if(result.P_OUT_ERRORESCONTADOR){
	        		bcsComunes.msg("error","Error","La operacion tiene errores" + result.P_OUT_ERRORESDETALLE[0].Codigo);
	        	}else{
	        		bcsComunes.msg("info","info","Libro de Órdenes :: " + accionMsj + " ::");
	        		mlo.libroOrdenesSeleccionado = undefined;
	        		mlo.apiGridLibroOrdenes.dataSource.read();
	        	}
	        	bcsComunes.ventanaOverlay(mlo.idVentana,false);
	        },
	        function(result){
	        	bcsComunes.msg("error","Error","La operacion tiene errores");
	        	bcsComunes.ventanaOverlay(mlo.idVentana,false);
	        });
	}
	
	// Llamada a SP para cambio de Estado
	var cambioEstadoLibroOrdenesSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=213";
		var r = $resource("", {}, {'urlConsulta': {method:"POST", url:urlConsulta}});
		var post = r.urlConsulta(p_datos);
		
		return post.$promise;
    };
    
    
    // Ventana Modal para accion Agregar de las Sub-Grillas de Libro de Ordenes
    mlo.abrirModalSubGrilla = function(p_TipoLibSel, p_elemento, p_ventana, p_accion){
		var id1 = bcsComunes.ventanaAbrir(p_ventana,{ TipoLibSel: p_TipoLibSel, AccionBoton : p_accion },false,false,mlo.idVentana);
		
        var promesa=bcsComunes.ventanaPromesa(id1);
        promesa.then(function(data){ 
        	if (data === true){
        		if(p_elemento === 'OPE'){
        			mlo.operadorSeleccionado = undefined;
        			mlo.apiGridOperadores.dataSource.read();
        		}
        		/*else if(p_elemento === 'SEG'){
        			mlo.segmentoSeleccionado = undefined;
        			$scope.data.detalleGridSegmento.dataSource.read();
        		}*/
        		else if(p_elemento === 'MAR'){
        			mlo.marcasNegociacionSeleccionado = undefined;
        			mlo.apiGridMarcasNegociacion.dataSource.read();
        		}
			}
    	});
	};
	
	// Se prepara llamada a SP para Eliminar un Elemento de alguna de las Sub-Grillas.
	// Se procesa la respuesta del SP.
	function cambioEstadoLibroOrdenesDetalle(p_tipoElemSel, p_elemento, p_elementoMsj){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes		 : p_tipoElemSel.IdLibroOrdenes,
			p_IdLibroOrdenesOperador : p_tipoElemSel.IdLibroOrdenesOperador ? p_tipoElemSel.IdLibroOrdenesOperador : 0,
			p_IdLibroOrdenesSegmento : p_tipoElemSel.IdLibroOrdenesSegmento ? p_tipoElemSel.IdLibroOrdenesSegmento : 0,
			p_IdMarcaNegociacion	 : p_tipoElemSel.IdMarcaNegociacion		? p_tipoElemSel.IdMarcaNegociacion : 0,
			p_IdEstado				 : 'INA',
			p_FechaModificacion		 : new Date(),
			p_Elemento				 : p_elemento
		};
	    
		bcsComunes.ventanaOverlay(mlo.idVentana,true);
	    cambioEstadoLibroOrdenesDetalleSP(datos).then(
	    	function(result){
	        	if(result.P_OUT_ADVERTENCIASCONTADOR){
	        		bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        	}else if(result.P_OUT_ERRORESCONTADOR){
	        		bcsComunes.msg("error","Error","La operacion tiene errores" + result.P_OUT_ERRORESDETALLE[0].Codigo);
	        	}else{
	        		bcsComunes.msg("info","info","Libro de Órdenes. Se elimina :: " + p_elementoMsj + " ::");
	        		
	        		if(p_elemento === 'OPE'){
	        			mlo.operadorSeleccionado = undefined;
	        			mlo.apiGridOperadores.dataSource.read();
	        		}
	        		else if(p_elemento === 'SEG'){
	        			mlo.segmentoSeleccionado = undefined;
	        			$scope.data.detalleGridSegmento.dataSource.read();
	        		}
	        		else if(p_elemento === 'MAR'){
	        			mlo.marcasNegociacionSeleccionado = undefined;
	        			mlo.apiGridMarcasNegociacion.dataSource.read();
	        		}
	        	}
	        	bcsComunes.ventanaOverlay(mlo.idVentana,false);
	        },
	        function(result){
	        	bcsComunes.msg("error","Error","La operacion tiene errores");
	        	bcsComunes.ventanaOverlay(mlo.idVentana,false);
	        });
	}
	
	// Llamada a SP para cambio de Estado de elementos de la Sub-Grilla
	var cambioEstadoLibroOrdenesDetalleSP = function(p_datos){
		var urlCambioEstadoLibroOrdenesDetalleSP = "/BSCL/Plataforma/GWJAVA/PLA/call?t=220";
		var r = $resource("", {}, {'urlCambioEstadoLibroOrdenesDetalleSP': {method:"POST", url:urlCambioEstadoLibroOrdenesDetalleSP}});
		var post = r.urlCambioEstadoLibroOrdenesDetalleSP(p_datos);
		
		return post.$promise;
    };
    
    mlo.noAbrirModal = function(){
    	mlo.libroOrdenesSeleccionado = undefined;
    	return;
	}
});