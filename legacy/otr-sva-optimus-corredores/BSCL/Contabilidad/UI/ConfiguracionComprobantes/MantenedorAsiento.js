angular.module('ModuloContabilidad').controller('MantenedorAsientoCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource) {

	$scope.data = {};
	$scope.formData = {};
	$scope.inputs = {};
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objAsiento = $.extend({},parametrosVentana.Objeto);
	$scope.inputs.nombreTipoContabilidad = parametrosVentana.data;
	$scope.inputs.IdTipoContabilidad = parametrosVentana.comprobante.IdTipoContabilidad;
	$scope.inputs.debe = true;
	$scope.inputs.detallado = true;
	//$scope.inputs.nombreCuenta 	= $scope.selecionadoCuentas.NombreCuenta;
	$scope.inputs.ejercicio = util.date($scope.SesionFechaSistema).format("YYYY")
	
	if(parametrosVentana.Accion === "Modificar" || parametrosVentana.Accion === "Replicar")
	{
		$scope.inputs.orden					= objAsiento.Orden;			
		$scope.data.numeroCuentas			= objAsiento.IdCuentaContable;
		$scope.data.nombreCuentas			= objAsiento.IdCuentaContable;	
		//$scope.data.nombreCuentas 			= objAsiento.NombreCuenta;		
		//	$scope.data.bloqueoCuenta		 	= false;
		 
		
		switch(objAsiento.Formato)
		{
			case "D" :
				$scope.inputs.detallado = true;
				$scope.inputs.resumido = false;
				break;
			case "R":
				$scope.inputs.detallado = false;
				$scope.inputs.resumido = true;
				break;
		}
		switch(objAsiento.DebeHaber)
		{
			case "D" :
				$scope.inputs.debe = true;
				$scope.inputs.haber = false;
				break;
			case "H":
				$scope.inputs.debe = false;
				$scope.inputs.haber = true;
				break;
		}
		
	}
	
	/*if(parametrosVentana.Accion === "Replicar")
	{
		$scope.inputs.numeroCuenta = objAsiento.IdCuentaContable;
		$scope.inputs.nombreCuenta = objAsiento.NombreCuenta;
		
		switch(objAsiento.Formato)
		{
			case "D" :
				$scope.inputs.detallado = true;
				$scope.inputs.resumido = false;
				break;
			case "R":
				$scope.inputs.detallado = false;
				$scope.inputs.resumido = true;
				break;
		}
		switch(objAsiento.DebeHaber)
		{
			case "D" :
				$scope.inputs.debe = true;
				$scope.inputs.haber = false;
				break;
			case "H":
				$scope.inputs.debe = false;
				$scope.inputs.haber = true;
				break;
		}
	}*/

	$scope.$watch('inputs.detallado',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.resumido = false; break;					
			case false: $scope.inputs.resumido = true;  break; 
		}			
	});
 
	$scope.$watch('inputs.resumido',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.detallado = false; break;					
			case false: $scope.inputs.detallado = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.debe',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.haber = false; break;					
			case false: $scope.inputs.haber = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.haber',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.debe = false; break;					
			case false: $scope.inputs.debe = true;  break; 
		}			
	});

	$scope.$watch('inputs.cuentas',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.debe = false; break;					
			case false: $scope.inputs.debe = true;  break; 
		}			
	});
	
	
	$scope.$watch('data.nombreCuenta',function(newval,oldval){
		var tipodato = typeof newval;		 				
		if (tipodato === "string"  ){	 
			$scope.data.numeroCuentas	= newval;	  		
		}		
	});

	$scope.$watch('data.numeroCuentas',function(newval,oldval){
		var tipodato = typeof newval;		 				
		if (tipodato === "string"  ){	 
			$scope.data.nombreCuenta	= newval;	  		
		}		
	});
	
	$scope.btnGrabar = function(){	
		var dataList = [];
		var params = {};
		switch (parametrosVentana.Accion){
		
			case "Ingresar"	: 
				//asdasd
				params.p_IdEmpresa				=	$scope.SesionIdEmpresa;	       		
				params.p_IdUsuario          	=	$scope.SesionIdUsuario;
				params.p_IdModulo        		=	"CON";  		
				params.p_UsaReglasNegocio		=	"N";
				params.p_IdTipoContabilidad		=	$scope.inputs.IdTipoContabilidad;
				params.p_IdCuentaContable		=	$scope.selecionadoCuentas.Cuenta;
				params.p_Ejercicio				=	util.date($scope.SesionFechaSistema).format("YYYY");		
				params.p_Orden					= 	$scope.inputs.orden;	
				params.p_DebeHaber				= 	$scope.inputs.debe ? "D" : "H";
				params.p_TipoEvento				=	"S";
				params.p_Condicionado			=	"S";	
				params.p_OmiteCondicionCmbte	=	"N";	
				params.p_Estado					=	"V";
				params.p_Formato				=	$scope.inputs.detallado ? "D" : "R";
				$scope.call("12031", params);
				break;
				
			case "Modificar":  
				
				params.p_IdEmpresa				=	$scope.SesionIdEmpresa;	       		
				params.p_IdUsuario          	=	$scope.SesionIdUsuario;
				params.p_IdModulo        		=	"CON";  		
				params.p_IdDefinicionAsiento	=	objAsiento.IdDefinicionAsiento;				
				params.p_IdTipoContabilidad		=	$scope.inputs.IdTipoContabilidad;
				params.p_IdCuentaContable		=	$scope.selecionadoCuentas.Cuenta;
				params.p_Ejercicio				=	util.date($scope.SesionFechaSistema).format("YYYY");		
				params.p_Orden					= 	$scope.inputs.orden;		
				params.p_DebeHaber				= 	$scope.inputs.debe ? "D" : "H";
				params.p_TipoEvento				=	"S";
				params.p_Condicionado			=	"S";	
				params.p_OmiteCondicionCmbte	=	"N";	
				params.p_Estado					=	"V";
				params.p_Formato				=	$scope.inputs.detallado ? "D" : "R";
				$scope.call("12032", params);
				break; 
			
			case "Replicar"	:  
				
				params.p_IdDefinicionAsientoAntiguo	=	objAsiento.IdDefinicionAsiento;
				params.p_IdEmpresa					=	$scope.SesionIdEmpresa;	       		
				params.p_IdUsuario          		=	$scope.SesionIdUsuario;
				params.p_IdModulo        			=	"CON";  		
				params.p_UsaReglasNegocio			=	"N";
				params.p_IdCuentaContable			=	objAsiento.IdCuentaContable;
				params.p_Ejercicio					=	util.date($scope.SesionFechaSistema).format("YYYY");
				params.p_DebeHaber					=	$scope.inputs.debe ? "D" : "H";
				params.p_Formato					=	$scope.inputs.detallado ? "D" : "R";
				params.p_IdCuentaContable			=	$scope.selecionadoCuentas.Cuenta;
				
				$scope.call("12026", params);
				break;
		}
		
	};
	
	$scope.call = function(callTransaction,parameters) {
		var resultSet = [];
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		var post = r.Filtros(parameters);
		post.$promise.then(success_fn);
	};
	
	$scope.Ejecutivos = function() {
			
			if($scope.seleccionGrilla.IdEmpleadoSupervisor){
				var id1 = bcsComunes.
					ventanaAbrir("ConsultarEjecutivo",
							{IdEmpleadoSupervisor: $scope.seleccionGrilla.IdEmpleadoSupervisor},false,false,ventanaId); 
			    var promesa=bcsComunes.ventanaPromesa(id1);                               
			}
	};
	
	$scope.actualizarGrillaFiltrosCombo = function()
	{		
		$scope.inputs.nombreCuenta = $scope.selecionadoCuentas.Cuenta;	 				
	}
	
	 
	
	var success_fn = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_GRABAR_EXITO"));
			bcsComunes.ventanaData(idVentana,true)
			bcsComunes.ventanaCerrar(idVentana);
			
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
});
