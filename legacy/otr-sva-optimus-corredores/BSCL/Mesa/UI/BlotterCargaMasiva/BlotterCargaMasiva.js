angular.module('ModuloMesa').controller('BlotterCargaMasivaMesaCtrl',
			function($scope,$translate,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	$scope.data.oConfiguracionMesa={
		filtros : {
			IdTipoProducto	: { "field"   : "IdTipoProducto","operator": "eq", "value" : "MERF"}
		}
	};

	$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	var gridCargaMasiva;
	
	//------------------------------------------------
	// CONFIGURACION COMBOBOX
	//------------------------------------------------

	$scope.oConfiguracionTipoCargaMasiva = {
			tipo 			: "combobox-tipo-carga-masiva-mesa-rf",
			configuracion : {},
			filtros : { 
					 IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "ACT" }
					},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true 		
	};
	 
	
	$scope.oConfiguracionEstado = {
			tipo : "combobox-estado-mesa",
			configuracion	: {},
			filtros 		: {
								 NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   : 'MES.CargaMasiva'},
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};
	
	

	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	$scope.confBasicaCargaMasiva = {
			dataSource			: new bcs.DataSource({
									type : "odata",
									definition : {
										serverFiltering 	: true, // Filtrado de datos en el servidor.
										serverPaging 		: true, // Paginado de los datos en el servidor.
										serverSorting 		: false, // Ordenamiento de los datos en el servidor.
										sort 				: { field: "IdCargaMasivaDetalle", dir: "desc" },
										transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=900211",dataType : "json"}},
										schema : {
											model : {
												fields : {
													IdCargaMasiva				: { type: "number"},
													FechaCarga					: { type: "sysdate"},
													FechaProceso				: { type: "sysdate"},
													CodigoUsuario				: { type: "string"},
													CantidadRegistros			: { type: "number"},
													MontoTotal					: { type: "number"},
													NombreArchivo				: { type: "string"},
													NombreEstadoCargaMasiva		: { type: "string"},
													CodigoTipoCargaMasiva		: { type: "string"},
													IdMesa						: { type: "number"},
													NombreMesa					: { type: "string"}
												}
											}
										}
									}
								}),		
			columns				: [
								     {field: "IdCargaMasiva", 				title: "UI_FOLIO_CARGA",        width: "40px"}
								    ,{field: "FechaCarga",				    title: "UI_FECHA_CARGA", 		width: "65px",	groupable: true }
									,{field: "CantidadRegistros", 			title: "UI_CANTIDAD_REGISTROS", width: "80px"}
									,{field: "NombreMesa", 					title: "UI_MESA", 				width: "100px",  groupable: true }
									,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "75px",  groupable: true }
									,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "65px",	groupable: true }
									,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "70px",  groupable: true }
									,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO",  groupable: true,   attributes:{style:"white-space:nowrap"} }
			
			],		
			sortable			: true,
			pageable			: true,		
			height				: 320,
			selectable 			: "row",
			userSettings 		: true,
			rowColorBackground	: function(row) { 
									  var color = "";
									  if (row.IdEstado == 'ING')
										 color = "yellow";
									  if (row.IdEstado == 'INV')
											 color = "red";
									  if (row.IdEstado == 'PRO')
											 color = "green";									  
									  return color;
								  },
			gridCreated: function(g)
			{	
				gridCargaMasiva = g;
			}
	};
	
	
	
	// --------------------------------------------------
	// FUNCIONES 
	// --------------------------------------------------
	$scope.buscarCargaMasiva = function(){
		var filters = new Array();
		
		if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva)
			filters.push({ field: "IdTipoCargaMasiva", operator: "eq", value: Number($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva || 0)});		

		if ($scope.kngFechaCargaDesde)			
			filters.push({ field: "FechaCarga",        operator: "gte", value: util.dateSPdate($scope.kngFechaCargaDesde) });
			
		if ($scope.kngFechaCargaHasta)			
			filters.push({ field: "FechaCarga",        operator: "lte", value: util.dateSPdate($scope.kngFechaCargaHasta) });		
		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado && $scope.oConfiguracionEstado.seleccionado.IdEstado != ' TODOS')
			filters.push({ field: "IdEstadoCargaMasiva", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado});
		
		if ($scope.data.oConfiguracionMesa.seleccionado.IdMesa)
			filters.push({ field: "IdMesa", operator: "eq", value: Number($scope.data.oConfiguracionMesa.seleccionado.IdMesa || 0)});
		
		$scope.confBasicaCargaMasiva.dataSource.filter(filters);	
		
	};
	

	
	$scope.limpiarFiltros = function(){
		$scope.oConfiguracionTipoCargaMasiva.idActual = false;
		$scope.kngFechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaCargaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.confBasicaCargaMasiva.dataSource.removeAll();
	};
	
		
	//------------------------------------------------------
	//CARGAR DATA DE ARCHIVO EN BD (sin procesar aun)
	//------------------------------------------------------
	$scope.cargarArchivo = function(){
		
		 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === 0 || $scope.oConfiguracionTipoCargaMasiva.seleccionado.IdTipoCargaMasiva === undefined || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === '' || $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === undefined)
		 {
			bcsComunes.ventanaMsg({mensaje: $translate.instant('DEBE_INDICAR_TIPO_CARGA')},$scope.IdVentana);
			return;			
		 };
		 
		 var datosArchivo = {codigoArchivo: $scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva || ''};		 
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,$scope.IdVentana);			
		 var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 
			 var countRegistrosInvalidos = 0;
			 $.each(data.fileData, function(key, value){
			     if (value.MENSAJEVALIDACION.toString() != "OK")
			     {
			    	countRegistrosInvalidos++;
			     }
			});
			 
			 if (countRegistrosInvalidos>0)
			 {
				 bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_ARCHIVO_CON_REGISTROS_INVALIDOS')},$scope.IdVentana);
				 return; 
			 }
			 
						 
			 if ($scope.oConfiguracionTipoCargaMasiva.seleccionado.CodigoTipoCargaMasiva === 'CARGA_OPERACIONES_A_TERMINO_RF')
			 {
				 var operaciones = [];
				 $.each(data.fileData, function(key, value){
					    operaciones.push(value);		    
				 });
				 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
				 pregunta.result.then(function(result){
						if (result===OPCIONES_BCSALERTA.ACEPTAR){
	
							bcsComunes.ventanaOverlay($scope.IdVentana, true);
	
							var parametros = {};
							parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
							parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
							parametros.p_IdModulo                	= 'MRF';
							parametros.p_UsaReglasNegocio			= 'S';
							parametros.p_SimularOperacion			= 'N',
							parametros.p_NombreArchivo				= data.fileName;
							parametros.p_IdMesa						= $scope.data.oConfiguracionMesa.seleccionado.IdMesa;
							parametros.p_Operaciones 				= operaciones;
							
							bcsComunes.log().info(JSON.stringify(parametros));
							bcsServiciosMesa.CargaMasivaArchivoOperacionTermino(parametros,
								function(respuesta){
									bcsComunes.ventanaOverlay($scope.IdVentana, false);
									 		
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
											conflictos:util.parseServerResponse(respuesta)
										},$scope.IdVentana);
										$scope.buscarCargaMasiva();									
									} else {	
										bcsComunes.ventanaAbrir('VentanaResultado',{usaNuevosMensajes : true , usaCampoSecuenciaTipoString: true, titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "id" , nombreSecuencia : 'UI_FOLIO_AGRUPADOR_CARTERA', datos : respuesta.P_OUT_ERRORES } , false, false, $scope.IdVentana);
									}
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.IdVentana);
									bcsComunes.ventanaOverlay($scope.IdVentana, false);
								}
							);
						}
						else
						{
							return; 
						}
				 });								      
			 }
  		}); 
	};
	
	$scope.detalleCargaMasiva = function (seleccion)
	{	
		if(seleccion.CodigoTipoCargaMasiva === 'CARGA_OPERACIONES_A_TERMINO_RF') {
			bcsComunes.ventanaAbrir("DetalleCargaOperacionTermino", seleccion, false, false,$scope.IdVentana );
		}
	};	
	
	
});
