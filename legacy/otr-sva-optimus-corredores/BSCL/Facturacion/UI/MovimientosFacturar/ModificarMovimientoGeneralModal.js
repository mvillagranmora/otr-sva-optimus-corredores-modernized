angular.module('ModuloFacturacion').controller('ModificarMovimientoGeneralModalCtrl',
		       function($scope,$translate,$modal,
				        bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma)
{
   
   $scope.data	= {}; 
   var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
      
   //-----------------------------------------------------
   // Configuracion de Combobox
   //-----------------------------------------------------
   $scope.data.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
   	
   $scope.data.oPortafolio={
	   tipo : "combobox-portafolio",
	   configuracion :{},
	   idActual : false,
	   seleccionado : false,
	   filtros: {
		  	      IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  $scope.bModifMvtoMod.oMovimientoComprobante.IdCliente}
			    },
   };	
   
   $scope.data.oFormaCustodia={
		tipo : "combobox-forma-custodia-orden",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros : {
			         IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }			
		          },
	};
   
   $scope.data.oBoveda={
		tipo : "combobox-boveda",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros : {			         
			         IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
		          },
	};  
   
	$scope.data.oTipoReferencia={
		tipo: "combobox-tipo-referencia",
		configuracion: {},
		idActual: false,
		seleccionado: false,
		filtros: {},
	}
   
   //Extras
   $scope.data.activaroComprobanteInd = false;
   $scope.data.oComprobanteInd = false;   
   
   //
   //Deshabilita afectos de cobro
   //   
   $scope.data.activarafectoComision = true;
   $scope.data.afectoComision = false;
   $scope.data.activarafectoIva = true;
   $scope.data.afectoIva = false;
   $scope.data.activarafectoGasto = true;
   $scope.data.afectoGasto = false;
   $scope.data.activarafectoDerecho = true;
   $scope.data.afectoDerecho = false;
   
   
     
   
   //-----------------------------------------------------
   //Actualiza tabla con nuevos datos 
   //-----------------------------------------------------
   $scope.data.modificarMovimientoComprobanteGral = function()   
     {	       
	    
	    $scope.MvtoCbmteOriginal = $.extend({},$scope.bModifMvtoMod.oMovimientoComprobante);
		$scope.MvtoCbmte = $.extend({},$scope.bModifMvtoMod.oMovimientoComprobante);
		
		bcsComunes.setConflictos([]);
		
		//Acumula tabla de entrada de movimiento comprobante - general
		var MovimientoFacturarGral = [];
		MovimientoFacturarGral.push(
	      		                     {     
	      			                    IdMovimientoComprobante : $scope.MvtoCbmte.IdMovimientoComprobante,
	      			                    IdPortafolio            : $scope.data.oPortafolio.idActual !== $scope.MvtoCbmteOriginal.IdPortafolio?$scope.data.oPortafolio.idActual:null,
	      			                    IdFormaCustodia         : $scope.data.oFormaCustodia.idActual !== $scope.MvtoCbmteOriginal.IdFormaCustodia?$scope.data.oFormaCustodia.idActual:null,
	      			                    IdBoveda                : $scope.data.oBoveda.idActual !== $scope.MvtoCbmteOriginal.IdBoveda?$scope.data.oBoveda.idActual:null,
	      			                    EsComprobIndividual     : $scope.data.oComprobanteInd ? 'S' : 'N',
	      			                    Glosa	                : $scope.data.oComentario !== $scope.MvtoCbmteOriginal.Glosa?$scope.data.oComentario:null,
	      			                    Referencia              : $scope.data.oReferencia !== $scope.MvtoCbmteOriginal.Referencia?$scope.data.oReferencia:$scope.MvtoCbmteOriginal.Referencia||null,
										TipoReferencia			: $scope.data.oTipoReferencia !== $scope.MvtoCbmteOriginal.TipoReferencia?$scope.data.oTipoReferencia.idActual||'':null,
	      			                    MontoMovimientoComprobante : $scope.MvtoCbmte.MontoMovimientoComprobante
	      			                 }
	      		                   );
	    		
		//parametros de servicio
		var datos = {};		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo  ='FAC';
		datos.p_simularOperacion = 'N';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_EsIngresoAutomatico = 'N';
		datos.P_MOVIMIENTOFACTURARGRAL = MovimientoFacturarGral;
		
		bcsComunes.ventanaOverlay(idVentana, true);
		
		bcsServiciosFacturacion.ModificarMovimientoComprobanteCallSP(datos).$promise.then(function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);
			  
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			} else{
				if (respuesta.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant(""),
						conflictos:util.parseServerResponse(respuesta)
					}, idVentana);
				} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant(""),
						conflictos:util.parseServerResponse(respuesta)
					}, idVentana); 
				}
			}
		},function(respuesta){
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.msg("error","Error",'UI_ERROR_MODIFICACION');
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		});
	};
	 
   //--------------------------------------------------------- 
   //Activa afectos
   //---------------------------------------------------------	 
   activaAfectos(); 
	 
   //---------------------------------------------------------
   //Activa afectos segun los cobros asociados al movimiento
   //---------------------------------------------------------
   function activaAfectos()
     {
	   if ($scope.bModifMvtoMod.oMovimientoComprobante.EsComprobIndividual === 'S')
	   {
	      $scope.data.activaroComprobanteInd = false;
	      $scope.data.oComprobanteInd = true;
	   }
	   
	   $scope.data.oComentario = $scope.bModifMvtoMod.oMovimientoComprobante.Glosa;
	   $scope.data.oReferencia = $scope.bModifMvtoMod.oMovimientoComprobante.Referencia || '';
	   
	   //Consulta cobros para movimiento seleccionado	
	   var MvtoCobrosDatasource = new kendo.data.DataSource
	     ({
		    type: "odata",
		    serverFiltering     : true, // Filtrado de datos en el servidor.	
		
		    filter : { 
			  	       field: "IdMovimientoComprobante", operator: "eq", value: $scope.bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante
				     },
		    transport: {
			             read: {
				                 url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",
				                 dataType: "json"
				               }
		               },
		 });
	   
	   
	    //Lee datos
		MvtoCobrosDatasource.fetch().then(function() 
	   	  {
		     var i=0;
		     var data = MvtoCobrosDatasource.data();
		     
		     for(i; i < data.length; i++)
		       {
		          var IdCobroMvto = data[i].CodigoCobroClasificacion;

		          switch(IdCobroMvto)
		            {
		       	      case "COMISION":
		       	        {
		       	    	   $scope.data.afectoComision = true;
		       	           break;
		       	        }
		       	      
		       	      case "DERECHO":
		       	        {
		       	    	   $scope.data.afectoDerecho = true;
		       	           break;
		       	        }

		       	      case "GASTO":
		       	        {
		       	    	   $scope.data.afectoGasto = true;
		       	           break;
		       	        }

		       	      case "IMPUESTO":
		       	        {
		       	    	   $scope.data.afectoIva = true;	
		       	           break;	
		       	        }    		       	        
		       	      default:  //Otros: muestra combo "Modalidad pago" y activa todas las modalidades
		    	      break;
		            };  	
		        }
	   	  });
     };
	 
   
   //-----------------------------------------------------
   //Cierra ventana de modificacion general
   //-----------------------------------------------------	 
   $scope.data.cancelar = function() 
     {
		bcsComunes.ventanaCerrar(idVentana);
	 };
   
   //-----------------------------------------------------
   //Opción no habilitada
   //-----------------------------------------------------	 
   $scope.data.opcionNoHabilitada = function() 
     {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	 }; 
	
});