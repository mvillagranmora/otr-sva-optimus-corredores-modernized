angular.module('ModuloOrdenes').controller('InterfazRefacturacionCtrl',
    function($scope, bcsServicioOrden, bcsConsultasOrdenes, bcsServiciosFacturacion, bcsComunes, bcsAlerta, bcsConfiguracionOrdenesV2, bcsServicioPlataforma, $timeout, $translate, $q, $rootScope, $resource) {
        var jpmColor ="";
        $scope.idVentana = bcsComunes.ventanaWinId($scope);;
        $scope.formData = {};
        $scope.formData.entidadesRefacturar = [];
        $scope.formData.CobrosFijos = [];

        var parametro = BCS_CONFIGURACION.FACTURACION["TOTALREFACTURACION"];
        $scope.parametros = bcsComunes.ventanaParams($scope.idVentana);
        $scope.formData.tipoCargaSaldoTransacciones = "";
      
        var cobrosAsignacionAnterior = [];
        $scope.formData.CobrosOrdenArrastre = true;
        

        if (!$scope.parametros.Origen) {
            $scope.parametros.Origen = "COMP";
        };
        if (!$scope.parametros.FormaAsignacion) {
            $scope.parametros.FormaAsignacion = "REFACTURACION";
        };

        $scope.formData.IdMonedaSistema = $rootScope.SesionIdMonedaSistema;

        bcsComunes.ventanaTitulo($scope.idVentana, ($scope.parametros.Origen === "ASIG") ? $translate.instant("UI_INTERFAZ_REASIGNACION") : $translate.instant("UI_INTERFAZ_REFACTURACION"));


        $scope.formData.transaccionesSaldos = [];
        $scope.formData.transaccionesComprobantes = [];
        $scope.formData.asignacionesComprobantes = [];

//---------------// DEFINICION DE DATASOURCES Y COLUMNAS PARA LAS GRILLAS //---------------//

        var dataSourceConfigCli = new bcs.DataSource({
            type: "array",
            definition: {
                data: [],
                schema: {
                    model: {
                        fields: {
                            NombreCompleto: 			{type: "string"},
                            DocumentoIdentidad: 		{type: "string"},
                            CodigoPortafolio: 			{type: "string"},
                            IdMoneda: 					{type: "string"},
                            CantidadSimulacionCliente:	{type: "number"},
                            PorcentajeSimulacion: 		{type: "number"},
                            DescripcionPortafolio: 		{type: "string"},
                            IdCliente: 					{type: "number"},
                            IdEstado: 					{type: "string"},
                            IdEstadoPortafolio: 		{type: "string"},
                            IdLibroOrdenes: 			{type: "string"},
                            IdPersona: 					{type: "number"},
                            IdPortafolio: 				{type: "number"},
                            Proposito:	 				{type: "string"},
                            RazonSocial: 				{type: "string"},
                            TipoDocumentoIdentidad: 	{type: "string"}
                        }
                    }
                }
            },
        });
		
		var columnsConfigCli = [
		    { field: "NombreCompleto", title: $translate.instant('UI_NOMBRE'), width: "100px", editable: false },
		    { field: "DocumentoIdentidad", title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "100px", editable: false },
		    { field: "CodigoPortafolio", title: $translate.instant('UI_CODIGO_PORTAFOLIO'), width: "100px", editable: false },
		    { field: "CantidadSimulacionCliente", title: $translate.instant('UI_CANTIDAD_SIMULACION'), width: "100px", editable: true, template: " <label> # if(CantidadSimulacionCliente){# #: CantidadSimulacionCliente # #}else{# #: 0 # #} #</label>" },
		    { field: "PorcentajeSimulacion", title: $translate.instant("UI_PORCENTAJE"), width: "100px", editable: true }
		];

        var dataSourceConfigAsig = new bcs.DataSource({
            type: "array",
            definition: {
                data: [],
                schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion()
            }
        });

		var columnsConfigAsig = [
		    { field: "IdInterno", title: $translate.instant("UI_SECUENCIA"), width: "100px" },
		    { field: "FolioTransaccion", title: $translate.instant("UI_FOLIO_TRANSACCION"), width: "100px" },
		    { field: "FechaTransaccion", title: $translate.instant("UI_FECHA_TRANSACCION"), width: "100px" },
		    { field: "Instrumento", title: $translate.instant("UI_INSTRUMENTO"), width: "100px" },
		    { field: "DocumentoIdentidad", title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"), width: "100px" },
		    { field: "NombreCompleto", title: $translate.instant("UI_NOMBRE_COMPLETO"), width: "100px" },
		    { field: "CodigoPortafolio", title: $translate.instant("UI_CODIGO_PORTAFOLIO"), width: "100px" },
		    { field: "CantidadAsignadaSimulacion", title: $translate.instant("UI_CANTIDAD"), width: "100px", editable: true, template: " <label> #:  (util.bcs_formato(CantidadAsignadaSimulacion,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>" },
		    { field: "PrecioPromedio", title: $translate.instant("UI_PRECIO_PROMEDIO"), width: "100px", editable: false, template: " <label> #:  (util.bcs_formato(PrecioPromedio,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>" },
		    { field: "MontoAsignacionSimulacion", title: $translate.instant("UI_MONTO_OPERACION"), width: "100px", editable: false, template: " <label> #:  (util.bcs_formato(MontoAsignacionSimulacion,'GEN','Monto',IdMonedaInstrumento,IdTipoInstrumento,'')).valor #</label>" },
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacionTran")
		];

        var dataSourceConfigTran = new bcs.DataSource({
            type: "array",
            definition: {
                data: [],
                schema: bcsConfiguracionOrdenesV2.getSchemaTransaccion()
            }
        });

		var columnsConfigTran = [
		    { field: "FolioTransaccion"                , title: $translate.instant('UI_FOLIO_TRANSACCION')         , width: "70px"  },
		    { field: "FechaOperacionTransaccion"       , title: $translate.instant('UI_FECHA_TRANSACCION')         , width: "100px" },
		    { field: "CantidadDisponibleTransaccion"   , title: $translate.instant('UI_CANTIDAD_TRANSACCION')      , width: "100px" , template: " <label> #:  (util.bcs_formato(CantidadDisponibleTransaccion,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>" },
		    { field: "CantidadSimulacionTransaccion"   , title: $translate.instant('UI_CANTIDAD_SIMULACION')       , width: "100px" , template: " <label> #:  (util.bcs_formato(CantidadSimulacionTransaccion,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>" },
		    { field: "CantidadTransaccionPorAsignar"   , title: $translate.instant('UI_CANTIDAD_POR_ASIGNAR')      , width: "100px" , template: " <label> #:  (util.bcs_formato(CantidadTransaccionPorAsignar,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>" },
		    { field: "InstrumentoUI"                   , title: $translate.instant('UI_NEMOTECNICO')               , width: "150px" },
		    { field: "NombreTipoOperacion"             , title: $translate.instant('UI_TIPO_OPERACION')            , width: "200px" },
		    { field: "NombreCondicionDeLiquidacion"    , title: $translate.instant('UI_CONDICION_LIQUIDACION_ABR') , width: "100px" },
		    { field: "PrecioTransaccion"               , title: $translate.instant('UI_PRECIO')                    , width: "75px"  , template: " <label> #:  (util.bcs_formato(PrecioTransaccion,'GEN','Precio',IdMonedaTransaccion,IdTipoInstrumento,'')).valor #</label>" },
		    { field: "IdBolsa"                         , title: $translate.instant('UI_BOLSA')                     , width: "75px"  },
		    { field: "IdMonedaTransaccion"             , title: $translate.instant('UI_MONEDA')                    , width: "50px"  },
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento"),
		    { field: "IdEmisor"                        , title: $translate.instant('UI_EMISOR')                    , width: "100px" },
		    { field: "Tasa"                            , title: $translate.instant('UI_TASA')                      , width: "75px"  , template: " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMonedaTransaccion,IdTipoInstrumento,'')).valor #</label>" },
		    { field: "IdTipoReajuste"                  , title: $translate.instant('UI_TIPO_REAJUSTE')             , width: "75px"  },
		    { field: "NemotecnicoSVS"                  , title: $translate.instant("UI_NEMOTECNICO_SVS")           , width: "75px"  },
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsFungibleUI"),
		    { field: "IndicadorOrdenDirecta"           , title: $translate.instant('UI_INDICADOR_ORDEN_DIRECTA')   , width: "75px"  },
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI"),
		    { field: "CantidadTransaccion"             , title: $translate.instant('UI_CANTIDAD')                  , width: "75px"  , template: " <label> #:  (util.bcs_formato(CantidadTransaccion,'GEN','Cantidad',IdMonedaTransaccion,'','')).valor #</label>" },
		    { field: "NombreTipoInstrumento"           , title: $translate.instant('UI_TIPO_INSTRUMENTO')          , width: "215px" },
		    { field: "NombreClasificacionTipoInstrumento", title: $translate.instant('UI_CLASIFICACION')           , width: "150px" },
		    { field: "NombreCorredorContraparte"       , title: $translate.instant('UI_CORREDOR_CONTRAPARTE')      , width: "100px" },
		    { field: "AccionOrden"                     , title: $translate.instant('UI_OPERACION')                 , width: "100px" },
		    { field: "IdComprobante"                   , title: $translate.instant('UI_SECUENCIA_COMPROBANTE')     , width: "80px"  },
            	    bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacionTran"),
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoTransaccion"),
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoUsuarioReserva"),
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdGrupoReserva")
		];


 
        var defCobrosAsignacion = new bcs.DataSource({
            type: "odata",
            definition: {
                serverFiltering: true, // Filtrado de datos en el servidor.
                serverPaging: true, // Paginado de los datos en el servidor.
                serverSorting: true, // Ordenamiento de los datos en el servidor.
                transport: {
                    read: {
                        url: bcsConsultasOrdenes('ConsultarCobrosOrden'),
                        dataType: "json"
                    }
                },
                schema: bcsConfiguracionOrdenesV2.getSchemaCobrosOrden()
            }
        });

        var dataSourceConfigComp = new bcs.DataSource({
            type: "array",
            definition: {
                data: [],
                schema: bcsConfiguracionOrdenesV2.getSchemaComprobante()

            }
        });
		
		var columnsConfigComp = [
		    { field: "IdComprobante"                    , title: $translate.instant('UI_SECUENCIA_COMPROBANTE')          , width: "80px"  },
		    bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaLiquidacion"),
		    { field: "IdTipoComprobante"                , title: $translate.instant('UI_TIPO_COMPROBANTE')               , width: "75px"  },
		    { field: "IdEstadoComprobante"              , title: $translate.instant('UI_ESTADO')                         , width: "70px"  },
		    { field: "DocumentoIdentidad"               , title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')            , width: "90px"  },
		    { field: "NombreCompleto"                   , title: $translate.instant('UI_CLIENTE')                        , width: "200px" },
		    { field: "CodigoPortafolio"                 , title: $translate.instant('UI_PORTAFOLIO')                     , width: "75px"  },
		    { field: "NemotecnicoInstrumentoComprobante", title: $translate.instant("UI_NEMOTECNICO")                    , width: "200px" , template: kendo.template("#if(SeparaPorInstrumento=='S'){# #: NemotecnicoInstrumentoComprobante # #}else{# #: ' ' # #}#") },
		    { field: "CantidadTotal"                    , title: $translate.instant("UI_CANTIDAD")                       , width: "200px" , template: kendo.template("#if(SeparaPorInstrumento=='S'&& NemotecnicoInstrumentoComprobante !=''){# #: CantidadTotal # #}else{# #: ' ' # #}#") },
		    { field: "PPP"                              , title: $translate.instant("UI_PRECIO_PROMEDIO_PONDERADO")      , width: "200px" , template: kendo.template("#if(SeparaPorInstrumento=='S' && NemotecnicoInstrumentoComprobante !=''){# #:  (util.bcs_formato(PPP,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor # #}else{# #: ' ' # #}#") }
		];


//---------------// FIN DEFINICION DE DATASOURCES Y COLUMNAS PARA LAS GRILLAS //---------------//

        $scope.select = function(grid) {
            $timeout(function() {
                if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
                    $("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
            }, 10);
        };

        $scope.formData.filtrosGrupoPortafolio = {
            read: true
        }

        $scope.formData.editarAsignacion = function(asig) {
            /*if (!asig[0].cobrosAsignacion || asig[0].cobrosAsignacion.length === 0) {
            	bcsComunes.msg("error","error","UI_ASIGNACION_SIN_COBROS");
            	return;
            }*/
            var id = bcsComunes.ventanaAbrir("EditarAsignacion", {
                asignacion: asig[0],
                transaccion: buscarTransaccion(asig[0].IdTransaccion)
            }, false, false, $scope.idVentana);
            bcsComunes.ventanaPromesa(id).then(function(data) {

                if (data !== undefined && data.hasOwnProperty("glosa")) {
                    asig[0].Glosa = data.glosa;
                    asig[0].IdBoveda = data.idBoveda;
                    asig[0].IdFormaCustodia = data.idFormaCustodia;
                    asig[0].TasaVentaCorta = data.tasaVentaCorta;
                    asig[0].PlazoVentaCorta = data.plazoVentaCorta;
                    asig[0].TipoCambio = data.tipoCambio;
                    asig[0].IdMonedaLiquidacionOrden = data.idMonedaLiquidacionOrden;
                    asig[0].PagoCobroCuentaInversion = data.pagoCobroCuentaInversion;
                    asig[0].IdPreferenciaPagoPortafolio = data.idPreferenciaPagoPortafolio;
                    asig[0].IdCuentaCustodioPortafolio = data.idCuentaCustodioPortafolio;


                    if (data.cobros && data.cobros.length !== 0) {
                        $.each(asig[0].cobrosAsignacion, function(key1, cobro) {
                            $.each(data.cobros, function(key2, cobroNuevo) {
                                if (cobro.IdCobro === key2)
                                    cobro.ValorCobroAplicado = cobroNuevo;
                            });
                        });
                    }
                }
            });
        };

        $scope.formData.eliminarAsignacion = function() {
            var asig = $scope.formData.asignacionSeleccionada;
            if (!asig) return;
            $scope.formData.apiSimulacionAsignaciones.dataSource.remove(asig);
            recalcularDisponibles();
        };

        var buscarTransaccion = function(idTransaccion) {
            var x = _.find($scope.formData.apiTransacciones.dataSource.data(), function(tran) {
                if (tran.IdTransaccion === idTransaccion) return tran;
            });
            return x;
        }

        $scope.formData.ventanaAgregarComprobantes = function() {
            var id1 = bcsComunes.ventanaAbrir("SelectorComprobantes", {}, false, false, $scope.idVentana);
            bcsComunes.ventanaPromesa(id1).then(function(data) {
                if (!data || !data[0]) return;

                console.log(data);
                $.each(data, function(key, value) {
                    $scope.formData.entidadesRefacturar.push({
                        IdEntidad: value.IdComprobante
                    });
                });

                refrescarTransacciones();

            });
        };

        $scope.formData.ventanaQuitarComprobantes = function() {

            var comprobantes = $scope.formData.comprobantesSeleccionados;
            var entidades = $scope.formData.entidadesRefacturar;

            var asdf = _.filter(entidades, function(item) {
                for (var i = 0; i < comprobantes.length; i++) {
                    if (item.IdEntidad === comprobantes[i].IdComprobante) {
                        return false;
                    }
                }
                return true;
            })
            $scope.formData.entidadesRefacturar = asdf;

            $scope.formData.apiComprobantes.dataSource.remove(comprobantes);
            refrescarTransacciones();
        }

        $scope.formData.ventanaAgregarElementos = function() {

            if ($scope.formData.apiTransacciones.dataSource.data().length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_TRANSACCIONES");
                return;
            }

            var id1 = bcsComunes.ventanaAbrir("SelectorClientes", {}, false, false, $scope.idVentana);
            bcsComunes.ventanaPromesa(id1).then(function(data) {
                if (!data || !data[0]) return;

                agregarClientes(data);
            });

        };

        $scope.formData.ventanaQuitarElementos = function() {
            var clientes = $scope.formData.clientesSeleccionados;
            var asignacionesQuitar = [];
            $.each($scope.formData.apiSimulacionAsignaciones.dataSource.data(), function(keyAsignacion, valueAsignacion) {
                $.each(clientes, function(keyCliente, valueCliente) {
                    if (valueAsignacion.IdCliente === valueCliente.IdCliente) {
                        asignacionesQuitar.push(valueAsignacion);
                    }
                })
            })

            $.each(asignacionesQuitar, function(key, value) {
                $scope.formData.apiSimulacionAsignaciones.dataSource.remove(value);
            })

            $scope.formData.apiSimulacionAsignaciones.refresh();
            $scope.formData.apiClientes.dataSource.remove($scope.formData.clientesSeleccionados);
            $scope.formData.apiClientes.refresh();
            $scope.formData.clientesSeleccionados = undefined;

        };

        var agregarClientes = function(clientes) {

            var sumaPorcentajes = 0;
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                sumaPorcentajes += Number(value.PorcentajeSimulacion);
            });
            
            if (clientes.length > 0) sumaPorcentajes = (100 - (sumaPorcentajes)) / clientes.length;

            var cantidadDisponibleTransaccion = 0;
            $.each($scope.formData.apiTransacciones.dataSource.data(), function(keyTran, valueTran) {
            	cantidadDisponibleTransaccion += valueTran.CantidadDisponibleTransaccion;
            });
                
            var current = [];
            var portafoliosNuevos = [];
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                current.push(value);
            });
            $.each(clientes, function(key, value) {
                value.CantidadSimulacionCliente = Math.round(cantidadDisponibleTransaccion * sumaPorcentajes) / 100;
                value.PorcentajeSimulacion = value.Porcentaje || sumaPorcentajes.toFixed(4);
                value.IdMoneda = value.IdMoneda || 'CLP';
                portafoliosNuevos.push(value);
            });

            var ports = _.uniq(_.union(current, portafoliosNuevos), false, function(item) {
                return item.IdPortafolio;
            });


            $scope.formData.apiClientes.dataSource.data(ports);
            $scope.formData.apiClientes.refresh();
            setearColumnas();
        }

        var recalcularDisponibles = function(evento) {
            var asignaciones = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
            var transaccionesC = {};
            var portafoliosC = {};

            var transaccionesM = {};
            var portafoliosM = {};

            $.each(asignaciones, function(key, value) {
                transaccionesC[value.IdTransaccion] = 0;
                transaccionesM[value.IdTransaccion] = 0;
                portafoliosC[value.IdPortafolio] = 0;
                portafoliosM[value.IdPortafolio] = 0;
            });

            $.each(asignaciones, function(key, value) {
                if (evento && value === evento.model) {
                    if ('CantidadAsignadaSimulacion' in evento.values) {
                        transaccionesC[value.IdTransaccion] = transaccionesC[value.IdTransaccion] + evento.values.CantidadAsignadaSimulacion;
                        portafoliosC[value.IdPortafolio] = portafoliosC[value.IdPortafolio] + evento.values.CantidadAsignadaSimulacion;
                    }
                } else {
                    transaccionesC[value.IdTransaccion] = transaccionesC[value.IdTransaccion] + Number(value.CantidadAsignadaSimulacion);


                    portafoliosC[value.IdPortafolio] = portafoliosC[value.IdPortafolio] + Number(value.CantidadAsignadaSimulacion);


                }
            });

            var transaccionesGrid = $scope.formData.apiTransacciones.dataSource.data();
            var transaccionPrecios = {};
            $.each(transaccionesGrid, function(key, value) {
                transaccionPrecios[value.IdTransaccion] = value.PrecioTransaccion;
                value.CantidadSimulacionTransaccion = transaccionesC[value.IdTransaccion];
                value.CantidadTransaccionPorAsignar = value.CantidadDisponibleTransaccion - (value.CantidadSimulacionTransaccion || 0);
            });

            var portafoliosGrid = $scope.formData.apiClientes.dataSource.data();
            var total = $scope.formData.saldoCantidadPorAsignar;
            if (total == undefined) {
                total = 0;
                $.each(portafoliosGrid, function(key, value) {
                    total = total + portafoliosC[value.IdPortafolio];
                });
            }
            $.each(portafoliosGrid, function(key, value) {
            	if(portafoliosC[value.IdPortafolio] != "" && portafoliosC[value.IdPortafolio] !== undefined)
            	{
	                value.CantidadSimulacionCliente = portafoliosC[value.IdPortafolio];
	                value.PorcentajeSimulacion = (portafoliosC[value.IdPortafolio] / total) * 100;
	                value.PorcentajeSimulacion = Number(value.PorcentajeSimulacion.toFixed(4));
            	}
            	else
            	{
            		value.CantidadSimulacionCliente = 0;
  	                value.PorcentajeSimulacion = 0;
            	}

            });

            var portafolios = _.groupBy(asignaciones, function(dd) {
                return dd.IdPortafolio
            });
            var totalPortafolio = {};
            var parcialPortafolio = {};

            $.each(asignaciones, function(key, value) {
                if (!totalPortafolio[value.IdPortafolio + " " + value.Instrumento]) totalPortafolio[value.IdPortafolio + " " + value.Instrumento] = 0;
                if (!parcialPortafolio[value.IdPortafolio + " " + value.Instrumento]) parcialPortafolio[value.IdPortafolio + " " + value.Instrumento] = 0;
                if (evento && value === evento.model) {
                    totalPortafolio[value.IdPortafolio + " " + value.Instrumento] += evento.values.CantidadAsignadaSimulacion;
                    parcialPortafolio[value.IdPortafolio + " " + value.Instrumento] += (evento.values.CantidadAsignadaSimulacion * transaccionPrecios[value.IdTransaccion]);
                } else {
                    totalPortafolio[value.IdPortafolio + " " + value.Instrumento] += value.CantidadAsignadaSimulacion;
                    parcialPortafolio[value.IdPortafolio + " " + value.Instrumento] += (value.CantidadAsignadaSimulacion * transaccionPrecios[value.IdTransaccion]);

                }
            });
            
            $.each(asignaciones, function(key, value) {
				value.PrecioPromedio = (totalPortafolio[value.IdPortafolio + " " + value.Instrumento] > 0) ? parcialPortafolio[value.IdPortafolio + " " + value.Instrumento] / totalPortafolio[value.IdPortafolio + " " + value.Instrumento] : 0;
				
				if (evento && value === evento.model) {
					value.MontoAsignacionSimulacion = value.PrecioPromedio * evento.values.CantidadAsignadaSimulacion; 
				} else {
					value.MontoAsignacionSimulacion = value.PrecioPromedio * value.CantidadAsignadaSimulacion;
				}
            });
            
            $scope.formData.apiTransacciones.refresh();
            $scope.formData.apiTransacciones.dataSource.sort({ field: "CantidadTransaccionPorAsignar", dir: "desc" });
            $scope.formData.apiClientes.refresh();
            $scope.formData.apiSimulacionAsignaciones.refresh();

        };

        $scope.formData.previsualizar = function(tipoAsignacion) {

            var sumaPorcentaje = 0;
            var i = 0;
            var elem = null;

            var transacciones = [];
            var portafolios = [];

            var transaccionesDS;
            var portafoliosDS;
            if (tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES') {
                transaccionesDS = $scope.formData.transaccionesSeleccionadas || [];
                portafoliosDS = $scope.formData.clientesSeleccionados || [];

            } else {
                transaccionesDS = $scope.formData.apiTransacciones.dataSource.data();
                portafoliosDS = $scope.formData.apiClientes.dataSource.data();
            }

            $.each(transaccionesDS, function(key, value) {
                tr = {
                    IdMovimiento: value.IdMovimiento ? value.IdMovimiento : value.IdTransaccion,
                    IdTransaccion: value.IdTransaccion,
                    Cantidad: value.CantidadRefacturar ? value.CantidadRefacturar : value.CantidadDisponibleTransaccion,
                    Monto: value.Monto ? value.MontoRefacturar : 0,
                    ValorRescate: value.ValorRescateRefacturar ? value.ValorRescateRefacturar : value.CantidadDisponibleTransaccion
                };
                transacciones.push(tr);
            });

            $.each(portafoliosDS, function(key, value) {
                portafolios.push({
                    IdPortafolio: value.IdPortafolio,
                    Cantidad: 0,
                    Porcentaje: value.PorcentajeSimulacion
                });
                sumaPorcentaje += Number(value.PorcentajeSimulacion);
            });

            if (transacciones.length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_TRANSACCIONES");
                return;
            }

            if ((tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES' || tipoAsignacion === 'ASIGNACION_PRECIO_PROMEDIO' || tipoAsignacion === 'ASIGNACION_POSTRERA') && portafolios.length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_CLIENTES");
                return;
            }

            if (tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES' || tipoAsignacion === 'ASIGNACION_PRECIO_PROMEDIO' || tipoAsignacion === 'ASIGNACION_POSTRERA') {
            	if (sumaPorcentaje.toFixed(2) > 100) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_PORCENTAJE_SOBREPASA_100");
                    return;
                }
            }

            var datos = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'ORD',
                p_UsaReglasNegocio: 'S',
                p_IdTipoAsignacion: tipoAsignacion,
                p_IdTipo: $scope.parametros.FormaAsignacion, //$scope.formData.formaAsignacion,
                p_Origen: $scope.parametros.Origen, //$scope.formData.origenAsignacion, 
                p_Transacciones: transacciones,
                p_Carteras: [],
                p_Ordenes: [],
                p_Portafolios: portafolios,

            };

            bcsComunes.ventanaOverlay($scope.idVentana, true);
            var p = bcsServicioOrden.CrearSimulacionAsignacion(datos).$promise;
            p.then(function(result) {
                procesarResultadoSimulacion(result, tipoAsignacion);

            }, function(result) {
                bcsComunes.msg("error", "Error", "Existen problemas al ingresar la operacion");
                bcsComunes.ventanaOverlay($scope.idVentana, false);
            });

        };

        var procesarResultadoSimulacion = function(result, tipoAsignacion) {
            bcsComunes.ventanaOverlay($scope.idVentana, false);

            $scope.formData.transaccionesSeleccionadas = undefined;
            $scope.formData.clientesSeleccionados = undefined;

            if (result.P_OUT_ERRORESCONTADOR > 0) {
                bcsComunes.ventanaMsg({
                    mensaje: $translate.instant("UI_ERROR_AL_CREAR_ASIGNACIONES"),
                    conflictos: util.parseServerResponse(result)
                }, $scope.idVentana);

                $scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
                return;
            }

            var asignaciones = [];
            var asigMap = {};
            $.each(result.P_OUT_SIMASIGNACION, function(key, value) {

                var asig = {
                    CantidadAsignadaSimulacion: value.IdClasificacionTipoInstrumento !== 'IF' ? value.CantidadAsignacion : value.ValorRescateAsignacion,
                    CantidadTransaccion: value.CantidadTransaccion,
                    CodigoIFRS: value.CodigoIFRS,
                    CodigoPortafolio: value.CodigoPortafolio,
                    DocumentoIdentidad: value.DocumentoIdentidad,
                    EsFungible: value.EsFungible,
                    EsExtranjera: value.EsExtranjera,
                    EstiloOpcion: value.EstiloOpcion,
                    FechaTransaccion: value.FechaTransaccion,
                    FolioCarteraAsignada: value.FolioCartera,
                    FolioTransaccion: value.FolioTransaccion,
                    Glosa: value.Glosa,
                    IdBolsa: value.IdBolsa,
                    IdBoveda: value.IdBoveda,
                    IdClasificacionTipoInstrumento: value.IdClasificacionTipoInstrumento,
                    IdCliente: value.IdCliente,
                    IdCorredoraCorresponsal: value.IdCorredoraCorresponsal,
                    IdCuentaCustodioPortafolio: value.IdCuentaCustodioPortafolio,
                    IdFormaCustodia: value.IdFormaCustodia,
                    IdInterno: value.IdInterno,
                    IdInstrumento: value.IdInstrumento,
                    IdMonedaInstrumento: value.IdMonedaInstrumento,
                    IdMonedaLiquidacionOrden: value.IdMonedaLiquidacionOrden,
                    IdMovimiento: value.IdMovimiento,
                    IdOrden: value.IdOrden,
                    IdPreferenciaPagoPortafolio: value.IdPreferenciaPagoPortafolio,
                    IdPortafolio: value.IdPortafolio,
                    IdTipoInstrumento: value.IdTipoInstrumento,
                    IdTransaccion: value.IdTransaccion,
                    IdTipoCanalIngreso: value.IdTipoCanalIngreso,
                    IdTipoOperacion: value.IdTipoOperacion,
                    IdTipoProducto: value.IdTipoProducto,
                    Instrumento: value.Instrumento,
                    MontoAsignacion: value.MontoAsignacion, //Regla de 3 > ((Monto Palo * Cantidad Asignada) / Cantidad Palo)
                    MontoTransaccion: value.MontoTransaccion,
                    ModalidadEjercicio: value.ModalidadEjercicio,
                    ModalidadLiquidacion: value.ModalidadLiquidacion,
                    NombreCompleto: value.NombreCompleto,
                    PagoCobroCuentaInversion: value.PagoCobroCuentaInversion,
                    PlazoVentaCorta: Number(value.PlazoVentaCorta),
                    PrecioPromedio: 0,
                    PrecioSimulacion: value.PrecioSimulacion,
                    PrecioStrike: value.PrecioStrike,
                    TasaVentaCorta: Number(value.TasaVentaCorta),
                    Tamano: value.Tamano,
                    TipoCambio: Number(value.TipoCambio),
                    TipoOpcion: value.TipoOpcion,
                    ValorRescateAsignadoSimulacion: value.ValorRescateAsignacion,
                    InstrumentoNegociacionTran: value.InstrumentoNegociacionTran,
                    cobrosAsignacion: [],

                };
                asigMap[value.IdInterno] = asig;
                asignaciones.push(asig);

            });
            
            $.each(result.P_OUT_COBROSASIGNACION, function(key, value) {
				for (var x = 0; x < cobrosAsignacionAnterior.length; x++) {
					if (cobrosAsignacionAnterior[x].IdCobro === value.IdCobro) {
						if (cobrosAsignacionAnterior[x].ValorCobroAplicado != value.ValorCobro) {
							asigMap[value.IdInterno].cobrosAsignacion.push({
							IdInterno: value.IdInterno,
							IdCobro: value.IdCobro,
							ValorCobroAplicado: value.ValorCobro != null? value.ValorCobro: cobrosAsignacionAnterior[x].ValorCobroAplicado
							});
						}
					}
				}   
            });

            if (tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES') {
                var currentAsignaciones = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
                for (var i = 0; i < asignaciones.length; i++) {
                    var asig = asignaciones[i];
                    var f = _.findIndex(currentAsignaciones, function(curr) {
                        return curr.IdTransaccion === asig.IdTransaccion && curr.IdPortafolio === asig.IdPortafolio && curr.FolioCartera === asig.FolioCartera && curr.IdOrden === curr.IdOrden;
                    });

                    if (f === -1) {
                        currentAsignaciones.push(asignaciones[i]);
                    }
                }
                $scope.formData.apiSimulacionAsignaciones.dataSource.data(currentAsignaciones);

            } else {
                $scope.formData.apiSimulacionAsignaciones.dataSource.data(asignaciones);
            }
            recalcularDisponibles();
        }

        $scope.formData.ejecutarAsignacion = function() {
            var CantidadAsignacion = 0;
            var CantidadTotal = 0;
            if (parametro === 'S') {
                var a = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
                $.each(a, function(key, value) {
                    CantidadAsignacion = CantidadAsignacion + value.CantidadAsignadaSimulacion;
                    CantidadTotal = value.CantidadTransaccion;
                });
                if (CantidadAsignacion < CantidadTotal) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_CANTIDAD_SIMULACION_INFERIOR_AL_SALDO");
                    return;
                }
                if (CantidadAsignacion > CantidadTotal) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_CANTIDAD_SIMULACION_SUPERA_SALDO");
                    return;
                }
            }
            var a = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
            if (a.length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_NO_HAY_ASIGNACIONES");
                return;
            }

            var asignacionesClientes = [];
            var asignacionesOrdenes = [];
            var cobrosModificados = [];
            
            $.each(a, function(key, value) {
                if (value.CantidadAsignadaSimulacion <= 0)
                    return true;

                asignacionesClientes.push({
                    Cantidad: value.CantidadAsignadaSimulacion,
                    IdBoveda: value.IdBoveda,

                    IdCuentaCustodioPortafolio: value.IdCuentaCustodioPortafolio,
                    IdFormaCustodia: value.IdFormaCustodia,
                    IdInterno: value.IdInterno,
                    IdMonedaLiquidacionOrden: value.IdMonedaLiquidacionOrden,
                    IdPortafolio: value.IdPortafolio,
                    IdPreferenciaPagoPortafolio: value.IdPreferenciaPagoPortafolio,

                    IdTransaccion: value.IdTransaccion,
                    PagoCobroCuentaInversion: value.PagoCobroCuentaInversion,
                    PlazoVentaCorta: value.PlazoVentaCorta,
                    TasaVentaCorta: value.TasaVentaCorta,
                    TipoCambio: value.TipoCambio
                });
               
    			//cobros modificados
    			for (var i = 0;i<value.cobrosAsignacion.length;i++) {
    				if (value.cobrosAsignacion[i].ValorCobro != value.cobrosAsignacion[i].ValorCobroAplicado) {
    					cobrosModificados.push( {
    						IdInterno 			: value.IdInterno,
    						IdCobro				: value.cobrosAsignacion[i].IdCobro,
    						ValorCobroAplicado	: value.cobrosAsignacion[i].ValorCobroAplicado
    					});
    				}
    			}
            });

            var datos = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'FAC',
                p_Origen: $scope.parametros.Origen, //$scope.formData.origenAsignacion,
                p_Tipo: $scope.parametros.FormaAsignacion, //$scope.formData.formaAsignacion,
                p_Asignaciones: asignacionesClientes,
                p_Entidades: $scope.formData.entidadesRefacturar,
                p_CobrosModificados: cobrosModificados,
                p_CobrosFijos: $scope.formData.CobrosFijos
            };
            
            
            var refacturarBatchDesde = BCS_CONFIGURACION.FACTURACION["REFACTURAR_BATCH_DESDE"];
            if (refacturarBatchDesde == null || refacturarBatchDesde == "")
            {
            	refacturarBatchDesde = 0;
            }
           
            if (asignacionesClientes.length < refacturarBatchDesde)
            {	// EJECUTA PROCESO DE REFACTURACION EN FORMA NORMAL       	
            	
                bcsComunes.ventanaOverlay($scope.idVentana, true);
                const promiseServicioOrden = bcsServicioOrden.RefacturarMovimiento(datos).$promise;

            	promiseServicioOrden.then(function(result) {
	                bcsComunes.ventanaOverlay($scope.idVentana, false);
	                
				    if (result.P_OUT_ERRORESCONTADOR > 0) {
						const errores = result.P_OUT_MENSAJES.filter(mensaje => mensaje.tipo === "ERROR");
						
				        bcsComunes.ventanaMsg({
				            conflictos: util.parseServerResponse({...result, P_OUT_MENSAJES: errores})
				        }, $scope.idVentana);
				
				        return;
				    }
				    
					const promiseVentana = bcsComunes.ventanaPromesa(
						bcsComunes.ventanaMsg({
							conflictos: util.parseServerResponse(result)
						}, $scope.idVentana)
					);
	
	                promiseVentana.then(function(result) {
	                    $scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
	                    
	                    if ($scope.formData.transaccionesSaldos.length > 0) {
	                        $scope.formData.cargarSaldos($scope.formData.tipoCargaSaldoTransacciones);
	                    }
	                    
	                    if ($scope.formData.entidadesRefacturar.length > 0) {
	                        refrescarTransacciones();
	                    }
	                })

                }, function(result) {
                    bcsComunes.ventanaOverlay($scope.idVentana, false);
                    bcsComunes.ventanaMsg({
                        mensaje: 'UI_ERROR_INTERNO_SISTEMA',
                        conflictos: util.parseServerResponse(result)
                    }, $scope.idVentana);
                });
            	
            }
            else
    		{	// EJECUTA PROCESO DE REFACTURACION EN FORMA BATCH            	
    			var entradaProcesoBatch		= {};					
    			entradaProcesoBatch.p_IdEmpresa			= $scope.SesionIdEmpresa;
    			entradaProcesoBatch.p_IdUsuario 		= $scope.SesionIdUsuario;
    			entradaProcesoBatch.p_IdModulo    		= 'ORD';

    			entradaProcesoBatch.p_TipoPrograma =
    			[
    			   {IdInterno : 0, 	IdTipoPrograma: 'REFACTURACION_MASIVA', SegundosDesfase: 0}
    			];

    			entradaProcesoBatch.p_Parametro =
    			[
    			   {IdInterno : 0,  NombreParametro: 'p_IdEmpresa',  		 Valor: $scope.SesionIdEmpresa},
    			   {IdInterno : 0,  NombreParametro: 'p_IdUsuario',  		 Valor: $scope.SesionIdUsuario},
    			   {IdInterno : 0,  NombreParametro: 'p_IdModulo',  		 Valor: 'FAC'},
    			   {IdInterno : 0,  NombreParametro: 'p_UsaReglasNegocio',   Valor: 'S'},
    			   {IdInterno : 0,  NombreParametro: 'p_Origen',  			 Valor: $scope.parametros.Origen},
    			   {IdInterno : 0,  NombreParametro: 'p_Tipo',  			 Valor: $scope.parametros.FormaAsignacion}, 
    			   {IdInterno : 0,  NombreParametro: 'p_Entidades',  		 Valor: '',  ValorBlob: JSON.stringify($scope.formData.entidadesRefacturar)},
    			   {IdInterno : 0,  NombreParametro: 'p_Asignaciones',  	 Valor: '',  ValorBlob:	JSON.stringify(asignacionesClientes)},
    			   {IdInterno : 0,  NombreParametro: 'p_CobrosModificados',  Valor: '',  ValorBlob: JSON.stringify(cobrosModificados)},
    			   {IdInterno : 0,  NombreParametro: 'p_CobrosFijos',  		 Valor: '',  ValorBlob: JSON.stringify($scope.formData.CobrosFijos)}
    			];  			

    			var urlInscribirProcesoBatch = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043";
    			var r = $resource("", {},
    		    {
    				'InscribirProcesoBatch': { method: "POST", url: urlInscribirProcesoBatch}
    			});
    			
    			bcsComunes.ventanaOverlay($scope.idVentana, true);
    			r.InscribirProcesoBatch(entradaProcesoBatch).$promise.then(
    			function(result) {
    				bcsComunes.ventanaOverlay($scope.idVentana, false);
    				
					bcsComunes.ventanaMsg({
						mensaje    : "Programa solicitado correctamente (#" + result.P_OUT_PROGRAMA[0].IdPrograma +"). Vea la salida en Consultar Programas"
					}, $scope.idVentana);
    			},
    			function(result){
                    bcsComunes.ventanaOverlay($scope.idVentana, false);
                    bcsComunes.ventanaMsg({
                        mensaje: 'UI_ERROR_INTERNO_SISTEMA',
                        conflictos: util.parseServerResponse(result)
                    }, $scope.idVentana);
    			});
    		}
        };

        var promiseGridTransacciones = $q.defer();
        var promiseGridAsignacionesResum = $q.defer();
        var promiseGridClientes = $q.defer();
        var promiseGridSimulacionAsignaciones = $q.defer();

        var inicializarGrillas = function() {
            //CONFIGURACION INICIAL GRILLAS
            $scope.formData.confTransacciones = {
                allowCopy: true,
                autoBind: true,
                filterable: true,
                groupable: false,
                height: 220,
                navigatable: true,
                reorderable: true,
                resizable: true,
                rowColorBackground: function(row) {
                    if (row.get("CantidadSimulacionTransaccion") > row.get("CantidadDisponibleTransaccion")) {
                        return "red";
                    }
                    if (row.get("CantidadSimulacionTransaccion") == row.get("CantidadDisponibleTransaccion")) {
                        return "green";
                    }
                    if (row.get("CantidadSimulacionTransaccion") < row.get("CantidadDisponibleTransaccion")) {
                        return "yellow";
                    }
                },
                selectable: "multiple,row",
                sortable: true,
                userSettings: true,

                dataSource: dataSourceConfigTran,
                columns: columnsConfigTran
            }

            $scope.formData.confTransacciones.gridCreated = function(grid) {
                promiseGridTransacciones.resolve();
            }

            $scope.formData.confClientes = {
                allowCopy: true,
                autoBind: false,
                height: 220,
                reorderable: true,
                resizable: true,
                selectable: "multiple,row",
                sortable: true,
                userSettings: true,
                navigatable: true,
                dataSource: dataSourceConfigCli,
                columns: columnsConfigCli
            };
            
            $scope.formData.confClientes.gridCreated = function(grid) {
                promiseGridClientes.resolve();
            }
            /*
            		var actualizarSaldos = function() {
            			$scope.formData.saldoCantidadPorAsignar = 0;
            			$scope.formData.saldoPorcentajePorAsignar = 100;
            			$.each($scope.formData.apiTransacciones.dataSource.data(),function(key,value){
            				$scope.formData.saldoCantidadPorAsignar = Number($scope.formData.saldoCantidadPorAsignar + value.CantidadDisponibleTransaccion);
            			});
            			$.each($scope.formData.apiClientes.dataSource.data(),function(key,value){
            				$scope.formData.saldoCantidadPorAsignar = Number($scope.formData.saldoCantidadPorAsignar - value.CantidadSimulacionCliente);
            			});
            			$.each($scope.formData.apiClientes.dataSource.data(),function(key,value){
            				$scope.formData.saldoPorcentajePorAsignar = Number($scope.formData.saldoPorcentajePorAsignar - value.PorcentajeSimulacion);
            			});
            			
            		};*/
            $scope.formData.updateCantidades = function(actualizaPorcentajes) {
                $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                    value.CantidadSimulacionCliente = Number(new Big($scope.formData.saldoCantidadPorAsignar).times(new Big(value.PorcentajeSimulacion)).div(new Big(100)).round(10, 1).toString());
                    value.CantidadSimulacionCliente = Number(value.CantidadSimulacionCliente.toFixed(0));
                });
            	
            	$scope.formData.apiClientes.refresh();
                
                //$scope.form.refreshTotal();
                if (actualizaPorcentajes)
                    $scope.formData.updatePorcentajes(false);
            }
            
            $scope.formData.updatePorcentajes = function(actualizaCantidades) {
                $scope.formData.porcentajeTotal = new Big(0);
                $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                    if ($scope.formData.saldoCantidadPorAsignar == undefined) {
                        $scope.formData.saldoCantidadPorAsignar = 100;
                    }
                    var num = (new Big(100)).times(new Big(value.CantidadSimulacionCliente)).div(new Big($scope.formData.saldoCantidadPorAsignar));
                    $scope.formData.porcentajeTotal = $scope.formData.porcentajeTotal.plus(num);
                    value.PorcentajeSimulacion = Number(num.toFixed(4));
                });
                $scope.formData.porcentajeTotal = Number($scope.formData.porcentajeTotal.round(10, 1).toString());
                $scope.formData.apiClientes.refresh();
                //$scope.formData.refreshTotal();
                if (actualizaCantidades)
                    $scope.formData.updateCantidades(false);
            }
            
            $scope.formData.confClientes.save = function(e) {

                //actualizarSaldos();
                if (e.values.PorcentajeSimulacion > 100) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_PORCENTAJE_SOBREPASA_100");
                    e.preventDefault();
                }
                if (e.values.PorcentajeSimulacion < 0) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_PORCENTAJE_NEGATIVO");
                    e.preventDefault();
                }

                
                $scope.formData.saldoCantidadPorAsignar = 0;
                $.each($scope.formData.apiTransacciones.dataSource.data(), function(key, value) {
                    $scope.formData.saldoCantidadPorAsignar = Number($scope.formData.saldoCantidadPorAsignar + value.CantidadDisponibleTransaccion);
                });
                
                
                if (e.values.PorcentajeSimulacion !== undefined) {


                    e.model.PorcentajeSimulacion = e.values.PorcentajeSimulacion;
                    $scope.formData.updateCantidades(false);

                } else if (e.values.CantidadSimulacionCliente !== undefined) {

                    var sumaCantidad = 0;
                    $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                        if (value === e.model) {
                            sumaCantidad += e.values.CantidadSimulacionCliente;
                        } else {
                            sumaCantidad += value.CantidadSimulacionCliente;
                        }

                    });

                    if (sumaCantidad > $scope.formData.saldoCantidadPorAsignar) {
                        bcsComunes.msg("error", "UI_ERROR", "UI_CANTIDAD_SOBREPASA_DISPONIBLE");
                        return;
                    }
                    e.model.CantidadSimulacionCliente = e.values.CantidadSimulacionCliente;


                    $scope.formData.updatePorcentajes();
                }
            };

            $scope.formData.confAsignacionesResum = {
                allowCopy: true,
                autoBind: false,
                filterable: false,
                groupable: false,
                height: 220,
                navigatable: true,
                reorderable: true,
                resizable: true,
                selectable: "multiple,row",
                sortable: true,
                userSettings: true,

                dataSource: new bcs.DataSource({
                    type: "array",
                    definition: {
                        data: [],
                        schema: bcsConfiguracionOrdenesV2.getSchemaAsignacionResum()
                    }
                }),
                columns: [
                    bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacion"), bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaAsignacion"), bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTransaccion"), bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignada"), bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoAsignado"), bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdEstadoAsignacion")
                ]
            };
            $scope.formData.confAsignacionesResum.gridCreated = function(grid) {
                promiseGridAsignacionesResum.resolve();
            }


            $scope.formData.confComprobantes = {
                allowCopy: true,
                autoBind: false,
                filterable: true,
                groupable: false,
                height: 220,
                navigatable: true,
                reorderable: true,
                resizable: true,
                rowColorBackground: function(row) { return jpmColor },
                selectable: "multiple,row",
                sortable: true,
                userSettings: true,

                dataSource: dataSourceConfigComp,
                columns: columnsConfigComp
            };

            $scope.formData.confSimulacionAsignaciones = {
                allowCopy: true,
                autoBind: true,
                filterable: false,
                groupable: false,
                height: 250,
                navigatable: true,
                reorderable: true,
                resizable: true,
                scrollable: false,
                selectable: "multiple,row",
                sortable: true,
                userSettings: true,
                dataSource: dataSourceConfigAsig,
                columns: columnsConfigAsig 
            };
            $scope.formData.confSimulacionAsignaciones.gridCreated = function(grid) {
                $scope.formData.apiSimulacionAsignaciones = grid;
                promiseGridSimulacionAsignaciones.resolve();
            }
            $scope.formData.confSimulacionAsignaciones.save = function(e) {
                recalcularDisponibles(e);
            };
        }



        $scope.formData.ExportarExcel = function(Grilla) {
            let nombreArchivo = "Interfaz Refacturacion "; 
            nombreArchivo += Grilla;
            //const fechaActual = new Date().toISOString().split('T')[0];
            //nombreArchivo += "_" + fechaActual;
 
            switch (Grilla) {
                case 'Asignaciones':
                    dataSourceConfigAsig.exportToExcel(columnsConfigAsig, '', nombreArchivo);
                    break;
                case 'Transacciones':
                    dataSourceConfigTran.exportToExcel(columnsConfigTran, '', nombreArchivo);
                    break;
                case 'Comprobantes':
                    dataSourceConfigComp.exportToExcel(columnsConfigComp, '', nombreArchivo);
                    break;
                case 'Clientes':
                    dataSourceConfigCli.exportToExcel(columnsConfigCli, '', nombreArchivo);
                    break;
            }
            
        };

        var setearColumnas = function() {
            $scope.formData.apiTransacciones.showColumn("CantidadTransaccion");
            $scope.formData.apiTransacciones.showColumn("CantidadSimulacionTransaccion");
            $scope.formData.apiTransacciones.showColumn("CantidadTransaccionPorAsignar");
            $scope.formData.apiTransacciones.showColumn("CantidadDisponibleTransaccion");

            $scope.formData.apiClientes.showColumn("CantidadSimulacionCliente");
            $scope.formData.apiClientes.showColumn("PorcentajeSimulacion");

            $scope.formData.apiSimulacionAsignaciones.showColumn("PrecioPromedio");
            $scope.formData.apiSimulacionAsignaciones.showColumn("CantidadAsignadaSimulacion");
            $scope.formData.apiSimulacionAsignaciones.showColumn("MontoAsignacionSimulacion");
        };

        //setea cabeceras

        //RF CARTERA, POSTRERA
        //IF CARTERA, POSTRERA
        //DV CRONOLOGICA, PROPORCIONAL,MANUAL,POSTRERA
        //SIM POSTRERA MANUAL
        //OTRAS RV MANUAL, POSTRERA,PRECIO PROMEDIO, CRONOLOGICA, PROPORCIONAL

        //habilita pestañas de asignaciones y habilita algoritmos de asignacion

        $scope.formData.guardarCambiosGrupo = function() {

            if (!$scope.formData.grupoPortafoliosSeleccionado) {
                bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_GRUPO");
                return;
            }
            if ($scope.formData.apiClientes.dataSource.data().length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_NO_HAY_CLIENTES");
                return;
            }

            var clientesGrupo = [];
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                clientesGrupo.push({
                    IdPortafolio: value.IdPortafolio,
                    Porcentaje: value.PorcentajeSimulacion
                });
            });

            var datosUpdate = {
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdModulo: 'ORD',
                p_IdGrupo: $scope.formData.grupoPortafoliosSeleccionado.IdGrupo,
                p_tablaClientesPortafolio: clientesGrupo
            };

            bcsComunes.ventanaOverlay($scope.idVentana, true);
            var post = bcsServicioOrden.ActualizarGrupoPortafolio(datosUpdate);
            post.$promise.then(function(result) {
                bcsComunes.msg("info", "UI_INFORMACION", "UI_GRUPO_ACTUALIZADO");
                bcsComunes.ventanaOverlay($scope.idVentana, false);
            }, function(result) {
                bcsComunes.ventanaMsg({
                    conflictos: util.parseServerResponse(result)
                }, false, false, idVentana);
                bcsComunes.ventanaOverlay($scope.idVentana, false);
            })
        };

        $scope.formData.crearGrupo = function() {

            if (!$scope.formData.grupoPortafoliosSeleccionado) {
                bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_GRUPO");
                return;
            }
            if ($scope.formData.apiClientes.dataSource.data().length === 0) {
                bcsComunes.msg("error", "UI_ERROR", "UI_NO_HAY_CLIENTES");
                return;
            }

            var clientesGrupo = [];
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                clientesGrupo.push({
                    IdCliente: value.IdCliente,
                    DocumentoIdentidad: value.DocumentoIdentidad,
                    NombreCompleto: value.NombreCompleto,
                    IdPortafolio: value.IdPortafolio,
                    EstadoPortafolio: 'ACTIVO',
                    CodigoPortafolio: value.CodigoPortafolio,
                    IdMoneda: value.IdMoneda,
                    Porcentaje: value.PorcentajeSimulacion
                });
            });

            var id1 = bcsComunes.ventanaAbrir("ModificarGrupoPortafolio", {
                Modo: "NUEVO",
                clientes: clientesGrupo
            }, false, false, $scope.idVentana);
            var promesa = bcsComunes.ventanaPromesa(id1);
            promesa.then(function(data) {
                $scope.formData.filtrosGrupoPortafolio.read = false;
                $scope.formData.filtrosGrupoPortafolio.read = true;
            });

        };


        var goo = function() {

            //var parametros = bcsComunes.ventanaParams($scope.idVentana);
            if (!$scope.parametros.movimientos || $scope.parametros.movimientos.length === 0)
                return;

            var entidades = [];
            $.each($scope.parametros.movimientos, function(key, value) {
                entidades.push({
                    IdEntidad: value.IdMovimiento
                });
            });

            $scope.formData.entidadesRefacturar = entidades;
            refrescarTransacciones();
        }

        $scope.formData.refrescarGrillaTransacciones = function() {

            //var currentData = _.extend([],$scope.formData.transaccionesComprobantes);
            var currentData = [];
            $scope.formData.apiAsignacionesResum.dataSource.data($scope.formData.asignacionesComprobantes);
            $.each($scope.formData.transaccionesComprobantes, function(key, value) {
                currentData.push(_.extend({}, value));
            });

            $.each($scope.formData.transaccionesSaldos, function(key, tranNueva) {
                var found = false;
                $.each(currentData, function(key2, tran) {
                    if (tranNueva.IdTransaccion === tran.IdTransaccion) {
                        tran.CantidadDisponibleTransaccion += tranNueva.CantidadDisponibleTransaccion;
                        found = true;
                    }
                })

                if (!found) {
                    tranNueva.CantidadSimulacionTransaccion = 0;
                    tranNueva.CantidadTransaccionPorAsignar = 0;
                    currentData.push(tranNueva);
                }

                found = false;
            });

            $scope.formData.apiTransacciones.dataSource.data(currentData);
            $scope.formData.apiTransacciones.refresh();


            recalcularDisponibles();
            
            var asignacionesQuitar = [];
            $.each($scope.formData.apiSimulacionAsignaciones.dataSource.data(), function(keyAsignacion, valueAsignacion) {
                var found = false;
                $.each(currentData, function(keyTran, valueTran) {
                    if (valueAsignacion.IdTransaccion === valueTran.IdTransaccion) {
                        found = true;
                    }

                })

                if (!found) {
                    asignacionesQuitar.push(valueAsignacion)
                }

            })
            $.each(asignacionesQuitar, function(key, value) {
                $scope.formData.apiSimulacionAsignaciones.dataSource.remove(value);
            })
            $scope.formData.apiSimulacionAsignaciones.refresh();
        }
        
        $scope.formData.cargarSaldos = function(tipoCargaSaldoTransacciones) {

   		 	var fechaTransaccionInicio = "";
	        var fechaTransaccionTermino = "";
	        $scope.formData.tipoCargaSaldoTransacciones = tipoCargaSaldoTransacciones;
	        		
        	if (tipoCargaSaldoTransacciones === "TRALIB")  // Transacciones libres con saldo disponible
        	{
        		fechaTransaccionInicio	= util.dateInicioDia($scope.SesionFechaSistema);
        		fechaTransaccionTermino	= util.dateTerminoDia($scope.SesionFechaSistema);
        	}

            var datos = {
	                p_IdEmpresa			: $scope.SesionIdEmpresa,
	                p_IdUsuario			: $scope.SesionIdUsuario,
	                p_IdModulo			: 'ORD',
	                p_TipoConsulta		: tipoCargaSaldoTransacciones,
	                p_FechaTransaccionInicio	:fechaTransaccionInicio,
	                p_FechaTransaccionTermino	:fechaTransaccionTermino
	            };
            
            
            bcsComunes.ventanaOverlay($scope.idVentana, true);
            bcsServicioOrden.ConsultarSaldoTransaccionesRefacturar(datos).$promise.then(function(result) {
                bcsComunes.ventanaOverlay($scope.idVentana, false);
                var transacciones = [];
                $.each(result.P_OUT_TRANSACCIONES, function(key, value) {
                    transacciones.push(value);
                });

                $scope.formData.transaccionesSaldos = transacciones;
                $scope.formData.refrescarGrillaTransacciones();


            }, function(result) {
                bcsComunes.ventanaOverlay($scope.idVentana, false);
                bcsComunes.ventanaMsg({
                    conflictos: util.parseServerResponse(result)
                })

            });

        }

        // Permite reservar o liberar transacciones por refacturacion (Accion  R (Reservar)  L(Liberar)
        $scope.formData.reservarLiberarTransaccion = function(accion)
		{
        	if ($scope.formData.transaccionesSeleccionadas)
        	{
        		//console.log(JSON.stringify($scope.formData.transaccionesSeleccionadas));
        		var estadoAccion = false;
        		if (accion !== 'Reservar' && accion !== 'Liberar')        		
        			return;
        		
        			
        		var transacciones = [];
        		$.each($scope.formData.transaccionesSeleccionadas,function(key,value) { 
        			transacciones.push({IdTransaccion: value.IdTransaccion});
        		});	
        		
        		//console.log(JSON.stringify(transacciones));
        		
        		var datos = {
        				p_IdEmpresa		: $scope.SesionIdEmpresa,
        			    p_IdUsuario		: $scope.SesionIdUsuario,
        			    p_IdModulo		: 'ORD',
        			    p_Accion		: (accion === 'Reservar') ? 'R' : 'L',    //  R(Reservar)  L(Liberar)
        				p_Motivo		: (accion === 'Reservar') ? 'RESERVADA POR REFACTURACION' : '',
        				p_Origen        : 'REFACTURACION',
        				p_Transacciones	:transacciones,
        			};        			
        			
        			bcsServicioOrden.ReservarLiberarTransacciones(datos).$promise.then(function(result){
        				var id = bcsComunes.ventanaMsg({
        						conflictos:util.parseServerResponse(result)
        					},$scope.idVentana);
        				estadoAccion = result.P_OUT_ERRORESCONTADOR === 0 ? true : false;
        				bcsComunes.ventanaPromesa(id).then(function() {
        					//Refresca grilla con datos de reserva si es que se reservo/libero correctamente
                            if (estadoAccion && $scope.formData.transaccionesSaldos.length > 0) {

                                $scope.formData.cargarSaldos($scope.formData.tipoCargaSaldoTransacciones);
                            }
                            if (estadoAccion && $scope.formData.entidadesRefacturar.length > 0) {
                                refrescarTransacciones();
                            }        		            
        				});
        						
        			},function(result){
        				bcsComunes.ventanaMsg({
        					conflictos:util.parseServerResponse(result)
        				},$scope.idVentana);
        			});
        			
                	//Limpiamos la seleccion actual
                	$scope.formData.liberarTransacciones();
        		
        	}
        	

		}
        

        // Selecciona(en grilla) un grupo de transacciones(filas) que cumplan los mismos criterios(Nemotecnico, Cond. Liquidacion y Operacion) que la fila seleccionada
        $scope.formData.agruparTransacciones = function()
		{
        	var newTransaccionesSeleccionadas = [];
        	
        	if ($scope.formData.transaccionesSeleccionadas.length > 1)
        	{
        		return;
        	}
        	
        	//Obtiene los criterior de busqueda desde la fila seleccionada actualmente
        	var instrumentoUI 				= $scope.formData.transaccionesSeleccionadas[0].InstrumentoUI;
        	var idCondicionDeLiquidacion 	= $scope.formData.transaccionesSeleccionadas[0].IdCondicionDeLiquidacion;
        	var accionOrden     			= $scope.formData.transaccionesSeleccionadas[0].AccionOrden;        	


        	//Limpiamos la seleccion actual
        	$scope.formData.liberarTransacciones();
        	
        	
        	
        	//Recorre la grilla en busca de filas que cumplan con los mismos criterios de la fila seleccionada
            $.each($scope.formData.apiTransacciones.dataSource.data(), function(keyTran, valueTran) {
        		//Obtiene una a una la fila de la grilla (OJO: el orden del dataSource no es el mismo que el de grilla)
        		row = $scope.formData.apiTransacciones.tbody.find("tr:eq("+keyTran+")");
        		data = $scope.formData.apiTransacciones.dataItem(row);
        		   
	        	//Revisa si los criterios la fila  coincide con los criterior de la seleccion
	       		if (data != null)
	       		{		
	        		if (data.InstrumentoUI === instrumentoUI && data.IdCondicionDeLiquidacion === idCondicionDeLiquidacion && data.AccionOrden === accionOrden)
	            	{   //Selecciona la fila a nivel de api(visible en grilla) y de seleccionado
	        			$scope.formData.apiTransacciones.select($scope.formData.apiTransacciones.tbody.find("tr")[keyTran]);
	        			newTransaccionesSeleccionadas.push(data);        			
	            	} 
	       		}
            });
             
          
            if (newTransaccionesSeleccionadas.length > 0)
            {
            	$scope.formData.transaccionesSeleccionadas = newTransaccionesSeleccionadas;
            }
 
		}
        

        
        
        
      
        $scope.formData.liberarTransacciones = function()
		{
        	$scope.formData.apiTransacciones.clearSelection();
        	$scope.formData.transaccionesSeleccionadas = undefined;
		}
        
        var refrescarTransacciones = function() {
            var datos = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'ORD',
                p_Origen: $scope.parametros.Origen, //$scope.formData.origenAsignacion,
                p_Entidades: $scope.formData.entidadesRefacturar
            };

            bcsComunes.ventanaOverlay($scope.idVentana, true);
            bcsServicioOrden.ObtenerDetalleRefacturacion(datos).$promise.then(function(result) {
                bcsComunes.ventanaOverlay($scope.idVentana, false);
                var transacciones = [];
                $.each(result.P_OUT_TRANSACCIONES, function(key, value) {
                    transacciones.push(value);
                });

                var asignacionesResum = [];
                $.each(result.P_OUT_ASIGNACIONES, function(key, value) {
                    asignacionesResum.push(value);
                });

                $scope.formData.apiComprobantes.dataSource.data([]);

                var comprobantes = [];
                if (result.P_OUT_COMPROBANTESFACTURACION.length > 0) {
                    $scope.formData.entidadesRefacturar = [];
                };
                $.each(result.P_OUT_COMPROBANTESFACTURACION, function(key, value) {
                    $scope.formData.entidadesRefacturar.push({
                        IdEntidad: value.IdComprobante
                    });
                    comprobantes.push({
                        IdComprobante: value.IdComprobante,
                        FechaLiquidacion: value.FechaLiquidacion,
                        IdTipoComprobante: value.IdTipoComprobante,
                        IdEstadoComprobante: value.IdEstadoComprobante,
                        DocumentoIdentidad: value.DocumentoIdentidad,
                        NombreCompleto: value.NombreCompleto,
                        CodigoPortafolio: value.CodigoPortafolio,
                        NemotecnicoInstrumentoComprobante: value.NemotecnicoInstrumentoComprobante,
                        SeparaPorInstrumento: value.SeparaPorInstrumento,
                        CantidadTotal: value.CantidadTotal,
                        PPP: value.PPP,
                        IdMoneda: value.IdMoneda,
                        IdTipoInstrumento: value.IdTipoInstrumento,

                    })
                });
                $scope.formData.apiComprobantes.dataSource.data(comprobantes);
                $scope.formData.transaccionesComprobantes = transacciones || [];
                $scope.formData.asignacionesComprobantes = asignacionesResum || [];
                $scope.formData.apiClientes.dataSource.data([]);
                $scope.formData.refrescarGrillaTransacciones();


                
                if ($scope.formData.asignacionesComprobantes.length > 0) {
                    var ConsultarCobrosAsignacion = new bcs.DataSource(defCobrosAsignacion);
                    var filtro = Array();
                    filtro.push({
                        field: "IdOrden",
                        operator: "eq",
                        value: $scope.formData.asignacionesComprobantes[0].IdOrden
                    });
                    ConsultarCobrosAsignacion.query({
                        filter: filtro
                    }).then(function() {
                        if (ConsultarCobrosAsignacion.data() != undefined || ConsultarCobrosAsignacion.data().length > 0) {
                            $.each(ConsultarCobrosAsignacion.data(), function(key, value) {
                                cobrosAsignacionAnterior.push({
                                    IdCobro: value.IdCobro,
                                    ValorCobroAplicado: value.ValorCobroAplicado
                                });
                            });
                        }
                    });
                }
                

            }, function(result) {
                bcsComunes.ventanaOverlay($scope.idVentana, false);
                bcsComunes.ventanaMsg({
                    conflictos: util.parseServerResponse(result)
                })
            });
        }

        //Carga archivo de clientes
        function clientesAsignacion(data) {

            var sumaPorcentajes = 0;
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                sumaPorcentajes += Number(value.PorcentajeSimulacion);
            });

            if (data.length > 0) sumaPorcentajes = Math.round(((100 - sumaPorcentajes) / data.length));

            var current = [];
            var portafoliosNuevos = [];
            $.each($scope.formData.apiClientes.dataSource.data(), function(key, value) {
                current.push(value);
            });

            $.each(data, function(key, value) {
                value.CantidadSimulacionCliente = value.CantidadSimulacionCliente;
                value.PorcentajeSimulacion = value.PorcentajeSimulacion;
                value.IdMoneda = value.IdMoneda;
                portafoliosNuevos.push(value);
            });

            var ports = _.uniq(_.union(current, portafoliosNuevos), false, function(item) {
                return item.IdPortafolio;
            });

            $scope.formData.apiClientes.dataSource.data(ports);
            $scope.formData.apiClientes.refresh();
            setearColumnas();

        };


        //Boton Carga clientes
        $scope.formData.cargarGrupoPortafolios = function() {
            var grupoPortafoliosSeleccionado = $scope.formData.grupoPortafoliosSeleccionado;

            if ($scope.formData.apiTransacciones) {
                if ($scope.formData.apiTransacciones.dataSource.data().length === 0) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_TRANSACCIONES");
                    return;
                }

                if (!grupoPortafoliosSeleccionado) {
                    bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_GRUPO");
                    return;
                }

                var confClientes = bcsConfiguracionOrdenesV2.getDataSourcePortafoliosGrupo();
                confClientes.definition.filter = [{
                    field: "IdGrupo",
                    operator: "eq",
                    value: grupoPortafoliosSeleccionado.IdGrupo
                }];

                var dsClientes = new bcs.DataSource(confClientes);
                bcsComunes.ventanaOverlay($scope.idVentana, true);
                dsClientes.fetch().then(function(result) {
                    bcsComunes.ventanaOverlay($scope.idVentana, false);
                    agregarClientes(dsClientes.data());
                }, function() {
                    bcsComunes.ventanaOverlay($scope.idVentana, false);
                });
            } else {
                bcsComunes.ventanaOverlay($scope.idVentana, false);
            }


        }


        $scope.formData.cargarArchivo = function() {
            var codArch = 'CARGA_CLIENTE_ASIGNACION';
            var titulo = 'Carga de Clientes';

            var datosArchivo = {
                codigoArchivo: codArch,
                titulo: titulo
            };
            var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, false, false);
            bcsComunes.ventanaOverlay($scope.idVentana, true);

            var ClienteAsignacion = bcsComunes.ventanaPromesa(id1);

            //
            ClienteAsignacion.then(function(data) {
                //el objeto data, contiene la informacion del archivo en formato JSON.
                if (!data || !data.fileData) {
                    bcsComunes.ventanaOverlay($scope.idVentana, false);
                    return;
                }

                //clientesAsignacion(data);
                var parametrosSP = {};
                parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
                parametrosSP.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
                parametrosSP.p_IdModulo = 'ORD';
                parametrosSP.p_DataArchivo = data.fileData;


                if (data.fileData.length) {
                    obtenerDatosDesdeArchivoAsigClientesSP(parametrosSP).then(function(result) {
                            if (result.P_OUT_ADVERTENCIASCONTADOR) {
                                bcsComunes.msg("warning", "Advertencia", "La operacion tiene advertencias");
                            } else if (result.P_OUT_ERRORESCONTADOR) {
                                bcsComunes.msg("error", "Error", "La operacion tiene errores");
                            } else {
                                // se limpian las grillas
                                clientesAsignacion(result.P_OUT_DATAARCHIVO);

                            }
                            bcsComunes.ventanaOverlay($scope.idVentana, false);
                        },
                        function(result) {
                            bcsComunes.msg("error", "Error", "La operacion tiene errores");
                            bcsComunes.ventanaOverlay($scope.idVentana, false);
                        });
                } else bcsComunes.ventanaOverlay($scope.idVentana, false);
            });



            bcsComunes.ventanaOverlay($scope.idVentana, false);
            return;
        };


        var obtenerDatosDesdeArchivoAsigClientesSP = function(p_datos) {
            var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4238";
            var r = $resource("", {}, {
                'urlConsulta': {
                    method: "POST",
                    url: urlConsulta
                }
            });
            var post = r.urlConsulta(p_datos);

            return post.$promise;
        };

        $scope.formData.opcionesCargaClientes = [{
                titulo: $translate.instant("UI_GRUPO"),
                funcion: $scope.formData.cargarGrupoPortafolios
            },
            {
                titulo: $translate.instant("UI_ARCHIVO"),
                funcion: $scope.formData.cargarArchivo
            }
        ];




        //Postrera
        $scope.formData.previsualizarPostrera = function() {
            $scope.formData.previsualizar('ASIGNACION_POSTRERA');
        }

        $scope.formData.previsualizarPrecioPromedio = function() {
            //if ($scope.configuracionInterfaz.IdClasificacionTipoInstrumento === 'RV' && $scope.configuracionInterfaz.IdTipoInstrumento !== 'SIM') {
            var ncumple = true;
            for (var i = 0; i < $scope.formData.transaccionesComprobantes.length; i++) {
                if ($scope.formData.transaccionesComprobantes[i].IdClasificacionTipoInstrumento !== 'RV' && $scope.formData.transaccionesComprobantes[i].IdTipoInstrumento === 'SIM') {
                    ncumple = false;
                }
            }

            if (ncumple) {
                $scope.formData.previsualizar('ASIGNACION_PRECIO_PROMEDIO');
            } else {
                bcsComunes.msg("error", "error", "UI_TIPO_ASIGNACION_NO_PERMITIDA");
            }
        }

        $scope.formData.previsualizarManualClientes = function() {
            $scope.formData.previsualizar('ASIGNACION_MANUAL_CLIENTES');
        }

        //Revisar columnas de archivo dependiendo 
        var cargaDetalleArchivo = function(data) {
            var Detalle = [];
            $.each(data, function(key, value) {
                Detalle.push({
                    FechaRegisro: value.FechaRegisro,
                    FechaTransaccion: value.FechaTransaccion,
                    Folio: value.Folio,
                    Nemotecnico: value.Nemotecnico,
                    Tipo: value.Tipo,
                    Cantidad: value.Cantidad,
                    DocumentoIdentidad: value.DocumentoIdentidad,
                    Portafolio: value.Portafolio,
                    Comisiones: value.Comisiones,
                    Derechos: value.Derechos,
                    Gastos: value.Gastos,
                    Identificador: value.Identificador
                });
            });
            return Detalle;
        }


        $scope.formData.obtenerDetalleTransacciones = function(cargaMasivaDetalle) 
        {
        	var arrDetalleTran = [];
        	
            $.each($scope.formData.apiTransacciones.dataSource.data(), function(key, value) {
            	var objDetalleTran = {};
            	var incluirTransaccion = false;
            	$.each(cargaMasivaDetalle, function(keyDet, valueDet)
            	{
            		if (valueDet.Folio == value.FolioTransaccion && valueDet.Nemotecnico.trim() == value.InstrumentoUI.trim())
            		{
            			incluirTransaccion = true;
            		}
            	});
            	
            	if (incluirTransaccion) 
            	{            	
            	objDetalleTran.CantidadDisponibleTransaccion = value.CantidadDisponibleTransaccion;
            	objDetalleTran.CantidadTransaccion = value.CantidadTransaccion;
            	objDetalleTran.CantidadTransaccionPorAsignar = value.CantidadTransaccionPorAsignar;
            	objDetalleTran.CantidadSimulacionTransaccion = 0;
            	objDetalleTran.EsFungible = value.EsFungible;
            	objDetalleTran.EsFungibleUI = value.EsFungibleUI;
            	objDetalleTran.EsVentaCorta = value.EsVentaCorta;
            	objDetalleTran.EsVentaCortaUI = value.EsVentaCortaUI;
            	objDetalleTran.FolioTransaccion = value.FolioTransaccion;
            	objDetalleTran.FechaSistemaCreacion = value.FechaSistemaCreacion;
            	objDetalleTran.FechaOperacionTransaccion = value.FechaOperacionTransaccion;
            	objDetalleTran.FechaVencimiento = value.FechaVencimiento;
            	objDetalleTran.HoraSistemaCreacion = value.HoraSistemaCreacion;
            	objDetalleTran.IdBolsa = value.IdBolsa;
            	objDetalleTran.IdCondicionDeLiquidacion = value.IdCondicionDeLiquidacion;
            	objDetalleTran.IdClasificacionTipoInstrumento = value.IdClasificacionTipoInstrumento;
            	objDetalleTran.IdEmisor = value.IdEmisor;
            	objDetalleTran.IdInstrumento = value.IdInstrumento;
            	objDetalleTran.IdMonedaTransaccion = value.IdMonedaTransaccion;
            	objDetalleTran.IdTipoInstrumento = value.IdTipoInstrumento;
            	objDetalleTran.IdTipoOperacion = value.IdTipoOperacion;
            	objDetalleTran.IdTipoProducto = value.IdTipoProducto;
            	objDetalleTran.IdTipoReajuste = value.IdTipoReajuste;
            	objDetalleTran.IdTransaccion = value.IdTransaccion;                        
            	objDetalleTran.IndicadorOrdenDirecta = value.IndicadorOrdenDirecta;
            	objDetalleTran.InstrumentoUI = value.InstrumentoUI;
            	objDetalleTran.MontoTransaccion = value.MontoTransaccion;
            	objDetalleTran.NemotecnicoSVS = value.NemotecnicoSVS;
            	objDetalleTran.NombreTipoOperacion = value.NombreTipoOperacion;
            	objDetalleTran.NombreTipoInstrumento = value.NombreTipoInstrumento;
            	objDetalleTran.NombreClasificacionTipoInstrumento = value.NombreClasificacionTipoInstrumento;
            	objDetalleTran.NombreCondicionDeLiquidacion = value.NombreCondicionDeLiquidacion;
            	objDetalleTran.NombreCorredorContraparte = value.NombreCorredorContraparte;
            	objDetalleTran.PrecioTransaccion = value.PrecioTransaccion;                        	
            	objDetalleTran.Tasa = value.Tasa;
                	objDetalleTran.AccionOrden = value.AccionOrden;   // COMPRA   VENTA

                	arrDetalleTran.push(objDetalleTran);            		
            	}          		
            });
            
            return arrDetalleTran;
        }
      
        
    

        /**
         * Procesa la data del archivo de carga y simula la asignacion
         * tipoCarga   : NEMO, NEMTRA, NEMFAC
         * tipoArchivo : REFACTURACION, REFACTURACION_JPM
         */
        $scope.formData.CargarArchivoRefacturacion = function(tipoCarga, tipoArchivo) {

            if (tipoCarga === "") {
                return;
            }
           
            if (tipoArchivo === "REFACTURACION_JPM")
                var datosArchivo = {codigoArchivo: 'CARGA_REFACTURACION_JPM_2'};
            else
            	var datosArchivo = {codigoArchivo: 'CARGA_REFACTURACION'};	

            

            var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);        
            var promesa = bcsComunes.ventanaPromesa(id1);

          promesa.then(function(data) {
                //El objeto data, contiene la informacion del archivo en formato JSON.
                if (!data || !data.fileData) {
                    return;
                }
                
                var cargaMasivaDetalle = [];
                if (tipoArchivo === "REFACTURACION_JPM") 
                {	//Para este caso los campos del archivo JPM que no se usan pueden venir en blanco(Ej: RutAnterior, CuentaAnterior, FolioFactura)
                    $.each(data.fileData, function(key, value) {
                        cargaMasivaDetalle.push({
                            FechaRegisro		: value.FechaRegistro,
                            FechaTransaccion	: value.FechaTransaccion,
                            Folio				: value.FolioTransaccion,
                            Nemotecnico			: value.Nemotecnico.trim(),
                            Tipo				: value.Tipo,
                            Cantidad			: value.Cantidad,
                            DocumentoIdentidad	: value.DocumentoIdentidad.trim(),
                            Portafolio			: value.Portafolio.trim(),
                            Comisiones			: Number(value.Comisiones),
                            Derechos			: Number(value.Derechos),
                            Gastos				: Number(value.Gastos),
                            Iva					: 19,
                            Identificador		: 'NEMTRA'
                        });
                    });  
                }
                else
                {
                $.each(data.fileData, function(key, value) {
                    cargaMasivaDetalle.push({
                        FechaRegisro: value.FechaRegistro,
                        FechaTransaccion: value.FechaTransaccion,
                        Folio: value.Folio,
                            Nemotecnico			: value.Nemotecnico.trim(),
                        Tipo: value.Tipo,
                        Cantidad: value.Cantidad,
                        DocumentoIdentidad: value.DocumentoIdentidad,
                        Portafolio: value.Portafolio,
                        Comisiones: value.Comisiones,
                        Derechos: value.Derechos,
                        Gastos: value.Gastos,
                        Iva: value.Iva,
                        Identificador: value.Identificador
                    });
                });
                }

                var pregunta = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO'));
                pregunta.result.then(function(result) {

                    if (result > 0) {
                        bcsComunes.ventanaOverlay($scope.IdVentana, true);
                        var parametros = {};
                        parametros.p_IdEmpresa 			= $scope.SesionIdEmpresa;
                        parametros.p_IdUsuario 			= $scope.SesionIdUsuario;
                        parametros.p_IdModulo 			= 'FAC';
                        parametros.p_UsaReglasNegocio 	= 'S';
                        parametros.p_IdTipoCargaMasiva 	= 0;                      
                        parametros.p_CodigoArchivo 		= (tipoArchivo == "REFACTURACION_JPM") ? 'CARGA_REFACTURACION_JPM_2' : 'CARGA_REFACTURACION'; 
                        parametros.p_TipoCarga 			= tipoCarga; //NEMO-NEMTRA-NEMFAC
                        parametros.p_FechaCarga 		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
                        parametros.p_DetalleCargaMasiva = cargaMasivaDetalle; // datos provenientes del archivo
                        parametros.p_DetalleTransacciones = $scope.formData.obtenerDetalleTransacciones(cargaMasivaDetalle); //datos de la grilla transacciones
                        
                        if (parametros.p_DetalleTransacciones.length == 0)
                        {
                            bcsComunes.ventanaMsg({
                                mensaje: $translate.instant("UI_SIN_COINCIDENCIAS_ENTRE_TRANSACCIONES_Y_ARCHIVO")                                
                            }, $scope.IdVentana);
                        	return;
                        }	

                        bcsComunes.log().info(JSON.stringify(parametros));

                        bcsServiciosFacturacion.CargaMasivaRefacturacion(parametros,
                            function(respuesta) {
                                bcsComunes.ventanaOverlay($scope.IdVentana, false);

                                if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
                                    //console.log(respuesta.P_OUT_SIMASIGNACION);
                                    $scope.formData.CobrosFijos = respuesta.P_OUT_COBROSASIGNACION;
                                    procesarResultadoSimulacion(respuesta, "ASIGNACION_PRECIO_PROMEDIO");
                                    bcsComunes.ventanaMsg({
                                        mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
                                        conflictos: util.parseServerResponse(respuesta)
                                    }, $scope.IdVentana);

                                } else {
                                    bcsComunes.ventanaMsg({
                                        mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
                                        conflictos: util.parseServerResponse(respuesta)
                                    }, $scope.IdVentana);
                                }
                            },
                            function(respuesta) {
                                bcsComunes.ventanaMsg({
                                    mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
                                    conflictos: util.parseServerResponse(respuesta)
                                }, $scope.IdVentana);
                                bcsComunes.ventanaOverlay($scope.IdVentana, false);
                            }
                        );

                    }
                });


           });

        }

        
        
        
        
        $scope.formData.CargarArchivoPorNemo = function() {

            $scope.formData.CargarArchivoRefacturacion("NEMO","REFACTURACION");
        }
        $scope.formData.CargarArchivoJPMorgan = function() {
            $scope.formData.CargarArchivoRefacturacionPorFolio("NEMTRA");
        }


        $scope.formData.CargarArchivoJPMorganPorTransaccion = function() {        	

        	$scope.formData.CargarArchivoRefacturacion("NEMTRA","REFACTURACION_JPM");        	
        }
       
        //Proceso que carga a la grilla de transacciones las transacciones con saldo asociadas a una nota de crédito
        $scope.formData.CargarSaldosTransaccionesNotasDeCredito = function() {
        	$scope.formData.cargarSaldos("TRANDC");
        }
        
        //Proceso que carga a la grilla de transacciones las transacciones con saldo disponible
        $scope.formData.CargarSaldosTransaccionesLibres = function() {
        	$scope.formData.cargarSaldos("TRALIB");
        }

        
        $scope.formData.CargarArchivoPorTransaccion = function (){

        	$scope.formData.CargarArchivoRefacturacion("NEMTRA","REFACTURACION");
        }
        

        /*$scope.formData.CargarArchivoPorfacturacion = function (){
        	$scope.formData.TipoCarga="NEMFAC";
        	$scope.formData.CargarArchivoRefacturacion("NEMFAC","REFACTURACION");
        }*/

        $scope.formData.opcionesClientes = [{
                titulo: $translate.instant("UI_POSTRERA"),
                funcion: $scope.formData.previsualizarPostrera
            },
            {
                titulo: $translate.instant("UI_PRECIO_PROMEDIO"),
                funcion: $scope.formData.previsualizarPrecioPromedio
            }
        ];

        

        $scope.formData.opcionesCargarSaldos = [
           { titulo: $translate.instant("UI_CARGAR_SALDOS_TRANSACCIONES_NDC"),    funcion: $scope.formData.CargarSaldosTransaccionesNotasDeCredito},  //Carga transacciones asociadas a notas de credito
           { titulo: $translate.instant("UI_CARGAR_SALDOS_TRANSACCIONES_LIBRES"), funcion: $scope.formData.CargarSaldosTransaccionesLibres}  //Carga transacciones libres con saldo disponible                              
        ];
        

        

        // Opciones de carga de archivos refacturacion 
        if (BCS_CONFIGURACION.FACTURACION["REFAC_MASIVA"] === 'S' ? true : false) {
            $scope.formData.opcionesArchivos = [
            { titulo: $translate.instant("UI_CARGAR_ARCHIVO_POR_FACTURA"),    funcion: $scope.formData.CargarArchivoJPMorgan}, 
            { titulo: $translate.instant("UI_CARGAR_ARCHIVO_POR_TRANSACCION"), funcion :$scope.formData.CargarArchivoJPMorganPorTransaccion }                                 
            ];
        } else {
            $scope.formData.opcionesArchivos = [
                //{ titulo : $translate.instant("UI_CARGAR"), funcion : $scope.formData.cargarGrupoPortafolios($scope.formData.grupoPortafoliosSeleccionado) },
                {
                    titulo: $translate.instant("UI_ARCHIVO_NEMO"),
                    funcion: $scope.formData.CargarArchivoPorNemo
                },
                //{ titulo : $translate.instant("UI_ARCHIVO_TRANSACCION"), funcion :$scope.formData.CargarArchivoPorTransaccion }
            ];
        };

        inicializarGrillas();


        $q.all([promiseGridTransacciones.promise, promiseGridClientes.promise, promiseGridSimulacionAsignaciones.promise, promiseGridAsignacionesResum.promise]).then(function(result) {
            goo();
        });
        //funcion que llama a la carga de archivos
        $scope.formData.CargarArchivoRefacturacionPorFolio = function(TipoCarga) {

            console.log("Entro Carga JP Morgan");
	
            
            var datosArchivo = {
                codigoArchivo: 'CARGA_REFACTURACION_JPM_2'
            };
            var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);
            var promesa = bcsComunes.ventanaPromesa(id1);

            //var s = TipoArchivo;

            promesa.then(function(data) {
                console.log($scope.formData.entidadesRefacturar);
                console.log("Entro Data");
                //El objeto data, contiene la informacion del archivo en formato JSON.			 
                if (!data || !data.fileData) {

                    return;
                }
                var pregunta = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO'));
                pregunta.result.then(function(result) {
                    console.log($scope.formData.entidadesRefacturar);
                    if (result === 3) {             
                        var cargaMasiva = [];
                        cargaMasiva.push({
                            IdInterno: 1,
                            NombreArchivo: data.fileName
                        });
                        var cargaMasivaDetalle = [];
                        $.each(data.fileData, function(key, value) {
                            cargaMasivaDetalle.push({
                            	// MODIFICAR PARA NUEVA CARGA
                                FechaRegisro: value.FechaRegistro,
                                FechaTransaccion: value.FechaTransaccion,
                                Folio: value.FolioTransaccion,
                                FolioFactura: value.FolioFactura,
                                Nemotecnico: value.Nemotecnico.trim(),
                                Tipo: value.Tipo,
                                Cantidad: value.Cantidad,
                                DocumentoIdentidad: value.DocumentoIdentidad.trim(),
                                Portafolio: value.Portafolio.trim(),
                                Comisiones: Number(value.Comisiones),
                                Derechos: Number(value.Derechos),
                                Gastos: Number(value.Gastos),
                                Iva: 19,
                                Identificador: 'NEMTRA',
                                Precio: value.Precio
                            });
                        });
                       
                        bcsComunes.ventanaOverlay($scope.IdVentana, true);
                        var parametros = {};
                        parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
                        parametros.p_IdUsuario = $scope.SesionIdUsuario;
                        parametros.p_IdModulo = 'FAC';
                        parametros.p_UsaReglasNegocio = 'S';
                        parametros.p_IdTipoCargaMasiva = 0;
                        parametros.p_CodigoArchivo = 'CARGA_REFACTURACION_JPM_2';
                        parametros.p_TipoCarga = TipoCarga; //NEMO-NEMTRA-NEMFAC
                        parametros.p_FechaCarga = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
                        parametros.p_DetalleCargaMasiva = cargaMasivaDetalle; // detalle de la carga 
                                                
                        parametros.p_DetalleTransacciones = $scope.formData.obtenerDetalleTransacciones(cargaMasivaDetalle);  //arreglo de transacciones pre cargadas en la grilla
                        

                        if (parametros.p_DetalleTransacciones.length == 0)
                        {
                            bcsComunes.ventanaMsg({
                                mensaje: $translate.instant("UI_SIN_COINCIDENCIAS_ENTRE_TRANSACCIONES_Y_ARCHIVO")                                
                            }, $scope.IdVentana);
                        	return;
                        }
                          
                        
                        bcsComunes.log().info(JSON.stringify(parametros));

                        bcsServiciosFacturacion.CargaMasivaRefacturacion(parametros,
                            function(respuesta) {
                                bcsComunes.ventanaOverlay($scope.IdVentana, false);
                                   var asignaciones = [];
            var asigMap = {};
            $.each(respuesta.P_OUT_SIMASIGNACION, function(key, value) {

                var asig = {
                    CantidadAsignadaSimulacion: value.IdClasificacionTipoInstrumento !== 'IF' ? value.CantidadAsignacion : value.ValorRescateAsignacion,
                    CantidadTransaccion: value.CantidadTransaccion,
                    CodigoIFRS: value.CodigoIFRS,
                    CodigoPortafolio: value.CodigoPortafolio,
                    DocumentoIdentidad: value.DocumentoIdentidad,
                    EsFungible: value.EsFungible,
                    EsExtranjera: value.EsExtranjera,
                    EstiloOpcion: value.EstiloOpcion,
                    FechaTransaccion: value.FechaTransaccion,
                    FolioCarteraAsignada: value.FolioCartera,
                    FolioTransaccion: value.FolioTransaccion,
                    Glosa: value.Glosa,
                    IdBolsa: value.IdBolsa,
                    IdBoveda: value.IdBoveda,
                    IdClasificacionTipoInstrumento: value.IdClasificacionTipoInstrumento,
                    IdCliente: value.IdCliente,
                    IdCorredoraCorresponsal: value.IdCorredoraCorresponsal,
                    IdCuentaCustodioPortafolio: value.IdCuentaCustodioPortafolio,
                    IdFormaCustodia: value.IdFormaCustodia,
                    IdInterno: value.IdInterno,
                    IdInstrumento: value.IdInstrumento,
                    IdMonedaInstrumento: value.IdMonedaInstrumento,
                    IdMonedaLiquidacionOrden: value.IdMonedaLiquidacionOrden,
                    IdMovimiento: value.IdMovimiento,
                    IdOrden: value.IdOrden,
                    IdPreferenciaPagoPortafolio: value.IdPreferenciaPagoPortafolio,
                    IdPortafolio: value.IdPortafolio,
                    IdTipoInstrumento: value.IdTipoInstrumento,
                    IdTransaccion: value.IdTransaccion,
                    IdTipoCanalIngreso: value.IdTipoCanalIngreso,
                    IdTipoOperacion: value.IdTipoOperacion,
                    IdTipoProducto: value.IdTipoProducto,
                    Instrumento: value.Instrumento,
                    MontoAsignacion: 1000, //Regla de 3 > ((Monto Palo * Cantidad Asignada) / Cantidad Palo)
                    MontoTransaccion: value.MontoTransaccion,
                    ModalidadEjercicio: value.ModalidadEjercicio,
                    ModalidadLiquidacion: value.ModalidadLiquidacion,
                    NombreCompleto: value.NombreCompleto,
                    PagoCobroCuentaInversion: value.PagoCobroCuentaInversion,
                    PlazoVentaCorta: Number(value.PlazoVentaCorta),
                    PrecioPromedio: 0,
                    PrecioSimulacion: value.PrecioSimulacion,
                    PrecioStrike: value.PrecioStrike,
                    TasaVentaCorta: Number(value.TasaVentaCorta),
                    Tamano: value.Tamano,
                    TipoCambio: Number(value.TipoCambio),
                    TipoOpcion: value.TipoOpcion,
                    ValorRescateAsignadoSimulacion: value.ValorRescateAsignacion,
                    cobrosAsignacion: [],

                };
                asigMap[value.IdInterno] = asig;
                asignaciones.push(asig);

            });
            $scope.formData.apiSimulacionAsignaciones.dataSource.data(asignaciones);
             $scope.formData.apiSimulacionAsignaciones.refresh();
           
                                                                
                                if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
                                	  if(respuesta.P_OUT_COMPROBANTES.length != 0){
                                         var omitir = false;
                                          var eliminarCompro = [];
                                          var comprobantes  = $scope.formData.apiComprobantes.dataSource.ds._data; 
                                      for(var i = 0; i < comprobantes.length; i++) {
                              			for (var y = 0; y < respuesta.P_OUT_COMPROBANTES.length; y++ ) {
                              				console.log(respuesta.P_OUT_COMPROBANTES[y].toString());
                              				console.log(comprobantes[i].IdComprobante.toString());                          			
                              				if (respuesta.P_OUT_COMPROBANTES[y].toString() === comprobantes[i].IdComprobante.toString()) {
                              				    console.log("Omitido: "+respuesta.P_OUT_COMPROBANTES[y].toString());	
                              				    omitir = true;		 
                              				}
                              			}
                              			if(omitir != true){
                              				    console.log("Eliminado: "+comprobantes[i].IdComprobante.toString());
                              				    eliminarCompro.push(comprobantes[i]); 
                              				    omitir = false;	    
                              			}else{
                              			        omitir = false;
                              			}
                              		};      
                              		
                              		if(eliminarCompro.length != 0){
                              		         console.log(eliminarCompro);
                              		         $scope.formData.quitarComprobante(eliminarCompro);
                              		        
                              		} 
                              		}
                              		$timeout(function(){            	
                                        //console.log(respuesta.P_OUT_SIMASIGNACION);
                                        $scope.formData.CobrosFijos = respuesta.P_OUT_COBROSASIGNACION;
                                        procesarResultadoSimulacion(respuesta, "ASIGNACION_PRECIO_PROMEDIO");                           
                                        bcsComunes.ventanaMsg({
                                            mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
                                            conflictos: util.parseServerResponse(respuesta)
                                        }, $scope.IdVentana);
                                               },2000);  
                              		
                              		
									
                                } else {
                                    bcsComunes.ventanaMsg({
                                        mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
                                        conflictos: util.parseServerResponse(respuesta)
                                    }, $scope.IdVentana);
                                }
                            },
                            function(respuesta) {
                                bcsComunes.ventanaMsg({
                                    mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
                                    conflictos: util.parseServerResponse(respuesta)
                                }, $scope.IdVentana);
                                bcsComunes.ventanaOverlay($scope.IdVentana, false);
                            }
                        );

                    }
                });


            });

        }
        $scope.formData.quitarComprobante = function(comprobante) {

            var comprobantes = comprobante;
            var entidades = $scope.formData.entidadesRefacturar;

            var asdf = _.filter(entidades, function(item) {
                for (var i = 0; i < comprobantes.length; i++) {
                    if (item.IdEntidad === comprobantes[i].IdComprobante) {
                        return false;
                    }
                }
                return true;
            })
            $scope.formData.entidadesRefacturar = asdf;

            $scope.formData.apiComprobantes.dataSource.remove(comprobantes);
            refrescarTransacciones();
            return true;
        }
    });