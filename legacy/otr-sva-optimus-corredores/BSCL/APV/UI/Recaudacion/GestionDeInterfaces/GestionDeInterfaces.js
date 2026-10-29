angular.module('ModuloAPV').controller('GestionDeInterfacesCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {}; 
		
		var gesint = this;
		//Configurar al principio
		gesint.data = {};
		
		var datosCarga = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdCargaMasiva", dir: "desc" },
                    pageSize        : 25,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarGestionCargaMasiva'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{													
													"IdCargaMasiva"			: {type: "number"},
												    "IdEmpresa" 			: {type: "number"},
													"NombreCargaArchivo" 	: {type: "string"},
													"CantidadRegistros" 	: {type: "number"},
													"IdTipoCarga" 			: {type: "number"},
													"NombreTipoCarga" 		: {type: "string"},
													"Descripcion" 			: {type: "string"},
													"FechaCarga"			: {type: "sysdate"},
													"IdUsuario"				: {type: "string"},
													"CodigoUsuario"			: {type: "string"},
													"IdEstado"				: {type: "string"},
												}
											}
									  }
    			 }
		};
		
		//------ GRILLA
		var dsDatosContrato = new bcs.DataSource(datosCarga);
		gesint.confBasicaCarga = 
		{
    		dataSource		: dsDatosContrato,
    			columns		: [  {field: "IdCargaMasiva"		, title: "UI_ID_CARGAMASIVA"	, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true}
								,{field: "IdEmpresa"			, title: "UI_ID_EMPRESA"		, width: "110px", align: "left"		, sortable : true, editable: false, hidden: true }
								,{field: "NombreCargaArchivo"	, title: "UI_NOMBRE_ARCHIVO"	, width: "160px", align: "left"		, sortable : true, editable: false }
								,{field: "CantidadRegistros"	, title: "UI_CANTIDAD_REGISTROS", width: "100px", align: "left"		, sortable : true, editable: false }
								,{field: "IdTipoCarga"			, title: "UI_ID_TIPOCARGA"		, width: "10px" , align: "right"	, sortable : true, editable: false, hidden: true }
								,{field: "NombreTipoCarga"		, title: "UI_NOMBRE_TIPOCARGA"	, width: "160px", align: "left"		, sortable : true, editable: false }
								,{field: "Descripcion"			, title: "UI_DESCRIPCION"	    , width: "220px", align: "left"		, sortable : true, editable: false }
								,{field: "FechaCarga"			, title: "UI_FECHA_CARGA"		, width: "70px" , align: "left"		, sortable : true, editable: false }
								,{field: "IdUsuario"			, title: "UI_ID_USUARIO"		, width: "80px"	, align: "center"	, sortable : true, editable: false, hidden: true }
								,{field: "CodigoUsuario"		, title: "UI_USUARIO"			, width: "80px"	, align: "center"	, sortable : true, editable: false }
								,{field: "IdEstado"				, title: "UI_ESTADO"			, width: "80px"	, align: "center"	, sortable : true, editable: false }
							  ],
				sortable	: true,
	    		pageable	: true,
	    		filterable	: true,
	    		navigatable	: true,
	    		height		: 250,
	    		selectable 	: "row",
	    		autoBind 	: false
		};	
		
		gesint.VentaDetalleCarga = function(){
			var filterVentana = Array(); 
			if(gesint.seleccionCarga !== undefined){
				filterVentana.IdCargaMasiva = gesint.seleccionCarga.IdCargaMasiva;
			}
			else{
				bcsComunes.msg("Warning","Error","Debe seleccionar una carga de archivo.");
				return;
			}
						
			if(gesint.seleccionCarga.Descripcion==="Carga Recaudacion"){
				if(gesint.seleccionCarga.IdEstado=='PRO'){
					bcsComunes.msg("Warning","Error","La carga ya ha sido procesada");
					return;
				}else{
					var IdVentanamodal = bcsComunes.ventanaAbrir("CargaDetalleRecaudacion", filterVentana, true, false, idVentana);
				}
				
			}else{
				var IdVentanamodal = bcsComunes.ventanaAbrir("CargaDeInterfaces", filterVentana, true, false, idVentana);
			}
			
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				//
	        });
		};
		
		gesint.ConsultarCargaArchivoMasiva = function(){
			var filter = Array(); 
			
			if(gesint.TipoCargaSeleccionado && gesint.TipoCargaSeleccionado.NombreTipoCarga !== " TODOS" )
				filter.push({ field: "IdTipoCarga"	, operator: "eq", value: Number(gesint.TipoCargaSeleccionado.IdTipoCarga) });
			
			if(gesint.EstadoSeleccionado && gesint.EstadoSeleccionado.Nombre !== " TODOS")
				filter.push({ field: "IdEstado"		, operator: "eq", value: gesint.EstadoSeleccionado.Codigo });

			if(gesint.kngFechaLiquidacionDesde !== undefined)
				filter.push({ field: "FechaCarga"	, operator: "gte", value: util.dateSPdate(gesint.kngFechaLiquidacionDesde) });
			
			if(gesint.kngFechaLiquidacionHasta !== undefined)
				filter.push({ field: "FechaCarga"	, operator: "lte", value: util.dateSPdate(gesint.kngFechaLiquidacionHasta) });
			
						
			//if(filter.length > 0)
			dsDatosContrato.read();
				dsDatosContrato.filter(filter);
				
			//else
			//	dsDatosContrato.read();
		};
		
		gesint.cargarArchivo = function(){
			
			var datosArchivo = {codigoArchivo: 'CARGA_CONTRATOS_SAAPV', titulo: $translate.instant('UI_CARGA_CONTRATOS_SAAPV')};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false, idVentana);
			var promesa=bcsComunes.ventanaPromesa(id1);
			var contDistri = 1;
			
			promesa.then(function(data){
				console.log(data);
				var contratos = [];
				var distribucion = [];
				var regimenes = [];
				var DistribucionMatriz = new Array(10);
				var detalleOrigenTraspasos = []; 
				if(data.fileData !== undefined){
					for(var i=1; i <= data.fileData.length; i++){
						DistribucionMatriz[i] = new Array(4);
						for(var j=1; j <= data.fileData.length; j++){
							DistribucionMatriz[i][j] = data.fileData[i]; 
						}
					}
					var i = 0;
					var IdPlan = 0;
					$.each(data.fileData,function(key,value){
						IdPlan = value.NombrePlan.toUpperCase() === "A. ABIERTO" ? 1 : 2;
								
						var contenidoContrato = {
								IdContratoSAAPV				: 0,
								IdEmpresa					: $scope.SesionIdEmpresa,
								FolioContratoSAAPV 			: value.FolioContratoSAAPV,
								IdPlan						: IdPlan,
								NombrePlan					: value.NombrePlan.toUpperCase(),
								FechaSuscripcion			: value.FechaSuscripcion.substring(6)+'-'+value.FechaSuscripcion.substring(3,5)+'-'+value.FechaSuscripcion.substring(0,2),
								FechaCreacion				: value.FechaCreacion.substring(6)+'-'+value.FechaCreacion.substring(3,5)+'-'+value.FechaCreacion.substring(0,2),
								FechaAprobacion				: value.FechaAprobacion.substring(6)+'-'+value.FechaAprobacion.substring(3,5)+'-'+value.FechaAprobacion.substring(0,2),
								FechaNotificacion			: value.FechaNotificacion.substring(6)+'-'+value.FechaNotificacion.substring(3,5)+'-'+value.FechaNotificacion.substring(0,2),
								FechaRecepcion				: value.FechaRecepcion.substring(6)+'-'+value.FechaRecepcion.substring(3,5)+'-'+value.FechaRecepcion.substring(0,2),						
								Folio						: value.Folio,
								IdPersona					: null,
								IdEmpleador					: null,
								IdCliente					: null,
								IdCategoriaInversionista	: value.IdCategoriaInversionista.toUpperCase(),
								IdPerfilComercial			: value.IdPerfilComercial.toUpperCase(),
								IdEnrolamiento				: 0,
								IdPortafolio				: 0,
								NombreCliente				: value.NombreCliente.toUpperCase(),
								ApellidoPaternoCliente		: value.ApellidoPaternoCliente.toUpperCase(),
								ApellidoMaternoCliente		: value.ApellidoMaternoCliente.toUpperCase(), 
								DocumentoIdentidadCliente	: value.DocumentoIdentidadCliente,
								FechaNacimientoCliente		: value.FechaNacimientoCliente.substring(6)+'-'+value.FechaNacimientoCliente.substring(3,5)+'-'+value.FechaNacimientoCliente.substring(0,2),
								EstadoCivil					: value.EstadoCivil.toUpperCase(),
								IdTipoGenero				: value.IdTipoGenero.toUpperCase(),
								IdPaisResidenciaCliente		: value.IdPaisResidenciaCliente,
								IdCiudadCliente				: value.IdCiudadCliente,
								IdComunaCliente				: value.IdCiudadCliente,
								DireccionCliente			: value.DireccionCliente.toUpperCase(),
								NumeroDireccionCliente		: value.NumeroDireccionCliente,
								IdTipoCargoPersona			: value.IdTipoCargoPersona === undefined ? 1 :value.IdTipoCargoPersona,
								TelefonoFijo				: value.TelefonoFijo,
								TelefonoMovil				: value.TelefonoMovil,
								TelefonoLaboral				: value.TelefonoLaboral,
								EmailCliente				: value.EmailCliente.toUpperCase(),
								RazonSocialEmpleador		: value.RazonSocialEmpleador.toUpperCase(),	    
								DocumentoIdentidadEmpleador	: value.DocumentoIdentidadEmpleador,
								IdPaisEmpleador				: 44,
								DireccionComercialEmpleador	: value.DireccionComercialEmpleador.toUpperCase(),
								NumeroDireccionEmpleador	: value.NumeroDireccionEmpleador,
								IdCiudadEmpleador			: value.IdCiudadEmpleador, 
								IdComunaEmpleador			: value.IdComunaEmpleador, 
								TelefonoEmpleador			: value.TelefonoEmpleador, 
								NombreEncargado				: value.NombreEncargado.toUpperCase(),
								EmailEncargado				: value.EmailEncargado.toUpperCase(),
								TelefonoEncargado			: value.TelefonoEncargado,
								IdMedioRecaudacionTrabajador: value.IdMedioRecaudacionTrabajador,
								IdEntidadRecaudadora		: value.IdEntidadRecaudadora,
								CodigoPrevision				: value.CodigoPrevision,
								Pensionado					: value.Pensionado.toUpperCase(),
								Trabajador					: value.Trabajador.toUpperCase(),
								OrigenAhorro				: "", 
								MontoPesos 				 	: 0,
								MontoUF  					: 0,
								PorcentajeRegimenImponible  : 0,
								Pago						: "",
								PeriodoInicio				: "",
								PeriodoTermino				: "",
								RegimenTributario			: "",
								FormaDePago					: value.FormaDePago.toUpperCase(),
								IdMedioRecaudacionFormaPago	: value.IdMedioRecaudacionFormaPago,
								DiaDescuento				: value.DiaDescuento,
								Institucion					: value.Institucion === '' || value.Institucion === undefined ? '0' : value.Institucion,		
								CodigoTipoSolicitud			: value.CodigoTipoSolicitud,
								TipoMovimiento				: value.TipoMovimiento,
								RazonRetiro					: value.RazonRetiro,						
								ComisionTransferencia		: Number(value.ComisionTransferencia),
								NombreEntidadOrigen			: value.NombreEntidadOrigen.toUpperCase(),
								RutEntidadOrigen			: value.RutEntidadOrigen,						
								IdPersonaOrigen				: value.IdPersonaOrigen,							
								TipoDeAhorro				: value.TipoDeAhorro.toUpperCase(),
								FondoDeOrigen				: value.FondoDeOrigen.toUpperCase(),							
								TipoTraspaso				: value.TipoTraspaso,
								MontoTraspaso				: value.MontoTraspaso,
								UFTraspaso					: value.UFTraspaso,
								PorcentajeTraspaso			: value.PorcentajeTraspaso,					
								Descripcion					: value.Descripcion,							
								IdEstadoEnvioNotificacion	: "NEV",
								IdEstadoSolicitud			: "",
								IdEstado					: "PDI",
								IdCargaMasiva				: null,
								/*Usuario*/
								IdEmpleado					: value.IdEmpleado === undefined ? 0 : value.IdEmpleado,
								IdPropositoPortafolio       : "",
								IdMatrizAgrupadorAPV        : 0,
								CodigoAgrupador             : "",
								IdInterno					: i
						};

						var tablaRegimenes = {
								CodigoAgrupador             : value.CodigoAgrupador === undefined ? "" : value.CodigoAgrupador.toUpperCase(),
								IdMatrizAgrupadorAPV        : value.IdMatrizAgrupador === undefined ? 0 : value.IdMatrizAgrupador,
								OrigenAhorro 				: value.OrigenAhorro.toUpperCase(),
							    IdPortafolio				: value.IdPortafolio,
							    IdPropositoPortafolio       : value.IdPropositoPortafolio.toUpperCase(),
							    MesInicio					: value.MesInicio,
							    MesTermino					: value.MesTermino,
							    MontoPesos					: value.MontoPesos,
								MontoUF 					: value.MontoUF,
								Pago						: value.Pago.toUpperCase(),
								PagoNombre					: value.Pago.toUpperCase() === 'UNI' ? 'UNICO' : (value.Pago.toUpperCase() === 'IND' ? 'INDEFINIDO' : 'TEMPORAL'),
								PorcentajeRegimenImponible	: value.PorcentajeRegimenImponible,
								Producto					: value.Producto === undefined ? "" : value.Producto,
							    Regimen						: value.Regimen.toUpperCase(),
							    IdInterno					: i
						};
						
						/*Traspasos*/
						if(value.D1_NombreEntidadTraspaso !== undefined && value.D1_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D1_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D1_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D1_NombreEntidadTraspaso,
								DocumentoEntidad : value.D1_DocumentoEntidadTraspaso,
								Fondo : value.D1_Fondo,
								MontoTraspaso : value.D1_MontoPesosTraspaso,
								UFTraspaso : value.D1_MontoUFtraspaso,
								PorcentajeTraspaso : value.D1_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D2_NombreEntidadTraspaso !== undefined && value.D2_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D2_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D2_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D2_NombreEntidadTraspaso,
								DocumentoEntidad : value.D2_DocumentoEntidadTraspaso,
								Fondo : value.D2_Fondo,
								MontoTraspaso : value.D2_MontoPesosTraspaso,
								UFTraspaso : value.D2_MontoUFtraspaso,
								PorcentajeTraspaso : value.D2_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D3_NombreEntidadTraspaso !== undefined && value.D3_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D3_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D3_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D3_NombreEntidadTraspaso,
								DocumentoEntidad : value.D3_DocumentoEntidadTraspaso,
								Fondo : value.D3_Fondo,
								MontoTraspaso : value.D3_MontoPesosTraspaso,
								UFTraspaso : value.D3_MontoUFtraspaso,
								PorcentajeTraspaso : value.D3_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D4_NombreEntidadTraspaso !== undefined && value.D4_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D4_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D4_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D4_NombreEntidadTraspaso,
								DocumentoEntidad : value.D4_DocumentoEntidadTraspaso,
								Fondo : value.D4_Fondo,
								MontoTraspaso : value.D4_MontoPesosTraspaso,
								UFTraspaso : value.D4_MontoUFtraspaso,
								PorcentajeTraspaso : value.D4_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D5_NombreEntidadTraspaso !== undefined && value.D5_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D5_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D5_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D5_NombreEntidadTraspaso,
								DocumentoEntidad : value.D5_DocumentoEntidadTraspaso,
								Fondo : value.D5_Fondo,
								MontoTraspaso : value.D5_MontoPesosTraspaso,
								UFTraspaso : value.D5_MontoUFtraspaso,
								PorcentajeTraspaso : value.D5_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D6_NombreEntidadTraspaso !== undefined && value.D6_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D6_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D6_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D6_NombreEntidadTraspaso,
								DocumentoEntidad : value.D6_DocumentoEntidadTraspaso,
								Fondo : value.D6_Fondo,
								MontoTraspaso : value.D6_MontoPesosTraspaso,
								UFTraspaso : value.D6_MontoUFtraspaso,
								PorcentajeTraspaso : value.D6_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						if(value.D7_NombreEntidadTraspaso !== undefined && value.D7_NombreEntidadTraspaso !== ''){
							var tablaDetalleOrigenTraspaso = {
								IdTraspaso : 0,
								IdDetalleTraspaso : 0,
								CodigoTipoAhorro : value.D7_CodigoTipoAhorro,
								IdEntidadRecaudadora : value.D7_IdEntidadRecaudadoraTraspaso,
								NombreEntidad : value.D7_NombreEntidadTraspaso,
								DocumentoEntidad : value.D7_DocumentoEntidadTraspaso,
								Fondo : value.D7_Fondo,
								MontoTraspaso : value.D7_MontoPesosTraspaso,
								UFTraspaso : value.D7_MontoUFtraspaso,
								PorcentajeTraspaso : value.D7_PorcentajeTraspaso,
								IdEstado : "VIG",
								IdInterno : i
							};
							detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
						}
						
						/*Traspaso*/
						
						if(value.D1_Instrumento !== undefined && value.D1_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D1_Instrumento,
									RazonSocial  				: value.D1_Serie,
									Nemotecnico 				: value.D1_Serie,
									MontoPesos 					: value.D1_MontoPesos === undefined ? 0 : Number(value.D1_MontoPesos),
									Porcentaje 					: value.D1_Porcentaje === undefined ? 0 : Number(value.D1_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D2_Instrumento !== undefined && value.D2_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D2_Instrumento,
									RazonSocial  				: value.D2_Serie,
									Nemotecnico 				: value.D2_Serie,
									MontoPesos 					: value.D2_MontoPesos === undefined ? 0 : Number(value.D2_MontoPesos),
									Porcentaje 					: value.D2_Porcentaje === undefined ? 0 : Number(value.D2_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D3_Instrumento !== undefined && value.D3_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D3_Instrumento,
									RazonSocial  				: value.D3_Serie,
									Nemotecnico 				: value.D3_Serie,
									MontoPesos 					: value.D3_MontoPesos === undefined ? 0 : Number(value.D3_MontoPesos),
									Porcentaje 					: value.D3_Porcentaje === undefined ? 0 : Number(value.D3_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D4_Instrumento !== undefined && value.D4_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D4_Instrumento,
									RazonSocial  				: value.D4_Serie,
									Nemotecnico 				: value.D4_Serie,
									MontoPesos 					: value.D4_MontoPesos === undefined ? 0 : Number(value.D4_MontoPesos),
									Porcentaje 					: value.D4_Porcentaje === undefined ? 0 : Number(value.D4_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D5_Instrumento !== undefined && value.D5_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D5_Instrumento,
									RazonSocial  				: value.D5_Serie,
									Nemotecnico 				: value.D5_Serie,
									MontoPesos 					: value.D5_MontoPesos === undefined ? 0 : Number(value.D5_MontoPesos),
									Porcentaje 					: value.D5_Porcentaje === undefined ? 0 : Number(value.D5_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D6_Instrumento !== undefined && value.D6_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D6_Instrumento,
									RazonSocial  				: value.D6_Serie,
									Nemotecnico 				: value.D6_Serie,
									MontoPesos 					: value.D6_MontoPesos === undefined ? 0 : Number(value.D6_MontoPesos),
									Porcentaje 					: value.D6_Porcentaje === undefined ? 0 : Number(value.D6_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D7_Instrumento !== undefined && value.D7_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D7_Instrumento,
									RazonSocial  				: value.D7_Serie,
									Nemotecnico 				: value.D7_Serie,
									MontoPesos 					: value.D7_MontoPesos === undefined ? 0 : Number(value.D7_MontoPesos),
									Porcentaje 					: value.D7_Porcentaje === undefined ? 0 : Number(value.D7_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D8_Instrumento !== undefined && value.D8_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D8_Instrumento,
									RazonSocial  				: value.D8_Serie,
									Nemotecnico 				: value.D8_Serie,
									MontoPesos 					: value.D8_MontoPesos === undefined ? 0 : Number(value.D8_MontoPesos),
									Porcentaje 					: value.D8_Porcentaje === undefined ? 0 : Number(value.D8_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D9_Instrumento !== undefined && value.D9_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D9_Instrumento,
									RazonSocial  				: value.D9_Serie,
									Nemotecnico 				: value.D9_Serie,
									MontoPesos 					: value.D9_MontoPesos === undefined ? 0 : Number(value.D9_MontoPesos),
									Porcentaje 					: value.D9_Porcentaje === undefined ? 0 : Number(value.D9_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};	
							distribucion.push(tablaDistribucion);
						}
						if(value.D10_Instrumento !== undefined && value.D10_Instrumento !== ''){
							var tablaDistribucion = {
									FolioContratoSAAPV 			: value.FolioContratoSAAPV,
									IdInstrumento 				: value.D10_Instrumento,
									RazonSocial  				: value.D10_Serie,
									Nemotecnico 				: value.D10_Serie,
									MontoPesos 					: value.D10_MontoPesos === undefined ? 0 : Number(value.D10_MontoPesos),
									Porcentaje 					: value.D10_Porcentaje === undefined ? 0 : Number(value.D10_Porcentaje),
									IdEstado 					: 'VIG',
									IdInterno					: i		
							};
							distribucion.push(tablaDistribucion);
						}
						contratos.push(contenidoContrato);
						regimenes.push(tablaRegimenes);

						i++;
					});

					var parametros = {				
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdModulo  : "APV",
							p_Archivo 	:[{
											IdEmpresa			: $scope.SesionIdEmpresa,
											IdCargaMasiva		: null,
											NombreCargaMasiva	: data.fileName,
											CantidadRegistros	: data.fileData.length,		
											IdUsuario			: $scope.SesionIdUsuario,
											IdTipoCarga			: 1,
											Descripcion			: "",
											FechaCarga			: $scope.SesionFechaSistema,
											IdEstado			: 'VIG'							
										  }],
							p_ParametrosContratos				: contratos,
							p_DistribucionInversion 			: distribucion,
							p_PortafoliosModificados			: regimenes,
							p_DetalleOrigenTraspaso				: detalleOrigenTraspasos
					};
				
					//LLAMAR CARGA CONTRATOS POR SU ID
					bcsServiciosAPV.CargaMasivaArchivo(parametros).$promise.then(function(respuesta){
						bcsComunes.setConflictos(util.parseServerResponse(respuesta));
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
						if(respuesta.P_OUT_RESULTADO.length > 0){
							bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga } ,false,false,$scope.winID);							
						}
						gesint.ConsultarCargaArchivoMasiva();
			   		},function(respuesta){
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
			   			$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					});
				}
			}); 
		};

		gesint.cargaCartola = function(){
			var datosArchivo = {codigoArchivo: 'CARGA_CARTOLAS_APV', titulo: $translate.instant('CARGA_PREVIRED_BANCO')};
			var id1 = bcsComunes.ventanaAbrir("CargarArchivoCartola", datosArchivo, false, false, idVentana);
		}
		gesint.cargaDepositos = function(){
			var datosArchivo = {codigoArchivo: 'CARGA_RECAUDACION_APV', titulo: $translate.instant('CARGA_DEPOSITOS_APV')};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false, idVentana);
			
			var promesa=bcsComunes.ventanaPromesa(id1);
			promesa.then(function(data){
				console.log(data);
				var contenidoArchivo= [];
					$.each(data.fileData,function(key,value){
						var filaDeposito = {
								IdEmpresa    				: $scope.SesionIdEmpresa,
								IdRecaudacion				: 0,
								FolioCartola				: 0,
								LineaCartola				: 0,
								Sucursal					: value.Sucursal,
								FechaDeposito				: value.FechaDeposito,
								CuentaCorriente				: value.CuentaCorriente,
								MontoDeposito				: value.MontoDeposito,
								Observacion				    : "CARGA MASIVA",
								NumeroDocumento				: value.NumeroDocumento,
								ConceptoRecaudacion			: value.ConceptoRecaudacion,
								IdBanco 					: value.Banco,
								IdTipoDeposito				: value.TipoDeposito,
								IdEstado     				: 'ING',
								Origen	 					: 'CARGA',
								IdCarga 					: 0,
								
						};
						contenidoArchivo.push(filaDeposito);
					});
					var parametros = {				
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdModulo  : "APV",
							p_Archivo :[{
								IdEmpresa					: $scope.SesionIdEmpresa,
								NombreCargaMasiva			: data.fileName,
								CantidadRegistros			: data.fileData.length,		
								IdUsuario					: $scope.SesionIdUsuario,
								IdTipoCarga					: 4,
								Descripcion					: "",
								FechaCarga					: $scope.SesionFechaSistema,
								IdEstado					: 'VIG'							}],
							p_ParametrosRecaudacion			: contenidoArchivo,
							};
				
					//LLAMAR CARGA CONTRATOS POR SU ID
					bcsServiciosAPV.CargaRecaudacion(parametros).$promise.then(function(respuesta){
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_IDSCARGAMASIVA[0] > 0){
						bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
					else{
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
						bcsComunes.ventanaOverlay(idVentana,false);
				    }
					gesint.ConsultarCargaArchivoMasiva();
		   		},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
		   			$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
				});
			});
		};
		
		/*
		 * CARGA DATOS PREVIRED
		 */
			gesint.cargaPrevired = function(){
				var datosArchivo = {codigoArchivo: 'CARGA_PREVIRED', titulo: $translate.instant('UI_CARGA_PREVIRED_APV')};
				//LLAMADA CARGA DE ARCHIVOS GRAL
				var id1 = bcsComunes.ventanaAbrir("CargaPrevired", datosArchivo, false, false, idVentana);		
				var promesa=bcsComunes.ventanaPromesa(id1);
			}
		/*
		 * FIN CARGA DATOS PREVIRED
		 */	

		/*
		 * CARGA DATOS PAC
		 */
			gesint.cargaPAC = function(){
				var datosArchivo = {codigoArchivo: 'CARGA_PAC', titulo: $translate.instant('UI_CARGA_PAC_APV')};
				//LLAMADA CARGA DE ARCHIVOS GRAL
				var id = bcsComunes.ventanaAbrir("CargaPAC", datosArchivo, false, false, idVentana);		
				var promesa=bcsComunes.ventanaPromesa(id);
			}
		/*
		 * FIN CARGA DATOS PAC
		 */
		
		//CARGA DATOS SAAPV+PAC
		gesint.cargaSAAPVPAC = function(){
			var datosArchivo = {codigoArchivo: 'CARGA_SAAPVPAC', titulo: $translate.instant('UI_CARGA_SAAPVPAC')};
			var id = bcsComunes.ventanaAbrir("CargaSAAPVPAC", datosArchivo, false, false, idVentana);		
			var promesa=bcsComunes.ventanaPromesa(id);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		gesint.seleccionCarga = undefined;
	        		gesint.ConsultarCargaArchivoMasiva();
				}
	    	});
		}
		
		/*
		 * CARGA DATOS BONIFICACION TCG
		 */
			gesint.cargaBonificacionTGR = function(){
				var datosArchivo = {codigoArchivo: 'CARGA_BONIFICACION_TGR', titulo: $translate.instant('UI_CARGA_BONIFICACION_TGR')};
				//LLAMADA CARGA DE ARCHIVOS GRAL
				var id = bcsComunes.ventanaAbrir("CargaBonificacionTGR", datosArchivo, false, false, idVentana);		
				var promesa=bcsComunes.ventanaPromesa(id);
			}
		/*
		 * FIN CARGA DATOS BONIFICACION TCG
		 */	
		
		/*
		 * CARGA DATOS REGISTRO HISTORICO TRASPASO
		 */
			gesint.CargaRegistroHistoricoTraspaso = function(){
				var datosArchivo = {codigoArchivo: 'CargaRegistroHistoricoTraspaso', titulo: $translate.instant('UI_REG_HIS_APV')};
				//LLAMADA CARGA DE ARCHIVOS GRAL
				var id = bcsComunes.ventanaAbrir("CargaRegistroHistoricoTraspaso", datosArchivo, false, false, idVentana);
				var promesa=bcsComunes.ventanaPromesa(id);
			}
		/*
		 * CARGA DATOS REGISTRO HISTORICO TRASPASO
		 */	
	/*
		 * CARGA RECAUDACION
		 */
			gesint.cargaRecaudacion = function(){
				var datosArchivo = {codigoArchivo: 'CARGA_RECAUDACION', titulo: $translate.instant('UI_CARGA_RECAUDACION')};
				//LLAMADA CARGA DE ARCHIVOS GRAL
				var id = bcsComunes.ventanaAbrir("CargaRecaudacion", datosArchivo, false, false, idVentana);
				var promesa=bcsComunes.ventanaPromesa(id);
			}
		/*
		 * CARGA RECAUDACION
		 */	
	});