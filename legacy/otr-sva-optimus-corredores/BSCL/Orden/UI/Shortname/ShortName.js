(function() {
	angular.module('ModuloOrdenes').controller('Shortname',
	function($scope,$translate,bcsComunes,bcsAlerta){
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var shortN = this;
		shortN.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		shortN.idVentana = bcsComunes.ventanaWinId($scope);
		shortN.win = {};
		shortN.win.idVentana = bcsComunes.ventanaWinId($scope);
		shortN.win.muestraEditar = true;
		shortN.win.muestraCrear = true;
		shortN.win.muestraCambiarEstado = true;

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
    shortN.win.crearShortName = function() {
			var winShortName = bcsComunes.ventanaAbrir("ModalShortName", {
				dato:undefined}, false,false,shortN.win.idVentana);
			var promesa = bcsComunes.ventanaPromesa(winShortName);                               
            promesa.then(function(res){
            	shortN.gridShortNames.dataSource.read();
            });
		};
		
		shortN.win.editarShortName = function(registro) {
			if(registro){
				var winShortName = bcsComunes.ventanaAbrir("ModalShortName", {
					dato:undefined, ShortName:registro}, false,false,shortN.win.idVentana);
				var promesa = bcsComunes.ventanaPromesa(winShortName);                               
	            promesa.then(function(res){
	            	shortN.gridShortNames.dataSource.read();
	            });	
			}
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
			
/*********************************************
 * COMBOBOX
 *********************************************/
		
/*********************************************
 * GRILLAS
 *********************************************/	
		$scope.initDetalle = function(e) {
			
			var filterShortName = [{ field: "ShortName", operator: "eq", value: e.data.ShortName }];
			shortN.dsDetalleConfiguracionShortName.query({filter:filterShortName}).then(function() {
				var datos = shortN.dsDetalleConfiguracionShortName.view();
				if (datos.length > 0){
					console.log(datos);
					$.each(datos,function(key,value){
						datos[key].ReservaTransaccion 		= (value.ReservaTransaccion==='S')?true:false;
						datos[key].AsignacionAuto 			= (value.AsignacionAuto==='S')?true:false;
						datos[key].MovimientoFacturarAuto	= (value.MovimientoFacturarAuto==='S')?true:false;
					});
					e.data.listaTipoInstrumentos = datos;
					
				}
			});
		}
		
		shortN.gridShortNames =  {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					schema 			: {
						model	: {
		                    fields	: {
		                    	ShortName		    	: { type: "string" },
		                    	Habilitado		    	: { type: "string" },
		                    	IdSocioDeNegocio		: { type: "string" },
		                    	DocumentoIdentidad		: { type: "string" },
		                    	CodigoPortafolio		: { type: "string" },
		                    	FechaHoraCreacion	    : { type: "date"   },
		                    	UsuarioCreador	    	: { type: "string" },
		                    	FechaHoraModificacion	: { type: "date"   },
		                    	UsuarioModificador		: { type: "string" },
		                    	IdSocioDeNegocio		: { type: "number" },
		                        IdPortafolio			: { type: "number" },
                                NombreCliente       	: { type: "string" },
	                        }
                   		}
					},
					transport : {
						read : {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1901",
							dataType : "json"
						}
					},
				}
			}),
			columns	: [
			 	{field: "ShortName",			width: "120px",	title: $translate.instant("UI_SHORTNAME")},
			 	{field: "Habilitado",		    width: "80px",	title: $translate.instant("UI_HABILITADO")},
			 	{field: "NombreCliente",		width: "100px",	title: $translate.instant("UI_CLIENTE")}, 
			 	{field: "DocumentoIdentidad",	width: "120px",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD")},
			 	{field: "CodigoPortafolio", 	width: "100px",	title: $translate.instant("UI_CODIGO_PORTAFOLIO")},
			 	{field: "FechaHoraCreacion",		width: "110px",	title: $translate.instant("UI_FECHA_CREACION")},
			 	{field: "UsuarioCreador",	    width: "110px",	title: $translate.instant("UI_USUARIO_CREADOR")},
			 	{field: "FechaHoraModificacion",	width: "110px",	title: $translate.instant("UI_FECHA_MODIFICACION")},
				{field: "UsuarioModificador",	width: "110px",	title: $translate.instant("UI_USUARIO_MODIFICADOR")},	
			],		
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			filterable			: true,
			height				: 450,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row",
	    	detailInit : function(e) {
		    	$scope.initDetalle(e);
			}
		};
	
/*********************************************
 * DATASOURCES
 *********************************************/	
    shortN.dsDetalleConfiguracionShortName = new bcs.DataSource({
			type : "odata",
			definition : {
				serverFiltering:true,
				transport : {
					read : {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1352",
						dataType: "json"
					}
				},
				schema : {
					model : {
						fields : {
							ShortName 			    : { type: "string"},
							CodigoCorredora			: { type: "string"},
							AsignacionAuto			: { type: "string"},	
							MovimientoFacturarAuto 	: { type: "string"},	
						}
					}
				}
			}
		});
		
/*********************************************
 * FUNCIONES
 *********************************************/
			
/*********************************************
 * WATCHERS
 ********************************************/
	
/*********************************************
 * GOOOOOOOOO!
 *********************************************/

	});
}());
//-------------------------- FIN CONTROLLER ----------------------
 