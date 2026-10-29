	/*************************************************************
 * Purpose	: Modal Firma de Anexo.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalClienteFirmaAnexoUrlCtrl',
			function($scope,$translate, bcsComunes, bcsServicioPlataforma,servicioSociodeNegocio,bcsAlerta,OPCIONES_BCSALERTA)
	{
		var ventanaId = bcsComunes.ventanaWinId($scope);
		dato = bcsComunes.ventanaParams (ventanaId);
		
		$scope.data = {UrlAnexo: "//"}; 
		$scope.data = dato.Anexo;
		$scope.data.IdCliente = dato.IdCliente;
		$scope.data.ValidaURL = dato.ValidaURL;
		$scope.data.Nuevo = dato.Nuevo;
		$scope.Bloqueo = dato.Bloqueo;
		
		bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_FIRMA_DOCUMENTOS')+' '+dato.Anexo.NombreDocumento);
		
		//Carga datos iniciales 
		$scope.data.swFirmaEmpresa1 = $scope.data.FirmaEmpresa1 === 'S' ? true : false;
		$scope.data.swFirmaEmpresa2 = $scope.data.FirmaEmpresa2 === 'S' ? true : false;
		$scope.data.swFirmaEmpresa3 = $scope.data.FirmaEmpresa3 === 'S' ? true : false;
		$scope.data.swFirmaCliente1 = $scope.data.FirmaCliente1 === 'S' ? true : false;
		$scope.data.swFirmaCliente2 = $scope.data.FirmaCliente2 === 'S' ? true : false;
		$scope.data.swFirmaCliente3 = $scope.data.FirmaCliente3 === 'S' ? true : false;
		
		$scope.data.mostrarEmpresa1 = $scope.data.ValidaURL === 'S' ? true : false;
		$scope.data.mostrarEmpresa2 = false;
		$scope.data.mostrarEmpresa3 = false;
		$scope.data.mostrarCliente1 = false;
		$scope.data.mostrarCliente2 = false;
		$scope.data.mostrarCliente3 = false;
		
		
		$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};
		

		////////////////////////////////////////////////////////////////
		$scope.FirmaUrl = function(){
			
					
 
		    //Declara funcion de firma de contrato
		    IrAFirmaURL = function(pNuevo) {
		    	
			    var datosContrato = {//cambiar parametros por variables
		    			p_IdEmpresa				: $scope.$root.SesionIdEmpresa,
		    			p_IdTipoDocu   			: $scope.data.IdTipoDocumento,
						p_IdCliente				: $scope.data.IdCliente,
						p_Pdf					: $scope.data.UrlAnexo,
						p_ValidaURLPDF			: $scope.data.ValidaURL, //se indica que debe validar la URL
						p_Nuevo					: pNuevo}		//indica si el contrato es nuevo o no (true o false)
						
									    bcsComunes.ventanaOverlay(ventanaId,true);
									 	servicioSociodeNegocio.validaFirmarContratoBlockchain(datosContrato).$promise.then(function(respuesta){
									 		bcsComunes.ventanaOverlay(ventanaId,false);
									        if (respuesta.p_Out_ContadorError === 0){ 
										         //bcsComunes.ventanaCerrar(ventanaId);
										         var VentanaIdMsj = bcsComunes.ventanaMsg({
										         mensaje:$translate.instant("UI_RESULTADOS_BLOCKCHAIN"),
										         conflictos:util.parseServerResponse(respuesta)
										         },ventanaId);
										         var promesa = bcsComunes.ventanaPromesa(VentanaIdMsj);
										         $.when(promesa).then(function(data){ 
										        	 bcsComunes.ventanaCerrar(ventanaId);
										         });
										         
										         //bcsComunes.ventanaOverlay(ventanaId,false);	
									         } else {
									        	 console.log(JSON.stringify(respuesta));
										          bcsComunes.ventanaOverlay(ventanaId, false);
										          bcsComunes.ventanaMsg({
										          mensaje:$translate.instant("UI_RESULTADOS_BLOCKCHAIN"),
										          conflictos:util.parseServerResponse(respuesta) 
										          },ventanaId);
									         }
									 
									        });
									    };
		    //varifica si ya hay un contrato "enviado" y prepara pregunta para que el usuario decida si sigue o no
		    
		    
			var datosContrato = {
					p_IdEmpresa		  : $scope.$root.SesionIdEmpresa,
					p_IdUsuario 	  : $scope.$root.SesionIdUsuario,
			        p_IdModulo		  : 'CLI',
					p_IdTipoDocumento : $scope.data.IdTipoDocumento,
					p_IdCliente       : $scope.data.IdCliente
				};
				bcsComunes.ventanaOverlay(ventanaId,true);
				var post2 = servicioSociodeNegocio.validaConsultarContratoBlockChain(datosContrato); // llama el servicio (modulo)
				post2.$promise.then(function(result) { // resultado  de la consulta para ver si el contrato ya "existe"
							bcsComunes.ventanaOverlay(ventanaId,false);
							$scope.etapa = '';
							if (!!result.p_Out_ContadorError
									&& result.p_Out_ContadorError > 0) {
								bcsComunes.ventanaMsg({
													mensaje : $translate.instant('UI_ERROR'),
													conflictos : util.parseServerResponse(result)
												},ventanaId);
							} else {

								if ((result.p_Out_Status == "APPROVED") || (result.p_Out_Status == "PENDING")) {
									// preguntar  si desea volver a  firmar
									var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_REFIRMAR_CONTRATO'));
									t.result.then(function(result) {
												if (result === OPCIONES_BCSALERTA.ACEPTAR) {
													IrAFirmaURL("false"); //Firmar contrato
												}
											});
								} else if (result.p_Out_Status == "REJECTED" || (result.p_Out_Status == "NOFOUND")) {// ***
									IrAFirmaURL("true"); //Firmar contrato
								}

							}
						}); // Fin resultado de valida consulta contrato
		    
		    }
	///////////////////////////////////////////////////////////////////////////
		var parseServerResponse = function(response)
		{
			var result = { errores :[],advertencias:[]};
			$.each(response.P_OUT_ADVERTENCIASDETALLE,function(index,value){;		
				result.adv.push(value);
			});
			$.each(response.P_OUT_ERRORESDETALLE,function(index,value){;
					result.errores.push(value);
			});
			return result;
		}  
	
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------

		