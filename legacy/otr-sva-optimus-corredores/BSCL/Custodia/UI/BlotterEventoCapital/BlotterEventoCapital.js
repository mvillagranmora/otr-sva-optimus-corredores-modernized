
angular.module('ModuloCustodia').controller('BlotterEventoCapitalCtrl',function($scope,bcsComunes,$timeout,$modal,$translate){
	var bEC = this;
	
	$scope.select = function(grid)
	{
		$timeout(function() {			
			if ($("#" + grid).find(".k-grid").data("kendo-grid"))	
				$("#" + grid).find(".k-grid").data("kendo-grid").refresh()
		},10);
	};
	
	bEC.cargaFiltroCliente = function (vars){
		
		// HABILITACION DE BOTONES
		bEC.ActivaBotonModificarEv		= false;
		bEC.ActivaBotonEliminarEv		= false;
		bEC.ActivaBotonProcesarEv		= false;
		bEC.ActivaBotonNotificarEv		= false;
		bEC.ActivaBotonReprocesarEv		= false;
		bEC.ActivaBotonRenovarEv		= false;
		bEC.ActivaBotonIndicesEv		= false;
		bEC.ActivaBotonCerrarEv			= false;
//		bEC.ActivaBotonCorteCuponEv		= true;
		bEC.ActivaBotonSorteoLetrasEv	= false;
		bEC.ActivaBotonActualizarEv		= false;
		
		if(vars.length == 1){
			if (vars[0].IdTipoEventoCapital == 'SU'){
				bEC.ActivaBotonCertificar		= true;
//				bEC.ActivaBotonCorteCuponEv		= false;
				bEC.ActivaBotonSorteoLetrasEv	= false;
				bEC.ActivaBotonActualizarEv		= false;
			}
			else
			{
//				if (vars.IdTipoEventoCapital == 'CC'){
//					bEC.ActivaBotonCorteCuponEv		= true;
//					bEC.ActivaBotonSorteoLetrasEv	= false;
//					bEC.ActivaBotonActualizarEv		= false;
//				}
				if (vars[0].IdTipoEventoCapital == 'SL'){
//					bEC.ActivaBotonCorteCuponEv		= false;
					bEC.ActivaBotonSorteoLetrasEv	= true;
					bEC.ActivaBotonActualizarEv		= false;
					bEC.ActivaBotonModificarEv	= false;
				}
				if (vars[0].IdTipoEventoCapital == 'EP'){
//					bEC.ActivaBotonCorteCuponEv		= false;				
					bEC.ActivaBotonSorteoLetrasEv	= false;
					if (vars[0].IdEstado == 'PRO'){
						bEC.ActivaBotonActualizarEv		= true;
						bEC.ActivaBotonNotificarEv		= true;
					};
					if (vars[0].IdEstado == 'NOT') {
						bEC.ActivaBotonActualizarEv		= true;
					}
				}			
				bEC.ActivaBotonCertificar = false;
			};
			
			if (vars[0].IdEstado == 'VIG'){
				bEC.ActivaBotonModificarEv	= true;
				bEC.ActivaBotonEliminarEv	= true;
				bEC.ActivaBotonProcesarEv	= true;
				bEC.ActivaBotonNotificarEv	= true;
				bEC.ActivaBotonReprocesarEv	= false;
				bEC.ActivaBotonIndicesEv	= true;
				
				if (vars[0].IdTipoEventoCapital == 'CC' || vars[0].IdTipoEventoCapital == 'SU'){
					bEC.ActivaBotonCerrarEv		= true;			
				}
				else
				{
					bEC.ActivaBotonCerrarEv		= false;	
				}
			}
			else
			{
				if ((vars[0].IdEstado == 'PRO') || (vars[0].IdEstado == 'ACT') || (vars[0].IdEstado == 'MOD')){
					
					if (vars[0].IdTipoEventoCapital == 'SL' && vars[0].OperacionesVigentes == 'S'){
						bEC.ActivaBotonCerrarEv		= false;
					}else{
						bEC.ActivaBotonCerrarEv		= true;
					}
					bEC.ActivaBotonModificarEv	= true;
					bEC.ActivaBotonEliminarEv	= true;
					bEC.ActivaBotonProcesarEv	= false;
					bEC.ActivaBotonReprocesarEv	= true;
					bEC.ActivaBotonRenovarEv	= false;
				}
				if (vars[0].IdEstado == 'NOT'){
					bEC.ActivaBotonCerrarEv		= true;
					bEC.ActivaBotonModificarEv	= true;
					bEC.ActivaBotonEliminarEv	= false;
					bEC.ActivaBotonProcesarEv	= true;
					bEC.ActivaBotonReprocesarEv	= true;
					bEC.ActivaBotonRenovarEv	= false;
				}
				if (vars[0].IdEstado == 'CER'){
					bEC.ActivaBotonCerrarEv		= false;
					bEC.ActivaBotonModificarEv	= false;
					bEC.ActivaBotonEliminarEv	= false;
					bEC.ActivaBotonProcesarEv	= false;
					bEC.ActivaBotonReprocesarEv	= false;
					bEC.ActivaBotonRenovarEv	= true;
				}
				if (vars[0].IdEstado == 'PEN'){
					bEC.ActivaBotonCerrarEv		= false;
					bEC.ActivaBotonModificarEv	= false;
					bEC.ActivaBotonEliminarEv	= false;
					bEC.ActivaBotonProcesarEv	= false;
					bEC.ActivaBotonReprocesarEv	= false;
					bEC.ActivaBotonRenovarEv	= false;
				}/*
				if (vars[0].IdTipoEventoCapital == 'SL'){
					bEC.ActivaBotonReprocesarEv	= false;
				}*/
				bEC.ActivaBotonNotificarEv	= true;
			};
			
			if (vars[0].IdTipoEventoCapital == 'SL'){
				bEC.ActivaBotonModificarEv	= false;
			}
			
			bEC.idActualEvento.TipoEvento	= vars[0].IdTipoEventoCapital; 
			bEC.idActualNumeroDividendo 	= vars[0].NumeroDividendo;
			bEC.idActualFechaPago 			= vars[0].FechaLimite;
			bEC.idActualFechaPagoHasta 		= vars[0].FechaPago;
			bEC.NemoActual	 				= vars[0].Nemo;
			bEC.IdEventoCapital 			= vars[0].IdEventoCapital;
			bEC.Secuencia 					= vars[0].Secuencia;
			bEC.ActivarBotones		 		= true;
			bEC.ActivarBotonModificarEvc	= true;
			
			if(vars[0].IdEstado == 'PRO')
				bEC.ActivarBotonIngresarEvc		= true;
			else
				if (vars[0].IdTipoEventoCapital == 'PB') {
					bEC.ActivarBotonIngresarEvc = true;
					bEC.ActivarBotonModificarEvc = false;
					bEC.ActivaBotonProcesarEv = false;
				}else if( vars[0].IdEstado == 'CER' || vars[0].IdEstado == 'REA'){
					switch (vars[0].IdTipoEventoCapital) {
						case 'DV':
							bEC.ActivarBotonIngresarEvc	=	true;
							break;
						case 'RC':
							bEC.ActivarBotonIngresarEvc	=	true;
							break;
						default:
							bEC.ActivarBotonIngresarEvc	=	false;
					}
					if(vars[0].IdEstado == 'REA'){
						bEC.ActivaBotonCerrarEv = true;
					}
					
				}
				else bEC.ActivarBotonIngresarEvc = false;

			//condicion replicada de ConsultarEventoCapital para reprocesar
			/*if (!(vars[0].IdTipoEventoCapital == 'DA' || vars[0].IdTipoEventoCapital == 'DV' || vars[0].IdTipoEventoCapital == 'CA' || vars[0].IdTipoEventoCapital == 'DS')) {
				bEC.ActivaBotonReprocesarEv	= false;
			}*/
		}
		else if(vars.length > 1){
			bEC.ActivaBotonEliminarEv		= true;
			bEC.ActivarBotones		 		= false;
			bEC.ActivarBotonIngresarEvc		= false;
			if(vars.length == vars.filter(bEC.validaActivacionBotonCerrar).length) 
				bEC.ActivaBotonCerrarEv = true;
			if(vars.length == vars.filter(bEC.validaActivacionBotonProcesar).length) 
				bEC.ActivaBotonProcesarEv = true;
			else bEC.ActivaBotonCerrarEv = false;
			
		}
	};
	bEC.validaActivacionBotonCerrar = function(eve) {

		if ((eve.IdEstado == 'VIG') && (eve.IdTipoEventoCapital == 'CC' || eve.IdTipoEventoCapital == 'SU')){
			return true;
		}else if ((eve.IdEstado == 'PRO') || (eve.IdEstado == 'ACT') || (eve.IdEstado == 'MOD')){
			
			if (eve.IdTipoEventoCapital == 'SL' && eve.OperacionesVigentes == 'S'){
				
				return false;
			}else{
				return true;
			}
		}else{
			return false;
		}
	};
	bEC.validaActivacionBotonProcesar = function(eve) {

		if ((eve.IdEstado == 'VIG') && (eve.IdTipoEventoCapital == 'SL')){
			return true;
		}else{
			return false;
		}
	};

	$scope.$on('evento:tipoSeleccionado', function(event, tipoEvento) {
	
		$timeout(function() {
			bEC.actualizarColumnas(tipoEvento);
		}, 100);
	});	

	
	bEC.actualizarColumnas= function(IdTipoEventoCapital) {
	console.info("INICIO::actualizarColumnas");
	const startTime = performance.now();
		var attempts = 0;
		var maxAttempts = 30; 
		var delay = 100;
	
		function tryFindAndUpdate() {
			attempts++;
	
				var $container = $("#gridEventoCapitalCliente");
			var kgrid = null;
			if ($container.length) {
				kgrid = $container.data("kendoGrid") || $container.data("kendo-grid") || $container.find(".k-grid").data("kendoGrid");
			}
	
			if (!kgrid) {
				if (attempts < maxAttempts) {
			
					setTimeout(tryFindAndUpdate, delay);
				} else {
					console.warn("actualizarColumnas: NO se encontró Kendo Grid tras intentos");
				}
				return;
			}
	
			var columnaMontoCapital = (kgrid.columns || []).find(c => c && (c.field === "MontoCapital"));
			var columnaFactorNuevo = (kgrid.columns || []).find(c => c && (c.field === "FactorNuevo"));

			var tituloMontoCapital = (IdTipoEventoCapital === "CC" || IdTipoEventoCapital === "CORTE CUPON")
				? ($translate ? $translate.instant("UI_AMORTIZACION") : "Amortización")
				: ($translate ? $translate.instant("UI_MONTO_CAPITAL") : "Monto Capital");

			var tituloFactorNuevo = (IdTipoEventoCapital === "EP" || IdTipoEventoCapital === "EMISION PAGADA")
				? ($translate ? $translate.instant("UI_VALOR_SUSCRIPCION") : "Valor Suscripción")
				: ($translate ? $translate.instant("UI_ACCIONES_NUEVAS") : "Acciones Nuevas");

			if (columnaMontoCapital) columnaMontoCapital.title = tituloMontoCapital;
			if (columnaFactorNuevo) columnaFactorNuevo.title = tituloFactorNuevo;

			if (kgrid.thead) {
				if (columnaMontoCapital) {
					var $thMonto = kgrid.thead.find("th[data-field='MontoCapital'] .k-link");
					if ($thMonto.length) $thMonto.text(tituloMontoCapital);
				}
				if (columnaFactorNuevo) {
					var $thCant = kgrid.thead.find("th[data-field='FactorNuevo'] .k-link");
					if ($thCant.length) $thCant.text(tituloFactorNuevo);
				}
			}

			try {
				if (kgrid.thead) {
					var $th = kgrid.thead.find("th[data-field='MontoCapital'] .k-link");
					if ($th && $th.length) {
						$th.text(titulo);
						updated = true;
					}
				}
			} catch (e) {
				console.warn(e);
			}


			try {
				kgrid.setOptions({ columns: kgrid.columns });
			} catch (e) {
				
			}
	
			try { kgrid.refresh(); } catch (e) { console.warn(e); }
	

		} 
	
		tryFindAndUpdate();
		const endTime = performance.now();
        console.info(`FIN::actualizarColumnas, tiempo transcurrido: ${(endTime - startTime).toFixed(2)} ms`);
	};

	

});