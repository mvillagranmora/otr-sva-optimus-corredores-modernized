angular.module('ModuloNormativa').factory('bcsConfiguracionMaestroDeclaraciones',function($translate,$rootScope,bcsComunes,$timeout,$http) {
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

        console.log(filtros);

        if ('fechaPago' in filtros) {
            filters.push({ field: "FechaPago", operator: "ge", value: `${filtros.fechaPago}-01-01`});
            filters.push({ field: "FechaPago", operator: "le", value: `${filtros.fechaPago}-12-31`});
        }
		if (filtros.esquema)
			filters.push({ field: "Esquema", operator: "eq", value: filtros.esquema});
		if (filtros.modulo)
			filters.push({ field: "Modulo", operator: "eq", value: filtros.modulo});
		if (filtros.version)
			filters.push({ field: "Version", operator: "eq", value: filtros.version});
		if (filtros.agnotributario)
			filters.push({ field: "AgnoTributario", operator: "eq", value: filtros.agnotributario});
		if (filtros.Anio)
			filters.push({ field: "AnioOrigen", operator: "eq", value: filtros.Anio});
		if (filtros.IdClasificacionTipoInstrumento)
			filters.push({ field: "Mercado", operator: "eq", value: filtros.IdClasificacionTipoInstrumento});
		if (filtros.instrumento)
			filters.push({ field: "IdInstrumento", operator: "eq", value: filtros.instrumento.IdInstrumento});
        if (filtros.PeriodoComercial)
			filters.push({ field: "Ejercicio", operator: "eq", value: (filtros.PeriodoComercial) });
		if (filtros.IdOrigen) {
            if (filtros.IdOrigen == 'COR' && !filtros.esCalificacionPendiente) {
                console.log('Entro como COR, se asigna CORREDOR');
                filters.push({ field: "Origen", operator: "eq", value: 'CORREDOR' });
            } else if (filtros.IdOrigen == 'BCS' && !filtros.esCalificacionPendiente) {
                filters.push({ field: "Origen", operator: "eq", value: 'BOLSA' });
            } else {
			    filters.push({ field: "IdOrigen", operator: "eq", value: filtros.IdOrigen });
            }
        }
        if('esCalificacionPendiente' in filtros) {
            if (filtros.esCalificacionPendiente) {
                console.log('calificacion pendiente');
                filters.push({ field: "TipoCalificacion", operator: "eq", value: 'PENDIENTE' });
            } else {
                console.log('calificacion ingresada');
                filters.push({ field: "TipoCalificacion", operator: "eq", value: 'INGRESADA' });
            }
        }

        console.log('dsps' ,filters);

		return filters;
	}
	
	var configuracionColumnas = function() {
		return { 
			Esquema: 			  		{field: "Esquema",			  title: 	$translate.instant('UI_ESQUEMA'), 			    width: "100px",	align: "left"},
			Objetivo:			  		{field: "Objetivo", 		  title:	$translate.instant('UI_DESCRIPCION'), 		    width: "100px",	align: "left"},
			Version_Activa: 	  		{field: "Version",			  title:	$translate.instant('UI_VERSION_ACTIVA'),	    width: "100px", align: "left"},
			Estado_Documento:	  		{field: "NombreEstado",		  title:	$translate.instant('UI_ESTADO_DOCUMENTO'),	    width: "100px", align: "left"},
			Flujo_Proceso:		  		{field: "FlujoProceso",		  title:	$translate.instant('UI_FLUJO_PROCESO'),		    width: "100px", align: "left"},
			Version:			  		{field: "Version",			  title:    $translate.instant('UI_VERSION'),			    width: "100px", align: "left"},
			Carga:				  		{field: "Carga",			  title:    $translate.instant('UI_CARGA'),				    width: "100px", align: "left"},
			Validacion:			  		{field: "Validacion",		  title:    $translate.instant('UI_VALIDACION'),		    width: "100px", align: "left"},
			PreDeclaracion:		  		{field: "PreDeclaracion",	  title:    $translate.instant('UI_PREDECLARACION'),	    width: "100px", align: "left"},
			Certificacion:		  		{field: "Certificacion",	  title:	$translate.instant('UI_CERTIFICACION'),		    width: "100px", align: "left"},
			IdFondo:   		      		{field: "IdFondo",	          title:	$translate.instant('UI_FONDO'),		            width: "100px", align: "left"},
			PorcentajeAccionario: 		{field: "PorcentajeAccionario", title:	$translate.instant('UI_PORCENTAJE_ACCIONARIO'),	width: "100px", align: "left"},
			Observacion:		  		{field: "Observacion",	      title:	$translate.instant('UI_OBSERVACION'),		    width: "100px", align: "left"},
			IdEstado:		      		{field: "IdEstado",	          title:	$translate.instant('UI_ESTADO'),		        width: "100px", align: "left"},
			Valor:		          		{field: "Valor",	              title:	$translate.instant('UI_VALOR'),		            width: "100px", align: "left"},
			Agno:		          		{field: "Agno",	              title:	$translate.instant('UI_ANIO'),		        	width: "100px", align: "left"},
			DescMes:		      		{field: "DescMes",	          title:	$translate.instant('UI_MES'),		        	width: "100px", align: "left"},
			Mes:		          		{field: "Mes",	              title:	$translate.instant('UI_MES'),		        	width: "100px", align: "left"},
			Fecha:		          		{field: "Fecha",	              title:	$translate.instant('UI_FECHA'),		        	width: "100px", align: "left"},
			AgnoTributario:		  		{field: "AgnoTributario",	      title:	$translate.instant('UI_ANIO_TRIBUTARIO'),		width: "100px", align: "left"},
			ValorIpc:		      		{field: "ValorIpc",	          title:	$translate.instant('UI_VALOR'),		            width: "100px", align: "left"},
			IdCliente:		      		{field: "IdCliente",	          title:	$translate.instant('UI_ID_CLIENTE'),		    width: "100px", align: "left"},
			IdCalificacionTributaria:   {field: "IdCalificacionTributaria",	      title:	$translate.instant('UI_CALIFICACION_TRIBUTARIA'),        width: "100px", align: "left"},
			Mercado:					{field: "Mercado",	      title:	$translate.instant('UI_MERCADO'),        width: "100px", align: "left"},
			Nemotecnico:		  		{field: "Nemotecnico",	      title:	$translate.instant('UI_INSTRUMENTO'),        width: "100px", align: "left"},
			ValorHistorico:		  		{field: "ValorHistorico",	      title:	$translate.instant('UI_VALOR_HISTORICO'),        width: "100px", align: "left"},
			FechaPago:		  			{field: "FechaPago",	      title:	$translate.instant('UI_FECHA_PAGO'),        width: "100px", align: "left"},
			IdEventoCapital:		    {field: "IdEventoCapital",	      title:	$translate.instant('UI_EVENTO_CAPITAL'),        width: "100px", align: "left"},
			NumeroDividendo:		    {field: "NumeroDividendo",	      title:	$translate.instant('UI_NUMERO_DIVIDENDO'),        width: "100px", align: "left"},
			Descripcion: 				{field: "Descripcion", title: $translate.instant('UI_DESCRIPCION'), width: "200px", align: "left" },
			SecuenciaEvento:		    {field: "SecuenciaEvento",	      title:	$translate.instant('UI_SECUENCIA_EVENTO'),        width: "100px", align: "left"},
			AnioOrigen:		  			{field: "AnioOrigen",	      title:	$translate.instant('UI_ANIOS_ORIGEN'),        width: "100px", align: "left"},
			TasaCredito:		 		{field: "TasaCredito",	      title:	$translate.instant('UI_TASA_CREDITO'),        width: "100px", align: "left"},
			PorcentajeDividendo:		{field: "PorcentajeDividendo",	      title:	$translate.instant('UI_PORCENTAJE_DIVIDENDO'),        width: "100px", align: "left"},
			TipoUtilidad:		        {field: "TipoUtilidad",	      title:	$translate.instant('UI_TIPO_DIVIDENDO'),        width: "100px", align: "left"},
			Factor08:		  			{field: "Factor08",	     		title:	$translate.instant('UI_FACTOR_08'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_08')}},
			Factor09:		  			{field: "Factor09",	      		title:	$translate.instant('UI_FACTOR_09'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_09')}},
			Factor10:		  			{field: "Factor10",	      		title:	$translate.instant('UI_FACTOR_10'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_10')}},
			Factor11:		  			{field: "Factor11",	     		title:	$translate.instant('UI_FACTOR_11'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_11')}},
			Factor12:		  			{field: "Factor12",	      		title:	$translate.instant('UI_FACTOR_12'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_12')}},
			Factor13:		  			{field: "Factor13",	      		title:	$translate.instant('UI_FACTOR_13'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_13')}},			
			Factor14:		  			{field: "Factor14",	      		title:	$translate.instant('UI_FACTOR_14'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_14')}},
			Factor15:		  			{field: "Factor15",	      		title:	$translate.instant('UI_FACTOR_15'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_15')}},
			Factor16:		  			{field: "Factor16",	      		title:	$translate.instant('UI_FACTOR_16'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_16')}},
			Factor17:		  			{field: "Factor17",	      		title:	$translate.instant('UI_FACTOR_17'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_17')}},		
			Factor18:		  			{field: "Factor18",	      		title:	$translate.instant('UI_FACTOR_18'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_18')}},
			Factor19:		  			{field: "Factor19",	      		title:	$translate.instant('UI_FACTOR_19'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_19')}},
			Factor20:		  			{field: "Factor20",	      		title:	$translate.instant('UI_FACTOR_20'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_20')}},
			Factor21:		  			{field: "Factor21",	      		title:	$translate.instant('UI_FACTOR_21'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_21')}},
			Factor22:		  			{field: "Factor22",	      		title:	$translate.instant('UI_FACTOR_22'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_22')}},
			Factor23:		  			{field: "Factor23",	      		title:	$translate.instant('UI_FACTOR_23'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_23')}},
			Factor24:		  			{field: "Factor24",	      		title:	$translate.instant('UI_FACTOR_24'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_24')}},
			Factor25:		  			{field: "Factor25",	      		title:	$translate.instant('UI_FACTOR_25'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_25')}},
			Factor26:		  			{field: "Factor26",	      		title:	$translate.instant('UI_FACTOR_26'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_26')}},
			Factor27:		  			{field: "Factor27",	      		title:	$translate.instant('UI_FACTOR_27'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_27')}},
			Factor28:		  			{field: "Factor28",	      		title:	$translate.instant('UI_FACTOR_28'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_28')}},
			Factor29:		  			{field: "Factor29",	      		title:	$translate.instant('UI_FACTOR_29'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_29')}},
			Factor30:		  			{field: "Factor30",	      		title:	$translate.instant('UI_FACTOR_30'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_30')}},
			Factor31:		  			{field: "Factor31",	      		title:	$translate.instant('UI_FACTOR_31'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_31')}},
			Factor32:		  			{field: "Factor32",	      		title:	$translate.instant('UI_FACTOR_32'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_32')}},
			Factor33:		  			{field: "Factor33",	      		title:	$translate.instant('UI_FACTOR_33'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_33')}},
			Factor34:		  			{field: "Factor34",	      		title:	$translate.instant('UI_FACTOR_34'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_34')}},
			Factor35:		  			{field: "Factor35",	      		title:	$translate.instant('UI_FACTOR_35'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_35')}},
			Factor36:		  			{field: "Factor36",	      		title:	$translate.instant('UI_FACTOR_36'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_36')}},
			Factor37:		  			{field: "Factor37",	      		title:	$translate.instant('UI_FACTOR_37'),        width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_37')}},
			Factor38:		  			{field: "Factor38",	      		title:	$translate.instant('UI_FACTOR_19B'),       width: "100px", align: "left",attributes:{title:	$translate.instant('FACTOR_38')}},
			Ejercicio:		  			{field: "Ejercicio",	      		title:	$translate.instant('UI_EJERCICIO'),        width: "100px", align: "left"},
			FactorAct:		  			{field: "FactorAct",	      		title:	$translate.instant('UI_FACTOR_ACT'),        width: "100px", align: "left"},
			Origen: 					{ field: "Origen", title: $translate.instant('UI_ORIGEN'), width: "100px", align: "left" },
			IdOrigen: 					{ field: "IdOrigen", title: $translate.instant('UI_ORIGEN'), width: "100px", align: "left" },
			TipoCalificacion: 			{ field: "TipoCalificacion", title: $translate.instant('UI_TIPO'), width: "100px", align: "left" },
			AcogidoISFUT:		  			{field: "AcogidoISFUT",	      		title:	$translate.instant('UI_ACOGIDO_ISFUT'),        width: "100px", align: "left"},
			TipoRegistro:				{field: "TipoRegistro",	        title:	$translate.instant('UI_TIPO_REGISTRO'),        width: "100px", align: "left"},
			NombreCliente:				{field: "NombreCliente",	        title:	$translate.instant('UI_NOMBRE_CLIENTE'),        width: "100px", align: "left"},
			RutSociedad:				{field: "RutSociedad",	        title:	$translate.instant('UI_RUT_SOCIEDAD'),        width: "100px", align: "left"},
			IdEventoCapitalCliente:		{field: "IdEventoCapitalCliente",	        title:	$translate.instant('UI_ID_EVENTO_CAPITAL'),        width: "100px", align: "left"},
			RazonSocial:				{field: "RazonSocial",	        title:	$translate.instant('UI_RAZON_SOCIAL'),        width: "100px", align: "left"},
			IdInstrumento:				{field: "IdInstrumento",	        title:	$translate.instant('UI_INSTRUMENTO'),        width: "100px", align: "left"},
			Nemo:		  				{field: "Nemo",	      title:	$translate.instant('UI_INSTRUMENTO'),        width: "100px", align: "left"},
            //DJ1899
			Numero:						{field: "Numero",				title:	$translate.instant('UI_NUMERO'),			width: "100px", align: "rigth"},
			IdMoneda:					{field: "IdMoneda",				title:	$translate.instant('UI_MONEDA'),			width: "50px", align: "left"},
			RutCliente:					{field: "RutCliente",			title:  $translate.instant('UI_RUT_CLIENTE'),		width: "90px", align: "left"},
			ActivoPensionado:			{field: "ActivoPensionado",title: $translate.instant('UI_ACTIVO_PENSIONADO'),		width: "90px", align: "rigth"},
			RutEmpleador:				{field: "RutEmpleador",			title: $translate.instant('UI_RUT_EMPLEADOR'),		width: "90px", align: "left"},
			FechaOperacion:				{field: "FechaOperacion",		title: $translate.instant('UI_FECHA_OPERACION'),	width: "100px", align: "center"},
			DepositoEmpleador:			{field: "DepositoEmpleador",	title:	$translate.instant('UI_DEPOSITO_EMPLEADOR'),width: "100px", align: "rigth"},
			AporteEmpleador:			{field: "AporteEmpleador",		title:	$translate.instant('UI_APORTE_EMPLEADOR'),	width: "100px", align: "rigth"},
			TraspasoEmpleador:			{field: "TraspasoEmpleador",	title:	$translate.instant('UI_TRASPASO_EMPLEADOR'),	width: "100px", align: "rigth"},
			RetiroEmpleadorActualizado:	{field: "RetiroEmpleadorActualizado", title:	$translate.instant('UI_RETIRO_EMPLEADOR_ACTUALIZADO'),width: "100px", align: "rigth"},
			MontoRetencionActualizado:	{field: "MontoRetencionActualizado", title:	$translate.instant('UI_MONTO_RETENCION_ACTUALIZADO'),width: "100px", align: "rigth"},
			AhorrosModalidadIndirectaEmpleador: {field: "AhorrosModalidadIndirectaEmpleador", title:	$translate.instant('UI_AHORROS_MODALIDAD_INDIRECTA_EMPLEADOR'), width: "100px", align: "rigth"},
			AhorrosModalidadDirectaTrabajador: {field: "AhorrosModalidadDirectaTrabajador", title:	$translate.instant('UI_AHORROS_MODALIDAD_DIRECTA_TRABAJADOR'),width: "100px", align: "rigth"},
			CotizacionesModalidadIndirectaEmpleador: {field: "CotizacionesModalidadIndirectaEmpleador", title: $translate.instant('UI_COTIZACIONES_MODALIDAD_INDIRECTA_EMPLEADOR'),width: "100px", align: "rigth"},
			CotizacionesModalidadDirectaTrabajador: {field: "CotizacionesModalidadDirectaTrabajador", title: $translate.instant('UI_COTIZACIONES_MODALIDAD_DIRECTA_TRABAJADOR'),width: "100px", align: "rigth"},
			CotizacionesTrabajadorIndependiente: {field: "CotizacionesTrabajadorIndependiente", title:	$translate.instant('UI_COTIZACIONES_TRABAJADOR_INDEPENDIENTE'),width: "100px", align: "rigth"},
			MontoRetiroPorAhorrosIncisoPrimero: {field: "MontoRetiroPorAhorrosIncisoPrimero", title: $translate.instant('UI_MONTO_RETIRO_AHORROS_INCISO_PRIMERO'),width: "100px", align: "rigth"},
			RetencionImpuestoSobreRetiros: {field: "RetencionImpuestoSobreRetiros", title: $translate.instant('UI_RETENCION_IMPUESTO_SOBRE_RETIROS'),width: "100px", align: "rigth"},
			MontoRetiroPorAhorrosIncisoSegundo: {field: "MontoRetiroPorAhorrosIncisoSegundo", title:	$translate.instant('UI_MONTO_RETIRO_AHORROS_INCISO_SEGUNDO'),width: "100px", align: "rigth"}
		}
	};
    
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.getSchemaEsquema = function() {
		return { 
			model: {
				fields : {
					Esquema 		 	 		 : 	{ type : "string"},
					Version 				 	 : 	{ type : "string"},
					Modulo 	  		     	 	 : 	{ type : "string"},
					Periocidad 	 	 			 : 	{ type : "string"},
					Objetivo   	     	 		 : 	{ type : "string"},
					PeriodoInicio   	     	 : 	{ type : "date"},
					IdEntidadReguladora 	 	 : 	{ type : "number"},
					FechaPresentacionLimite   	 : 	{ type : "date"},
					FechaDatosDesde   		 	 : 	{ type : "date"},
					FechaDatosHasta				 : 	{ type : "date"},
					CodigoVersionSII			 :	{ type : "number"},
					HabilitadoMantenedor		 :	{ type : "string"},
					IdEstadoDocumento			 :	{ type : "string"},
					NombreEstado				 :  { type : "string"},
					FlujoProceso				 :	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaCalificacionTributaria = function() {
		return { 
			model: {
				fields : {
					Ejercicio			 	 :	{ type : "number"},
					Mercado					 :	{ type : "string"},
					Nemo					 :	{ type : "string"},
					SecuenciaVariacion		 :	{ type : "number"},
					DescripcionVariacion	 :  { type : "string"},
					FactorAccionesAntiguas	 :	{ type : "number"},
					FactorAccionesNuevas	 :	{ type : "number"},
					FechaVariacion			 :	{ type : "sysdate"},
					FechaLimite				 :	{ type : "sysdate"},
					FechaPago				 :	{ type : "sysdate"},
					AnioVariacion			 :	{ type : "number"},
					NemoSegundoInstrumento	 :	{ type : "string"},
					NumeroAccionesAntiguas	 :	{ type : "number"},
					NumeroAccionesDerecho	 :	{ type : "number"},
					NumeroAccionesNuevas	 :	{ type : "number"},
					NumeroDividendo			 :	{ type : "number"},
					PrecioVariacion			 :	{ type : "number"},
					Precio					 :	{ type : "number"},
					TipoVariacion			 :	{ type : "string"},
					TipoDividendo			 :	{ type : "string"},
					ValorAccion				 :	{ type : "number"},
					ValorAvision			 :	{ type : "number"},
					ValorEion				 : 	{ type : "number"},
					ValorLAjustado			 :	{ type : "number"},
					Vigencia				 :	{ type : "string"},
					IndicadorProcesada		 :	{ type : "string"},
					Moneda					 :	{ type : "string"},
					IdCalificacionTributaria : 	{ type : "number"},
					IdClasificacionTipoInstrumento 		 : 	{ type : "string"},
					IdInstrumento 	  		 : 	{ type : "number"},
					ValorHistorico 	 	 	 : 	{ type : "number"},
					FechaPago   	     	 : 	{ type : "date"},
					IdEventoCapital   	     : 	{ type : "number"},
					Descripcion 	 	 	 : 	{ type : "string"},
					SecuenciaEvento   	 	 : 	{ type : "number"},
					AnioOrigen   		 	 : 	{ type : "string"},
					TasaCredito				 : 	{ type : "number"},
					PorcentajeDividendo		 :	{ type : "number"},
					IdTipoUtilidades		 :	{ type : "string"},
					Factor08				 :	{ type : "string"},
					Factor09				 :  { type : "string"},
					Factor10				 :  { type : "string"},
					Factor11				 :  { type : "string"},
					Factor12				 :  { type : "string"},
					Factor13				 :  { type : "string"},
					Factor14				 :  { type : "string"},
					Factor15				 :  { type : "string"},
					Factor16				 :  { type : "string"},
					Factor17				 :  { type : "string"},
					Factor18				 :  { type : "string"},
					Factor19				 :  { type : "string"},
					Factor20				 :  { type : "string"},
					Factor21				 :  { type : "string"},
					Factor22				 :  { type : "string"},
					Factor23				 :  { type : "string"},
					Factor24				 :  { type : "string"},
					Factor25				 :  { type : "string"},
					Factor26				 :  { type : "string"},
					Factor27				 :  { type : "string"},
					Factor28				 :  { type : "string"},
					Factor29				 :  { type : "string"},
					Factor30				 :  { type : "string"},
					Factor31				 :  { type : "string"},
					Factor32				 :  { type : "string"},
					Factor33				 :  { type : "string"},
					Factor34				 :  { type : "string"},
					Factor35				 :  { type : "string"},
					Factor36				 :  { type : "string"},
					Factor37				 :  { type : "string"},
					Factor38				 :  { type : "string"},
					FactorAct				 :  { type : "number"},
					AcogidoISFUT			 :  { type : "string"},
					Origen					 :  { type: "string" },
					IdOrigen				 :  { type: "string" },
					TipoRegistro			 :  { type : "string"},
					IdEstado				 :  { type : "string"},
					NombreEstado			 :  { type : "string"},
					Nemotecnico				 :  { type : "string"},
					IdInstrumentoEventoCapital	: { type : "string"},
					Mercado					 :  { type : "string"},
                    TipoSociedad			 :  { type : "string"},
				}
			}
		}
	};
	
	obj.getSchemaAnexoCalificacionTributaria = function() {
		return { 
			model: {
				fields : {
					IdCalificacionTributaria : 	{ type : "number"},
					IdTipoInstrumento 		 : 	{ type : "string"},
					IdInstrumento 	  		 : 	{ type : "number"},
					ValorHistorico 	 	 	 : 	{ type : "number"},
					FechaPago   	     	 : 	{ type : "date"},
					IdEventoCapital   	     : 	{ type : "number"},
					Descripcion 	 	 	 : 	{ type : "string"},
					SecuenciaEvento   	 	 : 	{ type : "number"},
					AnioOrigen   		 	 : 	{ type : "string"},
					TasaCredito				 : 	{ type : "number"},
					PorcentajeDividendo		 :	{ type : "number"},
					IdTipoUtilidades		 :	{ type : "string"},
					Factor06			 	 :	{ type : "number"},
					Factor07				 :  { type : "number"},
					Factor08				 :	{ type : "number"},
					Factor16				 :  { type : "number"},
					Factor17				 :  { type : "number"},
					Factor11				 :  { type : "number"},
					Factor09				 :  { type : "number"},
					Factor13				 :  { type : "number"},
					Factor12				 :  { type : "number"},
					Factor14				 :  { type : "number"},
					Factor15				 :  { type : "number"},
					Factor10				 :  { type : "number"},
					TipoRegistro			 :  { type : "string"},
					IdEstado				 :  { type : "string"},
					NombreEstado			 :  { type : "string"},
					Nemotecnico				 :  { type : "string"},
					Mercado					 :  { type : "string"},
					TipoUtilidad			 :	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaControlProceso = function() {
		return { 
			model: {
				fields : {
					Esquema 		 	 		 : 	{ type : "string"},
					Version 				 	 : 	{ type : "string"},
					Carga 	  		     	 	 : 	{ type : "code"},
					Validacion 	 	 			 : 	{ type : "code"},
					PreDeclaracion   	     	 : 	{ type : "code"},
					Certificacion   	     	 : 	{ type : "code"}
				}
			}
		}
	};
	
	obj.getSchemaEventosCapitalPendientes = function() {
		return { 
			model: {
				fields : {
					RutSociedad 		 	 		 : 	{ type : "string"},
					Nemotecnico 				 	 : 	{ type : "string"},
					FechaPago 	  		     	 	 : 	{ type : "date"},
					SecuenciaEvento 	 	 		 : 	{ type : "number"},
					IdEventoCapitalCliente   	     : 	{ type : "number"},
					RazonSocial   	     	 		 : 	{ type : "string"}
				}
			}
		}
	};

	obj.getSchemaEventosCapitalPendientesDJ1949 = function() {
		return { 
			model: {
				fields : {
					RutSociedad 		 	 		 : 	{ type : "string"},
					Nemotecnico 				 	 : 	{ type : "string"},
					FechaPago 	  		     	 	 : 	{ type : "date"},
					SecuenciaEvento 	 	 		 : 	{ type : "number"},
					IdEventoCapitalCliente   	     : 	{ type : "number"},
					RazonSocial   	     	 		 : 	{ type : "string"}
				}
			}
		}
	};

	obj.getSchemaCalificacionesPendientesDJ1949 = function() {
		return { 
			model: {
				fields : {
					RutSociedad 		 	 		 : 	{ type : "string"},
					Nemotecnico 				 	 : 	{ type : "string"},
					FechaPago 	  		     	 	 : 	{ type : "date"},
					SecuenciaEvento 	 	 		 : 	{ type : "number"},
					IdEventoCapitalCliente   	     : 	{ type : "number"},
					RazonSocial   	     	 		 : 	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaCalificacionesPendientesDJ1922 = function() {
		return { 
			model: {
				fields : {
					RutSociedad 		 	 		 : 	{ type : "string"},
					Nemotecnico 				 	 : 	{ type : "string"},
					FechaPago 	  		     	 	 : 	{ type : "date"},
					SecuenciaEvento 	 	 		 : 	{ type : "number"},
					IdEventoCapitalCliente   	     : 	{ type : "number"},
					RazonSocial   	     	 		 : 	{ type : "string"}
				}
			}
		}
	};
		
		
		
		
	obj.getSchemaEventosCapitalPendientesDJ1922 = function() {
		return { 
			model: {
				fields : {
					RutSociedad 		 	 		 : 	{ type : "string"},
					Nemotecnico 				 	 : 	{ type : "string"},
					FechaPago 	  		     	 	 : 	{ type : "date"},
					SecuenciaEvento 	 	 		 : 	{ type : "number"},
					IdEventoCapitalCliente   	     : 	{ type : "number"},
					RazonSocial   	     	 		 : 	{ type : "string"}
				}
			}
		}
	};

	
	obj.getSchemaCertificadosCliente = function() {
		return { 
			model: {
				fields : {
					IdCliente  		     	 	 : 	{ type : "number"},
					NombreCliente 	 			 : 	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaFondoAccionario = function() {
		return { 
			model: {
				fields : {
					IdFondo 		 	 		 : 	{ type : "string"},
					Esquema 				 	 : 	{ type : "string"},
					Version                      : 	{ type : "string"},
					PorcentajeAccionario         : 	{ type : "number"},
					Observacion 	 	 		 : 	{ type : "string"},
					IdEstado         	     	 : 	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaCalculo4Porciento = function() {
		return { 
			model: {
				fields : {
					IdEstado 		 	 		 : 	{ type : "string"},
					Nemotecnico         	     : 	{ type : "string"},
					Version						 :	{ type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaIPC = function() {
		return { 
			model: {
				fields : {
					Agno 		 	 		     : 	{ type : "number"},
					AgnoMes 				 	 : 	{ type : "string"},
					Fecha 	  		     	 	 : 	{ type : "date"},
					ValorIpc 	 			 	 : 	{ type : "number"},
					AgnoTributario               :  { type : "string"},
					DescMes                      :  { type : "string"}
				}
			}
		}
	};
	
	obj.getSchemaDJ1899 = function() {
		return { 
			model: {
				fields : {
					Numero									:	{ type : "number"},
					IdMoneda								:	{ type : "text"},
					RutCliente								:	{ type : "text"},
					ActivoPensionado						:	{ type : "number"},
					RutEmpleador							:	{ type : "text"},
					FechaOperacion							: 	{ type : "date"},
					DepositoEmpleador						: 	{ type : "number"},
					AporteEmpleador							: 	{ type : "number"},
					TraspasoEmpleador						: 	{ type : "number"},
					RetiroEmpleadorActualizado				: 	{ type : "number"},
					MontoRetencionActualizado				: 	{ type : "number"},
					AhorrosModalidadIndirectaEmpleador		: 	{ type : "number"},
					AhorrosModalidadDirectaTrabajador		: 	{ type : "number"},
					CotizacionesModalidadIndirectaEmpleador	: 	{ type : "number"},
					CotizacionesModalidadDirectaTrabajador	: 	{ type : "number"},
					CotizacionesTrabajadorIndependiente		: 	{ type : "number"},
					MontoRetiroPorAhorrosIncisoPrimero		: 	{ type : "number"},
					RetencionImpuestoSobreRetiros			: 	{ type : "number"},
					MontoRetiroPorAhorrosIncisoSegundo		: 	{ type : "number"}
				}
			}
		}
	};
	      
	obj.getDataSourceEsquema = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Esquema", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3226",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaEsquema()			
			}
		})
	};
	
	obj.getDataSourceEventosCapitalPendientes = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "RutSociedad", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3217",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaEventosCapitalPendientes()			
			}
		})
	};
	
	obj.getDataSourceEventosCapitalPendientesDJ1949 = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "RutSociedad", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290671",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaEventosCapitalPendientesDJ1949()			
			}
		})
	};

	obj.getDataSourceCalificacionesPendientesDJ1949 = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "RutSociedad", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290671",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaCalificacionesPendientesDJ1949()			
			}
		})
	};
	
	obj.getDataSourceCalificacionesPendientesDJ1922 = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "RutSociedad", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290674",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaCalificacionesPendientesDJ1922()			
			}
		})
	};
	obj.getDataSourceEventosCapitalPendientesDJ1922 = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "RutSociedad", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290676",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaEventosCapitalPendientesDJ1922()			
			}
		})
	};	
		
	
	obj.getDataSourceCalificacionTributaria = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
                serverFiltering :   true, 
                serverSorting :     true,
                //serverPaging: 	    true,
                sort:               { field: "Nemo", dir: "asc" },
                //pageSize:           25,
                transport: {
                    read: {
                        url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4150",
                        dataType:"json"
                    }
                },
			schema : obj.getSchemaCalificacionTributaria()			
			}
		})
	};

	obj.getDataSourceUsuarioLogeado = function () {
		return new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				serverSorting: true,
				sort: { field: "NumeroDividendo", dir: "asc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=41501",
						dataType: "json"
					}
				},
				schema: obj.getSchemaCalificacionTributaria()
			}
		})
	};
	
	obj.getDataSourceAnexoCalificacionTributaria = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "IdCalificacionTributaria", dir: "desc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4156",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaAnexoCalificacionTributaria()			
			}
		})
	};
	
	obj.getDataSourceCertificadosCliente = function () {
		return new bcs.DataSource({
			type : "array",
			definition : {
				schema : obj.getSchemaCertificadosCliente()			
			}
		})
	};
	
	obj.getDataSourceIPC = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "AgnoMes", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3532",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaIPC()			
		}
		})
	};
	
	obj.getDataSourceMontosDJ1899 = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "ID", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600041",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaDJ1899()			
		}
		})
	};
	
	obj.getDataSourceControlProceso = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "Esquema", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3246",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaControlProceso()			
		}
		})
	};
	
	obj.getDataSourceFondoAccionario = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			sort: { field: "IdFondo", dir: "asc" },
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3524",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaFondoAccionario()			
		}
		})
	};
	
	obj.getDataSourceCalculo4Porciento = function () {
		return new bcs.DataSource({
			type : "odata",
			definition : {
			serverFiltering : true, 
			serverSorting : true, 
			transport: {
	    		read: {
	    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3616",
					dataType:"json"
	    		}
	    	},
			schema : obj.getSchemaCalculo4Porciento()			
		}
		})
	};
	
	obj.getConsultaUltimaVersionEsquema = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceEsquema(),
				
					columns: [
					           obj.getConfiguracionColumna("Esquema",{ groupable: true})
					          ,obj.getConfiguracionColumna("Objetivo",{ groupable: true})
					          ,obj.getConfiguracionColumna("Version_Activa",{ groupable: true})
					          ,obj.getConfiguracionColumna("Estado_Documento",{ groupable: true})
					          ,obj.getConfiguracionColumna("Flujo_Proceso",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaEventosCapitalPendientes = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceEventosCapitalPendientes(),
				
					columns: [
					           obj.getConfiguracionColumna("RutSociedad")
					          ,obj.getConfiguracionColumna("Nemotecnico")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("SecuenciaEvento")
					          ,obj.getConfiguracionColumna("IdEventoCapitalCliente")
					          ,obj.getConfiguracionColumna("RazonSocial")
					]
				}
		};
		return config;
	};

	obj.getConsultaCalificacionesPendientesDJ1949 = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceCalificacionesPendientesDJ1949(),
				
					columns: [
					           obj.getConfiguracionColumna("RutSociedad")
					          ,obj.getConfiguracionColumna("Nemotecnico")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("SecuenciaEvento")
					          //,obj.getConfiguracionColumna("IdEventoCapitalCliente")
					          ,obj.getConfiguracionColumna("RazonSocial")
					]
				}
		};
		return config;
	};

	obj.getConsultaCalificacionesPendientesDJ1922 = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceCalificacionesPendientesDJ1922(),
				
					columns: [
					           obj.getConfiguracionColumna("RutSociedad")
					          ,obj.getConfiguracionColumna("Nemotecnico")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("SecuenciaEvento")
					          //,obj.getConfiguracionColumna("IdEventoCapitalCliente")
					          ,obj.getConfiguracionColumna("RazonSocial")
					]
				}
		};
		return config;
	};
	obj.getConsultaEventosCapitalPendientesDJ1949 = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceEventosCapitalPendientesDJ1949(),
				
					columns: [
					           obj.getConfiguracionColumna("RutSociedad")
					        	,obj.getConfiguracionColumna("RazonSocial")
					          ,obj.getConfiguracionColumna("Nemotecnico")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("SecuenciaEvento")
					          
					]
				}
		};
		return config;
	};	
	
	obj.getConsultaEventosCapitalPendientesDJ1922 = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceEventosCapitalPendientesDJ1922(),
				
					columns: [
				           obj.getConfiguracionColumna("RutSociedad")
				           ,obj.getConfiguracionColumna("RazonSocial")
				          ,obj.getConfiguracionColumna("Nemotecnico")
				          ,obj.getConfiguracionColumna("FechaPago")
				          ,obj.getConfiguracionColumna("SecuenciaEvento")
					]
				}
		};
		return config;
	};
	
	
	obj.getConsultaControlProceso = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'CONTROL_PROCESO'));
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
			
					dataSource: obj.getDataSourceControlProceso(),
				
					columns: [
					           obj.getConfiguracionColumna("Esquema",{ groupable: true})
					          ,obj.getConfiguracionColumna("Version",{ groupable: true})
					          ,obj.getConfiguracionColumna("Carga",{ groupable: true})
					          ,obj.getConfiguracionColumna("Validacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("PreDeclaracion",{ groupable: true})
					          ,obj.getConfiguracionColumna("Certificacion",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaCalificacionTributaria = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'CALIFICACION_TRIBUTARIA'));
				},
				options : {
					// userSettings: true,
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
			
					dataSource: obj.getDataSourceCalificacionTributaria(),
				
					columns: [
							   obj.getConfiguracionColumna("Ejercicio", { groupable: false})
					          //,obj.getConfiguracionColumna("Mercado", { groupable: false})
					          ,obj.getConfiguracionColumna("Nemo", { groupable: false})
					          ,obj.getConfiguracionColumna("FechaPago", { groupable: false})
					          ,obj.getConfiguracionColumna("Descripcion", { groupable: false})
					          ,obj.getConfiguracionColumna("SecuenciaEvento", { groupable: false})
							  ,obj.getConfiguracionColumna("FactorAct", { groupable: false})
					        //   ,obj.getConfiguracionColumna("Factor06", { groupable: false})
					        //   ,obj.getConfiguracionColumna("Factor07", { groupable: false})
					          ,obj.getConfiguracionColumna("Factor08", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor09", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor10", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor11", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor12", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor13", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor14", { groupable: false})
					          ,obj.getConfiguracionColumna("Factor15", { groupable: false})
					          ,obj.getConfiguracionColumna("Factor16", { groupable: false})
					          ,obj.getConfiguracionColumna("Factor17", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor18", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor19", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor38", { groupable: false}) // 19-B
							  ,obj.getConfiguracionColumna("Factor20", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor21", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor22", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor23", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor24", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor25", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor26", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor27", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor28", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor29", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor30", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor31", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor32", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor33", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor34", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor35", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor36", { groupable: false})
							  ,obj.getConfiguracionColumna("Factor37", { groupable: false})
							  ,obj.getConfiguracionColumna("AcogidoISFUT", { groupable: false})
							  ,obj.getConfiguracionColumna("Origen", { groupable: false})
							  
					]
				}
		};

		return config;
	};
	
	obj.getConsultaAnexoCalificacionTributaria = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'ANEXO_CALIFICACION_TRIBUTARIA'));
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
			
					dataSource: obj.getDataSourceAnexoCalificacionTributaria(),
				
					columns: [
					           obj.getConfiguracionColumna("SecuenciaEvento")
					          ,obj.getConfiguracionColumna("IdEventoCapital")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("AnioOrigen")
					          ,obj.getConfiguracionColumna("TasaCredito")
					          ,obj.getConfiguracionColumna("PorcentajeDividendo")
					          ,obj.getConfiguracionColumna("TipoUtilidad")
					          ,obj.getConfiguracionColumna("ValorHistorico")
					]
				}
		};
		return config;
	};
	
	obj.getConsultCalificacionesEventoCapital = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filter(obj.validarFiltros(filtros,'ANEXO_CALIFICACION_TRIBUTARIA'));
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
			
					dataSource: obj.getDataSourceCalificacionTributaria(),
						
					columns: [
					           obj.getConfiguracionColumna("Nemo")
					          ,obj.getConfiguracionColumna("FechaPago")
					          ,obj.getConfiguracionColumna("SecuenciaEvento")
					          //,obj.getConfiguracionColumna("IdEventoCapital")
					          ,obj.getConfiguracionColumna("NumeroDividendo")
					          ,obj.getConfiguracionColumna("Descripcion")
					          
					]
				}
		};
		return config;
	};
	
	obj.getConsultaFondoAccionario = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'ESQUEMA'));
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
			
					dataSource: obj.getDataSourceFondoAccionario(),
				
					columns: [
					           obj.getConfiguracionColumna("IdFondo",{ groupable: true})
					          ,obj.getConfiguracionColumna("Esquema",{ groupable: true})
					          ,obj.getConfiguracionColumna("Version",{ groupable: true})
					          ,obj.getConfiguracionColumna("PorcentajeAccionario",{ groupable: true})
					          ,obj.getConfiguracionColumna("Observacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("IdEstado",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaCalculo4Porciento = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'VERSION'));
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
			
					dataSource: obj.getDataSourceCalculo4Porciento(),
				
					columns: [
					           obj.getConfiguracionColumna("IdEstado")
					          ,obj.getConfiguracionColumna("Nemotecnico")
					]
				}
		};
		return config;
	};
	
	obj.getConsultaIPC = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'CONSULTA_IPC'));
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
			
					dataSource: obj.getDataSourceIPC(),
				
					columns: [
					           obj.getConfiguracionColumna("Agno",{ groupable: true})
					          ,obj.getConfiguracionColumna("AgnoTributario",{ groupable: true})
					          ,obj.getConfiguracionColumna("DescMes",{ groupable: true})
					          ,obj.getConfiguracionColumna("Fecha",{ groupable: true})
					          ,obj.getConfiguracionColumna("ValorIpc",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaMontosDJ1899 = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'CONSULTA_MONTOS'));
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
			
					dataSource: obj.getDataSourceMontosDJ1899(),
				
					columns: [
					          obj.getConfiguracionColumna("Numero",{ groupable: false})
					          ,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
					          ,obj.getConfiguracionColumna("RutCliente",{ groupable: true})
					          //,obj.getConfiguracionColumna("IdCliente",{ groupable: true})
					          ,obj.getConfiguracionColumna("ActivoPensionado",{ groupable: true})
					          ,obj.getConfiguracionColumna("RutEmpleador",{ groupable: true}) 
					          ,obj.getConfiguracionColumna("FechaOperacion",{ groupable: true})
					          ,obj.getConfiguracionColumna("DepositoEmpleador",{ groupable: true})
					          //Aporte Empleador
					          ,obj.getConfiguracionColumna("AporteEmpleador",{ groupable: false})
					          ,obj.getConfiguracionColumna("TraspasoEmpleador",{ groupable: false})
					          ,obj.getConfiguracionColumna("RetiroEmpleadorActualizado",{ groupable: false})
					          ,obj.getConfiguracionColumna("MontoRetencionActualizado",{ groupable: false})
					          //Ahorros del Trabajador acogidos al inciso primero del Art.42 bis de la LIR en UF
					          ,obj.getConfiguracionColumna("AhorrosModalidadIndirectaEmpleador",{ groupable: false})
					          ,obj.getConfiguracionColumna("AhorrosModalidadDirectaTrabajador",{ groupable: false})
					          //DAPV o Cotizacinoes Voluntarias enterados en calidad de trabajador Dependiente del ART. 42 Nª1 de la LIR en UF
					          ,obj.getConfiguracionColumna("CotizacionesModalidadIndirectaEmpleador",{ groupable: false})
					          ,obj.getConfiguracionColumna("CotizacionesModalidadDirectaTrabajador",{ groupable: false})
					          //DAP o Cotizacines Voluntarias enteradas en calidad de trabajador Independiente del Art.42 Nª2 de la LIR en UF
					          ,obj.getConfiguracionColumna("CotizacionesTrabajadorIndependiente",{ groupable: false})
					          //Monto del retiro proveniente de Ahorros Acogidos al Beneficio Tributario del inciso primero
					          //del Art. 42 bis de la L.I.R y/o retiros del trabajador efectuados con cargo a
					          //los aportes del empleador, actualizados en $
					          ,obj.getConfiguracionColumna("MontoRetiroPorAhorrosIncisoPrimero",{ groupable: false})
					          //--Retención de Impto de 15% practicada sobre retiros registrados en la columna anterior actualizados en $
					          ,obj.getConfiguracionColumna("RetencionImpuestoSobreRetiros",{ groupable: false})
					          //Monto del retiro proveniente de Ahorros Acogidos a Beneficio Tributario del inciso segundo del Art. 42 bis de la L.I.R
					          ,obj.getConfiguracionColumna("MontoRetiroPorAhorrosIncisoSegundo",{ groupable: false})
					]
				}
		};
		return config;
	};
	
	obj.getConsultaCertificadosCliente = function() {
		var config = {};
		config = {
				buscar : function(apiGrid,filtros) {
					apiGrid.dataSource.filters(obj.validarFiltros(filtros,'CERTIFICADOS'));
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
			
					dataSource: obj.getDataSourceCertificadosCliente(),
				
					columns: [
					           obj.getConfiguracionColumna("IdCliente",{ groupable: true})
					          ,obj.getConfiguracionColumna("NombreCliente",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	obj.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
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
	
	obj.getReport = function(dataTemplate){
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
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($rootScope.winID,false);
			});
	};
	
	obj.consultaControlProceso = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4200",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
						Esquema				: {type: "string"},
						Version				: {type: "string"},
						EstadoCertificado	: {type: "string"},
						Carga				: {type: "string"},
						Validacion			: {type: "string"},
						PreDeclaracion		: {type: "string"},
						Certificacion		: {type: "string"}
					}
				}
			}
		},
	});	
	
	return obj;
	
});