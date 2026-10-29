angular.module('ModuloAPV').controller('IngresoTraspasoCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsConsultasAPV,bcsServiciosAPV,bcsAlerta,OPCIONES_BCSALERTA)
{
	
	$scope.data = {};	
	
	//Recupera parametros desde la ventana recibida
	$scope.IdVentana    = bcsComunes.ventanaWinId ($scope);
	var idVentana 		= $scope.IdVentana;
	var IdVentana    = bcsComunes.ventanaWinId ($scope);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
	bcsComunes.ventanaOverlay(idVentana,true);
	var productoAgrup = false;
	
	var ingtraAPV = this;
	ingtraAPV.gridApi = {};
	ingtraAPV.datosForm = {};
	ingtraAPV.fn = {};
	ingtraAPV.datasource = {};
	ingtraAPV.ConfiguracionCmbo = {};
	ingtraAPV.consulta = {};
	ingtraAPV.parametrosVentana = {}
	ingtraAPV.parametrosVentana = bcsComunes.ventanaParams($scope.IdVentana);
	ingtraAPV.datosForm.fechaTraspaso = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	ingtraAPV.IdContratoSAAPV = 0;
	ingtraAPV.IdTraspaso = 0;
	ingtraAPV.IdPortafolio = 0;
	ingtraAPV.MontoPesoOrigenDeshabilitado = true;
	ingtraAPV.MontoUFOrigenDeshabilitado = true;
	ingtraAPV.MontoPesoOrigenRequerido = true;
	ingtraAPV.MontoUFOrigenRequerido = true;

	if(ingtraAPV.parametrosVentana.ModoVentana === 'R') bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_INGRESO_DETALLE_REAPERTURA'));	

	//*****************************************************************************************************************/
	//********************************************* Configuracion *****************************************************/
	//*****************************************************************************************************************/
	ingtraAPV.ConfiguracionCmbo.SocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}		
	};
	
	ingtraAPV.ConfiguracionCmbo.SocioNegocioOrigen = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsPersona : {"field": "EsPersona","operator": "eq", "value"   :  'UI_SI'}
		}		
	};
	
	ingtraAPV.ConfiguracionCmbo.ProductoAgrupador =  {
		filtros: {
			IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
		}	
	};

	ingtraAPV.ConfiguracionCmbo.RegimenAgrupador = {
		filtros: {
			IdProductoAgrupador : {"field": "IdProductoAgrupador", "operator": "eq", "value" : 0}
		}
	};
	
	ingtraAPV.ConfiguracionCmbo.PerfilComercial = {
			tipo 	: 'combobox-perfil-comercial', 
			filtros : { 
				IdCategoriaInversionista : {
						"field"   : "IdCategoriaInversionista",
						"operator": "eq",
						"value"   : "" }},
	};
	//*****************************************************************************************************************/
	//*********************************************** DataSource ******************************************************/
	//*****************************************************************************************************************/
	var defTraspaso = {
		type: "odata",
		definition : {
			serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            pageSize        : 100,
            sort 			: { field: "IdTraspaso", dir: "DESC" },
            pageSize        : 10,
            transport		: {
				read: {
					url 		: bcsConsultasAPV('ConsultarTraspasos'),
					dataType	: "json"
			    }
			},
			schema: {
				model: {
					fields: {
						   IdTraspaso				: {type: "number"}
						 , IdContratoSAAPV			: {type: "number"}
						 , TipoMovimiento			: {type: "string"}
						 , RazonRetiro				: {type: "string"}
						 , FechaIngreso				: {type: "sysdate"}
						 , CodigoTipoTraspaso		: {type: "string"}
						 , IdCliente				: {type: "number"}
						 , IdPersona				: {type: "number"}
						 , Descripcion				: {type: "string"}
						 , IdSocioNegocioCli		: {type: "number"}
						 , IdSocioNegocioEMP		: {type: "number"}
						 , IdDetalleOrigenTraspaso	: {type: "number"}
						 , CodigoTipoAhorro			: {type: "string"}
						 , IdEntidadRecaudadora		: {type: "number"}
						 , NombreEntidad			: {type: "string"}
						 , DocumentoEntidad			: {type: "string"}
						 , Fondo					: {type: "string"}
						 , MontoTraspaso			: {type: "number"}
						 , UFTraspaso				: {type: "number"}
						 , PorcentajeTraspaso		: {type: "number"}
						 , IdEstado					: {type: "string"}
					}
				}
			}
		}	
	};
	
	var defOrigenAhorro = {
		type: "odata",
		definition : {
			serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            pageSize        : 100,
            sort 			: { field: "IdDetalleOrigenTraspaso", dir: "DESC" },
            pageSize        : 10,
            transport		: {
				read: {
					url 		: bcsConsultasAPV('ConsultarOrigenTraspaso'),
					dataType	: "json"
			    }
			},
			schema: {
				model: {
					fields: {
						   IdTraspaso				: { type: "number"}
						 , IdContratoSAAPV			: { type: "number"}
						 , TipoMovimiento			: { type: "string"}
						 , RazonRetiro				: { type: "string"}
						 , FechaIngreso				: { type: "sysdate"}
						 , CodigoTipoTraspaso		: { type: "string"}
						 , IdCliente				: { type: "number"}
						 , IdPersona				: { type: "number"}
						 , Descripcion				: { type: "string"}
						 , IdSocioNegocioCli		: { type: "number"}
						 , IdSocioNegocioEMP		: { type: "number"}
						 , IdDetalleOrigenTraspaso	: { type: "number"}
						 , CodigoTipoAhorro			: { type: "string"}
						 , IdEntidadRecaudadora		: { type: "number"}
						 , NombreEntidad			: { type: "string"}
						 , DocumentoEntidad			: { type: "string"}
						 , Fondo					: { type: "string"}
						 , MontoTraspaso			: { type: "number"}
						 , UFTraspaso				: { type: "number"}
						 , PorcentajeTraspaso		: { type: "number"}
						 , IdEstado					: { type: "string"}
					}
				}
			}
		}	
	};	

	//if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
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
							IdSocioNegocioCli		: { type: "number"},
							NombreCompleto			: { type: "string"},
							IdSocioNegocioEMP		: { type: "number"},
							RazonSocial				: { type: "string"},
							DocumentoEntidadOrigen	: { type: "string"},	
							NombreEntidadOrigen		: { type: "string"},
							FechaIngreso			: { type: "sysdate"},
							IdProductoAgrupador		: { type: "number"},	
							Producto				: { type: "string"},
							IdPlanTipoInversion		: { type: "number"},
							NombrePlan				: { type: "string"},
							IdRegimenTributario		: { type: "number"},
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
							EsReapertura			: { type: "string"},
							MontoReapertura			: { type: "number"}
						}
					}
				},
			}	
		};

	var defContratoSAAPV = {
		type : "odata",
		definition : 
		{
            serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
            pageSize        : 10,
            transport		: {
							read: {
								url 		: bcsConsultasAPV('ConsultarGestionContratosSAAPV'),
								dataType	: "json"
						    }
			},
            schema 			: { 
            				   model: 
            						{
										fields : 
										{													
											IdContratoSAAPV				: {type: "number"},
										    FolioContratoSAAPV 			: {type: "string"},
										    IdPlan 						: {type: "number"},
											NombrePlan					: {type: "string"},
											RegimenTributario 			: {type: "string"},
											IdPortafolio				: {type: "number"},
											/*Cliente*/
											IdPersona 					: {type: "number"},
											DocumentoIdentidadCliente	: {type: "string"},
											NombrePersona				: {type: "string"},
											IdCliente					: {type: "number"},												
											/*Empleador*/
											IdEmpleador 				: {type: "number"},
											DocumentoIdentidadEmpleador : {type: "string"},
											NombreEmpleador 			: {type: "string"},												
											NombreEntidadOrigen 		: {type: "string"},
											RutEntidadOrigen			: {type: "string"},
											TipoDeAhorro 				: {type: "string"},
											FondoDeOrigen 				: {type: "string"},
											TipoTraspaso				: {type: "string"},
											MontoTraspaso 				: {type: "number"},
											UFTraspaso					: {type: "number"},
											PorcentajeTraspaso			: {type: "number"},
											IdEstado					: {type: "string"},
											IdMatrizAgrupadorAPV		: {type: "number"},
											Proposito					: {type: "string"},
											IdProductoAgrupador			: {type: "number"},
											CodigoAgrupador				: {type: "string"}
										}
									}
							  }
		 }
	};
	
	var dsConsultaContratoPortafolio = {
		type : "odata",
		definition : 
		{
            serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            sort 			: { field: "IdPortafolio", dir: "DESC" },
            pageSize        : 10,
            transport		: {
							read: {
								url 		: bcsConsultasAPV('ConsultarContratoPortafolio'),
								dataType	: "json"
						    }
			},
            schema 			: { 
            				   model: 
            						{
										fields : 
										{													
											"IdPortafolio"				: {type: "number"},
										    "IdPlanTipoInversion" 		: {type: "number"},
										    "IdProductoAgrupador" 		: {type: "number"},
											"NombrePlanTipoInversion" 	: {type: "string"},
											"Codigo" 					: {type: "string"}
										    
										}
									}
							  }
		 }
	};
	//*****************************************************************************************************************/
	//****************************************** Definicion de Grillas ************************************************/
	//*****************************************************************************************************************/
	var dsOrigenAhorro = new bcs.DataSource(defOrigenAhorro);
	ingtraAPV.datasource.OrigenAhorro = {
		dataSource: dsOrigenAhorro,
		columns: [	
			//{field: "NombrePlan"			,title: $translate.instant('UI_NOMBRE_PLAN'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "CodigoTipoAhorro"		,title: $translate.instant('UI_TIPO_AHORRO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "FechaIngreso"	 		,title: $translate.instant('UI_FECHA_INGRESO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "Fondo"					,title: $translate.instant('UI_FONDO_ORIGEN'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "MontoTraspaso"			,title: $translate.instant('UI_MONTO_PESOS_ORIGEN'), width: "80px", align: "rigth" , sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
		    {field: "UFTraspaso"			,title: $translate.instant('UI_MONTO_UF'), width: "80px", align: "rigth" , sortable : true, editable: false    , template : " <label> #:  (util.bcs_formato(UFTraspaso,'ORD','Monto','CLP','','')).valor #</label>"},
		    {field: "PorcentajeTraspaso"	,title: $translate.instant('UI_PORCENTAJE'), width: "80px", align: "rigth" , sortable : true, editable: false  , template : " <label> #:  (util.bcs_formato(PorcentajeTraspaso,'ORD','Monto','CLP','','')).valor #</label>"}
		    ],	
		sortable		: false,
		pageable		: true,
		autoBind		: false,
		height			: 125,
	    selectable 		: "row",
	    userSettings	: false,
		navigatable		: true,
		gridCreated		: function(grid){
			ingtraAPV.grillaAll = grid;
			if(ingtraAPV.parametrosVentana.ModoVentana === 'M' || ingtraAPV.parametrosVentana.ModoVentana === 'C' || ingtraAPV.parametrosVentana.ModoVentana === 'R'){
				ingtraAPV.consulta.TraspasosSAAPV(ingtraAPV.parametrosVentana.IdContratoSAAPV,ingtraAPV.parametrosVentana.IdTraspaso);	
			}
		}
	};

	//
	var dsDetalleTraspaso = new bcs.DataSource(defDetalleTraspaso);
	if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
		console.log("ingtraAPV.datasource.DetalleTraspaso",ingtraAPV.datasource.DetalleTraspaso);
		ingtraAPV.datasource.DetalleTraspaso = {
			dataSource: dsDetalleTraspaso,
			columns: [
				{field: "NombrePlan"			,title: $translate.instant('UI_NOMBRE_PLAN_AHORRO')	, width: "120px", align: "left", sortable : true, editable: false },
				{field: "Producto"				,title: $translate.instant('UI_PRODUCTO')			, width: "120px", align: "left" , sortable : true, editable: false },
				{field: "Codigo"				,title: $translate.instant('UI_REGIMEN')			, width: "50px", align: "rigth", sortable : true, editable: false },
				{field: "FechaIngresoDetalle"	,title:	$translate.instant('UI_FECHA_INGRESO_REAPERTURA'), width: "100px", align: "left" , sortable : true, editable: false},
				{field: "MontoPeso"				,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoPeso,'ORD','Monto','CLP','','')).valor #</label>" },
				{field: "MontoUF" 				,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoUF,'ORD','Monto','CLP','','')).valor #</label>"},
				{field: "Porcentaje"			,title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth", sortable : true, editable: false , hidden : true, template : " <label> #:  (util.bcs_formato(Porcentaje,'ORD','Monto','CLP','','')).valor #</label>"},
				{field: "IdEstadoDetalle"		,title: $translate.instant('UI_ESTADO')				, width: "50px", align: "rigth", sortable : true, editable: false , hidden : true},
				{field: "MontoReapertura"		,title: $translate.instant('UI_MONTO_REAPERTURA')	, width: "80px", align: "rigth", sortable : true, editable: true , template : " <label> #:  (util.bcs_formato(MontoReapertura,'ORD','Monto','CLP','','')).valor #</label>" }
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: false,
			height			: 160,
			selectable 		: "row",
			userSettings	: false,
			navigatable		: true,
			gridCreated		: function(gridDetalle){
				ingtraAPV.HabilitoGuardarEdicion = false;
				ingtraAPV.HabilitoEliminarDis = false;
				ingtraAPV.HabilitoGuardar = true;
				ingtraAPV.HabilitoAgregar = false;
				ingtraAPV.HabilitoEditar = false;
				ingtraAPV.HabilitoRecaudar = true;
				ingtraAPV.gridApi = gridDetalle;
				
				//ingtraAPV.gridApi.showColumn(8); //columna MontoReapertura	
				ingtraAPV.consulta.DetalleTraspaso(ingtraAPV.parametrosVentana.IdTraspaso);
					
			}
		};
	}else{
		ingtraAPV.datasource.DetalleTraspaso = {
			dataSource: dsDetalleTraspaso,
			columns: [
				{field: "NombrePlan"			,title: $translate.instant('UI_NOMBRE_PLAN_AHORRO')	, width: "100px", align: "left", sortable : true, editable: false },
				{field: "Producto"				,title: $translate.instant('UI_PRODUCTO')			, width: "150px", align: "left" , sortable : true, editable: false },
				{field: "Codigo"				,title: $translate.instant('UI_REGIMEN')			, width: "50px", align: "rigth", sortable : true, editable: false },
				{field: "FechaIngresoDetalle"	,title:	$translate.instant('UI_FECHA_INGRESO_REAPERTURA')	, width: "100px", align: "left" , sortable : true, editable: false},
				{field: "MontoPeso"				,title: $translate.instant('UI_MONTO_PESOS')		, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoPeso,'ORD','Monto','CLP','','')).valor #</label>" },
				{field: "MontoUF" 				,title: $translate.instant('UI_MONTO_UF')			, width: "80px", align: "rigth", sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(MontoUF,'ORD','Monto','CLP','','')).valor #</label>"},
				{field: "Porcentaje"			,title: $translate.instant('UI_PORCENTAJE')			, width: "80px", align: "rigth", sortable : true, editable: false  ,hidden: true, template : " <label>   #:  (util.bcs_formato(Porcentaje,'ORD','Monto','CLP','','')).valor #</label>"}
				//{field: "IdEstadoDetalle"		,title: $translate.instant('UI_ESTADO')				, width: "50px", align: "rigth", sortable : true, editable: false }
				],	
			sortable		: false,
			pageable		: true,
			autoBind		: false,
			height			: 150,
			selectable 		: "row",
			userSettings	: false,
			navigatable		: true,
			gridCreated		: function(gridDetalle){
				if(ingtraAPV.parametrosVentana.ModoVentana === 'C'){
					ingtraAPV.HabilitoGuardarEdicion = false;
					ingtraAPV.HabilitoEliminarDis = false;
					ingtraAPV.HabilitoGuardar = false;
					ingtraAPV.HabilitoAgregar = false;
					ingtraAPV.HabilitoEditar = false;
				}else{
					if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
						ingtraAPV.HabilitoGuardarEdicion = false;
						ingtraAPV.HabilitoEliminarDis = false;
						ingtraAPV.HabilitoGuardar = true;
						ingtraAPV.HabilitoAgregar = true;
						ingtraAPV.HabilitoEditar = false;
						ingtraAPV.fn.LimpiarIngresoTraspaso();
					}else{
						
						ingtraAPV.HabilitoGuardarEdicion = false;
						ingtraAPV.HabilitoEliminarDis = false;
						ingtraAPV.HabilitoGuardar = true;
						ingtraAPV.HabilitoAgregar = false;
						ingtraAPV.HabilitoEditar = false;
						ingtraAPV.fn.LimpiarIngresoTraspaso();
					}				
				}
				if(ingtraAPV.parametrosVentana.DobleClick === true){
					ingtraAPV.HabilitoGuardarEdicion = false;
					ingtraAPV.HabilitoEliminarDis = false;
					ingtraAPV.HabilitoGuardar = false;
					ingtraAPV.HabilitoAgregar = false;
					ingtraAPV.HabilitoEditar = false;
				}
			
				ingtraAPV.gridApi = gridDetalle;		
				ingtraAPV.consulta.DetalleTraspaso(ingtraAPV.parametrosVentana.IdTraspaso);
				//ingtraAPV.gridApi.hideColumn(8);
			}
		};
	};
	//*****************************************************************************************************************/
	//*********************************************** Consultas *******************************************************/
	//*****************************************************************************************************************/
	ingtraAPV.consulta.TraspasosSAAPV = function(IdContratoSAAPV,IdTraspaso){
		var filtro = Array();
		filtro.push({ field: "IdContratoSAAPV", operator: "eq", value: IdContratoSAAPV });
		ingtraAPV.IdContratoSAAPV = IdContratoSAAPV;
		ingtraAPV.IdTraspaso = IdTraspaso;
		
		var consultaTraspaso = new bcs.DataSource(defTraspaso);
		consultaTraspaso.query({filter: filtro}).then(function(){
			if(consultaTraspaso.view().length > 0){
				ingtraAPV.datosForm.SocioNegocioIdActual = consultaTraspaso.view()[0].IdSocioNegocioCli;
				ingtraAPV.datosForm.SocioNegocioIdActualDefecto = consultaTraspaso.view()[0].IdSocioNegocioCli;
				ingtraAPV.datosForm.SocioNegocioOrigenIdActual = consultaTraspaso.view()[0].IdSocioNegocioEMP;
				ingtraAPV.datosForm.SocioNegocioOrigenIdActualDefecto = consultaTraspaso.view()[0].IdSocioNegocioEMP;
				ingtraAPV.datosForm.fechaTraspaso = consultaTraspaso.view()[0].FechaIngreso;
				ingtraAPV.datosForm.TipoTraspasoIdActual = consultaTraspaso.view()[0].CodigoTipoTraspaso === 'PARCIAL' ? 'PAR' : 'TOT';
				ingtraAPV.datosForm.kngMontoTotalPesos = consultaTraspaso.view()[0].MontoTotalTraspaso;
				ingtraAPV.datosForm.kngMontoTotalUF = consultaTraspaso.view()[0].UFTotalTraspaso;
				ingtraAPV.datosForm.TotalPorcentaje = consultaTraspaso.view()[0].PorcentajeTotalTraspaso;
				ingtraAPV.datosForm.IdCategoriaInversionistaIdActual = consultaTraspaso.view()[0].IdCategoriaInversionista;
				ingtraAPV.datosForm.IdCategoriaInversionistaIdActualDefecto = consultaTraspaso.view()[0].IdCategoriaInversionista;			
				ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual = consultaTraspaso.view()[0].IdPerfilComercialBaseOrigen;
				ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActualDefecto = consultaTraspaso.view()[0].IdPerfilComercialBaseOrigen;
				ingtraAPV.dataForm.PlanAhorroIdActual = consultaTraspaso.view()[0].IdPlan;
				ingtraAPV.dataForm.PlanAhorroIdActualDefecto = consultaTraspaso.view()[0].IdPlan;	
				
				
				if(ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === false 
						|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === "" 
							|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === undefined
								|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === null){
					ingtraAPV.PerfilDeshabilitado = false;
					ingtraAPV.PerfilRequerido = true;
				}else{
					ingtraAPV.PerfilDeshabilitado = true;
					ingtraAPV.PerfilRequerido = false;
				};
		
				var ConsultaOrigenTraspaso = new bcs.DataSource(defOrigenAhorro);
				ConsultaOrigenTraspaso.query({filter: filtro}).then(function(){
					if(ConsultaOrigenTraspaso.data().length > 0 ){
						$.each(ConsultaOrigenTraspaso.data(),function(kew,value){
							dsOrigenAhorro.add({
								CodigoTipoAhorro	: value.CodigoTipoAhorro,
								Fondo				: value.Fondo,
								FechaIngreso		: value.FechaIngreso,
								MontoTraspaso		: value.MontoTraspaso,
							    UFTraspaso			: value.UFTraspaso,
							    PorcentajeTraspaso	: value.PorcentajeTraspaso
							});	
						});
					}
				});
			}
		});
	};
	
	ingtraAPV.consulta.DetalleTraspaso = function(IdTraspaso){
		var filtro = Array();
		filtro.push({ field: "IdTraspaso", operator: "eq", value: IdTraspaso });
		//filtro.push({ field: "EsReapertura", operator: "eq", value: "N" });
		var ConsultaDetalleTraspaso = new bcs.DataSource(defDetalleTraspaso);
		if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
			ConsultaDetalleTraspaso.query({filter: filtro}).then(function(){
				if(ConsultaDetalleTraspaso.data().length > 0 ){
					$.each(ConsultaDetalleTraspaso.data(),function(kew,value){
						dsDetalleTraspaso.add({
							IdDetalleTraspaso		: value.IdDetalleTraspaso,
							IdRegimenTributario		: value.IdRegimenTributario,
							Codigo					: value.Codigo,
							FechaIngresoDetalle		: value.FechaIngresoDetalle,
							MontoPeso				: value.MontoPeso === null ? 0 : value.MontoPeso,
							MontoUF 				: value.MontoUF === null ? 0 : value.MontoUF,
							IdPortafolio			: value.IdPortafolio,
							Porcentaje				: value.Porcentaje === null ? 0 : value.Porcentaje,
							IdMatrizAgrupadorAPV	: value.IdMatrizAgrupadorAPV,
							Producto				: value.Producto,
							CodigoAgrupador		  	: value.CodigoAgrupador,
							IdPropositoPortafolio 	: value.IdPropositoPortafolio,
							NombrePlan				: value.NombrePlan,
							IdPlanTipoInversion		: value.IdPlanTipoInversion,
							IdProductoAgrupador		: value.IdProductoAgrupador,
							MontoReapertura			: value.MontoReapertura,
							EsReapertura			: value.EsReapertura
							//EsReapertura			: value.EsReapertura === 'S' ? 'Si' : ''
						});	
					});
					
					if(dsDetalleTraspaso.view()[0].MontoPeso > 0)
					{
						ingtraAPV.MontoUFOrigenRequerido 		= false;
						ingtraAPV.MontoUFOrigenDeshabilitado	= false;
					}
					if(dsDetalleTraspaso.view()[0].MontoUF > 0)
					{
						ingtraAPV.MontoPesoOrigenRequerido		= false;
						ingtraAPV.MontoPesoOrigenDeshabilitado	= false;
					}
				}
			});
		}
		else{
			ConsultaDetalleTraspaso.query({filter: filtro}).then(function(){
				if(ConsultaDetalleTraspaso.data().length > 0 ){
					$.each(ConsultaDetalleTraspaso.data(),function(kew,value){
						dsDetalleTraspaso.add({
							IdDetalleTraspaso		: value.IdDetalleTraspaso,
							IdRegimenTributario		: value.IdRegimenTributario,
							Codigo					: value.Codigo,
							FechaIngresoDetalle		: value.FechaIngresoDetalle,
							MontoPeso				: value.MontoPeso === null ? 0 : value.MontoPeso,
							MontoUF 				: value.MontoUF === null ? 0 : value.MontoUF,
							IdPortafolio			: value.IdPortafolio,
							Porcentaje				: value.Porcentaje === null ? 0 : value.Porcentaje,
							IdMatrizAgrupadorAPV	: value.IdMatrizAgrupadorAPV,
							Producto				: value.Producto,
							CodigoAgrupador		  	: value.CodigoAgrupador,
							IdPropositoPortafolio 	: value.IdPropositoPortafolio,
							NombrePlan				: value.NombrePlan,
							IdPlanTipoInversion		: value.IdPlanTipoInversion,
							IdProductoAgrupador		: value.IdProductoAgrupador,
							EsReapertura			: value.EsReapertura
						});	
					});
					
					if(dsDetalleTraspaso.view()[0].MontoPeso > 0)
					{
						ingtraAPV.MontoUFOrigenRequerido 		= false;
						ingtraAPV.MontoUFOrigenDeshabilitado	= false;
					}
					if(dsDetalleTraspaso.view()[0].MontoUF > 0)
					{
						ingtraAPV.MontoPesoOrigenRequerido		= false;
						ingtraAPV.MontoPesoOrigenDeshabilitado	= false;
					}
				}
			});
		};
	};
	//*****************************************************************************************************************/
	//*********************************************** Funciones *******************************************************/
	//*****************************************************************************************************************/
	ingtraAPV.fn.AgregoDetalleTraspaso = function(){
		if(ingtraAPV.dataForm.NombrePlanAhorroSeleccion.NombrePlanTipoInversion === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Plan de Inversión.");
			return;
		}
		if(ingtraAPV.dataForm.ProductoAgrupadorSeleccion.NombreProductoAgrupador === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Producto.");
			return;
		}
		if(ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdMatrizAgrupadorAPV === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Régimen.");
			return;
		}
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso === undefined && ingtraAPV.dataForm.kngMontoUFTraspaso === undefined){
			bcsComunes.msg("warning","warning","Debe ingresar Monto peso o Monto UF.");
			return;
		}
		
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso !== 0 && ingtraAPV.dataForm.kngMontoUFTraspaso !== 0){
			ingtraAPV.dataForm.kngMontoPesoTraspaso = 0;
			ingtraAPV.dataForm.kngMontoUFTraspaso = 0;
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_INGRESAR_UN_MONTO_PESO_O_MONTO_UF"));
			return;
		}
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso !== undefined && ingtraAPV.dataForm.kngMontoPesoTraspaso !== 0)
		{
			ingtraAPV.MontoUFOrigenRequerido 		= false;
			ingtraAPV.MontoUFOrigenDeshabilitado	= false;
		}
		if(ingtraAPV.dataForm.kngMontoUFTraspaso !== undefined && ingtraAPV.dataForm.kngMontoUFTraspaso !== 0)
		{
			ingtraAPV.MontoPesoOrigenRequerido		= false;
			ingtraAPV.MontoPesoOrigenDeshabilitado	= false;
		}
		if(dsDetalleTraspaso.data().length > 0)
		{
			console.log("data Ingreso: ", dsDetalleTraspaso.data);
			for(var x=0; x<dsDetalleTraspaso.data().length; x++)
			{
				if(dsDetalleTraspaso.view()[x].Codigo === ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_NO_PUEDES_AGREGAR_UN_REGIMEN_YA_AGREGADO"));
					return;
				}
			}
		}
		
		var filtro = Array();
		filtro.push({ field: "IdPlanTipoInversion", operator: "eq", value: Number(ingtraAPV.dataForm.NombrePlanAhorroSeleccion.IdPlanTipoInversion)});
		filtro.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(ingtraAPV.dataForm.ProductoAgrupadorSeleccion.IdProductoAgrupador)});
		filtro.push({ field: "Codigo"			  , operator: "eq", value: ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo});
		filtro.push({ field: "IdPersona"		  , operator: "eq", value: Number(ingtraAPV.datosForm.SocioNegocioSeleccionado.IdPersona)});
		ingtraAPV.IdPortafolio = 0;
		var ConsultaContratoPortafolio = new bcs.DataSource(dsConsultaContratoPortafolio);
		ConsultaContratoPortafolio.query({ filter : filtro}).then(function()
		{
			
			
			if(ConsultaContratoPortafolio.view().length > 0)
			{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO","Ya existe un Contrato de Traspaso asignado a un portafolio, ¿Desea asignarlo?" ,idVentana);
				t .then(function(result)
				{
					if (result === OPCIONES_BCSALERTA.NO)
					{
						bcsComunes.ventanaOverlay(idVentana,false);
						return;
					}
					ingtraAPV.IdPortafolio = ConsultaContratoPortafolio.view()[0].IdPortafolio;
					fnAgregoDetalleTraspaso();
				});
			}
			else
			{
				ingtraAPV.IdPortafolio = 0;
				fnAgregoDetalleTraspaso();
			}	
			//$scope.csaapv.habilitoPlanAhorro 	= true;
		});		
	};	
	
	fnAgregoDetalleTraspaso = function(){
	
		dsDetalleTraspaso.add({
			IdPortafolio		  : ingtraAPV.IdPortafolio,
			IdProductoAgrupador   : ingtraAPV.dataForm.ProductoAgrupadorSeleccion.IdProductoAgrupador,
			Producto			  : ingtraAPV.dataForm.ProductoAgrupadorSeleccion.NombreProductoAgrupador,
			IdPlanTipoInversion	  : ingtraAPV.dataForm.NombrePlanAhorroSeleccion.IdPlanTipoInversion,
			NombrePlan	  		  : ingtraAPV.dataForm.NombrePlanAhorroSeleccion.NombrePlanTipoInversion,
			MontoPeso			  : ingtraAPV.dataForm.MontoPesoTraspaso === null || ingtraAPV.dataForm.MontoPesoTraspaso === undefined || ingtraAPV.parametrosVentana.ModoVentana === 'R' ? 0 : ingtraAPV.dataForm.kngMontoPesoTraspaso,
			MontoUF				  : ingtraAPV.dataForm.MontoUFTraspaso === null || ingtraAPV.dataForm.MontoUFTraspaso === undefined || ingtraAPV.parametrosVentana.ModoVentana === 'R' ? 0 : ingtraAPV.dataForm.kngMontoUFTraspaso, 
			Porcentaje			  : 0,
			FechaIngresoDetalle	  : util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
			IdRegimenTributario   : ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdRegimenTributario,
			Codigo				  : ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo,
			IdMatrizAgrupadorAPV  : ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdMatrizAgrupadorAPV,
			CodigoAgrupador		  : ingtraAPV.dataForm.RegimenAgrupadorSeleccion.CodigoAgrupador,
			IdPropositoPortafolio : ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdPropositoPortafolio,
			MontoReapertura		  : ingtraAPV.parametrosVentana.ModoVentana === 'R' ? ingtraAPV.dataForm.kngMontoPesoTraspaso : 0
		});	

		ingtraAPV.fn.LimpiarIngresoTraspaso();
		ingtraAPV.HabilitoAgregar = false;
	  	ingtraAPV.HabilitoEditar = false;
	  	ingtraAPV.HabilitoGuardarEdicion = false;
	  	ingtraAPV.HabilitoEliminarDis = false;
	};
	
	fnModificoDetalleTraspaso = function(DetalleTraspasoSeleccion){
		
		for(var i=0; i<dsDetalleTraspaso.data().length; i++){

			if(dsDetalleTraspaso.view()[i].IdDetalleTraspaso === DetalleTraspasoSeleccion.IdDetalleTraspaso){
				
				dsDetalleTraspaso.view()[i].IdPortafolio = DetalleTraspasoSeleccion.IdPortafolio;
				dsDetalleTraspaso.view()[i].IdProductoAgrupador = ingtraAPV.dataForm.ProductoAgrupadorSeleccion.IdProductoAgrupador;
				dsDetalleTraspaso.view()[i].Producto = ingtraAPV.dataForm.ProductoAgrupadorSeleccion.NombreProductoAgrupador;
				dsDetalleTraspaso.view()[i].IdPlanTipoInversion = ingtraAPV.dataForm.NombrePlanAhorroSeleccion.IdPlanTipoInversion;
				dsDetalleTraspaso.view()[i].NombrePlan = ingtraAPV.dataForm.NombrePlanAhorroSeleccion.NombrePlanTipoInversion;
				dsDetalleTraspaso.view()[i].MontoPeso = ingtraAPV.dataForm.kngMontoPesoTraspaso;
			    dsDetalleTraspaso.view()[i].MontoUF = ingtraAPV.dataForm.kngMontoUFTraspaso;
			    dsDetalleTraspaso.view()[i].Porcentaje = 0;
			    dsDetalleTraspaso.view()[i].FechaIngresoDetalle = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			    dsDetalleTraspaso.view()[i].IdRegimenTributario = ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdRegimenTributario;
			    dsDetalleTraspaso.view()[i].Codigo = ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo;
			    dsDetalleTraspaso.view()[i].IdMatrizAgrupadorAPV = ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdMatrizAgrupadorAPV;
			    dsDetalleTraspaso.view()[i].CodigoAgrupador = ingtraAPV.dataForm.RegimenAgrupadorSeleccion.CodigoAgrupador;
			    dsDetalleTraspaso.view()[i].IdPropositoPortafolio = DetalleTraspasoSeleccion.IdPropositoPortafolio;			
				break;
			}
		}
		
		ingtraAPV.gridApi.refresh();
		
		ingtraAPV.fn.LimpiarIngresoTraspaso();
		ingtraAPV.HabilitoAgregar = true;
	  	ingtraAPV.HabilitoEditar = false;
	  	ingtraAPV.HabilitoGuardarEdicion = false;
	  	ingtraAPV.HabilitoEliminarDis = false;
	  	
	};

	ingtraAPV.fn.LimpiarIngresoTraspaso = function(){
		//ingtraAPV.dataForm.NombrePlanAhorroSeleccion = undefined;
		//ingtraAPV.dataForm.PlanAhorroIdActual = undefined;
		ingtraAPV.dataForm.ProductoAgrupadorSeleccion = undefined;
		ingtraAPV.dataForm.ProductoAgrupadorIdActual = undefined;
		ingtraAPV.dataForm.ProductoAgrupadorIdDefecto = undefined;
		ingtraAPV.dataForm.RegimenAgrupadorSeleccion= undefined;
		ingtraAPV.dataForm.RegimenAgrupadorIdActual = undefined;
		ingtraAPV.dataForm.RegimenAgrupadorIdDefecto = undefined;
		
		ingtraAPV.dataForm.kngMontoPesoTraspaso = 0;
		ingtraAPV.dataForm.kngMontoUFTraspaso = 0;
	};	
	
	ingtraAPV.fn.EliminarDetalleTraspaso = function(DetalleTraspasoDS){		
		if(DetalleTraspasoDS !== undefined)
		{
			dsDetalleTraspaso.remove(DetalleTraspasoDS);
		}
		if(dsDetalleTraspaso.data().length === 0)
		{
			ingtraAPV.MontoUFOrigenRequerido 		= true;
			ingtraAPV.MontoUFOrigenDeshabilitado	= true;
			ingtraAPV.MontoPesoOrigenRequerido		= true;
			ingtraAPV.MontoPesoOrigenDeshabilitado	= true;
		}

		ingtraAPV.fn.LimpiarIngresoTraspaso();
		ingtraAPV.HabilitoEliminarDis = false;
		ingtraAPV.HabilitoEditar = false;
		ingtraAPV.HabilitoGuardarEdicion = false;
		
	};

	
	ingtraAPV.fn.EditoDetalleTraspaso = function(DetalleTraspasoSeleccion){
		
		if(ingtraAPV.dataForm.NombrePlanAhorroSeleccion.NombrePlanTipoInversion === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Plan de Inversión.");
			return;
		}
		if(ingtraAPV.dataForm.ProductoAgrupadorSeleccion.NombreProductoAgrupador === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Producto.");
			return;
		}
		if(ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdMatrizAgrupadorAPV === undefined){
			bcsComunes.msg("warning","warning","Debe seleccionar un Régimen.");
			return;
		}
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso === undefined && ingtraAPV.dataForm.kngMontoUFTraspaso === undefined){
			bcsComunes.msg("warning","warning","Debe ingresar Monto peso o Monto UF.");
			return;
		}
		
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso !== 0 && ingtraAPV.dataForm.kngMontoUFTraspaso !== 0){
			ingtraAPV.dataForm.kngMontoPesoTraspaso = 0;
			ingtraAPV.dataForm.kngMontoUFTraspaso = 0;
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_INGRESAR_UN_MONTO_PESO_O_MONTO_UF"));
			return;
		}
		if(ingtraAPV.dataForm.kngMontoPesoTraspaso !== undefined && ingtraAPV.dataForm.kngMontoPesoTraspaso !== 0)
		{
			ingtraAPV.MontoUFOrigenRequerido 		= false;
			ingtraAPV.MontoUFOrigenDeshabilitado	= false;
		}
		if(ingtraAPV.dataForm.kngMontoUFTraspaso !== undefined && ingtraAPV.dataForm.kngMontoUFTraspaso !== 0)
		{
			ingtraAPV.MontoPesoOrigenRequerido		= false;
			ingtraAPV.MontoPesoOrigenDeshabilitado	= false;
		}
		if(dsDetalleTraspaso.data().length > 0)
		{
			for(var x=0; x<dsDetalleTraspaso.data().length; x++)
			{
				if(dsDetalleTraspaso.view()[x].Codigo === ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo && dsDetalleTraspaso.view()[x].uid != DetalleTraspasoSeleccion.uid)
				{
					bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_NO_PUEDES_AGREGAR_UN_REGIMEN_YA_AGREGADO"));
					return;
				}
			}
		}
		
		var filtro = Array();
		filtro.push({ field: "IdPlanTipoInversion", operator: "eq", value: Number(ingtraAPV.dataForm.NombrePlanAhorroSeleccion.IdPlanTipoInversion)});
		filtro.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(ingtraAPV.dataForm.ProductoAgrupadorSeleccion.IdProductoAgrupador)});
		filtro.push({ field: "Codigo"			  , operator: "eq", value: ingtraAPV.dataForm.RegimenAgrupadorSeleccion.Codigo});
		filtro.push({ field: "IdPersona"		  , operator: "eq", value: Number(ingtraAPV.datosForm.SocioNegocioSeleccionado.IdPersona)});
		ingtraAPV.IdPortafolio = 0;
		var ConsultaContratoPortafolio = new bcs.DataSource(dsConsultaContratoPortafolio);
		ConsultaContratoPortafolio.query({ filter : filtro}).then(function()
		{		
				ingtraAPV.IdPortafolio = 0;
				fnModificoDetalleTraspaso(DetalleTraspasoSeleccion);			
		});		
	};	

	ingtraAPV.fn.EditarDetalleTraspaso = function(DetalleTraspasoDS) {
	  // Cargar valores combobox
		ingtraAPV.dataForm.kngMontoPesoTraspaso = DetalleTraspasoDS.MontoPeso;
		ingtraAPV.dataForm.kngMontoUFTraspaso = DetalleTraspasoDS.MontoUF;

		ingtraAPV.dataForm.RegimenAgrupadorIdActual = DetalleTraspasoDS.Codigo;
		ingtraAPV.dataForm.RegimenAgrupadorIdDefecto = DetalleTraspasoDS.Codigo

		ingtraAPV.dataForm.ProductoAgrupadorIdActual = DetalleTraspasoDS.IdProductoAgrupador;
		ingtraAPV.dataForm.ProductoAgrupadorIdDefecto = DetalleTraspasoDS.IdProductoAgrupador;
	
	  // Habilitar botones e inhabilitar botones al momento de editar
	  ingtraAPV.HabilitoAgregar = false;
	  ingtraAPV.HabilitoEditar = false;
	  ingtraAPV.HabilitoEliminarDis = true;
	  ingtraAPV.HabilitoGuardarEdicion = true;
	};
	
	
	
	ingtraAPV.fn.AgregoFormaPago = function(){
		var filterVentana = Array();
		filterVentana.IdContratoSAAPV = ingtraAPV.parametrosVentana.IdContratoSAAPV;
		
		var IdVentanamodal = bcsComunes.ventanaAbrir("FormaDePagoAPV", filterVentana, true, true, IdVentanamodal);
		var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
		promesa.then(function(data){
			//
        });	
	};
	
	ingtraAPV.fn.IngresarTraspaso = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		
		if(ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === false 
				|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === "" 
					|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === undefined
						|| ingtraAPV.datosForm.IdPerfilComercialBaseOrigenIdActual === null){
			bcsComunes.msg("warning","warning",$translate.instant("UI_FALTA_PERFIL_COMERCIAL"));
			bcsComunes.ventanaOverlay(idVentana,false);
			return;
		}
		
		if(dsDetalleTraspaso.data().length > 0){
			var tablaTraspaso = Array();
			$.each(dsDetalleTraspaso.view(),function(key,value){

				if(value.IdDetalleTraspaso === null){
					value.IdDetalleTraspaso = 0;
				}

				var objTraspaso = {
					IdTraspaso 				: ingtraAPV.IdTraspaso,
					IdContratoSAAPV 		: ingtraAPV.IdContratoSAAPV,
					FechaIngreso 			: $scope.SesionFechaSistema.slice(0,10),
					FechaModificacion 		: '0001-01-01',
					IdCliente 				: ingtraAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
					IdPersonaCliente		: ingtraAPV.datosForm.SocioNegocioSeleccionado.IdPersona,
					DocumentoIdentidad		: ingtraAPV.datosForm.SocioNegocioSeleccionado.DocumentoIdentidadDefecto,
					IdPerfilComercial		: ingtraAPV.datosForm.PerfilComercialSeleccionado.IdPerfilComercialBase,
					/*Detalle*/
					IdDetalleTraspaso 		: value.IdDetalleTraspaso, 	//0 por defecto
					IdPortafolio 			: value.IdPortafolio,		//0 por defecto
					IdPropositoPortafolio 	: value.IdPropositoPortafolio,
					IdPlanTipoInversion 	: value.IdPlanTipoInversion,
					NombrePlan 				: value.NombrePlan,	
					IdProductoAgrupador 	: value.IdProductoAgrupador,
					ProductoAgrupador 		: value.Producto,
					IdRegimenTributario 	: value.IdRegimenTributario,
					RegimenTributario 		: value.Codigo,
					CodigoAgrupador 		: value.CodigoAgrupador,
					MontoPeso 				: value.MontoPeso,
					MontoUF 				: value.MontoUF,
					Porcentaje 				: value.Porcentaje,
					MontoReapertura			: value.MontoReapertura
				};

				if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
					if(value.MontoReapertura > 0)tablaTraspaso.push(objTraspaso);	
				}else tablaTraspaso.push(objTraspaso);
	
			});
			
			var datos = {};
			datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : "APV",
				p_Opcion 	: "I",
				p_Fecha 	: $scope.SesionFechaSistema.slice(0,10),
				p_Traspasos	: tablaTraspaso 
			};
			
			if(ingtraAPV.parametrosVentana.TipoVentana === 'MOD'){
				if(ingtraAPV.parametrosVentana.ModoVentana === 'R'){
					var datosTraspaso = {};
					ingtraAPV.fn.IngresoRecaudacion(datos);
				}else{
					bcsServiciosAPV.ModificarPortafolioTraspaso(datos).$promise.then(function(respuesta){
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							var mensajeTotal = new Array();
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
							bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
	
						}else{
							respuesta.P_OUT_MENSAJES = respuesta.P_OUT_MENSAJES.filter(m => "ERROR" == m.tipo);
							bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						}
						bcsComunes.ventanaCerrar(idVentana);
						},function(respuesta){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
						});
				};
			}else{
				bcsServiciosAPV.GrabarTraspasos(datos).$promise.then(function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					var mensajeTotal = new Array();
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
					bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				}
				bcsComunes.ventanaCerrar(idVentana);
				},function(respuesta){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
				});
			}
				
			
		}else{
			bcsComunes.msg("warning","warning","El Traspaso debe tener al menos un Portafolio ingresado.");
			bcsComunes.ventanaCerrar(idVentana);
		}
	};
	
	ingtraAPV.fn.IngresoRecaudacion = function(datos){

		var EstadoTraspasoNoOk = false;
		var MontoDeposito = 0;

		console.log("datos:", datos);

		var traspasosRecaudacion = Array();
		$.each(datos.p_Traspasos, function(key,value){
			//if(value.EsReapertura === 'S') MontoDeposito = MontoDeposito + value.MontoReapertura;
			if(value.MontoReapertura > 0 ) MontoDeposito = MontoDeposito + value.MontoReapertura;
		});
		
		//console.log("MontoDeposito", MontoDeposito);

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
		var IdVentanamodal1 = bcsComunes.ventanaAbrir("IngresoRecaudacionDeposito", filterVentana, true, false, IdVentana);
		var promesa = bcsComunes.ventanaPromesa(IdVentanamodal1);
		promesa.then(function(data){
			if(data === undefined || Object.entries(data).length === 0) return;

			datos.p_Recaudacion = {
					FolioCartola : data.FolioCartola,
					LineaCartola : data.LineaCartola,
					IdSucursal : data.IdSucursal,
					CuentaCorriente : data.CuentaCorriente,
					IdTipoDeposito : data.IdTipoDeposito,
					IdBanco : data.IdBanco
				
			}
			var entrada = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'APV',
				p_TipoTraspaso : 'R',
				p_Recaudacion : {
					FolioCartola : data.FolioCartola,
					LineaCartola : data.LineaCartola,
					IdSucursal : data.IdSucursal,
					CuentaCorriente : data.CuentaCorriente,
					IdTipoDeposito : data.IdTipoDeposito,
					IdBanco : data.IdBanco
				},
				p_Traspasos : datos

			}

			bcsComunes.ventanaOverlay(IdVentana,true);
			
			bcsServiciosAPV.ReaperturarTraspaso(datos).$promise.then(function(respuesta){
			//bcsServiciosAPV.IngresoRecaudacionTraspaso(entrada).$promise.then(function(respuesta){
				 if(respuesta.P_OUT_ERRORESCONTADOR == 0){
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
					 bcsComunes.ventanaOverlay(IdVentana,false);
					 bcsComunes.ventanaCerrar(idVentana);
				 }else{
					 var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
					 var Errores =new Boolean(false);;
					 $.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
						if(value.tipo === 'ERROR'){
							Errores =true
						}
					 });
					 if(Errores){
					 	bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},IdVentana);
					 }
					 bcsComunes.ventanaOverlay(IdVentana,false);
					 bcsComunes.ventanaCerrar(idVentana);
				 }
			   },function(respuesta){
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR"),conflictos:util.parseServerResponse(respuesta)},IdVentana);
				   bcsComunes.ventanaOverlay(IdVentana,false);
				   bcsComunes.ventanaCerrar(idVentana);
			   });
			
		});
	};	
	
	ingtraAPV.Requerido = function(tipo)
	{
		if(tipo === 'MONTO')
		{
			if(ingtraAPV.dataForm.MontoPesoTraspaso !== null)
			{
				ingtraAPV.MontoUFOrigenRequerido 		= false;
				ingtraAPV.MontoUFOrigenDeshabilitado	= false;
				ingtraAPV.dataForm.kngMontoUFTraspaso = 0;
			}
		}
		else if(tipo === 'UF')
		{
			if(ingtraAPV.dataForm.MontoUFTraspaso !== null)
			{
				ingtraAPV.MontoPesoOrigenRequerido		= false;
				ingtraAPV.MontoPesoOrigenDeshabilitado	= false;
				ingtraAPV.dataForm.kngMontoPesoTraspaso = 0;
			}
		}
		
		
		if(ingtraAPV.dataForm.MontoPesoTraspaso === null || ingtraAPV.dataForm.MontoPesoTraspaso === undefined || ingtraAPV.dataForm.MontoPesoTraspaso === 0)
		{
			if(ingtraAPV.dataForm.MontoUFTraspaso === null || ingtraAPV.dataForm.MontoUFTraspaso === undefined || ingtraAPV.dataForm.MontoUFTraspaso === 0)
			{
				ingtraAPV.MontoPesoOrigenRequerido 		= true;
				ingtraAPV.MontoPesoOrigenDeshabilitado	= true;
				ingtraAPV.dataForm.kngMontoPesoTraspaso = 0;
				
				ingtraAPV.MontoUFOrigenRequerido 		= true;
				ingtraAPV.MontoUFOrigenDeshabilitado	= true;
				ingtraAPV.dataForm.kngMontoUFTraspaso = 0;
			}
		}
	};
	
	
	//*****************************************************************************************************************/
	//************************************************* Watch *********************************************************/
	//*****************************************************************************************************************/
	/*Este watch sirve para cuando necesiten llamar a un evento justo luego de que el DOM se haya cargado completamente. 
	 * Por lo que ese watch se ejecutará solo una vez*/
	$scope.$watch('$viewContentLoaded', function(){
		if(ingtraAPV.parametrosVentana.ModoVentana === 'C')
			ingtraAPV.datosForm.BloqueoControles = true;
		else
			ingtraAPV.datosForm.BloqueoControles = false;
		
		bcsComunes.ventanaOverlay(idVentana,false);
	});

	$scope.$watch('ingtraAPV.datosForm.PerfilComercialSeleccionado', function(newval,oldval ){
		let catInvSeleccionado = ingtraAPV.datosForm.CategoriaInversionistaSeleccionado;
		let catInvCliente = ingtraAPV.datosForm.IdCategoriaInversionistaIdActual;
		let pComercialSeleccionado = ingtraAPV.datosForm.PerfilComercialSeleccionado;
	
		if(catInvSeleccionado != undefined && catInvSeleccionado != false &&
			pComercialSeleccionado != undefined && pComercialSeleccionado != false){
				
				if(pComercialSeleccionado.IdCategoriaInversionista !== catInvCliente)
				{
					let titulo = "Perfil comercial: "+pComercialSeleccionado.IdPerfilComercialBase +" no corresponde a categoría inversionista: "+catInvSeleccionado.IdCategoriaInversionista;

					var result = {
						P_OUT_MENSAJES :
						[
							{
								id : 0,
								tipo : "ADVERTENCIA",
								codigo : "UI_PERFIL_COMERCIAL_NO_CORRESPONDE_CAT_INV",
								descripcion : "Perfil comercial no corresponde a categoría inversionista"
							}
						]
				}

				bcsComunes.ventanaMsg({
					mensaje:titulo,
					conflictos : util.parseServerResponse(result)
				},$scope.IdVentana);

				bcsComunes.msg("warning","warning","UI_PERFIL_COMERCIAL_NO_CORRESPONDE_CAT_INV");		
			}
		}
	});
	
	$scope.$watch('ingtraAPV.datosForm.PerfilComercialSeleccionado', function(newval,oldval ){
		let catInvSeleccionado = ingtraAPV.datosForm.CategoriaInversionistaSeleccionado;
		let catInvCliente = ingtraAPV.datosForm.IdCategoriaInversionistaIdActual;
		let pComercialSeleccionado = ingtraAPV.datosForm.PerfilComercialSeleccionado;
	
		if(catInvSeleccionado != undefined && catInvSeleccionado != false &&
			pComercialSeleccionado != undefined && pComercialSeleccionado != false){
				
				if(pComercialSeleccionado.IdCategoriaInversionista !== catInvCliente)
				{
					let titulo = "Perfil comercial: "+pComercialSeleccionado.IdPerfilComercialBase +" no corresponde a categoría inversionista: "+catInvSeleccionado.IdCategoriaInversionista;

					var result = {
						P_OUT_MENSAJES :
						[
							{
								id : 0,
								tipo : "ADVERTENCIA",
								codigo : "UI_PERFIL_COMERCIAL_NO_CORRESPONDE_CAT_INV",
								descripcion : "Perfil comercial no corresponde a categoría inversionista"
							}
						]
				}

				bcsComunes.ventanaMsg({
					mensaje:titulo,
					conflictos : util.parseServerResponse(result)
				},$scope.IdVentana);

				bcsComunes.msg("warning","warning","UI_PERFIL_COMERCIAL_NO_CORRESPONDE_CAT_INV");		
			}
		}
	});
	
	$scope.$watchGroup([
    	'ingtraAPV.dataForm.NombrePlanAhorroSeleccion.NombrePlanTipoInversion',
		'ingtraAPV.dataForm.ProductoAgrupadorSeleccion.NombreProductoAgrupador',
		'ingtraAPV.dataForm.RegimenAgrupadorSeleccion.IdMatrizAgrupadorAPV',
		'ingtraAPV.dataForm.kngMontoPesoTraspaso',
		'ingtraAPV.dataForm.kngMontoUFTraspaso'
	], function(newValues) {
	    let nombrePlan = newValues[0];
	    let producto = newValues[1];
	    let regimen = newValues[2];
	    let montoPeso = newValues[3];
	    let montoUF = newValues[4];
	    
		if (
			nombrePlan !== undefined &&
			producto !== undefined &&
			regimen !== undefined &&
		    ((montoPeso !== undefined && montoPeso > 0) || (montoUF !== undefined && montoUF > 0))
		) {
			if(ingtraAPV.HabilitoGuardarEdicion){
				ingtraAPV.HabilitoAgregar = false;
			}else{
				ingtraAPV.HabilitoAgregar = true;	
			}
			
		} else {
			ingtraAPV.HabilitoAgregar = false;
		}
	});
	
	$scope.$watch('ingtraAPV.datasource.DetalleTraspasoSeleccion', function(newValue) {
	  ingtraAPV.HabilitoEditar = newValue !== undefined && newValue !== null && ingtraAPV.parametrosVentana.ModoVentana === 'M';
	}, true);
});
