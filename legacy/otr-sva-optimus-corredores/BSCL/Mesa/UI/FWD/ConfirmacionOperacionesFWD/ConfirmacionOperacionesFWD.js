(function() {
	angular.module('ModuloMesa').controller('ConfirmacionOperacionesFWDCtrl',
	function(	$scope,
				$translate,
				$resource,
				$timeout,
				bcsComunes,
				bcsAlerta,
				OPCIONES_BCSALERTA,
				bcsServicioPlataforma,
				bcsServiciosMesa )
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var grillaForward = undefined;		
		var OperacionesOk = false;

		$scope.data = {};
		$scope.data.seleccionMovimiento = false;
		
		//Configurar al principio
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.HabilitoBotones = true;

		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento : { "field"  : "IdTipoInstrumento","operator": "eq", "value" : "FWD" }
		    }
		};
		
		$scope.data.oSocioNegocio = {
			filtros: {
				IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
			},
		};
		
		$scope.data.oConfiguracionMonedaPrincipal={
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
				Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
				OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 1 }
			}
		};
		
		$scope.data.oConfiguracionMonedaSecundaria={
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
				Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
				OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 2 }
			}
		};

		
		// SELECCION DE UN REGISTRO
		$scope.$watchCollection('data.seleccionMovimiento',function(newval,oldval){
			if(newval === undefined) return;
			if(newval === false) return;
			$scope.HabilitoBotones = (	newval[0].Estado === 'VIG' && 
										newval[0].EstadoConfirmacion === 'NCF' &&
										util.date($scope.data.kngFechaOperacion).format("YYYY-MM-DD") === $scope.SesionFechaSistema );
		});
		
		
		var Transaccion_OK = function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){		
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			else{			
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			bcsComunes.ventanaPromesa(id).then(function(result){
				bcsComunes.ventanaOverlay($scope.winID, false);
				// Vuelve a cargar datos en la grilla...
				$scope.data.BuscarOperaciones();
			});			
		};
		
		
		var Transaccion_Err = function(respuesta){
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.BuscarOperaciones();
		};
		
		/*********************************************
		 * BUSCAR OPERACIONES
		 ********************************************/

		$scope.data.BuscarOperaciones = function(){
			var Parametros = {};
			Parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
			Parametros.p_IdUsuario = $scope.SesionIdUsuario;
			Parametros.p_IdModulo = 'MES';
			Parametros.p_IdTipoProducto = 'MEFW';
			Parametros.p_IdTipoOperacion = $scope.data.oConfiguracionTipoInstrumento.idActual;
			Parametros.p_IdMonedaPrincipal = $scope.data.oConfiguracionMonedaPrincipal.idActual;
			Parametros.p_IdMonedaSecundaria = $scope.data.oConfiguracionMonedaSecundaria.idActual;
			Parametros.p_EstadoConfirmacion = $scope.data.oConfiguracionEstado.idActual;
			Parametros.p_IdCliente = $scope.data.oSocioNegocio.seleccionado.IdCliente||0;
			Parametros.p_Fecha = util.date($scope.data.kngFechaOperacion).format("YYYY-MM-DD");		
			
			bcsComunes.ventanaOverlay(idVentana, true);
			bcsServiciosMesa.ConsultarOperacionesFWRF(Parametros).$promise.then(function(Resp){
				var OperacionForward = [];
				for (var pos in Resp.P_OUT_OPERACIONES){
					var Campos_Forward = {};
					Campos_Forward.FolioOperacion = Resp.P_OUT_OPERACIONES[pos].FolioOperacion;
					Campos_Forward.FechaOperacion = Resp.P_OUT_OPERACIONES[pos].FechaOperacion;
					Campos_Forward.FechaLiquidacion = Resp.P_OUT_OPERACIONES[pos].FechaLiquidacion;
					Campos_Forward.FechaEntregamos  = Resp.P_OUT_OPERACIONES[pos].FechaEntregamos ;
					Campos_Forward.FechaRecibimos  = Resp.P_OUT_OPERACIONES[pos].FechaRecibimos ;
					Campos_Forward.IdCliente  = Resp.P_OUT_OPERACIONES[pos].IdCliente ;
					Campos_Forward.IdInstrumento  = Resp.P_OUT_OPERACIONES[pos].IdInstrumento ;
					Campos_Forward.NombreInstrumento = Resp.P_OUT_OPERACIONES[pos].NombreInstrumento;
					Campos_Forward.Nemotecnico  = Resp.P_OUT_OPERACIONES[pos].Nemotecnico ;
					Campos_Forward.TipoOperacion  = Resp.P_OUT_OPERACIONES[pos].TipoOperacion ;
					Campos_Forward.RutCliente  = Resp.P_OUT_OPERACIONES[pos].RutCliente ;
					Campos_Forward.NombreCliente   = Resp.P_OUT_OPERACIONES[pos].NombreCliente  ;
					Campos_Forward.CodigoPropositoPortafolio  = Resp.P_OUT_OPERACIONES[pos].Portafolio;
					Campos_Forward.IdMonedaPrincipal = Resp.P_OUT_OPERACIONES[pos].MonedaPrincipal;
					Campos_Forward.IdMonedaSecundaria = Resp.P_OUT_OPERACIONES[pos].MonedaSecundaria;
					Campos_Forward.MontoMonedaPrincipal = Resp.P_OUT_OPERACIONES[pos].MontoMonedaPrincipal;
					Campos_Forward.MontoMonedaSecundaria = Resp.P_OUT_OPERACIONES[pos].MontoMonedaSecundaria;
					Campos_Forward.TipoCambioCierre  = Resp.P_OUT_OPERACIONES[pos].TCCierre ;
					Campos_Forward.TipoCambioCosto  = Resp.P_OUT_OPERACIONES[pos].TCCosto ;
					Campos_Forward.ParidadCierre  = Resp.P_OUT_OPERACIONES[pos].ParidadCierre ;
					Campos_Forward.MontoEquivalente = Resp.P_OUT_OPERACIONES[pos].MontoEquivalente;
					Campos_Forward.CodigoUsuario  = Resp.P_OUT_OPERACIONES[pos].CodigoUsuario ;
					Campos_Forward.Estado  = Resp.P_OUT_OPERACIONES[pos].EstadoOperacion ;
					Campos_Forward.Mesa  = Resp.P_OUT_OPERACIONES[pos].Mesa ;
					Campos_Forward.Producto  = Resp.P_OUT_OPERACIONES[pos].Producto ;
					Campos_Forward.IdUsuario  = Resp.P_OUT_OPERACIONES[pos].IdUsuario ;
					Campos_Forward.IdPortafolio = Resp.P_OUT_OPERACIONES[pos].IdPortafolio;
					Campos_Forward.IdTipoInstrumento = Resp.P_OUT_OPERACIONES[pos].IdTipoInstrumento;
					Campos_Forward.EstadoConfirmacion = Resp.P_OUT_OPERACIONES[pos].EstadoConfirmacion;
					Campos_Forward.IdTipoCanalIngreso = Resp.P_OUT_OPERACIONES[pos].Canal;
					Campos_Forward.IdMovimientoComprobante = Resp.P_OUT_OPERACIONES[pos].IdMovimientoComprobante;
					Campos_Forward.IdOperacion = Resp.P_OUT_OPERACIONES[pos].IdOperacion;
					Campos_Forward.IdCartera = Resp.P_OUT_OPERACIONES[pos].IdCartera;
                    Campos_Forward.FolioCartera = Resp.P_OUT_OPERACIONES[pos].FolioCartera;
					OperacionForward.push(Campos_Forward);
				}
				
				$scope.confGrillaForward.dataSource.data(OperacionForward);
				bcsComunes.ventanaOverlay(idVentana, false);
			},function(Resp){
				bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
				bcsComunes.ventanaOverlay(idVentana, false);
			});
		};
		
		
		 /*********************************************
		 * CONFIRMAR OPERACIONES
		 ********************************************/
		 	
		$scope.data.ConfirmarOperacion = function(){
			if ($scope.data.seleccionMovimiento === undefined || $scope.data.seleccionMovimiento === false){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}
			else if ($scope.data.seleccionMovimiento.length > 1){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
			}
			else{
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_CONFIRMAR_OPERACION_FORWARD') );
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay($scope.winID, true);

						// Registros seleccionados
						var parametros = {};
						OperacionesOk = true;
						
						if(OperacionesOk === false){
							bcsComunes.msg("warning","info",$translate.instant('UI_ESTADOS_INVALIDOS_CONFIRMACION'));
						}
						else {
							$.each($scope.data.seleccionMovimiento,function(key,value){
							OperacionesOk = OperacionesOk && value.Estado === 'VIG' && value.EstadoConfirmacion === 'NCF';
							parametros.p_IdOperacion        = value.IdOperacion;
							parametros.p_IdCliente          = value.IdCliente;
						});
							parametros.p_IdEmpresa			= $scope.SesionIdEmpresa;
							parametros.p_IdUsuario			= $scope.SesionIdUsuario;
							parametros.p_IdModulo			= 'MES';
		                    parametros.p_IdMesa             = 'MEFW'
							bcsComunes.log().info(JSON.stringify(parametros));	
							bcsServiciosMesa.ConfirmarOperacionesFWRF(parametros, Transaccion_OK, Transaccion_Err);
				       	}
					}
				});
				return;
			}
		};
		

		/*********************************************
		 * RECHAZAR OPERACIONES
		 ********************************************/

		$scope.data.RechazarOperacion = function(){
			if ($scope.data.seleccionMovimiento === undefined || $scope.data.seleccionMovimiento === false){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}
			else{
				if ($scope.data.seleccionMovimiento.length > 1 ){
					bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}	
				else{
					var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_RECHAZARA_LA_OPERACION'));
					t.result.then(function(result){
						bcsComunes.ventanaOverlay($scope.winID, true);
						var tablaDatosForward = {};
						if (result===OPCIONES_BCSALERTA.ACEPTAR){
                            
								var datosRechazarForward = {
				                p_IdEmpresa 				: $scope.SesionIdEmpresa, //saca de variables de entorno
				                p_IdPais					: $scope.SesionIdPaisSistema,//saca de variables de entorno
				                p_IdUsuario 				: $scope.SesionIdUsuario, //saca de variables de entorno
				                p_IdBolsa					: $scope.SesionBolsaDefectoSistema,//saca de variables de entorno
				                p_IdSucursal				: 0,
				                p_IdModulo 					: 'MFW',
				                p_UsaReglasNegocio 			: 'N',
				                p_IdEstadoConfirmacion	: 'RZD',
				                p_tablaAnulacion    : [{ IdOperacion: $scope.data.seleccionMovimiento[0].IdOperacion,
				                                         FolioCartera: $scope.data.seleccionMovimiento[0].FolioCartera }]
			                    };	
								bcsComunes.log().info(JSON.stringify(datosRechazarForward));	
							    bcsServiciosMesa.AnulacionForward(datosRechazarForward, Transaccion_OK, Transaccion_Err)
							}
							else {
								bcsComunes.ventanaOverlay($scope.winID, false);
							}					
					});
					return;
				}
			}
		};

        
        /*********************************************
	 * MODIFICAR FORAMAS DE PAGO
	 ********************************************/

        $scope.data.ModificarFormaPago = function(){		
		  if ($scope.data.seleccionMovimiento === undefined || $scope.data.seleccionMovimiento === false){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}
			else{
				if ($scope.data.seleccionMovimiento.length > 1 ){
					bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}	
				else{
					var form = {};
					form.Opcion			= 'MODIFICARPAGO';
					form.IdCartera		= $scope.data.seleccionMovimiento[0].IdCartera;
					form.IdOperacion	= $scope.data.seleccionMovimiento[0].IdOperacion;
					form.IdTipoProducto = 'MEFW';
					form.FechaOperacion = util.date($scope.data.seleccionMovimiento[0].FechaOperacion).format("YYYY-MM-DD");
					form.IdEstado		= $scope.data.seleccionMovimiento[0].Estado;
					form.EstaAnticipada	= false;
			
					var IdVentanamodal = bcsComunes.ventanaAbrir("MesaForwardModal", form, false, false, idVentana);
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
						$scope.data.BuscarOperaciones();
					});
				}
			}
	    };
	    
    
		/*********************************************
		 * DATASOURCES
		 ********************************************/
		var dsDataForward = new bcs.DataSource({
			type : "array",
			definition: {
	    		pageSize : 200,
				data : [],
                schema: {
                	model: {
                		fields: {
                			FolioOperacion						: {type: "number"},
							FechaOperacion						: {type: "sysdate"},
							FechaLiquidacion					: {type: "sysdate"},
							FechaEntregamos  					: {type: "sysdate"},
							FechaRecibimos  					: {type: "sysdate"},
							IdInstrumento			 			: {type: "number"},
							IdTipoInstrumento       			: {type: "string"},							
							NombreInstrumento       			: {type: "string"},
							Nemotecnico       					: {type: "string"},
							TipoOperacion						: {type: "string"},
							IdCliente				 			: {type: "number"},							
							RutCliente							: {type: "string"},
							NombreCliente						: {type: "string"},
							CodigoPropositoPortafolio 			: {type: "string"},
							IdPortafolio						: {type: "number"},
							IdMonedaPrincipal					: {type: "string"},
							IdMonedaSecundaria					: {type: "string"},
							MontoMonedaPrincipal				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoMonedaSecundaria				: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							TipoCambioCierre					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							TipoCambioCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCierre						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoEquivalente					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							CodigoUsuario						: {type: "string"},
							Estado								: {type: "string"},
							EstadoConfirmacion					: {type: "string"},							
							Mesa								: {type: "string"},
							Producto							: {type: "string"},
							IdTipoCanalIngreso					: {type: "string"},
							IdMovimientoComprobante				: {type: "number"},
							IdOperacion						    : {type: "number"},
							IdCartera						    : {type: "number"},
							FolioCartera					    : {type: "number"},
						}
                	}
            	}
			}
		});

				       	
	 	$scope.columnasForward = function() {
			return  [		
			     {field: "FolioCartera", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
				,{field: "EstadoConfirmacion", title: "UI_ESTADO_CONFIRMACION", width: "40px", sortable : false, groupable: true }			     
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : true, groupable: true }
			    ,{field: "NombreInstrumento", title: "UI_NOMBRE_INSTRUMENTO", width: "130px", sortable : true, groupable: true }
			    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : false, groupable: true  }
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "150px", sortable : false, groupable: true  }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : false }
			    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : false, hidden: true, groupable: true }
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : false, groupable: true }
			    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "90px", sortable : false, groupable: true }
			    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : false, template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }		       				    
			    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : false, template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : false, template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : false, template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : false, template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>"}
			    ,{field: "FechaEntregamos", title: "UI_FECHA_ENTREGAMOS", width: "92px", sortable : false }
			    ,{field: "FechaRecibimos", title: "UI_FECHA_RECIBIMOS", width: "92px", sortable : false }
			    ,{field: "Estado", title: "UI_ESTADO_OPERACION", width: "40px", sortable : false, groupable: true  }				       				    
			    ,{field: "IdTipoCanalIngreso", title: "UI_CANAL", width: "40px", sortable : false, groupable: true  }
		    ]
	 	};
	 	
	 	
		$scope.confGrillaForward = {
			dataSource	: dsDataForward,
			columns		: $scope.columnasForward(),		 
			sortable	: true,
			pageable	: true, 
			excel       : { allPages:true },
			filterable 	: true,
			selectable  : "multiple, row",
			reorderable	: false,
			userSettings : true,
			groupable	: true,
			height		: 400,
			navigatable : true,			
			allowCopy	: true,
			resizable	: true,
		    autoBind	: true,
		    scrollable	: true,	    
		    gridCreated : function(grid) {
		    	grillaForward = grid;
		    },
		};
		
		
		//SUB-GRILLA FORMAS DE PAGO
		var dsFormaDePago = function(dataItem){
			var filtrosMovimientoForDePago = [
			  	{field: "IdOperacion", operator: "eq", value: Number(dataItem.IdCartera)},
			  	{field: "IdTipoProducto", operator: "eq", value: 'MEFW'}
		  	];
		  	return new bcs.DataSource({
		  		type : "odata",
		  		definition: {
		  			filter: filtrosMovimientoForDePago,
		  			serverFiltering 	: true,
		  			serverPaging 		: true,
		  			serverSorting 	: true,
		  			pageSize 			: 10, // Número de registros por página.
		  			transport 		: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3567",dataType : "json"}},
		  			schema : {
		  				model: {
		  					fields : {  						
		  						RecibePagaDesc 				: { type: "string"},
		  						RecibePagaPortafolioDesc	: { type: "string"},
		  						NombreFormaDePago           : { type: "string"},
		  						IdMonedaComprobantePago		: { type: "string"},
		  						Monto						: { type: "number"},
		  						FechaLiquidacionComprobantePagoOperacion: { type: "date"},
		  						IdMedioDePago               : { type: "string"},   //columna oculta
		  						IdModalidadPago             : { type: "string"},   //columna oculta
		  					}
		  				}
		  			}
		  		}
		  	});
		};
		
		$scope.columnasFormasDePago = function(){
			return  [
				 {field : "RecibePagaDesc", title:'UI_TIPO_MOVIMIENTO', width : "40px", align: "left", sortable : false }
				,{field : "NombreFormaDePago", title:'UI_FORMA_DE_PAGO', width : "80px", align: "left", sortable : false }
				,{field : "IdMonedaComprobantePago", title: "UI_MONEDA", width: "30px", align: "left", sortable : false}
				,{field : "Monto", title:'UI_MONTO_LIQUIDACION', width : "40px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(Monto,'GEN','Monto',IdMonedaComprobantePago,'','')).valor #</label>" }				
				,{field : "FechaLiquidacionComprobantePagoOperacion", title:'UI_FECHA_LIQUIDACION', width : "40px", align: "right" }				
			 	
				//Columnas Ocultas  			
				,{field : "IdMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", align: "right", sortable : false, hidden: true}
				,{field : "IdModalidadPago", title:'UI_MODALIDAD', width : "80px", align: "right", sortable : false, hidden: true}
			];
		};
		
		//SUB-GRILLA FORMAS DE PAGO
		$scope.confGrillaFormasDePago = function(DataItem){
			return {
				dataSource		: dsFormaDePago(DataItem),
				columns			: $scope.columnasFormasDePago(),		
				sortable		: true,
				pageable		: true,
				height			: 120,
				width			: 200,
				selectable 		: "multiple row",
				autoBind        : true,
				navigatable     : true,
				allowCopy 		: true
			}
		};
		
	});
}());