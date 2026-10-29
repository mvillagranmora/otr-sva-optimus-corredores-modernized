 
/**
  Proposito: Permite generar una partida de ingresos(PARING) o de egresos(PAREGR) a partir de uno o varios movimientos
  			 PARING --> El comprobante de partida de ingreso se generará a nombre de la corredora ya que esta recibirá el pago
  			 PAREGR --> El comprobante de partida de egresos se generará a nombre del banco al cual hay que pagarle
  Autor: JVILLAGRAN - BCS
  Fecha Creacion: 14/03/2018
 **/

angular.module('ModuloTesoreria').controller('PartidaIngresoEgresoCtrl', function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,
							bcsServiciosTesoreria,bcsBlotterComprobantesGrid)
{

	$scope.data = {};
	
	var IdComprobanteFormaDePago = 0;           //Id temporal(solo para edición)
	var IdComprobanteFormaDePagoEditado = 0;  	//Id temporal(solo para edición)
	var tipoPartida = "";                       //PARING | PAREGR	
	var mensajeExitoCrearPartida = "";
	var mensajeErrorCrearPartida = "";
	
	//Obtiene parametros desde la ventana que lo invoca
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);	
	$scope.objParametrosVentana = bcsComunes.ventanaParams($scope.IdVentana);
	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	
	//Personaliza títulos y mensajes de acuerdo al tipo de partida a generar (ingreso/egreso)
	tipoPartida = $scope.objParametrosVentana[0].TipoMovimiento;
	if (tipoPartida === 'PARING')
	{
		bcsComunes.ventanaTitulo($scope.IdVentana, $translate.instant("UI_PARTIDA_INGRESOS"));  //Personaliza título de ventana		
		mensajeExitoCrearPartida = "UI_CREAR_PARTIDA_INGRESOS_EXITO";  // Personaliza mensajes de salida exito crear partida
		mensajeErrorCrearPartida = "UI_CREAR_PARTIDA_INGRESOS_ERROR";  // Personaliza mensajes de salida error crear partida
	}
	else
	{
		bcsComunes.ventanaTitulo($scope.IdVentana, $translate.instant("UI_PARTIDA_EGRESOS"));		
		mensajeExitoCrearPartida = "UI_CREAR_PARTIDA_EGRESOS_EXITO";
		mensajeErrorCrearPartida = "UI_CREAR_PARTIDA_EGRESOS_ERROR";
	}	
		
	
	//Calcula el Saldo pendiente de asignar, según se asignen montos en formas de pago de la partida
	function ObtenerSaldoPorAsignar()
	{
		var saldoTotal = 0;
		if (ds !== undefined ){
			saldoTotal = $scope.objParametrosVentana[0].Monto - ds.aggregates().Monto.sum;
			$scope.data.saldoPorAsignar = saldoTotal;			
		}
	};
	

	//-----------------------------------------------------------------------
	// CONFIGURACION GRILLAS
	
	
	
	//Configuracion grilla(desconectada) forma de pago de la partida
	
	var change = function(e){
		ObtenerSaldoPorAsignar();
		//Redefine Data Source para filtro dinámico.
		switch (e.action){		
			case "remove":
			case "add":
				$scope.data.showBtnGrabar = true;
				break;
			default:
				$scope.data.showBtnGrabar = false;
				break;
		}
	};
	
	var ds = bcsBlotterComprobantesGrid.getDsComprobanteFormaDePago(change);	
	$scope.data.confBasicaPartidaIngresoEgresoFormaDePago = {
		dataSource			: ds,		
		columns				: bcsBlotterComprobantesGrid.columnasComprobanteFormaDePago(),		
		sortable			: true,
		pageable			: true,		
		height				: 250,
		selectable 			: "multiple row",
		dataBound			: function(e) {	ObtenerSaldoPorAsignar(); }  //Este evento se ejecuta cada vez que se actualiza el dataSource	
	};
	
	

	
	// Configuración grilla de movimientos para generar partida 
	// Filtra sólo los movimientos seleccionados
	var objFilterIngresoEgreso = {};	
	var filterIngresoEgreso = new Array();
	                          
	$.each($scope.objParametrosVentana[0].ingresoEgresoGenerar, function(key,value)
	{
		filterIngresoEgreso.push({ field: "IdIngresoEgreso", operator: "eq", value: value.IdIngresoEgreso });
	});	
	objFilterIngresoEgreso.logic = 'or';
	objFilterIngresoEgreso.filters = filterIngresoEgreso;
	
	//Redefine dataSource para filtro dinámico.
	var dataSourceIngresoEgreso = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgreso;
	dataSourceIngresoEgreso.definition.filter = objFilterIngresoEgreso;
	$scope.confBasicaIngresoEgreso = {
		dataSource			: new bcs.DataSource(dataSourceIngresoEgreso), 
		columns				: bcsBlotterComprobantesGrid.columnasIngresosEgresos(),		
		sortable			: true,
		groupable			: false,
		pageable			: false,
		autoBind			: true,
		filterable			: false,
		height				: 600,
		selectable 			: "multiple row",
		userSettings		: true
	};
 
	
	
	// Configuraciónn sub-grilla forma de pago de movimeintos para generar partida
	$scope.confBasicaIngresoEgresoFormaDePago = function(item) {
		var dsDef = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgresoFormaDePago;
		dsDef.definition.filter = [{ field: "IdIngresoEgreso", operator: "eq", value: item.IdIngresoEgreso }];
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: bcsBlotterComprobantesGrid.columnasIngresoEgresoFormaDePago(),		
			sortable			: true,
			autoBind			: true,
			pageable			: true,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row",
			userSettings		: true
		};
	};

	
	
	
	
	// ------------------------------------------------------------------------------
	// FUNCIONES GENERALES 


	// Cierra la ventana
	$scope.data.cancelar = function()
	{
		bcsComunes.ventanaData($scope.IdVentana);
		bcsComunes.ventanaCerrar($scope.IdVentana);  
	};


	// Quita una forma de pago de la grilla desconectada(visualmente) 
	$scope.data.eliminarFormaPago = function()
	{
		if ($scope.data.seleccionPartidaIngresoEgresoFormaDePago === undefined)
		{	
			bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_DEBE_SELECCIONAR_FORMA_DE_PAGO')} ,$scope.IdVentana);
		}
		else
		{	
			ds.remove($scope.data.seleccionPartidaIngresoEgresoFormaDePago);
			$scope.data.seleccionPartidaIngresoEgresoFormaDePago = undefined;
			ObtenerSaldoPorAsignar();
		}
	}


	
	// Agrega una nueva forma de pago(un nuevo row) a grilla desconectada (visualmente)
	$scope.data.agregarFormaPago = function()
	{
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPago(); // recupera la configuración desde la directiva
		
		if (typeof objFormaPago !== 'undefined')
		{	
			IdComprobanteFormaDePago  				= IdComprobanteFormaDePago  + 1;  //Id temporal(sólo para edición)
			objFormaPago.IdComprobanteFormaDePago 	= IdComprobanteFormaDePago;       //Id temporal(para edición)

			ds.add(objFormaPago);
			ObtenerSaldoPorAsignar();
		}
	}

	

	$scope.select = function(grid)
	{
		$timeout(function() {
			$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},10);
	};
	

	
	// -------------------------------------------------------------------------
	// LLAMADA SERVICIO CREAR COMPROBANTE DE PARTIDA DE INGRESO/EGRESO

	var successPartidaIngresos = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
		{
			if (respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
			{
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant(mensajeExitoCrearPartida),
					conflictos: util.parseServerResponse(respuesta)
				},$scope.IdVentana);
			}
			else
			{
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant(mensajeErrorCrearPartida),
					conflictos: util.parseServerResponse(respuesta)
				},$scope.IdVentana);
			}
			
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			$scope.data.cancelar('OK');    // Cerrar Modal
		}
		else
		{
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant(mensajeErrorCrearPartida),
				conflictos: util.parseServerResponse(respuesta)
			},$scope.IdVentana);
		}
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
	};


	var errorPartidaIngresos = function(respuesta)
	{
		bcsComunes.ventanaMsg({
			mensaje: $translate.instant(mensajeErrorCrearPartida),
			conflictos: util.parseServerResponse(respuesta)
		},$scope.IdVentana);		
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
	};


	$scope.data.crearPartidaIngresoEgreso = function()
	{
		//Rescata valores ingresados en formulario
		var parametros = {};
		bcsComunes.ventanaOverlay($scope.IdVentana, true);

		parametros.p_IdEmpresa              			= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario              			= $scope.SesionIdUsuario;
		parametros.p_IdModulo               			= 'TES';
		parametros.p_UsaReglasNegocio      		 		= 'S';
		parametros.p_FechaMovimiento        			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		parametros.p_IdCaja                 			= 1; ///CAMBIAR  //////// .. revisar ....... ???????
		parametros.p_GenerarComprobanteDesdeMovimientos = 'S';
		parametros.p_ComprobanteGenerar					= [];

		//Obtiene los movimientos
		var arrIngresoEgresoGenerar = new Array();			                                
		$.each($scope.objParametrosVentana[0].ingresoEgresoGenerar, function(key,value)		
		{				
			arrIngresoEgresoGenerar.push({ NumeroReferencia: 1, IdIngresoEgreso: value.IdIngresoEgreso, TipoComprobante: tipoPartida });
		});
		parametros.p_IngresoEgresoGenerar = arrIngresoEgresoGenerar;


		//Obtiene forma de pago desde la grilla desconectada
		var arrFormaDePago = new Array();
		arrFormaDePago = ds.dataColumns(['IdMedioDePago','IdModalidadDePago','IdMoneda','Monto','IdCuentaBancariaCorredora',
										 'IdTarjetaBancariaCorredora','IdBancoSocio','NumeroCuentaBancariaSocio','NumeroSerieSocio',
										 'NumeroDocumentoSocio','FechaDocumentoPagoSocio']);

		
		//Ajusta algunos parámetros de forma de pago
		if (arrFormaDePago !== undefined)
		{
			$.each(arrFormaDePago, function(key,value){
				arrFormaDePago[key].IdModalidadDePago = value.IdModalidadDePago;  //Tiene nombre distinto en SP CrearComprobante
			});			
		}		
		else
		{	
			bcsComunes.ventanaMsg({mensaje: $translate.instant('DEBE_INGRESAR_FORMA_DE_PAGO')} ,$scope.IdVentana);	
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			return;
		}
		

		var arrPartidaIngresosFormaDePago = new Array();			                                
		$.each(arrFormaDePago, function(key,value){				
			arrPartidaIngresosFormaDePago.push({
				  NumeroReferencia 			     : 1,
				  IdMedioDePago 				 : value.IdMedioDePago,
				  IdModalidadDePago 			 : value.IdModalidadDePago,
				  IdMoneda 						 : value.IdMoneda,
				  Monto 						 : value.Monto,
				  IdCuentaBancariaCorredora   	 : value.IdCuentaBancariaCorredora,
				  IdTarjetaBancariaCorredora  	 : value.IdTarjetaBancariaCorredora,
				  IdChequera				  	 : 0,
				  NumeroDocumentoCorredora	  	 : 0,
				  FechaDocumentoPagoCorredora 	 : '0001-01-01',
				  IdCuentaBancariaSocio			 : 0,
				  IdBancoSocio					 : value.IdBancoSocio,
				  NombreCuentaBancariaSocio		 : '',
				  NumeroCuentaBancariaSocio		 : value.NumeroCuentaBancariaSocio,
				  IdTipoCuentaBancariaSocio		 : '',
				  NombreTipoCuentaBancariaSocio  : '',
				  IdTarjetaBancariaSocio		 : 0,
				  IdTipoTarjetaBancariaSocio	 : '',
				  NombreTipoTarjetaBancariaSocio : '',
				  NumeroTarjetaBancariaSocio	 : '',
				  NumeroSerieSocio				 : value.NumeroSerieSocio,
				  NumeroDocumentoSocio			 : value.NumeroDocumentoSocio,
				  FechaDocumentoPagoSocio		 : util.dateSPdate(util.dateInicioDia(value.FechaDocumentoPagoSocio))

				});
		});			
		parametros.p_FormaDePago = arrPartidaIngresosFormaDePago;
		           
		console.log("DATA A INSERTAR:" + JSON.stringify(parametros));

		bcsServiciosTesoreria.CrearComprobante(parametros, successPartidaIngresos, errorPartidaIngresos);				
	};
	

});

