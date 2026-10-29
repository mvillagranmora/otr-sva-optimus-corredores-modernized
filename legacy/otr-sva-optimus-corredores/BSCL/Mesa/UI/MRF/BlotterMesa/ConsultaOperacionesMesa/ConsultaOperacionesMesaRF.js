angular.module('ModuloMesa').controller('ConsultarOperacionesMesaRFCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate, bcsServiciosMesa,$http,bcsServiciosGrillasMesaRF,bcsAlerta,OPCIONES_BCSALERTA, bcsConsultasMesa)
{	
	var conOperacion = this;
	$scope.data = {};
	$scope.Estado = '';
	$scope.dataTemplate	= {};
	$scope.dataTemplate2= {};
	$scope.TipoInformeGenerado= '';
	$scope.activaWebSocket = true;
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	$scope.opeReasignada;

	// MESAS:
	var cMesa = this;
	cMesa.mesas = [];
	cMesa.ds = {};

	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------
	
	confGridOperaciones = bcsServiciosGrillasMesaRF.getConfiguracionOperacionesMesaRF();
	$scope.data.confBasicaOperacionesMesaRF = confGridOperaciones.options;	
	$scope.data.buscarOperacionesMesaRF = confGridOperaciones.buscar;
	$scope.show = true;
	
	var dataSourceConsultarEstadoMesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa();
	var fechaOperacion = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.NombreArchivo = "ConsultaOperacionesMesaRF_"+$scope.SesionFechaSistema;
	$scope.data.HabilitaBotones = true;
	$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;

	//Se cargan portafolios mesa para intermesa
	var dataSourcePortafolioIntermesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa()
	var PortafoliosInterMesa;
	dataSourcePortafolioIntermesa.query({filter:{field:"IdTipoProducto", operator: "eq", value: "MERF"}}).then(function(){
		PortafoliosInterMesa = dataSourcePortafolioIntermesa.data();
	});
	// --------------------------------------------------
	// CONFIGURACION DE COMBOS
	// --------------------------------------------------
	$scope.data.oConfiguracionMesas = {
			tipo : "combobox-mesas",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
	};
	
	$scope.data.opcionesFiltro = {opPorDia: true, opPorEstadoFecha: false};
	$scope.data.opcionesPacto = {confPacto: false};
	$scope.$watch("data.opcionesFiltro.opPorDia",function(nval,oval){
		if(nval == true)
			$scope.data.opcionesFiltro.opPorEstadoFecha = false;
		else if(nval == false)
			$scope.data.opcionesFiltro.opPorEstadoFecha = true;
	});
	$scope.$watch("data.opcionesFiltro.opPorEstadoFecha",function(nval,oval){
		if(nval == true)
			$scope.data.opcionesFiltro.opPorDia = false;
		else if(nval == false)
			$scope.data.opcionesFiltro.opPorDia = true;
	});
	
	$scope.$watch('data.oConfiguracionMesas.seleccionado', function(newval,oldval ){
		if ($scope.data.oConfiguracionMesas.seleccionado && $scope.data.gridApiOperacionesMesaRF != undefined) {
			$scope.blotterOpMesa.IdMesa = $scope.data.oConfiguracionMesas.seleccionado.IdMesa;
			$scope.blotterOpMesa.OPEBUSCA();
		}
		
	});
	$scope.$watch('blotterOpMesa.IdMesa', function(newval,oldval ){
		if ($scope.blotterOpMesa.IdMesa) {
			$scope.data.oConfiguracionMesas.idActual = $scope.blotterOpMesa.IdMesa;
		}
		
	});
	
	$scope.data.Modo = 'OPE';	
	$scope.data.kngFechaOperacion = fechaOperacion;
	$scope.data.kngFechaOperacionFin = fechaOperacion;
	
	$scope.data.oConfiguracionFechaDesde = {
			max: $scope.data.kngFechaOperacionFin
	};
	$scope.data.oConfiguracionFechaHasta = {
			min: $scope.data.kngFechaOperacion,
			max: util.dateInicioDia($scope.SesionFechaSistema)
	};
	
	$scope.$watch('data.kngFechaOperacion',function(newval,oldval) {    
		if(newval && newval !== oldval){
			if( newval > $scope.data.kngFechaOperacionFin ){
				$scope.data.kngFechaOperacionFin = $scope.data.kngFechaOperacion;
			}
			$scope.data.oConfiguracionFechaHasta = {
				min: $scope.data.kngFechaOperacion,
				max: util.dateInicioDia($scope.SesionFechaSistema)
			};
		}
    });

	$scope.$watch('data.kngFechaOperacionFin',function(newval,oldval) {    
		if(newval && newval !== oldval){
			if( newval < $scope.data.kngFechaOperacion ){
				$scope.data.oConfiguracionFechaDesde = {
						max: $scope.data.kngFechaOperacionFin
				};
				$scope.data.kngFechaOperacion = $scope.data.kngFechaOperacionFin;
			}
		}
    });
	
	$scope.blotterOpMesa.OPEBUSCA = function(){
		$scope.data.buscarOperacionesMesaRF($scope.data.gridApiOperacionesMesaRF,$scope.data);
	};

	$scope.data.btnBuscar = function(){
		$scope.activaWebSocket = true;
		$scope.data.buscarOperacionesMesaRF($scope.data.gridApiOperacionesMesaRF,$scope.data);
	}
	
	$scope.data.cargaGrillas = function(data,opcion){
		if (opcion !== 'ANU' && $scope.data.idOperacionEnEdicion){
			var idOperacionALiberar = $scope.data.idOperacionEnEdicion;
			$scope.data.idOperacionEnEdicion = null;
			var datos = {"p_IdEmpresa": $scope.SesionIdEmpresa
						,"p_IdUsuario": $scope.SesionIdUsuario
						,"p_IdModulo" : 'MRF'
						,"p_Operaciones":[{
							 "Accion" : 'L' //Liberar
							,"IdOperacion" : idOperacionALiberar
						}]
		};
			bcsServiciosMesa.MarcarOperacion(datos,function(respuesta){
				console.log(respuesta);
				if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					//bcsComunes.msg("warning","info",$translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
					bcsComunes.ventanaMsg({
					                     conflictos:util.parseServerResponse(respuesta)
					                   },$scope.winID);
					
					return;
				}
				
	
			},function(respuesta){		
					//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_LIBERADA_ERROR'));
					//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaMsg({
					                     conflictos:util.parseServerResponse(respuesta)
					                   },$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID, false);
			});
		}
		
		$scope.data.buscarOperacionesMesaRF($scope.data.gridApiOperacionesMesaRF,$scope.data);
		$scope.blotterOpMesa.ANTBUSCA();
		$scope.blotterOpMesa.VENBUSCA();

		
		
	};
	$scope.data.detalleOperacion = function(datos){
		if (!datos) return;
		bcsComunes.ventanaAbrir("DetalleOperacionMesaRF", {
					IdOperacion: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
					IdTipoProducto : $scope.data.seleccionOperacionesMesaRF.IdTipoProducto,
				},false);
		$scope.data.seleccionOperacionesMesaRF = undefined;
		/*$scope.Estado = "CONSULTAR";
		if ($scope.data.seleccionOperacionesMesaRF.IdEstado == 'PEN') 
			$scope.Estado = "MODIFICAR";
		bcsComunes.ventanaAbrir("IngresoOperacionMesaRF", 
				{IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa, 
			     IdClienteCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdCliente,
				 IdPortafolioCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio,
				 FechaOperacion: $scope.data.kngFechaOperacion,
				 IdOperacion: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
				 Accion: $scope.Estado
				});*/
	};
	
	// funcion WebSocket
	$scope.msgWebsocket = function(message) { 
		if(!$scope.activaWebSocket || !$scope.data.opcionesFiltro.opPorDia) return;
    	if(message.Evento==='ACTMRF'){

			var nuevoRegistroSocket = JSON.parse(message.DATA);
			var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");			
			nuevoRegistroSocket.FechaOperacion=fechaSistema;
			$scope.data.gridApiOperacionesMesaRF.dataSource.add(nuevoRegistroSocket);
    		return;
    	}
		if(message.Evento==='ANUMRF'){
			var nuevoRegistroSocket = JSON.parse(message.DATA);
			$scope.data.gridApiOperacionesMesaRF.dataSource.data().forEach(a=>{
				if(a.IdOperacion == nuevoRegistroSocket.IdOperacion){
					a.IdEstado='ANU';
					a.NombreEstado ='ANULADO'
					return;
				}
			});
			return;
		}
		if(message.Evento==='MODMRF'){
			var nuevoRegistroSocket = JSON.parse(message.DATA);
			var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");			
			nuevoRegistroSocket.FechaOperacion=fechaSistema;
			$scope.data.gridApiOperacionesMesaRF.dataSource.data().forEach(a=>{
				if(a.IdOperacion == nuevoRegistroSocket.IdOperacion){
					$scope.data.gridApiOperacionesMesaRF.dataSource.remove(a);
					$scope.data.gridApiOperacionesMesaRF.dataSource.add(nuevoRegistroSocket);
					
					return;
				}
			});
			return;
		}
    };
	
	
	var success = function(respuesta)
	{
		var operaciones = [];
		var id;
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			$.each(respuesta.P_OUT_OPERACIONES,function(key,value) {
				operaciones.push({id: value.IdOperacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_OPERACION_ANULADA_EXITO')});
			});
	
			id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
				tituloVentana : $translate.instant("UI_MESA_RENTA_FIJA"), 
				nombreEntidad : $translate.instant("UI_SECUENCIA"), 
				operaciones : operaciones   
			},false,false,$scope.winID);
			
		}
		else
		{			
			//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_ANULADA_ERROR'));
			//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			id = bcsComunes.ventanaMsg({
				                     conflictos:util.parseServerResponse(respuesta)
				                   },$scope.winID);
		}
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.cargaGrillas(null,'ANU');
		});
		
		
	};
	
	var successPacto = function(respuesta)
	{
		var operaciones = [];
		var id;
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{		
			
			var mensaje = "Pacto operación actualizada ";
			bcsComunes.ventanaMsg({ mensaje:$translate.instant(mensaje), conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
			
		}
		else
		{			
			//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_ANULADA_ERROR'));
			//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			id = bcsComunes.ventanaMsg({
				                     conflictos:util.parseServerResponse(respuesta)
				                   },$scope.winID);
		}
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.cargaGrillas(null,'ANU');
		});
		
		
	};
	
	var error = function(respuesta)
	{		
		var id = bcsComunes.ventanaMsg({
		                     conflictos:util.parseServerResponse(respuesta)
		                   },$scope.winID);
		bcsComunes.ventanaPromesa(id).then(function(result){
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
		
	};
	// --------------------------------------------------
	// INGRESO DE UNA NUEVA OPERACION
	// --------------------------------------------------
	$scope.crearIngresoOperacion = function(accion) {
		var fechaSist = $scope.SesionFechaSistema;
		var fechaOpe = accion === "MODIFICAR"?util.dateSPdate($scope.data.seleccionOperacionesMesaRF.FechaOperacion):$scope.SesionFechaSistema;
		var ventana = "";
		
		if ($scope.SesionLocale === 'CL') ventana = "IngresoOperacionMesaRF";
		if ($scope.SesionLocale === 'PE') ventana = "IngresoDepositosDesembolsosMesaRF";

		if (accion === "MODIFICAR" && ($scope.data.seleccionOperacionesMesaRF.IdEstado !== 'PEN'
			|| fechaSist !== fechaOpe)){
			bcsComunes.msg("warning","info",$translate.instant('UI_ERROR_MODIFICACION_OPERACION'));
			return;
		}
		if (accion === "MODIFICAR" && $scope.data.seleccionOperacionesMesaRF.length > 1){
			bcsComunes.msg("warning","info",$translate.instant('UI_ERROR_CANTIDAD_OPERACIONES'));
			return;
		}
		dataSourceConsultarEstadoMesa.query({filter : {field:"IdMesa", operator: "eq", value: Number($scope.data.oConfiguracionMesas.idActual)}}).then(function(){
			var data = dataSourceConsultarEstadoMesa.data();
			if (data.length > 0){
				if (data[0].TipoEstadoModulo === 'C'){
					bcsComunes.msg("warning","info",$translate.instant('UI_MODULO_CERRADO'));
					return;
				}
				if (data[0].IdEstadoMesa === 'C'){
					bcsComunes.msg("warning","info",$translate.instant('MESA_RF_CERRADA'));
					return;
				}
				if (data[0].FechaActualSistema !== $scope.SesionFechaSistema){
					//bcsComunes.msg("warning","info",$translate.instant('FECHA_PROCESO_INVALIDA'));
					bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse({P_OUT_ERRORESDETALLE : [{Codigo:'FECHA_PROCESO_INVALIDA', Descripcion:$translate.instant('FECHA_PROCESO_INVALIDA')}]})
							},$scope.winID);
					return;
				}
				$scope.data.HabilitaBotones = false;
				if (accion === "MODIFICAR"){
					var datos = {"p_IdEmpresa": $scope.SesionIdEmpresa
				    			,"p_IdUsuario": $scope.SesionIdUsuario
				    			,"p_IdModulo" : 'MRF'
				    			,"p_Operaciones":[{
				    				 "Accion" : 'B' //Bloquear
									,"IdOperacion" : $scope.data.seleccionOperacionesMesaRF.IdOperacion
				    			}]			
						
					};
					bcsServiciosMesa.MarcarOperacion(datos,function(respuesta){
						console.log("RESPUESTA",respuesta);
						if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
							//bcsComunes.msg("warning","info",$translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
							bcsComunes.ventanaMsg({
				                     conflictos:util.parseServerResponse(respuesta)
				                   },$scope.winID);
				         	$scope.data.HabilitaBotones = true;
							return;
						}
						else {
							$scope.data.idOperacionEnEdicion = $scope.data.seleccionOperacionesMesaRF.IdOperacion;
							var id1=bcsComunes.ventanaAbrir(ventana, 
															{IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa, 
															 IdClienteCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdCliente,
															 IdPortafolioCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio,
															 PortafoliosMesas: PortafoliosInterMesa,
															 //FechaOperacion: fechaSist,
															 IdOperacion: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
															 CodigoIFRS: $scope.data.oConfiguracionMesas.seleccionado.CodigoIFRS,
															 Accion: accion,
															 ModificacionFormasDePago:false			 
															},false,false);//,$scope.winID);
							var promesa=bcsComunes.ventanaPromesa(id1);
							//bcsComunes.ventanaAbrir("IngresoOperacion", {} )
							$.when(promesa).then(function(data){
								$scope.data.HabilitaBotones = true;
								$scope.data.cargaGrillas();
							});
						}

					},function(respuesta){		
							//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_BLOQUEADA_ERROR'));
							//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							bcsComunes.ventanaMsg({
				                     conflictos:util.parseServerResponse(respuesta)
				                   },$scope.winID);
							$scope.data.HabilitaBotones = true;
							bcsComunes.ventanaOverlay($scope.winID, false);
					});
				}
				else {
					var id1=bcsComunes.ventanaAbrir(ventana, 
						{IdMesa: $scope.data.oConfiguracionMesas.seleccionado.IdMesa, 
						 IdClienteCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdCliente,
						 IdPortafolioCorredor: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio,
						 PortafoliosMesas: PortafoliosInterMesa,
						 //FechaOperacion: fechaSist,
						 IdOperacion: 0,
						 CodigoIFRS: $scope.data.oConfiguracionMesas.seleccionado.CodigoIFRS,
						 Accion: accion
						},false,false);//,$scope.winID)
					var promesa=bcsComunes.ventanaPromesa(id1);
					//bcsComunes.ventanaAbrir("IngresoOperacion", {} )
	
					$.when(promesa).then(function(data){
						console.log(conOperacion);
						$scope.data.HabilitaBotones = true;
						$scope.data.cargaGrillas(data);
					});
				}
			}
			
		});
	};

	
	// --------------------------------------------------
	// ANULAR OPERACION
	// --------------------------------------------------

	var verificarBloqueoOperacion = function(idOperacion, callback){
		var dsVerificarBloqueo = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url: bcsConsultasMesa('ConsultarOperacionesMesaRF'),
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdOperacion: { type: "number" },
							FolioOperacion: { type: "number" },
							BloqueoPorEdicion: { type: "string" },
							CodigoUsuarioBloqueoEdicion: { type: "string" }
						}
					}
				}
			}
		});
		dsVerificarBloqueo.query({filter: {field: "IdOperacion", operator: "eq", value: idOperacion}}).then(function(){
			var data = dsVerificarBloqueo.data();
			callback(data.length > 0 ? data[0] : null);
		});
	};
	
	$scope.anularOperacionMesaRF = function() {
		var datos = {};
		if ($scope.data.seleccionOperacionesMesaRF === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));		
		}
		else
		{	
			$scope.Estado = $scope.data.seleccionOperacionesMesaRF.IdEstado;
			if ( $scope.SesionFechaSistema !== util.dateSPdate($scope.data.seleccionOperacionesMesaRF.FechaOperacion)) {
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
				return;
			}
			/*if ( $scope.Estado !== 'PEN' ) {
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
			}
			else
			{*/
				dataSourceConsultarEstadoMesa.query({filter : {field:"IdMesa", operator: "eq", value: Number($scope.data.oConfiguracionMesas.idActual)}}).then(function(){
					var data = dataSourceConsultarEstadoMesa.data();
					if (data.length > 0){
						if (data[0].TipoEstadoModulo === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('UI_MODULO_CERRADO'));
							return;
						}
						if (data[0].IdEstadoMesa === 'C'){
							bcsComunes.msg("warning","info",$translate.instant('MESA_RF_CERRADA'));
							return;
						}
						if (data[0].FechaActualSistema !== $scope.SesionFechaSistema){
							//bcsComunes.msg("warning","info",$translate.instant('FECHA_PROCESO_INVALIDA'));
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse({P_OUT_ERRORESDETALLE : [{Codigo:'FECHA_PROCESO_INVALIDA', Descripcion:$translate.instant('FECHA_PROCESO_INVALIDA')}]})
							},$scope.winID);
							return;
						}
						var msg = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_MENSAJE_ANULACION_OPERACION')+' . '+$translate.instant('UI_CONTINUAR'));
						msg.result.then(function(result){
							bcsComunes.ventanaOverlay($scope.winID, true);
							if (result===OPCIONES_BCSALERTA.ACEPTAR) {
								verificarBloqueoOperacion($scope.data.seleccionOperacionesMesaRF.IdOperacion, function(operacionBloqueada){
									if (operacionBloqueada && operacionBloqueada.BloqueoPorEdicion === 'S'){
										bcsComunes.ventanaOverlay($scope.winID, false);
										bcsComunes.ventanaMsg({
											conflictos: util.parseServerResponse({P_OUT_ERRORESDETALLE: [{
												Codigo: 'UI_OPERACION_BLOQUEADA_POR_USUARIO',
												Descripcion: $translate.instant('UI_OPERACION_BLOQUEADA_POR_USUARIO')
													.replace("::IdOperacion::", operacionBloqueada.IdOperacion)
													.replace("::CodigoUsuarioBloqueoEdicion::", operacionBloqueada.CodigoUsuarioBloqueoEdicion)
											}]})
										}, $scope.winID);
										return;
									}
									datos.p_IdEmpresa = $scope.SesionIdEmpresa;
									datos.p_IdUsuario = $scope.SesionIdUsuario;
							    	datos.p_IdModulo = 'MRF';
							    	datos.p_UsaReglasNegocio = 'S';
							    	datos.p_Operaciones = [];
							    //	$.each($scope.data.seleccionOperacionesMesaRF,function(key,value) {
							    		var ope = {};
										ope.IdOperacion = $scope.data.seleccionOperacionesMesaRF.IdOperacion;
										ope.IdEstadoSiguiente = 'ANU';
										datos.p_Operaciones.push(ope);
									//});


									bcsServiciosMesa.AnularOperacionesMesaRF(datos,success,error);
								});
							}
							else {
								bcsComunes.ventanaOverlay($scope.winID, false);0
							}
		
						});
					}
				});
			//};
		};
		//bcsComunes.ventanaAbrir("IngresoOperacion", {} )
	};
	
	
	// --------------------------------------------------
	// TEMPLATES REPORTES
	// --------------------------------------------------
	var ConsultaTemplate = new bcs.newDataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						IdTemplateMesa	: {type: "string"},
						IdReporte		: {type: "string"},
						NombreReporte	: {type: "string"},
						TipoReporte		: {type: "string"},
						ServicioReporte	: {type: "string"},
						IdEmpresa		: {type: "number"},
						IdEstado		: {type: "string"}
					}
				}
			}
		},
	});
	
	
	$scope.generarReporteMesaRF = function(tipo) {
		 // TODO agregar que la Operacion este Cerrada para poder generar el Mandato o Adendum
		$scope.TipoInformeGenerado=tipo;
		$scope.dataTemplate	= {};
		$scope.dataTemplate2= {};
		
		var idEstadoOperacionMandato = ['VIG','ANT','LIQ','RGN'];
				
		if ($scope.data.seleccionOperacionesMesaRF === undefined || $scope.data.seleccionOperacionesMesaRF.length === 0){	
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));
		}
		else if($scope.data.seleccionOperacionesMesaRF.length > 1){
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UNA_OPERACION'));
		}
		//else if($scope.data.seleccionOperacionesMesaRF.IdEstado === "LIQ" || $scope.data.seleccionOperacionesMesaRF.IdEstado === "ANT"){
		else if(!idEstadoOperacionMandato.includes($scope.data.seleccionOperacionesMesaRF.IdEstado)){
			bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_DEBE_ESTAR_CERRADA'));
		}
		else{
			var idTipOpe = $scope.data.seleccionOperacionesMesaRF.IdTipoOperacion;
			if(tipo === 'MANDATO' && (idTipOpe === 'COMTER' || idTipOpe === 'VENTER')){
				$scope.dataTemplate.IdTemplateMesa = 'MRFCOMPRAVENTATERMINO';
				// $scope.dataTemplate.IdTemplateMesa2= 'PROMESACOMPRAVENTA';
			}
			else if(tipo === 'MANDATO' && (idTipOpe === 'COMTER' || idTipOpe === 'VENTER' || idTipOpe === 'COMPAC' || idTipOpe === 'VENPAC')){
				$scope.dataTemplate.IdTemplateMesa2= 'PROMESACOMPRAVENTA';	
				if (idTipOpe === 'COMPAC' || idTipOpe === 'VENPAC'){
					$scope.dataTemplate.IdTemplateMesa = 'COMPRAVENTAPACTO';
				}
				
			}

			else if(tipo === 'ADENDUM' && (idTipOpe === 'COMPAC' || idTipOpe === 'VENPAC')){
				$scope.dataTemplate.IdTemplateMesa = 'ADENDUMANEXO1';
				$scope.dataTemplate.IdTemplateMesa2 = 'ADENDUMANEXO2';
			}
			// TODO solo para prueba de Condiciones Generales
			else if(tipo === 'CONDGRALES'){
				$scope.dataTemplate.IdTemplateMesa = 'CONDICIONESGENERALES';
				$scope.dataTemplate.IdTemplateMesa2 = 'CONDICIONESGENERALESANEXO1';
			}
			// fin prueba de Condiciones Generales
			else{
				bcsComunes.msg("warning","info",$translate.instant('UI_OPERACION_INVALIDA'));
				return;
			}
			
			var filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: $scope.dataTemplate.IdTemplateMesa },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= ConsultaTemplate.view()[0].TipoReporte;
					$scope.dataTemplate.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
					$scope.dataTemplate.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte +'_'+ $scope.data.seleccionOperacionesMesaRF.FolioOperacion+'_'+$scope.data.seleccionOperacionesMesaRF.RazonSocial;
					
					var parametros = {	"p_IdEmpresa"	: $scope.$root.SesionIdEmpresa,
										"p_IdModulo"	: 'MRF',
										"p_IdOperacion"	: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
										"p_IdPais"		: $scope.$root.SesionIdPaisSistema,
										"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
									 };
					
					
					
					 if ($scope.dataTemplate.IdTemplateMesa === 'MRFCOMPRAVENTATERMINO') { 
						// Mandato => Tiene sub-reporte => setear los parametros del sub-reporte
						var subReports = new Array();
						subReports.push({ "reportName" 	: "PapeletaCertificado"
				              			 ,"data" 		: { "serviceId": '461', "serviceData": parametros }
						});
						subReports.push({ "reportName" 	: "PapeletaMOT"
	              			 ,"data" 		: { "serviceId": '461', "serviceData": parametros }
						});						
						
						$scope.dataTemplate.subReports = subReports;
					}
					
					
					$scope.dataTemplate.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros };
					$scope.getReport($scope.dataTemplate);
					
					//Promesa de Venta/Compra
					if($scope.dataTemplate.IdTemplateMesa2){
						filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: $scope.dataTemplate.IdTemplateMesa2 },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
						ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
							if(ConsultaTemplate.view().length > 0) {
								$scope.dataTemplate2.TipoReporte	= ConsultaTemplate.view()[0].TipoReporte;
								$scope.dataTemplate2.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
								$scope.dataTemplate2.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte +'_'+ $scope.data.seleccionOperacionesMesaRF.FolioOperacion+'_'+$scope.data.seleccionOperacionesMesaRF.RazonSocial;
								
								var parametros2 = {	"p_IdEmpresa"	: $scope.$root.SesionIdEmpresa,
													"p_IdModulo"	: 'MRF',
													"p_IdOperacion"	: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
													"p_IdPais"		: $scope.$root.SesionIdPaisSistema,
													"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
												 };
								$scope.dataTemplate2.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros2 };
								$scope.getReport($scope.dataTemplate2);
								
								// Solo para prueba de Condiciones Generales
								if(tipo === 'CONDGRALES'){
									filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: 'CONDICIONESGENERALESANEXO2' },
								                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
								                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
									ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
										if(ConsultaTemplate.view().length > 0) {
											$scope.dataTemplate2= {};
											parametros2 = {};
											
											$scope.dataTemplate2.TipoReporte	= ConsultaTemplate.view()[0].TipoReporte;
											$scope.dataTemplate2.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
											$scope.dataTemplate2.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte +'_'+ $scope.data.seleccionOperacionesMesaRF.FolioOperacion+'_'+$scope.data.seleccionOperacionesMesaRF.RazonSocial;
											
											var parametros2 = {	"p_IdEmpresa"	: $scope.$root.SesionIdEmpresa,
																"p_IdModulo"	: 'MRF',
																"p_IdOperacion"	: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
																"p_IdPais"		: $scope.$root.SesionIdPaisSistema,
																"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
															 };
											$scope.dataTemplate2.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros2 };
											$scope.getReport($scope.dataTemplate2);
											
											// Solo para Condiciones Generales
											filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: 'CONDICIONESGENERALESANEXO3' },
										                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
										                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
											ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
												if(ConsultaTemplate.view().length > 0) {
													$scope.dataTemplate2= {};
													parametros2 = {};
													
													$scope.dataTemplate2.TipoReporte	= ConsultaTemplate.view()[0].TipoReporte;
													$scope.dataTemplate2.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
													$scope.dataTemplate2.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte +'_'+ $scope.data.seleccionOperacionesMesaRF.FolioOperacion+'_'+$scope.data.seleccionOperacionesMesaRF.RazonSocial;
													
													var parametros2 = {	"p_IdEmpresa"	: $scope.$root.SesionIdEmpresa,
																		"p_IdModulo"	: 'MRF',
																		"p_IdOperacion"	: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
																		"p_IdPais"		: $scope.$root.SesionIdPaisSistema,
																		"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
																	 };
													$scope.dataTemplate2.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros2 };
													$scope.getReport($scope.dataTemplate2);
													
													// Solo para Condiciones Generales
													filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: 'CONDICIONESGENERALESANEXOA' },
												                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
												                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
													ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
														if(ConsultaTemplate.view().length > 0) {
															$scope.dataTemplate2= {};
															parametros2 = {};
															
															$scope.dataTemplate2.TipoReporte	= ConsultaTemplate.view()[0].TipoReporte;
															$scope.dataTemplate2.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
															$scope.dataTemplate2.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte +'_'+ $scope.data.seleccionOperacionesMesaRF.FolioOperacion+'_'+$scope.data.seleccionOperacionesMesaRF.RazonSocial;
															
															var parametros2 = {	"p_IdEmpresa"	: $scope.$root.SesionIdEmpresa,
																				"p_IdModulo"	: 'MRF',
																				"p_IdOperacion"	: $scope.data.seleccionOperacionesMesaRF.IdOperacion,
																				"p_IdPais"		: $scope.$root.SesionIdPaisSistema,
																				"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
																			 };
															$scope.dataTemplate2.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros2 };
															$scope.getReport($scope.dataTemplate2);
														}
													});
												}
											});
										}
									});
								}
								// fin prueba de Condiciones Generales
							}
						});
					}
				}
			});
		}
	};
	$scope.informeCarteraPropia = function(tipoVal){
		var subReports = [];
		var template = {};
		var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: 'INFORME_CARTERA_PROPIA_MRF' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
			var data = ConsultaTemplate.data();
			for(i=0;i<data.length;i++) {
				var dataTem = data[i];
				if (data[i].IdTemplateMesa === 'InfCartPropMesaRF_Cabecera'){
					template = {
								TipoReporte		: data[i].TipoReporte//'pdf' 
								,IdReporte		: data[i].IdReporte//'EBE13D58A5C74C1FE1000000A1EE22E7'
								,NombreReporte	: $translate.instant(data[i].NombreReporte)//("INFORME_CARTERA_PROPIA_MRF")
								,data			: { "serviceId" 	: data[i].ServicioReporte,//'1761',
													"serviceData" 	: {
														 "p_IdEmpresa"			: $scope.SesionIdEmpresa
														,"p_IdUsuario"			: $scope.SesionIdUsuario
														,"p_IdModulo"			: 'MRF'
														,"p_Fecha"				: $scope.data.kngFechaOperacion
														,"p_IdCliente"			: $scope.data.oConfiguracionMesas.seleccionado.IdCliente
														,"p_IdPortafolio"		: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio
														,"p_TipoValorizacion"	: tipoVal
														,"$schema"		: [{ "field" : "LogoCorredora", "type" : "blob" }]
													  }
													}
													//,subReports		: subReports
													,idReporte		: 'CAR_PRO'
											};
				}
				else {
					subReports.push({ "reportName" 	: data[i].IdTemplateMesaSubreporte//'InformeCarteraPropiaGarantiaMesaRF_Detalle'
						              ,"parameters" 	: {} 
						              ,"data" 			: {
											                  "serviceId" : data[i].ServicioReporte,//'1759',
											                  "serviceData" : {
											                	 "p_IdEmpresa"			: $scope.SesionIdEmpresa
															    ,"p_IdUsuario"			: $scope.SesionIdUsuario
															    ,"p_IdModulo"			: 'MRF'
																,"p_Fecha"				: $scope.data.kngFechaOperacion
																,"p_TipoValorizacion"	: tipoVal
																,"p_IdCliente"			: $scope.data.oConfiguracionMesas.seleccionado.IdCliente
																,"p_IdPortafolio"		: $scope.data.oConfiguracionMesas.seleccionado.IdPortafolio
											                  }
											              }
									});
				}				
			}
			template.subReports = subReports;
			console.log(template);
			$scope.getReport(template);
		});
	};
	
	$scope.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports || []
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')
				nombreArchivo = nombreArchivo;//!dataTemplate.idReporte ? nombreArchivo+'_'+$scope.data.seleccionOperacionesMesaRF.IdOperacion:nombreArchivo;
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			
				
				if ($scope.TipoInformeGenerado=== 'ADENDUM' || $scope.TipoInformeGenerado === 'MANDATO'){
					
				
				    	var datos = {"p_IdEmpresa": $scope.SesionIdEmpresa
				    			,"p_IdUsuario": $scope.SesionIdUsuario
				    			,"p_IdModulo" : 'MRF'
				    			,"p_UsaReglasNegocio" : 'S'
				    			,"p_Operacion":[{
									"IdOperacion" : $scope.data.seleccionOperacionesMesaRF.IdOperacion
				    			}]	
				    			,"p_TipoInformeGenerado" : $scope.TipoInformeGenerado};
						bcsServiciosMesa.MarcarOperacionMandato(datos,function(respuesta){
							$scope.opeReasignada = $scope.data.seleccionOperacionesMesaRF;
							console.log($scope.opeReasignada);

							if ($scope.opeReasignada.IdEstado !== 'RGN') {
								console.log(respuesta);
								if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
									bcsComunes.ventanaMsg({
											conflictos:util.parseServerResponse(respuesta)
										},$scope.winID);
									return;
								}
							}

							
		
						},function(respuesta){		
								bcsComunes.ventanaMsg({
					                     conflictos:util.parseServerResponse(respuesta)
					                   },$scope.winID);
									bcsComunes.ventanaOverlay($scope.winID, false);
						});
				}	
				
				
				
			})
			.error(function (response) {
				//bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaMsg({
				                     conflictos:util.parseServerResponse(response)
				                   },$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
	};
	
	// --------------------------------------------------
	// CONFIRMAR/DESCONFIRMAR PACTOS
	// --------------------------------------------------
	
	$scope.confirmarPacto = function(){
		var datos = {};
		var actual = $scope.data.seleccionOperacionesMesaRF.ConfirmaAdemdum;
		if(actual == null) actual = 'N';
		//actual.ConfirmaAdemdum = 'S';
		var errores = 0;
		var mensaje = "";
		var errorMsg = "";
		var errorArr = [];
		for(var i=1;i<$scope.data.seleccionOperacionesMesaRF.length;i++){
			if($scope.data.seleccionOperacionesMesaRF[i].ConfirmaAdemdum == null) $scope.data.seleccionOperacionesMesaRF[i].ConfirmaAdemdum = 'N';
			if($scope.data.seleccionOperacionesMesaRF[i].ConfirmaAdemdum != actual){
				errores++;
				mensaje = $translate.instant("UI_OPERACION_CON_ERRORES");
				errorMsg = $translate.instant("UI_OPERACION_PACTO_ERROR");
				errorArr.push({Descripcion:errorMsg});
				break;
			}
			if($scope.data.seleccionOperacionesMesaRF[i].IdTipoOperacion != "COMPAC" && $scope.data.seleccionOperacionesMesaRF[i].IdTipoOperacion != "VENPAC"){
				errores++;
				mensaje = $translate.instant("UI_OPERACION_CON_ERRORES");
				errorMsg = $translate.instant("UI_OPERACION_PACTO_ERROR_TERMINO");
				errorArr.push({Descripcion:errorMsg});
				break;
			}
		}
		if(errores > 0){
			bcsComunes.ventanaMsg({ mensaje:$translate.instant(mensaje), conflictos:{errores:errorArr}},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
			return;
		}
		
		var pregunta = $translate.instant("UI_MENSAJE_PACTO_CONFIRMACION")+'?';
		if($scope.data.seleccionOperacionesMesaRF.ConfirmaAdemdum == 'S') pregunta = $translate.instant("UI_MENSAJE_PACTO_DESCONFIRMACION")+'?';
		var t = bcsAlerta.crear("PREGUNTA_SI_NO",pregunta);
		t.result.then(function(result){
			if(result === OPCIONES_BCSALERTA.SI){
				
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;
				datos.p_IdUsuario = $scope.SesionIdUsuario;
		    	datos.p_IdModulo = 'MRF';
		    	datos.p_UsaReglasNegocio = 'S';
		    	datos.p_Operaciones = [];
				var ope = {};	
				$scope.data.seleccionOperacionesMesaRF.forEach(function(key,value) {
					if (value == 'IdOperacion') {
						ope.IdOperacion = key;
						ope.ConfirmaAdemdum = ($scope.data.seleccionOperacionesMesaRF.ConfirmaAdemdum === 'S') ? 'N' : 'S'
						datos.p_Operaciones.push(ope);
					}

				});
				;
				
				bcsServiciosMesa.ActualizarPactoOperacionesMesaRF(datos,successPacto,error);
			}
			else{
				//bcsComunes.ventanaCerrar($scope.winID);
				return;
			}
		});
		//bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_OPERACION'));
	}


	// --------------------------------------------------
	// Comprobar cierre de mesa
	// --------------------------------------------------

	cMesa.ds.Mesas = new bcs.DataSource({
		type: "odata",
		definition: {
			serverFiltering: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=452",
					dataType: "json"
				}
			},
			schema: {
				model: {
					fields: {
						IdMesa: { type: "number" },
						Codigo: { type: "string" },
						Nombre: { type: "string" },
						IdTipoProducto: { type: "string" },
						IdEstadoMesa: { type: "string" },
						IdEstadoRegistro: { type: "string" },
						FechaModificacionFrmt: { type: "string" }
					}
				}
			}
		}
	});

	async function comprobarCierreMesas() {
		var mesas = [];
		const IdTipoProductoMesaModulo = 'MERF';
		var filterMesas = [
			{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			{ field: "IdTipoProductoMesaModulo", operator: "eq", value: IdTipoProductoMesaModulo }
		];

		await cMesa.ds.Mesas.query({ filter: filterMesas }).then(function () {
			if (cMesa.ds.Mesas.view().length > 0) {
				$.each(cMesa.ds.Mesas.data(), function (key, value) {
					var aux = {};
					aux.IdMesa = value.IdMesa;
					aux.NombreMesa = value.NombreMesa;
					aux.IdEstadoMesa = value.IdEstadoMesa;
					aux.MesaCerrada = value.IdEstadoMesa === 'C' ? true : false;
					aux.MsjEstadoMesa = value.IdEstadoMesa === 'C' ? $translate.instant("UI_MESA_CERRADA") : $translate.instant("UI_MESA_ABIERTA");

					mesas.push(aux);
				});
				cMesa.mesas = mesas;
			}
		})
	}
	
	// --------------------------------------------------
	// Reasignar Compras y Ventas a termino
	// --------------------------------------------------
	
	$scope.reasignar = async function(operacion){

		await comprobarCierreMesas();

		var datosMesa = $scope.data.oConfiguracionMesas.seleccionado;
		if (datosMesa === undefined) {
			bcsComunes.msg("warning", $translate.instant(""), 'Debe seleccionar una operacion');
		} else {
		
			var mesaActual = cMesa.mesas.filter(mesa => mesa.IdMesa == datosMesa.IdMesa);
			mesaActual = mesaActual[0];

			if (mesaActual.MesaCerrada) {
				bcsComunes.ventanaMsgErr(`"${mesaActual.NombreMesa}" ` + $translate.instant("UI_MESA_CERRADA"), $scope.ventanaId)
			} else {
				var datos = {};
				var id1=bcsComunes.ventanaAbrir('ReasignacionMesaRF', 
					{operacion : operacion},false,false);
			}
		}
	}
	var array = [
		{ Estado: 'Vigente', Color: 'black' },
		{ Estado: 'Emitido', Color: 'orange' },
	    { Estado: 'Pendiente', Color: 'blue' },
	    { Estado: 'Liquidado', Color: 'green' },
	    { Estado: 'Reasignación', Color: 'brown-font' },
	    { Estado: 'Anticipado', Color: 'light-green-font' },
	    { Estado: 'Anulado', Color: 'red' }	    
	];
	
		bcsComunes.tootipGlosa('toolConsultaOperacionesMesaRF', array)
});