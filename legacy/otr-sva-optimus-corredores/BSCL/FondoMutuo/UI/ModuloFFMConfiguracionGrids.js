angular.module('ModuloFFM').factory('bcsConfiguracionFFM',function($translate,$rootScope,bcsComunes,bcsServicioFondosMutuos, $timeout,$http) {
	var obj = {};
	
	obj.ingresarCerrarVentana = function(id, idPadre){
		 if(id){
			 var promesa=bcsComunes.ventanaPromesa(id);                               
		     $.when(promesa).then(function(data){
		    	 $timeout(function() {
		    		 bcsComunes.ventanaCerrar(idPadre);
		    	 },20);
		     }); 
		 }else{
			 bcsComunes.ventanaCerrar(idPadre); 
		 }
	 }
	
	obj.validarFiltros = function(filtros,modo)
	{
		var filters = new Array();
		if(modo != 'REGIMEN' && modo != 'OMNIBUS')
			filters.push({ field: "IdEmpresa", operator: "eq", value: $rootScope.SesionIdEmpresa});
		if (filtros.FechaCurse)
			filters.push({ field: "FechaAceptacion", operator: "eq", value: util.dateOdata(filtros.FechaCurse)});
		if (filtros.FechaLiquidacion)
			filters.push({ field: "FechaLiquidacion", operator: "eq", value: util.dateOdata(filtros.FechaLiquidacion)});
		if (filtros.fechaDesde)
			filters.push({ field: "FechaAceptacion", operator: "gte", value: util.dateOdata(filtros.fechaDesde)});
		if (filtros.fechaHasta)
			filters.push({ field: "FechaAceptacion", operator: "lte", value: util.dateOdata(filtros.fechaHasta)});
		if (filtros.fechaIngresoDesde)
			filters.push({ field: "FechaIngreso", operator: "gte", value: util.dateOdata(filtros.fechaIngresoDesde)});
		if (filtros.fechaIngresoHasta)
			filters.push({ field: "FechaIngreso", operator: "lte", value: util.dateOdata(filtros.fechaIngresoHasta)});
		if (filtros.IdTipoMovimientoFondoMutuo)
			filters.push({ field: "IdTipoMovimientoFondoMutuo", operator: "eq", value: filtros.IdTipoMovimientoFondoMutuo });
		if (filtros.IdEstado && modo != 'REGIMEN' && modo != 'CUADRATURA' && modo != 'OMNIBUS')
			filters.push({ field: "EstadoSolicitud", operator: "eq", value: filtros.IdEstado });
		if (filtros.IdEstado && modo == 'REGIMEN')
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
		if (filtros.IdEmisor)
			filters.push({ field: "IdEmisor", operator: "eq", value: filtros.IdEmisor });
		if (filtros.FondoMutuo)
			filters.push({ field: "IdSerieFondo", operator: "eq", value: filtros.FondoMutuo.IdSerieFondo});
		if (filtros.IdSerieFondo)
			filters.push({ field: "IdSerieFondo", operator: "eq", value: filtros.IdSerieFondo});
		if (filtros.EsNacional){
			var esNacional = filtros.EsNacional ? 'S' : 'N';
			filters.push({ field: "EsFondoNacional", operator: "eq", value: esNacional});
		}
		if (filtros.PerfilFondoMutuo)
			filters.push({ field: "Perfil", operator: "eq", value: filtros.PerfilFondoMutuo});
		if (filtros.IdMoneda)
			filters.push({ field: "IdMoneda", operator: "eq", value: filtros.IdMoneda});
		if (filtros.TipoSolicitud)
			filters.push({ field: "TipoSolicitud", operator: "eq", value: filtros.TipoSolicitud});
		if (filtros.Switch)
			filters.push({ field: "Switch", operator: "eq", value: filtros.Switch});
		if (filtros.fechaCargaDesde)
			filters.push({ field: "FechaCalculo", operator: "gte", value: util.dateOdata(filtros.fechaCargaDesde)});
		if (filtros.fechaCargaHasta)
			filters.push({ field: "FechaCalculo", operator: "lte", value: util.dateOdata(filtros.fechaCargaHasta)});
		if (filtros.EsDiferido)
			filters.push({ field: "EsDiferido", operator: "eq", value: filtros.EsDiferido});
		if (filtros.IdPaisOrigen)
			filters.push({ field: "IdPaisOrigen", operator: "eq", value: filtros.IdPaisOrigen});
		if (filtros.FechaCertificacion)
			filters.push({ field: "FechaCertificacion", operator: "eq", value: filtros.FechaCertificacion});
		if (filtros.FechaSistema)
			filters.push({ field: "FechaSistema", operator: "eq", value: filtros.FechaSistema});
		if(filtros.IdCliente && modo == 'SOLICITUD')
			filters.push({ field: "IdCliente", operator: "eq", value: filtros.IdCliente})
		if(filtros.IdSolicitudRescate)
			filters.push({ field: "IdSolicitudRescate", operator: "eq", value: filtros.IdSolicitudRescate})
		if(filtros.IdPortafolio)
			filters.push({ field: "IdPortafolio", operator: "eq", value: filtros.IdPortafolio})
		if(filtros.IdTipoMovimientoCarga)
			filters.push({ field: "TipoMovimiento", operator: "eq", value: filtros.IdTipoMovimientoCarga})
		if(filtros.TipoRegistro)
			filters.push({ field: "TipoRegistro", operator: "eq", value: filtros.TipoRegistro})
		if(filtros.IdEstadoFondo)
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstadoFondo})
		if(filtros.IdTipoInstrumento)
			filters.push({ field: "IdTipoInstrumento", operator: "eq", value: filtros.IdTipoInstrumento})
		if(filtros.IdFondo)
			filters.push({ field: "IdFondo", operator: "eq", value: filtros.IdFondo})
		if (filtros.fechaVigenciaDesde)
			filters.push({ field: "FechaVigencia", operator: "gte", value: filtros.fechaVigenciaDesde});
		if (filtros.fechaVigenciaHasta)
			filters.push({ field: "FechaVigencia", operator: "lte", value: filtros.fechaVigenciaHasta});
		if(filtros.IdProducto && modo == 'SOLICITUD' || filtros.IdProducto === 0 && modo == 'SOLICITUD')
			filters.push({ field: "IdProductoAgrupador", operator: "eq", value: filtros.IdProducto});
		if(modo === 'CUADRATURA'){
			if(filtros.IdInstrumento)
				filters.push({ field: "IdInstrumento", operator: "eq", value: filtros.IdInstrumento})
			if(filtros.FechaPagoDesde)
				filters.push({ field: "FechaPago", operator: "gte", value: util.dateOdata(filtros.FechaPagoDesde)});
			if(filtros.FechaPagoHasta)
				filters.push({ field: "FechaPago", operator: "lte", value: util.dateOdata(filtros.FechaPagoHasta)});
			if(filtros.IdTipoEventoCapital)
				filters.push({ field: "IdTipoEventoCapital", operator: "eq", value: filtros.IdTipoEventoCapital});
			if(filtros.IdEstado)
				filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado});
			if(filtros.IdDividendo)
				filters.push({ field: "IdDividendo", operator: "eq", value: filtros.IdDividendo});
		}
		if(modo === 'CUADRATURAAGF'){
			if(filtros.fechaCuadratura){
				filters.push({ field: "FechaCuadratura", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaCuadratura))});
				filters.push({ field: "FechaCuadratura", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaCuadratura))});
			}				
		}
		if(modo === 'OMNIBUS'){
			if(filtros.fechaOmnibus){
				filters.push({ field: "FechaOmnibus", operator: "gte", value: util.dateOdata(util.dateInicioDia(filtros.fechaOmnibus))});
				filters.push({ field: "FechaOmnibus", operator: "lte", value: util.dateOdata(util.dateTerminoDia(filtros.fechaOmnibus))});
			}
			
			if(filtros.IdEstado){
				filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
			}
		}
		if(modo == 'SOLICITUD'){
			if(filtros.TiposMovimientoLiquidacion){
				filters.push({"logic": "or", filters:filtros.TiposMovimientoLiquidacion})
			}
			if(filtros.IdEstadoLiquidacion){
				filters.push({ field: "EstadoLiquidacion", operator: "eq", value: filtros.IdEstadoLiquidacion});
			}
		}
		if(modo == 'CSC'){
			if(filtros.fecha){
				filters.push({field: "Fecha", operator: "eq", value: util.dateOdata(filtros.fecha)});
			}
			if(filtros.socioNegocio){
				filters.push({field: "IdCliente", operator: "eq", value: filtros.socioNegocio.IdCliente});
			}
			
			
		}

		return filters;
	}
	
	obj.getSchemaFondo = function() {
		return { 
			model: {
				fields : {
					CobraComision 	  		 		: { type : "string"},
					CodigoSVS 	  	  		 		: { type : "string"},
					CodigoSerie   	  		 		: { type : "string"},
					Descripcion   	  		 		: { type : "string"},
					Diferido      	  		 		: { type : "code"},
					EsComisionEntrada        		: { type : "string"},
					EsComisionEntradaUI      		: { type : "code"},
					EsComisionSalida         		: { type : "string"},
					EsComisionSalidaUI       		: { type : "code"},
					BloqueoAporteUI					: { type : "code"},
					BloqueoRescateUI				: { type : "code"},
					EsPatrimonioMinimoUI     		: { type : "string"},
					EsPatrimonioMinimo		 		: { type : "string"},
					EstadoAporte             		: { type : "code"},
					EstadoRescate            		: { type : "code"},
					EsFondoNacional					: { type : "string"},
					FechaCierre   	  		 		: { type : "date"},
					FechaConstitucion 		 		: { type : "date"},
					FechaCreacion 	  		 		: { type : "date"},
					FechaFondo 	  		 		    : { type : "sysdate"},
					HoraCierreAporteBackoffice		: { type : "time"},
					HoraCierreAporteComercial 		: { type : "time"},
					HoraCierreLiquidezBackoffice	: { type : "time"},
					HoraCierreLiquidezComercial		: { type : "time"},
					HoraCierreReinversionBackoffice	: { type : "time"},
					HoraCierreReinversionComercial	: { type : "time"},
					HoraCierreRescateBackoffice		: { type : "time"},
					HoraCierreRescateComercial		: { type : "time"},
					IdCategoriaPerfilFondoMutuo  	: { type : "string"},
					IdEmisor 				 		: { type : "string"},
					IdFamiliaFondo 		     		: { type : "string"},
					IdFondo 				 		: { type : "string"},
					IdInstrumento					: { type : "number"},
					IdMoneda 				 		: { type : "string"},
					IdSerieFondo             		: { type : "string"},
					IdSerieFondoNombre             	: { type : "string"},
					IdPaisOrigen 			 		: { type : "number"},
					IdPortafolio					: { type : "number"},
					IdRegimenTributario 	 		: { type : "string"},
					IdTipoFondo 			 		: { type : "string"},
					MoneyMarket 			 		: { type : "string"},
					MontoMaximo              		: { type : "number"},
					MontoMinimo              		: { type : "number"},
					Nemotecnico 			 		: { type : "string"},
					NombreCorto 			 		: { type : "string"},
					NombreFondo 			 		: { type : "string"},
					NombreFamilia 			 		: { type : "string"},
					NombreProposito			 		: { type : "string"},
					NombreSerieFondo 	     		: { type : "string"},
					Omnibus 			     		: { type : "string"},
					PropositoSerie 		     		: { type : "string"},
					RazonSocialEmisor 		 		: { type : "string"},
					TipoCambioDiferido	     		: { type : "number"},
					UsaHorarioAporteDefault	 		: { type : "string"},
					UsaHorarioAporteDefaultUI	 	: { type : "code"},
					UsaHorarioLiquidezDefault		: { type : "string"},
					UsaHorarioLiquidezDefaultUI		: { type : "code"},
					UsaHorarioReinversionDefault	: { type : "string"},
					UsaHorarioReinversionDefaultUI	: { type : "code"},
					UsaHorarioRescateDefault		: { type : "string"},
					UsaHorarioRescateDefaultUI		: { type : "code"},
					Vigencia 			     		: { type : "string"},
					ValorCuota    			 		: { type : "number"},
					EsDiferidoUI					: { type : "code"},
					EstadoAporteComercialUI			: { type : "code"},
					EstadoRescateComercialUI	    : { type : "code"},
					EstadoAporteBackOfficeUI		: { type : "code"},
					EstadoRescateBackOfficeUI		: { type : "code"},
					NombreEstado					: { type : "string"},
					NombreCategoriaPerfilFondoMutuo : { type : "string"},
					ValorDolarAporte				: { type : "number"},
					ValorDolarRescate				: { type : "number"},
					NombreTipoInstrumento			: { type : "string"},
					AportesAltos					: { type : "string"},
					PatrimonioAltos					: { type : "string"},
					AportesAltosUI					: { type : "string"},
					PatrimonioAltosUI				: { type : "string"},
					MontoPatrimonioEmisor    		: { type : "number"},
					InstrumentoOWP					: { type : "string"},
					EsAPV							: { type : "string"},
					InstrumentoOWPUI				: { type : "string"},
					EsAPVUI							: { type : "string"},
					FechaValorCuota		   	  		: { type : "sysdate"},
					RestringeAporteUI				: { type : "string"},
					RestringeAporte					: { type : "string"},
					RestringeRescateUI				: { type : "string"},
					RestringeRescate				: { type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaConfiguracionRegimenFondoMutuo = function() {
		return { 
			model: {
				fields : {
					IdRegimenFondoMutuoOrigen 	  		 		: { type : "string"},
					IdRegimenFondoMutuoDestino 	  	  		 	: { type : "string"},
					DescripcionRegimenFondoMutuoOrigen   		: { type : "string"},
					DescripcionRegimenFondoMutuoDestino   	  	: { type : "string"},
				}
			}
		}
	};
	
	obj.getSchemaConfiguracionPropositoPortafolio = function() {
		return { 
			model: {
				fields : {
					IdRegimenFondoMutuo 	  		 			: { type : "string"},
					IdPropositoPortafolio 	  	  		 		: { type : "string"},
					DescripcionPropositoSerieRelacionadoOrigen  : { type : "string"},
					DescripcionPropositoSerieRelacionadoDestino : { type : "string"},
				}
			}
		}
	};
	
	obj.getSchemaCargaOperaciones = function() {
		return { 
			model: {
				fields : {
					IdRegistroCarga 		 	 : 	{ type : "number"},
					IdCarga 				 	 : 	{ type : "number"},
					RutEmisor 	  		     	 : 	{ type : "string"},
					CodigoPortafolioEmisor 	 	 : 	{ type : "string"},
					TipoMovimiento   	     	 : 	{ type : "string"},
					FechaMovimiento   	     	 : 	{ type : "date"},
					FechaValorCuota   	     	 : 	{ type : "date"},
					IdSolicitudAporteRescate 	 : 	{ type : "number"},
					DocumentoIdentidad   	 	 : 	{ type : "string"},
					CodigoPortafolio   		 	 : 	{ type : "string"},
					CalculoPorMontoCuotaUI   	 : 	{ type : "code"},
					IdSerieFondo        	 	 : 	{ type : "string"},
					NumeroCuotas       	 	 	 : 	{ type : "number"},
					ValorCuota			 	 	 : 	{ type : "number"},
					Monto				 	 	 : 	{ type : "number"},
					MontoComision        	 	 : 	{ type : "number"},
					IdMoneda      		 	 	 : 	{ type : "string"},
					IdMonedaPago      		 	 : 	{ type : "string"},
					IdMedioDePago      		 	 : 	{ type : "string"},
					IdSerieFondoDestino	 	 	 : 	{ type : "string"},
					IdEmisor				 	 : 	{ type : "string"},
					IndicadorMLT			 	 : 	{ type : "string"},
					EsIndicadorMLTUI			 : 	{ type : "code"},
					FechaCarga				 	 : 	{ type : "date"},
					FechaIngresoRegistro	 	 : 	{ type : "sysdatetime"},
					Estado			 	 	 	 : 	{ type : "string"},
					EstadoProceso			 	 : 	{ type : "string"},
					Eliminado				 	 : 	{ type : "string"},
					IdMonedaFondo			 	 : 	{ type : "string"},
					RazonSocialCliente		 	 : 	{ type : "string"},
					NombreCompletoCliente	 	 : 	{ type : "string"},
					DescripcionPortafolioCliente : 	{ type : "string"},
					IdEmpresa				 	 : 	{ type : "number"},
					DescEstado       	 	 	 : 	{ type : "code"},
					NombreEstadoProceso	 	 	 : 	{ type : "string"},
					DecimalesCuota		 	 	 : 	{ type : "number"},
					DecimalesValorCuota		 	 : 	{ type : "number"},
					DescripcionTipoMovimiento	 : 	{ type : "string"},
					Concepto					 :  { type : "string"},
					NombreEstado				 :  { type : "string"},
					DescEstadoProceso			 :  { type : "code"},
					EsActualizacion				 :	{ type : "string"},
					IdRegimenTributarioOrigen	 :  { type : "string"},
					IdRegimenTributarioDestino	 :  { type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaResumenCargaOperaciones = function() {
		return { 
			model: {
				fields : {
					IdCarga					  : { type : "number"},
					FechaCarga				  : { type : "date"},
					FechaMovimiento		      : { type : "date"},
					NombreTipoMovimiento 	  : { type : "string"},
					IdMoneda				  : { type : "string"},
					IdEmisor   	     		  : { type : "string"},
					RutEmisor				  : { type : "string"},
					RegistrosProcesado   	  : { type : "number"},
					RegistrosError   	 	  : { type : "number"},
					RegistrosProcesoPendiente : { type : "number"},
					RegistrosProcesoProcesado : { type : "number"},
					RegistrosProcesoError     : { type : "number"},
					Monto   				  : { type : "number"},
					NumeroCuotas   		      : { type : "number"}
				}
			}
		}
	};
	
	obj.getSchemaFondosCustodia = function() {
		return { 
			model: {
				fields : {
					IdSerieFondo 	  		 	: { type : "string"},
					IdSerieFondoNombre 	  	    : { type : "string"},
					IdCliente   	  		 	: { type : "number"},
					IdPortafolio   	  		 	: { type : "number"},
					IdFondo      	  		 	: { type : "string"},
					IdEmisor					: { type : "string"},
					Nemotecnico        			: { type : "string"},
					NombreFondo      			: { type : "string"},
					IdTipoFondo         		: { type : "string"},
					IdRegimenTributario       	: { type : "string"},
					IdPaisOrigen				: { type : "string"},
					CodigoSVS					: { type : "string"},
					IdCategoriaPerfilFondoMutuo : { type : "string"},
					ValorCuota		 			: { type : "number"},
					MoneyMarket             	: { type : "string"},
					EsMoneyMarketUI            	: { type : "code"},
					NombreCorto					: { type : "string"},
					PropositoSerie   	  		: { type : "string"},
					EsComisionEntradaUI 		: { type : "code"},
					EsPatrimonioMinimoUI 	  	: { type : "code"},
					EsComisionSalidaUI			: { type : "code"},
					IdMoneda 					: { type : "string"},
					NombreSerieFondo			: { type : "string"},
					DecimalesValorCuota			: { type : "number"},
					ValorDolarAporte			: { type : "number"},
					ValorDolarRescate			: { type : "number"},
					DecimalesCuota				: { type : "number"},
					EstadoAporteComercialUI		: { type : "code"},
					EstadoRescateComercialUI	: { type : "code"},
					EstadoAporteBackOfficeUI	: { type : "code"},
					EstadoRescateBackOfficeUI	: { type : "code"},
					EsDiferidoUI				: { type : "code"},
					BloqueoAporteUI				: { type : "code"},
					BloqueoRescateUI			: { type : "code"},
					NombreEstado				: { type : "string"},
					NombreCategoriaPerfilFondoMutuo : { type : "string"},
					NombreTipoInstrumento		: { type : "string"},
					NombreFamilia				: { type : "string"},
					MontoMinimo					: { type : "number"},
					MontoMaximo					: { type : "number"}
				}
			}
		}
	};
	
	obj.getSchemaSolicitudAporteRescate = function(){
		return {
			model : {
				fields : {
					IdSolicitudAporteRescate				: { type : "number"},
					IdCliente								: { type : "number"},
					IdPortafolio							: {	type : "number"},
					FechaEmision 							: { type : "date"},
					FechaIngresoRegistro 					: { type : "datetime"},
					IdTipoMovimientoFondoMutuo 				: { type : "string"},
					TipoSolicitud							: { type : "string"},
					DescripcionTipoMovimiento				: { type : "string"},
					EstadoSolicitud 						: { type : "string"},
					EstadoLiquidacion						: { type : "string"},
					EsIndicadorMLTUI						: { type : "code"},
					NombreEstado 							: { type : "string"},
					NombreEstadoLiquidacion					: { type : "string"},
					NombreFondo 							: { type : "string"},
					NombreFondoReal 						: { type : "string"},
					IdSerieFondo							: { type : "string"},
					NombreSerieFondo						: { type : "string"},
					CalculoPorMontoCuotaUI					: { type : "code"},
					NumeroCuotas			       			: { type : "number"},
					ValorCuota 	            				: { type : "number"},
					Monto            						: { type : "number"},
					MontoRetencion							: { type : "number"},
					MontoComision							: { type : "number"},
					DocumentoIdentidad 						: { type : "string"},
					NombreCompleto 		        			: { type : "string"},
					DescripcionPortafolio 	    			: { type : "string"},
					CodigoPortafolio						: { type : "string"},
					FechaCertificacion 						: { type : "date"},
					FechaLiquidacion 	        			: { type : "date"},
					FechaAceptacion							: { type : "date"},
					FechaAnulacion							: { type : "date"},
					FechaCertificacionRegistro				: { type : "sysdatetime"},
					FechaLiquidacionRegistro				: { type : "sysdatetime"},
					FechaAceptacionRegistro					: { type : "sysdatetime"},
					FechaAnulacionRegistro					: { type : "sysdatetime"},
					FechaValorCuota		   	  		 		: { type : "sysdate"},
					NombreEmpleado 			    			: { type : "string"},
					IdEmisor 								: { type : "string"},
					IdEmisorOrigen							: { type : "string"},
					IdEmpresa 		       					: { type : "number"},
					IdMoneda								: { type : "string"},
					TextoPreferenciaPago        			: { type : "string"},
					Glosa									: { type : "string"},
					DecimalesValorCuota						: { type : "number"},
					DecimalesCuota							: { type : "number"},
					EsOvernightUI							: { type : "code"},
					IdSolicitudAporteRescateRelacionado 	: { type : "number"},
					DescripcionPortafolioDestino			: { type : "string"},
					IdPortafolioDestino						: { type : "number"},
					IdClienteDestino						: { type : "number"},
					DocumentoIdentidadDestino				: { type : "string"},
					NombreCompletoDestino					: { type : "string"},
					EsIndicadorMLTUI						: { type : "code"},
					IndicadorMLT							: { type : "string"},
					MontoComision							: { type : "number"},
					IdTipoMovimientoFondoMutuoRelacionado	: { type : "string"},
					DescripcionTipoMovimientoRelacionado	: { type : "string"},
					IdMonedaFondo							: { type : "string"},
					EsDiferido								: { type : "string"},
					EsDiferidoUI						    : { type : "code"},
					CostoMonedaLocal						: { type : "number"},
					ParidadFechaInversion					: { type : "number"},
					MontoMovimientoAsociado					: { type : "number"},
					NumeroCuotasMovimientoAsociado			: { type : "number"},
					Spread									: { type : "number"},
					IdSerieFondoDestino						: { type : "string"},
					IdEmisorDestino							: { type : "string"},
					DescripcionTipoSolicitud				: { type : "string"},
					IdFondo									: { type : "string"},
					MontoMonedaSistema						: { type : "number"},
					Switch									: { type : "string"},
					SwitchUI								: { type : "code"},
					IdTipoCanalIngreso						: { type : "string"},
					FolioContrato							: { type : "number"},
					Producto								: { type : "string"},
					FolioCartaLiquidacion					: { type : "number"},
					IdIngresoEgreso							: { type : "number"},
					NombreEstadoTesoreria					: { type : "string"},
					FechaLiquidacionTesoreria				: { type : "string"},
					IdOperacionSpot							: { type : "number"},
					NombreEstadoSpot						: { type : "string"},
					IdTipoOperacionSpot						: { type : "string"},
					NombreTipoOperacionSpot					: { type : "string"},
					IdOmnibus								: { type : "number"},
					IdDividendo								: { type : "number"},
					IdAgrupador								: { type : "number"},
					DescripcionRegimenTributario            : { type : "string"},
					CostoUF									: { type : "number"},
					RentabilidadReal						: { type : "number"},
					DescripcionMotivoRetiro                 : { type : "string"},
					FolioContratoOperacion					: { type : "string"},
					TipoContratoOperacion					: { type : "string"},
					MontoMonedaSistemaInicial				: { type : "number"},
					Modificaciones							: { type : "string"},
					MontoInversion                          : { type : "number"},
					FolioCarteraAsociadoComision			: { type : "number"},
					RegimenTributario						: { type : "string"},
					
					Emisor       							: { type : "string"},
					FechaValorCuotaFondo           			: { type : "date"},
					NumeroCuotasInversion		     		: { type : "number"},			
					Paridad       							: { type : "number"}
						
					
				}
			}
		};
	}
	


	
	obj.getSchemaSolicitudAporteRescateDetalle = function(){
		return {
			model : {
				fields : {
					IdSolicitudAporte						: { type : "number"},
					IdSerieFondo							: { type : "string"},
					IdRegimenFondoMutuo						: { type : "string"},
					NombreSerieFondo						: { type : "string"},
					MontoAporte								: { type : "number"},
					Porcentaje								: { type : "number"},
					IdEmisor								: { type : "string"},
					Switch									: { type : "string"},
					
					
				
					SwitchUI								: { type : "code"},
					FechaCertificacionAporte 				: { type : "date"},
					FechaLiquidacionAporte 					: { type : "date"},
					FechaValorCuotaAporte 					: { type : "date"}
					
				}
			}
		};
	}
	
	obj.getSchemaConfiguracionRegimenFondoMutuo = function(){
		return {
			model : {
				fields : {
					IdRegimenFondoMutuoDestino				: { type : "string"},
					DescripcionRegimenFondoMutuoDestino		: { type : "string"}
				}
			}
		};
	}
	
	obj.getSchemaConfiguracionPropositoSerieRelacionado = function(){
		return {
			model : {
				fields : {
					IdPropositoSerieRelacionadoDestino			: { type : "string"},
					DescripcionPropositoSerieRelacionadoDestino : { type : "string"}
				}
			}
		};
	}

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
	obj.getSchemaSolicitudAporteRescate_FiltroPerfiles = function(){
		return {
			model : {
				fields : {
					IdSolicitudAporteRescate				: { type : "number"},
					IdCliente								: { type : "number"},
					IdPortafolio							: {	type : "number"},
					FechaEmision 							: { type : "sysdate"},
					FechaIngresoRegistro 					: { type : "sysdatetime"},
					IdTipoMovimientoFondoMutuo 				: { type : "string"},
					TipoSolicitud							: { type : "string"},
					DescripcionTipoMovimiento				: { type : "string"},
					EstadoSolicitud 						: { type : "string"},
					EstadoLiquidacion						: { type : "string"},
					EsIndicadorMLTUI						: { type : "code"},
					NombreEstado 							: { type : "string"},
					NombreEstadoLiquidacion					: { type : "string"},
					NombreFondo 							: { type : "string"},
					NombreFondoReal 						: { type : "string"},
					IdSerieFondo							: { type : "string"},
					NombreSerieFondo						: { type : "string"},
					CalculoPorMontoCuotaUI					: { type : "code"},
					NumeroCuotas 		        			: { type : "number"},
					ValorCuota 	            				: { type : "number"},
					Monto 									: { type : "number"},
					DocumentoIdentidad 						: { type : "string"},
					NombreCompleto 		        			: { type : "string"},
					DescripcionPortafolio 	    			: { type : "string"},
					CodigoPortafolio						: { type : "string"},
					FechaCertificacion 						: { type : "sysdate"},
					FechaLiquidacion 	        			: { type : "sysdate"},
					FechaAceptacion							: { type : "sysdate"},
					FechaAnulacion							: { type : "sysdate"},
					FechaCertificacionRegistro				: { type : "sysdatetime"},
					FechaLiquidacionRegistro				: { type : "sysdatetime"},
					FechaAceptacionRegistro					: { type : "sysdatetime"},
					FechaAnulacionRegistro					: { type : "sysdatetime"},
					FechaValorCuota		   	  		 		: { type : "sysdate"},
					NombreEmpleado 			    			: { type : "string"},
					IdEmisor 								: { type : "string"},
					IdEmisorOrigen							: { type : "string"},
					IdEmpresa 		       					: { type : "number"},
					IdMoneda								: { type : "string"},
					TextoPreferenciaPago        			: { type : "string"},
					Glosa									: { type : "string"},
					DecimalesValorCuota						: { type : "number"},
					DecimalesCuota							: { type : "number"},
					EsOvernightUI							: { type : "code"},
					IdSolicitudAporteRescateRelacionado 	: { type : "number"},
					DescripcionPortafolioDestino			: { type : "string"},
					IdPortafolioDestino						: { type : "number"},
					IdClienteDestino						: { type : "number"},
					DocumentoIdentidadDestino				: { type : "string"},
					NombreCompletoDestino					: { type : "string"},
					EsIndicadorMLTUI						: { type : "code"},
					IndicadorMLT							: { type : "string"},
					MontoComision							: { type : "number"},
					IdTipoMovimientoFondoMutuoRelacionado	: { type : "string"},
					DescripcionTipoMovimientoRelacionado	: { type : "string"},
					IdMonedaFondo							: { type : "string"},
					EsDiferido								: { type : "string"},
					EsDiferidoUI						    : { type : "code"},
					CostoMonedaLocal						: { type : "number"},
					ParidadFechaInversion					: { type : "number"},
					MontoMovimientoAsociado					: { type : "number"},
					NumeroCuotasMovimientoAsociado			: { type : "number"},                      
					Spread									: { type : "number"},
					IdSerieFondoDestino						: { type : "string"},
					IdEmisorDestino							: { type : "string"},
					DescripcionTipoSolicitud				: { type : "string"},
					IdFondo									: { type : "string"},
					MontoMonedaSistema						: { type : "number"},
					Switch									: { type : "string"},
					SwitchUI								: { type : "code"},
					IdTipoCanalIngreso						: { type : "string"},
					FolioContrato							: { type : "number"},
					Producto								: { type : "string"},
					FolioCartaLiquidacion					: { type : "number"},
					IdIngresoEgreso							: { type : "number"},
					NombreEstadoTesoreria					: { type : "string"},
					FechaLiquidacionTesoreria				: { type : "string"},
					IdOperacionSpot							: { type : "number"},
					NombreEstadoSpot						: { type : "string"},
					IdTipoOperacionSpot						: { type : "string"},
					NombreTipoOperacionSpot					: { type : "string"},
					IdOmnibus								: { type : "number"},
					IdDividendo								: { type : "number"}
				}
			}
		};
	}
//Fin GCMBIPAPAS-1
	
	obj.getSchemaConsultarParametroFondoMutuo = function(){
		return {
			model : {
				fields : {
					
					 IdEmpresa								: { type : "number"},
					 NombreTipoParametro					: { type : "string"},
					 ValorParametro							: { type : "string"},
					 NombreParametro						: { type : "string"},
					 IdEstado								: { type : "string"},
					 NombreEstado							: { type : "string"}
				}
			}
		};
	}
	
	obj.getSchemaConsultarTraspaso = function(){
		return {
			model : {
				fields : {
				   IdTraspaso							: { type : "number"},
				   IdEmpresa							: { type : "number"},
				   IdModulo								: { type : "string"},
				   IdUsuario							: { type : "string"},
				   IdCliente							: { type : "number"},
				   IdPortafolio							: { type : "number"},
				   IdEmisor								: { type : "string"},
				   IdMotivoTraspaso						: { type : "string"},
				   IdTipoMovimientoTraspaso				: { type : "string"},
				   IdTipoTraspaso						: { type : "string"},
				   Glosa								: { type : "string"},
				   FechaHoraSuscripcion					: { type : "sysdatetime"},
				   IdDivisionPais						: { type : "number"},
				   FolioTraspaso						: { type : "number"},
				   IdEstado								: { type : "string"},
				   Monto								: { type : "number"},
				   FechaIngreso							: { type : "date"},
				   FechaAceptacion						: { type : "date"},
				   FechaAnulacion						: { type : "date"},
				   FechaModificacion					: { type : "date"},
				   FechaIngresoRegistro					: { type : "date"},
				   FechaAceptacionRegistro				: { type : "date"}, 
				   FechaAnulacionRegistro				: { type : "date"}, 
				   FechaModificacionRegistro			: { type : "date"},
				   CodigoPortafolio						: { type : "string"},
				   RazonSocial							: { type : "string"},
				   NombreCompleto						: { type : "string"},
				   DocumentoIdentidad					: { type : "string"},
				   IdPersona							: { type : "number"},
				   EstadoTraspaso						: { type : "string"},
				   NombreCortoEmisor					: { type : "string"},
				   DocumentoIdentidadEmisor				: { type : "string"},
				   MotivoTraspaso						: { type : "string"},
				   TipoMovimientoTraspaso				: { type : "string"},
				   TipoTraspaso							: { type : "string"},
				   CodigoUsuario						: { type : "string"}
						}
				}
		};
	}
	
	obj.getSchemaConsultarTraspasoDetalleOrigen = function(){
		return {
			model : {
				fields : {
					IdDetalleTraspaso					: { type : "number"},
					IdTraspaso							: { type : "number"},
					IdSerieFondo						: { type : "string"},
					Cantidad							: { type : "number"},
					IdEstado							: { type : "string"},
					FechaIngreso						: { type : "date"},
					FechaModificacion					: { type : "date"},
					FechaIngresoRegistro				: { type : "date"},
					FechaModificacionRegistro			: { type : "date"},
					Estado								: { type : "string"},
					NombreSerieFondo					: { type : "string"},
					CodigoSerie							: { type : "string"}
						}
				}
		};
	}
	
	
	obj.getSchemaConsultarTraspasoDetalleDestino = function(){
		return {
			model : {
				fields : {
					IdDetalleTraspaso					: { type : "number"},
					IdTraspaso							: { type : "number"},
					IdSerieFondo						: { type : "string"},
					Porcentaje							: { type : "number"},
					IdEstado							: { type : "string"},
					FechaIngreso						: { type : "date"},
					FechaModificacion					: { type : "date"},
					FechaIngresoRegistro				: { type : "date"},
					FechaModificacionRegistro			: { type : "date"},
					Estado								: { type : "string"},
					NombreSerieFondo					: { type : "string"},
					CodigoSerie							: { type : "string"},
					NombreCortoEmisor					: { type : "string"},
					DocumentoIdentidadEmisor			: { type : "string"},
					RunFFMM								: { type : "string"},
					NombreFFMM							: { type : "string"}	
	
						}
				}
		};
	}
	
	 
	
	obj.getSchemaConsultarFondoAporteRescate = function(){
		return {
			model : {
				fields : {
					IdTipoMovimientoFondoMutuo				: { type : "string"},
					DescripcionTipoMovimiento				: { type : "string"},
					IdSerieFondo				            : { type : "string"},
					FechaCertificacion						: { type : "date"},
					ValorCuota							    : {	type : "number"},
					FechaCierre 							: { type : "date"},
					IdMonedaFondo 					        : { type : "string"},
					CantidadRegistros						: { type : "number"},
					Monto               					: { type : "number"},
					IdEmpresa								: { type : "number"},
					DecimalesValorCuota						: { type : "number"},
					IdEmisor								: { type : "string"},
					EsFondoNacional							: { type : "string"},
				}
			}
		};
	}
	
	obj.getSchemaConsultaInterfazSolicitudAgf = function(){
		return {
			model : {
				fields : {
					IdInterfazAgf							: { type : "number"},
					DocumentoIdentidad						: { type : "string"},
					CodigoPortafolio				        : { type : "string"},
					TipoRegistro							: { type : "string"},
					NombreCompleto							: { type : "string"},
					FechaLiquidacion						: { type : "sysdate"},
					FechaAceptacion							: {	type : "sysdate"},
					FechaIngreso							: { type : "sysdate"},
					FechaIngresoRegistro					: { type : "sysdatetime"},
					TipoSolicitud 							: { type : "string"},
					IdEstadoSolicitud 					    : { type : "string"},
					IdSerieFondo							: { type : "string"},
					IdUsuario               				: { type : "number"},
					NumeroCuotas							: { type : "number"},
					ValorCuota								: { type : "number"},
					Monto									: { type : "number"},
					CalculoPorMontoCuota					: { type : "string"},
					IdMoneda								: { type : "string"},
					PropositoSerie               			: { type : "string"},
					IdSerieFondoDestino						: { type : "string"},
					IdEstadoSerieFondo						: { type : "string"},
					IndicadorMLT							: { type : "string"},
					IdEstado								: { type : "string"},
					NombreEstado							: { type : "string"},
					IdSolicitudAporteRescate				: { type : "string"},
					IdSolicitudAporteRescateRelacionado		: { type : "string"},
					Observacion								: { type : "code"},
					IdEmpresa								: { type : "string"},
				}
			}
		};
	}
	
	obj.getSchemaConsultaRegimen = function(){
		return {
			model : {
				fields : {
					IdRegimenFondoMutuo			:	{ type : "string"},
					Descripcion					:	{ type : "string"},
					GrupoRegimen				:	{ type : "string"},
					PermiteOtrosGrupos			:	{ type : "string"},
					ValidaPortafolioCliente		: 	{ type : "string"},
					ValidaPortafolioAGF			:	{ type : "string"},
					CodigoPortafolio			:	{ type : "string"},
					PropositoSerieRelacionado	:	{ type : "string"},
					PermiteRescate				:	{ type : "string"},
					PermiteAporte				:	{ type : "string"},
					PermiteTraspaso				:	{ type : "string"},
					ValidaMontoMinimoInstrumento:	{ type : "string"},
					TipoCarteraNormativa		:	{ type : "string"},
					PermiteCambioGrupo			:	{ type : "string"},
					RescateConMandato			:	{ type : "string"},
					AceptaMandatoEnReinversion	:	{ type : "string"},
					PermiteRetencion			:	{ type : "string"},
					PorcentajeRetencion			:   { type : "number"},
					IdEstado					:	{ type : "string"},
					HomologacionNCG365			:	{ type : "string"},
					
				}
			}
		}
	}
	
	obj.getSchemaConsultaRegimenSerieFondo = function(){
		return {
			model : {
				fields : {
					IdRegimenFondoMutuo			:	{ type : "string"},
					IdSerieFondo				:	{ type : "string"},
					Descripcion					:	{ type : "string"},
					IdEstadoRegimenSerieDeFondo	:	{ type : "string"},
					IdEstadoRegimenFondoMutuo	: 	{ type : "string"},
					IdEmpresa					:	{ type : "number"},
				}
			}
		}
	}
	
	obj.getSchemaPerfilCategoriaFondoMutuo = function(){
		return {
			model : {
				fields : {
					IdCategoriaPerfilFondoMutuo		:	{ type : "string"},
					IdCategoriaInversionista		:	{ type : "string"},
					NombreCategoriaPerfilFondoMutuo	:	{ type : "string"},
					IdEmpresa						:	{ type : "number"},
					Jerarquia						:	{ type : "number"},
				}
			}
		}
	}
	
	obj.getDataSourceFondo = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Nemotecnico", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=356",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaFondo()			
		}
		})
	};
	
	obj.getDataSourceCartaLiquidacion = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "FolioCartaLiquidacion", dir: "asc" }, //campo y criterio de ordenamiento
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3116",
	                
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaCartaLiquidacion()			
		}
		})
	};
	
	obj.getDataSourceCargaOperaciones = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdRegistroCarga", dir: "desc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3046",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaCargaOperaciones()			
			}
		})
	};
	
	obj.getDataSourceResumenCargaOperaciones = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdCarga", dir: "desc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3047",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaResumenCargaOperaciones()			
			}
		})
	};
	
	obj.getDataSourceConfiguracionRegimenFondoMutuo = function (filtrosConfiguracionRegimenFondoMutuo) {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				filter : filtrosConfiguracionRegimenFondoMutuo,
				sort: { field: "IdRegimenFondoMutuoOrigen", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601109",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaConfiguracionRegimenFondoMutuo()			
			}
		})
	};
	
	obj.getDataSourceConfiguracionPropositoPortafolio = function (filtrosConfiguracionPropositoPortafolio) {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				filter : filtrosConfiguracionPropositoPortafolio,
				sort: { field: "IdRegimenFondoMutuo", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601110",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaConfiguracionPropositoPortafolio()			
			}
		})
	};
	
	obj.getDataSourceFormasPago = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true,
				sort: { field: "MedioDePago", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1266",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaFormasPago()			
			}
		})
	};
	
	obj.getDataSourceFondosCustodia = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Nemotecnico", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3023",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaFondosCustodia()			
		}
		})
	};
	
	obj.getDataSourcePerfilCategoriaFondoMutuo = function(){
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Jerarquia", dir: "desc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3026",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaPerfilCategoriaFondoMutuo()			
			}
		})
	}
	
	obj.getSchemaFormasPago = function() {
		return { 
			model: {
				fields : {
					IdPreferenciaPagoPortafolio	: { type : "number"},
					IdMoneda 	  	  		    : { type : "string"},
					NumeroCuentaBancaria	    : { type : "string"},
					MedioDePago   	  		    : { type : "string"},
					IdPortafolio	            : { type : "number"},
					IdTipoOperacion   	  		: { type : "string"},
				}
			}
		}
	};
	
	obj.getSchemaCartaLiquidacion= function() {
		return { 
			model: {
				fields : {
					MontoAporte	            : { type : "number"},
					MontoRescate 	  	    : { type : "number"},
					FolioCartaLiquidacion	: { type : "number"},
					IdSerieFondo   	  		: { type : "string"},
					Emisor	                : { type : "string"},
					IdEmisor				: { type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaHorarioFondo = function(){
		return { 
			model: {
				fields : {
					UsaHorarioAporteDefault	            : { type : "string"},
					UsaHorarioRescateDefault 	  	    : { type : "string"},
					UsaHorarioReinversionDefault		: { type : "string"},
					UsaHorarioLiquidezDefault   	  	: { type : "string"},
					UsaHorarioAporteDefaultUI	        : { type : "code"},
					UsaHorarioRescateDefaultUI 	  	    : { type : "code"},
					UsaHorarioReinversionDefaultUI		: { type : "code"},
					UsaHorarioLiquidezDefaultUI   	  	: { type : "code"},
					HoraCierreAporteComercial	        : { type : "time"},
					HoraCierreRescateComercial 	  	    : { type : "time"},
					HoraCierreReinversionComercial		: { type : "time"},
					HoraCierreLiquidezComercial   	  	: { type : "time"},
					HoraCierreAporteBackoffice	        : { type : "time"},
					HoraCierreRescateBackoffice 	  	: { type : "time"},
					HoraCierreReinversionBackoffice		: { type : "time"},
					HoraCierreLiquidezBackoffice   	  	: { type : "time"},
				}
			}
		}
	}
	
	obj.getDataSourceSolicitudAporteRescate = function(){
		return new bcs.newDataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdSolicitudAporteRescate", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=396",
						dataType: "json"
					}
				},
				schema : obj.getSchemaSolicitudAporteRescate(),
				aggregate: [
		            { field: "NumeroCuotas", aggregate: "sum" },
		            { field: "MontoMonedaSistema", aggregate: "sum"}
		        ]
			},
			
		});
	}
	
	obj.getDataSourceSolicitudAporteRescateDetalle = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true,
				pageSize : 100,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600076",
						dataType: "json"
					}
				},
				schema : obj.getSchemaSolicitudAporteRescateDetalle(),
			}
		});
	}

