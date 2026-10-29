(function() {
	angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPersonaFatcaEnrolCtrl',
	function($scope,$translate,bcsComunes,servicioSociodeNegocio,bcsAlerta,bcsServicioPlataforma){
		let ppf = this;
		ppf.showBloqueo = false;
		ppf.dataEnrolamiento = {};
		ppf.dataEnrolamientoOriginal = {};
		ppf.combo = {};
		ppf.combo.ClasificacionFatca = {};
		ppf.combo.ClasificacionEmpresa = {};
		ppf.idVentana = bcsComunes.ventanaWinId($scope);
		ppf.Habilitado = false;
		ppf.HabilitadoNACEEUUExplicacion = false;
		ppf.camposRequeridos = {};
		ppf.datosCargados = false;

		$scope.admSoc.socioNegocioPromise.then(function () {
			ppf.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['FATCA'];
			ppf.CodigoFormularioModalPaisResidencia = $scope.admSoc.dataSocioNegocio.Matriz['FATCA_PAIS_RESIDENCIA'];
			bcsServicioPlataforma.CamposRequeridosFormulario(ppf.CodigoFormulario, function(respuesta) {
				ppf.camposRequeridos = respuesta;
			});
		});
		//Persona Natural
		ppf.NacEEUU = {UI_CERTIFICADO_NAC_EEUU1 : false, UI_CERTIFICADO_NAC_EEUU2 : false};
		ppf.NacEEUU.Explicacion = '';
		
		$scope.$watch("ppf.NacEEUU.UI_CERTIFICADO_NAC_EEUU1",function(nval){
			if(nval){
				ppf.dataEnrolamiento.NACEEUU = 'UI_CERTIFICADO_NAC_EEUU1';
				ppf.dataEnrolamiento.NACEEUUExplicacion = '';
				ppf.HabilitadoNACEEUUExplicacion = false;
			}
		});
		
		$scope.$watch("ppf.NacEEUU.UI_CERTIFICADO_NAC_EEUU2",function(nval){
			if(nval){
				ppf.dataEnrolamiento.NACEEUU = 'UI_CERTIFICADO_NAC_EEUU2';
				ppf.HabilitadoNACEEUUExplicacion = true;
			}else{
				ppf.HabilitadoNACEEUUExplicacion = false;
			}
		});

		$scope.$watch("admSoc.actualizarTab.PERSONA.Fatca",function(nval){
		//evalua si debe actualizar los datos de la pestana por cambios en otra pestana
			if(nval){
				cargarDatosFatcaPPF();
				$scope.admSoc.actualizarTab.PERSONA["Fatca"] = false;
			}
		});
		//Fin Persona Natural
		
		//Persona Juridica
		ppf.EntidadFinanciera = {UI_ACTIVIDAD_ENTIDAD_FINANCIERA1 : false, UI_ACTIVIDAD_ENTIDAD_FINANCIERA2 : false};
		
		$scope.$watch("ppf.EntidadFinanciera.UI_ACTIVIDAD_ENTIDAD_FINANCIERA1",function(nval){
			if(nval){
				ppf.dataEnrolamiento.EntidadFinanciera = 'UI_ACTIVIDAD_ENTIDAD_FINANCIERA1';
			}
		});
		
		$scope.$watch("ppf.EntidadFinanciera.UI_ACTIVIDAD_ENTIDAD_FINANCIERA2",function(nval){
			if(nval){
				ppf.dataEnrolamiento.EntidadFinanciera = 'UI_ACTIVIDAD_ENTIDAD_FINANCIERA2';
			}
		});
		
		ppf.TipoInstitucionFinanciera = {UI_TIPO_INST_FINANCIERA1: false, 
				UI_TIPO_INST_FINANCIERA2	: false,
				UI_TIPO_INST_FINANCIERA3	: false,
				UI_TIPO_INST_FINANCIERA4	: false,
				UI_TIPO_INST_FINANCIERA5	: false,
				UI_TIPO_INST_FINANCIERA6	: false,
				UI_TIPO_INST_FINANCIERA7	: false,
				UI_TIPO_INST_FINANCIERA8	: false,
				UI_TIPO_INST_FINANCIERA9	: false,
				UI_TIPO_INST_FINANCIERA10	: false,
				UI_TIPO_INST_FINANCIERA11	: false,
				UI_TIPO_INST_FINANCIERA12	: false,
				UI_TIPO_INST_FINANCIERA13	: false,
				UI_TIPO_INST_FINANCIERA14	: false,
				UI_TIPO_INST_FINANCIERA15	: false,
				UI_TIPO_INST_FINANCIERA16	: false,
				UI_TIPO_INST_FINANCIERA17	: false,
				UI_TIPO_INST_FINANCIERA18	: false};
		
		$scope.$watchCollection("ppf.TipoInstitucionFinanciera",function(nval){
			if(nval){
				for(let key in ppf.TipoInstitucionFinanciera){
					if(ppf.TipoInstitucionFinanciera[key] === true){
						ppf.dataEnrolamiento.TipoInstitucionFinanciera = key;
						break;
					}
				}
			}
		});
		
		ppf.EntidadGestionada = {UI_TIPO_INST_FINANCIERA19_1: false, 
				UI_TIPO_INST_FINANCIERA19_2	: false};
		$scope.$watchCollection("ppf.EntidadGestionada",function(nval){
			if(nval){
				for(let key in ppf.EntidadGestionada){
					if(ppf.EntidadGestionada[key] === true){
						ppf.dataEnrolamiento.EntidadGestionada = key;
						break;
					}
				}
			}
		});
		
		ppf.IngresosPasivos = {UI_OTRAS_INSTITUCIONES1_1: false, 
				UI_OTRAS_INSTITUCIONES1_2	: false};
		
		$scope.$watchCollection("ppf.IngresosPasivos",function(nval){
			if(nval){
				for(let key in ppf.IngresosPasivos){
					if(ppf.IngresosPasivos[key] === true){
						ppf.dataEnrolamiento.IngresosPasivos = key;
						break;
					}
				}
			}
		});
		
		ppf.GenerarIngresosPasivos = {UI_OTRAS_INSTITUCIONES2_1: false, 
				UI_OTRAS_INSTITUCIONES2_2	: false};
		
		$scope.$watchCollection("ppf.GenerarIngresosPasivos",function(nval){
			if(nval){
				for(let key in ppf.GenerarIngresosPasivos){
					if(ppf.GenerarIngresosPasivos[key] === true){
						ppf.dataEnrolamiento.GenerarIngresosPasivos = key;
						break;
					}
				}
			}
		});
		
		ppf.CategoriaEntidad = {UI_OTRAS_INSTITUCIONES3_1: false, 
				UI_OTRAS_INSTITUCIONES3_2	: false,
				UI_OTRAS_INSTITUCIONES3_3	: false,
				UI_OTRAS_INSTITUCIONES3_4	: false,
				UI_OTRAS_INSTITUCIONES3_5	: false,
				UI_OTRAS_INSTITUCIONES3_6	: false,
				UI_OTRAS_INSTITUCIONES3_7	: false,
				UI_OTRAS_INSTITUCIONES3_8	: false,
				UI_OTRAS_INSTITUCIONES3_9	: false,
				UI_OTRAS_INSTITUCIONES3_10	: false,
				UI_OTRAS_INSTITUCIONES3_11	: false,
				UI_OTRAS_INSTITUCIONES3_12	: false,
				UI_OTRAS_INSTITUCIONES3_13	: false};
		
		$scope.$watchCollection("ppf.CategoriaEntidad",function(nval){
			if(nval){
				for(let key in ppf.CategoriaEntidad){
					if(ppf.CategoriaEntidad[key] === true){
						ppf.dataEnrolamiento.CategoriaEntidad = key;
						break;
					}
				}
			}
		});
		
		ppf.ObligacionesFiscales = {UI_BENEFICIARIOS_OBLIGACIONES1_1 : false, UI_BENEFICIARIOS_OBLIGACIONES1_2 : false};
		ppf.ObligacionesFiscales.Explicacion = '';
		
		$scope.$watch("ppf.ObligacionesFiscales.UI_BENEFICIARIOS_OBLIGACIONES1_1",function(nval){
			if(nval){
				ppf.dataEnrolamiento.ObligacionesFiscales = 'UI_BENEFICIARIOS_OBLIGACIONES1_1';
				ppf.dataEnrolamiento.ObligacionesFiscalesExplicacion = '';
			}
		});
		
		$scope.$watch("ppf.ObligacionesFiscales.UI_BENEFICIARIOS_OBLIGACIONES1_2",function(nval){
			if(nval)
				ppf.dataEnrolamiento.ObligacionesFiscales = 'UI_BENEFICIARIOS_OBLIGACIONES1_2';
		});
		
		ppf.IndividuoControl = {UI_INDIVIDUO_CONTROL1_1: false, 
				UI_INDIVIDUO_CONTROL1_2		: false,
				UI_INDIVIDUO_CONTROL1_3		: false,
				UI_INDIVIDUO_CONTROL1_4		: false,
				UI_INDIVIDUO_CONTROL1_5		: false,
				UI_INDIVIDUO_CONTROL1_6		: false,
				UI_INDIVIDUO_CONTROL1_7		: false,
				UI_INDIVIDUO_CONTROL1_8		: false,
				UI_INDIVIDUO_CONTROL1_9		: false,
				UI_INDIVIDUO_CONTROL1_10	: false,
				UI_INDIVIDUO_CONTROL1_11	: false,
				UI_INDIVIDUO_CONTROL1_12	: false,
				UI_INDIVIDUO_CONTROL1_13	: false};
		
		$scope.$watchCollection("ppf.IndividuoControl",function(nval){
			if(nval){
				for(let key in ppf.IndividuoControl){
					if(ppf.IndividuoControl[key] === true){
						ppf.dataEnrolamiento.IndividuoControl = key;
						break;
					}
				}
			}
		});
		//Fin Persona Juridica
		
		ppf.editar = function editar() {
			ppf.Habilitado = true;
			ppf.HabilitadoNACEEUUExplicacion = ppf.dataEnrolamiento.NACEEUU === 'UI_CERTIFICADO_NAC_EEUU2' ? true : false;
		};
		
		ppf.cancelar = function cancelar() {
			ppf.Habilitado = false;
			ppf.HabilitadoNACEEUUExplicacion = false;
			ppf.dataEnrolamiento = $.extend({},ppf.dataEnrolamientoOriginal);
		};
		
		let cargarDatosFatcaPPF = function cargarDatosFatcaPPF() {
			ppf.Habilitado = false;
			$scope.admSoc.socioNegocioPromise.then(function(){
				ppf.IdEnrolamiento = $scope.admSoc.dataSocioNegocio.IdEnrolamiento;
				ppf.CodigoTipoPersona = $scope.admSoc.dataSocioNegocio.CodigoTipoPersona;
				let enrolamientoFatcaDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700258",
								dataType: "json"
						    }
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(ppf.IdEnrolamiento) },
						schema: {
							model: {
								fields : {
									AutorizacionInformarOrganismo :	{ type: "string"},
									ExcepcionDomicilio : { type: "string"},
									ExcepcionFirma : { type: "string"},
									ExcepcionNacimiento : { type: "string"},
									ExcepcionOrdenPermanente : { type: "string"},
									ExcepcionResidente : { type: "string"},
									ExcepcionTelefono : { type: "string"},
									ParticipacionSociedadesUSA : { type: "string"},
									FechaFirmaFormulario : { type: "date"},
									IdClasificacionEmpresaFatca : { type: "string"},
									IdClasificacionFatca : { type: "string"},
									IdEstado : { type: "string"},
									IdPaisNacimiento : { type: "number"},
									IdPaisNacionalidadPrincipal : { type: "number"},
									IdPaisResidencia : { type: "number"},
									IdEnrolamiento : { type: "number"},
									NombreClasificacionEmpresaFatca : { type: "string"},
									NombreClasificacionFatca : { type: "string"},
									NombreEstado : { type: "string"},
									NumeroGIIN :{ type: "string"},
									NACEEUU: { type: "string"},
									NACEEUUExplicacion: { type: "string"},
									EntidadFinanciera: { type: "string"},
									TipoInstitucionFinanciera: { type: "string"},
									EntidadGestionada: { type: "string"},
									IngresosPasivos: { type: "string"},
									GenerarIngresosPasivos: { type: "string"},
									CategoriaEntidad: { type: "string"},
									GIINEntidad: { type: "string"},
									RazonSocial: { type: "string"},
									FechaLiquidacion: { type: "date"},
									NombreMercado: { type: "string"},
									FechaConstitucion: { type: "date"},
									ObligacionesFiscales: { type: "string"},
									ObligacionesFiscalesExplicacion: { type: "string"},
									IndividuoControl: { type: "string"},
									OrdenPermanente: { type: "string"}
								}
							}
						}
					}
				});
				
				enrolamientoFatcaDatasource.query().then(function(){
					ppf.dataEnrolamiento = $.extend({},enrolamientoFatcaDatasource.data()[0]);
					ppf.dataEnrolamiento.AutorizacionInformarOrganismo = ppf.dataEnrolamiento.AutorizacionInformarOrganismo === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionDomicilio = ppf.dataEnrolamiento.ExcepcionDomicilio === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionFirma = ppf.dataEnrolamiento.ExcepcionFirma === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionOrdenPermanente = ppf.dataEnrolamiento.ExcepcionOrdenPermanente === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionResidente = ppf.dataEnrolamiento.ExcepcionResidente === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionTelefono = ppf.dataEnrolamiento.ExcepcionTelefono === 'S' ? true:false;
					ppf.dataEnrolamiento.ExcepcionNacimiento = ppf.dataEnrolamiento.ExcepcionNacimiento === 'S' ? true:false;
					ppf.dataEnrolamiento.ParticipacionSociedadesUSA = ppf.dataEnrolamiento.ParticipacionSociedadesUSA === 'S' ? true:false;
					ppf.dataEnrolamiento.OrdenPermanente = ppf.dataEnrolamiento.OrdenPermanente === 'S' ? true:false;
					ppf.dataEnrolamiento.FechaFirmaFormulario = util.date(ppf.dataEnrolamiento.FechaFirmaFormulario).toDate(); 
					ppf.dataEnrolamiento.FechaConstitucion = util.date(ppf.dataEnrolamiento.FechaConstitucion).toDate(); 
					ppf.dataEnrolamiento.FechaLiquidacion = util.date(ppf.dataEnrolamiento.FechaLiquidacion).toDate(); 
					
					if(ppf.dataEnrolamiento.NACEEUU !== '')
						ppf.NacEEUU[ppf.dataEnrolamiento.NACEEUU] = true;
					
					if(ppf.dataEnrolamiento.EntidadFinanciera !== '')
						ppf.EntidadFinanciera[ppf.dataEnrolamiento.EntidadFinanciera] = true;
					
					if(ppf.dataEnrolamiento.TipoInstitucionFinanciera !== '')
						ppf.TipoInstitucionFinanciera[ppf.dataEnrolamiento.TipoInstitucionFinanciera] = true;
					
					if(ppf.dataEnrolamiento.EntidadGestionada !== '')
						ppf.EntidadGestionada[ppf.dataEnrolamiento.EntidadGestionada] = true;
					
					if(ppf.dataEnrolamiento.IngresosPasivos !== '')
						ppf.IngresosPasivos[ppf.dataEnrolamiento.IngresosPasivos] = true;
					
					if(ppf.dataEnrolamiento.GenerarIngresosPasivos !== '')
						ppf.GenerarIngresosPasivos[ppf.dataEnrolamiento.GenerarIngresosPasivos] = true;
					
					if(ppf.dataEnrolamiento.CategoriaEntidad !== '')
						ppf.CategoriaEntidad[ppf.dataEnrolamiento.CategoriaEntidad] = true;
					
					if(ppf.dataEnrolamiento.ObligacionesFiscales !== '')
						ppf.ObligacionesFiscales[ppf.dataEnrolamiento.ObligacionesFiscales] = true;
					
					if(ppf.dataEnrolamiento.IndividuoControl !== '')
						ppf.IndividuoControl[ppf.dataEnrolamiento.IndividuoControl] = true;
					
					ppf.dataEnrolamientoOriginal = $.extend({},ppf.dataEnrolamiento);
					ppf.datosCargados = true;
				});
			});
		};
		
		// GRILLA PAIS RESIDENCIA
		ppf.GrillaPaisResidencia = {
			dataSource : new bcs.DataSource( {
			type 		: "odata",
			definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
						pageSize 			: 10,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700259", 
								dataType: "json"
							}
						},
						filter : { field: "IdEnrolamiento", operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdEnrolamiento)},
						schema : {
							model: {
    							fields: {					
						    		IdEnrolamiento   		: { type: 'number'},
									IdPaisResidencia 		: { type: 'number'},
									PaisResidencia   		: { type: 'string'},
									TIN				 		: { type: 'string'},
									IdCausaNoTin 	 		: { type: 'string'},
									CausaNoTin 		 		: { type: 'string'},
									PaisResidenciaPrincipal : { type: 'string'}
    							} 
        					}
						}
					}
			}),
			columns :[
				 {field: "PaisResidencia",title: $scope.admSoc.dataSocioNegocio.CodigoTipoPersona === 'N' ? 'UI_PAIS_RESIDENCIA' : 'UI_PAIS_CASA_MATRIZ', width: "70px" },
				 {field: "TIN",title: 'UI_TIN' ,width: "40px" },
				 {field: "CausaNoTin",title: 'UI_CAUSA_NO_TIN', width: "40px" }		
			],
			pageable : {
		    	refresh: true,
		       	buttonCount: 5
		    },
			height : 120,
			selectable : "single row",
			sortable : true,
			filterable : false,
			groupable : false,
			autoBind: true
		};
		
		ppf.GrillaCuentasBancarias = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700212",
							dataType: "json"
						}
					},
					filter: [
						{ field: 'IdPaisOperacion'  , operator: "ne", value: Number($scope.$root.SesionIdPaisSistema) }, //Extranjeras.
						{ field: 'IdEnrolamiento', operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdEnrolamiento)},
					],
					schema 	 : {
						model	: { fields	: {		 	
								IdCuentaBancaria			: { type: "number"},
								IdBanco						: { type: "number"},
								IdEnrolamiento				: { type: "number"},
								NombreBanco					: { type: "string"},
								IdPaisOperacion				: { type: "number"},
								NombrePais					: { type: "string"},
								NombreTipoCuentaBancaria	: { type: "string"},
								NumeroCuentaBancaria		: { type: "string"},
								NombreMoneda				: { type: "string"},
								// TODO: ORDENES PERMANENTES PENDIENTE.
								OrdenesPendientes    		: { type: "string"},
							} 
						}
					}
				}
			}),
			columns			: [
			    {field: "NombreTipoCuentaBancaria"	,width: "150px",title: $translate.instant("UI_TIPO_CUENTA")							,attributes : {style : "text-align:left;"}},
				{field: "NumeroCuentaBancaria"		,width: "150px",title: $translate.instant("UI_NUMERO_CUENTA")						,attributes : {style : "text-align:left;"}},		
				{field: "NombreBanco"				,width: "150px",title: $translate.instant("UI_BANCO")								,attributes : {style : "text-align:left;"}},
				{field: "NombrePais"				,width: "150px",title: $translate.instant("UI_PAIS")								,attributes : {style : "text-align:left;"}},
				{field: "OrdenesPendientes"			,width: "150px",title: $translate.instant("UI_ORDENES_PERMANENTES")					,attributes : {style : "text-align:left;"}},
			],		
			sortable			: true,
			filterable			: false,
			groupable			: false,
			pageable			: true,
			autoBind			: true,  
			height				: 200,
		    selectable 			: "row"
		 };
			

		ppf.abrirModalPaisResidencia = function abrirModalPaisResidencia(idEnrolamiento, objFatcaPaisResidencia){
			let id1 = bcsComunes.ventanaAbrir("agregarModificarEnrolamientoFatcaPaisResidencia",{ IdEnrolamiento: idEnrolamiento , ObjFatcaPaisResidencia: objFatcaPaisResidencia, CodigoFormulario: ppf.CodigoFormularioModalPaisResidencia },false,false,ppf.idVentana); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					ppf.seleccionadoPaisResidencia = undefined;
					ppf.GrillaPaisResidencia.dataSource.read();
				}
	    	}); 
		};
			
		ppf.agregarPaisResidencia = function agregarPaisResidencia() {
			ppf.abrirModalPaisResidencia($scope.admSoc.dataSocioNegocio.IdEnrolamiento, undefined);
		};
		
		ppf.modificarPaisResidencia = function modificarPaisResidencia(objFatcaPaisResidencia) {
			ppf.abrirModalPaisResidencia($scope.admSoc.dataSocioNegocio.IdEnrolamiento, objFatcaPaisResidencia);
		};

		ppf.eliminarPaisResidencia = function eliminarPaisResidencia() {
			if (ppf.seleccionadoPaisResidencia.length === 0) {
				bcsComunes.msg("error",$translate.instant("UI_INFORMACION"),$translate.instant('UI_PAIS_RESIDENCIA_NO_SELECCIONADO'));
				return;
			}
			let confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", "¿Está seguro que desea eliminar el País de Residencia seleccionado?", ppf.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){				
					let lstPaisResidencia = [];
					
					lstPaisResidencia.push({ IdEnrolamiento  : ppf.seleccionadoPaisResidencia.IdEnrolamiento, IdPaisResidencia: ppf.seleccionadoPaisResidencia.IdPaisResidencia });
					
					datos = {
						p_IdEmpresa 	  		: $scope.SesionIdEmpresa,
						p_FatcaPaisResidencia  	: lstPaisResidencia
					};
					
					bcsComunes.ventanaOverlay(ppf.idVentana,true);
					servicioSociodeNegocio.eliminarBorradorFatcaPaisResidencia(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(ppf.idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.ventanaMsg({ 
								mensaje : $translate.instant("UI_ERROR_ELIMINAR_PAIS_RESIDENCIA"),
								conflictos : util.parseServerResponse(result)}, ppf.idVentana);				
							return;
						}
						
						let idMensaje = bcsComunes.ventanaMsg({ 
							mensaje : $translate.instant("UI_OK_ELIMINAR_PAIS_RESIDENCIA"),
							conflictos : util.parseServerResponse(result)}, ppf.idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(ppf.idVentana,true);
							bcsComunes.ventanaOverlay(ppf.idVentana,false);	
							ppf.GrillaPaisResidencia.dataSource.read();
						});
					},
					function(){
						bcsComunes.msg("error","error", $translate.instant("UI_ERROR_ELIMINAR_PAIS_RESIDENCIA"));
						bcsComunes.ventanaData(ppf.idVentana,true);
						bcsComunes.ventanaOverlay(ppf.idVentana,false);
					});		
				}
			});
		};
		
	    ppf.guardar = function guardar() {
			let datosSinTin = 0;
			
			$.each(ppf.GrillaPaisResidencia.dataSource.data(),function(key,value){
				if ((value.TIN === "" || value.TIN === null) && (value.IdCausaNoTin  === "" || value.IdCausaNoTin  === null)) {
					datosSinTin++;
				}
			});
			
			if(datosSinTin > 0){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_PAISES_SIN_CAUSA_NO_TIN'));  
				bcsComunes.ventanaOverlay(ppf.idVentana, false);
	    		return;
			}
	    	
			let objFatca = [];
			objFatca.push({
				IdInterno : 0,
				IdEnrolamiento : $scope.admSoc.dataSocioNegocio.IdEnrolamiento,
				IdClasificacionFatca : ppf.dataEnrolamiento.IdClasificacionFatca || 0,
				IdClasificacionEmpresaFatca : ppf.dataEnrolamiento.IdClasificacionEmpresaFatca || 0,
				NumeroGIIN : '',
				FechaFirmaFormulario : (ppf.dataEnrolamiento && ppf.dataEnrolamiento.FechaFirmaFormulario instanceof Date) ? util.dateSPdate(ppf.dataEnrolamiento.FechaFirmaFormulario) : '0001-01-01 00:00:00',
				AutorizacionInformarOrganismo : ppf.dataEnrolamiento.AutorizacionInformarOrganismo === true ? 'S' : 'N',
				ExcepcionResidente : ppf.dataEnrolamiento.ExcepcionResidente ? 'S' : 'N',
				ExcepcionNacimiento : ppf.dataEnrolamiento.ExcepcionNacimiento ? 'S' : 'N',
				ExcepcionDomicilio : ppf.dataEnrolamiento.ExcepcionDomicilio ? 'S' : 'N',
				ExcepcionTelefono : ppf.dataEnrolamiento.ExcepcionTelefono ? 'S' : 'N',
				ExcepcionOrdenPermanente : ppf.dataEnrolamiento.ExcepcionOrdenPermanente ? 'S' : 'N',
				ExcepcionFirma : ppf.dataEnrolamiento.ExcepcionFirma ? 'S' : 'N',
				ParticipacionSociedadesUSA : ppf.dataEnrolamiento.ParticipacionSociedadesUSA ? 'S' : 'N',
				OrdenPermanente : ppf.dataEnrolamiento.OrdenPermanente ? 'S' : 'N',
				NACEEUU : ppf.dataEnrolamiento.NACEEUU || '',
				NACEEUUExplicacion: ppf.dataEnrolamiento.NACEEUUExplicacion || '',
				EntidadFinanciera : ppf.dataEnrolamiento.EntidadFinanciera || '',
				TipoInstitucionFinanciera : ppf.dataEnrolamiento.TipoInstitucionFinanciera || '',
				EntidadGestionada : ppf.dataEnrolamiento.EntidadGestionada || '',
				IngresosPasivos : ppf.dataEnrolamiento.IngresosPasivos || '',
				GenerarIngresosPasivos : ppf.dataEnrolamiento.GenerarIngresosPasivos || '',
				CategoriaEntidad : ppf.dataEnrolamiento.CategoriaEntidad || '',
				GIINEntidad : ppf.dataEnrolamiento.GIINEntidad || '',
				RazonSocial : ppf.dataEnrolamiento.RazonSocial || '',
				FechaLiquidacion :  (ppf.dataEnrolamiento && ppf.dataEnrolamiento.FechaLiquidacion instanceof Date) ? util.dateSPdate(ppf.dataEnrolamiento.FechaLiquidacion) : '0001-01-01',
				NombreMercado : ppf.dataEnrolamiento.NombreMercado || '', 
				FechaConstitucion  : (ppf.dataEnrolamiento && ppf.dataEnrolamiento.FechaConstitucion instanceof Date) ? util.dateSPdate(ppf.dataEnrolamiento.FechaConstitucion) : '0001-01-01',
				ObligacionesFiscales : ppf.dataEnrolamiento.ObligacionesFiscales || '', 
				ObligacionesFiscalesExplicacion : ppf.dataEnrolamiento.ObligacionesFiscalesExplicacion || '', 
				IndividuoControl : ppf.dataEnrolamiento.IndividuoControl || ''
			});

			let datos = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'SOC',
				p_UsaReglasNegocio: 'S',
				p_CodigoFormulario: ppf.CodigoFormulario,
				p_IdTipoCanal: 'OPT',
				p_Fatcas : objFatca
			};
			
			bcsComunes.ventanaOverlay(ppf.idVentana,true);
			servicioSociodeNegocio.modificarBorradorFatca(datos).$promise.then(
				function(result) {				
					cargarDatosFatcaPPF();
					bcsComunes.ventanaOverlay(ppf.idVentana,false);

					bcsComunes.ventanaMsg({ mensaje : "Los datos de fatca fueron modificados, operacion: " + ppf.IdEnrolamiento ,
						conflictos : util.parseServerResponse(result)}, ppf.idVentana);
				},
				function(result){
					cargarDatosFatcaPPF();
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppf.idVentana,false);
			});
		};

		ppf.obtenerDatosComparables = function (data) {
			return {
				IdClasificacionFatca : data.IdClasificacionFatca || 0,
				IdClasificacionEmpresaFatca : data.IdClasificacionEmpresaFatca || 0,
				FechaFirmaFormulario :  data.FechaFirmaFormulario || '0001-01-01 00:00:00',
				AutorizacionInformarOrganismo : data.AutorizacionInformarOrganismo === true ? 'S' : 'N',
				ExcepcionResidente : data.ExcepcionResidente ? 'S' : 'N',
				ExcepcionNacimiento : data.ExcepcionNacimiento ? 'S' : 'N',
				ExcepcionDomicilio : data.ExcepcionDomicilio ? 'S' : 'N',
				ExcepcionTelefono : data.ExcepcionTelefono ? 'S' : 'N',
				ExcepcionOrdenPermanente : data.ExcepcionOrdenPermanente ? 'S' : 'N',
				ExcepcionFirma : data.ExcepcionFirma ? 'S' : 'N',
				ParticipacionSociedadesUSA : data.ParticipacionSociedadesUSA ? 'S' : 'N',
				OrdenPermanente : data.OrdenPermanente ? 'S' : 'N',
				NACEEUU : data.NACEEUU || '',
				NACEEUUExplicacion: data.NACEEUUExplicacion || '',
				EntidadFinanciera : data.EntidadFinanciera || '',
				TipoInstitucionFinanciera : data.TipoInstitucionFinanciera || '',
				EntidadGestionada : data.EntidadGestionada || '',
				IngresosPasivos : data.IngresosPasivos || '',
				GenerarIngresosPasivos : data.GenerarIngresosPasivos || '',
				CategoriaEntidad : data.CategoriaEntidad || '',
				GIINEntidad : data.GIINEntidad || '',
				RazonSocial : data.RazonSocial || '',
				FechaLiquidacion :  data.FechaLiquidacion  || '0001-01-01',
				NombreMercado : data.NombreMercado || '', 
				FechaConstitucion  : data.FechaConstitucion || '0001-01-01',
				ObligacionesFiscales : data.ObligacionesFiscales || '', 
				ObligacionesFiscalesExplicacion : data.ObligacionesFiscalesExplicacion || '', 
				IndividuoControl : data.IndividuoControl || ''
			}
		}

		ppf.datosPorGuardar = function() {
			const dataOriginal = ppf.obtenerDatosComparables(ppf.dataEnrolamientoOriginal);
			const data = ppf.obtenerDatosComparables(ppf.dataEnrolamiento);
			const sonIguales = _.isEqual(data, dataOriginal);
			
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaFatca' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaFatca' });
			}		
		}
		
		$scope.$watchCollection('ppf.dataEnrolamiento', function() {
			console.log('asjdkasjd', $scope.formPanelPersonaFatca.$error)
			if(ppf.datosCargados){
				ppf.datosPorGuardar();
			}
		});
		cargarDatosFatcaPPF();
	});
}());
//------------------------- FIN CONTROLLER --------------------------