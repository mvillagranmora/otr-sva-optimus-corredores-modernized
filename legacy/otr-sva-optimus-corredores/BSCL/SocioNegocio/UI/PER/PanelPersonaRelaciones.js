/*************************************************************
 * Purpose	: Panel Persona Relaciones
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaRelacionesCtrl',
	function($scope,$translate,bcsComunes,$uibModal,servicioSociodeNegocio){
	/******************************************************
	 * INICIO DE VARIABLES
	 * ***************************************************/
		var ppr = this;
		var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		ppr.IdPersona = parametros.IdPersona;
		var idVentana = bcsComunes.ventanaWinId($scope);

		$scope.admSoc.socioNegocioPromise.then(function () {
			ppr.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['RELACIONES'];
		});
		
	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/
		
		ppr.showBloqueo = false;
		$scope.$watch("ppr.relacionSeleccionada",
	        function handleRelacionado(newValue, oldValue) {
				if(newValue !== undefined){
					ppr.showBloqueo = newValue.IdEstado === 'ACT' ? true : false;
				}
	        }
	    );
		
		ppr.agregar = function() {
			ppr.abrirModal(ppr.IdPersona, undefined);
		};
		ppr.modificar = function(relSel) {
			ppr.abrirModal(ppr.IdPersona, relSel);
		};
		
		ppr.bloqRelDesbloqRel = function(accion){
			var registroCambio = [];
			registroCambio.push({CampoModificado : "IdEstado", ValorNuevo : accion});
			if(ppr.relacionSeleccionada.UsaPorcentajeParticipacion === "S"){
				if(accion === 'BLO'){
					registroCambio.push({CampoModificado : "PorcentajeParticipacion", ValorNuevo : 0});
				}else{
					if(ppr.totalPorcentajeParticipacion > 90){
						bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_SUMA_PORCENTAJE'));
						bcsComunes.ventanaOverlay(idVentana,false);
						return;
					}
					registroCambio.push({CampoModificado : "PorcentajeParticipacion", ValorNuevo : 10});
				}
			}
			var datos = {
				p_IdEmpresa			: $scope.SesionIdEmpresa,
				p_IdUsuario			: $scope.SesionIdUsuario,
				p_IdModulo			: 'SOC',
				p_UsaReglasNegocio	: 'S',
				p_IdRelacionPersona	: ppr.relacionSeleccionada.IdRelacionPersona,
				p_RegistroCambio	: registroCambio
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			servicioSociodeNegocio.modificarPersonaRelacion(datos).$promise.then(
	        	function(result){
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						var accionMensaje 		= (accion === 'BLO') ? 'Bloqueada' : 'Desbloqueada';
						var mensajeDiccionario	= "UI_SOCIO_NEGOCIO_PERSONA_ESTADO_RELACION";
					} else {
						var mensajeDiccionario	= result.P_OUT_ERRORESDETALLE[0].Codigo;
					}
		        	
	        		ppr.relacionSeleccionada = undefined;
	        		ppr.gridRelacionados.dataSource.read();
		        	
	        		bcsComunes.ventanaMsg({ mensaje:$translate.instant(mensajeDiccionario).replace("::EstadoRelacion::", accionMensaje),
						conflictos : util.parseServerResponse(result)}, idVentana);
	        		
	        		bcsComunes.ventanaOverlay(idVentana,false);
		        },
		        function(result){
		        	bcsComunes.msg("error","Error","La operacion tiene errores");
		        	bcsComunes.ventanaOverlay(idVentana,false);
		        });
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
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1293",
							dataType: "json"
						}
					},
					filter: { field: "IdPersonaOrigen", operator: "eq", value: Number(ppr.IdPersona) },
					schema: {
						model: {
							fields: {
								IdRelacionPersona		: { type: "number"},
								DocumentoIdentidad		: { type: "string"},
								IdTipoRelacion			: { type: "string"},
								NombreTipoRelacion		: { type: "string"},
								EsIndefinida			: { type: "string"},
								IdPersonaOrigen			: { type: "number"},
								RazonSocialOrigen		: { type: "string"},
								IdPersonaDestino		: { type: "number"},
								IdEstado				: { type: "string"},
								FechaIngreso			: { type: "sysdatetime"},
								FechaInicioRelacion		: { type: "sysdatetime"},
								FechaFinRelacion		: { type: "sysdatetime"},
								RetiraDocumentos		: { type: "string"},
								TextoRetiraDocumentos	: { type: "string"},
								FinalidadRelacion		: { type: "string"},
								CondicionesRelacion		: { type: "string"},
								IdGradoVinculacionParental		: { type: "number"},
								NombreGradoVinculacionParental	: { type: "string"},
								TipoPersonaDestino				: { type: "string"},
								NombreTipoDocumentoIdentidadDestino : { type: "string"},
								DocumentoIdentidadDestino 		: { type: "string"},
								NombreCompletoDestino			: { type: "string"},
								PorcentajeParticipacion			: { type: "string"},
								NombreEstado					: { type: "string"},
								Email							: { type: "string"},
								NombreLargo						: { type: "string"},
								Telefono						: { type: "string"},
								FechaModificacion				: { type: "sysdate"}
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
			    	field: "NombreEstado", 
			    	title: $translate.instant('UI_ESTADO'), 
			    	attributes: {style:"text-align:left;"}, 
			    	width: "80px"
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
			    },
			    {
			    	field: "FechaModificacion", 
			    	title: $translate.instant('UI_FECHA_MODIFICACION'), 
			    	attributes: {style:"text-align:left;"}, 
			    	format: "{0:d}", 
			    	width: "100px"
			    }
			],	
			sortable			: false,
			pageable			: true,
			autoBind			: true,
			height				: 200,
		    selectable 			: "row"
		};
		ppr.gridRelacionados.dataBound = function (e) {
			const data = e.sender.dataSource.data();
			let total = 0;
			data.forEach(function (item) {
				const porcentaje = parseFloat(item.PorcentajeParticipacion);
				if (!isNaN(porcentaje)) {
					total += porcentaje;
				}
			});
			ppr.totalPorcentajeParticipacion = total;
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
		ppr.abrirModal = function(p_IdPersona, p_RelSel){
			var id1 = bcsComunes.ventanaAbrir("agregarModificarRelacion", {
				IdPersona: p_IdPersona,
				RelSel: p_RelSel,
				CodigoFormulario: ppr.CodigoFormulario
			}, false, false, idVentana);
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