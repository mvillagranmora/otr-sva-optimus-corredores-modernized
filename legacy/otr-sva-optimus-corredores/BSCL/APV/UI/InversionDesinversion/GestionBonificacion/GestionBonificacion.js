angular.module('ModuloAPV').controller('GestionBonificacionCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {}; 
		
		var gesBon = this;
		//Configurar al principio
		gesBon.data = {};
		gesBon.OpcionesAnoAhorro = {format: 'yyyy', start: 'year',depth: 'year'};
		gesBon.OpcionesAnoProceso = {format: 'yyyy', start: 'year',depth: 'year'};
		var MontoModificado = false;
		var BonificacionesModificadas = [];
		
		gesBon.filtroEstadoBonificacion = {
				NombreEntidad : {field: "NombreEntidad", operator: "eq", value: 'BONIFICACION'}
		};
		
		var datosBonificacion = {
			type : "odata",
			definition : {
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                sort 			: { field: "IdBonificacion", dir: "desc" },
                pageSize        : 20,
                transport		: {
					read: {
						url 	: bcsConsultasAPV('ConsultarBonificacion'),
						dataType: "json"
				    }
				},
                schema: { 
				   model: {
						fields: {													
						    "IdEmpresa" 					: {type: "number"},
							"IdBonificacion" 				: {type: "number"},
							"FechaGeneracion" 				: {type: "sysdate"},
							"TipoRegistro" 					: {type: "string"},
							"AnoAhorro" 					: {type: "number"},
							"AnoProceso"					: {type: "number"},
							"FolioEgreso"					: {type: "number"},
							"MontoPagadoNeto"				: {type: "number"},
							"MontoSaldoNeto"				: {type: "number"},
							"MontoPagadoReajustado" 		: {type: "number"},
							"FechaPago" 					: {type: "sysdate"},
							"IdCliente" 					: {type: "number"},
							"MontoBonificacionAfiliadoNeto" : {type: "number"},
							"PA1" 							: {type: "number"},
							"PA2" 							: {type: "number"},
							"PA3"							: {type: "number"},
							"MontoAfiliadoNeto"				: {type: "number"},
							"MontoAfiliadoSaldoNeto"		: {type: "number"},
							"MontoReajustadoPagadoAfiliado"	: {type: "number"},
							"IdEstado"						: {type: "string"},
							"CodigoEstado"					: {type: "string"},
							"NombreEstado"					: {type: "string"},
							"CodigoArchivo"					: {type: "string"},
							"IdPersona"						: {type: "number"},
							"DocumentoIdentidad"			: {type: "string"},
							"NombreCompletoCliente"			: {type: "string"},
							"MontoAhorroDJ"					: {type: "number"},
							"MontoRetiroDJ"					: {type: "number"},
							"MontoAhorroNetoDJ"				: {type: "number"},
							"MontoCalculado"				: {type: "number"},
							"MontoBonificacionDiferencia"	: {type: "number"},
							"Comentario"					: {type: "string"},
							"MontoTraspasoEgreso"			: {type: "number"},
							"MontoEgreso"					: {type: "number"},
							"PorcentajeTraspasoEgreso"		: {type: "number"},
							"PorcentajeEgreso"				: {type: "number"},
							"MontoTraspasoDevolucion"		: {type: "number"},
							"MontoEgresoDevolucion"			: {type: "number"},
							"MontoCalculadoOrigen"			: {type: "string"}
						}
					}
                }
			}
		};

		//------ GRILLA
		var dsDatosBonificacion = new bcs.DataSource(datosBonificacion);
		gesBon.confBonificacion = {
			dataSource	: dsDatosBonificacion,
			columns		: [ {field: "IdEmpresa" 			, title: "UI_ID_EMPRESA"						, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "IdBonificacion" 					, title: "UI_ID_BONIFICACION"					, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "FechaGeneracion" 					, title: "UI_FECHA_GENERACION"					, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "TipoRegistro" 					, title: "UI_TIPO_REGISTRO"						, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "AnoAhorro" 						, title: "UI_ANO_AHORRO"						, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "AnoProceso"						, title: "UI_ANO_PROCESO"						, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "FolioEgreso"						, title: "UI_FOLIO_EGRESO"						, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "MontoPagadoNeto"					, title: "UI_MONTO_PAGADO_NETO"					, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoPagadoNeto ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoSaldoNeto"					, title: "UI_MONTO_SALDO_NETO"					, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoSaldoNeto ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoPagadoReajustado" 			, title: "UI_MONTO_PAGADO_REAJUSTADO"			, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoPagadoReajustado ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "FechaPago" 						, title: "UI_FECHA_PAGO"						, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "IdCliente" 						, title: "UI_ID_CLIENTE"						, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "DocumentoIdentidad"				, title: "UI_DOCUMENTO_IDENTIDAD"				, width: "110px", align: "left"		, sortable : true, editable: false }	
				,{field: "NombreCompletoCliente" 			, title: "UI_NOMBRE_CLIENTE"					, width: "200px", align: "left"		, sortable : true, editable: false }
				,{field: "MontoBonificacionAfiliadoNeto" 	, title: "UI_MONTO_BONIFICACION_AFILIADO_NETO"	, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoBonificacionAfiliadoNeto ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "PA1" 								, title: "UI_PA1"								, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "PA2" 								, title: "UI_PA2"								, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "PA3"								, title: "UI_PA3"								, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "MontoAfiliadoNeto"				, title: "UI_MONTO_AFILIADO_NETO"				, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoAfiliadoNeto ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoAfiliadoSaldoNeto"			, title: "UI_MONTO_AFILIADO_SALDO_NETO"			, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoAfiliadoSaldoNeto ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoReajustadoPagadoAfiliado"	, title: "UI_MONTO_REAJUSTADO_PAGADO_AFILIADO"	, width: "120px", align: "left"		, sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoReajustadoPagadoAfiliado ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "IdEstado"							, title: "UI_ID_ESTADO"							, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "CodigoEstado"						, title: "UI_CODIGO_ESTADO"						, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "NombreEstado"						, title: "UI_ESTADO"							, width: "90px", align: "left"		, sortable : true, editable: false }
				,{field: "CodigoArchivo"					, title: "UI_CODIGO_ARCHIVO"					, width: "250px", align: "left"		, sortable : true, editable: false }
				,{field: "IdPersona"						, title: "UI_PERSONA"							, width: "90px", align: "left"		, sortable : true, editable: false, hidden: true }
				,{field: "MontoAhorroDJ"					, title: "UI_MONTO_AHORRO_DJ"					, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoAhorroDJ ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoRetiroDJ"					, title: "UI_MONTO_RETIRO_DJ"					, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoRetiroDJ ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoAhorroNetoDJ"				, title: "UI_MONTO_AHORRO_NETO_DJ"				, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoAhorroNetoDJ ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoTraspasoEgreso"				, title: "UI_MONTO_TRASPASO_EGRESO"				, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoTraspasoEgreso ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoTraspasoDevolucion"			, title: "UI_MONTO_A_TRASPASAR"					, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoTraspasoDevolucion ,'GEN','Cantidad','CLP','','')).valor #</label>" }
				,{field: "MontoEgreso"						, title: "UI_MONTO_EGRESO"						, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoEgreso ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoEgresoDevolucion"			, title: "UI_MONTO_EGRESO_DEVOLUCION"			, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoEgresoDevolucion ,'GEN','Cantidad','CLP','','')).valor #</label>" }
				,{field: "MontoCalculado"					, title: "UI_MONTO_A_BONIFICAR"					, width: "90px", align: "left"		, sortable : true, editable: true, template : " <label> #:  (util.bcs_formato(MontoCalculado ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "MontoBonificacionDiferencia"		, title: "UI_MONTO_BONIFICACION_DIFERENCIA"		, width: "90px", align: "left"		, sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoBonificacionDiferencia ,'GEN','Monto','CLP','','')).valor #</label>" }
				,{field: "Comentario"						, title: "UI_COMENTARIO"						, width: "140px", align: "left"		, sortable : true, editable: false }
			],
			sortable	: true,
    		pageable	: true,
    		filterable	: true,
    		navigatable	: true,
    		height		: 500,
    		selectable 	: "multiple row",
    		autoBind 	: true,
    		userSettings: true,
    		save : function(e){
    			if(e.model.MontoCalculadoOrigen !== e.values.MontoCalculado){
    				e.model.MontoCalculado = e.values.MontoCalculado;
    				MontoModificado = true;
    			}
    		},
    		gridCreated : function(grid){
    			gesBon.grid = grid
    		}
		};	
		
		gesBon.fnGuardarBonificacion = function(){
			bcsComunes.ventanaOverlay(idVentana,true);

			if(MontoModificado === false){
				bcsComunes.msg("warning","warning","No hay datos para Modificar. Bonificaciones sin cambios ");
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			for (i = 0; i < dsDatosBonificacion.data().length; i++){
				var datos = dsDatosBonificacion.data()[i];
				if(datos.MontoCalculado !== datos.MontoCalculadoOrigen)
					BonificacionesModificadas.push({IdBonificacion: datos.IdBonificacion, MontoABonificar: datos.MontoCalculado});
			}
			
			var parametros = {				
				p_IdEmpresa 	: $scope.SesionIdEmpresa,
				p_IdUsuario 	: $scope.SesionIdUsuario,
				p_IdModulo  	: "APV",
				p_Parametros	: BonificacionesModificadas,
			};
			
			bcsServiciosAPV.ModificarBonificacion(parametros).$promise.then(function(respuesta){
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
		  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_BONIFICACIONES_MODIFICADAS_CORRECTAMENTE" )},idVentana);
				}else{
		  			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			    }
				bcsComunes.ventanaOverlay(idVentana,false);
			});

			
			BonificacionesModificadas = [];
			MontoModificado = false;
			bcsComunes.ventanaOverlay(idVentana,false);
		
		};
		
		gesBon.buscar = function(){
			var filter = Array(); 
			
			if(gesBon.datosForm.SocioNegocioidActual !== false )
				filter.push({ field: "IdPersona" , operator: "eq", value: gesBon.datosForm.SocioNegocioidActual });
			
			if(gesBon.EstadoIdActual !== false && gesBon.EstadoIdActual !== "0" )
				filter.push({ field: "CodigoEstado" , operator: "eq", value: gesBon.EstadoIdActual });
					
			if(gesBon.datosForm.AnoAhorroIdActual !== false)
				filter.push({ field: "AnoAhorro" , operator: "eq", value: gesBon.datosForm.AnoAhorroIdActual });
			
			if(gesBon.datosForm.AnoProcesoIdActual !== false)
				filter.push({ field: "AnoProceso" , operator: "eq", value: gesBon.datosForm.AnoProcesoIdActual });
			
			if(filter.length > 0){
				dsDatosBonificacion.filter(filter);
				dsDatosBonificacion.refresh;
			}
			else
			{
				filter.push({ field: "IdBonificacion"	, operator: "Neq", value: ""});
				gesBon.grid.dataSource.filter(filter);
			}
			$scope.gesBon.seleccionBonificacion = undefined;
		};
		
		gesBon.limpiar = function(){
			gesBon.EstadoIdActual = false;
			gesBon.datosForm.SocioNegocioidActual = false;
			gesBon.datosForm.AnoAhorroIdActual = false;
			gesBon.datosForm.AnoProcesoIdActual = false;
			var filter = Array();
			
			filter.push({ field: "IdBonificacion"	, operator: "Neq", value: -1});
			var ConsultaBonificacion = new bcs.DataSource(datosBonificacion);
			ConsultaBonificacion.query({filter : filter}).then(function(){
				dsDatosBonificacion.data(ConsultaBonificacion.data());
				dsDatosBonificacion.refresh;
				$scope.gesBon.seleccionBonificacion = undefined;
			});
		};

		gesBon.fnProcesarBonificacion = function(){

			if(gesBon.datosForm.AnoAhorroIdActual == false){
				bcsComunes.msg("warning","warning","Debe seleccionar el año ahorro.");
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se ejecutará proceso de calculo de bonificación fiscal. ¿Desea continuar?" ,idVentana);
			t .then(function(result)
			{
				if (result === OPCIONES_BCSALERTA.NO)
				{
					return;
				}

				bcsComunes.ventanaOverlay(idVentana,true);
				var parametrosBonificacion= [];
				
				var parametros = {				
					p_IdEmpresa  : $scope.SesionIdEmpresa,
					p_IdUsuario  : $scope.SesionIdUsuario,
					p_IdModulo   : "APV", 
					p_AnoAhorro  : gesBon.datosForm.AnoAhorroIdActual
				};

				bcsComunes.ventanaOverlay($scope.ventanaId,true);
					
				bcsServiciosAPV.ProcesarBonificacion(parametros).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
						bcsComunes.ventanaMsg({ mensaje: respuesta.P_OUT_MENSAJES[0].descripcion}, idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
					else{

			  			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					 }
					gesBon.buscar();
		   		},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
		   			$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   		});
			});	
		};
		
		gesBon.fnEjecutarBonificacion = function(){
			var validacion = true;
			
			if(MontoModificado === true){
				bcsComunes.msg("warning","warning","Existen datos pendientes de Guardar.");
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}

			if($scope.gesBon.seleccionBonificacion === undefined){
				bcsComunes.msg("warning","warning","No hay bonificaciones seleccionadas.");
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			$.each($scope.gesBon.seleccionBonificacion,function(key,value){
		    
				if(value.IdEstado === "EJE"){  
					bcsComunes.msg("warning","warning","Existen datos seleccionados que ya han sido ejecutados.");
					bcsComunes.ventanaOverlay(idVentana,false);
					validacion = false;
					return;
				}else if(value.IdEstado === "ING"){  
					bcsComunes.msg("warning","warning","La bonificacion debe ser PROCESADA.");
					bcsComunes.ventanaOverlay(idVentana,false);
					validacion = false;
					return;
				}
				
				if(value.MontoCalculado > value.MontoReajustadoPagadoAfiliado){  
					bcsComunes.msg("warning","warning","El Monto Calculado " + value.MontoCalculado + " es mayor al Monto Reajustado Pagado Afiliado " + value.MontoReajustadoPagadoAfiliado + ".");
					bcsComunes.ventanaOverlay(idVentana,false);
					validacion = false;
					return;
				}
			});	
			
			if(validacion === false){
				return;
			}
			var t = bcsAlerta.crear("PREGUNTA_SI_NO","Se ejecutará proceso de generación de ordenes de bonificación fiscal. ¿Desea continuar?" ,idVentana);
			t .then(function(result)
			{
				if (result === OPCIONES_BCSALERTA.NO)
				{
					return;
				}

				bcsComunes.ventanaOverlay(idVentana,true);
				var parametrosBonificacion= [];

				$.each($scope.gesBon.seleccionBonificacion,function(key,value){

					var datosparamEntrada = { 
							IdBonificacion: value.IdBonificacion
						};		
					parametrosBonificacion.push(datosparamEntrada);
				});			
				
				var parametros = {				
					p_IdEmpresa  : $scope.SesionIdEmpresa,
					p_IdUsuario  : $scope.SesionIdUsuario,
					p_IdModulo   : "APV", 
					p_Parametros : parametrosBonificacion
				};

				bcsComunes.ventanaOverlay($scope.ventanaId,true);
					
				bcsServiciosAPV.EjecutarBonificacion(parametros).$promise.then(function(respuesta){
			   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));

					if(respuesta.P_OUT_MENSAJES.length > 0){
						var mensajeCarga = new Array();
						$.each(respuesta.P_OUT_MENSAJES,function(key,value){
							 var objCarga = {
									IdInterno : value.id,
									tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
									codigo : value.codigo,
									descripcion : value.descripcion
							 };
							 mensajeCarga.push(objCarga);
						});
						respuesta.P_OUT_RESULTADO = mensajeCarga;
						bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							
		
						bcsComunes.ventanaOverlay(idVentana,false);
					}
					gesBon.buscar();
		   		},function(respuesta){
					var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
		   			$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   		});
			});	
		};
	});