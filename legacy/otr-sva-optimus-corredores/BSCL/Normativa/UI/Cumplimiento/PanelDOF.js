(function() {
	angular.module('ModuloNormativa').controller('PanelDOFCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,bcsNormativaDOF,
				 bcsAlerta, OPCIONES_BCSALERTA,$http){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pct 		= this;
		pct.idVentana	= bcsComunes.ventanaWinId($scope);
		pct.win		= {};
		pct.grilla		= {};
		pct.cmbbx		= {};
		pct.filtro		= {};
		pct.fn			= {};
		
		pct.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pct.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		pct.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		
		pct.TipoArchivo			= 'pdf';
	    pct.IdTipoDocumentoComprobante='UAFDOFCOMP';
		pct.IdVersionEsquemaComprobante 	= '';
		pct.modulo='NOR';
		pct.salidaMensaje='';
		pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.dataTemplate	= {};
		var filters = {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		pct.dsDetalleDOF = function(){
			return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1277",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						IdDeclaracionOrigenFondos 		: { type: "number"},
						IdCliente						: { type: "number"},
					} }
				}
				}
			})
		};
		
		pct.initDetalle = function(e) {
			bcsComunes.ventanaOverlay(pct.idVentana,true);
			console.log("Inicializa el detalle!",e);
			var filterDetalleDOF = [
			    { field: "IdCliente", operator: "eq", value: e.data.IdCliente },
				{ field: "IdDeclaracionOrigenFondos", operator: "eq", value: e.data.IdDeclaracionOrigenFondos }
			];
			var dsDet = pct.dsDetalleDOF();
			dsDet.query({filter:filterDetalleDOF}).then(function() {
				var datos = dsDet.data();
				if (datos.length > 0){
					console.log(datos);
					e.data.detalleDOF = datos;
				}
				bcsComunes.ventanaOverlay(pct.idVentana,false);
			});
		};
				
		pct.grilla.DOF = {
			dataSource: new bcs.DataSource({
				//type: "odata",
				type : "call",
				definition: {
					serverFiltering : false, // Filtrado de datos en el servidor.
					serverPaging 	: false, // Paginado de los datos en el servidor.
					serverSorting 	: false, // Ordenamiento de los datos en el servidor.
					sortable 		: false,
					pageSize 		: 100,
					callParameters      : filters,
					callTransaction 	: "3076",
					callResultSet 		: "P_OUT_TABLADOF",
					schema: {
						model: {
							fields: {
								   FechaLiquidacion					: { type: "date"},
								   NombreCompleto					: { type: "string"},
								   RazonSocial						: { type: "string"},
								   ApellidoPaterno					: { type: "string"},
								   ApellidoMaterno					: { type: "string"},
								   MontoOperacion					: { type: "number"},
								   TipoOperacion					: { type: "string"},
								   IdComprobante					: { type: "number"},
								   MontoComprobanteFormaDePago		: { type: "number"},
								   MonedaFormaDePago				: { type: "string"},
								   NombreMedioDePago				: { type: "string"},
								   Comentario						: { type: "string"},
								   IdDeclaracionOrigenFondos		: { type: "number"},
								   IdMonedaFormaDePago				: { type: "string"},
								   Declaracion						: { type : "string"},
								   DeclaracionUI					: { type : "code"}
							}
						}
					}
				}
			}),
			columns: [
				    {field: "FechaLiquidacion",				title: $translate.instant("UI_FECHA"),					    width: "70px"	},
					{field: "NombreCompleto",				title: $translate.instant("UI_NOMBRE"),						width: "200px"	},
					{field: "TipoOperacion",				title: $translate.instant("UI_TIPO_OPERACION"),				width: "130px"	},
					{field: "IdComprobante",				title: $translate.instant("UI_COMPROBANTE"),			    width: "50px"	},
					{field: "NombreMedioDePago",			title: $translate.instant("UI_MEDIO_DE_PAGO"),				width: "100px"	},
					{field: "MonedaFormaDePago",			title: $translate.instant("UI_MONEDA"),						width: "80px"	},
					{field: "MontoComprobanteFormaDePago",	title: $translate.instant("UI_MONTO"),	template: "<label> #: (util.bcs_formato(MontoComprobanteFormaDePago,'ORD','MONTO',IdMonedaFormaDePago,'','')).valor #</label>",							width: "80px"	},
					{field: "DeclaracionUI",				title: $translate.instant("UI_DECLARACION"),			    width: "70px"	},
					{field: "Comentario",					title: $translate.instant("UI_COMENTARIO"),					width: "100px"	},
					
					
			],
			hideDetailButton: function(row) {
				var hide = true;
				if(row.Declaracion === 'S') 
					hide = false;
				return hide;
			},
		    rowCellColor : function(row) {
		    	var column = new Array();
		    	if (row.get("Declaracion")=='S'){column.push({column: "DeclaracionUI", color: "green"});};
		    	if (row.get("Declaracion")=='N'){column.push({column: "DeclaracionUI", color: "red"});};
		    	return column;
		    },
			pageable: {
                refresh: true,
                buttonCount: 5
            },
			
			pageSize	: 50,
			//autoBind	: true,
			autoBind	: false,
			height		: 540,//580,
		    selectable 	: "row",
		    //selectable 			: "multiple row",
		    detailInit : function(e) {
			    	pct.initDetalle(e);},
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pct.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pct.fn.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pct.Periodo);
			pct.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pct.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pct.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		

		pct.fnVersionEsquemaComprobante = function(seleccionado) {
			if (seleccionado.FechaLiquidacion !=undefined)
				{
						var fd = util.dateSPdate(seleccionado.FechaLiquidacion);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pct.IdTipoDocumentoComprobante;
						if (fd !== null || fd !== undefined) {
							paramServicio.p_Fecha = fd;
						}
						else 
						{
							paramServicio.p_Fecha = $scope.SesionFechaSistema;
						}
						
						bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
							if (respuesta.P_OUT_VERSIONESQUEMA)
							{
								pct.IdVersionEsquemaComprobante=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
							}
							else
							{	
								bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
							}		
							
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});	
				};	
		};	
		
		
		pct.fn.buscar = function(){
				//Datos grillas	
			var filters = {};
			filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
			filters.p_FechaOperacionDesde	= util.dateSPdate(pct.FecInicio) || '0001-01-01';
			filters.p_FechaOperacionHasta	= util.dateSPdate(pct.FecTermino) || '0001-01-01';
			//filters.p_Monto					= 0;		

			if(pct.IdAgente>0){
	    		filters.p_IdEmpleado			= pct.IdAgente;
	    	};
			if(pct.IdSucursal>0){
	    		filters.p_IdSucursal			= pct.IdSucursal;
	    	};
			if(pct.SocioDeNegocioObj.IdCliente && pct.SocioDeNegocioObj.IdCliente > 0){
	    		filters.p_IdCliente= pct.SocioDeNegocioObj.IdCliente;
			};
			
			//dsData.setCallParameters(filters);
			pct.grilla.DOF.dataSource.setCallParameters(filters);
				    	
		};
		pct.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
						VersionArchivo			: {type: "string"},
						Esquema					: {type: "string"},
						Version 				: {type: "string"},
						IdTipoDocumento 		: {type: "string"},
						Descripcion				: {type: "string"},
						NombreArchivo			: {type: "string"},
						IdArchivo				: {type: "string"},
						TipoArchivo				: {type: "string"},
						ServicioArchivo			: {type: "string"},
						IdEmpresa				: {type: "number"},
						IdEstado				: {type: "string"}						
					}
				}
			}
		},
		});	
		
		
		pct.getReport = function(dataTemplate){
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
				"subReports": dataTemplate.subReports
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
		};
		
		
		pct.fn.imprimirComprobante = function(seleccionado) {
				
			if(seleccionado!==undefined)
			{	   
			   		//pct.fnVersionEsquemaComprobante(seleccionado);
			
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pct.IdTipoDocumentoComprobante },
					                       { field: "TipoArchivo",  operator: "eq", value: pct.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pct.IdVersionEsquemaComprobante },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					pct.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if(pct.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= pct.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= pct.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= pct.ConsultaArchivoNormativa.view()[0].NombreArchivo;
						//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_Fecha"					: util.dateSPdate(seleccionado.FechaLiquidacion),
												"p_IdCliente"				: seleccionado.IdCliente ,
												"p_Seccion"					: "OPE",
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'CI63Carta.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	pct.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							
								//SUB-REPORTE : DECLARACION
							var parametros1 =  { "p_IdEmpresa"					: $scope.$root.SesionIdEmpresa,
												 "p_IdDeclaracionOrigenFondos"	: seleccionado.IdDeclaracionOrigenFondos,
												 "p_Seccion"					: "DOF"
  											  };
							
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;	
							
							subrep.push({"reportName" : "DOFDETALLE",
									                  "data" : {
										                  		"serviceId" : pct.ConsultaArchivoNormativa.view()[0].ServicioArchivo
										                  		,"serviceData" :parametros1 
										                       }
									            });						
							
							
							
							pct.getReport($scope.dataTemplate);
							
										
						}
					});
					
				}else{
					pct.salidaMensaje=$translate.instant("UI_DEBE_SELECCIONAR_UNA_FILA");
					bcsComunes.msg("error","info", pct.salidaMensaje);
					//bcsComunes.ventanaMsg({ mensaje : pct.salidaMensaje, 
					//			 conflictos : ""}, pct.idVentana);

				};
		 };	
		
		 pct.fn.SolicitarDeclaracion= function(seleccionado) {
			
			 	var parametros = {	 IdEmpresa			: $scope.$root.SesionIdEmpresa
			 						,IdUsuario			: $scope.SesionIdUsuario
			 						,IdModulo			: pct.modulo
			 						,IdTipoOperacion	: seleccionado.IdTipoOperacion
			 						,IdCliente			: seleccionado.IdCliente
			 						,MontoOperacion 	: seleccionado.MontoOperacion
									,IdMonedaOperacion	: seleccionado.IdMonedaOperacion
									,IdMedioDePago		: seleccionado.IdMedioDePago
									,FechaOperacion		: util.dateSPdate(seleccionado.FechaLiquidacion)//'YYYY-MM-DD'
								 };
			// var  obj = new bcsNormativaDOF();
			  
			bcsNormativaDOF.evaluarOperacion(pct.idVentana,parametros);
				
		
				        
			
		 };	

		
		$scope.$watch('pct.grilla.DOFSel', function(newval,oldval){
			
			if (angular.equals(newval,oldval)) return;
			if (newval!==false) {
				if(newval !== 'undefined')
				{
					pct.fnVersionEsquemaComprobante(newval);
				}
			}
			
			
		},true);
		
				
		pct.fn.limpiar = function(){
			
			
			pct.grilla.DOF.dataSource.removeAll();
		};
		

	});
}());