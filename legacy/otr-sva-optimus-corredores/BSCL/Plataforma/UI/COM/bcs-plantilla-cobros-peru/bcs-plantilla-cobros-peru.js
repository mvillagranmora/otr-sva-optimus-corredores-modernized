angular.module('ModuloPlataforma').directive('bcsPlantillaCobrosPeru',function($translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosPlataformaComercialPeru){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idPlantilla 			: '=?',
			idVentana				: '=?',
			modo 		 			: '=?',// MP: MantenedorPlantillas, PB: PortafolioBase, PE: PortafolioExtra
			callbackCrearPerfil		: '&?',
			modocopiar				: '=?'
		},
    	templateUrl: '../../Plataforma/UI/COM/bcs-plantilla-cobros-peru/bcs-plantilla-cobros-peru.html', 
    	link: function (scope, element, attrs, fn) {

			
			scope.datosCabeceraAntes 		= {};
			scope.datosCabeceraDespues 		= {};
			
			scope.cmbbx						= {};
			scope.ds						= {};
			scope.grilla					= {};
			scope.fn						= {};
			
			//CABECERA
			scope.fn.administrarCabecera = function(perfilCobros, modocopiar) {
				var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaCobrosPeru",{perfil:perfilCobros, copiar:modocopiar},false,false,scope.idVentana); 
	            bcsComunes.ventanaPromesa(id1).then(function(respuesta){ 
	            	if(respuesta.aceptar === true){
	            		scope.fn.cargaDatosPantalla(respuesta.IdPerfilCobros);
	            		if (scope.callbackCrearPerfil) scope.callbackCrearPerfil();
					}
					else {console.log("usuario cancela en el PopUp!");}
	            });
			};
			
			scope.fn.configuracionCobros = function() {
				bcsComunes.ventanaAbrir("ConfiguracionCobros",{},false,false,scope.idVentana);
			}
			
			//DETALLE
			scope.fn.administrarDetalle = function(seleccionado,accion){
				if (seleccionado && seleccionado.length > 0) {
					if (accion === 'U'){
						var detalles = [];
						
						$.each(seleccionado,function(key,value){
							value.CodigoDefinicionCobro = value.CodigoDefinicionCobroBase;
							value.CodigoDefinicionCobroBase = '';
							//value.ValorCobroCombinado = value.valor;
							detalles.push(value);
						})
						var p = bcsComunes.ventanaAbrir("modalEditarCobrosPeru",{IdPerfilCobros: seleccionado[0].IdPerfilCobros, DefinicionCobro : detalles},false,false,scope.idVentana);
						bcsComunes.ventanaPromesa(p).then(function(result){
							scope.grilla.GrillaMatrizCobros.dataSource.read();
							//bcsComunes.ventanaCerrar(scope.idVentana);
						});
						
					}else {
						var detalles = [];
						
						$.each(seleccionado,function(key,value){
							detalles.push({
								IdDetallePerfilCobros : value.IdDetallePerfilCobros,
								CodigoDefinicionCobro : value.CodigoDefinicionCobro
							})
						})
						
						var datos = {
								p_IdEmpresa 			: scope.$root.SesionIdEmpresa,
								p_IdUsuario				: scope.$root.SesionIdUsuario,
								p_IdModulo				: "PLA",
								p_DetallesPerfilCobro : detalles
						}
						
						bcsComunes.ventanaOverlay(scope.idVentana,true);
						bcsServiciosPlataformaComercialPeru.eliminarDetallePerfilCobros(datos).$promise.then(function(result){
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
				}
				else {
					var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaCobrosDetallePeru",{
						IdPerfilCobros:scope.idPlantilla, modo: scope.modo},false,false,scope.idVentana); 
		            bcsComunes.ventanaPromesa(id1).then(function(respuesta){ 
		            	scope.grilla.GrillaMatrizCobros.dataSource.read();
					});
				}
				
			}
						
			scope.ds.PerfilCobros = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering : true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3544",
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
			
			scope.grilla.GrillaMatrizCobros = {
				dataSource		: new bcs.DataSource({
					type 		: "odata", //Los tipos posibles son: odata, call, array y object
					definition 	: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3548",
								dataType: "json"
							}
						},
						
						schema	: {
							model	: { 
								fields	: {
									NombreBolsa 			: {type : "string"},
									NombreTipoProducto 		: {type : "string"},
									NombreTipoOperacion 	: {type : "string"},
									NombreTipoCanalIngreso 	: {type : "string"},
									CodigoDefinicionCobro 	: {type : "string"},
									NombreDefinicionCobro 	: {type : "string"},
									NombreClaseCobro 		: {type : "string"},
									NombreTipoCobro 		: {type : "string"},
									ValorCobroCombinado 	: {type : "number"},
									IdMonedaCobroUI		 	: {type : "string"},
									NombreEstado			: {type : "string"},
									ValorCobroMonedaPorcentaje 	: {type : "string"},
									UsoMonedaFormato 	: {type : "string"}
								} 
							}
						},
						group: [
					        {field: "NombreTipoProducto"},
					        {field: "NombreTipoOperacion"},
					        {field: "NombreBolsa"},
					        {field: "NombreTipoCanalIngreso"}
					        ]
						}
				}),
				columns			: [
					{field: "NombreBolsa",					width: "120px",title: $translate.instant("UI_BOLSA"), hidden : true},
					{field: "NombreTipoProducto",			width: "120px",title: $translate.instant("UI_TIPO_PRODUCTO"), hidden : true},
					{field: "NombreTipoOperacion",			width: "120px",title: $translate.instant("UI_TIPO_OPERACION"), hidden : true},
					{field: "NombreTipoCanalIngreso",		width: "120px",title: $translate.instant("UI_CANAL"), hidden : true},
					{field: "NombreDefinicionCobro",		width: "100px",title: $translate.instant("UI_COBRO")},
					{field: "NombreClaseCobro",				width: "100px",title: $translate.instant("UI_CLASE_COBRO")},
					{field: "NombreTipoCobro",				width: "100px",title: $translate.instant("UI_TIPO_COBRO")},
					{field: "ValorCobroCombinado",			width: "100px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>"},
					{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},
					{field: "NombreEstado",					width: "100px",title: $translate.instant("UI_ESTADO")}



				],		
				sortable			: false,
				groupable			: false,
				autoBind			: false, 
				height				: 280,
				userSettings		: true,
				navigatable			: true,
				gridCreated			: function(grid) {
					scope.apiGrid = grid;	
				},
				excel : {
					allPages: true,
				},
			    selectable 			: "multiple,row"
			 };
			
			scope.fn.cargaDatosPantalla = function(IdPerfilCobros){
				if (IdPerfilCobros !== undefined  && IdPerfilCobros !== ''){
					var filterPerfilCobros = [ { field: "IdPerfilCobros", operator: "eq", value: IdPerfilCobros } ];
					//CABECERA
					scope.idPlantilla = IdPerfilCobros;
					bcsComunes.ventanaOverlay(scope.idVentana,true);
					scope.ds.PerfilCobros.query({ filter: filterPerfilCobros }).then(function(){
						bcsComunes.ventanaOverlay(scope.idVentana,false);
						if(scope.ds.PerfilCobros.data().length > 0) {
							scope.datosCabeceraAntes   				= _.clone(scope.ds.PerfilCobros.data()[0]);
							scope.datosCabeceraDespues 				= _.clone(scope.ds.PerfilCobros.data()[0]);
							scope.datosCabeceraAntes.Habilitado 	= (scope.ds.PerfilCobros.data()[0].EstadoPerfilCobros==='ACT')?true:false;
							scope.datosCabeceraDespues.Habilitado 	= (scope.ds.PerfilCobros.data()[0].EstadoPerfilCobros==='ACT')?true:false;
							scope.apiGrid.dataSource.filter({ field: "IdPerfilCobros", operator: "eq", value: IdPerfilCobros });
						}
						else { // esto no debiera ocurrir! Analizar insert de perfiles extras para clientes antiguos!.
							bcsComunes.msg("error","error","UI_NO_SE_ENCONTRO_PLANTILLA_DE_COBROS");
						}
					});
				}
				else {
					console.log("IdPerfilCobros undefined");	
				}
			};
					
			scope.$watch('idPlantilla',function(newval,oldval){
					if(newval !== undefined){
						scope.fn.cargaDatosPantalla(newval);
					}
			});
			

			/*********************************************
			 * GO!!!
			 *********************************************/
			if(scope.idPlantilla) scope.fn.cargaDatosPantalla(scope.idPlantilla);
    	}
	}
});

		 
		
		
		
