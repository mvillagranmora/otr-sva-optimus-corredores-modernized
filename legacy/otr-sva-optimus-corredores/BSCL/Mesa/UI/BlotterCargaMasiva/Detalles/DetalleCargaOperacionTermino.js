angular.module('ModuloMesa').controller('DetalleCargaOperacionTerminoCtrl',
			function($scope,$translate,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	var parametros = bcsComunes.ventanaParams($scope.IdVentana);
	var gridCargaMasiva;
	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	$scope.confBasicaOperacionesTermino = {
			dataSource			: new bcs.DataSource({
									type : "odata",
									definition : {
										serverFiltering 	: true, // Filtrado de datos en el servidor.
										serverPaging 		: true, // Paginado de los datos en el servidor.
										serverSorting 		: false, // Ordenamiento de los datos en el servidor.
										sort 				: { field: "IdCargaMasivaDetalle", dir: "desc" },
										transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=900208",dataType : "json"}},
										schema : {
											model : {
												fields : {
													IdTipoCargaMasiva			: { type: "number"},
													IdCargaMasiva				: { type: "number"},
													IdCargaMasivaDetalle		: { type: "number"},								
													FolioAgrupadorCartera		: { type: "string"},
													SecuenciaOperacion			: { type: "number"},
													DocumentoIdentidad			: { type: "string"},
													CodigoPortafolio			: { type: "string"},
													TipoOperacion				: { type: "string"},
													TipoReajuste				: { type: "string"},
													ValorInicial				: { type: "number"},
													CodigoUsuario				: { type: "string"},
													FechaCarga					: { type: "sysdate"},
													IdEstado					: { type: "string"}
												}
											}
										}
									}
								}),		
			columns				: [
								    {field: "IdCargaMasivaDetalle", 		title: "UI_ID", width: "120px"}
									,{field: "FolioAgrupadorCartera", 		title: "UI_FOLIO_AGRUPADOR_CARTERA", width: "120px"}
								    ,{field: "SecuenciaOperacion",			title: "UI_SECUENCIA_OPERACION", width: "120px",	groupable: true }
									,{field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD", width: "120px"}
									,{field: "CodigoPortafolio", 			title: "UI_CODIGO_PORTAFOLIO",   width: "100px", }
									,{field: "TipoOperacion", 				title: "UI_TIPO_OPERACION", 	width: "120px",  groupable: true }
									,{field: "TipoReajuste", 				title: "UI_TIPO_REAJUSTE", 		width: "100px" }
									,{field: "ValorInicial", 				title: "UI_VALOR_INICIAL", 		width: "100px",  template: " <label> #:  (util.bcs_formato(ValorInicial,'GEN','Monto',TipoReajuste, '','')).valor #</label>"}
									,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "60px",  groupable: true }
									,{field: "FechaCarga", 					title: "UI_FECHA_CARGA", 		width: "70px" }
			
			],		
			sortable			: true,
			pageable			: true,		
			height				: 320,
			selectable 			: "multiple, row",
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
				$scope.buscarOperacionesTermino();
				
			}
	};
	
	$scope.confBasicaOperacionesTerminoDetalle = function (item){
			var dsDef = {
		       	type : "odata",
		       	definition : {
		  			serverFiltering : true,
		  			serverPaging : true,
		  			pageSize : 10,
		  			filterable : true,
		            transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=900209",
							dataType: "json"
						}
					},
		  			schema : {
								model : {
									fields : {
										IdCargaMasivaDetalle		: { type: "number"},
										Mercado						: { type: "string"},
										SecuenciaCartera			: { type: "number"},								
										Nemotecnico					: { type: "string"},
										Emisor						: { type: "string"},
										Boveda						: { type: "string"},
										ReajusteCartera				: { type: "string"},
										MonedaLiquidacion			: { type: "string"},
										FechaVencimiento			: { type: "sysdate"},
										Cantidad					: { type: "number"},
										ValorRescate				: { type: "number"},
										Fungible					: { type: "string"},
										Tasa						: { type: "number"},
										Monto						: { type: "number"},
										IdCargaMasivaDetalleCabecera: { type: "number"},
										ObservacionValidacion		: { type: "string"},
									}
								}
							},
		       }
			};
			dsDef.definition.filter = [
	           { field: "IdCargaMasivaDetalleCabecera", operator: "eq", value: Number(item.IdCargaMasivaDetalle) }
			];                       
			return {
				dataSource			: new bcs.DataSource(dsDef),
				columns				: [
								    {field: "IdCargaMasivaDetalle", 	title: "UI_ID", width: "120px"}
									,{field: "Mercado", 				title: "UI_MERCADO",    		width: "120px"}
								    ,{field: "SecuenciaCartera",		title: "UI_FOLIO_CARTERA", 		width: "120px"}
									,{field: "Nemotecnico", 			title: "UI_NEMOTECNICO", 		width: "120px"}
									,{field: "Emisor", 					title: "UI_EMISOR",   			width: "100px"}
									,{field: "Boveda", 					title: "UI_BOVEDA", 			width: "120px"}
									,{field: "ReajusteCartera", 		title: "UI_TIPO_REAJUSTE", 		width: "100px"}
									,{field: "MonedaLiquidacion", 		title: "UI_MONEDA_LIQUIDACION", width: "100px"}
									,{field: "FechaVencimiento", 		title: "UI_FECHA_VENCIMIENTO", 	width: "100px"}
									,{field: "Cantidad", 				title: "UI_CANTIDAD", 			width: "100px",  template: " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',MonedaLiquidacion, '','')).valor #</label>" }
									,{field: "ValorRescate", 			title: "UI_VALOR_RESCATE", 		width: "100px",  template: " <label> #:  (util.bcs_formato(ValorRescate,'GEN','Monto',MonedaLiquidacion, '','')).valor #</label>" }
									,{field: "Fungible", 				title: "UI_FUNGIBLE", 			width: "100px" }
									,{field: "Tasa", 					title: "UI_TASA", 				width: "100px",  template: " <label> #:  (util.bcs_formato(Tasa,'GEN','Monto',MonedaLiquidacion, '','')).valor #</label>" }
									,{field: "Monto", 					title: "UI_MONTO_OPERACION", 	width: "100px",  template: " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',MonedaLiquidacion, '','')).valor #</label>"}
									,{field: "ObservacionValidacion", 	title: "UI_VALIDACION", 		width: "100px"}
									],		
				sortable			: true,
				groupable			: false,
				userSettings 		: true,
				allowCopy			: { delimeter: "\t" },				
				pageable			: true,
				autoBind			: true,
				height				: 150,
			}
		};
	
	// --------------------------------------------------
	// FUNCIONES 
	// --------------------------------------------------
	$scope.buscarOperacionesTermino = function(){
		var filters = new Array();
		
		filters.push({ field: "IdCargaMasiva", operator: "eq", value: parametros.IdCargaMasiva});
			
		
		$scope.confBasicaOperacionesTermino.dataSource.filter(filters);	
		
	};
	
	$scope.eliminarOperacion = function(operacionesTemp){
		 var operaciones = [];
		 $.each(operacionesTemp, function(key, value){
			    operaciones.push({IdCargaMasivaDetalle : value.IdCargaMasivaDetalle});		    
		 });
		 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARAN_OPERACIONES_TERMINO_CARGA') );		
		 pregunta.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
	
					bcsComunes.ventanaOverlay($scope.IdVentana, true);
	
					var parametros = {};
					parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
					parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
					parametros.p_IdModulo                	= 'MRF';
					parametros.p_UsaReglasNegocio			= 'S';
					parametros.p_SimularOperacion			= 'N',
					parametros.p_Operaciones 				= operaciones;
					
					bcsComunes.log().info(JSON.stringify(parametros));
					bcsServiciosMesa.EliminarRegistroCargaMasivaOperacionesTermino(parametros,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.IdVentana, false);
							 		
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
								$scope.buscarOperacionesTermino();
								$scope.seleccionOperacionTermino = undefined;							
							} else {	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
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
	};
	
	
	$scope.procesarOperacion = function(operacionesTemp){
		 var operaciones = [];
		 $.each(operacionesTemp, function(key, value){
			    operaciones.push({IdCargaMasivaDetalle : value.IdCargaMasivaDetalle});		    
		 });
		 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESAR_OPERACIONES_TERMINO_CARGA') );		
		 pregunta.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
	
					bcsComunes.ventanaOverlay($scope.IdVentana, true);
	
					var parametros = {};
					parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
					parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
					parametros.p_IdModulo                	= 'MRF';
					parametros.p_UsaReglasNegocio			= 'S';
					parametros.p_SimularOperacion			= 'N',
					parametros.p_Operaciones 				= operaciones;
					
					bcsComunes.log().info(JSON.stringify(parametros));
					bcsServiciosMesa.ProcesarCargaMasivaArchivoOperacionTermino(parametros,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.IdVentana, false);
	
				     		bcsComunes.ventanaAbrir('VentanaResultado',{usaNuevosMensajes : true , titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "id" , nombreSecuencia : 'UI_SECUENCIA', datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.winID);		
									
							if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
								$scope.buscarOperacionesTermino();
								$scope.seleccionOperacionTermino = undefined;							
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
	};
});
