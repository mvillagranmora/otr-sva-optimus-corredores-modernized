(function() {
	angular.module('ModuloAPV').controller('GestionSAAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConsultasAPV,bcsServiciosAPV,$http,$rootScope)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		var gsaapv = this;
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kOptFechaNacimiento = {};
		$scope.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		//$scope.autoBindGrilla = false;
		var gridContratoSAAPV = null;
		var empleadores = [];

/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
		gsaapv.oConfiguracionPersonaNatural = {
			filtros : {
				Tipo	: {field : "CodigoTipoPersona",	operator : "eq", value : "N"}
			}
		};
		
		gsaapv.oConfiguracionPersonaJuridica = {
			filtros : {
				Tipo	: {field : "CodigoTipoPersona",	operator : "eq", value : "J"}
			}
		};
		
		gsaapv.FiltroProductoAgrupador =  {
				filtros: {
					IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
				}	
		};
		
		gsaapv.FiltroRegimenAgrupador =  {
				filtros: {
					IdProductoAgrupador : {"field" : "IdProductoAgrupador", "operator" : "eq", "value" : 0}
				}	
		};	
		
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		
		var datosSAAPV = 
		{
			type : "odata",
			definition : 
			{
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
				pageSize        : 25,
				transport		: 
				{
					read: 
					{
						url 		: bcsConsultasAPV('ConsultarGestionContratosSAAPV'),
						dataType	: "json"
					}
				},
                schema 			:
				{
					model: 
                    {
						fields : 
						{													
							"IdContratoSAAPV"				: {type: "number"},
							"FolioContratoSAAPV" 			: {type: "string"},
							"IdPlan" 						: {type: "number"},
							"NombrePlan" 					: {type: "string"},
							"IdPortafolio"					: {type: "number"},
							"CodigoPortafolio"				: {type: "string"},
							"FechaSuscripcion" 				: {type: "sysdate"},
							"FechaCreacion" 				: {type: "sysdate"},
							"FechaAprobacion" 				: {type: "sysdate"},
							"FechaNotificacion" 			: {type: "sysdate"},
							"FechaRecepcion"				: {type: "sysdate"},
							"CodigoTipoSolicitud"			: {type: "string"},
							"Folio"							: {type: "string"},
							
							/*Cliente*/
							"IdPersona" 					: {type: "number"},
							"RutCliente"					: {type: "string"},
							"NombrePersona"					: {type: "string"},
							"Tipo"							: {type: "string"},
							"IdCliente"						: {type: "number"},
							"FechaNacimientoCliente"		: {type: "sysdate"},
							"EstadoCivil"					: {type: "string"},
							"IdTipoGenero"					: {type: "string"},
							"IdCiudadCliente"				: {type: "number"},
							"IdComunaCliente"				: {type: "number"},
							"DireccionCliente"				: {type: "string"},													
							"TelefonoCliente"				: {type: "string"},
							"TelefonoComercialCliente"		: {type: "string"},
							"TelefonoCelularCliente"		: {type: "string"},
							"EmailCliente"					: {type: "string"},	
							
							/*Empleador*/
							"IdEmpleador" 					: {type: "number"},
							"RutEmpleador" 					: {type: "string"},
							"NombreEmpleador" 				: {type: "string"},												
							"DireccionComercialEmpleador"	: {type: "string"},
							"TelefonoEmpleador"				: {type: "string"},
							"NombreEncargado"				: {type: "string"},
							"EmailEncargado"				: {type: "string"},
							"TelefonoEncargado"				: {type: "string"},
							
							"IdMedioRecaudacionTrabajador"	: {type: "number"},
							"IdEntidadRecaudadora"			: {type: "number"},
							"CodigoPrevision"				: {type: "number"},
							"Pensionado"		        	: {type: "string"},
							"Trabajador"					: {type: "string"},
							"OrigenAhorro"				    : {type: "string"}, //BDH se quitan diferencias de CV y DC 
							"MontoPesos" 				    : {type: "number"}, //BDH se quitan diferencias de CV y DC 
							"MontoUF" 					    : {type: "number"}, //BDH se quitan diferencias de CV y DC 
							"PorcentajeRegimenImponible"	: {type: "number"}, //BDH se quitan diferencias de CV y DC 
							"Pago" 							: {type: "string"},
							"PeriodoInicio" 				: {type: "string"},
							"PeriodoTermino"				: {type: "string"},
							"RegimenTributario" 			: {type: "string"},
							"FormaDePago"					: {type: "string"},
							"IdMedioRecaudacionFormaPago"   : {type: "number"},
							"CodigoMedioRecaudacion"		: {type: "string"},
							"NombreMedioRecaudacion"		: {type: "string"},
							"DiaDescuento" 					: {type: "number"},
							"Institucion" 					: {type: "string"},
							"ComisionTransferencia" 		: {type: "number"},
							"NombreEntidadOrigen" 			: {type: "string"},
							"RutEntidadOrigen"				: {type: "string"},
							"TipoDeAhorro" 					: {type: "string"},
							"FondoDeOrigen" 				: {type: "string"},
							"TipoTraspaso"					: {type: "string"},
							"MontoTraspaso" 				: {type: "number"},
							"UFTraspaso"					: {type: "number"},
							"PorcentajeTraspaso"			: {type: "number"},
							"IdEstadoEnvioNotificacion"		: {type: "string"},
							"IdEstadoSolicitud"				: {type: "string"},
							"IdEstado"						: {type: "string"},
							"IdProductoAgrupador"			: {type: "number"},
							"Producto"						: {type: "string"},
							"EstadoNotificacion"			: {type: "string"},
							"EstadoSolicitud"				: {type: "string"},
							"Estado"						: {type: "string"},
							"EstadoNuevo"					: {type: "string"},
							"IdEstadoNuevo"					: {type: "string"},
							"EstadoPAC"					    : {type: "string"},
							"Canal"							: {type: "string"},
							"Observacion"					: {type: "string"},
							"Motivo"						: {type: "string"}
						}
					}
				}
    		}
		};
		
		var datosEventoAutorizacion = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "FechaHoraRegistro", dir: "DESC" },
                pageSize        : 10,
                transport		: {
					read: {
						url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1396",
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdEmpresa				: {type: "number"},
							IdEvento				: {type: "number"},
							Fecha					: {type: "sysdate"},
							HoraRegistro			: {type: "sysdate"},
							HoraVigencia			: {type: "sysdate"},
							IdEstado				: {type: "string"},
							NombreEstado			: {type: "string"},
							Autorizacion			: {type: "string"},
							Glosa					: {type: "string"},
							IdGrupo					: {type: "number"},
							IdCliente				: {type: "number"},
							NumeroOperacionConcepto	: {type: "string"}
						}
					}
	            }
			}
		};

