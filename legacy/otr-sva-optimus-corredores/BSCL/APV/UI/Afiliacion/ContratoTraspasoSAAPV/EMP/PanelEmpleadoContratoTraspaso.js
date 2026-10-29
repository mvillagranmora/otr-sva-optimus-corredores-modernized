/*************************************************************
 * Purpose	: Panel Portafolio Contrato
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 15/01/2018
 **************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelEmpleadoContratoTraspasoCtrl',
	function($scope,
			$translate,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConsultasAPV,
			bcsServiciosAPV	
			){
		
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var petrasp = this;

		petrasp.datosForm ={};
		$scope.ctrasp.petrasp = petrasp;
		
		var date = util.dateInicioDia($scope.SesionFechaSistema);
		var ultimoDia = new Date(date.getFullYear(),date.getMonth(),0);
		
		petrasp.limpiarEmpleadorHabilitado 		= true;
		petrasp.entidadRecaudadoraDeshabilitado	= false;
		
		petrasp.DocumentoIdentidadEmpleadorRequerido	= true;
		petrasp.RazonSocialRequerido					= true;
		
		petrasp.IdentidadRecaudadoraRequerido 			= false;
		petrasp.ComunaEmpleadorRequerido				= false;
		petrasp.DireccionComercialEmpleadorRequerido	= false;
		petrasp.NumeroDireccionEmpleadorRequerido		= false;
		
/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
		petrasp.filtroEntidadRecaudadora =
		{
				IdTipoEntidadRecaudadora : {field: "IdTipoEntidadRecaudadora", operator: "eq", value: 1}
		};
		
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		
		var datosCiudad = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                transport		: {
					read: {
						url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1627",
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdCiudad			: {type: "number"},
							NombreCiudad		: {type: "string"},
							NombreCiudadPais	: {type: "string"},
							IdPais				: {type: "number"},
							NombrePais			: {type: "string"},
							CodigoPais			: {type: "string"}
						}
					}
	            }
			}
		};
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		petrasp.limpiarEmpleador = function()
		{
			//Identificación Empleador
			petrasp.datosForm.SocioNegocioidActualEmp	= false;
			petrasp.DocumentoIdentidadEmpleador		= undefined;
		    petrasp.RazonSocial						= undefined;
		    petrasp.ComunaEmpleadorIdActual			= false
		    petrasp.CiudadEmpleador					= undefined;
		    petrasp.DireccionComercialEmpleador		= undefined;
		    petrasp.kngNumeroDireccionEmpleador		= undefined;
		    petrasp.TelefonoEmpleador					= undefined;
		    
		    //Encargado RRHH
		    petrasp.NombreEncargado					= undefined;
		    petrasp.EmailEncargado						= undefined;
		    petrasp.TelefonoEncargado					= undefined;
		    
		    //Identificación Empleador
		    petrasp.datosForm.DoctoIdentidadEmpDeshabilitado	= false;
		    petrasp.datosForm.RazonSocialDeshabilitado			= false;
		    petrasp.datosForm.ComunaEmpDeshabilitado			= true;
			petrasp.datosForm.DirComercialEmpDeshabilitado		= false;
			petrasp.datosForm.NumeroEmpDeshabilitado			= false;
			petrasp.datosForm.telEmpDeshabilitado				= false;
			
			//Encargado RRHH
			petrasp.datosForm.nomEncDeshabilitado				= false;
			petrasp.datosForm.emailEncDeshabilitado			= false;
			petrasp.datosForm.telEncEncDeshabilitado			= false;
			
			petrasp.DocumentoIdentidadEmpleadorRequerido	= true;
			petrasp.RazonSocialRequerido					= true;
			
			if(petrasp.FormaPagoDirectaIdActual === 2)
			{
				petrasp.ComunaEmpleadorRequerido				= true;
				petrasp.DireccionComercialEmpleadorRequerido	= true;
				petrasp.NumeroDireccionEmpleadorRequerido		= true;
			}
			else
			{
				petrasp.ComunaEmpleadorRequerido				= false;
				petrasp.DireccionComercialEmpleadorRequerido	= false;
				petrasp.NumeroDireccionEmpleadorRequerido		= false;
			}
		}
		
		petrasp.validaFormatoEmail = function(email)
		{
			if(email === 'ENC')
			{
				if (petrasp.EmailEncargado !== undefined && petrasp.EmailEncargado !== '' && region.ValidaFormatoEmail(petrasp.EmailEncargado)==false)
				{
					petrasp.EmailEncargado = '';
		    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
					$scope.formContratoSAAPV.$valid = undefined;
					return false;
				}	
			}
	    	
	    	return true;
	    };
	    
	    /*
	    petrasp.ValidoDiaDescuento = function(num)
	    {
			if(num > ultimoDia.getDate(num) || num === 0)
			{
				bcsComunes.msg("warning","warning","DIA_DE_DESCUENTO_TIENE_QUE_SER_MAYOR_A_0_Y_MENOR_O_IGUAL_A_" + ultimoDia.getDate());
				petrasp.kngDiaDescuento = ultimoDia.getDate(); 
			}
		};
		*/
	    
