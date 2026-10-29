(function() {
	angular.module('ModuloNormativa').controller('PanelNominaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma, bcsServicioInterfaces){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLCP 		= this;
		pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		pLCP.win		= {};
		pLCP.grilla		= {};
		pLCP.cmbbx		= {};
		pLCP.filtro		= {};
		pLCP.fn			= {};
		
		pLCP.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pLCP.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		pLCP.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		
		pLCP.TipoArchivo			= 'pdf';
	    pLCP.IdVersionEsquema 	= 'V1';
		pLCP.IdTipoDocumento = 'NOTCIR1985';
		pLCP.IdTipoDocumentoCarta='NOTCIR1985';
		pLCP.IdVersionEsquemaCarta 	= 'V1';
		pLCP.modulo='NOR';
		pLCP.salidaMensaje='';
		$scope.dataTemplate	= {};
		pLCP.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pLCP.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLCP.grilla.Nomina = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290671",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								   IdEvento				: { type: "number"},
								   IdCliente				: { type: "number"},
								   CodigoPortafolio			: { type: "string"},
								   NombreCompleto			: { type: "string"},
								   DireccionElectronica		: { type: "string"},
								   DireccionFisica			: { type: "string"},
								   Procesado	    : { type: "string"},
								   FechaCreacion				: { type: "string"},
								   FechaEjecucion			: { type: "string"},
                                   UIFechaEjecucion			: { type: "string"},
								   DocumentoIdentidad		: { type: "string"}
								   
							}
						}
					}
				}
			}),
			columns: [
			        {field: "UIFechaEjecucion",				title: $translate.instant("UI_FECHA_EJECUCION"),			width: "80px"	},
				    {field: "DocumentoIdentidad",			title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),		width: "60px"	},
					{field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),					width: "60px"	},
					{field: "NombreCompleto",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DireccionFisica",				title: $translate.instant("UI_ENVIO_CORRESPONDENCIA"),		width: "140px"	},
					{field: "DireccionElectronica",			title: $translate.instant("UI_DIRECCION_ELECTRONICA"),		width: "140px"	},
					{field: "Procesado",			title: $translate.instant("UI_ESTADO_NOTIFICACION"),		width: "100px"	},
					{field: "FechaCreacion",					title: $translate.instant("UI_FECHA_NOTIFICACION"),				width: "60px"	}
			],
			hideDetailButton: function(row) {
				var hide = true;
				//if(row.IdTipoLimite === 'LIMCTPEXCLUIREMISOR' || row.IdTipoLimite === 'LIMCTPMARGENTASAS') 
				//	hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 540,//580,
		    //selectable 	: "row",
		    selectable 			: "multiple row",
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
		pLCP.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pLCP.fn.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pLCP.Periodo);
			pLCP.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pLCP.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pLCP.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		
				//Funcion Obtener version del Esquema
		pLCP.fnVersionEsquema = function() {
			if (pLCP.Periodo !=undefined)
				{
						var fd = util.dateSPdate(pLCP.Periodo);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.IdTipoDocumento;
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
								pLCP.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		pLCP.fnVersionEsquemaCarta = function() {
			if (pLCP.Periodo !=undefined)
				{
						var fd = util.dateSPdate(pLCP.Periodo);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.IdTipoDocumentoCarta;
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
								pLCP.IdVersionEsquemaCarta=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		pLCP.fn.buscar = function() {
			if (!pLCP.FecInicio || !pLCP.FecTermino) {
				bcsComunes.ventanaMsgErr(
					$translate.instant('UI_FECHA_REQUERIDA'), 
					pLCP.idVentana);
				return;
			}
			
			if (pLCP.FecInicio > pLCP.FecTermino) {
				bcsComunes.ventanaMsgErr(
					$translate.instant('UI_FECHA_NO_VALIDA'), 
					pLCP.idVentana);
				return;
			}

			bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise(
				'RANGO_CONSULTA_FECHA_CIRCULAR_1985',
				$scope.SesionIdEmpresa,
				'NORMATIVA'
			).then(function(parametro) {

				var fechaDesde = util.dateInicioDia(pLCP.FecInicio);
				var fechaHasta = util.dateInicioDia(pLCP.FecTermino);
				var diasRango  = Math.floor((fechaHasta - fechaDesde) / (1000 * 60 * 60 * 24));
				var maxDias    = parseInt(parametro.Valor);
				if (diasRango > maxDias) {
					bcsComunes.ventanaMsgErr(
						$translate.instant('UI_RANGO_FECHA_EXCEDIDO'),
						pLCP.idVentana
					);
					return;
				}

				var objfilters  = {};
				var niv1filters = new Array();
				objfilters.filters = niv1filters;

				if (pLCP.FecInicio) {
					pLCP.fn.envioFecha();
					niv1filters.push({ field: "FechaEjecucion", operator: "gte", value: util.dateSPdate(pLCP.FecInicio) });
				}

				if (pLCP.FecTermino) {
					pLCP.fn.envioFecha();
					niv1filters.push({ field: "FechaEjecucion", operator: "lte", value: util.dateSPdate(pLCP.FecTermino) });
				}

				if (pLCP.filtro.IdSocioDeNegocio && pLCP.filtro.IdSocioDeNegocio > 0) {
					niv1filters.push({ field: "IdCliente", operator: "eq", value: pLCP.SocioDeNegocioObj.IdCliente });
				}

				if (niv1filters.length > 0) pLCP.grilla.Nomina.dataSource.filter(objfilters);
				else pLCP.grilla.Nomina.dataSource.filter(new Array());

				pLCP.grilla.limiteSel = undefined;
			});
		};
		
		pLCP.fn.VerCarta = function(seleccionado) {
			
			if (seleccionado !== undefined && seleccionado.length > 1) {
				bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'),pLCP.idVentana);
				return;
			}
			
			if (seleccionado === undefined || seleccionado.length === 0) {
				bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'),pLCP.idVentana);
				return;
			}
			
			pLCP.fnVersionEsquemaCarta();

			if (seleccionado.length === 1)
			{
					var a= seleccionado;
					
								// Obtener  version del tipo documento
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.IdTipoDocumentoCarta},
					                       { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
					                       { field: "Version",  	operator: "eq", value: pLCP.IdVersionEsquemaCarta },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.cLim.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.cLim.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.cLim.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_IdCliente"				: seleccionado[0].IdCliente,
												"p_FechaDesde"				: seleccionado[0].FechaEjecucion,
												"p_FechaHasta"				: seleccionado[0].FechaEjecucion,
												"p_IdPortafolio"            : seleccionado[0].IdPortafolio,
											     "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'CIRRESUMEN.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							$scope.cLim.getReport($scope.dataTemplate);
							
										
						}
					});
					
				
			};
				
		};
		
		pLCP.fn.Notificar= function(seleccionado) {

			if(seleccionado!==undefined && seleccionado.length >=1 )
			{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_REENVIAR_CIRCULAR1985"),pLCP.idVentana);
				t .then(function(result)
			    //t.result.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						var a= seleccionado;
								
						pLCP.fn.envioFecha();
						var registro = [];		
						parametrosSP= {};
						var p_Eventos = [];
						//$scope.pc1.envioFecha();
				    	//Comunes
				    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  pLCP.modulo;
						$.each(seleccionado, function (key, value) {
								p_Eventos.push({p_IdEvento : value.IdEvento, p_IdCliente :  value.IdCliente});
						});
				    	parametrosSP.p_Eventos					= p_Eventos;
						bcsComunes.ventanaOverlay(pLCP.idVentana,true);
						pLCP.salidaMensaje='NOTIFICACION';
						bcsServiciosNormativa.ReprocesarEventoCircular1985(
                                   parametrosSP,
                                   function(respuesta){
                                    bcsComunes.ventanaMsg({
                                            conflictos:util.parseServerResponse(respuesta)
                                        },pLCP.idVentana);
                                             
                                    if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
                                        pLCP.fn.buscar();
                                    }
                                    bcsComunes.ventanaOverlay(pLCP.idVentana, false);
                                }
                               );
                        
						
				       };
				});
			};
					
		};
		
		pLCP.fn.limpiar = function(){

			//pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.grilla.Nomina.dataSource.removeAll();
		};

		var _obtenerClientesFiltrados = function() {
			var dsFiltrado = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					serverPaging: false,
					serverSorting: false,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290671",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdCliente: { type: "number" }
							}
						}
					}
				}
			});

			dsFiltrado.filter(pLCP.grilla.Nomina.dataSource.filter());

			return dsFiltrado.read().then(function() {
				var listaClientes = [];
				angular.forEach(dsFiltrado.data(), function(item) {
					if (item.IdCliente && listaClientes.indexOf(item.IdCliente) === -1) {
						listaClientes.push(item.IdCliente);
					}
				});
				return listaClientes;
			});
		};

		pLCP.fn.GenerarCartas = function() {
		
		// Validar que las fechas estén definidas
		if (!pLCP.FecInicio || !pLCP.FecTermino) {
			bcsComunes.ventanaMsgErr(
				$translate.instant('UI_FECHA_REQUERIDA'),
				pLCP.idVentana);
			return;
		}

		//Validar que haya data en la grilla
		var totalRegistros = pLCP.grilla.Nomina.dataSource.total();
		if (totalRegistros === 0) {
			bcsComunes.ventanaMsgErr(
				$translate.instant('UI_SIN_REGISTROS_PARA_GENERAR'),
			    pLCP.idVentana);
			return;
		}

		// Obtener parametro de rango máximo de fechas
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise(
			'RANGO_CONSULTA_FECHA_CIRCULAR_1985', 
			$scope.SesionIdEmpresa, 
			'NORMATIVA'
		).then(function(parametro) {

			// Validar rango de fechas
			var fechaDesde  = util.dateInicioDia(pLCP.FecInicio);
			var fechaHasta  = util.dateInicioDia(pLCP.FecTermino);
			var diasRango   = Math.floor((fechaHasta - fechaDesde) / (1000 * 60 * 60 * 24));
			var maxDias     = parseInt(parametro.Valor);

			if (diasRango > maxDias) {
				bcsComunes.ventanaMsgErr(
					$translate.instant('UI_RANGO_FECHA_EXCEDIDO')
								.replace('::MaxDias', maxDias),
				    pLCP.idVentana);
				return;
			}

			_obtenerClientesFiltrados().then(function(listaClientes) {

				if (listaClientes.length === 0) {
					bcsComunes.ventanaMsgErr(
						$translate.instant('UI_SIN_REGISTROS_PARA_GENERAR'),
						pLCP.idVentana);
					return;
				}

				// Armar parámetros del job
				var parametros = [{
					IdInterno      : 0,
					NombreParametro: 'IdUsuario',
					Valor          : String($scope.SesionIdUsuario)
				}, {
					IdInterno      : 0,
					NombreParametro: 'IdEmpresa',
					Valor          : String($scope.SesionIdEmpresa)
				}, {
					IdInterno      : 0,
					NombreParametro: 'FechaDesde',
					Valor          : kendo.toString(pLCP.FecInicio, 'yyyy-MM-dd')
				}, {
					IdInterno      : 0,
					NombreParametro: 'FechaHasta',
					Valor          : kendo.toString(pLCP.FecTermino, 'yyyy-MM-dd')
				}, {
					IdInterno      : 0,
					NombreParametro: 'IdClientes',
					ValorBlob      : listaClientes.join(',')
				}];

				var datosJob = {
					p_IdEmpresa   : $scope.SesionIdEmpresa,
					p_IdUsuario   : $scope.SesionIdUsuario,
					p_IdModulo    : 'NOR',
					p_TipoPrograma: [{
						IdInterno      : 0,
						IdTipoPrograma : 'GenerarCartasMasivasCIR1985',
						SegundosDesfase: 0,
						Comentario     : 'Generar Cartas Masivas Circular 1985'
					}],
					p_Parametro: parametros
				};

				bcsComunes.ventanaOverlay(pLCP.idVentana, true);

				var success = function(respuesta) {
					bcsComunes.ventanaOverlay(pLCP.idVentana, false);
					if (respuesta.P_OUT_ERRORESCONTADOR == '0') {
						var fechaLocal = util.dateDS(
							respuesta.P_OUT_PROGRAMA[0].FechaEjecucion, 'FechaHora', 'CALL'
						);
						bcsComunes.ventanaMsg({
							mensaje   : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO')
											.replace('::IdPrograma',     respuesta.P_OUT_PROGRAMA[0].IdPrograma)
											.replace('::FechaEjecucion', util.formatoSecondate(fechaLocal)),
							conflictos: util.parseServerResponse(respuesta)
						}, pLCP.idVentana);
					} else {
						bcsComunes.ventanaMsg({
							mensaje   : $translate.instant('UI_ERROR_INGRESO'),
							conflictos: util.parseServerResponse(respuesta)
						}, pLCP.idVentana);
					}
				};

				var error = function(respuesta) {
					bcsComunes.ventanaOverlay(pLCP.idVentana, false);
					bcsComunes.ventanaMsg({
						mensaje   : $translate.instant('UI_ERROR_INGRESO'),
						conflictos: util.parseServerResponse(respuesta)
					}, pLCP.idVentana);
				};

				bcsServicioInterfaces.InscribirProcesoBatch(datosJob, success, error);

			}, function() {
				bcsComunes.ventanaMsgErr(
					$translate.instant('UI_ERROR_INGRESO'),
					pLCP.idVentana);
			});
		});
	};

	pLCP.fn.NotificarTodos = function() {
		// Validar fechas
		if (!pLCP.FecInicio || !pLCP.FecTermino) {
			bcsComunes.ventanaMsgErr(
				$translate.instant('UI_FECHA_REQUERIDA'),
			    pLCP.idVentana);
			return;
		}

		// Validar que haya data en la grilla
		var totalRegistros = pLCP.grilla.Nomina.dataSource.total();
		if (totalRegistros === 0) {
			bcsComunes.ventanaMsgErr(
				$translate.instant('UI_SIN_REGISTROS_PARA_NOTIFICAR'),
			    pLCP.idVentana);
			return;
		}

		// Validar rango de fechas
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise(
			'RANGO_CONSULTA_FECHA_CIRCULAR_1985',
			$scope.SesionIdEmpresa,
			'NORMATIVA'
		).then(function(parametro) {

			var fechaDesde = util.dateInicioDia(pLCP.FecInicio);
			var fechaHasta = util.dateInicioDia(pLCP.FecTermino);
			var diasRango  = Math.floor((fechaHasta - fechaDesde) / (1000 * 60 * 60 * 24));
			var maxDias    = parseInt(parametro.Valor);

			if (diasRango > maxDias) {
				bcsComunes.ventanaMsgErr(
					$translate.instant('UI_RANGO_FECHA_EXCEDIDO')
							.replace('::MaxDias', maxDias),
					pLCP.idVentana);
				return;
			}

			// Confirmar accion
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",
				$translate.instant("UI_REENVIAR_CIRCULAR1985_TODOS"),
				pLCP.idVentana);

			t.then(function(result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					var parametrosSP = {
						p_IdEmpresa  : $scope.SesionIdEmpresa,
						p_IdUsuario  : $scope.SesionIdUsuario,
						p_IdModulo   : pLCP.modulo,
						p_FechaDesde : kendo.toString(pLCP.FecInicio,  'yyyy-MM-dd'),
						p_FechaHasta : kendo.toString(pLCP.FecTermino, 'yyyy-MM-dd'),
						p_IdCliente  : pLCP.SocioDeNegocioObj ? pLCP.SocioDeNegocioObj.IdCliente : null
					};

					bcsComunes.ventanaOverlay(pLCP.idVentana, true);

					bcsServiciosNormativa.NotificarTodosCircular1985(
						parametrosSP,
						function(respuesta) {
							bcsComunes.ventanaOverlay(pLCP.idVentana, false);
							bcsComunes.ventanaMsg({
								conflictos: util.parseServerResponse(respuesta)
							}, pLCP.idVentana);
							if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
								pLCP.fn.buscar();
							}
						},
						function(error) {
							bcsComunes.ventanaOverlay(pLCP.idVentana, false);
							bcsComunes.ventanaMsg({
								mensaje   : $translate.instant('UI_ERROR_INGRESO'),
								conflictos: util.parseServerResponse(error)
							}, pLCP.idVentana);
						}
					);
				}
			});
		});
	};
	
	});
}());