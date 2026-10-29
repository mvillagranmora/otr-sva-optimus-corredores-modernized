angular.module('ModuloFacturacion').controller('EmisionFacturacionMasivaCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsGridSocioDeNegocioGrid)
{
	
	$scope.data	= {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';		
	$scope.data.oFechaMovimiento = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oInclusion  = true;	
	
	$scope.data.oClasificacionTipoInstrumento={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : { 	
				IdClasificacionTipoInstrumento : { field   : "IdClasificacionTipoInstrumento", operator: "eq", value   : "" },
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	
	$scope.data.oTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento : { field   : "IdClasificacionTipoInstrumento", operator: "eq", value   : "" },
				Comprobante                    : { field   : "Comprobante", operator: "eq", value   : "S" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oTipoComprobante={
			tipo : "combobox-tipo-comprobante",
			configuracion : {},
			filtros : {
			    IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oCondicionLiquidacion={
			tipo : "combobox-condicion-liquidacion",
			configuracion : {},
			filtros : {
			    EstadoTipoOperacionYCondicionDeLiquidacion : {field : "EstadoTipoOperacionYCondicionDeLiquidacion", operator : "eq", value : "ACT"},
			    IdTipoOperacion : {field : "IdTipoOperacion", operator : "eq" , value : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oBoveda={
		tipo : "combobox-boveda",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
		}
	};
	
	$scope.data.oSocioNegocio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa }
			},
	};
	
	
	//Grilla Socio de Negocio	
	var defGridSocioDeNegocio = {
		 		          type : "array",
				          definition : {                                         
				        	             dataGridSocioDeNegocio     : [],                                        
                                         schema : bcsGridSocioDeNegocioGrid.modeloSocioDeNegocio(),
    			                       }
		};
	
	var dsGridSocioDeNegocio = new bcs.DataSource(defGridSocioDeNegocio)	  	
	$scope.confBasicaSocioDeNegocio = { 
		dataSource          : dsGridSocioDeNegocio,		
		columns				: bcsGridSocioDeNegocioGrid.columnasSocioDeNegocio(),
		sortable			: true,
		userSettings        : true,
		height				: 250,
		selectable 			: "multiple row"
		
	};
	
	
	//***********************************************************
	// Limpiar
	//***********************************************************
	$scope.data.limpiar = function()
	  {
		 $scope.data.oClasificacionTipoInstrumento.idActual = 0;
		 $scope.data.oTipoInstrumento.idActual = 0;
		 $scope.data.oTipoComprobante.idActual = 0;
		 $scope.data.oCondicionLiquidacion.idActual = 0;		 
		 $scope.data.oFechaMovimiento = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		 $scope.data.oBoveda.idActual = 0;
		 $scope.data.oOmision = false;
		 $scope.data.activaroOmision = false;
		 $scope.data.oInclusion = true;
		 $scope.data.activaroInclusion = false;
		 $scope.data.oSocioNegocio.idActual = -1;
		 $scope.gridApiSocioDeNegocio.dataSource.read();
		 
	  }
	
	
	
	
	//***********************************************************
	// Asigna a grilla cliente(s) seleccionado (s)
	//***********************************************************
	$scope.agregarSocioNegocio = function()
	  { 
		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if ($scope.data.oSocioNegocio.seleccionado.IdSocioDeNegocio){
			var data = $scope.gridApiSocioDeNegocio.dataSource.data();
			var datoAgr = _.find(data, function(elem) {
				return ((elem.IdSocioDeNegocio === $scope.data.oSocioNegocio.seleccionado.IdSocioDeNegocio) && (elem.IdPortafolio === $scope.data.oSocioNegocio.seleccionado.IdPortafolio));
			});
			
			if(!datoAgr) {
				data.push(bcs.cleanData([$scope.data.oSocioNegocio.seleccionado])[0]);
			}
		}
		
		$scope.data.oSocioNegocio.idActual = -1;
		//$scope.data.oInclusion = true;
		
   	  };
   	  
   	  
   	//***********************************************************
  	// Quita a grilla cliente(s) seleccionado (s)
  	//***********************************************************
   	/*$scope.quitarSocioNegocio = function()
	  {
   		
   		//gridApiSocioDeNegocio.dataSource.remove(seleccionSocioDeNegocio)
   		if ($scope.seleccionSocioDeNegocio === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_SOCIO_NEGOCIO'));
			return;
		};
		
   		
   		dsGridSocioDeNegocio.remove($scope.seleccionSocioDeNegocio);
   		
   		if(dsGridSocioDeNegocio.data().length === 0){
   			$scope.data.oInclusion = false;	
   		}
 	  };*/  
   	
   	
   	
   	
   	//***********************************************************
	// Controla inclusión / omisión
	//***********************************************************
   	$scope.$watch('data.oInclusion', function(newVal, oldVal)
   	  {

   		 if ($scope.data.oInclusion === true)    		     
   		   {
   		     $scope.data.oOmision = false;
   		   }
   	  });


   	  $scope.$watch('data.oOmision', function(newVal, oldVal)
   	  {

   		 if ($scope.data.oOmision === true)    		     
   		   {
   		     $scope.data.oInclusion = false;
   		   }  
   	  });
   	  
   	  
	
   	//***************************************************************************
	// Selecciona movimientos comprobantes dependiendo de filtros seleccionados
	//***************************************************************************
   	$scope.data.crearComprobante = function()
   	  {
  		if (BCS_ESTADO_MODULO['FAC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
   		if(dsGridSocioDeNegocio.data().length > 0 && ($scope.data.oInclusion === false || $scope.data.oInclusion === undefined) && ($scope.data.oOmision === false || $scope.data.oOmision === undefined))
   		  {
   		     bcsComunes.msg("warning","warning",$translate.instant('Existen clientes seleccionados, debe marcar opción incluir u omitir'));
   		     return;
   		  } 
   		
   		
   		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_COMPROBANTE_FACTURACION"), idVentana);
   		t.then(function(result)
   		 {
			if (result===OPCIONES_BCSALERTA.SI)
			  {
			    var MovimientoFacturar = [];
		        bcsComunes.ventanaOverlay(idVentana, true);
		        $scope.data.btnActivos  = false;
      	   
			    //Recopila datos cliente de movimientos seleccionados	
		        var v_MvtoCmbteClienACrear = [];
		        var v_dsGridSocioDeNegocio = dsGridSocioDeNegocio.data()|| [];		        
		        
      	        $.each(v_dsGridSocioDeNegocio,function(key,value){	
		           var ObjMvtoClien = {
			  	                         "IdCliente" : value.IdCliente,
			  	                         "IdPortafolio" : value.IdPortafolio
			  	                      };

		           v_MvtoCmbteClienACrear.push(ObjMvtoClien);
		        });
      	        
    	   	    //Parámetros de entrada al servicio
	      	    var p_SelCliente = '';
	      	    
	      	    if ($scope.data.oInclusion === true)
	      	    	p_SelCliente          = 'I';
	      	    else
	      	      if ($scope.data.oOmision === true)	 
	      	    	  p_SelCliente       = 'O';
	      	      else
	      	    	  p_SelCliente       = 'N'; 
	      	    
	      	    var datosFacturar = {
	  				p_IdEmpresa : $scope.SesionIdEmpresa,
	  				p_IdUsuario : $scope.SesionIdUsuario,
	  				p_IdModulo  : 'FAC',
	  				p_TipoPrograma : [{
	  					IdInterno : 0,
	  					IdTipoPrograma : 'EmisionMasivaComprobante',
	  					SegundosDesfase : 0
	  				}],
	  				p_Parametro : [{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdEmpresa',
	  					Valor : $scope.SesionIdEmpresa
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdUsuario',
	  					Valor : $scope.SesionIdUsuario
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdModulo',
	  					Valor : 'FAC'
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdClasificacionTipoInstrumento',
	  					Valor : ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento == 0 ? '' : $scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdTipoInstrumento',
	  					Valor : ($scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento == 0 ? '' : $scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdTipoComprobante',
	  					Valor : ($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante == 0 ? '' : $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdCondicionLiquidacion',
	  					Valor : ($scope.data.oCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion == 0 ? '' : $scope.data.oCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_IdBoveda',
	  					Valor : ($scope.data.oBoveda.seleccionado.IdBoveda == 0 ? '' : $scope.data.oBoveda.seleccionado.IdBoveda)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_FechaMovimientoComprobante',
	  					Valor : util.dateSPdate($scope.data.oFechaMovimiento)
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_Glosa',
	  					Valor : 'Comprobante Generado Emisión Masiva'
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_SelCliente',
	  					Valor : p_SelCliente
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_simularOperacion',
	  					Valor : 'N'
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_UsaReglasNegocio',
	  					Valor : 'S'
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_EsIngresoAutomatico',
	  					Valor : 'N'
	  				},{
	  					IdInterno : 0,
	  					NombreParametro : 'p_MovimientoFacturarjson',
	  					ValorBlob : JSON.stringify({"p_MovimientoFacturarjson":v_MvtoCmbteClienACrear})
	  				}]
	      	  	}
	      	    
	      	    var successFacturar = function(respuesta)
	      	      {	
	      	    	bcsComunes.ventanaOverlay(idVentana, false);
	    		    if (respuesta.P_OUT_ERRORESCONTADOR == "0")
	    		      {	
	    		    	//Se actualiza los datos de la grilla	    			
	    		    	var fechaLocal=util.dateDS(respuesta.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
	    				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",respuesta.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
	    				conflictos : util.parseServerResponse(respuesta)}, $scope.winID);

                        
                        $scope.gridApiSocioDeNegocio.dataSource.read();	
                        $scope.data.limpiar();
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
	    	          }
	    		
	    		    $scope.data.btnActivos  = true;
	    	      };
	    	      
	    	      var errorFacturar = function(respuesta)
	    	        {	
				      bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
				      $scope.data.btnActivos  = true;     //Activa botones
				           
				      bcsComunes.ventanaMsg({
				    	                      mensaje:$translate.instant(""),
				    	                      conflictos:util.parseServerResponse(respuesta)
		                                    },idVentana);
	    	        };
	    	
	    	
	    	      //Llamada a servicio para crear comprobante de facturación()
	      	      bcsServiciosFacturacion.InscribirProcesoBatch(datosFacturar,successFacturar,errorFacturar);
			  }
			
			else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		 });
   		
   	  };	
   	
});