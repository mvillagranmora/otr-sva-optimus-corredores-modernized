angular.module('ModuloPlataforma').controller('CambioClaveUsuarioReplicaCtrl', 
        function($scope,bcsComunes,$translate,$resource){
    
    $scope.data = {};   
    $scope.configData = {};
    $scope.recordData = {};
    $scope.configData.overlay = bcsComunes.overlay;
    $scope.configData.bloqueoCambioClave = false;
    $scope.configData.clavesIguales = true; 
    $scope.configData.Mensajes={};
    $scope.configData.SonIguales=" ";
    $scope.isButtonEnabled = false;
    

    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
    $scope.parametros = bcsComunes.ventanaParams(idVentana);
    
    $scope.limpiarPantalla = function(){
		$scope.data.codigoUsuario = '';
        $scope.data.clave='';
        $scope.data.confirmacionClave='';
        
    };
    
    function validarSiExisten(){
        if($scope.data.confirmacionClave && $scope.data.clave){
            return true
        }
        return false
    }

    function validarCampos(){
        if(($scope.data.confirmacionClave===$scope.data.clave) && ($scope.data.confirmacionClave.trim() != '') && ($scope.data.clave.trim() != '')){
            return true
        }
        return false
    }

    $scope.$watch('data.confirmacionClave', function(nval,oval){
        if(nval!=$scope.data.clave){
            $scope.configData.clavesIguales = true;
            $scope.isButtonEnabled = false;
            $scope.configData.SonIguales="UI_CLAVES_NO_IGUALES ";
        }else{
            $scope.configData.clavesIguales = false;
            $scope.isButtonEnabled = false;
            $scope.configData.SonIguales=" ";
            if(validarSiExisten()){
                if(validarCampos()){
                $scope.isButtonEnabled = true;
            }
        }}
    });
    $scope.$watch('data.clave', function(nval,oval){
        $scope.isButtonEnabled = false;
        if(validarSiExisten()){
            if(validarCampos()){
                $scope.isButtonEnabled = true;
            }
        }
    });
    
    $scope.cambiarClave = function(){
        console.log($scope.configData.bloqueoCambioClave);
        if($scope.configData.bloqueoCambioClave==false){
            
            $scope.configData.bloqueoCambioClave=true;
            var urlCambioClave = "/BSCL/Plataforma/GWJAVA/PLA/call?t=764";
            var r = $resource("", {},
            {
                'callCambioClave': { method: "POST", url: urlCambioClave}
            });
            
            var datosUsuario= {
                p_IdEmpresa : $scope.SesionIdEmpresa,
				p_CodigoUsuario: $scope.data.codigoUsuario,
                p_ClaveNueva: $scope.data.clave,
                
            };

            var post = r.callCambioClave(datosUsuario);
            
            post.$promise.then(function(result){
                if(result.P_OUT_UPDATE!=null && result.P_OUT_UPDATE==='ok+++'){
                    bcsComunes.msg("info",'Info',$translate.instant("UI_CLAVE_ACTUALIZADA_OK"));
                    bcsComunes.ventanaCerrar(idVentana);
                    $scope.limpiarPantalla();
                }else{                  
                    $scope.configData.Mensajes= result.P_OUT_ERRORESDETALLE;
                    
                    $scope.configData.bloqueoCambioClave = false;
                }
            });
            
        }
        
    };

    var firstHeightCrear = $('#Win_CambiarClaveUsuarioReplica');
        firstHeightCrear.css('height','220px');
    
});