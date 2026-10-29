/*************************************************************
 * Purpose	: Panel Cliente Documentos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PanelClienteDocumentosCtrl',
			function($scope,$uibModal,$translate, bcsComunes,bcsServicioPlataforma,servicioSociodeNegocio,$http,bcsAlerta,OPCIONES_BCSALERTA){

				var pcd = this;
				console.log($scope.admSoc.IdCliente);
				pcd.datos = {};
				pcd.confBasica = {};
				pcd.filtros = {};
				pcd.gridApi = {};
				pcd.seleccion = {};
				pcd.pestanas = [];
				var tabSeleccionado = "";
				pcd.ventanaId = bcsComunes.ventanaWinId($scope);
				var idVentana = bcsComunes.ventanaWinId($scope)
				$scope.admSoc.socioNegocioPromise.then(function () {
					pcd.CodigoFormularioSubirAnexo = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['DOCUMENTOS_CLIENTE_SUBIR_ANEXO'];
				});

				dataSourceDocumentos = function () {
					return new bcs.DataSource({
						type: "call",
						definition: {
							serverFiltering: true,
							serverPaging: true,
							pageSize: 40,
							callParameters: {
								p_IdEmpresa: $scope.$root.SesionIdEmpresa,
								p_IdCliente	: $scope.admSoc.dataSocioNegocio.IdCliente,
								p_DocumentoIdentidadFirmante : $scope.admSoc.dataSocioNegocio.DocumentoIdentidadDefecto,
					},
					callTransaction : 308,
					//callResultSet : "P_OUT_DOCUMENTOS",
					callResultSet : "p_out_Documentos",
					
					schema : {
						model: {
							fields : {
								IdTipoDocumento : {type : "string"},
								NombreDocumento : {type : "string"},
								Clasificacion : {type : "string"},
								IdUltimoAnexo : {type : "number"},
								IdCliente : {type : "number"},
								IdEstado : {type : "string"},
								NombreEstado : {type : "string"},
								FechaVigencia : {type : "date"},
								IdDocumento : {type : "number"},
								IdAnexo : {type:"number" },
								TieneAdjunto : {type:"string"},
								IdClaseDocumento : {type:"number"},
								NumeroFirmasMinimas: {type:"number"},
								NumeroFirmasMaximas: {type:"number"},
								CodigoArchivo: {type:"string"},
								NombrePlantillaContrato: {type:"string"},
								FechaIngreso: {type: "datetime"},
								ValidaClienteFull: {type: "string"},
								IdTipoCanalFirma: {type: "String"},
								FechaFirmaClienteConsulta : {type: "datetime"},								
								SolicitadoPor : {type: "String"},
								UsuarioSolicitante : {type: "String"},
								FirmadoPor : {type: "String"},
								UsuarioFirmante : {type: "String"}
							}
						}
					},
				}
			});
		};
		
		pcd.confGridDocumentos = {
				dataSource: dataSourceDocumentos(),
				columns:  [{
					field: "NombreDocumento",
					title: "UI_NOMBRE_DOCUMENTO",
					width: "500px",
				},{
					field: "NombreEstado",
					title: "UI_ESTADO",
					width: "150px",
					align: "center"
				},{
					field: "IdTipoCanalFirma" ,
					title: "UI_CANAL_INGRESO",
					width: "100px",
					align: "center"

				},{
					field: "FechaIngreso",
					title: "UI_FECHA_INGRESO",
					template : "<label> #: FechaIngreso === null ? '' : util.formatoTimeStamp(FechaIngreso) #</label>",
					width: "150px",
				},{
					field: "FechaVigencia",
					title: "UI_FECHA_VENCIMIENTO",
					template : "<label> #: (FechaVigencia === null || util.formatoDate(FechaVigencia) === '01-01-0001'  || util.formatoDate(FechaVigencia) === '31-12-2999') ? '' : util.formatoDate(FechaVigencia) #</label>",
					width: "110px",
				},{
					field: "FechaFirmaClienteConsulta",
					title: "UI_FECHA_FIRMA",
					template : "<label> #: (FechaFirmaClienteConsulta === null || util.formatoDate(FechaFirmaClienteConsulta) === '01-01-0001' || util.formatoDate(FechaFirmaClienteConsulta) === '31-12-2999') ? '' : util.formatoDate(FechaFirmaClienteConsulta) #</label>",
					width: "100px",
				},{
					field: "SolicitadoPor",
					title: "UI_SOLICITADO_POR",
					width: "100px",
					align: "center"
				},{
					field: "UsuarioSolicitante",
					title: "UI_USUARIO_SOLICITANTE",
					width: "110px",
					align: "center"
				},{
					field: "FirmadoPor",
					title: "UI_NOMBRE_USUARIO_FIRMA",
					width: "130px",
					align: "center"
				},{
					field: "UsuarioFirmante",
					title: "UI_USUARIO_FIRMA",
					width: "100px",
					align: "center"
				}
				],
				sortable: true,
				pageable: true,
				height: 250,
				selectable : "row",
				autoBind : true
			};
			
		
		dataSourceVencidos = function(dataItem){
			return new bcs.DataSource ({
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging : true, // Paginado de los datos en el servidor.
					pageSize : 40, // Número de registros por página.
					transport : {
						read : {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1274",
							dataType : "json"
						}
					},
					schema : {
						model: {
							fields : {
								IdTipoDocumento : {type : "string"},
								NombreDocumento : {type : "string"},
								NumeroFirmasMinimas : {type : "number"},
								NumeroFirmasMaximas : {type : "number"},
								IdAnexo : {type : "number"},
								IdCliente : {type : "number"},
								IdDocumento : {type : "number"},
								IdEstado : {type : "string"},
								NombreEstado : {type : "string"},
								FechaVigencia : {type : "datetime"},
								NombreArchivoAdjunto : {type : "string"},
							}
						}
					},
					filter : [{ field: "IdDocumento", operator: "eq", value: dataItem.IdDocumento }],
				}
			});
		};
		
		
		pcd.confGridVencidos = function(dataItem) {
			return {
				dataSource:  dataSourceVencidos(dataItem),
				columns:  [{
					field: "NombreDocumento",
					title: "UI_NOMBRE_DOCUMENTO",
					width: "500px",
				},{
					field: "NombreArchivoAdjunto",
					title: "UI_NOMBRE_DOCUMENTO",
					width: "200px",
				},{
					field: "NombreEstado",
					title: "UI_ESTADO",
					width: "200px",
				},{
					field: "FechaVigencia",
					title: "UI_FECHA_VENCIMIENTO",
					template : "<label> #: FechaVigencia === null || util.formatoDate(FechaVigencia) === '01-01-0001' ? '' : util.formatoTimeStamp(FechaVigencia) #</label>"
				}],
				sortable: true,
				pageable: true,
				height: 250,
				selectable : "row",
				autoBind : true
			};
		};
		
				
		var listaAtributosFnAgregarDataSource = new bcs.DataSource
	    (
			{		
		       type:"odata",
		       definition : {
		           serverFiltering: true,
			        serverSorting: true,  //ordena al lado del servidor
			        sort: { field: "Clasificacion", dir: "asc" }, //campo y criterio de ordenamiento
			        transport: 
			        {
			            read: 
			            {	
			   				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1272",
			               	dataType: "json" 
			            },
			         },
					 schema : {
						model: {
							fields : {
								Clasificacion : {type : "string"},
								IdCliente : {type : "number"},
							}
						}
					},
			    }
			}
	    );
				
		getClasificacion = function () {
			//Lista la clasificacion para pintar tabs
			pcd.seleccion = undefined;
			listaAtributosFnAgregarDataSource.query( 
				{
				  filter:
					[	        	  
	        		  { field: "IdCliente", operator: "eq", value: parseInt($scope.admSoc.dataSocioNegocio.IdCliente) }, 
		      		]
				}
			).then(function()
			  {
				var viewAtriFnAgregar = listaAtributosFnAgregarDataSource.view();
				if(viewAtriFnAgregar.length == 0)
				{
					if(pcd.pestanas.length == 0)
						{
							pcd.pestanas.push({Clasificacion : 'Sin Registros',active:true});
						}
				}
				else
				{
					pcd.pestanas = [];
					for (var i = 0; i < viewAtriFnAgregar.length;i++)
					  {
						if(i==0)
							pcd.pestanas.push({Clasificacion : viewAtriFnAgregar[i].Clasificacion,
											active:true});
						else
							pcd.pestanas.push({Clasificacion : viewAtriFnAgregar[i].Clasificacion,
								active:false});
					  }
				}
					tabSeleccionado = pcd.pestanas[0].Clasificacion;
				    
					dataSourceAnexos = function(){
						console.log(tabSeleccionado);
						return {
							type 		: "odata", //Los tipos posibles son: odata, call, array y object
							definition 	: {
											serverFiltering 	: true, // Filtrado de datos en el servidor.
											serverPaging 		: true, // Paginado de los datos en el servidor.
											serverSorting 		: true, // Ordenamiento de los datos en el servidor.
											sortable 			: true,
											pageSize 			: 50,
											transport 			: transporte('Anexo'),
											schema 				: ModeloAnexos(),
											filter 			: [
				                				
				                					{field: "IdCliente", operator: "eq", value: parseInt($scope.admSoc.dataSocioNegocio.IdCliente) },
				                					{field: "Clasificacion", operator: "eq", value: tabSeleccionado }
													]
										  }
						};
					};
					
	
			  });		
		};
		
		pcd.abrirDetalleFirmas=function(documento){
			if(documento && (documento.TieneAdjunto==='S' || documento.PermiteFirmarSinDocumento==='S')) {
				bcsComunes.ventanaAbrir("FirmaAnexo", {
					Anexo: documento,
					IdCliente: $scope.admSoc.dataSocioNegocio.IdCliente,
					IdPersona: $scope.admSoc.dataSocioNegocio.IdPersona,
					Bloqueo: true
				}, false, false, pcd.ventanaId);
			}
		}
				

	//------------------------------------------------------------------------------------------------------------------------			
		pcd.firmarDocumento=function(pNuevo){
			
			firmaSinURL = function(seleccion) {
				
				var datosContrato = {//cambiar parametros por variables
						p_IdEmpresa				: $scope.$root.SesionIdEmpresa,
						p_IdTipoDocu   			: seleccion.IdTipoDocumento,
						p_IdCliente				: $scope.admSoc.dataSocioNegocio.IdCliente,
						p_Pdf					: "",
						p_ValidaURLPDF			: "N", //se indica que debe validar la URL
						p_Nuevo					: pNuevo		//indica si el contrato es nuevo o no (true o false)
					};
 
			    bcsComunes.ventanaOverlay(pcd.ventanaId,true);
			 	servicioSociodeNegocio.validaFirmarContratoBlockchain(datosContrato).$promise.then(function(respuesta){
			 		bcsComunes.ventanaOverlay(pcd.ventanaId,false);
			        if (respuesta.p_Out_ContadorError === 0){ 
				         //bcsComunes.ventanaCerrar(ventanaId);
		                 pcd.gridApi.dataSource.read();
		                 pcd.seleccion = undefined;
				         var VentanaIdMsj = bcsComunes.ventanaMsg({
				         mensaje:$translate.instant("UI_RESULTADOS_BLOCKCHAIN"),
				         conflictos:util.parseServerResponse(respuesta)
				         },pcd.ventanaId);
				         
				         //var promesa = bcsComunes.ventanaPromesa(VentanaIdMsj);
				         //$.when(promesa).then(function(data){ 
				        //	 bcsComunes.ventanaCerrar(pcd.ventanaId);
				         //});
				         
				         //bcsComunes.ventanaOverlay(ventanaId,false);	
			         } else {
			        	 console.log(JSON.stringify(respuesta));
				          bcsComunes.ventanaOverlay(pcd.ventanaId, false);
				          bcsComunes.ventanaMsg({
				          mensaje:$translate.instant("UI_RESULTADOS_BLOCKCHAIN"),
				          conflictos:util.parseServerResponse(respuesta) 
				          },pcd.ventanaId);
			         }
		 
		        });

				
				
				
			}
			
//			firmaConURL = function() {
				
				
//			}
			
			
			
				if(pcd.seleccion && pcd.seleccion.seleccionVencidos === undefined)
				{
					//firma contrato sin URL
					if (pcd.seleccion.IdClaseDocumento == "2") {
						firmaSinURL(pcd.seleccion);
					}
					else { //va a formulario que solicita URL y valida antes de firmar
						var ValidaURL = pcd.seleccion.IdClaseDocumento == "2" ? "N" : "S";
						var ventanaId = bcsComunes.ventanaAbrir("FirmaAnexoUrl",{Anexo:pcd.seleccion, IdCliente:$scope.admSoc.dataSocioNegocio.IdCliente, Bloqueo: false, ValidaURL: ValidaURL, Nuevo: pNuevo},false,false,pcd.ventanaId);
						var promesa = bcsComunes.ventanaPromesa(ventanaId);   
						
						$.when(promesa).then(function(data){
							pcd.gridApi.dataSource.read();
							pcd.seleccion = undefined;
							bcsComunes.ventanaOverlay($scope.ventanaId,false);
							if(data.res==="OK"){
								bcsComunes.msg("info",'info',$translate.instant('UI_DOCUMENTO_FIRMADO_BLOCKCHAIN'));
							}
			             });
					}
				}
				else if(pcd.seleccion && pcd.seleccion.seleccionVencidos)  // arreglar condiciones luego de definiciones 
				{
					//firma contrato sin URL
					if (pcd.seleccion.IdClaseDocumento == "2") {
						firmaSinURL(pcd.seleccion.seleccionVencidos);
					}
					else { //va a formulario que solicita URL y valida antes de firmar
					var ValidaURL = pcd.seleccion.IdClaseDocumento == "2" ? "N" : "S";
					var ventanaId = bcsComunes.ventanaAbrir("FirmaAnexoUrl",{Anexo:pcd.seleccion.seleccionVencidos, IdCliente:$scope.admSoc.dataSocioNegocio.IdCliente, Bloqueo: true, ValidaURL: ValidaURL,Nuevo: pNuevo},false,false,pcd.ventanaId);
					var promesa = bcsComunes.ventanaPromesa(ventanaId);   
					
					$.when(promesa).then(function(data){
						pcd.gridApi.dataSource.read();
						pcd.seleccion = undefined;
						
						if(data.res==="OK"){
							bcsComunes.msg("info",'info',$translate.instant('UI_DOCUMENTO_FIRMADO_BLOCKCHAIN'));
						}
		             }); 
					}
				}
			//}
		}// fin si firmar			
				
	//------------------------------------------------------------------------------------------------------------------------------------		   
				pcd.onTabSelect=function(tabName){
					
					if(pcd.gridApi[tabName] !== undefined){
						var filters = new Array()
						filters.push({field: "Clasificacion", operator: "eq", value: tabName });
						filters.push({field: "IdCliente", operator: "eq", value: parseInt($scope.admSoc.dataSocioNegocio.IdCliente) });
						pcd.gridApi[tabName].dataSource.filters(filters);
						
					}
				};
				
				var cargarDatosMaestros = function(	) {
					getClasificacion();
					pcd.onTabSelect(tabSeleccionado);
				};
				
				pcd.abrirDetalle=function(tabName){
					if (pcd.seleccion && pcd.seleccion.IdClaseDocumento == "1") {
						// Funcionamiento habitual, documento != BlockChain
						if (pcd.seleccion && pcd.seleccion.seleccionVencidos === undefined) {
							var ventanaId = bcsComunes.ventanaAbrir("FirmaAnexo", {
								Anexo: pcd.seleccion,
								IdCliente: $scope.admSoc.dataSocioNegocio.IdCliente,
								IdPersona: $scope.admSoc.dataSocioNegocio.IdPersona,
								Bloqueo: false
							}, false, false, pcd.ventanaId);
							var promesa = bcsComunes.ventanaPromesa(ventanaId);
							$.when(promesa).then(function(data) {
								                 pcd.gridApi.dataSource.read();
								                 pcd.seleccion = undefined;
								                 if (data.res === "OK") {
													bcsComunes.ventanaMsg({
																		mensaje : "Documento Firmado",
																		conflictos : util.parseServerResponse(data.result)
																	},
																	pcd.ventanaId);
												}
											});
						} else if (pcd.seleccion
								&& pcd.seleccion.seleccionVencidos) {
							var ventanaId = bcsComunes.ventanaAbrir("FirmaAnexo",
								{
									Anexo: pcd.seleccion.seleccionVencidos,
									IdCliente: $scope.admSoc.dataSocioNegocio.IdCliente,
									Bloqueo: true
								}, false, false,
								pcd.ventanaId);
							var promesa = bcsComunes.ventanaPromesa(ventanaId);

							$.when(promesa).then(function(data) {
												pcd.gridApi.dataSource.read();
												pcd.seleccion = undefined;

												if (data.res === "OK") {
													bcsComunes.ventanaMsg({
																		mensaje : "Documento Firmado",
																		conflictos : util
																				.parseServerResponse(data.result)
																	},
																	pcd.ventanaId);
												}
											});
						}

					} else {
						// Contrato BlockChain

						// 1- Validamos que cumpla con los
						// requerimientos mínimos para firmar en
						// BlockChain
						// llamar servicio consultarContrato

						bcsComunes.ventanaOverlay(pcd.ventanaId, true);

						var datosConsulta = {
							p_IdEmpresa : $scope.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_IdCliente : $scope.admSoc.dataSocioNegocio.IdCliente
						};

						var post = servicioSociodeNegocio.validaCondicionesMinimasFirmaBlockhain(datosConsulta);
						post.$promise.then(function(result) {
									bcsComunes.ventanaOverlay(
											pcd.ventanaId, false);
									$scope.etapa = '';
									if (!!result.P_OUT_ERRORESCONTADOR
											&& result.P_OUT_ERRORESCONTADOR > 0) {
										bcsComunes.ventanaMsg({
															mensaje : $translate
																	.instant('UI_ERROR'),
															conflictos : util
																	.parseServerResponse(result)
														},
														$scope.ventanaId);
									} else {
										// es BlockChain sin URL
										if (pcd.seleccion.IdClaseDocumento == "2") {

											var datosContrato = {// cambiar
													p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
													p_IdUsuario 		: $scope.SesionIdUsuario,
													p_IdModulo         	: "",
													p_IdTipoDocumento 	: pcd.seleccion.IdTipoDocumento,
													p_IdCliente 		: $scope.admSoc.dataSocioNegocio.IdCliente,
													p_ConviertePDF 		: "S" //trae pdf convertido en "binario" para bajarlo con un saveas	
												/*p_TypeMSA : pcd.seleccion.IdTipoDocumento,
												p_IdCliente : $scope.admSoc.dataSocioNegocio.IdCliente,
												p_BrokerageRUT : "80993900-6",
												p_CodigoParametro : "BLOCKCHAIN.CONSULTACONTRATO.URL",
												p_CodigoParametroLogin : "BLOCKCHAIN.LOGIN.URL"*/
											};
											bcsComunes.ventanaOverlay(pcd.ventanaId,true);
											var post2 = servicioSociodeNegocio.validaConsultarContratoBlockChain(datosContrato); // llama el servicio (modulo)
											post2.$promise.then(function(result) { // resultado de valida consulta contrato
														bcsComunes.ventanaOverlay(pcd.ventanaId,false);
														$scope.etapa = '';
														if (!!result.p_Out_ContadorError
																&& result.p_Out_ContadorError > 0) {
															bcsComunes.ventanaMsg({
																				mensaje : $translate
																						.instant('UI_ERROR'),
																				conflictos : util
																						.parseServerResponse(result)
																			},
																			$scope.ventanaId);
														} else {
															var firmar = false;

															if ((result.p_Out_Status == "APPROVED")
																	|| (result.p_Out_Status == "PENDING")) {
																// preguntar si desea volver a  firmar
																var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_REFIRMAR_CONTRATO'));
																t.result.then(function(result) {
																			if (result === OPCIONES_BCSALERTA.ACEPTAR) {
																				pcd.firmarDocumento("false"); //Firmar contrato
																			}
																		});
															} else if (result.p_Out_Status == "REJECTED"
																	|| (result.p_Out_Status == "NOFOUND")) {// ***
																pcd.firmarDocumento("true"); //Firmar contrato
															}

														}
													}); // Fin resultado de valida consulta contrato
										} else { //va a pedir URL en pantalla
											
											pcd.firmarDocumento("true"); //Firmar contrato

										}
									}
								});

					}
				};
				
				pcd.SubirDocumento =function(tabName){
					
					console.log($scope.admSoc);
					if(pcd.seleccion)
					{
						const parametros = {
							IdAnexo: pcd.seleccion.seleccionVencidos ? pcd.seleccion.seleccionVencidos.IdAnexo : pcd.seleccion.IdAnexo,
							IdDocumento: pcd.seleccion.IdDocumento,
							IdTipoDocumento: pcd.seleccion.IdTipoDocumento,
							IdCliente: $scope.admSoc.dataSocioNegocio.IdCliente,
							TipoDocumento: pcd.seleccion,
							CodigoFormulario: pcd.CodigoFormularioSubirAnexo
						};
						var ventanaId = bcsComunes.ventanaAbrir("SubirAnexo",
								parametros,false,false,pcd.ventanaId);
						var promesa = bcsComunes.ventanaPromesa(ventanaId);   
						
						$.when(promesa).then(function(data){
							pcd.gridApi.dataSource.read();
							//pcd.gridApi[tabName].dataSource.read();
							pcd.seleccion.IdAnexo = undefined;
							if(data.res==="OK"){
							bcsComunes.ventanaMsg({ mensaje : "Documento Firmado",
								conflictos : util.parseServerResponse(data.result)}, pcd.ventanaId);
							}
			             }); 
					}
				};
				
				pcd.BajarDocumento= function(){
					
					bcsComunes.ventanaOverlay(pcd.ventanaId,true);
					
					//es contrato blockChain clase 1 o 2 (sin y con envio de URL PDF)
					if (pcd.seleccion && (pcd.seleccion.IdClaseDocumento == "2" || pcd.seleccion.IdClaseDocumento == "3" )) {
						//entonces llama a servicio BlockChain para rescatar URL de Contrato y poder mostrarlo
						
						
						var datosContrato = {// cambiar
								p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
								p_IdUsuario 		: $scope.SesionIdUsuario,
								p_IdModulo         	: "",
								p_IdTipoDocumento 	: pcd.seleccion.IdTipoDocumento,
								p_IdCliente 		: $scope.admSoc.dataSocioNegocio.IdCliente,
								p_ConviertePDF 		: "S" //trae pdf convertido en "binario" para bajarlo con un saveas	
									
								/*p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
								p_IdUsuario 		: $scope.SesionIdUsuario,
								p_IdModulo         	: "",
								p_IdTipoDocumento 	: pcd.seleccion.IdTipoDocumento,
								p_IdCliente 		: $scope.admSoc.dataSocioNegocio.IdCliente,
								p_ConviertePDF 		: "S" //trae pdf convertido en "binario" para bajarlo con un saveas	*/
							};
							//bcsComunes.ventanaOverlay(pcd.ventanaId,true);
							var post2 = servicioSociodeNegocio.validaConsultarContratoBlockChain(datosContrato); // llama el servicio que verifica estado y retorna PDF
							post2.$promise.then(function(result) { // resultado de valida consulta contrato
										bcsComunes.ventanaOverlay(pcd.ventanaId,false);
										$scope.etapa = '';
										if (!!result.p_Out_ContadorError
												&& result.p_Out_ContadorError > 0) {
											bcsComunes.ventanaMsg({
																mensaje : $translate.instant('UI_ERROR'),
																conflictos : util.parseServerResponse(result)
															},$scope.ventanaId);
										} else {

											//retorna estos estado?
											if ( result.p_Out_Status == "APPROVED" || 
												 result.p_Out_Status == "PENDING" || 
												 result.p_Out_Status == "REJECTED") {
												//entonces es probable que tenga URL de PDF para mostrar
												var leBlob = util.b64toBlob(result.p_Out_ObjPDF.encodedString,'application/pdf',512);
									      		saveAs(leBlob, "ContratoBlockChain.pdf");
												
											} 

										}
									}); // Fin resultado de valida consulta contrato
						
					}
					else 
					{ //contratos no BlockChain
						var documentos = [];
						documentos.push({
							IdAnexo : pcd.seleccion.IdAnexo,
							IdDocumento : pcd.seleccion.IdDocumento
						});
						var datos = {
							p_IdEmpresa	: $scope.$root.SesionIdEmpresa,
							p_IdUsuario : $scope.SesionIdUsuario,
							p_Documentos : documentos
						};
						
						console.log(datos);
						
						servicioSociodeNegocio.ConsultaDocumentos(datos).$promise.then(function(resp){
							console.log(resp);
							if(resp.P_OUT_ERRORESCONTADOR == 0){
								var files = resp.P_FILES;
								if(files.length > 0){
									var extensionsImg = ["PNG", "BMP", "JPG"]
									for(var i = 0; i < files.length; i++) {
										if(files[i].FormatoArchivo.toUpperCase() == "PDF"){
											var leBlob = util.b64toBlob(files[i].encodedString,'application/pdf');
								      		saveAs(leBlob, files[i].NombreArchivo);
										}
										else if(extensionsImg.indexOf(files[i].FormatoArchivo.toUpperCase()) > 0){
											var leBlob = util.b64toBlob(files[i].encodedString,'image/jpeg');
								      		saveAs(leBlob, files[i].NombreArchivo);
										}
									}
								}
								pcd.gridApi.dataSource.read();
							}
							else{
								bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_DESCARGAR_ARCHIVO"));
							}
							bcsComunes.ventanaOverlay(pcd.ventanaId,false);
						});
					}
				};


				pcd.validarDocumento = function validarDocumento() {
					bcsComunes.ventanaOverlay(pcd.idVentana, true);
					const selecionado = pcd.seleccion;
					const requestBody = {
						p_IdEmpresa: $scope.SesionIdEmpresa,
						p_IdUsuario: $scope.SesionIdUsuario,
						p_IdModulo: 'SOC',
						p_UsaReglasNegocio: 'N',
						p_CodigoFormulario: '',
						p_IdTipoCanalIngreso: 'OPT',
						IdCliente: $scope.admSoc.dataSocioNegocio.IdCliente,
					};

					if (selecionado.ValidaClienteFull === 'S') {
						servicioSociodeNegocio.validarImpresionContrato(requestBody).$promise.then(
							function (result) {
								if (result.P_OUT_ERRORESCONTADOR === 0) {
									pcd.BajarDocumento();
								} else {
									bcsComunes.ventanaOverlay(pcd.idVentana, false);
									bcsComunes.ventanaMsg({
										mensaje: $translate.instant('UI_REQUISITOS_ENROLAMIENTO_FULL'),
										conflictos: util.parseServerResponse(result)
									}, pcd.idVentana);
									if (result.P_OUT_REQUISITOS_ENR_FULL.length > 0) {
										bcsComunes.ventanaAbrir('ventanaGrillaRequisitosEnrolamientoFull', { validaciones: result.P_OUT_REQUISITOS_ENR_FULL });
									}
								}
							});
					} else {
						pcd.BajarDocumento();
					}
				};

				
				cargarDatosMaestros();
				bcsComunes.ventanaAutoHeight(idVentana);
			});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
