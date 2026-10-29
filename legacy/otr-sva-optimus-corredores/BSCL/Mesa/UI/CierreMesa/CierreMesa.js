(function() {
	angular.module('ModuloMesa').controller('CierreMesaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsAlerta,OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cMesa 		= this;
		cMesa.idVentana	= bcsComunes.ventanaWinId($scope);
		cMesa.params	= bcsComunes.ventanaParams(cMesa.idVentana);
		cMesa.win		= {};
		cMesa.grilla	= {};
		cMesa.cmbbx		= {};
		cMesa.filtro	= {};
		cMesa.fn		= {};
		cMesa.ds		= {};
		
		cMesa.data = [];
		cMesa.dataFiltros = {};
		cMesa.win.TodosTpoPrd = false;
		cMesa.modulo = null;
		cMesa.filtro.IdTipoProductoMesaModuloDefecto = '0';
		if(cMesa.modulo != undefined || cMesa.modulo != ''){
			cMesa.modulo = cMesa.params.modulo;
		}
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		cMesa.ds.CierreModulo = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1558",
						dataType: "json"
					}
				},
				schema: {
					model: { 
						fields: {		 	
							IdCREModuloEmpresa	: { type: "number"},
							IdModulo			: { type: "string"},
							IdEstado			: { type: "string"},
							TipoEstado			: { type: "string"}
						}
					}
				}
			}
		});
		
		
		cMesa.ds.Mesas = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=452",
						dataType: "json"
					}
				},
				schema: {
					model: { 
						fields: {		 	
							IdMesa				: { type: "number"},
							Codigo				: { type: "string"},
							Nombre				: { type: "string"},
							IdTipoProducto		: { type: "string"},
							IdEstadoMesa		: { type: "string"},
							IdEstadoRegistro	: { type: "string"},
							FechaModificacionFrmt:{ type: "string"}
						}
					}
				}
			}
		});
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		cMesa.cmbbx.TipoProductoMesaModulo={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		
		if(cMesa.modulo != null){
			cMesa.cmbbx.TipoProductoMesaModulo.filtros['IdTipoProductoMesaModulo'] = {field: "IdTipoProductoMesaModulo", operator: "eq", value: cMesa.modulo};
			cMesa.filtro.IdTipoProductoMesaModulo = cMesa.modulo;
			cMesa.filtro.IdTipoProductoMesaModuloDefecto = cMesa.modulo;
		}
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		cMesa.grilla.Mesas = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					//pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=452",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdMesa				: { type: "number"},
								Codigo				: { type: "string"},
								Nombre				: { type: "string"},
								IdTipoProducto		: { type: "string"},
								IdEstadoMesa		: { type: "string"},
								IdEstadoRegistro	: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "Nombre",			title: $translate.instant('UI_NOMBRE'),			width: "80px"	},
			    //{field: "IdTipoProducto",	title: $translate.instant("UI_TIPO_PRODUCTO"),	width: "100px"	},
			    {field: "IdEstadoMesa",		title: $translate.instant('UI_ESTADO'),			width: "70px"	}
			],
			//pageable	: true,
			//pageSize	: 100,
			autoBind	: false,
			height		: 420,
		    selectable 	: "row",
		    filterable	: true
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		cMesa.fn.buscar = function(){
			if(cMesa.filtro !== undefined && cMesa.filtro.IdTipoProductoMesaModulo !== undefined){
				bcsComunes.ventanaOverlay(cMesa.idVentana,true);
				
				cMesa.win.TodosTpoPrd = false;
				
				// se obtienen mesas por TipoProducto
				var data = [];
				var filterMesas = [ { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				                    { field: "IdTipoProductoMesaModulo",operator: cMesa.filtro.IdTipoProductoMesaModulo ? 'eq' : 'neq', value: cMesa.filtro.IdTipoProductoMesaModulo } ];
				cMesa.ds.Mesas.query({ filter: filterMesas }).then(function(){
					if(cMesa.ds.Mesas.view().length > 0){
						$.each(cMesa.ds.Mesas.data(),function(key,value){
							var aux = {};
							aux.IdMesa				= value.IdMesa;
							aux.NombreMesa			= value.NombreMesa;
							aux.IdEstadoMesa		= value.IdEstadoMesa;
							aux.MesaCerrada			= value.IdEstadoMesa === 'C' ? true : false;
							aux.MsjEstadoMesa		= value.IdEstadoMesa === 'C' ? $translate.instant("UI_MESA_CERRADA") : $translate.instant("UI_MESA_ABIERTA");
							aux.CodigoUsuario		= value.CodigoUsuario;
							aux.FechaModificacionFrmt=value.FechaModificacionFrmt;
							aux.TipoEstadoModulo	= value.TipoEstadoModulo;
							aux.ModuloMesaCerrado	= value.TipoEstadoModulo === 'C' ? true : false;
							aux.MsjEstadoModulo		= value.MsjEstadoModulo;
							
							data.push(aux);
						});
						cMesa.data = data;
						bcsComunes.ventanaOverlay(cMesa.idVentana,false);
					}
					else{
						bcsComunes.msg("error",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_NO_HAY_MESAS_CONFIGURADAS"));
						bcsComunes.ventanaOverlay(cMesa.idVentana,false);
					}
				},
	        	function(result){
	        		bcsComunes.msg("error",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_OPERACION_CON_ERRORES"));
	        		bcsComunes.ventanaOverlay(cMesa.idVentana,false);
	        	});
			}
		}
		
		
		cMesa.fn.limpiar = function(){
			cMesa.filtro.IdTipoProductoMesaModulo = 0;
			//cMesa.grilla.Mesa.dataSource.removeAll();
		}
		
		
		cMesa.fn.grabar = function(){
			bcsComunes.ventanaOverlay(cMesa.idVentana,true);
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'MRF';
	        parametrosSP.p_simularOperacion	= 'N';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
			
			var ContadorCierreMesa = 0;	        

	        if(cMesa.data.length){
	        	$.each(cMesa.data,function(key,value){
	        		value.IdEstadoMesa = value.MesaCerrada ? 'C' : 'A';
					ContadorCierreMesa = value.MesaCerrada ?  ContadorCierreMesa + 1 : ContadorCierreMesa;
				});

	        	parametrosSP.p_DatosMesas = cMesa.data;

				if(cMesa.dataFiltros.TipoProductoMesaModuloObj.IdTipoProductoMesa == "MEME"){	
					if(ContadorCierreMesa >= 1){
						t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("¿Cerrar Mesa si hay Operaciones Pendientes?"), cMesa.idVentana);
					}
					else{
						t = bcsAlerta.crear("INFO",$translate.instant("Mesa Abierta"), cMesa.idVentana);
					}
					t.then(function(result){
						parametrosSP.p_CierreMesa = result === OPCIONES_BCSALERTA.SI ? 'S' : 'N';
						bcsServiciosMesa.CierreAperturaMesa(parametrosSP).$promise.then(function(result) {
							if (result.P_OUT_ADVERTENCIASCONTADOR){
								bcsComunes.msg("warning",$translate.instant("UI_CIERRE_MESA"),$translate.instant(result.P_OUT_ADVERTENCIASDETALLE[0].Codigo));
							}
							else if (result.P_OUT_ERRORESCONTADOR){
								bcsComunes.ventanaMsg({
									mensaje: $translate.instant(result.P_OUT_ERRORESDETALLE[0].Codigo),
									conflictos:util.parseServerResponse(result)
								},cMesa.idVentana);
							}
							else{
								bcsComunes.msg("info",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_OPERACION_REALIZADA"));
								cMesa.fn.buscar();
							}
							bcsComunes.ventanaOverlay(cMesa.idVentana,false);
							console.log(result);
						},
						function(result){
							bcsComunes.msg("error",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_OPERACION_CON_ERRORES"));
							bcsComunes.ventanaOverlay(cMesa.idVentana,false);
						});
					});
				}
				else{
					bcsServiciosMesa.CierreAperturaMesa(parametrosSP).$promise.then(function(result) {
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning",$translate.instant("UI_CIERRE_MESA"),$translate.instant(result.P_OUT_ADVERTENCIASDETALLE[0].Codigo));
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.ventanaMsg({
								mensaje: $translate.instant(result.P_OUT_ERRORESDETALLE[0].Codigo),
								conflictos:util.parseServerResponse(result)
							},cMesa.idVentana);
						}
						else{
							bcsComunes.msg("info",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_OPERACION_REALIZADA"));
							cMesa.fn.buscar();
						}
						bcsComunes.ventanaOverlay(cMesa.idVentana,false);
						console.log(result);
					},
					function(result){
						bcsComunes.msg("error",$translate.instant("UI_CIERRE_MESA"),$translate.instant("UI_OPERACION_CON_ERRORES"));
						bcsComunes.ventanaOverlay(cMesa.idVentana,false);
					});
				}
	        }
	        else bcsComunes.ventanaOverlay(cMesa.idVentana,false);
		};
		
		
		$scope.$watch('cMesa.win.TodosTpoPrd',function(newValue, oldValue){
			if(newValue !== oldValue){
				$.each(cMesa.data,function(key,value){
					if(!value.ModuloMesaCerrado){
						value.IdEstadoMesa	= cMesa.win.TodosTpoPrd ? 'C' : 'A';
						value.MesaCerrada	= cMesa.win.TodosTpoPrd;
					}
				});
				
			}
		});
		
	});
}());