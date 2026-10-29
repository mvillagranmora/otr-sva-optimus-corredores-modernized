
	/*************************************************************
	 * Purpose	: Anotaciones Cliente (Modal)
	 * Author	: TEAM SOCIO NEGOCIO - BCS
	 * Date		: 30/06/2016 [Norm.]
	 *************************************************************/
	(function() {
		angular.module('ModuloAPV').controller('ModalDocumentoRecaudacionSubirCtrl',function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma){	
				mta = this;
				var idVentana = bcsComunes.ventanaWinId($scope);
				var parametros = bcsComunes.ventanaParams(idVentana);
				$scope.data = {};
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
				       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=6071";
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
				       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=6074";
				       var r = $resource
			         ("", {}, {
				                 'urlConsulta': { method: "POST", url: urlConsulta}
			                  }
			         );
				       var post = r.urlConsulta(v_Dato);
				       return post.$promise;
			    };
				
				$scope.Aceptar = function() {
					//modificacion
					if(formSubirArchivo()){
						if (parametros.Anotacion){
							bcsComunes.ventanaOverlay(idVentana,true);
							var parametrosSP = {};
							parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
					        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
					        parametrosSP.p_IdModulo = 'POR';
					        parametrosSP.p_UsaReglasNegocio = "N";
					        parametrosSP.p_IdRecaudacion = 1;
					        parametrosSP.p_IdAnotacion = parametros.Anotacion.IdAnotacion;
					        parametrosSP.p_Titulo = $scope.data.Titulo;
					        parametrosSP.p_Detalle = $scope.data.Detalle;
					        parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion;
					        parametrosSP.p_IdAmbito = $scope.data.Ambito;
					        parametrosSP.p_IdRelevancia = $scope.data.Relevancia;
					        parametrosSP.p_FechaDesde = ($scope.data.fechaDesde)? util.dateSPdate($scope.data.fechaDesde) : "";
					        parametrosSP.p_FechaHasta = ($scope.data.fechaDesde)?util.dateSPdate($scope.data.fechaHasta): "";
					        
					            	
					        modificarAnotacionSP(parametrosSP).then(
								function(result) {
									var titulo = "";
									console.log($scope.data);
									if (result.P_OUT_ADVERTENCIASCONTADOR){
										titulo = "La operacion tiene advertencias";
									}else if (result.P_OUT_ERRORESCONTADOR){
										titulo = "La operacion tiene errores";
									}else {
										titulo = "Anotación modificada";
									}
									//si hay un archivo seleccionado subirlo
									var file = document.getElementById("file").files[0];
									
									if(file===undefined){
										bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
										return;
									}else{
										
										if(file!=undefined){
											var formData = new FormData(document.getElementById("formSubirArchivo"));
											formData.append("entity", "ANOTACION_RECAUDACION_APV");
											formData.append("name", file.name);
											formData.append("id", result.P_OUT_REFID);
											uploadFile(formData);
											desplegarResultado(titulo, result);
										}else{
											desplegarResultado(titulo, result);
										}
									}
										
								},
								function(result){
									desplegarResultado("La operacion tiene errores", result);
								});	
						}
						//creacion
						else {
							bcsComunes.ventanaOverlay(idVentana,true);
							var parametrosSP = {};
					        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
					        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
					        parametrosSP.p_IdModulo = 'CLI';
					        parametrosSP.p_UsaReglasNegocio = "N";
					        parametrosSP.p_IdRecaudacion = parametros.IdRecaudacion;
					        parametrosSP.p_Titulo = $scope.data.Titulo;
					        parametrosSP.p_Detalle = $scope.data.Detalle;
					        parametrosSP.p_IdMotivo = $scope.data.MotivoAnotacion;
					        parametrosSP.p_IdAmbito = $scope.data.Ambito;
					        parametrosSP.p_IdRelevancia = $scope.data.Relevancia;
					        parametrosSP.p_FechaDesde = ($scope.data.fechaDesde)? util.dateSPdate($scope.data.fechaDesde) : "";
					        parametrosSP.p_FechaHasta = ($scope.data.fechaDesde)?util.dateSPdate($scope.data.fechaHasta): "";
					        
					        agregarAnotacionSP(parametrosSP).then(
								function(result) {								
									var titulo = "";
									if (result.P_OUT_ADVERTENCIASCONTADOR){
										titulo = "La operacion tiene advertencias";
									}else if (result.P_OUT_ERRORESCONTADOR){
										titulo = "La operacion tiene errores";
									}else {
										titulo = "Anotación creada";
									}
									//si hay un archivo seleccionado subirlo
									var file = document.getElementById("file").files[0];
									
									if(file!=undefined){
										var formData = new FormData(document.getElementById("formSubirArchivo"));
										formData.append("entity", "ANOTACION_RECAUDACION_APV");
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
				
				var formSubirArchivo = function(){
									
					var mensaje = $translate.instant("UI_ERROR");
					var result =[];
					if(!$scope.data.Titulo || $scope.data.Titulo.length ===0){
						result.push({"Codigo":"UI_TITULO_OBLIGATORIO","Descripcion":$translate.instant("UI_TITULO_OBLIGATORIO")});
					}
					if(!$scope.data.Detalle || $scope.data.Detalle.length ===0){
						result.push({"Codigo":"UI_DETALLE_OBLIGATORIO","Descripcion":$translate.instant("UI_DETALLE_OBLIGATORIO")});
					}
					if(!$scope.data.MotivoAnotacion || $scope.data.MotivoAnotacion.length ===0){
						result.push({"Codigo":"UI_MOTIVO_ANOTACION_OBLIGATORIO","Descripcion":$translate.instant("UI_MOTIVO_ANOTACION_OBLIGATORIO")});
					}
					if(!$scope.data.Ambito || $scope.data.Ambito.length ===0){
						result.push({"Codigo":"UI_AMBITO_OBLIGATORIO","Descripcion":$translate.instant("UI_AMBITO_OBLIGATORIO")});
					}
					if(!$scope.data.Relevancia || $scope.data.Relevancia.length===0){
						result.push({"Codigo":"UI_RELEVANCIA_OBLIGATORIO","Descripcion":$translate.instant("UI_RELEVANCIA_OBLIGATORIO")});
					}
					
					if(result.length>0){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR"),
							conflictos:util.parseServerResponse({"P_OUT_ERRORESDETALLE": result })
						},idVentana);
						return false;
					}else{
						return true;
					}				
				}
		});
	}());
	//------------------------- FIN CONTROLLER --------------------------