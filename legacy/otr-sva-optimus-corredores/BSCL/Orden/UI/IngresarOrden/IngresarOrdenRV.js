angular.module('ModuloOrdenes').controller('IngresarOrdenRVCtrl', function($q,$resource,$scope,OPCIONES_BCSALERTA,bcsServicioReportes,bcsServiciosPlataformaMercado,$translate,bcsComunes,bcsConfiguracionOrdenesV2,bcsServicioOrden,bcsServicioPlataforma,bcsAlerta,$timeout,bcsConfiguracionOrdenes,bcsConsultasOrdenes){
	
	var ordenRV = this;
	var controllerName = 'IngresarOrdenRVCtrl';
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);

	var PortafolioClienteRutea;
	var dataPlantillaEjecutivo = [];
	var ProductosRutean;
	var IdPortafolioCargado = "";

	var datosConsultarNemoNegociacion = {};
	//PARA CONTROLAR INGRESO DE ORDENES NORMALES Y OFERTAS PUBLICas
	ordenRV.modoVentana = 'NORMAL';	//NORMAL, OFERTAPUBLICA
	ordenRV.SW = 'N';
	ordenRV.MonedaSistema = $scope.SesionIdMonedaSistema;
	ordenRV.datosOrden = {};
	
	
	ordenRV.datosResumen = {};
	ordenRV.habilitarBoton = true;
	ordenRV.PermiteRuteo = false;
	ordenRV.datosOrden.EsVentaCorta=false;
	ordenRV.datosOrden.GeneraSpot=false;
	$scope.kOptFechaMax = {};
	$scope.kOptFechaMax.max = new Date($scope.SesionFechaSistema);
	$scope.kOptFechaMax.max.setDate($scope.kOptFechaMax.max.getDate()+30);
	$scope.kOptFechaMax.min = new Date($scope.SesionFechaSistema);
	$scope.kOptFechaMax.min.setDate($scope.kOptFechaMax.min.getDate()+1);
	$scope.CalculoPrecioPunta = BCS_CONFIGURACION.ORDENES["CalculoPrecioPunta"] === 'S' ? true : false;
	$scope.ActivaGeneraSpot = BCS_CONFIGURACION.ORDENES["ActivaGeneraSpot"] === 'S' ? true : false;	
	//$scope.ParametroPrecioMercado = BCS_CONFIGURACION.ORDENES["ParametroPrecioMercado"] === 'S' ? true : false;
	
	$scope.$watchGroup(['ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio','ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion'],function(newval,oldval){
			
		
		
		if (ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenRV.datosOrden.operacionSeleccionada && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion) {
			
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoOperacion	: ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion,
					P_IdPortafolio		: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
			}
			
			bcsServicioOrden.ConsultarRequisitosBasicosCrearOrdenes(datos).$promise.then(function(result){
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(result)
					},idVentana);
				}
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' 
						&& ordenRV.IdBovedaCorresponsal !== undefined 
							&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			
			ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
	});
	$scope.$watchGroup(['ordenRV.datosForm.AccionOrden','ordenRV.datosOrden.instrumento', 'ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion'],function(){
		if(ordenRV.datosForm.AccionOrden === 'COMPRA' && ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdClasificacionTipoInstrumento === 'RV' && ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion === 'USD' && ordenRV.datosOrden.pagoCobroCuentaInversion){
				ordenRV.datosForm.ActivaGeneraSpot= $scope.ActivaGeneraSpot;
		}else {
				ordenRV.datosForm.ActivaGeneraSpot=false;
				ordenRV.datosOrden.GeneraSpot=false;
		}

	});
	$scope.$watchGroup(['ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenRV.datosForm.AccionOrden',
		'ordenRV.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden',
		'ordenRV.datosOrden.EsVentaCorta'],function(){
		if (ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio &&
				ordenRV.datosForm.AccionOrden === 'VENTA' &&
				ordenRV.datosOrden.formaCustodiaSeleccionada && ordenRV.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden === 'CUS' &&
				ordenRV.datosOrden.EsVentaCorta === false
				) {
			ordenRV.datosForm.validaFlujoCustodia = 'S';
		}
		else { //ordenRV.datosForm.AccionOrden
			ordenRV.datosForm.validaFlujoCustodia = 'N';
		}
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenRV.IdBovedaCorresponsal !== undefined 
						&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
	});
	
	//watch exigibilidad y cobertura (futuros y venta corta)
	$scope.$watchGroup([
	            		'ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
	            		'ordenRV.datosOrden.EsVentaCorta',
	            		'ordenRV.datosOrden.instrumento.IdInstrumento',
	            		'ordenRV.datosOrden.bolsaSeleccionada.IdBolsa',
	            		'ordenRV.datosOrden.montoPlazo',
	            		'ordenRV.datosOrden.montoLiquidacionCalculado',
						'ordenRV.datosOrden.GeneraSpot',
	            	],function(newval,oldval){
	            		
	            		var operacionesExigibilidad = [];
	            		var operacionesCobertura    = [];
	            		
	            		if (ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdTipoInstrumento
	            			&& ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
	            				&& ordenRV.datosOrden.bolsaSeleccionada && ordenRV.datosOrden.bolsaSeleccionada.IdBolsa) {
	            			
	            			operacionesExigibilidad.push({
	            				EsVentaCorta		: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.EsVentaCorta:false,
								GeneraSpot                        : ordenRV.datosOrden.GeneraSpot?ordenRV.datosOrden.GeneraSpot:false,
	            				IdTipoInstrumento	: ordenRV.datosOrden.instrumento.IdTipoInstrumento || 0,
	            				IdBolsa				: ordenRV.datosOrden.bolsaSeleccionada.IdBolsa,
	            				IdPortafolio		: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
	            				IdInstrumento		: ordenRV.datosOrden.instrumento.IdInstrumento || 0,
	            				IdInstrumentoExige	: ordenRV.datosOrden.instrumento.IdInstrumento || 0,
	            				Cantidad			: 0,
	            				Precio				: 0,
	            				Monto 				: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.montoPlazo || 0:ordenRV.datosOrden.montoLiquidacionCalculado || 0,	            				
	            			});
	            			
	            		}
            			
	            		
            			if (ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
            					&& ordenRV.datosOrden.bolsaSeleccionada && ordenRV.datosOrden.bolsaSeleccionada.IdBolsa
            					&& ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento && ordenRV.datosOrden.EsVentaCorta) {
            				
            				var i = ordenRV.datosOrden.instrumento || {};           				
	            				

            				operacionesCobertura.push({
	        					EsVentaCorta		: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.EsVentaCorta:false,
	        					IdBolsa				: ordenRV.datosOrden.bolsaSeleccionada.IdBolsa,
	        					IdPortafolio		: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
	        					IdInstrumento		: 0,
	        					IdInstrumentoExige	: i.IdInstrumento,
	        					IdTipoInstrumentoExige	: i.IdTipoInstrumento, 
	        					Cantidad			:  0,
	        					Precio				: ordenRV.datosOrden.precio || 0,
	        					Monto 				: 0,
	        					MonedaDestino		: ordenRV.datosForm.operacionSalida.length!==0? ordenRV.datosForm.operacionSalida[0].idMonedaLiquidacionOrden :""
	        				});
	        				
	        			}
	            		
            			/*OPTIMUSPE-1215*/
            			if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
            					&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' 
            						&& ordenRV.IdBovedaCorresponsal !== undefined 
            							&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
            				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
            			/*OPTIMUSPE-1215*/
	            			
            			if (operacionesExigibilidad.length > 0) 
	            			ordenRV.datosForm.operacionesExigibilidad = operacionesExigibilidad;	    
            			
            			if (operacionesCobertura.length > 0) 
            				ordenRV.datosForm.operacionesCobertura = operacionesCobertura;
	            		
	            	});
	
	//watch nemotecnico negociacion
	$scope.$watchGroup([
		'ordenRV.datosOrden.instrumento.IdInstrumento',
		'ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion',
		'ordenRV.datosOrden.monedaNegociacion.IdMoneda'], function(newval,oldval){
		if (ordenRV.datosOrden.condicionLiquidacionSeleccionada &&
				ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion &&
				ordenRV.datosOrden.monedaNegociacion &&
				ordenRV.datosOrden.monedaNegociacion.IdMoneda &&
				ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento) {
			
			if (datosConsultarNemoNegociacion.p_DatosInstrumentoConsulta && ordenRV.datosForm.InstrumentoNegociacion.NemoInstrumentoNegociacion != "" ) {
				
				if (datosConsultarNemoNegociacion.p_DatosInstrumentoConsulta[0].IdInstrumentoRaiz == ordenRV.datosOrden.instrumento.IdInstrumento &&
					datosConsultarNemoNegociacion.p_DatosInstrumentoConsulta[0].IdCondLiquidacion == ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion &&
					datosConsultarNemoNegociacion.p_DatosInstrumentoConsulta[0].IdMoneda == ordenRV.datosOrden.monedaNegociacion.IdMoneda) {
					return
				}
			}
				
				 datosConsultarNemoNegociacion = {p_DatosInstrumentoConsulta: [{
						IdInstrumentoRaiz                :        ordenRV.datosOrden.instrumento.IdInstrumento,
						IdCondLiquidacion                :        ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
						IdMoneda                         :        ordenRV.datosOrden.monedaNegociacion.IdMoneda
				}]
				};
				
				bcsServicioOrden.ConsultarNemoNegociacion(datosConsultarNemoNegociacion).$promise.then(function(result){
					if (!result.P_InstrumentoSalida[0]){
							return;
					}else {
							
							ordenRV.datosForm.InstrumentoNegociacion.NemoInstrumentoNegociacion = result.P_InstrumentoSalida[0].Nemotecnico;
							ordenRV.datosForm.InstrumentoNegociacion.IdInstrumentoNegociacion = result.P_InstrumentoSalida[0].IdInstrumento;
							ordenRV.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = ordenRV.datosOrden.monedaNegociacion.IdMoneda;
							ordenRV.datosForm.InstrumentoNegociacion.IdInstrumentoRaiz = result.P_InstrumentoSalida[0].IdInstrumento;
					};
				});                                
		}
});
	//Watch Rut Interno
	$scope.$watchGroup(['ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio','ordenRV.datosOrden.bolsaSeleccionada.IdBolsa'], function(newval,oldval) {
		ordenRV.habilitarBoton = true;
		if($scope.SesionLocale === 'PE') {
			if (newval[0] !== undefined) {
				if (newval[1] !== undefined && newval[1] !== 'FRD') {
					var esRutInterno = ordenRV.datosOrden.portafolioSeleccionado.EsRutInterno;
					if (esRutInterno === 'S') {
						ordenRV.habilitarBoton = false;
						bcsComunes.ventanaMsg({
							conflictos : {
								erroresFatales: [],
			    				errores: [{
			   						Codigo : "",
			   						Descripcion : "Cliente con Rut Interno, seleccione un Mercado OTC."
			   					}],
			   					advertencias: [],
			    				autorizaciones: [],
			    				informaciones: [],
								erroresFatalesContador: 0,
								erroresContador: 1,
								autorizacionesContador: 0,
								advertenciasContador: 0,
								informacionesContador: 0
							}
						},idVentana);
					}
				}
			}
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenRV.IdBovedaCorresponsal !== undefined 
							&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
		}
	});

	//watch linea de riesgo
	$scope.$watchGroup([
		'ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenRV.datosOrden.EsVentaCorta',
		'ordenRV.datosOrden.instrumento.IdInstrumento',
		'ordenRV.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenRV.datosOrden.tasaVentaCorta',
		'ordenRV.datosOrden.montoPlazo',
		'ordenRV.datosOrden.plazoVentaCorta',
		'ordenRV.datosOrden.montoLiquidacionCalculado'
	],function(newval,oldval){
		
		var operaciones = [];
		if (ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento 
				&& ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
				&& ordenRV.datosOrden.operacionSeleccionada && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion
				&& ordenRV.datosOrden.bolsaSeleccionada && ordenRV.datosOrden.bolsaSeleccionada.IdBolsa) {
			
			operaciones.push({
				IdPortafolio		: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoOperacion		: ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion,
				EsVentaCorta		: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.EsVentaCorta:false,
				EsODCuentaPropia	: false,
				IdInstrumento		: ordenRV.datosOrden.instrumento.IdInstrumento || 0,
				Monto 				: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.montoPlazo || 0:ordenRV.datosOrden.montoLiquidacionCalculado || 0,
				Tasa				: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.tasaVentaCorta || 0:0,
				PlazoEnDias			: ordenRV.datosOrden.EsVentaCorta?ordenRV.datosOrden.plazoVentaCorta || 0:0,
				IdBolsa				: ordenRV.datosOrden.bolsaSeleccionada.IdBolsa,
				
			});
			
			ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		if (operaciones.length > 0) 
			ordenRV.datosForm.operacionesLineaRiesgo = operaciones;
		
		/*OPTIMUSPE-1215*/
		if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
				&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' 
					&& ordenRV.IdBovedaCorresponsal !== undefined 
						&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
			ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
		/*OPTIMUSPE-1215*/
	});
	
	var ConsultarEmpleadoUI = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarEmpleadoUI'),
					dataType : "json"
				}
			},
			schema 	: {
				model	: {
					fields	: {
						NombreCompleto		: { type: "string"},
						IdEmpleado			: { type: "number"},
						IdPlantillaEmpleado	: { type: "string"}
					}
				}
			}
		}	
	});
	var ConsultarDetallePlantillaEmpleado = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarDetallePlantillaEmpleado'),
					dataType : "json"
				}
			},
			schema 	: {
				model	: {
					fields	: {
						IdPlantillaEmpleado	: { type: "string"}
					}
				}
			}
		}	
	});

	var ConsultarDetallePerfilProductos = new bcs.DataSource(
		{
				type : 'odata',
				definition : {
					serverFiltering : true,
					transport : {
						read : {
							url :bcsConsultasOrdenes('ConsultarDetallePerfilProductos'),
							dataType : 'json'
						}
					},
					schema: {
						model: {
							fields: {
								IdPerfilProductos : { type: "string"}
							}
						}
					}
				}
	});

	var initDatosAgente = function initDatosAgente(){
		var ConsultarEmpleado = new bcs.DataSource(ConsultarEmpleadoUI);
			var filterTemplate = [ { field : "IdEmpleado", operator : "eq", value : $scope.SesionIdEmpleado} ];

			ConsultarEmpleado.query({ filter: filterTemplate }).then(function(){			
				if(ConsultarEmpleado.data().length > 0 && ConsultarEmpleado.data()[0] != undefined){		

					var Consultar = new bcs.DataSource(ConsultarDetallePlantillaEmpleado);
					var filter = [  { field: "IdPlantillaEmpleado", operator: "eq", value : ConsultarEmpleado.data()[0].IdPlantillaEmpleado}, 
									{ field: "IdEstado", 			operator: "eq", value: "ACT" }];
					
					Consultar.query({ filter: filter }).then(function(){	
						if(Consultar.data().length > 0 && Consultar.data()[0] != undefined){		
							Consultar.data().forEach(element => {
								dataPlantillaEjecutivo.push(element);
							});
						}
					});
				}
			});
	}

	var DatosRuteo = function DatosRuteo(){
		ProductosRutean = [];
		PortafolioClienteRutea="";
			
		//indicador Portafolio Cliente
		var parametrosSP = {
			p_IdEmpresa				: $scope.$root.SesionIdEmpresa,
			p_CodigoTipoIndicador	: 'ORDCLIRUR',
			p_GrupoIndicador		: 'GRUPOORDENES',
			p_IdIndicador			: 0,
			p_ModeloCliente			: '',
			p_IdTipoEntidad			: 'PORTAFOLIO',
			p_IdEntidad				: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio
		};
		bcsServicioOrden.consultaIndicadoresSP(parametrosSP).$promise.then(function(result){ 
			PortafolioClienteRutea = result.P_OUT_INDICADORES[0].ValorIndicador;
			IdPortafolioCargado = parametrosSP.p_IdEntidad;
		});

		//Detalle Perfil de productos
		var DetallePerfilProducto = new bcs.DataSource(ConsultarDetallePerfilProductos);
		var DetallePerfilProductosextra = new bcs.DataSource(ConsultarDetallePerfilProductos);
		var filter1 = [ { field: "IdPerfilProductos", 	operator: "eq", value: ordenRV.datosOrden.portafolioSeleccionado.IdPerfilProductos },
						{ field: "PermiteRuteo", 		operator: "eq", value: "S" },
						{ field: "EstadoProducto", 	operator: "eq", value: "ACT" }
						];
		var filter2 = [ { field: "IdPerfilProductos", 	operator: "eq", value: ordenRV.datosOrden.portafolioSeleccionado.IdPerfilProductosExtra }, 
						{ field: "PermiteRuteo", 		operator: "eq", value: "S" },
						{ field: "EstadoProducto", 	operator: "eq", value: "ACT" } 
					];

		DetallePerfilProducto.query({filter:filter1}).then(function(){
			if(DetallePerfilProducto.data().length > 0 && DetallePerfilProducto.data()[0] != undefined){		
				DetallePerfilProducto.data().forEach(element => {
					if (element.IdCanalIngreso == "0" || element.IdCanalIngreso == "AGE") {
						ProductosRutean.push(element);	
					}
				});
			}
		});
		if (ordenRV.datosOrden.portafolioSeleccionado.IdPerfilProductosExtra != "") {
			DetallePerfilProductosextra.query({filter:filter2}).then(function(){
				if(DetallePerfilProductosextra.data().length > 0 && DetallePerfilProductosextra.data()[0] != undefined){		
					DetallePerfilProductosextra.data().forEach(element => {
						if (element.IdCanalIngreso == "0" || element.IdCanalIngreso == "AGE") {
							ProductosRutean.push(element);	
						}
					});
				}
			});
		}
		
	}

	var PermitirRuteo = function PermitirRuteo() {
		if (PortafolioClienteRutea == "S") {
			if (dataPlantillaEjecutivo!=undefined && ProductosRutean!=undefined && ProductosRutean!=[] && 
				ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio &&
				ordenRV.datosOrden.operacionSeleccionada && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion &&
				ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento
				) {
				const enPlantillaEjecutivo = dataPlantillaEjecutivo.filter(objeto => objeto.IdTipoProducto.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto)
																					&& (objeto.IdTipoOperacion.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion)
																						|| objeto.IdTipoOperacion == "0"));
				const enProductosRutean = ProductosRutean.filter(objeto => objeto.IdTipoProducto.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto)
																			&& (objeto.IdTipoOperacion.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion)
																				|| objeto.IdTipoOperacion == "0"));

				const noRuteaOperacionEjecutivo	= dataPlantillaEjecutivo.filter(objeto 	=> objeto.IdTipoProducto.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto)
																							&& (objeto.IdTipoOperacion.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion)
																								|| objeto.IdTipoOperacion == "0")
																								&& objeto.PermiteRuteo == "N");															
				const noRuteaOperacionCliete = ProductosRutean.filter(objeto => objeto.IdTipoProducto.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto)
																				&& (objeto.IdTipoOperacion.includes(ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion)
																					|| objeto.IdTipoOperacion == "0")
																					&& objeto.PermiteRuteo == "N");																		
				if (enPlantillaEjecutivo.length>0 && enProductosRutean.length>0 
					&&(noRuteaOperacionEjecutivo.length == 0 && noRuteaOperacionCliete.length == 0)) {
						bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CanalDistribucionManualDefecto', $scope.SesionIdEmpresa, 'ORDENES')
						.then(function (result) {
							ordenRV.PermiteRuteo = true;
							if(result.Valor ==='S'){
								ordenRV.datosOrden.idTipoCanalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
								ordenRV.datosOrden.canalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
							}else{
								ordenRV.datosOrden.idTipoCanalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionAgente;
								ordenRV.datosOrden.canalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionAgente;
							}
							
						});
						
						
					}else{
					ordenRV.PermiteRuteo = false;
					ordenRV.datosOrden.idTipoCanalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
					ordenRV.datosOrden.canalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
				}
			}
		}else{
			ordenRV.PermiteRuteo = false;
			ordenRV.datosOrden.idTipoCanalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
			ordenRV.datosOrden.canalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
		}
	}

	$scope.$watchGroup([
		'ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden'
	],function(newval,oldval){
		if (newval == "M") {
			ordenRV.datosOrden.idTipoCanalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
			ordenRV.datosOrden.canalDistribucion = bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionManual;
			ordenRV.PermiteRuteo = false;
		}
		if (ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.instrumento) {
			if(newval == "L"){
				PermitirRuteo();
			}
		}
	});
	$scope.$watchGroup([
		'ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenRV.datosOrden.instrumento.IdTipoInstrumento'], function (newval, oldval) {
		if(newval != oldval){
			ordenRV.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
		}
		if (ordenRV.datosOrden.portafolioSeleccionado) {
			if (newval[0] != IdPortafolioCargado) {
				DatosRuteo();
			}
		}
	});


	ordenRV.datosForm = {
		InstrumentoNegociacion :{
			NemoInstrumentoNegociacion : '',
			IdInstrumentoNegociacion : 0
		},
		vencimientoUsaFechaLiquidacion: false,
		fechaInicial :util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL"),
		decimalesCantidad : 0,
		pensando 			: false,
		calculandoAlgo		: false,
		decimalesPrecio : 0,
		decimalesTasaVentaCorta : 0,
		decimalesMonto : 0,
		decimalesTipoCambio : 0,
		valorAllin : undefined,			
		idVentana : idVentana,
		modo : 'INGRESO',
		muestraCobros : false,
		idUso: "ORDEN",
		configuracionSocioNegocio : {
			filtros: {
				IdEmpresa 		: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				EsCliente 		: {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
			},
		},
		configuracionCompraVenta : {
			defecto : "COMPRA"
		},
		configuracionPortafolio : {
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
			}
		},
		configuracionLibroOrdenes : { 
			filtros : {
				IdEmpresa			: { field:"IdEmpresa", operator:"eq", value: $scope.SesionIdEmpresa },
				EsOfertaPublica		: { field:"EsLibroOfertaPublica", operator:"eq", value: "S" },
				IdInstrumento		: { field:"IdInstrumento", operator:"eq", value: "" },
			}
		},
		configuracionAutorizadoDarOrdenes : {
			filtros: {
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  -1 },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   :  "ACT" }
			},
		},
		configuracionMecanismoCaptacion : {
			filtros: {
				CodigoTipoIndicador : { "field" : "CodigoTipoIndicador","operator": "eq", "value" : "ORDMODALIDAD" },
				IdEntidad : { "field" : "IdEntidad", "operator" : "eq", "value": "" }
			},
			itemPostFiltro : '1'
		},
		configuracionCorredorCorresponsal : {
			filtros : ($scope.SesionLocale === 'PE') ? {
				IdEmpresa	: { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
			} : {
				IdPais		: { "field"   : "IdPais","operator": "eq", "value"   : "" },
				IdEmpresa	: { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
			}
		},configuracionBoveda : {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			}
		},
		configuracionFormaCustodia : {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			}
		},
		configuracionCuentaCustodio : {
			filtros : ($scope.SesionLocale === 'PE')?{
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
				IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
			}: 	
			{
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
				IdEstadoAutorizacion : { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				//IdModeloCustodia : { "field"   : "IdModeloCustodia","operator": "eq", "value"   : "" }
			}
		},
		configuracionPreferenciaPagoPortafolio : {
			filtros : {
				IdMedioDePago : { "field"   : "IdMedioDePago",operator: "neq", "value"   :  "CIN" },
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "" },
				IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   :  "" },
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "" }
			}
		},
		configuracionTipoPrecio : {
			defecto : bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado,
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
		},
		configuracionTipoVigencia : {
			defecto : bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy,
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
		},
		configuracionPorcentajeVisibilidad : {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
		},
		configuracionCondicionLiquidacion : { 
			filtros  : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "" },
				IdInstrumento: { "field"   : "IdInstrumento","operator": "eq", "value"   : "" }
			}
		},
		comboMonedaLiquidacionOrden : { 
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMonedaInstrumento : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			},
			itemPostFiltro : '1'				
		},
		comboMonedaOrden : {
			filtros : {
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
					IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : "" }
			},
			itemPostFiltro : '1'                                
		},
		configuracionCanalDistribucion : {
			filtros : {
				IdTipoInstrumento : { field : "IdTipoInstrumento", operator : "eq", value : ""},
				IdBolsa : { field : "IdBolsa", operator : "eq", value : ""},
				IdTipoCanalIngreso : { field : "IdTipoCanalIngreso", operator : "eq", value : $scope.SesionTipoCanalIngreso},
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
			}
		},
		configuracionBolsa : {
			defecto : $scope.SesionBolsaDefectoSistema,
			filtros : {
				IdInstrumento : { "field"   : "IdInstrumento", "operator": "eq", "value"   : 0 },
				
				
			}
		},
		configuracionTipoLiquidacion : {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			}
		},
		verComprobante : function(){
			bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,
					$scope.ordenRV.datosResumen.documentoIdentidad,
					$scope.ordenRV.datosResumen.fechaOperacionOrden,
					$scope.ordenRV.datosResumen.secuenciaOrden,
					$scope.ordenRV.datosResumen.montoOperacion,
					idVentana);
		},
		verDetalle : function() {
			bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : ordenRV.datosResumen.secuenciaOrden} );
		}
	};

	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
			.then(function (parametroUsaFechaLiquidacion) {
				ordenRV.datosForm.vencimientoUsaFechaLiquidacion = parametroUsaFechaLiquidacion.Valor === 'S';
			});

	if ($scope.SesionLocale === 'PE') {
		ordenRV.datosForm.configuracionBoveda.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value"   :  "" };
	}
	
	bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
		if ($scope.SesionLocale === 'CL'){
			if (bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto) {
				ordenRV.datosForm.configuracionBoveda.defecto = bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto;
			}
		};
		
		if (bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoOrdenRV) {
			ordenRV.datosForm.configuracionTipoVigencia.defecto = bcsConfiguracionOrdenes.CONFIGURACION.VigenciaDefectoOrdenRV;
		}
		
		if (BCS_CONFIGURACION.PRODUCTO) {
			let paramCobroComisionMensual = BCS_CONFIGURACION.PRODUCTO["PRO.CobroComisionesMensuales"];
			ordenRV.datosForm.configuracionTipoLiquidacion.visible = paramCobroComisionMensual === "ACTIVO" ? true : false;
		}
	});
	
	/*OPTIMUSPE-1215*/
	var defCorresponsalBoveda = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			serverPaging 	: true,
			serverSorting 	: true,
			schema 	: {
				model	: {
                    fields	: {
                    	IdCorredorCorresponsal	: { type: "number"},
                    	IdTipoProducto			: { type: "string"},
                    	NombreTipoProducto		: { type: "string"},
                    	IdBoveda				: { type: "string"}
                    }
           		}
			},
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarProductoBovedaCorredorCorresponsal'),
					dataType : "json"
				}
			}
		}	
	});
	
	//watch operacion cobros
	$scope.$watchGroup(['ordenRV.datosOrden.tipoCambio',
		'ordenRV.datosOrden.precio',
		'ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenRV.datosOrden.bolsaSeleccionada.IdBolsa',
		'ordenRV.datosOrden.montoInicial',
		'ordenRV.datosOrden.cantidadInicial',
		'ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion',
		'ordenRV.datosOrden.idCorredorCorresponsal',
		'ordenRV.datosOrden.corredorCorresponsalSeleccionada'], function (newval, oldval) {

	
		
		if (ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion !== undefined &&
				ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento !== undefined &&
				ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio !== undefined &&
				ordenRV.datosOrden.operacionSeleccionada && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion !== undefined &&
				ordenRV.datosOrden.bolsaSeleccionada && ordenRV.datosOrden.bolsaSeleccionada.IdBolsa !== undefined &&
				ordenRV.datosOrden.precio &&
				(ordenRV.datosOrden.cantidadInicial || ordenRV.datosOrden.montoInicial)
		) {
			
			var operacion = [{
				//IdInstrumento  				: ordenRV.datosOrden.instrumento.IdInstrumento,
				IdInstrumento                : ordenRV.datosForm.InstrumentoNegociacion.IdInstrumentoNegociacion,
				FechaOperacion 				: $scope.SesionFechaSistema,
				CantidadOrden   			: ordenRV.datosOrden.cantidadInicial || 0,
				PrecioOrden   				: ordenRV.datosOrden.precio || 0,
				MontoTotal					: ordenRV.datosOrden.montoInicial || 0,
				IdMonedaLiquidacionOrden	: ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,

				IdPortafolio 				: ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
				IdTipoProducto 				: ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto,
				IdTipoOperacion 			: ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion,
				IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
				IdBolsa 					: ordenRV.datosOrden.bolsaSeleccionada.IdBolsa,
				IdCorredorCorresponsal		: ordenRV.datosOrden.idCorredorCorresponsal,
				TipoCambio					: ordenRV.datosOrden.tipoCambio

			}];
			ordenRV.datosForm.operaciones = operacion;
			
			ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
		/* OPTIMUSPE-1215 - Boveda por defecto cuando es Bolsa Extranjera*/	
		if($scope.SesionLocale === 'PE'){
			if(ordenRV.datosOrden.bolsaSeleccionada != undefined && ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' &&
					ordenRV.datosOrden.operacionSeleccionada !== undefined && ordenRV.datosOrden.idCorredorCorresponsal !== undefined &&
						ordenRV.datosOrden.corredorCorresponsalSeleccionada !== undefined){
				var ConsultarCorresponsalBoveda = new bcs.DataSource(defCorresponsalBoveda);
				var filtro = Array();
				filtro.push({ field : "IdCorredorCorresponsal", operator : "eq", value : ordenRV.datosOrden.idCorredorCorresponsal == false ? null : ordenRV.datosOrden.idCorredorCorresponsal});
				filtro.push({ field : "IdTipoProducto", operator : "eq", value : ordenRV.datosOrden.operacionSeleccionada.IdTipoProducto});
				ConsultarCorresponsalBoveda.query({filter: filtro}).then(function(){
					if(ConsultarCorresponsalBoveda.data() != undefined && ConsultarCorresponsalBoveda.data().length > 0 && ConsultarCorresponsalBoveda.data()[0] != undefined){					
						ordenRV.IdBovedaCorresponsal = ConsultarCorresponsalBoveda.data()[0].IdBoveda == null ? "" : ConsultarCorresponsalBoveda.data()[0].IdBoveda;
						ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda = { "field"   : "IdBoveda","operator": "eq", "value" : ordenRV.IdBovedaCorresponsal };
					}else{
						ordenRV.IdBovedaCorresponsal = "";
					}
				});	
			}	
		}
		/*OPTIMUSPE-1215*/
	});	
	//},true);
	
	
	ordenRV.datosForm.cleanData = function() {
		ordenRV.SW = 'N';		
		ordenRV.datosForm.ingresoRapido = undefined;
		ordenRV.datosForm.operacionSalida = [];
		ordenRV.datosForm.operaciones = [];
		ordenRV.datosForm.AccionOrdenDefecto = "COMPRA";
		ordenRV.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
		ordenRV.datosForm.setInstrumento = "-1";
		ordenRV.datosForm.configuracionPortafolio.idActual = false;
		
		ordenRV.datosResumen = {
				fechaOperacionOrden : '',
				secuenciaOrden : '',
				tipoInstrumento : '',
				operacion : '',
				cantidad : '',
				instrumento : '',
				precio : '',
				tipoPrecio : '',
				montoOperacion : '',
				totalCobros : '',
				montoTotal :'',
				documentoIdentidad : '',
				nombreEjecutivoPortafolio : ''
		};
		
		ordenRV.datosOrden.tasaVentaCorta 			= undefined;
		ordenRV.datosOrden.precio 					= undefined;
		ordenRV.datosOrden.montoInicial 			= undefined;
		ordenRV.datosOrden.tipoCambio 				= undefined;
		ordenRV.datosOrden.fechaRecepcion 			= undefined;
		ordenRV.datosOrden.cantidadInicial 			= undefined;
		ordenRV.datosOrden.plazoVentaCorta 			= undefined;
		ordenRV.datosOrden.fechaVencimiento 		= undefined;
		ordenRV.datosOrden.montoPlazo 				= undefined;
		ordenRV.datosOrden.EsVentaCorta 			= false;
		ordenRV.datosOrden.montoOperacionCalculado 	= 0;
		ordenRV.datosOrden.cantidadCalculada 		= 0;
		ordenRV.datosOrden.totalCobros 				= 0;
		ordenRV.datosOrden.totalOrden 				= 0;
		ordenRV.datosOrden.EsVentaCorta 			= false;
		ordenRV.datosOrden.GeneraSpot               = false;
		
		if (ordenRV.modoVentana != 'NORMAL') {
			ordenRV.modoVentana = 'NORMAL';
			ordenRV.datosForm.configuracionLibroOrdenes.filtros.IdInstrumento.value = "";
			ordenRV.datosForm.configuracionLibroOrdenes.idPostFiltro = "";
			ordenRV.datosForm.configuracionCompraVenta.activado = true;
			ordenRV.datosForm.instrumentoDeshabilitado = false;
		}
		
		//limpia campos faltantes
		ordenRV.datosForm.IdCondicionDeLiquidacion = "T2";
		ordenRV.datosForm.configuracionTipoVigencia.IdActual = ordenRV.datosForm.configuracionTipoVigencia.defecto;
		ordenRV.datosOrden.fechaLiquidacion = undefined;
		ordenRV.datosForm.AccionOrden = "COMPRA";
		ordenRV.datosForm.ingresoRapidoTemp = undefined;
		ordenRV.datosForm.InstrumentoNegociacion.NemoInstrumentoNegociacion = "-1";
		ordenRV.datosForm.InstrumentoNegociacion.IdInstrumentoNegociacion = 0;
		ordenRV.datosOrden.monedaNegociacion = undefined;
		ordenRV.datosForm.comboMonedaLiquidacionOrden.filtros.IdMonedaInstrumento.value = '';
	};
	
	ordenRV.datosForm.volver = function() {
		ordenRV.datosForm.modo = 'INGRESO';
		ordenRV.datosForm.cleanData();
	};
	


	$scope.$watch('ordenRV.datosOrden.idBolsaSeleccionada',function(newval,oldval) {
   		if (ordenRV.datosOrden.idBolsaSeleccionada) {
   			if ((ordenRV.datosOrden.bolsaSeleccionada.EsExtranjera === 'N' || ordenRV.datosOrden.bolsaSeleccionada.UsaBovedaLocal ==='S') &&  ($scope.SesionLocale === 'PE')) {
   				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "LOCAL";
   			}
   			else if (ordenRV.datosOrden.bolsaSeleccionada.EsExtranjera === 'S'  &&  ($scope.SesionLocale === 'PE')) {
   				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdModeloCustodia.value = "EXTERIOR";
   				/*OPTIMUSPE-1215*/
   				if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
   						&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT' 
   							&& ordenRV.IdBovedaCorresponsal !== undefined 
   								&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
   					ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
   				/*OPTIMUSPE-1215*/
   			}
   			
   		}
   	});
   	


	ordenRV.datosForm.ingresarOrden = function(a) {
		
		var cobrosModificados = [];
		
		$.each(ordenRV.datosOrden.cobrosModificados,function(key,value) {
			cobrosModificados.push({ IdInterno: 0 ,IdCobro : key, ValorCobroAplicado : value  });
		});
		
		var datosIngresoOrden = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'ORD',
				p_simularOperacion : 'N',
				p_UsaReglasNegocio : 'S',
				
				p_Ordenes : [{
					IdInterno :0,
				    FechaRecepcion : (ordenRV.datosOrden.fechaRecepcion)? util.dateSPseconddate(ordenRV.datosOrden.fechaRecepcion) : '0001-01-01 00:00:00',
					FechaVigencia  : (ordenRV.datosOrden.fechaVigencia)? util.dateSPdate(ordenRV.datosOrden.fechaVigencia) :  '0001-01-01',
					FechaEmision : '0001-01-01',
					FechaOperacion : '0001-01-01', //se calcula en el server
					IdCondicionDeLiquidacion : ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,
					IdTipoOperacion : ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion,
					IdTipoVigencia : ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden,
					Glosa : ordenRV.datosOrden.comentario || '',
					Cantidad : ordenRV.datosForm.operacionSalida[0].cantidadCalculada,
					MontoNeto : 0,
					ValorRescate : 0,
					IdBoveda 		: ordenRV.datosOrden.bovedaSeleccionada.IdBoveda,
					IdFormaCustodia : ordenRV.datosOrden.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
					IdPorcentajeVisibilidad : ordenRV.datosOrden.porcentajeVisibilidadSeleccionada.IdPorcentajeVisibilidad,
					PrecioLimite : Number(ordenRV.datosOrden.precio),
					PrecioMercado : Number(ordenRV.datosOrden.instrumento.PrecioMercado),
					EsFacturaInmediata : 'N',
					IdTipoPrecio : ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden,
					IdPortafolio : ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
					IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					IdTipoReajuste : '',
					IdEmisor: ordenRV.datosOrden.instrumento.IdEmisor,
					IdBolsa : ordenRV.datosOrden.bolsaSeleccionada.IdBolsa,
					EspecificaBolsa : ordenRV.datosOrden.bolsaSeleccionada.IdBolsa === 0? 'N':'S',
					IdMonedaLiquidacionOrden : ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					IdMonedaLiquidacionCobros : ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion,
					TipoCambio : ordenRV.datosOrden.tipoCambio || null,
					IdInstrumento : ordenRV.datosOrden.instrumento.IdInstrumento,
					IdInstrumentoSubyacente : 0,
					IdCondicionDeLiquidacionPlazo : '',
					TasaVentaCorta : (ordenRV.datosOrden.EsVentaCorta)? ordenRV.datosOrden.tasaVentaCorta:0,
					PlazoVentaCorta : (ordenRV.datosOrden.EsVentaCorta)? ordenRV.datosOrden.plazoVentaCorta:0,
					EsVentaCorta : ordenRV.datosOrden.EsVentaCorta? 'S': 'N',
					TasaSimultanea : 0,
					PlazoSimultanea : 0,
					PlazoIF : 0,
					TasaRFIF : 0,
					EsOD: 'N',
					EsFungible : 'N',
					MontoPlazo : 0,
					IdTipoLibroOrden : $scope.SesionLocale === 'PE' && ordenRV.modoVentana === 'OFERTAPUBLICA' ? ordenRV.datosForm.configuracionLibroOrdenes.seleccionado.IdTipoLibroOrden : 'NORMAL',
					IdLibroOrdenes : $scope.SesionLocale === 'PE' && ordenRV.modoVentana === 'OFERTAPUBLICA' ? ordenRV.datosForm.configuracionLibroOrdenes.seleccionado.IdLibroOrdenes : null,
					IdCuentaCustodioPortafolio : ordenRV.datosOrden.cuentaCustodioSeleccionada? ordenRV.datosOrden.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null:null,
					EsPorMonto : ordenRV.datosOrden.montoInicial !== undefined && ordenRV.datosOrden.montoInicial > 0?"S":"N",
					IdCorredorCorresponsal : ordenRV.datosOrden.idCorredorCorresponsal || null,
					IdPersonaAutorizadoDarOrdenes : ordenRV.datosOrden.autorizadoDarOrdenes.IdPersona || null,
					IdTipoCanalDistribucion : 	ordenRV.datosOrden.idTipoCanalDistribucion || null,
					EsOrdenEspecial : ordenRV.datosOrden.esOrdenEspecial === true?"S":"N",
					PagoCobroCuentaInversion : ordenRV.datosOrden.pagoCobroCuentaInversion === true?"S":"N",
					IdPreferenciaPagoPortafolio : ordenRV.datosOrden.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
					BaseTasa : 0,
					TipoInteres : "",
					EsClienteFinal : ordenRV.datosOrden.esParaClienteFinal === true?"S":"N",
					IdMecanismoCaptacion : ordenRV.datosOrden.mecanismoDeCaptacion?ordenRV.datosOrden.mecanismoDeCaptacion.ValorIndicador:'',
					FechaConfirmacionPlazo : '0001-01-01', //se calcula en el server
					TipoLiquidacionFactura : ordenRV.datosForm.configuracionTipoLiquidacion.visible ? ordenRV.datosForm.configuracionTipoLiquidacion.idActual : '',
					IngresoDesde : "COR",
					GeneraSpot : ordenRV.datosOrden.GeneraSpot? 'S': 'N',
					NemoInstrumentoNegociacion : ordenRV.datosForm.InstrumentoNegociacion.NemoInstrumentoNegociacion,
					IdInstrumentoNegociacion : ordenRV.datosForm.InstrumentoNegociacion.IdInstrumentoNegociacion,
					IdMonedaOrden : (ordenRV.datosOrden.monedaNegociacion.IdMoneda)?ordenRV.datosOrden.monedaNegociacion.IdMoneda:''
				}],
				p_CobrosModificados : cobrosModificados
		};
		
		if ($scope.SesionLocale === 'PE' && ordenRV.datosOrden.cuentaCustodioSeleccionada) {
			datosIngresoOrden.p_Ordenes[0].IdCuentaCustodioPortafolio = ordenRV.datosOrden.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio || null;
		}

		bcsComunes.ventanaOverlay(idVentana,true);
		var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
		promise.then(function(respuesta){
   			
   			bcsComunes.ventanaOverlay(idVentana,false);
	   		
			//se valido la orden
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{
				ordenRV.datosForm.modo = 'RESUMEN';
				ordenRV.datosResumen = {
						fechaOperacionOrden :	util.dateDS(respuesta.P_OUT_RESULTADO[0].FechaOperacionOrden,"FechaHora","CALL"),
						secuenciaOrden		:	respuesta.P_OUT_RESULTADO[0].IdOrden,
						tipoInstrumento 	: 	respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						operacion 			:	respuesta.P_OUT_RESULTADO[0].AccionOrden,
						tipoInstrumento_operacion  : respuesta.P_OUT_RESULTADO[0].AccionOrden + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoInstrumento,
						cantidad 			:	respuesta.P_OUT_RESULTADO[0].Cantidad,
						instrumento 		: 	respuesta.P_OUT_RESULTADO[0].Instrumento,
						precio 				: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden),
						esVentaCorta		: 	respuesta.P_OUT_RESULTADO[0].EsVentaCorta === 'S'?true:false,
						tipoPrecio 			: 	respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
						precio_tipoPrecio	: 	Number(respuesta.P_OUT_RESULTADO[0].PrecioOrden) + ' ' + respuesta.P_OUT_RESULTADO[0].NombreTipoPrecio,
						montoOperacion 		: 	respuesta.P_OUT_RESULTADO[0].MontoLiquidacionOperacion,
						totalCobros 		: 	Number(respuesta.P_OUT_RESULTADO[0].TotalCobros),
						montoTotal 			:	Number(respuesta.P_OUT_RESULTADO[0].MontoTotal),
						idMoneda 			:	respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,
						idMonedaOrden		:	respuesta.P_OUT_RESULTADO[0].IdMonedaOrden,
						documentoIdentidad	:	respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						nombreEjecutivoPortafolio : respuesta.P_OUT_RESULTADO[0].NombreEjecutivoPortafolio
				};
				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				
				ordenRV.datosResumen.decimales = util.bcs_formato(0,'GEN','Monto',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenRV.datosResumen.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
				ordenRV.datosResumen.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',respuesta.P_OUT_RESULTADO[0].IdMonedaLiquidacionOrden,respuesta.P_OUT_RESULTADO[0].IdTipoInstrumento,'').cantidadDecimales;
			}
			else
		    {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
		    }
			
   		},function(respuesta){
   			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
   		});
   	};
	
   	$scope.$watchGroup(['ordenRV.datosOrden.pagoCobroCuentaInversion','ordenRV.datosOrden.GeneraSpot'],function(){
	
	 if(ordenRV.datosOrden.pagoCobroCuentaInversion===true && (ordenRV.datosOrden.GeneraSpot===true || ordenRV.datosOrden.GeneraSpot===false)){
				ordenRV.datosForm.IdPreferenciaPagoPortafolio = false;
			 ordenRV.datosForm.IdPreferenciaPagoPortafolio = false;
	 }
	 else if(ordenRV.datosOrden.pagoCobroCuentaInversion===false && ordenRV.datosOrden.GeneraSpot===true){
			 ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdMedioDePago.operator='eq';
			 ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdMedioDePago.value='TRA';
	 }
	 else if(ordenRV.datosOrden.pagoCobroCuentaInversion===false && ordenRV.datosOrden.GeneraSpot===false){
			 ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdMedioDePago.operator='neq';
			 ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdMedioDePago.value='CIN';
	 }                
	});

	$scope.$watch('ordenRV.datosOrden.instrumento.IdTipoInstrumento',function(newval,oldval) {
   		if (!newval) {
			ordenRV.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = undefined;
			ordenRV.datosForm.comboMonedaOrden.filtros.IdInstrumento.value = undefined;
        	ordenRV.datosForm.comboMonedaOrden.filtros.IdTipoInstrumento.value = undefined;
   		}
   		else {	
			ordenRV.datosForm.configuracionCanalDistribucion.filtros.IdTipoInstrumento.value = newval;
			ordenRV.datosForm.configuracionBolsa.defecto = ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion === "T2" ? 0 :ordenRV.datosOrden.instrumento.IdBolsa;
			$scope.ordenRV.datosOrden.bolsaActivada = ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion === "T2" && ordenRV.datosForm.configuracionBolsa.defecto == 0 ? false : true;
			ordenRV.datosForm.comboMonedaOrden.filtros.IdInstrumento.value = ordenRV.datosOrden.instrumento.IdInstrumento;
        	ordenRV.datosForm.comboMonedaOrden.filtros.IdTipoInstrumento.value = newval;
		}
	});
	
	$scope.$watch('ordenRV.datosForm.ingresoRapido',function(newval,oldval) {
		//NUEVO FORMATO {C/V}{ESPACIO}{CANTIDAD}{NEMO}{SIGNO PESO}{PRECIO}
		//C 5000PLANVITAL$1000
		if (!newval) {
			ordenRV.SW = 'N';
			return;
		}

		var entrada=newval;
		var parms=entrada.split(" ");
		if (parms.length != 2) return;
		if (parms[0].toUpperCase() !== 'C' && parms[0].toUpperCase() !== 'V') return;
		
		var indexNemo = (parms[1].match(/[a-zA-Z]/) || []).index;
		if (indexNemo === undefined) return;
		var indexPrecio = parms[1].indexOf('$');
		
		if (parms[0] == "C") { ordenRV.datosForm.AccionOrden = 'COMPRA';} 
		if (parms[0] == "V") { ordenRV.datosForm.AccionOrden = 'VENTA';} 
		
		ordenRV.datosForm.setInstrumento = parms[1].substring(indexNemo,indexPrecio!=-1?indexPrecio:parms[1].length);
		ordenRV.datosOrden.cantidadInicial=Number(parms[1].substring(0,indexNemo));
		if (indexPrecio!=-1) {
			ordenRV.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioLimite;
			ordenRV.datosOrden.precio = Number(parms[1].substring(indexPrecio +1));
		}
		else {
			ordenRV.datosForm.idTipoPrecio = bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado;
			
		}	
		
		});
	
	$scope.$watchGroup(['ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden','ordenRV.datosOrden.instrumento.Precio','ordenRV.datosOrden.InstrumentoNegociacion.Precio'],function(newval,oldval) {
		if (ordenRV.datosOrden.instrumento && ordenRV.datosOrden.tipoPrecioSeleccionado) {
			if (ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioLimite) {
				ordenRV.datosOrden.IdMoneda = ordenRV.datosOrden.InstrumentoNegociacion.IdMoneda;
			}

			if (ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoPrecioMercado) {
				
				if (ordenRV.datosOrden.InstrumentoNegociacion) {
					ordenRV.datosOrden.IdMoneda = ordenRV.datosOrden.InstrumentoNegociacion.IdMoneda;				
					ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.InstrumentoNegociacion.Precio);				
					if ($scope.CalculoPrecioPunta === true && ordenRV.datosOrden.InstrumentoNegociacion.IdClasificacionTipoInstrumento === "RV" && ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === "M"){ // $scope.ParametroPrecioMercado === true && 
						if (ordenRV.datosForm.AccionOrden==='COMPRA' &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta != null &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta > ordenRV.datosOrden.InstrumentoNegociacion.PrecioMercado){
							ordenRV.datosOrden.precio= ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta;						
						}
						else
						if (ordenRV.datosForm.AccionOrden==='VENTA' &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra != null &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra < ordenRV.datosOrden.InstrumentoNegociacion.PrecioMercado){
							ordenRV.datosOrden.precio= ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra;
						}					
					}
					else				
					ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.InstrumentoNegociacion.Precio);

				}else{
					ordenRV.datosOrden.IdMoneda = ordenRV.datosOrden.instrumento.IdMoneda;
					ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.instrumento.Precio);				
					if ($scope.CalculoPrecioPunta === true && ordenRV.datosOrden.instrumento.IdClasificacionTipoInstrumento === "RV" && ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === "M"){ // $scope.ParametroPrecioMercado === true && 
						if (ordenRV.datosForm.AccionOrden==='COMPRA' &&  ordenRV.datosOrden.instrumento.PuntaVenta != null &&  ordenRV.datosOrden.instrumento.PuntaVenta > ordenRV.datosOrden.instrumento.PrecioMercado){
							ordenRV.datosOrden.precio= ordenRV.datosOrden.instrumento.PuntaVenta;						
						}
						else
						if (ordenRV.datosForm.AccionOrden==='VENTA' &&  ordenRV.datosOrden.instrumento.PuntaCompra != null &&  ordenRV.datosOrden.instrumento.PuntaCompra < ordenRV.datosOrden.instrumento.PrecioMercado){
							ordenRV.datosOrden.precio= ordenRV.datosOrden.instrumento.PuntaCompra;
						}					
					}
					else				
					ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.instrumento.Precio);
				}
			}
		}
	});
	
	$scope.$watch('ordenRV.datosForm.calculandoAlgo',function(newval,oldval) {
		ordenRV.datosForm.pensando = newval;
	});
	
	$scope.$watchGroup(['ordenRV.datosOrden.tasaVentaCorta','ordenRV.datosOrden.plazoVentaCorta','ordenRV.datosForm.operacionSalida[0].montoOperacionCalculado'],function(newval,oldval){
		if (ordenRV.datosOrden.tasaVentaCorta && ordenRV.datosOrden.plazoVentaCorta && ordenRV.datosForm.operacionSalida[0] && ordenRV.datosForm.operacionSalida[0].montoOperacionCalculado)
			ordenRV.datosOrden.montoPlazo = bcsComunes.calcularMontoPlazo(Number(ordenRV.datosForm.operacionSalida[0].montoOperacionCalculado),Number(ordenRV.datosOrden.plazoVentaCorta),Number(ordenRV.datosOrden.tasaVentaCorta));
		else ordenRV.datosOrden.montoPlazo = undefined;
	});
	
	
	//Validad indicador Opera custodia RV
	$scope.$watch('ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval) {

		if(ordenRV.datosOrden.portafolioSeleccionado){
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				var indicadores = result[0];
				if(indicadores.OperaCustodiaRV === "S"){
					ordenRV.datosForm.configuracionFormaCustodia.idActual = "CUS";
				}else{
					ordenRV.datosForm.configuracionFormaCustodia.idActual = "NOM";
				}
				if(indicadores.ModificaTipoLiquidacion === "MODPREFNORM"){
					ordenRV.datosForm.configuracionTipoLiquidacion.idActual = "DIR"; 
					ordenRV.datosOrden.ModificaTipoLiquidacion = false;
				}else{
					ordenRV.datosForm.configuracionTipoLiquidacion.idActual = "MEN";
					ordenRV.datosOrden.ModificaTipoLiquidacion = true;
				}
			});
		}
	});
	
	

	$scope.$watch('ordenRV.datosOrden.instrumento.IdInstrumento',function(newval,oldval) {
		if (ordenRV.datosOrden.instrumento && ordenRV.datosOrden.instrumento.IdInstrumento) {
			ordenRV.datosForm.decimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',ordenRV.datosOrden.instrumento.IdMoneda,ordenRV.datosOrden.instrumento.IdTipoInstrumento,'').cantidadDecimales;
			ordenRV.datosForm.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',ordenRV.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenRV.datosForm.decimalesTasaVentaCorta = util.bcs_formato(0,'GEN','Tasa',ordenRV.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenRV.datosForm.decimalesMonto = util.bcs_formato(0,'GEN','Monto',ordenRV.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;
			ordenRV.datosForm.decimalesTipoCambio = util.bcs_formato(0,'GEN','TipoCambio',ordenRV.datosOrden.instrumento.IdMoneda,'','').cantidadDecimales;			
			ordenRV.datosForm.configuracionBolsa.filtros.IdInstrumento.value = ordenRV.datosOrden.instrumento.IdInstrumento;
			ordenRV.datosForm.comboMonedaOrden.filtros.IdInstrumento.value = ordenRV.datosOrden.instrumento.IdInstrumento;
			ordenRV.datosForm.comboMonedaLiquidacionOrden.filtros.IdTipoInstrumento.value = ordenRV.datosOrden.instrumento.IdTipoInstrumento;
			if ($scope.SesionLocale === 'PE') {
				var ds = new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarLibroOrdenes());
				ds.fetch().then(function(){
					var libros = ds.data();
					var libroEncontrado = undefined;
					$.each(libros,function(key,value){
						if (value.EsLibroOfertaPublica === "S" && value.IdEstado === "ABT" && value.IdInstrumento === ordenRV.datosOrden.instrumento.IdInstrumento) {
							libroEncontrado = value;
							return false;
						}
					});
					if (libroEncontrado) {
						var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_EXISTE_OFERTA_PUBLICA_ASOCIADA_INSTRUMENTO'),idVentana);
						//CARGAR FORMULARIO OFERTA PUBLICA
						t.then(function(result){
							if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
								ordenRV.modoVentana = 'OFERTAPUBLICA';
								ordenRV.datosForm.configuracionLibroOrdenes.filtros.IdInstrumento.value = libroEncontrado.IdInstrumento;
								ordenRV.datosForm.configuracionLibroOrdenes.idPostFiltro = libroEncontrado.IdLibroOrdenes;
								ordenRV.datosForm.AccionOrden = libroEncontrado.AccionOrden;
								ordenRV.datosForm.configuracionCompraVenta.activado = false;
								ordenRV.datosOrden.EsVentaCorta = false;
								ordenRV.datosForm.instrumentoDeshabilitado = true;

								$.each(BCS_OPERACION_INSTRUMENTO.LISTA,function(key,value){
									if (value.IdTipoOperacion === libroEncontrado.IdTipoOperacion) {
										ordenRV.datosOrden.operacionSeleccionada = value;
										return false;
									}

								})
							} 
						})//FIN PROMESA
					}
					
				},function(result){
					
				});
		  		
			}
			
			if($scope.ordenRV.datosOrden.instrumento.IdTipoInstrumento ==='MIL' || $scope.ordenRV.datosOrden.instrumento.IdTipoInstrumento ==='ACX'){
				$scope.ordenRV.datosForm.IdCondicionDeLiquidacion = "T3";	
			}else{
				$scope.ordenRV.datosForm.IdCondicionDeLiquidacion = "T2";
			}
			
		}
	});
	
	$scope.$watch('ordenRV.datosOrden.cantidadInicial',function(newval,oldval){

		if (newval && !oldval) {
			ordenRV.datosOrden.montoInicial = undefined;
		}

	});
	$scope.$watch('ordenRV.datosOrden.montoInicial',function(newval,oldval){
	
		if (newval && !oldval) {
			ordenRV.datosOrden.cantidadInicial = undefined;
		}

	});
	
	$scope.$watchGroup(['ordenRV.datosForm.AccionOrden','ordenRV.datosOrden.instrumento.IdInstrumento','ordenRV.datosOrden.InstrumentoNegociacion.IdInstrumento'],function(newval,oldval) {
		if (!newval) {
			return;
		}
		if (ordenRV.modoVentana === 'OFERTAPUBLICA') {
			return;
		}
		if ((!ordenRV.datosOrden.instrumento || !ordenRV.datosOrden.instrumento.IdTipoInstrumento) && ordenRV.SW === 'N') {
			ordenRV.datosOrden.operacionSeleccionada = undefined;
			return;
		}
		if(ordenRV.datosOrden.instrumento === undefined) {
			return;
		}
		var idTipoInstrumento = ordenRV.datosOrden.instrumento.IdTipoInstrumento;
		if (!idTipoInstrumento)
			return;
		
		var tipoOperacion = undefined;
		$.each(BCS_OPERACION_INSTRUMENTO,function(key,value){
			
			if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === ordenRV.datosForm.AccionOrden) {
				tipoOperacion = value;
				return false;
			}

		})
		
		if (tipoOperacion === undefined) {
			console.error('Tipo operacion no encontrada');
		}
		
		if (ordenRV.SW === 'N') {
			ordenRV.datosOrden.operacionSeleccionada = tipoOperacion;
		} else {
			ordenRV.datosOrden.operacionSeleccionada = tipoOperacion;
			calcularFechaCondicionLiquidacion(tipoOperacion);
		}
		if (ordenRV.datosOrden.InstrumentoNegociacion) {
			ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.InstrumentoNegociacion.Precio);				
			if ($scope.CalculoPrecioPunta === true && ordenRV.datosOrden.InstrumentoNegociacion.IdClasificacionTipoInstrumento === "RV" && ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === "M"){ // $scope.ParametroPrecioMercado === true && 
				if (ordenRV.datosForm.AccionOrden==='COMPRA' &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta != null &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta > ordenRV.datosOrden.InstrumentoNegociacion.PrecioMercado){
					ordenRV.datosOrden.precio= ordenRV.datosOrden.InstrumentoNegociacion.PuntaVenta;						
				}
				else
				if (ordenRV.datosForm.AccionOrden==='VENTA' &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra != null &&  ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra < ordenRV.datosOrden.InstrumentoNegociacion.PrecioMercado){
					ordenRV.datosOrden.precio= ordenRV.datosOrden.InstrumentoNegociacion.PuntaCompra;
				}					
			}
			else				
			ordenRV.datosOrden.precio = Number(ordenRV.datosOrden.InstrumentoNegociacion.Precio);

		}else{
			ordenRV.datosOrden.precio = ordenRV.datosOrden.instrumento.Precio;		
			if ($scope.CalculoPrecioPunta === true && ordenRV.datosOrden.instrumento.IdClasificacionTipoInstrumento === "RV" && ordenRV.datosOrden.tipoPrecioSeleccionado.IdTipoPrecioOrden === "M"){ // $scope.ParametroPrecioMercado === true && 
				if (ordenRV.datosForm.AccionOrden==='COMPRA' &&  ordenRV.datosOrden.instrumento.PuntaVenta != null  &&  ordenRV.datosOrden.instrumento.PuntaVenta > ordenRV.datosOrden.instrumento.PrecioMercado){
					ordenRV.datosOrden.precio= ordenRV.datosOrden.instrumento.PuntaVenta ; 
				}
				else
				if (ordenRV.datosForm.AccionOrden==='VENTA' &&  ordenRV.datosOrden.instrumento.PuntaCompra != null &&  ordenRV.datosOrden.instrumento.PuntaCompra < ordenRV.datosOrden.instrumento.PrecioMercado){
					ordenRV.datosOrden.precio= ordenRV.datosOrden.instrumento.PuntaCompra;
				}
				
			}
		}

		

	});
	
	
	//watch condicion de liquidacion y switch ingreso rapido activado
	$scope.$watch('ordenRV.datosOrden.condicionLiquidacionSeleccionada', function(newval, oldval) {
		if (newval && ordenRV.SW === 'S') {
			var tipoOperacion = undefined;
			if(ordenRV.datosOrden.instrumento === undefined) {
				return;
			}
			var idTipoInstrumento = ordenRV.datosOrden.instrumento.IdTipoInstrumento;
			$.each(BCS_OPERACION_INSTRUMENTO,function(key,value) {
			
				if (value.IdTipoInstrumento === idTipoInstrumento && value.AccionOrden === ordenRV.datosForm.AccionOrden) {
					tipoOperacion = value;
					return false;
				}

			});
			calcularFechaCondicionLiquidacion(tipoOperacion);
		}
	});

	//watch pago cobro cuenta inversion
	$scope.$watch('ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		if (newval){
			bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios([ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio]).then(function(result){
				
				if (result[0].OperaCuentaInversion === "S") {
					ordenRV.datosOrden.pagoCobroCuentaInversion = true;
					ordenRV.datosForm.pagoCobroCuentaInversionDisabled = false;
				} else {
					ordenRV.datosOrden.pagoCobroCuentaInversion = false;
					ordenRV.datosForm.pagoCobroCuentaInversionDisabled = true;
				}
				

				if (result[0].IdPersonaBrokerExtranjero !== undefined && result[0].IdPersonaBrokerExtranjero > 0 ) {
					ordenRV.datosForm.paraClienteFinalDisabled = false;
					ordenRV.datosOrden.esParaClienteFinal = true;
				} 
				else if (result[0].EsClienteFinal === 'S' ) {
					ordenRV.datosForm.paraClienteFinalDisabled = true;
					ordenRV.datosOrden.esParaClienteFinal = true;
				}
				else {
					ordenRV.datosForm.paraClienteFinalDisabled = true;
					ordenRV.datosOrden.esParaClienteFinal = false;
				}
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenRV.IdBovedaCorresponsal !== undefined 
							&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			ordenRV.datosForm.configuracionMecanismoCaptacion.filtros.IdEntidad.value = newval;
		} else {
			ordenRV.datosForm.configuracionMecanismoCaptacion.filtros.IdEntidad.value = -1;
		}
	});
	

	//watch forma de pago ultima orden
	$scope.$watchGroup([
		'ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio',
		'ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion',
		'ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion'],function(newval,oldval){
		if (ordenRV.datosOrden.portafolioSeleccionado && ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio && 
			ordenRV.datosOrden.operacionSeleccionada && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion && 
			ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada && ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion) {
			
			var datos = {
				p_IdPortafolio				:	ordenRV.datosOrden.portafolioSeleccionado.IdPortafolio,
				p_IdTipoOperacion			:	ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion,
				p_IdMonedaLiquidacionOrden	:	ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion
			}
			
			bcsServicioOrden.ConsultarFormaDePagoUltimaOrden(datos).$promise.then(function(result){
				if (!result.p_out_FormaDePagoOrden[0])
					return;
				
				if (result.p_out_FormaDePagoOrden[0].IdMedioDePago === 'CIN') {
					ordenRV.datosForm.IdPreferenciaPagoPortafolio = undefined;
					if (ordenRV.datosForm.pagoCobroCuentaInversionDisabled === false) {
						ordenRV.datosOrden.pagoCobroCuentaInversion = true;
					}
				}
				else {
					ordenRV.datosOrden.pagoCobroCuentaInversion = false;
					ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.idPostFiltro = result.p_out_FormaDePagoOrden[0].IdPreferenciaPagoPortafolio;
				}
				
			});
			/*OPTIMUSPE-1215*/
			if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.bolsaSeleccionada !== undefined 
					&& ordenRV.datosOrden.idBolsaSeleccionada === 'OUT'
						&& ordenRV.IdBovedaCorresponsal !== undefined 
							&& ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda !== undefined)
				ordenRV.datosForm.configuracionCuentaCustodio.filtros.IdBoveda.value = ordenRV.IdBovedaCorresponsal;
			/*OPTIMUSPE-1215*/
			
			ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
	});
	
	$scope.$watch('ordenRV.datosOrden.operacionSeleccionada',function(newval,oldval) {
		if (!newval)
			return;

		if (ordenRV.SW === 'N'
			&& (ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion != ordenRV.datosForm.configuracionCondicionLiquidacion.filtros.IdTipoOperacion.value
				|| ordenRV.datosForm.configuracionCondicionLiquidacion.filtros.IdInstrumento.value != (ordenRV.datosOrden.instrumento.IdInstrumento).toString())){
			ordenRV.datosForm.configuracionCondicionLiquidacion.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
			ordenRV.datosForm.configuracionCondicionLiquidacion.filtros.IdInstrumento.value = (ordenRV.datosOrden.instrumento.IdInstrumento).toString();			
			
		}
		if (ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value != ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion) {
			ordenRV.datosForm.configuracionPreferenciaPagoPortafolio.filtros.IdTipoOperacion.value = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion;
		}
	});
	
	$scope.$watch('ordenRV.datosOrden.condicionLiquidacionSeleccionada',function(newval,oldval) {
		if (newval !== undefined && ordenRV.datosOrden.condicionLiquidacionSeleccionada) {
			ordenRV.datosOrden.idBolsaSeleccionada = newval.IdCondicionDeLiquidacion === "T2" ? 0 : ordenRV.datosOrden.instrumento !== undefined ? ordenRV.datosOrden.instrumento.IdBolsa : "BCS";
			$scope.ordenRV.datosOrden.bolsaActivada = newval.IdCondicionDeLiquidacion === "T2" && ordenRV.datosOrden.idBolsaSeleccionada == 0 ? false : (newval.IdCondicionDeLiquidacion !== "T2" 
														&& (ordenRV.datosOrden.idTipoCanalDistribucion === "RUA" || ordenRV.datosOrden.idTipoCanalDistribucionSeleccionada.IdTipoCanalDistribucion === "RUA") ? false : true);
		}
	});
	
	$scope.$watch('ordenRV.datosOrden.idTipoCanalDistribucionSeleccionada',function(newval,oldval) {
		if (newval !== undefined && ordenRV.datosOrden.idTipoCanalDistribucionSeleccionada) {
			if(newval.IdTipoCanalDistribucion === "RUA"){
				ordenRV.datosForm.idTipoPrecio = "L";
				if (ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion === "T2") {
					ordenRV.datosOrden.idBolsaSeleccionada = 0;
					$scope.ordenRV.datosOrden.bolsaActivada = false;
					ordenRV.datosOrden.idTipoCanalDistribucion = "RUA";
				} else {
					ordenRV.datosOrden.idBolsaSeleccionada = "BCS";
					$scope.ordenRV.datosOrden.bolsaActivada = false;
				}
			} else if (ordenRV.datosOrden.condicionLiquidacionSeleccionada !== undefined && newval.IdTipoCanalDistribucion === "MAN" &&
				(ordenRV.datosOrden.idBolsaSeleccionada !== 0 || ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion !== "T2")) {
					
				$scope.ordenRV.datosOrden.bolsaActivada = true;
			} else {
				$scope.ordenRV.datosOrden.bolsaActivada = false;
			}
		}
	});
	
	$scope.$watchGroup(['ordenRV.datosOrden.EsVentaCorta','ordenRV.datosForm.muestraCobros'],function(newval,oldval) {
		$timeout(function() {bcsComunes.ventanaTamanoAutomatico(ordenRV.datosForm.idVentana);},10);
	});
	
	
	//watch paridades
	$scope.$watchGroup(["ordenRV.datosOrden.instrumento.IdInstrumento","ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion"
						,"ordenRV.datosOrden.bolsaSeleccionada.IdBolsa","ordenRV.datosForm.AccionOrden"],function(newval,oldval){
		if (!ordenRV.datosOrden.instrumento || !ordenRV.datosOrden.instrumento.IdMoneda || !ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada 
			|| !ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion)
			return;
		var idMonedaOrigen = ordenRV.datosOrden.instrumento.IdMoneda;
		var idMonedaDestino = ordenRV.datosOrden.monedaLiquidacionOrdenSeleccionada.IdMonedaLiquidacion;
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdTrabajo : 0,
			p_CalculosEntrada : [{
				IdTrabajo : 0,
				IdInterno : 0,
				Fecha : $scope.SesionFechaSistema,
				IdMonedaOrigen : idMonedaOrigen,
				IdMonedaDestino : idMonedaDestino,
				ValorCambioLocalModificado : 0,
				MontoOrigen : 1,
				IdUso : obtenerIdUsoPorIdTipoOperacion(ordenRV.datosForm.AccionOrden, ordenRV.datosOrden.instrumento.IdTipoInstrumento)
			}]
		};

		var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
		promise.then(function(result){
			if (result.p_out_CalculosSalida.length == 0 || result.p_out_CalculosSalida[0].Resultado === 'RECHAZO') {
				ordenRV.datosForm.paridadReferencia = $translate.instant("UI_NO_SE_ENCONTRO_PARIDAD") + ":" + idMonedaOrigen + " - " + idMonedaDestino;
			}
			else {
				ordenRV.datosForm.paridadReferencia = idMonedaOrigen + " - " + idMonedaDestino + ":" + result.p_out_CalculosSalida[0].FactorOrigenDestino;
				if (idMonedaDestino != $scope.SesionIdMonedaSistema) {
					ordenRV.datosForm.paridadReferencia+= "/" + idMonedaOrigen + " - " + $scope.SesionIdMonedaSistema + ":" + result.p_out_CalculosSalida[0].FactorOrigenLocal;
				}
			}
		});
		
	});
	
	var obtenerIdUsoPorIdTipoOperacion = function (accionOrden, idTipoOperacion) {
		var idUso = '';
		if($scope.SesionLocale === 'CL'){
			if ('COMPRA' === accionOrden && 'MIL' === idTipoOperacion)
				idUso = 'COMPRA MILA';
			else if ('VENTA' === accionOrden && 'MIL' === idTipoOperacion)
				idUso = 'VENTA MILA';
			else if ('COMPRA' === accionOrden && ('ETX' === idTipoOperacion || 'CFX' === idTipoOperacion || 'ACX' === idTipoOperacion))
				idUso = 'COMPRA EXTRANJEROS'; //COMETX;COMCFX;COMACX
			else if ('VENTA' === accionOrden && ('ETX' === idTipoOperacion || 'CFX' === idTipoOperacion || 'ACX' === idTipoOperacion))
				idUso = 'VENTA EXTRANJEROS'; //VENETX;VENCFX;VENACX
			else
				idUso = 'ORDENES';
		}else if($scope.SesionLocale === 'PE' && ordenRV.datosOrden.idBolsaSeleccionada !== undefined){
			if ('COMPRA' === accionOrden && 'MIL' === ordenRV.datosOrden.idBolsaSeleccionada)
				idUso = 'COMPRA MILA';
			else if ('VENTA' === accionOrden && 'MIL' === ordenRV.datosOrden.idBolsaSeleccionada)
				idUso = 'VENTA MILA';
			else if ('COMPRA' === accionOrden && 'OUT' === ordenRV.datosOrden.idBolsaSeleccionada)
				idUso = 'COMPRA EXTRANJEROS'; //COMETX;COMCFX;COMACX
			else if ('VENTA' === accionOrden && 'OUT' === ordenRV.datosOrden.idBolsaSeleccionada)
				idUso = 'VENTA EXTRANJEROS'; //VENETX;VENCFX;VENACX
			else
				idUso = 'ORDENES';
		}
		return idUso;
	};

	if ($scope.SesionLocale === 'PE') {
		$scope.$watch('ordenRV.datosForm.valorAllin',function(key,value){
			if (ordenRV.datosForm.valorAllin === undefined)
				return;
			if (ordenRV.datosForm.valorAllin < 0)
				return;
			
			ordenRV.datosForm.apiCobros.calcularAllIn($scope.ordenRV.datosForm.valorAllin);
		});
	}
	
	$scope.$watchGroup(['ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
		if (ordenRV.datosOrden.tipoVigenciaSeleccionada && ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
			if (ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSoloHoy){
				ordenRV.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaPermanente) {
				var datos = {
						p_fechas : [
							{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 29 }
							],
						p_diaHabil : 'N',
						p_soloCaeDiaHabil : 'S',
						p_consideraHabilAnterior : 'S',
						p_idPais : $scope.SesionIdPaisSistema
					};
				bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
						ordenRV.datosOrden.fechaVigencia = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
						}
					,function(result){
						console.log(result);
						}
					);
			}
			else if (ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaFechaFija) {
				ordenRV.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
			else if (ordenRV.datosOrden.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionOrdenes.CONSTANTES.IdTipoVigenciaSemanal) {
				var datos = {
					p_fechas : [
						{ fechaInicial :$scope.SesionFechaSistema, numeroDias : 6 }
						],
					p_diaHabil : 'N',
					p_soloCaeDiaHabil : 'S',
					p_consideraHabilAnterior : 'S',
					p_idPais : $scope.SesionIdPaisSistema
				};
			bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos,function(result) {
					ordenRV.datosOrden.fechaVigencia = util.dateDS(result.p_out_fechas[0].fechaCalculada,"FechaSistema","CALL");
					}
				,function(result){
					console.log(result);
					}
				);
			}
			else {
				ordenRV.datosOrden.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
			}
		}
	});
	
	//watch orden especial
	if ($scope.SesionLocale === 'PE') {
		$scope.$watch("ordenRV.datosForm.operacionSalida[0].totalOrden",function(newval,oldval){
			
			if (!newval) {
				ordenRV.datosForm.ordenEspecialDisabled = false;
				return;
			}
			
			var idMonedaOrigen = ordenRV.datosOrden.instrumento.IdMoneda;
			var idMonedaDestino = "USD";
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdTrabajo : 0,
				p_CalculosEntrada : [{
					IdTrabajo : 0,
					IdInterno : 0,
					Fecha : $scope.SesionFechaSistema,
					IdMonedaOrigen : idMonedaOrigen,
					IdMonedaDestino : idMonedaDestino,
					ValorCambioLocalModificado : 0,
					MontoOrigen : ordenRV.datosForm.operacionSalida[0].totalOrden,
					IdUso : obtenerIdUsoPorIdTipoOperacion(ordenRV.datosForm.AccionOrden, ordenRV.datosOrden.instrumento.IdTipoInstrumento)
				}]
			};
			
			var promise = bcsServiciosPlataformaMercado.CalcularParidadYTipoCambio(datos);
			promise.then(function(result){
				if (result.p_out_CalculosSalida.length > 0 && result.p_out_CalculosSalida[0].Resultado === 'OK') {
				
					if (result.p_out_CalculosSalida[0].MontoDestino > 50000) {
						ordenRV.datosForm.ordenEspecialDisabled = true;
						ordenRV.datosOrden.esOrdenEspecial = true;
					}
					else {
						ordenRV.datosForm.ordenEspecialDisabled = false;
					}
				}
			});
			
		});
	}
	
	if (parametros.orden) {
		ordenRV.datosForm.setInstrumento = parametros.orden.Instrumento;
		if (parametros.orden.AccionOrden === 'COMPRA') {
			ordenRV.datosForm.configuracionCompraVenta.defecto = 'VENTA';
		}
		else {
			ordenRV.datosForm.configuracionCompraVenta.defecto = 'COMPRA';
		}
		ordenRV.datosOrden.cantidadInicial = parametros.orden.CantidadOrden;
		ordenRV.datosForm.IdCondicionDeLiquidacion = parametros.orden.IdCondicionDeLiquidacion;
		ordenRV.datosForm.configuracionTipoVigencia.defecto = parametros.orden.IdTipoVigenciaOrden;
		if (ordenRV.datosForm.configuracionTipoVigencia.defecto === 'FF') {
			ordenRV.datosOrden.fechaVigencia = parametros.orden.FechaVigencia; 
		}
		ordenRV.datosForm.configuracionTipoPrecio.defecto = parametros.orden.IdTipoPrecio;
		if (ordenRV.datosForm.configuracionTipoPrecio.defecto === "L") {
			ordenRV.datosOrden.precio = parametros.orden.PrecioOrden;
		}
		ordenRV.datosForm.configuracionBolsa.defecto = parametros.orden.IdBolsa;
		ordenRV.datosOrden.EsVentaCorta = parametros.orden.EsVentaCorta === "S"?true: false;
		ordenRV.datosOrden.plazoVentaCorta = parametros.orden.PlazoVentaCorta;
		ordenRV.datosOrden.tasaVentaCorta = parametros.orden.TasaVentaCorta;
		
		
	} 
	
	//otros defaults
	else {
		if ($scope.SesionLocale === 'PE') {
			ordenRV.datosForm.IdCondicionDeLiquidacion = 'T2';
		} else {
			ordenRV.datosForm.IdCondicionDeLiquidacion = 'T2';
		}
	}
	
	var calcularFechaCondicionLiquidacion = function(tipoOperacion) {
		var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'PLA'; 
			datos.p_UsaReglasNegocio = 'S';
			datos.p_FechaIngreso = angular.isDate(ordenRV.datosForm.fechaInicial)?util.dateSPdate(ordenRV.datosForm.fechaInicial):ordenRV.datosForm.fechaInicial;//util.dateSPdate(scope.fechaInicial);
			datos.p_IdTipoOperacion = tipoOperacion.IdTipoOperacion || null;
			datos.P_IdCondicionDeLiquidacion = ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion;
			datos.p_IdPais = Number($scope.SesionIdPaisSistema);


			bcsServicioPlataforma.FechaLiquidacion(datos,function(result) {

				if (result.P_OUT_ERRORESCONTADOR === 0){
					ordenRV.datosOrden.fechaLiquidacion = util.dateDS(result.P_OUT_FECHALIQUIDACION,"FechaSistema","CALL");
				}
				else {
					ordenRV.datosOrden.fechaLiquidacion = util.dateDS("0001-01-01","FechaSistema","CALL");
					console.log("Error." + result.P_OUT_ERRORESCONTADOR);
				}
			},function(result) {

				console.error(result);
			});
	}
	
	

      $scope.$watch('ordenRV.datosForm.AccionOrden', function(newValue, oldValue) {
		 if (newValue === 'VENTA') {
			ordenRV.datosOrden.GeneraSpot=false;
			bcsComunes.ventanaAutoHeight(idVentana);
         } else {
			ordenRV.datosOrden.EsVentaCorta=false;
            bcsComunes.ventanaAutoHeight(idVentana);
         }
      });

	  $scope.$watch('ordenRV.datosForm.operacionSalida', function(newValue, oldValue) {
		if (newValue && newValue.length > 0) {
			bcsComunes.ventanaAutoHeight(idVentana);
		}
	  });

	  $scope.$watchGroup(['ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion','ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion','ordenRV.datosForm.fechaInicial'],function(newval,oldval){
		if (ordenRV.datosOrden.operacionSeleccionada != undefined){
			if (ordenRV.datosOrden.condicionLiquidacionSeleccionada && ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion && ordenRV.datosForm.fechaInicial && ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion){
				var datos = {};
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;
				datos.p_IdUsuario = $scope.SesionIdUsuario; 
				datos.p_IdModulo = 'PLA'; 
				datos.p_UsaReglasNegocio = 'S';
				datos.p_FechaIngreso = angular.isDate(ordenRV.datosForm.fechaInicial)?util.dateSPdate(ordenRV.datosForm.fechaInicial):ordenRV.datosForm.fechaInicial;
				datos.p_IdTipoOperacion = ordenRV.datosOrden.operacionSeleccionada.IdTipoOperacion || null;
				datos.P_IdCondicionDeLiquidacion =ordenRV.datosOrden.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion;
				datos.p_IdPais = Number($scope.SesionIdPaisSistema);
						 
				bcsServicioPlataforma.FechaLiquidacion(datos,function(result) {
					
					if (result.P_OUT_ERRORESCONTADOR === 0){
						ordenRV.datosOrden.fechaLiquidacion = util.dateDS(result.P_OUT_FECHALIQUIDACION,"FechaSistema","CALL");
					}
					else {
						ordenRV.datosOrden.fechaLiquidacion = util.dateDS("0001-01-01","FechaSistema","CALL");
						console.log("Error." + result.P_OUT_ERRORESCONTADOR);
					}
				},function(result) {
					
					console.error(result);
				});
			} 
		}
	});
	
		
	initDatosAgente();
});
