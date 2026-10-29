angular.module('ModuloTesoreria').controller('ConsultarNominasCtrl',
		function($scope,$state,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria)
{
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.frm = {};
	$scope.btnActivo = true;

	
	//SETEO INICIAL
	$scope.frm.kngFechaIngreso = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.frm.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	 
	
	//COMBOBOX
	$scope.frm.oConfiguracionBanco={
			tipo : "combobox-banco-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.frm.oConfiguracionCuentaCorredora={
			tipo : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdBanco : { "field"   : "IdBanco","operator": "eq", "value"   : 0 },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.frm.oConfiguracionEstado={
			tipo : "combobox-estado-tesoreria",
			configuracion : {},
			filtros : {
				NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value": 'TES.NominaTransferencia'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''			
	};
	
	
	//GRILLAS
	var dsNominas = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdNominaTransferencia", dir: "desc" },
			pageSize 			: 25,								
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1033",dataType : "json"}},
			schema : {
				model : {
					fields : {										
						IdNominaTransferencia		: {type: "number"},						
						IdComprobantePartidaEgreso	: {type: "number"},
						FechaMovimiento				: {type: "sysdate"},
						FechaLiquidacion			: {type: "sysdate"},
						FechaRecepcion				: {type: "sysdate"},
						IdMedioDePago				: {type: "string"},
						TipoMedioDePago				: {type: "string"},
						NombreMedioDePago			: {type: "string"},
						NombreModalidadDePago		: {type: "string"},
						IdTipoCuentaBanco			: {type: "number"},
						IdCuentaBanco				: {type: "number"},
						NombreCuentaBancariaFull	: {type: "string"},
						IdBanco						: {type: "number"},
						NombreBanco					: {type: "string"},
						IdMoneda					: {type: "string"},
						NombreMoneda				: {type: "string"},
						Monto						: {type: "number"},
						NombreEstado				: {type: "string"},
						IdEstado					: {type: "string"},
					}
				}
			}
		}
	});	
	$scope.frm.confBasicaNominas = {
		dataSource			: dsNominas,
		columns				: [	 {field : "IdNominaTransferencia", 		title:"UI_NOMINA",				width: "100px"}
								,{field : "FechaMovimiento", 			title:"UI_FECHA_INGRESO", 		width: "120px"}
								,{field : "FechaLiquidacion", 			title:"UI_FECHA_LIQUIDACION",	width: "120px"}
								,{field : "NombreMedioDePago", 			title:"UI_MEDIO_DE_PAGO", 		width: "120px"}	
								,{field : "NombreBanco", 				title:"UI_BANCO", 				width: "120px"}
								,{field : "NombreCuentaBancariaFull", 	title:"UI_CUENTA_BANCARIA", 	width: "250px"}
								,{field : "IdComprobantePartidaEgreso",	title:"UI_PARTIDA_EGRESO", 		width: "120px"}
								,{field : "NombreMoneda", 				title:"UI_MONEDA", 				width: "120px"}
								,{field : "Monto", 						title:"UI_MONTO", 				width: "120px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
								,{field : "FechaRecepcion", 			title:"UI_FECHA_RECEPCION", 	width: "120px"}
								,{field : "NombreEstado", 				title:"UI_ESTADO", 				width: "80px"}
							  ],
		rowColorBackground	: function(row) { 
								 var color = "";
								 if (row.IdEstado == 'ANU')
								 	color = "red";
								 return color;
							  },
		sortable			: true,
		pageable			: false,
		detailExpand		: function(e) { 
								this.collapseRow(this.tbody.find(' > tr.k-master-row').not(e.masterRow));
								this.clearSelection();
								this.select(e.masterRow);},
		//autoBind			: true,  //comentar
		filterable			: true,
		height				: 510,
		selectable 			: "multiple row",
		userSettings		: true
	};

	
	          
	$scope.verDetalleNominasTransferencias = function (seleccion)
	{
		bcsComunes.ventanaAbrir("DetalleNominasTransferencias", seleccion, false, false );
	};	
	
	
	$scope.talonarioCheque = function(datos)
	{ //Incorporar información de cheques
		if (datos[0].IdEstado === 'ING')
		{		
			if (datos[0].TipoMedioDePago = 'CHE' && datos[0].UsaTalonarioCheque == 'S' && datos[0].DetalleSinCheque > 0)
			{
				var idModal = bcsComunes.ventanaAbrir("IngresoDatosTalonarioChequesModal", datos, true, true );
				var promesa = bcsComunes.ventanaPromesa(idModal);

				promesa.then(function(data){
					$scope.buscarNominas($scope.frm);
				});
			}
			else
			{
				if ( datos[0].TipoMedioDePago == 'CHE' && datos[0].DetalleSinCheque == 0 )
					bcsComunes.msg("warning","warning",$translate.instant('UI_TODOS_REGISTRO_CON_NUMERO_CHEQUE'));
				else
					bcsComunes.msg("warning","warning",$translate.instant('UI_MEDIO_DE_PAGO_DISTINTO_A_CHEQUE'));
			}
		}
		else 
		{
			bcsComunes.msg("warning","warning",$translate.instant('UI_ESTADO_NO_PERMITE_EDITAR_REGISTRO'));
		}
	};
	
	
	

	//OPCIONES
	$scope.buscarNominas = function(filtros){
		var filters = new Array();
		
		filters.push({ field: "EsNomina", operator: "eq", value: 'S' });

		if (filtros.oConfiguracionBanco.seleccionado.IdBanco)
			filters.push({ field: "IdBanco", operator: "eq", value: filtros.oConfiguracionBanco.seleccionado.IdBanco });
		if (filtros.oConfiguracionCuentaCorredora.seleccionado.IdCuentaBanco)
			filters.push({ field: "IdCuentaBanco", operator: "eq", value: Number(filtros.oConfiguracionCuentaCorredora.seleccionado.IdCuentaBanco) });
		if (filtros.ngNomina && filtros.ngNomina != 0)
			filters.push({ field: "IdNominaTransferencia", operator: "eq", value: (filtros.ngNomina) });
		if (filtros.kngFechaIngreso)
			filters.push({ field: "FechaMovimiento", operator: "eq", value: util.date(util.dateOdata(filtros.kngFechaIngreso)).hours(0).minutes(0).seconds(0).toDate() });
		if (filtros.kngFechaLiquidacion)
			filters.push({ field: "FechaLiquidacion", operator: "eq", value: util.date(util.dateOdata(filtros.kngFechaLiquidacion)).hours(0).minutes(0).seconds(0).toDate() });
		if (filtros.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.oConfiguracionEstado.seleccionado.IdEstado });
						
		//dsNominas.filters(filters);
		dsNominas.filter(filters);
	};

	
	
	$scope.limpiarFiltros = function(filtros) 
	{
		filtros.oConfiguracionBanco.idActual = 0;
		filtros.oConfiguracionCuentaCorredora.idActual = 0;
		filtros.oConfiguracionEstado.idActual = 0;
		filtros.ngNomina = 0;

		filtros.kngFechaIngreso = util.dateInicioDia($scope.SesionFechaSistema);
		filtros.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.frm.seleccionNominas = false;

		dsNominas.removeAll();
	};
	
	
	
	//Cambio de estado, certificación de Formas de Pago y Liquidación Comprobante
	$scope.recepcionarLiquidar = function(seleccion) 
	{ 	
		if (seleccion != undefined)
		{
			var dataDVPCols = [];
			$.each(seleccion, function(index, value)
			{	
				dataDVPCols.push({IdNominaTransferencia : value.IdNominaTransferencia,
				                  IdMedioDePago         : value.IdMedioDePago,
                                  Monto                 : value.Monto  
				                 });
			});
			var datosDVP = {};
			datosDVP.p_IdEmpresa               = $scope.SesionIdEmpresa; 
			datosDVP.p_IdUsuario               = $scope.SesionIdUsuario; 
			datosDVP.p_IdModulo                = 'TES';
			datosDVP.p_UsaReglasNegocio        = 'S';
			datosDVP.p_Fecha			       = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			datosDVP.p_NominaTransferencia     = dataDVPCols;
			
			// Valida las transferencias DVP
			bcsComunes.log().info(JSON.stringify(datosDVP));
			var validarDCV = bcsServiciosTesoreria.ValidarTransferenciaDVP(datosDVP);
			validarDCV.$promise.then(function(result){
				
				if(result.P_OUT_VALIDO==='S') {
					var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_RECEPCIONARA_LOS_REGISTROS_SELECCIONADOS') );
					t.result.then(function(result){
						
						if (result===OPCIONES_BCSALERTA.ACEPTAR){
							bcsComunes.ventanaOverlay($scope.winID, true);
							$scope.btnActivo = false;

							var dataCols = [];
							$.each(seleccion, function(index, value)
							{
								dataCols.push({IdNominaTransferencia : value.IdNominaTransferencia});
							});
							
							if (dataCols.length > 0)
							{
								//DATOS SERVICIO
								var datos = {};
								datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
								datos.p_IdUsuario               = $scope.SesionIdUsuario; 
								datos.p_IdModulo                = 'TES';
								datos.p_UsaReglasNegocio        = 'S';
			
								datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
								datos.p_RegistrosARecepcionar	= dataCols;
						
								bcsComunes.log().info(JSON.stringify(datos));
								bcsServiciosTesoreria.RecepcionarNominaTransferencia(datos,success,error);
							}
						}
					});
					
				} else {
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(result)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay($scope.idVentana, false);
				}

				
			},function(result){
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(result)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay($scope.idVentana, false);
			})
		}
		else 
		{
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		}
		return;
	};
	
	
	


	//-----------------------------------------------------------
	// Generar carta de transferencias bancarias(RPT) 
	//-----------------------------------------------------------
	$scope.generarCarta = function(seleccion) {
		var seleccionNomina = seleccion[0]; //Sólo toma la primera fila seleccionada		
		var idModal = bcsComunes.ventanaAbrir("GenerarCartaBanco", seleccionNomina, false, false, $scope.winID);
	};	
	

	
	//-----------------------------------------------------------
	// Anular la nomina completa (En la UI de detalle nomina se puede anular parcialmente) 
	//-----------------------------------------------------------
	$scope.anular = function(seleccion) {	
		if (seleccion != undefined)
		{	
			var dataCols = [];	
			$.each(seleccion, function(index, value)
			{
				dataCols.push({IdNominaTransferencia : value.IdNominaTransferencia});
			});

			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ANULARA_LOS_REGISTROS_SELECCIONADOS') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){

					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;
					var dataCols = [];
					
					$.each(seleccion, function(index, value){
						var ObjCus = {
							"Id"		: value.IdNominaTransferencia || 0,
							"Entidad"	: 'TES.NominaTransferencia'
						};
						dataCols.push(ObjCus);
					});
					
					
					if (dataCols.length > 0)
					{
						var datos = {};
						datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
						datos.p_IdUsuario               = $scope.SesionIdUsuario; 
						datos.p_IdModulo                = 'TES';
						datos.p_UsaReglasNegocio        = 'S';
	
						datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_RegistrosAAnular		= dataCols;
	
						bcsComunes.log().info(JSON.stringify(datos));
						bcsServiciosTesoreria.AnularNominasTransferencias(datos,success,error);
					}

				}
			});
		}
		else 
		{
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		}
		return;
	};


	//-----------------------------------------------------------
	// MANEJO DE RESPUESTA DE SERVICIOS 
	//-----------------------------------------------------------	
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			//Se debe mostrar el listado del proceso
			
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			
			$scope.buscarNominas($scope.frm);
		}
		else
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
		}
		
		bcsComunes.ventanaOverlay($scope.winID, false);
		$scope.btnActivo = true;
	};
	
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
		
			
		bcsComunes.ventanaOverlay($scope.winID, false);
		$scope.btnActivo = true;
	};
});