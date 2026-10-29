/*************************************************************
 * Purpose	: Modal Tarjeta Bancaria
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalTarjetaBancariaCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
		var mtb = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mtb.IdPersona = parametros.IdPersona;
		mtb.TarjetaBancaria = $.extend({},parametros.TarjetaBancaria);
		mtb.kOptFechaVencimiento = {};
		mtb.CodigoFormulario = parametros.CodigoFormulario;
		mtb.camposRequeridos = {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mtb.CodigoFormulario, function(respuesta) {
			mtb.camposRequeridos = respuesta;
			mtb.asignarValoresPorDefecto();
			mtb.cargarDatos();
		});

		mtb.cmbbxEstadoTarjetaBancaria = {
			filtros : {
				IdEstado : {field: "IdEstado", operator: "ne", value: 'PEN'}
			}
		};
		
		mtb.cargarDatos = function(){
			if (parametros.TarjetaBancaria){
				//modificacion
				mtb.TarjetaBancariaOriginal = $.extend({},parametros.TarjetaBancaria);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TARJETA_BANCARIA'));
			}else{
				mtb.TarjetaBancariaOriginal = $.extend({},mtb.TarjetaBancaria);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TARJETA_BANCARIA'));
				mtb.TarjetaBancariaOriginal.IdTipoTarjetaBancaria = 'CUCO';
				mtb.kOptFechaVencimiento.min = $scope.$root.SesionFechaSistema;
				mtb.TarjetaBancaria.FechaVencimiento = util.date($scope.$root.SesionFechaSistema).toDate();
				mtb.TarjetaBancariaOriginal.IdEstado = 'ACT';
			}
		}
		
		mtb.aceptar = function() {
			if(mtb.TarjetaBancaria.IdTipoTarjetaBancaria === 'TDEB')
			{
				mtb.TarjetaBancaria.CodigoVerificacion = 0;
				mtb.TarjetaBancaria.FechaVencimiento = '';
				if(mtb.TarjetaBancaria.NumeroTarjetaBancaria == undefined)
					mtb.TarjetaBancaria.NumeroTarjetaBancaria = '';
				else
					if(!mtb.validaFormatoNroTarjeta()) return;
			}
			else{
				if(!mtb.validaFormatoNroTarjeta()) return;
				if(!mtb.validaFormatoCodVerificacion()) return;
			}
			//modificacion
			if (mtb.TarjetaBancaria.IdTarjetaBancaria){
				var campos = [
							{ nombre: "IdBanco" ,tipo : "combo"},
							{ nombre: "IdTipoTarjetaBancaria" ,tipo : "combo"},
							{ nombre: "NumeroTarjetaBancaria" ,tipo : "texto"},
							{ nombre: "FechaVencimiento" ,tipo : "fecha"},						
							{ nombre: "CodigoVerificacion" ,tipo : "combo"},
							{ nombre: "IdEstado" ,tipo : "combo"}];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mtb.TarjetaBancaria[value.nombre] !== mtb.TarjetaBancariaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mtb.TarjetaBancaria[value.nombre]?mtb.TarjetaBancaria[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mtb.TarjetaBancaria[value.nombre] === false && mtb.TarjetaBancariaOriginal[value.nombre] === null) && (mtb.TarjetaBancaria[value.nombre] != mtb.TarjetaBancariaOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mtb.TarjetaBancaria[value.nombre]?mtb.TarjetaBancaria[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mtb.TarjetaBancaria[value.nombre] !== mtb.TarjetaBancariaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mtb.TarjetaBancaria[value.nombre]?util.dateSPdate(mtb.TarjetaBancaria[value.nombre]) :"")});
					}
					if (value.tipo === 'bool') {
						if (mtb.TarjetaBancaria[value.nombre] !== mtb.TarjetaBancariaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mtb.TarjetaBancaria[value.nombre]?"S":"N")});
					}			
				});
				
				if (registroCambio.length === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo 			:'SOC',
					p_IdPersona			: mtb.IdPersona,
					p_UsaReglasNegocio 	:'S',
					p_IdTarjetaBancaria	: mtb.TarjetaBancaria.IdTarjetaBancaria,
					p_RegistroCambio	: registroCambio
				};
				
				if(!mtb.validarCamposVacios()) return;
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaTarjetaBancaria(datos).$promise.then(
					function(result) {
						console.log(result);
						if (result.P_OUT_ADVERTENCIASCONTADOR){
							bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
						}
						else if (result.P_OUT_ERRORESCONTADOR){
							bcsComunes.msg("error","error","Error al agregar tarjeta bancaria");
						}
						else
							bcsComunes.msg("info","info","Tarjeta bancaria agregada");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tarjeta bancaria agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al agregar tarjeta bancaria");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
			//creacion
			else {
				var datos = {
					p_IdEmpresa 			: $scope.SesionIdEmpresa,
					p_IdUsuario 			: $scope.SesionIdUsuario,
					p_IdModulo 				:'SOC',
					p_UsaReglasNegocio 		:'S',
					p_IdBanco 				: mtb.TarjetaBancaria.IdBanco || 0,
					p_IdTipoTarjetaBancaria	: mtb.TarjetaBancaria.IdTipoTarjetaBancaria || '',
					p_NumeroTarjetaBancaria	: mtb.TarjetaBancaria.NumeroTarjetaBancaria || '',
					p_CodigoVerificacion	: mtb.TarjetaBancaria.CodigoVerificacion || 0,
					p_IdPersona				: mtb.IdPersona,
					p_FechaVencimiento 		: mtb.TarjetaBancaria.FechaVencimiento || '0001-01-01',
					p_IdEstado				: mtb.TarjetaBancaria.IdEstado
						
				};

				if(!mtb.validarCamposVacios()) return;
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaTarjetaBancaria(datos).$promise.then(
					function(result) {
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tarjeta bancaria agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al agregar tarjeta bancaria");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mtb.asignarValoresPorDefecto = function (){
			if(parametros.TarjetaBancaria) return;
			$.each(mtb.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mtb.TarjetaBancaria[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mtb.TarjetaBancaria[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		mtb.validarCamposVacios = function(){
			
			let tipoTarjeta = mtb.TarjetaBancaria.IdTipoTarjetaBancaria || ''
			let numeroTarjeta =  mtb.TarjetaBancaria.NumeroTarjetaBancaria || ''
			let bancoID =  mtb.TarjetaBancaria.IdBanco || 0
			let fechaVencimiento =  mtb.TarjetaBancaria.FechaVencimiento || '0001-01-01'
			let codigoVerificacion= mtb.TarjetaBancaria.CodigoVerificacion || 0
			
			if(mtb.TarjetaBancaria.IdTipoTarjetaBancaria=="TDEB"){				
				if(bancoID==0 || tipoTarjeta=="" || numeroTarjeta==''){
                	bcsComunes.msg("error","error","Se deben completar todos los campos");
                	return;
            	} 
			}else{
				if(bancoID==0 || tipoTarjeta=="" || numeroTarjeta==''|| fechaVencimiento=='0001-01-01' || codigoVerificacion==0){
                	bcsComunes.msg("error","error","Se deben completar todos los campos"); 
                	return;
            	}	
			}
			return true;           
		}
		
		mtb.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		// Se obtienen placeholder
		mtb.placeholder = {};
		//mtb.placeholder.numeroTarjeta		= $translate.instant('UI_PLACEHOLDER_NUMERO_TARJETA');
		//mtb.placeholder.codigoVerificacion	= $translate.instant('UI_PLACEHOLDER_CODIGO_VERIFICACION');
		mtb.placeholder.numeroTarjeta		= '1111222233334444';
		mtb.placeholder.codigoVerificacion	= '111';
		
		// Valida formato numero tarjeta
		mtb.validaFormatoNroTarjeta = function(){
			if(mtb.TipoTarjetaBancoObj !== undefined && mtb.TipoTarjetaBancoObj && mtb.TarjetaBancaria.NumeroTarjetaBancaria !== undefined){
				//if(region.ValidaFormatoNumeroTarjetaCredito(mtb.TipoTarjetaBancoObj.IdTipoTarjetaBancaria, mtb.TipoTarjetaBancoObj.LargoTarjeta, mtb.TarjetaBancaria.NumeroTarjetaBancaria)==false){
				if(region.ValidaFormatoNumeroTarjetaCredito('',16,mtb.TarjetaBancaria.NumeroTarjetaBancaria) == false){  // TODO revisar mejora para traer el IdTipoTarjetaBancaria y el LargoTarjeta 
					bcsComunes.msg("warning","Advertencia",$translate.instant("UI_FORMATO_NUMERO_TARJETA_INCORRECTO"));
					mtb.formTarjetaBancaria.$valid = undefined;
					return false;
				}
			}
	    	return true;
	    }
		
		// Valida formato codigo verificacion
		mtb.validaFormatoCodVerificacion = function(){
			if(mtb.TipoTarjetaBancoObj !== undefined && mtb.TipoTarjetaBancoObj && mtb.TarjetaBancaria.CodigoVerificacion !== undefined){
				if(mtb.TipoTarjetaBancoObj.IdTipoTarjetaBancaria === 'TDEB')  return true;
				if(region.ValidaFormatoCodigoVerificacion('',4,mtb.TarjetaBancaria.CodigoVerificacion) == false){  // TODO revisar mejora para traer el IdTipoTarjetaBancaria y el LargoCodigoVerificacion
					bcsComunes.msg("warning","Advertencia",$translate.instant("UI_FORMATO_CODIGO_VERIFICACION_INCORRECTO"));
					mtb.formTarjetaBancaria.$valid = undefined;
					return false;
				}
			}
	    	return true;
	    }
	});
}());
//------------------------- FIN CONTROLLER --------------------------