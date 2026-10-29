/*************************************************************
 * Purpose	: Crear Empleado
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloOrdenes').controller('CrearEmpleadoCtrl', 
		function($scope,bcsComunes,$translate,$resource,bcsConfiguracionSociodeNegocio,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA ){
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
		$scope.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		//$scope.configData.TipoDocumentoIdentidadBloqueado = false;
		$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
		$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoEmpleado = true;	//BLOQUEA ZONA PERSONA
		$scope.maximoCodigoEmpleado = 0;

		bcsConfiguracionSociodeNegocio.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionSociodeNegocio.CONFIGURACION['CLI.BloqueoDocIdentidad'] === 'S')
				$scope.configData.TipoDocumentoIdentidadBloqueado = true;
			else
				$scope.configData.TipoDocumentoIdentidadBloqueado = false;
		});
		$scope.ValidaSalidaESC = function(){
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_ENROLAMIENTO_INCONCLUSO'));
			t.result.then(function(result){
					if (result === OPCIONES_BCSALERTA.NO) {
					bcsComunes.ventanaCerrar($scope.ventanaId);
				}
			});	
		};

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('EMP.MaximoCodigoEmpleado', $scope.SesionIdEmpresa, 'SOCIONEGOCIO')
		.then(function (parametroMaximoCodigoEmpleado) {
			$scope.maximoCodigoEmpleado = parametroMaximoCodigoEmpleado.Valor ? Number(parametroMaximoCodigoEmpleado.Valor) : 40;
		});
		
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
			}
			else{ //otro
				$scope.formConfig.showNumeroAdicional = false;
				$scope.formConfig.requiereNumeroAdicional = false;
				$scope.formConfig.labelNumeroIdentidad = 'UI_DOCUMENTO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'R';
				$scope.formConfig.tipoDocIdentidadAdicionalDef = 'R'
			}
			$scope.$apply;
		}
		$scope.fnLoadFormConfig($scope.SesionLocale);
		
		$scope.configData.filtroTipoDocumentoIdentidad = {
			PorDefecto : {field: "PorDefecto", operator: "eq", value: "S"},
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
		
		$scope.cancelar = function() {
			$scope.limpiarPantalla();
			$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
			$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.bloqueoEmpleado = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.TipoDocumentoIdentidadBloqueado = ($scope.SesionLocale==="PE")?false:true;
		};
		
		$scope.limpiarPantalla = function() {
			$scope.recordData = {
				tipoPersona : 'N',
				idCategoriaInversionista : '',
				idTipoCliente : '',
				IdEmpleadoEjecutivo : 0,
				idPaisResidencia : $scope.$root.SesionIdPaisSistema,
				CodigoTipoCargoEmpleado : '',
				IdSucursal : '',
				IdEmpleadoSupervisor : '',
			};
		};
		
		$scope.configData.buscarEmpleado = function() {
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
			
			if(!$scope.searchData.NumeroDocumentoIdentidad || $scope.searchData.NumeroDocumentoIdentidad.length==0){
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
				p_IdModulo: 'SOC',
				p_UsaReglasNegocio: 'S',
				p_IdTipoEntidad: 'EMPLEADO',
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
					bcsComunes.ventanaMsgErr($translate.instant('UI_EMPLEADO_EXISTE'),$scope.ventanaId);
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
							idClaseCliente : dataIN.CLI_IdClaseCliente,
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
							CodigoEmpleado:dataIN.EMP_CodigoEmpleado,
						    CodigoTipoCargoEmpleado:dataIN.EMP_CodigoTipoCargoEmpleado,
						    IdEmpleadoSupervisor:dataIN.EMP_IdEmpleadoSupervisor,
						    IdSucursal:dataIN.EMP_IdSucursal,
						    FechaInicioCargo:dataIN.EMP_FechaInicioCargo,
						    FechaTerminoCargo:dataIN.EMP_FechaTerminoCargo,
						    idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal
						};
					}else{
						bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_EMPLEADO'),$scope.ventanaId);
						console.log(JSON.stringify(result));
					}
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = true;
					$scope.configData.bloqueoEmpleado = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
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
							idCiudad : Number(dataIN.IdCiudad),
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
							CodigoEmpleado:dataIN.EMP_CodigoEmpleado,
						    CodigoTipoCargoEmpleado:dataIN.EMP_CodigoTipoCargoEmpleado,
						    IdEmpleadoSupervisor:dataIN.EMP_IdEmpleadoSupervisor,
						    IdSucursal:dataIN.EMP_IdSucursal,
						    FechaInicioCargo:dataIN.EMP_FechaInicioCargo,
						    FechaTerminoCargo:dataIN.EMP_FechaTerminoCargo,
						    idPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal,
							RegimenConyugalOriginal: dataIN.RegimenConyugal
						};
					}else{
					}
					//BLOQUEAR CONTROLES DE ARRIBA
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = false;
					$scope.configData.bloqueoEmpleado = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
				}
				//obtener IdSocioDeNegocio para conyuge en caso de existir:
				if(($scope.recordData.idEstadoCivil==='CA'||$scope.recordData.idEstadoCivil==='CC')&&$scope.recordData.idPersonaConyuge>0){
					var filter = { field: "IdPersona", operator: "eq", value: Number($scope.recordData.idPersonaConyuge) }
					var conyugeDatasource=bcsConfiguracionSociodeNegocio.getDatasourceSocioNegocio(filter);
					conyugeDatasource.query().then(function(result){
						if(conyugeDatasource.data().length>0){
							$scope.recordData.IdSocNegPersonaConyuge=conyugeDatasource.data()[0].IdSocioDeNegocio;
							$scope.recordData.IdSocNegPersonaConyugeOriginal=conyugeDatasource.data()[0].IdSocioDeNegocio;
						} 
					});
				}
				
				//Datos por defecto en pantalla!
				if($scope.recordData)  {
					$scope.recordData.idPaisResidenciaDefecto = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.FechaInicioCargo  = $scope.recordData.FechaInicioCargo  || $scope.$root.SesionFechaSistema;
					//$scope.recordData.FechaTerminoCargo = $scope.recordData.FechaTerminoCargo || $scope.$root.SesionFechaSistema ;
				} else {
					$scope.recordData = {};
					$scope.recordData.idPaisResidenciaDefecto = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.FechaInicioCargo = $scope.$root.SesionFechaSistema;
				}
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_CARGAR_BORRADOR'),$scope.ventanaId);
				console.log(JSON.stringify(result));
			});		
		};
		
		$scope.$watch('recordData.tipoPersona',function(newval,oldval) {
			if (newval === 'N'){
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

			if (($scope.recordData.CodigoEmpleado || '').length > $scope.maximoCodigoEmpleado) {
				bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant("UI_INGRESE_CODIGO_EMPLEADO_MAXIMO_PERMITIDO").replace("MaximoCodigoEmpleado", $scope.maximoCodigoEmpleado));
				return;
			}

			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearBorrador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=52";
			var r = $resource("", {},{'CrearBorrador': { method: "POST", url: urlCrearBorrador}});
			if ($scope.recordData.tipoPersona === 'N' || $scope.recordData.tipoPersona === 'M'){
				limpiarCamposPersonaJuridica($scope.recordData);
			}else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
			if($scope.recordData.CargoIndefinido){
				$scope.recordData.FechaTerminoCargo = "";
			}
			var IdCiudadCalculado = ($scope.modoRapido)?0:(($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad);
			var IdPersonaConyuge=($scope.seleccionado.RelacionadoObj&&$scope.seleccionado.RelacionadoObj.IdPersona>0)?$scope.seleccionado.RelacionadoObj.IdPersona:0;
			var datosBorrador = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'EMPLEADO',
	   				IdEntidadEnrolada: 0,
	   				IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAsociado: $scope.searchData.IdTipoDocumentoIdentidadAsociado || '',
	   				DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAdicional: '',
	   				DocumentoIdentidadAdicional: '',
	   				Tipo: $scope.recordData.tipoPersona,
	   				Nombres: $scope.recordData.nombres || '',
	   				ApellidoPaterno: $scope.recordData.apellidoPaterno || '',
	   				ApellidoMaterno: $scope.recordData.apellidoMaterno || '',
	   				RazonSocial: $scope.recordData.razonSocial || '',
	   				IdPaisNacimiento: $scope.recordData.idPaisNacimiento || 0,
	   				FechaNacimiento: $scope.recordData.fechaNacimiento ? util.dateSPdate($scope.recordData.fechaNacimiento) : '0001-01-01',
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
	   				POR_IdPerfilComercial: '',
	   				EMP_CodigoEmpleado:$scope.recordData.CodigoEmpleado || '',
				    EMP_CodigoTipoCargoEmpleado:$scope.recordData.CodigoTipoCargoEmpleado || '',
				    EMP_IdEmpleadoSupervisor:$scope.recordData.IdEmpleadoSupervisor || 0,
				    EMP_IdSucursal:$scope.recordData.IdSucursal || 0,
				    EMP_FechaInicioCargo: $scope.recordData.FechaInicioCargo ? util.date($scope.recordData.FechaInicioCargo).format('YYYY-MM-DD') : '',
				    EMP_FechaTerminoCargo:$scope.recordData.FechaTerminoCargo ? util.date($scope.recordData.FechaTerminoCargo).format('YYYY-MM-DD') : '',
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
	   				EMP_CargoIndefinido : ($scope.recordData.CargoIndefinido)?'S' : 'N',
	   				IdPaisResidencia: $scope.recordData.idPaisResidencia || 0
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
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_BORRADOR_GUARDADO') + ': ' + result.P_OUT_REFID,
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
		
		$scope.crearEmpleado = function(){
			$scope.etapa=='guardar';
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			
			if($scope.recordData.CodigoEmpleado.length > $scope.maximoCodigoEmpleado) {
				bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant("UI_INGRESE_CODIGO_EMPLEADO_MAXIMO_PERMITIDO").replace("MaximoCodigoEmpleado", $scope.maximoCodigoEmpleado));
				return;
			}
			
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearEmpleado = "/BSCL/Plataforma/GWJAVA/PLA/call?t=101";
			var r = $resource("", {},{'CrearEmpleado': { method: "POST", url: urlCrearEmpleado}});
			if ($scope.recordData.tipoPersona === 'N' || $scope.recordData.tipoPersona === 'M'){
				limpiarCamposPersonaJuridica($scope.recordData);
				var fecAct = new Date($scope.$root.SesionFechaSistema);
				var fecAct2 = new Date($scope.$root.SesionFechaSistema);
				agno = fecAct2.getFullYear();
				agno = agno-18;
				fecAct2.setFullYear(agno);
				if ($scope.recordData.fechaNacimiento > fecAct2){
		    		bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_PERSONA_MENOR_DE_EDAD"));
					$scope.formCrearEmpleado.$valid = undefined;
					bcsComunes.ventanaOverlay($scope.ventanaId,false);
					return;	
				}
				
			}else if ($scope.recordData.tipoPersona){
				limpiarCamposPersonaNatural($scope.recordData);
			}
			if($scope.recordData.CargoIndefinido){
				$scope.recordData.FechaTerminoCargo = "";
			}
			var IdCiudadCalculado = ($scope.modoRapido)?0:(($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad);
			var IdPersonaConyuge=($scope.seleccionado.RelacionadoObj&&$scope.seleccionado.RelacionadoObj.IdPersona>0)?$scope.seleccionado.RelacionadoObj.IdPersona:0;
			var datosCrearEmpleado = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'EMPLEADO',
	   				IdEntidadEnrolada: 0,
	   				IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAsociado: $scope.searchData.IdTipoDocumentoIdentidadAsociado || '',
	   				DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
	   				IdTipoDocumentoIdentidadAdicional: '',
	   				DocumentoIdentidadAdicional:  '',
	   				Tipo: $scope.recordData.tipoPersona,
	   				Nombres: $scope.recordData.nombres || '',
	   				ApellidoPaterno: $scope.recordData.apellidoPaterno || '',
	   				ApellidoMaterno: $scope.recordData.apellidoMaterno || '',
	   				RazonSocial: $scope.recordData.razonSocial || '',
	   				IdPaisNacimiento: $scope.recordData.idPaisNacimiento || 0,
	   				FechaNacimiento: $scope.recordData.fechaNacimiento || '',// || new Date(),
	   				Sexo: $scope.recordData.idGenero || 'M',
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
	   				POR_IdPerfilComercial: '',
	   				EMP_CodigoEmpleado:$scope.recordData.CodigoEmpleado || '',
				    EMP_CodigoTipoCargoEmpleado:$scope.recordData.CodigoTipoCargoEmpleado || '',
				    EMP_IdEmpleadoSupervisor:$scope.recordData.IdEmpleadoSupervisor || 0,
				    EMP_IdSucursal:$scope.recordData.IdSucursal || 0,
				    EMP_FechaInicioCargo:$scope.recordData.FechaInicioCargo || '',
				    //EMP_FechaTerminoCargo:($scope.recordData.FechaTerminoCargo instanceof Date && isFinite($scope.recordData.FechaTerminoCargo))? $scope.recordData.FechaTerminoCargo:'',
				    EMP_FechaTerminoCargo:(angular.isDate($scope.recordData.FechaTerminoCargo))? $scope.recordData.FechaTerminoCargo:'',
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
	   				EMP_CargoIndefinido : ($scope.recordData.CargoIndefinido)?'S' : 'N'
				}],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
			};
			
			console.log(JSON.stringify(datosCrearEmpleado));
			var post = r.CrearEmpleado(datosCrearEmpleado);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				if(result.P_OUT_ERRORESCONTADOR==0&&result.P_OUT_REFID&&result.P_OUT_REFID>0){
					$scope.cancelar();
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_EMPLEADO_CREADO') + ': ' + result.P_OUT_REFID,
						conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_EMPLEADO'),
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				}
				bcsComunes.setConflictos(util.parseServerResponse(result));
			},function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				$scope.etapa='';
				bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CREAR_EMPLEADO'),
					conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				bcsComunes.setConflictos(util.parseServerResponse(result));
				console.log(JSON.stringify(result));
			});
		};
		
		// Se obtienen placeholder
		$scope.placeholder = {};
		if($scope.SesionLocale=='CL')$scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		
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
	    	if ($scope.recordData.telefono !== undefined && $scope.recordData.telefono !== '' && $scope.recordData.telefono !== null && region.ValidaFormatoNumeroTelefono($scope.recordData.telefono)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO"));
				$scope.formCrearEmpleado.$valid = undefined;
				return false;
			}
	    	return true;
	    }
		// Valida formato email
		$scope.validaFormatoEmail = function(){
	    	if ($scope.recordData.email !== undefined && $scope.recordData.email !== '' && $scope.recordData.email !== null && region.ValidaFormatoEmail($scope.recordData.email)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
				$scope.formCrearEmpleado.$valid = undefined;
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
			//validar que el empleado sea mayor de edad
			if(!!$scope.recordData.fechaNacimiento && moment($scope.SesionFechaSistema,"YYYY-MM-DD").diff(moment($scope.recordData.fechaNacimiento), "years") >= parseInt($scope.SesionValoresDefectoSistema["EdadMinimaEmpleado"])){
				return true;
			}else{
				bcsComunes.msg("warning","warning",$translate.instant("UI_MENOR_DE_EDAD"));
				return false;
			}
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
				}else{filters = IdPais;}
				$scope.formConfig.oConfiguracionRegimenConyugal.filtros = filters;	
			}
		});
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
					console.log(validacion);
					if(!validacion.escorrecta){
						bcsComunes.msg("warning","warning",vm+':'+$translate.instant(validacion.mensaje));
						$scope['errorValidar'+vm]=true;
					}
				}
			}
			$scope.searchData[vm]=d;
		};

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
		                   'workingNet.PaisNacimiento',
		                   'workingNet.EstadoCivil',
		                   //'workingNet.Conyuge',
		                   'workingNet.RegimenConyugal',
		                   'workingNet.PaisResidencia',
		                   //'workingNet.CiudadResidencia',
		                   'workingNet.DivisionPaisResidencia',
		                   'workingNet.TipoTelefono',
		                   'workingNet.TipoCargoEmpleado',
		                   'workingNet.Sucursal',
		                   'workingNet.EmpleadoSupervisor'];
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