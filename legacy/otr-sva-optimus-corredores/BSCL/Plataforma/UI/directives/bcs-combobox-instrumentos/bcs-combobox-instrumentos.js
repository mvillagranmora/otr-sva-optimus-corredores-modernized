angular.module('ModuloPlataforma').directive('bcsComboboxInstrumentos', function($modal,$translate,$rootScope,$timeout) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			datosInstrumento : '=?',
			buscarInstrumento: '=?',
			muestraPrecio : '=?',
			kOptions: '=?',
			ngModel : '=?',
			kNgModel : '=?',
			kendoWidget : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			todos : '=?',
			filtroTipoInstrumento: '=?',
			filtroEstado: '=?',
			filtroClasificacion: '=?',
			filtroEmisor: '=?',
			filtroMoneda: '=?',
			filtroBolsa: '=?',
			filtroFechaPrecio: '=?',
			filtroSubyacente: '=?'
		},
		template:'<span class="k-textbox k-space-right" style="width: 100%;" >'+
			     '   <input class="bcs-autocomplete" kendo-auto-complete="kendoWidget"'+  
			     '                                k-ng-readonly="kNgReadonly" '+
			     '                                k-ng-disabled="kNgDisabled"'+ 
			     '                                k-options="kOptions" '+
			     '                                ng-model="ngModel" '+
			     '                                k-ng-model="kNgModel"'+
			     '                                k-ng-delay="kOptions"'+
			     '                                style="text-transform:uppercase!important;" '+
			     '                                />                    '+
			     '   <a href="#" ng-click="fnBusquedaInstrumento()" class="k-select k-icon k-i-search">&nbsp;</a>'+
				 '</span>'+
				 '<div ng-show="muestraPrecio===true">{{\'UI_NEMO\'|translate}}: {{datosInstrumento.Nemotecnico}} - '+ 
				 '{{\'UI_COMPRA\'|translate}}: {{datosInstrumento.MejorPrecioCompra| kformat:\'c\':orden.IdMonedaOrden:\'precio\':\'ORD\'}} - {{\'UI_VENTA\'|translate}}: {{datosInstrumento.MejorPrecioVenta| kformat:\'c\':orden.IdMonedaOrden:\'precio\':\'ORD\'}} - {{\'ULTIMO_TRANSADO\'|translate}}: {{datosInstrumento.UltimoPrecioTransado| kformat:\'c\':orden.IdMonedaOrden:\'precio\':\'ORD\'}} - {{\'UI_MONEDA\'|translate}}: {{datosInstrumento.IdMoneda}}</div>',
		controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosInstrumento && $scope.datosInstrumento.IdInstrumento);
			}
		},		
		link: function(scope, element, attrs, fn) {
			$timeout(function(){			
			//Valores iniciales
			scope.datosInstrumento={};
			scope.busquedaMinima = 2;				
			scope.postData = {};
			scope.ngModel='';
			if (scope.todos===true) scope.titulo=$translate.instant('UI_TODOS');
			else scope.titulo=$translate.instant('UI_INSTRUMENTO');	
			if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
			if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;			
			//Evento de creacion del widget
			scope.$on("kendoWidgetCreated", function (event, widget) {
				if (widget.$angular_scope === scope) {
					//widget
					scope.kendoWidget = widget;
					scope.kendoWidget.popupCerrar = false;		
					//perdida del foco
					scope.kendoWidget.element.on("blur", function() {
						scope.kendoWidget.popupCerrar = true;				
						scope.kendoWidget.close();
						scope.kendoWidget.popupCerrar = false;
					});	
				}					
			});			
			
			//Busqueda de Instrumento 
			scope.fnBusquedaInstrumento = function(e) {
				//TODO: siguiente sprint
				
			};
			
			scope.$watch("datosInstrumento",function(newval,oldval) {
				if (newval===false) { //clean datos
					scope.datosInstrumento = { };
					scope.kendoWidget.value("");
				}
			});
			
			scope.$watch('buscarInstrumento', function(newValue, oldValue) {
				if (scope.buscarInstrumento !== false) {
					scope.ngModel = scope.buscarInstrumento;
					if (scope.kendoWidget !== undefined) scope.kendoWidget.value(scope.buscarInstrumento);
					scope.kOptions.dataSource.read();	
					scope.buscarInstrumento=false;
				}
			});	
						
			var defaultOptions =
			{
					placeholder: scope.titulo,
					headerTemplate: '<div class="noDataMessage">No se encontrarón resultados.</div>',
		            dataTextField: "Nemotecnico",
		            dataValueField: "IdInstrumento",
		            filter: "startswith",
		            autoBind: false,
		            height: 300,
		            template: $.proxy(kendo.template("#= util.fnFormatHighlight(Nemotecnico, KInputVal ) #"), this.dataSource),
					dataBound: function(e) {
					  console.log("bcs-combobox-instrumentos event :: dataBound",e);
					  var noItems = this.list.find(".noDataMessage");
					  if (!this.dataSource.view()[0] && scope.kendoWidget.element.is(":focus")) {
						noItems.show();
						this.popup.open();						
					  } else {
						noItems.hide();
					  }
					},		
					open: function(e) {
						console.log("bcs-combobox-instrumentos event :: open",e);
					},						
					close: function(e) {
						console.log("bcs-combobox-instrumentos event :: close",e);
						var widget = e.sender;
						if (scope.ngModel.length>0 && !widget.popupCerrar && !this.dataSource.view()[0]) {
							e.preventDefault();
						}
					},		
					filtering: function(e) {
						console.log("bcs-combobox-instrumentos event :: filtering",e);
						if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {
							this.popup.close();	
							e.preventDefault();
							scope.datosInstrumento={};
						}
					},					
					select: function(e) {
						console.log("bcs-combobox-instrumentos event :: select",e);
						var dataItem = this.dataItem(e.item.index());
						scope.datosInstrumento=dataItem;
					},    
					change: function(e) {
						console.log("bcs-combobox-instrumentos event :: change",e);
					},    					
		            dataSource: new kendo.data.DataSource({		            			            
		            	type: "json",
		            	serverFiltering: true,		            	
		            	serverSorting: false, 
		            	serverPaging : false,
                        transport : {                                                                          
                                     read: function(options) {
                                          $.ajax({
                                          url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=17&r=p_out_BolsaInstrumentoPrecios",
                                          dataType: "json", 
                                          type: "POST",
                                          contentType:"application/json; charset=utf-8",
                                          data: JSON.stringify(scope.postData),
                                          success: function(result) { 
                                        	  var temp=scope.ngModel.toUpperCase();
                                              options.success(result);   
                                              //scope.ngModel=temp;
                                              scope.kendoWidget.value(temp);
                                          },
                                          error: function(result) {
                                            options.error(result);
                                          }});
                                    }                                                                                
                        },  
		            	requestStart: function(e) {
		            		console.log("bcs-combobox-instrumentos event :: requestStart",e);
							//filtros Iniciales antes de llamar		  
		            		var texto=(scope.ngModel + '').replace(/%/g, '~');
							if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {
								e.preventDefault();	
								scope.datosInstrumento={};
							}else {
								//Parametros de la llamada
								scope.postData.p_IdEmpresa = $rootScope.SesionIdEmpresa;
								scope.postData.p_FiltroInstrumento =  (scope.ngModel||'').toUpperCase();								
								scope.postData.p_FiltroTipoInstrumento =  scope.filtroTipoInstrumento  || undefined;
								scope.postData.p_FiltroClasificacion =  scope.filtroClasificacion || undefined;
								scope.postData.p_FiltroEstado  = scope.filtroEstado;
								scope.postData.p_FiltroEmisor =  scope.filtroEmisor;
								scope.postData.p_FiltroMoneda  = scope.filtroMoneda;
								scope.postData.p_FiltroBolsa =  scope.filtroBolsa || undefined;
								scope.postData.p_MuestraPrecio =  (scope.muestraPrecio)?'S':'N'||'N';
								scope.postData.p_FiltroSubyacente =  scope.filtroTipoInstrumento === 'SIM'? scope.filtroTipoInstrumento : scope.filtroSubyacente  || undefined;;
							}									
						},                         
		            	requestEnd: function(e) {
		            		console.log("bcs-combobox-instrumentos event :: requestEnd",e);
		            		if (typeof e.response !== 'undefined') {
			            		//Procesa datos
								var result = [];  
								var autosel = false;
								for (i=0;i<e.response.length;i++)
								{
									e.response[i].KInputVal=scope.ngModel;
									result.push(e.response[i]);
									//Auto selecciona
									if (scope.ngModel.toUpperCase()===e.response[i].Nemotecnico.toUpperCase()) {
										scope.datosInstrumento=e.response[i];
										autosel=true;
									}
								}
								if (!autosel) {
									scope.datosInstrumento={};
								}
								e.response = result;	
		            		}
		            	}            
		            })
			};						
			scope.kOptions = scope.kOptions || {};
			scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions );	
		});
		}
	};
});
