angular.module('ModuloFacturacion').controller('DetalleComprobanteFacturacionCtrl',
		function($scope,$uibModal,$translate,bcsComunes, bcsServiciosFacturacion, bcsComprobanteFacturacionGrid,$http, $rootScope){
	
	
	//Recupera parametros desde la ventana recibida
	$scope.data	= {};

    var idVentana = bcsComunes.ventanaWinId ($scope);   
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);
    let cobros = {};
	//Registro seleccionado
    var bModifMvtoMod = this;
    bModifMvtoMod.IdComprobante = $scope.parametros.IdComprobante;
    bModifMvtoMod.IdTipoComprobante = $scope.parametros.IdTipoComprobante;
    bModifMvtoMod.EsDuplicarFactura = $scope.parametros.EsDuplicarFactura ? $scope.parametros.EsDuplicarFactura : false;
	if(bModifMvtoMod.EsDuplicarFactura)
		bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_DUPLICAR_FACTURA'));
    
    //Variables de sistema
    $scope.data.IdEmpresa	= $scope.SesionIdEmpresa;	
    $scope.data.IdUsuario	= $scope.SesionIdUsuario;
    

	$scope.getSchema = function(){
		return {
			model: {
				fields : {
					NroLinea					: { type: "number"},
					FechaLiquidacion			: { type: "sysdate"},
					NombreTipoInstrumento		: { type: "string"},
					NemotecnicoInstrumento		: { type: "string"},
					Cantidad					: { type: "number"},
					Precio						: { type: "number"},
					Tasa						: { type: "number"},
					Monto                       : { type: "number"},
					NombreTipoProducto			: { type: "string"},
					NombreTipoOperacion			: { type: "string"},
					Descripcion					: { type: "string"},
					
					IdCobro						: { type: "string"},
					NombreCobro					: { type: "string"},
					FormaCobro					: { type: "string"},
					Porcentaje					: { type: "number"},
					IdMonedaLocalCobro			: { type: "string"},
					MontoLocalCobro				: { type: "number"},
					IdMonedaLiquidacionCobro    : { type: "string"},  
					MontoLiquidacionCobro		: { type: "number"},
					
					NombreMedioDePago			: { type: "string"},
					Monto                       : { type: "number"},
					NombreFormaCustodia         : { type: "string"},
					MontoLiquidacionFormaDePago : { type: "number"},
					NombreTipoCuentaBancaria	: { type: "string"},
					NombreCuentaBancaria		: { type: "string"},
					NumeroCuentaBancaria		: { type: "number"},
				}
			}
		};
	};
	

	//Buscar Información del Comprobante
	$scope.comprobanteDatasource = new bcs.DataSource({
		type: "odata",
		definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							filter				: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
							transport 			: { read: {
														     url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1240",
														     dataType: "json"
													 	  }
												  },
							schema				: bcsComprobanteFacturacionGrid.modeloComprobantes()
						}
	});
	
	
	$scope.comprobanteDatasource.query().then(function(){
		$scope.$apply(function() {
			bModifMvtoMod.oComprobante 	= $scope.comprobanteDatasource.data();
		});
	});


	$scope.confBasicaComprobanteDetalle = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "NroLinea", dir: "asc" } ],
									//filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1241",dataType : "json"}},
									schema 			: $scope.getSchema(),
								}
							  }),
		columns				: [
								 {field : "NroLinea", 				title:"UI_NUMERO_LINEA",		width: "30px"}
								,{field : "FechaLiquidacion", 		title:"UI_FECHA_LIQUIDACION", 	width: "85px"}
								,{field : "NombreTipoInstrumento", 	title:"UI_TIPO_INSTRUMENTO", 	width: "100px"}
								,{field : "NemotecnicoInstrumento", title:"UI_INSTRUMENTO", 		width: "100px"}
								,{field : "Cantidad", 				title:"UI_CANTIDAD", 			width: "80px",	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
								,{field : "Precio", 				title:"UI_PRECIO", 				width: "80px",	template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
								,{field : "Tasa",   				title:"UI_TASA", 				width: "80px",	template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
								,{field : "Monto",                	title:'UI_MONTO_OPERADO',		width: "80px", 	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
								,{field : "NombreFormaCustodia",    title:'UI_FORMA_CUSTODIA',		width: "80px"}
								,{field : "NombreTipoProducto",		title:'UI_TIPO_PRODUCTO',		width: "80px"}
								,{field : "NombreTipoOperacion",	title:'UI_TIPO_OPERACION',		width: "80px"}
								,{field : "Descripcion",			title:'UI_DESCRIPCION',			width: "80px"}
								
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter({ field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)}); },  
	};

	$scope.confBasicaComprobanteCobros = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "IdCobro", dir: "asc" } ],
									//filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1243",dataType : "json"}},
									schema 			: $scope.getSchema(),
								}
							  }),
		columns				: [
								 {field : "IdCobro", 			title:"UI_CODIGO",					width: "85px"}
								,{field : "NombreCobro", 		title:"UI_TIPO", 					width: "200px"}
								,{field : "FormaCobro", 		title:"UI_FORMA", 					width: "70px"}
								,{field : "Porcentaje", 		title:"UI_PORCENTAJE_COBRO",		width: "80px", template : " <label> #:  (util.bcs_formato(Porcentaje,'GEN','Puntos',IdMonedaCobro,'','')).valor #</label>", editable: true }
								,{field : "IdMonedaLocalCobro", title:"UI_MONEDA_LOCAL_COBRO",      width: "80px", template: "<span class='flags' title='#=NombreMonedaLocalCobro#'>#=IdMonedaLocalCobro#</span>" }
								,{field : "MontoLocalCobro",	title:"UI_MONTO_LOCAL_COBRO",    	width: "100px", template : " <label> #:  (util.bcs_formato(MontoLocalCobro,'GEN','Monto',IdMonedaLocalCobro,'','')).valor #</label>", editable: true}
								,{field : "IdMonedaLiquidacionCobro", title:"UI_MONEDA_LIQUIDACION_COBRO", width: "80px", template: "<span class='flags' title='#=NombreMonedaLiquidacionCobro#'>#=IdMonedaLiquidacionCobro#</span>" }
								,{field : "MontoLiquidacionCobro", 	title:"UI_MONTO_COBRO_LIQUIDACION", width: "80px",	template : " <label> #:  (util.bcs_formato(MontoLiquidacionCobro,'GEN','Monto',IdMonedaLiquidacionCobro,'','')).valor #</label>"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter([{ field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)}, {field: "IdEstado", operator: "eq", value: 'VIG'}]); },  
		notEditableColumns : function(fila) {
			let columns = [];
			if(!bModifMvtoMod.EsDuplicarFactura){
				columns.push('Porcentaje');
				columns.push('MontoLocalCobro');
				return columns;
			}
			if(fila.IdCobro !== 'COVA'){
				columns.push('Porcentaje');
			}
			if(fila.IdCobro !== 'COFI' && fila.IdCobro !== 'COVA'){
				columns.push('MontoLocalCobro');
			}
			return columns;
		},
		save :function(e) {
			$.each(e.model,function(key,value){
				if(e.model.IdCobro == 'COVA') {
					if(e.values.Porcentaje === undefined) {
						e.model.Porcentaje = (e.values.MontoLocalCobro / bModifMvtoMod.oComprobante[0].MontoComprobante) * 100;
						e.model.MontoLiquidacionCobro = e.values.MontoLocalCobro;
						e.model.MontoLocalCobro = e.values.MontoLocalCobro;
						e.model.MontoCobro = e.values.MontoLocalCobro;
					}
					if(e.values.MontoLocalCobro === undefined){
						e.model.MontoLocalCobro = bModifMvtoMod.oComprobante[0].MontoComprobante * (e.values.Porcentaje/100);
						e.model.MontoLiquidacionCobro = e.model.MontoLocalCobro;
						e.model.MontoCobro = e.model.MontoLocalCobro;
						
					}
				}
			})
			$scope.data.gridApiComprobanteCobros.refresh();
		}
	};

	$scope.confBasicaComprobanteFormaDePago = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
									sort 				: [ { field: "NombreMedioDePago", dir: "asc" } ],
									//filter		: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
									transport 	: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1244",dataType : "json"}},
									schema 		: $scope.getSchema(),
								}
							  }),
		columns				: [
								 {field : "NombreMedioDePago", 			title:"UI_MEDIO_DE_PAGO",			width: "85px"}							
								,{field : "MontoLiquidacionFormaDePago",title:"UI_MONTO_LIQUIDACION",		width: "100px", template : " <label> #:  (util.bcs_formato(MontoLiquidacionFormaDePago,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
								,{field : "NombreTipoCuentaBancaria", 	title:"UI_TIPO_CUENTA_BANCARIA",	width: "80px"}
								,{field : "NombreCuentaBancaria", 		title:"UI_CUENTA_BANCARIA", 		width: "100px"}
								,{field : "NumeroCuentaBancaria", 		title:"UI_NUMERO_CUENTA", 			width: "80px", 	template : " <label> #: util.formateaNumero(NumeroCuentaBancaria,0) #</label>"}
							  ],		
		sortable			: true,
		pageable			: false,
		autoBind			: false,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ e.dataSource.filter([{ field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)}, {field: "IdEstado", operator: "eq", value: 'VIG'}]); }, 
	};
	
	
	//Reporte de comprobante Pdf
	//--Definicion BD reporte y datos
	var definicionOMovimientoCobros = function()
	  {
						
				if ( bModifMvtoMod.IdTipoComprobante === 'POL')
				  {
					
					let IdTipoComprobante = $rootScope.SesionLocale === 'CL'? '': bModifMvtoMod.IdTipoComprobante;
					
					
					return {
						serverFiltering : true, // Filtrado de datos en el servidor.	
						filter : [
									 { field: "CodigoTemplateFacturacion", operator: "eq", value: 'CMBFAC' },
									 { field: "IdEstado",  			operator: "eq", value: 'ACT' },
									 { field: "IdEmpresa", 			operator: "eq", value: $scope.SesionIdEmpresa },
									 { field: "IdTipoComprobante", 	operator: "eq", value: IdTipoComprobante },
									 { field: "LocalizadorSistema", 	operator: "eq", value:  $rootScope.SesionLocale },
								 ],
						transport : {
									  read : {
											   url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4175",
											   dataType: "json"
											 }
									},			         
						schema	: { 
									 model: {
											 fields : {
														IdReporte		: {type: "string"},
														IdServicio		: {type: "number"},
														RutaServicio	    : {type: "string"}, // FALTA borrar
														NombreReporte	: {type: "string"},
														FormatoSalida	: {type: "string"},
													  }
											}
								   }  
					  };
				  }else{
				  	      if(bModifMvtoMod.IdTipoComprobante === 'BOE')
				  	        {
				  	   	       return {
		                                 serverFiltering : true, // Filtrado de datos en el servidor.	
		                                 filter : [
			          		                        { field: "CodigoTemplateFacturacion", operator: "eq", value: 'CMBFAC' },
			          		                        { field: "IdEstado",  			operator: "eq", value: 'ACT' },
			          		                        { field: "IdEmpresa", 			operator: "eq", value: $scope.SesionIdEmpresa },
			          		                        { field: "IdTipoComprobante", 	operator: "eq", value: 'BOE' },
			          		                        { field: "LocalizadorSistema", 	operator: "eq", value: 'CL' },
			                                      ],
		                                 transport : {
			                                            read : {
				                                                  url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4175",
				                                                  dataType: "json"
			                                                   }
		                                             },			         
		                                 schema	: { 
		            	                             model: {
							                                   fields : {
								                                          IdReporte		: {type: "string"},
								                                          IdServicio	: {type: "number"},
								                                          RutaServicio	: {type: "string"}, // FALTA borrar
								                                          NombreReporte	: {type: "string"},
								                                          FormatoSalida	: {type: "string"},
							                                            }
						                                    }
		                                          }  
		                              };
				  	        }else{
				  	   	           if ($rootScope.SesionLocale === 'CL') 
				  	   	             {
				  	   	      	        return {
		                                         serverFiltering : true, // Filtrado de datos en el servidor.	
		                                         filter : [
			          		                                 { field: "CodigoTemplateFacturacion", operator: "eq", value: 'CMBFAC' },
			          		                                 { field: "IdEstado",  			operator: "eq", value: 'ACT' },
			          		                                 { field: "IdEmpresa", 			operator: "eq", value: $scope.SesionIdEmpresa },
			          		                                 { field: "IdTipoComprobante", 	operator: "eq", value: '' },
			          		                                 { field: "LocalizadorSistema", 	operator: "eq", value: 'CL' },
			                                              ],
		                                         transport : {
			                                                    read : {
				                                                          url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4175",
				                                                          dataType: "json"
			                                                           }
		                                                     },
		                                         schema	: { 
		            	                                     model: {
							                                          fields : {
								                                                  IdReporte		: {type: "string"},
								                                                  IdServicio		: {type: "number"},
								                                                  RutaServicio	: {type: "string"}, // FALTA borrar
								                                                  NombreReporte	: {type: "string"},
								                                                  FormatoSalida	: {type: "string"},
							                                                   }
						                                            }
		                                                  }  
		                                       };
				  	   	             }else{
				  	   	   	                 return {
                                                       serverFiltering : true, // Filtrado de datos en el servidor.	
                                                       filter : [
	          		                                               { field: "CodigoTemplateFacturacion", operator: "eq", value: 'CMBFAC' },
	          		                                               { field: "IdEstado",  			operator: "eq", value: 'ACT' },
	          		                                               { field: "IdEmpresa", 			operator: "eq", value: $scope.SesionIdEmpresa },
	          		                                               { field: "IdTipoComprobante", 	operator: "eq", value: '' },
	          		                                               { field: "LocalizadorSistema", 	operator: "eq", value: 'PE' },
	                                                            ],
                                                       transport : {
	                                                                  read : {
		                                                                         url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4175",
		                                                                         dataType: "json"
	                                                                         }
                                                                   },
                                                       schema	: { 
          	                                                         model: {
					                                                           fields : {
						                                                                   IdReporte		: {type: "string"},
						                                                                   IdServicio		: {type: "number"},
						                                                                   RutaServicio	: {type: "string"}, // FALTA borrar
						                                                                   NombreReporte	: {type: "string"},
						                                                                   FormatoSalida	: {type: "string"},
					                                                                    }
				                                                            }
                                                                  }  
                                                    };
				  	   	                  }
				  	             }
				       }
      }
			
			
	
	
	var consultarTemplateFacturacion = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition : definicionOMovimientoCobros()
		
	});
	
	//Descarga pdf	
	$scope.data.pdfComprobante = function()
	{
		if (bModifMvtoMod.oComprobante !== undefined) {
			bcsComunes.ventanaOverlay(idVentana, true);

			if (bModifMvtoMod.oComprobante[0].IdEstadoComprobante === 'FAC' )
			 {
				consultarTemplateFacturacion.fetch().then(function() {

				var registro = consultarTemplateFacturacion.data()[0];
				$scope.ReporteDetalleComprobante = {
					IdReporte		: registro.IdReporte || '',
					NombreReporte	: registro.NombreReporte || '',
					IdServicio	 	: registro.IdServicio || 0,
					FormatoSalida	: registro.FormatoSalida || '',
					RutaServicio	: registro.RutaServicio  || '' // FALTA borrar
				};

				if ($scope.ReporteDetalleComprobante.IdReporte === '' || $scope.ReporteDetalleComprobante.NombreReporte === '' || $scope.ReporteDetalleComprobante.IdServicio === '' || $scope.ReporteDetalleComprobante.IdServicio == 0 || $scope.ReporteDetalleComprobante.FormatoSalida === '')
				{
					bcsComunes.ventanaOverlay($scope.winID, false);
					bcsComunes.msg("error",'Error',$translate.instant("UI_REPORTE_SIN_CONFIGURACION"));
					return false;
				}

				if (bModifMvtoMod.IdComprobante > 0)
				{
					var fecha = new Date();

					//Genera el nombre del reporte para descarga local
					var nombreArchivoDescarga = $scope.ReporteDetalleComprobante.NombreReporte  +"_Nro_"+  String(bModifMvtoMod.IdComprobante)  +"_"+  String(fecha.getFullYear()) + String(fecha.getMonth()+1) + String(fecha.getDate()) + "_" + String(fecha.getHours()) + String(fecha.getMinutes()) + String(fecha.getSeconds()) + "." + $scope.ReporteDetalleComprobante.FormatoSalida;

					var parameters = {
									   "reportId"      : $scope.ReporteDetalleComprobante.IdReporte,
									   "exportFormat"  : $scope.ReporteDetalleComprobante.FormatoSalida,						
									   "parameters"    : {},						
									   "configuration": {
														  "mainReport" : {	
																		   "data" : {
																					  "serviceId" : $scope.ReporteDetalleComprobante.IdServicio,
																					  "serviceData" : {
																										"P_IDEMPRESA"      : Number($scope.SesionIdEmpresa),
																										"P_IDCOMPROBANTE"  : Number(bModifMvtoMod.IdComprobante),
																										"$schema"			 : [{"field":"LogoCorredora", "type":"blob"}]
																									  },
																					},										                                  
																		   "parameters"	: {},
																		   "reportName"	: $scope.ReporteDetalleComprobante.NombreReporte
																		 },
														  "subReports": []
														},
									 };

					$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
					.success(function (response) {
												   var file = new Blob([response], {type: 'application/pdf'});
												   saveAs(file, nombreArchivoDescarga);
												   bcsComunes.ventanaOverlay(idVentana,false);
												 }).error(function (response) {
							 bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));

							});
				}
				else 
				{
					bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
				}

				return;
			  });
 
			 } else {
					//Parámetros de entrada al servicio de consulta dte		
					var datosConsultar = {};	
					var ComprobanteDTE = [];	

					ComprobanteDTE.push(
										 { 
											 IdDocumento : bModifMvtoMod.oComprobante[0].IdRespuestaDTE 
										 }
									   );

					datosConsultar.p_IdEmpresa  = $scope.data.IdEmpresa; 
					datosConsultar.p_IdUsuario	= $scope.data.IdUsuario;
					datosConsultar.p_Tipo   	= 'PDF';
					datosConsultar.p_IdDocumentos = ComprobanteDTE;  


					//Llamada a servicio para crear comprobante de facturación()
					bcsServiciosFacturacion.InformeDTE(datosConsultar,function(result)
					  {
						bcsComunes.ventanaOverlay(idVentana, false);
						var consResultado = [];
						var respuestaDTE = [];

						bcsComunes.ventanaOverlay(idVentana, true);

						for(var i = 0; i < result.P_FILES.length; i++)
						  {
							 var leBlob = util.b64toBlob(result.P_FILES[i].encodedString,'application/pdf',512);
							 saveAs(leBlob, "ComprobanteFacturacion_" + result.P_FILES[i].dteNumber + ".pdf");

							 var idRespuestaDTE = parseInt(result.P_FILES[i].dteNumber);
							 idRespuestaDTE = isNaN(idRespuestaDTE) ? 0 : idRespuestaDTE,
							 respuestaDTE[idRespuestaDTE] = result.P_FILES[i].mensaje;
						  }

						bcsComunes.ventanaOverlay(idVentana, false);


					  },function(resultado)
						 {
							bcsComunes.ventanaOverlay(idVentana, false);
						 });

				};		
			bcsComunes.ventanaOverlay(idVentana,false);
		}
	};
	
	$scope.data.verMapaRelaciones = function()
	{
		bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "CMBTEFACTURACION",IdEntidad :bModifMvtoMod.IdComprobante},false,false );
	};
	
	$scope.data.fnDuplicarFactura = function() {
		let listaNotasDeCredito = ["NCR", "NCE", "NEO", "NDR", "NDE", "NEE"];
		var idTipoComprobanteReferencia = "";
		var tipoOperacion = "";
		
		if(bModifMvtoMod && bModifMvtoMod.oComprobante && bModifMvtoMod.oComprobante[0] && bModifMvtoMod.oComprobante[0].CodigoTipoOperacion){
			tipoOperacion = bModifMvtoMod.oComprobante[0].CodigoTipoOperacion;
		}
		
		if(tipoOperacion && tipoOperacion.toUpperCase().includes("SIM")){
			const respuesta = {
				P_OUT_ERRORESCONTADOR: 1,
				P_OUT_MENSAJES: [{
						id : 0,
						tipo : "ERROR",
						codigo : "UI_ERROR_DUPLICAR_FACTURA_SIM",
						descripcion : $translate.instant("UI_ERROR_DUPLICAR_FACTURA_SIM")
					}
				]
			};	
			
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant("UI_ERROR_DUPLICAR_FACTURA"),
				conflictos: util.parseServerResponse(respuesta)
			}, $scope.winID);
			return;
		}
		
		
		if(bModifMvtoMod && bModifMvtoMod.oComprobante && bModifMvtoMod.oComprobante[0] && bModifMvtoMod.oComprobante[0].IdTipoComprobanteReferencia){
			idTipoComprobanteReferencia = bModifMvtoMod.oComprobante[0].IdTipoComprobanteReferencia;
		}
		
		if (!listaNotasDeCredito.includes(idTipoComprobanteReferencia)) {
			const respuesta = {
				P_OUT_ADVERTENCIASCONTADOR: 1,
				P_OUT_ADVERTENCIAS: [{
						id : 0,
						tipo : "ADVERTENCIA",
						codigo : "UI_ADVERTENCIA_DUPLICAR_FACTURA",
						descripcion : ""
					}
				]
			};	
			
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant("UI_ADVERTENCIA_DUPLICAR_FACTURA"),
				conflictos: util.parseServerResponse(respuesta)
			}, $scope.winID);
		}
		
		let entradaDuplicar = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FAC',
			p_UsaReglasNegocio : 'S',
			IdComprobanteBase : bModifMvtoMod.IdComprobante,
			p_MovimientosCobros : []
		}
		
		cobros = $scope.data.gridApiComprobanteCobros.dataSource.data();
		let index = 1;
		
		$.each(cobros,function(key,value){
			entradaDuplicar.p_MovimientosCobros.push({
				NumeroMvtoCmbte: 0,
				NumeroMvtoCmbteCobro : index,
				IdCobro: value.IdCobro,
				IdMonedaCobro: value.IdMonedaCobro,
				IdMonedaLiquidacionCobro: value.IdMonedaLiquidacionCobro,
				MontoCobro: value.MontoCobro,
				MontoMovimientoComprobanteCobro: value.MontoCobro,
				MontoLiquidacionCobro: value.MontoLiquidacionCobro,
				Porcentaje : value.Porcentaje
			});
			index++;
		});
		
		if(entradaDuplicar.p_MovimientosCobros.length === 0){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_NO_EXISTEN_CAMBIOS_EN_COBROS'));
			return;
		}
		bcsComunes.ventanaOverlay(idVentana, true);
		
		bcsServiciosFacturacion.DuplicarFactura(entradaDuplicar).$promise.then(
			function(respuesta) {
				bcsComunes.ventanaOverlay(idVentana, false);
				
				bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.winID);							
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.msg("error", "Error", $translate.instant('UI_ERROR_DUPLICAR_FACTURA'));
					return;
				}
				
				if(respuesta.P_OUT_ERRORESCONTADOR === 0){
					
				}
			
			},
			function() {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.msg("error", "Error", $translate.instant('UI_ERROR_DUPLICAR_FACTURA'));
		});
															
	};
	
	
	$scope.data.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
		
	};
});
