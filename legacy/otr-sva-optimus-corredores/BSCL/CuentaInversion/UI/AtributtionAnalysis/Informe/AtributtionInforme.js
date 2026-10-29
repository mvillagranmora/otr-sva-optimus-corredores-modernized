angular.module('ModuloCuentaInversion').controller(
		  'AtributtionInformeController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
		  'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils',
		  'Model',AtributtionInformeController ]);

function AtributtionInformeController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, Model) {
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var atributtionAnalysis = {};
	var nombreatributtionAnalysis = null;
	$scope.verInformeHabilitado = true;

	
	$scope.FechaActual 			= parametrosVentana.FechaActual;
	$scope.NombreSocioNegocio 	= parametrosVentana.NombreSocioNegocio;
	$scope.DocumentoIdentidad 	= parametrosVentana.DocumentoIdentidad;
	$scope.CodigoPortafolio		= parametrosVentana.CodigoPortafolio;
	
	$scope.Atributtion		    = parametrosVentana.Atributtion;
	$scope.RentabilidadClasificacion = parametrosVentana.RentabilidadClasificacion;
	$scope.rentabilidadindice	= parametrosVentana.rentabilidadindice;
	
	$scope.FechaDesde		    = parametrosVentana.FechaDesde;
	$scope.FechaHasta		    = parametrosVentana.FechaHasta;
	
	
	
	
	 //CARTERA DE INVERSIONES	 
	 if ($scope.Atributtion.length > 1) {
		 $("#atributtionAnalysis").kendoGrid({
			 dataBound: function(e){
	             var items = e.sender.items();
	             items.each(function(idx, item){
	               if((e.sender.dataItem(item).TipoFila == "mercado") || (e.sender.dataItem(item).TipoFila == "submercado") ){
	            	   $(item).addClass('rowTotalMercado');
	               }else if((e.sender.dataItem(item).TipoFila == "detalle")){
	                 $(item).addClass('rowDetalle');
	                 if((e.sender.dataItem(item).MonedaReajuste == "")){
	                	 e.sender.dataItem(item).MonedaReajuste = 'CLP';
	                 }
	               }else if((e.sender.dataItem(item).TipoFila == "blanco")){
	            	   $(item).addClass('rowDetalle');
	               }else if((e.sender.dataItem(item).TipoFila == "totalClasificacion") || (e.sender.dataItem(item).TipoFila == "clasificacion") ){
	            	   $(item).addClass('rowTotalClasificacion');
	               }else if((e.sender.dataItem(item).TipoFila == "totalSubMercado")){
	            	   $(item).addClass('rowTotalSubMercado');
	               }else if((e.sender.dataItem(item).TipoFila == "totalPatrimonio")){
	                 $(item).addClass('rowTotalMercado');
	               }
	             });
	           },	
			 dataSource: $scope.Atributtion,
			 
			 columns: [
			    	{ field: "nemotecnico", 						title: "Nemotecnico"                       						, width:120,  headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaTextoAtributtion"}},
			    	{ field: "MonedaReajuste", 		 			 	title: "Moneda Reajuste"                   						, width:50,   headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}},
			    	
			    	{ field: "ValorInstrumento", 		 			title: "Valor Portafolio al ("+parametrosVentana.FechaHasta+")" , width:100,  headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(ValorInstrumento,'CTA','Cantidad','CLP','','')).valor #</label>"},
			    	{ field: "utilidadPerdida", 					title: "Utilidad / Pérdida"                						, width:70,   headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(utilidadPerdida,'CTA','Cantidad','CLP','','')).valor #</label>"},
			    	{ field: "porcentajePortafoliodesde", 			title: "(%) Portafolio al ("+parametrosVentana.FechaDesde+")"   , width:100,  headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(porcentajePortafoliodesde,'CTA','Cantidad','CLP','','')).valor #</label>"},
			    	{ field: "porcentajePortafoliohasta",         	title: "(%) Portafolio al ("+parametrosVentana.FechaHasta+")" 	, width:100,  headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(porcentajePortafoliohasta,'CTA','Cantidad','CLP','','')).valor #</label>"},
			    	{ field: "rentabilidadMonedaReajuste", 	 		title: "Rentabilidad (%) (Moneda Reajuste)"					    , width:110,  headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(rentabilidadMonedaReajuste,'CTA','Cantidad','CLP','','')).valor #</label>"},
			    	{ field: "contribucionalPortafolio", 		 	title: "Contribución al Portafolio (%)"    						, width:90,   headerAttributes: {"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(contribucionalPortafolio,'CTA','Cantidad','CLP','','')).valor #</label>"}
			    ]
			  });
	}else
		$('#tit_carteraInver').css('visibility', 'hidden');

	 
	 //RENTABILIDAD POR CLASIFICACION
	 $("#rentabilidadClasificacion").kendoGrid({
		 dataBound: function(e){
             var items = e.sender.items();
             items.each(function(idx, item){
               if((e.sender.dataItem(item).tipoRegistro == "totalMercado")){
            	   $(item).addClass('rowTotalMercadoNegra');
               }else if((e.sender.dataItem(item).tipoRegistro == "totalSubMercado")){
            	   $(item).addClass('rowDetalle');
               }else if((e.sender.dataItem(item).tipoRegistro == "totalPatrimonio")){
            	   $(item).addClass('tituloPrincipalGrillaAtributtion');
               }else if((e.sender.dataItem(item).tipoRegistro == "blancoN")){
            	   $(item).addClass('tituloPrincipalGrillaAtributtion');
               }else if((e.sender.dataItem(item).tipoRegistro == "blanco")){
            	   $(item).addClass('rowDetalle');
               }
             });
           },	
		 	dataSource:  $scope.RentabilidadClasificacion,
		    columns: [		    	
		    	{ field: "nemotecnico", title: "Clasificación", width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaTextoAtributtion"}},		    	
		    	{ field: "MonedaReajuste", title: "Rentabilidad (%)",  width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaAtributtion"} , attributes: {"class": "columnaNumeroAtributtion"}, attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(MonedaReajuste,'CTA','Precio','CLP','','')).valor #</label>"}		    	
		    	]
		  }); 
	 
	//RENTABILIDAD POR INDICE
	 $("#rentabilidadIndice").kendoGrid({
		 dataBound: function(e){
             var items = e.sender.items();
             items.each(function(idx, item){
               if((e.sender.dataItem(item).tipoRegistro == "totalMercado")){
            	   $(item).addClass('rowTotalMercadoNegra');
               }else if((e.sender.dataItem(item).tipoRegistro == "totalSubMercado")){
            	   $(item).addClass('rowDetalle');
               }else if((e.sender.dataItem(item).tipoRegistro == "totalPatrimonio")){
            	   $(item).addClass('tituloPrincipalGrillaAtributtion');
               }else if((e.sender.dataItem(item).tipoRegistro == "blancoN")){
            	   $(item).addClass('tituloPrincipalGrillaAtributtion');
               }else if((e.sender.dataItem(item).tipoRegistro == "blanco")){
            	   $(item).addClass('rowDetalle');
               }
              
             });
           },	
		 	dataSource:  $scope.rentabilidadindice,
		    columns: [		    	
		    	{ field: "Indice", title: "Clasificación", width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaAtributtion"}, attributes: {"class": "columnaTextoAtributtion"}},		    	
		    	{ field: "rentabilidadIndice", title: "Índice (%)", width: "150px",headerAttributes:{"class": "tituloPrincipalGrillaAtributtion"} , attributes: {"class": "columnaNumeroAtributtion"}, template : " <label> #:  (util.bcs_formato(rentabilidadIndice,'CTA','Precio','CLP','','')).valor #</label>"}		    	
		    	]
		  });
	 
}