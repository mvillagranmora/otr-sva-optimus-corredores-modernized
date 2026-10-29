/*************************************************************
 * Purpose	: Grupo de Activos. Se debe agregar emisores y
 * 			  habilitar la función de editar.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('GrupoActivosCtrl',
		function($window,$scope,$q,$translate,bcsComunes,$timeout,$state,
				bcsConfiguracionMatrizPerfilComercial,$filter,$resource,bcsAlerta,OPCIONES_BCSALERTA,
				$uibModal,bcsPerfilComercial,servicioGrupoActivos){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var me = this; // controller as object!
		me.data										= {};
		me.formData 								= {};	//GUARDA DATOS DEL FORMULARIO
		me.formData.grillaGrupoActivosSeleccionado = {};
		me.dataGrupoActivos 						= {};
		me.dataGrupoActivos.muestraEditar 		= false;
		me.dataGrupoActivos.muestraCrear 		= true;
		me.dataGrupoActivos.muestraGuardar 		= false;
		me.dataGrupoActivos.muestraCancelar 	= false;
		me.dataGrupoActivos.muestraCargarDatos 	= true;
		me.idVentana 	= bcsComunes.ventanaWinId($scope);
		//
		me.pantalla 								= {}; // MODALIDADES DE PANTALLA
		me.pantalla.grupoActivos 				= {};
		me.pantalla.grupoActivos.modoLectura		= true;
		
		me.pantalla.modoComboInstr = true;  // para probar modalidades de carga de isnstrumento!.
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		
		me.dataGrupoActivos.cargarDatos = function() {
			cargaDatosPantalla(me.dataGrupoActivos.GrupoActivosObj.IdGrupoActivos);
		}
		
		me.dataGrupoActivos.editar = function() {
			bcsComunes.ventanaOverlay(me.idVentana,true);
			var idModal = bcsComunes.ventanaAbrir("ModalAdministrarGrupoActivo",{mod:"editar", data: me.dataGrupoActivos},false,false);
			bcsComunes.ventanaPromesa(idModal).then(function(data){
				me.dataGrupoActivos.cargarDatos();
				bcsComunes.ventanaOverlay(me.idVentana,false);
			});
		};

		me.dataGrupoActivos.crear = function() {
			bcsComunes.ventanaOverlay(me.idVentana,true);
			var idModal = bcsComunes.ventanaAbrir("ModalAdministrarGrupoActivo",{},false,false);
			bcsComunes.ventanaPromesa(idModal).then(function(data){
				bcsComunes.ventanaOverlay(me.idVentana,false);
				me.conf.grupoActivos.oConfiguracionGrupoActivos.filtros.read = true;
			});
		};

		me.dataGrupoActivos.crearDetalle = function() {
			bcsComunes.ventanaOverlay(me.idVentana,true);
			var grupoActivos = {};
			grupoActivos.idGrupoActivos = me.dataGrupoActivos.IdGrupoActivos;
			var idModal = bcsComunes.ventanaAbrir("ModalAdministrarGrupoActivoDetalle",{data:grupoActivos},false,false);
			bcsComunes.ventanaPromesa(idModal).then(function(data){
				me.dataGrupoActivos.cargarDatos();
				bcsComunes.ventanaOverlay(me.idVentana,false);
			});
		};
		me.dataGrupoActivos.editarDetalle = function() {
			var grupoActivos = {};
			grupoActivos.instrumento = me.formData.grillaGrupoActivosSeleccionado.kNgModel;
			grupoActivos.idGrupoActivos = me.dataGrupoActivos.IdGrupoActivos;
			grupoActivos.IdDetalleGrupoActivos = me.formData.grillaGrupoActivosSeleccionado.kNgModel.IdDetalleGrupoActivos;

			bcsComunes.ventanaOverlay(me.idVentana,true);
			var idModal = bcsComunes.ventanaAbrir("ModalAdministrarGrupoActivoDetalle",{mod:"editar", data: grupoActivos},false,false);
			bcsComunes.ventanaPromesa(idModal).then(function(data){
				me.dataGrupoActivos.cargarDatos();
				bcsComunes.ventanaOverlay(me.idVentana,false);
			});
		};

		me.dataGrupoActivos.cancelar = function() {
			me.pantalla.modalidad = '';
			me.pantalla.grupoActivos.modoLectura = true;
			me.dataGrupoActivos.muestraEditar 	= false;
			me.dataGrupoActivos.muestraCrear 	= true;
			me.dataGrupoActivos.muestraGuardar 	= false;
			me.dataGrupoActivos.muestraCancelar 	= false;
			me.dataGrupoActivos.instrumento = "-1";
			limpiarPantallaGrupoActivos();
		};
	
	
/*********************************************
 * DATASOURCES
 *********************************************/
		
		me.formData.dsGrillaGrupoActivos =  new bcs.DataSource({
			type 		: "odata", 	// Los tipos posibles son: odata, call,
									// array y object
			definition 	: {
				serverFiltering : true, // Filtrado de datos en el servidor.
				sort			: { field: "Nemotecnico", dir: "asc" },
				transport		: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1539",
						dataType: "json"
					}
				},
				serverPaging 	: true, // Paginado de los datos en el
							// servidor.
				serverSorting 	: true, // Ordenamiento de los datos en el
							// servidor.
				sortable 		: true,
				pageSize 		: 50,
				schema 			: {
					model	: {
						fields	: {
							IdDetalleGrupoActivos	: { type: "int"		},
							IdGrupoActivos 			: { type: "string"	},
							IdInstrumento 			: { type: "string"	},
							Nemotecnico				: { type: "string"	},
							IdTipoInstrumento		: { type: "string"	},
							CodigoTipoInstrumento	: { type: "string"	},
							NombreTipoInstrumento	: { type: "string"	},
							IdEstado	 			: { type: "string"	},
							IdEstadoDetalle 		: { type: "string"	},
							EstadoDetalle 			: { type: "string"	},
							// NombreEstado
							// IdEmpresa : { type: "string" }
						}
					}
				}
			}
		}),		
		
