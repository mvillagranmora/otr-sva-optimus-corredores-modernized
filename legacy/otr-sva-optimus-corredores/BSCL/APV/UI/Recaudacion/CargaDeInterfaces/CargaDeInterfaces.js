angular.module('ModuloAPV').controller('DetalleCargaInterfacesCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';
	
		$scope.data = {};
			
		var dci = this;
		
		//	Configurar al principio
		
		dci.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		dci.data = {};
		
		//Asigna valores iniciales
		var datos = {};

		dci.tabs = { 
				Afiliacion : {
					PanelDetalleCargaContratoSAAPV	: { id : 'PanelDetalleCargaContratoSAAPV',	url : '<div ng-include="\'../Paneles/PanelDetalleCargaContratoSAAPV.html\'" ></div>',	active : true,  cargado: false, datosCambiados: false }		
				},
				Recaudacion : {
					
				}			
		};
		dci.fnTabSelect = function(ambito,nombreTab) {
			if (dci.tabs[ambito][nombreTab].cargado !== true) {
				dci.tabs[ambito][nombreTab].cargado = true;
				dci.domVentana.find('#'+dci.tabs[ambito][nombreTab].id).append($compile(dci.tabs[ambito][nombreTab].url)($scope));
			}
		};
		
		dci.fnTabDeselect = function(ambito,nombreTab) {
			if (dci.tabs[ambito][nombreTab].datosCambiados === true) dci.fnPreguntaCambioDeTab(ambito,nombreTab);
		};	
		
		dci.buscarDistribucionPorContrato = function(contrato){			
			try {
				var filter = Array();	
				var IdContratoSAAPV = contrato[0].IdContratoSAAPV;
				filter.push({ field: "IdContratoSAAPV"	, operator: "eq", value: IdContratoSAAPV });
				dsDistribInversion.filter(filter);
			
			} catch (e) {
				var iii = 0;
			}			
		};
				
		dci.cargarArchivo = function(){
			var datosArchivo = {codigoArchivo: 'CARGA_CONTRATOS_SAAPV', titulo: $translate.instant('UI_CARGA_CONTRATOS_SAAPV')};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false, idVentana);
			
			var promesa=bcsComunes.ventanaPromesa(id1);
			promesa.then(function(data){
				//		     
			}); 
		};
		
		var datosSAAPV = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarGestionContratosSAAPV'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{													
													"IdContratoSAAPV"				: {type: "number"},
												    "FolioContratoSAAPV" 			: {type: "string"},
												    "IdPlan" 						: {type: "number"},
													"NombrePlan" 					: {type: "string"},
													"FechaSuscripcion" 				: {type: "sysdate"},
													"FechaCreacion" 				: {type: "sysdate"},
													"FechaAprobacion" 				: {type: "sysdate"},
													"FechaNotificacion" 			: {type: "sysdate"},
													"FechaRecepcion"				: {type: "sysdate"},
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
												    "OrigenAhorro"					: {type: "string"},
													"MontoPesos" 					: {type: "number"},
													"MontoUF" 						: {type: "number"},
													"PorcentajeRegimenImponible"	: {type: "number"},
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
													"IdCargaMasiva"					: {type: "number"},
												}
											}
									  }
    			 }
		};
		//------ GRILLA
		var dsDatosSAAPV = new bcs.DataSource(datosSAAPV);		

		dci.contratoOptions = 
		{
    		dataSource		: dsDatosSAAPV,
    			columns		: [  {field: "IdContratoSAAPV"				, title: "UI_ID_CONTRATO"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true}
								,{field: "FolioContratoSAAPV"			, title: "UI_FOLIO_CONTRATO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "IdPlan"						, title: "UI_ID_PLAN"				, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
								,{field: "NombrePlan"					, title: "UI_NOMBRE_PLAN"			, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "Folio"						, title: "UI_FOLIO"					, width: "110px", align: "right"	, sortable : true, editable: false }
								,{field: "IdPersona"					, title: "UI_ID_PERSONA"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
								,{field: "RutCliente"					, title: "UI_DOCUMENTO_IDENTIDAD"	, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "NombrePersona"				, title: "UI_NOMBRE_PERSONA"		, width: "300px", align: "left"		, sortable : true, editable: false }
								,{field: "Tipo"							, title: "UI_TIPO_PERSONA"			, width: "80px"	, align: "center"	, sortable : true, editable: false }
								
								,{field: "FechaNacimientoCliente"		, title: "UI_FECHA_NACIMIENTO"		, width: "80px"	, align: "center"	, sortable : true, editable: false }
								,{field: "EstadoCivil"					, title: "UI_ESTADO_CIVIL"			, width: "80px"	, align: "center"	, sortable : true, editable: false }
								,{field: "IdTipoGenero"					, title: "UI_TIPO_GENERO"			, width: "80px"	, align: "center"	, sortable : true, editable: false }
								,{field: "DireccionCliente"				, title: "UI_DIRECCION_CLIENTE"		, width: "200px", align: "center"	, sortable : true, editable: false }
								,{field: "TelefonoCliente"				, title: "UI_TELEFONO"				, width: "100px", align: "center"	, sortable : true, editable: false }
								,{field: "TelefonoComercialCliente"		, title: "UI_TELEFONO_COMERCIAL"	, width: "100px", align: "center"	, sortable : true, editable: false }
								,{field: "TelefonoCelularCliente"		, title: "UI_TELEFONO_CELULAR"		, width: "100px", align: "center"	, sortable : true, editable: false }
								,{field: "EmailCliente"					, title: "UI_EMAIL_CLIENTE"			, width: "200px", align: "center"	, sortable : true, editable: false }
										
								,{field: "IdEmpleador"					, title: "UI_ID_EMPLEADOR"			, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
								,{field: "RutEmpleador"					, title: "UI_RUT_EMPLEADOR"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreEmpleador"				, title: "UI_NOMBRE_EMPLEADOR"		, width: "200px", align: "left"		, sortable : true, editable: false }
								,{field: "DireccionComercialEmpleador"	, title: "UI_DIRECCION_EMPELADOR"	, width: "200px", align: "left"		, sortable : true, editable: false }
								,{field: "TelefonoEmpleador"			, title: "UI_TELEFONO"				, width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreEncargado"				, title: "UI_NOMBRE_ENCARGADO"		, width: "200px", align: "left"		, sortable : true, editable: false }
								,{field: "EmailEncargado"				, title: "UI_EMAIL_EMPLEADOR"		, width: "200px", align: "left"		, sortable : true, editable: false }
								,{field: "TelefonoEncargado"			, title: "UI_TELEFONO_EMPLEADOR"	, width: "100px", align: "left"		, sortable : true, editable: false }
								
								,{field: "FechaSuscripcion"				, title: "UI_FECHA_SUSCRIPCION"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FechaCreacion"				, title: "UI_FECHA_CREACION"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FechaAprobacion"				, title: "UI_FECHA_APROBACION"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FechaNotificacion"			, title: "UI_FECHA_NOTIFICACION"	, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FechaRecepcion"				, title: "UI_FECHA_RECEPCION"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "CodigoPrevision"				, title: "UI_CODIGO_PREVISION"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Pensionado"					, title: "UI_PENSIONADO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Trabajador"					, title: "UI_TRABAJADOR"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "OrigenAhorro"					, title: "UI_ORIGEN_AHORRO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoPesos"					, title: "UI_MONTO_AHORRO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoUF"						, title: "UI_UF"					, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "PorcentajeRegimenImponible" 	, title: "UI_PORCENTAJE_REGIMEN"	, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Pago"							, title: "UI_PAGO"					, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "PeriodoInicio"				, title: "UI_PERIODO_INICIO"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "PeriodoTermino"				, title: "UI_PERIODO_TERMINO"		, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "RegimenTributario"			, title: "UI_REGIMEN_TRIBUTARIO"	, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FormaDePago"					, title: "UI_FORMA_PAGO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "IdMedioRecaudacionFormaPago"	, title: "UI_ID_MEDIO_RECAUDACION"	, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
								,{field: "NombreMedioRecaudacion"		, title: "UI_NOMBRE_MEDIO_RECAUDACION"	, width: "220px", align: "left"		, sortable : true, editable: false }
								,{field: "DiaDescuento"					, title: "UI_DIA_DESCUENTO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "Institucion"					, title: "UI_INSTITUCION"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "ComisionTransferencia"		, title: "UI_COMISION_TRANSFERENCIA", width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "RutEntidadOrigen"				, title: "UI_RUT_ENTIDAD_ORIGEN"	, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "NombreEntidadOrigen"			, title: "UI_NOMBRE_ENTIDAD_ORIGEN"	, width: "180px", align: "left"		, sortable : true, editable: false }
								,{field: "TipoDeAhorro"					, title: "UI_TIPO_AHORRO"			, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "FondoDeOrigen"				, title: "UI_FONDO_ORIGEN"			, width: "110px", align: "center"	, sortable : true, editable: false }
								,{field: "TipoTraspaso"					, title: "UI_TIPO_TRASPASO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "MontoTraspaso"				, title: "UI_MONTO_TRASPASO"		, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "UFTraspaso"					, title: "UI_UF_TRASPASO"			, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "PorcentajeTraspaso"			, title: "UI_PORCENTAJE_TRASPASO"	, width: "110px", align: "left"		, sortable : true, editable: false }
								,{field: "IdEstadoEnvioNotificacion"	, title: "UI_ESTADO_NOTIFICACION"	, width: "70px"	, align: "center"	, sortable : true, editable: false }
								,{field: "IdEstadoSolicitud"			, title: "UI_ESTADO_SOLICITUD"		, width: "70px"	, align: "center"	, sortable : true, editable: false }
								,{field: "IdEstado"						, title: "UI_ESTADO"				, width: "70px"	, align: "center"	, sortable : true, editable: false }
								,{field: "IdCargaMasiva"				, title: "UI_ID_CARGA_MASIVA"		, width: "70px"	, align: "center"	, sortable : true, editable: false, hidden: true }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 267,
	    		selectable 	: "multiple row",
	    		autoBind 	: true,
	    		gridCreated: function(grid){
	    			var filter = Array();
	    			if(filter.length ==0){
	    				return;
	    			}else{
	    				filter.push({ field: "IdCargaMasiva", operator: "eq", value: dci.parametrosEntrada.IdCargaMasiva });
		    			dsDatosSAAPV.filter(filter);	
	    			}
	    		}
		};	
				
		var defDistrib = {
				type : "odata",
				definition : {
	                serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 15,
	                sort 			: false,
	                transport		: {
						read: {
							url 		: bcsConsultasAPV('ConsultarDistribucionInversion'),
							dataType	: "json"
					    }
	                },
	                schema 			: {
	                	model: {
	                		fields: {
	                			IdContratoSAAPV    	: {type: "number"},
	                			FolioContratoSAAPV  : {type: "strng"},
	                			IdInstrumento     	: {type: "number"},
								RazonSocial			: {type: "string"},
								Nemotecnico			: {type: "number"},
								MontoPesos			: {type: "number"},
								Porcentaje			: {type: "float"},
								IdEstado			: {type: "string"}
	                		}
	                	}
	                }
				}
		};
		
		var dsDistribInversion = new bcs.DataSource(defDistrib);
		dci.distribucionInvercionOptions = {
			dataSource: dsDistribInversion,
			columns: [
				 {field: "IdContratoSAAPV"	, title: "UI_ID_CONTRATO_SAAPV", width: "20px", align: "right", hidden: true },
			     {field: "IdInstrumento"	, title: "UI_ID_INSTRUMENTO", width: "20px", align: "left", hidden: true },
			     {field: "RazonSocial"		, title: "UI_RAZON_SOCIAL", width: "50px", align: "left"},
			     {field: "Nemotecnico"		, title: "UI_SERIE", width: "15px", align: "left"},
			     {field: "MontoPesos"		, title: "UI_MONTO", width: "15px", align: "right"},
			     {field: "Porcentaje"		, title: "UI_PORCENTAJE", width: "15px", align: "right"}
			],
			sortable	: true,
			pageable	: true,
			filterable	: false,
			height		: 280,
			autoBind	: false,
		};
		
		
	});