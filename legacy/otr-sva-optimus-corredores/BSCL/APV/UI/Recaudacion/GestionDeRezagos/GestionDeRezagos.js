angular.module('ModuloAPV').controller('DetalleGestionRezagosCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {};
		
		var grez = this;
		
		grez.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		
		//Configurar al principio
		
		grez.data = {};

		
		grez.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		grez.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		
		/******************************************************
		 * GRILLAS
		 * ***************************************************/
		grez.gridRecaudacion = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true,
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 15,
	                transport		: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6024",
							dataType	: "json"
					    }
	                },
					//filter: { field: "IdPersonaOrigen", operator: "eq", value: Number(ppr.IdPersona) },
					schema: {
						model: {
							fields: {
								CodigoEstado		: { type: "string"},
								NombreEstado		: { type: "string"},
								FechaDeposito		: { type: "sysdate"},
								FechaIngreso		: { type: "sysdate"},
								FolioRecaudacion    : { type: "string"},
								FolioCartola	    : { type: "string"},
								LineaCartola	    : { type: "string"},
								IdSocioDeNegocioCliente	: { type: "number"},
								IdPersonaCliente	: { type: "number"},
								RutCliente			: { type: "string"},	
								Nombre				: { type: "string"},
								RecMontoDC			: { type: "number"},
								RecMontoAPV			: { type: "number"},
								RecTotal			: { type: "number"},
								RecDiferencia		: { type: "number"},
								DisMontoDC			: { type: "number"},
								DisMontoAPV			: { type: "number"},
								DisTotal			: { type: "number"},
								DisDiferencia		: { type: "number"},
								CodigoBanco			: { type: "string"},
								CuentaCorriente		: { type: "string"},
								Regimen				: { type: "string"},
								IdSocioDeNegocioEmpleador	: { type: "number"},
								IdPersonaEmpleador	: { type: "number"},
								RutEmpleador		: { type: "string"},
								NombreEmpleador		: { type: "string"},
								Observacion  		: { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
			    {field: "FechaDeposito"		,title: $translate.instant('UI_FECHA_DEPOSITO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "FechaIngreso"		,title: $translate.instant('UI_FECHA_INGRESO'), width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "FolioRecaudacion"	,title: $translate.instant('UI_FOLIO_RECAUDACION'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "FolioCartola"		,title: $translate.instant('UI_FOLIO_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "LineaCartola"		,title: $translate.instant('UI_LINEA_CARTOLA'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "RutCliente"		,title: $translate.instant('UI_RUT_CLIENTE'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "Nombre"			,title: $translate.instant('UI_NOMBRE'), width: "250px", align: "left" , sortable : true, editable: false },
				{field: "RutEmpleador"		,title: $translate.instant('UI_RUT_EMPLEADOR'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "NombreEmpleador"	,title: $translate.instant('UI_NOMBRE_EMPLEADOR'), width: "80px", align: "left" , sortable : true, editable: false },
				{field: "RecMontoDC"		,title: $translate.instant('UI_MONTO_DC_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
				{field: "RecMontoAPV"		,title: $translate.instant('UI_MONTO_APV_RECAUDADO'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
				{field: "RecTotal"		    ,title: $translate.instant('UI_MONTO_TOTAL'), width: "80px", align: "left" , sortable : true, editable: false , format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','CLP','',''); }},
				{field: "Observacion"		,title: $translate.instant('UI_OBSERVACION'), width: "200px", align: "left" , sortable : true, editable: false },
				{field: "Regimen"			,title: $translate.instant('UI_REGIMEN'), width: "80px", align: "left" , sortable : true, editable: false },
			],	
			sortable			: false,
			pageable			: true,
			autoBind			: false,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true
		};
			
		grez.buscarRegistros = function(nroPag, filtrosAnteriores){
			var rango = 180;
			//Filtros Busqueda
			
			var filtroBusqueda = new Array();
			
			if (grez.kngFechaDesde != undefined) {
				var resp = util.validarRangoFecha(grez.kngFechaDesde, grez.kngFechaHasta,rango);
				if(resp.estado){
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
				filtroBusqueda.push({ field: "FechaDeposito", operator: "ge", value: util.dateSPdate($scope.grez.kngFechaDesde) });
				
			}
			if (grez.kngFechaHasta != undefined) {
				filtroBusqueda.push({ field: "FechaDeposito", operator: "le", value: util.dateSPdate($scope.grez.kngFechaHasta) });
			}
			
			if(grez.tipoDepositoSeleccionado.IdRecaudacion !== undefined){
				filtroBusqueda.push({ field: "FolioRecaudacion", operator: "eq", value: $scope.grez.tipoDepositoSeleccionado.IdRecaudacion});
			}
			
			if(grez.empleadorSeleccionado.DocumentoIdentidad !== undefined){
				filtroBusqueda.push({ field: "RutEmpleador", operator: "eq", value: $scope.grez.empleadorSeleccionado.DocumentoIdentidad});
			}
			
			if(grez.clienteSeleccionado.DocumentoIdentidad !== undefined){
				filtroBusqueda.push({ field: "RutCliente", operator: "eq", value: $scope.grez.clienteSeleccionado.DocumentoIdentidad});
			}

			if(grez.Observacion !== "" && grez.Observacion !== undefined ){
				filtroBusqueda.push({ field: "tolower(Observacion)", operator: "contains", value: grez.Observacion.toLowerCase() });
			}

			filtroBusqueda.push({ field: "CodigoEstado", operator: "eq", value: "REZ" });
			
			grez.gridRecaudacion.dataSource.filter(filtroBusqueda);
		};

		//ABRI ESTADOS
		grez.abrirEstadosDistr = function(items) {
														//PanelDistribucionEstados
			var IdVentanamodal = bcsComunes.ventanaAbrir("PanelDistribucionEstados", {IdRegistro : items[0].IdRecaudacionCliente } );
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				var x = 0;
			});
		
		};
		//FIN

		grez.DevolucionPagoRezagos = function(items,tipo){
			if( items !== undefined){
				var validacionOK = true;
				if (tipo === "CLIENTE"){
						var validacionCliente = '';
						
						$.each(items,function(key,value){
							if (value.IdPersonaCliente !== value.IdPersonaEmpleador){
								//bcsComunes.msg("error","Error",$translate.instant("UI_PLAZO_INVALIDO"));
								bcsComunes.msg("error","Error","El dinero fue depositado por entidad distinta a la indicada, por lo que no se puede devolver.");
								validacionOK = false;
								return;
							}else if (validacionCliente ===""){
								validacionCliente = value.IdPersonaCliente;
							}else if (validacionCliente !== value.IdPersonaCliente)validacionOK = false;
						});	
				}

				if (tipo === "EMPLEADOR"){
						var validacionEmpleador = '';
						
						$.each(items,function(key,value){
							if (value.IdPersonaCliente === value.IdPersonaEmpleador){
								bcsComunes.msg("error","Error","El dinero fue depositado por entidad distinta a la indicada, por lo que no se puede devolver.");
								validacionOK = false;
								return;
							}else if (validacionEmpleador ==="") {
								validacionEmpleador = value.IdPersonaEmpleador;
							}else if (validacionEmpleador !== value.IdPersonaEmpleador)validacionOK = false;
						});	
				}

				if (items[0].IdPersonaCliente === items[0].IdPersonaEmpleador && tipo === "OTROS"){
					bcsComunes.msg("error","Error","El dinero fue depositado por entidad distinta a la indicada, por lo que no se puede devolver.");
					validacionOK = false;
					return;
				}else if (validacionOK === false) return;
				
				var datosRecaudacion = [];
				$.each(items,function(key,value){
					/*if(items.CodigoEstado === 'REZ'){}*/
					var datos = {
						FolioRecaudacion    		: value.FolioRecaudacion,
						FolioCartola	    		: value.FolioCartola,
						LineaCartola	    		: value.LineaCartola,
						IdRecaudacionCliente		: value.IdRecaudacionCliente,
						IdSocioDeNegocioCliente		: value.IdSocioNegocioCliente,
						IdSocioDeNegocioEmpleador	: value.IdSocioNegocioEmpleador,
						IdPersonaCliente			: value.IdPersonaCliente,
						IdPersonaEmpleador			: value.IdPersonaEmpleador,
						RutCliente					: value.RutCliente,	
						Nombre						: value.Nombre,
						RecMontoDC					: value.RecMontoDC,
						RecMontoAPV					: value.RecMontoAPV,
						RecTotal					: value.RecTotal,
						RecDiferencia				: value.RecDiferencia,
						DisMontoDC					: value.DisMontoDC,
						DisMontoAPV					: value.DisMontoAPV,
						DisTotal					: value.DisTotal,
						DisDiferencia				: value.DisDiferencia,
						CodigoBanco					: value.CodigoBanco,
						CuentaCorriente				: value.CuentaCorriente,
						Regimen						: value.Regimen,
						IdRecaudacionEmpleador		: value.IdRecaudacionEmpleador,
						RutEmpleador				: value.RutEmpleador,
						NombreEmpleador				: value.NombreEmpleador
					}
					datosRecaudacion.push(datos);
				});
				
				if(datosRecaudacion.length > 0){
					var IdVentanamodal = bcsComunes.ventanaAbrir("DevolucionPagoRezagos", {dsDatosRecaudacion : datosRecaudacion, tipoPersona : tipo } );
					var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
					promesa.then(function(data){
						var x = 0;
					});	
				}		
			}
		};
		
	});