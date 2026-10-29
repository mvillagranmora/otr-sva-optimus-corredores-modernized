angular.module('ModuloTesoreria').controller('ConsultarComprobanteCtrl', function($scope, $timeout, $modal, $state, bcsComunes,
						bcsAlerta, OPCIONES_BCSALERTA, $translate,
						bcsServiciosTesoreria, bcsBlotterComprobantesGrid,
						$http) {
					$scope.winID = bcsComunes.ventanaWinId($scope);
					bcsComunes.ventanaAutoOverlay($scope.winID, false, 3000);
					$scope.btnActivo = false;

					var requiereCombo = this;
					$scope.requiereCombo = false;
					$scope.visibleTipoSocioNegocio = true;
					$scope.visibleCaja = true;
					$scope.visibleFechaComprobanteOrigen = false;	
					
					$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
					$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
					
					$scope.liquidando = false;
					$scope.parametroEdicionMultiple = 'N';		
					$scope.showListaCuentasSocioNegocio = false;				
			
					var ConsultarParametroEdicionMultiple = function(){
					var defConfiguracionTes = {
						type : "odata",
						definition :	{
								serverFiltering : true,
		            			serverSorting   : true, 
		            			sort 			: { field: "", dir: "DESC" },
		            			transport		: {
				        	    	        read: {
				        	            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1037",
				        						dataType:"json"
				        	            	}
				        	    },
				        	    schema	: { 
		                    			model:	{
												fields :	{
													IdEmpresa			: {type: "int"},												
													CodigoParametro		: {type: "string"},
													Descripcion			: {type: "string"},
													Valor				: {type: "string"},
												}
										}
								}
		
		    			}
					};
					var dsConfiguracionTes = new bcs.DataSource(defConfiguracionTes);
					var filtroConfiguracionTes = new Array();
						filtroConfiguracionTes.push({ field: "CodigoParametro", operator: "eq", value: 'DESPLIEGUEAUTOMATICOCUENTASSOCIONEGOCIO' });
						dsConfiguracionTes.query({ filter: filtroConfiguracionTes }).then(function(){
						$scope.configuracionTesParametroCuentasBancaria= dsConfiguracionTes.data();
				 		$scope.parametroEdicionMultiple = $scope.configuracionTesParametroCuentasBancaria[0].Valor;														
							});				
					};					
					ConsultarParametroEdicionMultiple();
					
					$scope.oConfiguracionCaja = { 
						tipo : "combobox-caja",
						configuracion : {},
						filtros : {
						// IdEstadoCaja : { "field" : "IdEstadoCaja","operator":
						// "eq", "value" : "ABT" },
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.oConfiguracionCajaMoneda = {
						tipo : "combobox-caja-moneda",
						configuracion : {},
						filtros : {
							IdCaja : {
								"field" : "IdCaja",
								"operator" : "eq",
								"value" : ""
							},
							IdMoneda : {
								"field" : "IdMoneda",
								"operator" : "neq",
								"value" : ""
							},
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};

					// Configuración combo Tipo Movimiento (Ingreso/Egreso)
					$scope.oConfiguracionTipoMovimiento = {
						tipo : "combobox-tipo-movimiento",
						configuracion : {},
						filtros : {
							IdModulo : {
								"field" : "IdModulo",
								"operator" : "eq",
								"value" : "TES"
							},
							IdEstado : {
								"field" : "IdEstado",
								"operator" : "eq",
								"value" : "ACT"
							}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};

					$scope.oConfiguracionTipoSocioNegocio = {
						tipo : "combobox-tipo-socio-negocio",
						configuracion : {},
						filtros : {},
						idActual : false,
						seleccionado : false,
						idDefecto : '',
						idPostFiltro : false,
						activado : true
					};

					$scope.oConfiguracionEstadoTesoreria = {
						tipo 			: "combobox-estado-tesoreria",
						configuracion 	: {},
						filtros 		: {
							NombreEntidad 	: {"field" : "NombreEntidad", "operator" : "eq",  "value" : 'TES.Comprobante'},
							IdEstado 		: {"field" : "IdEstado", 	  "operator" : "neq", "value" : 'MOD'},
						},
						idActual 		: false,
						seleccionado 	: false,
						idDefecto 		: 'ING'
					};
					
					$scope.oConfiguracionSocioNegocio = {
						tipo : "combobox-socio-negocio",
						configuracion : {},
						idActual : false,
						seleccionado : false,
						filtros : {
							IdEstado : {
								"field" : "IdEmpresa",
								"operator" : "eq",
								"value" : $scope.SesionIdEmpresa
							}
						},
					};
					$scope.oConfiguracionPortafolio = {
						tipo : "combobox-portafolio",
						configuracion : {},
						idActual : false,
						seleccionado : false,
						filtros : {
							IdCliente : {
								"field" : "IdCliente",
								"operator" : "eq",
								"value" : 0
							}
						},
						activado : true
					};
					
					$scope.oConfiguracionMedioDePago={
						tipo : "combobox-medio-pago-tesoreria",
						configuracion : {},
						filtros : {},
						idActual : false,
						seleccionado : false,
						idDefecto 		: '',
						idPostFiltro  	: false	
					};

					$scope.cmbbxCuentaBancariaSocio = {
						tipo: "combobox-banco",
						configuracion: {},
						filtros: {
							IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
							IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" },
							IdTipoBanco: { "field": "IdTipoBanco", "operator": "eq", "value": '' },
						},
						idActual: false,
						seleccionado: false,
						idDefecto: ''
					};

					$scope.cmbbxCuentaBancariaCorredora = {
						tipo: "combobox-banco",
						configuracion: {},
						filtros: {
							IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
							IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" },
							IdTipoBanco: { "field": "IdTipoBanco", "operator": "eq", "value": '' },
						},
						idActual: false,
						seleccionado: false,
						idDefecto: ''
					};

					$scope.oConfiguracionCuentaBancoCorredora={
						tipo : "combobox-cuentas-bancarias-corredora",
						configuracion : {},
						filtros : {
							IdEstado  : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },				
							IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : $scope.IdMoneda  }, // Cuentas de la misma moneda del comprobante
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
				};
					$scope.oConfiguracionNumeroCuentaBancoCorredora={
						tipo : "combobox-numero-cuentas-bancarias-corredora",
						configuracion : {},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};

					$scope.oConfiguracionClasificacionTipoInstrumento = {
						tipo: "combobox-clasificacion-tipo-instrumento",
						cascadaValor: "IdClasificacionTipoInstrumento",
						seleccionado: false,
						idActual: false,
						filtros: {
							IdClasificacionTipoInstrumento: { "field" : "IdClasificacionTipoInstrumento", "operator" : "neq", "value" : "IR" }
						}
					};
					
					$scope.oConfiguracionTipoInstrumento = {
						tipo: "combobox-tipo-instrumento",
						cascadaValor: "IdTipoInstrumento",
						seleccionado: false,
						idActual: false,
						filtros: {
							IdClasificacionTipoInstrumento: { "field" : "IdClasificacionTipoInstrumento", "operator" : "eq", "value" : "" }
						}
					};
					
					//Filtros para combo Concepto Tesoreria
					$scope.FiltroNombreEntidad		   		= { "field" : "NombreEntidad",        "operator": "eq", "value" : "TES.IngresoEgreso" };
					$scope.FiltroIdEstado			   		= { "field" : "IdEstado",             "operator": "eq", "value" : "VIG" };
					$scope.FiltroDesplegarEnComboBox   		= { "field" : "DesplegarEnComboBox",  "operator": "eq", "value" : "S" };
					$scope.FiltroDisponibleIngresoManual   	= { "field" : "DisponibleIngresoManual",  "operator": "eq", "value" : "S" };
					$scope.FiltroTipoConceptoTesoreria 		= { "field" : "TipoConceptoTesoreria","operator": "contains", "value" : "IN,EG" };  //IN|EG
					$scope.FiltroTipoConceptoTesoreriaVacio = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //vacio es para IN y EG 
					$scope.FiltroTipoConceptoTesoreriaAmbos = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "AM" }; //
					$scope.VisibleOptimus   	            = { "field" : "VisibleOptimus",  "operator": "eq", "value" : "S" };
					$scope.oConfiguracionConcepto={
							tipo 			: "combobox-concepto-tesoreria",
							configuracion 	: {},			
							filtros : {
								complejo : [$scope.FiltroNombreEntidad,$scope.FiltroIdEstado,$scope.FiltroDesplegarEnComboBox,
								            $scope.FiltroDisponibleIngresoManual, $scope.VisibleOptimus
								            ],				
							  	logic: "and",
							  	filters: [
								{
								    logic: "or",
								    filters: [$scope.FiltroTipoConceptoTesoreria, $scope.FiltroTipoConceptoTesoreriaVacio, $scope.FiltroTipoConceptoTesoreriaAmbos]
								},
								$scope.FiltroNombreEntidad,
								$scope.FiltroIdEstado,
								$scope.FiltroDesplegarEnComboBox,
								$scope.FiltroDisponibleIngresoManual,
								$scope.VisibleOptimus
							  ]},
							idActual 		: false,
							seleccionado 	: false,
							idDefecto 		: ''
					};	

					// GRILLA PRINCIPAL
					var dsComprobantes = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefComprobantes);
					$scope.confBasicaComprobantes = {
						dataSource : dsComprobantes,
						columns : bcsBlotterComprobantesGrid.columnasComprobantes(),
						sortable : true,
						pageable : true,
						sort 				: { field: "IdComprobante", dir: "desc" }, 		
						// autoBind : true, //comentar
						filterable : true,
						height : 470,
						selectable : "multiple row",
						navigatable : true,
						gridCreated : function(g) {
							$scope.ApiComprobantes = g;

							// Busqueda Inicial
							var filters = [];
							filters.push({field : "IdEstado", operator : "eq", value : 'ING'});
							filters.push({field : "FechaLiquidacion", operator : "gte", value : util.dateOdata($scope.kngFechaLiquidacionDesde)});
							filters.push({field : "FechaLiquidacion", operator : "lte", value : util.dateTerminoDia($scope.kngFechaLiquidacionHasta)});

							if (sesion.getPreferencias('recordarCliente')) {
								filters.push({field : "IdPersona", operator : "eq", value : Number(sesion.getPreferencias('IdPersonaRecordado'))});
							}

							dsComprobantes.filter(filters);
						},
						rowColorBackground : function(row) {
							var color = "";

							if (row.IdEstadoAutorizacion == 'VIG')
								color = "yellow";
							if (row.IdEstadoAutorizacion == 'AUT')
								color = "green";
							if (row.IdEstado === 'ANU')
								color = "red";
							return color;
						},
						userSettings : true
					};

					$scope.confBasicaComprobanteDetalle = function(item) {
						var dsDef = bcsBlotterComprobantesGrid.dsDefComprobanteDetalle;
						dsDef.definition.filter = {
							field : "IdComprobante",
							operator : "eq",
							value : item.IdComprobante
						};
						return {
							dataSource : new bcs.DataSource(dsDef),
							columns : bcsBlotterComprobantesGrid
									.columnasComprobanteDetalle(),
							sortable : true,
							pageable : true,
							autoBind : true,
							height : 150,
							selectable : "multiple row",
							navigatable : true,
							userSettings : true
						};
					};
					$scope.confBasicaComprobanteFormaPago = function(item) {
						var dsDef = bcsBlotterComprobantesGrid.dsDefComprobanteFormaDePago;
						dsDef.definition.filter = [ {
							field : "IdComprobante",
							operator : "eq",
							value : item.IdComprobante
						}];
						return {
							dataSource : new bcs.DataSource(dsDef),
							columns : bcsBlotterComprobantesGrid
									.columnasComprobanteFormaDePago(),
							sortable : true,
							pageable : true,
							autoBind : true,
							height : 150,
							navigatable : true,
							selectable : "multiple row",
							userSettings : true
						};
					};

					
					$scope.$watch('oConfiguracionEstadoTesoreria.seleccionado',
						function(newval, oldval) {
							if ($scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado === 0) // "TODOS" LOS ESTADO
							{
								$scope.disabledExcluyeComprobantesLiquidados = false; //Solo con "Todos" debe habilitar "Excluir Liquidador"   						
								$scope.ExcluyeComprobantesLiquidados = false;	//Se setea la variable en falso para mostrar comprobantes liquidados
							}
							else
							{
								$scope.ExcluyeComprobantesLiquidados = false;
								$scope.disabledExcluyeComprobantesLiquidados = true;
							}	
							if (($scope.cmbbxCuentaBancariaCorredora.idActual === 0 || $scope.cmbbxCuentaBancariaCorredora.idActual === false) && ($scope.oConfiguracionCuentaBancoCorredora.idActual === 0 || $scope.oConfiguracionCuentaBancoCorredora.idActual === false) && ($scope.oConfiguracionNumeroCuentaBancoCorredora.idActual === 0 || $scope.oConfiguracionNumeroCuentaBancoCorredora.idActual === false)) 
							{
								$scope.cmbbxCuentaBancariaCorredora.idActual = -1;
								$scope.oConfiguracionNumeroCuentaBancoCorredora.idActual = -1;
								$scope.oConfiguracionCuentaBancoCorredora.idActual = -1;
								$scope.requiereCombo = true;

							}
							$scope.requiereCombo = false;
							if($scope.cmbbxCuentaBancariaCorredora.idActual == false || $scope.cmbbxCuentaBancariaCorredora.idActual == -1){
						 	$scope.cmbbxCuentaBancariaCorredora.idActual =0;
							};
							if($scope.oConfiguracionNumeroCuentaBancoCorredora.idActual == false || $scope.oConfiguracionNumeroCuentaBancoCorredora.idActual == -1){
								$scope.oConfiguracionNumeroCuentaBancoCorredora.idActual =0;
							};
							if($scope.oConfiguracionCuentaBancoCorredora.idActual == false || $scope.oConfiguracionCuentaBancoCorredora.idActual == -1){
								$scope.oConfiguracionCuentaBancoCorredora.idActual =0;
							};
					});
					$scope.$watch('cmbbxCuentaBancariaCorredora.seleccionado',
						function(newval, oldval)
						{
							if($scope.cmbbxCuentaBancariaCorredora.idActual != 0){
								$scope.requiereCombo = false;
								$scope.oConfiguracionCuentaBancoCorredora.idActual =0;
								$scope.oConfiguracionNumeroCuentaBancoCorredora.idActual =0;

							}
						}
					);
					$scope.$watch('oConfiguracionNumeroCuentaBancoCorredora.seleccionado',
						function(newval, oldval)
						{
							if($scope.oConfiguracionNumeroCuentaBancoCorredora.idActual != 0){
								$scope.requiereCombo = false;
								$scope.cmbbxCuentaBancariaCorredora.idActual =0;
								$scope.oConfiguracionCuentaBancoCorredora.idActual =0;

							}
						}
					);
					$scope.$watch('oConfiguracionCuentaBancoCorredora.seleccionado',
						function(newval, oldval)
						{
							if($scope.oConfiguracionCuentaBancoCorredora.idActual != 0){
								$scope.requiereCombo = false;
								$scope.oConfiguracionNumeroCuentaBancoCorredora.idActual =0;
								$scope.cmbbxCuentaBancariaCorredora.idActual =0;
							}
						}
					);

					$scope.$watch('oConfiguracionConcepto.seleccionado',
							function(newval, oldval) 
							{
								if ($scope.oConfiguracionConcepto.seleccionado.IdConcepto === 'RECBCP' && $scope.SesionLocale === 'PE')  // Recaudación BCP Perú
								{   //Sólo para PERU	
									$scope.visibleTipoSocioNegocio = false;
									$scope.visibleCaja = false;
									$scope.visibleFechaComprobanteOrigen = true;
								}
								else
								{
									$scope.visibleTipoSocioNegocio = true;
									$scope.visibleCaja = true;
									$scope.visibleFechaComprobanteOrigen = false;									
								}
								
								
						});
					
					
					$scope.$watch('oConfiguracionTipoSocioNegocio.seleccionado',
									function(newval, oldval) {
										if (_.isUndefined(newval)
												|| newval === false)
											return;

										if (newval.CodigoTipoSocioDeNegocio === 'CLI'
												|| newval.CodigoTipoSocioDeNegocio === 0) {
											$scope.oConfiguracionPortafolio.activado = true;
										} else // Para: PRO(proveedor)
												// CNT(Contraparte)
												// EMP(Empleado)
										{
											$scope.oConfiguracionPortafolio.idActual = 0;
											$scope.oConfiguracionPortafolio.activado = false;
										}
									});

					$scope.$watch('oConfiguracionSocioNegocio.seleccionado',
									function(newval, oldval) {
										if (_.isUndefined(newval)
												|| newval === false)
											return;

										if ($scope.oConfiguracionTipoSocioNegocio.seleccionado.CodigoTipoSocioDeNegocio === 'CLI'
												|| $scope.oConfiguracionTipoSocioNegocio.seleccionado.CodigoTipoSocioDeNegocio === 0) {
											$scope.oConfiguracionPortafolio.activado = true;
										} else // Para: PRO(proveedor)
												// CNT(Contraparte)
												// EMP(Empleado)
										{
											$scope.oConfiguracionPortafolio.idActual = 0;
											$scope.oConfiguracionPortafolio.activado = false;
										}
									});
					
					$scope.$watch('oConfiguracionTipoMovimiento.seleccionado',function(newval,oldval){
						if (typeof $scope.oConfiguracionTipoMovimiento.seleccionado !== 'undefined' && $scope.oConfiguracionTipoMovimiento.seleccionado.seleccionado !== false)
						{						
							//Se actualizan filtros para conceptos
							$scope.FiltroTipoConceptoTesoreria.value = $scope.oConfiguracionTipoMovimiento.seleccionado.Tipo==null?"":$scope.oConfiguracionTipoMovimiento.seleccionado.Tipo;  // IN | EG
							
							if ($scope.oConfiguracionTipoMovimiento.seleccionado.Tipo==='EG')
							{
								$scope.oConfiguracionMedioDePago.idActual = 'TRA';
							}else{
								$scope.oConfiguracionMedioDePago.idActual = 0;
							}						
						}
						
					});

					$scope.edicionFormaDePago = function(selectRow) {
						if (selectRow.length > 1) {
							bcsComunes
									.ventanaMsgErr(
											$translate
													.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'),
											$scope.winID);
							return;
						}

						// No se puede editar movimientos anulados, bloqueados,
						// girados, liquidados
						if (selectRow[0].IdEstado !== 'ING') {
							bcsComunes
									.ventanaMsgErr(
											$translate
													.instant("UI_SELECCIONAR_COMPROBANTES_VALIDOS_PARA_EDICION"),
											$scope.winID);
							return;
						}
						;

						var objFormaDePago = $scope.seleccionComprobantes;
						var ventanaFormaDePago = bcsComunes.ventanaAbrir(
								"EditarComprobanteFormaDePago", objFormaDePago,
								false, false, $scope.winID);
						var promesa = bcsComunes
								.ventanaPromesa(ventanaFormaDePago);

						promesa.then(function(data) {
							dsComprobantes.read();
						});
					};

					$scope.buscarComprobantes = function() {
						limpiarSeleccionComprobantes();
						var filters = new Array();
						
						if ($scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento && $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 0){
							filters.push({	field : "IdTipoMovimiento", operator : "eq",value : $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento});
						} else {filters.push({	field : "IdTipoMovimiento", operator : "eq",value : -1 });}
						filters.push({field : "IdPersona",operator : "eq",value : Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona || -1)});
						filters.push({field : "IdPortafolioTitularPago",operator : "eq",value : Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || -1)	});
						filters.push({field : "IdCaja",operator : "eq",value : $scope.oConfiguracionCaja.seleccionado.IdCaja || -1 });
						filters.push({field : "IdMoneda",operator : "eq",value : $scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda || -1 });						
						filters.push({field : "IdEstado",operator : "eq",value : $scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado  || -1 });
						if ($scope.ExcluyeComprobantesLiquidados)
							filters.push({field : "IdEstado",operator : "neq",	value : 'LIQ'});
						if ($scope.oConfiguracionConcepto.seleccionado.IdConcepto && $scope.oConfiguracionConcepto.seleccionado.IdConcepto != 0){
							filters.push({field : "IdConcepto",operator : "eq",value : $scope.oConfiguracionConcepto.seleccionado.IdConcepto});
						}else {filters.push({field : "IdConcepto",operator : "eq",value : -1});}						
						filters.push({field : "FechaLiquidacion",operator : "gte",value : util.dateOdata($scope.kngFechaLiquidacionDesde) || -1 });
						filters.push({field : "FechaLiquidacion",operator : "lte",value : util.dateTerminoDia($scope.kngFechaLiquidacionHasta)  || -1});
						filters.push({field : "IdTipoPersona",operator : "eq",value : $scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio  || -1});
						filters.push({field : "IdMedioDePago",operator : "eq",value : $scope.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || -1 });
						filters.push({field : "NombreBancoSocio",operator: "eq",value: $scope.cmbbxCuentaBancariaSocio.seleccionado.NombreBanco || -1});
						filters.push({field : "NombreBancoCorredora",operator: "eq",value: $scope.cmbbxCuentaBancariaCorredora.seleccionado.NombreBanco || -1});
						if ($scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreCuentaBancaria !== ' TODOS') {
													filters.push({field : "AliasBanco",operator: "eq",value: $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreCuentaBancaria || -1 });
												}
						if ($scope.oConfiguracionNumeroCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria !== ' TODOS') {
							filters.push({field : "NumeroCuentaBancariaCorredora",operator: "eq",value: $scope.oConfiguracionNumeroCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria || -1 });
						}
						filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || -1 });	
						filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || -1 });	

						//Solo para el primer origen ya que puede tener n
						if ($scope.kngFechaComprobanteOrigen && $scope.kngFechaComprobanteOrigen != '0001-01-01')
							filters
									.push({
										field : "FechaComprobanteOrigen",
										operator : "eq",
										value : $scope.kngFechaComprobanteOrigen
									});
						let filtrosGrid = []
						if(filtrosGrid = $scope.ApiComprobantes.dataSource.ds._filter != undefined ) {
							filtrosGrid = $scope.ApiComprobantes.dataSource.ds._filter.filters;
						}
					
						var hashmap = new Map();
						
						//ingresamos filtros de busqueda a hash
						filters.forEach(element =>{
							hashmap.set(element.field+"**"+element.operator, element)
						});
						
						//ingresamos filtros de grilla al hash
						filtrosGrid.forEach(element =>{
							let validador= hashmap.get(element.field+"**"+element.operator);
						
							if(!validador){
								hashmap.set(element.field+"**"+element.operator, element);
							}
							
						});
						//limpiamos filters
						filters = new Array();
						
						//llenamos filters desde el hash
						hashmap.forEach( (value, key, map) => {
							if (value.value != -1){
								filters.push(value);
							}
						});
						
							dsComprobantes.filter(filters);
						};

					$scope.limpiarFiltros = function() {
						$scope.oConfiguracionTipoMovimiento.idActual = 0;
						$scope.oConfiguracionCaja.idActual = 0;
						$scope.oConfiguracionCajaMoneda.idActual = 0;
						$scope.oConfiguracionEstadoTesoreria.idActual = 0;
						$scope.oConfiguracionSocioNegocio.idActual = false;
						$scope.oConfiguracionConcepto.idActual = 0;
						$scope.oConfiguracionTipoSocioNegocio.idActual = 0;
						$scope.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
						$scope.oConfiguracionTipoInstrumento.IdClasificacionTipoInstrumento = 0;
						$scope.oConfiguracionMedioDePago.idActual = 0;
						$scope.cmbbxCuentaBancariaSocio.idActual = 0;
						$scope.cmbbxCuentaBancariaCorredora.idActual = 0;
						$scope.oConfiguracionCuentaBancoCorredora.idActual = 0;
						$scope.oConfiguracionNumeroCuentaBancoCorredora.idActual = 0;
						$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
						$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
						$scope.kngFechaComprobanteOrigen = "0001-01-01";   //Debe quedar vacía(sin fecha por defecto)

						dsComprobantes.removeAll();
						$scope.seleccionComprobantes = [];
						$scope.seleccionComprobanteDetalle = [];
						$scope.seleccionComprobanteFormaPago = [];
					};

					// -----------------------------------------------
					// EDITAR FORMA DE PAGO
					// -----------------------------------------------

					// OPCIONES
					$scope.liquidar = function() {
						if ($scope.liquidando) {
							return;
						}
						$scope.liquidando = true;
						
						var dataCols = [];
						$.each($scope.seleccionComprobantes, function(index,value) {
							// if (value.IdEstado === 'ING' ||value.IdEstado ===
							// 'LIP')
							dataCols.push({
								IdComprobante : value.IdComprobante,
								IdTipoMovimiento : value.IdTipoMovimiento,
								AfectaCuentaInversion : 'CLI' // Solo afecta
																// Cta. Inv. del
																// cliente
							});
					   	});
						if (dataCols.length === 0) {
							$scope.liquidando = false;
							bcsComunes.msg("warning","warning",
											$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
							return;
						}

						var t = bcsAlerta.crear(	"PREGUNTA",$translate.instant('UI_SE_LIQUIDARAN_LOS_COMPROBANTES_SELECCIONADOS'));
						t.result.then(function(result) {
										if (result !== OPCIONES_BCSALERTA.ACEPTAR) {
											$scope.liquidando = false;
											return;
										}

										bcsComunes.ventanaOverlay($scope.winID,	true);

     									var datos = {};
										datos.p_IdEmpresa = $scope.SesionIdEmpresa;
										datos.p_IdUsuario = $scope.SesionIdUsuario;
										datos.p_IdModulo = 'TES';
										datos.p_UsaReglasNegocio = 'S';
										datos.p_FechaMovimiento = util.dateSPdate(util
														.dateInicioDia($scope.SesionFechaSistema));
										datos.p_ComprobanteLiquidar = dataCols;
										datos.p_FormaDePagoLiquidar = []; // Liquida todas las formas
																		// de pago de los  comprobantes
	
                               bcsServiciosTesoreria.PreLiquidarComprobantes(datos,function(respuesta){
									bcsComunes.ventanaOverlay($scope.winID,	false);
									$scope.liquidando = false;				
   							    	if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
										liquidarPos(datos);

										} else {	
    										  var PreMernsaje=bcsComunes.ventanaAbrir("ModalVentanaMensajeResultado",{
													mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
													conflictos : util.parseServerResponse(respuesta)
													} , false, false);
													
										          	var promesa = bcsComunes.ventanaPromesa(PreMernsaje);							
												    promesa.then(function(resultado){ 
												    if (resultado === OPCIONES_BCSALERTA.ACEPTAR){ 
														liquidarPos(datos);
														
											      	}
											   }); 
										}
								    }, function(){
										bcsComunes.ventanaOverlay($scope.winID, false);
										$scope.liquidando = false;
									}); 												        
							  	}, function(){	
									$scope.liquidando = false;
								});							
					};
					liquidarPos = function(datos) {
						bcsServiciosTesoreria .LiquidarComprobantes(datos,
							function(respuesta) {
								bcsComunes.ventanaOverlay($scope.winID,	false);
		            	if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			            	bcsComunes.ventanaMsg({
		   		        	 mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
				        	conflictos : util.parseServerResponse(respuesta)
				         	},
				        	$scope.winID);
				        	$scope.buscarComprobantes();
				      	} else {
				        	bcsComunes.ventanaMsg({
					    	mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					    	conflictos : util.parseServerResponse(respuesta)
					    	},
					    	$scope.winID);
					     }
					    },function(respuesta) {
				        	bcsComunes.ventanaMsg({
					    	mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						    conflictos : util.parseServerResponse(respuesta)
						   },
						    $scope.winID);
						    bcsComunes.ventanaOverlay(
						    $scope.winID,false);
							});
					}

					$scope.generarComprobantes = function() {
						if(!bcsComunes.validarAcceso("ConsultarComprobantesTesoreria_btn_AgruparComprobantes")){return;}
						var data = dsComprobantes.dataColumns([
								"NombrePersona", "IdMoneda",
								"IdTipoMovimiento", "NombreTipoMovimiento",
								"Monto" ], $scope.seleccionComprobantes);

						var vistaPrevia = bcsComunes.ventanaAbrir(
								"VistaPreviaComprobante", data, false, false);
						var promesa = bcsComunes.ventanaPromesa(vistaPrevia);
						promesa
								.then(function(data) {
									if (data === "true") {
										bcsComunes.ventanaOverlay($scope.winID,
												true);


										var dataCols = [];
										$
												.each(
														$scope.seleccionComprobantes,
														function(index, value) {
															dataCols[index] = {
																'NumeroReferencia' : 1,
																'IdComprobante' : value.IdComprobante,
																'TipoComprobante' : ''
															};
														});

										var datos = {};
										datos.p_IdEmpresa = $scope.SesionIdEmpresa;
										datos.p_IdUsuario = $scope.SesionIdUsuario;
										datos.p_IdModulo = 'TES';
										datos.p_FechaMovimiento = util
												.dateSPdate(util
														.dateInicioDia($scope.SesionFechaSistema));
										datos.p_IdCaja = 0; // buscar la caja
															// del usuario
															// registrado
										datos.p_GenerarComprobanteDesdeComprobantes = 'S';
										datos.p_IngresoEgresoGenerar = [];
										datos.p_ComprobanteGenerar = dataCols;
										datos.p_FormasDePago = [];

										bcsComunes.log().info(
												JSON.stringify(datos));
										bcsServiciosTesoreria
												.CrearComprobante(
														datos,
														function(respuesta) {
															bcsComunes
																	.ventanaOverlay(
																			$scope.winID,
																			false);
															bcsComunes
																	.ventanaMsg(
																			{
																				conflictos : util
																						.parseServerResponse(respuesta)
																			},
																			$scope.winID);
															$scope
																	.buscarComprobantes();
														},
														function(respuesta) {
															bcsComunes
																	.ventanaMsg(
																			{
																				mensaje : $translate
																						.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
																				conflictos : util
																						.parseServerResponse(respuesta)
																			},
																			$scope.winID);
															bcsComunes
																	.ventanaOverlay(
																			$scope.winID,
																			false);
														});
									}
								});
						return;
					};

					$scope.validarGirarComprobantesEgreso = function(datos, comprobantesAGirar) {
						bcsServiciosTesoreria.ValidarComprobantesEgreso( datos, function(respuesta) {
							if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
								var parametrosVentana = [];
								// Si las validaciones resultaron exitosas, abre ventana de Giro de Egresos con los comprobantes seleccionados.
								parametrosVentana
								.push({
									IdConcepto : respuesta.p_out_IdConcepto,
									NombreConcepto : respuesta.p_out_NombreConcepto,
									IdMoneda : respuesta.p_out_IdMoneda,
									Monto : 0, // CAMBIAR
									NombreMoneda : respuesta.p_out_NombreMoneda,
									FechaLiquidacion : util.dateInicioDia(respuesta.p_out_FechaLiquidacion),
									IdCaja : respuesta.p_out_IdCaja,
									NombreCaja : respuesta.p_out_NombreCaja,
									comprobantesAGirar : comprobantesAGirar,
									mediosDePagoAGirar : respuesta.p_out_MediosDePagoAGirar,
									IdTipoMovimiento : 'MOVEGR',
									TipoMovimiento : 'PAREGR' 

								});

								bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								bcsComunes.ventanaOverlay($scope.winID, false);

								var ventanaGiroEgresos = bcsComunes.ventanaAbrir("GiroEgresos",parametrosVentana,false, false);
								var promesa = bcsComunes.ventanaPromesa(ventanaGiroEgresos);
								promesa.then(function(data) {
									$scope.buscarComprobantes();
								});

							} else {
								bcsComunes.ventanaMsg(
												{mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
													conflictos : util.parseServerResponse(respuesta)
												}, $scope.winID);
							}
							bcsComunes.ventanaOverlay(
									$scope.winID, false);
						}, function(respuesta) {
							bcsComunes.ventanaMsg({
												mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
												conflictos : util.parseServerResponse(respuesta)
											}, $scope.winID);
							bcsComunes.ventanaOverlay(
									$scope.winID, false);
						});
					};

					$scope.validarGirarComprobantesIngreso = function(datos, comprobantesAGirar) {
						bcsServiciosTesoreria.GirarComprobantesIngreso( datos, function(respuesta) {
							if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
								var parametrosVentana = [];
								parametrosVentana
									.push({
										IdConcepto : respuesta.p_out_IdConcepto,
										NombreConcepto : respuesta.p_out_NombreConcepto,
										IdMoneda : respuesta.p_out_IdMoneda,
										Monto : 0, // CAMBIAR
										NombreMoneda : respuesta.p_out_NombreMoneda,
										FechaLiquidacion : util.dateInicioDia(respuesta.p_out_FechaLiquidacion),
										IdCaja : respuesta.p_out_IdCaja,
										NombreCaja : respuesta.p_out_NombreCaja,
										comprobantesAGirar : comprobantesAGirar,
										mediosDePagoAGirar : respuesta.p_out_MediosDePagoAGirar,
										IdTipoMovimiento : 'MOVING',
										TipoMovimiento : 'PARING' 

									});

								bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								bcsComunes.ventanaOverlay($scope.winID, false);

								var ventanaGiroIngresos = bcsComunes.ventanaAbrir("GiroIngreso",parametrosVentana,false, false);
								var promesa = bcsComunes.ventanaPromesa(ventanaGiroIngresos);
								promesa.then(function(data) {
											$scope.buscarComprobantes();
										});
							} else {
								bcsComunes.ventanaMsg(
												{mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
													conflictos : util.parseServerResponse(respuesta)
												}, $scope.winID);
							}
							bcsComunes.ventanaOverlay(
									$scope.winID, false);
						},
						function(respuesta) {
							bcsComunes.ventanaMsg({
												mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
												conflictos : util.parseServerResponse(respuesta)
											}, $scope.winID);
							bcsComunes.ventanaOverlay(
									$scope.winID, false);
						});
					};

					$scope.girarIngresoEgresosTodos = function(tipoMovimiento) {

						bcsComunes.ventanaOverlay($scope.winID, true);
						let listaDeEgresosIngresos = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering: true,
								sort: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.sort,
								transport: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.transport,
								filter: $scope.ApiComprobantes.dataSource.ds._filter.filters,
								schema: bcsBlotterComprobantesGrid.dsDefComprobantes.definition.schema
							}
						});
						
						listaDeEgresosIngresos.query().then(function (result) {
							if(listaDeEgresosIngresos.data().length > 0){

								let comprobantesAGirar = [...new Set(listaDeEgresosIngresos.data().map( obj => ({"IdComprobante": obj.IdComprobante}) ))];
								if (comprobantesAGirar.length === 0) {
									bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")}, $scope.winID);
									return;
								};
								var datos = {};
								datos.p_IdEmpresa = $scope.SesionIdEmpresa;
								datos.p_IdUsuario = $scope.SesionIdUsuario;
								datos.p_IdModulo  = 'TES';
								datos.p_simularOperacion = 'S'; // Para este caso debe ir 'S' para que sólo valide
								datos.p_UsaReglasNegocio = 'S';
								datos.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
								datos.p_ComprobantesAGirar = comprobantesAGirar;
								// Estos parámetros se envían vacios, ya que en esta instancia este servicio se usa solo para validar las condiciones de los comprobantes seleccionados para girar.						
								datos.p_GeneraNomina = '';  
								datos.p_GeneraTransferencia = '';
								datos.p_IdMedioDePago = '';
								datos.p_IdCuentaBancariaCorredora = 0;
								datos.p_Monto = 0;
								// datos.p_IdCaja = 0 ;
								datos.p_FormasDePago = [];
								datos.p_usaTalonarioCheque = '';
								datos.p_IdModalidadDePago = '';

								if( tipoMovimiento === 'EGRESO')
									$scope.validarGirarComprobantesEgreso( datos,comprobantesAGirar );
								else if (( tipoMovimiento === 'INGRESO'))
									$scope.validarGirarComprobantesIngreso( datos,comprobantesAGirar );
										
							}

						});
				};

					$scope.girarIngreso = function() {
						var comprobantesAGirar = [];
						$.each(
									$scope.seleccionComprobantes,
									function(key, value) {
											comprobantesAGirar.push({IdComprobante : $scope.seleccionComprobantes[key].IdComprobante});
									});

						if (comprobantesAGirar.length === 0) {
							bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")}, $scope.winID);
							return;
						};

						bcsComunes.ventanaOverlay($scope.winID, true);

						var datos = {};
						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdModulo  = 'TES';
						datos.p_simularOperacion = 'S'; // Para este caso debe ir 'S' para que sólo valide
						datos.p_UsaReglasNegocio = 'S';
						datos.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_ComprobantesAGirar = comprobantesAGirar;

						// Estos parámetros se envían vacios, ya que en esta instancia este servicio se usa solo para validar las condiciones de los comprobantes seleccionados para girar.						
						datos.p_GeneraNomina = '';  
						datos.p_GeneraTransferencia = '';
						datos.p_IdMedioDePago = '';
						datos.p_IdCuentaBancariaCorredora = 0;
						datos.p_Monto = 0;
						datos.p_FormasDePago = [];
						datos.p_usaTalonarioCheque = '';
						datos.p_IdModalidadDePago = '';

						bcsServiciosTesoreria.GirarComprobantesIngreso(
								datos,
								function(respuesta) {
									if (respuesta.P_OUT_ERRORESCONTADOR == "0") {
										var parametrosVentana = [];
										var mediosDePagoAGirar = {};

										// Si las validaciones resultaron exitosas, abre ventana de Giro de Egresos con los comprobantes seleccionados.
										var comprobantesAGirar = [];
										$.each($scope.seleccionComprobantes,
														function(key,value) {
															comprobantesAGirar.push({IdComprobante : $scope.seleccionComprobantes[key].IdComprobante});
														});

										parametrosVentana
												.push({
													IdConcepto : respuesta.p_out_IdConcepto,
													NombreConcepto : respuesta.p_out_NombreConcepto,
													IdMoneda : respuesta.p_out_IdMoneda,
													Monto : 0, // CAMBIAR
													NombreMoneda : respuesta.p_out_NombreMoneda,
													FechaLiquidacion : util.dateInicioDia(respuesta.p_out_FechaLiquidacion),
													IdCaja : respuesta.p_out_IdCaja,
													NombreCaja : respuesta.p_out_NombreCaja,
													comprobantesAGirar : comprobantesAGirar,
													mediosDePagoAGirar : respuesta.p_out_MediosDePagoAGirar,
													IdTipoMovimiento : 'MOVING',
													TipoMovimiento : 'PARING' 

												});

										bcsComunes.setConflictos(util.parseServerResponse(respuesta));
										bcsComunes.ventanaOverlay($scope.winID, false);

										var ventanaGiroIngresos = bcsComunes.ventanaAbrir("GiroIngreso",parametrosVentana,false, false);
										var promesa = bcsComunes.ventanaPromesa(ventanaGiroIngresos);
										promesa.then(function(data) {
													$scope.buscarComprobantes();
												});
									} else {
										bcsComunes.ventanaMsg(
														{mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
															conflictos : util.parseServerResponse(respuesta)
														}, $scope.winID);
									}
									bcsComunes.ventanaOverlay(
											$scope.winID, false);
								},
								function(respuesta) {
									bcsComunes.ventanaMsg({
														mensaje : $translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
														conflictos : util.parseServerResponse(respuesta)
													}, $scope.winID);
									bcsComunes.ventanaOverlay(
											$scope.winID, false);
								});
					};
					$scope.girarEgresos = function() {
						var comprobantesAGirar = [];
						$.each(
									$scope.seleccionComprobantes,
									function(key, value) {
											comprobantesAGirar.push({IdComprobante : $scope.seleccionComprobantes[key].IdComprobante});
									});

						if (comprobantesAGirar.length === 0) {
							bcsComunes.ventanaMsg({mensaje : $translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")}, $scope.winID);
							return;
						};

						bcsComunes.ventanaOverlay($scope.winID, true);

						var datos = {};
						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdModulo  = 'TES';
						datos.p_simularOperacion = 'S'; // Para este caso debe ir 'S' para que sólo valide
						datos.p_UsaReglasNegocio = 'S';
						datos.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_ComprobantesAGirar = comprobantesAGirar;

						// Estos parámetros se envían vacios, ya que en esta instancia este servicio se usa solo para validar las condiciones de los comprobantes seleccionados para girar.						
						datos.p_GeneraNomina = '';  
						datos.p_GeneraTransferencia = '';
						datos.p_IdMedioDePago = '';
						datos.p_IdCuentaBancariaCorredora = 0;
						datos.p_Monto = 0;
						datos.p_FormasDePago = [];
						datos.p_usaTalonarioCheque = '';
						datos.p_IdModalidadDePago = '';

						$scope.validarGirarComprobantesEgreso( datos,comprobantesAGirar );

					};

					$scope.anularComprobantes = function() {
						if(!bcsComunes.validarAcceso("ConsultarComprobantesTesoreria_btn_Anular")){return;}
						var dataCols = [];
						$.each($scope.seleccionComprobantes, function(index,
								value) {
							dataCols.push({
								Id : value.IdComprobante,
								Entidad : 'TES.Comprobante'
							})
						});

						if (dataCols.length === 0) {
							bcsComunes
									.ventanaMsg(
											{
												mensaje : $translate
														.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS")
											}, $scope.winID);
							return;
						}
						;

						var t = bcsAlerta
								.crear(
										"PREGUNTA",
										$translate
												.instant('UI_SE_ANULARAN_LOS_COMPROBANTES_SELECCIONADOS'));
						t.result
								.then(function(result) {
									if (result === OPCIONES_BCSALERTA.ACEPTAR) {

										bcsComunes.ventanaOverlay($scope.winID,
												true);
 

										var datos = {};
										datos.p_IdEmpresa = $scope.SesionIdEmpresa;
										datos.p_IdUsuario = $scope.SesionIdUsuario;
										datos.p_IdModulo = 'TES';
										datos.p_UsaReglasNegocio = 'S';
										datos.p_FechaMovimiento = util
												.dateSPdate(util
														.dateInicioDia($scope.SesionFechaSistema));
										datos.p_RegistrosAProcesar = dataCols;

										bcsServiciosTesoreria
												.AnularRegistros(
														datos,
														function(respuesta) {
															if (respuesta.P_OUT_ERRORESCONTADOR == "0") {

																if (respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
																	bcsComunes
																			.ventanaMsg(
																					{
																						mensaje : $translate
																								.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					$scope.winID);
																} else {
																	bcsComunes
																			.ventanaMsg(
																					{
																						mensaje : $translate
																								.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					$scope.winID);
																}

																$scope
																		.buscarComprobantes();
															} else {
																bcsComunes
																		.ventanaMsg(
																				{
																					mensaje : $translate
																							.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
																					conflictos : util
																							.parseServerResponse(respuesta)
																				},
																				$scope.winID);
															}
															bcsComunes
																	.ventanaOverlay(
																			$scope.winID,
																			false);
														}, error);
									}
								});
						return;
					};
					
					$scope.edicionMultiple = function() {
						var data = dsComprobantes.dataColumns(
								[ "IdComprobante" ],
								$scope.seleccionComprobantes);
						data = _.map(data, function(info) {
							return {
								Id : info.IdComprobante
							}
						});
						cerrarventanasPorNombre('EdicionMultipleTesoreria');
						if (($scope.parametroEdicionMultiple === "S") && ($scope.seleccionComprobantes[0].IdTipoMovimiento === 'MOVEGR' && ($scope.seleccionComprobantes[0].IdTipoPersona === 'CNT'))) {					
							$scope.showListaCuentasSocioNegocio = true;
						} else {
							$scope.showListaCuentasSocioNegocio= false;						
						};	
						
						
						var j = 0;
						var IdTipoMovimiento = '';
						var IdMedioDePago = '';
						var IdMoneda = '';
						var countEstadoInvalidoParaEdicion = 0;

						for (i = 0; i < data.length; i++) {
							if (IdTipoMovimiento !== $scope.seleccionComprobantes[i].IdTipoMovimiento) {
								if (IdTipoMovimiento !== "")
									j++;
								IdTipoMovimiento = $scope.seleccionComprobantes[i].IdTipoMovimiento;
							}
							if (IdMoneda !== $scope.seleccionComprobantes[i].IdMoneda) {
								if (IdMoneda !== "")
									j++;
								IdMoneda = $scope.seleccionComprobantes[i].IdMoneda;
							}
							if ($scope.seleccionComprobantes[i].IdEstado !== "ING")
								countEstadoInvalidoParaEdicion++;
						}
						;

						// No se puede editar movimientos anulados, bloqueados,
						// girados, liquidados
						if (countEstadoInvalidoParaEdicion > 0) {
							bcsComunes
									.ventanaMsgErr(
											$translate
													.instant("UI_SELECCIONAR_COMPROBANTES_VALIDOS_PARA_EDICION"),
											$scope.winID);
							return;
						}
						;

						if (j > 0) {
							bcsComunes
									.ventanaMsgErr(
											$translate
													.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS"),
											$scope.winID);
							return;
						}
						;

						var datos = [];
						datos.push({
							"Entidad" : 'TES.Comprobante',
							"showListaCuentasSocioNegocio" : $scope.showListaCuentasSocioNegocio,
							"IdPersona"					   : $scope.seleccionComprobantes[0].IdPersona,
							"IdMoneda"					   : $scope.seleccionComprobantes[0].IdMoneda,							
						});
						datos.push(data);

						var edicionMultiple = bcsComunes.ventanaAbrir(
								"EdicionMultipleTesoreria", datos, false,
								false, $scope.winID);
						var promesa = bcsComunes
								.ventanaPromesa(edicionMultiple);
						promesa.then(function(data) {
							if (data === 'true') {
								$scope.buscarComprobantes();
							}
						});
					};

					$scope.ParametrosServicioBloquearDesbloquear = function(
							accion) {
						var dataCols = [];
						$.each($scope.seleccionComprobantes, function(index,
								value) {
							dataCols.push({
								Id : value.IdComprobante,
								Entidad : 'TES.Comprobante',
								Accion : accion
							});
						});

						bcsComunes.ventanaOverlay($scope.winID, true);
						var datos = {};

						datos.p_IdEmpresa = $scope.SesionIdEmpresa;
						datos.p_IdUsuario = $scope.SesionIdUsuario;
						datos.p_IdModulo = 'TES';
						datos.p_UsaReglasNegocio = 'S';
						datos.p_FechaMovimiento = util.dateSPdate(util
								.dateInicioDia($scope.SesionFechaSistema));
						datos.p_Entidad = 'TES.Comprobante';
						datos.p_FormasDePago = [];
						datos.p_RegistrosAProcesar = dataCols;

						return datos;
					};
					$scope.bloquearDesbloquearIngresoEgreso = function() {
						if(!bcsComunes.validarAcceso("ConsultarComprobantesTesoreria_btn_BloquearDesbloquear")){return;}
						bcsComunes.ventanaOverlay($scope.winID, true);

						var opcion = '';
						$scope.seleccionComprobantes[0].IdEstado === 'BLO' ? (opcion = 'DES')
								: (opcion = 'BLO');

						var datos = $scope
								.ParametrosServicioBloquearDesbloquear(opcion);
						if (datos != undefined) {

							bcsServiciosTesoreria
									.EdicionMultiple(
											datos,
											function(respuesta) {
												bcsComunes.ventanaOverlay(
														$scope.winID, false);
												bcsComunes
														.ventanaMsg(
																{
																	conflictos : util
																			.parseServerResponse(respuesta)
																}, $scope.winID);

												$scope.buscarComprobantes();
											},
											function(respuesta) {
												bcsComunes
														.ventanaMsg(
																{
																	mensaje : $translate
																			.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
																	conflictos : util
																			.parseServerResponse(respuesta)
																}, $scope.winID);
												bcsComunes.ventanaOverlay(
														$scope.winID, false);
											});
						}
					};
					$scope.seleccionarDeseleccionarTodosMovimientos = function() {
						// seleccionar
						$scope.ApiComprobantes
								.select($scope.ApiComprobantes.tbody
										.find(">tr"));

						// deseleccionar
						if (!!$scope.seleccionComprobantes
								&& $scope.seleccionComprobantes.length > 0) {
							$scope.ApiComprobantes.clearSelection();
						}
					};

					var error = function(respuesta) {
						bcsComunes.msg("error", "error", $translate
								.instant('UI_PROCESO_REALIZADO_CON_ERROR'));
						bcsComunes.setConflictos(util
								.parseServerResponse(respuesta));
						bcsComunes.ventanaOverlay($scope.winID, false);
					};

					$scope.detalleComprobante = function(datos) {
						bcsComunes.ventanaAbrir("DetalleComprobanteTesoreria",
								{
									IdComprobante : datos[0].IdComprobante
								}, false, false);
					};

					
					
					$scope.$watch('seleccionComprobantes',function(newval,oldval)
					{
						if ( _.isUndefined(newval) ) return;
						
						var countComprobantesInvalidos = 0;
						for (i=0;i<newval.length;i++)
						{	
							if (newval[i].IdEstado !== "ING") countComprobantesInvalidos++;
						}; 
								
						if (newval.length > 0)
							if (countComprobantesInvalidos == newval.length)			
								$scope.btnActivo = false;  //Si todos los comprobantes seleccionados son inválidos(ANU/BLO/LIQ/GIR), se desactivan los botones
							else
								$scope.btnActivo = true;   //Si uno de los comprobantes es válido, se activan los botones
						else
							$scope.btnActivo = false;
					});

					
					// -----------------------------------------------------------
					// Obtiene configuracion para reportes
					// -----------------------------------------------------------
					var consultarTemplateTesoreria = new bcs.DataSource( 
							{
								type : "odata", // Los tipos posibles son:
												// odata, call, array y object
								definition : {
									serverFiltering : true, // Filtrado de datos
															// en el servidor.
									transport : {
										read : {
											url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",
											dataType : "json",
										}
									},

									filter : [ {
										field : "CodigoTemplateTesoreria",
										operator : "eq",
										value : 'DECOTE'
									}, {
										field : "IdEstado",
										operator : "eq",
										value : 'ACT'
									}, {
										field : "IdEmpresa",
										operator : "eq",
										value : $scope.SesionIdEmpresa
									}, ],
									schema : {
										model : {
											fields : {
												IdReporte : {
													type : "string"
												},
												IdServicio : {
													type : "number"
												},
												RutaServicio : {
													type : "string"
												}, // FALTA borrar
												NombreReporte : {
													type : "string"
												},
												FormatoSalida : {
													type : "string"
												},
											}
										}
									}

								},
							});

					consultarTemplateTesoreria
							.fetch()
							.then(
									function() {
										if (consultarTemplateTesoreria.data().length > 0) {
											$scope
													.$apply(function() {
														$
																.each(
																		consultarTemplateTesoreria
																				.data(),
																		function(
																				key,
																				registro) {
																			$scope.ReporteDetalleComprobante = {
																				IdReporte : registro.IdReporte,
																				NombreReporte : registro.NombreReporte,
																				IdServicio : registro.IdServicio,
																				FormatoSalida : registro.FormatoSalida,
																				RutaServicio : registro.RutaServicio
																			// FALTA
																			// borrar
																			};

																		});
														return true;
													});
										} else {
											return false;
										}
									});

					$scope.imprimirDetalleComprobante = function(
							seleccionComprobantes) {

						if ($scope.ReporteDetalleComprobante.IdReporte == ''
								|| $scope.ReporteDetalleComprobante.NombreReporte == ''
								|| $scope.ReporteDetalleComprobante.IdServicio == ''
								|| $scope.ReporteDetalleComprobante.IdServicio == 0
								|| $scope.ReporteDetalleComprobante.FormatoSalida == '') {
							bcsComunes.ventanaOverlay($scope.winID, false);
							bcsComunes.msg("error", 'Error', $translate
									.instant("UI_REPORTE_SIN_CONFIGURACION"));
							return false;
						}

						if (seleccionComprobantes.length > 0) {
							bcsComunes.ventanaOverlay($scope.winID, true);
							// Recorre las filas seleccionadas
							for (var i = 0; i < seleccionComprobantes.length; i++) {
								var fecha = new Date();

								// Genera el nombre del reporte para descarga
								// local
								var nombreArchivoDescarga = $scope.ReporteDetalleComprobante.NombreReporte
										+ "_Nro_"
										+ String(seleccionComprobantes[i].IdComprobante)
										+ "_"
										+ String(fecha.getFullYear())
										+ String(fecha.getMonth() + 1)
										+ String(fecha.getDate())
										+ "_"
										+ String(fecha.getHours())
										+ String(fecha.getMinutes())
										+ String(fecha.getSeconds())
										+ "."
										+ $scope.ReporteDetalleComprobante.FormatoSalida;

								var parameters = {

									"reportId" : $scope.ReporteDetalleComprobante.IdReporte,
									"exportFormat" : $scope.ReporteDetalleComprobante.FormatoSalida,
									"parameters" : "",

									"configuration" : {
										"mainReport" : {
											"data" : {
												"serviceId" : $scope.ReporteDetalleComprobante.IdServicio,
												"serviceData" : {
													"P_IDEMPRESA" : Number($scope.SesionIdEmpresa),
													"P_IDCOMPROBANTE" : Number(seleccionComprobantes[i].IdComprobante),
													"$schema" : [ {
														"field" : "LogoCorredora",
														"type" : "blob"
													} ]
												},
											},
											"parameters" : {},
											"reportName" : $scope.ReporteDetalleComprobante.NombreReporte
										},
										"subReports" : []
									},
								};

								$http
										.post(
												'/BSCL/Plataforma/GWJAVA/PLA/report?file',
												parameters,
												{
													"responseType" : "arraybuffer"
												})
										.success(
												function(response) {
													var file = new Blob(
															[ response ],
															{
																type : 'application/pdf'
															});
													saveAs(file,
															nombreArchivoDescarga);
													bcsComunes
															.ventanaOverlay(
																	$scope.winID,
																	false);
													bcsComunes
															.msg(
																	"info",
																	"info",
																	$translate
																			.instant("UI_PDF_COMPROBANTE_GENERADO"));
												})
										.error(
												function(response) {
													bcsComunes
															.msg(
																	"error",
																	'Error',
																	$translate
																			.instant("UI_ERROR_AL_GENERAR_REPORTE"));
													bcsComunes
															.ventanaOverlay(
																	$scope.winID,
																	false);
												});
							} // Fin recorre las filas seleccionadas

						} else {
							bcsComunes.msg("warning", "warning", $translate
									.instant('UI_SELECCIONAR_MOVIMIENTO'));
						}

						return;
					};

					// funcion que llama a la carga de archivos
					$scope.cargarArchivoTesoreria = function() {
						if(!bcsComunes.validarAcceso("ConsultarComprobantesTesoreria_btn_CertificacionMasiva")){return;}
						console.log("Entro");
						var datosArchivo = {codigoArchivo : 'CARGA_LIQUIDAR_TES'};
						var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true,true);
						var promesa = bcsComunes.ventanaPromesa(id1);
						promesa.then(function(data) {
							if (!data || !data.fileData || !data.fileData[0]){
								return;
							}
							var error = 0;
							var dataCols = []
							var dataBanco = []
						for(data of data.fileData){
							if(data.MENSAJEVALIDACION !== "OK"){
								error =  1;
							}else{
									dataCols.push({
												IdComprobante : data.Factura,
												IdTipoMovimiento : '',
												AfectaCuentaInversion : 'COR' 

											});
									dataBanco.push({
										NombreCuentaBancaria : data.Banco,

									});
							}
						};

						var parametros = {};
						parametros.p_IdEmpresa = $scope.SesionIdEmpresa,
						parametros.p_IdUsuario = $scope.SesionIdUsuario,
						parametros.p_IdModulo  = 'TES',
						parametros.p_SimularOperacion = "N",
						parametros.p_UsaReglasNegocio = 'S',
						parametros.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
						parametros.p_ComprobanteLiquidar = dataCols,
						parametros.p_ComprobanteLiquidar = dataBanco,
						parametros.p_FormaDePagoLiquidar = [],
						 	
						
						console.log('DATOS: ' + JSON.stringify(parametros));
						
							if (error != 0){
								bcsComunes.ventanaMsg({
						            mensaje:$translate.instant("Debe cargar nuevamente el archivo, Con el Formato correcto"),
					//		            conflictos:util.parseServerResponse()
					                },$scope.idVentana);	
							
							}else{
								bcsComunes.ventanaOverlay($scope.idVentana,true);
								bcsServiciosTesoreria.LiquidacionMasivaComprobante(parametros).$promise.then(function(result){
									console.log('RESULT: ' + JSON.stringify(result));
									bcsComunes.ventanaOverlay($scope.idVentana,false);
										if (result.P_OUT_ERRORESCONTADOR > 0) {
										bcsComunes.ventanaMsg({
									            mensaje:$translate.instant("Error: Debe cargar nuevamente el archivo, error en el procesamiento del archivo"),
									            conflictos:util.parseServerResponse(result)
								                },$scope.idVentana);
											bcsComunes.ventanaAbrir('VentanaResultado',{ 
												usaNuevosMensajes : true ,
												titulo: $translate.instant('Procesamiento Archivo'), 
												campoSecuencia : "id" ,
												nombreSecuencia : $translate.instant('UI_SECUENCIA'),
												datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
							     			bcsComunes.ventanaOverlay($scope.idVentana,false);
//							     			bcsComunes.ventanaCerrar($scope.idVentana);
											console.log(result);
									}else{		
										
								
										var Liquidar = {};
										 Liquidar.p_IdEmpresa = $scope.SesionIdEmpresa,
										 Liquidar.p_IdUsuario = $scope.SesionIdUsuario,
										 Liquidar.p_IdModulo  = 'TES',
										 Liquidar.p_SimularOperacion = 'S',
										 Liquidar.p_UsaReglasNegocio = 'S',
										 Liquidar.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
										 Liquidar.p_ComprobanteLiquidar = result.P_OUT_COMPROBANTELIQUIDAR,
										 Liquidar.p_FormaDePagoLiquidar = result.P_OUT_FORMADEPAGO,
										
							
							bcsComunes.ventanaOverlay($scope.idVentana,true);
							bcsServiciosTesoreria.LiquidarComprobantes(Liquidar).$promise.then(function(resultadoSimulacion){					
								console.log('RESULT: ' + JSON.stringify(resultadoSimulacion));
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								if (resultadoSimulacion.P_OUT_ERRORESCONTADOR > 0) {
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("No se puede liquidar ni certificar los comprobantes"),
										conflictos:util.parseServerResponse(resultadoSimulacion)
										},$scope.idVentana);
									bcsComunes.ventanaAbrir('VentanaResultado',{ 
										usaNuevosMensajes : true ,
										titulo: $translate.instant('Simular Liquidaion'), 
										campoSecuencia : "id" ,
										nombreSecuencia : $translate.instant('UI_SECUENCIA'),
										datos : resultadoSimulacion.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									//bcsComunes.ventanaCerrar($scope.idVentana);
									console.log(resultadoSimulacion);
									}else{
										
										var datosCertificacion = {
												p_IdEmpresa: $scope.SesionIdEmpresa,
												p_IdUsuario: $scope.SesionIdUsuario,
												p_IdModulo: "CUS",
												P_MOVIMIENTOSCERTIFICAR: result.P_OUT_MOVIMIENTOSCERTIFICAR,
												p_FechaOperacion: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),	
										};
										
										bcsComunes.ventanaOverlay($scope.idVentana,true);
										bcsServiciosTesoreria.CertificarMovimientos(datosCertificacion).$promise.then(function(resultado){					
											console.log('RESULT: ' + JSON.stringify(resultado));
											bcsComunes.ventanaOverlay($scope.idVentana,false);
											if (resultado.P_OUT_ERRORESCONTADOR > 0) {
												bcsComunes.ventanaMsg({
													mensaje:$translate.instant("Error: Debe cargar nuevamente el archivo, error en el procesamiento del archivo"),
													conflictos:util.parseServerResponse(resultado)
													},$scope.idVentana);
												bcsComunes.ventanaAbrir('VentanaResultado',{ 
													usaNuevosMensajes : true ,
													titulo: $translate.instant('Certificar Movimientos'), 
													campoSecuencia : "id" ,
													nombreSecuencia : $translate.instant('UI_SECUENCIA'),
													datos : resultado.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
												bcsComunes.ventanaOverlay($scope.idVentana,false);
												//bcsComunes.ventanaCerrar($scope.idVentana);
												console.log(resultado);
											}
											else {

												bcsComunes.ventanaOverlay($scope.idVentana,true);
												bcsComunes.ventanaMsg({
													mensaje: "Comprobantes Certificados",
													conflictos: util.parseServerResponse(resultado)
												},$scope.idVentana);
												

												bcsComunes.ventanaCerrar($scope.idVentana);
												console.log(resultado);
										
												
												var Liquidar = {};
														 Liquidar.p_IdEmpresa = $scope.SesionIdEmpresa,
														 Liquidar.p_IdUsuario = $scope.SesionIdUsuario,
														 Liquidar.p_IdModulo  = 'TES',
														 Liquidar.p_SimularOperacion = "N",
														 Liquidar.p_UsaReglasNegocio = 'S',
														 Liquidar.p_FechaMovimiento = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
														 Liquidar.p_ComprobanteLiquidar = result.P_OUT_COMPROBANTELIQUIDAR,
														 Liquidar.p_FormaDePagoLiquidar = result.P_OUT_FORMADEPAGO,
														
											
											bcsComunes.ventanaOverlay($scope.idVentana,true);
											bcsServiciosTesoreria.LiquidarComprobantes(Liquidar).$promise.then(function(resultadoFinal){					
												console.log('RESULT: ' + JSON.stringify(resultadoFinal));
												bcsComunes.ventanaOverlay($scope.idVentana,false);
												if (resultado.P_OUT_ERRORESCONTADOR > 0) {
													bcsComunes.ventanaMsg({
														mensaje:$translate.instant("Error: Debe cargar nuevamente el archivo, error en el procesamiento del archivo"),
														conflictos:util.parseServerResponse(resultadoFinal)
														},$scope.idVentana);
													bcsComunes.ventanaAbrir('VentanaResultado',{ 
														usaNuevosMensajes : true ,
														titulo: $translate.instant('Liquidar Comprobantes'), 
														campoSecuencia : "id" ,
														nombreSecuencia : $translate.instant('UI_SECUENCIA'),
														datos : resultado.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
													bcsComunes.ventanaOverlay($scope.idVentana,false);
													//bcsComunes.ventanaCerrar($scope.idVentana);
													console.log(resultado);
													}else{
														bcsComunes.ventanaMsg({
															mensaje:$translate.instant("Proceso realizado"),
															conflictos:util.parseServerResponse(resultadoFinal)
															},$scope.idVentana);
														
														bcsComunes.ventanaAbrir('VentanaResultado',{ 
															usaNuevosMensajes : true ,
															titulo: $translate.instant('Comprobantes Liquidados'), 
															campoSecuencia : "id" ,
															nombreSecuencia : $translate.instant('UI_SECUENCIA'),
															datos : resultado.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
														bcsComunes.ventanaOverlay($scope.idVentana,false);

													}
											},function(resultadoFinal){
												bcsComunes.ventanaOverlay($scope.idVentana,true);
												bcsComunes.ventanaMsg({
													mensaje: "Error al cargar la informacion",
													conflictos: util.parseServerResponse(resultadoFinal)
												},$scope.idVentana)
												console.log(resultadoFinal);
													bcsComunes.ventanaAbrir('VentanaResultado',{ 
														usaNuevosMensajes : true ,
														titulo: $translate.instant('Liquidadar Coprobantes'), 
														campoSecuencia : "id" ,
														nombreSecuencia : $translate.instant('UI_SECUENCIA'),
														datos : resultadoFinal.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
													bcsComunes.ventanaOverlay($scope.idVentana,false);
													//bcsComunes.ventanaCerrar($scope.idVentana);
													console.log(resultadoFinal);
											});
																			
										}
											},function(resultado){
												bcsComunes.ventanaOverlay($scope.idVentana,true);
												bcsComunes.ventanaMsg({
													mensaje: "Error al cargar la informacion",
													conflictos: util.parseServerResponse(resultado)
												},$scope.idVentana)
												console.log(resultado);

														bcsComunes.ventanaAbrir('VentanaResultado',{ 
															usaNuevosMensajes : true ,
															titulo: $translate.instant('Certificar Comprobante'), 
															campoSecuencia : "id" ,
															nombreSecuencia : $translate.instant('UI_SECUENCIA'),
															datos : resultado.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
														bcsComunes.ventanaOverlay($scope.idVentana,false);
														//bcsComunes.ventanaCerrar($scope.idVentana);
														console.log(resultado);
												
											});
										}
//										
							},function(resultadoSimilacion){
								bcsComunes.ventanaOverlay($scope.idVentana,true);
								bcsComunes.ventanaMsg({
									mensaje: "Error al cargar la informacion",
									conflictos: util.parseServerResponse(resultadoSimilacion)
								},$scope.idVentana)
								console.log(resultadoSimilacion);

								bcsComunes.ventanaAbrir('VentanaResultado',{ 
											usaNuevosMensajes : true ,
											titulo: $translate.instant('Formato Incorrecto'), 
											campoSecuencia : "id" ,
											nombreSecuencia : $translate.instant('UI_SECUENCIA'),
											datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
										bcsComunes.ventanaOverlay($scope.idVentana,false);
										//bcsComunes.ventanaCerrar($scope.idVentana);
										console.log(resultadoSimilacion);
							});
									}
								
								},function(result){
									bcsComunes.ventanaOverlay($scope.idVentana,true);
									bcsComunes.ventanaMsg({
										mensaje: "Error al cargar la informacion",
										conflictos: util.parseServerResponse(result)
									},$scope.idVentana)
									console.log(result);

									bcsComunes.ventanaAbrir('VentanaResultado',{ 
												usaNuevosMensajes : true ,
												titulo: $translate.instant('Formato Incorrecto'), 
												campoSecuencia : "id" ,
												nombreSecuencia : $translate.instant('UI_SECUENCIA'),
												datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
											bcsComunes.ventanaOverlay($scope.idVentana,false);
											//bcsComunes.ventanaCerrar($scope.idVentana);
											console.log(result);
								});
								}
						
								
							});

							
		};
		$scope.LiquidacionComprobantesTodos= function(){
			var filters = {};
			
			if ($scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento && $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 0){
				filters.IdTipoMovimiento =$scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento;
			} else {filters.IdTipoMovimiento= -1 ;}
			filters.IdPersona =Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona || -1);
			filters.IdPortafolioTitularPago =Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || -1)	;
			filters.IdCaja =$scope.oConfiguracionCaja.seleccionado.IdCaja || -1 ;
			filters.IdMoneda =$scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda || -1 ;						
			filters.IdEstado= $scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado  || -1 ;
			if ($scope.oConfiguracionConcepto.seleccionado.IdConcepto && $scope.oConfiguracionConcepto.seleccionado.IdConcepto != 0){
				filters.IdConcepto= $scope.oConfiguracionConcepto.seleccionado.IdConcepto;
			}else {filters.IdConcepto= -1;}						
			filters.FechaLiquidacionDesde= util.dateOdata($scope.kngFechaLiquidacionDesde) || -1 ;
			filters.FechaLiquidacionHasta= util.dateTerminoDia($scope.kngFechaLiquidacionHasta)  || -1;
			filters.IdTipoPersona =$scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio  || -1;
			filters.IdMedioDePago =$scope.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || -1 ;
			filters.NombreBancoSocio=$scope.cmbbxCuentaBancariaSocio.seleccionado.NombreBanco || -1;
			filters.NombreBancoCorredora=$scope.cmbbxCuentaBancariaCorredora.seleccionado.NombreBanco || -1;
			if ($scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreCuentaBancaria !== ' TODOS') {
									filters.AliasBanco= $scope.oConfiguracionCuentaBancoCorredora.seleccionado.NombreCuentaBancaria || -1 ;
									}
			if ($scope.oConfiguracionNumeroCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria !== ' TODOS') {
				filters.NumeroCuentaBancariaCorredora=$scope.oConfiguracionNumeroCuentaBancoCorredora.seleccionado.NumeroCuentaBancaria || -1 ;
			}
			filters.IdClasificacionTipoInstrumento= $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || -1 ;	
			filters.IdTipoInstrumento= $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || -1 ;	
		
			if ($scope.kngFechaComprobanteOrigen && $scope.kngFechaComprobanteOrigen != '0001-01-01')
				filters.FechaComprobanteOrigen=$scope.kngFechaComprobanteOrigen
						;


			var t = bcsAlerta.crear(	"PREGUNTA",$translate.instant('UI_SE_LIQUIDARAN_TODOS_COMPROBANTES_FILTRADOS'));
			t.result.then(function(result) {
				if (result === OPCIONES_BCSALERTA.ACEPTAR) {

							bcsComunes.ventanaOverlay($scope.winID,	true);

							var datos = {};
							datos.p_IdEmpresa = $scope.SesionIdEmpresa;
							datos.p_IdUsuario = $scope.SesionIdUsuario;
							datos.p_IdModulo = 'TES';
							datos.p_UsaReglasNegocio = 'S';
							datos.p_FechaMovimiento = util.dateSPdate(util
											.dateInicioDia($scope.SesionFechaSistema));
							datos.p_Filtros = filters;
							datos.p_FormaDePagoLiquidar = []; 

					bcsServiciosTesoreria.LiquidarTodosComprobantes(datos,function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID,	false);
										
						if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			            	bcsComunes.ventanaMsg({
		   		        	 mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
				        	conflictos : util.parseServerResponse(respuesta)
				         	},
				        	$scope.winID);
				        	$scope.buscarComprobantes();
				      	} else {	
							var PreMernsaje=bcsComunes.ventanaAbrir("ModalVentanaMensajeResultado",{
								mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos : util.parseServerResponse(respuesta)
								} , false, false);
								var promesa = bcsComunes.ventanaPromesa(PreMernsaje);							
								promesa.then(function(resultado){ 
								if (resultado === OPCIONES_BCSALERTA.ACEPTAR){ 
									}	
								}) 
							}
					    });
					}													        
					}
				);
			return;
		}
		$scope.$on('$viewContentLoaded', function() {
			$timeout(function() {
			  var iconoConfiguracion = angular.element('bcs-grid#grid-comprobantes-tesoreria > span> div.bcsgridconf');
			  iconoConfiguracion.css('display', 'block');
			});
		  });
		
		function limpiarSeleccionComprobantes() {
			$scope.seleccionComprobantes = [];
		};
		
		function cerrarventanasPorNombre (winKey){
			$("[winkey='" + winKey + "']").each(function() {
				bcsComunes.ventanaCerrar($(this).attr('ventanaid'));
			});
		};
});
