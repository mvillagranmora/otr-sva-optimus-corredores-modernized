(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPortafolioPreferenciasNegocioEnrolCtrl',
	function($scope, bcsServicioPlataforma, bcsComunes){
	  	var ppAtr = this;
		ppAtr.portafolioIndicadores = $scope.admSoc.dataSocioNegocio;
		ppAtr.datosDespues = {};
		ppAtr.datosAntes = {};
		ppAtr.idVentana = bcsComunes.ventanaWinId($scope);
		
		ppAtr.dsModalidadOrdenes = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549",
						dataType: "json"
					}
				},
	    	    filter: { field:"CodigoTipoIndicador", operator:"eq", value:'ORDMODALIDAD' },
	    	    schema: {
	    	    	model : {
	    	    		fields : {
	        			
	    	    		}
	    	    	}
	    	    }
	    	  }
		});
		
		let consultaIndicadoresBase = 
		{
			type : "odata",
			definition : {
				serverFiltering 	: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700255",
						dataType: "json"}
				},
	             schema: { 
	             	model: {
	             		fields : {
	             			"IdEnrolamiento" 	 	: {type: "integer"},
	             			"IdTipoCliente"			: {type: "string"},
	             			"IdTipoEntidad" 		: {type: "string"},
	             			"NombreTipoIndicador" 	: {type: "string"},
	             			"IdUso" 				: {type: "string"},
	             			"IdTipoIndicador" 		: {type: "integer"},
	             			"CodigoTipoIndicador" 	: {type: "string"},
	             			"ClaseDato" 			: {type: "string"},
	             			"CantidadDecimales" 	: {type: "integer"},
	             			"LongitudMinima" 		: {type: "integer"},
	             			"LongitudMaxima" 		: {type: "integer"},
	             			"IdTipoPersona" 		: {type: "string"},
	             			"ModeloCliente" 		: {type: "string"},
	             			"ValorIndicador" 		: {type: "string"},
	             			"CodigoValorIndicador" 	: {type: "string"},
	             			"NombreValorIndicador" 	: {type: "string"},
	             			"IdIndicador" 			: {type: "integer"},
	             			"IdEmpresa" 			: {type: "integer"}
	             		}
	             	}
	             }
			}
		};
		
		let cargarDatos = function cargarDatos(){
			bcsComunes.ventanaOverlay(ppAtr.idVentana,true);
			let consultarIndicadores = new bcs.DataSource(consultaIndicadoresBase);
		
			let filtros = [{ field: "IdTipoEntidad", operator:"eq",value: 'PORTAFOLIO'}, { field: "IdEnrolamiento", operator:"eq",value: ppAtr.portafolioIndicadores.IdEnrolamiento}];
			ppAtr.definicionIndicadores = {};
			
			consultarIndicadores.query({ filter : filtros}).then(function(){
				if(consultarIndicadores.view().length > 0){
					$.each(consultarIndicadores.view(),function(key,value){
						ppAtr.definicionIndicadores[value.GrupoIndicador + "//" +value.CodigoTipoIndicador] = value;
						
					});
					
					ppAtr.datosAntes['GENERALES//ADMCART'] = (ppAtr.definicionIndicadores['GENERALES//ADMCART'].ValorIndicador === 'S') ? true : false;
					
					let mecanismos = {};
					if (ppAtr.definicionIndicadores['GRUPOORDENES//ORDMODALIDAD'].ValorIndicador) {
						mecanismos = JSON.parse(ppAtr.definicionIndicadores['GRUPOORDENES//ORDMODALIDAD'].ValorIndicador);
					}
					
					ppAtr.datosAntes['GRUPOORDENES//ORDMODALIDAD'] = mecanismos;
					
					ppAtr.dsModalidadOrdenes.fetch().then(function(){
						let result = ppAtr.dsModalidadOrdenes.data();
						ppAtr.valoresModalidadOrdenes = {};
						$.each(result,function(key,value){
							ppAtr.valoresModalidadOrdenes[value.Codigo] = value.DetalleIndicador;
						})
					});
					ppAtr.datosDespues = _.clone(ppAtr.datosAntes);
				}
				bcsComunes.ventanaOverlay(ppAtr.idVentana,false);
			});
		}
		
		
		
		ppAtr.guardar = function guardar(){
			bcsComunes.ventanaOverlay(ppAtr.idVentana,true);
			
			let datos = {
				p_IdEmpresa: $scope.$root.SesionIdEmpresa,
                p_IdUsuario: $scope.$root.SesionIdUsuario,
                p_IdModulo: 'SOC',
				//p_CodigoFormulario se establece en '' ya que no requiere actualmente un formulario
				//p_CodigoFormulario : $scope.admSoc.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO'], 				
				p_CodigoFormulario : '',
				//Se establece p_UsaReglasNegocio en 'N' ya que no requiere validación de campos obligatorios. 
				p_UsaReglasNegocio: 'N',
				p_IdTipoCanal : 'OPT',                
				p_Indicadores : []
			}
			
			$.each(ppAtr.datosDespues,function(key){
				let indicador = ppAtr.definicionIndicadores[key];
				if(indicador){
					idIndicador = indicador.IdIndicador;
					idTipoIndicador = indicador.IdTipoIndicador;
					let valorIndicador = '';
					
					if (indicador.ClaseDato === "CHECKBOX") {
						valorIndicador = (ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador]) ? 'S' : 'N';
					}
					else if (indicador.ClaseDato === "COMBO") {
						valorIndicador = (ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador]) ? ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador] : null;
					}
					else if (indicador.ClaseDato === "MULTI") {
						valorIndicador = (ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador]) ? JSON.stringify(ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador]) : null;
					}
					else {
						valorIndicador = ppAtr.datosDespues[indicador.GrupoIndicador + "//" +indicador.CodigoTipoIndicador];
					}
					
					datos.p_Indicadores.push({IdIndicador : idIndicador, IdEnrolamiento : ppAtr.portafolioIndicadores.IdEnrolamiento, ValorIndicador : valorIndicador});
				}
			});
			
			var successModificar = function(respuesta){	 
	    		bcsComunes.ventanaMsg({ mensaje : "Preferencias Modificadas",
					conflictos : util.parseServerResponse(respuesta)}, ppAtr.idVentana);
	    		bcsComunes.ventanaOverlay(ppAtr.idVentana,false);
				cargarDatos();
	    	};
	    	var errorModificar = function(respuesta){	
	    		console.log("Error:" + JSON.stringify(respuesta));	    		
				bcsComunes.msg("error","Error Interno",JSON.stringify(respuesta));
				bcsComunes.ventanaOverlay(ppAtr.idVentana,false);
	    	};

	    	bcsServicioPlataforma.ModificarAtributoEnrolamiento(datos,successModificar,errorModificar);
			
		}
		
		cargarDatos();

	});
}());