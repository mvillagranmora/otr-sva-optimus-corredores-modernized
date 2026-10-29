(function() {
	angular.module('ModuloAPV').controller('IngresoDepositoCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);		
		var x = 0;
		var IdEstadoDeposito = 0;
		var primeraCarga = 'SI';
		
		var depm = this;
		depm.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		var paramVentanaDistribuir = {};
		
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kOptFechaDeposito = {};
		$scope.kOptFechaDeposito.max = new Date(); //$scope.$root.SesionFechaSistema;
		$scope.data.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
		$scope.data.habilitaSucursal = false;
		
		/*este campo se utiliza para bloquear el campo monto deposito cuando es llamado desde la gestion de traspaso*/
		depm.DeshabilitoMontoDeposito = false;
		
		//Cuenta bancaria del socio de negocio
		depm.oConfiguracionCuentaBancoSocioNegocio={
				tipo : "combobox-cuentas-bancarias-corredora",
				configuracion : {},
				filtros : {
					IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
					IdMoneda  : { "field"   : "IdMoneda", "operator": "eq", "value"   : "CLP"  }, // Cuentas de la misma moneda del comprobante
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		//Al cambiar la "Cuenta Bancaria Socio de Negocio" se actualizan los datos de visualización.
		$scope.$watch('depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado',function(newval,oldval){
			
			if (typeof depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== 'undefined' && depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado !== false)
			{	
				depm.bancoDeposito = depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NombreBanco;			
				depm.numeroCuenta = depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.NumeroCuentaBancaria;
				$scope.data.habilitaSucursal = true;
			}else{
				$scope.data.habilitaSucursal = false;
			}
		});	
		depm.oConfiguracionTipoDeposito={
				tipo : "combobox-medio-pago-tesoreria",
				configuracion : {},
				filtros : {
					IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "CLP" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};	
		
		depm.oConfiguracionSucursalBancaria={
				tipo : "combobox-sucursal-bancaria",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		depm.oConfiguracionCuentaCorriente={
				tipo : "combobox-numero-cuentas-bancarias-corredora",
				configuracion : {},
				filtros : {
					IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : "" },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		depm.filtroBanco = {
				IdPaisOperacion : {field: "IdPaisOperacion", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		}
		
		depm.filtroOrigen = {
				IdEstado	: {field: "IdEstado", operator: "eq", value: 'ACT'},
		}
		
		$scope.$watch('depm.bancoDeposito',function(newval, oldval){
			
			if(depm.bancoDepositoSeleccionado !== undefined && depm.bancoDepositoSeleccionado !== false){
				$scope.data.habilitaSucursal = true;
			//	depm.oConfiguracionSucursalBancaria.filtros = { IdBanco: { field: "IdBanco", operator: "eq", value: depm.bancoDepositoSeleccionado.IdBanco } };
			}else{
				$scope.data.habilitaSucursal = false;
			}	
		});
		
		depm.fnLimpiar = function()
		{ 
			depm.oConfiguracionCuentaBancoSocioNegocio.idActual	= undefined;
			depm.idCuentaSocioNegocio			= undefined;
			depm.origen							= undefined;
			depm.origenPostFiltro	 			= undefined;
			depm.folioCartola 					= undefined;
			depm.lineaCartola					= undefined;
			depm.kngFolioCartola				= undefined;
			depm.kngLineaCartola				= undefined;
			depm.sucursal						= undefined;
			depm.idSucursalPostFiltro 			= undefined;
			depm.kngFechaDeposito 				= undefined;
			depm.cuentaCorriente				= undefined;
			depm.idCuentaCorrientePostFiltro	= undefined;
			depm.MontoDeposito 					= undefined;
			depm.kngMontoDeposito				= undefined;
			depm.observacion					= undefined;
			depm.numeroDocumento				= undefined;
			depm.kngNumeroDocumento				= undefined;
			depm.tipoDeposito					= undefined;
			depm.tipoDepositoPostFiltro 		= undefined;
			depm.bancoDeposito 					= undefined;			
			depm.numeroCuenta 					= undefined;
			$scope.data.habilitaSucursal		= false;
			depm.parametrosEntrada.modoVentana = 'I';
		};
		
		depm.fnValidaDeposito = function()
		{ 
			var valido = false;
				
			if(
					/*depm.folioCartola 				=== undefined	||
					depm.folioCartola 				=== ""			||
					depm.lineaCartola				=== undefined	||
					depm.lineaCartola				=== ""			||
					depm.sucursalSeleccionada		=== undefined	||
					depm.sucursalSeleccionada		=== false		||*/
					depm.kngFechaDeposito 			=== undefined	||
					depm.kngFechaDeposito 			=== null		||
					depm.numeroCuenta				=== undefined	||
					depm.numeroCuenta				=== null		||
					depm.MontoDeposito 				=== undefined	||
					depm.MontoDeposito 				=== ""			||
					/*depm.numeroDocumento			=== undefined	||
					depm.numeroDocumento			=== ""			||*/
					depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado	=== undefined	||
					depm.tipoDepositoSeleccionado	=== undefined 	||
					depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado	=== false		||
					depm.tipoDepositoSeleccionado	=== false	 	||
					depm.origenSeleccionado			=== undefined	||
					depm.origenSeleccionado			=== false
			)
			{
				return valido;
			}else{
				valido = true;
			}
			return valido;
		};
			
		depm.IngresarDeposito = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			/*Esta logica es para pasarle los valores de la ventana a la gestion de traspasos*/
			if(depm.parametrosEntrada.Solicita === 'TRASPASO'){
				depm.parametrosEntrada.FolioCartola = depm.folioCartola;
				depm.parametrosEntrada.LineaCartola	= depm.lineaCartola;
				depm.parametrosEntrada.IdSucursal = depm.sucursalSeleccionada.IdSucursal;
				depm.parametrosEntrada.CuentaCorriente	= depm.numeroCuenta;
				depm.parametrosEntrada.NumeroDocumento = depm.numeroDocumento;
				depm.parametrosEntrada.IdTipoDeposito = depm.tipoDepositoSeleccionado.TipoMedioDePago;
				depm.parametrosEntrada.IdBanco = depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco;
				bcsComunes.ventanaData(idVentana,depm.parametrosEntrada);
				bcsComunes.ventanaCerrar(idVentana);
				/*Fin - Esta logica es para pasarle los valores de la ventana a la gestion de traspasos*/
			}else{
				var parametros = {				
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo  : "APV",
						p_Tipo: depm.parametrosEntrada.modoVentana === "M" ? "M" : "I",  ///Individual Manual o Por Servicio
						p_Origen: depm.origenSeleccionado.Codigo,
						p_Parametros : [{
							IdRecaudacion		: depm.parametrosEntrada.modoVentana === "M"? depm.parametrosEntrada.idRecaudacion : 0,
							IdEmpresa			: $scope.SesionIdEmpresa,
							FolioCartola 		: depm.folioCartola === undefined ? 0 :depm.folioCartola,
							LineaCartola		: depm.lineaCartola === undefined ? 0 :depm.lineaCartola,
							Sucursal			: depm.sucursalSeleccionada.IdSucursal === undefined ? "" :depm.sucursalSeleccionada.IdSucursal,
							FechaDeposito		: depm.kngFechaDeposito.toISOString().slice(0,10),
							CuentaCorriente		: depm.numeroCuenta,
							MontoDeposito		: depm.MontoDeposito,
							ConceptoRecaudacion : '',
							Origen				: depm.origenSeleccionado.Codigo,
							Observacion			: depm.observacion === undefined ? 0 :depm.observacion,
							NumeroDocumento		: depm.numeroDocumento === undefined ? 0 :depm.numeroDocumento,
							IdTipoDeposito		: depm.tipoDepositoSeleccionado.TipoMedioDePago,
							IdBanco				: depm.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdBanco,
							IdCarga				: 0,
							IdRegimenTributario : 0,
							IdEstado			: depm.parametrosEntrada.modoVentana === "M" ? IdEstadoDeposito : 'ING',
						}],
					};
					
					bcsServiciosAPV.CrearRecaudacionDeposito(parametros).$promise.then(function(respuesta){
				   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));							 
						var mensajeTotal = new Array();
						var mensajeFormaPago = new Array();
						$.each(respuesta.P_OUT_MENSAJES,function(key,value){
							 var objMensaje = {
									Id : value.id,
									tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
									codigo : value.codigo,
									descripcion : $translate.instant(value.descripcion)
							 };
							 
							 if( value.codigo === 'FORMA_PAGO_COMPROBANTE_NO_EXISTE' 					||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_ANULADA'						||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_LIQUIDADA' 					||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_SIN_DEPOSITAR'				||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_EN_NOMINA_DEPOSITO' 			||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_SIN_CERTFICAR'				||
								 value.codigo === 'FORMA_PAGO_COMPROBANTE_SIN_CUENTA_BANCARIA_CORREDORA' ){
								 
								 mensajeFormaPago.push(objMensaje);
							 }
							 
							 mensajeTotal.push(objMensaje);
						});
						
						if(mensajeFormaPago.length > 0){
							respuesta.P_OUT_MENSAJES = mensajeFormaPago;
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_FORMA_PAGO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
						}else{
							respuesta.P_OUT_MENSAJES = mensajeTotal;
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							depm.fnLimpiar();
						}
						
						bcsComunes.ventanaOverlay(idVentana,false);

			   		},function(respuesta){
						var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
			   			$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						});
			   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			   		});	
			}
		};
		
		
		var datosRecaudacion = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdRecaudacion", dir: "DESC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: bcsConsultasAPV('ConsultarRecaudacion'),
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{		
													"IdRecaudacion"			: {type: "number"},
													"IdEmpresa"				: {type: "number"},
													"FolioCartola"			: {type: "number"},
													"LineaCartola"			: {type: "number"},
													"Sucursal"				: {type: "string"},
													"FechaDeposito"			: {type: "sysdate"},
													"CuentaCorriente"		: {type: "string"},
													"MontoDeposito"			: {type: "number"},
													"ConceptoRecaudacion"	: {type: "string"},
													"Origen"				: {type: "string"},
													"Observacion"			: {type: "string"},
													"NumeroDocumento"		: {type: "number"},
													"IdTipoDeposito"		: {type: "string"},
													"IdBanco"				: {type: "number"},
													"IdRegimenTributario"	: {type: "number"},
													"IdCarga"				: {type: "number"},
												}
                    						}
									  }
    			 }
		};
		
		depm.AsignaValoresDeposito = function(IdDeposito){
			if(IdDeposito === undefined)
				return;
			
			var filtro = Array(); 
			filtro.push({ field: "IdRecaudacion" , operator: "eq", value: Number(IdDeposito) });
			
			var ConsultaRecaudacion = new bcs.DataSource(datosRecaudacion);
			ConsultaRecaudacion.query({ filter: filtro }).then(function(){
				if(ConsultaRecaudacion.view().length > 0){					
					depm.folioCartola					=	ConsultaRecaudacion.view()[0].FolioCartola;
					depm.lineaCartola					= 	ConsultaRecaudacion.view()[0].LineaCartola; 
					depm.idSucursalPostFiltro			=	ConsultaRecaudacion.view()[0].Sucursal;
					depm.kngFechaDeposito				=	ConsultaRecaudacion.view()[0].FechaDeposito;
					depm.idCuentaCorrientePostFiltro	=	ConsultaRecaudacion.view()[0].CuentaCorriente; 
					depm.MontoDeposito					=	ConsultaRecaudacion.view()[0].MontoDeposito;
					depm.observacion					=	ConsultaRecaudacion.view()[0].Observacion;
					depm.numeroDocumento				=	ConsultaRecaudacion.view()[0].NumeroDocumento;
					depm.tipoDepositoPostFiltro			=	ConsultaRecaudacion.view()[0].IdTipoDeposito;
					depm.idBancoPostFiltro				=   ConsultaRecaudacion.view()[0].IdBanco;
					IdEstadoDeposito					=	ConsultaRecaudacion.view()[0].IdEstado;
					depm.origenPostFiltro 				= 	ConsultaRecaudacion.view()[0].Origen;
					depm.idCuentaSocioNegocio 			= 	ConsultaRecaudacion.view()[0].IdCuentaSocioNegocio;
				}
			});
		};

		$scope.$watchGroup(['depm.tipoDeposito', 'depm.sucursal','depm.cuentaCorriente'],function(newval,oldval){
			
			if (newval !== undefined && x===0){
				if(depm.parametrosEntrada.modoVentana === "M"){
					depm.AsignaValoresDeposito(depm.parametrosEntrada.idRecaudacion);
					if(primeraCarga === 'SI')
						$scope.data.habilitaSucursal = false;
					
				}
				/*Solicita Traspaso*/
				if(depm.parametrosEntrada.Solicita === 'TRASPASO'){
					depm.MontoDeposito = depm.parametrosEntrada.MontoDeposito;
					depm.kngMontoDeposito = depm.parametrosEntrada.MontoDeposito;
					depm.origenPostFiltro = 'TRASPA';
					depm.DeshabilitoMontoDeposito = true;
				}
				primeraCarga = 'NO';
			}
			
		});
		
		$scope.$watch('depm.origen',function(newval,oldval){		
			if (newval !== undefined && x===0){
				if(newval === "TRASPA"){
					depm.idCuentaSocioNegocio = depm.origenSeleccionado.DestinoCtaBco;
				}else{
					depm.idCuentaSocioNegocio = false;
					depm.bancoDeposito = "";			
					depm.numeroCuenta = "";
				}
			}		
		});
/*-------------------------------------------------------------------------------*/
	});
}());