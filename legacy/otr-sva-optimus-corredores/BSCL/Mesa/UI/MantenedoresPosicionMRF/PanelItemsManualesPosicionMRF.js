(function() {
	angular.module('ModuloMesa').controller('PanelItemsManualesPosicionMRFCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pIMPM		= this;
		pIMPM.idVentana	= bcsComunes.ventanaWinId($scope);
		pIMPM.win		= {};
		pIMPM.grilla	= {};
		pIMPM.cmbbx		= {};
		pIMPM.filtro	= {};
		pIMPM.fn		= {};
		
		pIMPM.win.MontosModificados = false;
		
		pIMPM.filtro.Fecha		= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		pIMPM.filtro.IdMetodoDef= 1;
		
		var filtroItemsManuales	= {};
		var itemsManualesArray	= [];
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		pIMPM.fn.agregar = function() {
			pIMPM.fn.abrirModal(undefined, 'Agregar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pIMPM.grilla.ItemsManualesPosicionMRF = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
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
								Fecha		: { type: "date"},
								IdMatriz	: { type: "number"},
								MontoEvento	: { type: "number"},
								Item		: { type: "string"},
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
				{field: "MontoEvento",	title: $translate.instant("UI_MONTO_EVENTO"),	width: "100px",	editable: true	}
			],
			sortable	: true,
			pageable	: true,
			autoBind	: false,
			height		: 480,
		    selectable 	: "row",
		    excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pIMPM.cmbbx.PosicionMRF={
			filtros: {
				IdEstado : {field: "IdEstado", operator: "eq", value: 'VIG'}
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pIMPM.fn.buscar = function(){
			itemsManualesArray = [];
			
			filtroItemsManuales = {};
			filtroItemsManuales.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
			filtroItemsManuales.p_IdUsuario	= $scope.$root.SesionIdUsuario;
			filtroItemsManuales.p_IdModulo	= 'MRF';
			filtroItemsManuales.p_Fecha		= pIMPM.filtro.Fecha;
			filtroItemsManuales.p_IdMetodo	= pIMPM.filtro.IdMetodo;
			
			bcsComunes.ventanaOverlay(pIMPM.idVentana,true);
						
			bcsServiciosMesa.ConsultarItemsManualesPosicionMRF(filtroItemsManuales,function (result){
				if(result && result.P_OUT_ITEMSMANUALES){
	    			$.each(result.P_OUT_ITEMSMANUALES,function(key,value){
	    				var aux = {};
						aux.IdPosicion	= value.IdPosicion;
						aux.Fecha		= value.Fecha;
						aux.IdMatriz	= value.IdMatriz;
						aux.Item		= value.Item;
						aux.NombreEvento= value.NombreEvento;
						aux.MontoEvento	= value.MontoEvento;
						itemsManualesArray.push(aux);
	    			});
				}
				pIMPM.grilla.ItemsManualesPosicionMRF.dataSource.data(itemsManualesArray);
				bcsComunes.ventanaOverlay(pIMPM.idVentana,false);
				
			}, function(resError){
				bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(pIMPM.idVentana,false);
			});
			pIMPM.win.MontosModificados = false;
		};
		
		
		pIMPM.fn.limpiar = function(){
			itemsManualesArray = [];
			pIMPM.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			pIMPM.filtro.IdMetodo = 1;
			pIMPM.grilla.ItemsManualesPosicionMRF.dataSource.removeAll();
			pIMPM.win.MontosModificados = false;
		}
		
		
		pIMPM.fn.grabar = function(){
			var datosGrabar = {};
			datosGrabar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
			datosGrabar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGrabar.p_IdModulo			= "MRF";
			datosGrabar.p_UsaReglasNegocio	= "N";
			datosGrabar.p_Fecha				= filtroItemsManuales.p_Fecha;
			datosGrabar.p_ItemsManuales		= pIMPM.grilla.ItemsManualesPosicionMRF.dataSource.data();
			
			for(var i=0; i<datosGrabar.p_ItemsManuales.length && i<itemsManualesArray.length; i++){
				if(itemsManualesArray[i].IdPosicion === datosGrabar.p_ItemsManuales[i].IdPosicion){
					if(itemsManualesArray[i].MontoEvento !== datosGrabar.p_ItemsManuales[i].MontoEvento)
						pIMPM.win.MontosModificados = true;
				}
			}
			
			if(datosGrabar.p_ItemsManuales.length && pIMPM.win.MontosModificados){
				bcsComunes.ventanaOverlay(pIMPM.idVentana,true);
				
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
					
					pIMPM.fn.buscar();
					bcsComunes.ventanaOverlay(pIMPM.idVentana,false);
					
				}, function(resError){
					bcsComunes.msg("error",$translate.instant("UI_ITEMS_MANUALES_POSICION_MRF"),$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(pIMPM.idVentana,false);
				});
	        }
			else{
				bcsComunes.msg("warning","UI_ITEMS_MANUALES_POSICION_MRF",$translate.instant("UI_SIN_MODIFICACIONES"));
			}
		};
		
		
		pIMPM.fn.abrirModal = function(itemManualSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalItemsManualesPosicionMRF",{ ItemManualSel: itemManualSel, AccionBoton : accion },false,false,pIMPM.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(respuesta){ 
	        	if(respuesta){
	        		pIMPM.grilla.itemManualSel = undefined;
	        		pIMPM.fn.buscar();
				}
	    	}); 
		};
		
	});	
}());