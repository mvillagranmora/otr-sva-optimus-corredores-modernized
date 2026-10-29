angular
		.module('ModuloPlataforma')
		.directive(
				'bcsGridAtributos',
				function($translate, bcsComunes, bcsServicioPlataforma,
						$resource) {
					
					return {
						restrict : 'E',
						replace : false,
						scope : {
							kOptions : '=?',
							// ngModel : '=?',
							kNgModel : '=?',
							idEntidad : '=?',
							idTipoEntidad : '=?',
							bcsHabilitado : '=?', /* activa/desactiva  el boton  */
							esPreferencia : '=?'
						// AGREGAR VARIABLES SUELTAS, COMO FILTRO, ETC
						},
						templateUrl : 'directives/bcs-grid-atributos/bcs-grid-atributos.html',
						link : function(scope, elem, attrs) {

							scope.postData = {};
							scope.camposAtr = [];
							scope.filtroCombobox = {};
							if(typeof scope.esPreferencia === 'undefined') {scope.esPreferencia = 'N'; }
							
							scope.grupoAtr = [];
							var tabSeleccionado = "";
							
							if (typeof scope.bcsHabilitado === 'undefined') { scope.bcsHabilitado=false; }
							
							var consultaCamposAtributos = function(v_Dato)
					         {
				  		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=63";
						       var r = $resource
						          ("", {}, {
							                 'urlConsulta': { method: "POST", url: urlConsulta}
						                   }
						          );
						       var post = r.urlConsulta(v_Dato);
						       return post.$promise;
					         };
							
					         var parametrosSP = {};
					         parametrosSP.p_IdEmpresa = scope.$root.SesionIdEmpresa;
					         parametrosSP.p_IdEntidad = scope.idEntidad;
					         parametrosSP.p_IdTipoEntidad =  scope.idTipoEntidad;	
					         parametrosSP.p_EsPreferencia = scope.esPreferencia
								
					       
					         
					         getAtributos = function () {
								scope.camposAtrOriginal = [];
								scope.camposAtr = [];
								scope.grupoAtr = [];
						         consultaCamposAtributos(parametrosSP).then(function(result)
								{
									for (var i = 0; i < result.P_OUT_ATRIBUTOSCONTADOR;i++)
									  {
										var campo = {};
										
										campo.IdAtributo = result.P_OUT_ATRIBUTOS[i].IdAtributo;
									    campo.IdTipoAtributo = result.P_OUT_ATRIBUTOS[i].IdTipoAtributo;
									    campo.NombreTipoAtributo = result.P_OUT_ATRIBUTOS[i].NombreTipoAtributo;			    
									    campo.ClaseDato = result.P_OUT_ATRIBUTOS[i].ClaseDato;
									    campo.IdEstadoAutorizado = result.P_OUT_ATRIBUTOS[i].IdEstadoAutorizado;
									    campo.GrupoAtributo = result.P_OUT_ATRIBUTOS[i].GrupoAtributo;
									    
									    if(campo.ClaseDato == 'BIGINT')
									    	campo.ValorAtributo = parseInt(result.P_OUT_ATRIBUTOS[i].ValorAtributo);
									    else
									    	campo.ValorAtributo = result.P_OUT_ATRIBUTOS[i].ValorAtributo;
									    
									    if(campo.ClaseDato == 'COMBO')
									    {
									    	scope.filtroCombobox[campo.IdTipoAtributo] = [];
									    	scope.filtroCombobox[campo.IdTipoAtributo].push({field : "IdTipoAtributo" , operator: "eq", value : parseInt(campo.IdTipoAtributo)});
									    	campo.ValorAtributoDef = result.P_OUT_ATRIBUTOS[i].ValorAtributo;
									    }
									    
									    
									    
									    scope.camposAtr.push(campo);
									   // scope.camposAtrOriginal.push(campo);
									  }		
									for (var i = 0; i < result.P_OUT_GRUPOATRIBUTOSCONTADOR;i++)
									  {
										var campo = {};
									    campo.GrupoAtributo = result.P_OUT_GRUPOATRIBUTOS[i].GrupoAtributo;
									    scope.grupoAtr.push(campo);
									   
									  }

									scope.camposAtrOriginal=angular.copy(scope.camposAtr);
									

									//scope.camposAtrOriginal = $.extend({},scope.camposAtr);
								});
						         
					         };
					         
					         getAtributos();
							
							scope.modificarDatos = function()
							{
								
								
							 if (scope.camposAtr !== scope.camposAtrOriginal)	
							 {	
									var camposMod = [];
									
									for (var i = 0; i < scope.camposAtr.length;i++)
									{
										if(scope.camposAtr[i].GrupoAtributo === tabSeleccionado)
										{
											var campoSel = {};
											campoSel.IdAtributo		=scope.camposAtr[i].IdAtributo;
											campoSel.IdTipoAtributo	= parseInt(scope.camposAtr[i].IdTipoAtributo);
										  if(scope.camposAtr[i].ClaseDato == 'BIGINT'||scope.camposAtr[i].ClaseDato == 'COMBO') 
											  campoSel.ValorAtributo = scope.camposAtr[i].ValorAtributo || '';
										  else
											 campoSel.ValorAtributo	=scope.camposAtr[i].ValorAtributo;
										  
										  if (JSON.stringify(scope.camposAtr[i].ValorAtributo) !== JSON.stringify(scope.camposAtrOriginal[i].ValorAtributo) && scope.camposAtr[i].ValorAtributo!==false )
											camposMod.push(campoSel);
											
											
										}
								    }
						
									console.log(camposMod);
									
									//llamar servicio modificar
									llamarServicioModificarAtributo(camposMod);
								
							 };
								
								
							};
							
							scope.clicEditar = function() {
								scope.bcsHabilitado=true;
								
							};
							
							scope.clicCancelar = function() {
								scope.bcsHabilitado=false;
							};
							
							scope.onTabSelect=function(tabName){
								  tabSeleccionado = tabName;
								}
							
							
							llamarServicioModificarAtributo = function(camposMod) {
									var EventoModificar = [];

						    	   //Parámetros de entrada al servicio
							   	    var datosModificar = {};
							   	    
							   	    datosModificar.p_IdEmpresa			= scope.$root.SesionIdEmpresa; 
							   	    datosModificar.p_IdUsuario			= scope.$root.SesionIdUsuario;
							   	    datosModificar.p_IdModulo			= 'PLA';
							   	    datosModificar.p_UsaReglasNegocio	= 'N';
							   	    datosModificar.p_IdTipoEntidad		= scope.idTipoEntidad;
							   	    datosModificar.p_IdEntidad			= scope.idEntidad;
							   	    datosModificar.p_RegistroAtributo	= camposMod;
				   	    
						      	
							      	console.log(datosModificar);

							    	var successModificar = function(respuesta)
							    	{	  		
							    		if (respuesta.P_OUT_ERRORESCONTADOR == "0")
							    		{
											var i = 0;
											if (respuesta.P_OUT_ADVERTENCIASCONTADOR)
							    				 {
											        for(i; i<respuesta.P_OUT_ADVERTENCIASCONTADOR; i++)
											        {
											        	bcsComunes.msg("warning","Advertencia",respuesta.P_OUT_ADVERTENCIASDETALLE[i].Descripcion);
											        }
											         getAtributos();
											      }
									    		else
									    	    {
									        
											       bcsComunes.msg("info","","Atributos Modificados");
											       getAtributos();
									    	    };
							    		}
							    		else
							    	    {	
							    			//El servicio arrojó errores. 
											var i = 0;
									        for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
									        {
									        	bcsComunes.msg("error","Error",respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);
									        }
									       
							    	    }
							    	};
							    	
							    	var errorModificar = function(respuesta)
							    	{	
							    		console.log("Error:" + JSON.stringify(respuesta));	    		
						       
										bcsComunes.msg("error","Error Interno",JSON.stringify(respuesta));
										//$scope.data.errorpopup(datos);
										
							    	};
							    	

							    	//Llamada a servicio para crear comprobante de facturación()
							    	bcsServicioPlataforma.ModificarAtributo(datosModificar,successModificar,errorModificar);
								
							};
							
							var parseServerResponse = function(response)
							{
								var result = { errores :[],advertencias:[]};
								$.each(response.P_OUT_ADVERTENCIASDETALLE,function(index,value){;		
						  		result.adv.push(value);
								});
								$.each(response.P_OUT_ERRORESDETALLE,function(index,value){;
						  			result.errores.push(value);
								});
							return result;
							} 
							
						}
					};
				});
