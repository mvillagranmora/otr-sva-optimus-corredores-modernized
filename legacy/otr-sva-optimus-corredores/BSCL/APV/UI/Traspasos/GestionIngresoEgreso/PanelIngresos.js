/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelIngresosCtrl',	function($scope,
			$translate,
			$resource,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConsultasAPV,
			bcsServiciosAPV){
		
		$scope.data = {};	
		//Recupera parametros desde la ventana recibida
		var IdVentana    = bcsComunes.ventanaWinId ($scope);	
		$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
		bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

		var panIng = this;
		panIng.datosForm = {};
		panIng.fn = {};
		panIng.datasource = {};
		panIng.ConfiguracionCmbo = {};
		panIng.consulta = {};
		panIng.HabilitoBotonesTraspaso = false;
		//panIng.datosForm.fechaTraspaso = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.gesIngEgr.panIng = panIng;

		//*****************************************************************************************************************/
		//*********************************************** DataSource ******************************************************/
		//*****************************************************************************************************************/
		var defGestionIngresoEgreso = {
			type: "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
	            serverPaging    : true, // Paginado de los datos en el servidor.
	            serverSorting   : true, // Ordenamiento de los datos en el servidor.
	            pageSize        : 100,
	            sort 			: { field: "IdTraspaso", dir: "DESC" },
	            pageSize        : 12,
	            transport		: {
								read: {
									url 		: bcsConsultasAPV('ConsultarTraspasos'),
									dataType	: "json"
							    }
				},
				schema: {
					model: {
						fields: {
							IdTraspaso				: { type: "number"},
							DocumentoIdentidadDefecto : { type: "string"},
							IdSocioNegocioCli		: { type: "number"},
							NombreCompleto			: { type: "string"},
							IdSocioNegocioEMP		: { type: "number"},
							RazonSocial				: { type: "string"},
							IdEmpleador				: { type: "number"},
							DocumentoEntidad		: { type: "string"},	
							NombreEntidad			: { type: "string"},
							FechaIngreso			: { type: "sysdate"},
							FolioContratoSAAPV		: { type: "string"},
							IdProductoAgrupador		: { type: "number"},	
							Producto				: { type: "string"},
							IdPlanTipoInversion		: { type: "number"},
							NombrePlan				: { type: "string"},
							IdRegimenTributario		: { type: "number"},
							Regimen					: { type: "string"},
							IdMatrizAgrupadorAPV	: { type: "number"},
							CodigoAgrupador			: { type: "string"},
							IdPropositoPortafolio	: { type: "string"},
							MontoTotalTraspaso		: { type: "number"},
							UFTotalTraspaso			: { type: "number"},
							PorcentajeTotalTraspaso	: { type: "number"},
							MontoPesoDetalle		: { type: "number"},
							MontoUFDetalle			: { type: "number"},
							PorcentajeDetalle		: { type: "number"},
							CodigoTipoTraspaso		: { type: "string"},
							IdEstado				: { type: "string"},
							IdEstadoContrato		: { type: "string"},
							EstadoNombre			: { type: "string"},
							Canal					: { type: "string"},
							CierreSolicitud			: { type: "sysdate"},
							DiferenciaPorcentajeMonto: { type: "number"}
						}
					}
				}
			}	
		};
		
		var defDetalleTraspaso = {
			type: "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
	            serverPaging    : true, // Paginado de los datos en el servidor.
	            serverSorting   : true, // Ordenamiento de los datos en el servidor.
	            pageSize        : 10,
	            sort 			: { field: "IdDetalleTraspaso", dir: "ASC" },
	            transport		: {
					read: {
						url 		: bcsConsultasAPV('ConsultarDetalleTraspasos'),
						dataType	: "json"
				    }
	            },
				schema: {
					model: {
						fields: {
							IdTraspaso				: { type: "number"},
							IdDetalleTraspaso		: { type: "number"},
							IdSocioNegocioCli		: { type: "number"},
							NombreCompleto			: { type: "string"},
							IdSocioNegocioEMP		: { type: "number"},
							DocumentoIdentidadDefecto : { type: "string"},
							RazonSocial				: { type: "string"},
							DocumentoEntidadOrigen	: { type: "string"},	
							NombreEntidadOrigen		: { type: "string"},
							FechaIngreso			: { type: "sysdate"},
							IdProductoAgrupador		: { type: "number"},	
							Producto				: { type: "string"},
							IdPlanTipoInversion		: { type: "number"},
							NombrePlan				: { type: "string"},
							IdRegimenTributario		: { type: "number"},
							Regimen					: { type: "string"},
							IdMatrizAgrupadorAPV	: { type: "number"},
							CodigoAgrupador			: { type: "string"},
							IdPropositoPortafolio	: { type: "string"},
							MontoTotalTraspaso		: { type: "number"},
							UFTotalTraspaso			: { type: "number"},
							PorcentajeTotalTraspaso	: { type: "number"},
							CodigoTipoTraspaso		: { type: "string"},
							Codigo					: { type: "string"},
							IdDetalleTraspaso		: { type: "number"},
							FechaIngresoDetalle		: { type: "sysdate"},
							MontoPeso				: { type: "number"},
							MontoUF					: { type: "number"},
							Porcentaje				: { type: "number"},
							IdEstadoDetalle			: { type: "string"},
							IdEstado				: { type: "string"},
							EsReapertura			: { type: "string"}						
						}
					}
				},
				aggregate: [
					{ field:"MontoPeso", aggregate: "sum" } ,       
				  ],
			}	
		};
		
		var defDistribucion = 
		{
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				pageSize        : 100,
				sort 			: false,
				transport		: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6010",
						dataType	: "json"
					}
				},
				schema: {
					model: {
						fields: {
							FolioContratoSAAPV  		: {type: "strng"},
							IdInstrumento     			: {type: "number"},
							RazonSocial					: {type: "string"},
							Nemotecnico					: {type: "string"},
							MontoPesos					: {type: "number"},
							PorcentajeRegimenImponible	: {type: "float"},
							IdEstado					: {type: "string"}
						}
					}
				}
			}
		};
		
		//*****************************************************************************************************************/
		//****************************************** Definicion de Grillas ************************************************/
		//*****************************************************************************************************************/
		var dsGestionIngreso = new bcs.DataSource(defGestionIngresoEgreso);
		panIng.datasource.GestionIngresoEgreso = {
				dataSource: dsGestionIngreso,
				columns: [
					{field: "IdTraspaso"				,title: $translate.instant('UI_FOLIO_TRASPASO'), width: "90px", align: "left", sortable : true, editable: false },
					{field: "FolioContratoSAAPV"		,title: $translate.instant('UI_FOLIO_CONTRATO'), width: "90px", align: "left", sortable : true, editable: false },
					{field: "DocumentoIdentidadDefecto"	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "110px", align: "left", sortable : true, editable: false },
					{field: "NombreCompleto"			,title: $translate.instant('UI_SOCIO_NEGOCIO')		, width: "170px", align: "left", sortable : true, editable: false },
					{field: "DocumentoEntidad"			,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_ORIGEN'), width: "120px", align: "left", sortable : true, editable: false },
					{field: "NombreEntidad"				,title: $translate.instant('UI_ENTIDAD_ORIGEN')		, width: "170px", align: "left" , sortable : true, editable: false },
					{field: "CodigoTipoTraspaso"		,title: $translate.instant('UI_TIPO_TRASPASO')		, width: "80px", align: "rigth", sortable : true, editable: false },
					{field: "FechaIngreso"				,title:	$translate.instant('UI_FECHA_INGRESO')		, width: "80px", align: "left" , sortable : true, editable: false},
					{field: "MontoTotalTraspaso"		,title: $translate.instant('UI_MONTO_PESOS_ORIGEN')	, width: "100px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoTotalTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "UFTotalTraspaso" 			,title: $translate.instant('UI_MONTO_UF_ORIGEN')	, width: "100px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(UFTotalTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
				    {field: "PorcentajeTotalTraspaso"	,title: $translate.instant('UI_PORCENTAJE_ORIGEN')	, width: "100px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(PorcentajeTotalTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
				    {field: "MontoPesoDetalle"			,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoPesoDetalle,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "DiferenciaPorcentajeMonto"	,title: $translate.instant('UI_DIFERENCIA_PORCENTAJE_MONTO'), width: "140px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(DiferenciaPorcentajeMonto,'ORD','Tasa','CLP','','')).valor #</label>"},
					{field: "MontoUFDetalle" 			,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoUFDetalle,'ORD','Monto','CLP','','')).valor #</label>"},
					{field: "Canal"	 					,title: $translate.instant('UI_CANAL')				, width: "100px", align: "rigth", sortable : true, editable: false },
					{field: "PorcentajeDetalle"			,title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth", sortable : true, editable: false , hidden: true, template : " <label> #:  (util.bcs_formato(PorcentajeDetalle,'ORD','Monto','CLP','','')).valor #</label>"},
				    {field: "IdEstado"	 				,title: $translate.instant('UI_ESTADO')				, width: "50px", align: "rigth", sortable : true, editable: false , hidden: true},
				    {field: "EstadoNombre"	 			,title: $translate.instant('UI_ESTADO')				, width: "100px", align: "rigth", sortable : true, editable: false },
				    {field: "CierreSolicitud"			,title:	$translate.instant('UI_FECHA_CIERRE')		, width: "80px", align: "left" , sortable : true, editable: false}
				],	
				sortable		: true,
				pageable		: true,
				autoBind		: false,
				height			: 300,
			    selectable 		: "multiple row",
			    userSettings	: false,
	    		filterable		: true,
				navigatable		: true
		};

		var dsDetalleTraspaso = new bcs.DataSource(defDetalleTraspaso);
		panIng.datasource.DetalleTraspaso = {
			dataSource: dsDetalleTraspaso,
			columns: [
				{field: "NombrePlan"			,title: $translate.instant('UI_NOMBRE_PLAN_AHORRO')	, width: "105px", align: "left", sortable : true, editable: false },
				{field: "DocumentoIdentidadDefecto"	,title: $translate.instant('UI_DOCUMENTO_IDENTIDAD')	, width: "95px", align: "left", sortable : true, editable: false },
				{field: "IdTraspaso"			,title: $translate.instant('UI_FOLIO_TRASPASO')	, width: "70px", align: "left", sortable : true, editable: false },
				{field: "Producto"				,title: $translate.instant('UI_PRODUCTO')			, width: "140px", align: "left" , sortable : true, editable: false },
				{field: "Codigo"				,title: $translate.instant('UI_REGIMEN')			, width: "50px", align: "rigth", sortable : true, editable: false },
				{field: "FechaIngresoDetalle"	,title:	$translate.instant('UI_FECHA_INGRESO_REAPERTURA')	, width: "100px", align: "left" , sortable : true, editable: false},
				{field: "MontoPeso"				,title:	$translate.instant('UI_MONTO_PESOS')		, width: "120px", align: "left" , format: "{0:n0}", sortable : true, editable: false, footerTemplate: " <label> #: 'Monto Total = ' #</label> #=kendo.toString(sum,'n0')#",footerAttributes: {style:'text-align:right'}},
				{field: "MontoUF" 				,title: $translate.instant('UI_MONTO_UF')			, width: "60px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoUF,'ORD','Monto','CLP','','')).valor #</label>"},
				{field: "Porcentaje"			,title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth", sortable : true, editable: false  ,hidden: true, template : " <label> #:  (util.bcs_formato(Porcentaje,'ORD','Monto','CLP','','')).valor #</label>"},
				{field: "IdEstadoDetalle"		,title: $translate.instant('UI_ESTADO')				, width: "40px", align: "rigth", sortable : true, editable: false },
				{field: "EsReapertura"			,title: $translate.instant('UI_REAPERTURA')			, width: "50px", align: "rigth", sortable : true, editable: false , template: "# if (EsReapertura == 'S') { # <label>Si</label> # } else { # <label></label> # } #"}
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: false,
			height			: 180,
			selectable 		: "row",
			userSettings	: false,
			navigatable		: true,
			gridCreated		: function(gridDetalle){
				panIng.gridApi = gridDetalle;
			}
		};

		//*****************************************************************************************************************/
		//*********************************************** Consultas *******************************************************/
		//*****************************************************************************************************************/
		panIng.consulta.GestionIngresoEgreso = function(){
			var filtro = Array();
			filtro.push({ field: "TipoMovimiento" , operator: "eq", value: 'INGRESO' });
			
			if(panIng.datosForm.SocioNegocioSeleccionado !== undefined && panIng.datosForm.SocioNegocioSeleccionado !== false){
				filtro.push({ field: "IdSocioNegocioCli" , operator: "eq", value: Number(panIng.datosForm.SocioNegocioSeleccionado.IdSocioDeNegocio) });	
			}
			if (panIng.datosForm.fechaTraspaso != undefined) {
				filtro.push({ field: "FechaIngreso", operator: "eq", value: panIng.datosForm.fechaTraspaso });
			}
			panIng.datasource.DetalleTraspaso.dataSource.removeAll();
			panIng.datasource.GestionIngresoEgreso.dataSource.filter(filtro);
		};

        panIng.consulta.ConsultaDetalleTraspaso = function(TraspasosSeleccionados){
            dsDetalleTraspaso.removeAll();
            var filtro = {
                logic: "or",
                filters: []
            };
            TraspasosSeleccionados.forEach(function(traspaso) {
                filtro.filters.push(
                    { field: "IdTraspaso", operator: "eq", value: traspaso.IdTraspaso },
                    { field: "IdContratoSAAPV", operator: "eq", value: traspaso.IdContratoSAAPV }
                );
            });
            if (TraspasosSeleccionados.some(function(traspaso) {
                return traspaso.IdEstado !== 'PDI';
            })) {
                panIng.datasource.DetalleTraspaso.dataSource.filter(filtro);
            }
        };


		//*****************************************************************************************************************/
		//*********************************************** Funciones *******************************************************/
		//*****************************************************************************************************************/
		panIng.fn.IngresoTraspaso = function(ModoVentana, dobleClick){
			var filterVentana = Array();
			
			if(panIng.datasource.GestionIngresoEgresoSeleccion === undefined || panIng.datasource.GestionIngresoEgresoSeleccion === false){
				bcsComunes.msg("warning","warning","Debe seleccionar uno o varios traspasos.");
				return;
			}
			
			if(ModoVentana === 'I'){
				filterVentana.ModoVentana = ModoVentana;
			}else if(ModoVentana === 'M'){
				//ModoVentana = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'ING' || panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM'? 'C':'M';
				ModoVentana = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM'? 'C':'M';
				filterVentana.ModoVentana = ModoVentana;
				filterVentana.IdContratoSAAPV = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
				filterVentana.IdTraspaso = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdTraspaso;
				if(panIng.datasource.DetalleTraspaso.dataSource.data().length > 0){
					filterVentana.TipoVentana = 'MOD'; //modificar detalles traspaso 
				}else{filterVentana.TipoVentana = 'ING';} //ingresar 1er detalle traspaso
				filterVentana.DobleClick = dobleClick;
			}else if(ModoVentana === 'R'){
				filterVentana.ModoVentana = ModoVentana;
				filterVentana.IdContratoSAAPV = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
				filterVentana.IdTraspaso = panIng.datasource.GestionIngresoEgresoSeleccion[0].IdTraspaso;
				filterVentana.TipoVentana = 'MOD';
			}
			//if(dsDetalleTraspaso.data().length === 0){
				var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoTraspasoAPV", filterVentana, true, false, IdVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					panIng.consulta.GestionIngresoEgreso();
		        });	
			//}
		};
		
		panIng.fn.IngresoRecaudacion = function(Traspaso){
			console.log("Traspaso : ", Traspaso);
			if(Traspaso  === undefined){
				bcsComunes.msg("warning","warning","Debe seleccionar uno o varios traspasos.");
				return;
			}else{
                var nombreEntidad = Traspaso[0].NombreEntidad;
            }

            var NombreEntidadOK = true;
            var EstadoTraspasoNoOk = false;
            var MontoDeposito = 0;

            $.each(Traspaso,function(key,value){
                if(value.NombreEntidad !== nombreEntidad)NombreEntidadOK = false;
                if(value.IdEstado === 'ING') MontoDeposito = MontoDeposito + value.MontoPesoDetalle;
                else EstadoTraspasoNoOk = true;
                   
            });

			var EstadoTraspasoNoOk = false;
			var MontoDeposito = 0;
			
			$.each(Traspaso,function(key,value){
				if(value.IdEstado === 'ING') MontoDeposito = MontoDeposito + value.MontoPesoDetalle;
				else EstadoTraspasoNoOk = true;	
			});

			if (NombreEntidadOK === false && EstadoTraspasoNoOk === true) {
				bcsComunes.msg("warning", "warning", " No se puede recaudar traspasos de ingreso con entidad de origen diferente y solo se pueden recaudar traspasos en estado INGRESADO.");
				return;
			}else if(NombreEntidadOK === false){
                bcsComunes.msg("warning","warning", " No se puede recaudar traspasos de ingreso con entidad de origen diferente.");
                return;
            }else if(EstadoTraspasoNoOk === true){
				bcsComunes.msg("warning","warning", " Solo se pueden recaudar traspasos en estado INGRESADO.");
				return;
			}
			var TablaRecaudacionTraspaso = Array();
			var filterVentana = {};
			filterVentana.Solicita = 'TRASPASO';
			filterVentana.MontoDeposito = MontoDeposito;
			filterVentana.IdSucursal = 0;
			filterVentana.CuentaCorriente = '';
			filterVentana.IdTipoDeposito = '';
			filterVentana.IdBanco = 0;
			filterVentana.FolioCartola = '';
			filterVentana.LineaCartola = '';
			var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoRecaudacionDeposito", filterVentana, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				if(data === undefined || Object.entries(data).length === 0) return;
				var entrada = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'APV',
					p_TipoTraspaso: 'I',
					p_Recaudacion : {
						FolioCartola : data.FolioCartola,
						LineaCartola : data.LineaCartola,
						IdSucursal : data.IdSucursal,
						CuentaCorriente : data.CuentaCorriente,
						IdTipoDeposito : data.IdTipoDeposito,
						IdBanco : data.IdBanco
					},
					p_Traspasos : []
				}
				$.each(Traspaso,function(key,value){
					entrada.p_Traspasos.push({IdTraspaso : value.IdTraspaso, MontoTraspaso : value.MontoPesoDetalle});
				});
				bcsComunes.ventanaOverlay(IdVentana,true);
				bcsServiciosAPV.IngresoRecaudacionTraspaso(entrada).$promise.then(function(respuesta){
					
					 if(respuesta.P_OUT_IDRESULTADO.length > 0){
						 var mensajeTotal = new Array();
						 var _objMensaje = {
									Id : 1,
									tipo : 'INFORMACION',
									codigo : '',
									descripcion : "Ingreso Recaudacion Nº: " + respuesta.P_OUT_IDRESULTADO[0].IdResultado
							 };
						 mensajeTotal.push(_objMensaje);
						 $.each(respuesta.P_OUT_MENSAJES,function(key,value){
							 var objMensaje = {
									Id : value.id,
									tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
									codigo : value.codigo,
									descripcion : $translate.instant(value.descripcion)
							 };
							 mensajeTotal.push(objMensaje);
						 });
						 respuesta.P_OUT_MENSAJES = mensajeTotal;
						 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},IdVentana);
						 panIng.consulta.GestionIngresoEgreso();
						 bcsComunes.ventanaOverlay(IdVentana,false);
					 }else{
						 var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
						 $.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas:" + value );
						 });
						 panIng.consulta.GestionIngresoEgreso();
						 bcsComunes.ventanaOverlay(IdVentana,false);
					 }
		   		},function(respuesta){
					console.log("Respuesta Java IT:", respuesta);
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},IdVentana);
		   			bcsComunes.ventanaOverlay(IdVentana,false);
		   		});
				
			});
		};	

		panIng.fn.ReaperturarTraspaso = function(dsDetalleTraspaso){
			if(dsDetalleTraspaso  === undefined){
				bcsComunes.msg("warning","warning","Debe seleccionar uno o varios traspasos.");
				return;
			}
			if(dsDetalleTraspaso.EsReapertura === 'S'){
				bcsComunes.msg("warning","warning","Traspaso seleccionado ya ha sido reaperturado.");
				return;
			}
			dsDetalleTraspaso.EsReapertura = "S";
			dsDetalleTraspaso.FechaIngresoDetalle = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			panIng.gridApi.refresh();

		};
		
		panIng.fn.ValidoBotones = function(tipo){
			if(tipo === 'DT'){
				if(panIng.datasource.GestionIngresoEgresoSeleccion !== undefined){
					if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM' || panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'ANU'
						|| panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstadoContrato === 'CER'){
						return true;
					}
					else{
						return false;
					}
				}else{
					return false;
				}
			}			
		}

		panIng.fn.ValidoBotonReapertura = function(tipo){
			if(tipo === 'RA'){
				if(panIng.datasource.GestionIngresoEgresoSeleccion !== undefined){
					if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM' || panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstadoContrato === 'CER'){
						return true;
					}
					else{
						return false;
					}
				}else{
					return false;
				}
			}			
		}
		
		panIng.VentanaIngresoTraspaso = function(){
			var IdVentanamodal = bcsComunes.ventanaAbrir("RegistroTraspasoAPV", { modoVentana : 'I', CodigoSolicitud : 'TRA', tipoTraspaso: 'ING' }, true, false, IdVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				panIng.consulta.GestionIngresoEgreso();	
	        });
		};
		
		panIng.VentanaContratoSAAPV = function(modoVentana)
		{
			var filterVentana = Array(); 
			filterVentana.modoVentana = modoVentana;
			
			if(modoVentana === 'I')
			{
				var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, idVentana);
			}
			else
			{		
				if(modoVentana === 'M')
				{
					if(panIng.datasource.GestionIngresoEgresoSeleccion !== undefined)
					{
						if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'PDI' && panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstadoSolicitud === 'PDA')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_PARA_MODIFICAR_CONTRATO_NO_DEBE_ESTAR_RECHAZADO_PENDIENTE_AUTORIZACION'));
							return;
						}
						else if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'REV')
						{
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_REVOCADO'));
							return;
						}else if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'COM'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_COMPLETADO'));
							return;
						}else if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'CER'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_CERRADO'));
							return;
						}else if(panIng.datasource.GestionIngresoEgresoSeleccion[0].IdEstado === 'ANU'){
							bcsComunes.msg("warning",'warning',$translate.instant('UI_NO_PUEDE_MODIFICAR_CONTRATO_ANULADO'));
							return;
						}
						
						filterVentana.IdContratoSAAPV 			= panIng.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
						filterVentana.DocumentoIdentidad		= panIng.datasource.GestionIngresoEgresoSeleccion[0].DocumentoIdentidadDefecto.replace(/[.]/gi,'');
						filterVentana.DocumentoIdentidadOrigen	= panIng.datasource.GestionIngresoEgresoSeleccion[0].CodigoEntidad;
						filterVentana.CodigoTipoTraspaso		= panIng.datasource.GestionIngresoEgresoSeleccion[0].CodigoTipoTraspaso;
					}
					else
					{
						bcsComunes.msg("warning",'warning',$translate.instant('UI_DEBE_SELECCIONAR_CONTRATO_PARA_MODIFICAR'));
						return;
					}
							
					var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, IdVentana);
				}
				else if(modoVentana === 'C')
				{
					filterVentana.IdContratoSAAPV 		= panIng.datasource.GestionIngresoEgresoSeleccion[0].IdContratoSAAPV;
					var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaContratoSAAPV", filterVentana, true, false, IdVentana);
				} 
			}
			
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data)
			{
				if(modoVentana === 'M')
				{
					panIng.consulta.GestionIngresoEgreso();
				}
	        });
		};
		
	});

}());
