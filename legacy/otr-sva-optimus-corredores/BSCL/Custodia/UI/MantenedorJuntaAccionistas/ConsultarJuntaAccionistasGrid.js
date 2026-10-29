angular.module('ModuloCustodia').factory('bcsBlotterConsultaJunta',function(bcsComunes,$translate, $timeout, $http, $rootScope)
{
	var obj = {};
	
	var configuracionColumnas = function() {
		return { 
			IdEventoJuntaAccionista	  	    :{field: "IdEventoJuntaAccionista",		    title: $translate.instant('UI_ID_EVENTO_JUNTA_ACCIONISTA'),		width: "100px",	align: "left"},
			IdEmisor	  	    			:{field: "IdEmisor",		    			title: $translate.instant('UI_EMISOR'),							width: "100px",	align: "left"},
			FechaCierre						:{field: "FechaCierre",						title: $translate.instant('UI_FECHA_CIERRE'),					Width: "115px", align: "left"},
			FechaRealizacion				:{field: "FechaRealizacion",				title: $translate.instant('UI_FECHA_REALIZACION'),				width: "115px", align: "left"},
			NombreEmisor	  				:{field: "NombreEmisor",					title: $translate.instant('UI_NOMBRE_EMISOR'),					width: "150px", align: "left"},
			TipoJunta						:{field: "TipoJunta",						title: $translate.instant('UI_TIPO_JUNTA'),						width: "95px", 	align: "left"},
			Lugar							:{field: "Lugar",							title: $translate.instant('UI_LUGAR'),							width: "120px", align: "left"},
			HoraRealizacion			  		:{field: "HoraRealizacion",					title: $translate.instant('UI_HORA'),							width: "90px", align: "left", 
				filterable: {
                    cell: {
                    	operator: "contains",
						template: function (args) {
							var options = {mask: "00:00:00"}; 
							bcs.MaskedTextBox(args,options);
						}
                    }
                }	
			},
			Materias						:{field: "Materias",						title: $translate.instant('UI_MATERIAS'),						width: "100px",	align: "left"},
			NombreEstadoJunta				:{field: "NombreEstadoJunta",		    	title: $translate.instant('UI_ESTADO_JUNTA_ACCIONISTA'),		width: "75px", 	align: "left"},
			NombreEstadoCustodia			:{field: "NombreEstadoCustodia",		    title: $translate.instant('UI_ESTADO_RESPALDO_CUSTODIA'),		width: "75px", 	align: "left"},
			DocumentoIdentidad				:{field: "DocumentoIdentidad",				title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),			width: "100px", align: "left"},
			NombreCliente					:{field: "NombreCliente",					title: $translate.instant('UI_NOMBRE_CLIENTE'),					width: "175px",	align: "left"},
			Nemotecnico						:{field: "Nemotecnico",						title: $translate.instant('UI_NEMOTECNICO'),					width: "175px",	align: "left"},
			IndicadorMandatoRepresentacion	:{field: "IndicadorMandatoRepresentacion",	title: $translate.instant('UI_INDICADOR_MANDATO'),				width: "50px",	align: "left"},
			CantidadDerecho					:{field: "CantidadDerecho",					title: $translate.instant('UI_CANTIDAD_DERECHO'),				width: "150px",	align: "left", template : " <label> #:   util.formateaNumero(CantidadDerecho, 0) #</label>"},
			TipoInstruccion					:{field: "TipoInstruccion",					title: $translate.instant('UI_TIPO_INSTRUCCION'),				width: "100px",	align: "left"},
			Instruccion						:{field: "Instruccion",						title: $translate.instant('UI_INSTRUCCION'),					width: "200px", align: "left"},
			NombreEstado					:{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),							width: "100px",	align: "left"},
			NombreUsuario					:{field: "NombreUsuario",					title: $translate.instant('UI_USUARIO'),						width: "100px", align: "left"},
			FechaModificacionRegistro		:{field: "FechaModificacionRegistro",		title: $translate.instant('UI_FECHA_MODIFICACION'),				width: "150px", align: "left"},
			Direccion						:{field: "Direccion",						title: $translate.instant('UI_ENVIO_CORRESPONDENCIA'),			width: "150px", align: "left"},
			Correo							:{field: "Correo",							title: $translate.instant('UI_DIRECCION_ELECTRONICA'),			width: "150px", align: "left"},
			NombreEstadoNotificacion		:{field: "NombreEstadoNotificacion",		title: $translate.instant('UI_ESTADO_NOTIFICACION'),			width: "100px", align: "left"},
			NombreEstadoRespuesta			:{field: "NombreEstadoRespuesta",			title: $translate.instant('UI_ESTADO_RESPUESTA'),				width: "100px", align: "left"},
			FechaIngreso					:{field: "FechaIngreso",					title: $translate.instant('UI_FECHA_INGRESO'),					width: "100px", align: "left"},
			CantidadDerechoCalculado		:{field: "CantidadDerechoCalculado",		title: $translate.instant('UI_CANTIDAD_DERECHO_CALCULADO'),		width: "100px", align: "left", template : " <label> #:   util.formateaNumero(CantidadDerechoCalculado, 0) #</label>"},
			Origen							:{field: "Origen",							title: $translate.instant('UI_ORIGEN'),							width: "100px", align: "left"},
			FechaCalculo					:{field: "FechaCalculo",					title: $translate.instant('UI_FECHA_CALCULO'),					width: "100px", align: "left"}
		}     
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.ingresarCerrarVentana = function(id, idPadre){
		 if(id){
			 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $timeout(function() {
		    		 bcsComunes.ventanaCerrar(idPadre);
		    	 },20);
		     }); 
		 }else{
			 bcsComunes.ventanaCerrar(idPadre); 
		 }
	 }
	
	obj.dataSourceConsultaJunta = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdEventoJuntaAccionista", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3168",
						dataType: "json"
					}
				},
				schema : { 
					model: {
						fields : {
							IdEventoJuntaAccionista			: { type : "number"},
							IdEmisor          	    		: { type : "string"},
							FechaCierre 	  	  			: { type : "date"},
							FechaRealizacion				: { type : "date"},
							NombreEmisor 					: { type : "string"},
							TipoJunta						: { type : "string"},
							IdTipoJuntaAccionista			: { type : "string"},
							Lugar							: { type : "string"},
							HoraRealizacion					: { type : "string"},
							NombreEstadoJunta				: { type : "string"},
							NombreEstadoCustodia    		: { type : "string"},
							IdEstadoCustodia				: { type : "string"},
							IdEstadoJunta					: { type : "string"},
							Instruccion						: { type : "string"},
							CuadraturaDCV					: { type : "number"},
							Materias						: { type : "string"},
							FechaCierreEditable				: { type : "string"},
							IdUsuario						: { type : "number"},
							IdEmpresa						: { type : "number"},
							TotalCantidadDerecho			: { type : "number"},
							TotalCantidadDerechoCalculado 	: { type : "number"}
						}
					}
				}
			},
		});
	
	obj.dataSourceConsultarCustodiaCierreJuntaAccionista = new bcs.DataSource({ 
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "DocumentoIdentidad", dir: "desc" },
			pageSize : 200,
			transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3170",
					dataType: "json"
				}
			},
			schema : { 
				model: {
					fields : {
						IdCliente          					: { type : "number"},
						DocumentoIdentidad 	  	  			: { type : "string"},
						NombreCliente						: { type : "string"},
						IndicadorMandatoRepresentacion		: { type : "string"},
						CantidadDerecho						: { type : "number"},
						IdTipoInstruccion					: { type : "string"},
						TipoInstruccion						: { type : "string"},
						Instruccion							: { type : "string"},
						NombreEstado						: { type : "string"},
						IdEstado							: { type : "string"},
						IdUsuario							: { type : "number"},
						NombreUsuario						: { type : "string"},
						FechaModificacionRegistro			: { type : "datetime"},
						IdInstrumento						: { type : "number"},
						Nemotecnico							: { type : "string"},
						IdClienteConDerechoJuntaAccionista	: { type : "number"},
						IdEventoJuntaAccionista				: { type : "number"},
						IdEmisor							: { type : "string"},
						IdSocioDeNegocio					: { type : "number"},
						CodigoISIN							: { type : "string"},
						CantidadDerechoCalculado			: { type : "number"},
						FechaCalculo						: { type : "datetime"},
						Origen								: { type : "string"}
					}
				}
			}
		},
		
	});
	
	obj.dataSourceConsultarNominasJuntaAccionista = new bcs.DataSource({ 
		type : "odata",
		definition : {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			sort: { field: "NombreCliente", dir: "asc" },
			pageSize : 200,
			transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3172",
					dataType: "json"
				}
			},
			schema : { 
				model: {
					fields : {
						IdCliente          					: { type : "number"},
						IdClienteNominaJuntaAccionista		: { type : "number"},
						IdEventoJuntaAccionista				: { type : "number"},
						IdEstadoNotificacion				: { type : "string"},
						NombreEstadoNotificacion			: { type : "string"},
						IdEstadoRespuesta					: { type : "string"},
						NombreEstadoRespuesta				: { type : "string"},
						FechaIngreso						: { type : "sysdate"},
						FechaIngresoRegistro				: { type : "datetime"},
						NombreCliente						: { type : "string"},
						Direccion							: { type : "string"},
						Correo								: { type : "string"},
						IdUsuario							: { type : "number"},
						NombreUsuario						: { type : "string"},
						IdDireccionElectronicaPrincipal		: { type : "number"},
						IdDireccionFisicaPrincipal			: { type : "number"},
						FechaCierre							: { type : "date"},
						FechaRealizacion					: { type : "date"}
					}
				}
			}
		},
		
	});
	
	obj.verInstructivo = function(evento) {
		var dataTemplate = {};		
		if(typeof evento === 'undefined' || evento.length < 1) {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBES_SELECCIONAR_UNA_JUNTA_ACCIONISTA"));
			return;
		}
		var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'JuntAcc' },
		                       { field: "IdEmpresa",	operator: "eq", value: $rootScope.SesionIdEmpresa },
		                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
			if(ConsultaArchivoFondoMutuo.view().length > 0) {
				dataTemplate.TipoReporte	= ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				dataTemplate.IdReporte		= ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				dataTemplate.NombreReporte	= ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
				
				var parametros =  { "p_IdEmpresa"		: $rootScope.SesionIdEmpresa,
									"p_IdModulo"		: 'CUS',
									"p_IdUsuario"		: $rootScope.SesionIdUsuario,
									"p_IdEvento"        : evento[0].IdEventoJuntaAccionista,
		                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									  };
				
				dataTemplate.data= {  "serviceId"   :	ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				
				dataTemplate.parameters= [];
				var subrep = new Array();
				dataTemplate.subReports=subrep;
				
				getReport(dataTemplate);
			}
		});
	};
	
	var ConsultaArchivoFondoMutuo = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3178",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoCustodia		: {type: "string"},
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
	
	var getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": dataTemplate.parameters
				},
				"subReports": dataTemplate.subReports
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$rootScope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($rootScope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($rootScope.winID,false);
			});
	};
	
	obj.gridConsultarCustodiaCierreJuntaAccionista = {
			columns: [
			           obj.getConfiguracionColumna("RutCliente",{ groupable: true})
			          ,obj.getConfiguracionColumna("NombreCliente",{ groupable: true})
			          ,obj.getConfiguracionColumna("Nemotecnico",{ groupable: true})
			          ,obj.getConfiguracionColumna("IndicadorMandatoRepresentacion",{ groupable: true})
			          ,obj.getConfiguracionColumna("CantidadDerecho",{ groupable: true})
			          ,obj.getConfiguracionColumna("TipoInstruccion",{ groupable: true})
			          ,obj.getConfiguracionColumna("Instruccion",{ groupable: true})
			          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})
			          ,obj.getConfiguracionColumna("NombreUsuario",{ groupable: true})
			          ,obj.getConfiguracionColumna("FechaModificacionRegistro",{ groupable: true})
		    ],
			userSettings:true,
			sortable: true,
     		reorderable: true,
     		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
     		autoBind: true,
     		scrollable: true,
     		height:400,
	
			dataSource: obj.dataSourceConsultarCustodiaCierreJuntaAccionista,
		};
	
	return obj;
	
})