angular.module('ModuloOrdenes').controller('AsignacionGrupoCtrl',function ($scope,$rootScope,$q,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConsultasOrdenes,bcsConfiguracionOrdenesV2,$translate,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA) {
	
	//TIENEN QUE LLEGAR TRANSACCIONES DE UN LIBRO, UNA CLASIFICACION, Y UNA MARCA (O MARCA VACIA)
	var defer = $q.defer();
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.transacciones = parametros.transacciones;
	$scope.esAsignacionGrupoPrecioPromedio = parametros.esAsignacionGrupoPrecioPromedio ? parametros.esAsignacionGrupoPrecioPromedio : false;
	$scope.IdBolsa = parametros.bolsa;
	$scope.IdBoveda = parametros.boveda;
	$scope.IdModeloCustodia = parametros.modeloCustodia;
	$scope.IdClasificacionTipoInstrumento = parametros.mercado;
	$scope.form = {};
	$scope.cliente = {};
	$scope.form.agregando = false;
	$scope.form.modificando = false;
	$scope.form.editable = false;
	$scope.form.esTransaccionExtranjera=false;
	$scope.asignaciones = {};
	$scope.formatoCarga = "RUT,CODIGO PORTAFOLIO,CANTIDAD O PORCENTAJE";
	$scope.IdGrupoSeleccionado = null
	$scope.form.tipoCambio=0;
	$scope.form.CodigoIFRS='';
	$scope.form.IdLibroOrdenes = $scope.transacciones[0].IdLibroOrdenes;
	$scope.form.EsVentaCorta = $scope.transacciones[0].EsVentaCorta === "S";
	$scope.form.FechaInicial = $scope.transacciones[0].FechaOperacionTransaccion;
	$scope.form.IdCondicionDeLiquidacion = $scope.transacciones[0].IdCondicionDeLiquidacion;
	$scope.form.IdClasificacionTipoInstrumento = $scope.transacciones[0].IdClasificacionTipoInstrumento;
	$scope.form.IdTipoOperacion = $scope.transacciones[0].IdTipoOperacion;
	$scope.form.IdMoneda = $scope.transacciones[0].IdMonedaTransaccion;
	$scope.form.decimales = util.bcs_formato(0,'GEN','Cantidad',$scope.form.IdMoneda,'','').cantidadDecimales;
	$scope.form.IdsPreferenciasPagoPortafolio = {};
	$scope.limiteDeAsignaciones = 0;
	var OperacionSobregirable = 'N';
	var tipoOperacionSobregirable = [];
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.OperacionesNegativas', $scope.SesionIdEmpresa, 'CUSTODIA')
	 .then(function (result) {
		tipoOperacionSobregirable = result.Valor.split(",").map(item => item.trim()) 
	 });

	if ($scope.form.EsVentaCorta) {
		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.UsaFechaLiquidacionEnVencimiento', $scope.SesionIdEmpresa, 'PLATAFORMA')
				.then(function (parametroUsaFechaLiquidacion) {
					if (parametroUsaFechaLiquidacion.Valor === 'S') {
						$scope.form.FechaInicial = $scope.transacciones[0].FechaLiquidacion;
					}
				});
	}

	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('LimiteDeAsignacionesPorVentana', $scope.SesionIdEmpresa, 'ORDENES')
				.then(function (result) {
					$scope.limiteDeAsignacionesPorVentana = result.Valor;
					
				});
	
	bcsConfiguracionOrdenes.CONFIGURACION.promise.then(function(result){
		$scope.form.idBovedaDefecto = bcsConfiguracionOrdenes.CONFIGURACION.IdBovedaDefecto;
	});
	
	$scope.form.filtrosGrupo = {
			IdEmpresa : { field :"IdEmpresa", operator : "eq", value :$scope.SesionIdEmpresa},
			read : true
	}
	$scope.form.columnaMontoActiva = false;
	$scope.form.tipoPegadoDefecto = "CANTIDAD";
	if ($scope.form.IdClasificacionTipoInstrumento === "RV" &&!($scope.form.EsVentaCorta)) {
		$scope.form.columnaMontoActiva = true;
		$scope.formatoCarga = "RUT,CODIGO PORTAFOLIO,CANTIDAD O PORCENTAJE O MONTO";
	}
	if (!$scope.form.columnaMontoActiva) {

		const elemento = document.getElementById('UiMonto');
		elemento.style.display = 'none';
	}
	
	$scope.form.TipoPegadoCombobox =$scope.form.columnaMontoActiva?"combobox-tipo-Pegado-Monto":"combobox-tipo-Pegado";
	$scope.form.cantidadPorAsignar = 0;
	$scope.form.cantidadAsignado = 0;
	$scope.form.montoPorAsignar = 0;
	$scope.form.montoAsignado = 0;

	var marcas = _.groupBy($scope.transacciones, function(tran){ 
		if (tran.MarcaNegociacion.trim() === "") return "$VACIO";
		return tran.MarcaNegociacion; 
	});
	if (marcas.$VACIO)
		delete marcas.$VACIO;
	if (Object.keys(marcas).length === 1) {
		$scope.form.MarcaNegociacion = Object.keys(marcas)[0];
	}
	
	$.each($scope.transacciones,function(key,value){
		if (value.IdClasificacionTipoInstrumento === "IF") {
			$scope.form.cantidadPorAsignar += value.ValorRescateDisponibleTransaccion;
		}
		else {
			$scope.form.cantidadPorAsignar += value.CantidadDisponibleTransaccion;
			$scope.form.montoPorAsignar += value.MontoDisponibleTransaccion;
		}

	}); 
	
	defer.promise.then(function(){
	
		if ($scope.form.MarcaNegociacion) {
			
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			bcsConfiguracionOrdenes.FUNCIONES.getGrupoPorMarca($scope.form.MarcaNegociacion).then(function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				if (result.length === 0) {
					bcsComunes.msg("warning","ADVERTENCIA","NO_SE_ENCONTRO_GRUPO_ASOCIADO");
					return;
				}
				console.log(result);
				$scope.form.idGrupoDefecto = result[0].IdGrupo;
				$scope.form.idGrupoActual = result[0].IdGrupo;
				$scope.form.cargarGrupo(result[0].IdGrupo);
			},function(){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			});
			
			
		}
		
		
	});
	
	$scope.form.filtrosCuentaCustodio = {
			IdPortafolio 			: { "field"   : "IdPortafolio","operator": "eq", "value"   : "" },
			IdEstadoAutorizacion 	: { "field"   : "IdEstadoAutorizacion","operator": "neq", "value"   : "RZD" },
			IdEstado 				: { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
	}
	
	$scope.form.filtrosBoveda = {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
			IdBoveda : { "field"   : "IdBoveda","operator": "eq", "value"   : "" }
	}
	
	//cuando cambie el cliente cargo los indicadores
	$scope.$watch('cliente.portafolioSeleccionado.IdPortafolio',function(newval,oldval){
		if (!$scope.cliente.portafolioSeleccionado || !$scope.cliente.portafolioSeleccionado.IdPortafolio) {
			return;
		}


		//CARGA DE INDICADORES
		var idsPortafolio = [$scope.cliente.portafolioSeleccionado.IdPortafolio];
		bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios(idsPortafolio).then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			$scope.indicadores = result[0];
			if(!!$scope.indicadores){
				if ($scope.indicadores.OperaCuentaInversion === "S") {
					$scope.form.PagoCobroCuentaInversionDisabled = false;
					$scope.cliente.PagoCobroCuentaInversion = true;
				}
				else {
					$scope.form.PagoCobroCuentaInversionDisabled = true;
					$scope.cliente.PagoCobroCuentaInversion = false;
				}
				
				if (!$scope.form.EsVentaCorta){
					if ($scope.form.IdClasificacionTipoInstrumento === "RV" && $scope.indicadores.OperaCustodiaRV === "S") {
						$scope.cliente.IdFormaCustodia = "CUS";
					}
					else if ($scope.form.IdClasificacionTipoInstrumento === "RF" && $scope.indicadores.OperaCustodiaRFIF === "S") {
						$scope.cliente.IdFormaCustodia = "CUS";
					}
					else if ($scope.form.IdClasificacionTipoInstrumento === "IF" && $scope.indicadores.OperaCustodiaRFIF === "S") {
						$scope.cliente.IdFormaCustodia = "CUS";
					}
					else {
						$scope.cliente.IdFormaCustodia = "NOM";
					}					
				}
				else {
					$scope.cliente.IdFormaCustodia = "NOM";
				}



				if($scope.indicadores.CodigoIfrs && $scope.cliente.portafolioSeleccionado.EsCuentaPropia==='S'){
					$scope.form.CodigoIFRS = $scope.indicadores.CodigoIfrs;
				}
			}
			
		});


	});
	
	$scope.form.inicioModificarPortafolio = function() {
		$scope.form.modificando = true;
		$scope.form.editable = true;
		$scope.form.esTransaccionExtranjera= parametros.transacciones[0].IdTipoInstrumento==="ACX"?true:false;
		var seleccionado = $scope.form.clienteSeleccionado;
		$scope.cliente.IdPortafolio = seleccionado.IdPortafolio;
		$scope.lineaSeleccionada = seleccionado;
		if (seleccionado.OperaCuentaInversion === "S") {
			$scope.form.PagoCobroCuentaInversionDisabled = false;
			$scope.cliente.PagoCobroCuentaInversion = true;
		}
		else {
			$scope.form.PagoCobroCuentaInversionDisabled = true;
			$scope.cliente.PagoCobroCuentaInversion = false;
		}
		
		if (!$scope.form.EsVentaCorta){
			if ($scope.form.IdClasificacionTipoInstrumento === "RV" && seleccionado.OperaCustodiaRV === "S") {
				$scope.cliente.IdFormaCustodia = "CUS";
			}
			else if ($scope.form.IdClasificacionTipoInstrumento === "RF" && seleccionado.OperaCustodiaRFIF === "S") {
				$scope.cliente.IdFormaCustodia = "CUS";
			}
			else if ($scope.form.IdClasificacionTipoInstrumento === "IF" && seleccionado.OperaCustodiaRFIF === "S") {
				$scope.cliente.IdFormaCustodia = "CUS";
			}
			else {
				$scope.cliente.IdFormaCustodia = "NOM";
			}			
		} else{
			$scope.cliente.IdFormaCustodia = "NOM";
		}

	
		$scope.cliente.IdCuentaCustodioPortafolio = seleccionado.IdCuentaCustodioPortafolio;
		if (seleccionado.IdCuentaCustodioPortafolio === undefined && seleccionado.IdBoveda !== undefined) {
			$scope.cliente.IdBoveda = seleccionado.IdBoveda;
		}
		$scope.cliente.PlazoVentaCorta = seleccionado.PlazoVentaCorta;
		$scope.cliente.TasaVentaCorta = seleccionado.TasaVentaCorta;
		
		$scope.apiGrid.clearSelection();
		
	}

	$scope.form.modificarPortafolio = function() {
		var repetido = undefined;
		$.each($scope.apiGrid.dataSource.data(),function(key,value){
			if (value.IdPortafolio === $scope.cliente.portafolioSeleccionado.IdPortafolio && value!==$scope.lineaSeleccionada) {
				repetido = true;
			}
		});
		if (repetido) {
			bcsComunes.msg("error","ERROR","UI_CLIENTE_YA_AGREGADO");
			return false;
		}
		$.each($scope.apiGrid.dataSource.data(),function(key,value){
			if ($scope.lineaSeleccionada === value) {
				value.IdSocioDeNegocio 				= $scope.cliente.portafolioSeleccionado.IdSocioDeNegocio;
				value.IdPortafolio 					= $scope.cliente.portafolioSeleccionado.IdPortafolio;
				value.IdCliente	 					= $scope.cliente.portafolioSeleccionado.IdCliente;
				value.DocumentoIdentidad 			= $scope.cliente.portafolioSeleccionado.DocumentoIdentidad;
				value.NombreCompleto 				= $scope.cliente.portafolioSeleccionado.NombreCompleto;
				value.CodigoPortafolio 				= $scope.cliente.portafolioSeleccionado.CodigoPortafolio;
				value.Cantidad 						= $scope.lineaSeleccionada.Cantidad;
				value.Porcentaje 					= $scope.lineaSeleccionada.Porcentaje;
				value.Monto 						= $scope.lineaSeleccionada.Monto;
				value.IdMoneda						= $scope.form.IdMoneda;
				value.TipoCambio					= $scope.form.tipoCambio || 0,
				value.IdBoveda				 		= $scope.cliente.bovedaSeleccionada.IdBoveda;
				value.IdCuentaCustodioPortafolio	= $scope.cliente.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio;
				value.NombreLargoCuentaCustodio		= $scope.cliente.cuentaCustodioSeleccionada.KdataTextTemplate;
				value.PagoCobroCuentaInversion		= $scope.cliente.PagoCobroCuentaInversion?"S":"N";
				value.IdFormaCustodia				= $scope.cliente.formaCustodiaSeleccionada.IdFormaCustodiaOrden;
				value.TasaVentaCorta				= $scope.cliente.TasaVentaCorta;
				value.PlazoVentaCorta				= $scope.cliente.PlazoVentaCorta;
				value.CodigoIFRS					= $scope.form.CodigoIFRS || '';
				return false;
			}
			
		});
		
		$scope.apiGrid.refresh();
		$scope.form.limpiar();
		$scope.form.modificando = false;
		$scope.form.editable = false;
		$scope.lineaSeleccionada = undefined;
	}

	$scope.form.agregarPortafolio = function() {
		
		var ports = $scope.apiGrid.dataSource.data();
		var idsPortafolio = [];
		for (i = 0;i<ports.length;i++) {
			if ($scope.cliente.portafolioSeleccionado.IdPortafolio === ports[i].IdPortafolio) {
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_PORTAFOLIO_YA_AGREGADO");
				return;
			}
		}
		
		var obj =  {
			IdSocioDeNegocio 	: $scope.cliente.portafolioSeleccionado.IdSocioDeNegocio,
			IdPortafolio 		: $scope.cliente.portafolioSeleccionado.IdPortafolio,
			IdCliente	 		: $scope.cliente.portafolioSeleccionado.IdCliente,
			DocumentoIdentidad 	: $scope.cliente.portafolioSeleccionado.DocumentoIdentidad,
			NombreCompleto 		: $scope.cliente.portafolioSeleccionado.NombreCompleto,
			CodigoPortafolio 	: $scope.cliente.portafolioSeleccionado.CodigoPortafolio,
			IdCuentaCustodioPortafolio	: $scope.cliente.cuentaCustodioSeleccionada.IdCuentaCustodioPortafolio,
			NombreLargoCuentaCustodio	: $scope.cliente.cuentaCustodioSeleccionada.KdataTextTemplate,
			Cantidad 			: 0,
			Porcentaje 			: 0,
			Monto				: 0,
			TipoCambio 			: $scope.form.tipoCambio || 0,
			IdBoveda			: $scope.cliente.bovedaSeleccionada.IdBoveda,
			PagoCobroCuentaInversion : $scope.cliente.PagoCobroCuentaInversion?"S":"N",
			IdFormaCustodia		: $scope.cliente.formaCustodiaSeleccionada.IdFormaCustodiaOrden,
			TasaVentaCorta		: $scope.cliente.TasaVentaCorta,
			PlazoVentaCorta		: $scope.cliente.PlazoVentaCorta,
			IdMoneda			: $scope.form.IdMoneda,
			IdMonedaInstrumento	: $scope.form.IdMoneda,
			CodigoIFRS			: $scope.form.CodigoIFRS || null
		}
		idsPortafolio.push(obj.IdPortafolio);
		$scope.form.ConsultaPreferenciaPagoPortafolio(idsPortafolio);
		$scope.form.agregando = false;
		$scope.form.editable = false;
		$scope.apiGrid.dataSource.add(obj);
		$scope.apiGrid.refresh();
		$scope.form.limpiar();
	}
	
	$scope.form.quitarPortafolio = function() {
		if (!$scope.form.clienteSeleccionado)
			return;
		$scope.apiGrid.dataSource.remove($scope.form.clienteSeleccionado);
		$scope.apiGrid.clearSelection();
	}
	
	$scope.form.limpiar = function() {
		$scope.cliente.IdPortafolio = false;
		$scope.cliente.IdFormaCustodia = false;
		$scope.cliente.IdCuentaCustodioPortafolioActual = false;
		$scope.cliente.IdBoveda = false;
		$scope.cliente.PagoCobroCuentaInversion = false;
		$scope.cliente.TasaVentaCorta = undefined;
		$scope.cliente.PlazoVentaCorta = undefined;
		$scope.apiGrid.clearSelection();
		$scope.form.tipoCambio=0;
		$scope.form.CodigoIFRS='';
	}
	

	$scope.form.pegarPortapapeles = function (event) {
		// console.log(event);
		var arrPortafolios = [];
		var arrValorPegar = [];
		try {
			var lineas = event.originalEvent.clipboardData.getData('text/plain').split('\r\n');
			tieneComa = lineas.some(texto => texto.includes(','));
			if (tieneComa) {
				bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_PEGADO_CON_COMA");
			return;
			}
			$.each(lineas,function(key,val){
				if (val.trim().length === 0) return true;
				var obj = {};
				obj.Porcentaje = 0;
				obj.Cantidad = 0;
				obj.Monto = 0;
				var campos = val.split('\t');
				if (campos.length != 3 && campos.length != 1) {
					return;
				}
				
				
				if(campos.length === 3){
					//RUT,CODIGO PORTAFOLIO,VALOR
					obj.DocumentoIdentidad = campos[0];
					obj.CodigoPortafolio = campos[1];
					if ($scope.form.idTipoPegado == "PORCENTAJE") {
						obj.Porcentaje = Number(campos[2]);
					} else if ($scope.form.idTipoPegado == "MONTO") {
						obj.Monto = Number(campos[2]);
					} else {
						obj.Cantidad = Number(campos[2]);
					}
					arrPortafolios.push(obj);
				}
				if (campos.length === 1 && !isNaN(campos[0])) {
					//COPIADO DE COLUMNA, SOLO UN VALOR
					if ($scope.form.idTipoPegado == "PORCENTAJE") {
						obj.Porcentaje = Number(campos[0]);
					} else if ($scope.form.idTipoPegado == "MONTO") {
						obj.Monto = Number(campos[0]);
					} else {
						obj.Cantidad = Number(campos[0]);
					}
					arrValorPegar.push(obj);
				}
			})
		}
		catch(e) {
			console.log("Error al pegar del Portapales");
			console.warn(e);
		}

		if (arrPortafolios.length === 0 && arrValorPegar.length === 0) {
			bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_PEGADO_EXCEL");
			return;
		}
        var temp = [];
		var hash = {};
		var portafoliosNuevos = [];
		var portafoliosNuevosObj = [];
		var hashPortafoliosNuevos = {};
		var portafoliosActuales = {};
		
		if(arrPortafolios.length > 0){
			$.each($scope.apiGrid.dataSource.data(),function(key,value){
				portafoliosActuales[value.DocumentoIdentidad + "-" + value.CodigoPortafolio] = value;
			});
			
			$.each(arrPortafolios,function(key,value2){
				if (portafoliosActuales[value2.DocumentoIdentidad + "-" + value2.CodigoPortafolio] === undefined) {
					portafoliosNuevos.push({ field :"DocumentoIdentidad", operator : "eq", value : value2.DocumentoIdentidad});
					portafoliosNuevosObj.push({DocumentoIdentidad : value2.DocumentoIdentidad, CodigoPortafolio : value2.CodigoPortafolio});
					hashPortafoliosNuevos[value2.DocumentoIdentidad + "-" + value2.CodigoPortafolio] = 0;
				}

			});
			
			var arrDocumentosIdentidad = [];
			$.each(arrPortafolios,function(key,value) {
				arrDocumentosIdentidad.push({
					DocumentoIdentidad :  value.DocumentoIdentidad,
				});
			});
			
            var datosClientes = {
                p_IdUsuario					: $scope.SesionIdUsuario,
                p_IdEmpresa 				: $scope.SesionIdEmpresa,
                p_IdModulo					: 'ORD',
                p_Identidades               : arrDocumentosIdentidad
            };

			var defer = $q.defer();
			var idsPortafolio = [];
			
			if (portafoliosNuevos.length > 0) {
                var promesaServicioConsultarClientesLibroOrdenes = bcsServicioOrden.ConsultarClientesLibroOrdenes(datosClientes).$promise;                
                bcsComunes.ventanaOverlay($scope.idVentana,true);
                promesaServicioConsultarClientesLibroOrdenes.then(function(result){
                    bcsComunes.ventanaOverlay($scope.idVentana,false);
                    var cargaIndicadores = [];  
                    var arrPortafoliosInvalidos = portafoliosNuevosObj;
                    $.each(result.P_OUT_CLIENTESLIBRO, function(key,value){
                        if (hashPortafoliosNuevos[value.DocumentoIdentidad+"-"+value.CodigoPortafolio] !== 0)
                            return;
                         arrPortafoliosInvalidos = arrPortafoliosInvalidos.filter(function(item){
				               return item.DocumentoIdentidad !== value.DocumentoIdentidad && item.CodigoPortafolio !== value.CodigoPortafolio
			               })
                        cargaIndicadores.push(value.IdPortafolio);
                        var obj = {
							IdSocioDeNegocio 	: value.IdSocioDeNegocio,
							IdPortafolio 		: value.IdPortafolio,
							IdCliente			: value.IdCliente,
							DocumentoIdentidad 	: value.DocumentoIdentidad,
							NombreCompleto 		: value.NombreCompleto,
							CodigoPortafolio 	: value.CodigoPortafolio,
							Cantidad 			: 0,
							Porcentaje 			: 0,
							Monto: 0,
							TipoCambio			: $scope.form.tipoCambio,
							IdBoveda			: $scope.form.idBovedaDefecto,
							PagoCobroCuentaInversion : "N",
							IdFormaCustodia		: "",
							TasaVentaCorta		: 0,
							PlazoVentaCorta		: 0,
							IdMoneda			: $scope.form.IdMoneda,
							CodigoIFRS			: $scope.form.CodigoIFRS
                        }
                        $scope.apiGrid.dataSource.add(obj);
                        idsPortafolio.push(value.IdPortafolio);
                    });    
                    $scope.form.ConsultaPreferenciaPagoPortafolio(idsPortafolio);
                    if (arrPortafoliosInvalidos.length > 0) { 
                         var nIdMensaje = 0;
                         $.each(arrPortafoliosInvalidos , function(key,v) {
                            result.P_OUT_ERRORESCONTADOR++;
                            nIdMensaje++;
					        result.P_OUT_MENSAJES.push({id: nIdMensaje, tipo: "ERROR", codigo: "PORTAFOLIO_INVALIDO", descripcion: $translate.instant("UI_CLIENTE") + ": " + v.DocumentoIdentidad + ", " + $translate.instant("UI_PORTAFOLIO") + ": " + v.CodigoPortafolio});
                            
                        });
                        if (result.P_OUT_ERRORESCONTADOR > 0){
                        bcsComunes.ventanaMsg({
							mensaje:'UI_ERROR_PORTAFOLIO_INVALIDO',
							conflictos:util.parseServerResponse(result)
							},$scope.idVentana);
                        }
                    } 
                    if (cargaIndicadores.length > 0)
						$scope.form.cargaIndicadores(cargaIndicadores);
					defer.resolve();              
                },function(result){
                    bcsComunes.msg("error","UI_ERROR","UI_ERROR_AL_OBTENER_DATOS");
                    bcsComunes.ventanaOverlay($scope.idVentana,false);
					defer.reject();
                })
			} else {
				defer.resolve();
			}
			
			//-----
			defer.promise.then(function(){
				$.each(arrPortafolios,function(key,value){
					var a = { Cantidad: value.Cantidad, Porcentaje: value.Porcentaje, Monto: value.Monto};
					temp.push(a);
				});

				if (!$scope.form.validacionSumaTotalPorColumna(temp)) return;

				$.each($scope.apiGrid.dataSource.data(),function(key,value){
					$.each(arrPortafolios,function(key2,value2){
						if (value2.DocumentoIdentidad === value.DocumentoIdentidad && value2.CodigoPortafolio === value.CodigoPortafolio) {
							if ($scope.form.idTipoPegado == "PORCENTAJE"){
								value.Porcentaje = value2.Porcentaje;
							}else if($scope.form.idTipoPegado == "MONTO"){
								value.Monto = value2.Monto;
							}else
								value.Cantidad = value2.Cantidad;
						}
					})
				});

				if ($scope.form.idTipoPegado == "PORCENTAJE"){
					$scope.form.actualizaColumnasEditablesEnFuncionA("porcentaje");
				}else if($scope.form.idTipoPegado == "MONTO"){
					$scope.form.actualizaColumnasEditablesEnFuncionA("monto");
				}else{
					$scope.form.actualizaColumnasEditablesEnFuncionA("cantidad");
				}
			});
		}
		
		if (arrValorPegar.length > 0) {
			var tempValores = [];
			$.each(arrValorPegar,function(key,value){
				var a = { Cantidad: value.Cantidad, Porcentaje: value.Porcentaje, Monto: value.Monto};
				tempValores.push(a);
			});
			
			if (!$scope.form.validacionSumaTotalPorColumna(tempValores)) return;
			
			var i = 0;
			$.each($scope.apiGrid.dataSource.data(),function(key,value){
				if (arrValorPegar.length>i) {
					if ($scope.form.idTipoPegado == "PORCENTAJE"){
						value.Porcentaje = arrValorPegar[i].Porcentaje;
					}else if($scope.form.idTipoPegado == "MONTO"){
						value.Monto = arrValorPegar[i].Monto;
					}else
					value.Cantidad = arrValorPegar[i].Cantidad;
					i++;
				}else{
					return;
				}
			});
			
			if ($scope.form.idTipoPegado == "PORCENTAJE"){
				$scope.form.actualizaColumnasEditablesEnFuncionA("porcentaje");
			} else if ($scope.form.idTipoPegado == "MONTO"){
				$scope.form.actualizaColumnasEditablesEnFuncionA("monto");
			} else {
				$scope.form.actualizaColumnasEditablesEnFuncionA("cantidad");
			}
		}

	}

	$scope.form.validacionSumaTotalPorColumna = function(listado) {
		var sumaCantidad = 0;
		var sumaPorcentajes = 0;
		var sumaMonto = 0;
		$.each(listado, function (key, value) {
			sumaCantidad+=value.Cantidad;
			sumaPorcentajes+=value.Porcentaje;
			sumaMonto += value.Monto;
		})
		if ($scope.form.idTipoPegado == "PORCENTAJE" && sumaPorcentajes > 100) {
			bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_SOBREPASA_100");
			return false;
		} else if ($scope.form.idTipoPegado == "CANTIDAD" && sumaCantidad > $scope.form.cantidadPorAsignar) {
			bcsComunes.msg("error","UI_ERROR","UI_CANTIDAD_SOBREPASA_DISPONIBLE");
			return false;
		} else if ($scope.form.idTipoPegado == "MONTO" && sumaMonto > $scope.form.montoPorAsignar) {
			bcsComunes.msg("error", "UI_ERROR", "UI_MONTO_SOBREPASA_DISPONIBLE");
			return false;
		}
		return true;
	}
	
	$scope.form.clicNuevoGrupo = function() {
		
		if ($scope.apiGrid.dataSource.data().length == 0)
			return;

		var clientesGrupo = [];
		$.each($scope.apiGrid.dataSource.data(),function(key,value) {
			clientesGrupo.push({
				IdCliente 			:  value.IdCliente,
				DocumentoIdentidad 	:  value.DocumentoIdentidad,
				NombreCompleto 		:  value.NombreCompleto,
				IdPortafolio 		:  value.IdPortafolio,
				IdMoneda			:	value.IdMoneda,
				EstadoPortafolio 	:  'ACTIVO',
				CodigoPortafolio 	:  value.CodigoPortafolio,
				Porcentaje 			: value.Porcentaje
			});
		});
		
		var id1 = bcsComunes.ventanaAbrir("ModificarGrupoPortafolio", {Modo : "NUEVO",clientes : clientesGrupo},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(result){
			if (result.IdGrupo) {
				$scope.form.filtrosGrupo.read = true;
				$scope.form.idGrupoDefecto = result.IdGrupo;
				$scope.form.idGrupoActual = result.IdGrupo;
			}
		},function(result){
		})
	}
	
	$scope.form.clicGuardarGrupo = function(idGrupo) {
		if (!$scope.form.idGrupoActual) {
			bcsComunes.msg("error", "UI_ERROR", "UI_DEBE_SELECCIONAR_GRUPO");
			return;
		}
		if ($scope.apiGrid.dataSource.data().length === 0) {
			bcsComunes.msg("error", "UI_ERROR", "UI_NO_HAY_PORTAFOLIOS");
			return;
		}
		
		var portafolios = [];
		$.each($scope.apiGrid.dataSource.data(),function(key,value) {
			portafolios.push({
				IdPortafolio :  value.IdPortafolio,
				Porcentaje : value.Porcentaje
			});
		});
		
		var datosUpdate= {
				p_IdUsuario					: $scope.SesionIdUsuario,
				p_IdEmpresa 				: $scope.SesionIdEmpresa,
				p_IdModulo					: 'ORD',
				p_IdGrupo					: $scope.form.idGrupoActual,
				p_tablaClientesPortafolio	: portafolios
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = bcsServicioOrden.ActualizarGrupoPortafolio(datosUpdate);
		post.$promise.then(function(result){
			bcsComunes.msg("info", "UI_INFORMACION", "UI_GRUPO_ACTUALIZADO");
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		},function(result){
			bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result)
			},$scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		})	
	}
	
	$scope.form.clicCargarGrupo = function(idGrupo) {
		$scope.form.cargarGrupo(idGrupo);
	}
	
	$scope.form.cargarGrupo = function(idGrupo){
		if (!idGrupo)
			return;
		
		var dataSourcedDef = bcsConfiguracionOrdenesV2.getDataSourcePortafoliosGrupo();
		dataSourcedDef.definition.filter = [
			{ field: "IdGrupo", operator: "eq", value: idGrupo }
			//{ field: "IdLibroOrdenes", operator: "eq", value: $scope.form.IdLibroOrdenes }
		];
		dsPortafolios = new bcs.DataSource(dataSourcedDef);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		dsPortafolios.fetch().then(function(){
			if (!dsPortafolios.data() || !dsPortafolios.data()[0]) {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				return;
			}
			
			var portafoliosNuevos = [];
			var idsPortafolio = [];
			
			var resultado = {};
			resultado.P_OUT_ERRORESCONTADOR = 0;
			resultado.P_OUT_MENSAJES = [];
			
			$.each(dsPortafolios.data(),function(key,value) {
				if (value.IdLibroOrdenes != $scope.form.IdLibroOrdenes) {
					bcsComunes.msg("warning", "UI_ADVERTENCIAS", "LIBRO_ORDENES_DE_TRANSACCION_SE_ACTUALIZARA");
				}
				
				var obj =  {
						IdSocioDeNegocio 	: value.IdSocioDeNegocio,
						IdPortafolio 		: value.IdPortafolio,
						IdCliente			: value.IdCliente,
						DocumentoIdentidad 	: value.DocumentoIdentidad,
						NombreCompleto 		: value.NombreCompleto,
						CodigoPortafolio 	: value.CodigoPortafolio,
						IdCuentaCustodioPortafolio : undefined,
						NombreLargoCuentaCustodio : undefined,
						Cantidad 			: 0,
						Porcentaje 			: value.Porcentaje,
						Monto				: 0,
						TipoCambio			: $scope.form.tipoCambio || 0,
						IdBoveda			: $scope.form.idBovedaDefecto,
						PagoCobroCuentaInversion : "N",
						IdFormaCustodia		: "",
						TasaVentaCorta		: 0,
						PlazoVentaCorta		: 0,
						IdMoneda			: $scope.form.IdMoneda,
						CodigoIFRS			: $scope.form.CodigoIFRS
					}
					idsPortafolio.push(value.IdPortafolio);
					portafoliosNuevos.push(obj);
			});
			
			$scope.form.ConsultaPreferenciaPagoPortafolio(idsPortafolio);
			
			if (idGrupo){
				$scope.IdGrupoSeleccionado = idGrupo;
			}
			if (resultado.P_OUT_ERRORESCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje:'CLIENTE_NO_PERTENECE_LIBRO_ORDENES',
					conflictos:util.parseServerResponse(resultado)
				},$scope.idVentana);
			}
			else {
				$scope.apiGrid.dataSource.data(portafoliosNuevos);
				
				$scope.form.actualizaColumnasEditablesEnFuncionA("porcentaje");

				$scope.form.cargaIndicadores(idsPortafolio);
			}
			
		},function(){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
	}
	
	$scope.form.cargaIndicadores = function(idsPortafolio) {
		
		var datosCons= {
				p_IdEmpresa 					: $scope.SesionIdEmpresa,
				p_IdUsuario						: $scope.SesionIdUsuario,
				p_IdModulo						: 'ORD',
				p_IdBolsa						: $scope.IdBolsa,
				p_IdModeloCustodia				: $scope.IdModeloCustodia,
				p_IdClasificacionTipoInstrumento: $scope.IdClasificacionTipoInstrumento,
				p_Portafolios					: idsPortafolio
		};
		var datosPortafolios= {
				p_IdEmpresa 					: $scope.SesionIdEmpresa,
				p_IdUsuario						: $scope.SesionIdUsuario,
				p_IdModulo						: 'ORD',
				p_Portafolios					: idsPortafolio	
		}
		var ctasCustodio;
		promise = bcsServicioOrden.ConsultarCtaCustodioDefectoPortafolio(datosCons).$promise;
		promise.then(function(result) {
			if (result.P_OUT_ERRORES_CONTADOR === 0) {
				ctasCustodio = result.P_OUT_CUENTASCUSTODIO;
                    
				//bcsConfiguracionOrdenes.FUNCIONES.getInformacionPortafolios(idsPortafolio).then(function(result){
				promise = bcsServicioOrden.ConsultarInformacionPortafolioOrdenes(datosPortafolios).$promise;
				promise.then(function(result) {	
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					$.each($scope.apiGrid.dataSource.data(),function(key,value){
						$.each(result.P_OUT_PORTAFOLIOSORDENES,function(key2,value2){
							if (value.IdPortafolio === value2.IdPortafolio) {

								value.OperaCuentaInversion = value2.OperaCuentaInversion;
								value.OperaCustodiaRV = value2.OperaCustodiaRV;
								value.OperaCustodiaRFIF = value2.OperaCustodiaRFIF;

								if (value.OperaCuentaInversion === "S")
									value.PagoCobroCuentaInversion = "S";
								else
									value.PagoCobroCuentaInversion = "N";

								if ($scope.form.IdClasificacionTipoInstrumento === "RV" && value.OperaCustodiaRV === "S") {
									value.IdFormaCustodia = "CUS";
								}
								else if ($scope.form.IdClasificacionTipoInstrumento === "RF" && value.OperaCustodiaRFIF === "S") {
									value.IdFormaCustodia = "CUS";
								}
								else if ($scope.form.IdClasificacionTipoInstrumento === "IF" && value.OperaCustodiaRFIF === "S") {
									value.IdFormaCustodia = "CUS";
								}
								else {
									value.IdFormaCustodia = "NOM";
								}
								if(value2.CodigoIfrs!==undefined){
									value.CodigoIFRS = value2.CodigoIfrs
								}
							}
						})

						$.each(ctasCustodio,function(key3,value3){
							if (value.IdPortafolio === value3.IdPortafolio) {
								value.IdCuentaCustodioPortafolio = value3.IdCuentaCustodioPortafolio;
								value.NombreLargoCuentaCustodio = value3.CuentaCustodioPortafolio;
								return false;
							}
						})

					});

				},function(){
					bcsComunes.ventanaOverlay($scope.idVentana,false);
				});


			}
			else
				bcsComunes.ventanaMsg({
					mensaje:'UI_ERROR_CARGAR_CUENTAS_CUSTODIO',
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:'UI_ERROR_CARGAR_CUENTAS_CUSTODIO',
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);

			
		});
		

	}
	$scope.form.actualizaColumnasEditablesEnFuncionA = function(columnaModificada) {
		if ($scope.apiGrid.dataSource.data().length === 0) return;
		$.each($scope.apiGrid.dataSource.data(),function(key,value){
			if (columnaModificada && columnaModificada !== "porcentaje") {
				var num = (new Big(100)).times(new Big(columnaModificada=="monto"? value.Monto:value.Cantidad)).div(new Big(columnaModificada=="monto"? $scope.form.montoPorAsignar:$scope.form.cantidadPorAsignar));
				value.Porcentaje = Number(num.toString());
			}
			if (columnaModificada && columnaModificada !== "cantidad") {
				value.Cantidad = Number(new Big(new Big($scope.form.cantidadPorAsignar).times(new Big(value.Porcentaje)).toFixed(0)).div(new Big(100)).toString());
			}
			if (columnaModificada && columnaModificada !== "monto" && $scope.form.columnaMontoActiva) {
				if($scope.form.IdMoneda== "USD"){
					value.Monto = Number(new Big(new Big($scope.form.montoPorAsignar).times(new Big(value.Porcentaje))).div(new Big(100)).toString());
				}else{
					value.Monto = Number(new Big(new Big($scope.form.montoPorAsignar).times(new Big(value.Porcentaje)).toFixed(0)).div(new Big(100)).toString());
				}
			}
		});
		$scope.form.ajustePrecisionDecimales();
		$scope.form.refreshTotal();
		$scope.form.ajusteValorCantidad();
		$scope.form.refreshTotal();
		$scope.apiGrid.refresh();
	}
	$scope.form.ajustePrecisionDecimales = function(){
		$.each($scope.apiGrid.dataSource.data(),function(key,value){
			value.Cantidad = value.IdMoneda != 'CLP' ? Number(new Big(value.Cantidad).toString()) : Number(new Big(value.Cantidad).round(10, 1).round(0, 1).toString());
			value.Porcentaje = Number(new Big(value.Porcentaje).toString());
			if($scope.form.IdMoneda== "CLP"){
				value.Monto = Number(new Big(value.Monto).round(10, 1).round(0, 1).toString());
			}
		});
	}
	$scope.form.ajusteValorCantidad = function(){
		if (Number(new Big($scope.form.porcentajeTotal).round(10, 1).round(0, 0).toString()) === Number(new Big(100))
				&& Number(new Big($scope.form.cantidadAsignado).round(10, 1).round(0, 0).toString()) !== Number(new Big($scope.form.cantidadPorAsignar))) {
			$scope.form.cantidadSuma = new Big(0);
			$scope.form.indicePorcentajeMaximo = -1;
			$.each($scope.apiGrid.dataSource.data(),function(key,value) {
				$scope.form.cantidadSuma = $scope.form.cantidadSuma.plus(new Big(value.Cantidad));
				if (new Big(value.Porcentaje) > new Big($scope.apiGrid.dataSource.data()[$scope.form.indicePorcentajeMaximo] ? $scope.apiGrid.dataSource.data()[$scope.form.indicePorcentajeMaximo].Porcentaje : 0)) {
					$scope.form.cantidadSuma = $scope.form.cantidadSuma.minus(new Big(value.Cantidad));
					if ($scope.form.indicePorcentajeMaximo !== -1) $scope.form.cantidadSuma = $scope.form.cantidadSuma.plus(new Big($scope.apiGrid.dataSource.data()[$scope.form.indicePorcentajeMaximo].Cantidad));
					$scope.form.indicePorcentajeMaximo = key;
				}
			});
			$scope.apiGrid.dataSource.data()[$scope.form.indicePorcentajeMaximo].Cantidad = Number(new Big($scope.form.cantidadPorAsignar).minus($scope.form.cantidadSuma));
		}
	}
	$scope.form.refreshTotal = function(){
		$scope.form.cantidadAsignado = 0;
		$scope.form.montoAsignado = 0;
		$scope.form.porcentajeTotal = new Big(0);
		$.each($scope.apiGrid.dataSource.data(),function(key,value){
			$scope.form.cantidadAsignado += value.Cantidad;
			if($scope.form.columnaMontoActiva)
				$scope.form.montoAsignado += value.Monto;
			$scope.form.porcentajeTotal =  $scope.form.porcentajeTotal.plus(value.Porcentaje);
		});
		$scope.form.porcentajeTotal = Number($scope.form.porcentajeTotal.round(10,1).toString());
	}
	$scope.form.asignar = function() {
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var saldoPortafolios = {};
		var saldoTransacciones = {};
		var asignacionesCalculadas = {};
		var saldoTotalPorAsignarPortafolios = new Big(0);
		var saldoTotalPorAsignarTransacciones = new Big(0);
		var monedaPortafolios = {};
		var detalle = {};
		var detalleTransacciones = {};

		$.each($scope.apiGrid.dataSource.data(),function(key,port){
			saldoPortafolios[port.IdPortafolio] = new Big(port.Cantidad);
			monedaPortafolios[port.IdPortafolio] = port.IdMoneda;
			saldoTotalPorAsignarPortafolios = saldoTotalPorAsignarPortafolios.plus(saldoPortafolios[port.IdPortafolio]);
		});

		$.each($scope.transacciones,function(key,tran){
			detalle.IdMonedaInstrumento = tran.IdMonedaInstrumento;
			detalle.IdTipoInstrumento = tran.IdTipoInstrumento;
			detalle.IdClasificacionTipoInstrumento = tran.IdClasificacionTipoInstrumento;
			detalleTransacciones[tran.IdTransaccion] = detalle;
			if (tran.IdClasificacionTipoInstrumento === "IF")
				saldoTransacciones[tran.IdTransaccion] = new Big(tran.ValorRescateDisponibleTransaccion);
			else
				saldoTransacciones[tran.IdTransaccion] = new Big(tran.CantidadDisponibleTransaccion);
			saldoTotalPorAsignarTransacciones = saldoTotalPorAsignarTransacciones.plus(saldoTransacciones[tran.IdTransaccion]);


		})
	
		do {
			var cantidadAsignaciones = 0;
			var porcentajesPorCliente = {};
			$.each(saldoPortafolios,function(keyPort,saldo){
				if (saldoTotalPorAsignarTransacciones.cmp(new Big(0)) === 0) {
					porcentajesPorCliente[keyPort] = new Big(0);
				}
				else {
					porcentajesPorCliente[keyPort] = saldo.div(saldoTotalPorAsignarTransacciones);
				}
			})

			var cantidadesPorCliente = {};
			$.each(saldoPortafolios,function(keyPort,saldo){
				var porcentaje = porcentajesPorCliente[keyPort];
				if (cantidadesPorCliente[keyPort] === undefined)
					cantidadesPorCliente[keyPort] = {};
				$.each(saldoTransacciones,function(keyTran,saldoTran){
					var idMonedaInstrumento = detalleTransacciones[keyTran].IdMonedaInstrumento;
					var idTipoInstrumento = detalleTransacciones[keyTran].IdTipoInstrumento;
					var idClasificacionTipoInstrumento = detalleTransacciones[keyTran].IdClasificacionTipoInstrumento;
					var _cantidadDecimales = util.bcs_formato(porcentaje.times(saldoTran),'GEN','Cantidad',idMonedaInstrumento,idTipoInstrumento,'').cantidadDecimales;
					if(monedaPortafolios[keyPort] !== 'CLP' && idClasificacionTipoInstrumento != 'RV'){
						cantidadesPorCliente[keyPort][keyTran] = porcentaje.times(saldoTran);
					}
					else if(monedaPortafolios[keyPort] !== 'CLP' && idClasificacionTipoInstrumento == 'RV') {
						cantidadesPorCliente[keyPort][keyTran] = porcentaje.times(saldoTran).round(10,1).round(_cantidadDecimales,0);
						
					}
					else{
						cantidadesPorCliente[keyPort][keyTran] = porcentaje.times(saldoTran).round(10,1).round(0,0);
					}
					})
			})

			$.each(cantidadesPorCliente,function(keyPort,port){
				$.each(port,function(keyTran,tran){
					if (asignacionesCalculadas[keyPort] === undefined)
						asignacionesCalculadas[keyPort] = {};
					if (asignacionesCalculadas[keyPort][keyTran] === undefined)
						asignacionesCalculadas[keyPort][keyTran] = new Big(0);
					if (!tran.eq(new Big(0))){
						//creamos asignacion.
						asignacionesCalculadas[keyPort][keyTran] = asignacionesCalculadas[keyPort][keyTran].plus(tran);
						cantidadAsignaciones++;

						//actualizamos los saldos
						saldoPortafolios[keyPort] = saldoPortafolios[keyPort].minus(tran);
						saldoTransacciones[keyTran] = saldoTransacciones[keyTran].minus(tran);
						saldoTotalPorAsignarPortafolios = saldoTotalPorAsignarPortafolios.minus(tran);
						saldoTotalPorAsignarTransacciones = saldoTotalPorAsignarTransacciones.minus(tran);
						
					}

				})
			})
		}
		while (cantidadAsignaciones > 0);

		//repartimos los conchos
		while (saldoTotalPorAsignarPortafolios.cmp(new Big(0)) === 1 && saldoTotalPorAsignarTransacciones.cmp(new Big(0)) === 1) {
			$.each(saldoPortafolios,function(keyPort,saldoPort){
				$.each(saldoTransacciones,function(keyTran,saldoTran){

					if (saldoTransacciones[keyTran].cmp(new Big(0)) === 1 && saldoPortafolios[keyPort].cmp(new Big(0)) === 1) {
						saldoTransacciones[keyTran] = saldoTransacciones[keyTran].minus(new Big(1));
						saldoPortafolios[keyPort] = saldoPortafolios[keyPort].minus(new Big(1))
						asignacionesCalculadas[keyPort][keyTran] = asignacionesCalculadas[keyPort][keyTran].plus(new Big(1));
						saldoTotalPorAsignarPortafolios = saldoTotalPorAsignarPortafolios.minus(new Big(1));
						saldoTotalPorAsignarTransacciones = saldoTotalPorAsignarTransacciones.minus(new Big(1));
					}

				});
			});
		}

		//si entra aqui, el algoritmo tiene un error, no alcanzo a asignar todos los clientes (el porcentaje supero el 100%)
		if (saldoTotalPorAsignarPortafolios.cmp(new Big(0)) === 1 || saldoTotalPorAsignarTransacciones.cmp(new Big(0)) === 0) {
			console.log("error?");
			//throw "error al calcular asignacion"
		}

		

		

	
		var asignaciones =   [];
		var i = 0;
		var portafolios = $scope.apiGrid.dataSource.data();
		$.each($scope.transacciones,function(key,tran){
			$.each(portafolios,function(key2,port){

				if (asignacionesCalculadas[port.IdPortafolio] !== undefined
				&& asignacionesCalculadas[port.IdPortafolio][tran.IdTransaccion] !== undefined
				&& asignacionesCalculadas[port.IdPortafolio][tran.IdTransaccion].cmp(new Big(0)) === 1) {
					var asig = {
						IdInterno					: i++,      
						FechaRecepcion				: util.dateSPseconddate(util.date($scope.SesionFechaSistema).hour(util.date(new Date()).hour()).minute(util.date(new Date()).minute()).second(util.date(new Date()).second()).toDate()),
						Glosa						: '', 
						IdBoveda					: port.IdBoveda,
						IdFormaCustodia				: port.IdFormaCustodia,
						EsFacturaInmediata			: 'N',
						IdTransaccion				: tran.IdTransaccion,
						FolioTransaccion			: tran.FolioTransaccion,
						IdPortafolio				: port.IdPortafolio,
						IdGrupo						: $scope.IdGrupoSeleccionado,
						FolioCartera				: 0,
						TipoCambio					: port.TipoCambio ||0,
						IdMonedaLiquidacionOrden 	: tran.IdMonedaTransaccion,
						IdCuentaCustodioPortafolio	: port.IdCuentaCustodioPortafolio,
						TasaVentaCorta				: port.TasaVentaCorta || 0,
						PlazoVentaCorta				: port.PlazoVentaCorta || 0,
						PagoCobroCuentaInversion 	: port.PagoCobroCuentaInversion,
						IdPreferenciaPagoPortafolio : ($scope.form.IdsPreferenciasPagoPortafolio[port.IdPortafolio] &&  $scope.form.IdsPreferenciasPagoPortafolio[port.IdPortafolio][tran.IdTransaccion]) ? $scope.form.IdsPreferenciasPagoPortafolio[port.IdPortafolio][tran.IdTransaccion] : null,
						CodigoIFRS					: port.CodigoIFRS || '',
						IngresoDesde		 		: "COR"
					};

					if (tran.IdClasificacionTipoInstrumento === "IF"){
						asig.Cantidad = 1;
						asig.ValorRescate = Number(asignacionesCalculadas[port.IdPortafolio][tran.IdTransaccion].toString());
					}
					else {
						asig.Cantidad = Number(asignacionesCalculadas[port.IdPortafolio][tran.IdTransaccion].toString());
						asig.ValorRescate = 0;
					}

					asignaciones.push(asig);
				}
								
			}) 
			
		})
		if (asignaciones === undefined || asignaciones.length === 0) {
			bcsComunes.msg("warning","UI_ADVERTENCIA","NO_SE_INGRESARON_ASIGNACIONES");
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			return;
		}

		for(var i = 0; i < tipoOperacionSobregirable.length; i++){
			if(tipoOperacionSobregirable[i] === $scope.form.IdTipoOperacion){
				OperacionSobregirable = 'S';
			}
		}
		if(OperacionSobregirable === 'S' && $scope.limiteDeAsignacionesPorVentana > asignaciones.length){ //Cuando el proceso pase por JOB no valida sobre giro
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'ORD',
				p_Ordenes : asignaciones,
				p_CobrosModificados : [],
				p_simularOperacion : 'S',
				p_AplicarComision: 'S',
				p_EnviarFacturarAutomatico: 'N',
				p_EsAsignacionMasivaPrecioPromedio: 'N',
				p_UsaLogDetallado: 'N'
				
			};
			var promiseSimulacion= {};
			var ClientesSobregirados = [];
			promiseSimulacion = bcsServicioOrden.ConsultarOrdenesConSobregiroCustodia(datos).$promise;
			promiseSimulacion.then(function(result) {
				if(result.P_OUT_MENSAJES.length === 0 || result.P_OUT_ERRORESCONTADOR > 0){
					confirmarOrden(asignaciones);
				}else{
					for(var i = 0; i < result.P_OUT_MENSAJES.length; i++){
						if(result.P_OUT_MENSAJES[i].codigo === "UI_ASIGNACION_SOBREGIRO"){
							ClientesSobregirados.push(result.P_OUT_MENSAJES[i].descripcion);
						}
					}
					if(ClientesSobregirados.length >0){
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant(ClientesSobregirados.join(" , ")+" "+$translate.instant('UI_CONTINUAR')), $scope.idVentana);
						t.then(function(result2){
							if (result2 === OPCIONES_BCSALERTA.SI) {
								confirmarOrden(asignaciones);
							}else{
								return;
							}
						});	
					}else{
						confirmarOrden(asignaciones);
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
			confirmarOrden(asignaciones);
		}
	}
	
	$scope.form.ConsultaPreferenciaPagoPortafolio = function(IdsPortafolio) {
		    var ConsultarProducto = new bcs.DataSource(ConsultarProductoTransaccion);
			var filterProducto = [];
			var transaccionesPosibles = [];
			if(IdsPortafolio){
				bcsComunes.ventanaOverlay($scope.idVentana,true);
			}
			var IdsTransaccion = $scope.transacciones.map(t => t.IdTransaccion);

			var filterProducto = {
			  logic: "or",
			  filters: IdsTransaccion.map(id => ({
			    field: "IdTransaccion",
			    operator: "eq",
			    value: id
			  }))
			};
			
			
			//Consultar producto y moneda transaccion
			ConsultarProducto.query({ filter: filterProducto }).then(function() {
		        var resultadoProducto = ConsultarProducto.data();
		        var combinaciones = new Set();
		
		        resultadoProducto.forEach(item => {
		            if (item.IdTipoOperacion && item.IdMonedaTransaccion) {
		                combinaciones.add(item.IdTipoOperacion + "|" + item.IdMonedaTransaccion);
		            }
		        });
		        
		        var transaccionesPosibles = Array.from(combinaciones).map(c => {
		            const [IdTipoOperacion, IdMonedaTransaccion] = c.split("|");
		            return { IdTipoOperacion, IdMonedaTransaccion };
		        });
				
				 //Consultar Preferencias
			    if(transaccionesPosibles.length > 0){
					var ConsultarPreferencia = new bcs.DataSource(ConsultarPreferenciaPagoPortafolio);
		            var filtrosPreferencia = [];
		            var filterPreferencia = [];
		            
		            $.each(transaccionesPosibles, function(index, tran) {
						
						IdsPortafolio.forEach(idportafolio => {
					        filtrosPreferencia.push({
					            logic: "and",
					            filters: [
					                { field: "IdPortafolio", operator: "eq", value: idportafolio },
					                { field: "IdTipoOperacion", operator: "eq", value: tran.IdTipoOperacion },
					                { field: "PorDefecto", operator: "eq", value: "S" },
					                { field: "IdMoneda", operator: "eq", value: tran.IdMonedaTransaccion }
					            ]
					        });
					    });
					    
					    filterPreferencia = {
					        logic: "or",
					        filters: filtrosPreferencia
					    };
					});
					
					ConsultarPreferencia.query({ filter: filterPreferencia }).then(function() {
						var preferenciasP = ConsultarPreferencia.data();
						
						if(preferenciasP){
							$.each(IdsPortafolio, function(index, idpor) {
								$.each(resultadoProducto, function(index, tran) {
							        if ($scope.form.IdsPreferenciasPagoPortafolio[idpor] === undefined) {
							            $scope.form.IdsPreferenciasPagoPortafolio[idpor] = {};
							        }
							
							        var prefPortafolio = preferenciasP.find(t =>
							            t.IdTipoOperacion === tran.IdTipoOperacion &&
							            t.IdMonedaTransaccion === tran.IdMoneda &&
							            t.IdPortafolio === idpor
							        );
							
							        if (prefPortafolio) {
							            if ($scope.form.IdsPreferenciasPagoPortafolio[idpor][tran.IdTransaccion] === undefined) {
							                $scope.form.IdsPreferenciasPagoPortafolio[idpor][tran.IdTransaccion] = new Big(0);
							            }
							
							            $scope.form.IdsPreferenciasPagoPortafolio[idpor][tran.IdTransaccion] = prefPortafolio.IdPreferenciaPagoPortafolio;
							        }
								 });
							});
							bcsComunes.ventanaOverlay($scope.idVentana,false);
						}else{
							bcsComunes.ventanaOverlay($scope.idVentana,false);
						}
					});
				}
		    });
		    
		    bcsComunes.ventanaOverlay($scope.idVentana,false);  
	};

	var ConsultarPreferenciaPagoPortafolio = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarPreferenciaPagoPortafolio'),
					dataType : "json"
				}
			},
			schema 	: {
				model	: {
					fields	: {
						IdPreferenciaPagoPortafolio : { type: "number"}
					}
				}
			}
		}	
	});
	
	var ConsultarProductoTransaccion = new bcs.DataSource({
		type: "odata",
		definition 	: {
			serverFiltering : true,
			transport : {
				read : {
					url :bcsConsultasOrdenes('ConsultarTransaccionesUI'),
					dataType : "json"
				}
			},
			schema 	: {
				model	: {
					fields	: {
						IdTipoOperacion : { type: "string"}
					}
				}
			}
		}	
	});
	
	$scope.form.configuracionGridClientes = {
			autoBind: true,
      		allowCopy: true,
      		height:400,
			reorderable: true,
			resizable: true,
      		selectable: "row",
      		scrollable: true,
      		sortable: true,
      		userSettings:true,
			navigatable: true,
      		gridCreated : function(grid) {
      			$scope.apiGrid = grid;
      			defer.resolve();
      		},
      		save : function(e) {
      			e.preventDefault();
      			if (e.values.Porcentaje !== undefined) {
      				if(e.values.Porcentaje > 100)
      					e.values.Porcentaje = 100;
      				//calculamos cuanto hay asignado en las filas superiores
      				var sumaSuperior = 0;
      				for (var i = 0; i < $scope.apiGrid.dataSource.data().indexOf(e.model);i++) {
						sumaSuperior+=$scope.apiGrid.dataSource.data()[i].Porcentaje;
      				}

      				var disponible = 100 - sumaSuperior;
      				if (e.values.Porcentaje > disponible)
      					e.values.Porcentaje = disponible;

					var sumaPorcentajes = 0;
					$.each($scope.apiGrid.dataSource.data(),function(key,value) {
						if (value === e.model) {
							sumaPorcentajes+=e.values.Porcentaje;
						}
						else {
							sumaPorcentajes+=value.Porcentaje;
						}
					});

					var remanente = sumaPorcentajes - 100;
					if (remanente > 0) {
						var index = $scope.apiGrid.dataSource.data().indexOf(e.model);
						for (var i = $scope.apiGrid.dataSource.data().length-1; i > index;i--) {
							if (remanente === 0) 
								break;
							var p = $scope.apiGrid.dataSource.data()[i].Porcentaje;
							if (p > remanente) {
								$scope.apiGrid.dataSource.data()[i].Porcentaje = p-remanente;
								remanente = 0;
							}
							else {
								remanente = remanente - p;
								$scope.apiGrid.dataSource.data()[i].Porcentaje = 0;

							}

						}
					}

					e.model.Porcentaje = e.values.Porcentaje;
					$scope.form.actualizaColumnasEditablesEnFuncionA("porcentaje");
      			}
      			else if (e.values.Cantidad !== undefined) {
      				
					var sumaCantidad = 0;
					$.each($scope.apiGrid.dataSource.data(),function(key,value) {
						if (value === e.model) {
							sumaCantidad+=e.values.Cantidad;
						}
						else {
							sumaCantidad+=value.Cantidad;
						}

					});
					
					if (sumaCantidad > $scope.form.cantidadPorAsignar) {
						bcsComunes.msg("error","UI_ERROR","UI_CANTIDAD_SOBREPASA_DISPONIBLE");
						return;
					}

					e.model.Cantidad = e.values.Cantidad;
					$scope.form.actualizaColumnasEditablesEnFuncionA("cantidad");
				}
				else if (e.values.Monto !== undefined) {
					var sumaMonto = 0;
					$.each($scope.apiGrid.dataSource.data(), function (key, value) {
						if (value === e.model) {
							sumaMonto += e.values.Monto;
						}
						else {
							sumaMonto += value.Monto;
						}
					});
					if (sumaMonto > $scope.form.montoPorAsignar) {
						bcsComunes.msg("error", "UI_ERROR", "UI_MONTO_SOBREPASA_DISPONIBLE");
						return;
					}
					e.model.Monto = e.values.Monto;
					$scope.form.actualizaColumnasEditablesEnFuncionA("monto");
      			}


      		},
			dataSource: new bcs.DataSource({
				type : "array",
				definition : {
					data : [],
					schema : {
						model : {
							fields : {
								IdSocioDeNegocio	: {type : "number"},
								IdPortafolio		: {type : "number"},
								IdCliente			: {type : "number"},
								DocumentoIdentidad 	: {type : "string"},
								NombreCompleto 		: {type : "string"},
								CodigoPortafolio 	: {type : "string"},
								Cantidad 			: {type : "number"},
								Porcentaje 			: {type : "number"},
								Monto				: {type : "number"},
								TipoCambio			: {type : "number"},
								IdBoveda			: {type : "string"},
								IdFormaCustodia		: {type : "string"},
								IdMoneda			: {type : "string"},
								IdMonedaInstrumento	: {type : "string"},
								IdCuentaCustodioPortafolio : {type : "number"},
								PagoCobroCuentaInversion	: {type : "string"},
								OperaCuentaInversion	: {type : "string"},
								OperaCustodiaRV			: {type : "string"},
								OperaCustodiaRFIF		: {type : "string"},
								TasaVentaCorta			: {type : "number"},
								PlazoVentaCorta			: {type : "number"},
								NombreLargoCuentaCustodio : {type : "string"},
								CodigoIFRS				: {type : "string"}
														
							}
						}
					}
				}
			}),
  			columns: [
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Cantidad",{editable : true}),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Porcentaje",{editable : true}),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Monto", { editable: true, hidden: !$scope.form.columnaMontoActiva }),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoCambio"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("PagoCobroCuentaInversion"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBoveda"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLargoCuentaCustodio"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdFormaCustodia"),
  				bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoIFRS")
			]
		};
	
	if ($scope.form.EsVentaCorta) {
		$scope.form.configuracionGridClientes.columns.push(bcsConfiguracionOrdenesV2.getConfiguracionColumna("PlazoVentaCorta"));
		$scope.form.configuracionGridClientes.columns.push(bcsConfiguracionOrdenesV2.getConfiguracionColumna("TasaVentaCorta"));
	}
		
	function confirmarOrden(asignaciones){		
		let datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'ORD',
			p_Ordenes : asignaciones,
			p_CobrosModificados : [],
			p_AplicarComision: $scope.esAsignacionGrupoPrecioPromedio ? $scope.cliente.NoAplicarComision ? 'N' : 'S' : 'S',
			p_EnviarFacturarAutomatico: $scope.esAsignacionGrupoPrecioPromedio ? $scope.cliente.EnviarFacturarAutomatico ? 'S' : 'N' : 'N',
			p_EsAsignacionMasivaPrecioPromedio: $scope.esAsignacionGrupoPrecioPromedio ? 'S' : 'N',
			p_UsaLogDetallado: $scope.esAsignacionGrupoPrecioPromedio ? 'S' : 'N'
		};

		bcsComunes.ventanaOverlay($scope.idVentana,true);
		let cantidadAsignaciones=asignaciones.length;
		if($scope.limiteDeAsignacionesPorVentana <= cantidadAsignaciones || $scope.esAsignacionGrupoPrecioPromedio){
			//jobman
			promise = bcsServicioOrden.PrepararOrdenesPostrerasJob(datos).$promise;
			promise.then(function(result) {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
	
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					let id1 = bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_PROGRAMA_ASIGNACIONES_AGENDADO') + ' '+ result.P_OUT_IDPROGRAMA},$scope.idVentana);
					bcsComunes.ventanaPromesa(id1).then(function(result){
						bcsComunes.ventanaCerrar($scope.idVentana);
						$rootScope.abrirConsultaProgresos();
					})
				} else {
					bcsComunes.ventanaMsg({
						mensaje:'UI_ERROR_AL_CREAR_ASIGNACIONES',
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
				}
				
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsg({
					mensaje:'UI_ERROR_INTERNO_SISTEMA',
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			});
		} else {
			promise = bcsServicioOrden.CrearOrdenesPostreras(datos).$promise;
		
			promise.then(function(result) {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
	
				if (result.P_OUT_ERRORESCONTADOR === 0) {
					bcsComunes.ventanaMsg({
						mensaje:'UI_ASIGNACIONES_CREADAS',
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
					bcsComunes.ventanaCerrar($scope.idVentana);
				} else {
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					$.each(result.P_OUT_MENSAJES,function(key,value) {
						if (value.codigo === "CLIENTE_NO_PERTENECE_LIBRO_ORDENES") {
							$.each(asignaciones,function(keyAsig,valueAsig) {
								if (value.id === valueAsig.IdInterno) {
									$.each($scope.apiGrid.dataSource.data(),function(keyDS,valueDS) {
										if (valueAsig.IdPortafolio === valueDS.IdPortafolio) {
											value.descripcion =  value.descripcion + ", " + $translate.instant("DOCUMENTO_IDENTIDAD") + ": " + valueDS.DocumentoIdentidad;
										}
									});
								}
							});
						}
					});
					
					bcsComunes.ventanaMsg({
						mensaje:'UI_ERROR_AL_CREAR_ASIGNACIONES',
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
				}
				
			},function(result){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				bcsComunes.ventanaMsg({
					mensaje:'UI_ERROR_INTERNO_SISTEMA',
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			});
		}
	}
	
	
});
