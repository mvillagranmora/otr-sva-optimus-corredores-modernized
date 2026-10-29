(function() {
	angular.module('ModuloMesa').controller('ModalEditarEstado',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
				
		var ingSpot = this;
		
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		
		//Configurar al principio
		
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		ingSpot.data = {};
		
		ingSpot.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		
		//if(typeof ingSpot.datosEntrada.items === 'undefined'){
			//return;
		//} 

        $scope.data.oConfiguracionTipoProducto ={
			filtros : {
				IdTipoMesa : { "field"   : "IdTipoMesa","operator": "eq", "value" : "MDIN" }
			},
            idActual: false
		};
		
		
		//Combobox Tipo Instrumento
		$scope.data.oConfiguracionTipoInstrumento={
			filtros : {
				IdTipoInstrumento : { "field"  : "IdTipoInstrumento","operator": "eq", "value" : "" }
		    },
            idActual: false
		};
		
		
		$scope.data.oConfiguracionMonedaPrincipal={

			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
				Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
				OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 1 }
				
			}
		};
		
		$scope.data.oConfiguracionMonedaSecundaria={

				filtros : {
					IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value" : "" },
					Nemotecnico : { "field"   : "Nemotecnico","operator": "eq", "value" : '' },
					OrdenEnPar : { "field"   : "OrdenEnPar","operator": "eq", "value" : 2 }
					
				},

		};
		if(ingSpot.datosEntrada.items != undefined){ 
            //se setean los filtros
            bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_EDITAR_MONEDA_PRODUCTO"));
            $scope.data.IdTipoProductoMesaModulo = ingSpot.datosEntrada.items.IdTipoProductoMesaModulo;
            $scope.data.Nemotecnico = ingSpot.datosEntrada.items.Nemotecnico;
			$scope.data.IdMonedaPrincipal = ingSpot.datosEntrada.items.IdMonedaPrincipal;
            $scope.data.IdMonedaSecundaria = ingSpot.datosEntrada.items.IdMonedaSecundaria;
		
			$scope.data.estadoDefecto = ingSpot.datosEntrada.items.IdEstado;
            $scope.data.FeriadoMonedaPrincipal=ingSpot.datosEntrada.items.FeriadoMonedaPrincipal=='SI'?true:false;
        }else{
            bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_CREAR_MONEDA_PRODUCTO"));
        }
        
		$scope.data.oConfiguracionEstado={
				filtros: {
					NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value" : 'MonedasPorProducto' }
				},
		};
        
        var errorFeriadoMonedaPrincipal='';
        var validaFeriadoMonedaPrincipal = function (){
            if(($scope.data.FeriadoMonedaPrincipal == true && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento=='SPT' && $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico=='INTERB' && $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda=='USD' && $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda == 'CLP' )
                || ($scope.data.FeriadoMonedaPrincipal == true && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento=='SPT' && $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico=='DISTRI' && $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda=='USD' && $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda == 'CLP' )
                || ($scope.data.FeriadoMonedaPrincipal == true && $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento=='FWD' && $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico=='SECANO' && $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda=='USD' && $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda == 'CLP' )){
                
                return {valido:true, error:errorFeriadoMonedaPrincipal };
            }
            else if($scope.data.FeriadoMonedaPrincipal){
                errorFeriadoMonedaPrincipal='Spot Interbancario USD/CLP, Spot Distribuci�n USD/CLP � Forward Seguro de cambio nominal USD/CLP';
                return {valido:false, error:errorFeriadoMonedaPrincipal };
            }
            else{
                return {valido:true, error:errorFeriadoMonedaPrincipal };
            }
        }
		
		$scope.data.crearEditarMonedaPorProducto = function() {
            
            if(ingSpot.datosEntrada.items != undefined){//SI SE EDITA
                if($scope.data.oConfiguracionTipoInstrumento.seleccionado === 0 || $scope.data.oConfiguracionTipoInstrumento.seleccionado === false)
                    bcsComunes.msg("error","error",$translate.instant('UI_INSTRUMENTO_ERROR'));
                else if ($scope.data.oConfiguracionMonedaPrincipal.seleccionado === false )
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_PRINCIPAL_ERROR'));
                else if ($scope.data.oConfiguracionMonedaSecundaria.seleccionado === false )
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_SECUNDARIA_ERROR'));
                else if($scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda == $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda){
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDAS_IGUALES_ERROR'));     
                }else{
                    var resultadovalidaFeriadoMonedaPrincipal= validaFeriadoMonedaPrincipal();
                    if( !resultadovalidaFeriadoMonedaPrincipal.valido){
                        bcsComunes.msg("error","error"," Solo se puede seleccionar 'feriado moneda principal' para las combinaciones "+resultadovalidaFeriadoMonedaPrincipal.error);
                    }else{   
                        var tablaMonedaPorProducto = [];
                
                        var ObjMonedaPorProductoParaTabla = {
                            "IdInstrumento"         : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdInstrumento
                            ,"IdEmpresa"			: $scope.SesionIdEmpresa
                            , "Nemotecnico"   		: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico  
                            , "IdTipoProducto"		: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa
                            , "IdTipoInstrumento"	: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoInstrumento
                            , "IdMonedaPrincipal"   : $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda   
                            , "IdMonedaSecundaria"  : $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda          
                            , "IdEstado" 			: $scope.data.oConfiguracionEstado.seleccionado.IdEstado
                            , "FeriadoMonedaPrincipal":$scope.data.FeriadoMonedaPrincipal==true?'S':'N'  
                        };
                        tablaMonedaPorProducto.push(ObjMonedaPorProductoParaTabla);
                                                        

                        var ObjMonedaPorProducto = {
                        
                            p_IdEmpresa : $scope.SesionIdEmpresa,
                            p_IdUsuario : $scope.SesionIdUsuario,
                            p_IdModulo : ingSpot.datosEntrada.items.IdTipoProducto === 'MEFW' ? 'MFW' : 'MSP',
                            p_UsaReglasNegocio: "S",
                            p_MonedasPorProducto: [{
                                IdInstrumentoMoneda:ingSpot.datosEntrada.items.IdInstrumentoMoneda,
                                IdInstrumento:$scope.data.oConfiguracionTipoInstrumento.seleccionado.IdInstrumento ,
                                Nemotecnico: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico,
                                IdTipoProducto: ingSpot.datosEntrada.items.IdTipoProducto,
                                IdTipoInstrumento:$scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento,
                                IdMonedaPrincipal: $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda,
                                IdMonedaSecundaria: $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda,
                                IdEstado: $scope.data.oConfiguracionEstado.seleccionado.IdEstado,
                                FeriadoMonedaPrincipal: $scope.data.FeriadoMonedaPrincipal==true?'S':'N',
                            }]

                        };
                        
                        //El nombre de la funcion hace referencia al estado, pero se puede editar toda la informaci�n
                        bcsServiciosMesa.EditarEstadoMonedaPorProducto(ObjMonedaPorProducto).$promise.then(successEditarEstado).catch(error);
                    }
                    
                    }
            }else{//si se crea
                if($scope.data.oConfiguracionTipoInstrumento.seleccionado === 0 || $scope.data.oConfiguracionTipoInstrumento.seleccionado === false)
                    bcsComunes.msg("error","error",$translate.instant('UI_INSTRUMENTO_ERROR'));
                else if ($scope.data.oConfiguracionMonedaPrincipal.seleccionado === false )
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_PRINCIPAL_ERROR'));
                else if ($scope.data.oConfiguracionMonedaSecundaria.seleccionado === false )
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDA_SECUNDARIA_ERROR'));
                else if($scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda == $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda){
                    bcsComunes.msg("error","error",$translate.instant('UI_MONEDAS_IGUALES_ERROR'));
                }else{
                    var resultadovalidaFeriadoMonedaPrincipal= validaFeriadoMonedaPrincipal();
                    if(!resultadovalidaFeriadoMonedaPrincipal.valido){
                        bcsComunes.msg("error","error"," Solo se puede seleccionar 'feriado moneda principal' para las combinaciones "+resultadovalidaFeriadoMonedaPrincipal.error);
                    }else{
                        var tablaMonedaPorProducto = [];
                        var ObjMonedaPorProducto = {
                            "IdInstrumento"         : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdInstrumento
                            ,"IdEmpresa"			: $scope.SesionIdEmpresa
                            , "Nemotecnico"   		: $scope.data.oConfiguracionTipoInstrumento.seleccionado.Nemotecnico  
                            , "IdTipoProducto"		: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa
                            , "IdTipoInstrumento"	: $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoInstrumento
                            , "IdMonedaPrincipal"   : $scope.data.oConfiguracionMonedaPrincipal.seleccionado.IdMoneda   
                            , "IdMonedaSecundaria"  : $scope.data.oConfiguracionMonedaSecundaria.seleccionado.IdMoneda          
                            , "IdEstado" 			: 'ACT'//por defecto
                            , "FeriadoMonedaPrincipal": $scope.data.FeriadoMonedaPrincipal==true?'S':'N', 
                        };
                    tablaMonedaPorProducto.push(ObjMonedaPorProducto);
                        
                    var parametros = {};
                    parametros.p_IdEmpresa = $scope.SesionIdEmpresa,
                    parametros.p_IdUsuario = $scope.SesionIdUsuario,
                    parametros.p_IdModulo = $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProductoMesa === 'MEFW' ? 'MFW' : 'MSP',
                    parametros.p_MonedaPorProducto = tablaMonedaPorProducto;
                    
                    bcsComunes.log().info(JSON.stringify(parametros));	
                    bcsServiciosMesa.CrearMonedaPorProducto(parametros,successEditarEstado,error);
                    }
                    
                }
            }
		};
		
		var error = function(respuesta)
		{
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		};
		
		var successEditarEstado = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{		
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Codigo));
				});
			}
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);	
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};	
	});
}());