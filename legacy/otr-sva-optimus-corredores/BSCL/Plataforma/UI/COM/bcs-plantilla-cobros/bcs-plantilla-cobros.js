angular.module('ModuloPlataforma').directive('bcsPlantillaCobros',
function($q,$translate,bcsComunes,$timeout,$state,bcsConfiguracionMatrizPerfilComercial,servicioPerfilComercial,bcsAlerta,OPCIONES_BCSALERTA,bcsPerfilComercial,servicioPerfilCobros){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idPlantilla 			: '=?',
			idVentana				: '=?',
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
    	templateUrl: '../../Plataforma/UI/COM/bcs-plantilla-cobros/bcs-plantilla-cobros.html', 
    	link: function (scope, element, attrs, fn) {

    		console.log("directiva plantilla cobros");

			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var overlay 					= bcsComunes.overlay;
			scope.win 						= scope.win 	|| {};
			scope.win.modoEditar 			= false;
			scope.win.muestraEditar 		= false;
			
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
				scope.fn.limpiarPantallaPerfilCobros();
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
			scope.win.administrarCabecera = function(perfilCobros) {
				var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaCobros",{perfil:perfilCobros},false,false,scope.idVentana); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(respuesta){ 
	            	console.log(respuesta);
	            	if(respuesta.aceptar === true){
	            		scope.fn.cargaDatosPantalla(respuesta.IdPerfilCobros);
	            		//scope.fn.limpiarPantallaPerfilCobros();
	            		scope.guardarOk = true;
					}
					else {console.log("usuario cancela en el PopUp!");}
	            });
			};
			
			//DETALLE
			scope.win.modificarDetalle = function(seleccionado,esPersonalizado){
				if(seleccionado && seleccionado.IdValorCobro) scope.win.administrarDetalle(seleccionado,esPersonalizado);
			};
			
			//DETALLE
			scope.win.quitarDetalle = function(seleccionado){
				
				var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_QUITAR_COBRO'),scope.idVentana);
				t.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
						
						var detalles = [];
						
						detalles.push({
							IdDetallePerfilCobros : seleccionado.IdDetallePerfilCobros
						});

						var datos = {
								p_IdEmpresa 			: scope.$root.SesionIdEmpresa,
								p_IdUsuario				: scope.$root.SesionIdUsuario,
								p_IdModulo				: "PLA",
								p_DetallesPerfilCobro : detalles
						}
						
						bcsComunes.ventanaOverlay(scope.idVentana,true);
						servicioPerfilComercial.quitarDetallePerfilCobros(datos).$promise.then(function(result){
							scope.grilla.GrillaMatrizCobrosSeleccionado = undefined;
							bcsComunes.ventanaOverlay(scope.idVentana,false);
							var id = bcsComunes.ventanaMsg({
								conflictos : util.parseServerResponse(result)
							},scope.idVentana)
							scope.grilla.GrillaMatrizCobros.dataSource.read();	
						},function(result){
							bcsComunes.ventanaOverlay(scope.idVentana,false);
							var id = bcsComunes.ventanaMsg({
								conflictos : util.parseServerResponse(result)
							},scope.idVentana)
						})
						
					}
				});
				
			};
			
			scope.win.administrarDetalle = function(seleccionado,esPersonalizado) { 
				var parametrosModal = {
					esPersonalizado:esPersonalizado||false,
					modo:scope.modo,
					lineaDetalle:seleccionado,
					detallePerfil:_.clone(scope.grilla.GrillaMatrizCobros.dataSource.data()),
					IdPerfilCobros:scope.idPlantilla
				};
				var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaCobrosDetalle",parametrosModal,false,false,scope.idVentana); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(respuesta){ 
	            	scope.grilla.GrillaMatrizCobros.dataSource.read();
	            	if(respuesta.aceptar === true) scope.guardarOk = true;
	            });
			};
			
			scope.win.administrarValoresCobro = function(seleccionado){
				("abre ventana administrar valores cobro");
				var id1 = bcsComunes.ventanaAbrir("AdministrarValoresCobro",{},false,false,scope.idVentana); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(respuesta){ 
	            	console.log(respuesta);
	            	if(respuesta.aceptar === true) console.log("cierra ventana administrar valores cobro");
	            });
			};
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			scope.ds.PerfilCobros = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1332",
							dataType: "json"
						}
					},
					schema: {
						model: { fields: {		 	
							IdPerfilCobros 					: { type: "string"},
							DescripcionPerfilCobros 		: { type: "string"},
							IdEmpresa 						: { type: "number"},
							EstadoPerfilCobros 				: { type: "string"},
							NombreEstadoPerfilCobros 		: { type: "string"},
						} }
					}
				}
			});
			
			/*********************************************
			 * COMBOBOX 
			 *********************************************/
			
			
			/*********************************************
			 * GRILLAS
			 *********************************************/
			scope.grilla.GrillaMatrizCobros = {
				dataSource		: new bcs.DataSource({
					type 		: "odata", //Los tipos posibles son: odata, call, array y object
					definition 	: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 1000,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1327",
								dataType: "json"
							}
						},
						filter: [
							{field: 'IdPerfilCobros', operator: "eq", value: '-1'},
							{field: 'EsPersonalizado', operator: "eq", value: (scope.modo=='PE')?'S':'N'},
						],
						schema 			: {
							model	: { fields	: bcsConfiguracionMatrizPerfilComercial.getEsquemaGridMatrizCobros() }
						},
						group: [
					        {field: "IdBolsa"},
					        {field: "TipoProducto"},
					        {field: "TipoOperacion"},
					        {field: "CanalIngreso"}
					        ]
						}
				}),
				columns			: [
					//{field: "IdBolsa",width: "0px",title: $translate.instant("UI_BOLSA"),attributes : {style : "text-align:left;"}},		
					//{field: "TipoProducto",width: "0px",title: $translate.instant("UI_PRODUCTO"),attributes : {style : "text-align:left;"}},
					//{field: "TipoOperacion",width: "0px",title: $translate.instant("UI_TIPO_OPERACION"),attributes : {style : "text-align:left;"}},
					//{field: "CanalIngreso",width: "0px",title: $translate.instant("UI_CANAL"), attributes: { style : "text-align:left;" }},
					//{field: "Corresponsal",width: "5%",title: $translate.instant("UI_CORRESPONSAL"),attributes : {style : "text-align:left;"}},
					//{field: "DescripcionValorCobro",width: "100px",title: $translate.instant("UI_VALOR_COBRO_LISTA"),format:"{0:c}",attributes : {style : "text-align:left;"}},
					{field: "CodigoCobroClasificacion",width: "120px",title: $translate.instant("UI_CLASIFICACION"),attributes : {style : "text-align:left;"}},
					{field: "IdCobro",width: "100px",title: $translate.instant("UI_COBRO"),attributes : {style : "text-align:left;"}},
					{field: "ValorCobro",width: "120px",title: $translate.instant("UI_VALOR"),attributes : {style : "text-align:left;"},template: kendo.template("#if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}#")},
					{field: "ValorTopeMinimoOrden",width: "120px",title: $translate.instant("UI_MINIMO"),format:"{0:n2}",attributes : {style : "text-align:left;"},template: "#= (IdMoneda === 'UF' ? 'UF ' : '') + util.bcs_formato(ValorTopeMinimoOrden, 'GEN', 'Monto', IdMoneda, '', '').valor #"},
					{field: "IdMoneda",width: "80px",title: $translate.instant("UI_MONEDA"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
					{field: "DiasPlazo",width: "100px",title: $translate.instant("UI_PLAZO"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
					{field: "NombreEstadoDetalle",width: "100px",title: $translate.instant("UI_ESTADO"),attributes : {style : "text-align:left;"}},
					{field: "Prioridad",width: "100px",title: $translate.instant("UI_PRIORIDAD"),format:"{0:n0}",attributes : {style : "text-align:left;"}}
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
			scope.fn.cargaDatosPantalla = function(IdPerfilCobros){
				if (IdPerfilCobros !== undefined  && IdPerfilCobros !== ''){
					var filterPerfilCobros = [ { field: "IdPerfilCobros", operator: "eq", value: IdPerfilCobros } ];
					//CABECERA
					scope.idPlantilla = IdPerfilCobros;
					scope.ds.PerfilCobros.query({ filter: filterPerfilCobros }).then(function(){
						if(scope.ds.PerfilCobros.data().length > 0) {
							scope.grilla.GrillaMatrizCobros.dataSource.removeAll();
							scope.datosCabeceraAntes   				= _.clone(scope.ds.PerfilCobros.data()[0]);
							scope.datosCabeceraDespues 				= _.clone(scope.ds.PerfilCobros.data()[0]);
							scope.datosCabeceraAntes.Habilitado 	= (scope.ds.PerfilCobros.data()[0].EstadoPerfilCobros==='ACT')?true:false;
							scope.datosCabeceraAntes.PorDefecto 	= (scope.ds.PerfilCobros.data()[0].PorDefecto==='S')?true:false;
							scope.datosCabeceraDespues.Habilitado 	= (scope.ds.PerfilCobros.data()[0].EstadoPerfilCobros==='ACT')?true:false;
							scope.datosCabeceraDespues.PorDefecto 	= (scope.ds.PerfilCobros.data()[0].PorDefecto==='S')?true:false;
							
							scope.win.perfilCargado = true;
							scope.perfilCargado 	= true;
						}
						else { // esto no debiera ocurrir! Analizar insert de perfiles extras para clientes antiguos!.
							var perfilCobros = {};
							perfilCobros.p_IdEmpresa 				= scope.$root.SesionIdEmpresa; 
							perfilCobros.p_IdUsuario 				= scope.$root.SesionIdUsuario;
							perfilCobros.p_IdModulo 				= "PLA";
							perfilCobros.p_UsaReglasNegocio 		= "N";
							perfilCobros.p_IdPerfilCobros 			= IdPerfilCobros;
							perfilCobros.p_DescripcionPerfilCobros 	= IdPerfilCobros;
							perfilCobros.p_EsPerfilPersonalizado	= 'S';
							perfilCobros.p_IdEstado  				= 'ACT';
							perfilCobros.p_PorDefecto 				= 'N';
							servicioPerfilCobros.savePerfilCobros(perfilCobros,function (result){
								if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0){
									console.log("creadoPerfilExtra",result);
									scope.win.perfilCargado = true;
									scope.perfilCargado 	= true;
									scope.grilla.GrillaMatrizCobros.dataSource.filters(filterPerfilCobros);
								}
								else {
									console.log("error",result);
								}
							}, function(resError){
								console.log("error",resError);
							});
						}
						//DETALLE
						scope.grilla.GrillaMatrizCobros.dataSource.filter(filterPerfilCobros);
						//scope.grilla.GrillaMatrizCobros.read();
						
						scope.win.editar();
					});
				}
				else {
					console.log("IdPerfilCobros undefined");	
				}
			};

			scope.fn.preparaDatosCrear = function(){
				scope.datosDetalleDespues = _.clone(scope.grilla.GrillaMatrizCobros.dataSource.data());
				if (scope.datosDetalleDespues.length>0){
					var perfilCobros = {};
					perfilCobros.p_IdEmpresa 		= scope.$root.SesionIdEmpresa; 
					perfilCobros.p_IdUsuario 		= scope.$root.SesionIdUsuario;
					perfilCobros.p_IdModulo 		= "PLA";
					perfilCobros.p_simularOperacion = "N";
					perfilCobros.p_UsaReglasNegocio = "N";
					perfilCobros.p_IdPerfilCobros 			= scope.datosCabeceraDespues.IdPerfilCobros;
					perfilCobros.p_DescripcionPerfilCobros 	= scope.datosCabeceraDespues.DescripcionPerfilCobros;
					perfilCobros.p_EsPerfilPersonalizado	= 'N';
					perfilCobros.p_IdEstado = (scope.datosCabeceraDespues.Habilitado === true) ? 'ACT' : 'INA';
					perfilCobros.p_PorDefecto = (scope.datosCabeceraDespues.PorDefecto === true) ? 'S' : 'N';
					var p_DetallePerfil = [];
					$.each(scope.datosDetalleDespues,function(key,value){
						var lineaDetalle = {};
						lineaDetalle.p_IdBolsa 				= value.IdBolsa;
						lineaDetalle.p_IdTipoProducto  		= value.IdTipoProducto;
						lineaDetalle.p_IdTipoOperacion  	= value.IdTipoOperacion;
						lineaDetalle.p_IdCanalIngreso  		= value.IdCanalIngreso;
						lineaDetalle.p_IdEstado  			= (value.EstadoDetalle === 'ACT') ? value.EstadoDetalle : 'INA';
						lineaDetalle.p_DiasPlazo 			= value.DiasPlazo;
						lineaDetalle.p_Prioridad 			= value.Prioridad;
						lineaDetalle.p_IdValorCobro 		= value.IdValorCobro;
						p_DetallePerfil.push(lineaDetalle);
					});
					if(p_DetallePerfil.length>0) {
						perfilCobros.p_DetallePerfil=p_DetallePerfil;
						return perfilCobros;
					}
					else return {};
				}
				else {
					bcsComunes.msg("warning","GUARDAR PERFIL DE COBROS","No hay lineas de detalle asociadas al nuevo perfil de cobros");
				}
			};
			

			scope.fn.editarLineaSeleccionada = function() {
				var dataPopUp = _.clone(scope.grilla.GrillaMatrizCobrosSeleccionado);
				dataPopUp.esEdicionLinea = false;
				var id1 = bcsComunes.ventanaAbrir("modalAgregarValorCobroDetalle",{ dato1 : dataPopUp},false,false,scope.idVentana); 
		        var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(respuesta){ 
	            	console.log(respuesta);
	            	if(respuesta.aceptar === true){	
	            		scope.grilla.GrillaMatrizCobrosSeleccionado = angular.extend(scope.grilla.GrillaMatrizCobrosSeleccionado, respuesta);
						scope.grilla.GrillaMatrizCobros.read();
					}
					else {console.log("usuario cancela en el PopUp!");}
	            });
			};

			scope.fn.limpiarPantallaPerfilCobros = function(){
				scope.datosCabeceraAntes 		= undefined;
				scope.datosCabeceraDespues 		= undefined;
				scope.grilla.GrillaMatrizCobros.dataSource.removeAll();
				scope.datosDetalleAntes 		= undefined;
				scope.datosDetalleDespues 		= undefined;
				scope.guardarOk = false;
				scope.cancelarOk = false;
				scope.perfilCargado = false;
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
				    		scope.grilla.GrillaMatrizCobros.read();
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

		 
		
		
		
