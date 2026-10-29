angular.module('ModuloTesoreria').controller('LibretaBancoCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,bcsServiciosTesoreria)
{	
	
	$scope.resumen = {};
	$scope.resumen.saldoDisponibleInicial  = 0;
	$scope.resumen.saldoDisponible         = 0;
	$scope.resumen.totalIngresos           = 0;
	$scope.resumen.totalEgresos            = 0;
	$scope.resumen.moneda                  = 0;
	
	
	if($scope.winID != undefined){bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);}
	
	//Esta ventana recibe parámetros desde ConsultarSaldoBanco
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana); // falta revisar
	var gridLibretaBanco;
	var obj = {};
	
	//SETEO INICIAL
	$scope.SaldoInicialSimulado  = 0;
	$scope.SaldoFinalSimulado    = 0;	
	$scope.SaldoInicialSimuladoDecimal = 0;
   	$scope.SaldoFinalSimuladoDecimal = 0;
	$scope.kngFechaConsulta 	 = util.dateInicioDia($scope.SesionFechaSistema);
	
	//---------------------------------------
	// COMBOBOX
	//---------------------------------------
	$scope.oConfiguracionMonedaTesoreria={
			tipo 			: "combobox-moneda-tesoreria",
			configuracion 	: {},
			filtros 		: {
								 IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
	
	$scope.oConfiguracionBanco={
		tipo 			: "combobox-banco-cuentas-bancarias-corredora",
		configuracion 	: {},
		filtros 		: {
							  IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },							  
							  IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "" },
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro	: false
	};
				
	$scope.oConfiguracionCuentaCorrienteCorredora={
		tipo 			: "combobox-cuentas-bancarias-corredora",		
		configuracion 	: {},
		filtros 		: {
							  IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
							  IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : "" },							  
							  IdBanco  : { "field"   : "IdBanco","operator": "eq", "value"    : 0 },
						  },
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro	: false	
	};
	
	//---------------------------------------
	// CONFIGURACION DE GRILLA
	//---------------------------------------

	obj.getSchemaLibretaBanco = function() {
		return {
			model : {
				fields : {
							NombreConcepto			 : { type : "string" },
							NombreMedioDePago		 : { type : "string" },
							MontoIngreso			 : { type : "number" },
							MontoEgreso				 : { type : "number" },
							SaldoDisponible			 : { type : "number" },			
							SaldoContable			 : { type : "number" },
							SaldoSimulado			 : { type : "number" },						
							NombreSocio				 : { type : "string" },
							DocumentoIdentidadSocio	 : { type : "string" },
							NombreEstado			 : { type : "string" },
							IdMoneda				 : { type : "string" },
							IdComprobante            : { type : "number" },
							IdEstado				 : { type : "string" },
							FechaAfectaSaldo		 : { type : "sysdate"},
							FechaLiquidacion		 : { type : "sysdate"},						
							NumeroDocumentoCorredora : { type : "number" },
							IdComprobanteFormaDePago : { type : "number" },
							SaldoFinalSimulado 		 : { type : "number" },
							IdConcepto				 : { type : "string" },
							IdPortafolio			 : { type : "number"},
							TipoSocioNegocio		 : { type : "string"},
							CodigoPortafolio		 : { type : "string"},
							Alias					 : { type : "string"},
							Comentario				 : { type : "string"},
							TcCierre				:	{ type : "number" },	
							MontoEquivalente				:	{ type : "number" },	
						}		
				}
			}
	};
	
	obj.getDataSourceLibretaBanco = function() { 
		return {
			type : "call",
			definition : {
				callParameters      : [],
				callTransaction 	: "1775",   // PLUGIN_TES.jar@cl.bcs.tesoreria.ban.ConsultarLibretaBanco
				callResultSet 		: "P_OUT_MOVIMIENTOSLIBRETABANCO",
				serverPaging 		: false,
				serverSorting 		: false,
				serverFiltering 	: false,
				pageSize		    : 200,
				schema 				: obj.getSchemaLibretaBanco(),
			}
  		}
	};

	obj.getDataSourceLibretaBanco2 = function() { 
		return {
			type : "call",
			definition : {
				callParameters      : [],
				callTransaction 	: "1775",   // PLUGIN_TES.jar@cl.bcs.tesoreria.ban.ConsultarLibretaBanco
				callResultSet 		: "P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS",
				serverPaging 		: false,
				serverSorting 		: false,
				serverFiltering 	: false,
				pageSize		    : 200,
				schema 				: obj.getSchemaLibretaBanco(),
			}
  		}
	};
	var dsNewLibretaBanco = new bcs.DataSource({
		type : "call",
		definition : {
			callTransaction         : "1775",
			serverFiltering         : false, // Filtrado de datos en el servidor.
			serverPaging            : false, // Paginado de los datos en el servidor.
			serverSorting           : false, // Ordenamiento de los datos en el servidor.
			callParameters          : [],
			callResultSet           : "P_OUT_MOVIMIENTOSLIBRETABANCO",
			sort                    : { field: "FechaAfectaSaldo", dir: "desc"},
			pageSize                : 200,    
			schema                  : obj.getSchemaLibretaBanco(),
		},
	});
	
	
	var dsNewLibretaBancoCSV = new bcs.DataSource({
		type : "call",
		definition : {
			callTransaction         : "1775",
			serverFiltering         : false, // Filtrado de datos en el servidor.
			serverPaging            : false, // Paginado de los datos en el servidor.
			serverSorting           : false, // Ordenamiento de los datos en el servidor.
			callParameters          : [],
			callResultSet           : "P_OUT_MOVIMIENTOSLIBRETABANCOCSVTODOS",
			sort                    : { field: "FechaAfectaSaldo", dir: "desc"},
			pageSize                : 200,    
			schema                  : obj.getSchemaLibretaBanco(),
		},
	});
	//
	

	obj.getConfiguracionLibretaBanco = function() {
		return {
			height			: 390,
			sortable		: true,
			selectable 		: "multiple row",
			userSettings	: true,
			pageable 		: true,
			filterable		: true,
			allowCopy		: true,
			autoBind		: false,						
			reorderable		: true,
			resizable		: true,
			navigatable		: true,	

			dataSource: dsNewLibretaBanco,
			columns 	: [	 {field : "FechaAfectaSaldo", 		  title:"UI_FECHA_DISPONIBLE", 	    		width: "65px"}
							,{field : "FechaLiquidacion", 		  title:"UI_FECHA_LIQUIDACION", 			width: "65px"}
							,{field : "NombreMedioDePago", 		  title:"UI_MEDIO_DE_PAGO", 				width: "115px"}	
							,{field : "IdComprobanteFormaDePago", title:"UI_ID_COMPROBANTE_FORMA_DE_PAGO",	width: "80"}
							,{field : "NumeroDocumentoCorredora", title:"UI_NUMERO_DOCUMENTO", 				width: "60px"}
							,{field : "MontoEgreso", 			  title:"UI_EGRESO", 						width: "80px",	template : "<label> #:(util.bcs_formato(MontoEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}  
							,{field : "MontoIngreso", 			  title:"UI_INGRESO", 						width: "80px",	template : "<label> #:(util.bcs_formato(MontoIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "SaldoDisponible", 		  title:"UI_SALDO_DISPONIBLE", 				width: "80px",	template : "<label> #:(util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "SaldoSimulado", 		      title:"UI_SALDO_SIMULADO", 	    		width: "80px",	template : "<label> #:(util.bcs_formato(SaldoSimulado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "NombreConcepto", 		  title:"UI_CONCEPTO", 			    		width: "230px"}
							,{field : "NombreSocio", 			  title:"UI_SOCIO_DE_NEGOCIO", 	    		width: "200px"}
							,{field : "DocumentoIdentidadSocio",  title:"UI_DOCUMENTO_IDENTIDAD",   		width: "80px"}						
							,{field : "IdComprobante", 			  title:"UI_SECUENCIA_COMPROBANTE", 		width: "70px"}
							,{field : "NombreEstado", 			  title:"UI_ESTADO",                		width: "70px"}	
							,{field : "IdPortafolio", 			  title:"UI_ID_PORTAFOLIO",               	width: "70px", hidden:true}
							,{field : "TipoSocioNegocio", 	      title:"UI_TIPO_SOCIO_NEGOCIO",           	width: "70px"}
							,{field : "CodigoPortafolio", 	      title:"UI_CODIGO_PORTAFOLIO",           	width: "70px"}
							,{field : "Alias",					  title:"UI_ALIAS",           				width: "70px"}
							,{field : "Comentario",				  title:"UI_COMENTARIO",           			width: "70px"}
	
							,{field : "TcCierre", 			  	  title:"UI_TC_CIERRE", 					width: "80px", template : "<label> #:(util.bcs_formato(TcCierre,'GEN','TipoCambio',IdMoneda,'SPT','')).valor #</label>"}			
							,{field : "MontoEquivalente", 		  title:"UI_MONTO_EQUIVALENTE", 			width: "80px",	template : "<label> #:(util.bcs_formato(MontoEquivalente,'GEN','Monto','CLP','','')).valor #</label>"}		
						  ]		
		}
	}
	
	
	

	$scope.confBasicaLibretaBanco = obj.getConfiguracionLibretaBanco();
	
	
	obj.getConfiguracionLibretaBanco2 = function() {
		return {
			height			: 390,
			sortable		: true,
			selectable 		: "multiple row",
			userSettings	: true,
			pageable 		: true,
			filterable		: true,
			allowCopy		: true,
			autoBind		: false,						
			reorderable		: true,
			resizable		: true,
			navigatable		: true,	

			dataSource: dsNewLibretaBancoCSV,
			columns 	: [	 {field : "FechaAfectaSaldo", 		  title:"UI_FECHA_DISPONIBLE", 	    		width: "65px"}
							,{field : "FechaLiquidacion", 		  title:"UI_FECHA_LIQUIDACION", 			width: "65px"}
							,{field : "NombreMedioDePago", 		  title:"UI_MEDIO_DE_PAGO", 				width: "115px"}	
							,{field : "IdComprobanteFormaDePago", title:"UI_ID_COMPROBANTE_FORMA_DE_PAGO",	width: "80"}
							,{field : "NumeroDocumentoCorredora", title:"UI_NUMERO_DOCUMENTO", 				width: "60px"}
							,{field : "MontoEgreso", 			  title:"UI_EGRESO", 						width: "80px",	template : "<label> #:(util.bcs_formato(MontoEgreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}  
							,{field : "MontoIngreso", 			  title:"UI_INGRESO", 						width: "80px",	template : "<label> #:(util.bcs_formato(MontoIngreso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "SaldoDisponible", 		  title:"UI_SALDO_DISPONIBLE", 				width: "80px",	template : "<label> #:(util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "SaldoSimulado", 		      title:"UI_SALDO_SIMULADO", 	    		width: "80px",	template : "<label> #:(util.bcs_formato(SaldoSimulado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						
							,{field : "NombreConcepto", 		  title:"UI_CONCEPTO", 			    		width: "230px"}
							,{field : "NombreSocio", 			  title:"UI_SOCIO_DE_NEGOCIO", 	    		width: "200px"}
							,{field : "DocumentoIdentidadSocio",  title:"UI_DOCUMENTO_IDENTIDAD",   		width: "80px"}						
							,{field : "IdComprobante", 			  title:"UI_SECUENCIA_COMPROBANTE", 		width: "70px"}
							,{field : "NombreEstado", 			  title:"UI_ESTADO",                		width: "70px"}	
							,{field : "IdPortafolio", 			  title:"UI_ID_PORTAFOLIO",               	width: "70px", hidden:true}
							,{field : "TipoSocioNegocio", 	      title:"UI_TIPO_SOCIO_NEGOCIO",           	width: "70px"}
							,{field : "CodigoPortafolio", 	      title:"UI_CODIGO_PORTAFOLIO",           	width: "70px"}
						    ,{field : "Alias", 	      			  title:"UI_ALIAS",           				width: "70px"}
						    ,{field : "Comentario",				  title:"UI_COMENTARIO",           			width: "70px"}
						    
						    ,{field : "TcCierre", 			  	  title:"UI_TC_CIERRE", 					width: "80px",	template : " <label> #:  (util.bcs_formato(TcCierre,'GEN','Monto',IdMoneda,'SPT','')).valor #</label>" }	
							,{field : "MontoEquivalente", 		  title:"UI_MONTO_EQUIVALENTE", 			width: "80px",	template : "<label> #:(util.bcs_formato(MontoEquivalente,'GEN','Monto','CLP','','')).valor #</label>"}		
						
						  ]		
		}
	}
	
	$scope.confBasicaLibretaBanco2 = obj.getConfiguracionLibretaBanco();

	obj.getDataSourceSaldoCuentaBanco = function() { 
		return {
			type : "call",
			definition : {
				serverFiltering 	: false, // Filtrado de datos en el servidor.
				serverPaging 		: false, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.						
				callParameters      : [],
				callTransaction 	: "314",
				callResultSet 		: "P_OUT_SALDOCUENTABANCO",
				schema : {
					model : {
						fields : {										
							IdBanco					: { type : "number" },
							IdCuentaBanco			: { type : "number" },
							IdMoneda				: { type : "string" },
							NombreBanco				: { type : "string" },
							NombreCuentaBanco		: { type : "string" },
							NombreMoneda			: { type : "string" },

							//Disponible
							SaldoDisponibleInicial	: { type : "number" },
						  	SaldoDisponibleIngreso	: { type : "number" },
						  	SaldoDisponibleEgreso	: { type : "number" },
						  	SaldoDisponible			: { type : "number" },
						}
					}
				}
			}
  		}
	};

	//------------------------------------------------
	// FUNCIONES
	//------------------------------------------------
	
	$scope.buscarSaldoCuentaBanco = function(){

		var filters = {};
	
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		filters.p_IdUsuario         	= $scope.SesionIdUsuario;
		filters.p_IdModulo				= 'TES';
		filters.p_UsaReglasNegocio		= 'S';
		filters.p_FechaDesde			= util.dateSPdate($scope.kngFechaConsulta) || '0001-01-01';
		filters.p_FechaHasta			= util.dateSPdate($scope.kngFechaConsulta) || '0001-01-01';
		filters.p_CuentasAConsultar		= [{ IdCuentaBanco 	: $scope.oConfiguracionCuentaCorrienteCorredora.idActual,
											     IdBanco 		: $scope.oConfiguracionBanco.idActual || null, 
											     IdMoneda 		: $scope.oConfiguracionMonedaTesoreria.idActual || null
										   }];
		filters.p_IncluyeSaldosPendientes = "N";
		filters.p_IncluyeSaldosContables  = "N";
		filters.p_IncluyeSaldosFuturo     = "N";
		
		var dsSaldoCuentaBanco  = obj.getDataSourceSaldoCuentaBanco();		                   
		dsSaldoCuentaBanco.definition.callParameters = filters;		
		dsSaldoCuentaBanco = new bcs.DataSource(dsSaldoCuentaBanco);

		dsSaldoCuentaBanco.fetch().then(function(){
			
			 if (dsSaldoCuentaBanco.data().length < 1 ) 
			 {
				  console.log("SALDO CUENTA BANCO NO EXISTE!");				 
				  $scope.resumen.saldoDisponibleInicial = 0;
				  $scope.resumen.saldoDisponible        = 0;
				  $scope.resumen.moneda                 = "CLP";
				  $scope.resumen.totalIngresos          = 0;
				  $scope.resumen.totalEgresos           = 0;
				  return false;
			 } 
			 else 
			 {
					 $.each(dsSaldoCuentaBanco.data(), function(key, value){
						  //console.log(key+" -> " + value.IdMoneda +" "+ value.IdBanco +" "+ value.IdCuentaBanco);
						  
						  if ( $scope.oConfiguracionCuentaCorrienteCorredora.idActual === value.IdCuentaBanco &&
							   $scope.oConfiguracionBanco.idActual === value.IdBanco &&
							   $scope.oConfiguracionMonedaTesoreria.idActual === value.IdMoneda)
						  {
							  //Asigna totales al resumen
							  $scope.resumen.saldoDisponibleInicial = value.SaldoDisponibleInicial;
							  $scope.resumen.saldoDisponible        = value.SaldoDisponible;  
							  $scope.resumen.moneda                 = value.IdMoneda;							  
							  $scope.resumen.totalIngresos          = value.SaldoDisponibleIngreso;
							  $scope.resumen.totalEgresos           = value.SaldoDisponibleEgreso;
							  return false;
						  }				  
					 });				
			 }
		});
	};

	$scope.buscarLibretaBanco = function(actualizaSaldoCuentaBanco)
	{
		var filters = {};
		
		filters.p_IdEmpresa 					= $scope.SesionIdEmpresa;
		filters.p_IdUsuario         			= $scope.SesionIdUsuario;
		filters.p_IdModulo						= 'TES';
		filters.p_UsaReglasNegocio 			    = 'S';
		filters.p_IdCuentaBancariaCorredora		= $scope.oConfiguracionCuentaCorrienteCorredora.idActual || 0; 
		filters.p_IdBancoCorredora 		  		= $scope.oConfiguracionBanco.idActual || 0;
		filters.p_IdMoneda         		  		= $scope.oConfiguracionMonedaTesoreria.idActual || '';
		filters.p_FechaConsulta 		  		= util.dateSPdate($scope.kngFechaConsulta);  // FechaAfectaSaldo
		filters.p_SaldoInicialSimulado 			= $scope.SaldoInicialSimulado || 0;

		var dsLibretaBanco  = obj.getDataSourceLibretaBanco();
		dsLibretaBanco.definition.callParameters = filters;

		dsNewLibretaBanco.setCallParameters(filters);

		dsLibretaBanco = new bcs.DataSource(dsLibretaBanco);
		$scope.SaldoFinalSimulado = 0;
		$scope.SaldoInicialSimuladoDecimal = (util.bcs_formato(0,'GEN','Monto',$scope.oConfiguracionMonedaTesoreria.idActual,'','')).cantidadDecimales;
   		$scope.SaldoFinalSimuladoDecimal = (util.bcs_formato(0,'GEN','Monto',$scope.oConfiguracionMonedaTesoreria.idActual,'','')).cantidadDecimales;
		
		dsLibretaBanco.fetch().then(function(){
			//Asigna la salida de la consulta(call) al dataSource de la grilla(array)
			$scope.confBasicaLibretaBanco.dataSource.data(dsLibretaBanco.data());
		
			//Actualiza Saldo final Simulado devuelto por el DS a variable para ver en UI
			$.each(dsLibretaBanco.data(),function(key,value) {
				$scope.SaldoFinalSimulado = util.bcs_formato(value.SaldoFinalSimulado,'GEN','Monto',value.IdMoneda,'','').valor;
				return false; 
			});
			
			//Actualiza Saldo Disponible e Inicial para resumen
			if (actualizaSaldoCuentaBanco)
			{	
				$scope.buscarSaldoCuentaBanco();
			}
		},function(){
			bcsComunes.msg("error","error","UI_ERROR_LIBRETA_BANCO");
		})	
	};
	
	//Precarga los filtros con valores enviados desde otras ventanas	
	$scope.cargarFiltros = function(parametros)
	{
		$scope.oConfiguracionBanco.idActual     = parametros[0].IdBanco;
		$scope.oConfiguracionBanco.idPostFiltro = parametros[0].IdBanco;
		$scope.oConfiguracionCuentaCorrienteCorredora.idActual     = parametros[0].IdCuentaBanco;
		$scope.oConfiguracionCuentaCorrienteCorredora.idPostFiltro = parametros[0].IdCuentaBanco;
		$scope.oConfiguracionMonedaTesoreria.idActual = parametros[0].IdMoneda;
		$scope.kngFechaConsulta 	= parametros[0].FechaConsulta;			
	};
	
	$scope.limpiarFiltros = function(){
		
		$scope.kngFechaConsulta	= util.dateInicioDia($scope.SesionFechaSistema);		
		$scope.oConfiguracionBanco.idActual = 0;
		$scope.oConfiguracionCuentaCorrienteCorredora.idActual = 0;
		$scope.oConfiguracionMonedaTesoreria.idActual = 0;		
		$scope.SaldoInicialSimulado = 0;	
		$scope.confBasicaLibretaBanco.dataSource.data([]); // limpia DS libreta banco
		$scope.confBasicaLibretaBanco.dataSource.data = [];
	};
	
	
	$scope.ExcelTodosLosBancos = function(){
		if($scope.oConfiguracionBanco.idActual != false && $scope.oConfiguracionMonedaTesoreria.idActual != false){
			var filters = {};
		
			filters.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			filters.p_IdUsuario         			= $scope.SesionIdUsuario;
			filters.p_IdModulo						= 'TES';
			filters.p_UsaReglasNegocio 			    = 'S';
			filters.p_IdCuentaBancariaCorredora		= -41; 
			filters.p_IdBancoCorredora 		  		= $scope.oConfiguracionBanco.idActual || 0;
			filters.p_IdMoneda         		  		= $scope.oConfiguracionMonedaTesoreria.idActual || '';
			filters.p_FechaConsulta 		  		= util.dateSPdate($scope.kngFechaConsulta);  // FechaAfectaSaldo
			filters.p_SaldoInicialSimulado 			= $scope.SaldoInicialSimulado || 0;
	
			var dsLibretaBanco  = obj.getDataSourceLibretaBanco2();
			dsLibretaBanco.definition.callParameters = filters;
	
			dsNewLibretaBancoCSV.setCallParameters(filters);
	
			dsLibretaBanco = new bcs.DataSource(dsLibretaBanco);
			
			console.log(dsLibretaBanco);
			
			dsLibretaBanco.fetch().then(function(){
				
				let data = dsLibretaBanco.data();
				var csvContent = '';
			  	let dataString ="Alias;CodigoPortafolio;DocumentoIdentidadSocio;FechaAfectaSaldo;FechaLiquidacion;IdComprobante;IdComprobanteFormaDePago;IdConcepto;IdEstado;IdMoneda;IdPortafolio;MontoEgreso;MontoIngreso;NombreConcepto;NombreEstado;NombreMedioDePago;NombreSocio;NumeroDocumentoCorredora;SaldoContable;SaldoDisponible;SaldoSimulado;TipoSocioNegocio;Comentario;TcCierre;MontoEquivalente;"+ '\n';
				
				
				
				for(let i=0 ; i< data.length; i++){
					let stringporlinea ="";
					let fechaLiquidacionParsed =parseDate(data[i].FechaLiquidacion);
 					let FechaAfectaSaldoParsed =parseDate(data[i].FechaAfectaSaldo);
					
					                    stringporlinea += String(data[i].Alias == null ? '' : data[i].Alias).concat(';');
					stringporlinea += String(data[i].CodigoPortafolio== null ? '' :data[i].CodigoPortafolio).concat(';');
					stringporlinea += String(data[i].DocumentoIdentidadSocio== null ? '' :data[i].DocumentoIdentidadSocio).concat(';');
					stringporlinea += fechaLiquidacionParsed.concat(';');
					stringporlinea += FechaAfectaSaldoParsed.concat(';');
					stringporlinea += String(data[i].IdComprobante== null ? '' :data[i].IdComprobante).concat(';');
					stringporlinea += String(data[i].IdComprobanteFormaDePago== null ? '' :data[i].IdComprobanteFormaDePago).concat(';');
					stringporlinea += String(data[i].IdConcepto== null ? '' :data[i].IdConcepto).concat(';');
					stringporlinea += String(data[i].IdEstado== null ? '' :data[i].IdEstado).concat(';');
					stringporlinea += String(data[i].IdMoneda== null ? '' :data[i].IdMoneda).concat(';');
					stringporlinea += String(data[i].IdPortafolio== null ? '' :data[i].IdPortafolio).concat(';');
					stringporlinea += String(data[i].MontoEgreso== null ? '' :data[i].MontoEgreso).concat(';');
					stringporlinea += String(data[i].MontoIngreso== null ? '' :data[i].MontoIngreso).concat(';');
					stringporlinea += String(data[i].NombreConcepto== null ? '' :data[i].NombreConcepto).concat(';');
					stringporlinea += String(data[i].NombreEstado== null ? '' :data[i].NombreEstado).concat(';');
					stringporlinea += String(data[i].NombreMedioDePago== null ? '' :data[i].NombreMedioDePago).concat(';');
					stringporlinea += String(data[i].NombreSocio== null ? '' :data[i].NombreSocio).concat(';');
					stringporlinea += String(data[i].NumeroDocumentoCorredora== null ? '' :data[i].NumeroDocumentoCorredora).concat(';');
					stringporlinea += String(data[i].SaldoContable== null ? '' :data[i].SaldoContable).concat(';');
					stringporlinea += String(data[i].SaldoDisponible== null ? '' :data[i].SaldoDisponible).concat(';');
					stringporlinea += String(data[i].SaldoSimulado== null ? '' :data[i].SaldoSimulado).concat(';');
					stringporlinea += String(data[i].TipoSocioNegocio== null ? '' :data[i].TipoSocioNegocio).concat(';');
					stringporlinea += String(data[i].Comentario== null ? '' :data[i].Comentario).concat(';');					
					stringporlinea += String(data[i].TcCierre == null ? '' : data[i].TcCierre).replace('.', ',').concat(';'); 
					stringporlinea += String(data[i].MontoEquivalente== null ? '' :data[i].MontoEquivalente).concat(';');
					stringporlinea += '\n' ; 
				  dataString += stringporlinea;

				
				}
				
				let nombre = "Libreta Banco "+ parseDate(new Date()) + ".csv";
				downloadBlob(dataString, nombre, 'text/csv;charset=utf-8;')
	
			},function(){
				bcsComunes.msg("error","error","UI_ERROR_LIBRETA_BANCO");
			});
		}else{
			bcsComunes.msg("error","error","Falta seleccionar banco o moneda");
	
		}
		
	};

	$scope.exportarExcel = function(){
		bcsComunes.ventanaAutoOverlay(idVentana,true);
		if($scope.oConfiguracionBanco.idActual != false && $scope.oConfiguracionMonedaTesoreria.idActual != false){


			var filters = {};
		
			filters.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			filters.p_IdUsuario         			= $scope.SesionIdUsuario;
			filters.p_IdModulo						= 'TES';
			filters.p_UsaReglasNegocio 			    = 'S';
			filters.p_IdCuentaBancariaCorredora		= $scope.oConfiguracionCuentaCorrienteCorredora.seleccionado.IdCuentaBanco; 
			filters.p_IdBancoCorredora 		  		= $scope.oConfiguracionBanco.idActual || 0;
			filters.p_IdMoneda         		  		= $scope.oConfiguracionMonedaTesoreria.idActual || '';
			filters.p_FechaConsulta 		  		= util.dateSPdate($scope.kngFechaConsulta);  
			filters.p_SaldoInicialSimulado 			= $scope.SaldoInicialSimulado || 0;
	
			var dsLibretaBanco  = obj.getDataSourceLibretaBanco();
			dsLibretaBanco.definition.callParameters = filters;
	
			dsLibretaBanco = new bcs.DataSource(dsLibretaBanco);
			filters = {};
			filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
			filters.p_IdUsuario         	= $scope.SesionIdUsuario;
			filters.p_IdModulo				= 'TES';
			filters.p_UsaReglasNegocio		= 'S';
			filters.p_FechaDesde			= util.dateSPdate($scope.kngFechaConsulta) || '0001-01-01';
			filters.p_FechaHasta			= util.dateSPdate($scope.kngFechaConsulta) || '0001-01-01';
			filters.p_CuentasAConsultar		= [{ IdCuentaBanco 	: $scope.oConfiguracionCuentaCorrienteCorredora.idActual,
													 IdBanco 		: $scope.oConfiguracionBanco.idActual || null, 
													 IdMoneda 		: $scope.oConfiguracionMonedaTesoreria.idActual || null
											   }];
			filters.p_IncluyeSaldosPendientes = "N";
			filters.p_IncluyeSaldosContables  = "N";
			filters.p_IncluyeSaldosFuturo     = "N";

			var dsLibretaBancoResumen  = obj.getDataSourceSaldoCuentaBanco();
			dsLibretaBancoResumen.definition.callParameters = filters;
	
			dsLibretaBancoResumen = new bcs.DataSource(dsLibretaBancoResumen);
			var newdata = [];
			dsLibretaBancoResumen.fetch().then(function(){
				var data =dsLibretaBancoResumen.data();
				
				$.each(data, function(key, value){
					//console.log(key+" -> " + value.IdMoneda +" "+ value.IdBanco +" "+ value.IdCuentaBanco);
					
					if ( $scope.oConfiguracionCuentaCorrienteCorredora.idActual === value.IdCuentaBanco &&
						 $scope.oConfiguracionBanco.idActual === value.IdBanco &&
						 $scope.oConfiguracionMonedaTesoreria.idActual === value.IdMoneda)
					{
						
						newdata = [{
							"IdComprobante": null,
							"IdComprobanteFormaDePago": null,
							"NombreSocio": null,
							"DocumentoIdentidadSocio": null,
							"IdConcepto": null,
							"NombreConcepto": "RESUMEN:   ",
							"NombreMedioDePago": "Saldo Inicial     :",
							"IdMoneda": value.SaldoDisponibleInicial,
							"MontoIngreso": null,
							"MontoEgreso":"Total Ingresos      :",
							"SaldoDisponible": value.SaldoDisponibleIngreso,
							"SaldoContable": null,
							"SaldoSimulado":"Total Egresos     :",
							"SaldoFinalSimulado": value.SaldoDisponibleEgreso,
							"FechaAfectaSaldo": null,
							"FechaLiquidacion": "Saldo Disponible     :",
							"IdEstado": value.SaldoDisponible,
							"NombreEstado": null,
							"NumeroDocumentoCorredora": null,
							"TipoSocioNegocio": null,
							"IdPortafolio": null,
							"CodigoPortafolio": null,
							"Alias": null,
							"Comentario": null,
							"TcCierre": null,
							"MontoEquivalente": null
						}];
						
						dsLibretaBanco.fetch().then(function(){
							if(!dsLibretaBanco.data() || (dsLibretaBanco.data()).length === 0){
								bcsComunes.msg("warning","warning","No hay datos")
								return false;
							}else{

							let columns = generateColumns(dsLibretaBanco.dataColumns(["IdComprobante","IdComprobanteFormaDePago","NombreSocio","DocumentoIdentidadSocio",
							"IdConcepto","NombreConcepto","NombreMedioDePago","IdMoneda","MontoIngreso","MontoEgreso","SaldoDisponible","SaldoContable","SaldoSimulado",
							"SaldoFinalSimulado","FechaAfectaSaldo","FechaLiquidacion","IdEstado","NombreEstado","NumeroDocumentoCorredora","TipoSocioNegocio","IdPortafolio",
							"CodigoPortafolio","Alias","Comentario","TcCierre","MontoEquivalente"],dsLibretaBanco.data()));
							dsLibretaBanco.pushCreate(newdata);
							const nombreArchivo = "Libreta Banco "+ parseDate(new Date());
							dsLibretaBanco.exportToExcel(columns,'',nombreArchivo);
							}
			
				
						},function(){
							bcsComunes.msg("error","error","UI_ERROR_LIBRETA_BANCO");
						});
						

						return false;
					}				  
			   });
			});
			
		bcsComunes.ventanaAutoOverlay(idVentana,false);	

			
		}else{
			bcsComunes.ventanaAutoOverlay(idVentana,false);
			bcsComunes.msg("error","error","Falta seleccionar banco o moneda");
	
		}
		bcsComunes.ventanaAutoOverlay(idVentana,false);
		
	};
	

	function generateColumns(data) {

		return Object.keys(data[0]).map(key => ({
			field: key,
			title: key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase()) // Formato: camelCase a Título
		}));
	}

	function downloadBlob(content, filename, contentType) {
	  // Create a blob
	  var blob = new Blob([content], { type: contentType });
	  var url = URL.createObjectURL(blob);
	
	  // Create a link to download it
	  var pom = document.createElement('a');
	  pom.href = url;
	  pom.setAttribute('download', filename);
	  pom.click();
	}
	
	function parseDate(dateIn){
		if(dateIn != undefined && dateIn != null && dateIn != '' ){
			let date =  dateIn.getDate();
		let month = dateIn.getMonth()+1;
		let year = dateIn.getFullYear();
		
		if(month < 10 ){
			let month2 = "0"+month;
			month = month2;
		}
		if(date < 10 ){
			let date2 = "0"+date;
			date = date2;
		}
		let finalDate = date+'/'+month+'/'+year;
		return finalDate;
		}
	}
	$timeout(function(){		
		if (parametros[0] !== undefined)
		{
			$scope.cargarFiltros(parametros);
			$scope.buscarSaldoCuentaBanco();
			$scope.buscarLibretaBanco(false);
		}
	},1000);
	
	var firstHeightCrear = $('#Win_LibretaBanco');
        firstHeightCrear.css('height', '680px');
		firstHeightCrear.css('width', '1000px');
});