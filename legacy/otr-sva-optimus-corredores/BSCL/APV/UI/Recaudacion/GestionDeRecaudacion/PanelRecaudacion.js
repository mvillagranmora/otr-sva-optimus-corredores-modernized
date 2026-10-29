/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelRecaudacionCtrl',function($scope,
			$translate,
			$resource,
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
		var pRec = this;
		$scope.kOptFechaMovimiento = {};
		
		pRec.filtroBanco = {
				IdPaisOperacion : {field: "IdPaisOperacion", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		}
		var datosDeposito = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: true,
                    pageSize        : 10,
					sortable		: true,
		      		reorderable		: true,
		      		filterable		: true,
					groupable		: true,
					pageable		: true,
		      		autoBind		: false,
		      		scrollable		: true,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacion"			: {type: "number"},
													"IdEmpresa"				: {type: "number"},
													"DetalleEmpleador"		: {type: "string"},
													"DetalleCliente"		: {type: "string"},
													"FolioCartola"			: {type: "string"},
													"LineaCartola"			: {type: "number"},
													"Sucursal"				: {type: "string"},
													"FechaDeposito"			: {type: "sysdate"},
													"CuentaCorriente"		: {type: "number"},
													"MontoDeposito"			: {type: "number"},
													"ConceptoRecaudacion"	: {type: "string"},
													"Origen"				: {type: "string"},
													"Observacion"			: {type: "string"},
													"NumeroDocumento"		: {type: "string"},
													"IdTipoDeposito"		: {type: "string"},
													"NombreTipoDeposito"	: {type: "string"},
													"IdBanco"				: {type: "number"},
													"NombreBanco"			: {type: "string"},
													"IdRegimenTributario"	: {type: "number"},
													"IdCarga"				: {type: "number"},
													"IdEstado"				: {type: "string"},
													"Estado"				: {type: "string"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosDeposito = new bcs.DataSource(datosDeposito);
		pRec.confGridDeposito = 
		{
    		dataSource		: dsDatosDeposito,
    			columns		: [  {field: "IdRecaudacion"		, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false }
    							,{field: "FolioCartola"			, title: "UI_FOLIO_CARTOLA"		, width: "100px", align: "right"	, sortable : true, editable: false }
    							,{field: "NombreTipoDeposito"	, title: "UI_TIPO_DEPOSITO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreBanco"			, title: "UI_BANCO"				, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaDeposito"		, title: "UI_FECHA_INGRESO"		, width: "90px", align: "left"		, sortable : true, editable: false }
								,{field: "CuentaCorriente"		, title: "UI_CUENTA_CORRIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoDeposito"		, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDeposito,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "Estado"				, title: "UI_ESTADO"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "Origen"				, title: "UI_ORIGEN"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleEmpleador"		, title: "UI_DETALLE_EMPLEADOR"	, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleCliente"		, title: "UI_DETALLE_CLIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 200,
	    		selectable 	: "row",
	    		autoBind 	: true
		};	
		
		var datosEmpleador = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "FechaIngreso", dir: "DESC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarEmpleadorRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacionEmpleador"	: {type: "number"},
													"IdRecaudacion"				: {type: "number"},
													"IdEmpresa"					: {type: "number"},
													"DetalleCliente"			: {type: "string"},
													"IdPersona"					: {type: "number"},
													"DocumentoIdentidad"		: {type: "string"},
													"NombreCompleto"			: {type: "string"},
													"FechaIngreso"				: {type: "sysdate"},
													"PeriodoPago"				: {type: "string"},
													"NumeroHojas"				: {type: "number"},
													"TipoAfiliado"				: {type: "string"},
													"TipoPago"					: {type: "string"},
													"TotalResumen"				: {type: "number"},
													"Insistencias"				: {type: "string"},
													"MontoDepositoConvenido"	: {type: "number"},
													"MontoDepositoAPV"			: {type: "number"},
													"Reajustes"					: {type: "number"},
													"Intereses"					: {type: "number"},
													"Origen"					: {type: "string"},
													"Observaciones"				: {type: "string"},
													"IdEstado"					: {type: "string"},
													"Estado"					: {type: "string"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosEmpleador = new bcs.DataSource(datosEmpleador);
		pRec.confGridEmpleador = 
		{
    		dataSource		: dsDatosEmpleador,
    			columns		: [  {field: "IdRecaudacionEmpleador"	, title: "UI_FOLIO_EMPLEADOR"	, width: "100px", align: "right"	, sortable : true, editable: false }
    							,{field: "IdRecaudacion"			, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "70px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "PeriodoPago"				, title: "UI_PERIODO_PAGO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "Estado"					, title: "UI_ESTADO"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "TotalResumen"				, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(TotalResumen,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "Origen"					, title: "UI_ORIGEN"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleCliente"			, title: "UI_DETALLE_CLIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 200,
	    		selectable 	: "row",
	    		autoBind 	: false
		};	
		
		
		var datosCliente = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "FechaIngreso", dir: "DESC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarClienteRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacionCliente"		: {type: "number"},
													"IdRecaudacion"				: {type: "number"},
													"IdRecaudacionEmpleador"	: {type: "number"},
													"IdEmpresa"					: {type: "number"},
													"IdCliente"					: {type: "number"},
													"IdPersona"					: {type: "number"},
													"DocumentoIdentidad"		: {type: "string"},
													"NombreCompleto"			: {type: "string"},
													"FechaIngreso"				: {type: "sysdate"},
													"TipoAfiliado"				: {type: "string"},
													"IdRegimenTriburario"		: {type: "string"},
													"MontoDepositoConvenido"	: {type: "number"},
													"MontoDepositoAPV"			: {type: "number"},
													"TotalDigitado"				: {type: "number"},
													"Origen"					: {type: "string"},
													"IdEstado"					: {type: "number"},
													"Estado"					: {type: "string"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosCliente = new bcs.DataSource(datosCliente);
		pRec.confGridCliente = 
		{
    		dataSource		: dsDatosCliente,
    			columns		: [	
    							 {field: "IdRecaudacionCliente"		, title: "UI_FOLIO_CLIENTE"		, width: "90px", align: "right"	, sortable : true, editable: false }
     							,{field: "IdRecaudacion"			, title: "UI_FOLIO_DEPOSITO"	, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdRecaudacionEmpleador"	, title: "UI_FOLIO_EMPLEADOR"	, width: "110px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "70px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "Estado"					, title: "UI_ESTADO"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "TotalDigitado"			, title: "UI_MONTO_DEPOSITO"	, width: "100px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(TotalDigitado,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoDepositoAPV"			, title: "UI_MONTO_APV"			, width: "100px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDepositoAPV,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoDepositoConvenido"	, title: "UI_MONTO_DC"			, width: "100px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDepositoConvenido,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "Origen"					, title: "UI_ORIGEN"			, width: "100px", align: "right"	, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 180,
	    		selectable 	: "row",
	    		autoBind 	: false
		};	
		
		var datosTotalesOrigen = 
		{
				type : "array",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "Origen", dir: "DESC" },
                    pageSize        : 10,
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"Origen"	: {type: "number"},
													"Total"		: {type: "number"},
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosTotalesOrigen = new bcs.DataSource(datosTotalesOrigen);
		pRec.confGridTotalesOrigen = 
		{
    		dataSource		: dsDatosTotalesOrigen,
    			columns		: [	
    							 {field: "Origen"	, title: "UI_ORIGEN"	, width: "110px", align: "right"	, sortable : true, editable: false }
    							,{field: "Total"	, title: "UI_TOTAL"		, width: "110px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(Total,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 180,
	    		selectable 	: "row",
	    		autoBind 	: true
		};
		
		pRec.filtroDepositos = function(){
			var filter = Array(); 
			
			if(pRec.tipoDepositoSeleccionado !== undefined && pRec.tipoDepositoSeleccionado !== false)
				filter.push({ field: "IdRecaudacion"	, operator: "eq", value: Number(pRec.tipoDepositoSeleccionado.IdRecaudacion) });
			
			if(pRec.bancoDepositoSeleccionado !== undefined && pRec.bancoDepositoSeleccionado !== false)
				filter.push({ field: "IdBanco"		, operator: "eq", value: Number(pRec.bancoDepositoSeleccionado.IdBanco) });
			
			if(pRec.kngFechaMovimiento !== undefined)
				filter.push({ field: "FechaDeposito"	, operator: "eq", value: pRec.kngFechaMovimiento });
			
			if(filter.length > 0)
				dsDatosDeposito.filter(filter);
			else
				dsDatosDeposito.read();
		};

		pRec.verDoctos = function()
		{
			var filterVentana = Array();
			if(pRec.seleccionDeposito !== undefined)
			{
				filterVentana.IdRecaudacion	= pRec.seleccionDeposito.IdRecaudacion;
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
		
		pRec.limpiarDepositos = function(){
			var filter = Array(); 
			
			pRec.tipoDepositoSeleccionado = undefined;
			pRec.bancoDepositoSeleccionado = undefined;
			pRec.kngFechaMovimiento = undefined;
			pRec.bancoDeposito = undefined;
			pRec.folioContrato = undefined;
			pRec.seleccionDeposito = undefined;
			
			var filter = Array(); 

			filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
			dsDatosDeposito.filter(filter);
			dsDatosDeposito.read().then(function(){
				pRec.resumen 				= 0;
				pRec.montoCalculado 		= 0;
				pRec.montoDigitado 			= 0;
				pRec.diferenciaEmpleador	= 0;
				pRec.diferenciaCliente		= 0;
				
				var filter = Array();
				filter.push({ field: "IdRecaudacion", operator: "eq", value: -1 });
				dsDatosEmpleador.filter(filter);
				dsDatosEmpleador.read().then(function(){
					dsDatosCliente.filter(filter);
					dsDatosCliente.read().then(function(){
					
					});
				});
			});
		};
		
		pRec.filtroEmpleadorPorDeposito = function(Deposito){			
			if(Deposito !== 0 && Deposito !== undefined){
				var filter = Array();
				filter.push({ field: "IdRecaudacion", operator: "eq", value: Deposito.IdRecaudacion });
				
				dsDatosEmpleador.query({filter:filter}).then(function(){	
					var filter = Array();
					filter.push({ field: "IdRecaudacion", operator: "eq", value: Deposito.IdRecaudacion });
					
					dsDatosCliente.query({filter:filter}).then(function(){
					
						pRec.resumen = Deposito.MontoDeposito;
						var totalEmpleador = 0;
						var totalCliente = 0;
						
						for (i = 0; i < dsDatosEmpleador.data().length; i++) { 
							totalEmpleador = totalEmpleador + dsDatosEmpleador.data()[i].TotalResumen;
						}
						
						pRec.montoCalculado = totalEmpleador;
						
						for (i = 0; i < dsDatosCliente.data().length; i++) { 
							totalCliente = totalCliente + dsDatosCliente.data()[i].TotalDigitado;
						}
						
						pRec.montoDigitado			= totalCliente;
						pRec.diferenciaEmpleador	= Deposito.MontoDeposito - totalEmpleador;
						pRec.diferenciaCliente		= Deposito.MontoDeposito - totalCliente;
					});
				});
				
				
				dsDatosEmpleador.filter(filter);
				dsDatosEmpleador.read().then(function(){
					dsDatosCliente.filter(filter);
					dsDatosCliente.read().then(function(){
					
					});
		        });
				
				
				var parametrosOrigen = {				
						p_IdEmpresa 	: $scope.SesionIdEmpresa,
						p_IdUsuario 	: $scope.SesionIdUsuario,
						p_IdModulo  	: "APV",
						p_AsignacionAutomaticaContrato : "S",
						p_IdRecaudacion	: Deposito.IdRecaudacion,
				};
					
				bcsServiciosAPV.ConsultarTotalesOrigen(parametrosOrigen).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			   		
			   		pRec.confGridTotalesOrigen.dataSource.data(respuesta.P_TOTALES_ORIGEN);

		   		},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
		   			$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   		});
				
			}
		};
		
		pRec.filtroClientePorEmpleador = function(Empleador){			
			if(Empleador !== 0 && Empleador !== undefined){
				var filter = Array();
				filter.push({ field: "IdRecaudacionEmpleador", operator: "eq", value: Empleador.IdRecaudacionEmpleador });
				dsDatosCliente.filter(filter);
				dsDatosCliente.read().then(function(){
					
				});
			}
		};
		
		pRec.VentanaDeposito = function(modoVentana){
			var parametros = Array(); 
			parametros.modoVentana = modoVentana === 'M' ? "M" : "I";
			if(modoVentana === "M"){
				if(pRec.seleccionDeposito !== undefined){
					if (pRec.seleccionDeposito.IdEstado === 'DIS'){
						bcsComunes.msg("error",'error','Depósito Distribuido, no se puede modificar.');
						return;
					}	
					parametros.idRecaudacion = pRec.seleccionDeposito.IdRecaudacion;
				}
				else{
					bcsComunes.msg("error",'error','Debe seleccionar un Depósito para poder modificar.');
					return;
				}
			}
			var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoRecaudacionDeposito", parametros, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				var filter = Array();
				filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
				dsDatosDeposito.filter(filter);
				dsDatosDeposito.read().then(function(){
					
				});
	        });
		};
		
		pRec.VentanaConciliarDeposito = function(modoVentana){
			var parametros = Array(); 
			parametros.modoVentana = modoVentana;

			var IdVentanamodal = bcsComunes.ventanaAbrir("AsignarDeposito", parametros, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				var filter = Array();
				filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
				dsDatosDeposito.filter(filter);
				dsDatosDeposito.read().then(function(){
					
				});
	        });
		};
		
		pRec.VentanaDetalle = function(){
			if(pRec.seleccionDeposito !== undefined){
				var parametros = Array(); 
				
				parametros.idRecaudacion = pRec.seleccionDeposito.IdRecaudacion;
	
				var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoRecaudacionDetalle", parametros, true, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					var filter = Array();
					filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
					dsDatosDeposito.filter(filter);
					dsDatosDeposito.read().then(function(){
						var filtro = Array();
						filtro.push({ field: "IdRecaudacion", operator: "eq", value: parametros.idRecaudacion });
						dsDatosEmpleador.filter(filtro);
						dsDatosEmpleador.read().then(function(){
							dsDatosCliente.filter(filtro);
							dsDatosCliente.read().then(function(){
							
							});
				        });
					});
		        });
			}else{
				bcsComunes.msg("error",'error','Debe seleccionar un Depósito para poder modificar.');
			}
		};
		
		pRec.fnConciliar = function(){
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se realizará el proceso de validación de los depositos ingresados. ¿Desea continuar?" ,idVentana);
			t .then(function(result)
			{
				if (result === OPCIONES_BCSALERTA.NO)
				{
					return;
				}
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				if(pRec.seleccionDeposito !== undefined){
					if(	pRec.seleccionDeposito.IdEstado === "ING" ||
						pRec.seleccionDeposito.IdEstado === "NCO")
					{
						var parametros = {				
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario : $scope.SesionIdUsuario,
								p_IdModulo  : "APV",
								p_AsignacionAutomaticaContrato : "S",
								p_Parametros : [{
									IdRecaudacion		: pRec.seleccionDeposito.IdRecaudacion,
								}],
						};
								
						var IdRecSelecionado = pRec.seleccionDeposito.IdRecaudacion;
						bcsComunes.ventanaOverlay($scope.ventanaId,true);
						
						bcsServiciosAPV.ConciliarDeposito(parametros).$promise.then(function(respuesta){
						  	bcsComunes.setConflictos(util.parseServerResponse(respuesta));
							if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
								var filter = Array();
								filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
								dsDatosDeposito.filter(filter);
								dsDatosDeposito.read().then(function(){
									var filtro = Array();
									filtro.push({ field: "IdRecaudacion", operator: "eq", value: IdRecSelecionado });
									dsDatosEmpleador.filter(filtro);
									dsDatosEmpleador.read().then(function(){
										dsDatosCliente.filter(filtro);
										dsDatosCliente.read().then(function(){
										
										});
							        });
								});
								bcsComunes.ventanaOverlay(idVentana,false);
							}else{
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
						},function(respuesta){
							var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						  	$.each(ErrorDetalle,function(key,value){	
						  		bcsComunes.msg("error","Error","Existen problemas:" + value );
						  	});
							bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					
					}else{
						//Mensaje: Deposito no cumple con estado para conciliar.
						var FolioRecaudacion = "";
						if(pRec.seleccionDeposito.IdRecaudacion && pRec.seleccionDeposito.IdRecaudacion > 0){
							FolioRecaudacion = pRec.seleccionDeposito.IdRecaudacion.toString();
						}
						var params = {IdRecaudacion:FolioRecaudacion}
						bcsComunes.ventanaMsgErr(util.translate($translate,'UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO', params),idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				}else{
					bcsComunes.ventanaMsgErr($translate.instant('UI_SELECCIONE_DEPOSITO'),idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				}
			});
			//pRec.seleccionDeposito.IdRecaudacion;
			//pRec.seleccionDeposito.MontoDeposito;
		};
		
		
		pRec.fnConciliarMasivo = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			let parametros = Array(); 

			let IdVentanamodal = bcsComunes.ventanaAbrir("ConciliacionMasivaPrevired", parametros, true, false, idVentana);
			
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				bcsComunes.ventanaOverlay(idVentana,false);
		    	});				
		};
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