/******************************************************/
/*** WATCH											***/
/******************************************************/
		
		$scope.$watch('petrasp.datosForm.SocioNegocioSeleccionadoEmp',function(newval,oldval)
		{ 	
			if ((newval=== oldval) && (newval === undefined))
			{
				return
			}
			else
			{
				if(petrasp.datosForm.SocioNegocioSeleccionadoEmp ===  false )
				{
					/*
					petrasp.datosForm.RazonSocialDeshabilitado						= false;
					petrasp.datosForm.DoctoIdentidadEmpDeshabilitado 				= false;
					petrasp.datosForm.DirComercialEmpDeshabilitado					= false;
					petrasp.datosForm.NumeroEmpDeshabilitado						= false;
					petrasp.datosForm.ComunaEmpDeshabilitado						= true;
					petrasp.bloqueoPersona											= false;
					petrasp.datosForm.telEmpDeshabilitado							= false;
					petrasp.datosForm.nomEncDeshabilitado							= false;
					petrasp.datosForm.emailEncDeshabilitado						= false;
					petrasp.datosForm.telEncEncDeshabilitado						= false;
					*/
					
					petrasp.datosForm.CiudadEmpDeshabilitado						= true;
					
					petrasp.datosForm.FormaDePagoIndirectaDesabilitado				= true;
					petrasp.datosForm.EntidadRecaudadoraInstitucionDesabilitado	= true;
				}
				else
				{								
					$scope.ctrasp.fnCargarValoresPersona('J');
					
					petrasp.DocumentoIdentidadEmpleadorRequerido	= false;
					petrasp.RazonSocialRequerido					= false;
					petrasp.ComunaEmpleadorRequerido				= false;
					petrasp.DireccionComercialEmpleadorRequerido	= false;
					petrasp.NumeroDireccionEmpleadorRequerido		= false;
				}
			}
		});

		petrasp.Directa = true;
		$scope.$watch('petrasp.Directa',function(newval,oldval)
		{
			if(petrasp.Directa)
			{
				petrasp.Indirecta = false;
				petrasp.FormaPagoDirectaEnabled = false;
				
			    petrasp.EntidadRecaudadoraInstitucion		= undefined;
			    petrasp.FormaDePagoIndirecta				= undefined;
			    petrasp.kngComisionTransferencia			= undefined;
			}
			else
			{
				petrasp.FormaPagoDirectaEnabled 	= true;
				petrasp.FormaPagoDirectaIdActual	= undefined;
				petrasp.DiaDescuentoIdActual 		= undefined;
				
				petrasp.Indirecta = true;
			}
		});
		
		$scope.$watch('petrasp.Indirecta',function(newval,oldval)
		{
			if(petrasp.Indirecta)
			{
				petrasp.Directa 						= false;
				petrasp.FormaPagoIndirectaEnabled 		= false;
				petrasp.ComisionTransferenciaRequerido = true;
				petrasp.DiaDescuentoIdActual 			= undefined;
				
				if(petrasp.MedioRecaudacionTrabajadorIdActual !== undefined)
					petrasp.FormaDePagoIndirecta = petrasp.IdMedioRecaudacionTrabajador.Nombre;
				
				if(petrasp.IdentidadRecaudadoraIdActual !== undefined)
					petrasp.EntidadRecaudadoraInstitucion = petrasp.entidadRecaudadora.Nombre;
				
				petrasp.ComunaEmpleadorRequerido				= false;
				petrasp.DireccionComercialEmpleadorRequerido	= false;
				petrasp.NumeroDireccionEmpleadorRequerido		= false;
			}
			else
			{
				petrasp.FormaPagoIndirectaEnabled					= true;
				petrasp.MedioRecaudacionFormaPagoIndirectaIdActual	= undefined;
				petrasp.EntidadRecaudadoraInstitucion 				= undefined;
				petrasp.kngComisionTransferencia 					= undefined;
				petrasp.ComisionTransferenciaRequerido				= false;
				
				petrasp.Directa = true;
			}
		});
		
		$scope.$watch('petrasp.FormaPagoDirecta',function(newval,oldval)
		{
			if(petrasp.FormaPagoDirecta !== undefined)
			{
				if(petrasp.FormaPagoDirecta.Nombre === "PAC")
				{
					petrasp.DiaDescuentoRequerido 	= true
					petrasp.DiaDescuentoEnabled 	= false;
				}
				else
				{
					petrasp.DiaDescuentoRequerido 	= false;
					petrasp.DiaDescuentoEnabled 	= true;
					petrasp.DiaDescuentoIdActual	= undefined;
				}
			}
		});

		$scope.$watch('petrasp.MedioRecaudacionTrabajadorIdActual',function(newval,oldval)
		{
			if(petrasp.MedioRecaudacionTrabajadorIdActual !== undefined && petrasp.MedioRecaudacionTrabajadorIdActual !== false)
			{
				if(petrasp.IdMedioRecaudacionTrabajador.Nombre === 'IPS-EX INP')
				{
					petrasp.entidadRecaudadoraDeshabilitado = false;
					petrasp.IdentidadRecaudadoraIdActual = undefined;
					
					petrasp.IdentidadRecaudadoraRequerido = false;
				}
				else
				{
					petrasp.entidadRecaudadoraDeshabilitado = true;
					
					petrasp.IdentidadRecaudadoraRequerido = true;
				}
			}
		});

		$scope.$watch('petrasp.FormaPagoDirectaIdActual',function(newval,oldval)
		{
			if(petrasp.TrabajadorIdActual !== undefined && petrasp.TrabajadorIdActual !== false  && newval !== oldval)
			{
				if(petrasp.TrabajadorIdActual === 'IND' && petrasp.FormaPagoDirectaIdActual ===2)
				{
					bcsComunes.msg("warning","warning",$translate.instant("FORMA_DE_PAGO_NO_APLICA_A_TIPO_TRABAJADOR")); 
					petrasp.FormaPagoDirectaIdActual = '';
				}
			}
			
			if(petrasp.FormaPagoDirectaIdActual !== undefined && petrasp.FormaPagoDirectaIdActual !== false)
			{
				if(petrasp.FormaPagoDirectaIdActual === 2 && parametrosVentana.modoVentana === 'I')
				{
					petrasp.ComunaEmpleadorRequerido				= true;
					petrasp.DireccionComercialEmpleadorRequerido	= true;
					petrasp.NumeroDireccionEmpleadorRequerido		= true;
				}
				else
				{
					petrasp.ComunaEmpleadorRequerido				= false;
					petrasp.DireccionComercialEmpleadorRequerido	= false;
					petrasp.NumeroDireccionEmpleadorRequerido		= false;
				}
			}
		});

		$scope.$watch('petrasp.TrabajadorIdActual',function(newval,oldval)
		{
			if(petrasp.TrabajadorIdActual !== undefined && petrasp.TrabajadorIdActual !== false  && newval !== oldval && newval !== '')
			{
				petrasp.FormaPagoDirectaIdActual = '';		
			}
		});
		
		$scope.$watch('petrasp.MedioRecaudacionTrabajadorIdActual',function(newval,oldval)
		{
			if(petrasp.Indirecta)
			{
				if(petrasp.MedioRecaudacionTrabajadorIdActual !== undefined)
					petrasp.FormaDePagoIndirecta = petrasp.IdMedioRecaudacionTrabajador.Nombre;
			}
		});
		
		$scope.$watch('petrasp.IdentidadRecaudadoraIdActual',function(newval,oldval)
		{
			if(petrasp.Indirecta)
			{
				if(petrasp.IdentidadRecaudadoraIdActual !== undefined)
					petrasp.EntidadRecaudadoraInstitucion = petrasp.entidadRecaudadora.Nombre;
			}
		});

		$scope.$watch('petrasp.ComunaEmpleadorIdActual',function(newval,oldval)
		{
			if(parametrosVentana.modoVentana === 'I')
			{
				if(petrasp.ComunaEmpleadorIdActual !== undefined && petrasp.ComunaEmpleadorIdActual !== false)
				{
					var ConsultaCiudad = new bcs.DataSource(datosCiudad);
					var filtro = { field: "IdCiudad", operator:"eq", value: Number(petrasp.comunaEmpleadorSeleccionada.IdCiudad) };
					
					ConsultaCiudad.query({ filter: filtro }).then(function()
					{
						petrasp.CiudadEmpleador = ConsultaCiudad.view()[0].NombreCiudad;
					});
				}
				else
				{
					petrasp.CiudadEmpleador = undefined;
				}
			}
		});

		if(parametrosVentana.modoVentana === 'M')
		{
			if ($scope.ctrasp.datosEmpleador.length  > 0 )
			{
				//Identificación Empleador
			    petrasp.RazonSocial											= ($scope.ctrasp.datosEmpleador[0].RazonSocial 					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].RazonSocial);
			    petrasp.DocumentoIdentidadEmpleador							= ($scope.ctrasp.datosEmpleador[0].DocumentoIdentidadEmpleador	== undefined ? "" : $scope.ctrasp.datosEmpleador[0].DocumentoIdentidadEmpleador);
			    petrasp.idComunaEmpleadorPostFiltro							= ($scope.ctrasp.datosEmpleador[0].ComunaEmpleador	  			== undefined ? "" : $scope.ctrasp.datosEmpleador[0].ComunaEmpleador);
			    petrasp.CiudadEmpleador										= ($scope.ctrasp.datosEmpleador[0].CiudadEmpleador	  			== undefined ? "" : $scope.ctrasp.datosEmpleador[0].CiudadEmpleador);
			    petrasp.DireccionComercialEmpleador							= ($scope.ctrasp.datosEmpleador[0].DireccionComercialEmpleador	== undefined ? "" : $scope.ctrasp.datosEmpleador[0].DireccionComercialEmpleador);
			    petrasp.kngNumeroDireccionEmpleador							= ($scope.ctrasp.datosEmpleador[0].NumeroDireccionEmpleador		== undefined ? "" : $scope.ctrasp.datosEmpleador[0].NumeroDireccionEmpleador);
			    petrasp.TelefonoEmpleador										= ($scope.ctrasp.datosEmpleador[0].TelefonoEmpleador		 	== undefined ? "" : $scope.ctrasp.datosEmpleador[0].TelefonoEmpleador);
			    //Encargado RRHH
			    petrasp.NombreEncargado										= ($scope.ctrasp.datosEmpleador[0].NombreEncargado				== undefined ? "" : $scope.ctrasp.datosEmpleador[0].NombreEncargado);
			    petrasp.EmailEncargado											= ($scope.ctrasp.datosEmpleador[0].EmailEncargado				== undefined ? "" : $scope.ctrasp.datosEmpleador[0].EmailEncargado);
			    petrasp.TelefonoEncargado										= ($scope.ctrasp.datosEmpleador[0].TelefonoEncargado			== undefined ? "" : $scope.ctrasp.datosEmpleador[0].TelefonoEncargado);
			    //Tipo Trabajador
			    petrasp.MedioRecaudacionTrabajadorIdActualPostFiltro			= ($scope.ctrasp.datosEmpleador[0].MedioRecaudacionTrabajador	== undefined ? "" : $scope.ctrasp.datosEmpleador[0].MedioRecaudacionTrabajador);
			    petrasp.IdentidadRecaudadoraIdActualPostFiltro					= ($scope.ctrasp.datosEmpleador[0].IdentidadRecaudadora			== undefined ? "" : $scope.ctrasp.datosEmpleador[0].IdentidadRecaudadora);
			    petrasp.PensionadoIdActualPostFiltro							= ($scope.ctrasp.datosEmpleador[0].Pensionado					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].Pensionado);
			    petrasp.TrabajadorIdActualPostFiltro							= ($scope.ctrasp.datosEmpleador[0].Trabajador					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].Trabajador);
			    //Forma Pago
			    petrasp.Directa												= ($scope.ctrasp.datosEmpleador[0].Directa						== undefined ? "" : $scope.ctrasp.datosEmpleador[0].Directa);
			    petrasp.FormaPagoDirectaIdActualPostFiltro						= ($scope.ctrasp.datosEmpleador[0].FormaPagoDirecta				== undefined ? "" : $scope.ctrasp.datosEmpleador[0].FormaPagoDirecta);
			    petrasp.DiaDescuentoIdActual									= ($scope.ctrasp.datosEmpleador[0].DiaDescuento					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].DiaDescuento);
			    petrasp.Indirecta												= ($scope.ctrasp.datosEmpleador[0].Indirecta					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].Indirecta);
			    petrasp.FormaDePagoIndirecta									= ($scope.ctrasp.datosEmpleador[0].MedioRecaudacionIndirecta	== undefined ? "" : $scope.ctrasp.datosEmpleador[0].MedioRecaudacionIndirecta);
			    petrasp.EntidadRecaudadoraInstitucion							= ($scope.ctrasp.datosEmpleador[0].Institucion					== undefined ? "" : $scope.ctrasp.datosEmpleador[0].Institucion);
			    petrasp.kngComisionTransferencia								= ($scope.ctrasp.datosEmpleador[0].ComisionTransferencia		== undefined ? "" : $scope.ctrasp.datosEmpleador[0].ComisionTransferencia);
			    
			    //Traspaso
			    //petrasp.NombreEntidadOrigen							= $scope.ctrasp.datosEmpleador[0].NombreEntidadOrigen;
			    //petrasp.PensionadoIdActual							= $scope.ctrasp.datosEmpleador[0].PensionadoIdActual;
			    //petrasp.RutEntidadOrigen								= $scope.ctrasp.datosEmpleador[0].RutEntidadOrigen;
			    //petrasp.TrabajadorIdActual							= $scope.ctrasp.datosEmpleador[0].TrabajadorIdActual;
			    //petrasp.fondoOrigen									= $scope.ctrasp.datosEmpleador[0].fondoOrigen;
			    //petrasp.tipoAhorroIdActual							= $scope.ctrasp.datosEmpleador[0].tipoAhorroIdActual;
			    //petrasp.tipoTraspasoIdActual							= $scope.ctrasp.datosEmpleador[0].tipoTraspasoIdActual;
			    
			    //Identificación Empleador
				petrasp.datosForm.RazonSocialDeshabilitado			= ($scope.ctrasp.datosEmpleador[0].RazonSocialDesabilitado 					== undefined ? $scope.ctrasp.datosEmpleador[0].RazonSocialDesabilitado : true);
				petrasp.datosForm.DoctoIdentidadEmpDeshabilitado 	= ($scope.ctrasp.datosEmpleador[0].DocumentoIdentidadEmpleadorDesabilitado	== undefined ? $scope.ctrasp.datosEmpleador[0].DocumentoIdentidadEmpleadorDesabilitado : true);
				petrasp.datosForm.ComunaEmpDeshabilitado			= ($scope.ctrasp.datosEmpleador[0].ComunaEmpleadorDesabilitado				== undefined ? $scope.ctrasp.datosEmpleador[0].ComunaEmpleadorDesabilitado : false);
				petrasp.datosForm.DirComercialEmpDeshabilitado		= ($scope.ctrasp.datosEmpleador[0].DireccionComercialEmpleadorDesabilitado	== undefined ? $scope.ctrasp.datosEmpleador[0].DireccionComercialEmpleadorDesabilitado : true);
				petrasp.datosForm.NumeroEmpDeshabilitado			= ($scope.ctrasp.datosEmpleador[0].NumeroDireccionEmpleadorDesabilitado		== undefined ? $scope.ctrasp.datosEmpleador[0].NumeroDireccionEmpleadorDesabilitado : true);
				petrasp.datosForm.telEmpDeshabilitado				= ($scope.ctrasp.datosEmpleador[0].TelefonoEmpleadorDesabilitado			== undefined ? $scope.ctrasp.datosEmpleador[0].TelefonoEmpleadorDesabilitado : true);
				//Encargado RRHH
				petrasp.datosForm.nomEncDeshabilitado				= ($scope.ctrasp.datosEmpleador[0].NombreEncargadoDesabilitado				== undefined ? $scope.ctrasp.datosEmpleador[0].NombreEncargadoDesabilitado : true);
				petrasp.datosForm.emailEncDeshabilitado			= ($scope.ctrasp.datosEmpleador[0].EmailEncargadoDesabilitado				== undefined ? $scope.ctrasp.datosEmpleador[0].EmailEncargadoDesabilitado : true);
				petrasp.datosForm.telEncEncDeshabilitado			= ($scope.ctrasp.datosEmpleador[0].TelefonoEncargadoDesabilitado			== undefined ? $scope.ctrasp.datosEmpleador[0].TelefonoEncargadoDesabilitado : true);
				//petrasp.datosForm.habilitoMedioRecaudacion			= ($scope.ctrasp.datosEmpleador[0].IdentidadRecaudadoraIdActual		== undefined ? $scope.ctrasp.datosEmpleador[0].IdentidadRecaudadoraIdActual : true);
			}
			
			petrasp.limpiarEmpleadorHabilitado = false;
			
			petrasp.DocumentoIdentidadEmpleadorRequerido	= false;
			petrasp.RazonSocialRequerido					= false;
			petrasp.ComunaEmpleadorRequerido				= false;
			petrasp.DireccionComercialEmpleadorRequerido	= false;
			petrasp.NumeroDireccionEmpleadorRequerido		= false;
		}
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
		
	});
}());