/*********************************************
 * COMBOBOX
 *********************************************/
		
		me.conf = {};
		me.conf.grupoActivos = {};

		me.conf.grupoActivos.oConfiguracionGrupoActivos={
			tipo : "combobox-grupo-activos",
			configuracion : {},
			filtros : {
				IdEstado : {field: "IdEstado", operator: "eq", value: "ACT"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		me.conf.grupoActivos.oConfiguracionTipoGrupoActivos={
				tipo : "combobox-tipo-grupo-activos",
				configuracion : {},
				filtros : {},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
			};
		

/*********************************************
 * GRILLAS
 *********************************************/
		
		me.formData.grillaGrupoActivos = {
				dataSource		: me.formData.dsGrillaGrupoActivos,
				columns			: [
					{field: "Nemotecnico",width: "200px",title: $translate.instant("UI_NEMOTECNICO"),attributes : {style : "text-align:left;"}},
					{field: "NombreTipoInstrumento",width: "350px",title: $translate.instant("UI_TIPO_INSTRUMENTO"),attributes : {style : "text-align:left;"}},
					{field: "IdEstadoDetalle",width: "30%",title: $translate.instant("UI_HABILITADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }, hidden:true},	
					{field: "EstadoDetalle",width: "200px",title: $translate.instant("UI_ESTADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }}	
				],		
				sortable			: false,
				pageable			: true,
				autoBind			: false,  // comentar
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

		var cargaDatosPantalla = function(IdGrupoActivos) {
			
			if (IdGrupoActivos != undefined)
			{
				bcsComunes.ventanaOverlay(me.idVentana,true);
				var filterGrupoActivos = [ { field: "IdGrupoActivos", operator: "eq", value: String(IdGrupoActivos) }, { field: "IdEstado", operator: "eq", value: "ACT" } ];
				me.formData.dsGrillaGrupoActivos.query({ filter: filterGrupoActivos }).then(function(){
					if(me.formData.dsGrillaGrupoActivos.ds._data.length > 0)
					{			
						me.dataGrupoActivos.IdGrupoActivos 								= me.formData.dsGrillaGrupoActivos.view()[0].IdGrupoActivos;
						me.dataGrupoActivos.DescripcionGrupoActivos 					= me.formData.dsGrillaGrupoActivos.view()[0].DescripcionGrupoActivos;
						me.dataGrupoActivos.Habilitado 									= (me.formData.dsGrillaGrupoActivos.view()[0].IdEstado === 'ACT') ? true : false;
						me.dataGrupoActivos.PorDefecto 									= (me.formData.dsGrillaGrupoActivos.view()[0].PorDefecto === 'S') ? true : false;
						me.conf.grupoActivos.oConfiguracionTipoGrupoActivos.idActual  	= me.formData.dsGrillaGrupoActivos.view()[0].IdTipoGrupoActivos;
						me.conf.grupoActivos.oConfiguracionTipoGrupoActivos.idDefecto 	= me.formData.dsGrillaGrupoActivos.view()[0].IdTipoGrupoActivos;
						
						var data = [];

						$.each(me.formData.dsGrillaGrupoActivos.data(),function(key,value){
							var aux = {}; //0: todos.
							if(value.IdDetalleGrupoActivos){
								aux.IdDetalleGrupoActivos 	= value.IdDetalleGrupoActivos;
								aux.IdGrupoActivos 			= value.IdGrupoActivos;
								aux.IdInstrumento 			= value.IdInstrumento;
								aux.Nemotecnico 			= value.Nemotecnico;
								aux.IdTipoInstrumento 		= value.IdTipoInstrumento;
								aux.CodigoTipoInstrumento 	= value.CodigoTipoInstrumento;
								aux.NombreTipoInstrumento	= value.NombreTipoInstrumento
								aux.IdEstado				= value.IdEstadoDetalle;
								aux.EstadoDetalle			= value.EstadoDetalle;
								data.push(aux);
							}
						});
						
						if (data.length>0) {
							data = _.sortBy(data, function(item) {
							   return [item.Nemotecnico];
							});
							me.formData.grillaGrupoActivos.dataSource.data(data);
						}
					
						me.dataGrupoActivos.muestraCrear = false;
						me.dataGrupoActivos.muestraCancelar 	= true;
					}
					else {
						console.log("No hay datos");
					}
					bcsComunes.ventanaOverlay(me.idVentana,false);
				});
			}
			else {
				bcsComunes.msg("warning","CARGAR DATOS","No ha seleccionado un dato.");
				bcsComunes.ventanaOverlay(me.idVentana,false);
			}
		};
			
		var limpiarPantallaGrupoActivos = function(){
			me.dataGrupoActivos.IdGrupoActivos 								= undefined;
			me.dataGrupoActivos.DescripcionGrupoActivos 					= undefined;
			me.dataGrupoActivos.PorDefecto 									= undefined;
			me.dataGrupoActivos.Habilitado 									= undefined;
			me.conf.grupoActivos.oConfiguracionTipoGrupoActivos.idActual	= undefined;
			me.conf.grupoActivos.oConfiguracionTipoGrupoActivos.idDefecto	= undefined;
			me.conf.grupoActivos.oConfiguracionGrupoActivos.idActual		= undefined;
			me.conf.grupoActivos.oConfiguracionGrupoActivos.idDefecto		= undefined;
			me.detalleProductoSeleccionado 									= '';
			me.formData.dsGrillaGrupoActivos.removeAll();
			me.formData.grillaGrupoActivosSeleccionado = {};
		};
		
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------


(function() {
	angular.module('ModuloPlataforma').factory('servicioGrupoActivos',['$resource',function($resource) {
		var urlGetGrupoActivos 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1539";
		var urlSaveGrupoActivos 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=97";
		var urlEditarGrupoActivos 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=13047";
		var urlSaveGrupoActivosDetalle 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=13048";
		var urlEditarGrupoActivosDetalle 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=13049";
		return $resource("", {},
		{
			'getGrupoActivos'	: {method: "GET"	, url:urlGetGrupoActivos },
			'saveGrupoActivos'	: {method: "POST"	, url:urlSaveGrupoActivos},
			'editarGrupoActivos': {method: "POST"	, url:urlEditarGrupoActivos},
			'saveGrupoActivosDetalle': {method: "POST"	, url:urlSaveGrupoActivosDetalle},
			'editarGrupoActivosDetalle': {method: "POST"	, url:urlEditarGrupoActivosDetalle},
		});
	}]);
}()); 

