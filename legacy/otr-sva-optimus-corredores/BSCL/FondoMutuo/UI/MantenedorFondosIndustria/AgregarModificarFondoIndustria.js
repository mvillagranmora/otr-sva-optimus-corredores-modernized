angular.module('ModuloFFM').controller('AgregarModificarFondoIndustriaCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	
	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.titulo = "";
	$scope.data.cmbbxMoneda		= {};
	
	$scope.parametrosEntrada = bcsComunes.ventanaParams($scope.winID);
	
	$scope.data.cmbbxMoneda.filtros = {
		complejo: [],
		filters: [
			{
				logic: "or",
				filters: [
					{ field: "IdMoneda", operator: "eq", value: 'CLP' },
					{ field: "IdMoneda", operator: "eq", value: 'USD' }
				]
			}
		]
	}
	
	$scope.inicio= function (){
		if($scope.parametrosEntrada.accion =='MOD'){
			$scope.titulo = "MODIFICAR FONDO";
			$scope.data.cmbbxMoneda.IdMonedaDefecto = $scope.parametrosEntrada.fondo.IdMoneda;		
			$scope.modificar($scope.parametrosEntrada.fondo);
		}
	}
	
	$scope.modificar = function (fondo){	
		$scope.data.IdFondoIndustria = fondo.IdFondoIndustria;	
		$scope.data.rutFFMM = fondo.RunFFMM;
		$scope.data.nombreFFMM = fondo.NombreFFMM;
		$scope.data.sociedadAdministradora = fondo.SociedadAdmFFMM;
		$scope.data.rutSociedadAdministradora = fondo.RutSociedadAdmFFMM;
		$scope.data.IdMoneda = fondo.IdMoneda;	
	}
	
	$scope.guardar = function (){		
		var mensajeCarga = $scope.validaDatos();	
		bcsComunes.ventanaOverlay($scope.winID,true);	
		
		if(mensajeCarga.length>0){			
			bcsComunes.ventanaOverlay($scope.winID,false);	
			bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga} ,false,false,$scope.winID);
		}else{			
			objFondos = $scope.crearObjeto($scope.parametrosEntrada.accion =='MOD'?'MOD':'ADD','ACT');			
			$scope.callActualizarFondos(objFondos);
		}
	}
	
	
	
	$scope.callActualizarFondos = function (objFondos){
		bcsComunes.ventanaOverlay($scope.winID,true);	
		bcsServicioFondosMutuos.ActualizaFondosIndustria(objFondos).$promise.then(function(respuesta){
		    var mensajeCarga = new Array();	
	 	 	if(respuesta.P_OUT_MENSAJES.length > 0){
				
				$.each(respuesta.P_OUT_MENSAJES,function(key,value){
					 var objCarga = {IdInterno : value.id,tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,codigo : value.codigo,descripcion : value.descripcion};
					 mensajeCarga.push(objCarga);
				});
			}
			if(respuesta.P_OUT_MENSAJES!==undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
				$scope.limpiarVentana();
				bcsComunes.ventanaCerrar($scope.winID);	
			}
			
			bcsComunes.ventanaOverlay($scope.winID,false);	
			bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeCarga} ,false,false,$scope.winID);
	 	});

	}
	
	$scope.crearObjeto = function (accion , estado){
		return	{
					p_IdEmpresa:$scope.SesionIdEmpresa,
					p_IdUsuario:$scope.SesionIdUsuario,
					p_IdModulo:'FMM',
					p_Accion:accion,
					p_FondosIndustria:[
						{
							IdFondoIndustria: accion == "ADD" ? 0 : $scope.data.IdFondoIndustria,
							RunFFMM:$scope.data.rutFFMM,
							NombreFFMM:$scope.data.nombreFFMM,
							SociedadAdmFFMM:$scope.data.sociedadAdministradora,
							RutSociedadAdmFFMM:$scope.data.rutSociedadAdministradora,
							IdEstado:estado,
							IdMoneda: $scope.data.IdMoneda
						}
					]
				}
	}
	
	$scope.limpiarVentana = function (){
		$scope.data.rutFFMM= '';
		$scope.data.nombreFFMM= '';
		$scope.data.sociedadAdministradora= '';
		$scope.data.rutSociedadAdministradora= '';
	}
	
	
	
	$scope.validaDatos = function (){
		var mensajeCarga = new Array();		
		if(!$scope.validaRut($scope.data.rutFFMM)){
			var objCarga = {IdInterno : 1,tipo : 'ERROR', codigo : 'UI_RUT_FFMM_INVALIDO',descripcion : 'Rut fondo no valido'};
			mensajeCarga.push(objCarga);
			
		}
		if(!$scope.validaRut($scope.data.rutSociedadAdministradora)){
			var objCarga = {IdInterno : 2,tipo : 'ERROR',codigo : 'UI_RUT_ADMINISTRADORA_INVALIDO',descripcion : 'Rut administradora no valido'};
			mensajeCarga.push(objCarga);
		}
		
		return mensajeCarga;		
	}
	
	
	//VALIDA RUT
	$scope.validaRut = function (rutCompleto) {
		rutCompleto = rutCompleto.replace("‐","-");
		if (!/^[0-9]+[-|‐]{1}[0-9kK]{1}$/.test( rutCompleto ))
			return false;
		var tmp 	= rutCompleto.split('-');
		var digv	= tmp[1]; 
		var rut 	= tmp[0];
		if ( digv == 'K' ) digv = 'k' ;
		
		return ($scope.modulo(rut) == digv );
	}
	
	$scope.modulo = function(T){
		var M=0,S=1;
		for(;T;T=Math.floor(T/10))
			S=(S+T%10*(9-M++%6))%11;
		return S?S-1:'k';
	}
	
	$scope.cancelar = function (){
		bcsComunes.ventanaCerrar($scope.winID);	
	}
	
	$scope.inicio();
	
	
});
