angular.module('ModuloNormativa', [ 'ui.bootstrap', 'ui.utils', 'ui.router', 'ngAnimate' ]);
angular.module('ModuloNormativa').factory('Model', ['$http', '$resource', Model]);
angular.module('ModuloNormativa').factory('ModelFactory', ['$http', '$resource', function($http, $resource) {
    return function() {
        return new Model($http, $resource);
    };
}]);

angular.module('ModuloNormativa').service('ModelService', function() {
    var model = null;

    this.setModel = function(Model) {
        model = Model;
    };

    this.getModel = function() {
        return model;
    };
});

angular.module('ModuloNormativa').factory('Utils', ['bcsComunes', Utils]);

angular.module('ModuloNormativa').service('FileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl, args, success, error) {
        if (file == null) {
            throw 'Debe seleccionar un archivo';
        }

        var fd = new FormData();
        for (var key in args) {
            var val = args[key];
            if (val != null) fd.append(key, val);
        }

        fd.append('file', file);
        $http.post(uploadUrl, fd, {
            transformRequest: angular.identity,
            headers: { 'Content-Type' : undefined, 'enctype' : 'multipart/form-data' }
        })
        .success(success)
        .error(error);
    }
}]);

angular.module('ModuloSocioNegocio').factory('bcsConfiguracionNormativa',function($translate, $rootScope, $q) {

	var obj = {};
	
	var CONFIGURACION = {};
	CONFIGURACION.defer = $q.defer();
	CONFIGURACION.promise = CONFIGURACION.defer.promise;
	
	var cargarConfiguracion = function() {
		var ds = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				filter : { field : "CodigoParametro", operator : "eq", value : "ConfiguracionCircularizacionCustodia"},
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3071",
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							CodigoParametro : { type : "string"},
							Descripcion 	: { type : "string"},
							Valor 			: { type : "string"},
						}
					}
				}
			}
		});
		
		var ds2 = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true,
				filter : { field : "CodigoParametro", operator : "eq", value : "NotificarNominaCircAgrupadoPorCliente"},
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3071",
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							IdEmpresa 		: { type : "number"},
							CodigoParametro : { type : "string"},
							Descripcion 	: { type : "string"},
							Valor 			: { type : "string"},
						}
					}
				}
			}
		});
		
		ds.fetch().then(function(){
			var data = ds.data();
			$.each(data,function(key,value){
				CONFIGURACION[value.CodigoParametro] = value.Valor;
			})
			CONFIGURACION.defer.resolve();
		},function() {
			console.error("ERROR AL CARGAR CONFIGURACION NORMATIVA");
		});
		
		ds2.fetch().then(function(){
			var data2 = ds2.data();
			$.each(data2,function(key,value){
				CONFIGURACION[value.CodigoParametro] = value.Valor;
			})
			CONFIGURACION.defer.resolve();
		},function() {
			console.error("ERROR AL CARGAR CONFIGURACION NORMATIVA");
		});
	};
	cargarConfiguracion();
	
	obj.CONFIGURACION = CONFIGURACION;
	 
	return obj;
	
});


angular.module('ModuloNormativa').factory('bcsConfGrillaNormativa',function($translate, $rootScope, $q) {

	var obj = {};
	
	var configuracionColumnas = function() {
		return {
			IdSolicitudAporteRescate	  	:{field: "IdSolicitudAporteRescate",		title: $translate.instant('UI_SECUENCIA'),								width: "100px",	align: "left"},
			FolioMandato					:{field: "FolioMandato",					title: $translate.instant('UI_FOLIO_MANDATO'),							width: "100px",	align: "left"},
			Valor						  	:{field: "Valor",							title: $translate.instant('UI_VALOR'),									width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Valor,'FMM','Monto','CLP', '','')).valor #</label>"},
			Regimen					  		:{field: "Regimen",							title: $translate.instant("UI_REGIMEN"),					width: "100px"	},
			Observacion					  	:{field: "Observacion",						title: $translate.instant("UI_COMENTARIO"),					width: "100px"	},
		}     
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.getSchemaMLT = function() {
		return { 
			model: {
				fields : {
					IdSolicitudAporteRescate	: { type : "number"},
					FolioMandato	   			: { type : "number"},
					Valor  						: { type : "number"},
					Regimen						: { type : "string"},
					Observacion					: { type : "string"},
				}
			}
		}
	};
	

	obj.getDataSourceValorCuotaArray = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : obj.getSchemaMLT()			
			}
		})
	};
	
	obj.getMLT = function(){
		var config = {};
		config = {
				options : {
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
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

					dataSource: obj.getDataSourceValorCuotaArray(),
				
					columns: [
						obj.getConfiguracionColumna("IdSolicitudAporteRescate"),
						obj.getConfiguracionColumna("FolioMandato"),
						obj.getConfiguracionColumna("Valor"),
						obj.getConfiguracionColumna("Regimen"),
						obj.getConfiguracionColumna("Observacion")
					]
				}
		};
		return config;
	};
	 
	return obj;
	
});



