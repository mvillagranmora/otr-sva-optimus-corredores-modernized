angular.module('ModuloAPV').controller('FormaDePagoAPVCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsConsultasAPV,bcsServiciosAPV)
{
	
	$scope.data = {};	
	//Recupera parametros desde la ventana recibida
	$scope.IdVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var fmpagAPV = this;
	fmpagAPV.datosForm = {};
	fmpagAPV.fn = {};
	fmpagAPV.datasource = {};
	fmpagAPV.ConfiguracionCmbo = {};
	fmpagAPV.consulta = {};
	fmpagAPV.parametrosVentana = {}
	fmpagAPV.parametrosVentana = bcsComunes.ventanaParams($scope.IdVentana);
	fmpagAPV.datosForm.fechaTraspaso = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	//fmpagAPV.datosForm.kngMontoTotal = fmpagAPV.parametrosVentana[0].MontoPesos;
	fmpagAPV.EsEntidadDestino = false;
	fmpagAPV.datosForm.HabilitaBanco = false;
	//*****************************************************************************************************************/
	//********************************************* Configuracion *****************************************************/
	//*****************************************************************************************************************/
	fmpagAPV.datosForm.ConfiguracionCuentaBancaria={
		filtros 		: {
							 IdPersona 			 : { "field": "IdPersona",       	"operator": "eq", "value" : fmpagAPV.parametrosVentana[0].IdPersona },
							 IdMoneda     		 : { "field": "IdMoneda",     		"operator": "eq", "value" : "CLP" }
						  }
	};	
	
	fmpagAPV.datosForm.EntidadDestino = {
		filtros 		: {  
							 IdTipoEntidadRecaudadora : {field: "IdTipoEntidadRecaudadora", operator: "eq", value: 1 }	
						  }	
	};
	
	//*****************************************************************************************************************/
	//*********************************************** DataSource ******************************************************/
	//*****************************************************************************************************************/
	var defMedioPago = {
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
						IdContratoSAAPV			: { type: "number"},
						FechaIngreso			: { type: "string"},
						CodigoTraspaso			: { type: "string"},
						IdProductoAgrupador		: { type: "number"},
						IdSocioNegocioCli		: { type: "number"},
						IdSocioNegocioEmp		: { type: "number"},
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
						PorcentajeTotalTraspaso	: { type: "number"}
					}
				}
			}
		}	
	};	

	var defConfiguracionRetencion = {
		type: "odata",
		definition : {
			serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            pageSize        : 100,
            sort 			: { field: "IdConfiguracionRetencionTraspaso", dir: "DESC" },
            pageSize        : 10,
            transport		: {
				read: {
					url 		: bcsConsultasAPV('ConsultarConfiguracionRetencionTraspaso'),
					dataType	: "json"
			    }
			},
			schema: {
				model: {
					fields: {
						IdConfiguracionRetencionTraspaso	: { type: "number"},
						IdEmpresa							: { type: "number"},
						CodigoRetiro						: { type: "string"},
						NombreRetiro						: { type: "string"},
						IdSocioDeNegocio					: { type: "number"},
						IdPersonaRetencion					: { type: "number"},
						DocumentoIdentidadDefecto			: { type: "string"},
						RazonSocial							: { type: "string"},
						IdRegimenTributario					: { type: "number"},
						Codigo								: { type: "string"},
						PorcentajeRetencion					: { type: "number"},
						MontoTopeRetencion					: { type: "number"},
						DescripcionRetencion				: { type: "string"},
						IdEstado							: { type: "string"}
					}
				}
			}
		}	
	};
					
	//*****************************************************************************************************************/
	//****************************************** Definicion de Grillas ************************************************/
	//*****************************************************************************************************************/
	var dsMedioPago = new bcs.DataSource(defMedioPago);
	fmpagAPV.datasource.MedioPago = {
		dataSource: dsMedioPago,
		columns: [	
			{field: "NombrePlan"				,title: $translate.instant('UI_NOMBRE_'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "CodigoAgrupador" 			,title: $translate.instant('UI_CODIGO_AGRUPADOR'), width: "80px", align: "rigth" , sortable : true, editable: false, hidden: true },
			{field: "Producto"					,title: $translate.instant('UI_PRODUCTO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "Regimen"					,title: $translate.instant('UI_REGIMEN'), width: "80px", align: "rigth" , sortable : true, editable: false },
			{field: "MontoTotalTraspaso"		,title: $translate.instant('UI_MONTO_PESOS'), width: "80px", align: "rigth" , sortable : true, editable: false },
		    {field: "UFTotalTraspaso"			,title: $translate.instant('UI_MONTO_UF'), width: "80px", align: "rigth" , sortable : true, editable: false },
		    {field: "PorcentajeTotalTraspaso"	,title: $translate.instant('UI_PORCENTAJE'), width: "80px", align: "rigth" , sortable : true, editable: false },
		    ],	
		sortable		: false,
		pageable		: true,
		autoBind		: false,
		height			: 130,
	    selectable 		: "row",
	    userSettings	: true,
		navigatable		: true,
		gridCreated		: function(grid){
//			if(fmpagAPV.parametrosVentana.ModoVentana === 'M'){
//				fmpagAPV.consulta.TraspasosSAAPV(fmpagAPV.parametrosVentana[0].IdContratoSAAPV);	
//			}
		}
	};

	//*****************************************************************************************************************/
	//*********************************************** Consultas *******************************************************/
	//*****************************************************************************************************************/
	fmpagAPV.fn.ConsultarRetencionTraspaso = function(){
		var filtro = Array();
		if(fmpagAPV.parametrosVentana[0].CodigoTipoRetiro !== undefined && fmpagAPV.parametrosVentana[0].CodigoTipoRetiro !== '')
			filtro.push({field: "CodigoRetiro"	, operator: "eq", value: fmpagAPV.parametrosVentana[0].CodigoTipoRetiro });
		/*
		if(fmpagAPV.parametrosVentana.CodigoTipoRetiro === 'RES'){
			if(fmpagAPV.parametrosVentana.CodigoRegimen !== undefined && fmpagAPV.parametrosVentana.CodigoRegimen !== ''){
				filtro.push({field: "Codigo"	, operator: "eq", value: fmpagAPV.parametrosVentana.CodigoRegimen });	
			}
		}*/
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var ConsultarRetencionTraspaso = new bcs.DataSource(defConfiguracionRetencion);
		ConsultarRetencionTraspaso.query({filter : filtro}).then(function(){
			if(ConsultarRetencionTraspaso.view().length > 0){
				if(ConsultarRetencionTraspaso.view()[0].PorcentajeRetencion !== null){ //&& ConsultarRetencionTraspaso.view()[0].PorcentajeRetencion > 0){
//					fmpagAPV.datosForm.kngMontoRetencion = fmpagAPV.datosForm.kngMontoTotal * (Number(ConsultarRetencionTraspaso.view()[0].PorcentajeRetencion)/100);
//					fmpagAPV.datosForm.kngMonto = fmpagAPV.datosForm.kngMontoTotal - fmpagAPV.datosForm.kngMontoRetencion;
				}
				bcsComunes.ventanaOverlay(idVentana,false);
			}
		});
		
	};	
	//*****************************************************************************************************************/
	//*********************************************** Funciones *******************************************************/
	//*****************************************************************************************************************/
	fmpagAPV.fn.CalculoRetencionTraspasos = function(){
		
		bcsComunes.ventanaOverlay($scope.IdVentana,true);

		var traspasos = [];
		
		$.each($scope.fmpagAPV.parametrosVentana,function(key,value){
			var Traspaso = {
					IdTraspaso    				: value.IdTraspaso
				}
			traspasos.push(Traspaso);
		});

		var parametros = {				
			p_IdEmpresa  	: $scope.SesionIdEmpresa,
			p_IdUsuario  	: $scope.SesionIdUsuario,
			p_IdModulo   	: "APV", 
			p_Parametros    : traspasos 
		};
		
		bcsServiciosAPV.CalculoRetencionTraspasos(parametros).$promise.then(function(respuesta){
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				
				fmpagAPV.datosForm.kngMontoTotal = respuesta.MontoTotal;
				fmpagAPV.datosForm.kngMonto = respuesta.MontoConDescuento;
				fmpagAPV.datosForm.kngMontoRetencion = respuesta.MontoRetencion;

				bcsComunes.ventanaOverlay($scope.IdVentana,false);
			}
			else{
				console.log(respuesta.P_OUT_MENSAJES[0].descripcion);
				bcsComunes.ventanaOverlay($scope.IdVentana,false);
		    }
   		},function(respuesta){
			console.log(respuesta);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
   		});
	};
	
	fmpagAPV.fn.EnviarTraspaso = function(){
		//Agregar validacion
		
		if(fmpagAPV.datosForm.MedioPagoSeleccionado === undefined || fmpagAPV.datosForm.MedioPagoSeleccionado === false){
			bcsComunes.msg("warning","warning","Debe seleccionar Medio de Pago.");
			return;
		}
		
		if(fmpagAPV.datosForm.MedioPagoSeleccionado.TipoMedioDePago === "TRA"){
			if(fmpagAPV.datosForm.CuentaBancariaSeleccion === undefined || fmpagAPV.datosForm.CuentaBancariaSeleccion === false){
				bcsComunes.msg("warning","warning","Debe seleccionar Banco.");
				return;
			}	
		}	
		
		if(fmpagAPV.datosForm.kngFechaPago === undefined || fmpagAPV.datosForm.kngFechaPago === false || fmpagAPV.datosForm.kngFechaPago === ''){
			bcsComunes.msg("warning","warning","Debe seleccionar Fecha de Pago.");
			return;
		}
		
		if(fmpagAPV.datosForm.kngMonto === 0){
			bcsComunes.msg("warning","warning","Debe venir Monto.");
			return;
		}
				
		datosTraspaso = [];
		
		$.each($scope.fmpagAPV.parametrosVentana,function(key,value){
			var Traspaso = {//IdPersona,IdPortafolio, fecha, IdBanco, IdCuentaCorrienteSocio, ModPago
					IdTraspaso    				: value.IdTraspaso,
					IdPersona	    			: value.IdPersona,
					IdPersonaCliente   			: value.IdPersonaCliente,
					IdPersonaEntidad   			: value.IdPersonaEntidad,
					IdPortafolio	    		: 0, //$scope.fmpagAPV.parametrosVentana.IdPortafolio,
					Fecha						: util.dateSPdate($scope.fmpagAPV.datosForm.kngFechaPago),
					IdBanco						: $scope.fmpagAPV.datosForm.CuentaBancariaSeleccion.IdBanco,
					IdCuentaCorriente			: $scope.fmpagAPV.datosForm.CuentaCorriente,
					IdMedioPago					: $scope.fmpagAPV.datosForm.MedioPagoIdActual,
					MontoTotal					: value.MontoPesos
				}
				datosTraspaso.push(Traspaso);
		});
		
		
		/*Se Debe Agregar Retenciones*/
		
		/*Se Debe Agregar Retenciones*/
		
		var parametros = {				
			p_IdEmpresa  				: $scope.SesionIdEmpresa,
			p_IdUsuario  				: $scope.SesionIdUsuario,
			p_IdModulo   				: "APV", 
			p_Parametros                : datosTraspaso 
		};

			
		bcsComunes.ventanaOverlay($scope.IdVentana,true);
		bcsServiciosAPV.CrearTraspasoTesoreria(parametros).$promise.then(function(respuesta){
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			var mensajeCarga = new Array();
			$.each(respuesta.P_OUT_MENSAJES,function(key,value){
				 var objCarga = {
						IdInterno : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : value.descripcion
				 };
				 mensajeCarga.push(objCarga);
			});
			respuesta.P_OUT_RESULTADO = mensajeCarga;
			if(respuesta.P_OUT_RESULTADO.length > 0){
				//bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga } ,false,false,$scope.winID);
				var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga } ,false,false, $scope.winID);		
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					bcsComunes.ventanaOverlay($scope.IdVentana,false);
					bcsComunes.ventanaCerrar($scope.IdVentana);
				});	
			}
			
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
			fmpagAPV.fnLimpiarMedioPago();
   		},function(respuesta){
			var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
   			$.each(ErrorDetalle,function(key,value){	
				bcsComunes.msg("error","Error","Existen problemas:" + value );
			});
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},$scope.IdVentana);
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
   		});
		
	};
	
	fmpagAPV.fnLimpiarMedioPago = function(){
		fmpagAPV.datosForm.MedioPagoSeleccionado = undefined;
		fmpagAPV.datosForm.CuentaBancariaSeleccion = undefined;
		fmpagAPV.datosForm.kngFechaPago = undefined;
		fmpagAPV.datosForm.kngMonto = 0;
	};
	
	//*****************************************************************************************************************/
	//************************************************ Watchs *********************************************************/
	//*****************************************************************************************************************/
	//Al cambiar la "Cuenta Bancaria de la corredora" se actualizan los datos de visualización.	
	$scope.$watch('fmpagAPV.datosForm.CuentaBancariaSeleccion',function(newval,oldval){
		if (typeof fmpagAPV.datosForm.CuentaBancariaSeleccion !== 'undefined' && fmpagAPV.datosForm.CuentaBancariaSeleccion !== false)
		{
			fmpagAPV.datosForm.CuentaCorriente = fmpagAPV.datosForm.CuentaBancariaSeleccion.NumeroCuentaBancaria;
		}
	});
	
	$scope.$watch('fmpagAPV.datosForm.MedioPagoSeleccionado',function(newval,oldval){
		if (typeof fmpagAPV.datosForm.MedioPagoSeleccionado !== 'undefined' && fmpagAPV.datosForm.MedioPagoSeleccionado !== false)
		{
			if(fmpagAPV.datosForm.MedioPagoSeleccionado.TipoMedioDePago === "TRA") {
				fmpagAPV.datosForm.HabilitaBanco = true;
			}else{
				fmpagAPV.datosForm.HabilitaBanco = false;
			}
			
			fmpagAPV.datosForm.CuentaCorriente = undefined;
			fmpagAPV.datosForm.CuentaBancariaSeleccion = false;
			fmpagAPV.datosForm.CuentaBancariaIdActual = false;
		}
	});
	
	/*Este watch sirve para cuando necesiten llamar a un evento justo luego de que el DOM se haya cargado completamente. 
	 * Por lo que ese watch se ejecutará solo una vez*/
	$scope.$watch('$viewContentLoaded', function(){	    
		fmpagAPV.fn.ConsultarRetencionTraspaso();
		fmpagAPV.fn.CalculoRetencionTraspasos();
		if(fmpagAPV.parametrosVentana[0].CodigoTipoRetiro === 'TRA'
			|| fmpagAPV.parametrosVentana[0].CodigoTipoRetiro === 'JUB'){
			fmpagAPV.EsEntidadDestino = true;
			fmpagAPV.datosForm.EntidadDestinoIdActual = fmpagAPV.parametrosVentana[0].CodigoEntidad;
			fmpagAPV.datosForm.EntidadDestinoIdActualDefecto = fmpagAPV.parametrosVentana[0].CodigoEntidad;
			fmpagAPV.datosForm.EntidadDestinoIdActualPostFiltro = fmpagAPV.parametrosVentana[0].CodigoEntidad;
		}else if(fmpagAPV.parametrosVentana[0].CodigoTipoRetiro === 'HER'
					|| fmpagAPV.parametrosVentana[0].CodigoTipoRetiro === 'RES'){
			fmpagAPV.EsEntidadDestino = false;
			fmpagAPV.datosForm.SocioNegocioidActual = fmpagAPV.parametrosVentana[0].IdSocioNegocioCli;
			fmpagAPV.datosForm.SocioNegocioidActualDefecto = fmpagAPV.parametrosVentana[0].IdSocioNegocioCli;
		}
	});	
});
