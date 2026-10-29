(function() {
	angular.module('ModuloPlataforma').controller('quincenalDePactosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes, bcsServiciosMesa,$http)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		$scope.data = {};
		
		//Configurar al principio
				
		$scope.fechaOperacion = {};
		//$scope.fechaOperacion.max = new Date();
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.monedaPrincipal; 
		$scope.data.kngMonedaPrincipal;
		var ArrayModificados = [];
		
		$scope.data.insertarQuincenalDePactos = function(tablaQuincenal)
		{
			var parametros = {};

			parametros.p_IdEmpresa  	= $scope.SesionIdEmpresa;
			parametros.p_IdUsuario  	= $scope.SesionIdUsuario;
			parametros.p_TablaQuincenal = tablaQuincenal;
				
			bcsServiciosMesa.InsertarQuincenalDePactos(parametros, successInsertQuincenal,errorInsertQuincenal);
		};
		
		$scope.data.consultarQuincenalDePactos = function()
		{	
			if($scope.data.kngFecha === undefined || $scope.data.kngFecha === null)
			{
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VACIA'));
			}
			else{
				
				var parametros = {};

				parametros.p_Fecha    	= $scope.data.kngFecha;
				parametros.p_IdEmpresa  = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario  = $scope.SesionIdUsuario;
				
				bcsServiciosMesa.ConsultarQuincenalDePactos(parametros, successQuincenalDePactos,errorQuincenalDePactos);
			}
		};
		
		var successInsertQuincenal = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				bcsComunes.msg("info","info",$translate.instant('UI_INGRESO_REALIZADO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));				
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
			overlay.encendido = false;
        };	
        
        var errorInsertQuincenal = function(respuesta)
		{
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			overlay.encendido = false;
		};
		
          
		var successQuincenalDePactos = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				bcsComunes.msg("info","info",$translate.instant('UI_CONSULTA_REALIZADA'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$scope.confQuincenalDePactos.dataSource.removeAll();
				if(respuesta.P_OUT_COUNTQUINCENALES > 0){
					dsQuincenalDePactos.data(respuesta.P_OUT_QUINCENALDEPACTOS);
				}else{
					if(respuesta.P_OUT_QUINCENALDEPACTOS.length > 0){
						var posicion = 0;
						var cantidadResto = 0;
						var diferencia = 0;
						var nemo = "";
						var emisor = "";
						var fecha = "";
						var moneda = "";
						if(respuesta.P_OUT_FOLIONEMO == "S"){
							$.each(respuesta.P_OUT_QUINCENALDEPACTOS, function( index, valueQ ) {
								$.each(respuesta.P_OUT_SUBYACENTEFOLIO, function( index, valueS ) {
									if(valueQ.FolioCartera == valueS.FolioCarteraRelacionada){
										if(valueQ.IdTipoOperacion == "COMTER"){
											if(valueQ.MontoNominal - valorS.Cantidad == 0)
												valueQ.IndicadorCobertura = "S";
											else
												valueQ.IndicadorCobertura = "N";

											valueQ.CantidadResto = valueQ.MontoNominal - valorS.Cantidad;
											valueQ.CantidadComprometida = valorS.Cantidad;

										}else{
											valueQ.CantidadResto = 0;
											valueQ.IndicadorCobertura = "S";
											valueQ.CantidadComprometida = valueQ.CantidadSubyacente;
										}
									}
								});
							});
						}
						else{
							$.each(respuesta.P_OUT_QUINCENALDEPACTOS, function( index, valueQ ) {
								$.each(respuesta.P_OUT_SUBYACENTENEMO, function( index, valueS ) {
									if(valueS.Nemotecnico ==  valueQ.Nemo && valueS.IdEmisor == valueQ.Emisor && 
										valueS.FechaVencimientoInstrumento == valueQ.FechaVencimientoInstrumento &&
										valueS.IdMonedaInstrumento == valueQ.IdMoneda){
										if(valueQ.IdTipoOperacion == "COMTER"){
											if(valueS.Nemotecnico !=  nemo || valueS.IdEmisor || emisor && 
												valueS.FechaVencimientoInstrumento != fecha || valueS.IdMonedaInstrumento != moneda){
												posicion = 0;
												diferencia = 0;
											}
											cantidadResto =  posicion == 0 ? valorS.Cantidad - valueQ.MontoNominal : diferencia - valueQ.MontoNominal;
											if(cantidadResto >= 0){
												valueQ.IndicadorCobertura = "S";
												valueQ.CantidadResto = 0;
												valueQ.CantidadComprometida = valueQ.MontoNominal;
												diferencia =  cantidadResto;
											}
											else{ 
												valueQ.IndicadorCobertura = "N";
												valueQ.CantidadResto = cantidadResto *-1;
												valueQ.CantidadComprometida = diferencia;
											}
											posicion = posicion++;
											nemo = valueS.Nemotecnico;
											emisor = valueS.IdEmisor;
											fecha = valueS.FechaVencimientoInstrumento;
											moneda = valueS.IdMonedaInstrumento;

										}else{
											if(valueS.Nemotecnico !=  nemo || valueS.IdEmisor || emisor && 
												valueS.FechaVencimientoInstrumento != fecha || valueS.IdMonedaInstrumento != moneda){
												posicion = 0;
												diferencia = 0;
											}
											cantidadResto =  posicion == 0 ? valorS.Cantidad - valueQ.CantidadSubyacente : diferencia - valueQ.CantidadSubyacente;
											if(cantidadResto >= 0){
												valueQ.CantidadResto = 0;
												valueQ.IndicadorCobertura = "S";
												valueQ.CantidadComprometida = valueQ.CantidadSubyacente;
												diferencia =  cantidadResto;

											}else{
												valueQ.IndicadorCobertura = "N";
												valueQ.CantidadResto = cantidadResto *-1;
												valueQ.CantidadComprometida = diferencia;
											}
										}
									}
								});
							});
						}
						$scope.data.insertarQuincenalDePactos(respuesta.P_OUT_QUINCENALDEPACTOS);
						dsQuincenalDePactos.data(respuesta.P_OUT_QUINCENALDEPACTOS);
					}
				}
				
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
			overlay.encendido = false;
          };	
		
		var errorQuincenalDePactos = function(respuesta)
		{
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			overlay.encendido = false;
		};
		
		$scope.data.grabarQuincenalDePactos = function()
	    { 
			var parametros = {};
						parametros.p_Fecha						= $scope.data.kngFecha;
						parametros.p_IdEmpresa         			= $scope.SesionIdEmpresa;
						parametros.p_IdUsuario					= $scope.SesionIdUsuario;
						parametros.p_IdModulo					= 'MRF';
						parametros.p_DatosQuincenalDePactos		= ArrayModificados;
						
						bcsServiciosMesa.ModificarQuincenalDePactos(parametros,successGrabarQuincenalDePactos,errorGrabarQuincenalDePactos);

		};
		
		var successGrabarQuincenalDePactos = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				bcsComunes.msg("info","info",$translate.instant('UI_GRABACION_REALIZADA'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				
				ArrayModificados = [];
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
			overlay.encendido = false;
          };	
		
		var errorGrabarQuincenalDePactos = function(respuesta)
		{
			bcsComunes.msg("error","error",$translate.instant('UI_QUINCENAL_PACTOS_ERROR_MODIFICACION'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			overlay.encendido = false;
		};

			var dsDefQuincenalDePactos = 
			{
					type : "call",
					definition : 
					{	//se Comenta Server Filtering por generar problemas con export excel
//	                    serverFiltering : true, // Filtrado de datos en el servidor.
//	                    serverPaging    : true, // Paginado de los datos en el servidor.
//	                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                    sort 			: { field: "", dir: "DESC" },
	                    pageSize        : 100,
//	                    transport		: {
//			        	            		read: {
//			        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=573",
//			        							dataType:"json"
//			        	            		}
//			        	            	},
	                    schema 			: { 
	                    				   model: 
	                    						{
													fields : 
													{
														IdQuincenalPactos				: {type: "int"},
														FechaProceso					: {type: "string"},
														TipoCartera						: {type: "string"},
														Emisor							: {type: "string"},
														TipoReajuste					: {type: "string"},
														FechaInicioCompromiso			: {type: "string"},
														FechaVencimientoCompromiso		: {type: "string"},
														FechaVencimientoInstrumento		: {type: "string"},
														MontoNominal					: {type: "decimal"},
														IdTipoOperacion					: {type: "string"},
														Nemo							: {type: "string"},
														IdMoneda						: {type: "string"},
														FolioOperacion					: {type: "int"},
														Secuencia_Pareo					: {type: "int"},
														CantidadResto					: {type: "decimal"},
														IndicadorCobertura				: {type: "string"},
														CantidadComprometida			: {type: "decimal"},
														IdRelacionCorredor				: {type: "int"},
														Lin_Comprom						: {type: "int"},
														ClasificacionTipoInstrumento	: {type: "string"},
														ValorPresente					: {type: "decimal"},
														PrecioPacto						: {type: "decimal"},
														ValorComprometido				: {type: "decimal"},
														NumeroCupones					: {type: "int"},
														PlazoResidual					: {type: "int"},
														RutCliente						: {type: "string"},
														SecuenciaRut					: {type: "int"},
														RutCorredor						: {type: "string"},
														Sec_Comprom						: {type: "int"},
														TasaInstrumento					: {type: "decimal"},
														TasaPacto						: {type: "decimal"},
														TipoFamilia						: {type: "string"},
														TipoInstrumento					: {type: "string"},
														TipoReajustePacto				: {type: "string"},
														TipoClasificacionSvsCliente		: {type: "string"},
														TipoAmortizacion				: {type: "string"},
														CategoriaRiesgo					: {type: "string"},
													}
												}
										  }

	    			 }
			};	
			
		//---------------------------------------------
		//GRILLA QUINCENAL DE PACTOS
 
		var dsQuincenalDePactos = new bcs.DataSource(dsDefQuincenalDePactos);
		$scope.confQuincenalDePactos = 
		{
    				dataSource		: dsQuincenalDePactos,
    					columns		: [  {field: "IdQuincenalPactos", title: "UI_QUINCENAL_DE_PACTOS", width: "150px", align: "left", sortable : false }
    					       		    ,{field: "FechaProceso", title: "UI_FECHA_PROCESO", width: "150px", align: "left", sortable : false }
				       				    ,{field: "TipoCartera", title: "UI_TIPO_CARTERA", width: "150px", align: "left", sortable : false}
				       				    ,{field: "Emisor", title: "UI_EMISOR", width: "150px", align: "left", sortable : false }
				       				    ,{field: "TipoReajuste", title: "UI_TIPO_REAJUSTE", width: "150px", align: "left", sortable : false }
				       				    ,{field: "FechaInicioCompromiso", title: "UI_FECHA_INI_COMP", width: "150px", align: "left", sortable : false}
				       				    ,{field: "FechaVencimientoCompromiso", title: "UI_FECHA_VENC_COMP", width: "150px", align: "left", sortable : false}
				       				    ,{field: "FechaVencimientoInstrumento", title: "UI_FECHA_VENC_INST", width: "150px", align: "left", sortable : false }
				       				    ,{field: "MontoNominal", title: "UI_MONTO_NOMINAL", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(MontoNominal,'GEN','Cantidad',TipoReajuste,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
				       				    ,{field: "IdTipoOperacion", title: "UI_ID_TIPO_OPERACION", width: "150px", align: "left", sortable : false}
				       				    ,{field: "TipoInstrumento", title: "UI_TIPO_INSTRUMENTO", width: "150px", align: "left", sortable : false}
				       				    ,{field: "Nemo", title: "UI_NEMOTECNICO_SVS", width: "150px", align: "left", sortable : false }
				       				    ,{field: "IdMoneda", title: "UI_MONEDA", width: "150px", align: "left", sortable : false }
				       				    ,{field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "150px", align: "left", sortable : false }
				       				    //,{field: "CantidadResto", title: "UI_CANTIDAD_RESTO", width: "150px", align: "left", sortable : false }
				       				    ,{field: "IndicadorCobertura", title: "UI_INDICADOR_COBERTURA", width: "150px", align: "left", sortable : false }
				       				    ,{field: "CantidadComprometida", title: "UI_CANTIDAD_COMPROMETIDA", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(CantidadComprometida,'GEN','Cantidad',TipoReajustePacto,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
				       				    ,{field: "IdRelacionCorredor", title: "UI_ID_RELACION_CORREDOR", width: "150px", align: "left", sortable : false }
				       				    //,{field: "Lin_Comprom", title: "UI_LIN_COMPROM", width: "150px", align: "left", sortable : false }
				       				    ,{field: "ClasificacionTipoInstrumento", title: "UI_CLASI_TIPO_INST", width: "150px", align: "left", sortable : false}
				       				    ,{field: "ValorPresente", title: "UI_VALOR_MERCADO", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(ValorPresente,'GEN','Monto',IdMoneda,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
				       				    ,{field: "PrecioPacto", title: "UI_PRECIO_PACTO", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(PrecioPacto,'GEN','Monto',IdMoneda,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,0)#" }
				       				    ,{field: "ValorComprometido", title: "UI_VALOR_COMPROMETIDO", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(ValorComprometido,'GEN','Monto','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
				       				    ,{field: "NumeroCupones", title: "UI_NUMERO_CUPNES", width: "150px", align: "left", sortable : false}
				       				    ,{field: "PlazoResidual", title: "UI_PLAZO_RESIDUAL", width: "150px", align: "left", sortable : false }
				       				    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "150px", align: "left", sortable : false }
				       				    //,{field: "SecuenciaRut", title: "UI_SECUENCIA_RUT", width: "150px", align: "left", sortable : false}
				       				    ,{field: "RutCorredor", title: "UI_RUT_CORREDOR", width: "150px", align: "left", sortable : false }
				       				    ,{field: "Sec_Comprom", title: "UI_SEC_COMPROM", width: "150px", align: "left", sortable : false }
				       				    ,{field: "TasaInstrumento", title: "UI_TASA_INSTRUMENTO", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(TasaInstrumento,'GEN','Tasa',TipoReajuste,TipoInstrumento,'')).valor #</label>"}
				       				    ,{field: "TasaPacto", title: "UI_TASA_PACTO", width: "150px", align: "left", sortable : false,template : " <label> #:  (util.bcs_formato(TasaPacto,'GEN','Tasa',TipoReajustePacto,TipoInstrumento,'')).valor #</label>" }
				       				    ,{field: "TipoFamilia", title: "UI_TIPO_FAMILIA", width: "150px", align: "left", sortable : false }
				       				    ,{field: "TipoReajustePacto", title: "UI_TIPO_REAJUSTE_PACTO", width: "150px", align: "left", sortable : false }
				       				    ,{field: "TipoClasificacionSvsCliente", title: "UI_TIPO_CLASI_SVS_CLI", width: "150px", align: "left", sortable : false }
				       				    ,{field: "Secuencia_Pareo", title: "UI_SECUENCIA_PAREO", width: "150px", align: "left", sortable : false, editable: true}
				       				    ,{field: "TipoAmortizacion", title: "UI_TIPO_AMORTIZACION", width: "150px", align: "left", sortable : false, editable: true }
				       				    ,{field: "CategoriaRiesgo", title: "UI_CATEGORIA_RIESGO", width: "150px", align: "left", sortable : false, editable: true }
	
									  ],		
    					sortable		: true,
    					filterable		: false,
    					navigatable 	: true,
    					height			: 250,
    					scrollable		: true,
    				    selectable 		: "multiple row",
    				    autoBind 	 	: true,
    				    pageable		: true,
    				    gridCreated		: function(grid){
    				    	
    				    	
    				    }
       	};
		
		var valQuincenalDePactos = $scope.confQuincenalDePactos;
		valQuincenalDePactos.save = function(e){
			var filaActual = e.model;
			var datosModificados = e.values;
			var existe = false;
			
			if(ArrayModificados.length > 0){
				$.each(ArrayModificados, function(key,value){
					if(value.IdQuincenalPactos === filaActual.IdQuincenalPactos){
						
						value.IdQuincenalPactos				= filaActual.IdQuincenalPactos;
						value.FechaProceso					= filaActual.FechaProceso;
						value.TipoCartera					= filaActual.TipoCartera;
						value.Emisor						= filaActual.Emisor;
						value.TipoReajuste					= filaActual.TipoReajuste;
						value.FechaInicioCompromiso			= filaActual.FechaInicioCompromiso;
						value.FechaVencimientoCompromiso	= filaActual.FechaVencimientoCompromiso;
						value.FechaVencimientoInstrumento	= filaActual.FechaVencimientoInstrumento;
						value.MontoNominal					= filaActual.MontoNominal;
						value.IdTipoOperacion				= filaActual.IdTipoOperacion;
						value.Nemo							= filaActual.Nemo;
						value.IdMoneda						= filaActual.IdMoneda;
						value.FolioOperacion				= filaActual.FolioOperacion;
						value.Secuencia_Pareo				= datosModificados.Secuencia_Pareo || filaActual.Secuencia_Pareo;
						value.CantidadResto					= filaActual.CantidadResto;
						value.IndicadorCobertura			= filaActual.IndicadorCobertura;
						value.CantidadComprometida			= filaActual.CantidadComprometida;
						value.IdRelacionCorredor			= filaActual.IdRelacionCorredor;
						value.Lin_Comprom					= filaActual.Lin_Comprom;
						value.ClasificacionTipoInstrumento	= filaActual.ClasificacionTipoInstrumento;
						value.ValorPresente					= filaActual.ValorPresente;
						value.ValorComprometido				= filaActual.ValorComprometido;
						value.NumeroCupones					= filaActual.NumeroCupones;
						value.PlazoResidual					= filaActual.PlazoResidual;
						value.RutCliente					= filaActual.RutCliente;
						value.SecuenciaRut					= filaActual.SecuenciaRut;
						value.RutCorredor					= filaActual.RutCorredor;
						value.Sec_Comprom					= filaActual.Sec_Comprom;
						value.TasaInstrumento				= filaActual.TasaInstrumento;
						value.TasaPacto						= filaActual.TasaPacto;
						value.TipoFamilia					= filaActual.TipoFamilia;
						value.TipoInstrumento				= filaActual.TipoInstrumento;
						value.TipoReajustePacto				= filaActual.TipoReajustePacto;
						value.TipoClasificacionSvsCliente	= filaActual.TipoClasificacionSvsCliente;
						value.TipoAmortizacion				= datosModificados.TipoAmortizacion || filaActual.TipoAmortizacion;
						value.CategoriaRiesgo				= datosModificados.CategoriaRiesgo || filaActual.CategoriaRiesgo;
						

					}else{
						var ObjDatosQuincenalDePactos = {
								"IdQuincenalPactos"				: filaActual.IdQuincenalPactos
								,"FechaProceso"					: filaActual.FechaProceso
								,"TipoCartera"					: filaActual.TipoCartera
								,"Emisor"						: filaActual.Emisor
								,"TipoReajuste"					: filaActual.TipoReajuste
								,"FechaInicioCompromiso"		: filaActual.FechaInicioCompromiso
								,"FechaVencimientoCompromiso"	: filaActual.FechaVencimientoCompromiso
								,"FechaVencimientoInstrumento"	: filaActual.FechaVencimientoInstrumento
								,"MontoNominal"					: filaActual.MontoNominal
								,"IdTipoOperacion"				: filaActual.IdTipoOperacion
								,"Nemo"							: filaActual.Nemo
								,"IdMoneda"						: filaActual.IdMoneda
								,"FolioOperacion"				: filaActual.FolioOperacion
								,"Secuencia_Pareo"				: datosModificados.Secuencia_Pareo || filaActual.Secuencia_Pareo
								,"CantidadResto"				: filaActual.CantidadResto
								,"IndicadorCobertura"			: filaActual.IndicadorCobertura
								,"CantidadComprometida"			: filaActual.CantidadComprometida
								,"IdRelacionCorredor"			: filaActual.IdRelacionCorredor
								,"Lin_Comprom"					: filaActual.Lin_Comprom
								,"ClasificacionTipoInstrumento"	: filaActual.ClasificacionTipoInstrumento
								,"ValorPresente"				: filaActual.ValorPresente
								,"ValorComprometido"			: filaActual.ValorComprometido
								,"NumeroCupones"				: filaActual.NumeroCupones
								,"PlazoResidual"				: filaActual.PlazoResidual
								,"RutCliente"					: filaActual.RutCliente
								,"SecuenciaRut"					: filaActual.SecuenciaRut
								,"RutCorredor"					: filaActual.RutCorredor
								,"Sec_Comprom"					: filaActual.Sec_Comprom
								,"TasaInstrumento"				: filaActual.TasaInstrumento
								,"TasaPacto"					: filaActual.TasaPacto
								,"TipoFamilia"					: filaActual.TipoFamilia
								,"TipoInstrumento"				: filaActual.TipoInstrumento
								,"TipoReajustePacto"			: filaActual.TipoReajustePacto
								,"TipoClasificacionSvsCliente"	: filaActual.TipoClasificacionSvsCliente
								,"TipoAmortizacion"				: datosModificados.TipoAmortizacion || filaActual.TipoAmortizacion
								,"CategoriaRiesgo"				: datosModificados.CategoriaRiesgo || filaActual.CategoriaRiesgo
						};
				
						ArrayModificados.push(ObjDatosQuincenalDePactos);
					}
				});					
			}else{
				
				var ObjDatosQuincenalDePactos = {
						"IdQuincenalPactos"				: filaActual.IdQuincenalPactos
						,"FechaProceso"					: filaActual.FechaProceso
						,"TipoCartera"					: filaActual.TipoCartera
						,"Emisor"						: filaActual.Emisor
						,"TipoReajuste"					: filaActual.TipoReajuste
						,"FechaInicioCompromiso"		: filaActual.FechaInicioCompromiso
						,"FechaVencimientoCompromiso"	: filaActual.FechaVencimientoCompromiso
						,"FechaVencimientoInstrumento"	: filaActual.FechaVencimientoInstrumento
						,"MontoNominal"					: filaActual.MontoNominal
						,"IdTipoOperacion"				: filaActual.IdTipoOperacion
						,"Nemo"							: filaActual.Nemo
						,"IdMoneda"						: filaActual.IdMoneda
						,"FolioOperacion"				: filaActual.FolioOperacion
						,"Secuencia_Pareo"				: datosModificados.Secuencia_Pareo || filaActual.Secuencia_Pareo
						,"CantidadResto"				: filaActual.CantidadResto
						,"IndicadorCobertura"			: filaActual.IndicadorCobertura
						,"CantidadComprometida"			: filaActual.CantidadComprometida
						,"IdRelacionCorredor"			: filaActual.IdRelacionCorredor
						,"Lin_Comprom"					: filaActual.Lin_Comprom
						,"ClasificacionTipoInstrumento"	: filaActual.ClasificacionTipoInstrumento
						,"ValorPresente"				: filaActual.ValorPresente
						,"ValorComprometido"			: filaActual.ValorComprometido
						,"NumeroCupones"				: filaActual.NumeroCupones
						,"PlazoResidual"				: filaActual.PlazoResidual
						,"RutCliente"					: filaActual.RutCliente
						,"SecuenciaRut"					: filaActual.SecuenciaRut
						,"RutCorredor"					: filaActual.RutCorredor
						,"Sec_Comprom"					: filaActual.Sec_Comprom
						,"TasaInstrumento"				: filaActual.TasaInstrumento
						,"TasaPacto"					: filaActual.TasaPacto
						,"TipoFamilia"					: filaActual.TipoFamilia
						,"TipoInstrumento"				: filaActual.TipoInstrumento
						,"TipoReajustePacto"			: filaActual.TipoReajustePacto
						,"TipoClasificacionSvsCliente"	: filaActual.TipoClasificacionSvsCliente
						,"TipoAmortizacion"				: datosModificados.TipoAmortizacion || filaActual.TipoAmortizacion
						,"CategoriaRiesgo"				: datosModificados.CategoriaRiesgo || filaActual.CategoriaRiesgo
				};
				
				ArrayModificados.push(ObjDatosQuincenalDePactos);
			}
		};

		//GENERACION DE REPORTE
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015",
						dataType : "json",
					}
				},
				schema 				: {
					model: {
						fields : {
							CompanyId	: {type: "string"},
							Id			: {type: "string"},
							ReportName	: {type: "string"}					
						}
					}
				}
			},
		});
		
		$scope.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.Id,
				"parameters"	: "",
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.ReportName,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": dataTemplate.subReports
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					//var fileURL = URL.createObjectURL(file);
					var nombreArchivo = dataTemplate.ReportName+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
					saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
						conflictos:{
							errores : [ {
			    				Codigo : 'COD',
			    				Descripcion : 'Un error inesperado ha ocurrido'
			    			} ],
						}
					},idVentana);
				});
		};
		
		$scope.dataTemplate= {};
		
		$scope.generarReporteQuinPactos = function() {
				var filterTemplate = [ { field: "ReportName",operator:"eq", value: 'MRF_QUINCENAL_DE_PACTOS' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= 'pdf';
					$scope.dataTemplate.Id				= ConsultaTemplate.view()[0].Id;
					$scope.dataTemplate.ReportName	= ConsultaTemplate.view()[0].ReportName;
				
					$scope.dataTemplate.data = { "serviceId" 	: '580', 
							 					"serviceData" 	:{ 
							 										"P_FECHA"		: $scope.data.kngFecha,
							 										"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
							 										"P_IDUSUARIO"	: $scope.SesionIdUsuario
							 									 }
					};
					
					var parametros = { 	"P_FECHA"		: $scope.data.kngFecha,
										"P_IDEMPRESA"	: $scope.SesionIdEmpresa,
										"P_IDUSUARIO"	: $scope.SesionIdUsuario
					};
						
					var subReports = new Array();

					$scope.dataTemplate.subReports = subReports;

					subReports.push({ "reportName" 	: "QuincenalDePactos"
									 ,"data" 		: {
														"serviceId" 	: '578',
														"serviceData" 	: parametros
									}
					});
					
					subReports.push({ "reportName" 	: "CompromisosVigentes"
									 ,"data" 		: {
														"serviceId" 	: '578',
														"serviceData" 	: parametros
									 }
					});
					$scope.getReport($scope.dataTemplate);
				}
			});
		};
		
		
		$scope.data.generarAnexoXIX = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MRF';
			parametrosSP.p_UsaReglasNegocio	= 'N';
	        parametrosSP.p_Fecha	    	= $scope.data.kngFecha;
	        parametrosSP.p_Formato  		= 'TXT';
				        
        	bcsComunes.ventanaOverlay(idVentana,true);
        	bcsServiciosMesa.GenerarArchivoPAccmmdd(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ADVERTENCIASCONTADOR){
        			bcsComunes.msg("warning",$translate.instant("UI_SOLICITUD_PACCMMDD"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else if (result.P_OUT_ERRORESCONTADOR){
        			bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_PACCMMDD"),result.P_OUT_ERRORESDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else{
        			var ruta = result.P_OUT_RESULTADOPROCESO[7].Valor;
        			var nombre = result.P_OUT_RESULTADOPROCESO[4].Valor;
        			
        			$timeout(function(){
        				var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
        			    var datos = { file: ruta + nombre };
        			    $http({
        			    	method: 'POST',
        			    	url: url,
        			    	params: datos
        			    }).success(function(datos){
        			    	var file = new File([datos], nombre);
        			    	saveAs(file);
        			    	bcsComunes.msg("info",$translate.instant("UI_SOLICITUD_PACCMMDD"),$translate.instant("UI_ARCHIVO_PACCMMDD_OK"));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    }).error(function (err){
        			    	bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_PACCMMDD"),$translate.instant('UI_ARCHIVO_PACCMMDD_ERROR'));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    });
        			}, 2000);
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_PACCMMDD"),$translate.instant("UI_ARCHIVO_PACCMMDD_ERROR"));
        		bcsComunes.ventanaOverlay(idVentana,false);
        	});
        	
        	bcsServiciosMesa.GenerarArchivoCCccmmdd(parametrosSP).$promise.then(function(result) {
        		if (result.P_OUT_ADVERTENCIASCONTADOR){
        			bcsComunes.msg("warning",$translate.instant("UI_SOLICITUD_CCCCMMDD"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else if (result.P_OUT_ERRORESCONTADOR){
        			bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_CCCCMMDD"),result.P_OUT_ERRORESDETALLE[0].Codigo);
        			bcsComunes.ventanaOverlay(idVentana,false);
        		}
        		else{
        			var ruta = result.P_OUT_RESULTADOPROCESO[7].Valor;
        			var nombre = result.P_OUT_RESULTADOPROCESO[4].Valor;
        			
        			$timeout(function(){
        				var url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
        			    var datos = { file: ruta + nombre };
        			    $http({
        			    	method: 'POST',
        			    	url: url,
        			    	params: datos
        			    }).success(function(datos){
        			    	var file = new File([datos], nombre);
        			    	saveAs(file);
        			    	bcsComunes.msg("info",$translate.instant("UI_SOLICITUD_CCCCMMDD"),$translate.instant("UI_ARCHIVO_CCCCMMDD_OK"));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    }).error(function (err){
        			    	bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_CCCCMMDD"),$translate.instant('UI_ARCHIVO_CCCCMMDD_ERROR'));
        			    	bcsComunes.ventanaOverlay(idVentana,false);
        			    });
        			}, 2000);
        		}
        	},
        	function(result){
        		bcsComunes.msg("error",$translate.instant("UI_SOLICITUD_CCCCMMDD"),$translate.instant("UI_ARCHIVO_ANEXO_XIX_ERROR"));
        		bcsComunes.ventanaOverlay(idVentana,false);
        	});
		};
		
		
		
		
		$scope.data.consultarQuincenalDePactos();
//------------		
	});
}());