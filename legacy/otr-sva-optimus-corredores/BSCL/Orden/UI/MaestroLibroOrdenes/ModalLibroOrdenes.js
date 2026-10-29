angular.module('ModuloOrdenes').controller('ModalLibroOrdenesCtrl',function($scope,$translate,bcsComunes,$resource) {

	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/
		
		var modlo = this;
		
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		modlo.LibroOrdenes			= $.extend({},parametros.TipoLibSel);
		modlo.LibroOrdenesOriginal	= $.extend({},parametros.TipoLibSel);
		
		modlo.IdBotonAceptar = 'ModalLibroOrdenes_aceptar' + parametros.AccionBoton;
		
		modlo.EtiquetasTipoSubasta	= {COLOCADORA: "UI_COLOCADORA", COCOLOCADORA : "UI_COCOLOCADORA"};
		modlo.ValoresTipoSubasta	= {COLOCADORA: false, COCOLOCADORA : false};
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/
		if(parametros.TipoLibSel){
			//modificacion
			//modlo.titulo = 'UI_MODIFICAR_LIBRO_ORDENES';
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_LIBRO_ORDENES'));
			
			modlo.LibroOrdenes = {};
			modlo.LibroOrdenes.IdLibroOrdenes		= parametros.TipoLibSel.IdLibroOrdenes;
			modlo.LibroOrdenes.NombreLibroOrdenes	= parametros.TipoLibSel.NombreLibroOrdenes;
			modlo.LibroOrdenes.IdTipoLibroOrden		= parametros.TipoLibSel.IdTipoLibroOrden;
			modlo.LibroOrdenes.IdEstado				= parametros.TipoLibSel.IdEstado;
			modlo.LibroOrdenes.AsignacionAutomatica	= parametros.TipoLibSel.AsignacionAutomatica === 'S' ? true : false;
			modlo.LibroOrdenes.IdTipoAsignacion		= parametros.TipoLibSel.IdTipoAsignacion;
			modlo.LibroOrdenes.Prioridad			= Number(parametros.TipoLibSel.Prioridad);
			modlo.LibroOrdenes.LibroPorDefecto		= parametros.TipoLibSel.LibroPorDefecto === 'NO' ? false : true;
			modlo.buscarInstrumento 				= parametros.TipoLibSel.IdInstrumento
			modlo.LibroOrdenes.FechaApertura		= parametros.TipoLibSel.FechaApertura;
			modlo.LibroOrdenes.FechaCierre			= parametros.TipoLibSel.FechaCierre;
			modlo.LibroOrdenes.IdClienteOfertante	= parametros.TipoLibSel.IdClienteOfertante;
			modlo.LibroOrdenes.Cantidad				= parametros.TipoLibSel.Cantidad;
	
			modlo.LibroOrdenes.Precio				= parametros.TipoLibSel.Precio;
			modlo.LibroOrdenes.IdBolsa				= parametros.TipoLibSel.IdBolsa;
			modlo.ValoresTipoSubasta[parametros.TipoLibSel.TipoSubasta] = true;
			
			modlo.LibroOrdenesOriginal				= $.extend({},modlo.LibroOrdenes);
			modlo.LibroOrdenes.modoCrear			= false;
		}
		else{
			//modlo.titulo = 'UI_AGREGAR_LIBRO_ORDENES';
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_LIBRO_ORDENES'));
			modlo.LibroOrdenesOriginal.IdTipoLibroOrden = $scope.SesionValoresDefectoSistema.IdTipoLibroOrden;
			modlo.LibroOrdenesOriginal.IdEstado = $scope.SesionValoresDefectoSistema.IdEstadoLibroOrden;
			modlo.LibroOrdenes = {};
			modlo.LibroOrdenes.modoCrear = true;
		}
		
		modlo.aceptar = function(){
			//modificacion
			if(!modlo.LibroOrdenes.modoCrear){
				var datos = {
					p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
					p_IdUsuario			: $scope.$root.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_UsaReglasNegocio	: 'N',
					
					p_IdLibroOrdenes		: modlo.LibroOrdenes.IdLibroOrdenes,
					p_NombreLibroOrdenes	: modlo.LibroOrdenes.NombreLibroOrdenes,
					p_IdTipoLibroOrden		: modlo.LibroOrdenes.IdTipoLibroOrden,
					p_IdEstado				: modlo.LibroOrdenes.IdEstado,
					p_AsignacionAutomatica	: modlo.LibroOrdenes.AsignacionAutomatica ? 'S' : 'N',
					p_IdTipoAsignacion		: modlo.LibroOrdenes.IdTipoAsignacion ? modlo.LibroOrdenes.IdTipoAsignacion : '',
					p_Prioridad				: modlo.LibroOrdenes.Prioridad,
	
					p_Bolsa					: modlo.LibroOrdenes.idBolsaSeleccionada != false ? modlo.LibroOrdenes.idBolsaSeleccionada : modlo.LibroOrdenes.IdBolsa,
					p_Precio				: modlo.LibroOrdenes.Precio,
					p_TipoSubasta			: (modlo.ValoresTipoSubasta.COLOCADORA) ? "COLOCADORA" : (modlo.ValoresTipoSubasta.COCOLOCADORA) ? "COCOLOCADORA" : "",
					
					p_IdInstrumento			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S' && modlo.LibroOrdenes.instrumento? modlo.LibroOrdenes.instrumento.IdInstrumento || null:null,
					p_IdClienteOfertante	: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.IdClienteOfertante || null:null,
					p_Cantidad				: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.Cantidad || null:null,
					p_IdPlantillaCobros		: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.IdPlantillaCobros || null:null,
					p_FechaApertura			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? util.dateSPdate(modlo.LibroOrdenes.FechaApertura):null,
					p_FechaCierre			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? util.dateSPdate(modlo.LibroOrdenes.FechaCierre):null
					
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				modificarLibroOrdenes(datos).then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						var p = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos:util.parseServerResponse(result)
						},idVentana);
						
						bcsComunes.ventanaPromesa(p).then(function(){
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
							}
						})
	
						
						
						
					},
					function(result){
						bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos:util.parseServerResponse(result)
						},idVentana);
						
					});
			}
			//creacion
			else{
				var datos = {
					p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
					p_IdUsuario			: $scope.$root.SesionIdUsuario,
					p_IdModulo			: 'ORD',
					p_UsaReglasNegocio	: 'N',
					
					p_NombreLibroOrdenes	: modlo.LibroOrdenes.NombreLibroOrdenes,
					p_IdTipoLibroOrden		: modlo.LibroOrdenes.IdTipoLibroOrden,
					p_IdEstado				: modlo.LibroOrdenes.IdEstado,
					p_AsignacionAutomatica	: modlo.LibroOrdenes.AsignacionAutomatica ? 'S' : 'N',
					p_IdTipoAsignacion		: modlo.LibroOrdenes.IdTipoAsignacion ? modlo.LibroOrdenes.IdTipoAsignacion : '',
					p_Prioridad				: modlo.LibroOrdenes.Prioridad,
	
					p_Bolsa					: modlo.LibroOrdenes.idBolsaSeleccionada,
					p_Precio				: modlo.LibroOrdenes.Precio,
					p_TipoSubasta			: (modlo.ValoresTipoSubasta.COLOCADORA) ? "COLOCADORA" : (modlo.ValoresTipoSubasta.COCOLOCADORA) ? "COCOLOCADORA" : "",
					
					
					p_IdInstrumento			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S' && modlo.LibroOrdenes.instrumento? modlo.LibroOrdenes.instrumento.IdInstrumento || null:null,
					p_IdClienteOfertante	: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.IdClienteOfertante || null:null,
					p_Cantidad				: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.Cantidad || null:null,
					p_IdPlantillaCobros		: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S'? modlo.LibroOrdenes.IdPlantillaCobros || null:null,
					p_FechaApertura			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S' && modlo.LibroOrdenes.FechaApertura? util.dateSPdate(modlo.LibroOrdenes.FechaApertura):null,
					p_FechaCierre			: modlo.TipoLibroObj.EsLibroOfertaPublica === 'S' && modlo.LibroOrdenes.FechaCierre? util.dateSPdate(modlo.LibroOrdenes.FechaCierre):null
					
				};
				
				
				bcsComunes.ventanaOverlay(idVentana,true);
				crearLibroOrdenes(datos).then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						var p = bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos:util.parseServerResponse(result)
						},idVentana);
						
						bcsComunes.ventanaPromesa(p).then(function(){
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
							}
						})
						
					},
					function(result){
						bcsComunes.ventanaMsg({
							mensaje: '',
							conflictos:util.parseServerResponse(result)
						},idVentana);
						
					});
			}
		};
		
		modlo.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	
	/*********************************************
	 * DATASOURCES
	 *********************************************/
	
		
	/*********************************************
	 * COMBOBOX
	 *********************************************/
		
		modlo.conf = {};
		modlo.conf.oConfiguracionTipoAsignacion={
			tipo : "combobox-tipo-asignacion",
			configuracion : {},
			filtros : {
				SoportaAsignacionAutomatica : {"field" : "SoportaAsignacionAutomatica", "operator" : "eq", "value" : 'S'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		modlo.conf.oSocioNegocio={
			filtros: {
				IdEmpresa : { field: "IdEmpresa",operator: "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : { field: "EsCliente",operator: "eq", "value"   :  'UI_SI'}
			}
		};
		
		
		modlo.conf.oBolsa = {
			defecto : $scope.SesionBolsaDefectoSistema,
			filtros : {
				IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : 0 },
			}
		};
	
	/*********************************************
	 * WATCH
	 *********************************************/	
		$scope.$watch('modlo.LibroOrdenes.instrumento.IdInstrumento',function(newval,oldval) {	
			if (modlo.LibroOrdenes.instrumento != undefined && modlo.LibroOrdenes.instrumento.IdInstrumento){
				modlo.conf.oBolsa.filtros.IdInstrumento.value = modlo.LibroOrdenes.instrumento.IdInstrumento;
				modlo.conf.oBolsa.filtros.read = true;
			}
			bcsComunes.ventanaAutoHeight(idVentana);
		});	
		
	/*********************************************
	 * GRILLAS
	 *********************************************/
		
		
	/*********************************************
	 * FUNCIONES
	 ********************************************/
		
		var crearLibroOrdenes = function(p_datos){
			var urlCrearLibroOrdenes = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=211";
			var r = $resource("", {}, {'urlCrearLibroOrdenes': {method:"POST", url:urlCrearLibroOrdenes}});
			var post = r.urlCrearLibroOrdenes(p_datos);
			
			return post.$promise;
		};
		
		var modificarLibroOrdenes = function(p_datos){
			var urlModificarLibroOrdenes = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=212";
			var r = $resource("", {}, {'urlModificarLibroOrdenes': {method:"POST", url:urlModificarLibroOrdenes}});
			var post = r.urlModificarLibroOrdenes(p_datos);
			
			return post.$promise;
		};
	
	//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
	});