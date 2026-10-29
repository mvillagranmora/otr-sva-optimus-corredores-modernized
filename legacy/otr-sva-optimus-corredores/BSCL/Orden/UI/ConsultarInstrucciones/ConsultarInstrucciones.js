angular.module('ModuloOrdenes').controller('ConsultarInstruccionesCtrl',
    function ($scope, $translate, bcsComunes, bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2) {
        const self = this;
        const controllerName = 'ConsultarInstruccionesCtrl';

        self.idVentana = bcsComunes.ventanaWinId($scope);
        const args = bcsComunes.ventanaParams($scope.idVentana);

        self.filtros = {};
		
		self.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
		self.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
		self.filtros.fechaConsultaDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
		self.filtros.fechaConsultaHasta 	= util.dateTerminoDia($scope.SesionFechaSistema);
		self.filtros.chkFechaLiquidacion  = true;
		self.filtros.chkInstruccionesVigentes  = true;
		
        self.form = {
            accion: {
                puedeIngresar: true,
                puedeVerBitacora: false,
                puedeConfirmar: false,
                puedeLiquidar: false,
                puedeRechazar: false,
                puedeAnular: false,
            },
            filtrosInstrumentos: {
                IdClasificacionTipoInstrumento: { "field": "IdClasificacionTipoInstrumento", "operator": "eq", "value": "" },
                IdTipoInstrumento: { "field": "IdTipoInstrumento", "operator": "eq", "value": "" }
            },
			filtrosTipoInstrumento : {
				IdEstado 		: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				IdBolsa 		: { "field"   : "IdBolsa","operator": "eq", "value"   : "BCS" },
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "IR" }
			},
			filtrosOperacion : {
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "IR" },
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
			},
			filtrosTipoInstrumentoSubyacente : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" }
			},
			filtrosEstado : {
				NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "INSTRUCCION" }
			},
			filtrosTipoCanalIngreso : {
				complejo : [self.CanalCliente, self.CanalAgente],
				logic : "or",
				filters : [ self.CanalCliente, self.CanalAgente ]
			},
        };

		let dsInstrucciones =  new bcs.DataSource({
                type: "odata",
                definition: {
                    serverFiltering: true,
                    serverPaging: true,
                    serverSorting: true,
                    pageSize: 200,
                    sort: { field: "IdInstruccion", dir: "desc" },
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=700329",
                            dataType: "json"
                        }
                    },
                    schema: bcsConfiguracionOrdenesV2.getSchemaInstruccion()
                },
            });

        self.form.gridInstruccionesConfig = {
            autoBind: false,
            allowCopy: true,
            excel: {
                allPages: true,
            },
            filterable: true,
            groupable: true,
            height: 400,
            pageable: true,
            reorderable: true,
            resizable: true,
            selectable: "multiple, row",
            scrollable: true,
            sortable: true,
            userSettings: true,
            navigatable: true,
            dataSource: dsInstrucciones,
            columns: [
                { field: "IdInstruccion", title: "UI_INSTRUCCION", width: "60px" , groupable: true},
                { field: "NombreEstado", title: "UI_ESTADO", width: "100px", groupable: true },
                { field: "Motivo", title: "UI_MOTIVO", width: "100px", groupable: true },
                { field: "DocumentoIdentidad", title: "UI_RUT_CLIENTE", width: "100px" , groupable: true},
                { field: "NombreCompleto", title: "UI_NOMBRE_COMPLETO_O_RAZON_SOCIAL", width: "200px", groupable: true },
                { field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "100px" , groupable: true},
                { field: "Proposito", title: "UI_PROPOSITO", width: "150px", groupable: true },
                { field: "NombreTipoOperacionInstruccion", title: "UI_TIPO", width: "100px", groupable: true },
                { field: "Mercado", title: "UI_MERCADO", width: "150px" , groupable: true},
                { field: "Nemotecnico", title: "UI_INSTRUMENTO", width: "100px" , groupable: true},
                { field: "FechaUltimaModificacion", title: "UI_FECHA_ULTIMA_ACTUALIZACION", width: "100px", groupable: true },
                { field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "100px", groupable: true },
                { field: "FechaTrade", title: "UI_FECHA_TRADE", width: "100px" },
                { field: "IdCondicionDeLiquidacion", title: "UI_CONDICION_LIQUIDACION", width: "100px", groupable: true },
                { field: "IdMonedaLiquidacion", title: "UI_MONEDA", width: "100px", groupable: true },
                { field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION", width: "100px", groupable: true },
                { field: "Cantidad", title: "UI_CANTIDAD", width: "100px", groupable: true },
				{ field: "ValorRescate", title: "UI_VALOR_RESCATE", width: "100px", groupable: true },
				{ field: "ValorNominal", title: "UI_VALOR_NOMINAL", width: "100px", groupable: true },
                { field: "PrecioInstruccion", title: "UI_PRECIO", width: "100px", groupable: true },
                { field: "TirTasa", title: "UI_TIR_O_TASA", width: "100px", groupable: true },
                { field: "MontoNeto", title: "UI_MONTO", width: "100px", groupable: true },
                { field: "IdEmisor", title: "UI_EMISOR", width: "100px", groupable: true },
                { field: "FechaVencimiento", title: "UI_VENCIMIENTO", width: "100px", groupable: true },
                { field: "IdTipoReajuste", title: "UI_REAJUSTE", width: "100px", groupable: true },
                { field: "PlazoVencimiento", title: "UI_DIAS", width: "40px", groupable: true },
                { field: "TotalCobros", title: "UI_GASTOS", width: "100px", groupable: true },
                { field: "FolioFactura", title: "UI_FOLIO_FACTURA", width: "100px", groupable: true },
                { field: "Intermediario", title: "UI_INTERMEDIARIO", width: "200px", groupable: true },
                { field: "FechaIngreso", title: "UI_FECHA_INGRESO", width: "100px", groupable: true },
                { field: "IdComprobante", title: "UI_FOLIO_POLIZA", width: "100px", groupable: true },
            ]
        };
        self.form.gridInstruccionesApi = {};

        self.form.buscarInstrucciones = function buscarInstrucciones(filtros) {
            self.form.kNgModel = undefined;
			filtros.tipoFecha = self.filtros.chkFechaLiquidacion ? "LIQ" : "TRA";
			filtros.fechaDesde = filtros.fechaConsultaDesde ? util.dateSPdate(filtros.fechaConsultaDesde) : '0001-01-01';
			filtros.fechaHasta = filtros.fechaConsultaHasta ? util.dateSPdate(filtros.fechaConsultaHasta) : $scope.SesionFechaSistema;
			self.form.gridInstruccionesApi.dataSource.filter(self.form.validarFiltros(filtros));
        };

		self.form.validarFiltros = function (filtros){
			
			let filters = new Array;
			
			if(filtros.IdTipoInstrumento){
				filters.push({ field: "IdTipoInstrumentoInstruccion", operator: "eq", value: filtros.IdTipoInstrumento })
			}
			if(filtros.IdTipoOperacion){
				filters.push({ field: "IdTipoOperacion", operator: "eq", value: filtros.IdTipoOperacion })
			}
			if(filtros.IdTipoInstrumentoSubyacente){
				filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.IdTipoInstrumentoSubyacente })
			}
			if(filtros.IdEstadoInstruccion){
				filters.push({ field: "IdEstadoInstruccion", operator: "eq", value: filtros.IdEstadoInstruccion });
			}
			if(filtros.IdTipoCanalIngreso){
				filters.push({ field: "IdTipoCanalIngreso", operator: "eq", value: filtros.IdTipoCanalIngreso });				
			}
			if(self.filtros.chkInstruccionesVigentes){
                filters.push({
                    logic: "or",
                    filters: [
                       {field: "IdEstadoInstruccion", operator: "eq", value: "ING"},
                       {field: "IdEstadoInstruccion", operator: "eq", value: "CNF"},
                       {field: "IdEstadoInstruccion", operator: "eq", value: "RZD"},
                       {field: "IdEstadoInstruccion", operator: "eq", value: "NLQ"},
                       {field: "IdEstadoInstruccion", operator: "eq", value: "SCN"},
                       {field: "IdEstadoInstruccion", operator: "eq", value: "MOD"}
                    ]
                })
			}
			if(filtros.tipoFecha ==="LIQ"){
				filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateInicioDia(filtros.fechaDesde) });
				filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia(filtros.fechaHasta) });
			}else{
				filters.push({ field: "FechaTrade", operator: "gte", value: util.dateInicioDia(filtros.fechaDesde) });
				filters.push({ field: "FechaTrade", operator: "lte", value: util.dateTerminoDia(filtros.fechaHasta) });
			}
			return filters;
		};

        self.form.limpiarFiltros = function limpiarFiltros() {
            self.filtros.IdInstrumento 			= 0;
            self.filtros.IdClasificacionTipoInstrumento = 0;
            self.filtros.IdTipoInstrumento 		= 0;
            self.filtros.AccionOrden 			= '';
            self.filtros.IdEstado 				= '';
			self.filtros.fechaConsultaDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
			self.filtros.fechaConsultaHasta 	= util.dateTerminoDia($scope.SesionFechaSistema);
			self.filtros.chkFechaLiquidacion 	= true;
			self.filtros.chkInstruccionesVigentes  = true;
        };

        self.form.exportarExcel = function exportarExcel() {
            console.log('exportarExcel');
        };

        self.form.seleccionarTodo = function seleccionarTodo() {
            console.log('seleccionarTodo');
        };

        self.form.abrirDetalleInstruccion = function abrirDetalleInstruccion(instruccion) {
            //console.log('abrirDetalleInstruccion');
			bcsComunes.ventanaAbrir("DetalleInstruccion", {IdInstruccion : instruccion[0].IdInstruccion},false,false, self.idVentana);
        
        };

        self.form.ingresarInstruccion = function ingresarInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("IngresarInstruccionMain", {}, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana)
                .then(function (result) {
                    console.log("IngresarVentana result", result);
                    self.form.gridInstruccionesApi.dataSource.read();
                });
        };

        self.form.abrirBitacora = function abrirBitacora() {
            const instruccion = self.form.kNgModel.length === 1 ? self.form.kNgModel[0] : undefined;
            if (instruccion === undefined) {
                bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_INSTRUCCION'), $scope.idVentana);
                return;
            }

            const idPromesaVentana = bcsComunes.ventanaAbrir('BitacoraInstruccion', { idInstruccion: instruccion.IdInstruccion }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
		};

        self.form.confirmarInstruccion = function confirmarInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", { Instrucciones: self.form.kNgModel, flujo: 'CONFIRMAR' }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
        };

        self.form.liquidarInstruccion = function liquidarInstruccion() {
        	
        	 const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", {Instrucciones : self.form.kNgModel, flujo: 'LIQUIDAR'}, false, false, self.idVentana);
        	 console.log("Parametro Ventana:", self.form.kNgModel);
             bcsComunes.ventanaPromesa(idPromesaVentana)
                 .then(function (result) {
                     console.log("IngresarVentana result", result);
                     self.form.buscarInstrucciones(self.filtros);
                 });
        };

        self.form.rechazarInstruccion = function rechazarInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", { Instrucciones: self.form.kNgModel, flujo: 'RECHAZAR' }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
        };
        
        self.form.cancelarInstruccion = function rechazarInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", { Instrucciones: self.form.kNgModel, flujo: 'CANCELAR' }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
        };
        
        self.form.aceptarCancelacionInstruccion = function aceptarCancelacionInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", { Instrucciones: self.form.kNgModel, flujo: 'ACEPTA_SOL_CANCELACION' }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
        };
        
        self.form.rechazarCancelacionInstruccion = function rechazarCancelacionInstruccion() {
            const idPromesaVentana = bcsComunes.ventanaAbrir("ConfirmarInstruccion", { Instrucciones: self.form.kNgModel, flujo: 'RECHAZA_SOL_CANCELACION' }, false, false, self.idVentana);
            bcsComunes.ventanaPromesa(idPromesaVentana).then(function () {
                self.form.buscarInstrucciones(self.filtros);
            });
        };

		$scope.$watch('self.filtros.chkFechaLiquidacion',function(newval,oldval){
			switch (newval){
				case true:  
					self.filtros.chkFechaTrade = false; //desclikea check fecha trade			
					break;
					
				case false: 
					self.filtros.chkFechaTrade = true;
					break;  
			}
		});
		
		$scope.$watch('self.filtros.chkFechaTrade',function(newval,oldval){
			switch (newval){
				case true:  
					self.filtros.chkFechaLiquidacion = false; //desclikea check fecha liquidacion				
					break;
					
				case false: 
					self.filtros.chkFechaLiquidacion = true;
					break;  
			}
		});

		$scope.$watch('self.form.kNgModel',  function (instruccionesSeleccionadas) {
			const libresPago = ['ENTIRV','RECIRV','ENTIRF','RECIRF','ENTIFI','RECIFI'];
			
		    const esSeleccionVacia = !instruccionesSeleccionadas || instruccionesSeleccionadas.length === 0;

		    const tieneInstruccionesIngresadas = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'ING';
            });
		    const tieneInstruccionesConfirmadas = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'CNF';
            });
			const tieneInstruccionesLibrePago = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && libresPago.includes(ins.IdTipoOperacionInstruccion) && ins.IdEstadoInstruccion !== "LIQ";
            });
			const tieneInstruccionesRechazadas = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'RZD';
            });
			const tieneInstruccionesNoLiquidadas = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'NLQ';
            });
			const tieneInstruccionesModificadas = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'MOD';
            });
		    const tieneInstruccionesSolCancelacion = !esSeleccionVacia && instruccionesSeleccionadas.some(function (ins) {
		        return ins && ins.IdEstadoInstruccion === 'SCN';
            });

		    self.form.accion.puedeConfirmar = (tieneInstruccionesIngresadas || tieneInstruccionesModificadas) && !tieneInstruccionesLibrePago;
		    self.form.accion.puedeLiquidar = tieneInstruccionesConfirmadas || tieneInstruccionesLibrePago;
		    self.form.accion.puedeRechazar = tieneInstruccionesIngresadas || tieneInstruccionesConfirmadas || tieneInstruccionesModificadas;
		    self.form.accion.puedeAnular = tieneInstruccionesIngresadas;
		    self.form.accion.puedeVerBitacora = !esSeleccionVacia && instruccionesSeleccionadas.length === 1;
		    self.form.accion.puedeCancelar = !esSeleccionVacia && (tieneInstruccionesIngresadas || tieneInstruccionesRechazadas
		    														||tieneInstruccionesNoLiquidadas || tieneInstruccionesModificadas);
	    	self.form.accion.puedeAceptarCancelacion = !esSeleccionVacia && (tieneInstruccionesSolCancelacion);
	    	self.form.accion.puedeRechazarCancelacion = !esSeleccionVacia && (tieneInstruccionesSolCancelacion);
        })
    });