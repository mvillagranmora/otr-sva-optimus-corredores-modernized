angular.module('ModuloOrdenes').controller('InterfazAsignacionCtrl',
		function($scope,bcsServicioOrden,bcsComunes,bcsConfiguracionOrdenesV2,$timeout,$translate,$q,$resource, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA){
	
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	$scope.formData = {};
	$scope.formData.muestraResultado = false;
	$scope.IdGrupoSeleccionado = null;
	var OperacionSobregirable = 'N';
	var IdTipoOperaciones = [];
	bcsComunes.ventanaCambiaCss($scope.idVentana,'overflow','hidden');
	
	var tipoOperacionSobregirable = [];
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.OperacionesNegativas', $scope.SesionIdEmpresa, 'CUSTODIA')
	 .then(function (result) {
		tipoOperacionSobregirable = result.Valor.split(",").map(item => item.trim())
	 });

	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},10);
	};
	
	$scope.formData.filtrosGrupoPortafolio = {
		read:true
	}
	
	$scope.formData.editarAsignacion = function(asig) {
		
		var id = bcsComunes.ventanaAbrir("EditarAsignacion",{asignacion : asig[0],transaccion: buscarTransaccion(asig[0].IdTransaccion)},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id).then(function(data){
			
			if (data !== undefined && data.hasOwnProperty("glosa")) {
				asig[0].Glosa = data.glosa;
				asig[0].IdBoveda = data.idBoveda;
				asig[0].IdCuentaCustodioPortafolio = data.idCuentaCustodioPortafolio;
				asig[0].IdFormaCustodia = data.idFormaCustodia;
				asig[0].TasaVentaCorta = data.tasaVentaCorta;
				asig[0].PlazoVentaCorta = data.plazoVentaCorta;
				asig[0].TipoCambio = data.tipoCambio;
				asig[0].IdMonedaLiquidacionOrden = data.idMonedaLiquidacionOrden;
				asig[0].PagoCobroCuentaInversion = data.pagoCobroCuentaInversion;
				asig[0].IdPreferenciaPagoPortafolio = data.idPreferenciaPagoPortafolio;
				asig[0].IdCuentaCustodioPortafolio = data.idCuentaCustodioPortafolio;
				asig[0].CodigoIFRS = data.codigoIFRS
				
				if (data.cobros && data.cobros.length !== 0) {
					$.each(asig[0].cobrosAsignacion,function(key1,cobro) {
						$.each(data.cobros,function(key2,cobroNuevo) {
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
	
	$scope.formData.editarCobros = function() {
		
		var asigs = $scope.formData.asignacionSeleccionada;
		var cobros  = [];

		$.each(asigs,function(key1,asig){
			$.each(asig.cobrosAsignacion,function(key2,cobro){
				var c = _.find(cobros, function(cob){ if (cob.IdCobro === cobro.IdCobro) return cob; });
				if (!c) {
					c =	{
						IdCobro : cobro.IdCobro,
						Maximo : cobro.ValorCobro,
						Minimo : cobro.ValorCobro,
						CodigoCobroClasificacion : cobro.CodigoCobroClasificacion,
						FormaCobro : cobro.FormaCobro,
						IdMoneda : cobro.IdMoneda,
						PermiteCambiarValor : cobro.PermiteCambiarValor
					}
					cobros.push(c);
				}
				else {
					if (c.Maximo <= cobro.ValorCobroAplicado)
						c.Maximo = cobro.ValorCobroAplicado;
					if (c.Minimo >= cobro.ValorCobroAplicado)
						c.Minimo = cobro.ValorCobroAplicado;
				}
			});
		});
		
		var id1 = bcsComunes.ventanaAbrir("EdicionMultiple", { 
			cobros : cobros,		
			fechaInicial : $scope.configuracionInterfaz.FechaOperacion,
			idCondicionDeLiquidacion : $scope.configuracionInterfaz.IdCondicionDeLiquidacion,			
			idMonedaInstrumento : asigs[0].IdMonedaInstrumento
			
		
		},false,false,$scope.idVentana);
		
		bcsComunes.ventanaPromesa(id1).then(function(data){
			var asignaciones = $scope.formData.asignacionSeleccionada;
			$.each(asignaciones,function(key1,asig){
				if (data.idBoveda !== undefined) asig.IdBoveda = data.idBoveda;
				if (data.idCuentaCustodioPortafolio !== undefined) asig.IdCuentaCustodioPortafolio = data.idCuentaCustodioPortafolio;
				if (data.idFormaCustodia !== undefined) asig.IdFormaCustodia = data.idFormaCustodia;
				if (data.plazoVentaCorta !== undefined) asig.PlazoVentaCorta = data.plazoVentaCorta;
				if (data.tasaVentaCorta !== undefined) asig.TasaVentaCorta = data.tasaVentaCorta;
				if (data.tipoCambio !== undefined) asig.TipoCambio = data.tipoCambio;
				if (data.idMonedaLiquidacionOrden !== undefined) asig.IdMonedaLiquidacionOrden = data.idMonedaLiquidacionOrden;
				if (data.codigoIFRS !== undefined) asig.CodigoIFRS = data.codigoIFRS

				$.each(data.cobros,function(key2,cobroMasivo){
					$.each(asig.cobrosAsignacion,function(key3,cobroAsignacion){
						if (cobroAsignacion.IdCobro === cobroMasivo.IdCobro)
							cobroAsignacion.ValorCobroAplicado = cobroMasivo.ValorNuevo;
					});
				});

			});
			


		});
	};
	
	var buscarTransaccion = function(idTransaccion) {
		 var x = _.find($scope.formData.apiTransacciones.dataSource.data(), function(tran){ if (tran.IdTransaccion === idTransaccion) return tran; });
		 return x;
	}
	var agregarTransacciones = function(transacciones) {
		console.log(transacciones);
		var current = [];
		var hayTransacciones = true;
		if ($scope.formData.confTransacciones.dataSource.data().length === 0) {
			hayTransacciones = false;
			setearConfiguracion(transacciones[0]);
			setEstadoAsignacion();
		};
		
		$.each($scope.formData.confTransacciones.dataSource.data(),function(key,value) {
			value.CantidadSimulacionTransaccion = 0;
			value.ValorRescateSimulacionTransaccion = 0;
			current.push(value);
		});
		$scope.formData.confTransacciones.dataSource.data(_.uniq(_.union(current,transacciones),false,function(item){
			return item.IdTransaccion;
		}));
		

		
		var marcas = _.groupBy(transacciones, function(num){ return num.MarcaNegociacion; });
		//si son las primeras transacciones, y todas tienen la misma marca, cargo los clientes del grupo
		if (hayTransacciones === false && Object.keys(marcas).length === 1 && Object.keys(marcas)[0] !== "") {
			//CARGAR CLIENTES
			var ds = bcsConfiguracionOrdenesV2.getDataSourcePortafoliosGrupo();
			ds.definition.filter = [
				{ field: "CodigoGrupo", operator: "eq", value: Object.keys(marcas)[0] },
				{ field: "IdLibroOrdenes", operator: "eq", value: $scope.configuracionInterfaz.IdLibroOrdenes }
			];
			ds = new bcs.DataSource(ds);
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			ds.fetch().then(function(){
				console.log(ds);

				if (!ds.data() || !ds.data()[0]) {
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					return;
				}

				var current = [];
				var portafoliosNuevos = [];
				$.each($scope.formData.confClientes.dataSource.data(),function(key,value) {
					current.push(value);
				});
				$.each(ds.data(),function(key,value) {
					value.CantidadSimulacionCliente =  0;
					value.ValorRescateSimulacionCliente = 0;
					value.PorcentajeSimulacion =  value.Porcentaje;
					value.IdMoneda = value.IdMoneda || 'CLP';
					portafoliosNuevos.push(value);
				});
				$scope.formData.confClientes.dataSource.data(_.uniq(_.union(current,portafoliosNuevos),false,function(item){
					return item.IdPortafolio;
				}));
				$scope.formData.apiClientes.refresh();
				$scope.formData.tabClientesActiva = true;
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				$scope.formData.previsualizar('ASIGNACION_POSTRERA');
			},function(){
				bcsComunes.msg("error","error","UI_ERROR_AL_CARGAR_CLIENTES");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			})
		}
		if ($scope.formData.apiTransacciones) $scope.formData.apiTransacciones.refresh();
		setearColumnas();
	}

	$scope.formData.ventanaAgregarTransacciones = function() {
		
		var id1 = bcsComunes.ventanaAbrir("SelectorTransacciones", {
			elemento : 'TRANSACCION', 
			permiteCombinar : false, 
			
			IdLibroOrdenes : $scope.configuracionInterfaz.IdLibroOrdenes,			
			IdCondicionDeLiquidacion : $scope.configuracionInterfaz.IdCondicionDeLiquidacion,			
			FechaOperacion : $scope.configuracionInterfaz.FechaOperacion
			
			} 
		,false,false,$scope.idVentana);
		
		bcsComunes.ventanaPromesa(id1).then(function(transacciones){
			
			if (!transacciones || !transacciones[0])
				return;
			
			agregarTransacciones(transacciones);
		});
		
	};
	
	$scope.formData.ventanaQuitarTransacciones = function() {
		$scope.formData.apiTransacciones.dataSource.remove($scope.formData.transaccionesSeleccionadas);
		if ($scope.formData.apiTransacciones.dataSource.data().length === 0) {
			limpiarGrids();
			setearConfiguracion();
			setEstadoAsignacion();
		} else {
			var asignaciones = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
			var eliminar = [];
			$.each($scope.formData.transaccionesSeleccionadas,function(key1,value1){
				$.each(asignaciones,function(key2,value2){
					if (value1.IdTransaccion === value2.IdTransaccion)
						eliminar.push(value2);
				})

			})
			$scope.formData.apiSimulacionAsignaciones.dataSource.remove(eliminar);
			
			
		}
		$scope.formData.transaccionesSeleccionadas = undefined;
		
		recalcularDisponibles();
	};
	
	$scope.formData.ventanaAgregarElementos = function() {
		
		if ($scope.formData.apiTransacciones.dataSource.data().length === 0) {
			bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_TRANSACCIONES");
			return;
		}
		
		if ($scope.formData.tabOrdenesActiva) {		
			var instrumentos = _.groupBy($scope.formData.apiTransacciones.dataSource.data(), function(x){ return x.Instrumento; });
						
			filtroInstrumento = instrumentos[0];
			
			var id1 = bcsComunes.ventanaAbrir("SelectorElementos", {
				elemento : 'ORDENPORASIGNAR', 
				permiteCombinar : false, 
			
				IdLibroOrdenes 					: $scope.configuracionInterfaz.IdLibroOrdenes,				
				IdCondicionDeLiquidacion 		: $scope.configuracionInterfaz.IdCondicionDeLiquidacion,
				

				instrumento 					: (!!$scope.formData.transaccionesSeleccionadas)?$scope.formData.transaccionesSeleccionadas[0] : undefined,
			
			} 
		,false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(id1).then(function(data){
				if (!data || !data[0]) return;
				
				agregarOrdenes(data);
			});
			
			
		}
		else if ($scope.formData.tabClientesActiva) {
			
			var id1 = bcsComunes.ventanaAbrir("SelectorClientes", { IdLibroOrdenes : $scope.configuracionInterfaz.IdLibroOrdenes } ,false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(id1).then(function(data){
				if (!data || !data[0]) return;
				
				agregarClientes(data);
			});
		}
		else if ($scope.formData.tabCarterasActiva) {
			
			var trans = $scope.formData.apiTransacciones.dataSource.data();
			var groupByClasificacion = _.groupBy(trans, function(num){ return num.IdClasificacionTipoInstrumento; });			
			var groupByAccion = _.groupBy(trans, function(num){ return num.AccionOrden; });
			if (groupByAccion.COMPRA !== undefined) {
				bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DEBEN_SER_VENTA");
				return;
			}
			
			var groupByFechaLiquidacion = _.groupBy(trans, function(num){ return num.FechaLiquidacion; });
			if (Object.keys(groupByFechaLiquidacion).length>1) {
				bcsComunes.msg("error","UI_ERROR","UI_TRANSACCIONES_DIFERENTE_FECHA_LIQUIDACION");
				return;
			}

			var parametros = {
				IdInstrumento 					: trans[0].IdInstrumento,
				IdTipoOperacion					: trans[0].IdTipoOperacion,
				Instrumento 					: trans[0].Instrumento,
				FechaLiquidacion 				: trans[0].FechaLiquidacion,
				IdEmisor 						: trans[0].IdEmisor,
				Tasa 							: trans[0].Tasa,
				IdTipoReajuste 					: trans[0].IdTipoReajuste,
				IdTipoInstrumento				: trans[0].IdTipoInstrumento,
				FechaVencimiento 				: trans[0].FechaVencimiento,
				EsFungible 						: trans[0].EsFungible,
				IdClasificacionTipoInstrumento 	: trans[0].IdClasificacionTipoInstrumento,
			};
			var id = bcsComunes.ventanaAbrir("SelectorCarteras",parametros,false,false,$scope.idVentana);
			bcsComunes.ventanaPromesa(id).then(function(data){
				if (!data || !data[0]) return;
				 
				var current = [];
				var carterasNuevas = [];
				$.each($scope.formData.apiCarteras.dataSource.data(),function(key,value) {
					current.push(value);
				});
				$.each(data,function(key,value) {
					value.CantidadSimulacionCartera =  0;
					carterasNuevas.push(value);
				});
				$scope.formData.apiCarteras.dataSource.data(_.uniq(_.union(current,carterasNuevas),false,function(item){
					return item.FolioCartera;
				}));
				$scope.formData.apiCarteras.refresh();
				setearColumnas();
				
			});
		}
	};
	
	var agregarOrdenes = function(data) {
		var current = [];
		var ordenesNuevas = [];
		$.each($scope.formData.apiOrdenes.dataSource.data(),function(key,value) {
			current.push(value);
		});
		$.each(data,function(key,value) {
			value.CantidadSimulacionOrden =  0;
			ordenesNuevas.push(value);
		});
		$scope.formData.apiOrdenes.dataSource.data(_.uniq(_.union(current,ordenesNuevas),false,function(item){
			return item.IdOrden;
		}));
		$scope.formData.apiOrdenes.refresh();
		setearColumnas();
	}
	
	$scope.formData.ventanaQuitarElementos = function() {
		if ($scope.formData.tabOrdenesActiva) {
			if ($scope.formData.ordenesSeleccionadas === undefined) {
				bcsComunes.msg("error","UI_ERROR", "UI_DEBE_SELECCIONAR_ORDENES");
			} else {
				$scope.formData.apiOrdenes.dataSource.remove($scope.formData.ordenesSeleccionadas);
				$scope.formData.ordenesSeleccionadas = undefined;
				$scope.formData.apiOrdenes.refresh();
				$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
			}
		}
		else if ($scope.formData.tabClientesActiva) {
			if ($scope.formData.clientesSeleccionados === undefined) {
				bcsComunes.msg("error","UI_ERROR", "UI_DEBE_SELECCIONAR_CLIENTES");
			} else {
				$scope.formData.apiClientes.dataSource.remove($scope.formData.clientesSeleccionados);
				$scope.formData.clientesSeleccionados = undefined;
				$scope.formData.apiClientes.refresh();
				$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
			}
		}
		else if ($scope.formData.tabCarterasActiva) {
			if ($scope.formData.carterasSeleccionadas === undefined) {
				bcsComunes.msg("error","UI_ERROR", "UI_DEBE_SELECCIONAR_CARTERAS");
			} else {
				$scope.formData.apiCarteras.dataSource.remove($scope.formData.carterasSeleccionadas);
				$scope.formData.carterasSeleccionadas = undefined;
				$scope.formData.apiCarteras.refresh();
				$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
			}
		}
		recalcularDisponibles();
	};
	
	var agregarClientes =  function(clientes) {
		
		var sumaPorcentajes = 0;
		$.each($scope.formData.apiClientes.dataSource.data(),function(key,value) {
			sumaPorcentajes+=Number(value.PorcentajeSimulacion);
		});
		
		if (clientes.length > 0 ) sumaPorcentajes = Math.round(((100 - sumaPorcentajes) /clientes.length));
		
		var current = [];
		var portafoliosNuevos = [];
		$.each($scope.formData.apiClientes.dataSource.data(),function(key,value) {
			current.push(value);
		});
		$.each(clientes,function(key,value) {
			value.CantidadSimulacionCliente =  0;
			value.ValorRescateSimulacionCliente = 0;
			value.PorcentajeSimulacion = value.Porcentaje ||  sumaPorcentajes;
			value.IdMoneda = value.IdMoneda || 'CLP';
			portafoliosNuevos.push(value);
		});

		var ports = _.uniq(_.union(current,portafoliosNuevos),false,function(item){
			return item.IdPortafolio;
		});

		$scope.formData.apiClientes.dataSource.data(ports);
		$scope.formData.apiClientes.refresh();
		setearColumnas();
	}

	$scope.updateTabs = function() {
		if ($scope.formData.apiClientes) $scope.formData.apiClientes.refresh();
		if ($scope.formData.apiOrdenes) $scope.formData.apiOrdenes.refresh();
		if ($scope.formData.apiCarteras) $scope.formData.apiCarteras.refresh();
	};
	
	
	// Recalcula el Monto Asignacion por Regla 3	
	var recalcularMontoAsignacion = function(values, model)
	{		
		if ('CantidadAsignadaSimulacion' in values) 
		{			
			if (values.CantidadAsignadaSimulacion > 0 && model.CantidadTransaccion > 0)
			{	
				model.MontoAsignacion = ((model.MontoTransaccion * values.CantidadAsignadaSimulacion)/model.ValorRescateMontoTransaccion);
				$scope.formData.apiSimulacionAsignaciones.refresh();
			}		
		}
		if ('ValorRescateAsignadoSimulacion' in values) 
		{
			if (values.ValorRescateAsignadoSimulacion > 0 && model.EsFungible === 'S')
			{	
				model.MontoAsignacion = ((model.MontoTransaccion * values.ValorRescateAsignadoSimulacion)/model.CantidadTransaccion);
				$scope.formData.apiSimulacionAsignaciones.refresh();
			}	
		}
		$("#gridSimulacionAsignaciones").find("[kendo-grid]").data("kendo-grid").refresh();
	}
	
	var recalcularDisponibles = function(evento) {
		var asignaciones = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
		var transaccionesC = {};
		var portafoliosC = {};
		var ordenesC = {};
		
		var transaccionesM = {};
		var portafoliosM = {};
		
		$.each(asignaciones,function(key,value) {
			transaccionesC[value.IdTransaccion] = 0;
			transaccionesM[value.IdTransaccion] = 0;
			ordenesC[value.IdOrden] = 0;
			portafoliosC[value.IdPortafolio] = 0;
			portafoliosM[value.IdPortafolio] = 0;
		});

		$.each(asignaciones,function(key,value) {
			if (evento && value === evento.model) {
				if ('CantidadAsignadaSimulacion' in evento.values) {
					transaccionesC[value.IdTransaccion] = transaccionesC[value.IdTransaccion] + evento.values.CantidadAsignadaSimulacion;
					portafoliosC[value.IdPortafolio] = portafoliosC[value.IdPortafolio] + evento.values.CantidadAsignadaSimulacion;
					ordenesC[value.IdOrden] = ordenesC[value.IdOrden] + evento.values.CantidadAsignadaSimulacion;
				}
				if ('ValorRescateAsignadoSimulacion' in evento.values) {
					transaccionesM[value.IdTransaccion] = transaccionesM[value.IdTransaccion] + evento.values.ValorRescateAsignadoSimulacion;
					portafoliosM[value.IdPortafolio] = portafoliosM[value.IdPortafolio] + evento.values.ValorRescateAsignadoSimulacion;
				}
			}
			else {
				transaccionesC[value.IdTransaccion] = transaccionesC[value.IdTransaccion] + Number(value.CantidadAsignadaSimulacion);
				transaccionesM[value.IdTransaccion] = transaccionesM[value.IdTransaccion] + Number(value.ValorRescateAsignadoSimulacion);
				
				portafoliosC[value.IdPortafolio] = portafoliosC[value.IdPortafolio] + Number(value.CantidadAsignadaSimulacion);
				portafoliosM[value.IdPortafolio] = portafoliosM[value.IdPortafolio] + Number(value.ValorRescateAsignadoSimulacion);
				
				ordenesC[value.IdOrden] = ordenesC[value.IdOrden] + Number(value.CantidadAsignadaSimulacion);
			}
		});
		
		var transaccionesGrid = $scope.formData.apiTransacciones.dataSource.data();
		var transaccionPrecios = {};
		$.each(transaccionesGrid, function(key,value) {
			transaccionPrecios[value.IdTransaccion] = value.PrecioTransaccion;
			value.CantidadSimulacionTransaccion = transaccionesC[value.IdTransaccion];
			value.ValorRescateSimulacionTransaccion = transaccionesM[value.IdTransaccion];
		});
		
		var ordenesGrid = $scope.formData.apiOrdenes.dataSource.data();
		$.each(ordenesGrid, function(key,value) {
			value.CantidadSimulacionOrden = ordenesC[value.IdOrden];
		});
		
		var portafoliosGrid = $scope.formData.apiClientes.dataSource.data();
		$.each(portafoliosGrid, function(key,value) {
			value.CantidadSimulacionCliente = portafoliosC[value.IdPortafolio];
			value.ValorRescateSimulacionCliente = portafoliosM[value.IdPortafolio];
		});
		
		var portafolios = _.groupBy(asignaciones, function(dd) { return dd.IdPortafolio});
		var totalPortafolio = {};
		var parcialPortafolio = {};
		
		$.each(asignaciones,function(key,value) {
			if (!totalPortafolio[value.IdPortafolio + " " + value.Instrumento]) totalPortafolio[value.IdPortafolio + " " + value.Instrumento] = 0;
			if (!parcialPortafolio[value.IdPortafolio + " " + value.Instrumento]) parcialPortafolio[value.IdPortafolio + " " + value.Instrumento] = 0;
			if (evento && value === evento.model) {
				totalPortafolio[value.IdPortafolio + " " + value.Instrumento]+=evento.values.CantidadAsignadaSimulacion;
				parcialPortafolio[value.IdPortafolio + " " + value.Instrumento]+=(evento.values.CantidadAsignadaSimulacion*transaccionPrecios[value.IdTransaccion]);
			}
			else {
				totalPortafolio[value.IdPortafolio + " " + value.Instrumento]+=value.CantidadAsignadaSimulacion;
				parcialPortafolio[value.IdPortafolio + " " + value.Instrumento]+=(value.CantidadAsignadaSimulacion*transaccionPrecios[value.IdTransaccion]);

			}
		});

		$.each(asignaciones, function(key,value){
			value.PrecioPromedio = (totalPortafolio[value.IdPortafolio + " " + value.Instrumento] > 0)?parcialPortafolio[value.IdPortafolio + " " + value.Instrumento]/totalPortafolio[value.IdPortafolio + " " + value.Instrumento]:0;
		} );
		
		
		$scope.formData.apiTransacciones.refresh();
		$scope.formData.apiOrdenes.refresh();
		$scope.formData.apiClientes.refresh();
		$scope.formData.apiSimulacionAsignaciones.refresh();
		$scope.formData.habilitado= true;
		
	};
	
	$scope.formData.previsualizar = function(tipoAsignacion) {
		
		var sumaPorcentaje = 0;
		var i = 0;
		var elem = null;
		
		var ordenes = [];
		var transacciones = [];
		var portafolios = [];
		
		var ordenesDS;
		var transaccionesDS;
		var portafoliosDS;
		var carterasDS;
		if (tipoAsignacion === 'ASIGNACION_MANUAL_CARTERAS' || tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES' || tipoAsignacion === 'ASIGNACION_MANUAL_ORDENES') {
			ordenesDS = $scope.formData.ordenesSeleccionadas || [];
			transaccionesDS = $scope.formData.transaccionesSeleccionadas || [];
			portafoliosDS = $scope.formData.clientesSeleccionados || [];
			carterasDS = $scope.formData.carterasSeleccionadas || [];
		}
		else {
			ordenesDS = $scope.formData.apiOrdenes.dataSource.data();
			transaccionesDS = $scope.formData.apiTransacciones.dataSource.data();
			portafoliosDS = $scope.formData.apiClientes.dataSource.data();
			carterasDS = $scope.formData.apiCarteras.dataSource.data();
		}
		
		$.each(ordenesDS,function(key,value) {
			ordenes.push({IdOrden: value.IdOrden});
		});
		
		$.each(transaccionesDS,function(key,value) {
			tr = {
				IdMovimiento: value.IdMovimiento?value.IdMovimiento:value.IdTransaccion, 
				IdTransaccion: value.IdTransaccion, 
				Cantidad : value.CantidadRefacturar?value.CantidadRefacturar:value.CantidadDisponibleTransaccion, 
				Monto: value.Monto?value.MontoRefacturar:0, 
				ValorRescate: value.ValorRescateRefacturar?value.ValorRescateRefacturar:0
			};
			if (value.EsFungible === 'S')
				tr.Cantidad = value.ValorRescateDisponibleTransaccion;
			transacciones.push(tr);
		});
		
		$.each(portafoliosDS,function(key,value) {
			portafolios.push({IdPortafolio: value.IdPortafolio, Cantidad : 0, Porcentaje : value.PorcentajeSimulacion});
			sumaPorcentaje+=Number(value.PorcentajeSimulacion);
		});
		
		var carteras = [];
		if (tipoAsignacion === 'ASIGNACION_MANUAL_CARTERAS' || tipoAsignacion === 'ASIGNACION_CARTERAS') {
			for (i = 0; i < carterasDS.length;i++) {
				elem = carterasDS[i];
				if (elem.CantidadDisponible < elem.CantidadSimulacionCartera) {
					bcsComunes.msg("error","UI_ERROR","UI_CARTERA_SOBREASIGNADA");
					return;
				}
				
				if(elem.IdClasificacionTipoInstrumento == 'IF' || elem.IdClasificacionTipoInstrumento == 'RF'){
					if (validarCartera(elem.CantidadSimulacionCartera, elem.CorteMinimo) != true){
						return;
					}
				}
				
				carteras.push({FolioCartera: elem.FolioCartera, IdPortafolio : elem.IdPortafolio, Cantidad: elem.CantidadSimulacionCartera, CorteMinimo : elem.CorteMinimo});
			}
		}
		
		if (transacciones.length === 0) {
		 	bcsComunes.msg("error","UI_ERROR","UI_DEBE_SELECCIONAR_TRANSACCIONES");
		 	return;
		}
		if ((tipoAsignacion === 'ASIGNACION_MANUAL_ORDENES' || tipoAsignacion === 'ASIGNACION_CRONOLOGICA' || tipoAsignacion === 'ASIGNACION_PROPORCIONAL') && ordenes.length === 0) {
		 	bcsComunes.msg("error","UI_ERROR","UI_DEBE_SELECCIONAR_ORDENES");
		 	return;
		}

		if (( tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES' || tipoAsignacion === 'ASIGNACION_PRECIO_PROMEDIO' || tipoAsignacion === 'ASIGNACION_POSTRERA') && portafolios.length === 0) {
		 	bcsComunes.msg("error","UI_ERROR","UI_DEBE_SELECCIONAR_CLIENTES");
		 	return;
		}
		
		if ((tipoAsignacion === 'ASIGNACION_MANUAL_CARTERAS' || tipoAsignacion === 'ASIGNACION_CARTERAS') && carteras.length === 0) {
		 	bcsComunes.msg("error","UI_ERROR","UI_DEBE_SELECCIONAR_CARTERAS");
		 	return;
		}
		
		if (tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES' || tipoAsignacion === 'ASIGNACION_PRECIO_PROMEDIO' || tipoAsignacion === 'ASIGNACION_POSTRERA') {
			if (sumaPorcentaje > 100) {
				bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_SOBREPASA_100");
				return;
			}
		}
		
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'ORD',
			p_UsaReglasNegocio: 'S',
			p_IdTipoAsignacion : tipoAsignacion,
			p_IdTipo: $scope.formData.formaAsignacion,
			p_Origen : $scope.formData.origenAsignacion?$scope.formData.origenAsignacion:'ASIG', 
			p_Transacciones : transacciones,	//TABLE ("IdTransaccion"  BIGINT),
			p_Carteras : carteras,	//TABLE ("IdTransaccion"  BIGINT),
			p_Ordenes: ordenes,					//TABLE ("IdOrden"    BIGINT),
			p_Portafolios: portafolios,			//TABLE ("IdPortafolio"  BIGINT, "Cantidad" DECIMAL (26,8), "Porcentaje" DECIMAL (26,8)),
			
		};
		
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var p = bcsServicioOrden.CrearSimulacionAsignacion(datos).$promise;
		p.then(function(result) {
			procesarResultadoSimulacion(result,tipoAsignacion);	
				
		},function(result){
			bcsComunes.msg("error","Error","Existen problemas al ingresar la operacion");
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
		
	};
	
	var corteMinimo = 0;
	
	var procesarResultadoSimulacion = function(result,tipoAsignacion) {
		
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		$scope.formData.ordenesSeleccionadas = undefined;
		$scope.formData.transaccionesSeleccionadas = undefined;
		$scope.formData.clientesSeleccionados = undefined;
		$scope.formData.carterasSeleccionadas = undefined;
			
		if (result.P_OUT_ERRORESCONTADOR > 0) {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ERROR_AL_CREAR_ASIGNACIONES"),
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
			
			$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
			return;
		}
		
		if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_ADVERTENCIA_AL_CREAR_ASIGNACIONES"),
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);
			
			$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
			return;
		}

		$scope.formData.IdTipoAsignacion = tipoAsignacion;
		var asignaciones = [];
		var asigMap = {};
		$.each(result.P_OUT_SIMASIGNACION,function(key,value){
			
			var asig = {
					 CantidadAsignadaSimulacion 	: value.CantidadAsignadaSimulacion
					,CantidadTransaccion 			: value.CantidadTransaccionSimulacion
					,CodigoIFRS 					: value.CodigoIFRS	
					,CodigoPortafolio 				: value.CodigoPortafolio
					,CorteMinimo					: value.CorteMinimo					
					
					,DocumentoIdentidad 			: value.DocumentoIdentidad
					
					,EsFungible						: value.EsFungible
					,EsExtranjera					: value.EsExtranjera	
					,EstiloOpcion 					: value.EstiloOpcion
					
					,FechaTransaccion 				: value.FechaTransaccion
					,FolioCarteraAsignada 			: value.FolioCartera
					,FolioTransaccion 				: value.FolioTransaccion
					
					,Glosa 							: value.Glosa
					
					,IdBolsa 						: value.IdBolsa
					,IdBoveda 						: value.IdBoveda
					,IdClasificacionTipoInstrumento : value.IdClasificacionTipoInstrumento
					,IdCliente 						: value.IdCliente
					,IdCorredoraCorresponsal 		: value.IdCorredoraCorresponsal
					,IdCuentaCustodioPortafolio 	: value.IdCuentaCustodioPortafolio
					,IdFormaCustodia 				: value.IdFormaCustodia
					,IdInterno 						: value.IdInterno
					,IdInstrumento 					: value.IdInstrumento
					,IdMonedaInstrumento 			: value.IdMonedaInstrumento
					,IdMonedaLiquidacionOrden 		: value.IdMonedaLiquidacionOrden
					,IdMovimiento 					: value.IdMovimiento
					,IdOrden 						: value.IdOrden
					,IdPreferenciaPagoPortafolio	: value.IdPreferenciaPagoPortafolio
					,IdPortafolio 					: value.IdPortafolio
					,IdTipoInstrumento 				: value.IdTipoInstrumento
					,IdTransaccion					: value.IdTransaccion
					,IdTipoCanalIngreso				: value.IdTipoCanalIngreso
					,IdTipoOperacion				: value.IdTipoOperacion
					,IdTipoProducto					: value.IdTipoProducto
					,Instrumento 					: value.Instrumento
					
					,MontoAsignacion				: value.MontoAsignacion
					,MontoTransaccion				: value.MontoTransaccion
					,ModalidadEjercicio 			: value.ModalidadEjercicio
					,ModalidadLiquidacion 			: value.ModalidadLiquidacion
					
					,NombreCompleto 				: value.NombreCompleto
					
					,PagoCobroCuentaInversion 		: value.PagoCobroCuentaInversion
					,PlazoVentaCorta 				: Number(value.PlazoVentaCorta)
					,PrecioPromedio 				: 0
					,PrecioSimulacion 				: value.PrecioSimulacion
					,PrecioStrike 					: value.PrecioStrike
					
					,TasaVentaCorta 				: Number(value.TasaVentaCorta)
					,Tamano 						: value.Tamano
					,TipoCambio 					: Number(value.TipoCambio)
					,TipoOpcion 					: value.TipoOpcion
					
					,ValorRescateAsignadoSimulacion : value.ValorRescateAsignacion
					,ValorRescateMontoTransaccion	: value.ValorRescateMontoTransaccion
					
					,cobrosAsignacion 				: []
					,FechaVencimiento               : '0001-01-01'
					,InstrumentoNegociacionTran 	: value.InstrumentoNegociacionTran

					
					
			};
			IdTipoOperaciones.push(value.IdTipoOperacion);
			asigMap[value.IdInterno] = asig;
			asig.MontoAsignacion = asig.MontoTransaccion * asig.CantidadAsignadaSimulacion / asig.ValorRescateMontoTransaccion; 

			corteMinimo = asig.CorteMinimo;
			IdClasificacionTipoInstrumento = asig.IdClasificacionTipoInstrumento;
			
			asignaciones.push(asig);
				
		});
		$.each(result.P_OUT_COBROSASIGNACION,function(key,value){
			value.ValorCobro = Number(value.ValorCobro);
			value.ValorCobroAplicado = undefined;
			asigMap[value.IdInterno].cobrosAsignacion.push(value);
		});
		if (tipoAsignacion === 'ASIGNACION_MANUAL_CARTERAS'
			|| tipoAsignacion === 'ASIGNACION_MANUAL_CLIENTES'
			|| tipoAsignacion === 'ASIGNACION_MANUAL_ORDENES') {
				var currentAsignaciones = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
				for (var i = 0 ; i < asignaciones.length ; i++ ) {
					var asig = asignaciones[i];
					var f = _.findIndex(currentAsignaciones,function(curr){
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
	//termina asignacion
	
	$scope.formData.ejecutarAsignacion = function() {
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var a = $scope.formData.apiSimulacionAsignaciones.dataSource.data();
		if (a.length === 0) {
			bcsComunes.msg("error","UI_ERROR","UI_NO_HAY_ASIGNACIONES");
			return;
		}
				
		var asignacionesClientes = [];
		var asignacionesOrdenes = [];
		var cobrosModificados = [];
		$.each(a,function(key,value) {
			
			if (value.CantidadAsignadaSimulacion <= 0)
				return true;
			
			if (value.IdOrden === 0) {//asignacion normal
				asignacionesClientes.push( {
					IdInterno			: value.IdInterno,      
					FechaRecepcion		: util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate()),
					Glosa				: value.Glosa || '', 
					IdBoveda			: value.IdBoveda,
					IdFormaCustodia		: value.IdFormaCustodia,
					EsFacturaInmediata	: 'N',
					IdTransaccion		: value.IdTransaccion,
					FolioTransaccion	: value.FolioTransaccion,
					IdPortafolio		: value.IdPortafolio,
					Cantidad			: value.CantidadAsignadaSimulacion,
					ValorRescate		: value.ValorRescateAsignadoSimulacion,
					IdGrupo				: $scope.IdGrupoSeleccionado,
					FolioCartera		: value.FolioCarteraAsignada || 0,
					TasaVentaCorta		: value.TasaVentaCorta || 0,
					PlazoVentaCorta		: value.PlazoVentaCorta || 0,
					TipoCambio			: value.TipoCambio || 0,
					IdMonedaLiquidacionOrden	: value.IdMonedaLiquidacionOrden || null,
					PagoCobroCuentaInversion	: value.PagoCobroCuentaInversion,
					IdPreferenciaPagoPortafolio : value.IdPreferenciaPagoPortafolio,
					IdCuentaCustodioPortafolio : value.IdCuentaCustodioPortafolio || null,
					CodigoIFRS			: value.CodigoIFRS || '',
					IngresoDesde		: "COR"
					});
			}
			else {
				asignacionesOrdenes.push( {
					IdInterno		: value.IdInterno,
					IdOrden			: value.IdOrden,
					IdTransaccion	: value.IdTransaccion, 
					Cantidad		: value.CantidadAsignadaSimulacion,
					CodigoIFRS		: value.CodigoIFRS || '',
					ValorRescate 	: value.ValorRescateAsignadoSimulacion,
					FolioCartera 	: 0
				});
			}

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
		if(asignacionesClientes.length > 0){	
			for(var i = 0; i < tipoOperacionSobregirable.length; i++){
				for(var j = 0; j < IdTipoOperaciones.length; j++){
					if(tipoOperacionSobregirable[i] === IdTipoOperaciones[j]){
						OperacionSobregirable = 'S';
					}
				}
			}	
			if(OperacionSobregirable === 'S'){
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ORD',
					p_Ordenes : asignacionesClientes,
					p_CobrosModificados : cobrosModificados,
					p_simularOperacion : 'S'
				};
				var promiseSimulacion= {};
				promiseSimulacion = bcsServicioOrden.CrearOrdenesPostreras(datos).$promise;
				promiseSimulacion.then(function(result) {
					if(result.P_OUT_MENSAJES.length === 0 || result.P_OUT_ERRORESCONTADOR > 0){
						confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes);
					}else{
						var ClientesSobregirados = [];
						for(var i = 0; i < result.P_OUT_MENSAJES.length; i++){
							if(result.P_OUT_MENSAJES[i].codigo === "UI_ASIGNACION_SOBREGIRO"){
								ClientesSobregirados.push(result.P_OUT_MENSAJES[i].descripcion);
							}	
						}
						if(ClientesSobregirados.length >0){
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant(ClientesSobregirados.join(", ")+" "+$translate.instant('UI_CONTINUAR')), $scope.idVentana);
							t.then(function(result2){
								if (result2 === OPCIONES_BCSALERTA.SI) {
									confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes);
								}else{
									return;
								}
							});	
						}else{
							confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes);
						}
					}	
				},function(result){
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					bcsComunes.ventanaMsg({
							mensaje:'UI_ERROR_INTERNO_SISTEMA',
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);	
				});
			}else{
				confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes);
			}
		}else{
			confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes);
		}	
	};		
	
	$scope.formData.cargarOrdenes = function() {
		var transacciones = $scope.formData.apiTransacciones.dataSource.data();
		var Instrumentos = [];
		var InstrumentosHash = {};
		var InstrumentosSubyacente = [];
		var InstrumentosSubyacenteHash = {};
		var IdsMonedaTransaccion = [];
		var IdsMonedaTransaccionHash = {};
		var InstrumentosNegociacion = [];
		var InstrumentosNegociacionHash = {};
		let precioOrden = undefined;
		let fechaOperacion = undefined;
		$.each(transacciones,function(key,value){
			if (value.IdInstrumento && InstrumentosHash[value.IdInstrumento] !== 1) {
				Instrumentos.push(value.IdInstrumento);
				InstrumentosHash[value.IdInstrumento] = 1;
			}
			if (value.IdTipoInstrumento === 'SIM' && value.IdInstrumentoSubyacente && InstrumentosSubyacenteHash[value.IdInstrumentoSubyacente] !== 1 && value.IdTipoOperacion!=="COMSIM") {
				InstrumentosSubyacente.push(value.IdInstrumentoSubyacente);
				InstrumentosSubyacenteHash[value.IdInstrumentoSubyacente] = 1;
			}
			if (value.IdMonedaTransaccion && IdsMonedaTransaccionHash[value.IdMonedaTransaccion] !== 1) {
				IdsMonedaTransaccion.push(value.IdMonedaTransaccion);
				IdsMonedaTransaccionHash[value.IdMonedaTransaccion] = 1;
			}
			if (value.IdInstrumentoNegociacion && InstrumentosNegociacionHash[value.IdInstrumentoNegociacion] !== 1) {
				InstrumentosNegociacion.push(value.IdInstrumentoNegociacion);
				InstrumentosNegociacionHash[value.IdInstrumentoNegociacion] = 1;
			}
			//EN COMPRA QUE EL PRECIO EL MENOR Y EN VENTA EL NEOR
			if(!precioOrden ||  value.PrecioTransaccion < precioOrden|| value.PrecioTransaccion > precioOrden){
				precioOrden = value.PrecioTransaccion
			}
			
			//LA MAYOR FECHA HORA
			if((!fechaOperacion) || (value.FechaOperacionTransaccion > fechaOperacion)){
				fechaOperacion = value.FechaOperacionTransaccion;
			}
		})

		var parametros = {
				IdLibroOrdenes 					: $scope.configuracionInterfaz.IdLibroOrdenes,
				IdCondicionDeLiquidacion 		: $scope.configuracionInterfaz.IdCondicionDeLiquidacion,
				FechaOperacion 					: InstrumentosNegociacion.length < 2 ? fechaOperacion : undefined,
				PrecioOrden						: InstrumentosNegociacion.length < 2 ? precioOrden : undefined
		}
		
		if (Instrumentos.length > 0) {
			parametros.Instrumentos = Instrumentos;
		}
		if (InstrumentosSubyacente.length > 0) {
			parametros.InstrumentosSubyacente = InstrumentosSubyacente;
		}
		if (IdsMonedaTransaccion.length > 0) {
			parametros.IdsMonedaTransaccion = IdsMonedaTransaccion;
		}
		if (InstrumentosNegociacion.length > 0) {
			parametros.InstrumentosNegociacion = InstrumentosNegociacion;
		}


		var f = bcsConfiguracionOrdenesV2.validarFiltrosV2(parametros,"ORDEN","PORASIGNAR");
		var ds = bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesPorAsignar();
		ds.definition.filter = f;
		ds = new bcs.DataSource(ds);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		ds.fetch().then(function(){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (ds.data().length === 0) {
				bcsComunes.msg("error","error","NO_SE_ENCONTRARON_ORDENES");
			}
			else {
				$scope.formData.apiOrdenes.dataSource.data();
				agregarOrdenes(ds.data());
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		})
		
	}
	
	var recargarTransacciones = function() {
		
		//consultar por los mismos palos y ver si tienen saldo, si es asi se mantienene y se devuelven al metodo agregartrnsacciones
		
		var tranDS = bcsConfiguracionOrdenesV2.getDataSourceConsultarTransacciones();
		var transacciones = $scope.formData.apiTransacciones.dataSource.data();
		var filtros = [];
		$.each(transacciones,function(key,value){
			filtros.push({field : "IdTransaccion" , operator : "eq" , value : value.IdTransaccion});
		})
		tranDS.definition.filter = {
			logic : "or",
			filters : filtros
		};
		var tranDS = new bcs.DataSource(tranDS);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		tranDS.fetch().then(function(){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var trans1 = tranDS.data();
			var tranReloaded = [];
			$.each(trans1,function(key,value){
				if (value.IdClasificacionTipoInstrumento !== 'IF' && value.CantidadDisponibleTransaccion > 0) {
					tranReloaded.push(value);
				}
				if (value.IdClasificacionTipoInstrumento === 'IF' && value.ValorRescateDisponibleTransaccion > 0) {
					tranReloaded.push(value);
				}
			});
			
			limpiarGrids();
			if (tranReloaded.length > 0) {
				agregarTransacciones(tranReloaded);
			}
			else {
				setearConfiguracion();
				setEstadoAsignacion();
			}

		},function(){
			bcsComunes.ventanaOverlay($scope.idVentana,true);
		});
		
		
	}
	
	var limpiarGrids = function() {
		
		$scope.formData.apiTransacciones.dataSource.data([]);
		if ($scope.formData.apiAsignaciones) $scope.formData.apiAsignaciones.dataSource.data([]);
		if ($scope.formData.apiComprobantes) $scope.formData.apiComprobantes.dataSource.data([]);
		$scope.formData.apiClientes.dataSource.data([]);
		$scope.formData.apiOrdenes.dataSource.data([]);
		$scope.formData.apiCarteras.dataSource.data([]);
		$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
		recalcularDisponibles();
	
	};
	
	var promiseGridTransacciones = $q.defer();
	var promiseGridClientes = $q.defer();
	var promiseGridOrdenes = $q.defer();
	var promiseGridSimulacionAsignaciones = $q.defer();
	

	var inicializarGrillas = function() {
		//CONFIGURACION INICIAL GRILLAS
		$scope.formData.confTransacciones = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionTransacciones();
		$scope.formData.confTransacciones.gridCreated = function(grid) {
			promiseGridTransacciones.resolve();
		}
			
		$scope.formData.confOrdenes = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionOrdenes();
		$scope.formData.confOrdenes.gridCreated = function(grid) {
			promiseGridOrdenes.resolve();
		}
		
		$scope.formData.confClientes = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionClientesAsignaciones();
		$scope.formData.confClientes.gridCreated = function(grid) {
			promiseGridClientes.resolve();
		}
		$scope.formData.confClientes.save = function(e) {
			if (e.values.PorcentajeSimulacion > 100) {
				bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_SOBREPASA_100");
				e.preventDefault();
			}
			if (e.values.PorcentajeSimulacion < 0) {
				bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_NEGATIVO");
				e.preventDefault();
			}
		};
		
		$scope.formData.confCarteras = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionCarteras();
		
		$scope.formData.confSimulacionAsignaciones = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionAsignaciones();
		$scope.formData.confSimulacionAsignaciones.gridCreated = function(grid) {
			$scope.formData.apiSimulacionAsignaciones = grid;
			promiseGridSimulacionAsignaciones.resolve();
		}

		$scope.formData.confComprobantes = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionComprobantes();
		$scope.formData.confComprobantes.dataSource.definition.data =  [];
		$scope.formData.confAsignaciones = bcsConfiguracionOrdenesV2.getConfiguracionSimulacionAsignacion();
		$scope.formData.confAsignaciones.dataSource.definition.data = [];
		$scope.formData.confSimulacionAsignaciones.save = function(e) {
			recalcularDisponibles(e);
			if(IdClasificacionTipoInstrumento == 'IF' || IdClasificacionTipoInstrumento == 'RF'){
				if(validarCartera(e.values.CantidadAsignadaSimulacion, corteMinimo) != true){
					e.preventDefault();
				}else{
					e.model.ValorRescateAsignadoSimulacion = e.values.CantidadAsignadaSimulacion;
					recalcularMontoAsignacion(e.values, e.model);
				}
			}else{
				recalcularMontoAsignacion(e.values, e.model);
			}
		};
	}	

	var setearColumnas = function() {

	};
	
	//setea cabeceras
	var setearConfiguracion = function(transaccion) {
		if (transaccion === undefined) {
			$scope.configuracionInterfaz = {};
			return;
		}
		$scope.configuracionInterfaz = {
				FechaLiquidacion : transaccion.FechaLiquidacion,
				FechaOperacionString : util.formatoDate(transaccion.FechaOperacionTransaccion),
				FechaOperacion : transaccion.FechaOperacionTransaccion,
				IdLibroOrdenes : Number(transaccion.IdLibroOrdenes),
				NombreLibroOrdenes : transaccion.NombreLibroOrdenes,
				IdTipoLibroOrden : transaccion.IdTipoLibroOrden,				
				IdCondicionDeLiquidacion : transaccion.IdCondicionDeLiquidacion,
				NombreCondicionDeLiquidacion : transaccion.NombreCondicionDeLiquidacion
				
		};		
	};
	
	//RF CARTERA, POSTRERA
	//IF CARTERA, POSTRERA
	//DV CRONOLOGICA, PROPORCIONAL,MANUAL,POSTRERA
	//SIM POSTRERA MANUAL
	//OTRAS RV MANUAL, POSTRERA,PRECIO PROMEDIO, CRONOLOGICA, PROPORCIONAL
	
	//habilita pestañas de asignaciones y habilita algoritmos de asignacion
	var setEstadoAsignacion = function() {
		
		
		$scope.formData.tabOrdenesDeshabilitada 			= false;
		$scope.formData.tabCarterasDeshabilitada 			= false;
		$scope.formData.tabClientesActiva 					= true;
	};
	
	$scope.formData.cargarArchivoOpas = function(tipoAsignacion){
		
		var palos = $scope.formData.apiTransacciones.dataSource.data();
		if (palos.length > 1) {
			bcsComunes.msg("error","UI_ERROR","UI_OPA_SOLO_PERMITE_UNA_TRANSACCION");
			return;
		}
		if (palos[0].IdTipoLibroOrden != 'OPA') {
			bcsComunes.msg("error","UI_ERROR","UI_TRANSACCION_NO_ES_TIPO_OPA");
			return;
		}

		var datosArchivo = {codigoArchivo: 'ARCHIVO_ASIGNACION_OPA', titulo: 'Carga de Asignaciones'};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,$scope.idVentana);
		
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){
			if (!data || !data.fileData || !data.fileData[0])
				return;

			var lineas = [];
			var i = 0;
			$.each(data.fileData,function(key,val){
				if (val.MENSAJEVALIDACION !== 'OK') return;
				lineas.push({
						IdInterno : i++,
						OPARUT: val.OPARUT.trim(),
						OPACODIGOPORTAFOLIO: val.OPACODIGOPORTAFOLIO,
						OPACANTIDAD: val.OPACANTIDAD
				});
			});
			var datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdTransaccion: palos[0].IdTransaccion,
					p_IdModulo: 'ORD',
					p_DataArchivo: lineas
			}
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			bcsServicioOrden.ObtenerDatosDesdeArchivoOPA(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					
					bcsComunes.ventanaMsg({
						mensaje: "Error al cargar la informacion",
						conflictos: util.parseServerResponse(result)
					},$scope.idVentana);
					return;
				}
				else {
					$scope.formData.apiClientes.dataSource.data([]);
					$scope.formData.apiOrdenes.dataSource.data([]);
					$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
					
					$scope.formData.apiClientes.dataSource.data(_.map(result.P_OUT_CLIENTES,function (value) { return value;}));
					procesarResultadoSimulacion(result,'ASIGNACION_MANUAL_CLIENTES');
					recalcularDisponibles();
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,true);
				bcsComunes.ventanaMsg({
					mensaje: "Error al cargar la informacion",
					conflictos: util.parseServerResponse(result)
				},$scope.idVentana)
				console.log(result);
			})
		});
	};
	
	$scope.formData.cargarArchivo = function(tipoAsignacion){
		var codArch = '';
		var titulo  = '';
		if(tipoAsignacion.toUpperCase() === 'ASIGNACION_MANUAL'){codArch = 'ARCHIVO_ASIGNACION_MANUAL'; titulo = 'Manual'}
		else if(tipoAsignacion.toUpperCase() === 'ASIGNACION_POSTRERA'){codArch = 'ARCHIVO_ASIGNACION_POSTRERA'; titulo = 'Postrera'}
		else return;
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Asignación '+titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,$scope.idVentana);
		
		var promesa=bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				return;
			}
			
			if(tipoAsignacion.toUpperCase() === 'ASIGNACION_POSTRERA'){
				for(var key in data.fileData){
					data.fileData[key].ORDEN = 0;
    			}
			}
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdModulo			= 'ORD';
	        parametrosSP.p_IdTipoAsignacion	= tipoAsignacion.toUpperCase();
	        parametrosSP.p_DataArchivo		= data.fileData;
	        
	        if(data.fileData.length){
	        	bcsServicioOrden.ObtenerDatosDesdeArchivoAsignacion(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error","La operacion tiene errores");
	        		}
	        		else{
	        			// se limpian las grillas
	        			$scope.formData.apiTransacciones.dataSource.data([]);
	        			$scope.formData.apiClientes.dataSource.data([]);
        				$scope.formData.apiOrdenes.dataSource.data([]);
        				$scope.formData.apiSimulacionAsignaciones.dataSource.data([]);
        				setearConfiguracion();
        				setEstadoAsignacion();
        				
        				// se valida la salida
	        			if (!result.P_OUT_TRANSACCIONES || !result.P_OUT_TRANSACCIONES[0] ||
	        				(tipoAsignacion.toUpperCase() === 'ASIGNACION_MANUAL'   && (!result.P_OUT_ORDENES  || !result.P_OUT_ORDENES[0]))  ||
	        				(tipoAsignacion.toUpperCase() === 'ASIGNACION_POSTRERA' && (!result.P_OUT_CLIENTES || !result.P_OUT_CLIENTES[0])) ||	
	        				!result.P_OUT_ASIGNACION || !result.P_OUT_ASIGNACION[0]) return;
	        			
	        			for(var key in result.P_OUT_TRANSACCIONES){
	        				$scope.formData.apiTransacciones.dataSource.add(result.P_OUT_TRANSACCIONES[key]);
	        			}
	        			
	        			if(tipoAsignacion.toUpperCase() === 'ASIGNACION_MANUAL'){
	        				for(var key in result.P_OUT_ORDENES){
		        				$scope.formData.apiOrdenes.dataSource.add(result.P_OUT_ORDENES[key]);
		        			}
	        			}
	        			else if(tipoAsignacion.toUpperCase() === 'ASIGNACION_POSTRERA'){
	        				for(var key in result.P_OUT_CLIENTES){
		        				$scope.formData.apiClientes.dataSource.add(result.P_OUT_CLIENTES[key]);
		        			}
	        			}
	        			else return;
	        			
	        			for(var key in result.P_OUT_ASIGNACION){
	        				$scope.formData.apiSimulacionAsignaciones.dataSource.add(result.P_OUT_ASIGNACION[key]);
	        			}
	        			
	        			setearColumnas();
	        			recalcularDisponibles();
	        			
        				bcsComunes.msg("info","info","Datos cargados a Grillas.");
	        		}
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay($scope.idVentana,false);
		}); 
	};
	
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
		$.each($scope.formData.apiClientes.dataSource.data(),function(key,value) {
			clientesGrupo.push({
				IdPortafolio :  value.IdPortafolio,
				Porcentaje : value.PorcentajeSimulacion
			});
		});
		
		var datosUpdate= {
				p_IdUsuario					: $scope.SesionIdUsuario,
				p_IdEmpresa 				: $scope.SesionIdEmpresa,
				p_IdModulo					: 'ORD',
				p_IdGrupo					: $scope.formData.grupoPortafoliosSeleccionado.IdGrupo,
				p_tablaClientesPortafolio	: clientesGrupo
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = bcsServicioOrden.ActualizarGrupoPortafolio(datosUpdate);
		post.$promise.then(function(result){
			bcsComunes.msg("info", "UI_INFORMACION", "UI_GRUPO_ACTUALIZADO");
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		},function(result){
			bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},false,false,idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		})
		
		
		
	};
	
	$scope.formData.crearGrupo = function() {
		
		
		if ($scope.formData.apiClientes.dataSource.data().length === 0) {
			bcsComunes.msg("error", "UI_ERROR", "UI_NO_HAY_CLIENTES");
			return;
		}
		
		var clientesGrupo = [];
		$.each($scope.formData.apiClientes.dataSource.data(),function(key,value) {
			clientesGrupo.push({
				IdCliente 			:  value.IdCliente,
				DocumentoIdentidad 	:  value.DocumentoIdentidad,
				NombreCompleto 		:  value.NombreCompleto,
				IdPortafolio 		:  value.IdPortafolio,
				EstadoPortafolio 	:  'ACTIVO',
				CodigoPortafolio 	:  value.CodigoPortafolio,
				IdMoneda			:  value.IdMoneda,
				Porcentaje 			: value.PorcentajeSimulacion
			});
		});

		var id1 = bcsComunes.ventanaAbrir("ModificarGrupoPortafolio", {Modo : "NUEVO",clientes : clientesGrupo},false,false,$scope.idVentana);
		var promesa=bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			$scope.formData.filtrosGrupoPortafolio.read = !$scope.formData.filtrosGrupoPortafolio.read;
		});
		
	};
	
	
	$scope.formData.cargarGrupoPortafolios = function(grupoPortafoliosSeleccionado) {
		
		if ($scope.formData.apiTransacciones.dataSource.data().length === 0) {
			bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_TRANSACCIONES");
			return;
		}

		if (!grupoPortafoliosSeleccionado) {
			bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_GRUPO");
			return;
		}
		
		var confClientes = bcsConfiguracionOrdenesV2.getDataSourcePortafoliosGrupo();
		confClientes.definition.filter = [
			{ field: "IdGrupo", operator: "eq", value: grupoPortafoliosSeleccionado.IdGrupo }
		];
		if (grupoPortafoliosSeleccionado.IdGrupo){
			$scope.IdGrupoSeleccionado = grupoPortafoliosSeleccionado.IdGrupo;
		}
		
		var dsClientes = new bcs.DataSource(confClientes);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		dsClientes.fetch().then(function(result){
			var resultado = {};
			resultado.P_OUT_ERRORESCONTADOR = 0;
			resultado.P_OUT_MENSAJES = [];
			$.each(dsClientes.data(),function(key,value) {
				if (value.IdLibroOrdenes != $scope.configuracionInterfaz.IdLibroOrdenes) {
					bcsComunes.msg("warning", "UI_ADVERTENCIAS", "LIBRO_ORDENES_DE_TRANSACCION_SE_ACTUALIZARA");
				}
			});

			bcsComunes.ventanaOverlay($scope.idVentana,false);
				
			if (resultado.P_OUT_ERRORESCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje:'CLIENTE_NO_PERTENECE_LIBRO_ORDENES',
					conflictos:util.parseServerResponse(resultado)
				},$scope.idVentana);
			}
			else {
				agregarClientes(dsClientes.data());
			}
		},function(){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
	}
	
	var setearFormaAsignacion = function(tipo,origen) {
		if (!tipo || tipo === 'NORMAL') {
			$scope.formData.formaAsignacion = 'NORMAL';
		}
		else if (tipo === 'REFACTURACION') {
			$scope.formData.formaAsignacion = 'REFACTURACION';
		}
		else if (tipo === 'REASIGNACION') {
			$scope.formData.formaAsignacion = 'REASIGNACION';
		}
		$scope.formData.origenAsignacion = origen;
	}
	
	var goo = function() {

		//goo
		var parametros = bcsComunes.ventanaParams($scope.idVentana);	
		setearFormaAsignacion(parametros.Tipo,parametros.Origen);	//NORMAL, REFACTURACION, REASIGNACION
		setearConfiguracion();
		//LECTURA DE DATOS DE ENTRADA
		if (parametros.transacciones) {
			agregarTransacciones(parametros.transacciones);
		}
		else if (parametros.movimientos && parametros.movimientos.length > 0) {
			
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			var datos = {
					P_IDEMPRESA : $scope.SesionIdEmpresa,
					P_IDUSUARIO : $scope.SesionIdUsuario,
					P_IDMODULO : 'ORD',
					P_USAREGLASNEGOCIO : 'N',
					P_ORIGEN : $scope.formData.origenAsignacion,
					P_MOVIMIENTOS : parametros.movimientos
				};
			bcsServicioOrden.ObtenerDetalleRefacturacion(datos).$promise.then(function(result){
				var transacciones = [];
				var comprobantes = [];
				var asignaciones = [];
				$.each(result.P_OUT_TRANSACCIONES,function(key,value) {
					value.IdMovimiento = value.IdMovimiento;
					value.CantidadDisponibleTransaccion = Number(value.CantidadRefacturar);
					value.CantidadSimulacionTransaccion = Number(value.CantidadSimulacionTransaccion);
			        value.ValorRescateDisponibleTransaccion = Number(value.ValorRescateRefacturar); 
			        value.ValorRescateSimulacionTransaccion = Number(value.ValorRescateSimulacionTransaccion);
			        value.MontoDisponibleTransaccion = Number(value.MontoRefacturar);
			        value.FechaOperacionTransaccion = new Date(value.FechaOperacionTransaccion);
			        value.FechaVencimiento = new Date(value.FechaVencimiento);
			        transacciones.push(value);
				});
				$.each(result.P_OUT_COMPROBANTES,function(key,value) {
					value.MontoCobroComprobante = Number(value.MontoCobroComprobante);
					value.MontoComprobante = Number(value.MontoComprobante);
					value.MontoFinal = Number(value.MontoFinal);
					comprobantes.push(value);
				});
				$.each(result.P_OUT_ASIGNACIONES,function(key,value) {
					value.CantidadAsignada = Number(value.CantidadAsignada);
					value.MontoAsignado = Number(value.MontoAsignado);
					value.MontoAsignadoLiquidacion = Number(value.MontoAsignadoLiquidacion);
					value.MontoAsignadoLocal = Number(value.MontoAsignadoLocal);
					value.MontoAsignadoPlazo = Number(value.MontoAsignadoPlazo);
					value.PrecioAsignacion = Number(value.PrecioAsignacion);
					value.Tasa = Number(value.Tasa);
					value.ValorRescateAsignado = Number(value.ValorRescateAsignado);
					value.FechaAsignacion = new Date(value.FechaAsignacion);
					value.FechaOperacionTransaccion = new Date(value.FechaOperacionTransaccion);
					value.FechaOperacionOrden = new Date(value.FechaOperacionOrden);
					asignaciones.push(value);
				});
				if (transacciones[0]) {
					setearConfiguracion(transacciones[0]);
					setEstadoAsignacion();
				}
				if ($scope.formData.apiTransacciones)
					$scope.formData.apiTransacciones.dataSource.data(transacciones || []);
				else
					$scope.formData.confTransacciones.dataSource.definition.data =  [];

				if ($scope.formData.apiComprobantes)
					$scope.formData.apiComprobantes.dataSource.data(comprobantes || []);
				else
					$scope.formData.confComprobantes.dataSource.definition.data =  [];

				if ($scope.formData.apiAsignaciones)
					$scope.formData.apiAsignaciones.dataSource.data(asignaciones || []);
				else
					$scope.formData.confAsignaciones.dataSource.definition.data =  [];

				bcsComunes.ventanaOverlay($scope.idVentana,false);
			});
			setEstadoAsignacion();
		}
		else {
			//ERROR, DEBE RECIBIR TRANSACCION O MOVIMIENTOS
		}
		
	}
	
			// funcion que llama a la carga de archivos
	$scope.cargarArchivoInterfazAsignacion = function() {
		console.log("Entro");
		var datosArchivo = {codigoArchivo : 'CARGA_INTERFAZ_ASIGNACION'};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true,true);
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data) {
			if (!data || !data.fileData || !data.fileData[0]){
				return;
			}
			var linea = [];
			var error = 0;
			for(asignacionInfo of data.fileData){
				if (asignacionInfo.MENSAJEVALIDACION !== 'OK'){
					error = 1;
				} else{
					linea.push({
						FolioTransaccion : asignacionInfo.FOLIOTRANSACCION,
						
						FechaTransaccion : asignacionInfo.FECHATRANSACCION,
						Nemotecnico : asignacionInfo.NEMOTECNICO,
						
						RutCliente : asignacionInfo.RUTCLIENTE,
						CodigoPortafolio: asignacionInfo.CODIGOPORTAFOLIO ,
						CantidadAsignar : asignacionInfo.CANTIDADAASIGNAR,
						
						
					});
				}
			};
			var datos = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'ORD',
				p_UsaReglasNegocio: 'S',
				p_IdTipoAsignacion: 'ASIGNACION_POSTRERA',
				p_IdTipo: 'NORMAL',
				p_Origen: 'ASIG',
				p_AsignacionMasiva:linea
			};
			
			console.log('DATOS: ' + JSON.stringify(datos));
			
			if (error != 0){
				bcsComunes.ventanaMsg({
		            mensaje:$translate.instant("Debe cargar nuevamente el archivo, Con el Formato correcto"),
	//		            conflictos:util.parseServerResponse()
	                },$scope.idVentana);	
			}else{
				bcsComunes.ventanaOverlay($scope.idVentana,true);
				bcsServicioOrden.AsignacionMasivaPorArchivo(datos).$promise.then(function(result){					
					console.log('RESULT: ' + JSON.stringify(result));
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("Error: Debe cargar nuevamente el archivo, error en el procesamiento del archivo"),
							conflictos:util.parseServerResponse(result)
							},$scope.idVentana);
						bcsComunes.ventanaAbrir('VentanaResultado',{ 
							usaNuevosMensajes : true ,
							titulo: $translate.instant('Interfaz Asignacion'), 
							campoSecuencia : "id" ,
							nombreSecuencia : $translate.instant('UI_SECUENCIA'),
							datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
						//bcsComunes.ventanaCerrar($scope.idVentana);
						console.log(result);
					}else{

						bcsComunes.ventanaMsg({
						mensaje:$translate.instant("Proceso realizado"),
						conflictos:util.parseServerResponse(result)
						},$scope.idVentana);

						bcsComunes.ventanaAbrir('VentanaResultado',{ 
							usaNuevosMensajes : true ,
							titulo: $translate.instant('Asignaciones Enviadas a Facturación'), 
							campoSecuencia : "id" ,
							nombreSecuencia : $translate.instant('UI_SECUENCIA'),
							datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);							
					
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
								titulo: $translate.instant('Interfaz Asignacion'), 
								campoSecuencia : "id" ,
								nombreSecuencia : $translate.instant('UI_SECUENCIA'),
								datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
							bcsComunes.ventanaOverlay($scope.idVentana,false);
							//bcsComunes.ventanaCerrar($scope.idVentana);
							console.log(result);
				});
			}

			
		});	

		
	}

	function confirmarOrden(asignacionesClientes, cobrosModificados, asignacionesOrdenes){		
		var promise = {};
		if (asignacionesClientes.length > 0) {
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_Ordenes : asignacionesClientes,
				p_CobrosModificados : cobrosModificados,
			};
		
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			promise = bcsServicioOrden.CrearOrdenesPostreras(datos).$promise;
		}
		else if (asignacionesOrdenes.length > 0) {
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_Asignaciones : asignacionesOrdenes
			};		
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			promise = bcsServicioOrden.CrearAsignaciones(datos).$promise;
		}
		else {
			bcsComunes.msg("error","UI_ERROR","UI_NO_HAY_ASIGNACIONES");
			return;
		}
	
		promise.then(function(result) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			if (result.P_OUT_ERRORESCONTADOR > 0) {
				if ($scope.formData.formaAsignacion && ($scope.formData.formaAsignacion === 'REFACTURACION' || $scope.formData.formaAsignacion === 'REASIGNACION')){
					bcsComunes.ventanaMsg({
						mensaje:'UI_ERROR_AL_CREAR_ASIGNACIONES',
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
				}
				else {
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADO_CREAR_ASIGNACIONES', campoSecuencia : "id" ,nombreSecuencia : 'UI_SECUENCIA',datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
				}
			}
			else {
				var operaciones = [];
				if ($scope.formData.formaAsignacion && ($scope.formData.formaAsignacion === 'REFACTURACION' || $scope.formData.formaAsignacion === 'REASIGNACION')){
					var NotasDeCreditoGeneradas = [];
					var ComprobantesAnulados = [];
					var MovimientoComprobanteAnulados = [];
					var AsignacionesAnuladas = [];
					var OrdenesAnuladas = [];
					var OrdenesGeneradas = [];
					var AsignacionesGeneradas = [];
					var MovimientoComprobanteGenerados = [];
					var ComprobantesGenerados = [];
					$.each(result.P_OUT_NOTASDECREDITOGENERADAS,function(key,value) {
						NotasDeCreditoGeneradas.push(value);
					});
					$.each(result.P_OUT_COMPROBANTESANULADOS,function(key,value) {
						ComprobantesAnulados.push(value);
					});
					$.each(result.P_OUT_MOVIMIENTOCOMPROBANTEANULADOS,function(key,value) {
						MovimientoComprobanteAnulados.push(value);
					});
					$.each(result.P_OUT_ASIGNACIONESANULADAS,function(key,value) {
						AsignacionesAnuladas.push(value);
					});
					$.each(result.P_OUT_ORDENESANULADAS,function(key,value) {
						OrdenesAnuladas.push(value);
					});
					$.each(result.P_OUT_ORDENESGENERADAS,function(key,value) {
						OrdenesGeneradas.push(value);
					});
					$.each(result.P_OUT_ASIGNACIONESGENERADAS,function(key,value) {
						AsignacionesGeneradas.push(value);
					});
					$.each(result.P_OUT_MOVIMIENTOCOMPROBANTEGENERADOS,function(key,value) {
						MovimientoComprobanteGenerados.push(value);
					});
					$.each(result.P_OUT_COMPROBANTESGENERADOS,function(key,value) {
						ComprobantesGenerados.push(value);
					});
					
					var promesa = bcsComunes.ventanaAbrir("DetalleRefacturacion", {NotasDeCreditoGeneradas : NotasDeCreditoGeneradas
							, ComprobantesAnulados : ComprobantesAnulados, MovimientoComprobanteAnulados : MovimientoComprobanteAnulados
							, AsignacionesAnuladas : AsignacionesAnuladas, OrdenesAnuladas : OrdenesAnuladas, OrdenesGeneradas : OrdenesGeneradas
							, AsignacionesGeneradas : AsignacionesGeneradas, MovimientoComprobanteGenerados : MovimientoComprobanteGenerados
							, ComprobantesGenerados : ComprobantesGenerados} ,false,false,$scope.idVentana);
						
					bcsComunes.ventanaPromesa(promesa).then(function(result){
						limpiarGrids();
						setearConfiguracion();
						setEstadoAsignacion();
					})
				}
				else {	
					if ($scope.formData.muestraResultado === true) {			
						$.each(result.p_out_Resultado,function(key,value) {
							operaciones.push({id: value.IdAsignacion , resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_ASIGNACION_CREADA')});
						});
							
						var promesa = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
							tituloVentana : $translate.instant("UI_ASIGNACIONES"), 
							nombreEntidad : $translate.instant("UI_ASIGNACION"), 
							operaciones : operaciones   
						},false,false,$scope.idVentana);
							
						bcsComunes.ventanaPromesa(promesa).then(function(result){
							recargarTransacciones();
						})
					}
					else {
						bcsComunes.ventanaMsg({
							mensaje:'UI_ASIGNACION_CREADAS',
							conflictos:util.parseServerResponse(result)
						},$scope.idVentana);
							recargarTransacciones();
						}
				}
			}
				
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
				mensaje:'UI_ERROR_INTERNO_SISTEMA',
				conflictos:util.parseServerResponse(result)
			},$scope.idVentana);	
		});
	} 

	//RF CARTERA, POSTRERA
	//IF CARTERA, POSTRERA
	//DV CRONOLOGICA, PROPORCIONAL,MANUAL,POSTRERA
	//SIM POSTRERA MANUAL
	//OTRAS RV MANUAL, POSTRERA,PRECIO PROMEDIO, CRONOLOGICA, PROPORCIONAL
	
	function validarCartera(cantAsig, cortMin){		
		
		if(cantAsig >= cortMin)
		{
			var resto = Math.floor(cantAsig % cortMin);
			
			if(resto == 0)
			{
				return true;
			}
			else
			{
				bcsComunes.msg("error","UI_ERROR","Cantidad Simulación no es múltiplo del Corte Mínimo");
				return false;
			}	
		}else
		{
			bcsComunes.msg("error","UI_ERROR","Cantidad Simulación menor al Corte Mínimo");
			return false;
		}		
	  
	} 	
	
	$scope.formData.previsualizarPostrera = function() {
		$scope.formData.previsualizar('ASIGNACION_POSTRERA');
	}
	$scope.formData.previsualizarPrecioPromedio = function() {
		
		$scope.formData.previsualizar('ASIGNACION_PRECIO_PROMEDIO');
	}	
	$scope.formData.previsualizarCartera = function() {
		$scope.formData.previsualizar('ASIGNACION_CARTERAS');
	}
	
	$scope.formData.previsualizarProporcional = function() {
		$scope.formData.previsualizar('ASIGNACION_PROPORCIONAL');		
	}
	
	$scope.formData.previsualizarCronologica = function() {
		$scope.formData.previsualizar('ASIGNACION_CRONOLOGICA');		
	}
	
	$scope.formData.previsualizarManualOrdenes = function() {
		$scope.formData.previsualizar('ASIGNACION_MANUAL_ORDENES');		
	}
	
	$scope.formData.previsualizarManualCarteras = function() {
		$scope.formData.previsualizar('ASIGNACION_MANUAL_CARTERAS');
	}
	
	$scope.formData.previsualizarManualClientes = function() {
		$scope.formData.previsualizar('ASIGNACION_MANUAL_CLIENTES');
	}

	$scope.formData.opcionesClientes = [
		{ titulo : $translate.instant("UI_POSTRERA"), funcion : $scope.formData.previsualizarPostrera },
		{ titulo : $translate.instant("UI_PRECIO_PROMEDIO"), funcion : $scope.formData.previsualizarPrecioPromedio }
	]
	
	$scope.formData.opcionesCartera = [
		{ titulo : $translate.instant("UI_CARTERA"), funcion : $scope.formData.previsualizarCartera }
	]
	
	$scope.formData.opcionesOrdenes = [
		{ titulo : $translate.instant("UI_CRONOLOGICA"), funcion : $scope.formData.previsualizarCronologica },
		{ titulo : $translate.instant("UI_PROPORCIONAL"), funcion : $scope.formData.previsualizarProporcional },
		{ titulo : $translate.instant("UI_MANUAL"), funcion : $scope.formData.previsualizarManualOrdenes }
	]
	
	inicializarGrillas();

	$q.all([promiseGridTransacciones.promise,promiseGridClientes.promise,promiseGridOrdenes.promise,promiseGridSimulacionAsignaciones.promise]).then(function(result){
	 	goo();
	 });
	
});
