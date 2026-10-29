angular.module('ModuloTesoreria').controller('BlotterPACCtrl',
		function($rootScope,$scope,$modal,$state,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria,$http)
{
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	var obj = {};
	$scope.formData = {};
	$scope.filtros = {};
    $scope.datos = {};
    var winID = bcsComunes.ventanaWinId($scope);
  	
	
	var buscar = function(apiGrid,filtros) {
		apiGrid.dataSource.filter(obj.validarFiltros(filtros,'PAC'));
		
		$scope.datos.kNgModel = false;
	}

	$scope.buscar = buscar;
	
	//$scope.filtros.fecha = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fecha  = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.cliente = false;
		$scope.filtros.IdSocioDeNegocio = undefined;
		
		$scope.datos.kNgModel = false;
	};
	
	obj.validarFiltros = function(filtros,modo)
	{
		var filters = new Array();
		filters.push({ field: "IdEmpresa", operator: "eq", value: $rootScope.SesionIdEmpresa});
		if(modo == 'PAC'){
			if(filtros.fecha){
				filters.push({field: "Fecha", operator: "eq", value: util.dateOdata(filtros.fecha)});
			}
			if(filtros.socioNegocio){
				filters.push({field: "IdCliente", operator: "eq", value: filtros.socioNegocio.IdCliente});
			}
		}
		return filters;
	}
	
	
	$scope.AnulacionContratoPAC = function(vOpcion)
	{
		if (!$scope.datos.kNgModel || $scope.datos.kNgModel.length != 1) {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_REGISTRO"));
			return;
		}
		
		if ($scope.datos.kNgModel[0].EstadoContrato == "CERRADO") {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CONTRATO_SE_ENCUENTRA_CERRADO"));
			return;
		}
		
		if(vOpcion === "CER")
		{
			var datosIngresoPAC = {
				p_IdEmpresa : $scope.SesionIdEmpresa, 
				p_IdUsuario : $scope.SesionIdUsuario, 
				p_IdModulo : 'TES',
				p_Contrato : [{
					IdContrato : $scope.datos.kNgModel[0].IdMandatoPAC
				}]
			}
			
			bcsServiciosTesoreria.AnulacionMandatoPAC(datosIngresoPAC).$promise.then(function(respuesta){
				// console.log(respuesta);
				bcsComunes.ventanaOverlay($scope.winID,true);
				var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREACION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
				$scope.ApiMovimientos.dataSource.ds.read();
			},function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
		}
	}
	

	$scope.verComprobante = function() {
		
		bcsComunes.ventanaOverlay(winID,true);
		
		var filterTemplate	= [
			{ field: "CodigoTemplateTesoreria", operator: "eq", value: 'PACMAN' },
			{ field: "IdEstado", operator: "eq", value: 'ACT' },
			{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
		];
		
		consultarTemplateTesoreria.query({ filter: filterTemplate }).then(function(){
			if(consultarTemplateTesoreria.view().length > 0) {
				var parametros = {
					"p_IdEmpresa"			: $scope.SesionIdEmpresa,
					"p_IdMandatoPAC"		: $scope.datos.kNgModel[0].IdMandatoPAC,
					"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
				};
				
				$scope.dataTemplate.IdReporte		= consultarTemplateTesoreria.view()[0].IdReporte;
				$scope.dataTemplate.NombreReporte	= consultarTemplateTesoreria.view()[0].NombreReporte;
				$scope.dataTemplate.FormatoSalida	= consultarTemplateTesoreria.view()[0].FormatoSalida;
				$scope.dataTemplate.dataSP			= { "serviceId": consultarTemplateTesoreria.view()[0].IdServicio, "serviceData": parametros };
				$scope.dataTemplate.subReports = new Array();
				
				$scope.dataTemplate.IdMandatoPAC =  $scope.datos.kNgModel[0].IdMandatoPAC;
			
				$scope.getReport($scope.dataTemplate);
			}
		});	
			 
	}
	
	$scope.RevocacionContratoCSC = function(vOpcion)
	{
		if (!$scope.datos.kNgModel || $scope.datos.kNgModel.length != 1) {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_REGISTRO"));
			return;
		}
		
		if ($scope.datos.kNgModel[0].EstadoContrato == "CERRADO") {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CONTRATO_SE_ENCUENTRA_CERRADO"));
			return;
		}
		
		if(vOpcion === "REV")
		{
			var datosIngresoPAC = {
				p_IdEmpresa : $scope.SesionIdEmpresa, 
				p_IdUsuario : $scope.SesionIdUsuario, 
				p_IdModulo : 'TES',
				p_Mandato : [{
					IdMandato : $scope.datos.kNgModel[0].IdMandatoPAC
				}]
			}
			
			bcsServiciosTesoreria.RevocacionMandatoPac(datosIngresoPAC).$promise.then(function(respuesta){
				// console.log(respuesta);
				bcsComunes.ventanaOverlay($scope.winID,true);
				var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREACION_CONTRATO"),conflictos:util.parseServerResponse(respuesta)},winID);
				$scope.ApiMovimientos.dataSource.ds.read();
			},function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)}, winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});

		}
	}
	
	$scope.ingresarMandatoPAC = function(Solicitud)
	{
		if (!$scope.datos.kNgModel || $scope.datos.kNgModel.length != 1) {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_REGISTRO"));
			return;
		}
		
		if ($scope.datos.kNgModel[0].EstadoContrato == "CERRADO") {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_CONTRATO_SE_ENCUENTRA_CERRADO"));
			return;
		}
		
		if (Solicitud == 'EDITAR')
		{
			if ($scope.datos){
				var id = bcsComunes.ventanaAbrir("IngresarMandatoPAC", {IdTipoSolicitud: 'Editar',  ObjetoPAC: $scope.datos.kNgModel[0]} );
				var promesa=bcsComunes.ventanaPromesa(id);                               
			    $.when(promesa).then(function(data){
			    	 $scope.ApiMovimientos.dataSource.ds.read();
			    });
				return;
			}else{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_FILA_NO_SELECCIONADA"));
				bcsComunes.ventanaOverlay($scope.winID,false);
				return;
			}
		}
		bcsComunes.ventanaAbrir("IngresarMandatoPAC", {});
	};


	$scope.defaultOptions = {
		dataSource: new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdMandatoPAC", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=601002",
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdMandatoPAC					: { type : "number"},
							DocumentoIdentidad			    : { type : "string"},
							//IdPortafolio			        : { type : "number"},
							NombreCompleto				    : { type : "string"},
							FolioCSC						: { type : "string"},
							FolioSAAPV						: { type : "string"},
							EstadoContrato					: { type : "string"},
							Receptor						: { type : "string"},
							Fecha						    : { type : "sysdate"},
							NumeroMandato					: { type : "number"},
							Firma							: { type : "string"},
							Estado							: { type : "string"},
							NombrePlaza			            : { type : "string"},
							NombreMoneda		            : { type : "string"},
							NombreTipoCuentaBancaria        : { type : "string"},
							NombreBanco      			    : { type : "string"},
							NumeroCuentaBancaria      	    : { type : "string"},
							IdEstado                 	    : { type : "string"},
							EstadoPortafolio                : { type : "string"},
							EstadoCliente                	: { type : "string"},
							Canal                			: { type : "string"},
							EstadoCargo                		: { type : "string"},
							MontoPesos                		: { type : "number"},
							MontoUF                			: { type : "number"},
							TotalMontoPesos                	: { type : "number"},
							TotalMontoUF                	: { type : "number"},
							FolioPacMandatoAgrupador        : { type : "string"},
							IdPersona					    : { type : "number"},
							DiaDescuento                    : { type : "number"},
						}
					}
				}			
			}
		}),
		columns: [
			    {field : "IdMandatoPAC",             title:'UI_ID_MANDATO_PAC'			,width :"150px"}
		 	   ,{field : "DocumentoIdentidad",     	 title:'UI_DOCUMENTO_IDENTIDAD'		,width :"150px"}
		 	   ,{field : "NombreCompleto",     	     title:'UI_NOMBRE_COMPLETO'			,width :"150px"}
		 	   ,{field : "FolioCSC",                 title:'UI_FOLIO_CSC'				,width :"150px"}
		 	   ,{field : "FolioSAAPV",               title:'UI_FOLIO_SAAPV'				,width :"150px"}
		 	   ,{field : "EstadoContrato",           title:'UI_ESTADO_CONTRATO'			,width :"150px"}
		 	   ,{field : "Receptor",                 title:'UI_RECEPTOR'				,width :"150px"}
		 	   ,{field : "Fecha",                    title:'UI_FECHA'					,width :"150px"}
		 	   ,{field : "NumeroMandato",            title:'UI_NUMERO_MANDATO'			,width :"150px"}
		 	   ,{field : "Firma",                    title:'UI_FIRMA'					,width :"150px"}
		 	   ,{field : "Estado",                   title:'UI_ESTADO'					,width :"150px"}
		 	   ,{field : "NombrePlaza",              title:'UI_NOMBRE_PLAZA'			,width :"150px"}
		 	   ,{field : "NombreMoneda",             title:'UI_NOMBRE_MONEDA'			,width :"150px"}
		 	   ,{field : "NombreTipoCuentaBancaria", title:'UI_NOMBRE_TIPO_CUENTA_BANCARIA'	,width :"150px"}
		 	   ,{field : "NombreBanco",              title:'UI_NOMBRE_BANCO'			,width :"150px"}
		 	   ,{field : "NumeroCuentaBancaria",     title:'UI_NUMERO_CUENTA_BANCARIA'	,width :"150px"}
		 	   ,{field : "EstadoPortafolio",         title:'UI_ESTADO_PORTAFOLIO'		,width :"150px"}
		 	   ,{field : "EstadoCliente",            title:'UI_ESTADO_CLIENTE'			,width :"150px"}
		 	   ,{field : "Canal",            		 title:'UI_CANAL'					,width :"150px"}
		 	   ,{field : "EstadoCargo",              title:'UI_ESTADO_CARGO'			,width :"150px"}
		 	   ,{field : "MontoPesos",               title:'UI_MONTO_PESOS'				,width :"150px"}
		 	   ,{field : "MontoUF",             	 title:'UI_MONTO_UF'				,width :"150px"}
		 	   ,{field : "FolioPacMandatoAgrupador", title:'UI_FOLIO_PAC_MANDATO_AGRUPADOR'	,width :"150px"}
		 	   ,{field : "DiaDescuento"            , title:'UI_DIA_DESCUENTO'			,width :"150px"}
		],	
		userSettings:true,
		sortable: true,
  		reorderable: true,
  		filterable: true,
		groupable: true,
		pageable: true,
		selectable: "multiple, row",
		reorderable: true,
		
		gridCreated     : function(g){
			$scope.ApiMovimientos = g;
		},
		allowCopy: true,
		resizable: true,
  		autoBind: true,
  		scrollable: true,
  		height:530,
	};
	
	
	//Datasource para obtener configuracion de reportes
	var consultarTemplateTesoreria = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering: true,
			transport: {
				read: {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",
					dataType : "json",
				}
			},
			schema: {
				model: {
					fields: {
						IdReporte : {type : "string"},
						IdServicio : {type : "number"},
						NombreReporte : {type : "string"},
						FormatoSalida : {type : "string"}
					}
				}
			}
		}
	});
	

	$scope.ingresarCSC = function()
	{
		var id = bcsComunes.ventanaAbrir("IngresarMandatoPAC", {} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
	    $.when(promesa).then(function(data){
	    	$scope.ApiMovimientos.dataSource.ds.read();
	    });
	};
	

	
	///-------------------------------------------------///
	
	$scope.CargaMasiva = function()
	{
		bcsComunes.ventanaAbrir("CargaMasivaTesoreria", {} );
	};
	//
	
	$scope.cargarArchivoUni = function(){
		var datosArchivo = {codigoArchivo: 'CARGA_UNIVERSO_PAC' || ''};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);				
		 var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 var cargaMasiva = [];
			 var cargaMasivaDetalle = [];			 
			 var idRegistro = 1;
			 
			 
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  NombreArchivo	        : data.fileName,
					  CodigoTipoCargaMasiva : 'CARGA_UNIVERSO_PAC' || ''
			 });
			 $.each(data.fileData, function(key, value){
				 cargaMasivaDetalle.push({CodigoBanco: value.UniCodigoBanco.toString()		
			                            ,CodigoEmpresa: value.UniCodigoEmpresa.toString()	
			                            ,CodigoConvenio: value.UniCodigoConvenio.toString()		
									    ,TipoRegistro: value.UniTipoRegistro.toString()
									    ,IdentServicio: value.UniIdentServicio.toString()
									    ,Filler: value.UniFiller.toString()		
									    ,FechaGeneracion: value.UniFechaGeneracion.toString()
									    ,FechaInicioServ: value.UniFechaInicioServ.toString()
									    ,Filler: value.UniFiller.toString()});	
				    idRegistro++;
			 });	
			
		
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay(idVentana, true);

						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'TES';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosTesoreria.CrearCargaMasivaPac(parametros,
							function(respuesta){
								bcsComunes.ventanaOverlay(idVentana, false);
								 		
								if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
										conflictos:util.parseServerResponse(respuesta)
									},idVentana);
								} else {
									bcsComunes.ventanaOverlay(idVentana, false);
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},idVentana);
								}
							},
							function(respuesta){
								bcsComunes.ventanaOverlay(idVentana, false);
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
								bcsComunes.ventanaOverlay(idVentana, false);
							}
						);
					}
			 });
						 

 		}); 
	};
	$scope.cargarArchivoRen = function(){
		
		var datosArchivo = {codigoArchivo: 'CARGA_RENDICION_PAC' || ''};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);				
		var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 var cargaMasiva = [];
			 var cargaMasivaDetalle = [];			 
			 var idRegistro = 1;
			 
			 
			 cargaMasiva.push({
			    	  IdInterno             : 1,
			    	  NombreArchivo	        : data.fileName,
					  CodigoTipoCargaMasiva : 'CARGA_RENDICION_PAC' || ''
			 });
			 $.each(data.fileData, function(key, value){
				 cargaMasivaDetalle.push({  CodigoBanco: value.RendCodBanco.toString(),	
										    CodigoEmpresa: value.RendCodEmpresa.toString(),
										    CodigoConvenio: value.RendCodConvenio.toString(),		
										    TipoRegistro: value.RendTipoRegistro.toString(),
										    IdentServicio: value.RendIdServicio.toString(), 
										    Filler1: value.RendFiller1.toString(),  			
										    MontoCargo: value.RendMontoCargo.toString(),
										    FechaVencimiento: value.RendFechaVencimiento.toString(),
										    FechaCargo: value.RendFechaCargo.toString(),
										    EstadoCargo: value.RendEstadoCargo.toString(),
										    CodigoRechazo: value.RendCodigoRechazo.toString(),
										    InfAdicional: value.RendInfAdicional.toString(),
										    Filler: value.RendFiller.toString()})	                                                            
				    idRegistro++;
			 });	

	
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay(idVentana, true);
						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'TES';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
						parametros.p_simular					= 'S';
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosTesoreria.CrearCargaMasivaPacRen(parametros,
							function(respuesta){

							
								bcsComunes.ventanaOverlay(idVentana, false);
								bcsComunes.ventanaAbrir("ConsultarRendicion",{Solicitudes: respuesta.P_OUT_SOLICITUDES, Parametros: parametros},false,false,idVentana); 
								return true;
								
							},
							function(respuesta){
								bcsComunes.ventanaOverlay(idVentana, false);
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
								bcsComunes.ventanaOverlay(idVentana, false);
							}
						);
					}
			 });
						 

 		}); 
	};
	$scope.ingresarPAC = function()
	{
		bcsComunes.ventanaAbrir("IngresarPAC", {} );
	};
	//--------------------------------------------------------------------------------------------
	// Solicita generación de archivo de transfefrencias bancarias (Inscripción en JobManager) 
	//--------------------------------------------------------------------------------------------		
	$scope.generarArchivo = function(filtros) { 
		//if (filtros.seleccionTransferencias && filtros.seleccionTransferencias.length > 0){

			var dataCols = [];
			
		
			dataCols.push(
					{
						IdNominaTransferencia : 1,
						IdBanco: 1,
						IdMoneda: 'CLP',
						IdMedioDePago: 'TRA',
						IdTipoTarea: 'GENERAR_ARCHIVO_CARGOS',
						IdAgenda: 0,
						Segundos: 1
					});
			 
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_SOLICITARA_GENERACION_ARCHIVO_PAGO') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay(idVentana, true);
					$scope.btnActivo = false;

					//DATOS SERVICIO
					var datos = {};
					datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario               = $scope.SesionIdUsuario; 
					datos.p_IdModulo                = 'TES';
					datos.p_UsaReglasNegocio        = 'S';
					datos.p_ArchivosAGenerar	    = dataCols;

					bcsComunes.log().info(JSON.stringify(datos));
					
					bcsServiciosTesoreria.SolicitudGenerarArchivoPac(datos).$promise.then(
					
							function(result) {
								bcsComunes.ventanaOverlay(idVentana,false);
								$scope.btnActivo = true;
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(result)
								},idVentana);
								
							},
							function(result){
								bcsComunes.ventanaOverlay(idVentana,false);
								$scope.btnActivo = true;								
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_SOLICITUD_GENERACION_ARCHIVO_ERROR"),
									conflictos:util.parseServerResponse(result)
								},idVentana);
							});
					
				}
			});
		/*}
		else {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")
			},idVentana);			
		}*/
		return;
	};
	
	
	//Reporte Carta de Notificación PAC
	$scope.dataTemplate= {};
	$scope.generarCartaBanco = function(datosPAC){
		var filterTemplate	= [
			{ field: "CodigoTemplateTesoreria", operator: "eq", value: 'RCNPAC' },
			{ field: "IdEstado", operator: "eq", value: 'ACT' },
			{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
		];
		
		consultarTemplateTesoreria.query({ filter: filterTemplate }).then(function(){
			if(consultarTemplateTesoreria.view().length > 0) {
				var parametros = {
					"P_IDEMPRESA"			: $scope.SesionIdEmpresa,
					"P_IDMANDATOPAC"		: datosPAC[0].IdMandatoPAC,
					"P_IDPERSONACORREDORA"	: $scope.SesionIdPersonaCorredora,
					"$schema"				: [{ "field" : "LogoCorredora", "type" : "blob" }]
				};
				
				$scope.dataTemplate.IdReporte		= consultarTemplateTesoreria.view()[0].IdReporte;
				$scope.dataTemplate.NombreReporte	= consultarTemplateTesoreria.view()[0].NombreReporte;
				$scope.dataTemplate.FormatoSalida	= consultarTemplateTesoreria.view()[0].FormatoSalida;
				$scope.dataTemplate.dataSP			= { "serviceId": consultarTemplateTesoreria.view()[0].IdServicio, "serviceData": parametros };
				$scope.dataTemplate.subReports = new Array();
				
				$scope.dataTemplate.IdMandatoPAC = datosPAC[0].IdMandatoPAC;
			
				$scope.getReport($scope.dataTemplate);
			}
		});
	};
	
	$scope.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.FormatoSalida,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: "",
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.dataSP,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports 
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = dataTemplate.NombreReporte+'_'+dataTemplate.IdMandatoPAC+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.FormatoSalida);
				bcsComunes.ventanaOverlay(winID,false);
			})
			.error(function(response) {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
					conflictos: { errores: [{ Codigo : 'COD', Descripcion : 'Un error inesperado ha ocurrido' }] }
				},idVentana);
				bcsComunes.ventanaOverlay(winID,false);
			});
	};
	
	$scope.abrirVentanaCargo = function()
	{
		bcsComunes.ventanaAbrir("GeneraArchivoCargoPAC", {} );
	};
	
	var firstHeightCrear = $('#Win_BlotterPAC');
        firstHeightCrear.css('height', '720px');
		firstHeightCrear.css('width', '1050px');
});