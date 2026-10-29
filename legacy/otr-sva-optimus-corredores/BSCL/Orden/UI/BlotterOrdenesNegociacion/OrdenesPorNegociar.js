angular.module('ModuloOrdenes').controller('OrdenesPorNegociarCtrl',function($scope,$rootScope,$translate,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsServicioOrden,$interval){
	
	var controllerName = 'OrdenesPorNegociarCtrl';
	var refresh = false;
	$scope.SesionLocal = $scope.SesionLocale;
	$scope.refrescoAutomatico 	= true;
	$scope.msgWebsocket = function(message) {    	
    	if(message.Evento==='CREAR_ORDEN'||message.Evento==='ORDEN_OFERTADA'){
    		$scope.refrescaAutomatico();
    	}
    };
	
	$scope.defaultOptionsNew = {
		allowCopy: true,
		autoBind: false,
		excel : {
			allPages: true,
		},
  		filterable: true,
		groupable: true,
		height:300,
		pageable: true,
		reorderable: true,
		resizable: true,
  		/*rowColorBackground : function(row) {
	  		switch(row.get("AccionOrden")) {
	            case 'VENTA':
	                return "red";
	                break;
	            case 'COMPRA':
	                return "green";
	                break;
	        }
	  	},*/
		rowColorBackground : function(row) {
			if (!row["AccionOrden"] && !row["EnAnulacion"]) return;
			if(row["AccionOrden"].toUpperCase()==='VENTA' && row["EnAnulacion"].toUpperCase()==='S') {
				return;
			}
			else if(row["AccionOrden"].toUpperCase()==='VENTA' && row["EnAnulacion"].toUpperCase()==='N'){
				return "red";		

			}
			else if(row["AccionOrden"].toUpperCase()==='COMPRA'){
				return "green";		

			}

		},
		rowCellColor : function(row) {
			if (!row["AccionOrden"] && !row["EnAnulacion"]) return;
			if((row["AccionOrden"].toUpperCase()==='VENTA' || row["AccionOrden"].toUpperCase()==='COMPRA') && row["EnAnulacion"].toUpperCase()==='S') {
				return {column:"IdOrden", color:"red"};
			}
			else if((row["AccionOrden"].toUpperCase()==='VENTA' || row["AccionOrden"].toUpperCase()==='COMPRA') && row["EnAnulacion"].toUpperCase()==='N'){
				return;		

			}
		},
	  	scrollable: true,
  		selectable: "multiple, row",
		sortable: true,
		userSettings:true,
		navigatable: true,
		
		dataSource: new bcs.newDataSource(bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesPorNegociar()),
		columns: [
			bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaOperacionOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadDisponibleOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoUI")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EsVentaCortaUI")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoOperacion")
			
			
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCondicionDeLiquidacion")
			
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("MontoOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("DocumentoIdentidad")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVigencia")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaCustodia")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoEmpleadoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoPortafolio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreEmpleadoDigitador")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoPrecio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCanal")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoVigenciaOrden")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes")
	 		, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstadoOrdenAsignacion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoInstrumento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreClasificacionTipoInstrumento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CantidadAsignadaOrden")
			
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("PrecioStrike")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("TipoOpcion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EstiloOpcion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadLiquidacion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("ModalidadEjercicio")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("Tamano")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoCorredorCorresponsal")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("FechaVencimiento")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("EnAnulacion")	
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("InstrumentoNegociacion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdMonedaNegociacion")
			, bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdCondicionDeLiquidacion")
		],
	};
	
    
	if ($scope.SesionLocale === 'PE') {
		//splice
		$scope.defaultOptionsNew.columns.push(bcsConfiguracionOrdenesV2.getConfiguracionColumna("OrdenEspecialUI"));
		$scope.defaultOptionsNew.columns.push(bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreFormaNegociacion"));
	}
	
	$scope.buscarOrdenes = function(apiGrid,filtros){
		var filtros = bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.filtros,'ORDEN');
		if ($scope.filtros.Operador && $scope.filtros.Operador.trim() != 'TODOS'){
			bcsConfiguracionOrdenesV2.generarFiltroOperadorCarteraInstrumento($scope.filtros,function(filtroInstrumentos){
				filtros.push(filtroInstrumentos);
				$scope.apiGridOrdenes.dataSource.filter(filtros);
				$scope.ordenesSeleccionadas = undefined;
			});
			bcsConfiguracionOrdenesV2.generarFiltroOperadorCarteraEjecutivo($scope.filtros,function(filtroEjecutivo){
				filtros.push(filtroEjecutivo);
				$scope.apiGridOrdenes.dataSource.filter(filtros);
				$scope.ordenesSeleccionadas = undefined;
			});
		}else{
			$scope.apiGridOrdenes.dataSource.filter(filtros);
			$scope.ordenesSeleccionadas = undefined;
		}
	}
	
	$scope.filtros = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.filtrosClasificacionTipoInstrumento = {
		logic : "or",
		RV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
		DV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"},
		RF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
		IF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
	};
	
 	$scope.filtrosTipoInstrumento = {
 	 		IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": ""},
 	 		IdTipoInstrumento : {"field": "IdTipoInstrumento","operator": "neq","value": "FWD"}
 	 	};

	
	$scope.filtros.filtroBolsa = {
		IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : 0 }
	};
	$scope.filtrosMonedaNegociacion = {
		IdMoneda : {"field": "IdMoneda","operator": "eq","value": 0}    
	};
	$scope.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
	$scope.CanalTrader	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "TRA" };
	$scope.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
	$scope.CanalOWP		= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "OWP" };
	
	$scope.filtros.filtrosCanalIngreso = {
		complejo : [$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente,$scope.CanalOWP],
		logic : "or",
		filters : [
			$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente, $scope.CanalOWP
		]
	};
	
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.IdTipoOperacion = 0;
		$scope.filtros.IdTipoInstrumento = 0;
		$scope.filtros.IdClasificacionTipoInstrumento = 'RV';
		
		
		$scope.filtros.fechaOperacion = "";
		
		
		$scope.filtros.Operador = 0;
		$scope.filtros.OperadorCarteraInversion = 0;
		$scope.filtros.OperadorCarteraEjecutivo = 0;
		$scope.filtros.IdCorredorCorresponsal = 0;
		
		$scope.filtros.filtroBolsa.IdInstrumento.value = 0;
		$scope.filtros.filtroBolsa.read = true;
		$scope.filtros.IdBolsa = 0;
		$scope.filtros.filtrosCanalIngreso.IdTipoCanalIngreso = 0;
		$scope.filtros.IdMoneda	= 0;
		$scope.filtros.IdCondicionLiquidacion = 0;
	};

	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden} );
	};
  
	
	$scope.$watch('filtros.Operador',function(newval,oldval) {	
		if (newval!==oldval){
			refresh=true;
		}
	});
	$scope.$watch('filtros.OperadorCarteraInversion',function(newval,oldval) {	
		if (newval!==oldval){
			refresh=true;
		}
	});
	$scope.$watch('filtros.OperadorCarteraEjecutivo',function(newval,oldval) {	
		if (newval!==oldval){
			refresh=true;
		}
	});
	
	$scope.$watch('filtros.IdMoneda',function(newval,oldval) {	
		if (newval!==oldval){
			refresh=true;
		}
	});

	$scope.$watch('ordenesSeleccionadas',function(newval,oldval) {
		if (!newval) {
			return;
		}
		
		var nemo = undefined;
		var exit = false;
		$.each(newval,function(key,value){
			if (nemo === undefined) {
				nemo = value.IdInstrumento;
			}
			
			if (value.IdInstrumento !== nemo) {
				exit = true;
				return false;
			}
		});
		
		
		
		$scope.sumaCompras = "-";
		$scope.sumaVentas = "-";
		$scope.precioMayorCompras = "-";
		$scope.precioMenorCompras = "-";
		$scope.precioMayorVentas = "-";
		$scope.precioMenorVentas = "-";

		if (exit) {
			return;
		}
		$scope.nemo = newval[0].InstrumentoUI;
		var sumaCompras = 0;
		var sumaVentas = 0;
		var mayorCompras = -1;
		var mayorVentas = -1;
		var menorCompras = 999999999999999;
		var menorVentas = 999999999999999;
		//variables para formatear resumen
		var monedaSeleccion = [];
		var idMoneda = "";
		var idTipoInstrumento = "";
		var idTipoOperacion = "";

		$.each(newval,function(key,value){
			if (value.AccionOrden === 'COMPRA') {
				sumaCompras = sumaCompras + value.CantidadOrden;
				menorCompras = Number(value.PrecioOrden)<menorCompras?Number(value.PrecioOrden):menorCompras;
				mayorCompras = Number(value.PrecioOrden)>mayorCompras?Number(value.PrecioOrden):mayorCompras;
			}
			if (value.AccionOrden === 'VENTA') {
				sumaVentas = sumaVentas + value.CantidadOrden;
				menorVentas = Number(value.PrecioOrden)<menorVentas?Number(value.PrecioOrden):menorVentas;
				mayorVentas = Number(value.PrecioOrden)>mayorVentas?Number(value.PrecioOrden):mayorVentas;
			}
			
			if(!_.contains(monedaSeleccion,value.IdMonedaOrden)){
				monedaSeleccion.push(value.IdMonedaOrden);
				idMoneda = value.IdMonedaOrden;
				idTipoOperacion = value.IdTipoOperacion;
				idTipoInstrumento = value.IdTipoInstrumento;
			}
		});
		
		if (sumaCompras !== 0) {
			$scope.sumaCompras = " " + util.formateaNumero(sumaCompras,0);
		}
		if (sumaVentas !== 0) {
			$scope.sumaVentas = " " + util.formateaNumero(sumaVentas,0);
		}
		if (menorCompras !== 999999999999999) {
			$scope.precioMenorCompras = monedaSeleccion[0] + " " + util.bcs_formato((menorCompras != 999999999999999 && monedaSeleccion.length==1?menorCompras:0),"GEN","precio",idMoneda,idTipoInstrumento,'').valor;
		}
		if (menorVentas !== 999999999999999) {
			$scope.precioMenorVentas = monedaSeleccion[0] + " " + util.bcs_formato((menorVentas != 999999999999999 && monedaSeleccion.length==1?menorVentas:0),"GEN","precio",idMoneda,idTipoInstrumento,'').valor;
		}
		if (mayorCompras !== -1) {
			$scope.precioMayorCompras = monedaSeleccion[0] + " " + util.bcs_formato((mayorCompras != -1 && monedaSeleccion.length==1 ?mayorCompras:0),"GEN","precio",idMoneda,idTipoInstrumento,'').valor;
		}
		
		if (mayorVentas !== -1) {
			$scope.precioMayorVentas = monedaSeleccion[0] + " " + util.bcs_formato((mayorVentas != -1 && monedaSeleccion.length==1 ?mayorVentas:0),"GEN","precio",idMoneda,idTipoInstrumento,'').valor;	
		}
		
	});
	
	$scope.aprobarSolicitud  = function(ordenesSeleccionadas) { 
		//Funcion para aprobar solicitudes de anulacion,  estas deben senalar si estan solo ofertadas o asignadas. (ESTADO_ORDEN) 
		//ademas de enviar todos los datos requeridos para ello
		
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		if(!$scope.ValidarSolicitudAnulacion(ordenesSeleccionadas)){
			var idModal = bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_ORDEN_SIN_SOLICITUD_ANULACION'), $scope.idVentana);
		bcsComunes.ventanaOverlay($scope.idVentana,false);	
		}else{
			//llamar servicio
			var motivo = "Anulacion por Solicitud";
			if (motivo && angular.isString(motivo)) {
					var datos = {
		    			p_IdEmpresa : $scope.SesionIdEmpresa,
			  			p_IdUsuario : $scope.SesionIdUsuario,
			  			p_IdModulo : 'ORD',
			  			p_Opcion  : 'APROBAR',
			  			p_ordenes : [],
		    		};
			    	$.each(ordenesSeleccionadas,function(key,ord){
			  			datos.p_ordenes.push({IdOrden : ord.IdOrden,MotivoAnulacion : motivo});
			  		});
			    	//bcsComunes.ventanaOverlay($scope.idVentana,true);
			    	bcsServicioOrden.AprobarRechazarSolicitudAnulacionOrdenes(datos).$promise.then(function(result){
			    	//	bcsComunes.ventanaOverlay($scope.idVentana,false);
						var id = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos: util.parseServerResponse(result)
						},$scope.idVentana)

			    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
			    		
			    	},function(result){
			    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
						//bcsComunes.ventanaOverlay($scope.idVentana,false);	
			    		
			    	});
				}

		
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		}
		
	};
	
	$scope.rechazarSolicitud  = function(ordenesSeleccionadas) { 
		//Funcion para rechazar solicitudes de anulacion, debe recibir la data suficiente para poder 
		//cambiar el estado de la orden de EnAnulacion = 'S' a 'N'
		var motivo = "Anulacion por Solicitud";
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		if(!$scope.ValidarSolicitudAnulacion(ordenesSeleccionadas)){
			var idModal = bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_ORDEN_SIN_SOLICITUD_ANULACION'), $scope.idVentana);
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		}else{
			//llamar servicio

			var datos = {
    			p_IdEmpresa : $scope.SesionIdEmpresa,
	  			p_IdUsuario : $scope.SesionIdUsuario,
	  			p_IdModulo : 'ORD',
	  			p_Opcion  : 'RECHAZAR',
	  			p_ordenes : [],
    		};
	    	$.each(ordenesSeleccionadas,function(key,ord){
	  			datos.p_ordenes.push({IdOrden : ord.IdOrden,MotivoAnulacion : motivo});
	  		});
	    	//bcsComunes.ventanaOverlay($scope.idVentana,true);
	    	bcsServicioOrden.AprobarRechazarSolicitudAnulacionOrdenes(datos).$promise.then(function(result){
	    	//	bcsComunes.ventanaOverlay($scope.idVentana,false);
				var id = bcsComunes.ventanaMsg({
					mensaje: '',
					conflictos: util.parseServerResponse(result)
				},$scope.idVentana)

	    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
	    		
	    	},function(result){
	    		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				//bcsComunes.ventanaOverlay($scope.idVentana,false);	
	    		
	    	});
		
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		}
		
	};

	$scope.cantarOrdenes = function(ordenesSeleccionadas) {
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		if(!$scope.ValidarOrdenANegociar(ordenesSeleccionadas)){
			var idModal = bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_ORDEN_CON_SOLICITUD_ANULACION'), $scope.idVentana);
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		}else{		
			var datos = {};
			var ordenes = [];
			$.each(ordenesSeleccionadas,function(key,orden){
				ordenes.push({IdInterno: orden.IdOrden, IdOrden : orden.IdOrden , IdBolsa : ''});

			});
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'ORD';
			datos.p_Ordenes = ordenes;

			bcsServicioOrden.CantarOrdenes(datos).$promise.then(function(result){
				$scope.ordenesSeleccionadas = undefined;
				var idModal = bcsComunes.ventanaMsg({
						mensaje: 'Operacion Exitosa',
						conflictos:util.parseServerResponse(result)
					}, $scope.idVentana);
				bcsComunes.ventanaPromesa(idModal).then(function(){
					$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
					bcsComunes.ventanaOverlay($scope.idVentana,false);		
				});

			},function(result){
				var idModal = bcsComunes.ventanaMsg({
					mensaje: $translate.instant('UI_ERROR_CANTAR_ORDENES'),
					conflictos:util.parseServerResponse(result)
				}, $scope.idVentana);
				bcsComunes.ventanaPromesa(idModal).then(function(){
					bcsComunes.ventanaOverlay($scope.idVentana,false);	
					$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				});
			});
		}
	};
	$scope.ValidarOrdenANegociar = function(ordenesSeleccionadas){
		var esValida = true;
		$.each(ordenesSeleccionadas,function(key,value){
			if(value.EnAnulacion === 'S'){
				esValida = false;
		}
		
		});
		return esValida;
	}
	
	$scope.ValidarSolicitudAnulacion = function(ordenesSeleccionadas){
		var esValida = true;
		$.each(ordenesSeleccionadas,function(key,value){
			if(value.EnAnulacion ==='N'){
				esValida = false;
			}
			
		});
		return esValida;
	}
	
	
	$scope.rutearOrdenes = function(ordenesSeleccionadas,operador,bolsaDistribucion) {
		
		if (!ordenesSeleccionadas || ordenesSeleccionadas.length === 0) return;

		bcsComunes.ventanaOverlay($scope.idVentana,true);		
		var datos = {};
		var ordenes = [];
  		$.each(ordenesSeleccionadas,function(key,orden){
  			ordenes.push(
  			{
  				IdOrden : orden.IdOrden , 
  				IdBolsaDistribucion : bolsaDistribucion,
  				CodigoOperador : operador,
  				IdTipoCanalDistribucion : bcsConfiguracionOrdenes.CONSTANTES.IdTipoCanalDistribucionAgente,
  				});
  			
  		});
  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
  		datos.p_IdUsuario = $scope.SesionIdUsuario;
  		datos.p_IdModulo = 'ORD';
  		datos.p_Ordenes = ordenes;
  		
  		bcsServicioOrden.RutearOrdenes(datos).$promise.then(function(result){
  			var idModal = bcsComunes.ventanaMsg({
					mensaje: 'Operacion Exitosa',
					conflictos:util.parseServerResponse(result)
				}, $scope.idVentana);
			bcsComunes.ventanaPromesa(idModal).then(function(){
				$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
				bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			});
  		},function(result){
  			var idModal = bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_ERROR_RUTEAR_ORDENES'),
				conflictos:util.parseServerResponse(result)
			}, $scope.idVentana);
			bcsComunes.ventanaPromesa(idModal).then(function(){
				bcsComunes.ventanaOverlay($scope.idVentana,false);	
				$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
			});
  			
  		});
	};

	
	
	$interval(function(){
		if ($scope.refrescoAutomatico){
			var audio = new Audio('sounds/beep-07.mp3');
			if($scope.apiGridOrdenes.dataSource.data().length=== 0 || $scope.apiGridOrdenes.dataSource === undefined){
				$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
			}else{
				var seleccionado = $scope.ordenesSeleccionadas;
				var ds = bcsConfiguracionOrdenesV2.getDataSourceConsultarOrdenesPorNegociar();
				ds.definition.filter = bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.filtros,'ORDEN');
				ds = new bcs.DataSource(ds);
				
				ds.fetch().then(function(){
					var dataNueva = ds.data();
					var actualizaGrilla = false;

					if ($scope.apiGridOrdenes.dataSource.data().length>0){
						dataActual = $scope.apiGridOrdenes.dataSource.data();
						
						actualizaGrilla = buscarSiExistenCambios(dataNueva,dataActual);
						
						if(actualizaGrilla){
							refrescar(audio);
							return;
						}
						
						actualizaGrilla = buscarSiExistenCambios(dataActual,dataNueva);
						
						if(actualizaGrilla){
							refrescar(audio);
							return;
						}
					}

				});
			}
		}
	},8000);
	
	var refrescar = function(audio){
		audio.play();
		$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
		$scope.ordenesSeleccionadas= seleccionado;
	}
	
	var buscarSiExistenCambios = function(data,dataComparacion){
		var existenCambios = false;
		data.forEach(function(valor){
			let encontrado = dataComparacion.filter(function(valorComparar){ 
				return valor.IdOrden===valorComparar.IdOrden
			});

			if(encontrado.length===0){
				existenCambios = true;
			}
		});
		
		return existenCambios;
	}
	
	$scope.filtrosTipoInstrumento = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-instrumento');
	$scope.filtrosOperacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operacion');
	$scope.filtrosTipoCanalDistribucionBolsa = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-canal-distribucion');
	$scope.filtrosOperadorCarteraInversion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operador-cartera-inversion');
	$scope.filtrosOperadorCarteraEjecutivo = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operador-cartera-ejecutivo');
	$scope.filtrosMonedaNegociacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-moneda-negociacion');
	$scope.limpiarFiltros();
  
	
});
