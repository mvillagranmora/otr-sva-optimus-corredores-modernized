/*************************************************************
 * Purpose	: Crear Contraparte
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloOrdenes').controller('CrearContraparteCtrl', 
	function($scope,bcsComunes,$translate,$resource,bcsConfiguracionSociodeNegocio,bcsAlerta,OPCIONES_BCSALERTA ){
		$scope.configData = {};
		$scope.searchData = {};
		$scope.recordData = {};
		$scope.workingNet = {};
		$scope.ventanaId = bcsComunes.ventanaWinId($scope); // bcsComunes.ventanaOverlay($scope.ventanaId,true);
		bcsComunes.ventanaAutoHeight($scope.ventanaId);
		$scope.kOptFechaNacimiento = {};
		$scope.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		//$scope.configData.TipoDocumentoIdentidadBloqueado = false;
		$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
		$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoContraparte = true;	//BLOQUEA ZONA PERSONA

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
		$scope.formConfig = {};
		$scope.formConfig.largoMaximoNumeroAdicional=0;
		$scope.fnLoadFormConfig = function(SesionLocale){
			$scope.formConfig.tipoPersona = 'J';
			if(SesionLocale == 'PE'){ //peru
				$scope.formConfig.showNumeroAdicional = true;
				$scope.formConfig.requiereNumeroAdicional = false;
				$scope.formConfig.labelNumeroIdentidad = 'UI_NUMERO_IDENTIDAD';
				$scope.formConfig.tipoDocIdentidadDef = 'IUP';
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
			J : { field : "IdTipoPersona" , operator : "eq" , value : "J"},
		};
		
		$scope.cancelar = function() {
			$scope.limpiarPantalla();
			$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
			$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
			$scope.configData.bloqueoContraparte = true;	//BLOQUEA ZONA PERSONA
		};
		
		$scope.limpiarPantalla = function() {
			$scope.recordData = {
				tipoPersona : 'J',
			};
		};
		
		$scope.configData.buscarContraparte = function() {
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
				p_IdTipoEntidad: 'CONTRAPARTE',
				p_IdTipoDocumentoIdentidad: $scope.searchData.IdTipoDocumentoIdentidad,
				p_DocumentoIdentidad: $scope.searchData.NumeroDocumentoIdentidad,
				p_DocumentoIdentidadAdicional: $scope.searchData.NumeroDocumentoIdentidadAdicional,
				p_IdTipoDocumentoIdentidadAdicional: $scope.searchData.IdTipoDocumentoIdentidadAdicional,
				p_IdTipoPersona : $scope.recordData.tipoPersona,
				p_esRutInterno : 'N'
			};
			
			var post = r.ConsultarEntidadEnrolamiento(datosConsulta);
			
			post.$promise.then(function(result){
				
				
				//ERROR
				if (result.P_OUT_PERSONAEXISTE === 'S' && result.P_OUT_ENTIDADEXISTE === 'S') {
					bcsComunes.msg("error",'Error',$translate.instant('CONTRAPARTE_EXISTE'));
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
							idPaisResidenciaDefecto: dataIN.IdPaisResidencia,
							idPaisResidencia: dataIN.IdPaisResidencia,
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
						    CNT_CodigoInscripcion:dataIN.CNT_CodigoInscripcion,
							Emisor:dataIN.CNT_Emisor,
							Depositos:dataIN.CNT_Depositos,
							Bolsa:dataIN.CNT_Bolsa,
							Boveda:dataIN.CNT_Boveda,
							Corredora:dataIN.CNT_Corredora,
							CamaraDeCompensacion:dataIN.CNT_CamaraDeCompensacion,
							CLI_IdEmpleadoEjecutivo:0,
							Otros : dataIN.CNT_Otros,
							Banco:dataIN.CNT_Banco,
						    IdSocNegPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal
						};
					}
					else
					{
						bcsComunes.msg('error','Error',$translate.instant('UI_ERROR_CARGAR_PERSONA'));
						console.log(JSON.stringify(result));
					}
					$scope.asignarcheckbox();
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = true;
					$scope.configData.bloqueoContraparte = false;
					
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
							CodigoEmpleado:dataIN.EMP_CodigoEmpleado,
						    CodigoTipoCargoEmpleado:dataIN.EMP_CodigoTipoCargoEmpleado,
						    IdEmpleadoSupervisor:dataIN.EMP_IdEmpleadoSupervisor,
						    IdSucursal:dataIN.EMP_IdSucursal,
						    FechaInicioCargo:dataIN.EMP_FechaInicioCargo,
						    FechaTerminoCargo:dataIN.EMP_FechaTerminoCargo,
						    CodigoInscripcion:dataIN.CNT_CodigoInscripcion,
							Emisor:dataIN.CNT_Emisor,
							Depositos:dataIN.CNT_Depositos,
							Bolsa:dataIN.CNT_Bolsa,
							Boveda:dataIN.CNT_Boveda,
							Corredora:dataIN.CNT_Corredora,
							CamaraDeCompensacion:dataIN.CNT_CamaraDeCompensacion,
							CLI_IdEmpleadoEjecutivo:0,
							Banco:dataIN.CNT_Banco,
							Otros : dataIN.CNT_Otros,
						    IdSocNegPersonaConyuge : dataIN.IdPersonaConyuge,
							RegimenConyugal: dataIN.RegimenConyugal	
						    
						};
					}
					else
					{
						
					}
					//BLOQUEAR CONTROLES DE ARRIBA
					$scope.asignarcheckbox();
					$scope.configData.bloqueoBuscador = true;
					$scope.configData.bloqueoPersona = false;
					$scope.configData.bloqueoContraparte = false;
					$scope.configData.TipoDocumentoIdentidadBloqueado = true;
				}else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_ERROR_CARGAR_CONTRAPARTE'),
						conflictos:util.parseServerResponse(result)},$scope.ventanaId);
				}
				//en caso de ser peru, y utilizar tipo documento ficticio.
				if($scope.SesionLocale=='PE' && $scope.searchData.IdTipoDocumentoIdentidadAsociado==="FIC"){
					$scope.recordData.documentoIdentidadNuevo = result.P_OUT_DOCUMENTOIDENTIDADFICTICIO;
				}
				
				//Datos por defecto en pantalla!
				if($scope.recordData)  {
					$scope.recordData.idPaisResidenciaDefecto = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.recordData.idPaisResidenciaDefecto || $scope.$root.SesionIdPaisSistema;
				} else {
					$scope.recordData = {};
					$scope.recordData.idPaisResidenciaDefecto = $scope.$root.SesionIdPaisSistema;
					$scope.recordData.idPaisResidencia = $scope.$root.SesionIdPaisSistema;
				}
				
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
				
				//en caso de no ingresar documento adicional+
				if($scope.SesionLocale=='PE' && result.P_OUT_PERSONAEXISTE ==="S" && $scope.searchData.NumeroDocumentoIdentidadAdicional !== result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional){
					if($scope.searchData.NumeroDocumentoIdentidadAdicional!==""){
						bcsComunes.ventanaMsgErr($translate.instant('UI_DOCUMENTO_ADICIONAL_INCORRECTO'),$scope.ventanaId);
						$scope.cancelar();
					}else{
						$scope.recordData.NumeroDocumentoIdentidadAdicional = result.P_OUT_CONSULTARENROLAMIENTO[0].DocumentoIdentidadAdicional;
					}
				}
				
			},function(result){
				bcsComunes.msg('error','Error',$translate.instant('UI_ERROR_GUARDAR_BORRADOR'));
				console.log(JSON.stringify(result));
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			});		
		};
		
		$scope.$watch('recordData.tipoPersona',function(newval,oldval) {
			if (newval === 'N')
			{
				limpiarCamposPersonaJuridica($scope.recordData);
			}
			else if (newval === 'J')
			{
				limpiarCamposPersonaNatural($scope.recordData);
			}
		});
		
		$scope.asignarcheckbox = function()
		{
			if ($scope.recordData.Corredora==='S') {$scope.recordData.Corredora=true;}else{$scope.recordData.Corredora=false;};
			if ($scope.recordData.Emisor==='S') {$scope.recordData.Emisor=true;}else{$scope.recordData.Emisor=false;};
			if ($scope.recordData.Depositos==='S') {$scope.recordData.Depositos=true;}else{$scope.recordData.Depositos=false;};
			if ($scope.recordData.Bolsa==='S') {$scope.recordData.Bolsa=true;}else{$scope.recordData.Bolsa=false;};
			if ($scope.recordData.CamaraDeCompensacion==='S') {$scope.recordData.CamaraDeCompensacion=true;}else{$scope.recordData.CamaraDeCompensacion=false;};
			if ($scope.recordData.Banco==='S') {$scope.recordData.Banco=true;}else{$scope.recordData.Banco=false;};
			if ($scope.recordData.Boveda==='S') {$scope.recordData.Boveda=true;}else{$scope.recordData.Boveda=false;};
		};		
		
		//borra campos de persona juridica
		var limpiarCamposPersonaJuridica = function(registro) {
			if (!registro)
				return;
			registro.razonSocial = '';
		};
		
		//borra campos de persona natural
		var limpiarCamposPersonaNatural = function(registro) {
			if (!registro)
				return;
			registro.nombres = '';
			registro.apellidoPaterno = '';
			registro.apellidoMaterno = '';
		};
		
		$scope.guardarBorrador = function(){
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearBorrador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=52";
			var r = $resource("", {},
		    {
				'CrearBorrador': { method: "POST", url: urlCrearBorrador}
			});
			
			if ($scope.recordData.tipoPersona === 'N' || $scope.recordData.tipoPersona === 'M')
			{
				limpiarCamposPersonaJuridica($scope.recordData);
			}
			else if ($scope.recordData.tipoPersona)
			{
				limpiarCamposPersonaNatural($scope.recordData);
			}
			
			var datosBorrador = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'CONTRAPARTE',
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
	   				FechaNacimiento: $scope.recordData.fechaNacimiento || '',
	   				Sexo: $scope.recordData.idGenero || '',
	   				EstadoCivil: $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: (($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad) || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0,
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento || '',
	   				FechaCreacion: new Date(),
	   				FechaModificacion: new Date(),
	   				IdPersonaConyuge: ($scope.recordData.RelacionadoObj&&$scope.recordData.RelacionadoObj.IdPersona>0)?$scope.recordData.RelacionadoObj.IdPersona:0,
	   	   			RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista:'',
	   				CLI_IdTipoCliente:'',
	   				CLI_IdClaseCliente:'',
	   				CLI_IdEmpleadoEjecutivo:0,
	   				CLI_CodigoAdicionalHolding:'',
	   				POR_IdPerfilComercial: '',
	   				EMP_CodigoEmpleado:'',
				    EMP_CodigoTipoCargoEmpleado:'',
				    EMP_IdEmpleadoSupervisor:0,
				    EMP_IdSucursal:0,
				    EMP_FechaInicioCargo:'0001-01-01',
				    EMP_FechaTerminoCargo:'0001-01-01',
				    CNT_CodigoInscripcion:$scope.recordData.CodigoInscripcion || '',
					CNT_Emisor:($scope.recordData.Emisor)? 'S': 'N',
					CNT_Depositos:($scope.recordData.Depositos)? 'S': 'N',
					CNT_Bolsa:($scope.recordData.Bolsa)? 'S': 'N',
					CNT_Corredora:($scope.recordData.Corredora)? 'S': 'N',
					CNT_CamaraDeCompensacion:($scope.recordData.CamaraDeCompensacion)? 'S': 'N',
					CNT_Banco:($scope.recordData.Banco)? 'S': 'N',	
					CNT_Boveda:($scope.recordData.Boveda)? 'S': 'N',	
					CNT_Otros: $scope.recordData.Otros || "",	
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

				var promesa = bcsComunes.ventanaMsg({ 
					mensaje : (result.P_OUT_REFID && result.P_OUT_REFID > 0) ? $translate.instant('UI_BORRADOR_GUARDADO') + ': ' + result.P_OUT_REFID : $translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
					conflictos : util.parseServerResponse(result)}, 
					$scope.ventanaId);
				
				bcsComunes.ventanaPromesa(promesa).then(function(){
					bcsComunes.ventanaOverlay($scope.ventanaId,false);	
				});
				
			},function(result){
				var promesa = bcsComunes.ventanaMsg({ 
					mensaje : $translate.instant('UI_ERROR_GUARDAR_BORRADOR'),
					conflictos : util.parseServerResponse(result)}, 
					$scope.ventanaId);
				console.log(JSON.stringify(result));
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			});
		};
		
		$scope.crearContraparte = function(){
			if(!$scope.validaFormatoTelefono()) return;
			if(!$scope.validaFormatoEmail()) return;
			
			bcsComunes.ventanaOverlay($scope.ventanaId,true);
			var urlCrearContraparte = "/BSCL/Plataforma/GWJAVA/PLA/call?t=104";
			var r = $resource("", {},
		    {
				'CrearContraparte': { method: "POST", url: urlCrearContraparte}
			});
			
			if ($scope.recordData.tipoPersona === 'N' || $scope.recordData.tipoPersona === 'M')
			{
				limpiarCamposPersonaJuridica($scope.recordData);
			}
			else if ($scope.recordData.tipoPersona)
			{
				limpiarCamposPersonaNatural($scope.recordData);
			}
			
			var datosCrearContraparte = {
				p_ConsultarEnrolamiento : [{
					IdEnrolamiento : 0,
	   				IdEmpresa: $scope.SesionIdEmpresa,
	   				IdUsuario: $scope.SesionIdUsuario,
	  				IdTipoEntidad: 'CONTRAPARTE',
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
	   				FechaNacimiento: $scope.recordData.fechaNacimiento || '0001-01-01',// || new Date(),
	   				Sexo: $scope.recordData.idGenero || 'M',
	   				EstadoCivil: $scope.recordData.idEstadoCivil || '',
	   				IdCiudad: (($scope.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?$scope.recordData.idCiudad:$scope.divisionPaisSeleccionado.IdCiudad) || 0,
	   				IdDivisionPais: $scope.recordData.idDivisionPais || 0,
	   				IdTipoTelefono : $scope.recordData.idTipoTelefono || 'OTRO',
	   				Telefono: $scope.recordData.telefono || '',
	   				Email: $scope.recordData.email || '',
	   				DireccionCalle: $scope.recordData.direccionCalle || '',
	   				DireccionNumero: $scope.recordData.direccionNumero || '',
	   				DireccionDepartamento: $scope.recordData.direccionDepartamento || '',
	   				FechaCreacion: new Date(),
	   				FechaModificacion: new Date(),
	   				IdPersonaConyuge: ($scope.recordData.RelacionadoObj&&$scope.recordData.RelacionadoObj.IdPersona>0)?$scope.recordData.RelacionadoObj.IdPersona:0,
	   	   			RegimenConyugal: $scope.recordData.RegimenConyugal || '',
	   				CLI_IdCategoriaInversionista:'',
	   				CLI_IdTipoCliente:'',
	   				CLI_IdClaseCliente:'',
	   				CLI_IdEmpleadoEjecutivo:0,
	   				CLI_CodigoAdicionalHolding:'',
	   				POR_IdPerfilComercial: '',
	   				EMP_CodigoEmpleado:'',
				    EMP_CodigoTipoCargoEmpleado:'',
				    EMP_IdEmpleadoSupervisor:0,
				    EMP_IdSucursal:0,
				    EMP_FechaInicioCargo:'0001-01-01',
				    EMP_FechaTerminoCargo:'0001-01-01',
				    CNT_CodigoInscripcion:$scope.recordData.CodigoInscripcion || '',
					CNT_Emisor:($scope.recordData.Emisor)? 'S': 'N',
					CNT_Depositos:($scope.recordData.Depositos)? 'S': 'N',
					CNT_Bolsa:($scope.recordData.Bolsa)? 'S': 'N',
					CNT_Corredora:($scope.recordData.Corredora)? 'S': 'N',
					CNT_CamaraDeCompensacion:($scope.recordData.CamaraDeCompensacion)? 'S': 'N',
					CNT_Banco:($scope.recordData.Banco)? 'S': 'N',	
					CNT_Boveda:($scope.recordData.Boveda)? 'S': 'N',
					CNT_Otros: $scope.recordData.Otros || "",		
	   				PRO_IdAreaProveedor:0,
	   				EMP_CargoIndefinido : '',
	   				EsRutInterno : 'N'
				}],
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
			};
			
			var post = r.CrearContraparte(datosCrearContraparte);
			post.$promise.then(function(result){
				$scope.cancelar();				
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_CONTRAPARTE_CREADO') + ': ' + result.P_OUT_REFIDCONTRAPARTE,
					conflictos : util.parseServerResponse(result)}, $scope.ventanaId);
				
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			},function(result){
				bcsComunes.msg('error','Error',$translate.instant('UI_ERROR_CREAR_CONTRAPARTE'));
				console.log(JSON.stringify(result));
				bcsComunes.ventanaOverlay($scope.ventanaId,false);
			});
		};
		
		
		// Se obtienen placeholder
		$scope.placeholder = {};
		if($scope.SesionLocale=='CL') $scope.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		
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
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_NUMERO_TELEFONO_INCORRECTO"));
				$scope.formCrearContraparte.$valid = undefined;
				return false;
			}
	    	return true;
	    }
		
		// Valida formato email
		$scope.validaFormatoEmail = function(){
	    	if ($scope.recordData.email !== undefined && $scope.recordData.email !== '' && region.ValidaFormatoEmail($scope.recordData.email)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
				$scope.formCrearContraparte.$valid = undefined;
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
		$scope.crearConyuge = function() {
			var parametros = {
				modoRapido:true, 
				documentoIdentidadCliente:$scope.searchData.NumeroDocumentoIdentidad,
				tipoPersonaCrear:'N',
			};
			var id1 = bcsComunes.ventanaAbrir("CrearPersona",parametros,false,false); 
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				if (data.creado === true){
					$scope.recordData.IdSocNegPersonaConyuge = data.IdSocioNegocio;
				}
			}); 
		};
		$scope.blurDocumentoIdentidad = function(tipo){
			if(!tipo)return;
			var d=$scope.searchData.NumeroDocumentoIdentidadAdicional||"";
			if(d.length==0)return;
			if(tipo=='RUT'){
				if(d.length>8){
					bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
				}else d=util.PadLeft($scope.searchData.NumeroDocumentoIdentidadAdicional,'0',8);
				$scope.searchData.NumeroDocumentoIdentidadAdicional=d;
			}
		};
		$scope.$watch('searchData.IdTipoDocumentoIdentidadAdicional',function(tipo){
			if(tipo=='RUT')$scope.formConfig.largoMaximoNumeroAdicional=10;
			else $scope.formConfig.largoMaximoNumeroAdicional=0;
			//$scope.blurNumeroAdicional(tipo);
		});


		/**************************
		 * 	WORKING NET COMBOBOX
		 **************************/ 
		//los procesados no deben tener caché. No aplicar a combobox-socio-negocio
		var wknDefinition=['workingNet.PaisResidencia',
		                   //'workingNet.CiudadResidencia',
		                   'workingNet.DivisionPaisResidencia'
		                   ];
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

	});
}());
//------------------------- FIN CONTROLLER -----------------------------
