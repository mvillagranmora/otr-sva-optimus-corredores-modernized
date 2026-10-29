(function() {
	angular.module('ModuloMesa').controller('MantenedorItemsManualesPosicionMRFCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var manIMPM		= this;
		manIMPM.idVentana	= bcsComunes.ventanaWinId($scope);
		manIMPM.win		= {};
		manIMPM.grilla	= {};
		manIMPM.cmbbx	= {};
		manIMPM.filtro	= {};
		manIMPM.fn		= {};
		
		manIMPM.filtro.Fecha		= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		manIMPM.filtro.IdMetodoDef	= 1;
		
		var filtroItemsManuales	= {};
		var itemsManualesArray	= [];
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		manIMPM.fn.agregar = function() {
			manIMPM.fn.abrirModal(undefined, 'Agregar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		manIMPM.grilla.ItemsManualesPosicionMRF = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : false, // Filtrado de datos en el servidor.
					serverPaging 	: false, // Paginado de los datos en el servidor.
					serverSorting 	: false, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=466",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdPosicion	: { type: "number"},
								IdEvento	: { type: "number"},
								IdMetodo	: { type: "number"},
								Fecha		: { type: "date"},
								IdMatriz	: { type: "number"},
								MontoEvento	: { type: "number"},
								Item		: { type: "string"},
								CodigoUsuario	: { type: "string"},
								FechaUltimaModificacion	: { type: "datetime"},
								NombreEvento: { type: "string"}
							}
						}
					},
					sort: { field: "IdPosicion", dir: "asc" },
				}
			}),
			columns: [
			    {field: "Item",			title: $translate.instant("UI_ITEM"),			width: "140px"	},
			    {field: "NombreEvento",	title: $translate.instant("UI_NOMBRE_EVENTO"),	width: "140px"	},
				{field: "MontoEvento",	title: $translate.instant("UI_MONTO_EVENTO"),	width: "100px",	editable: true,	template: "<label> #: (util.bcs_formato(MontoEvento,'MES','MONTO','USD','','')).valor #</label>"},
				{field: "CodigoUsuario",	title: $translate.instant("UI_USUARIO"),	width: "100px"	},
				{field: "FechaUltimaModificacion",	title: $translate.instant("UI_FECHA_ULTIMA_MODIFICACION"),	width: "100px"	}
			],
			sortable	: true,
			pageable	: true,
			autoBind	: false,
			height		: 400,
		    selectable 	: "row",
		    excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		manIMPM.cmbbx.PosicionMRF={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		manIMPM.fn.buscar = function(){
			itemsManualesArray = [];
			
			filtroItemsManuales = {};
			filtroItemsManuales.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
			filtroItemsManuales.p_IdUsuario	= $scope.$root.SesionIdUsuario;
			filtroItemsManuales.p_IdModulo	= 'MRF';
			filtroItemsManuales.p_Fecha		= manIMPM.filtro.Fecha;
			filtroItemsManuales.p_IdMetodo	= manIMPM.filtro.IdMetodo;
			
			bcsComunes.ventanaOverlay(manIMPM.idVentana,true);
						
			bcsServiciosMesa.ConsultarItemsManualesPosicionMRF(filtroItemsManuales,function (result){
				if(result && result.P_OUT_ITEMSMANUALES){
	    			$.each(result.P_OUT_ITEMSMANUALES,function(key,value){
	    				var aux = {};
						aux.IdPosicion	= value.IdPosicion;
						aux.IdEvento	= value.IdEvento;
						aux.IdMetodo	= value.IdMetodo;
						aux.Fecha		= value.Fecha;
						aux.IdMatriz	= value.IdMatriz;
						aux.Item		= value.Item;
						aux.CodigoUsuario		= value.CodigoUsuario;
						aux.FechaUltimaModificacion		= value.FechaUltimaModificacion;
						aux.NombreEvento= value.NombreEvento;
						aux.MontoEvento	= value.MontoEvento;
						itemsManualesArray.push(aux);
	    			});
				}
				manIMPM.grilla.ItemsManualesPosicionMRF.dataSource.data(itemsManualesArray);
				bcsComunes.ventanaOverlay(manIMPM.idVentana,false);
				
			}, function(resError){
				bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(manIMPM.idVentana,false);
			});
		};
		
		
		manIMPM.fn.limpiar = function(){
			itemsManualesArray = [];
			manIMPM.filtro.Fecha	= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			manIMPM.filtro.IdMetodo	= 1;
			manIMPM.grilla.ItemsManualesPosicionMRF.dataSource.removeAll();
		}
		
		
		manIMPM.fn.grabar = function(){
			var datosGrabar = {};
			datosGrabar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
			datosGrabar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGrabar.p_IdModulo			= "MRF";
			datosGrabar.p_UsaReglasNegocio	= "N";
			datosGrabar.p_Fecha				= filtroItemsManuales.p_Fecha;
			datosGrabar.p_ItemsManuales		= manIMPM.grilla.ItemsManualesPosicionMRF.dataSource.data();
			
			var tmpDatosGrabar = []; //Se almacenan solo los registros que tuvieron modificacion con respecto a la busqueda inicial
			for(var i=0; i<datosGrabar.p_ItemsManuales.length && i<itemsManualesArray.length; i++){
				if(itemsManualesArray[i].IdPosicion === datosGrabar.p_ItemsManuales[i].IdPosicion){
					if(itemsManualesArray[i].MontoEvento !== datosGrabar.p_ItemsManuales[i].MontoEvento)
						tmpDatosGrabar.push(datosGrabar.p_ItemsManuales[i]);
				}
			};
			datosGrabar.p_ItemsManuales		= tmpDatosGrabar;
			
			if(datosGrabar.p_ItemsManuales.length){
				bcsComunes.ventanaOverlay(manIMPM.idVentana,true);
				
				bcsServiciosMesa.GrabarItemsManualesPosicionMRF(datosGrabar,function (result){
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
						bcsComunes.msg("info",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_GRABADO"));
					}
					else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
						bcsComunes.msg("warning",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
					}
					else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),result.P_OUT_ERRORESDETALLE[0].Codigo);
					}
					else bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_REVISE_LOG"));
					
					manIMPM.fn.buscar();
					bcsComunes.ventanaOverlay(manIMPM.idVentana,false);
					
				}, function(resError){
					bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(manIMPM.idVentana,false);
				});
	        }
			else{
				bcsComunes.msg("warning","UI_ITEMS_MANUALES_POSICION_MRF",$translate.instant("UI_SIN_MODIFICACIONES"));
			}
		};
		
		
		manIMPM.fn.abrirModal = function(itemManualSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalItemsManualesPosicionMRF",{ 
											   ItemManualSel: itemManualSel, 
											   IdMetodo : manIMPM.dataFiltros.PosicionMRFObj.IdMetodo,
											   IdNombreMetodo : manIMPM.dataFiltros.PosicionMRFObj.NombreMetodo,
											   IdMoneda : manIMPM.dataFiltros.PosicionMRFObj.IdTipoReajuste,
											   NroDecimales : manIMPM.dataFiltros.PosicionMRFObj.CantidadDecimales,
											   AccionBoton : accion },
											   false,false,manIMPM.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		manIMPM.grilla.itemManualSel = undefined;
	        		manIMPM.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());