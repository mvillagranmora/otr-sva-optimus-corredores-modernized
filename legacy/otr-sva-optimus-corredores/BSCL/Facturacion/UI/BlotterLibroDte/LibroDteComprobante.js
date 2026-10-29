angular.module('ModuloFacturacion').controller('LibroDteComprobanteCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsLibroDteComprobanteGrid, bcsOperacionesComunes)
{
	$scope.data	= {};

	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.data.oPeriodoContable = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.data.monthSelectorOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
	
	//GRILLA
	$scope.confBasicaLibroDteComprobante = { 
		dataSource			: bcsLibroDteComprobanteGrid.dataSourceLibroDteComprobante,
		columns				: bcsLibroDteComprobanteGrid.columnasLibroDteComprobante(),
		sortable			: true,
		allowCopy			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		////autoBind			: true,
		height				: 450,
		selectable 			: "multiple row"
		
	};
	
	
	//BOTONES
	$scope.buscar = function(){
		var filters = new Array();

		if ($scope.data.oPeriodoContable)
			auxDatePerCont =  util.dateOdata(util.date($scope.data.oPeriodoContable).hours(0).minutes(0).seconds(0).toDate());
		    primerDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth(), 1);
		    ultimoDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth() + 1, 0);

		    filters.push( { field: "FechaComprobante", operator: "gte", value : primerDia });
		    filters.push( { field: "FechaComprobante", operator: "lte", value : ultimoDia });
		
 	    bcsLibroDteComprobanteGrid.dataSourceLibroDteComprobante.filters(filters);
	};
	
		
	$scope.limpiarFiltros = function()
	{	
		//$scope.data.portafolio = false;
		$scope.data.oPeriodoContable = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		bcsLibroDteComprobanteGrid.dataSourceLibroDteComprobante.removeAll();	
	};
	
	
	//*******************************************
	// Enviar Libro dte
	//*******************************************
	$scope.data.enviarLibroDte = function() {
		
		//Valida que se hayan seleccionado movimientos
		if ($scope.seleccionLibroDteComprobante === undefined)
		{
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COMPROBANTE'));
			return;
		}
		else
		{
			//Mensaje de confirmación
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_COMPROBANTE_LIBRO_DTE"), idVentana);
		    t.then(function(result)
		      {
			     if (result===OPCIONES_BCSALERTA.SI)
			       {
			    	  var ComprobanteEnviar = [];
		              bcsComunes.ventanaOverlay(idVentana, true);
		              $scope.data.btnActivos  = false;
      	   
			          //Recopila datos de movimientos seleccionados
      	              $.each($scope.seleccionLibroDteComprobante,function(key,value)
      	            	{      		     
      	    	           ComprobanteEnviar.push(
	      		               {     
	      			             IdPortafolio			: $scope.seleccionLibroDteComprobante[key].IdPortafolio,  
	      			             IdCliente				: $scope.seleccionLibroDteComprobante[key].IdCliente,
	      			             IdComprobante          : $scope.seleccionLibroDteComprobante[key].IdComprobante,
	      			             IdTipoComprobante		: $scope.seleccionLibroDteComprobante[key].IdTipoComprobante	      		                                  
	      		               });
      	                });
      	   
    	   	          //Parámetros de entrada al servicio
	   	              var datosEnviar = {};
	   	    
	      	          datosEnviar.p_IdEmpresa			= $scope.data.IdEmpresa; 
	      	          datosEnviar.p_IdUsuario			= $scope.data.IdUsuario;
	      	          datosEnviar.p_IdModulo			= $scope.data.IdModulo;
	      	          datosEnviar.p_simularOperacion	= 'N';
	      	          datosEnviar.p_UsaReglasNegocio	= 'S';
	      	          datosEnviar.p_EsIngresoAutomatico = 'N';  
	      	          datosEnviar.p_PeriodoContable     = util.dateSPdate($scope.data.oPeriodoContable);
	      	          datosEnviar.P_COMPROBANTE = ComprobanteEnviar;
      	
	    	          var successEnviar = function(respuesta)
	    	             {	  		
	    		            if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
	    		               {	
	    			             //Se actualiza los datos de la grilla	    			
                                 bcsComunes.ventanaMsg({
			  			                                  conflictos:util.parseServerResponse(respuesta)
					                                   },idVentana);
 	    	        
	    			             bcsLibroDteComprobanteGrid.dataSourceLibroDteComprobante.read();					
	    		               }
	    		            else
	    	                   {
					   			 if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
	    			               {
	    				              bcsComunes.ventanaMsg({
	    					          	                       mensaje:$translate.instant(""),
	    					                                   conflictos:util.parseServerResponse(respuesta)
	    					                                },idVentana);
		                           }
	    			             else
	    			               if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
	    			                 {
	    				                bcsComunes.ventanaMsg({
	    					            	                     mensaje:$translate.instant(""),
	    					                                     conflictos:util.parseServerResponse(respuesta)
	    					                                  },idVentana); 
	    			                 }
	    	                   }
	    		
	    		            $scope.data.btnActivos  = true;
	    		            bcsComunes.ventanaOverlay(idVentana, false);
	    		            bcsLibroDteComprobanteGrid.dataSourceLibroDteComprobante.read();
	    	             };
	    	
	    	
	    	          var errorEnviar = function(respuesta)
	    	             {	
				           bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
				           $scope.data.btnActivos  = true;     //Activa botones
				           bcsComunes.ventanaOverlay(idVentana, false);
	    	             };
	    	
	    	
	    	          //Llamada a servicio para crear comprobante de facturación()
	      	          bcsServiciosFacturacion.DteEnviarLibro(datosEnviar, successEnviar, errorEnviar);
			       }
			     else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		      });			
		}		
	};
		
	$scope.data.seleccionarTodosComprobantes = function() {	
		$scope.gridApiLibroDteComprobante.select($scope.gridApiLibroDteComprobante.tbody.find(">tr"));		
	};
	
	
	$scope.data.deseleccionarTodosComprobantes = function() {
		$scope.gridApiLibroDteComprobante.clearSelection();
	};

});