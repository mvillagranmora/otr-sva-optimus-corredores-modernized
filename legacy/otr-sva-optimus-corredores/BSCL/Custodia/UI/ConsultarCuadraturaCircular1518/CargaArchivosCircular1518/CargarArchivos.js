angular.module('ModuloCustodia').controller('cargarArchivoL0002DCVCtrl',  
    function(   $scope,
                bcsComunes,
                //bcsAlerta, 
                //OPCIONES_BCSALERTA, 
                ///$translate,
                bcsServiciosCustodia
            ){
	
	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.idVentana = idVentana;
	let parametros = bcsComunes.ventanaParams (idVentana);
    $scope.FechaCuadraturaLabel = parametros.FechaCuadratura;
    let TipoArchivoCarga = parametros.TipoArchivoCarga;
    $scope.TipoArchivoCarga = TipoArchivoCarga;
    $scope.TituloPrevioCarga = parametros.TituloPrevioCarga;
    $scope.formData = {};
    $scope.formData.SobreescribeArchivo = 'N';
    $scope.formData.matriz = {};
	$scope.formData.check = {};
	$scope.formData.checkDisable = {}

    $scope.cargarValoresCheckBox = function (matriz, check, checkDisable){
        let valorSI = { CodNombre : 'SI'}
        let valorNO = { CodNombre : 'NO'}
        let arrayValores = [valorSI, valorNO];
        $.each(arrayValores,function(key,value){
            matriz[value.CodNombre] = value;
            check[value.CodNombre] = (value.CodNombre === 'NO') ? true : false;
            checkDisable[value.CodNombre] = matriz[key] || false;
        });
    }
    $scope.cargarValoresCheckBox($scope.formData.matriz, $scope.formData.check, $scope.formData.checkDisable);

    $scope.$watchCollection('formData.check',function(newValue, oldValue){

        if(newValue && oldValue && JSON.stringify(newValue)!=JSON.stringify(oldValue)){
            var checkeado = false;
            $.each(newValue,function(key,value){	 
				if(value){
                    checkeado = true;
                    $scope.formData.SobreescribeArchivo = (key==="SI")?'S':'N';
                    $.each($scope.formData.check,function(key2,value2){
						if(key2 !== key){
							$scope.formData.check[key2] = false;
							$scope.formData.checkDisable[key2] = true;
						}
					});
				}
            });
            if(!checkeado){
                $.each($scope.formData.check,function(key,value){
					$scope.formData.check[key] = false;
					$scope.formData.checkDisable[key] = false;
                    $scope.formData.SobreescribeArchivo = 'N';
				})
			}
        }
	});
    
    $scope.callTocargarArchivo = function(){

        //VALIDA CASO ESPECIAL --- Siguiente if
        let cantidadNoChecked = 0;
        let cantidadChecBoxHTML = Object.keys($scope.formData.check).length; 
        let sobreescribeArchivo = 'NO';
        $.each( $scope.formData.check,function(key,value){
            if(value === false){ 
                cantidadNoChecked = cantidadNoChecked + 1;
            }
            if(value === true){
                sobreescribeArchivo = key;
            }
        });

        if(cantidadNoChecked !== cantidadChecBoxHTML){
            bcsComunes.ventanaOverlay($scope.idVentana,true);
            var codArch = "";
            var titulo = "";
            var datosArchivo = {};
            if(TipoArchivoCarga === "ARCHIVO_L02"){
                codArch = TipoArchivoCarga;
                titulo = 'Cuadratura de Custodia - Archivo L02';
                datosArchivo = {codigoArchivo: codArch, titulo: titulo};
                cargarArchivoL02(datosArchivo , $scope.formData.SobreescribeArchivo = (sobreescribeArchivo === 'SI')? 'S': 'N');
            }
            if(TipoArchivoCarga === "ARCHIVO_OTROS_SALDOS"){
                codArch = TipoArchivoCarga;
                titulo = 'Cuadratura de Custodia - Archivo Otros Saldos';
                datosArchivo = {codigoArchivo: codArch, titulo: titulo};
                cargarArchivoOtros(datosArchivo, $scope.formData.SobreescribeArchivo = (sobreescribeArchivo === 'SI')? 'S': 'N');
            } 
        }else{
			bcsComunes.msg("warning","Advertencia"," Seleccione una Opcion");
        }
    } 

    //Archivo L02
    let cargarArchivoL02 = function(datosArchivo, sobreEscribeArchivo){

        var ventanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,parametros.IdVentanaPadre,false);
        var ArchivoL02 = bcsComunes.ventanaPromesa(ventanaCargaArchivo);
        ArchivoL02.then(function(datosArchivos){
            bcsComunes.ventanaOverlay(idVentana,true);
            var datos = {};
            datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
            datos.p_IdUsuario =	$scope.SesionIdUsuario;
            datos.p_IdModulo = 'CUS';
            datos.p_SobreEscribeArchivo = sobreEscribeArchivo.trim();
            datos.p_FechaCuadratura = parametros.FechaCuadratura;
            datos.p_DatosArchivo = [];

            if(!datosArchivos || !datosArchivos.fileData){
                bcsComunes.ventanaOverlay(idVentana,false);
                return;
            }
            
            for(let i =0; i< datosArchivos.fileData.length; i++){
                if(datosArchivos.fileData[i].MENSAJEVALIDACION == 'OK') {
                    let posicion = {};
                    posicion.Cuenta = datosArchivos.fileData[i].CUENTA.trim();
                    posicion.Nemotecnico = datosArchivos.fileData[i].NEMO.trim();
                    posicion.PosicionDisponible = datosArchivos.fileData[i].POSICIONDISPONIBLE;
                    posicion.PosicionBloqueda = datosArchivos.fileData[i].POSICIONBLOQUEADA;
                    posicion.PosicionReventa = datosArchivos.fileData[i].POSICIONREVENTA;
                    posicion.PosicionRecompra = datosArchivos.fileData[i].POSICIONRECOMPRA;
                    posicion.Validacion = datosArchivos.fileData[i].MENSAJEVALIDACION;
                    datos.p_DatosArchivo.push(posicion);
                }
            }
            
            if(datos.p_DatosArchivo.length > 0){
                bcsServiciosCustodia.CargarArchivoL02(datos,
                    function(respuesta){	  		
                        bcsComunes.ventanaMsg({
                            conflictos:util.parseServerResponse(respuesta)
                        },$scope.idVentana);
                        bcsComunes.ventanaCerrar($scope.idVentana);
                        bcsComunes.ventanaOverlay(parametros.IdVentanaPadre,false);
                    },
                    function(respuesta){
                        bcsComunes.ventanaMsg({
                            conflictos:util.parseServerResponse(respuesta)
                        },$scope.idVentana);
                        bcsComunes.ventanaCerrar($scope.idVentana);
                        bcsComunes.ventanaOverlay(parametros.IdVentanaPadre,false);
                    }
                );
            }
            else{
                bcsComunes.ventanaOverlay(idVentana,false);
            } 
        });
    }

    //Archivo Otros Saldos
    let cargarArchivoOtros = function(datosArchivo, sobreEscribeArchivo){

		var ventanaCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,parametros.IdVentanaPadre,false);
		var ArchivoOtros = bcsComunes.ventanaPromesa(ventanaCargaArchivo);
		
		ArchivoOtros.then(function(datosArchivos){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			var datos = {};
			datos.p_IdEmpresa =	$scope.$root.SesionIdEmpresa;
		    datos.p_IdUsuario =	$scope.SesionIdUsuario;
		    datos.p_IdModulo = 'CUS';
            datos.p_SobreEscribeArchivo = sobreEscribeArchivo.trim();
			datos.p_FechaCuadratura = parametros.FechaCuadratura;
			datos.p_DatosArchivo = [];
			
			if(!datosArchivos || !datosArchivos.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
			
			for(let i =0; i< datosArchivos.fileData.length; i++){
				if(datosArchivos.fileData[i].MENSAJEVALIDACION == 'OK') {
					let posicion = {};
					posicion.Boveda = datosArchivos.fileData[i].BOVEDA.trim();
					posicion.Nemotecnico = datosArchivos.fileData[i].INSTRUMENTO.trim();
					posicion.PosicionDisponible = datosArchivos.fileData[i].POSICIONDISPONIBLE;
					posicion.Mercado = datosArchivos.fileData[i].MERCADO;
					posicion.Validacion = datosArchivos.fileData[i].MENSAJEVALIDACION;
					datos.p_DatosArchivo.push(posicion);
				}
			}
	        
	        if(datos.p_DatosArchivo.length > 0){
	        	bcsServiciosCustodia.CargarArchivoOtrosSaldos(datos,
					function(respuesta){	  		
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
                        bcsComunes.ventanaCerrar($scope.idVentana);
						bcsComunes.ventanaOverlay(parametros.IdVentanaPadre,false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
                        bcsComunes.ventanaCerrar($scope.idVentana);
						bcsComunes.ventanaOverlay(parametros.IdVentanaPadre,false);
					}
				);
	        }
	        else{
				bcsComunes.ventanaOverlay(idVentana,false);
			} 
		});
    }

    function changeHeight(height) {
        var firstHeightCrear = $('#Win_CargarArchivoCircular1518');
        firstHeightCrear.css('height', height);
      }

      $scope.$watch('formData.SobreescribeArchivo', function(newValue, oldValue) {
        if (newValue === 'S') {
            changeHeight('275px');
        } else {
            changeHeight('235px'); 
        }
      });

});