angular.module('ModuloNormativa').factory('bcsCartolas', ['$http', 'Model', 'FileUpload', function($http, Model, FileUpload) {

    var MODULO = 'CAR';
    var UPLOAD_URL = '/BSCL/Plataforma/GWJAVA/PLA/cartolas/subirImagen';
    var GENERAR_URL = '/BSCL/Plataforma/GWJAVA/PLA/cartolas/generar';
    
    var CONSULTAR_CARTOLAS_NOMINA = 101010383;
    var CONSULTAR_CARTOLAS_PORTAFOLIO = 600063;
    var CONSULTAR_CONFIG = 3131;
    var CONSULTAR_NOMINAS = 3101;
    var CONSULTAR_PARAMETROS = 3107;
    var CONSULTAR_PLANTILLAS = 3102;
    var CONSULTAR_RECURSOS = 3110;

    var COPIAR_PLANTILLA = 3105;
    var GRABAR_CONFIG = 3113;
    var GRABAR_NOMINA = 3104;
    var GRABAR_RECURSO = 3111;

    var ACTUALIZAR_VALOR_RECURSO = 3117;
    var ACTUALIZAR_VIGENCIA_PLANTILLA = 3115;
    var ACTUALIZAR_REPROCESO = 3120;

    var _bcsCartolas = {};

    _bcsCartolas.getDataSourceConfig = function(idEmpresa) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_CONFIG,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverPaging : true,
                serverSorting : true,
    			sort : {
    				field : "Descripcion",
    				dir : "asc"
    			},
                pageSize : 1000,
                filter : [ { field : 'IdEmpresa', operator : 'eq', value : idEmpresa } ],
                schema : {
                    model : {
                        fields : {
                            IdConfig : { type : 'number' },
                            IdEmpresa : { type : 'number' },
                            Descripcion : { type : 'string' },
                            Tipo : { type : 'string' },
                            Nombre : { type : 'string' },
                            Valor : { type : 'string' }
                        }
                    }
                }
            }
        });
    }

    var columnasConfig = [
        { field : 'Descripcion', title : 'Descripcion', width : '160px' },
        { field : 'Tipo', title : 'Tipo', width : '50px' },
        { field : 'Valor', title : 'Valor' }
    ];

    _bcsCartolas.getConfiguracionGrillaConfig = function(dataSource) {
        return {
            dataSource : dataSource,
            columns : columnasConfig,
            allowCopy : true,
            autoBind : true,
            userSettings : true,
            navigatable : true,
            excel : { allPages : true },
            filterable : true,
            height : 340,
            pageable : true,
            reorderable : true,
            resizable : true,
            scrollable : true,
            selectable : "row",
            sortable : true
        };
    }
                        
    var fieldsCartolas = {
        IdEmpresa : { type : 'number' },
        IdCartola : { type : 'number' },
        IdNomina : { type : 'number' },
        IdCliente : { type : 'number' },
        IdPortafolio : { type : 'number' },
        CodigoPortafolio : { type : 'string' },
        Periodo : { type : 'string' },
        Reprocesar : { type : 'string' },
        ReprocesarUI : { type : 'code' },
        Notificar : { type : 'string' },
        EstadoProceso : { type : 'string' },
        MensajeProceso : { type : 'string' },
        DocumentoIdentidad : { type : 'string' },
        NombreCliente : { type : 'string' },
        DireccionElectronica : { type : 'string' },
        NombreEstado : {type : 'string'}
    }

    _bcsCartolas.getDataSourceCartolas = function (idNomina) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_CARTOLAS_NOMINA,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverPaging : true,
                serverSorting : true,
                pageSize : 250,
                filter : [ { field : 'IdNomina', operator : 'eq', value : idNomina } ],
                schema : { model : { fields : fieldsCartolas } }
            }
        });
    }

    var columnasCartolas = [
        { field : 'IdCartola', title : 'Sec. Cartola', width : '100px' },
        { field : 'DocumentoIdentidad', title : 'Doc. Identidad', width : '100px' },
        { field : 'NombreCliente', title : 'Nombre Cliente', width : '100px' },
        { field : 'CodigoPortafolio', title : 'Código Portafolio', width : '100px' },
        { field : 'NombreEstado', title : 'Estado', width : '100px' },
        { field : 'ReprocesarUI', title : 'Reprocesar', width : '50px' },
        { field : 'Notificar', title : 'Notificar', width : '50px' },
        { field : 'DireccionElectronica', title : 'Direccion Electronica', width : '100px' },
        { field : 'MensajeProceso', title : 'Mensaje Proceso', width : '100px' }
    ];

    _bcsCartolas.getConfiguracionGrillaCartolas = function(dataSource) {
        return {
            dataSource : dataSource,
            columns : columnasCartolas,
            allowCopy : true,
            autoBind : true,
            userSettings : true,
            navigatable : true,
            excel : { allPages : true },
            filterable : true,
            height : 500,
            pageable : true,
            reorderable : true,
            resizable : true,
            scrollable : true,
            selectable : "multiple, row",
            sortable : true
        };
    }

    _bcsCartolas.getDataSourcePlantillas = function(idEmpresa) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_PLANTILLAS,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverSorting : true,
                filter : [ { field : 'IdEmpresa', operator : 'eq', value : idEmpresa } ],
                schema : {
                    model : {
                        fields : {
                        	IdPlantilla : { type : 'number' },
                            IdEmpresa : { type : 'number' },
                            Nombre : { type : 'string' },
                            VigenteDesde : { type : 'date' }
                        }
                    }
                }
            }
        });
    }

    _bcsCartolas.getDataSourceNominas = function(idEmpresa) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_NOMINAS,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverPaging : true,
                serverSorting : true,
    			sort : {
    				field : "IdNomina",
    				dir : "desc"
    			},
                pageSize : 20,
                filter : [ { field : 'IdEmpresa', operator : 'eq', value : idEmpresa } ],
                schema : {
                    model : {
                        fields : {
                            IdNomina : { type : 'number' },
                            TipoCartola : { type : 'string'},
                            IdEmpresa : { type : 'number' },
                            IdUsuario : { type : 'number' },
                            IdPlantilla : { type : 'number' },
                            CantidadCartolas : { type : 'number' },
                            CantidadCartolasTerminadas : { type : 'number' },
                            CantidadCartolasErroneas : { type : 'number' },
                            Periodo : { type : 'string' },
                            FechaDesde : { type : 'date' },
                            FechaHasta : { type : 'date' },
                            Comentario : { type : 'string' },
                            Certificada : { type : 'string' },
                            CertificadaUI : { type : 'code' },
                            NombreEstado : { type : 'string' },
                            Ano : { type : 'string' },
                            Mes : { type : 'string' },
                            IncluirPortafoliosConSaldo : { type : 'string' },
                            IncluirPortafoliosConSaldoUI : { type : 'string' },
                            IncluirPortafoliosConCustodia : { type : 'string' },
                            IncluirPortafoliosConCustodiaUI : { type : 'string' },
                            IncluirPortafoliosConMovimientoCuentaInversion : { type : 'string' },
                            IncluirPortafoliosConMovimientoCuentaInversionUI : { type : 'string' },
                            IncluirCategoriaInversionista : { type : 'string' },
                            IncluirCategoriaInversionistaUI : { type : 'string'},
                            CategoriaInversionista : { type : 'string'},
                            IncluirTipoPersona : { type : 'string'},
                            IncluirTipoPersonaUI : { type : 'string'},
                            TipoPersona : { type : 'string'},
                            IncluirTipoCliente : { type : 'string'},
                            IncluirTipoClienteUI : { type : 'string'},
                            TipoCliente : { type : 'string'},
                            IncluirPortafoliosExcluidos : { type : 'string'},
                            IncluirPortafoliosExcluidosUI : { type : 'string'},
                            EnvioAutomatico : { type : 'string'},
							EnvioAutomaticoUI : { type : 'code'},
                            ExcluirPortafoliosIncluidosOtraNomina : { type : 'string'},
                            ExcluirPortafoliosIncluidosOtraNominaUI : { type : 'string'},
                            ExcluirOperadoresDirectos : { type : 'string'},
                            ExcluirOperadoresDirectosUI : { type : 'string'},
                            FechaCertificacion : { type : 'date' },
                            IdUsuarioCertificador : { type : 'number' },
                            NombreUsuarioCertificador : { type : 'string' },
                            NotaCertificacion : { type : 'string' },
                            TipoProceso : { type : 'string' },
                            EstadoProceso : { type : 'string' },
                            FechaProgramadaProceso : { type : 'sysdatetime' },
                            FechaInicioProceso : { type : 'sysdatetime' },
                            FechaTerminoProceso : { type : 'sysdatetime' },
                            MensajeProceso : { type : 'string' },
                            OpcionProceso : { type : 'string' }
                            
                        }
                    }
                }
            }
        });
    }

    var columnasNominas = [
       { field : 'IdNomina', title : 'Secuencia', width : '100px' },
       { field : 'Periodo', title : 'Periodo', width : '100px' },
       { field : 'CantidadCartolas', title : 'Cant. Cartolas', width : '100px' },
       { field : 'CantidadCartolasTerminadas', title : 'Cant. Cartolas Terminadas', width : '100px' },
       { field : 'CantidadCartolasErroneas', title : 'Cant. Cartolas Erroneas', width : '100px' },
       { field : 'Comentario', title : 'Comentario', width : '100px' },
       { field : 'EnvioAutomaticoUI', title : 'UI_ENVIO_AUTOMATICO', width : '100px' },
       { field : 'CertificadaUI', title : 'Certificado', width : '100px' },
       { field : 'FechaCertificacion', title : 'Fecha Certificacion', width : '100px', format : '{0:dd-MM-yyyy}' },
       { field : 'NombreUsuarioCertificador', title : 'Nombre Usuario Certificador', width : '100px' },
       { field : 'NotaCertificacion', title : 'Nota Certificacion', width : '100px' },
       { field : 'TipoProceso', title : 'Tipo Proceso', width : '100px' },
       { field : 'NombreEstado', title : 'Estado Proceso', width : '100px' },
       { field : 'FechaProgramadaProceso', title : 'Fecha Programada Proceso', width : '100px' },
       { field : 'FechaInicioProceso', title : 'Fecha Inicio Proceso', width : '100px' },
       { field : 'FechaTerminoProceso', title : 'Fecha Termino Proceso', width : '100px' },
       { field : 'MensajeProceso', title : 'Mensaje Proceso', width : '200px', attributes : {style : 'white-space:nowrap;'} }
    ];

    _bcsCartolas.getConfiguracionGrillaNominas = function(dataSource) {
        return {
            dataSource : dataSource,
            columns : columnasNominas,
            allowCopy : true,
            autoBind : false,
            userSettings : true,
            navigatable : true,
            excel : { allPages : true },
            filterable : true,
            height : 500,
            pageable : true,
            reorderable : true,
            resizable : true,
            scrollable : true,
            selectable : "row",
            sortable : true
        };
    }

    _bcsCartolas.getDataSourceParametros = function() {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_PARAMETROS,
                        dataType : 'json'
                    }
                },
                serverPaging : true,
                serverSorting : true,
                schema : {
                    model : {
                        fields : {
                            IdParametro : { type : 'number' },
                            IdNomina : { type : 'number' },
                            Nombre : { type : 'string' },
                            Valor : { type : 'string' }
                        }
                    }
                }
            }
        });
    }

    _bcsCartolas.getDataSourceDescargaCartolas = function (idEmpresa) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_CARTOLAS_PORTAFOLIO,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverPaging : true,
                serverSorting : true,
                pageSize : 10,
                filter : [ { field : 'IdEmpresa', operator : 'eq', value : idEmpresa } ],
                schema : { model : { fields : fieldsCartolas } }
            }
        });
    }

    _bcsCartolas.getDataSourceRecursos = function() {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
                transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + CONSULTAR_RECURSOS,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverPaging : true,
                serverSorting : true,
    			sort : {
    				field : "Descripcion",
    				dir : "asc"
    			},
                pageSize : 10,
                schema : {
                    model : {
                        fields : {
                            IdRecurso : { type : 'number' },
                            IdPlantilla : { type : 'number' },
                            Editable : { type : 'string' },
                            Descripcion : { type : 'string' },
                            Tipo : { type : 'string' },
                            Grupo : { type : 'string' },
                            Nombre : { type : 'string' },
                            Valor : { type : 'string' }
                        }
                    }
                }
            }
        });
    }

    var columnasRecursos = [
        { field : 'Grupo', title : 'Grupo', width : '80px' },
        { field : 'Nombre', title : 'Nombre', width : '80px' },
        { field : 'Descripcion', title : 'Descripcion', width : '160px' },
        { field : 'Tipo', title : 'Tipo', width : '50px' },
        { field : 'Valor', title : 'Valor' }
    ];

    _bcsCartolas.getConfiguracionGrillaRecursos = function(dataSource) {
        return {
            dataSource : dataSource,
            columns : columnasRecursos,
            allowCopy : true,
            autoBind : false,
            userSettings : true,
            navigatable : true,
            excel : { allPages : true },
            filterable : true,
            height : 340,
            pageable : true,
            reorderable : true,
            resizable : true,
            scrollable : true,
            selectable : "row",
            sortable : true
        };
    }
    
    function fixDate(date) {
    	if (date === null) return null;
    	if (date.getFullYear && date.getFullYear() < 1900) {
    		return null;
    	}
    	
    	return date;
    }

    _bcsCartolas.gestionarNomina = function(nomina, fnSuccess, fnError) {
		nomina.SegundosEsperaProceso = 0;
		if (nomina.IdNomina === 0) {
			var y = nomina.FechaProgramadaProceso.getTime();
			var x = new Date().getTime();
			nomina.SegundosEsperaProceso = Math.floor((y - x) / 1000);
			if (nomina.SegundosEsperaProceso < 0)
				nomina.SegundosEsperaProceso = 0;
		};

        params = {};
        if(nomina.AccionNomina === 'INGRESAR' || nomina.AccionNomina === 'PROGRAMADO') {
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
            params.p_IdUsuario 				= nomina.IdUsuario;
            params.p_IdModulo 				= MODULO;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_TipoProceso 			= nomina.TipoProceso;
            params.p_Periodo 				= nomina.Periodo;
            params.p_FechaDesde 			= fixDate(nomina.FechaDesde);
            params.p_FechaHasta 			= fixDate(nomina.FechaHasta);
            params.p_SegundosEsperaProceso 	= nomina.SegundosEsperaProceso;
            params.p_Comentario 			= nomina.Comentario;
            params.p_IncluirPortafoliosConSaldo = nomina.IncluirPortafoliosConSaldo ? "S" : "N";
            params.p_IncluirPortafoliosConCustodia = nomina.IncluirPortafoliosConCustodia ? "S" : "N";
            params.p_IncluirPortafoliosConMovimientoCuentaInversion = nomina.IncluirPortafoliosConMovimientosCuentaInversion ? "S" : "N";
            params.p_IncluirCategoriaInversionista = nomina.IncluirCategoriaInversionista ? 'S' : 'N';
            params.p_CategoriaInversionista = nomina.IncluirCategoriaInversionista ? nomina.CategoriaInversionista : '';
            params.p_IncluirTipoCliente = nomina.IncluirTipoCliente ? 'S' : 'N';
            params.p_TipoCliente = nomina.IncluirTipoCliente ? nomina.TipoCliente : '';
            params.p_IncluirTipoPersona = nomina.IncluirTipoPersona ? 'S' : 'N';
            params.p_TipoPersona = nomina.IncluirTipoPersona ? nomina.TipoPersona : '';
            params.p_IncluirPortafoliosExcluidos = nomina.IncluirPortafoliosExcluidos ? "S" : "N";
            params.p_EnvioAutomatico = nomina.EnvioAutomatico ? "S" : "N";
            params.p_ExcluirPortafoliosIncluidosOtraNomina = nomina.ExcluirPortafoliosIncluidosOtraNomina ? "S" : "N";
            params.p_ExcluirOperadoresDirectos = nomina.ExcluirOperadoresDirectos ? "S" : "N";
            params.p_OpcionProceso = nomina.OpcionProceso;
            params.p_CartolaConsolidada = nomina.CartolaConsolidada;
     
            if(nomina.AccionNomina === 'PROGRAMADO'){
            	params.p_Habilitado = nomina.Habilitado;
            	params.p_DiaEjecucion = nomina.DiaEjecucion;
            	params.p_HoraEjecucion = nomina.HoraEjecucion;
            }
        }
        else if(nomina.AccionNomina === 'CERTIFICAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
            params.p_IdUsuario 				= nomina.IdUsuario;
            params.p_IdModulo 				= MODULO;
            params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_NotaCertificacion		= nomina.NotaCertificacion;
        }
        else if(nomina.AccionNomina === 'CANCELAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
        	params.p_IdUsuario 				= nomina.IdUsuario;
        	params.p_IdModulo 				= MODULO;
        	params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
        } else if(nomina.AccionNomina === 'REPROCESAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
        	params.p_IdUsuario 				= nomina.IdUsuario;
        	params.p_IdModulo 				= MODULO;
        	params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_SegundosEsperaProceso  = nomina.SegundosEsperaProceso;
        }
        
        Model.callServiceById(21021, params, fnSuccess, fnError);
    }
    
    _bcsCartolas.gestionarNominaConsolidada = function(nomina, fnSuccess, fnError) {
		nomina.SegundosEsperaProceso = 0;
		if (nomina.IdNomina === 0) {
			var y = nomina.FechaProgramadaProcesoCartolaConsolidada.getTime();
			var x = new Date().getTime();
			nomina.SegundosEsperaProceso = Math.floor((y - x) / 1000);
			if (nomina.SegundosEsperaProceso < 0)
				nomina.SegundosEsperaProceso = 0;
		};

        params = {};
        if(nomina.AccionNomina === 'INGRESAR' || nomina.AccionNomina === 'PROGRAMADO') {
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
            params.p_IdUsuario 				= nomina.IdUsuario;
            params.p_IdModulo 				= MODULO;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_TipoProceso 			= nomina.TipoProceso;
            params.p_Periodo 				= nomina.Periodo;
            params.p_FechaDesde 			= fixDate(nomina.FechaDesde);
            params.p_FechaHasta 			= fixDate(nomina.FechaHasta);
            params.p_SegundosEsperaProceso 	= nomina.SegundosEsperaProceso;
            params.p_Comentario 			= nomina.Comentario;
            params.p_IncluirPortafoliosConSaldo = nomina.IncluirPortafoliosConSaldo ? "S" : "N";
            params.p_IncluirPortafoliosConCustodia = nomina.IncluirPortafoliosConCustodia ? "S" : "N";
            params.p_IncluirPortafoliosConMovimientoCuentaInversion = nomina.IncluirPortafoliosConMovimientosCuentaInversion ? "S" : "N";
            params.p_IncluirCategoriaInversionista = nomina.IncluirCategoriaInversionista ? 'S' : 'N';
            params.p_CategoriaInversionista = nomina.IncluirCategoriaInversionista ? nomina.CategoriaInversionista : '';
            params.p_IncluirTipoCliente = nomina.IncluirTipoCliente ? 'S' : 'N';
            params.p_TipoCliente = nomina.IncluirTipoCliente ? nomina.TipoCliente : '';
            params.p_IncluirTipoPersona = nomina.IncluirTipoPersona ? 'S' : 'N';
            params.p_TipoPersona = nomina.IncluirTipoPersona ? nomina.TipoPersona : '';
            params.p_IncluirPortafoliosExcluidos = nomina.IncluirPortafoliosExcluidos ? "S" : "N";
            params.p_ExcluirPortafoliosIncluidosOtraNomina = nomina.ExcluirPortafoliosIncluidosOtraNomina ? "S" : "N";
            params.p_ExcluirOperadoresDirectos = nomina.ExcluirOperadoresDirectos ? "S" : "N";
            params.p_OpcionProceso = nomina.OpcionProceso;
            params.p_CartolaConsolidada = nomina.CartolaConsolidada;
     
            if(nomina.AccionNomina === 'PROGRAMADO'){
            	params.p_Habilitado = nomina.Habilitado;
            	params.p_DiaEjecucion = nomina.DiaEjecucion;
            	params.p_HoraEjecucion = nomina.HoraEjecucion;
            }
        }
        else if(nomina.AccionNomina === 'CERTIFICAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
            params.p_IdUsuario 				= nomina.IdUsuario;
            params.p_IdModulo 				= MODULO;
            params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_NotaCertificacion		= nomina.NotaCertificacion;
        }
        else if(nomina.AccionNomina === 'CANCELAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
        	params.p_IdUsuario 				= nomina.IdUsuario;
        	params.p_IdModulo 				= MODULO;
        	params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
        } else if(nomina.AccionNomina === 'REPROCESAR'){
        	params.p_IdEmpresa 				= nomina.IdEmpresa;
        	params.p_IdUsuario 				= nomina.IdUsuario;
        	params.p_IdModulo 				= MODULO;
        	params.p_IdNomina 				= nomina.IdNomina;
            params.p_AccionNomina			= nomina.AccionNomina;
            params.p_SegundosEsperaProceso  = nomina.SegundosEsperaProceso;
        }
        
        Model.callServiceById(21021, params, fnSuccess, fnError);
    }
       
    _bcsCartolas.grabarNomina = function(nomina, fnSuccess, fnError) {
		nomina.SegundosEsperaProceso = 0;
		if (nomina.IdNomina === 0) {
			var y = nomina.FechaProgramadaProceso.getTime();
			var x = new Date().getTime();
			nomina.SegundosEsperaProceso = Math.floor((y - x) / 1000);
			if (nomina.SegundosEsperaProceso < 0)
				nomina.SegundosEsperaProceso = 0;
		}

        params = {};
        params.p_IdEmpresa = nomina.IdEmpresa;
        params.p_IdUsuario = nomina.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdNomina = nomina.IdNomina;
        params.p_Periodo = nomina.Periodo;
        params.p_FechaDesde = fixDate(nomina.FechaDesde);
        params.p_FechaHasta = fixDate(nomina.FechaHasta);
        params.p_Comentario = nomina.Comentario;
        params.p_Certificada = nomina.Certificada;
        params.p_FechaCertificacion = fixDate(nomina.FechaCertificacion);
        params.p_IdUsuarioCertificador = nomina.IdUsuarioCertificador;
        params.p_NombreUsuarioCertificador = nomina.NombreUsuarioCertificador;
        params.p_NotaCertificacion = nomina.NotaCertificacion;
        params.p_TipoProceso = nomina.TipoProceso;
        params.p_EstadoProceso = nomina.EstadoProceso;
        params.p_SegundosEsperaProceso = nomina.SegundosEsperaProceso;
        params.p_MensajeProceso = nomina.MensajeProceso;

        Model.callServiceById(GRABAR_NOMINA, params, fnSuccess, fnError);
    }

    _bcsCartolas.copiarPlantilla = function(plantilla, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = plantilla.IdEmpresa;
        params.p_IdUsuario = plantilla.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdPlantilla = plantilla.IdPlantilla;
        params.p_Nombre = plantilla.Nombre;
        params.p_Vigente = plantilla.Vigente;
        params.p_VigenteDesde = plantilla.VigenteDesde;

        Model.callServiceById(COPIAR_PLANTILLA, params, fnSuccess, fnError);
    }

    _bcsCartolas.grabarConfig = function(config, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = config.IdEmpresa;
        params.p_IdUsuario = config.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdConfig = config.IdConfig;
        params.p_Descripcion = config.Descripcion;
        params.p_Nombre = config.Nombre;
        params.p_Tipo = config.Tipo;
        params.p_Valor = config.Valor;

        Model.callServiceById(GRABAR_CONFIG, params, fnSuccess, fnError);
    }

    _bcsCartolas.grabarRecurso = function(recurso, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = recurso.IdEmpresa;
        params.p_IdUsuario = recurso.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdRecurso = recurso.IdRecurso;
        params.p_IdPlantilla = recurso.IdPlantilla;
        params.p_Editable = recurso.Editable;
        params.p_Descripcion = recurso.Descripcion;
        params.p_Grupo = recurso.Grupo;
        params.p_Nombre = recurso.Nombre;
        params.p_Tipo = recurso.Tipo;
        params.p_Valor = recurso.Valor;

        Model.callServiceById(GRABAR_RECURSO, params, fnSuccess, fnError);
    }

    _bcsCartolas.actualizarValorRecurso = function(recurso, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = recurso.IdEmpresa;
        params.p_IdUsuario = recurso.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdRecurso = recurso.IdRecurso;
        params.p_Valor = recurso.Valor;

        Model.callServiceById(ACTUALIZAR_VALOR_RECURSO, params, fnSuccess, fnError);
    }

    _bcsCartolas.actualizarVigenciaPlantilla = function(plantilla, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = plantilla.IdEmpresa;
        params.p_IdUsuario = plantilla.IdUsuario;
        params.p_IdModulo = MODULO;
        params.p_IdPlantilla = plantilla.IdPlantilla;
        params.p_Vigente = plantilla.Vigente;

        Model.callServiceById(ACTUALIZAR_VIGENCIA_PLANTILLA, params, fnSuccess, fnError);
    }

    _bcsCartolas.actualizarReproceso = function(idEmpresa, idUsuario, cartolas, fnSuccess, fnError) {
        params = {};
        params.p_IdEmpresa = idEmpresa;
        params.p_IdUsuario = idUsuario;
        params.p_IdModulo = MODULO;
        params.p_Cartolas = cartolas;

        Model.callServiceById(ACTUALIZAR_REPROCESO, params, fnSuccess, fnError);
    }

    _bcsCartolas.submitirProceso = function(nomina, parametros, segundos, fnSuccess, fnError) {
        var params = {
            p_IdEmpresa : nomina.IdEmpresa,
            p_IdUsuario : nomina.IdUsuario,
            p_IdModulo : MODULO,

            p_IdAgenda : 0,
            p_IdTipoTarea : 'ProcesoCartolas',
            p_Segundos : segundos,
            p_CreaParam : 'S',

            p_TablaParametros : [ {
                NombreParametro : 'idEmpresa',
                Valor : nomina.IdEmpresa
            }, {
                NombreParametro : 'idUsuario',
                Valor : nomina.IdUsuario
            }, {
                NombreParametro : 'schema',
                Valor : Model.getSchema()
            }, {
                NombreParametro : 'idNomina',
                Valor : nomina.IdNomina
            } ]
        };
        
        if (parametros) {
            for (var prop in parametros) {
                if (parametros[prop] !== null) {
                    var valor = '' + parametros[prop];
                    if (valor.length > 0) {
                    	params.p_TablaParametros.push({
                            NombreParametro : prop,
                            Valor : valor
                        });
                    }
                }
            }
        }

        Model.agregarPrograma(params, fnSuccess, fnError);
    }

    _bcsCartolas.subirImagen = function(file, recurso, fnSuccess, fnError) {
        var args = {
            idEmpresa : recurso.IdEmpresa,
            idUsuario : recurso.IdUsuario,
            schema : Model.getSchema(),
            modulo : MODULO,
            idRecurso : recurso.IdRecurso
        };

        FileUpload.uploadFileToUrl(file, UPLOAD_URL, args, fnSuccess, fnError);
    }

    _bcsCartolas.generarOffline = function(params, fnSuccess, fnError) {
		var href = GENERAR_URL
			+ '?idEmpresa=' + params.IdEmpresa
			+ '&idUsuario=' + params.IdUsuario
			+ '&schema=' + Model.getSchema()
			+ '&tipo=offline'
			+ '&idCartola=' + params.IdCartola;
		
		$http({
			method : 'GET',
			responseType : 'blob',
			url : href
		}).then(fnSuccess, fnError);
	}

    _bcsCartolas.generarOnline = function(params, fnSuccess, fnError) {
		var href = GENERAR_URL
			+ '?idEmpresa=' + params.IdEmpresa
			+ '&idUsuario=' + params.IdUsuario
			+ '&schema=' + Model.getSchema()
			+ '&tipo=online'
			+ '&idCliente=' + params.IdCliente
			+ '&idPortafolio=' + params.IdPortafolio
			+ '&fechaDesde=' + params.FechaDesde
			+ '&fechaHasta=' + params.FechaHasta;

		$http({
			method : 'GET',
			responseType : 'blob',
			url : href
		}).then(fnSuccess, fnError);
	}

    return _bcsCartolas;

}]);

