
angular.module('ModuloCustodia').controller('InscribirClientePrepagoBonoCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsServiciosEventoCapital,bcsServicioPlataforma
				  ) {

	$scope.data = {};

	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(idVentana);


	$scope.instrumentoCustodia = {};
	$scope.valorizando = false;


	$scope.data.oDatosEvento = $scope.parametros.oEventoCapital[0];


	

	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};



  		
/*************************************************************************
* CARGA INICIAL
**************************************************************************/

  	
  	$scope.data.TipoEvento = $scope.data.oDatosEvento.IdTipoEventoCapital;
  	$scope.data.idInstrumento = $scope.data.oDatosEvento.IdInstrumento;

  	
  	$scope.data.TipoEventoTXT		= $scope.data.oDatosEvento.TipoEvento;
	$scope.data.idInstrumentoTXT	= $scope.data.oDatosEvento.Nemo;
	$scope.data.kngSecuenciaTXT		= $scope.data.oDatosEvento.Secuencia;
	$scope.data.DescripcionTXT 		= $scope.data.oDatosEvento.Descripcion;

		
	
	$scope.data.Cerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};


/*************************************************************************
* CARGA INICIAL MODIFICACIÓN
**************************************************************************/		
if ($scope.data.oDatosEvento  && $scope.parametros.accion == 'Modificar')
{	
	$scope.data.DescripcionTXT 				= $scope.data.oDatosEvento.DescripcionEvento;
	$scope.data.IdEventoCapitalCliente		= $scope.data.oDatosEvento.IdEventoCapitalCliente;
	$scope.data.TipoEventoTXT 				= $scope.data.oDatosEvento.TipoEvento;
	$scope.data.IdTipoEventoCapitalTXT		= $scope.data.oDatosEvento.IdTipoEventoCapital;
	$scope.data.ClienteTXT 					= $scope.data.oDatosEvento.DocumentoIdentidad  + ' - ' + $scope.data.oDatosEvento.NombreCliente;
	$scope.data.PortafolioTXT 				= '(' + $scope.data.oDatosEvento.CodigoPortafolio + ') - ' + $scope.data.oDatosEvento.Portafolio;
};




