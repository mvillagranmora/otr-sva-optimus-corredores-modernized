
angular.module('CartolaPDF', ['pascalprecht.translate','ngResource', 'ngSanitize']);

angular.module('CartolaPDF').config(['$locationProvider',function($locationProvider){    
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
}]);

angular.module('CartolaPDF').config(['$httpProvider',function($httpProvider) {
    $httpProvider.interceptors.push('HTTPInterceptor');
}])
.factory('HTTPInterceptor',['$q','$location',function($q, $location) {
    return {
        responseError: function(response){
            return $q.reject(response);
        },
        request: function(config){
            config.headers['X-SESSION-TOKEN'] = $location.search().token;  
            return config;
        }            
    };
}]);

angular.module('CartolaPDF').config(['$translateProvider', function($translateProvider) {        
    $translateProvider.translations('bcs',{"UI_CLIENTE" : "Cliente"});
    $translateProvider.translations('bcs',{"UI_RUT" : "Rut"});
    $translateProvider.translations('bcs',{"UI_FECHA" : "Fecha"});
    $translateProvider.translations('bcs',{"UI_MONEDA" : "Moneda"});
    $translateProvider.translations('bcs',{"UI_EJECUTIVO" : "Ejecutivo"});
    $translateProvider.translations('bcs',{"UI_TELEFONO" : "Telefono"});
    $translateProvider.translations('bcs',{"UI_PORTAFOLIO" : "Portafolio"});
    $translateProvider.translations('bcs',{"UI_LEYENDA_FLUJO_PATRIMONIAL" : "MTD*: Desde Inicio de Mes YTD*:Desde Inicio Año ITD*: Desde Apertura Cuenta"});
    $translateProvider.preferredLanguage('bcs');
}]);


angular.module('CartolaPDF').controller(
		'CartolaOnlineController',
		[ '$scope', '$http', '$translate', '$timeout','$resource','$location','bcsServiciosCartola',
				
				 CartolaOnlineController ]);


angular.module('CartolaPDF').factory('bcsServiciosCartola',['$resource',function($resource) {
	//llamadas para generar cartola copiadas desde modulo normativa
	urlGenerarCartola 	                        =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=21021";
    urlDataCartola		                        =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=601174";
    urlInformacionCliente	                    =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=601199";
	//______________________________________________________________
return $resource("", {},
  {
	//llamadas para generar cartola copiadas desde modulo normativa
    'GenerarCartola'					    :   { method: "POST", url: urlGenerarCartola},
    'ObtieneDataCartola'					:   { method: "POST", url: urlDataCartola},
    'InformacionCliente'					:   { method: "POST", url: urlInformacionCliente},
	//______________________________________________________________
   });
}]);

