(function() {
	angular.module('ModuloAPV').controller('ConciliarDepositosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);		
		var IdEmpleadorSeleccionado = 0;
		var IdClienteSeleccionado = 0;
		var primeraCarga = 'SI';
		
		var conDep = this;
		conDep.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		
		
		conDep.filtroBanco = {
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
                    sort 			: { field: "FechaDeposito", dir: "DESC" },
                    pageSize        : 10,
                    filter: {
                        logic: "or",
                        filters: [
                           {field: "IdEstado", operator: "eq", value: "ING"},
                           {field: "IdEstado", operator: "eq", value: "NCO"}
                        ]
                    },
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
													"FolioCartola"			: {type: "number"},
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
		conDep.confGridDeposito = 
		{
    		dataSource		: dsDatosDeposito,
    			columns		: [  {field: "IdRecaudacion"		, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false }
								,{field: "NombreBanco"			, title: "UI_BANCO"				, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaDeposito"		, title: "UI_FECHA_INGRESO"		, width: "90px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoDeposito"		, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "CuentaCorriente"		, title: "UI_CUENTA_CORRIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
    							,{field: "FolioCartola"			, title: "UI_FOLIO_CARTOLA"		, width: "100px", align: "right"	, sortable : true, editable: false }
    							,{field: "NombreTipoDeposito"	, title: "UI_TIPO_DEPOSITO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "Estado"				, title: "UI_ESTADO"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "Origen"				, title: "UI_ORIGEN"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleEmpleador"		, title: "UI_DETALLE_EMPLEADOR"	, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleCliente"		, title: "UI_DETALLE_CLIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 267,
	    		selectable 	: "multiple row",
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
                    filter: {
                        logic: "or",
                        filters: [
                           {field: "IdRecaudacion", operator: "eq", value: null}
                        ]
                    },
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
													"IdBanco"					: {type: "number"},
													"NumeroDocumento"			: {type: "string"},
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
												}
                    						}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosEmpleador = new bcs.DataSource(datosEmpleador);
		conDep.confGridEmpleador = 
		{
    		dataSource		: dsDatosEmpleador,
    			columns		: [  {field: "IdRecaudacionEmpleador"	, title: "UI_FOLIO_EMPLEADOR"	, width: "100px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdRecaudacion"			, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "70px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaIngreso"				, title: "UI_FECHA_INGRESO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "TotalResumen"				, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "right"	, sortable : true, editable: false }
								,{field: "PeriodoPago"				, title: "UI_PERIODO_PAGO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "Origen"					, title: "UI_ORIGEN"			, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "DetalleCliente"			, title: "UI_DETALLE_CLIENTE"	, width: "100px", align: "left"		, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 267,
	    		selectable 	: "multiple row",
	    		autoBind 	: true
		};	
		
		conDep.filtroDepositos = function(){
			var filter = Array(); 
			filter = {
                logic: "or",
                filters: [
                   {field: "IdEstado", operator: "eq", value: "ING"},
                   {field: "IdEstado", operator: "eq", value: "NCO"},
                   {
                	   logic: "and",
                	   filters:[]
                    }
                ]
            };
			if(conDep.depositoSeleccionado.IdRecaudacion !== undefined)
				filter.filters[2].filters.push({ field: "IdRecaudacion"	, operator: "eq", value: Number(conDep.depositoSeleccionado.IdRecaudacion) });
			
			if(conDep.bancoDepositoSeleccionado.IdBanco !== undefined)
				filter.filters[2].filters.push({ field: "IdBanco"		, operator: "eq", value: Number(conDep.bancoDepositoSeleccionado.IdBanco) });
			
			if(conDep.kngFechaDeposito !== undefined)
				filter.filters[2].filters.push({ field: "FechaDeposito"	, operator: "eq", value: conDep.kngFechaDeposito });
			
			if(filter.length > 0)
				dsDatosDeposito.filter(filter);
			else
				dsDatosDeposito.read();
		};
		
		conDep.limpiarDepositos = function(){
			var filter = Array(); 
			
			conDep.depositoSeleccionado = false;
			conDep.bancoDepositoSeleccionado = false;
			conDep.kngFechaDeposito = undefined;
			conDep.bancoDeposito = undefined;
			conDep.folioContrato = undefined;
			conDep.seleccionDeposito = undefined;
			
			var filter = Array(); 

			filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
			dsDatosDeposito.filter(filter);
			dsDatosDeposito.read().then(function(){
				
			});
		};
		
		conDep.filtroEmpleadores = function(){
			var filter = Array(); 
			
			filter.push({ field: "IdRecaudacion", operator: "eq", value: null });

			if(conDep.empleadorSeleccionado.DocumentoIdentidad !== undefined)
				filter.push({ field: "DocumentoIdentidad"	, operator: "eq", value: conDep.empleadorSeleccionado.DocumentoIdentidad });
			
			if(conDep.bancoEmpleadorSeleccionado.IdBanco !== undefined)
				filter.push({ field: "IdBanco"		, operator: "eq", value: Number(conDep.bancoEmpleadorSeleccionado.IdBanco) });
			
			if(conDep.kngFechaEmpleador !== undefined)
				filter.push({ field: "FechaIngreso"	, operator: "eq", value: conDep.kngFechaEmpleador });
			
			if(filter.length > 0)
				dsDatosEmpleador.filter(filter);
			else
				dsDatosEmpleador.read();
		};
		
		conDep.limpiarEmpleador = function(){
			var filter = Array(); 
			
			conDep.bancoEmpleadorSeleccionado = false;
			conDep.empleadorSeleccionado = false;
			conDep.kngFechaEmpleador = undefined;
			conDep.bancoEmpleador = undefined;
			conDep.seleccionEmpleador = undefined;
			conDep.empleador = undefined;
			
			var filter = Array(); 

			filter.push({ field: "IdRecaudacion", operator: "eq", value: null });
			dsDatosEmpleador.filter(filter);
			dsDatosEmpleador.read().then(function(){
				
			});
		};
		
		conDep.totalEmpleadores = function(datosDeposito){
		
			conDep.montoDeposito = datosDeposito.MontoDeposito;
			
			var dsDatosEmpleadorTotales = new bcs.DataSource(datosEmpleador);

			var filter = Array(); 
			
			filter.push({ field: "IdRecaudacion", operator: "eq", value: datosDeposito.IdRecaudacion });

			dsDatosEmpleadorTotales.filter(filter);
			
			var totalEmpleadores = 0;;
			dsDatosEmpleadorTotales.read().then(function(){
				for (i = 0; i < dsDatosEmpleadorTotales.data().length; i++) { 
				    totalEmpleadores += dsDatosEmpleadorTotales.data()[i].MontoDepositoAPV;
				    totalEmpleadores += dsDatosEmpleadorTotales.data()[i].MontoDepositoConvenido;
				}
				
				conDep.montoCalculado = totalEmpleadores;
				
				conDep.diferenciaEmpleador = conDep.montoDeposito - conDep.montoCalculado;
			});
		};
		
		conDep.conciliarDeposito = function(){
			if(conDep.seleccionDeposito === undefined){
				bcsComunes.msg("error","Error","Seleccione Depósito." );
			}else if(conDep.seleccionEmpleador === undefined){
				bcsComunes.msg("error","Error","Seleccione Empleador.");
			}else{
				var parametros = {				
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : "APV",
					p_AsignacionAutomaticaContrato : "S",
					p_Parametros : [{
						IdRecaudacion 			: conDep.seleccionDeposito[0].IdRecaudacion,
						MontoDeposito			: conDep.seleccionDeposito[0].MontoDeposito,
						IdRecaudacionEmpleador	: conDep.seleccionEmpleador[0].IdRecaudacionEmpleador,
						MontoEmpleador			: conDep.seleccionEmpleador[0].MontoDepositoAPV + conDep.seleccionEmpleador[0].MontoDepositoConvenido 
					}],
				};
									
				bcsServiciosAPV.AsignarDepositoEmpleador(parametros).$promise.then(function(respuesta){
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
						
					}
					else{
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
						bcsComunes.ventanaOverlay(idVentana,false);
				    }
				},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
				  	});
					bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				});
			};
		};	
	});
}());