(function() {
	angular.module('ModuloPortafolio').controller('CrearPortafolioMantenedorCtrl',
	function($scope,$translate,bcsComunes,servicioPortafolio,$timeout,bcsServicioPlataforma){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cportm 		 = this;
		cportm.idVentana	 = bcsComunes.ventanaWinId($scope);
		cportm.parametros = bcsComunes.ventanaParams(cportm.idVentana);
		cportm.domVentana = $("[ventanaid='"+cportm.idVentana+"']");
		
		
		cportm.win			= {};
		cportm.cmbbx		= {};
		cportm.fn			= {};
		cportm.grilla		= {};
		cportm.dataIFRS    	= {};
		cportm.datosElim 	= {};
		
		
		cportm.IdBotonAceptar = 'CrearPortafolioMantenedor_aceptar' + cportm.parametros.AccionBoton;
		

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
				
		cportm.win.boton ="UI_AGREGAR";
		bcsComunes.ventanaTitulo(cportm.idVentana, $translate.instant('UI_AGREGAR_PORTAFOLIO_MANTENEDOR'));
						
		/*********************************************
		 * DATASOURCES
		 *********************************************/
	
		cportm.domVentana.data("kendoWindow").setOptions({height: 120});

		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		cportm.fn.cancelar = function () {
			
			bcsComunes.ventanaData(cportm.idVentana, false);
			bcsComunes.ventanaCerrar(cportm.idVentana);
		};

		///hasta aca bueno falta hacia abajo
		
		cportm.fn.validacionMaximoCodigoPortafolio = async function() {

            if (cportm.datos.PropositoPortafolio === "PROPOSITOAPV"){
                return true;
            }
			try {
				const parametroMaximoCodigoPortafolio = await bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise(
					'POR.MaximoCodigoPortafolio',
					$scope.$root.SesionIdEmpresa,
					'SOCIONEGOCIO'
				);
	
				if (!parametroMaximoCodigoPortafolio.Valor){
					return true;
				}
	
				const maximoCodigoRegex = new RegExp(parametroMaximoCodigoPortafolio.Valor);
				return maximoCodigoRegex.test(cportm.datos.CodigoPortafolio);
			} catch (error) {
				console.log("ADMINISTRAR PORTAFOLIO", error)
				return true
			}
        }

		// PROCESO AGREGAR PORTAFOLIO
		cportm.fn.aceptar = async function () {  
			bcsComunes.ventanaOverlay(cportm.idVentana,true);
			var datosGuardar = {
				p_IdEmpresa 		: $scope.$root.SesionIdEmpresa, 
				p_IdUsuario 		: $scope.$root.SesionIdUsuario,
				p_IdModulo 			: "POR",
				p_UsaReglasNegocio	: "N",
				p_Opcion			: "INSERT",
				p_Portafolios		: [{
					IdPortafolioConfiguracionDefecto	: 0,           //Después será modificado en el servicio
					CodigoPortafolio				 	: cportm.datos.CodigoPortafolio,
					Proposito							: cportm.datos.PropositoPortafolio
				}]
			};
			
			const validacionMaximoCodigo = await cportm.fn.validacionMaximoCodigoPortafolio();

            if(!validacionMaximoCodigo){
                bcsComunes.msg("error", "DATOS PORTAFOLIO AUTOMATICO", $translate.instant('UI_ERROR_CODIGO_PORTAFOLIO'));
                bcsComunes.ventanaOverlay(cportm.idVentana, false);
                return; 
            }

			servicioPortafolio.AdministrarPortafolioDefecto(datosGuardar).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(cportm.idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, cportm.idVentana);
						if(result.P_OUT_ERRORESCONTADOR === 0) {
							if(id){
								var promesa=bcsComunes.ventanaPromesa(id);                               
							    $.when(promesa).then(function(data){
							    	 $timeout(function() {
							    		 bcsComunes.ventanaCerrar(cportm.idVentana);
							    	 },20);
							     }); 
							}else{
								 bcsComunes.ventanaCerrar(cportm.idVentana); 
							 }
						}
					}
				},
				function(result){
					bcsComunes.ventanaOverlay(cportm.idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				}
			);
		
		};

		bcsComunes.ventanaTamanoAutomatico(cportm.idVentana,40);
	});
})();