angular.module('ModuloPlataforma').directive('bcsPlantillaLimites',
function($q,$translate,bcsComunes,$timeout,$state,bcsConfiguracionMatrizPerfilComercial,bcsAlerta,OPCIONES_BCSALERTA,servicioPlantillaLimites){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idVentana				: '=?',
			idPlantilla 			: '=?',
			modo 		 			: '=?',// MP: MantenedorPlantillas, PB: PortafolioBase, PE: PortafolioExtra
			accionBtn				: '=?', // accion gatillada por ventana: E: editar, G:guardar, C:Cancelar
			//salida para modo Extra
			guardarOk				: '=?',
			//salida para verificar modo de lectura de la directiva
			modoLectura				: '=?',
			cancelarOk				: '=?',
			finalizarOk				: '=?',
			plantillaCargado		: '=?',
			idEntidad				: '=?'
		},
    	templateUrl: '../../Plataforma/UI/COM/bcs-plantilla-limites/bcs-plantilla-limites.html', 
    	link: function (scope, element, attrs, fn) {

    		console.log("directiva plantilla limites");

			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			scope.win 					= scope.win 	|| {};
			scope.win.modoEditar 		= false;
			scope.win.muestraEditar 	= false;
			
			scope.datosCabeceraAntes 	= {};
			scope.datosDetalleAntes 	= {};
			scope.datosCabeceraDespues 	= {};
			scope.datosDetalleDespues 	= {};
			scope.guardarOk				= false; //salida
			scope.dataSolicitudCambio	= {};
			scope.dataSolicitudCambio.CambiosPendientes		= false;
			scope.dataSolicitudCambio.CambioDetallePendiente= false;

			scope.cmbbx					= scope.cmbbx 	|| {};
			scope.ds					= scope.ds 		|| {};
			scope.grilla				= scope.grilla 	|| {};
			scope.fn					= scope.fn 		|| {};
			
			scope.modoLectura = true;

			
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
				scope.fn.limpiarPantallaPlantillaLimites();
				scope.idPlantilla = '0';
				scope.win.modoEditar = false;
				scope.win.plantillaCargado = false;
				scope.modoLectura = true;
				scope.guardarOk = false;
			};
			
			scope.win.finalizarEdicion = function() {
				scope.finalizarOk = true;
				scope.win.modoEditar = false;
				scope.modoLectura = true;
				scope.accionBtn = undefined;
			};
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			scope.ds.PlantillaLimites = new kendo.data.DataSource({
				type: "odata",
				serverFiltering : true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=362",
						dataType: "json"
					}
				},
				schema: {
					model: { 
						fields: {		 	
							IdPlantillaLimite			: { type: "string"},
							DescripcionPlantillaLimite	: { type: "string"},
							IdEstado					: { type: "string"},
							PorDefecto					: { type: "string"},
							IdEmpresa					: { type: "number"}
						}
					}
				}
			});
			
			
			scope.ds.Configuracion = new kendo.data.DataSource({
				type: "odata",
				serverFiltering : true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=408",
						dataType: "json"
					}
				},
				schema: {
					model: { 
						fields: {		 	
							IdEmpresa	: { type: "number"},
							Codigo		: { type: "string"},
							Descripcion	: { type: "string"},
							Valor		: { type: "string"}
						}
					}
				}
			});
			
			
			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			
			/*********************************************
			 * GRILLAS
			 *********************************************/
			scope.grilla.grillaPlantillaLimites = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=363",
								dataType: "json"
							}
						},
						filter: { field: 'IdPlantillaLimite', operator: "eq", value: ''},
						schema: {
							model: {
								fields: {
									IdPlantillaLimite	: { type: "string"},
									IdEmpresa			: { type: "number"},
									IdLimite			: { type: "number"},
									Nombre				: { type: "string"},
									NombreClasificacionTipoInstrumento: { type: "string"},
									TipoInstrumento		: { type: "string"},
									TipoOperacion		: { type: "string"},
									CanalIngreso		: { type: "string"},
									Moneda				: { type: "string"},
									Porcentaje			: { type: "number"},
									MontoTotal			: { type: "number"},
									NombreEstadoLimite	: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
				    {field: "Nombre",				title: $translate.instant("UI_NOMBRE_LIMITE"),		width: "140px"},
				    {field: "NombreClasificacionTipoInstrumento",title: $translate.instant("UI_CLASIFICACION"),width: "150px"},
				    {field: "TipoInstrumento",		title: $translate.instant("UI_TIPO_INSTRUMENTO"),	width: "140px"},		
					{field: "TipoOperacion",		title: $translate.instant("UI_TIPO_OPERACION"),		width: "140px"},
					{field: "CanalIngreso",			title: $translate.instant("UI_CANAL"),				width: "60px"},
					{field: "Moneda",				title: $translate.instant("UI_MONEDA"),				width: "90px"},
					{field: "Porcentaje",			title: $translate.instant("UI_PORCENTAJE"),			width: "50",	template: "<label> #: (util.bcs_formato(Porcentaje,'ORD','TASA', IdMoneda,'','')).valor #</label>"},
					{field: "MontoTotal",			title: $translate.instant("UI_LINEA_ASIGNADA"),		width: "80px",	template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO',IdMoneda,'','')).valor #</label>"},
					{field: "NombreEstadoLimite",	title: $translate.instant("UI_ESTADO"),				width: "50px"}
				],
				sortable			: false,
				pageable			: true,
				autoBind			: true,
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
			
			//Se obtiene valor Configuracion: IdClasificacionTipoInstrumento o IdTipoInstrumento
			var filterConfiguracion = [ { field: "IdEmpresa",	operator: "eq", value: scope.$root.SesionIdEmpresa },
			                            { field: "Codigo",		operator: "eq", value: 'FILTROPLANTILLA'} ];
			scope.ds.Configuracion.query({ filter: filterConfiguracion }).then(function(){
				if(scope.ds.Configuracion.view().length > 0) {
					scope.win.filtroPlantilla = scope.ds.Configuracion.view()[0].Valor;
				}
			});
			
			
			//Se cargan datos de plantilla, detalle y cambios (cab y det)
			scope.fn.cargaDatosPantalla = function(IdPlantillaLimite){
				if (IdPlantillaLimite !== undefined){
					scope.idPlantilla = IdPlantillaLimite;
					var filterPlantillaLimites = [ { field: "IdPlantillaLimite", operator: "eq", value: IdPlantillaLimite } ];
					scope.ds.PlantillaLimites.query({ filter: filterPlantillaLimites }).then(function(){
						//CABECERA
						if(scope.ds.PlantillaLimites.view().length > 0) {
							if(scope.grilla.grillaPlantillaLimites != undefined) scope.grilla.grillaPlantillaLimites.dataSource.removeAll();
							
							scope.datosCabeceraAntes   				= _.clone(scope.ds.PlantillaLimites.view()[0]);
							scope.datosCabeceraDespues 				= _.clone(scope.ds.PlantillaLimites.view()[0]);
							scope.datosCabeceraAntes.IdEstado		= (scope.ds.PlantillaLimites.view()[0].IdEstado		==='ACT') ? true : false;
							scope.datosCabeceraAntes.PorDefecto 	= (scope.ds.PlantillaLimites.view()[0].PorDefecto	==='S')	  ? true : false;
							scope.datosCabeceraDespues.IdEstado		= (scope.ds.PlantillaLimites.view()[0].IdEstado		==='ACT') ? true : false;
							scope.datosCabeceraDespues.PorDefecto	= (scope.ds.PlantillaLimites.view()[0].PorDefecto	==='S')	  ? true : false;
							
							scope.win.plantillaCargado	= true;
							scope.plantillaCargado		= true;
						}
						
						//DETALLE
						filterPlantillaLimites = [ { field: "IdPlantillaLimite", operator: "eq", value: IdPlantillaLimite },
						                           { field: "UI_Limite", operator: "eq", value: 'PLANTILLA' }];
						if(scope.grilla.grillaPlantillaLimites != undefined){
							scope.grilla.grillaPlantillaLimites.dataSource.filter(filterPlantillaLimites);
							//scope.grilla.grillaPlantillaLimites.refresh();
						}
						
						// Se consultan solicitudes de cambios Pendientes en CABECERA
						var filterSol = new Array();
						filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'PLANTILLALIMITE' },
								  	   { field: "IdEntidadOrigen", operator: "eq", value: scope.idPlantilla},
									   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
						var SolicitudDeCambioDatasource = new kendo.data.DataSource({
							type: "odata",
							serverFiltering:true,
							transport: {
								read: {
									url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1538",
									dataType: "json"
							    }
							},
							filter: filterSol,
							schema: {
								model: {
									fields : {
										IdSolicitudCambio		: { type: "number"},
										IdSecuenciaCambio		: { type: "number"},
										IdTipoEntidad			: { type: "string"},
										IdEntidadOrigen			: { type: "string"},
										CampoModificado			: { type: "string"},
										TipoDato				: { type: "string"},
										ValorAntiguo			: { type: "string"},
										ValorNuevo				: { type: "string"},
										FechaSolicitud			: { type: "string"},
										FechaRevision			: { type: "string"},
										IdEstadoAutorizacion	: { type: "string"}
									}
								}
							}
						});
						SolicitudDeCambioDatasource.query().then(function(result){
							if(SolicitudDeCambioDatasource.data().length > 0){
								scope.dataSolicitudCambio = $.extend({},SolicitudDeCambioDatasource.data());
								scope.dataSolicitudCambio.CambiosPendientes=true;
							}
							
							// Se consultan solicitudes de cambios Pendientes en DETALLE 
							var filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'PLANTILLALIMITEDETALLE' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: scope.idPlantilla},
										   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
							var SolicitudDeCambioDetalleDatasource = new kendo.data.DataSource({
								type: "odata",
								serverFiltering:true,
								transport: {
									read: {
										url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1538",
										dataType: "json"
								    }
								},
								filter: filterSol,
								schema: {
									model: {
										fields : {
											IdSolicitudCambio		: { type: "number"},
											IdSecuenciaCambio		: { type: "number"},
											IdTipoEntidad			: { type: "string"},
											IdEntidadOrigen			: { type: "string"},
											CampoModificado			: { type: "string"},
											TipoDato				: { type: "string"},
											ValorAntiguo			: { type: "string"},
											ValorNuevo				: { type: "string"},
											FechaSolicitud			: { type: "string"},
											FechaRevision			: { type: "string"},
											IdEstadoAutorizacion	: { type: "string"}
										}
									}
								}
							});
							SolicitudDeCambioDetalleDatasource.query().then(function(result){
								if(SolicitudDeCambioDetalleDatasource.data().length > 0){
									//scope.dataSolicitudCambio = $.extend({},SolicitudDeCambioDetalleDatasource.data());
									scope.dataSolicitudCambio.CambiosPendientesDetalle=true;
								}
								
							});
						});
						scope.win.editar();
					});
				}
				else {
					console.log("IdPlantillaLimite undefined");
				}
			};
			
			scope.fn.limpiarPantallaPlantillaLimites = function(){
				scope.datosCabeceraAntes 		= undefined;
				scope.datosCabeceraDespues 		= undefined;
				if(scope.grilla.grillaPlantillaLimites != undefined){
					scope.grilla.grillaPlantillaLimites.dataSource.removeAll();
				}
				scope.grilla.plantillaLimitesSel = undefined;
				scope.datosDetalleAntes 		= undefined;
				scope.datosDetalleDespues 		= undefined;
				scope.guardarOk = false;
				scope.cancelarOk = false;
				scope.plantillaCargado = false;
				
				scope.datosCabeceraDespues = {};
				
				scope.dataSolicitudCambio.CambiosPendientes		= false;
				scope.dataSolicitudCambio.CambioDetallePendiente= false;
			};
			
			
			//CABECERA
			scope.win.administrarCabecera = function(plantillaLimites) {
				var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaLimites",{plantilla:plantillaLimites},false,false,scope.idVentana);
				
	            var promesa=bcsComunes.ventanaPromesa(id1);
	            $.when(promesa).then(function(respuesta){
	            	console.log(respuesta);
	            	if(respuesta.IdPlantillaLimite){
	            		scope.win.cancelar();
	            		scope.fn.cargaDatosPantalla(respuesta.IdPlantillaLimite);
	            		scope.guardarOk = true;
					}
					else {console.log("usuario cancela en el PopUp!");}
	            });
			};
			
			//DETALLE
			scope.win.modificarDetalle = function(seleccionado){
				if(seleccionado && seleccionado.IdPlantillaLimite && seleccionado.IdLimite) scope.win.administrarDetalle(seleccionado);
			};
			
			scope.win.administrarDetalle = function(seleccionado) {
				bcsComunes.ventanaOverlay(scope.idVentana,true);
				var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaLimitesDetalle",{modo: scope.modo, lineaDetalle:seleccionado, IdPlantillaLimite:scope.idPlantilla, IdPortafolio:scope.idEntidad, FiltroPlantilla: scope.win.filtroPlantilla },false,false,scope.idVentana);
				
	            var promesa=bcsComunes.ventanaPromesa(id1);
	            $.when(promesa).then(function(respuesta){
	            	console.log(respuesta);
	            	bcsComunes.ventanaOverlay(scope.idVentana,false);
	            	if(respuesta.aceptar === true){
	            		scope.grilla.grillaPlantillaLimites.dataSource.read();
	            		//scope.fn.cargaDatosPantalla(scope.idPlantilla);
	            		scope.guardarOk = true;
	            		scope.grilla.plantillaLimitesSel = undefined;
					}
					else console.log("usuario cancela en el PopUp!");
	            });
			};
			
			
			scope.win.mostrarCambios = function() {
				var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : scope.dataSolicitudCambio},false,false,scope.idVentana);
				var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			};
					
			/*********************************************
			 * FUNCIONES WATCH
			 *********************************************/
			scope.$watch('idPlantilla',function(newval,oldval){
					if(newval !== undefined && newval != '0'){
						scope.fn.limpiarPantallaPlantillaLimites();
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
				    		//scope.grilla.grillaPlantillaLimites.refresh();
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