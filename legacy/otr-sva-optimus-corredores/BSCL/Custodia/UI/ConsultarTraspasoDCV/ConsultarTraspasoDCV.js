angular.module('ModuloCustodia').controller('ConsultarTraspasoDCVCtrl', 
		function($rootScope, $scope, $state, $translate, $resource, $timeout,
				 bcsComunes,
				 OPCIONES_BCSALERTA,
				 bcsServicioPlataforma,
				 bcsServiciosMesa,
				 bcsAlerta)				
{	
	
	//*** configuracion de objetos ***//	
	$scope.filtros = {};
	$scope.data ={};	
		
	var parametrosConsulta = {};	
	var conParametros = false;
		
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.kngFecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	


	$scope.data.oConfiguracionTipoOperacion={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Operacion" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};

	$scope.data.oConfiguracionTipoComprobante={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Comprobante" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true
	};

	$scope.data.oConfiguracionTipoCuenta={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Cuenta" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};	

	$scope.data.oConfiguracionMercado={
			tipo : "combobox-parametro-mesa",
			configuracion : {},
			filtros : { 
						IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
						TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Mercado" }
					  },
			idActual : false,
			seleccionado : false,
			idDefecto : '' 
	};		

	
	$scope.data.oConfiguracionEstadoMovimiento={
		tipo : "combobox-parametro-mesa",
		configuracion : {},
		filtros : { 
			IdEstado : { "field"   : "IdEstado", "operator": "eq", "value" : "VIG" },
			TipoParametro : { "field": "NombreTipoParametro", "operator": "eq", "value" : "CUS_OPPEND_Estado" }
		  },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	
	var dsArchivoDCV = new bcs.DataSource({
		type : "call",
	    definition : {
	    		pageSize		: 100,
                callTransaction : "3255",
                //callTransaction : "3580",
                callResultSet   : "P_OUT_SALIDA",
                schema : {
                	model : {
                		fields : {                                                                                                                                                             
                			FolioOperacion			  		: { type: "number"},		//	SEC_MOVTO
							NombreOperacion					: { type: "string"},		// 	DESC_OPER
							NombreConcepto					: { type: "string"},		// 	DESC_OPER
							NombreProducto					: { type: "string"},		// 	DESC_OPER
							IdTipoInstrumento				: { type: "string"},		//	TIPO_INST
							Nemo                      		: { type: "string"},		//	NEMO
							IdClasificacionTipoInstrumento  : { type: "string"},		//	MERCADO
							IdCondicionLiquidacion			: { type: "string"},		//	IND_LIQUID
							IdTipoOperacion                 : { type: "string"},		//	TIPO_OPER
							Accion            				: { type: "string"},		//	DESC_TIPO_OPER
							OperadorDirecto              	: { type: "string"},		//	OPER_DIRECTO
							FechaIngresoRegistro            : { type: "date"},			//	FEC_MVTO
							FechaVencimiento                : { type: "date"},			//	FEC_VCTO
							DocumentoIdentidad				: { type: "string"},		//	RUT_CLI
							IdPortafolio               		: { type: "number"},		//	SEC_RUT_CLI
							FolioComprobante            	: { type: "number"},		//	FOLIO_COMP_ADJ
							FolioComprobanteReferencia      : { type: "number"},		//	FOLIO_COMP_ASOC
							FolioComprobanteRelacionado     : { type: "number"},		//	FOLIO_COMP_CONT
							IdOrden                 		: { type: "number"},		//	SEC_ORDEN
							Cantidad                  		: { type: "number"},		//	CANTIDAD
							Precio                    		: { type: "number"},		//	PRECIO
							Tasa                      		: { type: "number"},		//	TASA
							Monto                     		: { type: "number"},		//	MONTO
							MontoComprom             		: { type: "number"},		//	MONTO_COMPROM
							IdEstado                    	: { type: "string"},		//	ESTADO
							IdAgente                    	: { type: "string"},		//	AGENTE
							IdMoneda                    	: { type: "string"},		//	MONEDA
							TipoGranatia             		: { type: "string"},		//	TIPO_GARANTIA
							FondoAfp                 		: { type: "string"},		//	FONDO_AFP
							IdTipoComprobante             	: { type: "string"},		//	TIPO_COMP_ADJ
							CodigoTipoProducto              : { type: "string"},		//	PRODUCTO
							IdBolsa                   		: { type: "string"},		//	ENTIDAD
							DocumentoIdentidadEntidad       : { type: "string"},		// 	RUT_ENTI
							IdBoveda                		: { type: "string"},		//	COD_BOVEDA
							FolioTransaccion               	: { type: "number"},		//	FOLIO_TRANS
							DescEstadoCclv          		: { type: "string"},		//	DESC_ESTADO_CCLV
							UsoGarantia          			: { type: "string"},		//	IND_USO_GARANTIA
							TopoOpePendiente              	: { type: "string"},		//	TIPO_OPE_PEN
							BlockEnvio            			: { type: "string"},		//	IND_BLOK_ENVIO
							IdConcepto             			: { type: "string"},		//	CONCEPTO_MVTO
				//					IND_ENV_MVTO              		: { type: "string"},		//	IND_ENV_MVTO
							IdEmisor                    	: { type: "string"},		//	EMISOR
							EsFungible          			: { type: "string"},		//	IND_FUNGIBILIDAD
							NemoSVS                  		: { type: "string"},		//	NEMO_SVS
							IdTipoReajuste                 	: { type: "string"},		//	TIPO_REAJ
							IdOperacionPendiente			: { type: "number"},		//	MONTO_COMPROM
							IdEstadoProceso					: { type: "string"},		//	TIPO_REAJ
							Clave							: { type: "string"},		//	CLAVE
							CodigoPortafolio				: { type: "string"},		//	CODIGO PORTAFOLIO
							CuentaClienteDCV				: { type: "string"},		//	CUENTA DCV
							//[CELCONDOR - 659] - Transferencia de Títulos a Depósito de Valores
							CuentaClienteDCVDepositante		: { type: "string"},		//	CUENTA DCV DEPOSITANTE
							CuentaClienteDCVContraparte		: { type: "string"},		//	CUENTA DCV CONTRAPARTE
							TipoSocioNegocio                : { type: "string"},        //	TIPOSOCIO
							//SocioNegocio		            : { type: "string"},		//	SOCIO NEGOCIO
							NombreCliente                   : { type: "string"},		//	NOMBRE CLIENTE
							Relacion		                : { type: "string"}		    //	RELACION
							//[CELCONDOR - 659] - Transferencia de Títulos a Depósito de Valores  
                        }
                    }
                }
           	}
	});
 
 	$scope.columnasArchivoDCV = function() {
		return  [

			     {field: "NombreProducto"    				,title: 'UI_PRODUCTO',		width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "IdTipoOperacion"            		,title: 'UI_TIPO_OPERACION',							width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "NombreOperacion"      			,title: 'UI_TIPO_OPERACION', 							width: "200px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				//,{field: "FolioComprobante"       			,title: 'UI_FOLIO_COMPROBANTE', format: "{0:n0}",	width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Cantidad"             			,title: 'UI_CANTIDAD', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Nemo"                 			,title: 'UI_NEMO_ABR', 									width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Clave"            				,title: 'UI_CLAVE',				                        width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }
				,{field: "IdEstadoProceso"            		,title: 'UI_ESTADO_PROCESO',	                        width: "70px", attributes:{style:"text-align:center;"}, headerAttributes:{style:"text-align: center;"} }				
//				,{field: "CuentaClienteDCVDepositante"		,title: 'UI_CUENTA_DCV_DEPOSITANTE',                    width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }				
				,{field: "CuentaClienteDCV"		            ,title: 'UI_CUENTA_DCV_DEPOSITANTE',                    width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }				
				,{field: "CuentaClienteDCVContraparte"		,title: 'UI_CUENTA_DCV_CONTRAPARTE',                    width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} }				
				,{field: "DocumentoIdentidad"           	,title: 'UI_DOCUMENTO_IDENTIDAD',						width: "80px", attributes:{style:"text-align:right;"}, headerAttributes:{style:"text-align: right;"} , groupable: true}				
				,{field: "CodigoPortafolio"          		,title: 'UI_PORTAFOLIO', 								width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "TipoSocioNegocio"           		,title: 'UI_TIPO_SOCIO_NEGOCIO',						width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "NombreCliente"          		    ,title: 'UI_SOCIO_NEGOCIO',					  		    width: "150px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Relacion"          		        ,title: 'UI_RELACION',  								width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdTipoReajuste"            		,title: 'UI_TIPO_REAJUSTE',								width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "Monto"                			,title: 'UI_MONTO', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "MontoComprom"        				,title: 'UI_MONTO_COMPROBANTE',	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}								
				,{field: "FolioOperacion"					,title: 'UI_SECUENCIA', 		format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "FechaIngresoRegistro"             ,title: 'UI_FECHA_OPERACION',							template : " <label> #:  util.formatoDate(FechaIngresoRegistro) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
				,{field: "IdClasificacionTipoInstrumento"   ,title: 'UI_MERCADO', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdTipoInstrumento"    			,title: 'UI_ID_CLASIFICACION_TIPO_INSTRUMENTO',			width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				,{field: "NemoSVS"             				,title: 'UI_NEMO_SVS', 									width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}				
				
				
/*
				,{field: "Precio"               			,title: 'UI_PRECIO', 			format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "Tasa"                 			,title: 'UI_TASA', 										width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdCondicionLiquidacion"           ,title: 'UI_CONDICION_LIQUIDACION_ABR',					width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "OperadorDirecto"         			,title: 'UI_OPER_DIRECTO', 								width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdEstado"               			,title: 'UI_ESTADO_OPER', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "DescEstadoCclv"     				,title: 'UI_DESCRIPCION_CCLV', 							width: "120px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FechaVencimiento"             	,title: 'UI_FECHA_VENCIMIENTO', 						template : " <label> #:  util.formatoDate(FechaVencimiento) #</label>" ,			 	width: "100px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
				,{field: "FolioComprobanteReferencia"      	,title: 'UI_FOLIO_COMP_ASOC', 	format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FolioComprobanteRelacionado"      ,title: 'UI_FOLIO_COMP_CONT', 	format: "{0:n0}",		width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdOrden"            				,title: 'UI_SECUENCIA_ORDEN', 	format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdAgente"               			,title: 'UI_AGENTE', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdMoneda"               			,title: 'UI_MONEDA', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "TipoGranatia"        				,title: 'UI_TIPO_GARANTIA', 							width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FondoAfp"            				,title: 'UI_FONDO_AFP', 								width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdTipoComprobante"        		,title: 'UI_TIPO_COMP_ADJ', 										width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdBolsa"              			,title: 'UI_ENTIDAD', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "DocumentoIdentidadEntidad"        ,title: 'UI_RUT_ENTI', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdBoveda"           				,title: 'UI_BOVEDA', 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "FolioTransaccion"          		,title: 'UI_FOLIO_TRANSACCION', 	format: "{0:n0}",	width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "UsoGarantia"     					,title: 'UI_GARANTIA', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "TopoOpePendiente"         		,title: 'UI_TIPO_OPE_PEN', 								width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "BlockEnvio"       				,title: 'UI_IND_BLOK_ENVIO', 							width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "NombreConcepto"        			,title: 'UI_CONCEPTO', 									width: "170px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "IdEmisor"               			,title: 'UI_EMISOR', 									width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
				,{field: "EsFungible"     					,title: 'UI_FUNGIBLE', 									width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
*/
				

				
	         
		];
	};
	
	var grillaArchivoDCV = undefined;
	$scope.confGrillaArchivoDCV = {
		dataSource	: dsArchivoDCV,
		columns		: $scope.columnasArchivoDCV(),		
		sortable	: true,
		pageable	: { pageSize : 25 },   //pagina localmente
		filterable 	: true,
		selectable  : "multiple, row",
		reorderable	: false,
		userSettings : true,
		groupable	: true,
		height		: 526,
		allowCopy	: true,
		resizable	: true,
	    autoBind	: true,
	    scrollable	: true,	    
	    gridCreated : function(grid) {
	    	grillaArchivoDCV = grid;
	    },

	};
	
   	//********* fin configuracion de objetos *********//

	
$scope.preparaParametros = function(filtros){
	parametrosConsulta = {};
	parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
	parametrosConsulta.p_IdUsuario = $scope.SesionIdUsuario
	parametrosConsulta.p_IdModulo = 'CUST'
	parametrosConsulta.p_IdPais = $scope.SesionIdPaisSistema

	parametrosConsulta.p_Fecha = util.dateSPdate($scope.data.kngFecha || '0001-01-01')
	parametrosConsulta.p_IdSocioNegocio = $scope.data.oConfiguracionSocioNegocio.idActual  || 0 
	parametrosConsulta.p_IdMercado = $scope.data.oConfiguracionMercado.idActual || ''
	parametrosConsulta.p_IdEstado = $scope.data.oConfiguracionEstadoMovimiento.idActual || ''	
	parametrosConsulta.p_IdTipoOperacion = $scope.data.oConfiguracionTipoOperacion.idActual || ''
	parametrosConsulta.p_IdTipoComprobante = $scope.data.oConfiguracionTipoComprobante.idActual || ''
	parametrosConsulta.p_IdTipoCuenta = $scope.data.oConfiguracionTipoCuenta.idActual || ''
	return parametrosConsulta;
	};
			
		
/*************************************************************************
* BUSCAR
**************************************************************************/	
	/** Transforma arreglo - string a archivo csv **/
	function ExportarArchivo(filename, rows) {

        var blob = new Blob([rows], { type: 'text/plain;charset=utf-8;' });
        if (navigator.msSaveBlob) { // IE 10+
            navigator.msSaveBlob(blob, filename);
        } else {
            var link = document.createElement("a");
            if (link.download !== undefined) { // feature detection
                // Browsers that support HTML5 download attribute
                var url = URL.createObjectURL(blob);
                link.setAttribute("href", url);
                link.setAttribute("download", filename);
                link.style.visibility = 'hidden';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    }

	
	
	
	$scope.btnExportarDCV = function(){
		if ($scope.seleccionMovimiento === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));		
		}
		else
		{	

			var info = {};			
			var txtArchivo = '';
			var Parametros = {};
			var Registros = [];
			bcsComunes.ventanaOverlay(idVentana, true);

		    // Parametros del servicio
			Parametros.P_IDEMPRESA = $scope.SesionIdEmpresa;
			Parametros.p_IdUsuario = $scope.SesionIdUsuario;
			Parametros.p_IdModulo  = 'CUS';
			Parametros.p_Registros = [];
						
			// Registros seleccionados
			$.each($scope.seleccionMovimiento,function(key,value){
				Parametros.p_Registros.push
				({     
					IdRegistro			: 0,
					IdOperacionPendiente: value.IdOperacionPendiente,
					IdTipoOperacion		: value.IdTipoOperacion,
					FolioOperacion		: value.FolioOperacion,
					Clave 				: ''
					
			    });
			});	
			
			
			// LLama a servicio que genera data para bajar a archivo
			//bcsServiciosMesa.GeneraArchivoDCV( Parametros, ArchivoDCV_OK, ArchivoDCV_Error );
			bcsServiciosMesa.GeneraArchivoDCV(Parametros).$promise.then(function(Resp){
				if ( Resp.P_OUT_ERRORESCONTADOR == "0" ) {
					 $.each(Resp.P_OUT_DATA, function(key,value) {					   	
						 txtArchivo = txtArchivo + Resp.P_OUT_DATA[key].Linea + '\r\n';
					 }); 					  

					 // Baja archivo en PC
					 ExportarArchivo('EXPOTDCV.Txt', txtArchivo);                     
					 bcsComunes.ventanaMsg({ conflictos:util.parseServerResponse(Resp) },idVentana);
					 grillaArchivoDCV.dataSource.setCallParameters($scope.preparaParametros($scope.data));

				} else {
					 if (Resp.P_OUT_ERRORESCONTADOR > 0) {
						 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
													conflictos:util.parseServerResponse(Resp)
												},	idVentana);
					 } else
						 if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
							 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
														conflictos:util.parseServerResponse(Resp)
													},	idVentana);
						 }
				}

				bcsComunes.ventanaOverlay(idVentana, false);
			},function(Resp){
				bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
			
		};
		
		var ArchivoDCV_OK = function(Resp) {
			if ( Resp.P_OUT_ERRORESCONTADOR == "0" ) {
				 $.each(Resp.P_OUT_DATA, function(key,value) {					   	
					 txtArchivo = txtArchivo + Resp.P_OUT_DATA[key].Linea;
				 }); 					  

				 // Baja archivo en PC
				 ExportarArchivo('EXPOTDCV.Txt', txtArchivo);                     
				 bcsComunes.ventanaMsg({ conflictos:util.parseServerResponse(Resp) },idVentana);
				 grillaArchivoDCV.dataSource.setCallParameters($scope.preparaParametros($scope.data));
				 
			} else {
				 if (Resp.P_OUT_ERRORESCONTADOR > 0) {
					 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
    				    	                    conflictos:util.parseServerResponse(Resp)
					 						},	idVentana);
				 } else
					 if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
						 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
    						                     	conflictos:util.parseServerResponse(Resp)
    						                    },	idVentana);
					 }
			}
    		
			bcsComunes.ventanaOverlay(idVentana, false);
    	   };
   		};
   		
		var ArchivoDCV_Error = function(respuesta) {
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
			bcsComunes.ventanaOverlay(idVentana, false);
		};
		

	
	
	$scope.buscarOperacionesPendientes = function(){
		grillaArchivoDCV.dataSource.setCallParameters($scope.preparaParametros($scope.data));
	};
	

	$scope.limpiarFiltros = function() {
		$scope.data.kngFecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.oConfiguracionMercado.idActual = ''
		$scope.data.oConfiguracionSocioNegocio.idActual  = 0 
		$scope.data.oConfiguracionEstadoMovimiento.idActual = ''
		$scope.data.oConfiguracionTipoOperacion.idActual = ''
		$scope.data.oConfiguracionTipoComprobante.idActual = ''
		$scope.data.oConfiguracionTipoCuenta.idActual = ''
	}

	$scope.btnClave = function() {

		if ($scope.seleccionMovimiento === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));		
		}
		else
		{	
			//Abre Modal de edición
			var info = {};
			info.Registro = $scope.seleccionMovimiento;
			
			var ventanaClave = bcsComunes.ventanaAbrir("IngresarClaveUna", info, false, true);		
	        var promesa=bcsComunes.ventanaPromesa(ventanaClave);
	
	        promesa.then(function(data){
	        	grillaArchivoDCV.dataSource.setCallParameters($scope.preparaParametros($scope.data));
			});
		};
	};		

	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		grillaArchivoDCV.select(grillaArchivoDCV.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		grillaArchivoDCV.clearSelection();
	};
		
	
	$scope.btnCompletar = function() {

		if ($scope.seleccionMovimiento === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));		
		}
		else
		{	
			
			var msg = bcsAlerta.crear("PREGUNTA_SI_NO",
					  $translate.instant('UI_MENSAJE_COMPLETAR_OPERACION'));//+' . '+$translate.instant('UI_CONTINUAR'));

			msg.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					
					var info = {};			
					var Registros = [];
					var Parametros = {};					
					bcsComunes.ventanaOverlay(idVentana, true);

				    // Parametros del servicio
					info.Registro = $scope.seleccionMovimiento;					
					Parametros.P_IdEmpresa = $scope.SesionIdEmpresa;
					Parametros.p_IdUsuario = $scope.SesionIdUsuario;
					Parametros.p_IdModulo  = 'CUS';
					Parametros.p_Registros = [];
								
					// Registros seleccionados
					$.each($scope.seleccionMovimiento,function(key,value){
						Parametros.p_Registros.push
						({     
							IdOperacionPendiente: value.IdOperacionPendiente,
							IdTipoOperacion		: value.IdTipoOperacion,
							FolioOperacion		: value.FolioOperacion,
							IdEstado			: 'TER'
					    });
					});	
					
					
					// LLama a servicio que genera data para bajar a archivo
					//bcsServiciosMesa.GeneraArchivoDCV( Parametros, ArchivoDCV_OK, ArchivoDCV_Error );
					bcsServiciosMesa.CambioEstadoOperacionPendiente(Parametros).$promise.then(function(Resp){
						if ( Resp.P_OUT_ERRORESCONTADOR == "0" ) {
							// Lee de nuevo 	

							 bcsComunes.ventanaMsg({ conflictos:util.parseServerResponse(Resp) },idVentana);
							 grillaArchivoDCV.dataSource.setCallParameters($scope.preparaParametros($scope.data));;

						} else {
							 if (Resp.P_OUT_ERRORESCONTADOR > 0) {
								 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
															conflictos:util.parseServerResponse(Resp)
														},	idVentana);
							 } else
								 if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
									 bcsComunes.ventanaMsg({	mensaje:$translate.instant(""),
																conflictos:util.parseServerResponse(Resp)
															},	idVentana);
								 }
						}

						bcsComunes.ventanaOverlay(idVentana, false);
					},function(Resp){
						bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
						bcsComunes.ventanaOverlay(idVentana, false);
					});
					 
				}
			});
			
		};
	};		
	
	
});

