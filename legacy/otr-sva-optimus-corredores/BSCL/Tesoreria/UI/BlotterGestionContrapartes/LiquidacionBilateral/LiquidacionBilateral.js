angular.module('ModuloTesoreria').controller('LiquidacionBilateralCtrl',
		function($scope,bcsComunes,$translate,bcsAlerta, OPCIONES_BCSALERTA,bcsServiciosTesoreria,$http)
{
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.btnActivo = false;

	
	//Seteo fecha de liquidacion con fecha de sistema
	$scope.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);	
 
	
	//---------------------------------------------------
	// OBTIENE DATOS DE LA EMPRESA
	//---------------------------------------------------	
	if ($scope.SesionIdEmpresa)
	{
		
			var dsEmpresa = new bcs.DataSource({
				type: "odata",
				
				definition 	: {			
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					transport 			: {
											read : {
												 url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
												 dataType : "json",
											  }
										  },
					
					filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
										  
					schema 				: {
							model: {
								fields : {
									IdEmpresa          : {type: "number"},
									DocumentoIdentidad : {type: "string"}
								}
							}
						}
										  
				},
			});
			
	        dsEmpresa.fetch().then(function(){
				 if (dsEmpresa.data().length <= 0 ) 
				 {
					 $scope.Empresa = $scope.SesionEmpresa;
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = dsEmpresa.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });
	};
	
	
	
	
	//---------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//---------------------------------------------------
	
	//Filtros para combo ClasificacionTipoInstrumento
	$scope.IdEstado				= { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
	$scope.Clasificacion		= [{ field: "Codigo", operator: "eq", value: 'RV' },  //Solo estos mercados, si se agregan más se debe editar SP para considerarlos
	                    		   { field: "Codigo", operator: "eq", value: 'RF' },
	                    		   { field: "Codigo", operator: "eq", value: 'IF' }]
	
	$scope.oConfiguracionClasificacionTipoInstrumento={
			tipo 			: "combobox-clasificacion-tipo-instrumento",
			configuracion 	: {},
			filtros			: {
				complejo : [$scope.IdEstado,$scope.Clasificacion],
			  	logic: "and",
			  	filters: [
				{
				    logic: "or",
				    filters: $scope.Clasificacion
				},
				$scope.IdEstado				
			  ]},			
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};


	$scope.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
			EsTransaccion 					: { "field": "EsTransaccion",  "operator": "eq", "value": "S" },
			IdEstado 						: { "field": "IdEstado",       "operator": "eq", "value": "VIG" },
		}
	};
	
	
	//CondicionLiquidacion
	$scope.oConfiguracionCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	
	
	if ($scope.SesionLocale === 'PE')
		$scope.ConsultarLiquidacionBilateral = 3085;  //BSCL.Tesoreria.BD.Servicios.LIQ::ConsultarLiquidacionBilateralPE
	else
		$scope.ConsultarLiquidacionBilateral = 1519;  //BSCL.Tesoreria.BD.Servicios.BIL::ConsultarLiquidacionBilateral
	
	
	//--------------------------------------------------
	// CONFIGURACION DE GRILLA
	//--------------------------------------------------	
	var obj = {};	
	
	//Grilla Principal (Resumen liquidación bilateral)
	obj.dataSourceLiquidacionBilateralResumen = new bcs.DataSource({
		type : "call",
		definition : {
			sort 				: { field: "IdAgrupacion", dir: "asc" },
			pageSize 			: 100,
			callParameters      : '',
			callTransaction 	: $scope.ConsultarLiquidacionBilateral,
			callResultSet 		: "P_OUT_LIQUIDACIONBILATERALRESUMEN",			
			schema : {
				model : {
					fields : {
						CodigoInscripcionCorredoraContraparte  	: {type : "string"}
		    			,NombreCortoCorredoraContraparte		: {type : "string"}
		    			,DocumentoIdentidadCorredoraContraparte : {type : "string"}				    			
		    			,IdBolsa						: {type : "string"}
		    			,IdClasificacionTipoInstrumento : {type : "string"}
		    			,IdTipoInstrumento				: {type : "string"}
		    			,CodigoCondicionDeLiquidacion   : {type : "string"}
		    			,IdMonedaLiquidacion			: {type : "string"}
		    			,TotalMontoCompra 				: {type : "number"}
		    			,TotalMontoVenta  				: {type : "number"}
		    			,IdAgrupacion  				    : {type : "number"}		    			
		    			,IdComprobanteTesoreria		    : {type : "number"}
					}
				}
			}
		}
	});


	obj.columnasGrillaLiquidacionBilateralResumen = function() {
		return  [
			
		    {field: "IdAgrupacion",         			title: "UI_GRUPO",             width: "41px"}    
		    //Datos de la Corredora contraparte
		    ,{title   : "UI_CONTRAPARTE", align: "center",
		           columns : [
		               	 {field: "NombreCortoCorredoraContraparte",          title: "UI_NOMBRE",    width: "250px"}
						,{field: "CodigoInscripcionCorredoraContraparte",    title: "UI_CODIGO",    width: "45px"}								
						,{field: "DocumentoIdentidadCorredoraContraparte",   title: "UI_RUT",       width: "68px"}
					 ]}
		 
			,{field: "IdBolsa",           				title: "UI_BOLSA",             width: "38px"}
			,{field: "IdClasificacionTipoInstrumento", 	title: "UI_CLASIFICACION",     width: "35px"}
			,{field: "IdTipoInstrumento",              	title: "UI_TIPO_INSTRUMENTO",  width: "55px"}
			,{field: "CodigoCondicionDeLiquidacion",    title: "UI_CONDICION_LIQUIDACION",  width: "35px"}			
			,{field: "IdMonedaLiquidacion",           	title: "UI_MONEDA",            width: "48px"}
			,{field: "TotalMontoCompra",                title: "UI_COMPRA",            width: "80px", template : " <label> #:  (util.bcs_formato(TotalMontoCompra,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"}
			,{field: "TotalMontoVenta",                 title: "UI_VENTA",             width: "80px", template : " <label> #:  (util.bcs_formato(TotalMontoVenta,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"}			
			,{field: "IdComprobanteTesoreria",          title: "UI_SECUENCIA_COMPROBANTE", width: "60px"}
		];
	};

		
	$scope.confBasicaLiquidacionBilateralResumen = {
			dataSource			: obj.dataSourceLiquidacionBilateralResumen,
			columns				: obj.columnasGrillaLiquidacionBilateralResumen(),		
			sortable			: true,
			pageable			: true,
			autoBind			: false,
			filterable			: true,
			height				: 450,
			selectable 			: "multiple row",				
			rowColorBackground	: function(row) {
					var color = "";
					if (row.IdComprobanteTesoreria === 0)
						color = "yellow";
					else
						color = "green";
					 
					return color;
			}		
					
	};


	//Sub-Grilla (Detalle liquidación bilateral)
	obj.dataSourceLiquidacionBilateralDetalle =  function() {
		return{
			type : "call",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "FechaSistemaCreacion", dir: "asc" },
				pageSize 			: 100,
				callParameters      : '',
				callTransaction 	: $scope.ConsultarLiquidacionBilateral,
				callResultSet 		: "P_OUT_LIQUIDACIONBILATERALDETALLE",			
				schema : {
					model : {
						fields : {
							 IdClasificacionTipoInstrumento : {type : "string"}
			    			,FechaSistemaCreacion		    : {type : "date"}   //Fecha de la transacción
			    			,Nemotecnico				    : {type : "string"}
			    			,FolioTransaccion			    : {type : "string"}
			    			,CantidadTransaccion		    : {type : "number"}		         
			    			,PrecioTransaccion			    : {type : "number"}
			    			,CodigoCondicionDeLiquidacion   : {type : "string"}
			    			,MontoTransaccion       		: {type : "number"}
			    			,Intereses			       		: {type : "number"}
			    			,MontoLiquidacion       		: {type : "number"}
			    			,IdComprobanteTesoreria 		: {type : "number"}
			    			,NombreTipoOperacion            : {type : "string"}
			    			,IdMonedaTransaccion            : {type : "string"}
			    			,IdMonedaLiquidacion            : {type : "string"}
						}
					}
				}
			}
		}
	};


	

	obj.columnasGrillaLiquidacionBilateralDetalle = function() {
		return  [		
			 {field: "FolioTransaccion",            	title: "UI_FOLIO_TRANSACCION", width: "70px"}
			,{field: "FechaSistemaCreacion",        	title: "UI_FECHA",             width: "70px"}
			,{field: "IdClasificacionTipoInstrumento", 	title: "UI_CLASIFICACION",     width: "40px"}
			,{field: "NombreTipoOperacion",             title: "UI_TIPO_OPERACION",    width: "200px"}
			,{field: "CodigoCondicionDeLiquidacion",	title: "UI_CONDICION_LIQUIDACION", width: "40px"}
			,{field: "Nemotecnico",                		title: "UI_INSTRUMENTO",       width: "90px"}
			,{field: "CantidadTransaccion",         	title: "UI_CANTIDAD",          width: "75px"}		         
			,{field: "PrecioTransaccion",           	title: "UI_PRECIO",            width: "75px"}
			,{field: "MontoTransaccion",                title: "UI_MONTO",             width: "85px", template : " <label> #:  (util.bcs_formato(MontoTransaccion,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"}			
			,{field: "Intereses",                       title: "UI_INTERESES",         width: "85px", template : " <label> #:  (util.bcs_formato(Intereses,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"}			
			,{field: "MontoLiquidacion",                title: "UI_MONTO_LIQUIDACION", width: "85px", template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'GEN','Monto',IdMonedaLiquidacion, '','')).valor #</label>"}			
			,{field: "IdComprobanteTesoreria",          title: "UI_SECUENCIA_COMPROBANTE"       }
			
		];
	};
				

	$scope.confBasicaLiquidacionBilateralDetalle = function(item) {
		
		var dsDef = obj.dataSourceLiquidacionBilateralDetalle();
		//Se modifica la definicion del datasource para los filtros
		dsDef.definition.callParameters = $scope.setearParametros('busqueda', item.IdAgrupacion); 
		
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: obj.columnasGrillaLiquidacionBilateralDetalle(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 150,
			selectable 			: "row"
		};
	};

	
	//--------------------------------------------------
	// FUNCIONES
	//--------------------------------------------------
	
	$scope.setearParametros = function(accion, idAgrupacion)
	{
		var parametros = {};
		var ContrapartesAProcesar = [];
		
		parametros.p_IdEmpresa 						= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario         	 			= $scope.SesionIdUsuario;
		parametros.p_IdModulo			 			= 'TES';
		parametros.p_FechaLiquidacion	 			= util.dateSPdate($scope.kngFechaLiquidacion) || '0001-01-01';
		parametros.p_ClasificacionTipoInstrumento 	= $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || '';
		parametros.p_TipoInstrumento 				= $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '';
		parametros.p_CondicionDeLiquidacion		  	= $scope.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || '';
		if (accion == 'proceso')
		{

			
			  //Recopila datos de movimientos seleccionados
			$.each($scope.seleccionLiquidacionBilateralResumen,function(key,value)
				{      		     
					ContrapartesAProcesar.push(
						{     
							IdAgrupacion				: $scope.seleccionLiquidacionBilateralResumen[key].IdAgrupacion
					   });
			});
			  
			parametros.p_IdAgrupacionAProcesar		= ContrapartesAProcesar;
			parametros.p_UsaReglasNegocio	 		= 'S';
			parametros.p_FechaSistema               = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) || '0001-01-01';
		}
		
		if (accion == 'busqueda')
		{
			parametros.p_IdAgrupacion                = idAgrupacion;
		}		
		
		return parametros;
	}
	
 
	
	//Busca transaccciones de acuerdo a filtros indicados
	$scope.buscar = function()
	{
		var parametros = {};
		parametros = $scope.setearParametros('busqueda', 0);
		//console.log("PARAMETROS CONSULTA:" + JSON.stringify(parametros));
		
		obj.dataSourceLiquidacionBilateralResumen.setCallParameters(parametros);
		$scope.seleccionLiquidacionBilateralResumen = undefined;
	}

	
	 
	//Limpia filtros de búsqueda
	$scope.limpiarFiltros = function ()
	{	
		$scope.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oConfiguracionClasificacionTipoInstrumento.idActual = false;
		$scope.oConfiguracionTipoInstrumento.idActual = false;
		$scope.oConfiguracionCondicionLiquidacion.idActual = false;
		
		obj.dataSourceLiquidacionBilateralResumen.removeAll();
		$scope.seleccionLiquidacionBilateralResumen = undefined;
	}
	
	
	
 
	//-----------------------------------------
	// PROCESAR
	//-----------------------------------------

	var success = function(respuesta)
	{	
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
		{	
			if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
			{
				bcsComunes.ventanaMsg({									
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			else
			{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
			}
			$scope.buscar(); //se actualiza grilla
		}
		else
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
		}
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);		
	};
	
		
	$scope.procesar = function()
    {	
		if ($scope.seleccionLiquidacionBilateralResumen === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_BUSCAR_INFORMACION_A_PROCESAR'));
		}
		//Logica procesar solo lo seleccionado
		else
		{
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LIQUIDACION_BILATERAL') );
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR)
					{	
						bcsComunes.ventanaOverlay($scope.winID, true);
						var parametros = {};
						parametros = $scope.setearParametros('proceso', 0);
						console.log("PARAMETROS PROCESO:" + JSON.stringify(parametros));		
						bcsServiciosTesoreria.ProcesarLiquidacionBilateral(parametros, success, error);
					}
				});
		}
    };

    
    
	//-------------------------------------------------
	// GENERAR REPORTE LIQUIDACION BILATERAL DETALLADO
	//-------------------------------------------------    
	// Obtiene configuracion para reportes
	var consultarTemplateTesoreria = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {			
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			transport 			: {
									read : {
										url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",
										dataType : "json",
									}
								},
								
			filter				: [
					          		{ field: "IdTemplateTesoreria", operator: "eq", value: 2 }, //Reporte Liquidacion Bilateral Detallado 
					          		{ field: "IdEstado",  operator: "eq", value: 'ACT' },
					          		{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
					            ],		
			schema 				: {
				model: {
					fields : {
						IdReporte       : {type: "string"},
						IdServicio	    : {type: "number"},
						NombreReporte   : {type: "string"},
						FormatoSalida	: {type: "string"},
					}
				}
			}
								
		},
	});
 	
 	
	consultarTemplateTesoreria.fetch().then(function(){
		 if (consultarTemplateTesoreria.data().length > 0 ) 
		 {
			 $scope.$apply(function() {				  
				 $.each(consultarTemplateTesoreria.data(), function(key, registro){
					 $scope.ReporteLiquidacionBilateral = {
							 IdReporte      : registro.IdReporte,
							 NombreReporte	: registro.NombreReporte,
							 IdServicio 	: registro.IdServicio,
							 FormatoSalida  : registro.FormatoSalida
					 };
					 
					 
				 });				 
				return true;
			 });
		 }
		 else 
		 {
			 return false;
		 }
	});
	

	//Generar reporte detallado de liquidación bilateral
	$scope.generarInformeDetallado = function(filtros) {
		    
		if ($scope.ReporteLiquidacionBilateral.IdReporte == '' || $scope.ReporteLiquidacionBilateral.IdReporte == undefined || $scope.ReporteLiquidacionBilateral.NombreReporte  == '' || $scope.ReporteLiquidacionBilateral.NombreReporte  == undefined || $scope.ReporteLiquidacionBilateral.IdServicio == '' || $scope.ReporteLiquidacionBilateral.IdServicio == undefined || $scope.ReporteLiquidacionBilateral.IdServicio == 0 || $scope.ReporteLiquidacionBilateral.FormatoSalida == '' || $scope.ReporteLiquidacionBilateral.FormatoSalida == undefined)
		{
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.msg("error",'Error',$translate.instant("UI_REPORTE_SIN_CONFIGURACION_TESORERIA"));
			return false;
		}
		
		
		if (obj.dataSourceLiquidacionBilateralResumen.data().length == 0)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_DEBE_BUSCAR_INFORMACION_A_PROCESAR'));
		}
		else
		{	
			bcsComunes.ventanaOverlay($scope.winID, true);
			
			var fecha = new Date();

			//Genera nombre para descarga local
			var nombreReporteDescarga = $scope.ReporteLiquidacionBilateral.NombreReporte + "_" + String(fecha.getHours()) + String(fecha.getMinutes()) + String(fecha.getSeconds()) + $scope.ReporteLiquidacionBilateral.FormatoSalida;
 
			var parameters = {
					"parameters"     : "",
					"configuration"  : {
						"mainReport" : {
								            "data" : {
								                  "serviceId" : $scope.ReporteLiquidacionBilateral.IdServicio,
								                  "serviceData" : {								                      
								          			  "P_IDEMPRESA"		: $scope.SesionIdEmpresa,  
								                      "P_IDUSUARIO"		: $scope.SesionIdUsuario, 
								                      "P_IDMODULO" 		: 'TES', 
								                      "P_FECHALIQUIDACION" 				: util.dateSPdate($scope.kngFechaLiquidacion) || '0001-01-01',
								                      "P_CLASIFICACIONTIPOINSTRUMENTO" 	: $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || '',
								                      "P_TIPOINSTRUMENTO" 				: $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '',
								                      "P_CONDICIONDELIQUIDACION" 		: $scope.oConfiguracionCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion || ''
								                  }
								              },
											"parameters" : {},
											"reportName" : $scope.ReporteLiquidacionBilateral.NombreReporte
										},
						"subReports": []
					},
					"exportFormat" : $scope.ReporteLiquidacionBilateral.FormatoSalida,
					"reportId"     : $scope.ReporteLiquidacionBilateral.IdReporte
				}; 
		
				$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				  .success(function (response) {					   
					   var file = '';
					   file = new Blob([response], {type: 'application/pdf'});
					   saveAs(file, nombreReporteDescarga);
					   bcsComunes.ventanaOverlay($scope.winID,false);
				}).error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
		}

		return;
	};	

	
	//Permite actualizar la marca "Bilateral" de las transacciones desde el SCL(con WS)
	$scope.actualizarMarcaBitateralTransaccionDesdeSCL = function(){		
		var parametros = {};
		parametros.fecha = $scope.kngFechaLiquidacion;			
		bcsComunes.ventanaAbrir('ComplementarTransaccion',parametros,false,false);
	};
	
	// WATCH //
	$scope.$watch('seleccionLiquidacionBilateralResumen',function(newval,oldval){
		if (newval != oldval && newval != undefined) {
			$scope.btnActivo = true;
			$.each(newval,function(key,value)
			{
				if(newval[key].IdComprobanteTesoreria != 0)
					$scope.btnActivo = false;
			});
		}else{
			$scope.btnActivo = false;
		};
		
	});
});


