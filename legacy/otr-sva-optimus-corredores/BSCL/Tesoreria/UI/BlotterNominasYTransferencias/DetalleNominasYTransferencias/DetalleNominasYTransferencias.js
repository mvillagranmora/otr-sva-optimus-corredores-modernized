angular.module('ModuloTesoreria').controller('DetalleNominasTransferenciasCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria)
{	
	$scope.parametros 	= {};
	$scope.winID  		= bcsComunes.ventanaWinId ($scope);
	$scope.parametros 	= bcsComunes.ventanaParams($scope.winID);
	
	$scope.oNominaTransferencia = $scope.parametros[0];	
	$scope.dataReprocesada = {}; //marca registro editados
	$scope.gridApi = {};
	
	
	var dsNominaDetalle = {
		type : "odata",
		definition : {
			serverFiltering 	: true,  // Filtrado de datos en el servidor.
			serverPaging 		: true,  // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			
			filter              : [{ field: "IdNominaTransferencia", operator: "eq", value: $scope.oNominaTransferencia.IdNominaTransferencia }],
			sort 				: { field: "IdNominaTransferenciaDetalle", dir: "desc" },
			pageSize 			: 24,												
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1034",dataType : "json"}},
			schema : {
				model : {
					fields : {										
						IdNominaTransferencia				: {type: "number"},
						IdComprobante						: {type: "number"},
						FechaIngresoComprobante				: {type: "sysdate"},
						NombrePersona						: {type: "string"},
						DocumentoIdentidad					: {type: "string"},
						NumeroCuentaBancariaSocio			: {type: "string"},
						NombreCuentaBancariaSocio			: {type: "string"},
						NumeroChequeCorredora				: {type: "number"},
						IdMoneda							: {type: "string"},
						NombreMoneda						: {type: "string"},
						Monto								: {type: "number"},
						NombreEstado						: {type: "string"},
						IdEstado							: {type: "string"},
						NombreBancoCorredora				: {type: "string"},
						NombreTipoCuentaBancariaCorredora	: {type: "string"},
						NombreCuentaBancariaCorredora		: {type: "string"},
						NumeroCuentaBancariaCorredora		: {type: "string"},
						NumeroSerieCorredora				: {type: "string"},
						NumeroDocumentoCorredora			: {type: "string"},
						NombreTipoCuentaBancariaSocio		: {type: "string"},
						NumeroSerieSocio					: {type: "string"},
						NumeroDocumentoSocio				: {type: "string"},
						AliasBanco				            : {type: "string"},
						CuentaDCVOrigen				        : {type: "string"},
						CuentaDCVDestino				    : {type: "string"},
						ClaveDCV				            : {type: "string"},
						Nemotecnico							: {type: "string"}
					}
				}
			}
		}
	};

	
	
	$scope.confBasicaNominaTransferenciaDetalle =  {
			dataSource			: new bcs.DataSource(dsNominaDetalle),
			columns				: [	 {field : "IdComprobante", 				title:"UI_COMPROBANTE", 		width: "110px"}
									,{field : "FechaIngresoComprobante", 	title:"UI_FECHA_INGRESO", 		width: "120px"}
									,{field : "NombrePersona", 				title:"UI_SOCIO_NEGOCIO", 		width: "200px", attributes:{style:"white-space:nowrap;"}}
									,{field : "DocumentoIdentidad", 		title:"UI_DOCUMENTO_IDENTIDAD",	width: "130px", attributes:{style:"white-space:nowrap;"}}
									,{field : "NombreMoneda", 				title:"UI_MONEDA", 				width: "100px", attributes:{style:"white-space:nowrap;"}}
									,{field : "Monto", 						title:"UI_MONTO", 				width: "120px", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
									,{field : "Nemotecnico", 			    title:"UI_NEMOTECNICO", 	    width: "100px"}
									//Datos de la Corredora
									,{title   : "UI_CORREDORA", align: "center",
									   columns : [
													 {field : "NombreBancoCorredora",  		       title:'UI_BANCO',           		width : "100px", attributes:{style:"white-space:nowrap;"}}
													,{field : "NombreTipoCuentaBancariaCorredora", title:'UI_TIPO_CUENTA_BANCARIA', width : "100px"}
													,{field : "NombreCuentaBancariaCorredora",     title:'UI_CUENTA_BANCARIA',      width : "100px", attributes:{style:"white-space:nowrap;"}}
													,{field : "NumeroCuentaBancariaCorredora",     title:'UI_NUMERO_CUENTA',        width : "80px"}
													,{field : "NumeroSerieCorredora",     		   title:'UI_SERIE_CHEQUERA', 		width : "80px"}
													,{field : "NumeroDocumentoCorredora",   	   title:'UI_FOLIO_DOCUMENTO', 		width : "80px"}
												]} 

									//Datos del Socio 
									,{title   : "UI_SOCIO_NEGOCIO", align: "center",
									  columns : [				             
													 {field : "NombreBancoSocio",     		   title:'UI_BANCO',           		width : "100px", attributes:{style:"white-space:nowrap;"}}
													,{field : "NombreTipoCuentaBancariaSocio", title:'UI_TIPO_CUENTA_BANCO', 	width : "100px"}
													,{field : "NombreCuentaBancariaSocio",     title:'UI_CUENTA_BANCARIA', 		width : "100px", attributes:{style:"white-space:nowrap;"}}
													,{field : "NumeroCuentaBancariaSocio",     title:'UI_NUMERO_CUENTA',   		width : "80px"}
													,{field : "NumeroSerieSocio",     		   title:'UI_SERIE_CHEQUERA', 		width : "80px"}
													,{field : "NumeroDocumentoSocio",     	   title:'UI_FOLIO_DOCUMENTO', 		width : "80px"}
									  ]}
									 
									,{field : "NombreEstado", 				title:"UI_ESTADO", 				width: "100px"}
									,{field : "AliasBanco", 				title:"UI_ALIAS_CUENTA_BANCO", 	width: "100px"}
									//Datos cuenta
									,{field : "CuentaDCVOrigen", 			title:"UI_CUENTA_DCV_ORIGEN", 	width: "100px"}
									,{field : "CuentaDCVDestino", 			title:"UI_CUENTA_DCV_DESTINO", 	width: "100px"}
									,{field : "ClaveDCV", 			        title:"UI_CLAVE_DCV", 	        width: "100px"}
								  ],
			rowColorBackground	: function(row) { 
									 var color = "";
									 if (row.IdEstado == 'ANU')
										color = "red";
									 return color;
								  },
			sortable			: true,
			pageable			: false,
			filterable			: true,
			autoBind			: true,
			height				: 320,
			selectable 			: "multiple row",
			gridCreated 		: function(g) { $scope.gridApi = g; }
									
				
		};
		
	
	

	//Editar la cuenta bancaria del socio de negocio
	$scope.editarCuentaSocio = function(datos) 
	{		
		if(datos != undefined){
			if (datos[0].IdEstado === 'ING')
			{
				var idModal = bcsComunes.ventanaAbrir("MedioDePagoSocioModal", datos, true, true );
				var promesa = bcsComunes.ventanaPromesa(idModal);

				promesa.then(function(data){ 
					if (data != undefined){
						//Registrar datos reprocesados para posterior envío a SP
						$scope.dataReprocesada[data.IdNominaTransferenciaDetalle] = 
							{'Id'						: data.IdNominaTransferenciaDetalle,
							 'IdChequera'				: 0,
							 'NumeroChequeDesde'		: 0,
							 'CantidadCheques'			: 0,
							 'IdCuentaBancariaSocio'	: data.IdCuentaBancaria,
							 'IdTarjetaBancariaSocio'	: data.IdTarjetaBancariaSocio,
							 'NumeroSerieSocio'			: '',
							 'NumeroDocumentoSocio'		: 0,
							 'ClaveDCV'					: data.ClaveDCV
							 };
						
						$scope.confBasicaNominaTransferenciaDetalle
						$scope.gridApi.refresh();

						//Actualizar Grilla 
						//Se realiza en el SP al momento de reprocesar
//						var nominasDetalle = dsNominaDetalle;
//						$.each(nominasDetalle, function(key, value){
//							if (Number(value.IdNominaTransferencia) == Number(data.IdNominaTransferencia) ){
//								value.IdCuentaBancariaSocio = data.IdCuentaBancaria;
//								value.NombreCuentaBancariaSocio = data.NombreCuentaBancaria;
//							}
//						});
					}        		
				});
			}
			else 
			{
				bcsComunes.msg("warning","warning",$translate.instant('UI_ESTADO_NO_PERMITE_EDITAR_REGISTRO'));
			}
		}
		else
		{
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));	
		}
		
		return;	
	};
	
	 //Re-generar Nomina de acuerdo a las ediciones realizadas
	$scope.reprocesar = function()
	{ 
		var dataCols = [];
		dataCols.push({IdNominaTransferencia : $scope.oNominaTransferencia.IdNominaTransferencia});

			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_REPROCESARA_NOMINA') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;
					var dataCols = [];
					dataCols = $.map($scope.dataReprocesada, function(el) { return el });

					if (dataCols.length > 0)
					{
						//DATOS SERVICIO
						var datos = {};
						datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
						datos.p_IdUsuario               = $scope.SesionIdUsuario; 
						datos.p_IdModulo                = 'TES';
						datos.p_UsaReglasNegocio        = 'S';

						datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datos.p_RegistrosAReprocesar	= dataCols;

						bcsComunes.log().info(JSON.stringify(datos));
						bcsServiciosTesoreria.ReprocesarNominasTransferencias(datos,success,error).$promise.then(function(respuesta){
							$scope.confBasicaNominaTransferenciaDetalle.dataSource.read();
						});

					}
					else
					{
						bcsComunes.msg("warning","warning",$translate.instant('UI_NO_EXISTEN_REGISTROS_EDITADOS_PARA_REPROCESAR'));
					}
				}
			});
		
		return;
	};
	
	


	
	//-----------------------------------------------------------
	// Anular nomina parcialmente (algunos registros de la nómina)  
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
								"Id"		: value.IdNominaTransferenciaDetalle || 0,
								"Entidad"	: 'TES.NominaTransferenciaDetalle'
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
	
	
	
	//MANEJO DE RESPUETA SERVICIOS
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
			
			//refresca la grilla con los datos editados
			$scope.gridApi.refresh(); //FALTA..revisar si se refresca bien la grilla.
		}
		else
		{
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		}			
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	var error = function(respuesta)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	
});
