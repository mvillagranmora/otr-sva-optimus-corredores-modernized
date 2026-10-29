(function() {
	angular.module('ModuloAPV').controller('ConsultaContratoSAAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConsultasAPV,bcsServiciosAPV,$rootScope)
	{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		var x = 0;
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		
		var csaapv = this;
		csaapv.parametrosEntrada = bcsComunes.ventanaParams(idVentana);
		var paramVentanaDistribuir = {}
		
		$scope.data = {};
		$scope.data.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
		$scope.fechaActual = new Date();
		
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		
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
				transport		:
				{
					read:
					{
						url 		: bcsConsultasAPV('ConsultarContratoSAAPV'),
						dataType	: "json"
					}
				},
                schema:
				{ 
					model: 
                    {
						fields: 
						{
							"IdContratoSAAPV"				: {type: "number"},
							
							"NombrePlanAhorro"				: {type: "string"},
							"FechaSuscripcion"				: {type: "string"},
							"FolioContrato"					: {type: "string"},
							
							"DocumentoIdentidadCliente"		: {type: "string"},
							"NombreCliente"					: {type: "string"},
							"ApellidoPaternoCliente"		: {type: "string"},
							"ApellidoMaternoCliente"		: {type: "string"},
							"FechaNacimientoCliente"		: {type: "string"},
							"Nacionalidad"					: {type: "string"},
							"EstadoCivil"					: {type: "string"},
							"Genero"						: {type: "string"},
							"Pais"							: {type: "string"},
							"Ciudad"						: {type: "string"},
							"ComunaCliente"					: {type: "string"},
							"DireccionCliente"				: {type: "string"},
							"NumeroDireccionCliente"		: {type: "string"},
							"CargoPersona"					: {type: "string"},
							"CategoriaInversionista"		: {type: "string"},
							"IdPerfilComercialBaseOrigen"	: {type: "string"},
							"TelefonoFijo"					: {type: "string"},
							"TelefonoMovil"					: {type: "string"},
							"TelefonoLaboral"				: {type: "string"},
							"EmailCliente"					: {type: "string"},
							
							"IdEmpleadoEjecutivo"			: {type: "number"},
							"NombrePersonaEjecutivo"		: {type: "string"},
							
							"NombreEmpleador"				: {type: "string"},
							"DocumentoIdentidadEmpleador"	: {type: "string"},
							"TelefonoEmpleador"				: {type: "string"},
							"DireccionComercialEmpleador"	: {type: "string"},
							"NumeroDireccionEmpleador"		: {type: "string"},
							"ComunaEmpleador"				: {type: "string"},
							"NombreEncargado"               : {type: "string"},
							"EmailEncargado"                : {type: "string"},
							"TelefonoEncargado"				: {type: "string"},
							
							"MedioRecaudacion"              : {type: "string"},
							"EntidadRecaudadora"            : {type: "string"},
							"CodigoPrevision"               : {type: "string"},
							"Pensionado"                    : {type: "string"},
							"Trabajador"                    : {type: "string"},
							
							"FormaDePago"                   : {type: "string"},
							"Recaudador"                    : {type: "string"},
							"Recaudacion"              		: {type: "string"},
							"Institucion"                   : {type: "string"},
							"DiaDescuento"                  : {type: "string"},
							"ComisionTransferencia"         : {type: "string"},
							
							"NombreEntidadOrigen"           : {type: "string"},
							"RutEntidadOrigen"              : {type: "string"},
							"TipoDeAhorro"                  : {type: "string"},
							"FondoDeOrigen"                 : {type: "string"},
							"TipoTraspaso"                  : {type: "string"},
							"MontoTraspaso"                 : {type: "string"},
							"UFTraspaso"                    : {type: "string"},
							"PorcentajeTraspaso"            : {type: "string"},
							
							"IdPortafolio"					: {type: "number"},
							"Producto"						: {type: "string"},
							"MontoPesos"					: {type: "number"},
							"MontoUF"						: {type: "number"},
							"Porcentaje"					: {type: "number"},
							"Pago"							: {type: "string"},
							"PeriodoInicio"					: {type: "string"},
							"PeriodoTermino"				: {type: "string"},
							"RegimenTributario"				: {type: "string"},
							"IdMatrizAgrupadorAPV"			: {type: "number"},
							"CodigoAgrupador"				: {type: "string"},
							"Proposito"						: {type: "string"}
						}
					}
				}
			}
		};
		
		var defOrigenAhorro = {
			type: "array",
			definition : {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                sort 			: false,
				schema: {
					model: {
						fields: {
							Producto				: { type: "string"},
							MontoPesos				: { type: "number"},
							MontoUF					: { type: "number"},
							Porcentaje				: { type: "number"},
							Pago	    			: { type: "string"},
							PagoNombre    			: { type: "string"},
							MesInicio				: { type: "string"},	
							MesTermino				: { type: "string"},
							Regimen					: { type: "string"},
							IdMatrizAgrupadorAPV	: { type: "number"},
							CodigoAgrupador			: { type: "string"},
							IdPropositoPortafolio	: { type: "string"},
						}
					}
				}
			}	
		};
		
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/

		var dsOrigenAhorro = new bcs.DataSource(defOrigenAhorro);
		csaapv.origenAhorro = {
			dataSource: dsOrigenAhorro,
			columns: [
				{field: "Producto"		,title: $translate.instant('UI_PRODUCTO')	, width: "80px", align: "rigth" , sortable : true, editable: false },
				{field: "MontoPesos"	,title: $translate.instant('UI_MONTO_PESOS'), width: "80px", align: "rigth" , sortable : true, editable: false, format: function(value, item){ return util.bcs_formato(value,'GEN','Monto',$rootScope.SesionIdMonedaSistema,'',''); }},
			    {field: "MontoUF"		,title: $translate.instant('UI_MONTO_UF')	, width: "80px", align: "rigth" , sortable : true, editable: false, format: function(value, item){ return util.bcs_formato(value,'GEN','Monto','USD','',''); }},
			    {field: "Porcentaje"	,title: $translate.instant('UI_PORCENTAJE')	, width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "Pago"			,title: $translate.instant('UI_PAGO')		, width: "80px", align: "left" , sortable : true, editable: false, hidden: true },
			    {field: "PagoNombre"	,title: $translate.instant('UI_PAGO')		, width: "80px", align: "left" , sortable : true, editable: false },
			    {field: "MesInicio"		,title: $translate.instant('UI_MES_INICIO')	, width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "MesTermino"	,title: $translate.instant('UI_MES_TERMINO'), width: "80px", align: "rigth" , sortable : true, editable: false },
			    {field: "Regimen"		,title: $translate.instant('UI_REGIMEN')	, width: "80px", align: "rigth" , sortable : true, editable: false }
			    ],	
			sortable		: false,
			pageable		: true,
			autoBind		: false,
			height			: 100,
		    selectable 		: "row",
		    userSettings	: false,
			navigatable		: true,
			gridCreated	    : function(grid)
			{
				if(csaapv.parametrosEntrada.modoVentana === 'C')
				{
					csaapv.IdContratoSAAPV = csaapv.parametrosEntrada.IdContratoSAAPV;
					//llamar query para traer el contrato
					csaapv.ConsultaContratoSAAPV(csaapv.IdContratoSAAPV);
					x=1;
				}
			}
		}
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		csaapv.DistribuirInversionSAAPV = function(){
			var filterVentana = Array(); 
			filterVentana.modoVentana = csaapv.parametrosEntrada.modoVentana;
			
			var MontoPesosAPV = 0;
			var MontoPesosDC = 0;
			var MontoUFAPV = 0;
			var MontoUFDC = 0;
			
			if(dsOrigenAhorro.view().length <= 0){
				bcsComunes.ventanaMsgErr('Debe ingresar Monto Origen Ahorro.',$scope.ventanaId);
				return;
			}
			
			$.each(dsOrigenAhorro.view(),function(key,value){
				if(value.Regimen.substring(0, 3) === 'APV'){
					MontoPesosAPV =+ value.MontoPeso;
					MontoUFAPV =+ value.MontoUF;
				}
				if(value.Regimen.substring(0, 2) === 'DC'){
					MontoPesosDC =+ value.MontoPeso;
					MontoUFDC =+ value.MontoUF;
				}
			});
			
			var MontoTotalPesos = MontoPesosAPV + MontoPesosDC;
			var MontoTotalUF = MontoUFAPV + MontoUFDC;
			
			if(filterVentana.modoVentana === 'I')
			{
				filterVentana.IdContratoSAAPV = csaapv.FolioContrato;
				filterVentana.MontoTotal = MontoTotalPesos > 0 ? MontoTotalPesos : MontoTotalUF; 
				filterVentana.paramVentanaDistribuir = paramVentanaDistribuir;
				filterVentana.IdTipoInstrumento = csaapv.NombrePlanTipoInversion === 'ABIERTO' ? "FFMM" : "";
				filterVentana.FolioContratoSAAPV = 0;
			}
			else
			{
				filterVentana.IdContratoSAAPV = csaapv.IdContratoSAAPV;
				filterVentana.FolioContratoSAAPV = csaapv.FolioContrato;
				filterVentana.MontoTotal = MontoTotalPesos > 0 ? MontoTotalPesos : MontoTotalUF;
				filterVentana.paramVentanaDistribuir = paramVentanaDistribuir;
				filterVentana.IdTipoInstrumento = csaapv.NombrePlanTipoInversion === 'ABIERTO' ? "FFMM" : "";
			}
			
			//var IdVentanamodal = bcsComunes.ventanaAbrir("DistribuirInversionSAAPV", filterVentana, true, false, idVentana);
			var IdVentanamodal = bcsComunes.ventanaAbrir("ConsultaDistribucionInversionSAAPV", filterVentana, true, false, idVentana);
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				if(data !== undefined)
					paramVentanaDistribuir = data;
	        });				
		};
				
		csaapv.ConsultaContratoSAAPV = function(IdContratoSAAPV){
			
			if(csaapv.IdContratoSAAPV === undefined)
				return;
			
			var filtro = Array(); 
			filtro.push({ field: "IdContratoSAAPV" , operator: "eq", value: Number(csaapv.IdContratoSAAPV) });
			
			var ConsultaContrato = new bcs.DataSource(datosSAAPV);
			ConsultaContrato.query({ filter: filtro }).then(function(){
				if(ConsultaContrato.view().length > 0)
				{	
					csaapv.NombrePlanTipoInversion			= ConsultaContrato.view()[0].NombrePlanAhorro;
					csaapv.FechaSuscripcion					= ConsultaContrato.view()[0].FechaSuscripcion;
					csaapv.FolioContrato					= ConsultaContrato.view()[0].FolioContrato;

					csaapv.DocumentoIdentidadCliente		= ConsultaContrato.view()[0].DocumentoIdentidadCliente;
					csaapv.Nombres							= ConsultaContrato.view()[0].NombreCliente;
					csaapv.ApellidoPaterno					= ConsultaContrato.view()[0].ApellidoPaternoCliente;
					csaapv.ApellidoMaterno					= ConsultaContrato.view()[0].ApellidoMaternoCliente;
					csaapv.FechaNacimiento					= ConsultaContrato.view()[0].FechaNacimientoCliente;
					csaapv.Nacionalidad						= ConsultaContrato.view()[0].Nacionalidad;
					csaapv.EstadoCivil						= ConsultaContrato.view()[0].EstadoCivil;
					csaapv.Genero							= ConsultaContrato.view()[0].Genero;
					csaapv.PaisResidencia					= ConsultaContrato.view()[0].Pais;
					csaapv.Ciudad							= ConsultaContrato.view()[0].Ciudad;
					csaapv.Comuna							= ConsultaContrato.view()[0].ComunaCliente;
					csaapv.CiudadComuna						= ConsultaContrato.view()[0].Ciudad;
					csaapv.Calle							= ConsultaContrato.view()[0].DireccionCliente;
					csaapv.kngNumeroDireccionCliente		= ConsultaContrato.view()[0].NumeroDireccionCliente;
					csaapv.CargoActividad					= ConsultaContrato.view()[0].CargoPersona;
					csaapv.CategoriaInversionista			= ConsultaContrato.view()[0].NombreCategoriaInversionista;
					csaapv.PerfilComercial					= ConsultaContrato.view()[0].NombrePerfilComercial;
					csaapv.TelefonoFijo						= ConsultaContrato.view()[0].TelefonoFijo;
					csaapv.TelefonoMovil					= ConsultaContrato.view()[0].TelefonoMovil;
					csaapv.TelefonoLaboral					= ConsultaContrato.view()[0].TelefonoLaboral;
					csaapv.EmailCliente						= ConsultaContrato.view()[0].EmailCliente;
					csaapv.Ejecutivo						= ConsultaContrato.view()[0].NombrePersonaEjecutivo;
					
					csaapv.RazonSocial						= ConsultaContrato.view()[0].NombreEmpleador;
					csaapv.DocumentoIdentidadEmpleador		= ConsultaContrato.view()[0].DocumentoIdentidadEmpleador;
					csaapv.TelefonoEmpleador				= ConsultaContrato.view()[0].TelefonoEmpleador;
					csaapv.DireccionComercialEmpleador		= ConsultaContrato.view()[0].DireccionComercialEmpleador;
					csaapv.kngNumeroDireccionEmpleador		= ConsultaContrato.view()[0].NumeroDireccionEmpleador;
					csaapv.ComunaEmpleador					= ConsultaContrato.view()[0].ComunaEmpleador;
					csaapv.CiudadEmpleador					= ConsultaContrato.view()[0].CiudadEmpleador;
					csaapv.NombreEncargado					= ConsultaContrato.view()[0].NombreEncargado;
					csaapv.EmailEncargado					= ConsultaContrato.view()[0].EmailEncargado;
					csaapv.TelefonoEncargado				= ConsultaContrato.view()[0].TelefonoEncargado;

					csaapv.MedioRecaudacionTrabajador		= ConsultaContrato.view()[0].MedioRecaudacion;
					csaapv.IdentidadRecaudadora				= ConsultaContrato.view()[0].EntidadRecaudadora;
					csaapv.Pensionado						= ConsultaContrato.view()[0].Pensionado;
					csaapv.Trabajador						= ConsultaContrato.view()[0].Trabajador;

					csaapv.FormaDePago						= ConsultaContrato.view()[0].FormaDePago;
					csaapv.Recaudador						= ConsultaContrato.view()[0].Recaudador;
					csaapv.Recaudacion						= ConsultaContrato.view()[0].Recaudacion;
					csaapv.Institucion						= ConsultaContrato.view()[0].Institucion;
					csaapv.DiaDescuento						= ConsultaContrato.view()[0].DiaDescuento;
					csaapv.ComisionTransferencia 			= ConsultaContrato.view()[0].ComisionTransferencia;

					csaapv.NombreEntidadOrigen				= ConsultaContrato.view()[0].NombreEntidadOrigen;
					csaapv.RutEntidadOrigen					= ConsultaContrato.view()[0].RutEntidadOrigen;
					csaapv.TipoAhorro						= ConsultaContrato.view()[0].TipoDeAhorro;
					csaapv.FondoOrigen						= ConsultaContrato.view()[0].FondoDeOrigen;
					csaapv.TipoTraspaso						= ConsultaContrato.view()[0].TipoTraspaso;
					csaapv.MontoTraspasoPeso				= ConsultaContrato.view()[0].MontoTraspaso;
					csaapv.UFTraspaso						= ConsultaContrato.view()[0].UFTraspaso;
					csaapv.porcentajeTraspaso				= ConsultaContrato.view()[0].PorcentajeTraspaso;
					csaapv.CodigoTipoSolicitud				= ConsultaContrato.view()[0].CodigoTipoSolicitud === 'TRA' ? 'TRASPASO' : 'AUTORIZACION';
					
				}
				if(ConsultaContrato.view()[0].CodigoTipoSolicitud === 'TRA')
				{
					csaapv.TipoTraspasoHabilito = true;
					csaapv.OrigenTraspaso = ConsultaContrato.view()[0].TipoMovimiento;
					
				}	
				
				if(ConsultaContrato.view()[0].IdPortafolio !== undefined && ConsultaContrato.view()[0].IdPortafolio !== 0)
				{
					if(ConsultaContrato.view().length > 1)
					{
						var Pagonombre = '';
						
						$.each(ConsultaContrato.view(),function(key,value){
							if(value.Pago === 'IND')
								Pagonombre = 'INDEFINIDO';
							else if(value.Pago === 'TEM')
								Pagonombre = 'TEMPORAL';
							else if(value.Pago === 'UNI')
								Pagonombre = 'UNICO';
							else
								Pagonombre = '';
							
							dsOrigenAhorro.add({
								IdPortafolio		  : value.IdPortafolio,
								Producto 			  : value.Producto,
								MontoPesos			  : value.MontoPesos,
								MontoUF				  : value.MontoUF,
								Porcentaje			  : value.Porcentaje,
							    Pago				  : value.Pago,
							    PagoNombre			  : Pagonombre,
							    MesInicio			  : value.PeriodoInicio,
							    MesTermino			  : value.PeriodoTermino,
							    Regimen				  : value.RegimenTributario,
							    IdMatrizAgrupadorAPV  : value.IdMatrizAgrupadorAPV,
							    CodigoAgrupador		  : value.CodigoAgrupador,
							    IdPropositoPortafolio : value.Proposito
							});
							
						});
					}
					
					if(ConsultaContrato.view().length === 1)
					{
						var Pagonombre = '';
						if(ConsultaContrato.view()[0].Pago === 'IND')
							Pagonombre = 'INDEFINIDO';
						else if(ConsultaContrato.view()[0].Pago === 'TEM')
							Pagonombre = 'TEMPORAL';
						else if(ConsultaContrato.view()[0].Pago === 'UNI')
							Pagonombre = 'UNICO';
						else
							Pagonombre = '';
						
						dsOrigenAhorro.add({
							IdPortafolio		  : ConsultaContrato.view()[0].IdPortafolio,
							Producto 			  : ConsultaContrato.view()[0].Producto,
							MontoPesos			  : ConsultaContrato.view()[0].MontoPesos,
						    MontoUF				  : ConsultaContrato.view()[0].MontoUF,
						    Porcentaje			  : ConsultaContrato.view()[0].Porcentaje,
						    Pago				  : ConsultaContrato.view()[0].Pago,
						    PagoNombre			  : Pagonombre,
						    MesInicio			  : ConsultaContrato.view()[0].PeriodoInicio,
						    MesTermino			  : ConsultaContrato.view()[0].PeriodoTermino,
						    Regimen				  : ConsultaContrato.view()[0].RegimenTributario,
						    IdMatrizAgrupadorAPV  : ConsultaContrato.view()[0].IdMatrizAgrupadorAPV,
						    CodigoAgrupador		  : ConsultaContrato.view()[0].CodigoAgrupador,
						    IdPropositoPortafolio : ConsultaContrato.view()[0].Proposito
						});
					}
				}
			});
		};
	});
}());