/*************************************************************
 * Purpose	: Crear Cliente
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloOrdenes').controller('CrearClienteCtrl', 
	function($scope,bcsComunes,$translate,$resource,bcsConfiguracionSociodeNegocio,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA,$timeout ){
		$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		
		const parametros = bcsComunes.ventanaParams($scope.ventanaId);

		$scope.clienteFinal = parametros.clienteFinal;
		
		bcsConfiguracionSociodeNegocio.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionSociodeNegocio.CONFIGURACION['CLI.BloqueoDocIdentidad'] === 'S')
				$scope.configData.TipoDocumentoIdentidadBloqueado = true;
			else
				$scope.configData.TipoDocumentoIdentidadBloqueado = false;
		});
		
		$scope.etapa='inicio';
		$scope.workingNet = {};
		bcsComunes.ventanaOverlay($scope.ventanaId,true);
		bcsComunes.ventanaAutoHeight($scope.ventanaId);
		$scope.configData = {};
		$scope.searchData = {};
		$scope.recordData = {};
		$scope.recordData.BrokerObj = {};
		$scope.camposRequeridosCC = {};
		
		$scope.seleccionado = {};
		$scope.kOptFechaNacimiento = {};
		$scope.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		//$scope.configData.TipoDocumentoIdentidadBloqueado = true;
		$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
		$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoPorRutInterno = false;
		$scope.recordData.NumeroIdentidadNumeroAdicional = '';
		$scope.ValidaSalidaESC = function(){
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_ENROLAMIENTO_INCONCLUSO'));
			t.result.then(function(result){
					if (result === OPCIONES_BCSALERTA.NO) {
					bcsComunes.ventanaCerrar($scope.ventanaId);
				}
			});	
		};
		$scope.obtenerPerfilComercialPorDefecto = function obtenerPerfilComercialPorDefecto(modelos) {
			const isDefaultModel = (comercialModel) => comercialModel.PorDefecto === 'S';
			const isSameEnrollmentType = (comercialModel) => comercialModel.TipoEnrolamiento === $scope.recordData.tipoEnrolamiento;
			const isSameInvestorCategory = (comercialModel) => comercialModel.IdCategoriaInversionista === $scope.recordData.idCategoriaInversionista;

			return modelos.find(comercialModel => isDefaultModel(comercialModel) && isSameEnrollmentType(comercialModel) && isSameInvestorCategory(comercialModel));
		}
		
		$scope.obtenerTipoClientePorDefecto = function obtenerTipoClientePorDefecto(modelos) {
			const esTipoClientePorDefecto = (tipoCliente) => tipoCliente.PorDefecto === 'S';
			const esMismoTipoCliente = (tipoCliente) => tipoCliente.TipoEnrolamiento === $scope.recordData.tipoEnrolamiento;

			return modelos.find(comercialModel => esTipoClientePorDefecto(comercialModel) && esMismoTipoCliente(comercialModel));
		}
		
		$scope.formConfig = {};
		$scope.formConfig.largoMaximoNumeroAdicional=0;
		$scope.fnLoadFormConfig = function(SesionLocale){
			if(SesionLocale == 'PE'){ //peru
				$scope.formConfig.showNumeroAdicional = true;
				$scope.formConfig.showCodigoAdicionalHolding = true;
				$scope.formConfig.labelNumeroIdentidad = 'UI_NUMERO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'DNI';
				$scope.formConfig.tipoDocIdentidadAdicionalDef = 'RUT';
				$scope.configData.TipoDocumentoIdentidadBloqueado = false;
			}else{ //otro
				$scope.formConfig.showNumeroAdicional = false;
				$scope.formConfig.showCodigoAdicionalHolding = false;
				$scope.formConfig.labelNumeroIdentidad = 'UI_DOCUMENTO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'R';
				$scope.formConfig.tipoDocIdentidadAdicionalDef = 'R';
			}
			$scope.$apply;
		}
		$scope.fnLoadFormConfig($scope.SesionLocale);
		
		
		if ($scope.clienteFinal) {		  	
			$scope.formConfig.oConfiguracionIdTipoPersona={filtros : {}};						
			$scope.formConfig.oConfiguracionIdTipoPersona.filtros = {
				logic : "or",
				N : {"field": "IdTipoPersona","operator": "eq","value": "N"},
				J : {"field": "IdTipoPersona","operator": "eq","value": "J"},
			 };
	    };
	    
		
		$scope.configData.filtroTipoDocumentoIdentidad = {
			PorDefecto : {field: "PorDefecto", operator: "eq", value: "S"},
			TipoPersona : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		};
		$scope.configData.filtroTipoDocumentoIdentidadAdicional = {
			PorDefecto : {field: "PorDefecto", operator: "eq", value: "N"},
			TipoPersona : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		};
		$scope.configData.filtroCiudad = {
			IdPais : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		};
		$scope.configData.filtroCategoriaInversionista = {
				IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}
			};
		$scope.configData.filtroClaseCliente = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}
		};
		$scope.configData.filtroDivisionPais = {
			IdPais : {field: "IdPais", operator: "eq", value: -1},//$scope.$root.SesionIdPaisSistema},
			IdCiudad : {field: "IdCiudad", operator: "neq", value: 0} //no pueden haber divisiones sin ciudad!
		};	
		$scope.configData.filtroPerfilComercial = {
			IdCategoriaInversionista : {field: "IdCategoriaInversionista", operator: "eq", value: ""},
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'},
			TipoEnrolamiento : {field: "TipoEnrolamiento", operator: "eq", value: ""},
		};
		$scope.configData.filtroEjecutivo = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'}
		};
		$scope.configData.filtroConyuge = {
			CodigoTipoPersona : {field: "CodigoTipoPersona", operator: "eq", value: 'N'},
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
		}

		$scope.configData.filtroTipoCliente = {
			IdEmpresa	: {field : "IdEmpresa", operator : "eq", value : $scope.$root.SesionIdEmpresa},
			IdEstado : {field : "IdEstado", operator : "eq", value : "ACT"},
			TipoEnrolamiento : {field: "TipoEnrolamiento", operator: "eq", value: ""}
		};
		
		
		$scope.configData.filtroBroker = {
				IdEstado : {field: "IdEstado", operator: "eq", value: "ACT"}
			};
		
		var  datosPortafolio = 
		{
			type : "odata",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				sort 			: { field: "IdCliente", dir: "DESC" },
				pageSize        : 10,
				transport		: {
					read: {
						url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1643",
						dataType	: "json"
					}
				},
                schema: { 
                    model: {
						fields: {													
							"IdCliente"			: {type: "number"},
							"IdPortafolio"		: {type: "number"},
							
						}
					}
				}
    		}
		};
		
		
		
		$scope.cancelar = function() {
			$scope.limpiarPantalla();
			$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
			$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.TipoDocumentoIdentidadBloqueado = false;
			$scope.configData.rutInterno = false;
			//$scope.configData.TipoDocumentoIdentidadBloqueado = ($scope.SesionLocale==="PE")?false:true;
		};
		
		$scope.limpiarPantalla = function() {
			$scope.recordData = {//si id-post-filtro definido, también se debe setear.
				tipoPersona : 'N',
				idCategoriaInversionista : '',
				idPerfilComercialActual:'',
				idTipoCliente : '',
				IdEmpleadoEjecutivo : 0,
				idPaisResidencia : $scope.$root.SesionIdPaisSistema
			};
		};
		
		$scope.configData.buscarCliente = function() {
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.etapa=='buscar';
			if($scope.$root.SesionIdPaisSistema == 44) {
				var obtieneDigitoVerificador = function dv(T) {
					var M=0,S=1;for(;T;T=Math.floor(T/10))
						S=(S+T%10*(9-M++%6))%11;return S?S-1:'K';
				};
				if($scope.searchData.IdTipoDocumentoIdentidad === 'R')
				{
					if(!($scope.searchData.NumeroDocumentoIdentidad.indexOf('-') > 0 &&
					$scope.searchData.NumeroDocumentoIdentidad.length > ($scope.searchData.NumeroDocumentoIdentidad.indexOf('-') + 1) &&
					$scope.searchData.NumeroDocumentoIdentidad[$scope.searchData.NumeroDocumentoIdentidad.indexOf('-') + 1] &&
					/^[0-9]+[-|‐]{1}[0-9kK]{1}$/.test($scope.searchData.NumeroDocumentoIdentidad) &&
					$scope.searchData.NumeroDocumentoIdentidad.length >= 3 &&
					obtieneDigitoVerificador($scope.searchData.NumeroDocumentoIdentidad.substr(0,$scope.searchData.NumeroDocumentoIdentidad.length-2)) == $scope.searchData.NumeroDocumentoIdentidad[$scope.searchData.NumeroDocumentoIdentidad.indexOf('-') + 1]))
					{
						bcsComunes.msg("error",'Error','UI_RUT_INVALIDO');
						return;
					}
				}
			}
			if(!$scope.searchData.NumeroDocumentoIdentidad || $scope.searchData.NumeroDocumentoIdentidad.length==0 || $scope.recordData.tipoPersona === false || !$scope.recordData.tipoEnrolamiento) {
				bcsComunes.msg("error",'Error',$translate.instant('UI_DATOS_INCOMPLETOS'));
				return;
			}	
						
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlConsultarEntidadEnrolamiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=53";
			var r = $resource("", {}, {
				'ConsultarEntidadEnrolamiento': { method: "POST", url: urlConsultarEntidadEnrolamiento}
			});
			
			var datosConsulta = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'CLI',
				p_UsaReglasNegocio: 'S',
				p_IdTipoEntidad: 'CLIENTE',
				p_IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
				p_IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidadAdicional: $scope.searchData.NumeroDocumentoIdentidadAdicional,
				p_IdTipoDocumentoIdentidadAdicional: $scope.searchData.IdTipoDocumentoIdentidadAdicional,
				p_IdTipoPersona : $scope.recordData.tipoPersona,
				p_esRutInterno : $scope.configData.rutInterno == true ? 'S' : 'N'
			};
				
			var post = r.ConsultarEntidadEnrolamiento(datosConsulta);
			var b_Cliente_Existe = false;
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				//ERROR
				if (result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'S') {
					bcsComunes.ventanaMsgErr($translate.instant('UI_CLIENTE_EXISTE'),$scope.ventanaId);
					b_Cliente_Existe = true;
				} 
				
				if(!b_Cliente_Existe && !!result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR>0){
					bcsComunes.ventanaMsg({ 
							mensaje: $translate.instant('UI_ERROR'),
							conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				} else if(result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'N') { //EXISTE SOLO PERSONA
					if(result.P_OUT_CONSULTARENROLAMIENTO["0"]) {												
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];					
						$scope.recordData = {
							apellidoMaterno : dataIN.ApellidoMaterno,
							apellidoPaterno : dataIN.ApellidoPaterno,
							idCategoriaInversionistaDefecto : dataIN.CLI_IdCategoriaInversionista,
							idCategoriaInversionista : dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente : dataIN.CLI_IdTipoCliente,
							idTipoClienteCMF : dataIN.CLI_IdTipoClienteCMF,
							idClaseCliente : dataIN.CLI_IdClaseCliente,
							direccionCalle : dataIN.DireccionCalle,
							direccionNumero : dataIN.DireccionNumero,
							direccionDepartamento : dataIN.DireccionDepartamento,
							email : dataIN.Email,
							idEstadoCivil : dataIN.EstadoCivil,
							fechaNacimiento : dataIN.FechaNacimiento!=="0001-01-01 00:00:00.000"?util.date(dataIN.FechaNacimiento).toDate():null,
							idPaisResidenciaDefecto: dataIN.IdPaisResidencia,
							idPaisResidencia: dataIN.IdPaisResidencia,
							idCiudadDefecto : dataIN.IdCiudad,
							idCiudad : dataIN.IdCiudad,
							idDivisionPaisDefecto : dataIN.IdDivisionPais,
							idDivisionPais : dataIN.IdDivisionPais,
							idPaisNacimiento : dataIN.IdPaisNacimiento,
							idPaisNacimientoDefecto : dataIN.IdPaisNacimiento,
							nombres : dataIN.Nombres,
							idPerfilComercial : dataIN.POR_IdPerfilComercial,
							razonSocial : dataIN.RazonSocial,
							idGenero : dataIN.Sexo,
							idGeneroOriginal : dataIN.Sexo,
							idTipoTelefono : dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto : dataIN.IdTipoTelefono,
							telefono : dataIN.Telefono,
							tipoPersona : dataIN.Tipo,
							idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal,
							idEmpleadoEjecutivo:dataIN.CLI_IdEmpleadoEjecutivo,
							idEmpleadoEjecutivoDefecto:dataIN.CLI_IdEmpleadoEjecutivo,
							idClaseClienteDefecto:dataIN.CLI_IdClaseCliente,
							codigoAdicionalHolding:dataIN.CLI_CodigoAdicionalHolding,
							CodigoArea : dataIN.CLI_AreaCliente,
							tipoEnrolamiento : $scope.recordData.tipoEnrolamiento,
							CodigoPropositoTransaccionDefecto : dataIN.CodigoPropositoTransaccion,
							CodigoPropositoTransaccion : dataIN.CodigoPropositoTransaccion							
						};
					}else{
						bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_CLIENTE'),$scope.ventanaId);
					}
					
					$scope.recordData.BrokerObj = {};
					asignarValoresPorDefecto();
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = false;	
					$scope.configData.bloqueoCliente = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
					$scope.configData.bloqueoPorRutInterno = true;
					$scope.configData.deshabilitarRutInterno = true
				} else if (result.P_OUT_PERSONAEXISTE === 'N' && result.P_OUT_ENTIDADEXISTE === 'N') { //NO EXISTE PERSONA -> PUEDE HABER BORRADOR
					if (result.P_OUT_CONSULTARENROLAMIENTO["0"]) {
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];
						$scope.recordData = {
							apellidoMaterno : dataIN.ApellidoMaterno,
							apellidoPaterno : dataIN.ApellidoPaterno,
							idCategoriaInversionistaDefecto : dataIN.CLI_IdCategoriaInversionista,
							idCategoriaInversionista : dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente : dataIN.CLI_IdTipoCliente,
							idTipoClienteCMF : dataIN.CLI_IdTipoClienteCMF,
							idClaseCliente : dataIN.CLI_IdClaseCliente,
							direccionCalle : dataIN.DireccionCalle,
							direccionNumero : dataIN.DireccionNumero,
							direccionDepartamento : dataIN.DireccionDepartamento,
							email : dataIN.Email,
							idEstadoCivil : dataIN.EstadoCivil,
							fechaNacimiento : dataIN.FechaNacimiento!=="0001-01-01 00:00:00.000"?util.date(dataIN.FechaNacimiento).toDate():null,
							idPaisResidenciaDefecto: dataIN.IdPaisResidencia,
							idPaisResidencia: dataIN.IdPaisResidencia,
							idCiudadDefecto : dataIN.IdCiudad,
							idCiudad : dataIN.IdCiudad,
							idDivisionPaisDefecto : dataIN.IdDivisionPais,
							idDivisionPais : dataIN.IdDivisionPais,
							idPaisNacimiento : dataIN.IdPaisNacimiento,
							idPaisNacimientoDefecto : dataIN.IdPaisNacimiento,
							nombres : dataIN.Nombres,
							idPerfilComercial : dataIN.POR_IdPerfilComercial,
							razonSocial : dataIN.RazonSocial,
							idGenero : dataIN.Sexo,
							idGeneroOriginal : dataIN.Sexo,
							idTipoTelefono : dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto : dataIN.IdTipoTelefono,
							telefono : dataIN.Telefono,
							tipoPersona : dataIN.Tipo,
							idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal,
							idEmpleadoEjecutivo:dataIN.CLI_IdEmpleadoEjecutivo,
							idEmpleadoEjecutivoDefecto:dataIN.CLI_IdEmpleadoEjecutivo,
							idClaseClienteDefecto:dataIN.CLI_IdClaseCliente,
							codigoAdicionalHolding:dataIN.CLI_CodigoAdicionalHolding,
							CodigoArea : dataIN.CLI_AreaCliente, 
							tipoEnrolamiento : $scope.recordData.tipoEnrolamiento,
							CodigoPropositoTransaccionDefecto : dataIN.CodigoPropositoTransaccion,
							CodigoPropositoTransaccion : dataIN.CodigoPropositoTransaccion
						};
					}
					
					$scope.recordData.BrokerObj = {};
					asignarValoresPorDefecto();
					//BLOQUEAR CONTROLES DE ARRIBA
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = false;
					$scope.configData.bloqueoCliente = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
					$scope.configData.bloqueoPorRutInterno = true;
					$scope.configData.deshabilitarRutInterno = true
				}
				
				if($scope.SesionLocale=='PE'){
					//en caso de utilizar tipo documento ficticio.
					if($scope.searchData.IdTipoDocumentoIdentidadAsociado==="FIC" && result.P_OUT_PERSONAEXISTE ==="N"){
						$scope.recordData.documentoIdentidadNuevo = result.P_OUT_DOCUMENTOIDENTIDADFICTICIO;
					}

					//en caso de no ingresar documento adicional+
					if(result.P_OUT_PERSONAEXISTE ==="S" && !!result.P_OUT_CONSULTARENROLAMIENTO[0] && $scope.searchData.NumeroDocumentoIdentidadAdicional !== result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional){
						if($scope.searchData.NumeroDocumentoIdentidadAdicional===""){
							bcsComunes.ventanaMsgErr($translate.instant('UI_DOCUMENTO_ADICIONAL_OBLIGATORIO'),$scope.ventanaId);
							$scope.cancelar();
						}else{
							if(!!result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional && result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional !== ""){
								$scope.recordData.NumeroDocumentoIdentidadAdicional = result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional;	
							}
						}
					}
					if ($scope.searchData.IdTipoDocumentoIdentidadAsociado==="FIC" && $scope.configData.rutInterno === true) {
						if($scope.recordData.documentoIdentidadNuevo === 0 && !!result.P_OUT_CONSULTARENROLAMIENTO[0]) {
							$scope.recordData.NumeroDocumentoIdentidadAdicional = result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional;
							$scope.searchData.NumeroDocumentoIdentidadAdicional = $scope.recordData.NumeroDocumentoIdentidadAdicional;
							$scope.recordData.documentoIdentidadNuevo = $scope.recordData.NumeroDocumentoIdentidadAdicional;
						} else {
							$scope.searchData.NumeroDocumentoIdentidadAdicional = $scope.recordData.documentoIdentidadNuevo;
							$scope.recordData.NumeroDocumentoIdentidadAdicional = $scope.recordData.documentoIdentidadNuevo;
						}
					}
				}
				
				//obtener IdSocioDeNegocio para conyuge en caso de existir:
				if(($scope.recordData.idEstadoCivil==='CA'||$scope.recordData.idEstadoCivil==='CC')&&$scope.recordData.idPersonaConyuge>0){
					var filter = { field: "IdPersona", operator: "eq", value: Number($scope.recordData.idPersonaConyuge) }
					var conyugeDatasource=bcsConfiguracionSociodeNegocio.getDatasourceSocioNegocio(filter);
					conyugeDatasource.query().then(function(result){
						if(conyugeDatasource.data().length>0){
							$timeout(function() {
								$scope.recordData.IdSocNegPersonaConyuge=conyugeDatasource.data()[0].IdSocioDeNegocio;
								$scope.recordData.IdSocNegPersonaConyugeOriginal=conyugeDatasource.data()[0].IdSocioDeNegocio;	
							},1000);

						} 
					});
				}
								
				//Datos por defecto en pantalla!
				if($scope.recordData)  {
					$scope.recordData.idPaisResidenciaDefecto = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idCiudadDefecto = $scope.recordData.idCiudadDefecto;
					$scope.recordData.idCiudad = $scope.recordData.idCiudadDefecto;
					$scope.recordData.idDivisionPaisDefecto = $scope.recordData.idDivisionPaisDefecto;
					$scope.recordData.idDivisionPais = $scope.recordData.idDivisionPaisDefecto;
					$scope.recordData.idEmpleadoEjecutivoDefecto = $scope.recordData.idEmpleadoEjecutivoDefecto;
					$scope.recordData.idEmpleadoEjecutivo = $scope.recordData.idEmpleadoEjecutivoDefecto;
					$scope.recordData.RegimenConyugalOriginal = $scope.recordData.RegimenConyugal;
					$scope.recordData.idPaisNacimientoDefecto = $scope.recordData.idPaisNacimientoDefecto||$scope.$root.SesionIdPaisSistema;
					$scope.recordData.idEstadoCivilDefecto = 'SO';
				
					
				}else {
					$scope.recordData = {};
					$scope.recordData.idPaisResidenciaDefecto = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.IdEmpleadoEjecutivo = $scope.$root.SesionIdEmpleado;
				
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_BORRADOR'),$scope.ventanaId);
			});		
		};
		
		$scope.$watch('recordData.tipoPersona',function(newval,oldval) {
			bcsComunes.ventanaAutoHeight($scope.ventanaId);
			if($scope.SesionLocale == 'PE'){ //PERU
				if (newval === 'N' || newval === 'M'){
					$scope.formConfig.tipoDocIdentidadDef = 'DNI';
					$scope.formConfig.tipoDocIdentidadAdicionalDef = 'RUT';
				}else{
					$scope.formConfig.tipoDocIdentidadDef = 'RUC';
					$scope.formConfig.tipoDocIdentidadAdicionalDef = 'RUT';
				}
			}else{//CHILE
				if (newval === 'N'){
					limpiarCamposPersonaJuridica($scope.recordData);
				}else if (newval === 'J'){
					limpiarCamposPersonaNatural($scope.recordData);
				}	
				
				var CodigoTipoIndicador = (newval == "N")?"TCLICMF":"TCLICMFJ";
				
				$scope.configData.filtroTipoClienteCMF = {
					filtros : {IdTipoIndicador : {field : "CodigoTipoIndicador", 	operator : "eq", value : CodigoTipoIndicador}}
				};	
			}
		});
		
		
		$scope.$watch('configData.rutInterno', function(newval,oldval) {
			if (newval !== undefined) {
				if(newval === true) {
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
					$scope.configData.bloqueoPorRutInterno = false;
				} else {
					$scope.recordData.NumeroDocumentoIdentidadAdicional = '';
					$scope.cancelar();
				}
			}
		});
		$scope.$watch('searchData.IdTipoDocumentoIdentidadAsociado', function(data){
			if (data !== undefined) {
				if (data === 'FIC' && $scope.recordData.tipoPersona === 'J') {
					$scope.configData.deshabilitarRutInterno = false
				} else {
					$scope.configData.deshabilitarRutInterno = true;
				}
			}
		});
		//borra campos de persona juridica
		var limpiarCamposPersonaJuridica = function(registro) {
			if (!registro)return;
			registro.razonSocial = '';
		};
		
		//borra campos de persona natural
		var limpiarCamposPersonaNatural = function(registro) {
			if (!registro)return;
			registro.nombres = '';
			registro.apellidoPaterno = '';
			registro.apellidoMaterno = '';
		};
		
		$scope.validarEstadoCivil = function(){
			if($scope.recordData.idEstadoCivil!="CC" && $scope.recordData.idEstadoCivil!="CA"){
				$scope.recordData.IdSocNegPersonaConyuge = -1;
				$scope.recordData.RegimenConyugal = "";
			}
		}
		
		$scope.obtenerDatasourceGuardarBorrador = function(){
			var urlCrearBorrador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=52";
			var r = $resource("", {},{'CrearBorrador': { method: "POST", url: urlCrearBorrador}});
			return r;
		}
		
		$scope.limpiarCamposPersona = function(){
			if ($scope.recordData.tipoPersona === 'N'){
				limpiarCamposPersonaJuridica($scope.recordData);
			}else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
		}
		
		$scope.calcularIdCiudad = function(){
			return ($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad;
		}
		
		$scope.calcularIdPersonaConyuge = function(){
			return ($scope.recordData.RelacionadoObj&&$scope.recordData.RelacionadoObj.IdPersona>0)?$scope.recordData.RelacionadoObj.IdPersona:0;
		}
		
		$scope.obtenerDatosBorrador = function(){
			var IdCiudadCalculado = $scope.calcularIdCiudad();
			var IdPersonaConyuge  = $scope.calcularIdPersonaConyuge();
			var datosEnrolamiento =  {
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'CLIENTE',
	   				IdEntidadEnrolada: 0,
	   				IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAsociado: $scope.searchData.IdTipoDocumentoIdentidadAsociado || '',
	   				DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAdicional: $scope.searchData.IdTipoDocumentoIdentidadAdicional || '',
	   				DocumentoIdentidadAdicional: $scope.searchData.NumeroDocumentoIdentidadAdicional || '',
	   				Tipo: $scope.recordData.tipoPersona,
	   				Nombres: $scope.recordData.nombres || '',
	   				ApellidoPaterno: $scope.recordData.apellidoPaterno || '',
	   				ApellidoMaterno: $scope.recordData.apellidoMaterno || '',
	   				RazonSocial: $scope.recordData.razonSocial || '',
	   				IdPaisNacimiento: $scope.recordData.idPaisNacimiento || 0,
					IdPaisResidencia: $scope.recordData.idPaisResidencia || 0,
	   				FechaNacimiento: ($scope.recordData.fechaNacimiento) ? util.dateSPdate($scope.recordData.fechaNacimiento) : '0001-01-01',
	   				Sexo: $scope.recordData.idGenero || '',
	   				EstadoCivil:($scope.recordData.tipoEnrolamiento === 'EXPRESS') ? 'SO' : $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: IdCiudadCalculado || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0,
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento || '',
	   				IdPersonaConyuge:  IdPersonaConyuge || 0,
	   				RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista: $scope.recordData.idCategoriaInversionista || '',
	   				CLI_IdTipoCliente: $scope.recordData.idTipoCliente || '',
	   				CLI_IdTipoClienteCMF: $scope.recordData.idTipoClienteCMF || 0,
	   				CLI_IdClaseCliente: $scope.recordData.idClaseCliente || '',
	   				CLI_IdEmpleadoEjecutivo:$scope.recordData.idEmpleadoEjecutivo || $scope.recordData.idEmpleadoEjecutivoDefecto || 0,
					CLI_CodigoAdicionalHolding:$scope.recordData.codigoAdicionalHolding || '',
					CLI_AreaCliente:$scope.recordData.CodigoArea || '',
	   				POR_IdPerfilComercial: $scope.recordData.idPerfilComercialActual || '',
	   				EMP_CodigoEmpleado:'',
				    EMP_CodigoTipoCargoEmpleado:'',
				    EMP_IdEmpleadoSupervisor:0,
				    EMP_IdSucursal:0,
				    EMP_FechaInicioCargo:'0001-01-01',
				    EMP_FechaTerminoCargo:'0001-01-01',
				    CNT_CodigoInscripcion:'',
					CNT_Emisor:'',
					CNT_Depositos:'',
					CNT_Bolsa:'',
					CNT_Corredora:'',
					CNT_CamaraDeCompensacion:'',
					CNT_Banco:'',
					CNT_Boveda:'',
					CNT_Otros : '',
	   				PRO_IdAreaProveedor:0,
		   			EMP_CargoIndefinido : '',
					TipoEnrolamiento : $scope.recordData.tipoEnrolamiento,
					UsaDivisionPais : $scope.paisResidenciaSeleccionado.UsaDivisionPais,
					IdTipoCanalIngresoCorredor : 'OPT',
					CodigoPropositoTransaccion : $scope.recordData.CodigoPropositoTransaccion || ''
				}
			return datosEnrolamiento
		}
		
		$scope.prepararDatosParaGuardarBorrador = function(){
			$scope.etapa=='guardarborrador';
			$scope.validarEstadoCivil();
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			$scope.limpiarCamposPersona();
		}
		
		$scope.guardarBorrador = function(){
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			$scope.prepararDatosParaGuardarBorrador();
			var r = $scope.obtenerDatasourceGuardarBorrador();
			
			var datosBorrador = {
				p_ConsultarEnrolamiento : [$scope.obtenerDatosBorrador()],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_CodigoFormulario : obtenerCodigoFormulario($scope.recordData.tipoPersona, $scope.recordData.tipoEnrolamiento),
				p_IdTipoCanal : 'OPT'
			};
			
			var post = r.CrearBorrador(datosBorrador);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				if (result.P_OUT_REFID && result.P_OUT_REFID > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_BORRADOR_GUARDADO') + ': ' + result.P_OUT_REFID,
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
					$scope.configData.buscarCliente();
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				}
				bcsComunes.setConflictos(util.parseServerResponse(result))
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));
			});
		};
		
		$scope.crearCliente = function(){
			$scope.etapa=='guardar';
			$scope.validarEstadoCivil();
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=54";
			var r = $resource("", {}, {'CrearCliente': { method: "POST", url: urlCrearCliente}});
			if ($scope.recordData.tipoPersona === 'N'){
				limpiarCamposPersonaJuridica($scope.recordData);
			}else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
			var IdCiudadCalculado = ($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad;
			var IdPersonaConyuge=($scope.recordData.RelacionadoObj&&$scope.recordData.RelacionadoObj.IdPersona>0)?$scope.recordData.RelacionadoObj.IdPersona:0;
			var datosCrearCliente = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'CLIENTE',
	   				IdEntidadEnrolada: 0,
	   				IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAsociado: $scope.searchData.IdTipoDocumentoIdentidadAsociado || '',
	   				DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAdicional: $scope.searchData.IdTipoDocumentoIdentidadAdicional || '',
	   				DocumentoIdentidadAdicional: $scope.searchData.NumeroDocumentoIdentidadAdicional || '',
	   				Tipo: $scope.recordData.tipoPersona,
	   				Nombres: $scope.recordData.nombres || '',
	   				ApellidoPaterno: $scope.recordData.apellidoPaterno || '',
	   				ApellidoMaterno: $scope.recordData.apellidoMaterno || '',
	   				RazonSocial: $scope.recordData.razonSocial || '',
					IdPaisResidencia: $scope.recordData.idPaisResidencia || 0,
	   				IdPaisNacimiento: $scope.recordData.idPaisNacimiento || 0,
	   				FechaNacimiento: ($scope.recordData.fechaNacimiento) ? util.dateSPdate($scope.recordData.fechaNacimiento) : '0001-01-01',
	   				Sexo: $scope.recordData.idGenero || 'M',
	   				EstadoCivil: ($scope.recordData.tipoEnrolamiento === 'EXPRESS') ? 'SO' : $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: IdCiudadCalculado || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0,
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle? $scope.recordData.direccionCalle: $scope.recordData.BrokerObj.DireccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero? $scope.recordData.direccionNumero: $scope.recordData.BrokerObj.DireccionNumero || '',
	   				//DireccionDepartamento: $scope.recordData.direccionDepartamento? $scope.recordData.direccionDepartamento: $scope.recordData.BrokerObj.DireccionDepartamento || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento? $scope.recordData.direccionDepartamento: ($scope.recordData.BrokerObj? $scope.recordData.BrokerObj.DireccionDepartamento: '') || '',
	   				IdPersonaConyuge: IdPersonaConyuge || 0,
	   				RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista: $scope.recordData.idCategoriaInversionista? $scope.recordData.idCategoriaInversionista: $scope.recordData.BrokerObj.IdCategoriaInversionista || '',
	   				CLI_IdTipoCliente: $scope.recordData.idTipoCliente? $scope.recordData.idTipoCliente: $scope.recordData.BrokerObj.IdTipoCliente || '',
	   				CLI_IdTipoClienteCMF: $scope.recordData.idTipoClienteCMF? $scope.recordData.idTipoClienteCMF: $scope.recordData.BrokerObj.idTipoClienteCMF || '0',
	   				CLI_IdClaseCliente: $scope.recordData.idClaseCliente || '',
	   				CLI_IdEmpleadoEjecutivo:$scope.recordData.idEmpleadoEjecutivo? $scope.recordData.idEmpleadoEjecutivo: $scope.recordData.BrokerObj.IdEmpleadoEjecutivo || 0,
					CLI_CodigoAdicionalHolding:$scope.recordData.codigoAdicionalHolding || '',
					CLI_ClienteFinal:$scope.clienteFinal ? 'S' : 'N',
	   				POR_IdPerfilComercial: $scope.recordData.idPerfilComercialActual? $scope.recordData.idPerfilComercialActual: $scope.recordData.BrokerObj.IdPerfilComercialBaseOrigen || '',
	   				POR_IdPersonaBrokerExtranjero: $scope.recordData.BrokerObj.IdPersona || 0,
	   				EMP_CodigoEmpleado:'',
				    EMP_CodigoTipoCargoEmpleado:'',
				    EMP_IdEmpleadoSupervisor:0,
				    EMP_IdSucursal:0,
				    EMP_FechaInicioCargo:'0001-01-01',
				    EMP_FechaTerminoCargo:'0001-01-01',
				    CNT_CodigoInscripcion:'',
					CNT_Emisor:'',
					CNT_Depositos:'',
					CNT_Bolsa:'',
					CNT_Corredora:'',
					CNT_CamaraDeCompensacion:'',
					CNT_Banco:'',
					CNT_Boveda:'',
					CNT_Otros : '',
	   				PRO_IdAreaProveedor:0,
		   			EMP_CargoIndefinido : '',
		   			EsRutInterno : $scope.configData.rutInterno == true ? 'S' : 'N',
					TipoEnrolamiento : $scope.recordData.tipoEnrolamiento,
					CLI_AreaCliente: $scope.recordData.CodigoArea || '',
					UsaDivisionPais: $scope.paisResidenciaSeleccionado.UsaDivisionPais || 'N',
					IdTipoCanalIngresoCorredor: 'OPT',
					CodigoPropositoTransaccion : $scope.recordData.CodigoPropositoTransaccion || ''
				}],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_CodigoFormulario : obtenerCodigoFormulario($scope.recordData.tipoPersona, $scope.recordData.tipoEnrolamiento),
				p_IdTipoCanal : "OPT",
				p_UsaReglasNegocio: 'S'
			};
			var post = r.CrearCliente(datosCrearCliente);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				if(result.P_OUT_ERRORESCONTADOR==0&&result.P_OUT_REFIDCLIENTE&&result.P_OUT_REFIDCLIENTE>0){
					var lngIdCliente = result.P_OUT_REFIDCLIENTE;
					var documentoIdentidad = result.P_OUT_DOCUMENTOIDENTIDAD;
					/*INICIO EDN - Una vez creado el Cliente y el portafolio se debe Modificar la Plantilla Producto*/
					if($scope.SesionLocale == 'PE' && $scope.clienteFinal){
						var filtro = Array(); 
						filtro.push({ field: "IdCliente" , operator: "eq", value: result.P_OUT_REFIDCLIENTE });
						var dsConsultaPortafolio = new bcs.DataSource(datosPortafolio);
						dsConsultaPortafolio.query({filter: filtro}).then(function()
						{
							if(dsConsultaPortafolio.view().length > 0){
								var urlModificaPortafolio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=57";
								var resp = $resource("", {}, {'ModificaPortafolio': { method: "POST", url: urlModificaPortafolio}});
								var datosModificaPortafolio = {
										p_IdEmpresa : $scope.SesionIdEmpresa,
										p_IdUsuario : $scope.SesionIdUsuario,
										p_IdModulo  : 'SOC',
										p_UsaReglasNegocio	: 'S',
										p_IdPortafolio		: dsConsultaPortafolio.view()[0].IdPortafolio,	
										p_RegistroCambio : [{
											CampoModificado: 'IdPerfilProductos',
											ValorNuevo : $scope.recordData.BrokerObj ? $scope.recordData.BrokerObj.IdPerfilProductos : undefined
										}]
									};
								var postCon = resp.ModificaPortafolio(datosModificaPortafolio);
								postCon.$promise.then(function(result){
									if(result.P_OUT_ERRORESCONTADOR==0 && result.P_OUT_REFID && result.P_OUT_REFID>0){
										$scope.cancelar();
										bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_CLIENTE_CREADO') + ': ' + documentoIdentidad,
											conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
									}
								},function(result){
									bcsComunes.ventanaOverlay($scope.ventanaId,false);
									$scope.etapa='';
									bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_MODIFICAR_PORTAFOLIO'),
										conflictos:util.parseServerResponse(result)},$scope.ventanaId);
									bcsComunes.setConflictos(util.parseServerResponse(result));
								});
							}
						});
					}else{
						$scope.cancelar();
						bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_CLIENTE_CREADO') + ': ' + documentoIdentidad,
							conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
					}
					/*FIN EDN*/
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_CLIENTE'),
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				}
				bcsComunes.setConflictos(util.parseServerResponse(result));
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_CLIENTE'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));

			});
		};			
				
		// Se obtienen placeholder
		$scope.placeholder = {};
		$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		
		$scope.$watch('recordData.idTipoTelefono',function(newval,oldval){
			if (newval) {
				if ($scope.recordData.idTipoTelefono === 'FIJO') {
					$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
				}
				else if ($scope.recordData.idTipoTelefono === 'MOVIL') {
					$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO_MOVIL;
				}
				else {
					$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
				}
			}
			else {
				$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
			}
		});
		
		
		$scope.$watch('searchData.IdTipoDocumentoIdentidad',function(newVal){
			if(newVal!==undefined){
				if(newVal==='R') 
					$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
				else if(newVal==='P')
					$scope.placeholder.documentoIdentidad = '';
				else $scope.placeholder.documentoIdentidad = '';
			}
		});
		// Valida formato Telefono
		$scope.validaFormatoTelefono = function(){
			if($scope.SesionLocale=='PE')return true;
			if($scope.recordData.telefono==null) $scope.recordData.telefono='';
	    	if ($scope.recordData.telefono !== undefined && $scope.recordData.telefono !== '' && region.ValidaFormatoNumeroTelefono($scope.recordData.telefono)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO"));
				$scope.formCrearCliente.$valid = undefined;
				return false;
			}
	    	return true;
	    };
		// Valida formato email
		$scope.validaFormatoEmail = function(){
			if($scope.recordData.email==null) $scope.recordData.email='';
	    	if ($scope.recordData.email !== undefined && $scope.recordData.email !== '' && region.ValidaFormatoEmail($scope.recordData.email)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
				$scope.formCrearCliente.$valid = undefined;
				return false;
			}
	    	return true;
	    };
		// Valida fecha nacimiento
		$scope.validaFechaNacimiento = function() {
			if (!$scope.recordData.fechaNacimiento
						|| !region.validarFechaNacimiento($scope.recordData.fechaNacimiento, new Date($scope.$root.SesionFechaSistema) , 100)){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FECHA_IMPROBABLE"));
				return false;
			}
			return true;
		};
		$scope.formConfig.oConfiguracionRegimenConyugal={filtros : {}};
		$scope.$watch('recordData.idEstadoCivil',function (nval,oval) {
			bcsComunes.ventanaAutoHeight($scope.ventanaId);
			if($scope.recordData.idPaisResidencia){
				var filters = {};
				var IdPais = {field: "IdPais", operator: "eq", value: $scope.recordData.idPaisResidencia};
				if(nval ==="CC"){
					filters = {
						complejo : [IdPais, []],
						logic: "and",
						filters: [
							{
							logic: "or",
							filters: [
								{ field: "IdTipoRegimenConyugal", operator: "eq", value: "PG" },
								{ field: "IdTipoRegimenConyugal", operator: "eq", value: "SB" }
							]},
						]
					};
										
				}else{
					filters = IdPais;
				}
				$scope.formConfig.oConfiguracionRegimenConyugal.filtros = filters;	
				
			}
		});
		
		$scope.siguiente = function() {
			
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			$scope.prepararDatosParaGuardarBorrador();
			var r = $scope.obtenerDatasourceGuardarBorrador();
			
			var datosBorrador = {
				p_ConsultarEnrolamiento : [$scope.obtenerDatosBorrador()],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_CodigoFormulario : obtenerCodigoFormulario($scope.recordData.tipoPersona, $scope.recordData.tipoEnrolamiento),
				p_IdTipoCanal : 'OPT'
			};
			
			var post = r.CrearBorrador(datosBorrador);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				var matriz = result.P_FORMULARIOS_SOCIO_NEGOCIO.reduce(function(acum, item){
					acum[item.Formulario] = item.CodigoFormulario;
					return acum;
				},{});
				if (result.P_OUT_REFID && result.P_OUT_REFID > 0){
					bcsComunes.ventanaAbrir("EnrolamientoSocioNegocio", {IdEnrolamiento : result.P_OUT_REFID, IdTipoPersona : $scope.recordData.tipoPersona, TipoEnrolamiento : $scope.recordData.tipoEnrolamiento, Matriz : matriz} );
					bcsComunes.ventanaCerrar($scope.ventanaId);
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				}
				bcsComunes.setConflictos(util.parseServerResponse(result));
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));
			});
						 
		};
		
		$scope.crearConyuge = function() {
			var parametros = {
				modoRapido:true, 
				documentoIdentidadCliente:$scope.searchData.NumeroDocumentoIdentidad,
				tipoPersonaCrear:'N',
				esVentanaConyugue: true
			};
			var id1 = bcsComunes.ventanaAbrir("CrearPersona",parametros,false,false); 
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				if (data.creado === true){
					$scope.recordData.IdSocNegPersonaConyuge = data.IdSocioNegocio;
					setTimeout(function(){
						$scope.recordData.IdSocNegPersonaConyuge = data.IdSocioNegocio;
					},1000);
				}
			}); 
		};
		$scope.blurDocumentoIdentidad = function(tipo,adicional){
			var vm=(adicional)?'NumeroDocumentoIdentidadAdicional':'NumeroDocumentoIdentidad';
			$scope['errorValidar'+vm]=false;
			if(!tipo)return;
			var d=$scope.searchData[vm]||"";
			if(d.length==0)return;
			if(tipo=='RUT'){
				if(d.length>8){
					bcsComunes.msg("warning","warning",vm+':'+$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
					$scope['errorValidar'+vm]=true;
				}else d=util.PadLeft($scope.searchData[vm],'0',8);
			}
			if(tipo=='IUP'){
				if(d.length<14){
					bcsComunes.msg("warning","warning",vm+':'+$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
					$scope['errorValidar'+vm]=true;
				}if(d.length>14){
					bcsComunes.msg("warning","warning",vm+':'+$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
					$scope['errorValidar'+vm]=true;
				}else{
					var validacion=util.validarIUP(d);
					if(!validacion.escorrecta){
						bcsComunes.msg("warning","warning",vm+':'+$translate.instant(validacion.mensaje));
						$scope['errorValidar'+vm]=true;
					}
				}
			}
			$scope.searchData[vm]=d;
		};

		$scope.$watch('searchData.IdTipoDocumentoIdentidadAdicional',function(tipo){
			if(tipo=='RUT')$scope.formConfig.largoMaximoNumeroAdicional=10;
			if(tipo=='IUP')$scope.formConfig.largoMaximoNumeroAdicional=14;
			else $scope.formConfig.largoMaximoNumeroAdicional=0;
			$scope.blurDocumentoIdentidad(tipo,true);
		});
		$scope.$watch('searchData.IdTipoDocumentoIdentidad',function(tipo){
			if(tipo=='RUT')$scope.formConfig.largoMaximoNumero=10;
			if(tipo=='IUP')$scope.formConfig.largoMaximoNumero=14;
			else $scope.formConfig.largoMaximoNumero=0;
			$scope.blurDocumentoIdentidad(tipo,false);
		});
		$scope.$watchGroup(['recordData.tipoPersona','searchData.IdTipoDocumentoIdentidad','searchData.NumeroDocumentoIdentidad'],function(newArr){
			for(var i=0; i < newArr.length; i++){
				if(newArr[i]===undefined || newArr[i]===null || newArr[i]==="" || newArr[i]===false){
					$scope.errorValidarNumeroDocumentoIdentidad=true;
					break;
				}else{
					$scope.errorValidarNumeroDocumentoIdentidad=false;
				}
			}
		});
		if($scope.SesionLocale==="PE"){
			$scope.$watchGroup(['recordData.tipoPersona','searchData.IdTipoDocumentoIdentidadAdicional','searchData.NumeroDocumentoIdentidadAdicional'],function(newArr){
				for(var i=0; i < newArr.length; i++){
					if(newArr[i]===undefined || newArr[i]===null || newArr[i]==="" || newArr[i]===false){
						$scope.errorValidarNumeroDocumentoIdentidadAdicional=true;
						break;
					}else{
						$scope.errorValidarNumeroDocumentoIdentidadAdicional=false;
					}
				}
			});
		}
		
		$scope.$watchGroup(['recordData.tipoEnrolamiento','recordData.tipoPersona'], function(newArr) {		
			if(newArr[0]){
				var tipoEnrolamiento = newArr[0];
				var tipoPersona = newArr[1];
				var codigoFormulario = obtenerCodigoFormulario(tipoPersona, tipoEnrolamiento);	
				bcsServicioPlataforma.CamposRequeridosFormulario(codigoFormulario, function(respuesta) {
			    	$scope.camposRequeridosCC = respuesta;
				});
			}
		});
		
		var asignarValoresPorDefecto = function asignarValoresPorDefecto(){
			$.each($scope.camposRequeridosCC,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !$scope.recordData[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					$scope.recordData[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		var obtenerCodigoFormulario = function(tipoPersona, tipoEnrolamiento){
			var codigoFormulario = "";
			if(tipoEnrolamiento === 'FULL'){
               	codigoFormulario = tipoPersona === 'N' ? 'INGRESAR_CLIENTE_FULL_NATURAL' : 'INGRESAR_CLIENTE_FULL_JURIDICA';
			}
			else if(tipoEnrolamiento === 'EXPRESS'){
				codigoFormulario = tipoPersona === 'N' ? 'INGRESAR_CLIENTE_EXPRESS_NATURAL': 'INGRESAR_CLIENTE_EXPRESS_JURIDICA';
			}
			return codigoFormulario;
		}
		
		/**************************
		 * 	WORKING NET COMBOBOX
		 **************************/ 
		//los procesados no deben tener caché. No aplicar a combobox-socio-negocio
		var wknDefinition=['workingNet.TipoPersona',
		                   'workingNet.TipoDocumentoIdentidad',
		                   'workingNet.TipoDocumentoIdentidadAdicional',
		                   'workingNet.PaisNacimiento',
		                   'workingNet.EstadoCivil',
		                   //'workingNet.Conyuge',
		                   'workingNet.RegimenConyugal',
		                   'workingNet.PaisResidencia',
		                   //'workingNet.CiudadResidencia',
		                   'workingNet.DivisionPaisResidencia',
		                   'workingNet.TipoTelefono',
		                   'workingNet.CategoriaInversionista',
		                   'workingNet.PerfilComercial',
		                   'workingNet.TipoCliente',
		                   'workingNet.TipoClienteCMF',
		                   'workingNet.ClaseCliente',
		                   'workingNet.EmpleadoEjecutivo',
						   'workingNet.CodigoPropositoTransaccion'];
		var wknDef=[];for(vm in wknDefinition)wknDef.push(wknDefinition[vm].replace('workingNet.',''));
		var errorRed=false;
		$scope.$watchGroup(wknDefinition,function(newVal){
			if(errorRed===true)return;
			for(vm in wknDef){//verificar error en llamada de red.
				if($scope.workingNet[wknDef[vm]]===-1){
					bcsComunes.msg("warning","warning",$translate.instant("UI_ERROR_EN_COMUNICACION")+'. No se pudo cargar combobox: '+wknDef[vm]);
					var p=bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_EN_COMUNICACION'),$scope.ventanaId);
					bcsComunes.ventanaPromesa(p).then(function(){bcsComunes.ventanaCerrar($scope.ventanaId);});                                       
					errorRed=true;
					return;
				}}
			for(vm in wknDef){
				if($scope.workingNet[wknDef[vm]]===undefined)return;}
			if($scope.etapa=='inicio'){
				for(vm in wknDef)if($scope.workingNet[wknDef[vm]]===0)return;
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
			}else{for(vm in wknDef){if($scope.workingNet[wknDef[vm]]==0){
				bcsComunes.ventanaOverlay($scope.ventanaId,true);return;}}
				if($scope.etapa==''){bcsComunes.ventanaOverlay($scope.ventanaId,false);}
			}
		});
		
		//defaults de pantalla
		$scope.limpiarPantalla();
	});
	
}());
//------------------------- FIN CONTROLLER --------------------------	