angular.module('ModuloPlataforma').controller('ModalContratoComisionCtrl',
		function ($scope, bcsComunes, $translate, servicioContratoComision, $timeout) {

			const mcc 						= this;
			const idVentana 				= bcsComunes.ventanaWinId($scope);
			mcc.parametros 					= bcsComunes.ventanaParams(idVentana);
			mcc.domVentana					= $("[ventanaid='"+idVentana+"']");
			mcc.combobox 					= {};
			mcc.dataOriginal				= angular.copy(mcc.parametros);
			mcc.data 						= {};
			mcc.dataOriginal.IdTipoContrato = 'CONT';
			mcc.dataOriginal.IdMoneda = mcc.dataOriginal.IdMoneda || 'CLP';			
			
			mcc.combobox.oConfiguracionEstado = {
				tipo 			: "combobox-estado-pla",
				configuracion 	: {},
				filtros 		: {
						NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "CONTRATOCOMISION" }
				},
				idActual 		: false,
				seleccionado 	: false,
				idDefecto 		: ''
			};
			
			mcc.combobox.oConfiguracionAreaCliente={
				tipo 			: "combobox-area-cliente",
				configuracion 	: {},
				filtros: {
							Codigo : { "field"   : "Codigo","operator": "eq", "value"   : "" },
						},
				idActual 		: false,
				seleccionado 	: false,
				idDefecto 		: 'POR DEFINIR'
				};
			
			
			mcc.cargaDatosPantalla = function(){
				
				mcc.data.IdContratoComision	= mcc.dataOriginal.IdContratoComision || null;
				mcc.data.Nombre				= mcc.dataOriginal.NombreContratoComision || '';
				mcc.data.Descripcion		= mcc.dataOriginal.DescripcionContratoComision || '';
				mcc.data.IdEstadoDef		= mcc.dataOriginal.IdEstado || 'ACT';
			
			};		
	
			mcc.cancelar = function () {
				bcsComunes.ventanaData(idVentana, mcc.data);
				bcsComunes.ventanaCerrar(idVentana);
			};

			mcc.aceptar = function () {
				if(mcc.parametros.IdContratoComision) { // EDITAR
				
					const datosGuardar = mcc.preparaDatos();
					bcsComunes.ventanaOverlay(idVentana,true);
					servicioContratoComision.modificarContratoComision(datosGuardar,function (result){
						
						let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
										? $translate.instant("UI_EDITAR_CONTRATO_EXITOSAMENTE")
										: $translate.instant("UI_ERROR_EDITAR_CONTRATO");
				
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
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_EDITAR_CONTRATO"));
						console.log(resError);
					});
					
				}
				else { // CREAR
					const datosGuardar = mcc.preparaDatos();
						
					bcsComunes.ventanaOverlay(idVentana,true);
					servicioContratoComision.crearContratoComision(datosGuardar,function (result){
						
						let titulo = (result.P_OUT_REFID.length  > 0 && result.P_OUT_ERRORESCONTADOR === 0) 
									? $translate.instant("UI_CREAR_CONTRATO_EXITOSAMENTE")
									: $translate.instant("UI_ERROR_CREAR_CONTRATO");
									
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
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_CREAR_CONTRATO"));
						console.log(resError);
					});
				}
			};
	

			mcc.preparaDatos = function(){
			
				const datosGuardar = {};
				datosGuardar.p_IdEmpresa 			= $scope.SesionIdEmpresa,
				datosGuardar.p_IdUsuario 			= $scope.SesionIdUsuario;
				datosGuardar.p_IdModulo				= "PLA",
				datosGuardar.p_UsaReglasNegocio		= "N";
				
				datosGuardar.p_ContratosComisiones 	= [{
					IdEmpresa 						: $scope.SesionIdEmpresa,
					IdContratoComision				: mcc.data.IdContratoComision,
					IdTipoContrato					: mcc.data.IdTipoContrato,
					Nombre							: mcc.data.Nombre,
					Descripcion						: mcc.data.Descripcion,
					CodigoArea						: mcc.combobox.oConfiguracionAreaCliente.seleccionado.Codigo || '',
					IdFormatoCobro					: mcc.data.IdFormatoCobro || '', 
					IdClasificacionTipoInstrumento	: mcc.data.IdClasificacionTipoInstrumento,
					IdMoneda						: mcc.data.IdMoneda || '',
					IdSegmentoCliente				: mcc.data.IdSegmentoCliente || 0,
					IdPortafolio					: mcc.data.portafolioSeleccionado.IdPortafolio,
					IdCliente						: mcc.data.portafolioSeleccionado.IdCliente,
					IdEstado						: mcc.combobox.oConfiguracionEstado.seleccionado.IdEstado,
				}];
				
				return datosGuardar;
			};
			
			$scope.$watch('mcc.data.portafolioSeleccionado', function(newval, oldval) {
				
				if(!newval){
					if(!mcc.parametros.IdContratoComision){
						mcc.data.IdSegmentoCliente = (mcc.data.portafolioSeleccionado ? (mcc.data.portafolioSeleccionado.IdCliente == undefined ? "" : mcc.data.IdSegmentoCliente) : "");
					}
					return;
				} 
				
				if(newval.Codigo === '') {
					mcc.combobox.oConfiguracionAreaCliente.idDefecto = 'POR DEFINIR';
				}
				
				if(newval && mcc.data.portafolioSeleccionado.IdCliente != undefined && !mcc.parametros.IdContratoComision){
					mcc.ConsultarSegmentoCliente(mcc.data.portafolioSeleccionado.IdCliente);
				}
			});
			
			mcc.ConsultarSegmentoCliente = function(IdCliente) {
			 	var ConsultarSegmento = new bcs.DataSource(ConsultarSegmentoCliente);
	            var filterSegmento = [
	            	{ field: "IdCliente", operator: "eq", value: IdCliente }
	    		];
	    		
	    		ConsultarSegmento.query({ filter: filterSegmento }).then(function() {
					if (ConsultarSegmento.data().length > 0 && ConsultarSegmento.data()[0] != undefined) {
						mcc.data.IdSegmentoCliente = ConsultarSegmento.data()[0].IdSegmentoCliente;
					}
				});
			};
			
			var ConsultarSegmentoCliente = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700353",
							dataType: "json"
					    }
					},
					schema 	: {
						model	: {
							fields	: {
								IdSegmentoCliente : { type: "number"}
							}
						}
					}
				}
			});
			
			mcc.cargaDatosPantalla();
			
});