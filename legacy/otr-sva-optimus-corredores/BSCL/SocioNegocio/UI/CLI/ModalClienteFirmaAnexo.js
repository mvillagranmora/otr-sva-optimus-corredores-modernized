/*************************************************************
 * Purpose	: Modal Firma de Anexo.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalClienteFirmaAnexoCtrl',function($scope,$translate, bcsComunes, bcsServicioPlataforma)
	{
		var ventanaId = bcsComunes.ventanaWinId($scope);
		dato = bcsComunes.ventanaParams (ventanaId);
		$scope.data = {}
		$scope.data = dato.Anexo;
		$scope.data.IdPersonaFirmante = -1;
		$scope.data.PersonaFirmanteCombo = undefined;
		$scope.data.PersonaFirmanteGrilla = undefined;
		$scope.Bloqueo = dato.Bloqueo;
		$scope.filtrosFormulario = {
			consultaPersonasFirmantes: {
				IdPersonaOrigen: { field: 'IdPersonaOrigen', operator: 'eq', value:  dato.IdPersona }
			}
		};
		if ($scope.data.FechaFirmaClienteConsulta===null || $scope.data.NombreEstado!=='ACTIVO'){
			$scope.data.FechaFirma = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");				
		} else {
			$scope.data.FechaFirma = util.dateDS($scope.data.FechaFirmaClienteConsulta,"Fecha","ODATA");
		}
		
		
		$scope.firmantes = [];
		$scope.idsPersonasFirmantes = [];
		$scope.firmaHabilitada = false;

		$scope.CodigoFormulario = dato.CodigoFormulario;
		$scope.habilitaModificar = false;
		$scope.fechaFirmaChanged = function() {
			if($scope.data.NombreEstado === 'ACTIVO'){
				$scope.habilitaModificar = true;
			} else{
				$scope.habilitaModificar = false;
			}
		  };
		if($scope.data.IdEstado==='ACT'){
			bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_DOCUMENTO_MODIFICAR')+' '+dato.Anexo.NombreDocumento);
			$scope.nombreBoton = $translate.instant('UI_DOCUMENTO_MODIFICAR');
		}
		else{
			$scope.nombreBoton = $translate.instant('UI_FIRMAR_DOCUMENTO');
			bcsComunes.ventanaTitulo(ventanaId, $translate.instant('UI_FIRMA_DOCUMENTOS')+' '+dato.Anexo.NombreDocumento);
			$scope.botonHabilitado = "formModalFirma.$valid && firmaHabilitada && data.NombreEstado !== 'ACTIVO'"
		}  
		

		(function init() {
			const dataSource = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700321",
							dataType: "json"
						}
					},
					filter: { field: 'IdAnexo', operator: 'eq', value: Number(dato.Anexo.IdAnexo) },
					schema: {
						model: {
							fields: {
								IdPersona: { type: 'number' },
								NombreCompleto: { type: 'string' },
								IdPersonaOrigen: { type: 'string' },
								FirmaIngresada: { type: 'string' }
							}
						}
					}
				}
			});


			$scope.grillaFirmantesOptions = {
				gridCreated: function (grid) {
					grid.dataSource.bind('change', function() {
						$scope.firmantes = grid.dataSource.data();

						$scope.idsPersonasFirmantes = [];
						$scope.firmaHabilitada = false;
						let filtrosNuevosCombo = { IdPersonaOrigen: $scope.filtrosFormulario.consultaPersonasFirmantes.IdPersonaOrigen };

						for (let firmante of $scope.firmantes) {
							const key = 'IdPersona' + firmante.IdPersona;
							filtrosNuevosCombo[key] = {
								field: 'IdPersona',
								operator: 'neq',
								value: firmante.IdPersona
							};

							const noTieneFirma = firmante.FirmaIngresada !== 'S';
							$scope.firmaHabilitada = $scope.firmaHabilitada || noTieneFirma;
							if (noTieneFirma) {
								$scope.idsPersonasFirmantes.push(firmante.IdPersona);
							}
						}
						$scope.filtrosFormulario.consultaPersonasFirmantes = filtrosNuevosCombo;
					});
				},
				dataSource: dataSource,
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height: 250,
				columns: [
					{
						field: "NombreCompleto",
						title: $translate.instant('UI_NOMBRE'),
						attributes: { style: "text-align: left;" },
						width: "300px"
					}
				]
			};
		})();

		$scope.agregarFirmante = function agregarFirmante(persona) {
			const personaGrilla = $scope.firmantes.find(function (p) {
				return p.IdPersona === persona.IdPersona;
			});

			if (personaGrilla === undefined) {
				persona.FirmaIngresada = 'N';
				$scope.firmantes.push(persona)
				$scope.grillaFirmantesOptions.dataSource.data($scope.firmantes);
			}
		};

		$scope.quitarFirmante = function quitarFirmante(persona) {
			const personaGrilla = $scope.firmantes.find(function (p) {
				return p.IdPersona === persona.IdPersona;
			});
			const noTieneFirmaIngresada = !personaGrilla || personaGrilla.FirmaIngresada !== 'S';

			if (noTieneFirmaIngresada) {
				$scope.firmantes.remove(personaGrilla);
				$scope.grillaFirmantesOptions.dataSource.data($scope.firmantes);
				delete $scope.data.PersonaFirmanteGrilla;
			}
		};

		$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};

		$scope.Firmar = function( ) {
		   //Parámetros de entrada al servicio
	   	    var datosCrear = {};
	   	    bcsComunes.ventanaOverlay(ventanaId,true);
		   	datosCrear.p_IdEmpresa			= $scope.$root.SesionIdEmpresa; 
		   	datosCrear.p_IdUsuario			= $scope.$root.SesionIdUsuario;
		   	datosCrear.p_IdModulo			= 'SOC';
		   	datosCrear.p_UsaReglasNegocio	= 'N';
		   	datosCrear.p_IdTipoCanalIngreso = 'OPT';
		    datosCrear.p_IdsPersonasFirmantes = $scope.idsPersonasFirmantes;
			if($scope.data.ValidaFechaVigencia === 'S') {
				datosCrear.p_FechaVigencia = util.dateSPseconddate(new Date($scope.data.FechaVigencia));
			}
	    	datosCrear.p_SolicitarFechaFirma = $scope.data.SolicitarFechaFirma;
			if($scope.data.SolicitarFechaFirma === 'S') {
				datosCrear.p_FechaFirma = util.dateSPseconddate(new Date($scope.data.FechaFirma));
			} else {
				datosCrear.p_FechaFirma = null;
			}

	    	var successModificar = function(respuesta)
	    	{	 
	    		
	    		var titulo = (respuesta.P_OUT_ERRORESCONTADOR>0) ? "UI_ERROR" : (respuesta.P_OUT_ADVERTENCIASCONTADOR>0)? "UI_ADVERTENCIAS" : "UI_DOCUMENTO_FIRMADO";
	    		var modalId = bcsComunes.ventanaMsg({
					mensaje:$translate.instant(titulo),
					conflictos:util.parseServerResponse(respuesta)
				},ventanaId);
				bcsComunes.ventanaPromesa(modalId).then(function(){
					bcsComunes.ventanaOverlay(ventanaId,false);
					bcsComunes.ventanaCerrar(ventanaId);

				});
				if (respuesta.P_OUT_REQUISITOS_ENR_FULL.length > 0) {
					bcsComunes.ventanaAbrir('ventanaGrillaRequisitosEnrolamientoFull', { validaciones: respuesta.P_OUT_REQUISITOS_ENR_FULL });
				}
	    	};
	    	
	    	var errorModificar = function(respuesta)
	    	{	
	    		bcsComunes.ventanaOverlay(ventanaId,false);
	    		console.log("Error:" + JSON.stringify(respuesta));	    		
	    		var datos = {};
				
				bcsComunes.msg("error","Error Interno",JSON.stringify(respuesta));
				$scope.data.btnActivo   = true;     //Activa botones
	    	};
			//FIRMAR SIN DOCUMENTO PDF
			if ($scope.data.PermiteFirmarSinDocumento === 'S' && !$scope.data.IdAnexo) {
				var formData = new FormData();
				const file = new File([''], 'FirmaSinArchivoPDF.pdf', { type: 'text/plain' });
				formData.append("IdDocumento", 0);
				formData.append("IdAnexo", 0);
				formData.append("IdCliente", dato.IdCliente);
				formData.append("IdTipoDocumento", dato.Anexo.IdTipoDocumento);
				formData.append("NombreArchivoAdjunto", 'FirmaSinArchivoPDF.pdf');
				formData.append("FormatoArchivoAdjunto", 'PDF');
				formData.append("NumeroFirmantesEmpresa", 0);
				formData.append("NumeroFirmantesCliente", 0);
				formData.append("IdTipoCanalIngreso", "OPT");
				formData.append("data", file);

				async function traerIdAnexo() {
					let obtenerIdAnexo;
					try {
					obtenerIdAnexo = await $.ajax({
						url: "/BSCL/Plataforma/GWJAVA/PLA/DocumentosClienteBin",
						method: 'POST',
						data: formData,
						contentType: false,
						processData: false,
						cache: false,
						dataType: "json"
					});
					return obtenerIdAnexo;
					} catch (error) {
					console.error(error);
					}
				}
				traerIdAnexo().then(function(result){
					datosCrear.p_IdAnexo = result.IdAnexo;
					datosCrear.p_DocumentoSubido = 'S';
					bcsServicioPlataforma.GestionarFirmasDocumento(datosCrear,successModificar,errorModificar)
				});
			} else {
				datosCrear.p_IdAnexo = $scope.data.IdAnexo;
				datosCrear.p_DocumentoSubido = $scope.data.DocumentoSubido;
				bcsServicioPlataforma.GestionarFirmasDocumento(datosCrear,successModificar,errorModificar)
			}

		};
		$scope.modificarFirma = function( ) {
			//Parámetros de entrada al servicio
			var datosCrear = {};
			bcsComunes.ventanaOverlay(ventanaId,true);
			datosCrear.p_IdEmpresa			= $scope.$root.SesionIdEmpresa; 
			datosCrear.p_IdUsuario			= $scope.$root.SesionIdUsuario;
			datosCrear.p_IdModulo			= 'SOC';
			datosCrear.p_UsaReglasNegocio	= 'N';
			datosCrear.p_IdTipoCanalIngreso = 'OPT';
			datosCrear.p_FechaFirma = util.dateSPseconddate(new Date($scope.data.FechaFirma));
			datosCrear.p_IdAnexo = $scope.data.IdAnexo;
 
			 var successModificar = function(respuesta)
			 {	 
				 
				 var titulo = (respuesta.P_OUT_ERRORESCONTADOR>0) ? "UI_ERROR" : (respuesta.P_OUT_ADVERTENCIASCONTADOR>0)? "UI_ADVERTENCIAS" : "UI_FECHAFIRMA_MODIFICAR";
				 var modalId = bcsComunes.ventanaMsg({
					 mensaje:$translate.instant(titulo),
					 conflictos:util.parseServerResponse(respuesta)
				 },ventanaId);
				 bcsComunes.ventanaPromesa(modalId).then(function(){
					 bcsComunes.ventanaOverlay(ventanaId,false);
					 bcsComunes.ventanaCerrar(ventanaId);
 
				 });
				 if (respuesta.P_OUT_REQUISITOS_ENR_FULL.length > 0) {
					 bcsComunes.ventanaAbrir('ventanaGrillaRequisitosEnrolamientoFull', { validaciones: respuesta.P_OUT_REQUISITOS_ENR_FULL });
				 }
			 };
			 
			 var errorModificar = function(respuesta)
			 {	
				 bcsComunes.ventanaOverlay(ventanaId,false);
				 console.log("Error:" + JSON.stringify(respuesta));	    		
				 var datos = {};
				 
				 bcsComunes.msg("error","Error Interno",JSON.stringify(respuesta));
				 $scope.data.btnActivo   = true;     //Activa botones
			 };

			bcsServicioPlataforma.ModificarFirmaDocumento(datosCrear,successModificar,errorModificar)
			 
 
		};
		$scope.Accion = function() {
			if($scope.data.IdEstado==='ACT'){
				$scope.modificarFirma();
			}else{
				$scope.Firmar();
			}
		}
	
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------

		