/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		
		var dsDatosSAAPV = new bcs.DataSource(datosSAAPV);
		gsaapv.confBasicaSAAPV = 
		{
    		dataSource	: dsDatosSAAPV,
    		columns		: 
			[
				 {field: "IdContratoSAAPV"				, title: "UI_ID_CONTRATO"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true}
				,{field: "FolioContratoSAAPV"			, title: "UI_FOLIO_CONTRATO"		, width: "80px", align: "left"		, sortable : true, editable: false }
				,{field: "IdPlan"						, title: "UI_ID_PLAN"				, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "NombrePlan"					, title: "UI_NOMBRE_PLAN"			, width: "80px", align: "left"		, sortable : true, editable: false }
				,{field: "Folio"						, title: "UI_FOLIO"					, width: "110px", align: "right"	, sortable : true, editable: false }
				,{field: "IdPersona"					, title: "UI_ID_PERSONA"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "RutCliente"					, title: "UI_DOCUMENTO_IDENTIDAD"	, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "NombrePersona"				, title: "UI_NOMBRE_PERSONA"		, width: "180px", align: "left"		, sortable : true, editable: false }
				,{field: "Tipo"							, title: "UI_TIPO_PERSONA"			, width: "80px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				
				,{field: "IdPortafolio"					, title: "UI_ID_PORTAFOLIO"			, width: "80px"	, align: "left"		, sortable : true, editable: false }
				,{field: "Producto" 					, title: "UI_CODIGO_PORTAFOLIO"		, width: "180px", align: "left"		, sortable : true, editable: false }
				
				,{field: "FechaNacimientoCliente"		, title: "UI_FECHA_NACIMIENTO"		, width: "80px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "EstadoCivil"					, title: "UI_ESTADO_CIVIL"			, width: "80px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "IdTipoGenero"					, title: "UI_TIPO_GENERO"			, width: "80px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "DireccionCliente"				, title: "UI_DIRECCION_CLIENTE"		, width: "200px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "TelefonoCliente"				, title: "UI_TELEFONO"				, width: "100px", align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "TelefonoComercialCliente"		, title: "UI_TELEFONO_COMERCIAL"	, width: "100px", align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "TelefonoCelularCliente"		, title: "UI_TELEFONO_CELULAR"		, width: "100px", align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "EmailCliente"					, title: "UI_EMAIL_CLIENTE"			, width: "200px", align: "center"	, sortable : true, editable: false, hidden: true }
						
				,{field: "IdEmpleador"					, title: "UI_ID_EMPLEADOR"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "RutEmpleador"					, title: "UI_RUT_EMPLEADOR"			, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "NombreEmpleador"				, title: "UI_NOMBRE_EMPLEADOR"		, width: "200px", align: "left"		, sortable : true, editable: false }
				,{field: "DireccionComercialEmpleador"	, title: "UI_DIRECCION_EMPLEADOR"	, width: "200px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "TelefonoEmpleador"			, title: "UI_TELEFONO"				, width: "100px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "NombreEncargado"				, title: "UI_NOMBRE_ENCARGADO"		, width: "200px", align: "left"		, sortable : true, editable: false }
				,{field: "EmailEncargado"				, title: "UI_EMAIL_EMPLEADOR"		, width: "200px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "TelefonoEncargado"			, title: "UI_TELEFONO_EMPLEADOR"	, width: "100px", align: "left"		, sortable : true, editable: false, hidden: true }
				
				,{field: "FechaSuscripcion"				, title: "UI_FECHA_SUSCRIPCION"		, width: "90px", align: "center"	, sortable : true, editable: false }
				,{field: "FechaCreacion"				, title: "UI_FECHA_CREACION"		, width: "90px", align: "center"	, sortable : true, editable: false }
				,{field: "FechaAprobacion"				, title: "UI_FECHA_APROBACION"		, width: "90px", align: "center"	, sortable : true, editable: false }
				,{field: "FechaNotificacion"			, title: "UI_FECHA_NOTIFICACION"	, width: "90px", align: "center"	, sortable : true, editable: false }
				,{field: "FechaRecepcion"				, title: "UI_FECHA_RECEPCION"		, width: "90px", align: "center"	, sortable : true, editable: false }
				,{field: "CodigoPrevision"				, title: "UI_CODIGO_PREVISION"		, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "Pensionado"					, title: "UI_PENSIONADO"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "Trabajador"					, title: "UI_TRABAJADOR"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "OrigenAhorro"  				, title: "UI_ORIGEN_AHORRO"			, width: "110px", align: "left"		, sortable : true, editable: false } //BDH se quitan diferencias de CV y DC 
				,{field: "MontoPesos"	  	    		, title: "UI_MONTO_AHORRO"			, width: "110px", align: "left"		, sortable : true, editable: false, format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'',''); }} //EOR 			
				,{field: "MontoUF"			 			, title: "UI_UF"					, width: "80px", align: "left"		, sortable : true, editable: false } //BDH se quitan diferencias de CV y DC 
				,{field: "PorcentajeRegimenImponible"	, title: "UI_PORCENTAJE_REGIMEN"	, width: "110px", align: "left"		, sortable : true, editable: false } //BDH se quitan diferencias de CV y DC 
				,{field: "Pago"							, title: "UI_PAGO"					, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "PeriodoInicio"				, title: "UI_PERIODO_INICIO"		, width: "80px", align: "center"	, sortable : true, editable: false }
				,{field: "PeriodoTermino"				, title: "UI_PERIODO_TERMINO"		, width: "80px", align: "center"	, sortable : true, editable: false }
				,{field: "RegimenTributario"			, title: "UI_REGIMEN_TRIBUTARIO"	, width: "110px", align: "center"	, sortable : true, editable: false }
				,{field: "FormaDePago"					, title: "UI_FORMA_PAGO"			, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "IdMedioRecaudacionFormaPago"	, title: "UI_ID_MEDIO_RECAUDACION"	, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "NombreMedioRecaudacion"		, title: "UI_NOMBRE_MEDIO_RECAUDACION"	, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "DiaDescuento"					, title: "UI_DIA_DESCUENTO"			, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "Institucion"					, title: "UI_INSTITUCION"			, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "ComisionTransferencia"		, title: "UI_COMISION_TRANSFERENCIA", width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "CodigoTipoSolicitud"			, title: "UI_TIPO_SOLICITUD"		, width: "110px", align: "left"		, sortable : true, editable: false } 
				,{field: "RutEntidadOrigen"				, title: "UI_RUT_ENTIDAD_ORIGEN"	, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "NombreEntidadOrigen"			, title: "UI_NOMBRE_ENTIDAD_ORIGEN"	, width: "180px", align: "left"		, sortable : true, editable: false }
				,{field: "TipoDeAhorro"					, title: "UI_TIPO_AHORRO"			, width: "80px", align: "center"	, sortable : true, editable: false }
				,{field: "FondoDeOrigen"				, title: "UI_FONDO_ORIGEN"			, width: "110px", align: "center"	, sortable : true, editable: false }
				,{field: "TipoTraspaso"					, title: "UI_TIPO_TRASPASO"			, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "MontoTraspaso"				, title: "UI_MONTO_TRASPASO"		, width: "110px", align: "left"		, sortable : true, editable: false, format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'',''); }} //EOR
				,{field: "UFTraspaso"					, title: "UI_UF_TRASPASO"			, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "PorcentajeTraspaso"			, title: "UI_PORCENTAJE_TRASPASO"	, width: "110px", align: "left"		, sortable : true, editable: false }
				,{field: "IdEstadoEnvioNotificacion"	, title: "UI_ESTADO_NOTIFICACION"	, width: "70px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "IdEstadoSolicitud"			, title: "UI_ESTADO_SOLICITUD"		, width: "70px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "IdEstado"						, title: "UI_ESTADO"				, width: "70px"	, align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "EstadoNotificacion"			, title: "UI_ESTADO_NOTIFICACION"	, width: "100px", align: "center"	, sortable : true, editable: false }
				//,{field: "EstadoSolicitud"				, title: "UI_ESTADO_SOLICITUD"		, width: "100px", align: "center"	, sortable : true, editable: false }
				//,{field: "Estado"						, title: "UI_ESTADO"				, width: "100px", align: "center"	, sortable : true, editable: false }
				,{field: "EstadoSolicitud"				, title: "UI_ESTADO_SOLICITUD"		, width: "100px", align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "Estado"						, title: "UI_ESTADO"				, width: "100px", align: "center"	, sortable : true, editable: false, hidden: true }
				,{field: "EstadoNuevo"					, title: "UI_ESTADO"				, width: "100px", align: "center"	, sortable : true, editable: false }
				,{field: "EstadoPAC"					, title: "UI_ESTADO_PAC"			, width: "100px", align: "center"	, sortable : true, editable: false }
				,{field: "Canal"						, title: "UI_CANAL"					, width: "100px", align: "center"	, sortable : true, editable: false }
				,{field: "Observacion"					, title: "UI_OBSERVACION"			, width: "100px", align: "center"	, sortable : true, editable: false }
				,{field: "Motivo"						, title: "UI_MOTIVO"				, width: "100px", align: "center"	, sortable : true, editable: false }
				
				],
			sortable		: true,
	    	pageable		: true,
	    	filterable		: true,
	    	navigatable		: true,
	    	height			: 420,
	    	userSettings	: true,
	    	selectable 		: "multiple row",
	    	autoBind 		: true,
	    	gridCreated : function(grid){
				gridContratoSAAPV = grid;
			}
		};	
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		gsaapv.ConsultarGestionContratosSAAPV = function()
		{

			var _filter = Array();
			
			if(gsaapv.dataPersona.Seleccionado.IdPersona !== undefined)
				_filter.push({ field: "IdPersona"		, operator: "eq", value: Number(gsaapv.dataPersona.Seleccionado.IdPersona) });
			
			if(gsaapv.dataPersona.SeleccionadoEmpleador.IdPersona !== undefined)
				_filter.push({ field: "IdEmpleador"		, operator: "eq", value: Number(gsaapv.dataPersona.SeleccionadoEmpleador.IdPersona) });
			
			//if(gsaapv.folio !== undefined)
			//	_filter.push({ field: "FolioContratoSAAPV"	, operator: "eq", value: gsaapv.folio });
			
			if(gsaapv.SeleccionadoEstado.Codigo !== undefined)
				_filter.push({ field: "IdEstadoNuevo"		, operator: "eq", value: gsaapv.SeleccionadoEstado.Codigo });

			if(gsaapv.fechaSuscripcion !== undefined)
				_filter.push({ field: "FechaSuscripcion"	, operator: "eq", value: gsaapv.fechaSuscripcion });
			
			if(gsaapv.fechaCreacion !== undefined)
				_filter.push({ field: "FechaCreacion"	, operator: "eq", value: gsaapv.fechaCreacion });
			
			if(gsaapv.fechaAprobacion !== undefined)
				_filter.push({ field: "FechaAprobacion"	, operator: "eq", value: gsaapv.fechaAprobacion });
			
			if(gsaapv.fechaNotificacion !== undefined)
				_filter.push({ field: "FechaNotificacion", operator: "eq", value: gsaapv.fechaNotificacion });
			
			if(gsaapv.fechaRecepcion !== undefined)
				_filter.push({ field: "FechaRecepcion"	, operator: "eq", value: gsaapv.fechaRecepcion });
			
			if(gsaapv.NombrePlanAhorroSeleccionado.IdPlanTipoInversion !== undefined && gsaapv.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion !== " TODOS")
				_filter.push({ field: "IdPlan"	, operator: "eq", value: Number(gsaapv.NombrePlanAhorroSeleccionado.IdPlanTipoInversion) });
			
			if(gsaapv.ProductoAgrupadorSeleccionado.IdProductoAgrupador !== undefined && gsaapv.ProductoAgrupadorSeleccionado.NombreProductoAgrupador !== " TODOS")
				_filter.push({ field: "IdProductoAgrupador"	, operator: "eq", value: Number(gsaapv.ProductoAgrupadorSeleccionado.IdProductoAgrupador) });
			
			if(gsaapv.RegimenAgrupadorSeleccionado.Codigo !== undefined && gsaapv.RegimenAgrupadorSeleccionado.Nombre !== " TODOS")
				_filter.push({ field: "RegimenTributario"	, operator: "eq", value: gsaapv.RegimenAgrupadorSeleccionado.Codigo});
						
			var datosContrato = {
					p_FolioContratoSAAPV : gsaapv.folio === undefined || gsaapv.folio === false || gsaapv.folio === '' ? null : gsaapv.folio
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			//-------------------------------------------------------------------------------------------------------------------------------------
			//-------------------------------------------------------------------------------------------------------------------------------------
			//CAMBIO PARA PERFORMANCE HMR
			//-------------------------------------------------------------------------------------------------------------------------------------
			//-------------------------------------------------------------------------------------------------------------------------------------
			if (gsaapv.folio === null || gsaapv.folio === undefined || gsaapv.folio === false || gsaapv.folio === '')
			{
				dsDatosSAAPV.filter(_filter);
				dsDatosSAAPV.read().then(function(){
					//gridContratoSAAPV.data(ConsultaGestionContratos.data());
					gridContratoSAAPV.refresh;
				});
				/*var consultaquery = new bcs.DataSource(datosSAAPV);
				consultaquery.query({ filter: _filter }).then(function()
				{
					if(consultaquery.view().length > 0)
					{
						gridContratoSAAPV.dataSource.data(consultaquery.view());
						//gridContratoSAAPV = consultaquery.view();
						gridContratoSAAPV.refresh;
					}
				});*/
				//gridContratoSAAPV = gsaapv.confBasicaSAAPV.dataSource;
				//gridContratoSAAPV.refresh;
			}
			else
			{
				var Resultado = bcsServiciosAPV.ConsultarGestionContratosSAAPV(datosContrato).$promise;
				Resultado.then(function(result) {
					if(result.P_CONTRATOS.length > 0){
						var ConsultaContratos = [];
						for(var ContratoIndex in result.P_CONTRATOS){
							var Contrato = {
									"IdContratoSAAPV"				: result.P_CONTRATOS[ContratoIndex].IdContratoSAAPV,
									"FolioContratoSAAPV" 			: result.P_CONTRATOS[ContratoIndex].FolioContratoSAAPV,
									"IdPlan" 						: result.P_CONTRATOS[ContratoIndex].IdPlan,
									"NombrePlan" 					: result.P_CONTRATOS[ContratoIndex].NombrePlan,
									"IdPortafolio"					: result.P_CONTRATOS[ContratoIndex].IdPortafolio,
									"CodigoPortafolio"				: result.P_CONTRATOS[ContratoIndex].CodigoPortafolio,
									"FechaSuscripcion" 				: result.P_CONTRATOS[ContratoIndex].FechaSuscripcion,
									"FechaCreacion" 				: result.P_CONTRATOS[ContratoIndex].FechaCreacion,
									"FechaAprobacion" 				: result.P_CONTRATOS[ContratoIndex].FechaAprobacion,
									"FechaNotificacion" 			: result.P_CONTRATOS[ContratoIndex].FechaNotificacion,
									"FechaRecepcion"				: result.P_CONTRATOS[ContratoIndex].FechaRecepcion,
									"Folio"							: result.P_CONTRATOS[ContratoIndex].Folio,

									/*Cliente*/                      
									"IdPersona" 					: result.P_CONTRATOS[ContratoIndex].IdPersona,
									"RutCliente"					: result.P_CONTRATOS[ContratoIndex].RutCliente,
									"NombrePersona"					: result.P_CONTRATOS[ContratoIndex].NombrePersona,
									"Tipo"							: result.P_CONTRATOS[ContratoIndex].Tipo,
									"IdCliente"						: result.P_CONTRATOS[ContratoIndex].IdCliente,
									"FechaNacimientoCliente"		: result.P_CONTRATOS[ContratoIndex].FechaNacimientoCliente,
									"EstadoCivil"					: result.P_CONTRATOS[ContratoIndex].EstadoCivil,
									"IdTipoGenero"					: result.P_CONTRATOS[ContratoIndex].IdTipoGenero,
									"IdCiudadCliente"				: result.P_CONTRATOS[ContratoIndex].IdCiudadCliente,
									"IdComunaCliente"				: result.P_CONTRATOS[ContratoIndex].IdComunaCliente,
									"DireccionCliente"				: result.P_CONTRATOS[ContratoIndex].DireccionCliente,
									"TelefonoCliente"				: result.P_CONTRATOS[ContratoIndex].TelefonoCliente,
									"TelefonoComercialCliente"		: result.P_CONTRATOS[ContratoIndex].TelefonoComercialCliente,
									"TelefonoCelularCliente"		: result.P_CONTRATOS[ContratoIndex].TelefonoCelularCliente,
									"EmailCliente"					: result.P_CONTRATOS[ContratoIndex].EmailCliente,

									/*Empleador*/                    
									"IdEmpleador" 					: result.P_CONTRATOS[ContratoIndex].IdEmpleador,
									"RutEmpleador" 					: result.P_CONTRATOS[ContratoIndex].RutEmpleador,
									"NombreEmpleador" 				: result.P_CONTRATOS[ContratoIndex].NombreEmpleador,
									"DireccionComercialEmpleador"	: result.P_CONTRATOS[ContratoIndex].DireccionComercialEmpleador,
									"TelefonoEmpleador"				: result.P_CONTRATOS[ContratoIndex].TelefonoEmpleador,
									"NombreEncargado"				: result.P_CONTRATOS[ContratoIndex].NombreEncargado,
									"EmailEncargado"				: result.P_CONTRATOS[ContratoIndex].EmailEncargado,
									"TelefonoEncargado"				: result.P_CONTRATOS[ContratoIndex].TelefonoEncargado,

									"IdMedioRecaudacionTrabajador"	: result.P_CONTRATOS[ContratoIndex].IdMedioRecaudacionTrabajador,
									"IdEntidadRecaudadora"			: result.P_CONTRATOS[ContratoIndex].IdEntidadRecaudadora,
									"CodigoPrevision"				: result.P_CONTRATOS[ContratoIndex].CodigoPrevision,
									"Pensionado"		        	: result.P_CONTRATOS[ContratoIndex].Pensionado,
									"Trabajador"					: result.P_CONTRATOS[ContratoIndex].Trabajador,
									"OrigenAhorro"				    : result.P_CONTRATOS[ContratoIndex].OrigenAhorro,
									"MontoPesos" 				    : result.P_CONTRATOS[ContratoIndex].MontoPesos,
									"MontoUF" 					    : result.P_CONTRATOS[ContratoIndex].MontoUF,
									"PorcentajeRegimenImponible"	: result.P_CONTRATOS[ContratoIndex].PorcentajeRegimenImponible,
									"Pago" 							: result.P_CONTRATOS[ContratoIndex].Pago,
									"PeriodoInicio" 				: result.P_CONTRATOS[ContratoIndex].PeriodoInicio,
									"PeriodoTermino"				: result.P_CONTRATOS[ContratoIndex].PeriodoTermino,
									"RegimenTributario" 			: result.P_CONTRATOS[ContratoIndex].RegimenTributario,
									"FormaDePago"					: result.P_CONTRATOS[ContratoIndex].FormaDePago,
									"IdMedioRecaudacionFormaPago"   : result.P_CONTRATOS[ContratoIndex].IdMedioRecaudacionFormaPago,
									"CodigoMedioRecaudacion"		: result.P_CONTRATOS[ContratoIndex].CodigoMedioRecaudacion,
									"NombreMedioRecaudacion"		: result.P_CONTRATOS[ContratoIndex].NombreMedioRecaudacion,
									"DiaDescuento" 					: result.P_CONTRATOS[ContratoIndex].DiaDescuento,
									"Institucion" 					: result.P_CONTRATOS[ContratoIndex].Institucion,
									"ComisionTransferencia" 		: result.P_CONTRATOS[ContratoIndex].ComisionTransferencia,
									"CodigoTipoSolicitud"           : result.P_CONTRATOS[ContratoIndex].CodigoTipoSolicitud,
									"NombreEntidadOrigen" 			: result.P_CONTRATOS[ContratoIndex].NombreEntidadOrigen,
									"RutEntidadOrigen"				: result.P_CONTRATOS[ContratoIndex].RutEntidadOrigen,
									"TipoDeAhorro" 					: result.P_CONTRATOS[ContratoIndex].TipoDeAhorro,
									"FondoDeOrigen" 				: result.P_CONTRATOS[ContratoIndex].FondoDeOrigen,
									"TipoTraspaso"					: result.P_CONTRATOS[ContratoIndex].TipoTraspaso,
									"MontoTraspaso" 				: result.P_CONTRATOS[ContratoIndex].MontoTraspaso,
									"UFTraspaso"					: result.P_CONTRATOS[ContratoIndex].UFTraspaso,
									"PorcentajeTraspaso"			: result.P_CONTRATOS[ContratoIndex].PorcentajeTraspaso,
									"IdEstadoEnvioNotificacion"		: result.P_CONTRATOS[ContratoIndex].IdEstadoEnvioNotificacion,
									"IdEstadoSolicitud"				: result.P_CONTRATOS[ContratoIndex].IdEstadoSolicitud,
									"IdEstado"						: result.P_CONTRATOS[ContratoIndex].IdEstado,
									"IdProductoAgrupador"			: result.P_CONTRATOS[ContratoIndex].IdProductoAgrupador,
									"Producto"						: result.P_CONTRATOS[ContratoIndex].Producto,
									"EstadoNotificacion"			: result.P_CONTRATOS[ContratoIndex].EstadoNotificacion,
									"EstadoSolicitud"				: result.P_CONTRATOS[ContratoIndex].EstadoSolicitud,
									"Estado"						: result.P_CONTRATOS[ContratoIndex].Estado,
									"EstadoNuevo"					: result.P_CONTRATOS[ContratoIndex].EstadoNuevo,
									"IdEstadoNuevo"					: result.P_CONTRATOS[ContratoIndex].IdEstadoNuevo
							};
							ConsultaContratos.push(Contrato);
						};
						gsaapv.confBasicaSAAPV.dataSource.data(ConsultaContratos);
						if(_filter.length > 0)
							gsaapv.confBasicaSAAPV.dataSource.filter(_filter);
					}
					else{
						gsaapv.confBasicaSAAPV.dataSource.data({});

					}
					gridContratoSAAPV = gsaapv.confBasicaSAAPV.dataSource;
					gridContratoSAAPV.refresh;
					
				});
				
			}
			bcsComunes.ventanaOverlay(idVentana,false);
			//-------------------------------------------------------------------------------------------------------------------------------------
			//-------------------------------------------------------------------------------------------------------------------------------------			
			//-------------------------------------------------------------------------------------------------------------------------------------			
			
			/*

			var Resultado = bcsServiciosAPV.ConsultarGestionContratosSAAPV(datosContrato).$promise;
			Resultado.then(function(result) {
				if(result.P_CONTRATOS.length > 0){
					var ConsultaContratos = [];
					for(var ContratoIndex in result.P_CONTRATOS){
						var Contrato = {
								"IdContratoSAAPV"				: result.P_CONTRATOS[ContratoIndex].IdContratoSAAPV,
								"FolioContratoSAAPV" 			: result.P_CONTRATOS[ContratoIndex].FolioContratoSAAPV,
								"IdPlan" 						: result.P_CONTRATOS[ContratoIndex].IdPlan,
								"NombrePlan" 					: result.P_CONTRATOS[ContratoIndex].NombrePlan,
								"IdPortafolio"					: result.P_CONTRATOS[ContratoIndex].IdPortafolio,
								"CodigoPortafolio"				: result.P_CONTRATOS[ContratoIndex].CodigoPortafolio,
								"FechaSuscripcion" 				: result.P_CONTRATOS[ContratoIndex].FechaSuscripcion,
								"FechaCreacion" 				: result.P_CONTRATOS[ContratoIndex].FechaCreacion,
								"FechaAprobacion" 				: result.P_CONTRATOS[ContratoIndex].FechaAprobacion,
								"FechaNotificacion" 			: result.P_CONTRATOS[ContratoIndex].FechaNotificacion,
								"FechaRecepcion"				: result.P_CONTRATOS[ContratoIndex].FechaRecepcion,
								"Folio"							: result.P_CONTRATOS[ContratoIndex].Folio,

								//Cliente                      
								"IdPersona" 					: result.P_CONTRATOS[ContratoIndex].IdPersona,
								"RutCliente"					: result.P_CONTRATOS[ContratoIndex].RutCliente,
								"NombrePersona"					: result.P_CONTRATOS[ContratoIndex].NombrePersona,
								"Tipo"							: result.P_CONTRATOS[ContratoIndex].Tipo,
								"IdCliente"						: result.P_CONTRATOS[ContratoIndex].IdCliente,
								"FechaNacimientoCliente"		: result.P_CONTRATOS[ContratoIndex].FechaNacimientoCliente,
								"EstadoCivil"					: result.P_CONTRATOS[ContratoIndex].EstadoCivil,
								"IdTipoGenero"					: result.P_CONTRATOS[ContratoIndex].IdTipoGenero,
								"IdCiudadCliente"				: result.P_CONTRATOS[ContratoIndex].IdCiudadCliente,
								"IdComunaCliente"				: result.P_CONTRATOS[ContratoIndex].IdComunaCliente,
								"DireccionCliente"				: result.P_CONTRATOS[ContratoIndex].DireccionCliente,
								"TelefonoCliente"				: result.P_CONTRATOS[ContratoIndex].TelefonoCliente,
								"TelefonoComercialCliente"		: result.P_CONTRATOS[ContratoIndex].TelefonoComercialCliente,
								"TelefonoCelularCliente"		: result.P_CONTRATOS[ContratoIndex].TelefonoCelularCliente,
								"EmailCliente"					: result.P_CONTRATOS[ContratoIndex].EmailCliente,

								//Empleador                    
								"IdEmpleador" 					: result.P_CONTRATOS[ContratoIndex].IdEmpleador,
								"RutEmpleador" 					: result.P_CONTRATOS[ContratoIndex].RutEmpleador,
								"NombreEmpleador" 				: result.P_CONTRATOS[ContratoIndex].NombreEmpleador,
								"DireccionComercialEmpleador"	: result.P_CONTRATOS[ContratoIndex].DireccionComercialEmpleador,
								"TelefonoEmpleador"				: result.P_CONTRATOS[ContratoIndex].TelefonoEmpleador,
								"NombreEncargado"				: result.P_CONTRATOS[ContratoIndex].NombreEncargado,
								"EmailEncargado"				: result.P_CONTRATOS[ContratoIndex].EmailEncargado,
								"TelefonoEncargado"				: result.P_CONTRATOS[ContratoIndex].TelefonoEncargado,

								"IdMedioRecaudacionTrabajador"	: result.P_CONTRATOS[ContratoIndex].IdMedioRecaudacionTrabajador,
								"IdEntidadRecaudadora"			: result.P_CONTRATOS[ContratoIndex].IdEntidadRecaudadora,
								"CodigoPrevision"				: result.P_CONTRATOS[ContratoIndex].CodigoPrevision,
								"Pensionado"		        	: result.P_CONTRATOS[ContratoIndex].Pensionado,
								"Trabajador"					: result.P_CONTRATOS[ContratoIndex].Trabajador,
								"OrigenAhorro"				    : result.P_CONTRATOS[ContratoIndex].OrigenAhorro,
								"MontoPesos" 				    : result.P_CONTRATOS[ContratoIndex].MontoPesos,
								"MontoUF" 					    : result.P_CONTRATOS[ContratoIndex].MontoUF,
								"PorcentajeRegimenImponible"	: result.P_CONTRATOS[ContratoIndex].PorcentajeRegimenImponible,
								"Pago" 							: result.P_CONTRATOS[ContratoIndex].Pago,
								"PeriodoInicio" 				: result.P_CONTRATOS[ContratoIndex].PeriodoInicio,
								"PeriodoTermino"				: result.P_CONTRATOS[ContratoIndex].PeriodoTermino,
								"RegimenTributario" 			: result.P_CONTRATOS[ContratoIndex].RegimenTributario,
								"FormaDePago"					: result.P_CONTRATOS[ContratoIndex].FormaDePago,
								"IdMedioRecaudacionFormaPago"   : result.P_CONTRATOS[ContratoIndex].IdMedioRecaudacionFormaPago,
								"CodigoMedioRecaudacion"		: result.P_CONTRATOS[ContratoIndex].CodigoMedioRecaudacion,
								"NombreMedioRecaudacion"		: result.P_CONTRATOS[ContratoIndex].NombreMedioRecaudacion,
								"DiaDescuento" 					: result.P_CONTRATOS[ContratoIndex].DiaDescuento,
								"Institucion" 					: result.P_CONTRATOS[ContratoIndex].Institucion,
								"ComisionTransferencia" 		: result.P_CONTRATOS[ContratoIndex].ComisionTransferencia,
								"CodigoTipoSolicitud"           : result.P_CONTRATOS[ContratoIndex].CodigoTipoSolicitud,
								"NombreEntidadOrigen" 			: result.P_CONTRATOS[ContratoIndex].NombreEntidadOrigen,
								"RutEntidadOrigen"				: result.P_CONTRATOS[ContratoIndex].RutEntidadOrigen,
								"TipoDeAhorro" 					: result.P_CONTRATOS[ContratoIndex].TipoDeAhorro,
								"FondoDeOrigen" 				: result.P_CONTRATOS[ContratoIndex].FondoDeOrigen,
								"TipoTraspaso"					: result.P_CONTRATOS[ContratoIndex].TipoTraspaso,
								"MontoTraspaso" 				: result.P_CONTRATOS[ContratoIndex].MontoTraspaso,
								"UFTraspaso"					: result.P_CONTRATOS[ContratoIndex].UFTraspaso,
								"PorcentajeTraspaso"			: result.P_CONTRATOS[ContratoIndex].PorcentajeTraspaso,
								"IdEstadoEnvioNotificacion"		: result.P_CONTRATOS[ContratoIndex].IdEstadoEnvioNotificacion,
								"IdEstadoSolicitud"				: result.P_CONTRATOS[ContratoIndex].IdEstadoSolicitud,
								"IdEstado"						: result.P_CONTRATOS[ContratoIndex].IdEstado,
								"IdProductoAgrupador"			: result.P_CONTRATOS[ContratoIndex].IdProductoAgrupador,
								"Producto"						: result.P_CONTRATOS[ContratoIndex].Producto,
								"EstadoNotificacion"			: result.P_CONTRATOS[ContratoIndex].EstadoNotificacion,
								"EstadoSolicitud"				: result.P_CONTRATOS[ContratoIndex].EstadoSolicitud,
								"Estado"						: result.P_CONTRATOS[ContratoIndex].Estado,
								"EstadoNuevo"					: result.P_CONTRATOS[ContratoIndex].EstadoNuevo,
								"IdEstadoNuevo"					: result.P_CONTRATOS[ContratoIndex].IdEstadoNuevo
						};
						ConsultaContratos.push(Contrato);
					};
					gsaapv.confBasicaSAAPV.dataSource.data(ConsultaContratos);
					if(_filter.length > 0)
						gsaapv.confBasicaSAAPV.dataSource.filter(_filter);
				}
				else{
					gsaapv.confBasicaSAAPV.dataSource.data({});
					
				}
				gridContratoSAAPV = gsaapv.confBasicaSAAPV.dataSource;
				gridContratoSAAPV.refresh;
				bcsComunes.ventanaOverlay(idVentana,false);
			});
			
			*/
			
			
			/*
			var ConsultaGestionContratos = new bcs.DataSource(datosSAAPV);
			ConsultaGestionContratos.query({filter : _filter}).then(function(){
				dsDatosSAAPV.data(ConsultaGestionContratos.data());
				dsDatosSAAPV.refresh;
			});
			*/
		};
		
		gsaapv.LimpiarConsultarGestionContratosSAAPV = function()
		{
			gsaapv.dataPersona.IdActual 		= false;
			gsaapv.EstadoIdActual				= false;
			gsaapv.fechaAprobacion				= undefined;
			gsaapv.IdPersonaEmpleador			= false;
			gsaapv.fechaSuscripcion				= undefined;
			gsaapv.fechaNotificacion			= undefined;
			gsaapv.folio						= undefined;
			gsaapv.fechaCreacion				= undefined;
			gsaapv.fechaRecepcionNotificacion	= undefined;
			gsaapv.PlanAhorroIdActual			= false;
			gsaapv.ProductoAgrupadorIdActual	= false;
			gsaapv.RegimenAgrupadorIdActual		= false;
			
			var _filter = Array();
			var ConsultaGestionContratos = new bcs.DataSource(datosSAAPV);
			
			_filter.push({ field: "IdContratoSAAPV"	, operator: "Neq", value: -1});
			dsDatosSAAPV.filter(_filter);
			dsDatosSAAPV.read().then(function(){
				gridContratoSAAPV.data(ConsultaGestionContratos.data());
				gridContratoSAAPV.refresh;
			});

			/*
			var ConsultaGestionContratos = new bcs.DataSource(datosSAAPV);
			ConsultaGestionContratos.query({filter : _filter}).then(function(){
				dsDatosSAAPV.data(ConsultaGestionContratos.data());
				dsDatosSAAPV.refresh;
			});
			*/
			
			//dsDatosSAAPV.filter(filter);
		};
		
		gsaapv.VentanaContratoSAAPV = function(modoVentana)
		{
			var filterVentana = Array(); 
			filterVentana.modoVentana = modoVentana;
			
			if(modoVentana === 'I')
			{
				var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, idVentana);
			}
			else
			{		
				if(modoVentana === 'M')
				{
					if(gsaapv.seleccionSAAPV !== undefined)
					{
						if(gsaapv.seleccionSAAPV[0].IdEstado === 'PDI' && gsaapv.seleccionSAAPV[0].IdEstadoSolicitud === 'PDA')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_PARA_MODIFICAR_CONTRATO_NO_DEBE_ESTAR_RECHAZADO_PENDIENTE_AUTORIZACION'));
							return;
						}
						else if(gsaapv.seleccionSAAPV[0].IdEstado === 'REV')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_REVOCADO'));
							return;
						}else if(gsaapv.seleccionSAAPV[0].IdEstado === 'COM'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_COMPLETADO'));
							return;
						}else if(gsaapv.seleccionSAAPV[0].IdEstado === 'CER'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_CERRADO'));
							return;
						}else if(gsaapv.seleccionSAAPV[0].IdEstado === 'ANU'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_ANULADO'));
							return;
						}
						
						filterVentana.IdContratoSAAPV 		= gsaapv.seleccionSAAPV[0].IdContratoSAAPV;
						filterVentana.DocumentoIdentidad	= gsaapv.seleccionSAAPV[0].RutCliente.replace(/[.]/gi,'');
					}
					else
					{
						bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_CONTRATO_PARA_MODIFICAR'));
						return;
					}
							
					var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, idVentana);
				}
				else if(modoVentana === 'C')
				{
					filterVentana.IdContratoSAAPV 		= gsaapv.seleccionSAAPV[0].IdContratoSAAPV;
					var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaContratoSAAPV", filterVentana, true, false, idVentana);
				} 
			}
			
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data)
			{
				if(modoVentana === 'M')
				{
					gsaapv.ConsultarGestionContratosSAAPV();
				}
	        });
		};
		
		gsaapv.ValidaContratoClientes = function(opcion)
		{
			var filtros = [];
			if(gsaapv.seleccionSAAPV !== undefined){
				$.each(gsaapv.seleccionSAAPV,function(key,value){
					if(value.IdEstado === 'PDI' || value.IdEstado === 'VIG' ){
						var Obj = {
							"IdPersona" : value.IdPersona === undefined ? null : value.IdPersona,
							"IdCliente" : value.IdCliente === undefined ? null : value.IdCliente,
							"IdEmpleador" : value.IdEmpleador === undefined ? null : value.IdEmpleador,
							"IdPortafolio" : value.IdPortafolio === undefined ? null : value.IdPortafolio,
							"DocumentoIdentidad" : value.RutCliente,		
							"DocumentoIdentidadEmpleador" : value.RutEmpleador,		
							"FolioContratoSAAPV" : value.FolioContratoSAAPV
						};
						filtros.push(Obj);
					}
				});
				
				var parametros = {				
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'APV', 
						p_Opcion : opcion,
						p_ContratosSAAPV : filtros
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServiciosAPV.ValidaContratoSAAPV(parametros).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_PROCESO_COMPLETADO')},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
					else{
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error",$translate.instant('UI_EXISTEN_PROBLEMAS_AL_VALIDAR_CONTRATOS'));
						});
						bcsComunes.ventanaOverlay(idVentana,false);
				    }
		   		},function(respuesta){
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_EXISTEN_PROBLEMAS_AL_LLAMAR_A_PROCESO_VALIDACION_CONTRATOS_SAAPV'),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   		});
			}
			
		};
		
		gsaapv.CambiarEstadoContratoSAAPV = function(vOpcion,vEntidad)
		{
			if(gsaapv.seleccionSAAPV !== undefined)
			{
				if (gsaapv.seleccionSAAPV[0].IdEstadoSolicitud === 'RZD' || gsaapv.seleccionSAAPV[0].IdEstado === 'REV')
				{
					bcsComunes.msg("warning",'warning',$translate.instant('UI_ESTA_FUNCIONALIDAD_ES_SOLO_PARA_CONTRATOS_VIGENTES'));
					return;
				}
				else if(vOpcion === "COM")
				{					
					//MENSAJE TEMPORAL
					bcsComunes.msg("warning",'warning',$translate.instant('UI_ESTA_ACCION_SOLO_ES_VALIDA_PARA_TRASPASOS'));
					return;
				}
				else
				{
					var ContratosSAAPV = [];
					
					if(vOpcion === "CER")
					{
						if(gsaapv.seleccionSAAPV[0].IdEstadoSolicitud !== 'PDS' && gsaapv.seleccionSAAPV[0].IdEstado === 'VIG')
						{
							var Obj = 
							{
									"IdPersona"					: Number(gsaapv.seleccionSAAPV[0].IdPersona),
									"IdCliente"					: Number(gsaapv.seleccionSAAPV[0].IdCliente),
									"IdEmpleador"				: Number(gsaapv.seleccionSAAPV[0].IdEmpleador),
									"IdFolioContratoSAAPV"		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
									"FechaRevocacion"			: $scope.SesionFechaSistema,
									"IdEstadoEnvioNotificacion"	: '',
									"IdEstadoSolicitud"			: 'PDS',
									"IdEstado"					: vOpcion
							}
							ContratosSAAPV.push(Obj);
						}
						else
						{
							bcsComunes.msg("warning","warning",$translate.instant("UI_DEBE_SELECCIONAR_CONTRATOS_ESTADO_VIGENTE_SIN_SOLICITUD_PENDIENTE"));
							return;
						}
					}
					else if(vOpcion === "REV")
					{
						if(gsaapv.seleccionSAAPV[0].IdEstadoSolicitud !== 'PDS' && (gsaapv.seleccionSAAPV[0].IdEstado === 'VIG' || gsaapv.seleccionSAAPV[0].IdEstado === 'PEN'))
						{
							var Obj = 
							{
									"IdPersona"					: Number(gsaapv.seleccionSAAPV[0].IdPersona),
									"IdCliente"					: Number(gsaapv.seleccionSAAPV[0].IdCliente),
									"IdEmpleador"				: Number(gsaapv.seleccionSAAPV[0].IdEmpleador),
									"IdFolioContratoSAAPV"		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
									"FechaRevocacion"			: $scope.SesionFechaSistema,
									"IdEstadoEnvioNotificacion"	: '',
									"IdEstadoSolicitud"			: 'PDS',
									"IdEstado"					: vOpcion
							}
							ContratosSAAPV.push(Obj);
						}
						else
						{
							bcsComunes.msg("warning","warning",$translate.instant("UI_DEBE_SELECCIONAR_CONTRATOS_ESTADO_VIGENTE_PENDIENTE_INGRESO_SIN_SOLICITUD_PENDIENTE"));
							return;
						}
					}
					else if(vOpcion === "ANU")
					{
						if(gsaapv.seleccionSAAPV[0].IdEstado === 'PDI')
						{
							var Obj = 
							{
									"IdPersona"					: Number(gsaapv.seleccionSAAPV[0].IdPersona),
									"IdCliente"					: Number(gsaapv.seleccionSAAPV[0].IdCliente),
									"IdEmpleador"				: Number(gsaapv.seleccionSAAPV[0].IdEmpleador),
									"IdFolioContratoSAAPV"		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
									"FechaRevocacion"			: $scope.SesionFechaSistema,
									"IdEstadoEnvioNotificacion"	: '',
									"IdEstadoSolicitud"			: 'ANU',
									"IdEstado"					: vOpcion
							}
							ContratosSAAPV.push(Obj);
						}
						else
						{
							bcsComunes.msg("warning","warning",$translate.instant("UI_DEBE_SELECCIONAR_CONTRATOS_ESTADO_PENDIENTE_INGRESO"));
							return;
						}
					}
					
					// Cambiar el estado del contrato SAAPV a VIGENTE
					else if(vOpcion === "VIG")
					{
						if(gsaapv.seleccionSAAPV[0].IdEstado !== 'VIG')
						{
							var Obj = 
							{
									"IdPersona"					: Number(gsaapv.seleccionSAAPV[0].IdPersona),
									"IdCliente"					: Number(gsaapv.seleccionSAAPV[0].IdCliente),
									"IdEmpleador"				: Number(gsaapv.seleccionSAAPV[0].IdEmpleador),
									"IdFolioContratoSAAPV"		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
									"FechaRevocacion"			: $scope.SesionFechaSistema,
									"IdEstadoEnvioNotificacion"	: '',
									"IdEstadoSolicitud"			: 'VIG',
									"IdEstado"					: vOpcion
							}
							ContratosSAAPV.push(Obj);
						}
						else
						{
							bcsComunes.msg("warning","warning",$translate.instant("UI_CONTRATO_SE_ENCUENTRA_VIGENTE"));
							return;
						}
					}
					
					
					var IdVentanamodal = bcsComunes.ventanaAbrir("ModalGestionSAAPV",{IdEntidad : vOpcion, Entidad : vEntidad, ContratosSAAPV : ContratosSAAPV},true,false,gsaapv.idVentana);
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
						gsaapv.ConsultarGestionContratosSAAPV();
					});	
				}
			}
			else
			{
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_DEBE_SELECCIONAR_CONTRATO_PARA_MODIFICARLO')},idVentana);
				return;
			}
		};
		
		//* LOGICA PARA GENERAR EL REPORTE *//	
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015", //*LLAMADA CONSULTA TEMPLATES
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
							CompanyId	: {type: "string"},
							Id			: {type: "string"},
							ReportName	: {type: "string"}					
						}
					}
				}
			},
		});
		
		gsaapv.getReport = function(dataTemplate)
		{
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.Id,
				"parameters"	:  {},
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
						"data"		: dataTemplate.data,
						"parameters": dataTemplate.parameters
					},
					"subReports": dataTemplate.subReports
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					//var fileURL = URL.createObjectURL(file);
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {				
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos:util.parseServerResponse(response)
					},idVentana);
				});
		};
			
		//* LOGICA PARA GENERAR EL REPORTE NOTIFICACION *//	
		gsaapv.GeneraCartaNotificacion = function(i,consultaTemplate,consulta)		
	    { 
			gsaapv.dataTemplate= {};
			
			if(gsaapv.seleccionSAAPV.length ===1){
				if(gsaapv.seleccionSAAPV[i].RutEmpleador === "" || gsaapv.seleccionSAAPV[i].RutEmpleador === undefined || gsaapv.seleccionSAAPV[i].RutEmpleador === null){
					bcsComunes.msg("warning","warning",$translate.instant("UI_CLIENTE_SIN_EMPLEADOR"));
					return;
				}
			}
			
			if(i === 0)empleadores = [];			
			if(consulta){
				var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'NOTIFICACIONEMPLEADORSAAPV' } ];
				ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
					gsaapv.GeneraCartaNotificacion(0,ConsultaTemplate.view(), false);
					return;
				});
			}
			
			if(consultaTemplate.length > 0) {
				if(empleadores.indexOf(gsaapv.seleccionSAAPV[i].RutEmpleador) === -1 && gsaapv.seleccionSAAPV[i].RutEmpleador !== "" && gsaapv.seleccionSAAPV[i].RutEmpleador !== undefined && gsaapv.seleccionSAAPV[i].RutEmpleador !== null){
					empleadores.push(gsaapv.seleccionSAAPV[i].RutEmpleador);
						gsaapv.dataTemplate.TipoReporte		= 'pdf';
						gsaapv.dataTemplate.Id				= consultaTemplate[0].Id;
						gsaapv.dataTemplate.ReportName		= 'CARTA_'+(gsaapv.seleccionSAAPV[i].RutEmpleador.replace(/[.]/gi,''));//consultaTemplate[0].ReportName;
					
						var parametros = { 	"P_IDEMPRESA"		: $scope.SesionIdEmpresa,
											"P_IDMODULO"		: 'APV',
											"P_FECHA"			: $scope.SesionFechaSistema,
											"P_IDEMPLEADOR"		: gsaapv.seleccionSAAPV[i].IdEmpleador,
											"$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										};
					 
						gsaapv.dataTemplate.data = { "serviceId" 	: Number(6092), 
													 "serviceData" 	: parametros 
												   };
						gsaapv.dataTemplate.parameters= [];
							
						var subrep = new Array();
						gsaapv.dataTemplate.subReports=subrep;
						
						var parameters = {
							"exportFormat"	: gsaapv.dataTemplate.TipoReporte,
							"reportId"		: gsaapv.dataTemplate.Id,
							"parameters"	:  {},
							"configuration"	: {
								"mainReport": {
									"reportName": gsaapv.dataTemplate.ReportName,
									"data"		: gsaapv.dataTemplate.data,
									"parameters": gsaapv.dataTemplate.parameters
								},
								"subReports": gsaapv.dataTemplate.subReports
							}
						};
							
						$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
							.success(function (response) {
								var file = new Blob([response], {type: 'application/pdf'});
								//var fileURL = URL.createObjectURL(file);
								var nombreArchivo = gsaapv.dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
								saveAs(file, nombreArchivo+"."+gsaapv.dataTemplate.TipoReporte);
								bcsComunes.ventanaOverlay($scope.winID,false);
								if(i < (gsaapv.seleccionSAAPV.length - 1)){
									var fila = i + 1;
									gsaapv.GeneraCartaNotificacion(fila,consultaTemplate,false);
								}	
							})
							.error(function (response) {				
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
									conflictos:util.parseServerResponse(response)
								},idVentana);
								if(i < (gsaapv.seleccionSAAPV.length - 1)){
									var fila = i + 1;
									gsaapv.GeneraCartaNotificacion(fila,consultaTemplate,false);
								}	
							});
						
						var parametros = 
						{				
							p_IdEmpresa 		: $scope.SesionIdEmpresa,
							p_IdUsuario 		: $scope.SesionIdUsuario,
							p_IdModulo  		: "APV",
							p_Opcion			: "VIG",
							p_ContratosSAAPV	: 
							[{
								IdFolioContratoSAAPV		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
								IdEstadoEnvioNotificacion	: gsaapv.seleccionSAAPV[0].IdEstadoEnvioNotificacion,
							}],
						};
						
						bcsServiciosAPV.CambiarEstadoEnvioNotificacion(parametros).$promise.then(function(respuesta){});
					
					}else{
						if(i < (gsaapv.seleccionSAAPV.length - 1)){
							var fila = i + 1;
							gsaapv.GeneraCartaNotificacion(fila,consultaTemplate,false);
						}	
					}
				}
			};
		
		//GENERAR REPORTE CONTRATO SAAPV
		gsaapv.dataTemplate1= {};
		gsaapv.GeneraContrato = function()	
	    {
			var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'CONTRATO_SAAPV' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function()
			{
				if(ConsultaTemplate.view().length > 0) 
				{
					gsaapv.dataTemplate1.TipoReporte	= 'pdf';
					gsaapv.dataTemplate1.Id				= ConsultaTemplate.view()[0].Id;
					gsaapv.dataTemplate1.ReportName		= 'SAAPV_'+(gsaapv.seleccionSAAPV[0].RutCliente.replace(/[.]/gi,''));//ConsultaTemplate.view()[0].ReportName;
					
//JALA al 05-04-2019 se condiciona P_SOLICITUD = T para cuando se trata de un tipo de solicitud de traspaso				
					var parametros = { 	"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
										"P_SOLICITUD"	: gsaapv.seleccionSAAPV[0].CodigoTipoSolicitud === "TRA" ? "T" : "A",
										"P_SECCION"		: 'REP',
										"P_IDCONTRATO"	: gsaapv.seleccionSAAPV[0].IdContratoSAAPV,
										"$schema" 		: [{ "field" : "LogoCorredora", "type" : "blob" }]
									};
/* JALA codigo antes de intervencion de condicion de tipo Solicitud					
					var parametros = { 	"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
							"P_SOLICITUD"	: 'A',
							"P_SECCION"		: 'REP',
							"P_IDCONTRATO"	: gsaapv.seleccionSAAPV[0].IdContratoSAAPV,
							"$schema" 		: [{ "field" : "LogoCorredora", "type" : "blob" }]
						};
*/
					
					
					
					gsaapv.dataTemplate1.data = { "serviceId" 	: Number(6044)
												, "serviceData" : parametros 
												};
					//SUB-REPORTE :RESCLIENTES
					var parametros1 =  { "P_IDEMPRESA"	: $scope.SesionIdEmpresa,
										 "P_SOLICITUD"	: gsaapv.seleccionSAAPV[0].CodigoTipoSolicitud === "TRA" ? "T" : "A",
										 "P_SECCION"	: 'ENT',
										 "P_IDCONTRATO"	:gsaapv.seleccionSAAPV[0].IdContratoSAAPV
										  };
					 //"P_SOLICITUD"	: 'A' Antes de intervencion de JALA del 05-04-2019

					var subrep = new Array();
					gsaapv.dataTemplate1.subReports = subrep;
					subrep.push({"reportName" : "Entidades",
										                  "data" : { "serviceId" 	: Number(6044)
																   , "serviceData" : parametros1 
																	}
								});				
					gsaapv.dataTemplate1.parameters= {};
					gsaapv.getReport(gsaapv.dataTemplate1);
				}
			});
		}
		//*FIN LOGICA REPORTE*//
		
		gsaapv.reenviarAutorizacion = function()
		{
			if(gsaapv.seleccionSAAPV !== undefined)
			{
				if(gsaapv.seleccionSAAPV[0].IdEstadoNuevo === 'PDA')
				{
					
					bcsComunes.ventanaOverlay(idVentana,true);
					
					var filtro = Array();
					filtro.push({ field: "IdCliente"				, operator: "eq", value: Number(gsaapv.seleccionSAAPV[0].IdCliente)});
					filtro.push({ field: "NumeroOperacionConcepto"	, operator: "eq", value: Number(gsaapv.seleccionSAAPV[0].FolioContratoSAAPV)});
					
					var ConsultaEventoAutorizacion = new bcs.DataSource(datosEventoAutorizacion);
					ConsultaEventoAutorizacion.query({ filter : filtro}).then(function()
					{
						if(ConsultaEventoAutorizacion.view()[0].NombreEstado === 'VENCIDO')
						{
							var parametros = 
							{				
								p_IdEmpresa 		: $scope.SesionIdEmpresa,
								p_IdUsuario 		: $scope.SesionIdUsuario,
								p_IdModulo  		: "APV",
								p_Opcion			: "VIG",
								p_ContratosSAAPV	: 
								[{
									IdPersona					: gsaapv.seleccionSAAPV[0].IdPersona,
									IdCliente					: gsaapv.seleccionSAAPV[0].IdCliente,
									IdEmpleador					: gsaapv.seleccionSAAPV[0].IdEmpleador,
									IdFolioContratoSAAPV		: gsaapv.seleccionSAAPV[0].FolioContratoSAAPV,
									FechaRevocacion				: "1900-01-01",
									IdEstadoEnvioNotificacion	: gsaapv.seleccionSAAPV[0].IdEstadoEnvioNotificacion,
									IdEstadoSolicitud			: "PDA",
									IdEstado					: "VIG"
								}],
							};
							
							bcsServiciosAPV.CambiarEstadoContratosSAAPV(parametros).$promise.then(function(respuesta)
							{
						   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						   		
								if (respuesta.P_OUT_ERRORESCONTADOR === 0)
								{
									bcsComunes.msg("info",'info',$translate.instant('UI_SOLICITUD_DE_AUTORIZACION_ENVIADA'));
									bcsComunes.ventanaOverlay(idVentana,false);
								}
								else
								{
									var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
									$.each(ErrorDetalle,function(key,value)
									{	
										bcsComunes.msg("error","Error","Existen problemas:" + value );
									});
									bcsComunes.ventanaOverlay(idVentana,false);
							    }
					   		},function(respuesta)
					   		{
								var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					   			$.each(ErrorDetalle,function(key,value)
					   			{	
									bcsComunes.msg("error","Error","Existen problemas:" + value );
								});
					   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					   		});
						}
						else if(ConsultaEventoAutorizacion.view()[0].NombreEstado === 'VIGENTE')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_CONTRATO_TIENE_UNA_AUTORIZACION_VIGENTE'));
							bcsComunes.ventanaOverlay(idVentana,false);
						}
						else if(ConsultaEventoAutorizacion.view()[0].NombreEstado === 'RECHAZADO')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_CONTRATO_ESTA_RECHAZADO'));
							bcsComunes.ventanaOverlay(idVentana,false);
						}
						else if(ConsultaEventoAutorizacion.view()[0].NombreEstado === 'AUTORIZADO')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_CONTRATO_ESTA_AUTORIZADO'));
							bcsComunes.ventanaOverlay(idVentana,false);
						}
						else
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_CONTRATO_NO_ESTA_PENDIENTE_DE_AUTORIZACION'));
							bcsComunes.ventanaOverlay(idVentana,false);
						}
					});
				}
				else
				{
					bcsComunes.msg("warning",'warning',$translate.instant('UI_EL_CONTRATO_DEBE_ESTAR_PENDIENTE_DE_APROBACION'));
					return;
				}
			}
			else
			{
				bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_CONTRATO_PARA_REENVIAR_AUTORIZACION'));
				return;
			}
		}
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());