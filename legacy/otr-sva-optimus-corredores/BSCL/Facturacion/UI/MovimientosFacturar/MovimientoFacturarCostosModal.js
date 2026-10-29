angular.module('ModuloFacturacion').controller('MovimientoFacturarCostosModalCtrl',
		       function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, $rootScope, 
		    		    bcsAlerta, OPCIONES_BCSALERTA,bcsMovimientoFacturarCostosModalGrid, bcsOperacionesComunes,$q)
{
   
    $scope.data	= {}; 
    var datosConsulta = {};
  
    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.   
    
    //Seleccionado desde grilla mvtofacturar (seleccionMovimientoFacturar)
    var dsSelecMvtoFact = bcsOperacionesComunes.RecuperarRegistrosAlmacenados();
    
    //Seteo de variables de sistema
    $scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;

	
    //Parametros para sp
    $scope.preparaParametros = function(filtros){
    	
    	var v_MvtoFactCostos = [];
    	datosConsulta = {};

    	//Recopila Id de movimientos seleccionados
      	$.each(dsSelecMvtoFact[0],function(key,value)
      	  { 
      		 v_MvtoFactCostos.push(
	      		           {   
	      			         IdPortafolio: dsSelecMvtoFact[0][key].IdPortafolio,
	      			         IdCliente   : dsSelecMvtoFact[0][key].IdCliente,
	      			         IdMovimientoComprobante : dsSelecMvtoFact[0][key].IdMovimientoComprobante,
	      			         IdTipoComprobante : dsSelecMvtoFact[0][key].IdTipoComprobante,
	      		           });
      	   });
    	
		
		datosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		datosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		datosConsulta.p_IdModulo = "FAC";
		datosConsulta.p_simularOperacion = "N";
		datosConsulta.p_UsaReglasNegocio = "S";
		datosConsulta.p_EsIngresoAutomatico = "N";
		datosConsulta.P_MOVIMIENTO = v_MvtoFactCostos;
		
		return datosConsulta;
	};
      
    //DataSource llenada desde un sp
    var dsMovtoFactCos = new bcs.DataSource({
             type : "call",
             definition : {
   				             callTransaction 	: "3162",
   				             callResultSet      : "P_OUT_MVTOFACTCOSTOSAGRUP",
   				             schema : bcsMovimientoFacturarCostosModalGrid.modeloMovimientoFacturarCostosModal()
             }
	});
    
   
    
   
    //GRILLA PRINCIPAL
    $scope.data.confBasicaMovimientoFacturacionCostos = {
		dataSource          : dsMovtoFactCos, 		
		columns				: bcsMovimientoFacturarCostosModalGrid.columnasMovimientoFacturarCostosModal(),
		sortable			: true,	
		pageable			: false,
		height				: 200,
		width				: 300,
		selectable 			: "row",
		autoBind            : false,
		gridCreated         : function(grid) {
			                                   grid.dataSource.setCallParameters($scope.preparaParametros($scope.data));
			                                   grillaCobros = grid;
			                                 },
	    notEditableColumns : function(fila)
			                         		{
			                         			if (fila.ModificarValor === 'N' && fila.AumentarValor === 'N' && fila.DisminuirValor === 'N')  //Sólo se puede ingresar monto a garantizar, cuando tenga saldo en cta inversión
			                         			{			                         				
			                         				return ['MontoAActualizarVis', 'PorcentajeAActualizarVis'];
			                         			}
			                         		}	                                 
	  };
    
    
    /** Tipo de calculo **/
    $scope.$watch('data.oIndMonto',function(newval,oldval)
      {    		    
    	switch (newval)
	      {
	    	 case true: 
	    	   {
	    	      $scope.data.oIndPorcentaje = false; 
	    	      grillaCobros.hideColumn(2); //Oculta la columna Monto no editable
	    	      grillaCobros.showColumn(3); //Oculta la columna Monto no editable
	    	        
	    	      grillaCobros.showColumn(4); //Oculta la columna Porcentaje no editable
	    	      grillaCobros.hideColumn(5); //Muestra la columna Porcentaje editable
	    	   }	
	         break;        
	      }

        if ($scope.data.oIndMonto === false)
         {
           grillaCobros.showColumn(2); //Muestra la columna Monto no editable
    	   grillaCobros.hideColumn(3); //Oculta la columna Monto editable
         };  	  
    	   
    }); 
    
    $scope.$watch('data.oIndPorcentaje',function(newval,oldval)
      {
    	switch (newval)
    	  {
    	    case true:
    	      {  $scope.data.oIndMonto = false;
    	         grillaCobros.showColumn(2); //Muestra la columna Monto no editable
    	      	 grillaCobros.hideColumn(3); //Oculta la columna Monto editable
    	         
    	         grillaCobros.hideColumn(4); //Oculta la columna Porcentaje no editable
    	         grillaCobros.showColumn(5); //Muestra la columna Porcentaje editable
    	      }
 
            break;
          }

        if ($scope.data.oIndPorcentaje === false)
          {    
    	     grillaCobros.showColumn(4); //Muestra la columna Porcentaje no editable
    	     grillaCobros.hideColumn(5); //Oculta la columna Porcentaje editable*/

          };
    	
    });
    
    
    
    //*******************************************
	// Calcular cobros
	//*******************************************      
    function calculaCobros(v_IdEmpresa, v_Usuario, v_tipoCalculo, v_CostosCobroAgrupados, v_CostosCobro)
      {
    	 var datosActualizar = {};
    	 
    	 datosActualizar.p_IdEmpresa = v_IdEmpresa;
    	 datosActualizar.p_IdUsuario = v_Usuario;
    	 datosActualizar.p_IdModulo	 = 'FAC';
    	 datosActualizar.p_simularOperacion	= 'N';
    	 datosActualizar.p_UsaReglasNegocio	= 'S';
    	 datosActualizar.p_EsIngresoAutomatico = 'N';
    	 datosActualizar.p_TipoCalculo  = v_tipoCalculo;
    	 datosActualizar.P_MOVIMIENTO   = v_CostosCobro;    	 
	     datosActualizar.P_COBRO        = v_CostosCobroAgrupados;
	     
	     var successActualizar = function(respuesta)
	      {
	    	  if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
	    	   {	
	    		  //Se actualiza los datos de la grilla
	    		  var vMsg = bcsComunes.ventanaMsg({
	    		    	                             conflictos:util.parseServerResponse(respuesta)
	    		        	                       },idVentana);
	    		      
	    		  bcsComunes.ventanaPromesa(vMsg).then(function() {
	    			                                                bcsComunes.ventanaCerrar(idVentana);
	    			                                              });
	    		 
			   }
	    	  else
	    	    {
	    		   if (respuesta.P_OUT_ERRORESCONTADOR > 0)
	    			 {
	    			    bcsComunes.ventanaMsg({ 
	    			    	                     conflictos:util.parseServerResponse(respuesta)
	    		        	                  },idVentana);
	    		      
	    		        
		             }
	    		   else
	    			 if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0)
	    			   {	 
	    				 var vMsg = bcsComunes.ventanaMsg({
	    		    	                                    conflictos:util.parseServerResponse(respuesta)
	    		        	                              },idVentana);
	    			   }
	    	    }
	    		
	    	  $scope.data.btnActivos  = true;
	    	  bcsComunes.ventanaOverlay(idVentana, false);
	      };
	      
	      var errorActualizar = function(respuesta)
	         {	
			   bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
			   $scope.data.btnActivos  = true;     //Activa botones
			   bcsComunes.ventanaOverlay(idVentana, false);
	    	 };
	      
	      //Llamada a servicio para crear comprobante de facturación()
	      bcsServiciosFacturacion.CalcularMovimientoComprobanteCobrosCostos(datosActualizar,successActualizar,errorActualizar);	 
	  };
    
    
    
    
	$scope.data.actualizarCostos = function() {
		
		//Valida que se hayan ingresado valores a actualizar

		if (!$scope.seleccionMovimientoCostos === undefined)
		  {
			 $scope.data.btnActivos   = true;     //Activa botones
			 bcsComunes.msg("warning","warning",$translate.instant('UI_INGRESAR_COSTOS'));
			 return;
		  }
		else
		  {
		  	 //Mensaje de confirmación
			 var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_MOVIMIENTO_COSTOS"), idVentana);

			 t.then(function(result)
		      {
		      	 if (result===OPCIONES_BCSALERTA.SI)
			       {
			       	  bcsComunes.ventanaOverlay(idVentana, true);
		              $scope.data.btnActivos  = false;
	               

      	              //Almacena datos de cobros de grilla agrupados
			    	  var v_CostosCobroAgrupados = [];	
			    	  var v_CostosCobro = [];
			    	  var v_CostosCobroParc = [];   
			    	   	  
		              var v_dsMovtoFactCos = dsMovtoFactCos.data()|| [];

		              //Valida que se hayan realizados cambios en grilla para modificar
		              v_modificado = false;

		              $.each(v_dsMovtoFactCos,function(key,value)
		               {
		                  if ((value.MontoAActualizarVis !== null || value.PorcentajeAActualizarVis !== null) && (value.MontoAActualizarVis !== 0 || value.PorcentajeAActualizarVis !== 0))
		                    {
		               	      v_modificado = true;
		                    }
		               }); //$.each(v_dsMovtoFactCos,function(key,value)


		              if (v_modificado === false)
		               {
		               	 $scope.data.btnActivos   = true;     //Activa botones
			             bcsComunes.msg("warning","warning",$translate.instant('UI_INGRESAR_COSTOS'));
			             bcsComunes.ventanaOverlay(idVentana, false);
			             return;
		               } 


		              //Variable tipo calculo 1=Monto, 2=Porcentaje
			    	  v_tipoCalculo = 0;			    	  

      	              switch ($scope.data.oIndMonto)
      	               { 
      	                 case true: v_tipoCalculo = 1;
      	                 break;
      	               }  
      	              
      	              switch ($scope.data.oIndPorcentaje)
      	               {   
      	                 case true: v_tipoCalculo = 2;
      	                 break;
      	               }

      	              //Crea tablas para costos agrupados
      	              var promesas = [];
      	              
		              $.each(v_dsMovtoFactCos,function(key,value)
		            	{ 
		            	   //Almacena dataSource para cobros agrupados modificados
		            	   if (value.MontoAActualizarVis !== null && v_tipoCalculo === 1)	
		            	    {
		            	       var ObjCostosCobroAgrupados = { "IdCobro" : value.IdCobro,
		            			                               "MontoCobro" : value.MontoCobro,
		            			                               "MontoCobroNuevo" :value.MontoAActualizarVis,
		            			                               "PorcentajeCobroNuevo" : 0
		            			                             };
		                       v_CostosCobroAgrupados.push(ObjCostosCobroAgrupados);
		                       		                       

		                       //Recorrer movimientos buscando el cobro
		                       $.each(dsSelecMvtoFact[0],function(key,value2)
		                        {
		                    	   var promesaLocal = $q.defer();
		                    	   promesas.push(promesaLocal);
		                    	   
		                    	   //DataSource CL
		                    	   if ($rootScope.SesionLocale === 'CL') {
		                    		  var mvtoCmbteCobroDatasource = new bcs.DataSource({
				                           type: "odata",
				                           definition : { 
				                                          serverFiltering:true,
				                                          transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",dataType: "json"}},
				                                          schema : { 
				                                                     model : {
				                                                               fields : {
				                                                                           IdMovimientoComprobante	: { type: "number"},
				                                                                           IdEstado     			: { type: "string"}
				                                                                        }
				                                                             }
				                                                   },
				                                          filter: [
				                                                     { field  : "IdMovimientoComprobante", operator: "eq", value  : dsSelecMvtoFact[0][key].IdMovimientoComprobante },
				                                                     { field  : "IdEstado", operator: "eq", value  : 'VIG'},
				                                                     { field  : "IdCobro",  operator: "eq", value  : value.IdCobro},
				                                                     { field  : "EsRecalculable",  operator: "eq", value  : 'N'},
				                                                  ]
						                                }
					                         }); //mvtoCmbteCobroDatasource = new bcs.DataSource

					                         mvtoCmbteCobroDatasource.query().then(function(){
					                         	$scope.$apply(function() {
					                           
					                              if (typeof mvtoCmbteCobroDatasource.data()[0] !== 'undefined')
					                                {	
														var ObjCostosMvtoCobro = 
																{ 
																  IdMovimientoComprobanteCobro : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobanteCobro,
																  IdMovimientoComprobante : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobante,
					                                              IdCobro : mvtoCmbteCobroDatasource.data()[0].IdCobro,
					                                              MontoCobro :mvtoCmbteCobroDatasource.data()[0].MontoCobro
																};	
														v_CostosCobro.push(ObjCostosMvtoCobro);	
					                                };											
					                             
					                             promesaLocal.resolve();   
						                         }); //$scope.$apply 
					                         }); //mvtoCmbteCobroDatasource.query().then(function()		                    		   
		                    	   } //If Sesion
		                    	   else 
		                    	     {
		                    		   var mvtoCmbteCobroDatasource = new bcs.DataSource({
				                           type: "odata",
				                           definition : { 
				                                          serverFiltering:true,
				                                          transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",dataType: "json"}},
				                                          schema : { 
				                                                     model : {
				                                                               fields : {
				                                                                           IdMovimientoComprobante	: { type: "number"},
				                                                                           IdEstado     			: { type: "string"}
				                                                                        }
				                                                             }
				                                                   },
				                                          filter:{
				                          					logic: "and",
				                        				    filters: [
				                        				      {	logic: "or",
				                        					    filters: [
				                        					    	{ field : "ModificarValor",  operator: "eq", value  : 'S' },
				                        					    	{ field	: "AumentarValor", operator: "eq", value:  'S' }, 
				                        					    	{ field	: "DisminuirValor", operator: "lte", value: 'S'}
				                        					    ]
				                        					  }, 
				                        				      {	logic: "and",
				                        					    filters: [
				                        					    	{ field  : "IdMovimientoComprobante", operator: "eq", value  : dsSelecMvtoFact[0][key].IdMovimientoComprobante },
				                                                     { field  : "IdEstado", operator: "eq", value  : 'VIG'},
				                                                     { field  : "IdCobro",  operator: "eq", value  : value.IdCobro},
				                        					    ]
				                        				      } 
				                        				    ]
				                        				  }
				                           }
		                    		   }); //mvtoCmbteCobroDatasource = new bcs.DataSource

					                         mvtoCmbteCobroDatasource.query().then(function(){
					                         	$scope.$apply(function() {
					                           
					                              if (typeof mvtoCmbteCobroDatasource.data()[0] !== 'undefined')
					                                {	
														var ObjCostosMvtoCobro = 
																{ 
																  IdMovimientoComprobanteCobro : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobanteCobro,
																  IdMovimientoComprobante : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobante,
					                                              IdCobro : mvtoCmbteCobroDatasource.data()[0].IdCobro,
					                                              MontoCobro :mvtoCmbteCobroDatasource.data()[0].MontoCobro
																};	
														v_CostosCobro.push(ObjCostosMvtoCobro);	
					                                };											
					                             
					                             promesaLocal.resolve();   
						                         }); //$scope.$apply 
					                         }); //mvtoCmbteCobroDatasource.query().then(function()    
		                    		   
		                    		   
		                    	     };
		                    		   
		                        });	//$.each(dsSelecMvtoFact[0],function(key,value2)
		                       
		                       
		                       
		            	    } // if (value.MontoAActualizarVis !== null && value.MontoAActualizarVis !== 0 && v_tipoCalculo === 1)	
		            	  else
		            	    {
		            	    	if (value.PorcentajeAActualizarVis !== null && value.PorcentajeAActualizarVis !== 0 && v_tipoCalculo === 2)	
		            	          {
		            	             var ObjCostosCobroAgrupados = { "IdCobro" : value.IdCobro,
		            			                                     "MontoCobro" : value.MontoCobro,
		            			                                     "MontoCobroNuevo" : 0,
		            			                                     "PorcentajeCobroNuevo" : value.PorcentajeAActualizarVis
		            			                                   };
		                              v_CostosCobroAgrupados.push(ObjCostosCobroAgrupados);

		                              //Recorrer movimientos buscando el cobro
		                              $.each(dsSelecMvtoFact[0],function(key,value2)
		                               {
		                    	         var promesaLocal = $q.defer();
		                    	         promesas.push(promesaLocal);
		                                 var mvtoCmbteCobroDatasource = new bcs.DataSource({
		                                 type: "odata",
		                                 definition : { 
		                                                serverFiltering:true,
		                                                transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",dataType: "json"}},
		                                                schema : { 
		                                                            model : {
		                                                                       fields : {
		                                                                                   IdMovimientoComprobante	: { type: "number"},
		                                                                                   IdEstado     			: { type: "string"}
		                                                                                }
		                                                                    }
		                                                         },
		                                                filter: [
		                                                          { field  : "IdMovimientoComprobante", operator: "eq", value  : dsSelecMvtoFact[0][key].IdMovimientoComprobante },
		                                                          { field  : "IdEstado", operator: "eq", value  : 'VIG'},
		                                                          { field  : "IdCobro",  operator: "eq", value  : value.IdCobro},
		                                                          { field  : "EsRecalculable",  operator: "eq", value  : 'N'},
		                                                        ]
				                                      }
			                           }); //mvtoCmbteCobroDatasource = new bcs.DataSource

			                          mvtoCmbteCobroDatasource.query().then(function(){
			                          	$scope.$apply(function() {
			                           
			                               if (typeof mvtoCmbteCobroDatasource.data()[0] !== 'undefined')
			                                 {	
											 	var ObjCostosMvtoCobro = 
														{ 
														  IdMovimientoComprobanteCobro : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobanteCobro,
														  IdMovimientoComprobante : mvtoCmbteCobroDatasource.data()[0].IdMovimientoComprobante,
			                                              IdCobro : mvtoCmbteCobroDatasource.data()[0].IdCobro,
			                                              MontoCobro :mvtoCmbteCobroDatasource.data()[0].MontoCobro
														};	
												v_CostosCobro.push(ObjCostosMvtoCobro);	
			                                 };											
			                             
			                               promesaLocal.resolve();   
				                          }); //$scope.$apply 
			                            }); //mvtoCmbteCobroDatasource.query().then(function()
		                               });	//$.each(dsSelecMvtoFact[0],function(key,value2) 		                       
		            	          }
		            	    }  
		            	}); //$.each(v_dsMovtoFactCos,function(key,value)
		              	
		              var pppp = [];
		              $.each(promesas,function(key,value){
		              	pppp.push(value.promise);
		              })
		              $q.all(pppp).then(function(result){
		            	  calculaCobros($scope.data.IdEmpresa, $scope.data.IdUsuario, v_tipoCalculo, v_CostosCobroAgrupados, v_CostosCobro);
		            	  bcsComunes.ventanaOverlay(idVentana, false);
		            	  //bcsComunes.ventanaCerrar(idVentana);
		            	  
		              })

			       } //if (result===OPCIONES_BCSALERTA.SI)
			      else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		      }); //t.result.then(function(result)	
			
		  } //Else

	   }; //End


	   
	  
});   