angular.module('ModuloNormativa').factory('bcsServiciosCartola',['$resource', function($resource) {
    var urlGenerarCartola 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=21021";
    var urlDataCartola		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601174";
    var urlInformacionCliente	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601199";
    var urlGenerarCartolaPDF = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601200";
    
    return $resource("", {},
    {     
        'GenerarCartola'							: { method: "POST", url: urlGenerarCartola},
        'ObtieneDataCartola'						: { method: "POST", url: urlDataCartola},
        'InformacionCliente'						: { method: "POST", url: urlInformacionCliente},
        'GenerarCartolaPDF'                         : { method: "POST", url: urlGenerarCartolaPDF},
    });
}]);

angular.module('ModuloNormativa').factory('bcsServiciosNormativa',['$resource', function($resource) {
	var urlCircularCrearSolicitud = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3084";
    var urlCrearSolicitudCircular = "/BSCL/Plataforma/GWJAVA/PLA/call?t=309";
    var urlObtenerRutaArchivo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=334";
    var urlCrearSolicitudClientes = "/BSCL/Plataforma/GWJAVA/PLA/call?t=309";
    var urlCrearDocumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=349";
    var urlActualizarEstadoSolicitudCircular = "/BSCL/Plataforma/GWJAVA/PLA/call?t=350";
    var urlObtenerVersionEsquema = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1679";
    var urlCircularizacionGuardarParametros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3019";
    var urlCircularizacionCrearSolicitud = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3024";
    var urlCircularizacionNotificarCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3064";
    var urlCircularizacionModificacionNomina = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3065";
    var urlCircularizacionNotificarNomina = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3066";
    var urlCircularizacionNotificarNominaPorCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600046";
    var urlCircularizacionActualizarDatos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3067";
    var urlCircularizacionSolicitudCartas = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3070";
    var urlEjecucionOrdenesNotificarCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3241";
    var urlEjecucionOrdenesSolicitudCartas = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3329";
    var urlCumplimientoEvaluarDOF = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3078";
    var urlCumplimientoNotificarEvaluacionSOI = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3082";
    var urlMantenedorItem = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12079";
    var urlMantenedorItemConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13003"; 
    var urlMantieneInforme = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13040"; 
    var urlMantenedorIndice = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13007";
    var urlMantenedorIndiceConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13013";
    var urlMantenedorConceptoConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13010";
    var urlMantenedorConceptoGrupoConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20078";
    var urlMantenedorInformeConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13042";
    var urlMantenedorConcepto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13012";
    var urlInformeResultadoBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13027";
    var urlConceptoResultadoBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13028";
    var urlItemResultadoBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13029";
    var urlIndiceBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=671";
    var urlIndiceInformeBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=672";
    var urlItemDetalleResultadoBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13030";
    var urlDetalleCoberturaLlenaGrilla = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13032";
    var urlDetalleSaldoCuentaContableLlenaGrilla = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20077";
    var urlItemDigBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13038";
    var urlItemGrabarDig = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13039";
    var urlGenerarILS = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13045";
    var urlp_SaldosCuentasContables = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13046";    
    var urlCrearFondoAccionario1894 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3526";
    var urlCargarFondos1894 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3527";
    var urlActivarDesactivarFondos1894 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3528";
    var urlCrearProgramaCargaIPC = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3540";
    var urlActualizarValorFactor1894 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3539";
    var urlGenerarCertificados1894 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3543";
    var urlCir1518EditarDatos= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3569";
    var urlCir1720EditarDatos= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3577";
    var urlGenerarCertificados1980 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3571";
    var urlCrearProgramaGenerarCertificados = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3574";
    var urlCrearFondoAccionario1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3581";
    var urlCargarFondos1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3582";
    var urlActivarDesactivarFondos1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3583";
    var urlGenerarCertificados1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3584";
    var urlGenerarCertificados1949 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3610"; 
    var urlGenerarCertificados1944 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3611";
    var urlGenerarCertificados1914 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=6124";
    var urlActualizarValorFactor1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3585";
    var urlAgregarCalificacionTributaria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4151";
    var urlAgregarAnexoCalificacionTributaria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4152";
    var urlModificarCalificacionTributaria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4153";
    var urlModificarAnexoCalificacionTributaria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4154";
    var urlValidarFactoresCalificacionTributaria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4155";
    var urlCrearProgramaGenerarCertificadosMasivos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4161";
    var urlAsociarEventoCapital = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4163";
    var urlAsociarEventoCapital1949 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290673";
    var urlAsociarEventoCapital1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290677";    
    var urlObtenerSaldosContablesSAP = "/BSCL/Plataforma/GWJAVA/PLA/call?t=22002";
    var urlGenerarReporteMonedaExtranjera = "/BSCL/Plataforma/GWJAVA/PLA/call?t=22003";
    var urlExposicionCPROBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=22100";
    var urlPosicionesDescubiertasBuscar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=22101";
    var urlActualizarMontos1899 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600040";
    var urlActualizarMontos1922 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290667";
    var urlActualizarMontos1949 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290668";
    var urlActualizarValorFactor1871 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600027";
    var urlActualizarValorFactor1889 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600034";
    var urlActualizarValorFactor1899 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600045";    
    var urlAjustarRescatesDelPeriodo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600054";
    var urlActualizarEstadoInstrumento1874 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3619";
    var urlConsultarValidacionDJ1874 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3620";
    var urlReajuste4porciento1874 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3621";
    var urlConsultarGruposPortafolioCartola = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700015";
    var urlHomologarPlanDeCuentas = "/BSCL/Plataforma/GWJAVA/PLA/call?t=601111";
    var urlReporteCoberturaPatrimonial = "/BSCL/Plataforma/GWJAVA/PLA/call?t=577";
    var urlGenerarArchivoCircular1720 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13016";
    var urlModificarCalendarioPago = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700209";
    var urlCrearCalendarioPago = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700208";
    var urlEliminarCalendarioPago = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700215";
    var urlModificarPagosPeriodo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700218";
    var urlCrearPagosPeriodo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700216";
    var urlEliminarPagosPeriodo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700217";	
    var urlModificarGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700221";
    var urlCrearGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700219";
    var urlEliminarGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700220";
    var urlModificarTipoActivoGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700224";
    var urlCrearTipoActivoGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700222";
    var urlEliminarTipoActivoGarantia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700223";
    var urlIngresarMLT = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290691";
    var urlCargarMLT = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290703";
    var urlReprocesarEventoCircular1985 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290672";
    var urlIngresarInstrumentoCircular1720 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290680";
    var urlValorizarDeclaracion1874 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306007";
    var urlAgregarModificarEntidadAuditora ="/BSCL/Plataforma/GWJAVA/PLA/call?t=4306009";
    var urlGenerarReporteSaldosCuentaContable = "/BSCL/Plataforma/GWJAVA/PLA/call?t=900218";
    var urlProcesarReporteSaldoCuentasCambio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290724";
    var urlRectificarRegistro = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290705";
    var urlGrabarDetalle = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290729";
    var urlGrabarDetalleRMO = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290723";
    var urlEliminarDetallesRMO = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290722";
    var urlIngresarCalificacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=55676755";
    var urlConsultaSecuenciaEvento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=2000";
    var urlRectificarDetalles = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290705";
    var urlModificarNCG499 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010105";
    var urlValorizarNCG499 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010107";

    //URL obtener FechaReporte REPO3P1
    var urlObtenerFechaREPO3P1 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010163";
    //URL Servicio Generar Repo
    var urlGenerarREPO = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010162";
    //URL servicio Enviar a Sicam Repo
    var urlEnviarSicam = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010164";

    var urlConsultarReporteAPV = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010280";
    var urlModificarReporteAPV = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010281";
    var urlEliminarReporteAPV = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010282";
    var urlNotificarTodosCircular1985  = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010400";

    return $resource("", {},
    {
    	'CircularCrearSolicitud'                    : { method: "POST", url: urlCircularCrearSolicitud},
        'CrearSolicitudCircular'                    : { method: "POST", url: urlCrearSolicitudCircular},
        'ObtenerRutaArchivo'                        : { method: "POST", url: urlObtenerRutaArchivo},
        'CrearSolicitudClientes'                    : { method: "POST", url: urlCrearSolicitudClientes},
        'CrearDocumento'                            : { method: "POST", url: urlCrearDocumento},
        'ActualizarEstadoSolicitudCircular'         : { method: "POST", url: urlActualizarEstadoSolicitudCircular},
        'ObtenerVersionEsquema'                     : { method: "POST", url: urlObtenerVersionEsquema},
        'CircularizacionGuardarParametros'          : { method: "POST", url: urlCircularizacionGuardarParametros},
        'CircularizacionCrearSolicitud'             : { method: "POST", url: urlCircularizacionCrearSolicitud},
        'CircularizacionNotificarCliente'           : { method: "POST", url: urlCircularizacionNotificarCliente},
        'CircularizacionModificacionNomina'         : { method: "POST", url: urlCircularizacionModificacionNomina},
        'CircularizacionNotificarNomina'            : { method: "POST", url: urlCircularizacionNotificarNomina},
        'CircularizacionNotificarNominaPorCliente'  : { method: "POST", url: urlCircularizacionNotificarNominaPorCliente},
        'CircularizacionActualizarDatos'            : { method: "POST", url: urlCircularizacionActualizarDatos},
        'CircularizacionSolicitudCartas'            : { method: "POST", url: urlCircularizacionSolicitudCartas},
        //'EjecucionOrdenesNotificarNomina'           : { method: "POST", url: urlEjecucionOrdenesNotificarNomina},
        'EjecucionOrdenesNotificarCliente' 			: { method: "POST", url: urlEjecucionOrdenesNotificarCliente},
        'EjecucionOrdenesSolicitudCartas' 			: { method: "POST", url: urlEjecucionOrdenesSolicitudCartas},
        'CumplimientoEvaluarDOF'           			: { method: "POST", url: urlCumplimientoEvaluarDOF},
        'CumplimientoNotificarEvaluacionSOI'        : { method: "POST", url: urlCumplimientoNotificarEvaluacionSOI},
        'MantenedorIndice'                          : { method: "POST", url: urlMantenedorIndice},
        'MantenedorIndiceConfiguracion'             : { method: "POST", url: urlMantenedorIndiceConfiguracion},
        'MantenedorConceptoConfiguracion'           : { method: "POST", url: urlMantenedorConceptoConfiguracion},
        'MantenedorConceptoGrupoConfiguracion'           : { method: "POST", url: urlMantenedorConceptoGrupoConfiguracion},
        'MantenedorInformeConfiguracion'            : { method: "POST", url: urlMantenedorInformeConfiguracion},
        'MantenedorConcepto'           				: { method: "POST", url: urlMantenedorConcepto},
        'MantenedorItem'           					: { method: "POST", url: urlMantenedorItem},
        'MantenedorItemConfiguracion'           	: { method: "POST", url: urlMantenedorItemConfiguracion},
        'MantieneInforme'				           	: { method: "POST", url: urlMantieneInforme},
        'InformeResultadoBuscar'           	        : { method: "POST", url: urlInformeResultadoBuscar},
        'ConceptoResultadoBuscar'      	            : { method: "POST", url: urlConceptoResultadoBuscar},
        'ItemResultadoBuscar'           	        : { method: "POST", url: urlItemResultadoBuscar},
        'IndiceBuscar'       		    	        : { method: "POST", url: urlIndiceBuscar},
        'IndiceInformeBuscar'       		    	: { method: "POST", url: urlIndiceInformeBuscar},
        'ItemDetalleResultadoBuscar'   	            : { method: "POST", url: urlItemDetalleResultadoBuscar},
        'DetalleCoberturaLlenaGrilla'   	        : { method: "POST", url: urlDetalleCoberturaLlenaGrilla},
        'DetalleSaldoCuentaContableLlenaGrilla'     : { method: "POST", url: urlDetalleSaldoCuentaContableLlenaGrilla},
        'ItemDigBuscar'   	                        : { method: "POST", url: urlItemDigBuscar},
        'ItemGrabarDig'   	                        : { method: "POST", url: urlItemGrabarDig},  
        'GenerarILS'	   	                        : { method: "POST", url: urlGenerarILS},  
        'p_SaldosCuentasContables'  	            : { method: "POST", url: urlp_SaldosCuentasContables},  
        'CrearFondoAccionario1894'   	            : { method: "POST", url: urlCrearFondoAccionario1894},
        'CargarFondos1894'   	                    : { method: "POST", url: urlCargarFondos1894},
        'ActivarDesactivarFondos1894'   	        : { method: "POST", url: urlActivarDesactivarFondos1894},
        'CrearProgramaCargaIPC'   	                : { method: "POST", url: urlCrearProgramaCargaIPC},
        'ActualizarValorFactor1894'   	            : { method: "POST", url: urlActualizarValorFactor1894},
        'GenerarCertificados1894'   	            : { method: "POST", url: urlGenerarCertificados1894},
        'Cir1518EditarDatos'   						: { method: "POST", url: urlCir1518EditarDatos},
        'Cir1720EditarDatos'   						: { method: "POST", url: urlCir1720EditarDatos},
        'GenerarCertificados1980'   	            : { method: "POST", url: urlGenerarCertificados1980},
        'CrearProgramaGenerarCertificados'   	    : { method: "POST", url: urlCrearProgramaGenerarCertificados},
        'CrearFondoAccionario1922'   	            : { method: "POST", url: urlCrearFondoAccionario1922},
        'CargarFondos1922'   	                    : { method: "POST", url: urlCargarFondos1922},
        'GenerarCertificados1922'   	            : { method: "POST", url: urlGenerarCertificados1922},
        'ActivarDesactivarFondos1922'   	        : { method: "POST", url: urlActivarDesactivarFondos1922},
        'ActualizarValorFactor1922'   	            : { method: "POST", url: urlActualizarValorFactor1922},
        'AgregarCalificacionTributaria'   	        : { method: "POST", url: urlAgregarCalificacionTributaria},
        'AgregarAnexoCalificacionTributaria'   	    : { method: "POST", url: urlAgregarAnexoCalificacionTributaria},
        'ModificarCalificacionTributaria'   	    : { method: "POST", url: urlModificarCalificacionTributaria},
        'ModificarAnexoCalificacionTributaria'   	: { method: "POST", url: urlModificarAnexoCalificacionTributaria},
        'ValidarFactoresCalificacionTributaria'   	: { method: "POST", url: urlValidarFactoresCalificacionTributaria},
        'CrearProgramaGenerarCertificadosMasivos'   : { method: "POST", url: urlCrearProgramaGenerarCertificadosMasivos},
        'AsociarEventoCapital'   					: { method: "POST", url: urlAsociarEventoCapital},
        'AsociarEventoCapital1949' 					: { method: "POST", url: urlAsociarEventoCapital1949},
        'AsociarEventoCapital1922' 					: { method: "POST", url: urlAsociarEventoCapital1922},
        'ActualizarMontos1899'						: { method: "POST", url: urlActualizarMontos1899},
        'ActualizarMontos1922'						: { method: "POST", url: urlActualizarMontos1922},
        'ActualizarMontos1949'						: { method: "POST", url: urlActualizarMontos1949},
        'ObtenerSaldosContablesSAP'					: { method: "POST", url: urlObtenerSaldosContablesSAP},
        'GenerarReporteMonedaExtranjera'			: { method: "POST", url: urlGenerarReporteMonedaExtranjera},
        'ActualizarValorFactor1871'   	            : { method: "POST", url: urlActualizarValorFactor1871},
        'ActualizarEstadoInstrumento1874'           : { method: "POST", url: urlActualizarEstadoInstrumento1874},
        'ConsultarValidacionDJ1874'                 : { method: "POST", url: urlConsultarValidacionDJ1874},
        'Reajuste4porciento1874'                    : { method: "POST", url: urlReajuste4porciento1874},
        'ActualizarValorFactor1889'   	            : { method: "POST", url: urlActualizarValorFactor1889},
        'ActualizarValorFactor1899'   	            : { method: "POST", url: urlActualizarValorFactor1899},
        'AjustarRescatesDelPeriodo'   	            : { method: "POST", url: urlAjustarRescatesDelPeriodo},
        'ConsultarGruposPortafolioCartola'   	    : { method: "POST", url: urlConsultarGruposPortafolioCartola},	//CELZORRO-464
        'HomologarPlanDeCuentas'   	   				: { method: "POST", url: urlHomologarPlanDeCuentas},
        'ReporteCoberturaPatrimonial'   	   		: { method: "POST", url: urlReporteCoberturaPatrimonial},
        'GenerarArchivoCircular1720'   	   		    : { method: "POST", url: urlGenerarArchivoCircular1720},
        'GenerarCertificados1949'   	            : { method: "POST", url: urlGenerarCertificados1949},
        'GenerarCertificados1944'   	            : { method: "POST", url: urlGenerarCertificados1944},
        'GenerarCertificados1914'   	            : { method: "POST", url: urlGenerarCertificados1914},
		'ModificarCalendarioPago'					: { method: "POST", url: urlModificarCalendarioPago},	
		'CrearCalendarioPago'						: { method: "POST", url: urlCrearCalendarioPago},
		'EliminarCalendarioPago'					: { method: "POST", url: urlEliminarCalendarioPago},	
		'ModificarPagosPeriodo'						: { method: "POST", url: urlModificarPagosPeriodo},	
		'CrearPagosPeriodo'							: { method: "POST", url: urlCrearPagosPeriodo},
		'EliminarPagosPeriodo'						: { method: "POST", url: urlEliminarPagosPeriodo},
		'ModificarGarantia'							: { method: "POST", url: urlModificarGarantia},	
		'CrearGarantia'								: { method: "POST", url: urlCrearGarantia},
		'EliminarGarantia'							: { method: "POST", url: urlEliminarGarantia},
		'ModificarTipoActivoGarantia'				: { method: "POST", url: urlModificarTipoActivoGarantia},	
		'CrearTipoActivoGarantia'					: { method: "POST", url: urlCrearTipoActivoGarantia},
		'EliminarTipoActivoGarantia'				: { method: "POST", url: urlEliminarTipoActivoGarantia},
		'IngresarMLT'								: { method: "POST", url: urlIngresarMLT},
		'CargarMLT'									: { method: "POST", url: urlCargarMLT},
	    'ReprocesarEventoCircular1985'              : { method: "POST", url: urlReprocesarEventoCircular1985},
		'IngresarInstrumentoCircular1720'			: { method: "POST", url: urlIngresarInstrumentoCircular1720},
		'ValorizarDeclaracion1874'					: { method: "POST", url: urlValorizarDeclaracion1874},
	    'AgregarModificarEntidadAuditora'           : { method: "POST", url: urlAgregarModificarEntidadAuditora},
	    'GenerarReporteSaldosCuentaContable'        : { method: "POST", url: urlGenerarReporteSaldosCuentaContable},
	    'ProcesarReporteSaldoCuentasCambio'         : { method: "POST", url: urlProcesarReporteSaldoCuentasCambio},
	    'RectificarRegistro'						: { method: "POST", url: urlRectificarRegistro},
	    'GrabarDetalle'								: { method: "POST", url: urlGrabarDetalle},
	    'GrabarDetalleRMO'        					: { method: "POST", url: urlGrabarDetalleRMO},
	    'EliminarDetallesRMO'         				: { method: "POST", url: urlEliminarDetallesRMO},
        'IngresarCalificacion'         				: { method: "POST", url: urlIngresarCalificacion},
        'ConsultaSecuenciaEvento'                   : { method: "POST", url: urlConsultaSecuenciaEvento},
	    'RectificarDetalles'						: { method: "POST", url: urlRectificarDetalles},
	    'EliminarDetallesRMO'         				: { method: "POST", url: urlEliminarDetallesRMO},
        'ModificarNCG499'                           : { method: "POST", url: urlModificarNCG499},
        'ValorizarNCG499'                           : { method: "POST", url: urlValorizarNCG499},
        'ObtenerFechaREPO3P1'                       : { method: "POST", url: urlObtenerFechaREPO3P1},
        'GenerarREPO'                               : { method: "POST", url: urlGenerarREPO},
        'EnviarSicam'                               : { method: "POST", url: urlEnviarSicam}, //TODO Ajara > enviar a java EnviarTransaccionSicam
        'ConsultarReporteAPV'                       : { method: "POST", url: urlConsultarReporteAPV},
        'ModificarReporteAPV'                       : { method: "POST", url: urlModificarReporteAPV},
        'EliminarReporteAPV'                        : { method: "POST", url: urlEliminarReporteAPV},
        'NotificarTodosCircular1985'                : { method: "POST", url: urlNotificarTodosCircular1985},
    });
}]);

