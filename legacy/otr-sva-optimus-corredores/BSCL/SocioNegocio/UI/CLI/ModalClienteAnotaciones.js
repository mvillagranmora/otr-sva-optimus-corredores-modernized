/*************************************************************
 * Purpose	: Anotaciones Cliente (Modal)
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalClienteAnotacionesCtrl',function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma, $http){	
			mta = this;
			var idVentana = bcsComunes.ventanaWinId($scope);
			var parametros = bcsComunes.ventanaParams(idVentana);
			$scope.data = {};

			$scope.CodigoFormulario = parametros.CodigoFormulario;
			$scope.camposRequeridos = {};
			bcsServicioPlataforma.CamposRequeridosFormulario($scope.CodigoFormulario, function(respuesta) {
				$scope.camposRequeridos = respuesta;
				asignarValoresPorDefecto();
			});

			const asignarValoresPorDefecto = function asignarValoresPorDefecto() {
				$.each($scope.camposRequeridos, function (key, value) {
					const noEsRequerido = value.Requerido === 'N';
					const esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					const codigoValorFrontend = value.CodigoValorFrontend;
					const esCampoEnBlanco = !$scope.data[codigoValorFrontend];

					if (noEsRequerido && esValorDefectoValido && esCampoEnBlanco) {
						let typedValue = value.ValorDefectoCampoFormulario;
						if (value.TipoDato === 'NUMBER') typedValue = parseInt(typedValue, 10);
						$scope.data[codigoValorFrontend] = typedValue;
					}
				});
			};

			$scope.Combobox = {};
			
			$scope.Combobox.oConfMotivo={
					tipo 	: 'combobox-atributo-anotacion-cliente', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "MOTIVO" }},
				};
			
			$scope.Combobox.oConfRelevancia={
					tipo 	: 'combobox-atributo-anotacion-cliente', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "RELEVANCIA" }},
				};
			
			$scope.Combobox.oConfAmbito={
					tipo 	: 'combobox-atributo-anotacion-cliente', 
					filtros : { 
						TipoAtributoAnotacion : {
								"field"   : "TipoAtributoAnotacion",
								"operator": "eq",
								"value"   : "AMBITO" }},
				};
					
			if (parametros.Anotacion) //modificacion
			{
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
			
			
					
			var agregarAnotacionSP = function(v_Dato)
		    {
			       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12091";
			       var r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
			       var post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };
		    
		    var modificarAnotacionSP = function(v_Dato)
		    {
			       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=12092";
			       var r = $resource
		         ("", {}, {
			                 'urlConsulta': { method: "POST", url: urlConsulta}
		                  }
		         );
			       var post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };

		$scope.Aceptar = function () {
			//modificacion
			if (parametros.Anotacion) {
				bcsComunes.ventanaOverlay(idVentana, true);
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;
				parametrosSP.p_IdModulo = 'POR';
				parametrosSP.p_UsaReglasNegocio = "N";
				parametrosSP.p_IdCliente = parametros.IdCliente;
				parametrosSP.p_IdAnotacion = parametros.Anotacion.IdAnotacion;
				parametrosSP.p_Titulo = $scope.data.Titulo;
				parametrosSP.p_Detalle = $scope.data.Detalle;
				parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion;
				parametrosSP.p_IdAmbito = $scope.data.Ambito;
				parametrosSP.p_IdRelevancia = $scope.data.Relevancia;
				parametrosSP.p_FechaDesde = ($scope.data.fechaDesde) ? util.dateSPdate($scope.data.fechaDesde) : "";
				parametrosSP.p_FechaHasta = ($scope.data.fechaDesde) ? util.dateSPdate($scope.data.fechaHasta) : "";


				modificarAnotacionSP(parametrosSP).then(
					function (result) {
						bcsComunes.ventanaOverlay(idVentana, false);
						var titulo = "";
						console.log($scope.data);
						if (result.P_OUT_ADVERTENCIASCONTADOR) {
							titulo = "La operacion tiene advertencias";
						} else if (result.P_OUT_ERRORESCONTADOR) {
							titulo = "La operacion tiene errores";
						} else {
							titulo = "Anotación modificada";
						}
						//si hay un archivo seleccionado subirlo
						var file = document.getElementById("file").files[0];

						if (file != undefined) {
							var formData = new FormData(document.getElementById("formSubirArchivo"));
							formData.append("entity", "ANOTACION_CLIENTE");
							formData.append("name", file.name);
							formData.append("id", result.P_OUT_REFID);
							
							uploadFile(formData).then(function(res){
								desplegarResultado(titulo, result);
								bcsComunes.ventanaOverlay(idVentana, false);
							}, function(err){
								desplegarResultado("La operacion tiene errores", err);
								bcsComunes.ventanaOverlay(idVentana, false);
							});
						} else {
							desplegarResultado(titulo, result);
							bcsComunes.ventanaOverlay(idVentana, false);
						}
					},
					function (result) {
						desplegarResultado("La operacion tiene errores", result);
						bcsComunes.ventanaOverlay(idVentana, false);
					});
			}
			//creacion
			else {
				bcsComunes.ventanaOverlay(idVentana, true);
				var parametrosSP = {};
				parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
				parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;
				parametrosSP.p_IdModulo = 'CLI';
				parametrosSP.p_UsaReglasNegocio = "N";
				parametrosSP.p_IdCliente = parametros.IdCliente;
				parametrosSP.p_Titulo = $scope.data.Titulo || '';
				parametrosSP.p_Detalle = $scope.data.Detalle || '';
				parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion || '';
				parametrosSP.p_IdAmbito = $scope.data.Ambito || '';
				parametrosSP.p_IdRelevancia = $scope.data.Relevancia || '';
				parametrosSP.p_FechaDesde = ($scope.data.fechaDesde) ? util.dateSPdate($scope.data.fechaDesde) : "0001-01-01";
				parametrosSP.p_FechaHasta = ($scope.data.fechaDesde) ? util.dateSPdate($scope.data.fechaHasta) : "0001-01-01";

				agregarAnotacionSP(parametrosSP).then(
					function (result) {
						var titulo = "";
						if (result.P_OUT_ADVERTENCIASCONTADOR) {
							titulo = "La operacion tiene advertencias";
						} else if (result.P_OUT_ERRORESCONTADOR) {
							titulo = "La operacion tiene errores";
						} else {
							titulo = "Anotación creada";
						}
						//si hay un archivo seleccionado subirlo
						var file = document.getElementById("file").files[0];

						if (file != undefined) {
							var formData = new FormData(document.getElementById("formSubirArchivo"));
							formData.append("entity", "ANOTACION_CLIENTE");
							formData.append("name", file.name);
							formData.append("id", result.P_OUT_REFID);
							
							uploadFile(formData).then(function(res){
								desplegarResultado(titulo, result);
								bcsComunes.ventanaOverlay(idVentana, false);
							}, function(err){
								desplegarResultado("La operacion tiene errores", err);
								bcsComunes.ventanaOverlay(idVentana, false);
							});
						} else {
							desplegarResultado(titulo, result);
							bcsComunes.ventanaOverlay(idVentana, false);
						}
					},
					function (result) {
						desplegarResultado("La operacion tiene errores", result);
					});
			}

		};

			var desplegarResultado = function(titulo, res){
					bcsComunes.ventanaMsg({
						mensaje: titulo,
						conflictos:util.parseServerResponse(res)
					},parametros.VentanaPadre);
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
			};

			var uploadFile = function(formData, titulo){
				return $http({
						method: "POST",
						url: "/BSCL/Plataforma/GWJAVA/PLA/FileBlobManager",
						data: formData,
						headers: {
				            "Content-Type": undefined // Permite que Angular gestione el multipart/form-data
				        },
				        transformRequest: angular.identity
					}).then (function( msg ) {
						if(msg.data ==="OK"){
							bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
						}else{
							bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
						}
						bcsComunes.ventanaOverlay(idVentana,false);
						const formSubirArchivoDOMElement = document.getElementById('formSubirArchivo');
						formSubirArchivoDOMElement && formSubirArchivoDOMElement.reset();
						return msg.data;
					}, function (jqXHR) {
						console.log(jqXHR)
						bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
						bcsComunes.ventanaOverlay(idVentana,false);
						const formSubirArchivoDOMElement = document.getElementById('formSubirArchivo');
						formSubirArchivoDOMElement && formSubirArchivoDOMElement.reset();
						return $q.reject(jqXHR);
					});
			};
			
			$scope.Cancelar = function() {
				bcsComunes.ventanaData(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			};
	});
}());
//------------------------- FIN CONTROLLER --------------------------