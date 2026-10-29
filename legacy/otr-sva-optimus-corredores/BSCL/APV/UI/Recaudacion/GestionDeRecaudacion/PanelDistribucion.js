/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelDistribucionCtrl',function($scope,
			$translate,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsAlerta,
			OPCIONES_BCSALERTA,
			bcsConsultasAPV,
			bcsServiciosAPV){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var idVentana = bcsComunes.ventanaWinId($scope);
		var pDistr = this;
		
		pDistr.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		pDistr.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		pDistr.habilitadoBotonValidar = false;
		/******************************************************
		 * PRECARGA DE VARIABLES
		 * ***************************************************/
		$scope.pDistr.filtroEstado = {
				NombreEntidad : {field: "NombreEntidad", operator: "eq", value: 'RECAUDACION'}
		}
		/******************************************************
		 * GRILLAS
		 * ***************************************************/
		pDistr.gridRecaudacion = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					sort: { field: "IdRecaudacionCliente", dir: "desc" },
					pageSize 		: 15,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6024",
							dataType	: "json"
					    }
	                },
					//filter: { field: "IdPersonaOrigen", operator: "eq", value: Number(ppr.IdPersona) },
					schema: {
						model: {
							fields: {
								CodigoEstado		: { type: "string"},
								NombreEstado		: { type: "string"},
								FechaDeposito		: { type: "sysdate"},
								FechaIngreso		: { type: "sysdate"},
								FolioRecaudacion    : { type: "string"},
								FolioCartola	    : { type: "string"},
								LineaCartola	    : { type: "string"},
								IdPersonaCliente	: { type: "number"},
								RutCliente			: { type: "string"},	
								Nombre				: { type: "string"},
								RecMonto			: { type: "number"},
								TipoRegimen			: { type: "string"},
								FolioContrato       : { type: "string"},
								RecTotal			: { type: "number"},
								RecDiferencia		: { type: "number"},
								DisMontoDC			: { type: "number"},
								DisMontoAPV			: { type: "number"},
								DisTotal			: { type: "number"},
								DisDiferencia		: { type: "number"},
								CodigoBanco			: { type: "string"},
								CuentaCorriente		: { type: "string"},
								Regimen				: { type: "string"},
								IdPersonaEmpleador	: { type: "number"},
								RutEmpleador		: { type: "string"},
								NombreEmpleador		: { type: "string"},
								PeriodoPago			: { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreEstado"		,title: $translate.instant('UI_ESTADO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "FechaDeposito"		,title: $translate.instant('UI_FECHA_DEPOSITO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "FechaIngreso"		,title: $translate.instant('UI_FECHA_INGRESO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "FolioRecaudacion"	,title: $translate.instant('UI_FOLIO_RECAUDACION'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "FolioCartola"		,title: $translate.instant('UI_FOLIO_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "LineaCartola"		,title: $translate.instant('UI_LINEA_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "RutCliente"		,title: $translate.instant('UI_RUT_CLIENTE'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "Nombre"			,title: $translate.instant('UI_NOMBRE'), width: "250px", align: "left" , sortable : true, editable: false },
				{field: "RecMonto"		    ,title: $translate.instant('UI_MONTO_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(RecMonto,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "TipoRegimen"		,title: $translate.instant('UI_TIPO_REGIMEN'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "FolioContrato"	    ,title: $translate.instant('UI_FOLIO_CONTRATO'), width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "RecTotal"			,title: $translate.instant('UI_TOTAL_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false		, template : " <label> #:  (util.bcs_formato(RecTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" },
				{field: "DisMontoDC"		,title: $translate.instant('UI_MONTO_DC_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(DisMontoDC,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "DisMontoAPV"		,title: $translate.instant('UI_MONTO_APV_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(DisMontoAPV,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "DisTotal"			,title: $translate.instant('UI_TOTAL_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(DisTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "CodigoBanco"		,title: $translate.instant('UI_BANCO'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "CuentaCorriente"	,title: $translate.instant('UI_CTA_CTE'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "Regimen"			,title: $translate.instant('UI_REGIMEN'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "RutEmpleador"		,title: $translate.instant('UI_RUT_EMPLEADOR'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "NombreEmpleador"	,title: $translate.instant('UI_NOMBRE_EMPLEADOR'), width: "250px", align: "left" , sortable : true, editable: false },
				{field: "PeriodoPago"		,title: $translate.instant('UI_PERIODO_PAGO'), width: "250px", align: "left" , sortable : true, editable: false },
			],	
			sortable		    : true,
      		reorderable			: true,
			pageable			: true,
			autoBind			: false,
			height				: 270,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			filterable			: true
		};
		
		//------ DATA SOURCE
		var DefConsultaTotales = new bcs.DataSource({ 
			type : "call",
			definition : {
				callTransaction	: "6030",
				serverFiltering : false, 
				serverPaging : false, 
				serverSorting : false,
				callParameters  : {},
				callResultSet   : "p_Resumen",
				sort: { field: "NombreEstado" },
				schema : {
					model : {
						fields : {
							NombreEstado		: {type : "string"},
							RegistrosRecaudado	: {type : "string"},
							DisTribuidoTotal	: {type : "number"},
							RecTotal			: {type : "number"},
							Diferencia			: {type : "number"}
			    		}
					}
				}
			}		
		});
		//------ GRILLA
		
		pDistr.ConsultaTotales =
			{
				dataSource	: DefConsultaTotales,
				columns		: [  {field: "NombreEstado"			, title: "&nbsp"			, width: "50px", align: "left" , sortable : true, editable: false }
								,{field: "RegistrosRecaudado"	, title: "UI_REGISTROS"		, width: "50px", align: "right" , sortable : true, editable: false }
								,{field: "RecTotal"				, title: "UI_RECAUDADO"		, width: "50px", align: "right" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(RecTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "DisTribuidoTotal"		, title: "UI_DISTRIBUIDO"	, width: "50px", align: "right" , sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(DisTribuidoTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "Diferencia"			, title: "UI_DIFERENCIA"	, width: "50px", align: "right", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(Diferencia,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"  }
								],
									sortable 	: true,
									filterable	: true,
						    		navigatable	: true,
						    		height		: 175,
						    		selectable 	: "row",
						    		autoBind 	: false,
			};	
		
		var dsConsultaTotales2 = new bcs.DataSource(DefConsultaTotales);
		pDistr.ConsultaTotales2 =
		{
			dataSource	:dsConsultaTotales2,
			columns		: [  {field: "NombreEstado"			, title: "UI_ESTADO"			, width: "30px", align: "left", sortable : true, editable: false }
							,{field: "RegistrosRecaudado"	, title: "UI_RECAUDADO"			, width: "40px", align: "right", sortable : true, editable: false }
							,{field: "DisTribuidoTotal"		, title: "UI_DISTRIBUIDO"		, width: "40px", align: "right", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(DisTribuidoTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
							,{field: "RecTotal"				, title: "UI_RECUPERADO"		, width: "40px", align: "right", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(RecTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
							,{field: "Diferencia"			, title: "UI_DIFERENCIA_REZAGO"	, width: "45px", align: "right", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(Diferencia,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>" }
							],
								sortable 	: false,
								filterable	: true,
					    		navigatable	: true,
					    		height		: 100,
					    		autoBind 	: false,
		};	
	

		/******************************************************
		 * FUNCIONES
		 * ***************************************************/
		$scope.pDistr.buscarRegistros = function(nroPag, filtrosAnteriores){
			
			//Filtros Busqueda
			
			var filtroBusqueda = new Array();
			
		
			if(pDistr.kngFechaDesde){
					filtroBusqueda.push({ field: "FechaDeposito", operator: "gte", value: util.dateSPdate($scope.pDistr.kngFechaDesde)});
			}
			if(pDistr.kngFechaHasta){
					filtroBusqueda.push({ field: "FechaDeposito", operator: "lte", value: util.dateSPdate($scope.pDistr.kngFechaHasta )});
			}
			
			if(pDistr.tipoDepositoSeleccionado.IdRecaudacion !== undefined){
				filtroBusqueda.push({ field: "FolioRecaudacion", operator: "eq", value: $scope.pDistr.tipoDepositoSeleccionado.IdRecaudacion});
			}
			
			if(pDistr.empleadorSeleccionado.DocumentoIdentidad !== undefined){
				filtroBusqueda.push({ field: "RutEmpleador", operator: "eq", value: $scope.pDistr.empleadorSeleccionado.DocumentoIdentidad});
			}
			
			if(pDistr.clienteSeleccionado.DocumentoIdentidad !== undefined){
				filtroBusqueda.push({ field: "RutCliente", operator: "eq", value: $scope.pDistr.clienteSeleccionado.DocumentoIdentidad});
			}
			
			if(pDistr.oConfEstado.seleccionado.Codigo !== undefined){
				filtroBusqueda.push({ field: "CodigoEstado", operator: "eq", value: $scope.pDistr.oConfEstado.seleccionado.Codigo });
			}
			
			pDistr.gridRecaudacion.dataSource.filter(filtroBusqueda);
			
			//LLenar Totales Por Fecha
			//Parametros Fecha
			var filtroBusquedaTotales = {
				
				p_FechaDesde: pDistr.kngFechaDesde.toISOString().slice(0,10),
				p_FechaHasta: pDistr.kngFechaHasta.toISOString().slice(0,10)
 	
			};
			bcsServiciosAPV.ConsultarTotalesDistRec(filtroBusquedaTotales).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
		   		pDistr.ConsultaTotales.dataSource.data(respuesta.p_Resumen);
		   		pDistr.ConsultaTotales2.dataSource.data(respuesta.p_Totales);
	
				let respRezago = respuesta.p_Resumen.find(function (res){ return res.NombreEstado === "REZAGO" });
				pDistr.habilitadoBotonValidar = pDistr.oConfEstado.seleccionado.Codigo == "REZ" && respRezago.RegistrosRecaudado > 0;
				
				/*if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
					bcsComunes.ventanaOverlay(idVentana,false);
			    }*/
	   		},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
	   			$.each(ErrorDetalle,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   		});
			
			
		};
		
		//ABRI ESTADOS
		pDistr.abrirEstadosDistr = function(items) {
														//PanelDistribucionEstados
			var IdVentanamodal = bcsComunes.ventanaAbrir("PanelDistribucionEstados", {IdRegistro : items[0].IdRecaudacionCliente } );
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				var x = 0;
			});
		
		};
		//FIN
		
		pDistr.DevolucionPagoRezagos = function(items,tipo){
			if( items !== undefined){
				var validacionOK = true;
				if (tipo === "CLIENTE"){
						var validacionCliente = '';
						$.each(items,function(key,value){
							if (validacionCliente ==="") validacionCliente = value.IdPersonaCliente;
							else if (validacionCliente !== value.IdPersonaCliente)validacionOK = false;
						});	
				}


				if (tipo === "EMPLEADOR"){
						var validacionEmpleador = '';
						$.each(items,function(key,value){
							if (validacionEmpleador ==="") validacionEmpleador = value.IdPersonaEmpleador;
							else if (validacionEmpleador !== value.IdPersonaEmpleador)validacionOK = false;
						});	
				}

				if (validacionOK === false) return;
				var datosRecaudacion = [];
				$.each(items,function(key,value){
					/*if(items.CodigoEstado === 'REZ'){}*/
					var datos = {
						FolioRecaudacion    	: value.FolioRecaudacion,
						FolioCartola	    	: value.FolioCartola,
						LineaCartola	    	: value.LineaCartola,
						IdRecaudacionCliente	: value.IdRecaudacionCliente,
						RutCliente				: value.RutCliente,	
						Nombre					: value.Nombre,
						RecMonto				: value.RecMonto,
						TipoRegimen				: value.TipoRegimen,
						RecTotal				: value.RecTotal,
						RecDiferencia			: value.RecDiferencia,
						DisMontoDC				: value.DisMontoDC,
						DisMontoAPV				: value.DisMontoAPV,
						DisTotal				: value.DisTotal,
						DisDiferencia			: value.DisDiferencia,
						CodigoBanco				: value.CodigoBanco,
						CuentaCorriente			: value.CuentaCorriente,
						Regimen					: value.Regimen,
						IdRecaudacionEmpleador	: value.IdRecaudacionEmpleador,
						RutEmpleador			: value.RutEmpleador,
						NombreEmpleador			: value.NombreEmpleador
					}
					datosRecaudacion.push(datos);
				});
				
				if(datosRecaudacion.length > 0){
					var IdVentanamodal = bcsComunes.ventanaAbrir("DevolucionPagoRezagos", {dsDatosRecaudacion : datosRecaudacion } );
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
						var x = 0;
					});	
				}		
			}
		};
		
		pDistr.DistribuirConciliadoRezagado = function(){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se realizará el proceso de Distribuir la recaudación. ¿Desea continuar?" ,idVentana);
			t .then(function(result)
			{
				if (result === OPCIONES_BCSALERTA.NO)
				{
					return;
				}
				
				bcsComunes.ventanaOverlay(idVentana,true);
				var paramEntrada = [];
				$.each(pDistr.gridRecaudacion.dataSource.view(),function(key,value){
					if(/*(value.CodigoEstado === 'CON' || value.CodigoEstado === 'REZ') &&*/ value.IdRecaudacionCliente !== undefined && value.IdRecaudacionCliente !== undefined){
						var datosparamEntrada = {
							IdRecaudacionCliente 	: value.IdRecaudacionCliente, 
							IdRecaudacionEmpleador  : 0
						};				
						paramEntrada.push(datosparamEntrada);
					}
				});
				
				var grupoParamEntrada = _.groupBy(paramEntrada,'IdRecaudacionCliente','IdRecaudacionEmpleador');
				
				if (grupoParamEntrada.length === 0){
					bcsComunes.ventanaMsgErr($translate.instant('UI_NO_HAY_RECAUDACIONES'),$scope.ventanaId);
					bcsComunes.ventanaOverlay(idVentana,false);
					return;				
				};
			
				var parametrosDistribucion= [];
				
				if($scope.pDistr.recaudacionSeleccionada && $scope.pDistr.recaudacionSeleccionada.length>0){
					
					parametrosDistribucion= $scope.pDistr.recaudacionSeleccionada.map(function(item){
						return {
								IdRecaudacion 	: item.FolioRecaudacion
						};
					})
				}else{
					parametrosDistribucion[{IdRecaudacion: 0}]
				}
				
				var parametros = {				
						p_IdEmpresa  		 : $scope.SesionIdEmpresa,
						p_IdUsuario  		 : $scope.SesionIdUsuario,
						p_IdModulo   		 : "APV", 
						p_Opcion	 		 : "DIS", 
						p_Parametros 		 : parametrosDistribucion,
						p_DistribucionOrigen : "S"
					};
								
					bcsComunes.ventanaOverlay($scope.ventanaId,true);
					bcsServiciosAPV.ProcesarDistribucion(parametros).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						if(respuesta.P_OUT_MENSAJES.length > 0){
							var mensajeCarga = new Array();
							$.each(respuesta.P_OUT_MENSAJES,function(key,value){
								 var objCarga = {
										IdInterno : value.id,
										tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
										codigo : value.codigo,
										descripcion : value.descripcion
								 };
								 mensajeCarga.push(objCarga);
							});
					
							respuesta.P_OUT_RESULTADO = mensajeCarga;
							bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							
			
							bcsComunes.ventanaOverlay(idVentana,false);
															
						}
						pDistr.buscarRegistros();
			   		},function(respuesta){
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
			   			$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
						
					  bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			   		});	
			});	
		};
		
		pDistr.verDoctos = function()
		{
			var filterVentana = Array();
			if(pDistr.recaudacionSeleccionada !== undefined)
			{
				filterVentana.Nombre 		= pDistr.recaudacionSeleccionada[0].Nombre;
				filterVentana.RutCliente	= pDistr.recaudacionSeleccionada[0].RutCliente;
				filterVentana.IdRecaudacionCliente	= pDistr.recaudacionSeleccionada[0].IdRecaudacionCliente;
			}
			else
			{
				bcsComunes.msg("warning",'Warning','Debe seleccionar una Recaudacion para poder ver las asignaciones.');
				return;
			}
			
		 	var IdVentanamodal = bcsComunes.ventanaAbrir("VerDoctosDistribucion",filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
					//
		    });
		}
		
		pDistr.AsignacionPortafolioAPV = function(modoVentana)
		{
			var filterVentana = Array();
			filterVentana.modoVentana = modoVentana;
			
			if(pDistr.recaudacionSeleccionada !== undefined)
			{
				filterVentana.Nombre 		= pDistr.recaudacionSeleccionada[0].Nombre;
				filterVentana.RutCliente	= pDistr.recaudacionSeleccionada[0].RutCliente;
				filterVentana.IdRecaudacionCliente	= pDistr.recaudacionSeleccionada[0].IdRecaudacionCliente;
				filterVentana.TipoRegimen	= pDistr.recaudacionSeleccionada[0].TipoRegimen;
				filterVentana.Regimen		= pDistr.recaudacionSeleccionada[0].Regimen;
				filterVentana.CodigoMedioRecaudacion = ( pDistr.recaudacionSeleccionada[0].RutEmpleador && pDistr.recaudacionSeleccionada[0].RutEmpleador != "" )
																? "ATREMP"
																: "DIREMP";
			}
			else
			{
				bcsComunes.msg("warning",'Warning','Debe seleccionar una Recaudacion para poder ver las asignaciones.');
				return;
			}
			
			var IdVentanamodal = bcsComunes.ventanaAbrir("AsignacionPortafolioAPV",filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				pDistr.buscarRegistros();
		    });
		}
		
		pDistr.ValidacionNuevosSAAPV = function(items) {

				if(pDistr.habilitadoBotonValidar){
					var parametros = {				
						p_IdEmpresa  : $scope.SesionIdEmpresa,
						p_IdUsuario  : $scope.SesionIdUsuario,
						p_IdModulo   : "APV", 
						p_FiltrosRecaudacion: {
							p_FechaDesde : pDistr.kngFechaDesde ? pDistr.kngFechaDesde.toISOString().slice(0,10) : "0001-01-01",
							p_FechaHasta : pDistr.kngFechaHasta ? pDistr.kngFechaHasta.toISOString().slice(0,10) : "0001-01-01",
							p_FolioRecaudacion : pDistr.tipoDepositoSeleccionado.IdRecaudacion !== undefined ? $scope.pDistr.tipoDepositoSeleccionado.IdRecaudacion : 0 ,
							p_RutCliente : pDistr.clienteSeleccionado.DocumentoIdentidad !== undefined ? $scope.pDistr.clienteSeleccionado.DocumentoIdentidad : "" ,
							p_RutEmpleador : pDistr.empleadorSeleccionado.DocumentoIdentidad !== undefined? $scope.pDistr.empleadorSeleccionado.DocumentoIdentidad : ""
						}
						
					};
					
					bcsServiciosAPV.ValidarYAsignarContratosARecaudaciones(parametros).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						respuesta.P_OUT_RESULTADO = new Array();
						
						if(respuesta.P_OUT_MENSAJES.length > 0){
							$.each(obtenerMensajes(respuesta.P_OUT_MENSAJES),function(key,value){
								respuesta.P_OUT_RESULTADO.push( value );
							});
						}
						
						if(respuesta.P_OUT_ADVERTENCIAS.length > 0){
							$.each(obtenerMensajes(respuesta.P_OUT_ADVERTENCIAS),function(key,value){
								respuesta.P_OUT_RESULTADO.push( value );
							});
						}
						
						bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							
			
						bcsComunes.ventanaOverlay(idVentana,false);
						$scope.habilitarBoton = true;
						
						pDistr.buscarRegistros();
						
					},function(respuesta){
						var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
			   			$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			   		});
				}
		}
		
		let obtenerMensajes = function (listadoMensajes){
			var mensajeCarga = new Array();
			$.each(listadoMensajes,function(key,value){
				 var objCarga = {
						IdInterno : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : value.descripcion
				 };
				 mensajeCarga.push(objCarga);
			});
			return mensajeCarga;
		};
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
