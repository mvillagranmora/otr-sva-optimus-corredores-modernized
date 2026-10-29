/*************************************************************
 * Purpose	: Panel Persona FATCA
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaFatcaCtrl',
	function($scope,$translate,bcsComunes,$resource,servicioSociodeNegocio,bcsAlerta,bcsServicioPlataforma){
		var ppf = this;
		ppf.showBloqueo = false;
		ppf.dataPersona = {};
		ppf.dataPersonaOriginal = {};
		ppf.combo = {};
		ppf.combo.ClasificacionFatca = {};
		ppf.combo.ClasificacionEmpresa = {};
		ppf.idVentana = bcsComunes.ventanaWinId($scope);
		ppf.Habilitado = false;
		ppf.HabilitadoNACEEUUExplicacion = false;
		ppf.camposRequeridos = {};
				
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
				ppf.dataPersona.NACEEUU = 'UI_CERTIFICADO_NAC_EEUU1';
				ppf.dataPersona.NACEEUUExplicacion = '';
				ppf.HabilitadoNACEEUUExplicacion = false;
			}
		});
		
		$scope.$watch("ppf.NacEEUU.UI_CERTIFICADO_NAC_EEUU2",function(nval){
			if(nval){
				ppf.dataPersona.NACEEUU = 'UI_CERTIFICADO_NAC_EEUU2';
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
				ppf.dataPersona.EntidadFinanciera = 'UI_ACTIVIDAD_ENTIDAD_FINANCIERA1';
			}
		});
		
		$scope.$watch("ppf.EntidadFinanciera.UI_ACTIVIDAD_ENTIDAD_FINANCIERA2",function(nval){
			if(nval){
				ppf.dataPersona.EntidadFinanciera = 'UI_ACTIVIDAD_ENTIDAD_FINANCIERA2';
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
		
		$scope.$watchCollection("ppf.TipoInstitucionFinanciera", function (nval) {
			if(nval){
				for(let key in ppf.TipoInstitucionFinanciera){
					if (ppf.TipoInstitucionFinanciera[key] === true) {				
						ppf.dataPersona.TipoInstitucionFinanciera = key;
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
						ppf.dataPersona.EntidadGestionada = key;
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
						ppf.dataPersona.IngresosPasivos = key;
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
						ppf.dataPersona.GenerarIngresosPasivos = key;
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
						ppf.dataPersona.CategoriaEntidad = key;
						break;
					}
				}
			}
		});
		
		ppf.ObligacionesFiscales = {UI_BENEFICIARIOS_OBLIGACIONES1_1 : false, UI_BENEFICIARIOS_OBLIGACIONES1_2 : false};
		ppf.ObligacionesFiscales.Explicacion = '';
		
		$scope.$watch("ppf.ObligacionesFiscales.UI_BENEFICIARIOS_OBLIGACIONES1_1",function(nval){
			if(nval){
				ppf.dataPersona.ObligacionesFiscales = 'UI_BENEFICIARIOS_OBLIGACIONES1_1';
				ppf.dataPersona.ObligacionesFiscalesExplicacion = '';
			}
		});
		
		$scope.$watch("ppf.ObligacionesFiscales.UI_BENEFICIARIOS_OBLIGACIONES1_2",function(nval){
			if(nval)
				ppf.dataPersona.ObligacionesFiscales = 'UI_BENEFICIARIOS_OBLIGACIONES1_2';
		});
		
		ppf.IndividuoControl = {UI_INDIVIDUO_CONTROL1_1: false, 
				UI_INDIVIDUO_CONTROL1_2	: false,
				UI_INDIVIDUO_CONTROL1_3	: false,
				UI_INDIVIDUO_CONTROL1_4	: false,
				UI_INDIVIDUO_CONTROL1_5	: false,
				UI_INDIVIDUO_CONTROL1_6	: false,
				UI_INDIVIDUO_CONTROL1_7	: false,
				UI_INDIVIDUO_CONTROL1_8	: false,
				UI_INDIVIDUO_CONTROL1_9	: false,
				UI_INDIVIDUO_CONTROL1_10	: false,
				UI_INDIVIDUO_CONTROL1_11	: false,
				UI_INDIVIDUO_CONTROL1_12	: false,
				UI_INDIVIDUO_CONTROL1_13	: false};
				
		$scope.$watchCollection("ppf.IndividuoControl",function(nval){
			if(nval){
				for(let key in ppf.IndividuoControl){
					if(ppf.IndividuoControl[key] === true){
						ppf.dataPersona.IndividuoControl = key;
						break;
					}
				}
			}
		});
		
		//Fin Persona Juridica
		ppf.editar = function editar() {
			ppf.Habilitado = true;
			ppf.HabilitadoNACEEUUExplicacion = ppf.dataPersona.NACEEUU === 'UI_CERTIFICADO_NAC_EEUU2' ? true : false;
		};
		
		ppf.cancelar = function cancelar() {
			ppf.Habilitado = false;
			ppf.HabilitadoNACEEUUExplicacion = false;
			ppf.dataPersona = $.extend({}, ppf.dataPersonaOriginal);
			ppf.avisarCambiosOk();
		};
		
		let cargarDatosFatcaPPF = function cargarDatosFatcaPPF() {
			ppf.Habilitado = false;
			$scope.admSoc.socioNegocioPromise.then(function(){
				ppf.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
				ppf.CodigoTipoPersona = $scope.admSoc.dataSocioNegocio.CodigoTipoPersona;
				let personaFatcaDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1315",
								dataType: "json"
						    }
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppf.IdPersona) },
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
									FechaCreacion : { type: "date"},
									FechaFirmaFormulario : { type: "date"},
									FechaIngreso : { type: "date"},
									FechaModificacion : { type: "date"},
									IdClasificacionEmpresaFatca : { type: "string"},
									IdClasificacionFatca : { type: "string"},
									IdEstado : { type: "string"},
									IdPaisNacimiento : { type: "number"},
									IdPaisNacionalidadPrincipal : { type: "number"},
									IdPaisResidencia : { type: "number"},
									IdPersona : { type: "number"},
									IdUsuarioCreador : { type: "number"},
									IdUsuarioModificador :{ type: "number"},
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
				
				personaFatcaDatasource.query().then(function(result){
					ppf.dataPersona = $.extend({},personaFatcaDatasource.data()[0]);
					ppf.dataPersona.AutorizacionInformarOrganismo = ppf.dataPersona.AutorizacionInformarOrganismo === 'S' ? true : false;
					ppf.dataPersona.ExcepcionDomicilio = ppf.dataPersona.ExcepcionDomicilio === 'S' ? true : false;
					ppf.dataPersona.ExcepcionFirma = ppf.dataPersona.ExcepcionFirma === 'S' ? true : false;
					ppf.dataPersona.ExcepcionOrdenPermanente = ppf.dataPersona.ExcepcionOrdenPermanente === 'S' ? true : false;
					ppf.dataPersona.ExcepcionResidente = ppf.dataPersona.ExcepcionResidente === 'S' ? true : false;
					ppf.dataPersona.ExcepcionTelefono = ppf.dataPersona.ExcepcionTelefono === 'S' ? true : false;
					ppf.dataPersona.ExcepcionNacimiento = ppf.dataPersona.ExcepcionNacimiento === 'S' ? true : false;
					ppf.dataPersona.ParticipacionSociedadesUSA = ppf.dataPersona.ParticipacionSociedadesUSA === 'S' ? true : false;
					ppf.dataPersona.OrdenPermanente = ppf.dataPersona.OrdenPermanente === 'S' ? true : false;
					ppf.dataPersona.FechaFirmaFormulario = util.date(ppf.dataPersona.FechaFirmaFormulario).toDate(); //rev fecha
					ppf.LogCambioFatcaOptions.dataSource.filter([{ field: "IdRegistro", operator: "eq", value: Number(ppf.IdPersona)}]);
					if(ppf.dataPersona.NACEEUU !== '')
						ppf.NacEEUU[ppf.dataPersona.NACEEUU] = true;
					
					if(ppf.dataPersona.EntidadFinanciera !== '')
						ppf.EntidadFinanciera[ppf.dataPersona.EntidadFinanciera] = true;
					
					if(ppf.dataPersona.TipoInstitucionFinanciera !== '')
						ppf.TipoInstitucionFinanciera[ppf.dataPersona.TipoInstitucionFinanciera] = true;
					
					if(ppf.dataPersona.EntidadGestionada !== '')
						ppf.EntidadGestionada[ppf.dataPersona.EntidadGestionada] = true;
					
					if(ppf.dataPersona.IngresosPasivos !== '')
						ppf.IngresosPasivos[ppf.dataPersona.IngresosPasivos] = true;
					
					if(ppf.dataPersona.GenerarIngresosPasivos !== '')
						ppf.GenerarIngresosPasivos[ppf.dataPersona.GenerarIngresosPasivos] = true;
					
					if(ppf.dataPersona.CategoriaEntidad !== '')
						ppf.CategoriaEntidad[ppf.dataPersona.CategoriaEntidad] = true;
					
					if(ppf.dataPersona.ObligacionesFiscales !== '')
						ppf.ObligacionesFiscales[ppf.dataPersona.ObligacionesFiscales] = true;
					
					if(ppf.dataPersona.IndividuoControl !== '')
						ppf.IndividuoControl[ppf.dataPersona.IndividuoControl] = true;
					
					ppf.dataPersonaOriginal = $.extend({},ppf.dataPersona);
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
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700229", 
								dataType: "json"
							}
						},
						filter : { field: "IdPersona", operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdPersona)},
						schema : {
							model: {
    							fields: {					
						    		IdPersona 		 		: { type: 'number'},
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
				 {field: "PaisResidencia",title: $scope.admSoc.dataSocioNegocio.CodigoTipoPersona === 'N' ? 'UI_PAIS_RESIDENCIA' : 'UI_PAIS_CASA_MATRIZ', width: "70px" }
				,{field: "TIN",title: 'UI_TIN' ,width: "40px" }
				,{field: "CausaNoTin",title: 'UI_CAUSA_NO_TIN', width: "40px" }		
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
					pageSize 		: 5,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
							dataType: "json"
						}
					},
					filter: [
						{ field: 'IdPaisOperacion'   , operator: "ne", value: $scope.$root.SesionIdPaisSistema}, //Extranjeras.
						{ field: 'IdPersona', operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdPersona)},
						{ field: 'IdEstado' , operator: "eq", value: 'ACT'}
					],
					schema 	 : {
						model	: { fields	: {		 	
								IdCuentaBancaria			: { type: "number"},
								IdBanco						: { type: "number"},
								IdPersona					: { type: "number"},
								NombreBanco					: { type: "string"},
								IdPaisOperacion				: { type: "number"},
								NombrePais					: { type: "string"},
								NombreTipoCuentaBancaria	: { type: "string"},
								NumeroCuentaBancaria		: { type: "string"},
								IdEstado					: { type: "string"},
								NombreEstado				: { type: "string"},
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
			pageable : {
		    	refresh: true,
		       	buttonCount: 5
		    },
			autoBind			: true,  //comentar
			height				: 200,
		    selectable 			: "row"
		 };
			
		 ppf.GrillaCuentasBancariasEsFatca = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 5,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
							dataType: "json"
						}
					},
					filter: [
						{ field: 'IdPaisOperacion'   , operator: "ne", value: $scope.$root.SesionIdPaisSistema}, //Extranjeras.
						{ field: 'EsFatca'  , operator: "eq", value: 'Si'},
						{ field: 'IdPersona', operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdPersona)},
						{ field: 'IdEstado' , operator: "eq", value: 'ACT'}
					],
					schema 	 : {
						model	: { fields	: {		 	
								IdCuentaBancaria			: { type: "number"},
								IdBanco						: { type: "number"},
								IdPersona					: { type: "number"},
								NombreBanco					: { type: "string"},
								IdPaisOperacion				: { type: "number"},
								EsFatca						: { type: "string"},
								NombrePais					: { type: "string"},
								NombreTipoCuentaBancaria	: { type: "string"},
								NumeroCuentaBancaria		: { type: "string"},
								IdEstado					: { type: "string"},
								NombreEstado				: { type: "string"},
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
			pageable : {
		    	refresh: true,
		       	buttonCount: 5
		    },
			autoBind			: true,  //comentar
			height				: 200,
		    selectable 			: "row"
		 };

		 ppf.LogCambioFatcaOptions = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: false,
					pageable:true,
					pageSize: 5,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700276",
										dataType: "json"
									  }
						},
					filter: [
						{ field: "IdRegistro", operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdPersona) }
					],					
					sort 			: { field: "FechaRegistro", dir: "desc" },
					schema 			: {
						model	: { fields	: { 
							FechaRegistro			: { type: "datetime"},
							CodigoUsuario			: { type: "string"},
							ValorAntiguo 			: { type: "string"},
							ValorNuevo   			: { type: "string"},
							CampoRegistro 			: { type: "string"},
							IdRegistro				: { type: "string"}
							}
						}
					},
				}
			}),
			columns	: [{
					field: "CodigoUsuario",
					title: $translate.instant('UI_USUARIO'),
					attributes: {style:"text-align:left;"},
					width: "60px"
				},{
					field: "CampoRegistro",
					title: $translate.instant('UI_CAMPO_MODIFICADO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "ValorAntiguo",
					title: $translate.instant('UI_VALOR_ANTIGUO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "ValorNuevo",
					title: $translate.instant('UI_VALOR_NUEVO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "FechaRegistro",
					title: $translate.instant('UI_FECHA_REGISTRO'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					width: "100px"
				}],		
			sortable			: true,
			groupable			: true,
			pageable : {
                pageSize : 5
            },
			autoBind	: true,  //comentar
			height		: 140,
			selectable	: "single row",
			filterable 	: false
		};
	
		let modificarFatca = function modificarFatca(v_Dato) {
			Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=304";
		    let r = $resource ("", {}, { 'url': { method: "POST", url: Url} } );
		    let post = r.url(v_Dato);
		    return post.$promise;
	    };

		ppf.agregarEsFatca = function agregarEsFatca(){
			ppf.abrirModalCuentasExtranjeras($scope.admSoc.dataSocioNegocio.IdPersona);
		}
		ppf.abrirModalCuentasExtranjeras = function abrirModal(idPersona){
			let id1 = bcsComunes.ventanaAbrir("ModalPersonaFatcaCuentasBancariasExtranjeras",{ 
				IdPersona: idPersona , 
				CodigoFormulario: ppf.CodigoFormularioModalPaisResidencia },
				false,false,ppf.idVentana); 
		};
		ppf.eliminarEsFatca = function eliminarEsFatca(){
			var registroCambio = [];
				registroCambio.push({CampoModificado :"EsFatca", ValorNuevo : "N"});
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdPersona :ppf.IdPersona,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdCuentaBancaria: ppf.GrillaCuentasBancariasEsFatcaSeleccionado.IdCuentaBancaria,
					p_RegistroCambio: registroCambio,
				};
				bcsComunes.ventanaOverlay(ppf.idVentana,true);
				servicioSociodeNegocio.modificarPersonaCuentaBancaria(datos).$promise.then(
					function(result) {	
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Cuenta bancaria modificada",
							conflictos : util.parseServerResponse(result)}, ppf.idVentana);
						bcsComunes.ventanaPromesa(ppf.idMensaje).then(function(){
							bcsComunes.ventanaData(ppf.idVentana,true);
							// bcsComunes.ventanaCerrar(ppf.idVentana);
							bcsComunes.ventanaOverlay(ppf.idVentana,false);	
						});	
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar cuenta bancaria");
						bcsComunes.ventanaData(ppf.idVentana,true);
						bcsComunes.ventanaCerrar(ppf.idVentana);
						bcsComunes.ventanaOverlay(ppf.idVentana,false);
					}
				);
		};
		ppf.abrirModalPaisResidencia = function abrirModalPaisResidencia(idPersona, objFatcaPaisResidencia){
			let id1 = bcsComunes.ventanaAbrir("agregarModificarFatcaPaisResidencia",{ 
				IdPersona: idPersona , 
				ObjFatcaPaisResidencia: objFatcaPaisResidencia,
				CodigoFormulario: ppf.CodigoFormularioModalPaisResidencia },
				false,false,ppf.idVentana); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
					ppf.seleccionadoPaisResidencia = undefined;
					ppf.GrillaPaisResidencia.dataSource.read();
				}
	    	}); 
		};
		
		
		ppf.agregarPaisResidencia = function agregarPaisResidencia() {
			ppf.abrirModalPaisResidencia($scope.admSoc.dataSocioNegocio.IdPersona, undefined);
		};
		
		ppf.modificarPaisResidencia = function modificarPaisResidencia(objFatcaPaisResidencia) {
			ppf.abrirModalPaisResidencia($scope.admSoc.dataSocioNegocio.IdPersona, objFatcaPaisResidencia);
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
					
					lstPaisResidencia.push({ IdPersona  : ppf.seleccionadoPaisResidencia.IdPersona, IdPaisResidencia: ppf.seleccionadoPaisResidencia.IdPaisResidencia });
					
					datos = {
						p_IdEmpresa 	  		: $scope.SesionIdEmpresa,
						p_FatcaPaisResidencia  	: lstPaisResidencia
					};
					
					bcsComunes.ventanaOverlay(ppf.idVentana,true);
					servicioSociodeNegocio.EliminarFatcaPaisResidencia(datos).$promise.then(
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
	    	bcsComunes.ventanaOverlay(ppf.idVentana, true);
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
	    	
			let campos = [{ nombre: "AutorizacionInformarOrganismo" ,tipo : "bool"},
	    	                      { nombre: "ExcepcionDomicilio",tipo : "bool"},
	    	                      { nombre: "ExcepcionFirma",tipo : "bool"},
	    	                      { nombre: "ExcepcionNacimiento",tipo : "bool"},
	    	                      { nombre: "ExcepcionOrdenPermanente",tipo : "bool"},
	    	                      { nombre: "ExcepcionResidente",tipo : "bool"},
	    	                      { nombre: "ExcepcionTelefono",tipo : "bool"},
	    	                      { nombre: "ParticipacionSociedadesUSA",tipo : "bool"},
	    	                      { nombre: "OrdenPermanente",tipo : "bool"},
	    	                      { nombre: "FechaFirmaFormulario",tipo : "fechahora"},
	    	                      { nombre: "IdClasificacionEmpresaFatca",tipo : "combo"},
	    	                      { nombre: "IdClasificacionFatca",tipo : "combo"},
	    	                      { nombre: "NACEEUU",tipo: "texto"},
	    	                      { nombre: "NACEEUUExplicacion",tipo: "texto"},
	    	                      { nombre: "EntidadFinanciera",tipo: "texto"},
	    	                      { nombre: "TipoInstitucionFinanciera",tipo: "texto"},
	    	                      { nombre: "EntidadGestionada",tipo: "texto"},
	    	                      { nombre: "IngresosPasivos",tipo: "texto"},
	    	                      { nombre: "GenerarIngresosPasivos",tipo: "texto"},
	    	                      { nombre: "CategoriaEntidad",tipo: "texto"},
	    	                      { nombre: "GIINEntidad",tipo: "texto"},
	    	                      { nombre: "RazonSocial",tipo: "texto"},
	    	                      { nombre: "FechaLiquidacion",tipo: "fecha"},
	    	                      { nombre: "NombreMercado",tipo: "texto"},
	    	                      { nombre: "FechaConstitucion",tipo: "fecha"},
	    	                      { nombre: "ObligacionesFiscales",tipo: "texto"},
	    	                      { nombre: "ObligacionesFiscalesExplicacion",tipo: "texto"},
	    	                      { nombre: "IndividuoControl",tipo: "texto"}
							];		
			
			let registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (ppf.dataPersona[value.nombre] !== ppf.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppf.dataPersona[value.nombre] ? ppf.dataPersona[value.nombre] : "")});
				}
				if (value.tipo === 'combo') {
					if (!(ppf.dataPersona[value.nombre] === false && ppf.dataPersonaOriginal[value.nombre] === null) && (ppf.dataPersona[value.nombre] != ppf.dataPersonaOriginal[value.nombre]))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppf.dataPersona[value.nombre] ? ppf.dataPersona[value.nombre] : "-1")});
				}
				if (value.tipo === 'fecha') {
					if (ppf.dataPersona[value.nombre] !== ppf.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppf.dataPersona[value.nombre] ? util.dateSPdate(ppf.dataPersona[value.nombre]) : "")});
				}
				if (value.tipo === 'fechahora') {
					if (ppf.dataPersona[value.nombre] !== ppf.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppf.dataPersona[value.nombre] ? util.dateSPseconddate(ppf.dataPersona[value.nombre]) : '0001-01-01 00:00:00')});
				}
				if (value.tipo === 'bool') {
					if (ppf.dataPersona[value.nombre] !== ppf.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppf.dataPersona[value.nombre]?"S":"N")});
				}			
			});
			
	    	parametros = {};
	    	parametros.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	    	parametros.p_IdUsuario = $scope.$root.SesionIdUsuario;
	    	parametros.p_IdModulo = 'SOC';
	    	parametros.p_UsaReglasNegocio = 'N';
	    	parametros.p_IdPersona = ppf.IdPersona;
	    	parametros.p_RegistroCambio = registroCambio;
	    	
	    	if (parametros.p_RegistroCambio.length===0) {
	    		bcsComunes.msg("warning","Advertencia","No hay modificaciones para guardar.");
	    		bcsComunes.ventanaOverlay(ppf.idVentana, false);
	    		cargarDatosFatcaPPF();
	    		return;
	    	}
	    	
	    	modificarFatca(parametros).then(
				function(result) {
					bcsComunes.ventanaOverlay(ppf.idVentana, false);
					var titulo = (result)? "Cambio de estado realizado." : "La operacion tiene errores";

					var idMensaje = bcsComunes.ventanaMsg({ mensaje : titulo,
						conflictos : util.parseServerResponse(result)}, ppf.idVentana);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						ppf.avisarCambiosOk();
						cargarDatosFatcaPPF();
						$scope.admSoc.actualizarTab.PERSONA["DatosGenerales"] = true;
					});
				},
				function(result){
					bcsComunes.ventanaOverlay(ppf.idVentana, false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
			);
		};
		cargarDatosFatcaPPF();

		ppf.obtenerDatosComparables = function (data) {
			return {
				AutorizacionInformarOrganismo: data.AutorizacionInformarOrganismo ? data.AutorizacionInformarOrganismo : '' ,
				ExcepcionDomicilio: data.ExcepcionDomicilio ? data.ExcepcionDomicilio : '' ,
				ExcepcionFirma: data.ExcepcionFirma ? data.ExcepcionFirma : '' ,
				ExcepcionNacimiento: data.ExcepcionNacimiento ? data.ExcepcionNacimiento : '' ,
				ExcepcionOrdenPermanente: data.ExcepcionOrdenPermanente ? data.ExcepcionOrdenPermanente : '' ,
				ExcepcionResidente: data.ExcepcionResidente ? data.ExcepcionResidente : '' ,
				ExcepcionTelefono: data.ExcepcionTelefono ? data.ExcepcionTelefono : '' ,
				ParticipacionSociedadesUSA: data.ParticipacionSociedadesUSA ? data.ParticipacionSociedadesUSA : '' ,
				OrdenPermanente: data.OrdenPermanente ? data.OrdenPermanente : '' ,
				FechaFirmaFormulario: data.FechaFirmaFormulario ? data.FechaFirmaFormulario : '' ,
				IdClasificacionEmpresaFatca: data.IdClasificacionEmpresaFatca ? data.IdClasificacionEmpresaFatca : '' ,
				IdClasificacionFatca: data.IdClasificacionFatca ? data.IdClasificacionFatca : '' ,
				NACEEUU: data.NACEEUU ? data.NACEEUU : '' ,
				NACEEUUExplicacion: data.NACEEUUExplicacion ? data.NACEEUUExplicacion : '' ,
				EntidadFinanciera: data.EntidadFinanciera ? data.EntidadFinanciera : '',
				GIINEntidad: data.GIINEntidad ? data.GIINEntidad : '' ,
				RazonSocial: data.RazonSocial ? data.RazonSocial : '' ,
				FechaLiquidacion: data.FechaLiquidacion ? data.FechaLiquidacion : '' ,
				NombreMercado: data.NombreMercado ? data.NombreMercado : '' ,
				FechaConstitucion: data.FechaConstitucion ? data.FechaConstitucion : '' ,
				ObligacionesFiscales: data.ObligacionesFiscales ? data.ObligacionesFiscales : '' ,
				ObligacionesFiscalesExplicacion: data.ObligacionesFiscalesExplicacion ? data.ObligacionesFiscalesExplicacion : '',
				TipoInstitucionFinanciera: data.TipoInstitucionFinanciera && data.TipoInstitucionFinanciera!=='undefined' ? data.TipoInstitucionFinanciera : '' ,
				EntidadGestionada:data.EntidadGestionada && data.EntidadGestionada!=='undefined' ?  data.EntidadGestionada : '' ,
				IngresosPasivos: data.IngresosPasivos && data.IngresosPasivos!=='undefined' ?  data.IngresosPasivos : '' ,
				GenerarIngresosPasivos: data.GenerarIngresosPasivos && data.GenerarIngresosPasivos!=='undefined' ?  data.GenerarIngresosPasivos : '' ,
				CategoriaEntidad: data.CategoriaEntidad && data.CategoriaEntidad !== 'undefined' ? data.CategoriaEntidad : '',
				IndividuoControl: data.IndividuoControl && data.IndividuoControl!=='undefined' ?  data.IndividuoControl : '' 
			};
		}

		ppf.avisarCambioPanel = function () {
			const dataOriginal = ppf.obtenerDatosComparables(ppf.dataPersonaOriginal);
			const data = ppf.obtenerDatosComparables(ppf.dataPersona);
			
			const sonIguales = _.isEqual(data, dataOriginal);
			if (sonIguales) {
				ppf.avisarCambiosOk();
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaFatca' });
			}
		}
		ppf.avisarCambiosOk = function() {
			$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaFatca' });	
		}

		$scope.$watchCollection('ppf.dataPersona', function (newv) {
			if (ppf.Habilitado) {
				ppf.avisarCambioPanel();
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------