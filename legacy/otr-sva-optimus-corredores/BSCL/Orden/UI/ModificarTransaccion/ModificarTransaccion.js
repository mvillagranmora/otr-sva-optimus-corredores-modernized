angular.module('ModuloOrdenes').controller('ModificarTransaccionCtrl',
			function($scope,bcsComunes,bcsConsultasOrdenes,bcsConfiguracionOrdenesV2,bcsServicioOrden)
{
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var datosEntrada = bcsComunes.ventanaParams(idVentana);
	
	$scope.formData = {};
	
	$scope.formData.filtrosLibroOrdenes = {
			EstadoLibroOrdenes 	: { "field"	: "EstadoLibroOrdenes"	,"operator"	:	"eq", "value"   : "ABT" },
			IdEmpresa:{field:"IdEmpresa",operator:"eq",value:$scope.SesionIdEmpresa},	
		};
	
	$scope.formData.filtrosCorredorCorresponsal = {
			IdPais : { field   : "IdPais",operator: "eq", value   : "" }
		};
	
	
	//Activar o desactivar elementos del formulario(dependiendo desde donde se invocó)
	$scope.formData.disabled 	= false;
	$scope.formData.requerido 	= true;	
	$scope.formData.activado	= true;
	
	if (datosEntrada.OrigenLlamadaModificar === 'TES.GestionContrapartes') //Proviene desde Tesoreria
	{	
		$scope.formData.disabled 	= true;
		$scope.formData.requerido 	= false;
		$scope.formData.activado 	= false;
	}
	
	
	$scope.oConfiguracionGrupoCamara = {
			tipo 			: "combobox-grupo-camara",
			configuracion 	: {},
			filtros 		: {
								 IdCamaraDeCompensacion     : { "field"   : "IdCamaraDeCompensacion",    "operator": "eq", "value"   : "CCLV"},
								 IdPaisCamaraDeCompensacion : { "field"   : "IdPaisCamaraDeCompensacion","operator": "eq", "value"   : $scope.SesionIdPaisSistema},
								 IdEstadoGrupoCamara        : { "field"   : "IdEstadoGrupoCamara",       "operator": "eq", "value"   : "ACT"},
								 SecuenciaAgrupacion        : { "field"   : "SecuenciaAgrupacion",       "operator": "eq", "value"   : "GRUPO3" },
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};		
	var ConsultarTransacciones = datosEntrada.IdClasificacionTipoInstrumento === "RE"?'ConsultarTransaccionesReporteUI':'ConsultarTransaccionesUI';
	
	if (!datosEntrada.IdTransaccion)
		console.log("Transaccion no existe!");
	else {
		var transaccionDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
    			transport: {
    				read: {
    					url: bcsConsultasOrdenes(ConsultarTransacciones),
    					dataType: "json"
		    		}
				},
				filter: { field: "IdTransaccion", operator: "eq", value: Number(datosEntrada.IdTransaccion) },
				schema: bcsConfiguracionOrdenesV2.getSchemaTransaccion()
			}
		});
		
		transaccionDatasource.fetch().then(function(){
			if (transaccionDatasource.data().length!=1) {
				console.log("Transaccion no existe!");
				return false;
			}
			else {
				$scope.$apply(function() {
					transaccionDatasource.data()[0].Precio = Number(transaccionDatasource.data()[0].Precio);
					transaccionDatasource.data()[0].EsFungible = transaccionDatasource.data()[0].EsFungible === 'S';
					transaccionDatasource.data()[0].EsVentaCorta = transaccionDatasource.data()[0].EsVentaCorta === 'S';
					transaccionDatasource.data()[0].EsCuentaPropia = transaccionDatasource.data()[0].EsCuentaPropia === 'S';
					transaccionDatasource.data()[0].EsBilateral = transaccionDatasource.data()[0].EsBilateral === 'S';
					transaccionDatasource.data()[0].BloqueoLiquidacion = transaccionDatasource.data()[0].BloqueoLiquidacion === 'S';
					
					$scope.transaccionOriginal = $.extend({},transaccionDatasource.data()[0]);

					if($scope.SesionLocale==="CL")
						$scope.oConfiguracionGrupoCamara.filtros.CodigoCamara = {"field": "Camaras", "operator": "contains", "value" : $scope.obtenerFiltroCamara($scope.transaccionOriginal)};


					$scope.transaccion = $.extend({},transaccionDatasource.data()[0]);
					$scope.formData.filtrosCorredorCorresponsal.IdPais = $scope.transaccion.IdPaisBolsa;
					
					$scope.formData.decimalesMonto = util.bcs_formato(0,'ORD','Cantidad',$scope.transaccion.IdMonedaInstrumento,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					$scope.formData.decimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.transaccion.IdMonedaInstrumento,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
					$scope.formData.decimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.transaccion.IdMonedaInstrumento,$scope.transaccion.IdTipoInstrumento,'').cantidadDecimales;
				});
			}
		});
	}

	$scope.obtenerFiltroCamara = function(transaccion){
		var idCamara = undefined;
		var condicionLiquidacion = transaccion.CodigoCondicionDeLiquidacion;
		var idClasificacionTipoInstrumento = transaccion.IdClasificacionTipoInstrumento;
		var idTipoInstrumento = transaccion.IdTipoInstrumento;

		// Asignación de camara CCLV a las transacciones para Clearing (esto no indica que vaya a clearing ya que existen otras condiciones que van en la consulta y procesamiento de clearing)
		// Esta logica debe ser la misma en:  CrearTransaccion.java / ModificarTransaccion.js / ConsultarClearingFinanciero.sql / ModificarTransaccionMarketData.java
		if( (condicionLiquidacion==="T2" || condicionLiquidacion==="CN") && idClasificacionTipoInstrumento==="RV"){
			idCamara = "RV";  // Toda operacion RV, CN (Incluye simultaneas)) --> Camara RV
		}else if( (condicionLiquidacion==="T1" || condicionLiquidacion==="PM") && (idClasificacionTipoInstrumento==="RF" || idClasificacionTipoInstrumento==="IF")){
			idCamara = "PM";  // Toda operacion RF o IF  T1 --> Camara PM
		}else if( (condicionLiquidacion==="T0" || condicionLiquidacion==="PH")  && (idClasificacionTipoInstrumento==="RF" || idClasificacionTipoInstrumento==="IF")){
			idCamara = "PH";  // Toda operacion RF o IF  T0 --> Camara PH
		}else if( (condicionLiquidacion==="T2" || condicionLiquidacion==="CN")  && (idClasificacionTipoInstrumento==="RF" || idClasificacionTipoInstrumento==="IF")){
			idCamara = "PH";  // Toda operacion RF o IF  T2 --> Camara PH	
		}else if((condicionLiquidacion==="T0"  || condicionLiquidacion==="PH") && idTipoInstrumento==="SIM"){
			idCamara = "SM";  // Toda operacion de SIMULTANEA, T0 --> Camara SM
		}else{
			idCamara = "";
		}
		return idCamara;
	}

	$scope.formData.modificarTransaccion = function() {
					
			var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
    			p_IdUsuario 		: $scope.SesionIdUsuario,
    			p_IdModulo 			: 'ORD',
    			p_UsaReglasNegocio 	: 'S',
    			p_Transacciones		: [{
    				IdTransaccion 		: $scope.transaccion.IdTransaccion,
        			Monto 				: $scope.transaccion.MontoTransaccion || 0,
        			Precio 				: $scope.transaccion.PrecioTransaccion !== $scope.transaccionOriginal.PrecioTransaccion?$scope.transaccion.PrecioTransaccion:undefined,
        			Tasa 				: $scope.transaccion.Tasa !== $scope.transaccionOriginal.Tasa?$scope.transaccion.Tasa:undefined,
        			ValorRescate 		: $scope.transaccion.ValorRescateTransaccion !== $scope.transaccionOriginal.ValorRescateTransaccion?$scope.transaccion.ValorRescateTransaccion:undefined,
        			Cantidad 			: $scope.transaccion.CantidadTransaccion !== $scope.transaccionOriginal.CantidadTransaccion?$scope.transaccion.CantidadTransaccion:undefined,
        			IdLibroOrdenes 		: $scope.transaccion.IdLibroOrdenes !== $scope.transaccionOriginal.IdLibroOrdenes?$scope.transaccion.IdLibroOrdenes:undefined,
        			FechaEmision 		: ($scope.transaccion.FechaEmision!==null && $scope.transaccion.FechaEmision !== $scope.transaccionOriginal.FechaEmision)?util.dateSPdate($scope.transaccion.FechaEmision):undefined,
					MontoAPlazo			: $scope.transaccion.MontoAPlazo !== null ? $scope.transaccion.MontoAPlazo: undefined,
					PrecioAPlazo		: $scope.transaccion.PrecioAPlazo !== null ? $scope.transaccion.PrecioAPlazo: undefined,
					IdTipoInstrumento	: $scope.transaccion.IdTipoInstrumento,
        			EsBilateral		 				: $scope.transaccion.EsBilateral ? 'S' : 'N',
        			BloqueoLiquidacion 				: $scope.transaccion.BloqueoLiquidacion ? 'S' : 'N',
        			FechaLiquidacion 	 			: util.dateSPdate($scope.transaccion.FechaLiquidacion),
					FechaVencimiento 	 			: util.dateSPdate($scope.transaccion.FechaVencimiento),
        			MontoRetencion 	 				: $scope.transaccion.MontoRetencion || 0,
        	    	AccionTipoOperacion				: $scope.transaccion.AccionTipoOperacion,
        	    	TipoCambioCorresponsal			: $scope.transaccion.TipoCambioCorresponsal || 0,
        	    	IdCorredorCorresponsal			: $scope.transaccion.IdCorredorCorresponsal || 0,
        	    	MontoLiquidacionCorresponsal 	: $scope.transaccion.MontoLiquidacionCorresponsal || 0,
        	    	GastosCorresponsal				: $scope.transaccion.GastosCorresponsal || 0,
        	    	MarcaNegociacion				: $scope.transaccion.MarcaNegociacion || '',
        	    	IdMonedaLiquidacionCorresponsal	: $scope.transaccion.IdMonedaLiquidacionCorresponsal || '',    	    	
        	    	IdCamara					    : $scope.oConfiguracionGrupoCamara.seleccionado.CodigoGrupo || ''  //Para clearing financiero
    			}]
    			
			}
		
			console.log("Datos:" + JSON.stringify(datos));			
			bcsComunes.ventanaOverlay(idVentana,true);			
			
			bcsServicioOrden.ModificarTransaccion(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				var id = bcsComunes.ventanaMsg({
						mensaje:'',
						conflictos:util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaPromesa(id).then(function(result2){
					if (result.P_OUT_ERRORESCONTADOR === 0)
						bcsComunes.ventanaCerrar(idVentana);
				})
			},function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsg({
					mensaje:"UI_EXISTEN_CONFLICTOS",
					conflictos:util.parseServerResponse(result)
				},idVentana);
			} );
		
		
	};
	
	$scope.formData.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}

	$scope.$watchGroup( ["transaccion.CantidadTransaccion", "transaccion.PrecioTransaccion"], function(newValues, oldValues,scope){
		if(scope.transaccion && scope.transaccion.IdClasificacionTipoInstrumento &&  scope.transaccion.IdClasificacionTipoInstrumento==="RV"){
			scope.transaccion.MontoTransaccion = (newValues[0] && newValues[1]) ? newValues[0] * newValues[1] : scope.transaccion.MontoTransaccion;
			if(scope.transaccion.IdTipoInstrumento && scope.transaccion.IdTipoInstrumento !== null){
				if(scope.transaccion.IdTipoInstrumento === 'SIM'){
					$scope.calcularPrecioAPlazo(scope.transaccion);
				}
			}
		}
		
	});

	$scope.$watch('transaccion.EsBilateral',function(newval,oldval)
	{	    
		if (newval !== undefined)
		{	
			if ($scope.transaccion.EsBilateral)
			{
				$scope.oConfiguracionGrupoCamara.idActual = 0;
			}
		}			
	});

	$scope.calcularPrecioAPlazo = function(transaccion){
		if (transaccion.Tasa && transaccion.Plazo  && transaccion.PrecioTransaccion
			&& transaccion.IdClasificacionTipoInstrumento  && transaccion.IdTipoInstrumento ){

			var precioAPlazo = 0;
			if (transaccion.IdClasificacionTipoInstrumento === 'RE') {
				precioAPlazo = bcsComunes.calcularMontoPlazoInteresCompuesto(transaccion.PrecioTransaccion
					,transaccion.Plazo,Number(transaccion.Tasa),360);
			}
			else {
				precioAPlazo = bcsComunes.calcularMontoPlazo(transaccion.PrecioTransaccion,transaccion.Plazo,Number(transaccion.Tasa));	
			}
			transaccion.PrecioAPlazo = util.bcs_formato(precioAPlazo,'GEN','Precio','CLP',transaccion.IdTipoInstrumento,'').valorNumerico;
			var montoAPlazo = 0;
			if (transaccion.IdTipoInstrumento === 'RRF'){
				montoAPlazo = (precioAPlazo/100) * transaccion.CantidadTransaccion;
			}
			else{
				montoAPlazo = precioAPlazo * transaccion.CantidadTransaccion;
			}
			transaccion.MontoAPlazo = montoAPlazo;
			
		}
	}

});


