angular.module('ModuloPlataforma').directive('bcsComboboxMultiseleccion',
    function ($rootScope,$timeout,$translate, bcsConfiguracionPlataforma,bcsComunes,$filter) {

        return {
            restrict: 'E',
            replace: false,
            scope: {
                // Igual que el base: indica qué definición usar
                tipo       : '=?',

                // Modelo de salida: array de IDs seleccionados
                ngModel    : '=?',

                // Habilitado / deshabilitado
                activado   : '=?',
                filtros: '=?', //Filtro que se aplicara al data source
                // Si quieres sobreescribir los campos de la configuración
                valueField : '@?',
                textField  : '@?',
                ngBlur	: '&?',
            },
            controller : function($scope){
				this.getSeleccionado = function(){
					let v = $scope.ngModel;
					// Si es array y está vacío => tráelo como null (inválido)
			        if (Array.isArray(v) && v.length === 0) {
			            return null;
			        }
			        // Si es null/undefined => también lo tratamos como sin valor
			        if (v === null || v === undefined) {
			            return null;
			        }
			
			        // Con al menos 1 elemento => valor "presente"
			        return v;
				}
			},
            templateUrl: 'directives/bcs-combobox-multiseleccion/bcs-combobox-multiseleccion.html',

            link: function (scope, element, attrs) {

                $timeout(function () {

                    if (!scope.tipo) {
                        throw "bcs-combobox-multiseleccion requiere atributo 'tipo'";
                    }
                    scope.log = bcsComunes.log();
                    scope.configuracion = scope.configuracion || {};
                    scope.filtros = scope.filtros || {};
                    
                    if (attrs.filtros) {
			            scope.$parent.$watch(attrs.filtros, function (nuevo, antiguo) {
			                if (!scope.kendoWidget) return;
			                scope.filtros = nuevo || {};
			                scope.fnRefrescarDatos();
			            }, true);
			        }
                    scope.seleccionado = scope.seleccionado || [];
                    scope.busquedaMinima = 0;
                    scope.itemDefecto = false;
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
					
                    // Activado por defecto
                    if (typeof scope.activado === 'undefined' ||
                        scope.activado === 'true'  ||
                        scope.activado === true) {
                        scope.activado = true;
                    } else {
                        scope.activado = false;
                    }

                    scope.ngModel = scope.ngModel || [];

                    // ---------- Helpers internos ----------
                    // Cada vez que cambie el modelo (los IDs seleccionados), actualizamos
					scope.$watchCollection('ngModel', function (nuevo, anterior) {
					    if (!scope.multiWidget) return; // aún no existe el widget
					
					    $timeout(function () {
					        syncCheckboxes();
					        updateDisplayText();
					    });
					});

                    // Sincroniza el estado de los checkboxes según los valores seleccionados
                    function syncCheckboxes() {
                        let ms = scope.kendoWidget;
                        if (!ms) return;

                        let values = ms.value() || [];
                        let view   = ms.dataSource.view();

                        ms.ul.find("li").each(function (idx, li) {
                            let dataItem = view[idx];
                            if (!dataItem) return;
                            let val = dataItem[scope.valueField];
                            let checked = values.indexOf(val) !== -1;

                            $(li).find("input[type=checkbox]").prop("checked", checked);
                        });
                    }

                    // Construye el texto en el input separado por "; "
                    function updateDisplayText() {
                        let ms = scope.kendoWidget;
                        if (!ms) return;

                        var values = ms.value() || [];
                        var textos = [];

                        // Todos los registros cargados en el MultiSelect
					    let data = ms.dataSource.data();
					
					    // Por cada valor seleccionado, buscamos el registro correspondiente
					    values.forEach(function (val) {
					        for (let i = 0; i < data.length; i++) {
					            if (data[i][scope.valueField] === val) {
					                textos.push(data[i][scope.textField]);
					                break;
					            }
					        }
					    });

                        // Escribimos el texto en el input del MultiSelect
                        ms.input.val(textos.join('; '));
                    }

                    // Handler de cambio
                    function onChange(e) {
                        let ms = this;

                        // Copiamos valores al ngModel (array de IDs)
                        scope.$applyAsync(function () {
                            scope.ngModel = ms.value() || [];
                        });

                        // Actualizamos checkboxes y texto
                        syncCheckboxes();
                        updateDisplayText();
                    }

                    // Handler al abrir y al databound
                    function onPopupSync(e) {
                        syncCheckboxes();
                        updateDisplayText();
                    }
                    
                    // Handler al cerrar el popup
					function onClose(e) {
					    // Kendo limpia el input al cerrar; lo volvemos a setear nosotros
					    $timeout(function () {
					        syncCheckboxes();
					        updateDisplayText();
					    }, 0);
					}

                    //referencia al widget
					scope.$on("kendoWidgetCreated", function (event, widget) {
						if (widget.$angular_scope === scope) {
							scope.kendoWidget = widget;
							if (typeof scope.memoria !== 'undefined') {
								if (typeof sesion.getPreferencias(scope.tipo+'_'+scope.memoria) !== 'undefined') {
									scope.iniciodefault=false;
								}
							}
							if (widget instanceof kendo.ui.MultiSelect) {
								scope.multiWidget = widget;
					            // Template del ítem con checkbox
					            let itemTemplate =
					                '<span class="bcs-ms-checkbox-item">' +
					                    '<input type="checkbox"/>' +
					                    '<span style="display:inline-block; margin-left:6px;">#= data["' + scope.textField + '"] #</span>' +
					                '</span>';
					
					            widget.setOptions({
					                autoClose     : false,   // no cerrar al seleccionar
					                valuePrimitive: true,    // array de valores primitivos
					                itemTemplate  : itemTemplate
					            });
					
					            // Eventos para mantener checkboxes + texto actualizado
					            widget.bind("change", onChange);
					            widget.bind("open", onPopupSync);
					            widget.bind("dataBound", onPopupSync);
					            widget.bind("close", onClose);
					            
					            // Sincronizar una vez por si ya hay valores en ngModel
					            $timeout(function () {
					                syncCheckboxes();
					                updateDisplayText();
					            });
					        }
							
							//Setup por control
							if (scope.loadFn!=undefined) {
								let obj = scope.loadFn(scope);
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
					//refresco de datos
					scope.fnRefrescarDatos = function(vid) {
						//Filtros complejos o simples
						if (scope.filtros["complejo"]!==undefined) {
							let nfiltros = $.extend({}, scope.filtros);
							delete nfiltros.complejo;
							if (vid!==undefined) {
						    	let nfiltersvid = {
							      logic: "and",
							      filters: [
							         {"field": scope.msOptions.dataValueField ,"operator":"eq", "value": vid  },
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
							let nfiltros = new Array();
							let sinFiltros = 0;
							let logicc = undefined;
							for (let prop in scope.filtros) {
							    if (scope.filtros.hasOwnProperty(prop) && (prop != 'read' && prop != 'logic')) {
							    	let object = scope.filtros[prop];
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
						    	let nfiltersvid = {"field": scope.msOptions.dataValueField ,"operator":"eq", "value": vid  };
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
	        					let key = scope.tipo+':::'+JSON.stringify( scope.filtros );
	        					let data = sessionStorage.getItem(key);
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
					//dato seleccionado
					 scope.fnCambiaID = function(vid) {
					   $rootScope.safeApply(function() {  
							if (vid===false) {
			            		scope.idActual=false;
			            		scope.LidActual=false;
			            		scope.seleccionado=[];
			            		scope.kendoWidget.value('');
							}
							else {
								let data =scope.kendoWidget.dataSource.view();
								let sel = false;
								for (i=0;i<data.length;i++) {
			            			if (data[i][scope.msOptions.dataValueField] == vid ) {
			            				//scope.kendoWidget.select(i);
			            				//scope.kendoWidget.value(vid);
			            				
			            				let yaEsta = scope.seleccionado.some(function (item) {
					                        return item[scope.msOptions.dataValueField] === vid;
					                    });
					                    if (!yaEsta) {
					                        scope.seleccionado.push(data[i]);   // 👈 AHORA SÍ
					                    }
			            				scope.seleccionado.push(data[i]);
			            				if (scope.idDefecto == vid) scope.itemDefecto = data[i];
			            				if (scope.changeFn!==undefined) scope.changeFn(scope.seleccionado);
			            				scope.idActual=data[i][scope.msOptions.dataValueField];
			            				scope.LidActual=scope.idActual;
										sel=true;
									}
			            		}
			            		if (!sel) {
			            			scope.fnCambiaID(false);
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
						let dataItem = this.dataItem(e.item.index());
						let datakey  = dataItem[scope.msOptions.dataValueField];
						if (typeof scope.kendoWidget !== 'undefined' && scope.idActual!==datakey) {
							scope.log.debug(scope.tipo,'fnSelect',datakey,scope.kendoWidget.value());
							scope.fnCambiaID(datakey);
						}
					};
					//dato cambiado - podria ser seleccionado si coincide
					 scope.fnChange = function(e) {
						if (typeof scope.kendoWidget !== 'undefined' && scope.kendoWidget.selectedIndex == -1 && scope.idActual!==scope.kendoWidget.value()) {
							scope.log.debug(scope.tipo,'fnChange normal',scope.kendoWidget.value());
							let data =scope.kendoWidget.dataSource.data();
							let sel = false;
							if (scope.kendoWidget.text() && scope.kendoWidget.text().length >= scope.busquedaMinima) {
								for (i=0;i<data.length;i++) {								
									if (scope.compareFn(data[i][scope.msOptions.dataTextField],scope.kendoWidget.text(),data[i])===true) {										
		            					scope.fnCambiaID(data[i][scope.msOptions.dataValueField]);
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
							let data =scope.kendoWidget.dataSource.view();
							let id   =data[scope.kendoWidget.selectedIndex][scope.msOptions.dataValueField];
							if (id !== scope.idActual) {
								scope.log.debug(scope.tipo,'fnChange sugerido',id);
								scope.fnCambiaID(id);
								scope.$apply();
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
										let vid=scope.fnGetIDFromText(scope.buscar);
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
									let newid=sesion.getPreferencias(scope.tipo+'_'+scope.memoria);
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
										let id = bcsComunes.getDefaultMap(scope.winkey,scope.tipo)[scope.filtros[bcsComunes.getDefaultMap(scope.winkey,scope.tipo).campo].value]
										scope.fnCambiaID(id);
									}
								//caso item post filtro
								else if (typeof scope.itemPostFiltro !== 'undefined' && scope.itemPostFiltro>0 && scope.itemPostFiltro <= scope.kendoWidget.dataSource.data().length) {
									let data =scope.kendoWidget.dataSource.data();
									scope.log.debug(scope.tipo,'set itemPostFiltro con elementos');
									let id=data[scope.itemPostFiltro-1][scope.msOptions.dataValueField];
									scope.fnCambiaID(id);
								}
								else if (typeof scope.autoSeleccion !== 'undefined' && scope.kendoWidget.dataSource.data().length === 1) {
									let data =scope.kendoWidget.dataSource.data();
									scope.log.debug(scope.tipo,'set autoSeleccion');
									let id=data[0][scope.msOptions.dataValueField];
									scope.fnCambiaID(id);
								}
								else if (typeof scope.filtroPorDefecto === 'function') {
									scope.log.debug(scope.tipo, 'run filtroPorDefecto');
	
									const opcionFiltrada = scope.filtroPorDefecto(scope.kendoWidget.dataSource.data())
	
									if (opcionFiltrada !== null && opcionFiltrada !== undefined) {
										scope.fnCambiaID(opcionFiltrada[scope.msOptions.dataValueField]);
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
					
					scope.fnBlur = function(e) {
						if (scope.ngBlur) scope.ngBlur();
					};
                    //respuesta de datos
					scope.fnRequestEnd = function(e) {
		            		let result = [];
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
												[scope.msOptions.dataValueField]: staticItem.value,
												[scope.msOptions.dataTextField]: $translate.instant(staticItem.text),
											}
										);
									}
								}
							}
			            	//Si se define el atributo todos="'S'" en el objeto, agrega la opción "Todos" al inicio del combo
		            		if (typeof scope.todos !== 'undefined' && scope.todos === "S") {
		            			let object = {};
		            			object[scope.msOptions.dataValueField]=scope.IDTODOS;
		            			object[scope.msOptions.dataTextField]=' '+  $translate.instant('UI_TODOS');
								e.response.d.results.unshift(object);
							}
		            		//Elimina duplicados
		            		if (scope.permiteRepetidos !== true)
		            			result=$filter('unique')(e.response.d.results,scope.msOptions.dataValueField);
		            		else {
		            			result = e.response.d.results;
		            		}
	
	                        //evaluar si contiene un JSON
		            		if (!!result) {
		            		    
	                            try {
	                                let obj =  result[0];
	                                let json = JSON.parse(obj[scope.msOptions.dataValueField]);
	                                let llaves = {};
	                                let codigos = obj.CodigoDetalleIndicador.split("||");
	                                for (let key in codigos) {
	                                    let indicador = codigos[key].split("-");
	                                    llaves[indicador[0]] = indicador[1];
	                                }
	                                result = [];
	                                for (let prop in json) {
	                                    if (json.hasOwnProperty(prop)) {
	                                        let t = $.extend({}, obj);
	                                        if (json[prop] === true) {
	                                            t[scope.msOptions.dataTextField] = llaves[prop];
	                                            t[scope.msOptions.dataValueField] = prop;
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
		            			let key = scope.tipo+':::'+JSON.stringify( scope.filtros );
		            			sessionStorage.setItem(key,JSON.stringify(result));
		            		}
		            		//data procesa
		            		scope.fnDataProcesa();
		            		scope.workingNet=1;
		            };
                    // ---------- Cargar configuración desde bcsConfiguracionPlataforma ----------

                    function aplicaConfiguracion(config) {
						scope.log.debug(scope.tipo,'getConfiguracion',config);
                        // Clonamos opciones por defecto, igual que en bcs-combobox-base
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
						if (config.permiteRepetidos === true)
							scope.permiteRepetidos = true;
						if (scope.compareFn===undefined) {
							scope.compareFn=function(x,y,row) {
								if (x.toUpperCase().trim().indexOf(y.toUpperCase().trim())===0) return true;
								return false;
							};
						}
						scope.loadFn=config.loadFn;
						
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
						//msOptions
						scope.msOptions = scope.defaultOptions;
                        // Campos de texto y valor
                        scope.valueField = scope.valueField || scope.defaultOptions.dataValueField || 'Id';
                        scope.textField  = scope.textField  || scope.defaultOptions.dataTextField  || 'Nombre';

                        // DataSource clonado
                        let dsConfig = jQuery.extend(true, {}, scope.defaultOptions.dataSource);
                        dsConfig.serverFiltering = true;
                        dsConfig.serverSorting   = true;
                        dsConfig.serverPaging    = false;

                        let dataSource = new kendo.data.DataSource(dsConfig);
                        
                        
                        // ---- Opciones del MultiSelect con CHECKBOX ----
						scope.msOptions = $.extend({}, scope.defaultOptions, {
						    autoClose     : false,              // No cerrar al seleccionar
						    valuePrimitive: true,               // ngModel = array de valores primitivos
						    dataSource    : dataSource,
						    dataTextField : scope.textField,
						    dataValueField: scope.valueField,
						
						    // Template de cada ítem con checkbox + texto
						    itemTemplate:
						        '<span class="bcs-ms-checkbox-item">' +
						            '<input type="checkbox" />' +
						            '<span style="display:inline-block; margin-left:6px;">#: data.' + scope.textField + ' #</span>' +
						        '</span>',
						
						    // Encadenamos la lógica original + la nuestra
						    change: function (e) {
						        if (scope.fnChange) {
						            scope.fnChange(e);   // comportamiento que ya tenías
						        }
						        onChange.call(this, e);  // sincroniza ngModel, texto y checkboxes
						    },
						
						    open     : onPopupSync,      // al abrir: marcar checks y actualizar texto
						    dataBound: onPopupSync       // al cargar datos: igual
						});
                    }

                    // Carga la configuración (igual que en bcs-combobox-base)
                    let p = bcsConfiguracionPlataforma.getConfiguracion(scope.tipo);
                    if (p && p.then) {
                        p.then(function(result){
							aplicaConfiguracion(result);
						});
                    } else if (p) {
                        aplicaConfiguracion(p);
                    } else {
                        throw "No se encontró configuración para combobox tipo '" + scope.tipo + "'";
                    }
                });
            }
        };
    });
