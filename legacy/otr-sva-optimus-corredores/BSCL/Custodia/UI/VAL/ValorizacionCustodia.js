
/*************************************************************
 * Purpose	: Valorizar carteras-garantias-subyacente
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 28/11/2016 [Norm.]
 * 
 * Mod		: Se procesa valorizacion como batch.
 * Author	: JGarces <JG>
 * Date		: 2017 Oct
 *************************************************************/
(function() {
	angular.module('ModuloCustodia').controller('ValorizacionCustodiaCtrl',
		function (	$rootScope,
					$scope, $translate, $timeout,
					bcsComunes, bcsAlerta, bcsComunes, $resource,
					bcsServicioPlataforma,
					bcsServiciosCustodia,
					bcsServicioCalcularMontoTipoInstrumento,
					bcsServiciosValorizacion) {
	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var valoCust = this;
		valoCust.datos = {};
		valoCust.fn = {};
		valoCust.ventanaId = bcsComunes.ventanaWinId($scope);
		var estadoInicial = false;
		valoCust.datos.carteraRV = estadoInicial;
		valoCust.datos.carteraRF = estadoInicial;
		valoCust.datos.carteraIF = estadoInicial;
		valoCust.datos.carteraFFMM = estadoInicial;
		valoCust.datos.prestamosEmprestitos = estadoInicial;
		valoCust.datos.garantiasRVRFIF = estadoInicial;
		valoCust.datos.simultaneas = estadoInicial;
		valoCust.datos.cupones = estadoInicial;
		valoCust.datos.pactos = estadoInicial;	
		valoCust.datos.carteraFuturos = estadoInicial;	
		valoCust.datos.carteraOpciones = estadoInicial;	
		valoCust.datos.fechaProceso = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
		$scope.oPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};
		
	
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		valoCust.fn.clicValorizar = function(){
			valoCust.fn.Valorizar();
		}

		valoCust.fn.Confirmar = function() {
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENS_CONTINUAR_VALORIZACION"));
			t.result.then(function(result){
				if (result===true){ 
						return true;
				}else{ 
					return false;
				}
			});
			return false;
		}
	 	 
		/*********************************************
		 * FUNCIONES
		 *********************************************/		
		var tipoSeleccionado = function(){
			if(valoCust.datos.carteraRV == true ||
			valoCust.datos.carteraRF == true ||
			valoCust.datos.carteraIF == true ||
			valoCust.datos.carteraFFMM == true ||
			valoCust.datos.prestamosEmprestitos == true ||
			valoCust.datos.garantiasRVRFIF == true ||
			valoCust.datos.simultaneas == true ||
			valoCust.datos.cupones == true ||
			valoCust.datos.pactos == true||
			valoCust.datos.carteraFuturos == true ||
			valoCust.datos.carteraOpciones == true) return true;
			else return false;
			
		};

		/** ---------------------------------------------------
		 ** Se verifica que siguiente dia sea mismo mes.
		 ** ---------------------------------------------------- **/
	    DiaHoy = new Date($scope.SesionFechaSistema);
		MesHoy = DiaHoy.getMonth() + 1;					

		var datoDia = {
				p_IdPais	: $scope.SesionIdPaisSistema,
				p_DiaHabil	: 'S',
				p_FechasIN	: [ { FechaInicial : $scope.SesionFechaSistema,
							      NumeroDias	: 1 }]
			};			
		bcsServicioPlataforma.Servicios().ConsultarDiaSiguienteMasivo(datoDia).$promise.then(function(result){
				DiaSgte = new Date(result.P_FECHASOUTF[0].FechaCalculada);
				MesSgte = DiaSgte.getMonth() + 1;
				MesSgteStr = '0' + MesSgte;
				MesSgteStr = MesSgteStr.substr(MesSgteStr.lenght-2);
			
				// La siguiente fecha habil es de distinto mes =>
				// El dia actual es el ultimo día del mes en curso
				Dia1ro = new Date(DiaSgte.getFullYear() + "/" + MesSgteStr + "/01");
				Dia1ro = Dia1ro.setDate( (Dia1ro).getDate() - 1 );
				DiaUltimo = new Date(Dia1ro);
		});

			
		ValorizarCarteraHoy = function() {
			var r = $resource("", {}, {
				'inscribirProcesoBatch': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
			});

			PrmVal = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo  : 'CUS',
					p_TipoPrograma : [{
						IdInterno : 0,
						IdTipoPrograma : 'ValorizarCustodiaUIbatch',
						SegundosDesfase : 0
					}],
					p_Parametro : [
						{ IdInterno : 0, NombreParametro : 'IdEmpresa', Valor : $scope.SesionIdEmpresa },
						{ IdInterno : 0, NombreParametro : 'IdUsuario', Valor : $scope.SesionIdUsuario },
						{ IdInterno : 0, NombreParametro : 'IdModulo', Valor : 'CUS' },
						{ IdInterno : 0, NombreParametro : 'FechaSistema', Valor : util.dateSPdate(valoCust.datos.fechaProceso) },
						{ IdInterno : 0, NombreParametro : 'IdPais', Valor : $scope.$root.SesionIdPaisSistema },
						{ IdInterno : 0, NombreParametro : 'Simular', Valor : 'N' },
						{ IdInterno : 0, NombreParametro : 'IdPortafolio', Valor : valoCust.datos.IdPortafolio||0 },
						{ IdInterno : 0, NombreParametro : 'IdCliente', Valor : valoCust.datos.socioNegocio.IdCliente || 0 },
						{ IdInterno : 0, NombreParametro : 'ValorizaCarteraRV', Valor : (valoCust.datos.carteraRV==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaCarteraFFMM', Valor : (valoCust.datos.carteraFFMM==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaCarteraRF', Valor : (valoCust.datos.carteraRF==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaCarteraIF', Valor : (valoCust.datos.carteraIF==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaPrestamosEmprestitos', Valor : (valoCust.datos.prestamosEmprestitos==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaGarantiasRVRFIF', Valor : (valoCust.datos.garantiasRVRFIF==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaSimultaneas', Valor : (valoCust.datos.simultaneas==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaCupones', Valor : (valoCust.datos.cupones==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizaPactos', Valor : (valoCust.datos.pactos==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizarCarteraFuturos', Valor : (valoCust.datos.carteraFuturos==true)?'S':'N' },
						{ IdInterno : 0, NombreParametro : 'ValorizarCarteraOpciones', Valor : (valoCust.datos.carteraOpciones==true)?'S':'N' }
						]
			};
			
			r.inscribirProcesoBatch(PrmVal).$promise.then(function(result){
				bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
				console.log("Resultado ejecucion SP",result);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_VALORIZACION_BATCH_EXITOSA')+'[IdPrograma: ::IdPrograma, Fecha Ejecución: ::FechaEjecucion]'.replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, valoCust.ventanaId);
			},function(error){
				bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
				console.log("Error Ejecucion SP",error);
				bcsComunes.msg("error","Error",$translate.instant('UI_VALORIZACION_BATCH_ERRONEA'));
			});
			
		};
			
		
		valoCust.fn.Valorizar = function()
	    {
			var datosValorizar = {};
			var PrmSgteDia = new Object();
			
			if( $scope.valoCust.datos.cuentaPropia===true){
				$scope.oSocioNegocio.idActual =  $scope.SesionCorredoraIdSocioDeNegocio;
			}
				 
			if (!tipoSeleccionado()) {
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TIPO"));
				return;
			}
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENS_CONTINUAR_VALORIZACION"));
			t.result.then(function(result){
				if (result===3){ 
					
			
					// Valorizacion cartera RF/IF ?  
					// ----------------------------------------------------
					if ( (valoCust.datos.carteraIF === true || valoCust.datos.carteraRF === true) && MesHoy !== MesSgte && DiaUltimo !== DiaSgte )  {
						
						// Seleccion Valorizacion IF/RF +
						// La siguiente fecha habil es de distinto mes +
						// El ultimo dia del mes no es el dia actual =>

						// => Valorizar la cartera a la ultima fecha del mes
						// => Respaldar la cartera en el historico
						// => Seguir Proceso Normal.
						
						var PrmVal_IFRF = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario : $scope.SesionIdUsuario,
								p_IdModulo  : 'CUS',
								p_TipoPrograma : [{
									IdInterno : 0,
									IdTipoPrograma : 'ValorizarCustodiaUIbatch',
									SegundosDesfase : 0
								}],
								p_Parametro : [
									{ IdInterno : 0, NombreParametro : 'IdEmpresa', Valor : $scope.SesionIdEmpresa },
									{ IdInterno : 0, NombreParametro : 'IdUsuario', Valor : $scope.SesionIdUsuario },
									{ IdInterno : 0, NombreParametro : 'IdModulo', Valor : 'CUS' },
									{ IdInterno : 0, NombreParametro : 'FechaSistema', Valor : util.dateSPdate(DiaUltimo) },
									{ IdInterno : 0, NombreParametro : 'IdPais', Valor : $scope.$root.SesionIdPaisSistema },
									{ IdInterno : 0, NombreParametro : 'Simular', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'IdPortafolio', Valor : valoCust.datos.IdPortafolio || 0 },
									{ IdInterno : 0, NombreParametro : 'IdCliente', Valor :  $scope.oSocioNegocio.idActual || 0 },
									{ IdInterno : 0, NombreParametro : 'ValorizaCarteraRV', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaCarteraFFMM', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaCarteraRF', Valor : 'S' },
									{ IdInterno : 0, NombreParametro : 'ValorizaCarteraIF', Valor : 'S' },
									{ IdInterno : 0, NombreParametro : 'ValorizaPrestamosEmprestitos', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaGarantiasRVRFIF', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaSimultaneas', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaCupones', Valor : 'N' },
									{ IdInterno : 0, NombreParametro : 'ValorizaPactos', Valor : 'S' }
								]
						};
						
						bcsComunes.ventanaOverlay(valoCust.ventanaId,true);
						var Val_IFRF = $resource("", {}, {
							'inscribirProcesoBatch': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
						});
						Val_IFRF.inscribirProcesoBatch(PrmVal_IFRF).$promise.then(function(Res_IFRF){
							bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
							console.log("Resultado ejecucion SP",Res_IFRF);
							var fechaLocal=util.dateDS(Res_IFRF.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
							bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_VALORIZACION_BATCH_EXITOSA')+'[IdPrograma: ::IdPrograma, Fecha Ejecución: ::FechaEjecucion]'.replace("::IdPrograma",Res_IFRF.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
							conflictos : util.parseServerResponse(Res_IFRF)}, valoCust.ventanaId);

							// Ahora viene el Respaldo de la cartera
							// -------------------------------------

							var PrmHis = {
									p_FechaRespaldo : util.dateSPdate(DiaUltimo),
									p_RespaldaCarteras : 'S',
									p_RespaldaGarantias  : 'S'
							}

							bcsServiciosCustodia.RespaldarCustodia(PrmHis,
									function(RespHis){
										// Valorizacion al dia que corresponde
										// -------------------------------------									
										ValorizarCarteraHoy();
									},
									function(RespHis){
										bcsComunes.ventanaMsg({
											conflictos:util.parseServerResponse(RespHis)
										},$scope.ventanaId);
									}
								);

						},function(error){
							bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
							console.log("Error Ejecucion SP",error);
							bcsComunes.msg("error","Error",$translate.instant('UI_VALORIZACION_BATCH_ERRONEA'));
						});
					} 	else {

						// Valorizacion NORMAL
						// -------------------------------------															
						ValorizarCarteraHoy();
					}
					
					/** -------------------------------------------------------------------------------------------------------------------------------
					var r = $resource("", {}, {
						'inscribirProcesoBatch': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
					});
					r.inscribirProcesoBatch(datos).$promise.then(function(result){
						bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
						console.log("Resultado ejecucion SP",result);
						var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
						bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_VALORIZACION_BATCH_EXITOSA')+'[IdPrograma: ::IdPrograma, Fecha Ejecución: ::FechaEjecucion]'.replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
							conflictos : util.parseServerResponse(result)}, valoCust.ventanaId);
					},function(error){
						bcsComunes.ventanaOverlay(valoCust.ventanaId,false);
						console.log("Error Ejecucion SP",error);
						bcsComunes.msg("error","Error",$translate.instant('UI_VALORIZACION_BATCH_ERRONEA'));
					});
					------------------------------------------------------------------------------------------------------------------------------- **/
				}else{ 
					return false;
				}
			});
	   	};
	   	
	   	$scope.$watch('valoCust.datos.cuentaPropia',function(newval,oldval){

			if(newval){
				$scope.oSocioNegocio.idActual = $scope.SesionCorredoraIdSocioDeNegocio;
			}else{
				$scope.oSocioNegocio.idActual = false;
			}
		})
	});
	
}());
//------------------------- FIN CONTROLLER --------------------------
