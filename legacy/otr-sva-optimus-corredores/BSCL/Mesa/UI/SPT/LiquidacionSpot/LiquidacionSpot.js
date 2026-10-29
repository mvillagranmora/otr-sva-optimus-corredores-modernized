(function() {
	angular.module('ModuloMesa').controller('LiquidacionSpotCtrl',
	function(	$scope,
				$translate,
				$resource,
				$timeout,
				bcsComunes,
				bcsAlerta,OPCIONES_BCSALERTA,
				bcsConsultasMesa, bcsServicioPlataforma, $http,
				bcsServiciosMesa )
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var grillaSpot = undefined;		
		var OperacionesOk = false;

		$scope.data = {};
		$scope.data.seleccionMovimiento = false;
		
		//Configurar al principio
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.HabilitoBotones = true;
		$scope.HabilitarActualizarEstadoCaja = false;

		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento : { "field"  : "IdTipoInstrumento","operator": "eq", "value" : "SPT" }
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
										newval[0].EstadoConfirmacion === 'PEN' &&
										(newval[0].IdEstadoCaja === 'NRE' || newval[0].IdEstadoCaja === 'CNF' )&&
										util.date($scope.data.kngFechaOperacion).format("YYYY-MM-DD") === $scope.SesionFechaSistema );
			$scope.HabilitarActualizarEstadoCaja = newval[0].IdEstadoCaja === 'PEN' && newval[0].EstadoConfirmacion === 'PEN';
		});
		
		// Validación Spot Facturado
		var ConsultarSpotFacturado = new bcs.newDataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=485",
						dataType : "json",
					}
				},
				schema: {
					model: {
						fields: {
							IdOperacionSpot	: {type: "number"},
							IdResultado		: {type: "number"},
							Codigo			: {type: "string"}
						}
					}
				}
			},
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
		
		
		$scope.data.RechazarOperacion = function(){
			if ($scope.data.seleccionMovimiento === undefined){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}
			else{
				if ($scope.data.seleccionMovimiento.length > 1 ){
					bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}	
				else{
					//logica para ffmm
					var tipoCanalIngreso = $scope.data.seleccionMovimiento[0].IdTipoCanalIngreso;	
					if(tipoCanalIngreso === "FMM"){
						var msgResult={};
		                msgResult.P_OUT_MENSAJES= [{id: 0, tipo: "ERROR", codigo: "COD", descripcion: $translate.instant('NO_RECHAZA_OPERACION_CANAL_FFMM')}];
					    bcsComunes.ventanaMsg({mensaje:"No se pudo realizar el rechazo",conflictos:util.parseServerResponse(msgResult)},$scope.winID);
						return;
					}
					//
					var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_RECHAZARA_LA_OPERACION'));
					t.result.then(function(result){
						bcsComunes.ventanaOverlay($scope.winID, true);
						if (result===OPCIONES_BCSALERTA.ACEPTAR){
							var tablaDatosSpot = [];		
							var ObjSpot = {
								"NumeroOperacion": $scope.data.seleccionMovimiento[0].IdOperacionSpot
							};
							tablaDatosSpot.push(ObjSpot);
							
							// SE ENVIA CON MODULO = TES PARA QUE PUEDA ANULAR LA OP. A PESAR DE
							// NO SER EL USUARIO CREADOR.
							var parametros = {};
							parametros.p_IdEmpresa			= $scope.SesionIdEmpresa;
							parametros.p_IdUsuario			= $scope.SesionIdUsuario;
							parametros.p_IdModulo			= 'TES';
							parametros.p_UsaReglasNegocio	= 'S';
							parametros.p_DatosOperacion		= tablaDatosSpot;

							bcsComunes.log().info(JSON.stringify(parametros));	
					       	bcsServiciosMesa.RechazoLiquidacionSpot(parametros, Transaccion_OK, Transaccion_Err);
						}
					});
					return;
				}
			}
		};

		$scope.data.ActualizarEstadoCaja = function(){
			if ($scope.data.seleccionMovimiento === undefined){	
				bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_REGISTROS_VALIDOS'));
				return;
			}

			var t = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_CONFIRMAR_CAMBIO_ESTADO_CAJA_OPERACION'));
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay(idVentana, true);

						var parametros = {};
						parametros.EstadoCajaModificar = [];
						OperacionesOk = true;
						
						$.each($scope.data.seleccionMovimiento,function(key,value){
							OperacionesOk = value.EstadoConfirmacion === 'PEN' && value.IdEstadoCaja === 'PEN';
							parametros.EstadoCajaModificar.push({IdOperacion: value.IdOperacionSpot, NuevoEstado: 'CNF'});
						});								
						
						if(OperacionesOk === false){
							bcsComunes.msg("warning","info",$translate.instant('UI_ESTADOS_INVALIDOS_CONFIRMACION_ESTADO_CAJA'));
						}
						else {
							parametros.p_IdEmpresa			= $scope.SesionIdEmpresa;
							parametros.p_IdUsuario			= $scope.SesionIdUsuario;
							parametros.p_IdModulo			= 'MSP';
							parametros.p_UsaReglasNegocio	= 'S';

							bcsComunes.log().info(JSON.stringify(parametros));	
							bcsServiciosMesa.ModificarEstadoCajaOperacionSpot(parametros, Transaccion_OK, Transaccion_Err);
				       	}
					}
				});
				return;

			bcsServiciosMesa.ModificarEstadoCajaOperacionSpot();
		}
		
		
		$scope.data.ConfirmarOperacion = function(){
			if ($scope.data.seleccionMovimiento === undefined){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
			}
			else{
				const resumenConfirmacion = $scope.data.seleccionMovimiento.reduce(function(acc, value) {
					acc[value.OperacionNoFacturable === 'S' ? 'cantidadTesoreria' : 'cantidadFacturacion']++;
					return acc;
				}, {
					cantidadFacturacion: 0,
					cantidadTesoreria: 0
				});
				
				let mensajeConfirmacion = 'UI_CONFIRMAR_OPERACION';
				
				if (resumenConfirmacion.cantidadFacturacion === 0 && resumenConfirmacion.cantidadTesoreria > 0) {
					mensajeConfirmacion = 'UI_CONFIRMACION_SPOT_TESORERIA';
				}
				
				if (resumenConfirmacion.cantidadFacturacion > 0 && resumenConfirmacion.cantidadTesoreria > 0) {
					mensajeConfirmacion = 'UI_CONFIRMACION_SPOT_FACTURACION_TESORERIA';
				}
				
				var t = bcsAlerta.crear("PREGUNTA", $translate.instant(mensajeConfirmacion, resumenConfirmacion));
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay($scope.winID, true);

						// Registros seleccionados
						var parametros = {};
						parametros.p_DatosOperacion = [];
						OperacionesOk = true;
						
						$.each($scope.data.seleccionMovimiento,function(key,value){
							OperacionesOk = OperacionesOk && value.Estado === 'VIG' && value.EstadoConfirmacion === 'PEN' && (value.IdEstadoCaja === 'CNF' || value.IdEstadoCaja === 'NRE');
							parametros.p_DatosOperacion.push({NumeroOperacion: value.IdOperacionSpot});
						});								
						
						if(OperacionesOk === false){
							bcsComunes.msg("warning","info",$translate.instant('UI_ESTADOS_INVALIDOS_CONFIRMACION'));
						}
						else {
							parametros.p_IdEmpresa			= $scope.SesionIdEmpresa;
							parametros.p_IdUsuario			= $scope.SesionIdUsuario;
							parametros.p_IdModulo			= 'MSP';
							parametros.p_UsaReglasNegocio	= 'S';

							bcsComunes.log().info(JSON.stringify(parametros));	
							bcsServiciosMesa.ConfirmaLiquidacionSpot(parametros, Transaccion_OK, Transaccion_Err);
				       	}
					}
				});
				return;
			}
		};		
		
		var cargarOperacion = function(varOpcion, seleccionMovimiento){
			if(seleccionMovimiento !== undefined && seleccionMovimiento[0].Mesa === 'MEME'){
				var form = {};
				form.Opcion			= varOpcion;
				form.IdOperacion	= seleccionMovimiento[0].IdOperacionSpot;
				form.IdOperacionUI	= seleccionMovimiento[0].IdOperacionSpot;
				form.IdTipoProducto = seleccionMovimiento[0].Mesa;
				//form.FechaOperacion = util.dateSPdate(seleccionMovimiento[0].FechaOperacion);
				form.FechaOperacion = seleccionMovimiento[0].FechaOperacion;
				form.EsEditarFormaDePago = true;
				
				if(varOpcion === 'MODIFICAR' || varOpcion === 'ANULAR'){
					var filtros = [ { field: "IdOperacionSpot",	operator: "eq", value: seleccionMovimiento[0].IdOperacionSpot },
					                { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa } ];
					ConsultarSpotFacturado.query({ filter: filtros }).then(function(){
						if(ConsultarSpotFacturado.view().length > 0){
							if(ConsultarSpotFacturado.view()[0].PuedeModificarAnular === 0){
								ConsultarSpotFacturado.view()[0].P_OUT_ERRORESCONTADOR = 1;
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant(ConsultarSpotFacturado.view()[0].CodigoModificarAnular),
									conflictos:util.parseServerResponse(ConsultarSpotFacturado.view()[0])
								},idVentana);
								return;
							}
							else{
								var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
								var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
								promesa.then(function(data){
									$scope.data.BuscarOperaciones();
								});
							} 
						}
						else{
							var IdVentanamodal = bcsComunes.ventanaAbrir("consutaOperacionSpot", form, false, false, idVentana);
							var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
							promesa.then(function(data){
								$scope.data.BuscarOperaciones();
							});
						}
					});
				}
			}
		};
		
		$scope.data.edicionFormaDePago = function(seleccionMovimiento){ 
			if (seleccionMovimiento === false) {
				bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'),
										 $scope.winID);
				return;
			} else {
				var tipoCanalIngreso = seleccionMovimiento[0].IdTipoCanalIngreso;	
				
				if(tipoCanalIngreso === "DATATEC"){
					bcsComunes.msg("warning","warning",$translate.instant('NO_MODIFICAR_OPERACION_CANAL_DATATEC'));
				}else{
					cargarOperacion('MODIFICAR',seleccionMovimiento);
				}				
			}
		};
		
		$scope.ConsultarContacto = function(datos){ 
			// Muestra la ventana con la información de contacto
			if (datos[0].IdContacto > 0) {
				var contacto = {
					Nombre: datos[0].NombreContacto,
					Correo: datos[0].CorreoContacto,
					Telefono: datos[0].TelefonoContacto,
					Calle: datos[0].Calle,
					NumeroCalle: datos[0].NumeroCalle,
					NombreCiudad: datos[0].NombreCiudad,
					NombrePais: datos[0].NombrePais
				};
				
				var ventanaContacto = bcsComunes.ventanaAbrir(
					"ConsultarContactoSpot", contacto, false, true, $scope.winID);
				var promesa = bcsComunes
					.ventanaPromesa(ventanaContacto); 
			}
		};
		
		$scope.data.BuscarOperaciones = function(){
			var Parametros = {};
			Parametros.pFechaOperacion = util.date($scope.data.kngFechaOperacion).format("YYYY-MM-DD");
			Parametros.pIdTipoOperacion = $scope.data.oConfiguracionTipoInstrumento.idActual;
			Parametros.pIdMonedaPrincipal = $scope.data.oConfiguracionMonedaPrincipal.idActual;
			Parametros.pIdMonedaSecundaria = $scope.data.oConfiguracionMonedaSecundaria.idActual;
			Parametros.pIdEstadoConfirmacion = $scope.data.oConfiguracionEstado.idActual;
			Parametros.pIdSocioNegocio = $scope.data.oSocioNegocio.seleccionado.IdCliente||0;
			Parametros.pIdEstadoCaja = $scope.data.oConfiguracionEstadoCaja.idActual;
			
			bcsComunes.ventanaOverlay(idVentana, true);
			bcsServiciosMesa.LiquidacionSpot(Parametros).$promise.then(function(Resp){
				var OperacionSpot = [];
				for (var pos in Resp.P_OUT_SPOT){
					var Campos_Spot = {};
					Campos_Spot.FolioOperacion = Resp.P_OUT_SPOT[pos].FolioOperacion;
					Campos_Spot.FechaOperacion = Resp.P_OUT_SPOT[pos].FechaOperacion;
					Campos_Spot.FechaLiquidacion = Resp.P_OUT_SPOT[pos].FechaLiquidacion;
					Campos_Spot.FechaEntregamos  = Resp.P_OUT_SPOT[pos].FechaEntregamos ;
					Campos_Spot.FechaRecibimos  = Resp.P_OUT_SPOT[pos].FechaRecibimos ;
					Campos_Spot.IdCliente  = Resp.P_OUT_SPOT[pos].IdCliente ;
					Campos_Spot.IdInstrumento  = Resp.P_OUT_SPOT[pos].IdInstrumento ;
					Campos_Spot.NombreInstrumento = Resp.P_OUT_SPOT[pos].NombreInstrumento;
					Campos_Spot.Nemotecnico  = Resp.P_OUT_SPOT[pos].Nemotecnico ;
					Campos_Spot.IdTipoOperacion = Resp.P_OUT_SPOT[pos].IdTipoOperacion;
					Campos_Spot.TipoOperacion  = Resp.P_OUT_SPOT[pos].TipoOperacion ;
					Campos_Spot.RutCliente  = Resp.P_OUT_SPOT[pos].RutCliente ;
					Campos_Spot.NombreCliente   = Resp.P_OUT_SPOT[pos].NombreCliente  ;
					Campos_Spot.CodigoPropositoPortafolio  = Resp.P_OUT_SPOT[pos].CodigoPropositoPortafolio ;
					Campos_Spot.IdMonedaPrincipal = Resp.P_OUT_SPOT[pos].IdMonedaPrincipal;
					Campos_Spot.IdMonedaSecundaria = Resp.P_OUT_SPOT[pos].IdMonedaSecundaria;
					Campos_Spot.MontoMonedaPrincipal = Resp.P_OUT_SPOT[pos].MontoMonedaPrincipal;
					Campos_Spot.MontoMonedaSecundaria = Resp.P_OUT_SPOT[pos].MontoMonedaSecundaria;
					Campos_Spot.TipoCambioCierre  = Resp.P_OUT_SPOT[pos].TipoCambioCierre ;
					Campos_Spot.TipoCambioCosto  = Resp.P_OUT_SPOT[pos].TipoCambioCosto ;
					Campos_Spot.TipoDocumento  = Resp.P_OUT_SPOT[pos].TipoDocumento ;
					Campos_Spot.ParidadCierre  = Resp.P_OUT_SPOT[pos].ParidadCierre ;
					Campos_Spot.ParidadCosto  = Resp.P_OUT_SPOT[pos].ParidadCosto ;
					Campos_Spot.MontoEquivalente = Resp.P_OUT_SPOT[pos].MontoEquivalente;
					Campos_Spot.CodigoUsuario  = Resp.P_OUT_SPOT[pos].CodigoUsuario ;
					Campos_Spot.Estado  = Resp.P_OUT_SPOT[pos].Estado ;
					Campos_Spot.IdMesa  = Resp.P_OUT_SPOT[pos].IdMesa ;
					Campos_Spot.Mesa  = Resp.P_OUT_SPOT[pos].Mesa ;
					Campos_Spot.OrigenForward  = Resp.P_OUT_SPOT[pos].OrigenForward ;
					Campos_Spot.Producto  = Resp.P_OUT_SPOT[pos].Producto ;
					Campos_Spot.CodigoComercio  = Resp.P_OUT_SPOT[pos].CodigoComercio ;
					Campos_Spot.IdUsuario  = Resp.P_OUT_SPOT[pos].IdUsuario ;
					Campos_Spot.IdPortafolio = Resp.P_OUT_SPOT[pos].IdPortafolio;
					Campos_Spot.IdTipoInstrumento = Resp.P_OUT_SPOT[pos].IdTipoInstrumento;
					Campos_Spot.EstadoConfirmacion = Resp.P_OUT_SPOT[pos].EstadoConfirmacion;
					Campos_Spot.IdTipoCanalIngreso = Resp.P_OUT_SPOT[pos].IdTipoCanalIngreso;
					Campos_Spot.IdMovimientoComprobante = Resp.P_OUT_SPOT[pos].IdMovimientoComprobante;
					Campos_Spot.IdOperacionSpot = Resp.P_OUT_SPOT[pos].IdOperacionSpot;
					Campos_Spot.SecuenciaAsignacionRv = Resp.P_OUT_SPOT[pos].IdAsignacion;
					Campos_Spot.OperacionNoFacturable = Resp.P_OUT_SPOT[pos].OperacionNoFacturable;
					Campos_Spot.IdEstadoCaja = Resp.P_OUT_SPOT[pos].IdEstadoCaja;
					Campos_Spot.EstadoCaja = Resp.P_OUT_SPOT[pos].EstadoCaja;
					OperacionSpot.push(Campos_Spot);
				}
				
				$scope.confGrillaSpot.dataSource.data(OperacionSpot);
				bcsComunes.ventanaOverlay(idVentana, false);
			},function(Resp){
				bcsComunes.msg("error","error",$translate.instant('UI_ERROR_GENERACION'));
				bcsComunes.ventanaOverlay(idVentana, false);
			});
		};
		
		
		/*********************************************
		 * DATASOURCES
		 ********************************************/
		var dsDataSpot = new bcs.DataSource({
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
							IdTipoOperacion						: {type: "string"},
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
							TipoDocumento			 			: {type: "string"},
							ParidadCierre						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							ParidadCosto						: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaPrincipal,'','').excelFormat;}},
							MontoEquivalente					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,item.IdTipoInstrumento,'Monto',item.IdMonedaSecundaria,'','').excelFormat;}},
							CodigoUsuario						: {type: "string"},
							Estado								: {type: "string"},
							EstadoConfirmacion					: {type: "string"},							
							Mesa								: {type: "string"},
							Producto							: {type: "string"},
							IdTipoCanalIngreso					: {type: "string"},
							IdMovimientoComprobante				: {type: "number"},
							IdOperacionSpot						: {type: "number"},
							SecuenciaAsignacionRv				: {type: "number"},
							OperacionNoFacturable				: {type: "string"},
							IdEstadoCaja                        : {tupe: "string"},
							EstadoCaja                          : {type: "string"}
						}
                	}
            	}
			}
		});

				       	
	 	$scope.columnasSpot = function() {
			return  [		
			     {field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable : true }
				,{field: "EstadoConfirmacion", title: "UI_ESTADO_CONFIRMACION", width: "40px", sortable : true, groupable: true }			     
			    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable : true, groupable: true }
			    ,{field: "NombreInstrumento", title: "UI_NOMBRE_INSTRUMENTO", width: "130px", sortable : true, groupable: true }
			    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable : true, groupable: true  }
			    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "150px", sortable : true, groupable: true  }
			    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : true }
			    ,{field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable : true, hidden: true, groupable: true }
			    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "90px", sortable : true, groupable: true }
			    ,{field: "IdMonedaSecundaria", title: "UI_MONEDA_SECUNDARIA", width: "90px", sortable : true, groupable: true }
			    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "100px", sortable : true, template : " <label> #:  (util.bcs_formato(MontoMonedaPrincipal,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }		       				    
			    ,{field: "TipoCambioCierre", title: "UI_TC_CIERRE", width: "60px", sortable : true, template : " <label> #:  (util.bcs_formato(TipoCambioCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "TipoCambioCosto", title: "UI_TC_COSTO", width: "60px", sortable : true, template : " <label> #:  (util.bcs_formato(TipoCambioCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }
			    ,{field: "TipoDocumento", title: "UI_DOCUMENTOS", width: "70px", sortable : true, groupable: true  }			    
			    ,{field: "ParidadCierre", title: "UI_PARIDAD_CIERRE", width: "90px", sortable : true, template : " <label> #:  (util.bcs_formato(ParidadCierre,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "ParidadCosto", title: "UI_PARIDAD_COSTO", width: "90px", sortable : true, template : " <label> #:  (util.bcs_formato(ParidadCosto,IdTipoInstrumento,'Monto',IdMonedaPrincipal,'','')).valor #</label>" }				       				    			    
			    ,{field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", width: "110px", sortable : true, template : " <label> #:  (util.bcs_formato(MontoMonedaSecundaria,IdTipoInstrumento,'Monto',IdMonedaSecundaria,'','')).valor #</label>"}
			    ,{field: "FechaEntregamos", title: "UI_FECHA_ENTREGAMOS", width: "92px", sortable : true, groupable: true }
			    ,{field: "FechaRecibimos", title: "UI_FECHA_RECIBIMOS", width: "92px", sortable : true, groupable: true }
			    ,{field: "Estado", title: "UI_ESTADO_OPERACION", width: "40px", sortable : true, groupable: true  }				       				    
			    ,{field: "IdTipoCanalIngreso", title: "UI_CANAL", width: "40px", sortable : true, groupable: true  }
			    ,{field: "SecuenciaAsignacionRv", title: "UI_SECUENCIA_ASIGNACION_RV", width: "80px", sortable : true, groupable: true  }
				,{field: "OperacionNoFacturable", title: "UI_OPERACION_NO_FACTURABLE", width: "80px", sortable : true, groupable: true  }
				,{field: "EstadoCaja", title: "UI_ESTADO_CAJA", width: "100px", sortable : true, groupable: true, hidden: true  }
		    ]
	 	};
	 	
	 	
		$scope.confGrillaSpot = {
			dataSource	: dsDataSpot,
			columns		: $scope.columnasSpot(),
			sortable	: true,
			pageable	: true, //{ pageSize : 25 },   //pagina localmente
			excel       : { allPages:true },
			filterable 	: true,
			selectable  : "multiple row",
			userSettings : true,
			groupable	: true,
			height		: 400,
			navigatable : true,			
			allowCopy	: true,
			resizable	: true,
		    autoBind	: true,
		    scrollable	: true,	    
		    gridCreated : function(grid) {
		    	grillaSpot = grid;
		    },
		};
		
		//SUB-GRILLA FORMAS DE PAGO
		var dsFormaDePago = function(dataItem){
			var filtrosMovimientoForDePago = [
			  	{field: "IdOperacion", operator: "eq", value: Number(dataItem.IdOperacionSpot)},
			  	{field: "IdTipoProducto", operator: "eq", value: 'MEME'}
		  	];
		  	return new bcs.DataSource({
		  		type : "odata",
		  		definition: {
		  			filter: filtrosMovimientoForDePago,
		  			serverFiltering 	: true,
		  			serverPaging 		: true,
		  			serverSorting 	: true,
		  			//sort 				: { field: "IdMovimientoComprobanteFormaDePago", dir: "asc" }, // Criterios de Ordenamiento.
		  			pageSize 			: 10, // Número de registros por página.
		  			//transport 		: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=542",dataType : "json"}},
		  			transport 		: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3567",dataType : "json"}},
		  			schema : {
		  				model: {
		  					fields : {
		  						/*NombreTipoMovimiento        : { type: "string"},
		  						NombreMedioDePago           : { type: "string"},
		  						NombreModalidadDePago       : { type: "string"},
		  						FechaCondicionLiquidacion	: { type: "string"},
		  						IdMoneda					: { type: "string"},
		  						Monto						: { type: "number"},
		  						IdMedioDePago               : { type: "string"},   //columna oculta
		  						IdModalidadPago             : { type: "string"},   //columna oculta*/
		  						
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
		
		//SUB-GRILLA OTROS
		var dsOtros = function(dataItem){
			var filtrosMovimientoOtros = [
			  	{field: "IdOperacion", operator: "eq", value: Number(dataItem.IdOperacionSpot)}
		  	];
		  	return new bcs.DataSource({
		  		type : "odata",
		  		definition: {
		  			filter: filtrosMovimientoOtros,
		  			serverFiltering : true,
		  			serverPaging 	: true,
		  			serverSorting 	: true,
		  			pageSize 		: 10, // Número de registros por página.
		  			transport 		: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700147",dataType : "json"}},
		  			schema : {
		  				model: {
		  					fields : {
		  						Mesa 						: { type: "string"},
		  						TipoDolar					: { type: "string"},
		  						TipoComprobante				: { type: "string"},
		  						NombreAgente				: { type: "string"},
		  						NombreContacto				: { type: "string"},		  						
		  						TipoDocumento				: { type: "string"},		  						
		  						Ben							: { type: "string"},
		  						EquivalenteConGasto			: { type: "string"},	
		  						Our							: { type: "string"},
		  						TipoCambioCierreConGasto	: { type: "string"},		  						
		  						OperacionFicticia			: { type: "string"},
		  						NombrePlanillaBancoCentral	: { type: "string"},
		  						Observacion					: { type: "string"},
		  						
		  						// Columnas ocultas de informacion de contacto
		  						IdContacto					: { type: "number"},
		  						NombreContacto				: { type: "string"},		  						
		  						CorreoContacto				: { type: "string"},
		  						TelefonoContacto			: { type: "string"},
		  						Calle						: { type: "string"},
		  						NumeroCalle					: { type: "string"},
		  						NombreCiudad				: { type: "string"},
		  						NombrePais					: { type: "string"},
		  					}
		  				}
		  			}
		  		}
		  	});
		};
		
		
		$scope.columnasFormasDePago = function(){
			return  [
				 /*{field : "NombreTipoMovimiento", title:'UI_TIPO_MOVIMIENTO', width : "40px", align: "left", sortable : false }
				,{field : "NombreMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", align: "left", sortable : false }
				,{field : "NombreModalidadDePago", title:'UI_MODALIDAD', width : "60px", sortable : false, hidden: true}									
				,{field : "IdMoneda", title: "UI_MONEDA", width: "30px", align: "left", sortable : false}
				,{field : "Monto", title:'UI_MONTO_LIQUIDACION', width : "40px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }				
				,{field : "FechaCondicionLiquidacion", title:'UI_FECHA_LIQUIDACION', width : "40px", align: "right" }				
			 	
				//Columnas Ocultas  			
				,{field : "IdMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", align: "right", sortable : false, hidden: true}
				,{field : "IdModalidadPago", title:'UI_MODALIDAD', width : "80px", align: "right", sortable : false, hidden: true}*/
				
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
		
		$scope.columnasOtros = function(){
			return  [
				 {field : "Mesa", title:'UI_MESA', width : "120px", align: "left", sortable : false }
				,{field : "TipoDolar", title:'UI_TIPO_DOLAR', width : "60px", align: "left", sortable : false }
				,{field : "TipoComprobante", title: "UI_TIPO_COMPROBANTE", width: "180px", align: "left", sortable : false}
				,{field : "NombreAgente", title:'UI_AGENTE', width : "120px", align: "left" }
				,{field : "NombreContacto", title:'UI_CONTACTO', width : "120px", align: "left", sortable : false }
				,{field : "TipoDocumento", title:'UI_DOCUMENTOS', width : "70px", align: "left", sortable : false }					
				,{field : "Ben", title:'UI_BEN', width : "40px", align: "center" }
				,{field : "EquivalenteConGasto", title:'UI_EQUIVALENTE_GASTO', width : "120px", align: "right" }				
				,{field : "Our", title:'UI_OUR', width : "40px", align: "center" }
				,{field : "TipoCambioCierreConGasto", title:'UI_TC_CIERRE_GASTO', width : "120px", align: "right", sortable : false }
				,{field : "OperacionFicticia", title:'UI_OPERACION_FICTICIA', width : "90px", align: "center", sortable : false }
				,{field : "NombrePlanillaBancoCentral", title:'UI_CODIGO_PLANILLA_BANCO_CENTRAL', width : "300px", align: "left", sortable : false }
				,{field : "Observacion", title:'UI_OBSERVACIONES_OPERACION', width : "300px", align: "left", sortable : false }
				
				// Columnas ocultas
				,{field : "IdContacto", title:'UI_CONTACTO', width : "80px", align: "right", sortable : false, hidden : true }	
				,{field : "NombreContacto", title:'UI_NOMBRE', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "CorreoContacto", title:'UI_CORREO', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "TelefonoContacto", title:'UI_TELEFONO', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "Calle", title:'UI_CALLE', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "NumeroCalle", title:'UI_NUMERO', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "NombreCiudad", title:'UI_CIUDAD', width : "300px", align: "left", sortable : false, hidden : true }
				,{field : "NombrePais", title:'UI_PAIS', width : "300px", align: "left", sortable : false, hidden : true }
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
		
		//SUB-GRILLA OTROS
		$scope.confGrillaOtros = function(DataItem){
			return {
				dataSource		: dsOtros(DataItem),
				columns			: $scope.columnasOtros(),		
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