//GCMBIPAPAS-1 Filtro de clientes por Perfiles
	obj.getDataSourceSolicitudAporteRescate_FiltroPerfiles = function(){
		return new bcs.DataSource({ 
			type : "call",
			definition : {
				callTransaction	: "13060",
				serverFiltering : false, 
				serverPaging : false, 
				serverSorting : false,
				callResultSet   : "P_OUT_GESTION_OPERACION",
				sort: { field: "IdSolicitudAporteRescate", dir: "desc" },
				pageSize : 200,
				schema : obj.getSchemaSolicitudAporteRescate_FiltroPerfiles(),
				//schema : obj.getSchemaSolicitudAporteRescate(),
				aggregate: [
		            { field: "NumeroCuotas", aggregate: "sum" },
		            { field: "MontoMonedaSistema", aggregate: "sum"}
		        ]
			},
			
		});
	}
	
//Fin GCMBIPAPAS-1
	
	
	obj.getDataSourceConsultarParametroFondoMutuo = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "NombreTipoParametro", dir: "asc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3088",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultarParametroFondoMutuo()

			},
			
		});
	}
		
	obj.getDataSourceConsultarTraspaso = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdTraspaso", dir: "asc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300014",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultarTraspaso()
			},			
		});
	}
		
		
	obj.getDataSourceConsultarTraspasoDetalleOrigen = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdDetalleTraspaso", dir: "asc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300015",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultarTraspasoDetalleOrigen()
			},			
		});
	}
	
	obj.getDataSourceConsultarTraspasoDetalleDestino = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdDetalleTraspaso", dir: "asc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300016",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultarTraspasoDetalleDestino()
			},			
		});
	}
		
	
	obj.getDataSourceConsultaCustodiaFondosMutuos = function(){
		return new bcs.DataSource({ 
			type : "call",
	        definition : {
				pageSize		: 100,
	            callTransaction : "3041",
	            callResultSet   : "P_OUT_TBL",
	            schema : {
	                model : {
	                   fields : {                                                                                                                                                             
		        			IdClasificacionTipoInstrumento			: { type: "string"},
		        			NombreClasificacionTipoInstrumento		: { type: "string"},
		        			IdTipoInstrumento						: { type: "string"},
		        			NombreTipoInstrumento					: { type: "string"},
		        			IdInstrumento							: { type: "number"},
		        			Nemotecnico								: { type: "string"},
		        			IdCliente								: { type: "number"},
		        			DocumentoIdentidad						: { type: "string"},
		        			RazonSocial								: { type: "string"},
		        			CodigoPortafolio						: { type: "string"},
		        			IdPortafolio							: { type: "number"},
		        			Relacion								: { type: "string"},
		        			IdBoveda								: { type: "string"},
		        			CantidadCustodia						: { type: "number"},
		        			Monto									: { type: "number"},
		        			IdMoneda								: { type: "string"},
		        			ValorCuota								: { type: "number"},
		        			DecimalesValorCuota						: { type: "number"},
		        			CantidadGarantia						: { type: "number"},
		        			CantidadTransIng						: { type: "number"},
		        			CantidadTransEgr						: { type: "number"},
		        			IdSocioDeNegocio						: { type: "number"},
		        			CantidadTransSolicitudesAporte			: { type: "number"},
		        			CantidadTransSolicitudesRescate			: { type: "number"},
		        			INGMontoPrecioPromedioMonedaInstrumento	: { type: "number"},
		        			INGPrecioPromedioCompra					: { type: "number"},
		        			Producto								: { type: "string"},
		        			MontoMonedaSistema						: { type: "number"},
		        			IdMonedaFondo							: { type: "string"},
		        			IdMonedaPrincipal						: { type: "string"},
		        			IdRegimenFondoMutuo						: { type: "string"},
		        			RegimenTributario						: { type: "string"},
		        			RegimenTributarioGarantia				: { type: "string"},
							NombreSerieFondo 						: { type: "string"},
	                   }
	                }
	            }
	        }
		});
	}
	
	obj.getDataSourceConsultaComisionesNN = function(){
		return new bcs.DataSource({ 
			type : "call",
	        definition : {
				pageSize		: 100,
	            callTransaction : "700387",
	            callResultSet   : "P_OUT_TBL",
	            schema : {
	                model : {
	                   fields : {                                                                                                                                                    
		        			DocumentoIdentidad			: { type: "string"},
		        			NombreCompleto				: { type: "string"},
		        			CodigoPortafolio			: { type: "string"},
		        			IdSerieFondo				: { type: "string"},
		        			FolioCartera				: { type: "number"},
		        			FechaHasta					: { type: "date"},
		        			FechaCompra					: { type: "date"},
		        			DiasPermanencia				: { type: "number"},
		        			ComisionAnual				: { type: "number"},
		        			ComisionDiaria				: { type: "number"},
		        			CuotasCliente				: { type: "number"},
		        			ComisionNN					: { type: "number"},
		        			CuotasFinalesCliente		: { type: "number"},
		        			IdCartera					: { type: "number"},
		        			IdSecuenciaRescateNN		: { type: "string"}
	                   }
	                }
	            }
	        }
		});
	}
	
	obj.getDataSourceConfirmacionComisionesNN = function(){
		return new bcs.DataSource({ 
			type : "call",
	        definition : {
				pageSize		: 100,
	            callTransaction : "700387",
	            callResultSet   : "P_OUT_TBL",
	            schema : {
	                model : {
	                   fields : {                                                                                                                                                    
		        			DocumentoIdentidad			: { type: "string"},
		        			NombreCliente				: { type: "string"},
		        			CodigoPortafolio			: { type: "string"},
		        			IdSerieFondo				: { type: "string"},
		        			FolioCartera				: { type: "number"},
		        			IdSolicitudAporteRescate	: { type: "number"},
		        			NumeroCuotas				: { type: "number"},
		        			MontoMonedaSistema			: { type: "number"}

	                   }
	                }
	            }
	        }
		});
	}
	
	obj.getDataSourceConsultarFlujoCustodia = function(){
		return new bcs.DataSource({ 
			type : "call",
	        definition : {
				pageSize		: 100,
	            callTransaction : "3143",
	            callResultSet   : "P_OUT_FLUJOFONDOMUTUOTOTALIZADO",
	            schema : {
	                model : {
	                   fields : {                                                                                                                                                             
		        			IdInstrumento					   : { type: "number"},
		        			IdCliente						   : { type: "number"},
		        			IdPortafolio					   : { type: "number"},
		        			Nemotecnico						   : { type: "string"},
		        			//DocumentoIdentidad				   : { type: "string"},
		        			//CodigoPortafolio				   : { type: "string"},
		        			CantidadDisponible				   : { type: "number"},
		        			MontoDisponible					   : { type: "number"},
		        			MontoDisponibleCustodia			   : { type: "number"},
		        			RescateTotal					   : { type: "string"}		        					        			
	                   }
	                }
	            }
	        }
		});
	}
	
	obj.getDataSourceConsultaFondoAporteRescate = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdSerieFondo", dir: "asc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1483",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultarFondoAporteRescate()
			},			
		});
	}
	
	obj.getDataSourceConsultaInterfazSolicitudAgf = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdInterfazAgf", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3122",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultaInterfazSolicitudAgf()
			},			
		});
	}

