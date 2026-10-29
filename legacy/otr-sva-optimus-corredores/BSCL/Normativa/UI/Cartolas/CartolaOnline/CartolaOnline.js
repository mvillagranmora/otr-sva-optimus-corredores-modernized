angular.module('ModuloNormativa').controller(
		'CartolaOnlineController',
		[ '$scope', '$translate', 'bcsComunes', 'Utils','bcsServiciosCartola', CartolaOnlineController ]);

/*
 * LOS CAMBIOS REALIZADOS A LOS kendoGrid DE ESTE JS TAMBIEN DEBEN SER APLICADOS A Plataforma\UI\CAR\Cartola.js
 * JS DESTINADO PARA LA GENERACION DEL PDF CON PUPPETEER
 * */
function CartolaOnlineController($scope, $translate, bcsComunes, Utils, bcsServiciosCartola) {
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var cartola = {};
	var nombrecartola = null;
	$scope.verCartolaHabilitado = true;
	$scope.esPorGrupo = parametrosVentana.IdGrupo ? true : false;
	
	
	if($scope.verCartolaHabilitado){
		
		$scope.uiCarteraInv  = $translate.instant('UI_CARTERA_INVERSIONES');
		$scope.uiMovTitulos  = $translate.instant('UI_MOVIMIENTOS_TITULOS');
		$scope.uiMovCaja	 = $translate.instant('UI_MOVIMIENTOS_CAJA');
		
	}
	
	
     
    console.log($translate.instant('UI_CLIENTE')); 
	
	$scope.FechaActual 			= parametrosVentana.FechaActual;
	$scope.NombreSocioNegocio 	= parametrosVentana.NombreSocioNegocio;
	$scope.DocumentoIdentidad 	= parametrosVentana.DocumentoIdentidad;
	$scope.MonedaCartola		= parametrosVentana.MonedaCartola;
	$scope.NombreEjecutivo		= parametrosVentana.NombreEjecutivo;
	$scope.Telefono         	= parametrosVentana.Telefono;
	$scope.CodigoPortafolio		= parametrosVentana.CodigoPortafolio;
	$scope.NombreGrupoCliente   = parametrosVentana.NombreGrupoCliente;
	
	
	$scope.MovimientosTitulos	= parametrosVentana.MovimientosTitulos;
	$scope.AliasPortafolio		= parametrosVentana.AliasPortafolio;
	$scope.MovimientosCartera   = parametrosVentana.CarteraInversiones;
	$scope.MovimientosCaja		= parametrosVentana.MovimientosCaja;
	$scope.FlujoPatrimonial     = parametrosVentana.FlujoPatrimonial;
	$scope.ComposicionCartera	= parametrosVentana.ComposicionCartera;
	$scope.FechaInicioFlujo		= parametrosVentana.FechaInicioFlujoPatrimonial;
	$scope.DistribucionActivos	= parametrosVentana.DistribucionPorClaseActivo;
	$scope.ExposicionPorMoneda	= parametrosVentana.ExposicionPorMoneda;
	$scope.FechaInicioFlujoPatrimonial = parametrosVentana.FechaInicioFlujoPatrimonial;
	$scope.LogoCorredora 		= parametrosVentana.LogoCorredora;

	
	document.getElementById("logoCartola").innerHTML = '<img style="width: 30%" src="data:image/jpeg;base64,' + $scope.LogoCorredora + '"/>';
	
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
		 dataSource: $scope.FlujoPatrimonial,		 	
		 columns: [
	    				    { field: "titulo", title: "Flujo Patrimonial",	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaTextoCartola"}},
							{ field: "mtd", title: "MTD*"              ,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && mtd!='undefined'){##=(util.bcs_formato(mtd,titulo!='Rentabilidad (%)'?'GEN':'MES',titulo!='Rentabilidad (%)'?'Monto':'Tasa', 'CLP', '','')).valor##} else {##=''##} #</label>"},
							{ field: "ytd", title: "YTD*"              ,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && ytd!='undefined'){##=(util.bcs_formato(ytd,titulo!='Rentabilidad (%)'?'GEN':'MES',titulo!='Rentabilidad (%)'?'Monto':'Tasa', 'CLP', '','')).valor##} else {} #</label>"},
							{ field: "itd", title: "ITD " + $scope.FechaInicioFlujo,	headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoRegistro != 'blanco' && tipoRegistro != 'blancoN'  && itd!='undefined'){##=(util.bcs_formato(itd,titulo!='Rentabilidad (%)'?'GEN':'MES',titulo!='Rentabilidad (%)'?'Monto':'Tasa', 'CLP', '','')).valor ## } else {} #</label>"}
			    	]  	
		  });
	 
	 
	
	 
	 //Exposicion por Monedas
	 $("#graficoexposicionpormonedas").kendoChart({
         title: {
            text: "Exposición por monedas",
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
             data: $scope.ExposicionPorMoneda
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

	 
	 //DISTRIBUCIÓN POR CALSE DE ACTIVOS
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
	            data: $scope.DistribucionActivos	            
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
	
	 
	 
	 
	 //COMPOSICIÓN DE LA CARTERA
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
		 	dataSource: $scope.ComposicionCartera,
		    columns: [
		    	{ field: "titulo", title: "Composición de la Cartera", width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaTextoCartola"}},
		    	{ field: "montoTotal", title: "Monto",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(orden != -1 && montoTotal!='undefined'){##=(util.bcs_formato(montoTotal,'GEN','Monto', 'CLP', '','')).valor##} else {} #</label>"},
		    	{ field: "porcentajeTotal", title: "%",headerAttributes:{"class": "tituloPrincipalGrillaCartolaGris"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(orden != -1 && porcentajeTotal!='undefined'){##=porcentajeTotal##} else {} #</label>"}
		     
		    ]
		  });
	 
	 
	 //CARTERA DE INVERSIONES	 
	 if ($scope.MovimientosCartera.length > 1) {
		 $("#carteraInversiones").kendoGrid({
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
			 dataSource: $scope.MovimientosCartera,
			 
			    columns: [
			    	{ field: "detalleInstrumentos", title: "Detalle Instrumentos", width:200, headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
			    	{ field: "emisor", title: "Emisor"                           , width:100, headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
			    	{ field: "moneda", title: "Moneda"                           , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
			    	{ field: "cantidad", title: "Cantidad"                       , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && cantidad!='undefined'){##=(util.bcs_formato(cantidad,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
			    	{ field: "fecha_Vcto", title: "Fecha Vcto."                  , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
			    	{ field: "duracion", title: "Duracion."                      , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && duracion!='undefined'){##=(util.bcs_formato(duracion,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
			    	{ field: "PrecioPromCompra", title: "Precio Prom. Compra"    , width:110, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && PrecioPromCompra!='undefined'){##=(util.bcs_formato(PrecioPromCompra,'GEN','Precio', moneda, idTipoInstrumento,'')).valor##} else {} #</label>"},
			    	{ field: "PrecioMercado", title: "Precio Mercado"            , width:90, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"},  template : "<label> # if(moneda!= '' && precioMercado!='undefined'){##=(util.bcs_formato(precioMercado,'GEN','Precio', moneda, idTipoInstrumento,'')).valor##} else {} #</label>"},
			    	{ field: "valorMercadoCLP", title: "Valor Mercado ($$)"      , width:110, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila!= 'blanco' && tipoFila!= 'clasificacion' && valorMercadoCLP!='undefined'){##=(util.bcs_formato(valorMercadoCLP,'GEN','MONTO', 'CLP', '','')).valor##} else {} #</label>"},
			    	{ field: "valorMercadoUSD", title: "Valor Mercado (US$)"     , width:100, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila != 'blanco' && tipoFila!= 'clasificacion' && valorMercadoUSD!='undefined'){##=(util.bcs_formato(valorMercadoUSD,'GEN','MONTO', 'USD', '','')).valor##} else {} #</label>"},
			    	{ field: "dividendosCupones", title: "Dividendos / Cupones." , width:120, headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && dividendosCupones!='undefined'){##=(util.bcs_formato(dividendosCupones,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"},
			    	{ field: "rentabP", title: "Rentab. (%)"                     , width:70,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}},
			    	{ field: "carteraP", title: "% Cartera"                      , width:60,  headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(tipoFila != 'blanco' && tipoFila!= 'clasificacion' && carteraP!='undefined'){##=carteraP##} else {} #</label>"}
			     
			    ]
			  });
	}else{
		$('#tit_carteraInver').css('visibility', 'hidden');
	}
		
	
	 
	//MOVIMIENTO DE TITULOS
	 if ($scope.MovimientosTitulos.length > 0) {
		 $("#movimientoTitulos").kendoGrid({
			 dataBound: function(e){
	             var items = e.sender.items();
	             items.each(function(idx, item){
	            	 if((e.sender.dataItem(item).tipoRegistro == "tituloCliente")){
		                 $(item).addClass('tituloPrincipalGrillaCartolaGris');
		               }else{
		            	   $(item).addClass('rowDetalle');
		               }	                 
	             });
	           },	
			 	dataSource:$scope.MovimientosTitulos,
			    		columns: [
			    			{ field: "fechaOperacion", title: "Fecha Operación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"} , attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "fechaLiquidacion", title: "Fecha Liquidación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "moneda", title: "Moneda", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "tipoMovimiento", title: "Tipo Movimiento", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "claseActivo", title: "Clase de Activo", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "nemoTecnico", title: "Nemotécnico", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaTextoCartola"}},
					    	{ field: "unidades", title: "Unidades", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && unidades!='undefined'){##=(util.bcs_formato(unidades,'GEN','Cantidad', moneda, '','')).valor##} else {} #</label>"},
					    	{ field: "precio", title: "Precio", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && precio!='undefined'){##=(util.bcs_formato(precio,'GEN','Precio', moneda, idTipoInstrumento,'')).valor##} else {} #</label>"},
					    	{ field: "gastos", title: "Gastos", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(gastos!='undefined'){##=(util.bcs_formato(gastos,'GEN','MONTO', 'CLP', '','')).valor##} else {} #</label>"},
					    	{ field: "montoOperacion", title: "Monto Operación", headerAttributes: {"class": "tituloPrincipalGrillaCartola"}, attributes: {"class": "columnaNumeroCartola"}, template : "<label> # if(moneda!= '' && montoOperacion!='undefined'){##=(util.bcs_formato(montoOperacion,'GEN','MONTO', moneda, '','')).valor##} else {} #</label>"}
			    		]
			 
			  });
	 }else{
		$('#tit_movTitulos').css('visibility', 'hidden'); 
	 }
		
	 
	//MOVIMIENTOS DE CAJA
	 if ($scope.MovimientosCaja.length > 0) {
		 $("#movimientosCaja").kendoGrid({
			 dataBound: function(e){
	             var items = e.sender.items();
	             items.each(function(idx, item){
	            	 if((e.sender.dataItem(item).tipoRegistro == "tituloCliente")){
		                 $(item).addClass('tituloPrincipalGrillaCartolaGris');
		               }else{
		            	   $(item).addClass('rowDetalle');
		               }	      
	             });
	           },	
			 dataSource:$scope.MovimientosCaja,
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
	 }else{
		 $('#tit_movCaja').css('visibility', 'hidden'); 
	 }
		
	 
	//Funcion que llama al servicio que obtiene pdf /////////////////////////////////////////////////////
		
	function peticioncartolapdf(){
		   
			var params = {};
			
			var infoCliente = [{
				"IdCliente"		:  $scope.IdCliente,
				"IdPortafolio" 	:  $scope.IdPortafolio
			}];
			
			
			params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
			params.p_IdModulo 		= 'NOR';
			params.p_IdUsuario 		= $scope.SesionIdUsuario;
			params.p_FechaDesde 	= parametrosVentana.fd;
			params.p_FechaHasta 	= parametrosVentana.fh;
			params.p_Datos			= parametrosVentana.p_Datos;
			params.p_token			= $scope.SesionToken();
			if (nombrecartola == null){
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
				bcsServiciosCartola.GenerarCartolaPDF(params, function(response){
					if (response.CartolaEncoded != null){
						bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
						nombrecartola = response.nombrearchivo;
						cartola = util.b64toBlob(response.CartolaEncoded,'application/pdf',512);
						saveAs(cartola, nombrecartola);
						
					}
				}, function(response) {
					bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
					Utils.showError(response, 'Consulta de Cartolas', bcsComunes
							.ventanaWinId($scope));
				});
			}else{
				saveAs(cartola, nombrecartola);
			}
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	 
	$scope.descargar = function(){
		
		peticioncartolapdf();
	
	}
	 
}