/*************************************************************************
* INGRESO SUSCRIPCION 
**************************************************************************/

	$scope.data.InscribirClientePrepagoBono = function()
    {
		var datos = {};
		bcsComunes.ventanaOverlay(idVentana,true);
		
   	    //Faltan validaciones (que haya seleccionado cliente)
    	 
		if ($scope.parametros.accion == "Crear")
		{
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo  = 'CUS';		
			datos.p_EventoCapitalCliente = [{		
				IdInterno						: 1, 
				IdEventoCapital					: $scope.data.oDatosEvento.IdEventoCapital,
				IdCliente						: $scope.oSocioNegocio.seleccionado.IdCliente,
				IdPortafolio					: $scope.oPortafolio.seleccionado.IdPortafolio,
			    IdClasificacionTipoInstrumento 	: $scope.instrumentoCustodia.IdClasificacionTipoInstrumento, 
			 	IdTipoInstrumento				: $scope.instrumentoCustodia.IdTipoInstrumento,
			  	IdInstrumento					: $scope.instrumentoCustodia.IdInstrumento,
				CantidadNueva					: $scope.data.kngValorNominal,
				PrecioNuevo						: $scope.data.kngPrecio,
				MontoCapital					: $scope.data.kngValorValorizado,
				MontoInteres					: $scope.data.kngInteresesCorridos,
				MontoReajuste					: $scope.data.kngReajustesVacCorrido,
				Total							: $scope.data.kngValorActualizado,
				IdCartera						: $scope.data.kngIdCartera
			}];
			
			bcsServiciosEventoCapital.InscribirClientePrepagoBono(datos, successInscribirClientePrepagoBono, errorInscribirClientePrepagoBono);
		};
	};		
   	
   	

	var successInscribirClientePrepagoBono = function(respuesta)
	{	
    	if (respuesta.P_OUT_ERRORESCONTADOR == 0)
    	{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"), conflictos:util.parseServerResponse(respuesta)}, idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
			$scope.data.Cerrar(false);
		}
		else
		{
			//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"), conflictos:util.parseServerResponse(respuesta)}, idVentana);
			bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)}, idVentana);
			bcsComunes.ventanaOverlay(idVentana, false);
		}
	};
	

	var errorInscribirClientePrepagoBono = function(respuesta)
	{
		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),conflictos:util.parseServerResponse(respuesta)}, idVentana);
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	
	
	

	/*************************************************************************
	* CONSULTA CARTERAS
	**************************************************************************/

	$scope.data.btnConsultarCarteras = function()
	{	
		var datos = {};
		datos.Filtrado = true;
		if ($scope.parametros.accion == 'Modificar') {
			if ($scope.data.oDatosEvento.IdCliente === "" || $scope.data.oDatosEvento.IdCliente === undefined || $scope.data.oDatosEvento.IdCliente === null) {
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_DEBE_SELECCIONAR_CLIENTE")}, idVentana);
				return
			}
			if ($scope.data.oDatosEvento.IdPortafolio === "" || $scope.data.oDatosEvento.IdPortafolio === undefined || $scope.data.oDatosEvento.IdPortafolio === null) {
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_DEBE_SELECCIONAR_PORTAFOLIO")}, idVentana);
				return
			}
			datos.IdClasificacionTipoInstrumento = $scope.data.oDatosEvento.IdClasificacionTipoInstrumento || ''; // RF 
			datos.IdTipoInstrumento = $scope.data.oDatosEvento.IdTipoInstrumento || '';  // BON
			datos.IdCliente = $scope.data.oDatosEvento.IdCliente || -1;
			datos.IdPortafolio = $scope.data.oDatosEvento.IdPortafolio || -1;
			datos.FechaCondicionLiquidacion =  util.dateSPdate($scope.data.oDatosEvento.FechaPago);
			datos.SocioNegocio = $scope.data.ClienteTXT;
			datos.Portafolio = $scope.data.PortafolioTXT;
		} else {
			if ($scope.oSocioNegocio.seleccionado.IdCliente === "" || $scope.oSocioNegocio.seleccionado.IdCliente === undefined || $scope.oSocioNegocio.seleccionado.IdCliente === null)
			{			
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_DEBE_SELECCIONAR_CLIENTE")}, idVentana);
				return
			}

			if ($scope.oPortafolio.seleccionado.IdPortafolio === "" || $scope.oPortafolio.seleccionado.IdPortafolio === undefined || $scope.oPortafolio.seleccionado.IdPortafolio === null)
			{			
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_DEBE_SELECCIONAR_PORTAFOLIO")}, idVentana);
				return
			}
			datos.IdClasificacionTipoInstrumento = $scope.data.oDatosEvento.IdClasificacionTipoInstrumento || ''; // RF 
			datos.IdTipoInstrumento = $scope.data.oDatosEvento.IdTipoInstrumento || '';  // BON
			datos.IdCliente = $scope.oSocioNegocio.seleccionado.IdCliente || -1;
			datos.IdPortafolio = $scope.oPortafolio.seleccionado.IdPortafolio || -1;
			datos.FechaCondicionLiquidacion =  util.dateSPdate($scope.data.oDatosEvento.FechaPago);
			datos.SocioNegocio = $scope.oSocioNegocio.seleccionado.KdataTextTemplate;
			datos.Portafolio = $scope.oPortafolio.seleccionado.KdataTextTemplate;
		}

		var id1 = bcsComunes.ventanaAbrir("ConsultarCarterasModal",datos,false, false, idVentana);		
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(data){
        	if (data && data.IdInstrumento)
        	{
        		$scope.instrumentoCustodia = data;
	        	$scope.data.kngPrecio = $scope.instrumentoCustodia.ValorizarPor == 'P' ? Number($scope.instrumentoCustodia.Precio) : undefined;
		        $scope.data.kngTasa = $scope.instrumentoCustodia.ValorizarPor == 'T' ? Number($scope.instrumentoCustodia.Tasa) : undefined;
	        	$scope.instrumentoCustodia.NombreCompletoTipoReajuste = $scope.instrumentoCustodia.IdTipoReajuste.concat(' - ',$scope.instrumentoCustodia.NombreTipoReajuste);
	        	$scope.data.kngCorteMinimo = ($scope.instrumentoCustodia.CorteMinimo >0 ? $scope.instrumentoCustodia.CorteMinimo : 1) || 1;
	        	$scope.data.kngCantidad = $scope.instrumentoCustodia.Repeticiones;
	        	$scope.data.kngValorNominal = $scope.instrumentoCustodia.ValorNominal;
	        	$scope.data.kngFolioCartera = $scope.instrumentoCustodia.FolioRegistro;
	        	$scope.data.kngIdCartera = $scope.instrumentoCustodia.IdRegistro;
        	}
        
    	}); 
	};
	
	
	
	/*************************************************************************
	* VALORIZACION
	**************************************************************************/	

	$scope.limpiarDatosValorizacion = function()
	{
   		
   		$scope.data.kngValorValorizado 		= undefined;
   		$scope.data.kngInteresesCorridos 	= undefined;
   		$scope.data.kngReajustesVacCorrido 	= undefined;
   		$scope.data.kngValorActualizado		= undefined;
	};



   	$scope.data.valorizar = function(modo)
   	{	 
   		
   	
   		if($scope.valorizando==true) return;
   		
   		$scope.valorizando = true;
   		$scope.limpiarDatosValorizacion();
   		
   		var continuar=true;
   		
   		if(!$scope.instrumentoCustodia.IdInstrumento || $scope.instrumentoCustodia.IdInstrumento==undefined || $scope.instrumentoCustodia.IdInstrumento == '')
   			continuar=false;
   		
   		
   		if(modo==='TASA')
   		{
   			if ($scope.data.kngPrecio !== undefined && $scope.data.kngPrecio !==null && $scope.data.kngPrecio !== '')
   			{
   				$scope.data.kngTir = undefined;
   			}
   			else 
   				continuar=false;
   		}
   		else if(modo==='PRECIO')
   		{
   			if ($scope.data.kngTir!==undefined && $scope.data.kngTir!==null && $scope.data.kngTir!=='')
   			{
   				$scope.data.kngPrecio = undefined;
   			}
   			else 
   				continuar=false;
   		}
   		else 
   			continuar=false;
   		
   		
   		if(continuar==false)
   		{                             
   			bcsComunes.ventanaOverlay(idVentana,false);
   			bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_FALTA_DATOS_PARA_VALORIZAR'));
   			$scope.valorizando = false;
   			return;
   		}
   		
   		//LOG:
   		var vgroup = "VALORIZAR "+modo;
   		var vtime = "Tiempo ejecución";
   		console.group(vgroup);
   		console.time(vtime);
   		bcsComunes.ventanaOverlay(idVentana,true);
   		
   		
   		
   		var datos = {
			p_IdEmpresa 			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario 			: $scope.$root.SesionIdUsuario,
			p_IdModulo 				: 'CUS',
			p_IdPais 				: $scope.$root.SesionIdPaisSistema,
			p_OrigenProcesamiento 	: 'ORDEN_UI',
			p_TicketProcesamiento	: 5601,
			p_Instrumentos			: [{
				
				IdValorizacion					: 1,
				FechaOperacion					: $scope.SesionFechaSistema,
				FechaVencimientoOperacion 		: $scope.SesionFechaSistema,
				PlazoEnDiasOperacion 			: 0, //Solo para operaciones a plazo
				FechaValorizacion				: $scope.SesionFechaSistema,
				IdClasificacionTipoInstrumento	: $scope.instrumentoCustodia.IdClasificacionTipoInstrumento,
				IdTipoInstrumento				: $scope.instrumentoCustodia.IdTipoInstrumento,
				IdInstrumento					: $scope.instrumentoCustodia.IdInstrumento,
				Nemotecnico						: $scope.instrumentoCustodia.Nemotecnico,
				FechaVencimiento				: util.dateSPdate($scope.instrumentoCustodia.FechaVencimiento),
				FechaEmision					: util.dateSPdate($scope.instrumentoCustodia.FechaEmision),
				IdMonedaInstrumento				: $scope.instrumentoCustodia.IdMoneda,
				IdTipoReajusteInstrumento		: $scope.instrumentoCustodia.IdTipoReajuste,
				Cantidad						: ($scope.data.kngValorNominal!=null && $scope.data.kngValorNominal!=undefined) ? $scope.data.kngValorNominal : null,
				
				ValorRescate				: null,
				PrecioCompra				: ($scope.data.kngPrecio!=null&&$scope.data.kngPrecio!=undefined) ? $scope.data.kngPrecio : null,
				TasaCompra					: ($scope.data.kngTir!=null&&$scope.data.kngTir!=undefined) ? $scope.data.kngTir : null,
				MontoCompra					: null,//en moneda del instrumento		
				PrecioSubyacente			: null, 
				TasaSubyacente				: null, 
				PrecioGarantia				: null, 
				TasaGarantia				: null, 
				TasaTIF						: null,		
				TieneCorteDeCupon			: null,
				IdCliente					: null, 
				IdPortafolio				: null,
				TablaOrigen					: null,	
				IdUso						: null, 
				TipoCartera					: null, 
				IdTipoCriterioTasa			: null,				
			}],
		};
		console.info("Datos valorizar",datos);
		
		bcsServicioPlataforma.ValorizarInstrumento(datos,function(response){
			bcsComunes.ventanaOverlay(idVentana,false);
			if (response.P_OUT_ERRORESCONTADOR > 0){
				console.error('Error valorizacion',$translate.instant('UI_ERROR_RUTINA_VALORIZACION'));
				bcsComunes.msg("warning",'Valorizador',$translate.instant('UI_ERROR_RUTINA_VALORIZACION'));
			}
			else if(response.P_OUT_INSTRUMENTOSVALORIZADOS.length==0){
				console.error('Sin datos valorizados');
				var p=bcsComunes.ventanaMsgErr($translate.instant('UI_SIN_DATOS_VALORIZADOS'),idVentana);
				bcsComunes.ventanaPromesa(p).then(function(){		
					console.timeEnd(vtime);
					console.groupEnd(vgroup);
				});
			}else{
				var itemval = response.P_OUT_INSTRUMENTOSVALORIZADOS[0];
				$.each(response.P_OUT_INSTRUMENTOSVALORIZADOS,function(key,value) {
					
					if (value.DimensionTasa === 'COMP' && value.DimensionTiempo === 'AC')
					{
						console.info('Valorizacion sin Error');
						$scope.data.kngTir 					= value.Tasa;
						$scope.data.kngPrecio 				= value.Precio;
						$scope.data.kngValorValorizado 		= value.MontoCapitalCorteCuponMonedaInstrumento;
						$scope.data.kngInteresesCorridos 	= value.MontoInteresCorteCuponMonedaInstrumento;						
						$scope.data.kngReajustesVacCorrido	= value.MontoReajusteCorteCuponMonedaInstrumento;
						$scope.data.kngValorActualizado		= value.MontoMonedaInstrumento;
					}
				});
				console.timeEnd(vtime);
				console.groupEnd(vgroup);
				
			}
			$timeout(function(){$scope.valorizando=false},200)
   		},function(response){
   			$timeout(function(){$scope.valorizando=false},200)
			var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EN_COMUNICACION'),idVentana);
			bcsComunes.ventanaPromesa(p).then(function(){
				bcsComunes.ventanaOverlay(idVentana,false);
	   			console.error(response);
	   			console.timeEnd(vtime);
				console.groupEnd(vgroup);
			});
   		});

   	};

   	
	$scope.data.blurCantidadRepeticiones = function() 
	{		
		v_corteMin = $scope.data.kngCorteMinimo;
		v_cantidad = $scope.data.kngCantidad;
		
		if (typeof v_corteMin === 'undefined' || v_corteMin === 0) return;
		if (typeof v_cantidad === 'undefined' || v_cantidad === 0) return;
		
		$scope.data.kngValorNominal = v_corteMin * v_cantidad;
		
		$scope.limpiarDatosValorizacion();
	};
	

	$scope.data.blurValorNominal = function() 
	{	
		v_corteMin = $scope.data.kngCorteMinimo;
		v_valorNominal = $scope.data.kngValorNominal;
				
		if (typeof v_corteMin === 'undefined' || v_corteMin === 0) return;
		if (typeof v_valorNominal === 'undefined' || v_valorNominal === 0) return;
				
		if(v_valorNominal % v_corteMin !==0){
			bcsComunes.msg("warning",'Valor Nominal','Valor Nominal no es múltiplo del corte mínimo.');
			$scope.data.kngValorNominal = 0;
			$scope.limpiarDatosValorizacion();
			return;
		}
		
		$scope.data.kngCantidad = v_valorNominal / v_corteMin;
			
		
		$scope.limpiarDatosValorizacion();
	};
	
	
});


