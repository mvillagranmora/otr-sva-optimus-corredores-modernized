/*************************************************************
 * Purpose	: Crear Persona
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloOrdenes').controller('CrearPersonaCtrl', 
	function($scope,bcsComunes,$translate,$resource,bcsConfiguracionSociodeNegocio,bcsAlerta,OPCIONES_BCSALERTA, $timeout ){
		
		bcsConfiguracionSociodeNegocio.CONFIGURACION.promise.then(function(result){
			//se invierte la logica de true y false, porque el html de crear persona
			//esta al reves del crear cliente
			if (bcsConfiguracionSociodeNegocio.CONFIGURACION['CLI.BloqueoDocIdentidad'] === 'S')
				$scope.configData.TipoDocumentoIdentidadBloqueado = false;
			else
				$scope.configData.TipoDocumentoIdentidadBloqueado = true;
		});
		
		$scope.ventanaId = bcsComunes.ventanaWinId($scope);
		$scope.etapa='inicio';
		$scope.workingNet = {};
		bcsComunes.ventanaOverlay($scope.ventanaId,true);
		bcsComunes.ventanaAutoHeight($scope.ventanaId);
		$scope.configData = {};
		$scope.searchData = {};
		$scope.recordData = {};
		$scope.seleccionado = {};
		$scope.kOptFechaNacimiento = {};
		$scope.kOptFechaNacimiento.max = new Date();
		var parametros = bcsComunes.ventanaParams($scope.ventanaId);
		$scope.modoRapido = parametros.modoRapido;
		$scope.juridicos = (parametros.juridicos!=undefined) ? parametros.juridicos : true;
		$scope.configData.habilitarTipoPersona=true;
		
		$scope.ValidaSalidaESC = function(){
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_ENROLAMIENTO_INCONCLUSO'));
			t.result.then(function(result){
					if (result === OPCIONES_BCSALERTA.NO) {
					bcsComunes.ventanaCerrar($scope.ventanaId);
				}
			});	
		};
		
		
		//revisión de los parámetros de entrada ventana.
		if($scope.modoRapido) {
			if(parametros.tipoPersonaCrear){
				$scope.tipoPersonaDefecto = parametros.tipoPersonaCrear;
				$scope.configData.habilitarTipoPersona=false;
			}else $scope.tipoPersonaDefecto='N';
			$scope.documentoIdentidadCliente = parametros.documentoIdentidadCliente;
			bcsComunes.ventanaAutoHeight($scope.ventanaId);
		}else{$scope.tipoPersonaDefecto = 'J';}

		if(parametros.modoEditar) {
			if(parametros.tipoPersonaCrear){
				$scope.tipoPersonaDefecto = parametros.tipoPersonaCrear;
				$scope.configData.habilitarTipoPersona=false;
			}else $scope.tipoPersonaDefecto='N';
			$scope.documentoIdentidadCliente = parametros.documentoIdentidadCliente;
		}
	
		
		$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
		$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA

		$scope.formConfig = {};
		$scope.formConfig.largoMaximoNumeroAdicional=0;
		$scope.formConfig.largoMaximoNumero=0;
		$scope.fnLoadFormConfig = function(SesionLocale){
			if(SesionLocale == 'PE'){ //peru
				$scope.formConfig.showNumeroAdicional = false;
				$scope.formConfig.requiereNumeroAdicional = false;
				$scope.formConfig.labelNumeroIdentidad = 'UI_NUMERO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'DNI';
				$scope.formConfig.tipoDocIdentidadAdicionalDef = 'RUT';
				$scope.recordData.tipoDocumentoIdentidad = $scope.formConfig.tipoDocIdentidadDef;
			}
			else{ //otro
				$scope.formConfig.showNumeroAdicional = false;
				$scope.formConfig.requiereNumeroAdicional = false;
				$scope.formConfig.labelNumeroIdentidad = 'UI_DOCUMENTO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'R';
				$scope.formConfig.tipoDocIdentidadAdicionalDef = 'R';
			}
			$scope.$apply;
		}
		$scope.fnLoadFormConfig($scope.SesionLocale);
		
		$scope.configData.filtroTipoDocumentoIdentidad = {
			PorDefecto : {field: "PorDefecto", operator: "eq", value: "S"},
			TipoPersona : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		};
		$scope.configData.filtroTipoDocumentoIdentidadAdicional = {
			PorDefecto : {field: "PorDefecto", operator: "eq", value: "N"},
			TipoPersona : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		};
		
		$scope.configData.filtroCiudad = {
			IdPais : {field: "IdPais", operator: "eq", value: -1}
		};	
		$scope.configData.filtroDivisionPais = {
			IdPais : {field: "IdPais", operator: "eq", value: -1},
			IdCiudad : {field: "IdCiudad", operator: "neq", value: 0} //no pueden haber divisiones sin ciudad!
		};	
		
		$scope.configData.filtroPerfilComercial = {
			IdCategoriaInversionista : {field: "IdCategoriaInversionista", operator: "eq", value: ""}
		};

		$scope.configData.filtroTipoPersona = {
			complejo : [
				{field: "IdTipoPersona", operator: "eq", value: "N"},
				{field: "IdTipoPersona", operator: "eq", value: "J"},
				{field: "IdTipoPersona", operator: "eq", value: "M"}
			]  ,
			logic: "and",
			filters: [
				{
				  logic: "or",
				  filters: getFiltersTipoPersona()
				}
			]
		};

		function getFiltersTipoPersona(){
			var filters = [];			
			filters.push({field: "IdTipoPersona", operator: "eq", value: "N"});
			console.log("**********************************************************esVentanaConyugue : "+ parametros.esVentanaConyugue);
			if(!parametros.esVentanaConyugue){
			filters.push({field: "IdTipoPersona", operator: "eq", value: "J"});
			}
			if($scope.SesionLocale == 'PE' && !$scope.modoRapido){
				filters.push({field: "IdTipoPersona", operator: "eq", value: "M"});
			}
			return filters;
		}
		
		if(parametros.esVentanaConyugue){
			$scope.configData.bloqueoPersona = false;
		}
		
		$scope.cancelar = function() {
			$scope.limpiarPantalla();
			$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
			$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.TipoDocumentoIdentidadBloqueado = true;
			$scope.recordData.tipoDocumentoIdentidad = $scope.formConfig.tipoDocIdentidadDef;
		};
		
		$scope.limpiarPantalla = function() {
			$scope.recordData = { 
				tipoPersona :'N',
				idCategoriaInversionista : '',
				idTipoCliente : '',
				IdEmpleadoEjecutivo : 0,
				idPaisResidencia : $scope.$root.SesionIdPaisSistema
			};
			//if ($scope.modoRapido) $scope.recordData.tipoPersona='J';
		};
		
		$scope.configData.buscarPersona = function() {
			if($scope.modoRapido && $scope.documentoIdentidadCliente && $scope.documentoIdentidadCliente === $scope.searchData.NumeroDocumentoIdentidad){
				//modo rapido, se ingreso el mismo documento de identidad del ingreso padre.
				bcsComunes.msg("error",'Error',$translate.instant('UI_PERSONA_EN_FORMULARIO_PADRE'));
				return false;
			}
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			//$scope.etapa='';
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
			if(!$scope.searchData.NumeroDocumentoIdentidad || $scope.searchData.NumeroDocumentoIdentidad.length==0 || $scope.recordData.tipoPersona === false) {
				bcsComunes.msg("error",'Error',$translate.instant('UI_DATOS_INCOMPLETOS'));
				return;
			}	
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlConsultarEntidadEnrolamiento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=53";
			var r = $resource("", {},
		    {
				'ConsultarEntidadEnrolamiento': { method: "POST", url: urlConsultarEntidadEnrolamiento}
			});
			
			var datosConsulta = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'CLI',
				p_UsaReglasNegocio: 'S',
				p_IdTipoEntidad: 'PERSONA',
				p_IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
				p_IdTipoPersona : $scope.recordData.tipoPersona,
				p_esRutInterno : 'N'
			};
			
			var post = r.ConsultarEntidadEnrolamiento(datosConsulta);
			
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				//ERROR
				if (result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'S') {
					bcsComunes.msg("error",'Error',$translate.instant('UI_PERSONA_EXISTE'));
				}
				//EXISTE SOLO PERSONA
				else if (result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'N') {
					
					if (result.P_OUT_CONSULTARENROLAMIENTO["0"]) {
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];
						$scope.recordData = {
							apellidoMaterno : dataIN.ApellidoMaterno,
							apellidoPaterno : dataIN.ApellidoPaterno,
							idCategoriaInversionista : dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente : dataIN.CLI_IdTipoCliente,
							direccionCalle : dataIN.DireccionCalle,
							direccionNumero : dataIN.DireccionNumero,
							direccionDepartamento : dataIN.DireccionDepartamento,
							email : dataIN.Email,
							idEstadoCivil : dataIN.EstadoCivil,
							fechaNacimiento : util.date(dataIN.FechaNacimiento).toDate(),
							idPaisResidenciaDefecto: dataIN.IdPais,
							idPaisResidencia: dataIN.IdPais,
							idCiudadDefecto : dataIN.IdCiudad,
							idCiudad : dataIN.IdCiudad,
							idDivisionPaisDefecto : dataIN.IdDivisionPais,
							idDivisionPais : dataIN.IdDivisionPais,
							idPaisNacimiento : dataIN.IdPaisNacimiento,
							nombres : dataIN.Nombres,
							idPerfilComercial : dataIN.POR_IdPerfilComercial,
							razonSocial : dataIN.RazonSocial,
							idGenero : dataIN.Sexo,
							idTipoTelefono : dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto : dataIN.IdTipoTelefono,
							telefono : dataIN.Telefono,
							tipoPersona : dataIN.Tipo,
							idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal
						};
					}else{
						bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_PERSONA'),$scope.ventanaId);
						console.log(JSON.stringify(result));
					}
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = true;
					$scope.configData.bloqueoCliente = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = false;

				}
				
				//NO EXISTE PERSONA -> PUEDE HABER BORRADOR
				else if (result.P_OUT_PERSONAEXISTE === 'N' && result.P_OUT_ENTIDADEXISTE === 'N') {
					if (result.P_OUT_CONSULTARENROLAMIENTO["0"]) {
						var dataIN = result.P_OUT_CONSULTARENROLAMIENTO["0"];
						$scope.recordData = {
							apellidoMaterno : dataIN.ApellidoMaterno,
							apellidoPaterno : dataIN.ApellidoPaterno,
							idCategoriaInversionista : dataIN.CLI_IdCategoriaInversionista,
							idTipoCliente : dataIN.CLI_IdTipoCliente,
							direccionCalle : dataIN.DireccionCalle,
							direccionNumero : dataIN.DireccionNumero,
							direccionDepartamento : dataIN.DireccionDepartamento,
							email : dataIN.Email,
							idEstadoCivil : dataIN.EstadoCivil,
							fechaNacimiento : util.date(dataIN.FechaNacimiento).toDate(),
							idPaisResidenciaDefecto: dataIN.IdPais,
							idPaisResidencia: dataIN.IdPais,
							idCiudadDefecto : dataIN.IdCiudad,
							idCiudad : dataIN.IdCiudad,
							idDivisionPaisDefecto : dataIN.IdDivisionPais,
							idDivisionPais : dataIN.IdDivisionPais,
							idPaisNacimiento : dataIN.IdPaisNacimiento,
							nombres : dataIN.Nombres,
							idPerfilComercial : dataIN.POR_IdPerfilComercial,
							razonSocial : dataIN.RazonSocial,
							idGenero : dataIN.Sexo,
							idTipoTelefono : dataIN.IdTipoTelefono,
							idTipoTelefonoDefecto : dataIN.IdTipoTelefono,
							telefono : dataIN.Telefono,
							tipoPersona : dataIN.Tipo,
							idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal
						};
					}
					else{	
					}
					//BLOQUEAR CONTROLES DE ARRIBA
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = false;
					$scope.configData.bloqueoCliente = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = false;
				}
				//en caso de ser peru, y utilizar tipo documento ficticio.
				if($scope.SesionLocale=='PE' && $scope.searchData.IdTipoDocumentoIdentidadAsociado==="FIC"){
					$scope.recordData.documentoIdentidadNuevo = result.P_OUT_DOCUMENTOIDENTIDADFICTICIO;
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
				}else {
					$scope.recordData = {};
					$scope.recordData.idPaisResidenciaDefecto = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.$root.SesionIdPaisSistema;
					//if($scope.modoRapido)$scope.recordData.tipoPersona='J';
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_BORRADOR'),$scope.ventanaId);
				console.log(JSON.stringify(result));
			});		
		};
		
		$scope.$watch('recordData.tipoPersona',function(newval,oldval) {
			if (newval === 'N')
			{
				limpiarCamposPersonaJuridica($scope.recordData);
			}else if (newval === 'J'){
				limpiarCamposPersonaNatural($scope.recordData);
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
		
		$scope.guardarBorrador = function(){
			$scope.etapa=='guardarborrador';
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearBorrador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=52";
			var r = $resource("", {},{'CrearBorrador': { method: "POST", url: urlCrearBorrador}});
			if ($scope.recordData.tipoPersona === 'N' || $scope.recordData.tipoPersona === 'M'){
				limpiarCamposPersonaJuridica($scope.recordData);
			}else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
			var IdCiudadCalculado = ($scope.modoRapido)?0:(($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad);
			var IdPersonaConyuge=($scope.seleccionado.RelacionadoObj&&$scope.seleccionado.RelacionadoObj.IdPersona>0)?$scope.seleccionado.RelacionadoObj.IdPersona:0;
			var datosBorrador = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'PERSONA',
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
	   				FechaNacimiento: $scope.recordData.fechaNacimiento || '0001-01-01',
	   				Sexo: $scope.recordData.idGenero || '',
	   				EstadoCivil: $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: IdCiudadCalculado || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0,
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento || '',
	   				FechaCreacion: new Date(),
	   				FechaModificacion: new Date(),
	   				IdPersonaConyuge: IdPersonaConyuge || 0,
	   	   			RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista:'',
	   				CLI_IdTipoCliente:'',
	   				CLI_IdClaseCliente:'',
	   				CLI_IdTipoClienteCMF:0,
	   				CLI_IdEmpleadoEjecutivo:0,
	   				CLI_CodigoAdicionalHolding:'',
	   				POR_IdPerfilComercial:'',
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
	   				EMP_CargoIndefinido : ''			    
				}],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S'
			};
			var post = r.CrearBorrador(datosBorrador);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				if (result.P_OUT_REFID && result.P_OUT_REFID > 0){
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_BORRADOR_GUARDADO') + ': ' + result.P_OUT_REFID,
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
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
				console.log(JSON.stringify(result));
				bcsComunes.setConflictos(util.parseServerResponse(result));
			});
		};
		
		$scope.crearCliente = function(){
			$scope.etapa=='guardar';
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=90";
			var r = $resource("", {},{'CrearCliente': { method: "POST", url: urlCrearCliente}});
			if ($scope.recordData.tipoPersona === 'N'){
				limpiarCamposPersonaJuridica($scope.recordData);
			}
			else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
			var IdCiudadCalculado = ($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad;
			var IdPersonaConyuge=($scope.seleccionado.RelacionadoObj&&$scope.seleccionado.RelacionadoObj.IdPersona>0)?$scope.seleccionado.RelacionadoObj.IdPersona:0;
			var datosCrearCliente = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'PERSONA',
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
	   				FechaNacimiento: ($scope.recordData.fechaNacimiento) ? util.dateSPdate($scope.recordData.fechaNacimiento) : '0001-01-01',
	   				Sexo: $scope.recordData.idGenero || 'M',
	   				EstadoCivil: $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: IdCiudadCalculado || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0, // DIVISIONPAIS
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento || '',
	   				FechaCreacion: new Date(),
	   				FechaModificacion: new Date(),
	   				IdPersonaConyuge: IdPersonaConyuge || 0,
   					RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista:'',
	   				CLI_IdTipoCliente:'',
	   				CLI_IdClaseCliente:'',
	   				CLI_IdEmpleadoEjecutivo:0,
	   				CLI_CodigoAdicionalHolding:'',
	   				POR_IdPerfilComercial:'',
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
	   				EsRutInterno : 'N'
				}],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
			};
			console.log(JSON.stringify(datosCrearCliente));
			var post = r.CrearCliente(datosCrearCliente);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				var titulo = "";
				
				if(result.P_OUT_ERRORESCONTADOR==0&&result.P_OUT_IDSOCIONEGOCIO&&result.P_OUT_REFID&&result.P_OUT_REFID>0){
					titulo = $translate.instant('UI_PERSONA_CREADA') + ': ' + result.P_OUT_DOCUMENTOIDENTIDAD;
					$scope.cancelar();
				}else{
					titulo = $translate.instant('UI_ERROR_CREAR_PERSONA');
				}

				var idMensajeModal = bcsComunes.ventanaMsg({mensaje: titulo, 
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				
				bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
					if($scope.modoRapido) {
						bcsComunes.ventanaData($scope.ventanaId,{creado:true,IdSocioNegocio:result.P_OUT_IDSOCIONEGOCIO});
						bcsComunes.ventanaCerrar($scope.ventanaId);
					}
					bcsComunes.setConflictos(util.parseServerResponse(result));	
				});					
				
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				var idMensajeModal = bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_PERSONA'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				
				bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
					bcsComunes.setConflictos(util.parseServerResponse(result));
					console.log(JSON.stringify(result));
				});
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
	    	if ($scope.recordData.telefono !== undefined && $scope.recordData.telefono !== '' && region.ValidaFormatoNumeroTelefono($scope.recordData.telefono)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO"));
				$scope.formCrearCliente.$valid = undefined;
				return false;
			}
	    	return true;
	    }
		// Valida formato email
		$scope.validaFormatoEmail = function(){
	    	if ($scope.recordData.email !== undefined && $scope.recordData.email !== '' && region.ValidaFormatoEmail($scope.recordData.email)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
				$scope.formCrearCliente.$valid = undefined;
				return false;
			}
	    	return true;
	    }
		// Valida fecha nacimiento
		$scope.validaFechaNacimiento = function() {
			if ($scope.recordData.fechaNacimiento !== undefined 
					&& $scope.recordData.fechaNacimiento !== '' 
						&& region.validarFechaNacimiento($scope.recordData.fechaNacimiento, new Date($scope.$root.SesionFechaSistema) , 100)==false){
				bcsComunes.msg("warning","warning",$translate.instant("UI_FECHA_IMPROBABLE"));
				return false;
			}
			return true;
		};
		
		$scope.formConfig.oConfiguracionRegimenConyugal={filtros : {}};
		$scope.$watch('recordData.idEstadoCivil',function (nval,oval) {
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
		$scope.$watch('recordData.tipoPersona',function(newval,oldval) {
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
			}
		});
		
		$scope.crearConyuge = function() {
			var parametros = {
				modoRapido:true, 
				documentoIdentidadCliente:$scope.searchData.NumeroDocumentoIdentidad,
				tipoPersonaCrear:'N',
				esVentanaConyugue: true
			};
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var id1 = bcsComunes.ventanaAbrir("CrearPersona",parametros,false,false); 
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				if (data.creado === true){
					$scope.recordData.IdSocNegPersonaConyuge = data.IdSocioNegocio;
					setTimeout(function(){
						$scope.recordData.IdSocNegPersonaConyuge = data.IdSocioNegocio;
					},1000);
				}
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
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
					console.log(validacion);
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
		                   'workingNet.TipoTelefono'];
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
			for(vm in wknDef){console.log('workingNet:'+wknDef[vm],$scope.workingNet[wknDef[vm]]);//verificar si no se han inicializado todos.
				if($scope.workingNet[wknDef[vm]]===undefined)return;}
			if($scope.etapa=='inicio'){
				for(vm in wknDef)if($scope.workingNet[wknDef[vm]]===0)return;
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				if(parametros.modoEditar) {
					$scope.searchData.NumeroDocumentoIdentidad = $scope.documentoIdentidadCliente;
					$scope.placeholder.documentoIdentidad  = $scope.documentoIdentidadCliente;
					$scope.configData.buscarPersona();
				}
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