//GCMBIPAPAS-1 Filtro de clientes por Perfiles - ConsultaSolicitudAgf
	
	obj.getDataSourceConsultaInterfazSolicitudAgf_FiltroPerfiles = function(){
		return new bcs.DataSource({ 
			type : "call",
			definition : {
				callTransaction	: "13061",
				//serverFiltering : false, 
				//serverPaging : false, 
				//serverSorting : false,
				callResultSet   : "P_OUT_GESTION_INTERFAZ",
				sort: { field: "IdInterfazAgf", dir: "desc" },
				pageSize : 200,
				schema : obj.getSchemaConsultaInterfazSolicitudAgf()
			},			
		});
	}	
	
	
//Fin GCMBIPAPAS-1


	obj.getDataSourcePerfilesFondoMutuo = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdKeyPerfil", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1395",
						dataType: "json"
					}
				},
				schema : { 
					model: {
						fields : {
							NombrePerfilConfiguracion	: { type : "string"},
							IdPerfil 	  	  		    : { type : "string"},	
							Nombre 	  	  		    	: { type : "string"},	
						}
					}
				}
			},			
		});
	}
	
	obj.getDataSourceFondosAdministracionLiquidez = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdMoneda", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3144",
						dataType: "json"
					}
				},
				schema : { 
					model: {
						fields : {
							IdMoneda									: { type : "string"},
							IdSerieFondo 	  	  						: { type : "string"},
							MontoMinimo									: { type : "number"},
							PortafoliosPersonalizados 					: { type : "code"},
							IdEmpresa									: { type : "number"},
							HoraCierreLiquidezComercial					: { type : "string"}
						}
					}
				}
			},			
		});
	}
	
	obj.getDataSourceFondosPortafolioAdministracionLiquidez = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "FechaIngresoRegistro", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3145",
						dataType: "json"
					}
				},
				schema : { 
					model: {
						fields : {
							IdConfiguracionPortafolioAdministracionLiquidez	: { type : "string"},
							IdCliente 	  	  								: { type : "number"},
							IdPortafolio									: { type : "number"},
							FechaIngreso 									: { type : "sysdate"},
							FechaIngresoRegistro							: { type : "datetime"},
							FechaModificacion								: { type : "sysdate"},
							FechaModificacionRegistro						: { type : "datetime"},
							IdMoneda										: { type : "string"},
							IdSerieFondo									: { type : "string"},
							MontoMinimo										: { type : "number"},
							CodigoPortafolio								: { type : "string"},
							DescripcionPortafolio							: { type : "string"},
							PropositoPortafolio								: { type : "string"},
							RazonSocial										: { type : "string"},
							NombreCompleto									: { type : "string"},
							DocumentoIdentidad								: { type : "string"},
							IdPersona										: { type : "number"},
							EsConfiguracionPersonalizada                    : { type : "code"}
						}
					}
				}
			},			
		});
	}
	
	obj.getDataSourceConsultaLogAdministracionLiquidez = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "FechaIngresoRegistro", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3153",
						dataType: "json"
					}
				},
				schema : { 
					model: {
						fields : {
							IdCliente 	  	  								: { type : "number"},
							IdPortafolio									: { type : "number"},
							FechaIngreso 									: { type : "sysdate"},
							FechaIngresoRegistro							: { type : "datetime"},
							IdMoneda										: { type : "string"},
							IdSerieFondo									: { type : "string"},
							MontoMinimo										: { type : "number"},
							CodigoPortafolio								: { type : "string"},
							DescripcionPortafolio							: { type : "string"},
							PropositoPortafolio								: { type : "string"},
							RazonSocial										: { type : "string"},
							NombreCompleto									: { type : "string"},
							DocumentoIdentidad								: { type : "string"},
							IdPersona										: { type : "number"},
							Codigo											: { type : "code"},
							Saldo											: { type : "number"}
						}
					}
				}
			},	
		});
	}
	
	obj.getDataSourceHorarioFondo = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "Nemotecnico", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3196",
						dataType: "json"
					}
				},
				schema : obj.getSchemaHorarioFondo()
			},		
		});
	}
	
	obj.getDataSourceConsultaRegimen = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdRegimenFondoMutuo", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=371",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultaRegimen()
			},		
		});
	}
	
	obj.getDataSourceConsultaRegimenSerieFondo = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdRegimenFondoMutuo", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3203",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultaRegimenSerieFondo()
			},		
		});
	}
	
	obj.getSchemaConsultaOmnibusDetalle = function(){
		return { 
			model: {
				fields : {
					IdOmnibus	            			: { type : "number"},
					FechaOmnibus 	  	    			: { type : "date"},
					FechaIngresoRegistro				: { type : "datetime"},
					IdEstadoArchivo   	  				: { type : "string"},
					IdOmnibusDetalle	        		: { type : "number"},
					IdSerieFondo 	  	    			: { type : "string"},
					CantidadOperaciones					: { type : "number"},
					ValorCuota   	  					: { type : "number"},
					UltimoValorCuota	        		: { type : "number"},
					Monto 	  	    					: { type : "number"},
					CantidadCuotas						: { type : "number"},
					ValorCuotaAGF   	  				: { type : "number"},
					MontoAGF	        				: { type : "number"},
					CantidadCuotasAGF 	  				: { type : "number"},
					DiferenciaValorCuota				: { type : "number"},
					DiferenciaValorCuotaAGF   	  		: { type : "number"},
					IdEstado 	  	    				: { type : "string"},
					DecimalesCuota						: { type : "number"},
					DecimalesValorCuota   	  			: { type : "number"},
					NombreEstado   	  					: { type : "string"},
					IdMoneda   	  						: { type : "string"},
					IdEmisor							: { type : "string"},
				}
			}
		}
	}
	
	obj.getDataSourceConsultaOmnibusDetalle = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdOmnibusDetalle", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600014",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConsultaOmnibusDetalle()
			},		
		});
	}
	
	var configuracionColumnas = function() {
		return {
			IdTipoCanalIngreso				:{field: "IdTipoCanalIngreso",				title: $translate.instant('UI_CANAL'),			    					width: "75px",	align: "left"},
			Modificaciones					:{field: "Modificaciones",					title: $translate.instant('UI_MODIFICACION_ESTADO'), 					width: "100px", align: "left"},	
			FolioContrato					:{field: "FolioContrato",					title: $translate.instant('UI_FOLIO_CONTRATO'),							width: "100px",	align: "left"},
			IdSolicitudAporteRescate	  	:{field: "IdSolicitudAporteRescate",		title: $translate.instant('UI_SECUENCIA'),								width: "100px",	align: "left"},
			FechaEmision				  	:{field: "FechaEmision",					title: $translate.instant('UI_FECHA_EMISION'),							width: "100px", align: "left"},
			FechaIngresoRegistro		  	:{field: "FechaIngresoRegistro",			title: $translate.instant('UI_FECHA_REGISTRO'),							width: "155px", align: "left"},
			DescripcionTipoMovimiento	  	:{field: "DescripcionTipoMovimiento",		title: $translate.instant('UI_OPERACION'),								width: "100px", align: "left"},
			NombreEstado				  	:{field: "NombreEstado",					title: $translate.instant('UI_ESTADO'),									width: "75px", 	align: "left"},
			IdSerieFondo				  	:{field: "IdSerieFondo",					title: $translate.instant('UI_INSTRUMENTO'),							width: "100px", align: "left"},
			NombreSerieFondo			  	:{field: "NombreSerieFondo",				title: $translate.instant('UI_SERIE'),									width: "150px", align: "left"},
			NombreFondo				  	  	:{field: "NombreFondo",						title: $translate.instant('UI_FONDO_MUTUO'),							width: "75px",	align: "left"},
			NombreFondoReal				  	:{field: "NombreFondoReal",					title: $translate.instant('UI_NOMBRE_FONDO'),							width: "75px",	align: "left"},
			NumeroCuotas				  	:{field: "NumeroCuotas",		    		title: $translate.instant('UI_CANTIDAD'),		    					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(NumeroCuotas, DecimalesCuota) #</label>"},
			ValorCuota					  	:{field: "ValorCuota",						title: $translate.instant('UI_VALOR_CUOTA'),							width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(ValorCuota, DecimalesValorCuota) #</label>"},
			Monto					      	:{field: "Monto",							title: $translate.instant('UI_MONTO'),									width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			MontoMinimo					  	:{field: "MontoMinimo",						title: $translate.instant('UI_MONTO_MINIMO'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoMinimo,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			MontoMaximo					  	:{field: "MontoMaximo",						title: $translate.instant('UI_MONTO_MAXIMO'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoMaximo,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			MontoMonedaOrigen			  	:{field: "Monto",						    title: $translate.instant('UI_MONTO'),						        	width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			IdCategoriaPerfilFondoMutuo	  	:{field: "IdCategoriaPerfilFondoMutuo",		title: $translate.instant('UI_PERFIL'),			    					width: "125px",	align: "left"},
			NombreCategoriaPerfilFondoMutuo	:{field: "NombreCategoriaPerfilFondoMutuo",	title: $translate.instant('UI_PERFIL'),			    					width: "125px",	align: "left"},
			NombreProposito	  			  	:{field: "NombreProposito",					title: $translate.instant('UI_PROPOSITO'),			    				width: "125px",	align: "left"},
			IdMoneda					   	:{field: "IdMoneda",				    	title: $translate.instant('UI_MONEDA'),			    					width: "75px",	align: "left"},
			Moneda					      	:{field: "IdMoneda",				    	title: $translate.instant('UI_MONEDA'),			    					width: "75px",	align: "left"},
			DocumentoIdentidad			  	:{field: "DocumentoIdentidad",        		title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),					width: "100px",	align: "left"},
			NombreCompleto				  	:{field: "NombreCompleto",					title: $translate.instant('UI_CLIENTE'),								width: "150px", 	align: "left"},
			DescripcionPortafolio		  	:{field: "CodigoPortafolio",				title: $translate.instant('UI_PORTAFOLIO'),								width: "100px", align: "left"},
			FechaAceptacion		      	  	:{field: "FechaAceptacion",	    			title: $translate.instant('UI_FECHA_CURSE'),							width: "125px",	align: "left"},
			FechaCertificacion    		  	:{field: "FechaCertificacion",	    		title: $translate.instant('UI_FECHA_CERTIFICACION'),					width: "125px", align: "left"},
			FechaAceptacionAnterior      	:{field: "FechaAceptacionAnterior",	    	title: $translate.instant('UI_FECHA_CURSE_ANTERIOR'),					width: "125px",	align: "left"},
			FechaCertificacionAnterior    	:{field: "FechaCertificacionAnterior",	    title: $translate.instant('UI_FECHA_CERTIFICACION_ANTERIOR'),			width: "125px", align: "left"},
			UsuarioAutorizador			    :{field: "UsuarioAutorizador",				title: $translate.instant('UI_USUARIO_AUTORIZADOR'),					width: "75px", 	align: "left"},
			UsuarioModificador			   	:{field: "UsuarioModificador",				title: $translate.instant('UI_USUARIO_MODIFICADOR'),					width: "75px", 	align: "left"},
			NombreEmpleado			      	:{field: "NombreEmpleado",					title: $translate.instant('UI_AGENTE'),									width: "75px", 	align: "left"},
			NombreEmpleado				  	:{field: "NombreEmpleado",					title: $translate.instant('UI_DIGITADOR'),								width: "75px", 	align: "left"},
			FechaAnulacion			      	:{field: "FechaAnulacion",		    		title: $translate.instant('UI_FECHA_ANULACION'),						width: "125px",	align: "left"},
			TextoPreferenciaPago		  	:{field: "TextoPreferenciaPago",			title: $translate.instant('UI_FORMA_DE_PAGO'),							width: "100px", align: "left"},
			Glosa						  	:{field: "Glosa",					    	title: $translate.instant('UI_GLOSA'),									width: "50px", 	align: "left"},
			NombreTipoFondo				  	:{field: "NombreCorto",						title: $translate.instant('UI_TIPO_FONDO'),								width: "100px", align: "left"},
			MotivoRetiro:                   {field: "DescripcionMotivoRetiro",          title: $translate.instant('UI_MOTIVO_RETIRO'),                          width: "100px", align: "left"},
			RazonSocialInstrumento		  	:{field: "NombreFondo",						title: $translate.instant('UI_RAZON_SOCIAL'),							width: "150px", align: "left"},
			Familia						  	:{field: "NombreFamilia",					title: $translate.instant('UI_FAMILIA'),								width: "100px", align: "left"},
			TipoInstrumento				  	:{field: "NombreTipoInstrumento",			title: $translate.instant('UI_TIPO_INSTRUMENTO'),						width: "100px", align: "left"},
			Emisor						  	:{field: "IdEmisor",						title: $translate.instant('UI_EMISOR'),									width: "100px", align: "left"},
			EmisorValorCuota				:{field: "EmisorValorCuota",				title: $translate.instant('UI_EMISOR_VALOR_CUOTA'),						width: "100px", align: "left"},
			ValorDolarAporte			  	:{field: "ValorDolarAporte",				title: $translate.instant('UI_VALOR_USD_APORTE'),						width: "100px", align: "left",	template : " <label> #:  (util.bcs_formato(ValorDolarAporte,'FMM','Monto','USD', '','')).valor #</label>"},
			ValorDolarRescate			  	:{field: "ValorDolarRescate",				title: $translate.instant('UI_VALOR_USD_RESCATE'),						width: "100px", align: "left",	template : " <label> #:  (util.bcs_formato(ValorDolarRescate,'FMM','Monto','USD', '','')).valor #</label>"},
			FechaVigencia				  	:{field: "FechaVigencia",					title: $translate.instant('UI_FECHA_VIGENCIA'),							width: "100px", align: "left"},
			FechaCalculo				  	:{field: "FechaCalculo",					title: $translate.instant('UI_FECHA_CARGA'),							width: "100px", align: "left"},
			Observacion					  	:{field: "Observacion",						title: $translate.instant('UI_OBSERVACION'),							width: "100px", align: "left"},
			FondoNacional				  	:{field: "EsFondoNacionalUI",				title: $translate.instant('UI_FONDO_NACIONAL'),							width: "50px",	align: "left"},
			EstadoAporte				  	:{field: "EstadoAporteUI",					title: $translate.instant('UI_ESTADO_APORTE'),							width: "100px", align: "left"},
			EstadoRescate				  	:{field: "EstadoRescateUI",					title: $translate.instant('UI_ESTADO_RESCATE'),							width: "100px", align: "left"},
			EstadoReinversion			  	:{field: "EstadoReinversionUI",				title: $translate.instant('UI_ESTADO_REINVERSION'),						width: "100px", align: "left"},
			BloqueoAporte				  	:{field: "BloqueoAporteUI",					title: $translate.instant('UI_BLOQUEO_APORTE'),							width: "100px", align: "left"},
			BloqueoRescate				  	:{field: "BloqueoRescateUI",				title: $translate.instant('UI_BLOQUEO_RESCATE'),						width: "100px", align: "left"},			
			IdTramo      				  	:{field: "IdTramo",			        		title: $translate.instant('UI_SECUENCIA'),								width: "100px", align: "left"},
			DiasDesde			          	:{field: "DiasDesde",		            	title: $translate.instant('UI_PERMANENCIA_MINIMA'),						width: "100px", align: "left", editable:true},
			DiasHasta			          	:{field: "DiasHasta",			        	title: $translate.instant('UI_PERMANENCIA_MAXIMA'),						width: "100px", align: "left", editable:true},
			PorcentajeComision			  	:{field: "PorcentajeComision",				title: $translate.instant('UI_PORCENTAJE_COBRO'),						width: "100px", align: "left", editable:true},
			IdMonedaFondo				  	:{field: "IdMonedaFondo",					title: $translate.instant('UI_MONEDA_INSTRUMENTO'), 					width: "75px", 	align: "left"},
			SaldoMonedaOrigen			  	:{field: "SaldoMonedaOrigen",				title: $translate.instant('UI_SALDO_MONEDA_ORIGEN'),					width: "100px", align: "left"},
			CostoMonedaLocal			  	:{field: "CostoMonedaLocal", 				title: $translate.instant('UI_MONTO_MONEDA_ORIGEN'), 					width: "150px", align: "left" , template : " <label> #:  (util.bcs_formato(CostoMonedaLocal,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			MontoDiferido				  	:{field: "Monto",							title: $translate.instant('UI_MONTO_MONEDA_FONDO'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			Paridad						  	:{field: "ParidadFechaInversion",			title: $translate.instant('UI_TIPO_CAMBIO'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(ParidadFechaInversion,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			ParidadValorCuota				:{field: "ParidadValorCuota",				title: $translate.instant('UI_PARIDAD_VALOR_CUOTA'),					width: "75px",  align: "left"},
			ValorCuotaCalculado				:{field: "ValorCuotaCalculado",				title: $translate.instant('UI_VALOR_CUOTA_CALCULADO'),					width: "100px", align: "left",},
			VariacionValorCuota			 	:{field: "VariacionValorCuota",				title: $translate.instant('UI_VARIACION_VALOR_CUOTA'),					width: "75px", 	align: "left",},
			CalculoMontoCuota			  	:{field: "CalculoPorMontoCuotaUI",			title: $translate.instant('UI_TIPO'),									width: "75px", 	align: "left"},
			MontoAporte					  	:{field: "MontoAporte",			    		title: $translate.instant('UI_MONTO_APORTE'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoAporte,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			MontoRescate                  	:{field: "MontoRescate",					title: $translate.instant('UI_MONTO_RESCATE'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoRescate,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			FechaOperacion				  	:{field: "FechaOperacion",					title: $translate.instant('UI_FECHA_OPERACION'),						width: "100px", align: "left"},
			FechaCierre  				  	:{field: "FechaCierre",   					title: $translate.instant('UI_FECHA_CIERRE'),			    			width: "100px", align: "left"},
			Estado  				      	:{field: "Estado",   			        	title: $translate.instant('UI_ESTADO'),		 	            			width: "75px", 	align: "left"},
			DescEstado  				  	:{field: "DescEstado",   			    	title: $translate.instant('UI_DESCRIPCION_ESTADO'),						width: "200px", align: "left"},
			TipoSolicitud  				  	:{field: "TipoSolicitud",   				title: $translate.instant('UI_TIPO_SOLICITUD'),		 	    			width: "150px", align: "left"},
			IdCliente  				      	:{field: "IdCliente",   			    	title: $translate.instant('UI_ID_CLIENTE'),		 	        			width: "150px", align: "left"},
			RazonSocial  				  	:{field: "RazonSocial",   					title: $translate.instant('UI_RAZON_SOCIAL'),		 	    			width: "150px", align: "left"},
			NombreCompleto  			  	:{field: "NombreCompleto",   				title: $translate.instant('UI_NOMBRE_COMPLETO'),		 				width: "150px", align: "left"},
			TipoMovimiento  			  	:{field: "TipoMovimiento",   				title: $translate.instant('UI_TIPO_MOVIMIENTO'),		 				width: "150px", align: "left"},
			CodigoUsuario  				  	:{field: "CodigoUsuario",   				title: $translate.instant('UI_CODIGO_USUARIO'),		 	    			width: "150px", align: "left"},
			MontoOriginal  				  	:{field: "MontoOriginal",   				title: $translate.instant('UI_MONTO_ORIGINAL'),		 	    			width: "150px", align: "left"},
			TipoCambio  				  	:{field: "TipoCambio",   			    	title: $translate.instant('UI_TIPO_CAMBIO'),		 	    			width: "150px", align: "left"},
			IdMonedaOriginal  			  	:{field: "IdMonedaOriginal",   				title: $translate.instant('UI_ID_MONEDA_ORIGINAL'),		 				width: "150px", align: "left"},
			RazonSocialEmisor  			  	:{field: "RazonSocialEmisor",   			title: $translate.instant('UI_RAZON_SOCIAL_EMISOR'),					width: "150px", align: "left"},
			MontoOperacion			      	:{field: "Monto",         					title: $translate.instant('UI_MONTO'),									width: "100px", align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			FechaSistema         		  	:{field: "FechaSistema",	            	title: $translate.instant('UI_FECHA_SISTEMA'),		        			width: "125px", align: "left"},
			Codigo           			  	:{field: "Codigo",   		            	title: $translate.instant('UI_DESCRIPCION_ESTADO'),		 				width: "150px", align: "left"},
			Overnight					  	:{field: "EsOvernightUI",					title: $translate.instant('UI_OVERNIGHT'),								width: "75px",  align: "left"},
			MLT							  	:{field: "EsIndicadorMLTUI",				title: $translate.instant('UI_INDICADORMLT'),							width: "75px",  align: "left"},
			FechaLiquidacion			  	:{field: "FechaLiquidacion",				title: $translate.instant('UI_FECHA_LIQUIDACION'),						width: "125px", align: "left"},
			FechaLiquidacionAnterior		:{field: "FechaLiquidacionAnterior",		title: $translate.instant('UI_FECHA_LIQUIDACION_ANTERIOR'),				width: "125px", align: "left"},
			Diferido					  	:{field: "EsDiferidoUI",					title: $translate.instant('UI_DIFERIDO'),								width: "75px",  align: "left"},
			Switch					  		:{field: "SwitchUI",						title: $translate.instant('UI_SWITCH'),									width: "75px",  align: "left"},			
			FechaRegistroAceptacion	  	  	:{field: "FechaAceptacionRegistro",   		title: $translate.instant('UI_FECHA_CURSE_REGISTRO'),  					width: "125px", align: "left"},
			FechaRegistroCertificacion	  	:{field: "FechaCertificacionRegistro",		title: $translate.instant('UI_FECHA_CERTIFICACION_REGISTRO'), 			width: "125px", align: "left"},
			FechaRegistroAnulacion		  	:{field: "FechaAnulacionRegistro",			title: $translate.instant('UI_FECHA_ANULACION_REGISTRO'),	  			width: "125px", align: "left"},
			FechaRegistroLiquidacion	  	:{field: "FechaLiquidacionRegistro",  		title: $translate.instant('UI_FECHA_LIQUIDACION_REGISTRO'),  			width: "125px", align: "left"},
			MontoRetencion				  	:{field: "MontoRetencion",					title: $translate.instant('UI_MONTO_RETENCION'),						width:"125px", 	align:"left" , template : " <label> #:  (util.bcs_formato(MontoRetencion,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			MontoComision				  	:{field: "MontoComision",					title: $translate.instant('UI_MONTO_COMISION'),							width:"125px", 	align:"left" , template : " <label> #:  (util.bcs_formato(MontoComision,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			IdFamiliaFondo				  	:{field: "IdFamiliaFondo",					title: $translate.instant('UI_ID_FAMILIA_FONDO'),						width: "125px", align: "left"},
			NombreFamilia				  	:{field: "NombreFamilia",					title: $translate.instant('UI_NOMBRE_FAMILIA'),							width: "125px", align: "left"},
			Simulacion					  	:{field: "EsSimulacionUI",					title: $translate.instant('UI_SIMULACION'),								width: "75px", 	align:"left"},
			EstadoAporteComercial		  	:{field: "EstadoAporteComercialUI",			title: $translate.instant('UI_ESTADO_APORTE_COMERCIAL'),				width: "100px", align: "left"},
			EstadoRescateComercial		  	:{field: "EstadoRescateComercialUI",		title: $translate.instant('UI_ESTADO_RESCATE_COMERCIAL'),				width: "100px", align: "left"},
			EstadoAporteBackOffice		  	:{field: "EstadoAporteBackOfficeUI",		title: $translate.instant('UI_ESTADO_APORTE_BACKOFFICE'),				width: "100px", align: "left"},
			EstadoRescateBackOffice		  	:{field: "EstadoRescateBackOfficeUI",		title: $translate.instant('UI_ESTADO_RESCATE_BACKOFFICE'),				width: "100px", align: "left"},
			CodigoPortafolio     		  	:{field: "CodigoPortafolio",		    	title: $translate.instant('UI_PORTAFOLIO'),								width: "100px", align: "left"},
			Cantidad     				  	:{field: "Cantidad",		       			title: $translate.instant('UI_CANTIDAD'),								width: "100px", align: "left"},
			CantidadTransEgr              	:{field: "CantidadTransEgr",              	title: $translate.instant('UI_EGRESOS_TRANSITO'),           			width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransEgr,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			CantidadTransIng              	:{field: "CantidadTransIng",              	title: $translate.instant('UI_INGRESO_TRANSITO'),           			width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransIng,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			CantidadGarantia              	:{field: "CantidadGarantia",              	title: $translate.instant('UI_GARANTIAS'),                      		width: "100px", align: "left", template : " <label> #:  util.bcs_formato(CantidadGarantia,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			CantidadCustodia              	:{field: "CantidadCustodia",              	title: $translate.instant('UI_CUSTODIA'),                       		width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadCustodia,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			IdBoveda                      	:{field: "IdBoveda",                      	title: $translate.instant('UI_BOVEDA'),                         		width: "60px", 	align: "left"},
			Nemotecnico                   	:{field: "Nemotecnico",                   	title: $translate.instant('UI_INSTRUMENTO'),                    		width: "100px", align: "left"},
			IdTipoInstrumento             	:{field: "IdTipoInstrumento",             	title: $translate.instant('UI_TIPO_INSTRUMENTO_ABR'),           		width: "60px", 	align: "left"},
			IdClasificacionTipoInstrumento	:{field: "IdClasificacionTipoInstrumento",	title: $translate.instant('UI_CLASIFICACION_ABR'),              		width: "60px", 	align: "left"},
			CodigoConfirmacionCertificacion	:{field: "DescripcionCodigo",   		    title: $translate.instant('UI_DESCRIPCION_ESTADO'),		 				width: "150px", align: "left",},
			CantidadTransSolicitudesAporte	:{field: "CantidadTransSolicitudesAporte",	title: $translate.instant('UI_APORTES_TRANSITO'),						width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesAporte,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			CantidadTransSolicitudesRescate	:{field: "CantidadTransSolicitudesRescate",	title: $translate.instant('UI_RESCATES_TRANSITO'),						width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesRescate,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			RutEmisor	 					:{field: "RutEmisor",   		    		title: $translate.instant('UI_RUT_EMISOR'),		 			    		width: "150px", align: "left"},
			PortafolioEmisor	 			:{field: "PortafolioEmisor",   		    	title: $translate.instant('UI_PORTAFOLIO_EMISOR'),		 				width: "150px", align: "left"},
			FechaMovimiento 	 			:{field: "FechaMovimiento",   		    	title: $translate.instant('UI_FECHA_MOVIMIENTO'),		 				width: "150px", align: "left"},
			FolioMovimiento 	 			:{field: "FolioMovimiento",   		    	title: $translate.instant('UI_FOLIO_MOVIMIENTO'),		 				width: "150px", align: "left"},
			Modalidad 	 					:{field: "Modalidad",   		    		title: $translate.instant('UI_MODALIDAD'),		 						width: "150px", align: "left"},
			IdSerieFondoDestino			  	:{field: "IdSerieFondoDestino",				title: $translate.instant('UI_INSTRUMENTO_DESTINO'),					width: "100px", align: "left"},
			EstadoCarga			  			:{field: "EstadoCarga",						title: $translate.instant('UI_ESTADO_CARGA'),							width: "100px", align: "left"},
			ObservacionCarga			  	:{field: "ObservacionCarga",				title: $translate.instant('UI_OBSERVACION_CARGA'),						width: "100px", align: "left"},
			EstadoProceso			  		:{field: "EstadoProceso",					title: $translate.instant('UI_ESTADO_PROCESO'),							width: "100px", align: "left"},
			DescripcionPortafolioCliente	:{field: "DescripcionPortafolioCliente",	title: $translate.instant('UI_PORTAFOLIO'),								width: "100px", align: "left"},
			NombreEstadoProceso				:{field: "NombreEstadoProceso",				title: $translate.instant('UI_ESTADO_PROCESO'),							width: "100px", align: "left"},
			CodigoPortafolioEmisor			:{field: "CodigoPortafolioEmisor",			title: $translate.instant('UI_CODIGO_PORTAFOLIO_EMISOR'),				width: "100px", align: "left"},
			NombreTipoMovimiento			:{field: "NombreTipoMovimiento",			title: $translate.instant('UI_TIPO_MOVIMIENTO'),						width: "100px", align: "left"},
			RegistrosProcesado				:{field: "RegistrosProcesado",				title: $translate.instant('UI_REGISTROS_CARGA_PROCESADO'),				width: "100px", align: "left"},
			RegistrosError					:{field: "RegistrosError",					title: $translate.instant('UI_REGISTROS_CARGA_ERROR'),					width: "100px", align: "left"},
			RegistrosProcesoPendiente		:{field: "RegistrosProcesoPendiente",		title: $translate.instant('UI_REGISTROS_PROCESO_PENDIENTE'),			width: "100px", align: "left"},
			RegistrosProcesoError			:{field: "RegistrosProcesoError",			title: $translate.instant('UI_REGISTROS_PROCESO_ERROR'),				width: "100px", align: "left"},
			RegistrosProcesoProcesado		:{field: "RegistrosProcesoProcesado",		title: $translate.instant('UI_REGISTROS_PROCESO_PROCESADO'),			width: "100px", align: "left"},
			IdRegistroCarga					:{field: "IdRegistroCarga",					title: $translate.instant('UI_ID_REGISTRO'),							width: "75px",  align: "left"},
			IdCarga							:{field: "IdCarga",							title: $translate.instant('UI_ID_CARGA'),								width: "75px",  align: "left"},
			NombreCompletoCliente			:{field: "NombreCompletoCliente",			title: $translate.instant('UI_CLIENTE'),								width: "150px", align: "left"},
			FechaCarga						:{field: "FechaCarga",						title: $translate.instant('UI_FECHA_CARGA'), 							width: "100px", align: "left"},
			Concepto						:{field: "Concepto",						title: $translate.instant('UI_CONCEPTO'),								width: "75px",  align: "left"},
			CantidadRegistros				:{field: "CantidadRegistros",				title: $translate.instant('UI_CANTIDAD_REGISTROS'),						width: "100px", align: "left"},
			TotalMonto						:{field: "Monto",							title: $translate.instant('UI_TOTAL_MONTO'),							width: "100px", align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			FechaValorCuota					:{field: "FechaCierre",						title: $translate.instant('UI_FECHA_VALOR_CUOTA'),						width: "100px", align: "left"},
			FechaValorCuota					:{field: "FechaValorCuota",					title: $translate.instant('UI_FECHA_VALOR_CUOTA'),						width: "100px", align: "left"},
			FechaValorCuotaAnterior			:{field: "FechaValorCuotaAnterior",			title: $translate.instant('UI_FECHA_VALOR_CUOTA_ANTERIOR'),				width: "100px", align: "left"},
			IdSerieFondoDestino				:{field: "IdSerieFondoDestino",				title: $translate.instant('UI_SERIE_FONDO_DESTINO'),					width: "100px", align: "left"},
			IdEmisorDestino					:{field: "IdEmisorDestino",					title: $translate.instant('UI_EMISOR_DESTINO'),							width: "100px", align: "left"},
			FolioCartaLiquidacion           :{field: "FolioCartaLiquidacion",		    title: $translate.instant('UI_FOLIO_CARTA_LIQUIDACION'),				width: "100px", align: "left"},
			EstadoLiquidacion				:{field: "NombreEstadoLiquidacion",			title: $translate.instant('UI_ESTADO_LIQUIDACION'),						width: "100px", align: "left"},
			IdInterfazAgf					:{field: "IdInterfazAgf",					title: $translate.instant('UI_ID_INTERFAZ'),							width: "100px", align: "left"},
			TipoRegistro					:{field: "TipoRegistro",					title: $translate.instant('UI_TIPO_REGISTRO'),							width: "100px", align: "left"},
			FechaIngreso					:{field: "FechaIngreso",					title: $translate.instant('UI_FECHA_INGRESO'),							width: "100px", align: "left"},
			MoneyMarket						:{field: "EsMoneyMarketUI",					title: $translate.instant('UI_MONEY_MARKET'),							width: "75px",  align: "left"},
			Spread							:{field: "Spread",							title: $translate.instant('UI_SPREAD'),									width: "100px", align: "left"},
			PortafoliosPersonalizados		:{field: "PortafoliosPersonalizados",		title: $translate.instant('UI_PORTAFOLIOS_PERSONALIZADOS'),				width: "100px", align: "left"},
			Saldo			  				:{field: "Saldo", 							title: $translate.instant('UI_SALDO'), 									width: "150px", align: "left" , template : " <label> #:  (util.bcs_formato(Saldo,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			EstadoCargaDatos                :{field: "NombreEstado",					title: $translate.instant('UI_ESTADO_CARGA_DATOS'),						width: "100px",	align: "left"},
			HoraCierreLiquidezComercial     :{field: "HoraCierreLiquidezComercial",		title: $translate.instant('UI_CIERRE_PLATAFORMA_COMERCIAL'),			width: "100px",	align: "left"},
			UltimoFechaHoraCierreAporte		:{field: "UltimoFechaHoraCierreAporte",		title: $translate.instant('UI_ULTIMO_FECHA_HORA_CIERRE_APORTE'),		width: "150px", align: "left"},
			UltimoFechaHoraCierreRescate	:{field: "UltimoFechaHoraCierreRescate",	title: $translate.instant('UI_ULTIMO_FECHA_HORA_CIERRE_RESCATE'),		width: "150px", align: "left"},
			UltimoFechaHoraCierreReinversion:{field: "UltimoFechaHoraCierreReinversion",title: $translate.instant('UI_ULTIMO_FECHA_HORA_CIERRE_REINVERSION'),	width: "150px", align: "left"},
			EsConfiguracionPersonalizada  	:{field: "EsConfiguracionPersonalizada",	title: $translate.instant('UI_ES_CONFIGURACION_PERSONALIZADA'),         width: "200px", align: "left"},
			IdFondo							:{field: "IdFondo",							title: $translate.instant('UI_FONDO'),									width: "200px", align: "left"},
			IdRegimenFondoMutuo				:{field: "IdRegimenFondoMutuo",				title: $translate.instant('UI_REGIMEN'),								width: "200px", align: "left"},
			DescripcionRegimen				:{field: "Descripcion",						title: $translate.instant('UI_DESCRIPCION'),							width: "200px", align: "left"},
			GrupoRegimen					:{field: "GrupoRegimen",					title: $translate.instant('UI_GRUPO_REGIMEN'),							width: "100px", align: "left"},
			PermiteOtrosGrupos				:{field: "PermiteOtrosGrupos",				title: $translate.instant('UI_PERMITE_OTROS_GRUPO'),					width: "75px",  align: "left"},
			ValidaPortafolioCliente			:{field: "ValidaPortafolioCliente",			title: $translate.instant('UI_VALIDA_PORTAFOLIO_CLIENTE'),				width: "75px",  align: "left"},
			ValidaPortafolioAGF				:{field: "ValidaPortafolioAGF",				title: $translate.instant('UI_VALIDA_PORTAFOLIO_AGF'),					width: "75px",  align: "left"},
			PropositoSerieRelacionado		:{field: "PropositoSerieRelacionado",		title: $translate.instant('UI_PROPOSITO'),								width: "150px", align: "left"},
			PermiteRescate					:{field: "PermiteRescate",					title: $translate.instant('UI_PERMITE_RESCATE'),						width: "75px",  align: "left"},
			PermiteTraspaso					:{field: "PermiteTraspaso",					title: $translate.instant('UI_PERMITE_TRASPASO'),						width: "75px",  align: "left"},
			RescateConMandato				:{field: "RescateConMandato",				title: $translate.instant('UI_PERMITE_MANDATO_LIQUIDACION'),			width: "75px",  align: "left"},
			AceptaMandatoEnReinversion		:{field: "AceptaMandatoEnReinversion",		title: $translate.instant('UI_PERMITE_MANDATO_LIQUIDACION_CAMBIO_GRUPO'),width: "75px",  align: "left"},
			EstadoRegimen					:{field: "IdEstado",						title: $translate.instant('UI_ESTADO'),									width: "75px",  align: "left"},
			PermiteCambioGrupo				:{field: "PermiteCambioGrupo",				title: $translate.instant('UI_PERMITE_CAMBIO_GRUPO'),					width: "75px",  align: "left"},
			FechaFondo						:{field: "FechaFondo",						title: $translate.instant('UI_FECHA'),									width: "100px", align: "left"},
			INGPrecioPromedioCompra			:{field: "INGPrecioPromedioCompra",			title: $translate.instant('UI_PRECIO_PROMEDIO'),						width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(INGPrecioPromedioCompra,'FMM','Precio',IdMonedaPrincipal, '','')).valor #</label>"},
			INGMontoPrecioPromedioMonedaInstrumento	:{field: "INGMontoPrecioPromedioMonedaInstrumento",	title: $translate.instant('UI_MONTO_PROMEDIO'),			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(INGMontoPrecioPromedioMonedaInstrumento,'FMM','Monto',IdMonedaPrincipal, '','')).valor #</label>"},
			DescripcionTipoSolicitud		:{field: "DescripcionTipoSolicitud",		title: $translate.instant('UI_TIPO_SOLICITUD'),							width: "100px", align: "left"},
			FechaValorCuotaFondo			:{field: "FechaValorCuota",					title: $translate.instant('UI_FECHA_VALOR_CUOTA'),						width: "100px", align: "left"},
			DescEstadoProceso				:{field: "DescEstadoProceso",				title: $translate.instant('UI_RESULTADO_PROCESO'),						width: "100px", align: "left"},
			MontoMonedaSistema				:{field: "MontoMonedaSistema",				title: $translate.instant('UI_MONTO_MONEDA_SISTEMA'),					width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoMonedaSistema,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #</label>"},			
			CantidadDisponible				:{field: "CantidadDisponible",				title: $translate.instant('UI_CANTIDAD_DISPONIBLE'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadDisponible,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			MontoDisponible					:{field: "MontoDisponible",			    	title: $translate.instant('UI_MONTO_DISPONIBLE'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoDisponible,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},
			MontoDisponibleCustodia			:{field: "MontoDisponibleCustodia",			title: $translate.instant('UI_MONTO_DISPONIBLE_CUSTODIA'),				width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoDisponibleCustodia,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},			
			MontoDividendo					:{field: "Monto",							title: $translate.instant('UI_MONTO_DIVIDENDO'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			CantidadCuotasDividendo			:{field: "CantidadCuotas",					title: $translate.instant('UI_CUOTAS_DIVIDENDO'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(CantidadCuotas,'FMM','Cantidad',IdMoneda, '','')).valor #</label>"},
			MontoPagadoAGF					:{field: "MontoPagadoAGF",					title: $translate.instant('UI_MONTO_PAGADO_AGF'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoPagadoAGF,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},
			CuotasAGF						:{field: "CuotasAGF",						title: $translate.instant('UI_CUOTAS_AGF'),								width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(CuotasAGF,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},
			CustodiaCongelada				:{field: "CustodiaCongelada",				title: $translate.instant('UI_CUSTODIA_CONGELADA'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(CustodiaCongelada,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},			
			RunFondo						:{field: "RunFondo",						title: $translate.instant('UI_RUN_FONDO'),								width: "75px", 	align: "left"},
			ISIN							:{field: "ISIN",							title: $translate.instant('UI_ISIN'),									width: "75px", 	align: "left"},
			IdCuadraturaCustodia			:{field: "IdCuadraturaCustodia",			title: $translate.instant('UI_SECUENCIA_CUADRATURA'),				    width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(IdCuadraturaCustodia,'FMM','Cantidad',IdMonedaFondo, '','')).valor #</label>"},
			TotalCuotasAGF					:{field: "TotalCuotasAGF",					title: $translate.instant('UI_TOTAL_CUOTAS_AGF'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(TotalCuotasAGF,'FMM','Cantidad',IdMonedaFondo, '','')).valor #</label>"},
			TotalCuotas						:{field: "TotalCuotas",						title: $translate.instant('UI_TOTAL_CUOTAS'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(TotalCuotas,'FMM','Cantidad',IdMonedaFondo, '','')).valor #</label>"},
			DiferenciaCuotas				:{field: "DiferenciaCuotas",				title: $translate.instant('UI_DIFERENCIA_CUOTAS'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(DiferenciaCuotas,'FMM','Cantidad',IdMonedaFondo, '','')).valor #</label>"},
			DiferenciaValorizacion			:{field: "DiferenciaValorizacion",			title: $translate.instant('UI_DIFERENCIA_VALORIZACION_CUOTAS'),			width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(DiferenciaValorizacion,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			ValorizacionCuotasAGF			:{field: "ValorizacionCuotasAGF",			title: $translate.instant('UI_VALORIZACION_SALDO_AGF'),					width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(ValorizacionCuotasAGF,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			ValorizacionCuotas				:{field: "ValorizacionCuotas",				title: $translate.instant('UI_VALORIZACION_SALDO_OPTIMUS'),				width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(ValorizacionCuotas,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
			IndicadorCuadratura				:{field: "IndicadorCuadratura",				title: $translate.instant('UI_INDICADOR_CUADRATURA'),					width: "75px", 	align: "left" },
			Producto						:{field: "Producto",						title: $translate.instant('UI_PRODUCTO'),								width: "75px", 	align: "left" },
			Nemo							:{field: "Nemo",							title: $translate.instant('UI_INSTRUMENTO'),							width: "75px", 	align: "left"},
			NumeroDividendo					:{field: "NumeroDividendo",					title: $translate.instant('UI_NUMERO_DIVIDENDO'),						width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(NumeroDividendo,'FMM','Cantidad',IdMoneda, '','')).valor #</label>"},
			IdEstado						:{field: "IdEstado",						title: $translate.instant('UI_ESTADO'),									width: "75px", 	align: "left" },
			IdRegimen						:{field: "IdRegimen",						title: $translate.instant('UI_REGIMEN_CUADRATURA_AGF'),					width: "75px", 	align: "left"},
			IdOmnibus						:{field: "IdOmnibus",						title: $translate.instant('UI_FOLIO_OMNIBUS'),							width: "75px", 	align: "left"},
			CantidadCuotas				  	:{field: "CantidadCuotas",		    		title: $translate.instant('UI_CANTIDAD'),		    					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(CantidadCuotas, DecimalesCuota) #</label>"},
			ValorCuotaArchivo				:{field: "ValorCuota",						title: $translate.instant('UI_VALOR_CUOTA_ARCHIVO'),					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(ValorCuota, DecimalesValorCuota) #</label>"},
			IndicadorCuadratura				:{field: "IndicadorCuadratura",				title: $translate.instant('UI_INDICADOR_CUADRATURA'),					width: "75px", 	align: "left"},
			UltimoValorCuota				:{field: "UltimoValorCuota",				title: $translate.instant('UI_ULTIMO_VALOR_CUOTA'),						width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(UltimoValorCuota, DecimalesValorCuota) #</label>"},
			MontoOmnibus					:{field: "Monto",							title: $translate.instant('UI_MONTO'),									width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			MontoAGF						:{field: "MontoAGF",						title: $translate.instant('UI_MONTO_AGF'),								width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoAGF,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
			ValorCuotaAGF					:{field: "ValorCuotaAGF",					title: $translate.instant('UI_VALOR_CUOTA_AGF'),						width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(ValorCuotaAGF, DecimalesValorCuota) #</label>"},
			CantidadCuotasAGF				:{field: "CantidadCuotasAGF",		    	title: $translate.instant('UI_CANTIDAD_AGF'),		    				width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(CantidadCuotasAGF, DecimalesCuota) #</label>"},
			DiferenciaValorCuota			:{field: "DiferenciaValorCuota",			title: $translate.instant('UI_DIREFENCIA_VALOR_CUOTA'),					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(DiferenciaValorCuota, DecimalesValorCuota) #</label>"},
			DiferenciaValorCuotaAGF			:{field: "DiferenciaValorCuotaAGF",			title: $translate.instant('UI_DIREFENCIA_VALOR_CUOTA_AGF'),				width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(DiferenciaValorCuotaAGF, DecimalesValorCuota) #</label>"},						
			//SPOT
			IdIngresoEgreso					:{field: "IdIngresoEgreso",					title: $translate.instant('UI_SECUENCIA_TESORERIA'),					width: "100px", align: "left"},
			NombreEstadoTesoreria			:{field: "NombreEstadoTesoreria",			title: $translate.instant('UI_ESTADO_TESORERIA_SPOT'),						width: "125px", 	align: "left"},
			FechaLiquidacionTesoreria		:{field: "FechaLiquidacionTesoreria",		title: $translate.instant('UI_FECHA_LIQUIDACION_TESORERIA'),			width: "100px", align: "left"},
			IdOperacionSpot					:{field: "IdOperacionSpot",					title: $translate.instant('UI_FOLIO_SPOT'),								width: "100px", align: "left"},
			NombreEstadoSpot				:{field: "NombreEstadoSpot",				title: $translate.instant('UI_ESTADO_SPOT'),							width: "75px", 	align: "left"},
			NombreTipoOperacionSpot			:{field: "NombreTipoOperacionSpot",			title: $translate.instant('UI_TIPO_OPERACION_SPOT'),					width: "75px", 	align: "left"},
			IdDividendo						:{field: "IdDividendo",						title: $translate.instant('UI_DIVIDENDO'),								width: "75px", 	align: "left"},
			//REINVERSION 1 A N
			IdAgrupador						:{field: "IdAgrupador",						title: $translate.instant('UI_FOLIO_AGRUPADOR'),						width: "100px", 	align: "left"},
			Porcentaje						:{field: "Porcentaje",						title: $translate.instant('UI_PORCENTAJE'),								width: "100px", 	align: "left"},
			MontoAporte						:{field: "MontoAporte",						title: $translate.instant('UI_MONTO_TRASPASO'),							width: "100px", align: "right", template : " <label> #: util.bcs_formato(MontoAporte,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
			IdSolicitudAporte				:{field: "IdSolicitudAporte",				title: $translate.instant('UI_SECUENCIA'),								width: "100px",	align: "left"},	
			IdFolioCSC						:{field: "IdFolioCSC",					title: $translate.instant('UI_ID_CONTRATO'),			    			width: "75px",	align: "left"},
			DocumentoIdentidad				:{field: "DocumentoIdentidad",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),					width: "100px",	align: "left"},
			CodigoPortafolio	  			:{field: "CodigoPortafolio",				title: $translate.instant('UI_PORTAFOLIO_CLIENTE'),					width: "100px",	align: "left"},
			NombreCompleto					:{field: "NombreCompleto",				title: $translate.instant('UI_NOMBRE_COMPLETO'),						width: "100px",	align: "left"},
			Fecha							:{field: "Fecha",						title: $translate.instant('UI_FECHA'),			    					width: "75px",	align: "left"},
			MontoPesos						:{field: "MontoPesos",					title: $translate.instant('UI_MONTO_PESOS'),							width: "100px",	align: "left"},
			MontoUF	  						:{field: "MontoUF",						title: $translate.instant('UI_MONTO_UF'),								width: "100px",	align: "left"},
			Nombre	  						:{field: "Nombre",						title: $translate.instant('UI_TIPO_PAGO'),								width: "100px",	align: "left"},
			NombreBanco	  					:{field: "NombreBanco",					title: $translate.instant('UI_NOMBRE_BANCO'),							width: "100px",	align: "left"},
			//IdEmpresa	  					:{field: "IdEmpresa",					title: $translate.instant('UI_ID_EMPRESA'),								width: "100px",	align: "left"},
			Estado	  						:{field: "Estado",						title: $translate.instant('UI_ESTADO'),									width: "100px",	align: "left"},
			FolioCSC	  					:{field: "FolioCSC",					title: $translate.instant('UI_FOLIO_CSC'),								width: "100px",	align: "left"},
			FolioRelacionado	  			:{field: "FolioRelacionado",			title: $translate.instant('UI_FOLIO_RELACIONADO'),						width: "100px",	align: "left"},
			EstadoPAC	  					:{field: "EstadoPAC",					title: $translate.instant('UI_ESTADO_PAC'),								width: "100px",	align: "left"},
			Canal  							:{field: "Canal",					title: $translate.instant('UI_CANAL'),										width: "100px",	align: "left"},	
			RazonSocial						:{field: "RazonSocial",					title: $translate.instant('UI_RAZON_SOCIAL'),			    			width: "75px",	align: "left"},
			Serie							:{field: "Serie",						title: $translate.instant('UI_SERIE'),									width: "100px",	align: "left"},
			PorcentajeCSC					:{field: "Porcentaje",					title: $translate.instant('UI_PORCENTAJE'),			    				width: "75px",	align: "left", editable: true},
			RegimenTributario  				:{field: "DescripcionRegimenTributario", title: $translate.instant('UI_REGIMEN_TRIBUTARIO'),					width: "100px",	align: "left"},
			RegimenTributarioCartera 		:{field: "RegimenTributarioCartera", 	title: $translate.instant('UI_REGIMEN_TRIBUTARIO_CARTERA'),				width: "100px",	align: "left"},
			RegimenTributarioGarantia		:{field: "RegimenTributarioGarantia", 	title: $translate.instant('UI_REGIMEN_TRIBUTARIO_GARANTIA'),			width: "100px",	align: "left"},
			IdRegimenFondoMutuoDestino      :{field: "IdRegimenFondoMutuoDestino", 	title: $translate.instant('UI_ID_REGIMEN_DESTINO'),						width: "100px",	align: "left"},
			RegimenFondoMutuoDestino        :{field: "DescripcionRegimenFondoMutuoDestino", title: $translate.instant('UI_REGIMEN_DESTINO'),				width: "100px",	align: "left"},
			PropositoDestino		        :{field: "DescripcionPropositoSerieRelacionadoDestino", title: $translate.instant('UI_PROPOSITO_DESTINO'),		width: "100px",	align: "left"},
			IdPropositoDestino		        :{field: "IdPropositoPortafolio", 		title: $translate.instant('UI_PROPOSITO'),								width: "100px",	align: "left"},
			PermiteAporte		        	:{field: "PermiteAporte", 				title: $translate.instant('UI_PERMITE_APORTE'),							width: "75px",	align: "left"},
			IdRegimenTributarioOrigen       :{field: "IdRegimenTributarioOrigen", 	title: $translate.instant('UI_REGIMEN_ORIGEN'),							width: "100px",	align: "left"},
			IdRegimenTributarioDestino      :{field: "IdRegimenTributarioDestino", 	title: $translate.instant('UI_REGIMEN_DESTINO'),						width: "100px",	align: "left"},
			FolioAPV						:{field: "FolioAPV", 					title: $translate.instant('UI_FOLIO_APV'),								width: "100px",	align: "left"},
			SumaTotal						:{field: "SumaTotal", 					title: $translate.instant('UI_SUMA_TOTAL'),								width: "100px",	align: "left"},
			MontoDeposito					:{field: "MontoDeposito", 				title: $translate.instant('UI_MONTO_DEPOSITO'),							width: "100px",	align: "left"},
			NombreCompleto					:{field: "NombreCompleto", 				title: $translate.instant('UI_NOMBRE_COMPLETO'),						width: "100px",	align: "left"},
			FolioPAC						:{field: "FolioPAC", 					title: $translate.instant('UI_FOLIO_PAC'),								width: "100px",	align: "left"},
			EstadoCliente					:{field: "EstadoCliente", 				title: $translate.instant('UI_ESTADO_CLIENTE'),							width: "100px",	align: "left"},
			EstadoPortafolio				:{field: "EstadoPortafolio", 			title: $translate.instant('UI_ESTADO_PORTAFOLIO'),						width: "100px",	align: "left"},
			IdentServicio					:{field: "IdentServicio", 				title: $translate.instant('UI_MANDATO_AGRUPADOR'),						width: "200px",	align: "left"},
			DescripcionSolicitud			:{field: "DescripcionSolicitud", 		title: $translate.instant('UI_DESCRIPCION_CODIGO'),						width: "100px",	align: "left"},
			EstadoCargo						:{field: "EstadoCargo", 				title: $translate.instant('UI_ESTADO_CARGO'),							width: "100px",	align: "left"},
			DescripcionEstadoCargo			:{field: "DescripcionEstadoCargo", 		title: $translate.instant('UI_DESCRIPCION_ESTADO_CARGO'),				width: "100px",	align: "left"},
			EstadoRendicion					:{field: "EstadoRendicion", 			title: $translate.instant('UI_ESTADO_RENDICION'),						width: "250px",	align: "left"},			
			CostoUF							:{field: "CostoUF",						title: $translate.instant('UI_COSTO_UF'),								width: "100px", align: "left"},
			RentabilidadReal				:{field: "RentabilidadReal",			title: $translate.instant('UI_RENTABILIDAD_REAL'),						width: "100px", align: "left"},
			EstadoCSC						:{field: "EstadoCSC", 					title: $translate.instant('UI_ESTADO_CSC'),								width: "100px",	align: "left"},
			EstadoSAAPV						:{field: "EstadoSAAPV", 				title: $translate.instant('UI_ESTADO_SAAPV'),							width: "100px",	align: "left"},
			PermiteRetencion				:{field: "PermiteRetencion",			title: $translate.instant('UI_PERMITE_RETENCION'),						width: "100px",	align: "left"},
			PorcentajeRetencion				:{field: "PorcentajeRetencion",			title: $translate.instant('UI_PORCENTAJE_RETENCION'),					width: "100px",	align: "left"},
			DescripcionCanal				:{field: "DescripcionCanal",			title: $translate.instant('UI_DESCRIPCION_CANAL'),						width: "100px",	align: "left"},
			FolioContratoOperacion			:{field: "FolioContratoOperacion",      title: $translate.instant('UI_FOLIO_CONTRATO_OPERACION'),   			width: "100px",	align: "left"},
	        TipoContratoOperacion			:{field: "TipoContratoOperacion",       title: $translate.instant('UI_TIPO_CONTRATO_OPERACION'),   				width: "100px",	align: "left"},
	        RegimenTributario2   	        :{field: "RegimenTributario", 			title: $translate.instant('UI_REGIMEN_TRIBUTARIO'),						width: "100px",	align: "left"},
	        ValidaMontoMinimoInstrumento   	:{field: "ValidaMontoMinimoInstrumento",title: $translate.instant('UI_VALIDAR_MONTO_MINIMO_INSTRUMENTO'),		width: "100px",	align: "left"},
	        IndicadorMLT   					:{field: "IndicadorMLT", 				title: $translate.instant('UI_MLT'),									width: "100px",	align: "left"},
	        IdSolicitudAporteRescateRelacionado:{field: "IdSolicitudAporteRescateRelacionado",	title: $translate.instant('UI_SECUENCIA_RELACIONADO'),		width: "100px",	align: "left"},
			MontoMonedaSistemaInicial		:{field: "MontoMonedaSistemaInicial",	title: $translate.instant('UI_MONTO_MONEDA_SISTEMA_INICIAL'),			width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoMonedaSistemaInicial,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #</label>"},			


	        FechaCertificacionAporte   		:{field: "FechaCertificacionAporte", 	title: $translate.instant('UI_FECHA_CERTIFICACION_APORTE'),	   			width: "100px",	align: "left"},        	  
	        FechaLiquidacionAporte   		:{field: "FechaLiquidacionAporte", 		title: $translate.instant('UI_FECHA_LIQUIDACION_APORTE'),   			width: "100px",	align: "left"},
	        FechaValorCuotaAporte   		:{field: "FechaValorCuotaAporte", 		title: $translate.instant('UI_FECHA_VALOR_CUOTA_APORTE'),   			width: "100px",	align: "left"},

			RunFFMM						  	:{field: "RunFFMM",        				title: $translate.instant('UI_RUN_FFMM'),								width: "100px",	align: "left"},
			RutSociedadAdmFFMM			  	:{field: "RutSociedadAdmFFMM",        	title: $translate.instant('UI_RUT_SOC_ADM_FFMM'),						width: "100px",	align: "left"},
			NombreFFMM   	        		:{field: "NombreFFMM", 					title: $translate.instant('UI_NOMBRE_FFMM'),							width: "100px",	align: "left"},
			SociedadAdmFFMM   	        	:{field: "SociedadAdmFFMM", 			title: $translate.instant('UI_SOCIEDAD_ADM_FFMM'),						width: "100px",	align: "left"},
			IdMonedaPago   	        		:{field: "IdMonedaPago", 				title: $translate.instant('UI_MONEDA_PAGO'),							width: "100px",	align: "left"},
			IdMedioDePago   	        	:{field: "IdMedioDePago", 				title: $translate.instant('UI_MEDIO_DE_PAGO'),							width: "100px",	align: "left"},
			FechaHasta   	        		:{field: "FechaHasta", 					title: $translate.instant('UI_FECHA_HASTA'),                           	width: "100px",	align: "left"},
			FechaCompra   	        		:{field: "FechaCompra", 				title: $translate.instant('UI_FECHA_COMPRA'),                           width: "100px",	align: "left"},
			DiasPermanencia	        		:{field: "DiasPermanencia", 			title: $translate.instant('UI_DIAS_PERMANENCIA'),                       width: "100px",	align: "left"},		
	        ComisionAnual	        		:{field: "ComisionAnual", 			    title: $translate.instant('UI_COMISION_ANUAL'),                         width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(ComisionAnual,'FMM','Tasa','" + $rootScope.SesionIdMonedaSistema + "','FMM','')).valor #</label>"},
	        ComisionDiaria	        		:{field: "ComisionDiaria", 			    title: $translate.instant('UI_COMISION_DIARIA'),                        width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(ComisionDiaria,'FMM','Tasa','" + $rootScope.SesionIdMonedaSistema + "','FMM','')).valor #</label>"},
	        CuotasCliente	        		:{field: "CuotasCliente", 			    title: $translate.instant('UI_CUOTAS_CLIENTE'),                         width: "100px",	align: "left"},
	        ComisionNN	        		    :{field: "ComisionNN", 			        title: $translate.instant('UI_COMISION_NN'),                            width: "100px",	align: "left"},
	        CuotasFinalesCliente	        :{field: "CuotasFinalesCliente", 		title: $translate.instant('UI_CUOTAS_FINALES'),                         width: "100px",	align: "left"},
	        FolioCartera	                :{field: "FolioCartera", 		        title: $translate.instant('UI_FOLIO_CARTERA'),                         	width: "100px",	align: "left"},
	        DocumentoIdentidadEmisor	    :{field: "DocumentoIdentidadEmisor", 	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_EMISOR'),             width: "100px",	align: "left"},
	        FolioCarteraAsociadoComision	:{field: "FolioCarteraAsociadoComision",title: $translate.instant('UI_FOLIO_CARTERA'),                         	width: "100px",	align: "left"},
			IdCartera						:{field: "IdCartera",					title: $translate.instant('UI_ID_CARTERA'),								width: "100px", align: "left"},				   
			NumeroCuotas					:{field: "NumeroCuotas",				title: $translate.instant('UI_CANTIDAD'),							width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(NumeroCuotas,'FMM','Cantidad','CLP', '','')).valor #</label>"},
			MontoMonedaSistema				:{field: "MontoMonedaSistema", 			title: $translate.instant('UI_MONTO_MONEDA_SISTEMA'),					width: "100px", align: "left"},
			NombreCliente					:{field: "NombreCliente",				title: $translate.instant('UI_NOMBRE_CLIENTE'),							width: "100px", align: "left"},
			HomologacionNCG365				:{field: "HomologacionNCG365",			title: $translate.instant('UI_HOMOLOGACIONNCG365'),							width: "100px", align: "left"},
			SecuenciaRescateNN				:{field: "IdSecuenciaRescateNN",		title: $translate.instant('UI_SECUENCIA_RESCATE_NN'),					width: "100px", align: "left"},
			MontoPatrimonioEmisor			:{field: "MontoPatrimonioEmisor",		title: $translate.instant('UI_PATRIMONIO_MINIMO_EMISOR'),				width: "100px", align: "left"},
			RestringeAporte					:{field: "RestringeAporte",				title: $translate.instant('UI_RESTRINGEAPORTE'),						width: "100px", align: "left"},
			RestringeRescate				:{field: "RestringeRescate",			title: $translate.instant('UI_RESTRINGERESCATE'),						width: "100px", align: "left"}
		}   
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	
	obj.getConsultaRegimen = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'REGIMEN'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: obj.getDataSourceConsultaRegimen(),
				
					columns: [
					           obj.getConfiguracionColumna("IdRegimenFondoMutuo",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescripcionRegimen",{ groupable: true})
					          ,obj.getConfiguracionColumna("GrupoRegimen",{ groupable: true})
					          ,obj.getConfiguracionColumna("PermiteOtrosGrupos",{ groupable: true})
					          ,obj.getConfiguracionColumna("ValidaPortafolioCliente",{ groupable: true})
					          ,obj.getConfiguracionColumna("ValidaPortafolioAGF",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
					          ,obj.getConfiguracionColumna("PropositoSerieRelacionado",{ groupable: true})
					          ,obj.getConfiguracionColumna("PermiteRescate",{ groupable: true})
					          ,obj.getConfiguracionColumna("PermiteAporte",{ groupable: true})
					          ,obj.getConfiguracionColumna("PermiteTraspaso",{ groupable: true})
					          ,obj.getConfiguracionColumna("ValidaMontoMinimoInstrumento",{ groupable: true})
					          ,obj.getConfiguracionColumna("PermiteCambioGrupo",{ groupable: true})
					          ,obj.getConfiguracionColumna("RescateConMandato",{ groupable: true})
							  ,obj.getConfiguracionColumna("AceptaMandatoEnReinversion",{ groupable: true})
							  ,obj.getConfiguracionColumna("PermiteRetencion",{ groupable: true})
							  ,obj.getConfiguracionColumna("PorcentajeRetencion",{ groupable: true})
							  ,obj.getConfiguracionColumna("EstadoRegimen",{ groupable: true})
							  ,obj.getConfiguracionColumna("HomologacionNCG365",{ groupable: true})								  
					]
				}
		};
		return config;
	};
	
	//pago agf ffmm
	obj.getConsultaMovimientosFondosMutuos = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUD'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: obj.getDataSourceSolicitudAporteRescate(),
				
					columns: [
						obj.getConfiguracionColumna("DescripcionTipoMovimiento",{ groupable: true})
				          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
				          ,obj.getConfiguracionColumna("IdMonedaFondo",{ groupable: true})
				          ,obj.getConfiguracionColumna("FechaCertificacion",{ groupable: true})
				          ,obj.getConfiguracionColumna("Monto")
				          ,obj.getConfiguracionColumna("FechaAceptacion",{ groupable: true})
				          ,obj.getConfiguracionColumna("IdFondo",{ groupable: true})
				          ,obj.getConfiguracionColumna("FechaValorCuotaFondo",{ groupable: true})
				          ,obj.getConfiguracionColumna("ValorCuota",{ groupable: true})
 						  ,obj.getConfiguracionColumna("Emisor",{ groupable: true})
						  ,obj.getConfiguracionColumna("IdOmnibus",{ groupable: true})
				 		  ,obj.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaOmnibusDetalle = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'OMNIBUS'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: obj.getDataSourceConsultaOmnibusDetalle(),
				
					columns: [
					           obj.getConfiguracionColumna("IdOmnibus",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("ValorCuotaArchivo")
					          ,obj.getConfiguracionColumna("UltimoValorCuota")
					          ,obj.getConfiguracionColumna("MontoOmnibus")
					          ,obj.getConfiguracionColumna("CantidadCuotas")
					          ,obj.getConfiguracionColumna("ValorCuotaAGF")
					          ,obj.getConfiguracionColumna("MontoAGF")
					          ,obj.getConfiguracionColumna("CantidadCuotasAGF")
					          ,obj.getConfiguracionColumna("DiferenciaValorCuota")
					          ,obj.getConfiguracionColumna("DiferenciaValorCuotaAGF")
					          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})
					]
				}
		};
		return config;
	};

	/* CONSULTA DE SOLICITUDES DE APORTE RESCATE FONDOS MUTUOS*/
	    
	obj.getConsultaSolicitudAporteRescateFondosMutuos = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUD'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

		      	//GCMBIPAPAS-1 Filtro de clientes por Perfiles
					dataSource: obj.getDataSourceSolicitudAporteRescate(),
		      		//dataSource: obj.getDataSourceSolicitudAporteRescate_FiltroPerfiles(),
		      	//Fin	
				
					columns: [
							   obj.getConfiguracionColumna("IdTipoCanalIngreso",{ groupable: true})
							  ,obj.getConfiguracionColumna("FolioContrato",{ groupable: true})
							  ,obj.getConfiguracionColumna("FolioContratoOperacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("TipoContratoOperacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaAceptacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaCertificacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescripcionTipoMovimiento",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescripcionTipoSolicitud",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})
					          ,obj.getConfiguracionColumna("EstadoLiquidacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("FolioCartaLiquidacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("Emisor",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreFondoReal",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreSerieFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdMonedaFondo", {groupable: true})
					          ,obj.getConfiguracionColumna("ValorCuota",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaValorCuotaFondo", {groupable: true})
					          ,obj.getConfiguracionColumna("NumeroCuotas",{aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'FMM','Cantidad','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'FMM','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}})
					          ,obj.getConfiguracionColumna("Monto")
							  ,obj.getConfiguracionColumna("MontoRetencion")
					          ,obj.getConfiguracionColumna("MontoComision")
					          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
					          ,obj.getConfiguracionColumna("Paridad",{ groupable: true})
					          ,obj.getConfiguracionColumna("CostoMonedaLocal")
					          ,obj.getConfiguracionColumna("CostoUF")
					          ,obj.getConfiguracionColumna("MontoMonedaSistema",{aggregates: ["sum"],groupFooterTemplate:" #=(util.bcs_formato(sum,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #",  footerTemplate:" #:  (util.bcs_formato(sum,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}})
					          ,obj.getConfiguracionColumna("TextoPreferenciaPago",{ groupable: true}) 
					          ,obj.getConfiguracionColumna("Spread",{ groupable: true})
					          ,obj.getConfiguracionColumna("RentabilidadReal")
					          ,obj.getConfiguracionColumna("IdSerieFondoDestino",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdEmisorDestino",{ groupable: true})
					          ,obj.getConfiguracionColumna("Switch",{ groupable: true})
					          ,obj.getConfiguracionColumna("Producto",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdIngresoEgreso")
					          ,obj.getConfiguracionColumna("NombreEstadoTesoreria")
					          ,obj.getConfiguracionColumna("FechaLiquidacionTesoreria")
					          ,obj.getConfiguracionColumna("IdOperacionSpot")
					          ,obj.getConfiguracionColumna("NombreEstadoSpot")
					          ,obj.getConfiguracionColumna("NombreTipoOperacionSpot")
					          ,obj.getConfiguracionColumna("IdOmnibus")
					          ,obj.getConfiguracionColumna("IdDividendo")
					          ,obj.getConfiguracionColumna("IdAgrupador")
					          ,obj.getConfiguracionColumna("RegimenTributario")
					          ,obj.getConfiguracionColumna("MotivoRetiro")   
					]
				}
		};
		return config;
	};
	
	obj.getConsultaSolicitudAporteRescateFondosMutuosDetalle = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUD'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: false,
					pageable: true,
					selectable: "multiple, row",
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:150,
					dataSource: obj.getDataSourceSolicitudAporteRescateDetalle(),
					columns: [
							obj.getConfiguracionColumna("IdSolicitudAporte")
							  ,obj.getConfiguracionColumna("IdSerieFondo")	
					          ,obj.getConfiguracionColumna("NombreSerieFondo")
					          ,obj.getConfiguracionColumna("Emisor")
					          ,obj.getConfiguracionColumna("IdRegimenFondoMutuo")
					          ,obj.getConfiguracionColumna("MontoAporte")
					          ,obj.getConfiguracionColumna("Porcentaje")
					          ,obj.getConfiguracionColumna("Switch")
					          
					          
					          ,obj.getConfiguracionColumna("FechaCertificacionAporte")
					          ,obj.getConfiguracionColumna("FechaLiquidacionAporte")
					          ,obj.getConfiguracionColumna("FechaValorCuotaAporte")
					          

								
								
					]
				}
		};
		return config;
	};
	
	obj.getConsultaCargaOperacionesFondosMutuos = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CARGA'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple,row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: obj.getDataSourceCargaOperaciones(),
				
					columns: [
								obj.getConfiguracionColumna("IdRegistroCarga",{ groupable: true})
								,obj.getConfiguracionColumna("IdCarga",{ groupable: true})
								,obj.getConfiguracionColumna("FechaCarga",{ groupable: true})
								,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
								,obj.getConfiguracionColumna("RutEmisor",{ groupable: true})
								,obj.getConfiguracionColumna("CodigoPortafolioEmisor",{ groupable: true})
								,obj.getConfiguracionColumna("Emisor",{ groupable: true})
								,obj.getConfiguracionColumna("DescripcionTipoMovimiento",{ groupable: true})
								,obj.getConfiguracionColumna("FechaMovimiento",{ groupable: true})
								,obj.getConfiguracionColumna("FechaValorCuotaFondo",{ groupable: true})
								,obj.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
								,obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
								,obj.getConfiguracionColumna("NombreCompletoCliente",{ groupable: true})
								,obj.getConfiguracionColumna("DescripcionPortafolioCliente",{ groupable: true})
								,obj.getConfiguracionColumna("CalculoMontoCuota",{ groupable: true})
								,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
								,obj.getConfiguracionColumna("NumeroCuotas",{ groupable: true})
								,obj.getConfiguracionColumna("ValorCuota",{ groupable: true})
								,obj.getConfiguracionColumna("Monto",{ groupable: true})
								,obj.getConfiguracionColumna("MontoComision",{ groupable: true})
								,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
								,obj.getConfiguracionColumna("IdMonedaPago",{ groupable: true})
								,obj.getConfiguracionColumna("IdMedioDePago",{ groupable: true})
								,obj.getConfiguracionColumna("IdSerieFondoDestino",{ groupable: true})
								,obj.getConfiguracionColumna("MLT",{ groupable: true})
								,obj.getConfiguracionColumna("Concepto",{ groupable: true})
								,obj.getConfiguracionColumna("IdRegimenTributarioOrigen",{ groupable: true})
								,obj.getConfiguracionColumna("IdRegimenTributarioDestino",{ groupable: true})
								,obj.getConfiguracionColumna("EstadoCargaDatos",{ groupable: true})
								,obj.getConfiguracionColumna("DescEstado",{ groupable: true})
								,obj.getConfiguracionColumna("NombreEstadoProceso",{ groupable: true})
								,obj.getConfiguracionColumna("DescEstadoProceso",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaResumenCargaOperacionesFondosMutuos = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'RESUMENCARGA'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:300,
			
					dataSource: obj.getDataSourceResumenCargaOperaciones(),
				
					columns: [
					           obj.getConfiguracionColumna("IdCarga", { groupable: true})
					          ,obj.getConfiguracionColumna("FechaCarga", { groupable: true})
					          ,obj.getConfiguracionColumna("FechaMovimiento", { groupable: true})
					          ,obj.getConfiguracionColumna("NombreTipoMovimiento",{ groupable: true})
					          ,obj.getConfiguracionColumna("RutEmisor",{ groupable: true})
					          ,obj.getConfiguracionColumna("Emisor",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
					          ,obj.getConfiguracionColumna("Monto",{ groupable: true})
					          ,obj.getConfiguracionColumna("NumeroCuotas",{ groupable: true})
					          ,obj.getConfiguracionColumna("RegistrosProcesado",{ groupable: true})
					          ,obj.getConfiguracionColumna("RegistrosError",{ groupable: true})
					          ,obj.getConfiguracionColumna("RegistrosProcesoPendiente",{ groupable: true})
					          ,obj.getConfiguracionColumna("RegistrosProcesoProcesado",{ groupable: true})
					          ,obj.getConfiguracionColumna("RegistrosProcesoError",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaSolicitudAporteRescateDiferidos = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUD'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: obj.getDataSourceSolicitudAporteRescate(),
				
					columns: [
					           obj.getConfiguracionColumna("IdSolicitudAporteRescate")
					          ,obj.getConfiguracionColumna("FechaEmision")
					          ,obj.getConfiguracionColumna("DocumentoIdentidad")
					          ,obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("DescripcionTipoMovimiento")
					          ,obj.getConfiguracionColumna("CalculoMontoCuota")
					          ,obj.getConfiguracionColumna("IdMoneda")
					          ,obj.getConfiguracionColumna("CostoMonedaLocal")
					          ,obj.getConfiguracionColumna("Paridad")
					          ,obj.getConfiguracionColumna("IdMonedaFondo")
					          ,obj.getConfiguracionColumna("Monto")
					          ,obj.getConfiguracionColumna("ValorCuota")
					          ,obj.getConfiguracionColumna("NumeroCuotas")
					          ,obj.getConfiguracionColumna("MontoComision")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaInterfazSolicitudAgf = function(){
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUDAGF'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

		      	//GCMBIPAPAS-1 Filtro de clientes por Perfiles (Visor Interfaz Agf)
					//dataSource: obj.getDataSourceConsultaInterfazSolicitudAgf(),
					dataSource: obj.getDataSourceConsultaInterfazSolicitudAgf_FiltroPerfiles(),
				//Fin GCMBIPAPAS-1 
					columns: [
					           obj.getConfiguracionColumna("IdInterfazAgf",{ groupable: true})
					          ,obj.getConfiguracionColumna("TipoRegistro",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaIngreso",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
					          ,obj.getConfiguracionColumna("FechaAceptacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
					          ,obj.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreEstado",{ groupable: true})
					          ,obj.getConfiguracionColumna("Observacion",{ groupable: true})
					]
				}
		};
		return config;
	}
	
	obj.getConsultaFondosIndustria = function(){
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'FONDOS'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: false,
		      		filterable: true,
					groupable: false,
					pageable: true,
					selectable: "single, row",
					reorderable: true,
					allowCopy: true,
					resizable: false,
		      		autoBind: true,
		      		scrollable: true,
		      		height:290,
			
					dataSource: new bcs.newDataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							sort: { field: "NombreFFMM", dir: "asc" },
							pageSize : 200,
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=900200", 
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdFondoIndustria			: { type : "number"},
										RunFFMM					: { type : "string"},
										NombreFFMM				: { type : "string"},
										SociedadAdmFFMM				: { type : "string"},
										RutSociedadAdmFFMM			: { type : "string"},
										IdMoneda				: { type : "string"}
										
									}
								}
							}
						},
						
					}),
				
					columns: [
					           obj.getConfiguracionColumna("RunFFMM")
					          ,obj.getConfiguracionColumna("NombreFFMM")
					          ,obj.getConfiguracionColumna("SociedadAdmFFMM")
					          ,obj.getConfiguracionColumna("RutSociedadAdmFFMM")
					          ,obj.getConfiguracionColumna("IdMoneda")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaInstrumentosFondosMutuos = function(){
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'FONDOS'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.newDataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							sort: { field: "IdFondo", dir: "desc" },
							pageSize : 200,
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1248",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										Emisor						: { type : "string"},
										IdEmisor					: { type : "string"},
										IdFondo						: { type : "string"},
										IdMoneda					: { type : "string"},
										IdSerieFondo				: { type : "string"},
										NombreEstado				: { type : "string"},
										NombreFamilia				: { type : "string"},
										NombreFondo					: {	type : "string"},
										NombreSerieFondo 			: { type : "string"},
										TipoInstrumento				: { type : "string"},
										RazonSocialEmisor			: { type : "string"},
										ValorCuota					: { type : "number"},
										FechaValorCuota				: { type : "sysdate"},
										ValorDolarAporte			: { type : "number"},
										ValorDolarRescate			: { type : "number"},
										NombreTipoInstrumento		: { type : "string"},
										IdEmpresa					: { type : "number"},
										DecimalesValorCuota			: { type : "number"},
										EsDiferidoUI				: { type : "code"},
										MontoMaximo					: { type : "number"},
										MontoMinimo					: { type : "number"},
										NombreCorto					: { type : "string"},
										EsMoneyMarketUI				: { type : "code"},
										AportesAltos				: { type : "string"},
										DocumentoIdentidadEmisor	: { type : "string"},
										MontoPatrimonioEmisor		: { type :  "number"},
										RestringeAporte				: { type : "string"},
										RestringeRescate			: { type : "string"}
									}
								}
							}
						},
						
					}),
				
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("NombreSerieFondo")
					          ,obj.getConfiguracionColumna("Emisor")
							  ,obj.getConfiguracionColumna("DocumentoIdentidadEmisor")
					          ,obj.getConfiguracionColumna("IdMoneda")
					          ,obj.getConfiguracionColumna("ValorCuota")
					          ,obj.getConfiguracionColumna("FechaValorCuotaFondo")
					          ,obj.getConfiguracionColumna("Diferido")
					          ,obj.getConfiguracionColumna("MoneyMarket")
					          ,obj.getConfiguracionColumna("NombreEstado")
					          ,obj.getConfiguracionColumna("Familia")
					          ,obj.getConfiguracionColumna("ValorDolarAporte")
					          ,obj.getConfiguracionColumna("ValorDolarRescate")
					          ,obj.getConfiguracionColumna("TipoInstrumento")
					          ,obj.getConfiguracionColumna("MontoMinimo")
					          ,obj.getConfiguracionColumna("MontoMaximo")
					          ,obj.getConfiguracionColumna("NombreTipoFondo")
					          ,obj.getConfiguracionColumna("MontoPatrimonioEmisor")
					          ,obj.getConfiguracionColumna("RestringeAporte")
					          ,obj.getConfiguracionColumna("RestringeRescate")
					          
					          
					]
				}
		};
		return config;
	};
	
	obj.getConsultaAperturaCierreFondosMutuos = function(){
		var config = {};
		config = {
				buscar : function(apiGrid, filtros){
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'INSTRUMENTOAPERTURACIERRE'));
				},
				options : {
					userSettings: true, 
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							sort: { field: "IdFondo", dir: "desc" },
							pageSize : 200,
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1374",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdFondo							: { type : "string"},
										Nemotecnico						: { type : "string"},
										IdEmisor						: { type : "string"},
										EsFondoNacionalUI				: { type : "code"},
										EsFondoNacional					: { type : "string"},
										EstadoAporte					: { type : "string"},
										EstadoAporteUI					: { type : "code"},
										EstadoRescate					: { type : "string"},
										EstadoRescateUI					: { type : "code"},
										EstadoReinversion				: { type : "string"},
										EstadoReinversionUI				: { type : "code"},
										EstadoLiquidez					: { type : "string"},
										EstadoLiquidezUI				: { type : "code"},
										BloqueoAporte					: { type : "string"},
										BloqueoAporteUI					: { type : "code"},
										BloqueoRescate					: { type : "string"},
										BloqueoRescateUI				: { type : "code"},
										Perfil							: { type : "string"},
										IdMoneda						: { type : "string"},
										UltimoFechaHoraCierreAporte 	: { type : "sysdatetime"},
										UltimoFechaHoraCierreRescate	: { type : "sysdatetime"},
										UltimoFechaHoraCierreReinversion: { type : "sysdatetime"},
										IdEmpresa						: { type : "string"},
										FechaFondo						: { type : "sysdate"},
									}
								}
							}
						},
						
					}),
				
					columns: [
					           obj.getConfiguracionColumna("IdFondo")
					          ,obj.getConfiguracionColumna("Emisor")
					          ,obj.getConfiguracionColumna("FondoNacional")
					          ,obj.getConfiguracionColumna("FechaFondo")
					          ,obj.getConfiguracionColumna("EstadoAporte")
					          ,obj.getConfiguracionColumna("EstadoRescate")
					          ,obj.getConfiguracionColumna("EstadoReinversion")
					          ,obj.getConfiguracionColumna("BloqueoAporte")
					          ,obj.getConfiguracionColumna("BloqueoRescate")
					          ,obj.getConfiguracionColumna("UltimoFechaHoraCierreAporte")
					          ,obj.getConfiguracionColumna("UltimoFechaHoraCierreRescate")
					          ,obj.getConfiguracionColumna("UltimoFechaHoraCierreReinversion")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaHorarioCierreFondosMutuos = function(){
		var config = {};
		config = {
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							pageSize : 200,
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1408",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										HoraCierreAporteComercial        : { type : "time"},
										HoraCierreAporteBackOffice	     : { type : "time"},
										HoraCierreRescateComercial   	 : { type : "time"},
										HoraCierreRescateBackOffice		 : { type : "time"},
										HoraCierreReinversionComercial   : { type : "time"},
										HoraCierreReinversionBackOffice	 : { type : "time"},
										HoraCierreLiquidezComercial      : { type : "time"},
										HoraCierreLiquidezBackOffice	 : { type : "time"},
									}
								}
							}
						},
						
					}),
				}
		};
		return config;
	};
	
	obj.getConsultaValoresCuotaHistorico = function(){
		var config = {};
		config = {
				buscar : function(apiGrid, filtros){
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'VALORCUOTAHISTORICO'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							pageSize : 200,
							sort: { field: "FechaCalculo", dir: "desc" },
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1416",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdValorCuotaHistorico        : { type : "number"},
										IdSerieFondo	     		 : { type : "string"},
										FechaVigencia   	 		 : { type : "date"},
										ValorCuota		 			 : { type : "number"},
										IdMoneda   					 : { type : "string"},
										Observacion					 : { type : "string"},
										FechaCalculo	 			 : { type : "sysdate"},
										FechaIngresoRegistro      	 : { type : "sysdatetime"},
										DecimalesValorCuota			 : { type : "number"},
										IdEmpresa 					 : { type : "number"},
									}
								}
							}
						},
						
					}),
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("FechaVigencia")
					          ,obj.getConfiguracionColumna("ValorCuota")
					          ,obj.getConfiguracionColumna("IdMoneda")
					          ,obj.getConfiguracionColumna("Observacion")
					          ,obj.getConfiguracionColumna("FechaCalculo")
					          ,obj.getConfiguracionColumna("FechaIngresoRegistro")
					]
				}
		};
		return config;
	}
	
	obj.getConsultaValoresTipoCambioHistorico = function(){
		var config = {};
		config = {
				buscar : function(apiGrid, filtros){
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'VALORTIPOCAMBIOHISTORICO'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							pageSize : 200,
							sort: { field: "FechaCalculo", dir: "desc" },
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3037",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdValorTipoCambioHistorico   : { type : "number"},
										IdSerieFondo	     		 : { type : "string"},
										FechaVigencia   	 		 : { type : "date"},
										ValorDolarAporte		 	 : { type : "number"},
										ValorDolarRescate		 	 : { type : "number"},
										Observacion					 : { type : "string"},
										FechaCalculo	 			 : { type : "sysdate"},
										FechaIngresoRegistro      	 : { type : "sysdatetime"},
										IdEmpresa 					 : { type : "number"},
									}
								}
							}
						},
						
					}),
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("FechaVigencia")
					          ,obj.getConfiguracionColumna("ValorDolarAporte")
					          ,obj.getConfiguracionColumna("ValorDolarRescate")
					          ,obj.getConfiguracionColumna("Observacion")
					          ,obj.getConfiguracionColumna("FechaCalculo")
					          ,obj.getConfiguracionColumna("FechaIngresoRegistro")
					]
				}
		};
		return config;
	}
	
	obj.getSchemaValorCuota = function() {
		return { 
			model: {
				fields : {
					IdSerieFondo 	   		: { type : "string"},
					Moneda 	  	  	   		: { type : "string"},
					FechaVigencia      		: { type : "date"},
					FechaCalculo   	   		: { type : "sysdate"},
					Valor      	  	   		: { type : "number"},
					ValorDolarAporte   		: { type : "number"},
					ValorDolarRescate  		: { type : "number"},
					DecimalesValorCuota		: { type : "number"},
					ISIN 	  	  	   		: { type : "string"},
					Observacion        		: { type : "string"},
					Estado                  : { type : "string"},
					DescEstado              : { type : "string"},
					ValorCuota              : { type : "number"},
					IdMoneda                : { type : "string"},
					EmisorValorCuota        : { type : "string"},
					ParidadValorCuota  		: { type : "number"},
					ValorCuotaFechaAnterior : { type : "number"},
					ValorCuotaCalculado     : { type : "number"},
					VariacionValorCuota     : { type : "number"}
				}
			}
		}
	};
	
	
	obj.getSchemaSolicitudProcesada = function() {
		return { 
			model: {
				fields : {
					Codigo 	   				: { type : "string"},
					EstadoProceso 	  	  	: { type : "string"},
					FolioAPV      			: { type : "string"},
					FolioCSC   	   			: { type : "string"},
					IdCliente      	  	   	: { type : "number"},				
					IdEstado   				: { type : "number"},			
					MontoDeposito			: { type : "number"},
					NombreCompleto			: { type : "string"},
					FolioPAC				: { type : "number"},
					DocumentoIdentidad		: { type : "string"},
					EstadoCliente			: { type : "string"},
					EstadoPortafolio		: { type : "string"},
					IdentServicio			: { type : "string"},
					EstadoPAC				: { type : "string"},
					DescripcionSolicitud	: { type : "string"},
					EstadoCargo				: { type : "string"},
					DescripcionEstadoCargo	: { type : "string"},
					EstadoRendicion			: { type : "string"},
					EstadoCSC				: { type : "string"},
					EstadoSAAPV				: { type : "string"}
					
				}
			}
		}
	};
	obj.getSchemaValorTipoCambio = function() {
		return { 
			model: {
				fields : {
					IdSerieFondo 	   		: { type : "string"},
					FechaVigencia      		: { type : "date"},
					FechaCalculo   	   		: { type : "sysdate"},
					ValorDolarAporte   		: { type : "number"},
					ValorDolarRescate  		: { type : "number"},
					Observacion        		: { type : "string"},
					Estado                  : { type : "string"},
					DescEstado              : { type : "string"}
				}
			}
		}
	};
	
	
	obj.getDataSourceValorCuotaArray = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : obj.getSchemaValorCuota()			
			}
		})
	};
	
	obj.getDataSourceSolicitudesProcesadas = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : obj.getSchemaSolicitudProcesada()			
			}
		})
	};
	
	obj.getDataSourceValorTipoCambioArray = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : obj.getSchemaValorTipoCambio()			
			}
		})
	};
	
	obj.getDataSourceTramoComisionesArray = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				sort: { field: "DiasHasta", dir: "asc" },
				schema : obj.getSchemaTramoComisiones()			
			}
		})
	};
	
	
	obj.getSolicitudesRendicion = function(){
		var config = {};
		config = {
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

					dataSource: obj.getDataSourceSolicitudesProcesadas(),
				
					columns: [
							   obj.getConfiguracionColumna("IdentServicio"),
							   obj.getConfiguracionColumna("NombreCompleto"),
							   obj.getConfiguracionColumna("DocumentoIdentidad"),
							   obj.getConfiguracionColumna("EstadoCliente"),
							   obj.getConfiguracionColumna("EstadoPortafolio"),
							   obj.getConfiguracionColumna("FolioAPV"),
					           obj.getConfiguracionColumna("FolioCSC"),
					           
					           obj.getConfiguracionColumna("FolioPAC"),
					           obj.getConfiguracionColumna("EstadoPAC"),
					           obj.getConfiguracionColumna("MontoDeposito"),
					           obj.getConfiguracionColumna("EstadoCargo"),
					           obj.getConfiguracionColumna("EstadoRendicion"),
					           obj.getConfiguracionColumna("EstadoSAAPV"),
					           obj.getConfiguracionColumna("EstadoCSC"),
					           obj.getConfiguracionColumna("EstadoProceso"),
					]
				}
		};
		return config;
	};
	
	
	obj.getConsultaValorCuota = function(){
		var config = {};
		config = {
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

					dataSource: obj.getDataSourceValorCuotaArray(),
				
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("IdMoneda")
					          ,obj.getConfiguracionColumna("FechaVigencia")
					          ,obj.getConfiguracionColumna("FechaCalculo")
					          ,obj.getConfiguracionColumna("ValorCuota")
					          ,obj.getConfiguracionColumna("Observacion")
					          ,obj.getConfiguracionColumna("Estado")
					          ,obj.getConfiguracionColumna("DescEstado")
					          ,obj.getConfiguracionColumna("EmisorValorCuota")
					          ,obj.getConfiguracionColumna("ParidadValorCuota")
					          ,obj.getConfiguracionColumna("ValorCuotaCalculado")
					          ,obj.getConfiguracionColumna("VariacionValorCuota")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaValorTipoCambio = function(){
		var config = {};
		config = {
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,

					dataSource: obj.getDataSourceValorTipoCambioArray(),
				
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("FechaVigencia")
					          ,obj.getConfiguracionColumna("FechaCalculo")
					          ,obj.getConfiguracionColumna("ValorDolarAporte")
					          ,obj.getConfiguracionColumna("ValorDolarRescate")
					          ,obj.getConfiguracionColumna("Observacion")
					          ,obj.getConfiguracionColumna("Estado")
					          ,obj.getConfiguracionColumna("DescEstado")
					]
				}
		};
		return config;
	};
	
	obj.getSchemaTramoComisiones = function(){
		return {
			model : {
				fields : {
					IdInterno			 : { type : "number"},
					IdTramo		 		 : { type : "number"},
					DiasDesde		     : { type : "number"},
					DiasHasta		     : { type : "number"},
					PorcentajeComision	 : { type : "number"},
				}
			}
		}
	};
	
	
	obj.getDataSourceTramoComisiones = function(){
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "DiasDesde", dir: "asc" },
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1427",
						dataType:"json"
		    		}
		    	},
				schema : obj.getSchemaTramoComisiones()
			}
		})
	};
	
	
	obj.getTramoComisionesFondoMutuo = function(){
		var config = {};
		config = {
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: false,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:150,
	
					dataSource: obj.getDataSourceTramoComisiones(),
				
					columns: [
					           obj.getConfiguracionColumna("IdTramo")
					          ,obj.getConfiguracionColumna("DiasDesde")
					          ,obj.getConfiguracionColumna("DiasHasta")
					          ,obj.getConfiguracionColumna("PorcentajeComision")
					]
				}
		};
		return config;
	};
	
	obj.getTramoComisionesFondoMutuoArray = function(){
		var config = {};
		config = {
				options : {
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: false,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: true,
		      		scrollable: true,
		      		height:150,
	
					dataSource: obj.getDataSourceTramoComisionesArray(),
				
					columns: [
					           obj.getConfiguracionColumna("IdTramo")
					          ,obj.getConfiguracionColumna("DiasDesde")
					          ,obj.getConfiguracionColumna("DiasHasta")
					          ,obj.getConfiguracionColumna("PorcentajeComision")
					]
				}
		};
		return config;
	};
	
	obj.getDataSourceComisionesFondoMutuo = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdComision", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1426",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdComision		 	 : { type : "number"},
							IdSerieFondo		 : { type : "string"},
							Descripcion		     : { type : "string"},
							TipoComision	     : { type : "string"},
							FechaVigenciaDesde	 : { type : "date"},
							FechaVigenciaHasta   : { type : "date"},
							PorcentajeLiberado   : { type : "number"},
							Estado				 : { type : "string"},
							IdEmpresa			 : { type : "number"}
						}
					}
				}			
			}
		})
	};
	
	obj.getDataSourceComisionesFondoMutuoMaxima = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdComision", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1430",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdComision		 	 : { type : "number"}
						}
					}
				}			
			}
		})
	};
	
	obj.getDataSourceComisionesFondoMutuoActiva = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdComision", dir: "asc" }, //campo y criterio de ordenamiento
				filter: [{ field: "Estado", operator: "eq", value: 'ACT' }],
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1430",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdComision		 	: { type : "number"},
							IdSerieFondo		: { type : "text"},
							Estado				: { type : "text"}
						}
					}
				}			
			}
		})
	};
	
	obj.getDataSourceEventoCapitalFondoMutuo = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdEventoCapital", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=364",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdEventoCapital		 : { type : "number"},
							Nemo		 		 : { type : "string"},
							Monto		 		 : { type : "number"},
							CantidadCuotas	     : { type : "number"},
							IdMoneda			 : { type : "string"},
							NumeroDividendo		 : { type : "number"},
							FechaPago   		 : { type : "date"}
						}
					}
				}			
			}
		})
	};
	
	obj.getDataSourceDetalleEventoCapitalFondoMutuo = function () {
		return new bcs.DataSource({
			
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdSolicitudAporteRescate", dir: "desc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=396",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdSerieFondo		 : { type : "string"},
							NumeroCuotas		 : { type : "number"},
							NombreCompleto	     : { type : "string"},
							CodigoPortafolio	 : { type : "string"}
						}
					}
				}			
			}
		})
	};
	
	obj.getDataSourceCuadraturaAGF = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverSorting : true, 
				sort: { field: "IdCuadraturaCustodia", dir: "asc" }, //campo y criterio de ordenamiento
				transport: {
		    		read: {
		    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600012",
						dataType:"json"
		    		}
		    	},
				schema : {
					model : {
						fields : {
							IdCuadraturaCustodia		 	: { type : "number"},
							ISIN	   		 				: { type : "string"},
							RunFondo		 				: { type : "string"},
							IdSerieFondo		 			: { type : "string"},
							NombreFondo	     				: { type : "string"},
							TotalCuotasAGF	 	 			: { type : "number"},
							TotalCuotas   		 			: { type : "number"},
							DiferenciaCuotas   			 	: { type : "number"},
							DiferenciaValorizacion   		: { type : "number"},
							ValorCuota		 				: { type : "number"},
							IndicadorCuadratura				: { type : "string"},
							IdMonedaFondo	     			: { type : "string"},
							ValorizacionCuotasAGF	 	 	: { type : "number"},
							ValorizacionCuotas   			: { type : "number"},
							IdEstado	 	 				: { type : "string"},
							IdRegimen   		 			: { type : "string"},
							FechaCuadratura   		 		: { type : "date"},
							DecimalesValorCuota				: { type : "number"},
							IdEmisor   		 				: { type : "string"}
						}
					}
				}			
			}
		})
	};
	
	
	
	obj.calcularMontoCuota = function(fondo,monto,cantidad,tipoOperacion,idMonedaAporte,tipo,fecha){
		 var valores = {};
		 var datos = {};
		 
		 datos = {
			p_IdEmpresa : $rootScope.SesionIdEmpresa,
			p_Series : [{
				IdInterno: 0,
				Fecha:util.dateSPdate(util.dateInicioDia(fecha)),
				IdSerieFondo: fondo ? fondo.IdSerieFondo : '',
				CalculoPorMontoCuota:tipo,
				Monto:tipo === 'M' || tipo === 'T'  ? monto : 0,
				NumeroCuotas:tipo === 'C' || tipo === 'T' ? cantidad : 0,
				IdMonedaFondo:fondo ? fondo.IdMoneda : '',
				IdMonedaInversion:idMonedaAporte,
				TipoSolicitud:tipoOperacion,
				TipoFactorAjuste:'UF',
				IdEmpresa:fondo ? fondo.IdEmpresa : 0
			}]}
			 
		return bcsServicioFondosMutuos.CalcularMontoNumeroCuota(datos).$promise;

	 };
	 
	 obj.calcularMontoCuotaMasivo = function(series){
		 var valores = {};
		 var datos = {};
		 
		 datos = {
			p_IdEmpresa : $rootScope.SesionIdEmpresa,
			p_Series : []
		 }
		 
		 $.each(series,function(key,value){
			 datos.p_Series.push({
					IdInterno: value.IdInterno,
					Fecha:util.dateSPdate(util.dateInicioDia(value.Fecha)),
					IdSerieFondo: value.IdSerieFondo,
					CalculoPorMontoCuota: value.CalculoPorMontoCuota,
					Monto:value.CalculoPorMontoCuota === 'M' || value.CalculoPorMontoCuota === 'T'  ? value.Monto : 0,
					NumeroCuotas:value.CalculoPorMontoCuota === 'C' || value.CalculoPorMontoCuota === 'T' ? value.NumeroCuotas : 0,
					IdMonedaFondo:value.IdMonedaFondo,
					IdMonedaInversion: value.IdMonedaInversion,
					TipoSolicitud: value.TipoSolicitud,
					TipoFactorAjuste:'UF',
					IdEmpresa:$rootScope.SesionIdEmpresa
			  });	
		 });
		 
		 
			 
		return bcsServicioFondosMutuos.CalcularMontoNumeroCuota(datos).$promise;

	 };
	 
	 obj.getConsultaLogCertificacionAporteRescate = function(){
			var config = {};
			config = {
					buscar : function(apiGrid, filtros){
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'LOGCERTIFICACIONAPORTERESCATE'));
					},
					options : {
						userSettings: true, 
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:350,
				
						dataSource: new bcs.DataSource({ 
							type : "odata",
							definition : {
								serverFiltering : true, 
								serverPaging : true, 
								serverSorting : true, 
								sort: { field: "IdLogCertificacionAporteRescate", dir: "desc" },
								pageSize : 200,
								transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1517",
										dataType: "json"
									}
								},
								schema : {
									model : {
										fields : {
											IdLogCertificacionAporteRescate : { type : "number"},
											IdSolicitudAporteRescate	: { type : "number"},
											NombreEstado			    : { type : "string"},
											Codigo                      : { type : "code"},
											CodigoPortafolio			: { type : "string"},
											TipoMovimiento         		: { type : "string"},
											FechaSistema         		: { type : "sysdate"},
											IdSerieFondo         		: { type : "string"},
											Monto		    			: { type : "number"},
											ValorCuota         			: { type : "number"},
											NumeroCuotas		    	: { type : "number"},
											MontoOriginal		    	: { type : "number"},
											FechaIngresoRegistro		: { type : "sysdatetime"},
											IdMoneda					: { type : "string"},
											IdCliente					: { type : "string"},
											NombreCompleto         		: { type : "string"},
											DocumentoIdentidad		    : { type : "string"},
											DescripcionPortafolio		: { type : "string"},
											CodigoUsuario		    	: { type : "string"},
											TipoCambio					: { type : "string"},
											IdMonedaOriginal			: { type : "string"},
											IdEmisor       			    : { type : "string"},
											RazonSocialEmisor		    : { type : "string"},
											EsSimulacionUI				: { type : "code"},
										}
									}
								}
							},
							
						}),
					
						columns: [
						           obj.getConfiguracionColumna("IdSolicitudAporteRescate")
						          ,obj.getConfiguracionColumna("NombreEstado")
						          ,obj.getConfiguracionColumna("Simulacion")
						          ,obj.getConfiguracionColumna("Codigo")
						          ,obj.getConfiguracionColumna("TipoMovimiento")
						          ,obj.getConfiguracionColumna("FechaSistema")
						          ,obj.getConfiguracionColumna("IdSerieFondo")
						          ,obj.getConfiguracionColumna("Emisor")
						          ,obj.getConfiguracionColumna("RazonSocialEmisor")
						          ,obj.getConfiguracionColumna("MontoOperacion")
						          ,obj.getConfiguracionColumna("IdMoneda")
						          ,obj.getConfiguracionColumna("ValorCuota")
						          ,obj.getConfiguracionColumna("NumeroCuotas")
						          ,obj.getConfiguracionColumna("MontoOriginal")
						          ,obj.getConfiguracionColumna("TipoCambio")
						          ,obj.getConfiguracionColumna("IdMonedaOriginal")
						          ,obj.getConfiguracionColumna("NombreCompleto")
						          ,obj.getConfiguracionColumna("DocumentoIdentidad")
						          ,obj.getConfiguracionColumna("DescripcionPortafolio")
						          ,obj.getConfiguracionColumna("FechaIngresoRegistro")
						          ,obj.getConfiguracionColumna("CodigoUsuario")
						]
					}
			};
			return config;
		};
	
		obj.getConsultaConfirmacionCertificacionAporteRescate = function(){
			var config = {};
			config = {
					buscar : function(apiGrid, filtros){
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CONFIRMACIONCERTIFICACIONAPORTERESCATE'));
					},
					options : {
						userSettings: true, 
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:350,
				
						dataSource: new bcs.newDataSource({
							type : "call",
							definition : { 
								sort: { field: "IdSolicitudAporteRescate", dir: "asc" },
								serverFiltering : false,
								serverPaging 	: false,
								serverSorting 	: false,
								pageSize		: 200,
								callTransaction : 3163,								
								schema :  {
									model : {
										fields : {
											IdSolicitudAporteRescate	: { type : "string"},
											IdSolicitudAporteRescateRelacionado: { type : "string"},
											Codigo                      : { type : "string"},
											IdSerieFondo         		: { type : "string"},
											Estado						: { type : "string"},
											NombreEstado				: { type : "string"},
											DescripcionCodigo			: { type : "string"},
											DocumentoIdentidad   		: { type : "string"},
											CodigoPortafolio			: { type : "string"},
										}	
									}
								}			
							},
						}),
					
						columns: [
						           obj.getConfiguracionColumna("IdSolicitudAporteRescate")
						          ,obj.getConfiguracionColumna("IdSolicitudAporteRescateRelacionado")
						          ,obj.getConfiguracionColumna("IdSerieFondo")
						          ,obj.getConfiguracionColumna("NombreEstado")
						          ,obj.getConfiguracionColumna("CodigoConfirmacionCertificacion")
						          ,obj.getConfiguracionColumna("DocumentoIdentidad")
						          ,obj.getConfiguracionColumna("CodigoPortafolio")
						]
					}
			};
			return config;
		};
		
		obj.getFondosAdministracionLiquidez = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'FONDOSLIQUIDEZ'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: false,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:200,
				
						dataSource: obj.getDataSourceFondosAdministracionLiquidez(),
					
						columns: [
						           obj.getConfiguracionColumna("IdMoneda")
						          ,obj.getConfiguracionColumna("IdSerieFondo")
						          ,obj.getConfiguracionColumna("MontoMinimo")
						          ,obj.getConfiguracionColumna("HoraCierreLiquidezComercial")
						]
					}
			};
			return config;
		};
		
		obj.getEventoCapitalFondoMutuo = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUADRATURA'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: false,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:150,
				
						dataSource: obj.getDataSourceEventoCapitalFondoMutuo(),
					
						columns: [
								   obj.getConfiguracionColumna("NumeroDividendo")
								  ,obj.getConfiguracionColumna("Nemo")
						          ,obj.getConfiguracionColumna("MontoDividendo")
						          ,obj.getConfiguracionColumna("CantidadCuotasDividendo")
						          
						]
					}
			};
			return config;
		};
		
		
		
		obj.getDetalleEventoCapitalFondoMutuo = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUADRATURA'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: false,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:400,
				
						dataSource: obj.getDataSourceDetalleEventoCapitalFondoMutuo(),
					
						columns: [
						           obj.getConfiguracionColumna("NombreCompleto")
						          ,obj.getConfiguracionColumna("CodigoPortafolio")
						          ,obj.getConfiguracionColumna("NumeroCuotas")
						          ,obj.getConfiguracionColumna("IdSerieFondo")
						]
					}
			};
			return config;
		};
		
		obj.getCuadraturaAGF = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUADRATURAAGF'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: false,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:600,
				
						dataSource: obj.getDataSourceCuadraturaAGF(),
					
						columns: [
						           obj.getConfiguracionColumna("IdCuadraturaCustodia")
						          ,obj.getConfiguracionColumna("RunFondo")
						          ,obj.getConfiguracionColumna("IdSerieFondo")
						          ,obj.getConfiguracionColumna("NombreFondo")
						          ,obj.getConfiguracionColumna("IdRegimen")
						          ,obj.getConfiguracionColumna("TotalCuotasAGF")
						          ,obj.getConfiguracionColumna("TotalCuotas")
						          ,obj.getConfiguracionColumna("DiferenciaCuotas")
						          ,obj.getConfiguracionColumna("DiferenciaValorizacion")
						          ,obj.getConfiguracionColumna("ValorCuota")
						          ,obj.getConfiguracionColumna("IdMonedaFondo")
						          ,obj.getConfiguracionColumna("ValorizacionCuotasAGF")
						          ,obj.getConfiguracionColumna("ValorizacionCuotas")
						          ,obj.getConfiguracionColumna("IdEstado")
						          ,obj.getConfiguracionColumna("ISIN")
						          ,obj.getConfiguracionColumna("IndicadorCuadratura")
						]
					}
			};
			return config;
		};
		
		obj.getConsultaLogAdministracionLiquidez = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'LOGADMINISTRACIONLIQUIDEZ'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height: 450,
				
						dataSource: obj.getDataSourceConsultaLogAdministracionLiquidez(),
					
						columns: [
						           obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						          ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
						          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
						          ,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
						          ,obj.getConfiguracionColumna("Codigo",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
						          ,obj.getConfiguracionColumna("Saldo",{ groupable: true})
						          ,obj.getConfiguracionColumna("MontoMinimo",{ groupable: true})
						]
					}
			};
			return config;
		}
	 
		obj.getConsultaFamiliasFondosMutuos = function(){
			var config = {};
			config = {
					buscar : function(apiGrid, filtros){
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'FAMILIAFONDOSMUTUOS'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:250,
				
						dataSource: new bcs.DataSource({ 
							type : "odata",
							definition : {
								serverFiltering : true, 
								serverPaging : true, 
								serverSorting : true, 
								pageSize : 200,
								sort: { field: "IdFamiliaFondo", dir: "desc" },
								transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1359",
										dataType: "json"
									}
								},
								schema : {
									model : {
										fields : {
											IdFamiliaFondo        : { type : "string"},
											NombreFamilia	      : { type : "string"},
											IdEstado   	 		  : { type : "string"},
											Estado				  : { type : "string"}
										}
									}
								}
							},
							
						}),
						columns: [
						           obj.getConfiguracionColumna("IdFamiliaFondo")
						          ,obj.getConfiguracionColumna("NombreFamilia")
						          ,obj.getConfiguracionColumna("Estado")
						]
					}
			};
			return config;
		}
		
		obj.ConsultaArchivoFondoMutuo = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1694",
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
						    IdArchivoFondoMutuo		: {type: "string"},
							VersionArchivo			: {type: "string"},
							Esquema					: {type: "string"},
							Version 				: {type: "string"},
							IdTipoDocumento 		: {type: "string"},
							Descripcion				: {type: "string"},
							NombreArchivo			: {type: "string"},
							IdArchivo				: {type: "string"},
							TipoArchivo				: {type: "string"},
							ServicioArchivo			: {type: "string"},
							IdEmpresa				: {type: "number"},
							IdEstado				: {type: "string"}						
						}
					}
				}
			},
		});	
		
		obj.getReport = function(dataTemplate,winID){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.IdReporte,
				"parameters"	: {},
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"data"		: dataTemplate.data,
						"parameters": dataTemplate.parameters
					},
					"subReports": dataTemplate.subReports
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					//var fileURL = URL.createObjectURL(file);
					var nombreArchivo = dataTemplate.NombreReporte+'_'+$rootScope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($rootScope.winID,false);
					if(winID){
						bcsComunes.ventanaOverlay(winID,false);
					}
					
				})
				.error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
					bcsComunes.ventanaOverlay($rootScope.winID,false);
					if(winID){
						bcsComunes.ventanaOverlay(winID,false);
					}
				});
		};
		
		/* CONSULTA DE CUSTODIA FONDOS MUTUOS*/
	    
		obj.getConsultaCustodiaFondosMutuos = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUSTODIA'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:450,
				
						dataSource: obj.getDataSourceConsultaCustodiaFondosMutuos(),
					
						columns: [
								   obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						          ,obj.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdClasificacionTipoInstrumento",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdTipoInstrumento",{ groupable: true})
								  ,obj.getConfiguracionColumna("Nemotecnico",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdRegimenFondoMutuo")
								  ,obj.getConfiguracionColumna("CantidadCustodia",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
								  ,obj.getConfiguracionColumna("ValorCuota",{ groupable: true})
								  ,obj.getConfiguracionColumna("Monto",{ groupable: true})
								  ,obj.getConfiguracionColumna("MontoMonedaSistema",{ groupable: true})
								  ,obj.getConfiguracionColumna("INGPrecioPromedioCompra",{ groupable: true})
								  ,obj.getConfiguracionColumna("INGMontoPrecioPromedioMonedaInstrumento",{ groupable: true})
								  ,obj.getConfiguracionColumna("CantidadGarantia",{ groupable: true})
								  ,obj.getConfiguracionColumna("CantidadTransIng",{ groupable: true})
								  ,obj.getConfiguracionColumna("CantidadTransEgr",{ groupable: true})
								  ,obj.getConfiguracionColumna("CantidadTransSolicitudesAporte",{ groupable: true})
								  ,obj.getConfiguracionColumna("CantidadTransSolicitudesRescate",{ groupable: true})
								  ,obj.getConfiguracionColumna("Producto",{ groupable: true})
								  ,obj.getConfiguracionColumna("RegimenTributario2")
								  ,obj.getConfiguracionColumna("RegimenTributarioGarantia",{ groupable: true})
								  ,obj.getConfiguracionColumna("NombreSerieFondo",{ groupable: true})
						]
					}
			};
			
			return config;
		};
		
	    
		obj.getConsultaComisionesNN = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUSTODIA'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:450,
				
						dataSource: obj.getDataSourceConsultaComisionesNN(),
					
						columns: [
								   obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
								  ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
						          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
								  ,obj.getConfiguracionColumna("FolioCartera")
								  ,obj.getConfiguracionColumna("FechaHasta")
								  ,obj.getConfiguracionColumna("FechaCompra")
								  ,obj.getConfiguracionColumna("DiasPermanencia")
								  ,obj.getConfiguracionColumna("ComisionAnual")
								  ,obj.getConfiguracionColumna("ComisionDiaria")
								  ,obj.getConfiguracionColumna("CuotasCliente")
								  ,obj.getConfiguracionColumna("ComisionNN")
								  ,obj.getConfiguracionColumna("CuotasFinalesCliente")
								  ,obj.getConfiguracionColumna("SecuenciaRescateNN")
						]
					}
			};
			return config;
		};
		
	    
		obj.getConfirmacionComisionesNN = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUSTODIA'));
					},
					options : {
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:450,
				
						dataSource: obj.getDataSourceConfirmacionComisionesNN(),
					
						columns: [
								   obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
								  ,obj.getConfiguracionColumna("NombreCliente",{ groupable: true})
						          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
								  ,obj.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
								  ,obj.getConfiguracionColumna("FolioCartera")
								  ,obj.getConfiguracionColumna("NumeroCuotas")
								  ,obj.getConfiguracionColumna("MontoMonedaSistema")
						]
					}
			};
			return config;
		};
		
		obj.getConsultarFlujoCustodia = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CUSTODIA'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:350,
				
						dataSource: obj.getDataSourceConsultarFlujoCustodia(),
					
						columns: [
								 //  obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						         // ,obj.getConfiguracionColumna("CodigoPortafolio",{ groupable: true})
								    obj.getConfiguracionColumna("Nemotecnico",{ groupable: true})
								  , obj.getConfiguracionColumna("CantidadDisponible",{ groupable: true})
								  ,obj.getConfiguracionColumna("MontoDisponible",{ groupable: true})
								 // ,obj.getConfiguracionColumna("MontoDisponibleCustodia",{ groupable: true})
						]
					}
			};
			
			return config;
		};
		
		
		
		
		obj.rpad = function(str, padString, length) {
		    while (str.length < length)
		        str = str + padString;
		    return str;
		}
		
		obj.generarPdf417 = function(parametros) {
			return parametros.DocumentoIdentidad + " " +
				   parametros.CodigoPortafolio + " " +
				   obj.removerCaracteresEspeciales(parametros.NombreCliente) + " " +
				   parametros.SecuenciaOperacion + " " +
				   parametros.FechaLiquidacion + " " +
				   parametros.FondoMutuo + " " +
				   parametros.IdEmisorOrigen + " " +
				   parametros.MontoInicial + " " +
				   parametros.NumeroCuotas + " " +
				   parametros.IdSerieFondoDestino + " " +
				   parametros.IdEmisorDestino + " " +
				   parametros.MontoTransferencia + " " +
				   parametros.NumeroCuotasTransferencia + " " +
				   parametros.FechaCertificacion + " " +
				   parametros.IndicadorMLT;
		}
		
		/*obj.generarPdf417 = function(parametros) {
			return obj.rpad(parametros.DocumentoIdentidad," ",15) +
				   obj.rpad(parametros.CodigoPortafolio," ",2) +
				   obj.rpad(parametros.NombreCliente," ",100) +
				   obj.rpad(parametros.SecuenciaOperacion," ",30) +
				   obj.rpad(parametros.FechaLiquidacion," ",20) +
				   obj.rpad(parametros.FondoMutuo," ",50) +
				   obj.rpad(parametros.IdEmisorOrigen," ",50) +
				   obj.rpad(parametros.MontoInicial," ",50) +
				   obj.rpad(parametros.NumeroCuotas," ",50) +
				   obj.rpad(parametros.IdSerieFondoDestino," ",50) +
				   obj.rpad(parametros.IdEmisorDestino," ",50) +
				   obj.rpad(parametros.MontoTransferencia," ",50) +
				   obj.rpad(parametros.NumeroCuotasTransferencia," ",50) +
				   obj.rpad(parametros.FechaCertificacion," ",20) +
				   obj.rpad(parametros.IndicadorMLT," ",4);
		}*/
		
		obj.removerCaracteresEspeciales = function(texto) {
			let normalizado = texto.normalize('NFD').replace(/[^a-zA-Z0-9]/g, '');
			return normalizado;
		}
		
		obj.getFondosPortafolioAdministracionLiquidez = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'PORTAFOLIOFONDOSLIQUIDEZ'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:450,
				
						dataSource: obj.getDataSourceFondosPortafolioAdministracionLiquidez(),
					
						columns: [
						           obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						          ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
						          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
						          ,obj.getConfiguracionColumna("MontoMinimo",{ groupable: true})
						          ,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
						]
					}
			};
			return config;
		};
		
		obj.getFondosPortafolioAdministracionLiquidez = function() {
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'PORTAFOLIOFONDOSLIQUIDEZ'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:450,
				
						dataSource: obj.getDataSourceFondosPortafolioAdministracionLiquidez(),
					
						columns: [
						           obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						          ,obj.getConfiguracionColumna("NombreCompleto",{ groupable: true})
						          ,obj.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
						          ,obj.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
						          ,obj.getConfiguracionColumna("MontoMinimo",{ groupable: true})
						          ,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
						]
					}
			};
			return config;
		};
		
		obj.confConfiguracionRegimenFondoMutuo = function(dataItem)
		  {
			 var filtrosConfiguracionRegimenFondoMutuo = [{field: "IdRegimenFondoMutuoOrigen", operator: "eq", value: dataItem.IdRegimenFondoMutuo}];
			 return {
				  dataSource		: obj.getDataSourceConfiguracionRegimenFondoMutuo(filtrosConfiguracionRegimenFondoMutuo),
				  columns: [
			           obj.getConfiguracionColumna("RegimenFondoMutuoDestino")
			      ],
			      sortable			: true,
			      pageable			: true,
			      height			: 200,
			      userSettings      : true,
			      selectable 		: "multiple row",
			      autoBind          : true,
				  navigatable         : true,
					allowCopy 			: true
			 };
		};
		
		obj.confConfiguracionPropositoPortafolio = function(dataItem)
		  {
			 var filtrosConfiguracionPropositoPortafolio = [{field: "IdRegimenFondoMutuo", operator: "eq", value: dataItem.IdRegimenFondoMutuo}];
			 return {
				  dataSource		: obj.getDataSourceConfiguracionPropositoPortafolio(filtrosConfiguracionPropositoPortafolio),
				  columns: [
			           obj.getConfiguracionColumna("PropositoDestino")
			      ],
			      sortable			: true,
			      pageable			: true,
			      height			: 200,
			      userSettings      : true,
			      selectable 		: "multiple row",
			      autoBind          : true,
				  navigatable       : true,
				  allowCopy 		: true
			 };
		};
		
		
		obj.getDistribuicionInversion = function(){
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'DI'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: false,
			      		scrollable: true,
			      		height:450,
				
						dataSource: new bcs.DataSource({ 
							type : "odata",
							definition : {
								serverFiltering : true, 
								serverPaging : true, 
								serverSorting : true, 
								sort: { field: "RazonSocial", dir: "desc" },
								pageSize : 200,
								transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600079",
										dataType: "json"
									}
								},
								schema : {
									model : {
										fields : {
											RazonSocial					: { type : "string"},
											Serie						: { type : "code"},
											Monto						: { type : "number"},
											Porcentaje					: { type : "number"},
										}
									}
								}
							},
							
						}),
					
						columns: [
						           obj.getConfiguracionColumna("RazonSocial")
						          ,obj.getConfiguracionColumna("Serie")
						          ,obj.getConfiguracionColumna("Monto")
						          ,obj.getConfiguracionColumna("Porcentaje")
						          
	   
						]
					}
			};
			return config;
		};
		
		obj.getSchemaInversiones = function(){
			return {
				model : {
					fields : {
						NombreFondo			 : { type : "string"},
						Serie		 		 : { type : "string"},
						Monto		     	 : { type : "number"},
						Regimen		     	 : { type : "string"},
						Porcentaje		     : { type : "number"},
						
					}
				}
			}
		};
		
		obj.getDataSourceInversionesArray = function () {
			return new bcs.DataSource({
				type : "array",
				definition : {
					sort: { field: "RazonSocial", dir: "asc" },
					schema : obj.getSchemaInversiones()			
				}
			})
		};
		
		obj.getInversionesArray = function(){
			var config = {};
			config = {
					options : {
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: false,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:150,
		
						dataSource: obj.getDataSourceInversionesArray(),
					
						columns: [
						           obj.getConfiguracionColumna("RazonSocial")
						          ,obj.getConfiguracionColumna("Serie")
						          ,obj.getConfiguracionColumna("Monto")
						          ,obj.getConfiguracionColumna("Regimen")
						          ,obj.getConfiguracionColumna("PorcentajeCSC")
						]
					}
			};
			return config;
		};
		
		obj.getDataSourceDetalleCSC = {
				type : "odata",
				definition : {
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					serverPaging 		: true, // Paginado de los datos en el servidor.
					serverSorting 		: false, // Ordenamiento de los datos en el servidor.
					sort 				: { field: "RazonSocial", dir: "asc" }, 
					pageSize 			: 5,								
					transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=600086",dataType : "json"}},
					schema : {
						model : {
							fields : {								
								NombreSerieFondo              	   	: { type: "string"},
								IdSerieFondo           				: { type: "string"},   
								Monto       						: { type: "number"},					 
								Porcentaje							: { type: "number"},
								Regimen								: { type: "string"},
															
							}
						}
					}	
				}
		};
		
		//grilla blotter general
		obj.columnasDetalleCSC = function() {
			return  [
				  {field : "NombreSerieFondo",     	 title:'UI_RAZON_SOCIAL', 	width :"150px"}
				 ,{field : "IdSerieFondo", 		     title:'UI_SERIE', 		width :"80px"}
				 ,{field : "Monto", 		         title:"UI_MONTO", 		width: "60px"}
				 ,{field : "Porcentaje",     		 title:'UI_PORCENTAJE', 	width :"50px"}
				 ,{field : "Regimen",   			 title:'UI_REGIMEN', 	     width :"50px"}
				 
				 
			];
		};
			
		obj.getDataSourceMetodosDePagoCSC = function(){
			return new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					serverPaging 		: true, // Paginado de los datos en el servidor.
					serverSorting		: true,
					sort 				: { field: "Orden", dir: "asc" }, 
					pageSize 			: 5,								
					transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=600088",dataType : "json"}},
					schema : {
						model : {
							fields : {								
								IdCSCPago              	   		: { type: "string"},
								IdMedioDePago          			: { type: "string"},   
								IdEstado      					: { type: "string"},					 
								Nombre							: { type: "string"}, 
								Orden							: { type: "number"}							
							}
						}
					}	
				}
			})
		};
		
		obj.getConsultaCSC = function(){
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CSC'));
					},
					options : {
						userSettings:	true,
						sortable: 		true,
			      		reorderable: 	true,
			      		filterable: 	true,
						groupable: 		true,
						pageable: 		true,
						selectable: 	"multiple, row",
						reorderable: 	true,
						allowCopy: 		true,
						resizable: 		true,
			      		autoBind: 		true,
			      		scrollable: 	true,
			      		height:450,
				
						dataSource: new bcs.DataSource({ 
							type : "odata",
							definition : {
								serverFiltering : true, 
								serverPaging : true, 
								serverSorting : true, 
								sort: { field: "IdFolioCSC", dir: "desc" },
								pageSize : 200,
								transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600079",
										dataType: "json"
									}
								},
								schema : {
									model : {
										fields : {
											IdFolioCSC					: { type : "number"},
											DocumentoIdentidad			: { type : "string"},
											CodigoPortafolio			: { type : "number"},
											NombreCompleto				: { type : "string"},
											Fecha						: { type : "sysdate"},
											MontoPesos					: { type : "number"},
											MontoUF						: { type : "number"},
											Nombre						: { type : "string"},
											NombreBanco					: { type : "string"},
											Estado						: { type : "string"},
											FolioCSC					: { type : "string"},
											FolioRelacionado			: { type : "string"},
											EstadoPAC					: { type : "string"},
											Canal						: { type : "string"},
											DescripcionCanal			: { type : "string"},
											IndicadorMLT				: { type : "string"},
											Monto            			: { type : "number"},
											IdMoneda          			: { type : "string"}
										}	
									}
								}
							},
							
						}),
					
						columns: [
						           obj.getConfiguracionColumna("IdFolioCSC")
						          ,obj.getConfiguracionColumna("DocumentoIdentidad")
						          ,obj.getConfiguracionColumna("CodigoPortafolio")
						          ,obj.getConfiguracionColumna("NombreCompleto")
						          ,obj.getConfiguracionColumna("Fecha")
						          ,obj.getConfiguracionColumna("MontoMonedaOrigen")
						          ,obj.getConfiguracionColumna("Moneda")
						          ,obj.getConfiguracionColumna("Nombre")
						          ,obj.getConfiguracionColumna("NombreBanco")
						          ,obj.getConfiguracionColumna("Estado")
						          ,obj.getConfiguracionColumna("FolioCSC")
						          ,obj.getConfiguracionColumna("FolioRelacionado")
						          ,obj.getConfiguracionColumna("EstadoPAC")
						          ,obj.getConfiguracionColumna("Canal")
						          ,obj.getConfiguracionColumna("DescripcionCanal")						          
						          ,obj.getConfiguracionColumna("IndicadorMLT")						          
						]
					}
			};
			return config;
		};
		
	
		
		obj.getDataSourceDetalleCSCEditar = function(){
			var config = {};
			config = {
					buscar : function(apiGrid,filtros) {
						apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CSC'));
					},
					options : {
						userSettings:true,
						sortable: true,
			      		reorderable: true,
			      		filterable: true,
						groupable: true,
						pageable: true,
						selectable: "multiple, row",
						reorderable: true,
						allowCopy: true,
						resizable: true,
			      		autoBind: true,
			      		scrollable: true,
			      		height:450,
				
						dataSource: new bcs.DataSource({ 
							type : "odata",
							definition : {
								serverFiltering : true, 
								serverPaging : true, 
								serverSorting : true, 
								sort : { field: "RazonSocial", dir: "asc" }, 
								pageSize : 200,
								transport: {
									read: {
										url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600086",
										dataType: "json"
									}
								},
								filter: { field: "IdFolioCSC", operator: "eq",  value: 150},
								schema : {
									model : {
										fields : {								
											NombreSerieFondo              	   	: { type: "string"},
											NombreFondo           				: { type: "string"},   
											Monto       						: { type: "number"},					 
											Porcentaje							: { type: "number"},
											Regimen								: { type: "string"},
											

											
										}
											
											
										}
									}
								
							},
							
						}),
					
						columns: [
						           obj.getConfiguracionColumna("NombreSerieFondo")
						          ,obj.getConfiguracionColumna("NombreFondo")
						          ,obj.getConfiguracionColumna("Monto")
						          ,obj.getConfiguracionColumna("Porcentaje")
						          ,obj.getConfiguracionColumna("Regimen")

	   
						]
					}
			};
			return config;
		};
		
	obj.getConsultaValoresCuotaVigente = function(){
		var config = {};
		config = {
				buscar : function(apiGrid, filtros){
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'VALORCUOTAVIGENTE'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							pageSize : 200,
							sort: { field: "FechaVigencia", dir: "desc" },
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700179",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdSerieFondo	     		 : { type : "string"},
										FechaVigencia   	 		 : { type : "date"},
										ValorCuota		 			 : { type : "number"},
										IdMoneda   					 : { type : "string"},
										Observacion					 : { type : "string"},
										FechaIngresoRegistro      	 : { type : "sysdatetime"},
										DecimalesValorCuota			 : { type : "number"},
										IdEmpresa 					 : { type : "number"},
										CodigoSerie					 : { type : "string"},
										IdFamiliaFondo				 : { type : "string"},
										IdInstrumento				 : { type : "number"},
										NombreSerieFondo			 : { type : "string"},
										Nemotecnico					 : { type : "string"},
										IdEmisor					 : { type : "string"},
										NombreFondo					 : { type : "string"}
									}
								}
							}
						},
						
					}),
					columns: [
					           obj.getConfiguracionColumna("IdSerieFondo")
					          ,obj.getConfiguracionColumna("NombreSerieFondo")
					          ,obj.getConfiguracionColumna("NombreFondo")
					          ,obj.getConfiguracionColumna("FechaVigencia")
					          ,obj.getConfiguracionColumna("ValorCuota")
					          ,obj.getConfiguracionColumna("IdMoneda")
					]
				}
		};
		return config;
	}
	
	obj.getHistorialCambiosModificarFechas = function(){
		var config = {};
		config = {
					buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'SOLICITUD'));
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					selectable: "multiple, row",
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: new bcs.DataSource({ 
						type : "odata",
						definition : {
							serverFiltering : true, 
							serverPaging : true, 
							serverSorting : true, 
							pageSize : 200,
							sort: { field: "IdSolicitudAporteRescate", dir: "desc" },
							transport: {
								read: {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700402",
									dataType: "json"
								}
							},
							schema : {
								model : {
									fields : {
										IdSolicitudAporteRescate		: { type : "number"},
										FechaAceptacion					: { type : "date"},
										FechaCertificacion				: { type : "date"},
										FechaLiquidacion			    : { type : "date"},
										FechaValorCuota	     			: { type : "date"},
										FechaAceptacionAnterior			: { type : "date"},
										FechaCertificacionAnterior		: { type : "date"},
										FechaLiquidacionAnterior    	: { type : "date"},
										FechaValorCuotaAnterior	    	: { type : "date"},
										UsuarioModificador			    : { type : "string"},
										UsuarioAutorizador				: { type : "string"}
												
									}								
								}
							}
						},
						
					}),
						columns: [
					           obj.getConfiguracionColumna("IdSolicitudAporteRescate")
					          ,obj.getConfiguracionColumna("FechaAceptacion")
					          ,obj.getConfiguracionColumna("FechaCertificacion")
					          ,obj.getConfiguracionColumna("FechaLiquidacion")
					          ,obj.getConfiguracionColumna("FechaValorCuota")
					          ,obj.getConfiguracionColumna("FechaAceptacionAnterior")
					          ,obj.getConfiguracionColumna("FechaCertificacionAnterior")
					          ,obj.getConfiguracionColumna("FechaLiquidacionAnterior")
					          ,obj.getConfiguracionColumna("FechaValorCuotaAnterior")
					          ,obj.getConfiguracionColumna("UsuarioModificador")
					          ,obj.getConfiguracionColumna("UsuarioAutorizador")
					]
				}
		};
		return config;
	}

	obj.asignarRegimenPorDefecto = function (portafolioSeleccionado, datosFondo, formData) {
		if (portafolioSeleccionado && portafolioSeleccionado.CodigoPortafolio.length > 0
			&& datosFondo && datosFondo.IdSerieFondo && datosFondo.IdSerieFondo.length > 0) {
				
				formData.IdRegimenFondoMutuo = portafolioSeleccionado.CodigoPortafolio;
				formData.RegimenDefecto = formData.IdRegimenFondoMutuo;
		}
	};
		
		
	return obj;
	
	
});