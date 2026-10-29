angular.module('ModuloContabilidad').controller('ConfiguracionTipoContabilidadCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource) {

	$scope.data = {};
	$scope.formData = {};
	$scope.inputs = {};
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	var objTipoComprobante = $.extend({},parametrosVentana.Objeto);
	
	$scope.inputs.tipoGeneracion_Automatica = true;
	$scope.inputs.tipoComprobante_Traspaso = true;
	
	$scope.inputs.reversa_Si = true;
	$scope.inputs.agrupa_Si	= true
	$scope.inputs.formato_Detallado = true;
	
	$scope.inputs.ejercicio = util.date($scope.SesionFechaSistema).format("YYYY")
	
	if(parametrosVentana.Accion === "Modificar")
	{
		$scope.inputs.numeroContabilidad = objTipoComprobante.IdTipoContabilidad;
		$scope.inputs.nombreTipoContabilidad = objTipoComprobante.Nombre;
		$scope.inputs.nemo = objTipoComprobante.Nemo;
		if(objTipoComprobante.TipoGeneracion === "A")
		{
			$scope.inputs.tipoGeneracion_Automatica = true;
			$scope.inputs.tipoGeneracion_Manual = false;
		}
		else
		{
			$scope.inputs.tipoGeneracion_Automatica = false;
			$scope.inputs.tipoGeneracion_Manual = true;
		}
		if(objTipoComprobante.Reversa === "S")
		{
			$scope.inputs.reversa_Si = true;
			$scope.inputs.reversa_No = false;
		}
		else
		{
			$scope.inputs.reversa_Si = false;
			$scope.inputs.reversa_No = true;
		}
		if(objTipoComprobante.Agrupa === "S")
		{
			$scope.inputs.agrupa_Si = true;
			$scope.inputs.agrupa_No = false;
		}
		else
		{
			$scope.inputs.agrupa_Si = false;
			$scope.inputs.agrupa_No = true;
		}
		if(objTipoComprobante.Formato === "D")
		{
			$scope.inputs.formato_Detallado = true;
			$scope.inputs.formato_Resumido = false;
		}
		else
		{
			$scope.inputs.formato_Detallado = false;
			$scope.inputs.formato_Resumido = true;
		}
	}
	
	if(parametrosVentana.Accion === "Replicar")
	{
		$scope.inputs.nombreTipoContabilidad = objTipoComprobante.Nombre;
		$scope.inputs.nemo = objTipoComprobante.Nemo;
		if(objTipoComprobante.TipoGeneracion === "A")
		{
			$scope.inputs.tipoGeneracion_Automatica = true;
			$scope.inputs.tipoGeneracion_Manual = false;
		}
		else
		{
			$scope.inputs.tipoGeneracion_Automatica = false;
			$scope.inputs.tipoGeneracion_Manual = true;
		}
		if(objTipoComprobante.Reversa === "S")
		{
			$scope.inputs.reversa_Si = true;
			$scope.inputs.reversa_No = false;
		}
		else
		{
			$scope.inputs.reversa_Si = false;
			$scope.inputs.reversa_No = true;
		}
		if(objTipoComprobante.Agrupa === "S")
		{
			$scope.inputs.agrupa_Si = true;
			$scope.inputs.agrupa_No = false;
		}
		else
		{
			$scope.inputs.agrupa_Si = false;
			$scope.inputs.agrupa_No = true;
		}
		if(objTipoComprobante.Formato === "D")
		{
			$scope.inputs.formato_Detallado = true;
			$scope.inputs.formato_Resumido = false;
		}
		else
		{
			$scope.inputs.formato_Detallado = false;
			$scope.inputs.formato_Resumido = true;
		}
	}

	$scope.$watch('inputs.tipoComprobante_Traspaso',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.tipoComprobante_Traspaso = true; break;					
			case false: $scope.inputs.tipoComprobante_Traspaso = true;  break; 
		}			
	});
 
	$scope.$watch('inputs.tipoGeneracion_Automatica',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.tipoGeneracion_Manual = false; break;					
			case false: $scope.inputs.tipoGeneracion_Manual = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.tipoGeneracion_Manual',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.tipoGeneracion_Automatica = false; break;					
			case false: $scope.inputs.tipoGeneracion_Automatica = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.reversa_Si',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.reversa_No = false; break;					
			case false: $scope.inputs.reversa_No = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.reversa_No',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.reversa_Si = false; break;					
			case false: $scope.inputs.reversa_Si = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.agrupa_Si',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.agrupa_No = false; break;					
			case false: $scope.inputs.agrupa_No = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.agrupa_No',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.agrupa_Si = false; break;					
			case false: $scope.inputs.agrupa_Si = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.formato_Detallado',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.formato_Resumido = false; break;					
			case false: $scope.inputs.formato_Resumido = true;  break; 
		}			
	});
	
	$scope.$watch('inputs.formato_Resumido',function(newval,oldval){
		switch (newval){
			case true:  $scope.inputs.formato_Detallado = false; break;					
			case false: $scope.inputs.formato_Detallado = true;  break; 
		}			
	});
	
	$scope.btnGrabar = function(){	
		var dataList = [];
		var params = {};
		switch (parametrosVentana.Accion){
		
			case "Ingresar"	: 
				dataList 						= $scope.obtenerParametrosEntrada();
				params.p_IdEmpresa 				= $scope.SesionIdEmpresa;
				params.p_IdUsuario 				= $scope.SesionIdUsuario
				params.p_IdModulo				= "CON";
				params.p_UsaReglasNegocio 		= 'N';	
				params.p_TabTipoContabilidad 	= dataList;
				$scope.call("12023", params, success_fn);
				break;
				
			case "Modificar":  
				dataList 						= $scope.obtenerParametrosEntrada();
				params.p_IdEmpresa 				= $scope.SesionIdEmpresa;
				params.p_IdUsuario 				= $scope.SesionIdUsuario
				params.p_IdModulo				= "CON";
				params.p_TabTipoContabilidad 	= dataList;
				$scope.call("12024", params, success_fn);
				break; 
			
			case "Replicar"	:  
				params.p_IdEmpresa 				= $scope.SesionIdEmpresa;
				params.p_IdUsuario 				= $scope.SesionIdUsuario
				params.p_IdModulo				= "CON";
				params.p_UsaReglasNegocio 		= 'N';	
				params.p_IdTipoContabilidad		= objTipoComprobante.IdTipoContabilidad;		
				params.p_Nombre					= $scope.inputs.nombreTipoContabilidad;		
				params.p_Nemo					= $scope.inputs.nemo;						
				params.p_Ejercicio				= $scope.inputs.ejercicio;	
				$scope.call("12027", params, success_fn_replicar);
				break;
		}
	};

	$scope.obtenerParametrosEntrada = function(){
		var dataList = [];
		var data = {};
		data.Nombre 			=	$scope.inputs.nombreTipoContabilidad;		
		data.Nemo 				=	$scope.inputs.nemo;
		data.Tipo 				= 	"T";
		data.Reversa 			=	$scope.inputs.reversa_Si ? 	"S" : "N";
		data.Agrupa 			=	$scope.inputs.agrupa_Si  ?	"S" : "N";
		data.Condicionado 		=	"S";
		data.TipoGeneracion 	=	$scope.inputs.tipoGeneracion_Automatica ? "A" : "M";
		data.IdEmpresa 			=	$scope.SesionIdEmpresa;
		data.Ejercicio 			=	$scope.inputs.ejercicio;
		data.Formato 			=	$scope.inputs.formato_Detallado ? "D" : "R";
		switch(parametrosVentana.Accion){
			case "Ingresar": data.Estado	= "I";break;
			case "Modificar": 
				data.Estado	= objTipoComprobante.Estado;
				data.IdTipoContabilidad	=	objTipoComprobante.IdTipoContabilidad;
				break;
		}
		dataList.push(data);
		return dataList;
	}
	
	$scope.call = function(callTransaction,parameters, success) {
		var resultSet = [];
		var urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t="+callTransaction;
		var r = $resource("", {},{
			'Filtros': { method: "POST", url: urlFiltros, async: false,}
		});
		var post = r.Filtros(parameters);
		post.$promise.then(success);
	};
	
	var success_fn = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var refrescar = false;
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_GRABAR_EXITO"));
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
	var success_fn_replicar = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
			var refrescar = false;
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_GRABAR_EXITO"));
			bcsComunes.ventanaData(idVentana,refrescar);
			bcsComunes.ventanaCerrar(idVentana);
		}
		else
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
	};
	
});
