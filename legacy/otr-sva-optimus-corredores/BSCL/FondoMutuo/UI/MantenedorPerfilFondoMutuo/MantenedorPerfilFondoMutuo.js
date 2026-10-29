angular.module('ModuloFFM').controller('MantenedorPerfilFondoMutuoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
	var administrarPerfilFondoMutuo = this;            
	administrarPerfilFondoMutuo.formData = {};
	administrarPerfilFondoMutuo.winID=bcsComunes.ventanaWinId($scope);
	
	var conf = bcsConfiguracionFFM.getDataSourcePerfilesFondoMutuo();		
	$scope.parametros = bcsComunes.ventanaParams(administrarPerfilFondoMutuo.winID);
	
	administrarPerfilFondoMutuo.formData.filtrosEmisor = {
			Key : { "field"   : "Key","operator": "eq", "value"   :  ''}
	};
	
	administrarPerfilFondoMutuo.buscarPerfil = function(){
		bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,true);
    	var filter = {field: "NombrePerfilConfiguracion", operator: "eq", value: administrarPerfilFondoMutuo.formData.IdKeyPerfil};
		
    	conf.query({filter:filter}).then(function(){
			if (conf.data().length<1)
			{
				console.log("Perfil no existe");
				bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,false);
				return false;
			}
			else
			{
				var datosSerieFondo = conf.data()[0];
				administrarPerfilFondoMutuo.formData.IdPerfil = conf.data()[0].IdPerfil;
				console.log("Serie si existe");
				bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,false);
				return true;
			}
		});
    }
    	
	$scope.$watch('administrarPerfilFondoMutuo.formData.IdKeyPerfil',function(newval,oldval){
		if(newval && newval != oldval && newval != ''){
			administrarPerfilFondoMutuo.buscarPerfil();
		}
	})
            
	administrarPerfilFondoMutuo.formData.administrarPerfilFondoMutuo = function(){
		bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,true);
		var datosAdministrarPerfilFondoMutuo = {
				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				p_IdModulo : 'FMM',
				p_UsaReglasNegocio : 'S',
				p_PerfilFondo : [{
					IdInterno					:	0,
					TipoPerfil					:	administrarPerfilFondoMutuo.formData.IdKeyPerfil,
					IdPerfil					:	administrarPerfilFondoMutuo.formData.IdPerfil,
				}]
		};
		

		bcsServicioFondosMutuos.AdministrarPerfilFondoMutuo(datosAdministrarPerfilFondoMutuo).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_PERFIL_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},administrarPerfilFondoMutuo.winID);
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, administrarPerfilFondoMutuo.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_PERFIL_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},administrarPerfilFondoMutuo.winID);
	     	 }
	     	bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,false);
  		 },function(respuesta){
  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_PERFIL_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},administrarPerfilFondoMutuo.winID);
  		   console.log('me cai jeje ' + respuesta);
  		   bcsComunes.ventanaOverlay(administrarPerfilFondoMutuo.winID,false);
  		 });
		
	}
	
	bcsComunes.ventanaTamanoAutomatico(administrarPerfilFondoMutuo.winID,10);
});