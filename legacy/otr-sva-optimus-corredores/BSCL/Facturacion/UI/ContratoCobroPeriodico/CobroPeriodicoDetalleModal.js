angular.module('ModuloFacturacion').controller('CobroPeriodicoDetalleModalCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsCobroPeriodicoDetalleGridModal)
 {
	$scope.data	= {};	
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	$scope.datosEntrada = bcsComunes.ventanaParams(idVentana);
	var datosGrilla = [];

	$.each($scope.datosEntrada.Det, function(key,cob) { 
			datosGrilla.push(_.extend({},cob));
		});
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	
	
	//ASIGNA CAMPOS	
	$scope.data.oCobro = $scope.datosEntrada.NombreCobro;
	$scope.data.simbolooMontoDesde = $scope.datosEntrada.IdMoneda;
	$scope.data.simbolooMontoHasta = $scope.datosEntrada.IdMoneda;
	$scope.data.simbolooMontoCobro = $scope.datosEntrada.IdMoneda;	
	$scope.data.oMontoDesde = 0;
	$scope.data.oMontoHasta = 0;
	$scope.data.oMontoCobro = 0;
	$scope.data.oPorcentajeCobro = 0;
	$scope.data.activaroAgregarRangoCobrosPer = true;
	$scope.data.activaroQuitarRangoCobrosPer = true;

    
    //Asigna valor a campo PorRango desde la grilla de cobro
	if ($scope.datosEntrada.PorRango === "S")
	  $scope.data.oCobroPorRangos = true
	else
	  $scope.data.oCobroPorRangos = false;

	//Habilita o no boton crear rango de cobro, depende si fue anteriormente asignado rangos
	if($scope.datosEntrada.Det.length !== 0)
	  {
	   	 $scope.data.activaroAgregarCobrosPer = true;		  		
	  };

	//Deshabilita boton agregar cuando el cobro es sin rango
	if ($scope.datosEntrada.PorRango === "N" && $scope.datosEntrada.Det.length > 0)
	   {
		  $scope.data.activaroAgregarRangoCobrosPer = false;
	   }; 
	   
	
	//Deshabilita monto cobro y porcentaje cobro
	if ($scope.datosEntrada.TipoCobro === "F")
	   {
		  $scope.data.activaroMontoCobro = false;
		  $scope.data.activaroPorcentajeCobro = true;
	   }
	else
       {
       	  $scope.data.activaroMontoCobro = true;
		  $scope.data.activaroPorcentajeCobro = false;
       };
       
    //Deshabilita monto desde y hasta por tipo cobro fijo
	if ($scope.datosEntrada.PorRango === "N")
	   {
		  $scope.data.activaroMontoHasta = true;
		  $scope.data.activaroMontoDesde = true;
	   }
	else
       {
       	  $scope.data.activaroMontoHasta = false;
		  $scope.data.activaroMontoDesde = false;
       };   
		
		
	
	//GRILLAS
	//Rango Cobros Detalle	
	var defGridCobroPeriodicoDet = {
		 		          type : "array",
				          definition : {                                         
				        	             data  : datosGrilla,                                        
                                         schema : bcsCobroPeriodicoDetalleGridModal.modeloCobroPeriodicoDet(),

    			                       }
		};	

    var dsGridCobroPeriodicoDet = new bcs.DataSource(defGridCobroPeriodicoDet);    
	$scope.confBasicaCobroDet = { 
		dataSource          : dsGridCobroPeriodicoDet,//new bcs.DataSource(defGridCobroPeriodicoDet),		
		columns				: bcsCobroPeriodicoDetalleGridModal.columnasCobroPeriodicoDet(),
		autoBind			: true,
		sortable			: true,
		pageable			: true,
		userSettings        : true,
		height				: 150,
		selectable 			: "multiple row"
		
	};
	
	

	
	
	//OPCIONES
	//**************************************
	// Agrega rango de cobros a la grilla 
	//**************************************
	$scope.agregarCobrosPeriodicoGrilla = function()
	  {		    	
		 	
	  	if(!$scope.data.oMontoDesde && $scope.data.oMontoDesde !== 0){
				bcsComunes.msg("error","Error","Debe ingresar monto desde");
				return;
			};
	  	
	  	if(!$scope.data.oMontoHasta && !$scope.data.activaroMontoHasta)
	  	    {
				bcsComunes.msg("error","Error","Debe ingresar monto hasta");
				return;
			};
		
		if($scope.data.oMontoDesde > $scope.data.oMontoHasta)
		 {	
			bcsComunes.msg("error","Error","Monto hasta no puede ser mayor a monto desde");	
			return;
		 };	
		 
		
		if (!$scope.data.activaroMontoCobro && !$scope.data.oMontoCobro)
		 {
			bcsComunes.msg("error","Error","Debe ingresar monto del cobro");
		    return;
		 };
		 
		if (!$scope.data.activaroPorcentajeCobro && !$scope.data.oPorcentajeCobro)
		 {
			bcsComunes.msg("error","Error","Debe ingresar porcentaje del cobro");
		    return;
		 }; 
			
			
	  	$scope.gridApiCobroDet.dataSource.add(
        			 {   
        			   MontoDesde : $scope.data.oMontoDesde,
					   MontoHasta : $scope.data.oMontoHasta,
					   MontoCobroPeriodico : $scope.data.oMontoCobro || 0,
					   PorcentajeCobroPeriodico : $scope.data.oPorcentajeCobro || 0,
        			 });
	  	
	  	//Habilita boton crear cuando ya se ha ingresado un rango
	  	if($scope.gridApiCobroDet.dataSource.data().length > 0)
	  	  {
	  		 $scope.data.activaroAgregarCobrosPer = true;		  		
		  };	


		//Deshabilita boton agregar cuando el cobro es sin rango
		if ($scope.datosEntrada.PorRango === "N" && $scope.gridApiCobroDet.dataSource.data().length > 0)
	      {
		    $scope.data.activaroAgregarRangoCobrosPer = false;
	      }; 
	 };
	 
	 
	 //**************************************
	// Quita rango de cobros a la grilla 
	//**************************************
	$scope.quitarCobrosPeriodicoGrilla = function()
	  {		    	
		
		if ($scope.seleccionCobroDet === undefined)
		{	
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_RANGO_COBRO'));
			return;
		};
    	
    	dsGridCobroPeriodicoDet.remove($scope.seleccionCobroDet);
    	
    	if ($scope.gridApiCobroDet.dataSource.data().length === 0)
       	 {
    	    $scope.data.activaroAgregarRangoCobrosPer = true;	
    	    $scope.data.activaroAgregarCobrosPer = false;
    	 };
	  	
	  };
	 	 
	 
	 
	 
	//***************************************************
	// Agrega detalle de rango por cobro y lo devuelve 
	//***************************************************
	$scope.data.agregarCobrosPeriodicoDet = function()
	 {
		var v_CobroPeriodicoDetalleACrear = [];		
		var v_dsGridCobroPeriodicoDet = $scope.gridApiCobroDet.dataSource.data()|| [];
	    $.each(v_dsGridCobroPeriodicoDet,function(key,value){	
		        var ObjCobroPeriodicoDet = {
			  	                             NumeroContratoCobroPeriodico : 1,
			  	                             IdCobro : $scope.datosEntrada.IdCobro,
                                             MontoDesde : value.MontoDesde,
                                             MontoHasta : value.MontoHasta,
                                             MontoCobroPeriodico : value.MontoCobroPeriodico || 0,
                                             PorcentajeCobroPeriodico : value.PorcentajeCobroPeriodico || 0
		 	                              };
		        v_CobroPeriodicoDetalleACrear.push(ObjCobroPeriodicoDet);
	    });
	    
	    bcsComunes.ventanaData(idVentana,v_CobroPeriodicoDetalleACrear);
	    bcsComunes.ventanaCerrar(idVentana);
	 };
	 
	 
	 
 });	