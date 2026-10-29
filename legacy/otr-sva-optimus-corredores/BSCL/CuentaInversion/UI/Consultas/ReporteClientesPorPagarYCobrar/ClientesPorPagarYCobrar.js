angular.module('ModuloCuentaInversion').controller('ClientesPorPagarYCobrarCtrl',
				function($scope,$translate,$timeout,bcsComunes,$translate,
						bcsServiciosCuentaInversion,bcsServicioPlataforma, $resource)
{

	
	var obj = {};

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);

	var filters = {};
	$scope.datos = {};
	$scope.localFilter = {IdMoneda: 'CLP', TipoSaldo:'TOD'};//TOD;POS;NEG
	
	$scope.kngFechaEfectiva 	= util.dateInicioDia($scope.SesionFechaSistema);
	
	obj.getSchemaReporte = function() {
		return { 
			model: {
				fields : {
					DocumentoIdentidad			: {type : "string"},
					CodigoProposito		 		: {type : "string"},
					Relacion					: {type : "string"},
					NombreCliente				: {type : "string"},
					IdMoneda		 			: {type : "string"},
					SaldoDisponible				: {type : "number"},
					SaldoRetenido				: {type : "number"},
					MoneyMarket					: {type : "number"},
					Garantias					: {type : "number"},
					Simultanea					: {type : "number"},
					Pacto						: {type : "number"},
					NombreAgenteCliente			: {type : "string"},
					FechaUltimaModificacion		: {type : "sysdate"}
				}
			}
		};
	};

	//datasource boton inferior
	var dsBasicCuentasPagaryCobrar = new bcs.DataSource({
		type : "call",
		definition : {
            serverFiltering	: false, // Filtrado de datos en el servidor.
            serverPaging	: false,	// Paginado de los datos en el servidor.
            serverSorting	: false,	// Ordenamiento de los datos en el servidor.
            pageSize		: 200,
            callParameters	: filters,
            callTransaction	: "4290654",
            callResultSet	: "P_OUT_RESULTADO",
			schema : {
				model : {
					fields : {
						DocumentoIdentidad			: {type : "string"},
						CodigoProposito		 		: {type : "string"},
						Relacion					: {type : "string"},
						NombreCliente				: {type : "string"},
						IdMoneda					: {type : "string"},
						Saldo						: {type : "number"},
						SaldoRetenido				: {type : "number"},
						MoneyMarket					: {type : "number"},
						Garantias					: {type : "number"},
						Simultanea					: {type : "number"},
						Pacto						: {type : "number"},
						NombreAgenteCliente			: {type : "string"},
						FechaIngresoRegistro		: {type : "sysdate"}
					}
				}
			}
		}
	
	});
	
	$scope.buscarMovimientos = function() {

		var FecSist = new Date($scope.kngFechaEfectiva);
		var FecSistParserddMMyyyy =convert1(FecSist.toString())
		var FechaSistReverse = FecSistParserddMMyyyy.split("-").reverse().join("-");
		
		var filters	= {};

		filters.p_IdEmpresa 				= $scope.SesionIdEmpresa;
        filters.p_IdUsuario 				= $scope.SesionIdUsuario;
        filters.p_IdModulo 					= 'CTA';
        filters.FechaSistema 				= FechaSistReverse;
        filters.EsBusqueda					= 'S';
		filters.IdMoneda					= $scope.localFilter.IdMoneda || '0';
		filters.TipoSaldo					= $scope.localFilter.TipoSaldo=='0'? 'TOD' : $scope.localFilter.TipoSaldo || 'TOD';
        dsBasicCuentasPagaryCobrar.setCallParameters(filters);
        $scope.apiGridReporte.dataSource.read();
	};
	
	function convert1(str) {
		  var date = new Date(str),
		    mnth = ("0" + (date.getMonth() + 1)).slice(-2),
		    day = ("0" + date.getDate()).slice(-2);
		  return [day, mnth,date.getFullYear()].join("-");
		}
	
	$scope.reportePersonalizado = function(){          	
		
		var entradaPrograma 		= {};					
		entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
		entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
		entradaPrograma.p_IdModulo    		= 'CTA';
		entradaPrograma.p_IdAgenda    		= 0;    
		entradaPrograma.p_IdTipoTarea   	= 'GENERA_EXCEL_CLIENTES_POR_PAGAR_COBRAR';
		entradaPrograma.p_Segundos      	= 0;			
		entradaPrograma.p_CreaParam     	= 'S';
		var FecSist = new Date($scope.kngFechaEfectiva);
		var FecSistParserddMMyyyy =convert1(FecSist.toString())
		//var FecSistParserddMMyyyy = "28-10-2020"
		var FechaSistReverse = FecSistParserddMMyyyy.split("-").reverse().join("-");
		
		entradaPrograma.p_TablaParametros =
			[
			   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
			   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
			   {NombreParametro	: 'IdModulo', 	  		Valor: 'CTA'},	
			   {NombreParametro	: 'FechaSistema', 	  	Valor:FechaSistReverse },
			   {NombreParametro	: 'EsBusqueda', 	  	Valor:'N' },
			   {NombreParametro	: 'IdMoneda', 	  		Valor: $scope.localFilter.IdMoneda || '0' },
			   {NombreParametro	: 'TipoSaldo', 	  		Valor: $scope.localFilter.TipoSaldo=='0'? 'TOD' : $scope.localFilter.TipoSaldo || 'TOD' },
			   
			];
		
		
		bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
						Programa = respuesta.P_OUT_IDPROGRAMA;					
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;								
								
						bcsComunes.ventanaMsg({
							mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
						},$scope.winID);						
					}
					else
					{						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
						},$scope.winID);
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				}
			);
	};
	
	
	
	var configuracionColumnas = function() {
		return { 
			DocumentoIdentidad: {field : "DocumentoIdentidad",					title:"UI_DOCUMENTO_IDENTIDAD",					width: "80px"}
			,CodigoProposito: {field : "CodigoProposito",						title:"UI_PORTAFOLIO",							width: "150px"}
			,Relacion: {field : "Relacion",										title:"UI_REPORTE_PAGAR_COBRAR_RELACION",		width: "70px"}
			,NombreCliente:  {field : "NombreCliente",							title:"UI_NOMBRE_CLIENTE",						width: "250px"}
			,IdMoneda: {field : "IdMoneda",										title:"UI_MONEDA",								width: "70px"}
			,Saldo: {field : "Saldo",											title:"UI_SALDO_DISPONIBLE",					width: "130px"}
			,SaldoRetenido: {field : "SaldoRetenido",							title:"UI_SALDO_RETENIDO",						width: "130px"}
			,MoneyMarket: {field : "MoneyMarket",								title:"UI_REPORTE_PAGAR_COBRAR_MONEYMARKET",	width: "130px"}
			,Garantias: {field : "Garantias",									title:"UI_REPORTE_PAGAR_COBRAR_GARANTIAS",		width: "130px"}
			,Simultanea: {field : "Simultanea",									title:"UI_REPORTE_PAGAR_COBRAR_SIMULTANEAS",	width: "130px"}
			,Pacto: {field : "Pacto",											title:"UI_PACTO",								width: "130px"}
			,NombreAgenteCliente: {field : "NombreAgenteCliente",				title:"UI_AGENTE",								width: "250px"}
			,FechaIngresoRegistro: {field : "FechaIngresoRegistro",				title:"UI_REPORTE_PAGAR_COBRAR_ULTIMAOPERACION",width: "100px"}
		}
	};
	
	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.getConsultaReporte = function() {
		var config = {};
		
		var filters	= {};
		filters.p_IdEmpresa 				= $scope.IdEmpresa;
        filters.p_IdUsuario 				= $scope.SesionIdUsuario;
        filters.p_IdModulo 					= 'CTA';
        
		config = {
				buscar : function(apiGrid,filters) {
					apiGrid.dataSource.filter(filters);
				},
				options : {
					userSettings:true,
					sortable: true,
		      		reorderable: true,
		      		filterable: true,
					groupable: true,
					pageable: true,
					
					reorderable: true,
					allowCopy: true,
					resizable: true,
		      		autoBind: false,
		      		scrollable: true,
		      		height:450,
			
					dataSource: dsBasicCuentasPagaryCobrar,
				
					columns: [
							obj.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
							,obj.getConfiguracionColumna("CodigoProposito",{ groupable: true})
							,obj.getConfiguracionColumna("Relacion",{ groupable: true})
							,obj.getConfiguracionColumna("NombreCliente",{ groupable: true})
							,obj.getConfiguracionColumna("IdMoneda",{ groupable: true})
							,obj.getConfiguracionColumna("Saldo",{ groupable: true})
							,obj.getConfiguracionColumna("SaldoRetenido",{ groupable: true})
							,obj.getConfiguracionColumna("MoneyMarket",{ groupable: true})
							,obj.getConfiguracionColumna("Garantias",{ groupable: true})
							,obj.getConfiguracionColumna("Simultanea",{ groupable: true})
							,obj.getConfiguracionColumna("Pacto",{ groupable: true})
							,obj.getConfiguracionColumna("NombreAgenteCliente",{ groupable: true})
							,obj.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
					]
				}
		};
		return config;
	};
	
	var firstHeightCrear = $('#Win_ReporteClientesPorPagarYCobrar');
        firstHeightCrear.css('height', '610px');
	
	var conf = obj.getConsultaReporte();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarReporte = conf.buscar;
	
});