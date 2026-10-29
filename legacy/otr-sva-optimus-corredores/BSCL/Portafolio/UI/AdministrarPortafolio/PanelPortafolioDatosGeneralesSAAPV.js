+(function() {
	angular.module('ModuloPortafolio').controller('DatosSAAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var psaapv = this;

		var contadorHabilita = 0;
		var varIdPosicionInicial;
		
		$scope.data = {};
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		psaapv.bloqueoModificar = true;
		
		$scope.admPort.psaapv = psaapv;
		
		psaapv.FolioContratoFiltro = {
				filtros: {
					IdCliente  : {"field": "IdCliente","operator": "eq", "value"  : parametrosVentana.IdCliente },
				},
		}
		
		var datosSAAPV = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdContratoSAAPV", dir: "DESC" },
                    pageSize        : 10,
                    transport		: {
									read: {
										url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6004" ,
										dataType	: "json"
								    }
					},
                    schema 			: { 
                    				   model: 
                    						{
	                    					   fields : 
												{													
													"IdContratoSAAPV"				: {type: "number"},
												    "FolioContratoSAAPV" 			: {type: "string"},
												    "IdPlan" 						: {type: "number"},
													"NombrePlan" 					: {type: "string"},
													"FechaSuscripcion" 				: {type: "sysdate"},
													"FechaCreacion" 				: {type: "sysdate"},
													"FechaAprobacion" 				: {type: "sysdate"},
													"FechaNotificacion" 			: {type: "sysdate"},
													"FechaRecepcion"				: {type: "sysdate"},
													"Folio"							: {type: "string"},
													
													/*Cliente*/
													"IdPersona" 					: {type: "number"},
													"RutCliente"					: {type: "string"},
													"NombrePersona"					: {type: "string"},
													"Tipo"							: {type: "string"},
													"IdCliente"						: {type: "number"},
													"FechaNacimientoCliente"		: {type: "sysdate"},
													"EstadoCivil"					: {type: "string"},
													"IdTipoGenero"					: {type: "string"},
													"DireccionCliente"				: {type: "string"},													
													"TelefonoCliente"				: {type: "string"},
													"TelefonoComercialCliente"		: {type: "string"},
													"TelefonoCelularCliente"		: {type: "string"},
													"EmailCliente"					: {type: "string"},													
													/*Empleador*/
													"IdEmpleador" 					: {type: "number"},
													"RutEmpleador" 					: {type: "string"},
													"NombreEmpleador" 				: {type: "string"},												
													"DireccionComercialEmpleador"	: {type: "string"},
													"TelefonoEmpleador"				: {type: "string"},
													"NombreEncargado"				: {type: "string"},
													"EmailEncargado"				: {type: "string"},
													"TelefonoEncargado"				: {type: "string"},
													
													"CodigoPrevision"				: {type: "number"},
													"Pensionado"		        	: {type: "string"},
													"Trabajador"					: {type: "string"},
												    "OrigenAhorroCV"				: {type: "string"},
													"MontoAhorroCV" 				: {type: "number"},
													"UFAhorroCV" 					: {type: "number"},
													"PorcentajeRegimenImponibleCV"	: {type: "number"},
													"OrigenAhorroDC" 				: {type: "string"},
													"MontoAhorroDC" 				: {type: "number"},
													"UFAhorroDC" 					: {type: "number"},
													"PorcentajeRegimenImponibleDC"	: {type: "number"},
													"Pago" 							: {type: "string"},
													"PeriodoInicio" 				: {type: "string"},
													"PeriodoTermino"				: {type: "string"},
													"RegimenTributario" 			: {type: "string"},
													"FormaDePago"					: {type: "string"},
													"IdMedioRecaudacion"		    : {type: "number"},
													"CodigoMedioRecaudacion"		: {type: "string"},
													"NombreMedioRecaudacion"		: {type: "string"},
												    "DiaDescuento" 					: {type: "number"},
												    "Institucion" 					: {type: "string"},
													"ComisionTransferencia" 		: {type: "number"},
													"NombreEntidadOrigen" 			: {type: "string"},
													"RutEntidadOrigen"				: {type: "string"},
													"TipoDeAhorro" 					: {type: "string"},
													"FondoDeOrigen" 				: {type: "string"},
													"TipoTraspaso"					: {type: "string"},
													"MontoTraspaso" 				: {type: "number"},
													"UFTraspaso"					: {type: "number"},
													"PorcentajeTraspaso"			: {type: "number"},
													"IdEstadoEnvioNotificacion"		: {type: "string"},
													"IdEstadoSolicitud"				: {type: "string"},
													"IdEstado"						: {type: "string"},
												}
											}
									  }
    			 }
		};
		
		
		psaapv.ConsultaContratoSAAPV = function(IdContratoSAAPV){
			
			if(IdContratoSAAPV === undefined)
				return;
			
			var filtro = Array(); 
			filtro.push({ field: "IdContratoSAAPV" , operator: "eq", value: Number(IdContratoSAAPV) });
			
			var ConsultaContrato = new bcs.DataSource(datosSAAPV);
			ConsultaContrato.query({ filter: filtro }).then(function(){
				if(ConsultaContrato.view().length > 0){
					psaapv.FolioContratoSAAPV = ConsultaContrato.view()[0].FolioContratoSAAPV;
					//$scope.data.FolioContrato = ConsultaContrato.view()[0].FolioContratoSAAPV;
					
					psaapv.NombrePlan = ConsultaContrato.view()[0].NombrePlan; 
					
					psaapv.kngFechaSuscripcion = ConsultaContrato.view()[0].FechaSuscripcion;
					psaapv.IdEstado = ConsultaContrato.view()[0].IdEstado;
					psaapv.kngFechaRegistro = "";
					psaapv.RazonSocial = ConsultaContrato.view()[0].NombreEmpleador; 
					psaapv.RutEmpleador = ConsultaContrato.view()[0].RutEmpleador;
					psaapv.Institucion = ConsultaContrato.view()[0].Institucion;
					psaapv.NombreInstitucion = ConsultaContrato.view()[0].NombreEntidadOrigen;
					psaapv.CodigoMedioRecaudacion = "";
					psaapv.Trabajador = ConsultaContrato.view()[0].Trabajador;
					
					if(ConsultaContrato.view()[0].OrigenAhorroCV !== ""){
						psaapv.kngChkCV = true;
						psaapv.ChkCV = true;
					}
						
					
					if(ConsultaContrato.view()[0].OrigenAhorroDC !== ""){
						psaapv.kngChkDC = true;
						psaapv.ChkDC = true;
					}
						
					
					psaapv.kngMontoCV = ConsultaContrato.view()[0].MontoAhorroCV;
					psaapv.kngMontoDC = ConsultaContrato.view()[0].MOntoAhorroDC;
					
					psaapv.Regimen = ConsultaContrato.view()[0].RegimenTributario;
					
					psaapv.kngFechaInicio = ConsultaContrato.view()[0].PeriodoInicio;
					psaapv.kngFechaTermino = ConsultaContrato.view()[0].PeriodoTermino;
					
					psaapv.TipoPago = ConsultaContrato.view()[0].Institucion;
					psaapv.FormaPago = ConsultaContrato.view()[0].NombreMedioRecaudacion;
					psaapv.DiaDescuento = ConsultaContrato.view()[0].DiaDescuento;
					
				}
			});
			
		};
		
		$scope.$watch('psaapv.FolioContratoSeleccionado.IdContratoSAAPV',function(newval,oldval) {
			if(newval && newval !== oldval){
					psaapv.bloqueoModificar = false;
					psaapv.ConsultaContratoSAAPV(psaapv.FolioContratoSeleccionado.IdContratoSAAPV);	
			}
		});
		
		psaapv.VentanaContratoSAAPV = function(modoVentana){
			var filterVentana = Array(); 
			filterVentana.modoVentana = modoVentana;
			if(modoVentana === 'M'){
				
				filterVentana.IdContratoSAAPV 		= psaapv.FolioContratoSeleccionado.IdContratoSAAPV;
				
			}
			var IdVentanamodal = bcsComunes.ventanaAbrir("ContratoSAAPV", filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				psaapv.ConsultaContratoSAAPV(psaapv.FolioContratoSeleccionado.IdContratoSAAPV);	
	        });
		};	
		
	});
}());