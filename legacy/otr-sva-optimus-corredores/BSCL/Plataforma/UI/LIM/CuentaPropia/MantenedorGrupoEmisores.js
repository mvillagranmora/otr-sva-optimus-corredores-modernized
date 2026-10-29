(function() {
	angular.module('ModuloPlataforma').controller('MantenedorGrupoEmisoresCtrl',
		function ($scope,bcsComunes,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mGE 		= this;
		mGE.idVentana	= bcsComunes.ventanaWinId($scope);
		mGE.win			= {};
		mGE.grilla		= {};
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		mGE.win.agregar = function() {
			mGE.abrirModal(undefined, 'Agregar');
		};
		
		mGE.win.modificar = function(grupoEmiSel) {
			mGE.abrirModal(grupoEmiSel, 'Modificar');
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		mGE.grilla.GrupoEmisores = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=412",
							dataType: "json"
						}
					},
					filter: [
					    { field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa },
					    { field: "toupper(IdGrupoEmisor)", operator: "neq",value: 'DEFAULT' }
					],
					schema: {
						model: {
							fields: {
								IdGrupoEmisor	: { type: "string"},
								Descripcion		: { type: "string"},
								CantidadEmisores: { type: "number"},
								EstadoGrupoEmisor:{ type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				{field: "IdGrupoEmisor",	title: $translate.instant("UI_CODIGO"),				width: "130px"},
				{field: "Descripcion",		title: $translate.instant("UI_DESCRIPCION"),		width: "170px"},
				{field: "CantidadEmisores",	title: $translate.instant("UI_CANTIDAD_EMISORES"),	width: "80px"},
				{field: "EstadoGrupoEmisor",title: $translate.instant("UI_ESTADO"),				width: "70px"}
			],
			sortable	: false,
			pageable	: true,
			autoBind	: true,
			height		: 380,
		    selectable 	: "row"
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mGE.abrirModal = function(grupoEmiSel, p_accion){
	        if(p_accion === 'Agregar' || (grupoEmiSel && grupoEmiSel.IdEstado !== 'PEN')){
				var id1 = bcsComunes.ventanaAbrir("ModalGrupoEmisores",{GrupoEmisorSel: grupoEmiSel, AccionBoton: p_accion},false,true);
				
				var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(respuesta){
		        	console.log(respuesta);
	        		mGE.grilla.GrupoEmisores.dataSource.read();
	        		mGE.grilla.grupoEmisorSel = undefined;
		    	});
			}
		};
	});
}());