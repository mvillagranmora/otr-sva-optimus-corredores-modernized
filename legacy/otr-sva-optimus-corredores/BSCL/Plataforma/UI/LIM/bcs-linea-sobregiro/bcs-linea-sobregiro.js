angular.module('ModuloPlataforma').directive('bcsLineaSobregiro',
function($q,$translate,bcsComunes,$timeout,$state){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			idPlantilla : '=?',
			idEntidad	: '=?',// IdPortafolio
			modo 		: '=?',// LS: LineaSobregiro
			uiLimite	: '=?' // indica la ui desde donde se hace la consulta
		},
    	templateUrl: '../../Plataforma/UI/LIM/bcs-linea-sobregiro/bcs-linea-sobregiro.html', 
    	link: function (scope, element, attrs, fn) {

    		console.log("directiva linea sobregiro");
    		
    		/*********************************************
			 * CHECK INICIAL DE VARIABLES
			 ********************************************/
			
    		if(scope.idPlantilla=== undefined) scope.idPlantilla=0;
			if(scope.idEntidad	=== undefined) scope.idEntidad=0;
			if(scope.modo 		=== undefined) scope.modo='';
			if(scope.uiLimite	=== undefined) scope.uiLimite='';
			
    		/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var overlay				= bcsComunes.overlay;
			scope.win 				= scope.win	|| {};
			scope.win.modoLectura 	= true;
			scope.idVentana 		= bcsComunes.ventanaWinId(scope);
			
			scope.cmbbx					= scope.cmbbx 	|| {};
			scope.ds					= scope.ds 		|| {};
			scope.grilla				= scope.grilla 	|| {};
			scope.fn					= scope.fn 		|| {};
			
			scope.dato					= scope.dato 		|| {};
			
			
			var dataDirectiva = {};
			dataDirectiva.IdPlantillaLimite	= 'EXT_'+scope.idEntidad;
			dataDirectiva.IdPortafolio		= scope.idEntidad;
			dataDirectiva.Modo				= scope.modo;
			dataDirectiva.UILimite			= scope.uiLimite;

			/*********************************************
			 * ACCIONES DE PANTALLA
			 ********************************************/					
			
			scope.win.crear = function() {
				scope.fn.abrirModal(scope.grilla.grillaLineaSobregiro.dataSource.data(), 'Agregar');
			};
			scope.win.modificar = function(lineaSobregiroSel) {
				scope.fn.abrirModal(lineaSobregiroSel, 'Modificar');
			};
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			scope.grilla.grillaLineaSobregiro = {
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
						filter: [ { field: "IdPlantillaLimite", operator: "eq", value: 'EXT_'+scope.idEntidad },
						          { field: "IdTipoLimite", operator: "eq", value: 'LIMPORTSOBREGIRO' },
						          //{ field: "IdEstado", operator: "neq", value: 'INA' },
						          { field: "UI_Limite", operator: "eq", value: scope.uiLimite }
						],
						schema: {
							model: {
								fields: {
									IdPlantillaLimite	: { type: "string"},
									IdEmpresa			: { type: "number"},
									IdLimite			: { type: "number"},
									Nombre				: { type: "string"},
									IdMoneda			: { type: "string"},
									NombreMoneda		: { type: "string"},
									MontoTotal			: { type: "number"},
									NombreEstadoLimite	: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
				    {field: "Nombre",		title: $translate.instant("UI_NOMBRE_LIMITE"),	width: "150px"},
				    {field: "IdMoneda",		title: $translate.instant("UI_CODIGO_MONEDA"),	width: "90px"},
					{field: "NombreMoneda",	title: $translate.instant("UI_MONEDA"),			width: "90px"},
					{field: "MontoTotal",	title: $translate.instant("UI_MONTO_LIMITE_SOBREGIRO"),	width: "100px",	template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO',IdMoneda,'','')).valor #</label>"},
					{field: "NombreEstadoLimite",title: $translate.instant("UI_ESTADO"),	width: "50px"}
				],
				sortable			: false,
				pageable			: true,
				autoBind			: true,
				height				: 280,
			    selectable 			: "row"
			};
			
			/*********************************************
			 * COMBOBOX
			 *********************************************/
			
			/*********************************************
			 * GRILLAS
			 *********************************************/
						
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			scope.fn.abrirModal = function(linSobGirSel, p_accion) {
				if(p_accion === 'Agregar' || (linSobGirSel && linSobGirSel.IdEstado !== 'PEN')){
					var id1 = bcsComunes.ventanaAbrir("modalAgregarLineaSobregiro",{LinSobGirSel: linSobGirSel, AccionBoton: p_accion, DataDirectiva: dataDirectiva},false,false);
					bcsComunes.ventanaOverlay(scope.idVentana,true);
					var promesa=bcsComunes.ventanaPromesa(id1);                               
			        promesa.then(function(respuesta){
			        	console.log(respuesta);
			        	if (respuesta.aceptar === true){
			        		scope.grilla.grillaLineaSobregiro.dataSource.read();
			        		scope.grilla.lineaSobregiroSel = undefined;
						}
			        	else {console.log("cierra modal");}
			        	bcsComunes.ventanaOverlay(scope.idVentana,false);
			    	});
				}
			};
					
			/*********************************************
			 * FUNCIONES WATCH
			 *********************************************/			
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			if((scope.idPlantilla!= undefined && scope.idEntidad > 0)){
				scope.win.directivaCargada = true;
			}
			else {
				console.log("directiva mal inicializada");
				scope.win.directivaCargada = false;
			}
    	}
	}
});