angular.module('ModuloFFM').controller('IngresarFamiliaFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	var ingresarFamiliaFFM = this;            
	ingresarFamiliaFFM.formData = {};
	ingresarFamiliaFFM.winID=bcsComunes.ventanaWinId($scope);
	ingresarFamiliaFFM.datosAntes = {};
	ingresarFamiliaFFM.datosDespues = {};
	
	var conf = bcsConfiguracionFFM.getConsultaFamiliasFondosMutuos();
	
	$scope.parametros = bcsComunes.ventanaParams(ingresarFamiliaFFM.winID);
	ingresarFamiliaFFM.parametros = $scope.parametros;
	
	ingresarFamiliaFFM.buscarFamiliaFondo = function(){
		bcsComunes.ventanaOverlay(ingresarFamiliaFFM.winID,true);
    	var filter = {field: "IdFamiliaFondo", operator: "eq", value: $scope.parametros.IdFamiliaFondo};
		
    	conf.options.dataSource.query({filter:filter}).then(function(){
			if (conf.options.dataSource.data().length<1)
			{
				bcsComunes.ventanaOverlay(ingresarFamiliaFFM.winID,false);
				bcsComunes.ventanaCerrar(ingresarFamiliaFFM.winID);
				return false;
			}
			else
			{
				var datosFamiliaFondo = conf.options.dataSource.data()[0];
				ingresarFamiliaFFM.datosAntes = conf.options.dataSource.data()[0];
				ingresarFamiliaFFM.datosDespues = angular.copy(ingresarFamiliaFFM.datosAntes);
				bcsComunes.ventanaOverlay(ingresarFamiliaFFM.winID,false);
				return true;
			}
		});
    }
    
	if($scope.parametros.IdFamiliaFondo){
		bcsComunes.ventanaTitulo(ingresarFamiliaFFM.winID, $translate.instant("UI_MODIFICAR_FAMILIA_FFM"));
		ingresarFamiliaFFM.buscarFamiliaFondo();
	}else{
		bcsComunes.ventanaTitulo(ingresarFamiliaFFM.winID, $translate.instant("UI_INGRESAR_FAMILIA_FFM"));
	}
        
	ingresarFamiliaFFM.formData.ingresarFamiliaFondoMutuo = function(){
		bcsComunes.ventanaOverlay(ingresarFamiliaFFM.winID,true);
		var datosIngresoFamiliaFondoMutuo = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'FMM',
				p_UsaReglasNegocio : 'S',
				p_FamiliaFondo : [{
					IdInterno				:	0,
					IdFamiliaFondo			:	ingresarFamiliaFFM.datosDespues.IdFamiliaFondo,
					NombreFamilia			:	ingresarFamiliaFFM.datosDespues.NombreFamilia,
					IdEstado    			:	ingresarFamiliaFFM.datosDespues.IdEstado ? ingresarFamiliaFFM.datosDespues.IdEstado : ''
				}]
		};
		
		if($scope.parametros.IdFamiliaFondo){
			bcsServicioFondosMutuos.ModificarFamiliaFondoMutuo(datosIngresoFamiliaFondoMutuo).$promise.then(function(respuesta){
		     	 console.log(respuesta);
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdFamiliaFondo != '' && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, ingresarFamiliaFFM.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
		     	 }
	  		 },function(respuesta){
	  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
	  		 });
		}else{
			bcsServicioFondosMutuos.CrearFamiliaFondoMutuo(datosIngresoFamiliaFondoMutuo).$promise.then(function(respuesta){
		     	 console.log(respuesta);
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdFamiliaFondo != '' && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESO_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, ingresarFamiliaFFM.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
		     	 }
	  		 },function(respuesta){
	  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_FAMILIA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},ingresarFamiliaFFM.winID);
	  		 });
		}
		
	}
    
	 
    bcsComunes.ventanaAutoHeight(ingresarFamiliaFFM.winID);

	$('div#Win_IngresarFamiliaFFM').css("width", "400px");
});