//-------------------------------------------------------------------
/*
(function() {
	angular.module('ModuloNormativa').factory('servicioClienteDOF',['$resource',function($resource) {
		var urlSaveDOF 		= "/BSCL/Plataforma/XSREST/call.xsjs?t=325";
		var urlUpdateDOF 	= "/BSCL/Plataforma/XSREST/call.xsjs?t=324";		
		return $resource("", {},
		{
	  	  'saveDOF'		: { method: "POST",	url: urlSaveDOF},
	  	  'updateDOF'	: { method: "POST",	url: urlUpdateDOF},
	     });
	}]);
}());
*/
angular.module('ModuloNormativa').factory('bcsNormativaDOF',['bcsComunes','bcsAlerta','bcsServiciosNormativa','Model','Utils','$compile','$modal','$translate',function(bcsComunes,bcsAlerta,bcsServiciosNormativa,Model,Utils,$compile,$modal,$translate)  
{
	
		var obj = {};
		var DOF1 = this;
		var parametrosSP = {};
		var datosOperacion = {};
		var Result	= {};
		var successEvaluarDOF = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				
				RequiereDOF =respuesta.P_OUT_REQUIEREDOF;
				if (RequiereDOF=='SI'){
					
				//var parametros = {
				//IdCliente					: 524896};
					//var id1 = bcsComunes.ventanaAbrir("ModalClienteDOF",datosOperacion,false,false,DOF1.idVentana); 
		            //var promesa=bcsComunes.ventanaPromesa(id1);                               
		            //$.when(promesa).then(function(data){}); 	
		            bcsComunes.ventanaAbrir("ModalClienteDOF",datosOperacion,false,false,DOF1.idVentana); 
		             	
				}; 
			}
			else
			{	
				
				DOF1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",DOF1.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			Result=respuesta;
			
		};
		
		var errorEvaluarDOF = function(respuesta)
		{	

			Result=respuesta;
		};
		
		obj.Result = function(){
			return Result;	
		};		
		obj.evaluarOperacion = function(v_IdVentana,v_Parametros) {
			datosOperacion= {}; 
						
			DOF1.idVentana=v_IdVentana;
			datosOperacion=v_Parametros;

			parametrosSP= {};			 
			//Comunes
	    	parametrosSP.p_IdEmpresa 				= 	datosOperacion.IdEmpresa;
	    	parametrosSP.p_IdUsuario              	= 	datosOperacion.IdUsuario;
	    	parametrosSP.p_IdModulo 				= 	datosOperacion.IdModulo;
			parametrosSP.p_UsaReglasNegocio 		=  'N';
			parametrosSP.p_simularOperacion			=  'N';
			parametrosSP.p_IdTipoOperacion			=	datosOperacion.IdTipoOperacion;
			parametrosSP.p_IdCliente				=	datosOperacion.IdCliente;
			parametrosSP.p_MontoOperacion			=	datosOperacion.MontoOperacion;
			parametrosSP.p_IdMonedaOperacion		=	datosOperacion.IdMonedaOperacion;
			parametrosSP.p_IdMedioDePago			=	datosOperacion.IdMedioDePago;
			parametrosSP.p_FechaOperacion			=	datosOperacion.FechaOperacion;
	
					
			//bcsComunes.msg('info', 'Carga', 'En proceso...');
			//bcsComunes.ventanaOverlay(DOF1.idVentana,true);
			
			 bcsServiciosNormativa.CumplimientoEvaluarDOF(parametrosSP,successEvaluarDOF,errorEvaluarDOF);
			
	        return obj;
			 
		 };
		
		
		
	return obj;
}]);

