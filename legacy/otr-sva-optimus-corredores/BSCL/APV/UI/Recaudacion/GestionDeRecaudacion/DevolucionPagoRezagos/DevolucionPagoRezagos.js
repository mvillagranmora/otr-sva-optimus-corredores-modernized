/********************************************************************
 * Purpose	: Rezagos
 * Author	: PRONET IT
 * Date		: 21/11/2017
 *******************************************************************/
(function() {
	angular.module('ModuloAPV').controller('DevolucionPagoRezagosCtrl',
	function($state,$q,$scope,$translate,bcsComunes,bcsConsultasAPV,bcsServiciosAPV){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var dprez 			= this;
		dprez.idVentana 	= bcsComunes.ventanaWinId($scope);
		dprez.parametros 	= bcsComunes.ventanaParams(dprez.idVentana);
		dprez.domVentana 	= $("[ventanaid='"+dprez.idVentana+"']");	
		dprez.win 			= {};
		dprez.ds 			= {};
		dprez.fn 			= {};
		dprez.pagoCliente = true;
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/
		if(!dprez.parametros.dsDatosRecaudacion) {
			bcsComunes.msg("error","Abrir Ventana",'No se recibe identificador de recaudación.');
			bcsComunes.ventanaCerrar(dprez.idVentana);
		};

		dprez.ConfiguracionCuentaBancaria={
			filtros 		: {
								 IdPersona 			 : { "field": "IdPersona",       	"operator": "eq", "value" : 0 },
								 IdMoneda     		 : { "field": "IdMoneda",     		"operator": "eq", "value" : "CLP" }
							  },
		}
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		dprez.win.guardar = function(){
			bcsComunes.ventanaOverlay(dprez.idVentana,true);
		};


		if(dprez.parametros.tipoPersona === "CLIENTE"){
			dprez.oSocioNegocioIdDefecto =dprez.parametros.dsDatosRecaudacion[0].IdSocioDeNegocioCliente;
			dprez.PermiteBusquedaSocioNegocio = false;
			
		};

		if(dprez.parametros.tipoPersona === "EMPLEADOR"){
			dprez.oSocioNegocioIdDefecto =dprez.parametros.dsDatosRecaudacion[0].IdSocioDeNegocioEmpleador;
			dprez.PermiteBusquedaSocioNegocio = false;
			dprez.PermiteBusquedaPortafolio = false
			
		};
		
		if(dprez.parametros.tipoPersona === "OTROS"){
			dprez.oSocioNegocio={
				filtros: {
					CodigoTipoPersona : {"field": "CodigoTipoPersona","operator": "eq", "value"   :  'J'}
				},
			};
			dprez.PermiteBusquedaSocioNegocio = true;
		};
		
		dprez.oPortafolio = {
				filtros: {
					IdCliente : {"field" : "IdCliente", "operator" : "eq", "value" : 0}
				}	
		};
		
		dprez.win.cancelar = function(){
			bcsComunes.ventanaCerrar(dprez.idVentana);
		};
		
		$scope.$watch('dprez.pagoCliente',function(newval,oldval){
			if(dprez.pagoCliente){
				dprez.pagoEmpleador = false;
			}
			else{
				dprez.pagoEmpleador = true;
			}
		});
		
		$scope.$watch('dprez.pagoEmpleador',function(newval,oldval){
			if(dprez.pagoEmpleador){
				dprez.pagoCliente = false;
			}
			else{
				dprez.pagoCliente = true;
			}
		});
		
		$scope.$watch('dprez.CuentaBancariaSeleccion',function(newval,oldval){
			if (typeof dprez.CuentaBancariaSeleccion !== 'undefined' && dprez.CuentaBancariaSeleccion !== false)
			{
				dprez.CuentaCorriente = dprez.CuentaBancariaSeleccion.NumeroCuentaBancaria;
			}
		});
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var confDatosRecaudacion = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                sort 			: false,
				schema: {
					model: {
						fields: {
							FechaDeposito			: { type: "sysdate"},
							FechaIngreso			: { type: "sysdate"},
							FolioRecaudacion    	: { type: "string"},
							FolioCartola	    	: { type: "string"},
							LineaCartola	    	: { type: "string"},
							IdRecaudacionCliente	: { type: "number"},
							RutCliente				: { type: "string"},	
							Nombre					: { type: "string"},
							RecMontoDC				: { type: "number"},
							RecMontoAPV				: { type: "number"},
							RecTotal				: { type: "number"},
							RecDiferencia			: { type: "number"},
							DisMontoDC				: { type: "number"},
							DisMontoAPV				: { type: "number"},
							DisTotal				: { type: "number"},
							DisDiferencia			: { type: "number"},
							CodigoBanco				: { type: "string"},
							CuentaCorriente			: { type: "string"},
							Regimen					: { type: "string"},
							IdRecaudacionEmpleador	: { type: "number"},
							RutEmpleador			: { type: "string"},
							NombreEmpleador			: { type: "string"}
						}
					}
				}
			}	
		};
		
		var dsRezagos = new bcs.DataSource(confDatosRecaudacion);
		dprez.gridRezagos = {				 
			dataSource: dsRezagos,
			columns: [
				{field: "FechaDeposito"	,title: $translate.instant('UI_FECHA_DEPOSITO'), width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "FechaIngreso"	,title: $translate.instant('UI_FECHA_INGRESO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "FolioRecaudacion"	,title: $translate.instant('UI_FOLIO_RECAUDACION'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "FolioCartola"		,title: $translate.instant('UI_FOLIO_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "LineaCartola"		,title: $translate.instant('UI_LINEA_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "IdRecaudacionCliente",title: $translate.instant('UI_ID_RECAUDACION_CLIENTE'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "Regimen"			,title: $translate.instant('UI_REGIMEN'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "RutCliente"		,title: $translate.instant('UI_RUT_CLIENTE'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "Nombre"			,title: $translate.instant('UI_NOMBRE'), width: "250px", align: "left" , sortable : true, editable: false },
				{field: "IdRecaudacionEmpleador",title: $translate.instant('UI_ID_RECAUDACION_EMPLEADOR'), width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "RutEmpleador"		,title: $translate.instant('UI_RUT_EMPLEADOR'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "NombreEmpleador"	,title: $translate.instant('UI_NOMBRE_EMPLEADOR'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "RecMontoDC"		,title: $translate.instant('UI_MONTO_DC_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(RecMontoDC,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "RecMontoAPV"		,title: $translate.instant('UI_MONTO_APV_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(RecMontoAPV,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "RecTotal"			,title: $translate.instant('UI_TOTAL_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false 		, template : " <label> #:  (util.bcs_formato(RecTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "DisMontoDC"		,title: $translate.instant('UI_MONTO_DC_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(DisMontoDC,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "DisMontoAPV"		,title: $translate.instant('UI_MONTO_APV_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false , template : " <label> #:  (util.bcs_formato(DisMontoAPV,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "DisTotal"			,title: $translate.instant('UI_TOTAL_DISTRIBUIDO'), width: "80px", align: "left" , sortable : true, editable: false 	, template : " <label> #:  (util.bcs_formato(DisTotal,'GEN','Monto','" + $scope.$root.SesionIdMonedaSistema + "','','')).valor #</label>"},
				{field: "CodigoBanco"		,title: $translate.instant('UI_CODIGO_BANCO'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "CuentaCorriente"	,title: $translate.instant('UI_CTA_CTE'), width: "80px", align: "left" , sortable : true, editable: false },
			],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 130,
		    selectable 		: "row",
		    userSettings	: true,
			navigatable		: true,
			gridCreated 	: function(gridRezagos){
				$.each(dprez.parametros.dsDatosRecaudacion,function(key,value){
					dsRezagos.add({
						FechaDeposito			: value.FechaDeposito,
						FechaIngreso			: value.FechaIngreso,
						FolioRecaudacion    	: value.FolioRecaudacion,
						FolioCartola	    	: value.FolioCartola,
						LineaCartola	    	: value.LineaCartola,
						IdRecaudacionCliente	: value.IdRecaudacionCliente,
						RutCliente				: value.RutCliente,	
						Nombre					: value.Nombre,
						RecMontoDC				: value.RecMontoDC,
						RecMontoAPV				: value.RecMontoAPV,
						RecTotal				: value.RecTotal,
						RecDiferencia			: value.RecDiferencia,
						DisMontoDC				: value.DisMontoDC,
						DisMontoAPV				: value.DisMontoAPV,
						DisTotal				: value.DisTotal,
						DisDiferencia			: value.DisDiferencia,
						CodigoBanco				: value.CodigoBanco,
						CuentaCorriente			: value.CuentaCorriente,
						Regimen					: value.Regimen,
						IdRecaudacionEmpleador	: value.IdRecaudacionEmpleador,
						RutEmpleador			: value.RutEmpleador,
						NombreEmpleador			: value.NombreEmpleador
					});	
				});
			}
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		/*
		dprez.fn.iniciar = function() {
			if(dprez.parametros.dsDatosRecaudacion.length > 0){
				$.each(dprez.parametros.dsDatosRecaudacion,function(key,value){
					dsRezagos.add({
						FechaDeposito			: value.FechaDeposito,
						FechaIngreso			: value.FechaIngreso,
						FolioRecaudacion    	: value.FolioRecaudacion,
						FolioCartola	    	: value.FolioCartola,
						LineaCartola	    	: value.LineaCartola,
						IdRecaudacionCliente	: value.IdRecaudacionCliente,
						RutCliente				: value.RutCliente,	
						Nombre					: value.Nombre,
						RecMontoDC				: value.RecMontoDC,
						RecMontoAPV				: value.RecMontoAPV,
						RecTotal				: value.RecTotal,
						RecDiferencia			: value.RecDiferencia,
						DisMontoDC				: value.DisMontoDC,
						DisMontoAPV				: value.DisMontoAPV,
						DisTotal				: value.DisTotal,
						DisDiferencia			: value.DisDiferencia,
						CodigoBanco				: value.CodigoBanco,
						CuentaCorriente			: value.CuentaCorriente,
						Regimen					: value.Regimen,
						IdRecaudacionEmpleador	: value.IdRecaudacionEmpleador,
						RutEmpleador			: value.RutEmpleador,
						NombreEmpleador			: value.NombreEmpleador
					});	
				});
			}
			
			var filtro = { field: "IdRecaudacionCliente", operator: "eq", value: dprez.parametros.IdRecaudacionCliente.IdRecaudacionCliente};
			dprez.ds.DatosRecaudacion.query({filter : filtro}).then(function(result){
				if(dsDatosRecaudacion.length > 0){
					dprez.ds.DatosRecaudacion.data = dsDatosRecaudacion;
										
					
					dprez.kngFechaIngreso = dprez.ds.DatosRecaudacion.data()[0].FechaIngreso; 
					dprez.kngFechaDeposito = dprez.ds.DatosRecaudacion.data()[0].FechaDeposito;
					dprez.kngFolioRecaudacion = dprez.ds.DatosRecaudacion.data()[0].FolioRecaudacion;
					dprez.kngFolioRecaudacionCliente = dprez.ds.DatosRecaudacion.data()[0].FolioRecaudacionCliente;
					dprez.kngFolioCartola = dprez.ds.DatosRecaudacion.data()[0].FolioCartola;
					dprez.RutCliente = dprez.ds.DatosRecaudacion.data()[0].RutCliente;
					dprez.NombreCliente = dprez.ds.DatosRecaudacion.data()[0].Nombre;
					dprez.RutEmpleador = dprez.ds.DatosRecaudacion.data()[0].RutEmpleador;
					dprez.NombreEmpleador = dprez.ds.DatosRecaudacion.data()[0].NombreEmpleador;
					dprez.kngMontoRecaudacionDC = dprez.ds.DatosRecaudacion.data()[0].RecMontoDC;
					dprez.kngMontoRecaudacionAPV = dprez.ds.DatosRecaudacion.data()[0].RecMontoAPV;
					dprez.kngMontoRecaudacionTotal = dprez.ds.DatosRecaudacion.data()[0].RecTotal;
					dprez.kngMontoRecaudacionDiferencia = dprez.ds.DatosRecaudacion.data()[0].RecDiferencia;
					dprez.kngMontoDistribuidoDC = dprez.ds.DatosRecaudacion.data()[0].DisMontoDC;
					dprez.kngMontoDistribuidoAPV = dprez.ds.DatosRecaudacion.data()[0].DisMontoAPV;
					dprez.kngMontoDistribuidoTotal = dprez.ds.DatosRecaudacion.data()[0].DisTotal;
					dprez.kngMontoDistribuidoDiferencia = dprez.ds.DatosRecaudacion.data()[0].DisDiferencia;
					
				}
			});
			
		};
			*/
		
		dprez.win.devolver = function(){
			bcsComunes.ventanaOverlay(dprez.idVentana,true);
			var v_DevolucionIdPersona = "";
			var paramEntrada = [];
			$.each(dprez.gridRezagos.dataSource.view(),function(key,value){
				if(value.IdRecaudacionCliente !== undefined){
					var datosparamEntrada = {
						IdRecaudacionCliente 	: value.IdRecaudacionCliente
					};				
					paramEntrada.push(datosparamEntrada);
					if("CLI" == "CLI") v_DevolucionIdPersona = value.IdRecaudacionCliente;
					
				}
			});
			
			var grupoParamEntrada = _.groupBy(paramEntrada,'IdRecaudacionCliente');
			
			if (grupoParamEntrada.length === 0){
				bcsComunes.ventanaMsgErr($translate.instant('UI_NO_HAY_RECAUDACIONES'),$scope.ventanaId);
				bcsComunes.ventanaOverlay(dprez.idVentana,false);
				return;				
			};
			var parametros = {				
				p_IdEmpresa  						: $scope.SesionIdEmpresa,
				p_IdUsuario  						: $scope.SesionIdUsuario,
				p_IdModulo   						: "APV", 
				p_DevolucionTipoPersona   			: dprez.parametros.tipoPersona, //CLI - EMP- OTR
				p_DevolucionIdPersona   			: dprez.oSocioNegocio.seleccionado.IdPersona,  
				p_DevolucionIdPortafolio   			: dprez.oPortafolioidActual === false ?0: dprez.oPortafolioidActual,  
				p_DevolucionIdMedioPago   			: dprez.MedioPagoIdActual,    
				p_DevolucionIdBanco		   			: dprez.CuentaBancariaIdActual,    
				p_DevolucionIdCuentaCorriente   	: dprez.CuentaCorriente,  
				p_Parametros : paramEntrada
			};

			bcsServiciosAPV.DevolucionPagoRezagos(parametros).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},dprez.idVentana);
					bcsComunes.ventanaOverlay(dprez.idVentana,false);
				}
				else{
					var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
					$.each(ErrorDetalle,function(key,value){	
						bcsComunes.msg("error","Error","Existen problemas:" + value );
					});
					bcsComunes.ventanaOverlay(dprez.idVentana,false);
			    }
	   		},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
	   			$.each(ErrorDetalle,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},dprez.idVentana);
	   		});
			
		};
		
	});
}());