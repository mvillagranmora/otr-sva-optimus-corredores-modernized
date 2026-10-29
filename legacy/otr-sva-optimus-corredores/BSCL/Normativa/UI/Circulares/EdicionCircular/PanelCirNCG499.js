(function() {
	angular.module('ModuloNormativa').controller('PanelCirNCG499trl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,bcsServicioPlataforma,$compile,
				 bcsAlerta, $rootScope, OPCIONES_BCSALERTA,$http,$timeout){

		
	/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	 var idVentana = bcsComunes.ventanaWinId($scope);
	 $scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
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
	 pct.IdTipoDocumentoComprobante='UAFPerfilCOMP';
	 pct.IdVersionEsquemaComprobante 	= '';
	 pct.modulo='NOR';
	 pct.salidaMensaje='';
	 pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
	 pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
	 $scope.data = {};
	 $scope.showDetalle = false;
	 $scope.filtros = {};
	 $scope.accionBoton = $scope.data.AccionBoton;
     $scope.filtros.validarBotonValorizar = "false";
     let fechaValorizarStr = util.dateSPdate(pct.FecTermino);

 
	 
	 $scope.dataTemplate	= {};
	 
	 /*********************************************
	  * ACCIONES DE PANTALLA
	  ********************************************/
	 
	 
	 /*********************************************
	  * DATASOURCES
	  *********************************************/
	 
	 pct.grilla.CirCMF = {
		 dataSource: new bcs.DataSource({
			 type : "odata",
			 definition : {
				 serverFiltering 	: true,
				 serverPaging     : true,
				 serverSorting    : true,
				 pageSize         : 100,
				
				 transport 		: {
					  read : {
						  url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010104",
						  dataType : "json"
					  }
				 },
				 schema : {
					model: {
						fields: {
							Fecha												: { type: "date"},
							IdClasificacionTipoInstrumentoModificado			: { type: "number"},
							Nemotecnico											: { type: "string"},
							ISIN												: { type: "string"},
							SituacionInstrumento								: { type: "string"},
							SituacionInstrumentoAux								: { type: "string"},
							RutCliente											: { type: "string"},
							Digito											    : { type: "string"},
							NombrePersona										: { type: "string"},
							CodigoPais 											: { type: "string"},
							IdTipoInstrumento									: { type: "string"},
							FechaVencimiento									: { type: "date"},
							TendenciaClasificacionriesgo						: { type: "string"},
							ClasificacionRiesgoInstrumento						: { type: "string"},
							CantidadUnidades								    : { type: "string"},
							IdMonedaPrincipal									: { type: "string"},									
							Tir											        : { type: "number"},
							CodigoValorizacion 								    : { type: "string"},
							BaseTasa 										    : { type: "number"},
							ValorizacionCierre								    : { type: "string"},
							CodigoPaisEmisionInstrumento					    : { type: "string"},
							Relacionado                                         : { type: "string"},
							FuentePrecios 									    : { type: "string"},						
							Duration 										    : { type: "string"},
                            IdEstado				                            : { type: "string"},
                            IdInstrumento										: { type: "number"},

						 }
					 }
				 }
			 }
		 }),
		 columns				: [
			{field: "Fecha",					title: 'UI_FECHA',						width: "110px", attributes:{style:"text-align:left;"}	},		         
			{field: "IdClasificacionTipoInstrumentoModificado",						title: 'UI_CLASIFICACIONINSTRUMENTO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},		         			
			{field: "Nemotecnico", 				title: 'UI_NEMOTECNICO', width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "ISIN", 				title: 'UI_ISIN',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "SituacionInstrumento", 				title: 'UI_SITUACION_INSTRUMENTO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "RutCliente",				title: 'UI_RUT_EMISOR',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Digito",				title: 'UI_DIGITO',width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "NombrePersona",	title: 'UI_RAZON_SOCIAL_EMISOR',	 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoPais",	title: 'UI_CODIGO_PAIS',	 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "IdTipoInstrumento",			title: 'UI_TIPO_INSTRUMENTO', 	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "FechaVencimiento",						title: 'UI_FECHA_VENCIMIENTO', 	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "ClasificacionRiesgoInstrumento",	 					title: 'UI_CLASIFICACION_RIESGO',	 width: "75px", attributes: { style: "text-align:center;" } },
			{field: "TendenciaClasificacionriesgo",	 					title: 'UI_TENDENCIA_CLASIFICACION_RIESGO',	 width: "75px", attributes: { style: "text-align:center;" } },
			{field: "CantidadUnidades",	 					title: 'UI_CANTIDAD_UNIDADES',	 width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "IdMonedaPrincipal",					title: 'UI_MONEDA_PRINCIPAL',	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "Tir",					title: 'TIR/PRECIO',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoValorizacion",					title: 'UI_C._VALORIZACION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "BaseTasa",					title: 'UI_BASE_TASA',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "ValorizacionCierre",					title: 'UI_VAL._CIERRE',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "CodigoPaisEmisionInstrumento",					title: 'UI_C._PAIS_EMISION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "FuentePrecios",					title: 'UI_FUENTE_PRECIOS',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			{field: "Relacionado",					title: 'UI_RELACIONADO',	width: "75px", attributes: { style: "text-align:center;" } },
			{field: "Duration",					title: 'UI_DURACION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},
			// {field: "IdEstado",					title: 'UI_ESTADO',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	},

			
					
				
		],
		pageable: true,
		pageSize: 50,
		autoBind: false,
		height: 490,
		selectable: "row",
        rowColorBackground: function(row) {
            switch(row.get("IdEstado")) {
                case 'DES':
					if(row.get("CantidadUnidades") < 0){
						return "red";
					}else{
						return "yellow";
					}
				case 'NEG':
					if(row.get("CantidadUnidades") < 0){
						return "red";
					}
            }
        },
		change: function(e) {
			var selectedData = this.dataItem(this.select());
			$scope.data.seleccionado = selectedData;
			$scope.$apply();
		},
		userSettings: true,
		navigatable: true,
		excel: {
			allPages: true,
		}
		
	};
	

	ModeloFechas = function() {
		return {
			model: {
				fields: {
					Fecha: { type: "date" }
				}
			}
		};
	};
	
	dataSourceFechas = function() {
		return new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				serverPaging: true,
				serverSorting: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010104",
						dataType: "json"
					}
				},
				schema: ModeloFechas()
			}
		});
	};
	
    $scope.fechas = [];
    $scope.fechasDataSource = dataSourceFechas();
    $scope.fechasDataSource.fetch(function() {
        var data = this.data();
        $scope.$apply(function() {
            $scope.fechas = data;
        });
    });

    $scope.existeFecha = function(fechaBuscada) {            
        var fechaBuscadaStr = util.dateSPdate(fechaBuscada);
        
        return $scope.fechas.some(function(fecha) {
            var fechaStr = util.dateSPdate(fecha.Fecha);
            return fechaStr === fechaBuscadaStr;
        });
    };

	pct.fn.buscar = function() {

        if (!$scope.existeFecha(pct.FecTermino)) {
			bcsComunes.msg("error", "error", "No existen registros para la fecha seleccionada");
			return;
		}
	
		var fechaFilter = {
			field: "Fecha",
			operator: "eq",
			value: pct.FecTermino
		};
	
		if (pct.grilla.CirCMF && pct.grilla.CirCMF.dataSource) {
			pct.grilla.CirCMF.dataSource.filter(fechaFilter);
		} else {
			bcsComunes.msg("error", "error", "La grilla no está correctamente inicializada");
		}
	};

    const esFechaInhabil = (fecha) => {
        console.log('fechaFunction', fecha);
        const result = !util.esFechaHabil($scope.$root.SesionIdPaisSistema, fecha) || util.esFechaFeriado($scope.$root.SesionIdPaisSistema, fecha)
        console.log("esFechaInhabil: ", result);
        return result;
    }

    $scope.$watch('pct.FecTermino', function(newValue, old) {
        console.log('newFechaPago', newValue);
        console.log('oldFechaPago', old);
        if (newValue !== old && newValue) {
            if(esFechaInhabil(newValue)) {
                $scope.filtros.validarBotonValorizar = "true"
            } else {
                $scope.filtros.validarBotonValorizar = "false"
            }
        }
    });

    pct.Valorizar = function() {

        if (!$scope.existeFecha(pct.FecTermino)) {
			bcsComunes.msg("error", "error", "No existen registros para la fecha seleccionada");
			return;
		}

        bcsComunes.ventanaOverlay(idVentana, true);

		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdPais =  $scope.$root.SesionIdPaisSistema;
        parametrosSP.p_IdModulo = 'NOR';
        parametrosSP.p_Fecha = fechaValorizarStr;

        var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_CONFIRMA" ) + " " + "Valorizar Circular ?", idVentana);
        t.then(function(result) {		
                    if (result===OPCIONES_BCSALERTA.SI) {	
                             
                        var entradaPrograma = {};					
                        entradaPrograma.p_IdEmpresa			= $scope.$root.SesionIdEmpresa;
                        entradaPrograma.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
                        entradaPrograma.p_IdModulo    		= 'CUS';
                        entradaPrograma.p_IdAgenda    		= 0;    
                        entradaPrograma.p_IdTipoTarea   	= 'VALORIZAR_NCG499';
                        entradaPrograma.p_Segundos      	= 0;			
                        entradaPrograma.p_CreaParam     	= 'S';
                        entradaPrograma.p_TablaParametros = [
                            {NombreParametro: 'IdEmpresa',  Valor: $scope.$root.SesionIdEmpresa},
                            {NombreParametro: 'IdUsuario',  Valor: $scope.$root.SesionIdUsuario},
                            {NombreParametro: 'IdPais',     Valor: $scope.$root.SesionIdPaisSistema},
                            {NombreParametro: 'IdModulo',   Valor: 'CUS'},
                            {NombreParametro: 'Fecha', 	  	Valor: util.dateSPdate(pct.FecTermino)},
                        ];

                        console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
                                    
                        bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
                            function(respuesta){
                                if (respuesta.P_OUT_ERRORESCONTADOR === 0) {						
                                    Programa = respuesta.P_OUT_IDPROGRAMA;

                                    if (Programa.length > 0) 
                                        idPrograma = Programa[0].IdPrograma;

                                    bcsComunes.ventanaMsg({
                                        mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea su ejecucion en Consultar Programas."							
                                    }, idVentana);						
                                } else {						
                                    bcsComunes.ventanaMsg({
                                        mensaje:$translate.instant("ERROR")							
                                    }, idVentana);
                                }
                            }, function(respuesta) {
                                bcsComunes.ventanaMsg({
                                    mensaje:$translate.instant("ERROR"),
                                    conflictos:util.parseServerResponse(respuesta)
                                }, idVentana);					
                            }
                        );	
                    };
            });
    }

    pct.ValorizarBuscar = function(fechaValorizar) {

        if (!$scope.existeFecha(pct.FecTermino)) {
			bcsComunes.msg("error", "error", "No existen registros para la fecha seleccionada");
			return;
		}

        return new Promise(function(resolve, reject) {
            bcsComunes.ventanaOverlay(idVentana, true);
            bcsComunes.msg("info", "info", "Fecha inhabil. Inicia proceso de valorizacion.");
    
            const valNcg = {
                p_IdEmpresa: $scope.$root.SesionIdEmpresa,
                p_IdUsuario: $scope.$root.SesionIdUsuario,
                p_IdModulo: "CUS",
                p_IdPais: $scope.$root.SesionIdPaisSistema,
                p_Fecha: fechaValorizar,
            }
    
            try {
                console.log(valNcg);
                bcsServiciosNormativa.ValorizarNCG499(valNcg).$promise.then(function(respuesta) {
                    console.log(respuesta);
                    if(respuesta.P_OUT_ERRORESCONTADOR === 0) {
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.ventanaMsg({
                            mensaje: respuesta.P_OUT_MENSAJE							
                        }, idVentana);
                        resolve();	
                    }
                }, function(respuesta){
                    bcsComunes.ventanaOverlay(idVentana, false);
                    bcsComunes.ventanaMsg({
                        mensaje: $translate.instant("ERROR"),
                        conflictos: util.parseServerResponse(respuesta)
                    }, idVentana);
                    reject(respuesta);
                });
            } catch (error) {
                bcsComunes.ventanaOverlay(idVentana, false);
                console.error('Error al valorizar carteras', error);
                bcsComunes.msg("error", "UI_ERROR", "Error al valorizar carteras");
                reject(error);
            }
        });
    } 

	pct.Modificar = function(seleccionado) {
		if (!seleccionado) {
			bcsComunes.msg("error", "error",'Por favor, selecciona un registro para modificar.');
			return; 
		}
		bcsComunes.ventanaOverlay(idVentana, true);
    	$scope.accionBoton = false;
		var ventanaModalId = bcsComunes.ventanaAbrir("ModalCirNCG499", {
            Fecha:$scope.data.seleccionado.Fecha,
            IdClasificacionTipoInstrumentoModificado:$scope.data.seleccionado.IdClasificacionTipoInstrumentoModificado,
            Nemotecnico:$scope.data.seleccionado.Nemotecnico,
			IdEmisor: $scope.data.seleccionado.IdEmisor,
            ISIN:$scope.data.seleccionado.ISIN,
			SituacionInstrumento:$scope.data.seleccionado.SituacionInstrumento,								
			RutCliente:$scope.data.seleccionado.RutCliente,
			Digito:$scope.data.seleccionado.Digito,										  
			NombrePersona:$scope.data.seleccionado.NombrePersona,										
			CodigoPais:$scope.data.seleccionado.CodigoPais,										
			IdTipoInstrumento:$scope.data.seleccionado.IdTipoInstrumento,									
			FechaVencimiento:$scope.data.seleccionado.FechaVencimiento,									
			TendenciaClasificacionriesgo:$scope.data.seleccionado.TendenciaClasificacionriesgo,						
			ClasificacionRiesgoInstrumento:$scope.data.seleccionado.ClasificacionRiesgoInstrumento,						
			CantidadUnidades:$scope.data.seleccionado.CantidadUnidades,								   
			IdMonedaPrincipal:$scope.data.seleccionado.IdMonedaPrincipal,																		
			Tir:$scope.data.seleccionado.Tir,											       
			CodigoValorizacion:$scope.data.seleccionado.CodigoValorizacion, 								
			BaseTasa:$scope.data.seleccionado.BaseTasa, 										
			ValorizacionCierre:$scope.data.seleccionado.ValorizacionCierre,								
			CodigoPaisEmisionInstrumento:$scope.data.seleccionado.CodigoPaisEmisionInstrumento,					
			Relacionado:$scope.data.seleccionado.Relacionado,                                   
			FuentePrecios:$scope.data.seleccionado.FuentePrecios,															
			Duration:$scope.data.seleccionado.Duration,
			AccionBoton: 'MODIFICAR',
			IdOrigen : $scope.data.seleccionado.IdOrigen,
            IdEstado: $scope.data.seleccionado.IdEstado,
            SituacionInstrumentoAux: $scope.data.seleccionado.SituacionInstrumentoAux,
            IdInstrumento: $scope.data.seleccionado.IdInstrumento
            }, true, false);
			
			var promesa = bcsComunes.ventanaPromesa(ventanaModalId);

			$.when(promesa).then(function() {
				bcsComunes.ventanaOverlay(idVentana, false);
				pct.grilla.CirCMF.dataSource.read();
			});
		};
	   		
	pct.fn.limpiar = function(){
		pct.grilla.CirCMF.dataSource.removeAll();
	};
	
	var array = [		
		{ Estado: 'Activo', Color: 'black' },
		{ Estado: 'Carteras Negativas', Color: 'red' },
		{ Estado: 'Excluido', Color: 'yellow' }
		
	];
	
	bcsComunes.tootipGlosa('toolInformacionCarteras', array)   
	 

 });
}());