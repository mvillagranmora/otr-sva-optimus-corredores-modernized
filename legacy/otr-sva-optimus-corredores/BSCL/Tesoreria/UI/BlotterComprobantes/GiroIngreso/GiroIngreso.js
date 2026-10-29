angular.module('ModuloTesoreria').controller('GiroIngresosCtrl',
			function($scope,$translate,$timeout,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					bcsServiciosTesoreria,bcsBlotterComprobantesGrid)
{

	$scope.data = {};
	var IdComprobanteFormaDePago = 0;           //Id temporal(solo para edición)
	var IdComprobanteFormaDePagoEditado = 0;  	//Id temporal(solo para edición)
	
	//Obtiene parametros traspasados desde la ventana de Ingresos/Ingresos
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

	$scope.objFormaDePago = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.comprobanteFormaDePago 		= {};          //objeto con las formas de pago editadas	
	$scope.data.showDatosOperacion 		= false;
	$scope.data.showDatosSocio 			= false;

	// --------------------------------------------------
	//  CONFIGURACION GRILLAS 
	// --------------------------------------------------	
	 
	// Filtra sólo los comprobantes seleccionados
	var objFilterComprobantes = {};	
	var filterComprobantes = new Array();
	                          
	$.each($scope.objFormaDePago[0].comprobantesAGirar, function(key,value){
		filterComprobantes.push({ field: "IdComprobante", operator: "eq", value: value.IdComprobante }); 
	});	
	objFilterComprobantes.logic = 'or';
	objFilterComprobantes.filters = filterComprobantes;
	
	//Redefine dataSource para filtro dinámico.
	var dataSourceComprobantes = bcsBlotterComprobantesGrid.dsDefComprobantes;
	dataSourceComprobantes.definition.filter = objFilterComprobantes;
	dataSourceComprobantes = new bcs.DataSource(dataSourceComprobantes);
	$scope.confBasicaComprobantes = {
		dataSource			: dataSourceComprobantes, 
		columns				: bcsBlotterComprobantesGrid.columnasComprobantesAGirar(),		
		sortable			: true,
		pageable			: false,
		autoBind			: true,
		filterable			: false,
		userSettings		: true,
		height				: 300,
		selectable 			: "multiple row"
	};
	$scope.confBasicaComprobanteFormaDePago = function(item) {
		var dsDef = bcsBlotterComprobantesGrid.dsDefComprobanteFormaDePago;
		dsDef.definition.filter = [{ field: "IdComprobante", operator: "eq", value: item.IdComprobante },
								   { field: "IdEstado", operator: "eq", value: 'ING' } 
								  ];
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: bcsBlotterComprobantesGrid.columnasComprobanteFormaDePago(),
			sortable			: true,
			autoBind			: true,
			pageable			: true,
			autoBind			: true,			
			height				: 150,
			selectable 			: "multiple row"
		};
	};
	//Abre modal para edición de forma de pago del socio
	$scope.editarMedioDePagoSocio = function(datos) {
		var idModal = bcsComunes.ventanaAbrir("MedioDePagoSocioModal", datos, true, false );
		var existeMarca = false;
		
		var promesa = bcsComunes.ventanaPromesa(idModal);
        promesa.then(function(data){ 
        	if (data != undefined){
        		//obtiene la data de la grilla
				//var comprobanteFormaPago = $scope.gridApiComprobanteFormaPago.dataSource.data();
				var comprobanteFormaPago = dataSourceComprobantes.data();
				

				$.each(comprobanteFormaPago, function(key,value){
					if (Number(value.IdComprobanteFormaDePago) == Number(data.IdComprobanteFormaDePago) ){
						//Actualiza los datos en la grilla
						value.IdCuentaBancariaSocio 		= data.IdCuentaBancaria;
						value.IdTipoCuentaBancariaSocio 	= data.IdTipoCuentaBancaria;
						value.NombreTipoCuentaBancariaSocio = data.NombreTipoCuentaBancaria;
						value.NombreCuentaBancariaSocio 	= data.NombreCuentaBancaria;
						value.NumeroCuentaBancariaSocio 	= data.NumeroCuentaBancaria;
						value.IdMonedaSocio 				= data.IdMoneda;
						
						//Ingresa registro editado a objeto
						$scope.comprobanteFormaDePago[Number(data.IdComprobanteFormaDePago)] = 
						{
								'IdComprobante'                 	: Number(value.IdComprobante),
								'IdComprobanteFormaDePago'      	: Number(data.IdComprobanteFormaDePago),	
								'IdCuentaBancariaSocio'         	: data.IdCuentaBancaria,
								'IdBancoSocio'                      : data.IdBanco,
								'NombreCuentaBancariaSocio'    	    : data.NombreCuentaBancaria,
								'NumeroCuentaBancariaSocio'     	: data.NumeroCuentaBancaria,
								'IdTipoCuentaBancariaSocio'     	: data.IdTipoCuentaBancaria,
								'NombreTipoCuentaBancariaSocio' 	: data.NombreTipoCuentaBancaria,
								'IdTarjetaBancariaSocio'  		    : 0,
								'IdTipoTarjetaBancariaSocio' 		: '',
								'NombreTipoTarjetaBancariaSocio' 	: '',
								'NumeroTarjetaBancariaSocio' 		: '',
								'NumeroSerieSocio'				    : '',
								'NumeroDocumentoSocio'			    : 0,
								'FechaDocumentoPagoSocio'		    : '0001-01-01'
						 };
					}
				});								
				dataSourceComprobantes.read();
        	}	        		
    	}); 
	};
	
	
	// --------------------------------------------------
	// SERVICIOS GIRAR COMPROBANTES INGRESOS  
	// --------------------------------------------------
	
	var success = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0)
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_GIRAR_COMPROBANTES_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.IdVentana);			
		}
		else
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_GIRAR_COMPROBANTES_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.IdVentana);
			
		}
		
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		bcsComunes.ventanaData($scope.IdVentana);
		bcsComunes.ventanaCerrar($scope.IdVentana);
	};	
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_GIRAR_COMPROBANTES_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.IdVentana);		
		
		bcsComunes.ventanaOverlay($scope.IdVentana, false);	
	};
	
	$scope.girarIngresos = function()
    {
		var configFP = {};
		configFP = $scope.data.configuracionFormaPago(); 	//Informacion desde la directiva
		
		if (typeof configFP !== 'undefined'){
			if (configFP.GeneraNomina===true && configFP.GeneraTransferencia===true) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_SELECCIONAR_NOMINA_TRANSFERENCIA'));
			} else {
		    	var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_GENERARA_PARTIDA_INGRESO_COMPROBANTES_INDICADOS') );
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay($scope.IdVentana, true);

						var parametros = {};
						var formaDePagoSocio = [];

						//Obtiene los comprobantes a girar 	    
						var comprobantesAGirar = dataSourceComprobantes.dataColumns(["IdComprobante"]);

						//Formatea en un objeto las formas de pago editadas.
						formaDePagoSocio = $.map($scope.comprobanteFormaDePago, function(el) { return el });

						parametros.p_IdEmpresa   		 		= $scope.SesionIdEmpresa;
						parametros.p_IdUsuario   		 		= $scope.SesionIdUsuario;
						parametros.p_IdModulo 			 		= 'TES';
						parametros.p_simularOperacion 		 	= 'N';
						parametros.p_UsaReglasNegocio     		= 'S';

						parametros.p_IdCaja						= configFP.IdCaja;
						parametros.p_FechaMovimiento            = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_ComprobantesAGirar	 		= comprobantesAGirar;
						parametros.p_IdMedioDePago  			= configFP.IdMedioDePago || '';
						parametros.p_IdCuentaBancariaCorredora  = configFP.IdCuentaBancariaCorredora || 0;
						parametros.p_FormasDePago     	        = formaDePagoSocio || [];
						parametros.p_monto     	                = configFP.Monto || 0;
						parametros.p_usaTalonarioCheque         = configFP.UsaTalonarioCheque ? 'S' : 'N';
						parametros.p_IdModalidadDePago          = configFP.IdModalidadDePago || '';
						parametros.p_IdMoneda                   = configFP.IdMoneda || '';
						parametros.p_GeneraNomina				= configFP.GeneraNomina ? 'S' : 'N';
						parametros.p_GeneraTransferencia		= configFP.GeneraTransferencia ? 'S' : 'N';
						parametros.p_GenerarComprobanteDesdeMovimientos = 'N';
						parametros.p_GenerarComprobanteDesdeComprobantes = 'S';
						parametros.p_EsIngresoAutomatico = 'S';
						bcsServiciosTesoreria.AgruparComprobantesIngreso(parametros,success,error);
			    	}
				});
			}
		} else {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_SELECCIONAR_CUENTA_BANCARIA_SOCIO_NEGOCIO'));
		}

		return;	
    };

	var successQuitar = function(respuesta){	  		
		if (respuesta.P_OUT_ERRORESCONTADOR == "0")
		{	
			var parametrosVentana = [];		
			var comprobantesAGirar = dataSourceComprobantes.dataColumns(["IdComprobante"]);

			parametrosVentana.push({
				IdConcepto			: respuesta.P_OUT_IDCONCEPTO,
				NombreConcepto		: respuesta.P_OUT_NOMBRECONCEPTO,						
				IdMoneda			: respuesta.P_OUT_IDMONEDA,				
				NombreMoneda		: respuesta.P_OUT_NOMBREMONEDA,
				FechaLiquidacion	: util.dateInicioDia(respuesta.P_OUT_FECHALIQUIDACION),
				IdCaja				: respuesta.P_OUT_IDCAJA,
				NombreCaja          : respuesta.P_OUT_NOMBRECAJA,
				comprobantesAGirar	: comprobantesAGirar,
				mediosDePagoAGirar	: respuesta.P_OUT_MEDIOSDEPAGOAGIRAR,						
				IdTipoMovimiento	: 'MOVING', //'MOVEGR', 	//cambiar
				TipoMovimiento		: 'PARING' //'PAREGR' 		//cambiar						

			});
			$scope.objFormaDePago = parametrosVentana;
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		}
		else 
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.IdVentana);			
		}
		
		$scope.btnActivo = true;
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
	};
	
	var errorQuitar = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_COMPROBANTES_NO_CUMPLEN_REQUISITOS_PARA_GIRARSE"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.IdVentana);
		$scope.btnActivo = true;
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
	};
	
    $scope.quitarComprobante = function()
	{	
		if ($scope.seleccionComprobantes === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_COMPROBANTE'));		
		}
		else
		{	
			//Remueve row de la grilla
			dataSourceComprobantes.remove($scope.seleccionComprobantes);			
			$scope.seleccionComprobantes = undefined;

			//Si se quitan todos los comprobantes desactiva botón Girar			
			$scope.btnGirarHabilitado = (dataSourceComprobantes.data().length > 0) ? true : false;
						
			//Refresca las forma de pago y montos, según los comprobantes que queden en la grilla			
			if (dataSourceComprobantes.data().length > 0)
			{
				var comprobantesAGirar = [];
				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				//Recorre grilla y llena arreglo con IdComprobante que quedan				
				$.each(dataSourceComprobantes.data(),function(key,value){
					comprobantesAGirar.push({IdComprobante: value.IdComprobante});
				});
				
				var datos = {};
				datos.p_IdEmpresa   		 		= $scope.SesionIdEmpresa;
				datos.p_IdUsuario   		 		= $scope.SesionIdUsuario;
				datos.p_IdModulo 			 		= 'TES';
				datos.p_simularOperacion 		 	= 'S';  //Para este caso debe ir 'S' para que sólo valide
				datos.p_UsaReglasNegocio     		= 'S';
				datos.p_FechaMovimiento             = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				datos.p_IdCaja						= 3;
				datos.p_ComprobantesAGirar	 		= comprobantesAGirar;
				
				//Estos parámetros se envían vaciós, ya que en esta instancia este servicio se usa solo para 
				//validar las condiciones de los comprobantes a girar.
				datos.p_IdMedioDePago  			    = '';
				datos.p_IdCaja						= 0 ;
			    datos.p_IdCuentaBancariaCorredora   = 0 ;
			    datos.p_monto     	                = 0 ;
				datos.p_FormaDePagoSocio     	    = [];
				datos.p_UsaTalonarioCheque          = '';    
				datos.p_IdModalidadDePago           = '';
		       
				bcsServiciosTesoreria.GirarComprobantesIngreso(datos, successQuitar, errorQuitar);
			}
		}
	};	
    // --------------------------------------------------
	// FIN SERVICIOS GIRAR COMPROBANTES INGRESOS  
	// --------------------------------------------------
});
