angular.module('ModuloTesoreria').controller('ConsultarTransferenciasCtrl',
		function($scope,$state,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria,$http)
{

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var dataSources = [];

	$scope.frm = {};
	$scope.dataReprocesada = {};
	$scope.btnActivo = true;
	$scope.habilitaQuitarDetalle = false;
	//SETEO INICIAL
	$scope.frm.kngFechaIngreso = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.frm.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	
	//COMBOBOX
	$scope.frm.oConfiguracionBanco={
			tipo : "combobox-banco-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.frm.oConfiguracionCuentaCorredora={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : 0 },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.frm.oConfiguracionEstado={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value": 'TES.NominaTransferencia'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	


	
	//GRILLAS
	var dsTransferencias = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdNominaTransferencia", dir: "desc" },
			pageSize 			: 200,								
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1033",dataType : "json"}},
			schema : {
				model : {
					fields : {										
						IdNominaTransferencia		: {type: "number"},						
						IdComprobantePartidaEgreso	: {type: "number"},
						FechaMovimiento				: {type: "sysdate"},
						FechaLiquidacion			: {type: "sysdate"},
						FechaRecepcion				: {type: "sysdate"},
						NombreMedioDePago			: {type: "string"},						
						NombreBanco					: {type: "string"},
						NombreModalidadDePago		: {type: "string"},
						IdTipoCuentaBanco			: {type: "number"},
						IdCuentaBanco				: {type: "number"},
						IdCuentaBanco				: {type: "number"},
						NombreCuentaBancariaFull	: {type: "string"},
						IdBanco						: {type: "number"},
						IdMoneda					: {type: "string"},
						NombreMoneda				: {type: "string"},
						Monto						: {type: "number"},
						NombreArchivo				: {type: "string"},
						NombreEstado				: {type: "string"},
						IdEstado					: {type: "string"},						
						IdEmpresa					: {type: "number"},
						NombreArchivo				: {type: "string"},
						RutaArchivo				    : {type: "string"},						
						RutaBase				    : {type: "string"},
						RutaTesoreria				: {type: "string"}
					}
				}
			}
		}
	});
	$scope.frm.confBasicaTransferencias = {
		dataSource			: dsTransferencias,
		columns				: [	 {field : "IdNominaTransferencia",	 	title:"UI_TRANSFERENCIA", 		width: "100px"}
								,{field : "FechaMovimiento", 			title:"UI_FECHA_INGRESO", 		width: "120px"}
								,{field : "FechaLiquidacion", 			title:"UI_FECHA_LIQUIDACION",	width: "120px"}
								,{field : "NombreMedioDePago", 			title:"UI_MEDIO_DE_PAGO", 		width: "120px"}
								,{field : "NombreBanco", 				title:"UI_BANCO", 				width: "120px"}
								,{field : "NombreCuentaBancariaFull", 	title:"UI_CUENTA_BANCARIA", 	width: "250px"}								
								,{field : "IdComprobantePartidaEgreso",	title:"UI_PARTIDA_EGRESO", 		width: "120px"}
								,{field : "NombreMoneda", 				title:"UI_MONEDA", 				width: "120px"}
								,{field : "Monto", 						title:"UI_MONTO", 				width: "120px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}								
								,{field : "FechaRecepcion", 			title:"UI_FECHA_RECEPCION",		width: "120px"}
								,{field : "NombreEstado", 				title:"UI_ESTADO", 				width: "80px"}
								,{field : "NombreArchivo",              title:"UI_ARCHIVO", 			width: "200px"}
								// Columna que se usa para descargar archivo físico(la ruta se arma cada vez que se actualiza el dataSource(dataBound)								
								,{field : "RutaArchivo",                title:"UI_DESCARGAR", 		    width: "200px" , template : kendo.template("#if (NombreArchivo == null || NombreArchivo == '') {#-#} else {#<a class='fileDownload'>Descargar</a> #}#") }
								
							  ],
		rowColorBackground	: function(row) { 
								 var color = "";
								 if (row.IdEstado == 'ANU')
								 	color = "red";
								 
								 return color;
							  },
		sortable			: true,
		pageable			: true,
		
		dataBound			: function(e) { // Este evento se ejecuta cada vez que la grilla se vincula al dataSource
								$scope.armaRutaDescargaArchivo();},
			  
		detailExpand		: function(e) { 
								this.collapseRow(this.tbody.find(' > tr.k-master-row').not(e.masterRow));
								this.clearSelection();
								this.select(e.masterRow);},

 	    gridCreated         : function(grid) {
					      			$scope.apiGrid = grid;					      			
					      		},
					      		
		filterable			: true,
		height				: 510,
		selectable 			: "row",
		userSettings		: true
	};

	var dsTransferenciaDetalle = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdNominaTransferenciaDetalle", dir: "desc" },
			pageSize 			: 200,				
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1034",dataType : "json"}},
			schema : {
				model : {
					fields : {										
						IdNominaTransferencia			: {type: "number"},
						IdComprobante					: {type: "number"},
						FechaIngresoComprobante			: {type: "sysdate"},
						NombrePersona					: {type: "string"},
						DocumentoIdentidad				: {type: "string"},
						IdMoneda						: {type: "string"},
						NombreMoneda					: {type: "string"},
						Monto							: {type: "number"},
						NombreEstado					: {type: "string"},
						IdEstado						: {type: "string"},
						AliasBanco				        : {type: "string"},
						CuentaDCVOrigen				    : {type: "string"},
						CuentaDCVDestino				: {type: "string"},
						ClaveDCV                        : {type: "string"},
						Nemotecnico						: {type: "string"}
					}
				}
			}      		
		}
	});
	$scope.frm.confBasicaTransferenciaDetalle = function(item) {		
		dsTransferenciaDetalle.definition.filter = [{ field: "IdNominaTransferencia", operator: "eq", value: item.IdNominaTransferencia }];
		return {
			dataSource			: dsTransferenciaDetalle,
			dataBound : function(e){
				if(dsTransferenciaDetalle.ds && $scope.frm.seleccionTransferencias){
					if(dsTransferenciaDetalle.ds._data[0].IdNominaTransferencia){
						var IdNominaTransferencia = dsTransferenciaDetalle.ds._data[0].IdNominaTransferencia;
						var dsEncontrada = dataSources.find(objeto => objeto.IdNominaTransferencia === IdNominaTransferencia);
					}
					var copiaDataSource = new kendo.data.DataSource($.extend({}, dsTransferenciaDetalle.ds.options, {
						data: dsTransferenciaDetalle.ds.data().toJSON()
					}));
					if (dsEncontrada) {
						(dataSources.find(objeto => objeto.IdNominaTransferencia === IdNominaTransferencia)).DataSource = copiaDataSource;
					} else {
						dataSources.push({
							IdNominaTransferencia: $scope.frm.seleccionTransferencias.IdNominaTransferencia,
							DataSource: copiaDataSource
						});
					}	
				}
			},
			columns				: [	 {field : "IdComprobante", 				title:"UI_COMPROBANTE", 		width: "150px"}
									,{field : "FechaIngresoComprobante", 	title:"UI_FECHA_INGRESO", 		width: "150px"}
									,{field : "NombrePersona", 				title:"UI_SOCIO_NEGOCIO", 		width: "150px"}
									,{field : "DocumentoIdentidad", 		title:"UI_DOCUMENTO_IDENTIDAD", width: "150px"}
									,{field : "NombreMoneda",	 			title:"UI_MONEDA", 				width: "150px"}
									,{field : "Monto", 						title:"UI_MONTO", 				width: "150px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
									,{field : "Nemotecnico", 				title:"UI_NEMOTECNICO", 		width: "100px"}
									,{field : "NombreEstado", 				title:"UI_ESTADO", 				width: "150px"}
									,{field : "AliasBanco", 				title:"UI_ALIAS_CUENTA_BANCO", 	width: "100px"}
									,{field : "CuentaDCVOrigen", 			title:"UI_CUENTA_DCV_ORIGEN", 	width: "100px"}
									,{field : "CuentaDCVDestino", 			title:"UI_CUENTA_DCV_DESTINO", 	width: "100px"}
									,{field : "ClaveDCV", 			        title:"UI_CLAVE_DCV", 			width: "100px"}
								  ],
			rowColorBackground	: function(row) { 
									 var color = "";
									 if (row.IdEstado == 'ANU')
										color = "red";
									 return color;
								  },
			sortable			: true,
			pageable			: true,
			filterable			: true,
			autoBind			: true,
			height				: 200,
			selectable 			: "multiple row"
			

		}
	};
	

	//WATCH
	$scope.$watch('frm.seleccionTransferencias', function(newval, oldval){
		$scope.habilitaQuitarDetalle = false;
	});
	$scope.$watch('frm.seleccionTransferenciaDetalle',function(newval,oldval){
		if($scope.frm.seleccionTransferenciaDetalle && dataSources){
			var IdNominaTransferenciaSeleccionada = $scope.frm.seleccionTransferenciaDetalle[0].IdNominaTransferencia;
			var dsEncontrada = dataSources.find(objeto => objeto.IdNominaTransferencia === IdNominaTransferenciaSeleccionada);
			let detallesLength;
			if(dsEncontrada){
				detallesLength = dsEncontrada.DataSource.options.data.length;
				if($scope.frm.seleccionTransferenciaDetalle && $scope.frm.seleccionTransferenciaDetalle.length<detallesLength){
					 	$scope.habilitaQuitarDetalle = true;
				}else {
					$scope.habilitaQuitarDetalle = false;
				}
			}
			else{
				console.log("NO ENCUENTRA DATASOURCE SELECCIONADA");
			}
		}
	});
	

	$scope.frm.fnDobleClick = function(datos) {
		if (datos[0].IdEstado != 'ANU'){
			var idModal = bcsComunes.ventanaAbrir("MedioDePagoSocioModal", datos, true, true );
			var promesa = bcsComunes.ventanaPromesa(idModal);

			promesa.then(function(data){ 
				if (data != undefined && Object.keys(data).length != 0 ){
					//Registrar datos reprocesados para posterior envío a SP
					$scope.dataReprocesada[data.IdNominaTransferenciaDetalle] = 
						{'Id'						: data.IdNominaTransferenciaDetalle,
						 'IdChequera'				: 0,
						 'NumeroChequeDesde'		: 0,
						 'CantidadCheques'			: 0,
						 'IdCuentaBancariaSocio'	: data.IdCuentaBancaria,
						 'IdTarjetaBancariaSocio'	: data.IdTarjetaBancariaSocio,
						 'NumeroSerieSocio'			: '',
						 'NumeroChequeSocio'		: 0,
						 'NumeroDocumentoSocio'		: 0,
						 'ClaveDCV'					: data.ClaveDCV
						 };

					//Actualizar Grilla
					var nominasDetalle = dsTransferenciaDetalle.data();
					$.each(nominasDetalle, function(key,value){
						if (Number(value.IdNominaTransferenciaDetalle) == Number(data.IdNominaTransferenciaDetalle) ){
							value.IdCuentaBancariaSocio = data.IdCuentaBancaria;
							value.NombreCuentaBancariaSocio = data.NombreCuentaBancaria;
						}

					});
				}	        		
			});
		}
	};
	



	
	//OPCIONES
	$scope.buscarTransferencias = function(filtros){
		var filters = new Array();
			
		filters.push({ field: "EsTransferencia", operator: "eq", value: 'S' });

		if (filtros.oConfiguracionBanco.seleccionado.IdBanco)
			filters.push({ field: "IdBanco", operator: "eq", value: filtros.oConfiguracionBanco.seleccionado.IdBanco });
		if (filtros.oConfiguracionCuentaCorredora.seleccionado.IdCuentaBanco)
			filters.push({ field: "IdCuentaBanco", operator: "eq", value: Number(filtros.oConfiguracionCuentaCorredora.seleccionado.IdCuentaBanco) });
		if (filtros.ngTransferencia && filtros.ngTransferencia != 0)
			filters.push({ field: "IdNominaTransferencia", operator: "eq", value: (filtros.ngTransferencia) });
		if (filtros.kngFechaIngreso)
			filters.push({ field: "FechaMovimiento", operator: "eq", value: util.date(util.dateOdata(filtros.kngFechaIngreso)).hours(0).minutes(0).seconds(0).toDate() });
		if (filtros.kngFechaLiquidacion)
			filters.push({ field: "FechaLiquidacion", operator: "eq", value: util.date(util.dateOdata(filtros.kngFechaLiquidacion)).hours(0).minutes(0).seconds(0).toDate() });
		
		if (filtros.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.oConfiguracionEstado.seleccionado.IdEstado });
		
		
		dsTransferencias.filter(filters);
	};

	
	
	// --------------------------------------------------------------------------------------
	// Funciones para armar ruta de descarga de archivo de transacciones
	var descargarArchivo = function(url,nombreArchivo,rutaArchivo) {
		 $http.post(
	  	        url+"file="+rutaArchivo + nombreArchivo,
	  	        {},
	  	        {"responseType": "arraybuffer"}
	  	    ).success(function (data) {  
		  	  	var file = new File([ data ], nombreArchivo);
				saveAs(file);
	  	    	bcsComunes.log().info($translate.instant('UI_ARCHIVO_DESCARGA_EXITO'));
	  	    	
	  	    }).error(function (err){
	  	    	bcsComunes.msg("error","error",$translate.instant('UI_ARCHIVO_DESCARGA_ERROR'));
	  	    });
		return false;
	};
	
	
	$scope.armaRutaDescargaArchivo = function()
	{	//Reemplaza "fileDownload" por la ruta real
		$("a.fileDownload").click(function() {
			var data = $("#frmConsultarTransferencias_grid_transferencias").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent())
			
			var nombreArchivo =  data.NombreArchivo==null? '' : data.NombreArchivo;
			var rutaTesoreria =  data.RutaTesoreria==null? '' : data.RutaTesoreria;  // RutaTesoreria: "tes/"
			var url="/BSCL/Plataforma/GWJAVA/PLA/download?";				
			
			descargarArchivo(url,nombreArchivo,rutaTesoreria);
			});		
	}
	
	
	
	
	$scope.limpiarFiltros = function(filtros) {
		filtros.oConfiguracionBanco.idActual = 0;
		filtros.oConfiguracionCuentaCorredora.idActual = 0;
		filtros.oConfiguracionEstado.idActual = 0;
		filtros.ngTransferencia = 0;

		filtros.kngFechaIngreso = util.dateInicioDia($scope.SesionFechaSistema);
		filtros.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);

		dsTransferencias.removeAll();
	};
	$scope.anular = function(filtros) { //Anular transferencia (total o parcial)
		if (filtros.seleccionTransferencias){
			var dataCols = [];			
			dataCols.push({IdNominaTransferencia : filtros.seleccionTransferencias.IdNominaTransferencia});
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ANULARA_LOS_REGISTROS_SELECCIONADOS') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){ 

					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;
					var dataCols = [];

						if (filtros.seleccionTransferencias != undefined){
			 				  var ObjCus = {
									"Id"		: filtros.seleccionTransferencias.IdNominaTransferencia || 0,
									"Entidad"	: 'TES.NominaTransferencia'
								};
								dataCols.push(ObjCus);
						}			

					//DATOS SERVICIO
					var datos = {};
					datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario               = $scope.SesionIdUsuario; 
					datos.p_IdModulo                = 'TES';
					datos.p_UsaReglasNegocio        = 'S';

					datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
					datos.p_RegistrosAAnular		= dataCols;

					bcsComunes.log().info(JSON.stringify(datos));
					bcsServiciosTesoreria.AnularNominasTransferencias(datos,success,error);

				}
			});
		}
		else {						
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")
			},$scope.winID);
		}
		return;
	};
	
	
	$scope.quitarDetalle = function(filtros) { //Quitar detalle de transferencia
		if(filtros.seleccionTransferencias){

			var dataCols = [];
			dataCols.push({IdNominaTransferencia : filtros.seleccionTransferencias.IdNominaTransferencia});
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_QUITARA_LOS_REGISTROS_SELECCIONADOS') );
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay($scope.winID, true);
						$scope.btnActivo = false;
						var dataCols = [];

						if (filtros.seleccionTransferenciaDetalle != undefined){
							$.each(filtros.seleccionTransferenciaDetalle, function(index, value){
									var ObjCus = {
										"IdComprobante"		: value.IdComprobante || 0,
										"IdNominaTransferencia" : filtros.seleccionTransferencias.IdNominaTransferencia || 0
									};
									dataCols.push(ObjCus);
								});
						}

						//DATOS SERVICIO
						var datos = {};
						datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
						datos.p_IdUsuario               = $scope.SesionIdUsuario; 
						datos.p_IdModulo                = 'TES';
						datos.p_UsaReglasNegocio        = 'S';

						datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_RegistrosAProcesar		= dataCols;

						bcsComunes.log().info(JSON.stringify(datos));
						bcsServiciosTesoreria.QuitarDetalleNomina(datos,success,error);

					}
				});
		}
	};

	//--------------------------------------------------------------------------------------------
	// Solicita generación de archivo de transfefrencias bancarias (Inscripción en JobManager) 
	//--------------------------------------------------------------------------------------------		
	$scope.generarArchivo = function(filtros) { 
		if (filtros.seleccionTransferencias){

			var dataCols = [];
			dataCols.push(
						{
							IdNominaTransferencia : filtros.seleccionTransferencias.IdNominaTransferencia,
							IdBanco: filtros.seleccionTransferencias.IdBanco,
							IdMoneda: filtros.seleccionTransferencias.IdMoneda,
							IdMedioDePago: filtros.seleccionTransferencias.IdMedioDePago,
							IdTipoTarea: 'GENERAR_ARCHIVO_PAGO',
							IdAgenda: 0,
							Segundos: 1
						});
			 
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_SOLICITARA_GENERACION_ARCHIVO_PAGO') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;

					//DATOS SERVICIO
					var datos = {};
					datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario               = $scope.SesionIdUsuario; 
					datos.p_IdModulo                = 'TES';
					datos.p_UsaReglasNegocio        = 'S';
					datos.p_ArchivosAGenerar	    = dataCols;

					bcsComunes.log().info(JSON.stringify(datos));
					
					
					bcsServiciosTesoreria.SolicitudGenerarArchivo(datos).$promise.then(
							function(result) {
								bcsComunes.ventanaOverlay($scope.winID,false);
								$scope.btnActivo = true;
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(result)
								},$scope.winID);
								
							},
							function(result){
								bcsComunes.ventanaOverlay($scope.winID,false);
								$scope.btnActivo = true;								
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_SOLICITUD_GENERACION_ARCHIVO_ERROR"),
									conflictos:util.parseServerResponse(result)
								},$scope.winID);
							});
					
				}
			});
		}
		else {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")
			},$scope.winID);			
		}
		return;
	};
	

	//MANEJO DE RESPUETA SERVICIOS
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR == 0){
			//Se debe mostrar el listado del proceso
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);			
			$scope.dataReprocesada = {};
			$scope.buscarTransferencias($scope.frm);
		}
		else
		{				
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);			
		}			
		bcsComunes.ventanaOverlay($scope.winID, false);
		$scope.btnActivo = true;
		$scope.habilitaQuitarDetalle = false;
	};
	var error = function(respuesta)
	{			
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
			
		bcsComunes.ventanaOverlay($scope.winID, false);
		$scope.btnActivo = true;
		$scope.habilitaQuitarDetalle = false;

	};
});