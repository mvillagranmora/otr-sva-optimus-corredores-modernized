/*************************************************************
 * Purpose	: Controlador para administración del servicio
 * 			  de transacciones (operadores-homologaciones)
 * Author	: JGarces - BCS
 * Date		: 05/02/2016
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarServTransaccionesCtrl',
	function($scope,$translate,bcsComunes,bcsAlerta){
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var admServTran = this;
		admServTran.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		admServTran.idVentana = bcsComunes.ventanaWinId($scope);
		admServTran.win = {};
		admServTran.win.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		admServTran.win.muestraEditar = true;
		admServTran.win.muestraCrear = true;
		admServTran.win.muestraCambiarEstado = true;
		
		admServTran.defCols = [ { titulo:'UI_MERCADO_RV', asigNom:'AsignMRV', factNom: 'MvtoFactMRV'},
		                    { titulo:'UI_MERCADO_MILA', asigNom:'AsignMMILA', factNom: 'MvtoFactMMILA'},
		                    { titulo:'UI_MERCADO_DEUDA', asigNom:'AsignMDEUDA', factNom: 'MvtoFactMDEUDA'},
		                    { titulo:'UI_MERCADO_DERIVADOS', asigNom:'AsignMDERIV', factNom: 'MvtoFactMDERIV'},
		                    { titulo:'UI_MERCADO_OTC', asigNom:'AsignMOTC', factNom: 'MvtoFactMOTC'} ];

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		admServTran.win.crearOperador = function() {
			var winOperador = bcsComunes.ventanaAbrir("ModalOperador", {
				dato:undefined}, false,false,admServTran.win.idVentana);
			var promesa = bcsComunes.ventanaPromesa(winOperador);                               
            promesa.then(function(res){
            	admServTran.gridOperadores.dataSource.read();
            });
		};
		
		admServTran.win.editarOperador = function(registro) {
			if(registro){
				var winOperador = bcsComunes.ventanaAbrir("ModalOperador", {
					dato:undefined, operador:registro}, false,false,admServTran.win.idVentana);
				var promesa = bcsComunes.ventanaPromesa(winOperador);                               
	            promesa.then(function(res){
	            	admServTran.gridOperadores.dataSource.read();
	            });	
			}
		};
		
		admServTran.win.crearHmlg = function(datosOperador) {
			if(datosOperador) {
				var winHmlg = bcsComunes.ventanaAbrir("ModalHomologOperador", {
					operador:datosOperador}, false,false,admServTran.win.idVentana);
				var promesa = bcsComunes.ventanaPromesa(winHmlg);                               
	            promesa.then(function(res){
	            	admServTran.gridOperadores.dataSource.read();
	            });
			}
		};
		
		admServTran.win.editarHmlg = function(datosOperador,registroHomologacion) {
			if(datosOperador && registroHomologacion){
				var winHmlg = bcsComunes.ventanaAbrir("ModalHomologOperador", {
					operador:datosOperador , homologacion : registroHomologacion}, false,false,admServTran.win.idVentana);
				var promesa = bcsComunes.ventanaPromesa(winHmlg);                               
	            promesa.then(function(res){
	            	admServTran.gridOperadores.dataSource.read();
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
			
			var filterOperador = [{ field: "CodigoOperador", operator: "eq", value: e.data.CodigoOperador }];
			admServTran.dsDetalleConfiguracionOperador.query({filter:filterOperador}).then(function() {
				var datos = admServTran.dsDetalleConfiguracionOperador.view();
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
		
		admServTran.gridHomologacion = function(dataItem){
			return {
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
			                    	IdBolsa				: { type: "string",editable: false 	},
			                    	Bolsa				: { type: "string",editable: false 	},
			                    	CodigoHomologacion	: { type: "string",editable: false	},
			                    	IdEstado			: { type: "string",editable: false 	},
			                    	Estado				: { type: "string",editable: false 	},
		                        }
	                   		}
						},
						transport : {
							read : {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1353",
								dataType : "json"
							}
						},
						filter : { field: "CodigoOperador", operator: "eq", value: dataItem.CodigoOperador },
					}
				}),
				columns	: [
				 	{field: "Bolsa",width: "10%",title: $translate.instant("UI_BOLSA"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
				 	{field: "CodigoHomologacion",width: "10%",title: $translate.instant("UI_CODIGO_HOMOLOGACION"),format:"{0:c}",attributes : {style : "text-align:left;"}},	
				 	{field: "Estado",width: "10%",title: $translate.instant("UI_ESTADO"),format:"{0:c}",attributes : {style : "text-align:left;"}},
				],		
				sortable			: false,
				pageable			: false,
				autoBind			: true, 
				height				: 100,
			    selectable 			: "row"
			};
		};
		
		admServTran.gridOperadores =  {
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
		                    	CodigoOperador		: { type: "string" },
		                    	NombreCorto		: { type: "string" },
		                    	NombreCorredora	: { type: "string" },
		                    	IdCorredora			: { type: "number" },
		                    	LibroOrdenes		: { type: "string" }, //[CELCONDOR-688] - Campo libro de ordenes en "operadores en rueda"
		                    	ReservaTransaccion	: { type: "string" },
		                    	UtilizaPortafolio	: { type: "string" },
		                    	NombreCliente		: { type: "string" },
		                    	DocumentoIdentidad	: { type: "string" },
		                    	IdSocioDeNegocio	: { type: "number" },
		                        IdPortafolio		: { type: "number" },
		                        CodigoPortafolio	: { type: "string" },
								RazonSocial			: { type: "string" },
			                    IdEstado			: { type: "string" },
			                    Estado				: { type: "string" },
	                        }
                   		}
					},
					transport : {
						read : {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1351",
							dataType : "json"
						}
					},
				}
			}),
			columns	: [
			 	{field: "NombreCorto",			width: "150px",	title: $translate.instant("UI_CORREDORA")},
			 	{field: "CodigoOperador",		width: "100px",	title: $translate.instant("UI_CODIGO_OPERADOR")},
			 	{field: "LibroOrdenes",		    width: "100px",	title: $translate.instant("UI_LIBRO_ORDENES")},  //[CELCONDOR-688] - Campo libro de ordenes en "operadores en rueda"
			 	{field: "ReservaTransaccion",	width: "110px",	title: $translate.instant("UI_RESERVAR_TRANSACCION")},
			 	{field: "UtilizaPortafolio",	width: "110px",	title: $translate.instant("UI_UTILIZA_PORTAFOLIO")},
			 	{field: "NombreCliente",		width: "250px",	title: $translate.instant("UI_CLIENTE")},
			 	{field: "DocumentoIdentidad",	width: "120px",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD")},
			 	{field: "CodigoPortafolio",		width: "90px",	title: $translate.instant("UI_CODIGO_PORTAFOLIO")},
				{field: "RazonSocial",		    width: "250px",	title: $translate.instant("UI_RAZON_SOCIAL")},
			 	{field: "Estado",				width: "80px",	title: $translate.instant("UI_ESTADO")},	
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
		admServTran.dsDetalleConfiguracionOperador = new bcs.DataSource({
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
							CodigoOperador 			: { type: "string"},
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

(function() {
	angular.module('ModuloPlataforma').factory('servicioTransacciones',['$resource',function($resource) {
		var urlSaveHomologacionOperador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=167";
		var urlSaveConfiguracionOperador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=168&c=1";
		var urlUpdateHomologacionOperador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=171";
		var urlUpdateConfiguracionOperador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=172&c=1";
		return $resource("", {},
		{
			'saveHomologacionOperador' : {method: "POST",url:urlSaveHomologacionOperador},
			'saveConfiguracionOperador' : {method: "POST",url:urlSaveConfiguracionOperador},
			'updateHomologacionOperador' : {method: "POST",url:urlUpdateHomologacionOperador},
			'updateConfiguracionOperador' : {method: "POST",url:urlUpdateConfiguracionOperador},
		});
	}]);
}()); 