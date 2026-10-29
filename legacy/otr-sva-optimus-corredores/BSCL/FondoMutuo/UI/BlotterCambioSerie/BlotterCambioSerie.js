angular.module('ModuloFFM').controller('BlotterCambioSerieCtrl',function($rootScope,$scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateCambioSerie = this;             
	$scope.rescateCambioSerie.formData = {};
	$scope.rescateCambioSerie.masInformacion = false;
	$scope.rescateCambioSerie.formData.setInstrumento = "-1";
	$scope.rescateCambioSerie.datosFondo = {};
	$scope.rescateCambioSerie.formData.IngresoConfirmacion = 'I';
	$scope.rescateCambioSerie.formData.AporteRescate = 'A';
	$scope.rescateCambioSerie.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateCambioSerie.formData.DatosAdicionales = "N";
	$scope.rescateCambioSerie.formData.visibleTruco = true;
	$scope.rescateCambioSerie.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.rescateCambioSerie.confGlobal = {};
	$scope.rescateCambioSerie.confGlobal.JerarquiaPerfil = {};
	$scope.rescateCambioSerie.solicitudesFFMM=0;
	$scope.rescateCambioSerie.datasource = {};
	rescateCambioSerie.datosFondo.watchActive=true
	$scope.rescateCambioSerie.habilitaSeleccion=true;
	var i = 0;
	
	var conf = bcsConfiguracionFFM.getConsultarFlujoCustodia();
	var apiGridCustodiaFondosMutuos = dsSaldoCustodia;//conf.options.dataSource;
	var apiGridSolicitudesFondosMutuos=dsSolicitudes;
	$scope.defaultOptions = conf.options;
	$scope.buscarCustodia = conf.buscar;
	var confCategoria = angular.copy(bcsConfiguracionFFM.getDataSourcePerfilCategoriaFondoMutuo());
	/*
	confCategoria.query({filter:{}}).then(function(){
		if(confCategoria.data().length > 0){
			var dataCategoria = confCategoria.data();
			for(var i = 0; i < dataCategoria.length; i++){
				$scope.rescateCambioSerie.confGlobal.JerarquiaPerfil[dataCategoria[i].IdCategoriaInversionista] = dataCategoria[i].Jerarquia;
			}
		}
	});
	*/
	
	//rescateCambioSerie.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.EsFondoNacional='S';
	$scope.EsSwitch='N';
	
	

	//******************************************************************************************************************************/
	//************************************************* Definicion de DataSources **************************************************/
	//******************************************************************************************************************************/
	var filters = new Array();
	var defSaldoCustodia = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
                filters			:  {field: "MontoDisponible", operator: "neq", value: Number('0')},
                
                /*{
                	 {field: "MontoDisponible", operator: "neq", value: Number('0')} ,
                	 {logic: "and",
                		 filter[{ field: "MontoDisponible", operator: "neq", value: undefined }
                			 
                		 ]
                		 
                	 }
                	 
                	
                
                	},
              */
				schema: {
					model: {
						fields: {
							
							IdInstrumento					   : { type: "number"},
		        			IdCliente						   : { type: "number"},
		        			IdPortafolio					   : { type: "number"},
		        			Nemotecnico						   : { type: "string"},
		        			CantidadDisponible				   : { type: "number"},
		        			MontoDisponible					   : { type: "number"},
		        			MontoDisponibleCustodia			   : { type: "number"},
		        			CantidadInvertido				   : { type: "number"},//calculado 
		        			MontoInvertido					   : { type: "number"},//calculado
		        			CantidadRestante				   : { type: "number"},//calculado 
		        			MontoRestante					   : { type: "number"},//calculado		        			
		        			Tipo							   : { type: "string"}
						}
					}
				}
			}	
		};
	
	
	
	var dsSaldoCustodia = new bcs.DataSource(defSaldoCustodia);
	$scope.rescateCambioSerie.datasource.SaldoCustodia = {
			dataSource: dsSaldoCustodia,
			columns: [
					{field: "Nemotecnico",                   	title: $translate.instant('UI_INSTRUMENTO'),                    		width: "100px", align: "left"},
					{field: "CantidadDisponible",				title: $translate.instant('UI_SALDO_CUOTAS'),							width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadDisponible,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
					{field: "MontoDisponible",			    	title: $translate.instant('UI_SALDO_MONTO'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoDisponible,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "', '','')).valor #</label>"},
					
					{field: "CantidadRestante",				    title: $translate.instant('UI_CANTIDAD_DISPONIBLE'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadRestante,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
					{field: "MontoRestante",					title: $translate.instant('UI_MONTO_DISPONIBLE'),						width: "100px", align: "left", template : " <label> #: util.bcs_formato(MontoRestante,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
					{field: "CantidadInvertido",				title: $translate.instant('UI_CANTIDAD_INVERTIDA'),						width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadInvertido,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
					{field: "MontoInvertido",					title: $translate.instant('UI_MONTO_INVERTIDO'),						width: "100px", align: "left", template : " <label> #: util.bcs_formato(MontoInvertido,'FMM','Monto','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"}
				    ],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 357,
			selectable		: "row",
		    userSettings	: true,
			navigatable		: true,
			
			/*
			rowColorBackground	: function(row) {		
				switch(row.get("Tipo")) {
		             case 'COMPLETA':
			            return "green";
			            break;     
		            default:
		            	return "white";
		                break;     
				  }	
				}
		    */
	   rowCellColor	: function(row) {		
				switch(row.get("Tipo")) {
		             case 'COMPLETA':
		            	 return {column: "Nemotecnico", color: "green"};
		                break;
		            default:
		            	 return {column: "Nemotecnico", color: "white"};
		                break;     
				  }	
				}
	};
	
	var defSolicitudes = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                sort 			: false,
				schema: {
					model: {
						fields: {
							
							IdInterno 	          		:	{ type: "number"},
		        			IdCliente 	          		:	{ type: "number"},
					    	IdPortafolio          		:	{ type: "number"},
					    	IdSerieFondo          		:	{ type: "string"},
					    	CalculoPorMontoCuota  		:	{ type: "number"},
					    	Monto 				  		:   { type: "number"},
							NumeroCuotas 	  	  		:	{ type: "number"},
					    	IdMoneda 			  		:	{ type: "string"},
					    	Glosa						: 	{ type: "string"},
					    	IdAutorizadoOrigen			:	{ type: "number"},
					    	Switch                      :   { type: "string"},
		        			
					    	IdSerieFondoDestino      	:	{ type: "string"},
					    	IdEmisorDestino             :   { type: "string"},
					    	MontoDestino 				:   { type: "number"},
							NumeroCuotasDestino 	  	:	{ type: "number"},
							IdMonedaDestino 			:	{ type: "string"}
						}
					}
				}
			}	
		};
	
	var dsSolicitudes = new bcs.DataSource(defSolicitudes);
	$scope.rescateCambioSerie.datasource.Solicitudes = {
			dataSource: dsSolicitudes,
			columns: [
					{field: "IdInterno",				title: $translate.instant('UI_SECUENCIA'),								width: "50px", 	align: "left"},
				    {field: "IdSerieFondo",				title: $translate.instant('UI_INSTRUMENTO'),							width: "100px", align: "left"},
				    {field: "Monto",					title: $translate.instant('UI_MONTO'),									width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
				    {field: "NumeroCuotas",		    	title: $translate.instant('UI_CANTIDAD'),		    					width: "75px", 	align: "left" , template : " <label> #: util.bcs_formato(NumeroCuotas,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
				    {field: "IdMoneda",				   	title: $translate.instant('UI_MONEDA'),			    					width: "75px",	align: "left"},
				    
				    {field: "IdSerieFondoDestino",		title: $translate.instant('UI_SERIE_FONDO_DESTINO'),					width: "100px", align: "left"},
					{field: "IdEmisorDestino",			title: $translate.instant('UI_EMISOR_DESTINO'),							width: "100px", align: "left"},
					{field: "MontoDestino",				title: $translate.instant('UI_MONTO_DESTINO'),							width: "75px", 	align: "left" , template : " <label> #:  (util.bcs_formato(MontoDestino,'FMM','Monto',IdMonedaDestino, '','')).valor #</label>"},
					{field: "NumeroCuotasDestino",		title: $translate.instant('UI_CANTIDAD_DESTINO'),		    			width: "75px", 	align: "left" , template : " <label> #: util.bcs_formato(NumeroCuotasDestino,'FMM','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>"},
					{field: "IdMonedaDestino",			title: $translate.instant('UI_MONEDA_DESTINO'),			    			width: "75px",	align: "left"}
				    ],	
			sortable		: false,
			pageable		: true,
			autoBind		: true,
			height			: 180,
			selectable		: "row",
		    userSettings	: true,
			navigatable		: true
	};
	
	
	
	$scope.rescateCambioSerie.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateCambioSerie.ajustaVentana();
	
	$scope.rescateCambioSerie.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
	
	$scope.rescateCambioSerie.formData.filtrosCuentaInversion = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1}		
	};
	$scope.rescateCambioSerie.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
		
	$scope.rescateCambioSerie.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
	/*
	$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdFondo : {"field" : "IdFondo", "operator": "eq", "value" : $scope.rescateCambioSerie.datosFondo.IdFondo},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
	*/
	 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino = {
				IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
				IdFondo : {"field" : "IdFondo", "operator": "eq", "value" : $scope.rescateCambioSerie.datosFondo.IdFondo},
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
				IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
				BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
				BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' },
				CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
			    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
				
				
				
		};
		
	 
	$scope.rescateCambioSerie.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
	    CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
	    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	    //Origen : {"field" : "Origen", "operator": "eq", "value" : 'CUSTODIA'}
	};
		
	$scope.rescateCambioSerie.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.rescateCambioSerie.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  -1},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	$scope.rescateCambioSerie.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.rescateCambioSerie.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	$scope.rescateCambioSerie.ajustafiltros = function()
    {
		$scope.rescateCambioSerie.formData.FiltrosComboFondo.EsFondoNacional.value= $scope.EsFondoNacional;
		$scope.rescateCambioSerie.formData.FiltrosBusquedaFondo.EsFondoNacional.value = $scope.EsFondoNacional;
		$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.EsFondoNacional.value= $scope.EsFondoNacional;
    }
	
	
	 $scope.rescateCambioSerie.formData.masDatos = function()
	 {
		 if ($scope.rescateCambioSerie.formData.DatosAdicionales === 'S'){
			 $scope.rescateCambioSerie.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.rescateCambioSerie.formData.DatosAdicionales = 'S';
	     }
	 }
	 $scope.rescateCambioSerie.formData.ingresarAporteRescate = function()
     {
		 
		 
		 if ($scope.rescateCambioSerie.datosFondo.IdSerieFondo == $scope.rescateCambioSerie.datosFondoDestino.IdSerieFondo)
		 {
			 bcsComunes.msg("warning","UI_ERROR",$translate.instant("FONDO_INVALIDO"));
				return;
		 };
		 
		 if ($scope.rescateCambioSerie.formData.montoAporte > $scope.rescateCambioSerie.datosFondoDestino.MontoMaximo)
		 {
			 bcsComunes.msg("warning","UI_ERROR",$translate.instant("MONTO_MAXIMO"));
				return;
		 };
		 
		 if ($scope.rescateCambioSerie.formData.montoAporte <	 $scope.rescateCambioSerie.datosFondoDestino.MontoMinimo)
		 {
			 bcsComunes.msg("warning","UI_ERROR",$translate.instant("MONTO_MINIMO"));
				return;
		 };
		 
		
		 
		 
		 
		 i =i+1;
		 //Grilla que muestra lo que se va a generar como orden (solo visual)
		 dsSolicitudes.add({
			 	IdInterno					:   i,
				IdCliente 	          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente,
		    	IdPortafolio          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdPortafolio,
		    	IdSerieFondo          		:	$scope.rescateCambioSerie.datosFondo.IdSerieFondo,
		    	CalculoPorMontoCuota  		:	$scope.rescateCambioSerie.formData.TipoRescate,
		    	Monto 				  		:   $scope.rescateCambioSerie.formData.montoInicial,
				NumeroCuotas 	  	  		:	$scope.rescateCambioSerie.formData.numeroCuotas,
		    	IdMoneda 			  		:	$scope.rescateCambioSerie.datosFondo.IdMoneda,
		    	Glosa						: 	$scope.rescateCambioSerie.formData.comentario,
		    	IdAutorizadoOrigen			:	$scope.rescateCambioSerie.formData.idAutorizadoDarOrdenes || 0,
		    	Switch                      :   $scope.EsSwitch,
    			///DESTINO
		    	IdSerieFondoDestino      	:	$scope.rescateCambioSerie.datosFondoDestino.IdSerieFondo,
		    	IdEmisorDestino             :   '',
		    	MontoDestino 				:   $scope.rescateCambioSerie.formData.montoAporte,
				NumeroCuotasDestino 	  	:	$scope.rescateCambioSerie.formData.numeroCuotasAporte,
				IdMonedaDestino 			:	$scope.rescateCambioSerie.datosFondoDestino.IdMoneda
		 });	
		 apiGridSolicitudesFondosMutuos=dsSolicitudes;
		 $scope.rescateCambioSerie.solicitudesFFMM=dsSolicitudes.data().length;
		 
		 rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoInvertido =  Number(rescateCambioSerie.formData.montoInicial) + Number(rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoInvertido || 0 ) ;
		 rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadInvertido =  Number(rescateCambioSerie.formData.numeroCuotas) + Number(rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadInvertido || 0 ) ;
		 //actualizar restos
		 rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoRestante=rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoDisponible - rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoInvertido;
		 rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadRestante=rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadDisponible - rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadInvertido;
		 
		 if (Math.round(rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoInvertido) == Math.round(rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoDisponible)){
			 	rescateCambioSerie.datasource.SaldoCustodia.seleccion.Tipo="COMPLETA";
			 }else{
				rescateCambioSerie.datasource.SaldoCustodia.seleccion.Tipo='';
			 };
		 
		 $scope.apiGridCustodiaFondosMutuos.refresh();
		 //$scope.rescateCambioSerie.habilitaBtnIngresar=false;
		 $scope.rescateCambioSerie.nemotecnico='';
		 $scope.rescateCambioSerie.nemotecnicoDestino='';
		 $scope.rescateCambioSerie.datasource.SaldoCustodia.seleccion='';
     }
	 
	 
	 $scope.rescateCambioSerie.formData.enviarAporteRescate = function()
     {	
		 $scope.rescateCambioSerie.solicitudesFFMM=dsSolicitudes.data().length;
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 
		var rescates =   [];
		var aportes =   [];
		
		var solicitudes = $scope.apiGridSolicitudesFondosMutuos.dataSource.data();
		
		$.each(solicitudes,function(key,sol){
			
			var resc = {
					IdInterno		      		:	sol.IdInterno,
					IdCliente 	          		:	sol.IdCliente,
			    	IdPortafolio          		:	sol.IdPortafolio,
			    	IdSerieFondo          		:	sol.IdSerieFondo,
			    	CalculoPorMontoCuota  		:	sol.CalculoPorMontoCuota,
			    	Monto 				  		:   sol.Monto,
					NumeroCuotas 	  	  		:	sol.NumeroCuotas,
			    	IdMoneda 			  		:	sol.IdMoneda,
			    	IdMedioDePago				:	'CIN',
			    	SolicitudBatch				:	'N',
			    	OrigenOperacion				: 	'M',
			    	Glosa						: 	sol.Glosa,
			    	IdAutorizadoOrigen			:	sol.IdAutorizadoOrigen,
			    	Switch                      :   sol.Switch,
			    	FolioContrato				:   0
				};
						
			rescates.push(resc);
			
			var apor = {
					IdInterno		      		:	sol.IdInterno,
					IdCliente 	          		:	sol.IdCliente,
			    	IdPortafolio          		:	sol.IdPortafolio,
			    	IdSerieFondo          		:	sol.IdSerieFondoDestino,
			    	IdEmisor                    :   sol.IdEmisorDestino,
			    	Monto 				  		:   sol.MontoDestino,
					NumeroCuotas 	  	  		:	sol.NumeroCuotasDestino,
					IdMoneda 			  		:	sol.IdMonedaDestino,
			    	IdMedioDePago				:	'CIN',
			    	Glosa						: 	sol.Glosa,
			    	Switch                      :   sol.Switch,
			    	FolioContrato				:   0
				};
						
			aportes.push(apor);
			
			
			
		})
		 
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesRescate : rescates,
				 
				 p_SolicitudesAporte : aportes
		 
		 
		 
		 }
	     bcsServicioFondosMutuos.CrearSolicitudCambioSerie(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		 /*
	     		$scope.rescateCambioSerie.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion          	: respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateCambioSerie.formData.decimalesCantidad),
						SaldoCuotas                 	: '',
				    	SaldoMonto                		: '',
				    	TipoRescate                     : $scope.rescateCambioSerie.formData.TipoRescateSeleccionado.NombreConcepto,
				    	MontoAporte  	 				: respuesta.P_OUT_RESULTADO[0].MontoAporte,
				    	NumeroCuotasDestino  	 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotasAporte, $scope.rescateCambioSerie.formData.decimalesCantidadAporte),
				    	EmisorDestino                   : respuesta.P_OUT_RESULTADO[0].IdEmisorAporte
	     		};
	     		*/
	     		$scope.rescateCambioSerie.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateCambioSerie.ajustaVentana();
	     		//var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : false ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta } ,false,false,$scope.winID);
	    		
	     	 }else{
	     		//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : false ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta } ,false,false,$scope.winID);
	     		
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
   		 },function(respuesta){
   			//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   			bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : false ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta } ,false,false,$scope.winID);
   			bcsComunes.ventanaOverlay($scope.winID,false);
   		 });
     }
	 $scope.buscarSaldo = function (){
			
			apiGridCustodiaFondosMutuos.setCallParameters($scope.preparaParametros($scope.data));
			apiGridCustodiaFondosMutuos.read();
			
			
	

		};
		
	 $scope.gridActualizarDisponible = function (){
			for(var x=0; dsSaldoCustodia.data().length - 1 >= x ; x++){
					 //actualizar restos
					dsSaldoCustodia.ds._data[x].MontoRestante=Number(dsSaldoCustodia.ds._data[x].MontoDisponible) - Number((dsSaldoCustodia.ds._data[x].MontoInvertido)?dsSaldoCustodia.ds._data[x].MontoInvertido :0);
					dsSaldoCustodia.ds._data[x].CantidadRestante=Number(dsSaldoCustodia.ds._data[x].CantidadDisponible) - Number((dsSaldoCustodia.ds._data[x].CantidadInvertido)?dsSaldoCustodia.ds._data[x].CantidadInvertido:0);

					 if (Math.round(dsSaldoCustodia.ds._data[x].MontoDestino) == Math.round(dsSaldoCustodia.ds._data[x].MontoDisponible)){
						 	dsSaldoCustodia.ds._data[x].Tipo="COMPLETA";
						 }else{
							 dsSaldoCustodia.ds._data[x].Tipo=Tipo='';
						 };
				
			};
			$scope.apiGridCustodiaFondosMutuos.refresh();
			 
		 };
	 $scope.buscar = function (){
			var EsExtranjero='';
			if($scope.rescateCambioSerie.formData.Switch==true) {EsExtranjero='S';}else{EsExtranjero='N';};
			var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'FMM',
					p_IdPais     		: $rootScope.SesionIdPaisSistema,
					p_Instrumentos 		: [{
						IdInstrumento 	: ($scope.rescateCambioSerie.datosFondoFiltro) ? $scope.rescateCambioSerie.datosFondoFiltro.IdInstrumento : 0
					}],
					p_FechaSaldo 		: util.dateSPdate($scope.rescateCambioSerie.formData.fechaSistema),
					p_DiasProyeccion 	: 0,
					p_EnTransito		: 'N',
					p_TieneCambioFondo  : 'S',
					p_EsExtranjero 		: EsExtranjero,
					p_Carteras : [{
						IdCliente : ($scope.rescateCambioSerie.oPortafolio.seleccionado) ? $scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente : 0,
						IdPortafolio : ($scope.rescateCambioSerie.oPortafolio.seleccionado) ? $scope.rescateCambioSerie.oPortafolio.seleccionado.IdPortafolio : 0,
					}],
				}
				dsSaldoCustodia.removeAll();
    			bcsServicioFondosMutuos.ConsultarFlujoCustodia(datos).$promise.then(function(respuesta){
					if (respuesta.P_OUT_FLUJOFONDOMUTUOTOTALIZADO){						
						var Saldos = Array();							
						var n = respuesta.P_OUT_FLUJOFONDOMUTUOTOTALIZADO.length;
						var RespuestaSaldo = respuesta.P_OUT_FLUJOFONDOMUTUOTOTALIZADO;
						var totalDisponible = 0;
						for(var i=0; i<n ; i++){

							Saldos.push(RespuestaSaldo[i]);
							dsSaldoCustodia.data(Saldos);
						}
					}
					//filters=[];
					//filters.push({ field: "MontoDisponible", operator: "neq", value: Number('0') });
    	    		//dsSaldoCustodia.filter(filters);	
	
					apiGridCustodiaFondosMutuos = dsSaldoCustodia;
					bcsComunes.ventanaOverlay($scope.winID, false);
					 $scope.gridActualizarDisponible();
				},function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);		
				});
			
	

		};
		
		$scope.preparaParametros = function(filtros){
			parametrosConsulta = {};
			parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
			parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo = "FMM";
			parametrosConsulta.p_IdPais   = $rootScope.SesionIdPaisSistema;
			parametrosConsulta.p_Instrumentos 	= [{IdInstrumento : ($scope.rescateCambioSerie.datosFondo) ? $scope.rescateCambioSerie.datosFondo.IdInstrumento : 0}];
			parametrosConsulta.p_FechaSaldo = util.dateSPdate($scope.rescateCambioSerie.formData.fechaSistema);
			parametrosConsulta.p_DiasProyeccion=0;
			parametrosConsulta.p_Carteras = [{
				IdCliente : ($scope.rescateCambioSerie.oPortafolio.seleccionado) ? $scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente : 0,
				IdPortafolio : ($scope.rescateCambioSerie.oPortafolio.seleccionado) ? $scope.rescateCambioSerie.oPortafolio.seleccionado.IdPortafolio : 0
			}]

			
			return parametrosConsulta;
		};
		
		
		$scope.rescateCambioSerie.formData.eliminarAporteRescate = function(Orden){
			var t1 = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_MENSAJE_ELIMINAR_OPERACION'),$scope.winID);
			t1.then(function(respuesta){
				if (respuesta===OPCIONES_BCSALERTA.ACEPTAR)  {
					
					
					for(var x=0; dsSaldoCustodia.data().length >= x ; x++){
						if(dsSaldoCustodia.ds._data[x].Nemotecnico === Orden.IdSerieFondo){
							dsSaldoCustodia.ds._data[x].MontoInvertido = Number(dsSaldoCustodia.ds._data[x].MontoInvertido) - Number(Orden.Monto);
							dsSaldoCustodia.ds._data[x].CantidadInvertido = Number(dsSaldoCustodia.ds._data[x].CantidadInvertido) - Number(Orden.NumeroCuotas);
							 //actualizar restos
							dsSaldoCustodia.ds._data[x].MontoRestante=dsSaldoCustodia.ds._data[x].MontoDisponible - dsSaldoCustodia.ds._data[x].MontoInvertido;
							dsSaldoCustodia.ds._data[x].CantidadRestante=dsSaldoCustodia.ds._data[x].CantidadDisponible - dsSaldoCustodia.ds._data[x].CantidadInvertido;

							 if (Math.round(dsSaldoCustodia.ds._data[x].MontoDestino) == Math.round(dsSaldoCustodia.ds._data[x].MontoDisponible)){
								 	dsSaldoCustodia.ds._data[x].Tipo="COMPLETA";
								 }else{
									 dsSaldoCustodia.ds._data[x].Tipo=Tipo='';
								 };
							break;
						}
					};
					
					

					dsSolicitudes.remove(Orden);
					$scope.rescateCambioSerie.solicitudesFFMM=dsSolicitudes.data().length;
					
					 
			
					$scope.apiGridCustodiaFondosMutuos.refresh();

					 
					 
					 
					 
				}
			})
				
		}
				
			
		$scope.$watch('rescateCambioSerie.datasource.SaldoCustodia.seleccion',function(newval,oldval){
			
			$scope.rescateCambioSerie.formData.TipoRescate='T';
			 if(newval && newval && Number(rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadRestante) >0 ){
				 //$scope.rescateCambioSerie.habilitaBtnIngresar=false;
				
				 if ($scope.rescateCambioSerie.nemotecnico==rescateCambioSerie.datasource.SaldoCustodia.seleccion.Nemotecnico){
					 if (rescateCambioSerie.datosFondo.watchActive==true){rescateCambioSerie.datosFondo.watchActive=false}else{rescateCambioSerie.datosFondo.watchActive=true};
				 };
				 $scope.rescateCambioSerie.nemotecnico=rescateCambioSerie.datasource.SaldoCustodia.seleccion.Nemotecnico;
				 
				 $scope.rescateCambioSerie.formData.numeroCuotas=rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadRestante;
				 $scope.rescateCambioSerie.formData.montoInicial=rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoRestante;
				 
				 //if ($scope.rescateCambioSerie.formData.TipoRescate=='C' || $scope.rescateCambioSerie.formData.TipoRescate=='T'){$scope.rescateCambioSerie.formData.numeroCuotas=rescateCambioSerie.datasource.SaldoCustodia.seleccion.CantidadRestante;};
				 //if ($scope.rescateCambioSerie.formData.TipoRescate=='M'){$scope.rescateCambioSerie.formData.montoInicial=rescateCambioSerie.datasource.SaldoCustodia.seleccion.MontoRestante;};
				 
				 //rescateCambioSerie.datosFondo.IdSerieFondo=rescateCambioSerie.datasource.SaldoCustodia.seleccion.Nemotecnico;
				 $scope.rescateCambioSerie.nemotecnicoDestino='';
				 
			
				 
			 }else{
				 
				 $scope.rescateCambioSerie.nemotecnico='';
				 $scope.rescateCambioSerie.nemotecnicoDestino='';
			 };
			
			//if (rescateCambioSerie.datosFondo.watchActive==true){rescateCambioSerie.datosFondo.watchActive=false}else{rescateCambioSerie.datosFondo.watchActive=true};
		 })
		
		 
	 $scope.$watch('rescateCambioSerie.oPortafolio.idActual',function(newval,oldval){
		 if(newval && newval > 0){
			 var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'FMM',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoProducto	: 'INFM',
					p_IdTipoOperacion	: 'RESCFM',
					P_IdPortafolio		: newval
			 }
			 
			 bcsServicioFondosMutuos.ConsultarRequisitosBasicosCrearSolicitudAporteRescate(datos).$promise.then(function(respuesta){
				 if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					 $scope.rescateCambioSerie.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 
	 $scope.$watch('rescateCambioSerie.formData.Switch', function(newval,oldaval){
		 if($scope.rescateCambioSerie.formData.Switch==true)
			 {
			 	$scope.EsFondoNacional='N';
			 	$scope.EsSwitch='S';
			 }else{
				 $scope.EsFondoNacional='S'; 
				 $scope.EsSwitch='N';
			 };
			 $scope.rescateCambioSerie.ajustaVentana();
			 $scope.rescateCambioSerie.ajustafiltros();
	 });
	 
	 	
	 
	 $scope.$watch('rescateCambioSerie.datosFondo',function(newval,oldval){
		 	
		 if (newval && newval.IdFondo) {
				$scope.rescateCambioSerie.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateCambioSerie.formData.montoInicial,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 
						 if ($scope.rescateCambioSerie.formData.TipoRescate=='M'){$scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;};
						 if ($scope.rescateCambioSerie.formData.TipoRescate=='C' || $scope.rescateCambioSerie.formData.TipoRescate=='T'){$scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;};
						 if ($scope.rescateCambioSerie.nemotecnicoDestino!=''){$scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;};
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						 $scope.rescateCambioSerie.formData.montoInicial = 0;
						 if ($scope.rescateCambioSerie.nemotecnicoDestino!=''){$scope.rescateCambioSerie.formData.montoAporte = 0;};
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 $scope.rescateCambioSerie.formData.montoInicial = 0;
					 if ($scope.rescateCambioSerie.nemotecnicoDestino!=''){$scope.rescateCambioSerie.formData.montoAporte = 0;};
		  		 });
				$scope.rescateCambioSerie.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.IdFondo.value = newval.IdFondo; 
						
				
				//$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.MontoMaximo.value = $scope.rescateCambioSerie.formData.montoAporte;
			    //$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.MontoMinimo.value = $scope.rescateCambioSerie.formData.montoAporte;
				    
				    
		    }
			else {
				$scope.rescateCambioSerie.formData.decimalesCantidad = 0;
				$scope.rescateCambioSerie.formData.numeroCuotas = 0;
				$scope.rescateCambioSerie.formData.montoInicial = 0;
				$scope.rescateCambioSerie.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}

		 $scope.rescateCambioSerie.ajustaVentana();
	 }, true);
	
	 $scope.$watch('rescateCambioSerie.datosFondoDestino',function(newval,oldval){
		 //$scope.rescateCambioSerie.habilitaBtnIngresar=false;
		 if (newval && newval.IdFondo) {
				$scope.rescateCambioSerie.formData.decimalesCantidadAporte = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateCambioSerie.formData.montoInicial,$scope.rescateCambioSerie.formData.numeroCuotasAporte,'RES',newval.IdMoneda,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
						 $scope.rescateCambioSerie.formData.montoAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
		  		 });
				$scope.rescateCambioSerie.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.rescateCambioSerie.formData.decimalesCantidadAporte = 0;
				$scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
				$scope.rescateCambioSerie.formData.montoAporte = 0;
			}
		 
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.IdMonedaAporte', function(newval,oldaval){

		 if(newval){
			 $scope.rescateCambioSerie.formData.decimalesMontoDestino = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateCambioSerie.formData.decimalesMontoDestino = 0;
		 }

		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.montoAporte',function(newval,oldval){
		if(newval && newval != oldval){
			// $scope.rescateCambioSerie.habilitaBtnIngresar=false;
			bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondoDestino,newval,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondoDestino.IdMoneda,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
				 }
			 },function(respuesta){
				 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
				 $scope.rescateCambioSerie.formData.montoAporte = 0;
	  		 });
			//$scope.rescateCambioSerie.habilitaBtnIngresar=true;
		}

	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.montoInicial',function(newval,oldval){
		 if ($scope.rescateCambioSerie.formData.TipoRescate=='M'){
			 if (rescateCambioSerie.datosFondo.watchActive==true){rescateCambioSerie.datosFondo.watchActive=false}else{rescateCambioSerie.datosFondo.watchActive=true};
		 };
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.numeroCuotas',function(newval,oldval){
		 if ($scope.rescateCambioSerie.formData.TipoRescate=='C' ){
			 if (rescateCambioSerie.datosFondo.watchActive==true){rescateCambioSerie.datosFondo.watchActive=false}else{rescateCambioSerie.datosFondo.watchActive=true};
		 };
	 });
	/*
	 $scope.$watch('rescateCambioSerie.formData.montoInicial',function(newval,oldval){
		if ($scope.rescateCambioSerie.formData.TipoRescate=='M'){
			if(newval && newval != oldval){
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondo,newval,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 //$scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 if ($scope.rescateCambioSerie.nemotecnicoDestino!=''){
							 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;	 
						 };
						 
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						// $scope.rescateCambioSerie.formData.montoInicial = 0;
						 $scope.rescateCambioSerie.formData.montoAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 //$scope.rescateCambioSerie.formData.montoInicial = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
		  		 });
			}
		 };
		// rescateCambioSerie.datasource.SaldoCustodia.seleccion='';
		 }, true);
	*/
	 $scope.$watch('rescateCambioSerie.masInformacion',function(newval,oldval){
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 /*
	 $scope.$watch('rescateCambioSerie.formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.rescateCambioSerie.formData.numeroCuotas = $scope.rescateCambioSerie.formData.SaldoCuotas;
			 $scope.rescateCambioSerie.formData.montoInicial = $scope.rescateCambioSerie.formData.SaldoMonto;
		 }else{
			 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
			 $scope.rescateCambioSerie.formData.montoInicial = 0;
		 }

	 });
	 */
	 $scope.$watch('rescateCambioSerie.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateCambioSerie.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateCambioSerie.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });
	 /*
	 $scope.$watch('rescateCambioSerie.formData.numeroCuotas',function(newval,oldval){
		 if ($scope.rescateCambioSerie.formData.TipoRescate=='C'){	 
		 
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondo,$scope.rescateCambioSerie.formData.montoInicial,newval,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						 $scope.rescateCambioSerie.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 $scope.rescateCambioSerie.formData.montoInicial = 0;
		  		 });
		 }
		 };
		 //rescateCambioSerie.datasource.SaldoCustodia.seleccion='';
	 }, true);
	 */
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateCambioSerie.formData.montoInicial = undefined;
				 $scope.rescateCambioSerie.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateCambioSerie.formData.AporteRescate = 'A';
				$scope.rescateCambioSerie.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateCambioSerie.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateCambioSerie.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.rescateCambioSerie.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateCambioSerie.formData.tipoOperacion = 0;
			 $scope.rescateCambioSerie.formData.AporteRescate = 'A';
		 }
		 $scope.rescateCambioSerie.formData.idAutorizadoDarOrdenes = undefined;
		 $scope.rescateCambioSerie.formData.IngresoConfirmacion = 'I';
		 $scope.rescateCambioSerie.formData.TipoRescate = 'M';
		 $scope.rescateCambioSerie.oSocioNegocio.idActual = 0;
		 $scope.rescateCambioSerie.oPortafolio.idActual = 0;
		 $scope.rescateCambioSerie.formData.setFondo = '-1';
		 $scope.rescateCambioSerie.formData.setFondoDestino = '-1';
		 $scope.rescateCambioSerie.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateCambioSerie.formData.montoInicial = undefined;
		 $scope.rescateCambioSerie.formData.montoAporte = undefined;
		 $scope.rescateCambioSerie.formData.numeroCuotas = undefined;
		 $scope.rescateCambioSerie.formData.numeroCuotasAporte = undefined;
		 $scope.rescateCambioSerie.formData.montoComisiones = undefined;
		 $scope.rescateCambioSerie.formData.IdFormaPago = '0';
		 $scope.rescateCambioSerie.formData.comentario = '';
		 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
			//Origen : {"field" : "Origen", "operator": "eq", "value" : 'CUSTODIA'}
		 };

		 $scope.rescateCambioSerie.ajustaVentana();
	 }
	 	 
	 $scope.rescateCambioSerie.formData.resetFoco =  function() {
	 	$scope.rescateCambioSerie.formData.focoVolver = false;
		if($scope.rescateCambioSerie.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateCambioSerie.formData.focoOperacion = true;
			$scope.rescateCambioSerie.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateCambioSerie.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						,IdCliente			: $scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: $scope.rescateCambioSerie.formData.IdMedioDePago ? $scope.rescateCambioSerie.formData.IdMedioDePago : 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };
});