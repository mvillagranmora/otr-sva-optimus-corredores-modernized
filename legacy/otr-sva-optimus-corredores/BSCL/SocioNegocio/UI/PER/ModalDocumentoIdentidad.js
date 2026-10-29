/*************************************************************
 * Purpose	: Modal Documento Identidad
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalDocumentoIdentidadCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		var mdi = this;
	    mdi.documentoValidado = false;
		mdi.placeholder = {};
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		mdi.IdPersona = parametros.IdPersona;
		mdi.tipoPersona = parametros.CodigoTipoPersona;
		mdi.DocumentoIdentidad = $.extend({},parametros.DocumentoIdentidad);
		mdi.CodigoFormulario = parametros.CodigoFormulario;
		mdi.camposRequeridos = {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mdi.CodigoFormulario, function(respuesta) {
			mdi.camposRequeridos = respuesta;
			mdi.asignarValoresPorDefecto();
			mdi.cargarDatos();
		});

		mdi.bloqueoDocumentoIdentidad = (!!parametros.DocumentoIdentidad===false || parametros.DocumentoIdentidad.IdTipoDocumentoIdentidad==="RUT") ? false : true;
		mdi.bloqueoTipoDocumentoIdentidad = (!!parametros.DocumentoIdentidad);
		mdi.forzarTipoDocumento = (parametros.DocumentoIdentidad != null)? "S" : "N";
		mdi.formConfig={};
		mdi.formConfig.largoMaximoNumero=0;

		mdi.oConfiguracionDocumentoIdentidad = {
			filtros : {
				IdPais : { "field"   : "IdPais","operator": "eq", "value"   : $scope.$root.SesionIdPaisSistema}
			}
		}
		
		mdi.cargarDatos = function() {
			if (parametros.DocumentoIdentidad){
				mdi.DocumentoIdentidadOriginal = $.extend({},parametros.DocumentoIdentidad);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DOCUMENTO_IDENTIDAD'));
			}else{
				mdi.DocumentoIdentidadOriginal = $.extend({},mdi.DocumentoIdentidad);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DOCUMENTO_IDENTIDAD'));
				mdi.DocumentoIdentidad.FechaEmision = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
				mdi.DocumentoIdentidadOriginal.IdEstado = 'ACT'; 
				mdi.DocumentoIdentidadOriginal.IdTipoDocumentoIdentidad = 'R';
			}
		}

		mdi.aceptar = function() {
			mdi.convertirFechas();
			//modificacion
			if (mdi.DocumentoIdentidad.IdDocumentoIdentidad){
				
				var campos = [
							{ nombre: "IdTipoDocumentoIdentidad" ,tipo : "combo"},
							{ nombre: "DocumentoIdentidad" ,tipo : "texto"},
							{ nombre: "NumeroSerie" ,tipo : "texto"},
							{ nombre: "FechaEmision" ,tipo : "fecha"},
							{ nombre: "FechaVencimiento" ,tipo : "fecha"},
							{ nombre: "IdEstado" ,tipo : "combo"}];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mdi.DocumentoIdentidad[value.nombre] !== mdi.DocumentoIdentidadOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdi.DocumentoIdentidad[value.nombre]?mdi.DocumentoIdentidad[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mdi.DocumentoIdentidad[value.nombre] === false && mdi.DocumentoIdentidadOriginal[value.nombre] === null) && (mdi.DocumentoIdentidad[value.nombre] != mdi.DocumentoIdentidadOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdi.DocumentoIdentidad[value.nombre]?mdi.DocumentoIdentidad[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mdi.DocumentoIdentidad[value.nombre] !== mdi.DocumentoIdentidadOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdi.DocumentoIdentidad[value.nombre]?util.dateSPdate(mdi.DocumentoIdentidad[value.nombre]) :"0001-01-01")});
					}
					if (value.tipo === 'bool') {
						if (mdi.DocumentoIdentidad[value.nombre] !== mdi.DocumentoIdentidadOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdi.DocumentoIdentidad[value.nombre]?"S":"N")});
					}			
				});
				
				if (registroCambio.length === 0)
				{
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdPersona :mdi.IdPersona,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdDocumentoIdentidad: mdi.DocumentoIdentidad.IdDocumentoIdentidad,
					p_RegistroCambio: registroCambio
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaDocumentoIdentidad(datos).$promise.then(
					function(result) {
						var titulo = "";
						if(result.P_OUT_ERRORESDETALLE.length>0){
							titulo = "UI_ERROR";
						}else{
							titulo = "UI_DOCUMENTO_IDENTIDAD_MODIFICADO";
						}						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(){
						bcsComunes.msg("error","error","Error al modificar documento de identidad");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
					
			}
			//creacion
			else {
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdPersona :mdi.IdPersona,
					p_IdTipoDocumentoIdentidad :mdi.DocumentoIdentidad.IdTipoDocumentoIdentidadAsociado,
					p_DocumentoIdentidad :mdi.DocumentoIdentidad.DocumentoIdentidadAsociado,
					p_NumeroSerie :mdi.DocumentoIdentidad.NumeroSerie,
					p_FechaEmision :mdi.DocumentoIdentidad.FechaEmision || '0001-01-01',
					p_FechaVencimiento : (!!mdi.DocumentoIdentidad.FechaVencimiento) ? mdi.DocumentoIdentidad.FechaVencimiento : '0001-01-01'
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaDocumentoIdentidad(datos).$promise.then(
					function(result) {
						var titulo = "";
						if(result.P_OUT_ERRORESDETALLE.length>0){
							titulo = "UI_ERROR";
						}else{
							titulo = "UI_DOCUMENTO_IDENTIDAD_AGREGADO";
						}
		
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : titulo,
							conflictos : util.parseServerResponse(result)}, idVentana);

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});						
					},
					function(){
						bcsComunes.msg("error","error","Error al agregar documento de identidad");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		 mdi.asignarValoresPorDefecto = function (){
			$.each(mdi.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mdi.DocumentoIdentidad[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mdi.DocumentoIdentidad[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
            
		mdi.convertirFechas = function() {
		
			if(mdi.DocumentoIdentidad.FechaVencimiento){
				mdi.DocumentoIdentidad.FechaVencimiento = util.date(mdi.DocumentoIdentidad.FechaVencimiento).toDate();
			}
			if(mdi.DocumentoIdentidad.FechaEmision) {
				mdi.FechaEmision = util.date(mdi.DocumentoIdentidad.FechaEmision).toDate();
			}
			
		}
		
		mdi.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		mdi.cargarFirma = function(){
			var IdPersona = parametros.IdPersona;
			var IdDocumentoIdentidad= parametros.DocumentoIdentidad.IdDocumentoIdentidad
			var data = {operacion: "CREATE", IdPersona,IdDocumentoIdentidad};
			bcsComunes.ventanaOverlay(idVentana,true);
			var idModal = bcsComunes.ventanaAbrir("OperarFirmaModal", data, true, false);
			var promesa = bcsComunes.ventanaPromesa(idModal);
			promesa.then(function(data){ 
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};

		// Se obtienen placeholder
		$scope.$watch('mdi.DocumentoIdentidad.IdTipoDocumentoIdentidadAsociado',function(tipo){
			if(!tipo)return;
			if(tipo==='R') 
				mdi.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
			else if(tipo==='P')
				mdi.placeholder.documentoIdentidad = '';
			else if(tipo=='RUT')
				mdi.formConfig.largoMaximoNumero=10;
			else{
				mdi.placeholder.documentoIdentidad = '';
				mdi.formConfig.largoMaximoNumero=0;
			}
			mdi.blurDocumentoIdentidad(tipo);
		});
		
		mdi.blurDocumentoIdentidad = function(tipo){
			if(!tipo)return;
			var d=mdi.DocumentoIdentidad.DocumentoIdentidadAsociado||"";
			if(d.length==0)return;
			mdi.documentoValidado = false;

			if(tipo=='R'){
				d=mdi.DocumentoIdentidad.DocumentoIdentidad;
				var esRutValido = bcsComunes.VerificaRut(d);
				
				if(!esRutValido){
					bcsComunes.msg("error",'Error','UI_RUT_INVALIDO');
					mdi.DocumentoIdentidad.DocumentoIdentidad="";	
					d="";
					return;
				}
			}
			
			if(tipo=='IUP'){
				if(d.length<14){
					bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				}
				if(d.length>14){
					bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
				}else{
					var validacion=util.validarIUP(d);
					if(!validacion.escorrecta){
						bcsComunes.msg("warning","warning",$translate.instant(validacion.mensaje));
					}
				}
			}
			
			if(tipo=='DNI'){
				if(d.length<8){
					bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				}
				if(d.length>8){
					bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
				}
			}
			
			if(tipo=='RUC'){
				if(d.length<11){
					bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				}
				if(d.length>11){
					bcsComunes.msg("warning","warning",$translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
					d="";
				}
			}
				
			if(tipo=='PAS'){
				if(d.length>11){
					bcsComunes.msg("warning","warning",$translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
				}
			}
					
			if(((tipo != 'PAS') && (tipo != 'RUC') && (tipo != 'DNI') && (tipo != 'IUP') && (tipo !='R') && (tipo != 'P') && (tipo != 'X') && (tipo != 'TAX'))){
				bcsComunes.msg("error",'Error',$translate.instant('UI_TIPO_INVALIDO'));//agregar msj al diccionario
				return;
			}
							
			mdi.DocumentoIdentidad.DocumentoIdentidad=d;
			mdi.documentoValidado = true;
		};

	});
}());
//------------------------- FIN CONTROLLER --------------------------