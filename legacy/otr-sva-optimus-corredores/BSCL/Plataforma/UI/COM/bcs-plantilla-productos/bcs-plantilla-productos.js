/*************************************************************
 * Purpose	: Directiva Plantilla Productos
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').directive('bcsPlantillaProductos',
	function($translate,$rootScope,bcsComunes,bcsConfiguracionMatrizPerfilComercial,servicioPerfilProductos){
		return {
			restrict: 'E',
	    	replace: false,
			scope : {
				idPlantilla 			: '=?',
				modo 		 			: '=?',// MP: MantenedorPlantillas, PB: PortafolioBase, PE: PortafolioExtra
				accionBtn				: '=?', // accion gatillada por ventana: E: edigar, G:guardar, C:Cancelar
				//salida para modo Extra
				guardarOk				: '=?',
				//salida para verificar modo de lectura de la directiva
				modoLectura				: '=?',
				cancelarOk				: '=?',
				finalizarOk				: '=?',
				perfilCargado			: '=?',
			},
	    	templateUrl: '../../Plataforma/UI/COM/bcs-plantilla-productos/bcs-plantilla-productos.html', 
	    	link: function (scope, element, attrs, fn) {
	    		console.log("directiva plantilla productos");
				/*********************************************
				 * VARIABLES Y CONTEXTOS
				 ********************************************/
				var overlay 					= bcsComunes.overlay;
				scope.win 						= scope.win 	|| {};
				scope.win.modoEditar 			= false;
				scope.win.muestraEditar 		= false;
				scope.idVentana					= bcsComunes.ventanaWinId(scope);
				
				scope.datosCabeceraAntes 		= {};
				scope.datosDetalleAntes 		= {};
				scope.datosCabeceraDespues 		= {};
				scope.datosDetalleDespues 		= {};
				scope.guardarOk					= false; //salida
	
				scope.cmbbx						= scope.cmbbx 	|| {};
				scope.ds						= scope.ds 		|| {};
				scope.grilla					= scope.grilla 	|| {};
				scope.fn						= scope.fn 		|| {};
				
				scope.modoLectura	= true;
				
				scope.parametroFiltroProductoPorCatInv = '';
	
				/*********************************************
				 * ACCIONES DE PANTALLA
				 ********************************************/					
				
				//sólo habilita los demás botones!
				scope.win.editar = function() {
					scope.win.modoEditar = true;
					scope.modoLectura = false;
					scope.guardarOk = false;
				};
				
				scope.win.cancelar = function() {
					scope.fn.limpiarPantallaPerfilProductos();
					scope.idPlantilla = '';
					scope.win.perfilCargado = false;
					scope.win.modoEditar = false;
					scope.modoLectura = true;
					scope.guardarOk = false;
				};
				
				scope.win.finalizarEdicion = function() {
					scope.finalizarOk = true;
					scope.win.modoEditar = false;
					scope.win.perfilCargado = false;
					scope.modoLectura = true;
					scope.accionBtn = undefined;
				};
				
				//CABECERA
				scope.win.administrarCabecera = function(perfilProductos) {
					//var id1 = bcsComunes.ventanaAbrir("ModalAdministrarPlantillaProductos",{perfil:perfilProductos},false,true); 
					var id1 = bcsComunes.ventanaAbrir("ModalAdministrarPlantillaProductos",{perfil:perfilProductos},false,false,scope.idVentana); 
		            var promesa=bcsComunes.ventanaPromesa(id1);                               
		            $.when(promesa).then(function(respuesta){ 
				if(respuesta){
		            		scope.fn.cargaDatosPantalla(respuesta.IdPerfilProductos);
		            		scope.guardarOk = true;
				}
//		            	if(respuesta.aceptar === true){
//					scope.fn.limpiarPantallaPerfilProductos();
//				}
//				else {console.log("usuario cancela en el PopUp!");}
		            });
				};
				
				//DETALLE
				scope.win.modificarDetalle = function(seleccionado){
					if(seleccionado && seleccionado.IdTipoProducto) scope.win.administrarDetalle(seleccionado);
				};
				
				scope.win.administrarDetalle = function(seleccionado) {
					bcsComunes.ventanaOverlay(scope.idVentana,true);
					var id1 = bcsComunes.ventanaAbrir("ModalAdministrarPlantillaProductosDetalle",{
						lineaDetalle:seleccionado,
						detallePerfil:_.clone(scope.grilla.GrillaMatrizProductos.dataSource.data()),
						IdPerfilProductos:scope.idPlantilla,
						parametroFiltroProductoPorCatInv:scope.parametroFiltroProductoPorCatInv},false,false); 
		            var promesa=bcsComunes.ventanaPromesa(id1);                               
		            $.when(promesa).then(function(respuesta){ 
		            	console.log(respuesta);
		            	bcsComunes.ventanaOverlay(scope.idVentana,false);
		            	
	            		scope.grilla.GrillaMatrizProductos.dataSource.read();
	            		//scope.fn.cargaDatosPantalla(scope.idPlantilla);
	            		scope.guardarOk = true;
		            });
				};
				
				/*********************************************
				 * DATASOURCES
				 *********************************************/
				scope.ds.PerfilProductos = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering : true,
						transport: {
							read: { 
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1334",
								dataType: "json"
							}
						},
						schema: {
							model: { fields: {		 	
								IdPerfilProductos 			: { type: "string"},
								DescripcionPerfilProductos	: { type: "string"},
								IdCategoriaInversionista 	: { type: "string"},
								FechaCreacion 				: { type: "date"},
								FechaModificacion 			: { type: "date"},
								EstadoPerfilProductos 		: { type: "string"},
								PorDefecto 					: { type: "string"},
								IdEmpresa 					: { type: "int"},
							} }
						}
					}
				});
				
				
				//Obtener parametro : productos Opciones y plantillas Categoría Inversionista Arriesgado
				var ConsultarConfiguracion = new bcs.DataSource({
					type 		: "odata",
					definition 	: {
						serverFiltering	: true,
						transport: {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
								dataType : "json",
							}
						},
						schema: {
							model: {
								fields: {
									IdEmpresa		: {type: "number"},
									CodigoParametro	: {type: "string"},
									Descripcion		: {type: "string"},
									Valor			: {type: "string"}
								}
							}
						}
					},
				});
				
				
				/*********************************************
				 * COMBOBOX
				 *********************************************/
				scope.cmbbx.CategoriaInversionista={
					tipo 			: "combobox-categoria-inversionista",
					filtros 		: {
						IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $rootScope.SesionIdEmpresa }
					},
				};
				
				/*********************************************
				 * GRILLAS
				 *********************************************/
				scope.grilla.GrillaMatrizProductos = {
					dataSource		: new bcs.DataSource({
						type 		: "odata", //Los tipos posibles son: odata, call, array y object
						definition 	: {
							serverFiltering : true, // Filtrado de datos en el servidor.
							serverPaging 	: true, // Paginado de los datos en el servidor.
							serverSorting 	: true, // Ordenamiento de los datos en el servidor.
							sortable 		: true,
							pageSize 		: 200,
							transport: {
								read: {
									url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1328",
									dataType: "json"
								}
							},
							filter: { field: 'IdPerfilProductos', operator: "eq", value: '-1'},
							
							schema 			: {
								model	: { fields	: bcsConfiguracionMatrizPerfilComercial.getEsquemaGridMatrizProductos() }
							}
						}
					}),
					columns	: [
		   				{field: "TipoProducto",width: "180px",title: $translate.instant("UI_PRODUCTO"),attributes : {style : "text-align:left;"}},
		   				{field: "TipoOperacion",width: "100px",title: $translate.instant("UI_TIPO_OPERACION"),attributes : {style : "text-align:left;"}},
		   				{field: "CanalIngreso",width: "100px",title: $translate.instant("UI_CANAL"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
		   				{field: "PermiteRuteo",width: "100px",title: $translate.instant("UI_PERMITE_RUTEO"),format:"{0:n0}",attributes : {style : "text-align:left;"}, values: [{text:"Si", value: "S"}, {text:"NO", value: "N"}]},
		   				//{field: "IdMoneda",width: "100px",title: $translate.instant("UI_MONEDA"),format:"{0:c}",attributes : {style : "text-align:left;"}},
		   				{field: "NombreEstadoProducto",width: "100px",title: $translate.instant("UI_HABILITADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }},
		   				{field: "IdGrupoActivos",width: "100px",title: $translate.instant("UI_GRUPO_ACTIVOS"), attributes: { style : "text-align:left;" }},
			   		],			
					sortable			: false,
					groupable			: false,
					pageable			: false,
					autoBind			: true,  //comentar
					height				: 280,
					userSettings		: true,
					navigatable			: true,
					excel : {
						allPages: true,
					},
				    selectable 			: "row"
				 };
				
				/*********************************************
				 * FUNCIONES
				 *********************************************/
				scope.fn.cargaDatosPantalla = function(IdPerfilProductos){
					if (IdPerfilProductos !== undefined && IdPerfilProductos !== ''){
						var filterPerfilProductos = [ { field: "IdPerfilProductos", operator: "eq", value: IdPerfilProductos } ];
						//CABECERA
						scope.idPlantilla = IdPerfilProductos;
						scope.ds.PerfilProductos.query({ filter: filterPerfilProductos }).then(function(){
							if(scope.ds.PerfilProductos.data().length > 0) {
								scope.grilla.GrillaMatrizProductos.dataSource.removeAll();
								scope.datosCabeceraAntes   				= _.clone(scope.ds.PerfilProductos.data()[0]);
								scope.datosCabeceraDespues 				= _.clone(scope.ds.PerfilProductos.data()[0]);
								scope.datosCabeceraAntes.Habilitado 	= (scope.ds.PerfilProductos.data()[0].EstadoPerfilProductos==='ACT')?true:false;
								scope.datosCabeceraAntes.PorDefecto 	= (scope.ds.PerfilProductos.data()[0].PorDefecto==='S')?true:false;
								scope.datosCabeceraDespues.Habilitado 	= (scope.ds.PerfilProductos.data()[0].EstadoPerfilProductos==='ACT')?true:false;
								scope.datosCabeceraDespues.PorDefecto 	= (scope.ds.PerfilProductos.data()[0].PorDefecto==='S')?true:false;
								
								scope.win.perfilCargado = true;
								scope.perfilCargado 	= true;
							}
							else { // esto no debiera ocurrir! Analizar insert de perfiles extras para clientes antiguos!.
								var perfilProductos = {};
								perfilProductos.p_IdEmpresa 				= scope.$root.SesionIdEmpresa; 
								perfilProductos.p_IdUsuario 				= scope.$root.SesionIdUsuario;
								perfilProductos.p_IdModulo 				= "PLA";
								perfilProductos.p_UsaReglasNegocio 		= "S";
								perfilProductos.p_IdPerfilProductos 		= IdPerfilProductos;
								perfilProductos.p_DescripcionPerfilProductos 	= IdPerfilProductos;
								perfilProductos.p_IdCategoriaInversionista = 0; // irrelevante pew!
								perfilProductos.p_EsPerfilPersonalizado	= 'S';
								perfilProductos.p_IdEstado  				= 'ACT';
								perfilProductos.p_PorDefecto 				= 'N';
								servicioPerfilProductos.savePerfilProductos(perfilProductos,function (result){
									if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
										console.log("creadoPerfilExtra",result);
										scope.win.perfilCargado = true;
										scope.perfilCargado 	= true;
										scope.grilla.GrillaMatrizProductos.dataSource.filters(filterPerfilProductos);
									}
									else {
										console.log("error",result);
									}
								}, function(resError){
									console.log("error",resError);
								});
							}
							//DETALLE
							scope.grilla.GrillaMatrizProductos.dataSource.filter(filterPerfilProductos);							
							scope.win.editar();
							
							fnConsultarConfiguracion();
						});	
					}
					else {
						console.log("IdPerfilProductos undefined");	
					}
				};
				
				scope.fn.limpiarPantallaPerfilProductos = function(){
					scope.datosCabeceraAntes 		= undefined;
					scope.datosCabeceraDespues 		= undefined;
					scope.grilla.GrillaMatrizProductos.dataSource.removeAll();
					scope.datosDetalleAntes 		= undefined;
					scope.datosDetalleDespues 		= undefined;
					scope.guardarOk = false;
					scope.cancelarOk = false;
					scope.perfilCargado = false;
				};
				
				
				var fnConsultarConfiguracion = function(){
					var filtrosConsultarConfiguracion = [	{ field: "IdEmpresa",		operator: "eq", value: scope.$root.SesionIdEmpresa },
					                                        { field: "CodigoParametro",	operator: "eq", value: 'COM.FiltroProductoPorCatInv' } ];
					ConsultarConfiguracion.query({ filter: filtrosConsultarConfiguracion }).then(function(){
						if(ConsultarConfiguracion.view().length > 0){
							scope.parametroFiltroProductoPorCatInv = ConsultarConfiguracion.view()[0].Valor || '';
						}
					});
				};
						
				/*********************************************
				 * FUNCIONES WATCH
				 *********************************************/
				scope.$watch('idPlantilla',function(newval,oldval){
						if(newval !== undefined){
							scope.fn.cargaDatosPantalla(newval);
						}
				});
				
				scope.$watch('win.modoLectura',function(newval,oldval){
					if(newval !== undefined){
						scope.modoLectura = newval;
					}
				});
				
				
				scope.$watch('accionBtn',function(newval,oldval){
					if(newval !== undefined && scope.modo === 'PE'){
						switch(newval) {
					    case 'E':
					    		scope.finalizarOk = false;
					    		scope.grilla.GrillaMatrizProductos.dataSource.read();
					    		scope.win.editar();
					        break;
					    case 'C':
					    	scope.win.cancelar();
					    	if(scope.idPlantilla!=='0') scope.fn.cargaDatosPantalla(scope.idPlantilla);
					        break;
					    default:
					    	scope.win.modoLectura = true;
						};
					}
				});
	
				/*********************************************
				 * GO!!!
				 *********************************************/
				if(scope.idPlantilla) scope.fn.cargaDatosPantalla(scope.idPlantilla);
				switch(scope.modo) {
				    case 'MP':
				    	scope.win.modoEditar = false;
				        break;
				    case 'PB':
				    	scope.win.modoLectura = true;
				        break;
				    case 'PE':
				    	scope.win.modoLectura = true;
				        break;
				    default:
				        console.log("directiva mal inicializada");
				};
	    	}
		}
	});
}());
//------------------------- FIN CONTROLLER --------------------------
