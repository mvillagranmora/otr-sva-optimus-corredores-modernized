/*************************************************************
 * Purpose	: Anotaciones Portafolio (Modal)
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('ModalPortafolioAnotacionesCtrl',function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){	
			const modPorAnot = this;
			const idVentana = bcsComunes.ventanaWinId($scope);
			const parametros = bcsComunes.ventanaParams(idVentana);
			$scope.modPorAnot = modPorAnot;
			
			$scope.camposRequeridos = {};
			modPorAnot.CodigoFormulario = parametros.CodigoFormulario;
			bcsServicioPlataforma.CamposRequeridosFormulario(modPorAnot.CodigoFormulario, function(respuesta){
				$scope.camposRequeridos = respuesta;
			});
			
			$scope.data = {};
			$scope.Combobox = {};
			
			$scope.Combobox.oConfMotivo={
					tipo 	: 'combobox-atributo-anotacion', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "MOTIVO" }},
				};
			
			$scope.Combobox.oConfRelevancia={
					tipo 	: 'combobox-atributo-anotacion', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "RELEVANCIA" }},
				};
			
			$scope.Combobox.oConfAmbito={
					tipo 	: 'combobox-atributo-anotacion', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "AMBITO" }},
				};
					
			if (parametros.Anotacion){ //modificacion
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_ANOTACION')); 
				$scope.data.MotivoAnotacionDefecto = parametros.Anotacion.IdMotivo;
				$scope.data.AmbitoDefecto = parametros.Anotacion.IdAmbito;
				$scope.data.RelevanciaDefecto = parametros.Anotacion.IdRelevancia;
				$scope.data.Titulo = parametros.Anotacion.Titulo;
				$scope.data.Detalle = parametros.Anotacion.Detalle;
				$scope.data.fechaDesde = parametros.Anotacion.ValidoDesde;
				$scope.data.fechaHasta = parametros.Anotacion.ValidoHasta;
			}
			else //AGREGAR
			{
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_ANOTACION')); 
			}
			
			
					
			const agregarAnotacionSP = function(v_Dato)
		    {
			       const urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=218";
			       const r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
			       const post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };
		    
		    const modificarAnotacionSP = function(v_Dato)
		    {
			       const urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=219";
			       const r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
			       const post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };
			
			$scope.Aceptar = function() {
				//modificacion
				if (parametros.Anotacion){
					bcsComunes.ventanaOverlay(idVentana,true);
					const parametrosSP = {};
					parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
			        parametrosSP.p_IdModulo = 'POR';
			        parametrosSP.p_UsaReglasNegocio = "N";
			        parametrosSP.p_IdPortafolio = parametros.IdPortafolio;
			        parametrosSP.p_IdAnotacion = parametros.Anotacion.IdAnotacion;
			        parametrosSP.p_Titulo = $scope.data.Titulo || '';
			        parametrosSP.p_Detalle = $scope.data.Detalle || '';
			        parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion || '';
			        parametrosSP.p_IdAmbito = $scope.data.Ambito || '';
			        parametrosSP.p_IdRelevancia = $scope.data.Relevancia || '';
			        parametrosSP.p_FechaDesde = ($scope.data.fechaDesde)? util.dateSPdate($scope.data.fechaDesde) : "0001-01-01";
			        parametrosSP.p_FechaHasta = ($scope.data.fechaDesde)?util.dateSPdate($scope.data.fechaHasta): "0001-01-01";
			        
			            	
			        modificarAnotacionSP(parametrosSP).then(
						function(result) {
							let titulo = "";
							if (result.P_OUT_ADVERTENCIASCONTADOR){
								titulo = "La operacion tiene advertencias";
							}else if (result.P_OUT_ERRORESCONTADOR){
								titulo = "La operacion tiene errores";
							}else {
								titulo = "Anotación modificada";
							}
							//si hay un archivo seleccionado subirlo
							const file = document.getElementById("file").files[0];
							if(file!=undefined){
								const formData = new FormData(document.getElementById("formSubirArchivo"));
								formData.append("entity", "ANOTACION_PORTAFOLIO");
								formData.append("name", file.name);
								formData.append("id", result.P_OUT_REFID);
								uploadFile(formData);
								desplegarResultado(titulo, result);
							}else{
								desplegarResultado(titulo, result);
							}

						},
						function(result){
							desplegarResultado("La operacion tiene errores", result);
						});	
				}
				//creacion
				else {
					bcsComunes.ventanaOverlay(idVentana,true);
					const parametrosSP = {};
			        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
			        parametrosSP.p_IdModulo = 'POR';
			        parametrosSP.p_UsaReglasNegocio = "N";
			        parametrosSP.p_IdPortafolio = parametros.IdPortafolio;
			        parametrosSP.p_Titulo = $scope.data.Titulo || '';
			        parametrosSP.p_Detalle = $scope.data.Detalle || '';
			        parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion || '';
			        parametrosSP.p_IdAmbito = $scope.data.Ambito || '';
			        parametrosSP.p_IdRelevancia = $scope.data.Relevancia || '';
			        parametrosSP.p_FechaDesde = ($scope.data.fechaDesde)? util.dateSPdate($scope.data.fechaDesde) : "0001-01-01";
			        parametrosSP.p_FechaHasta = ($scope.data.fechaDesde)?util.dateSPdate($scope.data.fechaHasta): "0001-01-01";
			        
			        agregarAnotacionSP(parametrosSP).then(
						function(result) {
							let titulo = "";
							if (result.P_OUT_ADVERTENCIASCONTADOR){
								titulo = "La operacion tiene advertencias";
							}else if (result.P_OUT_ERRORESCONTADOR){
								titulo = "La operacion tiene errores";
							}else {
								titulo = "Anotación creada";
							}
							//si hay un archivo seleccionado subirlo
							const file = document.getElementById("file").files[0];
							if(file!=undefined){
								var formData = new FormData(document.getElementById("formSubirArchivo"));
								formData.append("entity", "ANOTACION_PORTAFOLIO");
								formData.append("name", file.name);
								formData.append("id", result.P_OUT_REFID);
								uploadFile(formData);
								desplegarResultado(titulo, result);
							}else{
								desplegarResultado(titulo, result);
							}
						},
						function(result){
							desplegarResultado("La operacion tiene errores", result);
						});	
				}
			};

			const desplegarResultado = function(titulo, res){
					bcsComunes.ventanaMsg({
						mensaje: titulo,
						conflictos:util.parseServerResponse(res)
					},parametros.VentanaPadre);
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
			};

			const uploadFile = function(formData){
				$.ajax({
					method: "POST",
					url: "/BSCL/Plataforma/GWJAVA/PLA/FileBlobManager",
					data: formData,
					contentType : false,
					processData: false,
					cache: false
				}).done(function( msg ) {
					if(msg==="OK"){
						bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
					}else{
						bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
					}
					bcsComunes.ventanaOverlay(idVentana,false);
					document.getElementById('formSubirArchivo').reset();
				}).error(function (jqXHR) {
					console.log(jqXHR)
					bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
					bcsComunes.ventanaOverlay(idVentana,false);
					document.getElementById('formSubirArchivo').reset();
				});
			};
			
			$scope.Cancelar = function() {
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			};
	});
}());
//------------------------- FIN CONTROLLER --------------------------