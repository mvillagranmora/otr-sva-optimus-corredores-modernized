(function() {
	angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPersonaRelacionesEnrolCtrl',
	function($scope,$translate,bcsComunes,servicioSociodeNegocio){
	/******************************************************
	 * INICIO DE VARIABLES
	 * ***************************************************/
		var ppr = this;
		var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		ppr.IdEnrolamiento = parametros.IdEnrolamiento;
		ppr.CodigoFormulario = parametros.Matriz['RELACIONES'];
		var idVentana = bcsComunes.ventanaWinId($scope);
		
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/
		
		ppr.agregar = function() {
			ppr.abrirModal(ppr.IdEnrolamiento, undefined, ppr.CodigoFormulario);
		};
		
		ppr.modificar = function(relSel) {
			ppr.abrirModal(ppr.IdEnrolamiento, relSel, ppr.CodigoFormulario);
		};
		
		ppr.eliminar = function(relSel) {
			bcsComunes.ventanaOverlay(idVentana,true);
			var datos = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'SOC',
					p_UsaReglasNegocio	: 'S',
					p_CodigoFormulario  : ppr.CodigoFormulario,
					p_IdTipoCanal		: 'AGE',
					p_RelacionesPersona	: [{
						IdInterno					: 1,
						IdRelacionPersona			: relSel.IdRelacionPersona || 0,
						IdEnrolamientoOrigen		: relSel.IdEnrolamiento,
						IdTipoRelacion				: relSel.IdTipoRelacion || ""
					}]	
			};
			servicioSociodeNegocio.eliminarBorradorRelacionPersona(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result.P_OUT_ERRORESCONTADOR > 0){
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_ELIMINAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							ppr.relacionSeleccionada = undefined;
							ppr.gridRelacionados.dataSource.read();
						});
						return;
					}
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_RELACION_ELIMINADA'),conflictos : util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						ppr.relacionSeleccionada = undefined;
						ppr.gridRelacionados.dataSource.read();
					});
				},
				function(result){
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_ELIMINAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						ppr.relacionSeleccionada = undefined;
						ppr.gridRelacionados.dataSource.read();
					});
				}
			);
		};	
		
	/*********************************************
	 * DATASOURCES
	 *********************************************/
		 
		ppr.gridRelacionados = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700211",
							dataType: "json"
						}
					},
					filter: { field: "IdEnrolamientoOrigen", operator: "eq", value: Number(ppr.IdEnrolamiento) },
					schema: {
						model: {
							fields: {
								IdRelacionPersona                  : { type: "number" },
								IdTipoRelacion                     : { type: "string" },
								NombreTipoRelacion                 : { type: "string" },
								EsIndefinida                       : { type: "string" },
								IdEnrolamientoOrigen               : { type: "number" },
								RazonSocialOrigen                  : { type: "string" },
								NombreLargo                        : { type: "string" },
								IdPersonaDestino                   : { type: "number" },
								TipoPersonaDestino                 : { type: "string" },
								RazonSocialDestino                 : { type: "string" },
								NombreCompletoDestino              : { type: "string" },
								IdTipoDocumentoIdentidadDestino    : { type: "string" },
								DocumentoIdentidadDestino          : { type: "string" },
								NombreTipoDocumentoIdentidadDestino: { type: "string" },
								FechaInicioRelacion                : { type: "sysdatetime" },
								FechaFinRelacion                   : { type: "sysdatetime" },
								UsaPorcentajeParticipacion         : { type: "string" },
								PorcentajeParticipacion            : { type: "string" },
								RetiraDocumentos                   : { type: "string" },
								TextoRetiraDocumentos              : { type: "string" },
								FinalidadRelacion                  : { type: "string" },
								CondicionesRelacion                : { type: "string" },
								IdGradoVinculacionParental         : { type: "number" },
								NombreGradoVinculacionParental     : { type: "string" },
								IdCiudad                           : { type: "number" },
								Calle                              : { type: "string" },
								Numero                             : { type: "string" },
								Email                              : { type: "string" },
								Telefono                           : { type: "string" },
								Complemento                        : { type: "string" },
								Ciudad                             : { type: "string" },
							}
						}
					}
				}
			}),
			columns: [
			    {	
			    	field: "TipoPersonaDestino", 
			    	title: $translate.instant('UI_TIPO_PERSONA'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "110px"
			    },
			    {	
			    	field: "NombreTipoDocumentoIdentidadDestino", 
			    	title: $translate.instant('UI_TIPO_DOCUMENTO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "90px"
			    },
			    {
			    	field: "DocumentoIdentidadDestino", 
			    	template:"<label> #:  IdTipoDocumentoIdentidadDestino === 'R' ? util.formatRut(DocumentoIdentidadDestino) : DocumentoIdentidadDestino #</label>", 
			    	title: $translate.instant('UI_NUMERO_DOCUMENTO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "110px"
			    },
				{
			    	field: "NombreCompletoDestino", 
			    	title: $translate.instant('UI_RAZON_SOCIAL_O_NOMBRE'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "250px"
			    },
				{
			    	field: "NombreTipoRelacion", 
			    	title: $translate.instant('UI_TIPO_RELACION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "180px"
			    },
			    {
			    	field: "PorcentajeParticipacion", 
			    	title: $translate.instant('UI_PORCENTAJE_PARTICIPACION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	template: kendo.template("#if (UsaPorcentajeParticipacion==='S') {##=PorcentajeParticipacion# %#} else {#-#}#"),
			    	width: "180px"
			    },
			    {
			    	field: "TextoRetiraDocumentos", 
			    	title: $translate.instant('UI_RETIRA_DOCUMENTOS'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "180px"
			    },
			    {
			    	field: "FinalidadRelacion", 
			    	title: $translate.instant('UI_FINALIDAD_RELACION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "250px"
			    },
			    {
			    	field: "CondicionesRelacion", 
			    	title: $translate.instant('UI_CONDICIONES_RELACION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "250px"
			    },
			    {
			    	field: "NombreGradoVinculacionParental", 
			    	title: $translate.instant('UI_GRADO_VINCULACION_PARENTAL'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "180px"
			    },
				{
			    	field: "NombreLargo", 
			    	title: $translate.instant('UI_DIRECCION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "80px"
				},
				{
			    	field: "Email", 
			    	title: $translate.instant('UI_CORREO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "80px"
				},
				{
			    	field: "Telefono", 
			    	title: $translate.instant('UI_TELEFONO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "80px"
			    },
				{
			    	field: "FechaInicioRelacion", 
			    	title: $translate.instant('UI_FECHA_INICIO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	format: "{0:d}", 
			    	width: "85px",
			    	template: kendo.template("#if (kendo.toString(FechaInicioRelacion, 'dd-MM-yyyy')=='31-12-0001') {##} else {##=kendo.toString(FechaInicioRelacion, 'dd-MM-yyyy')##}#")
			    },
				{
			    	field: "FechaFinRelacion", 
			    	title: $translate.instant('UI_FECHA_TERMINO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	format: "{0:d}", 
			    	width: "85px",
			    	template: kendo.template("#if (kendo.toString(FechaFinRelacion, 'dd-MM-yyyy')=='31-12-0001') {##} else {##=kendo.toString(FechaFinRelacion, 'dd-MM-yyyy')##}#")
			    }
			],	
			sortable			: false,
			pageable			: true,
			autoBind			: true,
			height				: 200,
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
	 ********************************************/		
		ppr.abrirModal = function(p_IdEnrolamiento, p_RelSel, p_CodigoFormulario){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoRelacion",{ IdEnrolamiento: p_IdEnrolamiento, RelSel: p_RelSel, CodigoFormulario: p_CodigoFormulario},false,false,idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					ppr.relacionSeleccionada = undefined;
					ppr.gridRelacionados.dataSource.read();
				}
	    	}); 
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------