/*************************************************************
 * Purpose	: Mantenedor de Tipo de Relaciones
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Se incorporan definiciones Optimus Perú
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('MantenedorTipoRelacionesCtrl',
	function($scope,$translate,bcsComunes,$uibModal){	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var overlay = bcsComunes.overlay;
		var mtr = this;
		mtr.formData = {};
		mtr.formData.EsPersonaNatural = false;
		mtr.formData.EsIndefinida = false;
		mtr.ventanaId = bcsComunes.ventanaWinId($scope);
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		mtr.agregar = function() {
			mtr.abrirModal(undefined, 'Agregar');
		};
		mtr.modificar = function(tipoRelSel) {
			mtr.abrirModal(tipoRelSel, 'Modificar');
		};
		/*********************************************
		 * GRILLAS
		 *********************************************/
		mtr.gridTipoRelaciones = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1298",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdTipoRelacion					: { type: "string"},
								EsPersonaNatural				: { type: "string"},
								IdTipoSocioDeNegocio			: { type: "string"},
								NombreTipoRelacion				: { type: "string"},
								EsIndefinida					: { type: "string"},
								IdEstado						: { type: "string"},
								UsaPorcentajeParticipacion  	: { type: "string"},
								TextoUsaPorcentajeParticipacion : { type: "string"},
								RetiraDocumentos				: { type: "string"},
								TextoRetiraDocumentos 			: { type: "string"},
								UsaFinalidadRelacion 			: { type: "string"},
								TextoUsaFinalidadRelacion 		: { type: "string"},
								UsaCondicionesRelacion 			: { type: "string"},
								TextoUsaCondicionesRelacion 	: { type: "string"},
								UsaGradoVinculacionParental 	: { type: "string"},
								TextoUsaGradoVinculacionParental: { type: "string"},
								EsPersonaNaturalGrilla			: { type: "string"},
								EsIndefinidaGrilla				: { type: "string"},
								NombreEstado					: { type: "string"},
								UsaFirmaDocumento				: { type: "string"},
								TextoUsaFirmaDocumento			: { type: "string"},
								AccesoOptimusCliente			: { type: "string"},
								TextoAccesoOptimusCliente		: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreTipoRelacion", title: $translate.instant('UI_TIPO_RELACION'), attributes: {style:"text-align:left;"}, width: "180px"},
				{field: "EsPersonaNaturalGrilla", title: $translate.instant('UI_ES_PERSONA_NATURAL'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoUsaPorcentajeParticipacion", title: $translate.instant('UI_USA_PORCENTAJE_PARTICIPACION'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoRetiraDocumentos", title: $translate.instant('UI_RETIRA_DOCUMENTOS'), attributes: {style:"text-align:left;"}, width: "60px"},
				{field: "TextoUsaFinalidadRelacion", title: $translate.instant('UI_USA_FINALIDAD_RELACION'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoUsaCondicionesRelacion", title: $translate.instant('UI_USA_CONDICIONES_RELACION'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoUsaFirmaDocumento", title: $translate.instant('UI_USA_FIRMA_DOCUMENTO_RELACION'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoAccesoOptimusCliente", title: $translate.instant('UI_ACCESO_OPTIMUS_CLIENTE'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "TextoUsaGradoVinculacionParental", title: $translate.instant('UI_USA_GRADO_VINCULACION_PARENTAL'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "EsIndefinidaGrilla", title: $translate.instant('UI_ES_INDEFINIDA'), attributes: {style:"text-align:left;"}, width: "70px"},
				{field: "NombreEstado", title: $translate.instant('UI_ESTADO'), attributes: {style:"text-align:left;"}, width: "60px"},
			
			],	
			sortable			: false,
			pageable			: true,
			autoBind			: false,
			height				: 470,
			userSettings		: true,
			navigatable			: true,
			excel : { allPages: true,},
		    selectable 			: "row"
		};
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		mtr.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
			if (mtr.formData.EsPersonaNatural === false && mtr.formData.EsIndefinida === false){
				niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			}else{
				niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
				niv1filters.push({ field: "tolower(Administracion)", operator: "eq", value: 'relacionados' });
			}	
			if (mtr.formData.NombreTipoRelacion)
				niv1filters.push({ field: "tolower(NombreTipoRelacion)", operator: "contains", value: mtr.formData.NombreTipoRelacion.toLowerCase() });
	    	if (mtr.formData.IdTipoEntidadOrigen)
	    		niv1filters.push({ field: "tolower(IdTipoEntidadOrigen)", operator: "eq", value: mtr.TipoEntidadOrigenObj.NombreTipoSocioDeNegocio.toLowerCase() });	    	
	    	if (mtr.formData.EsPersonaNatural)
				niv1filters.push({ field: "EsPersonaNatural", operator: "eq", value: mtr.formData.EsPersonaNatural?'S':'N'});
			if (mtr.formData.EsIndefinida)
				niv1filters.push({ field: "EsIndefinida", operator: "eq", value: mtr.formData.EsIndefinida?'S':'N'});
	    	
	    	
	    	if (niv1filters.length>0) mtr.gridTipoRelaciones.dataSource.filters(objfilters);
	    	else  mtr.gridTipoRelaciones.dataSource.filters(new Array());
		};
		
		mtr.limpiar = function(){
			mtr.formData.NombreTipoRelacion	= '';
			mtr.formData.EsPersonaNatural	= false;
			mtr.formData.EsIndefinida		= false;
			mtr.gridTipoRelaciones.dataSource.removeAll();
		}
		
		mtr.abrirModal = function(p_TipoRelSel, accion){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarTipoRelacion",{ TipoRelSel: p_TipoRelSel, AccionBoton : accion },false,false,mtr.ventanaId);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
	        		mtr.tipoRelacionSeleccionada = undefined;
	        		mtr.buscar();
				}
	    	}); 
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------