angular.module('ModuloNormativa').directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;

            element.bind('change', function() {
                scope.$apply(function() {
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);

angular.module('ModuloNormativa').factory('bcsServicioMaestroDeclaraciones',['$resource',function($resource) {
	
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id};
			
		});
		return r;
	};
	
	var resources = [
	        { nombre : 'ConfigurarVersion', id : "3247" },
	        { nombre : 'BloquearDesbloquearEsquema', id: "3248"},
	        { nombre : 'ActivarVersion', id:"3249"},
	        { nombre : 'ActivarPeriodo', id:"3250"}
	     ];
	return $resource("", {},generarResource());
}]);

angular.module('ModuloNormativa').factory('bcsServicioInterfaces',['$resource',function($resource) {
	
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id};
			
		});
		return r;
	};
	
	var resources = [
	        { nombre : 'InscribirProcesoBatch', id : "20043" },
	        { nombre : 'ValidaGenerarInterfaz', id : "601154" },
	        { nombre : 'AgregarModificarFilialesP40',  id : "700168" },
	        { nombre : 'EliminarFilialesP40',  id : "700170" }
	     ];
	return $resource("", {},generarResource());
}]);

function Model($http, $resource) {

    /**
     * Schema de la base de datos.
     */
    var SCHEMA = 'BSCL';

    /**
     * ID del servicio para agregar programa.
     */
    var ID_SERVICIO_AGREGAR_PROGRAMA = 183;

    /**
     * ID del servicio para guardar documento.
     */
    var ID_SERVICIO_GUARDAR_DOCUMENTO = 36;

    /**
     * Nombre del campo que contiene la llave unica del registro de detalle.
     */
    var ID_NAME = 'ID';

    /**
     * Tipos de datos definidos.
     */
    var TYPE_AMOUNT = 'AMOUNT';
    var TYPE_DATE = 'DATE';
    var TYPE_DATE_FILTER = 'DATE_FILTER';
    var TYPE_DECIMAL = 'DECIMAL';
    var TYPE_ID = 'ID';
    var TYPE_ID_CLIENTE = 'ID_CLIENTE';
    var TYPE_ID_PORTAFOLIO = 'ID_PORTAFOLIO';
    var TYPE_NEMO = 'NEMO';
    var TYPE_QTY = 'QTY';
    var TYPE_RUT = 'RUT';
    var TYPE_RUT_CLIENTE = 'RUT_CLIENTE';
    var TYPE_TEXT = 'TEXT';

    /**
     * Metodos a incorporar a cada campo para otorgar comportamiento.
     */
    function FieldMethods() {

        this.isAmount = function() {
            return this.TipoDato === TYPE_AMOUNT;
        }

        this.isDecimal = function() {
            return this.TipoDato === TYPE_DECIMAL;
        }

        this.isText = function() {
            switch (this.TipoDato) {
            case TYPE_NEMO:
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
            case TYPE_TEXT:
                return true;
            default:
                return false;
            }
        }

        this.isList = function() {
            return this.TipoDiccionario != null &&
                this.TipoDiccionario.length > 0;
        }

        this.isDate = function() {
            return this.TipoDato === TYPE_DATE
                || this.TipoDato === TYPE_DATE_FILTER;
        }

        this.isNumber = function() {
            switch (this.TipoDato) {
            case TYPE_ID:
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_QTY:
                return true;
            default:
                return false;
            }
        }

        this.getAttributes = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
            case TYPE_QTY:
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
                return { style : 'text-align:right;' };
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return { style : 'text-align:center;' };
            default:
                return { style : 'text-align:left;' };
            }
        }

        this.getFormat = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
                return '{0:###,###,###,###,##0}';
            case TYPE_DECIMAL:
                return '{0:n' + this.CantidadDecimales + '}';
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return '{0:dd-MM-yyyy}';
            case TYPE_QTY:
                return '{0:###,###,##0}';
            default:
                return null;
            }
        }

        this.getType = function() {
            switch (this.TipoDato) {
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return 'date';
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
            case TYPE_QTY:
                return 'number';
            default:
                return 'string';
            }
        }

        this.getWidth = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
                return '100px';
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return '100px';
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
                return '80px';
            case TYPE_NEMO:
                return '100px';
            case TYPE_QTY:
                return '100px';
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
                return '80px';
            default:
                return '100px';
            }
        }

        this.isCommon = function() {
            return this.Comun === 'S';
        }

        this.isKey = function() {
            return this.Clave === 'S';
        }

        this.isSumable = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_QTY:
                return true;
            default:
                return false;
            }
        }

        this.isFiltrable = function() {
            return this.isNumber() || this.isDate() || this.isList();
        }

        this.isEditable = function() {
            return this.Editable === 'S';
        }

        this.isRequired = function() {
            return this.Obligatorio === 'S';
        }

        this.isHiddenInGrid = function() {
            return this.OcultoEnGrilla === 'S';
        }

        this.isHiddenInForm = function() {
            return this.OcultoEnMantenedor === 'S';
        }

        this.htmlRequired = function() {
            if (this.isEditable() && this.isRequired())
                return 'required';
            return '';
        }

        this.htmlReadonly = function(masivo) {
            if (!this.isEditable() || (masivo && this.ModoMasivo === 'N'))
                return 'readonly';
            return '';
        }

        this.htmlMaxlength = function() {
            if (this.TipoDato === TYPE_AMOUNT)
                return 'maxlength="15"';
            if (this.TipoDato === TYPE_DECIMAL)
                return 'maxlength="22"';
            if (this.TipoDato === TYPE_RUT)
                return 'maxlength="11"';
            if (this.TipoDato === TYPE_RUT_CLIENTE)
                return 'maxlength="11"';
            if (this.TipoDato === TYPE_QTY)
                return 'maxlength="9"';
            if (this.Largo > 0) {
                return 'maxlength="' + this.Largo + '"';
            }
            return '';
        }
    }

    /**
     * Obtiene el data source para la consulta de los campos asociados al
     * esquema/version.
     *
     * @param esquema
     *            El nombre del esquema.
     * @param version
     *            La version del esquema.
     * @returns {kendo.data.DataSource}
     */
    
   
    
    function getDataSourceCampo(esquema, version) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field : 'Grupo',
                dir : 'ASC'
            },  {
                field : 'Orden',
                dir : 'ASC'
            } ],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=1473',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            }]
        });
    }
    
    
    /**
     * Obtiene el data source para la consulta de los parametros asociados al
     * esquema/version.
     *
     * @param esquema
     *            El nombre del esquema.
     * @param version
     *            La version del esquema.
     * @returns {kendo.data.DataSource}
     */
    
   
    
    function getDataSourceParametro(esquema, version) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field : 'Codigo',
                dir : 'ASC'
            },  {
                field : 'Descripcion',
                dir : 'ASC'
            } ],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=100036',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            }]
        });
    }
    

    /**
     *
     */
    function getDataSourceConfiguracionMantenedor(IdMantenedor, IdTipoDocumento) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field: 'IdObjeto',
                dir : 'ASC'
            }],
            transport : {
                read : {
                    url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=3210',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [{
                field : 'IdMantenedor',
                operator : 'eq',
                value : IdMantenedor
            }, {
                field : 'IdTipoDocumento',
                operator : 'eq',
                value : IdTipoDocumento
            }]
        });
    }

    /**
     * Obtiene el data source para la consulta del documento asociado al
     * esquema/version.
     *
     * @param esquema
     *            El nombre del esquema.
     * @param version
     *            La version del esquema.
     * @returns {kendo.data.DataSource}
     */
    function getDataSourceDocumento(esquema, version, idEmpresa) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : {
                field : 'IdDocumento',
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3209',
                    dataType : 'json',
                    async : false
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            } , {
                field : 'IdEstadoDocumento',
                operator : 'eq',
                value : 'ACT'
            } , {
                field : 'IdEmpresa',
                operator : 'eq',
                value : idEmpresa
            } ],
            schema : {
                model : {
                    id: 'IdDocumento',
                    fields : {
                        IdDocumento : { type : 'number' },
                        IdEmpresa : { type : 'number' },
                        IdTitularDocumento : { type : 'number' },
                        IdRepresentanteLegal : { type : 'number' },
                        FechaLimite : { type : 'date' }
                    }
                }
            }
        });
    }

    /**
     * Obtiene el data source para consultar los diccionarios.
     */
    function getDataSourceDiccionario() {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [{
                field : 'Tipo',
                dir : 'ASC'
            }, {
                field : 'Orden',
                dir : 'ASC'
            }],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3208',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true
        });
    }
    
    

    /**
     * Obtiene nuevo data source para rescatar los servicios.
     *
     * @param servicio
     *            El nombre del servicio.
     * @returns {kendo.data.DataSource}
     */
    function getDataSourceServicio(servicio) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : {
                field : 'IdServicio',
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=1229',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'RutaServicio',
                operator : 'endswith',
                value : 'BSCL.Normativa.BD.Servicios::' + _model.getModulo() + '.' + servicio
            } ]
        });
    }

    /**
     * Obtiene el campo en formato Grid.Field.
     *
     * @param campo
     *            El campo.
     * @returns {___anonymous375_376}
     */
    function getField(campo) {
        var c = {};
        c.field = campo.Nombre;
        c.title = campo.Titulo;
        c.format = campo.getFormat();
        c.width = campo.getWidth();
        c.attributes = campo.getAttributes();
        c.hidden = campo.isHiddenInGrid();
        return c;
    }

    /**
     * Obtiene la URL del servicio asociado al ID.
     *
     * @param id
     *            ID del servicio.
     * @param args
     *            Argumentos que se agregaran a la URL.
     */
    function getUrlServicioById(id, args) {
        var url = '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + id;
        if (args != null) {
            var str = '';
            for (var key in args) {
                str += '&' + key + '=' + encodeURIComponent(args[key]);
            }
            url += str;
        }

        return url;
    }

    /**
     * Obtiene la URL del servicio asociado al esquema/metodo.
     *
     * @param esquema
     *            Nombre del esquema.
     * @param metodo
     *            Nombre del metodo.
     * @param args
     *            Argumentos que se agregaran a la URL.
     * @param success
     *            Funcion a llamar en caso de exito.
     * @param error
     *            Funcion a llamar en caso de error.
     */
    function getUrlServicio(esquema, metodo, args, success, error) {
        var servicio = esquema + '.' + metodo;
        var dataSource = getDataSourceServicio(servicio);
        dataSource.fetch().then(function() {
            var servicios = dataSource.data();
            if (servicios.length < 1) {
                error({ message : 'No esta definido el servicio ' + servicio });
            } else {
                var id = servicios[0].IdServicio;
                var url = getUrlServicioById(id, args);
                success(url);
            }
        });
    }

    var _model = {};
    var _campos = null;
    var _esquema = null;
    var _version = null;
    var _documento = null;
    var _configuracionMantenedor = null;
    var _parametros = null;
    
    var _filtrosDeTotales = {};
    var _columnasTotalesComunes = [
        { Nombre : 'TYPE', Titulo : '&nbsp;', TipoDato : TYPE_TEXT },
        { Nombre : 'COUNT', Titulo : 'Registros', TipoDato : TYPE_QTY }
    ];

    var _diccionarios = null;
    var dataSource = getDataSourceDiccionario();
    dataSource.fetch().then(function() {
        _diccionarios = dataSource.data();
    });

    /**
     * Obtiene el schema de la base de datos.
     */
    _model.getSchema = function() {
        return SCHEMA;
    }

    /**
     * Obtiene la lista de los campos del esquema actual.
     */
    _model.getCampos = function() {
        return _campos;
    }
    
    /**
     * Obtiene la lista de los parametros del esquema actual.
     */
    _model.getParametros = function() {
        return _parametros;
    }
    
    /**
     * Obtiene la configuracion de botones y tab del mantenedor actual.
     */
    _model.getConfiguracionMantenedor =  function() {
        return _configuracionMantenedor;
    }

    /**
     * Obtiene el esquema actual.
     */
    _model.getEsquema = function() {
        return _esquema;
    }

    /**
     * Obtiene la version del esquema actual.
     */
    _model.getVersion = function() {
        return _version;
    }

    /**
     * Asigna los filtros que seran usados en la consulta de totales.
     */
    _model.setFiltrosDeTotales = function(filtros) {
        _filtrosDeTotales = filtros;
    }

    /**
     * Limpia los filtros que seran usados en la consulta de totales.
     */
    _model.limpiarFiltrosDeTotales = function() {
        _filtrosDeTotales = {};
    }

    /**
     * Obtiene el data source para obtener la lista completa de esquemas.
     */
   
    _model.getDataSourceEsquema = function() {
        return new kendo.data.DataSource({
            type : 'odata',
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=1774',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'HabilitadoMantenedor',
                operator : 'eq',
                value : 'S'
            } ]
        });
    }

    
    
    /**
     * Obtiene la lista de campos filtrables.
     */
    _model.getCamposFiltrables = function() {
        var list = [];
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.isFiltrable()) {
                list.push(campo);
            }
        }
        return list;
    }
    
    /**
     * Obtiene el campo dado el titulo del campo.
     */
    _model.getCampoPorTitulo = function(titulo) {
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.Titulo === titulo) {
                return campo;
            }
        }
        return null;
    }

    /**
     * Obtiene el patrametro dado el codigo del parametro.
     */
    _model.getParametroPorCodigo = function(codigo) {
        for (var i = 0; i < _parametros.length; i++) {
            var parametro = _parametros[i];
            if (parametro.Codigo === codigo) {
                return parametro;
            }
        }
        return null;
    }

    /**
     * Obtiene el campo fecha del registro.
     */
    _model.getCampoFecha = function() {
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.TipoDato === TYPE_DATE_FILTER) {
                return campo;
            }
        }
        return null;
    }

    /**
     * Obtiene el campo RUT Cliente del registro.
     */
    _model.getCampoRutCliente = function() {
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.TipoDato === TYPE_RUT_CLIENTE) {
                return campo;
            }
        }
        return null;
    }

    /**
     * Obtiene un campo dado un nombre.
     */
    _model.getCampo = function(nombre) {
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.Nombre === nombre) {
                return campo;
            }
        }
        return null;
    }

    _model.getDocumento = function() {
        return _documento;
    }  

    function offsetDate(date) {
        var tempDate = parseInt(date);
        var offsetMiliseconds = new Date(tempDate).getTimezoneOffset() * 60000;
        return tempDate + offsetMiliseconds;
    }

    var DATE_PREFIX = '/Date(';

    function fixDate(val) {
        var jsonDateString = val.replace(/\-?\d+/, offsetDate);
        var d = new Date(parseInt(jsonDateString.replace(DATE_PREFIX, '')));
        return d.getFullYear() > 1900 ? d : null;
    }

    function fixDates(data) {
        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            for (var name in item) {
                if (typeof item[name] === 'string' && item[name].startsWith(DATE_PREFIX)) {
                    item[name] = fixDate(item[name]);
                }
            }
        }
    }

    /**
     * Obtiene el data source para la consulta de las cuadraturas asociados al
     * esquema/version.
     *
     * @param esquema
     *            El nombre del esquema.
     * @param version
     *            La version del esquema.
     * @returns {kendo.data.DataSource}
     */
     _model.getDataSourceCuadratura = function(esquema, version) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field : 'Tipo',
                dir : 'ASC'
            } ],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3222',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            } ]
        });
    }

    /**
     * Obtiene el data source para la consulta de los clasificadores asociados
     * al esquema/version.
     *
     * @param esquema
     *            El nombre del esquema.
     * @param version
     *            La version del esquema.
     * @returns {kendo.data.DataSource}
     */
     _model.getDataSourceClasificador = function(esquema, version) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field : 'Tipo',
                dir : 'ASC'
            } ],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3223',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            } ]
        });
    }

    /**
     * Obtiene el data source para consultar los detalles del documento. Usa el
     * esquema actual y sus campos asociados.
     */
    _model.getDataSourceDetalles = function(codigoValidacion) {
        var fields = {};
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.Nombre === ID_NAME) {
                fields[campo.Nombre] = { editable : false, nullable : true };
            } else {
                fields[campo.Nombre] = { type : campo.getType() };
                if (campo.isList()) {
                    fields[campo.Nombre + 'Descripcion'] = { type : 'string' };
                }
            }
        }

        var filtros = [];
        
		
		var year = _documento.Version.substring(0,4);
		var month = _documento.Version.substring(5,7).replace("0","") - 1;
		var startDate = new Date(year, month, 1);
		var endDate = new Date(year, month + 1, 0);
		
		if(_esquema === 'DJ1'){
			var filtro1 = { field: "kPeriodoComercial", operator: 'gte', value: startDate };
			filtros.push(filtro1);
			var filtro2 = { field: "kPeriodoComercial", operator: 'lte', value: endDate };
			filtros.push(filtro2);
		}
		
		if(_esquema === 'DJ999'){
			var filtro1 = { field: "FechaOperacion", operator: 'gte', value: startDate };
			filtros.push(filtro1);
			var filtro2 = { field: "FechaOperacion", operator: 'lte', value: endDate };
			filtros.push(filtro2);
		}
		
		var service=0;
		var nombre = _esquema;
        switch (nombre) {
        case 'DJ1':
          	service='4290626';
            break;
        case 'DJ999':
          	service='4290719';
            break;
        case 'DJ1820':
          	service='3280';
            break;
        case 'DJ1885':
          	service='3217';
            break;
        case 'DJ1884':
            service='3216';
            break;
        case 'DJ1891':
            service='4220';
            break;
        case 'CI1720':
            service='3218';
            break;    
        case 'DJ1919':
            service='3243';
            break;  
        case 'DJ1823':
            service='3258';
            break;
        case 'DJ1829':
            service='3259';
            break; 
        case 'DJ1839':
            service='3264';
            break;
        case 'DJ1848':
            service='3270';
            break; 
        case 'DJ1849':
            service='3275';
            break; 
        case 'DJ1870':
            service='3283';
            break;
        case 'DJ1874':
            service='3288';
            break;
        case 'DJ1890':
            service='3293';
            break;
        case 'DJ1862':
            service='3298';
            break;
        case 'DJ1894':
            service='3216';
            break;
        case 'DJ1922':
            service='3306';
            break;
        case 'DJ1932':
            service='3311';
            break;
        case 'DJ1914':
            service='3316';
            break;
        case 'DJ1871':
            service='600022';
            break;
        case 'DJ1889':
            service='600028';
            break;
        case 'DJ1899':
            service='600035';
            break;
        case 'DJ1944':
            service='4300003';
            break; 
        case 'DJ1946':
            service='4290710';
            break; 
        case 'DJ1949':
            service='4290666';
            break; 
         case 'DJ1958':
            service='4400001';
            break;
        case  'DJ1962':
            service='4400050';
            break;
         case 'REPO3P1':
            service='4400008';
            break;
         case 'REPO2P1':
            service='4400017';
            break;
         case 'REPO2P2':
            service='4400026';
            break;                                     
        }
        if (codigoValidacion !== null) {
        	  switch (nombre) {
        	    case 'DJ1':
                	service='4290631';
                  	break;
                case 'DJ999':
                	service='4290721';
                  	break;
        	  	case 'DJ1820':
		          	service='3281';
		            break;
		        case 'DJ1885':
		          	service='3219';
		            break;
		        case 'DJ1884':
		            service='3220';//se debe construir
		            break;
		        case 'DJ1891':
		            service='3220';
		            break;
		        case 'CI1720':
		            service='3221';
		            break;   
		        case 'DJ1919':
		            service='3244';
		            break; 
		        case 'DJ1823':
		            service='3256';
		            break; 
		        case 'DJ1829':
		            service='3260';
		            break; 
		        case 'DJ1839':
		            service='3265';
		            break; 
		        case 'DJ1848':
		            service='3271';
		            break;
		        case 'DJ1849':
		            service='3276';
		            break;
		        case 'DJ1870':
		            service='3284';
		            break;
		        case 'DJ1874':
		            service='3289';
		            break;
		        case 'DJ1890':
		            service='3294';
		            break;
		        case 'DJ1862':
		            service='3299';
		            break;
		        case 'DJ1894':
		            service='3303';
		            break;
		        case 'DJ1922':
		            service='3307';
		            break;
		        case 'DJ1932':
		            service='3312';
		            break;
		        case 'DJ1914':
		            service='3317';
		            break;
		        case 'DJ1871':
		            service='600024';
		            break;
		        case 'DJ1889':
		            service='600029';
		            break;
		        case 'DJ1899':
		            service='600036';
		            break;
		        case 'DJ1944':
		            service='4290721';
		            break;
		        case 'DJ1946':
		            service='4290722';
		            break;
		        case 'DJ1949':
		            service='4290723';
		            break;
		        case 'DJ1958':
         		   service='4400006';
            		break;      
                case 'DJ1962':
                    service='4400055';
                    break;
		        case 'REPO3P1':
         		   service='4400012';
            		break;          
		        case 'REPO2P1':
         		   service='4400019';
            		break;
                case 'REPO2P2':
         		   service='4400028';
            		break;  
		        }
            nombre = 'Invalidos' + _esquema;
            /* fields['codigoValidacion'] = { type : 'string' }; */
            
            filtros.push({
                field : 'CodigoValidacion',
                operator : 'eq',
                value : codigoValidacion
            } )
        }
        
        if (codigoValidacion == null){
            filtros.push({field : 'IdDocumento',operator : 'eq', value : _documento.IdDocumento});
        } 

        return new kendo.data.DataSource({
            type : 'odata',
            serverFiltering : true,
            serverPaging : true,
            serverSorting : true,
            pageSize : 12,
            sort : {
                field : ID_NAME,
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + service,
                    dataType : 'json'
                }
            },
            filter : filtros,
            requestEnd : onRequestEnd,
            schema : {
                model : {
                    id: ID_NAME,
                    fields : fields
                }
            }
        });
    }

    function onRequestEnd(e) {
        if ( typeof e.response !=='undefined' && e.response.d.results && e.response.d.results.length ) {
            var results = e.response.d.results;
            for (var name in this.options.schema.model.fields) {
                var field = this.options.schema.model.fields[name];
                if (field.hasOwnProperty('type')) {
                    if (field.type === 'date') {
                        for (var i = 0; i < results.length; i++) {
                            results[i][name] = fixDate(results[i][name]);
                        }
                    } else if (field.type === 'number') {
                        for (var i = 0; i < results.length; i++) {
                            var x = results[i][name] + '';
                            if (x !== null) {
                                results[i][name] = x.replace('.', ',');
                            }
                        }
                    }
                }
            }
        }
        
    }

    _model.getDataSourceResumenCarga = function() {
        return new kendo.data.DataSource({
            type : 'odata',
            serverFiltering : true,
            serverPaging : true,
            serverSorting : true,
            pageSize : 15,
            sort : {
                field : ID_NAME,
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3211',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            filter : [ {
                field : 'IdDocumento',
                operator : 'eq',
                value : _documento.IdDocumento
            } ],
            requestEnd : onRequestEnd,
            schema : {
                model : {
                    id: ID_NAME,
                    fields : {
                        ID : { type : 'number' },
                        IdProceso : { type : 'number' },
                        IdDocumento : { type : 'number' },
                        IdUsuario : { type : 'number' },
                        Fecha : { type : 'date' },
                        Glosa : { type : 'string' },
                        NombreArchivo : { type : 'string' },
                        TotalRegistrosLeidos : { type : 'number' },
                        TotalRegistrosCargados : { type : 'number' },
                        TotalRegistrosErroneos : { type : 'number' }
                    }
                }
            }
        });
    }

    _model.getDataSourceResumenCuadratura = function() {
        return new kendo.data.DataSource({
            type : 'odata',
            serverFiltering : true,
            serverPaging : true,
            serverSorting : true,
            pageSize : 15,
            sort : {
                field : ID_NAME,
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3212',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            filter : [ {
                field : 'IdDocumento',
                operator : 'eq',
                value : _documento.IdDocumento
            } ],
            requestEnd : onRequestEnd,
            schema : {
                model : {
                    id: ID_NAME,
                    fields : {
                        ID : { type : 'number' },
                        IdProceso : { type : 'number' },
                        IdDocumento : { type : 'number' },
                        IdUsuario : { type : 'number' },
                        AnoMes : { type : 'date' },
                        FechaCuadratura : { type : 'date' },
                        TipoCuadratura : { type : 'string' },
                        TipoDiferencia : { type : 'string' },
                        TipoClasificador : { type : 'string' },
                        Glosa : { type : 'string' },
                        CantidadCasos : { type : 'number' }
                    }
                }
            }
        });
    }

    _model.getDataSourceResumenGeneracion = function() {
        return new kendo.data.DataSource({
            type : 'odata',
            serverFiltering : true,
            serverPaging : true,
            serverSorting : true,
            pageSize : 15,
            sort : {
                field : ID_NAME,
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3213',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            filter : [ {
                field : 'IdDocumento',
                operator : 'eq',
                value : _documento.IdDocumento
            } ],
            requestEnd : onRequestEnd,
            schema : {
                model : {
                    id: ID_NAME,
                    fields : {
                        ID : { type : 'number' },
                        IdProceso : { type : 'number' },
                        IdDocumento : { type : 'number' },
                        IdUsuario : { type : 'number' },
                        Fecha : { type : 'date' },
                        Glosa : { type : 'string' },
                        TotalRegistrosGenerados : { type : 'number' },
                        Usuario : { type : 'string' }
                    }
                }
            }
        });
    }

    
    _model.getDataSourceResumenValidacion = function() {
        return new kendo.data.DataSource({
            type : 'odata',
            serverFiltering : true,
            serverPaging : true,
            serverSorting : true,
            pageSize : 15,
            sort : {
                field : ID_NAME,
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=3214',
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            filter : [ {
                field : 'IdDocumento',
                operator : 'eq',
                value : _documento.IdDocumento
            } ],
            requestEnd : onRequestEnd,
            schema : {
                model : {
                    id: ID_NAME,
                    fields : {
                        ID : { type : 'number' },
                        IdProceso : { type : 'number' },
                        IdDocumento : { type : 'number' },
                        IdUsuario : { type : 'number' },
                        CodigoValidacion : { type : 'string' },
                        DescripcionValidacion : { type : 'string' },
                        CantidadCasos : { type : 'number' },
                        FechaValidacion : { type : 'date' }
                    }
                }
            }
        });
    }

    /**
     * Establece el esquema/version solicitado. Ademas, carga los campos
     * asociados al esquema/version.
     */
    _model.establecer = function(esquema, version, idEmpresa, success, error) {
        var dataSourceCampo = getDataSourceCampo(esquema, version);
        dataSourceCampo.fetch().then(function() {
            _campos = dataSourceCampo.data();
            if (_campos.length < 1) {
                error({message : 'No hay campos definidos para ' + esquema + '-' + version});
            } else {
                _esquema = esquema;
                _version = version;

                var ds = getDataSourceDocumento(esquema, version, idEmpresa);
                ds.read();

                var data = ds.data();
                fixDates(data);

                _documento = null;
                if (data.length > 0) {
                    _documento = data[0];
                }
                
                // Complementa cada campo con el comportamiento deseado.
                var fieldMethods = new FieldMethods();
                for (var i = 0; i < _columnasTotalesComunes.length; i++) {
                    $.extend(_columnasTotalesComunes[i], fieldMethods);
                }
                for (var i = 0; i < _campos.length; i++) {
                    $.extend(_campos[i], fieldMethods);
                }
                
                success();
            }
        });
    }

    _model.establecerConfiguracionMantenedor = function (IdMantenedor, IdTipoDocumento, idEmpresa, success, error) {
        var dataSourceConfiguracionMantenedor = getDataSourceConfiguracionMantenedor(IdMantenedor, IdTipoDocumento);
        dataSourceConfiguracionMantenedor.read();
        _configuracionMantenedor = dataSourceConfiguracionMantenedor.data();
        dataSourceConfiguracionMantenedor.fetch().then(function() {
            _configuracionMantenedor = dataSourceConfiguracionMantenedor.data();
            if (_configuracionMantenedor.length < 1) {
                error({message : 'No hay configuracion definida para ' + IdMantenedor + '-' + IdTipoDocumento});
            } else {
                success();
            }
        });
    }

    _model.parametros = function(esquema, version, idEmpresa, success, error) {
        var dataSourceParametro = getDataSourceParametro(esquema, version);
        dataSourceParametro.fetch().then(function() {
            _parametros = dataSourceParametro.data();
            if (_parametros.length < 1) {
                error({message : 'No hay parametros definidos para ' + esquema + '-' + version});
            } else {
                success();
            }
        });
    }
    
    _model.ConsultaControlProceso = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4200",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						Esquema				: {type: "string"},
						Version				: {type: "string"},
						EstadoCertificado	: {type: "string"},
						Carga				: {type: "string"},
						Validacion			: {type: "string"},
						PreDeclaracion		: {type: "string"},
						Certificacion		: {type: "string"}
					}
				}
			}
		},
	});	
    
    function getFilter(campo, list) {
        return function(element) {
            element.kendoDropDownList({
                dataTextField : 'Descripcion',
                dataValueField : 'Codigo',
                dataSource : list
            });
        }
    }

    function setFilterable(column, campo) {
        if (campo.isList()) {
            column.filterable = {
                extra : false,
                ui : getFilter(campo, _model.getDiccionario(campo.TipoDiccionario, true)),
                operators : {
                    string : {
                        eq : 'Es igual a'
                    }
                }
            };
        } else if (campo.isText()) {
            column.filterable = {
                extra : false,
                operators : {
                    string : {
                        contains : 'Contiene',
                        startswith : 'Comienza con',
                        endswith : 'Termina en'
                    }
                }
            };
        } else if (campo.isDate()) {
            column.filterable = {
                operators : {
                    date: {
                        gte : 'Es mayor o igual que',
                        lte : 'Es menor o igual que'
                    }
                }
            };
        } else if (campo.Nombre != ID_NAME) {
            column.filterable = {
                operators : {
                    number: {
                        gte : 'Es mayor o igual que',
                        lte : 'Es menor o igual que'
                    }
                }
            };
        }
    }

    /**
     * Obtiene las opciones para la grilla que contiene los detalles. Usa el
     * esquema actual y sus campos asociados.
     */
    _model.getGridDataOptions = function(onDataBound, codigoValidacion) {
        var columns = [];
        var grupos = [];
        var grupo = null;
        var locked = true;
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            var column = getField(campo);
            if (campo.Nombre === ID_NAME) {
                // La columna ID se muestra con checkboxes para seleccion.
                column.template = '<input class="checkbox" type="checkbox" />';
                column.width = '50px';
                column.filterable = false;
            } else if (campo.TipoDato === TYPE_DATE_FILTER
                    || campo.TipoDato === TYPE_RUT_CLIENTE) {
                // No se filtran con el embudos, sino con los filtros del
                // formulario.
                column.filterable = false;
            } else {
                setFilterable(column, campo);
            }

            if (grupo == null || campo.Grupo != grupo) {
                grupo = campo.Grupo;
                columns = [];
                grupos.push({
                    title : grupo === '' ? '&nbsp;' : grupo,
                    columns: columns,
                    locked : locked
                });
                locked = false;
            }

            columns.push(column);
            if (campo.isList()) {
                column = {
                    field : campo.Nombre + 'Descripcion',
                    title : 'Descripcion ' + campo.Titulo,
                    width : '100px',
                    filterable : false
                };
                columns.push(column);
            }
        }

        var options = {
            columns : grupos,
            editable : 'inline',
            sortable : true,
            reorderable : true,
            filterable : { extra : true },
            filterMenuInit : onFilterMenuInit,
            groupable : false,
            pageable : true,
            selectable : 'multiple row',
            reorderable : true,
            allowCopy : true,
            resizable : true,
            autoBind : false,
            scrollable : true,
            height : 350,
            dataSource : _model.getDataSourceDetalles(codigoValidacion),
            dataBound : onDataBound
        };

        return options;
    }

    _model.getGridCargaOptions = function() {
        var options = {
            columns : [
                { field : 'ID', title: 'Reporte', locked : true, width : '100px', template : '<a class="fileDownload" href="/BSCL/Plataforma/GWSERVICES/files/download?schema=' + SCHEMA + '&tipo=reporte&idDocumento=#=IdDocumento#&idProceso=#=IdProceso#">Resultado #=IdProceso#</a>' },
                { field : 'Fecha', title: 'Fecha', format : '{0:dd-MM-yyyy}', width : '60px' },
                { field : 'Glosa', title: 'Glosa', width : '60px' },
                { field : 'NombreArchivo', title: 'Nombre Archivo', width : '240px' },
                { field : 'TotalRegistrosLeidos', title: '# Regs. Leidos', width : '100px', attributes : { style : 'text-align:right;' } },
                { field : 'TotalRegistrosCargados', title: '# Regs. Cargados', width : '120px', attributes : { style : 'text-align:right;' } },
                { field : 'TotalRegistrosErroneos', title: '# Regs. Erroneos', width : '120px', attributes : { style : 'text-align:right;' } }
            ],
            editable : 'inline',
            sortable : true,
            reorderable : true,
            filterable : { extra : true },
            groupable : false,
            pageable : true,
            selectable : 'multiple, row',
            reorderable : true,
            allowCopy : true,
            resizable : true,
            autoBind : true,
            scrollable : true,
            height : 360,
            dataSource : _model.getDataSourceResumenCarga()
        };

        return options;
    }

    _model.getGridCuadraturaOptions = function() {
        var options = {
            columns : [
                { field : 'ID', title: 'Reporte', locked : true, width : '100px', template : '<a class="fileDownload" href="/BSCL/Plataforma/GWSERVICES/files/download?schema=' + SCHEMA + '&tipo=reporte&idDocumento=#=IdDocumento#&idProceso=#=IdProceso#">Resultado #=IdProceso#</a>' },
                { field : 'AnoMes', title: 'Mes', format : '{0:MM-yyyy}', width : '50px' },
                { field : 'FechaCuadratura', title: 'Fecha Cuadratura', format : '{0:dd-MM-yyyy}', width : '60px' },
                { field : 'TipoCuadratura', title: 'Tipo Cuadratura', width : '60px' },
                { field : 'TipoDiferencia', title: 'Tipo Diferencia', width : '60px' },
                { field : 'TipoClasificador', title: 'Clasificador', width : '60px' },
                { field : 'Glosa', title: 'Glosa', width : '100px' },
                { field : 'CantidadCasos', title: '# Casos', attributes : { style : 'text-align:right;' }, width : '120px' }
            ],
            editable : 'inline',
            sortable : true,
            reorderable : true,
            filterable : { extra : true },
            groupable : false,
            pageable : true,
            selectable : 'multiple, row',
            reorderable : true,
            allowCopy : true,
            resizable : true,
            autoBind : true,
            scrollable : true,
            height : 360,
            dataSource : _model.getDataSourceResumenCuadratura()
        };

        return options;
    }

    _model.getGridGeneracionOptions = function() {
        var options = {
            editable : 'inline',
            sortable : true,
            reorderable : true,
            filterable : { extra : true },
            userSettings		: true,
            groupable : false,
            //pageable : true,
            pageable: {
                refresh: true,
                //pageSizes: true,
                buttonCount: 5
            },
            selectable : 'multiple, row',
            reorderable : true,
            allowCopy : true,
            resizable : true,
            autoBind : true,
            scrollable : true,
            height : 360,
            dataSource : _model.getDataSourceResumenGeneracion(),
            columns : [
                { field : 'ID', title: 'Reporte', locked : true, width : '100px', template : '<a class="fileDownload" href="/BSCL/Plataforma/GWSERVICES/files/download?schema=' + SCHEMA + '&tipo=reporte&idDocumento=#=IdDocumento#&idProceso=#=IdProceso#">Resultado #=IdProceso#</a>' },
                { field : 'IdDocumento', title: 'Resultado', locked : true, width : '100px', template : '<a class="fileDownload" href="/BSCL/Plataforma/GWSERVICES/files/download?schema=' + SCHEMA + '&tipo=resultado&idDocumento=#=IdDocumento#&idProceso=#=IdProceso#' + '#=(NombreArchivo==null? "" : "&nombreArchivo="+NombreArchivo)#' + '">#=(NombreArchivo==null? "Descargar" : NombreArchivo)#</a>' },
                { field : 'IdProceso', title: 'ZIP', locked : true, width : '100px', template : '<a class="fileDownload" href="/BSCL/Plataforma/GWSERVICES/files/download?schema=' + SCHEMA + '&tipo=resultado&idDocumento=#=IdDocumento#&idProceso=#=IdProceso#&zipped=true' + '#=(NombreArchivo==null? "" : "&nombreArchivo="+NombreArchivo)#' + '">#=(NombreArchivo==null? "Descargar" : NombreArchivo)# ZIP</a>' },
                { field : 'Fecha', title: 'Fecha Generacion', format : '{0:dd-MM-yyyy}', width : '120px' },
                { field : 'Fecha', title: 'Hora Generacion', format : '{0:hh:mm:ss}', width : '120px' },
                { field : 'Glosa', title: 'Glosa', width : '140px' },
                { field : 'TotalRegistrosGenerados', title: '# Regs. Generados', attributes : { style : 'text-align:right;' }, width : '120px' },
                { field : 'Usuario', title: 'Usuario', width : '80px' }
            ],
			rowCellColor : function(row) {
				switch(row.get("Glosa").trim()) {
					case 'Finalizada':
						if (row.TotalRegistrosGenerados > 0)
							return {column: "TotalRegistrosGenerados", color: "green"};
		            	else
		               		return {column: "TotalRegistrosGenerados", color: "yellow"};
		                break;
					case '*':
						if (row.TotalRegistrosGenerados > 0)
							return {column: "TotalRegistrosGenerados", color: "green"};
		            	else
		               		return {column: "TotalRegistrosGenerados", color: "yellow"};
		                break;    
					case '<':
						return {column: "TotalRegistrosGenerados", color: "yellow"};
	    	            break;
				    default:
		            	return {column: "TotalRegistrosGenerados", color: "white"};
		                break;  	
				}
			},
			rowColorBackground	: function(row) {		
				switch(row.get("Glosa").trim()) {
		            case 'E':
		                return "red";
		                break;
		            case 'Finalizada':
		            	if (row.TotalRegistrosGenerados > 0)
							return "yellow";
		            	else
		                return "green";
		                break;
		            case '*':
		            	if (row.TotalRegistrosGenerados > 0)
							return "yellow";
		            	else
		                return "green";
		                break;   
		             case '<':
		                return "blue";
		                break;    
		            default:
		            	return "white";
		                break;     
				  }	
				}
        };

        return options;
    }
    
    
    _model.getGridValidacionOptions = function() {
        var options = {
            editable : 'inline',
            sortable : true,
            reorderable : true,
            filterable : { extra : true },
            groupable : false,
            pageable : true,
            selectable : 'multiple, row',
            reorderable : true,
            allowCopy : true,
            resizable : true,
            autoBind : true,
            scrollable : true,
            height : 360,
            dataSource : _model.getDataSourceResumenValidacion(),
            columns : [
                { field : 'CodigoValidacion', title: 'Cod. Validacion', attributes : { style : 'text-align:left;' } },
                { field : 'DescripcionValidacion', title: 'Descripcion', attributes : { style : 'text-align:left;' } },
                { field : 'CantidadCasos', title: '# Casos', attributes : { style : 'text-align:right;' } },
                { field : 'FechaValidacion', title: 'Fecha', format : '{0:dd-MM-yyyy}' }
            ]
        };

        return options;
    }

    function onFilterMenuInit(e) {
        var c = null;
        for (var i = 0; i < _campos.length; i++) {
            if (_campos[i].Nombre === e.field) {
                c = _campos[i];
                break;
            }
        }

        if (c != null && c.isList()) {
            initListFilter.call(this, e);
        }
    }

    function initListFilter(e) {
        var popup = e.container.data('kendoPopup');
        var helpTextElement = e.container.children(':first').children(':first');
        helpTextElement[0].innerHTML = 'Seleccione un valor';
        var opr = helpTextElement.next();
        var val = opr.next();
        var div = val.next();
        div.children(':first').next().hide();
        opr.hide();
    }

    /**
     * Obtiene el data source para la consulta de totales. Usa el esquema actual
     * y sus campos asociados. Además referencia a los filtros que afectaran el
     * resultado de la consulta.
     */
    _model.getDataSourceTotales = function(success, error) {
        getUrlServicio(_esquema, 'ObtenerTotales', {r : 'P_OUT_TBL'}, function(url) {
            var fields = {};
            for (var i = 0; i < _columnasTotalesComunes.length; i++) {
                var campo = _columnasTotalesComunes[i];
                fields[campo.Nombre] = { type : campo.getType() };
            }
            for (var i = 0; i < _campos.length; i++) {
                var campo = _campos[i];
                var type = campo.getType();
                if (campo.isSumable()) {
                    fields[campo.Nombre] = { type : type };
                }
            }

            var dataSource = new kendo.data.DataSource({
                type : 'json',
                serverFiltering : false,
                serverPaging : false,
                autoBind : false,
                serverSorting : false,
                transport : {
                    read : function(options) {
                        $.ajax({
                            url : url,
                            dataType : 'json',
                            type : 'post',
                            contentType : 'application/json; charset=UTF-8',
                            data : JSON.stringify(_filtrosDeTotales),
                            success : function(result) {
                                var totals = [];
                                for (var item in result.P_OUT_TBL) {
                                    totals.push(result.P_OUT_TBL[item]);
                                }
                                options.success(totals);
                            },
                            error : function(result) {
                                options.error(result);
                            }
                        });
                     }
                },
                schema : {
                    model : {
                        fields : fields
                    }
                }
            });

            success(dataSource);
        },
        error);
    }
     _model.getDataSourceTotalesSICAM = function(success, error) {
        getUrlServicio(_esquema, 'ObtenerTotales', {r : 'P_OUT_TBL'}, function(url) {
            var fields = {};
            for (var i = 0; i < _columnasTotalesComunes.length; i++) {
                var campo = _columnasTotalesComunes[i];
                fields[campo.Nombre] = { type : campo.getType() };
            }
            fields['Monto'] = { type : TYPE_DECIMAL };
            var dataSource = new kendo.data.DataSource({
                type : 'json',
                serverFiltering : false,
                serverPaging : false,
                autoBind : false,
                serverSorting : false,
                transport : {
                    read : function(options) {
                        $.ajax({
                            url : url,
                            dataType : 'json',
                            type : 'post',
                            contentType : 'application/json; charset=UTF-8',
                            data : JSON.stringify(_filtrosDeTotales),
                            success : function(result) {
                                var totals = [];
                                for (var item in result.P_OUT_TBL) {
                                    totals.push(result.P_OUT_TBL[item]);
                                }
                                options.success(totals);
                            },
                            error : function(result) {
                                options.error(result);
                            }
                        });
                     }
                },
                schema : {
                    model : {
                        fields : fields
                    }
                }
            });
            success(dataSource);
        },
        error);
    }

    /**
     * Obtiene las opciones para la grilla que contiene los totales. Usa el
     * esquema actual y sus campos asociados.
     */
    _model.getGridSumsOptions = function(dataSource) {
        var columns = [];
        for (var i = 0; i < _columnasTotalesComunes.length; i++) {
            var campo = _columnasTotalesComunes[i];
            columns.push(getField(campo));
        }
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.isSumable()) {
                columns.push(getField(campo));
            }
        }

        return {
            columns : columns,
            sortable : false,
            reorderable : false,
            filterable : false,
            groupable : false,
            pageable : false,
            selectable : 'multiple, row',
            reorderable : false,
            allowCopy : true,
            resizable : true,
            autoBind : false,
            scrollable : true,
            height : 100,
            dataSource : dataSource
        };
    }
    _model.getGridSumsOptionsSICAM = function(dataSource) {
        var columns = [];
        for (var i = 0; i < _columnasTotalesComunes.length; i++) {
            var campo = _columnasTotalesComunes[i];
            columns.push(getField(campo));
        }
 		columns.push({ field : 'Monto', title : 'Monto', format:"{0:n5}", hidden : false ,width : "100px", attributes : {style: 'text-align:left;'}});
        return {
            columns : columns,
            sortable : false,
            reorderable : false,
            filterable : false,
            groupable : false,
            pageable : false,
            selectable : 'multiple, row',
            reorderable : false,
            allowCopy : true,
            resizable : true,
            autoBind : false,
            scrollable : true,
            height : 100,
            dataSource : dataSource
        };
    }
    /**
     * Ejecuta el servicio asociado al esquema/metodo.
     */
    _model.callService = function(metodo, params, success, error) {
        getUrlServicio(_esquema, metodo, {}, function(url) {
            var service = $resource('', {}, { call: { method: 'POST', url: url } });
            var post = service.call(params);
            post.$promise.then(success).catch(error);
        },
        error);
    }

    _model.callServiceById = function(id, params, success, error) {
        var url = getUrlServicioById(id, {});
        var service = $resource('', {}, { call: { method: 'POST', url: url } });
        var post = service.call(params);
        post.$promise.then(success).catch(error);
    }

    _model.agregarPrograma = function(params, success, error) {
        _model.callServiceById(ID_SERVICIO_AGREGAR_PROGRAMA, params, success, error);
    }

    _model.guardarDocumento = function(params, success, error) {
        _model.callServiceById(ID_SERVICIO_GUARDAR_DOCUMENTO, params, success, error);
    }

    /**
     * Obtiene los filtros de los totales para llamar al servicio
     * correspondiente.
     */
    _model.getFiltrosDeTotales = function(filter) {
        var filtros = {};

        if (typeof filter === 'undefined' || filter == null) {
            return filtros;
        }

        var filters = filter.filters;

        for (var i = 0; i < filters.length; i++) {
            var filter = filters[i];
            if (filter.field !== 'IdDocumento') {
                switch (filter.operator) {
                case 'eq':
                    filtros['p_' + filter.field + '_Desde'] = filter.value;
                    filtros['p_' + filter.field + '_Hasta'] = filter.value;
                    break;
                case 'gte':
                    filtros['p_' + filter.field + '_Desde'] = filter.value;
                    break;
                case 'lte':
                    filtros['p_' + filter.field + '_Hasta'] = filter.value;
                    break;
                case 'contains':
                    filtros['p_' + filter.field] = '%' + filter.value + '%';
                    break;
                case 'startswith':
                    filtros['p_' + filter.field] = filter.value + '%';
                    break;
                case 'endswith':
                    filtros['p_' + filter.field] = '%' + filter.value;
                    break;
                }
            }
            
            var filters2 = filter.filters;
            if (filters2 != undefined)
            {
		            for (var x = 0; x < filters2.length; x++) {
		                var filter = filters2[x];
		                if (filter.field !== 'IdDocumento') {
		                    switch (filter.operator) {
		                    case 'eq':
		                        filtros['p_' + filter.field + '_Desde'] = filter.value;
		                        filtros['p_' + filter.field + '_Hasta'] = filter.value;
		                        break;
		                    case 'gte':
		                        filtros['p_' + filter.field + '_Desde'] = filter.value;
		                        break;
		                    case 'lte':
		                        filtros['p_' + filter.field + '_Hasta'] = filter.value;
		                        break;
		                    case 'contains':
		                        filtros['p_' + filter.field] = '%' + filter.value + '%';
		                        break;
		                    case 'startswith':
		                        filtros['p_' + filter.field] = filter.value + '%';
		                        break;
		                    case 'endswith':
		                        filtros['p_' + filter.field] = '%' + filter.value;
		                        break;
		                    }
		                }
		            }
            }
            
            
        }

        return filtros;
    }

    /**
     * Obtiene diccionario para filtro del tipo solicitado.
     */
    _model.getDiccionario = function(tipo, todos) {
        var lista = [];
        if (todos) {
            lista.push({Codigo: '', Descripcion : 'TODOS'});
        }
        for (var i = 0; i < _diccionarios.length; i++) {
            if (_diccionarios[i].Tipo === tipo) {
                lista.push(_diccionarios[i]);
            }
        }
        return lista;
    }

    _model.existsField = function(nombre) {
        for (var i = 0; i < _campos.length; i++) {
            var campo = _campos[i];
            if (campo.Nombre === nombre) {
                return true;
            }
        }
        return false;
    }

    _model.getAyudaCampo = function(text) {
        if (text.startsWith("Filtrar")) {
            text = text.substring(7);
        }
        var campo = _model.getCampoPorTitulo(text);
        var ayuda = '';
        if (campo != null) {
            if (campo.CodigoSII != null
                    && campo.CodigoSII.length > 0) {
                ayuda = '[' + campo.CodigoSII + '] ';
            }
            ayuda += campo.Ayuda;
        }
        return ayuda;
    }
    
    _model.getValorParametro = function(text) {
        var parametro = _model.getParametroPorCodigo(text);
        var valor = '';
        if (parametro != null) {
        	valor += parametro.Valor;
        }
        return valor;
    }
    
    var _datosEsquemas;

    _model.setDatosEsquemas = function(datosEsquemas) {
        _datosEsquemas = datosEsquemas;
    }

    _model.getModulo = function() {
        for (var i = 0; i < _datosEsquemas.length; i++) {
            if (_datosEsquemas[i].Esquema === _esquema) {
                return _datosEsquemas[i].Modulo;
            }
        }
        return null;
    }

    return _model;
}

