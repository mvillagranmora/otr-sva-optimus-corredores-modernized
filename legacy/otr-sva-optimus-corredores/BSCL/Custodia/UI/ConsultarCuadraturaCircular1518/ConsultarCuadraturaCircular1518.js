angular.module('ModuloCustodia').controller('ConsultarCuadraturaCircular1518Ctrl', 
		function($scope,
				bcsComunes,
				bcsAlerta, 
				OPCIONES_BCSALERTA, 
				$translate,
				bcsServiciosCustodia)
{
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdClasificacionRE : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "RE"},
				IdClasificacionDV : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "DV"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : ""
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	var dsGrillaCuadraturaCircular = new bcs.DataSource({
	               type : "odata",
	               definition : {
	                   serverFiltering 	: true,
	                   serverPaging     : true,
	                   serverSorting    : true,
	                   pageSize         : 100,
	                   transport 		: {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700392",
								dataType : "json"
							}
	                   },
	                   schema : {
				    		model: {
				    			fields: {
				    				IdCuadraturaCircular1518		: { type: "number"},
									IdCarga							: { type: "number"},
				    				FechaCuadratura					: { type: "date"},
				    				Nemotecnico						: { type: "string"},
				    				ClasificacionTipoInstrumento	: { type: "string"},
									TipoInstrumento					: { type: "string"},
									CantidadCustodia				: { type: "number"},
									CantidadGarantia				: { type: "number"},
									CantidadVentaPlazo				: { type: "number"},
									CantidadCompraPlazo				: { type: "number"},
									TotalCustodia					: { type: "number"},
									CompraEnTransito				: { type: "number"},
									VentaEnTransito					: { type: "number"},
									TransitoIngreso					: { type: "number"},
									TransitoEgreso					: { type: "number"},
									CantidadDCVCuentaRelacionado	: { type: "number"},
									CantidadDisponibleDCVCuentaRelacionado 	: { type: "number"},
									CantidadBloqueadaDCVCuentaRelacionado 	: { type: "number"},
									CantidadReventaDCVCuentaRelacionado 	: { type: "number"},
									CantidadRecompraDCVCuentaRelacionado 	: { type: "number"},
									CantidadValorizadaUFDCVCuentaRelacionado: { type: "number"},
									CantidadDCVCuentaPropia				: { type: "number"},
									CantidadDisponibleDCVCuentaPropia 	: { type: "number"},
									CantidadBloqueadaDCVCuentaPropia 	: { type: "number"},
									CantidadReventaDCVCuentaPropia 		: { type: "number"},
									CantidadRecompraDCVCuentaPropia 	: { type: "number"},
									CantidadValorizadaUFDCVCuentaPropia	: { type: "number"},
									CantidadDCVCuentaTercero			: { type: "number"},
									CantidadDisponibleDCVCuentaTercero 	: { type: "number"},
									CantidadBloqueadaDCVCuentaTercero 	: { type: "number"},
									CantidadReventaDCVCuentaTercero 	: { type: "number"},
									CantidadRecompraDCVCuentaTercero 	: { type: "number"},
									CantidadValorizadaUFDCVCuentaTercero: { type: "number"},
									CantidadBCS						: { type: "number"},
									CantidadBEC 					: { type: "number"},
									CantidadBOV 					: { type: "number"},
									CantidadEmisores 				: { type: "number"},
									CantidadSorteoLetras 			: { type: "number"},
									TotalCustodioEmisores 			: { type: "number"},
									Diferencia						: { type: "number"},
									Precio							: { type: "number"},
									Valorizacion 					: { type: "number"},
									DiferenciaValorizada 			: { type: "number"},
									Explicacion 					: { type: "string"},
									IdEstado						: { type: "string"}
				        		} 
				        	}
					}
	           }
	});
	
   	//GRILLA PRINCIPAL
	$scope.confBasicaCuadraturaCircular = {
		dataSource			: dsGrillaCuadraturaCircular,
		columns				: [
			 {field: "FechaCuadratura",					title: 'UI_FECHA',						template : " <label> #:  util.formatoDate(FechaCuadratura) #</label>" , 	width: "70px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }		         
			,{field: "Nemotecnico",						title: 'UI_NEMO', 						width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 		, groupable: true}		         			
			,{field: "CantidadCustodia", 				title: 'UI_SALDO_CUSTODIA', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadGarantia", 				title: 'UI_GARANTIA',			 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadVentaPlazo",				title: 'UI_VENTA_PLAZO',				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadCompraPlazo",				title: 'UI_COMPRA_PLAZO', 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "TotalCustodia",	 				title: 'UI_TOTAL_CUSTODIA',		 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadDCVCuentaRelacionado",	title: 'UI_DCV_RELACIONADO',	 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadDCVCuentaTercero",		title: 'UI_DCV_CUENTA_TERCERO', 		width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadDCVCuentaPropia",			title: 'UI_DCV_CUENTA_PROPIA', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadBCS",						title: 'UI_CANTIDAD_BCS', 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadBEC",						title: 'UI_CANTIDAD_BEC', 				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadBOV",	 					title: 'UI_CANTIDAD_BOV',	 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadEmisores", 				title: 'UI_CANTIDAD_EMISORES', 			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}			
			,{field: "CompraEnTransito",				title: 'UI_COMPRA_TRANSITO',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "VentaEnTransito",					title: 'UI_VENTA_TRANSITO',				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "TransitoIngreso",					title: 'UI_TRANSITO_INGRESO',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "TransitoEgreso",					title: 'UI_TRANSITO_EGRESO',			width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "CantidadSorteoLetras",			title: 'UI_SORTEO_LETRAS',				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "TotalCustodioEmisores",			title: 'UI_TOTAL_CUSTODIO_EMISORES',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "Diferencia",						title: 'UI_DIFERENCIA',					width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "Precio",							title: 'UI_PRECIO',						width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "Valorizacion",					title: 'UI_VALORIZACION',				width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "DiferenciaValorizada",			title: 'UI_DIFERENCIA_VALORIZACION',	width: "110px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	}
			,{field: "Explicacion", 					title: 'UI_EXPLICACION_DESCUADRATURA', 	width: "250px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, editable: true}
			,{field: "ClasificacionTipoInstrumento",	title: 'UI_MERCADO',					width: "160px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "TipoInstrumento",					title: 'UI_TIPO_INSTRUMENTO',			width: "160px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}		
		],		
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		excel 				: { allPages : true },
		pageable			: true,
		height				: 450,
		selectable 			: "multiple row",
	    gridCreated : function(grid) {
	    	grillaCustPortafolio = grid;
	    }
		
	};
	
   	//********* fin configuracion de objetos *********//
	
	/*************************************************************************
	 * BUSCAR
	 *************************************************************************/
	
	$scope.buscarCustodia = function(){
		var filters = new Array();
		
		if ($scope.filtro.clasificacionTipoInstrumentoObj !== undefined && $scope.filtro.clasificacionTipoInstrumentoObj != "" && $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento != "0")
    		filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento });
	
    	if ($scope.filtro.tipoInstrumento.IdTipoInstrumento !== undefined && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "" && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "0")
    		filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.filtro.tipoInstrumento.IdTipoInstrumento });
		if ($scope.filtro.instrumentoObjeto !== undefined){
    		if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0) {
    			filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtro.instrumentoObjeto.Nemotecnico });
    		}
    	}
		
    	var auxDate1;
    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null){
    		auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
			filters.push({ field: "FechaCuadratura", operator: "eq", value: auxDate1 });
    	}
    	
		dsGrillaCuadraturaCircular.filters(filters);
	};
	
	/*************************************************************************
	 * LIMPIAR FILTROS
	 *************************************************************************/
	
	$scope.limpiarFiltros = function() {
		$scope.filtro.clasificacionTipoInstrumento = "";
		$scope.filtro.tipoInstrumento = "";				
		$scope.filtro.buscarinstrumento = "-1";
		$scope.filtro.fecInicio  = util.dateInicioDia($scope.SesionFechaSistema);
	}
   	
	/************************************************
	* CARGA CUADRATURA DE CUSTODIA
	************************************************/
	$scope.CargarCuadraturaCircular1518 = function() {
		var datos = {};
		datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
	    datos.p_IdUsuario =	$scope.SesionIdUsuario;
	    datos.p_IdModulo = 'CUS';
		datos.p_FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		bcsServiciosCustodia.CuadrarCircular1518(datos,
			function(respuesta){	  		
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
	};
	
	$scope.cargarArchivoL02 = function() {
		let parametros = {};
		parametros.FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
		parametros.TipoArchivoCarga = "ARCHIVO_L02";
		parametros.TituloPrevioCarga = 'UI_ARCHIVO_L02';
		var id1 = bcsComunes.ventanaAbrir("CargarArchivoCircular1518", parametros, false, false, $scope.idVentana);	
		var promesa=bcsComunes.ventanaPromesa(id1);  
		$.when(promesa).then(function(respuesta){
			$scope.buscarCustodia();
		});
	}


	$scope.cargarArchivoL043 = function(){
		bcsComunes.ventanaOverlay($scope.idVentana,true);

		var codArch = 'UI_ARCHIVO_L043';
		var titulo  = 'Cuadratura de Custodia - Archivo L043';

		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
		var ventanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);

		var ArchivoL02 = bcsComunes.ventanaPromesa(ventanaCargaArchivo);

		ArchivoL02.then(function(datosArchivos){
			bcsComunes.ventanaOverlay(idVentana,true);
			var datos = {};
			datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
			datos.p_IdUsuario =	$scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
			datos.p_DatosArchivo = [];

			if(!datosArchivos || !datosArchivos.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			for(let i =0; i< datosArchivos.fileData.length; i++){
				if(datosArchivos.fileData[i].MENSAJEVALIDACION == 'OK') {
					let posicion = {};
					posicion.Cuenta = datosArchivos.fileData[i].CUENTA.trim();
					posicion.Nemotecnico = datosArchivos.fileData[i].NEMOTEC.trim();
					posicion.Unidad = datosArchivos.fileData[i].UNIDAD.trim();
					posicion.TipoInstrumento = datosArchivos.fileData[i].TIPOINSTRUMENTO.trim();
					posicion.FechaArchivo = datosArchivos.fileData[i].FECHAARCHIVO.trim();
					posicion.CantidadSorteoLetras = datosArchivos.fileData[i].CANTIDADSORTEOLETRAS;
					posicion.Validacion = datosArchivos.fileData[i].MENSAJEVALIDACION;
					datos.p_DatosArchivo.push(posicion);
				}
			}
			
			if(datos.p_DatosArchivo.length > 0){

				bcsServiciosCustodia.CargarArchivoL043(datos,
					function(respuesta){	  		
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					}
				);
			}
			else{
				bcsComunes.ventanaOverlay(idVentana,false);
			} 
		});
	}
	
	$scope.cargarArchivoOtros = function() {
		let parametros = {};
		parametros.FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
		parametros.TipoArchivoCarga = "ARCHIVO_OTROS_SALDOS";
		parametros.TituloPrevioCarga = 'UI_ARCHIVO_OTROS_SALDOS';
		var id1 = bcsComunes.ventanaAbrir("CargarArchivoCircular1518", parametros, false, false, $scope.idVentana);	
		var promesa=bcsComunes.ventanaPromesa(id1);  
		$.when(promesa).then(function(respuesta){
			$scope.buscarCustodia();
		});
	}
	
	$scope.cargarArchivoOptimus = function() {
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		var codArch = 'ARCHIVO_CARGA_CUSTODIA_CIRCULAR1518_OPTIMUS';
		var titulo  = 'Cuadratura de Custodia - Archivo Optimus Custodia';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: titulo};
		var ventanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,$scope.idVentana,false);
		
		var ArchivoOptimus = bcsComunes.ventanaPromesa(ventanaCargaArchivo);
		
		ArchivoOptimus.then(function(datosArchivos){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			var datos = {};
			datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
		    datos.p_IdUsuario =	$scope.SesionIdUsuario;
		    datos.p_IdModulo = 'CUS';
			datos.p_FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
			datos.p_DatosArchivo = [];
			
			if(!datosArchivos || !datosArchivos.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			for(let i =0; i< datosArchivos.fileData.length; i++){
				if(datosArchivos.fileData[i].MENSAJEVALIDACION == 'OK') {
					let posicion = {};
					posicion.Nemotecnico = datosArchivos.fileData[i].NEMOTECNICO.trim();
					posicion.CantidadDisponible = datosArchivos.fileData[i].CANTIDADDISPONIBLE;
					posicion.CantidadGarantia = datosArchivos.fileData[i].CANTIDADGARANTIA;
					posicion.CantidadVentaPlazo = datosArchivos.fileData[i].CANTIDADVENTAPLAZO;
					posicion.CantidadCompraPlazo = datosArchivos.fileData[i].CANTIDADCOMPRAPLAZO;
					posicion.VentaEnTransito = datosArchivos.fileData[i].CANTIDADVENTATRANSITO;
					posicion.CompraEnTransito = datosArchivos.fileData[i].CANTIDADCOMPRATRANSITO;
					posicion.Precio = datosArchivos.fileData[i].PRECIO;
					posicion.Validacion = datosArchivos.fileData[i].MENSAJEVALIDACION;
					datos.p_DatosArchivo.push(posicion);
				}
			}
	        
	        if(datos.p_DatosArchivo.length > 0){
	        	bcsServiciosCustodia.CargarArchivoOptimus(datos,
					function(respuesta){	  		
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana,false);
					}
				);
	        }
	        else{
				bcsComunes.ventanaOverlay(idVentana,false);
			} 
		});
		
	}
	$scope.CierreCuadraturaCircular1518 = function() {
		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_CUADRATURA_CUSTODIA "));		
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};
					datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
					datos.p_FechaOperacion = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
					bcsComunes.ventanaOverlay($scope.idVentana,true);
					bcsServiciosCustodia.ProcesarCuadraturaCustodia(datos,
						function(respuesta){	  		
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.idVentana);
							bcsComunes.ventanaOverlay($scope.idVentana,false);
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.idVentana);
							bcsComunes.ventanaOverlay($scope.idVentana,false);
						}
					);
				}
			}
		)
	};
	
	// Boton para SVARESGIN-7050
	$scope.limpiarCargas = function() {		
		var mensaje = $translate.instant("UI_MENSAJE_LIMPIAR_CARGAS_CUSTODIA").replace("XXXXXXXX", util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio)));
		var t = bcsAlerta.crear("PREGUNTA_SI_NO", mensaje);
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				var datos = {};
				datos.p_FechaCuadratura = util.dateSPdate(util.dateInicioDia($scope.filtro.fecInicio));
				bcsComunes.ventanaOverlay($scope.idVentana, true);
				bcsServiciosCustodia.LimpiarCargasCircular1518(datos,
					function(respuesta) {
						bcsComunes.ventanaMsg({
							conflictos: util.parseServerResponse(respuesta)
						}, $scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana, false);
						$scope.buscarCustodia();
					},
					function(respuesta) {
						bcsComunes.ventanaMsg({
							conflictos: util.parseServerResponse(respuesta)
						}, $scope.idVentana);
						bcsComunes.ventanaOverlay($scope.idVentana, false);
					}
				);
			}
		});
	};

	$scope.limpiarFiltros();
});