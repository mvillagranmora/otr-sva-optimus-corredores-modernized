(function() {
	angular.module('ModuloAPV').controller('AsignarDepositosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);		
		var IdEmpleadorSeleccionado = 0;
		var IdClienteSeleccionado = 0;
		var primeraCarga = 'SI';
		
		var asigDep = this;
		asigDep.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.IdRecaudacion 	= 0;
		$scope.IdBanco 			= 0;
		$scope.FechaDeposito 	= "";
		$scope.FiltroIdRecaudacion 	= { "field"   	: "IdRecaudacion"	,"operator": "Neq", "value" : $scope.IdRecaudacion };
		$scope.FiltroIdBanco 		= { "field"   	: "IdBanco"			,"operator": "Neq", "value" : $scope.IdBanco };
		$scope.FiltroFechaDeposito 	= { "field"		: "FechaDeposito"	,"operator": "Neq", "value" : $scope.FechaDeposito };
		$scope.FiltroEstadoING		= { "field"		: "IdEstado"		,"operator": "eq", "value" : "ING"};
		$scope.FiltroEstadoNCO		= { "field"		: "IdEstado"		,"operator": "eq", "value" : "NCO"};
		asigDep.filtroBanco = {
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
                    filter			: {
				        				logic : 'or',
				        				filters : [
				        							{field : "IdEstado", operator : "eq", value: "ING"},
				        							{field : "IdEstado", operator : "eq", value: "NCO"}
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
													"CuentaCorriente"		: {type: "string"},
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
		asigDep.confGridDeposito = 
		{
    		dataSource		: dsDatosDeposito,
    			columns		: [  {field: "IdRecaudacion"		, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false }
								,{field: "NombreBanco"			, title: "UI_BANCO"				, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaDeposito"		, title: "UI_FECHA_INGRESO"		, width: "90px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoDeposito"		, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDeposito,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
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
		asigDep.confGridEmpleador = 
		{
    		dataSource		: dsDatosEmpleador,
    			columns		: [  {field: "IdRecaudacionEmpleador"	, title: "UI_FOLIO_EMPLEADOR"	, width: "100px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "IdRecaudacion"			, title: "UI_FOLIO_RECAUDACION"	, width: "100px", align: "right"	, sortable : true, editable: false, hidden: true }
    							,{field: "DocumentoIdentidad"		, title: "UI_RUT"				, width: "70px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreCompleto"			, title: "UI_NOMBRE"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaIngreso"				, title: "UI_FECHA_INGRESO"		, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "TotalResumen"				, title: "UI_TOTAL_DEPOSITO"	, width: "100px", align: "right"	, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoDeposito,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"}
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
		
		asigDep.filtroDepositos = function(){
			var filtros = Array(); 
			if(asigDep.depositoSeleccionado.IdRecaudacion !== undefined)
				filtros.push({ "field"   	: "IdRecaudacion"	,"operator": "eq", "value" : asigDep.depositoSeleccionado.IdRecaudacion });
			if(asigDep.bancoDepositoSeleccionado.IdBanco !== undefined)
				filtros.push({ "field"   	: "IdBanco"			,"operator": "eq", "value" : asigDep.bancoDepositoSeleccionado.IdBanco });
			if(asigDep.kngFechaDeposito !== undefined)
				filtros.push({ "field"		: "FechaDeposito"	,"operator": "eq", "value" : asigDep.kngFechaDeposito });
			
			filtros.push({
				logic : 'or',
				filters : [
							{field : "IdEstado", operator : "eq", value: "ING"},
							{field : "IdEstado", operator : "eq", value: "NCO"}
						  ]
			});
			dsDatosDeposito.filter(filtros);
		};
		
		asigDep.limpiarDepositos = function(){
			var filter = Array(); 
			
			asigDep.depositoSeleccionado = false;
			asigDep.bancoDepositoSeleccionado = false;
			asigDep.kngFechaDeposito = undefined;
			asigDep.bancoDeposito = undefined;
			asigDep.folioContrato = undefined;
			asigDep.seleccionDeposito = undefined;
			asigDep.diferenciaEmpleador = undefined;
			asigDep.montoDeposito = undefined;
			asigDep.montoCalculado = undefined;

			var filter = Array(); 

			filter.push({ field: "IdRecaudacion", operator: "Neq", value: 0 });
			dsDatosDeposito.filter(filter);
			dsDatosDeposito.read().then(function(){
				$scope.IdRecaudacion = "";
				$scope.IdBanco = "";
				$scope.FechaDeposito = "";
			});
		};
		
		asigDep.filtroEmpleadores = function(){
			var filter = Array(); 
			
			filter.push({ field: "IdRecaudacion", operator: "eq", value: null });

			if(asigDep.empleadorSeleccionado.DocumentoIdentidad !== undefined)
				filter.push({ field: "DocumentoIdentidad"	, operator: "eq", value: asigDep.empleadorSeleccionado.DocumentoIdentidad });
			
			if(asigDep.bancoEmpleadorSeleccionado.IdBanco !== undefined)
				filter.push({ field: "IdBanco"		, operator: "eq", value: Number(asigDep.bancoEmpleadorSeleccionado.IdBanco) });
			
			if(asigDep.kngFechaEmpleador !== undefined)
				filter.push({ field: "FechaIngreso"	, operator: "eq", value: asigDep.kngFechaEmpleador });
			
			if(filter.length > 0)
				dsDatosEmpleador.filter(filter);
			else
				dsDatosEmpleador.read();
		};
		
		asigDep.limpiarEmpleador = function(){
			var filter = Array(); 
			
			asigDep.bancoEmpleadorSeleccionado = false;
			asigDep.empleadorSeleccionado = false;
			asigDep.kngFechaEmpleador = undefined;
			asigDep.bancoEmpleador = undefined;
			asigDep.seleccionEmpleador = undefined;
			asigDep.empleador = undefined;
			
			var filter = Array(); 

			filter.push({ field: "IdRecaudacion", operator: "eq", value: null });
			dsDatosEmpleador.filter(filter);
			dsDatosEmpleador.read().then(function(){
				
			});
		};
		
		asigDep.totalEmpleadores = function(datosDeposito){
		
			if(datosDeposito !== undefined){
			
				asigDep.montoDeposito = datosDeposito.MontoDeposito;
				
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
					
					asigDep.montoCalculado = totalEmpleadores;
					
					asigDep.diferenciaEmpleador = asigDep.montoDeposito - asigDep.montoCalculado;
				});
			}
		};
		
		asigDep.asignarDeposito = function(){
			if(asigDep.seleccionDeposito === undefined){
				bcsComunes.msg("error","Error","Seleccione Depósito." );
			}else if(asigDep.seleccionEmpleador === undefined){
				bcsComunes.msg("error","Error","Seleccione Empleador.");
			}else{
				var parametros = {				
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : "APV",
					p_Parametros : [{
						IdRecaudacion 			: asigDep.seleccionDeposito[0].IdRecaudacion,
						MontoDeposito			: asigDep.seleccionDeposito[0].MontoDeposito,
						IdRecaudacionEmpleador	: asigDep.seleccionEmpleador[0].IdRecaudacionEmpleador,
						MontoEmpleador			: asigDep.seleccionEmpleador[0].MontoDepositoAPV + asigDep.seleccionEmpleador[0].MontoDepositoConvenido 
					}],
				};
									
				bcsServiciosAPV.AsignarDepositoEmpleador(parametros).$promise.then(function(respuesta){
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						if(respuesta.P_OUT_MENSAJES[0].codigo === "OK"){
							bcsComunes.ventanaMsg({ mensaje: respuesta.P_OUT_MENSAJES[0].descripcion}, idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
							
							asigDep.totalEmpleadores(asigDep.seleccionDeposito[0]);
							asigDep.limpiarEmpleador();
							asigDep.limpiarDepositos();
						}else{
							bcsComunes.ventanaMsgErr(respuesta.P_OUT_MENSAJES[0].descripcion, idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						}
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