function Utils(bcsComunes) {

    var _utils = {};
    var _windowIds = {};

    _utils.openWindow = function(name, params) {
        var id = _windowIds[name];
        /*
         * if (typeof id != 'undefined') { bcsComunes.ventanaCerrar(id);
         * _windowIds[name] = undefined; }
         */
        _windowIds[name] = bcsComunes.ventanaAbrir(name, params);
    }

    _utils.setWindowTitle = function($scope, title) {
        var id = bcsComunes.ventanaWinId($scope);
        bcsComunes.ventanaTitulo(id, title);
    }

    _utils.getWindowParams = function($scope) {
        var id = bcsComunes.ventanaWinId($scope);
        return bcsComunes.ventanaParams(id);
    }

    _utils.destroyGrid = function(grid) {
        if (grid == null)
            return;
        var w = grid.wrapper;
        if (w != null) {
            grid.destroy();
            w.empty();
        }
    }

    _utils.getUniqueByKey = function(data, key) {
        var objs = [];
        var keys = [];
        $.each(data, function(index, item) {
            var k = item[key];
            if ($.inArray(k, keys) < 0) {
                keys.push(k);
                objs.push(item);
            }
        });
        return objs;
    }

    _utils.getPrintable = function(valor) {
        if (valor instanceof Date) {
            return "'" + valor.getDate()
                + '-' + (valor.getMonth() + 1)
                + '-' + valor.getFullYear() + "'";
        }
        if (!isNaN(valor))
            return valor;
        return "'" + valor + "'";
    }

    _utils.isEmptyValue = function(valor) {
        if (valor == null)
            return true;
        if (typeof valor === 'undefined')
            return true;
        if (typeof valor === 'string' && valor.trim().length == 0)
            return true;
        return false;
    }

    _utils.getNormalDate = function(valor) {
        if (valor == null)
            return null;
        var x = valor.indexOf('-');
        if (x != 2) return valor;
        var dd = valor.substring(0, 2);
        var mm = valor.substring(3, 5);
        var yyyy = valor.substring(6, 10);
        return yyyy + '-' + mm + '-' + dd;
    }

    _utils.formatDate = function(date) {
        if (date == null)
            return null;
        var yyyy = date.getFullYear().toString();
        var mm = (date.getMonth()+1).toString();
        if (mm.length < 2) mm = '0' + mm;
        var dd = date.getDate().toString();
        if (dd.length < 2) dd = '0' + dd;
        return yyyy + '-' + mm + '-' + dd;
    }
    
    _utils.showError = function(error, message, winid) {
    	var msg = 'Error';
    	if (typeof error === 'string') {
    		msg = error;
    	} else if (typeof error.message !== 'undefined') {
			msg = error.message;
		} else if (typeof error.statusText !== 'undefined') {
			msg = error.statusText;
		}
    	
    	bcsComunes.ventanaMsg({
    		mensaje : message,
    		conflictos : {
    			advertencias : [],
    			autorizaciones : [],
    			errores : [ {
    				Codigo : 'COD',
    				Descripcion : msg
    			} ],
    			erroresFatales : [],
				informaciones : []
    		}
    	}, winid);
    }

    _utils.showErrors = function(errors, message, winid) {
    	var errores = [];
    	for (var i = 0; i < errors.length; i++) {
    		errores.push({
				Codigo : 'COD',
				Descripcion : errors[i]
			});
    	}
    	
    	bcsComunes.ventanaMsg({
    		mensaje : message,
    		conflictos : {
    			advertencias : [],
    			autorizaciones : [],
    			errores : errores,
    			erroresFatales : [],
				informaciones : []
    		}
    	}, winid);
    }

    _utils.showInfo = function(msg, message, winid) {
    	bcsComunes.ventanaMsg({
    		mensaje : message,
    		conflictos : {
    			advertencias : [],
    			autorizaciones : [],
    			errores : [],
    			erroresFatales : [],
    			informaciones : [ {
    				Codigo : 'COD',
    				Descripcion : msg
    			} ]
    		}
    	}, winid);
    }

    _utils.showWarning = function(msg, message, winid) {
    	bcsComunes.ventanaMsg({
    		mensaje : message,
    		conflictos : {
    			advertencias : [ {
    				Codigo : 'COD',
    				Descripcion : msg
    			} ],
    			autorizaciones : [],
    			errores : [],
    			erroresFatales : [],
    			informaciones : []
    		}
    	}, winid);
    }

    _utils.getFileNameFromHttpResponse = function(httpResponse) {
        var contentDispositionHeader = httpResponse.headers('Content-Disposition');
        var result = contentDispositionHeader.split(';')[1].trim().split('=')[1];
        return result.replace(/"/g, '');
    }

    return _utils;
}