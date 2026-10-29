angular.module('ModuloPlataforma').directive('bcsComboboxBase', function($rootScope,$timeout,$translate,bcsConfiguracionPlataforma,bcsComunes,$filter) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			valueField: '=?', // Sobreescribe dataValueField entregado por configuracion del combobox
			textField: '=?', // Sobreescribe dataTextField entregado por configuracion del combobox
			staticItems: '=?', // Agrega opciones fijas, antes de resultados de query y despues de opcion TODOS 
			filtroPorDefecto: '=?', // Callback para obtener la opción por defecto si esta es dinámica, ejemplo combobox formCrearCliente_perfilComercial en CrearCliente.html
			idDefecto : '=?',   //Establece cual va ser el ID inicial. Importante que esten los filtros iniales establecidos.
			idActual  : '=?',   //Da el ID actual seleccionado o establece el id dado. (set)
			seleccionado: '=?', //devuelve el objeto asociado al idActual, si no existe da blanco (get)
			itemDefecto: '=?', // Devuelve el objeto asociado al idDefecto, si no existe da blanco (get)
			tipo: '=?',           //tipo de combobox a ser utilizado
			activado : '=?',     //true/false para el control
			configuracion: '=?', //configuracion extra para el tipo
			filtros: '=?',   //Filtro que se aplicara al data source
			usaFiltrosCompletos: '=?',   //si esta en S, solo llama si estan todos los filtros
			idPostFiltro: '=?', //Luego de aplicarse el filtro se intentará auto seleccionar de la lista el id  indicado
			itemPostFiltro: '=?', //Si no tiene idPostFiltro, no tiene memoria, no tiene iddefecto se seleccionará el elemento indicado por la posición del item 1, a N.
			todos : '=?',    //S/N Agrega a la lista el campo item TODOS
			cascadaA : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascadaValor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascadaDefecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			memoria: '=?',  //Guarda preferencia de la seleccion memoria key + id
			autoCargar: '@?', //Detiene el inicio de datos automaticos al crearse el control.  Para autocompletar usa el idDefecto
			filtroAutomatico: '@?',  //LLama al servidor por datos cuando existen cambios en los filtros. Default true
			cache: '=?',      //Indica si se usara cache para los datos obtenidos
			buscar: '=?',     //Indica el texto por el cual va buscar, caso auto-completar
			foco: '=?',		  //dar foco en el input. 
			enviaTodos: '=?', //si esta en S, envia el valor 0 como filtro al odata
			defaultsMap : '=?',
			cargaClienteAutomatico: '=?', //si esta en S carga el cliente automatico o recordado desde las preferencias; en caso contrario no.
			workingNet: '=?', //undefined=no iniciado   0=inicadi  1=OK   -1=error  componente esta descargando informacion
			autoSeleccion : '=?', //si el combo tiene un solo resultado, se auto selecciona
			incluyeot : '=?',
			//--------------
			cascada1A : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascada1Valor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascada1Defecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			cascada2A : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascada2Valor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascada2Defecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			cascada3A : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascada3Valor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascada3Defecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			cascada4A : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascada4Valor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascada4Defecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			cascada5A : '=?',  //Aplica cascada a un valor de filtros cuando existe un cambio de seleccion
			cascada5Valor : '=?',  //Literal que se aplicara para obtener el elemento de seleccionado
			cascada5Defecto : '=?',  //Valor a ser utilizado en la cascada cuando no existe seleccion
			//--------------
			ngModel : '=?',
			kendoWidget : '=?',
			muestraEnlace : '=?'
		},
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.seleccionado;
			}
		},
		templateUrl: 'directives/bcs-combobox-base/bcs-combobox-base.html',
		link: function(scope, element, attrs, fn) {
			$timeout(function(){
				//Validaciones
				if (typeof scope.tipo === 'undefined') {
					throw "BCS-combobox-base no viene definido tipo";
				}
				//Iniciales
				scope.winkey = element.closest("[winkey]").attr("winkey");
				scope.IDTODOS = 0;
				scope.element = element;
				scope.usaFiltrosCompletos = scope.usaFiltrosCompletos || 'N';
				scope.muestraEnlace = scope.muestraEnlace || 'N';
				scope.log = bcsComunes.log();
				scope.filtros = scope.filtros || {};
				scope.configuracion = scope.configuracion || {};
				if (typeof scope.activado === 'undefined') scope.activado =  true;
				if (scope.activado === "false") scope.activado =  false;
				scope.idPostFiltro = scope.idPostFiltro || false;
				scope.idActual = false;
				scope.LidActual = false;
				scope.seleccionado = false;
				scope.itemDefecto = false;
				scope.busquedaMinima = 0;
				scope.usaLupa = false;
				scope.iniciodefault = true;
				if (typeof scope.autoCargar === 'undefined') scope.autoCargar =  true;
				if (scope.autoCargar==="true" || scope.autoCargar==="'true'" ) scope.autoCargar =  true;
				if (scope.autoCargar==="false" || scope.autoCargar==="'false'") scope.autoCargar = false;
				if (typeof scope.autoFiltro === 'undefined') scope.autoFiltro =  true;
				if (scope.autoFiltro==="true" || scope.autoFiltro==="'true'" ) scope.autoFiltro =  true;
				if (scope.autoFiltro==="false" || scope.autoFiltro==="'false'" ) scope.autoFiltro = false;
				if (typeof scope.cache === 'undefined') scope.cache =  false;
				if (scope.cache==="true" || scope.cache==="'true'") scope.cache =  true;
				if (scope.cache==="false" || scope.cache==="'false'") scope.cache = false;
				if (scope.enviaTodos !== 'S') scope.enviaTodos = 'N';
				if (typeof scope.filtroAutomatico === 'undefined') scope.filtroAutomatico =  true;
				if (scope.filtroAutomatico==="true"   || scope.filtroAutomatico==="'true'" ) scope.filtroAutomatico =  true;
				if (scope.filtroAutomatico==="false" || scope.filtroAutomatico==="'false'") scope.filtroAutomatico = false;
				if (typeof scope.buscar === 'undefined') scope.buscar =  false;		
				if (scope.autoSeleccion==="false" || scope.autoSeleccion==="'false'") scope.autoSeleccion = false;
				
				//Si tiene memoria, iddefecto pero define  set default
				if (typeof scope.memoria !== 'undefined' && typeof scope.idDefecto !== 'undefined') {
					if (typeof sesion.getPreferencias(scope.tipo+'_'+scope.memoria) === 'undefined' ) {
						sesion.setPreferencias(scope.tipo+'_'+scope.memoria,scope.idDefecto);
					}
				}

				scope.findValue = function(value) {
					return _.find(scope.kendoWidget.dataSource.data(), function(key) {
						 return key[scope.kOptions.dataValueField] == value;
						})!== undefined;
					
				}

				//Filtros extras configurado en la base datos
				scope.nombreCtrFormulario=$(element).parents('[ng-controller]').first().attr('ng-controller');				
				scope.filtroExtra=undefined;
				if (bcs_filtrocombobase[scope.nombreCtrFormulario]!=undefined)
					if (bcs_filtrocombobase[scope.nombreCtrFormulario]['ComboBox']!=undefined)
						if (bcs_filtrocombobase[scope.nombreCtrFormulario]['ComboBox'][scope.tipo]!=undefined)
							scope.filtroExtra=JSON.parse(bcs_filtrocombobase[scope.nombreCtrFormulario]['ComboBox'][scope.tipo]);				
				//referencia al widget
				scope.$on("kendoWidgetCreated", function (event, widget) {
					if (widget.$angular_scope === scope) {
						scope.kendoWidget = widget;
						if (typeof scope.memoria !== 'undefined') {
							if (typeof sesion.getPreferencias(scope.tipo+'_'+scope.memoria) !== 'undefined') {
								scope.iniciodefault=false;
							}
						}
						//Setup por control
						if (scope.loadFn!=undefined) {
							var obj = scope.loadFn(scope);
							scope.log.debug(scope.tipo,'kendoWidgetCreated fnLoad',obj);							
							$.extend(scope, obj);
						}
						//Condiciones Inicio
						if ((scope.autoCargar==true || scope.autoCargar=="true") && scope.autocomplete==true && scope.idDefecto!=undefined) {
							scope.log.debug(scope.tipo,'kendoWidgetCreated autoCargar true con autocompleta',scope.idDefecto);
							scope.idActual=false;
							$timeout(function () {  scope.idActual=scope.idDefecto; },10);
						}					
						else if (scope.autocomplete==false && (scope.autoCargar===true || scope.autoCargar=="true")) {
							scope.log.debug(scope.tipo,'kendoWidgetCreated autoCargar true');
							scope.fnRefrescarDatos();
						}
						else if (scope.filtros.read!==undefined && scope.filtros.read==true) {
							scope.log.debug(scope.tipo,'kendoWidgetCreated filtros read true');
							scope.fnRefrescarDatos();
						}
						else {
							scope.log.debug(scope.tipo,'kendoWidgetCreated autoCargar false');
							scope.fnBuscarTexto();
						}
						//Solo para autocomplentar
						if (scope.autocomplete==true) {
							scope.debounce = _.debounce(function() {
								if (scope.kendoWidget._prev != scope.autocompleteText && scope.kendoWidget._prev.length>=scope.defaultOptions.minLength)  {
									scope.log.debug(scope.tipo,'keydown autocompleta',scope.kendoWidget._prev);
									scope.autocompleteText=scope.kendoWidget._prev;
									scope.fnRefrescarDatos();
								}
							},300);
							scope.kendoWidget.input.keydown(function(e){
								if (e.keyCode!=13 && e.keyCode!=9   //Enter/tab
										&& e.keyCode!=37 && e.keyCode!=38 && e.keyCode!=39 && e.keyCode!=40) //Flechas
								  { 
									scope.debounce();
								}
							});
						}						
					}
				});
				//busqueda por texto
				scope.fnBuscarTexto = function(e) {
					if (scope.buscar !== undefined && scope.buscar!==false) {
						scope.log.debug(scope.tipo,'fnBuscarTexto',scope.buscar);
						scope.kendoWidget.text(scope.buscar);
						if (scope.autocomplete==true) {
							scope.autocompleteText=scope.buscar;
							scope.fnRefrescarDatos();
						}
					}
				};				
				/**********************************************************
				 *
				 *   MANEJO DE DATOS CON EL SERVIDOR
				 *
				 * *******************************************************/
				//refresco de datos
				scope.fnRefrescarDatos = function(vid) {
					//Filtros complejos o simples
					if (scope.filtros["complejo"]!==undefined) {
						var nfiltros = $.extend({}, scope.filtros);
						delete nfiltros.complejo;
						if (vid!==undefined) {
					    	var nfiltersvid = {
						      logic: "and",
						      filters: [
						         {"field": scope.kOptions.dataValueField ,"operator":"eq", "value": vid  },
						      ]
					    	};
							nfiltros.filters.push(nfiltersvid);
						}							
						if (vid===undefined && scope.autocomplete==true && scope.autocompleteFn != undefined) {
							scope.autocompleteFn(nfiltros.filters,scope.kendoWidget._prev||'');
						}											
						if (scope.filtroExtra!==undefined) throw 'FILTROS COMPLEJOS AUN NO IMPLEMENTADOS CON FILTROS EXTRAS';
						scope.DataSource.filter( nfiltros );
					}else {
						var nfiltros = new Array();
						var sinFiltros = 0;
						var logicc = undefined;
						for (var prop in scope.filtros) {
						    if (scope.filtros.hasOwnProperty(prop) && (prop != 'read' && prop != 'logic')) {
						    	var object = scope.filtros[prop];
						    	if (object.field !== undefined && object.operator !== undefined && object.value!== undefined && object.value!== "" && ( object.value !== scope.IDTODOS || scope.enviaTodos === 'S' ) ) {
						    		//si enviaTodos esta en S, envia el filtro TODOS
									nfiltros.push({
								        "field"   : object.field,
								        "operator": object.operator,
								        "value"   : object.value
									});
						    	}
						    	else {
						    		sinFiltros++;
						    	}
						    	
						    }
						    if (scope.filtros.hasOwnProperty(prop) && prop == 'logic') {
						    	logicc = scope.filtros[prop];
					    	}
						}
						if (scope.filtroExtra!=undefined) {
							nfiltros.push(scope.filtroExtra);
						}
						if (vid!==undefined) {
					    	var nfiltersvid = {"field": scope.kOptions.dataValueField ,"operator":"eq", "value": vid  };
							nfiltros.push(nfiltersvid);
						}
						if ((typeof scope.incluyeot == 'undefined' && scope.incluyeot != "S") && (scope.tipo == 'combobox-condicion-liquidacion' || scope.tipo == 'combobox-condicion-liquidacion-integracion')){
							nfiltros.push({ "field" : "Codigo", "operator" : "neq", "value" : "OT" });
						}
						if (vid===undefined && scope.autocomplete==true && scope.autocompleteFn != undefined) {
							scope.autocompleteFn(nfiltros,scope.kendoWidget._prev||'');
						}
						if (logicc) {
							scope.DataSource.filter( { logic : logicc , filters : nfiltros } );
						}
						else
							if (scope.usaFiltrosCompletos === 'S' && sinFiltros > 0) {
								//scope.DataSource.data([]); NO SIRVIO, COMO LIMPIAMOS EL COMBO??
								scope.log.debug(scope.tipo,'fnRefrescarDatos faltan filtros para realizar llamada');
							}
							else
								scope.DataSource.filter( nfiltros );
					}
				};

				//inicio de datos - filtros Iniciales antes de llamar
            	scope.fnRequestStart = function(e) {
            		if (typeof scope.kendoWidget === 'undefined') {
            			scope.log.debug(scope.tipo,'fnRequestStart kendoWidget undefined');
            			e.preventDefault();
            		}else {
            			if (scope.autocomplete==true) {
            				 scope.log.debug(scope.tipo,'fnRequestStart autocomplete request');
            			}
            			else if (scope.cache === true ) {
        					var key = scope.tipo+':::'+JSON.stringify( scope.filtros );
        					var data = sessionStorage.getItem(key);
        					if (data != null) {
        						 scope.log.debug(scope.tipo,'fnRequestStart cache internal');
	            				 scope.kendoWidget.setDataSource(new kendo.data.DataSource({ data: JSON.parse(data) , schema: { model : scope.DataSource.options.schema.model  } } ));
	            				 scope.kendoWidget.refresh();
	            				 scope.fnDataProcesa();
        						 e.preventDefault();
        					}else {
        						 scope.log.debug(scope.tipo,'fnRequestStart cache request');
        						 scope.fnCambiaID(false);
        					}
        				}else {
        					scope.log.debug(scope.tipo,'fnRequestStart server');
        					scope.workingNet=0;
        					scope.fnCambiaID(false);
        				}
            	    }
				};
				//procesa datos
				scope.fnDataProcesa = function() {
            		//Valores por default
            		if (scope.iniciodefault && typeof scope.idDefecto !== 'undefined') {
            			scope.iniciodefault=false;
            			scope.log.debug(scope.tipo,'set default ',scope.idDefecto);
            			scope.fnCambiaID(scope.idDefecto);
            		}
            		//cambio de filtro nuevos valores
            		else  {
            			//caso autocomplete
            			if (scope.autocomplete==true) {
            				//caso autocomplete - con texto de busqueda
							if (scope.autocompleteText!=false) {
								scope.log.debug(scope.tipo,'set autocomplete ',scope.autocompleteText);
								if (scope.buscar!=undefined && scope.buscar!==false) {
									var vid=scope.fnGetIDFromText(scope.buscar);
									scope.buscar=false;
									if (vid!=false) {									
										scope.fnCambiaID(vid);
									}
								}else {
									//scope.kendoWidget.search(scope.autocompleteText);
									scope.kendoWidget.open();
								}
							}
							//caso autocomplete - con id-actual
							else if (scope.idActual!=false) {
								scope.log.debug(scope.tipo,'set autocomplete id',scope.idActual);
								scope.fnCambiaID(scope.idActual);
							}						
            			}
            			else {
            				//caso con el postfiltro
							if (typeof scope.idPostFiltro !== 'undefined' && scope.idPostFiltro !== false && scope.findValue(scope.idPostFiltro)) {
            					scope.log.debug(scope.tipo,'set idPostFiltro ',scope.idPostFiltro);
								scope.fnCambiaID(scope.idPostFiltro);
            				}
            				//caso con memoria
            				else if (typeof scope.memoria !== 'undefined' && typeof sesion.getPreferencias(scope.tipo+'_'+scope.memoria) !== 'undefined' && scope.findValue(sesion.getPreferencias(scope.tipo+'_'+scope.memoria))) {
								var newid=sesion.getPreferencias(scope.tipo+'_'+scope.memoria);
								scope.log.debug(scope.tipo,'set memoria ',newid);
								scope.fnCambiaID(newid);
							}
							//caso default
							else if (typeof scope.idDefecto !== 'undefined' && scope.findValue(scope.idDefecto)) {
								scope.log.debug(scope.tipo,'set idDefecto ',scope.idDefecto);
								scope.fnCambiaID(scope.idDefecto);
							}
							//si hay mapa default
							else if (bcsComunes.getDefaultMap(scope.winkey,scope.tipo)
								&& bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo 
								&& scope.filtros[bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo] 
								&& scope.filtros[bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo].value
								&& bcsComunes.getDefaultMap(scope.winkey,scope.tipo)[scope.filtros[bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo].value]) {
									var id = bcsComunes.getDefaultMap(scope.winkey,scope.tipo)[scope.filtros[bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo].value]
									scope.fnCambiaID(id);
								}
							//caso item post filtro
							else if (typeof scope.itemPostFiltro !== 'undefined' && scope.itemPostFiltro>0 && scope.itemPostFiltro <= scope.kendoWidget.dataSource.data().length) {
								var data =scope.kendoWidget.dataSource.data();
								scope.log.debug(scope.tipo,'set itemPostFiltro con elementos');
								var id=data[scope.itemPostFiltro-1][scope.kOptions.dataValueField];
								scope.fnCambiaID(id);
							}
							else if (typeof scope.autoSeleccion !== 'undefined' && scope.kendoWidget.dataSource.data().length === 1) {
								var data =scope.kendoWidget.dataSource.data();
								scope.log.debug(scope.tipo,'set autoSeleccion');
								var id=data[0][scope.kOptions.dataValueField];
								scope.fnCambiaID(id);
							}
							else if (typeof scope.filtroPorDefecto === 'function') {
								scope.log.debug(scope.tipo, 'run filtroPorDefecto');

								const opcionFiltrada = scope.filtroPorDefecto(scope.kendoWidget.dataSource.data())

								if (opcionFiltrada !== null && opcionFiltrada !== undefined) {
									scope.fnCambiaID(opcionFiltrada[scope.kOptions.dataValueField]);
								}
							}
							//Caso default
							else {
								scope.log.debug(scope.tipo,'set SIN memoria false');
								scope.fnCambiaID(false);
							}
            			}
            		}
				};

				//respuesta de datos
				scope.fnRequestEnd = function(e) {
	            		var result = [];
	            		//Llamada sin respuesta de datos.
	            		if (typeof e.response === 'undefined') {
	            			scope.workingNet=-1;
	            			return;
	            		}
	            		
	            		//para el caso que el datasource sea un arreglo
	            		if (angular.isArray(e.response)) {
							e.response.d = { results : e.response , '__count' : e.response.length };
						}

	            		//Errora al procesar 1000 datos
	            		if (e.response.d.length>1000) {
	            			scope.workingNet=-1;
	            			throw(scope.tipo,'muchos datos>1000,filtros mal definidos',e.response.d.length);
	            		}
						//Si define items fijos agregarlos
						if (Array.isArray(scope.staticItems)) {
							const elementosIncluidos = new Set();
							for (const staticItem of scope.staticItems.reverse()) {
								if (staticItem.value && staticItem.text && elementosIncluidos.has(staticItem.value) === false) {
									e.response.d.results.unshift(
										{
											[scope.kOptions.dataValueField]: staticItem.value,
											[scope.kOptions.dataTextField]: $translate.instant(staticItem.text),
										}
									);
								}
							}
						}
		            	//Si se define el atributo todos="'S'" en el objeto, agrega la opción "Todos" al inicio del combo
	            		if (typeof scope.todos !== 'undefined' && scope.todos === "S") {
	            			var object = {};
	            			object[scope.kOptions.dataValueField]=scope.IDTODOS;
	            			object[scope.kOptions.dataTextField]=' '+  $translate.instant('UI_TODOS');
							e.response.d.results.unshift(object);
						}
	            		//Elimina duplicados
	            		if (scope.permiteRepetidos !== true)
	            			result=$filter('unique')(e.response.d.results,scope.kOptions.dataValueField);
	            		else {
	            			result = e.response.d.results;
	            		}

                        //evaluar si contiene un JSON
	            		if (!!result) {
	            		    
                            try {
                                var obj =  result[0];
                                var json = JSON.parse(obj[scope.kOptions.dataValueField]);
                                var llaves = {};
                                var codigos = obj.CodigoDetalleIndicador.split("||");
                                for (var key in codigos) {
                                    var indicador = codigos[key].split("-");
                                    llaves[indicador[0]] = indicador[1];
                                }
                                result = [];
                                for (var prop in json) {
                                    if (json.hasOwnProperty(prop)) {
                                        var t = $.extend({}, obj);
                                        if (json[prop] === true) {
                                            t[scope.kOptions.dataTextField] = llaves[prop];
                                            t[scope.kOptions.dataValueField] = prop;
                                            result.push(t);
                                        }
                                    }
                                }

                            } catch (e) {
                            }
                        }
						
	            		//Genera una estructura nueva a partir de una funcion
	            		if (scope.KdataTextTemplate!=undefined) {
							for (i=0;i<e.response.d.results.length;i++)
							{
								e.response.d.results[i].KdataTextTemplate=scope.KdataTextTemplate(e.response.d.results[i]);
							}
	            		}
	            		//asigna datasource
	            		scope.log.debug(scope.tipo,'fnRequestEnd datos obtenidos',result.length);
	            		scope.kendoWidget.setDataSource(new kendo.data.DataSource({ data: result , schema: { model : scope.DataSource.options.schema.model  } } ));
	            		scope.kendoWidget.refresh();
	            		if (scope.cache === true ) {
	            			var key = scope.tipo+':::'+JSON.stringify( scope.filtros );
	            			sessionStorage.setItem(key,JSON.stringify(result));
	            		}
	            		//data procesa
	            		scope.fnDataProcesa();
	            		scope.workingNet=1;
	            };
	            
	            /**********************************************************
				 *
				 *   MANEJO DE LUPA
				 *
				 * *******************************************************/
	            scope.ext = {};
	            scope.ext.fnLupa = function() {
	            	var params = {};
	            	params.parametros = scope.defLupa.parametros;
	            	if (_.isFunction(scope.defLupa.parametros)) {
	            		params.parametros = scope.defLupa.parametros();
	            	}

	            	params.filtros = scope.filtros;

					var sinFiltros = 0;
					for (var prop in scope.filtros) {
						if (scope.filtros.hasOwnProperty(prop) && (prop != 'read' && prop != 'logic')) {
							var object = scope.filtros[prop];
							if (object.field !== undefined && object.operator !== undefined && object.value!== undefined && object.value!== "" && ( object.value !== scope.IDTODOS || scope.enviaTodos === 'S' ) ) {
							}
							else {
								sinFiltros++;
							}

						}
					}
					
					if (sinFiltros > 0 && scope.usaFiltrosCompletos === "S") {
						return;
					}


	            	var id1 = bcsComunes.ventanaAbrir(scope.defLupa.ventana, params,false,false,scope.ventanaid);
					bcsComunes.ventanaPromesa(id1).then(function(data){
						console.log(data);
						if (data === null || data === undefined || (typeof data === 'object' && _.isEmpty(data))) {
							return;
						}
						scope.idActual = data;
					});
	            	
	            }
	            
				/**********************************************************
				 *
				 *   MANEJO DE EVENTOS DEL COMBO
				 *
				 * *******************************************************/
	            //Get ID by Text
				 scope.fnGetIDFromText = function(text) {					 
					var data =scope.kendoWidget.dataSource.data();
					var sel = false;
					for (i=0;i<data.length;i++) {
            			if (data[i][scope.kOptions.dataTextField].indexOf(text) == 0) {
            				return data[i][scope.kOptions.dataValueField];
            			}		            		
					}
					return false;
				 }		            							
				//dato seleccionado
				 scope.fnCambiaID = function(vid) {
				   $rootScope.safeApply(function() {  
						if (vid===false) {
		            		scope.idActual=false;
		            		scope.LidActual=false;
		            		scope.seleccionado=false;
		            		scope.kendoWidget.value('');
							if (typeof scope.cascadaA  !== 'undefined') scope.cascadaA.value=scope.cascadaDefecto;
							if (typeof scope.cascada1A !== 'undefined') scope.cascada1A.value=scope.cascada1Defecto;
							if (typeof scope.cascada2A !== 'undefined') scope.cascada2A.value=scope.cascada2Defecto;
							if (typeof scope.cascada3A !== 'undefined') scope.cascada3A.value=scope.cascada3Defecto;
							if (typeof scope.cascada4A !== 'undefined') scope.cascada4A.value=scope.cascada4Defecto;
							if (typeof scope.cascada5A !== 'undefined') scope.cascada5A.value=scope.cascada5Defecto;
						}
						else {
							var data =scope.kendoWidget.dataSource.view();
							var sel = false;
							for (i=0;i<data.length;i++) {
		            			if (data[i][scope.kOptions.dataValueField] == vid ) {
		            				scope.kendoWidget.select(i);
		            				//scope.kendoWidget.value(vid);
		            				scope.seleccionado=data[i];
		            				if (scope.idDefecto == vid) scope.itemDefecto = data[i];
		            				if (scope.changeFn!==undefined) scope.changeFn(scope.seleccionado);
		            				scope.idActual=data[i][scope.kOptions.dataValueField];
		            				scope.LidActual=scope.idActual;
									if (typeof scope.memoria !== 'undefined') sesion.setPreferencias(scope.tipo+'_'+scope.memoria,scope.idActual);
									if (typeof scope.cascadaA !== 'undefined') scope.cascadaA.value=scope.seleccionado[scope.cascadaValor];
									if (typeof scope.cascada1A !== 'undefined') scope.cascada1A.value=scope.seleccionado[scope.cascada1Valor];
									if (typeof scope.cascada2A !== 'undefined') scope.cascada2A.value=scope.seleccionado[scope.cascada2Valor];
									if (typeof scope.cascada3A !== 'undefined') scope.cascada3A.value=scope.seleccionado[scope.cascada3Valor];
									if (typeof scope.cascada4A !== 'undefined') scope.cascada4A.value=scope.seleccionado[scope.cascada4Valor];
									if (typeof scope.cascada5A !== 'undefined') scope.cascada5A.value=scope.seleccionado[scope.cascada5Valor];
		            				sel=true;
								}
		            		}
		            		if (!sel) {
		            			scope.fnCambiaID(false);
								if (typeof scope.cascadaA !== 'undefined') scope.cascadaA.value=scope.seleccionado[scope.cascadaValor] || '';
								if (typeof scope.cascada1A !== 'undefined') scope.cascada1A.value=scope.seleccionado[scope.cascada1Valor] || '';
								if (typeof scope.cascada2A !== 'undefined') scope.cascada2A.value=scope.seleccionado[scope.cascada2Valor] || '';
								if (typeof scope.cascada3A !== 'undefined') scope.cascada3A.value=scope.seleccionado[scope.cascada3Valor] || '';
								if (typeof scope.cascada4A !== 'undefined') scope.cascada4A.value=scope.seleccionado[scope.cascada4Valor] || '';
								if (typeof scope.cascada5A !== 'undefined') scope.cascada5A.value=scope.seleccionado[scope.cascada5Valor] || '';
		        			}
						}
					});
				 };
				//dato seleccionado
				scope.fnSelect = function(e) {

					
					if (e.item === null || e.item.index() === -1) {
						scope.fnCambiaID(false);
						return;
					}
					var dataItem = this.dataItem(e.item.index());
					var datakey  = dataItem[scope.kOptions.dataValueField];
					if (typeof scope.kendoWidget !== 'undefined' && scope.idActual!==datakey) {
						scope.log.debug(scope.tipo,'fnSelect',datakey,scope.kendoWidget.value());
						scope.fnCambiaID(datakey);
					}
				};
				//dato cambiado - podria ser seleccionado si coincide
				 scope.fnChange = function(e) {
					if (typeof scope.kendoWidget !== 'undefined' && scope.kendoWidget.selectedIndex == -1 && scope.idActual!==scope.kendoWidget.value()) {
						scope.log.debug(scope.tipo,'fnChange normal',scope.kendoWidget.value());
						var data =scope.kendoWidget.dataSource.data();
						var sel = false;
						if (scope.kendoWidget.text() && scope.kendoWidget.text().length >= scope.busquedaMinima) {
							for (i=0;i<data.length;i++) {								
								if (scope.compareFn(data[i][scope.kOptions.dataTextField],scope.kendoWidget.text(),data[i])===true) {										
	            					scope.fnCambiaID(data[i][scope.kOptions.dataValueField]);
	            					sel=true;
	            					break;									
		            			}								
		            		}
						}
	            		if (!sel) {
	            			scope.fnCambiaID(false);
	            		}
	            		scope.$apply();
					}
					else if (typeof scope.kendoWidget !== 'undefined' && scope.kendoWidget.selectedIndex != -1) {
						var data =scope.kendoWidget.dataSource.view();
						var id   =data[scope.kendoWidget.selectedIndex][scope.kOptions.dataValueField];
						if (id !== scope.idActual) {
							scope.log.debug(scope.tipo,'fnChange sugerido',id);
							scope.fnCambiaID(id);
							scope.$apply();
						}
					}
				};
				//Configuracion de filtros que van a ser usados
				scope.$watch('filtros', function(newValue, oldValue) {
					if (typeof scope.kendoWidget === 'undefined') return;
					if (angular.equals(newValue,oldValue)) return;
					if (newValue===undefined) return;
					if (newValue.read===false) {
						var obj1= $.extend( {}, newValue);
						var obj2= $.extend( {}, oldValue);
						if (obj1.read !== undefined) delete obj1.read;
						if (obj2.read !== undefined) delete obj2.read;
						if (angular.equals(obj1,obj2)) return;
					}
					scope.log.debug(scope.tipo,'set filtros',newValue);
					if (scope.filtros.read ===true) {
						scope.filtros.read=false;
						scope.fnRefrescarDatos();	
					}
					else if (scope.filtroAutomatico===true) {
						scope.fnRefrescarDatos();						
					}
				},true);
				//Dato a ser utilizado luego de traer los datos
				scope.$watch('idPostFiltro', function(newValue, oldValue) {
					if (typeof scope.kendoWidget === 'undefined') return;
					if (angular.equals(newValue,oldValue)) return;
					if (newValue===undefined) return;
					if (newValue!==false) {
						scope.log.debug(scope.tipo,'set idPostFiltro',newValue);
						if (scope.autocomplete==true) {
							scope.fnRefrescarDatos(scope.idActual);
							if (newValue==scope.idActual) scope.idPostFiltro=false;
							scope.fnCambiaID(newValue);
						}else {
							scope.fnRefrescarDatos();
						}
					}
				},true);
				//Cambiaron el valor por API
				scope.$watch('idActual', function(newValue, oldValue) {
					if (typeof scope.kendoWidget !== 'undefined') {
						if (angular.equals(newValue,oldValue)) return;
						if (newValue===undefined) scope.idActual = '';
						if (scope.idActual===false) scope.fnCambiaID(false);
						if (scope.idActual!==false &&
							scope.idActual!==scope.LidActual &&
							scope.kendoWidget.value()!==scope.idActual)
						{
							scope.log.debug(scope.tipo,'set idActual',scope.idActual,scope.kendoWidget.value());
							if (scope.autocomplete==true) {
								//Es equivalente al id-postfiltro, dado que no tengo los datos
								scope.autocompleteText=false;
								scope.idPostFiltro=scope.idActual;
							}else {
								scope.fnCambiaID(scope.idActual);								
							}
						}
					}
				});
				//Busca en el combo
				scope.$watch('buscar', function(newValue, oldValue) {
					if (typeof scope.kendoWidget !== 'undefined') {
						if (angular.equals(newValue,oldValue)) return;
						if (scope.newValue===false) return;
						if (newValue===undefined) return;
						scope.fnBuscarTexto();
					}
				});		
				//Pone el foco
				scope.$watch('foco', function(newValue){
					if (typeof scope.kendoWidget !== 'undefined') {
						if (newValue == true) { 
							scope.kendoWidget.input.focus();
							scope.foco=false;
						}
					}
	            });				
				/**********************************************************
				 *
				 *   CONFIGURACION
				 *
				 * *******************************************************/
				var aplicaConfiguracion = function(config) {
					scope.log.debug(scope.tipo,'getConfiguracion',config);
					if (scope.muestraEnlace === 'S' && config.enlace) {
						scope.tieneEnlace = 'S';
						scope.enlaceVentana = config.enlace.ventana;
						scope.enlaceParametros = config.enlace.parametros;
						scope.element.find('i.enlace').bind('click', function(e) {
							var ventana = scope.enlaceVentana;
							var params = undefined ;
							if (!scope.seleccionado) return;
							$.each(scope.enlaceParametros,function(key,val){
								if (!params) params = {};
								params[val] = scope.seleccionado[val];
							});
							if (ventana && params)
								bcsComunes.ventanaAbrir(ventana,params,false,false);

						});
					}
					if (config.fnIconoAlerta) {
						scope.mostrarAlerta = config.fnIconoAlerta;
						scope.tooltipText = config.tooltipTextAlerta;
					} else {
						scope.mostrarAlerta = function() {
							return false;
						}
					}
					if (config.permiteRepetidos === true)
						scope.permiteRepetidos = true;
					if (config.defLupa) {
						scope.usaLupa = true;
						scope.defLupa = config.defLupa;
					}

					
					scope.defaultOptions = $.extend( {}, config, scope.configuracion);
					scope.busquedaMinima = scope.defaultOptions.minLength||1;
					scope.KdataTextTemplate = config.KdataTextTemplate;
					scope.defaultOptions.minLength=scope.busquedaMinima;
					if (config.template) scope.defaultOptions.template = config.template;
					scope.defaultOptions.suggest = true;
					scope.defaultOptions.autoBind = true;
					scope.defaultOptions.clearButton = false;
					scope.defaultOptions.noDataTemplate = '';
					scope.defaultOptions.height = 150;
					scope.defaultOptions.ignoreCase = true;					
					scope.compareFn=config.compareFn;
					if (scope.compareFn===undefined) {
						scope.compareFn=function(x,y,row) {
							if (x.toUpperCase().trim().indexOf(y.toUpperCase().trim())===0) return true;
							return false;
						};
					}
					scope.loadFn=config.loadFn;		
					scope.changeFn=config.changeFn;	
					//dataSource
					scope.DataSource = jQuery.extend(true, {}, scope.defaultOptions.dataSource);
					scope.DataSource.options.serverFiltering = true;
					scope.DataSource.options.serverSorting   = true;
					scope.DataSource.options.serverPaging    = false;
					scope.DataSource.bind("requestEnd",   scope.fnRequestEnd);
					scope.DataSource.bind("requestStart", scope.fnRequestStart);
					if (config.autocomplete==true) {
						scope.cache = false;							
						scope.autocomplete=true;
						scope.autocompleteFn=config.autocompleteFn;
						scope.autocompleteText=false;
						scope.DataSource.options.serverPaging    = true;
						scope.DataSource.pageSize(50);
					}else {
						scope.autocomplete=false;
					}
					//Desvinculo el combo del datasource
					scope.defaultOptions.dataSource = undefined;
					//eventos
					scope.defaultOptions.select 	 = scope.fnSelect;
					scope.defaultOptions.change      = scope.fnChange;
					scope.defaultOptions.autoWidth = true;
					//kOptions
					scope.kOptions = scope.defaultOptions;
				}
				var p = bcsConfiguracionPlataforma.getConfiguracion(scope.tipo);
				if (p && p.then) {
					p.then(function(result){
						aplicaConfiguracion(result);
					});
				}
				else if (p) {
					aplicaConfiguracion(p);
				}
				else {
					throw "error al cargar definicion de combo";
				}

			});
		}
	};
});
