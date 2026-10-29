angular.module('ModuloMesa').controller('ReprocesarDatatecCtl', function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa,$interval){
	
	//Obtiene datos desde la ventana principal
	var idVentana = bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var reDatatec = this;
	$scope.data = {};
	$scope.fechaReprocesar = $scope.SesionFechaSistema;
	$scope.estadoReprocesar = "0";
	
	$scope.data.kngFechaOperacion = $scope.SesionFechaSistema;
	$scope.data.oConfiguracionFechaOperacion = {
		max: util.dateInicioDia($scope.SesionFechaSistema)
	};
	
	var operacionesDatatec = {
		type : "odata",
		definition : {
            serverFiltering : true, // Filtrado de datos en el servidor.
            serverPaging    : true, // Paginado de los datos en el servidor.
            serverSorting   : true, // Ordenamiento de los datos en el servidor.
            sort: [
                { field: "FechaOperacionFormato",	dir: "asc" },
                { field: "IdDatatec",				dir: "asc" },
            ],
            pageSize: 100,
            transport: {
            	read: {
            		url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601105",
            		dataType:"json"
            	}
            },
            schema: {
            	model: {
            		fields: {
            			"IdUnicoDatatec"			: { type: "number"}
						, "NumeroRegistro"			: { type: "number"}
						, "NumeroIntentos"			: { type: "number"}
						, "IdEstadoRegistro"		: { type: "string"}
						, "NombreEstado"			: { type: "string"}
						, "GlosaProceso"			: { type: "string"}
						, "IdEmpresa"				: { type: "number"}
						, "IdOperacionSpot"			: { type: "number"}
						, "FechaOperacion"			: { type: "sysdatetime"}
						, "IdCliente"				: { type: "number"}
						, "IdPortafolio"			: { type: "number"}
						, "NombreCliente"			: { type: "string"}
						, "IdInstrumento"			: { type: "number"}
						, "IdTipoOperacion"			: { type: "string"}
						, "NombreTipoOperacion"		: { type: "string"}
						, "IdMonedaPrincipal"		: { type: "string"}
						, "IdMonedaSecundaria"		: { type: "string"}
						, "IdMonedaLocal"			: { type: "string"}
						, "MontoMonedaPrincipal"	: { type: "number"}
						, "TipoCambioCierre"		: { type: "number"}
						, "MontoMonedaSecundaria"	: { type: "number"}
						, "TipoCambioCosto"			: { type: "number"}
						, "IdFormaPagoRecibo"		: { type: "string"}
						, "FechaValutaRecibo"		: { type: "sysdate"}
						, "IdFormaPagoEntrego"		: { type: "string"}
						, "FechaValutaEntrego"		: { type: "sysdate"}
						, "Origen"					: { type: "string"}
						, "IdUsuarioCreador"		: { type: "number"}
						, "FechaCreacion"			: { type: "sysdatetime"}
						, "FechaOperacionFormato"	: { type: "string"}
						, "FechaCreacionFormato"	: { type: "string"}
						, "UsuarioCreador"			: { type: "string"}
						, "IdDatatec"				: { type: "string"}
						, "CodigoPropositoPortafolio": { type: "string"}
					}
            	}
            }
		}
	};
	
	var dsPuntas = new bcs.DataSource(operacionesDatatec);
	$scope.defaultOptions = {
	    dataSource: dsPuntas,
		columns: [
			 //{field: "IdUnicoDatatec", title: "UI_ID_UNICO_DATATEC", width: "100px", sortable : true},
			 {field: "IdDatatec", title: "UI_ID_DATATEC", width: "110px", sortable : true}
			,{field: "NumeroRegistro", title: "UI_NUMERO_REGISTRO", width: "90px", sortable : true}
		    ,{field: "NumeroIntentos", title: "UI_NUMERO_INTENTOS", width: "90px", sortable : true}
		    ,{field: "NombreEstado", title: "UI_ESTADO", width: "70px", sortable : true}
		    ,{field: "GlosaProceso", title: "UI_GLOSA", width: "250px", sortable : false}
		    ,{field: "IdOperacionSpot", title: "UI_FOLIO_SPOT", width: "70px", sortable : true}
		    //,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "100px", sortable : true}
		    ,{field: "FechaOperacionFormato", title: "UI_FECHA_OPERACION", width: "110px", sortable : true}
		    //,{field: "IdPortafolio", title: "UI_PORTAFOLIO", width: "70px", sortable : true}
		    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "170px", sortable : true}
		    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable : true}
		    //,{field: "IdInstrumento", title: "UI_INSTRUMENTO", width: "70px", sortable : true}
		    ,{field: "NombreTipoOperacion", title: "UI_OPERACION_SPOT", width: "90px", sortable : true}
		    ,{field: "IdMonedaPrincipal", title: "UI_MONEDA", width: "50px", sortable : true}
		    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO", width: "70px", sortable : true}
		    ,{field: "TipoCambioCosto", title: "UI_TIPO_DE_CAMBIO", width: "90px", sortable : true}
		    //,{field: "FechaCreacion", title: "UI_FECHA_CREACION", width: "100px", sortable : true}
		    ,{field: "FechaCreacionFormato", title: "UI_FECHA_CREACION", width: "110px", sortable : true}
		    ,{field: "UsuarioCreador", title: "UI_USUARIO_CREA", width: "90px", sortable : true}
		],		
		sortable	: true,
		pageable	: true,
		filterable	: true,
		navigatable: true,
		height		: 400,
		selectable 	: "row",
	    autoBind 	 : false
   	};
	
	var apiGridDatatec = $scope.defaultOptions.dataSource;
	
	$scope.data.buscarDatatec = function(){
		var filters = new Array();
		//filters.push({field : "FechaOperacion", operator : "gte", value: util.dateInicioDia($scope.data.kngFechaOperacion)});
		filters.push({field : "FechaReproceso", operator : "gte", value: util.dateInicioDia($scope.data.kngFechaOperacion)});
		//filters.push({field : "FechaOperacion", operator : "lte", value: util.dateTerminoDia($scope.data.kngFechaOperacion)});
		filters.push({field : "FechaReproceso", operator : "lte", value: util.dateTerminoDia($scope.data.kngFechaOperacion)});
		if($scope.data.cmbbx.IdEstado != "0"){
			filters.push({field : "IdEstadoRegistro", operator : "eq", value: $scope.data.cmbbx.IdEstado});
		}
		$scope.fechaReprocesar = $scope.data.kngFechaOperacion;
		$scope.estadoReprocesar = $scope.data.cmbbx.IdEstado;
		apiGridDatatec.filter(filters)
		apiGridDatatec.read();
	};
	
	$scope.reprocesar = function(){
		var datos = {};
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo = 'MSP';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_Fecha = $scope.fechaReprocesar;
		datos.p_IdEstado = $scope.estadoReprocesar;
		datos.p_Origen = 'REPROCESO';
		
		bcsServiciosMesa.ReprocesoDatatec(datos, function(respuesta){
			if(respuesta.P_OUT_ERRORESCONTADOR === 0){
				if(respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR > 0){
					$.each(respuesta.P_OUT_ERRORCONFIGURACIONDETALLE, function(key,value){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.codigo));
					});
				}
				else {
					/*if(respuesta.P_OUT_DATOSDATATEC.length > 0){
						$.each(respuesta.P_OUT_DATOSDATATEC, function(key,value){
							apiGridDatatec.data().push(value);
						});
					}*/
					
					var filters = new Array();
					//filters.push({field : "FechaOperacion", operator : "gte", value: util.dateInicioDia($scope.fechaReprocesar)});
					filters.push({field : "FechaReproceso", operator : "gte", value: util.dateInicioDia($scope.fechaReprocesar)});
					//filters.push({field : "FechaOperacion", operator : "lte", value: util.dateTerminoDia($scope.fechaReprocesar)});
					filters.push({field : "FechaReproceso", operator : "lte", value: util.dateTerminoDia($scope.fechaReprocesar)});
					if($scope.estadoReprocesar != "0"){
						filters.push({field : "IdEstadoRegistro", operator : "eq", value: $scope.estadoReprocesar});
					}
					apiGridDatatec.filter(filters)
					apiGridDatatec.read();
				}
			}
			else{
				bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CON_ERRORES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID, false);
			}
			
			/*if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
				var vMsg = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.rescateReinversion1N.btnCerrar(true);});
				bcsComunes.ventanaOverlay($scope.winID, false);
	     	}*/
			
		},function(respuesta){
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CON_ERRORES"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
     		bcsComunes.ventanaOverlay($scope.winID, false);
		});
	};
	
});