function CartolaOnlineController($scope, $http, $translate, $timeout,$resource,$location, bcsServiciosCartola) {
	
	

	var respuesta = {};
	var datos = {};
	var params = $location.search();
	
	console.log($translate.instant('UI_AGENTE'));
	var logo = "";
	
	$scope.vistaHtmlCartolaConsolidada = "../../../Normativa/UI/Cartolas/CartolaOnline/CartolaOnline.html";
	//Abrir ventana de cartola Online
		var infoCliente = [];
		params.IdPortafolio.split(',').forEach(item => {
			infoCliente.push({"IdCliente": params.p_IdCliente, "IdPortafolio": item})
		});
		
		var fh = params.p_FechaHasta;
		var fd = params.p_FechaDesde;
		
		var datos = {};

		datos.p_IdEmpresa 		= params.IdEmpresa;
		datos.p_IdModulo 		= params.p_IdModulo;
		datos.p_IdUsuario 		= params.p_IdUsuario;
		datos.p_FechaDesde 		= fd;
		datos.p_FechaHasta 		= fh;
		datos.p_Datos			= infoCliente;
		
		bcsServiciosCartola.ObtieneDataCartola(datos,
				function(response){		
						if (response.P_OUT_SALIDA != null){

							$scope.Telefono			    		  = response.P_OUT_SALIDA.datosSocioNegocio[0].Telefono;
							$scope.NombreEjecutivo      		  = response.P_OUT_SALIDA.datosSocioNegocio[0].NombreEjecutivo;
							$scope.NombreSocioNegocio   	      = response.P_OUT_SALIDA.datosSocioNegocio[0].NombreSocioNegocio;
							$scope.CodigoPortafolio     		  = response.P_OUT_SALIDA.datosSocioNegocio[0].CodigoPortafolio;
							$scope.DocumentoIdentidad   	      = response.P_OUT_SALIDA.datosSocioNegocio[0].DocumentosIdentidad;
							$scope.MonedaCartola        		  = response.P_OUT_SALIDA.datosSocioNegocio[0].MonedaCartola;
							$scope.FechaActual 					  = datos.p_FechaHasta;

					 }
		});
		
		window.onload = $scope.solicitarCartolaOnline = function() {
			
			var promisePrincipal = new Promise((resolve, reject)=>{
				
		  
				bcsServiciosCartola.ObtieneDataCartola(datos,
						function(response){		
							if (response.P_OUT_SALIDA != null){

								$scope.FechaInicioFlujoPatrimonial    = response.P_OUT_SALIDA.flujoPatrimonial[2].fechaInicioCuenta;		
								respuesta.MovimientosTitulos          = response.P_OUT_SALIDA.movimientosTitulos;
								respuesta.MovimientosCaja             = response.P_OUT_SALIDA.movimientosCaja;
								respuesta.CarteraInversiones          = response.P_OUT_SALIDA.carteraInversiones;
								respuesta.ComposicionCartera          = response.P_OUT_SALIDA.composicionCartera;
								respuesta.FlujoPatrimonial            = response.P_OUT_SALIDA.flujoPatrimonial;
								respuesta.DistribucionPorClaseActivo  = response.P_OUT_SALIDA.distribucionPorClaseActivo;
								respuesta.ExposicionPorMoneda         = response.P_OUT_SALIDA.exposicionPorMoneda;	
								respuesta.LogoCorredora				  = response.P_OUT_SALIDA.datosConfCorredora[0].logoCorredora;
								//Flujo Patrimonial 
								
								//$scope.habilitaLogo = 'data:image/jpeg;base64,'+respuesta.LogoCorredora;
								document.getElementById("logoCartola").innerHTML = '<img style="width: 30%" src="data:image/jpeg;base64,' + respuesta.LogoCorredora + '"/>';
								
								//FLUJO PATRIMONIAL
								$("#flujoPatrimonial").kendoGrid({
									 dataBound: function(e){
					
										 var items = e.sender.items();
										 items.each(function(idx, item){
										   if((e.sender.dataItem(item).tipoRegistro == "detalle")){
											   $(item).addClass('rowDetalle');
										   }else if((e.sender.dataItem(item).tipoRegistro == "blancoN")){
											   $(item).addClass('tituloPrincipalGrillaCartola');
										   }else if((e.sender.dataItem(item).tipoRegistro == "blanco")){
											   $(item).addClass('rowDetalle');
										   }
										 });
									   },	
									 dataSource: respuesta.FlujoPatrimonial,
									 columns: [
														{ field: "titulo", title: "Flujo Patrimonial",	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaTextoCartola"}},
														{ field: "MTD", title: "MTD*"              ,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && mtd!='undefined'){##=(util.bcs_formato(mtd,'GEN','Monto', 'CLP', '','')).valor##} else {##=''##} #</label>"},
														{ field: "YTD", title: "YTD*"              ,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && ytd!='undefined'){##=(util.bcs_formato(ytd,'GEN','Monto', 'CLP', '','')).valor##} else {} #</label>"},
														{ field: "ITD", title: "ITD " + $scope.FechaInicioFlujoPatrimonial,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && itd!='undefined'){##=(util.bcs_formato(itd,'GEN','Monto', 'CLP', '','')).valor##} else {} #</label>"}
												]  	
									  });
								
								  //Exposicion por Monedas
								 $("#graficoexposicionpormonedas").kendoChart({
									 title: {
										text: "Exposixión por monedas",
										position: "top",
										align: "center",
										color : "white",
										background:"#808080",
										font: "13px"
									 },
									 legend: {
										 position: "custom",
										 offsetX: 10,
										 offsetY: 260
									 },
									 dataSource: {
										 data: respuesta.ExposicionPorMoneda
									 },
									 series: [{
										 type: "pie",
										 field: "porcentaje",
										 categoryField: "moneda",
									 }],
									 seriesColors: ["#f9a940", "#808080"],
									 tooltip: {
										 visible: true,
										 template: "${ category } - ${ value }%"
									 }
								 });
					
								 $("#graficoporclasedeactivos").kendoChart({
										title: {
											text: "Distribución por Clase de Activos",
											position: "top",
											align: "center",
											color : "white",
											background:"#808080",
											font: "13px"
										},
										dataSource: {
											data: respuesta.DistribucionPorClaseActivo	            
										},
										series: [{
										   field: "porcentaje",
										   color: "#f9a940"
										}],
										categoryAxis:[{
											labels:{
												rotation: 90,
												font: "10px"
											},
											field: "titulo"
										}]
								});

								$("#composicionCartera").kendoGrid({

									 dataBound: function(e){
										 var items = e.sender.items();
										 items.each(function(idx, item){
										   if((e.sender.dataItem(item).tipoRegistro == "totalMercado")){
											   $(item).addClass('rowTotalMercadoNegra');
										   }else if((e.sender.dataItem(item).tipoRegistro == "totalSubMercado")){
											   $(item).addClass('rowDetalle');
										   }else if((e.sender.dataItem(item).tipoRegistro == "totalPatrimonio")){
											   $(item).addClass('tituloPrincipalGrillaCartola');
										   }else if((e.sender.dataItem(item).tipoRegistro == "blancoN")){
											   $(item).addClass('tituloPrincipalGrillaCartola');
										   }else if((e.sender.dataItem(item).tipoRegistro == "blanco")){
											   $(item).addClass('rowDetalle');
										   }


										 });
									   },	
										dataSource: respuesta.ComposicionCartera,
										columns: [
											{ field: "titulo", title: "Composición de la Cartera", width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaTextoCartola"}},
											{ field: "montoTotal", title: "Monto",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(orden != -1 && montoTotal!='undefined'){##=(util.bcs_formato(montoTotal,'GEN','Monto', 'CLP', '','')).valor##} else {} #</label>"},
											{ field: "porcentajeTotal", title: "%",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(orden != -1 && porcentajeTotal!='undefined'){##=porcentajeTotal##} else {} #</label>"}

										]
							  });
							 
							 // Calculo de hojas 
								
								/*funcion que devuelve el numero de hojas por cada seccion, si la division
								  queda el resto se suma una proxima hoja y retorna el numero de hojas */
								
								var hojas_CarteraInversiones = calculodehojas(respuesta.CarteraInversiones.length);
								var hojas_MovimentosTitulos  = calculodehojas(respuesta.MovimientosTitulos.length);
								var hojas_MovimentoCaja      = calculodehojas(respuesta.MovimientosCaja.length);

								function calculodehojas(seccion){
									 var numerodehojas = 0;
									 var calulodehojas = seccion/35;
										if(seccion != 0 || seccion != null){
											if (calulodehojas - Math.floor(calulodehojas) == 0) {
												numerodehojas = calulodehojas;
											} else {
												numerodehojas = Math.floor(calulodehojas)+1;
											}
										return numerodehojas;
									   }
								}

							   
			       
							   function dibujarCarteraInversiones(numerohojas, arregloinicial, logoCorredora){
								   console.log(numerohojas);
								   if(numerohojas == 0 ){
									numerohojas = 1;
								   }
								   var arregloporhojas= {};
								   var num = 0;
								   var num2 = 21;
								   var ciclo1= 0;
								   for(ciclo1; ciclo1 <= numerohojas; ciclo1++) {//numero de hojas 
									   for(num; num < num2; num++) {
											if(num < arregloinicial.length){
												 arregloporhojas[num] = arregloinicial[num];
											}else{
												 break;
												 
											}
									   }
									   num2 = num2+21;
									   if(Object.values(arregloporhojas).length > 0){
										   CarteraInversiones(Object.values(arregloporhojas), ciclo1, logoCorredora);										      
									   }
									   arregloporhojas = {};
									   if(num > arregloinicial.length){break;}

								 }
							   }

							  
								function dibujarmovimientostitulos(numerohojas, arregloinicial){
								  if(numerohojas == 0 ){
									numerohojas = 1;
								   }
								   var arregloporhojas= {};
								   var num = 0;
								   var num2 = 21;
								   var ciclo1= 0;
								   for(ciclo1; ciclo1 < numerohojas; ciclo1++) {
									   for(num; num < num2; num++) {

										if(num < arregloinicial.length){
											 arregloporhojas[num] = arregloinicial[num];
											}
											else{
												break;
											}
										 }

									   num2 = num2+21;
									   if(Object.values(arregloporhojas).length > 0){
										   movimientostitulos(Object.values(arregloporhojas), ciclo1);
									   }
									   arregloporhojas = {};
									   if(num > arregloinicial.length){break;}
									 }
							   }
								
								
								function dibujarmovimientosCaja(numerohojas, arregloinicial){
								  if(numerohojas == 0 ){
									numerohojas = 1;
								   }
								   var arregloporhojas= {};
								   var num = 0;
								   var num2 = 21;
								   var ciclo1= 0;
								   for(ciclo1; ciclo1 < numerohojas; ciclo1++) {

									   for(num; num < num2; num++) {

										if(num < arregloinicial.length){
											 arregloporhojas[num] = arregloinicial[num];
											}
											else{
												break;
											}
										 }

									   num2 = num2+21;
									   if(Object.values(arregloporhojas).length > 0){
										   movimientosCaja(Object.values(arregloporhojas), ciclo1);
									   }
									   arregloporhojas = {};
									   if(num > arregloinicial.length){break;}
									 }
							   }
			
								
								var promiseCarteraInversion = new Promise((resolve, reject)=>{
									
									 if(respuesta.CarteraInversiones.length > 0){
										  dibujarCarteraInversiones(hojas_CarteraInversiones, respuesta.CarteraInversiones,respuesta.LogoCorredora);
									  }
									
									resolve(1);
								});
								
								var promiseMovTitulos = new Promise((resolve, reject)=>{
									 if(respuesta.MovimientosTitulos.length > 0){
										  dibujarmovimientostitulos(hojas_MovimentosTitulos, respuesta.MovimientosTitulos);
									  }	
									 resolve(2);
								});
								
								var promiseMovCaja = new Promise((resolve, reject)=>{
									if(respuesta.MovimientosCaja.length > 0){
										  dibujarmovimientosCaja(hojas_MovimentoCaja, respuesta.MovimientosCaja);
									  }
									resolve(3);
								});
							 	
								
								Promise.all([promiseCarteraInversion, promiseMovTitulos, promiseMovCaja]).then(values => { 

									   if(values[2] == 3){
											resolve(1);
									   }
								});
							  
							 //Cartera Inversiones 
							 
							  function CarteraInversiones(arreglodedatos, numero, LogoCorredora){
								var titulo = $('<label id="titulos">'+"Cartera de Inversiones"+'</label>');	
								var saltodepagina = document.getElementById("encabezadodepagina"); 		
								var nodo_duplicado = document.getElementById("encabezado");
								var encabezado = nodo_duplicado.cloneNode(true);
								//document.getElementById("logoCartola").innerHTML = '<img style="width: 70%" src="data:image/jpeg;base64,' + LogoCorredora + '"/>';
								
									$("#containerCarteraInversiones").append("<div class='row' id='carteraInversiones"+numero+"'"+"></div>");
									$(encabezado).insertBefore("#carteraInversiones"+numero+""); 
									titulo.appendTo("#carteraInversiones"+numero+"");
									   //CARTERA DE INVERSIONES
										 $("#carteraInversiones"+numero+"").kendoGrid({
											 dataBound: function(e){
												 var items = e.sender.items();
												 items.each(function(idx, item){
												   if((e.sender.dataItem(item).tipoFila == "totalMercado")){
													 $(item).addClass('rowTotalMercado');
												   }else if((e.sender.dataItem(item).tipoFila == "detalle")){
													 $(item).addClass('rowDetalle');
													 if((e.sender.dataItem(item).moneda == "")){
														 e.sender.dataItem(item).moneda = 'CLP';
													 }
												   }else if((e.sender.dataItem(item).tipoFila == "blanco")){
													   $(item).addClass('rowDetalle');
												   }else if((e.sender.dataItem(item).tipoFila == "totalClasificacion") || (e.sender.dataItem(item).tipoFila == "clasificacion") ){
													   $(item).addClass('rowTotalClasificacion');
												   }else if((e.sender.dataItem(item).tipoFila == "totalSubMercado")){
													   $(item).addClass('rowTotalSubMercado');
												   }else if((e.sender.dataItem(item).tipoFila == "totalPatrimonio")){
													 $(item).addClass('rowTotalMercado');
												   }
												 });
											   },	
											 dataSource: arreglodedatos,

												columns: [
													{ field: "detalleInstrumentos", title: "Detalle Instrumentos", width:200, headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
													{ field: "emisor", title: "Emisor"                           , width:100, headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
													{ field: "moneda", title: "Moneda"                           , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
													{ field: "cantidad", title: "Cantidad"                       , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && cantidad!='undefined'){##=(util.bcs_formato(cantidad,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
													{ field: "fecha_Vcto", title: "Fecha Vcto."                  , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
													{ field: "duracion", title: "Duracion."                      , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && duracion!='undefined'){##=(util.bcs_formato(duracion,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
													{ field: "PrecioPromCompra", title: "Precio Prom. Compra"    , width:110, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && PrecioPromCompra!='undefined'){##=(util.bcs_formato(PrecioPromCompra,'GEN','Precio', moneda, '','')).valor##} else {} #</label>"},
													{ field: "PrecioMercado", title: "Precio Mercado"            , width:90, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"},  template : "<label> # if(moneda!= '' && precioMercado!='undefined'){##=(util.bcs_formato(precioMercado,'GEN','Precio', moneda, '','')).valor##} else {} #</label>"},
													{ field: "valorMercadoCLP", title: "Valor Mercado ($$)"      , width:110, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila!= 'blanco' && tipoFila!= 'clasificacion' && valorMercadoCLP!='undefined'){##=(util.bcs_formato(valorMercadoCLP,'GEN','MONTO', 'CLP', '','')).valor##} else {} #</label>"},
													{ field: "valorMercadoUSD", title: "Valor Mercado (US$)"     , width:100, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila != 'blanco' && tipoFila!= 'clasificacion' && valorMercadoUSD!='undefined'){##=(util.bcs_formato(valorMercadoUSD,'GEN','MONTO', 'USD', '','')).valor##} else {} #</label>"},
													{ field: "dividendosCupones", title: "Dividendos / Cupones." , width:120, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && dividendosCupones!='undefined'){##=(util.bcs_formato(dividendosCupones,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"},
													{ field: "rentabP", title: "Rentab. (%)"                     , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}},
													{ field: "carteraP", title: "% Cartera"                      , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila != 'blanco' && tipoFila!= 'clasificacion' && carteraP!='undefined'){##=carteraP##} else {} #</label>"}

												]
											  });
							  }

							  function movimientostitulos(arreglodedatos, numero){	

								var titulo = $('<label id="titulos">'+"Movimientos de Titulos"+'</label>');			     
								var saltodepagina = document.getElementById("encabezadodepagina"); 
								var nodo_duplicado = document.getElementById("encabezado");
								var encabezado = nodo_duplicado.cloneNode(true);
								$("#containerMovimientoTitulos").append("<div class='row' id='movimientoTitulos"+numero+"'"+"></div>");
									$(encabezado).insertBefore("#movimientoTitulos"+numero+"");
									titulo.appendTo("#movimientoTitulos"+numero+"");
									//MOVIMIENTO DE TITULOS
									 $("#movimientoTitulos"+numero+"").kendoGrid({
										 dataBound: function(e){
											 var items = e.sender.items();
											 items.each(function(idx, item){
												 $(item).addClass('rowDetalle');
											 });
										   },	
											dataSource:arreglodedatos,
													columns: [
										    			{ field: "fechaOperacion", title: "Fecha Operación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "fechaLiquidacion", title: "Fecha Liquidación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "moneda", title: "Moneda", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "tipoMovimiento", title: "Tipo Movimiento", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "claseActivo", title: "Clase de Activo", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "nemoTecnico", title: "Nemotécnico", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
												    	{ field: "unidades", title: "Unidades", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && unidades!='undefined'){##=(util.bcs_formato(unidades,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
												    	{ field: "precio", title: "Precio", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && precio!='undefined'){##=(util.bcs_formato(precio,'GEN','Precio', moneda, '','')).valor##} else {} #</label>"},
												    	{ field: "gastos", title: "Gastos", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(gastos!='undefined'){##=(util.bcs_formato(gastos,'GEN','MONTO', 'CLP', '','')).valor##} else {} #</label>"},
												    	{ field: "montoOperacion", title: "Monto Operación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && montoOperacion!='undefined'){##=(util.bcs_formato(montoOperacion,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"}
										    		]	
								
										  });			
								}
							  
							  function movimientosCaja(arreglodedatos, numero){
										
								  var titulo = $('<label id="titulos">'+"Movimientos de Caja"+'</label>');	 
								  var saltodepagina = document.getElementById("encabezadodepagina"); 
								  var nodo_duplicado = document.getElementById("encabezado");
								  var encabezado = nodo_duplicado.cloneNode(true);
									$("#containerMovimientosCaja").append("<div class='row' id='movimientosCaja"+numero+"'"+"></div>");
										$(encabezado).insertBefore("#movimientosCaja"+numero+"");
										titulo.appendTo("#movimientosCaja"+numero+"");
										 //MOVIMIENTOS DE CAJA
										 $("#movimientosCaja"+numero+"").kendoGrid({
											 dataBound: function(e){
												 var items = e.sender.items();
												 items.each(function(idx, item){
													 $(item).addClass('rowDetalle');
												 });
											   },	
											 dataSource:arreglodedatos,
														columns: [
															{ field: "fechaOperacion", title: "Fecha Operación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
															{ field: "fechaLiquidacion", title: "Fecha Liquidación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
															{ field: "moneda", title: "Moneda", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
															{ field: "tipoMovimiento", title: "Tipo Movimiento", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
															{ field: "nemoTecnico", title: "Nemotécnico/Glosa", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
															{ field: "Ingreso", title: "Ingreso", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && Ingreso!='undefined' && Ingreso!='' ){##=(util.bcs_formato(Ingreso,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"},
															{ field: "Egreso", title: "Egreso", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && Egreso!='undefined' && Egreso!=''){##=(util.bcs_formato(Egreso,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"},
															{ field: "Saldo", title: "Saldo", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && Saldo!='undefined' && Saldo!=''){##=(util.bcs_formato(Saldo,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"}
														]

										});
							  
							}
					}
				});
			});
			
			
			var promiseDatosCartola =  new Promise((resolve, reject)=>{
				
				bcsServiciosCartola.ObtieneDataCartola(datos,
						function(response){		
								if (response.P_OUT_SALIDA != null){

									$scope.Telefono			    		  = response.P_OUT_SALIDA.datosSocioNegocio[0].Telefono;
									$scope.NombreEjecutivo      		  = response.P_OUT_SALIDA.datosSocioNegocio[0].NombreEjecutivo;
									$scope.NombreSocioNegocio   	      = response.P_OUT_SALIDA.datosSocioNegocio[0].NombreSocioNegocio;
									$scope.CodigoPortafolio     		  = response.P_OUT_SALIDA.datosSocioNegocio[0].CodigoPortafolio;
									$scope.DocumentoIdentidad   	      = response.P_OUT_SALIDA.datosSocioNegocio[0].DocumentosIdentidad;
									$scope.MonedaCartola        		  = response.P_OUT_SALIDA.datosSocioNegocio[0].MonedaCartola;
									$scope.FechaActual 					  = datos.p_FechaHasta;

							 }
						   
						});
				
				   resolve(2);
				   console.log(resolve);

			});
        
		
		Promise.all([promisePrincipal, promiseDatosCartola]).then(values => { 
		   if(values[1] == 2){
		
			     var saltodepagina = document.getElementById("encabezadodepagina"); 
			     var nodo_duplicado = document.getElementById("encabezado");
			     var nuevoDiv = document.createElement("div");
			     nuevoDiv.setAttribute("id","stop");
			     // añade el elemento creado y su contenido al DOM 
			     var stop = document.getElementById("#containerMovimientosCaja"); 
			     setTimeout(
			    		  function() 
			    		  {
			    			  document.body.insertBefore(nuevoDiv, stop);
			    		  }, 1000);
	
			     		 $(saltodepagina).insertBefore(encabezado);
			     		 $('div#encabezadodepagina:first').remove();
		   	   
		   }

		});
		
			
				
	}
}