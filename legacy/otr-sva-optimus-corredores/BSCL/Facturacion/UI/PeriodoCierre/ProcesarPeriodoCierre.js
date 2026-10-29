angular.module('ModuloFacturacion').controller('PeriodoCierreCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosFacturacion,bcsPeriodoCierreGrid, bcsAlerta, OPCIONES_BCSALERTA)
		

{
	$scope.data	= {};
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
		
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	$scope.data.oAnoPeriodo = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	
	
	
	//GRILLA PRINCIPAL
	$scope.confBasicaProcesarPeriodoCierre = { 
	    
		dataSource			: bcsPeriodoCierreGrid.dataSourcePeriodoCierre,
		columns				: bcsPeriodoCierreGrid.columnasPeriodoCierre(),
		rowColorBackground	: bcsPeriodoCierreGrid.fnColorBk,
		sortable			: true,
		pageable			: false,
		filterable			: false,
		userSettings        : true,
		height				: 260,
		selectable 			: "single row",
		navigatable         : true,
		allowCopy 			: true
		
	};
	
	
	$scope.confBasicaAnoPeriodo = {
	   depth : "decade",
	   start : "decade",
	   format : "yyyy"
	}
	
		
	
	$scope.buscar = function(){
		var filters = new Array();
	
		if ($scope.data.oAnoPeriodo)
			filters.push( { "field": "AnoPeriodo","operator": "eq", "value" : (util.dateSPdate($scope.data.oAnoPeriodo)).substring(0, 4)});
	
		filters.push( { "field": "IdEstado","operator": "eq", "value" : 'VIG'});
		
		$scope.gridApiProcPerCierre.dataSource.filter(filters);
	}
	
	
	//Procesa Periodo Cierre
    $scope.data.cerrarPeriodo = function() {
		
		//Valida que se hayan seleccionado un registro
		if ($scope.seleccionProcPerCierre === undefined)
		{	
			$scope.data.btnActivos   = true;     //Activa botones
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_PERIODO'));
			return;
		}
		else
		{
			//Mensaje de confirmación
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_PERIODO_CERRAR"), idVentana);
		    t.then(function(result)
		      {
			     if (result===OPCIONES_BCSALERTA.SI)
			       {
			    	 var PeriodoProcesar = [];
			         bcsComunes.ventanaOverlay(idVentana, true);
			         $scope.data.btnActivos  = false;   //Bloquea botones
			
			         //Recopila Id de movimientos seleccionados
			         //$.each($scope.seleccionProcPerCierre,function(key,value)
		      	      // {
			      		  PeriodoProcesar.push(
			      		           {   
			      			         IdPeriodoCierre        : $scope.seleccionProcPerCierre.IdPeriodoCierre,
			      		             Periodo                : $scope.seleccionProcPerCierre.Periodo,
			      		             IdEstadoPeriodo        : $scope.seleccionProcPerCierre.IdEstadoPeriodo,			      			      
			      		           });
		      	       //});
      	    
			
	    	         //Parámetros de entrada al servicio de bloqueo.
	   	             var datosProcesar = {};
	   	    
	   	             datosProcesar.p_IdEmpresa			= $scope.data.IdEmpresa;
	   	             datosProcesar.p_IdUsuario			= $scope.data.IdUsuario;
	   	             datosProcesar.p_IdModulo			= $scope.data.IdModulo;
	   	             datosProcesar.p_simularOperacion   = 'N';
	   	             datosProcesar.p_UsaReglasNegocio   = 'S';
	   	             datosProcesar.p_EsIngresoAutomatico = 'N';
	   	             datosProcesar.P_PERIODOPROCESAR 	 = PeriodoProcesar;   
	      	         
	      		      	
	      	         //Resultado del servicio
	     	         var successProcesar = function(respuesta)
	    	           {	
	     	        	  bcsComunes.ventanaOverlay(idVentana, false);
	    		          if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
	    		            {	
	    			          //Se actualiza los datos de la grilla	    			
                              /*bcsComunes.ventanaMsg({
			  			                               conflictos:util.parseServerResponse(respuesta)
					                                },idVentana);*/
 	    	        
	    		        	  bcsComunes.msg("info","Periodo Cierre", "Periodo Modificado correctamente");
	    		        	  $scope.gridApiProcPerCierre.dataSource.read();
	    		        	  $scope.seleccionProcPerCierre = undefined;
	    		        	  
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
	    			            	 bcsComunes.setConflictos(util.parseServerResponse(respuesta));	    			            	 
	    			               }
	    	                }

	    		          $scope.data.btnActivos  = true;
	    		          $scope.gridApiProcPerCierre.dataSource.read();
	    	           };	    	
	    	
	    	         var errorProcesar = function(respuesta)
	    	           {	
	    		         bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				         bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
				
				         bcsComunes.ventanaOverlay(idVentana, false);
				         $scope.data.btnActivos  = true; //Activa botones
	    	           };
	    	
	    	         //Llamada a servicio para bloquear comprobantes
	    	         bcsServiciosFacturacion.ProcesarPeriodoCierre(datosProcesar,successProcesar,errorProcesar);
			       }  
			     
	    	     else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		      });
		}
	}
	
	
	
});