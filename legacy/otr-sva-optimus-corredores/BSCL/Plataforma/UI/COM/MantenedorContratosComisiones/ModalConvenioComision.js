angular.module('ModuloPlataforma').controller('ModalConvenioComisionCtrl',
		function ($scope, bcsComunes, $translate, servicioContratoComision) {

			const mconvc 						= this;
			const idVentana 					= bcsComunes.ventanaWinId($scope);
			mconvc.parametros 					= bcsComunes.ventanaParams(idVentana);
			mconvc.domVentana					= $("[ventanaid='"+idVentana+"']");
			mconvc.combobox 					= {};
			mconvc.dataOriginal					= angular.copy(mconvc.parametros);
			mconvc.data 						= {};
			mconvc.dataOriginal.IdTipoContrato	= 'CONV';
			
			
			mconvc.combobox.oConfiguracionEstado = {
				tipo 			: "combobox-estado-pla",
				configuracion 	: {},
				filtros 		: {
						NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "CONTRATOCOMISION" }
				},
				idActual 		: false,
				seleccionado 	: false,
				idDefecto 		: ''
			};
	
			mconvc.cargaDatosPantalla = function(){
				
				mconvc.data.IdContratoComision	= mconvc.dataOriginal.IdContratoComision || null;
				mconvc.data.Nombre				= mconvc.dataOriginal.NombreContratoComision || '';
				mconvc.data.Descripcion			= mconvc.dataOriginal.DescripcionContratoComision || '';
				mconvc.data.IdEstadoDef			= mconvc.dataOriginal.IdEstado || 'ACT';
			
			};		
	
			mconvc.cancelar = function () {
				bcsComunes.ventanaData(idVentana, false);
				bcsComunes.ventanaCerrar(idVentana);
			};

			mconvc.aceptar = function () {
				if(mconvc.parametros.IdContratoComision) { // EDITAR
				
					const datosGuardar = mconvc.preparaDatos();
					bcsComunes.ventanaOverlay(idVentana,true);
					servicioContratoComision.modificarContratoComision(datosGuardar,function (result){
						
						let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
										? $translate.instant("UI_EDITAR_CONVENIO_EXITOSAMENTE")
										: $translate.instant("UI_ERROR_EDITAR_CONVENIO");
				
						let idModal = bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},idVentana);

						bcsComunes.ventanaPromesa(idModal).then(function(){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);	
						});
						
					}, function(resError){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_EDITAR_COVENIO"));
						console.log(resError);
					});
					
				}
				else { // CREAR
					const datosGuardar = mconvc.preparaDatos();
						
					bcsComunes.ventanaOverlay(idVentana,true);
					servicioContratoComision.crearContratoComision(datosGuardar,function (result){
						
						let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
									? $translate.instant("UI_CREAR_CONVENIO_EXITOSAMENTE")
									: $translate.instant("UI_ERROR_CREAR_CONVENIO");
									
						let idModal = bcsComunes.ventanaMsg({
							mensaje: titulo,
							conflictos:util.parseServerResponse(result)
						},idVentana);

						bcsComunes.ventanaPromesa(idModal).then(function(){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);	
						});
							
					}, function(resError){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_CREAR_CONVENIO"));
						console.log(resError);
					});
				}
			};

			mconvc.preparaDatos = function(){
			
				const datosGuardar = {};
				datosGuardar.p_IdEmpresa 			= $scope.SesionIdEmpresa,
				datosGuardar.p_IdUsuario 			= $scope.SesionIdUsuario;
				datosGuardar.p_IdModulo				= "PLA",
				datosGuardar.p_UsaReglasNegocio		= "N";
				
				datosGuardar.p_ContratosComisiones 	= [{
					IdEmpresa 						: $scope.SesionIdEmpresa,
					IdContratoComision				: mconvc.data.IdContratoComision,
					IdTipoContrato					: mconvc.data.IdTipoContrato,
					Nombre							: mconvc.data.Nombre,
					Descripcion						: mconvc.data.Descripcion,
					CodigoArea						: mconvc.data.CodigoArea || '',
					IdFormatoCobro					: mconvc.data.IdFormatoCobro || '', 
					IdClasificacionTipoInstrumento	: mconvc.data.IdClasificacionTipoInstrumento,
					IdMoneda						: mconvc.data.IdMoneda || '',
					IdSegmentoCliente				: mconvc.data.IdSegmentoCliente || null,
					IdPortafolio					: mconvc.data.portafolioSeleccionado.IdPortafolio,
					IdCliente						: mconvc.data.portafolioSeleccionado.IdCliente,
					IdEstado						: mconvc.combobox.oConfiguracionEstado.seleccionado.IdEstado,
				}];

				return datosGuardar;
			};
			
			
			mconvc.cargaDatosPantalla();
			
});